
//
// Linear3DSet.java
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
   Linear3DSet represents a finite set of samples of R^3 in
   a cross product of three arithmetic progressions.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>
*/
public class Linear3DSet extends Gridded3DSet {

  Linear1DSet X, Y, Z;

  public Linear3DSet(MathType type, Linear1DSet[] sets) throws VisADException {
    this(type, sets, null, null, null);
  }

  public Linear3DSet(MathType type, double first1, double last1, int length1,
                                    double first2, double last2, int length2,
                                    double first3, double last3, int length3)
         throws VisADException {
    this(type, LinearSet.get_linear1d_array(type, first1, last1, length1,
         first2, last2, length2, first3, last3, length3), null, null, null);
  }

  public Linear3DSet(MathType type, Linear1DSet[] sets,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    super(type, (double[][]) null, sets[0].getLength(), sets[1].getLength(),
          sets[2].getLength(), coord_sys, units, errors);
    if (DomainDimension != 3) {
      throw new SetException("Linear3DSet: DomainDimension must be 3");
    }
    if (sets.length != 3) {
      throw new SetException("Linear3DSet: ManifoldDimension must be 3");
    }
    X = sets[0];
    Y = sets[1];
    Z = sets[2];
    LengthX = X.getLength();
    LengthY = Y.getLength();
    LengthZ = Z.getLength();
    Length = LengthX * LengthY * LengthZ;
    Low[0] = X.getLowX();
    Hi[0] = X.getHiX();
    Low[1] = Y.getLowX();
    Hi[1] = Y.getHiX();
    Low[2] = Z.getLowX();
    Hi[2] = Z.getHiX();
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
    if (SetErrors[2] != null ) {
      SetErrors[2] =
        new ErrorEstimate(SetErrors[2].getErrorValue(), (Low[2] + Hi[2]) / 2.0,
                          Length, SetErrors[2].getUnit());
    }
  }

  public Linear3DSet(MathType type, double first1, double last1, int length1,
                     double first2, double last2, int length2, double first3,
                     double last3, int length3, CoordinateSystem coord_sys,
                     Unit[] units, ErrorEstimate[] errors) throws VisADException {
    this(type, LinearSet.get_linear1d_array(type, first1, last1, length1,
         first2, last2, length2, first3, last3, length3), coord_sys,
         units, errors);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    int[] indexX = new int[length];
    int[] indexY = new int[length];
    int[] indexZ = new int[length];
    double[][] values = new double[3][length];
 
    for (int i=0; i<length; i++) {
      if (0 <= index[i] && index[i] < Length) {
        indexX[i] = index[i] % LengthX;
        int k = index[i] / LengthX;
        indexY[i] = k % LengthY;
        indexZ[i] = k / LengthY;
      }
      else {
        indexX[i] = -1;
        indexY[i] = -1;
        indexZ[i] = -1;
      }
    }
    double[][] valuesX = X.indexToValue(indexX);
    double[][] valuesY = Y.indexToValue(indexY);
    double[][] valuesZ = Z.indexToValue(indexZ);
    values[0] = valuesX[0];
    values[1] = valuesY[0];
    values[2] = valuesZ[0];
    return values;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^3 */
  public double[][] gridToValue(double[][] grid) throws VisADException {
    if (grid.length != 3) {
      throw new SetException("Linear3DSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2) {
      throw new SetException("Linear3DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    double[][] gridX = new double[1][];
    gridX[0] = grid[0];
    double[][] gridY = new double[1][];
    gridY[0] = grid[1];
    double[][] gridZ = new double[1][];
    gridZ[0] = grid[2];
    double[][] valueX = X.gridToValue(gridX);
    double[][] valueY = Y.gridToValue(gridY);
    double[][] valueZ = Z.gridToValue(gridZ);
    double[][] value = new double[3][];
    value[0] = valueX[0];
    value[1] = valueY[0];
    value[2] = valueZ[0];
    return value;
  }

  /** transform an array of values in R^3 to an array
      of non-integer grid coordinates */
  public double[][] valueToGrid(double[][] value) throws VisADException {
    if (value.length != 3) {
      throw new SetException("Linear3DSet.valueToGrid: bad dimension");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2) {
      throw new SetException("Linear3DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = value[0].length;
    double[][] valueX = new double[1][];
    valueX[0] = value[0];
    double[][] valueY = new double[1][];
    valueY[0] = value[1];
    double[][] valueZ = new double[1][];
    valueZ[0] = value[2];
    double[][] gridX = X.valueToGrid(valueX);
    double[][] gridY = Y.valueToGrid(valueY);
    double[][] gridZ = Z.valueToGrid(valueZ);
    double[][] grid = new double[3][];
    grid[0] = gridX[0];
    grid[1] = gridY[0];
    grid[2] = gridZ[0];
    return grid;
  }

  public Linear1DSet getX() {
    return X;
  }

  public Linear1DSet getY() {
    return Y;
  }

  public Linear1DSet getZ() {
    return Z;
  }

  public boolean isMissing() {
    return false;
  }

  public boolean isLinearSet() {
    return true;
  }

  public boolean equals(Object set) {
    if (!(set instanceof Linear3DSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (X.equals(((Linear3DSet) set).getX()) &&
            Y.equals(((Linear3DSet) set).getY()) &&
            Z.equals(((Linear3DSet) set).getZ()));
  }

  public Object clone() {
    try {
      Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                            (Linear1DSet) Y.clone(),
                            (Linear1DSet) Z.clone()};
      return new Linear3DSet(Type, sets, DomainCoordinateSystem,
                             SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Linear3DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                          (Linear1DSet) Y.clone(),
                          (Linear1DSet) Z.clone()};
    return new Linear3DSet(type, sets, DomainCoordinateSystem,
                           SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s = pre + "Linear3DSet: Length = " + Length + "\n";
    s = s + pre + "  Dimension 1: Length = " + X.getLength() +
            " Range = " + X.getFirst() + " to " + X.getLast() + "\n";
    s = s + pre + "  Dimension 2: Length = " + Y.getLength() +
            " Range = " + Y.getFirst() + " to " + Y.getLast() + "\n";
    s = s + pre + "  Dimension 3: Length = " + Z.getLength() +
            " Range = " + Z.getFirst() + " to " + Z.getLast() + "\n";
    return s;
  }

}

