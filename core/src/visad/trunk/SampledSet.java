
//
// SampledSet.java
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

import javax.media.j3d.*;
import java.vecmath.*;

/**
   SampledSet is the abstract superclass of GriddedSets, PolyCells and MultiCells.
   SampledSet objects are immutable.<P>
*/
public abstract class SampledSet extends SimpleSet {

  float[][] Samples;
  float Low[], Hi[];

  public SampledSet(MathType type, int manifold_dimension) throws VisADException {
    super(type, manifold_dimension);
  }

  public SampledSet(MathType type, int manifold_dimension,
                   CoordinateSystem coord_sys, Unit[] units,
                   ErrorEstimate[] errors)
         throws VisADException {
    super(type, manifold_dimension, coord_sys, units, errors);
    Low = new float[DomainDimension];
    Hi = new float[DomainDimension];
  }

  public SampledSet(MathType type) throws VisADException {
    this(type, null, null, null);
  }

  public SampledSet(MathType type, CoordinateSystem coord_sys, Unit[] units,
                   ErrorEstimate[] errors) throws VisADException {
    super(type, coord_sys, units, errors);
    Low = new float[DomainDimension];
    Hi = new float[DomainDimension];
  }

  void init_samples(float[][] samples) throws VisADException {
    init_samples(samples, true);
  }

