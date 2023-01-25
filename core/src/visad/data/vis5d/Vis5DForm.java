//
// Vis5DForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Hashtable;
import java.util.Enumeration;

// JFC packages
import javax.swing.*;
import javax.swing.event.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   Vis5DForm is the VisAD data format adapter for Vis5D files.<P>
*/
public class Vis5DForm extends Form implements FormFileInformer {

  /** from vis5d-4.3/src/v5d.h */
  private final int MAXVARS     = 200;
  private final int MAXTIMES    = 400;
  private final int MAXROWS     = 400;
  private final int MAXCOLUMNS  = 400;
  private final int MAXLEVELS   = 400;
  private final int MAXPROJARGS = MAXROWS+MAXCOLUMNS+1;
  private final int MAXVERTARGS = MAXLEVELS+1;

  private static int num = 0;

  private static boolean loaded = false;


  public Vis5DForm() {
    super("Vis5DForm" + num++);
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
    RealType[] vars;

    if (id == null) {
      throw new BadFormException("Vis5DForm.open: null name String");
    }

    byte[] name = id.getBytes();
    int[] sizes = new int[5];
    int[] map_proj = new int[1];
    String[] varnames = new String[MAXVARS];
    String[] varunits = new String[MAXVARS];
    int[] n_levels = new int[MAXVARS];
    int[]  vert_sys = new int[1];
    float[]  vertargs = new float[MAXVERTARGS];
 //-float[] times = new float[MAXTIMES];
    double[] times = new double[MAXTIMES];
    float[] projargs = new float[MAXPROJARGS];

    vv = V5DStruct.v5d_open(name,
                            name.length,
                            sizes,
                            n_levels,
                            varnames,
                            varunits,
                            map_proj,
                            projargs,
                            vert_sys,
                            vertargs,
                            times);

    if (sizes[0] < 1) {
      throw new BadFormException("Vis5DForm.open: bad file");
    }
    //-System.out.println("proj: "+map_proj[0]);

    int nr = sizes[0];
    int nc = sizes[1];
    int nl = sizes[2];
    int ntimes = sizes[3];
    nvars = sizes[4];

    //-System.out.println("nr: "+nr);
    //-System.out.println("nc: "+nc);
    //-System.out.println("nl: "+nl);
    //-System.out.println("ntimes: "+ntimes);
    //-System.out.println("nvars: "+nvars);

    RealType time = RealType.Time;

    RealType row = RealType.getRealType("row");
    RealType col = RealType.getRealType("col");
    //RealType lev = RealType.getRealType("lev");  not used

    vars = new RealType[nvars];

    for (int i=0; i<nvars; i++) {
      String unit_spec = varunits[i];
      Unit unit = null;
      if ( unit_spec != null ) {
        try {
          unit = Parser.parse(unit_spec);
        }
        catch (ParseException e) {
          System.out.println(e.getMessage());
        }
      }
      vars[i] = RealType.getRealType(varnames[i], unit);
      if ( vars[i] == null ) {
        vars[i] = RealType.getRealType("var"+i);
      }
    }

    double[][] proj_args =
      Set.floatToDouble(new float[][] {projargs});
    double[][] vert_args =
      Set.floatToDouble(new float[][] {vertargs});

    CoordinateSystem coord_sys =
      new Vis5DCoordinateSystem(map_proj[0], proj_args[0], nr, nc);

    RealTupleType domain;
    Vis5DVerticalSystem vert_coord_sys = null;


   /*----------------------------------------------------------
      Sort variables by n_levels.  There should only be two
      possibilities:
       (1) all variables with same n_levels.
       (2) some variables all with same n_levels, the rest with
           only one level.

      Throw BadFormException otherwise. */
    

    Hashtable var_table = new Hashtable();
    for (int i = 0; i < nvars; i++) {
      var_table.put(new Integer(n_levels[i]), new Object());
    }
    int n_var_groups = var_table.size();

    if ( n_var_groups > 2 ) {
      throw
        new BadFormException("more than two variable groups by n_levels");
    }
    else if ( n_var_groups == 0 ) {
      throw
        new BadFormException("n_var_groups == 0");
    }

    RealType[][] var_grps = new RealType[n_var_groups][];
    RealType[] tmp_r = new RealType[nvars];
    int[][] var_grps_indexes = new int[n_var_groups][];
    int[] tmp_i = new int[nvars];
    int[] var_grps_nlevels = new int[n_var_groups];
    

    Enumeration en = var_table.keys();
    for ( int grp = 0; grp < n_var_groups; grp++)
    {
      Integer key = (Integer)en.nextElement();
      int cnt = 0;
      for (int i = 0; i < nvars; i++) {
        if ( n_levels[i] == key.intValue() ) {
           tmp_r[cnt] = vars[i];
           tmp_i[cnt] = i;
           cnt++;
        }
      }
      var_grps[grp] = new RealType[cnt];
      System.arraycopy(tmp_r, 0, var_grps[grp], 0, cnt);

      var_grps_indexes[grp] = new int[cnt];
      System.arraycopy(tmp_i, 0, var_grps_indexes[grp], 0, cnt);
      var_grps_nlevels[grp] = key.intValue();
    }
    /*---------------------------------------------------------*/


  FunctionType[][] grid_type = new FunctionType[n_var_groups][];
  Vis5DFile[][] v5dfile_s = new Vis5DFile[n_var_groups][];

  int n_comps = 0;

  for ( int grp = 0; grp < n_var_groups; grp++ )
  {
    RealType[] sub_vars = var_grps[grp];
    int[] sub_vars_indexes = var_grps_indexes[grp];
    nl = var_grps_nlevels[grp];

    if (nl > 1) {
      vert_coord_sys =
        new Vis5DVerticalSystem(vert_sys[0], nl, vert_args[0]);

      RealType height = vert_coord_sys.vert_type;

      CoordinateSystem pcs =
        new CachingCoordinateSystem(
          new CartesianProductCoordinateSystem(
            new CoordinateSystem[]
              {coord_sys, vert_coord_sys.vert_cs}));
             

      domain =
        new RealTupleType( new RealType[] {row, col, height}, pcs, null);
        /*
    }
    else {
      domain = new RealTupleType(new RealType[] {row, col}, 
        new CachingCoordinateSystem(coord_sys), null);
    }

    if (nl > 1)
    {
    */
      SampledSet vert_set = vert_coord_sys.vertSet;

     /**-  Maybe sometime in the future
      RealTupleType row_col = new RealTupleType(new RealType[] {row, col});
      SampledSet row_col_set = new Integer2DSet(row_col, nr, nc);
      space_set = new ProductSet(domain,
        new SampledSet[] {row_col_set, vert_set});
      */

      if (vert_set instanceof Linear1DSet) {
        space_set =
          new Linear3DSet(domain, 
                          new Linear1DSet[] {
                              new Integer1DSet(row, nr),
                              new Integer1DSet(col, nc),
                              (Linear1DSet) vert_set},
                           (CoordinateSystem) null,
                           new Unit[] {null, null, vert_coord_sys.vert_unit},
                           (ErrorEstimate[]) null);
      }
      else {  // Gridde1DSet
        float[][] vert_samples = vert_set.getSamples();
        float[][] domain_samples = new float[3][nr*nc*nl];
        int idx = 0;
        for (int kk = 0; kk < nl; kk++) {
          for (int jj = 0; jj < nc; jj++) {
            for ( int ii = 0; ii < nr; ii++) {
              domain_samples[0][idx] = ii;
              domain_samples[1][idx] = jj;
              domain_samples[2][idx] = vert_samples[0][kk];
              idx++;
            }
          }
        }
        space_set =
          //new Gridded3DSet(domain, domain_samples, nr, nc, nl);
          new Gridded3DSet(
                  domain, domain_samples, nr, nc, nl,
                  (CoordinateSystem) null,
                  //new Unit[] {null, null, height.getDefaultUnit()},
                  new Unit[] {null, null, vert_coord_sys.vert_unit},
                  (ErrorEstimate[]) null);
      }

      
      grid_type[grp] = new FunctionType[sub_vars.length];
      v5dfile_s[grp] = new Vis5DFile[sub_vars.length];
      
      grid_size = nr * nc * nl;

      for (int k = 0; k < sub_vars.length; k++) {
        grid_type[grp][k] = new FunctionType(domain, sub_vars[k]);
        v5dfile_s[grp][k] =
          new Vis5DFile(id, vv, space_set,
                        grid_type[grp][k],
                        new RealType[] {sub_vars[k]},
                        new int[] {sub_vars_indexes[k]}, grid_size);
      }
      n_comps += grid_type[grp].length;
    }
    else 
    {
      domain = new RealTupleType(new RealType[] {row, col}, 
        new CachingCoordinateSystem(coord_sys), null);
      space_set = new Integer2DSet(domain, nr, nc);

      grid_type[grp] = new FunctionType[1];
      v5dfile_s[grp] = new Vis5DFile[1];

      RealTupleType range = new RealTupleType(sub_vars);
      grid_type[grp][0] = new FunctionType(domain, range);

      grid_size = nr * nc * nl;

      v5dfile_s[grp][0] =
        new Vis5DFile(id, vv, space_set,
           grid_type[grp][0], sub_vars, sub_vars_indexes, grid_size);

      n_comps += grid_type[grp].length;
    }
  }


    RealTupleType time_domain = new RealTupleType(time);

    MathType v5d_range;
    MathType[] range_comps = new MathType[n_comps];
    int cnt = 0;
    for ( int grp = 0; grp < grid_type.length; grp++) {
      for (int i = 0; i < grid_type[grp].length; i++) {
        range_comps[cnt++] = grid_type[grp][i];
      }
    }
    if (range_comps.length == 1) {
      v5d_range = range_comps[0];
    }
    else {
      v5d_range = new TupleType(range_comps);
    }
    v5d_type = new FunctionType(time_domain, v5d_range);



    double[][] timeses = new double[1][ntimes];
    for (int i=0; i<ntimes; i++)  {
      timeses[0][i] = times[i];
    }
    Unit v5d_time_unit = new OffsetUnit(
                             visad.data.units.UnitParser.encodeTimestamp(
                                1900, 1, 1, 0, 0, 0, 0), SI.second);
    Gridded1DDoubleSet time_set =
      new Gridded1DDoubleSet(time, timeses, ntimes,
                             null, new Unit[] {v5d_time_unit}, null);


    FieldImpl v5d = new FieldImpl(v5d_type, time_set);

    DataImpl range_data;
    for (int i=0; i<ntimes; i++)
    {
      if (range_comps.length == 1) {
        range_data = getFlatField(v5dfile_s[0][0], i);
      }
      else {
        DataImpl[] datas = new DataImpl[range_comps.length];
        cnt = 0;
        for (int j = 0; j < v5dfile_s.length; j++) {
          for (int k = 0; k < v5dfile_s[j].length; k++) {
            datas[cnt++] = getFlatField(v5dfile_s[j][k], i); 
          }
        }
        range_data = new Tuple(datas, false);
      }
      v5d.setSample(i, range_data, false);
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
    int[] vars_indexes = v5dfile.vars_indexes;


    float[][] data = new float[nvars][grid_size];
    Linear1DSet[] range_sets = new Linear1DSet[nvars];
    for (int j=0; j<nvars; j++) {
      float[] ranges = new float[2];
      vv.v5d_read(time_idx, vars_indexes[j], ranges, data[j]);
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


      //- invert rows
      float[] tmp_data = new float[grid_size];
      int[] lens = ((GriddedSet)space_set).getLengths();

      if ( lens.length == 2 ) {
        int cnt = 0;
        for ( int mm = 0; mm < lens[1]; mm++ ) {
          int start = (mm+1)*lens[0] - 1;
          for ( int nn = 0; nn < lens[0]; nn++ ) {
            tmp_data[cnt++] = data[j][start--];
          }
        }
      }
      else if ( lens.length == 3 ) {
        int cnt = 0;
        for ( int ll = 0; ll < lens[2]; ll++ ) {
          for ( int mm = 0; mm < lens[1]; mm++ ) {
            int start = ((mm+1)*lens[0] - 1) + lens[0]*lens[1]*ll;
            for ( int nn = 0; nn < lens[0]; nn++ ) {
              tmp_data[cnt++] = data[j][start--];
            }
          }
        }
      }
      System.arraycopy(tmp_data, 0, data[j], 0, grid_size);
      tmp_data = null;



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
    return open(url.toString());
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
    Vis5DForm form = new Vis5DAdaptedForm();
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
    FunctionType type = (FunctionType) vis5d.getType();
    FieldImpl new_vis5d;
    if ( type.getRange() instanceof TupleType ) {
   //-new_vis5d = (FieldImpl)vis5d.extract(20);
      new_vis5d = vis5d;
    }
    else {
      new_vis5d = vis5d;
    }
    FunctionType vis5d_type = (FunctionType) new_vis5d.getType();
    System.out.println(vis5d_type);
    DataReference vis5d_ref = new DataReferenceImpl("vis5d_ref");
    vis5d_ref.setData(new_vis5d);

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

 // frame.setVisible(true);

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
    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);
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

    /*
 //-RealTupleType reference = (domain.getCoordinateSystem()).getReference();
 // domain = reference;
    // map grid coordinates to display coordinates
 //-display.addMap(new ScalarMap((RealType) domain.getComponent(1),
    display.addMap(new ScalarMap(RealType.getRealType("col"),
                                 Display.XAxis));
 //-display.addMap(new ScalarMap((RealType) domain.getComponent(0),
    display.addMap(new ScalarMap(RealType.getRealType("row"),
                                 Display.YAxis));
//-display.addMap(new ScalarMap((RealType) domain.getComponent(2),
   display.addMap(new ScalarMap(RealType.getRealType("lev"),
                                   Display.ZAxis));
   */
   display.addMap(new ScalarMap(RealType.Latitude,  Display.YAxis));
   display.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
   display.addMap(new ScalarMap(RealType.Altitude,  Display.ZAxis));

    // map grid values to IsoContour

    int n_range_real_types = 0;
    MathType v5d_range = vis5d_type.getRange();
    RealType[] tmp = new RealType[200];
    if ( v5d_range instanceof TupleType ) {
      for ( int ii = 0; ii < ((TupleType)v5d_range).getDimension(); ii++) {
        FunctionType f_type = 
          (FunctionType)
            ((TupleType)v5d_range).getComponent(ii);
        MathType mtype = f_type.getRange();
        if (mtype instanceof TupleType) {
          int nn = ((TupleType)mtype).getDimension();
          for ( int kk = 0; kk < nn; kk++) {
            tmp[n_range_real_types++] = (RealType)((TupleType)mtype).getComponent(kk);
          }
        }
        else {
          tmp[n_range_real_types++] = (RealType)mtype;
        }
      }
    }
    else {
      MathType mtype = ((FunctionType)v5d_range).getRange();
      if (mtype instanceof TupleType) {
        int nn = ((TupleType)mtype).getDimension();
        for ( int kk = 0; kk < nn; kk++) {
          tmp[n_range_real_types++] = (RealType)((TupleType)mtype).getComponent(kk);
        }
      }
      else {
        tmp[n_range_real_types++] = (RealType)mtype;
      }
    }

    int dim = n_range_real_types;
    RealType[] range_types = new RealType[dim];
    ScalarMap[] contour_maps = new ScalarMap[dim];
    ContourControl[] contour_controls = new ContourControl[dim];
    DataReference[] range_refs = new DataReferenceImpl[dim];
    for (int i=0; i<dim; i++) {
   //-range_types[i] = (RealType) range.getComponent(i);
      range_types[i] = (RealType) tmp[i];
      contour_maps[i] = new ScalarMap(range_types[i], Display.IsoContour);
      try {
          display.addMap(contour_maps[i]);
          contour_controls[i] = (ContourControl) contour_maps[i].getControl();
          contour_controls[i].enableContours(false);
      } catch (BadMappingException bme) {;} // handle case of duplicate names
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

    final JToggleButton button = new JToggleButton("Animate", false);
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

