package amazons;

import org.junit.Test;

import static org.junit.Assert.*;

import ucb.junit.textui;

/**
 * The suite of all JUnit tests for the enigma package.
 *
 * @author
 */
public class UnitTest {

    /**
     * Run the JUnit tests in this package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }


    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */


    /**
     * Tests basic correctness of put and get on the initialized board.
     */
    @Test
    public void testBasicPutGet() {
        Board b = new Board();
        b.put(amazons.Piece.BLACK, Square.sq(3, 5));
        assertEquals(b.get(3, 5), amazons.Piece.BLACK);
        b.put(amazons.Piece.WHITE, Square.sq(9, 9));
        assertEquals(b.get(9, 9), amazons.Piece.WHITE);
        b.put(amazons.Piece.EMPTY, Square.sq(3, 5));
        assertEquals(b.get(3, 5), amazons.Piece.EMPTY);
    }

    /**
     * Tests proper identification of legal/illegal queen moves.
     */
    @Test
    public void testIsQueenMove() {
        assertFalse(Square.sq(1, 5).isQueenMove(Square.sq(1, 5)));
        assertFalse(Square.sq(1, 5).isQueenMove(Square.sq(2, 7)));
        assertFalse(Square.sq(0, 0).isQueenMove(Square.sq(5, 1)));
        assertTrue(Square.sq(1, 1).isQueenMove(Square.sq(9, 9)));
        assertTrue(Square.sq(2, 7).isQueenMove(Square.sq(8, 7)));
        assertTrue(Square.sq(3, 0).isQueenMove(Square.sq(3, 4)));
        assertTrue(Square.sq(7, 9).isQueenMove(Square.sq(0, 2)));
    }

    /**
     * Tests toString for initial board state and a smiling board state. :)
     */
    @Test
    public void testToString() {
        Board b = new Board();
        assertEquals(INIT_BOARD_STATE, b.toString());
        makeSmile(b);
        assertEquals(SMILE, b.toString());
    }

    @Test
    public void testIsLegal() {
        Board b = new Board();
        Move a = Move.mv("a4-a5(a6)");
        assertEquals(true, b.isLegal(a));
    }

    private void makeSmile(Board b) {
        b.put(amazons.Piece.EMPTY, Square.sq(0, 3));
        b.put(amazons.Piece.EMPTY, Square.sq(0, 6));
        b.put(amazons.Piece.EMPTY, Square.sq(9, 3));
        b.put(amazons.Piece.EMPTY, Square.sq(9, 6));
        b.put(amazons.Piece.EMPTY, Square.sq(3, 0));
        b.put(amazons.Piece.EMPTY, Square.sq(3, 9));
        b.put(amazons.Piece.EMPTY, Square.sq(6, 0));
        b.put(amazons.Piece.EMPTY, Square.sq(6, 9));
        for (int col = 1; col < 4; col += 1) {
            for (int row = 6; row < 9; row += 1) {
                b.put(amazons.Piece.SPEAR, Square.sq(col, row));
            }
        }
        b.put(amazons.Piece.EMPTY, Square.sq(2, 7));
        for (int col = 6; col < 9; col += 1) {
            for (int row = 6; row < 9; row += 1) {
                b.put(amazons.Piece.SPEAR, Square.sq(col, row));
            }
        }
        b.put(amazons.Piece.EMPTY, Square.sq(7, 7));
        for (int lip = 3; lip < 7; lip += 1) {
            b.put(amazons.Piece.WHITE, Square.sq(lip, 2));
        }
        b.put(amazons.Piece.WHITE, Square.sq(2, 3));
        b.put(amazons.Piece.WHITE, Square.sq(7, 3));
    }

    static final String INIT_BOARD_STATE =
            "   - - - B - - B - - -\n"
                    + "   - - - - - - - - - -\n"
                    + "   - - - - - - - - - -\n"
                    + "   B - - - - - - - - B\n"
                    + "   - - - - - - - - - -\n"
                    + "   - - - - - - - - - -\n"
                    + "   W - - - - - - - - W\n"
                    + "   - - - - - - - - - -\n"
                    + "   - - - - - - - - - -\n"
                    + "   - - - W - - W - - -\n";

    static final String SMILE =
            "   - - - - - - - - - -\n"
                    + "   - S S S - - S S S -\n"
                    + "   - S - S - - S - S -\n"
                    + "   - S S S - - S S S -\n"
                    + "   - - - - - - - - - -\n"
                    + "   - - - - - - - - - -\n"
                    + "   - - W - - - - W - -\n"
                    + "   - - - W W W W - - -\n"
                    + "   - - - - - - - - - -\n"
                    + "   - - - - - - - - - -\n";

}


