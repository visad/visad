//
// Vis5DForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.vis5d;

import visad.*;
import visad.java3d.*;
import visad.data.*;
import visad.data.units.Parser;
import visad.data.units.ParseException;
import visad.util.*;
import visad.jmet.DumpType;
import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URL;

// JFC packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   Vis5DForm is the VisAD data format adapter for Vis5D files.<P>
*/
public class Vis5DForm extends Form implements FormFileInformer {

  /** from vis5d-4.3/src/v5d.h */
  private final int MAXVARS = 30;
  private final int MAXTIMES = 400;
  private final int MAXROWS = 300;
  private final int MAXCOLUMNS = 300;
  private final int MAXLEVELS = 100;
  private final int MAXPROJARGS = 100;

  private static int num = 0;

  private static boolean loaded = false;


  public Vis5DForm() {
    super("Vis5DForm" + num++);
/*
    if (!loaded) {
      System.loadLibrary("vis5d");
      loaded = true;
    }
*/
  }

  public boolean isThisType(String name) {
    return name.endsWith(".v5d");
  }

  public boolean isThisType(byte[] block) {
    String v5d = new String(block, 0, 3);
    return v5d.equals("V5D");
  }

  public String[] getDefaultSuffixes() {
    String[] suff = { "v5d" };
    return suff;
  }

  public synchronized void save(String id, Data data, boolean replace)
         throws BadFormException, IOException, RemoteException, VisADException {
    throw new UnimplementedException("Vis5DForm.save");
  }

  public synchronized void add(String id, Data data, boolean replace)
         throws BadFormException {
    throw new BadFormException("Vis5DForm.add");
  }

  public synchronized DataImpl open(String id)
         throws BadFormException, IOException, VisADException {

    Set space_set;
    V5DStruct vv;
    FunctionType v5d_type;
    int nvars;
    int grid_size;
    FunctionType grid_type;
    RealType[] vars;

    if (id == null) {
      throw new BadFormException("Vis5DForm.open: null name String");
    }
    byte[] name = id.getBytes();
    int[] sizes = new int[5];
    int[] map_proj = new int[1];
    byte[] varnames = new byte[10 * MAXVARS];
    byte[] varunits = new byte[20 * MAXVARS];
    float[] times = new float[MAXTIMES];
    float[] projargs = new float[MAXPROJARGS];
    vv = V5DStruct.v5d_open(name,
                            name.length,
                            sizes,
                            varnames,
                            varunits,
                            map_proj,
                            projargs,
                            times);
    if (sizes[0] < 1) {
      throw new BadFormException("Vis5DForm.open: bad file");
    }
    int nr = sizes[0];
    int nc = sizes[1];
    int nl = sizes[2];
    int ntimes = sizes[3];
    nvars = sizes[4];
    /*- TDR
    RealType time = RealType.getRealType("time");
     */
    RealType time = RealType.Time;

    RealType row = RealType.getRealType("row");
    RealType col = RealType.getRealType("col");
    RealType lev = RealType.getRealType("lev");
    vars = new RealType[nvars];
    for (int i=0; i<nvars; i++) {
      int k = 10 * i;
      int k2 = 20 * i;
      int m = k;
      int m2 = k2;
      while (varnames[m] != 0) {m++;}
      while (varunits[m2] != 0) {m2++;}
      String unit_spec = new String(varunits, k2, m2 - k2);
      Unit unit = null;
      if ( unit_spec != null ) {
        try {
          unit = Parser.parse(unit_spec);
        }
        catch (ParseException e) {
          System.out.println(e.getMessage());
        }
      }
      vars[i] = RealType.getRealType(new String(varnames, k, m - k), unit);
    }
    RealTupleType domain;
    if (nl > 1) {
      domain = new RealTupleType(row, col, lev);
    }
    else {
      domain = new RealTupleType(row, col);
    }
    RealTupleType range = new RealTupleType(vars);
    RealTupleType time_domain = new RealTupleType(time);
    grid_type = new FunctionType(domain, range);
    v5d_type = new FunctionType(time_domain, grid_type);


    float[][] timeses = new float[1][ntimes];
    for (int i=0; i<ntimes; i++)  {
      timeses[0][i] = times[i];
    }
    /*- TDR
    Gridded1DSet time_set =
      new Gridded1DSet(time, timeses, ntimes);
     */
    Unit v5d_time_unit = new OffsetUnit(
                             visad.data.units.UnitParser.encodeTimestamp(
                                1900, 1, 1, 0, 0, 0, 0), SI.second);
    Gridded1DSet time_set =
      new Gridded1DSet(time, timeses, ntimes,
                       null, new Unit[] {v5d_time_unit}, null);

    if (nl > 1) {
      space_set = new Integer3DSet(nr, nc, nl);
    }
    else {
      space_set = new Integer2DSet(nr, nc);
    }
    FieldImpl v5d = new FieldImpl(v5d_type, time_set);
    grid_size = nr * nc * nl;

    Vis5DFile v5dfile =
      new Vis5DFile(id, vv, space_set, grid_type, vars, nvars, grid_size);

    for (int i=0; i<ntimes; i++)
    {
      FlatField grid = getFlatField(v5dfile, i);
      v5d.setSample(i, grid, false);
    }

    return v5d;
  }

