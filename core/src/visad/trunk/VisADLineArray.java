//
// VisADLineArray.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

/**
   VisADLineArray stands in for j3d.LineArray
   and is Serializable.<P>
*/
public class VisADLineArray extends VisADGeometryArray {

  /**
   * Merge an array of VisADLineArrays into a single VisADLineArray.
   * @param  arrays  array of VisADLineArrays (may be null)
   * @return a single VisADLineArray with all the info of arrays.
   *         returns null if input is null.
   */
  public static VisADLineArray merge(VisADLineArray[] arrays)
         throws VisADException {
    if (arrays == null || arrays.length == 0) return null;
    VisADLineArray array = new VisADLineArray();
    merge(arrays, array);
/*
    int n = arrays.length;
    int count = 0;
    boolean color_flag = (arrays[0].colors != null);
    for (int i=0; i<n; i++) {
      count += arrays[i].vertexCount;
      if (color_flag != (arrays[i].colors != null)) {
        throw new DisplayException("VisADLineArray.merge: formats don't match");
      }
    }
    float[] coordinates = new float[3 * count];
    byte[] colors = null;
    if (color_flag) {
      colors = new byte[3 * count];
    }
    int k = 0;
    int m = 0;
    for (int i=0; i<n; i++) {
      float[] c = arrays[i].coordinates;
      for (int j=0; j<3*arrays[i].vertexCount; j++) {
        coordinates[k++] = c[j];
      }
      if (color_flag) {
        byte[] b = arrays[i].colors;
        for (int j=0; j<3*arrays[i].vertexCount; j++) {
          colors[m++] = b[j];
        }
      }
    }
    VisADLineArray array = new VisADLineArray();
    array.vertexCount = count;
    array.coordinates = coordinates;
    array.colors = colors;
    array.vertexFormat = arrays[0].vertexFormat;
*/
    return array;
  }

  public VisADGeometryArray adjustLongitude(DataRenderer renderer)
         throws VisADException {
    float[] lons = getLongitudes(renderer);
    if (any_longitude_rotate) {
      VisADLineArray array = new VisADLineArray();
      array.vertexCount = vertexCount;
      array.coordinates = coordinates;
      array.colors = colors;
      return array;
    }
    else {
      return this;
    }
  }


  private final static double LIMIT = 1.0f; // constant for TEST = 0
  private final static double ALPHA = 0.01f; // constant for TEST = 1

