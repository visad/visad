
//
// RainSheet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.rabin;

// import JFC packages
import javax.swing.*;
import javax.swing.border.*;

// import AWT package
import java.awt.*;
import java.awt.event.*;

// import needed Spread Sheet classes
import visad.ss.BasicSSCell;
import visad.ss.FancySSCell;

// import other needed classes
import visad.*;
import visad.util.VisADSlider;
import java.rmi.RemoteException;


/** RainSheet is a &quot;toy&quot; version of visad.ss.SpreadSheet,
    with only two cells.  It demonstrates how the developer can use
    the classes in the visad.ss package as GUI components for their
    own applications.<P> */
public class RainSheet extends JFrame implements ActionListener {

  // type 'java RainSheet' to run this application




  static final int N_COLUMNS = 3;
  static final int N_ROWS = 4;
  static final JPanel[] row_panels = new JPanel[N_ROWS];
  static final FancySSCell[] cells = new FancySSCell[N_ROWS * N_COLUMNS];
  static final JTextField[] formulas = new JTextField[N_ROWS * N_COLUMNS];
  static final JButton[] maps = new JButton[N_ROWS * N_COLUMNS];


  /** the width and height of the UI frame */
  static final int WIDTH = 1100;
  static final int HEIGHT = 900;
  static final int CELL_WIDTH = 200;
  static final int CELL_HEIGHT = 200;

  static String[] formula_name =
    {"A1", "B1", "C1", "A2", "B2", "C2", "A3", "B3", "C3", "A4", "B4", "C4"};

  static String[] formula_array =
    {"file:dallas_2.5km_v5d.v5d",
     "A1(0)",
     "((10^((extract(B1,0))/10))/SLIDER300)^(1/SLIDER1_4)",
     "((10^((extract(B1,1))/10))/SLIDER300)^(1/SLIDER1_4)",
     "((10^((extract(B1,2))/10))/SLIDER300)^(1/SLIDER1_4)",
     "((10^((extract(B1,3))/10))/SLIDER300)^(1/SLIDER1_4)",
     "((10^((extract(B1,4))/10))/SLIDER300)^(1/SLIDER1_4)",
     "((10^((extract(B1,5))/10))/SLIDER300)^(1/SLIDER1_4)",
     "(10*C1+10*A2+10*B2+10*C2+10*A3+3*B3)/53",
     "extract(B1,6)",
     "extract(B1,7)",
     "extract(B1,8)"};


  /** The main method just constructs a RainSheet, displays it, and exits */
  public static void main(String[] argv)
         throws VisADException, RemoteException {
    RainSheet ms = new RainSheet();
    ms.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    ms.setSize(WIDTH, HEIGHT);
    Dimension appSize = ms.getSize();
    ms.setLocation(screenSize.width/2 - appSize.width/2,
                   screenSize.height/2 - appSize.height/2);
    ms.setVisible(true);
  }

  /** Creates a label with black text s and adds it to p */
  private static void createLabel(JPanel p, String s) {
    JLabel l = new JLabel(s);
    l.setForeground(Color.black);
    p.add(l);
  }


  /** Two text fields */
  private JTextField Formula1, Formula2;

