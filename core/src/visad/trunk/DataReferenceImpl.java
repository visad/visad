
//
// DataReferenceImpl.java
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

import java.util.*;
import java.rmi.*;

/**
   DataReferenceImpl is a local implementation of DataReference.<P>

   DataReferenceImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public class DataReferenceImpl extends ThingReferenceImpl
       implements DataReference {

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

  /** set this DataReferenceImpl to refer to d;
      must be local DataImpl */
  public void setData(Data d)
         throws VisADException, RemoteException {
    setThing(d);
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

