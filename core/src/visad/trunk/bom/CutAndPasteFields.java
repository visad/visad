//
// CutAndPasteFields.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

/*
sequence of operation
1. method (may be called by JButton) to start process
2. enable RubberBandBoxRendererJ3D
3. user selects rectangle
4. disable RubberBandBoxRendererJ3D
   draw draggable rectangle (drag at corner?)
5. user may drag or change time step
   drag release, updates grid at location/time
   may repeat drag or change time step
6. method (may be called by JButton) to stop process

constructor input:
1. FieldImpl: (Time -> ((x, y) -> range)) or
   FlatField: ((x, y) -> range)
2. DisplayImpl (check ScalarMaps)
*/

/**
   CutAndPasteFields is the VisAD class for cutting and pasting
   regions of fields.<p>
*/
public class CutAndPasteFields extends Object implements ActionListener {

  private boolean debug = true;

  private Field grids = null;
  private DisplayImpl display = null;

  private RealType t = null; // non-null if animation
  private RealType x = null;
  private RealType y = null;
  private RealTupleType xy = null;
  private MathType range = null; // RealType or RealTupleType
  private int rangedim = 0;
  private int nts = 0; // number of steps in sequence

  Set tset = null; // t domain Set
  Set xyset = null; // (x, y) domain Set

  AnimationControl acontrol = null;

  ScalarMap tmap = null;
  ScalarMap xmap = null;
  ScalarMap ymap = null;

  private double xlow, xhi, ylow, yhi; // rect boundaries

  private CellImpl cell_rbb = null;
  private CellImpl cell_xlyl = null;
  private CellImpl cell_xlyh = null;
  private CellImpl cell_xhyl = null;
  private CellImpl cell_xhyh = null;

  private DataReferenceImpl ref_rbb = null;
  private DataReferenceImpl ref_xlyl = null;
  private DataReferenceImpl ref_xlyh = null;
  private DataReferenceImpl ref_xhyl = null;
  private DataReferenceImpl ref_xhyh = null;
  private DataReferenceImpl ref_rect = null;

  private RubberBandBoxRendererJ3D rbbr = null;
  private BoxDragRendererJ3D xlylr = null;
  private BoxDragRendererJ3D xlyhr = null;
  private BoxDragRendererJ3D xhylr = null;
  private BoxDragRendererJ3D xhyhr = null;
  private DefaultRendererJ3D rectr = null;

  private CutAndPasteFields thiscp = null;

