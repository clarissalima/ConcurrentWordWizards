import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankingPanel extends JPanel {

    public RankingPanel(List<Partida.RankingJogador> ranking) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Ranking Final", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        add(titulo, BorderLayout.NORTH);

        JPanel rankingList = new JPanel();
        rankingList.setLayout(new BoxLayout(rankingList, BoxLayout.Y_AXIS));

        for (int i = 0; i < Math.min(3, ranking.size()); i++) {
            Partida.RankingJogador r = ranking.get(i);
            String texto = String.format(
                    "%dº lugar - Jogador %d | Pontos: %d | Tempo: %.2f segundos",
                    i + 1, r.playerNumber, r.score, r.tempo / 1000.0
            );

            JLabel label = new JLabel(texto);
            label.setFont(new Font("Monospaced", Font.PLAIN, 16));
            rankingList.add(label);
            rankingList.add(Box.createVerticalStrut(10)); // espaçamento entre linhas
        }

        add(rankingList, BorderLayout.CENTER);

        JLabel fim = new JLabel("O jogo acabou! Obrigado por jogar.", SwingConstants.CENTER);
        fim.setFont(new Font("Arial", Font.ITALIC, 14));
        fim.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        add(fim, BorderLayout.SOUTH);
    }

    // Método auxiliar para exibir em uma janela separada
    public static void mostrarRanking(List<Partida.RankingJogador> ranking) {
        JFrame frame = new JFrame("Ranking Final");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(new RankingPanel(ranking));
        frame.setVisible(true);
    }
}
