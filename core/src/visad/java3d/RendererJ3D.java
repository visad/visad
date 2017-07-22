//
// RendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java3d;

import visad.*;
import visad.util.Delay;

import org.jogamp.java3d.*;

import java.util.*;
import java.rmi.*;
import java.awt.Image;


/**
   RendererJ3D is the VisAD abstract super-class for graphics rendering
   algorithms under Java3D.  These transform Data objects into 3-D
   (or 2-D) depictions in a Display window.<P>

   RendererJ3D is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class RendererJ3D extends DataRenderer {

  /** switch is parent of any BranchGroups created by this */
  Switch sw = null;
  /** parent of sw for 'detach' */
  BranchGroup swParent = null;
  /** index of current 'intended' child of Switch sw;
      not necessarily == sw.getWhichChild() */
  /** currentIndex is always = 0; this logic is a vestige of a
      workaround for an old (circa 1998) bug in Java3D */
  private static final int currentIndex = 0;
  BranchGroup[] branches = null;
  boolean[] switchFlags = {false, false, false};
  boolean[] branchNonEmpty = {false, false, false};

  
  public RendererJ3D() {
    super();
  }

  public void setLinks(DataDisplayLink[] links, DisplayImpl d)
       throws VisADException {
    if (getDisplay() != null || getLinks() != null) {
      throw new DisplayException("RendererJ3D.setLinks: already set\n" +
                                 "you are probably re-using a DataRenderer");
    }
    if (!(d instanceof DisplayImplJ3D)) {
      throw new DisplayException("RendererJ3D.setLinks: must be DisplayImplJ3D");
    }
    setDisplay(d);
    setDisplayRenderer(d.getDisplayRenderer());
    setLinks(links);

    // set up switch logic for clean BranchGroup replacement
    Switch swt = new Switch(); // J3D
    swt.setCapability(Group.ALLOW_CHILDREN_READ);
    swt.setCapability(Group.ALLOW_CHILDREN_WRITE);
    swt.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    swt.setCapability(Switch.ALLOW_SWITCH_READ);
    swt.setCapability(Switch.ALLOW_SWITCH_WRITE);

    swParent = new BranchGroup();
    swParent.setCapability(BranchGroup.ALLOW_DETACH);
    swParent.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    swParent.addChild(swt);
    // make it 'live'
    addSwitch((DisplayRendererJ3D) getDisplayRenderer(), swParent);

    branches = new BranchGroup[3];
    for (int i=0; i<3; i++) {
      branches[i] = new BranchGroup();
      branches[i].setCapability(Group.ALLOW_CHILDREN_READ);
      branches[i].setCapability(Group.ALLOW_CHILDREN_WRITE);
      branches[i].setCapability(Group.ALLOW_CHILDREN_EXTEND);
      swt.addChild(branches[i]);
    }
/*
System.out.println("setLinks: sw.setWhichChild(" + currentIndex + ")");
*/
    swt.setWhichChild(currentIndex);
    sw = swt; // avoid IndexOutOfBoundsException in toggle()
    toggle(getEnabled());
  }

  public void toggle(boolean on) {
    if (sw != null) sw.setWhichChild(on ? currentIndex : ((currentIndex+1)%3));
    super.toggle(on);
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
    if (branches == null) return false;
    BranchGroup branch; // J3D
    boolean all_feasible = get_all_feasible();
    boolean any_changed = get_any_changed();
    boolean any_transform_control = get_any_transform_control();
/*
System.out.println("doAction " + getDisplay().getName() + " " +
                   getLinks()[0].getThingReference().getName() +
                   " any_changed = " + any_changed +
                   " all_feasible = " + all_feasible +
                   " any_transform_control = " + any_transform_control);
*/
    if (all_feasible && (any_changed || any_transform_control)) {
/*
System.out.println("doAction " + getDisplay().getName() + " " +
                   getLinks()[0].getThingReference().getName() +
                   " any_changed = " + any_changed +
                   " all_feasible = " + all_feasible +
                   " any_transform_control = " + any_transform_control);
*/
      // exceptionVector.removeAllElements();
      clearAVControls();
      try {
        // doTransform creates a BranchGroup from a Data object
        branch = doTransform();
      }
      catch (OutOfMemoryError e) {
        // System.out.println("OutOfMemoryError, try again ...");
        clearBranch();
        branch = null;
        new Delay(250);
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        try {
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
        synchronized (this) {
          if (!branchNonEmpty[currentIndex] ||
              branches[currentIndex].numChildren() == 0) {
            /* WLH 18 Nov 98 */
            branches[currentIndex].addChild(branch);
            branchNonEmpty[currentIndex] = true;
          }
          else { // if (branchNonEmpty[currentIndex])
            if (!(branches[currentIndex].getChild(0) == branch)) {// TDR, Nov 02
              flush(branches[currentIndex]);
              branches[currentIndex].setChild(branch, 0);
            }
          } // end if (branchNonEmpty[currentIndex])
        } // end synchronized (this)
      }
      else { // if (branch == null)

        // WLH 31 March 99
        clearBranch();

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
    return (all_feasible && (any_changed || any_transform_control));
  }

  public BranchGroup getBranch() {
    synchronized (this) {
      if (branches != null && branchNonEmpty[currentIndex] &&
          branches[currentIndex].numChildren() > 0) {
        return (BranchGroup) branches[currentIndex].getChild(0);
      }
      else {
        return null;
      }
    }
  }

  public void setBranchEarly(BranchGroup branch) {
    if (branches == null) return;
    // needed (?) to avoid NullPointerException
    ShadowTypeJ3D shadow = (ShadowTypeJ3D) (getLinks()[0].getShadow());
    shadow.ensureNotEmpty(branch);

    synchronized (this) {
      if (!branchNonEmpty[currentIndex] ||
          branches[currentIndex].numChildren() == 0) {
        /* WLH 18 Nov 98 */
        branches[currentIndex].addChild(branch);
        branchNonEmpty[currentIndex] = true;
      }
      else { // if (branchNonEmpty[currentIndex])
        if (!(branches[currentIndex].getChild(0) == branch)) {
          flush(branches[currentIndex]);
          branches[currentIndex].setChild(branch, 0);
        }
      } // end if (branchNonEmpty[currentIndex])
    } // end synchronized (this)
  }

  public void clearBranch() {
    if (branches == null) return;
    synchronized (this) {
      if (branchNonEmpty[currentIndex]) {
        flush(branches[currentIndex]);
        Enumeration ch = branches[currentIndex].getAllChildren();
        while(ch.hasMoreElements()) {
          BranchGroup b = (BranchGroup) ch.nextElement();
          b.detach();
        }
      }
      branchNonEmpty[currentIndex] = false;
    }
  }

  public void flush(Group branch) {
    if (branches == null) return;
    Enumeration ch = branch.getAllChildren();
    while(ch.hasMoreElements()) {
      Node n = (Node) ch.nextElement();
      if (n instanceof Group) {
        flush((Group) n);
      }
      else if (n instanceof Shape3D &&
               ((Shape3D) n).getCapability(Shape3D.ALLOW_APPEARANCE_READ)) {
        Appearance appearance = ((Shape3D) n).getAppearance();
        if (appearance != null &&
            appearance.getCapability(Appearance.ALLOW_TEXTURE_READ)) {
          Texture texture = appearance.getTexture();
          if (texture != null &&
              texture.getCapability(Texture.ALLOW_IMAGE_READ)) {
            ImageComponent ic = texture.getImage(0);
            if (ic != null && ic.getCapability(ImageComponent.ALLOW_IMAGE_READ)) {
              if (ic instanceof ImageComponent2D) {
                Image image = ((ImageComponent2D) ic).getImage();
                if (image != null) image.flush();
// System.out.println("flush");
              }
              else if (ic instanceof ImageComponent3D) {
                Image[] images = ((ImageComponent3D) ic).getImage();
                if (images != null) {
                  for (int j=0; j<images.length; j++) {
                    if (images[j] != null) images[j].flush();
// System.out.println("flush");
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  public void clearScene() {
    if (branches == null) return;
    flush(swParent);
    swParent.detach();
    ((DisplayRendererJ3D) getDisplayRenderer()).clearScene(this);
    branches = null;
    sw = null;
    swParent = null;
    super.clearScene();
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
         throws VisADException, RemoteException;

}

