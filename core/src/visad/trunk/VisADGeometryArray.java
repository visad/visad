//
// VisADGeometryArray.java
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

/**
   VisADGeometryArray stands in for j3d.GeometryArray
   and is Serializable.<P>
*/
public abstract class VisADGeometryArray extends VisADSceneGraphObject
       implements Cloneable {

  public int vertexCount;
  public int vertexFormat;
  public float[] coordinates;
  public float[] normals;
  public byte[] colors;
  public float[] texCoords;

  // stuff for longitude
  boolean any_longitude_rotate = false;
  int longitude_axis = -1;
  ScalarMap longitude_map = null;

  public VisADGeometryArray() {
    vertexCount = 0;
    vertexFormat = 0;
    coordinates = null;
    normals = null;
    colors = null;
    texCoords = null;
  }

  /** default case: do nothing */
  public VisADGeometryArray adjustSeam(DataRenderer renderer)
         throws VisADException {
    CoordinateSystem coord_sys = renderer.getDisplayCoordinateSystem();
    // WLH 13 March 2000
    // if (coord_sys == null) return this;
    if (coord_sys == null || coord_sys instanceof SphericalCoordinateSystem) {
      return this;
    }
    return this;
  }

  /** default case: rotate if necessary, then return points */
  public VisADGeometryArray adjustLongitude(DataRenderer renderer)
         throws VisADException {
    float[] lons = getLongitudes(renderer);
    if (any_longitude_rotate) {
      // some coordinates changed, so return VisADPointArray
      VisADPointArray array = new VisADPointArray();
      array.vertexCount = vertexCount;
      array.coordinates = coordinates;
      array.colors = colors;
      return array;
    }
    else {
      return this;
    }
  }

  void rotateLongitudes(float[] lons, float base, int axis,
                        ScalarMap map) {
    // so rotate longitudes to base
    for (int i=0; i<vertexCount; i++) {
      if (lons[i] == lons[i]) {
        float x = (lons[i] - base) % 360.0f;
        x += (x < 0.0f) ? (360.0f + base) : base;
        if (x != lons[i]) {
          lons[i] = x;
          any_longitude_rotate = true;
        }
      }
    }
    if (any_longitude_rotate) {
      float[] coords = map.scaleValues(lons);
      for (int i=0; i<vertexCount; i++) {
        coordinates[3 * i + axis] = coords[i];
      }
    }
  }

  float[] getLongitudes(DataRenderer renderer)
          throws VisADException {
    any_longitude_rotate = false;
    longitude_map = null;
    longitude_axis = -1;
    Vector mapVector = renderer.getDisplay().getMapVector();
    Enumeration maps = mapVector.elements();
    while(maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      DisplayRealType dreal = map.getDisplayScalar();
      DisplayTupleType tuple = dreal.getTuple();
      if (!RealType.Longitude.equals(map.getScalar())) continue;
      if (Display.Longitude.equals(dreal)) return null; // do nothing!
      if (Display.DisplaySpatialCartesianTuple.equals(tuple)) {
        // have found a map from Longitude to a Cartesian spatial axis
        double[] map_range = map.getRange();
        float map_min = (float) map_range[0];
        float map_max = (float) map_range[1];
        // get Longitude values
        int axis = dreal.getTupleIndex();

        // leave some information for getLongitudeRange
        longitude_map = map;
        longitude_axis = axis;

        float[] lons = new float[vertexCount];
        for (int i=0; i<vertexCount; i++) {
          lons[i] = coordinates[3 * i + axis];
        }
        lons = map.inverseScaleValues(lons);
        // get range of Longitude values
        float lon_min = Float.MAX_VALUE;
        float lon_max = Float.MIN_VALUE;
        for (int i=0; i<vertexCount; i++) {
          if (lons[i] == lons[i]) {
            if (lons[i] < lon_min) lon_min = lons[i];
            if (lons[i] > lon_max) lon_max = lons[i];
          }
        }
        if (lon_min == Float.MAX_VALUE) return lons;
        if (map_min == map_min && map_max == map_max) {
          float map_delta = 0.1f * (map_max - map_min);
          if ( ((map_min + map_delta) < lon_min &&
                (map_max + map_delta) < lon_max) ||
               (lon_min < (map_min - map_delta) &&
                lon_max < (map_max - map_delta)) ) {
            // actual longitudes are shifted significantly from map,
            // so rotate longitudes to base at map_min
            rotateLongitudes(lons, map_min, axis, map);
          }
          else if ((lon_min + 360.0f) < lon_max) {
            rotateLongitudes(lons, lon_min, axis, map);
          }
        }
        else if ((lon_min + 360.0f) < lon_max) {
          rotateLongitudes(lons, lon_min, axis, map);
        }
        return lons;
      }
    }

    int[] indices = renderer.getLatLonIndices();
    if (indices[0] < 0 || indices[1] < 0) return null;
    float[][] locs = new float[3][vertexCount];
    int k = 0;
    for (int i=0; i<vertexCount; i++) {
      locs[0][i] = coordinates[k++];
      locs[1][i] = coordinates[k++];
      locs[2][i] = coordinates[k++];
    }
    float[][] latlons = renderer.earthToSpatial(locs, null);
    return latlons[1];
  }

  float[] getLongitudeRange(float[] lons, int[] axis,
                            float[] coords) {
    float[] lon_range = {Float.NaN, Float.NaN};
    axis[0] = -1;
    coords[0] = Float.NaN;
    coords[1] = Float.NaN;
    float lon_min = Float.MAX_VALUE;
    float lon_max = Float.MIN_VALUE;
    for (int i=0; i<vertexCount; i++) {
      if (lons[i] == lons[i]) {
        if (lons[i] < lon_min) lon_min = lons[i];
        if (lons[i] > lon_max) lon_max = lons[i];
      }
    }
    // WLH 30 Dec 99
    if ((lon_max - lon_min) < 1.0f) {
      lon_max += 0.5f;
      lon_min -= 0.5f;
    }
    if (lon_min <= lon_max) {
/* WLH 30 Dec 99
      float delta = 1.0f; // allow a little slop in Longitudes
*/
      float delta = (lon_max - lon_min) / 10.0f; // allow a little slop in Longitudes
      if (delta > 1.0f) delta = 1.0f;

      float x = (lon_min + delta) % 180.0f;
      if (x < 0.0f) x += 180.0f;
      float y = (lon_min + delta) - x;
      if ((lon_max - delta) < y + 360.0f) {
        lon_range[0] = y;
        lon_range[1] = y + 360.0f;
      }
      else {
        lon_range[0] = lon_min;
        lon_range[1] = lon_min + 360.0f;
      }
      axis[0] = longitude_axis;
      if (longitude_map != null) {
        float[] xcoords = longitude_map.scaleValues(lon_range);
        coords[0] = xcoords[0];
        coords[1] = xcoords[1];
      }
      else {
        coords[0] = Float.NaN;
        coords[1] = Float.NaN;
      }
    }
    return lon_range;
  }

  public VisADGeometryArray removeMissing() {
    VisADPointArray array = new VisADPointArray();
    float[] coords = new float[coordinates.length];
    int color_length = 3;
    byte[] cols = null;
    if (colors != null) {
      cols = new byte[colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    int k = 0;
    int m = 0;
    int j = 0;
    boolean any_missing = false;
    for (int i=0; i<coordinates.length; i+=3) {
      if (coordinates[i] == coordinates[i] &&
          coordinates[i+1] == coordinates[i+1] &&
          coordinates[i+2] == coordinates[i+2]) {
        coords[k] = coordinates[i];
        coords[k+1] = coordinates[i+1];
        coords[k+2] = coordinates[i+2];
        if (colors != null) {
          cols[m] = colors[j];
          cols[m+1] = colors[j+1];
          cols[m+2] = colors[j+2];
          m += 3;
          if (color_length == 4) {
            cols[m++] = colors[j+3];
          }
        }
        k += 3;
      }
      else { // missing coordinates values
        any_missing = true;
      }
      j += color_length;
    }
    if (!any_missing) {
      return this;
    }
    else {
      array.coordinates = new float[k];
      System.arraycopy(coords, 0, array.coordinates, 0, k);
      if (colors != null) {
        array.colors = new byte[m];
        System.arraycopy(cols, 0, array.colors, 0, m);
      }
      return array;
    }
  }

  static void merge(VisADGeometryArray[] arrays, VisADGeometryArray array)
         throws VisADException {
    if (arrays == null || arrays.length == 0 ||
        arrays[0] == null || array == null) return;
    int n = arrays.length;
    int count = 0;
    boolean color_flag = (arrays[0].colors != null);
    boolean normal_flag = (arrays[0].normals != null);
    boolean texCoord_flag = (arrays[0].texCoords != null);

    for (int i=0; i<n; i++) {
      if (arrays[i] == null) continue;
      count += arrays[i].vertexCount;
      if (color_flag != (arrays[i].colors != null) ||
          normal_flag != (arrays[i].normals != null) ||
          texCoord_flag != (arrays[i].texCoords != null)) {
        throw new DisplayException("VisADGeometryArray.merge: formats don't match");
      }
    }
    float[] coordinates = new float[3 * count];
    byte[] colors = null;
    float[] normals = null;
    float[] texCoords = null;
    if (color_flag) {
      colors = new byte[3 * count];
    }
    if (normal_flag) {
      normals = new float[3 * count];
    }
    if (texCoord_flag) {
      texCoords = new float[3 * count];
    }
    int k = 0;
    int kc = 0;
    int kn = 0;
    int kt = 0;
    for (int i=0; i<n; i++) {
      if (arrays[i] == null) continue;
      float[] c = arrays[i].coordinates;
      for (int j=0; j<3*arrays[i].vertexCount; j++) {
        coordinates[k++] = c[j];
      }
      if (color_flag) {
        byte[] b = arrays[i].colors;
        for (int j=0; j<3*arrays[i].vertexCount; j++) {
          colors[kc++] = b[j];
        }
      }
      if (normal_flag) {
        c = arrays[i].normals;
        for (int j=0; j<3*arrays[i].vertexCount; j++) {
          normals[kn++] = c[j];
        }
      }
      if (texCoord_flag) {
        c = arrays[i].texCoords;
        for (int j=0; j<3*arrays[i].vertexCount; j++) {
          texCoords[kt++] = c[j];
        }
      }
    }
    array.vertexCount = count;
    array.coordinates = coordinates;
    array.colors = colors;
    array.normals = normals;
    array.texCoords = texCoords;
    array.vertexFormat = arrays[0].vertexFormat;
    return;
  }

  public String toString() {
    String string = "GeometryArray, vertexCount = " + vertexCount +
                    " vertexFormat = " + vertexFormat;
    if (coordinates != null) {
      string = string + "\n coordinates = " + floatArrayString(coordinates);
    }
    if (colors != null) {
      string = string + "\n colors = " + byteArrayString(colors);
    }
    if (normals != null) {
      string = string + "\n normals = " + floatArrayString(normals);
    }
    if (texCoords != null) {
      string = string + "\n texCoords = " + floatArrayString(texCoords);
    }

    return string;
  }

  static String floatArrayString(float[] value) {
    String string = "";
    for (int i=0; i<value.length; i++) string = string + " " + value[i];
    return string;
  }

  static String byteArrayString(byte[] value) {
    String string = "";
    for (int i=0; i<value.length; i++) string = string + " " + value[i];
    return string;
  }

  public void copy(VisADGeometryArray array) {
    array.vertexCount = vertexCount;
    array.vertexFormat = vertexFormat;
    if (coordinates != null) {
      array.coordinates = new float[coordinates.length];
      System.arraycopy(coordinates, 0, array.coordinates, 0,
                       coordinates.length);
    }
    if (normals != null) {
      array.normals = new float[normals.length];
      System.arraycopy(normals, 0, array.normals, 0,
                       normals.length);
    }
    if (colors != null) {
      array.colors = new byte[colors.length];
      System.arraycopy(colors, 0, array.colors, 0,
                       colors.length);
    }
    if (texCoords != null) {
      array.texCoords = new float[texCoords.length];
      System.arraycopy(texCoords, 0, array.texCoords, 0,
                       texCoords.length);
    }
  }

  public abstract Object clone();

}

