//
// DataRenderer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
public abstract class DataRenderer extends Object implements Cloneable {

  /** DisplayImpl this is associated with */
  private DisplayImpl display = null;

  /** used to insert output into scene graph */
  private DisplayRenderer displayRenderer = null;

  /** links to Data to be renderer by this */
  private transient DataDisplayLink[] Links = null;

  /** flag from DataDisplayLink.prepareData() */
  private boolean[] feasible; // it's a miracle if this is correct

  private boolean[] is_null; // WLH 7 May 2001

  /** flag to indicate that DataDisplayLink.prepareData() was invoked */
  private boolean[] changed;

  private boolean any_changed;
  private boolean all_feasible;
  private boolean any_transform_control;

  /** a Vector of BadMappingException and UnimplementedException
      Strings generated during the last invocation of doAction() */
  private Vector exceptionVector = new Vector();

  /** flag indicating whether to suppress Exceptions
      generated during doAction() */
  private boolean suppress_exceptions = false;

  /** flag for visibility of Data depictions */
  protected boolean enabled = true;
  
  /** place to hold ProjectionControl Listeners created by this renderer */
  private ArrayList<ControlListener> projCntrlListeners = new ArrayList<ControlListener>();
  
  /** Depth buffer offset and factor for this DataRenderer */
  private boolean hasPolygonOffset = false;
  private float polygonOffset = 0f;
  private float polygonOffsetFactor = 0f;

  /**
   * construct a DataRenderer
   */
  public DataRenderer() {
    Links = null;
    display = null;
  }

  /**
   * clear Vector of Exceptions generated during doAction()
   */
  public void clearExceptions() {
    exceptionVector.removeAllElements();
  }

  /**
   * set a flag indicating whether to suppress Exceptions
   * generated during doAction()
   */
  public void suppressExceptions(boolean suppress) {
    suppress_exceptions = suppress;
  }
    
  /**
   * add a BadMappingException or UnimplementedException to
   * Vector of Exceptions generated during doAction()
   * @param error Exception to add
   */
  public void addException(Exception error) {
    if (display == null) return;
    exceptionVector.addElement(error);
    // System.out.println(error.getMessage());
  }

  /**
   * there is no need to over-ride this method, but it may be invoked
   * by DisplayRenderer; gets a clone of exceptionVector to avoid
   * concurrent access by Display thread
   * @return a Vector of Strings from the BadMappingExceptions
   * and UnimplementedExceptions generated during the last invocation
   * of this DataRenderer's doAction method;
   */
  public Vector getExceptionVector() {
    return (suppress_exceptions ? new Vector() : (Vector) exceptionVector.clone());
  }

  /**
   * @return flag indicating whether depiction generation is feasible
   * for all linked Data
   */
  public boolean get_all_feasible() {
    return all_feasible;
  }

  /**
   * @return flag indicating whether any linked Data have changed
   * since last invocation of prepareAction()
   */
  public boolean get_any_changed() {
    return any_changed;
  }

  /**
   * @return flag indicating whether any Controls associated with
   * ScalarMaps applying to any linked Data have changed and require
   * re-transform
   */
  public boolean get_any_transform_control() {
    return any_transform_control;
  }

  /**
   * set flag indicating whether depiction generation is feasible
   * for all linked Data
   * @param b value to set in flag
   */
  public void set_all_feasible(boolean b) {
    all_feasible = b;
  }

  /**
   * set DataDisplayLinks for linked Data, and set associated DisplayImpl
   * @param links array of DataDisplayLinks to set
   * @param d associated DisplayImpl to set
   * @throws VisADException a VisAD error occurred
   */
  public abstract void setLinks(DataDisplayLink[] links, DisplayImpl d)
           throws VisADException;

  /**
   * Sets the visibility of the data being rendered by this instance.
   *
   * @param on                 Whether or not to render the data.
   */
  public void toggle(boolean on) {
    enabled = on;
  }

  /**
   * Returns the visibility of the data being rendered by this instance.
   *
   * @return                   Whether or not the data is being rendered.
   */
  public boolean getEnabled() {
    return enabled;
  }
  
  /**
   * Returns the list of ProjectionControl listeners created by this.
   * Provides a way to remove listeners from the Control that may only
   * be needed per doTransform.
   * 
   * @return
   */
  public ArrayList<ControlListener> getProjectionControlListeners() {
      return projCntrlListeners;
  }

  /**
   * set DataDisplayLinks for linked Data, including constructing
   * arrays of booleans associated with DataDisplayLinks; called by
   * setLinks(DataDisplayLink[], DisplayImpl)
   * @param links array of DataDisplayLinks to set
   */
  public synchronized void setLinks(DataDisplayLink[] links) {
    if (display == null) return;
    if (links == null || links.length == 0) return;
    Links = links;
    feasible = new boolean[Links.length];
    is_null = new boolean[Links.length];
    changed = new boolean[Links.length];
    for (int i=0; i<Links.length; i++) {
      feasible[i] = false;
      is_null[i] = true;
    }
  }

  /**
   * @return an array of DataDisplayLinks to Data objects to be rendered
   * (Data objects are accessed by DataDisplayLink.getData())
   */
  public DataDisplayLink[] getLinks() {
    return Links;
  }

  /**
   * @return DisplayImpl associated with this DataRenderer
   */
  public DisplayImpl getDisplay() {
    return display;
  }

  /**
   * set DisplayImpl associated with this DataRenderer
   * @param d DisplayImpl to set
   */
  public void setDisplay(DisplayImpl d) {
    display = d;
  }

  /**
   * @return  associated with this DataRenderer
   */
  public DisplayRenderer getDisplayRenderer() {
    return displayRenderer;
  }

  /**
   * set DisplayRenderer associated with this DataRenderer
   * @param r DisplayRenderer to set
   */
  public void setDisplayRenderer(DisplayRenderer r) {
    displayRenderer = r;
  }