  /** Constructs the RainSheet frame */
  public RainSheet() throws VisADException, RemoteException {
    // construct a frame with appropriate title
    super("RainSheet");

    // mapping dialog is the wrong color without this line
    setBackground(Color.white);

    // end program when this frame is closed
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quitProgram();
      }
    });

    // construct main panel
    JPanel main = new JPanel();
    setContentPane(main);
    main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));

    // construct left panel
    JPanel left = new JPanel();
    left.setBorder(new CompoundBorder(new EtchedBorder(),
                                      new EmptyBorder(5, 10, 5, 10)));
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    main.add(left);

    JPanel display_panel = new JPanel();
    display_panel.setLayout(new BoxLayout(display_panel, BoxLayout.Y_AXIS));
    display_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    main.add(display_panel);


    // add JLabels to left panel
    createLabel(left, "RainSheet -- a custom spreadsheet");
    createLabel(left, "for rain estimation");

    DataReference ref300 = new DataReferenceImpl("num300");
    DataReference ref1_4 = new DataReferenceImpl("num1_4");
    VisADSlider slider300 = new VisADSlider("num300", 0, 600, 300, 1.0,
                                            ref300, RealType.Generic);
    VisADSlider slider1_4 = new VisADSlider("num1_4", 0, 280, 140, 0.01,
                                            ref1_4, RealType.Generic);
    left.add(slider300);
    left.add(slider1_4);
    BasicSSCell.createVar("SLIDER300", ref300);
    BasicSSCell.createVar("SLIDER1_4", ref1_4);

    // create panel for Quit button so the button can be centered
    JPanel qpanel = new JPanel();
    qpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    qpanel.setLayout(new BoxLayout(qpanel, BoxLayout.X_AXIS));
    left.add(Box.createRigidArea(new Dimension(0, 15)));
    left.add(qpanel);

    // add Quit button to its panel
    JButton quit = new JButton("Quit");
    quit.addActionListener(this);
    quit.setActionCommand("quit");
    qpanel.add(Box.createHorizontalGlue());
    qpanel.add(quit);
    qpanel.add(Box.createHorizontalGlue());

    // don't let left panel shrink or grow in size
    Dimension lps = left.getPreferredSize();
    left.setMinimumSize(lps);
    left.setMaximumSize(lps);

    // construct two spreadsheet cells and related GUI components
    int i = 0;
    // create row JPanels
    for (int k=0; k<N_ROWS; k++) {
      row_panels[k] = new JPanel();
      row_panels[k].setLayout(new BoxLayout(row_panels[k],
                                            BoxLayout.X_AXIS));
      row_panels[k].setAlignmentY(JPanel.TOP_ALIGNMENT);
      row_panels[k].setAlignmentX(JPanel.LEFT_ALIGNMENT);
      display_panel.add(row_panels[k]);

      // create cell JPanels
      for (int j=0; j<N_COLUMNS; j++) {
        JPanel cellPanel = new JPanel();
        cellPanel.setLayout(new BoxLayout(cellPanel, BoxLayout.Y_AXIS));
        row_panels[k].add(cellPanel);
        FancySSCell fCell = null;
        try {
          fCell = new FancySSCell(formula_name[i], this);
          fCell.setDimension(true, false);  // set twoD = true, java2d = false
          // fCell.setFormula(formula_array[i]);
        }
        catch (Exception exc) {
          System.out.println("Could not create the first spreadsheet cell!");
          System.out.println("Received the following exception:");
          System.out.println(exc.toString());
          System.exit(i);
        }

        fCell.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
        fCell.setMaximumSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));

        JPanel bPanel = new JPanel();
        bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.X_AXIS));
        JButton load = new JButton("Load");
        load.addActionListener(this);
        load.setActionCommand("load" + i);
        bPanel.add(load);
        JButton save = new JButton("Save");
        save.addActionListener(this);
        save.setActionCommand("save" + i);
        bPanel.add(save);
        JButton map = new JButton("Maps");
        map.addActionListener(this);
        map.setActionCommand("map" + i);
        bPanel.add(map);
        JButton show = new JButton("Show");
        show.addActionListener(this);
        show.setActionCommand("show" + i);
        bPanel.add(show);
        JTextField tf = new JTextField();

        // WLH 2 Dec 98
        Dimension msize = tf.getMaximumSize();
        Dimension psize = tf.getPreferredSize();
        msize.height = psize.height;
        tf.setMaximumSize(msize);

        JPanel lPanel = new JPanel();
        lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.X_AXIS));
        lPanel.add(Box.createHorizontalGlue());
        JLabel l = new JLabel(formula_name[i]);
        l.setForeground(Color.blue);
        lPanel.add(l);
        lPanel.add(Box.createHorizontalGlue());
        JPanel fPanel = new JPanel();
        fPanel.setLayout(new BoxLayout(fPanel, BoxLayout.X_AXIS));
        createLabel(fPanel, "Formula:  ");
        JTextField textf = new JTextField(formula_array[i]);

        // WLH 2 Dec 98
        msize = textf.getMaximumSize();
        psize = textf.getPreferredSize();
        msize.height = psize.height;
        textf.setMaximumSize(msize);

        textf.addActionListener(this);
        textf.setActionCommand("formula" + i);
        fPanel.add(textf);
        cellPanel.add(lPanel);
        cellPanel.add(fPanel);
        cellPanel.add(fCell);
        cellPanel.add(bPanel);
        cells[i] = fCell;
        maps[i] = map;
        formulas[i] = textf;
        i++;
      } // end for (int j=0; j<N_COLUMNS; j++)
    } // end for (int k=0; k<N_ROWS; k++)
    for (i=0; i<N_ROWS * N_COLUMNS; i++) {
      // cells[i].setFormula(formula_array[i]);
    }
  }

  /** This method handles button presses */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("quit")) {
      quitProgram();
      return;
    }
    for (int i=0; i<N_ROWS * N_COLUMNS; i++) {
      if (cmd.equals("load" + i)) cells[i].loadDataDialog();
      else if (cmd.equals("save" + i)) cells[i].saveDataDialog(true);
      else if (cmd.equals("map" + i)) cells[i].addMapDialog();
      else if (cmd.equals("show" + i)) cells[i].showWidgetFrame();
      else if (cmd.equals("formula" + i)) {
        maps[i].requestFocus();
        try {
          cells[i].setFormula(formulas[i].getText());
        }
        catch (Exception exc) { }
      }
    }
  }

  /** Waits for files to finish saving before quitting */
  void quitProgram() {
    Thread t = new Thread() {
      public void run() {
        if (BasicSSCell.isSaving()) {
          System.out.println("Please wait for RainSheet to finish " +
                             "saving files...");
        }
        while (BasicSSCell.isSaving()) {
          try {
            sleep(500);
          }
          catch (InterruptedException exc) { }
        }
        System.exit(0);
      }
    };
    t.start();
  }
}

