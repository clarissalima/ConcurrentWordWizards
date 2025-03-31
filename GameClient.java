import java.io.*;
import java.net.Socket;
import java.util.Scanner;

//essa classe cria conexoes e cria um ClientHandler pra cada conexao (cliente)
public class GameClient {
    private static final String SERVER_ADDRESS = "localhost"; //se o servidor virar remoto tem que trocar aqui
    private static final int SERVER_PORT = 12345;




    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Iniciando cliente...");

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Conectado ao servidor!");

            // Thread para ler mensagens do servidor e exibir na tela
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = input.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Conexão encerrada.");
                }
            }).start();

            // Enviar tentativas para o servidor
            while (true) {
                String guess = scanner.nextLine();
                output.println(guess);
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }

        scanner.close();
    }
}