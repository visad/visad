
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
  /** scene graph component */
  BranchGroup sceneGraphComponent;

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
    sceneGraphComponent = null;
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
  }

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
/* DEBUG
      System.out.println("Renderer.prepareAction " + ref.getName());
*/
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
          if (control.checkTicks(this, Links[i])) {
            any_transform_control = true;
          }
        }
      }
      Links[i].syncTicks();
    }
    return shadow;
  }

  /** re-transform if needed;
      return false if not done */
  public boolean doAction() throws VisADException, RemoteException {
    clearScene();
    if (all_feasible && (any_changed || any_transform_control)) {
      try {
        sceneGraphComponent = doTransform();
      }
      catch (UnimplementedException e) {
        String errorMessage = e.getMessage(); // do something with this
        System.out.println("UnimplementedException: " + errorMessage);
        sceneGraphComponent = null;
      }

      if (sceneGraphComponent != null) {
        displayRenderer.addSceneGraphComponent(sceneGraphComponent);
      }
      else {
        all_feasible = false;
      }
    }
    return all_feasible;
  }

  public void clearScene() {
    if (sceneGraphComponent != null) {
      displayRenderer.removeSceneGraphComponent(sceneGraphComponent);
      sceneGraphComponent = null;
    }
  }

  /** Renderer-specific decision about which Controls require re-transform;
      may be over-ridden by Renderer sub-classes */
  public boolean isTransformControl(Control control, DataDisplayLink link) {
    if (control instanceof ProjectionControl ||
        control instanceof ToggleControl) return false;
    if (control instanceof AnimationControl ||
        control instanceof ValueControl ||
        control instanceof RangeControl) {
      return link.isTransform[control.getIndex()];
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

