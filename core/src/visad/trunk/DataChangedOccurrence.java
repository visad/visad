 
//
// DataChangedOccurrence.java
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
   DataChangedOccurrence is the VisAD class for changes in Data objects
   referred to by DataReference objects.  They are sourced by
   DataReference objects and received by Action objects.<P>
*/
public class DataChangedOccurrence extends Object
       implements java.io.Serializable {

  private DataReference Source;
  private long Tick;

  public DataChangedOccurrence(DataReference source, long tick)
         throws VisADException, RemoteException {
    if (!(source instanceof DataReference)) {
      throw new ReferenceException("DataChangedOccurrence: source must be " +
                                   "DataReference");
    }
    Source = source;
    Tick = tick;
  }

  public DataReference getDataReference() {
    return Source;
  }

  public long getTick() {
    return Tick;
  }

}

