import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    //como nao temos autorizacao IANA, devemos usar uma porta entre 49152 e 65535
    private static final int PORT = 49160;
    private static final ConcurrentHashMap<Integer, Partida> partidas = new ConcurrentHashMap<>();
    private static int nextPartidaId = 1;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    // REMOVIDO: private static ServerGUI serverGUI; // Não precisamos mais da GUI do servidor aqui

    // REMOVIDO: public void setServerGUI(ServerGUI gui) { GameServer.serverGUI = gui; } // Não precisamos mais

    public static ConcurrentHashMap<Integer, Partida> getPartidas() {
        return partidas;
    }

    // REMOVIDO: public static ServerGUI getServerGUI() { return serverGUI; } // Não precisamos mais

    public static void main(String[] args) {

        // REMOVIDO: GameServer server = new GameServer(); // Não é mais necessário instanciar a si mesmo
        // REMOVIDO: ServerGUI gui = new ServerGUI(server); // Não precisamos mais da GUI do servidor
        // REMOVIDO: server.setServerGUI(gui); // Não precisamos mais

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            // Aceitar conexões de clientes
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado!");

                // Criar handler e delegar para o executor
                executor.execute(() -> {
                    tratarNovoCliente(clientSocket);
                });
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
        // REMOVIDO: if (serverGUI != null) { ServerGUI.atualizarListaPartidas(); } // Não precisamos mais
        return partidaId;
    }

    // REMOVIDO: private static void listarPartidas() { ... } // Não será mais chamado do servidor, mas sim solicitado pelo cliente

    private static void tratarNovoCliente(Socket clientSocket) {
        BufferedReader input = null;
        PrintWriter output = null;

        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // O servidor agora espera um comando inicial do cliente
            String comandoInicial = input.readLine();
            System.out.println("Comando inicial do cliente: " + comandoInicial);

            if (comandoInicial == null) {
                clientSocket.close();
                return;
            }

            // Processar comandos do cliente
            if (comandoInicial.startsWith("CRIAR_PARTIDA")) {
                String[] partes = comandoInicial.split("\\|");
                if (partes.length == 3) {
                    String modo = partes[1];
                    int totalPlayers = Integer.parseInt(partes[2]);
                    try {
                        int partidaId = criarNovaPartida(modo, totalPlayers);
                        output.println("PARTIDA_CRIADA|" + partidaId); // Confirma a criação da partida
                        System.out.println("Cliente solicitou criação de partida " + partidaId);
                    } catch (IOException e) {
                        output.println("ERRO|Falha ao criar partida.");
                        e.printStackTrace();
                    }
                } else {
                    output.println("ERRO|Formato inválido para CRIAR_PARTIDA.");
                }
            } else if (comandoInicial.equals("LISTAR_PARTIDAS")) {
                StringBuilder sb = new StringBuilder("LISTA_PARTIDAS|");
                if (partidas.isEmpty()) {
                    sb.append("Nenhuma partida ativa no momento.");
                } else {
                    partidas.entrySet().stream()
                            .filter(e -> !e.getValue().estaCheia() && !e.getValue().estaEncerrada())
                            .forEach(e -> {
                                Partida p = e.getValue();
                                sb.append(String.format("%d;%s;%d;%d,",
                                        e.getKey(), p.modo, p.clients.size(), p.totalPlayers));
                            });
                    // Remove a última vírgula se houver partidas
                    if (sb.charAt(sb.length() - 1) == ',') {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                }
                output.println(sb.toString()); // Envia a lista de partidas
                System.out.println("Cliente solicitou lista de partidas.");

            } else if (comandoInicial.startsWith("ENTRAR_PARTIDA")) {
                String[] partes = comandoInicial.split("\\|");
                if (partes.length == 2) {
                    try {
                        int partidaId = Integer.parseInt(partes[1]);
                        Partida partida = partidas.get(partidaId);

                        if (partida == null || partida.estaEncerrada()) {
                            output.println("ERRO|Partida inválida ou já encerrada.");
                            clientSocket.close();
                            return;
                        }

                        if (partida.estaCheia()) {
                            output.println("ERRO|Partida já está cheia.");
                            // GameServer.notificarInicioPartida(partidaId); // Esta notificação agora virá do ClientHandler
                            clientSocket.close();
                            return;
                        }

                        int playerNumber = partida.clients.size() + 1;
                        ClientHandler clientHandler = new ClientHandler(clientSocket, playerNumber, partida);
                        partida.adicionarJogador(clientHandler);
                        // REMOVIDO: ServerGUI.atualizarListaPartidas(); // Não é mais necessário

                        output.println("ENTROU_PARTIDA|" + partidaId + "|" + playerNumber); // Confirma que o jogador entrou

                        if (partida.estaCheia()) {
                            System.out.println("Partida " + partidaId + " iniciando com " + partida.totalPlayers + " jogadores!");
                            partida.iniciarTemporizador();
                            GameServer.notificarInicioPartida(partidaId); // Notifica inicio (agora via ClientHandler para clientes)
                            for (ClientHandler client : partida.clients) {
                                client.start(); // Inicia a thread do handler para cada cliente
                            }
                        } else {
                            output.println("AGUARDANDO_JOGADORES|" + partida.clients.size() + "|" + partida.totalPlayers);
                        }

                    } catch (NumberFormatException e) {
                        output.println("ERRO|ID de partida inválido.");
                        clientSocket.close();
                    }
                } else {
                    output.println("ERRO|Formato inválido para ENTRAR_PARTIDA.");
                }
            } else {
                output.println("ERRO|Comando desconhecido.");
                clientSocket.close(); // Fecha a conexão se o comando inicial não for reconhecido
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

    //notifica inicio da partida
    public static void notificarInicioPartida(int partidaId) {
        Partida partida = partidas.get(partidaId);
        if (partida != null) {
            // A mensagem de "PARTIDA_INICIADA" será enviada diretamente pelo ClientHandler aos seus respectivos clientes
            // quando a partida estiver cheia. Não há mais GUI do servidor para atualizar aqui.
            // A GUI do cliente receberá essa mensagem e mudará para a tela de jogo.
            System.out.println("Notificando início da partida " + partidaId + " para os clientes.");
        }
    }

    // REMOVIDO: public void atualizarGUIparaPartida(int partidaId) { ... } // Não precisamos mais
    // REMOVIDO: public void iniciarCronometro(int segundosTotais, List<Socket> jogadores) { ... } // Lógica movida ou adaptada
}