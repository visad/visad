
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

  /** View associated with this VirtualUniverse */
  private View view;
  /** Canvas3D associated with this VirtualUniverse */
  private Canvas3D canvas;

  /** root BranchGroup of scene graph under Locale */
  private BranchGroup root = null;
  /** single TransformGroup between root and BranchGroups for all
      Data depictions */
  private TransformGroup trans = null;
  /** BranchGroup between trans and all direct manipulation
      Data depictions */
  private BranchGroup direct = null;
  /** Behavior for delayed removal of BranchNodes */
  RemoveBehavior remove = null;

  /** distance threshhold for successful pick */
  private static final float PICK_THRESHHOLD = 0.05f;

  private Vector directs = new Vector();

  public DisplayRenderer () {
  }

  void setDisplay(DisplayImpl d) throws VisADException {
    if (display != null) {
      throw new DisplayException("DisplayRenderer.setDisplay: " +
                                 "display already set");
    }
    display = d;
  }

  public View getView() {
    return view;
  }

  public Canvas3D getCanvas() {
    return canvas;
  }

  public DisplayImpl getDisplay() {
    return display;
  }

  public BranchGroup getRoot() {
    return root;
  }

  public TransformGroup getTrans() {
    return trans;
  }

  // public BranchGroup getDirect() {
  public BranchGroup getDirect() {
    return direct;
  }

  /** create scene graph root, if none exists, with Transform
      and direct manipulation root;
      create special graphics (e.g., 3-D box, SkewT background),
      any lights, any user interface embedded in scene */
  public abstract BranchGroup createSceneGraph(View v, Canvas3D c);

  /** create scene graph root, if none exists, with Transform
      and direct manipulation root */
  public BranchGroup createBasicSceneGraph(View v, Canvas3D c) {
    if (root != null) return root;
    view = v;
    canvas = c;
    // Create the root of the branch graph
    root = new BranchGroup();
    // create the TransformGroup that is the parent of
    // Data object Group objects
    trans = new TransformGroup();
    trans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    trans.setCapability(Group.ALLOW_CHILDREN_READ);
    trans.setCapability(Group.ALLOW_CHILDREN_WRITE);
    trans.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    root.addChild(trans);
 
    // create the BranchGroup that is the parent of direct
    // manipulation Data object BranchGroup objects
    direct = new BranchGroup();
    direct.setCapability(Group.ALLOW_CHILDREN_READ);
    direct.setCapability(Group.ALLOW_CHILDREN_WRITE);
    direct.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    direct.setCapability(Node.ENABLE_PICK_REPORTING);
    trans.addChild(direct);

    // create removeBehavior
    remove = new RemoveBehavior(this);
    BoundingSphere bounds =
      new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
    remove.setSchedulingBounds(bounds);
    trans.addChild(remove);

    return root;
  }

  public void addSceneGraphComponent(Group group) {
    trans.addChild(group);
  }

  public void addDirectManipulationSceneGraphComponent(Group group,
                         DirectManipulationRenderer renderer) {
    direct.addChild(group);
    directs.addElement(renderer);
/* WLH 12 Dec 97 - this didn't help
    if (last == null) {
      direct.addChild(branch);
    }
    else {
      int n = direct.numChildren();
      for (int i=0; i<n; i++) {
        if (last.equals(direct.getChild(i))) {
          direct.setChild(branch, i);
        }
      }
    }
*/
  }

  public void switchScene(Renderer renderer, int index) {
    remove.addRemove(renderer, index);
  }

  public void clearScene(Renderer renderer) {
    directs.removeElement(renderer);
  }

  public DirectManipulationRenderer findDirect(PickRay ray) {
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();
    ray.get(origin, direction);
    DirectManipulationRenderer renderer = null;
    float distance = Float.MAX_VALUE;
    Enumeration renderers = directs.elements();
    while (renderers.hasMoreElements()) {
      DirectManipulationRenderer r =
        (DirectManipulationRenderer) renderers.nextElement();
      float d = r.checkClose(origin, direction);
      if (d < distance) {
        distance = d;
        renderer = r;
      }
    }
    if (distance < PICK_THRESHHOLD) {
      return renderer;
    }
    else {
      return null;
    }
  }

  public boolean anyDirects() {
    return !directs.isEmpty();
  }

  public void setTransform3D(Transform3D t) {
    trans.setTransform(t);
  }

}

