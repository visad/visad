//
// GriddedSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
public class GriddedSet extends SampledSet implements GriddedSetIface {

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

  public GriddedSet(MathType type, float[][] samples, int[] lengths,
             CoordinateSystem coord_sys, Unit[] units,
             ErrorEstimate[] errors, boolean copy)
             throws VisADException {
    super(type, lengths.length, coord_sys, units, errors);
    init_lengths(lengths);
    if (samples == null ) {
	setMySamples(null);
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
        throw new SetException("GriddedSet: each grid length must be" +
                               " at least 1 (length#" + j + " is " +
                               lengths[j]);
      }
      Lengths[j] = lengths[j];
      Length = Length * lengths[j];
    }
  }

  /**
   * Abreviated Factory method for creating the proper gridded set
   * (Gridded1DSet, Gridded2DSet, etc.).
   *
   * @param type                 MathType for the returned set
   * @param samples              Set samples
   * @param lengths              The dimensionality of the manifold.  <code>
   *                             lengths[i}</code> contains the number of points
   *                             in the manifold for dimension <code>i</code>.
   * @throws VisADException      problem creating the set
   */
  public static GriddedSet create(MathType type, float[][] samples,
                                  int[] lengths) throws VisADException {
    return create(type, samples, lengths, null, null, null);
  }

  /**
   * General Factory method for creating the proper gridded set
   * (Gridded1DSet, Gridded2DSet, etc.).
   *
   * @param type                 MathType for the returned set
   * @param samples              Set samples
   * @param lengths              The dimensionality of the manifold.  <code>
   *                             lengths[i}</code> contains the number of points
   *                             in the manifold for dimension <code>i</code>.
   * @param coord_sys            CoordinateSystem for the GriddedSet
   * @param units                Unit-s of the values in <code>samples</code>
   * @param errors               ErrorEstimate-s for the values
   * @throws VisADException      problem creating the set
   */
  public static GriddedSet create(MathType type, float[][] samples,
                                  int[] lengths, CoordinateSystem coord_sys,
                                  Unit[] units, ErrorEstimate[] errors)
         throws VisADException {
    return create(type, samples, lengths, coord_sys, units, errors,
                  true, true);
  }

  /**
   * General Factory method for creating the proper gridded set
   * (Gridded1DSet, Gridded2DSet, etc.).
   *
   * @param type                 MathType for the returned set
   * @param samples              Set samples
   * @param lengths              The dimensionality of the manifold.  <code>
   *                             lengths[i}</code> contains the number of points
   *                             in the manifold for dimension <code>i</code>.
   * @param coord_sys            CoordinateSystem for the GriddedSet
   * @param units                Unit-s of the values in <code>samples</code>
   * @param errors               ErrorEstimate-s for the values
   * @param copy                 make a copy of the samples
   * @throws VisADException      problem creating the set
   */
  public static GriddedSet create(MathType type, float[][] samples,
                                  int[] lengths, CoordinateSystem coord_sys,
                                  Unit[] units, ErrorEstimate[] errors, 
                                  boolean copy)
         throws VisADException {
    return create(type, samples, lengths, coord_sys, units, errors,
                  copy, true);
  }

  /**
   * General Factory method for creating the proper gridded set
   * (Gridded1DSet, Gridded2DSet, etc.).
   *
   * @param type                 MathType for the returned set
   * @param samples              Set samples
   * @param lengths              The dimensionality of the manifold.  <code>
   *                             lengths[i}</code> contains the number of points
   *                             in the manifold for dimension <code>i</code>.
   * @param coord_sys            CoordinateSystem for the GriddedSet
   * @param units                Unit-s of the values in <code>samples</code>
   * @param errors               ErrorEstimate-s for the values
   * @param copy                 make a copy of the samples
   * @param test                 test to make sure samples are valid.  Used
   *                             for creating Gridded*DSets where the 
   *                             manifold dimension is equal to the domain
   *                             dimension
   * @throws VisADException      problem creating the set
   */
    public static GriddedSet create(MathType type, float[][] samples,
                                   int[] lengths, CoordinateSystem coord_sys,
                           Unit[] units, ErrorEstimate[] errors,
                           boolean copy, boolean test)
          throws VisADException {
    int domain_dimension = samples.length;
    int manifold_dimension = lengths.length;
    if (manifold_dimension > domain_dimension) {
      throw new SetException("GriddedSet.create: manifold_dimension " +
                             manifold_dimension + " is greater than" +
                             " domain_dimension " + domain_dimension);
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
                                  coord_sys, units, errors, copy, test);
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
                                  coord_sys, units, errors, copy, test);
        }
      default:
        return new GriddedSet(type, samples,
                              lengths,
                              coord_sys, units, errors, copy);
    }
  }

  public Set makeSpatial(SetType type, float[][] samples) throws VisADException {
    return create(type, samples, Lengths, null, null, null, false, false);
  }

  public int getLength(int i) {
    return Lengths[i];
  }

  public int[] getLengths() {
    int[] lens = new int[Lengths.length];
    for (int i=0; i<Lengths.length; i++) {
      lens[i] = Lengths[i];
    }
    return lens;
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
        k = index[i];
        for (j=0; j<ManifoldDimension-1; j++) {
          indexI[j] = k % Lengths[j];
          k = k / Lengths[j];
        }
        indexI[ManifoldDimension-1] = k;
      }
      else {
        for (j=0; j<ManifoldDimension; j++) indexI[j] = -1;
      }
      for (j=0; j<ManifoldDimension; j++) grid[j][i] = (float) indexI[j];
    }
    return gridToValue(grid);
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    int i, j, k;
    if (value.length != DomainDimension) {
      throw new SetException("GriddedSet.valueToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
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
    if (Length > 1) {
      for (int j=0; j<DomainDimension; j++) {
        if (Lengths[j] < 2) {
          throw new SetException("GriddedSet.gridToValue: requires all grid " +
                                 "dimensions to be > 1");
        }
      }
    }
    throw new UnimplementedException("GriddedSet.gridToValue");
  }

  /** Transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates. 
      A guess for for the first value may be supplied: useful when making
      repeated calls with one value on this set if the resulting grid coords are
      in proximity with one another. The last determined grid location becomes the guess.
  */   
  public float[][] valueToGrid(float[][] value, int[] guess) throws VisADException {
    if (Length > 1) {
      for (int j=0; j<DomainDimension; j++) {
        if (Lengths[j] < 2) {
          throw new SetException("GriddedSet.valueToGrid: requires all grid " +
                                 "dimensions to be > 1");
        }
      }
    }
    throw new UnimplementedException("GriddedSet.valueToGrid with supplied guess");
  }
  
  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (Length > 1) {
      for (int j=0; j<DomainDimension; j++) {
        if (Lengths[j] < 2) {
          throw new SetException("GriddedSet.valueToGrid: requires all grid " +
                                 "dimensions to be > 1");
        }
      }
    }
    throw new UnimplementedException("GriddedSet.valueToGrid");      
  }
  
  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) 
      A guess for the first value may be supplied */
  
  public void valueToInterp(float[][] value, int[][] indices, float[][] weights)
              throws VisADException {
    valueToInterp(value, indices, weights, null);
  }
  
  public void valueToInterp(float[][] value, int[][] indices, float[][] weights, int[] guess)
              throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("GriddedSet.valueToInterp: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length; // number of values
    if (indices.length != length) {
      throw new SetException("GriddedSet.valueToInterp: indices length " +
                             indices.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    if (weights.length != length) {
      throw new SetException("GriddedSet.valueToInterp: weights length " +
                             weights.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    if (guess != null && guess.length != DomainDimension) {
      throw new SetException("GriddedSet.valueToInterp: guess length " +
                             guess.length +
                             " not equal to Domain dimension " +
                             DomainDimension);
    }
    
    float[][] grid;
    // convert value array to grid coord array
    if (guess != null) {
      grid = valueToGrid(value, guess);
    }
    else {
      grid = valueToGrid(value);
    }
    
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
        // WLH 23 Dec 99
        if (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1]) {
          l[ManifoldDimension-1]--;
        }
        c[ManifoldDimension-1] = grid[ManifoldDimension-1][i] -
                                 ((float) l[ManifoldDimension-1]);
        if (!((l[ManifoldDimension-1] == 0 && c[ManifoldDimension-1] <= 0.0) ||
              (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1] - 1 &&
               c[ManifoldDimension-1] >= 0.0))) {
          // only interp along ManifoldDimension-1 if between two valid grid coords
          length_is *= 2;
        }
        base = l[ManifoldDimension-1];
        if (base >= Lengths[ManifoldDimension-1]) base = -1;
      }
      for (j=ManifoldDimension-2; j>=0 && base>=0; j--) {
        if (Double.isNaN(grid[j][i])) {
          base = -1;
        }
        else {
          l[j] = (int) (grid[j][i] + 0.5);
          if (l[j] == Lengths[j]) l[j]--; // WLH 23 Dec 99
          c[j] = grid[j][i] - ((float) l[j]);
          if (!((l[j] == 0 && c[j] <= 0.0) ||
                (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
            // only interp along dimension j if between two valid grid coords
            length_is *= 2;
          }
          base = l[j] + Lengths[j] * base;
          if (l[j] < 0 || l[j] >= Lengths[j]) base = -1;
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

  /**
   * Returns the indexes of the neighboring points for every point in the set.
   * <code>neighbors.length</code> should be at least <code>getLength()</code>.
   * On return, <code>neighbors[i][j]</code> will be the index of the <code>j
   * </code>-th neighboring point of point <code>i</code>.  This method
   * will allocate and set the array <code>neighbors[i]</code> for all
   * <code>i</code>.  For points in the interior of the set, the number of
   * neighboring points is equal to <code>2*getManifoldDimension()</code>.  The
   * number of neighboring points decreases, however, if the point in question
   * is an exterior point on a hyperface, hyperedge, or hypercorner.
   *
   * @param neighbors                The array to contain the indexes of the
   *                                 neighboring points.
   * @throws NullPointerException    if the array is <code>null</code>.
   * @throws ArrayIndexOutOfBoundsException
   *                                 if <code>neighbors.length < getLength()
   *                                 </code>.
   * @throws VisADException          if a VisAD failure occurs.
   */
  public void getNeighbors( int[][] neighbors )
              throws VisADException
  {
    int ii, ix, iy, iz, ii_R, ii_L, ii_U, ii_D, ii_F, ii_B;
    int a, b, c, d;
    int LengthX;
    int LengthY;
    int LengthZ;
    int LengthXY;
    int LengthXYZ;

    switch( ManifoldDimension )
    {
       case 1:
         neighbors[0] = new int[1];
         neighbors[Length - 1] = new int[1];
         neighbors[0][0] = 1;
         neighbors[Length - 1][0] = Length - 2;

         for ( ii = 1; ii < (Length - 1); ii++ ) {
           neighbors[ii] = new int[2];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + 1;
         }
         break;
       case 2:

         LengthX = Lengths[0];
         LengthY = Lengths[1];

         //- corners:
         ii = 0;
         neighbors[ii] = new int[2];
         neighbors[ii][0] = ii + LengthX;
         neighbors[ii][1] = ii + 1;

         ii = Length - 1;
         neighbors[ii] = new int[2];
         neighbors[ii][0] = ii - LengthX;
         neighbors[ii][1] = ii - 1;

         ii = Length - LengthX;
         neighbors[ii] = new int[2];
         neighbors[ii][0] = ii - LengthX;
         neighbors[ii][1] = ii + 1;

         ii = LengthX - 1;
         neighbors[ii] = new int[2];
         neighbors[ii][0] = ii + LengthX;
         neighbors[ii][1] = ii - 1;

         //- edge points, not corners:
         for ( iy = 1; iy < (LengthY - 1); iy++ )
         {
           ii = iy*LengthX;
           neighbors[ii] = new int[3];
           neighbors[ii][0] = ii + LengthX;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii + 1;

           ii = iy*LengthX + LengthX - 1;
           neighbors[ii] = new int[3];
           neighbors[ii][0] = ii + LengthX;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii - 1;
         }

         for ( ix = 1; ix < (LengthX - 1); ix++ )
         {
           ii = ix;
           neighbors[ii] = new int[3];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + 1;
           neighbors[ii][2] = ii + LengthX;

           ii = (LengthY-1)*LengthX + ix;
           neighbors[ii] = new int[3];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + 1;
           neighbors[ii][2] = ii - LengthX;
         }
         //- interior points:
         for ( iy = 1; iy < (LengthY - 1); iy++) {
           for ( ix = 1; ix < (LengthX - 1); ix++) {

             ii   = iy*LengthX + ix;
             ii_R = ii + 1;
             ii_L = ii - 1;
             ii_U = ii + LengthX;
             ii_D = ii - LengthX;
             neighbors[ii] = new int[4];
             neighbors[ii][0] = ii_R;
             neighbors[ii][1] = ii_L;
             neighbors[ii][2] = ii_U;
             neighbors[ii][3] = ii_D;
           }
         }
         break;

       case 3:

         LengthX = Lengths[0];
         LengthY = Lengths[1];
         LengthZ = Lengths[2];
         LengthXY = LengthX*LengthY;
         LengthXYZ = LengthX*LengthY*LengthZ;

         //- corners:
         ii = 0;
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii + LengthX;
         neighbors[ii][1] = ii + 1;
         neighbors[ii][2] = ii + LengthXY;

         ii = LengthX*LengthY*( LengthZ - 1);
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii + 1;
         neighbors[ii][1] = ii + LengthX;
         neighbors[ii][2] = ii - LengthXY;

         ii = LengthX*LengthY - 1;
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii - LengthX;
         neighbors[ii][1] = ii - 1;
         neighbors[ii][2] = ii + LengthXY;

         ii = LengthX*LengthY*LengthZ - 1;
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii - 1;
         neighbors[ii][1] = ii - LengthX;
         neighbors[ii][2] = ii - LengthXY;

         ii = LengthX*(LengthY - 1);
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii - LengthX;
         neighbors[ii][1] = ii + 1;
         neighbors[ii][2] = ii + LengthXY;

         ii = LengthXY*(LengthZ - 1) + LengthX*(LengthY - 1);
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii + 1;
         neighbors[ii][1] = ii - LengthX;
         neighbors[ii][2] = ii - LengthXY;

         ii = LengthX - 1;
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii + LengthX;
         neighbors[ii][1] = ii - 1;
         neighbors[ii][2] = ii + LengthXY;

         ii = LengthXY*(LengthZ - 1) + (LengthX - 1);
         neighbors[ii] = new int[3];
         neighbors[ii][0] = ii - 1;
         neighbors[ii][1] = ii + LengthX;
         neighbors[ii][2] = ii - LengthXY;

         //- edges:
         for ( iy = 1; iy < (LengthY - 1); iy++ )
         {
           a = iy*LengthX;
           b = a + LengthX-1;

           ii = a;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii + LengthX;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii + 1;
           neighbors[ii][3] = ii + LengthXY;

           ii = a + LengthX-1;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii + LengthX;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii - 1;
           neighbors[ii][3] = ii + LengthXY;

           ii = a + (LengthZ-1)*LengthXY;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii + LengthX;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii - 1;
           neighbors[ii][3] = ii - LengthXY;

           ii = b + (LengthZ-1)*LengthXY;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii + LengthX;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii - 1;
           neighbors[ii][3] = ii - LengthXY;
         }
         for ( ix = 1; ix < (LengthX - 1); ix++ )
         {
           ii = ix;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + 1;
           neighbors[ii][2] = ii + LengthX;
           neighbors[ii][3] = ii + LengthXY;

           ii = (LengthY-1)*LengthX + ix;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + 1;
           neighbors[ii][2] = ii - LengthX;
           neighbors[ii][3] = ii + LengthXY;

           ii = (LengthZ-1)*LengthXY + ix;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + 1;
           neighbors[ii][2] = ii + LengthX;
           neighbors[ii][3] = ii - LengthXY;

           ii = (LengthZ-1)*LengthXY + (LengthY-1)*LengthX + ix;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + 1;
           neighbors[ii][2] = ii - LengthX;
           neighbors[ii][3] = ii - LengthXY;
         }

         for ( iz = 1; iz < (LengthZ - 1); iz++ )
         {
           a = iz*LengthXY;
           b = a + (LengthX-1);

           ii = a;
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii + 1;
           neighbors[ii][1] = ii + LengthX;
           neighbors[ii][2] = ii + LengthXY;
           neighbors[ii][3] = ii - LengthXY;

           ii = a + (LengthX-1);
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii + LengthX;
           neighbors[ii][2] = ii + LengthXY;
           neighbors[ii][3] = ii + LengthXY;

           ii = a + LengthX*(LengthY-1);
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii + 1;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii + LengthXY;
           neighbors[ii][3] = ii - LengthXY;

           ii = a + (LengthXY-1);
           neighbors[ii] = new int[4];
           neighbors[ii][0] = ii - 1;
           neighbors[ii][1] = ii - LengthX;
           neighbors[ii][2] = ii + LengthXY;
           neighbors[ii][3] = ii - LengthXY;
         }

         //- sides:

         for ( iy = 1; iy < (LengthY - 1); iy++ )
         {
           for ( ix = 1; ix < ( LengthX - 1); ix++ )
           {
             ii = iy*LengthX + ix;
             neighbors[ii] = new int[5];
             neighbors[ii][0] = ii + 1;
             neighbors[ii][1] = ii - 1;
             neighbors[ii][2] = ii + LengthX;
             neighbors[ii][3] = ii - LengthX;
             neighbors[ii][4] = ii + LengthXY;

             ii = (LengthZ - 1)*LengthXY + iy*LengthX + ix;
             neighbors[ii] = new int[5];
             neighbors[ii][0] = ii + 1;
             neighbors[ii][1] = ii - 1;
             neighbors[ii][2] = ii + LengthX;
             neighbors[ii][3] = ii - LengthX;
             neighbors[ii][4] = ii - LengthXY;
           }
         }

         for ( iz = 1; iz < (LengthZ - 1); iz++ )
         {
           for ( ix = 1; ix < ( LengthX - 1); ix++ )
           {
             ii = iz*LengthXY + ix;
             neighbors[ii] = new int[5];
             neighbors[ii][0] = ii + 1;
             neighbors[ii][1] = ii - 1;
             neighbors[ii][2] = ii + LengthX;
             neighbors[ii][3] = ii + LengthXY;
             neighbors[ii][4] = ii - LengthXY;

             ii = iz*LengthXY + LengthX*(LengthY - 1) + ix;
             neighbors[ii] = new int[5];
             neighbors[ii][0] = ii + 1;
             neighbors[ii][1] = ii - 1;
             neighbors[ii][2] = ii - LengthX;
             neighbors[ii][3] = ii + LengthXY;
             neighbors[ii][4] = ii - LengthXY;
           }
         }

         for ( iz = 1; iz < (LengthZ - 1); iz++ )
         {
           for ( iy = 1; iy < ( LengthY - 1); iy++ )
           {
             ii = iz*LengthXY + iy*LengthX;
             neighbors[ii] = new int[5];
             neighbors[ii][0] = ii + 1;
             neighbors[ii][1] = ii + LengthX;
             neighbors[ii][2] = ii - LengthX;
             neighbors[ii][3] = ii + LengthXY;
             neighbors[ii][4] = ii - LengthXY;

             ii = iz*LengthXY + iy*LengthX + ( LengthX - 1);
             neighbors[ii] = new int[5];
             neighbors[ii][0] = ii - 1;
             neighbors[ii][1] = ii + LengthX;
             neighbors[ii][2] = ii - LengthX;
             neighbors[ii][3] = ii + LengthXY;
             neighbors[ii][4] = ii - LengthXY;
           }
         }

         //- interior points:
         for ( iz = 1; iz < (LengthZ - 1); iz++) {
           for ( iy = 1; iy < (LengthY - 1); iy++) {
             for ( ix = 1; ix < (LengthX - 1); ix++) {

               ii   = iz*LengthXY + iy*LengthX + ix;
               ii_R = ii + 1;
               ii_L = ii - 1;
               ii_F = ii + LengthX;
               ii_B = ii - LengthX;
               ii_U = ii + LengthXY;
               ii_D = ii - LengthXY;
               neighbors[ii] = new int[6];
               neighbors[ii][0] = ii_R;
               neighbors[ii][1] = ii_L;
               neighbors[ii][2] = ii_F;
               neighbors[ii][3] = ii_B;
               neighbors[ii][4] = ii_U;
               neighbors[ii][5] = ii_D;
             }
           }
         }
         break;

       default:
         throw new UnimplementedException("getNeighbors(): ManifoldDimension >"+
                                 ManifoldDimension+" not currently implemented" );

    }
  }

  /**
   * Returns the indexes of the neighboring points along a manifold
   * dimesion for every point in the set. Elements <code>[i][0]</code>
   * and <code>[i][1]</code> of the returned array are the indexes of the
   * neighboring sample points in the direction of decreasing and increasing
   * manifold index, respectively, for sample point <code>i</code>.  If a sample
   * point doesn't have a neighboring point (because it is an exterior point,
   * for example) then the value of the corresponding index will be -1.
   *
   * @param manifoldIndex          The index of the manifold dimension along
   *                               which to return the neighboring points.
   * @throws ArrayIndexOutOfBoundsException
   *                               if <code>manifoldIndex < 0 || 
   *                               manifoldIndex >= getManifoldDimension()
   *                               </code>.
   * @see #getManifoldDimension()
   */
  public int[][] getNeighbors( int manifoldIndex )
  {
    int[][] neighbors = new int[ Length ][2];
    int[] m_coords = new int[ ManifoldDimension ];
    int[][] indeces = new int[2][ ManifoldDimension ];
    int ii_tmp, idx_u, idx_d, mm, tt, ii, jj, kk, k;

    for ( ii = 0; ii < Length; ii++ )
    {
      //- get the manifold coordinates for each index  -*
      ii_tmp = ii;
      for ( jj = 0; jj < (ManifoldDimension-1); jj++ ) {
        m_coords[jj] = ii_tmp % Lengths[jj];
        ii_tmp /= Lengths[jj];
      }
      m_coords[ManifoldDimension-1] = ii_tmp;

      //- get coordinates of two neighbors on each side  -*
      for ( kk = 0; kk < 2; kk++) {
        for ( jj = 0; jj < ManifoldDimension; jj++) {
          indeces[kk][jj] = m_coords[jj];
        }
      }

      idx_u = m_coords[ manifoldIndex ] + 1;
      idx_d = m_coords[ manifoldIndex ] - 1;
      indeces[1][manifoldIndex] = idx_u;
      indeces[0][manifoldIndex] = idx_d;
      if ( idx_u >= Lengths[manifoldIndex] ) {
        indeces[1][manifoldIndex] = -1;
      }
      else if ( idx_d < 0 ) {
        indeces[0][manifoldIndex] = -1;
      }

      //- compute index for each new coordinates  -*
      for ( kk = 0; kk < 2; kk++ )
      {
        if ( indeces[kk][manifoldIndex] != -1 ) {
          ii_tmp = 0;
          for ( mm = (ManifoldDimension-1); mm>=0; mm--) {
            k = indeces[kk][mm];
            for ( tt = 0; tt < mm; tt++ ) {
              k = k*Lengths[tt];
            }
            ii_tmp += k;
          }
          neighbors[ii][kk] = ii_tmp;
        }
        else {
          neighbors[ii][kk] = -1;
        }
      }
    }
    return neighbors;
  }

  public boolean equals(Object set) {
    if (!(set instanceof GriddedSet) || set == null ||
        set instanceof LinearSet ||
        set instanceof Gridded1DDoubleSet) return false;
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
      float[][]mySamples = getMySamples();
      if (mySamples != null) {
        for (j=0; j<DomainDimension; j++) {
          for (i=0; i<Length; i++) {
            if (mySamples[j][i] == mySamples[j][i]
                  ? mySamples[j][i] != samples[j][i]
                  : samples[j][i] == samples[j][i]) {
              addNotEqualsCache((Set) set);
              return false;
            }
          }
        }
      }
      else {
        float[][] this_samples = getSamples(false);
        for (j=0; j<DomainDimension; j++) {
          for (i=0; i<Length; i++) {
            if (this_samples[j][i] == this_samples[j][i]
                  ? this_samples[j][i] != samples[j][i]
                  : samples[j][i] == samples[j][i]) {
              addNotEqualsCache((Set) set);
              return false;
            }
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

  /**
   * Returns the hash code of this instance. {@link Object#hashCode()} should be
   * overridden whenever {@link Object#equals(Object)} is.
   * @return			The hash code of this instance (includes the
   *				values).
   */
  public int hashCode()
  {
    if (!hashCodeSet)
    {
      hashCode = unitAndCSHashCode();
      hashCode ^= DomainDimension ^ ManifoldDimension ^ Length;
      for (int j=0; j<ManifoldDimension; j++)
	hashCode ^= Lengths[j];
      float[][]mySamples = getMySamples();
      if (mySamples != null)
	for (int j=0; j<DomainDimension; j++)
	  for (int i=0; i<Length; i++)
	    hashCode ^= Float.floatToIntBits(mySamples[j][i]);
      hashCodeSet = true;
    }
    return hashCode;
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new GriddedSet(type,     getMySamples(), Lengths, DomainCoordinateSystem,
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

