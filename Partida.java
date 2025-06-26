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
    // REMOVIDO: private List<Socket> sockets; // Não é mais necessário, os sockets estão nos ClientHandlers

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

    // REMOVIDO: public void setSockets(List<Socket> sockets) { this.sockets = sockets; }
    // REMOVIDO: public List<Socket> getSockets() { return sockets; }

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
            System.out.println("Primeira palavra: " + palavrasRodadas.get(0)[0] +
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

        // Verifica se todos os jogadores já registraram seus rankings
        // (Isso é uma simplificação, idealmente um contador de jogadoresFinalizados seria mais robusto)
        // O `true` para `ultimo` só deve ser retornado uma vez para quem dispara o ranking final.
        if (ranking.size() == totalPlayers) {
            return true; // É o último a registrar ranking
        }
        return false;
    }

    public synchronized void verificaTerminoJogo() {
        if (clients.stream().allMatch(ClientHandler::terminou)) {
            jogoEncerrado = true;
            System.out.println("A partida " + id + " foi encerrada!");
        }
    }

    public void iniciarTemporizador() {
        // Notifica todos os clientes que a partida vai iniciar
        String startMsg = "PARTIDA_INICIADA|" + getGameDuration();
        enviarParaTodos(startMsg); // Envia para todos os ClientHandlers

        scheduler.schedule(() -> {
            if (!estaEncerrada()) {
                System.out.println("Tempo esgotado! Encerrando partida " + id);
                encerrarJogo();
            }
        }, gameDuration, TimeUnit.SECONDS);
    }

    public void encerrarJogo() {
        if (jogoEncerrado) return;
        jogoEncerrado = true;

        String rankingFinal = getRankingFinal();
        String rankingFinalComplete = "Partida " + id + " - Ranking Final:\n" + rankingFinal;
        System.out.println("Partida " + id + " - Ranking Final:\n" + rankingFinal);

        for (ClientHandler client : clients) {
            client.enviarRanking(rankingFinal); // Envia o ranking para cada cliente
            client.exibirResultado(rankingFinalComplete); // Isso é um stub, a exibição é no cliente
            client.encerrar(); // Fecha a conexão do cliente
        }

        // REMOVIDO: SwingUtilities.invokeLater(() -> RankingPanel.mostrarRanking(ranking)); // Agora é responsabilidade do cliente
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

        if (clients.size() == 1) { // Primeiro jogador a entrar
            this.startTime = System.currentTimeMillis(); // Reinicia o timer quando o primeiro jogador entra
        }

        // REMOVIDO: ServerGUI.atualizarListaPartidas(); // Não é mais necessário
    }

    public boolean estaCheia() {
        return clients.size() >= totalPlayers;
    }

    public boolean estaEncerrada() {
        return jogoEncerrado;
    }

    // REMOVIDO: public synchronized String obterRankingFinal() { ... } // Duplicado de getRankingFinal()

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

    // REMOVIDO: public String getInfoPartida() { ... } // Não é mais necessário para a GUI do servidor
    // REMOVIDO: public int getTempoRestante() { ... } // Agora a ClientGUI calcula com base na duração total
}