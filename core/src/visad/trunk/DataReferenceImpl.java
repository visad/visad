//
// DataReferenceImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
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

  public Data getData() {
    return (Data) getThing();
  }

  public MathType getType()
         throws VisADException, RemoteException {
    Data data = getData();
    return (data == null) ? null : data.getType();
  }

  /**
   * Sets the data object to which this instance refers.
   *
   * @param d                     The data object.
   * @throws ReferenceException   if the data object is <code>null</code>.
   * @throws RemoteVisADException if the data object is a {@link RemoteData}.
   */
  public void setData(Data d)
         throws VisADException, RemoteException {
    setThing(d);
// DisplayImpl.printStack(getName());
  }

  /** method for use by RemoteDataReferenceImpl that adapts this
      DataReferenceImpl */
  void adaptedSetData(RemoteData d, RemoteDataReference r)
               throws VisADException, RemoteException {
    adaptedSetThing(d, r);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof DataReference)) return false;
    return obj == this;
  }

  public String toString() {
    return "DataReference " + Name;
  }

}

