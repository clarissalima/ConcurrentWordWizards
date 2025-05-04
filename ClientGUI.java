import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientGUI {
    private static JFrame frame;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static String playerName;
    private String serverAddress;
    private int serverPort;

    public ClientGUI(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Cliente do Jogo de Adivinhação");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        showNameInputScreen();
        frame.setVisible(true);
    }

    private void showNameInputScreen() {
        JPanel panel = new GradientPanel(new Color(150, 100, 255), new Color(100, 50, 200));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Bem-vindo ao Jogo!");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(300, 30));
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton submitButton = createStyledButton("Continuar", new Color(200, 150, 255));
        submitButton.addActionListener(e -> {
            playerName = nameField.getText().trim();
            if (!playerName.isEmpty()) {
                connectToServer();
            }
        });

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(new JLabel("Digite seu nome:"));
        panel.add(nameField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(submitButton);
        panel.add(Box.createVerticalGlue());

        frame.setContentPane(panel);
        frame.revalidate();
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Envia o nome do jogador primeiro
            out.println(playerName);

            showGameSelectionScreen();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Erro ao conectar ao servidor: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showGameSelectionScreen() {
        JPanel panel = new GradientPanel(new Color(150, 100, 255), new Color(100, 50, 200));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Escolha uma Partida");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel gamesPanel = new JPanel();
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Aqui você precisará receber a lista de partidas do servidor
        // Esta é uma implementação simulada - você precisará adaptar para sua comunicação real
        gamesPanel.add(new JLabel("Carregando partidas disponíveis..."));

        JButton refreshButton = createStyledButton("Atualizar Lista", new Color(200, 150, 255));
        refreshButton.addActionListener(e -> updateGamesList(gamesPanel));

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(refreshButton);
        panel.add(Box.createVerticalGlue());

        scrollPane.setViewportView(gamesPanel);
        frame.setContentPane(panel);
        frame.revalidate();
    }

    private void updateGamesList(JPanel gamesPanel) {
        gamesPanel.removeAll();
        // Implemente a lógica para atualizar a lista de partidas do servidor
        gamesPanel.revalidate();
        gamesPanel.repaint();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 45));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(5, 25, 5, 25)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    // Classe interna para o painel com gradiente
    private static class GradientPanel extends JPanel {
        private Color color1;
        private Color color2;

        public GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        private void showWaitingScreen(String gameName) {
            JPanel panel = new GradientPanel(new Color(150, 100, 255), new Color(100, 50, 200));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            JLabel titleLabel = new JLabel("Aguardando Jogadores");
            titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setForeground(Color.WHITE);

            JLabel messageLabel = new JLabel(playerName + ", aguarde mais jogadores para " + gameName);
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messageLabel.setForeground(Color.WHITE);

            panel.add(Box.createVerticalGlue());
            panel.add(titleLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(messageLabel);
            panel.add(Box.createVerticalGlue());

            frame.setContentPane(panel);
            frame.revalidate();

            // Thread para verificar quando a partida começar
            new Thread(() -> {
                try {
                    // Implemente a lógica para verificar quando a partida começa
                    // Quando começar, chame showGameScreen()
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}