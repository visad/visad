
//
// ReferenceDataPair.java
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

/**
   ReferenceDataPair is the VisAD class for a
   (RemoteDataReference, RemoteData) pair
   on the references Vector of DataImpl
*/
class ReferenceDataPair extends Object {

  /** an alternate policy option would be to propogate Data
      changes to ref; but currently not used */
  RemoteDataReference ref;

  /** Data changes noted by data.incTick();
      later polled by data.getTick() from a RemoteAction */
  RemoteData data;
 
  ReferenceDataPair(RemoteDataReference r, RemoteData d) {
    ref = r;
    data = d;
  }
 
  public boolean equals(Object pair) {
    if (!(pair instanceof ReferenceDataPair)) return false;
    return (ref.equals(((ReferenceDataPair) pair).ref) &&
            data.equals(((ReferenceDataPair) pair).data));
  }
 
}

