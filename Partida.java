import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.Socket;
import java.util.List;

public class Partida {
    final int id;
    final String modo;
    final int totalPlayers;
    final List<ClientHandler> clients = new ArrayList<>();
    private final List<String[]> palavrasRodadas = new ArrayList<>();
    private final List<RankingJogador> ranking = new ArrayList<>();
    private int jogadoresFinalizados = 0;
    private boolean jogoEncerrado = false;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    //private static int GAME_DURATION_SECONDS;
    private int gameDuration;
    private long startTime;
    private List<Socket> sockets;

    // Durações padrão para cada modo
    public static final int FACIL_DURATION = 180;
    public static final int MEDIO_DURATION = 120;
    public static final int DIFICIL_DURATION = 60;


    public Partida(int id, String modo, int totalPlayers) {
        this.id = id;
        this.modo = modo.toLowerCase();
        this.totalPlayers = totalPlayers;
        //definirModoJogo(modo);
        this.gameDuration = calcularDuracao(modo);
        this.startTime = System.currentTimeMillis();
        gerarPalavrasParaRodadas(this.modo);
    }

    //private List<Socket> sockets;

    public void setSockets(List<Socket> sockets) {
        this.sockets = sockets;
    }

    public List<Socket> getSockets() {
        return sockets;
    }

    private int calcularDuracao(String modo) {
        switch (modo.toLowerCase()) {
            case "facil":
                return FACIL_DURATION;
            case "medio":
                return MEDIO_DURATION;
            case "dificil":
                return DIFICIL_DURATION;
            default:
                return MEDIO_DURATION;
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
            if (!estaEncerrada()) {
                System.out.println("Tempo esgotado! Encerrando partida " + id);
                encerrarJogo();
            }
        }, gameDuration, TimeUnit.SECONDS);
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

    public synchronized void adicionarJogador(ClientHandler client) {
        if (clients.size() >= totalPlayers) {
            throw new IllegalStateException("Partida já está cheia");
        }
        clients.add(client);

        if (clients.size() == 1) { // Primeiro jogador
            this.sockets = Collections.singletonList(client.clientSocket);
            this.startTime = System.currentTimeMillis(); // Reinicia o timer quando o primeiro jogador entra
        }

        GameServer.serverGUI.atualizarListaPartidas();

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

    public int getGameDuration() {
        return this.gameDuration;
    }


    public void enviarParaTodos(String mensagem) {
        clients.forEach(client -> client.enviarMensagem(mensagem));
    }


    public String getInfoPartida() {
        return String.format("Partida %d - %s (%d/%d) %02d:%02d",
                id,
                modo.toUpperCase(),
                clients.size(),
                totalPlayers,
                getTempoRestante() / 60,
                getTempoRestante() % 60);
    }

    // Na classe Partida
    public int getTempoRestante() {
        int decorrido = (int)((System.currentTimeMillis() - startTime) / 1000);
        return Math.max(0, gameDuration - decorrido); // Nunca retorna negativo
    }




}