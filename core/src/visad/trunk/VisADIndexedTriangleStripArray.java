 
//
// VisADIndexedTriangleStripArray.java
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
 
import javax.media.j3d.*;
import java.vecmath.*;

/**
   VisADIndexedTriangleStripArray stands in for
   j3d.IndexedTriangleStripArray and is Serializable.<P>
*/
public class VisADIndexedTriangleStripArray extends VisADGeometryArray {
  int indexCount;
  int[] indices;
  int[] stripVertexCounts;

/* this expands indices
  public GeometryArray makeGeometry() throws VisADException {
    if (vertexCount == 0) return null;
    //
    // expand coordinates, colors, normals and texCoords
    //
    int count = indices.length;
    int len = 3 * count;

    int sum = 0;
    for (int i=0; i<stripVertexCounts.length; i++) sum += stripVertexCounts[i];
    System.out.println("indexCount = " + indexCount + " sum = " + sum +
                       " count = " + count + " stripVertexCounts.length = " +
                       stripVertexCounts.length);
// indexCount = 1984 sum = 1984 count = 1984 stripVertexCounts.length = 31
    int[] strip_counts = new int[1];
    strip_counts[0] = count;
//    TriangleStripArray array =
//      new TriangleStripArray(count, vertexFormat, strip_counts);

    TriangleStripArray array =
      new TriangleStripArray(count, vertexFormat, stripVertexCounts);


    if (coordinates != null) {
      System.out.println("expand coordinates");
      float[] coords = new float[len];
      for (int k=0; k<count; k++) {
        int i = 3 * k;
        int j = 3 * indices[k];
        coords[i] = coordinates[j];
        coords[i + 1] = coordinates[j + 1];
        coords[i + 2] = coordinates[j + 2];
      }
      array.setCoordinates(0, coords);
    }
    if (colors != null) {
      System.out.println("expand colors");
      float[] cols = new float[len];
      for (int k=0; k<count; k++) {
        int i = 3 * k;
        int j = 3 * indices[k];
        cols[i] = colors[j];
        cols[i + 1] = colors[j + 1];
        cols[i + 2] = colors[j + 2];
      }
      array.setColors(0, cols);
    }
    if (normals != null) {
      System.out.println("expand normals");
      float[] norms = new float[len];
      for (int k=0; k<count; k++) {
        int i = 3 * k;
        int j = 3 * indices[k];
        norms[i] = normals[j];
        norms[i + 1] = normals[j + 1];
        norms[i + 2] = normals[j + 2];
      }
      array.setNormals(0, norms);
    }
    if (texCoords != null) {
      System.out.println("expand texCoords");
      float[] tex = new float[len];
      for (int k=0; k<count; k++) {
        int i = 3 * k;
        int j = 3 * indices[k];
        tex[i] = texCoords[j];
        tex[i + 1] = texCoords[j + 1];
        tex[i + 2] = texCoords[j + 2];
      }
      array.setTextureCoordinates(0, tex);
    }
    return array;
  }
*/

  public GeometryArray makeGeometry() throws VisADException {
    if (vertexCount == 0) return null;
    IndexedTriangleStripArray array = 
      new IndexedTriangleStripArray(vertexCount, vertexFormat, indexCount,
                                    stripVertexCounts);
    basicGeometry(array);
    if (coordinates != null) array.setCoordinateIndices(0, indices);
    if (colors != null) array.setColorIndices(0, indices);
    if (normals != null) array.setNormalIndices(0, indices);
    if (texCoords != null) array.setTextureCoordinateIndices(0, indices);
    return array;
  }

/** this draws the 'dots'
  public GeometryArray makeGeometry() throws VisADException {
    if (vertexCount == 0) return null;
    PointArray array = new PointArray(vertexCount, vertexFormat);
    basicGeometry(array);
    return array;
  }
*/

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

