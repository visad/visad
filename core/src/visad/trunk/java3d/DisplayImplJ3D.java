
//
// DisplayImplJ3D.java
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

package visad.java3d;

import visad.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.*;

import java.awt.*;

import javax.media.j3d.*;
import com.sun.j3d.utils.applet.MainFrame;
// import com.sun.j3d.utils.applet.AppletFrame;

/**
   DisplayImplJ3D is the VisAD class for displays that use
   Java 3D.  It is runnable.<P>

   DisplayImplJ3D is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DisplayImplJ3D extends DisplayImpl {

  /** legal values for api */
  public static final int JPANEL = 1;
  public static final int APPLETFRAME = 2;
  /** this is used for APPLETFRAME */
  private DisplayApplet applet = null;

  private ProjectionControlJ3D projection = null;
  private GraphicsModeControlJ3D mode = null;

  /** constructor with DefaultDisplayRendererJ3D */
  public DisplayImplJ3D(String name)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRendererJ3D(), JPANEL);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL);
  }

  /** constructor with DefaultDisplayRenderer */
  public DisplayImplJ3D(String name, int api)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRendererJ3D(), api);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImplJ3D(String name, DisplayRendererJ3D renderer, int api)
         throws VisADException, RemoteException {
    super(name, renderer);

    // a GraphicsModeControl always exists
    mode = new GraphicsModeControlJ3D(this);
    addControl(mode);
    // a ProjectionControl always exists
    projection = new ProjectionControlJ3D(this);
    addControl(projection);

    if (api == APPLETFRAME) {
      applet = new DisplayApplet(this);
      Component component = new MainFrame(applet, 256, 256);
      // Component component = new AppletFrame(applet, 256, 256);
      setComponent(component);
      // component.setTitle(name);
    }
    else if (api == JPANEL) {
      Component component = new DisplayPanel(this);
      setComponent(component);
    }
    else {
      throw new DisplayException("DisplayImplJ3D: bad graphicsApi");
    }
  }

  public ProjectionControl getProjectionControl() {
    return projection;
  }

  public GraphicsModeControl getGraphicsModeControl() {
    return mode;
  }

  public DisplayApplet getApplet() {
    return applet;
  }

  public GeometryArray makeGeometry(VisADGeometryArray vga)
         throws VisADException {
    if (vga == null) return null;
    if (vga instanceof VisADIndexedTriangleStripArray) {
      /* this is the 'normal' makeGeometry */
      VisADIndexedTriangleStripArray vgb = (VisADIndexedTriangleStripArray) vga;
      if (vga.vertexCount == 0) return null;
      IndexedTriangleStripArray array =
        new IndexedTriangleStripArray(vga.vertexCount, makeFormat(vga),
                                      vgb.indexCount, vgb.stripVertexCounts);
      basicGeometry(vga, array);
      if (vga.coordinates != null) {
        array.setCoordinateIndices(0, vgb.indices);
      }
      if (vga.colors != null) {
        array.setColorIndices(0, vgb.indices);
      }
      if (vga.normals != null) {
        array.setNormalIndices(0, vgb.indices);
      }
      if (vga.texCoords != null) {
        array.setTextureCoordinateIndices(0, vgb.indices);
      }
      return array;
  
/* this expands indices
      if (vga.vertexCount == 0) return null;
      //
      // expand vga.coordinates, vga.colors, vga.normals and vga.texCoords
      //
      int count = vga.indices.length;
      int len = 3 * count;
  
      int sum = 0;
      for (int i=0; i<vga.stripVertexCounts.length; i++) sum += vga.stripVertexCounts[i];
      System.out.println("vga.indexCount = " + vga.indexCount + " sum = " + sum +
                         " count = " + count + " vga.stripVertexCounts.length = " +
                         vga.stripVertexCounts.length);
      // int[] strip_counts = new int[1];
      // strip_counts[0] = count;
      // TriangleStripArray array =
      //   new TriangleStripArray(count, makeFormat(vga), strip_counts);
  
      TriangleStripArray array =
        new TriangleStripArray(count, makeFormat(vga), vga.stripVertexCounts);
  
      if (vga.coordinates != null) {
        System.out.println("expand vga.coordinates");
        float[] coords = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          coords[i] = vga.coordinates[j];
          coords[i + 1] = vga.coordinates[j + 1];
          coords[i + 2] = vga.coordinates[j + 2];
        }
        array.setCoordinates(0, coords);
      }
      if (vga.colors != null) {
        System.out.println("expand vga.colors");
        float[] cols = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          cols[i] = vga.colors[j];
          cols[i + 1] = vga.colors[j + 1];
          cols[i + 2] = vga.colors[j + 2];
        }
        array.setColors(0, cols);
      }
      if (vga.normals != null) {
        System.out.println("expand vga.normals");
        float[] norms = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          norms[i] = vga.normals[j];
          norms[i + 1] = vga.normals[j + 1];
          norms[i + 2] = vga.normals[j + 2];
        }
        array.setNormals(0, norms);
      }
      if (vga.texCoords != null) {
        System.out.println("expand vga.texCoords");
        float[] tex = new float[len];
        for (int k=0; k<count; k++) {
          int i = 3 * k;
          int j = 3 * vga.indices[k];
          tex[i] = vga.texCoords[j];
          tex[i + 1] = vga.texCoords[j + 1];
          tex[i + 2] = vga.texCoords[j + 2];
        }
        array.setTextureCoordinates(0, tex);
      }
      return array;
*/
  
/* this draws normal vectors
      if (vga.vertexCount == 0) return null;
      LineArray array = new LineArray(2 * vga.vertexCount, LineArray.COORDINATES);
      float[] new_coords = new float[6 * vga.vertexCount];
      int i = 0;
      int j = 0;
      for (int k=0; k<vga.vertexCount; k++) {
        new_coords[j] = vga.coordinates[i];
        new_coords[j+1] = vga.coordinates[i+1];
        new_coords[j+2] = vga.coordinates[i+2];
        j += 3;
        new_coords[j] = vga.coordinates[i] + 0.05f * vga.normals[i];
        new_coords[j+1] = vga.coordinates[i+1] + 0.05f * vga.normals[i+1];
        new_coords[j+2] = vga.coordinates[i+2] + 0.05f * vga.normals[i+2];
        i += 3;
        j += 3;
      }
      array.setCoordinates(0, new_coords);
      return array;
*/
  
/* this draws the 'dots'
      if (vga.vertexCount == 0) return null;
      PointArray array =
        new PointArray(vga.vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array);
      return array;
*/
    }
    else if (vga instanceof VisADLineArray) {
      if (vga.vertexCount == 0) return null;
      LineArray array = new LineArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array);
      return array;
    }
    else if (vga instanceof VisADLineStripArray) {
      if (vga.vertexCount == 0) return null;
      VisADLineStripArray vgb = (VisADLineStripArray) vga;
      LineStripArray array =
        new LineStripArray(vga.vertexCount, makeFormat(vga),
                           vgb.stripVertexCounts);
      basicGeometry(vga, array);
      return array;
    }
    else if (vga instanceof VisADPointArray) {
      if (vga.vertexCount == 0) return null;
      PointArray array = new PointArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array);
      return array;
    }
    else if (vga instanceof VisADTriangleArray) {
      if (vga.vertexCount == 0) return null;
      TriangleArray array = new TriangleArray(vga.vertexCount, makeFormat(vga));
      basicGeometry(vga, array);
      return array;
    }
    else {
      throw new DisplayException("DisplayImplJ3D.makeGeometry");
    }
  }

  private void basicGeometry(VisADGeometryArray vga,
                             GeometryArray array) {
    if (vga.coordinates != null) array.setCoordinates(0, vga.coordinates);
    if (vga.colors != null) array.setColors(0, vga.colors);
    if (vga.normals != null) array.setNormals(0, vga.normals);
    if (vga.texCoords != null) array.setTextureCoordinates(0, vga.texCoords);
  }

  private static int makeFormat(VisADGeometryArray vga) {
    int format = 0;
    if (vga.coordinates != null) format |= GeometryArray.COORDINATES;
    if (vga.colors != null) {
      if (vga.colors.length == 3 * vga.vertexCount) {
        format |= GeometryArray.COLOR_3;
      }
      else {
        format |= GeometryArray.COLOR_4;
      }
    }
    if (vga.normals != null) format |= GeometryArray.NORMALS;
    if (vga.texCoords != null) {
      if (vga.texCoords.length == 2 * vga.vertexCount) {
        format |= GeometryArray.TEXTURE_COORDINATE_2;
      }
      else {
        format |= GeometryArray.TEXTURE_COORDINATE_3;
      }
    }
    return format;
  }

}

