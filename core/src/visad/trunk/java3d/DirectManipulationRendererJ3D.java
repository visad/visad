
//
// DirectManipulationRendererJ3D.java
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

package visad.java3d;
 
import visad.*;

import javax.media.j3d.*;
import java.vecmath.*;

import java.util.*;
import java.rmi.*;


/**
   DirectManipulationRendererJ3D is the VisAD class for the default graphics rendering
   algorithm.<P>
*/
public class DirectManipulationRendererJ3D extends RendererJ3D {

  BranchGroup branch = null;

  private float[][] spatialValues = null;

  /** if Function, last domain index and range values */
  private int lastIndex = -1;
  double[] lastD = null;
  float[] lastX = new float[6];

  /** index into spatialValues found by checkClose */
  private int closeIndex = -1;

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  private transient ShadowTypeJ3D type = null;
  private transient DataReference ref = null;

  /** point on direct manifold line or plane */
  private float point_x, point_y, point_z;
  /** normalized direction of line or perpendicular to plane */
  private float line_x, line_y, line_z;

  /** arrays of length one for inverseScaleValues */
  private float[] f = new float[1];
  private double[] d = new double[1];
  private float[][] value = new float[1][1];

  /** information calculated by checkDirect */
  /** explanation for invalid use of DirectManipulationRenderer */
  private String whyNotDirect = null;
  /** mapping from spatial axes to tuple component */
  private int[] axisToComponent = {-1, -1, -1};
  /** mapping from spatial axes to ScalarMaps */
  private ScalarMap[] directMap = {null, null, null};
  /** spatial axis for Function domain */
  private int domainAxis = -1;
  /** dimension of direct manipulation
      (including any Function domain) */
  private int directManifoldDimension = 0;
 
  /** possible values for whyNotDirect */
  private final static String notRealFunction =
    "FunctionType must be Real";
  private final static String notSimpleField =
    "not simple field";
  private final static String notSimpleTuple =
    "not simple tuple";
  private final static String multipleMapping =
    "RealType with multiple mappings";
  private final static String nonCartesian =
    "mapping to non-Cartesian spatial display tuple";
  private final static String viaReference =
    "spatial mapping through Reference";
  private final static String domainDimension =
    "domain dimension must be 1";
  private final static String domainNotSpatial =
    "domain must be mapped to spatial";
  private final static String rangeType =
    "range must be RealType or RealTupleType";
  private final static String rangeNotSpatial =
    "range must be mapped to spatial";
  private final static String domainSet =
    "domain Set must be Gridded1DSet";

