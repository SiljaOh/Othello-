package de.lmu.bio.ifi;

import szte.mi.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;

public class OthelloGUI extends JFrame {
    private final Othello game;
    private final AI ai;
    private boolean playerTurn = true;
    private Move lastMoveNotAI = null;

    public OthelloGUI() {
        game = new Othello();
        ai = new AI();
        ai.init(1, Integer.MAX_VALUE, new Random());

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
                    if (game.gameStatus() != GameStatus.RUNNING) return; //ignore klicks if over

                    if (!playerTurn) return;

                    //if i cannot move
                    List<Move> mePossible = game.getPossibleMoves(true);
                    if (mePossible == null || mePossible.isEmpty()) {
                        game.pass();
                        playerTurn = false;


                        javax.swing.Timer t = new javax.swing.Timer(200, e2 -> doAIMove());
                        t.setRepeats(false);
                        t.start();
                        repaint();
                        return;

                    }

                    int x = e.getX() / cellsize;
                    int y = e.getY() / cellsize;

                    if (x < 0 || x > 7 || y < 0 || y > 7) return; //out of bounds

                    //proof ob klick gueltig ist
                    var possible = game.getPossibleMoves(true);
                    if (possible != null && possible.stream().anyMatch(m -> m.x == x && m.y == y)) {
                        game.makeMove(true, x, y);

                        lastMoveNotAI = new Move(x, y);

                        playerTurn = false;
                        repaint();

                        //1sec wait
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

        if (game.gameStatus() != GameStatus.RUNNING) return;

        Move aiMove = ai.nextMove(lastMoveNotAI, 1, 1);
        lastMoveNotAI = null;

        List<Move> possibleWhite = game.getPossibleMoves(false);
        if (possibleWhite == null || possibleWhite.isEmpty()) {
            game.pass();
            playerTurn = true;
            repaint();
            isGameOver();
            return;
        }

        if (aiMove != null) {
            boolean valid = possibleWhite.stream().anyMatch(m -> m.x == aiMove.x && m.y == aiMove.y);
            if (valid) {
                game.makeMove(false, aiMove.x, aiMove.y);
            } else {
                game.pass();
            }
        } else game.pass();

        playerTurn = true;
        repaint();


        isGameOver();

    }

    public void isGameOver() {
        GameStatus status = game.gameStatus();
        if (status != null && status != GameStatus.RUNNING) {
            String message;
            switch (status) {
                case PLAYER_1_WON:
                    message = "Black won";
                    break;
                case PLAYER_2_WON:
                    message = "White won";
                    break;
                default:
                    message = "Draw";
                    break;
            }
            JOptionPane.showMessageDialog(this, message);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(OthelloGUI::new);
    }
}
