//
// CellImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
   CellImpl is the abstract superclass for computations.  It has a
   set of input DataReferences and an output DataReference, which
   updates whenever an input changes.  Cell is runnable.<P>

   CellImpl is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class CellImpl extends ActionImpl implements Cell {

  /** references to Data use in computation, other than
      those in ReferenceActionLink-s */
  DataReference[] otherReferences = null;

  public CellImpl() {
    this(null);
  }

  /** the name String can be useful for debugging */
  public CellImpl(String name) {
    super(name);
    otherReferences = null;
  }

  /** subclasses of CellImpl implement doAction to compute a new Data
      value for outputReference as a function of the Data objects
      referenced by the DataReferences in LinkVector */
  public abstract void doAction() throws VisADException, RemoteException;

  /** create link to an output DataReference */
  public void setOtherReference(int index, DataReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof DataReferenceImpl)) {
      throw new RemoteVisADException("CellImpl.addReference: requires " +
                                     "DataReferenceImpl");
    }
    setOtherReferences(index, ref);
  }

  void adaptedSetOtherReference(int index, RemoteDataReference ref) {
    setOtherReferences(index, ref);
  }

  /** get link to an output DataReference */
  public synchronized DataReference getOtherReference(int index)
         throws VisADException, RemoteException {
    if (otherReferences == null ||
        index < 0 || index >= otherReferences.length) {
      return null;
    }
    else {
      return otherReferences[index];
    }
  }

  private synchronized void setOtherReferences(int index, DataReference ref) {
    if (otherReferences == null) {
      otherReferences = new DataReference[index];
      for (int i=0; i<index; i++) {
        otherReferences[i] = null;
      }
    }
    else if (index >= otherReferences.length) {
      DataReference[] os = new DataReference[index];
      for (int i=0; i<otherReferences.length; i++) {
        os[i] = otherReferences[i];
      }
      for (int i=otherReferences.length; i<index; i++) {
        os[i] = null;
      }
      otherReferences = os;
    }
    otherReferences[index] = ref;
  }

}

