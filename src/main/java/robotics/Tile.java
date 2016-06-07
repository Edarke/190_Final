package robotics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Evan on 6/3/16.
 */
public class Tile implements Drawable {
    public static Image gasImage;
    static{
        try {
            gasImage = ImageIO.read(Car.class.getResourceAsStream("/gas.png")).getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final int row;
    public final int col;
    private int height;
    private boolean isWall;
    private boolean isFuel;
    private boolean isDest;
    private Direction policy;
    private Optional<Tile> previous = Optional.empty();
    private double cost = Double.POSITIVE_INFINITY;


    public Tile(int p, int r, int c){
        this(r, c, rand(p, r,c), null);
    }


    private static int rand(int p, int r, int c){
        final double mod = Math.pow(Math.cos((pNorm(p, r,c))/12.0 * Math.PI), 2);
        return (int)ThreadLocalRandom.current().nextDouble(mod * 100 , 100 + mod * 155);
    }

    private static double pNorm(int p, int r, int c){
        double r2  = Math.pow(Math.abs(r-Frame.ROWS/2), p);
        double c2  = Math.pow(Math.abs(c-Frame.COLS/2), p);

        return Math.pow(r2 + c2, 1.0/p);
    }

    private Tile(int r, int c, int h, Object privy){
        row = r;
        col = c;
        height = Math.min(255, Math.max(h, 0));
    }


    public Tile(Tile other) {
        this(other.row, other.col, other.height, null);
        isWall = other.isWall;
        isFuel = other.isFuel;
        isDest = other.isDest;
    }


    @Override
    public void draw(Graphics2D g) {
        g.setColor(getColor());

        final Rectangle bounds = getBounds(g);
        g.fill(bounds);


        if(isFuel) {
            g.drawImage(gasImage, bounds.x, bounds.y, bounds.width, bounds.height, g.getColor(), null);
        }

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
        return "Tile(" + row + ", " + col + ')';
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


    public void update(Tile other, Map map, boolean isFromSource){
        if(isWall()){
            return;
        }


        final double proposedCost;

        if(!isFromSource)
            proposedCost = other.cost + map.getCar().getCost(other, this);
        else
            proposedCost = other.cost + map.getCar().getCost(this, other);


        if(proposedCost < cost && proposedCost < map.getCar().getFuel()){
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


    public double getCost() {
        return cost;
    }

    public boolean isFuel() {
        return isFuel;
    }

    public void setHeight(int h) {
        height = Math.min(255, Math.max(h, 0));
    }

    public int getHeight() {
        return height;
    }


}
