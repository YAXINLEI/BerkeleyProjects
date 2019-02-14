package galaxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static galaxy.Place.pl;


/** The state of a Galaxies Puzzle.  Each cell, cell edge, and intersection of
 *  edges has coordinates (x, y). For cells, x and y are positive and odd.
 *  For intersections, x and y are even.  For horizontal edges, x is odd and
 *  y is even.  For vertical edges, x is even and y is odd.  On a board
 *  with w columns and h rows of cells, (0, 0) indicates the bottom left
 *  corner of the board, and (2w, 2h) indicates the upper right corner.
 *  If (x, y) are the coordinates of a cell, then (x-1, y) is its left edge,
 *  (x+1, y) its right edge, (x, y-1) its bottom edge, and (x, y+1) its
 *  top edge.  The four cells (x, y), (x+2, y), (x, y+2), and (x+2, y+2)
 *  meet at intersection (x+1, y+1).  Cells contain nonnegative integer
 *  values, or "marks". A cell containing 0 is said to be unmarked.
 *  @author yaxinlei
 */
class Model {

    /** The default number of squares on a side of the board. */
    static final int DEFAULT_SIZE = 7;
    /** Board records all the boundaries as boolean. */
    private boolean[][] board;
    /** These two records the x_lim and y_lim. */
    private int _xlim, _ylim;
    /** This records all the centers. */
    private HashSet<Place> centers = new HashSet<Place>();
    /** This mark board records all the marks. */
    private int[][] _markboard;

