import java.io.*;
import java.net.*;
import java.util.concurrent.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    private static final int PORT = 49160;
    private static final ConcurrentHashMap<Integer, Partida> partidas = new ConcurrentHashMap<>();
    public static ServerGUI ServerGUI;
    private static int nextPartidaId = 1;
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static ServerGUI serverGUI;

    public void setServerGUI(ServerGUI gui) {
        GameServer.serverGUI = gui;
    }

    public static ConcurrentHashMap<Integer, Partida> getPartidas() {
        return partidas;
    }

    public static ServerGUI getServerGUI() {
        return serverGUI;
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        ServerGUI gui = new ServerGUI(server);
        server.setServerGUI(gui);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado!");
                executor.execute(() -> tratarNovoCliente(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int criarNovaPartida(String modo, int totalPlayers) throws IOException {
        int partidaId = nextPartidaId++;
        Partida partida = new Partida(partidaId, modo, totalPlayers);
        partidas.put(partidaId, partida);

        System.out.println("Partida " + partidaId + " criada! Aguardando jogadores...");
        if (serverGUI != null) {
            ServerGUI.atualizarListaPartidas();
        }
        return partidaId;
    }

    private static void tratarNovoCliente(Socket clientSocket) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            // 1. Solicitar nome do jogador
            output.println("DIGITE_SEU_NOME");
            String playerName = input.readLine();

            // 2. Enviar lista de partidas
            output.println("LISTA_PARTIDAS");
            partidas.entrySet().stream()
                    .filter(e -> !e.getValue().estaCheia() && !e.getValue().estaEncerrada())
                    .forEach(e -> {
                        Partida p = e.getValue();
                        output.printf("PARTIDA:%d|%s|%d|%d%n",
                                e.getKey(), p.modo, p.clients.size(), p.totalPlayers);
                    });
            output.println("FIM_LISTA");

        try {
            String resposta = input.readLine();
            if (resposta == null || !resposta.startsWith("ENTRAR_PARTIDA|")) {
                output.println("Comando inválido.");
                clientSocket.close();
                return;
            }

            int partidaId = Integer.parseInt(resposta.split("\\|")[1]);
            Partida partida = partidas.get(partidaId);

            if (partida == null || partida.estaEncerrada()) {
                output.println("Partida inválida ou já encerrada.");
                clientSocket.close();
                return;
            }

            if (partida.estaCheia()) {
                output.println("Partida já está cheia.");
                notificarInicioPartida(partidaId);
                clientSocket.close();
                return;
            }

            int playerNumber = partida.clients.size() + 1;
            ClientHandler clientHandler = new ClientHandler(clientSocket, playerNumber, partida);
            partida.adicionarJogador(clientHandler);
            ServerGUI.atualizarListaPartidas();

            output.println("Você entrou na partida " + partidaId + " como " + playerName);

            clientHandler.start(); // Sempre inicia o thread logo após adicionar o jogador

            if (partida.estaCheia()) {
                System.out.println("Partida " + partidaId + " iniciando com " + partida.totalPlayers + " jogadores!");
                notificarInicioPartida(partidaId);
            } else {
                output.println("Aguardando mais jogadores... (" + partida.clients.size() + "/" + partida.totalPlayers + ")");
            }


        } catch (NumberFormatException e) {
            output.println("Entrada inválida.");
            clientSocket.close();
        }
    } catch (IOException e) {
            System.out.println("Erro ao tratar novo cliente: " + e.getMessage());
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException ex) {
                System.out.println("Erro ao fechar socket: " + ex.getMessage());
            }
        }
    }

    public static void notificarInicioPartida(int partidaId) {
        Partida partida = partidas.get(partidaId);
        if (partida != null && serverGUI != null) {
            String mensagem = "PARTIDA_INICIADA|" + partida.getGameDuration();
            partida.enviarParaTodos(mensagem);
            serverGUI.atualizarParaTelaDePartida(partidaId);
        }
    }

    }


//public void main() {
//}

//private boolean serverGUI;





