
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
    TransformGroup trans = getTrans();

    // create the box containing data depictions
    VisADLineArray box_geometry = new VisADLineArray();
    float[] coordinates = new float[8 * 3];
    int j = 0;
    for (int i=0; i<8; i++) {
      coordinates[i] = box_verts[j++];
      coordinates[8 + i] = box_verts[j++];
      coordinates[16 + i] = box_verts[j++];
    }
    box_geometry.coordinates = coordinates;

    box = new VisADAppearance();
    box.red = 1.0f;
    box.green = 1.0f;
    box.blue = 1.0f;
    box.array = box_geometry;
    // add box to root
    root.addChild(box);
 
    // create cursor
    VisADLineArray cursor_geometry = new VisADLineArray();
    coordinates = new float[8 * 3];
    j = 0;
    for (int i=0; i<4; i++) {
      coordinates[i] = cursor_verts[j++];
      coordinates[4 + i] = cursor_verts[j++];
      coordinates[8 + i] = cursor_verts[j++];
    }
    cursor_geometry.coordinates = coordinates;

    cursor = new VisADAppearance();
    cursor.red = 1.0f;
    cursor.green = 1.0f;
    cursor.blue = 1.0f;
    cursor.array = cursor_geometry;
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

