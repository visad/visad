
//
// BarbManipulationRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.bom;
 
import visad.*;
import visad.java2d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


/**
   BarbManipulationRendererJ2D is the VisAD class for direct
   manipulation rendering of wind barbs under Java2D
*/
public class BarbManipulationRendererJ2D extends DirectManipulationRendererJ2D {


/*

 **** must invert CoordinateSystem adjustment to flow, from assembleSpatial ****

*/


  /** this DataRenderer supports direct manipulation for RealTuple
      representations of wind barbs; four of the RealTuple's Real
      components must be mapped to XAxis, YAxis, Flow1X and Flow1Y */
  public BarbManipulationRendererJ2D () {
    super();
  }
 
  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbFunctionTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTupleTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbSetTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbTupleTypeJ2D(type, link, parent);
  }

  /** information calculated by checkDirect */
  /** explanation for invalid use of DirectManipulationRenderer */
  private String whyNotDirect = null;
  private final static String notFlatTupleType =
    "not Flat Tuple";
  private final static String multipleFlowTuples =
    "mappings to both Flow1 and Flow2";
  private final static String multipleFlowMapping =
    "RealType with multiple flow mappings";
  private final static String noFlow =
    "must be RealTypes mapped to flow X and flow Y";


  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient DataReference ref = null;
  private transient MathType type = null;
  private transient ShadowTupleType shadow = null;

  /** point on direct manifold line or plane */
  private float point_x, point_y, point_z;
  /** normalized direction of line or perpendicular to plane */
  private float line_x, line_y, line_z;
  /** arrays of length one for inverseScaleValues */
  private float[] f = new float[1];
  private float[] d = new float[1];

  /** DisplayFlow1Tuple or DisplayFlow2Tuple */
  private DisplayTupleType tuple;
  /** mapping from flow components to RealTuple components */
  private int[] flowToComponent = {-1, -1, -1};
  /** mapping from flow components to ScalarMaps */
  private ScalarMap[] directMap = {null, null, null};

  /** (barbValues[0], barbValues[1]) = (x, y) barb head location
      (barbValues[2], barbValues[3]) = (x, y) barb tail location */
  private float[] barbValues = null;

  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  public void checkDirect() throws VisADException, RemoteException {
    // realCheckDirect();
    //
    // must customize
    setIsDirectManipulation(false);

    DisplayImpl display = getDisplay();
    link = getLinks()[0];
    ref = link.getDataReference();
    type = link.getType();
    tuple = null;
    if (!(type instanceof TupleType) || !((TupleType) type).getFlat()) {
      whyNotDirect = notFlatTupleType;
      return;
    }
    flowToComponent = new int[] {-1, -1, -1};
    directMap = new ScalarMap[] {null, null, null};
    shadow = (ShadowTupleType) link.getShadow().getAdaptedShadowType();
    DisplayTupleType[] tuples = {tuple};
    whyNotDirect = findFlow(shadow, display, tuples, flowToComponent);
    if (whyNotDirect != null) return;
    if (tuples == null || flowToComponent[0] < 0 || flowToComponent[1] < 0) {
      whyNotDirect = noFlow;
      return;
    }
    // needs more, will find out when we write drag_direct
    setIsDirectManipulation(true);
  }

  private String findFlow(ShadowTupleType shadow,
                          DisplayImpl display, DisplayTupleType[] tuples,
                          int[] flowToComponent) {
    ShadowRealType[] components = shadow.getRealComponents();
    for (int i=0; i<components.length; i++) {
      int num_flow_per_real = 0;
      Enumeration maps = components[i].getSelectedMapVector().elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType tuple = dreal.getTuple();
        if (Display.DisplayFlow1Tuple.equals(tuple) ||
            Display.DisplayFlow2Tuple.equals(tuple)) {
          if (tuples[0] != null) {
            if (!tuples[0].equals(tuple)) {
              return multipleFlowTuples;
            }
          }
          else {
            tuples[0] = tuple;
          }
          num_flow_per_real++;
          if (num_flow_per_real > 1) {
            return multipleFlowMapping;
          }
          int index = dreal.getTupleIndex();
          flowToComponent[index] = i;
          directMap[index] = map;
        }
      }
    }
    return null;
  }

  public void addPoint(float[] x) throws VisADException {
/* may need to do this for performance
    int count = x.length / 3;
    VisADGeometryArray array = null;
    if (count == 1) {
      array = new VisADPointArray();
    }
    else if (count == 2) {
      array = new VisADLineArray();
    }
    else {
      return;
    }
    array.coordinates = x;
    array.vertexCount = count;
    VisADAppearance appearance = new VisADAppearance();
    DataDisplayLink link = getLinks()[0];
    float[] default_values = link.getDefaultValues();
    DisplayImpl display = getDisplay();
    appearance.pointSize =
      default_values[display.getDisplayScalarIndex(Display.PointSize)];
    appearance.lineWidth = 
      default_values[display.getDisplayScalarIndex(Display.LineWidth)];
    appearance.red = 1.0f;
    appearance.green = 1.0f;
    appearance.blue = 1.0f;
    appearance.array = array;
    //
    VisADGroup extra_branch = getExtraBranch();
    //
    // want replace rather than add
    extra_branch.addChild(appearance);
*/
  }

  public synchronized void setBarbSpatialValues(float[] mbarb) {
    // (barbValues[0], barbValues[1]) = (x, y) barb head location
    // (barbValues[2], barbValues[3]) = (x, y) barb tail location
    barbValues = mbarb;
  }

