
//
// SingletonSet.java
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
   SingletonSet is the class for Set-s containing one member.<P>
*/
public class SingletonSet extends SimpleSet {

  private RealTuple data;

  /** construct a SingletonSet with the single sample given by a RealTuple */
  public SingletonSet(RealTuple d) throws VisADException {
    this(d, d.getType(), null, null, null);
  }

  /** construct a SingletonSet with the single sample given by a RealTuple,
      and a non-default CoordinateSystem */
  public SingletonSet(RealTuple d, CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(d, d.getType(), coord_sys, units, errors);
  }

  /** construct a SingletonSet with a different MathType than its
      RealTuple argument, and a non-default CoordinateSystem */
  private SingletonSet(RealTuple d, MathType type, CoordinateSystem coord_sys,
                       Unit[] units, ErrorEstimate[] errors)
          throws VisADException {
    super(type, coord_sys, units, errors);
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

  public boolean isMissing() {
    return false;
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    float[][] value = new float[DomainDimension][length];
    float[] v = new float[DomainDimension];
    for (int k=0; k<DomainDimension; k++) {
      v[k] = (float) (((Real) data.getComponent(k)).getValue());
    }
    for (int i=0; i<length; i++) {
      if (index[i] < 0 || index[i] >= Length) {
        for (int k=0; k<DomainDimension; k++) {
          value[k][i] = Float.NaN;
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

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("SingletonSet.valueToIndex: bad dimension");
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

  public RealTuple getData() {
    return data;
  }

  public boolean equals(Object set) {
    if (!(set instanceof SingletonSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      if (DomainDimension != ((SingletonSet) set).getDimension()) return false;
      for (int i=0; i<DomainDimension; i++) {
        if (((Real) data.getComponent(i)).getValue() !=
            ((Real) ((SingletonSet) set).getData().getComponent(i)).getValue()) {
          return false;
        }
      }
      return true;
    }
    catch (VisADException e) {
      return false;
    }
  }

  public Object clone() {
    try {
      return new SingletonSet(data, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("SingletonSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new SingletonSet(data, type, DomainCoordinateSystem,
                            SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    return pre + "SingletonSet: " + data;
  }
}

