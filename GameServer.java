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
    private static final List<RankingJogador> ranking = new ArrayList<>();
    private static int totalPlayers = 0;
    private static int GAME_DURATION_SECONDS;
    //agenda tarefa que vai ser chamado depois do GMAE_DURATION_SECONDS: vai chamar o encerrarJogo()
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static int jogadoresFinalizados = 0;
    private static boolean jogoEncerrado = false;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            System.out.print("Escolha o modo de jogo (Fácil, Médio, Difícil): ");
            String modo = consoleInput.readLine().trim().toLowerCase();
            definirModoJogo(modo);


            System.out.print("Quantos jogadores vão participar? ");
            totalPlayers = Integer.parseInt(consoleInput.readLine().trim());


            gerarPalavrasParaRodadas(modo);

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


    private static void definirModoJogo(String modo) {
        switch (modo) {
            case "fácil":
                GAME_DURATION_SECONDS = 180;
                break;

            case "facil":
                GAME_DURATION_SECONDS = 180;
                break;

            case "médio":
                GAME_DURATION_SECONDS = 120;
                break;

            case "medio":
                GAME_DURATION_SECONDS = 120;
                break;

            case "difícil":
                GAME_DURATION_SECONDS = 60;
                break;

            case "dificil":
                GAME_DURATION_SECONDS = 60;
                break;

            default:
                System.out.println("Modo inválido! Definindo como Médio por padrão.");
                GAME_DURATION_SECONDS = 120;
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
        if (jogoEncerrado) return;
        jogoEncerrado = true;
        try {
            String rankingFinal = getRankingFinal();
            System.out.println(rankingFinal); // Mostrar no terminal do servidor

            for (ClientHandler client : clients) {
                client.enviarRanking(rankingFinal);
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

    private static void gerarPalavrasParaRodadas(String modo) {
        //Random random = new Random();
        List<String[]> shuffledWords = PalavrasDoJogo.getPalavrasPorDiculdade(modo);
        //Collections.shuffle(shuffledWords, random);

        for (int i = 0; i < ClientHandler.ROUNDS; i++) {
            palavrasRodadas.add(shuffledWords.get(i));
        }
    }

    public static String[] getPalavraDaRodada(int rodada) {
        return palavrasRodadas.get(rodada);
    }

    //classe auxiliar para armazenar dados de pontuação e tempo de cada jogador
    static class RankingJogador{
        int playerNumber;
        int score;
        long tempo;

        RankingJogador(int playerNumber, int score, long tempo) {
            this.playerNumber = playerNumber;
            this.score = score;
            this.tempo = tempo;
        }
    }

    // add no rancking quando um jogador acaba de responder
    public static synchronized void registrarRanking(int playerNumber, int score, long tempoTotal){
        ranking.add(new RankingJogador(playerNumber, score, tempoTotal));
    }

    //verifica se todos os jogadores finalizaram o jogo --> se sim, encerra o jogo para todos
    //nao estava conseguindo colocar o ranking pros jogadores que tinham encerrado primeiro sem isso
    public static synchronized void verificaTerminoJogo() {
        jogadoresFinalizados++;
        if (jogadoresFinalizados == totalPlayers) {
            encerrarJogo();
        }
    }

    //gera e retorna uma string com o ranking final ordenado por pontuação
    public static synchronized String getRankingFinal() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n ---------------- Ranking Final ---------------\n");

        if (ranking.isEmpty()) {
            sb.append("Nenhum jogador terminou a tempo.\n");
            return sb.toString();
        }

        ranking.sort(Comparator
                .comparingInt((RankingJogador r) -> r.score).reversed() // primeiro ordena pela pontuação
                .thenComparingLong(r -> r.tempo)); //ordena pelo tempo

        // ranking dos tres melhores
        for (int i = 0; i < Math.min(3, ranking.size()); i++) {
            RankingJogador r = ranking.get(i);
            sb.append(String.format("%dº lugar - Jogador %d | Pontos: %d | Tempo: %.2f segundos\n",
                    i + 1, r.playerNumber, r.score, r.tempo / 1000.0));
        }

        return sb.toString();
    }
}
