import javax.swing.*;
import java.awt.event.*;

public class TelaDeJogo extends JFrame {
    private String palavraSecreta;
    private JTextField guessField;
    private JButton submitButton;
    private JLabel dicaLabel, statusLabel;
    private JTextArea resultadoArea;
    private String palpite;  // Guarda o palpite do jogador

    public TelaDeJogo(String palavraSecreta, String dica) {
        this.palavraSecreta = palavraSecreta;

        // Configurações da janela
        setTitle("Jogo de Adivinhação de Palavras");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Criar painel e layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Exibir dica
        dicaLabel = new JLabel("Dica: " + dica);
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

    // Método para exibir a janela de forma assíncrona
    public void exibirTela() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    public void atualizarTela(String palavraSecreta, String dica) {
        this.palavraSecreta = palavraSecreta;
        dicaLabel.setText("Dica: " + dica);
        guessField.setText(""); // Limpa o campo de palpite
        resultadoArea.append("\nNova rodada! Dica: " + dica + "\n");
    }


    // Método principal apenas para testes, não faz parte da solução final
    public static void main(String[] args) {
        TelaDeJogo tela = new TelaDeJogo("EXEMPLO", "É algo comum na informática.");
        tela.exibirTela();
    }
}
