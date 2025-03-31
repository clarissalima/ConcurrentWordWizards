import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("CallToPrintStackTrace")
public class GameServer {
    //como nao temos autorizacao IANA, devemos usar uma porta entre 49152 e 65535
    private static final int PORT = 49160;
    private static final String[][] WORDS = {
            {"JAVA", "Linguagem de programação orientada a objetos."},
            {"SOCKET", "Usado para comunicação em rede."},
            {"TCP", "Protocolo de comunicação confiável, orientado à conexão."},
            {"UDP", "Protocolo de comunicação sem conexão e não confiável."},
            {"JOIN", "Metodo usado para garantir que uma thread espere pela conclusão de outra thread."},
            {"THREAD", "Execução paralela dentro de um programa."},
            {"DEADLOCK", "Situação onde dois ou mais processos ficam bloqueados esperando uns aos outros."},
            {"NETWORK", "Conjunto de computadores interconectados."},
            {"CLIENTE", "Solicita serviços de um servidor e inicia a interação na comunicação em rede."},
            {"SERVIDOR", "Fornece serviços a clientes e opera continuamente."},
            {"PORTA", "Identificador numérico usado para distinguir processos em comunicação de rede."},
            {"BUFFER", "Área de memória usada para armazenar temporariamente dados em transmissão."},
            {"EXCLUSÃO MÚTUA", "Mecanismo que impede que múltiplos processos acessem simultaneamente uma seção crítica."},
            {"DEKKER", "Algoritmo que garante exclusão mútua para dois processos."},
            {"PETERSON", "Algoritmo eficiente para exclusão mútua entre dois processos concorrentes."},
            {"DIJKSTRA", "Algoritmo que coordena múltiplos processos garantindo exclusão mútua."},
            {"BAKERY", "Algoritmo inspirado em senhas de padaria para controlar o acesso concorrente."},
            {"BUFFER TX", "Armazena mensagens antes do envio para o destino."},
            {"BUFFER RX", "Recebe mensagens enviadas antes de serem processadas."},
            {"INPUTSTREAM", "Classe Java usada para leitura de dados em um Socket TCP."},
            {"OUTPUTSTREAM", "Classe Java usada para escrita de dados em um Socket TCP."},
            {"STARVATION", "Situação onde um processo nunca ganha acesso à seção crítica devido à prioridade de outros."},
            {"DEADLOCK", "Situação onde dois ou mais processos ficam bloqueados esperando uns aos outros."},
            {"MUTEX", "Mecanismo de sincronização que permite apenas um processo por vez em uma seção crítica."},
            {"SEMAFORO", "Estrutura usada para controle de acesso concorrente em seções críticas."},
            {"WAIT", "Operação que bloqueia um processo até que uma condição seja atendida."},
            {"PROCESSO", "Programa em execução que pode conter múltiplas threads."},
            {"MONITOR", "Estrutura que combina bloqueios e variáveis de condição para sincronização de processos."},
            {"ATOMICIDADE", "Propriedade onde uma operação ocorre completamente ou não ocorre."},

    };

    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final List<String[]> palavrasRodadas = new ArrayList<>();
    private static int totalPlayers = 0;
    private static final int GAME_DURATION_SECONDS = 30;
    //agenda tarefa que vai ser chamado depois do GMAE_DURATION_SECONDS: vai chamar o encerrarJogo()
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            System.out.print("Quantos jogadores vão participar? ");
            totalPlayers = Integer.parseInt(consoleInput.readLine().trim());

            gerarPalavrasParaRodadas();

            while (clients.size() < totalPlayers) {
                System.out.println("Aguardando jogador...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo jogador conectado!");

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size() + 1);
                clients.add(clientHandler);
            }

            System.out.println("Todos os jogadores conectados! O jogo começou.");

            iniciarTemporizador();
            for (ClientHandler client : clients) {
                client.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void iniciarTemporizador() {
        scheduler.schedule(() -> {
            System.out.println("Tempo acabou! Encerrando o jogo.");
            encerrarJogo();
        }, GAME_DURATION_SECONDS, TimeUnit.SECONDS);

        System.out.println("O temporizador de " + GAME_DURATION_SECONDS + " segundos começou.");
    }

    private static void encerrarJogo() {
        try {
            for (ClientHandler client : clients) {
                client.encerrar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdown();
            System.out.println("Jogo encerrado para todos os jogadores.");
            System.exit(0);
        }
    }

    private static void gerarPalavrasParaRodadas() {
        Random random = new Random();
        List<String[]> shuffledWords = new ArrayList<>(Arrays.asList(WORDS));
        Collections.shuffle(shuffledWords, random);

        for (int i = 0; i < ClientHandler.ROUNDS; i++) {
            palavrasRodadas.add(shuffledWords.get(i));
        }
    }

    public static String[] getPalavraDaRodada(int rodada) {
        return palavrasRodadas.get(rodada);
    }
}
