
//
// RendererJ3D.java
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

import javax.media.j3d.*;

import java.util.*;
import java.rmi.*;


/**
   RendererJ3D is the VisAD abstract super-class for graphics rendering
   algorithms under Java3D.  These transform Data objects into 3-D
   (or 2-D) depictions in a Display window.<P>

   RendererJ3D is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class RendererJ3D extends DataRenderer {

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

  public RendererJ3D() {
    super();
  }

  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (getDisplay() != null || getLinks() != null) {
      throw new DisplayException("RendererJ3D.setLinks: already set");
    }
    if (!(d instanceof DisplayImplJ3D)) {
      throw new DisplayException("RendererJ3D.setLinks: must be DisplayImplJ3D");
    }
    setDisplay(d);
    setDisplayRenderer(d.getDisplayRenderer());
    setLinks(links);

    // set up switch logic for clean BranchGroup replacement
    sw = new Switch(); // J3D
    sw.setCapability(Group.ALLOW_CHILDREN_READ);
    sw.setCapability(Group.ALLOW_CHILDREN_WRITE);
    sw.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    sw.setCapability(Switch.ALLOW_SWITCH_READ);
    sw.setCapability(Switch.ALLOW_SWITCH_WRITE);

    swParent = new BranchGroup();
    swParent.setCapability(BranchGroup.ALLOW_DETACH);
    swParent.addChild(sw);
    // make it 'live'
    addSwitch((DisplayRendererJ3D) getDisplayRenderer(), swParent);

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

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowFunctionTypeJ3D(type, link, parent);
  }
 
  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowRealTupleTypeJ3D(type, link, parent);
  }
 
  public ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowRealTypeJ3D(type, link, parent);
  }
 
  public ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowSetTypeJ3D(type, link, parent);
  }
 
  public ShadowType makeShadowTextType(
         TextType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowTextTypeJ3D(type, link, parent);
  }
 
  public ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowTupleTypeJ3D(type, link, parent);
  }

  abstract void addSwitch(DisplayRendererJ3D displayRenderer,
                          BranchGroup branch);

  /** re-transform if needed;
      return false if not done */
  public boolean doAction() throws VisADException, RemoteException {
    BranchGroup branch; // J3D
    boolean all_feasible = get_all_feasible();
    boolean any_changed = get_any_changed();
    boolean any_transform_control = get_any_transform_control();
    if (all_feasible && (any_changed || any_transform_control)) {
     // exceptionVector.removeAllElements();
      clearAVControls();
      try {
        // doTransform creates a BranchGroup from a Data object
        branch = doTransform();
      }
      catch (BadMappingException e) {
        addException(e);
        branch = null;
      }
      catch (UnimplementedException e) {
        addException(e);
        branch = null;
      }
      catch (RemoteException e) {
        addException(e);
        branch = null;
      }
      catch (DisplayInterruptException e) {
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
            doRemove = true;
            switchFlags[nextIndex] = true;
            branchNonEmpty[nextIndex] = true;
            currentIndex = nextIndex;
          } // end if (branches[currentIndex].numChildren() != 0)
        } // end synchronized (this)
        if (doRemove) {
          ((DisplayRendererJ3D) getDisplayRenderer()).
                 switchScene(this, nextIndex);
        }
      }
      else { // if (branch == null)
        all_feasible = false;
        set_all_feasible(all_feasible);
      }
    }
    else { // !(all_feasible && (any_changed || any_transform_control))
      DataDisplayLink[] links = getLinks();
      for (int i=0; i<links.length; i++) {
        links[i].clearData();
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
      sw.setWhichChild(index); // J3D
      actualIndex = index;
      switchFlags[index] = false;
      return true;
    }
    else {
      for (int m=0; m<branches[i].numChildren(); m++) { // J3D
        branches[i].removeChild(m); // J3D
      }
      branchNonEmpty[i] = false;
      notify();
      return false;
    }
  }

  public void clearScene() {
    swParent.detach(); // J3D
    ((DisplayRendererJ3D) getDisplayRenderer()).clearScene(this);
  }

  /** create a BranchGroup scene graph for Data in links;
      this can put Behavior objects in the scene graph for
      DataRenderer classes that implement direct manipulation widgets;
      may reduce work by only changing scene graph for Data and
      Controls that have changed:
      1. use boolean[] changed to determine which Data objects have changed
      2. if Data has not changed, then use Control.checkTicks loop like in
         prepareAction to determine which Control-s have changed */
  public abstract BranchGroup doTransform()
         throws VisADException, RemoteException; // J3D

}

