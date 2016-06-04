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
    private String policy = "";
    private Optional<Tile> previous = Optional.empty();
    private double cost = Double.POSITIVE_INFINITY;


    public Tile(int r, int c){
        this(r, c, ThreadLocalRandom.current().nextInt(Math.abs((r + c) % 6) <= 1 ? 150 : 0 , Math.abs((r + c) % 5) > 3 ? 255 : 151));
    }

    public Tile(int r, int c, int h){
        row = r;
        col = c;
        height = h;
    }



    @Override
    public void draw(Graphics2D g) {
        g.setColor(getColor());

        final Rectangle bounds = getBounds(g);
        g.fill(bounds);

        if(isFuel)
            g.drawImage(gasImage, bounds.x, bounds.y, bounds.width, bounds.height, g.getColor(), null);


        g.setColor(Color.BLACK);
        g.drawString(policy, bounds.x+ bounds.width/2, bounds.y + bounds.height/2);
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
                policy = Direction.DOWN.symbol;
            else if (other.row < row){
                policy = Direction.UP.symbol;
            } else if (other.col > col){
                policy = Direction.RIGHT.symbol;
            } else if (other.col < col){
                policy = Direction.LEFT.symbol;
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


    public void update(Tile other, Map map){
        if(isWall()){
            return;
        }

        final double proposedCost = other.cost + map.getCost(other, this);
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
        policy = "";
    }
}
