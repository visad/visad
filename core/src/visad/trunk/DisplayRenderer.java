
//
// DisplayRenderer.java
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

import java.awt.event.*;
import javax.media.j3d.*;
import java.vecmath.*;

import java.util.*;


/**
   DisplayRenderer is the VisAD abstract super-class for background and
   metadata rendering algorithms.  These complement depictions of Data
   objects created by Renderer objects.<P>

   DisplayRenderer also manages the overall relation of Renderer
   output to Java3D and manages the scene graph.<P>

   It creates the binding between Control objects and scene graph
   Behavior objects for direct manipulation of Control objects.<P>

   DisplayRenderer is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class DisplayRenderer extends Object {

  private DisplayImpl display;

  private BranchGroup root = null;
  private TransformGroup trans;

  // box outline for data
  Shape3D box;
  // color of box
  ColoringAttributes box_color;

  // Behavior for mouse interactions
  MouseBehavior mouse;

  public DisplayRenderer () {
  }

  void setDisplay(DisplayImpl d) throws VisADException {
    if (display != null) {
      throw new DisplayException("DisplayRenderer.setDisplay: " +
                                 "display already set");
    }
    display = d;
  }

  /** Java3D - create scene graph, if none exists, with Transform
      and Behavior objects linked to appropriate Control objects
      [e.g., AWTEvent Behavior changes ProjectionControl.Matrix
      and invokes ProjectionControl.changeControl()]
      link scene graph to VirtualUniverse */
  public BranchGroup createSceneGraph() {
    if (root != null) return root;
    // Create the root of the branch graph
    root = new BranchGroup();
    // create the TransformGroup that is the parent of
    // Data object BranchGroup objects
    trans = new TransformGroup();
    trans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    trans.setCapability(Group.ALLOW_CHILDREN_READ);
    trans.setCapability(Group.ALLOW_CHILDREN_WRITE);
    trans.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    root.addChild(trans);
 
    // create the box containing data depictions
    LineArray box_geometry = new LineArray(24, LineArray.COORDINATES);
    box_geometry.setCoordinates(0, box_verts);
    Appearance box_appearance = new Appearance();
    box_color = new ColoringAttributes();
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
    box_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    box_color.setColor(1.0f, 1.0f, 1.0f);
    box_appearance.setColoringAttributes(box_color);
    box = new Shape3D(box_geometry, box_appearance);
    // first child of trans
    trans.addChild(box);
 
    // create the Bahevior for mouse interactions
    ProjectionControl proj = (ProjectionControl)
      display.getControl(ProjectionControl.prototype.getClass());
    mouse = new MouseBehavior(proj);
    BoundingSphere bounds =
      new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
    mouse.setSchedulingBounds(bounds);
    // second child of trnas
    trans.addChild(mouse);
 
    return root;
  }

  // argument type must be changed to Java3D.Group
  public void addSceneGraphComponent(BranchGroup branch) {
    trans.addChild(branch);
  }

  // argument type must be changed to Java3D.Group
  public void removeSceneGraphComponent(BranchGroup branch) {
    branch.detach();
  }

  public void setTransform3D(Transform3D t) {
    trans.setTransform(t);
  }

  // Java3D
  public void setSwitch(int step) {
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
          1.0f, -1.0f,  1.0f,                        1.0f, -1.0f, -1.0f,
  };

}

