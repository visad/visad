//
// List1DDoubleSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
 * List1DDoubleSet is the class for Set-s containing lists of
 * 1-D double values with no topology.  This set does not support 
 * interpolation.<P>
 */
public class List1DDoubleSet extends SimpleSet {

  double[] data;

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
  public List1DDoubleSet(double[] d, MathType type, CoordinateSystem coord_sys,
            Unit[] units) throws VisADException {
    super(type, coord_sys, units, null);
    if (DomainDimension != 1) {
      throw new SetException("List1DDoubleSet: type must be 1-D");
    }
    data = d;
    Length = d.length;
  }

  public boolean isMissing() {
    return (data == null);
  }

  /**
   * Converts an array of 1-D indices to an array of values in R^1.  Note that a
   * returned values may be Double.NaN due to double-to-float conversion.
   * @param indices		The indices of the values to be returned.
   * @return			The values associated with the given indices.
   *				A Float.NaN will be returned for an out-of-range
   *				index or a double value that cannot be converted
   *				to a float.
   */
  public float[][] indexToValue(int[] indices) throws VisADException {
    int length = indices.length;
    float[][] value = new float[1][length];
    for (int i=0; i<length; i++) {
      if (indices[i] < 0 || indices[i] >= Length) {
        for (int k=0; k<DomainDimension; k++) {
          value[k][i] = Float.NaN;
        }
      }
      else {
        value[0][i] = (float)data[indices[i]];
      }
    }
    return value;
  }

  /**
   * Converts an array of 1-D indices to an array of values in R^1.
   * @param indices		The indices of the values to be returned.
   * @return			The values associated with the given indices.
   *				A Double.NaN will be returned for an out-of-
   *				range index.
   */
  public double[][] indexToDouble(int[] indices) throws VisADException {
    int length = indices.length;
    double[][] value = new double[1][length];
    for (int i=0; i<length; i++) {
      if (indices[i] < 0 || indices[i] >= Length) {
        for (int k=0; k<DomainDimension; k++) {
          value[k][i] = Double.NaN;
        }
      }
      else {
        value[0][i] = data[indices[i]];
      }
    }
    return value;
  }

  /**
   * Converts an array of values in R^1 to an array of 1-D indices.  This
   * method is unimplemented because this class doesn't support interpolation.
   * @param values		The values to have their indicies returned.
   * @throws UnimplementedException
   *				Always.
   */
  public int[] valueToIndex(float[][] values) throws VisADException {
    throw new UnimplementedException("List1DDoubleSet.valueToIndex");
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
                            float weights[][]) throws VisADException {
    throw new UnimplementedException("List1DDoubleSet.valueToInterpx");
  }

  public boolean equals(Object set) {
    if (!(set instanceof List1DDoubleSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    if (Length != ((Set) set).Length) return false;
    for (int i=0; i<Length; i++) {
      /*
       * Use of Double.doubleToLongBits(...) in the following accomodates
       * Double.NaN values and matches the use of Double.hashCode() in the
       * hashCode() method.
       */
      if (Double.doubleToLongBits(data[i]) != 
            Double.doubleToLongBits(((List1DDoubleSet) set).data[i]))
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
	hashCode ^= new Double(data[i]).hashCode();
      hashCodeSet = true;
    }
    return hashCode;
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new List1DDoubleSet(data, type, DomainCoordinateSystem, SetUnits);
  }

  /**
   * Returns a pretty string about this instance.
   * @return		A pretty string about this instance.
   */
  public String longString(String pre)
  {
    return pre + getClass().getName() + ": Dimension = 1" +
	" Length = " + data.length + "\n";
  }
}

