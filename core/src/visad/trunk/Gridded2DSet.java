//
// Gridded2DSet.java
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

import java.io.*;

/**
   Gridded2DSet represents a finite set of samples of R^2.<P>
*/
public class Gridded2DSet extends GriddedSet {

  int LengthX, LengthY;
  float LowX, HiX, LowY, HiY;

  /** a 2-D set whose topology is a lengthX x lengthY grid, with
      null errors, CoordinateSystem and Units are defaults from type */
  public Gridded2DSet(MathType type, float[][] samples, int lengthX, int lengthY)
         throws VisADException {
    this(type, samples, lengthX, lengthY, null, null, null);
  }

  /** a 2-D set whose topology is a lengthX x lengthY grid;
      samples array is organized float[2][number_of_samples] where
      lengthX * lengthY = number_of_samples; samples must form a
      non-degenerate 2-D grid (no bow-tie-shaped grid boxes); the
      X component increases fastest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded2DSet(MathType type, float[][] samples, int lengthX, int lengthY,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, lengthY, coord_sys, units, errors,
         true, true);
  }

  public Gridded2DSet(MathType type, float[][] samples, int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, samples, lengthX, lengthY, coord_sys, units, errors,
         true, true);
  }

  public Gridded2DSet(MathType type, float[][] samples, int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy, boolean test)
               throws VisADException {
    super(type, samples, make_lengths(lengthX, lengthY), coord_sys,
          units, errors, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LengthY = Lengths[1];

    if (Samples != null && Lengths[0] > 1 && Lengths[1] > 1) {
      Pos = ( (Samples[0][1]-Samples[0][0])
               *(Samples[1][LengthX+1]-Samples[1][1])
              - (Samples[1][1]-Samples[1][0])
               *(Samples[0][LengthX+1]-Samples[0][1]) > 0);

      if (test) {
        for (int i=0; i<Length; i++) {
          if (Samples[0][i] != Samples[0][i]) {
            throw new SetException(
             "Gridded2DSet: samples value #" + i + " may not be missing");
          }
        }

        // Samples consistency test
        for (int j=0; j<LengthY-1; j++) {
          for (int i=0; i<LengthX-1; i++) {
            float[] v00 = new float[2];
            float[] v10 = new float[2];
            float[] v01 = new float[2];
            float[] v11 = new float[2];
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
/*
System.out.println("Samples[0][1] = " + Samples[0][1] +
                   " Samples[0][0] = " + Samples[0][0] +
                   " Samples[1][LengthX+1] = " + Samples[1][LengthX+1] +
                   " Samples[1][1] = " + Samples[1][1]);
System.out.println("Samples[1][1] = " + Samples[1][1] +
                   " Samples[1][0] = " + Samples[1][0] +
                   " Samples[0][LengthX+1] = " + Samples[0][LengthX+1] +
                   " Samples[0][1] = " + Samples[0][1]);
System.out.println("v00[]=Samples[]["+(j*LengthX+i)+"] " +
                   "v10[]=Samples[]["+(j*LengthX+i+1)+"] " +
                   "v01[]=Samples[]["+((j+1)*LengthX+i)+"] " +
                   "v11[]=Samples[]["+((j+1)*LengthX+i+1)+"]");
System.out.println("Pos = " + Pos);
System.out.println("1st = " + ( (v10[0]-v00[0])*(v11[1]-v10[1])
                              - (v10[1]-v00[1])*(v11[0]-v10[0]) ) +
                  " 2nd = " + ( (v11[0]-v10[0])*(v01[1]-v11[1])
                              - (v11[1]-v10[1])*(v01[0]-v11[0]) ) +
                  " 3rd = " + ( (v01[0]-v11[0])*(v00[1]-v01[1])
                              - (v01[1]-v11[1])*(v00[0]-v01[0]) ) +
                  " 4th = " + ( (v00[0]-v01[0])*(v10[1]-v00[1])
                              - (v00[1]-v01[1])*(v10[0]-v00[0]) ) );
*/
              throw new SetException(
               "Gridded2DSet: samples do not form a valid grid ("+i+","+j+")");
            }
          }
        }
      } // end if (test)
    }
  }

  /** a 2-D set with manifold dimension = 1, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded2DSet(MathType type, float[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null);
  }

  /** a 2-D set with manifold dimension = 1; samples array is
      organized float[2][number_of_samples] where lengthX =
      number_of_samples; no geometric constraint on samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded2DSet(MathType type, float[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, coord_sys, units, errors, true);
  }

  public Gridded2DSet(MathType type, float[][] samples, int lengthX,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    super(type, samples, Gridded1DSet.make_lengths(lengthX),
          coord_sys, units, errors, copy);

    if (DomainDimension != 2) {
      throw new SetException("Gridded2DSet Domain dimension" +
                             " should be 2, not " + DomainDimension);
    }

    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];

    // no Samples consistency test
  }

  static int[] make_lengths(int lengthX, int lengthY) {
    int[] lens = new int[2];
    lens[0] = lengthX;
    lens[1] = lengthY;
    return lens;
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear2DSet.indexToValue
      int indexX, indexY;
      float[][] grid = new float[ManifoldDimension][length];

      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          indexX = index[i] % LengthX;
          indexY = index[i] / LengthX;
        }
        else {
          indexX = -1;
          indexY = -1;
        }
        grid[0][i] = (float) indexX;
        grid[1][i] = (float) indexY;
      }
      return gridToValue(grid);
    }
    else {
      float[][] values = new float[2][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
          values[1][i] = Samples[1][index[i]];
        }
        else {
          values[0][i] = Float.NaN;
          values[1][i] = Float.NaN;
        }
      }
      return values;
    }
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded2DSet.valueToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length;
    int[] index = new int[length];

    float[][] grid = valueToGrid(value);
    float[] grid0 = grid[0];
    float[] grid1 = grid[1];
    float g0, g1;
    for (int i=0; i<length; i++) {
      g0 = grid0[i];
      g1 = grid1[i];
/* WLH 24 Oct 97
      index[i] = (Float.isNaN(g0) || Float.isNaN(g1)) ? -1 :
*/
      // test for missing
      index[i] = (g0 != g0 || g1 != g1) ? -1 :
                 ((int) (g0 + 0.5)) + LengthX * ((int) (g1 + 0.5));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != ManifoldDimension) {
      throw new SetException("Gridded2DSet.gridToValue: grid dimension " +
                             grid.length +
                             " not equal to Manifold dimension " +
                             ManifoldDimension);
    }
    if (ManifoldDimension < 2) {
      throw new SetException("Gridded2DSet.gridToValue: Manifold dimension " +
                             "must be 2, not " + ManifoldDimension);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded2DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    float[][] value = new float[2][length];
    for (int i=0; i<length; i++) {
      // let gx and gy by the current grid values
      float gx = grid[0][i];
      float gy = grid[1][i];
      if ( (gx < -0.5)        || (gy < -0.5) ||
           (gx > LengthX-0.5) || (gy > LengthY-0.5) ) {
        value[0][i] = value[1][i] = Float.NaN;
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
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded2DSet.valueToGrid: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    if (ManifoldDimension < 2) {
      throw new SetException("Gridded2DSet.valueToGrid: Manifold dimension " +
                             "must be 2, not " + ManifoldDimension);
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded2DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = Math.min(value[0].length, value[1].length);
    float[][] grid = new float[ManifoldDimension][length];

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
      if ( (i != 0) && (Float.isNaN(grid[0][i-1])) )
*/
      if (Length == 1) {
        if (Float.isNaN(value[0][i]) || Float.isNaN(value[1][i])) {
           grid[0][i] = grid[1][i] = Float.NaN;
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
      grid[0][i] = grid[1][i] = Float.NaN;
      for (int itnum=0; itnum<2*(LengthX+LengthY); itnum++) {
        // define the four vertices of the current grid box
        float[] v0 = {Samples[0][gy*LengthX+gx],
                       Samples[1][gy*LengthX+gx]};
        float[] v1 = {Samples[0][gy*LengthX+gx+1],
                       Samples[1][gy*LengthX+gx+1]};
        float[] v2 = {Samples[0][(gy+1)*LengthX+gx],
                       Samples[1][(gy+1)*LengthX+gx]};
        float[] v3 = {Samples[0][(gy+1)*LengthX+gx+1],
                       Samples[1][(gy+1)*LengthX+gx+1]};

        // Both cases use diagonal D-B and point distances P-B and P-D
        float[] bd = {v2[0]-v1[0], v2[1]-v1[1]};
        float[] bp = {value[0][i]-v1[0], value[1][i]-v1[1]};
        float[] dp = {value[0][i]-v2[0], value[1][i]-v2[1]};

        // check the LOWER triangle of the grid box
        if (lowertri) {
          float[] ab = {v1[0]-v0[0], v1[1]-v0[1]};
          float[] da = {v0[0]-v2[0], v0[1]-v2[1]};
          float[] ap = {value[0][i]-v0[0], value[1][i]-v0[1]};
          float tval1 = ab[0]*ap[1]-ab[1]*ap[0];
          float tval2 = bd[0]*bp[1]-bd[1]*bp[0];
          float tval3 = da[0]*dp[1]-da[1]*dp[0];
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
          float[] bc = {v3[0]-v1[0], v3[1]-v1[1]};
          float[] cd = {v2[0]-v3[0], v2[1]-v3[1]};
          float[] cp = {value[0][i]-v3[0], value[1][i]-v3[1]};
          float tval1 = bc[0]*bp[1]-bc[1]*bp[0];
          float tval2 = cd[0]*cp[1]-cd[1]*cp[0];
          float tval3 = bd[0]*dp[1]-bd[1]*dp[0];
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
      }
      if ( (grid[0][i] >= LengthX-0.5) || (grid[1][i] >= LengthY-0.5)
        || (grid[0][i] <= -0.5) || (grid[1][i] <= -0.5) ) {
        grid[0][i] = grid[1][i] = Float.NaN;
      }
    }
    return grid;
  }

  public Object cloneButType(MathType type) throws VisADException {
    if (ManifoldDimension == 2) {
      return new Gridded2DSet(type, Samples, LengthX, LengthY,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else {
      return new Gridded2DSet(type, Samples, LengthX,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
  }

  /* run 'java visad.Gridded2DSet < formatted_input_stream'
     to test the Gridded2DSet class */
  public static void main(String[] argv) throws VisADException {

    // Define input stream
    InputStreamReader inStr = new InputStreamReader(System.in);

    // Define temporary integer array
    int[] ints = new int[80];
    try {
      ints[0] = inStr.read();
    }
    catch(Exception e) {
      System.out.println("Gridded2DSet: "+e);
    }
    int l = 0;
    while (ints[l] != 10) {
      try {
        ints[++l] = inStr.read();
      }
      catch (Exception e) {
        System.out.println("Gridded2DSet: "+e);
      }
    }
    // convert array of integers to array of characters
    char[] chars = new char[l];
    for (int i=0; i<l; i++) {
      chars[i] = (char) ints[i];
    }
    int num_coords = Integer.parseInt(new String(chars));

    // num_coords should be a nice round number
    if (num_coords % 4 != 0) {
      System.out.println("Gridded2DSet: input coordinates must be divisible by 4"
                                     +" for main function testing routines.");
    }

    // Define size of Samples array
    float[][] samp = new float[2][num_coords];
    System.out.println("num_dimensions = 2, num_coords = "+num_coords+"\n");

    // Skip blank line
    try {
      ints[0] = inStr.read();
    }
    catch (Exception e) {
      System.out.println("Gridded2DSet: "+e);
    }

    for (int c=0; c<num_coords; c++) {
      for (int d=0; d<2; d++) {
        l = 0;
        try {
          ints[0] = inStr.read();
        }
        catch (Exception e) {
          System.out.println("Gridded2DSet: "+e);
        }
        while ( (ints[l] != 32) && (ints[l] != 10) ) {
          try {
            ints[++l] = inStr.read();
          }
          catch (Exception e) {
            System.out.println("Gridded2DSet: "+e);
          }
        }
        chars = new char[l];
        for (int i=0; i<l; i++) {
          chars[i] = (char) ints[i];
        }
        samp[d][c] = (Float.valueOf(new String(chars))).floatValue();
      }
    }

    // do EOF stuff
    try {
      inStr.close();
    }
    catch (Exception e) {
      System.out.println("Gridded2DSet: "+e);
    }

    // Set up instance of Gridded2DSet
    RealType vis_xcoord = RealType.getRealType("xcoord");
    RealType vis_ycoord = RealType.getRealType("ycoord");
    RealType[] vis_array = {vis_xcoord, vis_ycoord};
    RealTupleType vis_tuple = new RealTupleType(vis_array);
    Gridded2DSet gSet2D = new Gridded2DSet(vis_tuple, samp, num_coords/4, 4);

    System.out.println("Lengths = " + num_coords/4 + " 4 " + " wedge = ");
    int[] wedge = gSet2D.getWedge();
    for (int i=0; i<wedge.length; i++) System.out.println(" " + wedge[i]);


    // print out Samples information
    System.out.println("Samples ("+gSet2D.LengthX+" x "+gSet2D.LengthY+"):");
    for (int i=0; i<gSet2D.LengthX*gSet2D.LengthY; i++) {
      System.out.println("#"+i+":\t"+gSet2D.Samples[0][i]+", "+gSet2D.Samples[1][i]);
    }

    // Test gridToValue function
    System.out.println("\ngridToValue test:");
    int myLengthX = gSet2D.LengthX+1;
    int myLengthY = gSet2D.LengthY+1;
    float[][] myGrid = new float[2][myLengthX*myLengthY];
    for (int j=0; j<myLengthY; j++) {
      for (int i=0; i<myLengthX; i++) {
        myGrid[0][j*myLengthX+i] = i-0.5f;
        myGrid[1][j*myLengthX+i] = j-0.5f;
        if (myGrid[0][j*myLengthX+i] < 0) {
          myGrid[0][j*myLengthX+i] += 0.1;
        }
        if (myGrid[0][j*myLengthX+i] > gSet2D.LengthX-1) {
          myGrid[0][j*myLengthX+i] -= 0.1;
        }
        if (myGrid[1][j*myLengthX+i] < 0) {
          myGrid[1][j*myLengthX+i] += 0.1;
        }
        if (myGrid[1][j*myLengthX+i] > gSet2D.LengthY-1) {
          myGrid[1][j*myLengthX+i] -= 0.1;
        }
      }
    }
    float[][] myValue = gSet2D.gridToValue(myGrid);
    for (int i=0; i<myLengthX*myLengthY; i++) {
      System.out.println("("+((float) Math.round(1000000
                                      *myGrid[0][i]) /1000000)+", "
                            +((float) Math.round(1000000
                                      *myGrid[1][i]) /1000000)+")\t-->  "
                            +((float) Math.round(1000000
                                      *myValue[0][i]) /1000000)+", "
                            +((float) Math.round(1000000
                                      *myValue[1][i]) /1000000));
    }

    // Test valueToGrid function
    System.out.println("\nvalueToGrid test:");
    float[][] gridTwo = gSet2D.valueToGrid(myValue);
    for (int i=0; i<gridTwo[0].length; i++) {
      System.out.println(((float) Math.round(1000000
                                  *myValue[0][i]) /1000000)+", "
                        +((float) Math.round(1000000
                                  *myValue[1][i]) /1000000)+"\t-->  ("
                        +((float) Math.round(1000000
                                  *gridTwo[0][i]) /1000000)+", "
                        +((float) Math.round(1000000
                                  *gridTwo[1][i]) /1000000)+")");
    }
    System.out.println();

  }

/* Here's the output with sample file Gridded2D.txt:

iris 26% java visad.Gridded2DSet < Gridded2D.txt
num_dimensions = 2, num_coords = 20

Lengths = 5 4  wedge =
 0
 1
 2
 3
 4
 9
 8
 7
 6
 5
. . .

Samples (5 x 4):
#0:     13.298374, 40.239864
#1:     19.182746, 40.097643
. . .
#18:    40.213987, 19.230974
#19:    46.293732, 18.239872

gridToValue test:
(-0.4, -0.4)    -->  11.100636, 43.15516
(0.5, -0.4)     -->  16.396571, 43.027161
. . .
(3.5, 3.4)      -->  44.087403, 15.515757
(4.4, 3.4)      -->  49.559174, 14.623765

valueToGrid test:
11.100636, 43.15516     -->  (-0.4, -0.4)
16.396571, 43.027161    -->  (0.5, -0.4)
. . .
44.087403, 15.515757    -->  (3.5, 3.4)
49.559174, 14.623765    -->  (4.4, 3.4)

iris 27%

*/

}

