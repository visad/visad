
//
// LinearLatLonSet.java
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

/**
   LinearLatLonSet represents a finite set of samples of
   (Latitude, Longitude) in a cross product of two
   arithmetic progressions.<P>

   This class exists to override valueToInterp (as defined in GriddedSet)
   in order to handle Longitude wrapping.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>
*/
public class LinearLatLonSet extends Linear2DSet {

  private boolean LongitudeWrap;
  private double WrapStep, WrapFactor;

  public LinearLatLonSet(MathType type, Linear1DSet[] sets) throws VisADException {
    this(type, sets, null, null, null);
  }

  public LinearLatLonSet(MathType type, double first1, double last1, int length1,
                                        double first2, double last2, int length2)
         throws VisADException {
    this(type, first1, last1, length1, first2, last2, length2, null, null, null);
  }

  public LinearLatLonSet(MathType type, Linear1DSet[] sets,
                         CoordinateSystem coord_sys, Unit[] units,
                         ErrorEstimate[] errors) throws VisADException {
    super(type, sets, coord_sys, units, errors);
    checkWrap();
  }

  public LinearLatLonSet(MathType type, double first1, double last1, int length1,
                         double first2, double last2, int length2,
                         CoordinateSystem coord_sys, Unit[] units,
                         ErrorEstimate[] errors) throws VisADException {
    super(type, first1, last1, length1, first2, last2, length2, coord_sys,
          units, errors);
    checkWrap();
  }

