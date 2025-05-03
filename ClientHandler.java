import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

// Essa classe trata cada cliente que se conecta ao servidor
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    BufferedReader input;
    PrintWriter output;
    int score = 0;
    private long startTime;
    private long endTime;
    public static final int ROUNDS = 4;
    final int playerNumber;
    private final Partida partida;
    private boolean jogadorTerminou = false;

    public ClientHandler(Socket socket, int playerNumber, Partida partida) {
        this.clientSocket = socket;
        this.playerNumber = playerNumber;
        this.partida = partida;
        List<Socket> listaDeSockets = List.of();
        partida.setSockets(listaDeSockets);
    }


    @Override
    public void run() {
        jogadorTerminou = false;
        TelaDeJogo tela = null;

        try {
            // Inicialização da interface
            try {
                tela = new TelaDeJogo("", "");
                tela.exibirTela();
            } catch (Exception e) {
                System.out.println("Erro ao criar interface do jogador " + playerNumber + ": " + e.getMessage());
                return;
            }

            // Configuração de I/O
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            output.println("Bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas para jogar.");
            startTime = System.currentTimeMillis();

            // Inicia temporizador (apenas para o primeiro jogador)
            if (playerNumber == 1) {
                partida.setSockets(Collections.singletonList(clientSocket));  // Lista imutável com 1 socket
                partida.iniciarTemporizador();  // Usar método da partida, não do GameServer
            }

            // Rodadas do jogo
            for (int i = 0; i < ROUNDS && !partida.estaEncerrada(); i++) {
                String[] wordInfo = partida.getPalavraDaRodada(i);
                String secretWord = wordInfo[0];
                String hint = wordInfo[1];

                // Atualiza interface e envia informações
                tela.atualizarTela(secretWord, hint);
                output.println("\nRodada " + (i + 1) + " de " + ROUNDS);
                output.println("Dica: " + hint);
                output.println("A palavra tem " + secretWord.length() + " letras.");

                boolean acertou = false;
                while (!acertou && !partida.estaEncerrada()) {
                    String guess = tela.getPalpite();

                    // Verifica se o jogo foi encerrado durante a espera
                    if (partida.estaEncerrada()) {
                        break;
                    }

                    if (guess == null || guess.isEmpty()) {
                        continue;
                    }

                    guess = guess.trim().toUpperCase();

                    // Verifica a adivinhação
                    if (guess.equals(secretWord)) {
                        score += 10;
                        output.println("Parabéns, jogador " + playerNumber + "! Você acertou: " + secretWord);
                        output.println("Pontuação: " + score);
                        acertou = true;
                    } else {
                        output.println("Palavra errada. Tente novamente!");
                    }
                }
            }

            // Finalização
            endTime = System.currentTimeMillis();
            output.println("Sua pontuação final: " + score);

            // Registra no ranking
            boolean ultimo = partida.registrarRanking(playerNumber, score, endTime - startTime);
            System.out.println("Jogador " + playerNumber + " finalizou com " + score + " pontos");

            // Se for o último jogador, envia ranking
            if (ultimo) {
                String ranking = partida.obterRankingFinal();
                partida.enviarRankingParaTodos(ranking);
            }

        } catch (IOException e) {
            System.out.println("Erro na comunicação com jogador " + playerNumber + ": " + e.getMessage());
        } finally {
            jogadorTerminou = true;
            partida.verificaTerminoJogo();

            // Fecha recursos
            if (tela != null) {
                tela.dispose();
            }
            encerrar();
        }
    }

    public void enviarRanking(String rankingFinal) {
        if (output != null) {
            output.println(rankingFinal);
        }
    }

    public void encerrar() {
        try {
            if (output != null) {
                output.println("O jogo acabou! Obrigado por jogar.");
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Erro ao encerrar conexão com o jogador " + playerNumber);
        }
    }

    public boolean terminou() {
        return jogadorTerminou;
    }


}