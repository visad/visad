
//
// Integer1DSet.java
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
   Integer1DSet represents a finite set of samples of R at
   an integer lattice based at the origin (i.e, 0, 1, 2, ..., length-1).<P>
*/
public class Integer1DSet extends Linear1DSet {

  public Integer1DSet(MathType type, int length) throws VisADException {
    this(type, length, null, null, null);
  }

  public Integer1DSet(MathType type, int length, CoordinateSystem coord_sys,
                      Unit[] units, ErrorEstimate[] errors) throws VisADException {
    super(type, 0.0, (double) (length - 1), length, coord_sys, units, errors);
  }

  public boolean isIntegerSet() {
    return true;
  }

  public Object clone() {
    try {
      return new Integer1DSet(Type, Length, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Integer1DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Integer1DSet(type, Length, DomainCoordinateSystem,
                            SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    return pre + "Integer1DSet: Length = " + Length + "\n";
  }

}

