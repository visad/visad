 
//
// VisADGeometryArray.java
//
 
/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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
public class VisADGeometryArray extends Object
       implements java.io.Serializable {
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

