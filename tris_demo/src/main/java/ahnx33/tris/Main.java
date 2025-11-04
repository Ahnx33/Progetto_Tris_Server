package ahnx33.tris;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 3000;
        try {
            TicTacToeServer server = new TicTacToeServer(port);
            server.start();
        } catch (IOException e) {

            System.err.println("Failed to start server: " + e.getMessage());

        }

    }
}