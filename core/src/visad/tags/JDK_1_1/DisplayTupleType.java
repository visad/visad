
//
// DisplayTupleType.java
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

/**
   DisplayTupleType is the class for tuples of DisplayRealType's.<P>
*/

public class DisplayTupleType extends RealTupleType {

  public DisplayTupleType(DisplayRealType[] types) throws VisADException {
    this(types, null);
  }

  public DisplayTupleType(DisplayRealType[] types, CoordinateSystem coord_sys)
         throws VisADException {
    super(types, coord_sys, null);
    if (coord_sys != null) {
      RealTupleType ref = coord_sys.getReference();
      if (!(ref instanceof DisplayTupleType)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
                    "CoordinateSystem.Reference must be a DisplayTupleType");
      }
      else if (Display.DisplaySpatialOffsetTuple.equals(ref)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
               "CoordinateSystem.Reference cannot be DisplaySpatialOffsetTuple");
      }
      else if (Display.DisplayFlow1Tuple.equals(ref)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
               "CoordinateSystem.Reference cannot be DisplaySpatialOffsetTuple");
      }
      else if (Display.DisplayFlow2Tuple.equals(ref)) {
        throw new CoordinateSystemException("DisplayTupleType: " +
               "CoordinateSystem.Reference cannot be DisplaySpatialOffsetTuple");
      }
    }
    for (int i=0; i<types.length; i++) {
      if (types[i].getTuple() != null) {
        throw new DisplayException("DisplayTupleType: DisplayRealType already " +
                                   "part of a DisplayTupleType");
      }
      types[i].setTuple(this, i);
    }
  }

  /** trusted constructor for initializers */
  DisplayTupleType(DisplayRealType[] types, boolean b) {
    this(types, null, b);
  }

  /** trusted constructor for initializers */
  DisplayTupleType(DisplayRealType[] types, CoordinateSystem coord_sys,
                   boolean b) {
    super(types, coord_sys, b);
    for (int i=0; i<types.length; i++) {
      types[i].setTuple(this, i);
    }
  }

}

