import java.io.*;
import java.net.Socket;

//essa classe trata cada cliente que se conecta ao servidor


public class ClientHandler extends Thread{
    //final torna constante
    private final Socket clientSocket;
  //  private final String secretWord;
  //  private String hint;
    private BufferedReader input;
    private PrintWriter output;
    private int score = 0;
    private static final int ROUNDS = 5; //se quiser mudar a quantidade de rounds só trocar aqui

    public ClientHandler(Socket socket){
        this.clientSocket = socket;
      //  this.secretWord = word;
      //  this.hint = hint;
    }

    @Override
    public void run(){
        try{
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            try {
                output = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            output.println("Bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas para jogar.");

            for (int i = 0; i < ROUNDS; i++) {
                String[] wordInfo = GameServer.getRandomWord();
                String secretWord = wordInfo[0];
                String hint = wordInfo[1];
                output.println("\nRodada " + (i + 1) + " de " + ROUNDS);
                output.println("Dica: " + hint);
                output.println("A palavra tem " + secretWord.length() + " letras.");
                boolean acertou = false;

                //sempre vai entrar aqui da primeira vez por causa do boolean false
                while (!acertou) {
                    output.println("Digite a palavra completa: ");
                    String guess = input.readLine();
                    if (guess == null) {
                        System.out.println("Jogador desconectado.");
                        return;
                    }
                    guess = guess.trim().toUpperCase();

                    if (guess.isEmpty()) {
                        output.println("Entrada inválida. Digite uma palavra válida.");
                        continue;
                    }

                    if (guess.equals(secretWord)) {
                        score += 10;
                        output.println("Parabéns! Você acertou a palavra: " + secretWord);
                        output.println("Sua pontuação atual: " + score + " pontos.");
                        acertou = true; //mudando boolean aqui pra nao precisar digitar a palavra de novo
                    } else {
                        output.println("Palavra errada :( Tente de novo.");
                        output.println("Pontuação atual: " + score + " pontos.");
                    }
                }
            }

            output.println("Fim do jogo! Sua pontuação final foi: " + score + " pontos.");
            System.out.println("Jogador finalizou o jogo com " + score + " pontos.");
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
