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
  extends EventListener
  implements DisplaySync
{
  private DisplayMonitor monitor;
  private DisplayImpl myDisplay;

  public DisplaySyncImpl(DisplayImpl dpy)
    throws RemoteException
  {
    super(dpy.getName() + ":Sync");
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
   * Attempt to deliver the queued events.
   *
   * @exception RemoteException If a connection could not be made to the
   * 					remote listener.
   */
  void deliverEventTable(EventTable tbl)
    throws RemoteException
  {
    MonitorEventTable mTable = (MonitorEventTable )tbl;

    // deliver events
    Iterator iter = mTable.keyIterator();
    while (iter.hasNext()) {
      Object key = iter.next();

      MonitorEvent evt = (MonitorEvent )mTable.remove(key);
      if (evt == null) {
        System.err.println("Skipping null event for key " + key);
        continue;
      }

      try {
        deliverOneEvent(evt);
      } catch (RemoteException re) {
        // restore failed event to table
        mTable.restore(key, evt);
        // let caller handle RemoteExceptions
        throw re;
      } catch (Throwable t) {
        // whine loudly about all other Exceptions
        t.printStackTrace();
      }
    }
  }

  private void deliverOneEvent(MonitorEvent evt)
    throws RemoteException, RemoteVisADException
  {
    Control lclCtl, rmtCtl;

    switch (evt.getType()) {
    case MonitorEvent.MAP_ADDED:
      // forward to any listeners
      monitor.notifyListeners(evt);

      ScalarMap map = ((MapMonitorEvent )evt).getMap();
      try {
        myDisplay.addMap(map);
      } catch (VisADException ve) {
        ve.printStackTrace();
        throw new RemoteVisADException("Map " + map + " not added: " + ve);
      }
      break;
    case MonitorEvent.MAP_CHANGED:
      // forward to any listeners
      monitor.notifyListeners(evt);

      ScalarMap rmtMap = ((MapMonitorEvent )evt).getMap();
      ScalarMap lclMap = findMap(rmtMap);
      if (lclMap == null) {
        throw new RemoteVisADException("ScalarMap " + rmtMap + " not found");
      }
      double[] rng = rmtMap.getRange();
      try {
        lclMap.setRange(rng[0], rng[1]);
      } catch (VisADException ve) {
        throw new RemoteVisADException("Map not changed: " + ve);
      }
      break;
    case MonitorEvent.MAPS_CLEARED:
      // forward to any listeners
      monitor.notifyListeners(evt);

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
      // forward to any listeners
      monitor.notifyListeners(evt);

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
      if (!monitor.hasEventQueued(evt.getOriginator(), lclCtl)) {

        if (lclCtl != null) {

          try {
            lclCtl.syncControl(rmtCtl);
          } catch (VisADException ve) {
            throw new RemoteVisADException("Control " + lclCtl +
                                           " not changed by " + Name + ": " +
                                           ve.getMessage());
          }

          // forward to any listeners
          monitor.notifyListeners(evt);
        }
      }

      break;
    default:
      throw new RemoteVisADException("Event " + evt + " not handled");
    }
  }

  EventTable getNewEventTable() { return new MonitorEventTable(Name); }

  public void stateChanged(MonitorEvent evt)
    throws RemoteException, RemoteVisADException
  {
    addEvent(evt);
  }
}
