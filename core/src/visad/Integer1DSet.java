//
// Integer1DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

/**
   Integer1DSet represents a finite set of samples of R at
   an integer lattice based at the origin (i.e, 0, 1, 2, ..., length-1).<P>
*/
public class Integer1DSet extends Linear1DSet
       implements IntegerSet {

  /** a 1-D set with null errors and generic type */
  public Integer1DSet(int length) throws VisADException {
    this(RealType.Generic, length, null, null, null);
  }

  public Integer1DSet(MathType type, int length) throws VisADException {
    this(type, length, null, null, null);
  }

  /** construct a 1-dimensional set with values {0, 1, ..., length-1};
      coordinate_system and units must be compatible with defaults for
      type, or may be null; errors may be null */
  public Integer1DSet(MathType type, int length, CoordinateSystem coord_sys,
                      Unit[] units, ErrorEstimate[] errors) throws VisADException {
    super(type, 0.0, (double) (length - 1), length, coord_sys, units, errors);
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Integer1DSet(type, Length, DomainCoordinateSystem,
                            SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    return pre + "Integer1DSet: Length = " + Length + "\n";
  }

}

