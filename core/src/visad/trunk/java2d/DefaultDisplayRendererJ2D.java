//
// DefaultDisplayRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.java2d;

import visad.*;

import java.awt.event.*;

import java.util.*;


/**
 * <CODE>DefaultDisplayRendererJ2D</CODE> is the VisAD class for
 * default background and metadata rendering under Java2D.<P>
 */
public class DefaultDisplayRendererJ2D extends DisplayRendererJ2D {

  /** box outline for data */
  VisADAppearance box = null;
  /** cursor */
  VisADAppearance cursor = null;
  /** Behavior for mouse interactions */
  MouseBehaviorJ2D mouse = null;

  /**
   * This is the default <CODE>DisplayRenderer</CODE> used by the
   * <CODE>DisplayImplJ2D</CODE> constructor.
   * It draws a 2-D box around the scene and a 2-D cursor.<P>
   * The left mouse button controls the projection as follows:
   * <UL>
   *  <LI>mouse drag or mouse drag with Ctrl translates the scene sideways
   *  <LI>mouse drag with Shift down zooms the scene
   * </UL>
   * The center mouse button activates and controls the 2-D cursor as
   * follows:
   * <UL>
   *  <LI>mouse drag translates the cursor sideways
   * </UL>
   * The right mouse button is used for direct manipulation by clicking on
   * the depiction of a <CODE>Data</CODE> object and dragging or re-drawing
   * it.<P>
   * Cursor and direct manipulation locations are displayed in RealType
   * values<P>
   * <CODE>BadMappingExceptions</CODE> and
   * <CODE>UnimplementedExceptions</CODE> are displayed<P>
   * No RealType may be mapped to ZAxis, Latitude or Alpha.
   */
  public DefaultDisplayRendererJ2D () {
    super();
  }

  public boolean getMode2D() {
    return true;
  }

  public boolean legalDisplayScalar(DisplayRealType type) {
    if (Display.ZAxis.equals(type) ||
        Display.Latitude.equals(type) ||
        Display.Alpha.equals(type)) return false;
    else return super.legalDisplayScalar(type);
  }

  public void setBoxColor(float r, float g, float b) {
    box.red = r;
    box.green = g;
    box.blue = b;
    getCanvas().scratchImages();
  }

  public void setCursorColor(float r, float g, float b) {
    cursor.red = r;
    cursor.green = g;
    cursor.blue = b;
    render_trigger();
  }

  public float[] getCursorColor() {
    float[] c3 = new float[3];
    c3[0] = cursor.red;
    c3[1] = cursor.green;
    c3[2] = cursor.blue;
    return c3;
  }

  /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root;
   * create 3-D box, lights and <CODE>MouseBehaviorJ2D</CODE> for
   * embedded user interface.
   * @param c
   * @return Scene graph root.
   * @exception DisplayException
   */
  public VisADGroup createSceneGraph(VisADCanvasJ2D c)
         throws DisplayException {
    VisADGroup root = getRoot();
    if (root != null) return root;

    // create MouseBehaviorJ2D for mouse interactions
    mouse = new MouseBehaviorJ2D(this);
    getDisplay().setMouseBehavior(mouse);
    root = createBasicSceneGraph(c, mouse);

    // create the box containing data depictions
    VisADLineArray box_array = new VisADLineArray();
    box_array.coordinates = box_verts;
    box_array.vertexCount = 8;

    box = new VisADAppearance();
    box.red = 1.0f;
    box.green = 1.0f;
    box.blue = 1.0f;
    box.color_flag = true;
    box.array = box_array;
    // add box to root
/* WLH 5 Feb 99
    root.addChild(box);
*/
    VisADGroup box_on = getBoxOnBranch();
    box_on.addChild(box);

    // create cursor
    VisADLineArray cursor_array = new VisADLineArray();
    cursor_array.coordinates = cursor_verts;
    cursor_array.vertexCount = 4;

    cursor = new VisADAppearance();
    cursor.red = 1.0f;
    cursor.green = 1.0f;
    cursor.blue = 1.0f;
    cursor.color_flag = true;
    cursor.array = cursor_array;
    // add cursor to cursor_on branch
    VisADGroup cursor_on = getCursorOnBranch();
    cursor_on.addChild(cursor);

    return root;
  }

  private static final float[] box_verts = {
     // front face
         -1.0f, -1.0f,  0.0f,                       -1.0f,  1.0f,  0.0f,
         -1.0f,  1.0f,  0.0f,                        1.0f,  1.0f,  0.0f,
          1.0f,  1.0f,  0.0f,                        1.0f, -1.0f,  0.0f,
          1.0f, -1.0f,  0.0f,                       -1.0f, -1.0f,  0.0f
  };

  private static final float[] cursor_verts = {
          0.0f,  0.1f,  0.0f,                        0.0f, -0.1f,  0.0f,
          0.1f,  0.0f,  0.0f,                       -0.1f,  0.0f,  0.0f
  };

}

