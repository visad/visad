//
// BarbManipulationRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
   BarbManipulationRendererJ3D is the VisAD class for direct
   manipulation rendering of wind barbs under Java3D
*/
public class BarbManipulationRendererJ3D extends DirectManipulationRendererJ3D {

  /** this DataRenderer supports direct manipulation for Tuple
      representations of wind barbs; two of the Tuple's Real components
      must be mapped to Flow1X and Flow1Y, or Flow2X and Flow2Y */
  public BarbManipulationRendererJ3D () {
    super();
  }
 
  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbFunctionTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTupleTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbSetTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbTupleTypeJ3D(type, link, parent);
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
  private final static String nonCartesian =
    "non-Cartesian spatial mapping";


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
  /** mapping from flow components to Tuple Real components */
  private int[] flowToComponent = {-1, -1, -1};
  /** mapping from flow components to ScalarMaps */
  private ScalarMap[] directMap = {null, null, null};

  /** (barbValues[0], barbValues[1]) = (x, y) barb head location
      (barbValues[2], barbValues[3]) = (x, y) barb tail location */
  private float[] barbValues = null;
  /** which_barb = 0 (Flow1) or 1 (Flow2);
      redundant with tuple */
  private int which_barb = -1;
  /** flow from data when first */
  private float[] data_flow = {0.0f, 0.0f, 0.0f};
  /** data and display magnitudes when first */
  private float data_speed = 0.0f;
  private float display_speed = 0.0f;

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
    if (!(type instanceof TupleType) || !((TupleType) type).getFlat()) {
      whyNotDirect = notFlatTupleType;
      return;
    }
    flowToComponent = new int[] {-1, -1, -1};
    directMap = new ScalarMap[] {null, null, null};
    shadow = (ShadowTupleType) link.getShadow().getAdaptedShadowType();
    DisplayTupleType[] tuples = {null};
    whyNotDirect = findFlow(shadow, display, tuples, flowToComponent);
    if (whyNotDirect != null) return;
    if (tuples[0] == null || flowToComponent[0] < 0 || flowToComponent[1] < 0) {
      whyNotDirect = noFlow;
      return;
    }

    ShadowRealType[] components = shadow.getRealComponents();
    for (int i=0; i<components.length; i++) {
      DisplayTupleType spatial_tuple = components[i].getDisplaySpatialTuple();
      if (spatial_tuple != null &&
          !Display.DisplaySpatialCartesianTuple.equals(spatial_tuple)) {
        whyNotDirect = nonCartesian;
        return;
      }
    }

