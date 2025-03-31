import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    //como nao temos autorizacao IANA, devemos usar uma porta entre 49152 e 65535
    private static final int PORT = 49160;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final List<String[]> palavrasRodadas = new ArrayList<>();
    private static int totalPlayers = 0;
    private static final int GAME_DURATION_SECONDS = 30;
    //agenda tarefa que vai ser chamado depois do GMAE_DURATION_SECONDS: vai chamar o encerrarJogo()
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            System.out.print("Quantos jogadores vão participar? ");
            totalPlayers = Integer.parseInt(consoleInput.readLine().trim());


            gerarPalavrasParaRodadas();

            while (clients.size() < totalPlayers) {
                System.out.println("Aguardando jogador...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo jogador conectado!");

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size() + 1);
                clients.add(clientHandler);
            }

            System.out.println("Todos os jogadores conectados! O jogo começou.");

            iniciarTemporizador();
            for (ClientHandler client : clients) {
                client.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void iniciarTemporizador() {
        scheduler.schedule(() -> {
            System.out.println("Tempo acabou! Encerrando o jogo.");
            encerrarJogo();
        }, GAME_DURATION_SECONDS, TimeUnit.SECONDS);

        System.out.println("O temporizador de " + GAME_DURATION_SECONDS + " segundos começou.");
    }

    private static void encerrarJogo() {
        try {
            for (ClientHandler client : clients) {
                client.encerrar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdown();
            System.out.println("Jogo encerrado para todos os jogadores.");
            System.exit(0);
        }
    }

    private static void gerarPalavrasParaRodadas() {
        //Random random = new Random();
        List<String[]> shuffledWords = PalavrasDoJogo.getPalavrasEmbaralhadas();
        //Collections.shuffle(shuffledWords, random);

        for (int i = 0; i < ClientHandler.ROUNDS; i++) {
            palavrasRodadas.add(shuffledWords.get(i));
        }
    }

    public static String[] getPalavraDaRodada(int rodada) {
        return palavrasRodadas.get(rodada);
    }
}
