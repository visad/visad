//
// RemoteServer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

/**
   RemoteServer is the interface for serving RemoteDataReferences.
   A RemoteServerImpl should be bound to a URL via Naming.rebind,
   and accessed remotely via this RemoteServer interface.<P>
*/
public interface RemoteServer extends Remote {

  /** return the RemoteDataReference with index on this
      RemoteServer, or null */
  RemoteDataReference getDataReference(int index)
         throws RemoteException;

  /** return the RemoteDataReference with name on this
      RemoteServer, or null */
  RemoteDataReference getDataReference(String name)
         throws VisADException, RemoteException;

  /** return an array of all RemoteDataReferences on this
      RemoteServer, or null */
  RemoteDataReference[] getDataReferences()
         throws RemoteException;

  /** add a new RemoteDataReferenceImpl to server and extend array */
  void addDataReference(RemoteDataReferenceImpl ref)
         throws RemoteException;

  /** set array of all RemoteDataReferences on this RemoteServer */
  void setDataReferences(RemoteDataReferenceImpl[] rs)
         throws RemoteException;

  /** remove a RemoteDataReferenceImpl from server and shrink size of array */
  void removeDataReference(RemoteDataReferenceImpl ref)
         throws RemoteException;

  /** return array of all RemoteDisplays in this RemoteServer */
  RemoteDisplay[] getDisplays()
         throws RemoteException;

  /** get a RemoteDisplay by index */
  RemoteDisplay getDisplay(int index)
         throws RemoteException;

  /** get a RemoteDisplay by name */
  RemoteDisplay getDisplay(String name)
         throws VisADException, RemoteException;

  /** add a new RemoteDisplayImpl to server and extend array */
  void addDisplay(RemoteDisplayImpl rd)
         throws RemoteException;

  /** set all RemoteDisplayImpls to serve */
  void setDisplays(RemoteDisplayImpl[] rd)
         throws RemoteException;

  /** remove a RemoteDisplayImpl from server and shrink size of array */
  void removeDisplay(RemoteDisplayImpl rd)
         throws RemoteException;
}