  void checkWrap() throws VisADException {
    if (Low[0] < -0.5 * Math.PI || Hi[0] > 0.5 * Math.PI ||
        Low[1] < -2.0 * Math.PI || Hi[1] > 2.0 * Math.PI ||
        (Hi[1] - Low[1]) > 2.0 * Math.PI) {
      throw new SetException("LinearLatLonSet: out of bounds " +
                             "(note Lat and Lon in Radians)");
    }
    LongitudeWrap =
        (Hi[1] - Low[1]) + 2.0 * Math.abs(Y.getStep()) >= 2.0 * Math.PI &&
        Y.getLength() > 1;
    if (LongitudeWrap) {
      WrapStep = 2.0 * Math.PI - (Hi[1] - Low[1]);
      WrapFactor = Math.abs(Y.getStep()) / WrapStep;
    }
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in (Latitude, Longitude) */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != 2) {
      throw new SetException("LinearLatLonSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2) {
      throw new SetException("LinearLatLonSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    float[][] gridX = new float[1][];
    gridX[0] = grid[0];
    float[][] gridY = new float[1][];
    gridY[0] = grid[1];
    if (LongitudeWrap) {
      float len = (float) Y.getLength();
      float lenh = len - 0.5f;
      float lenm = len - 1.0f;
      for (int i=0; i<length; i++) {
        if (gridY[0][i] > lenm) {
          gridY[0][i] = (float) (lenm + (gridY[0][i] - lenm) / WrapFactor);
          if (gridY[0][i] > lenh) gridY[0][i] -= len;
        }
        else if (gridY[0][i] < 0.0) {
          gridY[0][i] = (float) (gridY[0][i] / WrapFactor);
          if (gridY[0][i] < -0.5) gridY[0][i] += len;
        }
      }
    }
    float[][] valueX = X.gridToValue(gridX);
    float[][] valueY = Y.gridToValue(gridY);
    float[][] value = new float[2][];
    value[0] = valueX[0];
    value[1] = valueY[0];
    return value;
  }

  /** transform an array of values in (Latitude, Longitude)
      to an array of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length != 2) {
      throw new SetException("LinearLatLonSet.valueToGrid: bad dimension");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2) {
      throw new SetException("LinearLatLonSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = value[0].length;
    float[][] valueX = new float[1][];
    valueX[0] = value[0];
    float[] valueY = value[1];
    float[][] gridX = X.valueToGrid(valueX); // Latitude-s

    float[] gridY = new float[length];
    float l = (float) (Y.getFirst() - 0.5 * Y.getStep());
    float h = (float) (Y.getFirst() + (((float) Y.getLength()) - 0.5) * Y.getStep());
    if (h < l) {
      float temp = l;
      l = h;
      h = temp;
    }
    if (LongitudeWrap) {
      l = (float) (l + 0.5 * Math.abs(Y.getStep()) - 0.5 * WrapStep);
      h = (float) (l + 2.0 * Math.PI);
    }
    for (int i=0; i<length; i++) {
      float v = (float) (valueY[i] % (2.0 * Math.PI));
      if (v <= l ) v += 2.0 * Math.PI;
      else if (h <= v) v -= 2.0 * Math.PI;
      gridY[i] = (float)
        ((l < v && v < h) ? (v - Y.getFirst()) * Y.getInvstep() : Double.NaN);
    }
    if (LongitudeWrap) {
      float len = (float) Y.getLength();
      float lenh = len - 0.5f;
      float lenm = len - 1.0f;
      for (int i=0; i<length; i++) {
        if (gridY[i] > lenm) {
          gridY[i] = (float) (lenm + (gridY[i] - lenm) * WrapFactor);
          if (gridY[i] > lenh) gridY[i] -= len;
        }
        else if (gridY[i] < 0.0) {
          gridY[i] = (float) (gridY[i] * WrapFactor);
          if (gridY[i] < -0.5) gridY[i] += len;
        }
      }
    }
    float[][] grid = new float[2][];
    grid[0] = gridX[0];
    grid[1] = gridY;
    return grid;
  }

  /** for each of an array of values in (Latitude, Longitude), compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible).
      this code is the result of substituting 2 for ManifoldDimension in
      GriddedSet.valueToInterp, and adding logic to handle LngitudeWrap */
  public void valueToInterp(float[][] value, int[][] indices, float weights[][])
              throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("LinearLatLonSet.valueToInterp: bad dimension");
    }
    int length = value[0].length; // number of values
    if (indices.length != length || weights.length != length) {
      throw new SetException("LinearLatLonSet.valueToInterp: lengths don't match");
    }
    float[][] grid = valueToGrid(value); // convert value array to grid coord array
    /* if LongitudeWrap, grid[1][i] should be between -0.5 and Y.Length-0.5 */

    int i, j, k; // loop indices
    int lis; // temporary length of is & cs
    int length_is; // final length of is & cs, varies by i
    int isoff; // offset along one grid dimension
    float a, b; // weights along one grid dimension; a + b = 1.0
    int[] is; // array of indices, becomes part of indices
    float[] cs; // array of coefficients, become part of weights

    int base; // base index, as would be returned by valueToIndex
    int[] l = new int[2]; // integer 'factors' of base
    float[] c = new float[2]; // fractions with l; -0.5 <= c <= 0.5

    // array of index offsets by grid dimension
    int[] off = new int[2];
    off[0] = 1;
    off[1] = off[0] * Lengths[0];

    for (i=0; i<length; i++) {
      boolean WrapThis = false;
      // compute length_is, base, l & c
      length_is = 1;
      if (Double.isNaN(grid[1][i])) {
        base = -1;
      }
      else {
        l[1] = (int) (grid[1][i] + 0.5);
        c[1] = grid[1][i] - ((float) l[1]);
        if (!((l[1] == 0 && c[1] <= 0.0) ||
              (l[1] == Lengths[1] - 1 && c[1] >= 0.0))) {
          // interp along Longitude (dim 1) if between two valid grid coords
          length_is *= 2;
        }
        else if (LongitudeWrap) {
          length_is *= 2;
          WrapThis = true;
        }
        base = l[1];
      }
      if (base>=0) {
        if (Double.isNaN(grid[0][i])) {
          base = -1;
        }
        else {
          l[0] = (int) (grid[0][i] + 0.5);
          c[0] = grid[0][i] - ((float) l[0]);
          if (!((l[0] == 0 && c[0] <= 0.0) ||
                (l[0] == Lengths[0] - 1 && c[0] >= 0.0))) {
            // interp along Latitude (dim 0) if between two valid grid coords
            length_is *= 2;
          }
          base = l[0] + Lengths[0] * base;
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
        cs = new float[length_is];
        is[0] = base;
        cs[0] = 1.0f;
        lis = 1;

        // unroll loop over dimension = 0, 1
        if (WrapThis || !((l[0] == 0 && c[0] <= 0.0) ||
              (l[0] == Lengths[0] - 1 && c[0] >= 0.0)) ) {
          // interp along Latitude (dim 0) if between two valid grid coords
          if (c[0] >= 0.0) {
            // grid coord above base
            isoff = off[0];
            a = 1.0f - c[0];
            b = c[0];
          }
          else {
            // grid coord below base
            isoff = -off[0];
            a = 1.0f + c[0];
            b = -c[0];
          }
          // float is & cs; adjust new offsets; split weights
          for (k=0; k<lis; k++) {
            is[k+lis] = is[k] + isoff;
            cs[k+lis] = cs[k] * b;
            cs[k] *= a;
          }
          lis *= 2;
        }
        if (WrapThis || !((l[1] == 0 && c[1] <= 0.0) ||
              (l[1] == Lengths[1] - 1 && c[1] >= 0.0)) ) {
          // interp along Longitude (dim 1) if between two valid grid coords
          if (WrapThis && l[1] == 0) {
            // c[1] <= 0.0; grid coord below base
            isoff = off[1] * (Lengths[1] - 1); // so Wrap to top
            a = 1.0f + c[1];
            b = -c[1];
          }
          else if (WrapThis && l[1] == Lengths[1] - 1) {
            // c[1] >= 0.0; grid coord above base
            isoff = -off[1] * (Lengths[1] - 1); // so Wrap to bottom
            a = 1.0f - c[1];
            b = c[1];
          }
          else if (c[1] >= 0.0) {
            // grid coord above base
            isoff = off[1];
            a = 1.0f - c[1];
            b = c[1];
          }
          else {
            // grid coord below base
            isoff = -off[1];
            a = 1.0f + c[1];
            b = -c[1];
          }
          // float is & cs; adjust new offsets; split weights
          for (k=0; k<lis; k++) {
            is[k+lis] = is[k] + isoff;
            cs[k+lis] = cs[k] * b;
            cs[k] *= a;
          }
          lis *= 2;
        }
        // end of unroll loop over dimension = 0, 1
      }
      indices[i] = is;
      weights[i] = cs;
    }
  }

  public boolean equals(Object set) {
    if (!(set instanceof LinearLatLonSet) || set == null) return false;
    if (this == set) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    return (X.equals(((LinearLatLonSet) set).getX()) &&
            Y.equals(((LinearLatLonSet) set).getY()));
  }

  public Object clone() {
    try {
      Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                            (Linear1DSet) Y.clone()};
      return new LinearLatLonSet(Type, sets, DomainCoordinateSystem,
                                 SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("LinearLatLonSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    Linear1DSet[] sets = {(Linear1DSet) X.clone(),
                          (Linear1DSet) Y.clone()};
    return new LinearLatLonSet(type, sets, DomainCoordinateSystem,
                               SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s = pre + "LinearLatLonSet: Length = " + Length + "\n";
    s = s + pre + "  Dimension 1: Length = " + X.getLength() +
            " Range = " + X.getFirst() + " to " + X.getLast() + "\n";
    s = s + pre + "  Dimension 2: Length = " + Y.getLength() +
            " Range = " + Y.getFirst() + " to " + Y.getLast() + "\n";
    return s;
  }

}

