package robotics;


import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Evan on 6/3/16.
 */
public class Map implements Drawable {

    private final Queue<Tile> destinations = new LinkedList<>();
    private final Tile[][] grid;
    private Car car;

    private final Tile source;

    private final boolean isReversed;


    public Map(Tile[][] protoType, Tile src, Car car1, boolean reversed) {
        isReversed = reversed;
        grid = new Tile[protoType.length][protoType[0].length];
        car = car1;


        for(int r = 0; r < grid.length; ++r){
            grid[r] = new Tile[grid[0].length];
            for(int c = 0; c < grid[0].length; ++c){
                grid[r][c] = new Tile(protoType[r][c]);
            }
        }

        source = getTile(src.getRow(), src.getCol());
        bellmanFord();
    }

    public Map(Tile[][] protoType, Tile src, Car car1){
        this(protoType, src, car1, false);
    }




    public void foreach(Consumer<Tile> f) {
        foreach(null, f);
    }

    public void foreach(Predicate<Tile> filter, Consumer<Tile> f){
        for(Tile[] row: grid)
            for(Tile t : row)
                if(filter == null || filter.test(t))
                    f.accept(t);
    }





    private void bellmanFord(){
        foreach(Tile::reset);

        source.setCost(0);

        for(int i = 0; i < grid.length * grid[0].length; ++i)
            foreach(t -> getNeighbors(t).forEach(n -> n.update(t, this, isReversed)));
    }


    private List<Tile> getNeighbors(Tile t){
        final List<Tile> neighbors = new ArrayList<>(4);

        if(t.row > 0)
            neighbors.add(grid[t.row-1][t.col]);

        if(t.row < grid.length -1)
            neighbors.add(grid[t.row+1][t.col]);

        if(t.col > 0)
            neighbors.add(grid[t.row][t.col-1]);

        if(t.col < grid[0].length - 1)
            neighbors.add(grid[t.row][t.col+1]);

        return neighbors;

    }



    public double getDistance(Tile t){
        return getTile(t.getRow(), t.getCol()).getCost();
    }


    public Comparator<Tile> getCompartor(){
        return (t1, t2) -> Double.compare(getDistance(t1), getDistance(t2));
    }



    public Tile getSource(){
        return source;
    }


    @Override
    public void draw(Graphics2D g) {
        foreach(t -> t.draw(g));

        if(car != null)
            car.draw(g);

        g.setColor(Color.GREEN);
        g.draw(source.getBounds(g));
    }


    public Car getCar() {
        return car;
    }

    public Tile getTile(int r, int c){
        return grid[r][c];
    }



    public double getCost(Tile from, Tile to){
        if(!from.isNeighbor(to))
            return Double.POSITIVE_INFINITY;

        final boolean isUpHill = to.height - from.height > 5.0;
        final boolean isDownHill = from.height - to.height > 5.0;

        if(isUpHill){
            return 1.0 + car.getWeight() * (to.height - from.height);  // idk, this is random
        } else if (isDownHill){
            return 1.0 ; // idk, this should have some non-constant cost right?
        } else {
            return 1.0;
        }
    }
}
