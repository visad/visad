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

package visad.collab;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import visad.Control;

import visad.util.ObjectCache;
import visad.util.ThreadPool;

/**
   * <TT>MonitorListener</TT> is an encapsulation of all the data related to
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

  private EventCache cache;

  MonitorListener(DisplayMonitorListener listener, int id)
  {
    this.listener = listener;
    this.id = id;

    cache = new EventCache("Cache#" + id);
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
  boolean eventSeen(MonitorEvent evt)
  {
    // if we've already delivered this object, ignore it
    if (cache.isCached(evt)) {
      return true;
    }

    // remember this event for future reference
    cache.add(evt);
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
   * Cache to track recently seen events
   */
  class EventCache
  {
    String name;
    ObjectCache cache;

    public EventCache(String name)
    {
      this.name = name;
      cache = new ObjectCache(name);
    }

    public void add(MonitorEvent evt)
    {
      // HACK ALERT!!!
      //  If this is a MAPS_CLEARED event, delete all added map events
      //  If this is a MAP_ADDED event, delete any clear events
      switch (evt.getType()) {
      case MonitorEvent.MAPS_CLEARED:
        deleteEvents(MonitorEvent.MAP_ADDED);
        break;
      case MonitorEvent.MAP_ADDED:
        deleteEvents(MonitorEvent.MAPS_CLEARED);
        break;
      }
      Object obj = getClonedObject(evt);
      cache.add(obj);
    }

    private void deleteEvents(int type)
    {
      Iterator iter = cache.keys();
      while (iter.hasNext()) {
        Object key = iter.next();
        Object obj = cache.get(key);
        if (obj instanceof MonitorEvent) {
          MonitorEvent evt = (MonitorEvent )obj;
          if (evt.getType() == type) {
            cache.remove(key);
          }
        }
      }
    }

    private Object getClonedObject(MonitorEvent evt)
    {
      if (evt instanceof ControlMonitorEvent) {
        return ((ControlMonitorEvent )evt).getControl().clone();
      }

      return evt.clone();
    }

    private Object getObject(MonitorEvent evt)
    {
      if (evt instanceof ControlMonitorEvent) {
        return ((ControlMonitorEvent )evt).getControl();
      }

      return evt;
    }

    public boolean isCached(MonitorEvent evt)
    {
      Object obj = getObject(evt);
      if (obj == null) {
        // assume other side hasn't seen null events
        return false;
      }

      return cache.isCached(obj);
    }
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
}
