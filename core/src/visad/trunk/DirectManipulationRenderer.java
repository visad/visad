
//
// DirectManipulationRenderer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

package visad;

import javax.media.j3d.*;
import java.vecmath.*;

import java.util.*;
import java.rmi.*;


/**
   DirectManipulationRenderer is the VisAD class for the default graphics rendering
   algorithm.<P>
*/
public class DirectManipulationRenderer extends Renderer {

  private float[][] spatialValues = null;

  /** if Function, last domain index and range values */
  private int lastIndex = -1;
  double[] lastD = null;

  /** index into spatialValues found by checkClose */
  private int closeIndex = -1;

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient ShadowType type = null;
  private transient DataReference ref = null;
  private transient Data data = null;

  /** point on direct manifold line or plane */
  private float point_x, point_y, point_z;
  /** normalized direction of line or perpendicular to plane */
  private float line_x, line_y, line_z;

  /** arrays of length one for inverseScaleValues */
  private float[] f = new float[1];
  private double[] d = new double[1];
  private float[][] value = new float[1][1];

  public DirectManipulationRenderer () {
  }

  void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DirectManipulationRenderer.setLinks: must be " +
                                 "exactly one DataDisplayLink");
    }
    super.setLinks(links, d);
  }

  /** set spatialValues from ShadowType.doTransform */
  void setSpatialValues(float[][] spatial_values) {
    spatialValues = spatial_values;
  }

  /** find minimum distance from ray to spatialValues */
  float checkClose(Point3d origin, Vector3d direction) {
    float distance = Float.MAX_VALUE;
    lastIndex = -1;
    if (spatialValues == null) return distance;
    float o_x = (float) origin.x;
    float o_y = (float) origin.y;
    float o_z = (float) origin.z;
    float d_x = (float) direction.x;
    float d_y = (float) direction.y;
    float d_z = (float) direction.z;
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
    }
    return distance;
  }

  void drag_direct(PickRay ray, boolean first) {
    // System.out.println("drag_direct " + first);
    if (spatialValues == null || ref == null || type == null) return;
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();
    ray.get(origin, direction);
    float o_x = (float) origin.x;
    float o_y = (float) origin.y;
    float o_z = (float) origin.z;
    float d_x = (float) direction.x;
    float d_y = (float) direction.y;
    float d_z = (float) direction.z;

    if (first) {
      point_x = spatialValues[0][closeIndex];
      point_y = spatialValues[1][closeIndex];
      point_z = spatialValues[2][closeIndex];
      int lineAxis = -1;
      if (type.directManifoldDimension == 3) {
        line_x = d_x;
        line_y = d_y;
        line_z = d_z;
      }
      else {
        if (type.directManifoldDimension == 2) {
          for (int i=0; i<3; i++) {
            if (type.axisToComponent[i] < 0 && type.domainAxis != i) {
              lineAxis = i;
            }
          }
        }
        else if (type.directManifoldDimension == 1) {
          for (int i=0; i<3; i++) {
            if (type.axisToComponent[i] >= 0) {
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
    if (type.directManifoldDimension == 1) {
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
    else { // type.directManifoldDimension = 2 or 3
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
    //
    // TO_DO
    // might estimate errors from pixel resolution on screen
    //
    try {
      Data newData = null;
      if (type instanceof ShadowRealType) {
        for (int i=0; i<3; i++) {
          if (type.axisToComponent[i] >= 0) {
            f[0] = x[i];
            d = type.directMap[i].inverseScaleValues(f);
            RealType rtype = (RealType) data.getType();
            newData = new Real(rtype, d[0], rtype.getDefaultUnit(), null);
            // create location string
            Vector vect = new Vector();
            float g = (float) d[0];
            vect.addElement(rtype.getName() + " = " + g);
            displayRenderer.setCursorStringVector(vect);
            break;
          }
        }
        ref.setData(newData);
      }
      else if (type instanceof ShadowRealTupleType) {
        int n = ((RealTuple) data).getDimension();
        Real[] reals = new Real[n];
        Vector vect = new Vector();
        for (int i=0; i<3; i++) {
          int j = type.axisToComponent[i];
          if (j >= 0) {
            f[0] = x[i];
            d = type.directMap[i].inverseScaleValues(f);
            Real c = (Real) ((RealTuple) data).getComponent(j);
            RealType rtype = (RealType) c.getType();
            reals[j] = new Real(rtype, d[0], rtype.getDefaultUnit(), null);
            // create location string
            float g = (float) d[0];
            vect.addElement(rtype.getName() + " = " + g);
          }
        }
        displayRenderer.setCursorStringVector(vect);
        for (int j=0; j<n; j++) {
          if (reals[j] == null) {
            reals[j] = (Real) ((RealTuple) data).getComponent(j);
          }
        }
        newData = new RealTuple(reals);
        ref.setData(newData);
      }
      else if (type instanceof ShadowFunctionType) {
        Vector vect = new Vector();
        if (first) lastIndex = -1;
        int k = type.domainAxis;
        f[0] = x[k]; 
        d = type.directMap[k].inverseScaleValues(f);
        // create location string
        float g = (float) d[0];
        RealType rtype = type.directMap[k].getScalar();
        vect.addElement(rtype.getName() + " = " + g);
        // convert domain value to domain index
        Gridded1DSet set = (Gridded1DSet) ((Field) data).getDomainSet();
        value[0][0] = (float) d[0];
        int[] indices = set.valueToIndex(value);
        int thisIndex = indices[0];
        if (thisIndex < 0) {
          lastIndex = -1;
          return;
        }
        int n;
        ShadowType range = ((ShadowFunctionType) type).getRange();
        if (range instanceof ShadowRealType) {
          n = 1;
        }
        else {
          n = ((ShadowRealTupleType) range).getDimension();
        }
        double[] thisD = new double[n];
        boolean[] directComponent = new boolean[n];
        for (int j=0; j<n; j++) {
          thisD[j] = Double.NaN;
          directComponent[j] = false;
        }
        for (int i=0; i<3; i++) {
          int j = type.axisToComponent[i];
          if (j >= 0) {
            f[0] = x[i];
            d = type.directMap[i].inverseScaleValues(f);
            // create location string
            g = (float) d[0];
            rtype = type.directMap[i].getScalar();
            vect.addElement(rtype.getName() + " = " + g);
            thisD[j] = d[0];
            directComponent[j] = true;
          }
        }
        displayRenderer.setCursorStringVector(vect);
        if (lastIndex < 0) {
          lastIndex = thisIndex;
          lastD = new double[n];
          for (int j=0; j<n; j++) {
            lastD[j] = thisD[j];
          }
        }
        Real[] reals = new Real[n];
        int m = Math.abs(lastIndex - thisIndex) + 1;
        indices = new int[m];
        int index = thisIndex;
        int inc = (lastIndex >= thisIndex) ? 1 : -1;
        for (int i=0; i<m; i++) {
          indices[i] = index;
          index += inc;
        }
        float[][] values = set.indexToValue(indices);
        double coefDiv = values[0][m-1] - values[0][0];
        for (int i=0; i<m; i++) {
          index = indices[i];
          double coef = (i == 0 || coefDiv == 0.0) ? 0.0 :
                          (values[0][i] - values[0][0]) / coefDiv;
          Data tuple = ((Field) data).getSample(index);
          if (tuple instanceof Real) {
            if (directComponent[0]) {
              rtype = (RealType) tuple.getType();
              tuple = new Real(rtype, thisD[0] + coef * (lastD[0] - thisD[0]),
                               rtype.getDefaultUnit(), null);
            }
          }
          else {
            for (int j=0; j<n; j++) {
              Real c = (Real) ((RealTuple) tuple).getComponent(j);
              if (directComponent[j]) {
                rtype = (RealType) c.getType();
                reals[j] = new Real(rtype, thisD[j] + coef * (lastD[j] - thisD[j]),
                                    rtype.getDefaultUnit(), null);
              }
              else {
                reals[j] = c;
              }
            }
            tuple = new RealTuple(reals);
          }
          ((Field) ref.getData()).setSample(index, tuple);
          // ((Field) data).setSample(index, tuple);
        } // end for (int i=0; i<m; i++)

        // set last index to this, and component values
        lastIndex = thisIndex;
        for (int j=0; j<n; j++) {
          lastD[j] = thisD[j];
        }
      } // end else if (type instanceof ShadowFunctionType)
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

  /** create a BranchGroup scene graph for Data in links[0] */
  public BranchGroup doTransform() throws VisADException, RemoteException {
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);

    // values needed by drag_direct, which cannot throw Exceptions
    link = Links[0];
    ref = link.getDataReference();
    data = link.getData();
    type = link.getShadow();

    // check type and maps for valid direct manipulation
    if (!type.isDirectManipulation) {
      throw new BadDirectManipulationException(
        "DirectManipulationRenderer.doTransform: " + type.whyNotDirect);
    }

    // initialize valueArray to missing
    float[] valueArray = new float[display.valueArrayLength];
    for (int i=0; i<display.valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }

    // no preProcess or postProcess for direct manipulation */
    type.doTransform(branch, data, valueArray, link.getDefaultValues(), this);

    return branch;
  }

  void addSwitch(DisplayRenderer displayRenderer, BranchGroup branch) {
    displayRenderer.addDirectManipulationSceneGraphComponent(branch, this);
  }

}

