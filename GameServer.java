import java.io.*;
import java.net.*;
import java.util.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    private static final int PORT = 12345;
    private static final String[][] WORDS = {
            {"JAVA", "Linguagem de programação orientada a objetos."},
            {"SOCKET", "Usado para comunicação em rede."},
            {"TCP", "Protocolo de comunicação confiável, orientado à conexão."},
            {"UDP", "Protocolo de comunicação sem conexão e não confiável."},
            {"JOIN", "Método usado para garantir que uma thread espere pela conclusão de outra thread."},
            {"THREAD", "Execução paralela dentro de um programa."},
            {"DEADLOCK", "Situação onde dois ou mais processos ficam bloqueados esperando uns aos outros."},
            {"NETWORK", "Conjunto de computadores interconectados."}
    };
    // Cria uma lista de clientes conectados
    private static final List<ClientHandler> clients = new ArrayList<>();
    // Instanciar a lista que tera as palavras do jogo
    private static final List<String[]> palavrasRodadas = new ArrayList<>();
    //Para diferenciar os jogadores
    private static int totalPlayers = 0;

    private String secretWord;
    private String hint;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            // Perguntar quantos jogadores participarão
            System.out.print("Quantos jogadores vão participar? ");
            totalPlayers = Integer.parseInt(consoleInput.readLine().trim());

            // Gerar palavras para todas as rodadas antes de os jogadores entrarem
            gerarPalavrasParaRodadas();

            // Esperar todos os jogadores conectarem
            while (clients.size() < totalPlayers) {
                System.out.println("Aguardando jogador...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo jogador conectado!");

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size() + 1);
                clients.add(clientHandler);
            }

            System.out.println("Todos os jogadores conectados! O jogo começou.");

            // Iniciar jogo para cada cliente
            for (ClientHandler client : clients) {
                client.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gera uma lista fixa de palavras aleatórias para cada rodada
    private static void gerarPalavrasParaRodadas() {
        Random random = new Random();
        // Para cada Round adiciona uma palavra aleatoria na lista palavras rodadas
        for (int i = 0; i < ClientHandler.ROUNDS; i++) {   // Acessando ROUNDS do ClientHandler
            palavrasRodadas.add(WORDS[random.nextInt(WORDS.length)]);
        }
    }

    // Permite que os ClientHandlers acessem a palavra da rodada
    public static String[] getPalavraDaRodada(int rodada) {
        // Retorna o array[palavra][dica] da palavra da lista --> isso que garante que as palavras sejam iguais para cada cliente
        return palavrasRodadas.get(rodada);
    }
}
