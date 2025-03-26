import java.io.*;
import java.net.Socket;
import java.util.Scanner;

//essa classe cria conexoes e cria um ClientHandler pra cada conexao
public class GameClient {
    private static final String SERVER_ADRESS = "localhost"; //se o servidor virar remoto tem que trocar aqui
    private static final int SERVER_PORT = 12345;




    public static void main(String[] args){
        try (Socket socket = new Socket(SERVER_ADRESS, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor");

            //lendo mensagens do servidor e exibindo no terminal do cliente
            new Thread(() -> {
                try {
                    String serverMessage;
                    while((serverMessage = input.readLine()) != null){
                        System.out.println(serverMessage);
                    }
                } catch (IOException e){
                    System.out.println("Conexão encerrada");
                }
            }).start();

            //enviando tentativas pro servidor
            while(true){
                //System.out.println("Digite sua tentativa: "); essa linha ta sendo exibida antes das do ClientHandler, colocar algo diferente aqui
                String guess = scanner.nextLine();
                output.println(guess);
            }

        } catch (IOException e){
            System.out.println("Erro ao conectar servidor: " + e.getMessage());
        }
    }

}
