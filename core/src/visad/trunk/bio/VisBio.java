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
  protected static final String TITLE = "VisBio";

  /** Application version. */
  protected static final String VERSION = "v1.0 beta 3";

  /** Flag for enabling or disabling Java3D, for debugging. */
  protected static final boolean ALLOW_3D = true;

  /** Tool panel names. */
  protected static final String[] TOOL_PANELS = {
    "View", "Color", "Align", "Measure"
  };

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

  /** Object for handling slice logic. */
  SliceManager sm;

  /** Object for handling measurement logic. */
  MeasureManager mm;

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

  /** Panel containing all components. */
  private JPanel pane;

  /** Panel containing VisAD displays. */
  private JPanel displayPane;

  /** Tabbed pane containing tool panels. */
  private JTabbedPane tabs;

  /** Frames containing tool panels. */
  private JFrame[] toolFrames;

  /** Panels containing tool panels. */
  private JPanel[] toolPanes;

  /** Menu item for exporting data. */
  private JMenuItem fileExport;

  /** File chooser for snapshots. */
  private JFileChooser snapBox;

  /** File chooser for state saves. */
  private JFileChooser stateBox;


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

  /** Whether tool panels are located in separate, floating windows. */
  private boolean floating;


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

    // file menu
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

    // edit menu
    addMenuItem("Edit", "Undo", "editUndo", 'u');

    // window menu
    for (int i=0; i<TOOL_PANELS.length; i++) {
      String t = TOOL_PANELS[i];
      addMenuItem("Window", t, "window" + t, t.charAt(0));
    }

    // help menu
    addMenuItem("Help", "Overview", "helpOverview", 'o');
    addMenuItem("Help", "QuickTime", "helpQuickTime", 'q');
    addMenuSeparator("Help");
    for (int i=0; i<TOOL_PANELS.length; i++) {
      String t = TOOL_PANELS[i];
      addMenuItem("Help", t, "help" + t, t.charAt(0));
    }
    addMenuSeparator("Help");
    addMenuItem("Help", "About", "helpAbout", 'a');

    // lay out components
    pane = new JPanel();
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
    displayPane.add(display2.getComponent());
    if (display3 != null) {
      GraphicsModeControl gmc = display3.getGraphicsModeControl();
      gmc.setColorMode(GraphicsModeControl.SUM_COLOR_MODE);
      gmc.setLineWidth(2.0f);
      DisplayRendererJ3D renderer =
        (DisplayRendererJ3D) display3.getDisplayRenderer();
      renderer.setPickThreshhold(Float.MAX_VALUE);
      displayPane.add(display3.getComponent());
      displayPane.add(previewPane);
    }

    // logic managers
    sm = new SliceManager(this);
    mm = new MeasureManager(this);
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

    // tool panel containers
    tabs = new JTabbedPane();
    toolFrames = new JFrame[4];
    toolPanes = new JPanel[4];
    for (int i=0; i<4; i++) {
      toolFrames[i] = new JFrame(TOOL_PANELS[i]);
      toolFrames[i].getContentPane().setLayout(new BorderLayout());
      toolPanes[i] = new JPanel();
      toolPanes[i].setLayout(new BorderLayout());
      JScrollPane scroll = new JScrollPane(toolPanes[i]);
      toolFrames[i].getContentPane().add(scroll, BorderLayout.CENTER);
    }

    // tool panels
    toolView = new ViewToolPanel(this);
    toolColor = new ColorToolPanel(this);
    toolAlign = new AlignToolPanel(this);
    toolMeasure = new MeasureToolPanel(this);
    floating = !options.isFloating();
    setFloating(!floating); // force update

    // snapshot file chooser
    snapBox = new JFileChooser();
    snapBox.addChoosableFileFilter(new ExtensionFileFilter(
      new String[] {"jpg", "jpeg"}, "JPEG files"));
    snapBox.addChoosableFileFilter(new ExtensionFileFilter(
      "raw", "RAW files"));
    snapBox.addChoosableFileFilter(new ExtensionFileFilter(
      new String[] {"tif", "tiff"}, "TIFF files"));

    // save state file chooser
    stateBox = new JFileChooser();
    stateBox.addChoosableFileFilter(new ExtensionFileFilter(
      "txt", "VisBio state files"));
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

  /** Sets whether control panels are separate, floating windows. */
  public void setFloating(boolean floating) {
    if (this.floating == floating) return;
    this.floating = floating;

    final ToolPanel[] panels = {toolView, toolColor, toolAlign, toolMeasure};
    final String[] tips = {
      "Controls for the displays",
      "Controls for manipulating color",
      "Controls for orienting data",
      "Controls for measuring data"
    };
    if (floating) {
      pane.remove(tabs);
      tabs.removeAll();
      for (int i=0; i<panels.length; i++) {
        toolPanes[i].add(panels[i], BorderLayout.CENTER);
      }
    }
    else {
      for (int i=0; i<panels.length; i++) {
        toolPanes[i].removeAll();
        toolFrames[i].hide();
        tabs.addTab(TOOL_PANELS[i], null, panels[i], tips[i]);
      }
      pane.add(tabs, BorderLayout.EAST);
    }
    doPack();
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
    state.saveState();
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
    int rval = stateBox.showOpenDialog(this);
    if (rval == JFileChooser.APPROVE_OPTION) {
      setWaitCursor(true);
      state.restoreState(stateBox.getSelectedFile());
      setWaitCursor(false);
    }
  }

  /** Saves the current state to a text file specified by the user. */
  public void fileSave() {
    int rval = stateBox.showSaveDialog(this);
    if (rval == JFileChooser.APPROVE_OPTION) {
      setWaitCursor(true);
      File file = stateBox.getSelectedFile();
      if (file.getName().indexOf(".") < 0) {
        file = new File(file.getAbsolutePath() + ".txt");
      }
      state.saveState(file);
      setWaitCursor(false);
    }
  }

  /** Saves a snapshot of the displays to a file specified by the user. */
  public void fileSnap() {
    int rval = snapBox.showSaveDialog(this);
    if (rval != JFileChooser.APPROVE_OPTION) return;

    setWaitCursor(true);

    // determine file type
    final String file = snapBox.getSelectedFile().getPath();
    String ext = "";
    int dot = file.indexOf(".");
    if (dot >= 0) ext = file.substring(dot + 1).toLowerCase();
    final boolean tiff = ext.equals("tif") || ext.equals("tiff");
    final boolean jpeg = ext.equals("jpg") || ext.equals("jpeg");
    final boolean raw = ext.equals("raw");
    if (!tiff && !jpeg && !raw) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(this, "Invalid filename (" +
        file + "): " + "extension must be TIFF, JPEG or RAW.",
        "Cannot export snapshot", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // construct output image
    final boolean has2 = display2.getComponent().isVisible();
    final boolean has3 = display3 != null &&
      display3.getComponent().isVisible();
    if (!has2 && !has3) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(this, "No displays are visible.",
        "Cannot export snapshot", JOptionPane.ERROR_MESSAGE);
      return;
    }

    Thread t = new Thread(new Runnable() {
      public void run() {
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
        g.dispose();

        // save image to disk
        FileSaver saver = new FileSaver(new ImagePlus("null", img));
        if (tiff) saver.saveAsTiff(file);
        else if (jpeg) saver.saveAsJpeg(file);
        else if (raw) saver.saveAsRaw(file);

        setWaitCursor(false);
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
    state.checkSave();
    state.destroy();
    System.exit(0);
  }

  /** Undoes the last action taken. */
  public void editUndo() { state.undo(); }

  /** Displays or switches to the View tool panel. */
  public void windowView() { doWindow(0); }

  /** Displays or switches to the Color tool panel. */
  public void windowColor() { doWindow(1); }

  /** Displays or switches to the Align tool panel. */
  public void windowAlign() { doWindow(2); }

  /** Displays or switches to the Measure tool panel. */
  public void windowMeasure() { doWindow(3); }

  /** Brings up the help window on the Overview tab. */
  public void helpOverview() { doHelp(0); }

  /** Brings up the help window on the QuickTime tab. */
  public void helpQuickTime() { doHelp(1); }

  /** Brings up the help window on the View tab. */
  public void helpView() { doHelp(2); }

  /** Brings up the help window on the Color tab. */
  public void helpColor() { doHelp(3); }

  /** Brings up the help window on the Align tab. */
  public void helpAlign() { doHelp(4); }

  /** Brings up the help window on the Measure tab. */
  public void helpMeasure() { doHelp(5); }

  /** Brings up the help window on the About tab. */
  public void helpAbout() { doHelp(6); }


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
    fout.println("# " + TITLE + " " + VERSION +
      " state file - written at " + Util.getTimestamp());
    fout.println(prefix);
    fout.println(brightness);
    fout.println(contrast);
    fout.println(model);
    fout.println(composite);
    fout.println(red == null ? "null" : red.getName());
    fout.println(green == null ? "null" : green.getName());
    fout.println(blue == null ? "null" : blue.getName());
    sm.saveState(fout);
    mm.saveState(fout);
  }

  /** Restores the current program state from the given input stream. */
  void restoreState(BufferedReader fin)
    throws IOException, VisADException
  {
    String header = fin.readLine();
    if (!header.startsWith("# " + TITLE + " " + VERSION + " state file")) {
      final JFrame frame = this;
      Util.invoke(false, new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(frame,
            "State data is not a " + TITLE + " " + VERSION + " state file",
            "Cannot restore state", JOptionPane.ERROR_MESSAGE);
        }
      });
      return;
    }
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
    toolColor.setColors(bright, cont, model, comp, red, green, blue);
    sm.restoreState(fin);
    mm.restoreState(fin);
  }


  // -- HELPER METHODS --

  /** Displays or switches to the given tool panel. */
  private void doWindow(int tab) {
    if (floating) {
      toolFrames[tab].pack();
      Point loc = getLocation();
      int x = loc.x + getSize().width;
      int y = loc.y;
      Dimension fsize = toolFrames[tab].getSize();
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      if (x + fsize.width > screen.width) x = screen.width - fsize.width;
      if (y + fsize.height > screen.height) y = screen.height - fsize.height;
      toolFrames[tab].setLocation(x, y);
      toolFrames[tab].show();
    }
    else tabs.setSelectedIndex(tab);
  }

  /** Brings up a window detailing basic program usage. */
  private void doHelp(int tab) {
    final JFrame frame = this;
    final int ftab = tab;
    Util.invoke(false, new Runnable() {
      public void run() { help.showWindow(frame, ftab); }
    });
  }

  /** Packs the window, but ensure displays are square as well. */
  private void doPack() {
    pack();
    Dimension d = displayPane.getSize();
    int w = d.height / 2;
    Dimension size = getSize();
    setSize(new Dimension(size.width - d.width + w, size.height));
  }


  // -- MAIN --

  /** Launches the VisBio GUI. */
  public static void main(String[] args) throws Exception {
    final VisBio bio = new VisBio();
    bio.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { bio.fileExit(); }
    });
    bio.doPack();
    Util.centerWindow(bio);
    bio.show();
    if (bio.options.isQTAuto()) {
      bio.options.searchQT();
      bio.options.writeIni();
    }
    bio.state.checkCrash();
  }

}
