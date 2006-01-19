//
// CurveManipulationRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java2d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


/**
 * CurveManipulationRendererJ3D is the VisAD class for direct
 * manipulation rendering of curves under Java2D, where curves
 * are represented by UnionSets of Gridded2DSets with manifold
 * dimension = 2
 */
public class CurveManipulationRendererJ3D extends DirectManipulationRendererJ3D {

  private int mouseModifiersMask = 0;
  private int mouseModifiersValue = 0;
  private boolean only_one = false;

  /** 
   * Construct a DataRenderer that supports direct manipulation of
   * representations of curves by UnionSets of Gridded2DSets
   * with manifold dimension = 2; the Set's domain RealTypes
   * must be mapped to two of (XAxis, YAxis, ZAxis).
   */
  public CurveManipulationRendererJ3D () {
    this(0, 0);
  }

  /** 
   * Construct a DataRenderer that supports direct manipulation of
   * representations of curves by UnionSets of Gridded2DSets
   * with manifold dimension = 2; the Set's domain RealTypes
   * must be mapped to two of (XAxis, YAxis, ZAxis).
   * mmm and mmv determine whehter SHIFT or CTRL keys are required -
   * this is needed since this is a greedy DirectManipulationRenderer
   * that will grab any right mouse click (that intersects its 2-D
   * sub-manifold) 
   * @param  mmm  mouse modifiers
   * @param  mmv  mouse mask to check.
   */
  public CurveManipulationRendererJ3D (int mmm, int mmv) {
    this(mmm, mmv, false);
  }

  /** 
   * Construct a DataRenderer that supports direct manipulation of
   * representations of curves by UnionSets of Gridded2DSets
   * with manifold dimension = 2; the Set's domain RealTypes
   * must be mapped to two of (XAxis, YAxis, ZAxis).
   * mmm and mmv determine whehter SHIFT or CTRL keys are required -
   * this is needed since this is a greedy DirectManipulationRenderer
   * that will grab any right mouse click (that intersects its 2-D
   * sub-manifold) 
   * @param  mmm  mouse modifiers
   * @param  mmv  mouse mask to check.
   * @param  oo  if true, only one curve should exist at any one time.
   */
  public CurveManipulationRendererJ3D (int mmm, int mmv, boolean oo) {
    super();
    mouseModifiersMask = mmm;
    mouseModifiersValue = mmv;
    only_one = oo;
  }

  /**
   * Create a ShadowType based on the SetType
   * @param    type   SetType of UnionSet.
   * @param    link   DataDisplayLink for DataReference
   * @param    parent Parent ShadowType
   * @throws   VisADException  problem creating a VisAD object
   * @throws   RemoteException  problem creating a remote object
   */
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
  private CoordinateSystem tuplecs;

  private int otherindex = -1;
  private float othervalue;

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

  /** pick error offset, communicated from checkClose() to drag_direct() */
  private float offsetx = 0.0f, offsety = 0.0f, offsetz = 0.0f;
  /** count down to decay offset to 0.0 */
  private int offset_count = 0;
  /** initial offset_count */
  private static final int OFFSET_COUNT_INIT = 30;

  /**
   * Check whether direct manipulation is possible for this Renderer.
   * Set appropriate error conditions if not possible.
   * @throws  VisADException  problem accessing information
   * @throws  RemoteException  problem accessing information for remote objects
   */
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

    int[] indices = {-1, -1};
    for (int i=0; i<components.length; i++) {
      Enumeration maps = components[i].getSelectedMapVector().elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType tuple = dreal.getTuple();
        if (tuple != null &&
            (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
             (tuple.getCoordinateSystem() != null &&
              tuple.getCoordinateSystem().getReference().equals(
              Display.DisplaySpatialCartesianTuple)))) {
          int index = dreal.getTupleIndex();
          axisToComponent[index] = i;
          directMap[index] = map;
          indices[i] = index;
        }
      } // end while (maps.hasMoreElements())
    }

    if (indices[0] < 0 || indices[1] < 0) {
      whyNotDirect = domainNotSpatial;
      return;
    }

    // get default value for other component of tuple
    otherindex = 3 - (indices[0] + indices[1]);
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

  private int getDirectManifoldDimension() {
    return directManifoldDimension;
  }

  /**
   * Get the error messages on why direct manipulation is not possible.
   * @return error messages.
   */
  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  private int getAxisToComponent(int i) {
    return axisToComponent[i];
  }

  private ScalarMap getDirectMap(int i) {
    return directMap[i];
  }

  /**
   * Add a point to the data. Not used, but may be needed for
   * performance.
   * @param x  point to add.
   * @throws VisADException   error occured.
   */
  public void addPoint(float[] x) throws VisADException {
    // may need to do this for performance
  }

