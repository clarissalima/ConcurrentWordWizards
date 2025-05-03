import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuPanel extends JPanel {
    private final ServerGUI gui;

    public MainMenuPanel(ServerGUI gui) {
        this.gui = gui;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        setOpaque(false);

        // Título
        JLabel titleLabel = new JLabel("🎀 Jogo de Adivinhação de Palavras 🎀");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        // Subtítulo com porta
        JLabel portLabel = new JLabel("Servidor rodando na porta: 49160");
        portLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        portLabel.setForeground(Color.WHITE);

        // Botões
        JButton newGameButton = createStyledButton("Criar Nova Partida");
        JButton listGamesButton = createStyledButton("Listar Partidas Existentes");

        // Layout
        add(Box.createVerticalGlue());
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(portLabel);
        add(Box.createVerticalGlue());
        add(newGameButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(listGamesButton);
        add(Box.createVerticalGlue());

        newGameButton.addActionListener(e -> gui.showCreateGameScreen());
        listGamesButton.addActionListener(e -> gui.showGamesList());
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