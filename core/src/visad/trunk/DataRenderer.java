//
// DataRenderer.java
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

  /** this returns a Vector of Strings from the BadMappingExceptions
      and UnimplementedExceptions generated during the last invocation
      of this DataRenderer's doAction method;
      there is no need to over-ride this method, but it may be invoked
      by DisplayRenderer; gets a clone of exceptionVector to avoid
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

  public abstract void toggle(boolean on);

  public synchronized void setLinks(DataDisplayLink[] links) {
    if (links == null || links.length == 0) return;
    Links = links;
    feasible = new boolean[Links.length];
    changed = new boolean[Links.length];
    for (int i=0; i<Links.length; i++) feasible[i] = false;
  }

  /** return an array of links to Data objects to be rendered;
      Data objects are accessed by DataDisplayLink.getData() */
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

  public boolean checkAction() {
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

  /** check if re-transform is needed; if initialize is true then
      compute ranges for RealType-s and Animation sampling */
  public DataShadow prepareAction(boolean go, boolean initialize,
                                  DataShadow shadow)
         throws VisADException, RemoteException {
    any_changed = false;
    all_feasible = true;
    any_transform_control = false;

    for (int i=0; i<Links.length; i++) {
      changed[i] = false;
      DataReference ref = Links[i].getDataReference();
      // test for changed Controls that require doTransform
/*
System.out.println(display.getName() +
                   " Links[" + i + "].checkTicks() = " + Links[i].checkTicks() +
                   " feasible[" + i + "] = " + feasible[i] + " go = " + go);
MathType junk = Links[i].getType();
if (junk != null) System.out.println(junk.prettyString());
*/
      if (Links[i].checkTicks() || !feasible[i] || go) {
/*
boolean check = Links[i].checkTicks();
System.out.println("DataRenderer.prepareAction: check = " + check + " feasible = " +
                   feasible[i] + " go = " + go + "  " +
                   Links[i].getThingReference().getName());
DisplayImpl.printStack("prepareAction");
*/
        // data has changed - need to re-display
        changed[i] = true;
        any_changed = true;

        // create ShadowType for data, classify data for display
        feasible[i] = Links[i].prepareData();
        if (!feasible[i]) {
          all_feasible = false;
          // WLH 31 March 99
          clearBranch();
        }
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

    return shadow;
  }

  /** clear scene graph component */
  public abstract void clearBranch();

  /** re-transform if needed;
      return false if not done */
  /** transform linked Data objects into a display list, if
      any Data object values have changed or relevant Controls
      have changed; DataRenderers that assume the default
      implementation of DisplayImpl.doAction can determine
      whether re-transform is needed by:
        (get_all_feasible() && (get_any_changed() || get_any_transform_control()));
      these flags are computed by the default DataRenderer
      implementation of prepareAction;
      the return boolean is true if the transform was done
      successfully */
  public abstract boolean doAction() throws VisADException, RemoteException;

  public boolean getBadScale() {
    boolean badScale = false;
    for (int i=0; i<Links.length; i++) {
      if (!feasible[i]) {
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
    return badScale;
  }

  /** clear any display list created by the most recent doAction
      invocation */
  public abstract void clearScene();

  public void clearAVControls() {
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

  /** factory for constructing a subclass of ShadowType appropriate
      for the graphics API, that also adapts ShadowFunctionType;
      these factories are invoked by the buildShadowType methods of
      the MathType subclasses, which are invoked by
      DataDisplayLink.prepareData, which is invoked by
      DataRenderer.prepareAction */
  public abstract ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** factory for constructing a subclass of ShadowType appropriate
      for the graphics API, that also adapts ShadowRealTupleType */
  public abstract ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** factory for constructing a subclass of ShadowType appropriate
      for the graphics API, that also adapts ShadowRealType */
  public abstract ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** factory for constructing a subclass of ShadowType appropriate
      for the graphics API, that also adapts ShadowSetType */
  public abstract ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** factory for constructing a subclass of ShadowType appropriate
      for the graphics API, that also adapts ShadowTextType */
  public abstract ShadowType makeShadowTextType(
         TextType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** factory for constructing a subclass of ShadowType appropriate
      for the graphics API, that also adapts ShadowTupleType */
  public abstract ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** DataRenderer-specific decision about which Controls require re-transform;
      may be over-ridden by DataRenderer sub-classes; this decision may use
      some values computed by link.prepareData */
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

  /** used for transform time-out hack */
  public DataDisplayLink getLink() {
    return null;
  }

  public boolean isLegalTextureMap() {
    return true;
  }

  /* ********************** */
  /*  flow rendering stuff  */
  /* ********************** */

  // value array (display_values) indices
  //   ((ScalarMap) MapVector.elementAt(valueToMap[index]))
  // can get these indices through shadow_data_out or shadow_data_in


  // true if lat and lon in data_in & shadow_data_in is allSpatial
  // or if lat and lon in data_in & lat_lon_in_by_coord
  boolean lat_lon_in = false;
  // true if lat_lon_in and shadow_data_out is allSpatial
  // i.e., map from lat, lon to display is through data CoordinateSystem
  boolean lat_lon_in_by_coord = false;
  // true if lat and lon in data_out & shadow_data_out is allSpatial
  boolean lat_lon_out = false;
  // true if lat_lon_out and shadow_data_in is allSpatial
  // i.e., map from lat, lon to display is inverse via data CoordinateSystem
  boolean lat_lon_out_by_coord = false;

  int lat_lon_dimension = -1;

  ShadowRealTupleType shadow_data_out = null;
  RealTupleType data_out = null;
  Unit[] data_units_out = null;
  // CoordinateSystem data_coord_out is always null

  ShadowRealTupleType shadow_data_in = null;
  RealTupleType data_in = null;
  Unit[] data_units_in = null;
  CoordinateSystem[] data_coord_in = null; // may be one per point

  // spatial ScalarMaps for allSpatial shadow_data_out
  ScalarMap[] sdo_maps = null;
  // spatial ScalarMaps for allSpatial shadow_data_in
  ScalarMap[] sdi_maps = null;
  int[] sdo_spatial_index = null;
  int[] sdi_spatial_index = null;

  // indices of RealType.Latitude and RealType.Longitude
  // if lat_lon_in then indices in data_in
  // if lat_lon_out then indices in data_out
  // if lat_lon_spatial then values indices
  int lat_index = -1;
  int lon_index = -1;
  // non-negative if lat & lon in a RealTupleType of length 3
  int other_index = -1;
  // true if other_index Units convertable to meter
  boolean other_meters = false;

  // from doTransform
  RealVectorType[] rvts = {null, null};

  public RealVectorType getRealVectorTypes(int index) {
    if (index == 0 || index == 1) return rvts[index];
    else return null;
  }

  public int[] getLatLonIndices() {
    return new int[] {lat_index, lon_index};
  }

  public void setLatLonIndices(int[] indices) {
    lat_index = indices[0];
    lon_index = indices[1];
  }

  public int getEarthDimension() {
    return lat_lon_dimension;
  }

  public Unit[] getEarthUnits() {
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

  public float getLatLonRange() {
    double[] rlat = null;
    double[] rlon = null;
    int lat = lat_index;
    int lon = lon_index;
    if ((lat_lon_out && !lat_lon_out_by_coord) ||
        (lat_lon_in && lat_lon_in_by_coord)) {
      rlat = lat >= 0 ? sdo_maps[lat].getRange() : new double[] {Double.NaN, Double.NaN};
      rlon = lon >= 0 ? sdo_maps[lon].getRange() : new double[] {Double.NaN, Double.NaN};
    }
    else if ((lat_lon_in && !lat_lon_in_by_coord) ||
             (lat_lon_out && lat_lon_out_by_coord)) {
      rlat = lat >= 0 ? sdi_maps[lat].getRange() : new double[] {Double.NaN, Double.NaN};
      rlon = lon >= 0 ? sdi_maps[lon].getRange() : new double[] {Double.NaN, Double.NaN};
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

  /** convert (lat, lon) or (lat, lon, other) values to
      display (x, y, z) */
  public float[][] earthToSpatial(float[][] locs, float[] vert)
         throws VisADException {
    return earthToSpatial(locs, vert, null);
  }

  public float[][] earthToSpatial(float[][] locs, float[] vert,
                                  float[][] base_spatial_locs)
         throws VisADException {
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
      spatial_locs = display_coordinate_system.toReference(spatial_locs);
    }
    return spatial_locs;
  }

  /** convert display (x, y, z) to (lat, lon) or (lat, lon, other)
      values */
  public float[][] spatialToEarth(float[][] spatial_locs)
         throws VisADException {
    float[][] base_spatial_locs = new float[3][];
    return spatialToEarth(spatial_locs, base_spatial_locs);
  }

  public float[][] spatialToEarth(float[][] spatial_locs,
                                  float[][] base_spatial_locs)
         throws VisADException {
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
      spatial_locs = display_coordinate_system.fromReference(spatial_locs);
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

  // information from doTransform
  public void setEarthSpatialData(ShadowRealTupleType s_d_i,
                    ShadowRealTupleType s_d_o, RealTupleType d_o,
                    Unit[] d_u_o, RealTupleType d_i,
                    CoordinateSystem[] d_c_i, Unit[] d_u_i)
         throws VisADException {

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
    other_index = -1;
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
        other_index = 3 - (lat_index_local + lon_index_local);
        if (Unit.canConvert(d_u_i[other_index], CommonUnit.meter)) {
          other_meters = true;
        }
      }
    }
    else { // if( !(lat & lon in di, di dimension = 2 or 3) )
      lat_index_local = -1;
      lon_index_local = -1;
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
        other_index = 3 - (lat_index_local + lon_index_local);
        if (Unit.canConvert(d_u_i[other_index], CommonUnit.meter)) {
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
    return;
  }

  /** return array of spatial ScalarMap for srt, or null */
  private ScalarMap[] getSpatialMaps(ShadowRealTupleType srt,
                                     int[] spatial_index) {
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

  /** return array of flow ScalarMap for srt, or null */
  private int getFlowMaps(ShadowRealTupleType srt, ScalarMap[] maps) {
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


/* WLH 15 April 2000
  // from assembleFlow
  ScalarMap[][] flow_maps = null;
  float[] flow_scale = null;

  public void setFlowDisplay(ScalarMap[][] maps, float[] fs) {
    flow_maps = maps;
    flow_scale = fs;
  }
*/

  // if non-null, float[][] new_spatial_values =
  //   display_coordinate_system.toReference(spatial_values);
  CoordinateSystem display_coordinate_system = null;
  // spatial_tuple and spatial_value_indices are set whether
  //   display_coordinate_system is null or not
  DisplayTupleType spatial_tuple = null;
  // map from spatial_tuple tuple_index to value array indices
  int[] spatial_value_indices = {-1, -1, -1};

  float[] default_spatial_in = {0.0f, 0.0f, 0.0f};

  // true if lat and lon mapped directly to spatial
  boolean lat_lon_spatial = false;
  ScalarMap lat_map = null;
  ScalarMap lon_map = null;
  int lat_spatial_index = -1;
  int lon_spatial_index = -1;

  // spatial map getRange() results for flow adjustment
  double[] ranges = null;

  public double[] getRanges() {
    return ranges;
  }

  // WLH 4 March 2000
  public CoordinateSystem getDisplayCoordinateSystem() {
    return display_coordinate_system ;
  }

  // information from assembleSpatial
  public void setEarthSpatialDisplay(CoordinateSystem coord,
           DisplayTupleType t, DisplayImpl display, int[] indices,
           float[] default_values, double[] r)
         throws VisADException {
    display_coordinate_system = coord;
    spatial_tuple = t;
    System.arraycopy(indices, 0, spatial_value_indices, 0, 3);
/* WLH 5 Dec 99
    spatial_value_indices = indices;
*/
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
  DisplayTupleType tuple;

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

  private boolean stop = false;

  public synchronized void realCheckDirect()
         throws VisADException, RemoteException {
    setIsDirectManipulation(false);

    link = getLinks()[0];
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
/* WLH 3 Aug 98
      if (!Display.DisplaySpatialCartesianTuple.equals(rtuple)) {
*/
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
/* WLH 3 Aug 98
      if (domainAxis == -1) {
        throw new DisplayException("DataRenderer.realCheckDirect:" +
                                   "too few spatial domain");
      }
      if (directManifoldDimension < 2) {
        throw new DisplayException("DataRenderer.realCheckDirect:" +
                                   "directManifoldDimension < 2");
      }
*/
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
/* WLH 3 Aug 98
      else if (!Display.DisplaySpatialCartesianTuple.equals(
                   ((ShadowRealTupleType) shadow).getDisplaySpatialTuple())) {
*/
        whyNotDirect = nonSpatial;
        return;
      }
      else if (((ShadowRealTupleType) shadow).getSpatialReference()) {
        whyNotDirect = viaReference;
        return;
      }
/* WLH 3 Aug 98
      setIsDirectManipulation(true);
*/
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
/* WLH 3 Aug 98
      else if(!Display.DisplaySpatialCartesianTuple.equals(
                   ((ShadowRealType) shadow).getDisplaySpatialTuple())) {
*/
        whyNotDirect = nonSpatial;
        return;
      }
/* WLH 3 Aug 98
      setIsDirectManipulation(true);
*/
      if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
        tuple = null;
      }

      domainAxis = -1;
      for (int i=0; i<3; i++) {
        axisToComponent[i] = -1;
        directMap[i] = null;
      }
      directManifoldDimension = setDirectMap((ShadowRealType) shadow, 0, false);
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

  /** set directMap and axisToComponent (domain = false) or
      domainAxis (domain = true) from real; called by realCheckDirect */
  synchronized int setDirectMap(ShadowRealType real, int component, boolean domain) {
    Enumeration maps = real.getSelectedMapVector().elements();
    while (maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      DisplayRealType dreal = map.getDisplayScalar();
      DisplayTupleType tuple = dreal.getTuple();
      if (Display.DisplaySpatialCartesianTuple.equals(tuple) ||
          (tuple != null &&
           tuple.getCoordinateSystem().getReference().equals(
           Display.DisplaySpatialCartesianTuple)) ) {
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
    // these are X, Y, Z values
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
/*
System.out.println("checkClose: distance = " + distance);
*/
    return distance;
  }

  /** mouse button released, ending direct manipulation */
  public synchronized void release_direct() {
  }

  public void stop_direct() {
    stop = true;
  }

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
      Data data = link.getData();

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
/* WLH 26 July 99
            float g = d[0];
            vect.addElement(rtype.getName() + " = " + g);
*/
            String valueString = new Real(rtype, d[0]).toValueString();
            vect.addElement(rtype.getName() + " = " + valueString);

            getDisplayRenderer().setCursorStringVector(vect);
            break;
          }
        }
        ref.setData(newData);
        link.clearData(); // WLH 27 July 99
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
/* WLH 26 July 99
            float g = d[0];
            vect.addElement(rtype.getName() + " = " + g);
*/
            String valueString = new Real(rtype, d[0]).toValueString();
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
        link.clearData(); // WLH 27 July 99
      }
      else if (type instanceof FunctionType) {
        Vector vect = new Vector();
        if (first) lastIndex = -1;
        int k = getDomainAxis();
        f[0] = x[k];
        d = getDirectMap(k).inverseScaleValues(f);
        RealType rtype = (RealType) getDirectMap(k).getScalar();
        // WLH 4 Jan 99
        // convert d from default Unit to actual domain Unit of data
        Unit[] us = ((Field) data).getDomainUnits();
        if (us != null && us[0] != null) {
          d[0] = (float) us[0].toThis((double) d[0], rtype.getDefaultUnit());
        }
        // create location string
        float g = d[0];
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
/* WLH 26 July 99
            g = (float) d[0];
            vect.addElement(rtype.getName() + " = " + g);
*/
            String valueString = new Real(rtype, d[0]).toValueString();
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

  public void addPoint(float[] x) throws VisADException {
  }



  /** flag indicating whether DirectManipulationRenderer is valid
      for this ShadowType */
  private boolean isDirectManipulation;

  /** set isDirectManipulation = true if this DataRenderer
      supports direct manipulation for its linked Data */
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

