
//
// DataReference.java
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

import java.rmi.*;

/**
   DataReference is the VisAD interface for named holders for data
   objects.  It can provide a symbol table binding between a name and
   a variable for a user interface that includes a formula interpreter,
   or a full language interpreter (e.g., a Java interpreter).<P>

   During computations the Data object referenced by a DataReference
   may change.  DataReference objects are passed to Display objects, so
   that a display may depict the changing values of named variables.<P>

   DataReference is a source of DataChangedOccurrence-s, and thus defines
   addDataChangedListener and removeDataChangedListener.<P>

   DataReference objects may be local (DataReferenceImpl) or
   remote (RemoteDataReferenceImpl).<P>
*/
public interface DataReference {

  /** invokes d.addReference((DataReference r) */
  public abstract void setData(Data d) throws VisADException, RemoteException;

  public abstract Data getData() throws VisADException, RemoteException;

  public abstract long getTick() throws VisADException, RemoteException;

  public abstract long incTick() throws VisADException, RemoteException;

  public abstract String getName() throws VisADException, RemoteException;

  public abstract void addDataChangedListener(Action a)
         throws VisADException, RemoteException;

  public abstract void removeDataChangedListener(Action a)
         throws VisADException, RemoteException;

  public DataChangedOccurrence acknowledgeDataChanged(Action a)
         throws VisADException, RemoteException;
}

