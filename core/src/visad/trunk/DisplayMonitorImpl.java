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

import java.rmi.ConnectException;
import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;

import visad.util.ObjectCache;
import visad.util.ThreadPool;

/**
 * <CODE>DisplayMonitorImpl</CODE> is the <CODE>Display</CODE> monitor
 * implementation.<P>
 * <CODE>DisplayMonitorImpl</CODE> is not <CODE>Serializable</CODE> and
 * should not be copied between JVMs.<P>
 */
public class DisplayMonitorImpl
  implements DisplayMonitor
{
  private int nextListenerID = 0;

  /**
   * Creates a new wrapper for a <CODE>DisplayMonitorListener</CODE>
   * which is mapped to a unique ID.
   *
   * @param listener The <CODE>DisplayMonitorListener</CODE> to wrap.
   */
  private synchronized Listener
    getNextListener(DisplayMonitorListener listener)
  {
    return new Listener(listener, nextListenerID++);
  }

  /**
   * The event delivery thread pool and its lock.
   */
  private transient static ThreadPool listenerPool = null;
  private static Object listenerPoolLock = new Object();

  /**
   * Creates the shared thread pool.
   */
  private static void startThreadPool()
  {
    synchronized (listenerPoolLock) {
      if (listenerPool == null) {
        // ...fill the pool; die if pool wasn't created
        try {
          listenerPool = new ThreadPool("ListenerThread");
        } catch (Exception e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
          System.exit(1);
        }
      }
    }
  }

  /**
   * Destroys all threads after they've drained the job queue.
   */
  public static void stopThreadPool()
  {
    if (listenerPool != null) {
      listenerPool.stopThreads();
      listenerPool = null;
    }
  }

  /**
   * Increases the maximum number of threads allowed for the thread pool.
   *
   * @param num The new maximum number of threads.
   *
   * @exception Exception If there was a problem with the specified value.
   */
  public static void setThreadPoolMaximum(int num)
    throws Exception
  {
    if (listenerPool == null) {
      startThreadPool();
    }
    listenerPool.setThreadMaximum(num);
  }

  /**
   * <CODE>EventComparator</CODE> is used to sort the event table just
   * before delivering it to a remote <CODE>DisplayMonitorListener</CODE>.
   */
  class EventComparator
    implements Comparator
  {
    private Hashtable table;

    /**
     * Creates a new comparison class for the given <CODE>Hashtable</CODE>
     *
     * @param tbl The <CODE>Hashtable</CODE> to use for comparisons.
     */
    EventComparator(Hashtable tbl)
    {
      table = tbl;
    }

    /**
     * Sorts in reverse order of creation.
     *
     * @param o1 the first object
     * @param o2 the second object
     */
    public int compare(Object o1, Object o2)
    {
      return (((MonitorEvent )(table.get(o2))).getSequenceNumber() -
              ((MonitorEvent )(table.get(o1))).getSequenceNumber());
    }

    /**
     * Returns <CODE>true</CODE> if the specified object is
     * an <CODE>EventComparator</CODE>.
     *
     * @param obj The object to examine.
     */
    public boolean equals(Object obj)
    {
      return (obj instanceof EventComparator);
    }
  }

  /**
   * Used as key for ControlEvents in listener queue
   */
  class ControlEventKey
  {
    private Class cclass;
    private int instance;

    ControlEventKey(Control ctl)
    {
      cclass = ctl.getClass();
      instance = ctl.getInstanceNumber();
    }

    public boolean equals(ControlEventKey key)
    {
      return instance == key.instance && cclass.equals(key.cclass);
    }

    public boolean equals(Object obj)
    {
      if (!(obj instanceof ControlEventKey)) {
        return false;
      }
      return equals((ControlEventKey )obj);
    }

    public String toString() { return cclass.getName() + "#" + instance; }
  }

  /**
   * <CODE>Listener</CODE> is an encapsulation of all the data related to
   * delivering events to a single <CODE>DIsplayMonitorListener</CODE>.
   */
  class Listener
    implements Runnable
  {
    private int id;
    private DisplayMonitorListener listener;
    private boolean haveThread = false;
    private boolean dead = false;

    private Object tableLock = new Object();
    private Hashtable table = new Hashtable();
    private Hashtable diverted = null;

    /**
     * Creates a new <CODE>Listener</CODE> with the specified ID.
     *
     * @param l The listener to which events will be sent.
     * @param n The unique identifier for this listener.
     */
    public Listener(DisplayMonitorListener l, int n)
    {
      listener = l;
      id = n;

      if (listenerPool == null) {
        startThreadPool();
      }
    }

    /**
     * Starts event delivery.
     */
    private synchronized void startDelivery()
    {
      if (haveThread) {
        return;
      }

      // remember that we've started a thread
      haveThread = true;

      if (listenerPool == null) {
        startThreadPool();
      }
      listenerPool.queue(this);
    }

    /**
     * Adds the event to the specified table.
     *
     * @param tbl The <CODE>Hashtable</CODE> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(Hashtable tbl, ControlMonitorEvent evt)
    {
      tbl.put(new ControlEventKey(evt.getControl()), evt);
    }

    /**
     * Adds the event to the specified table.
     *
     * @param tbl The <CODE>Hashtable</CODE> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(Hashtable tbl, MapMonitorEvent evt)
    {
      Object key = evt.getMap();
      if (key == null) {
        if (evt.getType() != MonitorEvent.MAPS_CLEARED) {
          System.err.println("Got null map for " + evt);
          return;
        }

        key = "null";
      }

      tbl.put(key, evt);
    }

    /**
     * Adds the event to the specified table.
     *
     * @param tbl The <CODE>Hashtable</CODE> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(Hashtable tbl, ReferenceMonitorEvent evt)
    {
      tbl.put(evt.getLink(), evt);
    }

    /**
     * Adds a generic <CODE>MonitorEvent</CODE> to the specified table.
     * (This method simply forwards the event to the
     * <CODE>addEventToTable</CODE> method for this specific
     * <CODE>MonitorEvent</CODE> type.)
     *
     * @param tbl The <CODE>Hashtable</CODE> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(Hashtable tbl, MonitorEvent evt)
    {
      if (evt instanceof ControlMonitorEvent) {
        addEventToTable(tbl, (ControlMonitorEvent )evt);
      } else if (evt instanceof MapMonitorEvent) {
        addEventToTable(tbl, (MapMonitorEvent )evt);
      } else if (evt instanceof ReferenceMonitorEvent) {
        addEventToTable(tbl, (ReferenceMonitorEvent )evt);
      } else {
        System.err.println("Unknown MonitorEvent type " +
                           evt.getClass().getName());
      }
    }

    /**
     * Delivers the event if possible, otherwise just queues it up for
     * later delivery.
     *
     * @param evt The event to deliver.
     */
    public void addEvent(MonitorEvent evt)
    {
      int evtID = evt.getOriginator();
      if (evtID == id) {
        return;
      }

      Hashtable tbl;
      synchronized (tableLock) {
        if (haveThread) {
          if (diverted == null) {
            diverted = new Hashtable();
          }
          addEventToTable(diverted, evt);
        } else {
          addEventToTable(table, evt);
          startDelivery();
        }
      }
    }

    /**
     * Gets the unique identifier for this <CODE>Listener</CODE>.
     */
    public int getID() { return id; }

    /**
     * Gets the actual <CODE>DisplayMonitorListener</CODE> for this
     * <CODE>Listener</CODE>.
     */
    public DisplayMonitorListener getListener() { return listener; }

    /**
     * Returns <CODE>true</CODE> if there wass already a
     * <CODE>MonitorEvent</CODE> queued for the specified
     * <CODE>Control</CODE>.
     *
     * @param ctl The <CODE>Control</CODE>.
     */
    public boolean hasControlEventQueued(Control ctl)
    {
      Object key = new ControlEventKey(ctl);
      if (table != null && table.containsKey(key)) {
        return true;
      }
      if (diverted != null && diverted.containsKey(key)) {
        return true;
      }

      return false;
    }

    /**
     * Returns <CODE>true</CODE> if there were diverted events
     *  to be delivered
     */
    private boolean undivertEvents()
    {
      final boolean undivert;
      synchronized (tableLock) {
        // if there are events queued, restore them to the main table
        undivert = (diverted != null);
        if (undivert) {
          table = diverted;
          diverted = null;
        }
      }

      return undivert;
    }

    /**
     * Returns <CODE>true</CODE> if this Listener is dead.
     */
    public boolean isDead() { return dead; }

    /**
     * Delivers events to the remote listener.
     */
    public void run()
    {
      boolean delivered = false;
      int attempts = 0;
      do {
        try {
          deliverEventTable();
          delivered = true;
        } catch (ConnectException ce) {
          if (attempts++ >= 10) {
            // if we failed to connect for 10 times, give up
            break;
          }
          try { Thread.sleep(500); } catch (InterruptedException ie) { }
        } catch (RemoteException re) {
          re.printStackTrace();
        }
      } while (!delivered || undivertEvents());

      // mark this listener as dead
      if (!delivered) {
        dead = true;
      }

      // indicate that the thread has exited
      haveThread = false;
    }

    /**
     * Tries to deliver the queued events to this listener
     *
     * @exception ConnectException If a connection could not be made to the
     * 					remote listener.
     */
    private void deliverEventTable()
      throws ConnectException
    {
      // build the array of events
      Object[] list = new Object[table.size()];
      Enumeration enum = table.keys();
      for (int i = list.length - 1; i >= 0; i--) {
        if (enum.hasMoreElements()) {
          list[i] = enum.nextElement();
        } else {
          list[i] = null;
        }
      }

      // sort events by order of creation
      Arrays.sort(list, new EventComparator(table));

      // deliver events
      int len = list.length;
      for (int i = len - 1; i >= 0; i--) {
        MonitorEvent evt = (MonitorEvent )table.remove(list[i]);
        MonitorEvent e2;
        if (len > 1) {
          e2 = (MonitorEvent )evt.clone();
        } else {
          e2 = evt;
        }
        e2.setOriginator(id);
        try {
          listener.stateChanged(e2);
        } catch (ConnectException ce) {
          // let caller handle ConnectExceptions
          throw ce;
        } catch (Throwable t) {
          // whine loudly about all other Exceptions
          t.printStackTrace();
        }
      }
    }
  }

  /**
   * The name of this display monitor.
   */
  private String Name;

  /**
   * The <CODE>Display</CODE> being monitored.
   */
  private transient DisplayImpl myDisplay;

  /**
   * The list of objects interested in changes to the monitored
   * <CODE>Display</CODE>.
   */
  private transient Vector ListenerVector = new Vector();

  /**
   * The queue of events received from listeners -- used to suppress
   * events generated due to changes in remote displays.
   */
  private transient ObjectCache objCache;

  /**
   * Creates a monitor for the specified <CODE>Display</CODE>.
   *
   * @param dpy The <CODE>Display</CODE> to monitor.
   *
   * @exception VisADException If the <CODE>Display</CODE> encountered
   * 				problems adding this new object as a
   * 				<CODE>DisplayListener</CODE>.
   */
  public DisplayMonitorImpl(DisplayImpl dpy)
    throws VisADException
  {
    Name = dpy.getName() + " Monitor";
    dpy.addDisplayListener(this);
    myDisplay = dpy;

    objCache = new ObjectCache(Name);
  }

  /**
   * Adds the specified listener to receive <CODE>MonitorEvent</CODE>s
   * when the monitored <CODE>Display</CODE>'s state changes.
   *
   * @param listener The listener to add.
   * @param id The unique listener identifier.
   *
   * @exception VisADException If the listener <CODE>Vector</CODE>
   * 				is uninitialized.
   */
  public void addListener(DisplayMonitorListener listener, int id)
    throws VisADException
  {
    if (ListenerVector == null) {
      throw new VisADException("Null DisplayMonitor Listener Vector");
    }

    ListenerVector.addElement(new Listener(listener, id));
  }

  /**
   * Returns a suggestion for a unique listener identifier which is
   * equal to or greater than the supplied ID.
   *
   * @param id The identifier to check.
   */
  public int checkID(int id)
  {
    ListIterator iter = ListenerVector.listIterator();
    while (iter.hasNext()) {
      Listener li = (Listener )iter.next();
      if (li.getID() == id) {
        if (nextListenerID == id) {
          nextListenerID++;
        }
        return nextListenerID++;
      }
    }

    return id;
  }

  /**
   * Negotiates a listener identifier which is unique for both this
   * <CODE>DisplayMonitor</CODE> and the remote <CODE>DisplayMonitor</CODE>.
   *
   * @param rdm The remote <CODE>DisplayMonitor</CODE> to negotiate with.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If negotiations failed to converge
   * 					upon a common ID.
   */
  private int negotiateUniqueID(RemoteDisplayMonitor rdm)
    throws RemoteException, RemoteVisADException
  {
    // maximum number of rounds of ID negotiation
    final int MAX_ROUNDS = 20;

    int id;
    int rmtID = nextListenerID++;
    int round = 0;
    do {
      id = rmtID;
      rmtID = rdm.checkID(id);
      if (rmtID != id) {
        id = checkID(rmtID);
      }
      round++;
    } while (id != rmtID && round < MAX_ROUNDS);

    if (round >= MAX_ROUNDS) {
      throw new RemoteVisADException("ID negotiation failed");
    }

    return id;
  }

  /**
   * Initializes links so that <CODE>MonitorEvent</CODE>s will be
   * exchanged with the specified remote <CODE>Display</CODE>.
   *
   * @param rd The remote <CODE>Display</CODE> to synchronize.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If the inter-<CODE>Display</CODE>
   * 					links could not be made.
   */
  public void addRemoteListener(RemoteDisplay rd)
    throws RemoteException, RemoteVisADException
  {
    RemoteDisplayMonitor rdm = rd.getRemoteDisplayMonitor();
    int id = negotiateUniqueID(rdm);

    DisplaySyncImpl dsi = (DisplaySyncImpl )myDisplay.getDisplaySync();
    RemoteDisplaySyncImpl wrap = new RemoteDisplaySyncImpl(dsi);
    try {
      rdm.addListener(wrap, id);
    } catch (Exception e) {
      throw new RemoteVisADException("Couldn't make this object" +
                                     " a listener for the remote display");
    }

    boolean unwind = false;
    try {
      addListener(rd.getRemoteDisplaySync(), id);
    } catch (Exception e) {
      unwind = true;
    }

    if (unwind) {
      removeListener(wrap);
      throw new RemoteVisADException("Couldn't add listener for the" +
                                     " remote display to this object");
    }
  }

  /**
   * Stops forwarding <CODE>MonitorEvent</CODE>s to the specified listener.
   *
   * @param l Listener to remove.
   */
  public void removeListener(DisplayMonitorListener l)
  {
    if (l != null && ListenerVector != null) {
      ListIterator iter = ListenerVector.listIterator();
      while (iter.hasNext()) {
        Listener li = (Listener )iter.next();
        if (li.getListener().equals(l)) {
          iter.remove();
          break;
        }
      }
    }
  }

  /**
   * Returns <CODE>true</CODE> if there are listeners for this display.
   */
  public boolean hasListeners()
  {
    return (ListenerVector != null && ListenerVector.size() > 0);
  }

  /**
   * Forwards the <CODE>MonitorEvent</CODE> to all the listeners
   * associated with this <CODE>DisplayMonitor</CODE>.
   *
   * @param evt The event to forward.
   */
  public void notifyListeners(MonitorEvent evt)
  {
    if (evt == null) {
      return;
    }

    boolean cached = objCache.isCached(evt);
    if (cached) {
      return;
    }

    ListIterator iter = ListenerVector.listIterator();
    while (iter.hasNext()) {
      Listener li = (Listener )iter.next();
      if (li.isDead()) {
        iter.remove();
      } else {
        li.addEvent(evt);
      }
    }
  }

  /**
   * Caches this (remotely-originated) <CODE>MonitorEvent</CODE>
   * for future reference.
   *
   * @param evt The event to cache.
   */
  public void addRemoteMonitorEvent(MonitorEvent evt)
  {
    objCache.add(evt);
  }

  /**
   * Returns <CODE>true</CODE> if there is a <CODE>MonitorEvent</CODE>
   * for the specified <CODE>Control</CODE> waiting to be delivered to the
   * listener with the specified id.
   *
   * @param listenerID The identifier for the listener.
   * @param ctl The <CODE>Control</CODE> being found.
   */
  public boolean hasEventQueued(int listenerID, Control ctl)
  {
    if (ctl != null) {
      ListIterator iter = ListenerVector.listIterator();
      while (iter.hasNext()) {
        Listener li = (Listener )iter.next();
        if (li.getID() == listenerID) {
          return li.hasControlEventQueued(ctl);
        }
      }
    }

    return false;
  }

  /**
   * Handles <CODE>Control</CODE> changes.<BR><BR>
   * If the <CODE>ControlEvent</CODE> is not ignored, a
   * <CODE>ControlMonitorEvent</CODE> will be sent to all listeners.
   *
   * @param e The details of the <CODE>Control</CODE> change.
   */
  public void controlChanged(ControlEvent e)
  {
    // don't bother if nobody's listening
    if (!hasListeners()) {
      return;
    }

    if (e.getControl() instanceof AnimationControl) {
      // ignore all animation-related events
      return;
    }

    try {
      notifyListeners(new ControlMonitorEvent(MonitorEvent.CONTROL_CHANGED,
                                           (Control )e.getControl().clone()));
    } catch (VisADException ve) {
    }
  }

  /**
   * Handles notification of objects being added to or removed from
   * the <CODE>Display</CODE>.<BR><BR>
   * If the <CODE>DisplayEvent</CODE> is not ignored, a
   * <CODE>MapMonitorEvent</CODE> or <CODE>ReferenceMonitorEvent</CODE>
   * will be sent to all listeners.
   *
   * @param e The details of the <CODE>Display</CODE> change.
   */
  public void displayChanged(DisplayEvent e)
  {
    // don't bother if nobody's listening
    if (!hasListeners()) {
      return;
    }

    switch (e.getId()) {
    case DisplayEvent.MOUSE_PRESSED:
    case DisplayEvent.TRANSFORM_DONE:
    case DisplayEvent.FRAME_DONE:
    case DisplayEvent.MOUSE_PRESSED_CENTER:
    case DisplayEvent.MOUSE_PRESSED_LEFT:
    case DisplayEvent.MOUSE_PRESSED_RIGHT:
    case DisplayEvent.MOUSE_RELEASED:
    case DisplayEvent.MOUSE_RELEASED_CENTER:
    case DisplayEvent.MOUSE_RELEASED_LEFT:
    case DisplayEvent.MOUSE_RELEASED_RIGHT:
      break;
    case DisplayEvent.MAP_ADDED:
      ScalarMap map = ((DisplayMapEvent )e).getMap();

      try {
        notifyListeners(new MapMonitorEvent(MonitorEvent.MAP_ADDED,
                                         (ScalarMap )map.clone()));
      } catch (VisADException ve) {
      }
      break;
    case DisplayEvent.MAPS_CLEARED:
      try {
        notifyListeners(new MapMonitorEvent(MonitorEvent.MAPS_CLEARED, null));
      } catch (VisADException ve) {
      }
      break;
    case DisplayEvent.REFERENCE_ADDED:
      DisplayReferenceEvent dre = (DisplayReferenceEvent )e;

      DataDisplayLink link = dre.getDataDisplayLink();
      try {
        RemoteReferenceLinkImpl rrl = new RemoteReferenceLinkImpl(link);
        ReferenceMonitorEvent evt;
        evt = new ReferenceMonitorEvent(MonitorEvent.REFERENCE_ADDED, rrl);
        notifyListeners(evt);
      } catch (VisADException ve) {
      } catch (RemoteException ve) {
      }
      break;

    default:
      System.err.println("DisplayMonitorImpl.displayChange: " + Name +
                         " got " + e.getClass().getName() + " " + e +
                         "=>" + e.getDisplay());
      System.exit(1);
      break;
    }
  }

  /**
   * Handles ScalarMap data changes.<BR><BR>
   * If the <CODE>ScalarMapEvent</CODE> is not ignored, a
   * <CODE>MapMonitorEvent</CODE> will be sent to all listeners.
   *
   * @param e The details of the <CODE>ScalarMap</CODE> change.
   */
  public void mapChanged(ScalarMapEvent evt)
  {
    // don't bother if nobody's listening
    if (!hasListeners()) {
      return;
    }

    if (evt.getId() == ScalarMapEvent.AUTO_SCALE) {
      // ignore internal autoscale events
      return;
    }

    ScalarMap map = evt.getScalarMap();
    try {
      notifyListeners(new MapMonitorEvent(MonitorEvent.MAP_CHANGED,
                                          (ScalarMap )map.clone()));
    } catch (VisADException ve) {
    }
  }

  /**
   * Handles ScalarMap control changes.<BR>
   * <FONT SIZE="-1">This is just a stub which ignores the event.</FONT>
   *
   * @param e The details of the <CODE>ScalarMap</CODE> change.
   */
  public void controlChanged(ScalarMapControlEvent evt)
  {
    int id = evt.getId();
    if (id == ScalarMapEvent.CONTROL_REMOVED ||
        id == ScalarMapEvent.CONTROL_REPLACED)
    {
      evt.getControl().removeControlListener(this);
    }

    if (id == ScalarMapEvent.CONTROL_REPLACED ||
        id == ScalarMapEvent.CONTROL_ADDED)
    {
      Control ctl = evt.getScalarMap().getControl();
      controlChanged(new ControlEvent(ctl));
      ctl.addControlListener(this);
    }
  }

  /**
   * Returns the name of this <CODE>DisplayMonitor</CODE>.
   */
  public String toString()
  {
    return Name;
  }
}
