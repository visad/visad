 
//
// VisADGeometryArray.java
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
   VisADGeometryArray stands in for j3d.GeometryArray
   and is Serializable.<P>
*/
public class VisADGeometryArray extends VisADSceneGraphObject {

  public int vertexCount;
  public int vertexFormat;
  public float[] coordinates;
  public float[] normals;
  public float[] colors; // should this be bytes ????
  public float[] texCoords;

  public VisADGeometryArray() {
    vertexCount = 0;
    vertexFormat = 0;
    coordinates = null;
    normals = null;
    colors = null;
    texCoords = null;
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
      count += arrays[i].vertexCount;
      if (color_flag != (arrays[i].colors != null) ||
          normal_flag != (arrays[i].normals != null) ||
          texCoord_flag != (arrays[i].texCoords != null)) {
        throw new DisplayException("VisADGeometryArray.merge: formats don't match");
      }
    }
    float[] coordinates = new float[3 * count];
    float[] colors = null;
    float[] normals = null;
    float[] texCoords = null;
    if (color_flag) {
      colors = new float[3 * count];
    }
    if (normal_flag) {
      normals = new float[3 * count];
    }
    if (texCoord_flag) {
      texCoords = new float[3 * count];
    }
    int k = 0;
    int m = 0;
    for (int i=0; i<n; i++) {
      float[] c = arrays[i].coordinates;
      for (int j=0; j<3*arrays[i].vertexCount; j++) {
        coordinates[k++] = c[j];
      }
      if (color_flag) {
        c = arrays[i].colors;
        for (int j=0; j<3*arrays[i].vertexCount; j++) {
          colors[m++] = c[j];
        }
      }
      if (normal_flag) {
        c = arrays[i].normals;
        for (int j=0; j<3*arrays[i].vertexCount; j++) {
          normals[m++] = c[j];
        }
      }
      if (texCoord_flag) {
        c = arrays[i].texCoords;
        for (int j=0; j<3*arrays[i].vertexCount; j++) {
          texCoords[m++] = c[j];
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
      string = string + "\n colores = " + floatArrayString(colors);
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

}

