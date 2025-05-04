import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGUI {
    private JFrame frame;
    private static JPanel currentPanel;
    private ConcurrentHashMap<Integer, Partida> partidas;
    private int nextPartidaId = 1;
    private GameServer gameServer;

    public ServerGUI(GameServer gameServer) {
        this.gameServer = gameServer;
        this.partidas = gameServer.getPartidas();
        //gameServer.setServerGUI(this);
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

    // MAIN MENU
    void showMainMenu() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("🎀 Jogo de Adivinhação de Palavras 🎀");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        JLabel portLabel = new JLabel("Servidor rodando na porta: 49160");
        portLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        portLabel.setForeground(Color.WHITE);

        JButton newGameButton = createStyledButton("Criar Nova Partida");
        JButton listGamesButton = createStyledButton("Listar Partidas Existentes");

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

    // CREATE GAME SCREEN
    void showCreateGameScreen() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Criar Nova Partida");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        // Dificuldade
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

        // Jogadores
        JPanel playersPanel = new JPanel();
        playersPanel.setOpaque(false);
        playersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JLabel playersLabel = new JLabel("Número de Jogadores:");
        playersLabel.setForeground(Color.WHITE);
        playersLabel.setFont(new Font("Arial", Font.BOLD, 16));

        SpinnerModel spinnerModel = new SpinnerNumberModel(2, 2, 10, 1);
        JSpinner playersSpinner = new JSpinner(spinnerModel);
        playersSpinner.setPreferredSize(new Dimension(60, 30));

        playersPanel.add(playersLabel);
        playersPanel.add(playersSpinner);

        // Botões
        JButton startButton = createStyledButton("Iniciar Partida");
        JButton backButton = createStyledButton("Voltar ao Menu");

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
                int novapartidaId = GameServer.criarNovaPartida(difficulty, players);
                showGameCreatedScreen(novapartidaId);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Erro ao criar partida: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> showMainMenu());
        setCurrentPanel(panel);
    }

    // GAME CREATED SCREEN
    void showGameCreatedScreen(int partidaId) {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel successLabel = new JLabel("Partida #" + partidaId + " criada com sucesso!");
        successLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 24));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successLabel.setForeground(Color.WHITE);

        JLabel waitingLabel = new JLabel("Aguardando jogadores se conectarem...");
        waitingLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        waitingLabel.setForeground(Color.WHITE);

        JButton backButton = createStyledButton("Voltar ao Menu");
        panel.add(Box.createVerticalGlue());
        panel.add(successLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(waitingLabel);
        panel.add(Box.createVerticalGlue());
        panel.add(backButton);

        backButton.addActionListener(e -> showMainMenu());
        setCurrentPanel(panel);
    }

    // GAMES LIST SCREEN
    void showGamesList() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Partidas Existentes");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);

        JPanel gamesPanel = new JPanel();
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
                JPanel cardPanel = createGameCard(p, id);
                gamesPanel.add(cardPanel);
                gamesPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        scrollPane.setViewportView(gamesPanel);
        JButton backButton = createStyledButton("Voltar ao Menu Principal");
        backButton.addActionListener(e -> showMainMenu());

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scrollPane);
        panel.add(Box.createVerticalGlue());
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        setCurrentPanel(panel);
    }

    private JPanel createGameCard(Partida p, int id) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(new Color(255, 255, 255, 200));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 220, 150), 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        cardPanel.setMaximumSize(new Dimension(450, 90));
        cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        infoPanel.setOpaque(false);

        JLabel idLabel = new JLabel("PARTIDA " + id);
        idLabel.setFont(new Font("Arial", Font.BOLD, 16));
        idLabel.setForeground(new Color(60, 60, 60));

        JLabel modeLabel = new JLabel(p.modo.toUpperCase());
        modeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        modeLabel.setForeground(getColorByMode(p.modo));

        infoPanel.add(idLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        infoPanel.add(modeLabel);

        JLabel playersLabel = new JLabel(p.clients.size() + "/" + p.totalPlayers);
        playersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        playersLabel.setForeground(new Color(100, 100, 100));

        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(playersLabel, BorderLayout.EAST);

        JProgressBar progressBar = new JProgressBar(0, p.totalPlayers);
        progressBar.setValue(p.clients.size());
        progressBar.setForeground(getColorByMode(p.modo).darker());
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setPreferredSize(new Dimension(0, 6));

        cardPanel.add(headerPanel, BorderLayout.NORTH);
        cardPanel.add(progressBar, BorderLayout.SOUTH);

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
                    mostrarTelaPartida(p.id);
                }
            }
        });

        return cardPanel;
    }

    // PARTIDA SCREEN
    public void mostrarTelaPartida(int partidaId) {
        Partida partida = this.gameServer.getPartidas().get(partidaId);
        if (partida != null) {
            PartidaPanel panel = new PartidaPanel(partida);

            Timer timer = new Timer(1000, e -> {
                panel.atualizarTempo(partida.getTempoRestante());
                if (partida.estaEncerrada()) {
                    ((Timer)e.getSource()).stop();
                }
            });
            timer.start();

            JButton backButton = new JButton("Voltar para Partidas");
            backButton.addActionListener(e -> showGamesList());
            panel.add(backButton, BorderLayout.SOUTH);

            setCurrentPanel(panel);
        }
    }

    public static void atualizarListaPartidas() {
        if (currentPanel instanceof GamesListPanel) {
            ((GamesListPanel) currentPanel).atualizarLista();
        }
    }

    public void atualizarParaTelaDePartida(int partidaId) {
        SwingUtilities.invokeLater(() -> mostrarTelaPartida(partidaId));
    }

    // HELPER METHODS
    private Color getColorByMode(String modo) {
        switch (modo.toLowerCase()) {
            case "facil": return new Color(144, 238, 144);
            case "medio": return new Color(255, 215, 0);
            case "dificil": return new Color(255, 99, 71);
            default: return new Color(173, 216, 230);
        }
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
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
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


    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, new Color(255, 182, 193),
                    getWidth(), getHeight(), new Color(255, 105, 180));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Classe interna apenas para compatibilidade
    private static class GamesListPanel extends JPanel {
        public void atualizarLista() {}
    }
}