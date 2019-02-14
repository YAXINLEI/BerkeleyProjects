package amazons;

import java.util.Iterator;
import java.util.Stack;
import java.util.Date;

import static amazons.Piece.*;

/**
 * A Player that automatically generates moves.
 *
 * @author yaxinlei
 */
class AI extends Player {

    /**
     * A position magnitude indicating a win (for white if positive, black
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;
    /**
     * The start time /find move function call.
     */
    private static long startingTime = System.currentTimeMillis();
    /**
     * The time.
     */
    private static long myTime = 0L;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        startingTime = System.currentTimeMillis();
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        myTime = (new Date()).getTime() - startingTime;
        if (depth == 0 || board.winner() != null
                || myTime > Integer.parseInt("200")) {
            return staticScore(board);
        }
        Iterator<Move> allMoves = board.legalMoves();

        if (sense == 1) {
            while (allMoves.hasNext()) {
                Move currentMove = allMoves.next();
                board.makeMove(currentMove);
                int score = staticScore(board);
                if (alpha < score && saveMove) {
                    _lastFoundMove = currentMove;
                    alpha = score;
                    board.undo();
                    findMove(board, depth - 1, true, -1, alpha, beta);
                } else if (alpha >= score) {
                    board.undo();
                    findMove(board, depth - 1, true, -1, alpha, beta);
                }
                if (alpha > beta) {
                    break;
                }
            }
            return alpha;
        } else {
            while (allMoves.hasNext()) {
                Move currentMove = allMoves.next();
                board.makeMove(currentMove);
                int score = staticScore(board);

                if (beta > score && saveMove) {
                    _lastFoundMove = currentMove;
                    beta = staticScore(board);
                    board.undo();
                    findMove(board, depth - 1, true, 1, alpha, beta);
                } else if (beta <= score) {
                    board.undo();
                    findMove(board, depth - 1, true, 1, alpha, beta);
                }
                if (alpha > beta) {
                    break;
                }
            }
            return beta;
        }
    }

    /** Three. */
    private final int five = 5;
    /** Ten. */
    private final int ten = 10;
    /** Twenty. */
    private final int twenty = 20;
    /** Thirty. */
    private final int thirty = 30;
    /** Forty. */
    private final int forty = 40;
    /** Fifty. */
    private final int fifty = 30;
    /** Seventy. */
    private final int seventy = 70;

    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        if (N < five) {
            return 3;
        } else if (N < ten) {
            return 4;
        } else if (N <= twenty) {
            return 5;
        } else if (N < thirty) {
            return 6;
        } else if (N < forty) {
            return 7;
        } else if (N < fifty) {
            return 8;
        } else if (N < seventy) {
            return 9;
        }
        return 10;
    }


    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }

        int score = 0;
        Piece side = board.turn(), opponent = side.opponent();
        int totalQueenFound = 0;
        Stack<Square> myQueens = new Stack<>(), opponentQueens = new Stack<>();

        for (int index = 0; index < 100; index++) {
            Square mySquare = Square.sq(index);
            if (board.get(mySquare) == side) {
                totalQueenFound += 1;
                myQueens.push(mySquare);
            } else if (board.get(mySquare) == opponent) {
                totalQueenFound += 1;
                opponentQueens.push(mySquare);
            }
            if (totalQueenFound == 8) {
                break;
            }
        }

        for (Square tempQueen = myQueens.pop();
             !myQueens.isEmpty(); tempQueen = myQueens.pop()) {
            Iterator<Square> reachable = board.reachableFrom(tempQueen, null);
            while (reachable.hasNext()) {
                reachable.next();
                score += 1;
            }
        }

        for (Square tempQueen = opponentQueens.pop();
             !opponentQueens.isEmpty(); tempQueen = opponentQueens.pop()) {
            Iterator<Square> reachable = board.reachableFrom(tempQueen, null);
            while (reachable.hasNext()) {
                reachable.next();
                score -= 1;
            }
        }

        return side == WHITE ? score : -score;
    }
}
