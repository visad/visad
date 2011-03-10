//
// SampledSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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


import java.util.Arrays;


/**
 * SampledSet is the abstract superclass of GriddedSets, PolyCells and MultiCells.
 * SampledSet objects are intended to be immutable (but see {@link
 * #getSamples(boolean)} for an exception).
 */
public abstract class SampledSet extends SimpleSet implements SampledSetIface {

  /**           */
  static int cnt = 0;

  /**           */
  int mycnt = cnt++;

  /**           */
  private static int cacheSizeThreshold = -1;




  /**           */
  private Object cacheId;

  /**           */
  float[][] Samples;

  /**           */
  float Low[], Hi[];

  /**
   * 
   *
   * @param type 
   * @param manifold_dimension 
   *
   * @throws VisADException 
   */
  public SampledSet(MathType type, int manifold_dimension)
          throws VisADException {
    super(type, manifold_dimension);
  }

  /**
   * 
   *
   * @param type 
   * @param manifold_dimension 
   * @param coord_sys 
   * @param units 
   * @param errors 
   *
   * @throws VisADException 
   */
  public SampledSet(MathType type, int manifold_dimension,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors)
          throws VisADException {
    super(type, manifold_dimension, coord_sys, units, errors);
    Low = new float[DomainDimension];
    Hi = new float[DomainDimension];
  }

  /**
   * 
   *
   * @param type 
   *
   * @throws VisADException 
   */
  public SampledSet(MathType type) throws VisADException {
    this(type, null, null, null);
  }

  /**
   * 
   *
   * @param type 
   * @param coord_sys 
   * @param units 
   * @param errors 
   *
   * @throws VisADException 
   */
  public SampledSet(MathType type, CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors)
          throws VisADException {
    super(type, coord_sys, units, errors);
    Low = new float[DomainDimension];
    Hi = new float[DomainDimension];
  }


  /**
   * 
   *
   * @throws Throwable 
   */
  public void finalize() throws Throwable {
    if (cacheId != null) {
      // System.err.println ("sampled set finalize");
      visad.data.DataCacheManager.getCacheManager().removeFromCache(cacheId);
    }
    super.finalize();
  }


  /**
   * 
   *
   * @param threshold 
   */
  public static void setCacheSizeThreshold(int threshold) {
    cacheSizeThreshold = threshold;
  }


  /**
   * 
   *
   * @param samples 
   */
  protected void setMySamples(float[][] samples) {
    if (cacheSizeThreshold >= 0 && samples != null && samples.length > 0 &&
        samples[0].length > cacheSizeThreshold) {
      if (cacheId != null) {
        visad.data.DataCacheManager.getCacheManager().updateData(
          cacheId, samples);
      }
      else {
        cacheId =
            visad.data.DataCacheManager.getCacheManager().addToCache(getClass().getSimpleName(), samples);
      }
      //      visad.data.DataCacheManager.getCacheManager().printStats();
      return;
    }
    this.Samples = samples;
  }


  /**
   * 
   *
   * @return 
   */
  protected float[][] getMySamples() {
    if (cacheId != null) {
      return visad.data.DataCacheManager.getCacheManager().getFloatArray2D(
               cacheId);
    }
    return Samples;
  }

  /**
   * 
   *
   * @param samples 
   *
   * @throws VisADException 
   */
  void init_samples(float[][] samples) throws VisADException {
    init_samples(samples, true);
  }

