import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// Esta classe agora apenas inicia a ClientGUI, que gerencia a conexão e a interface.
public class GameClient {
    public static void main(String[] args) {
        // A lógica de conexão e interface agora está dentro de ClientGUI
        // O ClientGUI se conectará ao servidor e exibirá a interface.
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}