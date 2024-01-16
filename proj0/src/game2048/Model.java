package game2048;

import java.util.Formatter;


/** The state of a game of 2048.
 *  @author P. N. Hilfinger + Josh Hug
 */
public class Model {
    /** Current contents of the board. */
    private final Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;

    /** A 2D array to track whether certain tile has had merge happened or not.
     *  All values initialized to false by default.
     *  Gets reset after every Tilt action. */
    private boolean[][] flags;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        flags = new boolean[board.size()][board.size()];
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore) {
        board = new Board(rawValues);
        this.score = score;
        this.maxScore = maxScore;
        flags = new boolean[board.size()][board.size()];
    }


    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board. */
    public int size() {
        return board.size();
    }


    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        board.clear();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        return maxTileExists(board) || !atLeastOneMoveExists(board);
    }

    /** Checks if the game is over and sets the maxScore variable
     *  appropriately.
     */
    private void checkGameOver() {
        if (gameOver()) {
            maxScore = Math.max(score, maxScore);
        }
    }
    
    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for (int col = 0; col < b.size(); col++){
            for (int row = 0; row < b.size(); row++){
                if (b.tile(col, row) == null){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by this.MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for (int col = 0; col < b.size(); col++){
            for (int row = 0; row < b.size(); row++){
                if (b.tile(col, row) != null){
                    if (b.tile(col, row).value() == MAX_PIECE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        // case 1: when there is at least one empty space on the board.
        if (emptySpaceExists(b)){
            return true;
        }

        // case 2a: if there are two adjacent tiles in the same ROW with the same value
        for (int col = 0; col < b.size()-1; col++){
            for (int row = 0; row < b.size(); row++){
                if ((b.tile(col, row) != null) && (b.tile(col+1, row) != null)){
                    if (b.tile(col, row).value() == b.tile(col+1, row).value()) {
                        return true;
                    }
                }
            }
        }

        // case 2b: if there are two adjacent tiles in the same COLUMN with the same value
        for (int col = 0; col < b.size(); col++){
            for (int row = 0; row < b.size()-1; row++){
                if ((b.tile(col, row) != null) && (b.tile(col, row+1) != null)){
                    if (b.tile(col, row).value() == b.tile(col, row+1 ).value()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Tilt the board toward SIDE.

     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public void tilt(Side side) {
        // TODO: Modify this.board (and if applicable, this.score) to account
        // for the tilt to the Side SIDE.
        board.setViewingPerspective(side);
        for (int col = 0; col < board.size(); col++) {
            columnHandler(col);
        }
        board.setViewingPerspective(Side.NORTH);

        mergedReset();
        checkGameOver();
    }

    /** Each column is handled in the same way. All the tiles within the column
     * (except the top one) will be handled in a top-down approach. */
    public void columnHandler(int col) {
        for (int row = board.size()-2; row >= 0; row--) {
            if (tile(col, row) != null) {
                tileHandler(col, row);
            }
        }
    }

    /** For each tile, we should look at the very top row, and see if it can
     *  fit there. If not, go down until we reach the tile itself.

     *  Three scenarios:
     *  1. If the new place is null, then move the tile there.
     *  2. If the new place has the same value, and has never been merged before,
     *     then move the tile there.
     *  3. Otherwise, we look at 1 row lower. (since the new place has been
     *     occupied, and there is no way to move the tile there.)*/
    public void tileHandler(int col, int row) {
        Tile tile = board.tile(col, row);
        boolean flag = false;
        int new_row;
        for (new_row = board.size() - 1; new_row > row; new_row--) {
            Tile destination = board.tile(col, new_row);
            if (destination == null) {
                flag = board.move(col, new_row, tile);
                break;
            } else if (destination.value() == tile.value() && !merged(col, new_row)) {
                flag = board.move(col, new_row, tile);
                break;
            } /* else {
                continue;
            } */
        }
        flags[col][new_row] = flag;
        if (flag) {
            score += board.tile(col, new_row).value();
        }
    }

    /** This method tests whether a certain place has had merge action happened
     *  before. If yes, then tileHandler method cannot move a tile to that place.*/
    public boolean merged(int col, int row) {
        return flags[col][row];
    }

    /** To reset the flags array to all false after every Tilt action.*/
    public void mergedReset() {
        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.size(); j++) {
                flags[i][j] = false;
            }
        }
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

