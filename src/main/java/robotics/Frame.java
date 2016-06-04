package robotics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Evan on 6/3/16.
 */
public class Frame extends JPanel implements MouseMotionListener {
    public static final int ROWS = 31, COLS = 31;



    enum State {Inc, Dec, Wall, Fuel, Car, Dest, Start}

    private final JFrame frame = new JFrame("CSE 190");
    private final JProgressBar fuel = new JProgressBar(0, 1000);
    private final MultiMap map = new MultiMap(ROWS, COLS);
    private State state = State.Wall;

    private final JButton hiller = new JButton("Add Hills");
    private final JButton valler = new JButton("Add Valleys");
    private final JButton waller = new JButton("Add Walls");
    private final JButton fueler = new JButton("Add Fuel");
    private final JButton carer = new JButton("Set Car");
    private final JButton dester = new JButton("Set Destination");
    private final JButton starter = new JButton("Start");

    public Frame() {
        frame.setSize(1200, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(fuel, BorderLayout.NORTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        final MouseListener clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final int tWidth = getWidth() / COLS;
                final int tHeight = getHeight() / ROWS;

                final int col = (e.getPoint().x) / tWidth;
                final int row = (e.getPoint().y) / tHeight;

                if(state == State.Wall)
                    map.setWall(row, col);
                else if(state == State.Fuel)
                    map.setFuel(row, col);
                else if(state == State.Car)
                    map.setCar(row, col);
                else if(state == State.Dest)
                    map.setDestination(row, col);
            }
        };
        this.addMouseMotionListener(this);
        this.addMouseListener(clickListener);

        fuel.setValue(fuel.getMaximum());


        final JPanel south = new JPanel(new FlowLayout());
        south.add(hiller);
        south.add(valler);
        south.add(waller);
        south.add(fueler);
        south.add(carer);
        south.add(dester);
        south.add(starter);


        waller.addActionListener(e -> setState(State.Wall, fuel));
        fueler.addActionListener(e -> setState(State.Fuel, fuel));
        carer.addActionListener(e -> setState(State.Car, fuel));
        dester.addActionListener(e -> setState(State.Dest, fuel));
        starter.addActionListener(e -> setState(State.Start, fuel));
        hiller.addActionListener(e -> setState(State.Inc, fuel));
        valler.addActionListener(e -> setState(State.Dec, fuel));



        frame.add(south, BorderLayout.SOUTH);
        new Timer(50, e -> repaint()).start();
    }





    public void setState(State s, JProgressBar progress){
        state = s;

        progress.setValue(progress.getMaximum());
        new Thread( () -> {
            if (state == State.Start)
                map.start(progress);
            else
                 map.resetView();
        }).start();
    }




    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g);
        map.draw(g2d);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        final int tWidth = getWidth() / COLS;
        final int tHeight = getHeight() / ROWS;

        final int col = (e.getPoint().x) / tWidth;
        final int row = (e.getPoint().y) / tHeight;


        if(state == State.Wall)
            map.setWall(row, col);
        else if(state == State.Inc)
            map.addHeight(5, row, col);
        else if(state == State.Dec)
            map.addHeight(-5, row, col);
    }

    @Override
    public void mouseMoved(MouseEvent e) { }



    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new Frame();
    }
}
