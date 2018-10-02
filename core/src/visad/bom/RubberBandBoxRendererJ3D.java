//
// RubberBandBoxRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java3d.*;

import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

import org.jogamp.java3d.*;

/**
   RubberBandBoxRendererJ3D is the VisAD class for direct
   manipulation of rubber band boxes
*/
public class RubberBandBoxRendererJ3D extends DirectManipulationRendererJ3D {

  private RealType x = null;
  private RealType y = null;
  private RealTupleType xy = null;

  private int mouseModifiersMask = 0;
  private int mouseModifiersValue = 0;

  private BranchGroup branch = null;
  private BranchGroup group = null;

  /** this DirectManipulationRenderer is quite different - it does not
      render its data, but only place values into its DataReference
      on right mouse button release;
      it uses xarg and yarg to determine spatial ScalarMaps */
  public RubberBandBoxRendererJ3D (RealType xarg, RealType yarg) {
    this(xarg, yarg, 0, 0);
  }

  /** xarg and yarg determine spatial ScalarMaps;
      mmm and mmv determine whehter SHIFT or CTRL keys are required -
      this is needed since this is a greedy DirectManipulationRenderer
      that will grab any right mouse click (that intersects its 2-D
      sub-manifold) */
  public RubberBandBoxRendererJ3D (RealType xarg, RealType yarg, int mmm, int mmv) {
    super();
    x = xarg;
    y = yarg;
    mouseModifiersMask = mmm;
    mouseModifiersValue = mmv;
  }

  /** don't render - just return BranchGroup for scene graph to
      render rectangle into */
  public synchronized BranchGroup doTransform()
         throws VisADException, RemoteException {
    branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(Group.ALLOW_CHILDREN_READ);
    branch.setCapability(Group.ALLOW_CHILDREN_WRITE);
    branch.setCapability(Group.ALLOW_CHILDREN_EXTEND);

    // check type and maps for valid direct manipulation
    if (!getIsDirectManipulation()) {
      throw new BadDirectManipulationException(getWhyNotDirect() +
        ": DirectManipulationRendererJ3D.doTransform");
    }
    setBranch(branch);
    return branch;
  }

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient DataReference ref = null;

  private transient ScalarMap xmap = null;
  private transient ScalarMap ymap = null;

  float[] default_values;

  /** arrays of length one for inverseScaleValues */
  private float[] f = new float[1];
  private float[] d = new float[1];
  private float[] value = new float[2];

  /** information calculated by checkDirect */
  /** explanation for invalid use of DirectManipulationRenderer */
  private String whyNotDirect = null;
  /** dimension of direct manipulation
      (always 2 for RubberBandBoxRendererJ3D) */
  private int directManifoldDimension = 2;
  /** spatial DisplayTupleType other than
      DisplaySpatialCartesianTuple */
  private DisplayTupleType tuple;
  private CoordinateSystem tuplecs;

  private int xindex = -1;
  private int yindex = -1;
  private int otherindex = -1;
  private float othervalue;

  private byte red, green, blue; // default colors

  private float[][] first_x;
  private float[][] last_x;
  private float[][] clast_x;
  private float cum_lon;

  /** possible values for whyNotDirect */
  private final static String xandyNotMatch =
    "x and y spatial domains don't match";
  private final static String xandyNotSpatial =
    "x and y must be mapped to spatial";


  private boolean stop = false;

  public void checkDirect() throws VisADException, RemoteException {
    setIsDirectManipulation(false);

    DisplayImpl display = getDisplay();

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      link = null;
      return;
    }
    link = Links[0];

    ref = link.getDataReference();
    default_values = link.getDefaultValues();

