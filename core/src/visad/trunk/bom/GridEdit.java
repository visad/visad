//
// GridEdit.java
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

package visad.bom;

import visad.*;
import visad.util.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

/**
</pre>
   GridEdit is the VisAD class for warping and modifying fields.<p>
   Construct a GridEdit object linked to a 2-D grid [a FlatField
   with MathType ((x, y) -> range)]) or a sequence of 2-D grids {a FieldImpl
   with MathType (t -> ((x, y) -> range))], and a DisplayImpl.
   The grid or grids must all have the same
   Linear domain Set (Linear2DSet or LinearNDSet with domain dimension = 2),
   and the domain must be mapped to two of XAxis, YAxis and ZAxis. If a
   sequence of grids, the sequence domain must be mapped to Animation. The
   grid may have any number of range RealTypes.

   The GridEdit object operates in a sequence:
   1. Invokes its start() method to start.
   2. User drags a grid sector rectangle with the right mouse button.
      This rectangle must lie inside the grid.
   3. When the user releases, the rectangle appears fixed over the grid.
   4. The user can change to a different time step, and drag the rectangle
      by one of its corners. On release, it must lie inside the grid.
   5. The source grid sector is pasted into the destination, within blending
      over a certain width near the destination border.
   6. The user can change time step or drag the rectangle any number of
      times: each time the previous paste is undone and the source grid
      sector is pasted into the new time and location.
   7. At any point after start(), the application can invoke stop() to
      stop the process.
   8. After a source rectangle has been pasted, the application can invoke
      undo() to undo the paste and stop the process.
   9. The process can be restarted by invoking start(), any number of times.
   10. At any point, the application can invoke setBlend() to change the
       width of the blend region.

   The main() method illustrates a simple GUI and test case with a sequnece
   of grids. Run 'java visad.bom.GridEdit' to test with contour
   values, and run 'java visad.bom.GridEdit 1' to test with color
   values.
</pre>
*/
public class GridEdit extends Object implements ActionListener {

  private boolean debug = true;

  private Field grids = null;
  private DisplayImpl display = null;
  private int blend = 0;

  private Object lock = new Object();

  private RealType t = null; // non-null if animation
  private RealType x = null;
  private RealType y = null;
  private RealTupleType xy = null;
  private MathType range = null; // RealType or RealTupleType
  private int rangedim = 0;
  private int nts = 0; // number of steps in sequence

  Set tset = null; // t domain Set
  GriddedSet xyset = null; // (x, y) domain Set
  int nx, ny; // dimensions of xyset
  int nb; // number of bounbary points in xyset
  float[][] samplesb; // boundary locations of xyset
  FunctionType grid_type;
  FunctionType move_type;

  float[][] cut = null;
  float[][] replaced = null;
  FlatField replacedff = null;
  FlatField savedff = null;

  AnimationControl acontrol = null;

  ScalarMap tmap = null;
  ScalarMap xmap = null;
  ScalarMap ymap = null;
  DisplayTupleType xtuple = null;
  DisplayTupleType ytuple = null;

  private final static int NPICKS = 20;
  private CellImpl cell_rbl = null;
  private PickCell[] cell_picks = new PickCell[NPICKS];

  private DataReferenceImpl ref_rbl = null;
  private DataReferenceImpl[] ref_picks = new DataReferenceImpl[NPICKS];

  private RubberBandLineRendererJ3D rblr = null;
  private PickManipulationRendererJ3D[] rend_picks = new PickManipulationRendererJ3D[NPICKS];

  private GridEdit thiscp = null;

  public GridEdit(Field gs, DisplayImplJ3D d)
         throws VisADException, RemoteException {
    this(gs, d, 0);
  }

