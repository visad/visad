
//
// MiniSheet.java
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

// import JFC packages
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;
import com.sun.java.swing.event.*;

// import AWT package
import java.awt.*;
import java.awt.event.*;

// import needed Spread Sheet classes
import visad.ss.FancySSCell;
import visad.ss.MappingDialog;

/** MiniSheet is a &quot;toy&quot; version of visad.ss.SpreadSheet,
    with only two cells.  It demonstrates how the developer can use
    the classes in the visad.ss package as GUI components for their
    own applications.<P> */
public class MiniSheet extends JFrame implements ActionListener {

  // type 'java MiniSheet' to run this application

  /** The main method just constructs a MiniSheet, displays it, and exits */
  public static void main(String[] argv) {
    MiniSheet ms = new MiniSheet();
    ms.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
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

  /** The two spreadsheet cells */
  private FancySSCell Cell1, Cell2;

  /** Two mapping buttons */
  private JButton Maps1, Maps2;

  /** Two text fields */
  private JTextField Formula1, Formula2;

  /** Constructs the MiniSheet frame */
  public MiniSheet() {
    // construct a frame with appropriate title
    super("MiniSheet");

    // mapping dialog is the wrong color without this line
    setBackground(Color.white);

    // end program when this frame is closed
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
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

    // add JLabels to left panel
    createLabel(left, "MiniSheet -- a toy spreadsheet example program");
    createLabel(left, "created using components from the");
    createLabel(left, "VisAD Visualization Spread Sheet.  See:");
    createLabel(left, "    http://www.ssec.wisc.edu/~curtis/ss.html");
    createLabel(left, "for more information about the Spread Sheet.");
    createLabel(left, "  ");
    createLabel(left, "Drag the left mouse button for 3-D rotation.");
    createLabel(left, "  ");
    createLabel(left, "Click the Load button to import a data set.");
    createLabel(left, "  ");
    createLabel(left, "Click the Save button to export a data set");
    createLabel(left, "to a netCDF file.");
    createLabel(left, "  ");
    createLabel(left, "Click the Maps button to change the mappings");
    createLabel(left, "from the data to the display.");
    createLabel(left, "  ");
    createLabel(left, "Click the Show button to display a cell's");
    createLabel(left, "controls if they were closed.");
    createLabel(left, "  ");
    createLabel(left, "Type a formula into a cell's Formula text field");
    createLabel(left, "to compute the cell using that formula.");
    createLabel(left, "For example, try typing \"SQRT(CELL1) / 3\"");
    createLabel(left, "into the second cell's Formula Text Field");
    createLabel(left, "after loading data into the first cell.");

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
    for (int i=1; i<=2; i++) {
      JPanel cellPanel = new JPanel();
      cellPanel.setLayout(new BoxLayout(cellPanel, BoxLayout.Y_AXIS));
      main.add(cellPanel);
      FancySSCell fCell = null;
      try {
        fCell = new FancySSCell("CELL" + i, this);
        fCell.setDimension(false, false);  // set Java3D display mode
      }
      catch (Exception exc) {
        System.out.println("Could not create the first spreadsheet cell!");
        System.out.println("Received the following exception:");
        System.out.println(exc.toString());
        System.exit(i);
      }
      fCell.setPreferredSize(new Dimension(400, 400));
      fCell.setMaximumSize(new Dimension(400, 400));
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
      JButton maps = new JButton("Maps");
      maps.addActionListener(this);
      maps.setActionCommand("maps" + i);
      bPanel.add(maps);
      JButton show = new JButton("Show");
      show.addActionListener(this);
      show.setActionCommand("show" + i);
      bPanel.add(show);
      JTextField tf = new JTextField();
      JPanel lPanel = new JPanel();
      lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.X_AXIS));
      lPanel.add(Box.createHorizontalGlue());
      JLabel l = new JLabel("CELL" + i);
      l.setForeground(Color.blue);
      lPanel.add(l);
      lPanel.add(Box.createHorizontalGlue());
      JPanel fPanel = new JPanel();
      fPanel.setLayout(new BoxLayout(fPanel, BoxLayout.X_AXIS));
      createLabel(fPanel, "Formula:  ");
      JTextField textf = new JTextField();
      textf.addActionListener(this);
      textf.setActionCommand("formula" + i);
      fPanel.add(textf);
      cellPanel.add(lPanel);
      cellPanel.add(fPanel);
      cellPanel.add(fCell);
      cellPanel.add(bPanel);
      if (i == 1) {
        Cell1 = fCell;
        Maps1 = maps;
        Formula1 = textf;
      }
      else {
        Cell2 = fCell;
        Maps2 = maps;
        Formula2 = textf;
      }
    }
  }

  /** This method handles button presses */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("quit")) System.exit(0);
    else if (cmd.equals("load1")) Cell1.loadDataDialog();
    else if (cmd.equals("load2")) Cell2.loadDataDialog();
    else if (cmd.equals("save1")) Cell1.saveDataDialog(true);
    else if (cmd.equals("save2")) Cell2.saveDataDialog(true);
    else if (cmd.equals("maps1")) Cell1.addMapDialog();
    else if (cmd.equals("maps2")) Cell2.addMapDialog();
    else if (cmd.equals("show1")) Cell1.showWidgetFrame();
    else if (cmd.equals("show2")) Cell2.showWidgetFrame();
    else if (cmd.equals("formula1")) {
      Maps1.requestFocus();
      try {
        Cell1.setFormula(Formula1.getText());
      }
      catch (Exception exc) { }
    }
    else if (cmd.equals("formula2")) {
      Maps2.requestFocus();
      try {
        Cell2.setFormula(Formula2.getText());
      }
      catch (Exception exc) { }
    }
  }

}

