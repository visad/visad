//
// CutAndPasteFields.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

/**
</pre>
   CutAndPasteFields is the VisAD class for cutting and pasting
   regions of fields.<p>
   Construct a CutAndPasteFields object linked to a 2-D grid [a FlatField
   with MathType ((x, y) -> range)]) or a sequence of 2-D grids {a FieldImpl
   with MathType (t -> ((x, y) -> range))], a DisplayImpl, and an optional
   integer blend region width. The grid or grids must all have the same
   Linear domain Set (Linear2DSet or LinearNDSet with domain dimension = 2),
   and the domain must be mapped to two of XAxis, YAxis and ZAxis. If a
   sequence of grids, the sequence domain must be mapped to Animation. The
   grid may have any number of range RealTypes.

   The CutAndPasteFields object operates in a sequence:
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
   of grids. Run 'java visad.bom.CutAndPasteFields' to test with contour
   values, and run 'java visad.bom.CutAndPasteFields 1' to test with color
   values.
</pre>
*/
public class CutAndPasteFields extends Object implements ActionListener {

  private boolean debug = false;

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
  Set xyset = null; // (x, y) domain Set
  int nx, ny; // dimensions of xyset
  int rx, ry; // dimensions of cut rectangle

  float[][] cut = null;
  float[][] replaced = null;
  FlatField replacedff = null;
  int replacedixlow, replacedixhi;
  int replacediylow, replacediyhi;

  AnimationControl acontrol = null;

  ScalarMap tmap = null;
  ScalarMap xmap = null;
  ScalarMap ymap = null;

  private double xlow, xhi, ylow, yhi; // rect boundaries
  private int ixlow, ixhi, iylow, iyhi; // x and y indices

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

  public CutAndPasteFields(Field gs, DisplayImplJ3D d)
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
  public CutAndPasteFields(Field gs, DisplayImplJ3D d, int b)
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
    nx = ((LinearSet) xyset).getLinear1DComponent(0).getLength();
    ny = ((LinearSet) xyset).getLinear1DComponent(1).getLength();

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
    Enumeration en = scalar_map_vector.elements();
    while (en.hasMoreElements()) {
      ScalarMap map = (ScalarMap) en.nextElement();
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
        synchronized (lock) {
          RealTuple rt = (RealTuple) ref_xlyl.getData();
          if (rt == null) return;
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
      }
    };

