//
// VisBio.java
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

import ij.ImagePlus;
import ij.io.FileSaver;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.data.*;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.*;
import visad.util.*;

/**
 * VisBio is a multi-purpose biological analysis tool.
 *
 * It provides multi-dimensional visualization of an image or stack
 * of images across time, arbitrary slicing of the data, and
 * measurement tools for computing distances between data points.
 */
public class VisBio extends GUIFrame implements ChangeListener {

  // -- CONSTANTS --

  /** Application title. */
  private static final String TITLE = "VisBio";

  /** Flag for enabling or disabling Java3D, for debugging. */
  private static final boolean ALLOW_3D = true;

  /** Maximum pixel distance for picking. */
  static final int PICKING_THRESHOLD = 10;

  /** Amount of detail for color. */
  static final int COLOR_DETAIL = 256;

  /** Starting brightness value. */
  static final int NORMAL_BRIGHTNESS = COLOR_DETAIL / 2;

  /** Starting contrast value. */
  static final int NORMAL_CONTRAST = COLOR_DETAIL / 2;

  /** Amount of detail for resolution sliders. */
  static final int RESOLUTION_DETAIL = 256;

  /** Default measurement group. */
  static MeasureGroup noneGroup;

  /** RGB composite color table. */
  static final float[][] rainbow =
    ColorControl.initTableVis5D(new float[3][COLOR_DETAIL]);

  /** HSV composite color table. */
  static final float[][] hsv =
    ColorControl.initTableHSV(new float[3][COLOR_DETAIL]);


  // -- DISPLAYS --

  /** VisAD 2-D display. */
  DisplayImpl display2;

  /** VisAD 3-D display. */
  DisplayImpl display3;

  /** Previous preview display. */
  DisplayImpl previous;

  /** Next preview display. */
  DisplayImpl next;

  /** Previous and last preview displays. */
  JPanel previewPane;


  // -- SLIDER WIDGETS --

  /** Widget for stepping through the image stack. */
  ImageStackWidget vert;

  /** Widget for stepping through the timestep indices. */
  FileSeriesWidget horiz;


  // -- TOOL PANELS --

  /** Tool panel for adjusting viewing parameters. */
  ViewToolPanel toolView;

  /** Tool panel for manipulating colors. */
  ColorToolPanel toolColor;

  /** Tool panel for managing image alignment, spacing and drift correction. */
  AlignToolPanel toolAlign;

  /** Tool panel for performing measurement operations. */
  MeasureToolPanel toolMeasure;


  // -- LOGIC MANAGEMENT OBJECTS --

  /** Object for handling measurement logic. */
  MeasureManager mm;

  /** Object for handling slice logic. */
  SliceManager sm;

  /** Object for handling state logic in case of a program crash. */
  StateManager state;


  // -- GUI COMPONENTS --

  /** Import dialog for loading data series. */
  private ImportDialog importer;

  /** Export dialog for saving data series. */
  private ExportDialog exporter;

  /** Options dialog for configuring VisBio. */
  OptionDialog options;

  /** Help dialog for detailing basic program usage. */
  private BioHelpWindow help;

  /** Panel containing VisAD displays. */
  private JPanel displayPane;

  /** Menu item for exporting data. */
  private JMenuItem fileExport;


  // -- COLOR SETTINGS --

  /** Brightness and contrast of images. */
  int brightness, contrast;

  /** Color model (RGB or HSV). */
  int model;

  /** Whether to use RGB composite coloring. */
  boolean composite;

  /** Red, green, blue and composite components of images. */
  RealType red, green, blue;


  // -- OTHER FIELDS --

  /** Prefix of current data series. */
  private String prefix;


  // -- CONSTRUCTOR --

