
//
// Cell.java
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
   Cell is the VisAD interface for computations.  It has a set of
   'triggering' DataReferences and access to a set of non-triggering
   DataReferences. <P>
*/
public interface Cell extends Action {

  /** set a non-triggering link to a DataReference; this is
      used to give the Cell access to Data without triggering
      the Cell's doAction whenever the Data changes;
      these 'other' DataReferences are identified by their
      integer index */
  public abstract void setOtherReference(int index, DataReference ref)
         throws VisADException, RemoteException;
 
  /** return the non-triggering link to a DataReference
      identified by index */
  public abstract DataReference getOtherReference(int index)
         throws VisADException, RemoteException;

}