  /**
   * @return flag indicating whether there is any pending need
   * for re-transform for this DataRenderer
   */
  public boolean checkAction() {
    if (display == null) return false;
    for (int i=0; i<Links.length; i++) {
      if (Links[i].checkTicks() || !feasible[i]) {
        return true;
      }

      // check if this Data includes any changed Controls
      Enumeration maps = Links[i].getSelectedMapVector().elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        if (map.checkTicks(this, Links[i])) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * check if re-transform is needed; if initialize is true then
   * compute ranges for RealTypes and Animation sampling
   * @param go flag indicating that re-transform is required for
   *           at least one DataRenderer linked to DisplayImpl
   * @param initialize flag indicating that initialization (i.e.,
   *                   auto-scaling) is required
   * @param shadow DataShadow shared by prepareAction() method of
   *               all DataRenderers linked to DisplayImpl
   * @return DataShadow containing ranges and animation sampling
   *         Set (return null if no need for initialization)
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public DataShadow prepareAction(boolean go, boolean initialize,
                                  DataShadow shadow)
         throws VisADException, RemoteException {
    if (display == null) return null;
    any_changed = false;
    all_feasible = true;
    any_transform_control = false;

    for (int i=0; i<Links.length; i++) {
      changed[i] = false;
      DataReference ref = Links[i].getDataReference();
      // test for changed Controls that require doTransform

      boolean do_prepare = Links[i].checkTicks() || !feasible[i] || go;
      if (feasible[i] && !do_prepare) {
        // check if this Data includes any changed Controls
        Enumeration maps = Links[i].getSelectedMapVector().elements();
        while(maps.hasMoreElements()) {
          ScalarMap map = (ScalarMap) maps.nextElement();
          if (map.checkTicks(this, Links[i])) {
            do_prepare = true;
          }
        }
      }
/*
System.out.println("prepareAction " + display.getName() + " " +
                   Links[i].getThingReference().getName() +
                   " Links[" + i + "].checkTicks() = " + Links[i].checkTicks() +
                   " feasible[" + i + "] = " + feasible[i] + " go = " + go +
                   " do_prepare = " + do_prepare);
*/
      if (do_prepare) {
        // data has changed - need to re-display
        changed[i] = true;
        any_changed = true;

        // create ShadowType for data, classify data for display
        try {
          feasible[i] = Links[i].prepareData();
          is_null[i] = (Links[i].getData() == null); // WLH 7 May 2001
        } catch (RemoteException re) {
          if (visad.collab.CollabUtil.isDisconnectException(re)) {
            getDisplay().connectionFailed(this, Links[i]);
            removeLink(Links[i]);
            i--;
            continue;
          }
          throw re;
        }

        if (!feasible[i]) {
          all_feasible = false;
          clearBranch();
        }
        if (initialize && feasible[i]) {
          // compute ranges of RealTypes and Animation sampling
          ShadowType type = Links[i].getShadow().getAdaptedShadowType();
          Data data;
          try {
            data = Links[i].getData();
          } catch (RemoteException re) {
            if (visad.collab.CollabUtil.isDisconnectException(re)) {
              getDisplay().connectionFailed(this, Links[i]);
              removeLink(Links[i]);
              i--;
              continue;
            }
            throw re;
          }

          shadow = computeRanges(data, type, shadow);
        }
      } // end if (Links[i].checkTicks() || !feasible[i] || go)

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
/*
System.out.println("any_changed = " + any_changed +
                   " all_feasible = " + all_feasible +
                   " any_transform_control = " + any_transform_control);
*/
    return shadow;
  }

  /**
   * Compute ranges of values for each RealType in
   * DisplayImpl.RealTypeVector.
   * @param data Data object in which to compute ranges of RealType values
   * @param type ShadowType generated for MathType of data
   * @param shadow DataShadow instance whose contained double[][]
   *               array and animation sampling Set are modified
   *               according to RealType values in data, and used
   *               as return value
   * @return DataShadow instance containing double[][] array
   *         of RealType ranges, and an animation sampling Set
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public DataShadow computeRanges(Data data, ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException {
    if (display == null) return null;
    if (shadow == null) {
      shadow =
        data.computeRanges(type, display.getScalarCount());
    }
    else {
      shadow = data.computeRanges(type, shadow);
    }
    return shadow;
  }

  /**
   * clear part of Display scene graph generated by this DataRenderer
   */
  public abstract void clearBranch();

  /**
   * transform linked Data objects into a scene graph depiction,
   * if any Data object values have changed or relevant Controls
   * have changed; DataRenderers that assume the default
   * implementation of DisplayImpl.doAction can determine
   * whether re-transform is needed by:
   *   (get_all_feasible() &&
   *    (get_any_changed() || get_any_transform_control()))
   * these flags are computed by the default DataRenderer
   * implementation of prepareAction()
   * @return flag indicating if the transform was done successfully
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract boolean doAction() throws VisADException, RemoteException;

  /**
   * @return flag indicating whether initialization (i.e.,
   *         auto-scale) is needed on next re-transform
   */
  public boolean getBadScale(boolean anyBadMap) {
    if (display == null) return false;
    boolean badScale = false;
    for (int i=0; i<Links.length; i++) {
      if (!feasible[i] && (anyBadMap || !is_null[i])) {
/*
try {
  System.out.println("getBadScale not feasible " +
                     Links[i].getThingReference().getName());
}
catch (VisADException e) {}
catch (RemoteException e) {}
*/
        return true;
      }
      Enumeration maps = Links[i].getSelectedMapVector().elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        badScale |= map.badRange();
/*
if (map.badRange()) {
  System.out.println("getBadScale: " + map.getScalar().getName() + " -> " +
                     map.getDisplayScalar().getName());
}
*/
      }
    }
// System.out.println("getBadScale return " + badScale);
    return badScale;
  }

  /**
   * clear any scene graph created by this DataRenderer, and
   * clear all instance variables
   */
  public void clearScene() {
// test for display == null in methods
      
    // remove any ProjectionControl listeners this renderer may have added
    // to the display.
    ProjectionControl pCntrl = display.getProjectionControl();
    Iterator<ControlListener> iter = projCntrlListeners.iterator();
    while (iter.hasNext()) {
      pCntrl.removeControlListener(iter.next());
    }
    
    display = null;
    displayRenderer = null;
    Links = null;
    exceptionVector.removeAllElements();
    projCntrlListeners.clear();

// clear flow rendering and direct manipulation variables
    shadow_data_out = null;
    data_out = null;
    data_units_out = null;
    shadow_data_in = null;
    data_in = null;
    data_units_in = null;
    data_coord_in = null;
    sdo_maps = null;
    sdi_maps = null;
    rvts = new RealVectorType[] {null, null};
    display_coordinate_system = null;
    spatial_tuple = null;
    lat_map = null;
    lon_map = null;

    link = null;
    ref = null;
    type = null;
    shadow = null;
    directMap = new ScalarMap[] {null, null, null};
    tuple = null;
  }

  /**
   * clear all information associated with AnimationControls
   * and ValueControls created by this DataRenderer
   */
  public void clearAVControls() {
    if (display == null) return;
    Enumeration controls = display.getControls(AVControl.class).elements();
    while (controls.hasMoreElements()) {
      ((AVControl )controls.nextElement()).clearSwitches(this);
    }

    // a convenient place to throw this in
    ProjectionControl control = display.getProjectionControl();
    control.clearSwitches(this);

    // a convenient place to throw this in
    lat_index = -1;
    lon_index = -1;
  }

  /**
   * factory method for constructing a subclass of ShadowType appropriate
   * for the graphics API, that also adapts ShadowFunctionType;
   * ShadowType trees are constructed that 'shadow' the MathType trees of
   * Data to be depicted, via recursive calls to buildShadowType() methods
   * of MathType sub-classes, to DataRenderer.makeShadow*Type() methods,
   * to Shadow*Type constructors, then back to buildShadowType() methods;
   * the recursive call chain is initiated by DataDisplayLink.prepareData()
   * calls to buildShadowType() methods of MathType sub-classes;
   * @param type FunctionType that returned ShadowType will shadow
   * @param link DataDisplayLink linking Data to be depicted
   * @param parent parent in ShadowType tree structure
   * @return constructed ShadowType
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /**
   * factory for constructing a subclass of ShadowType appropriate
   * for the graphics API, that also adapts ShadowRealTupleType;
   * ShadowType trees are constructed that 'shadow' the MathType trees of
   * Data to be depicted, via recursive calls to buildShadowType() methods 
   * of MathType sub-classes, to DataRenderer.makeShadow*Type() methods, 
   * to Shadow*Type constructors, then back to buildShadowType() methods;
   * the recursive call chain is initiated by DataDisplayLink.prepareData()
   * calls to buildShadowType() methods of MathType sub-classes;
   * @param type FunctionType that returned ShadowType will shadow
   * @param link DataDisplayLink linking Data to be depicted
   * @param parent parent in ShadowType tree structure
   * @return constructed ShadowType
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /**
   * factory for constructing a subclass of ShadowType appropriate
   * for the graphics API, that also adapts ShadowRealType;
   * ShadowType trees are constructed that 'shadow' the MathType trees of
   * Data to be depicted, via recursive calls to buildShadowType() methods
   * of MathType sub-classes, to DataRenderer.makeShadow*Type() methods,
   * to Shadow*Type constructors, then back to buildShadowType() methods;
   * the recursive call chain is initiated by DataDisplayLink.prepareData()
   * calls to buildShadowType() methods of MathType sub-classes;
   * @param type FunctionType that returned ShadowType will shadow
   * @param link DataDisplayLink linking Data to be depicted
   * @param parent parent in ShadowType tree structure
   * @return constructed ShadowType 
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /**
   * factory for constructing a subclass of ShadowType appropriate
   * for the graphics API, that also adapts ShadowSetType;
   * ShadowType trees are constructed that 'shadow' the MathType trees of
   * Data to be depicted, via recursive calls to buildShadowType() methods
   * of MathType sub-classes, to DataRenderer.makeShadow*Type() methods,
   * to Shadow*Type constructors, then back to buildShadowType() methods;
   * the recursive call chain is initiated by DataDisplayLink.prepareData()
   * calls to buildShadowType() methods of MathType sub-classes;
   * @param type FunctionType that returned ShadowType will shadow
   * @param link DataDisplayLink linking Data to be depicted
   * @param parent parent in ShadowType tree structure
   * @return constructed ShadowType 
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /**
   * factory for constructing a subclass of ShadowType appropriate
   * for the graphics API, that also adapts ShadowTextType;
   * ShadowType trees are constructed that 'shadow' the MathType trees of
   * Data to be depicted, via recursive calls to buildShadowType() methods
   * of MathType sub-classes, to DataRenderer.makeShadow*Type() methods,
   * to Shadow*Type constructors, then back to buildShadowType() methods;
   * the recursive call chain is initiated by DataDisplayLink.prepareData()
   * calls to buildShadowType() methods of MathType sub-classes;
   * @param type FunctionType that returned ShadowType will shadow
   * @param link DataDisplayLink linking Data to be depicted
   * @param parent parent in ShadowType tree structure
   * @return constructed ShadowType 
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract ShadowType makeShadowTextType(
         TextType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /**
   * factory for constructing a subclass of ShadowType appropriate
   * for the graphics API, that also adapts ShadowTupleType;
   * ShadowType trees are constructed that 'shadow' the MathType trees of
   * Data to be depicted, via recursive calls to buildShadowType() methods
   * of MathType sub-classes, to DataRenderer.makeShadow*Type() methods,
   * to Shadow*Type constructors, then back to buildShadowType() methods;
   * the recursive call chain is initiated by DataDisplayLink.prepareData()
   * calls to buildShadowType() methods of MathType sub-classes;
   * @param type FunctionType that returned ShadowType will shadow
   * @param link DataDisplayLink linking Data to be depicted
   * @param parent parent in ShadowType tree structure
   * @return constructed ShadowType 
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /**
   * DataRenderer-specific decision about which Controls require
   * re-transform; may be over-ridden by DataRenderer sub-classes;
   * this decision may use some values computed by link.prepareData()
   * @param control Control being judged whether it needs re-transform
   * @param link DataDisplayLink possibly involved in decision
   * @return flag indicating whether re-transform is needed
   */
  public boolean isTransformControl(Control control, DataDisplayLink link) {
    if (display == null) return false;
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

  /**
   * used by ShadowFunctionOrSetType for transform time-out hack
   * @return single DataDisplayLink (over-ridden by sub-classes)
   */
  public DataDisplayLink getLink() {
    return null;
  }

  /**
   * @return flag indicating whether texture mapping is legal
   *         for this DataRenderer
   */
  public boolean isLegalTextureMap() {
    return true;
  }

  /* ********************** */
  /*  flow rendering stuff  */
  /* ********************** */

  // value array (display_values) indices
  //   ((ScalarMap) MapVector.elementAt(valueToMap[index]))
  // can get these indices through shadow_data_out or shadow_data_in


  /** true if lat and lon in data_in & shadow_data_in is allSpatial
      or if lat and lon in data_in & lat_lon_in_by_coord */
  boolean lat_lon_in = false;
  /** true if lat_lon_in and shadow_data_out is allSpatial, i.e.,
      map from lat, lon to display is through data CoordinateSystem */
  boolean lat_lon_in_by_coord = false;
  /** true if lat and lon in data_out & shadow_data_out is allSpatial */
  boolean lat_lon_out = false;
  /** true if lat_lon_out and shadow_data_in is allSpatial, i.e.,
      map from lat, lon to display is inverse via data CoordinateSystem */
  boolean lat_lon_out_by_coord = false;

  /** earth dimension, either 2, 3 or -1 (for none) */
  int lat_lon_dimension = -1;

  /** Shadow of reference RealTupleType in Data */
  ShadowRealTupleType shadow_data_out = null;
  /** reference RealTupleType in Data */
  RealTupleType data_out = null;
  /** Units of reference RealTupleType in Data */
  Unit[] data_units_out = null;
  // CoordinateSystem data_coord_out is always null

  /** Shadow of RealTupleType with reference in Data */
  ShadowRealTupleType shadow_data_in = null;
  /** RealTupleType with reference in Data */
  RealTupleType data_in = null;
  /** Units of RealTupleType with reference in Data */
  Unit[] data_units_in = null;
  /** CoordinateSystems relating data_in to data_out,
      usually just one, but may be one per point */
  CoordinateSystem[] data_coord_in = null;

  /** spatial ScalarMaps for allSpatial shadow_data_out */
  ScalarMap[] sdo_maps = null;
  /** spatial ScalarMaps for allSpatial shadow_data_in */
  ScalarMap[] sdi_maps = null;
  /** indices in DisplayTupleType of DisplayRealTypes in sdo_maps */
  int[] sdo_spatial_index = null;
  /** indices in DisplayTupleType of DisplayRealTypes in sdi_maps */
  int[] sdi_spatial_index = null;

  /** index of RealType.Latitude
      if lat_lon_in then index in data_in
      if lat_lon_out then index in data_out
      if lat_lon_spatial then values index */
  int lat_index = -1;
  /** index of RealType.Longitude
      if lat_lon_in then index in data_in
      if lat_lon_out then index in data_out
      if lat_lon_spatial then values indices */
  int lon_index = -1;
  /** other index if lat & lon in a RealTupleType of length 3,
      otherwise -1 */
  int other_index = -1;
  /** true if other_index Units convertable to meter */
  boolean other_meters = false;

  /** RealVectorTypes (extends RealTupleType) mapped to flow1 and
      flow2, to be tested for being instances of EarthVectorType;
      values will be either data_in, data_out or null */
  RealVectorType[] rvts = {null, null};

  /**
   * @param index 0 or 1 for flow1 and flow2
   * @return RealVectorType (extends RealTupleType) mapped to flow1 or
   *         flow2 (values will be either data_in, data_out or null)
   */
  public RealVectorType getRealVectorTypes(int index) {
    if (index == 0 || index == 1) return rvts[index];
    else return null;
  }

  /**
   * @return indices of RealType.Latitude and RealType.Longitude
   *         in data_in, data_out, or just spatial value array
   */
  public int[] getLatLonIndices() {
    return new int[] {lat_index, lon_index};
  }

  /**
   * @param indices indices of RealType.Latitude and RealType.Longitude
   *                in data_in, data_out, or just spatial value array
   */
  public void setLatLonIndices(int[] indices) {
    lat_index = indices[0];
    lon_index = indices[1];
  }

  /**
   * @return earth dimension, either 2, 3 or -1 (for none)
   */
  public int getEarthDimension() {
    return lat_lon_dimension;
  }

  /**
   * @return Units of earth coordinates used in Data
   */
  public Unit[] getEarthUnits() {
    if (display == null) return null;
    Unit[] units = null;
    if (lat_lon_in) {
      units = data_units_in;
    }
    else if (lat_lon_out) {
      units = data_units_out;
    }
    else if (lat_lon_spatial) {
      units = new Unit[] {RealType.Latitude.getDefaultUnit(),
                          RealType.Longitude.getDefaultUnit()};
    }
    else {
      units = null;
    }
    int lat = lat_index;
    int lon = lon_index;
    int other = other_index;
    if (units == null) {
      return null;
    }
    else if (units.length == 2) {
      return new Unit[] {lat >= 0 ? units[lat] : null,
                         lon >= 0 ? units[lon] : null};
    }
    else if (units.length == 3) {
      return new Unit[] {lat >= 0 ? units[lat] : null,
                         lon >= 0 ? units[lon] : null,
                         other >= 0 ? units[other] : null};
    }
    else {
      return null;
    }
  }

  /**
   * @return maximum of display ranges of latitude and longitude
   */
  public float getLatLonRange() {
    if (display == null) return 1.0f;
    double[] rlat = null;
    double[] rlon = null;
    int lat = lat_index;
    int lon = lon_index;
    if ((lat_lon_out && !lat_lon_out_by_coord) ||
        (lat_lon_in && lat_lon_in_by_coord)) {
      rlat = lat >= 0 ? sdo_maps[lat].getRange() :
                        new double[] {Double.NaN, Double.NaN};
      rlon = lon >= 0 ? sdo_maps[lon].getRange() :
                        new double[] {Double.NaN, Double.NaN};
    }
    else if ((lat_lon_in && !lat_lon_in_by_coord) ||
             (lat_lon_out && lat_lon_out_by_coord)) {
      rlat = lat >= 0 ? sdi_maps[lat].getRange() :
                        new double[] {Double.NaN, Double.NaN};
      rlon = lon >= 0 ? sdi_maps[lon].getRange() :
                        new double[] {Double.NaN, Double.NaN};
    }
    else if (lat_lon_spatial) {
      rlat = lat_map.getRange();
      rlon = lon_map.getRange();
    }
    else {
      return 1.0f;
    }
    double dlat = Math.abs(rlat[1] - rlat[0]);
    double dlon = Math.abs(rlon[1] - rlon[0]);
    if (dlat != dlat) dlat = 1.0f;
    if (dlon != dlon) dlon = 1.0f;
    return (dlat > dlon) ? (float) dlat : (float) dlon;
  }

  /**
   * convert (lat, lon) or (lat, lon, other) values to display (x, y, z)
   * @param locs (lat, lon) or (lat, lon, other) coordinates
   * @param vert vertical flow component (if non-null, used to
   *             adjust non-lat/lon spatial_locs
   * @return display (x, y, z) coordinates
   * @throws VisADException a VisAD error occurred
   */
  public float[][] earthToSpatial(float[][] locs, float[] vert)
         throws VisADException {
    if (display == null) return null;
    return earthToSpatial(locs, vert, null);
  }

  /**
   * convert (lat, lon) or (lat, lon, other) values to display (x, y, z)
   * @param locs (lat, lon) or (lat, lon, other) coordinates
   * @param vert vertical flow component (if non-null, used to
   *        adjust non-lat/lon spatial_locs
   * @param base_spatial_locs saved spatial_locs argument from
   *        spatialToEarth() call used to fill in any null members
   *        of return array
   * @return display (x, y, z) coordinates
   * @throws VisADException a VisAD error occurred
   */
  public float[][] earthToSpatial(float[][] locs, float[] vert,
                                  float[][] base_spatial_locs)
         throws VisADException {
    if (display == null) return null;
    int lat = lat_index;
    int lon = lon_index;
    int other = other_index;
    if (lat_index < 0 || lon_index < 0) return null;

    int size = locs[0].length;
    if (locs.length < lat_lon_dimension) {
      // extend locs to lat_lon_dimension with zero fill
      float[][] temp = locs;
      locs = new float[lat_lon_dimension][];
      for (int i=0; i<locs.length; i++) {
        locs[i] = temp[i];
      }
      float[] zero = new float[size];
      for (int j=0; j<size; j++) zero[j] = 0.0f;
      for (int i=locs.length; i<lat_lon_dimension; i++) {
        locs[i] = zero;
      }
    }
    else if (locs.length > lat_lon_dimension) {
      // truncate locs to lat_lon_dimension
      float[][] temp = locs;
      locs = new float[lat_lon_dimension][];
      for (int i=0; i<lat_lon_dimension; i++) {
        locs[i] = temp[i];
      }
    }

    // permute (lat, lon, other) to data RealTupleType
    float[][] tuple_locs = new float[lat_lon_dimension][];
    float[][] spatial_locs = new float[3][];
    tuple_locs[lat] = locs[0];
    tuple_locs[lon] = locs[1];
    if (lat_lon_dimension == 3) tuple_locs[other] = locs[2];

    int vert_index = -1; // non-lat/lon index for lat_lon_dimension = 2

    if (lat_lon_in) {
      if (lat_lon_in_by_coord) {
        // transform 'RealTupleType data_in' to 'RealTupleType data_out'
        if (data_coord_in.length == 1) {
          // one data_coord_in applies to all data points
          tuple_locs = CoordinateSystem.transformCoordinates(data_out, null,
                           data_units_out, null, data_in, data_coord_in[0],
                           data_units_in, null, tuple_locs);
        }
        else {
          // one data_coord_in per data point
          float[][] temp = new float[lat_lon_dimension][1];
          for (int j=0; j<size; j++) {
            for (int k=0; k<lat_lon_dimension; k++) temp[k][0] = tuple_locs[k][j];
              temp = CoordinateSystem.transformCoordinates(data_out, null,
                             data_units_out, null, data_in, data_coord_in[j],
                             data_units_in, null, temp);
            for (int k=0; k<lat_lon_dimension; k++) tuple_locs[k][j] = temp[k][0];
          }
        }
        // map data_out to spatial DisplayRealTypes
        for (int i=0; i<lat_lon_dimension; i++) {
          spatial_locs[sdo_spatial_index[i]] =
            sdo_maps[i].scaleValues(tuple_locs[i]);
        }
        if (lat_lon_dimension == 2) {
          vert_index = 3 - (sdo_spatial_index[0] + sdo_spatial_index[1]);
        }
      }
      else {
        // map data_in to spatial DisplayRealTypes
        for (int i=0; i<lat_lon_dimension; i++) {
          spatial_locs[sdi_spatial_index[i]] =
            sdi_maps[i].scaleValues(tuple_locs[i]);
        }
        if (lat_lon_dimension == 2) {
          vert_index = 3 - (sdi_spatial_index[0] + sdi_spatial_index[1]);
        }
      }
    }
    else if (lat_lon_out) {
      if (lat_lon_out_by_coord) {
        // transform 'RealTupleType data_out' to 'RealTupleType data_in'
        if (data_coord_in.length == 1) {
          // one data_coord_in applies to all data points
          tuple_locs = CoordinateSystem.transformCoordinates(data_in,
                           data_coord_in[0], data_units_in, null, data_out,
                           null, data_units_out, null, tuple_locs);
        }
        else {
          // one data_coord_in per data point
          float[][] temp = new float[lat_lon_dimension][1];
          for (int j=0; j<size; j++) {
            for (int k=0; k<lat_lon_dimension; k++) temp[k][0] = tuple_locs[k][j];
              temp = CoordinateSystem.transformCoordinates(data_in,
                             data_coord_in[j], data_units_in, null, data_out,
                             null, data_units_out, null, temp);
            for (int k=0; k<lat_lon_dimension; k++) tuple_locs[k][j] = temp[k][0];
          }
        }
        // map data_in to spatial DisplayRealTypes
        for (int i=0; i<lat_lon_dimension; i++) {
          spatial_locs[sdi_spatial_index[i]] =
            sdi_maps[i].scaleValues(tuple_locs[i]);
        }
        if (lat_lon_dimension == 2) {
          vert_index = 3 - (sdi_spatial_index[0] + sdi_spatial_index[1]);
        }
      }
      else {
        // map data_out to spatial DisplayRealTypes
        for (int i=0; i<lat_lon_dimension; i++) {
          spatial_locs[sdo_spatial_index[i]] =
            sdo_maps[i].scaleValues(tuple_locs[i]);
        }
        if (lat_lon_dimension == 2) {
          vert_index = 3 - (sdo_spatial_index[0] + sdo_spatial_index[1]);
        }
      }
    }
    else if (lat_lon_spatial) {
      // map lat & lon, not in allSpatial RealTupleType, to
      // spatial DisplayRealTypes
      spatial_locs[lat_spatial_index] =
        lat_map.scaleValues(tuple_locs[lat]);
      spatial_locs[lon_spatial_index] =
        lon_map.scaleValues(tuple_locs[lon]);
      vert_index = 3 - (lat_spatial_index + lon_spatial_index);
    }
    else {
      // should never happen
      return null;
    }

    // WLH 9 Dec 99
    // fill any empty spatial DisplayRealTypes with default values
    for (int i=0; i<3; i++) {
      if (spatial_locs[i] == null) {
        if (base_spatial_locs != null &&  base_spatial_locs[i] != null) {
          spatial_locs[i] = base_spatial_locs[i]; // copy not necessary
        }
        else {
          spatial_locs[i] = new float[size];
          float def = default_spatial_in[i]; // may be non-Cartesian
          for (int j=0; j<size; j++) spatial_locs[i][j] = def;
        }
      }
    }

    // adjust non-lat/lon spatial_locs by vertical flow component
/* WLH 28 July 99
    if (vert != null && vert_index > -1) {
      for (int j=0; j<size; j++) spatial_locs[vert_index][j] += vert[j];
    }
*/
    if (vert != null && vert_index > -1 && spatial_locs[vert_index] != null) {
      for (int j=0; j<size; j++) spatial_locs[vert_index][j] += vert[j];
    }

    if (display_coordinate_system != null) {
      // transform non-Cartesian spatial DisplayRealTypes to Cartesian
      if (spatial_locs != null && spatial_locs.length > 0 &&
          spatial_locs[0] != null && spatial_locs[0].length > 0) {
        // DRM 14 Apr 2003 - could do transform in place
        //spatial_locs = display_coordinate_system.toReference(spatial_locs);
        spatial_locs = 
          display_coordinate_system.toReference(Set.copyFloats(spatial_locs));
      }
    }
    return spatial_locs;
  }

  /**
   * convert display (x, y, z) to (lat, lon) or (lat, lon, other) values
   * @param spatial_locs display (x, y, z) coordinates
   * @return (lat, lon) or (lat, lon, other) coordinates
   * @throws VisADException a VisAD error occurred
   */
  public float[][] spatialToEarth(float[][] spatial_locs)
         throws VisADException {
    if (display == null) return null;
    float[][] base_spatial_locs = new float[3][];
    return spatialToEarth(spatial_locs, base_spatial_locs);
  }

  /**
   * convert display (x, y, z) to (lat, lon) or (lat, lon, other) values
   * @param spatial_locs display (x, y, z) coordinates
   * @param base_spatial_locs float[3][] array used to return member
   *        arrays of spatial_locs argument
   * @return (lat, lon) or (lat, lon, other) coordinates
   * @throws VisADException a VisAD error occurred
   */
  public float[][] spatialToEarth(float[][] spatial_locs,
                                  float[][] base_spatial_locs)
         throws VisADException {
    if (display == null) return null;
    int lat = lat_index;
    int lon = lon_index;
    int other = other_index;
    if (lat_index < 0 || lon_index < 0) return null;
    if (spatial_locs.length != 3) return null;

    int size = 0;
    for (int i=0; i<3; i++) {
      if (spatial_locs[i] != null && spatial_locs[i].length > size) {
        size = spatial_locs[i].length;
      }
    }
    if (size == 0) return null;

    // fill any empty spatial DisplayRealTypes with default values
    for (int i=0; i<3; i++) {
      if (spatial_locs[i] == null) {
        spatial_locs[i] = new float[size];
        // defaults for Cartesian spatial DisplayRealTypes = 0.0f
        for (int j=0; j<size; j++) spatial_locs[i][j] = 0.0f;
      }
    }
    if (display_coordinate_system != null) {
      // transform Cartesian spatial DisplayRealTypes to non-Cartesian
      // DRM 14 Apr 2003 - could do transform in place
      //spatial_locs = display_coordinate_system.fromReference(spatial_locs);
      spatial_locs = 
          display_coordinate_system.fromReference(Set.copyFloats(spatial_locs));
    }
    base_spatial_locs[0] = spatial_locs[0];
    base_spatial_locs[1] = spatial_locs[1];
    base_spatial_locs[2] = spatial_locs[2];

    float[][] tuple_locs = new float[lat_lon_dimension][];

    if (lat_lon_in) {
      if (lat_lon_in_by_coord) {
        // map spatial DisplayRealTypes to data_out
        for (int i=0; i<lat_lon_dimension; i++) {
          tuple_locs[i] =
            sdo_maps[i].inverseScaleValues(spatial_locs[sdo_spatial_index[i]]);
        }
        // transform 'RealTupleType data_out' to 'RealTupleType data_in'
        if (data_coord_in.length == 1) {
          // one data_coord_in applies to all data points
          tuple_locs = CoordinateSystem.transformCoordinates(data_in,
                           data_coord_in[0], data_units_in, null, data_out,
                           null, data_units_out, null, tuple_locs);
        }
        else {
          // one data_coord_in per data point
          float[][] temp = new float[lat_lon_dimension][1];
          for (int j=0; j<size; j++) {
            for (int k=0; k<lat_lon_dimension; k++) temp[k][0] = tuple_locs[k][j];
              temp = CoordinateSystem.transformCoordinates(data_in,
                             data_coord_in[j], data_units_in, null, data_out,
                             null, data_units_out, null, temp);
            for (int k=0; k<lat_lon_dimension; k++) tuple_locs[k][j] = temp[k][0];
          }
        }
      }
      else {
        // map spatial DisplayRealTypes to data_in
        for (int i=0; i<lat_lon_dimension; i++) {
          tuple_locs[i] =
            sdi_maps[i].inverseScaleValues(spatial_locs[sdi_spatial_index[i]]);
        }
      }
    }
    else if (lat_lon_out) {
      if (lat_lon_out_by_coord) {
        // map spatial DisplayRealTypes to data_in
        for (int i=0; i<lat_lon_dimension; i++) {
          tuple_locs[i] =
            sdi_maps[i].inverseScaleValues(spatial_locs[sdi_spatial_index[i]]);
        }
        // transform 'RealTupleType data_in' to 'RealTupleType data_out'
        if (data_coord_in.length == 1) {
          // one data_coord_in applies to all data points
          tuple_locs = CoordinateSystem.transformCoordinates(data_out, null,
                           data_units_out, null, data_in, data_coord_in[0],
                           data_units_in, null, tuple_locs);
        }
        else {
          // one data_coord_in per data point
          float[][] temp = new float[lat_lon_dimension][1];
          for (int j=0; j<size; j++) {
            for (int k=0; k<lat_lon_dimension; k++) temp[k][0] = tuple_locs[k][j];
              temp = CoordinateSystem.transformCoordinates(data_out, null,
                             data_units_out, null, data_in, data_coord_in[j],
                             data_units_in, null, temp);
            for (int k=0; k<lat_lon_dimension; k++) tuple_locs[k][j] = temp[k][0];
          }
        }
      }
      else {
        // map spatial DisplayRealTypes to data_out
        for (int i=0; i<lat_lon_dimension; i++) {
          tuple_locs[i] =
            sdo_maps[i].inverseScaleValues(spatial_locs[sdo_spatial_index[i]]);
        }
      }
    }
    else if (lat_lon_spatial) {
      // map spatial DisplayRealTypes to lat & lon, not in
      // allSpatial RealTupleType
      tuple_locs[lat] =
        lat_map.inverseScaleValues(spatial_locs[lat_spatial_index]);
      tuple_locs[lon] =
        lon_map.inverseScaleValues(spatial_locs[lon_spatial_index]);
    }
    else {
      // should never happen
      return null;
    }

    // permute data RealTupleType to (lat, lon, other)
    float[][] locs = new float[lat_lon_dimension][];
    locs[0] = tuple_locs[lat];
    locs[1] = tuple_locs[lon];
    if (lat_lon_dimension == 3) locs[2] = tuple_locs[other];

    return locs;
  }

  /**
   * save information about relation between earth and display
   * spatial coordinates, IF the arguments do define the relation
   * @param s_d_i candidate shadow_data_in
   *              (Shadow of RealTupleType with reference in Data)
   * @param s_d_o candidate shadow_data_out
   *              (Shadow of reference RealTupleType in Data)
   * @param d_o candidate data_out
   *            (reference RealTupleType in Data)
   * @param d_u_o candidate data_units_out
   *              (Units of reference RealTupleType in Data)
   * @param d_i candidate data_in
   *            (RealTupleType with reference in Data)
   * @param d_c_i candidate data_coord_in
   *              (CoordinateSystems relating data_in to data_out)
   * @param d_u_i candidate data_units_in
   *              (Units of RealTupleType with reference in Data)
   * @throws VisADException a VisAD error occurred
   */
  public void setEarthSpatialData(ShadowRealTupleType s_d_i,
                    ShadowRealTupleType s_d_o, RealTupleType d_o,
                    Unit[] d_u_o, RealTupleType d_i,
                    CoordinateSystem[] d_c_i, Unit[] d_u_i)
         throws VisADException {
    if (display == null) return;

    // first check for VectorRealType components mapped to flow
    // TO_DO:  check here for flow mapped via CoordinateSystem
    if (d_o != null && d_o instanceof RealVectorType) {
      ScalarMap[] maps = new ScalarMap[3];
      int k = getFlowMaps(s_d_o, maps);
      if (k > -1) rvts[k] = (RealVectorType) d_o;
    }
    if (d_i != null && d_i instanceof RealVectorType) {
      ScalarMap[] maps = new ScalarMap[3];
      int k = getFlowMaps(s_d_i, maps);
      if (k > -1) rvts[k] = (RealVectorType) d_i;
    }

    int lat_index_local = -1;
    int lon_index_local = -1;
    int other_index_local = -1;
    int n = 0;
    int m = 0;
    if (d_i != null) {
      n = d_i.getDimension();
      for (int i=0; i<n; i++) {
        RealType real = (RealType) d_i.getComponent(i);
        if (RealType.Latitude.equals(real)) lat_index_local = i;
        if (RealType.Longitude.equals(real)) lon_index_local = i;
      }
    }
    if (lat_index_local > -1 && lon_index_local > -1 && (n == 2 || n == 3)) {
      if (s_d_i != null && s_d_i.getAllSpatial() &&
          !s_d_i.getSpatialReference()) {
        lat_lon_in_by_coord = false;
        sdi_spatial_index = new int[s_d_i.getDimension()];
        sdi_maps = getSpatialMaps(s_d_i, sdi_spatial_index);
        if (sdi_maps == null) {
          throw new DisplayException("sdi_maps null A");
        }
      }
      else if (s_d_o != null && s_d_o.getAllSpatial() &&
               !s_d_o.getSpatialReference()) {
        lat_lon_in_by_coord = true;
        sdo_spatial_index = new int[s_d_o.getDimension()];
        sdo_maps = getSpatialMaps(s_d_o, sdo_spatial_index);
        if (sdo_maps == null) {
          throw new DisplayException("sdo_maps null A");
        }
      }
      else {
        // do not update lat_index & lon_index
        return;
      }
      lat_lon_in = true;
      lat_lon_out = false;
      lat_lon_out_by_coord = false;
      lat_lon_spatial = false;
      lat_lon_dimension = n;
      if (n == 3) {
        other_index_local = 3 - (lat_index_local + lon_index_local);
        if (Unit.canConvert(d_u_i[other_index_local], CommonUnit.meter)) {
          other_meters = true;
        }
      }
    }
    else { // if( !(lat & lon in di, di dimension = 2 or 3) )
      lat_index_local = -1;
      lon_index_local = -1;
      other_index_local = -1;
      if (d_o != null) {
        m = d_o.getDimension();
        for (int i=0; i<m; i++) {
          RealType real = (RealType) d_o.getComponent(i);
          if (RealType.Latitude.equals(real)) lat_index_local = i;
          if (RealType.Longitude.equals(real)) lon_index_local = i;
        }
      }
      if (lat_index_local < 0 || lon_index_local < 0 || !(m == 2 || m == 3)) {
        // do not update lat_index & lon_index
        return;
      }
      if (s_d_o != null && s_d_o.getAllSpatial() &&
          !s_d_o.getSpatialReference()) {
        lat_lon_out_by_coord = false;
        sdo_spatial_index = new int[s_d_o.getDimension()];
        sdo_maps = getSpatialMaps(s_d_o, sdo_spatial_index);
        if (sdo_maps == null) {
          throw new DisplayException("sdo_maps null B");
        }
      }
      else if (s_d_i != null && s_d_i.getAllSpatial() &&
               !s_d_i.getSpatialReference()) {
        lat_lon_out_by_coord = true;
        sdi_spatial_index = new int[s_d_i.getDimension()];
        sdi_maps = getSpatialMaps(s_d_i, sdi_spatial_index);
        if (sdi_maps == null) {
          throw new DisplayException("sdi_maps null B");
        }
      }
      else {
        // do not update lat_index & lon_index
        return;
      }

      lat_lon_out = true;
      lat_lon_in = false;
      lat_lon_in_by_coord = false;
      lat_lon_spatial = false;
      lat_lon_dimension = m;
      if (m == 3) {
        other_index_local = 3 - (lat_index_local + lon_index_local);
        if (Unit.canConvert(d_u_i[other_index_local], CommonUnit.meter)) {
          other_meters = true;
        }
      }
    }
    shadow_data_out = s_d_o;
    data_out = d_o;
    data_units_out = d_u_o;
    shadow_data_in = s_d_i;
    data_in = d_i;
    data_units_in = d_u_i;
    data_coord_in = d_c_i; // may be one per point
    lat_index = lat_index_local;
    lon_index = lon_index_local;
    other_index = other_index_local;
    return;
  }

  /**
   * get information about spatial ScalarMaps of components in srt
   * @param srt tuple of ShadowRealTypes
   * @param spatial_index array with length equal to number of
   *        components in srt, used to return indices in
   *        DisplayTupleTypes of DisplayRealType mapped from
   *        RealTypes of corresponding components in srt
   * @return array of spatial ScalarMaps for components in srt (or null)
   */
  private ScalarMap[] getSpatialMaps(ShadowRealTupleType srt,
                                     int[] spatial_index) {
    if (display == null) return null;
    int n = srt.getDimension();
    ScalarMap[] maps = new ScalarMap[n];
    for (int i=0; i<n; i++) {
      ShadowRealType real = (ShadowRealType) srt.getComponent(i);
      Enumeration ms = real.getSelectedMapVector().elements();
      while (ms.hasMoreElements()) {
        ScalarMap map = (ScalarMap) ms.nextElement();
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType tuple = dreal.getTuple();
        if (tuple != null &&
            (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
             (tuple.getCoordinateSystem() != null &&
              tuple.getCoordinateSystem().getReference().equals(
                  Display.DisplaySpatialCartesianTuple)))) {
          maps[i] = map;
          spatial_index[i] = dreal.getTupleIndex();
          break;
        }
      }
      if (maps[i] == null) {
        return null;
      }
    }
    return maps;
  }

  /**
   * determine whether Flow1 or Flow2 is used by given ShadowRealTupleType
   * @param srt tuple of ShadowRealTypes
   * @param maps array with length equal to number of components in srt,
   *             used to return ScalarMaps to Flow from RealTypes of
   *             corresponding components in srt
   * @return 0 for Flow1, 1 for Flow2, or -1 for neither (or both - error)
   */
  private int getFlowMaps(ShadowRealTupleType srt, ScalarMap[] maps) {
    if (display == null) return -1;
    int n = srt.getDimension();
    maps[0] = null;
    maps[1] = null;
    maps[2] = null;
    DisplayTupleType ftuple = null;
    for (int i=0; i<n; i++) {
      ShadowRealType real = (ShadowRealType) srt.getComponent(i);
      Enumeration ms = real.getSelectedMapVector().elements();
      while (ms.hasMoreElements()) {
        ScalarMap map = (ScalarMap) ms.nextElement();
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType tuple = dreal.getTuple();
        if (Display.DisplayFlow1Tuple.equals(tuple) ||
            Display.DisplayFlow2Tuple.equals(tuple)) {
          if (ftuple != null && !ftuple.equals(tuple)) return -1;
          ftuple = tuple;
          maps[i] = map;
          break;
        }
      }
      if (maps[i] == null) return -1;
    }
    return Display.DisplayFlow1Tuple.equals(ftuple) ? 0 : 1;
  }


  /** if non-null, float[][] new_spatial_values =
      display_coordinate_system.toReference(spatial_values); */
  CoordinateSystem display_coordinate_system = null;

  /** spatial DisplayTupleType; set whether
      display_coordinate_system is null or not */
  DisplayTupleType spatial_tuple = null;

  /** map from spatial_tuple tuple_index to value array indices;
      set whether display_coordinate_system is null or not */
  int[] spatial_value_indices = {-1, -1, -1};

  /** default values for spatial DisplayRealTypes */
  float[] default_spatial_in = {0.0f, 0.0f, 0.0f};

  /** true if lat and lon mapped directly to spatial */
  boolean lat_lon_spatial = false;

  /** ScalarMap from RealType.Latitude */
  ScalarMap lat_map = null;

  /** ScalarMap from RealType.Longitude */
  ScalarMap lon_map = null;

  /** index of lat_map DisplayRealType in DisplayTupleType */
  int lat_spatial_index = -1;

  /** index of lon_map DisplayRealType in DisplayTupleType */
  int lon_spatial_index = -1;

  /** array of normalized (i.e., max = 1.0) ranges for spatial
      ScalarMaps, for flow adjustment */
  double[] ranges = null;

  /**
   * @return array of normalized (i.e., max = 1.0) ranges for spatial 
   *         ScalarMaps, for flow adjustment
   */
  public double[] getRanges() {
    return ranges;
  }

  /**
   * @return CoordinateSystem for spatial DisplayTupleType
   *         (null if DisplaySpatialCartesianTuple)
   */
  public CoordinateSystem getDisplayCoordinateSystem() {
    return display_coordinate_system ;
  }

  /**
   * save information from ShadowType.assembleSpatial() about
   * relation between earth and display spatial coordinates
   * @param coord CoordinateSystem for spatial DisplayTupleType
   *              (null if DisplaySpatialCartesianTuple)
   * @param t spatial DisplayTupleType
   * @param display the DisplayImpl
   * @param indices indices in display_values array for 3 spatial
   *                coordinates
   * @param default_values default values for 3 spatial coordinates
   * @param r double[3] array of normalized (i.e., max = 1.0)
   *          ranges for spatial ScalarMaps, for flow adjustment
   * @throws VisADException a VisAD error occurred
   */
  public void setEarthSpatialDisplay(CoordinateSystem coord,
           DisplayTupleType t, DisplayImpl display, int[] indices,
           float[] default_values, double[] r)
         throws VisADException {
    if (display == null) return;
    display_coordinate_system = coord;
    spatial_tuple = t;
    System.arraycopy(indices, 0, spatial_value_indices, 0, 3);
    ranges = r;
    for (int i=0; i<3; i++) {
      int default_index = display.getDisplayScalarIndex(
              ((DisplayRealType) t.getComponent(i)) );
      default_spatial_in[i] = default_values[default_index];
    }

    if (lat_index > -1 && lon_index > -1) return;

    lat_index = -1;
    lon_index = -1;
    other_index = -1;

    int valueArrayLength = display.getValueArrayLength();
    int[] valueToScalar = display.getValueToScalar();
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

    for (int i=0; i<valueArrayLength; i++) {
      ScalarMap map = (ScalarMap) MapVector.elementAt(valueToMap[i]);
      ScalarType real = map.getScalar();
      DisplayRealType dreal = map.getDisplayScalar();
      DisplayTupleType tuple = dreal.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
                Display.DisplaySpatialCartesianTuple)))) {
        if (RealType.Latitude.equals(real)) {
          lat_index = 0;
          lat_map = map;
          lat_spatial_index = dreal.getTupleIndex();
        }
        if (RealType.Longitude.equals(real)) {
          lon_index = 1;
          lon_map = map;
          lon_spatial_index = dreal.getTupleIndex();
        }
      }
    }
    if (lat_index > -1 && lon_index > -1) {
      lat_lon_spatial = true;
      lat_lon_dimension = 2;
      lat_lon_out = false;
      lat_lon_in_by_coord = false;
      lat_lon_in = false;
    }
    else {
      lat_lon_spatial = false;
      lat_index = -1;
      lon_index = -1;
    }
  }



  /* *************************** */
  /*  direct manipulation stuff  */
  /* *************************** */


  private float[][] spatialValues = null;

  /** if Function, last domain index and range values */
  private int lastIndex = -1;
  private double[] lastD = null;
  private float[] lastX = new float[6];

  /** index into spatialValues found by checkClose */
  private int closeIndex = -1;

  /** pick error offset, communicated from checkClose() to drag_direct() */
  private float offsetx = 0.0f, offsety = 0.0f, offsetz = 0.0f;
  /** count down to decay offset to 0.0 */
  private int offset_count = 0;
  /** initial offset_count */
  private static final int OFFSET_COUNT_INIT = 30;

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
  private float[] d = new float[1];
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
  /** spatial DisplayTupleType other than
      DisplaySpatialCartesianTuple */
  private DisplayTupleType tuple = null;

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
  private final static String nonSpatial =
    "no spatial mapping";
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
  private final static String tooFewSpatial =
    "Function without spatial domain";
  private final static String functionTooFew =
    "Function directManifoldDimension < 2";
  private final static String badCoordSysManifoldDim =
    "bad directManifoldDimension with spatial CoordinateSystem";
  private final static String lostConnection =
    "lost connection to Data server";

  private boolean stop = false;

  private int LastMouseModifiers = 0;

  /**
   * determine if direct manipulation is feasible for the Data
   * objects rendered by this, and for the ScalarMaps linked to
   * the associated DisplayImpl;
   * "returns" its result by calls to setIsDirectManipulation()
   * called by checkDirect() method of DirectManipulationRendererJ2D
   * and DirectManipulationRendererJ3D, basically just to share
   * code between those two classes
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public synchronized void realCheckDirect()
         throws VisADException, RemoteException {
    if (display == null) return;
    setIsDirectManipulation(false);

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      link = null;
      return;
    }
    link = Links[0];

    ref = link.getDataReference();
    shadow = link.getShadow().getAdaptedShadowType();
    type = link.getType();
    tuple = null;

    if (type instanceof FunctionType) {
      ShadowRealTupleType domain =
        ((ShadowFunctionType) shadow).getDomain();
      ShadowType range =
        ((ShadowFunctionType) shadow).getRange();
      tuple = domain.getDisplaySpatialTuple();
      // there is some redundancy among these conditions
      if (!((FunctionType) type).getReal()) {
        whyNotDirect = notRealFunction;
        return;
      }
      else if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_FIELD) {
        whyNotDirect = notSimpleField;
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
      else if(!(Display.DisplaySpatialCartesianTuple.equals(tuple) ||
                (tuple != null &&
                 tuple.getCoordinateSystem().getReference().equals(
                 Display.DisplaySpatialCartesianTuple)) )) {
        whyNotDirect = domainNotSpatial;
        return;
      }
      else if (domain.getSpatialReference()) {
        whyNotDirect = viaReference;
        return;
      }
      DisplayTupleType rtuple = null;
      if (range instanceof ShadowRealTupleType) {
        rtuple = ((ShadowRealTupleType) range).getDisplaySpatialTuple();
      }
      else if (range instanceof ShadowRealType) {
        rtuple = ((ShadowRealType) range).getDisplaySpatialTuple();
      }
      else {
        whyNotDirect = rangeType;
        return;
      }
      if (!tuple.equals(rtuple)) {
        whyNotDirect = rangeNotSpatial;
        return;
      }
      else if (range instanceof ShadowRealTupleType &&
               ((ShadowRealTupleType) range).getSpatialReference()) {
        whyNotDirect = viaReference;
        return;
      }
      else {
        Data data;
        try {
          data = link.getData();
        } catch (RemoteException re) {
          if (visad.collab.CollabUtil.isDisconnectException(re)) {
            getDisplay().connectionFailed(this, link);
            removeLink(link);
            link = null;
            whyNotDirect = lostConnection;
            return;
          }
          throw re;
        }

        if (!(data instanceof Field) ||
            !(((Field) data).getDomainSet() instanceof Gridded1DSet))
        {
          whyNotDirect = domainSet;
          return;
        }
      }
      if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
        tuple = null;
      }

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
        whyNotDirect = tooFewSpatial;
        return;
      }
      if (directManifoldDimension < 2) {
        whyNotDirect = functionTooFew;
        return;
      }
      boolean twod = displayRenderer.getMode2D();
      if (tuple != null &&
          (!twod && directManifoldDimension != 3 ||
           twod && directManifoldDimension != 2) ) {
        whyNotDirect = badCoordSysManifoldDim;
        return;
      }
      setIsDirectManipulation(true);
    }
    else if (type instanceof RealTupleType) {
      //
      // TO_DO
      // allow for any Flat ShadowTupleType
      //
      tuple = ((ShadowRealTupleType) shadow).getDisplaySpatialTuple();
      if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_TUPLE) {
        whyNotDirect = notSimpleTuple;
        return;
      }
      else if (shadow.getMultipleSpatialDisplayScalar()) {
        whyNotDirect = multipleSpatialMapping;
        return;
      }
      else if(!(Display.DisplaySpatialCartesianTuple.equals(tuple) ||
                (tuple != null &&
                 tuple.getCoordinateSystem().getReference().equals(
                 Display.DisplaySpatialCartesianTuple)) )) {
        whyNotDirect = nonSpatial;
        return;
      }
      else if (((ShadowRealTupleType) shadow).getSpatialReference()) {
        whyNotDirect = viaReference;
        return;
      }
      if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
        tuple = null;
      }

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
      boolean twod = displayRenderer.getMode2D();
      if (tuple != null &&
          (!twod && directManifoldDimension != 3 ||
           twod && directManifoldDimension != 2) ) {
        whyNotDirect = badCoordSysManifoldDim;
        return;
      }
      setIsDirectManipulation(true);
    }
    else if (type instanceof RealType) {
      tuple = ((ShadowRealType) shadow).getDisplaySpatialTuple();
      if (shadow.getLevelOfDifficulty() != ShadowType.SIMPLE_TUPLE) {
        whyNotDirect = notSimpleTuple;
        return;
      }
      else if (shadow.getMultipleSpatialDisplayScalar()) {
        whyNotDirect = multipleSpatialMapping;
        return;
      }
      else if(!(Display.DisplaySpatialCartesianTuple.equals(tuple) ||
                (tuple != null &&
                 tuple.getCoordinateSystem().getReference().equals(
                 Display.DisplaySpatialCartesianTuple)) )) {
        whyNotDirect = nonSpatial;
        return;
      }
      if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
        tuple = null;
      }

      domainAxis = -1;
      for (int i=0; i<3; i++) {
        axisToComponent[i] = -1;
        directMap[i] = null;
      }
      directManifoldDimension =
        setDirectMap((ShadowRealType) shadow, 0, false);
      boolean twod = displayRenderer.getMode2D();
      if (tuple != null &&
          (!twod && directManifoldDimension != 3 ||
           twod && directManifoldDimension != 2) ) {
        whyNotDirect = badCoordSysManifoldDim;
        return;
      }
      setIsDirectManipulation(true);
    } // end else if (type instanceof RealType)
  }

  /**
   * set directMap and axisToComponent (domain = false) or domainAxis
   * (domain = true) from real; called by realCheckDirect()
   * @param real shadow of RealType in a ScalarMap
   * @param component index of real in a ShadowRealTupleType
   *                  -1 if real is a domain, 0 if range not in tuple
   * @param domain true if real occurs in a Function domain
   * @return direct manifold dimension (i.e., degrees of freedom
   *         of manipulation)
   */
  synchronized int setDirectMap(ShadowRealType real, int component,
                                boolean domain) {
    if (display == null) return 0;
    Enumeration maps = real.getSelectedMapVector().elements();
    while (maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      DisplayRealType dreal = map.getDisplayScalar();
      DisplayTupleType tuple = dreal.getTuple();
      if (Display.DisplaySpatialCartesianTuple.equals(tuple) ||
          (tuple != null && tuple.getCoordinateSystem() != null &&
           Display.DisplaySpatialCartesianTuple.equals(
             tuple.getCoordinateSystem().getReference() )) ) {
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

  /**
   * @return direct manifold dimension (i.e., degrees of freedom
   *         of manipulation)
   */
  private int getDirectManifoldDimension() {
    return directManifoldDimension;
  }

  /**
   * @return String with reason MathType and ScalarMaps do not
   *         qualify for direct manipulation
   */
  public String getWhyNotDirect() {
    return whyNotDirect;
  }

  /**
   * @param index of a spatial axis
   * @return index of tuple component for spatial axis i
   */
  private int getAxisToComponent(int i) {
    return axisToComponent[i];
  }

  /**
   * @param index of a spatial axis
   * @return ScalarMap for spatial axis i
   */
  private ScalarMap getDirectMap(int i) {
    return directMap[i];
  }

  /**
   * @return spatial axis for Function domain
   */
  private int getDomainAxis() {
    return domainAxis;
  }

  /**
   * set spatial values for Data depiction; used to detect when
   * direct manipulation mouse selects a point of a Data depiction
   * @param spatial_values float[3][number_of_points] of 3-D locations
   *                       of depiction points
   */
  public synchronized void setSpatialValues(float[][] spatial_values) {
    // these are X, Y, Z values
    spatialValues = spatial_values;
  }

  /**
   * find minimum distance from ray to spatialValues; save index of
   * point with minimum distance in closeIndex; reset lastIndex to -1
   * (Field domain index of Field range value last modified by
   *  drag_direct())
   * @param origin 3-D origin of ray
   * @param direction 3-D direction of ray
   * @return minimum distance of ray to any point in spatialValues
   *         (spatial values for Data depiction)
   */
  public synchronized float checkClose(double[] origin, double[] direction) {
    float distance = Float.MAX_VALUE;
    if (display == null) return distance;
    lastIndex = -1;
    if (spatialValues == null) return distance;
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
        offsetx = x;
        offsety = y;
        offsetz = z;
      }
/*
System.out.println("spatialValues["+i+"] = " + spatialValues[0][i] + " " +
spatialValues[1][i] + " " + spatialValues[2][i] + " d = " + d);
*/
    }
/*
System.out.println("checkClose: distance = " + distance);
*/
    return distance;
  }

  /**
   * called when mouse button is released ending direct manipulation;
   * intended to be over-ridden by DataRenderer extensions that need
   * to act on this event
   */
  public synchronized void release_direct() {
  }

  /**
   * discontinue manipulating Data values for current mouse drag;
   * (this only applies to the current mouse drag and is not a
   *  general disable)
   */
  public void stop_direct() {
    stop = true;
  }

  /**
   * @return value of InputEvent.getModifiers() from most recent
   *         direct manipulation mouse click
   */
  public int getLastMouseModifiers() {
    return LastMouseModifiers;
  }

  /**
   * called by MouseHelper.processEvent() to set LastMouseModifiers
   * @param mouseModifiers value of InputEvent.getModifiers() from
   *                       last direct manipulation mouse click
   */
  public void setLastMouseModifiers(int mouseModifiers) {
    LastMouseModifiers = mouseModifiers;
  }

  /**
   * modify Data values based on direct manipulation mouse actions
   * @param ray 3-D graphics coordinates of ray corresponding to
   *            mouse screen location
   * @param first flag if this is first call (for MouseEvent.MOUSE_PRESSED,
   *              not for MouseEvent.MOUSE_DRAGGED)
   * @param mouseModifiers value of InputEvent.getModifiers() from
   *                       most recent direct manipulation mouse click
   */
  public synchronized void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    if (display == null) return;
    // System.out.println("drag_direct " + first + " " + type);
    if (spatialValues == null || ref == null || shadow == null ||
        link == null) return;

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
          if (displayRenderer.getMode2D()) {
            // coord sys ok
            lineAxis = 2;
          }
          else {
            for (int i=0; i<3; i++) {
              if (getAxisToComponent(i) < 0 && getDomainAxis() != i) {
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

    //
    //jeffmc: new call so a derived class can constrain the position of the drag point
    //
    constrainDragPoint(x);

    //
    // TO_DO
    // might estimate errors from pixel resolution on screen
    //

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
      Data data;
      try {
        data = link.getData();
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          link = null;
          return;
        }
        throw re;
      }


      if (type instanceof RealType) {
        addPoint(xx);
        for (int i=0; i<3; i++) {
          if (getAxisToComponent(i) >= 0) {
            f[0] = x[i];
            d = getDirectMap(i).inverseScaleValues(f);
            // RealType rtype = (RealType) data.getType();
            RealType rtype = (RealType) type;
            newData = new Real(rtype, (double) d[0], rtype.getDefaultUnit(), null);

            // create location string
            Vector vect = new Vector();
            Real r = new Real(rtype, d[0]);
            Unit overrideUnit = getDirectMap(i).getOverrideUnit();
            Unit rtunit = rtype.getDefaultUnit();
            // units not part of Time string
            if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
                (!Unit.canConvert(rtunit, CommonUnit.secondsSinceTheEpoch) ||
                 rtunit.getAbsoluteUnit().equals(rtunit))) {
              double dval =  overrideUnit.toThis((double) d[0], rtunit);
              r = new Real(rtype, dval, overrideUnit);
            }
            String valueString = r.toValueString();
            vect.addElement(rtype.getName() + " = " + valueString);

            getDisplayRenderer().setCursorStringVector(vect);
            break;
          }
        }
        ref.setData(newData);
        link.clearData();
      }
      else if (type instanceof RealTupleType) {

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
            Real r = new Real(rtype, d[0]);
            Unit overrideUnit = getDirectMap(i).getOverrideUnit();
            Unit rtunit = rtype.getDefaultUnit();
            // units not part of Time string
            if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
                (!Unit.canConvert(rtunit, CommonUnit.secondsSinceTheEpoch) ||
                 rtunit.getAbsoluteUnit().equals(rtunit))) {
              double dval = overrideUnit.toThis((double) d[0], rtunit);
              r = new Real(rtype, dval, overrideUnit);
            }
            String valueString = r.toValueString();
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
      }
      else if (type instanceof FunctionType) {
        Vector vect = new Vector();
        if (first) lastIndex = -1;
        int k = getDomainAxis();
        f[0] = x[k];
        d = getDirectMap(k).inverseScaleValues(f);
        RealType rtype = (RealType) getDirectMap(k).getScalar();

        // first, save value in default Unit
        double dsave = d[0];

        // WLH 4 Jan 99
        // convert d from default Unit to actual domain Unit of data
        Unit[] us = ((Field) data).getDomainUnits();
        if (us != null && us[0] != null) {
          d[0] = (float) us[0].toThis((double) d[0], rtype.getDefaultUnit());
        }

        // create location string
        Real r = new Real(rtype, dsave);
        Unit overrideUnit = getDirectMap(k).getOverrideUnit();
        Unit rtunit = rtype.getDefaultUnit();
        // units not part of Time string
        if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
            (!Unit.canConvert(rtunit, CommonUnit.secondsSinceTheEpoch) ||
             rtunit.getAbsoluteUnit().equals(rtunit))) {
          dsave = overrideUnit.toThis(dsave, rtunit);
          r = new Real(rtype, dsave, overrideUnit);
        }
        String valueString = r.toValueString();
        vect.addElement(rtype.getName() + " = " + valueString);

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
          addPoint(xx);
        }
        else {
          lastX[3] = xx[0];
          lastX[4] = xx[1];
          lastX[5] = xx[2];
          addPoint(lastX);
        }
        lastX[0] = xx[0];
        lastX[1] = xx[1];
        lastX[2] = xx[2];

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
            rtype = (RealType) getDirectMap(i).getScalar();

            r = new Real(rtype, d[0]);
            overrideUnit = getDirectMap(i).getOverrideUnit();
            rtunit = rtype.getDefaultUnit();
            // units not part of Time string
            if (overrideUnit != null && !overrideUnit.equals(rtunit) &&
                (!Unit.canConvert(rtunit, CommonUnit.secondsSinceTheEpoch) ||
                 rtunit.getAbsoluteUnit().equals(rtunit))) {
              double dval = overrideUnit.toThis((double) d[0], rtunit);
              r = new Real(rtype, dval, overrideUnit);
            }
            valueString = r.toValueString();
            vect.addElement(rtype.getName() + " = " + valueString);

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
          link.clearData(); // WLH 27 July 99
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

  /**
   * jeffmc: new method that provides a hook so derived classes can easily constrain the position of a drag point
   *
   * @param dragPoint The position of the drag point
   */
  public void constrainDragPoint(float[]dragPoint) {}


  /**
   * add point for temporary rendering; intended to be
   * over-ridden by graphics-API-specific extensions of
   * DataRenderer
   * @param x 3-D graphics coordinates of point to render
   * @throws VisADException a VisAD error occurred
   */
  public void addPoint(float[] x) throws VisADException {
  }

  /** flag indicating whether DirectManipulationRenderer is valid
      for this ShadowType */
  private boolean isDirectManipulation;

  /**
   * set isDirectManipulation = true if this DataRenderer supports
   * direct manipulation for the MathType of its linked Data, and
   * for its ScalarMaps; intended to be over-ridden by extensions of
   * DataRenderer
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void checkDirect() throws VisADException, RemoteException {
    isDirectManipulation = false;
  }

  /**
   * set value of isDirectManipulation flag (indicating whether this
   * DataRenderer supports direct manipulation for its MathType and
   * ScalarMaps)
   * @param b value to set in isDirectManipulation
   */
  public void setIsDirectManipulation(boolean b) {
    isDirectManipulation = b;
  }

  /**
   * @return value of isDirectManipulation flag (indicating whether this
   *         DataRenderer supports direct manipulation for its MathType
   *         and ScalarMaps)
   */
  public boolean getIsDirectManipulation() {
    return isDirectManipulation;
  }

  /** flag indicating whether points affected by direct manipulation should
      "crawl" toward the cursor instead of jumping to it immediately. */
  protected boolean pickCrawlToCursor = true;

  /**
   * set pickCrawlToCursor flag indicating whether Data points being
   * manipulated should "crawl" toward the cursor instead of jumping
   * to it immediately
   * @param b value to set in pickCrawlToCursor
   */
  public void setPickCrawlToCursor(boolean b) {
    pickCrawlToCursor = b;
  }

  /**
   * @return pickCrawlToCursor flag indicating whether Data points being
   *         manipulated should "crawl" toward the cursor instead of
   *         jumping to it immediately
   */
  public boolean getPickCrawlToCursor() {
    return pickCrawlToCursor;
  }

  private float ray_pos; // save last ray_pos as first guess for next
  private static final int HALF_GUESSES = 200;
  private static final int GUESSES = 2 * HALF_GUESSES + 1;
  private static final float RAY_POS_INC = 0.1f;
  private static final int TRYS = 10;
  private static final double EPS = 0.001f;

  /**
   * find intersection of a ray and a 2-D manifold, using Newton's method
   * @param first flag requesting to generate a first guess ray position
   *              by brute force search
   * @param origin 3-D graphics coordinates of ray origin
   * @param direction 3-D graphics coordinates of ray direction
   * @param tuple spatial DisplayTupleType used to define 2-D manifold
   *              (manifold is defined by fixing value of one component
   *               of 3-D tuple)
   * @param otherindex index of tuple component to be fixed
   * @param othervalue value at which to fix otherindex tuple component
   * @return parameter of point along ray of ray-manifold intersection
   *        (point = origin + parameter * direction)
   * @throws VisADException a VisAD error occurred
   */
  public float findRayManifoldIntersection(boolean first, double[] origin,
                             double[] direction, DisplayTupleType tuple,
                             int otherindex, float othervalue)
          throws VisADException {
    ray_pos = Float.NaN;
    if (display == null) return ray_pos;
    if (otherindex < 0) return ray_pos;
    CoordinateSystem tuplecs = null;
    if (tuple != null) tuplecs = tuple.getCoordinateSystem();
    if (tuple == null || tuplecs == null) {
      ray_pos = (float)
        ((othervalue - origin[otherindex]) / direction[otherindex]);
    }
    else { // tuple != null
      double adjust = Double.NaN;
      if (Display.DisplaySpatialSphericalTuple.equals(tuple)) {
        if (otherindex == 1) adjust = 360.0;
      }
      if (first) {
        // generate a first guess ray_pos by brute force
        ray_pos = Float.NaN;
        float[][] guesses = new float[3][GUESSES];
        for (int i=0; i<GUESSES; i++) {
          float rp = (i - HALF_GUESSES) * RAY_POS_INC;
          guesses[0][i] = (float) (origin[0] + rp * direction[0]);
          guesses[1][i] = (float) (origin[1] + rp * direction[1]);
          guesses[2][i] = (float) (origin[2] + rp * direction[2]);
          if (adjust == adjust) {
            guesses[otherindex][i] = (float)
              (((othervalue + 0.5 * adjust + guesses[otherindex][i]) % adjust) -
               (othervalue + 0.5 * adjust));
          }
        }
        guesses = tuplecs.fromReference(guesses);
        double distance = Double.MAX_VALUE;
        float lastg = 0.0f;
        for (int i=0; i<GUESSES; i++) {
          float g = othervalue - guesses[otherindex][i];
          // first, look for nearest zero crossing and interpolate
          if (i > 0 && ((g < 0.0f && lastg >= 0.0f) || (g >= 0.0f && lastg < 0.0f))) {
            float r = (float)
              (i - (Math.abs(g) / (Math.abs(lastg) + Math.abs(g))));
            ray_pos = (r - HALF_GUESSES) * RAY_POS_INC;
            break;
          }
          lastg = g;

          // otherwise look for closest to zero
          double d = Math.abs(othervalue - guesses[otherindex][i]);
          if (d < distance) {
            distance = d;
            ray_pos = (i - HALF_GUESSES) * RAY_POS_INC;
          }
        } // end for (int i=0; i<GUESSES; i++)
      }
      if (ray_pos == ray_pos) {
        // use Newton's method to refine first guess
        // double error_limit = 10.0 * EPS;
        double error_limit = EPS;
        double r = ray_pos;
        double error = 1.0f;
        double[][] guesses = new double[3][3];
        int itry;
// System.out.println("\nothervalue = " + (float) othervalue + " r = " + (float) r);
        for (itry=0; (itry<TRYS && r == r); itry++) {
          double rp = r + EPS;
          double rm = r - EPS;
          guesses[0][0] = origin[0] + rp * direction[0];
          guesses[1][0] = origin[1] + rp * direction[1];
          guesses[2][0] = origin[2] + rp * direction[2];
          guesses[0][1] = origin[0] + r * direction[0];
          guesses[1][1] = origin[1] + r * direction[1];
          guesses[2][1] = origin[2] + r * direction[2];
          guesses[0][2] = origin[0] + rm * direction[0];
          guesses[1][2] = origin[1] + rm * direction[1];
          guesses[2][2] = origin[2] + rm * direction[2];
// System.out.println(" guesses = " + (float) guesses[0][1] + " " +
//                    (float) guesses[1][1] + " " + (float) guesses[2][1]);
          guesses = tuplecs.fromReference(guesses);
// System.out.println(" transformed = " + (float) guesses[0][1] + " " +
//                    (float) guesses[1][1] + " " + (float) guesses[2][1]);
          if (adjust == adjust) {
            guesses[otherindex][0] =
              ((othervalue + 0.5 * adjust + guesses[otherindex][0]) % adjust) -
               (othervalue + 0.5 * adjust);
            guesses[otherindex][1] =
              ((othervalue + 0.5 * adjust + guesses[otherindex][1]) % adjust) -
               (othervalue + 0.5 * adjust);
            guesses[otherindex][2] =
              ((othervalue + 0.5 * adjust + guesses[otherindex][2]) % adjust) -
               (othervalue + 0.5 * adjust);
          }
// System.out.println(" adjusted = " + (float) guesses[0][1] + " " +
//                    (float) guesses[1][1] + " " + (float) guesses[2][1]);
          double g = othervalue - guesses[otherindex][1];
          error = Math.abs(g);
          if (error <= EPS) break;
          double gp = othervalue - guesses[otherindex][0];
          double gm = othervalue - guesses[otherindex][2];
          double dg = (gp - gm) / (EPS + EPS);
// System.out.println("r = " + (float) r + " g = " + (float) g + " gm = " +
//                    (float) gm + " gp = " + (float) gp + " dg = " + (float) dg);
          r = r - g / dg;
        }
        if (error < error_limit) {
          ray_pos = (float) r;
        }
        else {
          // System.out.println("error = " + error + " itry = " + itry);
          ray_pos = Float.NaN;
        }
      }
    } // end (tuple != null)
    return ray_pos;
  }

  /**
   * <b>WARNING!</b>
   * Do <b>NOT</b> use this routine unless you know what you are doing!
   * remove link from Links[] array when remote connection fails
   * @param link DataDisplayLink to remove
   */
  public void removeLink(DataDisplayLink link)
  {
    if (display == null) return;
    final int newLen = Links.length - 1;
    if (newLen < 0) {
      // give up if the Links array is already empty
      return;
    }

    DataDisplayLink[] newLinks = new DataDisplayLink[newLen];

    int n = 0;
    for (int i = 0; i <= newLen; i++) {
      if (Links[i] == link) {
        // skip the specified link
      } else {
        if (n == newLen) {
          // yikes!   Obviously didn't find this link in the list!
          return;
        }
        newLinks[n++] = Links[i];
      }
    }

    if (n < newLen) {
      // Hmmm ... seem to have removed multiple instances of 'link'!
      DataDisplayLink[] newest = new DataDisplayLink[n];
      System.arraycopy(newLinks, 0, newest, 0, n);
      newLinks = newest;
    }

    Links = newLinks;
  }

  /**
   * @return a copy of this DataRenderer
   */
  public abstract Object clone() throws CloneNotSupportedException;
  
  public boolean hasPolygonOffset() {
    return hasPolygonOffset;
  }
  
  public void setHasPolygonOffset(boolean hasPolygonOffset) {
    this.hasPolygonOffset = hasPolygonOffset;
  }
  
  public float getPolygonOffset() {
    return polygonOffset;
  }
  
  public void setPolygonOffset(float polygonOffset) {
    this.polygonOffset = polygonOffset;
  }
  
  public float getPolygonOffsetFactor() {
    return polygonOffsetFactor;
  }
  
  public void setPolygonOffsetFactor(float polygonOffsetFactor) {
    this.polygonOffsetFactor = polygonOffsetFactor;
  }

}

