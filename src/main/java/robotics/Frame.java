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
    public static final int ROWS = 20, COLS = 20;



    enum State { WALLS, FUELS, CAR, DESTS, STARTED }

    private final JFrame frame = new JFrame("CSE 190");
    private final JProgressBar fuel = new JProgressBar(0, 100);
    private final Map map = new Map(ROWS, COLS);
    private State state = State.WALLS;
    private final JButton stateChanger = new JButton(state.toString());


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

                if(state == State.FUELS)
                    map.setFuel(row, col);
                else if(state == State.CAR)
                    map.setCar(row, col);
                else if(state == State.DESTS)
                    map.addDestination(row, col);
            }
        };
        this.addMouseMotionListener(this);
        this.addMouseListener(clickListener);

        fuel.setValue(100);

        stateChanger.addActionListener(e -> nextState());
        frame.add(stateChanger, BorderLayout.SOUTH);

        new Timer(20, e -> repaint()).start();
    }


    public void nextState(){
        state = State.values()[(state.ordinal()+1) % State.values().length];
        if(state == State.STARTED)
            map.start();
        stateChanger.setText(state.name());
    }




    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        map.draw(g2d);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        final int tWidth = getWidth() / COLS;
        final int tHeight = getHeight() / ROWS;

        final int col = (e.getPoint().x) / tWidth;
        final int row = (e.getPoint().y) / tHeight;


        if(state == State.WALLS)
            map.setWall(row, col);
    }

    @Override
    public void mouseMoved(MouseEvent e) { }



    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new Frame();
    }
}
