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
    return new ShadowCurveSetTypeJ3D(type, link, parent);
  }

  private float[][] spatialValues = null;

  /** index into spatialValues found by checkClose */
  private int closeIndex = -1;

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient DataReference ref = null;
  private transient MathType type = null;
  private transient ShadowSetType shadow = null;

  private transient RealType[] domain_reals;

  float[] default_values;

  /** point on direct manifold line or plane */
  private float point_x, point_y, point_z;
  /** normalized direction of line or perpendicular to plane */
  private float line_x, line_y, line_z;
  /** arrays of length one for inverseScaleValues */
  private float[] f = new float[1];
  private float[] d = new float[1];
  private float[] value = new float[2];

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
    RealTupleType real_tuple = ((SetType) type).getDomain();
    domain_reals = real_tuple.getRealComponents();

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
      closeIndex = -1;
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

  int which_set = -1;
  int which_point = -1;

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
      if (closeIndex < 0) {
        if (lineAxis < 0) return; // don't know how to do this
        DisplayRealType dreal = null;
        try {
          dreal = (DisplayRealType)
            Display.DisplaySpatialCartesianTuple.getComponent(lineAxis);
        }
        catch (VisADException e) {
          // do nothing
          System.out.println("drag_direct " + e);
          e.printStackTrace();
        }
        int line_index = getDisplay().getDisplayScalarIndex(dreal);
        float lineAxisValue = (line_index > 0) ? default_values[line_index] :
                                                 (float) dreal.getDefaultValue();
        if (lineAxis == 0) {
          float intersect = (lineAxisValue - o_x) / d_x;
          point_x = lineAxisValue;
          point_y = o_y + intersect * d_y;
          point_z = o_z + intersect * d_z;
        }
        else if (lineAxis == 1) {
          float intersect = (lineAxisValue - o_y) / d_y;
          point_x = o_x + intersect * d_x;
          point_y = lineAxisValue;
          point_z = o_z + intersect * d_z;
        }
        else { // lineAxis == 2
          float intersect = (lineAxisValue - o_z) / d_z;
          point_x = o_x + intersect * d_x;
          point_y = o_y + intersect * d_y;
          point_z = lineAxisValue;
        }
      }
      else { // closeIndex >= 0
        point_x = spatialValues[0][closeIndex];
        point_y = spatialValues[1][closeIndex];
        point_z = spatialValues[2][closeIndex];
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
      addPoint(xx);

      UnionSet newData = null;
      UnionSet data = (UnionSet) link.getData();
      if (data == null) return;
      SampledSet[] sets = data.getSets();
      int n = sets.length;

      
      Vector vect = new Vector();
      for (int i=0; i<3; i++) {
        int j = getAxisToComponent(i);
        if (j >= 0) {
          f[0] = x[i];
          d = getDirectMap(i).inverseScaleValues(f);
          value[j] = d[0];
          RealType rtype = domain_reals[j];
          // create location string
          String valueString = new Real(rtype, d[0]).toValueString();
          vect.addElement(rtype.getName() + " = " + valueString);
        }
      }
      getDisplayRenderer().setCursorStringVector(vect);

      if (closeIndex < 0) {
        if (first) {
          SampledSet[] new_sets = new SampledSet[n+1];
          System.arraycopy(sets, 0, new_sets, 0, n);
          float[][] new_samples = {{value[0]}, {value[1]}};
          new_sets[n] = new Gridded2DSet(type, new_samples, 1,
                                         data.getCoordinateSystem(),
                                         data.getSetUnits(), null);
          newData = new UnionSet(type, new_sets);
        }
        else { // !first
          float[][] samples = sets[n-1].getSamples(false);
          int m = samples[0].length;
          float[][] new_samples = new float[2][m+1];
          System.arraycopy(samples[0], 0, new_samples[0], 0, m);
          System.arraycopy(samples[1], 0, new_samples[1], 0, m);
          new_samples[0][m] = value[0];
          new_samples[1][m] = value[1];
          sets[n-1] = new Gridded2DSet(type, new_samples, m+1,
                                       data.getCoordinateSystem(),
                                       data.getSetUnits(), null);
          newData = new UnionSet(type, sets);
        }
      }
      else { // closeIndex >= 0
        if (first) {
          which_set = -1;
          which_point = -1;
          int w = closeIndex;
          int len = 0;
          for (int i=0; i<n; i++) {
            len = sets[i].getLength();
            if (w < len) {
              which_set = i;
              which_point = w;
              break;
            }
            w -= len;
          }
          if (which_set < 0) return; // shouldn't happen
          float[][] samples = sets[which_set].getSamples(false);
          if (which_point == 0) {
            samples = invert(samples);
            which_point = len - 1;
          }
          samples[0][which_point] = value[0];
          samples[1][which_point] = value[1];

          // int m = samples[0].length;
          if (which_point == (len - 1)) {
            sets = rotate(sets, which_set);
            which_set = n - 1;
            closeIndex = -1;
          }
          // sets[which_set]= new Gridded2DSet(type, samples, m,
          sets[which_set]= new Gridded2DSet(type, samples, len,
                                     data.getCoordinateSystem(),
                                     data.getSetUnits(), null);
          newData = new UnionSet(type, sets);
        }
        else { // !first
          if (which_set < 0) return; // shouldn't happen
          float[][] samples = sets[which_set].getSamples(false);
          int len = samples[0].length;

          // figure out which direction to look for new nearest point
          int dir = 0;
          if (which_point == 0) {
            dir = 1; // cannot happen
          }
          else if (which_point == len - 1) {
            dir = -1; // cannot happen
          }
          else {
            float v0 = value[0] - samples[0][which_point];
            float v1 = value[1] - samples[1][which_point];
            float v = (float) Math.sqrt(v0 * v0 + v1 * v1);
            int NPTS = 5;
            int wp = which_point + NPTS;
            if (wp > len - 2) wp = len - 2;
            float pplus = 0;
            float psum = 0.0f;
            for (int i=which_point; i<wp; i++) {
              float vp0 = samples[0][i + 1] - samples[0][i];
              float vp1 = samples[1][i + 1] - samples[1][i];
              float vp = (float) Math.sqrt(vp0 * vp0 + vp1 * vp1);
              float pp = (v0 * vp0 + v1 * vp1) / v * vp;
              if (pp > 0.0) pplus += 1;
              psum += pp;
            }
            float pdiv = (wp - which_point) > 0 ? (wp - which_point) : 1.0f;
            pplus = pplus / pdiv;
            psum = psum / pdiv;

            int wm = which_point - NPTS;
            if (wm < 1) wm = 1;
            float mplus = 0;
            float msum = 0.0f;
            for (int i=which_point; i>wm; i--) {
              float vm0 = samples[0][i - 1] - samples[0][i];
              float vm1 = samples[1][i - 1] - samples[1][i];
              float vm = (float) Math.sqrt(vm0 * vm0 + vm1 * vm1);
              float mm = (v0 * vm0 + v1 * vm1) / v * vm;
              if (mm > 0.0) mplus += 1;
              msum += mm;
            }
            float mdiv = (which_point - wm) > 0 ? (which_point - wm) : 1.0f;
            mplus = mplus / mdiv;
            msum = msum / mdiv;
            dir = (pplus > mplus) ? 1 : -1;
            if (pplus == mplus) dir = (psum > msum) ? 1 : -1;
          }

          float distance = Float.MAX_VALUE; // actually distance squared
          int new_point = -1;
          if (dir > 0) {
            for (int i=which_point + 1; i<len; i++) {
              float d = (samples[0][i] - value[0]) * (samples[0][i] - value[0]) +
                        (samples[1][i] - value[1]) * (samples[1][i] - value[1]);
              if (d < distance) {
                distance = d;
                new_point = i;
              }
            }
            if (new_point < 0) return; // shouldn't happen
            if (which_point + 1 == new_point) {
              if (which_point + 2 == len) {
                samples[0][which_point + 1] = value[0];
                samples[1][which_point + 1] = value[1];
                which_point = which_point + 1;
              }
              else {
                float[][] new_samples = new float[2][len + 1];
                System.arraycopy(samples[0], 0, new_samples[0], 0, which_point + 1);
                System.arraycopy(samples[1], 0, new_samples[1], 0, which_point + 1);
                new_samples[0][which_point + 1] = value[0];
                new_samples[1][which_point + 1] = value[1];
                System.arraycopy(samples[0], which_point + 1, new_samples[0],
                                 which_point + 2, len - (which_point + 1));
                System.arraycopy(samples[1], which_point + 1, new_samples[1],
                                 which_point + 2, len - (which_point + 1));
                samples = new_samples;
                which_point = which_point + 1;
                len++;
              }
            }
            else if (which_point + 2 == new_point) {
              samples[0][which_point + 1] = value[0];
              samples[1][which_point + 1] = value[1];
              which_point = which_point + 1;
            }
            else { // which_point + 2 < new_point
              int new_len = len - (new_point - (which_point + 2));
              float[][] new_samples = new float[2][new_len];
              System.arraycopy(samples[0], 0, new_samples[0], 0, which_point + 1);
              System.arraycopy(samples[1], 0, new_samples[1], 0, which_point + 1);
              new_samples[0][which_point + 1] = value[0];
              new_samples[1][which_point + 1] = value[1];
              System.arraycopy(samples[0], new_point, new_samples[0],
                               which_point + 2, len - new_point);
              System.arraycopy(samples[1], new_point, new_samples[1],
                               which_point + 2, len - new_point);
              samples = new_samples;
              which_point = which_point + 1;
              len = new_len;
            }
          }
          else { // if (dir < 0)
            for (int i=0; i<which_point; i++) {
              float d = (samples[0][i] - value[0]) * (samples[0][i] - value[0]) +
                        (samples[1][i] - value[1]) * (samples[1][i] - value[1]);
              if (d < distance) {
                distance = d;
                new_point = i;
              }
            }
            if (new_point < 0) return; // shouldn't happen
            if (new_point == which_point - 1) {
              if (which_point - 1 == 0) {
                samples[0][which_point - 1] = value[0];
                samples[1][which_point - 1] = value[1];
                which_point = which_point - 1;
              }
              else {
                float[][] new_samples = new float[2][len + 1];
                System.arraycopy(samples[0], 0, new_samples[0], 0, which_point);
                System.arraycopy(samples[1], 0, new_samples[1], 0, which_point);
                new_samples[0][which_point] = value[0];
                new_samples[1][which_point] = value[1];
                System.arraycopy(samples[0], which_point, new_samples[0],
                                 which_point + 1, len - which_point);
                System.arraycopy(samples[1], which_point, new_samples[1],
                                 which_point + 1, len - which_point);
                samples = new_samples;
                len++;
              }
            }
            else if (new_point == which_point - 2) {
              samples[0][which_point - 1] = value[0];
              samples[1][which_point - 1] = value[1];
              which_point = which_point - 1;
            }
            else { // new_point < which_point - 2
              int new_len = len - ((which_point - 2) - new_point);
              float[][] new_samples = new float[2][new_len];
              System.arraycopy(samples[0], 0, new_samples[0], 0, new_point + 1);
              System.arraycopy(samples[1], 0, new_samples[1], 0, new_point + 1);
              new_samples[0][new_point + 1] = value[0];
              new_samples[1][new_point + 1] = value[1];
              System.arraycopy(samples[0], which_point, new_samples[0],
                               new_point + 2, len - which_point);
              System.arraycopy(samples[1], which_point, new_samples[1],
                               new_point + 2, len - which_point);
              samples = new_samples;
              which_point = new_point + 1;
              len = new_len;
            }
          } // if end (dir < 0)


          if (which_point == 0) {
            samples = invert(samples);
            which_point = len - 1;
          }

          if (which_point == (len - 1)) {
            sets = rotate(sets, which_set);
            which_set = n - 1;
            closeIndex = -1;
          }
          sets[which_set]= new Gridded2DSet(type, samples, len,
                                     data.getCoordinateSystem(),
                                     data.getSetUnits(), null);
          newData = new UnionSet(type, sets);

        }
      }

      ref.setData(newData);
      link.clearData();

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

  private float[][] invert(float[][] samples) {
    int m = samples[0].length;
    float[][] new_samples = new float[2][m];
    int m1 = m - 1;
    for (int i=0; i<m; i++) {
      new_samples[0][i] = samples[0][m1 - i];
      new_samples[1][i] = samples[1][m1 - i];
    }
    return new_samples;
  }

  private SampledSet[] rotate(SampledSet[] sets, int which_set) {
    int n = sets.length;
    int k = (n - 1) - which_set;
    if (k == 0) return sets;
    SampledSet[] new_sets = new SampledSet[n];
    if (which_set > 0) {
      System.arraycopy(sets, 0, new_sets, 0, which_set);
    }
    if (k > 0) {
      System.arraycopy(sets, which_set + 1, new_sets, which_set, k);
    }
    new_sets[n - 1] = sets[which_set];
    return new_sets;
  }

  /** test CurveManipulationRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealTupleType earth = new RealTupleType(lat, lon);

    // construct invisible starter set
    Gridded2DSet set1 =
      new Gridded2DSet(earth, new float[][] {{0.0f, 0.0f}, {0.0f, 0.0f}}, 2);
    Gridded2DSet[] sets = {set1};
    UnionSet set = new UnionSet(earth, sets);

    // construct Java3D display and mappings
    DisplayImpl display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    lonmap.setRange(-1.0, 1.0);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(0.0, 3.0);

    DataReferenceImpl ref = new DataReferenceImpl("set");
    ref.setData(set);
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

    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
    button_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    button_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    CurveDelete cd = new CurveDelete(ref, display);
    JButton del = new JButton("delete last");
    del.addActionListener(cd);
    del.setActionCommand("del");
    button_panel.add(del);
    JButton fill = new JButton("fill");
    fill.addActionListener(cd);
    fill.setActionCommand("fill");
    button_panel.add(fill);
    panel.add(button_panel);
    JButton lines = new JButton("lines");
    lines.addActionListener(cd);
    lines.setActionCommand("lines");
    button_panel.add(lines);
    panel.add(button_panel);

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}

class CurveDelete implements ActionListener {

  DataReferenceImpl ref;
  DisplayImpl display;
  boolean lines = false;
  DataReferenceImpl new_ref;

  CurveDelete(DataReferenceImpl r, DisplayImpl d) {
    ref = r;
    display = d;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("del")) {
      try {
        UnionSet set = (UnionSet) ref.getData();
        SampledSet[] sets = set.getSets();
        SampledSet[] new_sets = new SampledSet[sets.length - 1];
        System.arraycopy(sets, 0, new_sets, 0, sets.length - 1);
        ref.setData(new UnionSet(set.getType(), new_sets));
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
    else if (cmd.equals("fill")) {
      try {
        UnionSet set = (UnionSet) ref.getData();
        System.out.println("area = " + DelaunayCustom.computeArea(set));
        // Irregular2DSet new_set = DelaunayCustom.fill(set);
        Irregular2DSet new_set = DelaunayCustom.fillCheck(set, false);
        if (new_set != null) {
          if (new_ref == null) {
            new_ref = new DataReferenceImpl("fill");
            ConstantMap[] cmaps = new ConstantMap[]
              {new ConstantMap(1.0, Display.Blue),
               new ConstantMap(1.0, Display.Red),
               new ConstantMap(0.0, Display.Green)};
            display.addReference(new_ref, cmaps);
          }
          new_ref.setData(new_set);
        }
      }
      catch (VisADException ex) {
        System.out.println(ex.getMessage());
      }
      catch (RemoteException ex) {
        System.out.println(ex.getMessage());
      }
    }
    else if (cmd.equals("lines")) {
      try {
        lines = !lines;
        GraphicsModeControl mode = display.getGraphicsModeControl();
        if (lines) {
          mode.setPolygonMode(DisplayImplJ3D.POLYGON_LINE);
        }
        else {
          mode.setPolygonMode(DisplayImplJ3D.POLYGON_FILL);
        }
      }
      catch (VisADException ex) {
        System.out.println(ex.getMessage());
      }
      catch (RemoteException ex) {
        System.out.println(ex.getMessage());
      }
    }
  }
}

