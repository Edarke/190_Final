package robotics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Evan on 6/3/16.
 */
public class Tile implements Drawable {
    private static Image gasImage;
    static{
        try {
            gasImage = ImageIO.read(Car.class.getResourceAsStream("/gas.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final int row;
    public final int col;
    public int height;
    private boolean isWall;
    private boolean isFuel;
    private boolean isDest;
    private Direction policy;
    private Optional<Tile> previous = Optional.empty();
    private double cost = Double.POSITIVE_INFINITY;


    public Tile(int r, int c){
        this(r, c, ThreadLocalRandom.current().nextInt(Math.abs((r + c) % 6) <= 1 ? 100 : 0 , Math.abs((r + c) % 5) > 3 ? 255 : 151));
    }

    public Tile(int r, int c, int h){
        row = r;
        col = c;
        height = h;
    }

    public Tile(Tile other){
        this(other, other.height);
    }

    public Tile(Tile other, int h) {
        row = other.row;
        col = other.col;
        height = Math.min(255, Math.max(h, 0));
        isWall = other.isWall;
        isFuel = other.isFuel;
        isDest = other.isDest;
    }


    @Override
    public void draw(Graphics2D g) {
        g.setColor(getColor());

        final Rectangle bounds = getBounds(g);
        g.fill(bounds);

        if(isFuel)
            g.drawImage(gasImage, bounds.x, bounds.y, bounds.width, bounds.height, g.getColor(), null);


        g.setColor(Color.BLACK);
        if(policy != null)
            g.drawString(policy.symbol, bounds.x+ bounds.width/2, bounds.y + bounds.height/2);
        g.draw(bounds);
    }





    public boolean isNeighbor(Tile other){
        return Math.abs(other.row - row) + Math.abs(other.col - col) == 1;
    }


    public Rectangle getBounds(Graphics2D g){
        final Rectangle bounds = new Rectangle(g.getClipBounds().width / Frame.COLS, g.getClipBounds().height / Frame.ROWS);
        bounds.translate(col*bounds.width, row*bounds.height);
        return bounds;
    }




    public void setPrevious(Tile other){
        previous = Optional.ofNullable(other);
        if(other != null){
            if(other.row > row)
                policy = Direction.DOWN;
            else if (other.row < row){
                policy = Direction.UP;
            } else if (other.col > col){
                policy = Direction.RIGHT;
            } else if (other.col < col){
                policy = Direction.LEFT;
            }
        }
    }



    @Override
    public String toString() {
        return "Tile(" +
                "col=" + col +
                ", row=" + row +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tile tile = (Tile) o;
        return row == tile.row && col == tile.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
    }

    public void setFuel(boolean fuel) {
        isFuel = fuel;
    }

    public void setDest(boolean dest) {
        this.isDest = dest;
    }

    public Color getColor() {
        if(isWall)
            return Color.DARK_GRAY;
        else if(isDest)
            return Color.CYAN;
        else
            return new Color(height, 0, 255 - height);
    }

    public Optional<Tile> getPrevious(){
        return previous;
    }


    public void update(Tile other, Map map, boolean isReversed){
        if(isWall()){
            return;
        }


        final double proposedCost;

        if(!isReversed)
            proposedCost = other.cost + map.getCost(other, this);
        else
            proposedCost = other.cost + map.getCost(this, other);


        if(proposedCost < map.getCar().getFuel() && proposedCost < cost){
            cost = proposedCost;
            setPrevious(other);
        }
    }

    public void setCost(int cost) {
        this.cost = cost;
    }


    public void reset(){
        cost = Double.POSITIVE_INFINITY;
        previous = Optional.empty();
        policy = null;
    }

    public int getRow(){
        return row;
    }
    public int getCol(){
        return col;
    }

    public double getCost() {
        return cost;
    }

    public boolean isFuel() {
        return isFuel;
    }
}
