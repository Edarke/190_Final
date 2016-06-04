package robotics;

/**
 * Created by Evan on 6/3/16.
 */
public enum Direction {
    UP(-1, 0, "\u2191"), DOWN(1, 0, "\u2193"), LEFT(0, -1, "\u2190"), RIGHT(0,1,"\u2192");


    public final int rowDelta, colDelta;
    public final String symbol;


    Direction(int dr, int dc, String symb){
        rowDelta = dr;
        colDelta = dc;
        symbol = symb;
    }

    public Direction reverse() {
        switch(this){
            case UP: return DOWN;
            case DOWN: return UP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
        }
        return null;
    }
}
