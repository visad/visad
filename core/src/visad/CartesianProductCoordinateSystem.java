//
// $Id: CartesianProductCoordinateSystem.java,v 1.13 2009-03-02 23:35:41 curtis Exp $
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


import java.util.Vector;


/**
 * <P>A class for creating a new CoordinateSystem that is the product of
 * two or more CoordinateSystems.  This is useful
 * for creating a new CoordinateSystem when combining values from different
 * Data objects with different CoordinateSystems into a new Data object with a
 * new CoordinateSystem that combines the CoordinateSystems from the original
 * Data objects.  An example would be where one CoordinateSystem
 * transforms (row,col) -> (lat,lon) and another CoordinateSystem transforms
 * (pressure) -> (altitude).  The resulting CartesianProductCoordinateSystem
 * would transform (row, col, pressure) -> (lat, lon, alt).</P>
 *
 * <P>The resulting CartesianProductCoordinateSystem will have a dimension
 * of the sum of the dimensions of the individual CoordinateSystems and a
 * Reference RealTupleType that is the composite of the references of
 * each CoordinateSystem.  The CoordinateSystem Units are a composite of the
 * Units of each of the CoordinateSystems as well.</P>
 */
public class CartesianProductCoordinateSystem extends CoordinateSystem {

  /** array of coordinate systems */
  private CoordinateSystem[] csArray;

  /**
   * Construct a CartesianProductCoordinateSystem from two other
   * CoordinateSystems.
   *
   * @param   a   first non-null CoordinateSystem
   * @param   b   second non-null CoordinateSystem
   * @throws  VisADException  a or b are null or VisAD object can't be created
   */
  public CartesianProductCoordinateSystem(CoordinateSystem a,
                                          CoordinateSystem b)
          throws VisADException {
    this(new CoordinateSystem[] {a, b});
  }

  /**
   * Construct a CartesianProductCoordinateSystem from an array of
   * CoordinateSystems.
   *
   * @param  csArray  non-null array of non-null CoordinateSystems
   * @throws VisADException  an element is null or VisAD object can't be
   *                         created.
   */
  public CartesianProductCoordinateSystem(CoordinateSystem[] csArray)
          throws VisADException {
    super(getProductReference(csArray), getProductUnits(csArray));
    this.csArray = csArray;
  }

  /**
   * Get the arrays of CoordinateSystems being used in this product
   * @return array of CoordinateSystems
   */
  public CoordinateSystem[] getCoordinateSystems() {
    return csArray;
  }

  /**
   * Get a particular CoordinateSystem
   * @param  index  index into the array
   * @return CoordinateSystem from array
   * @throws ArrayIndexOutOfBoundsException (no need to declare)
   *         if index out of bounds
   */
  public CoordinateSystem getCoordinateSystem(int index) {
    return csArray[index];
  }

  /**
   * create a RealTupleType that concatenates the references of
   * the CoordinateSystems in csArray (used by constructor)
   * @param csArray - array of CoordinateSystems
   * @return concatenated reference RealTupleType
   * @throws VisADException
   */
  static RealTupleType getProductReference(CoordinateSystem[] csArray)
          throws VisADException {
    if (csArray == null)
      throw new VisADException("CoordinateSystem array can't be null");
    if (csArray.length < 2)
      throw new VisADException(
        "CoordinateSystem array must have more than one element");
    Vector typeVector = new Vector();
    for (int i = 0; i < csArray.length; i++) {
      CoordinateSystem cs = csArray[i];
      if (cs == null)
        throw new VisADException(
          "CoordinateSystem array can't have null members "+i);
      RealType[] reals = cs.getReference().getRealComponents();
      for (int j = 0; j < reals.length; j++) {
        typeVector.add(reals[j]);
      }
    }

    return new RealTupleType(
      (RealType[]) typeVector.toArray(new RealType[typeVector.size()]));
  }

  /**
   * create an array of Units that concatenates the Unit arrays
   * of the CoordinateSystems in csArray (used by constructor)
   * @param csArray - array of CoordinateSystems
   * @return concatenated array of Units
   * @throws VisADException
   */
  static Unit[] getProductUnits(CoordinateSystem[] csArray)
          throws VisADException {
    if (csArray == null)
      throw new VisADException("CoordinateSystem array can't be null");
    if (csArray.length < 2)
      throw new VisADException(
        "CoordinateSystem array must have more than one element");
    Vector unitVector = new Vector();
    for (int i = 0; i < csArray.length; i++) {
      Unit[] units = csArray[i].getCoordinateSystemUnits();
      for (int j = 0; j < units.length; j++) {
        unitVector.add(units[j]);
      }
    }

    return (Unit[]) unitVector.toArray(new Unit[unitVector.size()]);
  }

