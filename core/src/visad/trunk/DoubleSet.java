
//
// DoubleSet.java
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
   DoubleSet represents the finite (but large) set of samples of
   R^dimension made by vectors of IEEE double precision floating
   point numbers.  DoubleSet objects are immutable.<P>

   DoubleSet cannot be used for the domain sampling of a Field.<P>
*/
public class DoubleSet extends SimpleSet {

  /** construct a DoubleSet object */
  public DoubleSet(MathType type) throws VisADException {
    this(type, null, null);
  }

  /** construct a DoubleSet object with a non-default CoordinateSystem */
  public DoubleSet(MathType type, CoordinateSystem coord_sys, Unit[] units)
         throws VisADException {
    super(type, coord_sys, units, null); // no ErrorEstimate for DoubleSet
  }

  public double[][] indexToValue(int[] index) throws VisADException {
    throw new SetException("DoubleSet.indexToValue");
  }

  public int[] valueToIndex(double[][] value) throws VisADException {
    throw new SetException("DoubleSet.valueToIndex");
  }

  public void valueToInterp(double[][] value, int[][] indices, double weights[][])
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

