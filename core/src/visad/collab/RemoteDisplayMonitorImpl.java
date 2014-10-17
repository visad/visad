/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.server.UnicastRemoteObject;

import visad.Control;
import visad.ControlEvent;
import visad.DisplayEvent;
import visad.MessageEvent;
import visad.RemoteDisplay;
import visad.RemoteVisADException;
import visad.ScalarMapControlEvent;
import visad.ScalarMapEvent;
import visad.VisADException;

/**
 * <CODE>RemoteDisplayMonitorImpl</CODE> is the implementation of the VisAD
 * {@link RemoteDisplayMonitor RemoteDisplayMonitor} class.
 */
public class RemoteDisplayMonitorImpl
  extends UnicastRemoteObject
  implements RemoteDisplayMonitor
{
  private final transient DisplayMonitorImpl AdaptedMonitor;

  /**
   * Creates a remotely-accessible wrapper for the specified
   * {@link DisplayMonitor DisplayMonitor}
   *
   * @param dpyMonitor The local DisplayMonitor object to adapt.
   *
   * @exception RemoteException If there was an RMI-related problem.
   */
  public RemoteDisplayMonitorImpl(DisplayMonitorImpl dpyMonitor)
    throws RemoteException
  {
    AdaptedMonitor = dpyMonitor;
  }

  /**
   * Forwards the event to the adapted remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param e The {@link visad.DisplayEvent DisplayEvent} to forward.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If there was an internal problem.
   */
  public void displayChanged(DisplayEvent e)
    throws RemoteException, RemoteVisADException
  {
    if (AdaptedMonitor == null) {
      throw new RemoteVisADException("AdaptedMonitor is null");
    }
    AdaptedMonitor.displayChanged(e);
  }

  /**
   * Forwards the event to the adapted remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param e The {@link visad.ScalarMapEvent ScalarMapEvent} to forward.
   */
  public void mapChanged(ScalarMapEvent e)
  {
    if (AdaptedMonitor != null) {
      AdaptedMonitor.mapChanged(e);
    }
  }

  /**
   * Forwards the event to the adapted remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param e The {@link visad.ScalarMapEvent ScalarMapEvent} to forward.
   */
  public void controlChanged(ScalarMapControlEvent e)
  {
    if (AdaptedMonitor != null) {
      AdaptedMonitor.controlChanged(e);
    }
  }

  /**
   * Forwards the event to the adapted remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param e ControlEvent to forward
   *
   * @exception RemoteVisADException If there was an internal problem.
   */
  public void controlChanged(ControlEvent e)
    throws RemoteVisADException
  {
    if (AdaptedMonitor == null) {
      throw new RemoteVisADException("AdaptedMonitor is null");
    }
    AdaptedMonitor.controlChanged(e);
  }

  /**
   * Asks remote {@link DisplayMonitor DisplayMonitor} to check this ID for uniqueness.
   *
   * @param id The identifier to check for uniqueness.
   *
   * @exception RemoteException If there was an RMI-related problem.
   */
  public int checkID(int id)
    throws RemoteException
  {
    if (AdaptedMonitor == null) {
      throw new RemoteException("AdaptedMonitor is null");
    }
    return AdaptedMonitor.checkID(id);
  }

  /** destroy this monitor */
  public void destroy()
    throws RemoteVisADException
  {
    throw new RemoteVisADException("Cannot destroy RemoteDisplayMonitor");
  }

  /**
   * Adds this listener to the remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param l The listener to add
   * @param id The unique identifer (determined with <tt>checkID</tt>.)
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If there was an internal problem.
   */
  public void addListener(MonitorCallback l, int id)
    throws RemoteException, RemoteVisADException
  {
    if (AdaptedMonitor == null) {
      throw new RemoteVisADException("AdaptedMonitor is null");
    }
    try {
      AdaptedMonitor.addListener(l, id);
    } catch (VisADException ve) {
      throw new RemoteVisADException(ve.getMessage());
    }
  }

  /**
   * Adds this remote display to the remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param rd The remote display to add
   * @param id The unique identifer (determined with <tt>checkID</tt>.)
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If there was an internal problem.
   */
  public void addListener(RemoteDisplay rd, int id)
    throws RemoteException, RemoteVisADException
  {
    if (AdaptedMonitor == null) {
      throw new RemoteVisADException("AdaptedMonitor is null");
    }
    try {
      AdaptedMonitor.addListener(rd, id);
    } catch (VisADException ve) {
      throw new RemoteVisADException(ve.getMessage());
    }
  }

  /**
   * Unusable stub.  Cannot connect two RemoteDisplayMonitors.
   *
   * @param rd Ignored.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException <B>ALWAYS</B> thrown.
   */
  public void addRemoteListener(RemoteDisplay rd)
    throws RemoteException, RemoteVisADException
  {
    throw new RemoteVisADException("Cannot connect two RemoteDisplayMonitors");
  }

  /**
   * Return the ID associated with the specified <tt>RemoteDisplay</tt>.
   *
   * @return <tt>UNKNOWN_LISTENER_ID</tt> if not found;
   *         otherwise, returns the ID.
   */
  public int getConnectionID(RemoteDisplay rmtDpy)
  throws RemoteException
  {
    if (AdaptedMonitor == null) {
      return UNKNOWN_LISTENER_ID;
    }
    return AdaptedMonitor.getConnectionID(rmtDpy);
  }

  /**
   * Forwards the event to the remote {@link DisplayMonitor DisplayMonitor}.
   *
   * @param evt The event to forward.
   *
   * @exception RemoteVisADException If there was an internal problem.
   */
  public void notifyListeners(MonitorEvent evt)
    throws RemoteVisADException
  {
    if (AdaptedMonitor == null) {
      throw new RemoteVisADException("AdaptedMonitor is null");
    }
    AdaptedMonitor.notifyListeners(evt);
  }

  /**
   * Unusable stub.  Unimplemented.
   *
   * @param originator Ignored.
   * @param ctl Ignored.
   *
   * @exception RemoteException <B>ALWAYS</B> thrown.
   */
  public boolean hasEventQueued(int originator, Control ctl)
    throws RemoteException
  {
    throw new RemoteException("Unimplemented");
  }

  // WLH 12 April 2001
  public boolean isEmpty()
        throws RemoteException {
    throw new RemoteException("Unimplemented");
  }

  /**
   * Unusable stub.  Unimplemented.
   *
   * @param ctl Ignored.
   *
   * @exception RemoteException <B>ALWAYS</B> thrown.
   */
  public boolean hasEventQueued(Control ctl)
    throws RemoteException
  {
    throw new RemoteException("Unimplemented");
  }

  /**
   * Handles <tt>MessageEvent</tt> forwarding.
   *
   * @param msg The message to forward.
   */
  public void receiveMessage(MessageEvent msg)
    throws RemoteException
  {
    if (AdaptedMonitor == null) {
      throw new RemoteException("AdaptedMonitor is null");
    }
    AdaptedMonitor.receiveMessage(msg);
  }

  /**
   * Set the display synchronization object for this display
   */
  public void setDisplaySync(DisplaySync sync)
    throws RemoteException
  {
    throw new RemoteException("Unimplemented");
  }

  /**
   * Returns a string representation of this object.
   */
  public String toString()
  {
    return "Remote " + AdaptedMonitor;
  }
}
