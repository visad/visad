//
// BioVisAD.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.data.DefaultFamily;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.*;
import visad.util.*;

/**
 * BioVisAD is a multi-purpose biological analysis tool.
 *
 * It provides multi-dimensional visualization of biological data,
 * and supports distance measurement between points in a field or
 * stack of fields.
 */
public class BioVisAD extends GUIFrame implements ChangeListener {

  // -- CONSTANTS --

  /** Application title. */
  private static final String TITLE = "BioVisAD";

  /** Amount of detail for color. */
  static final int COLOR_DETAIL = 256;

  /** Starting brightness value. */
  static final int NORMAL_BRIGHTNESS = COLOR_DETAIL / 2;

  /** Starting contrast value. */
  static final int NORMAL_CONTRAST = COLOR_DETAIL / 2;


  // -- DISPLAYS --

  /** VisAD 2-D display. */
  DisplayImpl display2;

  /** VisAD 3-D display. */
  DisplayImpl display3;


  // -- SLIDER WIDGETS --

  /** Widget for stepping through the image stack. */
  ImageStackWidget vert;

  /** Widget for stepping through the timestep indices. */
  FileSeriesWidget horiz;


  // -- TOOL PANELS --

  /** Tool panel for adjusting viewing parameters. */
  ViewToolPanel toolView;

  /** Tool panel for performing measurement operations. */
  MeasureToolPanel toolMeasure;


  // -- LOGIC MANAGEMENT OBJECTS --

  /** Object for handling measurement logic. */
  MeasureManager mm;

  /** Object for handling slice logic. */
  SliceManager sm;


  // -- GUI COMPONENTS --

  /** Series chooser for loading a series of data files. */
  private SeriesChooser seriesBox;

  /** Panel containing VisAD displays. */
  private JPanel displayPane;


  // -- OTHER FIELDS --

  /** Prefix of current data series. */
  private String prefix;


  // -- CONSTRUCTORS --

  /** Constructs a new instance of BioVisAD. */
  public BioVisAD() throws VisADException, RemoteException { this(32); }

  /**
   * Constructs a new instance of BioVisAD with the specified
   * maximum low-resolution thumbnail size in megabytes.
   */
  public BioVisAD(int thumbSize) throws VisADException, RemoteException {
    super(true);
    setTitle(TITLE);
    seriesBox = new SeriesChooser();

    // menu bar
    addMenuItem("File", "Open...", "fileOpen", 'o');
    addMenuSeparator("File");
    addMenuItem("File", "Exit", "fileExit", 'x');

    // lay out components
    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    setContentPane(pane);

    // display panel
    displayPane = new JPanel();
    displayPane.setLayout(new BoxLayout(displayPane, BoxLayout.Y_AXIS));
    pane.add(displayPane, BorderLayout.CENTER);

    // 2-D and 3-D displays
    if (Util.canDoJava3D()) {
      display2 = new DisplayImplJ3D("display2", new TwoDDisplayRendererJ3D());
      display3 = new DisplayImplJ3D("display3");
    }
    else {
      display2 = (DisplayImpl) new DisplayImplJ2D("display2");
      display3 = null;
    }
    display2.getGraphicsModeControl().setPointSize(5.0f);
    display2.getDisplayRenderer().setPickThreshhold(Float.MAX_VALUE);
    displayPane.add(display2.getComponent());
    if (display3 != null) {
      GraphicsModeControl gmc = display3.getGraphicsModeControl();
      gmc.setPointSize(5.0f);
      gmc.setLineWidth(2.0f);
      display3.getDisplayRenderer().setPickThreshhold(Float.MAX_VALUE);
      displayPane.add(display3.getComponent());
    }

    // logic managers
    mm = new MeasureManager(this);
    sm = new SliceManager(this, thumbSize);

    // vertical slider
    vert = new ImageStackWidget(this);
    vert.setAlignmentY(ImageStackWidget.TOP_ALIGNMENT);
    pane.add(vert, BorderLayout.WEST);

    // horizontal slider
    horiz = new FileSeriesWidget(this);
    horiz.addChangeListener(this);
    pane.add(horiz, BorderLayout.SOUTH);

    // tool panels
    JTabbedPane tabs = new JTabbedPane();
    pane.add(tabs, BorderLayout.EAST);

    // viewing tool panel
    toolView = new ViewToolPanel(this);
    tabs.addTab("View", toolView);

    // measurement tool panel
    toolMeasure = new MeasureToolPanel(this);
    tabs.addTab("Measure", toolMeasure);
  }


  // -- API METHODS --

  /** Toggles the visibility of the 2-D display. */
  public void set2D(boolean twoD) {
    display2.getComponent().setVisible(twoD);
  }

  /** Toggles the visibility of the 3-D display. */
  public void set3D(boolean threeD) {
    if (display3 == null) return;
    display3.getComponent().setVisible(threeD);
  }

