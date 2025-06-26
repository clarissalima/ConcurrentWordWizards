import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class TelaDeJogo extends JFrame {
    private String palavraSecreta; // Será usada para validação local do palpite
    private JTextField guessField;
    private JButton submitButton;
    private JLabel dicaLabel;
    public JTextArea resultadoArea; // Alterado para public para ClientGUI poder adicionar mensagens
    private volatile String palpiteEnviado = null; // Usado para comunicação com ClientGUI
    private int playerNumber;
    private ClientGUI clientGUI; // Referência para a GUI principal do cliente

    // Construtor atualizado para receber a referência à ClientGUI
    public TelaDeJogo(String palavraSecreta, String dica, int playerNumber) {
        this.palavraSecreta = palavraSecreta;
        this.playerNumber = playerNumber;

        setTitle("Jogo de Adivinhação de Palavras - Jogador " + playerNumber);
        setSize(450, 400);
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Não queremos fechar o cliente inteiro
        //setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Comentado, pois ClientGUI vai gerenciar isso
        setLocationRelativeTo(null);

        // Painel de fundo com gradiente
        JPanel backgroundPanel = new JPanel() {
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
        };
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Label de dica
        dicaLabel = new JLabel("Jogador " + playerNumber + " - Dica: " + dica);
        dicaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dicaLabel.setForeground(Color.WHITE);
        dicaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(dicaLabel);

        backgroundPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Campo de texto para o palpite
        guessField = new JTextField();
        guessField.setFont(new Font("Arial", Font.PLAIN, 14));
        guessField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 182, 193), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        guessField.setMaximumSize(new Dimension(300, 30));
        backgroundPanel.add(guessField);

        backgroundPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Botão para submeter o palpite
        submitButton = new JButton("Submeter Palpite");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(new Color(255, 105, 180));
        submitButton.setForeground(Color.WHITE);
        submitButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(submitButton);

        backgroundPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Área de resultado
        resultadoArea = new JTextArea(5, 30);
        resultadoArea.setEditable(false);
        resultadoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        resultadoArea.setForeground(new Color(70, 70, 70));
        resultadoArea.setBackground(new Color(255, 255, 255, 180));
        resultadoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 182, 193), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        JScrollPane scrollPane = new JScrollPane(resultadoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        backgroundPanel.add(scrollPane);

        // Adicionar o painel à janela
        add(backgroundPanel);

        // Ação do botão
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // A validação do palpite agora acontece aqui e o palpite é enviado ao servidor.
                String palpite = guessField.getText().trim().toUpperCase();
                if (!palpite.isEmpty()) {
                    palpiteEnviado = palpite; // Sinaliza que há um palpite para ser pego
                }
                guessField.setText(""); // Limpa o campo para o próximo palpite
            }
        });
    }

    // Este método agora é chamado pela ClientGUI
    public void setClientGUI(ClientGUI gui) {
        this.clientGUI = gui;
    }

    // Método que retorna o palpite do jogador (agora para ser chamado pela ClientGUI)
    public String getPalpite() {
        String currentPalpite = null;
        while (currentPalpite == null) {
            // Espera até que um palpite seja submetido pelo botão
            if (palpiteEnviado != null) {
                currentPalpite = palpiteEnviado;
                palpiteEnviado = null; // Reseta para o próximo palpite
            } else {
                try {
                    Thread.sleep(100); // Espera curta para não sobrecarregar a CPU
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return currentPalpite;
    }


    // Método para exibir a janela de forma assíncrona
    public void exibirTela() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    public void exibirResultado(String rankingFinal) {
        // Remove todos os componentes atuais
        getContentPane().removeAll();

        // Cria um novo painel com layout BoxLayout
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Adiciona gradiente de fundo
        resultPanel = new JPanel() {
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
        };
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titleLabel = new JLabel("🏆 Resultado Final 🏆");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        // Área de texto para o ranking (com estilização)
        JTextArea rankingArea = new JTextArea(rankingFinal);
        rankingArea.setEditable(false);
        rankingArea.setFont(new Font("Arial", Font.BOLD, 16));
        rankingArea.setForeground(new Color(70, 70, 70));
        rankingArea.setBackground(new Color(255, 255, 255, 180));
        rankingArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Botão para fechar
        JButton closeButton = new JButton("Fechar");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(255, 255, 255));
        closeButton.setForeground(new Color(255, 105, 180));
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        closeButton.addActionListener(e -> dispose());

        resultPanel.add(Box.createVerticalGlue());
        resultPanel.add(titleLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JScrollPane scrollPane = new JScrollPane(rankingArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        resultPanel.add(scrollPane);

        resultPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        resultPanel.add(closeButton);
        resultPanel.add(Box.createVerticalGlue());

        add(resultPanel);
        revalidate();
        repaint();

    }

    public void atualizarTela(String palavraSecreta, String dica, int playerNumber) {
        this.palavraSecreta = palavraSecreta;
        dicaLabel.setText("Jogador " + playerNumber + " - Dica: " + dica);
        guessField.setText(""); // Limpa o campo de palpite
        resultadoArea.append("\nNova rodada! Dica: " + dica + "\n");
    }
}