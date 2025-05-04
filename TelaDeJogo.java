import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class TelaDeJogo extends JFrame {
    private String palavraSecreta;
    private JTextField guessField;
    private JButton submitButton;
    private JLabel dicaLabel, statusLabel, fimJogo, mensagemLabel;
    private JTextArea resultadoArea;
    private String palpite;
    private int playerNumber; // Guarda o palpite do jogador

    public TelaDeJogo(String palavraSecreta, String dica, int playerNumber) {
        this.palavraSecreta = palavraSecreta;
        this.playerNumber = playerNumber;

        // Configurações da janela
        setTitle("Jogo de Adivinhação de Palavras");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Criar painel e layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Exibir dica
        dicaLabel = new JLabel("Jogador " + playerNumber + " - Dica: " + dica);
        panel.add(dicaLabel);

        // Campo de texto para o palpite
        guessField = new JTextField();
        panel.add(guessField);

        // Botão para submeter o palpite
        submitButton = new JButton("Submeter Palpite");
        panel.add(submitButton);

        // Status do jogo
        statusLabel = new JLabel("Tente adivinhar a palavra!");
        panel.add(statusLabel);

        // Área de resultado
        resultadoArea = new JTextArea(5, 30);
        resultadoArea.setEditable(false);
        panel.add(new JScrollPane(resultadoArea));

        // Adicionar o painel à janela
        add(panel);

        // Ação do botão
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checarPalpite();
            }
        });
    }

    // Método que checa o palpite do jogador
    private void checarPalpite() {
        palpite = guessField.getText().trim().toUpperCase();

        // Verifica se o palpite está correto
        if (palpite.equals(palavraSecreta)) {
            resultadoArea.append("Parabéns! Você acertou a palavra: " + palavraSecreta + "\n");
        } else {
            resultadoArea.append("Palavra errada. Tente novamente!\n");
        }

        guessField.setText("");
    }

    // Método que retorna o palpite do jogador
    public String getPalpite() {
        palpite = null;
        while (palpite == null) {
            try {
                Thread.sleep(100); // Espera curta para não sobrecarregar a CPU
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return palpite;
    }

    public void atualizarMensagem(String mensagem) {
        dicaLabel.setText(mensagem);
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
//
//    public void exibirResultado(String rankingFinal) {
//        dicaLabel.setText("Fim de jogo");
//        resultadoArea.append(rankingFinal);
//    }

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

        // Adiciona os componentes ao painel
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

        // Adiciona o painel ao frame e atualiza
        add(resultPanel);
        revalidate();
        repaint();
//
//        // Centraliza a janela novamente
//        setLocationRelativeTo(null);
    }

    public void atualizarTela(String palavraSecreta, String dica, int playerNumber) {
        this.palavraSecreta = palavraSecreta;
        dicaLabel.setText("Jogador " + playerNumber + " - Dica: " + dica);
        guessField.setText(""); // Limpa o campo de palpite
        resultadoArea.append("\nNova rodada! Dica: " + dica + "\n");
    }


//    // Método principal apenas para testes, não faz parte da solução final
//    public static void main(String[] args) {
//        TelaDeJogo tela = new TelaDeJogo("EXEMPLO", "É algo comum na informática.");
//        tela.exibirTela();
//    }
}
