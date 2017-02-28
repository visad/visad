//
// LinearLatLonSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
  private int latI;             // index of latitude component
  private int lonI;             // index of longitude component
  private double halfPiLat;     // 0.5*pi in set lat units
  private double halfPiLon;     // 0.5*pi in set lon units
  private double twoPiLon;      // 2*pi in set lon units
  private Linear1DSet lat;      // references X or Y in super
  private Linear1DSet lon;      // references Y or X in super

  /**
   * Construct a 2-D cross product of arithmetic progressions whose east
   * and west edges may be joined (for interpolation purposes), with
   * null errors, CoordinateSystem and Units are defaults from type.
   * @param type     MathType for this LinearLatLonSet.  Must be consistent
   *                 with MathType-s of sets.
   * @param sets     Linear1DSets that make up this LinearLatLonSet.
   * @throws VisADException illegal sets or other VisAD error.
   */
  public LinearLatLonSet(MathType type, Linear1DSet[] sets) throws VisADException {
    this(type, sets, null, null, null);
  }

  /** 
   * Construct a 2-D cross product of arithmetic progressions whose east
   * and west edges may be joined (for interpolation purposes), with
   * null errors, CoordinateSystem and Units are defaults from type 
   * @param type       MathType for this LinearLatLonSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @throws VisADException illegal sets or other VisAD error.
   */
  public LinearLatLonSet(MathType type, double first1, double last1, int length1,
                                        double first2, double last2, int length2)
         throws VisADException {
    this(type, first1, last1, length1, first2, last2, length2, null, null, null);
  }

  /**
   * Construct a 2-D cross product of arithmetic progressions whose east
   * and west edges may be joined (for interpolation purposes), with
   * specified <code>errors</code>, <code>coord_sys</code> and <code>
   * units</code>.
   * @param type       MathType for this LinearLatLonSet.  Must be consistent
   *                   with MathType-s of sets.
   * @param sets       Linear1DSets that make up this LinearLatLonSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException illegal sets or other VisAD error.
   */
  public LinearLatLonSet(MathType type, Linear1DSet[] sets,
                         CoordinateSystem coord_sys, Unit[] units,
                         ErrorEstimate[] errors) throws VisADException {
    this(type, sets, coord_sys, units, errors, false);
  }

  /**
   * Construct a 2-D cross product of arithmetic progressions whose east
   * and west edges may be joined (for interpolation purposes), with
   * specified <code>errors</code>, <code>coord_sys</code> and <code>
   * units</code>.
   * @param type       MathType for this LinearLatLonSet.  Must be consistent
   *                   with MathType-s of sets.
   * @param sets       Linear1DSets that make up this LinearLatLonSet.
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}
   * @throws VisADException illegal sets or other VisAD error.
   */
  public LinearLatLonSet(MathType type, Linear1DSet[] sets,
                         CoordinateSystem coord_sys, Unit[] units,
                         ErrorEstimate[] errors, 
                         boolean cache) throws VisADException {
    super(type, sets, coord_sys, units, errors, cache);
    setParameters();
    checkWrap();
  }

  /** a 2-D cross product of arithmetic progressions that whose east
      and west edges may be joined (for interpolation purposes);
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  /** 
   * Construct a 2-D cross product of arithmetic progressions whose east
   * and west edges may be joined (for interpolation purposes), with
   * specified <code>errors</code>, <code>coord_sys</code> and <code>
   * units</code>.
   * @param type       MathType for this LinearLatLonSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @throws VisADException illegal sets or other VisAD error.
   */
  public LinearLatLonSet(MathType type, double first1, double last1, int length1,
                         double first2, double last2, int length2,
                         CoordinateSystem coord_sys, Unit[] units,
                         ErrorEstimate[] errors) throws VisADException {
    this(type, first1, last1, length1, first2, last2, length2, coord_sys,
            units, errors, false);
  }

  /** 
   * Construct a 2-D cross product of arithmetic progressions whose east
   * and west edges may be joined (for interpolation purposes), with
   * specified <code>errors</code>, <code>coord_sys</code> and <code>
   * units</code>.
   * @param type       MathType for this LinearLatLonSet.  
   * @param first1     first value in first arithmetic progression
   * @param last1      last value in first arithmetic progression
   * @param length1    number of values in first arithmetic progression
   * @param first2     first value in second arithmetic progression
   * @param last2      last value in second arithmetic progression
   * @param length2    number of values in second arithmetic progression
   * @param coord_sys  CoordinateSystem for this set.  May be null, but
   *                   if not, must be consistent with <code>type</code>.
   * @param units      Unit-s for the values in <code>sets</code>.  May
   *                   be null, but must be convertible with values in
   *                   <code>sets</code>.
   * @param errors     ErrorEstimate-s for values in <code>sets</code>,
   *                   may be null
   * @param cache      if true, enumerate and cache the samples.  This will
   *                   result in a larger memory footprint, but will
   *                   reduce the time to return samples from calls to 
   *                   {@link #getSamples()}
   * @throws VisADException illegal sets or other VisAD error.
   */
  public LinearLatLonSet(MathType type, double first1, double last1, int length1,
                         double first2, double last2, int length2,
                         CoordinateSystem coord_sys, Unit[] units,
                         ErrorEstimate[] errors,
                         boolean cache) throws VisADException {
    super(type, first1, last1, length1, first2, last2, length2, coord_sys,
          units, errors, cache);
    setParameters();
    checkWrap();
  }

  private void setParameters() throws VisADException, UnitException {
    MathType    type0 = ((SetType)getType()).getDomain().getComponent(0);

    latI = RealType.Latitude.equals(type0) ? 0 : 1;

    if (latI == 0) {
      lonI = 1;
      lat = X;
      lon = Y;
    } else {
      lonI = 0;
      lat = Y;
      lon = X;
    }

    Unit[] units = getSetUnits();
    halfPiLat = SI.radian.toThat(0.5*Math.PI, units[latI]);
    halfPiLon = SI.radian.toThat(0.5*Math.PI, units[lonI]);
    twoPiLon = 4.0 * halfPiLon;
  }

  private void setLatLonUnits()
  {
  }

  void checkWrap() throws VisADException {
    Unit[] units = getSetUnits();

    if (Low[latI] < -halfPiLat || Hi[latI] > halfPiLat ||
        Low[lonI] < -twoPiLon || Hi[lonI] > twoPiLon ||
        (Hi[lonI] - Low[lonI]) > twoPiLon) {
      throw new SetException("LinearLatLonSet: out of bounds " +
                             "(note Lat and Lon in Radians)");
    }
    LongitudeWrap =
        (Hi[lonI] - Low[lonI]) + 2.0 * Math.abs(lon.getStep()) >= twoPiLon &&
        lon.getLength() > 1;

    if(lon.getLength() > 1 && lon.getFirst() > 0 && lon.getLast() < 0){
      LongitudeWrap = true;
    }

    if (LongitudeWrap) {
      WrapStep = twoPiLon - (Hi[lonI] - Low[lonI]);
      WrapFactor = Math.abs(lon.getStep()) / WrapStep;
    }
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in (Latitude, Longitude) */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != 2) {
      throw new SetException("LinearLatLonSet.gridToValue: grid dimension" +
                             " should be 2, not " + grid.length);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("LinearLatLonSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    float[][] gridLat = new float[1][];
    gridLat[0] = grid[latI];
    float[][] gridLon = new float[1][];
    gridLon[0] = grid[lonI];
    if (LongitudeWrap) {
      float len = (float) lon.getLength();
      float lenh = len - 0.5f;
      float lenm = len - 1.0f;
      for (int i=0; i<length; i++) {
        if (gridLon[0][i] > lenm) {
          gridLon[0][i] = (float) (lenm + (gridLon[0][i] - lenm) / WrapFactor);
          if (gridLon[0][i] > lenh) gridLon[0][i] -= len;
        }
        else if (gridLon[0][i] < 0.0) {
          gridLon[0][i] = (float) (gridLon[0][i] / WrapFactor);
          if (gridLon[0][i] < -0.5) gridLon[0][i] += len;
        }
      }
    }
    float[][] valueLat = lat.gridToValue(gridLat);
    float[][] valueLon = lon.gridToValue(gridLon);
    float[][] value = new float[2][];
    value[latI] = valueLat[0];
    value[lonI] = valueLon[0];
    return value;
  }

  /** transform an array of values in (Latitude, Longitude)
      to an array of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length != 2) {
      throw new SetException("LinearLatLonSet.valueToGrid: value dimension" +
                             " should be 2, not " + value.length);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("LinearLatLonSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = value[0].length;
    float[][] valueLat = new float[1][];
    valueLat[0] = value[latI];
    float[] valueLon = value[lonI];
    float[][] gridLat = lat.valueToGrid(valueLat); // Latitude-s

    float[] gridLon = new float[length];
    float l = (float) (lon.getFirst() - 0.5 * lon.getStep());
    float h = (float) (lon.getFirst() + (((float) lon.getLength()) - 0.5) * lon.getStep());
    if (h < l) {
      float temp = l;
      l = h;
      h = temp;
    }
    if (LongitudeWrap) {
      l = (float) (l + 0.5 * Math.abs(lon.getStep()) - 0.5 * WrapStep);
      h = (float) (l + twoPiLon);
    }
    for (int i=0; i<length; i++) {
      float v = (float) (valueLon[i] % (twoPiLon));
      if (v <= l ) v += twoPiLon;
      else if (h <= v) v -= twoPiLon;
      gridLon[i] = (float)
        ((l < v && v < h) ? (v - lon.getFirst()) * lon.getInvstep() : Double.NaN);
    }
    if (LongitudeWrap) {
      float len = (float) lon.getLength();
      float lenh = len - 0.5f;
      float lenm = len - 1.0f;
      for (int i=0; i<length; i++) {
        if (gridLon[i] > lenm) {
          gridLon[i] = (float) (lenm + (gridLon[i] - lenm) * WrapFactor);
          if (gridLon[i] > lenh) gridLon[i] -= len;
        }
        else if (gridLon[i] < 0.0) {
          gridLon[i] = (float) (gridLon[i] * WrapFactor);
          if (gridLon[i] < -0.5) gridLon[i] += len;
        }
      }
    }
    float[][] grid = new float[2][];
    grid[latI] = gridLat[0];
    grid[lonI] = gridLon;
    return grid;
  }

  /** for each of an array of values in (Latitude, Longitude), compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible).
      this code is the result of substituting 2 for ManifoldDimension in
      GriddedSet.valueToInterp, and adding logic to handle LongitudeWrap */
  public void valueToInterp(float[][] value, int[][] indices, float weights[][])
              throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("LinearLatLonSet Domain dimension" +
                             " should be 2, not " + DomainDimension);
    }
    int length = value[0].length; // number of values
    if (indices.length != length) {
      throw new SetException("LinearLatLonSet.valueToInterp: indices length " +
                             indices.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    if (weights.length != length) {
      throw new SetException("LinearLatLonSet.valueToInterp: weights length " +
                             weights.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    float[][] grid = valueToGrid(value); // convert value array to grid coord array
    /* if LongitudeWrap, grid[lonI][i] should be between -0.5 and lon.Length-0.5 */

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
      if (Double.isNaN(grid[lonI][i])) {
        base = -1;
      }
      else {
        l[lonI] = (int) (grid[lonI][i] + 0.5);
        c[lonI] = grid[lonI][i] - ((float) l[lonI]);
        if (!((l[lonI] == 0 && c[lonI] <= 0.0) ||
              (l[lonI] == Lengths[lonI] - 1 && c[lonI] >= 0.0))) {
          // interp along Longitude (dim lonI) if between two valid grid coords
          length_is *= 2;
        }
        else if (LongitudeWrap) {
          length_is *= 2;
          WrapThis = true;
        }
        base = l[lonI];
      }
      if (base>=0) {
        if (Double.isNaN(grid[latI][i])) {
          base = -1;
        }
        else {
          l[latI] = (int) (grid[latI][i] + 0.5);
          c[latI] = grid[latI][i] - ((float) l[latI]);
          if (!((l[latI] == 0 && c[latI] <= 0.0) ||
                (l[latI] == Lengths[latI] - 1 && c[latI] >= 0.0))) {
            // interp along Latitude (dim latI) if between two valid grid coords
            length_is *= 2;
          }
          base = off[latI] * l[latI] + off[lonI] * base;
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
        // WLH 31 Oct 2001
        if (!((l[latI] == 0 && c[latI] <= 0.0) ||
              (l[latI] == Lengths[latI] - 1 && c[latI] >= 0.0)) ) {
        // if (WrapThis || !((l[latI] == 0 && c[latI] <= 0.0) ||
        //       (l[latI] == Lengths[latI] - 1 && c[latI] >= 0.0)) ) {

          // interp along Latitude (dim latI) if between two valid grid coords
          if (c[latI] >= 0.0) {
            // grid coord above base
            isoff = off[latI];
            a = 1.0f - c[latI];
            b = c[latI];
          }
          else {
            // grid coord below base
            isoff = -off[latI];
            a = 1.0f + c[latI];
            b = -c[latI];
          }
          // float is & cs; adjust new offsets; split weights
          for (k=0; k<lis; k++) {
            is[k+lis] = is[k] + isoff;
            cs[k+lis] = cs[k] * b;
            cs[k] *= a;
          }
          lis *= 2;
        }
        if (WrapThis || !((l[lonI] == 0 && c[lonI] <= 0.0) ||
              (l[lonI] == Lengths[lonI] - 1 && c[lonI] >= 0.0)) ) {
          // interp along Longitude (dim lonI) if between two valid grid coords
          if (WrapThis && l[lonI] == 0) {
            // c[lonI] <= 0.0; grid coord below base
            isoff = off[lonI] * (Lengths[lonI] - 1); // so Wrap to top
            a = 1.0f + c[lonI];
            b = -c[lonI];
          }
          else if (WrapThis && l[lonI] == Lengths[lonI] - 1) {
            // c[lonI] >= 0.0; grid coord above base
            isoff = -off[lonI] * (Lengths[lonI] - 1); // so Wrap to bottom
            a = 1.0f - c[lonI];
            b = c[lonI];
          }
          else if (c[lonI] >= 0.0) {
            // grid coord above base
            isoff = off[lonI];
            a = 1.0f - c[lonI];
            b = c[lonI];
          }
          else {
            // grid coord below base
            isoff = -off[lonI];
            a = 1.0f + c[lonI];
            b = -c[lonI];
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

