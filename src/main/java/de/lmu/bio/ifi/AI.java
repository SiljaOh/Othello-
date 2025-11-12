package de.lmu.bio.ifi;

import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Random;

public class AI implements Player {
    private boolean isPlayerOne;
    private Random rnd;
    private Othello game;

    public AI(Othello game, boolean isPlayerOne) {
        this.game = game;
        this.rnd = new Random();
        this.isPlayerOne = isPlayerOne;
    }


    @Override
    public void init(int order, long t, Random rnd) {
    //initialisiert Spieler
        //order ob 1 oder 2 t=verbleibende Zeit , rnd= Zufallsquelle
        this.isPlayerOne = order==1;
        this.rnd = rnd;
    }

    @Override
    public Move nextMove(Move prevMove, long tOpponent, long t) {
        //brechnet naechsten Zug basierend auf letztem zug und aktuellem Spielstand
        List<Move> moves=game.getPossibleMoves(isPlayerOne);
        if (moves.isEmpty()) {return null;}
        return moves.get(rnd.nextInt(moves.size()));
    }
}
