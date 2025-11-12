package de.lmu.bio.ifi;

import szte.mi.Move;

import java.util.ArrayList;
import java.util.List;

public class Othello implements Game{
    private final int[][] board;
    private int movecount;
    private int player1count;
    private int player2count;

    public Othello(){
        board = new int[8][8];
        board[3][3] = 2;
        board[4][4] = 2;
        board[3][4] = 1;
        board[4][3] = 1;
    }

    public int[][] getBoard() {
        return board;
    }

    @Override
    public boolean makeMove(boolean playerOne, int x, int y) {
        boolean isBlackTurn = (movecount % 2 == 0);
        if (playerOne != isBlackTurn) {
            return false;
        }

        List<Move> possible = getPossibleMoves(playerOne);
        boolean ispossible = possible.stream().anyMatch(m-> m.x==x && m.y==y);
        if(!ispossible) return false;

        int player= playerOne ? 1:2;
        int opponent= playerOne ? 2:1;

        board[y][x] = player;

        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},          {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };
        for (int[] d : directions) {
            flipDirection(x,y,d[0],d[1],player,opponent,true);
        }

        movecount++;
        updatecounts();
        return true;
    }

    public boolean flipDirection(int x, int y, int dirX, int dirY, int player, int opponent, boolean doflip) {
        int curX = x + dirX;
        int curY = y + dirY;
        boolean hasOpponentBetween = false;

        while (curX >= 0 && curX < 8 && curY >= 0 && curY < 8) {
            if (board[curY][curX] == opponent) {
                hasOpponentBetween = true;
                curX += dirX;
                curY += dirY;
            } else if (board[curY][curX] == player) {
                if (!hasOpponentBetween) return false;
                if (doflip) {
                    int flipX = x + dirX;
                    int flipY = y + dirY;
                    while (flipX != curX || flipY != curY) {
                        board[flipY][flipX] = player;
                        flipX += dirX;
                        flipY += dirY;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return false;

    }

    public void updatecounts(){
        player1count = player2count = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board[y][x] == 1) player1count++;
                else if (board[y][x] == 2) player2count++;
            }

        }
    }

    @Override
    public GameStatus gameStatus() {
        List<Move> p1 = getPossibleMoves(true);
        List<Move> p2 = getPossibleMoves(false);

        boolean noMovesP1 = (p1 == null || p1.isEmpty());
        boolean noMovesP2 = (p2 == null || p2.isEmpty());

        if (movecount == 64 || (noMovesP1 && noMovesP2)) {
            updatecounts();
            if (player1count > player2count) return GameStatus.PLAYER_1_WON;
            if (player2count > player1count) return GameStatus.PLAYER_2_WON;
            return GameStatus.DRAW;
        }
        return GameStatus.RUNNING;
    }

    @Override
    public List<Move> getPossibleMoves(boolean playerOne) {
        boolean isBlackTurn = (movecount % 2 == 0);
        if (playerOne != isBlackTurn) return null;

        int player = playerOne ? 1 : 2;
        int opponent = playerOne ? 2 : 1;
        List<Move> moves = new ArrayList<>();

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board[y][x] != 0) continue;
                boolean valid = false;

                for (int dx = -1; dx <= 1 && !valid; dx++) {
                    for (int dy = -1; dy <= 1 && !valid; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        if (flipDirection(x, y, dx, dy, player, opponent, false)) {
                            valid = true;

                        }
                    }
                }
                if (valid) moves.add(new Move(x, y));
            }
        }
        return moves.isEmpty() ? null : moves;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board[y][x] == 1) sb.append("X");
                else if (board[y][x] == 2) sb.append("O");
                else sb.append(".");
                if (x < 7) sb.append(" ");
            }
            if (y < 7) sb.append("\n");
        }
        return sb.toString();
    }
}