// methods customized from DataRenderer:

  /** 
   * Set spatialValues from ShadowType.doTransform.
   * @param spatial_values  spatial values.
   */
  public synchronized void setSpatialValues(float[][] spatial_values) {
    spatialValues = spatial_values;
  }

  /** 
   * Find minimum distance from ray to spatialValues.
   * @param   origin     origin of ray
   * @param   direction  direction of the ray
   * @return  miniumum distance from ray to spatial values
   */
  public synchronized float checkClose(double[] origin, double[] direction) {
    int mouseModifiers = getLastMouseModifiers();
    if ((mouseModifiers & mouseModifiersMask) != mouseModifiersValue) {
      return Float.MAX_VALUE;
    }

    float distance = Float.MAX_VALUE;
    closeIndex = -1;
    if (spatialValues != null) {
      float o_x = (float) origin[0];
      float o_y = (float) origin[1];
      float o_z = (float) origin[2];
      float d_x = (float) direction[0];
      float d_y = (float) direction[1];
      float d_z = (float) direction[2];
  
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

          offsetx = x;
          offsety = y;
          offsetz = z;

        }
  
      }
      if (distance <= getDisplayRenderer().getPickThreshhold()) {
        return distance;
      }
    } // end if (spatialValues != null)

    offsetx = 0.0f;
    offsety = 0.0f;
    offsetz = 0.0f;

    closeIndex = -1;
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

  /** 
   * Mouse button released, ending direct manipulation.  Does nothing
   * now, may need to do this for performance.
   */
  public synchronized void release_direct() {
    // may need to do this for performance
  }

  /**
   * Stop direct manipulation.
   */
  public void stop_direct() {
    stop = true;
  }

  int which_set = -1;
  int which_point = -1;

  /**
   * This method is called when direct manipulation is occuring.
   * This adds points to the Gridded2DSet that comprises the current
   * line being drawn. 
   * @param  ray              ray to dragging point of mouse
   * @param  first            true if this is the first point.
   * @param  mouseModifiers   modifiers used with mouse click.
   */
  public synchronized void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    // System.out.println("drag_direct " + first + " " + ref + " " + shadow);
    // if (spatialValues == null || ref == null || shadow == null) return;
    if (ref == null || shadow == null) return;

    if (first) {
      stop = false;
    }
    else {
      if (stop) return;
    }

    // double[] origin = ray.position;
    double[] origin = {ray.position[0], ray.position[1], ray.position[2]};
    double[] direction = ray.vector;

    if (pickCrawlToCursor) {
      if (first) {
        offset_count = OFFSET_COUNT_INIT;
      }
      else {
        if (offset_count > 0) offset_count--;
      }
      if (offset_count > 0) {
        float mult = ((float) offset_count) / ((float) OFFSET_COUNT_INIT);
        origin[0] += mult * offsetx;
        origin[1] += mult * offsety;
        origin[2] += mult * offsetz;
      }
    }

    try {
      float r = findRayManifoldIntersection(true, origin, direction, tuple,
                                            otherindex, othervalue);
      if (r != r) {
        return;
      }
      float[][] xcs = {{(float) (origin[0] + r * direction[0])},
                      {(float) (origin[1] + r * direction[1])},
                      {(float) (origin[2] + r * direction[2])}};
      float[] xx = {xcs[0][0], xcs[1][0], xcs[2][0]};
      if (tuple != null) {
        if (tuplecs == null) return;
        xcs = tuplecs.fromReference(xcs);
      }
      float[] x = {xcs[0][0], xcs[1][0], xcs[2][0]};

      addPoint(xx);

      UnionSet data;
      try {
        data = (UnionSet) link.getData();
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          return;
        }
        throw re;
      }

      if (data == null) return;
      SampledSet[] sets = data.getSets();
      int n = sets.length;

      UnionSet newData = null;

      // create location string
      Vector vect = new Vector();
      for (int i=0; i<3; i++) {
        int j = getAxisToComponent(i);
        if (j >= 0) {
          f[0] = x[i];

          // WLH 13 Feb 2001
          ScalarMap dm = getDirectMap(i);
          if (dm == null) return;
          d = dm.inverseScaleValues(f);
          // d = getDirectMap(i).inverseScaleValues(f);

          value[j] = d[0];
          RealType rtype = domain_reals[j];

          // WLH 31 Aug 2000
          Real rr = new Real(rtype, d[0]);
          Unit overrideUnit = dm.getOverrideUnit(); // WLH 13 Feb 2001
          // Unit overrideUnit = getDirectMap(i).getOverrideUnit();
          Unit rtunit = rtype.getDefaultUnit();
          // units not part of Time string
          if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
              !RealType.Time.equals(rtype)) {
            double dval = overrideUnit.toThis((double) d[0], rtunit);
            rr = new Real(rtype, dval, overrideUnit);
          }
          String valueString = rr.toValueString();
          vect.addElement(rtype.getName() + " = " + valueString);

        }
      }
      getDisplayRenderer().setCursorStringVector(vect);

      if (closeIndex < 0) {
        if (first) {
          if (only_one) {
            SampledSet[] new_sets = new SampledSet[1];
            float[][] new_samples = {{value[0]}, {value[1]}};
            new_sets[0] = new Gridded2DSet(type, new_samples, 1,
                                           data.getCoordinateSystem(),
                                           data.getSetUnits(), null);
            newData = new UnionSet(type, new_sets);
// System.out.println("drag_direct new");
          }
          else {
            SampledSet[] new_sets = new SampledSet[n+1];
            System.arraycopy(sets, 0, new_sets, 0, n);
            float[][] new_samples = {{value[0]}, {value[1]}};
            new_sets[n] = new Gridded2DSet(type, new_samples, 1,
                                           data.getCoordinateSystem(),
                                           data.getSetUnits(), null);
            newData = new UnionSet(type, new_sets);
          }
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
// System.out.println("drag_direct len = " + len + " which_set = " + which_set);
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

  /**
   * Clone this renderer.
   * @return  new Renderer with same parameters.
   */
  public Object clone() {
    return new CurveManipulationRendererJ3D(mouseModifiersMask,
                                            mouseModifiersValue, only_one);
  }

  private static final int N = 64;

  /** test CurveManipulationRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealTupleType xy = new RealTupleType(x, y);

    RealType c = RealType.getRealType("c");
    FunctionType ft = new FunctionType(xy, c);

    // construct Java3D display and mappings
    DisplayImpl display = null;
    if (args.length == 0) {
      display = new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    }
    else {
      display = new DisplayImplJ3D("display1");
    }
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

    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    displayRenderer.setBoxOn(false);
    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setLineWidth(2.0f);

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
    // display.addReference(field_ref);
    DefaultRendererJ3D renderer = new DefaultRendererJ3D();
    if (args.length > 0 && args[0].equals("radius")) {
      ConstantMap[] cmaps = {new ConstantMap(0.99, Display.Radius)};
      display.addReferences(renderer, field_ref, cmaps);
    }
    else {
      display.addReferences(renderer, field_ref);
      renderer.toggle(false);
    }

    // construct invisible starter set
    Gridded2DSet set1 = (args.length > 0 && args[0].equals("radius")) ?
      new Gridded2DSet(xy, new float[][] {{0.0f}, {0.0f}}, 1) :
      new Gridded2DSet(xy, new float[][] {{-1000.0f}, {-1000.0f}}, 1);
    Gridded2DSet[] sets = {set1};
    UnionSet set = new UnionSet(xy, sets);

    DataReferenceImpl ref = new DataReferenceImpl("set");
    ref.setData(set);
    boolean only_one = (args.length > 1);
    CurveManipulationRendererJ3D cmr =
      new CurveManipulationRendererJ3D(0, 0, only_one);
    display.addReferences(cmr, ref);

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
    if (!only_one) {
      JButton del = new JButton("delete last");
      del.addActionListener(cd);
      del.setActionCommand("del");
      button_panel.add(del);
    }
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

