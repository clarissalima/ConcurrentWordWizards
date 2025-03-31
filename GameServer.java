import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    private static final int PORT = 12345;
    private static final String[][] WORDS = {
            {"JAVA", "Linguagem de programação orientada a objetos."},
            {"SOCKET", "Usado para comunicação em rede."},
            {"TCP", "Protocolo de comunicação confiável, orientado à conexão."},
            {"UDP", "Protocolo de comunicação sem conexão e não confiável."},
            {"JOIN", "Metodo usado para garantir que uma thread espere pela conclusão de outra thread."},
            {"THREAD", "Execução paralela dentro de um programa."},
            {"DEADLOCK", "Situação onde dois ou mais processos ficam bloqueados esperando uns aos outros."},
            {"NETWORK", "Conjunto de computadores interconectados."}
    };

    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final List<String[]> palavrasRodadas = new ArrayList<>();
    private static int totalPlayers = 0;
    private static final int GAME_DURATION_SECONDS = 30;
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

        System.out.println("O temporizador de 1 minuto começou.");
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
        Random random = new Random();
        List<String[]> shuffledWords = new ArrayList<>(Arrays.asList(WORDS));
        Collections.shuffle(shuffledWords, random);

        for (int i = 0; i < ClientHandler.ROUNDS; i++) {
            palavrasRodadas.add(shuffledWords.get(i));
        }
    }

    public static String[] getPalavraDaRodada(int rodada) {
        return palavrasRodadas.get(rodada);
    }
}
