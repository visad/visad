/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import visad.ConstantMap;
import visad.Control;
import visad.DataRenderer;
import visad.DisplayImpl;
import visad.RemoteDataReference;
import visad.RemoteDisplayImpl;
import visad.RemoteReferenceLink;
import visad.RemoteVisADException;
import visad.ScalarMap;
import visad.VisADException;

public class DisplaySyncImpl
  implements Comparator, DisplaySync, Runnable
{
  private String Name;
  private DisplayImpl myDisplay;
  private DisplayMonitor monitor;

  private Object mapClearSync = new Object();
  private int mapClearCount = 0;

  private boolean dead = false;

  private Object tableLock = new Object();
  private Thread thisThread = null;

  private HashMap current = new HashMap();
  private HashMap diverted = null;

  public DisplaySyncImpl(DisplayImpl dpy)
    throws RemoteException
  {
    Name = dpy.getName() + ":Sync";
    myDisplay = dpy;
    monitor = dpy.getDisplayMonitor();
    monitor.setDisplaySync(this);
  }

  /**
   * Adds the specified data reference to this <TT>Display</TT>.
   *
   * @param link The link to the remote data reference.
   *
   * @exception VisADException If a link could not be made for the
   * 				remote data reference.
   */
  private void addLink(RemoteReferenceLink link)
    throws VisADException
  {
    // build array of ConstantMap values
    ConstantMap[] cm = null;
    try {
      Vector v = link.getConstantMapVector();
      int len = v.size();
      if (len > 0) {
        cm = new ConstantMap[len];
        for (int i = 0; i < len; i++) {
          ConstantMap tmp = (ConstantMap )v.elementAt(i);
          cm[i] = (ConstantMap )tmp.clone();
        }
      }
    } catch (Exception e) {
      throw new VisADException("Couldn't copy ConstantMaps" +
                               " for remote DataReference");
    }

    // get reference to Data object
    RemoteDataReference ref;
    try {
      ref = link.getReference();
    } catch (Exception e) {
      throw new VisADException("Couldn't copy remote DataReference");
    }

    if (ref != null) {

      DataRenderer dr = myDisplay.getDisplayRenderer().makeDefaultRenderer();
      String defaultClass = dr.getClass().getName();

      // get proper DataRenderer
      DataRenderer renderer;
      try {
        String newClass = link.getRendererClassName();
        if (newClass == defaultClass) {
          renderer = null;
        } else {
          Object obj = Class.forName(newClass).newInstance();
          renderer = (DataRenderer )obj;
        }
      } catch (Exception e) {
        throw new VisADException("Couldn't copy remote DataRenderer " +
                                 "name; using " + defaultClass);
      }

      // build RemoteDisplayImpl to which reference is attached
      try {
        RemoteDisplayImpl rd = new RemoteDisplayImpl(myDisplay);

        // if this reference uses the default renderer...
        if (renderer == null) {
          rd.addReference(ref, cm);
        } else {
          rd.addReferences(renderer, ref, cm);
        }
      } catch (Exception e) {
        e.printStackTrace();
        throw new VisADException("Couldn't add remote DataReference " +
                                 ref + ": " + e.getClass().getName() +
                                 ": " + e.getMessage());
      }
    }
  }

  public int compare(Object o1, Object o2)
  {
    return (((MonitorEvent )o1).getSequenceNumber() -
            ((MonitorEvent )o2).getSequenceNumber());
  }

  public void destroy()
  {
    monitor = null;
    myDisplay = null;
  }
 
  /**
   * Finds the first map associated with this <TT>Display</TT>
   * which matches the specified <TT>ScalarMap</TT>.
   *
   * @param map The <TT>ScalarMap</TT> to find.
   */
  private ScalarMap findMap(ScalarMap map)
  {
    ScalarMap found = null;

    boolean isConstMap;
    Vector v;
    if (map instanceof ConstantMap) {
      v = myDisplay.getConstantMapVector();
      isConstMap = true;
    } else {
      v = myDisplay.getMapVector();
      isConstMap = false;
    }

    ListIterator iter = v.listIterator();
    while (iter.hasNext()) {
      ScalarMap sm = (ScalarMap )iter.next();
      if (sm.equals(map)) {
        found = sm;
        break;
      }
    }

    return found;
  }

  /**
   * Start event callback.
   */
  public void eventReady(RemoteEventProvider provider, Object key)
  {
    synchronized (tableLock) {
      if (thisThread != null) {
        if (diverted == null) {
          diverted = new HashMap();
        }
        diverted.put(key, provider);
      } else {
        current.put(key, provider);
        thisThread = new Thread(this);
        thisThread.start();
      }
    }
  }

  public String getName() { return Name; }

  public boolean isLocalClear()
  {
    boolean result = true;
    synchronized (mapClearSync) {
      if (mapClearCount > 0) {
        mapClearCount--;
        result = false;
      }
    }

    return result;
  }

  public boolean isThreadRunning()
  {
    return (thisThread != null);
  }

  private void processMap(HashMap map)
  {
    MonitorEvent[] list = new MonitorEvent[map.size()];

    // build the array of events
    Iterator iter = map.keySet().iterator();
    for (int i = list.length - 1; i >= 0; i--) {
      if (iter.hasNext()) {
        String key = (String )iter.next();
        list[i] = (MonitorEvent )map.get(key);
      } else {
        list[i] = null;
      }
    }

    // sort events by order of creation
    Arrays.sort(list, this);

    int i, attempts;
    i = attempts = 0;
    while (i < list.length) {
      try {
        processOneEvent(list[i]);
        i++;
      } catch (RemoteException re) {
        if (attempts++ < 5) {
          // wait a bit, then try again to request the events
          try { Thread.sleep(500); } catch (InterruptedException ie) { }
        } else {
          // if we failed to connect for 10 times, give up
          dead = true;
          break;
        }
      } catch (RemoteVisADException rve) {
        System.err.println("While processing " + list[i] + ":");
        i++;
        rve.printStackTrace();
      }
    }
  }

  private void processOneEvent(MonitorEvent evt)
    throws RemoteException, RemoteVisADException
  {
    Control lclCtl, rmtCtl;
    ScalarMap lclMap, rmtMap;

    switch (evt.getType()) {
    case MonitorEvent.MAP_ADDED:

      rmtMap = ((MapMonitorEvent )evt).getMap();

      // if we haven't already added this map...
      if (findMap(rmtMap) == null) {
/* WLH 26 Dec 2002
        if (!myDisplay.getRendererVector().isEmpty()) {
          System.err.println("Late addMap: " + rmtMap);
        } else {
*/
          try {
            myDisplay.addMap(rmtMap, evt.getOriginator());
          } catch (VisADException ve) {
            ve.printStackTrace();
            throw new RemoteVisADException("Map " + rmtMap + " not added: " +
                                           ve);
          }
/*
        }
*/
      }
      break;
    case MonitorEvent.MAP_REMOVED:

      rmtMap = ((MapMonitorEvent )evt).getMap();

      // if we have already added this map...
      if (findMap(rmtMap) != null) {
          try {
            myDisplay.removeMap(rmtMap, evt.getOriginator());
          } catch (VisADException ve) {
            ve.printStackTrace();
            throw new RemoteVisADException("Map " + rmtMap + " not removed: " +
                                           ve);
          }
      }
      break;
    case MonitorEvent.MAP_CHANGED:
      rmtMap = ((MapMonitorEvent )evt).getMap();
      lclMap = findMap(rmtMap);
      if (lclMap == null) {
        throw new RemoteVisADException("ScalarMap " + rmtMap + " not found");
      }
      // CTR 2 June 2000 - do not set map range if already set (avoid loops)
      double[] rng = rmtMap.getRange();
      double[] lclRng = lclMap.getRange();
      if (rng[0] != lclRng[0] || rng[1] != lclRng[1]) {
        try {
          lclMap.setRange(rng[0], rng[1], evt.getOriginator());
        } catch (VisADException ve) {
          throw new RemoteVisADException("Map not changed: " + ve);
        }
      }
      break;
    case MonitorEvent.MAPS_CLEARED:
      try {
        myDisplay.removeAllReferences();
        myDisplay.clearMaps();
      } catch (VisADException ve) {
        throw new RemoteVisADException("Maps not cleared: " + ve);
      } catch (NullPointerException npe) {
        npe.printStackTrace();
        throw new RemoteVisADException("Maps not cleared");
      }
      break;
    case MonitorEvent.REFERENCE_ADDED:
      RemoteReferenceLink ref = ((ReferenceMonitorEvent )evt).getLink();
      try {
        addLink(ref);
      } catch (VisADException ve) {
        throw new RemoteVisADException("DataReference " + ref +
                                       " not found by " + Name + ": " +
                                       ve.getMessage());
      }

      break;
    case MonitorEvent.CONTROL_INIT_REQUESTED:
      // !!! DON'T FORWARD INIT EVENTS TO LISTENERS !!!

      rmtCtl = ((ControlMonitorEvent )evt).getControl();
      lclCtl = myDisplay.getControl(rmtCtl.getClass(),
                                    rmtCtl.getInstanceNumber());
      if (lclCtl == null) {
        // didn't find control ... maybe it doesn't exist yet?
        break;
      }

      try {
        ControlMonitorEvent cme;
        cme = new ControlMonitorEvent(MonitorEvent.CONTROL_CHANGED,
                                      (Control )lclCtl.clone());
        monitor.notifyListeners(cme);
      } catch (VisADException ve) {
        throw new RemoteVisADException("Control " + rmtCtl +
                                       " not changed by " + Name + ": " + ve);
      }
      break;
    case MonitorEvent.CONTROL_CHANGED:
      rmtCtl = ((ControlMonitorEvent )evt).getControl();
      lclCtl = myDisplay.getControl(rmtCtl.getClass(),
                                    rmtCtl.getInstanceNumber());

      // skip this if we have change events to deliver for this control
      if (lclCtl != null &&
          !monitor.hasEventQueued(evt.getOriginator(), lclCtl))
      {

        try {
          lclCtl.syncControl(rmtCtl);
        } catch (VisADException ve) {
          throw new RemoteVisADException("Control " + lclCtl +
                                         " not changed by " + Name + ": " +
                                         ve.getMessage());
        }
      }

      break;
    case MonitorEvent.MESSAGE_SENT:
      myDisplay.sendMessage(((MessageMonitorEvent )evt).getMessage());
      break;
    default:
      throw new RemoteVisADException("Event " + evt + " not handled");
    }
  }

  private HashMap requestEventTable(HashMap table)
    throws RemoteException
  {
    HashMap map = null;

    Iterator iter = table.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String )iter.next();
      RemoteEventProvider provider = (RemoteEventProvider )table.get(key);
      iter.remove();

      MonitorEvent evt = requestOneEvent(key, provider);
      if (evt != null) {
        if (map == null) {
          map = new HashMap();
        }
        map.put(key, evt);
      }
    }

    return map;
  }

  private MonitorEvent requestOneEvent(String key,
                                       RemoteEventProvider provider)
    throws RemoteException
  {
    // get the event
    MonitorEvent evt;
    try {
      evt = provider.getEvent(key);
    } catch (RemoteVisADException rve) {
      rve.printStackTrace();
      throw new RemoteException(rve.getMessage());
    }

    if (evt == null) {
      // if it's already been picked up, we're done
      return null;
    }

    switch (evt.getType()) {
    case MonitorEvent.MAPS_CLEARED:
      synchronized (mapClearSync) {
        mapClearCount++;
      }
      break;
    case MonitorEvent.CONTROL_CHANGED:
      boolean result;
      try {
        result = monitor.hasEventQueued(evt.getOriginator(),
                                        ((ControlMonitorEvent )evt).getControl());
      } catch (RemoteException re) {
        re.printStackTrace();
        result = false;
      }

      if (result) {
        // drop this event since we're about to override it
        return null;
      }
      break;
    }

    return evt;
  }

  /**
   * Requests events from the remote provider(s).
   */
  public void run()
  {
    HashMap map = null;

    boolean done = false;
    try {

      int attempts = 0;
      while (!done) {
        HashMap newMap;
        try {
          newMap = requestEventTable(current);
          done = true;
        } catch (RemoteException re) {
          if (attempts++ < 5) {
            // wait a bit, then try again to request the events
            try { Thread.sleep(500); } catch (InterruptedException ie) { }
            newMap = null;
          } else {
            // if we failed to connect for 10 times, give up
            dead = true;
            break;
          }
        }

        if (map == null) {
          map = newMap;
        } else if (newMap != null) {
          map.putAll(newMap);
        }

        if (done) {
          synchronized (tableLock) {
            if (!undivertEvents()) {
              break;
            }

            done = false;
          }
        }
      }
    } finally {

      if (map != null) {
        processMap(map);
      }

      // indicate that the thread has exited
      synchronized (tableLock) {
        thisThread = null;
      }
    }
  }

  /**
   * Returns <TT>true</TT> if there were diverted requests.
   */
  private boolean undivertEvents()
  {
    final boolean undivert;
    synchronized (tableLock) {
      // if there are events queued, restore them to the main table
      undivert = (diverted != null);
      if (undivert) {
        current = diverted;
        diverted = null;
      }
    }

    return undivert;
  }
}
