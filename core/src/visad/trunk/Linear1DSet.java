
//
// Linear1DSet.java
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
   Linear1DSet represents a finite set of samples of R in
   an arithmetic progression.<P>

   The samples are ordered from First to Last.<P>
*/
public class Linear1DSet extends Gridded1DSet {

  private double First, Last, Step, Invstep;

  public Linear1DSet(MathType type, double first, double last, int length)
         throws VisADException {
    this(type, first, last, length, null, null, null);
  }

  public Linear1DSet(MathType type, double first, double last, int length,
                     CoordinateSystem coord_sys, Unit[] units,
                     ErrorEstimate[] errors) throws VisADException {
    super(type, (double[][]) null, length, coord_sys, units, errors);
    if (DomainDimension != 1) {
      throw new SetException("Linear1DSet: DomainDimension must be 1");
    }
    First = first;
    Last = last;
    Length = length;
    if (Length < 1) throw new SetException("Linear1DSet: bad # samples");
    Step = (Length < 2) ? 1.0 : (Last - First) / (Length - 1);
    Invstep = 1.0 / Step; 
    LowX = Math.min(First, First + Step * (Length - 1));
    HiX = Math.max(First, First + Step * (Length - 1));
    Low[0] = LowX;
    Hi[0] = HiX;
    if (SetErrors[0] != null ) {
      SetErrors[0] =
        new ErrorEstimate(SetErrors[0].getErrorValue(), (Low[0] + Hi[0]) / 2.0,
                          Length, SetErrors[0].getUnit());
    }
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    double[][] values = new double[1][length];
    for (int i=0; i<length; i++) {
      if (0 <= index[i] && index[i] < Length) {
        values[0][i] = First + ((double) index[i]) * Step;
      }
      else {
        values[0][i] = Double.NaN;
      }
    }
    return values;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R */
  public double[][] gridToValue(double[][] grid) throws VisADException {
    if (grid.length != 1) {
      throw new SetException("Linear1DSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Linear1DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    double[][] value = new double[1][length];
    double[] value0 = value[0];
    double[] grid0 = grid[0];
    double l = -0.5;
    double h = ((double) Length) - 0.5;
    double g;

    for (int i=0; i<length; i++) {
      g = grid0[i];
      value0[i] = (l < g && g < h) ? First + g * Step : Double.NaN;
    }
    return value;
  }

  /** transform an array of values in R to an array
      of non-integer grid coordinates */
  public double[][] valueToGrid(double[][] value) throws VisADException {
    if (value.length != 1) {
      throw new SetException("Linear1DSet.valueToGrid: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Linear1DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = value[0].length;
    double[][] grid = new double[1][length];
    double[] grid0 = grid[0];
    double[] value0 = value[0];
    double l = First - 0.5 * Step;
    double h = First + (((double) Length) - 0.5) * Step;
    double v;

    if (h < l) {
      double temp = l;
      l = h;
      h = temp;
    }
    for (int i=0; i<length; i++) {
      v = value0[i];
      grid0[i] = (l < v && v < h) ? (v - First) * Invstep : Double.NaN;
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

  public boolean isLinearSet() {
    return true;
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

