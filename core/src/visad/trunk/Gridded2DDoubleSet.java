//
// Gridded2DDoubleSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
   Gridded2DDoubleSet is a Gridded2DSet with double-precision samples.<P>
*/
public class Gridded2DDoubleSet extends Gridded2DSet
       implements GriddedDoubleSet {

  double[] Low = new double[2];
  double[] Hi = new double[2];
  double LowX, HiX, LowY, HiY;
  double[][] Samples;


  // Overridden Gridded2DSet constructors (float[][])

  /** a 2-D set whose topology is a lengthX x lengthY grid, with
      null errors, CoordinateSystem and Units are defaults from type */
  public Gridded2DDoubleSet(MathType type, float[][] samples, int lengthX, int lengthY)
         throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY, null, null, null, true);
  }

  /** a 2-D set whose topology is a lengthX x lengthY grid;
      samples array is organized float[2][number_of_samples] where
      lengthX * lengthY = number_of_samples; samples must form a
      non-degenerate 2-D grid (no bow-tie-shaped grid boxes); the
      X component increases fastest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded2DDoubleSet(MathType type, float[][] samples, int lengthX, int lengthY,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY, coord_sys,
         units, errors, true);
  }

  Gridded2DDoubleSet(MathType type, float[][] samples, int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, lengthY, coord_sys,
         units, errors, copy);
  }

  /** a 2-D set with manifold dimension = 1, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded2DDoubleSet(MathType type, float[][] samples, int lengthX)
         throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, null, null, null, true);
  }

  /** a 2-D set with manifold dimension = 1; samples array is
      organized float[2][number_of_samples] where lengthX =
      number_of_samples; no geometric constraint on samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded2DDoubleSet(MathType type, float[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, coord_sys, units, errors, true);
  }

  public Gridded2DDoubleSet(MathType type, float[][] samples, int lengthX,
                            CoordinateSystem coord_sys, Unit[] units,
                            ErrorEstimate[] errors, boolean copy)
                            throws VisADException {
    this(type, Set.floatToDouble(samples), lengthX, coord_sys, units, errors, copy);
  }


  // Corresponding Gridded2DDoubleSet constructors (double[][])

  /** a 2-D set whose topology is a lengthX x lengthY grid, with
      null errors, CoordinateSystem and Units are defaults from type */
  public Gridded2DDoubleSet(MathType type, double[][] samples, int lengthX, int lengthY)
         throws VisADException {
    this(type, samples, lengthX, lengthY, null, null, null, true);
  }

  /** a 2-D set whose topology is a lengthX x lengthY grid;
      samples array is organized double[2][number_of_samples] where
      lengthX * lengthY = number_of_samples; samples must form a
      non-degenerate 2-D grid (no bow-tie-shaped grid boxes); the
      X component increases fastest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded2DDoubleSet(MathType type, double[][] samples, int lengthX, int lengthY,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, lengthY, coord_sys, units, errors, true);
  }

  public Gridded2DDoubleSet(MathType type, double[][] samples, int lengthX,
                            int lengthY, CoordinateSystem coord_sys, Unit[] units,
                            ErrorEstimate[] errors, boolean copy)
                            throws VisADException {
    super(type, null, lengthX, lengthY, coord_sys, units, errors, copy);
    if (samples == null) {
      throw new SetException("Gridded2DDoubleSet: samples are null");
    }
    init_doubles(samples, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LengthY = Lengths[1];

    if (Samples != null && Lengths[0] > 1 && Lengths[1] > 1) {
      for (int i=0; i<Length; i++) {
        if (Samples[0][i] != Samples[0][i]) {
          throw new SetException(
           "Gridded2DDoubleSet: samples values may not be missing");
        }
      }
      // Samples consistency test
      Pos = ( (Samples[0][1]-Samples[0][0])
             *(Samples[1][LengthX+1]-Samples[1][1])
            - (Samples[1][1]-Samples[1][0])
             *(Samples[0][LengthX+1]-Samples[0][1]) > 0);
      for (int j=0; j<LengthY-1; j++) {
        for (int i=0; i<LengthX-1; i++) {
          double[] v00 = new double[2];
          double[] v10 = new double[2];
          double[] v01 = new double[2];
          double[] v11 = new double[2];
          for (int v=0; v<2; v++) {
            v00[v] = Samples[v][j*LengthX+i];
            v10[v] = Samples[v][j*LengthX+i+1];
            v01[v] = Samples[v][(j+1)*LengthX+i];
            v11[v] = Samples[v][(j+1)*LengthX+i+1];
          }
          if (  ( (v10[0]-v00[0])*(v11[1]-v10[1])
                - (v10[1]-v00[1])*(v11[0]-v10[0]) > 0 != Pos)
             || ( (v11[0]-v10[0])*(v01[1]-v11[1])
                - (v11[1]-v10[1])*(v01[0]-v11[0]) > 0 != Pos)
             || ( (v01[0]-v11[0])*(v00[1]-v01[1])
                - (v01[1]-v11[1])*(v00[0]-v01[0]) > 0 != Pos)
             || ( (v00[0]-v01[0])*(v10[1]-v00[1])
                - (v00[1]-v01[1])*(v10[0]-v00[0]) > 0 != Pos)  ) {
            throw new SetException(
             "Gridded2DDoubleSet: samples do not form a valid grid ("+i+","+j+")");
          }
        }
      }
    }
  }

  /** a 2-D set with manifold dimension = 1, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded2DDoubleSet(MathType type, double[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null);
  }

  /** a 2-D set with manifold dimension = 1; samples array is
      organized double[2][number_of_samples] where lengthX =
      number_of_samples; no geometric constraint on samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded2DDoubleSet(MathType type, double[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, coord_sys, units, errors, true);
  }

  public Gridded2DDoubleSet(MathType type, double[][] samples, int lengthX,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    super(type, null, lengthX, coord_sys, units, errors, copy);
    if (samples == null) {
      throw new SetException("Gridded2DDoubleSet: samples are null");
    }
    init_doubles(samples, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];

    // no Samples consistency test
  }

//END


  // Overridden Gridded2DSet methods (float[][])

  public float[][] getSamples() throws VisADException {
    return getSamples(true);
  }

  public float[][] getSamples(boolean copy) throws VisADException {
    return Set.doubleToFloat(Samples);
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    return Set.doubleToFloat(indexToDouble(index));
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    return doubleToIndex(Set.floatToDouble(value));
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    return Set.doubleToFloat(gridToDouble(Set.floatToDouble(grid)));
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    return Set.doubleToFloat(doubleToGrid(Set.floatToDouble(value)));
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void valueToInterp(float[][] value, int[][] indices,
    float[][] weights) throws VisADException
  {
    int len = weights.length;
    double[][] w = new double[len][];
    doubleToInterp(Set.floatToDouble(value), indices, w);
    for (int i=0; i<len; i++) {
      if (w[i] != null) {
        weights[i] = new float[w[i].length];
        for (int j=0; j<w[i].length; j++) {
          weights[i][j] = (float) w[i][j];
        }
      }
    }
  }


  // Corresponding Gridded2DDoubleSet methods (double[][])

  public double[][] getDoubles() throws VisADException {
    return getDoubles(true);
  }

  public double[][] getDoubles(boolean copy) throws VisADException {
    return copy ? Set.copyDoubles(Samples) : Samples;
  }

  /** convert an array of 1-D indices to an array of values in
      R^DomainDimension */
  public double[][] indexToDouble(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear2DSet.indexToValue
      int indexX, indexY;
      double[][] grid = new double[ManifoldDimension][length];

      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          indexX = index[i] % LengthX;
          indexY = index[i] / LengthX;
        }
        else {
          indexX = -1;
          indexY = -1;
        }
        grid[0][i] = indexX;
        grid[1][i] = indexY;
      }
      return gridToDouble(grid);
    }
    else {
      double[][] values = new double[2][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
          values[1][i] = Samples[1][index[i]];
        }
        else {
          values[0][i] = Double.NaN;
          values[1][i] = Double.NaN;
        }
      }
      return values;
    }
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] doubleToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded2DDoubleSet.doubleToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length;
    int[] index = new int[length];

    double[][] grid = doubleToGrid(value);
    double[] grid0 = grid[0];
    double[] grid1 = grid[1];
    double g0, g1;
    for (int i=0; i<length; i++) {
      g0 = grid0[i];
      g1 = grid1[i];
/* WLH 24 Oct 97
      index[i] = (Double.isNaN(g0) || Double.isNaN(g1)) ? -1 :
*/
      // test for missing
      index[i] = (g0 != g0 || g1 != g1) ? -1 :
                 ((int) (g0 + 0.5)) + LengthX * ((int) (g1 + 0.5));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public double[][] gridToDouble(double[][] grid) throws VisADException {
    if (grid.length != ManifoldDimension) {
      throw new SetException("Gridded2DDoubleSet.gridToDouble: bad dimension");
    }
    if (ManifoldDimension < 2) {
      throw new SetException("Gridded2DDoubleSet.gridToDouble: ManifoldDimension " +
                             "must be 2");
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded2DDoubleSet.gridToDouble: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    double[][] value = new double[2][length];
    for (int i=0; i<length; i++) {
      // let gx and gy by the current grid values
      double gx = grid[0][i];
      double gy = grid[1][i];
      if ( (gx < -0.5)        || (gy < -0.5) ||
           (gx > LengthX-0.5) || (gy > LengthY-0.5) ) {
        value[0][i] = value[1][i] = Double.NaN;
      } else if (Length == 1) {
        value[0][i] = Samples[0][0];
        value[1][i] = Samples[1][0];
      } else {
        // calculate closest integer variables
        int igx = (int) gx;
        int igy = (int) gy;
        if (igx < 0) igx = 0;
        if (igx > LengthX-2) igx = LengthX-2;
        if (igy < 0) igy = 0;
        if (igy > LengthY-2) igy = LengthY-2;
  
        // set up conversion to 1D Samples array
        int[][] s = { {LengthX*igy+igx,           // (0, 0)
                       LengthX*(igy+1)+igx},      // (0, 1)
                      {LengthX*igy+igx+1,         // (1, 0)
                       LengthX*(igy+1)+igx+1} };  // (1, 1)
        if (gx+gy-igx-igy-1 <= 0) {
          // point is in LOWER triangle
          for (int j=0; j<2; j++) {
            value[j][i] = Samples[j][s[0][0]]
              + (gx-igx)*(Samples[j][s[1][0]]-Samples[j][s[0][0]])
              + (gy-igy)*(Samples[j][s[0][1]]-Samples[j][s[0][0]]);
          }
        }
        else {
          // point is in UPPER triangle
          for (int j=0; j<2; j++) {
            value[j][i] = Samples[j][s[1][1]]
              + (1+igx-gx)*(Samples[j][s[0][1]]-Samples[j][s[1][1]])
              + (1+igy-gy)*(Samples[j][s[1][0]]-Samples[j][s[1][1]]);
          }
        }
      }
    }
    return value;
  }

  // WLH 6 Dec 2001
  private int gx = -1;
  private int gy = -1;

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public double[][] doubleToGrid(double[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded2DDoubleSet.doubleToGrid: bad dimension");
    }
    if (ManifoldDimension < 2) {
      throw new SetException("Gridded2DDoubleSet.doubleToGrid: ManifoldDimension " +
                             "must be 2");
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded2DDoubleSet.doubleToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = Math.min(value[0].length, value[1].length);
    double[][] grid = new double[ManifoldDimension][length];

    // (gx, gy) is the current grid box guess
/* WLH 6 Dec 2001
    int gx = (LengthX-1)/2;
    int gy = (LengthY-1)/2;
*/
    // use value from last call as first guess, if reasonable
    if (gx < 0 || gx >= LengthX || gy < 0 || gy >= LengthY) {
      gx = (LengthX-1)/2;
      gy = (LengthY-1)/2;
    }

    boolean lowertri = true;
    for (int i=0; i<length; i++) {
      // grid box guess starts at previous box unless there was no solution
/* WLH 24 Oct 97
      if ( (i != 0) && (Double.isNaN(grid[0][i-1])) )
*/

      if (Length == 1) {
        if (Double.isNaN(value[0][i]) || Double.isNaN(value[1][i])) {
           grid[0][i] = grid[1][i] = Double.NaN;
        } else {
           grid[0][i] = 0;
           grid[1][i] = 0;
        }
        continue;
      }

      // test for missing
      if ( (i != 0) && grid[0][i-1] != grid[0][i-1] ) {
        gx = (LengthX-1)/2;
        gy = (LengthY-1)/2;
      }

      // if the loop doesn't find the answer, the result should be NaN
      grid[0][i] = grid[1][i] = Double.NaN;
      for (int itnum=0; itnum<2*(LengthX+LengthY); itnum++) {
        // define the four vertices of the current grid box
        double[] v0 = {Samples[0][gy*LengthX+gx],
                       Samples[1][gy*LengthX+gx]};
        double[] v1 = {Samples[0][gy*LengthX+gx+1],
                       Samples[1][gy*LengthX+gx+1]};
        double[] v2 = {Samples[0][(gy+1)*LengthX+gx],
                       Samples[1][(gy+1)*LengthX+gx]};
        double[] v3 = {Samples[0][(gy+1)*LengthX+gx+1],
                       Samples[1][(gy+1)*LengthX+gx+1]};

        // Both cases use diagonal D-B and point distances P-B and P-D
        double[] bd = {v2[0]-v1[0], v2[1]-v1[1]};
        double[] bp = {value[0][i]-v1[0], value[1][i]-v1[1]};
        double[] dp = {value[0][i]-v2[0], value[1][i]-v2[1]};

        // check the LOWER triangle of the grid box
        if (lowertri) {
          double[] ab = {v1[0]-v0[0], v1[1]-v0[1]};
          double[] da = {v0[0]-v2[0], v0[1]-v2[1]};
          double[] ap = {value[0][i]-v0[0], value[1][i]-v0[1]};
          double tval1 = ab[0]*ap[1]-ab[1]*ap[0];
          double tval2 = bd[0]*bp[1]-bd[1]*bp[0];
          double tval3 = da[0]*dp[1]-da[1]*dp[0];
          boolean test1 = (tval1 == 0) || ((tval1 > 0) == Pos);
          boolean test2 = (tval2 == 0) || ((tval2 > 0) == Pos);
          boolean test3 = (tval3 == 0) || ((tval3 > 0) == Pos);
          int ogx = gx;
          int ogy = gy;
          if (!test1 && !test2) {      // Go UP & RIGHT
            gx++;
            gy--;
          }
          else if (!test2 && !test3) { // Go DOWN & LEFT
            gx--;
            gy++;
          }
          else if (!test1 && !test3) { // Go UP & LEFT
            gx--;
            gy--;
          }
          else if (!test1) {           // Go UP
            gy--;
          }
          else if (!test3) {           // Go LEFT
            gx--;
          }
          // Snap guesses back into the grid
          if (gx < 0) gx = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy < 0) gy = 0;
          if (gy > LengthY-2) gy = LengthY-2;
          if ( (gx == ogx) && (gy == ogy) && (test2) ) {
            // Found correct grid triangle
            // Solve the point with the reverse interpolation
            grid[0][i] = ((value[0][i]-v0[0])*(v2[1]-v0[1])
                        + (v0[1]-value[1][i])*(v2[0]-v0[0]))
                       / ((v1[0]-v0[0])*(v2[1]-v0[1])
                        + (v0[1]-v1[1])*(v2[0]-v0[0])) + gx;
            grid[1][i] = ((value[0][i]-v0[0])*(v1[1]-v0[1])
                        + (v0[1]-value[1][i])*(v1[0]-v0[0]))
                       / ((v2[0]-v0[0])*(v1[1]-v0[1])
                        + (v0[1]-v2[1])*(v1[0]-v0[0])) + gy;
            break;
          }
          else {
            lowertri = false;
          }
        }

        // check the UPPER triangle of the grid box
        else {
          double[] bc = {v3[0]-v1[0], v3[1]-v1[1]};
          double[] cd = {v2[0]-v3[0], v2[1]-v3[1]};
          double[] cp = {value[0][i]-v3[0], value[1][i]-v3[1]};
          double tval1 = bc[0]*bp[1]-bc[1]*bp[0];
          double tval2 = cd[0]*cp[1]-cd[1]*cp[0];
          double tval3 = bd[0]*dp[1]-bd[1]*dp[0];
          boolean test1 = (tval1 == 0) || ((tval1 > 0) == Pos);
          boolean test2 = (tval2 == 0) || ((tval2 > 0) == Pos);
          boolean test3 = (tval3 == 0) || ((tval3 < 0) == Pos);
          int ogx = gx;
          int ogy = gy;
          if (!test1 && !test3) {      // Go UP & RIGHT
            gx++;
            gy--;
          }
          else if (!test2 && !test3) { // Go DOWN & LEFT
            gx--;
            gy++;
          }
          else if (!test1 && !test2) { // Go DOWN & RIGHT
            gx++;
            gy++;
          }
          else if (!test1) {           // Go RIGHT
            gx++;
          }
          else if (!test2) {           // Go DOWN
            gy++;
          }
          // Snap guesses back into the grid
          if (gx < 0) gx = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy < 0) gy = 0;
          if (gy > LengthY-2) gy = LengthY-2;
          if ( (gx == ogx) && (gy == ogy) && (test3) ) {
            // Found correct grid triangle
            // Solve the point with the reverse interpolation
            grid[0][i] = ((v3[0]-value[0][i])*(v1[1]-v3[1])
                        + (value[1][i]-v3[1])*(v1[0]-v3[0]))
                       / ((v2[0]-v3[0])*(v1[1]-v3[1])
                        - (v2[1]-v3[1])*(v1[0]-v3[0])) + gx + 1;
            grid[1][i] = ((v2[1]-v3[1])*(v3[0]-value[0][i])
                        + (v2[0]-v3[0])*(value[1][i]-v3[1]))
                       / ((v1[0]-v3[0])*(v2[1]-v3[1])
                        - (v2[0]-v3[0])*(v1[1]-v3[1])) + gy + 1;
            break;
          }
          else {
            lowertri = true;
          }
        }
        if ( (grid[0][i] >= LengthX-0.5) || (grid[1][i] >= LengthY-0.5)
          || (grid[0][i] <= -0.5) || (grid[1][i] <= -0.5) ) {
          grid[0][i] = grid[1][i] = Double.NaN;
        }
      }
    }
    return grid;
  }

  /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
  public void doubleToInterp(double[][] value, int[][] indices,
    double[][] weights) throws VisADException
  {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded2DDoubleSet.doubleToInterp: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length; // number of values
    if (indices.length != length) {
      throw new SetException("Gridded2DDoubleSet.valueToInterp: indices length " +
                             indices.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    if (weights.length != length) {
      throw new SetException("Gridded2DDoubleSet.valueToInterp: weights length " +
                             weights.length +
                             " doesn't match value[0] length " +
                             value[0].length);
    }
    // convert value array to grid coord array
    double[][] grid = doubleToGrid(value);

    int i, j, k; // loop indices
    int lis; // temporary length of is & cs
    int length_is; // final length of is & cs, varies by i
    int isoff; // offset along one grid dimension
    double a, b; // weights along one grid dimension; a + b = 1.0
    int[] is; // array of indices, becomes part of indices
    double[] cs; // array of coefficients, become part of weights

    int base; // base index, as would be returned by valueToIndex
    int[] l = new int[ManifoldDimension]; // integer 'factors' of base
    // fractions with l; -0.5 <= c <= 0.5
    double[] c = new double[ManifoldDimension];

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
                                 ((double) l[ManifoldDimension-1]);
        if (!((l[ManifoldDimension-1] == 0 && c[ManifoldDimension-1] <= 0.0) ||
              (l[ManifoldDimension-1] == Lengths[ManifoldDimension-1] - 1 &&
               c[ManifoldDimension-1] >= 0.0))) {
          // only interp along ManifoldDimension-1
          // if between two valid grid coords
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
          if (l[j] == Lengths[j]) l[j]--; // WLH 23 Dec 99
          c[j] = grid[j][i] - ((double) l[j]);
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
        cs = new double[length_is];
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
            // double is & cs; adjust new offsets; split weights
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


  // Miscellaneous Set methods that must be overridden
  // (this code is duplicated throughout all *DoubleSet classes)

  void init_doubles(double[][] samples, boolean copy)
       throws VisADException {
    if (samples.length != DomainDimension) {
      throw new SetException("Gridded2DDoubleSet.init_doubles: samples " +
                             " dimension " + samples.length +
                             " not equal to domain dimension " +
                             DomainDimension);
    }
    if (Length == 0) {
      // Length set in init_lengths, but not called for IrregularSet
      Length = samples[0].length;
    }
    else {
      if (Length != samples[0].length) {
        throw new SetException("Gridded2DDoubleSet.init_doubles: " +
                               "samples[0] length " + samples[0].length +
                               " doesn't match expected length " + Length);
      }
    }
    // MEM
    if (copy) {
      Samples = new double[DomainDimension][Length];
    }
    else {
      Samples = samples;
    }
    for (int j=0; j<DomainDimension; j++) {
      if (samples[j].length != Length) {
        throw new SetException("Gridded2DDoubleSet.init_doubles: " +
                               "samples[" + j + "] length " +
                               samples[0].length +
                               " doesn't match expected length " + Length);
      }
      double[] samplesJ = samples[j];
      double[] SamplesJ = Samples[j];
      if (copy) {
        System.arraycopy(samplesJ, 0, SamplesJ, 0, Length);
      }
      Low[j] = Double.POSITIVE_INFINITY;
      Hi[j] = Double.NEGATIVE_INFINITY;
      double sum = 0.0f;
      for (int i=0; i<Length; i++) {
        if (SamplesJ[i] == SamplesJ[i] && !Double.isInfinite(SamplesJ[i])) {
          if (SamplesJ[i] < Low[j]) Low[j] = SamplesJ[i];
          if (SamplesJ[i] > Hi[j]) Hi[j] = SamplesJ[i];
        }
        else {
          SamplesJ[i] = Double.NaN;
        }
        sum += SamplesJ[i];
      }
      if (SetErrors[j] != null ) {
        SetErrors[j] =
          new ErrorEstimate(SetErrors[j].getErrorValue(), sum / Length,
                            Length, SetErrors[j].getUnit());
      }
      super.Low[j] = (float) Low[j];
      super.Hi[j] = (float) Hi[j];
    }
  }

  public void cram_missing(boolean[] range_select) {
    int n = Math.min(range_select.length, Samples[0].length);
    for (int i=0; i<n; i++) {
      if (!range_select[i]) Samples[0][i] = Double.NaN;
    }
  }

  public boolean isMissing() {
    return (Samples == null);
  }

  public boolean equals(Object set) {
    if (!(set instanceof Gridded2DDoubleSet) || set == null) return false;
    if (this == set) return true;
    if (testNotEqualsCache((Set) set)) return false;
    if (testEqualsCache((Set) set)) return true;
    if (!equalUnitAndCS((Set) set)) return false;
    try {
      int i, j;
      if (DomainDimension != ((Gridded2DDoubleSet) set).getDimension() ||
          ManifoldDimension !=
            ((Gridded2DDoubleSet) set).getManifoldDimension() ||
          Length != ((Gridded2DDoubleSet) set).getLength()) return false;
      for (j=0; j<ManifoldDimension; j++) {
        if (Lengths[j] != ((Gridded2DDoubleSet) set).getLength(j)) {
          return false;
        }
      }
      // Sets are immutable, so no need for 'synchronized'
      double[][] samples = ((Gridded2DDoubleSet) set).getDoubles(false);
      if (Samples != null && samples != null) {
        for (j=0; j<DomainDimension; j++) {
          for (i=0; i<Length; i++) {
            if (Samples[j][i] != samples[j][i]) {
              addNotEqualsCache((Set) set);
              return false;
            }
          }
        }
      }
      else {
        double[][] this_samples = getDoubles(false);
        if (this_samples == null) {
          if (samples != null) {
            return false;
          }
        } else if (samples == null) {
          return false;
        } else {
          for (j=0; j<DomainDimension; j++) {
            for (i=0; i<Length; i++) {
              if (this_samples[j][i] != samples[j][i]) {
                addNotEqualsCache((Set) set);
                return false;
              }
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
   * Clones this instance.
   *
   * @return                    A clone of this instance.
   */
  public Object clone() {
    Gridded2DDoubleSet clone = (Gridded2DDoubleSet)super.clone();
    
    if (Samples != null) {
      /*
       * The Samples array is cloned because getDoubles(false) allows clients
       * to manipulate the array and the general clone() contract forbids
       * cross-clone contamination.
       */
      clone.Samples = (double[][])Samples.clone();
      for (int i = 0; i < Samples.length; i++)
        clone.Samples[i] = (double[])Samples[i].clone();
    }
    
    return clone;
  }

  public Object cloneButType(MathType type) throws VisADException {
    if (ManifoldDimension == 2) {
      return new Gridded2DDoubleSet(type, Samples, LengthX, LengthY,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else {
      return new Gridded2DDoubleSet(type, Samples, LengthX,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
  }
/* WLH 3 April 2003
  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded2DDoubleSet(type, Samples, Length,
      DomainCoordinateSystem, SetUnits, SetErrors);
  }
*/
}

