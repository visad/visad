//
// MappingDialog.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.util.*;

/**
 * MappingDialog is a dialog that lets the user create ScalarMaps.
 */
public class MappingDialog extends JDialog
  implements ActionListener, ListSelectionListener, MouseListener
{
  /**
   * Flag whether user hit Done or Cancel button.
   */
  private boolean Confirm = false;

  /**
   * ScalarMaps selected by the user.
   */
  private ScalarMap[] ScalarMaps;


  // -- GUI components --

  private JComponent MathCanvas;
  private JScrollPane MathCanvasView;
  private JComponent CoordCanvas;
  private JScrollPane CoordCanvasView;
  private boolean CoordRefs;
  private JList MathList;
  private JComponent DisplayCanvas;
  private DefaultListModel CurMaps;
  private JList CurrentMaps;
  private JScrollPane CurrentMapsView;
  private JLabel description;


  // -- State info --

  /**
   * Array of ScalarTypes.
   */
  private ScalarType[] MathTypes;

  /**
   * Array of ScalarType names.
   */
  private String[] Scalars;

  /**
   * Array of ScalarType widths.
   */
  private int[] ScW;

  /**
   * Vector of MDTuples indicating ScalarType locations.
   */
  private Vector[] ScP;

  /**
   * ScalarType height.
   */
  private int ScH;

  /**
   * Width and height of MathType string.
   */
  private Dimension StrSize;

  /**
   * Width and height of CoordinateSystem string.
   */
  private Dimension CoordSize;

  /**
   * Flags marking whether each possible ScalarMap has been assigned.
   */
  private boolean[][][] Maps;

  /**
   * String representation of each possible ScalarMap.
   */
  private String[][][] CurMapLabel;

  /**
   * 11 pt monospaced font.
   */
  private static final Font Mono = new Font("Monospaced", Font.PLAIN, 11);

  /**
   * Names of system intrinsic DisplayRealTypes.
   */
  private static final String[][] MapNames = {
    {"X Axis", "Y Axis", "Z Axis", "X Offset", "Y Offset", "Z Offset"},
    {"Latitude", "Longitude", "Radius",
      "Cyl Radius", "Cyl Azimuth", "Cyl Z Axis"},
    {"Flow1 X", "Flow1 Y", "Flow1 Z", "Flow2 X", "Flow2 Y", "Flow2 Z"},
    {"Flow1 Elevation", "Flow1 Azimuth", "Flow1 Radial",
      "Flow2 Elevation", "Flow2 Azimuth", "Flow2 Radial"},
    {"Red", "Green", "Blue", "RGB", "RGBA", "Alpha"},
    {"Cyan", "Magenta", "Yellow", "CMY", "Animation", "Iso-contour"},
    {"Hue", "Saturation", "Value", "HSV", "Select Value", "Select Range"},
    {"", "Text", "", "", "Shape", ""}
  };

  /**
   * List of system intrinsic DisplayRealTypes.
   */
  private static final DisplayRealType[][] MapTypes = {
    {Display.XAxis, Display.YAxis, Display.ZAxis,
      Display.XAxisOffset, Display.YAxisOffset, Display.ZAxisOffset},
    {Display.Latitude, Display.Longitude, Display.Radius,
      Display.CylRadius, Display.CylAzimuth, Display.CylZAxis},
    {Display.Flow1X, Display.Flow1Y, Display.Flow1Z,
      Display.Flow2X, Display.Flow2Y, Display.Flow2Z},
    {Display.Flow1Elevation, Display.Flow1Azimuth, Display.Flow1Radial,
      Display.Flow2Elevation, Display.Flow2Azimuth, Display.Flow2Radial},
    {Display.Red, Display.Green, Display.Blue,
      Display.RGB, Display.RGBA, Display.Alpha},
    {Display.Cyan, Display.Magenta, Display.Yellow,
      Display.CMY, Display.Animation, Display.IsoContour},
    {Display.Hue, Display.Saturation, Display.Value,
      Display.HSV, Display.SelectValue, Display.SelectRange},
    {null, Display.Text, null, null, Display.Shape, null}
  };

  /**
   * Indices into MapTypes of alpha-related DisplayRealTypes.
   */
  private static final Point[] AlphaMaps = {
    new Point(4, 4), // RGBA
    new Point(4, 5)  // Alpha
  };

  /**
   * Indices into MapTypes of 3D-related DisplayRealTypes.
   */
  private static final Point[] ThreeDMaps = {
    new Point(0, 2), // ZAxis
    new Point(0, 5), // ZAxisOffset
    new Point(1, 0), // Latitude
    new Point(1, 5), // CylZAxis
    new Point(2, 2), // Flow1Z
    new Point(2, 5), // Flow2Z
    new Point(3, 0), // Flow1Elevation
    new Point(3, 3)  // Flow2Elevation
  };
  
  /**
   * Width of mapping arrays.
   */
  private static final int MapWidth = MapTypes[0].length;

  /**
   * Height of mapping arrays.
   */
  private static final int MapHeight = MapTypes.length;

  /**
   * display.gif image.
   */
  private static Image DRT = null;

  /**
   * Whether DRT image has been initialized.
   */
  private static boolean Inited = false;

  /**
   * Pre-loads the display.gif file, so it's ready
   * when mapping dialog is requested.
   */
  public static void initDialog() {
    if (DRT == null) {
      URL url = MappingDialog.class.getResource("display.gif");
      DRT = Toolkit.getDefaultToolkit().getImage(url);
    }
    Inited = true;
  }

  /**
   * Returns a human-readable list of CoordinateSystem dependencies.
   */
  private static String prettyCoordSys(MathType type) {
    String s = "";
    if (type instanceof FunctionType) s = s + pcsFunction((FunctionType) type);
    else if (type instanceof SetType) s = s + pcsSet((SetType) type);
    else if (type instanceof TupleType) s = s + pcsTuple((TupleType) type);
    return s;
  }

  /**
   * prettyCoordSys helper method.
   */
  private static String pcsFunction(FunctionType mathType) {
    String s = "";

    // extract domain
    RealTupleType domain = mathType.getDomain();
    s = s + pcsTuple((TupleType) domain);

    // extract range
    MathType range = mathType.getRange();
    s = s + prettyCoordSys(range);

    return s;
  }

  /**
   * prettyCoordSys helper method.
   */
  private static String pcsSet(SetType mathType) {
    // extract domain
    RealTupleType domain = mathType.getDomain();
    return pcsTuple((TupleType) domain);
  }

  /**
   * prettyCoordSys helper method.
   */
  private static String pcsTuple(TupleType mathType) {
    String s = "";
    if (mathType instanceof RealTupleType) {
      RealTupleType rtt = (RealTupleType) mathType;
      CoordinateSystem cs = rtt.getCoordinateSystem();
      if (cs != null) {
        RealTupleType ref = cs.getReference();
        if (ref != null) {
          s = s + rtt.prettyString() + " ==> " + ref.prettyString() + "\n";
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
        if (cType != null) s = s + prettyCoordSys(cType);
      }
    }
    return s;
  }

  /**
   * Converts a string index into a graphical position.
   */
  private static Point indexToPoint(int ndx, int[] lines, int w, int h) {
    int i = 0;
    while (i < lines.length && lines[i] <= ndx) i++;
    int surplus = ndx - lines[i - 1];
    return new Point(w * surplus + 5, (h + 2) * (i - 1) + 6);
  }

  /**
   * For synchronization.
   */
  private Object Lock = new Object();

  /**
   * Flags marking whether each DisplayRealType is illegal.
   */
  private boolean[][] Illegal = new boolean[MapHeight][MapWidth];

  /**
   * This MappingDialog's copy of DRT with certain
   * DisplayRealTypes blacked out as necessary.
   */
  private Image MapTo;

  /**
   * The MathType with which this mapping dialog works.
   */
  private MathType[] Types = null;

  /**
   * Whether this mapping dialog allows mappings to Alpha and RGBA.
   */
  private boolean AllowAlpha;

  /**
   * Whether this mapping dialog allows mappings to Z-Axis,
   * Latitude, and Z-Offset.
   */
  private boolean Allow3D;

  /**
   * Constructs a MappingDialog from a single Data object.
   */
  public MappingDialog(Frame parent, Data data, ScalarMap[] startMaps,
    boolean allowAlpha, boolean allow3D)
  {
    this(parent, new Data[] {data}, startMaps, allowAlpha, allow3D);
  }

  /**
   * Constructs a MappingDialog from multiple Data objects.
   */
  public MappingDialog(Frame parent, Data[] data, ScalarMap[] startMaps,
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

    // parse MathTypes
    int numData = data.length;
    Types = new MathType[numData];
    String typeString = "";
    String coordString = "";
    for (int i=0; i<numData; i++) {
      try {
        Data d = data[i];
        if (d == null) Types[i] = null;
        else {
          Types[i] = d.getType();
          typeString = typeString + Types[i].prettyString() + "\n";
          coordString = coordString + prettyCoordSys(Types[i]);
        }
      }
      catch (VisADException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
      catch (RemoteException exc) {
        if (BasicSSCell.DEBUG) exc.printStackTrace();
      }
    }
    Vector v = new Vector();
    int dupl = 0;
    try {
      dupl = DataUtility.getScalarTypes(data, v, true, true);
    }
    catch (VisADException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }
    catch (RemoteException exc) {
      if (BasicSSCell.DEBUG) exc.printStackTrace();
    }

    // determine width and height of MathType string
    FontMetrics fm = getFontMetrics(Mono);
    ScH = fm.getHeight();
    int charWidth = fm.stringWidth(" ");
    int width = 1;
    int pos = -1;
    int numLines = 0;
    do {
      int nextPos = typeString.indexOf("\n", pos + 1);
      if (nextPos < 0) nextPos = typeString.length();
      int w = charWidth * (nextPos - pos);
      if (w > width) width = w;
      pos = nextPos;
      numLines++;
    }
    while (pos < typeString.length());
    StrSize = new Dimension(width, (ScH + 2) * numLines + 10);

    // get starting line position for each line of MathType string
    int[] mathLines = new int[numLines];
    pos = -1;
    for (int i=0; i<numLines; i++) {
      mathLines[i] = pos + 1;
      pos = typeString.indexOf("\n", pos + 1);
    }

    // determine width and height of CoordinateSystem string
    width = 1;
    pos = -1;
    numLines = 0;
    do {
      int nextPos = coordString.indexOf("\n", pos + 1);
      int w = charWidth * (nextPos - pos);
      if (w > width) width = w;
      pos = nextPos;
      numLines++;
    }
    while (pos < coordString.length() - 1);
    CoordSize = new Dimension(width, (ScH + 2) * numLines + 10);

    // get starting line position for each line of CoordinateSystem string
    int[] coordLines = new int[numLines];
    pos = -1;
    for (int i=0; i<numLines; i++) {
      coordLines[i] = pos + 1;
      pos = coordString.indexOf("\n", pos + 1);
    }

    // extract information about ScalarType locations within MathType strings
    int len = v.size();
    int unique = len - dupl;
    MathTypes = new ScalarType[unique];
    Scalars = new String[unique];
    ScW = new int[unique];
    ScP = new Vector[unique];
    Hashtable nameToIndex = new Hashtable();
    int s = 0;
    boolean inCoord = false;
    pos = -1;
    for (int i=0; i<len; i++) {
      ScalarType type = (ScalarType) v.elementAt(i);
      String name = type.getName();
      if (v.indexOf(type) == i) {
        // first occurrence of this ScalarType
        MathTypes[s] = type;
        Scalars[s] = name;
        ScW[s] = charWidth * name.length();
        ScP[s] = new Vector();
        nameToIndex.put(name, new Integer(s));
        s++;
      }
      // determine position of ScalarType and add to position vector
      int index = ((Integer) nameToIndex.get(name)).intValue();
      MDTuple tuple = null;
      if (!inCoord) {
        pos = typeString.indexOf(name, pos + 1);
        if (pos == -1) inCoord = true;
        else {
          Point p = indexToPoint(pos, mathLines, charWidth, ScH);
          tuple = new MDTuple(p.x, p.y, false);
        }
      }
      if (inCoord) {
        pos = coordString.indexOf(name, pos + 1);
        Point p = indexToPoint(pos, coordLines, charWidth, ScH);
        tuple = new MDTuple(p.x, p.y, true);
      }
      ScP[index].add(tuple);
    }

    // alphabetize Scalars list
    sort(0, unique - 1);

    // mark whether there are CoordinateSystem references
    CoordRefs = !coordString.equals("");

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

    // draw the pretty-print MathType sequence to an Image (slow!)
    final BufferedImage ppImg = new BufferedImage(
      StrSize.width, StrSize.height, BufferedImage.TYPE_INT_RGB);
    Graphics g = ppImg.getGraphics();
    g.setFont(Mono);
    g.setColor(Color.white);
    g.fillRect(0, 0, StrSize.width, StrSize.height);
    g.setColor(Color.black);
    for (int i=0; i<mathLines.length; i++) {
      int start = mathLines[i];
      int end = (i < mathLines.length - 1 ?
        mathLines[i + 1] - 1 : typeString.length());
      String line = typeString.substring(start, end);
      g.drawString(line, 5, (ScH + 2) * (i + 1));
    }
    g.dispose();

    // set up MathType canvas
    MathCanvas = new JComponent() {
      public void paint(Graphics g2) {
        // draw pretty-print MathType using its Image
        g2.drawImage(ppImg, 0, 0, this);
        int ndx = MathList.getSelectedIndex();
        if (ndx >= 0) {
          for (int i=0; i<ScP[ndx].size(); i++) {
            MDTuple tuple = (MDTuple) ScP[ndx].elementAt(i);
            if (!tuple.b) {
              g2.setFont(Mono);
              String ss = Scalars[ndx];
              int x = tuple.x;
              int y = tuple.y;
              g2.setColor(Color.blue);
              g2.fillRect(tuple.x, tuple.y, ScW[ndx], ScH);
              g2.setColor(Color.white);
              g2.drawString(ss, tuple.x, tuple.y + ScH - 4);
            }
          }
        }
        // work-around for JDK 1.3 bug
        Dimension d = MathCanvas.getSize();
        g2.setColor(Color.white);
        g2.fillRect(StrSize.width, 0, d.width, d.height);
        g2.fillRect(0, StrSize.height, StrSize.width, d.height);
      }
    };
    MathCanvas.setMinimumSize(StrSize);
    MathCanvas.setPreferredSize(StrSize);
    MathCanvas.addMouseListener(this);
    MathCanvas.setBackground(Color.white);

    // set up MathCanvas's ScrollPane
    MathCanvasView = new JScrollPane(MathCanvas);
    MathCanvasView.setMinimumSize(new Dimension(0, 0));
    int prefMCHeight = StrSize.height + 10;
    if (prefMCHeight < 70) prefMCHeight = 70;
    int maxMCHeight = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
    if (prefMCHeight > maxMCHeight) prefMCHeight = maxMCHeight;
    MathCanvasView.setPreferredSize(new Dimension(0, prefMCHeight));
    MathCanvasView.setBackground(Color.white);
    JScrollBar horiz = MathCanvasView.getHorizontalScrollBar();
    JScrollBar verti = MathCanvasView.getVerticalScrollBar();
    horiz.setBlockIncrement(5 * ScH + 10);
    horiz.setUnitIncrement(ScH+2);
    verti.setBlockIncrement(5 * ScH + 10);
    verti.setUnitIncrement(ScH+2);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    JPanel whitePanel = new JPanel();
    whitePanel.setBackground(Color.white);
    whitePanel.setLayout(new BoxLayout(whitePanel, BoxLayout.X_AXIS));
    whitePanel.add(MathCanvasView);
    topPanel.add(whitePanel);
    topPanel.add(Box.createRigidArea(new Dimension(5, 0)));

    // set up description label
    description = new JLabel(" ");
    JPanel descPanel = new JPanel();
    descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.X_AXIS));
    contentPane.add(descPanel);
    contentPane.add(Box.createRigidArea(new Dimension(0, 15)));
    descPanel.add(description);

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
      final BufferedImage csImg = new BufferedImage(
        CoordSize.width, CoordSize.height, BufferedImage.TYPE_INT_RGB);
      g = csImg.getGraphics();
      g.setFont(Mono);
      g.setColor(Color.white);
      g.fillRect(0, 0, CoordSize.width, CoordSize.height);
      g.setColor(Color.black);
      for (int i=0; i<coordLines.length; i++) {
        int start = coordLines[i];
        int end = (i < coordLines.length - 1 ?
          coordLines[i + 1] - 1 : coordString.length() - 1);
        String line = coordString.substring(start, end);
        g.drawString(line, 5, (ScH + 2) * (i + 1));
      }
      g.dispose();

      // set up CoordinateSystem references canvas
      CoordCanvas = new JComponent() {
        public void paint(Graphics g2) {
          // draw pretty-print CoordinateSystem reference list using its Image
          g2.drawImage(csImg, 0, 0, this);
          int ndx = MathList.getSelectedIndex();
          if (ndx >= 0) {
            for (int i=0; i<ScP[ndx].size(); i++) {
              MDTuple tuple = (MDTuple) ScP[ndx].elementAt(i);
              if (tuple.b) {
                g2.setFont(Mono);
                String ss = Scalars[ndx];
                int x = tuple.x;
                int y = tuple.y;
                g2.setColor(Color.blue);
                g2.fillRect(tuple.x, tuple.y, ScW[ndx], ScH);
                g2.setColor(Color.white);
                g2.drawString(ss, tuple.x, tuple.y + ScH - 4);
              }
            }
          }
          // work-around for JDK 1.3 bug
          Dimension d = CoordCanvas.getSize();
          g2.setColor(Color.white);
          g2.fillRect(CoordSize.width, 0, d.width, d.height);
          g2.fillRect(0, CoordSize.height, CoordSize.width, d.height);
        }
      };
      CoordCanvas.setMinimumSize(CoordSize);
      CoordCanvas.setPreferredSize(CoordSize);
      CoordCanvas.addMouseListener(this);
      CoordCanvas.setBackground(Color.white);

      // set up CoordCanvas's ScrollPane
      CoordCanvasView = new JScrollPane(CoordCanvas);
      CoordCanvasView.setMinimumSize(new Dimension(0, 0));
      CoordCanvasView.setPreferredSize(new Dimension(0, CoordSize.height));
      int prefCCHeight = CoordSize.height + 10;
      if (prefCCHeight < 70) prefCCHeight = 70;
      int maxCCHeight = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
      if (prefCCHeight > maxCCHeight) prefCCHeight = maxCCHeight;
      CoordCanvasView.setPreferredSize(new Dimension(0, prefCCHeight));
      CoordCanvasView.setBackground(Color.white);
      horiz = CoordCanvasView.getHorizontalScrollBar();
      verti = CoordCanvasView.getVerticalScrollBar();
      horiz.setBlockIncrement(5 * ScH + 10);
      horiz.setUnitIncrement(ScH + 2);
      verti.setBlockIncrement(5 * ScH + 10);
      verti.setUnitIncrement(ScH + 2);
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
    CurMaps.ensureCapacity(num * MapWidth * MapHeight);
    CurrentMaps = new JList(CurMaps);

    // set up "map from" list
    Maps = new boolean[num][MapHeight][MapWidth];
    CurMapLabel = new String[num][MapHeight][MapWidth];
    for (int i=0; i<num; i++) {
      for (int j=0; j<MapHeight; j++) {
        for (int k=0; k<MapWidth; k++) {
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
    JScrollPane mathListView = new JScrollPane(MathList) {
      public Dimension getMinimumSize() {
        return new Dimension(0, 40 * MapHeight + 64);
      }
      public Dimension getPreferredSize() {
        return new Dimension(200, 40 * MapHeight + 64);
      }
      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 40 * MapHeight + 64);
      }
    };
    JLabel l2 = new JLabel("Map from:");
    l2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    lsPanel.add(l2);
    lsPanel.add(mathListView);
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

    // flag illegal DisplayRealTypes
    for (int i=0; i<MapHeight; i++) {
      for (int j=0; j<MapWidth; j++) Illegal[i][j] = MapTypes[i][j] == null;
    }
    if (!AllowAlpha) {
      for (int i=0; i<AlphaMaps.length; i++) {
        Point p = AlphaMaps[i];
        Illegal[p.x][p.y] = true;
      }
    }
    if (!Allow3D) {
      for (int i=0; i<ThreeDMaps.length; i++) {
        Point p = ThreeDMaps[i];
        Illegal[p.x][p.y] = true;
      }
    }

    // copy DRT into MapTo and black out icons of illegal DisplayRealTypes
    MapTo = new BufferedImage(40 * MapWidth, 40 * MapHeight,
      BufferedImage.TYPE_INT_RGB);
    Graphics gr = MapTo.getGraphics();
    gr.drawImage(DRT, 0, 0, this);
    for (int i=0; i<MapHeight; i++) {
      for (int j=0; j<MapWidth; j++) {
        if (Illegal[i][j]) eraseBox(j, i, gr);
      }
    }
    gr.dispose();

    // set up "map to" canvas
    DisplayCanvas = new JComponent() {
      public void paint(Graphics g2) {
        g2.drawImage(MapTo, 0, 0, this);
        int ndx = MathList.getSelectedIndex();
        if (ndx >= 0) {
          for (int col=0; col<MapWidth; col++) {
            for (int row=0; row<MapHeight; row++) {
              if (Maps[ndx][row][col]) highlightBox(col, row, g2);
            }
          }
        }
      }

      public Dimension getMinimumSize() {
        return new Dimension(40 * MapWidth, 40 * MapHeight);
      }

      public Dimension getPreferredSize() {
        return new Dimension(40 * MapWidth, 40 * MapHeight);
      }

      public Dimension getMaximumSize() {
        return new Dimension(40 * MapWidth, 40 * MapHeight);
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
        return new Dimension(0, 40 * MapHeight + 64);
      }

      public Dimension getPreferredSize() {
        return new Dimension(200, 40 * MapHeight + 64);
      }

      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 40 * MapHeight + 64);
      }
    };
    JLabel l4 = new JLabel("Current maps:");
    l4.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    rsPanel.add(l4);
    rsPanel.add(CurrentMapsView);
  }

  /**
   * Ensure mapping dialog is not larger than the screen size.
   */
  public Dimension getPreferredSize() {
    Dimension max = super.getPreferredSize();

    // take Windows Start bar into account
    String os = System.getProperty("os.name");
    final int pad = os.startsWith("Windows") ? 60 : 20;

    // ensure dialog size does not exceed screen size
    int w = max.width;
    int h = max.height;
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    if (w > screen.width - pad) w = screen.width - pad;
    if (h > screen.height - pad) h = screen.height - pad;
    return new Dimension(w, h);
  }

  /**
   * Displays the dialog in the center of the screen.
   */
  public void display() {
    pack();
    Util.centerWindow(this);
    setVisible(true);
  }

  /**
   * Gets the mappings selected by the user.
   */
  public ScalarMap[] getMaps() {
    return ScalarMaps;
  }

  /**
   * Gets whether the user pressed the Ok button.
   */
  public boolean okPressed() {
    return Confirm;
  }

  /**
   * Recursive quick-sort routine for alphabetizing scalars.
   */
  private void sort(int lo0, int hi0) {
    if (hi0 < lo0) return;
    int lo = lo0;
    int hi = hi0;
    String mid = Scalars[(lo0 + hi0) / 2].toLowerCase();
    while (lo <= hi) {
      while (lo < hi0 && Scalars[lo].toLowerCase().compareTo(mid) < 0) ++lo;
      while (hi > lo0 && Scalars[hi].toLowerCase().compareTo(mid) > 0) --hi;
      if (lo <= hi) {
        // MathTypes[lo] <-> MathTypes[hi]
        ScalarType st = MathTypes[lo];
        MathTypes[lo] = MathTypes[hi];
        MathTypes[hi] = st;
        // Scalars[lo] <-> Scalars[hi]
        String s = Scalars[lo];
        Scalars[lo] = Scalars[hi];
        Scalars[hi] = s;
        // ScW[lo] <-> ScW[hi]
        int i = ScW[lo];
        ScW[lo] = ScW[hi];
        ScW[hi] = i;
        // ScP[lo] <-> ScP[hi]
        Vector v = ScP[lo];
        ScP[lo] = ScP[hi];
        ScP[hi] = v;

        lo++;
        hi--;
      }
    }
    if (lo0 < hi) sort(lo0, hi);
    if (lo < hi0) sort(lo, hi0);
  }

  /**
   * Clears a box in the &quot;map to&quot; canvas.
   */
  void eraseBox(int col, int row, Graphics g) {
    int x = 40 * col;
    int y = 40 * row;
    g.setColor(Color.black);
    for (int i=0; i<40; i+=2) {
      g.drawLine(x, y + i, x + i, y);
      g.drawLine(x + i, y + 38, x + 38, y + i);
    }
  }

  /**
   * Highlights a box in the &quot;map to&quot; canvas.
   */
  void highlightBox(int col, int row, Graphics g) {
    int x = 40 * col;
    int y = 40 * row;
    final int n1 = 11;
    final int n2 = 29;
    g.setColor(Color.blue);
    g.drawRect(x, y, 39, 39);
    g.drawLine(x, y + n1, x + n1, y);
    g.drawLine(x, y + n2, x + n2, y);
    g.drawLine(x + n1, y + 39, x + 39, y + n1);
    g.drawLine(x + n2, y + 39, x + 39, y + n2);
    g.drawLine(x + n2, y, x + 39, y + n1);
    g.drawLine(x + n1, y, x + 39, y + n2);
    g.drawLine(x, y + n1, x + n2, y + 39);
    g.drawLine(x, y + n2, x + n1, y + 39);
  }

  /**
   * Clears all maps from the current mappings list.
   */
  private void clearAll() {
    CurrentMaps.clearSelection(); // work-around for nasty swing bug
    CurMaps.removeAllElements();
    for (int i=0; i<CurMapLabel.length; i++) {
      for (int j=0; j<MapHeight; j++) {
        for (int k=0; k<MapWidth; k++) Maps[i][j][k] = false;
      }
    }
  }

  /**
   * Updates the description label to match the currently selected ScalarType.
   */
  private void updateDescriptionLabel(int i) {
    ScalarType st = MathTypes[i];
    String desc = "     " + Scalars[i];
    if (st instanceof RealType) {
      RealType r = (RealType) st;
      Unit unit = r.getDefaultUnit();
      Set set = r.getDefaultSet();
      String u = unit == null ? "none" : unit.toString();
      String s;
      if (set == null) s = "none";
      else {
        String setType = set.getClass().getName();
        int index = setType.lastIndexOf(".");
        if (index >= 0) setType = setType.substring(index + 1);
        int dim = set.getDimension();
        s = setType + "(" + dim + ")";
      }
      desc = desc + ": Unit=" + u + "; Set=" + s;
    }
    else {
      desc = desc + " (text)";
    }
    description.setText(desc);
  }

  /**
   * Handles button press events.
   */
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
        int[] ndx = CurrentMaps.getSelectedIndices();
        int len = ndx.length;
        for (int x=len-1; x>=0; x--) {
          String s = (String) CurMaps.getElementAt(ndx[x]);
          boolean looking = true;
          for (int i=0; i<CurMapLabel.length && looking; i++) {
            for (int j=0; j<MapHeight && looking; j++) {
              for (int k=0; k<MapWidth && looking; k++) {
                if (CurMapLabel[i][j][k] == s) {
                  Maps[i][j][k] = false;
                  looking = false;
                }
              }
            }
          }
          // take map off list
          CurMaps.removeElementAt(ndx[x]);
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
        ScalarMap[] maps = DataUtility.guessMaps(Types, Allow3D);
        if (CurMaps.getSize() > 0) clearAll();
        if (maps != null) {
          for (int i=0; i<MathTypes.length; i++) {
            for (int j=0; j<MapHeight; j++) {
              for (int k=0; k<MapWidth; k++) {
                for (int m=0; m<maps.length; m++) {
                  if (maps[m].getScalar() == MathTypes[i] &&
                    maps[m].getDisplayScalar() == MapTypes[j][k])
                  {
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
          for (int j=0; j<MapHeight; j++) {
            for (int k=0; k<MapWidth; k++) {
              if (Maps[i][j][k]) {
                try {
                  ScalarMaps[s++] =
                    new ScalarMap(MathTypes[i], MapTypes[j][k]);
                }
                catch (VisADException exc) {
                  okay = false;
                  JOptionPane.showMessageDialog(this, "The mapping (" +
                    Scalars[i] + " -> " + MapNames[j][k] + ") is not valid.",
                    "Illegal mapping", JOptionPane.ERROR_MESSAGE);
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

  /**
   * Handles list selection change events.
   */
  public void valueChanged(ListSelectionEvent e) {
    synchronized (Lock) {
      if (!e.getValueIsAdjusting()) {
        if ((JList) e.getSource() == MathList) {
          Graphics g = DisplayCanvas.getGraphics();
          DisplayCanvas.paint(g);
          g.dispose();
          int i = MathList.getSelectedIndex();
          MDTuple tuple = (MDTuple) ScP[i].elementAt(0);
          Rectangle r = new Rectangle(tuple.x, tuple.y, ScW[i], ScH);
          MathList.ensureIndexIsVisible(i);
          if (tuple.b) CoordCanvas.scrollRectToVisible(r);
          else MathCanvas.scrollRectToVisible(r);
          MathCanvas.repaint();
          if (CoordRefs) CoordCanvas.repaint();
          updateDescriptionLabel(i);
        }
      }
    }
  }

  /**
   * Handles mouse clicks in the MathType window and &quot;map to&quot; canvas.
   */
  public void mousePressed(MouseEvent e) {
    synchronized (Lock) {
      Component c = e.getComponent();
      if (c == MathCanvas) {
        Point p = e.getPoint();
        for (int i=0; i<Scalars.length; i++) {
          for (int j=0; j<ScP[i].size(); j++) {
            MDTuple tuple = (MDTuple) ScP[i].elementAt(j);
            if (!tuple.b) {
              Rectangle r = new Rectangle(tuple.x, tuple.y, ScW[i], ScH);
              if (r.contains(p)) {
                // highlight clicked ScalarType
                MathList.setSelectedIndex(i);
                MathList.ensureIndexIsVisible(i);
                MathCanvas.scrollRectToVisible(r);
                updateDescriptionLabel(i);
                return;
              }
            }
          }
        }
      }
      else if (c == DisplayCanvas) {
        int col = e.getX() / 40;
        int row = e.getY() / 40;
        int ndx = MathList.getSelectedIndex();
        if (ndx >= 0 && row >= 0 && col >= 0 &&
          row < MapHeight && col < MapWidth && !Illegal[row][col])
        {
          Maps[ndx][row][col] = !Maps[ndx][row][col];
          if (Maps[ndx][row][col]) {
            CurMaps.addElement(CurMapLabel[ndx][row][col]);
          }
          else {
            CurrentMaps.clearSelection();
            CurMaps.removeElement(CurMapLabel[ndx][row][col]);
          }
          // redraw DisplayCanvas
          Graphics g = DisplayCanvas.getGraphics();
          g.setClip(40 * col, 40 * row, 41, 41);
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
          for (int j=0; j<ScP[i].size(); j++) {
            MDTuple tuple = (MDTuple) ScP[i].elementAt(j);
            if (tuple.b) {
              Rectangle r = new Rectangle(tuple.x, tuple.y, ScW[i], ScH);
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

  /**
   * Unused MouseListener method.
   */
  public void mouseEntered(MouseEvent e) { }

  /**
   * Unused MouseListener method.
   */
  public void mouseExited(MouseEvent e) { }

  /**
   * Unused MouseListener method.
   */
  public void mouseClicked(MouseEvent e) { }

  /** 
   * Unused MouseListener method.
   */
  public void mouseReleased(MouseEvent e) { }


  /**
   * Helper class representing an (int, int, boolean) tuple.
   */
  class MDTuple {
    int x, y;
    boolean b;

    MDTuple(int x, int y, boolean b) {
      this.x = x;
      this.y = y;
      this.b = b;
    }
  }

}