    xmap = null;
    ymap = null;
    Vector scalar_map_vector = display.getMapVector();
    Enumeration en = scalar_map_vector.elements();
    while (en.hasMoreElements()) {
      ScalarMap map = (ScalarMap) en.nextElement();
      ScalarType real = map.getScalar();
      if (real.equals(x)) {
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType t = dreal.getTuple();
        if (t != null &&
            (t.equals(Display.DisplaySpatialCartesianTuple) ||
             (t.getCoordinateSystem() != null &&
              t.getCoordinateSystem().getReference().equals(
              Display.DisplaySpatialCartesianTuple)))) {
          xmap = map;
          xindex = dreal.getTupleIndex();
          if (tuple == null) {
            tuple = t;
          }
          else if (!t.equals(tuple)) {
            whyNotDirect = xandyNotMatch;
            return;
          }
        }
      }
      if (real.equals(y)) {
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType t = dreal.getTuple();
        if (t != null &&
            (t.equals(Display.DisplaySpatialCartesianTuple) ||
             (t.getCoordinateSystem() != null &&
              t.getCoordinateSystem().getReference().equals(
              Display.DisplaySpatialCartesianTuple)))) {
          ymap = map;
          yindex = dreal.getTupleIndex();
          if (tuple == null) {
            tuple = t;
          }
          else if (!t.equals(tuple)) {
            whyNotDirect = xandyNotMatch;
            return;
          }
        }
      }
    }

    if (xmap == null || ymap == null) {
      whyNotDirect = xandyNotSpatial;
      return;
    }

    xy = new RealTupleType(x, y);

    // get default value for other component of tuple
    otherindex = 3 - (xindex + yindex);
    DisplayRealType dreal = (DisplayRealType) tuple.getComponent(otherindex);
    int index = getDisplay().getDisplayScalarIndex(dreal);
    othervalue = (index > 0) ? default_values[index] :
                               (float) dreal.getDefaultValue();

    // get default colors
    index = getDisplay().getDisplayScalarIndex(Display.Red);
    float v = (index > 0) ? default_values[index] :
                           (float) Display.Red.getDefaultValue();
    red = ShadowType.floatToByte(v);
    index = getDisplay().getDisplayScalarIndex(Display.Green);
    v = (index > 0) ? default_values[index] :
                      (float) Display.Green.getDefaultValue();
    green = ShadowType.floatToByte(v);
    index = getDisplay().getDisplayScalarIndex(Display.Blue);
    v = (index > 0) ? default_values[index] :
                      (float) Display.Blue.getDefaultValue();
    blue = ShadowType.floatToByte(v);

    if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
      tuple = null;
      tuplecs = null;
    }
    else {
      tuplecs = tuple.getCoordinateSystem();
    }

    directManifoldDimension = 2;
    setIsDirectManipulation(true);
  }

  private int getDirectManifoldDimension() {
    return directManifoldDimension;
  }

  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  public void addPoint(float[] x) throws VisADException {
    // may need to do this for performance
  }