  public DirectManipulationRendererJ3D () {
    super();
  }

  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (links == null || links.length != 1) {
      throw new DisplayException("DirectManipulationRendererJ3D.setLinks: " +
                                 "must be exactly one DataDisplayLink");
    }
    super.setLinks(links, d);
  }

  public synchronized void checkDirect() throws VisADException, RemoteException {
    setIsDirectManipulation(false);

    link = getLinks()[0];
    ref = link.getDataReference();
    ShadowType shadow = link.getShadow().getAdaptedShadowType();
    MathType type = link.getType();

    if (type instanceof FunctionType) {
      ShadowRealTupleType domain =
        ((ShadowFunctionType) shadow).getDomain();
      ShadowType range =
        ((ShadowFunctionType) shadow).getRange();
      // there is some redundancy among these conditions
      if (!((FunctionType) type).getReal()) {
        whyNotDirect = notRealFunction;
        return;
      }
      else if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_FIELD) {
        whyNotDirect = notSimpleTuple;
        return;
      }
      else if (shadow.getMultipleDisplayScalar()) {
        whyNotDirect = multipleMapping;
        return;
      }
      else if (domain.getDimension() != 1) {
        whyNotDirect = domainDimension;
        return;
      }
      else if(!(Display.DisplaySpatialCartesianTuple.equals(
                           domain.getDisplaySpatialTuple() ) ) ) {
        whyNotDirect = domainNotSpatial;
        return;
      }
      else if (domain.getSpatialReference()) {
        whyNotDirect = viaReference;
        return;
      }
      DisplayTupleType tuple = null;
      if (range instanceof ShadowRealTupleType) {
        tuple = ((ShadowRealTupleType) range).getDisplaySpatialTuple();
      }
      else if (range instanceof ShadowRealType) {
        tuple = ((ShadowRealType) range).getDisplaySpatialTuple();
      }
      else {
        whyNotDirect = rangeType;
        return;
      }
      if (!Display.DisplaySpatialCartesianTuple.equals(tuple)) {
        whyNotDirect = rangeNotSpatial;
        return;
      }
      else if (range instanceof ShadowRealTupleType &&
               ((ShadowRealTupleType) range).getSpatialReference()) {
        whyNotDirect = viaReference;
        return;
      }
      else if (!(link.getData() instanceof Field) ||
               !(((Field) link.getData()).getDomainSet() instanceof Gridded1DSet)) {
        whyNotDirect = domainSet;
        return;
      }
      setIsDirectManipulation(true);
   
      domainAxis = -1;
      for (int i=0; i<3; i++) {
        axisToComponent[i] = -1;
        directMap[i] = null;
      }
   
      directManifoldDimension =
        setDirectMap((ShadowRealType) domain.getComponent(0), -1, true);
      if (range instanceof ShadowRealType) {
        directManifoldDimension +=
          setDirectMap((ShadowRealType) range, 0, false);
      }
      else if (range instanceof ShadowRealTupleType) {
        ShadowRealTupleType r = (ShadowRealTupleType) range;
        for (int i=0; i<r.getDimension(); i++) {
          directManifoldDimension +=
            setDirectMap((ShadowRealType) r.getComponent(i), i, false);
        }
      }
   
      if (domainAxis == -1) {
        throw new DisplayException("DirectManipulationRendererJ3D.checkDirect:" +
                                   "too few spatial domain");
      }
      if (directManifoldDimension < 2) {
        throw new DisplayException("DirectManipulationRendererJ3D.checkDirect:" +
                                   "directManifoldDimension < 2");
      }
    }
    else if (type instanceof RealTupleType) {
      //
      // TO_DO
      // allow for any Flat ShadowTupleType
      //
      if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_TUPLE) {
        whyNotDirect = notSimpleTuple;
        return;
      }
      else if (shadow.getMultipleDisplayScalar()) {
        whyNotDirect = multipleMapping;
        return;
      }
      else if (!Display.DisplaySpatialCartesianTuple.equals(
                   ((ShadowRealTupleType) shadow).getDisplaySpatialTuple())) {
        whyNotDirect = nonCartesian;
        return;
      }
      else if (((ShadowRealTupleType) shadow).getSpatialReference()) {
        whyNotDirect = viaReference;
        return;
      }
      setIsDirectManipulation(true);
   
      domainAxis = -1;
      for (int i=0; i<3; i++) {
        axisToComponent[i] = -1;
        directMap[i] = null;
      }
   
      directManifoldDimension = 0;
      for (int i=0; i<((ShadowRealTupleType) shadow).getDimension(); i++) {
        directManifoldDimension += setDirectMap((ShadowRealType)
                  ((ShadowRealTupleType) shadow).getComponent(i), i, false);
      }
    }
    else if (type instanceof RealType) {
      if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_TUPLE) {
        whyNotDirect = notSimpleTuple;
        return;
      }
      else if (shadow.getMultipleDisplayScalar()) {
        whyNotDirect = multipleMapping;
        return;
      }
      else if(!Display.DisplaySpatialCartesianTuple.equals(
                   ((ShadowRealType) shadow).getDisplaySpatialTuple())) {
        whyNotDirect = nonCartesian;
        return;
      }
      setIsDirectManipulation(true);
   
      domainAxis = -1;
      for (int i=0; i<3; i++) {
        axisToComponent[i] = -1;
        directMap[i] = null;
      }
      directManifoldDimension = setDirectMap((ShadowRealType) shadow, 0, false);
    } // end else if (type instanceof RealType)
  }

  /** set domainAxis and axisToComponent (domain = false) or
      directMap (domain = true) from real; called by checkDirect */
  synchronized int setDirectMap(ShadowRealType real, int component, boolean domain) {
    Enumeration maps = real.getSelectedMapVector().elements();
    while(maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      DisplayRealType dreal = map.getDisplayScalar();
      if (Display.DisplaySpatialCartesianTuple.equals(dreal.getTuple())) {
        int index = dreal.getTupleIndex();
        if (domain) {
          domainAxis = index;
        }
        else {
          axisToComponent[index] = component;
        }
        directMap[index] = map;
        return 1;
      }
    }
    return 0;
  }

  private int getDirectManifoldDimension() {
    return directManifoldDimension;
  }
 
  private String getWhyNotDirect() {
    return whyNotDirect;
  }
 
  private int getAxisToComponent(int i) {
    return axisToComponent[i];
  }
 
  private ScalarMap getDirectMap(int i) {
    return directMap[i];
  }
 
  private int getDomainAxis() {
    return domainAxis;
  }

  /** set spatialValues from ShadowType.doTransform */
  synchronized void setSpatialValues(float[][] spatial_values) {
    spatialValues = spatial_values;
  }

  /** find minimum distance from ray to spatialValues */
  synchronized float checkClose(Point3d origin, Vector3d direction) {
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

  public void addPoint(float[] x) throws VisADException {
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
    DisplayImplJ3D display = (DisplayImplJ3D) getDisplay();
    GeometryArray geometry = display.makeGeometry(array);
    Appearance appearance =
      ShadowTypeJ3D.makeAppearance(display.getGraphicsModeControl(),
                                   null, null, geometry);
    Shape3D shape = new Shape3D(geometry, appearance);
    BranchGroup group = new BranchGroup();
    group.addChild(shape);
    branch.addChild(group);
  }

  synchronized void drag_direct(PickRay ray, boolean first) {
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
      if (getDirectManifoldDimension() == 3) {
        line_x = d_x;
        line_y = d_y;
        line_z = d_z;
      }
      else {
        if (getDirectManifoldDimension() == 2) {
          for (int i=0; i<3; i++) {
            if (getAxisToComponent(i) < 0 && getDomainAxis() != i) {
              lineAxis = i;
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
    //
    // TO_DO
    // might estimate errors from pixel resolution on screen
    //
    try {
      Data newData = null;
      Data data = link.getData();
      if (type instanceof ShadowRealTypeJ3D) {
        addPoint(x);
        for (int i=0; i<3; i++) {
          if (getAxisToComponent(i) >= 0) {
            f[0] = x[i];
            d = getDirectMap(i).inverseScaleValues(f);
            RealType rtype = (RealType) data.getType();
            newData = new Real(rtype, d[0], rtype.getDefaultUnit(), null);
            // create location string
            Vector vect = new Vector();
            float g = (float) d[0];
            vect.addElement(rtype.getName() + " = " + g);
            getDisplayRenderer().setCursorStringVector(vect);
            break;
          }
        }
        ref.setData(newData);
      }
      else if (type instanceof ShadowRealTupleTypeJ3D) {
        addPoint(x);
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
            reals[j] = new Real(rtype, d[0], rtype.getDefaultUnit(), null);
            // create location string
            float g = (float) d[0];
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
      else if (type instanceof ShadowFunctionTypeJ3D) {
        Vector vect = new Vector();
        if (first) lastIndex = -1;
        if (lastIndex < 0) {
          addPoint(x);
        }
        else {
          lastX[3] = x[0];
          lastX[4] = x[1];
          lastX[5] = x[2];
          addPoint(lastX);
        }
        lastX[0] = x[0];
        lastX[1] = x[1];
        lastX[2] = x[2];
        int k = getDomainAxis();
        f[0] = x[k]; 
        d = getDirectMap(k).inverseScaleValues(f);
        // create location string
        float g = (float) d[0];
        RealType rtype = getDirectMap(k).getScalar();
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
        ShadowTypeJ3D range =
          (ShadowTypeJ3D) ((ShadowFunctionTypeJ3D) type).getRange();
        if (range instanceof ShadowRealTypeJ3D) {
          n = 1;
        }
        else {
          n = ((ShadowRealTupleTypeJ3D) range).getDimension();
        }
        double[] thisD = new double[n];
        boolean[] directComponent = new boolean[n];
        for (int j=0; j<n; j++) {
          thisD[j] = Double.NaN;
          directComponent[j] = false;
        }
        for (int i=0; i<3; i++) {
          int j = getAxisToComponent(i);
          if (j >= 0) {
            f[0] = x[i];
            d = getDirectMap(i).inverseScaleValues(f);
            // create location string
            g = (float) d[0];
            rtype = getDirectMap(i).getScalar();
            vect.addElement(rtype.getName() + " = " + g);
            thisD[j] = d[0];
            directComponent[j] = true;
          }
        }
        getDisplayRenderer().setCursorStringVector(vect);
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
          ((Field) data).setSample(index, tuple);
        } // end for (int i=0; i<m; i++)
        if (ref instanceof RemoteDataReference &&
            !(data instanceof RemoteData)) {
          // ref is Remote and data is local, so we have only modified
          // a local copy and must send it back to ref
          ref.setData(data);
        }
        // set last index to this, and component values
        lastIndex = thisIndex;
        for (int j=0; j<n; j++) {
          lastD[j] = thisD[j];
        }
      } // end else if (type instanceof ShadowFunctionTypeJ3D)
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
  public synchronized BranchGroup doTransform()
         throws VisADException, RemoteException {
    branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(Group.ALLOW_CHILDREN_READ);
    branch.setCapability(Group.ALLOW_CHILDREN_WRITE);
    branch.setCapability(Group.ALLOW_CHILDREN_EXTEND);

    // values needed by drag_direct, which cannot throw Exceptions
    type = (ShadowTypeJ3D) link.getShadow();

    // check type and maps for valid direct manipulation
    if (!getIsDirectManipulation()) {
      throw new BadDirectManipulationException(
        "DirectManipulationRendererJ3D.doTransform: " + getWhyNotDirect());
    }

    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }

    Data data = link.getData();
    if (data == null) {
      branch = null;
      addException("Data is null");
    }
    else {
      // no preProcess or postProcess for direct manipulation */
      type.doTransform(branch, data, valueArray,
                       link.getDefaultValues(), this);
    }
    return branch;
  }

  void addSwitch(DisplayRendererJ3D displayRenderer, BranchGroup branch) {
    displayRenderer.addDirectManipulationSceneGraphComponent(branch, this);
  }

}

