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

import java.rmi.server.UnicastRemoteObject;

import visad.RemoteVisADException;
import visad.VisADException;

/**
 * <CODE>RemoteDisplaySyncImpl</CODE> is the implementation of the VisAD
 * <CODE>RemoteDisplaySync</CODE> class.
 */
public class RemoteDisplaySyncImpl
  extends UnicastRemoteObject
  implements RemoteDisplaySync
{
  private final transient DisplaySyncImpl AdaptedSync;

  /**
   * Creates a remotely-accessible wrapper for the specified
   * <CODE>DisplaySync</CODE>
   *
   * @param dpySync The local <CODE>DisplaySync</CODE> object to adapt.
   *
   * @exception RemoteException If there was an RMI-related problem.
   */
  public RemoteDisplaySyncImpl(DisplaySyncImpl dpySync)
    throws RemoteException
  {
    AdaptedSync = dpySync;
  }

  /**
   * Forwards the event to the adapted remote <CODE>DisplaySync</CODE>
   * object.
   *
   * @param evt The event to forward.
   *
   * @exception RemoteException If there was an RMI-related problem.
   * @exception RemoteVisADException If there was an internal problem.
   */
  public void stateChanged(MonitorEvent evt)
    throws RemoteException, RemoteVisADException
  {
    if (AdaptedSync == null) {
      throw new RemoteVisADException("AdaptedSync is null");
    }

    AdaptedSync.stateChanged(evt);
  }
}
