package de.lmu.bio.ifi;

import szte.mi.Move;
import java.util.ArrayList;
import java.util.List;

public class Othello implements Game {

    private long playerBlackBoard;
    private long playerWhiteBoard;
    private int movecount;
    private int player1count;
    private int player2count;

    private static final long RowA = 0x0101010101010101L; //??
    private static final long RowH = 0x8080808080808080L;

    private boolean islastPass = false; // for 2x pass

    public Othello() {
        initStartPosition();
    }

    private void initStartPosition() {
        playerBlackBoard = 0L;
        playerWhiteBoard = 0L;

        playerBlackBoard |= bit(3, 4);
        playerBlackBoard |= bit(4, 3);

        playerWhiteBoard |= bit(3, 3);
        playerWhiteBoard |= bit(4, 4);

        movecount = 4;
        updatecounts();
    }

    private static int idx(int x, int y) { return y * 8 + x; } //calculate position in long form x and y
    private static long bit(int x, int y) { return 1L << idx(x, y); } //makes long with only 00 and 1x 1 on the right position

    private static long shiftN(long b)  { return b << 8; }
    private static long shiftS(long b)  { return b >>> 8; }
    private static long shiftE(long b)  { return (b & ~RowH) << 1; }
    private static long shiftW(long b)  { return (b & ~RowA) >>> 1; }
    private static long shiftNE(long b) { return (b & ~RowH) << 9; }
    private static long shiftNW(long b) { return (b & ~RowA) << 7; }
    private static long shiftSE(long b) { return (b & ~RowH) >>> 7; }
    private static long shiftSW(long b) { return (b & ~RowA) >>> 9; }

    private static long shiftDir(long b, int dir) {
        switch (dir) {
            case 0: return shiftN(b);
            case 1: return shiftS(b);
            case 2: return shiftE(b);
            case 3: return shiftW(b);
            case 4: return shiftNE(b);
            case 5: return shiftNW(b);
            case 6: return shiftSE(b);
            case 7: return shiftSW(b);
        }
        return 0L;
    }

    @Override
    public boolean makeMove(boolean playerOne, int x, int y) {
        long move = bit(x, y);

        long meBoard  = playerOne ? playerBlackBoard : playerWhiteBoard;
        long oppBoard = playerOne ? playerWhiteBoard : playerBlackBoard;

        if (((playerBlackBoard | playerWhiteBoard) & move) != 0L) return false; // if position is occupied

        long flips = 0L;

        for (int dir = 0; dir < 8; dir++) {
            long t = shiftDir(move, dir);
            long mask = 0L;

            while ((t != 0L) && ((t & oppBoard) != 0L)) {
                mask |= t;
                t = shiftDir(t, dir);
            }

            if ((t != 0L) && ((t & meBoard) != 0L)) {
                flips |= mask;
            }
        }

        if (flips == 0L) return false;

        meBoard ^= (move | flips); //bitwise xor
        oppBoard ^= flips;

        if (playerOne) {
            playerBlackBoard = meBoard;
            playerWhiteBoard = oppBoard;
        } else {
            playerWhiteBoard = meBoard;
            playerBlackBoard = oppBoard;
        }

        movecount++;
        islastPass = false;
        updatecounts();
        return true;
    }

    @Override
    public List<Move> getPossibleMoves(boolean playerOne) {
        long meBoard  = playerOne ? playerBlackBoard : playerWhiteBoard;
        long oppBoard = playerOne ? playerWhiteBoard : playerBlackBoard;
        long empty = ~(playerBlackBoard | playerWhiteBoard); // all empty fields

        long possibleMovesBoard = 0L;

        for (int dir = 0; dir < 8; dir++) {
            long potential = shiftDir(meBoard, dir) & oppBoard;
            long t = potential;
            while (t != 0L) {
                t = shiftDir(t, dir) & oppBoard;
                potential |= t;
            }
            long candidates = shiftDir(potential, dir) & empty;
            possibleMovesBoard |= candidates;
        }

        List<Move> list = new ArrayList<>();
        long m = possibleMovesBoard;
        while (m != 0L) {
            int pos = Long.numberOfTrailingZeros(m);
            m &= m - 1;
            list.add(new Move(pos % 8, pos / 8));
        }

        return list;
    }

    @Override
    public GameStatus gameStatus() {
        List<Move> p1moves = getPossibleMoves(true);
        List<Move> p2moves = getPossibleMoves(false);

        boolean noP1 = (p1moves.isEmpty());
        boolean noP2 = (p2moves.isEmpty());

        if ((noP1 && noP2) || movecount >= 64) {
            updatecounts();
            if (player1count > player2count) return GameStatus.PLAYER_1_WON;
            if (player2count > player1count) return GameStatus.PLAYER_2_WON;
            return GameStatus.DRAW;
        }

        return GameStatus.RUNNING;
    }

    public void pass() {
        movecount++;
        islastPass = true;
    }

    public void updatecounts() {
        player1count = Long.bitCount(playerBlackBoard);
        player2count = Long.bitCount(playerWhiteBoard);
    }

    public int[][] getBoard() {
        int[][] b = new int[8][8];
        for (int pos = 0; pos < 64; pos++) {
            long m = 1L << pos;
            int x = pos % 8;
            int y = pos / 8;
            if ((playerBlackBoard & m) != 0L) b[y][x] = 1;
            else if ((playerWhiteBoard & m) != 0L) b[y][x] = 2;
            else b[y][x] = 0;
        }
        return b;
    }


    public int getPlayer1count() { return player1count; }
    public int getPlayer2count() { return player2count; }

    public Othello copy() {
        Othello copiedBoard = new Othello();
        copiedBoard.playerBlackBoard = this.playerBlackBoard;
        copiedBoard.playerWhiteBoard = this.playerWhiteBoard;
        copiedBoard.movecount = this.movecount;
        copiedBoard.player1count = this.player1count;
        copiedBoard.player2count = this.player2count;
        copiedBoard.islastPass = this.islastPass;
        return copiedBoard;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                long mask = 1L << (y * 8 + x);

                if ((playerBlackBoard & mask) != 0L) sb.append("X");
                else if ((playerWhiteBoard & mask) != 0L) sb.append("O");
                else sb.append(".");

                if (x < 7) sb.append(" ");
            }
            if (y < 7) sb.append("\n");
        }
        return sb.toString();
    }

}
