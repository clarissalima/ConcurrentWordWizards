import java.io.*;
import java.net.Socket;

//essa classe trata cada cliente que se conecta ao servidor
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private int score = 0;
    // Publico para que gameserver possa acessar
    public static final int ROUNDS = 4;  // se quiser mudar a quantidade de rounds só trocar aqui
    private final int playerNumber;

    public ClientHandler(Socket socket, int playerNumber) {
        this.clientSocket = socket;
        this.playerNumber = playerNumber;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            output.println("Bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas para jogar.");

            for (int i = 0; i < ROUNDS; i++) {

                // Pega a palavra da rodada do servidor
                String[] wordInfo = GameServer.getPalavraDaRodada(i);
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

            output.println("Fim do jogo! Sua pontuação final foi: " + score + " pontos.");
            System.out.println("Jogador " + playerNumber + " finalizou o jogo com " + score + " pontos.");
        } catch (IOException e) {
            System.out.println("Erro na comunicação com o jogador.");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}