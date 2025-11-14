package de.lmu.bio.ifi;

import szte.mi.Move;
import java.util.ArrayList;
import java.util.List;

public class Othello implements Game {

    private long p1;   // Bitboard Player 1 (Schwarz)
    private long p2;   // Bitboard Player 2 (Weiß)
    private int movecount;
    private int player1count;
    private int player2count;

    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_H = 0x8080808080808080L;

    private boolean lastPass = false; // für zwei aufeinanderfolgende Pässe

    public Othello() {
        initStartPosition();
    }

    private void initStartPosition() {
        p1 = 0L;
        p2 = 0L;

        p1 |= bit(3, 4);
        p1 |= bit(4, 3);

        p2 |= bit(3, 3);
        p2 |= bit(4, 4);

        movecount = 4;
        updatecounts();
    }

    private static int idx(int x, int y) { return y * 8 + x; }
    private static long bit(int x, int y) { return 1L << idx(x, y); }

    private static long shiftN(long b)  { return b << 8; }
    private static long shiftS(long b)  { return b >>> 8; }
    private static long shiftE(long b)  { return (b & ~FILE_H) << 1; }
    private static long shiftW(long b)  { return (b & ~FILE_A) >>> 1; }
    private static long shiftNE(long b) { return (b & ~FILE_H) << 9; }
    private static long shiftNW(long b) { return (b & ~FILE_A) << 7; }
    private static long shiftSE(long b) { return (b & ~FILE_H) >>> 7; }
    private static long shiftSW(long b) { return (b & ~FILE_A) >>> 9; }

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

        long me  = playerOne ? p1 : p2;
        long opp = playerOne ? p2 : p1;

        if (((p1 | p2) & move) != 0L) return false;

        long flips = 0L;

        for (int dir = 0; dir < 8; dir++) {
            long t = shiftDir(move, dir);
            long mask = 0L;

            while ((t != 0L) && ((t & opp) != 0L)) {
                mask |= t;
                t = shiftDir(t, dir);
            }

            if ((t != 0L) && ((t & me) != 0L)) {
                flips |= mask;
            }
        }

        if (flips == 0L) return false;

        me ^= (move | flips);
        opp ^= flips;

        if (playerOne) {
            p1 = me;
            p2 = opp;
        } else {
            p2 = me;
            p1 = opp;
        }

        movecount++;
        lastPass = false;
        updatecounts();
        return true;
    }

    @Override
    public List<Move> getPossibleMoves(boolean playerOne) {
        long me  = playerOne ? p1 : p2;
        long opp = playerOne ? p2 : p1;
        long empty = ~(p1 | p2);

        long movesBB = 0L;

        for (int dir = 0; dir < 8; dir++) {
            long potential = shiftDir(me, dir) & opp;
            long t = potential;
            while (t != 0L) {
                t = shiftDir(t, dir) & opp;
                potential |= t;
            }
            long candidates = shiftDir(potential, dir) & empty;
            movesBB |= candidates;
        }

        List<Move> list = new ArrayList<>();
        long m = movesBB;
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
        lastPass = true;
    }

    public void updatecounts() {
        player1count = Long.bitCount(p1);
        player2count = Long.bitCount(p2);
    }

    public int[][] getBoard() {
        int[][] b = new int[8][8];
        for (int pos = 0; pos < 64; pos++) {
            long m = 1L << pos;
            int x = pos % 8;
            int y = pos / 8;
            if ((p1 & m) != 0L) b[y][x] = 1;
            else if ((p2 & m) != 0L) b[y][x] = 2;
            else b[y][x] = 0;
        }
        return b;
    }

    public int getMovecount() { return movecount; }
    public int getPlayer1count() { return player1count; }
    public int getPlayer2count() { return player2count; }

    public Othello copy() {
        Othello c = new Othello();
        c.p1 = this.p1;
        c.p2 = this.p2;
        c.movecount = this.movecount;
        c.player1count = this.player1count;
        c.player2count = this.player2count;
        c.lastPass = this.lastPass;
        return c;
    }
}
