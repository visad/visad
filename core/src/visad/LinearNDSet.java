//
// LinearNDSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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
   LinearNDSet represents a finite set of samples of R^Dimension
   in a cross product of arithmetic progressions.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>
*/
public class LinearNDSet extends GriddedSet
       implements LinearSet {

  Linear1DSet[] L;
  private boolean cacheSamples = false;

  /** 
   * Construct an N-dimensional set as the product of N Linear1DSets,
   * with null errors, CoordinateSystem and Units are defaults from
   * type.
   * @param type     MathType for this LinearNDSet.  Must be consistent
   *                 with MathType-s of sets.
   * @param l	     Linear1DSets that make up this LinearNDSet.
   * @throws VisADException problem creating VisAD objects.
   */
  public LinearNDSet(MathType type, Linear1DSet[] l) throws VisADException {
    this(type, l, null, null, null);
  }

  /** 
   * Construct an N-dimensional set as the product of N arithmetic
   * progressions (lengths[i] samples between firsts[i] and lasts[i]),
   * with null errors, CoordinateSystem and Units are defaults from type 
   * @param type       MathType for this LinearNDSet.
   * @param firsts     array of first values for each of the 
   *                   arithmetic progressions
   * @param lasts      array of last values for each of the 
   *                   arithmetic progressions
   * @param lengths    array of number of samples for each of the 
   *                   arithmetic progressions
   * @throws VisADException problem creating VisAD objects.
   */
  public LinearNDSet(MathType type, double[] firsts, double[] lasts, int[] lengths)
         throws VisADException {
    this(type, get_linear1d_array(type, firsts, lasts, lengths, null),
         null, null, null);
  }

  /** 
   * Construct an N-dimensional set as the product of N arithmetic
   * progressions (lengths[i] samples between firsts[i] and lasts[i]),
   * coordinate_system and units must be compatible with defaults for
   * type, or may be null; errors may be null.
   * @param type       MathType for this LinearNDSet.
   * @param firsts     array of first values for each of the 
   *                   arithmetic progressions
   * @param lasts      array of last values for each of the 
   *                   arithmetic progressions
   * @param lengths    array of number of samples for each of the 
   *                   arithmetic progressions
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with default of
   *                   <code>type</code> if not null.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException problem creating VisAD objects.
   */
  public LinearNDSet(MathType type, double[] firsts, 
                     double[] lasts, int[] lengths,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    this(type, firsts, lasts, lengths, coord_sys, units, errors, false);
  }

  /** 
   * Construct an N-dimensional set as the product of N arithmetic
   * progressions (lengths[i] samples between firsts[i] and lasts[i]),
   * coordinate_system and units must be compatible with defaults for
   * type, or may be null; errors may be null.
   * @param type       MathType for this LinearNDSet.
   * @param firsts     array of first values for each of the 
   *                   arithmetic progressions
   * @param lasts      array of last values for each of the 
   *                   arithmetic progressions
   * @param lengths    array of number of samples for each of the 
   *                   arithmetic progressions
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with default of
   *                   <code>type</code> if not null.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}.
   * @throws VisADException problem creating VisAD objects.
   */
  public LinearNDSet(MathType type, double[] firsts, 
                     double[] lasts, int[] lengths,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors,
                     boolean cache) throws VisADException {
    this(type, get_linear1d_array(type, firsts, lasts, lengths, units),
         coord_sys, units, errors, cache);
  }

  /** 
   * Construct an N-dimensional set as the product of N Linear1DSets;
   * coordinate_system and units must be compatible with defaults
   * for type, or may be null; errors may be null 
   * @param type     MathType for this LinearNDSet.  Must be consistent
   *                 with MathType-s of sets.
   * @param l	     Linear1DSets that make up this LinearNDSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with default of
   *                   <code>type</code> if not null.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException problem creating VisAD objects.
   */
  public LinearNDSet(MathType type, Linear1DSet[] l, CoordinateSystem coord_sys,
                   Unit[] units, ErrorEstimate[] errors) throws VisADException {
    this(type, l, coord_sys, units, errors, false);
  }

  /** 
   * Construct an N-dimensional set as the product of N Linear1DSets;
   * coordinate_system and units must be compatible with defaults
   * for type, or may be null; errors may be null 
   * @param type     MathType for this LinearNDSet.  Must be consistent
   *                 with MathType-s of sets.
   * @param l	     Linear1DSets that make up this LinearNDSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with default of
   *                   <code>type</code> if not null.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}.
   * @throws VisADException problem creating VisAD objects.
   */
  public LinearNDSet(MathType type, Linear1DSet[] l,
                   CoordinateSystem coord_sys,
                   Unit[] units, ErrorEstimate[] errors,
                   boolean cache) throws VisADException {
    super(type, null, get_lengths(l), coord_sys,
          LinearNDSet.units_array_linear1d(l, units), errors);
    if (DomainDimension != ManifoldDimension) {
      throw new SetException("LinearNDSet: DomainDimension != ManifoldDimension");
    }
    L = LinearNDSet.linear1d_array_units(l, units);
    for (int j=0; j<DomainDimension; j++) {
      Low[j] = L[j].getLowX();
      Hi[j] = L[j].getHiX();
      if (SetErrors[j] != null ) {
        SetErrors[j] =
          new ErrorEstimate(SetErrors[j].getErrorValue(), (Low[j] + Hi[j]) / 2.0,
                            Length, SetErrors[j].getUnit());
      }
    }
    cacheSamples = cache;
  }

  private static int[] get_lengths(Linear1DSet[] l) throws VisADException {
    // used by LinearNDSet constructor
    int[] lengths = new int[l.length];
    for (int j=0; j<l.length; j++) lengths[j] = l[j].getLength();
    return lengths;
  }

  /**
   * General Factory method for creating the proper linear set
   * (Linear1DSet, Linear2DSet, etc.).
   */
  public static LinearSet create(MathType type, double[] firsts,
                                  double[] lasts, int[] lengths)
         throws VisADException {
    return create(type, firsts, lasts, lengths, null, null, null);
  }

  public static LinearSet create(MathType type, double[] firsts, double[] lasts,
                                  int[] lengths, CoordinateSystem coord_sys,
                                  Unit[] units, ErrorEstimate[] errors)
         throws VisADException {
    switch (firsts.length) {
      case 1:
        return new Linear1DSet(type, firsts[0], lasts[0], lengths[0],
                                coord_sys, units, errors);
      case 2:
        return new Linear2DSet(type, firsts[0], lasts[0], lengths[0],
                                firsts[1], lasts[1], lengths[1],
                                coord_sys, units, errors);
      case 3:
        return new Linear3DSet(type, firsts[0], lasts[0], lengths[0],
                                firsts[1], lasts[1], lengths[1],
                                firsts[2], lasts[2], lengths[2],
                                coord_sys, units, errors);
      default:
        return new LinearNDSet(type, firsts, lasts, lengths,
                               coord_sys, units, errors);
    }
  }

  static Linear1DSet[] linear1d_array_units(Linear1DSet[] sets,
                    Unit[] units) throws VisADException {
    if (units == null) return sets;
    int n = sets.length;
    if (units.length != n) {
      throw new SetException("units and sets lengths don't match");
    }
    Linear1DSet[] ss = new Linear1DSet[n];
    for (int i=0; i<n; i++) {
      Unit[] su = sets[i].getSetUnits();
      if (units[i] == null || units[i].equals(su[0])) {
        ss[i] = sets[i];
      }
      else {
        CoordinateSystem cs = sets[i].getCoordinateSystem();
        double first = sets[i].getFirst();
        double last = sets[i].getLast();
        if (su[0] != null) {
          first = units[i].toThis(first, su[0]);
          last = units[i].toThis(last, su[0]);
        }
        su = new Unit[] {units[i]};
        ss[i] = new Linear1DSet(sets[i].getType(), first, last,
                                sets[i].getLength(),
                                sets[i].getCoordinateSystem(), su,
                                sets[i].getSetErrors());
      }
    }
    return ss;
  }

  static Unit[] units_array_linear1d(Linear1DSet[] sets,
                    Unit[] units) throws VisADException {
    int n = sets.length;
    Unit[] newu = new Unit[n];
    if (units != null && units.length != n) {
      throw new SetException("units and sets lengths don't match");
    }
    for (int i=0; i<n; i++) {
      if (units != null && units[i] != null) {
        newu[i] = units[i];
      }
      else {
        Unit[] su = sets[i].getSetUnits();
        if (su != null) newu[i] = su[0];
      }
    }
    return newu;
  }

  static Linear1DSet[] get_linear1d_array(MathType type, double[] firsts,
                    double[] lasts, int[] lengths, Unit[] units)
         throws VisADException {
    // used by LinearNDSet and IntegerNDSet constructors
    type = Set.adjustType(type);
    int len = lengths.length;
    if (len != firsts.length || len != lasts.length) {
      throw new SetException("LinearNDSet: array dimensions don't match");
    }
    Linear1DSet[] l = new Linear1DSet[len];
    for (int j=0; j<len; j++) {
      RealType[] types =
        {(RealType) ((SetType) type).getDomain().getComponent(j)};
      SetType set_type = new SetType(new RealTupleType(types));
      Unit[] us = {null};
      if (units != null && units.length > j) us[0] = units[j];
      l[j] = new Linear1DSet(set_type, firsts[j], lasts[j], lengths[j],
                             null, us, null);
    }
    return l;
  }

  static Linear1DSet[] get_linear1d_array(MathType type,
                 double first1, double last1, int length1,
                 double first2, double last2, int length2, Unit[] units)
                       throws VisADException {
    double[] firsts = {first1, first2};
    double[] lasts = {last1, last2};
    int[] lengths = {length1, length2};
    return get_linear1d_array(type, firsts, lasts, lengths, units);
  }

  static Linear1DSet[] get_linear1d_array(MathType type,
                 double first1, double last1, int length1,
                 double first2, double last2, int length2,
                 double first3, double last3, int length3, Unit[] units)
                       throws VisADException {
    double[] firsts = {first1, first2, first3};
    double[] lasts = {last1, last2, last3};
    int[] lengths = {length1, length2, length3};
    return get_linear1d_array(type, firsts, lasts, lengths, units);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int dim = getDimension();
    int length = index.length;
    int[][] indexN = new int[dim][length];
    float[][] values = new float[dim][];
    int[] lengthN = new int[dim];
    for (int j=0; j<dim; j++) lengthN[j] = L[j].getLength();
    for (int i=0; i<length; i++) {
      int k = index[i];
      if (0 <= k && k < Length) {
        for (int j=0; j<dim-1; j++) {
          indexN[j][i] = k % lengthN[j];
          k = k / lengthN[j];
        }
        indexN[dim-1][i] = k;
      }
      else {
        for (int j=0; j<dim; j++) indexN[j][i] = -1;
      }
    }
    for (int j=0; j<dim; j++) {
      float[][] vals = L[j].indexToValue(indexN[j]);
      values[j] = vals[0];
    }
    return values;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    int j;
    if (grid.length != DomainDimension) {
      throw new SetException("LinearNDSet.gridToValue: grid dimension " +
                             grid.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    if (Length > 1) {
      for (j=0; j<DomainDimension; j++) {
        if (Lengths[j] < 2) {
          throw new SetException("LinearNDSet.gridToValue: requires all grid " +
                                 "dimensions to be > 1");
        }
      }
    }
    int length = grid[0].length;
    float[][][] gridJ = new float[DomainDimension][1][];
    float[][][] valueJ = new float[DomainDimension][][];
    for (j=0; j<DomainDimension; j++) {
      gridJ[j][0] = grid[j];
      valueJ[j] = L[j].gridToValue(gridJ[j]);
    }
    float[][] value = new float[DomainDimension][];
    for (j=0; j<DomainDimension; j++) value[j] = valueJ[j][0];
    return value;
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    int j;
    if (value.length != DomainDimension) {
      throw new SetException("LinearNDSet.valueToGrid: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    if (Length > 1) {
      for (j=0; j<DomainDimension; j++) {
        if (Lengths[j] < 2) {
          throw new SetException("LinearNDSet.valueToGrid: requires all grid " +
                                 "dimensions to be > 1");
        }
      }
    }
    int length = value[0].length;
    float[][][] valueJ = new float[DomainDimension][1][];
    float[][][] gridJ = new float[DomainDimension][][];
    for (j=0; j<DomainDimension; j++) {
      valueJ[j][0] = value[j];
      gridJ[j] = L[j].valueToGrid(valueJ[j]);
    }
    float[][] grid = new float[DomainDimension][];
    for (j=0; j<DomainDimension; j++) grid[j] = gridJ[j][0];
    return grid;
  }

  /**
   * Check to see if this is an empty cross-product.
   * @return always false.
   */
  public boolean isMissing() {
    return false;
  }

  /**
   * Get the array of first values of each of the arithmetic progressions
   * in this cross product.
   * @return  array of first values.
   */
  public double[] getFirsts() throws VisADException {
    double[] firsts = new double[L.length];
    for (int j=0; j<firsts.length; j++) firsts[j] = L[j].getFirst();
    return firsts;
  }

  /**
   * Get the array of last values of each of the arithmetic progressions
   * in this cross product.
   * @return  array of last values.
   */
  public double[] getLasts() throws VisADException {
    double[] lasts = new double[L.length];
    for (int j=0; j<lasts.length; j++) lasts[j] = L[j].getLast();
    return lasts;
  }

  /**
   * Return the array of values in R^N space corresponding to
   * this cross product of arithmetic progressions.
   * @param  copy  if true, return a copy of the samples.
   * @return  array of values in R^N space.
   * @throws  VisADException  problem creating samples.
   */
  public float[][] getSamples(boolean copy) throws VisADException {
    float[][]mySamples = getMySamples();
    if (mySamples != null) {
      return copy ? Set.copyFloats(mySamples) : mySamples;
    }
    float[][] samples = makeSamples ();
    if (cacheSamples) {
      setMySamples(samples);
      return copy ? Set.copyFloats(samples) : samples;
    }
    return samples;
  }

  private float[][] makeSamples() throws VisADException {
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToValue(indices);
  }

  /**
   * Check to see if this LinearNDSet is equal to the Object
   * in question.
   * @param  set  Object in question
   * @return true if <code>set</code> is a LinearNDSet and each
   *         of the Linear1DSet-s that make up this cross product
   *         are equal.
   */
  public boolean equals(Object set) {
    if (!(set instanceof LinearNDSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    if (DomainDimension != ((LinearNDSet) set).getDimension()) return false;
    for (int j=0; j<DomainDimension; j++) {
      if (!L[j].equals(((LinearNDSet) set).getLinear1DComponent(j))) return false;
    }
    return true;
  }

  /**
   * Returns the hash code for this instance.
   * @return                    The hash code for this instance.
   */
  public int hashCode()
  {
    if (!hashCodeSet)
    {
      hashCode = unitAndCSHashCode();
      for (int i = 0; i < DomainDimension; ++i)
        hashCode ^= L[i].hashCode();
      hashCodeSet = true;
    }
    return hashCode;
  }

  /**
   * Get the indexed component.
   *
   * @param i Index of component
   * @return The requested component
   * @exception ArrayIndexOutOfBoundsException If an invalid index is
   *                                           specified.
   */
  public Linear1DSet getLinear1DComponent(int i) {
    return L[i];
  }

  /**
   * Return a clone of this object with a new MathType.
   * @param  type  new MathType.
   * @return  new LinearNDSet with <code>type</code>.
   * @throws VisADException  if <code>type</code> is not compatible
   *                         with MathType of component Linear1DSets.
   */
  public Object cloneButType(MathType type) throws VisADException {
    Linear1DSet[] l = new Linear1DSet[DomainDimension];
    for (int j=0; j<DomainDimension; j++) {
      l[j] = (Linear1DSet) L[j].clone();
    }
    return new LinearNDSet(type, l, DomainCoordinateSystem,
                         SetUnits, SetErrors, cacheSamples);
  }

  /**
   * Extended version of the toString() method.
   * @param  pre  prefix for string.
   * @return wordy string describing this LinearNDSet.
   */
  public String longString(String pre) throws VisADException {
    String s = pre + "LinearNDSet: Dimension = " +
               DomainDimension + " Length = " + Length + "\n";
    for (int j=0; j<DomainDimension; j++) {
      s = s + pre + "  Dimension " + j + ":" + " Length = " + L[j].getLength() +
              " Range = " + L[j].getFirst() + " to " + L[j].getLast() + "\n";
    }
    return s;
  }

  /** run 'java visad.LinearNDSet' to test the LinearNDSet class */
  public static void main(String args[]) throws VisADException {
    RealTupleType type =
      new RealTupleType(RealType.Generic, RealType.Generic);
    double[] firsts = {0.0, 0.0};
    double[] lasts = {3.0, 3.0};
    int[] lengths = {4, 4};
    LinearNDSet set = new LinearNDSet(type, firsts, lasts, lengths);
    float[][] values = set.getSamples();
    int n = values.length;
    int m = values[0].length;
    for (int j=0; j<m; j++) {
      for (int i=0; i<n; i++) {
        System.out.println("values[" + i + "][" + j + "] = " +
                           values[i][j]);
      }
    }
  }

}

