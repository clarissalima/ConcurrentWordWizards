import java.io.*;
import java.net.*;
import java.util.Random;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    private static final int PORT = 12345;
    private static final String[][] WORDS = {

            {"JAVA", "Linguagem de programação orientada a objetos."},
            {"SOCKET", "Usado para comunicação em rede."},
            {"THREAD", "Execução paralela dentro de um programa."},
            {"NETWORK", "Conjunto de computadores interconectados."}

    };
    private String secretWord;
    private String hint;


    public GameServer(){
        //escolhendo palavras aleatoria pro jogo
        Random random = new Random();
        int index = random.nextInt(WORDS.length);
        secretWord = WORDS[index][0]; //palavra a ser adivinhada
        hint = WORDS[index][1]; //dica para exatamente aquela palavra


    }

    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Servidor iniciado na porta: " + PORT);

            //aceitando varios clientes enquanto servidor fica listen
            while(true){
                System.out.println("Aguardando jogador...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo jogador conectado");

                //criando nova thread pra cliente ou clientes q conectaram
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            while (true) {
                System.out.println("Aguardando jogador...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo jogador conectado!");

                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String[] getRandomWord() {
        Random random = new Random();
        return WORDS[random.nextInt(WORDS.length)];
    }


}
