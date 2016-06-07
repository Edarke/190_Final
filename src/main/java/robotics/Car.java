package robotics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

/**
 * Created by Evan on 6/3/16.
 */
public class Car implements Drawable {
    public static Image truckImg;
    static{
        try {
            truckImg = ImageIO.read(Car.class.getResourceAsStream("/truck.png")).getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Tile current;
    private double fuel;
    private double  weight;

    public Car(Tile start, double initialWeight, double initialFuel){
        current = start;
        fuel = initialFuel;
        weight = initialWeight;
    }


    public void draw(Graphics2D g){
        g.setColor(new Color(0,255,0,125));
        final Rectangle bounds = current.getBounds(g);
        g.drawImage(truckImg, bounds.x, bounds.y, bounds.width, bounds.height, current.getColor(), null);
    }

    public double getWeight() {
        return weight;
    }

    public double getFuel() {
        return fuel;
    }

    public Tile getTile() {
        return current;
    }

    public void setTile(Tile tile) {
        this.current = tile;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public double getCost(Tile from, Tile to){
        if(!from.isNeighbor(to))
            return Double.POSITIVE_INFINITY;

        final boolean isUpHill = to.getHeight() - from.getHeight() > 0;

        if(isUpHill){
            return 10.0 + 2*(to.getHeight() - from.getHeight());  // idk, this is random
        } else
            return 10.0;
    }
}
