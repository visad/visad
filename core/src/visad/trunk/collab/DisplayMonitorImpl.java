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

import visad.AnimationControl;
import visad.Control;
import visad.ControlEvent;
import visad.DataDisplayLink;
import visad.DisplayEvent;
import visad.DisplayImpl;
import visad.DisplayMapEvent;
import visad.DisplayReferenceEvent;
import visad.RemoteDisplay;
import visad.RemoteReferenceLinkImpl;
import visad.RemoteVisADException;
import visad.ScalarMap;
import visad.ScalarMapControlEvent;
import visad.ScalarMapEvent;
import visad.VisADException;

/**
 * <TT>DisplayMonitorImpl</TT> is the <TT>Display</TT> monitor
 * implementation.<P>
 * <TT>DisplayMonitorImpl</TT> is not <TT>Serializable</TT> and
 * should not be copied between JVMs.<P>
 */
public class DisplayMonitorImpl
  implements DisplayMonitor
{
  /**
   * The name of this display monitor.
   */
  private String Name;

  /**
   * The list of objects interested in changes to the monitored
   * <TT>Display</TT>.
   */
  private transient MonitorBroadcaster broadcaster;

  /**
   * Creates a monitor for the specified <TT>Display</TT>.
   *
   * @param dpy The <TT>Display</TT> to monitor.
   *
   * @exception VisADException If the <TT>Display</TT> encountered
   * 				problems adding this new object as a
   * 				<TT>DisplayListener</TT>.
   */
  public DisplayMonitorImpl(DisplayImpl dpy)
  {
    Name = dpy.getName() + " Monitor";

    dpy.addDisplayListener(this);
    broadcaster = new MonitorBroadcaster(dpy);
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
    broadcaster.addListener(listener, id);
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
    broadcaster.addRemoteListener(rd);
  }

  /**
   * Returns a suggestion for a unique listener identifier which is
   * equal to or greater than the supplied ID.
   *
   * @param id The identifier to check.
   */
  public int checkID(int id)
  {
    return broadcaster.checkID(id);
  }

  /**
   * Handles <TT>Control</TT> changes.<BR><BR>
   * If the <TT>ControlEvent</TT> is not ignored, a
   * <TT>ControlMonitorEvent</TT> will be sent to all listeners.
   *
   * @param e The details of the <TT>Control</TT> change.
   */
  public void controlChanged(ControlEvent evt)
  {
    // don't bother if nobody's listening
    if (!broadcaster.hasListeners()) {
      return;
    }

    if (evt.getControl() instanceof AnimationControl) {
      // ignore all animation-related events
      return;
    }

    Control ctlClone = (Control )(evt.getControl().clone());
    ControlMonitorEvent ctlEvt;
    try {
      ctlEvt = new ControlMonitorEvent(MonitorEvent.CONTROL_CHANGED, ctlClone);
    } catch (VisADException ve) {
      ve.printStackTrace();
      ctlEvt = null;
    }

    if (ctlEvt != null) {
      broadcaster.notifyListeners(ctlEvt);
    }
  }

  /**
   * Handles ScalarMap control changes.<BR>
   * <FONT SIZE="-1">This is just a stub which ignores the event.</FONT>
   *
   * @param e The details of the <TT>ScalarMap</TT> change.
   */
  public void controlChanged(ScalarMapControlEvent evt)
  {
    // don't bother if nobody's listening
    if (!broadcaster.hasListeners()) {
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
      controlChanged(new ControlEvent(ctl));
      ctl.addControlListener(this);
    }
  }

  /**
   * Handles notification of objects being added to or removed from
   * the <TT>Display</TT>.<BR><BR>
   * If the <TT>DisplayEvent</TT> is not ignored, a
   * <TT>MapMonitorEvent</TT> or <TT>ReferenceMonitorEvent</TT>
   * will be sent to all listeners.
   *
   * @param e The details of the <TT>Display</TT> change.
   */
  public void displayChanged(DisplayEvent evt)
  {
    // don't bother if nobody's listening
    if (!broadcaster.hasListeners()) {
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
        mapEvt = new MapMonitorEvent(MonitorEvent.MAP_ADDED, map);
      } catch (VisADException ve) {
        ve.printStackTrace();
        mapEvt = null;
      }
      if (mapEvt != null) {
        broadcaster.notifyListeners(mapEvt);
      }
      break;
    case DisplayEvent.MAPS_CLEARED:
      try {
        mapEvt = new MapMonitorEvent(MonitorEvent.MAPS_CLEARED, null);
      } catch (VisADException ve) {
        ve.printStackTrace();
        mapEvt = null;
      }
      if (mapEvt != null) {
        broadcaster.notifyListeners(mapEvt);
      }
      break;
    case DisplayEvent.REFERENCE_ADDED:
      DisplayReferenceEvent dre = (DisplayReferenceEvent )evt;

      DataDisplayLink link = dre.getDataDisplayLink();

      RemoteReferenceLinkImpl rrl;
      try {
        rrl = new RemoteReferenceLinkImpl(link);
      } catch (RemoteException re) {
        // ignore attempt to link in a remote reference
        rrl = null;
      }

      if (rrl != null) {
        try {
          refEvt = new ReferenceMonitorEvent(MonitorEvent.REFERENCE_ADDED,
                                             rrl);
        } catch (VisADException ve) {
          ve.printStackTrace();
          refEvt = null;
        }
        if (refEvt != null) {
          broadcaster.notifyListeners(refEvt);
        }
      }
      break;

    default:
      System.err.println("DisplayMonitorImpl.displayChange: " + Name +
                         " got " + evt.getClass().getName() + " " + evt +
                         "=>" + evt.getDisplay());
      System.exit(1);
      break;
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
    return broadcaster.hasEventQueued(listenerID, ctl);
  }

  /**
   * Handles ScalarMap data changes.<BR><BR>
   * If the <TT>ScalarMapEvent</TT> is not ignored, a
   * <TT>MapMonitorEvent</TT> will be sent to all listeners.
   *
   * @param e The details of the <TT>ScalarMap</TT> change.
   */
  public void mapChanged(ScalarMapEvent evt)
  {
    // don't bother if nobody's listening
    if (!broadcaster.hasListeners()) {
      return;
    }

    if (evt.getId() == ScalarMapEvent.AUTO_SCALE) {
      // ignore internal autoscale events
      return;
    }

    ScalarMap mapClone = (ScalarMap )(evt.getScalarMap().clone());

    MapMonitorEvent mapEvt;
    try {
      mapEvt = new MapMonitorEvent(MonitorEvent.MAP_CHANGED, mapClone);
    } catch (VisADException ve) {
      ve.printStackTrace();
      mapEvt = null;
    }

    if (mapEvt != null) {
      broadcaster.notifyListeners(mapEvt);
    }
  }

  /**
   * Forwards the <TT>MonitorEvent</TT> to all the listeners
   * associated with this <TT>DisplayMonitor</TT>.
   *
   * @param evt The event to forward.
   */
  public void notifyListeners(MonitorEvent evt)
  {
    broadcaster.notifyListeners(evt);
  }

  /**
   * Returns the name of this <TT>DisplayMonitor</TT>.
   */
  public String toString()
  {
    return Name;
  }
}
