//
// VisADLineStripArray.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
   VisADLineStripArray stands in for j3d.LineStripArray
   and is Serializable.<P>
*/
public class VisADLineStripArray extends VisADGeometryArray {
  public int[] stripVertexCounts;

  public static VisADLineStripArray merge(VisADLineStripArray[] arrays)
         throws VisADException {
    if (arrays == null || arrays.length == 0) return null;
    VisADLineStripArray array = new VisADLineStripArray();
    merge(arrays, array);
    int n = arrays.length;
    int nstrips = 0;
    for (int i=0; i<n; i++) {
      if (arrays[i] != null) {
        nstrips += arrays[i].stripVertexCounts.length;
      }
    }
    if (nstrips <= 0) return null;
    int[] stripVertexCounts = new int[nstrips];
    nstrips = 0;
    for (int i=0; i<n; i++) {
      if (arrays[i] != null) {
        int incnstrips = arrays[i].stripVertexCounts.length;
        for (int j=0; j<incnstrips; j++) {
          stripVertexCounts[nstrips + j] = arrays[i].stripVertexCounts[j];
        }
        nstrips += incnstrips;
      }
    }
    array.stripVertexCounts = stripVertexCounts;
    return array;
  }

  private final static int TEST = 1;
  private final static float LIMIT = 4.0f; // constant for TEST = 0
  private final static float ALPHA = 0.1f; // constant for TEST = 1

