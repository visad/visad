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

import java.io.EOFException;

import java.rmi.ConnectException;
import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import visad.Control;
import visad.DisplayImpl;
import visad.RemoteDisplay;
import visad.RemoteVisADException;
import visad.VisADException;

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
      list.add(new MonitorListener(myDisplay.getName(), listener, id));
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
    synchronized (list) {
      ListIterator iter = list.listIterator();
      while (iter.hasNext()) {
        MonitorListener li = (MonitorListener )iter.next();
        if (li.getID() == id) {
          if (nextListenerID == id) {
            nextListenerID++;
          }
          nextListenerID++;
        }
        id = nextListenerID;
        break;
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

    synchronized (list) {
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
  }

  /**
   * Stops forwarding <TT>MonitorEvent</TT>s to the specified listener.
   *
   * @param l Listener to remove.
   */
  public void removeListener(DisplayMonitorListener l)
  {
    if (l != null) {
      synchronized (list) {
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
    boolean result = false;

    synchronized (list) {
      ListIterator iter = list.listIterator();
      while (iter.hasNext()) {
        MonitorListener li = (MonitorListener )iter.next();

        if (listenerID == li.getID()) {
          result = li.hasControlEventQueued(ctl);
          break;
        }
      }
    }

    return result;
  }
}