  /** eliminate any vectors or triangles crossing seams of
      map projections, defined by display-side CoordinateSystems;
      this default implementation does nothing */
  public VisADGeometryArray adjustSeam(DataRenderer renderer)
         throws VisADException {
    CoordinateSystem coord_sys = renderer.getDisplayCoordinateSystem();
    if (coord_sys == null || 
        coord_sys instanceof SphericalCoordinateSystem ||
        coord_sys instanceof CylindricalCoordinateSystem ||
        coordinates == null) {
      return this;
    }

    int len = coordinates.length / 3;

    // WLH 15 March 2000
    if (len < 6) return this;

// System.out.println("VisADLineArray.adjustSeam try");

    double[][] cs = new double[3][len];
    int j = 0;
    for (int i=0; i<len; i++) {
      cs[0][i] = coordinates[j++];
      cs[1][i] = coordinates[j++];
      cs[2][i] = coordinates[j++];
    }
    double[][] rs = coord_sys.fromReference(Set.copyDoubles (cs));
    boolean[] test = new boolean[len];
    int last_i;

    boolean any_split = false;

    // TEST 1
    if (len < 2) return this;
    double[][] bs = new double[3][len/2];
    double[][] ss = new double[3][len/2];
    // ALPHA = 0.01f
    double ALPHA1 = 1.0f + ALPHA;
    double ALPHA1m = 1.0f - ALPHA;
    for (int i=0; i<len/2; i++) {
      // BS = point ALPHA * opposite direction
      // bs = pt_i + 0.01 * (pt_i - pt_ip1), not ref
      bs[0][i] = ALPHA1 * rs[0][2*i] - ALPHA * rs[0][2*i+1];
      bs[1][i] = ALPHA1 * rs[1][2*i] - ALPHA * rs[1][2*i+1];
      bs[2][i] = ALPHA1 * rs[2][2*i] - ALPHA * rs[2][2*i+1];
      // SS = point ALPHA * same direction
      // ss = pt_ip1 + 0.01 * (pt_ip1 - pt_i), not ref
      ss[0][i] = ALPHA1 * rs[0][2*i+1] - ALPHA * rs[0][2*i];
      ss[1][i] = ALPHA1 * rs[1][2*i+1] - ALPHA * rs[1][2*i];
      ss[2][i] = ALPHA1 * rs[2][2*i+1] - ALPHA * rs[2][2*i];
    }
    double[][] ds = coord_sys.toReference(bs);
    // ds = pt_i + 0.01 * (pt_i - pt_ip1), ref
    double[][] es = coord_sys.toReference(ss);
    // es = pt_ip1 + 0.01 * (pt_ip1 - pt_i), ref
    double IALPHA = 1.0f / ALPHA;
    for (int i=0; i<len; i+=2) {
      // a = original line segment, ref
      // a = pt_ip1 - pt_i, ref
      double a0 = cs[0][i+1] - cs[0][i];
      double a1 = cs[1][i+1] - cs[1][i];
      double a2 = cs[2][i+1] - cs[2][i];
      // b = estimate of vector using ALPHA * opposite direction
      // b = 100.0 * (pt_i - toRef(pt_i + 0.01 * (pt_i - pt_ip1)) )
      // if no break, b = pt_ip1 - pt_i, ref
      double b0 = IALPHA * (cs[0][i] - ds[0][i/2]);
      double b1 = IALPHA * (cs[1][i] - ds[1][i/2]);
      double b2 = IALPHA * (cs[2][i] - ds[2][i/2]);
      double aa = (a0 * a0 + a1 * a1 + a2 * a2);
      double aminusb =
        (b0 - a0) * (b0 - a0) +
        (b1 - a1) * (b1 - a1) +
        (b2 - a2) * (b2 - a2);
      double abratio = aminusb / aa;

      // c = estimate of vector using ALPHA * opposite direction
      // c = 100.0 * (pt_ip1 - toRef(pt_ip1 + 0.01 * (pt_ip1 - pt_i)) )
      // if no break, c = pti - pt_ip1, ref
      double c0 = IALPHA * (cs[0][i+1] - es[0][i/2]);
      double c1 = IALPHA * (cs[1][i+1] - es[1][i/2]);
      double c2 = IALPHA * (cs[2][i+1] - es[2][i/2]);
      double aminusc =
        (c0 + a0) * (c0 + a0) +
        (c1 + a1) * (c1 + a1) +
        (c2 + a2) * (c2 + a2);
      double acratio = aminusc / aa;

      // true for bad segment
      test[i] = (0.01f < abratio) || (0.01f < acratio);
/*
if ((0.01f < abratio) != (0.01f < acratio)) {
System.out.println("test[" + i + "] " + abratio + " " + acratio);
}
*/

      if (test[i]) any_split = true;
    } // end for (int i=0; i<len; i+=2)

    bs = null;
    ss = null;

    if (!any_split) {
      return this;
    }

    // TEST 0
    double[] lengths = new double[len];
    for (int i=0; i<len;  i++) lengths[i] = 0.0f;
    double mean_length = 0.0f;
    double var_length = 0.0f;
    double max_length = 0.0f;
    int num_length = 0;
    
    for (int i=0; i<len; i+=2) {
      double cd = (cs[0][i+1] - cs[0][i]) * (cs[0][i+1] - cs[0][i]) +
                 (cs[1][i+1] - cs[1][i]) * (cs[1][i+1] - cs[1][i]) +
                 (cs[2][i+1] - cs[2][i]) * (cs[2][i+1] - cs[2][i]);
      if (!test[i]) {
        lengths[i] = cd;
        num_length++;
        mean_length += lengths[i];
        var_length += lengths[i] * lengths[i];
        if (lengths[i] > max_length) max_length = lengths[i];
      }
    } // end for (int i=0; i<len; i+=2)
    if (num_length < 2) return this;
    mean_length = mean_length / num_length;
    var_length = (double)
      Math.sqrt((var_length - mean_length * mean_length) / num_length);
    double limit_length = mean_length + LIMIT * var_length;
/**TDR, TEST0 causes some problems...come back to this.
System.out.println("limit_length = " + limit_length + " " + max_length + " " +
                   mean_length + " " + var_length + " " + num_length);
    if (max_length >= limit_length) {
      for (int i=0; i<len; i+=2) {
        test[i] = test[i] || (lengths[i] > limit_length);
      }
    }
    */

    cs = null;
    rs = null;

    float[] lastcoord = null;
    byte[] lastcol = null;
    VisADLineArray array = new VisADLineArray();
    // worst case splits every line
    float[] coords = new float[3 * coordinates.length];
    int color_length = 0;
    byte[] cols = null;
    if (colors != null) {
      color_length = 3;
      cols = new byte[3 * colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }

    int ki = 0;
    int kj = 0;
    j = 0;
    for (int i=0; i<3*len; i+=6) {
      if (!test[i/3]) {
        coords[ki] = coordinates[i];
        coords[ki+1] = coordinates[i+1];
        coords[ki+2] = coordinates[i+2];
        coords[ki+3] = coordinates[i+3];
        coords[ki+4] = coordinates[i+4];
        coords[ki+5] = coordinates[i+5];
        ki += 6;
        if (color_length == 3) {
          cols[kj] = colors[j];
          cols[kj+1] = colors[j+1];
          cols[kj+2] = colors[j+2];
          cols[kj+3] = colors[j+3];
          cols[kj+4] = colors[j+4];
          cols[kj+5] = colors[j+5];
          kj += 6;
        }
        else if (color_length == 4) {
          cols[kj] = colors[j];
          cols[kj+1] = colors[j+1];
          cols[kj+2] = colors[j+2];
          cols[kj+3] = colors[j+3];
          cols[kj+4] = colors[j+4];
          cols[kj+5] = colors[j+5];
          cols[kj+6] = colors[j+6];
          cols[kj+7] = colors[j+7];
          kj += 8;
        }
      }
      else {
        any_split = true;
      }
      j += 2 * color_length;
    }

   //System.out.println("VisADLineArray.adjustSeam any_split = " + any_split);

    if (!any_split) {
      return this;
    }
    else {
      array.vertexCount = ki / 3;
      array.coordinates = new float[ki];
      System.arraycopy(coords, 0, array.coordinates, 0, ki);
      if (colors != null) {
        array.colors = new byte[kj];
        System.arraycopy(cols, 0, array.colors, 0, kj);
      }
      return array;
    }
  }

  public VisADGeometryArray removeMissing() {
    VisADLineArray array = new VisADLineArray();
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
    for (int i=0; i<coordinates.length; i+=6) {
      if (coordinates[i] == coordinates[i] &&
          coordinates[i+1] == coordinates[i+1] &&
          coordinates[i+2] == coordinates[i+2] &&
          coordinates[i+3] == coordinates[i+3] &&
          coordinates[i+4] == coordinates[i+4] &&
          coordinates[i+5] == coordinates[i+5] &&
          !Float.isInfinite(coordinates[i]) &&
          !Float.isInfinite(coordinates[i+1]) &&
          !Float.isInfinite(coordinates[i+2]) &&
          !Float.isInfinite(coordinates[i+3]) &&
          !Float.isInfinite(coordinates[i+4]) &&
          !Float.isInfinite(coordinates[i+5])) {
        coords[k] = coordinates[i];
        coords[k+1] = coordinates[i+1];
        coords[k+2] = coordinates[i+2];
        coords[k+3] = coordinates[i+3];
        coords[k+4] = coordinates[i+4];
        coords[k+5] = coordinates[i+5];
        if (colors != null) {
          cols[m] = colors[j];
          cols[m+1] = colors[j+1];
          cols[m+2] = colors[j+2];
          m += 3;
          if (color_length == 4) {
            cols[m++] = colors[j+3];
          }
          cols[m] = colors[j+color_length];
          cols[m+1] = colors[j+color_length+1];
          cols[m+2] = colors[j+color_length+2];
          m += 3;
          if (color_length == 4) {
            cols[m++] = colors[j+color_length+3];
          }
        }
        k += 6;
      }
      else { // missing coordinates values
        any_missing = true;
      }
      j += 2 * color_length;
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

  /**
   * Clone this VisADLineArray
   * @return clone of this
   */
  public Object clone() {
    VisADLineArray array = new VisADLineArray();
    copy(array);
    return array;
  }

}