  /**
     gs has MathType (t -> ((x, y) -> v)) or ((x, y) -> v)
     conditions:
     1. x and y mapped to XAxis, YAxis, ZAxis
     2. (x, y) domain LinearSet
     3. if Time, it is mapped to Animation
  */
  public CutAndPasteFields(Field gs, DisplayImplJ3D d)
         throws VisADException, RemoteException {
    grids = gs;
    display = d;
    thiscp = this;

    FunctionType gstype = (FunctionType) gs.getType();
    RealTupleType domain = gstype.getDomain();
    int domdim = domain.getDimension();
    if (domdim == 1) {
      t = (RealType) domain.getComponent(0);
      tset = gs.getDomainSet();
      FunctionType gridtype = (FunctionType) gstype.getRange();
      xy = gridtype.getDomain();
      int dim = xy.getDimension();
      if (dim != 2) {
        throw new VisADException("bad grid Field domain dimension: " + dim);
      }
      range = gridtype.getRange();
      nts = tset.getLength();
      for (int i=0; i<nts; i++) {
        FlatField ff = (FlatField) gs.getSample(i);
        Set s = ff.getDomainSet();
        if (xyset == null) {
          xyset = s;
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
      range = gstype.getRange();
      xyset = gs.getDomainSet();
    }
    else {
      throw new VisADException("bad grid Field domain dimension: " + domdim);
    }
    x = (RealType) xy.getComponent(0);
    y = (RealType) xy.getComponent(1);
    if (!(xyset instanceof LinearSet)) {
      throw new VisADException("grid set must be LinearSet");
    }

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
      if (scalar.equals(t)) {
        if (Display.Animation.equals(dreal)) {
          tmap = map;
          acontrol = (AnimationControl) tmap.getControl();
        }
      }
      else if (scalar.equals(x)) {
        if (Display.XAxis.equals(dreal) ||
            Display.YAxis.equals(dreal) ||
            Display.ZAxis.equals(dreal)) {
          xmap = map;
        }
      }
      else if (scalar.equals(y)) {
        if (Display.XAxis.equals(dreal) ||
            Display.YAxis.equals(dreal) ||
            Display.ZAxis.equals(dreal)) {
          ymap = map;
        }
      }
    }
    if (xmap == null || ymap == null) {
      throw new VisADException("grid domain RealType must be mapped to " +
                               "XAxis, YAxis or ZAxis");
    }
    if (t != null && tmap == null) {
      throw new VisADException("grid sequence must be mapped to Animation");
    }

    ref_rbb = new DataReferenceImpl("rbb");
    ref_xlyl = new DataReferenceImpl("xlyl");
    ref_xlyh = new DataReferenceImpl("xlyh");
    ref_xhyl = new DataReferenceImpl("xhyl");
    ref_xhyh = new DataReferenceImpl("xhyh");
    ref_rect = new DataReferenceImpl("rect");

    rbbr = new RubberBandBoxRendererJ3D(x, y);
    display.addReferences(rbbr, ref_rbb);
    rbbr.suppressExceptions(true);
    rbbr.toggle(false);

    xlylr = new BoxDragRendererJ3D(thiscp);
    display.addReferences(xlylr, ref_xlyl);
    xlylr.suppressExceptions(true);
    xlylr.toggle(false);

    xlyhr = new BoxDragRendererJ3D(thiscp);
    display.addReferences(xlyhr, ref_xlyh);
    xlyhr.suppressExceptions(true);
    xlyhr.toggle(false);

    xhylr = new BoxDragRendererJ3D(thiscp);
    display.addReferences(xhylr, ref_xhyl);
    xhylr.suppressExceptions(true);
    xhylr.toggle(false);

    xhyhr = new BoxDragRendererJ3D(thiscp);
    display.addReferences(xhyhr, ref_xhyh);
    xhyhr.suppressExceptions(true);
    xhyhr.toggle(false);

    rectr = new DefaultRendererJ3D();
    display.addReferences(rectr, ref_rect);
    rectr.suppressExceptions(true);
    rectr.toggle(false);


    cell_xlyl = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xlyl.getData();
        double xl = ((Real) rt.getComponent(0)).getValue();
        double yl = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xl, xlow) ||
            !Util.isApproximatelyEqual(yl, ylow)) {
          xhi += (xl - xlow);
          yhi += (yl - ylow);
          xlow = xl;
          ylow = yl;
          drag();
        }
      }
    };

    cell_xlyh = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xlyh.getData();
        double xl = ((Real) rt.getComponent(0)).getValue();
        double yh = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xl, xlow) ||
            !Util.isApproximatelyEqual(yh, yhi)) {
          xhi += (xl - xlow);
          ylow += (yh - yhi);
          xlow = xl;
          yhi = yh;
          drag();
        }
      }
    };

    cell_xhyl = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xhyl.getData();
        double xh = ((Real) rt.getComponent(0)).getValue();
        double yl = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xh, xhi) ||
            !Util.isApproximatelyEqual(yl, ylow)) {
          xlow += (xh - xhi);
          yhi += (yl - ylow);
          xhi = xh;
          ylow = yl;
          drag();
        }
      }
    };

    cell_xhyh = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref_xhyh.getData();
        double xh = ((Real) rt.getComponent(0)).getValue();
        double yh = ((Real) rt.getComponent(1)).getValue();
        if (!Util.isApproximatelyEqual(xh, xhi) ||
            !Util.isApproximatelyEqual(yh, yhi)) {
          xlow += (xh - xhi);
          ylow += (yh - yhi);
          xhi = xh;
          yhi = yh;
          drag();
        }
      }
    };

    // rubber band box release
    cell_rbb = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        Set set = (Set) ref_rbb.getData();
        if (set == null) return;
        float[][] samples = set.getSamples();
        if (samples == null) return;
        cell_rbb.removeReference(ref_rbb);
        xlow = samples[0][0];
        ylow = samples[1][0];
        xhi = samples[0][1];
        yhi = samples[1][1];
        drag();
        cell_xlyl.addReference(ref_xlyl);
        cell_xlyh.addReference(ref_xlyh);
        cell_xhyl.addReference(ref_xhyl);
        cell_xhyh.addReference(ref_xhyh);

        display.disableAction();
        xlylr.toggle(true);
        xlyhr.toggle(true);
        xhylr.toggle(true);
        xhyhr.toggle(true);
        rectr.toggle(true);
        rbbr.toggle(false);
        display.enableAction();
      }
    };


  }

  private float[][] getRect() throws VisADException, RemoteException {
    FlatField ff = null;
    if (t != null) {
      int index = getAnimationIndex();
      if (index < 0 || index >= nts) return null;
      ff = (FlatField) grids.getSample(index);
    }
    else {
      ff = (FlatField) grids;
    }
    return null;
  }

  private float[][] replaceRect() throws VisADException, RemoteException {
    int index = getAnimationIndex();
    if (index < 0) return null;

    return null;
  }

  private int getAnimationIndex() throws VisADException {
    int[] indices = {acontrol.getCurrent()};
    Set aset = acontrol.getSet();
    double[][] values = aset.indexToDouble(indices);
    int[] tindices = tset.doubleToIndex(values);
    return tindices[0];
  }

  public void start() throws VisADException, RemoteException {
    cell_rbb.addReference(ref_rbb);
    Gridded2DSet dummy_set = new Gridded2DSet(xy, null, 1);
    ref_rbb.setData(dummy_set);
    rbbr.toggle(true);
  }

  private void drag() throws VisADException, RemoteException {
    display.disableAction();
    ref_xlyl.setData(new RealTuple(xy, new double[] {xlow, ylow}));
    ref_xlyh.setData(new RealTuple(xy, new double[] {xlow, yhi}));
    ref_xhyl.setData(new RealTuple(xy, new double[] {xhi, ylow}));
    ref_xhyh.setData(new RealTuple(xy, new double[] {xhi, yhi}));
    float[][] samples =
      {{(float) xlow, (float) xlow, (float) xhi, (float) xhi, (float) xlow},
       {(float) ylow, (float) yhi, (float) yhi, (float) ylow, (float) ylow}};
    ref_rect.setData(new Gridded2DSet(xy, samples, 5));
    display.enableAction();
  }

  // BoxDragRendererJ3D button release
  public void drag_release() {
/*
    try {

// need to cut and paste grid, and save replaced section

    }
    catch (VisADException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
    catch (RemoteException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
*/
  }

  public void stop() throws VisADException, RemoteException {
    display.disableAction();
    rbbr.toggle(false);
    xlylr.toggle(false);
    xlyhr.toggle(false);
    xhylr.toggle(false);
    xhyhr.toggle(false);
    rectr.toggle(false);
    ref_xlyl.setData(null);
    ref_xlyh.setData(null);
    ref_xhyl.setData(null);
    ref_xhyh.setData(null);
    ref_rect.setData(null);
    display.enableAction();

    try { cell_rbb.removeReference(ref_rbb); }
    catch (ReferenceException e) { }
    try { cell_xlyl.removeReference(ref_xlyl); }
    catch (ReferenceException e) { }
    try { cell_xlyh.removeReference(ref_xlyh); }
    catch (ReferenceException e) { }
    try { cell_xhyl.removeReference(ref_xhyl); }
    catch (ReferenceException e) { }
    try { cell_xhyh.removeReference(ref_xhyh); }
    catch (ReferenceException e) { }
  }


  private static final int NSTAS = 32; // actually NSTAS * NSTAS
  private static final int NTIMES = 10;

  public static void main(String args[])
         throws VisADException, RemoteException {

    // construct RealTypes for wind record components
    RealType x = new RealType("x");
    RealType y = new RealType("y");
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealTupleType xy = new RealTupleType(x, y);
    RealType windx = new RealType("windx",
                          CommonUnit.meterPerSecond, null);     
    RealType windy = new RealType("windy",
                          CommonUnit.meterPerSecond, null);     
    RealType red = new RealType("red");
    RealType green = new RealType("green");

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

    ScalarMap cmap = new ScalarMap(windy, Display.RGB);
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

    CutAndPasteFields cp = new CutAndPasteFields(field, display1);

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
    panel3.add(start);
    panel3.add(stop);

    panel2.add(new AnimationWidget(amap));
    LabeledColorWidget lcw = new LabeledColorWidget(cmap);
    lcw.setMaximumSize(new Dimension(400, 200));
    panel2.add(lcw);
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
  }

}

class BoxDragRendererJ3D extends DirectManipulationRendererJ3D {

  CutAndPasteFields cp;

  BoxDragRendererJ3D(CutAndPasteFields c) {
    super();
    cp = c;
  }

  /** mouse button released, ending direct manipulation */
  public void release_direct() {
    cp.drag_release();
  }
}

