package ahnx33.tris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        final int PORT = 3000;

        ServerSocket server = new ServerSocket(PORT);
        ArrayList matrice = new ArrayList<>();

        Socket player1 = server.accept();
        BufferedReader input_player1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
        PrintWriter output_player1 = new PrintWriter(player1.getOutputStream(), true);

        output_player1.println("wait");

        Socket player2 = server.accept();
        BufferedReader input_player2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
        PrintWriter output_player2 = new PrintWriter(player2.getOutputStream(), true);

        output_player2.println("ready");
        output_player1.println("ready");

        do {

        } while (true);

    }
}