    tuple = tuples[0];
    // needs more, will find out when we write drag_direct
    setIsDirectManipulation(true);
  }

  /** check for flow mappings;
      does not allow flow mapping through CoordinateSystem */
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
    // may need to do this for performance
  }

  public synchronized void setBarbSpatialValues(float[] mbarb, int which) {
    // (barbValues[0], barbValues[1]) = (x, y) barb head location
    // (barbValues[2], barbValues[3]) = (x, y) barb tail location
    barbValues = mbarb;
    which_barb = which;
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
    // modify speed if mctrl != 0
    // modify speed and direction if neither
    int mshift = mouseModifiers & InputEvent.SHIFT_MASK;
    int mctrl = mouseModifiers & InputEvent.CTRL_MASK;

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
      line_z = 1.0f; // lineAxis == 2 in DataRenderer.drag_direct
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
/*
System.out.println("x = " + x[0] + " " + x[1] + " " + x[2]);
*/
    try {

      Tuple data = (Tuple) link.getData();
      int n = ((TupleType) data.getType()).getNumberOfRealComponents();
      Real[] reals = new Real[n];
 
      int k = 0;
      int m = data.getDimension();
      for (int i=0; i<m; i++) {
        Data component = data.getComponent(i);
        if (component instanceof Real) {
          reals[k++] = (Real) component;
        }
        else if (component instanceof RealTuple) {
          for (int j=0; j<((RealTuple) component).getDimension(); j++) {
            reals[k++] = (Real) ((RealTuple) component).getComponent(j);
          }
        }
      }

      if (first) {
        // get first Data flow vector
        for (int i=0; i<3; i++) {
          int j = flowToComponent[i];
          data_flow[i] = (j >= 0) ? (float) reals[j].getValue() : 0.0f;
        }
        data_speed = (float) Math.sqrt(data_flow[0] * data_flow[0] +
                                       data_flow[1] * data_flow[1] +
                                       data_flow[2] * data_flow[2]);
        float barb0 = barbValues[2] - barbValues[0];
        float barb1 = barbValues[3] - barbValues[1];
/*
System.out.println("data_flow = " + data_flow[0] + " " + data_flow[1] +
                   " " + data_flow[2]);
System.out.println("barbValues = " + barbValues[0] + " " + barbValues[1] +
                   "   " + barbValues[2] + " " + barbValues[3]);
System.out.println("data_speed = " + data_speed);
*/
      }

      // convert x to a flow vector, and from spatial to earth
      if (getRealVectorTypes(which_barb) instanceof EarthVectorType) {
        // don't worry about vector magnitude -
        // data_speed & display_speed take care of that
        float eps = 0.0001f; // estimate derivative with a little vector
        float[][] spatial_locs =
          {{barbValues[0], barbValues[0] + eps * (x[0] - barbValues[0])},
           {barbValues[1], barbValues[1] + eps * (x[1] - barbValues[1])},
           {0.0f, 0.0f}};
/*
System.out.println("spatial_locs = " + spatial_locs[0][0] + " " +
                   spatial_locs[0][1] + " " + spatial_locs[1][0] + " " +
                   spatial_locs[1][1]);
*/
        float[][] earth_locs = spatialToEarth(spatial_locs);
        // WLH - 18 Aug 99
        if (earth_locs == null) return;
/*
System.out.println("earth_locs = " + earth_locs[0][0] + " " +
                   earth_locs[0][1] + " " + earth_locs[1][0] + " " +
                   earth_locs[1][1]);
*/
        x[2] = 0.0f;
        x[0] = (earth_locs[1][1] - earth_locs[1][0]) *
               ((float) Math.cos(Data.DEGREES_TO_RADIANS * earth_locs[0][0]));
        x[1] = earth_locs[0][1] - earth_locs[0][0];
/*
System.out.println("x = " + x[0] + " " + x[1] + " " + x[2]);
*/
      }
      else { // if (!(getRealVectorTypes(which_barb) instanceof EarthVectorType))
        // convert x to vector
        x[0] -= barbValues[0];
        x[1] -= barbValues[1];

        // adjust for spatial map scalings but don't worry about vector
        // magnitude - data_speed & display_speed take care of that
        // also, spatial is Cartesian
        double[] ranges = getRanges();
        for (int i=0; i<3; i++) {
          x[i] /= ranges[i];
        }
/*
System.out.println("ranges = " + ranges[0] + " " + ranges[1] +
                   " " + ranges[2]);
System.out.println("x = " + x[0] + " " + x[1] + " " + x[2]);
*/
      }

      // WLH 6 August 99
      x[0] = -x[0];
      x[1] = -x[1];
      x[2] = -x[2];

/* may need to do this for performance
      float[] xx = {x[0], x[1], x[2]};
      addPoint(xx);
*/

      float x_speed =
        (float) Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
      if (first) {
        display_speed = x_speed;
      }

      if (mshift != 0) {
        // only modify data_flow direction
        float ratio = data_speed / x_speed;
        x[0] *= ratio;
        x[1] *= ratio;
        x[2] *= ratio;
/*
System.out.println("direction, ratio = " + ratio + " " +
                   data_speed + " " + x_speed);
System.out.println("x = " + x[0] + " " + x[1] + " " + x[2]);
*/
      }
      else if (mctrl != 0) {
        // only modify data_flow speed
        float ratio = x_speed / display_speed;
        x[0] = ratio * data_flow[0];
        x[1] = ratio * data_flow[1];
        x[2] = ratio * data_flow[2];
/*
System.out.println("speed, ratio = " + ratio + " " +
                   x_speed + " " + display_speed);
System.out.println("x = " + x[0] + " " + x[1] + " " + x[2]);
*/
      }
      else {
        // modify data_flow speed and direction
        float ratio = data_speed / display_speed;
        x[0] *= ratio;
        x[1] *= ratio;
        x[2] *= ratio;
      }

      // now replace flow values
      Vector vect = new Vector();
      for (int i=0; i<3; i++) {
        int j = flowToComponent[i];
        if (j >= 0) {
          RealType rtype = (RealType) reals[j].getType();
          reals[j] = new Real(rtype, (double) x[i], rtype.getDefaultUnit(), null);
          // create location string
          vect.addElement(rtype.getName() + " = " + x[i]);
        }
      }
      getDisplayRenderer().setCursorStringVector(vect);

      Data newData = null;
      // now build new RealTuple or Flat Tuple
      if (data instanceof RealTuple) {
        newData = new RealTuple(((RealTupleType) data.getType()), reals,
                                ((RealTuple) data).getCoordinateSystem());
      }
      else {
        Data[] new_components = new Data[m];
        k = 0;
        for (int i=0; i<m; i++) {
          Data component = data.getComponent(i);
          if (component instanceof Real) {
            new_components[i] = reals[k++];
          }
          else if (component instanceof RealTuple) {
            Real[] sub_reals = new Real[((RealTuple) component).getDimension()];
            for (int j=0; j<((RealTuple) component).getDimension(); j++) {
              sub_reals[j] = reals[k++];
            }
            new_components[i] =
              new RealTuple(((RealTupleType) component.getType()), sub_reals,
                            ((RealTuple) component).getCoordinateSystem());
          }
        }
        newData = new Tuple(new_components, false);
      }
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

  /** test BarbManipulationRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType windx = new RealType("windx");
    RealType windy = new RealType("windy");
    RealType red = new RealType("red");
    RealType green = new RealType("green");

    // EarthVectorType extends RealTupleType and says that its
    // components are vectors in m/s with components parallel
    // to Longitude (positive east) and Latitude (positive north)
    EarthVectorType windxy = new EarthVectorType(windx, windy);

    // construct Java3D display and mappings that govern
    // how wind records are displayed
    DisplayImpl display = new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    ScalarMap windx_map = new ScalarMap(windx, Display.Flow1X);
    display.addMap(windx_map);
    windx_map.setRange(-1.0, 1.0); // do this for barb rendering
    ScalarMap windy_map = new ScalarMap(windy, Display.Flow1Y);
    display.addMap(windy_map);
    windy_map.setRange(-1.0, 1.0); // do this for barb rendering
    FlowControl flow_control = (FlowControl) windy_map.getControl();
    flow_control.setFlowScale(0.15f); // this controls size of barbs
    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    DataReferenceImpl[] refs = new DataReferenceImpl[N * N];
    int k = 0;
    // create an array of N by N winds
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        double u = 2.0 * i / (N - 1.0) - 1.0;
        double v = 2.0 * j / (N - 1.0) - 1.0;

        // each wind record is a Tuple (lon, lat, (windx, windy), red, green)
        // set colors by wind components, just for grins
        Tuple tuple = new Tuple(new Data[]
          {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
           new RealTuple(windxy, new double[] {30.0 * u, 30.0 * v}),
           new Real(red, u), new Real(green, v)});

        // construct reference for wind record
        refs[k] = new DataReferenceImpl("ref_" + k);
        refs[k].setData(tuple);

        // link wind record to display via BarbManipulationRendererJ3D
        // so user can change barb by dragging it
        // drag with right mouse button and shift to change direction
        // drag with right mouse button and no shift to change speed
        display.addReferences(new BarbManipulationRendererJ3D(), refs[k]);

        // link wind record to a CellImpl that will listen for changes
        // and print them
        WindGetterJ3D cell = new WindGetterJ3D(flow_control, refs[k]);
        cell.addReference(refs[k]);

        k++;
      }
    }

    // instead of linking the wind record "DataReferenceImpl refs" to
    // the WindGetterJ3Ds, you can have some user interface event (e.g.,
    // the user clicks on "DONE") trigger code that does a getData() on
    // all the refs and stores the records in a file.

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test BarbManipulationRendererJ3D");
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

class WindGetterJ3D extends CellImpl {
  DataReferenceImpl ref;

  float scale = 0.15f;
  int count = 20;
  FlowControl flow_control;

  public WindGetterJ3D(FlowControl f, DataReferenceImpl r) {
    ref = r;
    flow_control = f;
  }

  public void doAction() throws VisADException, RemoteException {
    Tuple tuple = (Tuple) ref.getData();
    float lon = (float) ((Real) tuple.getComponent(0)).getValue();
    float lat = (float) ((Real) tuple.getComponent(1)).getValue();
    RealTuple wind = (RealTuple) tuple.getComponent(2);
    float windx = (float) ((Real) wind.getComponent(0)).getValue();
    float windy = (float) ((Real) wind.getComponent(1)).getValue();
    System.out.println("wind = (" + windx + ", " + windy + ") at (" +
                       + lat + ", " + lon +")");
/* a testing hack
    count--;
    if (count < 0) {
      count = 20;
      scale = 0.15f * 0.3f / scale;
      flow_control.setFlowScale(scale);
    }
*/
  }

}