// methods customized from DataRenderer:

  /** set spatialValues from ShadowType.doTransform */
  public synchronized void setSpatialValues(float[][] spatial_values) {
    // do nothing - manipulate barb values rather than spatial values
    // spatialValues = spatial_values;
  }

  /** find minimum distance from ray to barb tail */
  public synchronized float checkClose(double[] origin, double[] direction) {
    if (barbValues == null) return Float.MAX_VALUE;
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
    float x = barbValues[2] - o_x;
    float y = barbValues[3] - o_y;
    float z = 0.0f - o_z;
    float dot = x * d_x + y * d_y + z * d_z;
    x = x - dot * d_x;
    y = y - dot * d_y;
    z = z - dot * d_z;
    return (float) Math.sqrt(x * x + y * y + z * z); // distance
  }

  /** mouse button released, ending direct manipulation */
  public synchronized void release_direct() {
    // may need to do this for performance
  }

  public synchronized void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    // System.out.println("drag_direct " + first + " " + type);
    if (barbValues == null || ref == null || shadow == null) return;
    // modify direction if mshift != 0
    int mshift = mouseModifiers & InputEvent.SHIFT_MASK;

    float o_x = (float) ray.position[0];
    float o_y = (float) ray.position[1];
    float o_z = (float) ray.position[2];
    float d_x = (float) ray.vector[0];
    float d_y = (float) ray.vector[1];
    float d_z = (float) ray.vector[2];

    if (first) {
      point_x = barbValues[2];
      point_y = barbValues[3];
      point_z = 0.0f;
      line_x = 0.0f;
      line_y = 0.0f;
      line_z = 1.0f;
    } // end if (first)

    float[] x = new float[3]; // x marks the spot
    // DirectManifoldDimension = 2
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

    try {
      Data newData = null;
      Data data = link.getData();
      // type is a RealTupleType
/* may need to do this for performance
      float[] xx = {x[0], x[1], x[2]};
      addPoint(xx);
*/
      int n = ((TupleType) data.getType()).getNumberOfRealComponents();
      Real[] reals = new Real[n];
      Vector vect = new Vector();
      for (int i=0; i<3; i++) {
        int j = flowToComponent[i];
        if (j >= 0) {
          f[0] = x[i];
          d = directMap[i].inverseScaleValues(f);
          Real c = (Real) ((RealTuple) data).getComponent(j);
          RealType rtype = (RealType) c.getType();
          reals[j] = new Real(rtype, (double) d[0], rtype.getDefaultUnit(), null);
          // create location string
          float g = d[0];
          vect.addElement(rtype.getName() + " = " + g);
        }
      }
      getDisplayRenderer().setCursorStringVector(vect);
      for (int j=0; j<n; j++) {
        if (reals[j] == null) {
          reals[j] = (Real) ((RealTuple) data).getComponent(j);
        }
      }
      newData = new RealTuple(reals);
      ref.setData(newData);
    }
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

  /** test BarbManipulationRendererJ2D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType flowx = new RealType("flowx");
    RealType flowy = new RealType("flowy");
    RealType red = new RealType("red");
    RealType green = new RealType("green");

    EarthVectorType flowxy = new EarthVectorType(flowx, flowy);



    DisplayImpl display = new DisplayImplJ2D("display1");
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    ScalarMap flowx_map = new ScalarMap(flowx, Display.Flow1X);
    display.addMap(flowx_map);
    flowx_map.setRange(-1.0, 1.0);
    ScalarMap flowy_map = new ScalarMap(flowy, Display.Flow1Y);
    display.addMap(flowy_map);
    flowy_map.setRange(-1.0, 1.0);
    FlowControl flow_control = (FlowControl) flowy_map.getControl();
    flow_control.setFlowScale(0.15f);
    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        double u = 2.0 * i / (N - 1.0) - 1.0;
        double v = 2.0 * j / (N - 1.0) - 1.0;
        Tuple tuple = new Tuple(new Data[]
          {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
           new RealTuple(new EarthVectorType(flowx, flowy),
                         new double[] {30.0 * u, 30.0 * v}),
           new Real(red, u), new Real(green, v)});
/*
        RealTuple tuple = new RealTuple(new Real[]
          {new Real(x, u), new Real(y, v),
           new Real(flowx, 30.0 * u), new Real(flowy, 30.0 * v),
           new Real(red, 1.0), new Real(green, 1.0), new Real(blue, 0.0)});
*/
        DataReferenceImpl ref = new DataReferenceImpl("ref");
        ref.setData(tuple);
        display.addReferences(new BarbManipulationRendererJ2D(), ref);
      }
    }

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test BarbManipulationRendererJ2D");
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

