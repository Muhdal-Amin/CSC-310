import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class AyoGameGUI {
    private static final int PITS_PER_PLAYER = 6;
    private static final int TOTAL_PITS = 12;
    private static final int INITIAL_SEEDS = 4;
    private int[] board;
    private int[] captured;
    private int currentPlayer;
    private JFrame frame;
    private JButton[] pitButtons;
    private JLabel statusLabel;
    private JLabel player1Score, player2Score;
    private JButton restartButton;

    public AyoGameGUI() {
        board = new int[TOTAL_PITS];
        captured = new int[2];
        currentPlayer = 0;
        initializeBoard();
        initializeGUI();
    }

    private void initializeBoard() {
        for (int i = 0; i < TOTAL_PITS; i++) {
            board[i] = INITIAL_SEEDS;
        }
    }

    private void initializeGUI() {
        frame = new JFrame("Ayo Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel boardPanel = new JPanel(new GridLayout(2, PITS_PER_PLAYER, 10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pitButtons = new JButton[TOTAL_PITS];

        JLabel player2Label = new JLabel("Player 2's Side", SwingConstants.CENTER);
        JLabel player1Label = new JLabel("Player 1's Side", SwingConstants.CENTER);
        player2Label.setFont(new Font("Arial", Font.BOLD, 16));
        player1Label.setFont(new Font("Arial", Font.BOLD, 16));
        
        mainPanel.add(player2Label, BorderLayout.NORTH);
        mainPanel.add(player1Label, BorderLayout.SOUTH);

        for (int i = TOTAL_PITS - 1; i >= PITS_PER_PLAYER; i--) {
            pitButtons[i] = createPitButton(i);
            boardPanel.add(pitButtons[i]);
        }
        for (int i = 0; i < PITS_PER_PLAYER; i++) {
            pitButtons[i] = createPitButton(i);
            boardPanel.add(pitButtons[i]);
        }

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        player1Score = new JLabel("Player 1 Captured: 0", SwingConstants.CENTER);
        player2Score = new JLabel("Player 2 Captured: 0", SwingConstants.CENTER);
        statusLabel = new JLabel("Player 1's turn", SwingConstants.CENTER);
        restartButton = new JButton("Restart Game");
        restartButton.addActionListener(e -> restartGame());
        
        player1Score.setFont(new Font("Arial", Font.BOLD, 14));
        player2Score.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        infoPanel.add(player1Score);
        infoPanel.add(player2Score);
        infoPanel.add(statusLabel);
        infoPanel.add(restartButton);
        
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(infoPanel, BorderLayout.EAST);
        frame.setVisible(true);
    }

    private JButton createPitButton(int index) {
        JButton button = new JButton(String.valueOf(board[index]));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(240, 230, 140));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMove(index);
            }
        });
        return button;
    }

    private void handleMove(int pit) {
        if ((currentPlayer == 0 && pit >= PITS_PER_PLAYER) || (currentPlayer == 1 && pit < PITS_PER_PLAYER) || board[pit] == 0) {
            return;
        }
        
        int seeds = board[pit];
        board[pit] = 0;
        int index = pit;
        
        while (seeds > 0) {
            index = (index + 1) % TOTAL_PITS;
            board[index]++;
            seeds--;
        }
        captureSeeds(index);
        updateBoard();
        checkGameOver();
    }

    private void captureSeeds(int lastIndex) {
        while (lastIndex >= PITS_PER_PLAYER * (1 - currentPlayer) && lastIndex < PITS_PER_PLAYER * (2 - currentPlayer)) {
            if (board[lastIndex] == 2 || board[lastIndex] == 3) {
                captured[currentPlayer] += board[lastIndex];
                board[lastIndex] = 0;
                lastIndex--;
            } else {
                break;
            }
        }
    }

    private void updateBoard() {
        for (int i = 0; i < TOTAL_PITS; i++) {
            pitButtons[i].setText(String.valueOf(board[i]));
        }
        player1Score.setText("Player 1 Captured: " + captured[0]);
        player2Score.setText("Player 2 Captured: " + captured[1]);
        currentPlayer = 1 - currentPlayer;
        statusLabel.setText("Player " + (currentPlayer + 1) + "'s turn");
    }

    private void checkGameOver() {
        int sumP1 = 0, sumP2 = 0;
        for (int i = 0; i < PITS_PER_PLAYER; i++) sumP1 += board[i];
        for (int i = PITS_PER_PLAYER; i < TOTAL_PITS; i++) sumP2 += board[i];
        
        if (sumP1 == 0 || sumP2 == 0) {
            JOptionPane.showMessageDialog(frame, "Game Over!\nPlayer 1: " + captured[0] + " seeds\nPlayer 2: " + captured[1] + " seeds");
            frame.dispose();
        }
    }

    private void restartGame() {
        initializeBoard();
        captured[0] = 0;
        captured[1] = 0;
        currentPlayer = 0;
        updateBoard();
    }

    public static void main(String[] args) {
        new AyoGameGUI();
    }
}
