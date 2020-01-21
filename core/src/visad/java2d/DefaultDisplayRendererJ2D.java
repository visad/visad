//
// DefaultDisplayRendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

import java.lang.reflect.Constructor;

import visad.Display;
import visad.DisplayException;
import visad.DisplayRealType;
import visad.VisADAppearance;
import visad.VisADError;
import visad.VisADGroup;
import visad.VisADLineArray;

/**
 * <CODE>DefaultDisplayRendererJ2D</CODE> is the VisAD class for
 * default background and metadata rendering under Java2D.<P>
 */
public class DefaultDisplayRendererJ2D extends DisplayRendererJ2D {

  /** box outline for data */
  private VisADAppearance box = null;
  /** cursor */
  private VisADAppearance cursor = null;

  private Class mouseBehaviorJ2DClass = null;

  /** Behavior for mouse interactions */
  private MouseBehaviorJ2D mouse = null;

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
    mouseBehaviorJ2DClass = MouseBehaviorJ2D.class;
  }

  /**
   * @param mbj2dClass - sub Class of MouseBehaviorJ2D
   */
  
  public DefaultDisplayRendererJ2D (Class mbj2dClass) {
    super();
    mouseBehaviorJ2DClass = mbj2dClass;
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
    try {
      Class[] param = new Class[] {DisplayRendererJ2D.class};
      Constructor mbConstructor =
        mouseBehaviorJ2DClass.getConstructor(param);
      mouse = (MouseBehaviorJ2D) mbConstructor.newInstance(new Object[] {this});
    }
    catch (Exception e) {
      throw new VisADError("cannot construct " + mouseBehaviorJ2DClass);
    }
    // mouse = new MouseBehaviorJ2D(this);

    getDisplay().setMouseBehavior(mouse);
    box = new VisADAppearance();
    cursor = new VisADAppearance();
    root = createBasicSceneGraph(c, mouse, box, cursor);

    // create the box containing data depictions
    VisADLineArray box_array = new VisADLineArray();
    box_array.coordinates = box_verts;
    box_array.vertexCount = 8;

    float[] ctlBox = getRendererControl().getBoxColor();
    box.red = ctlBox[0];
    box.green = ctlBox[1];
    box.blue = ctlBox[2];
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

    float[] ctlCursor = getRendererControl().getCursorColor();
    cursor.red = ctlCursor[0];
    cursor.green = ctlCursor[1];
    cursor.blue = ctlCursor[2];
    cursor.color_flag = true;
    cursor.array = cursor_array;
    // add cursor to cursor_on branch
    VisADGroup cursor_on = getCursorOnBranch();
    cursor_on.addChild(cursor);

    return root;
  }

  /**
   * set the aspect for the containing box
   * aspect double[3] array used to scale x, y and z box sizes
   */
  public void setBoxAspect(double[] aspect) {
    float[] new_verts = new float[box_verts.length];
    for (int i=0; i<box_verts.length; i+=3) {
      new_verts[i] = (float) (box_verts[i] * aspect[0]);
      new_verts[i+1] = (float) (box_verts[i+1] * aspect[1]);
      new_verts[i+2] = (float) (box_verts[i+2] * aspect[2]);
    }
    VisADLineArray box_array = (VisADLineArray) box.array;
    box_array.coordinates = new_verts;
  }

  // WLH 2 Dec 2002 in response to qomo2.txt
  public void setLineWidth(float width) {
    box.lineWidth = width;
    cursor.lineWidth = width;
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

