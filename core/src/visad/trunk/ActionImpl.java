//
// ActionImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.util.*;
import java.rmi.*;

import visad.util.ThreadPool;

/**
   ActionImpl is the abstract superclass for runnable threads that
   need to be notified when ThingReference objects change.  For example,
   this may be used for a Data display or for a spreadsheet cell.<P>

   ActionImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class ActionImpl
       implements Action, Runnable {

  /** thread pool and its lock */
  private transient static ThreadPool pool = null;
  private static Object poolLock = new Object();

  private boolean enabled = true;
  private Object lockEnabled = new Object();

  String Name;

  /** Vector of ReferenceActionLink-s;
      ActionImpl is not Serializable, but mark as transient anyway */
  private transient Vector LinkVector = new Vector();

  /** counter used to give a unique id to each ReferenceActionLink
      in LinkVector */
  private long link_id;

  private boolean requeue = false;

  public ActionImpl(String name) {
    // if the thread pool hasn't been initialized...
    if (pool == null) {
      startThreadPool();
    }

    Name = name;
    link_id = 0;
  }

  /** used internally to create the shared Action thread pool */
  private static void startThreadPool()
  {
    synchronized (poolLock) {
      if (pool == null) {
        // ...fill the pool; die if pool wasn't created
        try {
          pool = new ThreadPool("ActionThread");
        } catch (Exception e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
          System.exit(1);
        }
      }
    }
  }

  /** destroy all threads after they've drained the job queue */
  public static void stopThreadPool()
  {
    if (pool != null) {
      pool.stopThreads();
      pool = null;
    }
  }

  /** increase the maximum number of threads allowed for the thread pool */
  public static void setThreadPoolMaximum(int num)
        throws Exception
  {
    if (pool == null) {
      startThreadPool();
    }
    pool.setThreadMaximum(num);
  }

  public void stop() {
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

  private void setTicks() {
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

  private void resetTicks() {
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        link.resetTicks();
      }
    }
  }

  /** enable and notify this Action */
  public void enableAction() {
    enabled = true;
    notifyAction();
  }

  /** disable this Action and if necessary wait for end of doAction */
  public void disableAction() {
    enabled = false;
    synchronized (lockEnabled) {
      enabled = false; // probably not necessary, just don't trust a nop
    }
  }

  /** code executed by a thread to manage updates to the corresponding Thing */
  public void run() {
    synchronized (lockEnabled) {
      if (enabled) {
        try {
          setTicks();
          if (checkTicks()) {
            doAction();
          }
          Enumeration links;
          synchronized (LinkVector) {
            links = ((Vector) LinkVector.clone()).elements();
          }
          while (links.hasMoreElements()) {
            ReferenceActionLink link =
              (ReferenceActionLink) links.nextElement();
            ThingChangedEvent e = link.getThingChangedEvent();
            if (e != null) {
              thingChanged(e);
              requeue = true;
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
      } // end if (enabled)

      // if there's more to do, add this to the end of the task list
      if (requeue) {
        if (pool != null) {
          pool.queue(this);
        }
        requeue = false;
      }

    } // end synchronized (lockEnabled)
  }

  public abstract void doAction() throws VisADException, RemoteException;

  public boolean thingChanged(ThingChangedEvent e)
         throws VisADException, RemoteException {
    long id = e.getId();
    ReferenceActionLink link = findLink(id);

    boolean changed = true;
    if (link != null) {
      link.acknowledgeThingChangedEvent(e.getTick());
      notifyAction();
      changed = false;
    }

    return changed;
  }

  /** add a ReferenceActionLink */
  void addLink(ReferenceActionLink link)
       throws VisADException, RemoteException {
    ThingReference ref = link.getThingReference();
    if (findReference(ref) != null) {
      throw new ReferenceException("Action.addLink: link to " +
                                   "ThingReference already exists");
    }

// WLH 4 Dec 98 - moved this above LinkVector stuff
    ref.addThingChangedListener(link.getAction(), link.getId());

    if (LinkVector == null) LinkVector = new Vector();
    synchronized (LinkVector) {
      LinkVector.addElement(link);
    }
  }

  void notifyAction() {
    requeue = true;
    if (pool == null) {
      startThreadPool();
    }
    pool.queue(this);
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
      throw new ReferenceException("ActionImpl.addReference: " +
                                   "link already exists");
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
    notifyAction();
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
    notifyAction();
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
    notifyAction();
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
    notifyAction();
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

