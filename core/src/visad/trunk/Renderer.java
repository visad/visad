
//
// Renderer.java
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

import javax.media.j3d.*;
import java.vecmath.*;

import java.util.*;
import java.rmi.*;


/**
   Renderer is the VisAD abstract super-class for graphics rendering
   algorithms.  These transform Data objects into 3-D (or 2-D)
   depictions in a Display window.<P>

   Renderer is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class Renderer extends Object {

  DisplayImpl display;
  /** used to insert output into scene graph */
  DisplayRenderer displayRenderer;
  /** switch is parent of any BranchGroups created by this */
  Switch sw;
  /** parent of sw for 'detach' */
  BranchGroup swParent;
  /** index of current 'intended' child of Switch sw;
      not necessarily == sw.getWhichChild() */
  int currentIndex;
  BranchGroup[] branches;
  boolean[] switchFlags = {false, false, false};
  boolean[] branchNonEmpty = {false, false, false};
  int actualIndex;

  /** links to Data to be renderer by this */
  transient DataDisplayLink[] Links;
  /** flag from DataDisplayLink.prepareData */
  boolean[] feasible; // it's a miracle if this is correct
  /** flag to indicate that DataDisplayLink.prepareData was invoked */
  boolean[] changed;

  private boolean any_changed;
  private boolean all_feasible;
  private boolean any_transform_control;

  public Renderer () {
    Links = null;
    display = null;
  }

  void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (display != null || Links != null) {
      throw new DisplayException("Renderer.setLinks: already set");
    }
    display = d;
    displayRenderer = display.getDisplayRenderer();
    Links = links;
    feasible = new boolean[Links.length];
    changed = new boolean[Links.length];
    for (int i=0; i<Links.length; i++) feasible[i] = false;

    // set up switch logic for clean BranchGroup replacement
    sw = new Switch();
    sw.setCapability(Group.ALLOW_CHILDREN_READ);
    sw.setCapability(Group.ALLOW_CHILDREN_WRITE);
    sw.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    sw.setCapability(Switch.ALLOW_SWITCH_READ);
    sw.setCapability(Switch.ALLOW_SWITCH_WRITE);

    swParent = new BranchGroup();
    swParent.setCapability(BranchGroup.ALLOW_DETACH);
    swParent.addChild(sw);
    // make it 'live'
    addSwitch(displayRenderer, swParent);

    branches = new BranchGroup[3];
    for (int i=0; i<3; i++) {
      branches[i] = new BranchGroup();
      branches[i].setCapability(Group.ALLOW_CHILDREN_READ);
      branches[i].setCapability(Group.ALLOW_CHILDREN_WRITE);
      branches[i].setCapability(Group.ALLOW_CHILDREN_EXTEND);
      sw.addChild(branches[i]);
      // sw.setChild(branches[i], i);
    }
    sw.setWhichChild(currentIndex);
    currentIndex = 0;
    actualIndex = 0;

  }

  abstract void addSwitch(DisplayRenderer displayRenderer,
                          BranchGroup branch);

  DataDisplayLink[] getLinks() {
    return Links;
  }

  /** check if re-transform is needed; if initialize is true then
      compute ranges for RealType-s and Animation sampling */
  public DataShadow prepareAction(boolean initialize, DataShadow shadow)
         throws VisADException, RemoteException {
    any_changed = false;
    all_feasible = true;
    any_transform_control = false;

    for (int i=0; i<Links.length; i++) {
      changed[i] = false;
      DataReference ref = Links[i].getDataReference();
      // test for changed Controls that require doTransform
      if (Links[i].checkTicks() || !feasible[i] || initialize) {
        // data has changed - need to re-display
        changed[i] = true;
        any_changed = true;
        // create ShadowType for data, classify data for display
        feasible[i] = Links[i].prepareData();
        if (!feasible[i]) all_feasible = false;
        if (initialize && feasible[i]) {
          // compute ranges of RealTypes and Animation sampling
          ShadowType type = Links[i].getShadow();
          if (shadow == null) {
            shadow =
              Links[i].getData().computeRanges(type, display.getScalarCount());
          }
          else {
            shadow = Links[i].getData().computeRanges(type, shadow);
          }
        }
      }

      if (feasible[i]) {
        // check if this Data includes any changed Controls
        Enumeration maps = Links[i].getSelectedMapVector().elements();
        while(maps.hasMoreElements()) {
          Control control = ((ScalarMap) maps.nextElement()).getControl();
          if (control != null && control.checkTicks(this, Links[i])) {
            any_transform_control = true;
          }
        }
      }
    }
    return shadow;
  }

  boolean getBadScale() {
    boolean badScale = false;
    for (int i=0; i<Links.length; i++) {
      if (!feasible[i]) return true;
      Enumeration maps = Links[i].getSelectedMapVector().elements();
      while(maps.hasMoreElements()) {
        badScale |= ((ScalarMap) maps.nextElement()).badRange();
      }
    }
    return badScale;
  }

  /** re-transform if needed;
      return false if not done */
  public boolean doAction() throws VisADException, RemoteException {
    BranchGroup branch;
    if (all_feasible && (any_changed || any_transform_control)) {
      clearAVControls();
      try {
        branch = doTransform();
      }
      catch (BadMappingException e) {
        display.addException(e.getMessage());
        branch = null;
      }
      catch (UnimplementedException e) {
        display.addException(e.getMessage());
        branch = null;
      }

      if (branch != null) {
        int nextIndex = 0;
        boolean doRemove = false;
        synchronized (this) {
          if (!branchNonEmpty[currentIndex]) {
            branches[currentIndex].addChild(branch);
            sw.setWhichChild(currentIndex);
            actualIndex = currentIndex;
            branchNonEmpty[currentIndex] = true;
          }
          else { // if (branchNonEmpty[currentIndex])
            nextIndex = (currentIndex + 1) % 3;
            while (branchNonEmpty[nextIndex]) {
              try {
                wait(5000);
              }
              catch(InterruptedException e) {
                // note notify generates a normal return from wait rather
                // than an Exception - control doesn't normally come here
              }
            }
            branches[nextIndex].addChild(branch);
            // displayRenderer.switchScene(this, nextIndex);
            doRemove = true;
            switchFlags[nextIndex] = true;
            branchNonEmpty[nextIndex] = true;
            currentIndex = nextIndex;
          } // end if (branches[currentIndex].numChildren() != 0)
        } // end synchronized (this)
        if (doRemove) displayRenderer.switchScene(this, nextIndex);
      }
      else { // if (branch == null)
        all_feasible = false;
      }
    }
    return all_feasible;
  }

  synchronized boolean switchTransition(int index) {
    // this is the same as (index - 1) % 3 but always positive
    int i = (index + 2) % 3;
    if (switchFlags[index]) {
      if (actualIndex != i) {
        return true;
      }
      sw.setWhichChild(index);
      actualIndex = index;
      switchFlags[index] = false;
      return true;
    }
    else {
      for (int m=0; m<branches[i].numChildren(); m++) {
        branches[i].removeChild(m);
      }
      branchNonEmpty[i] = false;
      notify();
      return false;
    }
  }

  public void clearScene() {
    swParent.detach();
    displayRenderer.clearScene(this);
  }

  public void clearAVControls() {
    Enumeration controls = display.getControlVector().elements();
    while (controls.hasMoreElements()) {
      Control control = (Control) controls.nextElement();
      if (control instanceof AVControl) {
        ((AVControl) control).clearSwitches(this);
      }
    }
  }

  /** Renderer-specific decision about which Controls require re-transform;
      may be over-ridden by Renderer sub-classes */
  public boolean isTransformControl(Control control, DataDisplayLink link) {
    if (control instanceof ProjectionControl ||
        control instanceof ToggleControl) {
      return false;
    }
/* WLH 1 Nov 97 - temporary hack -
   RangeControl changes always require Transform
   ValueControl and AnimationControl never do

    if (control instanceof AnimationControl ||
        control instanceof ValueControl ||
        control instanceof RangeControl) {
      return link.isTransform[control.getIndex()];
*/
    if (control instanceof AnimationControl ||
        control instanceof ValueControl) {
      return false;
    }
    return true;
  }

  /** create a BranchGroup scene graph for Data in links;
      this can put Behavior objects in the scene graph for
      Renderer classes that implement direct manipulation widgets;
      may reduce work by only changing scene graph for Data and
      Controls that have changed:
      1. use boolean[] changed to determine which Data objects have changed
      2. if Data has not changed, then use Control.checkTicks loop like in
         prepareAction to determine which Control-s have changed */
  public abstract BranchGroup doTransform()
         throws VisADException, RemoteException;

}

