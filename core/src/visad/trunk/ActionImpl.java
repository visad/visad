//
// ActionImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

/*
Action - ThingReference event logic

ActionImpl has Vector of ReferenceActionLinks, one per linked ThingReference
ThingReferenceImpl has Vector of ThingChangedLinks, one per linked Action



call stacks:
  // create and send ThingChangedEvent
  ThingReferenceImpl.incTick() calls
    ThingChangedLink.queueThingChangedEvent('new' ThingChangedEvent e) calls
      Action.thingChanged(e) calls
        ReferenceActionLink.acknowledgeThingChangedEvent(e.getTick())

  // get queued ThingChangedEvent
  ActionImpl.run() calls
    ReferenceActionLink.getThingChangedEvent() calls
      ThingReference.acknowledgeThingChanged(Action a) calls
        ThingChangedLink.acknowledgeThingChangedEvent()
    ActionImpl.thingChanged(ThingChangedEvent e) calls
      ReferenceActionLink.acknowledgeThingChangedEvent(e.getTick())

  // peek at queued ThingChangedEvent
  ActionImpl.run() calls
    ReferenceActionLink.peekThingChangedEvent() calls
      ThingReference.peekThingChanged() calls
        ThingChangedLink.peekThingChangedEvent()
*/

/**
 * ActionImpl is the abstract superclass for runnable threads that
 * need to be notified when ThingReference objects change.<P>
 *
 * ActionImpl is the superclass of DisplayImpl and CellImpl.<P>
 *
 * ActionImpl is not Serializable and should not be copied
 * between JVMs.<P>
 */