  /** Updates image color table to match the given values. */
  public void setImageColors(int brightness, int contrast,
    RealType red, RealType green, RealType blue)
  {
    // get color controls
    ColorControl[] cc2 = sm.getColorControls2D();
    if (cc2 == null) return;
    ColorControl[] cc3 = sm.getColorControls3D();

    double mid = COLOR_DETAIL / 2.0;
    double center = (double) brightness / COLOR_DETAIL;
    double slope;
    if (contrast <= mid) slope = contrast / mid;
    else slope = mid / (COLOR_DETAIL - contrast);

    float[] vals = new float[COLOR_DETAIL];
    for (int i=0; i<COLOR_DETAIL; i++) {
      vals[i] = (float) (0.5 * slope * (i / mid - 1.0) + center);
      if (vals[i] < 0) vals[i] = 0;
      else if (vals[i] > 1) vals[i] = 1;
    }

    // initialize color tables
    for (int i=0; i<cc2.length; i++) {
      if (i >= sm.rtypes.length) break;
      RealType rt = sm.rtypes[i];
      float[][] t = new float[3][COLOR_DETAIL];
      if (rt.equals(red)) System.arraycopy(vals, 0, t[0], 0, COLOR_DETAIL);
      if (rt.equals(green)) System.arraycopy(vals, 0, t[1], 0, COLOR_DETAIL);
      if (rt.equals(blue)) System.arraycopy(vals, 0, t[2], 0, COLOR_DETAIL);

      // set color table
      try {
        if (cc2[i] != null) cc2[i].setTable(t);
        if (cc3 != null && cc3[i] != null) cc3[i].setTable(t);
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }


  // -- MENU COMMANDS --

  /** Loads a series of datasets specified by the user. */
  public void fileOpen() {
    final JFrame frame = this;
    Util.invoke(false, new Runnable() {
      public void run() {
        // get file series from file dialog
        if (seriesBox.showDialog(frame) != SeriesChooser.APPROVE_OPTION) {
          return;
        }

        // load first file in series
        File[] f = seriesBox.getSeries();
        prefix = seriesBox.getPrefix();
        sm.setThumbnails(seriesBox.getThumbs());
        if (f == null || f.length < 1) {
          JOptionPane.showMessageDialog(frame,
            "Invalid series", "Cannot load series",
            JOptionPane.ERROR_MESSAGE);
          return;
        }
        sm.setSeries(f);
      }
    });
  }

  /** Exits the application. */
  public void fileExit() { System.exit(0); }


  // -- INTERNAL API METHODS --

  /** Listens for file series widget changes. */
  public void stateChanged(ChangeEvent e) {
    int max = horiz.getMaximum();
    int cur = horiz.getValue();
    setTitle(TITLE + " - " + prefix + " (" + cur + "/" + max + ")");
  }

  /** Toggles the cursor between hourglass and normal pointer mode. */
  void setWaitCursor(boolean wait) {
    setCursor(wait ?
      Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
      Cursor.getDefaultCursor());
  }


  // -- UTILITY METHODS --

  /** Makes a deep copy of the given RealTuple array. */
  public static RealTuple[] copy(RealTuple[] tuples) {
    return copy(tuples, -1);
  }

  /**
   * Makes a deep copy of the given RealTuple array,
   * altering the last dimension to match the specified Z-slice value.
   */
  public static RealTuple[] copy(RealTuple[] tuples, int slice) {
    try {
      RealTuple[] n_tuples = new RealTuple[tuples.length];
      for (int j=0; j<tuples.length; j++) {
        int dim = tuples[j].getDimension();
        Data[] comps = tuples[j].getComponents();
        Real[] n_comps = new Real[dim];
        for (int i=0; i<dim; i++) {
          Real real = (Real) comps[i];
          double value;
          RealType type;
          if (slice >= 0 && i == dim - 1) {
            value = slice;
            type = SliceManager.Z_TYPE;
          }
          else {
            value = real.getValue();
            type = (RealType) real.getType();
          }
          n_comps[i] = new Real(type, value, real.getUnit(), real.getError());
        }
        RealTupleType tuple_type = (RealTupleType) tuples[j].getType();
        RealType[] real_types = tuple_type.getRealComponents();
        RealType[] n_real_types = new RealType[dim];
        System.arraycopy(real_types, 0, n_real_types, 0, dim);
        n_tuples[j] = new RealTuple(new RealTupleType(n_real_types),
          n_comps, tuples[j].getCoordinateSystem());
      }
      return n_tuples;
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    return null;
  }

  /** Dumps information about the given RealTuple to the screen. */
  public static void dump(RealTuple tuple) {
    Data[] comps = tuple.getComponents();
    for (int i=0; i<comps.length; i++) {
      Real real = (Real) comps[i];
      System.out.println("#" + i +
        ": type=" + real.getType() + "; value=" + real.getValue());
    }
  }


  // -- MAIN --

  /** Launches the BioVisAD GUI. */
  public static void main(String[] args) throws Exception {
    int thumbSize = 32;
    if (args.length > 0) {
      try { thumbSize = Integer.parseInt(args[0]); }
      catch (NumberFormatException exc) { }
    }
    final BioVisAD bio = new BioVisAD(thumbSize);
    bio.pack();
    bio.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    Util.centerWindow(bio);
    bio.show();
  }

}
