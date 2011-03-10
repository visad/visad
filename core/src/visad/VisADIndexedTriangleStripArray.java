//
// VisADIndexedTriangleStripArray.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
   VisADIndexedTriangleStripArray stands in for
   j3d.IndexedTriangleStripArray and is Serializable.<P>
*/
public class VisADIndexedTriangleStripArray extends VisADGeometryArray {
  public int indexCount; // should = indices.length
  public int[] indices;
  public int[] stripVertexCounts;

  public static VisADIndexedTriangleStripArray
                merge(VisADIndexedTriangleStripArray[] arrays)
         throws VisADException {
    if (arrays == null || arrays.length == 0) return null;
    VisADIndexedTriangleStripArray array =
      new VisADIndexedTriangleStripArray();
    merge(arrays, array);
    int count = 0;
    int nind = 0;
    int nstrips = 0;
    int n = arrays.length;

    // WLH 1 May 99
    int[] start = new int[n];

    start[0] = 0;
    for (int i=0; i<n; i++) {
      if (arrays[i] != null) {
        count += arrays[i].indexCount;
        nind += arrays[i].indices.length;
        nstrips += arrays[i].stripVertexCounts.length;

        // WLH 1 May 99
        if (i > 0) start[i] = start[i-1] + arrays[i-1].vertexCount;

      }
    }
    if (nstrips <= 0) return null;
    int[] indices = new int[nind];
    int[] stripVertexCounts = new int[nstrips];
    nind = 0;
    nstrips = 0;
    for (int i=0; i<n; i++) {
      if (arrays[i] != null) {
        int incind = arrays[i].indices.length;
        int incnstrips = arrays[i].stripVertexCounts.length;
        for (int j=0; j<incind; j++) {

          // WLH 1 May 99
          // indices[nind + j] = arrays[i].indices[j];
          indices[nind + j] = arrays[i].indices[j] + start[i];

        }
        for (int j=0; j<incnstrips; j++) {
          stripVertexCounts[nstrips + j] = arrays[i].stripVertexCounts[j];
        }
        nind += incind;
        nstrips += incnstrips;
      }
    }
    array.indexCount = count;
    array.indices = indices;
    array.stripVertexCounts = stripVertexCounts;
    return array;
  }

  private static final int MUL = 6;

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

    // midpoint from this (if this_split)
    int mid_first = -1;
    int mid_second = -1;

    // save midpoint from last (if last_split)
    int last_mid_first = -1;
    int last_mid_second = -1;

    // midpoint between -3 and +3 (if this_split^last_split)
    int sillymid_first = -1;
    int sillymid_second = -1;

    // most recent point
    int last = -1;

    // point before most recent
    int early = -1;

    // this point
    int point = -1;


    VisADIndexedTriangleStripArray array = new VisADIndexedTriangleStripArray();
    // worst case makes 3 times as many triangles
    float[] coords = new float[MUL * coordinates.length];
    System.arraycopy(coordinates, 0, coords, 0, coordinates.length);
    float[] nos = null;
    if (normals != null) {
      nos = new float[MUL * normals.length];
      System.arraycopy(normals, 0, nos, 0, normals.length);
    }
    int color_length = 0;
    byte[] cols = null;
    if (colors != null) {
      color_length = 3;
      cols = new byte[MUL * colors.length];
      if (colors.length != coordinates.length) color_length = 4;
      System.arraycopy(colors, 0, cols, 0, colors.length);
    }
    float[] texs = null;
    if (texCoords != null) {
      texs = new float[MUL * texCoords.length];
      System.arraycopy(texCoords, 0, texs, 0, texCoords.length);
    }
    int coord_index = coordinates.length / 3; // index to add next point
    // worst case makes 3 times as many indices
    int[] inds = new int[MUL * indices.length];
    int ind_index = 0; // index to add next indices entry
    // worst case makes as many strips as there were points
    int[] svcs = new int[coordinates.length];
    int svc_index = 0;
    int last_i = 0; // start i for each vertex strip

