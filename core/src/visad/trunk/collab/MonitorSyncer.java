/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import visad.Control;
import visad.RemoteDisplay;
import visad.RemoteVisADException;
import visad.ScalarMap;

import visad.util.ThreadPool;

class MonitorSyncer
  implements Runnable
{
  private String Name;

  private boolean dead = false;

  private Object cacheLock = new Object();
  private Thread thisThread = null;

  private ArrayList current = new ArrayList();
  private ArrayList diverted = null;

  private HashMap eventCache = null;
 
  private RemoteDisplay rmtDpy;
  private MonitorCallback callback;
  private int id;

  private RemoteEventProvider provider;

  /**
   * The event callback thread pool and its lock.
   */
  private transient static ThreadPool callbackPool = null;
  private static Object callbackPoolLock = new Object();

  public MonitorSyncer(String name, MonitorCallback callback, int id)
    throws RemoteException
  {
    this.Name = name + ":MonL";

    this.eventCache = new HashMap();

    this.rmtDpy = null;
    this.callback = callback;
    this.id = id;

    this.provider = new RemoteEventProviderImpl(this);
  }

  public MonitorSyncer(String name, RemoteDisplay rmtDpy, int id)
    throws RemoteException
  {
    this.Name = name + ":MonL";

    this.eventCache = new HashMap();

    this.rmtDpy = rmtDpy;
    this.callback = rmtDpy.getRemoteDisplaySync();
    this.id = id;

    this.provider = new RemoteEventProviderImpl(this);
  }

  public void addEvent(MonitorEvent evt)
  {
    String key = getKey(evt);
    synchronized (cacheLock) {
      MonitorEvent oldEvt = (MonitorEvent )eventCache.put(key, evt);

      if (thisThread != null) {
        if (diverted == null) {
          diverted = new ArrayList();
        }
        diverted.add(key);
      } else {
        current.add(key);
        thisThread = new Thread(this);
        thisThread.start();
      }
    }
  }

  public MonitorEvent getEvent(Object key)
  {
    MonitorEvent evt;
    synchronized (cacheLock) {
      evt = (MonitorEvent )eventCache.remove(key);
    }

    // mark message as coming from this connection, so we don't see it again
    if (evt != null) {
      evt.setOriginator(id);
    }

    return evt;
  }

  /**
   * Get the unique identifier.
   *
   * @return the unique identifier.
   */
  public int getID() { return id; }

  /**
   * Get a key for the specified <TT>MonitorEvent</TT>
   *
   * @param evt The event.
   */
  public String getKey(MonitorEvent evt)
  {
    String key = null;

    if (evt instanceof ControlMonitorEvent) {
      key = getKey((ControlMonitorEvent )evt);
    } else if (evt instanceof MapMonitorEvent) {
      key = getKey((MapMonitorEvent )evt);
    } else if (evt instanceof ReferenceMonitorEvent) {
      key = getKey((ReferenceMonitorEvent )evt);
    }

    return key;
  }

  /**
   * Get a key for the specified <TT>Control</TT>
   *
   * @param evt The event.
   */
  private String getKey(Control ctl)
  {
    return ctl.getClass().getName() + "#" + ctl.getInstanceNumber();
  }

  /**
   * Get a key for the specified <TT>ControlMonitorEvent</TT>
   *
   * @param evt The event.
   */
  private String getKey(ControlMonitorEvent evt)
  {
    return getKey(evt.getControl());
  }

  /**
   * Get a key for the specified <TT>MapMonitorEvent</TT>
   *
   * @param evt The event.
   */
  private String getKey(MapMonitorEvent evt)
  {
    String key;

    ScalarMap map = evt.getMap();
    if (map != null) {
      key = map.toString();
      switch (evt.getType()) {
      case MonitorEvent.MAP_ADDED:
        key = "ADD " + key;
        break;
      case MonitorEvent.MAP_CHANGED:
        key = "CHG " + key;
        break;
      }
    } else {
      if (evt.getType() != MonitorEvent.MAPS_CLEARED) {
        System.err.println("Got null map for " + evt);
        return null;
      }

      key = "MAPS_CLEARED";
    }

    return key;
  }

  /**
   * Get a key for the specified <TT>ReferenceMonitorEvent</TT>
   *
   * @param evt The event.
   */
  private String getKey(ReferenceMonitorEvent evt)
  {
    return evt.getLink().toString();
  }

  public MonitorCallback getListener() { return callback; }

  public String getName() { return Name; }

  public boolean hasControlEventQueued(Control ctl)
  {
    return eventCache.containsKey(getKey(ctl));
  }

  /**
   * Check to see if the connection is dead.
   *
   * @return <TT>true</TT> if the connection is dead.
   */
  public boolean isDead() { return dead; }

  /**
   * Check to see if this object is monitoring the specified
   * <tt>RemoteDisplay</tt>.
   *
   * @param rmtDpy <tt>RemoteDisplay</tt> being searched for.
   *
   * @return <tt>true</tt> if this object is monitoring the display.
   */
  public boolean isMonitored(RemoteDisplay rmtDpy)
  {
    return this.rmtDpy.equals(rmtDpy);
  }

  public void run()
  {
    boolean done = false;
    try {

      int attempts = 0;
      while (!done) {
        try {
          sendEventKeys(current);
          done = true;
        } catch (RemoteException re) {
          if (attempts++ < 5) {
            // wait a bit, then try again to notify the remote Display
            try { Thread.sleep(500); } catch (InterruptedException ie) { }
          } else {
            // if we failed to connect for 10 times, give up
            dead = true;
            break;
          }
        } catch (RemoteVisADException rve) {
          rve.printStackTrace();
          done = true;
        }

        if (done) {
          synchronized (cacheLock) {
            if (!undivertEvents()) {
              break;
            }

            done = false;
          }
        }
      }
    } finally {
      // indicate that the thread has exited
      synchronized (cacheLock) {
        thisThread = null;
      }
    }
  }

  private void sendEventKeys(ArrayList list)
    throws RemoteException, RemoteVisADException
  {
    while (list.size() > 0) {
      String key = (String )list.remove(0);
      callback.eventReady(provider, key);
    }
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer("MonitorSyncer[");
    buf.append(Name);
    buf.append("=#");
    buf.append(id);
    buf.append(']');
    return buf.toString();
  }

  /**
   * Returns <TT>true</TT> if there were diverted requests.
   */
  private boolean undivertEvents()
  {
    final boolean undivert;
    synchronized (cacheLock) {
      // if there are events queued, restore them to the main table
      undivert = (diverted != null);
      if (undivert) {
        current = diverted;
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
    private Control control;
    private Class cclass;
    private int instance;

    ControlEventKey(Control ctl)
    {
      control = ctl;
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

    public boolean equals(Control ctl)
    {
      return cclass.equals(ctl.getClass());
    }

    public int hashCode()
    {
      return cclass.hashCode() + instance;
    }

    public String toString()
    {
      StringBuffer buf = new StringBuffer(control.toString());
      if (buf.length() > 48) {
        buf.setLength(0);
      }
      buf.insert(0, ':');
      buf.insert(0, instance);
      buf.insert(0, '#');
      buf.insert(0, cclass.getName());
      return buf.toString();
    }
  }
}
