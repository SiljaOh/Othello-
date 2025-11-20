package de.lmu.bio.ifi;

import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Random;

public class AI implements Player {

    private boolean isPlayerOne;
    private Othello game;
    private final int depth = 6;

    public AI() {}

    @Override
    public void init(int order, long t, Random rnd) {
        this.isPlayerOne = (order == 0);
        this.game = new Othello();
    }

    @Override
    public Move nextMove(Move prevMove, long tOpponent, long t) {


        if (prevMove != null) {
            boolean opp = !isPlayerOne;
            game.makeMove(opp, prevMove.x, prevMove.y);
        }

        List<Move> moves = game.getPossibleMoves(isPlayerOne);

        if (moves.isEmpty()) {
            game.pass();
            return null;
        }

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move m : moves) {
            Othello copy = game.copy();
            copy.makeMove(isPlayerOne, m.x, m.y);

            int value = -negamax(copy, depth - 1, !isPlayerOne,
                    Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);

            if (value > bestScore) {
                bestScore = value;
                bestMove = m;
            }
        }

        if (bestMove != null) game.makeMove(isPlayerOne, bestMove.x, bestMove.y);

        return bestMove;
    }

    private int negamax(Othello state, int depth, boolean player, int alpha, int beta) {
        if (depth == 0 || state.gameStatus() != GameStatus.RUNNING)
            return evaluate(state, player);

        List<Move> moves = state.getPossibleMoves(player);

        if (moves.isEmpty()) {
            Othello c = state.copy();
            c.pass();
            return -negamax(c, depth - 1, !player, -beta, -alpha);
        }

        int best = Integer.MIN_VALUE;

        for (Move m : moves) {
            Othello c = state.copy();
            c.makeMove(player, m.x, m.y);
            int v = -negamax(c, depth - 1, !player, -beta, -alpha);

            if (v > best) best = v;
            if (v > alpha) alpha = v;
            if (alpha >= beta) break;
        }
        return best;
    }

    private int evaluate(Othello g, boolean player) {
        int myDisc = player ? g.getPlayer1count() : g.getPlayer2count();
        int oppDisc = player ? g.getPlayer2count() : g.getPlayer1count();

        int score = myDisc - oppDisc;


        int myMob = g.getPossibleMoves(player).size();
        int oppMob = g.getPossibleMoves(!player).size();
        score += 5 * (myMob - oppMob);

        int[][] weights = {
                {100, -25, 10, 5, 5, 10, -25, 100},
                {-25, -50, 1, 1, 1, 1, -50, -25},
                {10, 1, 5, 2, 2, 5, 1, 10},
                {5, 1, 2, 1, 1, 2, 1, 5},
                {5, 1, 2, 1, 1, 2, 1, 5},
                {10, 1, 5, 2, 2, 5, 1, 10},
                {-25, -50, 1, 1, 1, 1, -50, -25},
                {100, -25, 10, 5, 5, 10, -25, 100}
        };

        int[][] board = g.getBoard();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board[y][x] == (player ? 1 : 2)) {
                    score += weights[y][x];
                } else if (board[y][x] == (player ? 2 : 1)) {
                    score -= weights[y][x];
                }
            }
        }

        return score;
    }

}
