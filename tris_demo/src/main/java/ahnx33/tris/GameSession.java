package ahnx33.tris;

import java.io.IOException;
import java.util.Arrays;

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
                    sendBoardUpdate(opponent, "L");
                    gameOver = true;
                } else if (isDraw) {
                    current.sendMessage("P");
                    sendBoardUpdate(opponent, "P");
                    gameOver = true;
                } else {
                    // Valid non-terminal move
                    current.sendMessage("OK");
                    sendBoardUpdate(opponent, "");

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