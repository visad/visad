 
//
// VisADIndexedTriangleStripArray.java
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
   VisADIndexedTriangleStripArray stands in for
   j3d.IndexedTriangleStripArray and is Serializable.<P>
*/
public class VisADIndexedTriangleStripArray extends VisADGeometryArray {
  public int indexCount;
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
        if (i > 0) start[i] = start[i-1] + arrays[i].vertexCount;

      }
    }
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
      } // end for (int i=last_i; i<last_i+stripVertexCounts[i_svc]*3; i+=3)
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

