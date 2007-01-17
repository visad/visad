//
// List1DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
 * List1DSet is the class for Set-s containing lists of
 * 1-D values with no topology.  This set does not support interpolation.<P>
 */
public class List1DSet extends SimpleSet {

  float[] data;

  /**
   * Constructs with a non-default CoordinateSystem.
   *
   * @param d			The data samples.  Must not be 
   *				<code>null</code>.
   * @param type		The type of the set.
   * @param coord_sys		The coordinate system transformation.  May be
   *				<code>null</code>.
   * @param units		The units of the values.  May be 
   *				<code>null</code>.
   * @throws VisADException	Couldn't create set.
   */
  public List1DSet(float[] d, MathType type, CoordinateSystem coord_sys,
            Unit[] units) throws VisADException {
    super(type, coord_sys, units, null);
    if (DomainDimension != 1) {
      throw new SetException("List1DSet: type must be 1-D");
    }
    data = d;
    Length = d.length;
  }

  public boolean isMissing() {
    return (data == null);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    float[][] value = new float[1][length];
    for (int i=0; i<length; i++) {
      if (index[i] < 0 || index[i] >= Length) {
        for (int k=0; k<DomainDimension; k++) {
          value[k][i] = Float.NaN;
        }
      }
      else {
        value[0][i] = data[index[i]];
      }
    }
    return value;
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    throw new UnimplementedException("List1DSet.valueToIndex");
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
                            float weights[][]) throws VisADException {
    throw new UnimplementedException("List1DSet.valueToInterpx");
  }

  public boolean equals(Object set) {
    if (!(set instanceof List1DSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    if (Length != ((Set) set).Length) return false;
    for (int i=0; i<Length; i++) {
      /*
       * The use of Float.floatToIntBits(...) in the following accomodates 
       * Float.NaN values and matches the equals(...) method.
       */
      if (Float.floatToIntBits(data[i]) !=
          Float.floatToIntBits(((List1DSet) set).data[i]))
        return false;
    }
    return true;
  }

  /**
   * Returns the hash code of this instance.
   * @param		The hash code of this instance.
   */
  public int hashCode() {
    if (!hashCodeSet)
    {
      hashCode = unitAndCSHashCode() ^ Length;
      for (int i=0; i<Length; i++)
	hashCode ^= Float.floatToIntBits(data[i]);
      hashCodeSet = true;
    }
    return hashCode;
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new List1DSet(data, type, DomainCoordinateSystem, SetUnits);
  }

  public String longString(String pre) throws VisADException {
    return pre + "List1DSet";
  }
}