  public FlatField getFlatField(Vis5DFile v5dfile, int time_idx)
         throws VisADException, IOException, BadFormException
  {
    return makeFlatField(v5dfile, time_idx);
  }

  public static FlatField makeFlatField(Vis5DFile v5dfile, int time_idx)
         throws VisADException, IOException, BadFormException
  {
    int nvars = v5dfile.nvars;
    int grid_size = v5dfile.grid_size;
    FunctionType grid_type = v5dfile.grid_type;
    Set space_set = v5dfile.space_set;
    V5DStruct vv = v5dfile.vv;
    RealType[] vars = v5dfile.vars;


    float[][] data = new float[nvars][grid_size];
    Linear1DSet[] range_sets = new Linear1DSet[nvars];
    for (int j=0; j<nvars; j++) {
      float[] ranges = new float[2];
      vv.v5d_read(time_idx, j, ranges, data[j]);
      if (ranges[0] >= 0.99E30 && ranges[1] <= -0.99E30) {
        range_sets[j] = new Linear1DSet(0.0, 1.0, 255);
      }
      else {
        if (ranges[0] > ranges[1]) {
          throw new BadFormException("Vis5DForm.open: bad read " +
                                       vars[j].getName());
        }
        range_sets[j] =
          new Linear1DSet((double) ranges[0], (double) ranges[1], 255);
      }
      for (int k=0; k<grid_size; k++) {
        if (data[j][k] > 0.5e35) data[j][k] = Float.NaN;
      }
    }
    // FlatField grid =
    //   new FlatField(grid_type, space_set, null, null, range_sets, null);
    FlatField grid =
      new FlatField(grid_type, space_set);
    grid.setSamples(data, false);

    return grid;
  }


  public synchronized DataImpl open(URL url)
         throws BadFormException, VisADException, IOException {
    throw new UnimplementedException("Vis5DForm.open(URL url)");
  }

  public synchronized FormNode getForms(Data data) {
    return null;
  }

  /** the width and height of the UI frame */
  public static int WIDTH = 800;
  public static int HEIGHT = 600;

