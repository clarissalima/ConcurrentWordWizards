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
        this.gameDuration = calcularDuracao(modo);
        this.startTime = System.currentTimeMillis();
        gerarPalavrasParaRodadas(this.modo);
    }

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
            System.out.println("Primeira palavra - Dica: " + palavrasRodadas.get(0)[1]);
        }
    }

    public String[] getPalavraDaRodada(int rodada) {
        return palavrasRodadas.get(rodada);
    }

    public synchronized boolean registrarRanking(int playerNumber, int score, long tempoTotal) {
        // Obter o nome do jogador do ClientHandler correspondente
        String nomeJogador = clients.stream()
                .filter(c -> c.playerNumber == playerNumber)
                .findFirst()
                .map(ClientHandler::getNomeJogador)
                .orElse("Jogador" + playerNumber);

        RankingJogador rankingJogador = new RankingJogador(nomeJogador, playerNumber, score, tempoTotal);
        ranking.add(rankingJogador);

        return playerNumber == totalPlayers; // Retorna true apenas para o último jogador
    }

    public synchronized void verificaTerminoJogo() {
        if (clients.stream().allMatch(ClientHandler::terminou)) {
            jogoEncerrado = true;
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

        String rankingFinal = obterRankingFinal();
        System.out.println("Partida " + id + " - Ranking Final:\n" + rankingFinal);

        for (ClientHandler client : clients) {
            client.enviarRanking(rankingFinal);
            client.encerrar();
        }

        SwingUtilities.invokeLater(() -> RankingPanel.mostrarRanking(ranking));
        scheduler.shutdown();
    }

    public synchronized void adicionarJogador(ClientHandler client) {
        if (clients.size() >= totalPlayers) {
            throw new IllegalStateException("Partida já está cheia");
        }
        clients.add(client);

        if (clients.size() == 1) { // Primeiro jogador
            this.sockets = Collections.singletonList(client.clientSocket);
            this.startTime = System.currentTimeMillis();

            if (clients.size() == totalPlayers) {
                iniciarPartida();
            }
        }

        ServerGUI.atualizarListaPartidas();
    }

    public synchronized void iniciarPartida() {
        if (!jogoEncerrado) {
            iniciarTemporizador();
            //enviarParaTodos("PARTIDA_INICIADA|" + gameDuration);
            System.out.println("Partida " + id + " iniciada com " + clients.size() + " jogador(es).");
        }
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
                .comparingInt((RankingJogador r) -> r.score).reversed()
                .thenComparingLong(r -> r.tempo));

        for (int i = 0; i < ranking.size(); i++) {
            RankingJogador r = ranking.get(i);
            sb.append(String.format("%dº lugar - %s | Pontos: %d | Tempo: %.2f segundos\n",
                    i + 1, r.nome, r.score, r.tempo / 1000.0));
        }

        return sb.toString();
    }

    public void enviarRankingParaTodos(String rankingFinal) {
        for (ClientHandler client : clients) {
            client.enviarRanking(rankingFinal);
        }
    }

    public void enviarParaTodos(String mensagem) {
        System.out.println("entrou em enviar para todos");
        clients.forEach(client -> client.enviarMensagem(mensagem));
    }

    public int getGameDuration() {
        return this.gameDuration;
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

    public int getTempoRestante() {
        int decorrido = (int)((System.currentTimeMillis() - startTime) / 1000);
        return Math.max(0, gameDuration - decorrido);
    }

    static class RankingJogador {
        String nome;
        int playerNumber;
        int score;
        long tempo;

        RankingJogador(String nome, int playerNumber, int score, long tempo) {
            this.nome = nome;
            this.playerNumber = playerNumber;
            this.score = score;
            this.tempo = tempo;
        }
    }
}