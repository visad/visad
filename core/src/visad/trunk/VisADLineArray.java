 
//
// VisADLineArray.java
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
   VisADLineArray stands in for j3d.LineArray
   and is Serializable.<P>
*/
public class VisADLineArray extends VisADGeometryArray {

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

  public Object clone() {
    VisADLineArray array = new VisADLineArray();
    copy(array);
    return array;
  }

}

