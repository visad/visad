//
// Gridded1DDoubleSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
   Gridded1DDoubleSet is a Gridded1DSet with double-precision samples.<P>
*/
public class Gridded1DDoubleSet extends Gridded1DSet
       implements GriddedDoubleSet {

  double LowX, HiX;
  double[][] Samples;


  // Overridden Gridded1DSet constructors (float[][])

  /** a 1-D sequence with no regular interval with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded1DDoubleSet(MathType type, float[][] samples, int lengthX)
         throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, null, null, null);
  }

  public Gridded1DDoubleSet(MathType type, float[][] samples, int lengthX,
                            CoordinateSystem coord_sys, Unit[] units,
                            ErrorEstimate[] errors) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX,
         coord_sys, units, errors, true);
  }

  /** a 1-D sorted sequence with no regular interval. samples array
      is organized float[1][number_of_samples] where lengthX =
      number_of_samples. samples must be sorted (either increasing
      or decreasing). coordinate_system and units must be compatible
      with defaults for type, or may be null. errors may be null */
  Gridded1DDoubleSet(MathType type, float[][] samples, int lengthX,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors, boolean copy)
                     throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX,
      coord_sys, units, errors, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];

    if (Samples != null && Lengths[0] > 1) {
      // samples consistency test
      for (int i=0; i<Length; i++) {
        if (Samples[0][i] != Samples[0][i]) {
          throw new SetException(
           "Gridded1DSet: samples values may not be missing");
        }
      }
      Ascending = (Samples[0][LengthX-1] > Samples[0][0]);
      if (Ascending) {
        for (int i=1; i<LengthX; i++) {
          if (Samples[0][i] < Samples[0][i-1]) {
            throw new SetException(
             "Gridded1DSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
      else { // !Ascending
        for (int i=1; i<LengthX; i++) {
          if (Samples[0][i] > Samples[0][i-1]) {
            throw new SetException(
             "Gridded1DSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
    }
  }


  // Corresponding Gridded1DDoubleSet constructors (double[][])

  /** a 1-D sequence with no regular interval with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded1DDoubleSet(MathType type, double[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null);
  }

  public Gridded1DDoubleSet(MathType type, double[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, coord_sys, units, errors, true);
  }

  /** a 1-D sorted sequence with no regular interval. samples array
      is organized double[1][number_of_samples] where lengthX =
      number_of_samples. samples must be sorted (either increasing
      or decreasing). coordinate_system and units must be compatible
      with defaults for type, or may be null. errors may be null */
  Gridded1DDoubleSet(MathType type, double[][] samples, int lengthX,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors, boolean copy)
                     throws VisADException {
    super(type, null, lengthX, coord_sys, units, errors, copy);
    if (samples == null) {
      throw new SetException("Gridded1DDoubleSet: samples are null");
    }
    init_samples(samples, true);
    Low = new float[] {(float) LowX};
    Hi = new float[] {(float) HiX};
    LengthX = Lengths[0];

    if (Samples != null && Lengths[0] > 1) {
      // samples consistency test
      for (int i=0; i<Length; i++) {
        if (Samples[0][i] != Samples[0][i]) {
          throw new SetException(
           "Gridded1DDoubleSet: samples values may not be missing");
        }
      }
      Ascending = (Samples[0][LengthX-1] > Samples[0][0]);
      if (Ascending) {
        for (int i=1; i<LengthX; i++) {
          if (Samples[0][i] < Samples[0][i-1]) {
            throw new SetException(
             "Gridded1DDoubleSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
      else { // !Ascending
        for (int i=1; i<LengthX; i++) {
          if (Samples[0][i] > Samples[0][i-1]) {
            throw new SetException(
             "Gridded1DDoubleSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
    }
  }


  // Overridden Gridded1DSet methods (float[][])

  public float[][] getSamples() throws VisADException {
    return getSamples(true);
  }

  public float[][] getSamples(boolean copy) throws VisADException {
    return Set.doubleToFloat(Samples);
  }

  /** convert an array of 1-D indices to an array of values in
      R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    return Set.doubleToFloat(indexToDouble(index));
  }

  /**
   * Convert an array of values in R^DomainDimension to an array of
   * 1-D indices.  This Gridded1DDoubleSet must have at least two points in the
   * set.
   * @param value	An array of coordinates.  <code>value[i][j]
   *			<code> contains the <code>i</code>th component of the
   *			<code>j</code>th point.
   * @return		Indices of nearest points.  RETURN_VALUE<code>[i]</code>
   *			will contain the index of the point in the set closest
   *			to <code>value[][i]</code> or <code>-1</code> if
   *			<code>value[][i]</code> lies outside the set.
   */
  public int[] valueToIndex(float[][] value) throws VisADException {
    return doubleToIndex(Set.floatToDouble(value));
  }

  public float[][] gridToValue(float[][] grid) throws VisADException {
    return Set.doubleToFloat(gridToDouble(Set.floatToDouble(grid)));
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    return Set.doubleToFloat(doubleToGrid(Set.floatToDouble(value)));
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
    float[][] weights) throws VisADException
  {
    int len = weights.length;
    double[][] w = new double[len][];
    for (int i=0; i<len; i++) w[i] = new double[weights[i].length];
    doubleToInterp(Set.floatToDouble(value), indices, w);
    for (int i=0; i<len; i++) {
      System.arraycopy(w[i], 0, weights[i], 0, w.length);
    }
  }

  public float getLowX() {
    return (float) LowX;
  }

  public float getHiX() {
    return (float) HiX;
  }


  // Corresponding Gridded1DDoubleSet methods (double[][])

  public double[][] getDoubles() throws VisADException {
    return getDoubles(true);
  }

  public double[][] getDoubles(boolean copy) throws VisADException {
    return copy ? Set.copyDoubles(Samples) : Samples;
  }

  /** convert an array of 1-D indices to an array of values in
      R^DomainDimension */
  public double[][] indexToDouble(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear1DSet.indexToValue
      double[][] grid = new double[ManifoldDimension][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          grid[0][i] = (double) index[i];
        }
        else {
          grid[0][i] = -1;
        }
      }
      return gridToDouble(grid);
    }
    else {
      double[][] values = new double[1][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
        }
        else {
          values[0][i] = Double.NaN;
        }
      }
      return values;
    }
  }

  public int[] doubleToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException(
        "Gridded1DDoubleSet.doubleToIndex: bad dimension");
    }
    int length = value[0].length;
    int[] index = new int[length];

    double[][] grid = doubleToGrid(value);
    double[] grid0 = grid[0];
    double g;
    for (int i=0; i<length; i++) {
      g = grid0[i];
      index[i] = Double.isNaN(g) ? -1 : ((int) (g + 0.5));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public double[][] gridToDouble(double[][] grid) throws VisADException {
    if (grid.length < DomainDimension) {
      throw new SetException("Gridded1DDoubleSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DDoubleSet.gridToValue: " +
        "requires all grid dimensions to be > 1");
    }
    int length = grid[0].length;
    double[][] value = new double[1][length];
    for (int i=0; i<length; i++) {
      // let g be the current grid coordinate
      double g = grid[0][i];
      if ( (g < -0.5) || (g > LengthX-0.5) ) {
        value[0][i] = Double.NaN;
        continue;
      }
      // calculate closest integer variable
      int ig;
      if (g < 0) ig = 0;
      else if (g >= LengthX-1) ig = LengthX - 2;
      else ig = (int) g;
      double A = g - ig;  // distance variable
      // do the linear interpolation
      value[0][i] = (1-A)*Samples[0][ig] + A*Samples[0][ig+1];
    }
    return value;
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public double[][] doubleToGrid(double[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded1DDoubleSet.doubleToGrid: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DDoubleSet.valueToGrid: " +
        "requires all grid dimensions to be > 1");
    }
    double[] vals = value[0];
    int length = vals.length;
    double[] samps = Samples[0];
    double[][] grid = new double[1][length];
    int ig = (LengthX - 1)/2;
    for (int i=0; i<length; i++) {
      if (Double.isNaN(vals[i])) grid[0][i] = Double.NaN;
      else {
	int lower = 0;
	int upper = LengthX-1;
	while (lower < upper) {
	  if ((vals[i] - samps[ig]) * (vals[i] - samps[ig+1]) <= 0) break;
	  if (Ascending ? samps[ig+1] < vals[i] : samps[ig+1] > vals[i]) {
	    lower = ig+1;
          }
	  else if (Ascending ? samps[ig] > vals[i] : samps[ig] < vals[i]) {
	    upper = ig;
          }
	  if (lower < upper) ig = (lower + upper) / 2;
	}
        // Newton's method
	double solv = ig + (vals[i] - samps[ig]) / (samps[ig+1] - samps[ig]);
        if (solv >= -0.5 && solv <= LengthX - 0.5) grid[0][i] = solv;
        else {
          grid[0][i] = Double.NaN;
          // next guess should be in the middle if previous value was missing
          ig = (LengthX - 1)/2;
        }
      }
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
      throw new SetException("Gridded1DDoubleSet.doubleToInterp: " +
        "bad dimension");
    }
    int length = value[0].length; // number of values
    if (indices.length != length || weights.length != length) {
      throw new SetException("Gridded1DDoubleSet.doubleToInterp: " +
        "lengths don't match");
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

  public double getDoubleLowX() {
    return LowX;
  }

  public double getDoubleHiX() {
    return HiX;
  }


  // Miscellaneous Set methods that must be overridden
  // (this code is duplicated throughout all Gridded*DoubleSet classes)

  void init_samples(double[][] samples, boolean copy)
       throws VisADException {
    if (samples.length != DomainDimension) {
      throw new SetException("Gridded1DDoubleSet.init_samples: " +
                             "dimensions don't match");
    }
    if (Length != samples[0].length) {
      throw new SetException("Gridded1DDoubleSet.init_samples: " +
                             "lengths don't match");
    }
    // MEM
    if (copy) {
      Samples = new double[DomainDimension][Length];
    }
    else {
      Samples = samples;
    }
    if (samples[0].length != Length) {
      throw new SetException("Gridded1DDoubleSet.init_samples: " +
        "lengths don't match");
    }
    double[] samplesJ = samples[0];
    double[] SamplesJ = Samples[0];
    if (copy) {
      System.arraycopy(samplesJ, 0, SamplesJ, 0, Length);
    }
    LowX = Double.POSITIVE_INFINITY;
    HiX = Double.NEGATIVE_INFINITY;
    double sum = 0.0f;
    for (int i=0; i<Length; i++) {
      if (SamplesJ[i] != SamplesJ[i]) {
        throw new SetException("Gridded1DDoubleSet.init_samples: " +
          "sample values cannot be missing");
      }
      if (Double.isInfinite(SamplesJ[i])) {
        throw new SetException("Gridded1DDoubleSet.init_samples: " +
          "sample values cannot be infinite");
      }
      if (SamplesJ[i] < LowX) LowX = SamplesJ[i];
      if (SamplesJ[i] > HiX) HiX = SamplesJ[i];
      sum += SamplesJ[i];
    }
    if (SetErrors[0] != null) {
      SetErrors[0] =
        new ErrorEstimate(SetErrors[0].getErrorValue(), sum / Length,
                          Length, SetErrors[0].getUnit());
    }
  }

  public void cram_missing(boolean[] range_select) {
    int n = Math.min(range_select.length, Samples[0].length);
    for (int i=0; i<n; i++) {
      if (!range_select[i]) Samples[0][i] = Double.NaN;
    }
  }

  public boolean isMissing() {
    return (Samples == null);
  }

  public boolean equals(Object set) {
    if (!(set instanceof Gridded1DDoubleSet) || set == null) return false;
    if (this == set) return true;
    if (testNotEqualsCache((Set) set)) return false;
    if (testEqualsCache((Set) set)) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      int i, j;
      if (DomainDimension != ((Gridded1DDoubleSet) set).getDimension() ||
          ManifoldDimension !=
            ((Gridded1DDoubleSet) set).getManifoldDimension() ||
          Length != ((Gridded1DDoubleSet) set).getLength()) return false;
      for (j=0; j<ManifoldDimension; j++) {
        if (Lengths[j] != ((Gridded1DDoubleSet) set).getLength(j)) {
          return false;
        }
      }
      // Sets are immutable, so no need for 'synchronized'
      double[][] samples = ((Gridded1DDoubleSet) set).getDoubles(false);
      if (Samples != null) {
        for (j=0; j<DomainDimension; j++) {
          for (i=0; i<Length; i++) {
            if (Samples[j][i] != samples[j][i]) {
              addNotEqualsCache((Set) set);
              return false;
            }
          }
        }
      }
      else {
        double[][] this_samples = getDoubles(false);
        for (j=0; j<DomainDimension; j++) {
          for (i=0; i<Length; i++) {
            if (this_samples[j][i] != samples[j][i]) {
              addNotEqualsCache((Set) set);
              return false;
            }
          }
        }
      }
      addEqualsCache((Set) set);
      return true;
    }
    catch (VisADException e) {
      return false;
    }
  }

  public Object clone() {
    try {
      return new Gridded1DDoubleSet(Type, Samples, Length,
        DomainCoordinateSystem, SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Gridded1DDoubleSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded1DDoubleSet(type, Samples, Length,
      DomainCoordinateSystem, SetUnits, SetErrors);
  }

}

