import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGUI {
    private JFrame frame;
    private JPanel currentPanel;
    private ConcurrentHashMap<Integer, Partida> partidas;
    private int nextPartidaId = 1;

    public ServerGUI() {
        ConcurrentHashMap<Integer, Partida> partidas = new ConcurrentHashMap<>();
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

    public void atualizarListaPartidas() {
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
                GameServer.criarNovaPartida(difficulty, players);
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
        JLabel successLabel = new JLabel("Partida #" + partidaId+1 + " criada com sucesso!");
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

        // Título
        JLabel titleLabel = new JLabel("Partidas Existentes");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(255, 255, 255));

        // Lista de partidas em cards
        JPanel gamesPanel = new JPanel();
        gamesPanel.setOpaque(false);
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        if (partidas.isEmpty()) {
            JLabel emptyLabel = new JLabel("Nenhuma partida ativa no momento");
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setForeground(Color.WHITE);
            gamesPanel.add(emptyLabel);
        } else {
            for (Integer id : partidas.keySet()) {
                Partida p = partidas.get(id);

                // Card de partida (estilo Gartic)
                JPanel cardPanel = new JPanel(new BorderLayout());
                cardPanel.setOpaque(false);


                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                        // Border 1: Borda externa colorida
                        BorderFactory.createLineBorder(new Color(255, 215, 220)),

                        // Border 2: Combinação do efeito 3D com margem interna
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createRaisedBevelBorder(),
                                BorderFactory.createEmptyBorder(10, 15, 10, 15)
                        )
                ));
                cardPanel.setMaximumSize(new Dimension(400, 80));
                cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Cabeçalho do card
                JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                headerPanel.setOpaque(false);

                JLabel idLabel = new JLabel("Partida #" + id);
                idLabel.setFont(new Font("Arial", Font.BOLD, 18));
                idLabel.setForeground(new Color(255, 255, 255));

                JLabel modeLabel = new JLabel("(" + p.modo.toUpperCase() + ")");
                modeLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                modeLabel.setForeground(new Color(255, 255, 255));

                headerPanel.add(idLabel);
                headerPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                headerPanel.add(modeLabel);

                // Barra de progresso de jogadores
                JProgressBar progressBar = new JProgressBar(0, p.totalPlayers);
                progressBar.setValue(p.clients.size());
                progressBar.setString(p.clients.size() + "/" + p.totalPlayers + " jogadores");
                progressBar.setStringPainted(true);
                progressBar.setForeground(getColorByMode(p.modo));
                progressBar.setBackground(new Color(70, 70, 70));
                progressBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

                // Adiciona componentes ao card
                cardPanel.add(headerPanel, BorderLayout.NORTH);
                cardPanel.add(progressBar, BorderLayout.CENTER);

                // Efeito hover no card
                cardPanel.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(255, 255, 255)),
                                BorderFactory.createEmptyBorder(15, 15, 15, 15)
                        ));
                    }

                    public void mouseExited(MouseEvent e) {
                        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(255, 215, 220)),
                                BorderFactory.createEmptyBorder(15, 15, 15, 15)
                        ));
                    }
                });

                gamesPanel.add(cardPanel);
                gamesPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        // Botão de voltar
        JButton backButton = createStyledButton("Voltar ao Menu");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Layout
        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(new JScrollPane(gamesPanel));
        panel.add(Box.createVerticalGlue());
        panel.add(backButton);

        backButton.addActionListener(e -> showMainMenu());

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
        GameServer.criarNovaPartida(difficulty, players);
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
            Partida partida = GameServer.getPartidas().get(partidaId);
            if (partida != null) {
                mostrarTelaPartida(partidaId);
            }
        });
    }

    private void mostrarTelaPartida(int partidaId) {
        Partida partida = GameServer.getPartidas().get(partidaId);
        if (partida != null) {
            PartidaPanel panel = new PartidaPanel(partida);

            // Atualiza o cronômetro a cada segundo
            Timer timer = new Timer(1000, e -> {
                panel.atualizarTempo(partida.getTempoRestante());
                if (partida.estaEncerrada()) {
                    ((Timer)e.getSource()).stop();
                }
            });
            timer.start();

            setCurrentPanel(panel);
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerGUI();
        });
    }
}