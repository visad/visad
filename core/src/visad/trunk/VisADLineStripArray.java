 
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
      int accum = 0;
      for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3) {
        j = color_length * last_i / 3;
 
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

