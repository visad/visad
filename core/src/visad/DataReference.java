//
// DataReference.java
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
   DataReference is the VisAD interface for named holders for data
   objects.  It can provide a symbol table binding between a name and
   a variable for a user interface that includes a formula interpreter,
   or a full language interpreter (e.g., a Java interpreter).<P>

   During computations the Data object referenced by a DataReference
   may change.  DataReference objects are passed to Display objects, so
   that a display may depict the changing values of named variables.<P>

   DataReference is a source of ThingChangedEvent-s, and thus defines
   addThingChangedListener and removeThingChangedListener.<P>

   DataReference objects may be local (DataReferenceImpl) or
   remote (RemoteDataReferenceImpl).<P>
*/
public interface DataReference extends ThingReference {

  /**
   * set reference to data, replacing any currently referenced
   * Data object; if this is local (i.e., an instance of
   * DataReferenceImpl) then the Data argument must also be
   * local (i.e., an instance of DataImpl);
   * if this is Remote (i.e., an instance of RemoteDataReference)
   * then a local Data argument (i.e., an instance of DataImpl)
   * will be passed by copy and a remote Data argument (i.e., an
   * instance of RemoteData) will be passed by remote reference;
   * invokes d.addReference(DataReference r)
   * @param d Data object to be set
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void setData(Data d) throws VisADException, RemoteException;

  /**
   * @return referenced Data object, or null if none
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  Data getData() throws VisADException, RemoteException;

  /**
   * this is more efficient than getData().getType() for
   * RemoteDataReferences
   * @return the MathType of referenced Data object, or null if none;
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  MathType getType() throws VisADException, RemoteException;

}

