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

import java.io.EOFException;

import java.rmi.ConnectException;
import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

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
  private ArrayList list;

  public MonitorBroadcaster(DisplayImpl dpy)
  {
    myDisplay = dpy;

    list = new ArrayList();
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
    synchronized (list) {
      list.add(new MonitorListener(listener, id));
    }
  }

  /**
   * Returns a suggestion for a unique listener identifier which is
   * equal to or greater than the supplied ID.
   *
   * @param id The identifier to check.
   */
  public int checkID(int id)
  {
    ListIterator iter = list.listIterator();
    while (iter.hasNext()) {
      MonitorListener li = (MonitorListener )iter.next();
      if (li.getID() == id) {
        synchronized (list) {
          if (nextListenerID == id) {
            nextListenerID++;
          }
          nextListenerID++;
        }
        return nextListenerID;
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

    int rmtID;
    synchronized (list) {
      rmtID = nextListenerID++;
    }

    int id;
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
    final int id = negotiateUniqueID(rdm);

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
    return (list.size() > 0);
  }

  /**
   * Forwards the <TT>MonitorEvent</TT> to all the listeners
   * associated with this <TT>DisplayMonitor</TT>.
   *
   * @param evt The event to forward.
   */
  public void notifyListeners(MonitorEvent evt)
  {
    final int evtID = evt.getOriginator();

    ListIterator iter = list.listIterator();
    while (iter.hasNext()) {
      MonitorListener li = (MonitorListener )iter.next();

      if (li.isDead()) {
        // delete dead listeners
        iter.remove();
        continue;
      }

      if (li.eventSeen(evt)) {
        // don't rebroadcast events
        continue;
      }

      if (evtID == li.getID()) {
        // don't return event to its source
        continue;
      }

      li.addEvent(evt);
    }
  }

  /**
   * Stops forwarding <TT>MonitorEvent</TT>s to the specified listener.
   *
   * @param l Listener to remove.
   */
  public void removeListener(DisplayMonitorListener l)
  {
    if (l != null) {
      ListIterator iter = list.listIterator();
      while (iter.hasNext()) {
        MonitorListener li = (MonitorListener )iter.next();
        if (li.getListener().equals(l)) {
          iter.remove();
          break;
        }
      }
    }
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
    return false;
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
    private HashMap table;

    /**
     * Creates a new comparison class for the given <TT>HashMap</TT>
     *
     * @param tbl The <TT>HashMap</TT> to use for comparisons.
     */
    EventComparator(HashMap tbl)
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
   * delivering events to a single <TT>DisplayMonitorListener</TT>.
   */
  class MonitorListener
    implements Runnable
  {
    private DisplayMonitorListener listener;
    private int id;

    private boolean dead = false;

    private Object tableLock = new Object();
    private boolean haveThread = false;
    private HashMap table = new HashMap();
    private HashMap diverted = null;

    private ObjectCache cache;

    MonitorListener(DisplayMonitorListener listener, int id)
    {
      this.listener = listener;
      this.id = id;

      cache = new ObjectCache("Cache#" + id);
    }

    public void addEvent(MonitorEvent evt)
    {
      synchronized (tableLock) {
        if (haveThread) {
          if (diverted == null) {
            diverted = new HashMap();
          }
          addEventToTable(diverted, evt);
        } else {
          addEventToTable(table, evt);
          startDelivery();
        }
      }
    }

    /**
     * Adds a generic <TT>MonitorEvent</TT> to the specified table.
     * (This method simply forwards the event to the
     * <TT>addEventToTable</TT> method for this specific
     * <TT>MonitorEvent</TT> type.)
     *
     * @param tbl The <TT>HashMap</TT> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(HashMap tbl, MonitorEvent evt)
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
     * Adds the event to the specified table.
     *
     * @param tbl The <TT>HashMap</TT> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(HashMap tbl, ControlMonitorEvent evt)
    {
      synchronized (tableLock) {
        tbl.put(new ControlEventKey(evt.getControl()), evt);
      }
    }

    /**
     * Adds the event to the specified table.
     *
     * @param tbl The <TT>HashMap</TT> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(HashMap tbl, MapMonitorEvent evt)
    {
      Object key = evt.getMap();
      if (key == null) {
        if (evt.getType() != MonitorEvent.MAPS_CLEARED) {
          System.err.println("Got null map for " + evt);
          return;
        }

        key = "null";
      }

      synchronized (tableLock) {
        tbl.put(key, evt);
      }
    }

    /**
     * Adds the event to the specified table.
     *
     * @param tbl The <TT>HashMap</TT> to which the event will be added.
     * @param evt The event to add.
     */
    private void addEventToTable(HashMap tbl, ReferenceMonitorEvent evt)
    {
      synchronized (tableLock) {
        tbl.put(evt.getLink(), evt);
      }
    }

    /**
     * Tries to deliver the queued events to this listener
     *
     * @exception RemoteException If a connection could not be made to the
     * 					remote listener.
     */
    private void deliverEventTable()
      throws RemoteException
    {
      // build the array of events
      Object[] list = new Object[table.size()];
      Iterator iter = table.keySet().iterator();
      for (int i = list.length - 1; i >= 0; i--) {
        if (iter.hasNext()) {
          list[i] = iter.next();
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
        } catch (RemoteException re) {
          // restore failed event to table
          table.put(list[i], evt);
          // let caller handle RemoteExceptions
          throw re;
        } catch (Throwable t) {
          // whine loudly about all other Exceptions
          t.printStackTrace();
        }
      }
    }

    /**
     * Check to see if this listener has already seen the event.
     *
     * @param evt The event to examine.
     *
     * @return <TT>true</TT> if the event has been seen.
     */
    public boolean eventSeen(MonitorEvent evt)
    {
      Object obj = evt.getObject();
      if (obj == null) {
        // assume other side hasn't seen null events
        return false;
      }

      // if we've already delivered this object, ignore it
      if (cache.isCached(obj)) {
        return true;
      }

      // remember this event for future reference
      cache.add(evt.getClonedObject());
      return false;
    }

    /**
     * Get the unique identifier.
     *
     * @return the unique identifier.
     */
    int getID() { return id; }

    /**
     * Get the <TT>DisplayMonitorListener</TT>.
     *
     * @return the listener.
     */
    DisplayMonitorListener getListener() { return listener; }

    /**
     * Check to see if the connection is dead.
     *
     * @return <TT>true</TT> if the connection is dead.
     */
    public boolean isDead() { return dead; }

    /**
     * Delivers events to the remote listener.
     */
    public void run()
    {
      int attempts = 0;
      boolean delivered = false;

      while (!delivered || undivertEvents()) {
        try {
          deliverEventTable();
          delivered = true;
        } catch (RemoteException re) {
          if (attempts++ < 10) {
            // wait a bit, then try again to deliver the events
            try { Thread.sleep(500); } catch (InterruptedException ie) { }
          } else {
            // if we failed to connect for 10 times, give up
            break;
          }
        }
      }

      // mark this listener as dead
      if (!delivered) {
        dead = true;
      }

      // indicate that the thread has exited
      haveThread = false;
    }

    /**
     * Start event delivery.
     */
    private void startDelivery()
    {
      synchronized (tableLock) {
        if (haveThread) {
          return;
        }

        // remember that we've started a thread
        haveThread = true;
      }

      if (listenerPool == null) {
        startThreadPool();
      }
      listenerPool.queue(this);
    }

    private String description = null;

    public String toString()
    {
      if (description == null) {
        StringBuffer buf = new StringBuffer("Listener[");
        buf.append(id);
        buf.append('=');

        String lName = listener.toString();
        int stubIdx = lName.indexOf("_Stub[");
        if (stubIdx > 0) {
          lName = lName.substring(0, stubIdx);
        }
        buf.append(lName);

        buf.append(" (");
        buf.append(listener.getClass().getName());
        buf.append(")]");
        description = buf.toString();
      }
      return description;
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
  }
}
