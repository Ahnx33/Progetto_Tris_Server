package ahnx33.tris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

    void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException ignored) {
        }
    }
}