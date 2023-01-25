//
// Cell.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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
   Cell is the VisAD interface for computations.  It has a set of
   'triggering' DataReferences and access to a set of non-triggering
   DataReferences. <P>
*/
public interface Cell extends Action {

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
  void setOtherReference(int index, DataReference ref)
         throws VisADException, RemoteException;

  /**
   * @return the non-triggering link to a DataReference
   * identified by index
   * @param index - identifier of DataReference to return
   * @throws VisADException - a VisAD error occurred
   * @throws RemoteException - an RMI error occurred
   */
  DataReference getOtherReference(int index)
         throws VisADException, RemoteException;

}

