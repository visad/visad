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

  /** RealType for mapping to Red. */
  private static final RealType RED_TYPE = RealType.getRealType("bio_red");

  /** RealType for mapping to Green. */
  private static final RealType GREEN_TYPE = RealType.getRealType("bio_green");

  /** RealType for mapping to Blue. */
  private static final RealType BLUE_TYPE = RealType.getRealType("bio_blue");

  /** RealType for mapping measurements to Z axis. */
  static final RealType Z_TYPE = RealType.getRealType("bio_line_z");

  /** Starting brightness value. */
  static final int NORMAL_BRIGHTNESS = 50;


  // -- PACKAGE-WIDE BIO-VISAD OBJECTS --

  /** List of measurements for each timestep. */
  MeasureList[] lists;

  /** VisAD 2-D display. */
  DisplayImpl display2;

  /** VisAD 3-D display. */
  DisplayImpl display3;

  /** Measurement pool for 2-D display. */
  MeasurePool pool2;

  /** Measurement pool for 3-D display. */
  MeasurePool pool3;

  /** Reference for image stack data. */
  DataReferenceImpl ref;

  /** Domain type for 2-D image stack data. */
  RealTupleType domain2;

  /** Domain type for 3-D image stack data. */
  RealTupleType domain3;

  /** Range type for image stack data. */
  MathType range;

  /** List of domain type components for image stack data. */
  RealType[] dtypes;

  /** List of range type components for image stack data. */
  RealType[] rtypes;

  /** Tuple type for fields with (r, g, b) range. */
  RealTupleType colorRange;

  /** Widget for stepping through the image stack. */
  ImageStackWidget vert;

  /** Widget for stepping through data from the series of files. */
  FileSeriesWidget horiz;

  /** Tool panel for adjusting viewing parameters. */
  ViewToolPanel toolView;

  /** Tool panel for performing measurement operations. */
  MeasureToolPanel toolMeasure;

  /** X and Y range of images. */
  double xRange, yRange;

  /** First free id number for measurement groups. */
  int maxId = 0;

  /** Measurement group list. */
  Vector groups = new Vector();


  // -- GUI COMPONENTS --

  /** Series chooser for loading a series of data files. */
  private SeriesChooser seriesBox;

  /** Panel containing VisAD displays. */
  private JPanel displayPane;


  // -- OTHER FIELDS --

  /** Prefix of current data series. */
  private String prefix;

  /** Mappings for 2-D display. */
  private ScalarMap animMap2, xMap2, yMap2;

  /** Mappings for 3-D display. */
  private ScalarMap xMap3, yMap3, zMap3, zMap3b;


  // -- CONSTRUCTOR --

  /** Constructs a new instance of BioVisAD. */
  public BioVisAD() throws VisADException, RemoteException {
    super(true);
    setTitle(TITLE);
    seriesBox = new SeriesChooser();
    colorRange = new RealTupleType(
      new RealType[] {RED_TYPE, GREEN_TYPE, BLUE_TYPE});

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

    // 2-D and 3-D measurement pools
    pool2 = new MeasurePool(this, display2, 2);
    if (display3 != null) pool3 = new MeasurePool(this, display3, 3);

    // image stack reference
    ref = new DataReferenceImpl("bio_ref");

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
    setComponent(twoD, display2.getComponent());
  }

  /** Toggles the visibility of the 3-D display. */
  public void set3D(boolean threeD) {
    if (display3 == null) return;
    setComponent(threeD, display3.getComponent());
  }

  /** Sets the displays to use the given image stack timestep. */
  public boolean setData(Data data) throws VisADException, RemoteException {
    FieldImpl field = analyzeData(data);
    if (field == null) return false;
    ref.setData(field);
    return true;
  }

  /** Initializes the displays to use the given image stack data. */
  public boolean init(Data data, int timesteps)
    throws VisADException, RemoteException
  {
    FieldImpl field = analyzeData(data);
    if (field == null) return false;

    // clear old displays
    display2.removeAllReferences();
    display2.clearMaps();
    if (display3 != null) {
      display3.removeAllReferences();
      display3.clearMaps();
    }

    // reset measurements
    if (lists != null) clear();

    // set new data
    ref.setData(field);

    // The FieldImpl must be in one of the following forms:
    //     (index -> ((x, y) -> range))
    //     (index -> ((x, y) -> (r1, r2, ..., rn))
    //
    // dtypes = {x, y, index}; rtypes = {r1, r2, ..., rn}

    // extract types
    FunctionType time_function = (FunctionType) field.getType();
    RealTupleType time_domain = time_function.getDomain();
    MathType time_range = time_function.getRange();
    if (time_domain.getDimension() > 1 ||
      !(time_range instanceof FunctionType))
    {
      throw new VisADException("Field is not an image stack");
    }
    RealType time_slice = (RealType) time_domain.getComponent(0);
    FunctionType image_function = (FunctionType) time_range;
    domain2 = image_function.getDomain();
    RealType[] image_dtypes = domain2.getRealComponents();
    if (image_dtypes.length < 2) {
      throw new VisADException("Data stack does not contain images");
    }
    dtypes = new RealType[] {image_dtypes[0], image_dtypes[1], time_slice};
    domain3 = new RealTupleType(dtypes);
    range = image_function.getRange();
    if (!(range instanceof RealTupleType) && !(range instanceof RealType)) {
      throw new VisADException("Invalid field range");
    }
    dtypes = domain3.getRealComponents();
    rtypes = range instanceof RealTupleType ?
      ((RealTupleType) range).getRealComponents() :
      new RealType[] {(RealType) range};

    // set up mappings to 2-D display
    ScalarMap x_map2 = new ScalarMap(dtypes[0], Display.XAxis);
    ScalarMap y_map2 = new ScalarMap(dtypes[1], Display.YAxis);
    ScalarMap anim_map = new ScalarMap(time_slice, Display.Animation);
    ScalarMap r_map2 = new ScalarMap(RED_TYPE, Display.Red);
    ScalarMap g_map2 = new ScalarMap(GREEN_TYPE, Display.Green);
    ScalarMap b_map2 = new ScalarMap(BLUE_TYPE, Display.Blue);
    display2.addMap(x_map2);
    display2.addMap(y_map2);
    display2.addMap(anim_map);
    vert.setMap(anim_map);
    display2.addMap(r_map2);
    display2.addMap(g_map2);
    display2.addMap(b_map2);

    // CTR - TODO - full range component color support
    display2.addMap(new ScalarMap(rtypes[0], Display.RGB));

    // set up 2-D data references
    display2.addReference(ref);
    pool2.init();

    // set up mappings to 3-D display
    ScalarMap x_map3 = null, y_map3 = null, z_map3a = null, z_map3b = null;
    ScalarMap r_map3 = null, g_map3 = null, b_map3 = null;
    if (display3 != null) {
      x_map3 = new ScalarMap(dtypes[0], Display.XAxis);
      y_map3 = new ScalarMap(dtypes[1], Display.YAxis);
      z_map3a = new ScalarMap(time_slice, Display.ZAxis);
      z_map3b = new ScalarMap(Z_TYPE, Display.ZAxis);
      r_map3 = new ScalarMap(RED_TYPE, Display.Red);
      g_map3 = new ScalarMap(GREEN_TYPE, Display.Green);
      b_map3 = new ScalarMap(BLUE_TYPE, Display.Blue);
      display3.addMap(x_map3);
      display3.addMap(y_map3);
      display3.addMap(z_map3a);
      display3.addMap(z_map3b);
      display3.addMap(r_map3);
      display3.addMap(g_map3);
      display3.addMap(b_map3);

      // CTR - TODO - full range component color support
      display3.addMap(new ScalarMap(rtypes[0], Display.RGB));

      // set up 3-D data references
      display3.addReference(ref);
      pool3.init();
    }

    // set up 2-D ranges
    Set set = ((FieldImpl) field.getSample(0)).getDomainSet();
    float[][] samples = set.getSamples(false);
    int dim = samples.length;

    // x-axis range
    float min_x = samples[0][0];
    float max_x = samples[0][samples[0].length - 1];
    xRange = Math.abs(max_x - min_x);
    if (min_x != min_x) min_x = 0;
    if (max_x != max_x) max_x = 0;
    x_map2.setRange(min_x, max_x);

    // y-axis range
    float min_y = samples[1][0];
    float max_y = samples[1][samples[1].length - 1];
    yRange = Math.abs(max_y - min_y);
    if (min_y != min_y) min_y = 0;
    if (max_y != max_y) max_y = 0;
    y_map2.setRange(min_y, max_y);

    // color ranges
    r_map2.setRange(0, 255);
    g_map2.setRange(0, 255);
    b_map2.setRange(0, 255);

    // set up 3-D ranges
    if (display3 != null) {
      // x-axis and y-axis ranges
      x_map3.setRange(min_x, max_x);
      y_map3.setRange(min_y, max_y);

      // z-axis range
      float min_z = 0;
      float max_z = field.getLength() - 1;
      if (min_z != min_z) min_z = 0;
      if (max_z != max_z) max_z = 0;
      z_map3a.setRange(min_z, max_z);
      z_map3b.setRange(min_z, max_z);

      // color ranges
      r_map3.setRange(0, 255);
      g_map3.setRange(0, 255);
      b_map3.setRange(0, 255);
    }

    toolView.doColorTable();

    // initialize measurement list array
    lists = new MeasureList[timesteps];
    for (int i=0; i<timesteps; i++) lists[i] = new MeasureList(this);

    return true;
  }

  /** Clears all measurements from all image slices. */
  public void clear() {
    int index = getIndex();
    for (int i=0; i<lists.length; i++) {
      lists[i].removeAllMeasurements(i == index);
    }
  }

  /**
   * Updates image color table to match the
   * given grayscale and brightness values.
   */
  public void setImageColors(boolean grayscale, int brightness) {
    float[][] table = grayscale ?
      ColorControl.initTableGreyWedge(new float[3][256]) :
      ColorControl.initTableVis5D(new float[3][256]);

    // apply brightness (actually gamma correction)
    double gamma = 1.0 -
      (1.0 / NORMAL_BRIGHTNESS) * (brightness - NORMAL_BRIGHTNESS);
    for (int i=0; i<256; i++) {
      table[0][i] = (float) Math.pow(table[0][i], gamma);
      table[1][i] = (float) Math.pow(table[1][i], gamma);
      table[2][i] = (float) Math.pow(table[2][i], gamma);
    }

    // get color controls
    ColorControl cc2 = (ColorControl) display2.getControl(ColorControl.class);
    ColorControl cc3 = display3 == null ? null :
      (ColorControl) display3.getControl(ColorControl.class);

    // set color tables
    try {
      if (cc2 != null) cc2.setTable(table);
      if (cc3 != null) cc3.setTable(table);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }
  /** Gets current index value. */
  public int getIndex() { return horiz.getValue() - 1; }

  /** Gets current slice value. */
  public int getSlice() { return vert.getValue() - 1; }

  /** Gets the number of slice values. */
  public int getNumberOfSlices() { return vert.getMaximum(); }

  /** Gets measurement list for current index. */
  public MeasureList getList() { return lists[horiz.getValue() - 1]; }


  // -- HELPER METHODS --

  /** Toggles the given component on or off in the left side panel. */
  private void setComponent(boolean on, Component c) {
    if (on) {
      if (displayPane.isAncestorOf(c)) return;
      displayPane.add(c);
    }
    else {
      if (!displayPane.isAncestorOf(c)) return;
      displayPane.remove(c);
    }
    displayPane.validate();
    displayPane.repaint();
  }

  /** Ensures that the given data object is of the proper form. */
  public FieldImpl analyzeData(Data data) {
    FieldImpl field = null;
    if (data instanceof FieldImpl) field = (FieldImpl) data;
    else if (data instanceof Tuple) {
      Tuple tuple = (Tuple) data;
      Data[] d = tuple.getComponents();
      for (int i=0; i<d.length; i++) {
        if (d[i] instanceof FieldImpl) {
          field = (FieldImpl) d[i];
          break;
        }
      }
    }
    return field;
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
        if (f == null || f.length < 1) {
          JOptionPane.showMessageDialog(frame,
            "Invalid series", "Cannot load series",
            JOptionPane.ERROR_MESSAGE);
          return;
        }
        horiz.setSeries(f);
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


  // -- UTILITY METHODS --

  /** Toggles the cursor between hourglass and normal pointer mode. */
  void setWaitCursor(boolean wait) {
    setCursor(wait ?
      Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
      Cursor.getDefaultCursor());
  }

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
            type = Z_TYPE;
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
    final BioVisAD bio = new BioVisAD();
    bio.pack();
    bio.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    Util.centerWindow(bio);
    bio.show();
  }

}
