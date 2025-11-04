package ahnx33.tris;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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