  /**
<pre>
     gs has MathType (t -> ((x, y) -> v)) or ((x, y) -> v)
     conditions on gs and display:
     1. x and y mapped to XAxis, YAxis, ZAxis
     2. (x, y) domain LinearSet
     3. if (t -> ...), then t is mapped to Animation
     b is width of blend region
</pre>
  */
  public GridEdit(Field gs, DisplayImplJ3D d, int b)
         throws VisADException, RemoteException {
    grids = gs;
    display = d;
    if (b >= 0) blend = b;

    thiscp = this;

    FunctionType gstype = (FunctionType) gs.getType();
    RealTupleType domain = gstype.getDomain();
    int domdim = domain.getDimension();
    if (domdim == 1) {
      t = (RealType) domain.getComponent(0);
      tset = gs.getDomainSet();
      grid_type = (FunctionType) gstype.getRange();
      xy = grid_type.getDomain();
      int dim = xy.getDimension();
      if (dim != 2) {
        throw new VisADException("bad grid Field domain dimension: " + dim);
      }
      range = grid_type.getRange();
      nts = tset.getLength();
      for (int i=0; i<nts; i++) {
        FlatField ff = (FlatField) gs.getSample(i);
        Set s = ff.getDomainSet();
        if (!(s instanceof GriddedSet)) {
          throw new VisADException("grid set must be GriddedSet");
        }
        if (xyset == null) {
          xyset = (GriddedSet) s;
        }
        else {
          if (!xyset.equals(s)) {
            throw new VisADException("grid sets must match in animation");
          }
        }
      }
    }
    else if (domdim == 2) {
      t = null;
      tset = null;
      xy = domain;
      grid_type = gstype;
      range = gstype.getRange();
      Set s = gs.getDomainSet();
      if (!(s instanceof GriddedSet)) {
        throw new VisADException("grid set must be GriddedSet");
      }
      xyset = (GriddedSet) s;
    }
    else {
      throw new VisADException("bad grid Field domain dimension: " + domdim);
    }
    x = (RealType) xy.getComponent(0);
    y = (RealType) xy.getComponent(1);
    nx = xyset.getLength(0);
    ny = xyset.getLength(1);

    if (range instanceof RealType) {
      rangedim = 1;
    }
    else if (range instanceof RealTupleType) {
      rangedim = ((RealTupleType) range).getDimension();
    }
    else {
      throw new VisADException("bad grid Field range type: " + range);
    }

    Vector scalar_map_vector = display.getMapVector();
    Enumeration enum = scalar_map_vector.elements();
    while (enum.hasMoreElements()) {
      ScalarMap map = (ScalarMap) enum.nextElement();
      ScalarType scalar = map.getScalar();
      DisplayRealType dreal = map.getDisplayScalar();
      DisplayTupleType tuple = dreal.getTuple();
      if (scalar.equals(t)) {
        if (Display.Animation.equals(dreal)) {
          tmap = map;
          acontrol = (AnimationControl) tmap.getControl();
        }
      }
      else if (tuple != null &&
               (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
                (tuple.getCoordinateSystem() != null &&
                 tuple.getCoordinateSystem().getReference().equals(
                 Display.DisplaySpatialCartesianTuple)))) { // spatial
        if (scalar.equals(x)) {
          xmap = map;
          xtuple = tuple;
        }
        else if (scalar.equals(y)) {
          ymap = map;
          ytuple = tuple;
        }
      }
    }
    if (xmap == null || ymap == null || xtuple != ytuple) {
      throw new VisADException("grid domain RealTypes must be mapped to " +
                               "spatial DisplayRealTypes from the same DisplayTupleType");
    }
    if (t != null && tmap == null) {
      throw new VisADException("grid sequence must be mapped to Animation");
    }


    for (int i=0; i<NPICKS; i++) {
      final int ii = i;
      ref_picks[i] = new DataReferenceImpl("pick" + i);
      rend_picks[i] = new PickManipulationRendererJ3D();
      rend_picks[i].suppressExceptions(true);
      rend_picks[i].toggle(false);
      cell_picks[i] = new PickCell(ref_picks[i],  rend_picks[i]);
      cell_picks[i].setSkip();
      cell_picks[i].addReference(ref_picks[i]);
      display.addReferences(rend_picks[i], ref_picks[i]);

    }


    ref_rbl = new DataReferenceImpl("rbl");

    rblr = new RubberBandLineRendererJ3D(x, y);
    display.addReferences(rblr, ref_rbl);
    rblr.suppressExceptions(true);
    rblr.toggle(false);

    // rubber band line release
    cell_rbl = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        synchronized (lock) {
          Set set = (Set) ref_rbl.getData();
          if (set == null) return;
          float[][] samples = set.getSamples();
          if (samples == null) return;
          // make sure both ends of set are within grid domain
          int[] indices = xyset.valueToIndex(samples);
          if (indices[0] < 0 || indices[1] < 0) return;

          int index = -1;
          for (int i=0; i<NPICKS; i++) {
            if (ref_picks[i].getData() == null) {
              index = i;
              cell_picks[i].setSkip();
              ref_picks[i].setData(set);
              rend_picks[i].toggle(true);
              break;
            }
          }
        }
      }
    };


    nb = 2 * (nx + ny) - 4; // number of grid border points
    int[] indicesb = new int[nb];
    for (int i=0; i<nx; i++) {
      indicesb[i] = i;
      indicesb[nx + i] = (ny - 1) * nx + i;
    }
    int b0 = 2 * nx - 1;
    int b1 = b0 + ny - 2;
    for (int i=1; i<(ny-1); i++) {
      indicesb[b0 + i] = i * nx;
      indicesb[b1 + i] = i * nx + nx - 1;
    }
    samplesb = xyset.indexToValue(indicesb);
    RealType xmove = RealType.getRealType("xmove");
    RealType ymove = RealType.getRealType("ymove");
    RealTupleType xymove = new RealTupleType(xmove, ymove);
    move_type = new FunctionType(xy, xymove);
  }

  public void start() throws VisADException, RemoteException {
    synchronized (lock) {
      cell_rbl.addReference(ref_rbl);
      Gridded2DSet dummy_set = new Gridded2DSet(xy, null, 1);
      ref_rbl.setData(dummy_set);
      rblr.toggle(true);
    }
  }

  public void stop() throws VisADException, RemoteException {
    synchronized (lock) {
      int np = 0; // number of pick points
      for (int i=0; i<NPICKS; i++) {
        if (ref_picks[i].getData() != null) np++;
      }
      if (np != 0) {

        FlatField ff = null;
        int index;
        if (t != null) {
          index = getAnimationIndex();
          if (index < 0 || index >= nts) return;
          ff = (FlatField) grids.getSample(index);
        }
        else {
          ff = (FlatField) grids;
        }
        savedff = new FlatField(grid_type, xyset);
        savedff.setSamples(ff.getFloats(false), false);

        Set[] sets = new Set[np];
        int k = 0;
        for (int i=0; i<NPICKS; i++) {
          if (ref_picks[i].getData() != null) {
            sets[k++] = (Set) ref_picks[i].getData();
          }
        }
        int ns = nb + np;
        float[][] mover = new float[2][ns];
        float[][] moved = new float[2][ns];
        for (int i=0; i<nb; i++) {
          moved[0][i] = samplesb[0][i];
          moved[1][i] = samplesb[1][i];
          mover[0][i] = 0.0f;
          mover[1][i] = 0.0f;
        }
        for (int i=0; i<np; i++) {
          int ip = nb + i;
          float[][] samples = sets[i].getSamples(false);
          moved[0][ip] = samples[0][1];
          moved[1][ip] = samples[1][1];
          mover[0][ip] = samples[0][0] - samples[0][1];
          mover[1][ip] = samples[1][0] - samples[1][1];
        }
        Irregular2DSet iset = new Irregular2DSet(xy, moved);
        FlatField moveff = new FlatField(move_type, iset);
        moveff.setSamples(mover);
        FlatField move_interp = (FlatField) moveff.resample(xyset);
        float[][] bases = xyset.getSamples(true); // copy
        float[][] offsets = move_interp.getFloats(false);
        for (int i=0; i<nx*ny; i++) {
/*
System.out.println("bases[" + i +  "] = " + bases[0][i] + " " + bases[1][i] +
                   " offsets[" + i +  "] = " + offsets[0][i] + " " + offsets[1][i]);
*/
          bases[0][i] += offsets[0][i];
          bases[1][i] += offsets[1][i];
        }
        Gridded2DSet warpset =
          new Gridded2DSet(xy, bases, nx, ny, null, null, null, false, false);
        FlatField warpff = (FlatField) ff.resample(warpset);
        ff.setSamples(warpff.getFloats(false), false);
        replacedff = ff;
      } // end if (np != 0)


      display.disableAction();
      rblr.toggle(false);
      for (int i=0; i<NPICKS; i++) {
        cell_picks[i].setSkip();
        ref_picks[i].setData(null);
        rend_picks[i].toggle(true);
      }
      display.enableAction();
 
      try { cell_rbl.removeReference(ref_rbl); }
      catch (ReferenceException e) { }

    }
  }

  public void undo() throws VisADException, RemoteException {
    synchronized (lock) {
      if (replacedff != null) {
        float[][] samples = savedff.getFloats(false);
        replacedff.setSamples(samples, false);
      }
      replacedff = null;
    }
    stop();
  }

  private int getAnimationIndex() throws VisADException {
    int[] indices = {acontrol.getCurrent()};
    Set aset = acontrol.getSet();
    double[][] values = aset.indexToDouble(indices);
    int[] tindices = tset.doubleToIndex(values);
    return tindices[0];
  }

  private static final int NSTAS = 64; // actually NSTAS * NSTAS
  private static final int NTIMES = 10;

  public static void main(String args[])
         throws VisADException, RemoteException {

    // construct RealTypes for wind record components
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealTupleType xy = new RealTupleType(x, y);
    RealType windx = RealType.getRealType("windx",
                          CommonUnit.meterPerSecond);     
    RealType windy = RealType.getRealType("windy",
                          CommonUnit.meterPerSecond);     
    RealType red = RealType.getRealType("red");
    RealType green = RealType.getRealType("green");

    // EarthVectorType extends RealTupleType and says that its
    // components are vectors in m/s with components parallel
    // to Longitude (positive east) and Latitude (positive north)
    EarthVectorType windxy = new EarthVectorType(windx, windy);

    RealType time = RealType.Time;
    double startt = new DateTime(1999, 122, 57060).getValue();
    Linear1DSet time_set = new Linear1DSet(time, startt, startt + 2700.0, NTIMES);

    Linear2DSet grid_set = new Integer2DSet(xy, NSTAS, NSTAS);

    RealTupleType tuple_type = new RealTupleType(new RealType[]
             {lon, lat, windx, windy, red, green});

    FunctionType field_type = new FunctionType(xy, tuple_type);
    FunctionType seq_type = new FunctionType(time, field_type);

    // construct first Java3D display and mappings that govern
    // how wind records are displayed
    DisplayImplJ3D display1 =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap ymap = new ScalarMap(y, Display.YAxis);
    display1.addMap(ymap);
    ScalarMap xmap = new ScalarMap(x, Display.XAxis);
    display1.addMap(xmap);

    ScalarMap cmap = null;
    if (args.length > 0) {
      cmap = new ScalarMap(windy, Display.RGB);
    }
    else {
      cmap = new ScalarMap(windy, Display.IsoContour);
    }
    display1.addMap(cmap);

    ScalarMap amap = new ScalarMap(time, Display.Animation);
    display1.addMap(amap);
    AnimationControl acontrol = (AnimationControl) amap.getControl();
    acontrol.setStep(500);

    // create an array of NSTAS by NSTAS winds
    FieldImpl field = new FieldImpl(seq_type, time_set);
    double[][] values = new double[6][NSTAS * NSTAS];
    for (int k=0; k<NTIMES; k++) {
      FlatField ff = new FlatField(field_type, grid_set);
      int m = 0;
      for (int i=0; i<NSTAS; i++) {
        for (int j=0; j<NSTAS; j++) {

          double u = 2.0 * i / (NSTAS - 1.0) - 1.0;
          double v = 2.0 * j / (NSTAS - 1.0) - 1.0;
  
          // each wind record is a Tuple (lon, lat, (windx, windy), red, green)
          // set colors by wind components, just for grins
          values[0][m] = 10.0 * u;
          values[1][m] = 10.0 * v - 40.0;
          double fx = k + 30.0 * u;
          double fy = 30.0 * v;
          double fd =
            Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy) + k * 15.0;
          double fs = Math.sqrt(fx * fx + fy * fy);
          values[2][m] = fd;
          values[3][m] = fs;
          values[4][m] = u;
          values[5][m] = v;
          m++;
        }
      }
      ff.setSamples(values);
      field.setSample(k, ff);
    }

    DataReferenceImpl seq_ref = new DataReferenceImpl("seq");
    seq_ref.setData(field);
    display1.addReference(seq_ref);

    final GridEdit cp = new GridEdit(field, display1);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test CollectiveBarbManipulation");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    JPanel panel1 = new JPanel();
    panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));


    panel1.add(display1.getComponent());
    panel1.setMaximumSize(new Dimension(400, 600));

    JPanel panel3 = new JPanel();
    panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
    final JButton start = new JButton("start");
    start.addActionListener(cp);
    start.setActionCommand("start");
    final JButton stop = new JButton("stop");
    stop.addActionListener(cp);
    stop.setActionCommand("stop");
    final JButton undo = new JButton("undo");
    undo.addActionListener(cp);
    undo.setActionCommand("undo");
    panel3.add(start);
    panel3.add(stop);
    panel3.add(undo);

    panel2.add(new AnimationWidget(amap));
    if (args.length > 0) {
      LabeledColorWidget lcw = new LabeledColorWidget(cmap);
      lcw.setMaximumSize(new Dimension(400, 200));
      panel2.add(lcw);
    }
    else {
      ContourWidget cw = new ContourWidget(cmap);
      cw.setMaximumSize(new Dimension(400, 200));
      panel2.add(cw);
    }
    panel2.add(new JLabel(" "));
    // panel2.add(blend_slider);
    panel2.add(new JLabel(" "));
    panel2.add(panel3);
    panel2.setMaximumSize(new Dimension(400, 600));

    panel.add(panel1);
    panel.add(panel2);
    frame.getContentPane().add(panel);

    // set size of JFrame and make it visible
    frame.setSize(800, 600);
    frame.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("start")) {
      try {
        start();
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
    else if (cmd.equals("stop")) {
      try {
        stop();
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
    else if (cmd.equals("undo")) {
      try {
        undo();
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
  }

}

class PickCell extends CellImpl {

  private boolean skip;
  private DataRenderer rend;
  private DataReferenceImpl ref;

  public PickCell(DataReferenceImpl rf, DataRenderer rd) {
    rend = rd;
    ref = rf;
    skip = true;
  }

  public void doAction() throws VisADException, RemoteException {
    if (skip) {
       skip = false;
    }
    else {
      rend.toggle(false);
      skip = true;
      ref.setData(null);
    }
  }

  public void setSkip() {
    skip = true;
  }
}

