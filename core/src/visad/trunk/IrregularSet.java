
//
// IrregularSet.java
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
   IrregularSet is implemented by those Set sub-classes whose samples
   do not form any ordered pattern.  It is a M-dimensional array of
   points in R^N where ManifoldDimension = M <= N = DomainDimension.<P> 

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>
*/
public class IrregularSet extends SampledSet {

  /** construct an IrregularSet with samples */
  public IrregularSet(MathType type, double[][] samples)
         throws VisADException {
    this(type, samples, null, null, null);
  }

  /** construct an IrregularSet with samples and non-default CoordinateSystem */
  public IrregularSet(MathType type, double[][] samples,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors) throws VisADException {
    super(type, samples.length, coord_sys, units, errors);
    Low = new double[DomainDimension];
    Hi = new double[DomainDimension];
    if (samples == null ) {
      Samples = null;
    }
    else {
      init_samples(samples);
    }
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    double[][] value = new double[DomainDimension][index.length];
    for (int i=0; i<index.length; i++)
      if ( (index[i] >= 0) && (index[i] < Samples[0].length) )
        for (int j=0; j<DomainDimension; j++)
          value[j][i] = Samples[j][index[i]];
      else
        for (int j=0; j<DomainDimension; j++)
          value[j][i] = Double.NaN;
    return value;
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(double[][] value) throws VisADException {
    throw new UnimplementedException("IrregularSet.valueToIndex");
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if no interpolation is possible */
  public void valueToInterp(double[][] value, int[][] indices,
                            double weights[][]) throws VisADException {
    throw new UnimplementedException("IrregularSet.valueToInterp");
  }

  public boolean equals(Object set) {
    if (!(set instanceof IrregularSet) || set == null ||
        ((IrregularSet) set).isLinearSet()) return false;
    if (this == set) return true;
    if (testNotEqualsCache((Set) set)) return false;
    if (testEqualsCache((Set) set)) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      int i, j;
      if (DomainDimension != ((IrregularSet) set).getDimension() ||
          ManifoldDimension != ((IrregularSet) set).getManifoldDimension() ||
          Length != ((IrregularSet) set).getLength()) return false;
      // Sets are immutable, so no need for 'synchronized'
      double[][] samples = ((IrregularSet) set).getSamples();
      for (j=0; j<DomainDimension; j++) {
        for (i=0; i<Length; i++) {
          if (Samples[j][i] != samples[j][i]) {
            addNotEqualsCache((Set) set);
            return false;
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
      return new IrregularSet(Type, Samples, DomainCoordinateSystem,
                            SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("IrregularSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new IrregularSet(type, Samples, DomainCoordinateSystem,
                          SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s;
    int j;
    if (DomainDimension == ManifoldDimension) {
      s = pre + getClass().getName() + ": Dimension = " +
          DomainDimension + " Length = " + Length + "\n";
      for (j=0; j<DomainDimension; j++) {
        s = s + pre + "  Dimension " + j + ":" +
                " Range = " + Low[j] + " to " + Hi[j] + "\n";
      }
    }
    else {
      s = pre + getClass().getName() + ": DomainDimension = " +
          DomainDimension + " ManifoldDimension = " + ManifoldDimension +
          " Length = " + Length + "\n";
      for (j=0; j<DomainDimension; j++) {
        s = s + pre + "  DomainDimension " + j + ":" + " Range = " +
            Low[j] + " to " + Hi[j] + "\n";
      }
    }
    return s;
  }

}