    // int m = 0;  replaced by: ind_index
    boolean any_split = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      boolean this_split = false;
      int accum = 0; // strip counter
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]-1; i++) {
        boolean last_split = this_split; // true if last edge was split
        if (last_split) {
          last_mid_first = mid_first;
          last_mid_second = mid_second;
        }
        if ((lons[indices[i]] < low && hi < lons[indices[i+1]]) ||
            (lons[indices[i+1]] < low && hi < lons[indices[i]])) {
          this_split = true;
          any_split = true;
          if (lon_axis < 0) {
            // not enough info to interpolate, so treat split as a break
            if (accum >= 3) {
              svcs[svc_index] = accum;
              svc_index++;
            }
            last = indices[i+1];
            accum = 1; // reset strip counter;
            continue;
          }
          // lon_axis >= 0
          // split line by interpolation
          float a, b;
          float coord_first, coord_second;
          if (lons[indices[i]] < low) {
            a = lons[indices[i]] - bottom;
            b = top - lons[indices[i+1]];
            coord_first = coord_bottom;
            coord_second = coord_top;
          }
          else {
            a = top - lons[indices[i]];
            b = lons[indices[i+1]] - bottom;
            coord_first = coord_top;
            coord_second = coord_bottom;
          }
          float alpha = b / (a + b);
          alpha = (alpha != alpha || alpha < 0.0f) ? 0.0f :
                    ((1.0f < alpha) ? 1.0f : alpha);
          float beta = 1.0f - alpha;

          mid_first = coord_index++;
          mid_second = coord_index++;
          int f3 = 3 * mid_first;
          int s3 = 3 * mid_second;
          int i3 = 3 * indices[i];
          int ip3 = 3 * indices[i+1];
          coords[f3] = alpha * coordinates[i3] + beta * coordinates[ip3];
          coords[f3+1] = alpha * coordinates[i3+1] + beta * coordinates[ip3+1];
          coords[f3+2] = alpha * coordinates[i3+2] + beta * coordinates[ip3+2];
          coords[s3] = coords[f3];
          coords[s3+1] = coords[f3+1];
          coords[s3+2] = coords[f3+2];
          coords[f3+lon_axis] = coord_first;
          coords[s3+lon_axis] = coord_second;
          if (normals != null) {
            nos[f3] = alpha * normals[i3] + beta * normals[ip3];
            nos[f3+1] = alpha * normals[i3+1] + beta * normals[ip3+1];
            nos[f3+2] = alpha * normals[i3+2] + beta * normals[ip3+2];
            nos[s3] = nos[f3];
            nos[s3+1] = nos[f3+1];
            nos[s3+2] = nos[f3+2];
          }
          if (color_length == 3) {
            cols[f3] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[i3]) +
              beta * ShadowType.byteToFloat(colors[ip3]));
            cols[f3+1] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[i3+1]) +
              beta * ShadowType.byteToFloat(colors[ip3+1]));
            cols[f3+2] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[i3+2]) +
              beta * ShadowType.byteToFloat(colors[ip3+2]));
            cols[s3] = cols[f3];
            cols[s3+1] = cols[f3+1];
            cols[s3+2] = cols[f3+2];
          }
          else if (color_length == 4) {
            int f4 = 4 * mid_first;
            int s4 = 4 * mid_second;
            int i4 = 4 * indices[i];
            int ip4 = 4 * indices[i+1];
            cols[f4] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[i4]) +
              beta * ShadowType.byteToFloat(colors[ip4]));
            cols[f4+1] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[i4+1]) +
              beta * ShadowType.byteToFloat(colors[ip4+1]));
            cols[f4+2] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[i4+2]) +
              beta * ShadowType.byteToFloat(colors[ip4+2]));
            cols[f4+3] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[i4+3]) +
              beta * ShadowType.byteToFloat(colors[ip4+3]));
            cols[s4] = cols[f4];
            cols[s4+1] = cols[f4+1];
            cols[s4+2] = cols[f4+2];
            cols[s4+3] = cols[f4+3];
          }
          if (texCoords != null) {
            int f2 = 2 * mid_first;
            int s2 = 2 * mid_second;
            int i2 = 2 * indices[i];
            int ip2 = 2 * indices[i+1];
            texs[f2] = alpha * texCoords[i2] + beta * texCoords[ip2];
            texs[f2+1] = alpha * texCoords[i2+1] + beta * texCoords[ip2+1];
            texs[s2] = cols[f2];
            texs[s2+1] = cols[f2+1];
          }
        }
        else { // no split
          this_split = false;
        }

        if (accum > 0 && this_split != last_split && lon_axis >= 0) {
          // need to compute mid edge from -3 to +3

          // split line by interpolation
          float a, b;
          float coord_first, coord_second;
          if (lons[indices[i-1]] < low) {
            a = lons[indices[i-1]] - bottom;
            b = top - lons[indices[i+1]];
            coord_first = coord_bottom;
            coord_second = coord_top;
          }
          else {
            a = top - lons[indices[i-1]];
            b = lons[indices[i+1]] - bottom;
            coord_first = coord_top;
            coord_second = coord_bottom;
          }
          float alpha = b / (a + b);
          alpha = (alpha != alpha || alpha < 0.0f) ? 0.0f :
                    ((1.0f < alpha) ? 1.0f : alpha);
          float beta = 1.0f - alpha;

          sillymid_first = coord_index++;
          sillymid_second = coord_index++;
          int f3 = 3 * sillymid_first;
          int s3 = 3 * sillymid_second;
          int im3 = 3 * indices[i-1];
          int ip3 = 3 * indices[i+1];
          coords[f3] = alpha * coordinates[im3] + beta * coordinates[ip3];
          coords[f3+1] = alpha * coordinates[im3+1] + beta * coordinates[ip3+1];
          coords[f3+2] = alpha * coordinates[im3+2] + beta * coordinates[ip3+2];
          coords[s3] = coords[f3];
          coords[s3+1] = coords[f3+1];
          coords[s3+2] = coords[f3+2];
          coords[f3+lon_axis] = coord_first;
          coords[s3+lon_axis] = coord_second;
          if (normals != null) {
            nos[f3] = alpha * normals[im3] + beta * normals[ip3];
            nos[f3+1] = alpha * normals[im3+1] + beta * normals[ip3+1];
            nos[f3+2] = alpha * normals[im3+2] + beta * normals[ip3+2];
            nos[s3] = nos[f3];
            nos[s3+1] = nos[f3+1];
            nos[s3+2] = nos[f3+2];
          }
          if (color_length == 3) {
            cols[f3] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[im3]) +
              beta * ShadowType.byteToFloat(colors[ip3]));
            cols[f3+1] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[im3+1]) +
              beta * ShadowType.byteToFloat(colors[ip3+1]));
            cols[f3+2] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[im3+2]) +
              beta * ShadowType.byteToFloat(colors[ip3+2]));
            cols[s3] = cols[f3];
            cols[s3+1] = cols[f3+1];
            cols[s3+2] = cols[f3+2];
          }
          else if (color_length == 4) {
            int f4 = 4 * mid_first;
            int s4 = 4 * mid_second;
            int im4 = 4 * indices[i-1];
            int ip4 = 4 * indices[i+1];
            cols[f4] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[im4]) +
              beta * ShadowType.byteToFloat(colors[ip4]));
            cols[f4+1] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[im4+1]) +
              beta * ShadowType.byteToFloat(colors[ip4+1]));
            cols[f4+2] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[im4+2]) +
              beta * ShadowType.byteToFloat(colors[ip4+2]));
            cols[f4+3] = ShadowType.floatToByte(
              alpha * ShadowType.byteToFloat(colors[im4+3]) +
              beta * ShadowType.byteToFloat(colors[ip4+3]));
            cols[s4] = cols[f4];
            cols[s4+1] = cols[f4+1];
            cols[s4+2] = cols[f4+2];
            cols[s4+3] = cols[f4+3];
          }
          if (texCoords != null) {
            int f2 = 2 * mid_first;
            int s2 = 2 * mid_second;
            int im2 = 2 * indices[i-1];
            int ip2 = 2 * indices[i+1];
            texs[f2] = alpha * texCoords[im2] + beta * texCoords[ip2];
            texs[f2+1] = alpha * texCoords[im2+1] + beta * texCoords[ip2+1];
            texs[s2] = cols[f2];
            texs[s2+1] = cols[f2+1];
          }
        }

        if (this_split) {
          if (accum == 0) {
            early = mid_second;
            last = indices[i+1];
            accum = 2;
            continue; // don't make any triangles yet, for accum = 0
          } // end if (accum == 0)
          else if (last_split) { // && this_split
            point = mid_first;
            accum++;
            if (accum == 3) {
              inds[ind_index++] = early;
              inds[ind_index++] = last;
              inds[ind_index++] = point;
            }
            else {
              inds[ind_index++] = point;
            }
            // bind off
            svcs[svc_index] = accum;
            svc_index++;
            // accum = 0, but more to come

            // create a 2-triangle strip
            //   (last_mid_first, i-3, mid_second, i+3)
            early = last_mid_first;
            last = indices[i-1];
            point = mid_second;
            accum = 3;
            inds[ind_index++] = early;
            inds[ind_index++] = last;
            inds[ind_index++] = point;

            point = indices[i+1];
            accum++;
            inds[ind_index++] = point;
          }
          else { // !last_split && accum > 0 && this_split
            // add (mid_first, i-3, sillymid_first)
            point = mid_first;
            accum++;
            if (accum == 2) {
              early = last;
              last = point;
            }
            else if (accum == 3) {
              inds[ind_index++] = early;
              inds[ind_index++] = last;
              inds[ind_index++] = point;
            }
            else {
              inds[ind_index++] = point;
            }

            point = indices[i-1];
            accum++;
            if (accum == 2) {
              early = last;
              last = point;
            }
            else if (accum == 3) {
              inds[ind_index++] = early;
              inds[ind_index++] = last;
              inds[ind_index++] = point;
            }
            else {
              inds[ind_index++] = point;
            }

            point = sillymid_first;
            accum++;
            if (accum == 2) {
              early = last;
              last = point;
            }
            else if (accum == 3) {
              inds[ind_index++] = early;
              inds[ind_index++] = last;
              inds[ind_index++] = point;
            }
            else {
              inds[ind_index++] = point;
            }

            // (accum >= 3)
            svcs[svc_index] = accum;
            svc_index++;
            // accum = 0, but more to come

            // start a triangle strip
            //   (sillymid_second, mid_second, i+3)
            early = sillymid_second;
            last = mid_second;
            point = indices[i+1];
            accum = 3;
            inds[ind_index++] = early;
            inds[ind_index++] = last;
            inds[ind_index++] = point;
          }
        }
        else { // !this_split
          if (accum == 0) {
            early = indices[i];
            last = indices[i+1];
            accum = 2;
            continue; // don't make any triangles yet, for accum = 0
          } // end if (accum == 0)
          else if (last_split && lon_axis >= 0) { // && !this_split
            // first, bind off
            if (accum >= 3) {
              svcs[svc_index] = accum;
              svc_index++;
            }
            // accum = 0, but more to come

            early = indices[i-1];
            last = last_mid_first;
            point = sillymid_first;
            accum = 3;
            inds[ind_index++] = early;
            inds[ind_index++] = last;
            inds[ind_index++] = point;
            // bind off again
            svcs[svc_index] = accum;
            svc_index++;
            // accum = 0, but more to come

            // create a 2-triangle strip
            //   (last_mid_second, sillymid_second, i, i+3)
            early = last_mid_second;
            last = sillymid_second;
            point = indices[i];
            accum = 3;
            inds[ind_index++] = early;
            inds[ind_index++] = last;
            inds[ind_index++] = point;

            point = indices[i+1];
            accum++;
            inds[ind_index++] = point;
          }
          else { // (!last_split || lon_axis < 0) && accum > 0 && !this_split
            // just add the next point (i+3)
            point = indices[i+1];
            accum++;
            if (accum == 2) {
              early = last;
              last = point;
            }
            else if (accum == 3) {
              inds[ind_index++] = early;
              inds[ind_index++] = last;
              inds[ind_index++] = point;
            }
            else {
              inds[ind_index++] = point;
            }
          }
        } // end if no split
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]-1; i++)
      if (accum >= 3) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc];
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_split) {
      return this;
    }
    else {
      array.coordinates = new float[3 * coord_index];
      System.arraycopy(coords, 0, array.coordinates, 0, 3 * coord_index);
      if (normals != null) {
        array.normals = new float[3 * coord_index];
        System.arraycopy(nos, 0, array.normals, 0, 3 * coord_index);
      }
      if (colors != null) {
        array.colors = new byte[color_length * coord_index];
        System.arraycopy(cols, 0, array.colors, 0, color_length * coord_index);
      }
      if (texCoords != null) {
        array.texCoords = new float[2 * coord_index];
        System.arraycopy(texs, 0, array.texCoords, 0, 2 * coord_index);
      }
      array.vertexCount = coord_index;
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      array.indices = new int[ind_index];
      System.arraycopy(inds, 0, array.indices, 0, ind_index);
      return array;
    }
  }

  public VisADGeometryArray removeMissing() {
    VisADIndexedTriangleStripArray array =
      new VisADIndexedTriangleStripArray();
    float[] coords = new float[coordinates.length];
    float[] nos = null;
    if (normals != null) {
      nos = new float[normals.length];
    }
    int color_length = 3;
    byte[] cols = null;
    if (colors != null) {
      cols = new byte[colors.length];
      if (colors.length != coordinates.length) color_length = 4;
    }
    float[] texs = null;
    if (texCoords != null) {
      texs = new float[texCoords.length];
    }
    int[] compress = new int[indices.length];
    int j = 0;
    int k = 0;
    for (int i=0; i<coordinates.length; i+=3) {
      if (coordinates[i] == coordinates[i] &&
          coordinates[i+1] == coordinates[i+1] &&
          coordinates[i+2] == coordinates[i+2]) {
        compress[j] = k;
        int k3 = 3 * k;
        coords[k3] = coordinates[i];
        coords[k3+1] = coordinates[i+1];
        coords[k3+2] = coordinates[i+2];
        if (normals != null) {
          nos[k3] = normals[i];
          nos[k3+1] = normals[i+1];
          nos[k3+2] = normals[i+2];
        }
        if (colors != null) {
          int kc = color_length * k;
          int ic = color_length * j;
          cols[kc] = colors[ic];
          cols[kc+1] = colors[ic+1];
          cols[kc+2] = colors[ic+2];
          if (color_length == 4) {
            cols[kc+3] = colors[ic+3];
          }
        }
        if (texCoords != null) {
          int kt = 2 * k;
          int it = 2 * j;
          texs[kt] = texCoords[it];
          texs[kt+1] = texCoords[it+1];
        }
        k++;
      }
      else { // missing coordinates
        compress[j] = -1;
      }
      j++;
    } // end for (int i=0; i<coordinates.length; i+=3)
    array.coordinates = new float[3 * k];
    System.arraycopy(coords, 0, array.coordinates, 0, 3 * k);
    if (normals != null) {
      array.normals = new float[3 * k];
      System.arraycopy(nos, 0, array.normals, 0, 3 * k);
    }
    if (colors != null) {
      array.colors = new byte[color_length * k];
      System.arraycopy(cols, 0, array.colors, 0, color_length * k);
    }
    if (texCoords != null) {
      array.texCoords = new float[2 * k];
      System.arraycopy(texs, 0, array.texCoords, 0, 2 * k);
    }
    array.vertexCount = k;

    int[] new_indices = new int[indices.length];
    int[] svcs = new int[coordinates.length / 4];
    int svc_index = 0;
    int last_i = 0; // start i for each vertex strip

    int m = 0;
    boolean any_missing = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      int accum = 0;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]; i++) {

        if (compress[indices[i]] >= 0) {
          accum++;
          if (accum >= 3) {
            int iml = i;
            if (accum == 3) {
              iml = i - 2;
            }
            for (int im=iml; im<=i; im++) {
              new_indices[m] = compress[indices[im]];
              m++;
            } // end for (im=iml; im<=i; im+=3)
          } // end if (accum >= 3)
        }
        else { // missing coordinates values
          any_missing = true;
          if (accum >= 3) {
            svcs[svc_index] = accum;
            svc_index++;
          }
          accum = 0; // reset strip counter;
        }
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]; i++)
      if (accum >= 3) {
        svcs[svc_index] = accum;
        svc_index++;
      }
      last_i += stripVertexCounts[i_svc];
    } // end for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++)

    if (!any_missing) {
      return this;
    }
    else {
      array.indices = new int[m];
      System.arraycopy(new_indices, 0, array.indices, 0, m);
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }
  }

  public String toString() {
/*
    String string = "VisADIndexedTriangleStripArray\n" + super.toString() +
                    "\n indexCount = " + indexCount;
*/
    String string = "VisADIndexedTriangleStripArray, indexCount = " + indexCount;
    string = string + "\n stripVertexCounts = ";
    for (int i=0; i<stripVertexCounts.length; i++) {
      string = string + stripVertexCounts[i] + " ";
    }
    string = string + "\n indices = ";
    for (int i=0; i<indices.length; i++) {
      string = string + indices[i] + " ";
    }
    return string;
  }

  public Object clone() {
    VisADIndexedTriangleStripArray array =
      new VisADIndexedTriangleStripArray();
    copy(array);
    array.indexCount = indexCount;
    if (stripVertexCounts != null) {
      array.stripVertexCounts = new int[stripVertexCounts.length];
      System.arraycopy(stripVertexCounts, 0, array.stripVertexCounts, 0,
                       stripVertexCounts.length);
    }
    if (indices != null) {
      array.indices = new int[indices.length];
      System.arraycopy(indices, 0, array.indices, 0, indices.length);
    }
    return array;
  }

}