  public VisADGeometryArray adjustSeam(DataRenderer renderer)
         throws VisADException {
    CoordinateSystem coord_sys = renderer.getDisplayCoordinateSystem();
    // DRM 19 March 2002
    //if (coord_sys == null || coord_sys instanceof SphericalCoordinateSystem) {
    //  return this;
    //}
    if (coord_sys == null || coord_sys instanceof SphericalCoordinateSystem ||
        coordinates == null) {
      return this;
    }

    int len = coordinates.length / 3;

    // WLH 15 March 2000
    if (len < 6) return this;

    float[][] cs = new float[3][len];
    int j = 0;
    for (int i=0; i<len; i++) {
      cs[0][i] = coordinates[j++];
      cs[1][i] = coordinates[j++];
      cs[2][i] = coordinates[j++];
    }
    float[][] rs = coord_sys.fromReference(Set.copyFloats (cs));
    boolean[] test = new boolean[len];
    int last_i;

    if (TEST == 0) {
      float[] ratios = new float[len];
      for (int i=0; i<len;  i++) ratios[i] = 0.0f;
      float mean_ratio = 0.0f;
      float var_ratio = 0.0f;
      float max_ratio = 0.0f;
      int num_ratio = 0;

      last_i = 0; // start i for each vertex strip
      for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
        for (int i=last_i; i<last_i+stripVertexCounts[i_svc]-1; i++) {
          float cd = (cs[0][i+1] - cs[0][i]) * (cs[0][i+1] - cs[0][i]) +
                     (cs[1][i+1] - cs[1][i]) * (cs[1][i+1] - cs[1][i]) +
                     (cs[2][i+1] - cs[2][i]) * (cs[2][i+1] - cs[2][i]);
          float rd = (rs[0][i+1] - rs[0][i]) * (rs[0][i+1] - rs[0][i]) +
                     (rs[1][i+1] - rs[1][i]) * (rs[1][i+1] - rs[1][i]) +
                     (rs[2][i+1] - rs[2][i]) * (rs[2][i+1] - rs[2][i]);
          if (rd > 0.0f) {
            ratios[i] = cd / rd;
            num_ratio++;
            mean_ratio += ratios[i];
            var_ratio += ratios[i] * ratios[i];
            if (ratios[i] > max_ratio) max_ratio = ratios[i];
          }
        } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
        last_i += stripVertexCounts[i_svc];
      } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)
      if (num_ratio < 2) return this;
      mean_ratio = mean_ratio / num_ratio;
      var_ratio = (float)
        Math.sqrt((var_ratio - mean_ratio * mean_ratio) / num_ratio);
      float limit_ratio = mean_ratio + LIMIT * var_ratio;
      if (max_ratio < limit_ratio) return this;
      last_i = 0; // start i for each vertex strip
      for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
        for (int i=last_i; i<last_i+stripVertexCounts[i_svc]-1; i++) {
          test[i] = (ratios[i] > limit_ratio);
        } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
        last_i += stripVertexCounts[i_svc];
      } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)
    }
    else if (TEST == 1) {
      if (len < 2) return this;
      float[][] bs = new float[3][len-1];
      float ALPHA1 = 1.0f + ALPHA;
      for (int i=0; i<len-1; i++) {
        bs[0][i] = ALPHA1 * rs[0][i] - ALPHA * rs[0][i+1];
        bs[1][i] = ALPHA1 * rs[1][i] - ALPHA * rs[1][i+1];
        bs[2][i] = ALPHA1 * rs[2][i] - ALPHA * rs[2][i+1];
      }
      float[][] ds = coord_sys.toReference(bs);
      float IALPHA = 1.0f / ALPHA;
      last_i = 0; // start i for each vertex strip
      for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
        for (int i=last_i; i<last_i+stripVertexCounts[i_svc]-1; i++) {
          float a0 = cs[0][i+1] - cs[0][i];
          float a1 = cs[1][i+1] - cs[1][i];
          float a2 = cs[2][i+1] - cs[2][i];
          float b0 = IALPHA * (cs[0][i] - ds[0][i]);
          float b1 = IALPHA * (cs[1][i] - ds[1][i]);
          float b2 = IALPHA * (cs[2][i] - ds[2][i]);
          float aa = (a0 * a0 + a1 * a1 + a2 * a2);
          float bb = (b0 * b0 + b1 * b1 + b2 * b2);
          float ab = (b0 * a0 + b1 * a1 + b2 * a2);
          // b = A projected onto B, as a signed fraction of B
          float b = ab / bb;
          // c = (norm(A projected onto B) / norm(A)) ^ 2
          float c = (ab * ab) / (aa * bb);
          test[i] = !(0.5f < b && b < 2.0f && 0.5f < c);
        } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
        last_i += stripVertexCounts[i_svc];
      } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)
    } // end TEST == 1
    cs = null;
    rs = null;

    float[] lastcoord = null;
    byte[] lastcol = null;
    VisADLineStripArray array = new VisADLineStripArray();
    // worst case splits every line
    float[] coords = new float[3 * coordinates.length];
    int color_length = 0;
    byte[] cols = null;
    if (colors != null) {
      color_length = 3;
      cols = new byte[3 * colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    // worst case makes as many strips as there were points
    int[] svcs = new int[coordinates.length];
    int svc_index = 0;
    last_i = 0; // start i for each vertex strip

    int[] km = {0, 0};
    j = 0;
    boolean any_split = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      int accum = 0; // strip counter
      j = color_length * last_i / 3;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3) {
        // first, add point at "i"
        float coord[] =
          new float[] {coordinates[i], coordinates[i+1], coordinates[i+2]};
        byte[] col = null;
        if (color_length == 3) {
          col = new byte[] {colors[j], colors[j+1], colors[j+2]};
        }
        else if (color_length == 4) {
          col = new byte[] {colors[j], colors[j+1], colors[j+2], colors[j+3]};
        }
        accum++;
        if (accum == 1) {
          lastcoord = coord;
          lastcol = col;
        }
        else {
          nextPoint(accum, color_length, coords, cols, coord, col,
                    lastcoord, lastcol, km);
        }
        if (i == last_i+stripVertexCounts[i_svc]*3-3) continue; // last point
        if (test[i/3]) {
          any_split = true;
          // treat split as a break
          if (accum >= 2) {
            svcs[svc_index] = accum;
            svc_index++;
          }
          accum = 0; // reset strip counter;
        } // end if split
        j += color_length;
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
      if (accum >= 2) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc]*3;
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_split) {
      return this;
    }
    else {
      array.vertexCount = km[0] / 3;
      array.coordinates = new float[km[0]];
      System.arraycopy(coords, 0, array.coordinates, 0, km[0]);
      if (colors != null) {
        array.colors = new byte[km[1]];
        System.arraycopy(cols, 0, array.colors, 0, km[1]);
      }
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }
  }

  public VisADGeometryArray adjustLongitude(DataRenderer renderer)
         throws VisADException {
    float[] lons = getLongitudes(renderer);
    if (lons == null) return this;
    int[] axis = new int[1];
    float[] lon_coords = new float[2];
    float[] lon_range = getLongitudeRange(lons, axis, lon_coords);
    if (lon_range[0] != lon_range[0] ||
        lon_range[1] != lon_range[1]) return this;
    float bottom = lon_range[0];
    float top = lon_range[1];
    float low = bottom + 30.0f;
    float hi = top - 30.0f;
    int lon_axis = axis[0];
    float coord_bottom = lon_coords[0];
    float coord_top = lon_coords[1];
    float[] lastcoord = null;
    byte[] lastcol = null;

    VisADLineStripArray array = new VisADLineStripArray();
    // worst case splits every line
    float[] coords = new float[3 * coordinates.length];
    int color_length = 0;
    byte[] cols = null;
    if (colors != null) {
      color_length = 3;
      cols = new byte[3 * colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    // worst case makes as many strips as there were points
    int[] svcs = new int[coordinates.length];
    int svc_index = 0;
    int last_i = 0; // start i for each vertex strip

    int[] km = {0, 0};
    int j = 0;
    boolean any_split = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      int accum = 0; // strip counter
      j = color_length * last_i / 3;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3) {
        // first, add point at "i"
        float coord[] =
          new float[] {coordinates[i], coordinates[i+1], coordinates[i+2]};
        byte[] col = null;
        if (color_length == 3) {
          col = new byte[] {colors[j], colors[j+1], colors[j+2]};
        }
        else if (color_length == 4) {
          col = new byte[] {colors[j], colors[j+1], colors[j+2], colors[j+3]};
        }
        accum++;
        if (accum == 1) {
          lastcoord = coord;
          lastcol = col;
        }
        else {
          nextPoint(accum, color_length, coords, cols, coord, col,
                    lastcoord, lastcol, km);
        }
        if (i == last_i+stripVertexCounts[i_svc]*3-3) continue; // last point
        int i3 = i / 3;
        if ((lons[i3] < low && hi < lons[i3 + 1]) ||
            (lons[i3 + 1] < low && hi < lons[i3])) {
          any_split = true;
          if (lon_axis < 0) {
            // not enough info to interpolate, so treat split as a break
            if (accum >= 2) {
              svcs[svc_index] = accum;
              svc_index++;
            }
            accum = 0; // reset strip counter;
          }
          else { // lon_axis >= 0
            // split line by interpolation
            float a, b;
            float coord_first, coord_second;
            if (lons[i3] < low) {
              a = lons[i3] - bottom;
              b = top - lons[i3 + 1];
              coord_first = coord_bottom;
              coord_second = coord_top;
            }
            else {
              a = top - lons[i3];
              b = lons[i3 + 1] - bottom;
              coord_first = coord_top;
              coord_second = coord_bottom;
            }
            float alpha = b / (a + b);
            alpha = (alpha != alpha || alpha < 0.0f) ? 0.0f :
                      ((1.0f < alpha) ? 1.0f : alpha);
            float beta = 1.0f - alpha;
            // create first point of split;
            coord = new float[]
              {alpha * coordinates[i] + beta * coordinates[i+3],
               alpha * coordinates[i+1] + beta * coordinates[i+4],
               alpha * coordinates[i+2] + beta * coordinates[i+5]};
            coord[lon_axis] = coord_first;
            col = null;
            if (color_length == 3) {
              col = new byte[]
                {ShadowType.floatToByte(
                  alpha * ShadowType.byteToFloat(colors[j]) +
                  beta * ShadowType.byteToFloat(colors[j+3])),
                 ShadowType.floatToByte(
                  alpha * ShadowType.byteToFloat(colors[j+1]) +
                  beta * ShadowType.byteToFloat(colors[j+4])),
                 ShadowType.floatToByte(
                  alpha * ShadowType.byteToFloat(colors[j+2]) +
                  beta * ShadowType.byteToFloat(colors[j+5]))};
            }
            else if (color_length == 4) {
              col = new byte[]
                {ShadowType.floatToByte(
                  alpha * ShadowType.byteToFloat(colors[j]) +
                  beta * ShadowType.byteToFloat(colors[j+4])),
                 ShadowType.floatToByte(
                  alpha * ShadowType.byteToFloat(colors[j+1]) +
                  beta * ShadowType.byteToFloat(colors[j+5])),
                 ShadowType.floatToByte(
                  alpha * ShadowType.byteToFloat(colors[j+2]) +
                  beta * ShadowType.byteToFloat(colors[j+6])),
                 ShadowType.floatToByte(
                  alpha * ShadowType.byteToFloat(colors[j+3]) +
                  beta * ShadowType.byteToFloat(colors[j+7]))};
            }
            accum++;
            if (accum == 1) {
              lastcoord = coord;
              lastcol = col;
            }
            else {
              nextPoint(accum, color_length, coords, cols, coord, col,
                        lastcoord, lastcol, km);
            }
            // break strip between first and second points
            if (accum >= 2) {
              svcs[svc_index] = accum;
              svc_index++;
            }
            accum = 0; // reset strip counter;

            // create second point of split, identical to first except:
            coord[lon_axis] = coord_second;
            accum++;
            if (accum == 1) {
              lastcoord = coord;
              lastcol = col;
            }
            else {
              nextPoint(accum, color_length, coords, cols, coord, col,
                        lastcoord, lastcol, km);
            }

          } // end if (lon_axis >= 0)
        } // end if split
        j += color_length;
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
      if (accum >= 2) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc]*3;
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_split) {
      return this;
    }
    else {
      array.vertexCount = km[0] / 3;
      array.coordinates = new float[km[0]];
      System.arraycopy(coords, 0, array.coordinates, 0, km[0]);
      if (colors != null) {
        array.colors = new byte[km[1]];
        System.arraycopy(cols, 0, array.colors, 0, km[1]);
      }
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }

  }

  private void nextPoint(int accum, int color_length, float[] coords,
                         byte[] cols, float[] coord, byte[] col,
                         float[] lastcoord, byte[] lastcol, int[] km) {
    if (accum == 2) {
      coords[km[0]] = lastcoord[0];
      coords[km[0]+1] = lastcoord[1];
      coords[km[0]+2] = lastcoord[2];
      km[0] += 3;
      if (colors != null) {
        cols[km[1]] = lastcol[0];
        cols[km[1]+1] = lastcol[1];
        cols[km[1]+2] = lastcol[2];
        km[1] += 3;
        if (color_length == 4) {
          cols[km[1]++] = lastcol[3];
        }
      }
    } // end if (accum == 2)
    coords[km[0]] = coord[0];
    coords[km[0]+1] = coord[1];
    coords[km[0]+2] = coord[2];
    km[0] += 3;
    if (colors != null) {
      cols[km[1]] = col[0];
      cols[km[1]+1] = col[1];
      cols[km[1]+2] = col[2];
      km[1] += 3;
      if (color_length == 4) {
        cols[km[1]++] = col[3];
      }
    }
  }

  public VisADGeometryArray removeMissing() {
    VisADLineStripArray array = new VisADLineStripArray();
    float[] coords = new float[coordinates.length];
    int color_length = 3;
    byte[] cols = null;
    if (colors != null) {
      cols = new byte[colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    int[] svcs = new int[coordinates.length / 4];
    int svc_index = 0;
    int last_i = 0; // start i for each vertex strip

    int k = 0;
    int m = 0;
    int j = 0;
    boolean any_missing = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      int accum = 0; // strip counter
      j = color_length * last_i / 3;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3) {

        if (coordinates[i] == coordinates[i] &&
            coordinates[i+1] == coordinates[i+1] &&
            coordinates[i+2] == coordinates[i+2]) {
          accum++;
          if (accum >= 2) {
            int iml = i;
            int jml = j;
            if (accum == 2) {
              iml = i - 3;
              jml = j - color_length;
            }
            int jm = jml;
            for (int im=iml; im<=i; im+=3) {
              coords[k] = coordinates[im];
              coords[k+1] = coordinates[im+1];
              coords[k+2] = coordinates[im+2];
              if (colors != null) {
                cols[m] = colors[jm];
                cols[m+1] = colors[jm+1];
                cols[m+2] = colors[jm+2];
                m += 3;
                if (color_length == 4) {
                  cols[m++] = colors[jm+3];
                }
              }
              k += 3;
              jm += color_length;
            } // end for (im=iml; im<=i; im+=3)
          } // end if (accum >= 2)
        }
        else { // missing coordinates values
          any_missing = true;
          if (accum >= 2) {
            svcs[svc_index] = accum;
            svc_index++;
          }
          accum = 0; // reset strip counter;
        }
        j += color_length;
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
      if (accum >= 2) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc]*3;
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_missing) {
      return this;
    }
    else {
      array.vertexCount = k / 3;
      array.coordinates = new float[k];
      System.arraycopy(coords, 0, array.coordinates, 0, k);
      if (colors != null) {
        array.colors = new byte[m];
        System.arraycopy(cols, 0, array.colors, 0, m);
      }
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }
  }

  public Object clone() {
    VisADLineStripArray array = new VisADLineStripArray();
    copy(array);
    if (stripVertexCounts != null) {
      array.stripVertexCounts = new int[stripVertexCounts.length];
      System.arraycopy(stripVertexCounts, 0, array.stripVertexCounts, 0,
                       stripVertexCounts.length);
    }
    return array;
  }

}

