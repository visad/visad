
//
// DataReferenceImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

import java.util.*;
import java.rmi.*;

/**
   DataReferenceImpl is a local implementation of DataReference.<P>

   DataReferenceImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DataReferenceImpl extends Object implements DataReference {

  /** name of scalar type */
  private String Name;

  /** Data object refered to (mutable);
      DataReferenceImpl is not Serializable, but mark as transient anyway */
  private transient Data data;
  /** ref = this if data is local
      ref = a RemoteDataReferenceImpl if data is remote;
      DataReferenceImpl is not Serializable, but mark as transient anyway */
  private transient DataReference ref;

  /** Tick increments each time data changes */
  private long Tick;

  /** Vector of DataChangedListeners;
      DataReferenceImpl is not Serializable, but mark as transient anyway */
  transient Vector ListenerVector = new Vector();

  public DataReferenceImpl(String name) throws VisADException {
    if (name == null) {
      throw new VisADException("DataReference: name cannot be null");
    }
    Name = name;
    Tick = Long.MIN_VALUE + 1;
  }

  public synchronized Data getData() {
    return data;
  }

  /** set this DataReferenceImpl to refer to d;
      must be local DataImpl */
  public synchronized void setData(Data d)
         throws VisADException, RemoteException {
    if (d == null) {
      throw new ReferenceException("DataReferenceImpl: data cannot be null");
    }
    if (!(d instanceof DataImpl)) {
      throw new RemoteVisADException("DataReferenceImpl.setData: must use " +
                                     "RemoteDataReference for RemoteData");
    }
    if (data != null) data.removeReference(ref);
    ref = this;
    data = d;
    d.addReference(ref);
    incTick();
  }

  /** method for use by RemoteDataReferenceImpl that adapts this
      DataReferenceImpl */
  synchronized void adaptedSetData(RemoteData d, RemoteDataReference r)
               throws VisADException, RemoteException {
    if (data != null) data.removeReference(ref);
    ref = r;
    data = d;
    d.addReference(ref);
    incTick();
  }

  public long getTick() {
    return Tick;
  }

  /** synchronized to because incTick, setData, and adaptedSetData
      share access to data and ref */
  public synchronized long incTick()
         throws VisADException, RemoteException {
    Tick += 1;
    if (Tick == Long.MAX_VALUE) Tick = Long.MIN_VALUE + 1;
    if (ListenerVector == null) return Tick;
    synchronized (ListenerVector) {
      Enumeration listeners = ListenerVector.elements();
      while (listeners.hasMoreElements()) {
        DataChangedOccurrence e = new DataChangedOccurrence(ref, Tick);
        DataChangedListener listener =
          (DataChangedListener) listeners.nextElement();
        if (listener.getBall()) {
          Action a = listener.getAction();
          a.dataChanged(e);
          listener.setBall(false);
        }
        else {
          listener.setDataChangedOccurrence(e);
        }
      }
    }
    return Tick;
  }

  public DataChangedOccurrence acknowledgeDataChanged(Action a)
         throws VisADException {
    if (!(a instanceof ActionImpl)) {
      throw new RemoteVisADException("DataReferenceImpl.acknowledgeDataChanged:" +
                                     " Action must be local");
    }
    if (ListenerVector == null) return null;
    DataChangedListener listener = findDataChangedListener(a);
    DataChangedOccurrence e = listener.getDataChangedOccurrence();
    listener.setDataChangedOccurrence(null);
    if (e == null) {
      listener.setBall(true);
    }
    return e;
  }

  public DataChangedOccurrence adaptedAcknowledgeDataChanged(RemoteAction a)
         throws VisADException {
    if (ListenerVector == null) return null;
    DataChangedListener listener = findDataChangedListener(a);
    DataChangedOccurrence e = listener.getDataChangedOccurrence();
    listener.setDataChangedOccurrence(null);
    if (e == null) listener.setBall(true);
    return e;
  }

  /** find DataChangedListener with action */
  public DataChangedListener findDataChangedListener(Action a)
         throws VisADException {
    if (a == null) {
      throw new ReferenceException("DataReferenceImpl.findDataChangedListener: " +
                                   "Action cannot be null");
    }
    if (ListenerVector == null) return null;
    synchronized (ListenerVector) {
      Enumeration listeners = ListenerVector.elements();
      while (listeners.hasMoreElements()) {
        DataChangedListener listener =
          (DataChangedListener) listeners.nextElement();
        if (a.equals(listener.getAction())) return listener;
      }
    }
    return null;
  }

  public String getName() {
    return Name;
  }

  /** addDataChangedListener and removeDataChangedListener provide
      DataChangedOccurrence source semantics;
      Action must be local ActionImpl */
  public void addDataChangedListener(Action a)
         throws VisADException {
    if (!(a instanceof ActionImpl)) {
      throw new RemoteVisADException("DataReferenceImpl.addDataChanged" +
                                     "Listener: Action must be local");
    }
    synchronized (this) {
      if (ListenerVector == null) ListenerVector = new Vector();
    }
    synchronized(ListenerVector) {
      if (findDataChangedListener(a) != null) {
        throw new ReferenceException("DataReferenceImpl.addDataChangedListener:" +
                                     " link to Action already exists");
      }
      ListenerVector.addElement(new DataChangedListener(a));
    }
  }

  /** method for use by RemoteDataReferenceImpl that adapts this
      DataReferenceImpl */
  void adaptedAddDataChangedListener(RemoteAction a)
       throws VisADException {
    synchronized (this) {
      if (ListenerVector == null) ListenerVector = new Vector();
    }
    synchronized(ListenerVector) {
      if (findDataChangedListener(a) != null) {
        throw new ReferenceException("DataReferenceImpl.addDataChangedListener:" +
                                     " link to Action already exists");
      }
      ListenerVector.addElement(new DataChangedListener(a));
    }
  }

  /** DataChangedListener must be local ActionImpl */
  public void removeDataChangedListener(Action a)
         throws VisADException {
    if (!(a instanceof ActionImpl)) {
      throw new RemoteVisADException("DataReferenceImpl.removeDataChanged" +
                                     "Listener: Action must be local");
    }
    if (ListenerVector != null) {
      synchronized(ListenerVector) {
        DataChangedListener listener = findDataChangedListener(a);
        if (listener != null) {
          ListenerVector.removeElement(listener);
        }
      }
    }
  }

  /** method for use by RemoteDataReferenceImpl that adapts this
      DataReferenceImpl */
  void adaptedRemoveDataChangedListener(RemoteAction a)
       throws VisADException {
    if (ListenerVector != null) {
      synchronized(ListenerVector) {
        DataChangedListener listener = findDataChangedListener(a);
        if (listener != null) {
          ListenerVector.removeElement(listener);
        }
      }
    }
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof DataReference)) return false;
    return obj == this;
  }

  public String toString() {
    return "DataReference " + Name;
  }

}

