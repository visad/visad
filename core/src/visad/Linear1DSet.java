//
// Linear1DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
   Linear1DSet represents a finite set of samples of R in
   an arithmetic progression.<P>

   The samples are ordered from First to Last.<P>
*/
public class Linear1DSet extends Gridded1DSet
       implements LinearSet, GriddedDoubleSet {

  private double First, Last, Step, Invstep;
  private boolean cacheSamples;

  /** 
   * Construct a 1-D arithmetic progression with null 
   * errors and generic type 
   * @param first      first value in progression
   * @param last       last value in progression
   * @param length     number of samples in progression (R)
   * @throws  VisADException  problem creating set
   */
  public Linear1DSet(double first, double last, int length)
         throws VisADException {
    this(RealType.Generic, first, last, length, null, null, null);
  }

  /** 
   * Construct a 1-D arithmetic progression with the specified
   * <code>type</code> and null errors.
   * @param type       MathType for this Linear1DSet.  
   * @param first      first value in progression
   * @param last       last value in progression
   * @param length     number of samples in progression (R)
   * @throws  VisADException  problem creating set
   */
  public Linear1DSet(MathType type, double first, double last, int length)
         throws VisADException {
    this(type, first, last, length, null, null, null);
  }

  /** 
   * Construct a 1-D arithmetic progression with 
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear2DSet.  
   * @param first      first value in progression
   * @param last       last value in progression
   * @param length     number of samples in progression (R)
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with default
   *                   of <code>type</code> if specified.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear1DSet(MathType type, double first, double last, int length,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    this(type, first, last, length, coord_sys, units, errors, false);
  }

  /** 
   * Construct a 1-D arithmetic progression with 
   * the specified <code>type</code>, <code>coord_sys</code>, 
   * <code>units</code> and <code>errors</code>.
   * @param type       MathType for this Linear2DSet.  
   * @param first      first value in progression
   * @param last       last value in progression
   * @param length     number of samples in progression (R)
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with default
   *                   of <code>type</code> if specified.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}
   * @throws VisADException problem creating VisAD objects.
   */
  public Linear1DSet(MathType type, double first, double last, int length,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors, 
                     boolean cache) throws VisADException {
    super(type, (float[][]) null, length, coord_sys, units, errors);
    if (DomainDimension != 1) {
      throw new SetException("Linear1DSet: DomainDimension must be 1, not " +
                             DomainDimension);
    }
    First = first;
    Last = last;
    Length = length;
    if (Length < 1) throw new SetException("Linear1DSet: number of samples (" +
                                           Length + " must be greater than 0");
    Step = (Length < 2) ? 1.0 : (Last - First) / (Length - 1);
    Invstep = 1.0 / Step;
    LowX = (float) Math.min(First, First + Step * (Length - 1));
    HiX = (float) Math.max(First, First + Step * (Length - 1));
    Low[0] = LowX;
    Hi[0] = HiX;
    if (SetErrors[0] != null ) {
      SetErrors[0] =
        new ErrorEstimate(SetErrors[0].getErrorValue(), (Low[0] + Hi[0]) / 2.0,
                          Length, SetErrors[0].getUnit());
    }
    cacheSamples = cache;
  }

  /** 
   * Convert an array of 1-D indices to an array of values in 
   * the set corresponding to those indices.
   * @param index  array of indices of values in R^1 space.
   * @return  values in R^1 space corresponding to indices.
   * @throws  VisADException  problem converting indices to values.
   */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    float[][] values = new float[1][length];
    for (int i=0; i<length; i++) {
      if (0 <= index[i] && index[i] < Length) {
        values[0][i] = (float) (First + ((double) index[i]) * Step);
      }
      else {
        values[0][i] = Float.NaN;
      }
    }
    return values;
  }

  /** 
   * Convert an array of 1-D indices to an array of double values in 
   * the set corresponding to those indices.
   * @param index  array of indices of values in R space.
   * @return  values in R space corresponding to indices.
   * @throws  VisADException  problem converting indices to values.
   */
  public double[][] indexToDouble(int[] index) throws VisADException {
    int length = index.length;
    double[][] values = new double[1][length];
    for (int i=0; i<length; i++) {
      if (0 <= index[i] && index[i] < Length) {
        values[0][i] = (First + ((double) index[i]) * Step);
      }
      else {
        values[0][i] = Double.NaN;
      }
    }
    return values;
  }

  public int[] doubleToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Linear1DSet.doubleToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length;
    int[] index = new int[length];
    double l = -0.5;
    double h = Length - 0.5;
    for (int i=0; i<length; i++) {
      double di = 0.5 + (value[0][i] - First) * Invstep;
      index[i] = (0.0 < di && di < (double) Length) ? (int) di : -1;
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != 1) {
      throw new SetException("Linear1DSet.gridToValue: grid dimension" +
                             " should be 1, not " + grid.length);
    }
    /* remove DRM: 2004-09-14
    if (Length < 2) {
      throw new SetException("Linear1DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    */
    int length = grid[0].length;
    float[][] value = new float[1][length];
    float[] value0 = value[0];
    float[] grid0 = grid[0];
    float l = -0.5f;
    float h = ((float) Length) - 0.5f;
    float g;

    for (int i=0; i<length; i++) {
      g = grid0[i];
      value0[i] = (float) ((l < g && g < h) ? First + g * Step : Float.NaN);
    }
    return value;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R */
  public double[][] gridToDouble(double[][] grid) throws VisADException {
    if (grid.length != 1) {
      throw new SetException("Linear1DSet.gridToValue: grid dimension" +
                             " should be 1, not " + grid.length);
    }
    /* remove DRM: 2004-09-14
    if (Length < 2) {
      throw new SetException("Linear1DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    */
    int length = grid[0].length;
    double[][] value = new double[1][length];
    double[] value0 = value[0];
    double[] grid0 = grid[0];
    double l = -0.5;
    double h = (Length) - 0.5;
    double g;

    for (int i=0; i<length; i++) {
      g = grid0[i];
      value0[i] = ((l < g && g < h) ? First + g * Step : Float.NaN);
    }
    return value;
  }

  /** transform an array of values in R to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length != 1) {
      throw new SetException("Linear1DSet.valueToGrid: value dimension" +
                             " should be 1, not " + value.length);
    }
    /* remove DRM: 2004-09-14
    if (Lengths[0] < 2) {
      throw new SetException("Linear1DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    */
    int length = value[0].length;
    float[][] grid = new float[1][length];
    float[] grid0 = grid[0];
    float[] value0 = value[0];
    float l = (float) (First - 0.5 * Step);
    float h = (float) (First + (((float) Length) - 0.5) * Step);
    float v;

    if (h < l) {
      float temp = l;
      l = h;
      h = temp;
    }
    for (int i=0; i<length; i++) {
      v = value0[i];
      grid0[i] = (float) ((l < v && v < h) ? (v - First) * Invstep : Float.NaN);
    }
    return grid;
  }

  /** transform an array of values in R to an array
      of non-integer grid coordinates */
  public double[][] doubleToGrid(double[][] value) throws VisADException {
    if (value.length != 1) {
      throw new SetException("Linear1DSet.valueToGrid: value dimension" +
                             " should be 1, not " + value.length);
    }
    /* remove DRM: 2004-09-14
    if (Lengths[0] < 2) {
      throw new SetException("Linear1DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    */
    int length = value[0].length;
    double[][] grid = new double[1][length];
    double[] grid0 = grid[0];
    double[] value0 = value[0];
    double l = (First - 0.5 * Step);
    double h = (First + (((float) Length) - 0.5) * Step);
    double v;

    if (h < l) {
      double temp = l;
      l = h;
      h = temp;
    }
    for (int i=0; i<length; i++) {
      v = value0[i];
      grid0[i] = ((l < v && v < h) ? (v - First) * Invstep : Double.NaN);
    }
    return grid;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void doubleToInterp(double[][] value, int[][] indices,
    double[][] weights) throws VisADException
  {
    if (value.length != DomainDimension) {
      throw new SetException("Linear1DSet.doubleToInterp: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length; // number of values
    if (indices.length != length) {
      throw new SetException("Linear1DSet.doubleToInterp: indices length " +
                             indices.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    if (weights.length != length) {
      throw new SetException("Linear1DSet.doubleToInterp: weights length " +
                             weights.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    // convert value array to grid coord array
    double[][] grid = doubleToGrid(value);

    int i, j, k; // loop indices
    int lis; // temporary length of is & cs
    int length_is; // final length of is & cs, varies by i
    int isoff; // offset along one grid dimension
    double a, b; // weights along one grid dimension; a + b = 1.0
    int[] is; // array of indices, becomes part of indices
    double[] cs; // array of coefficients, become part of weights

    int base; // base index, as would be returned by valueToIndex
    int[] l = new int[ManifoldDimension]; // integer 'factors' of base
    // fractions with l; -0.5 <= c <= 0.5
    double[] c = new double[ManifoldDimension];

    // array of index offsets by grid dimension
    int[] off = new int[ManifoldDimension];
    off[0] = 1;
    for (j=1; j<ManifoldDimension; j++) off[j] = off[j-1] * Lengths[j-1];

    for (i=0; i<length; i++) {
      // compute length_is, base, l & c
      length_is = 1;
      if (Double.isNaN(grid[ManifoldDimension-1][i])) {
        base = -1;
      }
      else {
        l[ManifoldDimension-1] = (int) (grid[ManifoldDimension-1][i] + 0.5);
        // WLH 23 Dec 99
        if (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1]) {
          l[ManifoldDimension-1]--;
        }
        c[ManifoldDimension-1] = grid[ManifoldDimension-1][i] -
                                 ((double) l[ManifoldDimension-1]);
        if (!((l[ManifoldDimension-1] == 0 && c[ManifoldDimension-1] <= 0.0) ||
              (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1] - 1 &&
               c[ManifoldDimension-1] >= 0.0))) {
          // only interp along ManifoldDimension-1
          // if between two valid grid coords
          length_is *= 2;
        }
        base = l[ManifoldDimension-1];
      }
      for (j=ManifoldDimension-2; j>=0 && base>=0; j--) {
        if (Double.isNaN(grid[j][i])) {
          base = -1;
        }
        else {
          l[j] = (int) (grid[j][i] + 0.5);
          if (l[j] == Lengths[j]) l[j]--; // WLH 23 Dec 99
          c[j] = grid[j][i] - ((double) l[j]);
          if (!((l[j] == 0 && c[j] <= 0.0) ||
                (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
            // only interp along dimension j if between two valid grid coords
            length_is *= 2;
          }
          base = l[j] + Lengths[j] * base;
        }
      }

      if (base < 0) {
        // value is out of grid so return null
        is = null;
        cs = null;
      }
      else {
        // create is & cs of proper length, and init first element
        is = new int[length_is];
        cs = new double[length_is];
        is[0] = base;
        cs[0] = 1.0f;
        lis = 1;

        for (j=0; j<ManifoldDimension; j++) {
          if (!((l[j] == 0 && c[j] <= 0.0) ||
                (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
            // only interp along dimension j if between two valid grid coords
            if (c[j] >= 0.0) {
              // grid coord above base
              isoff = off[j];
              a = 1.0f - c[j];
              b = c[j];
            }
            else {
              // grid coord below base
              isoff = -off[j];
              a = 1.0f + c[j];
              b = -c[j];
            }
            // double is & cs; adjust new offsets; split weights
            for (k=0; k<lis; k++) {
              is[k+lis] = is[k] + isoff;
              cs[k+lis] = cs[k] * b;
              cs[k] *= a;
            }
            lis *= 2;
          }
        }
      }
      indices[i] = is;
      weights[i] = cs;
    }
  }

  /**
   * Get the first value in this arithmetic progression.
   * @return  first value
   */
  public double getFirst() {
    return First;
  }

  /**
   * Get the last value in this arithmetic progression.
   * @return  last value
   */
  public double getLast() {
    return Last;
  }

  /**
   * Get the interval between values in this progression
   * @return  step interval of progression
   */
  public double getStep() {
    return Step;
  }

  /**
   * Get the inverse of the step (1.0/getStep()).
   * @return  inverse of step interval of progression
   */
  public double getInvstep() {
    return Invstep;
  }

  /**
   * Check to see if this is an empty progression
   * @return always false.
   */
  public boolean isMissing() {
    return false;
  }

  /**
   * Return the array of values as doubles in R space corresponding to
   * this arithmetic progression.
   * @param  copy  if true, return a copy of the samples.
   * @return  array of values in R space.
   * @throws  VisADException  problem creating samples.
   */
  public double[][] getDoubles(boolean copy) throws VisADException {
    /*  DRM 2003-01-16
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToDouble(indices);
    */
    double[][] samples = new double[1][Length];
    for (int i=0; i<Length; i++) {
      samples[0][i] = (First + ((double) i) * Step);
    }
    return samples;
  }

  /**
   * Return the array of values in R space corresponding to
   * this arithmetic progression.
   * @param  copy  if true, return a copy of the samples.
   * @return  array of values in R space.
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

  /** code to actually enumerate the samples for this progression*/
  private float[][] makeSamples() throws VisADException {
    float[][] samples = new float[1][Length];
    for (int i=0; i<Length; i++) {
      samples[0][i] = (float) (First + ((double) i) * Step);
    }
    return samples;
  }

  /**
   * Check to see if this Linear1DSet is equal to the Object
   * in question.
   * @param  set  Object in question
   * @return true if <code>set</code> is a Linear1DSet and 
   *         the type, first, last and lengths of the progressions
   *         are equal.
   */
  public boolean equals(Object set) {
    boolean flag;
    if (!(set instanceof Linear1DSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      flag = (First == ((Linear1DSet) set).getFirst() &&
              Last == ((Linear1DSet) set).getLast() &&
              Length == ((Linear1DSet) set).getLength());
    }
    catch (VisADException e) {
      return false;
    }
    return flag;
  }

  /**
   * Returns the hash code for this instance.
   * @return                    The hash code for this instance.
   */
  public int hashCode()
  {
    if (!hashCodeSet)
    {
      hashCode =
        unitAndCSHashCode() ^
        new Double(First).hashCode() ^
        new Double(Last).hashCode() ^
        Length;
      hashCodeSet = true;
    }
    return hashCode;
  }

  /**
   * Get the indexed component (X is at 0 and is the only valid index)
   *
   * @param i Index of component
   * @return The requested component
   * @exception ArrayIndexOutOfBoundsException If an invalid index is
   *                                           specified.
   */
  public Linear1DSet getLinear1DComponent(int i) {
    if (i == 0) return this;
    else throw new ArrayIndexOutOfBoundsException("Invalid component index " +
                                                  i);
  }

  /**
   * Return a clone of this object with a new MathType.
   * @param  type  new MathType.
   * @return  new Linear1DSet with <code>type</code>.
   * @throws VisADException  if <code>type</code> is not compatible
   *                         with MathType of this.
   */
  public Object cloneButType(MathType type) throws VisADException {
    return new Linear1DSet(type, First, Last, Length, DomainCoordinateSystem,
                           SetUnits, SetErrors, cacheSamples);
  }

  /**
   * Extended version of the toString() method.
   * @param  pre  prefix for string.
   * @return wordy string describing this Linear1DSet.
   */
  public String longString(String pre) throws VisADException {
    return pre + "Linear1DSet: Length = " + Length +
           " Range = " + First + " to " + Last + "\n";
  }

}

