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

import java.io.Serializable;

import java.rmi.RemoteException;

/**
 * <CODE>DisplayMonitor</CODE> is the interface for objects which monitor
 * the state of <CODE>Control</CODE>s, <CODE>Display</CODE>s and
 * <CODE>ScalarMap</CODE>s.
 */
public interface DisplayMonitor
  extends ControlListener, DisplayListener, ScalarMapListener, Serializable
{
  /**
   * Adds the specified listener to receive <CODE>MonitorEvent</CODE>s
   * when the monitored <CODE>Display</CODE>'s state changes.
   *
   * @param listener The listener to add.
   * @param id The unique listener identifier.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception VisADException If the listener <CODE>Vector</CODE>
   * 				is uninitialized.
   */
  void addListener(DisplayMonitorListener listener, int id)
    throws RemoteException, VisADException;

  /**
   * Initializes links so that <CODE>MonitorEvent</CODE>s will be
   * exchanged with the specified remote <CODE>Display</CODE>.
   *
   * @param rd The remote <CODE>Display</CODE> to synchronize.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If the inter-<CODE>Display</CODE>
   * 					links could not be made.
   */
  void addRemoteListener(RemoteDisplay rd)
    throws RemoteException, RemoteVisADException;

  /**
   * Caches this (remotely-originated) <CODE>MonitorEvent</CODE>
   * for future reference.
   *
   * @param evt The event to cache.
   *
   * @exception RemoteException If there was an RMI-related problem.
   */
  void addRemoteMonitorEvent(MonitorEvent evt)
    throws RemoteException;

  /**
   * Returns a suggestion for a unique listener identifier which is
   * equal to or greater than the supplied ID.
   *
   * @param id The identifier to check.
   *
   * @exception RemoteException If there was an RMI-related problem.
   */
  int checkID(int id)
    throws RemoteException;

  /**
   * Returns <CODE>true</CODE> if there is a <CODE>MonitorEvent</CODE>
   * for the specified <CODE>Control</CODE> waiting to be delivered to the
   * listener with the specified id.
   *
   * @param listenerID The identifier for the listener.
   * @param ctl The <CODE>Control</CODE> being found.
   *
   * @exception RemoteException If there was an RMI-related problem.
   */
  boolean hasEventQueued(int listenerID, Control ctl)
    throws RemoteException;

  /**
   * Forwards the <CODE>MonitorEvent</CODE> to all the listeners
   * associated with this <CODE>DisplayMonitor</CODE>.
   *
   * @param evt The event to forward.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If there is an internal error.
   */
  void notifyListeners(MonitorEvent evt)
    throws RemoteException, RemoteVisADException;

  /**
   * Synchronizes the monitored <CODE>Display</CODE>'s controls with those
   * of all the remote listeners.<BR><BR>
   * (Overwrites the current state of all controls for the
   * <CODE>Display</CODE> being monitored, so should be used with caution.)
   *
   * @exception RemoteVisADException If there was a problem synchronizing
   * 					with the remote <CODE>Display</CODE>.
   * @exception RemoteException If there was an RMI-related problem.
   */
  void syncControls()
    throws RemoteException, RemoteVisADException;
}
