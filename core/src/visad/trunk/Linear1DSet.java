//
// Linear1DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
       implements LinearSet {

  private double First, Last, Step, Invstep;

  /** a 1-D arithmetic progression with null errors and generic type */
  public Linear1DSet(double first, double last, int length)
         throws VisADException {
    this(RealType.Generic, first, last, length, null, null, null);
  }

  public Linear1DSet(MathType type, double first, double last, int length)
         throws VisADException {
    this(type, first, last, length, null, null, null);
  }

  /** an arithmetic progression of length values between first and last;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Linear1DSet(MathType type, double first, double last, int length,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    super(type, (float[][]) null, length, coord_sys, units, errors);
    if (DomainDimension != 1) {
      throw new SetException("Linear1DSet: DomainDimension must be 1, not " +
                             DomainDimension);
    }
    First = first;
    Last = last;
    Length = length;
    if (Length < 1) throw new SetException("Linear1DSet: number of samples (" +
                                           Length + " must be greater than 1");
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
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
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
    if (Lengths[0] < 2) {
      throw new SetException("Linear1DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
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

  /** transform an array of values in R to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length != 1) {
      throw new SetException("Linear1DSet.valueToGrid: value dimension" +
                             " should be 1, not " + value.length);
    }
    if (Lengths[0] < 2) {
      throw new SetException("Linear1DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
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

  public double getFirst() {
    return First;
  }

  public double getLast() {
    return Last;
  }

  public double getStep() {
    return Step;
  }

  public double getInvstep() {
    return Invstep;
  }

  public boolean isMissing() {
    return false;
  }

  public double[][] getDoubles(boolean copy) throws VisADException {
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToDouble(indices);
  }

  public float[][] getSamples(boolean copy) throws VisADException {
    int n = getLength();
    int[] indices = new int[n];
    // do NOT call getWedge
    for (int i=0; i<n; i++) indices[i] = i;
    return indexToValue(indices);
  }

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
   * @return			The hash code for this instance.
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

  public Object clone() {
    try {
      return new Linear1DSet(Type, First, Last, Length, DomainCoordinateSystem,
                             SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Linear1DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Linear1DSet(type, First, Last, Length, DomainCoordinateSystem,
                           SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    return pre + "Linear1DSet: Length = " + Length +
           " Range = " + First + " to " + Last + "\n";
  }

}

