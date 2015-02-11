//
// Linear2DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
  private boolean cacheSamples;

  /**
   * Construct a 2-D cross product of <code>sets</code> with a
   * generic MathType.
   * @param sets     Linear1DSets that make up this Linear2DSet.
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear2DSet(Linear1DSet[] sets) throws VisADException {
    this (RealTupleType.Generic2D, sets, null, null, null);
  }

  /**
   * Construct a 2-D cross product of <code>sets</code> with the
   * specified <code>type</code>.
   * @param type     MathType for this Linear2DSet.  Must be consistent
   *                 with MathType-s of sets.
   * @param sets     Linear1DSets that make up this Linear2DSet.
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear2DSet(MathType type, Linear1DSet[] sets) throws VisADException {
    this (type, sets, null, null, null);
  }

  /** 
   * Construct a 2-D cross product of arithmetic progressions with
   * null errors and generic type.
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear2DSet(double first1, double last1, int length1,
                     double first2, double last2, int length2)
         throws VisADException {
    this(RealTupleType.Generic2D,
         LinearNDSet.get_linear1d_array(RealTupleType.Generic2D,
                                        first1, last1, length1,
                                        first2, last2, length2, null),
         null, null, null);
  }

  /** 
   * Construct a 2-D cross product of arithmetic progressions with
   * null errors and the specified <code>type</code>.
   * @param type       MathType for this Linear2DSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear2DSet(MathType type, double first1, double last1, int length1,
                                    double first2, double last2, int length2)
         throws VisADException {
    this(type, LinearNDSet.get_linear1d_array(type, first1, last1, length1,
                                              first2, last2, length2, null),
         null, null, null);
  }

  /** 
   * Construct a 2-D cross product of arithmetic progressions with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear2DSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear2DSet(MathType type, double first1, double last1, int length1,
                     double first2, double last2, int length2,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    this(type, first1, last1, length1,
         first2, last2, length2, coord_sys, units, errors, false);
  }

  /** 
   * Construct a 2-D cross product of arithmetic progressions with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear2DSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear2DSet(MathType type, double first1, double last1, int length1,
                     double first2, double last2, int length2,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors, 
                     boolean cache) throws VisADException {
    this(type,
         LinearNDSet.get_linear1d_array(type, first1, last1, length1,
                                        first2, last2, length2, units),
         coord_sys, units, errors, cache);
  }

  /**
   * Construct a 2-D cross product of <code>sets</code>, with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear2DSet.  Must be consistent
   *                   with MathType-s of <code>sets</code>.
   * @param sets       Linear1DSets that make up this Linear2DSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear2DSet(MathType type, Linear1DSet[] sets,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    this(type, sets, coord_sys, units, errors, false);
  }

  /**
   * Construct a 2-D cross product of <code>sets</code>, with
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear2DSet.  Must be consistent
   *                   with MathType-s of <code>sets</code>.
   * @param sets       Linear1DSets that make up this Linear2DSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}.
   * @throws VisADException illegal sets or other VisAD error.
   */
  public Linear2DSet(MathType type, Linear1DSet[] sets,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors, boolean cache) throws VisADException {
    super(type, (float[][]) null, sets[0].getLength(), sets[1].getLength(),
          coord_sys, LinearNDSet.units_array_linear1d(sets, units), errors);
    if (DomainDimension != 2) {
      throw new SetException("Linear2DSet: DomainDimension must be 2, not " +
                             DomainDimension);
    }
    if (sets.length != 2) {
      throw new SetException("Linear2DSet: ManifoldDimension must be 2" +
                             ", not " + sets.length);
    }
    Linear1DSet[] ss = LinearNDSet.linear1d_array_units(sets, units);
    X = ss[0];
    Y = ss[1];
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
    cacheSamples = cache;
  }

  /** 
   * Convert an array of 1-D indices to an array of values in 
   * R^2 space.
   * @param index  array of indices of values in R^2 space.
   * @return  values in R^2 space corresponding to indices.
   * @throws  VisADException  problem converting indices to values.
   */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    int[] indexX = new int[length];
    int[] indexY = new int[length];
    float[][] values = new float[2][];

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
      throw new SetException("Linear2DSet.gridToValue: grid dimension " +
                             grid.length +
                             " not equal to Manifold dimension " +
                             ManifoldDimension);
    }
    if (ManifoldDimension != 2) {
      throw new SetException("Linear2DSet.gridToValue: Manifold dimension " +
                             "must be 2, not " + ManifoldDimension);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
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
      throw new SetException("Linear2DSet.valueToGrid: value dimension" +
                             " must be 2, not " + value.length);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
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

  /**
   * Return the first arithmetic progression for this
   * cross product (X of XY).
   * @return first arithmetic progression as a Linear1DSet.
   */
  public Linear1DSet getX() {
    return X;
  }

  /**
   * Return the second arithmetic progression for this
   * cross product (Y of XY).
   * @return second arithmetic progression as a Linear1DSet.
   */
  public Linear1DSet getY() {
    return Y;
  }

  /**
   * Check to see if this is an empty cross-product.
   * @return always false.
   */
  public boolean isMissing() {
    return false;
  }

  /**
   * Return the array of values in R^2 space corresponding to
   * this cross product of arithmetic progressions.
   * @param  copy  if true, return a copy of the samples.
   * @return  array of values in R^2 space.
   * @throws  VisADException  problem creating samples.
   */
  public float[][] getSamples(boolean copy) throws VisADException {
    /*  DRM 2003-01-16
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToValue(indices);
    */
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

  /** code to actually enumerate the samples from the Linear1DSets
      into an array in R^2 space. */
  private float[][] makeSamples() throws VisADException {
    float[][] xVals = X.getSamples(false);
    float[][] yVals = Y.getSamples(false);
    float[][] samples = new float[2][getLength()];
    int idx = 0;
    for (int j = 0; j < yVals[0].length; j++) {
      for (int i = 0; i < xVals[0].length; i++) {
        samples[0][idx] = (float) xVals[0][i];
        samples[1][idx] = (float) yVals[0][j];
        idx++;
      }
    }
    return samples;
  }

  /**
   * Check to see if this Linear2DSet is equal to the Object
   * in question.
   * @param  set  Object in question
   * @return true if <code>set</code> is a Linear2DSet and each
   *         of the Linear1DSet-s that make up this cross product
   *         are equal.
   */
  public boolean equals(Object set) {
    if (!(set instanceof Linear2DSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (X.equals(((Linear2DSet) set).getX()) &&
            Y.equals(((Linear2DSet) set).getY()));
  }

  /**
   * Returns the hash code for this instance.
   * @return                    The hash code for this instance.
   */
  public int hashCode()
  {
    if (!hashCodeSet)
    {
      hashCode = unitAndCSHashCode() ^ X.hashCode() ^ Y.hashCode();
      hashCodeSet = true;
    }
    return hashCode;
  }

  /**
   * Get the indexed component (X is at 0, Y is at 1)
   *
   * @param i Index of component
   * @return The requested component
   * @exception ArrayIndexOutOfBoundsException If an invalid index is
   *                                           specified.
   */
  public Linear1DSet getLinear1DComponent(int i) {
    if (i == 0) return getX();
    else if (i == 1) return getY();
    else if (i < 0) {
      throw new ArrayIndexOutOfBoundsException("Negative component index " + i);
    } else {
      throw new ArrayIndexOutOfBoundsException("Component index " + i +
                                               " must be less than 2");
    }
  }

  /**
   * Return a clone of this object with a new MathType.
   * @param  type  new MathType.
   * @return  new Linear2DSet with <code>type</code>.
   * @throws VisADException  if <code>type</code> is not compatible
   *                         with MathType of component Linear1DSets.
   */
  public Object cloneButType(MathType type) throws VisADException {
    Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                          (Linear1DSet) Y.clone()};
    return new Linear2DSet(type, sets, DomainCoordinateSystem,
                           SetUnits, SetErrors, cacheSamples);
  }

  /**
   * Extended version of the toString() method.
   * @param  pre  prefix for string.
   * @return wordy string describing this Linear2DSet.
   */
  public String longString(String pre) throws VisADException {
    String s = pre + "Linear2DSet: Length = " + Length + "\n";
    s = s + pre + "  Dimension 1: Length = " + X.getLength() +
            " Range = " + X.getFirst() + " to " + X.getLast() + "\n";
    s = s + pre + "  Dimension 2: Length = " + Y.getLength() +
            " Range = " + Y.getFirst() + " to " + Y.getLast() + "\n";
    return s;
  }

}

