//
// PointManipulationRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.BadDirectManipulationException;
import visad.CellImpl;
import visad.CoordinateSystem;
import visad.DataDisplayLink;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.DisplayRealType;
import visad.DisplayTupleType;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer2DSet;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.ScalarType;
import visad.Unit;
import visad.VisADException;
import visad.VisADRay;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;

/**
   PointManipulationRendererJ3D is the VisAD class for direct
   manipulation of single points
*/
public class PointManipulationRendererJ3D extends DirectManipulationRendererJ3D {

  private RealType x = null;
  private RealType y = null;
  private RealTupleType xy = null;

  private int mouseModifiersMask = 0;
  private int mouseModifiersValue = 0;

  private BranchGroup branch = null;
  private BranchGroup group = null;

  /** this DirectManipulationRenderer is quite different - it does not
      render its data, but only place values into its DataReference
      on right mouse button press;
      it uses xarg and yarg to determine spatial ScalarMaps */
  public PointManipulationRendererJ3D (RealType xarg, RealType yarg) {
    // Don't match any modifier combinations (mmm = 0)
    // Don't test for any - ie: accept (and steal) all combinations (mmv = 0)
    this(xarg, yarg, 0, 0);
  }

	/**
	 * xarg and yarg determine spatial ScalarMaps; mmm and mmv determine whether
	 * SHIFT or CTRL keys are required - this is needed since this is a greedy
	 * DirectManipulationRenderer that will grab any right mouse click (that
	 * intersects its 2-D sub-manifold)
	 * 
	 * @param mmm
	 *            - "Mouse Modifier Mask", matches the modifiers we want plus
	 *            all that we don't want
	 * @param mmv
	 *            - "Mouse Modifier Value", equals the subset of mask that we
	 *            want to match
	 */
  
  public PointManipulationRendererJ3D (RealType xarg, RealType yarg, int mmm, int mmv) {
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

  /** information calculated by checkDirect */
  /** explanation for invalid use of DirectManipulationRenderer */
  private String whyNotDirect = null;
  /** dimension of direct manipulation
      (always 2 for PointManipulationRendererJ3D) */
  private int directManifoldDimension = 2;
  /** spatial DisplayTupleType other than
      DisplaySpatialCartesianTuple */
  private DisplayTupleType tuple;
  private CoordinateSystem tuplecs;

  private int xindex = -1;
  private int yindex = -1;
  private int otherindex = -1;
  private float othervalue;
  private float[][] first_x;

  /** possible values for whyNotDirect */
  private final static String xandyNotMatch =
    "x and y spatial domains don't match";
  private final static String xandyNotSpatial =
    "x and y must be mapped to spatial";

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

  /** check if ray intersects sub-manifold
      @return float, 0 - hit, Float.MAX_VALUE - no hit
  */
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

  public void stop_direct() {
  }

  public synchronized void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    if (ref == null) return;

    if (!first) return;

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

      first_x = xx;

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
      // getDisplayRenderer().setCursorStringVector(vect);


      double[] dd = new double[2];
      f[0] = first_x[xindex][0];
      d = xmap.inverseScaleValues(f);
      dd[0] = d[0];
      f[0] = first_x[yindex][0];
      d = ymap.inverseScaleValues(f);
      dd[1] = d[0];

      RealTuple rt = new RealTuple(xy, dd);
      ref.setData(rt);

    } // end try
    catch (VisADException e) {
      // do nothing
      System.out.println("drag_direct " + e);
      e.printStackTrace();
    }
    catch (RemoteException e) {
      // do nothing
      System.out.println("drag_direct " + e);
      e.printStackTrace();
    }
  }

  public Object clone() {
    return new PointManipulationRendererJ3D(x, y, mouseModifiersMask,
                                            mouseModifiersValue);
  }

  private static final int N = 64;

  /** test PointManipulationRendererJ3D */
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

    RealTuple dummy_rt = new RealTuple(xy, new double[] {Double.NaN, Double.NaN});
    final DataReferenceImpl ref = new DataReferenceImpl("rt");
    ref.setData(dummy_rt);
    int m = (args.length > 1) ? InputEvent.CTRL_MASK : 0;
    display.addReferences(new PointManipulationRendererJ3D(x, y, m, m), ref);

    CellImpl cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple rt = (RealTuple) ref.getData();
        double dx = ((Real) rt.getComponent(0)).getValue();
        double dy = ((Real) rt.getComponent(1)).getValue();
        if (dx == dx && dy == dy) {
          System.out.println("point (" + dx + ", " + dy + ")");
        }
      }
    };
    cell.addReference(ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test PointManipulationRendererJ3D");
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

