
//
// ActionImpl.java
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   ActionImpl is the abstract superclass for runnable threads that
   need to be notified when ThingReference objects change.  For example,
   this may be used for a Data display or for a spreadsheet cell.<P>

   ActionImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class ActionImpl extends Object
       implements Action, Runnable {

  String Name;

  /** ActionImpl is not Serializable, but mark as transient anyway */
  private transient Thread actionThread;

  /** Vector of ReferenceActionLink-s;
      ActionImpl is not Serializable, but mark as transient anyway */
  private transient Vector LinkVector = new Vector();

  /** counter used to give a unique id to each ReferenceActionLink
      in LinkVector */
  private long link_id;

  public ActionImpl(String name) {
    Name = name;
    link_id = 0;
    actionThread = new Thread(this);
    actionThread.start();
  }

  public void stop() {
    actionThread = null;
    if (LinkVector == null) return;
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        try {
          link.getThingReference().removeThingChangedListener(link.getAction());
        }
        catch (RemoteException e) {
        }
        catch (VisADException e) {
        }
      }
      LinkVector.removeAllElements();
    }
  }

  synchronized long getLinkId() {
    long i = link_id;
    link_id++;
    return i;
  }

  public void setTicks() {
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        link.setTicks();
      }
    }
  }

  public boolean checkTicks() {
    boolean doIt = false;
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        doIt |= link.checkTicks();
      }
    }
    return doIt;
  }

  public void resetTicks() {
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        link.resetTicks();
      }
    }
  }


  public void run() {
    boolean dontSleep = false;
/* WLH 28 Feb 98
   put this back once Swing-Java3D bugs are fixed
    try {
      DisplayImpl.delay(500);
    }
    catch (VisADException e) {
    }
*/
    Thread me = Thread.currentThread();
    while (actionThread == me) {
      if (!dontSleep) {
        try {
          synchronized (this) {
            wait(2000);
          }
        }
        catch(InterruptedException e) {
          // note notify generates a normal return from wait rather
          // than an Exception - control doesn't normally come here
        }
      } // end if (!dontSleep)
      try {
        dontSleep = false;
        setTicks();
        if (checkTicks() || this instanceof DisplayImpl) {
          boolean check = checkTicks();
          boolean display = (this instanceof DisplayImpl);
/*
System.out.println("doAction: Name = " + Name + " checkTicks = " + check +
                   " display = " + display);
*/
          doAction();
        }
        synchronized (LinkVector) {
          Enumeration links = LinkVector.elements();
          dontSleep = false;
          while (links.hasMoreElements()) {
            ReferenceActionLink link =
              (ReferenceActionLink) links.nextElement();
            if (link.getBall()) {
              link.setBall(false);
              ThingReference ref = link.getThingReference();
              ThingChangedEvent e =
                ref.acknowledgeThingChanged(link.getAction());
              if (e != null) {
                thingChanged(e);
                dontSleep = true;
              }
            }
          }
        }
        resetTicks();
      }
      catch(VisADException v) {
        v.printStackTrace();
        throw new VisADError("Action.run: " + v.toString());
      }
      catch(RemoteException v) {
        v.printStackTrace();
        throw new VisADError("Action.run: " + v.toString());
      }
/* WLH 20 Feb 98
      if (!dontSleep) {
        try {
          synchronized (this) {
            wait(5000);
          }
        }
        catch(InterruptedException e) {
          // note notify generates a normal return from wait rather
          // than an Exception - control doesn't normally come here
        }
      } // end if (!dontSleep)
*/
    } // end while (actionThread == me)
  }

  public abstract void doAction() throws VisADException, RemoteException;

  public void thingChanged(ThingChangedEvent e)
         throws VisADException, RemoteException {
    long id = e.getId();
    ReferenceActionLink link = findLink(id);

    if (link != null) {
      link.incTick(e.getTick());
      link.setBall(true);
      synchronized (this) {
        notify();
      }
    }
  }

  /** add a ReferenceActionLink */
  void addLink(ReferenceActionLink link)
       throws VisADException, RemoteException {
    ThingReference ref = link.getThingReference();
    if (findReference(ref) != null) {
      throw new ReferenceException("Action.addLink: link to " +
                                   "ThingReference already exists");
    }
    link.initTicks(ref.getTick());
    if (LinkVector == null) LinkVector = new Vector();
    synchronized (LinkVector) {
      LinkVector.addElement(link);
    }
    ref.addThingChangedListener(link.getAction(), link.getId());
  }

  void notifyAction() {
    synchronized (this) {
      notify();
    }
  }

  /** create link to a ThingReference;
      must be local ThingReferenceImpl */
  public void addReference(ThingReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof ThingReferenceImpl)) {
      throw new RemoteVisADException("ActionImpl.addReference: requires " +
                                     "ThingReferenceImpl");
    }
    if (findReference(ref) != null) {
      throw new TypeException("ActionImpl.addReference: link already exists");
    }
    addLink(new ReferenceActionLink(ref, this, this, getLinkId()));
    notifyAction();
  }

  /** method for use by RemoteActionImpl that adapts this ActionImpl */
  void adaptedAddReference(RemoteThingReference ref, Action action)
       throws VisADException, RemoteException {
    if (findReference(ref) != null) {
      throw new ReferenceException("ActionImpl.adaptedAddReference: " +
                                   "link already exists");
    }
    addLink(new ReferenceActionLink(ref, this, action, getLinkId()));
    notifyAction();
  }

  /** remove link to a ThingReference;
      must be local ThingReferenceImpl */
  public void removeReference(ThingReference ref)
         throws VisADException, RemoteException {
    ReferenceActionLink link = null;
    if (!(ref instanceof ThingReferenceImpl)) {
      throw new RemoteVisADException("ActionImpl.removeReference: requires " +
                                     "ThingReferenceImpl");
    }
    if (LinkVector != null) {
      synchronized (LinkVector) {
        link = findReference(ref);
        if (link == null) {
          throw new ReferenceException("ActionImpl.removeReference: " +
                                       "ThingReference not linked");
        }
        LinkVector.removeElement(link);
      }
    }
    if (link != null) ref.removeThingChangedListener(link.getAction());
    synchronized (this) {
      notify();
    }
  }

  /** method for use by RemoteActionImpl that adapts this ActionImpl */
  void adaptedRemoveReference(RemoteThingReference ref)
       throws VisADException, RemoteException {
    ReferenceActionLink link = null;
    if (LinkVector != null) {
      synchronized (LinkVector) {
        link = findReference(ref);
        if (link == null) {
          throw new ReferenceException("ActionImpl.adaptedRemoveReference: " +
                                       "ThingReference not linked");
        }
        LinkVector.removeElement(link);
      }
    }
    if (link != null) ref.removeThingChangedListener(link.getAction());
    synchronized (this) {
      notify();
    }
  }

  /** remove all ThingReferences */
  public void removeAllReferences()
         throws VisADException, RemoteException {
    Vector cloneLink = null;
    if (LinkVector != null) {
      synchronized (LinkVector) {
        cloneLink = (Vector) LinkVector.clone();
        LinkVector.removeAllElements();
      }
    }
    if (cloneLink != null) {
      Enumeration links = cloneLink.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        ThingReference ref = link.getThingReference();
        ref.removeThingChangedListener(link.getAction());
      }
    }
    synchronized (this) {
      notify();
    }
  }

  /** used by DisplayImpl.removeReference and
      DisplayImpl.adaptedDisplayRemoveReference */
  void removeLinks(ReferenceActionLink[] links)
    throws VisADException, RemoteException {
    if (LinkVector != null) {
      synchronized (LinkVector) {
        for (int i=0; i<links.length; i++) {
          if (!LinkVector.removeElement(links[i])) links[i] = null;
        }
      }
    }
    for (int i=0; i<links.length; i++) {
      if (links[i] != null) {
        ThingReference ref = links[i].getThingReference();
        ref.removeThingChangedListener(links[i].getAction());
      }
    }
    synchronized (this) {
      notify();
    }
  }

  ReferenceActionLink findLink(long id) throws VisADException {
    if (LinkVector == null) return null;
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        if (id == link.getId()) return link;
      }
    }
    return null;
  }


  /** find link to a ThingReference */
  public ReferenceActionLink findReference(ThingReference ref)
         throws VisADException {
    if (ref == null) {
      throw new ReferenceException("ActionImpl.findReference: " +
                                   "ThingReference cannot be null");
    }
    if (LinkVector == null) return null;
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        if (ref.equals(link.getThingReference())) return link;
      }
    }
    return null;
  }

  /** return vector of ReferenceActionLink-s */
  public Vector getLinks() {
/* WLH 14 Feb 98
    return LinkVector;
*/
    return (Vector) LinkVector.clone();
  }

  /** return name of this Action */
  public String getName() {
    return Name;
  }

}