  /**
   * 
   *
   * @param samples 
   * @param copy 
   *
   * @throws VisADException 
   */
  void init_samples(float[][] samples, boolean copy) throws VisADException {
    if (samples.length != DomainDimension) {
      throw new SetException("SampledSet.init_samples: " +
                             "sample dimension " + samples.length +
                             " doesn't match expected length " +
                             DomainDimension);
    }
    if (Length == 0) {
      // Length set in init_lengths, but not called for IrregularSet
      Length = samples[0].length;
    }
    else {
      if (Length != samples[0].length) {
        throw new SetException("SampledSet.init_samples: " +
                               "sample#0 length " + samples[0].length +
                               " doesn't match expected length " + Length);
      }
    }
    // MEM
    float[][] mySamples;

    if (copy) {
      mySamples = new float[DomainDimension][Length];
    }
    else {
      mySamples = samples;
    }
    for (int j = 0; j < DomainDimension; j++) {
      if (samples[j].length != Length) {
        throw new SetException("SampledSet.init_samples: " + "sample#" + j +
                               " length " + samples[j].length +
                               " doesn't match expected length " + Length);
      }
      float[] samplesJ = samples[j];
      float[] SamplesJ = mySamples[j];
      if (copy) {
        System.arraycopy(samplesJ, 0, SamplesJ, 0, Length);
      }
      Low[j] = Float.POSITIVE_INFINITY;
      Hi[j] = Float.NEGATIVE_INFINITY;
      float sum = 0.0f;
      for (int i = 0; i < Length; i++) {
/* WLH 4 May 99
        if (SamplesJ[i] != SamplesJ[i]) {
          throw new SetException(
                     "SampledSet.init_samples: sample values cannot be missing");
        }
        if (Float.isInfinite(SamplesJ[i])) {
          throw new SetException(
                     "SampledSet.init_samples: sample values cannot be infinite");
        }
        if (SamplesJ[i] < Low[j]) Low[j] = SamplesJ[i];
        if (SamplesJ[i] > Hi[j]) Hi[j] = SamplesJ[i];
*/
        if (SamplesJ[i] == SamplesJ[i] && !Float.isInfinite(SamplesJ[i])) {
          if (SamplesJ[i] < Low[j]) Low[j] = SamplesJ[i];
          if (SamplesJ[i] > Hi[j]) Hi[j] = SamplesJ[i];
        }
        else {
          SamplesJ[i] = Float.NaN;
        }
        sum += SamplesJ[i];
      }
      if (SetErrors[j] != null) {
        SetErrors[j] = new ErrorEstimate(SetErrors[j].getErrorValue(),
                                         sum / Length, Length,
                                         SetErrors[j].getUnit());
      }
    }
    setMySamples(mySamples);
  }

  /**
   * 
   *
   * @param range_select 
   */
  public void cram_missing(boolean[] range_select) {
    float[][] mySamples = getMySamples();
    int n = Math.min(range_select.length, mySamples[0].length);
    for (int i = 0; i < n; i++) {
      if (!range_select[i]) mySamples[0][i] = Float.NaN;
    }
    setMySamples(mySamples);
  }

  /**
   * 
   *
   * @param samples 
   */
  void cram_samples(float[][] samples) {
    setMySamples(samples);
  }


  /**
   * 
   *
   * @param neighbors 
   * @param weights 
   *
   * @throws VisADException 
   */
  public void getNeighbors(int[][] neighbors, float[][] weights)
          throws VisADException {
    getNeighbors(neighbors);

    int n_points;
    float distance;
    float distance_squared;
    float diff;
    float lambda_squared;
    float constant = 4f;
    float pi_squared = (float)(Math.PI * Math.PI);

    float[][] mySamples = getMySamples();
    float[][] samples = (mySamples != null)
                        ? mySamples
                        : getSamples();

    for (int ii = 0; ii < Length; ii++) {
      n_points = neighbors[ii].length;
      weights[ii] = new float[n_points];

      for (int kk = 0; kk < n_points; kk++) {
        distance_squared = 0f;
        for (int tt = 0; tt < DomainDimension; tt++) {
          diff = samples[tt][ii] - samples[tt][neighbors[ii][kk]];
          distance_squared += diff * diff;
        }
        lambda_squared = (distance_squared * constant) / pi_squared;

        weights[ii][kk] = (float)Math.exp((double)(-1f *
                (distance_squared / lambda_squared)));
      }
    }
  }

  /**
   * 
   *
   * @return 
   */
  public boolean isMissing() {
    return (getMySamples() == null);
  }

  /**
   * <p>Returns a copy of the samples of this instance.  Element <code>[i][j]
   * </code> of the returned array is the <code>j</code>-th value of the
   * <code>i</code>-th component.</p>
   *
   * <p>This method is equivalent to <code>getSamples(true)</code>.</p>
   *
   * @return                     A copy of the sample array.
   * @see #getSamples(boolean)
   *
   * @throws VisADException 
   */
  public float[][] getSamples() throws VisADException {
    return getSamples(true);
  }

  /**
   * <p>Returns the samples of this instance or a copy of the samples.</p>
   *
   * <p>Note that, if the actual sample array is returned, then it is possible
   * to modify the values of this instance -- breaking the immutability aspect
   * of this class.  Don't do this unless you enjoy debugging.</p>
   *
   * @param copy                 Whether or not a copy of the sample array
   *                             should be returned.
   * @return                     The sample array is <code>copy</code> is <code>
   *                             false; otherwise, a copy of the sample array.
   *
   * @throws VisADException 
   */
  public float[][] getSamples(boolean copy) throws VisADException {
    float[][] mySamples = getMySamples();
    return copy
           ? Set.copyFloats(mySamples)
           : mySamples;
  }