  /** run 'java visad.data.vis5d.Vis5DForm QLQ.v5d' to test */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args == null || args.length < 1) {
      System.out.println("run 'java visad.data.vis5d.Vis5DForm file.v5d'");
    }
    Vis5DForm form = new Vis5DForm();
    FieldImpl vis5d = null;
    try {
      vis5d = (FieldImpl) form.open(args[0]);
      DumpType.dumpMathType(vis5d.getType());
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      return;
    }
    if (vis5d == null) {
      System.out.println("bad Vis5D file read");
      return;
    }
    FunctionType vis5d_type = (FunctionType) vis5d.getType();
    System.out.println(vis5d_type);
    DataReference vis5d_ref = new DataReferenceImpl("vis5d_ref");
    vis5d_ref.setData(vis5d);
    // vis5d_ref.setData(vis5d.getSample(8));

    //
    // construct JFC user interface with JSliders linked to
    // Data objects, and embed Displays into JFC JFrame
    //

    // create a JFrame
    JFrame frame = new JFrame("Vis5D");
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    };
    frame.addWindowListener(l);
    frame.setSize(WIDTH, HEIGHT);
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);

    // create big_panel JPanel in frame
    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
    big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(big_panel);

    // create left hand side JPanel for sliders and text
    JPanel left = new JPanel(); // FlowLayout and double buffer
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setAlignmentY(JPanel.TOP_ALIGNMENT);
    left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(left);

    // construct JLabels
    // (JTextArea does not align in BoxLayout well, so use JLabels)
    left.add(new JLabel("Simple Vis5D File Viewer using VisAD - See:"));
    left.add(new JLabel("  "));
    left.add(new JLabel("  http://www.ssec.wisc.edu/~billh/visad.html"));
    left.add(new JLabel("  "));
    left.add(new JLabel("for more information about VisAD."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Space Science and Engineering Center"));
    left.add(new JLabel("University of Wisconsin - Madison"));
    left.add(new JLabel("  "));
    left.add(new JLabel("  "));
    left.add(new JLabel("Move sliders to adjust iso-surface levels"));
    left.add(new JLabel("  "));
    left.add(new JLabel("Click Animate button to toggle animation"));
    left.add(new JLabel("  "));
    left.add(new JLabel("Rotate scenes with left mouse button."));
    left.add(new JLabel("  "));
    left.add(new JLabel("  "));
    left.add(new JLabel("  "));

    // create sliders JPanel
    JPanel sliders = new JPanel();
    sliders.setName("GoesRetrieval Sliders");
    sliders.setFont(new Font("Dialog", Font.PLAIN, 12));
    sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));
    sliders.setAlignmentY(JPanel.TOP_ALIGNMENT);
    sliders.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    left.add(sliders);

    // construct JPanel and sub-panels for Displays
    JPanel display_panel = new JPanel();
    display_panel.setLayout(new BoxLayout(display_panel,
                                          BoxLayout.X_AXIS));
    display_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(display_panel);

    // create a Display and add it to panel
    DisplayImpl display = new DisplayImplJ3D("image display");
    display_panel.add(display.getComponent());

    // extract RealType components from vis5d_type and use
    // them to determine how data are displayed

    // map time to Animation
    RealType time = (RealType) vis5d_type.getDomain().getComponent(0);
    ScalarMap animation_map = new ScalarMap(time, Display.Animation);
    display.addMap(animation_map);
    // default is ON
    final AnimationControl animation_control =
      (AnimationControl) animation_map.getControl();

    // get grid type
    FunctionType grid_type = (FunctionType) vis5d_type.getRange();
    RealTupleType domain = grid_type.getDomain();
    // map grid coordinates to display coordinates
    display.addMap(new ScalarMap((RealType) domain.getComponent(0),
                                 Display.XAxis));
    display.addMap(new ScalarMap((RealType) domain.getComponent(1),
                                 Display.YAxis));
    if (domain.getDimension() > 2) {
      display.addMap(new ScalarMap((RealType) domain.getComponent(2),
                                   Display.ZAxis));
    }

    // map grid values to IsoContour
    RealTupleType range = (RealTupleType) grid_type.getRange();
    int dim = range.getDimension();
    RealType[] range_types = new RealType[dim];
    ScalarMap[] contour_maps = new ScalarMap[dim];
    ContourControl[] contour_controls = new ContourControl[dim];
    DataReference[] range_refs = new DataReferenceImpl[dim];
    for (int i=0; i<dim; i++) {
      range_types[i] = (RealType) range.getComponent(i);
      contour_maps[i] = new ScalarMap(range_types[i], Display.IsoContour);
      display.addMap(contour_maps[i]);
      contour_controls[i] = (ContourControl) contour_maps[i].getControl();
      contour_controls[i].enableContours(false);
      range_refs[i] = new DataReferenceImpl(range_types[i].getName() + "_ref");
    }

