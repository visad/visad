//
// RemoteCellImpl.java
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
   RemoteCellImpl is the VisAD class for remote access to
   Cell-s.<P>
*/
public class RemoteCellImpl extends RemoteActionImpl
       implements RemoteCell {
  // and RemoteActionImpl extends UnicastRemoteObject

  public RemoteCellImpl(CellImpl d) throws RemoteException {
    super(d);
  }

  /**
   * set a non-triggering link to a DataReference; this is
   * used to give the Cell access to Data without triggering
   * the Cell's doAction whenever the Data changes;
   * these 'other' DataReferences are identified by their
   * integer index
   * @param index - identifier of DataReference
   * @param ref - DataReference to be linked
   * @throws VisADException - a VisAD error occurred
   * @throws RemoteException - an RMI error occurred
   */
  public void setOtherReference(int index, DataReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof RemoteDataReference)) {
      throw new RemoteVisADException("RemoteCellImpl.setOutputReference: " +
                            "requires RemoteDataReferenceImpl");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteCellImpl.setOutputReference: " +
                                     "AdaptedAction is null");
    }
    ((CellImpl) AdaptedAction).
      adaptedSetOtherReference(index, (RemoteDataReference) ref);
  }

  /**
   * @return the non-triggering link to a DataReference
   * identified by index
   * @param index - identifier of DataReference to return
   * @throws VisADException - a VisAD error occurred
   * @throws RemoteException - an RMI error occurred
   */
  public DataReference getOtherReference(int index)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteCellImpl.getOutputReference: " +
                                     "AdaptedAction is null");
    }
    return ((CellImpl) AdaptedAction).getOtherReference(index);
  }

}

