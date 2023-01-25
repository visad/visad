//
// BarbManipulationRendererJ2D.java
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

package visad.bom;

import visad.*;
import visad.java2d.*;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


/**
   BarbManipulationRendererJ2D is the VisAD class for direct
   manipulation rendering of wind barbs under Java2D
*/
public class BarbManipulationRendererJ2D extends DirectManipulationRendererJ2D
       implements BarbRenderer {

  /** this DataRenderer supports direct manipulation for Tuple
      representations of wind barbs; two of the Tuple's Real components
      must be mapped to Flow1X and Flow1Y, or Flow2X and Flow2Y */
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

  /** dummy for BarbRenderer */
  public float[] makeVector(boolean south, float x, float y, float z,
                     float scale, float pt_size, float f0, float f1,
                     float[] vx, float[] vy, float[] vz, int[] numv,
                     float[] tx, float[] ty, float[] tz, int[] numt) {
    return null;
  }

  private boolean knotsConvert = true;

  public void setKnotsConvert(boolean enable) {
    knotsConvert = enable;
  }

  public boolean getKnotsConvert() {
    return knotsConvert;
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

  private CoordinateSystem coord = null;

  /** point on direct manifold line or plane */
  private float point_x, point_y, point_z;
  /** normalized direction of line or perpendicular to plane */
  private float line_x, line_y, line_z;
  /** arrays of length one for inverseScaleValues */
  private float[] f = new float[1];
  private float[] d = new float[1];

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

  /** if user adjusts speed, make sure start speed is greater than EPS */
  private static final float EPS = 0.2f;

  private boolean refirst = false;

  private boolean stop = false;

  /** pick error offset, communicated from checkClose() to drag_direct() */
  private float offsetx = 0.0f, offsety = 0.0f, offsetz = 0.0f;
  /** count down to decay offset to 0.0 */
  private int offset_count = 0;
  /** initial offset_count */
  private static final int OFFSET_COUNT_INIT = 30;

  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  public void checkDirect() throws VisADException, RemoteException {
    // realCheckDirect();
    //
    // must customize
    setIsDirectManipulation(false);

    DisplayImpl display = getDisplay();
    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      link = null;
      return;
    }
    link = Links[0];

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
    if (coord == null) {
      if (tuples[0] == null ||
          flowToComponent[0] < 0 || flowToComponent[1] < 0) {
        whyNotDirect = noFlow;
        return;
      }
    }
    else {
      if (tuples[0] == null ||
          flowToComponent[1] < 0 || flowToComponent[2] < 0) {
        whyNotDirect = noFlow;
        return;
      }
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
        else if (Display.DisplayFlow1SphericalTuple.equals(tuple) ||
                 Display.DisplayFlow2SphericalTuple.equals(tuple)) {
          if (tuples[0] != null) {
            if (!tuples[0].equals(tuple)) {
              return multipleFlowTuples;
            }
          }
          else {
            tuples[0] = tuple;
            coord = tuple.getCoordinateSystem();
          }
          num_flow_per_real++;
          if (num_flow_per_real > 1) {
            return multipleFlowMapping;
          }
          int index = dreal.getTupleIndex();
          flowToComponent[index] = i;
          directMap[index] = map;
        }
      } // while (maps.hasMoreElements())
    }
    return null;
  }

  public void addPoint(float[] x) throws VisADException {
    // may need to do this for performance
  }

  public synchronized void setVectorSpatialValues(float[] mbarb, int which) {
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

    offsetx = x;
    offsety = y;
    offsetz = z;

    return (float) Math.sqrt(x * x + y * y + z * z); // distance
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
    if (barbValues == null || ref == null || shadow == null) return;

    if (first) {
      stop = false;
    }
    else {
      if (stop) return;
    }

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

    if (pickCrawlToCursor) {
      if (first) {
        offset_count = OFFSET_COUNT_INIT;
      }
      else {
        if (offset_count > 0) offset_count--;
      }
      if (offset_count > 0) {
        float mult = ((float) offset_count) / ((float) OFFSET_COUNT_INIT);
        o_x += mult * offsetx;
        o_y += mult * offsety;
        o_z += mult * offsetz;
      }
    }

    if (first || refirst) {
      point_x = barbValues[2];
      point_y = barbValues[3];
      point_z = 0.0f;
      line_x = 0.0f;
      line_y = 0.0f;
      line_z = 1.0f; // lineAxis == 2 in DataRenderer.drag_direct
    } // end if (first || refirst)

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

      Tuple data;
      try {
        data = (Tuple) link.getData();
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          return;
        }
        throw re;
      }

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

      if (first || refirst) {
        // get first Data flow vector
        for (int i=0; i<3; i++) {
          int j = flowToComponent[i];
          data_flow[i] = (j >= 0) ? (float) reals[j].getValue() : 0.0f;
        }

        if (coord != null) {
          float[][] ds = {{data_flow[0]}, {data_flow[1]}, {data_flow[2]}};
          ds = coord.toReference(ds);
          data_flow[0] = ds[0][0];
          data_flow[1] = ds[1][0];
          data_flow[2] = ds[2][0];
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
      } // end if (first || refirst)

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
      if (x_speed < 0.000001f) x_speed = 0.000001f;
      if (first || refirst) {
        display_speed = x_speed;
      }
      refirst = false;

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
        if (data_speed < EPS) {
          data_flow[0] = 2.0f * EPS;
          refirst = true;
        }
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
        if (data_speed < EPS) {
          data_flow[0] = 2.0f * EPS;
          x[0] = data_flow[0];
          x[1] = data_flow[1];
          x[2] = data_flow[2];
          refirst = true;
        }
        else {
          x[0] *= ratio;
          x[1] *= ratio;
          x[2] *= ratio;
        }
      }

      if (coord != null) {
        float[][] xs = {{x[0]}, {x[1]}, {x[2]}};
        xs = coord.fromReference(xs);
        x[0] = xs[0][0];
        x[1] = xs[1][0];
        x[2] = xs[2][0];
      }

      // now replace flow values
      Vector vect = new Vector();
      for (int i=0; i<3; i++) {
        int j = flowToComponent[i];
        if (j >= 0) {
          RealType rtype = (RealType) reals[j].getType();
          reals[j] = new Real(rtype, (double) x[i], rtype.getDefaultUnit(), null);

          // WLH 31 Aug 2000
          Real r = reals[j];
          Unit overrideUnit = directMap[i].getOverrideUnit();
          Unit rtunit = rtype.getDefaultUnit();
          // units not part of Time string
          if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
              !RealType.Time.equals(rtype)) {
            double d = (float) overrideUnit.toThis((double) x[0], rtunit);
            r = new Real(rtype, d, overrideUnit);
            String valueString = r.toValueString();
            vect.addElement(rtype.getName() + " = " + valueString);
          }
          else {
            // create location string
            vect.addElement(rtype.getName() + " = " + x[i]);
          }

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

  public Object clone() {
    return new BarbManipulationRendererJ2D();
  }

  static final int N = 5;

  /** test BarbManipulationRendererJ2D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
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

    RealType wind_dir = RealType.getRealType("wind_dir",
                          CommonUnit.degree);
    RealType wind_speed = RealType.getRealType("wind_speed",
                          CommonUnit.meterPerSecond);
    RealTupleType windds = null;
    if (args.length > 0) {
      System.out.println("polar winds");
      windds =
        new RealTupleType(new RealType[] {wind_dir, wind_speed},
        new WindPolarCoordinateSystem(windxy), null);
    }

    // construct Java2D display and mappings that govern
    // how wind records are displayed
    DisplayImpl display = new DisplayImplJ2D("display1");
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);

    FlowControl flow_control;
    if (args.length > 0) {
      ScalarMap winds_map = new ScalarMap(wind_speed, Display.Flow1Radial);
      display.addMap(winds_map);
      winds_map.setRange(0.0, 1.0); // do this for barb rendering
      ScalarMap windd_map = new ScalarMap(wind_dir, Display.Flow1Azimuth);
      display.addMap(windd_map);
      windd_map.setRange(0.0, 360.0); // do this for barb rendering
      flow_control = (FlowControl) windd_map.getControl();
      flow_control.setFlowScale(0.15f); // this controls size of barbs
    }
    else {
      ScalarMap windx_map = new ScalarMap(windx, Display.Flow1X);
      display.addMap(windx_map);
      windx_map.setRange(-1.0, 1.0); // do this for barb rendering
      ScalarMap windy_map = new ScalarMap(windy, Display.Flow1Y);
      display.addMap(windy_map);
      windy_map.setRange(-1.0, 1.0); // do this for barb rendering
      flow_control = (FlowControl) windy_map.getControl();
      flow_control.setFlowScale(0.15f); // this controls size of barbs
    }

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
        Tuple tuple;
        double fx = 30.0 * u;
        double fy = 30.0 * v;
        if (args.length > 0) {
          double fd = Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy);
          double fs = Math.sqrt(fx * fx + fy * fy);
          tuple = new Tuple(new Data[]
            {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
             new RealTuple(windds, new double[] {fd, fs}),
             new Real(red, u), new Real(green, v)});
        }
        else {
          tuple = new Tuple(new Data[]
            {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
             new RealTuple(windxy, new double[] {fx, fy}),
             new Real(red, u), new Real(green, v)});
        }

        // construct reference for wind record
        refs[k] = new DataReferenceImpl("ref_" + k);
        refs[k].setData(tuple);

        // link wind record to display via BarbManipulationRendererJ2D
        // so user can change barb by dragging it
        // drag with right mouse button and shift to change direction
        // drag with right mouse button and no shift to change speed
        BarbManipulationRendererJ2D renderer = new BarbManipulationRendererJ2D();
        renderer.setKnotsConvert(true);
        display.addReferences(renderer, refs[k]);

        // link wind record to a CellImpl that will listen for changes
        // and print them
        WindGetterJ2D cell = new WindGetterJ2D(refs[k]);
        cell.addReference(refs[k]);

        k++;
      }
    }

    // instead of linking the wind record "DataReferenceImpl refs" to
    // the WindGetterJ2Ds, you can have some user interface event (e.g.,
    // the user clicks on "DONE") trigger code that does a getData() on
    // all the refs and stores the records in a file.

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

class WindGetterJ2D extends CellImpl {
  DataReferenceImpl ref;

  public WindGetterJ2D(DataReferenceImpl r) {
    ref = r;
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
  }

}

