 
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
    for (int i=0; i<n; i++) {
      if (arrays[i] != null) {
        count += arrays[i].indexCount;
        nind += arrays[i].indices.length;
        nstrips += arrays[i].stripVertexCounts.length;
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
          indices[nind + j] = arrays[i].indices[j];
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

}

