
//
// DefaultDisplayRendererJ2D.java
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

package visad.java2d;

import visad.*;

import java.awt.event.*;

import java.util.*;


/**
   DefaultDisplayRendererJ2D is the VisAD class for default background
   and metadata rendering under Java2D.<P>
*/
public class DefaultDisplayRendererJ2D extends DisplayRendererJ2D {

  /** box outline for data */
  VisADAppearance box = null;
  /** cursor */
  VisADAppearance cursor = null;
  /** Behavior for mouse interactions */
  MouseBehaviorJ2D mouse = null;

  /** this is the default DisplayRenderer used by the
      DisplayImplJ2D constructor;
      it draws a 2-D box around the scene and a 2-D cursor;
      the left mouse button controls the projection as
      follows: mouse drag or mouse drag with Ctrl translates
      the scene sideways, mouse drag with Shift down zooms
      the scene; the center mouse button activates and
      controls the 2-D cursor as follows: mouse drag
      translates the cursor sideways; the right mouse button
      is used for direct manipulation by clicking on the
      depiction of a Data object and dragging or re-drawing
      it; cursor and direct manipulation locations are
      displayed in RealType values; BadMappingExceptions
      and UnimplementedExceptions are displayed;
      no RealType may be mapped to ZAxis, Latitude
      or Alpha */
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

  float[] getCursorColor() {
    float[] c3 = new float[3];
    c3[0] = cursor.red;
    c3[1] = cursor.green;
    c3[2] = cursor.blue;
    return c3;
  }

  /** create scene graph root, if none exists, with Transform
      and direct manipulation root;
      create 3-D box, lights and MouseBehaviorJ2D for
      embedded user interface */
  public VisADGroup createSceneGraph(VisADCanvasJ2D c)
         throws DisplayException {
    VisADGroup root = getRoot();
    if (root != null) return root;

    // create MouseBehaviorJ2D for mouse interactions
    mouse = new MouseBehaviorJ2D(this);
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

