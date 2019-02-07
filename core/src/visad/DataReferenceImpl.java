//
// DataReferenceImpl.java
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

/**
   DataReferenceImpl is a local implementation of DataReference.<P>

   DataReferenceImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DataReferenceImpl extends ThingReferenceImpl
       implements DataReference {
  /**
   * Constructs from a name for the instance.
   *
   * @param name                   The name for this instance.
   * @throws VisADException        if the name is <code>null</code>.
   */
  public DataReferenceImpl(String name) throws VisADException {
    super(name);
  }

  /** 
   * @return referenced Data object, or null if none 
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public Data getData() {
    return (Data) getThing();
  }

  /** 
   * this is more efficient than getData().getType() for 
   * RemoteDataReferences
   * @return the MathType of referenced Data object, or null if none;
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public MathType getType()
         throws VisADException, RemoteException {
    Data data = getData();
    return (data == null) ? null : data.getType();
  }

  /**
   * Sets the Data object to which this instance refers.
   *
   * @param d                     The data object.
   * @throws ReferenceException   if the data object is <code>null</code>.
   * @throws RemoteVisADException if the data object is a {@link RemoteData}.
   */
  public void setData(Data d)
         throws VisADException, RemoteException {
    setThing(d);
  }

  /**
   * method for use by setData() method of the RemoteDataReferenceImpl
   * that adapts this DataReferenceImpl
   * @param d RemoteData being set
   * @param r RemoteDataReference adapting this DataReferenceImpl
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void adaptedSetData(RemoteData d, RemoteDataReference r)
               throws VisADException, RemoteException {
    adaptedSetThing(d, r);
  }

  /**
   * Indicates whether or not this instance is equal to an object
   * @param obj the object in question.
   * @return <code>true</code> if and only if this instance equals o.
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof DataReference)) return false;
    return obj == this;
  }

  /**
   * @return String representation of this
   */
  public String toString() {
    return "DataReference " + Name;
  }

}