  /** Constructs a new instance of VisBio. */
  public VisBio() throws VisADException, RemoteException {
    super(true);
    setTitle(TITLE);
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    importer = new ImportDialog();
    exporter = new ExportDialog(this);
    options = new OptionDialog(this);
    help = new BioHelpWindow();

    // menu bar
    addMenuItem("File", "Import data...", "fileImport", 'i');
    fileExport = addMenuItem("File", "Export data...", "fileExport", 'e');
    addMenuSeparator("File");
    addMenuItem("File", "Restore state...", "fileRestore", 'r');
    addMenuItem("File", "Save state...", "fileSave", 's');
    addMenuSeparator("File");
    addMenuItem("File", "Take snapshot...", "fileSnap", 's');
    addMenuSeparator("File");
    addMenuItem("File", "Options...", "fileOptions", 't');
    addMenuSeparator("File");
    addMenuItem("File", "Exit", "fileExit", 'x');
    fileExport.setEnabled(false);
    addMenuItem("Help", "Overview", "helpOverview", 'o');
    addMenuItem("Help", "QuickTime", "helpQuickTime", 'q');
    addMenuItem("Help", "About", "helpAbout", 'a');

    // lay out components
    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    setContentPane(pane);

    // display panel
    displayPane = new JPanel();
    displayPane.setLayout(new BoxLayout(displayPane, BoxLayout.Y_AXIS));
    pane.add(displayPane, BorderLayout.CENTER);

    // 2-D and 3-D displays
    if (Util.canDoJava3D() && ALLOW_3D) {
      display2 = new DisplayImplJ3D("display2", new TwoDDisplayRendererJ3D());
      display3 = new DisplayImplJ3D("display3");
      previous = new DisplayImplJ3D("previous");
      next = new DisplayImplJ3D("next");
      previewPane = new JPanel();
      previewPane.setPreferredSize(new Dimension(0, 130));
      previewPane.setLayout(new BoxLayout(previewPane, BoxLayout.X_AXIS));
      previewPane.add(previous.getComponent());
      previewPane.add(next.getComponent());
      previewPane.setVisible(false);
    }
    else {
      display2 = (DisplayImpl) new DisplayImplJ2D("display2");
      display3 = previous = next = null;
      previewPane = null;
    }
    display2.getGraphicsModeControl().setColorMode(
      GraphicsModeControl.SUM_COLOR_MODE);
    display2.getDisplayRenderer().setPickThreshhold(Float.MAX_VALUE);
    //display2.enableEvent(DisplayEvent.MOUSE_DRAGGED);
    displayPane.add(display2.getComponent());
    if (display3 != null) {
      GraphicsModeControl gmc = display3.getGraphicsModeControl();
      gmc.setColorMode(GraphicsModeControl.SUM_COLOR_MODE);
      gmc.setLineWidth(2.0f);
      DisplayRendererJ3D renderer =
        (DisplayRendererJ3D) display3.getDisplayRenderer();
      renderer.setPickThreshhold(Float.MAX_VALUE);
      //display3.enableEvent(DisplayEvent.MOUSE_DRAGGED);
      displayPane.add(display3.getComponent());
      displayPane.add(previewPane);
    }

    // logic managers
    mm = new MeasureManager(this);
    sm = new SliceManager(this);
    state = new StateManager(this);
    if (noneGroup == null) noneGroup = new MeasureGroup(this, "NONE");

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
    tabs.addTab("View", null, toolView, "Controls for the displays");

    // color tool panel
    toolColor = new ColorToolPanel(this);
    tabs.addTab("Color", null, toolColor, "Controls for manipulating color");

    // alignment tool panel
    toolAlign = new AlignToolPanel(this);
    tabs.addTab("Align", null, toolAlign, "Controls for orienting data");

    // measurement tool panel
    toolMeasure = new MeasureToolPanel(this);
    tabs.addTab("Measure", null, toolMeasure, "Controls for measuring data");
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

  /** Toggles the visibility of the preview displays. */
  public void setPreview(boolean preview) {
    if (previewPane == null) return;
    if (preview) sm.updateAnimationControls();
    previewPane.setVisible(preview);
  }

  /** Zooms a display by the given amount. */
  public void setZoom(boolean threeD, double scale) {
    DisplayImpl d = threeD ? display3 : display2;
    try {
      ProjectionControl control = d.getProjectionControl();
      double[] matrix = control.getMatrix();
      double[] zoom = d.make_matrix(0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0);
      control.setMatrix(d.multiply_matrix(zoom, matrix));
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Restores a display's zoom to the original value. */
  public void resetZoom(boolean threeD) {
    DisplayImpl d = threeD ? display3 : display2;
    try { d.getProjectionControl().resetProjection(); }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Toggles the 3-D display between image stack and volume render modes. */
  public void setVolume(boolean volume) { sm.setVolumeRender(volume); }

  /** Adjusts the aspect ratio of the displays. */
  public void setAspect(double x, double y, double z) {
    double d = x > y ? x : y;
    double xasp = x / d;
    double yasp = y / d;
    double zasp = z == z ? z / d : 1.0;
    ProjectionControl pc2 = display2.getProjectionControl();
    try {
      if (display3 == null) pc2.setAspect(new double[] {xasp, yasp});
      else pc2.setAspect(new double[] {xasp, yasp, zasp});
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    if (display3 != null) {
      ProjectionControl pc3 = display3.getProjectionControl();
      ProjectionControl pcPp = previous.getProjectionControl();
      ProjectionControl pcPn = next.getProjectionControl();
      try {
        pc3.setAspect(new double[] {xasp, yasp, zasp});
        pcPp.setAspect(new double[] {xasp, yasp, zasp});
        pcPn.setAspect(new double[] {xasp, yasp, zasp});
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

  /** Updates image color table to match the given values. */
  public void setImageColors(int brightness, int contrast, int model,
    boolean composite, RealType r, RealType g, RealType b)
  {
    // verify that image color information has changed
    if (this.brightness == brightness && this.contrast == contrast &&
      this.model == model && this.composite == composite &&
      red == r && green == g && blue == b)
    {
      return;
    }
    this.brightness = brightness;
    this.contrast = contrast;
    this.model = model;
    this.composite = composite;
    red = r;
    green = g;
    blue = b;

    // get color widgets
    LabeledColorWidget[] widgets = sm.getColorWidgets();
    if (widgets == null) return;

    // compute center and slope from brightness and contrast
    double mid = COLOR_DETAIL / 2.0;
    double slope;
    if (contrast <= mid) slope = contrast / mid;
    else slope = mid / (COLOR_DETAIL - contrast);
    if (slope == Double.POSITIVE_INFINITY) slope = Double.MAX_VALUE;
    double center = (slope + 1) * brightness / COLOR_DETAIL - 0.5 * slope;

    // compute color channel table values from center and slope
    float[] vals = new float[COLOR_DETAIL];
    float[] rvals, gvals, bvals;
    if (composite) {
      rvals = new float[COLOR_DETAIL];
      gvals = new float[COLOR_DETAIL];
      bvals = new float[COLOR_DETAIL];
      float[][] comp = model == 0 ? rainbow : hsv;
      for (int i=0; i<COLOR_DETAIL; i++) {
        rvals[i] = (float) (slope * (comp[0][i] - 0.5) + center);
        gvals[i] = (float) (slope * (comp[1][i] - 0.5) + center);
        bvals[i] = (float) (slope * (comp[2][i] - 0.5) + center);
        if (rvals[i] > 1) rvals[i] = 1;
        if (rvals[i] < 0) rvals[i] = 0;
        if (gvals[i] > 1) gvals[i] = 1;
        if (gvals[i] < 0) gvals[i] = 0;
        if (bvals[i] > 1) bvals[i] = 1;
        if (bvals[i] < 0) bvals[i] = 0;
      }
    }
    else {
      for (int i=0; i<COLOR_DETAIL; i++) {
        vals[i] = (float) (0.5 * slope * (i / mid - 1.0) + center);
      }
      rvals = gvals = bvals = vals;
    }
    for (int i=0; i<COLOR_DETAIL; i++) {
      if (vals[i] < 0.0f) vals[i] = 0.0f;
      else if (vals[i] > 1.0f) vals[i] = 1.0f;
    }

    // initialize color tables
    boolean r_solid = r == BioColorWidget.SOLID;
    boolean g_solid = g == BioColorWidget.SOLID;
    boolean b_solid = b == BioColorWidget.SOLID;
    for (int j=0; j<widgets.length; j++) {
      if (j >= sm.rtypes.length) break;
      RealType rt = sm.rtypes[j];

      // fill in color table elements
      float[][] t;
      if (composite) t = new float[][] {rvals, gvals, bvals};
      else {
        t = new float[][] {
          rt.equals(r) ? rvals : new float[COLOR_DETAIL],
          rt.equals(g) ? gvals : new float[COLOR_DETAIL],
          rt.equals(b) ? bvals : new float[COLOR_DETAIL]
        };
        if (r_solid) Arrays.fill(t[0], 1.0f);
        if (g_solid) Arrays.fill(t[1], 1.0f);
        if (b_solid) Arrays.fill(t[2], 1.0f);

        // convert color table to HSV color model if necessary
        if (model == BioColorWidget.HSV) {
          float[][] newt = new float[3][COLOR_DETAIL];
          for (int i=0; i<COLOR_DETAIL; i++) {
            int rgb = Color.HSBtoRGB(t[0][i], t[1][i], t[2][i]);
            Color c = new Color(rgb);
            newt[0][i] = c.getRed() / 255f;
            newt[1][i] = c.getGreen() / 255f;
            newt[2][i] = c.getBlue() / 255f;
          }
          t = newt;
        }
      }

      // set widget color table
      boolean doAlpha = display3 != null;
      float[][] oldt = widgets[j].getTable();
      t = BioUtil.adjustColorTable(t,
        oldt.length > 3 ? oldt[3] : null, doAlpha);
      widgets[j].setTable(t);
    }
    state.saveState(true);
  }


  // -- MENU COMMANDS --

  /** Loads a data series specified by the user. */
  public void fileImport() {
    final JFrame frame = this;
    Util.invoke(false, new Runnable() {
      public void run() {
        // get file series from import dialog
        if (importer.showDialog(frame) != ImportDialog.APPROVE_OPTION) return;

        // load first file in series
        File[] f = importer.getSeries();
        prefix = importer.getPrefix();
        sm.setThumbnails(importer.getThumbs(),
          importer.getThumbResX(), importer.getThumbResY());
        if (f == null || f.length < 1) {
          JOptionPane.showMessageDialog(frame, "Invalid series",
            "Cannot load series", JOptionPane.ERROR_MESSAGE);
          return;
        }
        sm.setSeries(f, importer.getFilesAsSlices());
        fileExport.setEnabled(true);
      }
    });
  }

  /** Exports the current dataset as specified by the user. */
  public void fileExport() {
    Util.invoke(false, new Runnable() {
      public void run() {
        // display export dialog
        if (exporter.showDialog() != ExportDialog.APPROVE_OPTION) return;
        exporter.export();
      }
    });
  }

  /** Restores the current state from a text file specified by the user. */
  public void fileRestore() {
    // CTR - TODO - fileRestore
  }

  /** Saves the current state to a text file specified by the user. */
  public void fileSave() {
    // CTR - TODO - fileSave
  }

  /** Saves a snapshot of the displays to a file specified by the user. */
  public void fileSnap() {
    final VisBio bio = this;
    Thread t = new Thread(new Runnable() {
      public void run() {
        JFileChooser fileBox = new JFileChooser();
        fileBox.addChoosableFileFilter(new ExtensionFileFilter(
          new String[] {"jpg", "jpeg"}, "JPEG files"));
        fileBox.addChoosableFileFilter(new ExtensionFileFilter(
          "raw", "RAW files"));
        fileBox.addChoosableFileFilter(new ExtensionFileFilter(
          new String[] {"tif", "tiff"}, "TIFF files"));
        int rval = fileBox.showSaveDialog(bio);
        if (rval == JFileChooser.APPROVE_OPTION) {
          setWaitCursor(true);

          // determine file type
          String file = fileBox.getSelectedFile().getPath();
          String ext = "";
          int dot = file.indexOf(".");
          if (dot >= 0) ext = file.substring(dot + 1).toLowerCase();
          boolean tiff = ext.equals("tif") || ext.equals("tiff");
          boolean jpeg = ext.equals("jpg") || ext.equals("jpeg");
          boolean raw = ext.equals("raw");
          if (!tiff && !jpeg && !raw) {
            setWaitCursor(false);
            JOptionPane.showMessageDialog(bio, "Invalid filename (" +
              file + "): " + "extension must be TIFF, JPEG or RAW.",
              "Cannot export snapshot", JOptionPane.ERROR_MESSAGE);
            return;
          }

          // construct output image
          boolean has2 = display2.getComponent().isVisible();
          boolean has3 = display3 != null &&
            display3.getComponent().isVisible();
          if (!has2 && !has3) {
            setWaitCursor(false);
            JOptionPane.showMessageDialog(bio, "No displays are visible.",
              "Cannot export snapshot", JOptionPane.ERROR_MESSAGE);
            return;
          }

          BufferedImage image2 = has2 ? display2.getImage() : null;
          BufferedImage image3 = has3 ? display3.getImage() : null;
          int w2 = 0, h2 = 0, w3 = 0, h3 = 0;
          int type = BufferedImage.TYPE_INT_RGB;
          if (has2) {
            w2 = image2.getWidth();
            h2 = image2.getHeight();
            type = image2.getType();
          }
          if (has3) {
            w3 = image3.getWidth();
            h3 = image3.getHeight();
            type = image3.getType();
          }
          BufferedImage img = new BufferedImage(
            w2 > w3 ? w2 : w3, h2 + h3, type);
          Graphics g = img.createGraphics();
          if (has2) g.drawImage(image2, 0, 0, null);
          if (has3) g.drawImage(image3, 0, h2, null);

          // save image to disk
          FileSaver saver = new FileSaver(new ImagePlus("null", img));
          if (tiff) saver.saveAsTiff(file);
          else if (jpeg) saver.saveAsJpeg(file);
          else if (raw) saver.saveAsRaw(file);

          setWaitCursor(false);
        }
      }
    });
    t.start();
  }

  /** Displays the VisBio options dialog box. */
  public void fileOptions() {
    Util.invoke(false, new Runnable() {
      public void run() {
        // display options dialog
        options.showDialog();
      }
    });
  }

  /** Exits the application. */
  public void fileExit() {
    mm.checkSave();
    state.destroy();
    System.exit(0);
  }

  /** Brings up the help window on the Overview tab. */
  public void helpOverview() { doHelp(0); }

  /** Brings up the help window on the QuickTime tab. */
  public void helpQuickTime() { doHelp(1); }

  /** Brings up the help window on the About tab. */
  public void helpAbout() { doHelp(2); }


  // -- INTERNAL API METHODS --

  /** Listens for file series widget changes. */
  public void stateChanged(ChangeEvent e) {
    int max = horiz.getMaximum();
    int cur = horiz.getValue();
    setTitle(TITLE + " - " + prefix + " (" + cur + "/" + max + ")");
  }

  /** Toggles the cursor between hourglass and normal pointer mode. */
  void setWaitCursor(boolean wait) {
    setCursor(wait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
      Cursor.getDefaultCursor());
  }

  /** Writes the current program state to the given output stream. */
  void saveState(PrintWriter fout) throws IOException, VisADException {
    fout.println(prefix);
    fout.println(brightness);
    fout.println(contrast);
    fout.println(model);
    fout.println(composite);
    fout.println(red == null ? "null" : red.getName());
    fout.println(green == null ? "null" : green.getName());
    fout.println(blue == null ? "null" : blue.getName());
    sm.saveState(fout);
  }

  /** Restores the current program state from the given input stream. */
  void restoreState(BufferedReader fin)
    throws IOException, VisADException
  {
    prefix = fin.readLine().trim();
    int bright = Integer.parseInt(fin.readLine().trim());
    int cont = Integer.parseInt(fin.readLine().trim());
    int model = Integer.parseInt(fin.readLine().trim());
    boolean comp = fin.readLine().trim().equals("true");
    String r = fin.readLine().trim();
    String g = fin.readLine().trim();
    String b = fin.readLine().trim();
    RealType red = r.equals("null") ? null : RealType.getRealType(r);
    RealType green = g.equals("null") ? null : RealType.getRealType(g);
    RealType blue = b.equals("null") ? null : RealType.getRealType(b);
    sm.restoreState(fin);
    toolColor.setColors(bright, cont, model, comp, red, green, blue);
  }


  // -- HELPER METHODS --

  /** Brings up a window detailing basic program usage. */
  private void doHelp(int tab) {
    final JFrame frame = this;
    final int ftab = tab;
    Util.invoke(false, new Runnable() {
      public void run() { help.showWindow(frame, ftab); }
    });
  }


  // -- MAIN --

  /** Launches the VisBio GUI. */
  public static void main(String[] args) throws Exception {
    final VisBio bio = new VisBio();
    bio.pack();
    bio.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { bio.fileExit(); }
    });
    Util.centerWindow(bio);
    bio.show();
    if (bio.options.isQTAuto()) {
      bio.options.searchQT();
      bio.options.writeIni();
    }
    bio.state.checkState();
  }

}
