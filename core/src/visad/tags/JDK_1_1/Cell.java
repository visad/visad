
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
   Cell is the VisAD interface for "spread sheet" cells.  It has
   a set of input DataReferences and an output DataReference, which
   updates whenever an input changes.  Cell is runnable.<P>
*/
public interface Cell extends Action {

  /** create link to an output DataReference */
  public abstract void setOtherReference(int index, DataReference ref)
         throws VisADException, RemoteException;
 
  /** get link to an output DataReference */
  public abstract DataReference getOtherReference(int index)
         throws VisADException, RemoteException;

}

