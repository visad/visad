
//
// GriddedSet.java
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
   GriddedSet is implemented by those Set sub-classes whose samples
   lie on a rectangular grid topology (but note the geometry need
   not be rectangular).  It is a M-dimensional array of points in 
   R^N where ManifoldDimension = M <= N = DomainDimension.<P>

   The order of the samples is the rasterization of the orders of
   the 1D components, with the first component increasing fastest.
   For more detail, see the example in Linear2DSet.java.<P>

   Grid coordinates are zero-based and in the range -0.5 to length-0.5
   i.e., there are "length" intervals of length 1.0,
   the first centered on 0.0 and the last centered on length-1.0
   points outside this range are indicated by the grid coordinate
   Double.NaN.<P>
*/
public class GriddedSet extends SampledSet {

  int[] Lengths;
  // error tolerance for Newton's method solvers
  // not static because may become data dependent
  float EPS = (float) 1.0E-15;
  // Pos records the grid's orientation
  // (i.e., the sign of the cross-products of the grid edges)
  boolean Pos;

  /** construct a GriddedSet with samples */
  public GriddedSet(MathType type, float[][] samples, int[] lengths)
         throws VisADException {
    this(type, samples, lengths, null, null, null, true);
  }

  /** construct a GriddedSet with samples and non-default CoordinateSystem */
  public GriddedSet(MathType type, float[][] samples, int[] lengths,
                    CoordinateSystem coord_sys, Unit[] units,
                    ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengths, coord_sys, units, errors, true);
  }

  GriddedSet(MathType type, float[][] samples, int[] lengths,
             CoordinateSystem coord_sys, Unit[] units,
             ErrorEstimate[] errors, boolean copy)
             throws VisADException {
    super(type, lengths.length, coord_sys, units, errors);
    init_lengths(lengths);
    if (samples == null ) {
      Samples = null;
    }
    else {
      init_samples(samples, copy);
    }
  }

  private void init_lengths(int[] lengths) throws VisADException {
    Lengths = new int[ManifoldDimension];
    Length = 1;
    for (int j=0; j<ManifoldDimension; j++) {
      if (lengths[j] < 1) {
        throw new SetException("GriddedSet: each grid length must be at least 1");
      }
      Lengths[j] = lengths[j];
      Length = Length * lengths[j];
    }
  }

  /**
   * Abreviated Factory method for creating the proper gridded set
   * (Gridded1DSet, Gridded2DSet, etc.).
   */
  public static GriddedSet create(MathType type, float[][] samples,
                                  int[] lengths) throws VisADException {
    return create(type, samples, lengths, null, null, null);
  }
 
  /**
   * General Factory method for creating the proper gridded set
   * (Gridded1DSet, Gridded2DSet, etc.).
   */
  public static GriddedSet create(MathType type, float[][] samples,
                                  int[] lengths, CoordinateSystem coord_sys,
                                  Unit[] units, ErrorEstimate[] errors)
         throws VisADException {
    return create(type, samples, lengths, coord_sys, units, errors, true);
  }

  static GriddedSet create(MathType type, float[][] samples,
                           int[] lengths, CoordinateSystem coord_sys,
                           Unit[] units, ErrorEstimate[] errors,
                           boolean copy) throws VisADException {
    int domain_dimension = samples.length;
    int manifold_dimension = lengths.length;
    if (manifold_dimension > domain_dimension) {
      throw new SetException("GriddedSet.create: manifold_dimension greater " +
                             "than domain_dimension");
    }
 
    switch (domain_dimension) {
      case 1:
        return new Gridded1DSet(type, samples,
                                lengths[0],
                                coord_sys, units, errors, copy);
      case 2:
        if (manifold_dimension == 1) {
          return new Gridded2DSet(type, samples,
                                  lengths[0],
                                  coord_sys, units, errors, copy);
        }
        else {
          return new Gridded2DSet(type, samples,
                                  lengths[0], lengths[1],
                                  coord_sys, units, errors, copy);
        }
      case 3:
        if (manifold_dimension == 1) {
          return new Gridded3DSet(type, samples,
                                  lengths[0],
                                  coord_sys, units, errors, copy);
        }
        else if (manifold_dimension == 2) {
          return new Gridded3DSet(type, samples,
                                  lengths[0], lengths[1],
                                  coord_sys, units, errors, copy);
        }
        else {
          return new Gridded3DSet(type, samples,
                                  lengths[0], lengths[1], lengths[2],
                                  coord_sys, units, errors, copy);
        }
      default:
        return new GriddedSet(type, samples,
                              lengths,
                              coord_sys, units, errors, copy);
    }
  }

  Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    return create(type, samples, Lengths, null, null, null, false);
  }

  public int getLength(int i) {
    return Lengths[i];
  }

  /** returns a zig-zagging ennumeration of index values with
      good coherence */
  public int[] getWedge() {
    int[] wedge = new int[Length];
    int len = Lengths[0];
    int i, j, k, base, dim;
    boolean flip;
    for (i=0; i<len; i++) wedge[i] = i;
    for (dim=1; dim<ManifoldDimension; dim++) {
      flip = true;
      base = len;
      k = len;
      for (j=1; j<Lengths[dim]; j++) {
        if (flip) {
          for (i=len-1; i>=0; i--) {
            wedge[k] = wedge[i] + base;
            k++;
          }
        }
        else {
          for (i=0; i<len; i++) {
            wedge[k] = wedge[i] + base;
            k++;
          }
        }
        base += len;
        flip = !flip;
      }
      len *= Lengths[dim];
    }
    return wedge;
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int i, j, k;
    int[] indexI = new int[ManifoldDimension];
    int length = index.length;
    float[][] grid = new float[ManifoldDimension][length];
    for (i=0; i<length; i++) {
      if (0 <= index[i] && index[i] < Length) {
        for (j=0; j<ManifoldDimension; j++) indexI[j] = -1;
      }
      else {
        k = index[i];
        for (j=0; j<ManifoldDimension-1; j++) {
          indexI[j] = k % Lengths[j];
          k = k / Lengths[j];
        }
        indexI[ManifoldDimension-1] = k;
      }
      for (j=0; j<ManifoldDimension; j++) grid[j][i] = (float) indexI[j];
    }
    return gridToValue(grid);
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    int i, j, k;
    if (value.length != DomainDimension) {
      throw new SetException("GriddedSet.valueToIndex: bad dimension");
    }
    int length = value[0].length;
    int[] index = new int[length];

    float[][] grid = valueToGrid(value);
    for (i=0; i<length; i++) {
      if (Double.isNaN(grid[ManifoldDimension-1][i])) {
        index[i] = -1;
        continue;
      }
      k = (int) (grid[ManifoldDimension-1][i] + 0.5);
      for (j=ManifoldDimension-2; j>=0; j--) {
        if (Double.isNaN(grid[j][i])) {
          k = -1;
          break;
        }
        k = ((int) (grid[j][i] + 0.5)) + Lengths[j] * k;
      }
      index[i] = k;
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    for (int j=0; j<DomainDimension; j++) {
      if (Lengths[j] < 2) {
        throw new SetException("GriddedSet.gridToValue: requires all grid " +
                               "dimensions to be > 1");
      }
    }
    throw new UnimplementedException("GriddedSet.gridToValue");
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    for (int j=0; j<DomainDimension; j++) {
      if (Lengths[j] < 2) {
        throw new SetException("GriddedSet.valueToGrid: requires all grid " +
                               "dimensions to be > 1");
      }
    }
    throw new UnimplementedException("GriddedSet.valueToGrid");
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices, float[][] weights)
              throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("GriddedSet.valueToInterp: bad dimension");
    }
    int length = value[0].length; // number of values
    if (indices.length != length || weights.length != length) {
      throw new SetException("GriddedSet.valueToInterp: lengths don't match");
    }
    float[][] grid = valueToGrid(value); // convert value array to grid coord array

    int i, j, k; // loop indices
    int lis; // temporary length of is & cs
    int length_is; // final length of is & cs, varies by i
    int isoff; // offset along one grid dimension
    float a, b; // weights along one grid dimension; a + b = 1.0
    int[] is; // array of indices, becomes part of indices
    float[] cs; // array of coefficients, become part of weights

    int base; // base index, as would be returned by valueToIndex
    int[] l = new int[ManifoldDimension]; // integer 'factors' of base
    float[] c = new float[ManifoldDimension]; // fractions with l; -0.5 <= c <= 0.5

    // array of index offsets by grid dimension
    int[] off = new int[ManifoldDimension];
    off[0] = 1;
    for (j=1; j<ManifoldDimension; j++) off[j] = off[j-1] * Lengths[j-1];

    for (i=0; i<length; i++) {
      // compute length_is, base, l & c
      length_is = 1;
      if (Double.isNaN(grid[ManifoldDimension-1][i])) {
        base = -1;
      }
      else {
        l[ManifoldDimension-1] = (int) (grid[ManifoldDimension-1][i] + 0.5);
        c[ManifoldDimension-1] = grid[ManifoldDimension-1][i] -
                                 ((float) l[ManifoldDimension-1]);
        if (!((l[ManifoldDimension-1] == 0 && c[ManifoldDimension-1] <= 0.0) ||
              (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1] - 1 &&
               c[ManifoldDimension-1] >= 0.0))) {
          // only interp along ManifoldDimension-1 if between two valid grid coords
          length_is *= 2;
        }
        base = l[ManifoldDimension-1];
      }
      for (j=ManifoldDimension-2; j>=0 && base>=0; j--) {
        if (Double.isNaN(grid[j][i])) {
          base = -1;
        }
        else {
          l[j] = (int) (grid[j][i] + 0.5);
          c[j] = grid[j][i] - ((float) l[j]);
          if (!((l[j] == 0 && c[j] <= 0.0) ||
                (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
            // only interp along dimension j if between two valid grid coords
            length_is *= 2;
          }
          base = l[j] + Lengths[j] * base;
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

        for (j=0; j<ManifoldDimension; j++) {
          if (!((l[j] == 0 && c[j] <= 0.0) ||
                (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
            // only interp along dimension j if between two valid grid coords
            if (c[j] >= 0.0) {
              // grid coord above base
              isoff = off[j];
              a = 1.0f - c[j];
              b = c[j];
            }
            else {
              // grid coord below base
              isoff = -off[j];
              a = 1.0f + c[j];
              b = -c[j];
            }
            // float is & cs; adjust new offsets; split weights
            for (k=0; k<lis; k++) {
              is[k+lis] = is[k] + isoff;
              cs[k+lis] = cs[k] * b;
              cs[k] *= a;
            }
            lis *= 2;
          }
        }
      }
      indices[i] = is;
      weights[i] = cs;
    }
  }

  public boolean equals(Object set) {
    if (!(set instanceof GriddedSet) || set == null ||
        set instanceof LinearSet) return false;
    if (this == set) return true;
    if (testNotEqualsCache((Set) set)) return false;
    if (testEqualsCache((Set) set)) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      int i, j;
      if (DomainDimension != ((GriddedSet) set).getDimension() ||
          ManifoldDimension != ((GriddedSet) set).getManifoldDimension() ||
          Length != ((GriddedSet) set).getLength()) return false;
      for (j=0; j<ManifoldDimension; j++) {
        if (Lengths[j] != ((GriddedSet) set).getLength(j)) return false;
      }
      // Sets are immutable, so no need for 'synchronized'
      float[][] samples = ((GriddedSet) set).getSamples(false);
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
      return new GriddedSet(Type, Samples, Lengths, DomainCoordinateSystem,
                            SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("GriddedSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new GriddedSet(type, Samples, Lengths, DomainCoordinateSystem,
                          SetUnits, SetErrors);
  }

  public String longString(String pre) throws VisADException {
    String s;
    int j;
    if (DomainDimension == ManifoldDimension) {
      s = pre + getClass().getName() + ": Dimension = " +
          DomainDimension + " Length = " + Length + "\n";
      for (j=0; j<DomainDimension; j++) {
        s = s + pre + "  Dimension " + j + ":" + " Length = " + Lengths[j] +
                " Range = " + Low[j] + " to " + Hi[j] + "\n";
      }
    }
    else {
      s = pre + getClass().getName() + ": DomainDimension = " +
          DomainDimension + " ManifoldDimension = " + ManifoldDimension +
          " Length = " + Length + "\n";
      for (j=0; j<ManifoldDimension; j++) {
        s = s + pre + "  ManifoldDimension " + j + ":" +
            " Length = " + Lengths[j] + "\n";
      }
      for (j=0; j<DomainDimension; j++) {
        s = s + pre + "  DomainDimension " + j + ":" + " Range = " +
            Low[j] + " to " + Hi[j] + "\n";
      }
    }
    return s;
  }

}

