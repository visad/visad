
//
// TwoDDisplayRendererJ3D.java
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

import java.awt.event.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.*;


/**
   TwoDDisplayRendererJ3D is the VisAD class for 2-D background
   and metadata rendering under Java3D.<P>
*/
public class TwoDDisplayRendererJ3D extends DisplayRendererJ3D {

  /** color of box */
  ColoringAttributes box_color = null;
  MouseBehaviorJ3D mouse = null; // Behavior for mouse interactions

  /** this DisplayRenderer supports 2-D only rendering;
      is easiest to describe in terms of differences
      from DefaultDisplayRendererJ3D: the cursor and box
      around the scene are 2-D, the scene cannot be rotated,
      the cursor cannot be translated in and out, and the
      scene can be translated sideways with the left mouse
      button with or without pressing the Ctrl key;
      no RealType may be mapped to ZAxis or Latitude */
  public TwoDDisplayRendererJ3D () {
    super();
  }

  public boolean getMode2D() {
    return true;
  }

  public boolean legalDisplayScalar(DisplayRealType type) {
    if (Display.ZAxis.equals(type) ||
        Display.Latitude.equals(type)) return false;
    else return super.legalDisplayScalar(type);
  }

  /** create scene graph root, if none exists, with Transform
      and direct manipulation root;
      create 3-D box, lights and MouseBehaviorJ3D for
      embedded user interface */
  public BranchGroup createSceneGraph(View v, VisADCanvasJ3D c) {
    BranchGroup root = getRoot();
    if (root != null) return root;
 
    // create MouseBehaviorJ3D for mouse interactions
    mouse = new MouseBehaviorJ3D(this);
    root = createBasicSceneGraph(v, c, mouse);
    TransformGroup trans = getTrans();

    // create the box containing data depictions
    LineArray box_geometry = new LineArray(8, LineArray.COORDINATES);
    box_geometry.setCoordinates(0, box_verts);
    Appearance box_appearance = new Appearance();
    box_color = new ColoringAttributes();
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    box_color.setColor(1.0f, 1.0f, 1.0f);
    box_appearance.setColoringAttributes(box_color);
    Shape3D box = new Shape3D(box_geometry, box_appearance);
    // first child of trans
    trans.addChild(box);
 
    BranchGroup cursor_on = getCursorOnBranch();
    LineArray cursor_geometry = new LineArray(4, LineArray.COORDINATES);
    cursor_geometry.setCoordinates(0, cursor_verts);
    Shape3D cursor = new Shape3D(cursor_geometry, box_appearance);
    cursor_on.addChild(cursor);

    // insert MouseBehaviorJ3D into scene graph
    BoundingSphere bounds =
      new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
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

/*
  private static final float[] box_verts = {
     // front face
         -1.0f, -1.0f,  0.3f,                       -1.0f,  1.0f,  0.3f,
         -1.0f,  1.0f,  0.3f,                        1.0f,  1.0f,  0.3f,
          1.0f,  1.0f,  0.3f,                        1.0f, -1.0f,  0.3f,
          1.0f, -1.0f,  0.3f,                       -1.0f, -1.0f,  0.3f
  };

  private static final float[] cursor_verts = {
          0.0f,  0.1f,  0.3f,                        0.0f, -0.1f,  0.3f,
          0.1f,  0.0f,  0.3f,                       -0.1f,  0.0f,  0.3f
  };
*/

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

