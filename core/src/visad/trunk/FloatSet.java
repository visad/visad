
//
// FloatSet.java
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
   FloatSet represents the finite (but large) set of samples of
   R^dimension made by vectors of IEEE single precision floating
   point numbers.  FloatSet objects are immutable.<P>

   FloatSet cannot be used for the domain sampling of a Field.<P>
*/
public class FloatSet extends SimpleSet {

  /** construct a FloatSet object */
  public FloatSet(MathType type) throws VisADException {
    this(type, null, null);
  }

  /** construct a FloatSet object with a non-default CoordinateSystem */
  public FloatSet(MathType type, CoordinateSystem coord_sys, Unit[] units)
         throws VisADException {
    super(type, coord_sys, units, null); // no ErrorEstimate for FloatSet
  }

  public float[][] indexToValue(int[] index) throws VisADException {
    throw new SetException("FloatSet.indexToValue");
  }

  public int[] valueToIndex(float[][] value) throws VisADException {
    throw new SetException("FloatSet.valueToIndex");
  }

  public void valueToInterp(float[][] value, int[][] indices, float weights[][])
              throws VisADException {
    throw new SetException("FloatSet.valueToInterp");
  }

  public int getLength() throws VisADException {
    throw new SetException("FloatSet.getLength");
  }

  public boolean equals(Object set) {
    if (!(set instanceof FloatSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (DomainDimension == ((FloatSet) set).getDimension());
  }

  public boolean isMissing() {
    return false;
  }

  public Object clone() {
    try {
      return new FloatSet(Type, DomainCoordinateSystem, SetUnits);
    }
    catch (VisADException e) {
      throw new VisADError("FloatSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new FloatSet(type, DomainCoordinateSystem, SetUnits);
  }

  public String longString(String pre) throws VisADException {
    return pre + "FloatSet: Dimension = " + DomainDimension + "\n";
  }

}

