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

public class MonitorBroadcaster
{
  private int nextListenerID = 0;

  /**
   * The <TT>Display</TT> being monitored.
   */
  private DisplayImpl myDisplay;

  /**
   * The list of objects interested in changes to the monitored
   * <TT>Display</TT>.
   */
  private Vector ListenerVector;

  /**
   * The queue of events received from listeners -- used to suppress
   * events generated due to changes in remote displays.
   */
  private ObjectCache objCache;

  public MonitorBroadcaster(DisplayImpl dpy)
  {
    myDisplay = dpy;

    ListenerVector = new Vector();
    objCache = new ObjectCache(dpy.getName() + " Cache");
  }

  /**
   * Adds the specified listener to receive <TT>MonitorEvent</TT>s
   * when the monitored <TT>Display</TT>'s state changes.
   *
   * @param listener The listener to add.
   * @param id The unique listener identifier.
   *
   * @exception VisADException If the listener <TT>Vector</TT>
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
   * <TT>DisplayMonitor</TT> and the remote <TT>DisplayMonitor</TT>.
   *
   * @param rdm The remote <TT>DisplayMonitor</TT> to negotiate with.
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
   * Initializes links so that <TT>MonitorEvent</TT>s will be
   * exchanged with the specified remote <TT>Display</TT>.
   *
   * @param rd The remote <TT>Display</TT> to synchronize.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If the inter-<TT>Display</TT>
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
   * Returns <TT>true</TT> if there are listeners for this display.
   */
  public boolean hasListeners()
  {
    return (ListenerVector != null && ListenerVector.size() > 0);
  }

  /**
   * Forwards the <TT>MonitorEvent</TT> to all the listeners
   * associated with this <TT>DisplayMonitor</TT>.
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
   * Stops forwarding <TT>MonitorEvent</TT>s to the specified listener.
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
   * Caches this (remotely-originated) <TT>MonitorEvent</TT>
   * for future reference.
   *
   * @param evt The event to cache.
   */
  public void addRemoteMonitorEvent(MonitorEvent evt)
  {
    objCache.add(evt);
  }

  /**
   * Returns <TT>true</TT> if there is a <TT>MonitorEvent</TT>
   * for the specified <TT>Control</TT> waiting to be delivered to the
   * listener with the specified id.
   *
   * @param listenerID The identifier for the listener.
   * @param ctl The <TT>Control</TT> being found.
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
   * Creates a new wrapper for a <TT>DisplayMonitorListener</TT>
   * which is mapped to a unique ID.
   *
   * @param listener The <TT>DisplayMonitorListener</TT> to wrap.
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
   * <TT>EventComparator</TT> is used to sort the event table just
   * before delivering it to a remote <TT>DisplayMonitorListener</TT>.
   */
  class EventComparator
    implements Comparator
  {
    private Hashtable table;

    /**
     * Creates a new comparison class for the given <TT>Hashtable</TT>
     *
     * @param tbl The <TT>Hashtable</TT> to use for comparisons.
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
     * Returns <TT>true</TT> if the specified object is
     * an <TT>EventComparator</TT>.
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
   * <TT>Listener</TT> is an encapsulation of all the data related to
   * delivering events to a single <TT>DIsplayMonitorListener</TT>.
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
     * Creates a new <TT>Listener</TT> with the specified ID.
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
     * @param tbl The <TT>Hashtable</TT> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(Hashtable tbl, ControlMonitorEvent evt)
    {
      tbl.put(new ControlEventKey(evt.getControl()), evt);
    }

    /**
     * Adds the event to the specified table.
     *
     * @param tbl The <TT>Hashtable</TT> to which the event will be added.
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
     * @param tbl The <TT>Hashtable</TT> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(Hashtable tbl, ReferenceMonitorEvent evt)
    {
      tbl.put(evt.getLink(), evt);
    }

    /**
     * Adds a generic <TT>MonitorEvent</TT> to the specified table.
     * (This method simply forwards the event to the
     * <TT>addEventToTable</TT> method for this specific
     * <TT>MonitorEvent</TT> type.)
     *
     * @param tbl The <TT>Hashtable</TT> to which the event will be added.
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
     * Gets the unique identifier for this <TT>Listener</TT>.
     */
    public int getID() { return id; }

    /**
     * Gets the actual <TT>DisplayMonitorListener</TT> for this
     * <TT>Listener</TT>.
     */
    public DisplayMonitorListener getListener() { return listener; }

    /**
     * Returns <TT>true</TT> if there wass already a
     * <TT>MonitorEvent</TT> queued for the specified
     * <TT>Control</TT>.
     *
     * @param ctl The <TT>Control</TT>.
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
     * Returns <TT>true</TT> if there were diverted events
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
     * Returns <TT>true</TT> if this Listener is dead.
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
          // restore failed event to table
          table.put(list[i], evt);
          // let caller handle ConnectExceptions
          throw ce;
        } catch (Throwable t) {
          // whine loudly about all other Exceptions
          t.printStackTrace();
        }
      }
    }
  }
}
