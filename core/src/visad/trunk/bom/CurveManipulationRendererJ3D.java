//
// CurveManipulationRendererJ3D.java
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

package visad.bom;

import visad.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


/**
   CurveManipulationRendererJ3D is the VisAD class for direct
   manipulation rendering of curves under Java3D, where curves
   are represented by UnionSets of Gridded2DSets with manifold
   dimension = 2
*/
public class CurveManipulationRendererJ3D extends DirectManipulationRendererJ3D {

  /** this DataRenderer supports direct manipulation for
      representations of curves by UnionSets of Gridded2DSets
      with manifold dimension = 2; the Set's domain RealTypes
      must be mapped to two of (XAxis, YAxis, ZAxis) */
  public CurveManipulationRendererJ3D () {
    super();
  }

  public ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbSetTypeJ3D(type, link, parent);
  }

  private float[][] spatialValues = null;

  /** index into spatialValues found by checkClose */
  private int closeIndex = -1;

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient DataReference ref = null;
  private transient MathType type = null;
  private transient ShadowSetType shadow = null;

  /** point on direct manifold line or plane */
  private float point_x, point_y, point_z;
  /** normalized direction of line or perpendicular to plane */
  private float line_x, line_y, line_z;
  /** arrays of length one for inverseScaleValues */
  private float[] f = new float[1];
  private float[] d = new float[1];
  private float[][] value = new float[1][1];

  /** information calculated by checkDirect */
  /** explanation for invalid use of DirectManipulationRenderer */
  private String whyNotDirect = null;
  /** mapping from spatial axes to tuple component */
  private int[] axisToComponent = {-1, -1, -1};
  /** mapping from spatial axes to ScalarMaps */
  private ScalarMap[] directMap = {null, null, null};
  /** dimension of direct manipulation
      (always 2 for CurveManipulationRendererJ3D) */
  private int directManifoldDimension = 2;
  /** spatial DisplayTupleType other than
      DisplaySpatialCartesianTuple */
  DisplayTupleType tuple;

  /** possible values for whyNotDirect */
  private final static String notSetType =
    "not Set";
  private final static String notTwoD =
    "Set must have domain dimension = 2";
  private final static String domainNotSpatial =
    "domain must be mapped to spatial";
  private final static String badCoordSysManifoldDim =
    "directManifoldDimension must be 2 with spatial CoordinateSystem";


  private boolean stop = false;

  public void checkDirect() throws VisADException, RemoteException {
    // realCheckDirect();
    //
    // must customize
    setIsDirectManipulation(false);

    DisplayImpl display = getDisplay();
    link = getLinks()[0];
    ref = link.getDataReference();
    type = link.getType();
    if (!(type instanceof SetType)) {
      whyNotDirect = notSetType;
      return;
    }
    shadow = (ShadowSetType) link.getShadow().getAdaptedShadowType();
    ShadowRealTupleType domain = ((ShadowSetType) shadow).getDomain();

    directMap = new ScalarMap[] {null, null, null};
    ShadowRealType[] components = shadow.getDomain().getRealComponents();
    if (components.length != 2) {
      whyNotDirect = notTwoD;
      return;
    }

    tuple = domain.getDisplaySpatialTuple();
    if(!(Display.DisplaySpatialCartesianTuple.equals(tuple) ||
         (tuple != null &&
          tuple.getCoordinateSystem().getReference().equals(
          Display.DisplaySpatialCartesianTuple)) )) {
      whyNotDirect = domainNotSpatial;
      return;
    }

    for (int i=0; i<components.length; i++) {
      Enumeration maps = components[i].getSelectedMapVector().elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType tuple = dreal.getTuple();
        if (tuple != null &&
            Display.DisplaySpatialCartesianTuple.equals(tuple)) {
          int index = dreal.getTupleIndex();
          axisToComponent[index] = i;
          directMap[index] = map;
        }
      } // end while (maps.hasMoreElements())
    }

    if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
      tuple = null;
    }

    boolean twod = getDisplayRenderer().getMode2D();
    if (tuple != null && !twod) {
      whyNotDirect = badCoordSysManifoldDim;
      return;
    }

    directManifoldDimension = 2;


    // needs more, will find out when we write drag_direct
    setIsDirectManipulation(true);
  }

  private int getDirectManifoldDimension() {
    return directManifoldDimension;
  }

  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  private int getAxisToComponent(int i) {
    return axisToComponent[i];
  }

  private ScalarMap getDirectMap(int i) {
    return directMap[i];
  }

  public void addPoint(float[] x) throws VisADException {
    // may need to do this for performance
  }

