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

    private static ServerGUI serverGUI;



    public void setServerGUI(ServerGUI gui) {
        GameServer.serverGUI = gui;
    }

    public ConcurrentHashMap<Integer, Partida> getPartidas() {
        return partidas;
    }


    public static ServerGUI getServerGUI() {
        return serverGUI;
    }


    public void main(String[] args) {

        GameServer server = new GameServer();

        ServerGUI gui = new ServerGUI(server); // Passa a instância do servidor
        server.setServerGUI(gui);


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
        if (serverGUI != null) {
            ServerGUI.atualizarListaPartidas();
        }
        return partidaId;
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
                    GameServer.notificarInicioPartida(partidaId);
                    clientSocket.close();
                    return;
                }

                int playerNumber = partida.clients.size() + 1;
                ClientHandler clientHandler = new ClientHandler(clientSocket, playerNumber, partida);
                partida.adicionarJogador(clientHandler);
                ServerGUI.atualizarListaPartidas();

                output.println("Você entrou na partida " + partidaId + " como Jogador " + playerNumber);

                if (partida.estaCheia()) {
                    System.out.println("Partida " + partidaId + " iniciando com " + partida.totalPlayers + " jogadores!");
                    partida.iniciarTemporizador();
                    GameServer.notificarInicioPartida(partidaId);
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

    //notifica inicio da partida
//    public static ConcurrentHashMap<Integer, Partida> getPartidas() {
//        return partidas;
//    }

    public static void notificarInicioPartida(int partidaId) {
        Partida partida = partidas.get(partidaId);
        if (partida != null && serverGUI != null) {
            String mensagem = "PARTIDA_INICIADA|" + partida.getGameDuration();
            partida.enviarParaTodos(mensagem);
            serverGUI.atualizarParaTelaDePartida(partidaId);
        }
    }

    public void atualizarGUIparaPartida(int partidaId) {
        if (this.serverGUI != null) {
            this.serverGUI.mostrarTelaPartida(partidaId);
        }
    }



    public void iniciarCronometro(int segundosTotais, List<Socket> jogadores) {
        new Thread(() -> {
            try {
                for (int i = segundosTotais; i >= 0; i--) {
                    String msg;
                    if (i == segundosTotais) {
                        msg = " A partida começou! Tempo total: " + segundosTotais + " segundos.";
                    } else if (i == 10) {
                        msg = "Faltam apenas 10 segundos!";
                    } else if (i == 0) {
                        msg = "Tempo esgotado! A partida será encerrada.";
                    } else {
                        msg = "Tempo restante: " + i + " segundos";
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

    public class ServerGUI {
        private JFrame frame;
        private static JPanel currentPanel;
        private ConcurrentHashMap<Integer, Partida> partidas;
        private int nextPartidaId = 1;

        public ServerGUI(GameServer server) {
            this.partidas = getPartidas();
            System.out.println("Partidas no servidor: " + this.partidas.size());
            initialize();
        }

        private void initialize() {
            frame = new JFrame("Servidor do Jogo de Adivinhação");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            showMainMenu();

            frame.setVisible(true);
        }

        public static void atualizarListaPartidas() {
            if (currentPanel instanceof GamesListPanel) {
                ((GamesListPanel) currentPanel).atualizarLista();
            }
        }


        void showMainMenu() {
            JPanel panel = new GradientPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            // Título
            JLabel titleLabel = new JLabel("🎀 Jogo de Adivinhação de Palavras 🎀");
            titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 32));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setForeground(new Color(255, 255, 255));

            // Subtítulo com porta
            JLabel portLabel = new JLabel("Servidor rodando na porta: 49160");
            portLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            portLabel.setForeground(new Color(255, 255, 255));

            // Botões
            JButton newGameButton = createStyledButton("Criar Nova Partida");
            JButton listGamesButton = createStyledButton("Listar Partidas Existentes");

            // Espaçamento
            panel.add(Box.createVerticalGlue());
            panel.add(titleLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(portLabel);
            panel.add(Box.createVerticalGlue());
            panel.add(newGameButton);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(listGamesButton);
            panel.add(Box.createVerticalGlue());

            newGameButton.addActionListener(e -> showCreateGameScreen());
            listGamesButton.addActionListener(e -> showGamesList());

            setCurrentPanel(panel);
        }

        void showCreateGameScreen() {
            JPanel panel = new GradientPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            // Título
            JLabel titleLabel = new JLabel("Criar Nova Partida");
            titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setForeground(new Color(255, 255, 255));

            // Seletor de dificuldade
            JPanel difficultyPanel = new JPanel();
            difficultyPanel.setOpaque(false);
            difficultyPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

            ButtonGroup difficultyGroup = new ButtonGroup();
            JRadioButton easyButton = createRadioButton("Fácil", "facil");
            JRadioButton mediumButton = createRadioButton("Médio", "medio");
            JRadioButton hardButton = createRadioButton("Difícil", "dificil");

            difficultyGroup.add(easyButton);
            difficultyGroup.add(mediumButton);
            difficultyGroup.add(hardButton);
            mediumButton.setSelected(true);

            difficultyPanel.add(easyButton);
            difficultyPanel.add(mediumButton);
            difficultyPanel.add(hardButton);

            // Seletor de número de jogadores
            JPanel playersPanel = new JPanel();
            playersPanel.setOpaque(false);
            playersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

            JLabel playersLabel = new JLabel("Número de Jogadores:");
            playersLabel.setForeground(Color.WHITE);
            playersLabel.setFont(new Font("Arial", Font.BOLD, 16));

            SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
            JSpinner playersSpinner = new JSpinner(spinnerModel);
            playersSpinner.setPreferredSize(new Dimension(60, 30));

            playersPanel.add(playersLabel);
            playersPanel.add(playersSpinner);

            // Botões
            JButton startButton = createStyledButton("Iniciar Partida");
            JButton backButton = createStyledButton("Voltar ao Menu");

            // Layout
            panel.add(Box.createVerticalGlue());
            panel.add(titleLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 30)));
            panel.add(difficultyPanel);
            panel.add(playersPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 30)));
            panel.add(startButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(backButton);
            panel.add(Box.createVerticalGlue());

            startButton.addActionListener(e -> {
                String difficulty = "";
                if (easyButton.isSelected()) difficulty = "facil";
                else if (mediumButton.isSelected()) difficulty = "medio";
                else if (hardButton.isSelected()) difficulty = "dificil";

                int players = (int) playersSpinner.getValue();

                try {
                    criarNovaPartida(difficulty, players);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                showGameCreatedScreen(nextPartidaId - 1);
            });

            backButton.addActionListener(e -> showMainMenu());

            setCurrentPanel(panel);
        }

        private void showGameCreatedScreen(int partidaId) {
            JPanel panel = new GradientPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            // Mensagem de sucesso
            JLabel successLabel = new JLabel("Partida #" + (partidaId+1) + " criada com sucesso!");
            successLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 24));
            successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            successLabel.setForeground(new Color(255, 255, 255));

            JLabel waitingLabel = new JLabel("Aguardando jogadores se conectarem...");
            waitingLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            waitingLabel.setForeground(new Color(255, 255, 255));

            // Botão de voltar
            JButton backButton = createStyledButton("Voltar ao Menu");

            // Layout
            panel.add(Box.createVerticalGlue());
            panel.add(successLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(waitingLabel);
            panel.add(Box.createVerticalGlue());
            panel.add(backButton);

            backButton.addActionListener(e -> showMainMenu());

            setCurrentPanel(panel);
        }

        void showGamesList() {
            JPanel panel = new GradientPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Título com ícone
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            titlePanel.setOpaque(false);
            JLabel titleLabel = new JLabel("Partidas Existentes");
            titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
            titleLabel.setForeground(new Color(255, 255, 255));
            // Adiciona um ícone (substitua pelo seu próprio ícone)
            titlePanel.add(new JLabel(new ImageIcon("game_icon.png")));
            titlePanel.add(titleLabel);

            // Container principal com rolagem
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setPreferredSize(new Dimension(500, 350));

            // Painel de cards
            JPanel gamesPanel = new JPanel();
            gamesPanel.setOpaque(false);
            gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
            gamesPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

            if (partidas.isEmpty()) {
                JLabel emptyLabel = new JLabel("Nenhuma partida ativa no momento");
                emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                emptyLabel.setForeground(new Color(240, 240, 240));
                gamesPanel.add(emptyLabel);
            } else {
                for (Integer id : partidas.keySet()) {
                    Partida p = partidas.get(id);

                    // Card moderno
                    JPanel cardPanel = new JPanel(new BorderLayout());
                    cardPanel.setOpaque(true);
                    cardPanel.setBackground(new Color(255, 255, 255, 200)); // Fundo branco semi-transparente
                    cardPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 215, 220, 150), 2),
                            BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                    cardPanel.setMaximumSize(new Dimension(450, 90));
                    cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    // Header com ícone de status
                    JPanel headerPanel = new JPanel(new BorderLayout());
                    headerPanel.setOpaque(false);

                    JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                    infoPanel.setOpaque(false);

                    // Ícone baseado no status (substitua pelos seus próprios ícones)
                    JLabel statusIcon = new JLabel(new ImageIcon(
                            p.estaCheia() ? "full_icon.png" :
                                    p.clients.size() > 0 ? "active_icon.png" : "waiting_icon.png"
                    ));

                    JLabel idLabel = new JLabel("PARTIDA " + id);
                    idLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    idLabel.setForeground(new Color(60, 60, 60));

                    JLabel modeLabel = new JLabel(p.modo.toUpperCase());
                    modeLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    modeLabel.setForeground(getColorByMode(p.modo));

                    infoPanel.add(statusIcon);
                    infoPanel.add(idLabel);
                    infoPanel.add(Box.createRigidArea(new Dimension(15, 0)));
                    infoPanel.add(modeLabel);

                    // Contador de jogadores alinhado à direita
                    JLabel playersLabel = new JLabel(p.clients.size() + "/" + p.totalPlayers);
                    playersLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    playersLabel.setForeground(new Color(100, 100, 100));

                    headerPanel.add(infoPanel, BorderLayout.WEST);
                    headerPanel.add(playersLabel, BorderLayout.EAST);

                    // Barra de progresso slim
                    JProgressBar progressBar = new JProgressBar(0, p.totalPlayers);
                    progressBar.setValue(p.clients.size());
                    progressBar.setStringPainted(false);
                    progressBar.setForeground(getColorByMode(p.modo).darker());
                    progressBar.setBackground(new Color(230, 230, 230));
                    progressBar.setPreferredSize(new Dimension(0, 6));
                    progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

                    cardPanel.add(headerPanel, BorderLayout.NORTH);
                    cardPanel.add(progressBar, BorderLayout.SOUTH);

                    // Efeito hover moderno
                    cardPanel.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            cardPanel.setBackground(new Color(255, 255, 255, 230));
                            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(getColorByMode(p.modo), 2),
                                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                            ));
                        }

                        public void mouseExited(MouseEvent e) {
                            cardPanel.setBackground(new Color(255, 255, 255, 200));
                            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(255, 215, 220, 150), 2),
                                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                            ));
                        }

                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e)) {
                                mostrarTelaPartida(p.id); // Mostra a tela do cronômetro
                            }
                        }
                    });

                    gamesPanel.add(cardPanel);
                    gamesPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                }
            }

            scrollPane.setViewportView(gamesPanel);

            // Botão de voltar estilizado
            JButton backButton = new JButton("Voltar ao Menu Principal");
            backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            backButton.setFont(new Font("Arial", Font.BOLD, 14));
            backButton.setForeground(new Color(70, 70, 70));
            backButton.setBackground(new Color(255, 235, 235));
            backButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 200, 200), 1),
                    BorderFactory.createEmptyBorder(8, 25, 8, 25)
            ));
            backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backButton.addActionListener(e -> showMainMenu());

            // Layout final
            panel.add(Box.createVerticalGlue());
            panel.add(titlePanel);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(scrollPane);
            panel.add(Box.createVerticalGlue());
            panel.add(backButton);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));

            setCurrentPanel(panel);
        }

        // Método auxiliar para cores por dificuldade (adicione na classe)
        private Color getColorByMode(String modo) {
            return switch (modo.toLowerCase()) {
                case "facil" -> new Color(144, 238, 144); // Verde claro
                case "medio" -> new Color(255, 215, 0);   // Amarelo ouro
                case "dificil" -> new Color(255, 99, 71); // Vermelho tomate
                default -> new Color(173, 216, 230);      // Azul claro
            };
        }

        public void createNewGame(String difficulty, int players) throws IOException {
            criarNovaPartida(difficulty, players);
            showGameCreatedScreen(nextPartidaId - 1);
            nextPartidaId++;
        }

        private void setCurrentPanel(JPanel panel) {
            if (currentPanel != null) {
                frame.remove(currentPanel);
            }
            currentPanel = panel;
            frame.add(currentPanel);
            frame.revalidate();
            frame.repaint();
        }

        private JButton createStyledButton(String text) {
            JButton button = new JButton(text);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setPreferredSize(new Dimension(250, 50));
            button.setMaximumSize(new Dimension(250, 50));
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setBackground(new Color(255, 182, 193));
            button.setForeground(new Color(70, 70, 70));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255), 2),
                    BorderFactory.createEmptyBorder(10, 25, 10, 25)
            ));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    button.setBackground(new Color(255, 215, 220));
                }

                public void mouseExited(MouseEvent evt) {
                    button.setBackground(new Color(255, 182, 193));
                }
            });

            return button;
        }

        private JRadioButton createRadioButton(String text, String actionCommand) {
            JRadioButton button = new JRadioButton(text);
            button.setActionCommand(actionCommand);
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.setForeground(Color.WHITE);
            button.setOpaque(false);
            button.setFocusPainted(false);
            return button;
        }

        // Painel com gradiente rosinha
        private static class GradientPanel extends JPanel {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                Color color1 = new Color(255, 182, 193);
                Color color2 = new Color(255, 105, 180);

                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }

        public void atualizarParaTelaDePartida(int partidaId) {
            SwingUtilities.invokeLater(() -> {
                mostrarTelaPartida(partidaId);
            });
        }






        private void mostrarTelaPartida(int partidaId) {
            Partida partida = getPartidas().get(partidaId);
            if (partida != null) {
                PartidaPanel panel = new PartidaPanel(partida);

                // Atualiza o cronômetro a cada segundo
                javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                    panel.atualizarTempo(partida.getTempoRestante());
                    if (partida.estaEncerrada()) {
                        ((Timer)e.getSource()).stop();
                    }
                });
                timer.start();

                JButton backButton = new JButton("Voltar para Partidas");
                backButton.addActionListener(e -> showGamesList());

                // Adiciona o botão no painel
                panel.add(backButton, BorderLayout.SOUTH);
                setCurrentPanel(panel);
                SwingUtilities.invokeLater(() -> {
                    frame.revalidate();
                    frame.repaint();
                });
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Partida não encontrada!",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }


            }
        }
}





