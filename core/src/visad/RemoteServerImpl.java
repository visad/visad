//
// RemoteServerImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.server.UnicastRemoteObject;

public class RemoteServerImpl extends UnicastRemoteObject
       implements RemoteServer
{
  private RemoteDataReferenceImpl[] refs;
  private RemoteDisplayImpl[] dpys;

  public RemoteServerImpl()
         throws RemoteException {
    this(null, null);
  }

  public RemoteServerImpl(RemoteDataReferenceImpl[] rs)
         throws RemoteException {
    this(rs, null);
  }

  public RemoteServerImpl(RemoteDisplayImpl[] rd)
         throws RemoteException {
    this(null, rd);
  }

  /** construct a RemoteServerImpl and initialize it with
      an array of RemoteDataReferenceImpls */
  public RemoteServerImpl(RemoteDataReferenceImpl[] rs, RemoteDisplayImpl[] rd)
         throws RemoteException {
    super();
    refs = rs;
    dpys = rd;
  }

  /** get a RemoteDataReference by index */
  public synchronized RemoteDataReference getDataReference(int index)
         throws RemoteException {
    if (refs != null && 0 <= index && index < refs.length) return refs[index];
    else return null;
  }

  /** get a RemoteDataReference by name */
  public synchronized RemoteDataReference getDataReference(String name)
         throws VisADException, RemoteException {
    if (name != null && refs != null) {
      for (int i=0; i<refs.length; i++) {
        if (name.equals(refs[i].getName())) return refs[i];
      }
    }
    return null;
  }

  /** return array of all RemoteDataReferences in this RemoteServer */
  public synchronized RemoteDataReference[] getDataReferences()
         throws RemoteException {
    if (refs == null || refs.length == 0) return null;
    // is this copy necessary?
    RemoteDataReference[] rs =
      new RemoteDataReference[refs.length];
    for (int i=0; i<refs.length; i++) rs[i] = refs[i];
    return rs;
  }

  /** set one RemoteDataReference in the array on this
      RemoteServer (and extend length of array if necessary) */
  public synchronized void setDataReference(int index, RemoteDataReferenceImpl ref)
         throws VisADException {
    if (index < 0) {
      throw new RemoteVisADException("RemoteServerImpl.setDataReference: " +
                                     "negative index");
    }
    if (refs == null || index >= refs.length) {
      RemoteDataReferenceImpl[] rs = new RemoteDataReferenceImpl[index + 1];
      for (int i=0; i<index; i++) {
        if (refs != null && i < refs.length) rs[i] = refs[i];
        else rs[i] = null;
      }
      refs = rs;
    }
    refs[index] = ref;
  }

  /**
   * Add a DataReferenceImpl to server (after wrapping it in
   * a RemoteDataReferenceImpl)
   */
  public void addDataReference(DataReferenceImpl ref)
    throws RemoteException
  {
    addDataReference(new RemoteDataReferenceImpl(ref));
  }

  /** add a new RemoteDataReferenceImpl to server and extend array */
  public synchronized void addDataReference(RemoteDataReferenceImpl ref) {
    if (ref == null) return;

    int len;
    if (refs == null || refs.length == 0) len = 0;
    else len = refs.length;

    RemoteDataReferenceImpl[] nr = new RemoteDataReferenceImpl[len + 1];

    if (len > 0) System.arraycopy(refs, 0, nr, 0, len);

    nr[len] = ref;
    refs = nr;
  }

  /** set array of all RemoteDataReferences on this RemoteServer */
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

  /** remove a RemoteDataReferenceImpl from server and shrink size of array */
  public synchronized void removeDataReference(RemoteDataReferenceImpl ref) {
    int len;
    if (refs == null || refs.length == 0) len = 0;
    else len = refs.length;

    int index = -1;
    for (int i=0; i<len; i++) {
      if (refs[i] == ref) {
        index = i;
        break;
      }
    }
    if (index < 0) return;

    RemoteDataReferenceImpl[] nr = new RemoteDataReferenceImpl[len - 1];
    if (index > 0) System.arraycopy(refs, 0, nr, 0, index);
    if (index < len - 1) {
      System.arraycopy(refs, index + 1, nr, index, len - index - 1);
    }

    refs = nr;
  }

  /** return array of all RemoteDisplays in this RemoteServer */
  public RemoteDisplay[] getDisplays()
         throws RemoteException
  {
    if (dpys == null || dpys.length == 0) return null;
    // is this copy necessary?
    RemoteDisplay[] rd =
      new RemoteDisplay[dpys.length];
    for (int i=0; i<dpys.length; i++) rd[i] = dpys[i];
    return rd;
  }

  /** get a RemoteDisplay by index */
  public RemoteDisplay getDisplay(int index)
  {
    if (dpys != null && index >= 0 && index < dpys.length) {
      return dpys[index];
    }
    return null;
  }

  /** get a RemoteDisplay by name */
  public RemoteDisplay getDisplay(String name)
	throws VisADException, RemoteException
  {
    if (dpys == null) {
      throw new RemoteException("No displays associated with this server");
    }

    for (int i=0; i<dpys.length; i++) {
      if (dpys[i] == null) {
	continue;
      }
      if (dpys[i].getName().equals(name)) {
	return dpys[i];
      }
    }

    throw new RemoteException("Display \"" + name + "\" not found");
  }

  /** add DisplayImpl to server (after wrapping it in a RemoteDisplayImpl) */
  public void addDisplay(DisplayImpl di)
    throws RemoteException
  {
    addDisplay(new RemoteDisplayImpl(di));
  }

  /** add a new RemoteDisplayImpl to server and extend array */
  public synchronized void addDisplay(RemoteDisplayImpl rd) {
    if (rd == null) {
      return;
    }

    int len;
    if (dpys == null || dpys.length == 0) {
      len = 0;
    } else {
      len = dpys.length;
    }

    RemoteDisplayImpl[] nd = new RemoteDisplayImpl[len + 1];

    if (len > 0) System.arraycopy(dpys, 0, nd, 0, len);

    nd[len] = rd;
    dpys = nd;
  }

  /** set all RemoteDisplayImpls to serve */
  public synchronized void setDisplays(RemoteDisplayImpl[] rd) {
    if (rd == null) {
      dpys = null;
      return;
    }
    dpys = new RemoteDisplayImpl[rd.length];
    for (int i=0; i<dpys.length; i++) {
      dpys[i] = rd[i];
    }
  }

  /** remove a RemoteDisplayImpl from server and shrink size of array */
  public synchronized void removeDisplay(RemoteDisplayImpl rd) {
    //
    int len;
    if (dpys == null || dpys.length == 0) len = 0;
    else len = dpys.length;

    int index = -1;
    for (int i=0; i<len; i++) {
      if (dpys[i] == rd) {
        index = i;
        break;
      }
    }
    if (index < 0) return;

    RemoteDisplayImpl[] nd = new RemoteDisplayImpl[len - 1];
    if (index > 0) System.arraycopy(dpys, 0, nd, 0, index);
    if (index < len - 1) {
      System.arraycopy(dpys, index + 1, nd, index, len - index - 1);
    }

    dpys = nd;
  }

}