    cell_xlyh = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        synchronized (lock) {
          RealTuple rt = (RealTuple) ref_xlyh.getData();
          if (rt == null) return;
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
      }
    };

    cell_xhyl = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        synchronized (lock) {
          RealTuple rt = (RealTuple) ref_xhyl.getData();
          if (rt == null) return;
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
      }
    };

    cell_xhyh = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        synchronized (lock) {
          RealTuple rt = (RealTuple) ref_xhyh.getData();
          if (rt == null) return;
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
      }
    };

    // rubber band box release
    cell_rbb = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        synchronized (lock) {
          Set set = (Set) ref_rbb.getData();
          if (set == null) return;
          float[][] samples = set.getSamples();
          if (samples == null) return;
  
          xlow = samples[0][0];
          ylow = samples[1][0];
          xhi = samples[0][1];
          yhi = samples[1][1];
  
          if (xlow > xhi) {
            double t = xlow;
            xlow = xhi;
            xhi = t;
          }
          if (ylow > yhi) {
            double t = ylow;
            ylow = yhi;
            yhi = t;
          }
  
          if (!getIndices(true)) {
            if (debug) System.out.println("bad box");
            return;
          }
          getRect();
          replacedff = null;
   
          cell_rbb.removeReference(ref_rbb);
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
      }
    };
  }

  public void start() throws VisADException, RemoteException {
    synchronized (lock) {
      cell_rbb.addReference(ref_rbb);
      Gridded2DSet dummy_set = new Gridded2DSet(xy, null, 1);
      ref_rbb.setData(dummy_set);
      rbbr.toggle(true);
    }
  }

  public void stop() throws VisADException, RemoteException {
    synchronized (lock) {
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
  }

  public void undo() throws VisADException, RemoteException {
    synchronized (lock) {
      if (replacedff != null) {
        float[][] samples = replacedff.getFloats(false);
        for (int ix=replacedixlow; ix<=replacedixhi; ix++) {
          for (int iy=replacediylow; iy<=replacediyhi; iy++) {
            int i = ix + nx * iy;
            int j = (ix - replacedixlow) + rx * (iy - replacediylow);
            for (int k=0; k<rangedim; k++) samples[k][i] = replaced[k][j];
          }
        }
        replacedff.setSamples(samples, false);
      }
      replacedff = null;
    }
    stop();
  }

  public void setBlend(int b) {
    if (b >= 0) blend = b;
  }

  /**
   set ixlow, iylow, ixhi, iyhi from xlow, ylow, xhi, yhi
   if (rubber) set rx, ry
   else make sure ixlow, iylow, ixhi, iyhi are consistent with rx, ry
   */
  private boolean getIndices(boolean rubber) throws VisADException {
    float[][] samples = {{(float) xlow, (float) xhi},
                         {(float) ylow, (float) yhi}};
    int[] indices = xyset.valueToIndex(samples);
    int indexlow = indices[0];
    int indexhi = indices[1];
    if (indexlow < 0 || indexhi < 0) return false;

    if (rubber) {
      samples = xyset.indexToValue(indices);
      xlow = samples[0][0];
      ylow = samples[1][0];
      xhi = samples[0][1];
      yhi = samples[1][1];
    }

    if (indexlow > indexhi) {
      int t= indexlow;
      indexlow = indexhi;
      indexhi = t;
    }

    // i = ix + nx * iy
    iylow = indexlow / nx;
    ixlow = indexlow % nx;
    iyhi = indexhi / nx;
    ixhi = indexhi % nx;
    if (ixlow > ixhi) {
      int t= ixlow;
      ixlow = ixhi;
      ixhi = t;
    }
    if (iylow > iyhi) {
      int t= iylow;
      iylow = iyhi;
      iyhi = t;
    }

    if (rubber) {
      rx = (ixhi - ixlow) + 1;
      ry = (iyhi - iylow) + 1;
    }
    else {
      int tx = (ixhi - ixlow) + 1;
      int ty = (iyhi - iylow) + 1;
      if (rx != tx) {
        if ((ixlow + rx - 1) < nx) ixhi = ixlow + rx - 1;
        else ixlow = ixhi - (rx - 1);
      }
      if (ry != ty) {
        if ((iylow + ry - 1) < ny) iyhi = iylow + ry - 1;
        else iylow = iyhi - (ry - 1);
      }
    }

    return true;
  }

  private void getRect() throws VisADException, RemoteException {
    FlatField ff = null;
    if (t != null) {
      int index = getAnimationIndex();
      if (index < 0 || index >= nts) return;
      ff = (FlatField) grids.getSample(index);
    }
    else {
      ff = (FlatField) grids;
    }
    float[][] samples = ff.getFloats(false);
    cut = new float[rangedim][rx * ry];
    for (int ix=ixlow; ix<=ixhi; ix++) {
      for (int iy=iylow; iy<=iyhi; iy++) {
        int i = ix + nx * iy;
        int j = (ix - ixlow) + rx * (iy - iylow);
        for (int k=0; k<rangedim; k++) cut[k][j] = samples[k][i];
      }
    }
  }

  private void replaceRect() throws VisADException, RemoteException {
    int index = 0;
    if (t != null) {
      index = getAnimationIndex();
      if (index < 0 || index >= nts) return;
    }

    if (!getIndices(false)) {
      if (debug) System.out.println("bad box");
      return;
    }

    if (replacedff != null) {
      float[][] samples = replacedff.getFloats(false);
      for (int ix=replacedixlow; ix<=replacedixhi; ix++) {
        for (int iy=replacediylow; iy<=replacediyhi; iy++) {
          int i = ix + nx * iy;
          int j = (ix - replacedixlow) + rx * (iy - replacediylow);
          for (int k=0; k<rangedim; k++) samples[k][i] = replaced[k][j];
        }
      }
      replacedff.setSamples(samples, false);
    }

    FlatField ff = null;
    if (t != null) {
      ff = (FlatField) grids.getSample(index);
    }
    else {
      ff = (FlatField) grids;
    }
    float[][] samples = ff.getFloats(false);
    replaced = new float[rangedim][rx * ry];
    for (int ix=ixlow; ix<=ixhi; ix++) {
      for (int iy=iylow; iy<=iyhi; iy++) {
        int i = ix + nx * iy;
        int j = (ix - ixlow) + rx * (iy - iylow);
        for (int k=0; k<rangedim; k++) replaced[k][j] = samples[k][i];
        if (blend == 0) {
          for (int k=0; k<rangedim; k++) samples[k][i] = cut[k][j];
        }
        else {
          int d = ix - ixlow;
          if ((ixhi - ix) < d) d = ixhi - ix;
          if ((iy - iylow) < d) d = iy - iylow;
          if ((iyhi - iy) < d) d = iyhi - iy;
          if (d > blend) {
            for (int k=0; k<rangedim; k++) samples[k][i] = cut[k][j];
          }
          else {
            float a = ((float) d) / ((float) blend);
            float b = 1.0f - a;
            for (int k=0; k<rangedim; k++) {
              samples[k][i] = b * samples[k][i] + a * cut[k][j];
            }
          }
        }
      }
    }
    ff.setSamples(samples, false);
    replacedff = ff;
    replacedixlow = ixlow;
    replacedixhi = ixhi;
    replacediylow = iylow;
    replacediyhi = iyhi;
  }

  private int getAnimationIndex() throws VisADException {
    int[] indices = {acontrol.getCurrent()};
    Set aset = acontrol.getSet();
    double[][] values = aset.indexToDouble(indices);
    int[] tindices = tset.doubleToIndex(values);
    return tindices[0];
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
  void drag_release() {
    synchronized (lock) {
      try {
        replaceRect();
      }
      catch (VisADException e) {
        if (debug) System.out.println("release fail: " + e.toString());
      }
      catch (RemoteException e) {
        if (debug) System.out.println("release fail: " + e.toString());
      }
    }
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

    final CutAndPasteFields cp = new CutAndPasteFields(field, display1);

    final DataReferenceImpl blend_ref = new DataReferenceImpl("blend_ref");
    VisADSlider blend_slider =
      new VisADSlider("blend", 0, 10, 0, 1.0, blend_ref, RealType.Generic);
    CellImpl blend_cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        int blend = (int) ((Real) blend_ref.getData()).getValue();
        cp.setBlend(blend);
      }
    };
    blend_cell.addReference(blend_ref);

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
    panel2.add(blend_slider);
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