// methods customized from DataRenderer:

  /** set spatialValues from ShadowType.doTransform */
  public synchronized void setSpatialValues(float[][] spatial_values) {
    spatialValues = spatial_values;
  }

  /** find minimum distance from ray to spatialValues */
  public synchronized float checkClose(double[] origin, double[] direction) {
    float distance = Float.MAX_VALUE;
    closeIndex = -1;
    if (spatialValues == null) {
      return 0.0f;
    }
    float o_x = (float) origin[0];
    float o_y = (float) origin[1];
    float o_z = (float) origin[2];
    float d_x = (float) direction[0];
    float d_y = (float) direction[1];
    float d_z = (float) direction[2];
/*
System.out.println("origin = " + o_x + " " + o_y + " " + o_z);
System.out.println("direction = " + d_x + " " + d_y + " " + d_z);
*/
    for (int i=0; i<spatialValues[0].length; i++) {
      float x = spatialValues[0][i] - o_x;
      float y = spatialValues[1][i] - o_y;
      float z = spatialValues[2][i] - o_z;
      float dot = x * d_x + y * d_y + z * d_z;
      x = x - dot * d_x;
      y = y - dot * d_y;
      z = z - dot * d_z;
      float d = (float) Math.sqrt(x * x + y * y + z * z);
      if (d < distance) {
        distance = d;
        closeIndex = i;
      }
/*
System.out.println("spatialValues["+i+"] = " + spatialValues[0][i] + " " +
spatialValues[1][i] + " " + spatialValues[2][i] + " d = " + d);
*/
    }
    if (distance > getDisplayRenderer().getPickThreshhold()) {
      return 0.0f;
    }
/*
System.out.println("checkClose: distance = " + distance);
*/
    return distance;
  }

  /** mouse button released, ending direct manipulation */
  public synchronized void release_direct() {
    // may need to do this for performance
  }

  public void stop_direct() {
    stop = true;
  }

  public synchronized void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    // System.out.println("drag_direct " + first + " " + type);
    if (spatialValues == null || ref == null || shadow == null) return;

    if (first) {
      stop = false;
    }
    else {
      if (stop) return;
    }

    float o_x = (float) ray.position[0];
    float o_y = (float) ray.position[1];
    float o_z = (float) ray.position[2];
    float d_x = (float) ray.vector[0];
    float d_y = (float) ray.vector[1];
    float d_z = (float) ray.vector[2];

    // leave logic about getDirectManifoldDimension in, in case
    // CurveManipulationRendererJ3D is ever extended to include
    // ManifoldDimension != 2
    if (first) {
      point_x = spatialValues[0][closeIndex];
      point_y = spatialValues[1][closeIndex];
      point_z = spatialValues[2][closeIndex];
      int lineAxis = -1;
      if (getDirectManifoldDimension() == 3) {
        // coord sys ok
        line_x = d_x;
        line_y = d_y;
        line_z = d_z;
      }
      else {
        if (getDirectManifoldDimension() == 2) {
          if (getDisplayRenderer().getMode2D()) {
            // coord sys ok
            lineAxis = 2;
          }
          else {
            for (int i=0; i<3; i++) {
              if (getAxisToComponent(i) < 0) {
                lineAxis = i;
              }
            }
          }
        }
        else if (getDirectManifoldDimension() == 1) {
          for (int i=0; i<3; i++) {
            if (getAxisToComponent(i) >= 0) {
              lineAxis = i;
            }
          }
        }
        line_x = (lineAxis == 0) ? 1.0f : 0.0f;
        line_y = (lineAxis == 1) ? 1.0f : 0.0f;
        line_z = (lineAxis == 2) ? 1.0f : 0.0f;
      }
    } // end if (first)

    float[] x = new float[3]; // x marks the spot
    if (getDirectManifoldDimension() == 1) {
      // find closest point on line to ray
      // logic from vis5d/cursor.c
      // line o_, d_ to line point_, line_
      float ld = d_x * line_x + d_y * line_y + d_z * line_z;
      float od = o_x * d_x + o_y * d_y + o_z * d_z;
      float pd = point_x * d_x + point_y * d_y + point_z * d_z;
      float ol = o_x * line_x + o_y * line_y + o_z * line_z;
      float pl = point_x * line_x + point_y * line_y + point_z * line_z;
      if (ld * ld == 1.0f) return;
      float t = ((pl - ol) - (ld * (pd - od))) / (ld * ld - 1.0f);
      // x is closest point
      x[0] = point_x + t * line_x;
      x[1] = point_y + t * line_y;
      x[2] = point_z + t * line_z;
    }
    else { // getDirectManifoldDimension() = 2 or 3
      // intersect ray with plane
      float dot = (point_x - o_x) * line_x +
                  (point_y - o_y) * line_y +
                  (point_z - o_z) * line_z;
      float dot2 = d_x * line_x + d_y * line_y + d_z * line_z;
      if (dot2 == 0.0) return;
      dot = dot / dot2;
      // x is intersection
      x[0] = o_x + dot * d_x;
      x[1] = o_y + dot * d_y;
      x[2] = o_z + dot * d_z;
    }

    try {
      float[] xx = {x[0], x[1], x[2]};
      if (tuple != null) {
        float[][] cursor = {{x[0]}, {x[1]}, {x[2]}};
        float[][] new_cursor =
          tuple.getCoordinateSystem().fromReference(cursor);
        x[0] = new_cursor[0][0];
        x[1] = new_cursor[1][0];
        x[2] = new_cursor[2][0];
      }
      Data newData = null;
      Data data = link.getData();


/*
      // RealTupleType case
      addPoint(xx);
      int n = ((RealTuple) data).getDimension();
      Real[] reals = new Real[n];
      Vector vect = new Vector();
      for (int i=0; i<3; i++) {
        int j = getAxisToComponent(i);
        if (j >= 0) {
          f[0] = x[i];
          d = getDirectMap(i).inverseScaleValues(f);
          Real c = (Real) ((RealTuple) data).getComponent(j);
          RealType rtype = (RealType) c.getType();
          reals[j] = new Real(rtype, (double) d[0], rtype.getDefaultUnit(), null);
          // create location string
          String valueString = new Real(rtype, d[0]).toValueString();
          vect.addElement(rtype.getName() + " = " + valueString);
        }
      }
      getDisplayRenderer().setCursorStringVector(vect);
      for (int j=0; j<n; j++) {
        if (reals[j] == null) {
          reals[j] = (Real) ((RealTuple) data).getComponent(j);
        }
      }
      newData = new RealTuple((RealTupleType) type, reals,
                              ((RealTuple) data).getCoordinateSystem());
      ref.setData(newData);
      link.clearData();
*/

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


  static final int N = 5;

  /** test CurveManipulationRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealTupleType earth = new RealTupleType(lat, lon);

    // construct straight south flowing river1
    float[][] points1 = {{3.0f, 2.0f, 1.0f, 0.0f},
                         {0.0f, 0.0f, 0.0f, 0.0f}};
    Gridded2DSet set1 = new Gridded2DSet(earth, points1, 4);
    Gridded2DSet[] sets = {set1};
    UnionSet set = new UnionSet(earth, sets);

    DataReferenceImpl ref = new DataReferenceImpl("set");
    ref.setData(set);

    // construct Java3D display and mappings
    DisplayImpl display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);

    display.addReferences(new CurveManipulationRendererJ3D(), ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test CurveManipulationRendererJ3D");
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