    /** Initializes an empty puzzle board of size DEFAULT_SIZE x DEFAULT_SIZE,
     *  with a boundary around the periphery. */
    Model() {
        init(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /** Initializes an empty puzzle board of size COLS x ROWS, with a boundary
     *  around the periphery. */
    Model(int cols, int rows) {
        init(cols, rows);
    }

    /** Initializes a copy of MODEL. */
    Model(Model model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Model model) {
        if (model == this) {
            return;
        }

        this.board = new boolean[model.xlim()][model.ylim()];
        this._markboard = new int[model.xlim()][model.ylim()];

        for (int i = 0; i < model._xlim; i++) {
            for (int j = 0; j < model._ylim; j++) {
                this.board[i][j] = model.board[i][j];
                this._markboard[i][j] = model._markboard[i][j];
            }
        }

        this._xlim = model._xlim;
        this._ylim = model._ylim;

        for (Place places : model.centers) {
            this.centers.add(places);
        }
    }


    /** Sets the puzzle board size to COLS x ROWS, and clears it. */
    void init(int cols, int rows) {
        this.board = new boolean[cols * 2 + 1][rows * 2 + 1];
        this._xlim = cols * 2 + 1;
        this._ylim = rows * 2 + 1;
        this._markboard = new int[cols * 2 + 1][rows * 2 + 1];
        this.centers = new HashSet<Place>();


        for (int i = 0; i < this._xlim; i++) {
            if (i == 0 || i == this._xlim - 1) {
                java.util.Arrays.fill(this.board[i], true);
            } else if (i % 2 == 1) {
                this.board[i][0] = true;
                this.board[i][this._ylim - 1] = true;
            }
        }
    }

    /** Clears the board (removes centers, boundaries that are not on the
     *  periphery, and marked cells) without resizing. */
    void clear() {
        init(cols(), rows());
    }

    /** Returns the number of columns of cells in the board. */
    int cols() {
        return xlim() / 2;
    }

    /** Returns the number of rows of cells in the board. */
    int rows() {
        return ylim() / 2;
    }

    /** Returns the number of vertical edges and cells in a row. */
    int xlim() {
        return this._xlim;
    }

    /** Returns the number of horizontal edges and cells in a column. */
    int ylim() {
        return this._ylim;
    }

    /** Returns true iff (X, Y) is a valid cell. */
    boolean isCell(int x, int y) {
        return 0 <= x && x < xlim() && 0 <= y && y < ylim()
            && x % 2 == 1 && y % 2 == 1;
    }

    /** Returns true iff P is a valid cell. */
    boolean isCell(Place p) {
        return isCell(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a valid edge. */
    boolean isEdge(int x, int y) {
        return 0 <= x && x < xlim() && 0 <= y && y < ylim() && x % 2 != y % 2;
    }

    /** Returns true iff P is a valid edge. */
    boolean isEdge(Place p) {
        return isEdge(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a vertical edge. */
    boolean isVert(int x, int y) {
        return isEdge(x, y) && x % 2 == 0;
    }

    /** Returns true iff P is a vertical edge. */
    boolean isVert(Place p) {
        return isVert(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a horizontal edge. */
    boolean isHoriz(int x, int y) {
        return isEdge(x, y) && y % 2 == 0;
    }

    /** Returns true iff P is a horizontal edge. */
    boolean isHoriz(Place p) {
        return isHoriz(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a valid intersection. */
    boolean isIntersection(int x, int y) {
        return x % 2 == 0 && y % 2 == 0
            && x >= 0 && y >= 0 && x < xlim() && y < ylim();
    }

    /** Returns true iff P is a valid intersection. */
    boolean isIntersection(Place p) {
        return isIntersection(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a center. */
    boolean isCenter(int x, int y) {
        Place currentcenter = Place.pl(x, y);
        return this.centers.contains(currentcenter);
    }

    /** Returns true iff P is a center. */
    boolean isCenter(Place p) {
        return this.centers.contains(p);
    }

    /** Returns true iff (X, Y) is a boundary. */
    boolean isBoundary(int x, int y) {
        return this.board[x][y];
    }

    /** Returns true iff P is a boundary. */
    boolean isBoundary(Place p) {
        return isBoundary(p.x, p.y);
    }

    /** Returns true iff the puzzle board is solved, given the centers and
     *  boundaries that are currently on the board. */
    boolean solved() {
        int total;
        total = 0;
        for (Place c : centers()) {
            HashSet<Place> r = findGalaxy(c);
            if (r == null) {
                return false;
            } else {
                total += r.size();
            }
        }
        return total == rows() * cols();
    }

    /** Finds cells reachable from CELL and adds them to REGION.  Specifically,
     *  it finds cells that are reachable using only vertical and horizontal
     *  moves starting from CELL that do not cross any boundaries and
     *  do not touch any cells that were initially in REGION. Requires
     *  that CELL is a valid cell. */
    private void accreteRegion(Place cell, HashSet<Place> region) {
        assert isCell(cell);
        if (region.contains(cell)) {
            return;
        }
        region.add(cell);
        for (int i = 0; i < 4; i += 1) {
            int dx = (i % 2) * (2 * (i / 2) - 1),
                dy = ((i + 1) % 2) * (2 * (i / 2) - 1);
            if (isCell(cell.move(2 * dx, 2 * dy))
                    && !isBoundary(cell.move(dx, dy))) {
                accreteRegion(cell.move(2 * dx, 2 * dy), region);
            }
        }
    }

    /** Returns true iff REGION is a correctly formed galaxy. A correctly formed
     *  galaxy has the following characteristics:
     *      - is symmetric about CENTER,
     *      - contains no interior boundaries, and
     *      - contains no other centers.
     * Assumes that REGION is connected. */
    private boolean isGalaxy(Place center, HashSet<Place> region) {
        for (Place cell : region) {

            if (cell != center && isCenter(cell)
                    || !region.contains(opposing(center, cell))) {
                return false;
            }
            for (int i = 0; i < 4; i += 1) {
                int dx = (i % 2) * (2 * (i / 2) - 1),
                    dy = ((i + 1) % 2) * (2 * (i / 2) - 1);
                Place boundary = cell.move(dx, dy),
                    nextCell = cell.move(2 * dx, 2 * dy);

                if (region.contains(nextCell) && isBoundary(boundary)
                        || isCenter(boundary) && (center != boundary)) {
                    return false;
                }
            }

            for (int i = 0; i < 4; i += 1) {
                int dx = 2 * (i / 2) - 1,
                    dy = 2 * (i % 2) - 1;
                Place intersection = cell.move(dx, dy);
                if (intersection != center && isCenter(intersection)) {
                    return false;
                }
            }
        }
        return true;
    }


    /** Returns the galaxy containing CENTER that has the following
     *  characteristics:
     *      - encloses CENTER completely,
     *      - is symmetric about CENTER,
     *      - is connected,
     *      - contains no stray boundary edges, and
     *      - contains no other centers aside from CENTER.
     *  Otherwise, returns null. Requires that CENTER is not on the
     *  periphery. */


    HashSet<Place> findGalaxy(Place center) {
        HashSet<Place> galaxy = new HashSet<>();

        if (center.x == 0
                || center.x == xlim() - 1
                || center.y == 0
                || center.y == ylim() - 1) {
            return null;
        }
        if (isIntersection(center)) {
            for (int i = 0; i < 4; i += 1) {
                int dx = 2 * (i / 2) - 1, dy = 2 * (i % 2) - 1;
                accreteRegion(center.move(dx, dy), galaxy);
            }
        } else if (isVert(center)) {
            accreteRegion(center.move(1, 0), galaxy);
            accreteRegion(center.move(-1, 0), galaxy);
        } else if (isHoriz(center)) {
            accreteRegion(center.move(0, 1), galaxy);
            accreteRegion(center.move(0, -1), galaxy);
        } else if (isCell(center)) {
            accreteRegion(center, galaxy);
        }

        if (isGalaxy(center, galaxy)) {
            return galaxy;
        } else {
            return null;
        }
    }

    /** Returns the largest, unmarked region around CENTER with the
     *  following characteristics:
     *      - contains all cells touching CENTER,
     *      - consists only of unmarked cells,
     *      - is symmetric about CENTER, and
     *      - is contiguous.
     *  The method ignores boundaries and other centers on the current board.
     *  If there is no such region, returns the empty set. */
    Set<Place> maxUnmarkedRegion(Place center) {
        HashSet<Place> region = new HashSet<>();
        region.addAll(unmarkedContaining(center));
        List<Place> regList = new ArrayList<Place>(region);
        while (!regList.isEmpty()) {
            region.addAll(unmarkedSymAdjacent(center, regList));
            regList = unmarkedSymAdjacent(center, regList);
            markAll(region, 1);
        }
        markAll(region, 0);
        return region;
    }

    /** Marks all properly formed galaxies with value V. Unmarks all cells that
     *  are not contained in any of these galaxies. Requires that V is greater
     *  than or equal to 0. */
    void markGalaxies(int v) {
        assert v >= 0;
        markAll(0);
        for (Place c : centers()) {
            HashSet<Place> region = findGalaxy(c);
            if (region != null) {
                markAll(region, v);
            }
        }
    }

    /** Toggles the presence of a boundary at the edge (X, Y). That is, negates
     *  the value of isBoundary(X, Y) (from true to false or vice-versa).
     *  Requires that (X, Y) is an edge. */
    void toggleBoundary(int x, int y) {
        if (isEdge(x, y) && x != this.xlim() - 1 && y != this.ylim() - 1) {
            this.board[x][y] = !this.board[x][y];
        }
    }

    /** Places a center at (X, Y). Requires that X and Y are within bounds of
     *  the board. */
    void placeCenter(int x, int y) {
        placeCenter(pl(x, y));
    }

    /** Places center at P. */
    void placeCenter(Place p) {

        if (p.x > 0 && p.x < this.xlim() - 1
                && p.y > 0 && p.y < this.ylim() - 1) {
            if (isCell(p)) {
                for (int i = 0; i < 4; i++) {
                    int dx = (i % 2) * (2 * (i / 2) - 1),
                            dy = ((i + 1) % 2) * (2 * (i / 2) - 1);
                    Place beside = p.move(dx, dy);
                    int dx2 = 2 * (i / 2) - 1;
                    int dy2 = 2 * (i % 2) - 1;
                    Place diagonal = p.move(dx2, dy2);

                    if (this.centers.contains(beside)
                            || this.centers.contains(diagonal)) {
                        return;
                    }
                }
            } else if (isIntersection(p)) {
                for (Place existingCenter : centers) {
                    if (Math.abs(existingCenter.x - p.x) <= 2
                            && Math.abs(existingCenter.y - p.y) <= 2) {
                        return;
                    }
                }

            } else if (isEdge(p)) {
                if (isVert(p)) {
                    for (Place existingCenter : centers) {
                        if (Math.abs(existingCenter.x - p.x) <= 2
                                && Math.abs(existingCenter.y - p.y) <= 1) {
                            return;
                        }
                    }
                } else if (isHoriz(p)) {
                    for (Place existingCenter : centers) {
                        if (Math.abs(existingCenter.x - p.x) <= 1
                                && Math.abs(existingCenter.y - p.y) <= 2) {
                            return;
                        }
                    }
                }
            }
            this.centers.add(p);
        }
    }

    /** Returns the current mark on cell (X, Y), or -1 if (X, Y) is not a valid
     *  cell address. */
    int mark(int x, int y) {
        if (!isCell(x, y)) {
            return -1;
        }
        return this._markboard[x][y];
    }

    /** Returns the current mark on cell P, or -1 if P is not a valid cell
     *  address. */
    int mark(Place p) {
        return mark(p.x, p.y);
    }

    /** Marks the cell at (X, Y) with value V. Requires that V must be greater
     *  than or equal to 0, and that (X, Y) is a valid cell address. */
    void mark(int x, int y, int v) {
        if (!isCell(x, y)) {
            throw new IllegalArgumentException("bad cell coordinates");
        }
        if (v < 0) {
            throw new IllegalArgumentException("bad mark value");
        } else {
            this._markboard[x][y] = v;
        }
    }

    /** Marks the cell at P with value V. Requires that V must be greater
     *  than or equal to 0, and that P is a valid cell address. */
    void mark(Place p, int v) {
        mark(p.x, p.y, v);
    }

    /** Sets the marks of all cells in CELLS to V. Requires that V must be
     *  greater than or equal to 0. */
    void markAll(Collection<Place> cells, int v) {
        assert v >= 0;
        for (Place eachcell : cells) {
            if (isCell(eachcell)) {
                this._markboard[eachcell.x][eachcell.y] = v;
            }
        }
    }

    /** Sets the marks of all cells to V. Requires that V must be greater than
     *  or equal to 0. */
    void markAll(int v) {
        assert v >= 0;
        for (int i = 0; i < this._markboard.length; i++) {
            for (int j = 0; j < this._markboard[i].length; j++) {
                this._markboard[i][j] = v;
            }
        }
    }

    /** Returns the position of the cell that is opposite P using P0 as the
     *  center, or null if that is not a valid cell address. */
    Place opposing(Place p0, Place p) {
        int xDistance = p0.x - p.x;
        int yDistance = p0.y - p.y;
        int newX = p0.x + xDistance;
        int newY = p0.y + yDistance;
        if (isCell(newX, newY)) {
            return pl(newX, newY);
        }
        return null;
    }

    /** Returns a list of all cells "containing" PLACE if all of the cells are
     *  unmarked. A cell, c, "contains" PLACE if
     *      - c is PLACE itself,
     *      - PLACE is a corner of c, or
     *      - PLACE is an edge of c.
     *  Otherwise, returns an empty list. */
    List<Place> unmarkedContaining(Place place) {
        if (isCell(place)) {
            if (this._markboard[place.x][place.y] == 0) {
                return asList(place);
            }
        } else if (isVert(place)) {
            if (this._markboard[place.x - 1][place.y] == 0
                    && this._markboard[place.x + 1][place.y] == 0) {
                return asList(place.move(-1, 0), place.move(1, 0));
            }
        } else if (isHoriz(place)) {
            if (this._markboard[place.x][place.y - 1] == 0
                    && this._markboard[place.x + 1][place.y + 1] == 0) {
                return asList(place.move(0, -1), place.move(0, 1));
            }
        } else {
            for (int i = 0; i < 4; i += 1) {
                if (mark(place.move(2 * (i / 2) - 1, 2 * (i % 2) - 1)) != 0) {
                    return Collections.emptyList();
                }
            }
            return asList(place.move(1, 1),
                    place.move(-1, -1),
                    place.move(-1, 1),
                    place.move(1, -1));
        }
        return Collections.emptyList();
    }

    /** Returns a list of all cells, c, such that:
     *      - c is unmarked,
     *      - The opposite cell from c relative to CENTER exists and
     *        is unmarked, and
     *      - c is vertically or horizontally adjacent to a cell in REGION.
     *  CENTER and all cells in REGION must be valid cell positions.
     *  Each cell appears at most once in the resulting list. */
    List<Place> unmarkedSymAdjacent(Place center, List<Place> region) {
        ArrayList<Place> result = new ArrayList<>();
        for (Place r : region) {
            assert isCell(r);
            for (int i = 0; i < 4; i += 1) {
                Place p = r.move(2 * (i % 2) * (2 * (i / 2) - 1),
                        2 * ((i + 1) % 2) * (2 * (i / 2) - 1));
                Place opp = opposing(center, p);
                if (p != null && opp != null
                        && isCell(p.x, p.y)
                        && isCell(opp.x, opp.y)
                        && !result.contains(p)) {
                    if (mark(p) == 0 && mark(opp) == 0) {
                        result.add(p);
                    }
                }
            }
        }
        return result;
    }

    /** Returns an unmodifiable view of the list of all centers. */
    List<Place> centers() {
        return Collections.unmodifiableList(new ArrayList<Place>(centers));
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        int w = xlim(), h = ylim();
        for (int y = h - 1; y >= 0; y -= 1) {
            for (int x = 0; x < w; x += 1) {
                boolean cent = isCenter(x, y);
                boolean bound = isBoundary(x, y);
                if (isIntersection(x, y)) {
                    out.format(cent ? "o" : " ");
                } else if (isCell(x, y)) {
                    if (cent) {
                        out.format(mark(x, y) > 0 ? "O" : "o");
                    } else {
                        out.format(mark(x, y) > 0 ? "*" : " ");
                    }
                } else if (y % 2 == 0) {
                    if (cent) {
                        out.format(bound ? "O" : "o");
                    } else {
                        out.format(bound ? "=" : "-");
                    }
                } else if (cent) {
                    out.format(bound ? "O" : "o");
                } else {
                    out.format(bound ? "I" : "|");
                }
            }
            out.format("%n");
        }
        return out.toString();
    }
}