public abstract class ActionImpl
       implements Action, Runnable {

  /** thread pool and its lock */
  private transient static ThreadPool pool = null;
  private static Object poolLock = new Object();

  private boolean enabled = true;
  private Object lockEnabled = new Object();

  private boolean peek = false;

  private Thread currentActionThread = null;

  /** String name, used only for debugging */
  private String Name;

  // WLH 17 Dec 2001 - get it off Thread stack
  private Enumeration run_links = null;

  /** Vector of ReferenceActionLink-s;
      ActionImpl is not Serializable, but mark as transient anyway */
  private transient Vector LinkVector = new Vector();

  /** counter used to give a unique id to each ReferenceActionLink
      in LinkVector */
  private long link_id;

  private boolean requeue = false;

  /**
   * construct an ActionImpl
   * @param name - String name, used only for debugging
   */
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


 /** return the number of tasks in the threadpool queue 
  * @return number of queued and active tasks
 */
  public static int getTaskCount() 
  {
    if(pool == null) return 0;
    return pool.getTaskCount();
  }


  /**
   * destroy all threads after they've drained the job queue
   */
  public static void stopThreadPool()
  {
    if (pool != null) {
      pool.stopThreads();
      pool = null;
    }
  }

  /**
   * increase the maximum number of Threads allowed in the ThreadPool
   * @param num - new maximum number of Threads in ThreadPool
   * @throws Exception - num is less than previous maximum
   */
  public static void setThreadPoolMaximum(int num)
        throws Exception
  {
    if (pool == null) {
      startThreadPool();
    }
    pool.setThreadMaximum(num);
  }

  /**
   * stop activity in this ActionImpl
   */
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
    if (pool != null && !pool.isTerminated()) {
      pool.queue(this);
    }
    run_links = null;

  }

  /**
   * @return long value of long counter used to give a unique id to
   *         each linked ReferenceActionLink
   */
  synchronized long getLinkId() {
    long i = link_id;
    link_id++;
    return i;
  }

  /**
   * call setTicks() for each linked ReferenceActionLink 
   * which saves boolean flag indicating whether its incTick()
   * has been called since last setTicks()
   */
  private void setTicks() {
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        link.setTicks();
      }
    }
  }

  /**
   * @return boolean that is disjunction (or) of flags saved
   * in setTicks() calls to each linked ReferenceActionLink
   */
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

  /**
   * call resetTicks() for each linked ReferenceActionLink
   * which resets boolean flag indicating whether its incTick()
   * has been called since last setTicks()
   */
  private void resetTicks() {
    synchronized (LinkVector) {
      Enumeration links = LinkVector.elements();
      while (links.hasMoreElements()) {
        ReferenceActionLink link = (ReferenceActionLink) links.nextElement();
        link.resetTicks();
      }
    }
  }

  /**
   * enable activity in this ActionImpl and trigger any pending activity
   */
  public void enableAction() {
// System.out.println("enableAction " + getName());
    if (!enabled) peek = true;
    enabled = true;
    notifyAction();
  }

  /**
   * disable activity in this ActionImpl and if necessary wait
   * for end of current doAction() call
   */
  public void disableAction() {
// System.out.println("disableAction " + getName());
    enabled = false;
    // wait for possible current run() invocation to finish
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

  /**
   * return Thread currently active in run() method of this
   * ActionImpl, or null is run() is not active
   */
  public Thread getCurrentActionThread() {
    return currentActionThread;
  }

  /**
   * remove linked ReferenceActionLink
   * @param link - linked ReferenceActionLink to remove
   */
  void handleRunDisconnectException(ReferenceActionLink link)
  {
    LinkVector.removeElement(link);
  }

  /**
   * invoked by a Thread from the ThreadPool whenever
   * there is a request for activity in this ActionImpl
   */
  public void run() {

    // Save the current thread so we can prohibit it from calling
    // getImage.  This is thread-safe, because only one ActionImpl
    // thread can be running at a time.
    currentActionThread = Thread.currentThread();

    synchronized (lockEnabled) {
// if (getName() != null) System.out.println("ENABLED = " + enabled + " " + getName());
      if (enabled) {
        try {
          if (peek) {
            // WLH 17 Dec 2001 - keep run_links off Thread stack
            synchronized (LinkVector) {
              run_links = ((Vector) LinkVector.clone()).elements();
            }
            while (run_links.hasMoreElements()) {
              ReferenceActionLink link =
                (ReferenceActionLink) run_links.nextElement();
  
              try {
                link.peekThingChangedEvent();
              } catch (RemoteException re) {
                if (!visad.collab.CollabUtil.isDisconnectException(re)) {
                  throw re;
                }
  
                // remote side has died
                handleRunDisconnectException(link);
              }
            }
            run_links = null;
            peek = false;
          } // end if (peek)

          setTicks();
          if (checkTicks()) {
// if (getName() != null) System.out.println("RUN " + getName());
            doAction();
          }
          // WLH 17 Dec 2001 - keep run_links off Thread stack
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
            }
          }
          run_links = null;
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

  /**
   * abstract method that implements activity of this ActionImpl
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public abstract void doAction() throws VisADException, RemoteException;

  /**
   * a linked ThingReference has changed, requesting activity
   * in this ActionImpl
   * @param e ThingChangedEvent for change to ThingReference
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
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

  /**
   * add a link to a ReferenceActionLink (and via it
   * link to a ThingReference)
   * @param link ReferenceActionLink to link to
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
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

  /**
   * trigger activity in this ActionImpl
   */
  void notifyAction() {
// if (getName() != null) DisplayImpl.printStack("notifyAction " + getName());
    requeue = true;
    if (pool == null) {
      startThreadPool();
    }
    pool.queue(this);
  }

  /**
   * wait for all queued tasks in ThreadPool to finish
   */
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

  /**
   * does essentially the same thing as addReference(), but is
   * called by the addReference() method of any RemoteActionImpl
   * that adapts this ActionImpl
   * @param ref RemoteThingReference being linked
   * @param action RemoteActionImpl adapting this ActionImpl
   * @throws ReferenceException   if the reference has already been added.
   * @throws VisADException	  if a VisAD failure occurs.
   * @throws RemoteException	  if a Java RMI failure occurs.
   */
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

  /**
   * does essentially the same thing as removeReference(), but is
   * called by the removeReference() method of any RemoteActionImpl
   * that adapts this ActionImpl
   * @param ref RemoteThingReference being removed
   * @throws ReferenceException   if the reference is not linked.
   * @throws VisADException       if a VisAD failure occurs.
   * @throws RemoteException      if a Java RMI failure occurs.
   */
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

  /** 
    * delete all links to ThingReferences 
    */
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

  /**
   * called by DisplayImpl.removeReference and
   * DisplayImpl.adaptedDisplayRemoveReference to remove links
   * @param links array of ReferenceActionLinks to remove
   * @throws VisADException       if a VisAD failure occurs.
   * @throws RemoteException      if a Java RMI failure occurs.
   */
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

  /**
   * returns a linked ReferenceActionLink with the given id
   * @param id value to search for
   * @return linked ReferenceActionLink with given id
   * @throws VisADException       if a VisAD failure occurs.
   */
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

  /**
   * @return Vector of linked ReferenceActionLinks
   */
  public Vector getLinks() {
    return (Vector) LinkVector.clone();
  }

  /**
    * @return String name of this Action
    */
  public String getName() {
    return Name;
  }

  /**
   * change the name of this Action
   * @param name new String name
   */
  public void setName(String name) {
    Name = name;
  }

}
