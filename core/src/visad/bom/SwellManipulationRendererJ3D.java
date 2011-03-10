//
// SwellManipulationRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.*;
import java.rmi.*;


/**
   SwellManipulationRendererJ3D is the VisAD class for direct
   manipulation rendering of swells under Java3D
*/
public class SwellManipulationRendererJ3D extends BarbManipulationRendererJ3D {

  /** this DataRenderer supports direct manipulation for Tuple
      representations of wind barbs; two of the Tuple's Real components
      must be mapped to Flow1X and Flow1Y, or Flow2X and Flow2Y */
  public SwellManipulationRendererJ3D () {
    super();
  }

  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTupleTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbTupleTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbFunctionTypeJ3D(type, link, parent);
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
    "must be RealTypes mapped to flow Azimuth and flow Radial";
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
    if (tuples[0] == null || flowToComponent[1] < 0 || flowToComponent[2] < 0) {
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
        if (Display.DisplayFlow1SphericalTuple.equals(tuple) ||
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
      }
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

      if (first || refirst) {
        // get first Data flow vector
        for (int i=0; i<3; i++) {
          int j = flowToComponent[i];
          data_flow[i] = (j >= 0) ? (float) reals[j].getValue() : 0.0f;
        }

        float[][] ds = {{data_flow[0]}, {data_flow[1]}, {data_flow[2]}};
        ds = coord.toReference(ds);
        data_flow[0] = ds[0][0];
        data_flow[1] = ds[1][0];
        data_flow[2] = ds[2][0];

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

      float[][] xs = {{x[0]}, {x[1]}, {x[2]}};
      xs = coord.fromReference(xs);
      x[0] = xs[0][0];
      x[1] = xs[1][0];
      x[2] = xs[2][0];

      // now replace flow values
      Vector vect = new Vector();
      for (int i=0; i<3; i++) {
        int j = flowToComponent[i];
        if (j >= 0) {
          RealType rtype = (RealType) reals[j].getType();
          reals[j] = new Real(rtype, (double) x[i], rtype.getDefaultUnit(), null);

          // WLH 31 Aug 2000
          Real r = reals[j];
          Unit overrideUnit = null; 
          if (directMap[i] != null) {
            overrideUnit = directMap[i].getOverrideUnit();
          }
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

  /** draw swell, f0 and f1 in meters */
  public float[] makeVector(boolean south, float x, float y, float z,
                          float scale, float pt_size, float f0, float f1,
                          float[] vx, float[] vy, float[] vz, int[] numv,
                          float[] tx, float[] ty, float[] tz, int[] numt) {

    float d, xd, yd;
    float x0, y0, x1, y1, x2, y2, x3, y3, x4, y4;
    float sscale = 0.75f * scale;

    float[] mbarb = new float[4];
    mbarb[0] = x;
    mbarb[1] = y;

    float swell_height = (float) Math.sqrt(f0 * f0 + f1 * f1);

    int lenv = vx.length;
    int nv = numv[0];

    //determine the initial (minimum) length of the flag pole
    if (swell_height >= 0.1f) {
      // normalize direction
      x0 = -f0 / swell_height;
      y0 = -f1 / swell_height;

      float start_arrow = 0.9f * sscale;
      float end_arrow = 1.9f * sscale;
      float arrow_head = 0.3f * sscale;
      x1 = (x + x0 * start_arrow);
      y1 = (y + y0 * start_arrow);
      x2 = (x + x0 * end_arrow);
      y2 = (y + y0 * end_arrow);

      // draw arrow shaft
      vx[nv] = x1;
      vy[nv] = y1;
      vz[nv] = z;
      nv++;
      vx[nv] = x2;
      vy[nv] = y2;
      vz[nv] = z;
      nv++;

      mbarb[2] = x2;
      mbarb[3] = y2;

      xd = x2 - x1;
      yd = y2 - y1;

      x3 = x2 - 0.3f * (xd - yd);
      y3 = y2 - 0.3f * (yd + xd);
      x4 = x2 - 0.3f * (xd + yd);
      y4 = y2 - 0.3f * (yd - xd);

      // draw arrow head
      vx[nv] = x2;
      vy[nv] = y2;
      vz[nv] = z;
      nv++;
      vx[nv] = x3;
      vy[nv] = y3;
      vz[nv] = z;
      nv++;

      vx[nv] = x2;
      vy[nv] = y2;
      vz[nv] = z;
      nv++;
      vx[nv] = x4;
      vy[nv] = y4;
      vz[nv] = z;
      nv++;

      // DRM 2001-07-04
      //int shi = (int) (10.0f * (swell_height + 0.5f));
      int shi = Math.round(10.0f * (swell_height));
      float shf = 0.1f * shi;
      String sh_string = Float.toString(shf);
      int point = sh_string.indexOf('.');
      sh_string = sh_string.substring(0, point + 2);
      // grf 2 Jun 2004 set z value the same as the barb
      double[] start = {x, y - 0.25 * sscale, z};
      double[] base = {0.5 * sscale, 0.0, 0.0};
      double[] up = {0.0, 0.5 * sscale, 0.0};
      VisADLineArray array =
        PlotText.render_label(sh_string, start, base, up, true);
      int nl = array.vertexCount;
      int k = 0;
      for (int i=0; i<nl; i++) {
        vx[nv] = array.coordinates[k++];
        vy[nv] = array.coordinates[k++];
        vz[nv] = array.coordinates[k++];
        nv++;
      }
    }
    else { // if (swell_height < 0.1)

      // wind < 2.5 kts.  Plot a circle
      float rad = (0.7f * pt_size);

      // draw 8 segment circle, center = (x, y), radius = rad
      // 1st segment
      vx[nv] = x - rad;
      vy[nv] = y;
      vz[nv] = z;
      nv++;
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      vz[nv] = z;
      nv++;
      // 2nd segment
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      vz[nv] = z;
      nv++;
      vx[nv] = x;
      vy[nv] = y + rad;
      vz[nv] = z;
      nv++;
      // 3rd segment
      vx[nv] = x;
      vy[nv] = y + rad;
      vz[nv] = z;
      nv++;
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      vz[nv] = z;
      nv++;
      // 4th segment
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      vz[nv] = z;
      nv++;
      vx[nv] = x + rad;
      vy[nv] = y;
      vz[nv] = z;
      nv++;
      // 5th segment
      vx[nv] = x + rad;
      vy[nv] = y;
      vz[nv] = z;
      nv++;
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      vz[nv] = z;
      nv++;
      // 6th segment
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      vz[nv] = z;
      nv++;
      vx[nv] = x;
      vy[nv] = y - rad;
      vz[nv] = z;
      nv++;
      // 7th segment
      vx[nv] = x;
      vy[nv] = y - rad;
      vz[nv] = z;
      nv++;
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      vz[nv] = z;
      nv++;
      // 8th segment
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      vz[nv] = z;
      nv++;
      vx[nv] = x - rad;
      vy[nv] = y;
      vz[nv] = z;
      nv++;
// System.out.println("circle " + x + " " + y + "" + rad);
      mbarb[2] = x;
      mbarb[3] = y;
    }

    numv[0] = nv;
    return mbarb;
  }

  public Object clone() {
    return new SwellManipulationRendererJ3D();
  }

  static final int N = 5;

  /** test SwellManipulationRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct RealTypes for swell record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType red = RealType.getRealType("red");
    RealType green = RealType.getRealType("green");
    RealType swell_degree = RealType.getRealType("swell_degree",
                          CommonUnit.degree);
    RealType swell_height = RealType.getRealType("swell_height",
                          CommonUnit.meter);

    // construct Java3D display and mappings that govern
    // how swell records are displayed
    DisplayImpl display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    ScalarMap swella_map = new ScalarMap(swell_degree, Display.Flow1Azimuth);
    display.addMap(swella_map);
    swella_map.setRange(0.0, 360.0); // do this for swell rendering
    ScalarMap swellh_map = new ScalarMap(swell_height, Display.Flow1Radial);
    display.addMap(swellh_map);
    swellh_map.setRange(0.0, 1.0); // do this for swell rendering
    FlowControl flow_control = (FlowControl) swellh_map.getControl();
    flow_control.setFlowScale(0.15f); // this controls size of barbs
    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    DataReferenceImpl[] refs = new DataReferenceImpl[N * N];
    int k = 0;
    // create an array of N by N swells
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        double u = 2.0 * i / (N - 1.0) - 1.0;
        double v = 2.0 * j / (N - 1.0) - 1.0;

        double fx = 30.0 * u;
        double fy = 30.0 * v;
        double fa = Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy);
        double fh = Math.sqrt(fx * fx + fy * fy);

        // each swell record is a RealTuple (lon, lat,
        //   swell_degree, swell_height, red, green)
        // set colors by swell components, just for grins
        RealTuple tuple = new RealTuple(new Real[]
          {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
           new Real(swell_degree, fa), new Real(swell_height, fh),
           new Real(red, u), new Real(green, v)});

        // construct reference for swell record
        refs[k] = new DataReferenceImpl("ref_" + k);
        refs[k].setData(tuple);

        // link swell record to display via SwellManipulationRendererJ3D
        // so user can change barb by dragging it
        // drag with right mouse button and shift to change direction
        // drag with right mouse button and no shift to change speed
        SwellManipulationRendererJ3D renderer =
          new SwellManipulationRendererJ3D();
        display.addReferences(renderer, refs[k]);

        // link swell record to a CellImpl that will listen for changes
        // and print them
        SwellGetterJ3D cell = new SwellGetterJ3D(flow_control, refs[k]);
        cell.addReference(refs[k]);

        k++;
      }
    }

    // instead of linking the wind record "DataReferenceImpl refs" to
    // the SwellGetterJ3Ds, you can have some user interface event (e.g.,
    // the user clicks on "DONE") trigger code that does a getData() on
    // all the refs and stores the records in a file.

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test SwellManipulationRendererJ3D");
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

class SwellGetterJ3D extends CellImpl {
  DataReferenceImpl ref;

  float scale = 0.15f;
  int count = 20;
  FlowControl flow_control;

  public SwellGetterJ3D(FlowControl f, DataReferenceImpl r) {
    ref = r;
    flow_control = f;
  }

  public void doAction() throws VisADException, RemoteException {
    RealTuple tuple = (RealTuple) ref.getData();
    float lon = (float) ((Real) tuple.getComponent(0)).getValue();
    float lat = (float) ((Real) tuple.getComponent(1)).getValue();
    float dir = (float) ((Real) tuple.getComponent(2)).getValue();
    float height = (float) ((Real) tuple.getComponent(3)).getValue();
    System.out.println("swell = (" + dir + ", " + height + ") at (" +
                       + lat + ", " + lon +")");
  }

}

