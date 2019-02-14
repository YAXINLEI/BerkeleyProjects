package amazons;

import ucb.gui2.Pad;

import java.io.IOException;

import java.util.concurrent.ArrayBlockingQueue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import static amazons.Piece.*;
import static amazons.Square.sq;

/** A widget that displays an Amazons game.
 *  @author yaxinlei
 */
class BoardWidget extends Pad {

    /* Parameters controlling sizes, speeds, colors, and fonts. */

    /** Colors of empty squares and grid lines. */
    static final Color
        SPEAR_COLOR = new Color(30, 30, 30),
        LIGHT_SQUARE_COLOR = new Color(238, 207, 161),
        DARK_SQUARE_COLOR = new Color(205, 133, 63);

    /** Locations of images of white and black queens. */
    private static final String
        WHITE_QUEEN_IMAGE = "wq4.png",
        BLACK_QUEEN_IMAGE = "bq4.png";

    /** Size parameters. */
    private static final int
        SQUARE_SIDE = 30,
        BOARD_SIDE = SQUARE_SIDE * 10;

    /** A graphical representation of an Amazons board that sends commands
     *  derived from mouse clicks to COMMANDS.  */
    BoardWidget(ArrayBlockingQueue<String> commands) {
        _commands = commands;
        setMouseHandler("click", this::mouseClicked);
        setPreferredSize(BOARD_SIDE, BOARD_SIDE);
        try {
            _whiteQueen = ImageIO.read(Utils.getResource(WHITE_QUEEN_IMAGE));
            _blackQueen = ImageIO.read(Utils.getResource(BLACK_QUEEN_IMAGE));
        } catch (IOException excp) {
            System.err.println("Could not read queen images.");
            System.exit(1);
        }
        _acceptingMoves = false;
        _myNextMove = "";
        _count = 0;
    }

    /** Draw the bare board G.  */
    private void drawGrid(Graphics2D g) {
        g.setColor(LIGHT_SQUARE_COLOR);
        g.fillRect(0, 0, BOARD_SIDE, BOARD_SIDE);
        Boolean color = false;

        for (int i = 0; i < Board.SIZE; i += 1) {
            color = !color;
            for (int j = 0; j < Board.SIZE; j += 1) {
                Square mySquare = sq(i,  Board.SIZE - 1 - j);
                if (!color) {
                    g.setColor(DARK_SQUARE_COLOR);
                    color = !color;
                } else {
                    g.setColor(LIGHT_SQUARE_COLOR);
                    color = !color;
                }
                if (mySquare == _queenPosition) {
                    g.setColor(Color.LIGHT_GRAY);
                } else if (mySquare == _nextPosition) {
                    g.setColor(Color.LIGHT_GRAY);
                } else if (mySquare == _spearPosition) {
                    g.setColor(Color.DARK_GRAY);
                }
                g.fillRect(cx(i), cx(j), SQUARE_SIDE, SQUARE_SIDE);
            }
        }
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawGrid(g);
        for (int index = 0; index < 100; index += 1) {
            Square mySquare = sq(index);
            Piece myPiece = _board.get(mySquare);
            if (myPiece == WHITE) {
                drawQueen(g, mySquare, WHITE);
            } else if (myPiece == BLACK) {
                drawQueen(g, mySquare, BLACK);
            } else if (myPiece == SPEAR) {
                g.setColor(SPEAR_COLOR);
                g.fillRect(cx(mySquare), cy(mySquare),
                        SQUARE_SIDE, SQUARE_SIDE);
            }
        }
    }

    /** Draw a queen for side PIECE at square S on G.  */
    private void drawQueen(Graphics2D g, Square s, Piece piece) {
        g.drawImage(piece == WHITE ? _whiteQueen : _blackQueen,
                    cx(s.col()) + 2, cy(s.row()) + 4, null);
    }

    /** Handle mouse click event E. */
    private synchronized void mouseClicked(String unused, MouseEvent e) {
        int xpos = e.getX(), ypos = e.getY();
        int x = xpos / SQUARE_SIDE,
            y = (BOARD_SIDE - ypos) / SQUARE_SIDE;
        if (_acceptingMoves
            && x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
            Square mySquare = sq(x, y);
            _count++;

            String temp = mySquare.toString() + " ";
            if (_count % 3 == 1) {
                _queenPosition = mySquare;
                _myNextMove +=  temp;
            } else if (_count % 3 == 2) {
                _nextPosition = mySquare;
                _myNextMove += temp;
            } else if (_count % 3 == 0) {
                _spearPosition = mySquare;
                _commands.offer(_myNextMove + mySquare.toString());
                _myNextMove = "";
                _queenPosition = null;
                _nextPosition = null;
                _spearPosition = null;
            }
            repaint();
        }
    }

    /** Revise the displayed board according to BOARD. */
    synchronized void update(Board board) {
        _board.copy(board);
        repaint();
    }

    /** Turn on move collection iff COLLECTING, and clear any current
     *  partial selection.   When move collection is off, ignore clicks on
     *  the board. */
    void setMoveCollection(boolean collecting) {
        _acceptingMoves = collecting;
        repaint();
    }

    /** Return x-pixel coordinate of the left corners of column X
     *  relative to the upper-left corner of the board. */
    private int cx(int x) {
        return x * SQUARE_SIDE;
    }

    /** Return y-pixel coordinate of the upper corners of row Y
     *  relative to the upper-left corner of the board. */
    private int cy(int y) {
        return (Board.SIZE - y - 1) * SQUARE_SIDE;
    }

    /** Return x-pixel coordinate of the left corner of S
     *  relative to the upper-left corner of the board. */
    private int cx(Square s) {
        return cx(s.col());
    }

    /** Return y-pixel coordinate of the upper corner of S
     *  relative to the upper-left corner of the board. */
    private int cy(Square s) {
        return cy(s.row());
    }

    /** Queue on which to post move commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;
    /** Board being displayed. */
    private final Board _board = new Board();

    /** Image of white queen. */
    private BufferedImage _whiteQueen;
    /** Image of black queen. */
    private BufferedImage _blackQueen;

    /** True iff accepting moves from user. */
    private boolean _acceptingMoves;

    /** The string of next move. */
    private String _myNextMove;

    /** The number of clicks. */
    private int _count;

    /** This saves the queen position. */
    private Square _queenPosition;

    /** This saves the plausible next position. */
    private Square _nextPosition;

    /** This saves the plausible next position. */
    private Square _spearPosition;
}
