//
// CMYCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
   CMYCoordinateSystem is the VisAD CoordinateSystem class for
   (Cyan, Magenta, Yellow) with Reference (Red, Green, Blue).
   Algorithm from Foley and van Dam.<P>
*/
public class CMYCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units = {null, null, null};

  /**
   * construct a CMYCoordinateSystem with given reference
   * @param reference - reference RealTupleType
   */
  public CMYCoordinateSystem(RealTupleType reference) throws VisADException {
    super(reference, coordinate_system_units);
  }

  /**
   * trusted constructor for initializers (does not throw
   * any declared Exceptions)
   * @param reference - reference RealTupleType
   * @param b - dummy argument for trusted constructor signature
   */
  CMYCoordinateSystem(RealTupleType reference, boolean b) {
    super(reference, coordinate_system_units, b);
  }

  /**
   *  Convert RealTuple values to Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  double[][] arrays rather than RealTuple[] arrays; the array
   *  organization is double[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.
   *  @param  value  array of values assumed to be in coordinateSystem
   *                 units. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of double values in reference coordinates and Unit-s.
   *  @throws VisADException  if problem with conversion.
   */
  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0 - tuples[0][i];
      value[1][i] = 1.0 - tuples[1][i];
      value[2][i] = 1.0 - tuples[2][i];
    }
    return value;
  }

  /**
   *  Convert RealTuple values from Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  double[][] arrays rather than RealTuple[] arrays; the array
   *  organization is double[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.
   *  @param  value  array of values assumed to be in reference
   *                 Unit-s. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of double values in CoordinateSystem Unit-s.
   *  @throws VisADException  if problem with conversion.
   */
  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0 - tuples[0][i];
      value[1][i] = 1.0 - tuples[1][i];
      value[2][i] = 1.0 - tuples[2][i];
    }
    return value;
  }

  /**
   *  Convert RealTuple values to Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  float[][] arrays rather than RealTuple[] arrays; the array
   *  organization is float[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.
   *  @param  value  array of values assumed to be in coordinateSystem
   *                 units. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of float values in reference coordinates and Unit-s.
   *  @throws VisADException  if problem with conversion.
   */
  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0f - tuples[0][i];
      value[1][i] = 1.0f - tuples[1][i];
      value[2][i] = 1.0f - tuples[2][i];
    }
    return value;
  }

  /**
   *  Convert RealTuple values from Reference coordinates;
   *  for efficiency, input and output values are passed as
   *  float[][] arrays rather than RealTuple[] arrays; the array
   *  organization is float[tuple_dimension][number_of_tuples];
   *  can modify and return argument array.
   *  @param  value  array of values assumed to be in reference
   *                 Unit-s. Input array is not guaranteed to be immutable
   *                 and could be used for return.
   *  @return array of float values in CoordinateSystem Unit-s.
   *  @throws VisADException  if problem with conversion.
   */
  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("CMYCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      value[0][i] = 1.0f - tuples[0][i];
      value[1][i] = 1.0f - tuples[1][i];
      value[2][i] = 1.0f - tuples[2][i];
    }
    return value;
  }

  /**
   * Indicates whether or not this instance is equal to an object.
   * @param cs - the object in question.
   * @return <code>true</code> if and only if this instance equals cs.
   */
  public boolean equals(Object cs) {
    return (cs instanceof CMYCoordinateSystem);
  }

}