  /**
   * 
   *
   * @param type 
   * @param shadow 
   *
   * @return 
   *
   * @throws VisADException 
   */
  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
          throws VisADException {
    int n = getDimension();
    double[][] ranges = new double[2][n];
    return computeRanges(type, shadow, ranges, false);
  }

  /**
   * 
   *
   * @param type 
   * @param shadow 
   * @param ranges 
   * @param domain 
   *
   * @return 
   *
   * @throws VisADException 
   */
  public DataShadow computeRanges(ShadowType type, DataShadow shadow,
                                  double[][] ranges, boolean domain)
          throws VisADException {
    if (isMissing()) return shadow;
    setAnimationSampling(type, shadow, domain);

    int[] indices = new int[DomainDimension];
    for (int i = 0; i < DomainDimension; i++) {
      ShadowRealType real = null;
      if (type instanceof ShadowSetType) {
        real =
          (ShadowRealType)((ShadowSetType)type).getDomain().getComponent(i);
      }
      else if (type instanceof ShadowRealTupleType) {
        real = (ShadowRealType)((ShadowRealTupleType)type).getComponent(i);
      }
      else {
        throw new TypeException("SampledSet.computeRanges: bad ShadowType " +
                                type.getClass().getName());
      }
      indices[i] = real.getIndex();
    }

    for (int i = 0; i < DomainDimension; i++) {
      int k = indices[i];
      double min = Low[i];
      double max = Hi[i];
      Unit dunit = ((RealType)((SetType)Type).getDomain().getComponent(
                     i)).getDefaultUnit();
      if (dunit != null && !dunit.equals(SetUnits[i])) {
        min = dunit.toThis(min, SetUnits[i]);
        max = dunit.toThis(max, SetUnits[i]);
      }
      if (ranges != null) {
        ranges[0][i] = min;
        ranges[1][i] = max;
      }
      if (k >= 0 && k < shadow.ranges[0].length) {
        shadow.ranges[0][k] = Math.min(shadow.ranges[0][k], min);
        shadow.ranges[1][k] = Math.max(shadow.ranges[1][k], max);
      }
    }

    /* WLH 1 March 98 - moved from FieldImpl and FlatField computeRanges */
    ShadowRealTupleType domain_type = null;
    if (type instanceof ShadowRealTupleType) {
      domain_type = (ShadowRealTupleType)type;
    }
    else if (type instanceof ShadowSetType) {
      domain_type = ((ShadowSetType)type).getDomain();
    }
    if (domain_type != null && ranges != null) {
      ShadowRealTupleType shad_ref = domain_type.getReference();
      if (shad_ref != null) {
        // computeRanges for Reference (relative to domain) RealTypes
        // WLH 20 Nov 2001
        shadow = computeReferenceRanges(
          domain_type, DomainCoordinateSystem,
          ((SetType)Type).getDomain().getDefaultUnits(), shadow, shad_ref,
          ranges);
        // SetUnits, shadow, shad_ref, ranges);
      }
    }

    return shadow;
  }