  /**
   * Convert input array to reference coordinates.
   *
   * @param input   input array
   * @return  array of values in reference space
   * @throws VisADException  input array has the wrong dimension or is null
   */
  public double[][] toReference(double[][] input) throws VisADException {
    if (input.length != getDimension() || input == null)
      throw new VisADException("input has wrong dimension");
    int numElements = input[0].length;
    double[][] output = new double[getDimension()][];
    int pointer = 0;
    for (int i = 0; i < csArray.length; i++) {
      int dimension = csArray[i].getDimension();
      double[][] temp = new double[dimension][];
      // assign input values
      for (int j = 0; j < dimension; j++) {
        temp[j] = (double[]) input[pointer+j];
      }

      // do the transformation
      temp = csArray[i].toReference(temp);

      // assign values to the output array
      for (int j = 0; j < dimension; j++) {
        output[pointer+j] = temp[j];
      }

      pointer += dimension;
    }

    return output;
  }

  /**
   * Convert array of reference valeus from Reference coordinates.  Input
   * values are modified in this transaction.
   *
   * @param refTuple   reference tuple array
   * @return  array of values in non-reference space
   * @throws VisADException  input array has the wrong dimension or is null
   */
  public double[][] fromReference(double[][] refTuple) throws VisADException {
    if (refTuple.length != getDimension() || refTuple == null)
      throw new VisADException("refTuple has wrong dimension");
    int numElements = refTuple[0].length;
    double[][] output = new double[getDimension()][];
    int pointer = 0;
    for (int i = 0; i < csArray.length; i++) {
      int dimension = csArray[i].getDimension();
      double[][] temp = new double[dimension][];
      // assign over the input values
      for (int j = 0; j < dimension; j++) {
        temp[j] = (double[]) refTuple[pointer+j];
      }

      // do the transformation
      temp = csArray[i].fromReference(temp);

      // assign values to the output array
      for (int j = 0; j < dimension; j++) {
        output[pointer+j] = temp[j];
      }

      pointer += dimension;
    }

    return output;
  }

  /**
   * Convert input array to reference coordinates.
   *
   * @param input   input array
   * @return  array of values in reference space
   * @throws VisADException  input array has the wrong dimension or is null
   */
  public float[][] toReference(float[][] input) throws VisADException {
    if (input.length != getDimension() || input == null)
      throw new VisADException("input has wrong dimension");
    int numElements = input[0].length;
    float[][] output = new float[getDimension()][];
    int pointer = 0;
    for (int i = 0; i < csArray.length; i++) {
      int dimension = csArray[i].getDimension();
      float[][] temp = new float[dimension][];
      // assign input values
      for (int j = 0; j < dimension; j++) {
        temp[j] = input[pointer+j];
      }

      // do the transformation
      temp = csArray[i].toReference(temp);

      // assign values to the output array
      for (int j = 0; j < dimension; j++) {
        output[pointer+j] = temp[j];
      }

      pointer += dimension;
    }

    return output;
  }

  /**
   * Convert array of reference valeus from Reference coordinates.  Input
   * values are modified in this transaction.
   *
   * @param refTuple   reference tuple array
   * @return  array of values in non-reference space
   * @throws VisADException  input array has the wrong dimension or is null
   */
  public float[][] fromReference(float[][] refTuple) throws VisADException {
    if (refTuple.length != getDimension() || refTuple == null)
      throw new VisADException("refTuple has wrong dimension");
    int numElements = refTuple[0].length;
    float[][] output = new float[getDimension()][];
    int pointer = 0;
    for (int i = 0; i < csArray.length; i++) {
      int dimension = csArray[i].getDimension();
      float[][] temp = new float[dimension][];
      // assign over the input values
      for (int j = 0; j < dimension; j++) {
        temp[j] = (float[]) refTuple[pointer+j];
      }

      // do the transformation
      temp = csArray[i].fromReference(temp);

      // assign values to the output array
      for (int j = 0; j < dimension; j++) {
        output[pointer+j] = temp[j];
      }

      pointer += dimension;
    }

    return output;
  }

  /**
   * Check to see if the object in question is equal to this
   * CartesianProductCoordinateSystem.
   *
   * @param  o  object in question
   * @return  true if the the object in question is composed of equal
   *          CoordinateSystems.
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof CartesianProductCoordinateSystem) {
      CoordinateSystem[] ocsa =
        ((CartesianProductCoordinateSystem) o).csArray;
      int n = csArray.length;
      if (n != ocsa.length) return false;

      for (int i = 0; i < n; i++) {
        if (!csArray[i].equals(ocsa[i])) return false;
      }

      return true;
    }

    return false;
  }

}

