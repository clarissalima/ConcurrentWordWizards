import javax.swing.*;
import java.io.*;
import java.net.Socket;
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
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            output.println("Bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas para jogar.");
            startTime = System.currentTimeMillis();

            if (playerNumber == 1) {
                new GameServer().iniciarCronometro(Partida.getGameDuration(), partida.getSockets());
            }

            // Rodadas do jogo
            for (int i = 0; i < ROUNDS; i++) {
                String[] wordInfo = partida.getPalavraDaRodada(i);
                String secretWord = wordInfo[0];  // palavra secreta
                String hint = wordInfo[1];       // dica

                output.println("\nRodada " + (i + 1) + " de " + ROUNDS);
                output.println("Dica: " + hint);
                output.println("A palavra tem " + secretWord.length() + " letras.");

                // Criar e exibir a interface gráfica para o jogador
                TelaDeJogo tela = new TelaDeJogo(secretWord, hint);
                tela.exibirTela();  // Exibe a interface para o jogador

                boolean acertou = false;

                while (!acertou) {
                    String guess = tela.getPalpite();  // Obter o palpite da interface gráfica

                    if (guess == null) {
                        System.out.println("Jogador " + playerNumber + " desconectado.");
                        return;
                    }

                    guess = guess.trim().toUpperCase();

                    if (guess.isEmpty()) {
                        output.println("Entrada inválida. Digite uma palavra válida.");
                        continue;
                    }

                    // Verifica a adivinhação
                    if (guess.equals(secretWord)) {
                        score += 10;
                        output.println("Parabéns, jogador " + playerNumber + "! Você acertou a palavra: " + secretWord);
                        output.println("Sua pontuação atual: " + score + " pontos.");
                        acertou = true;
                    } else {
                        output.println("Palavra errada :( Tente de novo.");
                        output.println("Pontuação atual: " + score + " pontos.");
                    }
                }
            }

            output.println("Sua pontuação final foi: " + score + " pontos.");
            endTime = System.currentTimeMillis();
            partida.registrarRanking(playerNumber, score, endTime - startTime);


            boolean ultimo = partida.registrarRanking(playerNumber, score, endTime - startTime);


            System.out.println("Jogador " + playerNumber + " finalizou o jogo com " + score + " pontos.");


            if (ultimo) {
                String ranking = partida.obterRankingFinal(); // ALTERAÇÃO
                partida.enviarRankingParaTodos(ranking);      // ALTERAÇÃO
            }


        } catch (IOException e) {
            System.out.println("Erro na comunicação com o jogador.");
        } finally {
            partida.verificaTerminoJogo();
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