  /**
   * create a 1-D GeometryArray from this Set and color_values;
   *   only used by Irregular3DSet and Gridded3DSet 
   *
   * @param color_values 
   *
   * @return 
   *
   * @throws VisADException 
   */
  public VisADGeometryArray make1DGeometry(byte[][] color_values)
          throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("SampledSet.make1DGeometry: " +
                             "DomainDimension must be 3, not " +
                             DomainDimension);
    }
    if (ManifoldDimension != 1) {
      throw new SetException("SampledSet.make1DGeometry: " +
                             "ManifoldDimension must be 1, not " +
                             ManifoldDimension);
    }
    VisADGeometryArray array = null;
    if (Length == 0) {
      return null;
    }
    else if (Length == 1) {
      array = new VisADPointArray();
    }
    else {
      array = new VisADLineStripArray();
      ((VisADLineStripArray)array).stripVertexCounts = new int[1];
      ((VisADLineStripArray)array).stripVertexCounts[0] = Length;
    }
    // set coordinates and colors
    setGeometryArray(array, 4, color_values);
    return array;
  }

  /**
   * create a 3-D GeometryArray from this Set and color_values;
   *   NOTE - this version only makes points;
   *   NOTE - when textures are supported by Java3D the Gridded3DSet
   *   implementation of make3DGeometry should use Texture3D, and
   *   the Irregular3DSet implementation should resample to a
   *   Gridded3DSet and use Texture3D;
   *   only used by Irregular3DSet and Gridded3DSet 
   *
   * @param color_values 
   *
   * @return 
   *
   * @throws VisADException 
   */
  public VisADGeometryArray[] make3DGeometry(byte[][] color_values)
          throws VisADException {
    if (ManifoldDimension != 3) {
      throw new SetException("SampledSet.make3DGeometry: " +
                             "ManifoldDimension must be 3, not " +
                             ManifoldDimension);
    }
    VisADGeometryArray array = makePointGeometry(color_values);
    return new VisADGeometryArray[] {array, array, array};
  }

  /**
   * create a PointArray from this Set and color_values;
   *   can be applied to  ManifoldDimension = 1, 2 or 3 
   *
   * @param color_values 
   *
   * @return 
   *
   * @throws VisADException 
   */
  public VisADGeometryArray makePointGeometry(byte[][] color_values)
          throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("SampledSet.makePointGeometry: " +
                             "DomainDimension must be 3, not " +
                             DomainDimension);
    }
    VisADPointArray array = new VisADPointArray();
    // set coordinates and colors
    setGeometryArray(array, 4, color_values);
    return array;
  }

  /**
   * copy and transpose Samples (from this Set( and color_values
   *   into array; if color_length == 3 don't use color_values[3] 
   *
   * @param array 
   * @param color_length 
   * @param color_values 
   *
   * @throws VisADException 
   */
  public void setGeometryArray(VisADGeometryArray array, int color_length,
                               byte[][] color_values)
          throws VisADException {
    setGeometryArray(array, getSamples(false), color_length, color_values);
  }

  /**
   * copy and transpose samples and color_values into array;
   *   if color_length == 3 don't use color_values[3] 
   *
   * @param array 
   * @param samples 
   * @param color_length 
   * @param color_values 
   *
   * @throws VisADException 
   */
  public static void setGeometryArray(VisADGeometryArray array,
                                      float[][] samples, int color_length,
                                      byte[][] color_values)
          throws VisADException {
    if (samples == null) {
      throw new SetException("SampledSet.setGeometryArray: " +
                             "Null samples array");
    }
    else if (samples.length != 3) {
      throw new SetException("SampledSet.setGeometryArray: " +
                             "Expected 3 dimensions in samples array, not " +
                             samples.length);
    }
    int len = samples[0].length;
    array.vertexCount = len;
    // MEM
    float[] coordinates = new float[3 * len];
    int j = 0;
    for (int i = 0; i < len; i++) {
      coordinates[j++] = samples[0][i];
      coordinates[j++] = samples[1][i];
      coordinates[j++] = samples[2][i];
    }

    array.coordinates = coordinates;
    if (color_values != null) {
      color_length = Math.min(color_length, color_values.length);
      // MEM
      byte[] colors = new byte[color_length * len];
      j = 0;
      if (color_length == 4) {
        for (int i = 0; i < len; i++) {
          colors[j++] = color_values[0][i];
          colors[j++] = color_values[1][i];
          colors[j++] = color_values[2][i];
          colors[j++] = color_values[3][i];
          // colors[j++] = (1.0f - color_values[3][i]);
        }
      }
      else if (color_length == 3) {
        for (int i = 0; i < len; i++) {
          colors[j++] = color_values[0][i];
          colors[j++] = color_values[1][i];
          colors[j++] = color_values[2][i];
        }
      }

      // BMF 2009-03-17
      // this addresses a issue where a 2D display is used without color
      // mapping
      else if (color_length == 0) {
        colors = null;
      }
      else {
        throw new SetException("SampledSet.setGeometryArray: " +
                               "color_length must be 0, 3 or 4, not " +
                               color_length);
      }

      array.colors = colors;
    }
  }

  /**
   * 
   *
   * @return 
   */
  public float[] getLow() {
    float[] low = new float[Low.length];
    for (int i = 0; i < Low.length; i++)
      low[i] = Low[i];
    return low;
  }

  /**
   * 
   *
   * @return 
   */
  public float[] getHi() {
    float[] hi = new float[Hi.length];
    for (int i = 0; i < Hi.length; i++)
      hi[i] = Hi[i];
    return hi;
  }

  /**
   * Clones this instance.
   *
   * @return                    A clone of this instance.
   */
  public Object clone() {
    SampledSet clone = (SampledSet)super.clone();
    if (clone.cacheId != null) {
      clone.cacheId = null;
    }

    /*
     * The array of sample values is cloned because getSamples(false) allows
     * clients to modify the values and the clone() general contract forbids
     * cross-clone effects.
     */
    float[][] mySamples = getMySamples();
    if (mySamples != null) {
      clone.setMySamples(visad.util.Util.clone(mySamples));
    }
    return clone;
  }
}

