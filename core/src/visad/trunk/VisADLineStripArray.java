 
//
// VisADLineStripArray.java
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
 
/**
   VisADLineStripArray stands in for j3d.LineStripArray
   and is Serializable.<P>
*/
public class VisADLineStripArray extends VisADGeometryArray {
  public int[] stripVertexCounts;

  public static VisADLineStripArray
                merge(VisADLineStripArray[] arrays)
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



  public VisADGeometryArray adjustLongitude(DataRenderer renderer)
         throws VisADException {
    float[] lons = getLongitudes(renderer);
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
 
    int lon_k = 0;
    int lon_m = 0;
    int j = 0;
    boolean any_split = false;
    for (int i_svc=0; i_svc<stripVertexCounts.length; i_svc++) {
      int accum = 0; // strip counter
      j = color_length * last_i / 3;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3-3; i+=3) {
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
        nextPoint(accum, color_length, coords, cols, coord, col);
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
            nextPoint(accum, color_length, coords, cols, coord, col);
            // break strip between first and second points
            if (accum >= 2) {
              svcs[svc_index] = accum;
              svc_index++;
            }
            accum = 0; // reset strip counter;

            // create second point of split, identical to first except:
            coord[lon_axis] = coord_second;
            accum++;
            nextPoint(accum, color_length, coords, cols, coord, col);

          } // end if (lon_axis >= 0)
        } // end if split
        j += color_length;
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3-3; i+=3)
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
      array.vertexCount = lon_k / 3;
      array.coordinates = new float[lon_k];
      System.arraycopy(coords, 0, array.coordinates, 0, lon_k);
      if (colors != null) {
        array.colors = new byte[lon_m];
        System.arraycopy(cols, 0, array.colors, 0, lon_m);
      }
      array.stripVertexCounts = new int[svc_index];
      System.arraycopy(svcs, 0, array.stripVertexCounts, 0, svc_index);
      return array;
    }

  }

  static float[] lastcoord;
  static byte[] lastcol;
  static int lon_k = 0;
  static int lon_m = 0;

  private void nextPoint(int accum, int color_length, float[] coords,
                         byte[] cols, float[] coord, byte[] col) {
    if (accum == 1) {
      lastcoord = coord;
      lastcol = col;
    }
    else { // accum >= 2
      if (accum == 2) {
        coords[lon_k] = lastcoord[0];
        coords[lon_k+1] = lastcoord[1];
        coords[lon_k+2] = lastcoord[2];
        lon_k += 3;
        if (colors != null) {
          cols[lon_m] = lastcol[0];
          cols[lon_m+1] = lastcol[1];
          cols[lon_m+2] = lastcol[2];
          lon_m += 3;
          if (color_length == 4) {
            cols[lon_m++] = lastcol[3];
          }
        }
      }
      coords[lon_k] = coord[0];
      coords[lon_k+1] = coord[1];
      coords[lon_k+2] = coord[2];
      lon_k += 3;
      if (colors != null) {
        cols[lon_m] = col[0];
        cols[lon_m+1] = col[1];
        cols[lon_m+2] = col[2];
        lon_m += 3;
        if (color_length == 4) {
          cols[lon_m++] = col[3];
        }
      }
    } // end if (accum >= 2)
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

