
//
// DataRenderer.java
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

package visad;

import java.util.*;
import java.rmi.*;


/**
   DataRenderer is the VisAD abstract super-class for graphics rendering
   algorithms.  These transform Data objects into 3-D (or 2-D)
   depictions in a Display window.<P>

   DataRenderer is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class DataRenderer extends Object {

  private DisplayImpl display;
  /** used to insert output into scene graph */
  private DisplayRenderer displayRenderer;

  /** links to Data to be renderer by this */
  private transient DataDisplayLink[] Links;
  /** flag from DataDisplayLink.prepareData */
  private boolean[] feasible; // it's a miracle if this is correct
  /** flag to indicate that DataDisplayLink.prepareData was invoked */
  private boolean[] changed;

  private boolean any_changed;
  private boolean all_feasible;
  private boolean any_transform_control;

  /** a Vector of BadMappingException and UnimplementedException
      Strings generated during the last invocation of doAction */
  private Vector exceptionVector = new Vector();

  public DataRenderer() {
    Links = null;
    display = null;
  }

  public void clearExceptions() {
    exceptionVector.removeAllElements();
  }

  /** add message from BadMappingException or
      UnimplementedException to exceptionVector */
  public void addException(Exception error) {
    exceptionVector.addElement(error);
    // System.out.println(error.getMessage());
  }

  /** get a clone of exceptionVector to avoid
      concurrent access by Display thread */
  public Vector getExceptionVector() {
    return (Vector) exceptionVector.clone();
  }

  public boolean get_all_feasible() {
    return all_feasible;
  }

  public boolean get_any_changed() {
    return any_changed;
  }

  public boolean get_any_transform_control() {
    return any_transform_control;
  }

  public void set_all_feasible(boolean b) {
    all_feasible = b;
  }

  public abstract void setLinks(DataDisplayLink[] links, DisplayImpl d)
           throws VisADException;

  public void setLinks(DataDisplayLink[] links) {
    Links = links;
    feasible = new boolean[Links.length];
    changed = new boolean[Links.length];
    for (int i=0; i<Links.length; i++) feasible[i] = false;
  }

  public DataDisplayLink[] getLinks() {
    return Links;
  }

  public DisplayImpl getDisplay() {
    return display;
  }

  public void setDisplay(DisplayImpl d) {
    display = d;
  }

  public DisplayRenderer getDisplayRenderer() {
    return displayRenderer;
  }

  public void setDisplayRenderer(DisplayRenderer r) {
    displayRenderer = r;
  }

  /** check if re-transform is needed; if initialize is true then
      compute ranges for RealType-s and Animation sampling */
  public DataShadow prepareAction(boolean initialize, DataShadow shadow)
         throws VisADException, RemoteException {
    any_changed = false;
    all_feasible = true;
    any_transform_control = false;
 
    for (int i=0; i<Links.length; i++) {
      changed[i] = false;
      DataReference ref = Links[i].getDataReference();
      // test for changed Controls that require doTransform
      if (Links[i].checkTicks() || !feasible[i] || initialize) {
        // data has changed - need to re-display
        changed[i] = true;
        any_changed = true;
        // create ShadowType for data, classify data for display
        feasible[i] = Links[i].prepareData();
        if (!feasible[i]) all_feasible = false;
        if (initialize && feasible[i]) {
          // compute ranges of RealTypes and Animation sampling
          ShadowType type = Links[i].getShadow().getAdaptedShadowType();
          if (shadow == null) {
            shadow =
              Links[i].getData().computeRanges(type, display.getScalarCount());
          }
          else {
            shadow = Links[i].getData().computeRanges(type, shadow);
          }
        }
      } // end if (Links[i].checkTicks() || !feasible[i] || initialize)
 
      if (feasible[i]) {
        // check if this Data includes any changed Controls
        Enumeration maps = Links[i].getSelectedMapVector().elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap) maps.nextElement();
          if (map.checkTicks(this, Links[i])) {
            any_transform_control = true;
          }
        }
      } // end if (feasible[i])
    } // end for (int i=0; i<Links.length; i++)
    return shadow;
  }

  /** re-transform if needed;
      return false if not done */
  public abstract boolean doAction() throws VisADException, RemoteException;

  public boolean getBadScale() {
    boolean badScale = false;
    for (int i=0; i<Links.length; i++) {
      if (!feasible[i]) return true;
      Enumeration maps = Links[i].getSelectedMapVector().elements();
      while(maps.hasMoreElements()) {
        badScale |= ((ScalarMap) maps.nextElement()).badRange();
      }
    }
    return badScale;
  }

  public abstract void clearScene();

  public void clearAVControls() {
    Enumeration controls = display.getControlVector().elements();
    while (controls.hasMoreElements()) {
      Control control = (Control) controls.nextElement();
      if (control instanceof AVControl) {
        ((AVControl) control).clearSwitches(this);
      }
    }
  }

  public abstract ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowTextType(
         TextType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** DataRenderer-specific decision about which Controls require re-transform;
      may be over-ridden by DataRenderer sub-classes */
  public boolean isTransformControl(Control control, DataDisplayLink link) {
    if (control instanceof ProjectionControl ||
        control instanceof ToggleControl) {
      return false;
    }
/* WLH 1 Nov 97 - temporary hack -
   RangeControl changes always require Transform
   ValueControl and AnimationControl never do

    if (control instanceof AnimationControl ||
        control instanceof ValueControl ||
        control instanceof RangeControl) {
      return link.isTransform[control.getIndex()];
*/
    if (control instanceof AnimationControl ||
        control instanceof ValueControl) {
      return false;
    }
    return true;
  }


  /* **************************** */
  /**  direct manipulation stuff  */
  /* **************************** */


  private float[][] spatialValues = null;

  /** if Function, last domain index and range values */
  private int lastIndex = -1;
  double[] lastD = null;
  float[] lastX = new float[6];

  /** index into spatialValues found by checkClose */
  private int closeIndex = -1;

  /** for use in drag_direct */
  private transient DataDisplayLink link = null;
  // private transient ShadowTypeJ3D type = null;
  private transient DataReference ref = null;
  private transient MathType type = null;
  private transient ShadowType shadow = null;


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
  private final static String multipleSpatialMapping =
    "RealType with multiple spatial mappings";
  private final static String nonCartesian =
    "no spatial or non-Cartesian spatial mapping";
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

  public synchronized void realCheckDirect()
         throws VisADException, RemoteException {
    setIsDirectManipulation(false);

    link = getLinks()[0];
    ref = link.getDataReference();
    shadow = link.getShadow().getAdaptedShadowType();
    type = link.getType();

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
      else if (shadow.getMultipleSpatialDisplayScalar()) {
        whyNotDirect = multipleSpatialMapping;
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
        throw new DisplayException("DataRenderer.realCheckDirect:" +
                                   "too few spatial domain");
      }
      if (directManifoldDimension < 2) {
        throw new DisplayException("DataRenderer.realCheckDirect:" +
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
      else if (shadow.getMultipleSpatialDisplayScalar()) {
        whyNotDirect = multipleSpatialMapping;
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
      else if (shadow.getMultipleSpatialDisplayScalar()) {
        whyNotDirect = multipleSpatialMapping;
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
 
  public String getWhyNotDirect() {
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
  public synchronized void setSpatialValues(float[][] spatial_values) {
    spatialValues = spatial_values;
  }

  /** find minimum distance from ray to spatialValues */
  public synchronized float checkClose(double[] origin, double[] direction) {
    float distance = Float.MAX_VALUE;
    lastIndex = -1;
    if (spatialValues == null) return distance;
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
      }
    }
    return distance;
  }

  public synchronized void drag_direct(VisADRay ray, boolean first) {
    // System.out.println("drag_direct " + first);
    if (spatialValues == null || ref == null || shadow == null) return;
    float o_x = (float) ray.position[0];
    float o_y = (float) ray.position[1];
    float o_z = (float) ray.position[2];
    float d_x = (float) ray.vector[0];
    float d_y = (float) ray.vector[1];
    float d_z = (float) ray.vector[2];

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
      if (type instanceof RealType) {
        addPoint(x);
        for (int i=0; i<3; i++) {
          if (getAxisToComponent(i) >= 0) {
            f[0] = x[i];
            d = getDirectMap(i).inverseScaleValues(f);
            // RealType rtype = (RealType) data.getType();
            RealType rtype = (RealType) type;
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
      else if (type instanceof RealTupleType) {
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
      else if (type instanceof FunctionType) {
        Vector vect = new Vector();
        if (first) lastIndex = -1;
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

        int n;
        MathType range = ((FunctionType) type).getRange();
        if (range instanceof RealType) {
          n = 1;
        }
        else {
          n = ((RealTupleType) range).getDimension();
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
      } // end else if (type instanceof FunctionType)
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

  public void addPoint(float[] x) throws VisADException {
  }



  /** flag indicating whether DirectManipulationRenderer is valid
      for this ShadowType */
  private boolean isDirectManipulation;

  public void checkDirect() throws VisADException, RemoteException {
    isDirectManipulation = false;
  }
 
  public void setIsDirectManipulation(boolean b) {
    isDirectManipulation = b;
  }
 
  public boolean getIsDirectManipulation() {
    return isDirectManipulation;
  }
 
/*
  public void drag_direct(VisADRay ray, boolean first) {
    throw new VisADError("DataRenderer.drag_direct: not direct " +
                         "manipulation renderer");
  }
*/


}

