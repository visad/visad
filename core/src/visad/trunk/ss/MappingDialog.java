//
// MappingDialog.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.ss;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;

/** MappingDialog is a dialog that lets the user create ScalarMaps */
public class MappingDialog extends JDialog
  implements ActionListener, ListSelectionListener, MouseListener
{
  /** Flag whether user hit Done or Cancel button */
  public boolean Confirm = false;

  /** ScalarMaps selected by the user */
  public ScalarMap[] ScalarMaps;

  // GUI components
  private JComponent MathCanvas;
  private JScrollPane MathCanvasView;
  private JComponent CoordCanvas = null;
  private JScrollPane CoordCanvasView;
  private boolean CoordRefs;
  private JList MathList;
  private JComponent DisplayCanvas;
  private DefaultListModel CurMaps;
  private JList CurrentMaps;
  private JScrollPane CurrentMapsView;

  // state info
  private String[] Scalars;
  private RealType[] MathTypes;
  private int[][] ScX;
  private int[][] ScY;
  private int[] ScW;
  private int ScH;
  private int[] ScB;
  private int[] StrWidth;
  private int[] StrHeight;
  private boolean[][][] Maps;
  private String[][][] CurMapLabel;

  private static final Font Mono = new Font("Monospaced", Font.PLAIN, 11);

  /** names of system intrinsic DisplayRealTypes */
  private static final String[][] MapNames = {
    {"X Axis", "X Offset", "Latitude", "Flow1 X", "Flow2 X"},
    {"Y Axis", "Y Offset", "Longitude", "Flow1 Y", "Flow2 Y"},
    {"Z Axis", "Z Offset", "Radius", "Flow1 Z", "Flow2 Z"},
    {"Red", "Cyan", "Hue", "Animation", "Select Value"},
    {"Green", "Magenta", "Saturation", "Iso-contour", "Select Range"},
    {"Blue", "Yellow", "Value", "Alpha", "Text"},
    {"RGB", "CMY", "HSV", "RGBA", "Shape"}
  };

  /** list of system intrinsic DisplayRealTypes */
  private static final DisplayRealType[][] MapTypes = {
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
     Display.Alpha, Display.Text},
    {Display.RGB, Display.CMY, Display.HSV,
     Display.RGBA, Display.Shape}
  };

  /** number of system intrinsic DisplayRealTypes */
  private static final int NumMaps = MapTypes.length;

  /** display.gif image */
  private static Image DRT = null;

  /** whether DRT image has been initialized */
  private static boolean Inited = false;

  /** for synchronization */
  private Object Lock = new Object();

  /** pre-load the display.gif file, so it's ready when
      mapping dialog is requested */
  static void initDialog() {
    if (DRT == null) {
      URL url = MappingDialog.class.getResource("display.gif");
      DRT = Toolkit.getDefaultToolkit().getImage(url);
    }
    Inited = true;
  }

  /** return a human-readable list of CoordinateSystem dependencies
      and fill in Vector v with CoordinateSystem reference RealTypes */
  private static String prettyCoordSys(MathType type, Vector v) {
    String s = "";
    if (type instanceof FunctionType) {
      s = s + pcsFunction((FunctionType) type, v);
    }
    else if (type instanceof SetType) {
      s = s + pcsSet((SetType) type, v);
    }
    else if (type instanceof TupleType) {
      s = s + pcsTuple((TupleType) type, v);
    }

    return s;
  }

  /** used by prettyCoordSys */
  private static String pcsFunction(FunctionType mathType, Vector v) {
    String s = "";
    // extract domain
    RealTupleType domain = mathType.getDomain();
    s = s + pcsTuple((TupleType) domain, v);

    // extract range
    MathType range = mathType.getRange();
    if (range instanceof FunctionType) {
      s = s + pcsFunction((FunctionType) range, v);
    }
    else if (range instanceof SetType) {
      s = s + pcsSet((SetType) range, v);
    }
    else if (range instanceof TupleType) {
      s = s + pcsTuple((TupleType) range, v);
    }

    return s;
  }

  /** used by prettyCoordSys */
  private static String pcsSet(SetType mathType, Vector v) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    return pcsTuple((TupleType) domain, v);
  }

  /** used by prettyCoordSys */
  private static String pcsTuple(TupleType mathType, Vector v) {
    String s = "";
    if (mathType instanceof RealTupleType) {
      RealTupleType rtt = (RealTupleType) mathType;
      CoordinateSystem cs = rtt.getCoordinateSystem();
      if (cs != null) {
        RealTupleType ref = cs.getReference();
        if (ref != null) {
          s = s + rtt.prettyString() + " ==> " + ref.prettyString() + '\n';
          for (int i=0; i<ref.getDimension(); i++) {
            try {
              v.add(ref.getComponent(i));
            }
            catch (VisADException exc) { }
          }
        }
      }
    }
    else {
      // extract components
      for (int j=0; j<mathType.getDimension(); j++) {
        MathType cType = null;
        try {
          cType = mathType.getComponent(j);
        }
        catch (VisADException exc) { }

        if (cType != null) {
          if (cType instanceof FunctionType) {
            s = s + pcsFunction((FunctionType) cType, v);
          }
          else if (cType instanceof SetType) {
            s = s + pcsSet((SetType) cType, v);
          }
          else if (cType instanceof TupleType) {
            s = s + pcsTuple((TupleType) cType, v);
          }
        }
      }
    }
    return s;
  }

  /** this MappingDialog's copy of DRT with certain DisplayRealTypes
      blacked out as necessary */
  private Image MapTo;

  /** the MathType that this mapping dialog works with */
  private MathType type = null;

  /** whether this mapping dialog allows mappings to Alpha and RGBA */
  private boolean AllowAlpha;

  /** whether this mapping dialog allows mappings to Z-Axis, Latitude,
      and Z-Offset */
  private boolean Allow3D;

  /** constructor for MappingDialog */
  public MappingDialog(Frame parent, Data data, ScalarMap[] startMaps,
    boolean allowAlpha, boolean allow3D)
  {
    super(parent, "Set up data mappings", true);
    AllowAlpha = allowAlpha;
    Allow3D = allow3D;

    // set up content pane
    setBackground(Color.white);
    JPanel contentPane = new JPanel();
    setContentPane(contentPane);
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // parse MathType
    try {
      type = data.getType();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
    Vector[] v = new Vector[2];
    v[0] = new Vector();
    v[1] = new Vector();
    String[] mt = new String[2];
    int dupl = BasicSSCell.getRealTypes(data, v[0]);
    mt[0] = (type == null ? "" : type.prettyString());
    mt[1] = (type == null ? "" : prettyCoordSys(type, v[1]));

    // extract a bunch of info
    int[] duplA = new int[2];
    duplA[0] = dupl;
    duplA[1] = 0;
    int[] nl = extraPretty(mt, v, duplA);
    String[][] mtype = new String[2][];
    for (int j=0; j<2; j++) {
      mtype[j] = new String[nl[j]];
      int curLine = 0;
      int c = 0;
      for (int i=0; i<nl[j]; i++) {
        int sc = c;
        while (c < mt[j].length() && mt[j].charAt(c) != '\n') c++;
        mtype[j][curLine++] = mt[j].substring(sc, c++);
      }
    }

    // alphabetize Scalars list
    sort(0, Scalars.length-1);

    // mark whether there are CoordinateSystem references
    CoordRefs = (mtype[1].length > 1);

    // set up "MathType" label
    JLabel l0 = new JLabel("MathType:");
    l0.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    contentPane.add(l0);

    // set up top panel
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
    contentPane.add(topPanel);
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // set up "CoordinateSystem references" label
    if (CoordRefs) {
      JLabel l1 = new JLabel("CoordinateSystem references:");
      l1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      contentPane.add(l1);
    }

    // set up second top panel
    JPanel topPanel2 = new JPanel();
    topPanel2.setLayout(new BoxLayout(topPanel2, BoxLayout.X_AXIS));
    contentPane.add(topPanel2);
    contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

    // draw the "pretty-print" MathType to an Image (slow!)
    final Image ppImg = parent.createImage(StrWidth[0], StrHeight[0]);
    Graphics g = ppImg.getGraphics();
    g.setFont(Mono);
    g.setColor(Color.black);
    for (int i=0; i<mtype[0].length; i++) {
      g.drawString(mtype[0][i], 5, (ScH+2)*(i+1));
    }
    g.dispose();

    // set up MathType canvas
    MathCanvas = new JComponent() {
      public void paint(Graphics g2) {
        // draw "pretty-print" MathType using its Image
        g2.drawImage(ppImg, 0, 0, this);
        int ind = MathList.getSelectedIndex();
        if (ind >= 0 && ScB[ind] == 0) {
          g2.setFont(Mono);
          String s = (String) Scalars[ind];
          for (int i=0; i<ScX[ind].length; i++) {
            int x = ScX[ind][i]+5;
            int y = (ScH+2)*ScY[ind][i];
            g2.setColor(Color.blue);
            g2.fillRect(x, y+6, ScW[ind], ScH);
            g2.setColor(Color.white);
            g2.drawString(s, x, y+ScH+2);
          }
        }
        // work-around for JDK 1.3 beta bug
        Dimension d = MathCanvasView.getSize();
        g2.setColor(Color.white);
        g2.fillRect(StrWidth[0], 0, d.width, d.height);
        g2.fillRect(0, StrHeight[0], StrWidth[0], d.height);
      }
    };
    MathCanvas.setMinimumSize(new Dimension(StrWidth[0], StrHeight[0]));
    MathCanvas.setPreferredSize(new Dimension(StrWidth[0], StrHeight[0]));
    MathCanvas.addMouseListener(this);
    MathCanvas.setBackground(Color.white);

    // set up MathCanvas's ScrollPane
    MathCanvasView = new JScrollPane(MathCanvas);
    MathCanvasView.setMinimumSize(new Dimension(0, 0));
    int prefMCHeight = StrHeight[0] + 10;
    if (prefMCHeight < 70) prefMCHeight = 70;
    int maxMCHeight = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
    if (prefMCHeight > maxMCHeight) prefMCHeight = maxMCHeight;
    MathCanvasView.setPreferredSize(new Dimension(0, prefMCHeight));
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

    // set up far-left-side panel
    JPanel flsPanel = new JPanel();
    flsPanel.setLayout(new BoxLayout(flsPanel, BoxLayout.Y_AXIS));
    lowerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    lowerPanel.add(flsPanel);

    // only do CoordSys stuff if there are CoordinateSystem references
    if (CoordRefs) {
      // draw the pretty-print CoordinateSystem references to an Image (slow!)
      final Image csImg = parent.createImage(StrWidth[1], StrHeight[1]);
      g = csImg.getGraphics();
      g.setFont(Mono);
      g.setColor(Color.black);
      for (int i=0; i<mtype[1].length; i++) {
        g.drawString(mtype[1][i], 5, (ScH+2)*(i+1));
      }
      g.dispose();

      // set up CoordinateSystem references canvas
      CoordCanvas = new JComponent() {
        public void paint(Graphics g2) {
          // draw pretty-print CoordinateSystem reference list using its Image
          g2.drawImage(csImg, 0, 0, this);
          int ind = MathList.getSelectedIndex();
          if (ind >= 0 && ScB[ind] == 1) {
            g2.setFont(Mono);
            String s = (String) Scalars[ind];
            for (int i=0; i<ScX[ind].length; i++) {
              int x = ScX[ind][i]+5;
              int y = (ScH+2)*ScY[ind][i];
              g2.setColor(Color.blue);
              g2.fillRect(x, y+6, ScW[ind], ScH);
              g2.setColor(Color.white);
              g2.drawString(s, x, y+ScH+2);
            }
          }
        }
      };
      CoordCanvas.setMinimumSize(new Dimension(StrWidth[1], StrHeight[1]));
      CoordCanvas.setPreferredSize(new Dimension(StrWidth[1], StrHeight[1]));
      CoordCanvas.addMouseListener(this);
      CoordCanvas.setBackground(Color.white);

      // set up CoordCanvas's ScrollPane
      CoordCanvasView = new JScrollPane(CoordCanvas);
      CoordCanvasView.setMinimumSize(new Dimension(0, 0));
      CoordCanvasView.setPreferredSize(new Dimension(0, StrHeight[1]));
      int prefCCHeight = StrHeight[1] + 10;
      if (prefCCHeight < 70) prefCCHeight = 70;
      int maxCCHeight = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
      if (prefCCHeight > maxCCHeight) prefCCHeight = maxCCHeight;
      CoordCanvasView.setPreferredSize(new Dimension(0, prefCCHeight));
      CoordCanvasView.setBackground(Color.white);
      horiz = CoordCanvasView.getHorizontalScrollBar();
      verti = CoordCanvasView.getVerticalScrollBar();
      horiz.setBlockIncrement(5*ScH+10);
      horiz.setUnitIncrement(ScH+2);
      verti.setBlockIncrement(5*ScH+10);
      verti.setUnitIncrement(ScH+2);
      topPanel2.add(Box.createRigidArea(new Dimension(5, 0)));
      JPanel whitePanel2 = new JPanel();
      whitePanel2.setBackground(Color.white);
      whitePanel2.setLayout(new BoxLayout(whitePanel2, BoxLayout.X_AXIS));
      whitePanel2.add(CoordCanvasView);
      topPanel2.add(whitePanel2);
      topPanel2.add(Box.createRigidArea(new Dimension(5, 0)));
    }

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
          CurMapLabel[i][j][k] = Scalars[i] + " -> " + MapNames[j][k];
          if (startMaps != null) {
            for (int m=0; m<startMaps.length; m++) {
              if (startMaps[m].getScalar().equals(MathTypes[i]) &&
                  startMaps[m].getDisplayScalar().equals(MapTypes[j][k])) {
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
    JLabel l2 = new JLabel("Map from:");
    l2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    lsPanel.add(l2);
    lsPanel.add(mathListS);
    lowerPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    // set up center panel
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    lowerPanel.add(centerPanel);
    lowerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    JLabel l3 = new JLabel("Map to:");
    l3.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    centerPanel.add(l3);

    // finish loading DRT if necessary
    if (!Inited) initDialog();

    MediaTracker mtracker = new MediaTracker(this);
    mtracker.addImage(DRT, 0);
    try {
      mtracker.waitForID(0);
    }
    catch (InterruptedException exc) { }

    // copy DRT into MapTo and black out icons of illegal DisplayRealTypes
    MapTo = parent.createImage(280, 200);
    Graphics gr = MapTo.getGraphics();
    gr.drawImage(DRT, 0, 0, this);
    if (!AllowAlpha) {
      eraseBox(5, 3, gr);
      eraseBox(6, 3, gr);
    }
    if (!Allow3D) {
      eraseBox(2, 0, gr);
      eraseBox(2, 1, gr);
      eraseBox(0, 2, gr);
      eraseBox(2, 3, gr);
      eraseBox(2, 4, gr);
    }
    gr.dispose();

    // wait for MapTo to finish loading
    mtracker.addImage(MapTo, 1);
    try {
      mtracker.waitForID(1);
    }
    catch (InterruptedException exc) { }

    // set up "map to" canvas
    DisplayCanvas = new JComponent() {
      public void paint(Graphics g2) {
        g2.drawImage(MapTo, 0, 0, this);
        int ind = MathList.getSelectedIndex();
        if (ind >= 0) {
          for (int col=0; col<7; col++) {
            for (int row=0; row<5; row++) {
              if (Maps[ind][col][row]) highlightBox(col, row, g2);
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
    b2Panel.add(Box.createRigidArea(new Dimension(5, 0)));

    // set up detect button
    JButton detect = new JButton("Detect");
    detect.setAlignmentX(JButton.CENTER_ALIGNMENT);
    detect.setToolTipText("Automatically identify some good mappings");
    detect.setActionCommand("detect");
    detect.addActionListener(this);
    b2Panel.add(detect);
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
    JLabel l4 = new JLabel("Current maps:");
    l4.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    rsPanel.add(l4);
    rsPanel.add(CurrentMapsView);
  }

  /** parse an array of prettyStrings to find out some information
      and eliminate duplicate RealTypes */
  private int[] extraPretty(String[] pStr, Vector[] v, int[] duplCount) {
    int numStrings = pStr.length;
    StrWidth = new int[numStrings];
    StrHeight = new int[numStrings];
    int[] numScalars = new int[numStrings];
    int[] numLines = new int[numStrings];
    int[] mod = new int[numStrings];
    int num = 0;
    for (int j=0; j<numStrings; j++) {
      numScalars[j] = v[j].size();
      mod[j] = num;
      num += numScalars[j] - duplCount[j];
    }
    Scalars = new String[num];
    MathTypes = new RealType[num];
    FontMetrics fm = getFontMetrics(Mono);
    ScX = new int[num][];
    ScY = new int[num][];
    ScW = new int[num];
    ScH = fm.getHeight();
    ScB = new int[num];

    for (int j=0; j<numStrings; j++) {
      // extract number of lines from prettyString
      numLines[j] = 1;
      int len = pStr[j].length();
      for (int i=0; i<len; i++) {
        if (pStr[j].charAt(i) == '\n') numLines[j]++;
      }
      StrWidth[j] = 1;
      StrHeight[j] = (ScH+2)*numLines[j]+10;

      if (numScalars[j] > 0) {
        // define new array of strings and other stuff
        int lineNum = 0;
        int lastLine = 0;
        int[] x = new int[numScalars[j]];
        int[] y = new int[numScalars[j]];
        int[] w = new int[numScalars[j]];
        int scalarNum = 0;
        String scalar = ((RealType) v[j].elementAt(0)).getName();
        int scalarLen = scalar.length();

        // extract info from prettyString
        int q = 0;
        while (q <= len) {
          // fill in array of strings
          if (q == len || pStr[j].charAt(q) == '\n') {
            String lnStr = pStr[j].substring(lastLine, q);
            lnStr = lnStr + ' '; // FontMetrics bug work-around
            int lnStrLen = fm.stringWidth(lnStr);
            if (lnStrLen > StrWidth[j]) StrWidth[j] = lnStrLen;
            lineNum++;
            lastLine = q+1;
          }

          // fill in scalar info
          if (q + scalarLen <= len &&
              pStr[j].substring(q, q + scalarLen).equals(scalar)) {
            x[scalarNum] = fm.stringWidth(pStr[j].substring(lastLine, q));
            y[scalarNum] = lineNum;
            w[scalarNum] = fm.stringWidth(scalar);
            scalarNum++;
            q += scalarLen;
            if (scalarNum < numScalars[j]) {
              scalar = ((RealType) v[j].elementAt(scalarNum)).getName();
              scalarLen = scalar.length();
            }
          }
          else q++;
        }

        // remove duplicates from all data structures
        int[] trans = new int[numScalars[j]];
        int[] numXY = new int[numScalars[j]];
        for (int i=0; i<numScalars[j]; i++) numXY[i] = 0;
        int modifier = 0;
        for (int i=0; i<numScalars[j]; i++) {
          Object o = v[j].elementAt(i);
          int ind = i;
          // find FIRST index in v[j] occupied by o
          for (int k=i-1; k>=0; k--) {
            if (v[j].elementAt(k) == o) ind = k;
          }
          if (i == ind) trans[i] = ind - modifier;
          else {
            trans[i] = trans[ind];
            modifier++;
          }
          numXY[ind]++;
        }
        int[] z = new int[num];
        for (int i=0; i<numScalars[j]; i++) {
          int t = trans[i] + mod[j];
          if (numXY[i] > 0) {
            MathTypes[t] = (RealType) v[j].elementAt(i);
            Scalars[t] = MathTypes[t].getName();
            int u = numXY[i];
            ScX[t] = new int[u];
            ScY[t] = new int[u];
            ScX[t][u-1] = x[i];
            ScY[t][u-1] = y[i];
            ScW[t] = w[i];
            ScB[t] = j;
            z[t] = u - 2;
          }
          else {
            int zt = z[t]--;
            ScX[t][zt] = x[i];
            ScY[t][zt] = y[i];
          }
        }
      }
    }
    return numLines;
  }

  /** recursive quick-sort routine for alphabetizing scalars */
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
        i = ScB[lo];
        ScB[lo] = ScB[hi];
        ScB[hi] = i;
        lo++;
        hi--;
      }
    }
    if (lo0 < hi) sort(lo0, hi);
    if (lo < hi0) sort(lo, hi0);
  }

  /** clear a box in the &quot;map to&quot; canvas */
  void eraseBox(int col, int row, Graphics g) {
    int x = 40*col;
    int y = 40*row;
    g.setColor(Color.black);
    for (int i=0; i<40; i+=2) {
      g.drawLine(x, y+i, x+i, y);
      g.drawLine(x+i, y+38, x+38, y+i);
    }
  }

  /** highlight a box in the &quot;map to&quot; canvas */
  void highlightBox(int col, int row, Graphics g) {
    int x = 40*col;
    int y = 40*row;
    final int n1 = 11;
    final int n2 = 29;
    g.setColor(Color.blue);
    g.drawRect(x, y, 39, 39);
    g.drawLine(x, y+n1, x+n1, y);
    g.drawLine(x, y+n2, x+n2, y);
    g.drawLine(x+n1, y+39, x+39, y+n1);
    g.drawLine(x+n2, y+39, x+39, y+n2);
    g.drawLine(x+n2, y, x+39, y+n1);
    g.drawLine(x+n1, y, x+39, y+n2);
    g.drawLine(x, y+n1, x+n2, y+39);
    g.drawLine(x, y+n2, x+n1, y+39);
  }

  /** clear all maps from the current mappings list */
  private void clearAll() {
    CurrentMaps.clearSelection(); // work-around for nasty swing bug
    CurMaps.removeAllElements();
    for (int i=0; i<CurMapLabel.length; i++) {
      for (int j=0; j<7; j++) {
        for (int k=0; k<5; k++) Maps[i][j][k] = false;
      }
    }
  }

  /** handle button press events */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    synchronized (Lock) {
      if (cmd.equals("all")) { // clear all
        if (CurMaps.getSize() > 0) {
          clearAll();
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

      else if (cmd.equals("detect")) {
        ScalarMap[] maps = type.guessMaps(Allow3D);
        if (CurMaps.getSize() > 0) clearAll();
        if (maps != null) {
          for (int i=0; i<MathTypes.length; i++) {
            for (int j=0; j<7; j++) {
              for (int k=0; k<5; k++) {
                for (int m=0; m<maps.length; m++) {
                  if (maps[m].getScalar() == MathTypes[i] &&
                      maps[m].getDisplayScalar() == MapTypes[j][k]) {
                    Maps[i][j][k] = true;
                    CurMaps.addElement(CurMapLabel[i][j][k]);
                  }
                }
              }
            }
          }
        }
        // update components
        Graphics g = DisplayCanvas.getGraphics();
        DisplayCanvas.paint(g);
        g.dispose();
        CurrentMaps.repaint();
        CurrentMapsView.validate();
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
                  ScalarMaps[s++] = new ScalarMap(MathTypes[i],
                                                  MapTypes[j][k]);
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

  /** handle list selection change events */
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
          if (ScB[i] == 0) MathCanvas.scrollRectToVisible(r);
          else if (ScB[i] == 1) CoordCanvas.scrollRectToVisible(r);
          MathCanvas.repaint();
          if (CoordRefs) CoordCanvas.repaint();
        }
      }
    }
  }

  /** handle mouse clicks in the MathType window and
      &quot;map to&quot; canvas */
  public void mousePressed(MouseEvent e) {
    synchronized (Lock) {
      Component c = e.getComponent();
      if (c == MathCanvas) {
        Point p = e.getPoint();
        for (int i=0; i<Scalars.length; i++) {
          if (ScB[i] == 0) {
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
      }
      else if (c == DisplayCanvas) {
        int col = e.getX() / 40;
        int row = e.getY() / 40;
        int ind = MathList.getSelectedIndex();
        if (ind >= 0 && row >= 0 && col >= 0 && row < 5 && col < 7 &&
            (AllowAlpha || ((col != 5 || row != 3) &&
                            (col != 6 || row != 3))) &&
               (Allow3D || ((col != 2 || row != 0) &&
                            (col != 2 || row != 1) &&
                            (col != 0 || row != 2) &&
                            (col != 2 || row != 3) &&
                            (col != 2 || row != 4)))) {
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
      else if (c == CoordCanvas) {
        Point p = e.getPoint();
        for (int i=0; i<Scalars.length; i++) {
          if (ScB[i] == 1) {
            for (int j=0; j<ScX[i].length; j++) {
              Rectangle r = new Rectangle(ScX[i][j]+5, (ScH+2)*ScY[i][j]+6,
                                          ScW[i], ScH);
              if (r.contains(p)) {
                MathList.setSelectedIndex(i);
                MathList.ensureIndexIsVisible(i);
                CoordCanvas.scrollRectToVisible(r);
                return;
              }
            }
          }
        }
      }
    }
  }

  /** unused MouseListener method */
  public void mouseEntered(MouseEvent e) { }

  /** unused MouseListener method */
  public void mouseExited(MouseEvent e) { }
  
  /** unused MouseListener method */
  public void mouseClicked(MouseEvent e) { }

  /** unused MouseListener method */
  public void mouseReleased(MouseEvent e) { }

}