// methods customized from DataRenderer:

  public CoordinateSystem getDisplayCoordinateSystem() {
    return tuplecs;
  }

  /** set spatialValues from ShadowType.doTransform */
  public synchronized void setSpatialValues(float[][] spatial_values) {
    // do nothing
  }

  /** check if ray intersects sub-manifold */
  public synchronized float checkClose(double[] origin, double[] direction) {
    int mouseModifiers = getLastMouseModifiers();
    if ((mouseModifiers & mouseModifiersMask) != mouseModifiersValue) {
      return Float.MAX_VALUE;
    }

    try {
      float r = findRayManifoldIntersection(true, origin, direction, tuple,
                                            otherindex, othervalue);
      if (r == r) {
        return 0.0f;
      }
      else {
        return Float.MAX_VALUE;
      }
    }
    catch (VisADException ex) {
      return Float.MAX_VALUE;
    }
  }

  /** mouse button released, ending direct manipulation */
  public synchronized void release_direct() {
    // set data in ref
    if (group != null) group.detach();
    group = null;
    try {
      float[][] samples = new float[2][2];
      f[0] = first_x[xindex][0];
      d = xmap.inverseScaleValues(f);
      samples[0][0] = (float) d[0];
      f[0] = first_x[yindex][0];
      d = ymap.inverseScaleValues(f);
      samples[1][0] = (float) d[0];
      f[0] = last_x[xindex][0];
      d = xmap.inverseScaleValues(f);
      samples[0][1] = (float) d[0];
      f[0] = last_x[yindex][0];
      d = ymap.inverseScaleValues(f);
      samples[1][1] = (float) d[0];
      Gridded2DSet set = new Gridded2DSet(xy, samples, 2);
      ref.setData(set);
      link.clearData();
    } // end try
    catch (VisADException e) {
      // do nothing
      System.out.println("release_direct " + e);
      e.printStackTrace();
    }
    catch (RemoteException e) {
      // do nothing
      System.out.println("release_direct " + e);
      e.printStackTrace();
    }
  }

  public void stop_direct() {
    stop = true;
  }

  private static final int EDGE = 20;

  private static final float EPS = 0.005f;

  public synchronized void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    if (ref == null) return;

    if (first) {
      stop = false;
    }
    else {
      if (stop) return;
    }

    double[] origin = ray.position;
    double[] direction = ray.vector;

    try {
      float r = findRayManifoldIntersection(true, origin, direction, tuple,
                                            otherindex, othervalue);
      if (r != r) {
        if (group != null) group.detach();
        return;
      }
      float[][] xx = {{(float) (origin[0] + r * direction[0])},
                      {(float) (origin[1] + r * direction[1])},
                      {(float) (origin[2] + r * direction[2])}};
      if (tuple != null) xx = tuplecs.fromReference(xx);

      if (first) {
        first_x = xx;
        cum_lon = 0.0f;
      }
      else if (Display.DisplaySpatialSphericalTuple.equals(tuple)) {
        float diff = xx[1][0] - clast_x[1][0];
        if (diff > 180.0f) diff -= 360.0f;
        else if (diff < -180.0f) diff += 360.0f;
        cum_lon += diff;
        if (cum_lon > 360.0f) cum_lon -= 360.0f;
        else if (cum_lon < -360.0f) cum_lon += 360.0f;
      }
      clast_x = xx;

      Vector vect = new Vector();
      f[0] = xx[xindex][0];
      d = xmap.inverseScaleValues(f);

      // WLH 31 Aug 2000
      Real rr = new Real(x, d[0]);
      Unit overrideUnit = xmap.getOverrideUnit();
      Unit rtunit = x.getDefaultUnit();
      // units not part of Time string
      if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
          !RealType.Time.equals(x)) {
        double dval =  overrideUnit.toThis((double) d[0], rtunit);
        rr = new Real(x, dval, overrideUnit);
      }   
      String valueString = rr.toValueString();

      vect.addElement(x.getName() + " = " + valueString);
      f[0] = xx[yindex][0];
      d = ymap.inverseScaleValues(f);

      // WLH 31 Aug 2000
      rr = new Real(y, d[0]);
      overrideUnit = ymap.getOverrideUnit();
      rtunit = y.getDefaultUnit();
      // units not part of Time string
      if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
          !RealType.Time.equals(y)) {
        double dval =  overrideUnit.toThis((double) d[0], rtunit);
        rr = new Real(y, dval, overrideUnit);
      }
      valueString = rr.toValueString();

      valueString = new Real(y, d[0]).toValueString();
      vect.addElement(y.getName() + " = " + valueString);
      getDisplayRenderer().setCursorStringVector(vect);

      float[][] xxp = {{xx[0][0]}, {xx[1][0]}, {xx[2][0]}};
      xxp[otherindex][0] += EPS;
      if (tuplecs != null) xxp = tuplecs.toReference(xxp);
      float[][] xxm = {{xx[0][0]}, {xx[1][0]}, {xx[2][0]}};
      xxm[otherindex][0] -= EPS;
      if (tuplecs != null) xxm = tuplecs.toReference(xxm);
      double dot = (xxp[0][0] - xxm[0][0]) * direction[0] +
                   (xxp[1][0] - xxm[1][0]) * direction[1] +
                   (xxp[2][0] - xxm[2][0]) * direction[2];
      float abs = (float)
        Math.sqrt((xxp[0][0] - xxm[0][0]) * (xxp[0][0] - xxm[0][0]) +
                 (xxp[1][0] - xxm[1][0]) * (xxp[1][0] - xxm[1][0]) +
                 (xxp[2][0] - xxm[2][0]) * (xxp[2][0] - xxm[2][0]));
      float other_offset = EPS * (2.0f * EPS / abs);
      if (dot >= 0.0) other_offset = -other_offset;

      last_x =
        new float[][] {{clast_x[0][0]}, {clast_x[1][0]}, {clast_x[2][0]}};
      if (Display.DisplaySpatialSphericalTuple.equals(tuple) &&
          otherindex != 1) {
        if (last_x[1][0] < first_x[1][0] && cum_lon > 0.0f) {
          last_x[1][0] += 360.0;
        }
        else if (last_x[1][0] > first_x[1][0] && cum_lon < 0.0f) {
          last_x[1][0] -= 360.0;
        }
      }

      int npoints = 4 * EDGE + 1;
      float[][] c = new float[3][npoints];
      for (int i=0; i<EDGE; i++) {
        float a = ((float) i) / EDGE;
        float b = 1.0f - a;
        c[xindex][i] = b * first_x[xindex][0] + a * last_x[xindex][0];
        c[yindex][i] = first_x[yindex][0];
        c[otherindex][i] = first_x[otherindex][0] + other_offset;
        c[xindex][EDGE + i] = last_x[xindex][0];
        c[yindex][EDGE + i] = b * first_x[yindex][0] + a * last_x[yindex][0];
        c[otherindex][EDGE + i] = first_x[otherindex][0] + other_offset;
        c[xindex][2 * EDGE + i] = b * last_x[xindex][0] + a * first_x[xindex][0];
        c[yindex][2 * EDGE + i] = last_x[yindex][0];
        c[otherindex][2 * EDGE + i] = first_x[otherindex][0] + other_offset;
        c[xindex][3 * EDGE + i] = first_x[xindex][0];
        c[yindex][3 * EDGE + i] = b * last_x[yindex][0] + a * first_x[yindex][0];
        c[otherindex][3 * EDGE + i] = first_x[otherindex][0] + other_offset;
      }
      c[0][npoints - 1] = c[0][0];
      c[1][npoints - 1] = c[1][0];
      c[2][npoints - 1] = c[2][0];
      if (tuple != null) c = tuplecs.toReference(c);
      float[] coordinates = new float[3 * npoints];
      for (int i=0; i<npoints; i++) {
        int i3 = 3 * i;
        coordinates[i3] = c[0][i];
        coordinates[i3 + 1] = c[1][i];
        coordinates[i3 + 2] = c[2][i];
      }
      VisADLineStripArray array = new VisADLineStripArray();
      array.vertexCount = npoints;
      array.stripVertexCounts = new int[1];
      array.stripVertexCounts[0] = npoints;
      array.coordinates = coordinates;
      byte[] colors = new byte[3 * npoints];
      for (int i=0; i<npoints; i++) {
        int i3 = 3 * i;
        colors[i3] = red;
        colors[i3 + 1] = green;
        colors[i3 + 2] = blue;
      }
      array.colors = colors;
      array = (VisADLineStripArray) array.adjustSeam(this);

      DisplayImplJ3D display = (DisplayImplJ3D) getDisplay();
      GeometryArray geometry = display.makeGeometry(array);
  
      DataDisplayLink[] Links = getLinks();
      if (Links == null || Links.length == 0) {
        return;
      }
      DataDisplayLink link = Links[0];

      float[] default_values = link.getDefaultValues();
      GraphicsModeControl mode = (GraphicsModeControl)
        display.getGraphicsModeControl().clone();
      float pointSize =
        default_values[display.getDisplayScalarIndex(Display.PointSize)];
      float lineWidth =
        default_values[display.getDisplayScalarIndex(Display.LineWidth)];
      mode.setPointSize(pointSize, true);
      mode.setLineWidth(lineWidth, true);
      Appearance appearance =
        ShadowTypeJ3D.staticMakeAppearance(mode, null, null, geometry, false);

      if (group != null) group.detach();
      group = null;

      Shape3D shape = new Shape3D(geometry, appearance);
      group = new BranchGroup();
      group.setCapability(Group.ALLOW_CHILDREN_READ);
      group.setCapability(BranchGroup.ALLOW_DETACH);
      group.addChild(shape);
      if (branch != null) branch.addChild(group);
    } // end try
    catch (VisADException e) {
      // do nothing
      System.out.println("drag_direct " + e);
      e.printStackTrace();
    }
  }

  public Object clone() {
    return new RubberBandBoxRendererJ3D(x, y, mouseModifiersMask,
                                        mouseModifiersValue);
  }

  private static final int N = 64;

  /** test RubberBandBoxRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealTupleType xy = new RealTupleType(x, y);

    RealType c = RealType.getRealType("c");
    FunctionType ft = new FunctionType(xy, c);

    // construct Java3D display and mappings
    DisplayImpl display = new DisplayImplJ3D("display1");
    if (args.length == 0 || args[0].equals("z")) {
      display.addMap(new ScalarMap(x, Display.XAxis));
      display.addMap(new ScalarMap(y, Display.YAxis));
    }
    else if (args[0].equals("x")) {
      display.addMap(new ScalarMap(x, Display.YAxis));
      display.addMap(new ScalarMap(y, Display.ZAxis));
    }
    else if (args[0].equals("y")) {
      display.addMap(new ScalarMap(x, Display.XAxis));
      display.addMap(new ScalarMap(y, Display.ZAxis));
    }
    else if (args[0].equals("radius")) {
      display.addMap(new ScalarMap(x, Display.Longitude));
      display.addMap(new ScalarMap(y, Display.Latitude));
    }
    else if (args[0].equals("lat")) {
      display.addMap(new ScalarMap(x, Display.Longitude));
      display.addMap(new ScalarMap(y, Display.Radius));
    }
    else if (args[0].equals("lon")) {
      display.addMap(new ScalarMap(x, Display.Latitude));
      display.addMap(new ScalarMap(y, Display.Radius));
    }
    else {
      display.addMap(new ScalarMap(x, Display.Longitude));
      display.addMap(new ScalarMap(y, Display.Latitude));
    }
    display.addMap(new ScalarMap(c, Display.RGB));

    Integer2DSet fset = new Integer2DSet(xy, N, N);
    FlatField field = new FlatField(ft, fset);
    float[][] values = new float[1][N * N];
    int k = 0;
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        values[0][k++] = (i - N / 2) * (j - N / 2);
      }
    }
    field.setSamples(values);
    DataReferenceImpl field_ref = new DataReferenceImpl("field");
    field_ref.setData(field);
    display.addReference(field_ref);

    Gridded2DSet dummy_set = new Gridded2DSet(xy, null, 1);
    final DataReferenceImpl ref = new DataReferenceImpl("set");
    ref.setData(dummy_set);
    int m = (args.length > 1) ? InputEvent.CTRL_MASK : 0;
    display.addReferences(new RubberBandBoxRendererJ3D(x, y, m, m), ref);

    CellImpl cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        Set set = (Set) ref.getData();
        float[][] samples = set.getSamples();
        if (samples != null) {
          System.out.println("box (" + samples[0][0] + ", " + samples[1][0] +
                             ") to (" + samples[0][1] + ", " + samples[1][1] + ")");
        }
      }
    };
    cell.addReference(ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test RubberBandBoxRendererJ3D");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    // add display to JPanel
    panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}

