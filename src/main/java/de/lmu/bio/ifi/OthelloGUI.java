package de.lmu.bio.ifi;

import szte.mi.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OthelloGUI extends JFrame {
    private final Othello game;
    private final AI ai;
    private boolean playerTurn = true;

    public OthelloGUI() {
        game = new Othello();
        ai = new AI(game, false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        BoardPanel board = new BoardPanel();
        add(board);
        setVisible(true);
    }

    private class BoardPanel extends JPanel {
        private final int cellsize = 70;

        public BoardPanel() {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (!playerTurn) return;
                    int x = e.getX() / cellsize;
                    int y = e.getY() / cellsize;


                    //prüfen ob klick gueltig ist
                    var possible = game.getPossibleMoves(true);
                    if (possible != null && possible.stream().anyMatch(m -> m.x == x && m.y == y)) {
                        game.makeMove(true, x, y);
                        playerTurn = false;
                        repaint();

                        //2sek wait
                        javax.swing.Timer delayTimer = new javax.swing.Timer(1000, e2 -> doAIMove());
                        delayTimer.setRepeats(false);
                        delayTimer.start();

                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
        }

        public void drawBoard(Graphics g) {

            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    g.setColor(Color.GRAY);
                    g.fillRect(x * cellsize, y * cellsize, cellsize, cellsize);
                    g.setColor(Color.BLACK);
                    g.drawRect(x * cellsize, y * cellsize, cellsize, cellsize);
                    g.setColor(Color.BLACK);

                    if (game.getBoard()[y][x] == 1) {
                        g.fillOval(x * cellsize + 10, y * cellsize + 10, 50, 50);
                    }
                    g.setColor(Color.WHITE);
                    if (game.getBoard()[y][x] == 2) {
                        g.fillOval(x * cellsize + 10, y * cellsize + 10, 50, 50);
                    }
                }

            }

        }
    }

    public void doAIMove() {

        Move aiMove = ai.nextMove(null, 1, 1);
        if (aiMove != null && game.getPossibleMoves(false) != null) {
            boolean valid = game.getPossibleMoves(false).stream().anyMatch(m -> m.x == aiMove.x && m.y == aiMove.y);
            if (valid) game.makeMove(false, aiMove.x, aiMove.y);
        }
        playerTurn = true;
        repaint();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(OthelloGUI::new);
    }
}
