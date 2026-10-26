import de.lmu.bio.ifi.AI;
import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.Othello;
import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Random;

/**
 * Evaluation of the Othello engine:
 *  1. perft  - counts all move sequences up to depth d and compares them with
 *              the published reference values (checks the bitboard move generator)
 *  2. arena  - plays the negamax AI against baseline opponents
 *
 * Build & run from the repository root:
 *   javac -d out $(find src/main/java bench -name "*.java")
 *   java -cp out Benchmark
 */
public class Benchmark {

    // reference perft values for Othello from the standard start position (passes count as moves)
    static final long[] PERFT_REF = {1, 4, 12, 56, 244, 1396, 8200, 55092, 390216, 3005288};

    static long perft(Othello g, boolean p1, int depth, boolean lastPassed) {
        if (depth == 0) return 1;
        List<Move> moves = g.getPossibleMoves(p1);
        if (moves.isEmpty()) {
            if (lastPassed) return 1;           // both players pass: game over, leaf
            Othello c = g.copy();
            c.pass();
            return perft(c, !p1, depth - 1, true);
        }
        long n = 0;
        for (Move m : moves) {
            Othello c = g.copy();
            c.makeMove(p1, m.x, m.y);
            n += perft(c, !p1, depth - 1, false);
        }
        return n;
    }

    static class RandomPlayer implements Player {
        Othello g; boolean me; Random r;
        public void init(int order, long t, Random rnd) { me = order == 0; g = new Othello(); r = rnd; }
        public Move nextMove(Move prev, long tO, long t) {
            if (prev != null) g.makeMove(!me, prev.x, prev.y);
            List<Move> ms = g.getPossibleMoves(me);
            if (ms.isEmpty()) { g.pass(); return null; }
            Move m = ms.get(r.nextInt(ms.size()));
            g.makeMove(me, m.x, m.y);
            return m;
        }
    }

    /** Greedy baseline: takes the move that flips the most discs right now. */
    static class GreedyPlayer extends RandomPlayer {
        public Move nextMove(Move prev, long tO, long t) {
            if (prev != null) g.makeMove(!me, prev.x, prev.y);
            List<Move> ms = g.getPossibleMoves(me);
            if (ms.isEmpty()) { g.pass(); return null; }
            Move best = null; int bestN = -1;
            for (Move m : ms) {
                Othello c = g.copy(); c.makeMove(me, m.x, m.y);
                int n = me ? c.getPlayer1count() : c.getPlayer2count();
                if (n > bestN || (n == bestN && r.nextBoolean())) { bestN = n; best = m; }
            }
            g.makeMove(me, best.x, best.y);
            return best;
        }
    }

    /** returns +1 if a (black when aBlack) wins, -1 loss, 0 draw; accumulates AI think time */
    static int play(Player a, Player b, boolean aBlack, long seed, long[] aiNanos, int[] aiMoves) {
        Random rnd = new Random(seed);
        Player black = aBlack ? a : b, white = aBlack ? b : a;
        black.init(0, 1000, rnd);
        white.init(1, 1000, rnd);
        Othello ref = new Othello();
        boolean p1 = true; Move last = null;
        while (ref.gameStatus() == GameStatus.RUNNING) {
            Player cur = p1 ? black : white;
            long t0 = System.nanoTime();
            Move m = cur.nextMove(last, 0, 0);
            if (cur == a) { aiNanos[0] += System.nanoTime() - t0; aiMoves[0]++; }
            if (m == null) ref.pass();
            else if (!ref.makeMove(p1, m.x, m.y)) throw new IllegalStateException("illegal move " + m.x + "," + m.y);
            last = m; p1 = !p1;
        }
        GameStatus s = ref.gameStatus();
        if (s == GameStatus.DRAW) return 0;
        boolean blackWon = s == GameStatus.PLAYER_1_WON;
        return (blackWon == aBlack) ? 1 : -1;
    }

    public static void main(String[] args) {
        int maxDepth = args.length > 0 ? Integer.parseInt(args[0]) : 9;
        int games = args.length > 1 ? Integer.parseInt(args[1]) : 100;

        System.out.println("PERFT");
        for (int d = 1; d <= maxDepth; d++) {
            long t0 = System.nanoTime();
            long n = perft(new Othello(), true, d, false);
            double s = (System.nanoTime() - t0) / 1e9;
            System.out.printf("perft %d %d %s %.3f%n", d, n, n == PERFT_REF[d] ? "OK" : "MISMATCH(ref " + PERFT_REF[d] + ")", s);
        }

        System.out.println("ARENA");
        String[] names = {"random", "greedy"};
        for (String name : names) {
            int w = 0, l = 0, dr = 0; long[] ns = {0}; int[] mv = {0};
            for (int i = 0; i < games; i++) {
                Player opp = name.equals("random") ? new RandomPlayer() : new GreedyPlayer();
                int r = play(new AI(), opp, i % 2 == 0, 1000 + i, ns, mv);
                if (r > 0) w++; else if (r < 0) l++; else dr++;
            }
            System.out.printf("arena %s games %d wins %d draws %d losses %d ms_per_move %.1f%n",
                    name, games, w, dr, l, ns[0] / 1e6 / mv[0]);
        }
    }
}
