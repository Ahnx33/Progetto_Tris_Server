package ahnx33.tris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PlayerHandler {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final int playerNumber;

    /**
     * Crea un PlayerHandler associato a uno socket e a un numero giocatore.
     * 
     * @param socket       socket connesso al client
     * @param playerNumber identificativo del giocatore (1 o 2)
     * @throws IOException se non è possibile aprire gli stream
     */
    public PlayerHandler(Socket socket, int playerNumber) throws IOException {
        this.socket = socket;
        this.playerNumber = playerNumber;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    void sendBoardUpdate() {
    }

    /**
     * Invia una linea al client (aggiunge automaticamente newline).
     */
    public void sendLine(String msg) {
        out.println(msg);
    }

    /**
     * Legge una singola linea dal client, null se stream chiuso.
     */
    public String readLine() throws IOException {
        return in.readLine();
    }

    /**
     * Chiude risorse associate.
     */
    public void close() {
        try {
            in.close();
        } catch (IOException ignored) {
        }
        out.close();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}