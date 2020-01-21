/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

/* Dave's design comments, inserted as a comment by Bill

VisAD collaboration

Collaboration is implemented within two classes, DisplayMonitor
and DisplaySync.  DisplayMonitor listens to the local Display
and notifies remote listeners of significant changes.
DisplaySync receives those notifications and makes them
happen on the local Display.

The notification takes the form of the various MonitorEvents:
 * a ControlMonitorEvent is sent when a Control is initialized
   or changed.
 * a MapMonitorEvents is sent when a ControlMap or ScalarMap
   is added or changed, or when all the ControlMaps and
   ScalarMaps are cleared from a Display.
 * a ReferenceMonitorEvent is sent when a Data reference is
   added to or removed from a Display.

All Displays can be collaborative servers, since a
DisplayMonitor and DisplaySync object are created for every
Display.

When a collaborative Display is constructed, the following
steps take place:
 * The client Display constructor uses its RemoteDisplay
   parameter to fetch the remote Display's ScalarMaps,
   ConstantMaps and remote Data references.
 * The remote Display is connected to the client Display
   via the client Display's DisplayMonitor and DisplaySync
   objects.
 * The client's Controls are synchronized with the
   remote Display's Controls
 * From this point, MonitorEvents are used to keep the
   two Displays synchronized.

MonitorEvent transmission happens in several steps.
 * DisplayMonitor is notified that the Display has changed.
 * It builds a MonitorEvent which is forwarded to each of
   its MonitorSyncer listeners, which are each connected to
   a remote DisplaySync object.
 * If the MonitorSyncer is already trying to deliver one
   or more events, the forwarded event will be added to
   a queue which will be delivered as soon as the current
   events have been delivered.
 * To deliver one or more events, the MonitorSyncer sends a
   "key" for each event to the remote DisplaySync.
 * The remote DisplaySync gathers all the event keys,
   then uses them to request the actual events from the
   MonitorSyncer.
 * The MonitorSyncer removes each requested event from its
   list, then sends the event back to the remote DisplaySync.
 * The remote DisplaySync receives the event and uses it to
   synchronize its Display.

This is somewhat complicated but necessary, mainly due to
networking and execution delays and ordering problems.

The original implementation simply sent each event.  This
caused problems due to events being delivered out of order.
For instance, a Control might move from state A to state B
to state C, but the event for state C would occasionally
be delivered before state B.

This was fixed by only allowing one set of events to be
delivered at a time, and forcing events to be delivered
in the order in which they were received.  This ran into
problems because if, as above, a Control moved from state A
through state B to state C, the event for state B might be
delivered after the local Control moved to state C, but then
the remote Display might forward the event for state B back
to the local Display, causing a loop which might eventually
settle on state B rather than state C.

The current solution causes events to accumulate in a
single cache, which uses keys which are unique to a given
Control, ScalarMap, etc. and only the keys are forwarded.
Events can be superceded up to the point where the remote
DisplaySync actually requests the event using the key.
The event for state C would overwrite the event for state B
in the MonitorSyncer's cache as long as the MonitorSyncer
received that event before it received the DisplaySync
request for the event.
*/

package visad.collab;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import visad.AnimationControl;
import visad.Control;
import visad.ControlEvent;
import visad.DataDisplayLink;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.DisplayEvent;
import visad.DisplayImpl;
import visad.DisplayMapEvent;
import visad.DisplayReferenceEvent;
import visad.MessageEvent;
import visad.RemoteDisplay;
import visad.RemoteDisplayImpl;
import visad.RemoteReferenceLinkImpl;
import visad.RemoteVisADException;
import visad.ScalarMap;
import visad.ScalarMapControlEvent;
import visad.ScalarMapEvent;
import visad.VisADException;

/**
 * <tt>DisplayMonitorImpl</tt> is the {@link visad.Display Display} monitor
 * implementation.<P>
 * <tt>DisplayMonitorImpl</tt> is not {@link java.io.Serializable Serializable} and
 * should not be copied between JVMs.<P>
 */
