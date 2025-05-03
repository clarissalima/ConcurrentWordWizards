import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.Socket;
import java.util.List;

public class Partida {
    private final int id;
    final String modo;
    final int totalPlayers;
    final List<ClientHandler> clients = new ArrayList<>();
    private final List<String[]> palavrasRodadas = new ArrayList<>();
    private final List<RankingJogador> ranking = new ArrayList<>();
    private int jogadoresFinalizados = 0;
    private boolean jogoEncerrado = false;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static int GAME_DURATION_SECONDS;


    public Partida(int id, String modo, int totalPlayers) {
        this.id = id;
        this.modo = modo;
        this.totalPlayers = totalPlayers;
        definirModoJogo(modo);
        gerarPalavrasParaRodadas(modo);
    }
    private List<Socket> sockets;

    public void setSockets(List<Socket> sockets) {
        this.sockets = sockets;
    }

    public List<Socket> getSockets() {
        return sockets;
    }
    // Métodos da partida (similar aos métodos estáticos que estavam em GameServer)
    private static void definirModoJogo(String modo) {
        switch (modo) {
            case "fácil":
            case "facil":
            case "Facil":
            case "Fácil":
                GAME_DURATION_SECONDS = 180;
                break;

            case "médio":
            case "medio":
            case "Medio":
            case "Médio":
                GAME_DURATION_SECONDS = 120;
                break;

            case "difícil":
            case "dificil":
            case "Difícil":
            case "Dificil":
                GAME_DURATION_SECONDS = 60;
                break;

            default:
                System.out.println("Modo inválido! Definindo como Médio por padrão.");
                GAME_DURATION_SECONDS = 120;
        }
    }

    private void gerarPalavrasParaRodadas(String modo) {
        this.palavrasRodadas.clear();
        List<String[]> palavrasSelecionadas = PalavrasDoJogo.getPalavrasPorDiculdade(modo);
        this.palavrasRodadas.addAll(palavrasSelecionadas);


        System.out.println("Partida " + id + " - Modo: " + modo +
                " - Palavras carregadas: " + palavrasRodadas.size());
        if (!palavrasRodadas.isEmpty()) {
            System.out.println("Primeira palavra." +
                    " - Dica: " + palavrasRodadas.get(0)[1]);
        }
    }

    public String[] getPalavraDaRodada(int rodada) {
        return palavrasRodadas.get(rodada);
    }

    public synchronized boolean registrarRanking(int playerNumber, int score, long tempoTotal) {
        // Criação de um novo objeto de RankingJogador para cada jogador
        RankingJogador rankingJogador = new RankingJogador(playerNumber, score, tempoTotal);
        ranking.add(rankingJogador);

        if (clients.size() == totalPlayers) {
            // Verificar se é o último jogador, se sim, encerra a partida e envia o ranking
            return playerNumber == totalPlayers;  // RETORNA TRUE APENAS PARA O ÚLTIMO JOGADOR
        }
        return false;
    }

    public synchronized void verificaTerminoJogo() {
        if (clients.stream().allMatch(ClientHandler::terminou)) {
            jogoEncerrado = true; // Corrigido: O nome da variável era `encerrado` e estava errado.
            System.out.println("A partida " + id + " foi encerrada!");
        }
    }

    public void iniciarTemporizador() {
        scheduler.schedule(() -> {
            System.out.println("Tempo acabou! Encerrando a partida " + id);
            encerrarJogo();
        }, GAME_DURATION_SECONDS, TimeUnit.SECONDS);
    }

    private void encerrarJogo() {
        if (jogoEncerrado) return;
        jogoEncerrado = true;

        String rankingFinal = getRankingFinal();
        System.out.println("Partida " + id + " - Ranking Final:\n" + rankingFinal);

        for (ClientHandler client : clients) {
            client.enviarRanking(rankingFinal);
            client.encerrar();
        }

        SwingUtilities.invokeLater(() -> RankingPanel.mostrarRanking(ranking));
        scheduler.shutdown();
    }

    public synchronized String getRankingFinal() {
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

    public void adicionarJogador(ClientHandler client) {
        clients.add(client);
    }

    public boolean estaCheia() {
        return clients.size() >= totalPlayers;
    }

    public boolean estaEncerrada() {
        return jogoEncerrado;
    }

    public synchronized String obterRankingFinal() {
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

    public void enviarRankingParaTodos(String rankingFinal) {
        for (ClientHandler client : clients) {
            client.enviarRanking(rankingFinal);
        }
    }

    // Classe RankingJogador pode ficar aqui como inner class
    static class RankingJogador {
        int playerNumber;
        int score;
        long tempo;

        RankingJogador(int playerNumber, int score, long tempo) {
            this.playerNumber = playerNumber;
            this.score = score;
            this.tempo = tempo;
        }
    }

    public static int getGameDuration() {
        return GAME_DURATION_SECONDS;
    }



}