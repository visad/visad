
//
// DataRenderer.java
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

import java.util.*;
import java.rmi.*;


/**
   DataRenderer is the VisAD abstract super-class for graphics rendering
   algorithms.  These transform Data objects into 3-D (or 2-D)
   depictions in a Display window.<P>

   DataRenderer is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class DataRenderer extends Object {

  //
  // TO_DO
  // make these non-public
  //
  public DisplayImpl display;
  /** used to insert output into scene graph */
  public DisplayRenderer displayRenderer;

  /** links to Data to be renderer by this */
  public transient DataDisplayLink[] Links;
  /** flag from DataDisplayLink.prepareData */
  public boolean[] feasible; // it's a miracle if this is correct
  /** flag to indicate that DataDisplayLink.prepareData was invoked */
  public boolean[] changed;

  public boolean any_changed;
  public boolean all_feasible;
  public boolean any_transform_control;

  /** flag indicating whether DirectManipulationRenderer is valid
      for this ShadowType */
  public boolean isDirectManipulation;

  /** a Vector of BadMappingException and UnimplementedException
      Strings generated during the last invocation of doAction */
  public Vector exceptionVector = new Vector();

  public DataRenderer() {
    Links = null;
    display = null;
  }

  public void clearExceptions() {
    exceptionVector.removeAllElements();
  }

  /** add message from BadMappingException or
      UnimplementedException to exceptionVector */
  public void addException(String error_string) {
    exceptionVector.addElement(error_string);
    // System.out.println(error_string);
  }

  /** get a clone of exceptionVector to avoid
      concurrent access by Display thread */
  public Vector getExceptionVector() {
    return (Vector) exceptionVector.clone();
  }

  public abstract void setLinks(DataDisplayLink[] links, DisplayImpl d)
           throws VisADException;

  public DataDisplayLink[] getLinks() {
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
 
// WLH ????
 
          ShadowType type = Links[i].getShadow().getAdaptedShadowType();
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

  /** re-transform if needed;
      return false if not done */
  public abstract boolean doAction() throws VisADException, RemoteException;

  public void checkDirect() throws VisADException, RemoteException {
    isDirectManipulation = false;
  }

  public boolean getBadScale() {
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

  public abstract void clearScene();

  public void clearAVControls() {
    Enumeration controls = display.getControlVector().elements();
    while (controls.hasMoreElements()) {
      Control control = (Control) controls.nextElement();
      if (control instanceof AVControl) {
        ((AVControl) control).clearSwitches(this);
      }
    }
  }

  public abstract ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowTextType(
         TextType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;
 
  public abstract ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException;

  /** DataRenderer-specific decision about which Controls require re-transform;
      may be over-ridden by DataRenderer sub-classes */
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

}

