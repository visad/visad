
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

/** MappingDialog is a dialog that lets the user create ScalarMaps */
public class MappingDialog extends JDialog implements ActionListener,
                                                      ListSelectionListener,
                                                      MouseListener {
  /** Flag whether user hit Done or Cancel button */
  public boolean Confirm = false;

  /** ScalarMaps selected by the user */
  public ScalarMap[] ScalarMaps;

  // These components affect each other
  JComponent MathCanvas;
  JScrollPane MathCanvasView;
  JList MathList;
  JComponent DisplayCanvas;
  DefaultListModel CurMaps;
  JList CurrentMaps;
  JScrollPane CurrentMapsView;
  Vector MathVector;
  int DuplCount = 0;
  String[] Scalars;
  RealType[] MathTypes;
  int[][] ScX;
  int[][] ScY;
  int[] ScW;
  int ScH;
  int StrWidth = 0;
  int StrHeight = 0;
  boolean[][][] Maps;
  String[][][] CurMapLabel;

  static final Font Mono = new Font("Monospaced", Font.PLAIN, 11);

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

  /** For synchronization */
  private Vector Lock = new Vector();

  /** Pre-loads the display.gif file, so it's ready when
      mapping dialog is requested */
  static void initDialog() {
    if (DRT == null) DRT = Toolkit.getDefaultToolkit().getImage("display.gif");
    Inited = true;
  }

  /** Constructor for MappingDialog */
  public MappingDialog(Frame parent, Data data, ScalarMap[] startMaps) {
    super(parent, "Set up data mappings", true);

    // set up content pane
    setBackground(Color.white);
    JPanel contentPane = new JPanel();
    setContentPane(contentPane);
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // parse MathType
    MathVector = new Vector();
    parseMathType(data);
    String mt = null;
    try {
      mt = data.getType().prettyString();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    final String[] mtype = extraPretty(mt);

    // alphabetize Scalars list
    sort(0, Scalars.length-1);

    // set up "MathType" label
    JLabel l0 = new JLabel("MathType:");
    l0.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    contentPane.add(l0);

    // set up top panel
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
    contentPane.add(topPanel);
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // draw the "pretty-print" MathType to an Image (slow!)
    final Image img = parent.createImage(StrWidth, StrHeight);
    Graphics g = img.getGraphics();
    g.setFont(Mono);
    g.setColor(Color.black);

    for (int i=0; i<mtype.length; i++) {
      g.drawString(mtype[i], 5, (ScH+2)*(i+1));
    }
    g.dispose();

    // set up MathType canvas
    MathCanvas = new JComponent() {
      public void paint(Graphics g) {
        // draw "pretty-print" MathType using image
        g.drawImage(img, 0, 0, this);
        int ind = MathList.getSelectedIndex();
        if (ind >= 0) {
          g.setFont(Mono);
          String s = (String) Scalars[ind];
          for (int i=0; i<ScX[ind].length; i++) {
            int x = ScX[ind][i]+5;
            int y = (ScH+2)*ScY[ind][i];
            g.setColor(Color.blue);
            g.fillRect(x, y+6, ScW[ind], ScH);
            g.setColor(Color.white);
            g.drawString(s, x, y+ScH+2);
          }
        }
      }
    };
    MathCanvas.setMinimumSize(new Dimension(StrWidth, StrHeight));
    MathCanvas.setPreferredSize(new Dimension(StrWidth, StrHeight));
    MathCanvas.addMouseListener(this);
    MathCanvas.setBackground(Color.white);

    // set up MathCanvas's ScrollPane
    MathCanvasView = new JScrollPane(MathCanvas);
    MathCanvasView.setMinimumSize(new Dimension(0, 0));
    MathCanvasView.setPreferredSize(new Dimension(0, 70));
    MathCanvasView.setBackground(Color.white);
    JScrollBar horiz = MathCanvasView.getHorizontalScrollBar();
    JScrollBar verti = MathCanvasView.getVerticalScrollBar();
    horiz.setBlockIncrement(5*ScH+10);
    horiz.setUnitIncrement(ScH+2);
    verti.setBlockIncrement(5*ScH+10);
    verti.setUnitIncrement(ScH+2);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    JPanel whitePanel = new JPanel();
    whitePanel.setBackground(Color.white);
    whitePanel.setLayout(new BoxLayout(whitePanel, BoxLayout.X_AXIS));
    whitePanel.add(MathCanvasView);
    topPanel.add(whitePanel);
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
    CurMaps = new DefaultListModel();
    int num = MathTypes.length;
    CurMaps.ensureCapacity(num*35);
    CurrentMaps = new JList(CurMaps);

    // set up "map from" list
    Maps = new boolean[num][7][5];
    CurMapLabel = new String[num][7][5];
    for (int i=0; i<num; i++) {
      for (int j=0; j<7; j++) {
        for (int k=0; k<5; k++) {
          Maps[i][j][k] = false;
          CurMapLabel[i][j][k] = Scalars[i]+" -> "+MapNames[j][k];
          if (startMaps != null) {
            for (int m=0; m<startMaps.length; m++) {
              if (startMaps[m].getScalar() == MathTypes[i]
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
    DisplayCanvas = new JComponent() {
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

  /** Parses a prettyString to find out some information,
      then eliminates duplicate RealTypes */
  String[] extraPretty(String pStr) {
    // extract number of lines from prettyString
    int numLines = 1;
    int len = pStr.length();
    for (int i=0; i<len; i++) {
      if (pStr.charAt(i) == '\n') numLines++;
    }

    // define new array of strings and other stuff
    String[] retStr = new String[numLines];
    int lineNum = 0;
    int lastLine = 0;
    String scalar = ((RealType) MathVector.elementAt(0)).getName();
    int scalarNum = 0;
    int scalarLen = scalar.length();
    int numScalars = MathVector.size();
    int[] x = new int[numScalars];
    int[] y = new int[numScalars];
    int[] w = new int[numScalars];
    FontMetrics fm = getFontMetrics(Mono);
    ScH = fm.getHeight();
    StrWidth = 0;
    StrHeight = (ScH+2)*numLines+10;

    // extract info from prettyString
    int q = 0;
    while (q <= len) {
      // fill in array of strings
      if (q == len || pStr.charAt(q) == '\n') {
        String lnStr = pStr.substring(lastLine, q);
        lnStr = lnStr+" ";  // stupid FontMetrics bug work-around
        int lnStrLen = fm.stringWidth(lnStr);
        if (lnStrLen > StrWidth) StrWidth = lnStrLen;
        retStr[lineNum++] = lnStr;
        lastLine = q+1;
      }

      // fill in scalar info
      if (q+scalarLen <= len &&
          pStr.substring(q, q+scalarLen).equals(scalar)) {
        x[scalarNum] = fm.stringWidth(pStr.substring(lastLine, q));
        y[scalarNum] = lineNum;
        w[scalarNum] = fm.stringWidth(scalar);
        scalarNum++;
        q += scalarLen;
        if (scalarNum < numScalars) {
          scalar = ((RealType) MathVector.elementAt(scalarNum)).getName();
          scalarLen = scalar.length();
        }
      }
      else q++;
    }

    // remove duplicates from all data structures
    int num = numScalars - DuplCount;
    Scalars = new String[num];
    MathTypes = new RealType[num];
    ScX = new int[num][];
    ScY = new int[num][];
    ScW = new int[num];
    int[] trans = new int[numScalars];
    int[] numXY = new int[numScalars];
    for (int i=0; i<numScalars; i++) numXY[i] = 0;
    int modifier = 0;
    for (int i=0; i<numScalars; i++) {
      // NOTE: This implementation assumes that Vector.indexOf(Object)
      //       returns the FIRST index in the Vector occupied by that
      //       Object (i.e., the closest to 0).
      int ind = MathVector.indexOf(MathVector.elementAt(i));
      if (i == ind) trans[i] = ind - modifier;
      else {
        trans[i] = trans[ind];
        modifier++;
      }
      numXY[ind]++;
    }
    int[] j = new int[num];
    for (int i=0; i<numScalars; i++) {
      int t = trans[i];
      if (numXY[i] > 0) {
        MathTypes[t] = (RealType) MathVector.elementAt(i);
        Scalars[t] = MathTypes[t].getName();
        int u = numXY[i];
        ScX[t] = new int[u];
        ScY[t] = new int[u];
        ScX[t][u-1] = x[i];
        ScY[t][u-1] = y[i];
        ScW[t] = w[i];
        j[t] = u - 2;
      }
      else {
        int jt = j[t]--;
        ScX[t][jt] = x[i];
        ScY[t][jt] = y[i];
      }
    }
    MathVector = null;

    return retStr;
  }

  /** Returns a Vector of Strings containing the ScalarType names */
  void parseMathType(Data data) {
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
      parseFunction((FunctionType) dataType);
    }
    else if (dataType instanceof SetType) {
      parseSet((SetType) dataType);
    }
    else if (dataType instanceof TupleType) {
      parseTuple((TupleType) dataType);
    }
    else parseScalar((ScalarType) dataType);
  }

  /** Used by parseMathType */
  void parseFunction(FunctionType mathType) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain);

    // extract range
    MathType range = mathType.getRange();
    if (range instanceof FunctionType) {
      parseFunction((FunctionType) range);
    }
    else if (range instanceof SetType) {
      parseSet((SetType) range);
    }
    else if (range instanceof TupleType) {
      parseTuple((TupleType) range);
    }
    else parseScalar((ScalarType) range);

    return;
  }

  /** Used by parseMathType */
  void parseSet(SetType mathType) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    parseTuple((TupleType) domain);

    return;
  }

  /** Used by parseMathType */
  void parseTuple(TupleType mathType) {
    // extract components
    for (int i=0; i<mathType.getDimension(); i++) {
      MathType cType = null;
      try {
        cType = mathType.getComponent(i);
      }
      catch (VisADException exc) { }

      if (cType != null) {
        if (cType instanceof FunctionType) {
          parseFunction((FunctionType) cType);
        }
        else if (cType instanceof SetType) {
          parseSet((SetType) cType);
        }
        else if (cType instanceof TupleType) {
          parseTuple((TupleType) cType);
        }
        else parseScalar((ScalarType) cType);
      }
    }
    return;
  }

  /** Used by parseMathType */
  void parseScalar(ScalarType mathType) {
    if (mathType instanceof RealType) {
      if (MathVector.contains(mathType)) DuplCount++;
      MathVector.addElement(mathType);
    }
  }

  /** Recursive quick-sort routine used to alphabetize scalars */
  private void sort(int lo0, int hi0) {
    int lo = lo0;
    int hi = hi0;
    String mid = Scalars[(lo0+hi0)/2].toLowerCase();
    while (lo <= hi) {
      while (lo < hi0 && Scalars[lo].toLowerCase().compareTo(mid) < 0) ++lo;
      while (hi > lo0 && Scalars[hi].toLowerCase().compareTo(mid) > 0) --hi;
      if (lo <= hi) {
        String s = Scalars[lo];
        Scalars[lo] = Scalars[hi];
        Scalars[hi] = s;
        RealType r = MathTypes[lo];
        MathTypes[lo] = MathTypes[hi];
        MathTypes[hi] = r;
        int[] ia = ScX[lo];
        ScX[lo] = ScX[hi];
        ScX[hi] = ia;
        ia = ScY[lo];
        ScY[lo] = ScY[hi];
        ScY[hi] = ia;
        int i = ScW[lo];
        ScW[lo] = ScW[hi];
        ScW[hi] = i;
        lo++;
        hi--;
      }
    }
    if (lo0 < hi) sort(lo0, hi);
    if (lo < hi0) sort(lo, hi0);
  }

  /** Highlights a box in the "map to" canvas */
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

  /** Handles button press events */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    synchronized (Lock) {
      if (cmd.equals("all")) { // clear all
        if (CurMaps.getSize() > 0) {
          // take all maps off list
          CurrentMaps.clearSelection(); // work-around for nasty swing bug
          CurMaps.removeAllElements();
          for (int i=0; i<CurMapLabel.length; i++) {
            for (int j=0; j<7; j++) {
              for (int k=0; k<5; k++) Maps[i][j][k] = false;
            }
          }
          // update components
          Graphics g = DisplayCanvas.getGraphics();
          DisplayCanvas.paint(g);
          g.dispose();
          CurrentMaps.repaint();
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
          Graphics g = DisplayCanvas.getGraphics();
          DisplayCanvas.paint(g);
          g.dispose();
          CurrentMaps.repaint();
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
                  ScalarMaps[s++] = new ScalarMap(MathTypes[i], MapTypes[j][k]);
                }
                catch (VisADException exc) {
                  okay = false;
                  JOptionPane.showMessageDialog(this, "The mapping ("
                          +Scalars[i]+" -> "+MapNames[j][k]
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
  }

  /** Handles list selection change events */
  public void valueChanged(ListSelectionEvent e) {
    synchronized (Lock) {
      if (!e.getValueIsAdjusting()) {
        if ((JList) e.getSource() == MathList) {
          Graphics g = DisplayCanvas.getGraphics();
          DisplayCanvas.paint(g);
          g.dispose();
          int i = MathList.getSelectedIndex();
          Rectangle r = new Rectangle(ScX[i][0]+5, (ScH+2)*ScY[i][0]+6,
                                      ScW[i], ScH);
          MathList.ensureIndexIsVisible(i);
          MathCanvas.scrollRectToVisible(r);
          MathCanvas.repaint();
        }
      }
    }
  }

  /** Handles mouse clicks in the "map to" canvas */
  public void mousePressed(MouseEvent e) {
    synchronized (Lock) {
      Component c = e.getComponent();
      if (c == MathCanvas) {
        Point p = e.getPoint();
        for (int i=0; i<Scalars.length; i++) {
          for (int j=0; j<ScX[i].length; j++) {
            Rectangle r = new Rectangle(ScX[i][j]+5, (ScH+2)*ScY[i][j]+6,
                                        ScW[i], ScH);
            if (r.contains(p)) {
              MathList.setSelectedIndex(i);
              MathList.ensureIndexIsVisible(i);
              MathCanvas.scrollRectToVisible(r);
              return;
            }
          }
        }
      }
      else if (c == DisplayCanvas) {
        int col = e.getX() / 40;
        int row = e.getY() / 40;
        int ind = MathList.getSelectedIndex();
/* WLH 5 September 98
        if (ind >= 0) {
*/
        if (ind >= 0 && row >= 0 && col >= 0 && row < 5 && col < 7) {
          Maps[ind][col][row] = !Maps[ind][col][row];
          if (Maps[ind][col][row]) {
            CurMaps.addElement(CurMapLabel[ind][col][row]);
          }
          else {
            CurrentMaps.clearSelection();
            CurMaps.removeElement(CurMapLabel[ind][col][row]);
          }
          // redraw DisplayCanvas
          Graphics g = DisplayCanvas.getGraphics();
          g.setClip(40*col, 40*row, 41, 41);
          DisplayCanvas.paint(g);
          g.dispose();

          // redraw CurrentMaps
          CurrentMaps.repaint();
          CurrentMapsView.validate();
        }
      }
    }
  }

  // unused MouseListener methods
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseClicked(MouseEvent e) { }
  public void mouseReleased(MouseEvent e) { }

}

