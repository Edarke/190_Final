package robotics;


import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Evan on 6/3/16.
 */
public class Map implements Drawable {

    private final Queue<Tile> destinations = new LinkedList<>();
    private final Tile[][] grid;
    private Car car;


    public Map(int rows, int cols) {
        grid = new Tile[rows][cols];
        for(int r = 0; r < rows; ++r) {
            grid[r] = new Tile[cols];
            for(int c = 0; c < cols; ++c)
                grid[r][c] = new Tile(r, c);
        }

        foreach(t -> {
            foreach(t::isNeighbor, n -> {
                t.height = (int)(t.height * .8  + .2 * n.height);
            });
        });
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


    public void setWall(int r, int c){
        grid[r][c].setWall(true);
    }

    public void setCar(int r, int c){
        car = new Car(grid[r][c], 10, 1000);
    }


    public void setFuel(int row, int col) {
        grid[row][col].setFuel(true);
    }



    public void start(){
        Tile dest = destinations.poll();
        dest.setCost(0);

        for(int i = 0; i < grid.length * grid[0].length; ++i){
            foreach(t -> foreach(n -> n.update(t, this)));
        }
    }



    public double getCost(Tile from, Tile to){
        if(!from.isNeighbor(to))
            return Double.POSITIVE_INFINITY;

        final boolean isUphil = from.height < to.height;

        if(isUphil){
            return 1.0 + car.getWeight() * (to.height - from.height)/2;  // idk, this is random
        } else {
            return 1.0 ; // idk, this should have some non-constant cost right?
        }
    }



    @Override
    public void draw(Graphics2D g) {
        for(Tile[] row : grid)
            for(Tile t : row)
                t.draw(g);

        if(car != null)
            car.draw(g);
    }

    public void addDestination(int row, int col) {
        destinations.add(grid[row][col]);
        grid[row][col].setDest(true);
    }

    public Car getCar() {
        return car;
    }
}
