
//
// MappingDialog.java
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

package visad.ss;

// JFC packages
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

// RMI classes
import java.rmi.RemoteException;

// Utility classes
import java.util.Vector;

// VisAD packages
import visad.*;

/** MappingDialog is a dialog that lets the user create ScalarMaps. */
public class MappingDialog extends JDialog implements ActionListener,
                                                      ListSelectionListener,
                                                      MouseListener {
  /** Flag whether user hit Done or Cancel button. */
  public boolean Confirm = false;

  /** ScalarMaps selected by the user */
  public ScalarMap[] ScalarMaps;

  // These components affect each other
  Canvas MathCanvas;
  ScrollPane MathCanvasView;
  JList MathList;
  Canvas DisplayCanvas;
  DefaultListModel CurMaps;
  JList CurrentMaps;
  JScrollPane CurrentMapsView;
  Vector Scalars;
  Vector MathTypes;
  boolean[][][] Maps;
  String[][][] CurMapLabel;

  /** names of system intrinsic DisplayRealTypes */
  static final String[][] MapNames = {
    {"X Axis", "X Offset", "Latitude", "Flow1 X", "Flow2 X"},
    {"Y Axis", "Y Offset", "Longitude", "Flow1 Y", "Flow2 Y"},
    {"Z Axis", "Z Offset", "Radius", "Flow1 Z", "Flow2 Z"},
    {"Red", "Cyan", "Hue", "Animation", "Select Value"},
    {"Green", "Magenta", "Saturation", "Iso-contour", "Select Range"},
    {"Blue", "Yellow", "Value", "Alpha", "List"},
    {"RGB", "CMY", "HSV", "RGBA", "Shape"}
  };

  /** list of system intrinsic DisplayRealTypes */
  static final DisplayRealType[][] MapTypes = {
    {Display.XAxis, Display.XAxisOffset, Display.Latitude,
     Display.Flow1X, Display.Flow2X},
    {Display.YAxis, Display.YAxisOffset, Display.Longitude,
     Display.Flow1Y, Display.Flow2Y},
    {Display.ZAxis, Display.ZAxisOffset, Display.Radius,
     Display.Flow1Z, Display.Flow2Z},
    {Display.Red, Display.Cyan, Display.Hue,
     Display.Animation, Display.SelectValue},
    {Display.Green, Display.Magenta, Display.Saturation,
     Display.IsoContour, Display.SelectRange},
    {Display.Blue, Display.Yellow, Display.Value,
     Display.Alpha, Display.List},
    {Display.RGB, Display.CMY, Display.HSV,
     Display.RGBA, Display.Shape}
  };

  /** number of system intrinsic DisplayRealTypes */
  static final int NumMaps = MapTypes.length;

  /** display.gif image */
  static Image DRT = null;

  /** Whether DRT image has been initialized */
  static boolean Inited = false;

  /** Pre-loads the display.gif file, so it's ready when
      mapping dialog is requested. */
  static void initDialog() {
    if (DRT == null) DRT = Toolkit.getDefaultToolkit().getImage("display.gif");
    Inited = true;
  }

  /** Constructor for MappingDialog. */
  public MappingDialog(Frame parent, Data data, ScalarMap[] startMaps) {
    super(parent, "Set up data mappings", true);

    // set up content pane
    setBackground(Color.white);
    JPanel contentPane = new JPanel();
    setContentPane(contentPane);
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // set up "MathType" label
    Scalars = new Vector();
    MathTypes = new Vector();
    parseMathType(data, Scalars, MathTypes);
    String mt = null;
    try {
      mt = data.getType().prettyString();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    final String mtype = mt;
    final Font mono = new Font("Monospaced", Font.PLAIN, 11);
    JLabel l0 = new JLabel("MathType:");
    l0.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    contentPane.add(l0);

    // set up top panel
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
    contentPane.add(topPanel);
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // set up MathType canvas
    MathCanvas = new Canvas() {
      // this paint method draws the "pretty-print" MathType
      public void paint(Graphics g) {
        g.setFont(mono);
        g.drawString(mtype, 5, 10); /* CTR: TEMP */
        /* CTR: Need an array of strings here, since \n does not cause
                drawString to go down a line.  Probably should pre-
                calculate array of where arrows are to speed up
                drawing them.  Also, pre-calculate array of where
                scalars are, and perhaps how long they are, so that
                they can be quickly highlighted. */
      }

      public Dimension getMinimumSize() {
        return new Dimension(0, 0);
      }

      public Dimension getPreferredSize() {
        return new Dimension(0, 0);
      }
    };
    MathCanvas.setBackground(Color.white);
    MathCanvasView = new ScrollPane() {
      public Dimension getMinimumSize() {
        return new Dimension(0, 0);
      }

      public Dimension getPreferredSize() {
        return new Dimension(0, 70);
      }
    };
    MathCanvasView.setBackground(Color.white);
    MathCanvasView.add(MathCanvas);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    topPanel.add(MathCanvasView);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    // set up lower panel
    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
    contentPane.add(lowerPanel);
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // set up left-side panel
    JPanel lsPanel = new JPanel();
    lsPanel.setLayout(new BoxLayout(lsPanel, BoxLayout.Y_AXIS));
    lowerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    lowerPanel.add(lsPanel);

    // begin set up "current mappings" list
    int num = Scalars.size();
    CurMaps = new DefaultListModel();
    CurMaps.ensureCapacity(num*35);
    CurrentMaps = new JList(CurMaps);

    // set up "map from" list
    Maps = new boolean[num][7][5];
    CurMapLabel = new String[num][7][5];
    for (int i=0; i<num; i++) {
      for (int j=0; j<7; j++) {
        for (int k=0; k<5; k++) {
          Maps[i][j][k] = false;
          CurMapLabel[i][j][k] = Scalars.elementAt(i)+" -> "+MapNames[j][k];
          if (startMaps != null) {
            for (int m=0; m<startMaps.length; m++) {
              if (startMaps[m].getScalar() == (RealType) MathTypes.elementAt(i)
                        && startMaps[m].getDisplayScalar() == MapTypes[j][k]) {
                Maps[i][j][k] = true;
                CurMaps.addElement(CurMapLabel[i][j][k]);
              }
            }
          }
        }
      }
    }
    MathList = new JList(Scalars);
    MathList.addListSelectionListener(this);
    MathList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane mathListS = new JScrollPane(MathList) {
      public Dimension getMinimumSize() {
        return new Dimension(0, 265);
      }
      public Dimension getPreferredSize() {
        return new Dimension(200, 265);
      }
      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 265);
      }
    };
    JLabel l1 = new JLabel("Map from:");
    l1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    lsPanel.add(l1);
    lsPanel.add(mathListS);
    lowerPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    // set up center panel
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    lowerPanel.add(centerPanel);
    lowerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    JLabel l2 = new JLabel("Map to:");
    l2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    centerPanel.add(l2);

    // set up "map to" canvas
    DisplayCanvas = new Canvas() {
      public void paint(Graphics g) {
        if (DRT == null) {
          if (!Inited) {
            DRT = Toolkit.getDefaultToolkit().getImage("display.gif");
          }
          MediaTracker mtracker = new MediaTracker(this);
          mtracker.addImage(DRT, 0);
          try {
            mtracker.waitForID(0);
          }
          catch (InterruptedException exc) {
            return;
          }
          if (DRT == null) return;
        }
        g.drawImage(DRT, 0, 0, this);
        int ind = MathList.getSelectedIndex();
        if (ind >= 0) {
          for (int col=0; col<7; col++) {
            for (int row=0; row<5; row++) {
              if (Maps[ind][col][row]) highlightBox(col, row, g);
            }
          }
        }
      }

      public Dimension getMinimumSize() {
        return new Dimension(280, 200);
      }

      public Dimension getPreferredSize() {
        return new Dimension(280, 200);
      }

      public Dimension getMaximumSize() {
        return new Dimension(280, 200);
      }
    };
    DisplayCanvas.addMouseListener(this);
    centerPanel.add(DisplayCanvas);
    centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));

    // set up button panel 1
    JPanel b1Panel = new JPanel();
    b1Panel.setLayout(new BoxLayout(b1Panel, BoxLayout.X_AXIS));
    centerPanel.add(b1Panel);
    centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    b1Panel.add(Box.createHorizontalGlue());

    // set up "clear all" button
    JButton clearAll = new JButton("Clear all");
    clearAll.setAlignmentX(JButton.CENTER_ALIGNMENT);
    clearAll.setToolTipText("Clear all mappings from mappings box");
    clearAll.setActionCommand("all");
    clearAll.addActionListener(this);
    b1Panel.add(clearAll);
    b1Panel.add(Box.createRigidArea(new Dimension(5, 0)));

    // set up "clear selected" button
    JButton clearSel = new JButton("Clear selected");
    clearSel.setAlignmentX(JButton.CENTER_ALIGNMENT);
    clearSel.setToolTipText("Clear selected mappings from mappings box");
    clearSel.setActionCommand("sel");
    clearSel.addActionListener(this);
    b1Panel.add(clearSel);
    b1Panel.add(Box.createHorizontalGlue());

    // set up button panel 2
    JPanel b2Panel = new JPanel();
    b2Panel.setLayout(new BoxLayout(b2Panel, BoxLayout.X_AXIS));
    centerPanel.add(b2Panel);
    b2Panel.add(Box.createHorizontalGlue());

    // set up done button
    JButton done = new JButton("Done");
    done.setAlignmentX(JButton.CENTER_ALIGNMENT);
    done.setToolTipText("Apply selected mappings");
    done.setActionCommand("done");
    done.addActionListener(this);
    b2Panel.add(done);
    b2Panel.add(Box.createRigidArea(new Dimension(5, 0)));

    // set up cancel button
    JButton cancel = new JButton("Cancel");
    cancel.setAlignmentX(JButton.CENTER_ALIGNMENT);
    cancel.setToolTipText("Close dialog box without applying mappings");
    cancel.setActionCommand("cancel");
    cancel.addActionListener(this);
    b2Panel.add(cancel);
    b2Panel.add(Box.createHorizontalGlue());

    // set up right-side panel
    JPanel rsPanel = new JPanel();
    rsPanel.setLayout(new BoxLayout(rsPanel, BoxLayout.Y_AXIS));
    lowerPanel.add(rsPanel);
    lowerPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    // finish set up "current mappings" list
    CurrentMaps.addListSelectionListener(this);
    CurrentMapsView = new JScrollPane(CurrentMaps) {
      public Dimension getMinimumSize() {
        return new Dimension(0, 265);
      }

      public Dimension getPreferredSize() {
        return new Dimension(200, 265);
      }

      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 265);
      }
    };
    JLabel l3 = new JLabel("Current maps:");
    l3.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    rsPanel.add(l3);
    rsPanel.add(CurrentMapsView);
  }

  /** Returns a Vector of Strings containing the ScalarType names. */
  void parseMathType(Data data, Vector strs, Vector objs) {
    MathType dataType;
    try {
      dataType = data.getType();
    }
    catch (RemoteException exc) {
      return;
    }
    catch (VisADException exc) {
      return;
    }

    if (dataType instanceof FunctionType) {
      parseFunction((FunctionType) dataType, strs, objs);
    }
    else if (dataType instanceof SetType) {
      parseSet((SetType) dataType, strs, objs);
    }
    else if (dataType instanceof TupleType) {
      parseTuple((TupleType) dataType, strs, objs);
    }
    else parseScalar((ScalarType) dataType, strs, objs);
  }

  /** Used by parseMathType. */
  void parseFunction(FunctionType mathType, Vector strs, Vector objs) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, strs, objs);

    // extract range
    MathType range = mathType.getRange();
    if (range instanceof FunctionType) {
      parseFunction((FunctionType) range, strs, objs);
    }
    else if (range instanceof SetType) {
      parseSet((SetType) range, strs, objs);
    }
    else if (range instanceof TupleType) {
      parseTuple((TupleType) range, strs, objs);
    }
    else parseScalar((ScalarType) range, strs, objs);

    return;
  }

  /** Used by parseMathType. */
  void parseSet(SetType mathType, Vector strs, Vector objs) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain, strs, objs);

    return;
  }

  /** Used by parseMathType. */
  void parseTuple(TupleType mathType, Vector strs, Vector objs) {
    // extract components
    for (int i=0; i<mathType.getDimension(); i++) {
      MathType cType = null;
      try {
        cType = mathType.getComponent(i);
      }
      catch (VisADException exc) { }

      if (cType != null) {
        if (cType instanceof FunctionType) {
          parseFunction((FunctionType) cType, strs, objs);
        }
        else if (cType instanceof SetType) {
          parseSet((SetType) cType, strs, objs);
        }
        else if (cType instanceof TupleType) {
          parseTuple((TupleType) cType, strs, objs);
        }
        else parseScalar((ScalarType) cType, strs, objs);
      }
    }
    return;
  }

  /** Used by parseMathType. */
  void parseScalar(ScalarType mathType, Vector strs, Vector objs) {
    String name = mathType.getName();
    if (mathType instanceof RealType) {
      strs.addElement(name);
      objs.addElement(mathType);
    }
  }

  /** Highlights a box in the "map to" canvas. */
  void highlightBox(int col, int row, Graphics g) {
    int x = 40*col;
    int y = 40*row;
    final int n1 = 11;
    final int n2 = 29;
    g.setColor(Color.blue);
    g.drawRect(x, y, 40, 40);
    g.drawLine(x, y+n1, x+n1, y);
    g.drawLine(x, y+n2, x+n2, y);
    g.drawLine(x+n1, y+40, x+40, y+n1);
    g.drawLine(x+n2, y+40, x+40, y+n2);
    g.drawLine(x+n2, y, x+40, y+n1);
    g.drawLine(x+n1, y, x+40, y+n2);
    g.drawLine(x, y+n1, x+n2, y+40);
    g.drawLine(x, y+n2, x+n1, y+40);
  }

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("all")) { // clear all
      if (CurMaps.getSize() > 0) {
        // take all maps off list
        CurMaps.removeAllElements();
        for (int i=0; i<CurMapLabel.length; i++) {
          for (int j=0; j<7; j++) {
            for (int k=0; k<5; k++) Maps[i][j][k] = false;
          }
        }
        // update components
        DisplayCanvas.paint(DisplayCanvas.getGraphics());
        CurrentMapsView.validate();
      }
    }

    else if (cmd.equals("sel")) { // clear selected
      int[] ind = CurrentMaps.getSelectedIndices();
      int len = ind.length;
      for (int x=len-1; x>=0; x--) {
        String s = (String) CurMaps.getElementAt(ind[x]);
        boolean looking = true;
        for (int i=0; i<CurMapLabel.length && looking; i++) {
          for (int j=0; j<7 && looking; j++) {
            for (int k=0; k<5 && looking; k++) {
              if (CurMapLabel[i][j][k] == s) {
                Maps[i][j][k] = false;
                looking = false;
              }
            }
          }
        }
        // take map off list
        CurMaps.removeElementAt(ind[x]);
      }
      if (len > 0) {
        // update components
        DisplayCanvas.paint(DisplayCanvas.getGraphics());
        CurrentMapsView.validate();
      }
    }

    else if (cmd.equals("done")) {
      boolean okay = true;
      int size = CurMaps.getSize();
      ScalarMaps = new ScalarMap[size];
      int s = 0;
      for (int i=0; i<CurMapLabel.length; i++) {
        for (int j=0; j<7; j++) {
          for (int k=0; k<5; k++) {
            if (Maps[i][j][k]) {
              try {
                ScalarMaps[s++] = new ScalarMap((RealType)
                                  MathTypes.elementAt(i), MapTypes[j][k]);
              }
              catch (VisADException exc) {
                okay = false;
                JOptionPane.showMessageDialog(this, "The mapping ("
                        +Scalars.elementAt(i)+" -> "+MapNames[j][k]
                        +") is not valid.", "Illegal mapping",
                         JOptionPane.ERROR_MESSAGE);
              }
            }
          }
        }
      }
      if (okay) {
        Confirm = true;
        setVisible(false);
      }
    }

    else if (cmd.equals("cancel")) setVisible(false);
  }

  /** Handles list selection change events. */
  public void valueChanged(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      if ((JList) e.getSource() == MathList) {
        DisplayCanvas.paint(DisplayCanvas.getGraphics());
      }
    }
  }

  /** Handles mouse clicks in the "map to" canvas. */
  public void mousePressed(MouseEvent e) {
    int col = e.getX() / 40;
    int row = e.getY() / 40;
    int ind = MathList.getSelectedIndex();
    if (ind >= 0) {
      Maps[ind][col][row] = !Maps[ind][col][row];
      if (Maps[ind][col][row]) {
        CurMaps.addElement(CurMapLabel[ind][col][row]);
      }
      else {
        CurMaps.removeElement(CurMapLabel[ind][col][row]);
      }
      Graphics g = DisplayCanvas.getGraphics();
      g.setClip(40*col, 40*row, 41, 41);
      DisplayCanvas.paint(g);
      
      CurrentMapsView.validate();
    }
  }

  // unused MouseListener methods
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseClicked(MouseEvent e) { }
  public void mouseReleased(MouseEvent e) { }

}

