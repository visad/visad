//
// RendererJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java2d;

import visad.*;

import java.rmi.*;


/**
   RendererJ2D is the VisAD abstract super-class for graphics
   rendering algorithms under Java2D.  These transform Data
   objects into 2-D depictions in a Display window.<P>

   RendererJ2D is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class RendererJ2D extends DataRenderer {

  /** parent of branch made by doAction */
  VisADGroup swParent;

  VisADSwitch swit;

  public RendererJ2D() {
    super();
  }

  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (getDisplay() != null || getLinks() != null) {
      throw new DisplayException("RendererJ2D.setLinks: already set");
    }
    if (!(d instanceof DisplayImplJ2D)) {
      throw new DisplayException("RendererJ2D.setLinks: must be DisplayImplJ2D");
    }
    setDisplay(d);
    setDisplayRenderer(d.getDisplayRenderer());
    setLinks(links);

    swParent = new VisADGroup();
    // addSwitch((DisplayRendererJ2D) getDisplayRenderer(), swParent);

    swit = new VisADSwitch();
    VisADGroup empty = new VisADGroup();
    swit.addChild(swParent);
    swit.addChild(empty);
    swit.setWhichChild(0);
    addSwitch((DisplayRendererJ2D) getDisplayRenderer(), swit);
    toggle(getEnabled());
  }

  public void toggle(boolean on) {
    if (swit != null) {
      swit.setWhichChild(on ? 0 : 1);
      VisADCanvasJ2D canvas =
        ((DisplayRendererJ2D) getDisplayRenderer()).getCanvas();
      canvas.scratchImages();
    }
    super.toggle(on);
  }

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowFunctionTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowRealTupleTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowRealTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowSetTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowTextType(
         TextType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowTextTypeJ2D(type, link, parent);
  }

  public ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowTupleTypeJ2D(type, link, parent);
  }

  abstract void addSwitch(DisplayRendererJ2D displayRenderer,
                          VisADGroup branch) throws VisADException;

  /** re-transform if needed;
      return false if not done */
  public boolean doAction() throws VisADException, RemoteException {
    VisADGroup branch;
    boolean all_feasible = get_all_feasible();
    boolean any_changed = get_any_changed();
    boolean any_transform_control = get_any_transform_control();
    boolean scratch = false;
    if (all_feasible && (any_changed || any_transform_control)) {
      // exceptionVector.removeAllElements();
      clearAVControls();
      try {
        // doTransform creates a VisADGroup from a Data object
        branch = doTransform();
      }
      catch (OutOfMemoryError e) {
        // System.out.println("OutOfMemoryError, try again ...");
        try {
          if (swParent.numChildren() > 0) {
            swParent.removeChild(0);
          }
          branch = null;
          Runtime.getRuntime().gc();
          Runtime.getRuntime().runFinalization();
          branch = doTransform();
        }
        catch (BadMappingException ee) {
          addException(ee);
          branch = null;
        }
        catch (UnimplementedException ee) {
          addException(ee);
          branch = null;
        }
        catch (RemoteException ee) {
          addException(ee);
          branch = null;
        }
        catch (DisplayInterruptException ee) {
          branch = null;
        }
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
        swParent.setChild(branch, 0);
        scratch = true;
      }
      else { // if (branch == null)
        if (swParent.numChildren() > 0) {
          swParent.removeChild(0);
          scratch = true;
        }
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
    if (scratch) {
      ((DisplayImplJ2D) getDisplay()).setScratch();
    }

/* WLH 28 Oct 98
    return all_feasible;
*/
    /* WLH 28 Oct 98 */
    return (all_feasible && (any_changed || any_transform_control));

  }

  public void clearBranch() {
    if (swParent.numChildren() > 0) {
      swParent.removeChild(0);
      VisADCanvasJ2D canvas =
        ((DisplayRendererJ2D) getDisplayRenderer()).getCanvas();
      canvas.scratchImages();
    }
  }

  public void clearScene() {
    swParent.detach();
    ((DisplayRendererJ2D) getDisplayRenderer()).clearScene(this);
    VisADCanvasJ2D canvas =
      ((DisplayRendererJ2D) getDisplayRenderer()).getCanvas();
    canvas.scratchImages();
    super.clearScene();
  }

  /** create a VisADGroup scene graph for Data in links;
      this can put Behavior objects in the scene graph for
      DataRenderer classes that implement direct manipulation widgets;
      may reduce work by only changing scene graph for Data and
      Controls that have changed:
      1. use boolean[] changed to determine which Data objects have changed
      2. if Data has not changed, then use Control.checkTicks loop like in
         prepareAction to determine which Control-s have changed */
  public abstract VisADGroup doTransform()
         throws VisADException, RemoteException; // J2D

}