/* WLH - uncomment these for color demo images from the QLQ.v5d data set

    ScalarMap color_map = new ScalarMap(range_types[1], Display.Green);
    display.addMap(color_map);
    color_map.setRange(23.5, 0.0);
    display.addMap(new ConstantMap(0.5, Display.Red));
    display.addMap(new ConstantMap(0.5, Display.Blue));
*/

    // now Display vis5d data
    display.addReference(vis5d_ref);

    // wait for auto-scaling
    boolean scaled = false;
    double[][] ranges = new double[dim][];
    while (!scaled) {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
      }
      scaled = true;
      for (int i=0; i<dim; i++) {
        ranges[i] = contour_maps[i].getRange();
        if (ranges[i][0] != ranges[i][0] ||
            ranges[i][1] != ranges[i][1]) {
          scaled = false;
          // System.out.println("tick");
          break;
        }
      }
    }
    for (int i=0; i<dim; i++) {
      double scale = (ranges[i][1] - ranges[i][0]) / 255.0;
      int low = (int) (ranges[i][0] / scale);
      int hi = (int) (ranges[i][1] / scale);
      range_refs[i].setData(new Real(range_types[i], scale * low));
      sliders.add(new VisADSlider(range_types[i].getName(), low, hi, low, scale,
                                  range_refs[i], range_types[i]));
      sliders.add(new JLabel("  "));

      ContourCell cell =
        form. new ContourCell(contour_controls[i], range_refs[i]);
      cell.addReference(range_refs[i]);
    }

    final JToggleButton button = new JToggleButton("Animate", true);
    button.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        try {
          // boolean state = ((ToggleButtonModel) button.getModel()).isSelected();
          boolean state = button.getModel().isSelected();
          animation_control.setOn(state);
        }
        catch (VisADException ee) {
        }
        catch (RemoteException ee) {
        }
      }
    });
    sliders.add(button);

    // make the JFrame visible
    frame.setVisible(true);
  }

  class ContourCell extends CellImpl {
    ContourControl control;
    DataReference ref;
    double value;

    ContourCell(ContourControl c, DataReference r)
           throws VisADException, RemoteException {
      control = c;
      ref = r;
      value = ((Real) ref.getData()).getValue();
    }

    public void doAction() throws VisADException, RemoteException {
      double val = ((Real) ref.getData()).getValue();
      if (val == val && val != value) {
        control.setSurfaceValue((float) ((Real) ref.getData()).getValue());
        control.enableContours(true);
        value = val;
      }
    }

  }

/* here's the output:

demedici% java visad.data.vis5d.Vis5DForm SCHL.v5d
FunctionType: (time) -> FunctionType (Real): (row, col, lev) -> (U, V, W, QL, TH, Q, P, ED, F)

demedici% java visad.data.vis5d.Vis5DForm QLQ.v5d
FunctionType: (time) -> FunctionType (Real): (row, col, lev) -> (QL, Q)
demedici%

*/


  /** native method declarations */
  /** calls v5dOpenFile in v5d.c */
/*
  private native void v5d_open(byte[] name, int name_length, int[] sizes,
                               byte[] varnames, float[] times);
*/

  /** calls v5dReadGrid in v5d.c */
/*
  private native void v5d_read(int time, int var, float[] ranges, float[] data);
*/

}

