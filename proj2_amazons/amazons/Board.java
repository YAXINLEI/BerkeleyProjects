package amazons;

import java.util.Iterator;
import java.util.Collections;
import java.util.Stack;

import static amazons.Utils.error;

import static amazons.Piece.*;
import static amazons.Move.mv;


/**
 * The state of an Amazons Game.
 *
 * @author yaxinlei
 */
class Board {

    /**
     * The number of squares on a side of the board.
     */
    static final int SIZE = 10;

    /**
     * An empty iterator for initialization.
     */
    private static final Iterator<Square> NO_SQUARES =
            Collections.emptyIterator();

    /**
     * Piece whose turn it is (BLACK or WHITE).
     */
    private Piece _turn;

    /**
     * Cached value of winner on this board, or EMPTY if it has not been
     * computed.
     */
    private Piece _winner;

    /**
     * A 2D array to store the board.
     */
    private Piece[][] _myBoard = new Piece[SIZE][SIZE];

    /**
     * This stores all the past moves.
     */
    private Stack<Move> _pastMoves = new Stack<>();

    /**
     * Initializes a game board with SIZE squares on a side in the
     * initial position.
     */
    Board() {
        init();
    }

    /**
     * Initializes a copy of MODEL.
     */
    Board(Board model) {
        copy(model);
    }

