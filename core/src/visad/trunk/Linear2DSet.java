
//
// Linear2DSet.java
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
   Linear2DSet represents a finite set of samples of R^2 in
   a cross product of two arithmetic progressions.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For example, here is the order of 30 product samples when the
   first component is has 6 samples and the second component has
   5 samples (note index is 0-based):<P>

<PRE>
<!-- -->             second (Y) component
<!-- -->
<!-- -->   first      0   6  12  18  24
<!-- -->              1   7  13  19  25
<!-- -->    (X)       2   8  14  20  26
<!-- -->              3   9  15  21  27
<!-- -->  component   4  10  16  22  28
<!-- -->              5  11  17  23  29
</PRE>
*/
public class Linear2DSet extends Gridded2DSet
       implements LinearSet {

  Linear1DSet X, Y;

  public Linear2DSet(Linear1DSet[] sets) throws VisADException {
    this (RealTupleType.Generic2D, sets, null, null, null);
  }

  public Linear2DSet(MathType type, Linear1DSet[] sets) throws VisADException {
    this (type, sets, null, null, null);
  }

  /** a 2-D cross product of arithmetic progressions with
      null errors and generic type */
  public Linear2DSet(double first1, double last1, int length1,
                     double first2, double last2, int length2)
         throws VisADException {
    this(RealTupleType.Generic2D, LinearNDSet.get_linear1d_array(
           RealTupleType.Generic2D, first1, last1, length1,
           first2, last2, length2), null, null, null);
  }

  public Linear2DSet(MathType type, double first1, double last1, int length1,
                                    double first2, double last2, int length2)
         throws VisADException {
    this(type, LinearNDSet.get_linear1d_array(type, first1, last1, length1,
         first2, last2, length2), null, null, null);
  }

  public Linear2DSet(MathType type, Linear1DSet[] sets,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    super(type, (float[][]) null, sets[0].getLength(), sets[1].getLength(),
          coord_sys, units, errors);
    if (DomainDimension != 2) {
      throw new SetException("Linear2DSet: DomainDimension must be 2");
    }
    if (sets.length != 2) {
      throw new SetException("Linear2DSet: ManifoldDimension must be 2");
    }
    X = sets[0];
    Y = sets[1];
    LengthX = X.getLength();
    LengthY = Y.getLength();
    Length = LengthX * LengthY;
    Low[0] = X.getLowX();
    Hi[0] = X.getHiX();
    Low[1] = Y.getLowX();
    Hi[1] = Y.getHiX();
    if (SetErrors[0] != null ) {
      SetErrors[0] =
        new ErrorEstimate(SetErrors[0].getErrorValue(), (Low[0] + Hi[0]) / 2.0,
                          Length, SetErrors[0].getUnit());
    }
    if (SetErrors[1] != null ) {
      SetErrors[1] =
        new ErrorEstimate(SetErrors[1].getErrorValue(), (Low[1] + Hi[1]) / 2.0,
                          Length, SetErrors[1].getUnit());
    }
  }

  /** a 2-D cross product of arithmetic progressions;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Linear2DSet(MathType type, double first1, double last1, int length1,
                     double first2, double last2, int length2,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    this(type, LinearNDSet.get_linear1d_array(type, first1, last1, length1,
         first2, last2, length2), coord_sys, units, errors);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    int[] indexX = new int[length];
    int[] indexY = new int[length];
    float[][] values = new float[2][length];
 
    for (int i=0; i<length; i++) {
      if (0 <= index[i] && index[i] < Length) {
        indexX[i] = index[i] % LengthX;
        indexY[i] = index[i] / LengthX;
      }
      else {
        indexX[i] = -1;
        indexY[i] = -1;
      }
    }
    float[][] valuesX = X.indexToValue(indexX);
    float[][] valuesY = Y.indexToValue(indexY);
    values[0] = valuesX[0];
    values[1] = valuesY[0];
    return values;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^2 */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != ManifoldDimension) {
      throw new SetException("Linear2DSet.gridToValue: bad dimension");
    }
    if (ManifoldDimension != 2) {
      throw new SetException("Linear2DSet.gridToValue: ManifoldDimension " +
                             "must be 2");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2) {
      throw new SetException("Linear2DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    float[][] gridX = new float[1][];
    gridX[0] = grid[0];
    float[][] gridY = new float[1][];
    gridY[0] = grid[1];
    float[][] valueX = X.gridToValue(gridX);
    float[][] valueY = Y.gridToValue(gridY);
    float[][] value = new float[2][];
    value[0] = valueX[0];
    value[1] = valueY[0];
    return value;
  }

  /** transform an array of values in R^2 to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length != 2) {
      throw new SetException("Linear2DSet.valueToGrid: bad dimension");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2) {
      throw new SetException("Linear2DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = value[0].length;
    float[][] valueX = new float[1][];
    valueX[0] = value[0];
    float[][] valueY = new float[1][];
    valueY[0] = value[1];
    float[][] gridX = X.valueToGrid(valueX);
    float[][] gridY = Y.valueToGrid(valueY);
    float[][] grid = new float[2][];
    grid[0] = gridX[0];
    grid[1] = gridY[0];
    return grid;
  }

  public Linear1DSet getX() {
    return X;
  }

  public Linear1DSet getY() {
    return Y;
  }

  public boolean isMissing() {
    return false;
  }

  public float[][] getSamples(boolean copy) throws VisADException {
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToValue(indices);
  }

  public boolean equals(Object set) {
    if (!(set instanceof Linear2DSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (X.equals(((Linear2DSet) set).getX()) &&
            Y.equals(((Linear2DSet) set).getY()));
  }

  public Linear1DSet getLinear1DComponent(int i) {
    if (i == 0) return getX();
    else if (i == 1) return getY();
    else throw new ArrayIndexOutOfBoundsException("Invalid component index");
  }

  public Object clone() {
    try {
      Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                            (Linear1DSet) Y.clone()};
      return new Linear2DSet(Type, sets, DomainCoordinateSystem,
                             SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Linear2DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                          (Linear1DSet) Y.clone()};
    return new Linear2DSet(type, sets, DomainCoordinateSystem,
                           SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s = pre + "Linear2DSet: Length = " + Length + "\n";
    s = s + pre + "  Dimension 1: Length = " + X.getLength() +
            " Range = " + X.getFirst() + " to " + X.getLast() + "\n";
    s = s + pre + "  Dimension 2: Length = " + Y.getLength() +
            " Range = " + Y.getFirst() + " to " + Y.getLast() + "\n";
    return s;
  }

}

