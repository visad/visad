//
// DoubleSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
   DoubleSet represents the finite (but large) set of samples of
   R^dimension made by vectors of IEEE double precision floating
   point numbers.  DoubleSet objects are immutable.<P>

   DoubleSet cannot be used for the domain sampling of a Field.<P>
*/
public class DoubleSet extends SimpleSet {

  /** construct a DoubleSet object with null CoordinateSystem and Units */
  public DoubleSet(MathType type) throws VisADException {
    this(type, null, null);
  }

  /** the set of values representable by N doubles;
      type must be a RealType, a RealTupleType or a SetType;
      coordinate_system and units must be compatible with defaults
      for type, or may be null;
      a DoubleSet may not be used as a Field domain */
  public DoubleSet(MathType type, CoordinateSystem coord_sys, Unit[] units)
         throws VisADException {
    super(type, coord_sys, units, null); // no ErrorEstimate for DoubleSet
  }

  public float[][] indexToValue(int[] index) throws VisADException {
    throw new SetException("DoubleSet.indexToValue");
  }

  public int[] valueToIndex(float[][] value) throws VisADException {
    throw new SetException("DoubleSet.valueToIndex");
  }

  public void valueToInterp(float[][] value, int[][] indices, float weights[][])
              throws VisADException {
    throw new SetException("DoubleSet.valueToInterp");
  }

  public int getLength() throws VisADException {
    throw new SetException("DoubleSet.getLength");
  }

  public boolean equals(Object set) {
    if (!(set instanceof DoubleSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (DomainDimension == ((DoubleSet) set).getDimension());
  }

  public boolean isMissing() {
    return false;
  }

  public Object clone() {
    try {
      return new DoubleSet(Type, DomainCoordinateSystem, SetUnits);
    }
    catch (VisADException e) {
      throw new VisADError("DoubleSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new DoubleSet(type, DomainCoordinateSystem, SetUnits);
  }

  public String longString(String pre) throws VisADException {
    return pre + "DoubleSet: Dimension = " + DomainDimension + "\n";
  }

}