    /**
     * Copies MODEL into me.
     */
    void copy(Board model) {

        this._turn = model.turn();
        this._winner = model.winner();
        this._myBoard = new Piece[SIZE][SIZE];
        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE; row++) {
                this._myBoard[col][row] = model.get(col, row);
            }
        }
    }

    /**
     * Clears the board to the initial position.
     */
    void init() {

        for (int col = 0; col < SIZE; col += 1) {
            for (int row = 0; row < SIZE; row += 1) {
                _myBoard[col][row] = EMPTY;

            }
        }

        _turn = WHITE;
        _winner = null;

        put(WHITE, 3, 0);
        put(WHITE, 6, 0);
        put(WHITE, 0, 3);
        put(WHITE, 9, 3);

        put(BLACK, 3, 9);
        put(BLACK, 6, 9);
        put(BLACK, 0, 6);
        put(BLACK, 9, 6);
    }

    /**
     * Return the Piece whose move it is (WHITE or BLACK).
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return the number of moves (that have not been undone) for this
     * board.
     */
    int numMoves() {
        return _pastMoves.size();
    }

    /**
     * Return the winner in the current position, or null if the game is
     * not yet finished.
     */
    Piece winner() {
        Iterator<Move> futureMoves = legalMoves();
        if (!futureMoves.hasNext()) {
            return _turn.opponent();
        } else {
            return null;
        }
    }

    /**
     * Return the contents the square at S.
     */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /**
     * Return the contents of the square at (COL, ROW), where
     * 0 <= COL, ROW <= 9.
     */
    final Piece get(int col, int row) {
        return _myBoard[col][row];
    }

    /**
     * Return the contents of the square at COL ROW.
     */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /**
     * Set square S to P.
     */
    final void put(Piece p, Square s) {
        put(p, s.col(), s.row());
    }

    /**
     * Set square (COL, ROW) to P.
     */
    final void put(Piece p, int col, int row) {
        _myBoard[col][row] = p;
    }

    /**
     * Set square COL ROW to P.
     */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - '1');
    }

    /**
     * Return true iff FROM - TO is an unblocked queen move on the current
     * board, ignoring the contents of ASEMPTY, if it is encountered.
     * For this to be true, FROM-TO must be a queen move and the
     * squares along it, other than FROM and ASEMPTY, must be
     * empty. ASEMPTY may be null, in which case it has no effect.
     */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {

        final int ninetyNine = 99;

        if (to == null || from == null) {
            return false;
        }
        if (to.index() > ninetyNine || to.index() < 0) {
            return false;
        }
        if (!from.isQueenMove(to)) {
            return false;
        }
        if (get(to) != EMPTY
                && Square.sq(to.col(), to.row()) != asEmpty) {
            return false;
        }

        int direction = from.direction(to);

        if (asEmpty == null) {
            for (Square temp = from.queenMove(direction, 1);
                 temp.index() != to.index();
                 temp = temp.queenMove(direction, 1)) {
                if (get(temp) != EMPTY) {
                    return false;
                }
            }
        } else {
            for (Square temp = from.queenMove(direction, 1);
                 temp.index() != to.index();
                 temp = temp.queenMove(direction, 1)) {
                if (get(temp) != EMPTY && temp != asEmpty) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Return true iff FROM is a valid starting square for a move.
     */
    boolean isLegal(Square from) {
        return get(from) == _turn;
    }

    /**
     * Return true iff FROM-TO is a valid first part of move, ignoring
     * spear throwing.
     */
    boolean isLegal(Square from, Square to) {
        return isLegal(from) && (get(to) == EMPTY)
                && isUnblockedMove(from, to, null);
    }

    /**
     * Return true iff FROM-TO(SPEAR) is a legal move in the current
     * position.
     */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(from, to)
                && isUnblockedMove(to, spear, from);
    }

    /**
     * Return true iff MOVE is a legal move in the current
     * position.
     */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to(), move.spear());
    }


    /**
     * Move FROM-TO(SPEAR), assuming this is a legal move.
     */
    void makeMove(Square from, Square to, Square spear) {
        makeMove(mv(from, to, spear));
    }

    /**
     * Move according to MOVE, assuming it is a legal move.
     */
    void makeMove(Move move) {
        if (move == null) {
            throw error("This move is not valid :P");
        }
        if (!isLegal(move)) {
            throw error("This move is not valid :P");
        }

        put(EMPTY, move.from());
        put(_turn, move.to());
        put(SPEAR, move.spear());
        _pastMoves.push(move);

        _turn = (_turn == WHITE) ?  BLACK : WHITE;
    }


    /**
     * Undo one move. Has no effect on the initial board.
     */
    void undo() {
        if (!_pastMoves.isEmpty()) {
            Move undoMove = _pastMoves.pop();

            put(EMPTY, undoMove.spear());
            put(get(undoMove.to()), undoMove.from());
            put(EMPTY, undoMove.to());

            _turn = (_turn == WHITE) ?  BLACK : WHITE;
        } else {
            throw error(" No moves to undo");
        }
    }


    /**
     * Return an Iterator over the Squares that are reachable by an
     * unblocked queen move from FROM. Does not pay attention to what
     * piece (if any) is on FROM, nor to whether the game is finished.
     * Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     * feature is useful when looking for Moves, because after moving a
     * piece, one wants to treat the Square it came from as empty for
     * purposes of spear throwing.)
     */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /**
     * Return an Iterator over all legal moves on the current board.
     */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /**
     * Return an Iterator over all legal moves on the current board for
     * SIDE (regardless of whose turn it is).
     */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }


    /**
     * An iterator used by reachableFrom.
     */
    private class ReachableFromIterator implements Iterator<Square> {

        /**
         * Iterator of all squares reachable by queen move from FROM,
         * treating ASEMPTY as empty.
         */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = -1;
            _steps = 0;
            _asEmpty = asEmpty;
            _fromCol = _from.col();
            _fromRow = _from.row();
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _dir < 8;
        }

        @Override
        public Square next() {
            Square temp = Square.sq(_fromCol + _colNum * _steps,
                    _fromRow + _rowNum * _steps);
            toNext();
            return temp;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            for (_rowNum = (1 + _dir) / 3 - 1,
                 _colNum = _dir - (_dir + 1) / 3 * 3,
                 _steps = _steps + 1;
                 Square.sq(_fromCol + _colNum * _steps,
                    _fromRow + _rowNum * _steps) == null;
                 _dir += 1, _steps = 1,
                         _rowNum = (_dir + 1) / 3 - 1,
                         _colNum = -(_dir + 1) / 3 * 3 + _dir) {
                if (!hasNext()) {
                    return;
                }
            }

            Square sqr = Square.sq(
                    _fromCol + _steps * _colNum, _fromRow + _rowNum * _steps);

            if (isUnblockedMove(_from, sqr, _asEmpty)) {
                return;
            }

            _steps = 1;

            while (!isUnblockedMove(_from, sqr, _asEmpty)) {
                for (_dir = _dir + 1,
                     _rowNum = (_dir + 1) / 3 - 1,
                     _colNum = -(_dir + 1) / 3 * 3 + _dir;
                     Square.sq(_fromCol + _colNum * _steps,
                        _fromRow + _rowNum * _steps) == null;
                     _dir = _dir + 1,
                             _rowNum = (_dir + 1) / 3 - 1,
                             _colNum = -(_dir + 1) / 3 * 3 + _dir) {
                    if (!hasNext()) {
                        return;
                    }
                }
                sqr = Square.sq(_fromCol + _colNum * _steps,
                        _fromRow + _rowNum * _steps);
                if (!hasNext()) {
                    break;
                }
            }
        }



        /**
         * Starting square.
         */
        private Square _from;
        /**
         * Current direction.
         */
        private int _dir;
        /**
         * Current distance.
         */
        private int _steps;
        /**
         * Square treated as empty.
         */
        private Square _asEmpty;
        /**
         * This is used to save row and col increments.
         */
        private int _rowNum, _colNum;

        /** This stores the information for from. */
        private int _fromCol, _fromRow;
    }

    /**
     * An iterator used by legalMoves.
     */
    private class LegalMoveIterator implements Iterator<Move> {

        /**
         * All legal moves for SIDE (WHITE or BLACK).
         */
        LegalMoveIterator(Piece side) {
            _startingSquares = Square.iterator();
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _fromPiece = side;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return get(_start) != EMPTY && exist;
        }

        @Override
        public Move next() {
            Move myMove = Move.mv(_start, _myNextSquare, _spearThrows.next());
            toNext();
            return myMove;
        }

        /**
         * Advance so that the next valid Move is
         * _start-_nextSquare(sp), where sp is the next value of
         * _spearThrows.
         */
        private void toNext() {
            if (!_spearThrows.hasNext()) {
                if (_pieceMoves.hasNext()) {
                    _myNextSquare = _pieceMoves.next();
                    _spearThrows = new
                            ReachableFromIterator(_myNextSquare, _start);
                }

                if (!_pieceMoves.hasNext()) {
                    if (_startingSquares.hasNext()) {
                        _start = _startingSquares.next();
                    }

                    while (_startingSquares.hasNext()) {
                        for (_start = _startingSquares.next();
                             _startingSquares.hasNext()
                                && get(_start) == _fromPiece.opponent();
                             _start = _startingSquares.next()) {
                            continue;
                        }

                        if (get(_start) == _fromPiece) {
                            _pieceMoves = new
                                    ReachableFromIterator(_start, null);
                            if (_pieceMoves.hasNext()) {
                                _myNextSquare = _pieceMoves.next();
                                _spearThrows = new
                                        ReachableFromIterator(_myNextSquare,
                                        _start);
                                return;
                            }
                        }
                    }
                    exist = false;
                }
            }
        }

        /**
         * Color of side whose moves we are iterating.
         */
        private Piece _fromPiece;
        /**
         * Current starting square.
         */
        private Square _start;
        /**
         * Remaining starting squares to consider.
         */
        private Iterator<Square> _startingSquares;
        /**
         * Current piece's new position.
         */
        private Square _myNextSquare;
        /**
         * Remaining moves from _start to consider.
         */
        private Iterator<Square> _pieceMoves;
        /**
         * Remaining spear throws from _piece to consider.
         */
        private Iterator<Square> _spearThrows;
        /**
         * Does it contain next.
         */
        private boolean exist = true;
    }


    @Override
    public String toString() {
        String resultString = "";
        for (int r = 9; r >= 0; r -= 1) {
            for (int c = 0; c < SIZE; c += 1) {
                if (c == 0) {
                    resultString += "   ";
                }
                resultString += get(c, r);
                resultString = (c == SIZE - 1)
                        ? (resultString + "\n") : (resultString + " ");
            }
        }
        return resultString;
    }
}









