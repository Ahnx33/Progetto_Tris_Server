package ahnx33.tris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeServer {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private volatile boolean running = true;

    public TicTacToeServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        System.out.println("Server started. Waiting for players...");

        while (running) {
            try {
                // Wait for first player
                Socket socket1 = serverSocket.accept();
                System.out.println("Player 1 connected from " + socket1.getRemoteSocketAddress());
                PlayerHandler player1 = new PlayerHandler(socket1, 1);
                player1.sendMessage("WAIT");

                // Wait for second player
                Socket socket2 = serverSocket.accept();
                System.out.println("Player 2 connected from " + socket2.getRemoteSocketAddress());
                PlayerHandler player2 = new PlayerHandler(socket2, 2);

                // Create a new game session in a separate thread
                GameSession session = new GameSession(player1, player2);
                threadPool.execute(session);

            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    /**
     * Represents a single game session between two players.
     * Each session runs in its own thread.
     */
    class GameSession implements Runnable {
        private final PlayerHandler player1;
        private final PlayerHandler player2;
        private final int[] board = new int[9]; // 0 = empty, 1 = G1, 2 = G2

        GameSession(PlayerHandler p1, PlayerHandler p2) {
            this.player1 = p1;
            this.player2 = p2;
            Arrays.fill(board, 0);
        }

        @Override
        public void run() {
            System.out.println("Game session started between Player " +
                    player1.playerNumber + " and Player " + player2.playerNumber);

            // Notify both players that the game is ready
            player1.sendMessage("READY");

            player2.sendMessage("READY");

            // Run the game
            runGame();

            System.out.println("Game session ended");
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
        }

        private int checkWinner() {
            int[][] wins = {
                    { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 },
                    { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 },
                    { 0, 4, 8 }, { 2, 4, 6 }
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
                if (b == 0)
                    return false;
            }
            return true;
        }

        void sendBoardUpdate(PlayerHandler player, String result) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < board.length; i++) {
                sb.append(board[i]);
                sb.append(",");
            }
            sb.append(result);
            player.sendMessage(sb.toString());
        }
    }

    class PlayerHandler {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        final int playerNumber;

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
            // This method is called from GameSession context
            // Will be replaced by GameSession.sendBoardUpdate
            GameSession session = new GameSession(this, this);
            session.sendBoardUpdate(this, result);
        }

        void close() {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /*
     * public static void main(String[] args) {
     * try {
     * int port = args.length > 0 ? Integer.parseInt(args[0]) : 3000;
     * TicTacToeServer server = new TicTacToeServer(port);
     * 
     * // Add shutdown hook for graceful termination
     * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     * System.out.println("\nShutting down server...");
     * server.stop();
     * }));
     * 
     * server.start();
     * } catch (IOException e) {
     * System.err.println("Failed to start server: " + e.getMessage());
     * }
     * }
     */
}