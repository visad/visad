//
// ImmersaDeskDisplayRendererJ3D.java
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

package visad.java3d;

import visad.*;

import java.awt.event.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.rmi.RemoteException;

import java.util.*;

/*
extend MouseBehaviorJ3D to:
  draw ray from wand for direct manipulation (right button)
  also to drive cursor location from wand (center button)
  rotation, scale, translate (left button)?

connect head tracker to DisplayRendererJ3D.vpTrans
*/

/**
 * <CODE>ImmersaDeskDisplayRendererJ3D</CODE> is the VisAD class for the
 * ImmersaDesk background and metadata rendering algorithm under Java3D.<P>
 */
public class ImmersaDeskDisplayRendererJ3D extends DisplayRendererJ3D {

  /** color of box and cursor */
  private ColoringAttributes box_color = null;
  private ColoringAttributes cursor_color = null;

  private MouseBehaviorJ3D mouse = null; // Behavior for mouse interactions

  /**
   * This is the default <CODE>DisplayRenderer</CODE> used by the
   * <CODE>DisplayImplJ3D</CODE> constructor.
   * It draws a 3-D cube around the scene.<P>
   * The left mouse button controls the projection as follows:
   * <UL>
   *  <LI>mouse drag rotates in 3-D
   *  <LI>mouse drag with Shift down zooms the scene
   *  <LI>mouse drag with Ctrl translates the scene sideways
   * </UL>
   * The center mouse button activates and controls the
   * 3-D cursor as follows:
   * <UL>
   *  <LI>mouse drag translates the cursor sideways
   *  <LI>mouse drag with Shift translates the cursor in and out
   *  <LI>mouse drag with Ctrl rotates scene in 3-D with cursor on
   * </UL>
   * The right mouse button is used for direct manipulation by clicking on
   * the depiction of a <CODE>Data</CODE> object and dragging or re-drawing
   * it.<P>
   * Cursor and direct manipulation locations are displayed in RealType
   * values.<P>
   * <CODE>BadMappingExceptions</CODE> and
   * <CODE>UnimplementedExceptions</CODE> are displayed<P>
   */
  public ImmersaDeskDisplayRendererJ3D () {
    super();
  }

  /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root;
   * create 3-D box, lights and <CODE>MouseBehaviorJ3D</CODE> for
   * embedded user interface.
   * @param v
   * @param vpt
   * @param c
   * @return Scene graph root.
   */
  public BranchGroup createSceneGraph(View v, TransformGroup vpt,
                                      VisADCanvasJ3D c) {
    BranchGroup root = getRoot();
    if (root != null) return root;

    // create MouseBehaviorJ3D for mouse interactions
    mouse = new MouseBehaviorJ3D(this);
    getDisplay().setMouseBehavior(mouse);
    box_color = new ColoringAttributes();
    cursor_color = new ColoringAttributes();
    root = createBasicSceneGraph(v, vpt, c, mouse, box_color, cursor_color);
    TransformGroup trans = getTrans();

    // create the box containing data depictions
    LineArray box_geometry = new LineArray(24, LineArray.COORDINATES);
    box_geometry.setCoordinates(0, box_verts);
    Appearance box_appearance = new Appearance();
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    float[] ctlBox = getRendererControl().getBoxColor();
    box_color.setColor(ctlBox[0], ctlBox[1], ctlBox[2]);
    box_appearance.setColoringAttributes(box_color);
    Shape3D box = new Shape3D(box_geometry, box_appearance);
    BranchGroup box_on = getBoxOnBranch();
    box_on.addChild(box);

    Appearance cursor_appearance = new Appearance();
    cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    float[] ctlCursor = getRendererControl().getCursorColor();
    cursor_color.setColor(ctlCursor[0], ctlCursor[1], ctlCursor[2]);
    cursor_appearance.setColoringAttributes(cursor_color);

    BranchGroup cursor_on = getCursorOnBranch();
    LineArray cursor_geometry = new LineArray(6, LineArray.COORDINATES);
    cursor_geometry.setCoordinates(0, cursor_verts);
    Shape3D cursor = new Shape3D(cursor_geometry, cursor_appearance);
    cursor_on.addChild(cursor);

    // insert MouseBehaviorJ3D into scene graph
    BoundingSphere bounds =
      new BoundingSphere(new Point3d(0.0,0.0,0.0), 2000000.0);
    mouse.setSchedulingBounds(bounds);
    trans.addChild(mouse);

    // create ambient light, directly under root (not transformed)
/* WLH 27 Jan 98
    Color3f color = new Color3f(0.4f, 0.4f, 0.4f);
*/
    Color3f color = new Color3f(0.6f, 0.6f, 0.6f);
    AmbientLight light = new AmbientLight(color);
    light.setInfluencingBounds(bounds);
    root.addChild(light);

    // create directional lights, directly under root (not transformed)
    Color3f dcolor = new Color3f(0.9f, 0.9f, 0.9f);
    Vector3f direction1 = new Vector3f(0.0f, 0.0f, 1.0f);
    Vector3f direction2 = new Vector3f(0.0f, 0.0f, -1.0f);
    DirectionalLight light1 =
      new DirectionalLight(true, dcolor, direction1);
    light1.setInfluencingBounds(bounds);
    DirectionalLight light2 =
      new DirectionalLight(true, dcolor, direction2);
    light2.setInfluencingBounds(bounds);
    root.addChild(light1);
    root.addChild(light2);

    return root;
  }

  private static final float[] box_verts = {
     // front face
         -1.0f, -1.0f,  1.0f,                       -1.0f,  1.0f,  1.0f,
         -1.0f,  1.0f,  1.0f,                        1.0f,  1.0f,  1.0f,
          1.0f,  1.0f,  1.0f,                        1.0f, -1.0f,  1.0f,
          1.0f, -1.0f,  1.0f,                       -1.0f, -1.0f,  1.0f,
     // back face
         -1.0f, -1.0f, -1.0f,                       -1.0f,  1.0f, -1.0f,
         -1.0f,  1.0f, -1.0f,                        1.0f,  1.0f, -1.0f,
          1.0f,  1.0f, -1.0f,                        1.0f, -1.0f, -1.0f,
          1.0f, -1.0f, -1.0f,                       -1.0f, -1.0f, -1.0f,
     // connectors
         -1.0f, -1.0f,  1.0f,                       -1.0f, -1.0f, -1.0f,
         -1.0f,  1.0f,  1.0f,                       -1.0f,  1.0f, -1.0f,
          1.0f,  1.0f,  1.0f,                        1.0f,  1.0f, -1.0f,
          1.0f, -1.0f,  1.0f,                        1.0f, -1.0f, -1.0f
  };

  private static final float[] cursor_verts = {
          0.0f,  0.0f,  0.1f,                        0.0f,  0.0f, -0.1f,
          0.0f,  0.1f,  0.0f,                        0.0f, -0.1f,  0.0f,
          0.1f,  0.0f,  0.0f,                       -0.1f,  0.0f,  0.0f
  };

}

