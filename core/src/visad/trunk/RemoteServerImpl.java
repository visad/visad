
//
// RemoteServerImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;
 
import visad.*;
 
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
 
public class RemoteServerImpl extends UnicastRemoteObject
       implements RemoteServer
{
  private RemoteDataReferenceImpl[] refs;
 
  public RemoteServerImpl(RemoteDataReferenceImpl[] rs)
         throws RemoteException {
    super();
    refs = rs;
  }
 
  public synchronized RemoteDataReference getDataReference(int index)
         throws RemoteException {
    if (0 <= index && index < refs.length) return refs[index];
    else return null;
  }
 
  /** get a RemoteDataReference by name */
  public synchronized RemoteDataReference getDataReference(String name)
         throws VisADException, RemoteException {
    if (name == null) return null;
    for (int i=0; i<refs.length; i++) {
      if (name.equals(refs[i].getName())) return refs[i];
    }
    return null;
  }

  /** return array of all RemoteDataReferences in this RemoteServer */
  public synchronized RemoteDataReference[] getDataReferences()
         throws RemoteException {
    if (refs == null || refs.length == 0) return null;
    // is this copy necessary?
    RemoteDataReferenceImpl[] rs =
      new RemoteDataReferenceImpl[refs.length];
    for (int i=0; i<refs.length; i++) rs[i] = refs[i];
    return rs;
  }

  /** set a single RemoteDataReferenceImpl to serve */
  public synchronized void setDataReference(int index, RemoteDataReferenceImpl ref)
         throws VisADException {
    if (index < 0) {
      throw new RemoteVisADException("RemoteServerImpl.setDataReference: " +
                                     "negative index");
    }
    if (index >= refs.length) {
      RemoteDataReferenceImpl[] rs = new RemoteDataReferenceImpl[index + 1];
      for (int i=0; i<index; i++) {
        if (i < refs.length) rs[i] = refs[i];
        else rs[i] = null;
      }
      refs = rs;
    }
    refs[index] = ref;
  }

  /** set all RemoteDataReferenceImpls to serve */
  public synchronized void setDataReferences(RemoteDataReferenceImpl[] rs) {
    if (rs == null) {
      refs = null;
      return;
    }
    refs = new RemoteDataReferenceImpl[rs.length];
    for (int i=0; i<refs.length; i++) {
      refs[i] = rs[i];
    }
  }

}

