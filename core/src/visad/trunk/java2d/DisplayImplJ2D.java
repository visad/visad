
//
// DisplayImplJ2D.java
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

/*

visad.java2d design:
 
0. common code in ClassNameJ2D and ClassNameJ3D
     DirectManipulationRendererJ2/3D
       common methods in visad.DataRenderer
     GraphicsModeControlJ2/3D
       why isn't GraphicsModeControl a class
       where we can put common methods ? ? ? ?
       all *ControlJ3D extend Control and implement interfaces
       --> looks like interfaces could be abstract classes
             extending Control
     MouseBehaviorJ2/3D
       visad.MouseBehaviorJND.processAWTEvents
       remove Transform3D constructor from make_matrix
     DefaultDisplayRendererJ2D/TwoDDisplayRendererJ3D
       common methods in visad.DisplayRenderer
     ShadowFunctionOrSetTypeJ2/3D, ShadowRealTypeJ2/3D and
       ShadowTupleTypeJ2/3D ShadowTypeJ2/3D adapt common
       methods in visad.Shadow*Type

1. add VisAD-specific scene graph classes:
 
     canvas, root, trans, direct, cursor_trans & other
       scene graph stuff in DisplayRendererJ2D

     VisADSceneGraphObject
       VisADGroup
         VisADSwitch
       VisADAppearance
         incl VisADGeometryArray
         incl Image "texture"
         incl red, green, blue, alpha
         linewidth and pointsize in GraphicsModeControl
       (VisADTexture2D not needed; Image in VisADAppearance)
         texture.setImage(0, image2d);
         new Shape3D(geometry, appearance);
         appearance.setTexture(texture);
       (VisADShape not needed; VisADGeometryArray in VisADAppearance)
       (VisADTransform not needed; trans in DisplayRendererJnD)
       (hence VisADBranchGroup not needed; a VisADBranchGroup
        is a VisADGroup that is not a VisADSwitch)
 
 
2. add VisADSceneGraphObject as parent of
   existing VisAD-specific scene graph classes:

     VisADSceneGraphObject
       VisADGeometryArray
         VisADIndexedTriangleStripArray
         VisADLineArray
         VisADLineStripArray
         VisADPointArray
         VisADTriangleArray
 
3. DisplayRendererJ2D
     add Image[] array with element for each animation step

4. DisplayImplJ2D.doAction
     scratch Image[] array
     super.doAction()
     re-build Image[] element for current time step

5. AnimationControlJ2D.selectSwitches()
     index = super.selectSwitches();
     if (Image[index] == null) re-build Image[index];
     drawImage(Image[index]);
       (build Image[index] using Component.createImage -
        see ObjectAnim Java2D code example)
 
6. ValueControlJ2D.selectSwitches()
     super.selectSwitches();
     scratch Image[] array
     set value
     re-build Image[] element for current time step

7. ProjectionControlJ2D.setMatrix()
     scratch Image[] array
     set projection
     re-build Image[] element for current time step
 
8. VisADCanvasJ2D.renderField()
     invokes DisplayRendererJ2D.drawCursorStringVector()
     which draws cursor strings, Exception strings,
       WaitFlag & Animation string
     add draw of extra_branch from
       DirectManipulationRendererJ2D.addPoint
     invoke after any drawImage(Image[index])
 
9. DirectManipulationRendererJ2D
     doTransform: create branch and extra_branch
     addPoint: add to extra_branch

10. MouseBehaviorJ2D
     just do AWTEvent's

11. DefaultDisplayRendererJ2D = TwoDDisplayRendererJ2D
     legalDisplayScalar?

12. DisplayAppletJ2D delete

13. DisplayImplJ2D.makeGeometry() = return vga;

14. DisplayRendererJ2D.render(int index)

15. RemoveBehaviorJ2D delete

16. UniverseBuilderJ2D delete

NN. renderer thread or Control call-backs ? ? ? ?

*/

package visad.java2d;

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
   DisplayImplJ2D is the VisAD class for displays that use
   Java 3D.  It is runnable.<P>

   DisplayImplJ2D is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DisplayImplJ2D extends DisplayImpl {

  /** legal values for api */
  public static final int JPANEL = 1;
  public static final int APPLETFRAME = 2;
  /** this is used for APPLETFRAME */
  private DisplayAppletJ2D applet = null;

  private ProjectionControlJ2D projection = null;
  private GraphicsModeControlJ2D mode = null;

  /** constructor with DefaultDisplayRendererJ2D */
  public DisplayImplJ2D(String name)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRendererJ2D(), JPANEL);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImplJ2D(String name, DisplayRendererJ2D renderer)
         throws VisADException, RemoteException {
    this(name, renderer, JPANEL);
  }

  /** constructor with DefaultDisplayRenderer */
  public DisplayImplJ2D(String name, int api)
         throws VisADException, RemoteException {
    this(name, new DefaultDisplayRendererJ2D(), api);
  }

  /** constructor with non-DefaultDisplayRenderer */
  public DisplayImplJ2D(String name, DisplayRendererJ2D renderer, int api)
         throws VisADException, RemoteException {
    super(name, renderer);

    // a GraphicsModeControl always exists
    mode = new GraphicsModeControlJ2D(this);
    addControl(mode);
    // a ProjectionControl always exists
    projection = new ProjectionControlJ2D(this);
    addControl(projection);

    if (api == APPLETFRAME) {
      applet = new DisplayAppletJ2D(this);
      Component component = new MainFrame(applet, 256, 256);
      // Component component = new AppletFrame(applet, 256, 256);
      setComponent(component);
      // component.setTitle(name);
    }
    else if (api == JPANEL) {
      Component component = new DisplayPanelJ2D(this);
      setComponent(component);
    }
    else {
      throw new DisplayException("DisplayImplJ2D: bad graphicsApi");
    }
  }

  public ProjectionControl getProjectionControl() {
    return projection;
  }

  public GraphicsModeControl getGraphicsModeControl() {
    return mode;
  }

  public DisplayAppletJ2D getApplet() {
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
      throw new DisplayException("DisplayImplJ2D.makeGeometry");
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

