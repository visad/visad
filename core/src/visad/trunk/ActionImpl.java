//
// ActionImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

  private Thread currentActionThread = null;

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

    // WLH 17 Dec 2001
    if (pool != null) {
      pool.queue(this);
    }
    run_links = null;

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

  /**
   * Set the "enabled" state of this action.  This may be used in code like the
   * following to ensure that the action has the same "enabled" state on leaving
   * the code as it did on entering it:
   * <BLOCKQUOTE>
   * <PRE><CODE>
   * ActionImpl action = ...;
   * boolean wasEnabled = action.setEnabled(false);
   * ...
   * action.setEnabled(wasEnabled);
   * </CODE></PRE>
   * </BLOCKQUOTE>
   * @param enable		The new "enabled" state for this action.
   * @return			The previous "enabled" state of this action.
   */
  public boolean setEnabled(boolean enable) {
    boolean	wasEnabled;
    synchronized (lockEnabled) {
      wasEnabled = enabled;
      if (enable && !wasEnabled) {
	enableAction();
      }
      else if (!enable && wasEnabled) {
	disableAction();
      }
    }
    return wasEnabled;
  }

  public Thread getCurrentActionThread() {
    return currentActionThread;
  }

  void handleRunDisconnectException(ReferenceActionLink link)
  {
    LinkVector.removeElement(link);
  }

  Enumeration run_links = null; // WLH 17 Dec 2001 - get it off Thread stack

  /** code executed by a thread to manage updates to the corresponding Thing */
  public void run() {

    // Save the current thread so we can prohibit it from calling
    // getImage.  This is thread-safe, because only one ActionImpl
    // thread can be running at a time.
    currentActionThread = Thread.currentThread();

    synchronized (lockEnabled) {
// if (getName() != null) System.out.println("ENABLED = " + enabled + " " + getName());
      if (enabled) {
        try {
          setTicks();
          if (checkTicks()) {
// if (getName() != null) System.out.println("RUN " + getName());
            doAction();
          }
          // Enumeration run_links; // WLH 17 Dec 2001 - get it off Thread stack
          synchronized (LinkVector) {
            run_links = ((Vector) LinkVector.clone()).elements();
          }
          while (run_links.hasMoreElements()) {
            ReferenceActionLink link =
              (ReferenceActionLink) run_links.nextElement();

            ThingChangedEvent e;
            try {
              e = link.getThingChangedEvent();
            } catch (RemoteException re) {
              if (!visad.collab.CollabUtil.isDisconnectException(re)) {
                throw re;
              }

              // remote side has died
              handleRunDisconnectException(link);
              e = null;
            }

            if (e != null) {
              thingChanged(e);
              // requeue = true;  **** WLH 20 Feb 2001 ****
            }
          }
          run_links = null; // WLH 17 Dec 2001
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
// if (getName() != null) System.out.println("requeue " + getName());
          pool.queue(this);
        }
        requeue = false;
      }

    } // end synchronized (lockEnabled)
    currentActionThread = null;
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
// if (getName() != null) DisplayImpl.printStack("notifyAction " + getName());
    requeue = true;
    if (pool == null) {
      startThreadPool();
    }
    pool.queue(this);
  }

  /** wait for currently-running actions to finish */
  public void waitForTasks()
  {
    if (pool != null) {
      pool.waitForTasks();
    }
  }

  /**
   * Creates a link to a ThingReference.  Note that this method causes this
   * object to register itself with the ThingReference.
   * @param ref                   The ThingReference to which to create
   *                              the link.  Subsequent invocation of
   *                              <code>thingChanged(ThingChangedEvent)</code>
   *                              causes invocation of
   *                              <code>ref.acknowledgeThingChanged(this)</code>
   *                              .  This method invokes <code>
   *                              ref.addThingChangedListener(this, ...)</code>.
   * @throws RemoteVisADException if the reference isn't a {@link 
   *                              ThingReferenceImpl}.
   * @throws ReferenceException   if the reference has already been added.
   * @throws VisADException	  if a VisAD failure occurs.
   * @throws RemoteException	  if a Java RMI failure occurs.
   * @see #thingChanged(ThingChangedEvent)
   * @see ThingReference#addThingChangedListener(ThingChangedListener, long)
   */
  public void addReference(ThingReference ref)
      throws ReferenceException, RemoteVisADException, VisADException,
	RemoteException {
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

  /**
   * <p>Removes a link to a ThingReference.</p>
   *
   * <p>This implementation invokes {@link #findReference(ThingReference)}.</p>
   *
   * @param ref                   The reference to be removed.
   * @throws RemoteVisADException if the reference isn't a {@link 
   *                              ThingReferenceImpl}.
   * @throws ReferenceException   if the reference isn't a part of this 
   *                              instance.
   * @throws VisADException       if a VisAD failure occurs.
   * @throws RemoteException      if a Java RMI failure occurs.
   */
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
        try {
          ref.removeThingChangedListener(links[i].getAction());
        } catch (RemoteException re) {
          // don't throw exception if the other side has died
          if (!visad.collab.CollabUtil.isDisconnectException(re)) {
            throw re;
          }
        }
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


  /**
   * Returns the link associated with a ThingReference.
   *
   * @param ref                 The reference to find.
   * @return                    The link associated with the reference.
   * @throws ReferenceException if the argument is <code>null</code>.
   * @throws VisADException     if the argument is <code>null</code>.
   */
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

  /** change name of this Action */
  public void setName(String name) {
    Name = name;
  }
}
