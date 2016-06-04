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
        final Map toStart = new Map(grid, car.getTile(), car);
        sources.put(dest, toDest);
        sources.put(car.getTile(), toStart);

        fuels.parallelStream().forEach(t -> {
            sources.put(t, new Map(grid, t, car));
        });




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
        }


        final Deque<Map> metaList = new LinkedList<>();

        for(MetaNode next = end; next != null; next = next.previous)
            metaList.addFirst(sources.get(next.tile));

        System.out.println("MetaList size is " + metaList.size());




        while(!metaList.isEmpty()){
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

            final Deque<Tile> path = new LinkedList<>();
            while(terminal != null){
                path.push(terminal);
                terminal = terminal.getPrevious().orElse(null);
            }

            for(Tile t : path){
                final double cost = getCost(car.getTile(), t);

                if(t.isFuel())
                    car.setFuel(1000);
                else
                    car.setFuel(car.getFuel() - cost);
                SwingUtilities.invokeLater(() -> progress.setValue((int) car.getFuel()));

                car.setTile(t);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

            return metaList;
    }





    public double getCost(Tile from, Tile to){
        if(!from.isNeighbor(to))
            return Double.POSITIVE_INFINITY;

        final boolean isUpHill = to.height - from.height > 5.0;
        final boolean isDownHill = from.height - to.height > 5.0;

        if(isUpHill){
            return 1.0 + car.getWeight() * (to.height - from.height);  // idk, this is random
        } else if (isDownHill){
            return .5 ; // idk, this should have some non-constant cost right?
        } else {
            return 1.0;
        }
    }



    public void setWall(int r, int c){
        grid[r][c].setWall(true);
    }

    public void setCar(int r, int c){
        car = new Car(grid[r][c], 10, 1000);
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
