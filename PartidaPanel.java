import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PartidaPanel extends JPanel {
    private JLabel tempoLabel;
    private final Partida partida;
    private Timer timer;

    public PartidaPanel(Partida partida) {
        this.partida = partida;
        initializeUI();
        startTimer();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 182, 193));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("PARTIDA #" + partida.id, SwingConstants.CENTER);
        titulo.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);

        JLabel modoLabel = new JLabel("Modo: " + partida.modo.toUpperCase(), SwingConstants.CENTER);
        modoLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        modoLabel.setForeground(Color.WHITE);

        headerPanel.add(titulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(modoLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Timer panel
        JPanel timerPanel = new JPanel();
        timerPanel.setOpaque(false);
        tempoLabel = new JLabel("00:00", SwingConstants.CENTER);
        tempoLabel.setFont(new Font("Arial", Font.BOLD, 72));
        tempoLabel.setForeground(Color.WHITE);
        timerPanel.add(tempoLabel);
        add(timerPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        JButton voltarBtn = createStyledButton("Voltar ao Menu");
        voltarBtn.addActionListener(e -> {
            if (timer != null) {
                timer.stop();
            }
            GameServer.getServerGUI().showMainMenu();
        });
        footerPanel.add(voltarBtn);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void startTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int segundosRestantes = partida.getTempoRestante();
                atualizarTempo(segundosRestantes);

                // Mudança de cor quando o tempo está acabando
                if (segundosRestantes <= 30) {
                    tempoLabel.setForeground(new Color(255, 100, 100));
                }

                if (segundosRestantes <= 0) {
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    public void atualizarTempo(int segundos) {
        int minutos = segundos / 60;
        int segs = segundos % 60;
        SwingUtilities.invokeLater(() -> {
            tempoLabel.setText(String.format("%02d:%02d", minutos, segs));
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 45));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(255, 215, 220));
        button.setForeground(new Color(70, 70, 70));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(5, 25, 5, 25)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 235, 240));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 215, 220));
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