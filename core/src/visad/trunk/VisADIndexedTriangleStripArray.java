 
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

  public GeometryArray makeGeometry() throws VisADException {
    if (vertexCount == 0) return null;
    IndexedTriangleStripArray array = 
      new IndexedTriangleStripArray(vertexCount, VertexFormat, indexCount,
                                    stripVertexCounts);
    basicGeometry(array);
    if (coordinates != null) array.setCoordinateIndices(0, indices);
    if (colors != null) array.setColorIndices(0, indices);
    if (normals != null) array.setNormalIndices(0, indices);
    if (texCoords != null) array.setTextureCoordinateIndices(0, indices);
    return array;
  }

}

