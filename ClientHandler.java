import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Collections;

public class ClientHandler extends Thread {
    final Socket clientSocket;
    BufferedReader input;
    PrintWriter output;
    int score = 0;
    private long startTime;
    private long endTime;
    public static final int ROUNDS = 4;
    final int playerNumber;
    private final Partida partida;
    private boolean jogadorTerminou = false;
    private String nomeJogador;

    public ClientHandler(Socket socket, int playerNumber, Partida partida) {
        this.clientSocket = socket;
        this.playerNumber = playerNumber;
        this.partida = partida;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    @Override
    public void run() {
        jogadorTerminou = false;
        TelaDeJogo tela = null;

        try {
            // 1. Inicializa streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // 2. Recebe nome do jogador
            output.println("DIGITE_SEU_NOME");
            nomeJogador = input.readLine();
            if (nomeJogador == null || nomeJogador.trim().isEmpty()) {
                nomeJogador = "Jogador" + playerNumber;
            }
            System.out.println("Jogador conectado: " + nomeJogador);

            // 3. Cria interface de jogo
            tela = new TelaDeJogo("", "");
            tela.exibirTela();

            output.println(nomeJogador + ", bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas.");
            startTime = System.currentTimeMillis();

            // 4. Configura sockets se for o primeiro jogador
            if (playerNumber == 1 && partida.totalPlayers > 1) {
                partida.setSockets(Collections.singletonList(clientSocket));
            }

            // 5. Rodadas do jogo
            for (int i = 0; i < ROUNDS && !partida.estaEncerrada(); i++) {
                String[] wordInfo = partida.getPalavraDaRodada(i);
                String secretWord = wordInfo[0];
                String hint = wordInfo[1];

                tela.atualizarTela(secretWord, hint);
                output.println("\nRodada " + (i + 1) + "/" + ROUNDS);
                output.println("Dica: " + hint);

                boolean acertou = false;
                while (!acertou && !partida.estaEncerrada()) {
                    String guess = tela.getPalpite();
                    if (partida.estaEncerrada()) break;
                    if (guess == null || guess.isEmpty()) continue;

                    guess = guess.trim().toUpperCase();
                    if (guess.equals(secretWord)) {
                        score += 10;
                        output.println("Acertou! +10 pontos (Total: " + score + ")");
                        acertou = true;
                    } else {
                        output.println("Errado! Tente novamente");
                    }
                }

                if (partida.estaEncerrada()) break;
            }

            // 6. Fim de jogo
            endTime = System.currentTimeMillis();
            output.println("FIM DE JOGO! Pontuação: " + score);

            boolean ultimo = partida.registrarRanking(playerNumber, score, endTime - startTime);
            if (ultimo) {
                String ranking = partida.obterRankingFinal();
                partida.enviarRankingParaTodos(ranking);
                System.out.println("Ranking enviado para todos os jogadores");
            }

        } catch (IOException e) {
            System.out.println("Erro com jogador " + nomeJogador + ": " + e.getMessage());
        } finally {
            jogadorTerminou = true;
            partida.verificaTerminoJogo();

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
            System.out.println("Erro ao encerrar conexão com o jogador " + nomeJogador);
        }
    }

    public boolean terminou() {
        return jogadorTerminou;
    }

    public void enviarMensagem(String mensagem) {
        if (output != null) {
            output.println(mensagem);
            output.flush();
        } else {
            System.err.println("Erro: output não inicializado para jogador " + nomeJogador);
        }
    }
}
