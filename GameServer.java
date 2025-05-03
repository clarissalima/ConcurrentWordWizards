import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    //como nao temos autorizacao IANA, devemos usar uma porta entre 49152 e 65535
    private static final int PORT = 49160;
    private static final ConcurrentHashMap<Integer, Partida> partidas = new ConcurrentHashMap<>();
    private static int nextPartidaId = 1;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    static ServerGUI serverGUI;

    public static void main(String[] args) {

        serverGUI = new ServerGUI();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta: " + PORT);


            // Thread para aceitar comandos do administrador
//            new Thread(() -> {
//                try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
//                    while (true) {
//                        System.out.println("Comandos disponíveis:");
//                        System.out.println("1 - Criar nova partida");
//                        System.out.println("2 - Listar partidas");
//                        System.out.print("Escolha uma opção: ");
//
//                        String opcao = consoleInput.readLine().trim();
//                        if ("1".equals(opcao)) {
//                            criarNovaPartida(consoleInput);
//                        } else if ("2".equals(opcao)) {
//                            listarPartidas();
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }).start();

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

    public static void criarNovaPartida(String modo, int totalPlayers) throws IOException {

        int partidaId = nextPartidaId++;
        Partida partida = new Partida(partidaId, modo, totalPlayers);
        partidas.put(partidaId, partida);

        System.out.println("Partida " + partidaId + " criada! Aguardando jogadores...");
        serverGUI.atualizarListaPartidas();
    }

    private static void listarPartidas() {
        if (partidas.isEmpty()) {
            System.out.println("Nenhuma partida ativa no momento.");
            return;
        }

        System.out.println("\n--- Partidas Ativas ---");
        for (Map.Entry<Integer, Partida> entry : partidas.entrySet()) {
            Partida p = entry.getValue();
            System.out.printf("Partida %d - Modo: %s - Jogadores: %d/%d - Status: %s%n",
                    entry.getKey(),
                    p.modo,
                    p.clients.size(),
                    p.totalPlayers,
                    p.estaEncerrada() ? "Encerrada" : "Em andamento");
        }
        System.out.println();
    }

    private static void tratarNovoCliente(Socket clientSocket) {
        BufferedReader input = null;
        PrintWriter output = null;

        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Enviar lista de partidas disponíveis
            output.println("Bem-vindo ao servidor de jogos!");
            output.println("Partidas disponíveis:");

            PrintWriter finalOutput = output;
            partidas.entrySet().stream()
                    .filter(e -> !e.getValue().estaCheia() && !e.getValue().estaEncerrada())
                    .forEach(e -> {
                        Partida p = e.getValue();
                        finalOutput.printf("%d - Modo %s (%d/%d jogadores)%n",
                                e.getKey(), p.modo, p.clients.size(), p.totalPlayers);
                    });

            output.println("Digite o número da partida que deseja entrar");

            String resposta = input.readLine();
            if (resposta == null) {
                clientSocket.close();
                return;
            }

            try {
                int partidaId = Integer.parseInt(resposta.trim());
                Partida partida = partidas.get(partidaId);

                if (partida == null || partida.estaEncerrada()) {
                    output.println("Partida inválida ou já encerrada.");
                    clientSocket.close();
                    return;
                }

                if (partida.estaCheia()) {
                    output.println("Partida já está cheia.");
                    clientSocket.close();
                    return;
                }

                int playerNumber = partida.clients.size() + 1;
                ClientHandler clientHandler = new ClientHandler(clientSocket, playerNumber, partida);
                partida.adicionarJogador(clientHandler);
                serverGUI.atualizarListaPartidas();

                output.println("Você entrou na partida " + partidaId + " como Jogador " + playerNumber);

                if (partida.estaCheia()) {
                    System.out.println("Partida " + partidaId + " iniciando com " + partida.totalPlayers + " jogadores!");
                    partida.iniciarTemporizador();
                    for (ClientHandler client : partida.clients) {
                        client.start();
                    }
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

    public void iniciarCronometro(int segundosTotais, List<Socket> jogadores) {
        new Thread(() -> {
            try {
                for (int i = segundosTotais; i >= 0; i--) {
                    String msg;
                    if (i == segundosTotais) {
                        msg = " A partida começou! Tempo total: " + segundosTotais + " segundos.";

                    //TA ERRADOOOOOOOOOOOOOOOOOOOOO
                    } else if (i == 10) {
                        msg = "Faltam apenas 10 segundos!";
                    } else if (i == 0) {
                        msg = "Tempo esgotado! A partida será encerrada.";
                    } else {
                        Thread.sleep(1000);
                        continue;
                    }

                    for (Socket socket : jogadores) {
                        try {
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(msg);
                        } catch (IOException e) {
                            System.out.println("Erro ao enviar cronômetro para jogador: " + e.getMessage());
                        }
                    }

                    System.out.println("[Servidor] " + msg);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                System.out.println("Erro no cronômetro: " + e.getMessage());
            }
        }).start();
    }
}





