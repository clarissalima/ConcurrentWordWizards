import java.io.*;
import java.net.Socket;

// Essa classe trata cada cliente que se conecta ao servidor
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private int score = 0;
    private long startTime;
    private long endTime;
    public static final int ROUNDS = 4;
    private final int playerNumber;
    private final Partida partida;
    private boolean terminou = false;

    public ClientHandler(Socket socket, int playerNumber, Partida partida) {
        this.clientSocket = socket;
        this.playerNumber = playerNumber;
        this.partida = partida;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            output.println("Bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas para jogar.");
            startTime = System.currentTimeMillis();

            for (int i = 0; i < ROUNDS; i++) {
                String[] wordInfo = partida.getPalavraDaRodada(i);
                String secretWord = wordInfo[0];
                String hint = wordInfo[1];

                output.println("\nRodada " + (i + 1) + " de " + ROUNDS);
                output.println("Dica: " + hint);
                output.println("A palavra tem " + secretWord.length() + " letras.");
                boolean acertou = false;

                while (!acertou) {
                    output.println("Digite a palavra completa: ");
                    String guess = input.readLine();
                    if (guess == null) {
                        System.out.println("Jogador " + playerNumber + " desconectado.");
                        return;
                    }
                    guess = guess.trim().toUpperCase();

                    if (guess.isEmpty()) {
                        output.println("Entrada inválida. Digite uma palavra válida.");
                        continue;
                    }

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
            terminou = true;
            System.out.println("Jogador " + playerNumber + " finalizou o jogo com " + score + " pontos.");
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
}