public class DisplayMonitorImpl
  implements DisplayMonitor
{
  private int nextListenerID = UNKNOWN_LISTENER_ID + 1;

  /**
   * The name of this display monitor.
   */
  private String Name;

  /**
   * The {@link visad.Display Display} being monitored.
   */
  private DisplayImpl myDisplay;

  /**
   * The list of objects interested in changes to the monitored
   * {@link visad.Display Display}.
   */
  private ArrayList listeners;

  /**
   * The synchronization object for the monitored display
   */
  private DisplaySync sync;

  /**
   * Creates a monitor for the specified {@link visad.Display Display}.
   *
   * @param dpy The {@link visad.Display Display} to monitor.
   */
  public DisplayMonitorImpl(DisplayImpl dpy)
  {
    Name = dpy.getName() + ":Mon";

    dpy.addDisplayListener(this);
    dpy.addMessageListener(this);

    myDisplay = dpy;

    listeners = new ArrayList();
  }

  /**
   * Adds the specified listener to receive {@link MonitorEvent MonitorEvents}
   * when the monitored {@link visad.Display Display's} state changes.
   *
   * @param listener The listener to add.
   * @param id The unique listener identifier.
   *
   * @exception VisADException If the listener {@link java.util.Vector Vector}
   * 				is uninitialized.
   */
  public void addListener(MonitorCallback listener, int id)
    throws RemoteException, VisADException
  {
    MonitorSyncer ms = new MonitorSyncer(myDisplay.getName(), listener, id);
    synchronized (listeners) {
      listeners.add(ms);
    }
  }

  /**
   * Adds the specified remote display to receive {@link MonitorEvent MonitorEvents}
   * when the monitored {@link visad.Display Display's} state changes.
   *
   * @param rmtDpy The remote display to add.
   * @param id The unique listener identifier.
   *
   * @exception VisADException If the listener {@link java.util.Vector Vector}
   * 				is uninitialized.
   */
  public void addListener(RemoteDisplay rmtDpy, int id)
    throws RemoteException, VisADException
  {
    MonitorSyncer ms = new MonitorSyncer(myDisplay.getName(), rmtDpy, id);
    synchronized (listeners) {
      listeners.add(ms);
    }
  }

  /**
   * Initializes links so that {@link MonitorEvent MonitorEvents} will be
   * exchanged with the specified remote {@link visad.Display Display}.
   *
   * @param rd The remote {@link visad.Display Display} to synchronize.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If the inter-{@link visad.Display Display}
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
      rdm.addListener(new RemoteDisplayImpl(myDisplay), id);
    } catch (Exception e) {
      throw new RemoteVisADException("Couldn't make this object" +
                                     " a listener for the remote display");
    }

    boolean unwind = false;
    try {
      addListener(rd, id);
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
   * Returns a suggestion for a unique listener identifier which is
   * equal to or greater than the supplied ID.
   *
   * @param id The identifier to check.
   */
  public int checkID(int id)
  {
    synchronized (listeners) {
      boolean failed = true;
      while (failed) {
        failed = false;
        ListIterator iter = listeners.listIterator();
        while (iter.hasNext()) {
          MonitorSyncer li = (MonitorSyncer )iter.next();
          if (id == UNKNOWN_LISTENER_ID || li.getID() == id) {
            id = getNextListenerID();
            failed = true;
            break;
          }
        }
      }
    }

    return id;
  }

  /**
   * Handles {@link visad.Control Control} changes.<BR><BR>
   * If the {@link visad.ControlEvent ControlEvent} is not ignored, a
   * {@link ControlMonitorEvent ControlMonitorEvent} will be sent to all listeners.
   *
   * @param evt The details of the {@link visad.Control Control} change.
   */
  public void controlChanged(ControlEvent evt)
  {
    // CTR - notify display slaves of control changes
    if (myDisplay.hasSlaves()) {
      // construct properly formatted control change string
      Control control = evt.getControl();
      String msg = control.getSaveString();
      Class c = control.getClass();
      Vector v = myDisplay.getControls(c);
      int index = -1;
      for (int i=0; i<v.size(); i++) {
        Control ctrl = (Control) v.elementAt(i);
        if (control == ctrl) {
          index = i;
          break;
        }
      }
      String message = c.getName() + "\n" + index + "\n" + msg;
      myDisplay.updateSlaves(message);
    }

    // don't bother if nobody's listening
    if (!hasListeners()) {
      return;
    }

    if (evt.getControl() instanceof AnimationControl) {
      // ignore all animation-related events
      return;
    }

    Control ctlClone = (Control )(evt.getControl().clone());
    ControlMonitorEvent ctlEvt;
    try {
      ctlEvt = new ControlMonitorEvent(MonitorEvent.CONTROL_CHANGED,
                                       evt.getRemoteId(), ctlClone);
    } catch (VisADException ve) {
      ve.printStackTrace();
      ctlEvt = null;
    }

    if (ctlEvt != null) {
      notifyListeners(ctlEvt);
    }
  }

  /**
   * Handles ScalarMap control changes.<BR>
   * <FONT SIZE="-1">This is just a stub which ignores the event.</FONT>
   *
   * @param evt The details of the {@link visad.ScalarMap ScalarMap} change.
   */
  public void controlChanged(ScalarMapControlEvent evt)
  {
    // don't bother if nobody's listening
    if (!hasListeners()) {
      return;
    }

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
      controlChanged(new ControlEvent(ctl, evt.getRemoteId()));
      ctl.addControlListener(this);
    }
  }

  /** destroy this monitor */
  public void destroy()
  {
    sync = null;
    myDisplay.removeDisplayListener(this);
  }

  /**
   * Handles notification of objects being added to or removed from
   * the {@link visad.Display Display}.<BR><BR>
   * If the {@link visad.DisplayEvent DisplayEvent} is not ignored, a
   * {@link MapMonitorEvent MapMonitorEvent} or {@link ReferenceMonitorEvent ReferenceMonitorEvent}
   * will be sent to all listeners.
   *
   * @param evt The details of the {@link visad.Display Display} change.
   */
  public void displayChanged(DisplayEvent evt)
  {
    // don't bother if nobody's listening
    if (!hasListeners()) {
      return;
    }

    MapMonitorEvent mapEvt;
    ReferenceMonitorEvent refEvt;

    switch (evt.getId()) {
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
      ScalarMap map = (ScalarMap )((DisplayMapEvent )evt).getMap().clone();
      try {
        mapEvt = new MapMonitorEvent(MonitorEvent.MAP_ADDED,
                                     evt.getRemoteId(), map);
      } catch (VisADException ve) {
        ve.printStackTrace();
        mapEvt = null;
      }
      if (mapEvt != null) {
        notifyListeners(mapEvt);
      }
      break;
    case DisplayEvent.MAP_REMOVED:
      map = (ScalarMap )((DisplayMapEvent )evt).getMap().clone();
      try {
        mapEvt = new MapMonitorEvent(MonitorEvent.MAP_REMOVED,
                                     evt.getRemoteId(), map);
      } catch (VisADException ve) {
        ve.printStackTrace();
        mapEvt = null;
      }
      if (mapEvt != null) {
        notifyListeners(mapEvt);
      }
      break;
    case DisplayEvent.MAPS_CLEARED:
      boolean sendClear;
      try {
        sendClear = sync.isLocalClear();
      } catch (RemoteException re) {
        sendClear = false;
      }

      if (sendClear) {
        try {
          mapEvt = new MapMonitorEvent(MonitorEvent.MAPS_CLEARED,
                                       evt.getRemoteId(), null);
        } catch (VisADException ve) {
          ve.printStackTrace();
          mapEvt = null;
        }

        if (mapEvt != null) {
          notifyListeners(mapEvt);
        }
      }
      break;
    case DisplayEvent.REFERENCE_ADDED:
      DisplayReferenceEvent dre = (DisplayReferenceEvent )evt;

      DataDisplayLink link = dre.getDataDisplayLink();

      DataReference linkRef;
      try {
        linkRef = link.getDataReference();
      } catch (Exception e) {
        linkRef = null;
      }

      if (linkRef != null && linkRef instanceof DataReferenceImpl) {
        RemoteReferenceLinkImpl rrl;
        try {
          rrl = new RemoteReferenceLinkImpl(link);
        } catch (RemoteException re) {
          re.printStackTrace();
          // ignore attempt to link in a remote reference
          rrl = null;
        }

        if (rrl != null) {
          try {
            refEvt = new ReferenceMonitorEvent(MonitorEvent.REFERENCE_ADDED,
                                               evt.getRemoteId(), rrl);
          } catch (VisADException ve) {
            ve.printStackTrace();
            refEvt = null;
          }
          if (refEvt != null) {
            notifyListeners(refEvt);
          }
        }
      }
      break;

    case DisplayEvent.DESTROYED:
      // Hmmm ... not sure what we want to do here
      break;

    default:
      System.err.println("DisplayMonitorImpl.displayChanged: " + Name +
                         " got " + evt.getClass().getName() + " " + evt +
                         "=>" + evt.getDisplay());
      System.exit(1);
      break;
    }
  }

  /**
   * Return the ID associated with the specified <tt>RemoteDisplay</tt>.
   *
   * @return <tt>UNKNOWN_LISTENER_ID</tt> if not found;
   *         otherwise, returns the ID.
   */
  public int getConnectionID(RemoteDisplay rmtDpy)
  {
    synchronized (listeners) {
      ListIterator iter = listeners.listIterator();
      while (iter.hasNext()) {
        MonitorSyncer li = (MonitorSyncer )iter.next();

        if (li.isMonitored(rmtDpy)) {
          return li.getID();
        }
      }
    }

    return UNKNOWN_LISTENER_ID;
  }

  private int getNextListenerID()
  {
    synchronized (listeners) {
      if (nextListenerID == UNKNOWN_LISTENER_ID) {
        // don't let anyone have the magic ID
        nextListenerID++;
      }
      return nextListenerID++;
    }
  }

  /**
   * Returns <tt>true</tt> if there is a {@link MonitorEvent MonitorEvent}
   * for the specified {@link visad.Control Control} waiting to be delivered to
   * any listener.
   *
   * @param ctl The {@link visad.Control Control} being found.
   */
  public boolean hasEventQueued(Control ctl)
  {
    return hasEventQueued(0, ctl, true);
  }

  /**
   * Returns <tt>true</tt> if there is a {@link MonitorEvent MonitorEvent}
   * for the specified {@link visad.Control Control} waiting to be delivered to the
   * listener with the specified id.
   *
   * @param listenerID The identifier for the listener.
   * @param ctl The {@link visad.Control Control} being found.
   */
  public boolean hasEventQueued(int listenerID, Control ctl)
  {
    return hasEventQueued(listenerID, ctl, false);
  }


  /**
   * Returns <tt>true</tt> if there is a {@link MonitorEvent MonitorEvent}
   * for the specified {@link visad.Control Control} waiting to be delivered to the
   * listener with the specified id.
   *
   * @param listenerID The identifier for the listener.
   * @param ctl The {@link visad.Control Control} being found.
   * @param anyListener return <tt>true</tt> if there is a
   *                    {@link MonitorEvent MonitorEvent} queued for any
   *                    listener.
   */
  private boolean hasEventQueued(int listenerID, Control ctl,
                                 boolean anyListener)
  {
    boolean result = false;

    synchronized (listeners) {
      ListIterator iter = listeners.listIterator();
      while (iter.hasNext()) {
        MonitorSyncer li = (MonitorSyncer )iter.next();

        if (anyListener || listenerID == li.getID()) {
          result = li.hasControlEventQueued(ctl);
          if (!anyListener || result) {
            break;
          }
        }
      }
    }

    return result;
  }

  // WLH 12 April 2001
  public boolean isEmpty() {
    boolean result = true;
    synchronized (listeners) {
      ListIterator iter = listeners.listIterator();
      while (iter.hasNext()) {
        MonitorSyncer li = (MonitorSyncer )iter.next();
        if (!li.isEmpty()) result = false;
      }
    }
    return result;
  }

  /**
   * Returns <tt>true</tt> if there are listeners for this display.
   */
  private boolean hasListeners()
  {
    return (listeners.size() > 0);
  }

  /**
   * Handles ScalarMap data changes.<BR><BR>
   * If the {@link visad.ScalarMapEvent ScalarMapEvent} is not ignored, a
   * {@link MapMonitorEvent MapMonitorEvent} will be sent to all listeners.
   *
   * @param evt The details of the {@link visad.ScalarMap ScalarMap} change.
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

    ScalarMap mapClone = (ScalarMap )(evt.getScalarMap().clone());

    MapMonitorEvent mapEvt;
    try {
      mapEvt = new MapMonitorEvent(MonitorEvent.MAP_CHANGED,
                                   evt.getRemoteId(),  mapClone);
    } catch (VisADException ve) {
      ve.printStackTrace();
      mapEvt = null;
    }

    if (mapEvt != null) {
      notifyListeners(mapEvt);
    }
  }

  /**
   * Negotiates a listener identifier which is unique for both this
   * {@link DisplayMonitor DisplayMonitor} and the remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param rdm The remote {@link DisplayMonitor DisplayMonitor} to negotiate with.
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

    int rmtID = getNextListenerID();

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
   * Forwards the {@link MonitorEvent MonitorEvent} to all the listeners
   * associated with this {@link DisplayMonitor DisplayMonitor}.
   *
   * @param evt The event to forward.
   */
  public void notifyListeners(MonitorEvent evt)
  {
    final int evtID = evt.getOriginator();

    synchronized (listeners) {
      ListIterator iter = listeners.listIterator();
      while (iter.hasNext()) {
        MonitorSyncer li = (MonitorSyncer )iter.next();

        if (li.isDead()) {
          // notify Display that this connection is gone
          myDisplay.lostCollabConnection(li.getID());

          // delete dead listeners
          iter.remove();
          continue;
        }

        if (evtID == li.getID()) {
          // don't return event to its source
          continue;
        }

        li.addEvent(evt);
      }
    }
  }

  /**
   * Handles <tt>MessageEvent</tt> forwarding.
   *
   * @param msg The message to forward.
   */
  public void receiveMessage(MessageEvent msg)
  {
    // don't bother if nobody's listening
    if (!hasListeners()) {
      return;
    }

    MessageMonitorEvent msgEvt;
    try {
      msgEvt = new MessageMonitorEvent(MonitorEvent.MESSAGE_SENT,
                                   msg.getOriginatorId(),  msg);
    } catch (RemoteException re) {
      re.printStackTrace();
      msgEvt = null;
    } catch (VisADException ve) {
      ve.printStackTrace();
      msgEvt = null;
    }

    if (msgEvt != null) {
      notifyListeners(msgEvt);
    }
  }

  /**
   * Stops forwarding {@link MonitorEvent MonitorEvent}s to the specified listener.
   *
   * @param l Listener to remove.
   */
  public void removeListener(MonitorCallback l)
  {
    if (l != null) {
      synchronized (listeners) {
        ListIterator iter = listeners.listIterator();
        while (iter.hasNext()) {
          MonitorSyncer li = (MonitorSyncer )iter.next();
          if (li.getListener().equals(l)) {
            iter.remove();
            break;
          }
        }
      }
    }
  }

  /**
   * Set the display synchronization object for this display
   */
  public void setDisplaySync(DisplaySync sync)
  {
    this.sync = sync;
  }

  /**
   * Returns the name of this {@link DisplayMonitor DisplayMonitor}.
   */
  public String toString()
  {
    return Name;
  }
}
