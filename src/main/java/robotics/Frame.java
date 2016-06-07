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
    public static final int ROWS = 40, COLS = (ROWS * 16) / 9;
    public static final int NORM = 1;

    enum Algorithm { Minimize_Fuel, Maximize_Reward}
    enum State {Inc, Dec, Wall, Fuel, Car, Dest, Start}

    private final JFrame frame = new JFrame("CSE 190");
    private final JProgressBar fuel = new JProgressBar(0, (int) MultiMap.INITIAL_FUEL);
    private MultiMap map = new MultiMap(NORM, ROWS, COLS);
    private State state = State.Car;
    private final ButtonGroup group = new ButtonGroup();
    final JComboBox<Algorithm> toggleButton = new JComboBox<>();



    public Frame() {
        frame.setSize(1290, 900);
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

        final JRadioButton hiller = new JRadioButton("Add Hills");
        final JRadioButton valler = new JRadioButton("Add Valleys");
        final JRadioButton waller = new JRadioButton("Add Walls");
        final JRadioButton fueler = new JRadioButton("Add Fuel");
        final JRadioButton carer = new JRadioButton("Set Car");
        final JRadioButton dester = new JRadioButton("Set Destination");
        final JButton starter = new JButton("Start");

        group.add(hiller);
        group.add(valler);
        group.add(waller);
        group.add(fueler);
        group.add(carer);
        group.add(dester);

        carer.setSelected(true);


        final JSpinner norm = new JSpinner(new SpinnerNumberModel(NORM, 1, 10, 1));
        norm.addChangeListener(e -> map = new MultiMap((int) norm.getValue(), ROWS, COLS) );
        south.add(norm);




        south.add(hiller);
        south.add(valler);
        south.add(waller);
        south.add(fueler);
        south.add(carer);
        south.add(dester);
        south.add(starter);



        toggleButton.addItem(Algorithm.Minimize_Fuel);
        toggleButton.addItem(Algorithm.Maximize_Reward);


        south.add(toggleButton);

        waller.addActionListener(e -> setState(State.Wall, fuel));
        fueler.addActionListener(e -> setState(State.Fuel, fuel));
        carer.addActionListener(e -> setState(State.Car, fuel));
        dester.addActionListener(e -> setState(State.Dest, fuel));
        starter.addActionListener(e -> setState(State.Start, fuel));
        hiller.addActionListener(e -> setState(State.Inc, fuel));
        valler.addActionListener(e -> setState(State.Dec, fuel));



        frame.add(south, BorderLayout.SOUTH);
        frame.invalidate();
        new Timer(20, e -> repaint()).start();
    }





    private void setState(State s, JProgressBar progress){
        state = s;
        if(state == State.Start)
            group.clearSelection();

        progress.setValue((int) MultiMap.INITIAL_FUEL);
        new Thread( () -> {
            if (state == State.Start)
                map.start(toggleButton.getSelectedIndex() == 0, progress);
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
            map.addHeight(10, row, col);
        else if(state == State.Dec)
            map.addHeight(-10, row, col);
    }

    @Override
    public void mouseMoved(MouseEvent e) { }



    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new Frame();
    }
}