  void init_samples(float[][] samples, boolean copy)
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
      Samples = new float[DomainDimension][Length];
    }
    else {
      Samples = samples;
    }
    for (int j=0; j<DomainDimension; j++) {
      if (samples[j].length != Length) {
        throw new SetException("SampledSet.init_samples: lengths don't match");
      }
      float[] samplesJ = samples[j];
      float[] SamplesJ = Samples[j];
      if (copy) {
        System.arraycopy(samplesJ, 0, SamplesJ, 0, Length);
      }
      Low[j] = Float.POSITIVE_INFINITY;
      Hi[j] = Float.NEGATIVE_INFINITY;
      float sum = 0.0f;
      for (int i=0; i<Length; i++) {
/* WLH 24 Oct 97
        if (Double.isNaN(SamplesJ[i])) {
*/
        if (SamplesJ[i] != SamplesJ[i]) {
          throw new SetException(
                     "SampledSet.init_samples: sample values cannot be missing");
        }
        if (Double.isInfinite(SamplesJ[i])) {
          throw new SetException(
                     "SampledSet.init_samples: sample values cannot be infinite");
        }
        if (SamplesJ[i] < Low[j]) Low[j] = SamplesJ[i];
        if (SamplesJ[i] > Hi[j]) Hi[j] = SamplesJ[i];
        sum += SamplesJ[i];
      }
      if (SetErrors[j] != null ) {
        SetErrors[j] =
          new ErrorEstimate(SetErrors[j].getErrorValue(), sum / Length,
                            Length, SetErrors[j].getUnit());
      }
    }
  }

  public boolean isMissing() {
    return (Samples == null);
  }

  float[][] getSamples() throws VisADException {
    return getSamples(true);
  }

  float[][] getSamples(boolean copy) throws VisADException {
    if (copy) {
      // MEM
      float[][] samples = new float[DomainDimension][Length];
      for (int j=0; j<DomainDimension; j++) {
        System.arraycopy(Samples[j], 0, samples[j], 0, Length);
      }
      return samples;
    }
    else {
      return Samples;
    }
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException {
    return computeRanges(type, shadow, null, false);
  }

  public DataShadow computeRanges(ShadowType type, DataShadow shadow,
                                  double[][] ranges, boolean domain)
         throws VisADException {
    if (isMissing()) return shadow;
    setAnimationSampling(type, shadow, domain);

    int[] indices = new int[DomainDimension];
    for (int i=0; i<DomainDimension; i++) {
      ShadowRealType real = null;
      if (type instanceof ShadowSetType) {
        real = (ShadowRealType) ((ShadowSetType) type).getDomain().getComponent(i);
      }
      else if(type instanceof ShadowRealTupleType) {
        real = (ShadowRealType) ((ShadowRealTupleType) type).getComponent(i);
      }
      else {
        throw new TypeException("SampledSet.computeRanges: bad ShadowType");
      }
      indices[i] = real.getIndex();
    }
 
    for (int i=0; i<DomainDimension; i++) {
      int k = indices[i];
      if (k >= 0) {
        double min = Low[i];
        double max = Hi[i];
        Unit dunit =
          ((RealType) ((SetType) Type).getDomain().getComponent(i)).
            getDefaultUnit();
        if (dunit != null && !dunit.equals(SetUnits[i])) {
          min = dunit.toThis(min, SetUnits[i]);
          max = dunit.toThis(max, SetUnits[i]);
        }
        if (ranges != null) {
          ranges[0][i] = min;
          ranges[1][i] = max;
        }
        shadow.ranges[0][k] = Math.min(shadow.ranges[0][k], min);
        shadow.ranges[1][k] = Math.max(shadow.ranges[1][k], max);
      }
    }
    return shadow;
  }

  /** create a 1-D GeometryArray from this Set and color_values;
      only used by Irregular3DSet and Gridded3DSet */
  public VisADGeometryArray make1DGeometry(float[][] color_values)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("SampledSet.make1DGeometry: " +
                             "DomainDimension must be 3");
    }
    if (ManifoldDimension != 1) {
      throw new SetException("SampledSet.make1DGeometry: " +
                             "ManifoldDimension must be 1");
    }
    VisADLineStripArray array = new VisADLineStripArray();
    array.stripVertexCounts = new int[1];
    array.stripVertexCounts[0] = Length;
    // set coordinates and colors
    setGeometryArray(array, 3, color_values);
    return array;
  }

  /** create a 3-D GeometryArray from this Set and color_values;
      NOTE - this version only makes points;
      NOTE - when textures are supported by Java3D the Gridded3DSet
      implementation of make3DGeometry should use Texture3D, and
      the Irregular3DSet implementation should resample to a
      Gridded3DSet and use Texture3D;
      only used by Irregular3DSet and Gridded3DSet */
  public VisADGeometryArray make3DGeometry(float[][] color_values)
         throws VisADException {
    if (ManifoldDimension != 1) {
      throw new SetException("SampledSet.make1DGeometry: " +
                             "ManifoldDimension must be 1");
    }
    return makePointGeometry(color_values);
  }

  /** create a PointArray from this Set and color_values;
      can be applied to  ManifoldDimension = 1, 2 or 3 */
  public VisADGeometryArray makePointGeometry(float[][] color_values)
         throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("SampledSet.makePointGeometry: " +
                             "DomainDimension must be 3");
    }
    VisADPointArray array = new VisADPointArray();
    // set coordinates and colors
    setGeometryArray(array, 3, color_values);
    return array;
  }

  /** copy and transpose Samples (from this Set( and color_values
      into array; if color_length == 3 don't use color_values[3] */
  void setGeometryArray(VisADGeometryArray array, int color_length,
                        float[][] color_values) throws VisADException {
    setGeometryArray(array, getSamples(false), color_length, color_values);
  }

  /** copy and transpose samples and color_values into array;
      if color_length == 3 don't use color_values[3] */
  static void setGeometryArray(VisADGeometryArray array, float[][] samples,
                               int color_length, float[][] color_values)
       throws VisADException {
    if (samples == null || samples.length != 3) {
      throw new SetException("SampledSet.setGeometryArray: " +
                             "bad samples array");
    }
    int len = samples[0].length;
    array.vertexCount = len;
    // MEM
    float[] coordinates = new float[3 * len];
    int j = 0;
    for (int i=0; i<len; i++) {
      coordinates[j++] = samples[0][i];
      coordinates[j++] = samples[1][i];
      coordinates[j++] = samples[2][i];
    }
    array.coordinates = coordinates;
    array.vertexFormat |= GeometryArray.COORDINATES;
    if (color_values != null) {
      color_length = Math.min(color_length, color_values.length);
      // MEM
      float[] colors = new float[color_length * len];
      j = 0;
      if (color_length == 4) {
        for (int i=0; i<len; i++) {
          colors[j++] = color_values[0][i];
          colors[j++] = color_values[1][i];
          colors[j++] = color_values[2][i];
          colors[j++] = (1.0f - color_values[3][i]);
        }
System.out.println("COLOR_4 " + color_values[3][0] + " " + color_values[3][1] +
                          " " + color_values[3][2] + " " + color_values[3][3]);
        array.vertexFormat |= GeometryArray.COLOR_4;
      }
      else if (color_length == 3) {
        for (int i=0; i<len; i++) {
          colors[j++] = color_values[0][i];
          colors[j++] = color_values[1][i];
          colors[j++] = color_values[2][i];
        }
        array.vertexFormat |= GeometryArray.COLOR_3;
      }
      else {
        throw new SetException("SampledSet.setGeometryArray: " +
                                "color_length must be 3 or 4");
      }
      array.colors = colors;
    }
  }

}

