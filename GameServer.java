import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
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

    private static final List<ClientHandler> clients = new ArrayList<>();
    private static int totalPlayers = 0;


    public GameServer(){
        //escolhendo palavras aleatoria pro jogo
        Random random = new Random();
        int index = random.nextInt(WORDS.length);
        secretWord = WORDS[index][0]; //palavra a ser adivinhada
        hint = WORDS[index][1]; //dica para exatamente aquela palavra


    }


    //start tinha sido feito pro single client, nao ta sendo mais usado
//    public void start(){
//        try(ServerSocket serverSocket = new ServerSocket(PORT)){
//            System.out.println("Servidor iniciado na porta: " + PORT);
//
//            //aceitando varios clientes enquanto servidor fica listen
//            while(true){
//                System.out.println("Aguardando jogador...");
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("Novo jogador conectado");
//
//                //criando nova thread pra cliente ou clientes q conectaram
//                new ClientHandler(clientSocket).start();
//            }
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            // Perguntar quantos jogadores participarão
            System.out.print("Quantos jogadores vão participar? ");
            totalPlayers = Integer.parseInt(consoleInput.readLine().trim());

            // Esperar os jogadores conectarem
            while (clients.size() < totalPlayers) {
                System.out.println("Aguardando jogador...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo jogador conectado!");


                //tem que gerar uma aleatoria pra cada rodada
                // Criar uma palavra aleatória para o jogador
                String[] wordInfo = getRandomWord();
                //instância do ClientHandler com: socket aceito, palavra, dica e numero do jogador (ver classe Client Handler)
                ClientHandler clientHandler = new ClientHandler(clientSocket, wordInfo[0], wordInfo[1], clients.size() + 1);
                clients.add(clientHandler);

                // Iniciar a thread do jogador
            //    clientHandler.start();
            }

            System.out.println("Todos os jogadores conectados! O jogo começou.");

//tava fazendo ficar com a mesma palavra
//            // Iniciar o jogo para todos os jogadores
            for (ClientHandler client : clients) {
                client.start();
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