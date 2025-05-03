import javax.swing.*;
import java.awt.*;

public class GameCreatedPanel extends JPanel {
    public GameCreatedPanel(ServerGUI gui, int partidaId) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        setOpaque(false);

        // Mensagem de sucesso
        JLabel successLabel = new JLabel("Partida #" + partidaId + " criada com sucesso!");
        successLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 24));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successLabel.setForeground(Color.WHITE);

        JLabel waitingLabel = new JLabel("Aguardando jogadores se conectarem...");
        waitingLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        waitingLabel.setForeground(Color.WHITE);

        // Botão de voltar
        JButton backButton = new JButton("Voltar ao Menu");

        // Layout
        add(Box.createVerticalGlue());
        add(successLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(waitingLabel);
        add(Box.createVerticalGlue());
        add(backButton);

        backButton.addActionListener(e -> gui.showMainMenu());
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