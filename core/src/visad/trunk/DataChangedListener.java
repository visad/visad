
//
// DataChangedListener.java
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
   DataChangedListener objects are used by DataReference objects to
   define their connections with Action objects.<P>
*/
class DataChangedListener extends Object {

  Action action;  // may be remote or local
  boolean Ball; // true when Action is waiting for a DataChangedOccurrence
                // false when this is waiting for an acknowledgement
  DataChangedOccurrence event; // non-null only when Ball = false;

  DataChangedListener(Action a)
                      throws VisADException {
    if (a == null) {
      throw new ReferenceException("DataChangedListener: Action cannot be null");
    }
    action = a;
    Ball = false;
  }

  Action getAction() {
    return action;
  }

  boolean getBall() {
    return Ball;
  }

  void setBall(boolean b) {
    Ball = b;
  }

  DataChangedOccurrence getDataChangedOccurrence() {
    return event;
  }

  void setDataChangedOccurrence(DataChangedOccurrence e) {
    event = e;
  }

}

