
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

/**
   SampledSet is the abstract superclass of GriddedSets, PolyCells and MultiCells.
   SampledSet objects are immutable.<P>
*/
public abstract class SampledSet extends SimpleSet {

  double[][] Samples;
  double Low[], Hi[];

  public SampledSet(MathType type, int manifold_dimension) throws VisADException {
    super(type, manifold_dimension);
  }

  public SampledSet(MathType type, int manifold_dimension,
                   CoordinateSystem coord_sys, Unit[] units,
                   ErrorEstimate[] errors)
         throws VisADException {
    super(type, manifold_dimension, coord_sys, units, errors);
  }

  public SampledSet(MathType type) throws VisADException {
    super(type);
  }

  public SampledSet(MathType type, CoordinateSystem coord_sys, Unit[] units,
                   ErrorEstimate[] errors) throws VisADException {
    super(type, coord_sys, units, errors);
  }

  void init_samples(double[][] samples) throws VisADException {
    if (samples.length != DomainDimension) {
      throw new SetException("GriddedSet: dimensions don't match");
    }
    if (this instanceof IrregularSet) {
      Length = samples[0].length;    // fixes an odd problem
    }
    Samples = new double[DomainDimension][Length];
    for (int j=0; j<ManifoldDimension; j++) {
      if (samples[j].length != Length) {
        throw new SetException("GriddedSet: lengths don't match");
      }
      double[] samplesJ = samples[j];
      double[] SamplesJ = Samples[j];
      System.arraycopy(samplesJ, 0, SamplesJ, 0, Length);
      Low[j] = Double.POSITIVE_INFINITY;
      Hi[j] = Double.NEGATIVE_INFINITY;
      double sum = 0.0;
      for (int i=0; i<Length; i++) {
        if (Double.isNaN(SamplesJ[i])) {
          throw new SetException(
                     "GriddedSet: sample values cannot be missing");
        }
        if (Double.isInfinite(SamplesJ[i])) {
          throw new SetException(
                     "GriddedSet: sample values cannot be infinite");
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

  double[][] getSamples() {
    return Samples;
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
      ShadowRealType real =
        (ShadowRealType) ((ShadowSetType) type).getDomain().getComponent(i);
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

}

