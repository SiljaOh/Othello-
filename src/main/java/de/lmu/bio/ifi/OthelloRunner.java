package de.lmu.bio.ifi;

import szte.mi.Move;

import java.util.List;

public class OthelloRunner {
    public static void main(String[] args) {
        Othello game = new Othello();

        System.out.println(game.toString());

        int[][] moves = {
                {5, 4},
                {5, 3},
                {4, 2},
                {5, 5},
                {2, 4},
                {2, 3},
                {1, 2}
        };

        boolean playerOne=true;
        for (int[] move : moves) {
            System.out.println("\nSpieler " + (playerOne ? "1 (X)" : "2 (O)") + " ist am Zug.");
            List<Move> possible = game.getPossibleMoves(playerOne);
            System.out.print("Mögliche Züge: ");
            for (Move m : possible) {
                System.out.print("(" + m.x + "/" + m.y + ") ");
            }
            System.out.println();

            System.out.println("Führe Zug aus: (" + move[0] + "/" + move[1] + ")");
            game.makeMove(playerOne, move[0], move[1]);

            System.out.println(game);

            playerOne = !playerOne;

        }
    }
}
