 
//
// Gridded1DDoubleSet.java
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

import java.io.*;

/**
   Gridded1DDoubleSet represents a finite set of samples of R.<P>
*/
public class Gridded1DDoubleSet extends GriddedSet {

  int LengthX;
  double LowX, HiX;
  double[][] Samples;

  /** Whether this set is ascending or descending */
  boolean Ascending;

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

  /** a 1-D sorted sequence with no regular interval; samples array
      is organized float[1][number_of_samples] where lengthX =
      number_of_samples; samples must be sorted (either increasing
      or decreasing); coordinate_system and units must be compatible
      with defaults for type, or may be null; errors may be null */
  Gridded1DDoubleSet(MathType type, double[][] samples, int lengthX,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    // super(type, Set.doubleToFloat(samples), make_lengths(lengthX),
    //       coord_sys, units, errors, copy);
    super(type, null, make_lengths(lengthX), coord_sys, units, errors, copy);
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
      else { // !Pos
        for (int i=1; i<LengthX; i++) {
          if (Samples[0][i] > Samples[0][i-1]) {
            throw new SetException(
             "Gridded1DDoubleSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
    }
  }

  void init_samples(double[][] samples, boolean copy)
       throws VisADException {
    if (samples.length != DomainDimension) {
      throw new SetException("SampledSet.init_samples: " +
                             "dimensions don't match");
    }
    if (Length == 0) {
      // Length set in init_lengths, but not called for IrregularSet
      Length = samples[0].length;
    }
    else {
      if (Length != samples[0].length) {
        throw new SetException("SampledSet.init_samples: " +
                               "lengths don't match");
      }
    }
    // MEM
    if (copy) {
      Samples = new double[DomainDimension][Length];
    }
    else {
      Samples = samples;
    }
    if (samples[0].length != Length) {
      throw new SetException("SampledSet.init_samples: lengths don't match");
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
        throw new SetException(
                   "SampledSet.init_samples: sample values cannot be missing");
      }
      if (Double.isInfinite(SamplesJ[i])) {
        throw new SetException(
                   "SampledSet.init_samples: sample values cannot be infinite");
      }
      if (SamplesJ[i] < LowX) LowX = SamplesJ[i];
      if (SamplesJ[i] > HiX) HiX = SamplesJ[i];
      sum += SamplesJ[i];
    }
    if (SetErrors[0] != null ) {
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

  public float[][] getSamples(boolean copy) throws VisADException {
    return Set.doubleToFloat(Samples);
  }

  public double[][] getDoubles(boolean copy) throws VisADException {
    return Samples;
  }

  static int[] make_lengths(int lengthX) {
    int[] lens = new int[1];
    lens[0] = lengthX;
    return lens;
  }

  public boolean isMissing() {
    return (Samples == null);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
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

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    return Set.doubleToFloat(indexToDouble(index));
  }

  public int[] doubleToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded1DDoubleSet.doubleToIndex: bad dimension");
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

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public double[][] gridToDouble(double[][] grid) throws VisADException {
    if (grid.length < DomainDimension) {
      throw new SetException("Gridded1DDoubleSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DDoubleSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
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

  public float[][] floatToGrid(float[][] value) throws VisADException {
    return Set.doubleToFloat(doubleToGrid(Set.floatToDouble(value)));
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public double[][] doubleToGrid(double[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded1DDoubleSet.doubleToGrid: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DDoubleSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
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

  public float getLowX() {
    return (float) LowX;
  }

  public float getHiX() {
    return (float) HiX;
  }

  public Object clone() {
    try {
      return new Gridded1DDoubleSet(Type, Samples, Length, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Gridded1DDoubleSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded1DDoubleSet(type, Samples, Length, DomainCoordinateSystem,
                                  SetUnits, SetErrors);
  }

}

