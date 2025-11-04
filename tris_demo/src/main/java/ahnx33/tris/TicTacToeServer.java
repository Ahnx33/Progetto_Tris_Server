package ahnx33.tris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TicTacToeServer {
    private final ServerSocket serverSocket;
    private PlayerHandler player1;
    private PlayerHandler player2;
    private final int[] board = new int[9]; // 0 = empty, 1 = G1, 2 = G2

    public TicTacToeServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Arrays.fill(board, 0);
    }

    public void start() {
        System.out.println("Server started. Waiting for players...");

        try {
            Socket socket1 = serverSocket.accept();
            System.out.println("Player 1 connected.");
            player1 = new PlayerHandler(socket1, 1);

            player1.sendMessage("WAIT");

            Socket socket2 = serverSocket.accept();
            System.out.println("Player 2 connected.");
            player2 = new PlayerHandler(socket2, 2);

            // Notify both
            player1.sendMessage("READY");
            player2.sendMessage("READY");

            // Start game loop
            runGame();

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private void runGame() {
        PlayerHandler current = player1;
        PlayerHandler opponent = player2;
        boolean gameOver = false;

        while (!gameOver) {
            try {
                String line = current.readMessage();
                if (line == null) {
                    handleDisconnect(opponent);
                    return;
                }

                int move;
                try {
                    move = Integer.parseInt(line.trim());
                } catch (NumberFormatException nfe) {
                    // invalid input, ask again
                    current.sendMessage("KO");
                    continue;
                }

                // validate move
                if (move < 0 || move >= 9 || board[move] != 0) {
                    current.sendMessage("KO");
                    continue;
                }

                // apply move
                board[move] = current.playerNumber;
                // evaluate game state
                int winner = checkWinner();
                boolean isDraw = checkDraw();
                if (winner == current.playerNumber) {
                    current.sendMessage("W");
                    opponent.sendBoardUpdate("L");
                    gameOver = true;
                } else if (isDraw) {
                    current.sendMessage("P");
                    opponent.sendBoardUpdate("P");
                    gameOver = true;
                } else {
                    // Valid non-terminal move
                    current.sendMessage("OK");
                    opponent.sendBoardUpdate("");
                    
                    // swap turn
                    PlayerHandler tmp = current;
                    current = opponent;
                    opponent = tmp;
                }

            } catch (IOException e) {
                // disconnected
                handleDisconnect(opponent);
                return;
            }
        }

        // close connections
        current.close();
        opponent.close();
    }

    private void handleDisconnect(PlayerHandler other) {
        other.sendMessage("DISCONNECTED");
        other.close();
        stop();
    }

    private int checkWinner() {
        int[][] wins = {
            {0,1,2}, {3,4,5}, {6,7,8},
            {0,3,6}, {1,4,7}, {2,5,8},
            {0,4,8}, {2,4,6}
        };
        for (int[] w : wins) {
            if (board[w[0]] != 0 &&
                board[w[0]] == board[w[1]] &&
                board[w[1]] == board[w[2]]) {
                return board[w[0]];
            }
        }
        return 0;
    }

    private boolean checkDraw() {
        for (int b : board) {
            if (b == 0) return false;
        }
        return true;
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    class PlayerHandler {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        int playerNumber;

        PlayerHandler(Socket s, int num) throws IOException {
            socket = s;
            playerNumber = num;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        void sendMessage(String msg) {
            out.println(msg);
        }

        String readMessage() throws IOException {
            return in.readLine();
        }

        void sendBoardUpdate(String result) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < board.length; i++) {
                sb.append(board[i]);
                sb.append(",");
            }
            sb.append(result);
            out.println(sb.toString());
        }

        void close() {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
