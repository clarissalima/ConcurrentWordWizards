import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class CreateGamePanel extends JPanel {
    private final ServerGUI gui;

    public CreateGamePanel(ServerGUI gui) {
        this.gui = gui;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        setOpaque(false);

        // Título
        JLabel titleLabel = new JLabel("Criar Nova Partida");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

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

        SpinnerModel spinnerModel = new SpinnerNumberModel(2, 2, 10, 1);
        JSpinner playersSpinner = new JSpinner(spinnerModel);
        playersSpinner.setPreferredSize(new Dimension(60, 30));

        playersPanel.add(playersLabel);
        playersPanel.add(playersSpinner);

        // Botões
        JButton startButton = new JButton("Iniciar Partida");
        JButton backButton = new JButton("Voltar ao Menu");

        // Layout
        add(Box.createVerticalGlue());
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(difficultyPanel);
        add(playersPanel);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(startButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(backButton);
        add(Box.createVerticalGlue());

        startButton.addActionListener(e -> {
            String difficulty = "";
            if (easyButton.isSelected()) difficulty = "facil";
            else if (mediumButton.isSelected()) difficulty = "medio";
            else if (hardButton.isSelected()) difficulty = "dificil";

            int players = (int) playersSpinner.getValue();
            try {
                gui.createNewGame(difficulty, players);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        backButton.addActionListener(e -> gui.showMainMenu());
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