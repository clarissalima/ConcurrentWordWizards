import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public class GamesListPanel extends JPanel {
    private final ServerGUI gui;
    private final ConcurrentHashMap<Integer, Partida> partidas;
    private JPanel gamesPanel;

    public GamesListPanel(ServerGUI gui, ConcurrentHashMap<Integer, Partida> partidas) {
        this.gui = gui;
        this.partidas = partidas;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        // Título
        JLabel titleLabel = new JLabel("Partidas Existentes");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        // Lista de partidas
        gamesPanel = new JPanel();
        gamesPanel.setOpaque(false);
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        atualizarLista();

        // Botão de voltar
        JButton backButton = new JButton("Voltar ao Menu");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Layout
        add(Box.createVerticalGlue());
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(gamesPanel);
        add(Box.createVerticalGlue());
        add(backButton);

        backButton.addActionListener(e -> gui.showMainMenu());
    }

    public void atualizarLista() {
        gamesPanel.removeAll();

        if (partidas.isEmpty()) {
            JLabel emptyLabel = new JLabel("Nenhuma partida ativa no momento");
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setForeground(Color.WHITE);
            gamesPanel.add(emptyLabel);
        } else {
            for (Integer id : partidas.keySet()) {
                Partida p = partidas.get(id);
                JLabel gameLabel = new JLabel(
                        String.format("Partida %d - Modo: %s - Jogadores: %d/%d",
                                id, p.modo, p.clients.size(), p.totalPlayers)
                );
                gameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                gameLabel.setForeground(Color.WHITE);
                gameLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                gamesPanel.add(gameLabel);
            }
        }

        gamesPanel.revalidate();
        gamesPanel.repaint();
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