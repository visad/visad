//
// SingletonSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

/**
   SingletonSet is the class for Set-s containing one member.<P>
*/
public class SingletonSet extends SampledSet {

  private RealTuple data;

  /**  
   * Construct a SingletonSet with the single sample given by a RealTuple 
   *
   * @param d  sample as a RealTuple
   * @throws  VisADException  Couldn't create the necessary VisAD object
   * @throws  RemoteException  Couldn't create the necessary remote object
   */
  public SingletonSet(RealTuple d) throws VisADException, RemoteException {
    this(d, d.getType(), null, d.getTupleUnits(), null);
  }

  /**  
   * Construct a SingletonSet with the single sample given by a RealTuple,
   * and a non-default CoordinateSystem, Units and ErrorEstimates.
   *
   * @param d  sample as a RealTuple
   * @param coord_sys  CoordinateSystem
   * @param units   Units
   * @param errors  ErrorEstimate
   *
   * @throws  VisADException  Couldn't create the necessary VisAD object
   * @throws  RemoteException  Couldn't create the necessary remote object
   */
  public SingletonSet(RealTuple d, CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors)
         throws VisADException, RemoteException {
    this(d, d.getType(), coord_sys, units, errors);
  }

  /** construct a SingletonSet with a different MathType than its
      RealTuple argument, and a non-default CoordinateSystem */
  private SingletonSet(RealTuple d, MathType type, CoordinateSystem coord_sys,
                       Unit[] units, ErrorEstimate[] errors)
          throws VisADException, RemoteException {
    super(type, 0, coord_sys, units, errors); // ManifoldDimension = 0
    int dim = d.getDimension();
    float[][] samples = new float[dim][1];
    for (int k=0; k<dim; k++) {
      samples[k][0] = (float) (((Real) d.getComponent(k)).getValue());
    }
    init_samples(samples);
    data = d;
    Length = 1;
    for (int j=0; j<DomainDimension; j++) {
      if (SetErrors[j] != null ) {
        SetErrors[j] =
          new ErrorEstimate(SetErrors[j].getErrorValue(),
                            ((Real) data.getComponent(j)).getValue(), 1,
                            SetErrors[j].getUnit());
      }
    }
  }

  /**
   * Check if the samples are missing.
   *
   * @return false by definition, a SingletonSet never has missing samples
   *               since it is initialized with a RealTuple..
   */
  public boolean isMissing() {
    return false;
  }

  /** 
   * convert an array of 1-D indices to an array of values in R^DomainDimension 
   *
   * @return float array of values
   * @throws VisADException  couldn't create the necessary VisAD object
   */
  public float[][] indexToValue(int[] index)
         throws VisADException {
    return Set.doubleToFloat(indexToDouble(index));
  }

  /** 
   * convert an array of 1-D indices to an array of doubles in R^DomainDimension
   *
   * @return double array of values
   * @throws VisADException  couldn't create the necessary VisAD object
   */
  public double[][] indexToDouble(int[] index)
         throws VisADException {
    int length = index.length;
    double[][] value = new double[DomainDimension][length];
    double[] v = new double[DomainDimension];
    for (int k=0; k<DomainDimension; k++) {
      try {
        v[k] = ((Real) data.getComponent(k)).getValue();
      }
      catch (RemoteException e) {
        v[k] = Double.NaN;
      }
    }
    for (int i=0; i<length; i++) {
      if (index[i] < 0 || index[i] >= Length) {
        for (int k=0; k<DomainDimension; k++) {
          value[k][i] = Double.NaN;
        }
      }
      else {
        for (int k=0; k<DomainDimension; k++) {
          value[k][i] = v[k];
        }
      }
    }
    return value;
  }

  /** 
   * convert an array of values in R^DomainDimension to an array of 1-D indices
   *
   * @return indices 
   * @throws VisADException  couldn't create the necessary VisAD object
   */
  public int[] valueToIndex(float[][] value) throws VisADException {
    return doubleToIndex(Set.floatToDouble(value));
  }

  /** 
   * convert an array of doubles in R^DomainDimension to an array of 1-D indices
   *
   * @return indices 
   * @throws VisADException  couldn't create the necessary VisAD object
   */
  public int[] doubleToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("SingletonSet.doubleToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length;
    int[] index = new int[length];
    for (int i=0; i<length; i++) {
      index[i] = 0;
    }
    return index;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
                            float weights[][]) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("SingletonSet.valueToInterp: bad dimension");
    }
    int length = value[0].length; // number of values
    if (indices.length != length || weights.length != length) {
      throw new SetException("SingletonSet.valueToInterp: lengths don't match");
    }
    for (int i=0; i<length; i++) {
      indices[i] = new int[1];
      weights[i] = new float[1];
      indices[i][0] = 0;
      weights[i][0] = 1.0f;
    }
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void doubleToInterp(double[][] value, int[][] indices,
                            double weights[][]) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("SingletonSet.doubleToInterp: bad dimension");
    }
    int length = value[0].length; // number of values
    if (indices.length != length || weights.length != length) {
      throw new SetException("SingletonSet.doubleToInterp: lengths don't match");
    }
    for (int i=0; i<length; i++) {
      indices[i] = new int[1];
      weights[i] = new double[1];
      indices[i][0] = 0;
      weights[i][0] = 1.0;
    }
  }

  /**
   * Get the values from the RealTuple as an array of doubles
   * @return samples as doubles.
   * @throws VisADException  couldn't create the necessary VisAD object
   */
  public double[][] getDoubles(boolean copy) throws VisADException {
    int n = getLength();
    int[] indices = new int[n];
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToDouble(indices);
  }

  /**
   * Get the RealTuple that this SingletonSet was initialized with.
   *
   * @return  RealTuple
   */
  public RealTuple getData() {
    return data;
  }

  /**
   * See if this SingletonSet is equal to the Object in question.  They
   * are equal if they are the same object and have the same Units, 
   * CoordinateSystem, dimension and their getData() returns are the equal.
   *
   * @return  true if equal, otherwise false
   */
  public boolean equals(Object set) {
    if (!(set instanceof SingletonSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    if (DomainDimension != ((SingletonSet) set).getDimension()) return false;
    if ( !data.equals( ((SingletonSet) set).getData()) ) return false;
    return true;
  }

  /**
   * Clone this SingletonSet.
   *
   * @return  clone of this set
   */
  public Object clone() {
    try {
      return new SingletonSet(data, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("SingletonSet.clone: " + e.toString());
    }
    catch (RemoteException e) {
      throw new VisADError("SingletonSet.clone: " + e.toString());
    }
  }

  /**
   * Clone this SingletonSet, but change the MathType
   *
   * @param  type  new MathType
   * 
   * @return  clone of this set with new MathType
   * @throws VisADException  couldn't create the new SingletonSet
   */
  public Object cloneButType(MathType type) throws VisADException {
    try {
      return new SingletonSet(data, type, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (RemoteException e) {
      throw new VisADError("SingletonSet.cloneButType: " + e.toString());
    }
  }

  /**
   * Create string representation of this Set with a given prefix
   *
   * @param  pre   Prefix for the string.
   * @return  String of the form pre + "SingletonSet: " + getData().toString()
   * @throws VisADException  couldn't create the longString
   */
  public String longString(String pre) throws VisADException {
    return pre + "SingletonSet: " + data;
  }
}

