
//
// LinearSet.java
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
   LinearSet represents a finite set of samples of R^Dimension
   in a cross product of three arithmetic progressions.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>
*/
public class LinearSet extends GriddedSet {

  Linear1DSet[] L;

  public LinearSet(MathType type, Linear1DSet[] l) throws VisADException {
    this(type, l, null, null, null);
  }

  public LinearSet(MathType type, double[] firsts, double[] lasts, int[] lengths)
         throws VisADException {
    this(type, get_linear1d_array(type, firsts, lasts, lengths), null,
         null, null);
  }

  public LinearSet(MathType type, Linear1DSet[] l, CoordinateSystem coord_sys,
                   Unit[] units, ErrorEstimate[] errors) throws VisADException {
    super(type, null, get_lengths(l), coord_sys, units, errors);
    if (DomainDimension != ManifoldDimension) {
      throw new SetException("LinearSet: DomainDimension != ManifoldDimension");
    }
    L = new Linear1DSet[DomainDimension];
    for (int j=0; j<DomainDimension; j++) {
      L[j] = l[j];
      if (SetErrors[j] != null ) {
        SetErrors[j] =
          new ErrorEstimate(SetErrors[j].getErrorValue(), (Low[j] + Hi[j]) / 2.0,
                            Length, SetErrors[j].getUnit());
      }
    }
  }

  public LinearSet(MathType type, double[] firsts, double[] lasts, int[] lengths,
                   CoordinateSystem coord_sys, Unit[] units,
                   ErrorEstimate[] errors) throws VisADException {
    this(type, get_linear1d_array(type, firsts, lasts, lengths), coord_sys,
         units, errors);
  }

  private static int[] get_lengths(Linear1DSet[] l) throws VisADException {
    // used by LinearSet constructor
    int[] lengths = new int[l.length];
    for (int j=0; j<l.length; j++) lengths[j] = l[j].getLength();
    return lengths;
  }

  static Linear1DSet[] get_linear1d_array(MathType type, double[] firsts,
                    double[] lasts, int[] lengths) throws VisADException {
    // used by LinearSet and IntegerSet constructors
    type = Set.adjustType(type);
    int len = lengths.length;
    if (len != firsts.length || len != lasts.length) {
      throw new SetException("LinearSet: dimensions don't match");
    }
    Linear1DSet[] l = new Linear1DSet[len];
    for (int j=0; j<len; j++) {
      RealType[] types =
        {(RealType) ((SetType) type).getDomain().getComponent(j)};
      SetType set_type = new SetType(new RealTupleType(types));
      l[j] = new Linear1DSet(set_type, firsts[j], lasts[j], lengths[j]);
    }
    return l;
  }

  static Linear1DSet[] get_linear1d_array(MathType type,
                                          double first1, double last1, int length1,
                                          double first2, double last2, int length2)
                       throws VisADException {
    double[] firsts = {first1, first2};
    double[] lasts = {last1, last2};
    int[] lengths = {length1, length2};
    return get_linear1d_array(type, firsts, lasts, lengths);
  }

  static Linear1DSet[] get_linear1d_array(MathType type,
                                          double first1, double last1, int length1,
                                          double first2, double last2, int length2,
                                          double first3, double last3, int length3)
                       throws VisADException {
    double[] firsts = {first1, first2, first3};
    double[] lasts = {last1, last2, last3};
    int[] lengths = {length1, length2, length3};
    return get_linear1d_array(type, firsts, lasts, lengths);
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public double[][] gridToValue(double[][] grid) throws VisADException {
    int j;
    if (grid.length != DomainDimension) {
      throw new SetException("LinearSet.gridToValue: bad dimension");
    }
    for (j=0; j<DomainDimension; j++) {
      if (Lengths[j] < 2) {
        throw new SetException("LinearSet.gridToValue: requires all grid " +
                               "dimensions to be > 1");
      }
    }
    int length = grid[0].length;
    double[][][] gridJ = new double[DomainDimension][1][];
    double[][][] valueJ = new double[DomainDimension][][];
    for (j=0; j<DomainDimension; j++) {
      gridJ[j][0] = grid[j];
      valueJ[j] = L[j].gridToValue(gridJ[j]);
    }
    double[][] value = new double[DomainDimension][];
    for (j=0; j<DomainDimension; j++) value[j] = valueJ[j][0];
    return value;
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public double[][] valueToGrid(double[][] value) throws VisADException {
    int j;
    if (value.length != DomainDimension) {
      throw new SetException("LinearSet.valueToGrid: bad dimension");
    }
    for (j=0; j<DomainDimension; j++) {
      if (Lengths[j] < 2) {
        throw new SetException("LinearSet.valueToGrid: requires all grid " +
                               "dimensions to be > 1");
      }
    }
    int length = value[0].length;
    double[][][] valueJ = new double[DomainDimension][1][];
    double[][][] gridJ = new double[DomainDimension][][];
    for (j=0; j<DomainDimension; j++) {
      valueJ[j][0] = value[j];
      gridJ[j] = L[j].valueToGrid(valueJ[j]);
    }
    double[][] grid = new double[DomainDimension][];
    for (j=0; j<DomainDimension; j++) grid[j] = gridJ[j][0];
    return grid;
  }

  public boolean isMissing() {
    return false;
  }

  public boolean isLinearSet() {
    return true;
  }

  public boolean equals(Object set) {
    if (!(set instanceof LinearSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    if (DomainDimension != ((LinearSet) set).getDimension()) return false;
    for (int j=0; j<DomainDimension; j++) {
      if (!L[j].equals(((LinearSet) set).getLinear1DComponent(j))) return false;
    }
    return true;
  }

  public Linear1DSet getLinear1DComponent(int i) {
    return L[i];
  }

  public Object clone() {
    try {
      Linear1DSet[] l = new Linear1DSet[DomainDimension];
      for (int j=0; j<DomainDimension; j++) {
        l[j] = (Linear1DSet) L[j].clone();
      }
      return new LinearSet(Type, l, DomainCoordinateSystem,
                           SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("LinearSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    Linear1DSet[] l = new Linear1DSet[DomainDimension];
    for (int j=0; j<DomainDimension; j++) {
      l[j] = (Linear1DSet) L[j].clone();
    }
    return new LinearSet(type, l, DomainCoordinateSystem,
                         SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s = pre + "LinearSet: Dimension = " +
               DomainDimension + " Length = " + Length + "\n";
    for (int j=0; j<DomainDimension; j++) {
      s = s + pre + "  Dimension " + j + ":" + " Length = " + L[j].getLength() +
              " Range = " + L[j].getFirst() + " to " + L[j].getLast() + "\n";
    }
    return s;
  }

}

