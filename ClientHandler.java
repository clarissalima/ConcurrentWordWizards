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
    // REMOVIDO: private TelaDeJogo tela; // A tela agora é do lado do cliente

    public ClientHandler(Socket socket, int playerNumber, Partida partida) {
        this.clientSocket = socket;
        this.playerNumber = playerNumber;
        this.partida = partida;
        // REMOVIDO: List<Socket> listaDeSockets = List.of(); partida.setSockets(listaDeSockets);
        // O socket é gerenciado pela própria thread, não precisa de uma lista global aqui.
    }


    @Override
    public void run() {
        jogadorTerminou = false;
        // REMOVIDO: tela = null; // A tela agora é do lado do cliente

        try {
            // REMOVIDO: Initialize game interface try { tela = new TelaDeJogo("", "", playerNumber); tela.exibirTela(); } catch (Exception e) { ... }

            //Set up I/O streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            //Send welcome message - Já é feito pelo ClientGUI ao entrar na partida
            // output.println("Bem-vindo ao jogo! Você tem " + ROUNDS + " rodadas."); // REMOVIDO ou adaptado

            //Start game timer (only for first player) - Lógica de temporizador já está na partida
            // if (playerNumber == 1) {
            //    partida.setSockets(Collections.singletonList(clientSocket)); // Isso não faz sentido aqui, sockets são de cada handler
            //    partida.iniciarTemporizador();
            //
            //    // Notify all players game has started
            //    String startMsg = "PARTIDA_INICIADA|" + partida.getGameDuration();
            //    partida.enviarParaTodos(startMsg);
            // }

            // Game rounds loop
            // O timer será iniciado pelo primeiro jogador no servidor quando a partida estiver cheia.
            // O ClientHandler apenas aguarda por esse sinal para iniciar as rodadas.
            output.println("VOCE_ENTROU_PARTIDA_AGUARDANDO_INICIO"); // Mensagem para o cliente saber que está conectado e aguardando

            // Esperar o início da partida
            // Este loop espera até que a partida seja marcada como iniciada
            // A flag `gameStarted` no ClientGUI será definida por uma mensagem do servidor
            while (!partida.estaEncerrada() && !partida.clients.stream().allMatch(c -> c.playerNumber > 0)) {
                // Pequena pausa para evitar spin lock, mas a lógica de iniciar as rodadas
                // deve ser acionada de forma mais robusta, como por uma mensagem do servidor
                // ou pela verificação de que todos os jogadores estão conectados.
                Thread.sleep(100); // Aguarda para não consumir CPU
            }

            // O timer do jogo começa quando a partida está cheia
            startTime = System.currentTimeMillis(); // Inicia o timer para este jogador

            for (int i = 0; i < ROUNDS && !partida.estaEncerrada(); i++) {
                String[] wordInfo = partida.getPalavraDaRodada(i);
                String secretWord = wordInfo[0]; // A palavra real
                String hint = wordInfo[1];       // A dica

                // Envia a palavra secreta e a dica para o cliente para que a GUI possa processar o palpite localmente
                output.println("DICA|" + secretWord + "|" + hint);
                output.println("\nRodada " + (i + 1) + "/" + ROUNDS);
                output.println("Dica: " + hint);

                //Word guessing loop
                boolean acertou = false;
                while (!acertou && !partida.estaEncerrada()) {
                    String guess = input.readLine(); // Agora recebe o palpite do cliente

                    if (guess == null) { // Cliente desconectou
                        System.out.println("Cliente " + playerNumber + " desconectou.");
                        break; // Sai do loop de palpite
                    }

                    guess = guess.trim().toUpperCase();

                    // O ClientHandler apenas valida se o palpite foi correto para pontuar
                    // A validação inicial já é feita na TelaDeJogo do cliente.
                    // Esta validação aqui é uma segurança/confirmação.
                    if (guess.equals(secretWord)) {
                        score += 10;
                        output.println("Acertou! +10 pontos (Total: " + score + ")");
                        acertou = true;
                    } else {
                        output.println("Errado! Tente novamente");
                    }
                }
            }

            // 7. Game ending
            endTime = System.currentTimeMillis();
            output.println("FIM DE JOGO! Pontuação: " + score);

            // Register score
            boolean ultimo = partida.registrarRanking(playerNumber, score, endTime - startTime);

            // Send final ranking if last player
            if (ultimo) {
                String ranking = partida.getRankingFinal(); // CORRIGIDO: de obterRankingFinal() para getRankingFinal()
                partida.enviarRankingParaTodos(ranking); // Envia o ranking para todos os clientes
                System.out.println("Ranking enviado para todos os jogadores!");
                partida.encerrarJogo(); // Encerrar a partida no lado do servidor
            } else {
                // Se não é o último, o jogador termina mas aguarda o ranking final
                output.println("AGUARDANDO_OUTROS_JOGADORES");
            }


        } catch (IOException e) {
            System.out.println("Erro com jogador " + playerNumber + ": " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("ClientHandler para jogador " + playerNumber + " foi interrompido.");
            Thread.currentThread().interrupt();
        } finally {
            // Garante que o jogador seja marcado como terminado para a verificação da partida
            jogadorTerminou = true;
            partida.verificaTerminoJogo();
            // A partida será encerrada pelo último jogador a registrar o ranking,
            // que chamará encerrarJogo(), que por sua vez chamará encerrar() para cada ClientHandler.
            // Aqui, apenas garantir que o socket seja fechado se ainda estiver aberto.
            if (!clientSocket.isClosed() && !partida.estaEncerrada()) {
                encerrar(); // Fecha o socket se a partida não estiver encerrada
            }
        }
    }

    public void enviarRanking(String rankingFinal) {
        if (output != null) {
            output.println(rankingFinal);
            output.flush(); // Garante que a mensagem seja enviada imediatamente
        }
    }

    public void exibirResultado(String rankingFinal) {
        // Esta função agora é chamada do servidor, mas a exibição REAL
        // será tratada pelo ClientGUI através das mensagens recebidas.
        // O ClientGUI receberá a string do ranking e passará para a TelaDeJogo.
        // Portanto, aqui não fazemos nada visual.
    }

    public void encerrar() {
        try {
            if (output != null) {
                output.println("O jogo acabou! Obrigado por jogar.");
                output.flush();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.out.println("Conexão encerrada para o jogador " + playerNumber);
        } catch (IOException e) {
            System.out.println("Erro ao encerrar conexão com o jogador " + playerNumber + ": " + e.getMessage());
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