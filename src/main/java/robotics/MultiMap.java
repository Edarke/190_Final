package robotics;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Evan on 6/3/16.
 */
public class MultiMap implements Drawable {
    public static final double INITIAL_FUEL = 1000;
    private final List<Tile> fuels = new LinkedList<>();
    private final Tile[][] grid;

    private Car car;
    private Tile dest;

    private Map view;


    public MultiMap(int rows, int cols) {
        grid = new Tile[rows][cols];
        for(int r = 0; r < rows; ++r) {
            grid[r] = new Tile[cols];
            for(int c = 0; c < cols; ++c)
                grid[r][c] = new Tile(r, c);
        }

        foreach(t -> {
            foreach(t::isNeighbor, n -> {
                t.height = (int)(t.height * .9  + .1 * n.height);
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

    public void resetView() {
        view = null;
        if(car != null)
            car.setFuel(1000);
    }

    public void addHeight(int deltaH, int row, int col) {
        grid[row][col] = new Tile(grid[row][col], grid[row][col].height + deltaH);
    }


    class MetaNode {
        public final Tile tile;
        public double cost = Double.POSITIVE_INFINITY;
        public MetaNode previous;

        private final java.util.Map<Tile, Map> edges;


        public MetaNode(Tile t, java.util.Map edgeWeights){
            tile = t;
            edges = edgeWeights;
        }


        public double getDistanceTo(MetaNode other){
            return edges.get(tile).getDistance(other.tile);
        }

        public void update(MetaNode other){
            final double proposedCost = other.cost + other.getDistanceTo(this);
            if(proposedCost < cost){
                cost = proposedCost;
                previous = other;
            }
        }

    }



    public Queue<Map> start(JProgressBar progress){
        final java.util.Map<Tile, Map> sources = new LinkedHashMap<>();

        final Map toDest = new Map(grid, dest, car, false);
        final Map toStart = new Map(grid, car.getTile(), car, false);
        sources.put(dest, toDest);
        sources.put(car.getTile(), toStart);

        fuels.parallelStream().forEach(t -> sources.put(t, new Map(grid, t, car, true)));




        final List<MetaNode> metas = new ArrayList<>();
        for(Tile t: fuels)
            metas.add(new MetaNode(t, sources));

        final MetaNode start = new MetaNode(car.getTile(), sources);
        final MetaNode end = new MetaNode(dest, sources);
        start.cost = 0;
        metas.add(start);
        metas.add(end);


        for(int i = 0; i < metas.size(); ++i){
            metas.forEach(m -> metas.forEach(m::update));
        }

        if(end.cost == Double.POSITIVE_INFINITY){
            JOptionPane.showMessageDialog(null, "There is no such path.");
            view = sources.get(start.tile);
            return null;
        }


        final Deque<Map> metaList = new LinkedList<>();

        for(MetaNode next = end; next != null; next = next.previous)
            metaList.addFirst(sources.get(next.tile));

        System.out.println("MetaList size is " + metaList.size());




        while(metaList.size() != 1){
            Map v = metaList.poll();
            view = v;
            System.out.println("Switching views");


            Tile terminal;
            if(metaList.isEmpty())
                terminal = v.getTile(dest.row, dest.col);
            else {
                final Tile checkpoint = metaList.peek().getSource();
                terminal = v.getTile(checkpoint.row, checkpoint.col);
            }

            Tile term = terminal;
            final Deque<Tile> path = new LinkedList<>();
            while(terminal != null){
                path.push(terminal);
                terminal = terminal.getPrevious().orElse(null);
            }

            System.out.println("Estimated Segment cost: " + term.getCost());



            for(Tile t : path){
                final double cost = car.getTile().equals(t) ? 0 : car.getCost(car.getTile(), t);

                System.out.println("Total cost is " + t.getCost());
                System.out.println("Delta cost is " + (t.getCost() - t.getPrevious().map(Tile::getCost).orElse(0.0) + "; " + cost));
                if(t.equals(term)){
                    System.out.println("Done with segment! (Estimation: " + term.getCost() + ")");
                }

                if(t.isFuel())
                    car.setFuel(INITIAL_FUEL);
                else
                    car.setFuel(car.getFuel() - cost);

                SwingUtilities.invokeLater(() -> progress.setValue((int) car.getFuel()));

                car.setTile(t);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Actual segment cost " + (INITIAL_FUEL - car.getFuel()));
        }
            return metaList;
    }



    public void setWall(int r, int c){
        grid[r][c].setWall(true);
    }

    public void setCar(int r, int c){
        car = new Car(grid[r][c], 10, INITIAL_FUEL);
    }


    public void setFuel(int row, int col) {
        grid[row][col].setFuel(true);
        fuels.add(grid[row][col]);
    }


    public void setDestination(int row, int col) {
        if(dest != null)
            dest.setDest(false);
        dest = grid[row][col];
        dest.setDest(true);
    }



    @Override
    public void draw(Graphics2D g) {
        if(view != null)
            view.draw(g);
        else {
            foreach(t -> t.draw(g));

            if(car != null)
                car.draw(g);
        }
    }
}
