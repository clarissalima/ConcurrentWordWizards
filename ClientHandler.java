import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

// Essa classe trata cada cliente que se conecta ao servidor
public class ClientHandler extends Thread {
    final Socket clientSocket;
    BufferedReader input;
    PrintWriter output;
    int score = 0;
    private long startTime;
    private long endTime;
    public static final int ROUNDS = 4;
    final int playerNumber;
    private final Partida partida;
    private boolean jogadorTerminou = false;
    private TelaDeJogo tela;

    public ClientHandler(Socket socket, int playerNumber, Partida partida) {
        this.clientSocket = socket;
        this.playerNumber = playerNumber;
        this.partida = partida;
        List<Socket> listaDeSockets = List.of();
        partida.setSockets(listaDeSockets);
    }


    @Override
    public void run() {
        jogadorTerminou = false;
        tela = null;

        try {
            // 1. Initialize game interface
            try {
                tela = new TelaDeJogo("", "", playerNumber);
                tela.exibirTela();
            } catch (Exception e) {
                System.out.println("Erro na interface do jogador " + playerNumber + ": " + e.getMessage());
                return;
            }

            // 2. Set up I/O streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // 3. Send welcome message
            output.println("Bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas.");
            startTime = System.currentTimeMillis();

            // 4. Start game timer (only for first player)
            if (playerNumber == 1) {
                partida.setSockets(Collections.singletonList(clientSocket));
                partida.iniciarTemporizador();

                // Notify all players game has started
                String startMsg = "PARTIDA_INICIADA|" + partida.getGameDuration();
                partida.enviarParaTodos(startMsg);
            }

            // 5. Game rounds loop
            for (int i = 0; i < ROUNDS && !partida.estaEncerrada(); i++) {
                String[] wordInfo = partida.getPalavraDaRodada(i);
                String secretWord = wordInfo[0];
                String hint = wordInfo[1];

                // Update interface
                tela.atualizarTela(secretWord, hint, playerNumber);
                output.println("\nRodada " + (i + 1) + "/" + ROUNDS);
                output.println("Dica: " + hint);

                // 6. Word guessing loop
                boolean acertou = false;
                while (!acertou && !partida.estaEncerrada()) {
                    String guess = tela.getPalpite();


                    if (guess == null || guess.isEmpty()) continue;

                    guess = guess.trim().toUpperCase();

                    if (guess.equals(secretWord)) {
                        score += 10;
                        output.println("Acertou! +10 pontos (Total: " + score + ")");
                        acertou = true;
                    } else {
                        output.println("Errado! Tente novamente");
                    }
                }

//                if (partida.estaEncerrada()) {
//                    Thread.currentThread().interrupt(); // Força sair do loop
//                    break;
//                }
            }

            // 7. Game ending
            endTime = System.currentTimeMillis();
            output.println("FIM DE JOGO! Pontuação: " + score);

            // Register score
            boolean ultimo = partida.registrarRanking(playerNumber, score, endTime - startTime);

            // Send final ranking if last player
            if (ultimo) {

                String ranking = partida.obterRankingFinal();
                partida.enviarRankingParaTodos(ranking);
                System.out.println("Ranking enviado para todos os jogadoresSS");
                partida.encerrarJogo();

            }

        } catch (IOException e) {
            System.out.println("Erro com jogador " + playerNumber + ": " + e.getMessage());
        } finally {

            if (partida.estaEncerrada()) {
                System.out.println("[DEBUG] Entrou no finally - Jogador " + playerNumber);
                jogadorTerminou = true;
                partida.verificaTerminoJogo();
                partida.encerrarJogo();
                encerrar();
            }

        }
    }

    public void enviarRanking(String rankingFinal) {
        if (output != null) {
            output.println(rankingFinal);
        }
    }

    public void exibirResultado(String rankingFinal) {
        tela.exibirResultado(rankingFinal);
    }

    public void encerrar() {
        try {
            if (output != null) {
                output.println("O jogo acabou! Obrigado por jogar.");
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Erro ao encerrar conexão com o jogador " + playerNumber);
        }
    }

    public boolean terminou() {
        return jogadorTerminou;
    }

    public void enviarMensagem(String mensagem) {
        if (output != null) {
            output.println(mensagem);  // Usa o PrintWriter já existente
            output.flush();
        } else {
            System.err.println("Erro: output não inicializado para jogador " + playerNumber);
        }
    }


}