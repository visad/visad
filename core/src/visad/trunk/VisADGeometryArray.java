 
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
 
import javax.media.j3d.*;
import java.vecmath.*;

/**
   VisADGeometryArray stands in for j3d.GeometryArray
   and is Serializable.<P>
*/
public class VisADGeometryArray extends Object
       implements java.io.Serializable {
  int vertexCount;
  int VertexFormat;
  float[] coordinates;
  float[] normals;
  float[] colors; // should this be bytes ????
  float[] texCoords;

  public VisADGeometryArray() {
    vertexCount = 0;
    VertexFormat = 0;
    coordinates = null;
    normals = null;
    colors = null;
    texCoords = null;
  }

  public GeometryArray makeGeometry() throws VisADException {
    throw new DisplayException("VisADGeometryArray.makeGeometry");
  }

  public void basicGeometry(GeometryArray array) {
    if (coordinates != null) array.setCoordinates(0, coordinates);
    if (colors != null) array.setColors(0, colors);
    if (normals != null) array.setNormals(0, normals);
    if (texCoords != null) array.setTextureCoordinates(0, texCoords);
  }

}

