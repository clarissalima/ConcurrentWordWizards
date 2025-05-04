import java.io.*;
import java.net.*;
import java.util.concurrent.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    private static final int PORT = 49160;
    private static final ConcurrentHashMap<Integer, Partida> partidas = new ConcurrentHashMap<>();
    public static ServerGUI ServerGUI;
    private static int nextPartidaId = 1;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
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
            System.out.println(playerName+ " fo game serverrr");
            if (playerName == null || playerName.trim().isEmpty()) {
                output.println("NOME_INVALIDO");
            }

            String comando;
            while ((comando = input.readLine()) != null) {
                System.out.println("entrou");
                System.out.println(comando + " comandoo");
                if (comando.equals("LISTAR_PARTIDAS")) {
                    output.println("LISTA_PARTIDAS");
                    partidas.entrySet().stream()
                            .filter(e -> !e.getValue().estaCheia() && !e.getValue().estaEncerrada())
                            .forEach(e -> {
                                Partida p = e.getValue();
                                output.printf("PARTIDA:%d|%s|%d|%d%n",
                                        e.getKey(), p.modo, p.clients.size(), p.totalPlayers);
                            });
                    output.println("FIM_LISTA");
                } else if (comando.startsWith("ENTRAR_PARTIDA|")) {
                    System.out.println("entrou no outro if");
                    try {
                        int partidaId = Integer.parseInt(comando.split("\\|")[1]);
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
                        //partida.adicionarJogador(clientHandler);
                        ServerGUI.atualizarListaPartidas();
                        System.out.println(playerNumber + " numero do jogador");

                        output.println("Você entrou na partida " + partidaId + " como " + playerName);

                        System.out.println(playerName+" playeer namee dentro de ifs la ");

                        clientHandler.start();

                        if (partida.estaCheia()) {
                            System.out.println("Partida " + partidaId + " iniciando com " + partida.totalPlayers + " jogadores!");
                            System.out.println(partidaId+" partida id");
                            notificarInicioPartida(partidaId); // tirar
                        } else {
                            output.println("Aguardando mais jogadores... (" + partida.clients.size() + "/" + partida.totalPlayers + ")");
                        }

                    } catch (NumberFormatException e) {
                        output.println("Entrada inválida.");
                        clientSocket.close();
                    }
                } else {
                    output.println("Comando inválido.");
                }
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
        System.out.println("Inicio da paratida");
        Partida partida = partidas.get(partidaId);
        if (partida != null && serverGUI != null) {
            System.out.println("entrou em notificarinicio partida");
            String mensagem = "PARTIDA_INICIADA|" + partida.getGameDuration();
            partida.enviarParaTodos(mensagem);
            System.out.println("passou do enviar p todos");
            serverGUI.atualizarParaTelaDePartida(partidaId);
        }
    }

    }


//public void main() {
//}

//private boolean serverGUI;





