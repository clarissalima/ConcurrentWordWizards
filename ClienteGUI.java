import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientGUI extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 49160;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private JPanel currentPanel;

    // Construtor
    public ClientGUI() {
        setTitle("Jogo de Adivinhação - Cliente");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Conectado ao servidor!");
            showMainMenu(); // Mostra o menu principal após a conexão
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao conectar ao servidor: " + e.getMessage() + "\nCertifique-se de que o servidor está rodando.",
                    "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Sai se não conseguir conectar
        }
    }

    // Métodos para navegação entre telas
    private void setCurrentPanel(JPanel panel) {
        if (currentPanel != null) {
            remove(currentPanel);
        }
        currentPanel = panel;
        add(currentPanel);
        revalidate();
        repaint();
    }

    // --- Telas da GUI ---

    // 1. Main Menu
    private void showMainMenu() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("🎀 Jogo de Adivinhação de Palavras 🎀");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        JLabel clientInfoLabel = new JLabel("Cliente conectado ao servidor em " + SERVER_ADDRESS + ":" + SERVER_PORT);
        clientInfoLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        clientInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        clientInfoLabel.setForeground(Color.WHITE);

        JButton newGameButton = createStyledButton("Criar Nova Partida");
        JButton listGamesButton = createStyledButton("Listar Partidas Existentes");

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(clientInfoLabel);
        panel.add(Box.createVerticalGlue());
        panel.add(newGameButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(listGamesButton);
        panel.add(Box.createVerticalGlue());

        newGameButton.addActionListener(e -> showCreateGameScreen());
        listGamesButton.addActionListener(e -> showGamesList());

        setCurrentPanel(panel);
    }

    // 2. Create Game Screen
    private void showCreateGameScreen() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Criar Nova Partida");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        // Dificuldade
        JPanel difficultyPanel = new JPanel();
        difficultyPanel.setOpaque(false);
        difficultyPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        ButtonGroup difficultyGroup = new ButtonGroup();
        JRadioButton easyButton = createRadioButton("Fácil", "facil");
        JRadioButton mediumButton = createRadioButton("Médio", "medio");
        JRadioButton hardButton = createRadioButton("Difícil", "dificil");

        difficultyGroup.add(easyButton);
        difficultyGroup.add(mediumButton);
        difficultyGroup.add(hardButton);
        mediumButton.setSelected(true);

        difficultyPanel.add(easyButton);
        difficultyPanel.add(mediumButton);
        difficultyPanel.add(hardButton);

        // Jogadores
        JPanel playersPanel = new JPanel();
        playersPanel.setOpaque(false);
        playersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JLabel playersLabel = new JLabel("Número de Jogadores:");
        playersLabel.setForeground(Color.WHITE);
        playersLabel.setFont(new Font("Arial", Font.BOLD, 16));

        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
        JSpinner playersSpinner = new JSpinner(spinnerModel);
        playersSpinner.setPreferredSize(new Dimension(60, 30));

        playersPanel.add(playersLabel);
        playersPanel.add(playersSpinner);

        // Botões
        JButton startButton = createStyledButton("Criar Partida");
        JButton backButton = createStyledButton("Voltar ao Menu");

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(difficultyPanel);
        panel.add(playersPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(startButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(backButton);
        panel.add(Box.createVerticalGlue());

        startButton.addActionListener(e -> {
            String difficulty = "";
            if (easyButton.isSelected()) difficulty = "facil";
            else if (mediumButton.isSelected()) difficulty = "medio";
            else if (hardButton.isSelected()) difficulty = "dificil";

            int players = (int) playersSpinner.getValue();

            // Envia o comando para o servidor
            sendCommand("CRIAR_PARTIDA|" + difficulty + "|" + players);

            // Espera a resposta do servidor
            new Thread(() -> {
                try {
                    String response = input.readLine();
                    if (response != null && response.startsWith("PARTIDA_CRIADA")) {
                        int partidaId = Integer.parseInt(response.split("\\|")[1]);
                        SwingUtilities.invokeLater(() -> showGameCreatedScreen(partidaId));
                    } else if (response != null && response.startsWith("ERRO")) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                                "Erro ao criar partida: " + response.split("\\|")[1],
                                "Erro", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Erro de comunicação: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        backButton.addActionListener(e -> showMainMenu());
        setCurrentPanel(panel);
    }

    // 3. Game Created Screen
    private void showGameCreatedScreen(int partidaId) {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel successLabel = new JLabel("Partida #" + partidaId + " criada com sucesso!");
        successLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 24));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successLabel.setForeground(Color.WHITE);

        JLabel waitingLabel = new JLabel("Aguardando jogadores se conectarem...");
        waitingLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        waitingLabel.setForeground(Color.WHITE);

        JButton backButton = createStyledButton("Voltar ao Menu");
        panel.add(Box.createVerticalGlue());
        panel.add(successLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(waitingLabel);
        panel.add(Box.createVerticalGlue());
        panel.add(backButton);

        backButton.addActionListener(e -> showMainMenu());
        setCurrentPanel(panel);
    }

    // 4. Games List Screen
    private void showGamesList() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Partidas Existentes");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);

        JPanel gamesPanel = new JPanel();
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        gamesPanel.setOpaque(false); // Manter transparente para o gradiente

        // Solicita a lista de partidas ao servidor
        sendCommand("LISTAR_PARTIDAS");

        new Thread(() -> {
            try {
                String response = input.readLine();
                SwingUtilities.invokeLater(() -> {
                    gamesPanel.removeAll(); // Limpa antes de adicionar
                    if (response != null && response.startsWith("LISTA_PARTIDAS")) {
                        String data = response.substring("LISTA_PARTIDAS|".length());
                        if (data.equals("Nenhuma partida ativa no momento.")) {
                            JLabel emptyLabel = new JLabel("Nenhuma partida ativa no momento");
                            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            emptyLabel.setForeground(new Color(240, 240, 240));
                            gamesPanel.add(emptyLabel);
                        } else {
                            String[] partidasInfo = data.split(",");
                            for (String pInfo : partidasInfo) {
                                String[] details = pInfo.split(";");
                                if (details.length == 4) {
                                    try {
                                        int id = Integer.parseInt(details[0]);
                                        String modo = details[1];
                                        int currentPlayers = Integer.parseInt(details[2]);
                                        int totalPlayers = Integer.parseInt(details[3]);
                                        // Cria um "card" para cada partida
                                        JPanel cardPanel = createGameCard(id, modo, currentPlayers, totalPlayers);
                                        gamesPanel.add(cardPanel);
                                        gamesPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                                    } catch (NumberFormatException ex) {
                                        System.err.println("Erro ao parsear info da partida: " + pInfo);
                                    }
                                }
                            }
                        }
                    } else if (response != null && response.startsWith("ERRO")) {
                        JOptionPane.showMessageDialog(this,
                                "Erro ao listar partidas: " + response.split("\\|")[1],
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    gamesPanel.revalidate();
                    gamesPanel.repaint();
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Erro de comunicação ao listar partidas: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE));
            }
        }).start();

        scrollPane.setViewportView(gamesPanel);
        JButton backButton = createStyledButton("Voltar ao Menu Principal");
        backButton.addActionListener(e -> showMainMenu());

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scrollPane);
        panel.add(Box.createVerticalGlue());
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        setCurrentPanel(panel);
    }

    private JPanel createGameCard(int id, String modo, int currentPlayers, int totalPlayers) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(new Color(255, 255, 255, 200));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 220, 150), 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        cardPanel.setMaximumSize(new Dimension(450, 90));
        cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        infoPanel.setOpaque(false);

        JLabel idLabel = new JLabel("PARTIDA " + id);
        idLabel.setFont(new Font("Arial", Font.BOLD, 16));
        idLabel.setForeground(new Color(60, 60, 60));

        JLabel modeLabel = new JLabel(modo.toUpperCase());
        modeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        modeLabel.setForeground(getColorByMode(modo));

        infoPanel.add(idLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        infoPanel.add(modeLabel);

        JLabel playersLabel = new JLabel(currentPlayers + "/" + totalPlayers);
        playersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        playersLabel.setForeground(new Color(100, 100, 100));

        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(playersLabel, BorderLayout.EAST);

        JProgressBar progressBar = new JProgressBar(0, totalPlayers);
        progressBar.setValue(currentPlayers);
        progressBar.setForeground(getColorByMode(modo).darker());
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setPreferredSize(new Dimension(0, 6));

        cardPanel.add(headerPanel, BorderLayout.NORTH);
        cardPanel.add(progressBar, BorderLayout.SOUTH);

        cardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cardPanel.setBackground(new Color(255, 255, 255, 230));
                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(getColorByMode(modo), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                cardPanel.setBackground(new Color(255, 255, 255, 200));
                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 215, 220, 150), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }

            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Tenta entrar na partida
                    sendCommand("ENTRAR_PARTIDA|" + id);

                    new Thread(() -> {
                        try {
                            String response = input.readLine();
                            if (response != null && response.startsWith("ENTROU_PARTIDA")) {
                                int partidaId = Integer.parseInt(response.split("\\|")[1]);
                                int playerNumber = Integer.parseInt(response.split("\\|")[2]);
                                SwingUtilities.invokeLater(() -> showGameScreen(partidaId, playerNumber));
                                // Inicia a thread que vai escutar as mensagens do jogo
                                startListeningForGameMessages(partidaId, playerNumber);
                            } else if (response != null && response.startsWith("ERRO")) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ClientGUI.this,
                                        "Erro ao entrar na partida: " + response.split("\\|")[1],
                                        "Erro", JOptionPane.ERROR_MESSAGE));
                            } else if (response != null && response.startsWith("AGUARDANDO_JOGADORES")) {
                                String[] parts = response.split("\\|");
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(ClientGUI.this,
                                            "Aguardando mais jogadores... (" + parts[1] + "/" + parts[2] + ")",
                                            "Aguardando", JOptionPane.INFORMATION_MESSAGE);
                                });
                                // Ainda precisa escutar por PARTIDA_INICIADA
                                startListeningForGameMessages(id, currentPlayers); // currentPlayers aqui é apenas placeholder para o playerNumber
                            }
                        } catch (IOException ex) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ClientGUI.this,
                                    "Erro de comunicação ao entrar na partida: " + ex.getMessage(),
                                    "Erro", JOptionPane.ERROR_MESSAGE));
                        }
                    }).start();
                }
            }
        });

        return cardPanel;
    }

    // 5. Game Screen (similar à antiga TelaDeJogo, mas agora gerenciada pelo cliente)
    private TelaDeJogo gameScreen;
    private AtomicBoolean gameStarted = new AtomicBoolean(false);

    private void showGameScreen(int partidaId, int playerNumber) {
        // A TelaDeJogo será gerenciada dentro de uma nova janela ou diretamente aqui.
        // Por simplicidade, vamos usar a TelaDeJogo existente.
        // O cliente precisa de uma maneira de receber a palavra e a dica do servidor.
        // Isso será feito através da thread de escuta.
        gameScreen = new TelaDeJogo("", "", playerNumber); // Inicializa com valores vazios
        gameScreen.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Impede que o jogador feche a janela e desconecte
        gameScreen.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Ao tentar fechar a tela de jogo, volta ao menu principal
                gameScreen.dispose(); // Fecha a janela da tela de jogo
                showMainMenu(); // Volta ao menu principal
            }
        });
        gameScreen.setVisible(true);

        // A lógica de "getPalpite" na TelaDeJogo agora precisa ser ativada por um botão na GUI.
        // O ClientHandler no servidor é que irá enviar as palavras e dicas.
        // E o ClientGUI que irá enviar o palpite para o servidor.
    }

    // Thread para escutar mensagens do servidor após entrar em uma partida
    private void startListeningForGameMessages(int partidaId, int playerNumber) {
        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = input.readLine()) != null) {
                    System.out.println("[Mensagem do Servidor]: " + serverMessage);
                    // Processar mensagens do servidor
                    SwingUtilities.invokeLater(() -> {
                        if (serverMessage.startsWith("PARTIDA_INICIADA")) {
                            gameStarted.set(true);
                            JOptionPane.showMessageDialog(this, "A partida começou!", "Jogo Iniciado", JOptionPane.INFORMATION_MESSAGE);
                            // Pode-se fazer mais coisas aqui, como iniciar o cronômetro na GUI do cliente
                            // int gameDuration = Integer.parseInt(serverMessage.split("\\|")[1]);
                        } else if (serverMessage.startsWith("Rodada")) {
                            // Isso é uma mensagem de rodada do ClientHandler, incluindo a dica
                            // Ex: Rodada 1/4
                            // Dica: Linguagem de programação orientada a objetos.
                            // A palavra secreta será enviada separadamente na ClientHandler quando o palpite é checado.
                            // Para a tela do jogo funcionar, precisamos da palavra secreta aqui também.
                            // Isso exigirá uma pequena mudança no ClientHandler ou uma nova forma de comunicação.

                            // Adaptação: Vamos supor que a mensagem de dica do servidor agora inclui a palavra secreta para a TelaDeJogo.
                            // Ex: "DICA|JAVA|Linguagem de programação orientada a objetos."
                            if (serverMessage.startsWith("DICA|")) {
                                String[] parts = serverMessage.split("\\|");
                                if (parts.length >= 3) {
                                    String secretWord = parts[1];
                                    String hint = parts[2];
                                    if (gameScreen != null) {
                                        gameScreen.atualizarTela(secretWord, hint, playerNumber);
                                    }
                                }
                            } else {
                                if (gameScreen != null) {
                                    gameScreen.resultadoArea.append(serverMessage + "\n");
                                    gameScreen.resultadoArea.setCaretPosition(gameScreen.resultadoArea.getDocument().getLength());
                                }
                            }
                        } else if (serverMessage.startsWith("FIM DE JOGO!")) {
                            if (gameScreen != null) {
                                gameScreen.resultadoArea.append(serverMessage + "\n");
                                gameScreen.resultadoArea.setCaretPosition(gameScreen.resultadoArea.getDocument().getLength());
                            }
                        } else if (serverMessage.startsWith("---------------- Ranking Final ---------------")) {
                            if (gameScreen != null) {
                                gameScreen.exibirResultado(serverMessage); // Exibe o ranking final
                            }
                        } else if (serverMessage.equals("O jogo acabou! Obrigado por jogar.")) {
                            // A conexão será encerrada pelo ClientHandler. O cliente pode voltar ao menu principal.
                            if (gameScreen != null) {
                                gameScreen.dispose(); // Fecha a tela de jogo
                            }
                            showMainMenu(); // Volta para o menu principal
                            JOptionPane.showMessageDialog(ClientGUI.this, "O jogo terminou! Volte ao menu principal para jogar novamente.", "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                            // Certifique-se de que o socket está fechado ou preparado para nova conexão se necessário
                        } else {
                            if (gameScreen != null) {
                                gameScreen.resultadoArea.append(serverMessage + "\n");
                                gameScreen.resultadoArea.setCaretPosition(gameScreen.resultadoArea.getDocument().getLength());
                            }
                        }
                    });
                }
            } catch (IOException e) {
                // Conexão encerrada ou erro
                System.out.println("Conexão com o servidor encerrada: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    if (gameScreen != null) {
                        gameScreen.dispose();
                    }
                    JOptionPane.showMessageDialog(ClientGUI.this,
                            "A conexão com o servidor foi perdida. Por favor, reinicie o cliente.",
                            "Conexão Perdida", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                });
            }
        }).start();
    }

    // --- Métodos Auxiliares para Estilização ---

    private Color getColorByMode(String modo) {
        switch (modo.toLowerCase()) {
            case "facil": return new Color(144, 238, 144);
            case "medio": return new Color(255, 215, 0);
            case "dificil": return new Color(255, 99, 71);
            default: return new Color(173, 216, 230);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(250, 50));
        button.setMaximumSize(new Dimension(250, 50));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(255, 182, 193));
        button.setForeground(new Color(70, 70, 70));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 215, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 182, 193));
            }
        });
        return button;
    }

    private JRadioButton createRadioButton(String text, String actionCommand) {
        JRadioButton button = new JRadioButton(text);
        button.setActionCommand(actionCommand);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setFocusPainted(false);
        return button;
    }

    // Painel com gradiente de fundo
    private static class GradientPanel extends JPanel {
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
    }

    // Método para enviar comandos ao servidor
    public void sendCommand(String command) {
        output.println(command);
    }

    // Método para a TelaDeJogo enviar palpites através da GUI do cliente
    public void sendGuess(String guess) {
        sendCommand(guess);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
}