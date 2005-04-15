//
// Gridded3DSet.java
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
   Gridded3DSet represents a finite set of samples of R^3.<P>
*/
public class Gridded3DSet extends GriddedSet {

  int LengthX, LengthY, LengthZ;

  float LowX, HiX, LowY, HiY, LowZ, HiZ;

  /** a 3-D set whose topology is a lengthX x lengthY x lengthZ
      grid, with null errors, CoordinateSystem and Units are
      defaults from type */
  public Gridded3DSet(MathType type, float[][] samples, int lengthX,
                      int lengthY, int lengthZ) throws VisADException {
    this(type, samples, lengthX, lengthY, lengthZ, null, null, null);
  }

  /** a 3-D set whose topology is a lengthX x lengthY x lengthZ
      grid; samples array is organized float[3][number_of_samples]
      where lengthX * lengthY * lengthZ = number_of_samples;
      samples must form a non-degenerate 3-D grid (no bow-tie-shaped
      grid cubes);  the X component increases fastest and the Z
      component slowest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DSet(MathType type, float[][] samples,
                      int lengthX, int lengthY, int lengthZ,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, lengthY, lengthZ, coord_sys,
         units, errors, true, true);
  }

  public Gridded3DSet(MathType type, float[][] samples,
               int lengthX, int lengthY, int lengthZ,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    this(type, samples, lengthX, lengthY, lengthZ, coord_sys,
         units, errors, copy, true);
  }

  public Gridded3DSet(MathType type, float[][] samples,
               int lengthX, int lengthY, int lengthZ,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy,
               boolean test) throws VisADException {
    super(type, samples, make_lengths(lengthX, lengthY, lengthZ),
          coord_sys, units, errors, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LengthY = Lengths[1];
    LowZ = Low[2];
    HiZ = Hi[2];
    LengthZ = Lengths[2];

    if (Samples != null &&
        Lengths[0] > 1 && Lengths[1] > 1 && Lengths[2] > 1) {
      for (int i=0; i<Length; i++) {
        if (Samples[0][i] != Samples[0][i]) {
          throw new SetException(
           "Gridded3DSet: samples values may not be missing");
        }
      }
    // Samples consistency test
    float[] t000 = new float[3];
    float[] t100 = new float[3];
    float[] t010 = new float[3];
    float[] t001 = new float[3];
    float[] t110 = new float[3];
    float[] t101 = new float[3];
    float[] t011 = new float[3];
    float[] t111 = new float[3];
    for (int v=0; v<3; v++) {
      t000[v] = Samples[v][0];
      t100[v] = Samples[v][1];
      t010[v] = Samples[v][LengthX];
      t001[v] = Samples[v][LengthY*LengthX];
      t110[v] = Samples[v][LengthX+1];
      t101[v] = Samples[v][LengthY*LengthX+1];
      t011[v] = Samples[v][(LengthY+1)*LengthX];
      t111[v] = Samples[v][(LengthY+1)*LengthX+1];
    }
    Pos = (  ( (t100[1]-t000[1])*(t101[2]-t100[2])
             - (t100[2]-t000[2])*(t101[1]-t100[1]) )
              *(t110[0]-t100[0])  )
        + (  ( (t100[2]-t000[2])*(t101[0]-t100[0])
             - (t100[0]-t000[0])*(t101[2]-t100[2]) )
              *(t110[1]-t100[1])  )
        + (  ( (t100[0]-t000[0])*(t101[1]-t100[1])
             - (t100[1]-t000[1])*(t101[0]-t100[0]) )
              *(t110[2]-t100[2])  ) > 0;

      if (test) {
          float[] v000 = new float[3];
          float[] v100 = new float[3];
          float[] v010 = new float[3];
          float[] v001 = new float[3];
          float[] v110 = new float[3];
          float[] v101 = new float[3];
          float[] v011 = new float[3];
          float[] v111 = new float[3];

        for (int k=0; k<LengthZ-1; k++) {
          for (int j=0; j<LengthY-1; j++) {
            for (int i=0; i<LengthX-1; i++) {
              for (int v=0; v<3; v++) {
                int zadd = LengthY*LengthX;
                int base = k*zadd + j*LengthX + i;
                v000[v] = Samples[v][base];
                v100[v] = Samples[v][base+1];
                v010[v] = Samples[v][base+LengthX];
                v001[v] = Samples[v][base+zadd];
                v110[v] = Samples[v][base+LengthX+1];
                v101[v] = Samples[v][base+zadd+1];
                v011[v] = Samples[v][base+zadd+LengthX];
                v111[v] = Samples[v][base+zadd+LengthX+1];
              }
              if (((  ( (v100[1]-v000[1])*(v101[2]-v100[2])    // test 1
                      - (v100[2]-v000[2])*(v101[1]-v100[1]) )
                       *(v110[0]-v100[0])  )
                 + (  ( (v100[2]-v000[2])*(v101[0]-v100[0])
                      - (v100[0]-v000[0])*(v101[2]-v100[2]) )
                       *(v110[1]-v100[1])  )
                 + (  ( (v100[0]-v000[0])*(v101[1]-v100[1])
                      - (v100[1]-v000[1])*(v101[0]-v100[0]) )
                       *(v110[2]-v100[2])  ) > 0 != Pos)
               || ((  ( (v101[1]-v100[1])*(v001[2]-v101[2])    // test 2
                      - (v101[2]-v100[2])*(v001[1]-v101[1]) )
                       *(v111[0]-v101[0])  )
                 + (  ( (v101[2]-v100[2])*(v001[0]-v101[0])
                      - (v101[0]-v100[0])*(v001[2]-v101[2]) )
                       *(v111[1]-v101[1])  )
                 + (  ( (v101[0]-v100[0])*(v001[1]-v101[1])
                      - (v101[1]-v100[1])*(v001[0]-v101[0]) )
                       *(v111[2]-v101[2])  ) > 0 != Pos)
               || ((  ( (v001[1]-v101[1])*(v000[2]-v001[2])    // test 3
                      - (v001[2]-v101[2])*(v000[1]-v001[1]) )
                       *(v011[0]-v001[0])  )
                 + (  ( (v001[2]-v101[2])*(v000[0]-v001[0])
                      - (v001[0]-v101[0])*(v000[2]-v001[2]) )
                       *(v011[1]-v001[1])  )
                 + (  ( (v001[0]-v101[0])*(v000[1]-v001[1])
                      - (v001[1]-v101[1])*(v000[0]-v001[0]) )
                       *(v011[2]-v001[2])  ) > 0 != Pos)
               || ((  ( (v000[1]-v001[1])*(v100[2]-v000[2])    // test 4
                      - (v000[2]-v001[2])*(v100[1]-v000[1]) )
                       *(v010[0]-v000[0])  )
                 + (  ( (v000[2]-v001[2])*(v100[0]-v000[0])
                      - (v000[0]-v001[0])*(v100[2]-v000[2]) )
                       *(v010[1]-v000[1])  )
                 + (  ( (v000[0]-v001[0])*(v100[1]-v000[1])
                      - (v000[1]-v001[1])*(v100[0]-v000[0]) )
                       *(v010[2]-v000[2])  ) > 0 != Pos)
               || ((  ( (v110[1]-v111[1])*(v010[2]-v110[2])    // test 5
                      - (v110[2]-v111[2])*(v010[1]-v110[1]) )
                       *(v100[0]-v110[0])  )
                 + (  ( (v110[2]-v111[2])*(v010[0]-v110[0])
                      - (v110[0]-v111[0])*(v010[2]-v110[2]) )
                       *(v100[1]-v110[1])  )
                 + (  ( (v110[0]-v111[0])*(v010[1]-v110[1])
                      - (v110[1]-v111[1])*(v010[0]-v110[0]) )
                       *(v100[2]-v110[2])  ) > 0 != Pos)
               || ((  ( (v111[1]-v011[1])*(v110[2]-v111[2])    // test 6
                      - (v111[2]-v011[2])*(v110[1]-v111[1]) )
                       *(v101[0]-v111[0])  )
                 + (  ( (v111[2]-v011[2])*(v110[0]-v111[0])
                      - (v111[0]-v011[0])*(v110[2]-v111[2]) )
                       *(v101[1]-v111[1])  )
                 + (  ( (v111[0]-v011[0])*(v110[1]-v111[1])
                      - (v111[1]-v011[1])*(v110[0]-v111[0]) )
                       *(v101[2]-v111[2])  ) > 0 != Pos)
               || ((  ( (v011[1]-v010[1])*(v111[2]-v011[2])    // test 7
                      - (v011[2]-v010[2])*(v111[1]-v011[1]) )
                       *(v001[0]-v011[0])  )
                 + (  ( (v011[2]-v010[2])*(v111[0]-v011[0])
                      - (v011[0]-v010[0])*(v111[2]-v011[2]) )
                       *(v001[1]-v011[1])  )
                 + (  ( (v011[0]-v010[0])*(v111[1]-v011[1])
                      - (v011[1]-v010[1])*(v111[0]-v011[0]) )
                       *(v001[2]-v011[2])  ) > 0 != Pos)
               || ((  ( (v010[1]-v110[1])*(v011[2]-v010[2])    // test 8
                      - (v010[2]-v110[2])*(v011[1]-v010[1]) )
                       *(v000[0]-v010[0])  )
                 + (  ( (v010[2]-v110[2])*(v011[0]-v010[0])
                      - (v010[0]-v110[0])*(v011[2]-v010[2]) )
                       *(v000[1]-v010[1])  )
                 + (  ( (v010[0]-v110[0])*(v011[1]-v010[1])
                      - (v010[1]-v110[1])*(v011[0]-v010[0]) )
                       *(v000[2]-v010[2])  ) > 0 != Pos)) {
                throw new SetException("Gridded3DSet: samples do not form "
                                       +"a valid grid ("+i+","+j+","+k+")");
              }
            }
          }
        }
      } // end if (test)
    }
  }

  /** a 3-D set with manifold dimension = 2, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded3DSet(MathType type, float[][] samples, int lengthX,
                      int lengthY) throws VisADException {
    this(type, samples, lengthX, lengthY, null, null, null);
  }

  /** a 3-D set with manifold dimension = 2; samples array is
      organized float[3][number_of_samples] where lengthX * lengthY
      = number_of_samples; no geometric constraint on samples; the
      X component increases fastest in the second index of samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DSet(MathType type, float[][] samples,
                      int lengthX, int lengthY,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, lengthY, coord_sys, units,
         errors, true);
  }

  public Gridded3DSet(MathType type, float[][] samples,
               int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    super(type, samples, Gridded2DSet.make_lengths(lengthX, lengthY),
          coord_sys, units, errors, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LengthY = Lengths[1];
    LowZ = Low[2];
    HiZ = Hi[2];

    // no Samples consistency test
  }

  /** a 3-D set with manifold dimension = 1, with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded3DSet(MathType type, float[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null);
  }

  /** a 3-D set with manifold dimension = 1; samples array is
      organized float[3][number_of_samples] where lengthX =
      number_of_samples; no geometric constraint on samples;
      coordinate_system and units must be compatible with defaults
      for type, or may be null; errors may be null */
  public Gridded3DSet(MathType type, float[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, coord_sys, units, errors, true);
  }

  public Gridded3DSet(MathType type, float[][] samples, int lengthX,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    super(type, samples, Gridded1DSet.make_lengths(lengthX),
          coord_sys, units, errors, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];
    LowY = Low[1];
    HiY = Hi[1];
    LowZ = Low[2];
    HiZ = Hi[2];

    // no Samples consistency test
  }

  static int[] make_lengths(int lengthX, int lengthY, int lengthZ) {
    int[] lens = new int[3];
    lens[0] = lengthX;
    lens[1] = lengthY;
    lens[2] = lengthZ;
    return lens;
  }


  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear3DSet.indexToValue
      int indexX, indexY, indexZ;
      int k;
      float[][] grid = new float[ManifoldDimension][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          indexX = index[i] % LengthX;
          k = index[i] / LengthX;
          indexY = k % LengthY;
          indexZ = k / LengthY;
        }
        else {
          indexX = -1;
          indexY = -1;
          indexZ = -1;
        }
        grid[0][i] = (float) indexX;
        grid[1][i] = (float) indexY;
        grid[2][i] = (float) indexZ;
      }
      return gridToValue(grid);
    }
    else {
      float[][] values = new float[3][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
          values[1][i] = Samples[1][index[i]];
          values[2][i] = Samples[2][index[i]];
        }
        else {
          values[0][i] = Float.NaN;
          values[1][i] = Float.NaN;
          values[2][i] = Float.NaN;
        }
      }
      return values;
    }
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(float[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded3DSet.valueToIndex: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    int length = value[0].length;
    int[] index = new int[length];

    float[][] grid = valueToGrid(value);
    float[] grid0 = grid[0];
    float[] grid1 = grid[1];
    float[] grid2 = grid[2];
    float g0, g1, g2;
    for (int i=0; i<length; i++) {
      g0 = grid0[i];
      g1 = grid1[i];
      g2 = grid2[i];
/* WLH 24 Oct 97
      index[i] = (Float.isNaN(g0)
               || Float.isNaN(g1)
               || Float.isNaN(g2)) ? -1 :
*/
      // test for missing
/* WLH 2 April 99
      index[i] = (g0 != g0 || g1 != g1 || g2 != g2) ? 1 :
*/
      index[i] = (g0 != g0 || g1 != g1 || g2 != g2) ? -1 :
                 ((int) (g0 + 0.5)) + LengthX*( ((int) (g1 + 0.5)) +
                  LengthY*((int) (g2 + 0.5)));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length != ManifoldDimension) {
      throw new SetException("Gridded3DSet.gridToValue: grid dimension " +
                             grid.length + " not equal to Manifold dimension " +
                             ManifoldDimension);
    }
    if (ManifoldDimension == 3) {
      return gridToValue3D(grid);
    }
    else if (ManifoldDimension == 2) {
      return gridToValue2D(grid);
    }
    else {
      throw new SetException("Gridded3DSet.gridToValue: ManifoldDimension " +
                             "must be 2 or 3");
    }
  }

  private float[][] gridToValue2D(float[][] grid) throws VisADException {
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2)) {
      throw new SetException("Gridded3DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    float[][] value = new float[3][length];
    for (int i=0; i<length; i++) {
      // let gx and gy by the current grid values
      float gx = grid[0][i];
      float gy = grid[1][i];
      if ( (gx < -0.5)        || (gy < -0.5) ||
           (gx > LengthX-0.5) || (gy > LengthY-0.5) ) {
        value[0][i] = value[1][i] = value[2][i] = Float.NaN;
      } else if (Length == 1) {
        value[0][i] = Samples[0][0];
        value[1][i] = Samples[1][0];
        value[2][i] = Samples[2][0];
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
          for (int j=0; j<3; j++) {
            value[j][i] = Samples[j][s[0][0]]
              + (gx-igx)*(Samples[j][s[1][0]]-Samples[j][s[0][0]])
              + (gy-igy)*(Samples[j][s[0][1]]-Samples[j][s[0][0]]);
          }
        }
        else {
          // point is in UPPER triangle
          for (int j=0; j<3; j++) {
            value[j][i] = Samples[j][s[1][1]]
              + (1+igx-gx)*(Samples[j][s[0][1]]-Samples[j][s[1][1]])
              + (1+igy-gy)*(Samples[j][s[1][0]]-Samples[j][s[1][1]]);
          }
        }
  /*
  for (int j=0; j<3; j++) {
    if (value[j][i] != value[j][i]) {
      System.out.println("gridToValue2D: bad Samples j = " + j + " gx, gy = " + gx +
                         " " + gy + " " + s[0][0] + " " + s[0][1] +
                         " " + s[1][0] + " " + s[1][1]);
    }
  }
  */
      }
    }
    return value;
  }

  private float[][] gridToValue3D(float[][] grid) throws VisADException {
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2)) {
      throw new SetException("Gridded3DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    length = Math.min(length, grid[2].length);
    float[][] value = new float[3][length];

    float[] A = new float[3];
    float[] B = new float[3];
    float[] C = new float[3];
    float[] D = new float[3];
    float[] E = new float[3];
    float[] F = new float[3];
    float[] G = new float[3];
    float[] H = new float[3];

    for (int i=0; i<length; i++) {
      // let gx, gy, and gz be the current grid values
      float gx = grid[0][i];
      float gy = grid[1][i];
      float gz = grid[2][i];
      if ( (gx < -0.5)        || (gy < -0.5)        || (gz < -0.5) ||
           (gx > LengthX-0.5) || (gy > LengthY-0.5) || (gz > LengthZ-0.5) ) {
        value[0][i] = value[1][i] = value[2][i] = Float.NaN;
      } else if (Length == 1) {
        value[0][i] = Samples[0][0];
        value[1][i] = Samples[1][0];
        value[2][i] = Samples[2][0];
      } else {
        // calculate closest integer variables
        int igx, igy, igz;
        if (gx < 0) igx = 0;
        else if (gx > LengthX-2) igx = LengthX - 2;
        else igx = (int) gx;
        if (gy < 0) igy = 0;
        else if (gy > LengthY-2) igy = LengthY - 2;
        else igy = (int) gy;
        if (gz < 0) igz = 0;
        else if (gz > LengthZ-2) igz = LengthZ - 2;
        else igz = (int) gz;
  
        // determine tetrahedralization type
        boolean evencube = ((igx+igy+igz) % 2 == 0);
  
        // calculate distances from integer grid point
        float s, t, u;
        if (evencube) {
          s = gx - igx;
          t = gy - igy;
          u = gz - igz;
        }
        else {
          s = 1 + igx - gx;
          t = 1 + igy - gy;
          u = 1 + igz - gz;
        }
  
        // Define vertices of grid box
        int zadd = LengthY*LengthX;
        int base = igz*zadd + igy*LengthX + igx;
        int ai = base+zadd;            // 0, 0, 1
        int bi = base+zadd+1;          // 1, 0, 1
        int ci = base+zadd+LengthX+1;  // 1, 1, 1
        int di = base+zadd+LengthX;    // 0, 1, 1
        int ei = base;                 // 0, 0, 0
        int fi = base+1;               // 1, 0, 0
        int gi = base+LengthX+1;       // 1, 1, 0
        int hi = base+LengthX;         // 0, 1, 0
        if (evencube) {
          A[0] = Samples[0][ai];
          A[1] = Samples[1][ai];
          A[2] = Samples[2][ai];
          B[0] = Samples[0][bi];
          B[1] = Samples[1][bi];
          B[2] = Samples[2][bi];
          C[0] = Samples[0][ci];
          C[1] = Samples[1][ci];
          C[2] = Samples[2][ci];
          D[0] = Samples[0][di];
          D[1] = Samples[1][di];
          D[2] = Samples[2][di];
          E[0] = Samples[0][ei];
          E[1] = Samples[1][ei];
          E[2] = Samples[2][ei];
          F[0] = Samples[0][fi];
          F[1] = Samples[1][fi];
          F[2] = Samples[2][fi];
          G[0] = Samples[0][gi];
          G[1] = Samples[1][gi];
          G[2] = Samples[2][gi];
          H[0] = Samples[0][hi];
          H[1] = Samples[1][hi];
          H[2] = Samples[2][hi];
        }
        else {
          G[0] = Samples[0][ai];
          G[1] = Samples[1][ai];
          G[2] = Samples[2][ai];
          H[0] = Samples[0][bi];
          H[1] = Samples[1][bi];
          H[2] = Samples[2][bi];
          E[0] = Samples[0][ci];
          E[1] = Samples[1][ci];
          E[2] = Samples[2][ci];
          F[0] = Samples[0][di];
          F[1] = Samples[1][di];
          F[2] = Samples[2][di];
          C[0] = Samples[0][ei];
          C[1] = Samples[1][ei];
          C[2] = Samples[2][ei];
          D[0] = Samples[0][fi];
          D[1] = Samples[1][fi];
          D[2] = Samples[2][fi];
          A[0] = Samples[0][gi];
          A[1] = Samples[1][gi];
          A[2] = Samples[2][gi];
          B[0] = Samples[0][hi];
          B[1] = Samples[1][hi];
          B[2] = Samples[2][hi];
        }
  
        // These tests determine which tetrahedron the point is in
        boolean test1 = (1 - s - t - u >= 0);
        boolean test2 = (s - t + u - 1 >= 0);
        boolean test3 = (t - s + u - 1 >= 0);
        boolean test4 = (s + t - u - 1 >= 0);
  
        // These cases handle grid coordinates off the grid
        // (Different tetrahedrons must be chosen accordingly)
        if ( (gx < 0) || (gx > LengthX-1)
          || (gy < 0) || (gy > LengthY-1)
          || (gz < 0) || (gz > LengthZ-1) ) {
          boolean OX, OY, OZ, MX, MY, MZ, LX, LY, LZ;
          OX = OY = OZ = MX = MY = MZ = LX = LY = LZ = false;
          if (igx == 0) OX = true;
          if (igy == 0) OY = true;
          if (igz == 0) OZ = true;
          if (igx == LengthX-2) LX = true;
          if (igy == LengthY-2) LY = true;
          if (igz == LengthZ-2) LZ = true;
          if (!OX && !LX) MX = true;
          if (!OY && !LY) MY = true;
          if (!OZ && !LZ) MZ = true;
          test1 = test2 = test3 = test4 = false;
          // 26 cases
          if (evencube) {
            if (!LX && !LY && !LZ) test1 = true;
            else if ( (LX && OY && MZ) || (MX && OY && LZ)
                   || (LX && MY && LZ) || (LX && OY && LZ)
                   || (MX && MY && LZ) || (LX && MY && MZ) ) test2 = true;
            else if ( (OX && LY && MZ) || (OX && MY && LZ)
                   || (MX && LY && LZ) || (OX && LY && LZ)
                                       || (MX && LY && MZ) ) test3 = true;
            else if ( (MX && LY && OZ) || (LX && MY && OZ)
                   || (LX && LY && MZ) || (LX && LY && OZ) ) test4 = true;
          }
          else {
            if (!OX && !OY && !OZ) test1 = true;
            else if ( (OX && MY && OZ) || (MX && LY && OZ)
                   || (OX && LY && MZ) || (OX && LY && OZ)
                   || (MX && MY && OZ) || (OX && MY && MZ) ) test2 = true;
            else if ( (LX && MY && OZ) || (MX && OY && OZ)
                   || (LX && OY && MZ) || (LX && OY && OZ)
                                       || (MX && OY && MZ) ) test3 = true;
            else if ( (OX && OY && MZ) || (OX && MY && OZ)
                   || (MX && OY && LZ) || (OX && OY && LZ) ) test4 = true;
          }
        }
        if (test1) {
          for (int j=0; j<3; j++) {
            value[j][i] = E[j] + s*(F[j]-E[j])
                               + t*(H[j]-E[j])
                               + u*(A[j]-E[j]);
          }
        }
        else if (test2) {
          for (int j=0; j<3; j++) {
            value[j][i] = B[j] + (1-s)*(A[j]-B[j])
                                   + t*(C[j]-B[j])
                               + (1-u)*(F[j]-B[j]);
          }
        }
        else if (test3) {
          for (int j=0; j<3; j++) {
            value[j][i] = D[j]     + s*(C[j]-D[j])
                               + (1-t)*(A[j]-D[j])
                               + (1-u)*(H[j]-D[j]);
          }
        }
        else if (test4) {
          for (int j=0; j<3; j++) {
            value[j][i] = G[j] + (1-s)*(H[j]-G[j])
                               + (1-t)*(F[j]-G[j])
                                   + u*(C[j]-G[j]);
          }
        }
        else {
          for (int j=0; j<3; j++) {
            value[j][i] = (H[j]+F[j]+A[j]-C[j])/2 + s*(C[j]+F[j]-H[j]-A[j])/2
                                                  + t*(C[j]-F[j]+H[j]-A[j])/2
                                                  + u*(C[j]-F[j]-H[j]+A[j])/2;
          }
        }
      }
    }
    return value;
  }

  // WLH 6 Dec 2001
  private int gx = -1;
  private int gy = -1;
  private int gz = -1;


  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {

    if (value.length < DomainDimension) {
      throw new SetException("Gridded3DSet.valueToGrid: value dimension " +
                             value.length + " not equal to Domain dimension " +
                             DomainDimension);
    }
    if (ManifoldDimension < 3) {
      throw new SetException("Gridded3DSet.valueToGrid: ManifoldDimension " +
                             "must be 3");
    }
    if (Length > 1 && (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2)) {
      throw new SetException("Gridded3DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    // Avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(value[0].length, value[1].length);
    length = Math.min(length, value[2].length);
    float[][] grid = new float[ManifoldDimension][length];

    // (gx, gy, gz) is the current grid box guess
/* WLH 6 Dec 2001
    int gx = (LengthX-1)/2;
    int gy = (LengthY-1)/2;
    int gz = (LengthZ-1)/2;
*/
    // use value from last call as first guess, if reasonable
    if (gx < 0 || gx >= LengthX || gy < 0 || gy >= LengthY ||
        gz < 0 || gz >= LengthZ) {
      gx = (LengthX-1)/2;
      gy = (LengthY-1)/2;
      gz = (LengthZ-1)/2;
    }


    float[] A = new float[3];
    float[] B = new float[3];
    float[] C = new float[3];
    float[] D = new float[3];
    float[] E = new float[3];
    float[] F = new float[3];
    float[] G = new float[3];
    float[] H = new float[3];
    float[] M = new float[3];
    float[] N = new float[3];
    float[] O = new float[3];
    float[] P = new float[3];
    float[] Q = new float[3];
    float[] X = new float[3];
    float[] Y = new float[3];


    for (int i=0; i<length; i++) {
      // a flag indicating whether point is off the grid
      boolean offgrid = false;
      // the first guess should be the last box unless there was no solution
/* WLH 24 Oct 97
      if ( (i != 0) && (Float.isNaN(grid[0][i-1])) )
*/
      if (Length == 1) {
        if (Float.isNaN(value[0][i]) || Float.isNaN(value[1][i]) || Float.isNaN(value[2][i])) {
           grid[0][i] = grid[1][i] = grid[2][i] = Float.NaN;
        } else {
           grid[0][i] = 0;
           grid[1][i] = 0;
           grid[2][i] = 0;
        }
        continue;
      }
      // test for missing
      if ( (i != 0) && grid[0][i-1] != grid[0][i-1] ) {
        //gx = (LengthX-1)/2;
        //gy = (LengthY-1)/2;
        //gz = (LengthZ-1)/2;
      }
      int tetnum = 5;  // Tetrahedron number in which to start search
      // if the iteration loop fails, the result should be NaN
      grid[0][i] = grid[1][i] = grid[2][i] = Float.NaN;

      //--TDR, don't let value and initial guess be too far apart
      float v_x, v_y, v_z;
      v_x = value[0][i];
      v_y = value[1][i];
      v_z = value[2][i];
      int ii = LengthX*LengthY*LengthZ - 1;
      int gii = (int) gz*LengthX*LengthY+gy*LengthX+gx;
      float sx = Samples[0][gii];
      float sy = Samples[1][gii];
      float sz = Samples[2][gii];
      if ( (Math.sqrt((v_x-sx)*(v_x-sx)) > 0.4*Math.sqrt((Samples[0][0]-Samples[0][ii])*(Samples[0][0]-Samples[0][ii]))) ||
           (Math.sqrt((v_y-sy)*(v_y-sy)) > 0.4*Math.sqrt((Samples[1][0]-Samples[1][ii])*(Samples[1][0]-Samples[1][ii]))) ||
           (Math.sqrt((v_z-sz)*(v_z-sz)) > 0.4*Math.sqrt((Samples[2][0]-Samples[2][ii])*(Samples[2][0]-Samples[2][ii]))) )
      {
        float[] ginit = getStartPoint(value[0][i], value[1][i], value[2][i]);
        gx = (int)ginit[0];
        gy = (int)ginit[1];
        gz = (int)ginit[2];
      }
      //----
      for (int itnum=0; itnum<2*(LengthX+LengthY+LengthZ); itnum++) {
        // determine tetrahedralization type
        boolean evencube = ((gx+gy+gz) % 2 == 0);

        // Define vertices of grid box
        int zadd = LengthY*LengthX;
        int base = gz*zadd + gy*LengthX + gx;
        int ai = base+zadd;            // 0, 0, 1
        int bi = base+zadd+1;          // 1, 0, 1
        int ci = base+zadd+LengthX+1;  // 1, 1, 1
        int di = base+zadd+LengthX;    // 0, 1, 1
        int ei = base;                 // 0, 0, 0
        int fi = base+1;               // 1, 0, 0
        int gi = base+LengthX+1;       // 1, 1, 0
        int hi = base+LengthX;         // 0, 1, 0



        if (evencube) {
          A[0] = Samples[0][ai];
          A[1] = Samples[1][ai];
          A[2] = Samples[2][ai];
          B[0] = Samples[0][bi];
          B[1] = Samples[1][bi];
          B[2] = Samples[2][bi];
          C[0] = Samples[0][ci];
          C[1] = Samples[1][ci];
          C[2] = Samples[2][ci];
          D[0] = Samples[0][di];
          D[1] = Samples[1][di];
          D[2] = Samples[2][di];
          E[0] = Samples[0][ei];
          E[1] = Samples[1][ei];
          E[2] = Samples[2][ei];
          F[0] = Samples[0][fi];
          F[1] = Samples[1][fi];
          F[2] = Samples[2][fi];
          G[0] = Samples[0][gi];
          G[1] = Samples[1][gi];
          G[2] = Samples[2][gi];
          H[0] = Samples[0][hi];
          H[1] = Samples[1][hi];
          H[2] = Samples[2][hi];
        }
        else {
          G[0] = Samples[0][ai];
          G[1] = Samples[1][ai];
          G[2] = Samples[2][ai];
          H[0] = Samples[0][bi];
          H[1] = Samples[1][bi];
          H[2] = Samples[2][bi];
          E[0] = Samples[0][ci];
          E[1] = Samples[1][ci];
          E[2] = Samples[2][ci];
          F[0] = Samples[0][di];
          F[1] = Samples[1][di];
          F[2] = Samples[2][di];
          C[0] = Samples[0][ei];
          C[1] = Samples[1][ei];
          C[2] = Samples[2][ei];
          D[0] = Samples[0][fi];
          D[1] = Samples[1][fi];
          D[2] = Samples[2][fi];
          A[0] = Samples[0][gi];
          A[1] = Samples[1][gi];
          A[2] = Samples[2][gi];
          B[0] = Samples[0][hi];
          B[1] = Samples[1][hi];
          B[2] = Samples[2][hi];
        }

        // Compute tests and go to a new box depending on results
        boolean test1, test2, test3, test4;
        float tval1, tval2, tval3, tval4;
        int ogx = gx;
        int ogy = gy;
        int ogz = gz;
        if (tetnum==1) {
          tval1 = ( (E[1]-A[1])*(F[2]-E[2]) - (E[2]-A[2])*(F[1]-E[1]) )
                   *(value[0][i]-E[0])
                + ( (E[2]-A[2])*(F[0]-E[0]) - (E[0]-A[0])*(F[2]-E[2]) )
                   *(value[1][i]-E[1])
                + ( (E[0]-A[0])*(F[1]-E[1]) - (E[1]-A[1])*(F[0]-E[0]) )
                   *(value[2][i]-E[2]);
          tval2 = ( (E[1]-H[1])*(A[2]-E[2]) - (E[2]-H[2])*(A[1]-E[1]) )
                   *(value[0][i]-E[0])
                + ( (E[2]-H[2])*(A[0]-E[0]) - (E[0]-H[0])*(A[2]-E[2]) )
                   *(value[1][i]-E[1])
                + ( (E[0]-H[0])*(A[1]-E[1]) - (E[1]-H[1])*(A[0]-E[0]) )
                   *(value[2][i]-E[2]);
          tval3 = ( (E[1]-F[1])*(H[2]-E[2]) - (E[2]-F[2])*(H[1]-E[1]) )
                   *(value[0][i]-E[0])
                + ( (E[2]-F[2])*(H[0]-E[0]) - (E[0]-F[0])*(H[2]-E[2]) )
                   *(value[1][i]-E[1])
                + ( (E[0]-F[0])*(H[1]-E[1]) - (E[1]-F[1])*(H[0]-E[0]) )
                   *(value[2][i]-E[2]);
          test1 = (visad.util.Util.isApproximatelyEqual(tval1,0.0,1E-04)) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (visad.util.Util.isApproximatelyEqual(tval2,0.0,1E-04)) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (visad.util.Util.isApproximatelyEqual(tval3,0.0,1E-04)) || ((tval3 > 0) == (!evencube)^Pos);


          // if a test failed go to a new box
          int updown = (evencube) ? -1 : 1;
          if (!test1) gy += updown; // UP/DOWN
          if (!test2) gx += updown; // LEFT/RIGHT
          if (!test3) gz += updown; // BACK/FORWARD
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off.
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            if ((value[0][i]==E[0])&&(value[1][i]==E[1])&&(value[2][i]==E[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              break;
            }
            if ((value[0][i]==A[0])&&(value[1][i]==A[1])&&(value[2][i]==A[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              break;
            }
            if ((value[0][i]==F[0])&&(value[1][i]==F[1])&&(value[2][i]==F[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              break;
            }
            if ((value[0][i]==H[0])&&(value[1][i]==H[1])&&(value[2][i]==H[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              break;
            }
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (F[j]-E[j])*(A[(j+1)%3]-E[(j+1)%3])
                   - (F[(j+1)%3]-E[(j+1)%3])*(A[j]-E[j]);
              N[j] = (H[j]-E[j])*(A[(j+1)%3]-E[(j+1)%3])
                   - (H[(j+1)%3]-E[(j+1)%3])*(A[j]-E[j]);
              O[j] = (F[(j+1)%3]-E[(j+1)%3])*(A[(j+2)%3]-E[(j+2)%3])
                   - (F[(j+2)%3]-E[(j+2)%3])*(A[(j+1)%3]-E[(j+1)%3]);
              P[j] = (H[(j+1)%3]-E[(j+1)%3])*(A[(j+2)%3]-E[(j+2)%3])
                   - (H[(j+2)%3]-E[(j+2)%3])*(A[(j+1)%3]-E[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(A[(j+1)%3]-E[(j+1)%3])
                   - value[(j+1)%3][i]*(A[(j+2)%3]-E[(j+2)%3])
                   + E[(j+1)%3]*A[(j+2)%3] - E[(j+2)%3]*A[(j+1)%3];
              Y[j] = value[j][i]*(A[(j+1)%3]-E[(j+1)%3])
                   - value[(j+1)%3][i]*(A[j]-E[j])
                   + E[(j+1)%3]*A[j] - E[j]*A[(j+1)%3];
            }
            float s, t, u;
            // these if statements handle skewed grids
            float d0 = M[0]*P[0] - N[0]*O[0];
            float d1 = M[1]*P[1] - N[1]*O[1];
            float d2 = M[2]*P[2] - N[2]*O[2];
            float ad0 = Math.abs(d0);
            float ad1 = Math.abs(d1);
            float ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = (N[0]*X[0] + P[0]*Y[0])/d0;
              t = -(M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = (N[1]*X[1] + P[1]*Y[1])/d1;
              t = -(M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/d2;
              t = -(M[2]*X[2] + O[2]*Y[2])/d2;
            }
/* WLH 5 April 99
            if (M[0]*P[0] != N[0]*O[0]) {
              s = (N[0]*X[0] + P[0]*Y[0])/(M[0]*P[0] - N[0]*O[0]);
              t = (M[0]*X[0] + O[0]*Y[0])/(N[0]*O[0] - M[0]*P[0]);
            }
            else if (M[1]*P[1] != N[1]*O[1]) {
              s = (N[1]*X[1] + P[1]*Y[1])/(M[1]*P[1] - N[1]*O[1]);
              t = (M[1]*X[1] + O[1]*Y[1])/(N[1]*O[1] - M[1]*P[1]);
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/(M[2]*P[2] - N[2]*O[2]);
              t = (M[2]*X[2] + O[2]*Y[2])/(N[2]*O[2] - M[2]*P[2]);
            }
*/
            d0 = A[0]-E[0];
            d1 = A[1]-E[1];
            d2 = A[2]-E[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = ( value[0][i] - E[0] - s*(F[0]-E[0])
                - t*(H[0]-E[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = ( value[1][i] - E[1] - s*(F[1]-E[1])
                - t*(H[1]-E[1]) ) / d1;
            }
            else {
              u = ( value[2][i] - E[2] - s*(F[2]-E[2])
                - t*(H[2]-E[2]) ) / d2;
            }
/* WLH 5 April 99
            if (A[0] != E[0]) {
              u = ( value[0][i] - E[0] - s*(F[0]-E[0])
                - t*(H[0]-E[0]) ) / (A[0]-E[0]);
            }
            else if (A[1] != E[1]) {
              u = ( value[1][i] - E[1] - s*(F[1]-E[1])
                - t*(H[1]-E[1]) ) / (A[1]-E[1]);
            }
            else {
              u = ( value[2][i] - E[2] - s*(F[2]-E[2])
                - t*(H[2]-E[2]) ) / (A[2]-E[2]);
            }
*/
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }

            break;
          }
        }
        else if (tetnum==2) {
          tval1 = ( (B[1]-C[1])*(F[2]-B[2]) - (B[2]-C[2])*(F[1]-B[1]) )
                   *(value[0][i]-B[0])
                + ( (B[2]-C[2])*(F[0]-B[0]) - (B[0]-C[0])*(F[2]-B[2]) )
                   *(value[1][i]-B[1])
                + ( (B[0]-C[0])*(F[1]-B[1]) - (B[1]-C[1])*(F[0]-B[0]) )
                   *(value[2][i]-B[2]);
          tval2 = ( (B[1]-A[1])*(C[2]-B[2]) - (B[2]-A[2])*(C[1]-B[1]) )
                   *(value[0][i]-B[0])
                + ( (B[2]-A[2])*(C[0]-B[0]) - (B[0]-A[0])*(C[2]-B[2]) )
                   *(value[1][i]-B[1])
                + ( (B[0]-A[0])*(C[1]-B[1]) - (B[1]-A[1])*(C[0]-B[0]) )
                   *(value[2][i]-B[2]);
          tval3 = ( (B[1]-F[1])*(A[2]-B[2]) - (B[2]-F[2])*(A[1]-B[1]) )
                   *(value[0][i]-B[0])
                + ( (B[2]-F[2])*(A[0]-B[0]) - (B[0]-F[0])*(A[2]-B[2]) )
                   *(value[1][i]-B[1])
                + ( (B[0]-F[0])*(A[1]-B[1]) - (B[1]-F[1])*(A[0]-B[0]) )
                   *(value[2][i]-B[2]);
          test1 = (visad.util.Util.isApproximatelyEqual(tval1,0.0,1E-04)) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (visad.util.Util.isApproximatelyEqual(tval2,0.0,1E-04)) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (visad.util.Util.isApproximatelyEqual(tval3,0.0,1E-04)) || ((tval3 > 0) == (!evencube)^Pos);


          // if a test failed go to a new box
          if (!test1 &&  evencube) gx++; // RIGHT
          if (!test1 && !evencube) gx--; // LEFT
          if (!test2 &&  evencube) gz++; // FORWARD
          if (!test2 && !evencube) gz--; // BACK
          if (!test3 &&  evencube) gy--; // UP
          if (!test3 && !evencube) gy++; // DOWN
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            if ((value[0][i]==B[0])&&(value[1][i]==B[1])&&(value[2][i]==B[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              break;
            }
            if ((value[0][i]==A[0])&&(value[1][i]==A[1])&&(value[2][i]==A[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              break;
            }
            if ((value[0][i]==F[0])&&(value[1][i]==F[1])&&(value[2][i]==F[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              break;
            }
            if ((value[0][i]==C[0])&&(value[1][i]==C[1])&&(value[2][i]==C[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              break;
            }
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (A[j]-B[j])*(F[(j+1)%3]-B[(j+1)%3])
                   - (A[(j+1)%3]-B[(j+1)%3])*(F[j]-B[j]);
              N[j] = (C[j]-B[j])*(F[(j+1)%3]-B[(j+1)%3])
                   - (C[(j+1)%3]-B[(j+1)%3])*(F[j]-B[j]);
              O[j] = (A[(j+1)%3]-B[(j+1)%3])*(F[(j+2)%3]-B[(j+2)%3])
                   - (A[(j+2)%3]-B[(j+2)%3])*(F[(j+1)%3]-B[(j+1)%3]);
              P[j] = (C[(j+1)%3]-B[(j+1)%3])*(F[(j+2)%3]-B[(j+2)%3])
                   - (C[(j+2)%3]-B[(j+2)%3])*(F[(j+1)%3]-B[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(F[(j+1)%3]-B[(j+1)%3])
                   - value[(j+1)%3][i]*(F[(j+2)%3]-B[(j+2)%3])
                   + B[(j+1)%3]*F[(j+2)%3] - B[(j+2)%3]*F[(j+1)%3];
              Y[j] = value[j][i]*(F[(j+1)%3]-B[(j+1)%3])
                   - value[1][i]*(F[j]-B[j])
                   + B[(j+1)%3]*F[j] - B[j]*F[(j+1)%3];
            }
            float s, t, u;
            // these if statements handle skewed grids
            float d0 = M[0]*P[0] - N[0]*O[0];
            float d1 = M[1]*P[1] - N[1]*O[1];
            float d2 = M[2]*P[2] - N[2]*O[2];
            float ad0 = Math.abs(d0);
            float ad1 = Math.abs(d1);
            float ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = 1 - (N[0]*X[0] + P[0]*Y[0])/d0;
              t = -(M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = 1 - (N[1]*X[1] + P[1]*Y[1])/d1;
              t = -(M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = 1 - (N[2]*X[2] + P[2]*Y[2])/d2;
              t = -(M[2]*X[2] + O[2]*Y[2])/d2;
            }
/* WLH 5 April 99
            if (M[0]*P[0] != N[0]*O[0]) {
              s = 1 - (N[0]*X[0] + P[0]*Y[0])/(M[0]*P[0] - N[0]*O[0]);
              t = (M[0]*X[0] + O[0]*Y[0])/(N[0]*O[0] - M[0]*P[0]);
            }
            else if (M[1]*P[1] != N[1]*O[1]) {
              s = 1 - (N[1]*X[1] + P[1]*Y[1])/(M[1]*P[1] - N[1]*O[1]);
              t = (M[1]*X[1] + O[1]*Y[1])/(N[1]*O[1] - M[1]*P[1]);
            }
            else {
              s = 1 - (N[2]*X[2] + P[2]*Y[2])/(M[2]*P[2] - N[2]*O[2]);
              t = (M[2]*X[2] + O[2]*Y[2])/(N[2]*O[2] - M[2]*P[2]);
            }
*/
            d0 = F[0]-B[0];
            d1 = F[1]-B[1];
            d2 = F[2]-B[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = 1 - ( value[0][i] - B[0] - (1-s)*(A[0]-B[0])
                - t*(C[0]-B[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = 1 - ( value[1][i] - B[1] - (1-s)*(A[1]-B[1])
                - t*(C[1]-B[1]) ) / d1;
            }
            else {
              u = 1 - ( value[2][i] - B[2] - (1-s)*(A[2]-B[2])
                - t*(C[2]-B[2]) ) / d2;
            }
/* WLH 5 April 99
            if (F[0] != B[0]) {
              u = 1 - ( value[0][i] - B[0] - (1-s)*(A[0]-B[0])
                - t*(C[0]-B[0]) ) / (F[0]-B[0]);
            }
            else if (F[1] != B[1]) {
              u = 1 - ( value[1][i] - B[1] - (1-s)*(A[1]-B[1])
                - t*(C[1]-B[1]) ) / (F[1]-B[1]);
            }
            else {
              u = 1 - ( value[2][i] - B[2] - (1-s)*(A[2]-B[2])
                - t*(C[2]-B[2]) ) / (F[2]-B[2]);
            }
*/
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }

            break;
          }
        }
        else if (tetnum==3) {
          tval1 = ( (D[1]-A[1])*(H[2]-D[2]) - (D[2]-A[2])*(H[1]-D[1]) )
                   *(value[0][i]-D[0])
                + ( (D[2]-A[2])*(H[0]-D[0]) - (D[0]-A[0])*(H[2]-D[2]) )
                   *(value[1][i]-D[1])
                + ( (D[0]-A[0])*(H[1]-D[1]) - (D[1]-A[1])*(H[0]-D[0]) )
                   *(value[2][i]-D[2]);
          tval2 = ( (D[1]-C[1])*(A[2]-D[2]) - (D[2]-C[2])*(A[1]-D[1]) )
                   *(value[0][i]-D[0])
                + ( (D[2]-C[2])*(A[0]-D[0]) - (D[0]-C[0])*(A[2]-D[2]) )
                   *(value[1][i]-D[1])
                + ( (D[0]-C[0])*(A[1]-D[1]) - (D[1]-C[1])*(A[0]-D[0]) )
                   *(value[2][i]-D[2]);
          tval3 = ( (D[1]-H[1])*(C[2]-D[2]) - (D[2]-H[2])*(C[1]-D[1]) )
                   *(value[0][i]-D[0])
                + ( (D[2]-H[2])*(C[0]-D[0]) - (D[0]-H[0])*(C[2]-D[2]) )
                   *(value[1][i]-D[1])
                + ( (D[0]-H[0])*(C[1]-D[1]) - (D[1]-H[1])*(C[0]-D[0]) )
                   *(value[2][i]-D[2]);
          test1 = (visad.util.Util.isApproximatelyEqual(tval1,0.0,1E-04)) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (visad.util.Util.isApproximatelyEqual(tval2,0.0,1E-04)) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (visad.util.Util.isApproximatelyEqual(tval3,0.0,1E-04)) || ((tval3 > 0) == (!evencube)^Pos);


          // if a test failed go to a new box
          if (!test1 &&  evencube) gx--; // LEFT
          if (!test1 && !evencube) gx++; // RIGHT
          if (!test2 &&  evencube) gz++; // FORWARD
          if (!test2 && !evencube) gz--; // BACK
          if (!test3 &&  evencube) gy++; // DOWN
          if (!test3 && !evencube) gy--; // UP
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            if ((value[0][i]==H[0])&&(value[1][i]==H[1])&&(value[2][i]==H[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              break;
            }
            if ((value[0][i]==A[0])&&(value[1][i]==A[1])&&(value[2][i]==A[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              break;
            }
            if ((value[0][i]==D[0])&&(value[1][i]==D[1])&&(value[2][i]==D[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              break;
            }
            if ((value[0][i]==C[0])&&(value[1][i]==C[1])&&(value[2][i]==C[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              break;
            }
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (C[j]-D[j])*(H[(j+1)%3]-D[(j+1)%3])
                   - (C[(j+1)%3]-D[(j+1)%3])*(H[j]-D[j]);
              N[j] = (A[j]-D[j])*(H[(j+1)%3]-D[(j+1)%3])
                   - (A[(j+1)%3]-D[(j+1)%3])*(H[j]-D[j]);
              O[j] = (C[(j+1)%3]-D[(j+1)%3])*(H[(j+2)%3]-D[(j+2)%3])
                   - (C[(j+2)%3]-D[(j+2)%3])*(H[(j+1)%3]-D[(j+1)%3]);
              P[j] = (A[(j+1)%3]-D[(j+1)%3])*(H[(j+2)%3]-D[(j+2)%3])
                   - (A[(j+2)%3]-D[(j+2)%3])*(H[(j+1)%3]-D[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(H[(j+1)%3]-D[(j+1)%3])
                   - value[(j+1)%3][i]*(H[(j+2)%3]-D[(j+2)%3])
                   + D[(j+1)%3]*H[(j+2)%3] - D[(j+2)%3]*H[(j+1)%3];
              Y[j] = value[j][i]*(H[(j+1)%3]-D[(j+1)%3])
                   - value[(j+1)%3][i]*(H[j]-D[j])
                   + D[(j+1)%3]*H[j] - D[j]*H[(j+1)%3];
            }
            float s, t, u;
            // these if statements handle skewed grids
            float d0 = M[0]*P[0] - N[0]*O[0];
            float d1 = M[1]*P[1] - N[1]*O[1];
            float d2 = M[2]*P[2] - N[2]*O[2];
            float ad0 = Math.abs(d0);
            float ad1 = Math.abs(d1);
            float ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = (N[0]*X[0] + P[0]*Y[0])/d0;
              t = 1 + (M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = (N[1]*X[1] + P[1]*Y[1])/d1;
              t = 1 + (M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/d2;
              t =  1 + (M[2]*X[2] + O[2]*Y[2])/d2;
            }
/* WLH 5 April 99
            if (M[0]*P[0] != N[0]*O[0]) {
              s = (N[0]*X[0] + P[0]*Y[0])/(M[0]*P[0] - N[0]*O[0]);
              t = 1 - (M[0]*X[0] + O[0]*Y[0])/(N[0]*O[0] - M[0]*P[0]);
            }
            else if (M[1]*P[1] != N[1]*O[1]) {
              s = (N[1]*X[1] + P[1]*Y[1])/(M[1]*P[1] - N[1]*O[1]);
              t = 1 - (M[1]*X[1] + O[1]*Y[1])/(N[1]*O[1] - M[1]*P[1]);
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/(M[2]*P[2] - N[2]*O[2]);
              t = 1 - (M[2]*X[2] + O[2]*Y[2])/(N[2]*O[2] - M[2]*P[2]);
            }
*/
            d0 = H[0]-D[0];
            d1 = H[1]-D[1];
            d2 = H[2]-D[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = 1 - ( value[0][i] - D[0] - s*(C[0]-D[0])
                - (1-t)*(A[0]-D[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = 1 - ( value[1][i] - D[1] - s*(C[1]-D[1])
                - (1-t)*(A[1]-D[1]) ) / d1;
            }
            else {
              u = 1 - ( value[2][i] - D[2] - s*(C[2]-D[2])
                - (1-t)*(A[2]-D[2]) ) / d2;
            }
/* WLH 5 April 99
            if (H[0] != D[0]) {
              u = 1 - ( value[0][i] - D[0] - s*(C[0]-D[0])
                - (1-t)*(A[0]-D[0]) ) / (H[0]-D[0]);
            }
            else if (H[1] != D[1]) {
              u = 1 - ( value[1][i] - D[1] - s*(C[1]-D[1])
                - (1-t)*(A[1]-D[1]) ) / (H[1]-D[1]);
            }
            else {
              u = 1 - ( value[2][i] - D[2] - s*(C[2]-D[2])
                - (1-t)*(A[2]-D[2]) ) / (H[2]-D[2]);
            }
*/
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }

            break;
          }
        }
        else if (tetnum==4) {
          tval1 = ( (G[1]-C[1])*(H[2]-G[2]) - (G[2]-C[2])*(H[1]-G[1]) )
                   *(value[0][i]-G[0])
                + ( (G[2]-C[2])*(H[0]-G[0]) - (G[0]-C[0])*(H[2]-G[2]) )
                   *(value[1][i]-G[1])
                + ( (G[0]-C[0])*(H[1]-G[1]) - (G[1]-C[1])*(H[0]-G[0]) )
                   *(value[2][i]-G[2]);
          tval2 = ( (G[1]-F[1])*(C[2]-G[2]) - (G[2]-F[2])*(C[1]-G[1]) )
                   *(value[0][i]-G[0])
                + ( (G[2]-F[2])*(C[0]-G[0]) - (G[0]-F[0])*(C[2]-G[2]) )
                   *(value[1][i]-G[1])
                + ( (G[0]-F[0])*(C[1]-G[1]) - (G[1]-F[1])*(C[0]-G[0]) )
                   *(value[2][i]-G[2]);
          tval3 = ( (G[1]-H[1])*(F[2]-G[2]) - (G[2]-H[2])*(F[1]-G[1]) )
                   *(value[0][i]-G[0])
                + ( (G[2]-H[2])*(F[0]-G[0]) - (G[0]-H[0])*(F[2]-G[2]) )
                   *(value[1][i]-G[1])
                + ( (G[0]-H[0])*(F[1]-G[1]) - (G[1]-H[1])*(F[0]-G[0]) )
                   *(value[2][i]-G[2]);
          test1 = (visad.util.Util.isApproximatelyEqual(tval1,0.0,1E-04)) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (visad.util.Util.isApproximatelyEqual(tval2,0.0,1E-04)) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (visad.util.Util.isApproximatelyEqual(tval3,0.0,1E-04)) || ((tval3 > 0) == (!evencube)^Pos);


          // if a test failed go to a new box
          if (!test1 &&  evencube) gy++; // DOWN
          if (!test1 && !evencube) gy--; // UP
          if (!test2 &&  evencube) gx++; // RIGHT
          if (!test2 && !evencube) gx--; // LEFT
          if (!test3 &&  evencube) gz--; // BACK
          if (!test3 && !evencube) gz++; // FORWARD
          tetnum = 5;
          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz)
            && (!test1 || !test2 || !test3) && !offgrid ) {
            offgrid = true;
            continue;
          }

          // If all tests pass then this is the correct tetrahedron
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz) )
                || offgrid) {
            if ((value[0][i]==H[0])&&(value[1][i]==H[1])&&(value[2][i]==H[2])) {
              if (evencube) {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              break;
            }
            if ((value[0][i]==G[0])&&(value[1][i]==G[1])&&(value[2][i]==G[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz+1;
              }
              break;
            }
            if ((value[0][i]==F[0])&&(value[1][i]==F[1])&&(value[2][i]==F[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              break;
            }
            if ((value[0][i]==C[0])&&(value[1][i]==C[1])&&(value[2][i]==C[2])) {
              if (evencube) {
                grid[0][i] = gx+1;
                grid[1][i] = gy+1;
                grid[2][i] = gz+1;
              }
              else {
                grid[0][i] = gx;
                grid[1][i] = gy;
                grid[2][i] = gz;
              }
              break;
            }
            // solve point
            for (int j=0; j<3; j++) {
              M[j] = (H[j]-G[j])*(C[(j+1)%3]-G[(j+1)%3])
                   - (H[(j+1)%3]-G[(j+1)%3])*(C[j]-G[j]);
              N[j] = (F[j]-G[j])*(C[(j+1)%3]-G[(j+1)%3])
                   - (F[(j+1)%3]-G[(j+1)%3])*(C[j]-G[j]);
              O[j] = (H[(j+1)%3]-G[(j+1)%3])*(C[(j+2)%3]-G[(j+2)%3])
                   - (H[(j+2)%3]-G[(j+2)%3])*(C[(j+1)%3]-G[(j+1)%3]);
              P[j] = (F[(j+1)%3]-G[(j+1)%3])*(C[(j+2)%3]-G[(j+2)%3])
                   - (F[(j+2)%3]-G[(j+2)%3])*(C[(j+1)%3]-G[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(C[(j+1)%3]-G[(j+1)%3])
                   - value[(j+1)%3][i]*(C[(j+2)%3]-G[(j+2)%3])
                   + G[(j+1)%3]*C[(j+2)%3] - G[(j+2)%3]*C[(j+1)%3];
              Y[j] = value[j][i]*(C[(j+1)%3]-G[(j+1)%3])
                   - value[(j+1)%3][i]*(C[j]-G[j])
                   + G[(j+1)%3]*C[j] - G[j]*C[(j+1)%3];
            }
            float s, t, u;
            // these if statements handle skewed grids
            float d0 = M[0]*P[0] - N[0]*O[0];
            float d1 = M[1]*P[1] - N[1]*O[1];
            float d2 = M[2]*P[2] - N[2]*O[2];
            float ad0 = Math.abs(d0);
            float ad1 = Math.abs(d1);
            float ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = 1 - (N[0]*X[0] + P[0]*Y[0])/d0;
              t = 1 + (M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = 1 - (N[1]*X[1] + P[1]*Y[1])/d1;
              t = 1 + (M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = 1 - (N[2]*X[2] + P[2]*Y[2])/d2;
              t = 1 + (M[2]*X[2] + O[2]*Y[2])/d2;
            }
/* WLH 5 April 99
            if (M[0]*P[0] != N[0]*O[0]) {
              s = 1 - (N[0]*X[0] + P[0]*Y[0])/(M[0]*P[0] - N[0]*O[0]);
              t = 1 - (M[0]*X[0] + O[0]*Y[0])/(N[0]*O[0] - M[0]*P[0]);
            }
            else if (M[1]*P[1] != N[1]*O[1]) {
              s = 1 - (N[1]*X[1] + P[1]*Y[1])/(M[1]*P[1] - N[1]*O[1]);
              t = 1 - (M[1]*X[1] + O[1]*Y[1])/(N[1]*O[1] - M[1]*P[1]);
            }
            else {
              s = 1 - (N[2]*X[2] + P[2]*Y[2])/(M[2]*P[2] - N[2]*O[2]);
              t = 1 - (M[2]*X[2] + O[2]*Y[2])/(N[2]*O[2] - M[2]*P[2]);
            }
*/
            d0 = C[0]-G[0];
            d1 = C[1]-G[1];
            d2 = C[2]-G[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = ( value[0][i] - G[0] - (1-s)*(H[0]-G[0])
                - (1-t)*(F[0]-G[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = ( value[1][i] - G[1] - (1-s)*(H[1]-G[1])
                - (1-t)*(F[1]-G[1]) ) / d1;
            }
            else {
              u = ( value[2][i] - G[2] - (1-s)*(H[2]-G[2])
                - (1-t)*(F[2]-G[2]) ) / d2;
            }
/* WLH 5 April 99
            if (C[0] != G[0]) {
              u = ( value[0][i] - G[0] - (1-s)*(H[0]-G[0])
                - (1-t)*(F[0]-G[0]) ) / (C[0]-G[0]);
            }
            else if (C[1] != G[1]) {
              u = ( value[1][i] - G[1] - (1-s)*(H[1]-G[1])
                - (1-t)*(F[1]-G[1]) ) / (C[1]-G[1]);
            }
            else {
              u = ( value[2][i] - G[2] - (1-s)*(H[2]-G[2])
                - (1-t)*(F[2]-G[2]) ) / (C[2]-G[2]);
            }
*/
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }

            break;
          }
        }
        else {    // tetnum==5
          tval1 = ( (F[1]-H[1])*(A[2]-F[2]) - (F[2]-H[2])*(A[1]-F[1]) )
                   *(value[0][i]-F[0])
                + ( (F[2]-H[2])*(A[0]-F[0]) - (F[0]-H[0])*(A[2]-F[2]) )
                   *(value[1][i]-F[1])
                + ( (F[0]-H[0])*(A[1]-F[1]) - (F[1]-H[1])*(A[0]-F[0]) )
                   *(value[2][i]-F[2]);
          tval2 = ( (C[1]-F[1])*(A[2]-C[2]) - (C[2]-F[2])*(A[1]-C[1]) )
                   *(value[0][i]-C[0])
                + ( (C[2]-F[2])*(A[0]-C[0]) - (C[0]-F[0])*(A[2]-C[2]) )
                   *(value[1][i]-C[1])
                + ( (C[0]-F[0])*(A[1]-C[1]) - (C[1]-F[1])*(A[0]-C[0]) )
                   *(value[2][i]-C[2]);
          tval3 = ( (C[1]-A[1])*(H[2]-C[2]) - (C[2]-A[2])*(H[1]-C[1]) )
                   *(value[0][i]-C[0])
                + ( (C[2]-A[2])*(H[0]-C[0]) - (C[0]-A[0])*(H[2]-C[2]) )
                   *(value[1][i]-C[1])
                + ( (C[0]-A[0])*(H[1]-C[1]) - (C[1]-A[1])*(H[0]-C[0]) )
                   *(value[2][i]-C[2]);
          tval4 = ( (F[1]-C[1])*(H[2]-F[2]) - (F[2]-C[2])*(H[1]-F[1]) )
                   *(value[0][i]-F[0])
                + ( (F[2]-C[2])*(H[0]-F[0]) - (F[0]-C[0])*(H[2]-F[2]) )
                   *(value[1][i]-F[1])
                + ( (F[0]-C[0])*(H[1]-F[1]) - (F[1]-C[1])*(H[0]-F[0]) )
                   *(value[2][i]-F[2]);
          test1 = (visad.util.Util.isApproximatelyEqual(tval1,0.0,1E-04)) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (visad.util.Util.isApproximatelyEqual(tval2,0.0,1E-04)) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (visad.util.Util.isApproximatelyEqual(tval3,0.0,1E-04)) || ((tval3 > 0) == (!evencube)^Pos);
          test4 = (visad.util.Util.isApproximatelyEqual(tval4,0.0,1E-04)) || ((tval4 > 0) == (!evencube)^Pos);


          // if a test failed go to a new tetrahedron
          if (!test1 && test2 && test3 && test4) tetnum = 1;
          if (test1 && !test2 && test3 && test4) tetnum = 2;
          if (test1 && test2 && !test3 && test4) tetnum = 3;
          if (test1 && test2 && test3 && !test4) tetnum = 4;
          if ( (!test1 && !test2 && evencube)
            || (!test3 && !test4 && !evencube) ) gy--; // GO UP
          if ( (!test1 && !test3 && evencube)
            || (!test2 && !test4 && !evencube) ) gx--; // GO LEFT
          if ( (!test1 && !test4 && evencube)
            || (!test2 && !test3 && !evencube) ) gz--; // GO BACK
          if ( (!test2 && !test3 && evencube)
            || (!test1 && !test4 && !evencube) ) gz++; // GO FORWARD
          if ( (!test2 && !test4 && evencube)
            || (!test1 && !test3 && !evencube) ) gx++; // GO RIGHT
          if ( (!test3 && !test4 && evencube)
            || (!test1 && !test2 && !evencube) ) gy++; // GO DOWN

          // Snap coordinates back onto grid in case they fell off
          if (gx < 0) gx = 0;
          if (gy < 0) gy = 0;
          if (gz < 0) gz = 0;
          if (gx > LengthX-2) gx = LengthX-2;
          if (gy > LengthY-2) gy = LengthY-2;
          if (gz > LengthZ-2) gz = LengthZ-2;

          // Detect if the point is off the grid entirely
          if (  ( (gx == ogx) && (gy == ogy) && (gz == ogz)
               && (!test1 || !test2 || !test3 || !test4)
               && (tetnum == 5)) || offgrid) {
            offgrid = true;
            boolean OX, OY, OZ, MX, MY, MZ, LX, LY, LZ;
            OX = OY = OZ = MX = MY = MZ = LX = LY = LZ = false;
            if (gx == 0) OX = true;
            if (gy == 0) OY = true;
            if (gz == 0) OZ = true;
            if (gx == LengthX-2) LX = true;
            if (gy == LengthY-2) LY = true;
            if (gz == LengthZ-2) LZ = true;
            if (!OX && !LX) MX = true;
            if (!OY && !LY) MY = true;
            if (!OZ && !LZ) MZ = true;
            test1 = test2 = test3 = test4 = false;
            // 26 cases
            if (evencube) {
              if (!LX && !LY && !LZ) tetnum = 1;
              else if ( (LX && OY && MZ) || (MX && OY && LZ)
                     || (LX && MY && LZ) || (LX && OY && LZ)
                     || (MX && MY && LZ) || (LX && MY && MZ) ) tetnum = 2;
              else if ( (OX && LY && MZ) || (OX && MY && LZ)
                     || (MX && LY && LZ) || (OX && LY && LZ)
                                         || (MX && LY && MZ) ) tetnum = 3;
              else if ( (MX && LY && OZ) || (LX && MY && OZ)
                     || (LX && LY && MZ) || (LX && LY && OZ) ) tetnum = 4;
            }
            else {
              if (!OX && !OY && !OZ) tetnum = 1;
              else if ( (OX && MY && OZ) || (MX && LY && OZ)
                     || (OX && LY && MZ) || (OX && LY && OZ)
                     || (MX && MY && OZ) || (OX && MY && MZ) ) tetnum = 2;
              else if ( (LX && MY && OZ) || (MX && OY && OZ)
                     || (LX && OY && MZ) || (LX && OY && OZ)
                                         || (MX && OY && MZ) ) tetnum = 3;
              else if ( (OX && OY && MZ) || (OX && MY && OZ)
                     || (MX && OY && LZ) || (OX && OY && LZ) ) tetnum = 4;
            }
          }

          // If all tests pass then this is the correct tetrahedron
          if ( (gx == ogx) && (gy == ogy) && (gz == ogz) && (tetnum == 5) ) {
            // solve point
            for (int j=0; j<3; j++) {
              Q[j] = (H[j] + F[j] + A[j] - C[j])/2;
            }


            for (int j=0; j<3; j++) {
              M[j] = (F[j]-Q[j])*(A[(j+1)%3]-Q[(j+1)%3])
                   - (F[(j+1)%3]-Q[(j+1)%3])*(A[j]-Q[j]);
              N[j] = (H[j]-Q[j])*(A[(j+1)%3]-Q[(j+1)%3])
                   - (H[(j+1)%3]-Q[(j+1)%3])*(A[j]-Q[j]);
              O[j] = (F[(j+1)%3]-Q[(j+1)%3])*(A[(j+2)%3]-Q[(j+2)%3])
                   - (F[(j+2)%3]-Q[(j+2)%3])*(A[(j+1)%3]-Q[(j+1)%3]);
              P[j] = (H[(j+1)%3]-Q[(j+1)%3])*(A[(j+2)%3]-Q[(j+2)%3])
                   - (H[(j+2)%3]-Q[(j+2)%3])*(A[(j+1)%3]-Q[(j+1)%3]);
              X[j] = value[(j+2)%3][i]*(A[(j+1)%3]-Q[(j+1)%3])
                   - value[(j+1)%3][i]*(A[(j+2)%3]-Q[(j+2)%3])
                   + Q[(j+1)%3]*A[(j+2)%3] - Q[(j+2)%3]*A[(j+1)%3];
              Y[j] = value[j][i]*(A[(j+1)%3]-Q[(j+1)%3])
                   - value[(j+1)%3][i]*(A[j]-Q[j])
                   + Q[(j+1)%3]*A[j] - Q[j]*A[(j+1)%3];
            }
            float s, t, u;
            // these if statements handle skewed grids
            float d0 = M[0]*P[0] - N[0]*O[0];
            float d1 = M[1]*P[1] - N[1]*O[1];
            float d2 = M[2]*P[2] - N[2]*O[2];
            float ad0 = Math.abs(d0);
            float ad1 = Math.abs(d1);
            float ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              s = (N[0]*X[0] + P[0]*Y[0])/d0;
              t = -(M[0]*X[0] + O[0]*Y[0])/d0;
            }
            else if (ad1 > ad2) {
              s = (N[1]*X[1] + P[1]*Y[1])/d1;
              t = -(M[1]*X[1] + O[1]*Y[1])/d1;
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/d2;
              t = -(M[2]*X[2] + O[2]*Y[2])/d2;
            }
/* WLH 3 April 99
            if (M[0]*P[0] != N[0]*O[0]) {
              s = (N[0]*X[0] + P[0]*Y[0])/(M[0]*P[0] - N[0]*O[0]);
              t = (M[0]*X[0] + O[0]*Y[0])/(N[0]*O[0] - M[0]*P[0]);
            }
            else if (M[1]*P[1] != N[1]*O[1]) {
              s = (N[1]*X[1] + P[1]*Y[1])/(M[1]*P[1] - N[1]*O[1]);
              t = (M[1]*X[1] + O[1]*Y[1])/(N[1]*O[1] - M[1]*P[1]);
            }
            else {
              s = (N[2]*X[2] + P[2]*Y[2])/(M[2]*P[2] - N[2]*O[2]);
              t = (M[2]*X[2] + O[2]*Y[2])/(N[2]*O[2] - M[2]*P[2]);
            }
*/
            d0 = A[0]-Q[0];
            d1 = A[1]-Q[1];
            d2 = A[2]-Q[2];
            ad0 = Math.abs(d0);
            ad1 = Math.abs(d1);
            ad2 = Math.abs(d2);
            if (ad0 > ad1 && ad0 > ad2) {
              u = ( value[0][i] - Q[0] - s*(F[0]-Q[0])
                - t*(H[0]-Q[0]) ) / d0;
            }
            else if (ad1 > ad2) {
              u = ( value[1][i] - Q[1] - s*(F[1]-Q[1])
                - t*(H[1]-Q[1]) ) / d1;
            }
            else {
              u = ( value[2][i] - Q[2] - s*(F[2]-Q[2])
                - t*(H[2]-Q[2]) ) / d2;
            }
/* WLH 3 April 99
            if (A[0] != Q[0]) {
              u = ( value[0][i] - Q[0] - s*(F[0]-Q[0])
                - t*(H[0]-Q[0]) ) / (A[0]-Q[0]);
            }
            else if (A[1] != Q[1]) {
              u = ( value[1][i] - Q[1] - s*(F[1]-Q[1])
                - t*(H[1]-Q[1]) ) / (A[1]-Q[1]);
            }
            else {
              u = ( value[2][i] - Q[2] - s*(F[2]-Q[2])
                - t*(H[2]-Q[2]) ) / (A[2]-Q[2]);
            }
*/
            if (evencube) {
              grid[0][i] = gx+s;
              grid[1][i] = gy+t;
              grid[2][i] = gz+u;
            }
            else {
              grid[0][i] = gx+1-s;
              grid[1][i] = gy+1-t;
              grid[2][i] = gz+1-u;
            }

            break;
          }
        }
      }

      // allow estimations up to 0.5 boxes outside of defined samples
      if ( (grid[0][i] <= -0.5) || (grid[0][i] >= LengthX-0.5)
        || (grid[1][i] <= -0.5) || (grid[1][i] >= LengthY-0.5)
        || (grid[2][i] <= -0.5) || (grid[2][i] >= LengthZ-0.5) ) {
        grid[0][i] = grid[1][i] = grid[2][i] = Float.NaN;
      }
    }

    return grid;
  }

  public float[] getStartPoint(float x, float y, float z) {
    int factor = 4;
    int nx = LengthX/factor;
    int ny = LengthY/factor;
    int nz = LengthZ/factor;
    float gx, gy, gz, dist;
    gx = gy = gz = 0;

    float min_dist = Float.MAX_VALUE;
    for (int k=0; k <nz; k++) {
      for (int j=0; j<ny; j++) {
        for (int i=0; i<nx; i++) {
          int idx = k*factor*(LengthX*LengthY) + j*factor*LengthX + i*factor;
          dist = (Samples[0][idx]-x)*(Samples[0][idx]-x)+(Samples[1][idx]-y)*(Samples[1][idx]-y)+(Samples[2][idx]-z)*(Samples[2][idx]-z);
          if (dist < min_dist) {
            min_dist = dist;
            gx = i*factor;
            gy = j*factor;
            gz = k*factor;
          }
        }
      } 
    }
    return new float[] {gx, gy, gz};
  }

  public VisADGeometryArray[][] makeIsoLines(float[] intervals,
                  float lowlimit, float highlimit, float base,
                  float[] fieldValues, byte[][] color_values,
                  boolean[] swap, boolean dash,
                  boolean fill, ScalarMap[] smap, double scale_ratio,
                  double label_size, float[][][] f_array)
         throws VisADException {

    int Length = getLength();
    int ManifoldDimension = getManifoldDimension();
    int[] Lengths = getLengths();
    int LengthX = Lengths[0];
    int LengthY = Lengths[1];

    if (ManifoldDimension != 2) {
      throw new DisplayException("Gridded3DSet.makeIsoLines: " +
                                 "ManifoldDimension must be 2");
    }

    int nr = LengthX;
    int nc = LengthY;
    float[] g = fieldValues;

    // these are just estimates
    // int est = 2 * Length; WLH 14 April 2000
    double dest = Math.sqrt((double) Length);
    int est = (int) (dest * Math.sqrt(dest));
    if (est < 1000) est = 1000;
    int maxv2 = est;
    int maxv1 = 2 * 2 * maxv2;
    // maxv3 and maxv4 should be equal
    int maxv3 = est;
    int maxv4 = maxv3;

/* memory use for temporaries, in bytes (for est = 2 * Length):
12 * color_length * Length +
64 * Length +
48 * Length +
 = (112 + 12 * color_length) * Length
for color_length = 3 this is 148 * Length
*/
    int color_length = (color_values != null) ? color_values.length : 0;
    byte[][] color_levels1 = null;
    byte[][] color_levels2 = null;
    byte[][] color_levels3 = null;
    if (color_length > 0) {
      color_levels1 = new byte[color_length][maxv1];
      color_levels2 = new byte[color_length][maxv2];
      color_levels3 = new byte[color_length][maxv3];
    }
    float[][] vx1 = new float[1][maxv1];
    float[][] vy1 = new float[1][maxv1];
    float[][] vz1 = new float[1][maxv1];
    float[][] vx2 = new float[1][maxv2];
    float[][] vy2 = new float[1][maxv2];
    float[][] vz2 = new float[1][maxv2];
    float[][] vx3 = new float[1][maxv3];
    float[][] vy3 = new float[1][maxv3];
    float[][] vz3 = new float[1][maxv3];
    float[][] vx4 = new float[1][maxv4];
    float[][] vy4 = new float[1][maxv4];
    float[][] vz4 = new float[1][maxv4];
    int[] numv1 = new int[1];
    int[] numv2 = new int[1];
    int[] numv3 = new int[1];
    int[] numv4 = new int[1];

    float[][] tri            = new float[2][];
    float[][] tri_normals    = new float[1][];
    byte[][]  tri_color      = new byte[color_length][];
    float[][][] grd_normals  = null;
    if (intervals == null) {
      return null;
    }
    byte[][] interval_colors = new byte[color_length][intervals.length];


    if (fill) { //- compute normals at grid points
      float[][] samples = getSamples(false);
      grd_normals = new float[nc][nr][3];
      // calculate normals
      int k = 0;
      int k3 = 0;
      int ki, kj;
      for (int i=0; i<LengthY; i++) {
        for (int j=0; j<LengthX; j++) {
          float c0 = samples[0][k3];
          float c1 = samples[1][k3];
          float c2 = samples[2][k3];
          float n0 = 0.0f;
          float n1 = 0.0f;
          float n2 = 0.0f;
          float n, m, m0, m1, m2;
          for (int ip = -1; ip<=1; ip += 2) {
            for (int jp = -1; jp<=1; jp += 2) {
              int ii = i + ip;
              int jj = j + jp;
              if (0 <= ii && ii < LengthY && 0 <= jj && jj < LengthX) {
                ki = k3 + ip * LengthX;
                kj = k3 + jp;
                m0 = (samples[2][kj] - c2) * (samples[1][ki] - c1) -
                     (samples[1][kj] - c1) * (samples[2][ki] - c2);
                m1 = (samples[0][kj] - c0) * (samples[2][ki] - c2) -
                     (samples[2][kj] - c2) * (samples[0][ki] - c0);
                m2 = (samples[1][kj] - c1) * (samples[0][ki] - c0) -
                     (samples[0][kj] - c0) * (samples[1][ki] - c1);
                m = (float) Math.sqrt(m0 * m0 + m1 * m1 + m2 * m2);
                if (ip == jp) {
                  n0 += m0 / m;
                  n1 += m1 / m;
                  n2 += m2 / m;
                }
                else {
                  n0 -= m0 / m;
                  n1 -= m1 / m;
                  n2 -= m2 / m;
                }
              }
            }
          }
          n = (float) Math.sqrt(n0 * n0 + n1 * n1 + n2 * n2);
          grd_normals[i][j][0] = n0 / n;
          grd_normals[i][j][1] = n1 / n;
          grd_normals[i][j][2] = n2 / n;
          k += 3;
          k3++;
        }
      }
      //-- compute color at field contour intervals
      float[] default_intervals = null;
      Unit ounit = smap[0].getOverrideUnit();
      if (ounit != null) {
        default_intervals =
          (((RealType)smap[0].getScalar()).getDefaultUnit()).toThis(intervals, ounit);
      }
      else {
        default_intervals = intervals;
      }
      float[] display_intervals = smap[0].scaleValues(default_intervals);
      BaseColorControl color_control = (BaseColorControl)smap[0].getControl();
      float[][] temp = null;
      try {
       temp = color_control.lookupValues(display_intervals);
      }
      catch (Exception e) {
      }
      for (int tt=0; tt<temp.length; tt++) {
        for (int kk=0; kk<interval_colors[0].length; kk++) {
          interval_colors[tt][kk] = ShadowType.floatToByte(temp[tt][kk]);
        }
      }
    }

    float[][][][] lbl_vv     = new float[4][][][];
    byte[][][][]  lbl_cc     = new byte[4][][][];
    float[][][]   lbl_loc    = new float[3][][];
  
    Contour2D.contour( g, nr, nc, intervals, lowlimit, highlimit, base, dash,
                      vx1, vy1, vz1, maxv1, numv1, vx2, vy2, vz2, maxv2, numv2,
                      vx3, vy3, vz3, maxv3, numv3, vx4, vy4, vz4, maxv4, numv4,
                      color_values, color_levels1, color_levels2,
                      color_levels3, swap,
                      fill, tri, tri_color, grd_normals, tri_normals,
                      interval_colors, lbl_vv, lbl_cc, lbl_loc, scale_ratio, label_size,
                      this);

    if (fill) {
      VisADGeometryArray[][] tri_array = new VisADGeometryArray[2][];
      tri_array[0] = new VisADGeometryArray[1];
      tri_array[0][0] = new VisADTriangleArray();
      tri_array[0][0].normals = tri_normals[0];
      setGeometryArray(tri_array[0][0], gridToValue(tri), 3, tri_color);
      return tri_array;
    }
    float[][] grid1 = new float[3][numv1[0]];
    System.arraycopy(vx1[0], 0, grid1[0], 0, numv1[0]);
    vx1 = null;
    System.arraycopy(vy1[0], 0, grid1[1], 0, numv1[0]);
    vy1 = null;
    System.arraycopy(vz1[0], 0, grid1[2], 0, numv1[0]);
    vz1 = null;

    if (color_length > 0) {
      byte[][] a = new byte[color_length][numv1[0]];
      for (int i=0; i<color_length; i++) {
        System.arraycopy(color_levels1[i], 0, a[i], 0, numv1[0]);
      }
      color_levels1 = a;
    }

    float[][] grid2 = new float[3][numv2[0]];
    System.arraycopy(vx2[0], 0, grid2[0], 0, numv2[0]);
    vx2 = null;
    System.arraycopy(vy2[0], 0, grid2[1], 0, numv2[0]);
    vy2 = null;
    System.arraycopy(vz2[0], 0, grid2[2], 0, numv2[0]);
    vz2 = null;

    if (color_length > 0) {
      byte[][] a = new byte[color_length][numv2[0]];
      for (int i=0; i<color_length; i++) {
        System.arraycopy(color_levels2[i], 0, a[i], 0, numv2[0]);
      }
      color_levels2 = a;
    }

    int n_labels = lbl_loc[0].length;

    f_array[0] = new float[n_labels][4];

    VisADLineArray[][] arrays = new VisADLineArray[4][];
    arrays[0] = new VisADLineArray[1];
    arrays[1] = new VisADLineArray[1];

    arrays[0][0] = new VisADLineArray();
    setGeometryArray(arrays[0][0], grid1, 3, color_levels1);
    grid1 = null;

    arrays[1][0] = new VisADLineArray();
    setGeometryArray(arrays[1][0], grid2, 3, color_levels2);
    grid2 = null;

    arrays[2] = new VisADLineArray[n_labels*2];
    arrays[3] = new VisADLineArray[n_labels*4];
    for (int kk = 0; kk < n_labels; kk++) {

      f_array[0][kk][0] = lbl_loc[0][kk][3];
      f_array[0][kk][1] = lbl_loc[0][kk][4];
      f_array[0][kk][2] = lbl_loc[0][kk][5];
      f_array[0][kk][3] = lbl_loc[0][kk][6];

      // temporary label orientation hack
      // TO_DO - eventually return all 4 label orientations
      // and have ProjectionControl switch among them
      boolean backwards  = true;
      boolean upsidedown = false;
      float[] vx = null;
      float[] vy = null;
      if (backwards) {
        //vy = vy4[0];
        vy = lbl_vv[1][kk][1];
      }
      else {
        //vy = vy3[0];
        vy = lbl_vv[0][kk][1];
      }
      vy3 = null;
      vy4 = null;
      if (upsidedown) {
        //vx = vx4[0];
        vx = lbl_vv[1][kk][0];
      }
      else {
        //vx = vx3[0];
        vx = lbl_vv[0][kk][0];
      }
      //vx3 = null;
      //vx4 = null;
      int num = vx.length;

      float[][] grid_label = new float[3][num];
      System.arraycopy(vx, 0, grid_label[0], 0, num);
      System.arraycopy(vy, 0, grid_label[1], 0, num);
      System.arraycopy(lbl_vv[0][kk][2], 0, grid_label[2], 0, num);

      // WLH 5 Nov 98
      vx = null;
      vy = null;

      byte[][] segL_color = null;
      byte[][] segR_color = null;
      if (color_length > 0) {
        byte[][] a = new byte[color_length][num];
        segL_color = new byte[color_length][2];
        segR_color = new byte[color_length][2];
        for (int i=0; i<color_length; i++) {
          System.arraycopy(lbl_cc[0][kk][i], 0, a[i], 0, num);
          System.arraycopy(lbl_cc[2][kk][i], 0, segL_color[i], 0, 2);
          System.arraycopy(lbl_cc[3][kk][i], 0, segR_color[i], 0, 2);
        }
        color_levels3 = a;
      }

      arrays[2][kk*2] = new VisADLineArray();
      arrays[2][kk*2+1] = new VisADLineArray();
      setGeometryArray(arrays[2][kk*2], grid_label, 3, color_levels3);
      grid_label = null;

      float[][] loc = new float[3][1];
      loc[0][0] = lbl_loc[0][kk][0];
      loc[1][0] = lbl_loc[0][kk][1];
      loc[2][0] = lbl_loc[0][kk][2];
      setGeometryArray(arrays[2][kk*2+1], loc, 3, null);

      arrays[3][kk*4] = new VisADLineArray();
      arrays[3][kk*4+1] = new VisADLineArray();
      arrays[3][kk*4+2] = new VisADLineArray();
      arrays[3][kk*4+3] = new VisADLineArray();

      float[][] segL = new float[3][2];
      segL[0][0]     = lbl_vv[2][kk][0][0];
      segL[1][0]     = lbl_vv[2][kk][1][0];
      segL[2][0]     = lbl_vv[2][kk][2][0];
      segL[0][1]     = lbl_vv[2][kk][0][1];
      segL[1][1]     = lbl_vv[2][kk][1][1];
      segL[2][1]     = lbl_vv[2][kk][2][1];
      setGeometryArray(arrays[3][kk*4], segL, 3, segL_color);

      loc[0][0]      = lbl_loc[1][kk][0];
      loc[1][0]      = lbl_loc[1][kk][1];
      loc[2][0]      = lbl_loc[1][kk][2];
      setGeometryArray(arrays[3][kk*4+1], loc, 3, null);

      float[][] segR = new float[3][2];
      segR[0][0]     = lbl_vv[3][kk][0][0];
      segR[1][0]     = lbl_vv[3][kk][1][0];
      segR[2][0]     = lbl_vv[3][kk][2][0];
      segR[0][1]     = lbl_vv[3][kk][0][1];
      segR[1][1]     = lbl_vv[3][kk][1][1];
      segR[2][1]     = lbl_vv[3][kk][2][1];
      setGeometryArray(arrays[3][kk*4+2], segR, 3, segR_color);


      loc[0][0]      = lbl_loc[2][kk][0];
      loc[1][0]      = lbl_loc[2][kk][1];
      loc[2][0]      = lbl_loc[2][kk][2];
      setGeometryArray(arrays[3][kk*4+3], loc, 3, null);
    }

    return arrays;
  }

  public float[][] getNormals(float[][] grid)
         throws VisADException
  {
    int[] Lengths = getLengths();
    int LengthX = Lengths[0];
    int LengthY = Lengths[1];

    float[][] samples = getSamples(false);
    float[][] grd_normals = new float[3][grid[0].length];
    // calculate normals
    int k3 = 0;
    int ki, kj;
    for (int tt=0; tt<grid[0].length; tt++) {
       k3 = ((int)grid[1][tt])*LengthX + (int)grid[0][tt];

       int i = k3/LengthX;
       int j = k3 - i*LengthX;
       float c0 = samples[0][k3];
       float c1 = samples[1][k3];
       float c2 = samples[2][k3];
       float n0 = 0.0f;
       float n1 = 0.0f;
       float n2 = 0.0f;
       float n, m, m0, m1, m2;
       for (int ip = -1; ip<=1; ip += 2) {
         for (int jp = -1; jp<=1; jp += 2) {
           int ii = i + ip;
           int jj = j + jp;
           if (0 <= ii && ii < LengthY && 0 <= jj && jj < LengthX) {
             ki = k3 + ip * LengthX;
             kj = k3 + jp;
             m0 = (samples[2][kj] - c2) * (samples[1][ki] - c1) -
                  (samples[1][kj] - c1) * (samples[2][ki] - c2);
             m1 = (samples[0][kj] - c0) * (samples[2][ki] - c2) -
                  (samples[2][kj] - c2) * (samples[0][ki] - c0);
             m2 = (samples[1][kj] - c1) * (samples[0][ki] - c0) -
                  (samples[0][kj] - c0) * (samples[1][ki] - c1);
             m = (float) Math.sqrt(m0 * m0 + m1 * m1 + m2 * m2);
             if (ip == jp) {
               n0 += m0 / m;
               n1 += m1 / m;
               n2 += m2 / m;
             }
             else {
               n0 -= m0 / m;
               n1 -= m1 / m;
               n2 -= m2 / m;
             }
           }
         }
       }
       n = (float) Math.sqrt(n0 * n0 + n1 * n1 + n2 * n2);
       grd_normals[0][tt] = n0 / n;
       grd_normals[1][tt] = n1 / n;
       grd_normals[2][tt] = n2 / n;
      }
    return grd_normals;
  }


  /** constants for isosurface, etc */
  static final int BIG_NEG = (int) -2e+9;
  static final float EPS_0 = (float) 1.0e-5;

  static final boolean  TRUE = true;
  static final boolean  FALSE = false;
  static final int  MASK = 0x0F;
  static final int MAX_FLAG_NUM = 317;
  static final int SF_6B = 0;
  static final int SF_6D = 6;
  static final int SF_79 = 12;
  static final int SF_97 = 18;
  static final int SF_9E = 24;
  static final int SF_B6 = 30;
  static final int SF_D6 = 36;
  static final int SF_E9 = 42;
  static final int Zp = 0;
  static final int Zn = 1;
  static final int Yp = 2;
  static final int Yn = 3;
  static final int Xp = 4;
  static final int Xn = 5;
  static final int incZn = 0;
  static final int incYn = 8;
  static final int incXn = 16;

  static final int pol_edges[][] =
  {
    {  0x0,    0,   0x0,   0x0,     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0xe,     1, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0x32,    4, 5, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x3c,    2, 4, 5, 3, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0xc4,    2, 7, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0xca,    6, 1, 3, 7, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0xf6,    1, 4, 5, 2, 7, 6, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xf8,    4, 5, 3, 7, 6, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0x150,   6, 8, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x15e,   4, 6, 8, 1, 3, 2, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x162,   1, 6, 8, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x16c,   6, 8, 5, 3, 2, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x194,   4, 2, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x19a,   1, 3, 7, 8, 4, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1a6,   2, 7, 8, 5, 1, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf,    1,   0x4,   0x1a8,   5, 3, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0x608,   3, 9,10, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x606,  10, 2, 1, 9, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x63a,   3, 9,10, 1, 4, 5, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x634,   9,10, 2, 4, 5, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x6cc,   2, 7, 6, 3, 9,10, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x6c2,   7, 6, 1, 9,10, 0, 0, 0, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0x6fe,   1, 4, 5, 2, 7, 6, 3, 9,10, 0, 0, 0  },
    {  0x17,   1,   0x6,   0x6f0,   5, 9,10, 7, 6, 4, 0, 0, 0, 0, 0, 0  },
    {  0x18,   2,   0x33,  0x758,   3, 9,10, 4, 6, 8, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x756,   1, 9,10, 2, 4, 6, 8, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x76a,   1, 6, 8, 5, 3, 9,10, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x764,   2, 6, 8, 5, 9,10, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x79c,   7, 8, 4, 2,10, 3, 9, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x792,   1, 9,10, 7, 8, 4, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x7ae,   3, 9,10, 2, 7, 8, 5, 1, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x7a0,  10, 7, 8, 5, 9, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0xa20,   9, 5,11, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0xa2e,   1, 3, 2, 5,11, 9, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0xa12,   4,11, 9, 1, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xa1c,   3, 2, 4,11, 9, 0, 0, 0, 0, 0, 0, 0  },
    {  0x18,   2,   0x33,  0xae4,   5,11, 9, 6, 2, 7, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0xaea,   3, 7, 6, 1, 9, 5,11, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0xad6,   4,11, 9, 1, 6, 2, 7, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0xad8,   3, 7, 6, 4,11, 9, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0xb70,   5,11, 9, 4, 6, 8, 0, 0, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0xb7e,   4, 6, 8, 1, 3, 2, 5,11, 9, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xb42,  11, 9, 1, 6, 8, 0, 0, 0, 0, 0, 0, 0  },
    {  0x17,   1,   0x6,   0xb4c,   8,11, 9, 3, 2, 6, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0xbb4,   4, 2, 7, 8, 5,11, 9, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0xbba,   5,11, 9, 1, 3, 7, 8, 4, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0xb86,   1, 2, 7, 8,11, 9, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xb88,   9, 3, 7, 8,11, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0xc28,  11,10, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xc26,   5,11,10, 2, 1, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xc1a,   1, 4,11,10, 3, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf,    1,   0x4,   0xc14,   2, 4,11,10, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0xcec,   3, 5,11,10, 2, 7, 6, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0xce2,  10, 7, 6, 1, 5,11, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0xcde,   2, 7, 6, 1, 4,11,10, 3, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xcd0,   6, 4,11,10, 7, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0xd78,  11,10, 3, 5, 8, 4, 6, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0xd76,   4, 6, 8, 5,11,10, 2, 1, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0xd4a,   1, 6, 8,11,10, 3, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xd44,   8,11,10, 2, 6, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3c,   2,   0x44,  0xdbc,   4, 2, 7, 8, 5,11,10, 3, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0xdb2,   8,11,10, 7, 4, 1, 5, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0xd8e,  10, 7, 8,11, 3, 1, 2, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0xd80,  10, 7, 8,11, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0x1480, 10,12, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x148e,  3, 2, 1,10,12, 7, 0, 0, 0, 0, 0, 0  },
    {  0x18,   2,   0x33,  0x14b2,  7,10,12, 1, 4, 5, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x14bc,  2, 4, 5, 3, 7,10,12, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x1444,  2,10,12, 6, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x144a, 10,12, 6, 1, 3, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x1476,  2,10,12, 6, 1, 4, 5, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x1478,  6, 4, 5, 3,10,12, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x15d0,  6, 8, 4, 7,10,12, 0, 0, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0x15de,  2, 1, 3, 6, 8, 4, 7,10,12, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x15e2,  8, 5, 1, 6,12, 7,10, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x15ec,  7,10,12, 6, 8, 5, 3, 2, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1514,  8, 4, 2,10,12, 0, 0, 0, 0, 0, 0, 0  },
    {  0x17,   1,   0x6,   0x151a,  3,10,12, 8, 4, 1, 0, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x1526,  2,10,12, 8, 5, 1, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1528, 12, 8, 5, 3,10, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x1288,  7, 3, 9,12, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1286,  2, 1, 9,12, 7, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x12ba,  9,12, 7, 3, 5, 1, 4, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x12b4,  9,12, 7, 2, 4, 5, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x124c,  3, 9,12, 6, 2, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf,    1,   0x4,   0x1242,  1, 9,12, 6, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x127e,  1, 4, 5, 3, 9,12, 6, 2, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1270,  5, 9,12, 6, 4, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x13d8,  7, 3, 9,12, 6, 8, 4, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x13d6,  6, 8, 4, 2, 1, 9,12, 7, 0, 0, 0, 0  },
    {  0x3c,   2,   0x44,  0x13ea,  1, 6, 8, 5, 3, 9,12, 7, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x13e4, 12, 8, 5, 9, 7, 2, 6, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x131c,  2, 3, 9,12, 8, 4, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1312,  4, 1, 9,12, 8, 0, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x132e,  5, 9,12, 8, 1, 2, 3, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x1320,  5, 9,12, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x1ea0, 10,12, 7, 9, 5,11, 0, 0, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0x1eae,  3, 2, 1,10,12, 7, 9, 5,11, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x1e92,  9, 1, 4,11,10,12, 7, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x1e9c, 10,12, 7, 3, 2, 4,11, 9, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x1e64, 12, 6, 2,10,11, 9, 5, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x1e6a,  9, 5,11,10,12, 6, 1, 3, 0, 0, 0, 0  },
    {  0x3c,   2,   0x44,  0x1e56,  2,10,12, 6, 1, 4,11, 9, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x1e58, 11,12, 6, 4, 9, 3,10, 0, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0x1ff0, 11, 9, 5,12, 7,10, 8, 4, 6, 0, 0, 0  },
    {  0x69,   4,   0x3333,0x1ffe,  1, 3, 2, 6, 8, 4, 9, 5,11,10,12, 7  },
    {  0x1e,   2,   0x53,  0x1fc2, 12, 7,10,11, 9, 1, 6, 8, 0, 0, 0, 0  },
    {  0xe9,   3,   0x333, 0x1fcc, 12, 8,11,10, 9, 3, 7, 2, 6, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x1f34, 11, 9, 5, 8, 4, 2,10,12, 0, 0, 0, 0  },
    {  0xe9,   3,   0x333, 0x1f3a,  5, 4, 1, 9, 3,10,11,12, 8, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x1f06, 10, 9, 1, 2,12, 8,11, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x1f08,  9, 3,10,11,12, 8, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x18a8, 12, 7, 3, 5,11, 0, 0, 0, 0, 0, 0, 0  },
    {  0x17,   1,   0x6,   0x18a6,  1, 5,11,12, 7, 2, 0, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x189a, 11,12, 7, 3, 1, 4, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1894,  7, 2, 4,11,12, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x186c, 12, 6, 2, 3, 5,11, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1862, 11,12, 6, 1, 5, 0, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x185e,  6, 4,11,12, 2, 3, 1, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x1850, 11,12, 6, 4, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x19f8,  8, 4, 6,12, 7, 3, 5,11, 0, 0, 0, 0  },
    {  0xe9,   3,   0x333, 0x19f6,  6, 7, 2, 4, 1, 5, 8,11,12, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x19ca,  6, 7, 3, 1, 8,11,12, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x19c4,  8,11,12, 6, 7, 2, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x193c,  5, 4, 2, 3,11,12, 8, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x1932, 11,12, 8, 5, 4, 1, 0, 0, 0, 0, 0, 0  },
    {  0xe7,   2,   0x33,  0x190e,  3, 1, 2,12, 8,11, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0x1900, 11,12, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1,    1,   0x3,   0x1900, 11, 8,12, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x18,   2,   0x33,  0x190e,  8,12,11, 2, 1, 3, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x1932, 11, 8,12, 5, 1, 4, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x193c,  5, 3, 2, 4,11, 8,12, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x19c4,  8,12,11, 6, 2, 7, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x19ca,  6, 1, 3, 7, 8,12,11, 0, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0x19f6,  6, 2, 7, 4, 5, 1, 8,12,11, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x19f8,  8,12,11, 4, 5, 3, 7, 6, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x1850, 11, 4, 6,12, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x185e,  6,12,11, 4, 2, 1, 3, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1862,  5, 1, 6,12,11, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x186c,  6,12,11, 5, 3, 2, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1894, 12,11, 4, 2, 7, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x189a,  4, 1, 3, 7,12,11, 0, 0, 0, 0, 0, 0  },
    {  0x17,   1,   0x6,   0x18a6,  7,12,11, 5, 1, 2, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x18a8, 11, 5, 3, 7,12, 0, 0, 0, 0, 0, 0, 0  },
    {  0x6,    2,   0x33,  0x1f08,  9,10, 3,11, 8,12, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x1f06, 10, 2, 1, 9,12,11, 8, 0, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0x1f3a,  9,10, 3,11, 8,12, 5, 1, 4, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x1f34, 11, 8,12, 9,10, 2, 4, 5, 0, 0, 0, 0  },
    {  0x16,   3,   0x333, 0x1fcc, 10, 3, 9, 7, 6, 2,12,11, 8, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x1fc2, 12,11, 8, 7, 6, 1, 9,10, 0, 0, 0, 0  },
    {  0x69,   4,   0x3333,0x1ffe,  4, 5, 1, 2, 7, 6,11, 8,12, 9,10, 3  },
    {  0xe9,   3,   0x333, 0x1ff0, 11, 5, 9,12,10, 7, 8, 6, 4, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x1e58, 11, 4, 6,12, 9,10, 3, 0, 0, 0, 0, 0  },
    {  0x3c,   2,   0x44,  0x1e56, 10, 2, 1, 9,12,11, 4, 6, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x1e6a,  9,10, 3, 5, 1, 6,12,11, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x1e64, 12,10, 2, 6,11, 5, 9, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x1e9c, 10, 3, 9,12,11, 4, 2, 7, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x1e92,  9,11, 4, 1,10, 7,12, 0, 0, 0, 0, 0  },
    {  0xe9,   3,   0x333, 0x1eae, 10, 7,12, 9,11, 5, 3, 1, 2, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x1ea0, 11, 5, 9,12,10, 7, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0x1320, 12, 9, 5, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x132e,  5, 8,12, 9, 1, 3, 2, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1312,  8,12, 9, 1, 4, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x131c,  4, 8,12, 9, 3, 2, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0x13e4, 12, 9, 5, 8, 7, 6, 2, 0, 0, 0, 0, 0  },
    {  0x3c,   2,   0x44,  0x13ea,  6, 1, 3, 7, 8,12, 9, 5, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x13d6,  6, 2, 7, 8,12, 9, 1, 4, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x13d8,  7,12, 9, 3, 6, 4, 8, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1270,  4, 6,12, 9, 5, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x127e,  1, 3, 2, 4, 6,12, 9, 5, 0, 0, 0, 0  },
    {  0xf,    1,   0x4,   0x1242,  9, 1, 6,12, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x124c,  2, 6,12, 9, 3, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x12b4, 12, 9, 5, 4, 2, 7, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x12ba,  9, 3, 7,12, 5, 4, 1, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1286,  7,12, 9, 1, 2, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x1288,  7,12, 9, 3, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x1528, 10, 3, 5, 8,12, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x1526,  5, 8,12,10, 2, 1, 0, 0, 0, 0, 0, 0  },
    {  0x17,   1,   0x6,   0x151a,  3, 1, 4, 8,12,10, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1514, 12,10, 2, 4, 8, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x15ec,  7, 6, 2,10, 3, 5, 8,12, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x15e2,  8, 6, 1, 5,12,10, 7, 0, 0, 0, 0, 0  },
    {  0xe9,   3,   0x333, 0x15de,  2, 3, 1, 6, 4, 8, 7,12,10, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x15d0,  6, 4, 8, 7,12,10, 0, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x1478, 12,10, 3, 5, 4, 6, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x1476,  2, 6,12,10, 1, 5, 4, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x144a,  3, 1, 6,12,10, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x1444,  2, 6,12,10, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x14bc,  2, 3, 5, 4, 7,12,10, 0, 0, 0, 0, 0  },
    {  0xe7,   2,   0x33,  0x14b2,  7,12,10, 1, 5, 4, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x148e,  3, 1, 2,10, 7,12, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0x1480, 10, 7,12, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x3,    1,   0x4,   0xd80,  10,11, 8, 7, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0xd8e,  10,11, 8, 7, 3, 2, 1, 0, 0, 0, 0, 0  },
    {  0x19,   2,   0x34,  0xdb2,   8, 7,10,11, 4, 5, 1, 0, 0, 0, 0, 0  },
    {  0x3c,   2,   0x44,  0xdbc,   2, 4, 5, 3, 7,10,11, 8, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xd44,   6, 2,10,11, 8, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0xd4a,  10,11, 8, 6, 1, 3, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0xd76,   4, 5, 1, 6, 2,10,11, 8, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0xd78,  11, 5, 3,10, 8, 6, 4, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xcd0,   7,10,11, 4, 6, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0xcde,   2, 1, 3, 7,10,11, 4, 6, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0xce2,  11, 5, 1, 6, 7,10, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0xcec,   3,10,11, 5, 2, 6, 7, 0, 0, 0, 0, 0  },
    {  0xf,    1,   0x4,   0xc14,   4, 2,10,11, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xc1a,   3,10,11, 4, 1, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xc26,   1, 2,10,11, 5, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0xc28,   3,10,11, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0xb88,  11, 8, 7, 3, 9, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0xb86,   7, 2, 1, 9,11, 8, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0xbba,   5, 1, 4,11, 8, 7, 3, 9, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0xbb4,   4, 8, 7, 2, 5, 9,11, 0, 0, 0, 0, 0  },
    {  0x17,   1,   0x6,   0xb4c,   9,11, 8, 6, 2, 3, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xb42,   8, 6, 1, 9,11, 0, 0, 0, 0, 0, 0, 0  },
    {  0xe9,   3,   0x333, 0xb7e,   4, 8, 6, 1, 2, 3, 5, 9,11, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0xb70,   5, 9,11, 4, 8, 6, 0, 0, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0xad8,   7, 3, 9,11, 4, 6, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0xad6,   4, 1, 9,11, 6, 7, 2, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0xaea,   3, 1, 6, 7, 9,11, 5, 0, 0, 0, 0, 0  },
    {  0xe7,   2,   0x33,  0xae4,   5, 9,11, 6, 7, 2, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xa1c,   9,11, 4, 2, 3, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0xa12,   4, 1, 9,11, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0xa2e,   9,11, 5, 3, 1, 2, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0xa20,   9,11, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x7,    1,   0x5,   0x7a0,   9, 5, 8, 7,10, 0, 0, 0, 0, 0, 0, 0  },
    {  0x1e,   2,   0x53,  0x7ae,   3, 2, 1, 9, 5, 8, 7,10, 0, 0, 0, 0  },
    {  0x1d,   1,   0x6,   0x792,   9, 1, 4, 8, 7,10, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x79c,   7, 2, 4, 8,10, 9, 3, 0, 0, 0, 0, 0  },
    {  0x1b,   1,   0x6,   0x764,   8, 6, 2,10, 9, 5, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x76a,   1, 5, 8, 6, 3,10, 9, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x34,  0x756,   1, 2,10, 9, 4, 8, 6, 0, 0, 0, 0, 0  },
    {  0xe7,   2,   0x33,  0x758,   3,10, 9, 4, 8, 6, 0, 0, 0, 0, 0, 0  },
    {  0x17,   1,   0x6,   0x6f0,  10, 9, 5, 4, 6, 7, 0, 0, 0, 0, 0, 0  },
    {  0xe9,   3,   0x333, 0x6fe,   1, 5, 4, 2, 6, 7, 3,10, 9, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x6c2,  10, 9, 1, 6, 7, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x6cc,   2, 6, 7, 3,10, 9, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x634,   5, 4, 2,10, 9, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x63a,   5, 4, 1, 9, 3,10, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x606,   1, 2,10, 9, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0x608,   9, 3,10, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf,    1,   0x4,   0x1a8,   8, 7, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x1a6,   1, 5, 8, 7, 2, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x19a,   4, 8, 7, 3, 1, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x194,   4, 8, 7, 2, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0x16c,   2, 3, 5, 8, 6, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x162,   1, 5, 8, 6, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0x15e,   4, 8, 6, 1, 2, 3, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0x150,   6, 4, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf8,   1,   0x5,   0xf8,    6, 7, 3, 5, 4, 0, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   2,   0x33,  0xf6,    1, 5, 4, 2, 6, 7, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0xca,    6, 7, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0xc4,    2, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfc,   1,   0x4,   0x3c,    2, 3, 5, 4, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0x32,    4, 1, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xfe,   1,   0x3,   0xe,     1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0x0,    0,   0x0,   0x0,     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0xdb2,   8,11,10, 7, 4, 1, 5,11, 8, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0xd8e,  10, 7, 8,11, 3, 1, 2, 7,10, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x13e4, 12, 8, 5, 9, 7, 2, 6, 8,12, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x132e,  5, 9,12, 8, 1, 2, 3, 9, 5, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x1e58, 11,12, 6, 4, 9, 3,10,12,11, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x1f06, 10, 9, 1, 2,12, 8,11, 9,10, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x185e,  6, 4,11,12, 2, 3, 1, 4, 6, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x19ca,  6, 7, 3, 1, 8,11,12, 7, 6, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x193c,  5, 4, 2, 3,11,12, 8, 4, 5, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x1e64, 12,10, 2, 6,11, 5, 9,10,12, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x1e92,  9,11, 4, 1,10, 7,12,11, 9, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x13d8,  7,12, 9, 3, 6, 4, 8,12, 7, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x12ba,  9, 3, 7,12, 5, 4, 1, 3, 9, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x15e2,  8, 6, 1, 5,12,10, 7, 6, 8, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x1476,  2, 6,12,10, 1, 5, 4, 6, 2, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x14bc,  2, 3, 5, 4, 7,12,10, 3, 2, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0xd78,  11, 5, 3,10, 8, 6, 4, 5,11, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0xcec,   3,10,11, 5, 2, 6, 7,10, 3, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0xbb4,   4, 8, 7, 2, 5, 9,11, 8, 4, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0xad6,   4, 1, 9,11, 6, 7, 2, 1, 4, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0xaea,   3, 1, 6, 7, 9,11, 5, 1, 3, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x79c,   7, 2, 4, 8,10, 9, 3, 2, 7, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x76a,   1, 5, 8, 6, 3,10, 9, 5, 1, 0, 0, 0  },
    {  0xe6,   2,   0x54,  0x756,   1, 2,10, 9, 4, 8, 6, 2, 1, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x1f08,  9, 3,10,12, 8,11, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x19c4,  8,11,12, 7, 2, 6, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x1932, 11,12, 8, 4, 1, 5, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x1ea0, 11, 5, 9,10, 7,12, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x15d0,  6, 4, 8,12,10, 7, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x148e,  3, 1, 2, 7,12,10, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0xb70,   5, 9,11, 8, 6, 4, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0xa2e,   9,11, 5, 1, 2, 3, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x6cc,   2, 6, 7,10, 9, 3, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x63a,   5, 4, 1, 3,10, 9, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0x15e,   4, 8, 6, 2, 3, 1, 0, 0, 0, 0, 0, 0  },
    {  0xf9,   1,   0x6,   0xf6,    1, 5, 4, 6, 7, 2, 0, 0, 0, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1fcc, 12, 8,11, 9, 3,10, 7, 2, 6, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1f3a,  5, 4, 1, 3,10, 9,11,12, 8, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x19f6,  6, 7, 2, 1, 5, 4, 8,11,12, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1ff0, 11, 5, 9,10, 7,12, 8, 6, 4, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1eae, 10, 7,12,11, 5, 9, 3, 1, 2, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x15de,  2, 3, 1, 4, 8, 6, 7,12,10, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0xb7e,   4, 8, 6, 2, 3, 1, 5, 9,11, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x6fe,   1, 5, 4, 6, 7, 2, 3,10, 9, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1fcc,  8,11,12, 7, 2, 6, 9, 3,10, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1f3a,  4, 1, 5,11,12, 8, 3,10, 9, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x19f6,  7, 2, 6, 8,11,12, 1, 5, 4, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1ff0,  5, 9,11, 8, 6, 4,10, 7,12, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1eae,  7,12,10, 3, 1, 2,11, 5, 9, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x15de,  3, 1, 2, 7,12,10, 4, 8, 6, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0xb7e,   8, 6, 4, 5, 9,11, 2, 3, 1, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x6fe,   5, 4, 1, 3,10, 9, 6, 7, 2, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1fcc,  2, 6, 7,10, 9, 3,12, 8,11, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1f3a, 12, 8,11, 9, 3,10, 5, 4, 1, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x19f6, 11,12, 8, 4, 1, 5, 6, 7, 2, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1ff0,  6, 4, 8,12,10, 7,11, 5, 9, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x1eae,  1, 2, 3, 9,11, 5,10, 7,12, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x15de, 12,10, 7, 6, 4, 8, 2, 3, 1, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0xb7e,   9,11, 5, 1, 2, 3, 4, 8, 6, 0, 0, 0  },
    {  0xe9,   2,   0x36,  0x6fe,  10, 9, 3, 2, 6, 7, 1, 5, 4, 0, 0, 0  }
  };

  static final int sp_cases[] =
  {
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 256, 257, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 258, 000, 000, 259, 000, 000, 000, 000, 000,
    000, 000, 000, 260, 000, 000, 000, 292, 000, 293,
    261, 280, 000, 000, 000, 000, 000, 000, 262, 000,
    000, 294, 263, 281, 264, 282, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
    000, 295, 000, 000, 000, 265, 000, 266, 296, 283,
    000, 000, 000, 000, 000, 000, 000, 267, 000, 000,
    000, 000, 000, 268, 000, 000, 000, 000, 000, 000,
    000, 269, 297, 284, 000, 270, 000, 000, 271, 000,
    285, 000, 000, 000, 000, 000, 000, 000, 000, 272,
    000, 000, 000, 273, 000, 000, 000, 000, 000, 000,
    000, 274, 000, 000, 298, 286, 000, 275, 276, 000,
    000, 000, 287, 000, 000, 000, 000, 277, 000, 278,
    279, 000, 000, 299, 000, 288, 000, 289, 000, 000,
    000, 000, 000, 000, 000, 000, 290, 000, 000, 291,
    000, 000, 000, 000, 000, 000
  };

  static final int case_E9[] =
  {
    Xn, Yp, Zp, incXn, incYn, incZn,
    Xp, Yn, Zp, incYn, incZn, incXn,
    Xp, Yp, Zn, incXn, incYn, incZn,
    Xp, Yp, Zp, incYn, incXn, incZn,
    Xn, Yn, Zp, incYn, incXn, incZn,
    Xn, Yp, Zp, incYn, incXn, incZn,
    Xp, Yn, Zn, incYn, incXn, incZn,
    Xn, Yn, Zn, incXn, incYn, incZn
  };

  static final int NTAB[] =
  {   0,1,2,       1,2,0,       2,0,1,
      0,1,3,2,     1,2,0,3,     2,3,1,0,     3,0,2,1,
      0,1,4,2,3,   1,2,0,3,4,   2,3,1,4,0,   3,4,2,0,1,   4,0,3,1,2,
      0,1,5,2,4,3, 1,2,0,3,5,4, 2,3,1,4,0,5, 3,4,2,5,1,0, 4,5,3,0,2,1,
      5,0,4,1,3,2
  };

  static final int ITAB[] =
  {   0,2,1,       1,0,2,       2,1,0,
      0,3,1,2,     1,0,2,3,     2,1,3,0,     3,2,0,1,
      0,4,1,3,2,   1,0,2,4,3,   2,1,3,0,4,   3,2,4,1,0,   4,3,0,2,1,
      0,5,1,4,2,3, 1,0,2,5,3,4, 2,1,3,0,4,5, 3,2,4,1,5,0, 4,3,5,2,0,1,
      5,4,0,3,1,2
  };

  static final int STAB[] =  { 0, 9, 25, 50 };


  public VisADGeometryArray makeIsoSurface(float isolevel,
         float[] fieldValues, byte[][] color_values, boolean indexed)
         throws VisADException {
    boolean debug = false;

    int      i, NVT, cnt;
    int      size_stripe;
    int      xdim_x_ydim, xdim_x_ydim_x_zdim;
    int      num_cubes, nvertex, npolygons;
    int      ix, iy, ii;
    int nvertex_estimate;

    if (ManifoldDimension != 3) {
      throw new DisplayException("Gridded3DSet.makeIsoSurface: " +
                                 "ManifoldDimension must be 3");
    }

    /* adapt isosurf algorithm to Gridded3DSet variables */
    // NOTE X & Y swap
    int xdim = LengthY;
    int ydim = LengthX;
    int zdim = LengthZ;

    float[] ptGRID = fieldValues;

    xdim_x_ydim = xdim * ydim;
    xdim_x_ydim_x_zdim = xdim_x_ydim * zdim;
    num_cubes = (xdim-1) * (ydim-1) * (zdim-1);

    int[]  ptFLAG = new int[ num_cubes ];
    int[]  ptAUX  = new int[ xdim_x_ydim_x_zdim ];
    int[]  pcube  = new int[ num_cubes+1 ];

    // System.out.println("pre-flags: isolevel = " + isolevel +
    //                    " xdim, ydim, zdim = " + xdim + " " + ydim + " " + zdim);

    npolygons = flags( isolevel, ptFLAG, ptAUX, pcube,
                       ptGRID, xdim, ydim, zdim );

    if (debug) System.out.println("npolygons= "+npolygons);

    if (npolygons == 0) return null;

    // take the garbage out
    pcube = null;

    nvertex_estimate = 4 * npolygons + 100;
    ix = 9 * (nvertex_estimate + 50);
    iy = 7 * npolygons;

    float[][] VX = new float[1][nvertex_estimate];
    float[][] VY = new float[1][nvertex_estimate];
    float[][] VZ = new float[1][nvertex_estimate];

    byte[][] color_temps = null;
    if (color_values != null) {
      color_temps = new byte[color_values.length][];
    }

    int[] Pol_f_Vert = new int[ix];
    int[] Vert_f_Pol = new int[iy];
    int[][] arg_Pol_f_Vert = new int[][] {Pol_f_Vert};

    nvertex = isosurf( isolevel, ptFLAG, nvertex_estimate, npolygons,
                       ptGRID, xdim, ydim, zdim, VX, VY, VZ,
                       color_values, color_temps, arg_Pol_f_Vert, Vert_f_Pol );
    Pol_f_Vert = arg_Pol_f_Vert[0];

    if (nvertex == 0) return null;

    // take the garbage out
    ptFLAG = null;
    ptAUX = null;
/*
for (int j=0; j<nvertex; j++) {
  System.out.println("iso vertex[" + j + "] " + VX[0][j] + " " + VY[0][j] +
                     " " + VZ[0][j]);
}
*/
    float[][] fieldVertices = new float[3][nvertex];
    // NOTE - NO X & Y swap
    System.arraycopy(VX[0], 0, fieldVertices[0], 0, nvertex);
    System.arraycopy(VY[0], 0, fieldVertices[1], 0, nvertex);
    System.arraycopy(VZ[0], 0, fieldVertices[2], 0, nvertex);
    // take the garbage out
    VX = null;
    VY = null;
    VZ = null;

    byte[][] color_levels = null;
    if (color_values != null) {
      color_levels = new byte[color_values.length][nvertex];
      System.arraycopy(color_temps[0], 0, color_levels[0], 0, nvertex);
      System.arraycopy(color_temps[1], 0, color_levels[1], 0, nvertex);
      System.arraycopy(color_temps[2], 0, color_levels[2], 0, nvertex);
      if (color_temps.length > 3) {
        System.arraycopy(color_temps[3], 0, color_levels[3], 0, nvertex);
      }
      // take the garbage out
      color_temps = null;
    }

    if (debug) System.out.println("nvertex= "+nvertex);

    float[] NxA = new float[npolygons];
    float[] NxB = new float[npolygons];
    float[] NyA = new float[npolygons];
    float[] NyB = new float[npolygons];
    float[] NzA = new float[npolygons];
    float[] NzB = new float[npolygons];

    float[] Pnx = new float[npolygons];
    float[] Pny = new float[npolygons];
    float[] Pnz = new float[npolygons];

    float[] NX = new float[nvertex];
    float[] NY = new float[nvertex];
    float[] NZ = new float[nvertex];

    make_normals( fieldVertices[0], fieldVertices[1],  fieldVertices[2],
                  NX, NY, NZ, nvertex, npolygons, Pnx, Pny, Pnz,
                  NxA, NxB, NyA, NyB, NzA, NzB, Pol_f_Vert, Vert_f_Pol);

    // take the garbage out
    NxA = NxB = NyA = NyB = NzA = NzB = Pnx = Pny = Pnz = null;

    float[] normals = new float[3 * nvertex];
    int j = 0;
    for (i=0; i<nvertex; i++) {
      normals[j++] = (float) NX[i];
      normals[j++] = (float) NY[i];
      normals[j++] = (float) NZ[i];
    }
    // take the garbage out
    NX = NY = NZ = null;

    /* ----- Find PolyTriangle Stripe */
    // temporary array to hold maximum possible polytriangle strip
    int[] stripe = new int[6 * npolygons];
    int[] vet_pol = new int[npolygons];
    size_stripe = poly_triangle_stripe( vet_pol, stripe, nvertex,
                                        npolygons, Pol_f_Vert, Vert_f_Pol );

    // take the garbage out
    Pol_f_Vert = null;
    Vert_f_Pol = null;

    if (indexed) {
      VisADIndexedTriangleStripArray array =
        new VisADIndexedTriangleStripArray();

      // set up indices
      array.indexCount = size_stripe;
      array.indices = new int[size_stripe];
      System.arraycopy(stripe, 0, array.indices, 0, size_stripe);
      array.stripVertexCounts = new int[1];
      array.stripVertexCounts[0] = size_stripe;
      // take the garbage out
      stripe = null;

      // set coordinates and colors
      setGeometryArray(array, fieldVertices, 4, color_levels);
      // take the garbage out
      fieldVertices = null;
      color_levels = null;

      // array.vertexFormat |= NORMALS;
      array.normals = normals;

      if (debug) {
        System.out.println("size_stripe= "+size_stripe);
        for(ii=0;ii<size_stripe;ii++) System.out.println(+array.indices[ii]);
      }
      return array;
    }
    else { // if (!indexed)
      VisADTriangleStripArray array = new VisADTriangleStripArray();
      array.stripVertexCounts = new int[] {size_stripe};
      array.vertexCount = size_stripe;

      array.normals = new float[3 * size_stripe];
      int k = 0;
      for (i=0; i<3*size_stripe; i+=3) {
        j = 3 * stripe[k];
        array.normals[i] = normals[j];
        array.normals[i+1] = normals[j+1];
        array.normals[i+2] = normals[j+2];
        k++;
      }
      normals = null;

      array.coordinates = new float[3 * size_stripe];
      k = 0;
      for (i=0; i<3*size_stripe; i+=3) {
        j = stripe[k];
        array.coordinates[i] = fieldVertices[0][j];
        array.coordinates[i+1] = fieldVertices[1][j];
        array.coordinates[i+2] = fieldVertices[2][j];
        k++;
      }
      fieldVertices = null;

      if (color_levels != null) {
        int color_length = color_levels.length;
        array.colors = new byte[color_length * size_stripe];
        k = 0;
        if (color_length == 4) {
          for (i=0; i<color_length*size_stripe; i+=color_length) {
            j = stripe[k];
            array.colors[i] = color_levels[0][j];
            array.colors[i+1] = color_levels[1][j];
            array.colors[i+2] = color_levels[2][j];
            array.colors[i+3] = color_levels[3][j];
            k++;
          }
        }
        else { // if (color_length == 3)
          for (i=0; i<color_length*size_stripe; i+=color_length) {
            j = stripe[k];
            array.colors[i] = color_levels[0][j];
            array.colors[i+1] = color_levels[1][j];
            array.colors[i+2] = color_levels[2][j];
            k++;
          }
        }
      }
      color_levels = null;
      stripe = null;
      return array;
    } // end if (!indexed)
  }

  public static int flags( float isovalue, int[] ptFLAG, int[] ptAUX, int[] pcube,
                            float[] ptGRID, int xdim, int ydim, int zdim ) {
      int ii, jj, ix, iy, iz, cb, SF, bcase;
      int num_cubes, num_cubes_xy, num_cubes_y;
      int xdim_x_ydim = xdim*ydim;
      int xdim_x_ydim_x_zdim = xdim_x_ydim*zdim;
      int npolygons;

      num_cubes_y  = ydim-1;
      num_cubes_xy = (xdim-1) * num_cubes_y;
      num_cubes = (zdim-1) * num_cubes_xy;


    /*
    *************
    Big Attention
    *************
    pcube must have the dimension "num_cubes+1" because in terms of
    eficiency the best loop to calculate "pcube" will use more one
    component to pcube.
    */

    /* Calculate the Flag Value of each Cube */
    /* In order to simplify the Flags calculations, "pcube" will
       be used to store the number of the first vertex of each cube */
    ii = 0;     pcube[0] = 0;  cb = 0;
    for (iz=0; iz<(zdim-1); iz++)
    {   for (ix=0; ix<(xdim-1); ix++)
        {   cb = pcube[ii];
            for (iy=1; iy<(ydim-1); iy++) /* Vectorized */
                pcube[ii+iy] = cb+iy;
            ii += ydim-1;
            pcube[ii] = pcube[ii-1]+2;
        }
        pcube[ii] += ydim;
    }

   /* Vectorized */
    for (ii = 0; ii < xdim_x_ydim_x_zdim; ii++) {
/* WLH 24 Oct 97
        if      (ptGRID[ii] >= INVALID_VALUE) ptAUX[ii] = 0x1001;
        if      (Float.isNaN(ptGRID[ii]) ptAUX[ii] = 0x1001;
*/
        // test for missing
        if      (ptGRID[ii] != ptGRID[ii]) ptAUX[ii] = 0x1001;
        else if (ptGRID[ii] >= isovalue)      ptAUX[ii] = 1;
        else                                  ptAUX[ii] = 0;
    }

   /* Vectorized */
    for (ii = 0; ii < num_cubes; ii++) {
        ptFLAG[ii] = ((ptAUX[ pcube[ii] ]      ) |
                      (ptAUX[ pcube[ii] + ydim ]  << 1) |
                      (ptAUX[ pcube[ii] + 1 ]  << 2) |
                      (ptAUX[ pcube[ii] + ydim + 1 ]  << 3) |
                      (ptAUX[ pcube[ii] + xdim_x_ydim ]  << 4) |
                      (ptAUX[ pcube[ii] + ydim + xdim_x_ydim ]  << 5) |
                      (ptAUX[ pcube[ii] + 1 + xdim_x_ydim ]  << 6) |
                      (ptAUX[ pcube[ii] + 1 + ydim + xdim_x_ydim ]  << 7));
     }
    /* After this Point it is not more used pcube */

    /* Analyse Special Cases in FLAG */
    ii = npolygons = 0;
    while ( TRUE )
    {
        for (; ii < num_cubes; ii++) {
            if ( ((ptFLAG[ii] != 0) && (ptFLAG[ii] != 0xFF)) &&
                 ptFLAG[ii] < MAX_FLAG_NUM) break;
        }

        if ( ii == num_cubes ) break;

        bcase = pol_edges[ptFLAG[ii]][0];
        if (bcase == 0xE6 || bcase == 0xF9) {
            iz = ii/num_cubes_xy;
            ix = (int)((ii - (iz*num_cubes_xy))/num_cubes_y);
            iy = ii - (iz*num_cubes_xy) - (ix*num_cubes_y);

        /* == Z+ == */
            if      ((ptFLAG[ii] & 0xF0) == 0x90 ||
                     (ptFLAG[ii] & 0xF0) == 0x60) {
                   cb = (iz < (zdim - 1)) ? ii + num_cubes_xy : -1 ;
              }
        /* == Z- == */
            else if ((ptFLAG[ii] & 0x0F) == 0x09 ||
                     (ptFLAG[ii] & 0x0F) == 0x06) {
                   cb = (iz > 0) ? ii - num_cubes_xy : -1 ;
              }
        /* == Y+ == */
            else if ((ptFLAG[ii] & 0xCC) == 0x84 ||
                     (ptFLAG[ii] & 0xCC) == 0x48) {
                   cb = (iy < (ydim - 1)) ? ii + 1 : -1 ;
              }
        /* == Y- == */
            else if ((ptFLAG[ii] & 0x33) == 0x21 ||
                     (ptFLAG[ii] & 0x33) == 0x12) {
                   cb = (iy > 0) ? ii - 1 : -1 ;
              }
        /* == X+ == */
            else if ((ptFLAG[ii] & 0xAA) == 0x82 ||
                     (ptFLAG[ii] & 0xAA) == 0x28) {
                   cb = (ix < (xdim - 1)) ? ii + num_cubes_y : -1 ;
              }
        /* == X- == */
            else if ((ptFLAG[ii] & 0x55) == 0x41 ||
                     (ptFLAG[ii] & 0x55) == 0x14) {
                   cb = (ix > 0) ? ii - num_cubes_y : -1 ;
              }
        /* == Map Special Case == */
            if  ((cb > -1 && cb < num_cubes) && ptFLAG[cb]<316)  /*changed by BEP on 7-20-92*/
            {   bcase = pol_edges[ptFLAG[cb]][0];
                if (bcase == 0x06 || bcase == 0x16 ||
                    bcase == 0x19 || bcase == 0x1E ||
                    bcase == 0x3C || bcase == 0x69) {
                    ptFLAG[ii] = sp_cases[ptFLAG[ii]];
                }
            }
        }
        else if (bcase == 0xE9) {
            iz = ii/num_cubes_xy;
            ix = (int)((ii - (iz*num_cubes_xy))/num_cubes_y);
            iy = ii - (iz*num_cubes_xy) - (ix*num_cubes_y);

               SF = 0;
            if      (ptFLAG[ii] == 0x6B) SF = SF_6B;
            else if (ptFLAG[ii] == 0x6D) SF = SF_6D;
            else if (ptFLAG[ii] == 0x79) SF = SF_79;
            else if (ptFLAG[ii] == 0x97) SF = SF_97;
            else if (ptFLAG[ii] == 0x9E) SF = SF_9E;
            else if (ptFLAG[ii] == 0xB6) SF = SF_B6;
            else if (ptFLAG[ii] == 0xD6) SF = SF_D6;
            else if (ptFLAG[ii] == 0xE9) SF = SF_E9;
            for (jj=0; jj<3; jj++) {
                if      (case_E9[jj+SF] == Zp) {
                     cb = (iz < (zdim - 1)) ? ii + num_cubes_xy : -1 ;
                  }
                else if (case_E9[jj+SF] == Zn) {
                     cb = (iz > 0) ? ii - num_cubes_xy : -1 ;
                  }
                else if (case_E9[jj+SF] == Yp) {
                     cb = (iy < (ydim - 1)) ? ii + 1 : -1 ;
                  }
                else if (case_E9[jj+SF] == Yn) {
                     cb = (iy > 0) ? ii - 1 : -1 ;
                  }
                else if (case_E9[jj+SF] == Xp) {
                     cb = (ix < (xdim - 1)) ? ii + num_cubes_y : -1 ;
                  }
                else if (case_E9[jj+SF] == Xn) {
                     cb = (ix > 0) ? ii - num_cubes_y : -1 ;
                  }
       /* changed:
                if  ((cb > -1 && cb < num_cubes))
          to: */
                if  ((cb > -1 && cb < num_cubes) && ptFLAG[cb]<316)
       /* changed by BEP on 7-20-92*/
                {   bcase = pol_edges[ptFLAG[cb]][0];
                    if (bcase == 0x06 || bcase == 0x16 ||
                        bcase == 0x19 || bcase == 0x1E ||
                        bcase == 0x3C || bcase == 0x69)
                    {
                        ptFLAG[ii] = sp_cases[ptFLAG[ii]] +
                                     case_E9[jj+SF+3];
                        break;
                    }
                }
            }
        }

        /* Calculate the Number of Generated Triangles and Polygons */
        npolygons  += pol_edges[ptFLAG[ii]][1];
        ii++;
    }

     /*  npolygons2 = 2*npolygons; */

    return npolygons;
  }

  private int isosurf( float isovalue, int[] ptFLAG, int nvertex_estimate,
                       int npolygons, float[] ptGRID, int xdim, int ydim,
                       int zdim, float[][] VX, float[][] VY, float[][] VZ,
                       byte[][] auxValues, byte[][] auxLevels,
                       int[][] Pol_f_Vert, int[] Vert_f_Pol )
          throws VisADException {

    int  ix, iy, iz, caseA, above, bellow, front, rear, mm, nn;
    int  ii, jj, kk, ncube, cpl, pvp, pa, ve;
    int[] calc_edge = new int[13];
    int  xx, yy, zz;
    float    cp;
    float  vnode0 = 0;
    float  vnode1 = 0;
    float  vnode2 = 0;
    float  vnode3 = 0;
    float  vnode4 = 0;
    float  vnode5 = 0;
    float  vnode6 = 0;
    float  vnode7 = 0;
    int  pt = 0;
    int  n_pol;
    int  aa;
    int  bb;
    int  temp;
    float  nodeDiff;
    int xdim_x_ydim = xdim*ydim;
    int nvet;

    int t;

    float[][] samples = getSamples(false);

    int naux = (auxValues != null) ? auxValues.length : 0;
    if (naux > 0) {
      if (auxLevels == null || auxLevels.length != naux) {
        throw new SetException("Gridded3DSet.isosurf: "
                              +"auxLevels length " + auxLevels.length +
                               " doesn't match expected " + naux);
      }
      for (int i=0; i<naux; i++) {
        if (auxValues[i].length != Length) {
          throw new SetException("Gridded3DSet.isosurf: expected auxValues " +
                                " length#" + i + " to be " + Length +
                                 ", not " + auxValues[i].length);
        }
      }
    }
    else {
      if (auxLevels != null) {
        throw new SetException("Gridded3DSet.isosurf: "
                              +"auxValues null but auxLevels not null");
      }
    }

    // temporary storage of auxLevels structure
    byte[][] tempaux = (naux > 0) ? new byte[naux][nvertex_estimate] : null;

    bellow = rear = 0;  above = front = 1;

    /* Initialize the Auxiliar Arrays of Pointers */
/* WLH 25 Oct 97
    ix = 9 * (npolygons*2 + 50);
    iy = 7 * npolygons;
    ii = ix + iy;
*/
    for (jj=0; jj<Pol_f_Vert[0].length; jj++) {
      Pol_f_Vert[0][jj] = BIG_NEG;
    }
    for (jj=8; jj<Pol_f_Vert[0].length; jj+=9) {
      Pol_f_Vert[0][jj] = 0;
    }
    for (jj=0; jj<Vert_f_Pol.length; jj++) {
      Vert_f_Pol[jj] = BIG_NEG;
    }
    for (jj=6; jj<Vert_f_Pol.length; jj+=7) {
      Vert_f_Pol[jj] = 0;
    }

    /* Allocate the auxiliar edge vectors
    size ixPlane = (xdim - 1) * ydim = xdim_x_ydim - ydim
    size iyPlane = (ydim - 1) * xdim = xdim_x_ydim - xdim
    size izPlane = xdim
    */

    xx = xdim_x_ydim - ydim;
    yy = xdim_x_ydim - xdim;
    zz = ydim;
    ii = 2 * (xx + yy + zz);

    int[] P_array = new int[ii];

    /* Calculate the Vertex of the Polygons which edges were
       calculated above */
    nvet = ncube = cpl = pvp = 0;


        for ( iz = 0; iz < zdim - 1; iz++ ) {

            for ( ix = 0; ix < xdim - 1; ix++ ) {

                for ( iy = 0; iy < ydim - 1; iy++ ) {
                    if ( (ptFLAG[ncube] != 0 & ptFLAG[ncube] != 0xFF) ) {

                        if (nvet + 12 > nvertex_estimate) {
                          // allocate more space
                          nvertex_estimate = 2 * (nvet + 12);
                          if (naux > 0) {
                            for (int i=0; i<naux; i++) {
                              byte[] tt = tempaux[i];
                              tempaux[i] = new byte[nvertex_estimate];
                              System.arraycopy(tt, 0, tempaux[i], 0, nvet);
                            }
                          }
                          float[] tt = VX[0];
                          VX[0] = new float[nvertex_estimate];
                          System.arraycopy(tt, 0, VX[0], 0, tt.length);
                          tt = VY[0];
                          VY[0] = new float[nvertex_estimate];
                          System.arraycopy(tt, 0, VY[0], 0, tt.length);
                          tt = VZ[0];
                          VZ[0] = new float[nvertex_estimate];
                          System.arraycopy(tt, 0, VZ[0], 0, tt.length);
                          int big_ix = 9 * (nvertex_estimate + 50);
                          int[] it = Pol_f_Vert[0];
                          Pol_f_Vert[0] = new int[big_ix];
                          for (jj=0; jj<Pol_f_Vert[0].length; jj++) {
                            Pol_f_Vert[0][jj] = BIG_NEG;
                          }
                          for (jj=8; jj<Pol_f_Vert[0].length; jj+=9) {
                            Pol_f_Vert[0][jj] = 0;
                          }
                          System.arraycopy(it, 0, Pol_f_Vert[0], 0, it.length);
                        }

           /* WLH 2 April 99 */
           vnode0 = ptGRID[pt];
           vnode1 = ptGRID[pt + ydim];
           vnode2 = ptGRID[pt + 1];
           vnode3 = ptGRID[pt + ydim + 1];
           vnode4 = ptGRID[pt + xdim_x_ydim];
           vnode5 = ptGRID[pt + ydim + xdim_x_ydim];
           vnode6 = ptGRID[pt + 1 + xdim_x_ydim];
           vnode7 = ptGRID[pt + 1 + ydim + xdim_x_ydim];

                        if ( (ptFLAG[ncube] < MAX_FLAG_NUM) ) {
                        /*  fill_Vert_f_Pol(ncube); */

                                  kk  = pol_edges[ptFLAG[ncube]][2];
                                  aa = ptFLAG[ncube];
                                  bb = 4;
                                  pa  = pvp;
                                  n_pol = pol_edges[ptFLAG[ncube]][1];
                                  for (ii=0; ii < n_pol; ii++) {
                                      Vert_f_Pol[pa+6] = ve = kk&MASK;
                                      ve+=pa;
                                      for (jj=pa; jj<ve && jj<pa+6; jj++) {

                                            Vert_f_Pol[jj] = pol_edges[aa][bb];
                                            bb++;
                                            if (bb >= 16) {
                                                aa++;
                                                bb -= 16;
                                            }
                                      }
                                           kk >>= 4;    pa += 7;
                                  }
                        /* end  fill_Vert_f_Pol(ncube); */
                        /* */

         /* find_vertex(); */
/* WLH 2 April 99
           vnode0 = ptGRID[pt];
           vnode1 = ptGRID[pt + ydim];
           vnode2 = ptGRID[pt + 1];
           vnode3 = ptGRID[pt + ydim + 1];
           vnode4 = ptGRID[pt + xdim_x_ydim];
           vnode5 = ptGRID[pt + ydim + xdim_x_ydim];
           vnode6 = ptGRID[pt + 1 + xdim_x_ydim];
           vnode7 = ptGRID[pt + 1 + ydim + xdim_x_ydim];
*/


   if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0002) != 0) ) {  /* cube vertex 0-1 */
         if ( (iz != 0) || (iy != 0) ) {
           calc_edge[1] = P_array[ bellow*xx + ix*ydim + iy ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode1 - vnode0;
             cp = ( ( isovalue - vnode0 ) / nodeDiff ) + ix;
             VX[0][nvet] = cp;
             VY[0][nvet] = iy;
             VZ[0][nvet] = iz;
*/
             cp = ( ( isovalue - vnode0 ) / ( vnode1 - vnode0 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + ydim] + (1.0f-cp) * samples[0][pt];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim] + (1.0f-cp) * samples[1][pt];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim] + (1.0f-cp) * samples[2][pt];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt] < 0) ?
                     ((float) auxValues[j][pt]) + 256.0f :
                     ((float) auxValues[j][pt]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim] +
                                  (1.0f-cp) * auxValues[j][pt];
*/
             }

             calc_edge[1] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0004) != 0) ) {  /* cube vertex 0-2 */
         if ( (iz != 0) || (ix != 0) ) {
           calc_edge[2] = P_array[ 2*xx + bellow*yy + iy*xdim + ix ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode2 - vnode0;
             cp = ( ( isovalue - vnode0 ) / nodeDiff ) + iy;
             VX[0][nvet] = ix;
             VY[0][nvet] = cp;
             VZ[0][nvet] = iz;
*/
             cp = ( ( isovalue - vnode0 ) / ( vnode2 - vnode0 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + 1] + (1.0f-cp) * samples[0][pt];
             VY[0][nvet] = (float) cp * samples[1][pt + 1] + (1.0f-cp) * samples[1][pt];
             VZ[0][nvet] = (float) cp * samples[2][pt + 1] + (1.0f-cp) * samples[2][pt];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + 1] < 0) ?
                     ((float) auxValues[j][pt + 1]) + 256.0f :
                     ((float) auxValues[j][pt + 1]) ) +
                   (1.0f - cp) * ((auxValues[j][pt] < 0) ?
                     ((float) auxValues[j][pt]) + 256.0f :
                     ((float) auxValues[j][pt]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1] +
                                  (1.0f-cp) * auxValues[j][pt];
*/
             }

             calc_edge[2] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0008) != 0) ) {  /* cube vertex 0-4 */
         if ( (ix != 0) || (iy != 0) ) {
           calc_edge[3] = P_array[ 2*xx + 2*yy + rear*zz + iy ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode4 - vnode0;
             cp = ( ( isovalue - vnode0 ) / nodeDiff ) + iz;
             VX[0][nvet] = ix;
             VY[0][nvet] = iy;
             VZ[0][nvet] = cp;
*/
             cp = ( ( isovalue - vnode0 ) / ( vnode4 - vnode0 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt];
             VY[0][nvet] = (float) cp * samples[1][pt + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt];
             VZ[0][nvet] = (float) cp * samples[2][pt + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt] < 0) ?
                     ((float) auxValues[j][pt]) + 256.0f :
                     ((float) auxValues[j][pt]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt];
*/
             }

             calc_edge[3] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0010) != 0) ) {  /* cube vertex 1-3 */
         if ( (iz != 0) ) {
           calc_edge[4] =  P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode3 - vnode1;
             cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iy;
             VX[0][nvet] = ix+1;
             VY[0][nvet] = cp;
             VZ[0][nvet] = iz;
*/
             cp = ( ( isovalue - vnode1 ) / ( vnode3 - vnode1 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + 1] +
                        (1.0f-cp) * samples[0][pt + ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + 1] +
                        (1.0f-cp) * samples[1][pt + ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + 1] +
                        (1.0f-cp) * samples[2][pt + ydim];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                     ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + 1]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                  (1.0f-cp) * auxValues[j][pt + ydim];
*/
             }

             calc_edge[4] = nvet;
             P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0020) != 0) ) {  /* cube vertex 1-5 */
         if ( (iy != 0) ) {
           calc_edge[5] = P_array[ 2*xx + 2*yy + front*zz + iy ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode5 - vnode1;
             cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iz;
             VX[0][nvet] = ix+1;
             VY[0][nvet] = iy;
             VZ[0][nvet] = cp;
*/
             cp = ( ( isovalue - vnode1 ) / ( vnode5 - vnode1 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + ydim];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + ydim];
*/
             }

             calc_edge[5] = nvet;
             P_array[ 2*xx + 2*yy + front*zz + iy ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0040) != 0) ) {  /* cube vertex 2-3 */
         if ( (iz != 0) ) {
           calc_edge[6] = P_array[ bellow*xx + ix*ydim + (iy+1) ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode3 - vnode2;
             cp = ( ( isovalue - vnode2 ) / nodeDiff ) + ix;
             VX[0][nvet] = cp;
             VY[0][nvet] = iy+1;
             VZ[0][nvet] = iz;
*/
             cp = ( ( isovalue - vnode2 ) / ( vnode3 - vnode2 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + 1] +
                        (1.0f-cp) * samples[0][pt + 1];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + 1] +
                        (1.0f-cp) * samples[1][pt + 1];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + 1] +
                        (1.0f-cp) * samples[2][pt + 1];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                     ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + 1]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                     ((float) auxValues[j][pt + 1]) + 256.0f :
                     ((float) auxValues[j][pt + 1]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                  (1.0f-cp) * auxValues[j][pt + 1];
*/
             }

             calc_edge[6] = nvet;
             P_array[ bellow*xx + ix*ydim + (iy+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0080) != 0) ) {  /* cube vertex 2-6 */
         if ( (ix != 0) ) {
           calc_edge[7] = P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode6 - vnode2;
             cp = ( ( isovalue - vnode2 ) / nodeDiff ) + iz;
             VX[0][nvet] = ix;
             VY[0][nvet] = iy+1;
             VZ[0][nvet] = cp;
*/
             cp = ( ( isovalue - vnode2 ) / ( vnode6 - vnode2 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + 1];
             VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + 1];
             VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + 1];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                     ((float) auxValues[j][pt + 1]) + 256.0f :
                     ((float) auxValues[j][pt + 1]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + 1];
*/
             }

             calc_edge[7] = nvet;
             P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0100) != 0) ) {  /* cube vertex 3-7 */
/* WLH 26 Oct 97
         nodeDiff = vnode7 - vnode3;
         cp = ( ( isovalue - vnode3 ) / nodeDiff ) + iz;
         VX[0][nvet] = ix+1;
         VY[0][nvet] = iy+1;
         VZ[0][nvet] = cp;
*/
         cp = ( ( isovalue - vnode3 ) / ( vnode7 - vnode3 ) );
         VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[0][pt + ydim + 1];
         VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[1][pt + ydim + 1];
         VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[2][pt + ydim + 1];

         for (int j=0; j<naux; j++) {
           t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
               (1.0f - cp) * ((auxValues[j][pt + ydim + 1] < 0) ?
                 ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                 ((float) auxValues[j][pt + ydim + 1]) ) );
           tempaux[j][nvet] = (byte)
             ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
           tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                              (1.0f-cp) * auxValues[j][pt + ydim + 1];
*/
         }

         calc_edge[8] = nvet;
         P_array[ 2*xx + 2*yy + front*zz + (iy+1) ] = nvet;
         nvet++;
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0200) != 0) ) {  /* cube vertex 4-5 */
         if ( (iy != 0) ) {
           calc_edge[9] = P_array[ above*xx + ix*ydim + iy ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode5 - vnode4;
             cp = ( ( isovalue - vnode4 ) / nodeDiff ) + ix;
             VX[0][nvet] = cp;
             VY[0][nvet] = iy;
             VZ[0][nvet] = iz+1;
*/
             cp = ( ( isovalue - vnode4 ) / ( vnode5 - vnode4 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + xdim_x_ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + xdim_x_ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + xdim_x_ydim];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + xdim_x_ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
             }

             calc_edge[9] = nvet;
             P_array[ above*xx + ix*ydim + iy ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0400) != 0) ) {  /* cube vertex 4-6 */
         if ( (ix != 0) ) {
           calc_edge[10] = P_array[ 2*xx + above*yy + iy*xdim + ix ];
         }
         else {
/* WLH 26 Oct 97
             nodeDiff = vnode6 - vnode4;
             cp = ( ( isovalue - vnode4 ) / nodeDiff ) + iy;
             VX[0][nvet] = ix;
             VY[0][nvet] = cp;
             VZ[0][nvet] = iz+1;
*/
             cp = ( ( isovalue - vnode4 ) / ( vnode6 - vnode4 ) );
             VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[0][pt + xdim_x_ydim];
             VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[1][pt + xdim_x_ydim];
             VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                        (1.0f-cp) * samples[2][pt + xdim_x_ydim];

             for (int j=0; j<naux; j++) {
               t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                   (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                     ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                     ((float) auxValues[j][pt + xdim_x_ydim]) ) );
               tempaux[j][nvet] = (byte)
                 ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
               tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                  (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
             }

             calc_edge[10] = nvet;
             P_array[ 2*xx + above*yy + iy*xdim + ix ] = nvet;
             nvet++;
         }
     }
    if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0800) != 0) ) {  /* cube vertex 5-7 */
/* WLH 26 Oct 97
         nodeDiff = vnode7 - vnode5;
         cp = ( ( isovalue - vnode5 ) / nodeDiff ) + iy;
         VX[0][nvet] = ix+1;
         VY[0][nvet] = cp;
         VZ[0][nvet] = iz+1;
*/
         cp = ( ( isovalue - vnode5 ) / ( vnode7 - vnode5 ) );
         VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[0][pt + ydim + xdim_x_ydim];
         VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[1][pt + ydim + xdim_x_ydim];
         VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[2][pt + ydim + xdim_x_ydim];

         for (int j=0; j<naux; j++) {
           t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
               (1.0f - cp) * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) );
           tempaux[j][nvet] = (byte)
             ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
           tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                              (1.0f-cp) * auxValues[j][pt + ydim + xdim_x_ydim];
*/
         }

         calc_edge[11] = nvet;
         P_array[ 2*xx + above*yy + iy*xdim + (ix+1) ] = nvet;
         nvet++;
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x1000) != 0) ) {  /* cube vertex 6-7 */
/* WLH 26 Oct 97
         nodeDiff = vnode7 - vnode6;
         cp = ( ( isovalue - vnode6 ) / nodeDiff ) + ix;
         VX[0][nvet] = cp;
         VY[0][nvet] = iy+1;
         VZ[0][nvet] = iz+1;
*/
         cp = ( ( isovalue - vnode6 ) / ( vnode7 - vnode6 ) );
         VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[0][pt + 1 + xdim_x_ydim];
         VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[1][pt + 1 + xdim_x_ydim];
         VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                    (1.0f-cp) * samples[2][pt + 1 + xdim_x_ydim];

         for (int j=0; j<naux; j++) {
           t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
               (1.0f - cp) * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                 ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                 ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) );
           tempaux[j][nvet] = (byte)
             ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
           tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                              (1.0f-cp) * auxValues[j][pt + 1 + xdim_x_ydim];
*/
         }

         calc_edge[12] = nvet;
         P_array[ above*xx + ix*ydim + (iy+1) ] = nvet;
         nvet++;
     }

         /* end  find_vertex(); */
                         /* update_data_structure(ncube); */
                             kk = pol_edges[ptFLAG[ncube]][2];
                             nn = pol_edges[ptFLAG[ncube]][1];
                             for (ii=0; ii<nn; ii++) {
                                  mm = pvp+(kk&MASK);
                                  for (jj=pvp; jj<mm; jj++) {
                                      Vert_f_Pol [jj] = ve = calc_edge[Vert_f_Pol [jj]];
                            //        Pol_f_Vert[0][ve*9 + (Pol_f_Vert[0][ve*9 + 8])++]  = cpl;
                                      temp = Pol_f_Vert[0][ve*9 + 8];
                                      Pol_f_Vert[0][ve*9 + temp] = cpl;
                                      Pol_f_Vert[0][ve*9 + 8] = temp + 1;
                                  }
                                  kk >>= 4;    pvp += 7;    cpl++;
                             }
                         /* end  update_data_structure(ncube); */
                        }
                        else { // !(ptFLAG[ncube] < MAX_FLAG_NUM)
       /* find_vertex_invalid_cube(ncube); */

    ptFLAG[ncube] &= 0x1FF;
    if ( (ptFLAG[ncube] != 0 & ptFLAG[ncube] != 0xFF) )
    { if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0010) != 0) )     /* cube vertex 1-3 */
/* WLH 24 Oct 97
      {   if (!(iz != 0 ) && vnode3 < INV_VAL && vnode1 < INV_VAL)
      {   if (!(iz != 0 ) && !Float.isNaN(vnode3) && !Float.isNaN(vnode1))
*/
      // test for not missing
      {   if (!(iz != 0 ) && vnode3 == vnode3 && vnode1 == vnode1)
        {
/* WLH 26 Oct 97
              nodeDiff = vnode3 - vnode1;
              cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iy;
              VX[0][nvet] = ix+1;
              VY[0][nvet] = cp;
              VZ[0][nvet] = iz;
*/
              cp = ( ( isovalue - vnode1 ) / ( vnode3 - vnode1 ) );
              // WLH 4 Aug 2000 - replace Samples by samples
              VX[0][nvet] = (float) cp * samples[0][pt + ydim + 1] +
                         (1.0f-cp) * samples[0][pt + ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + ydim + 1] +
                         (1.0f-cp) * samples[1][pt + ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + ydim + 1] +
                         (1.0f-cp) * samples[2][pt + ydim];
              // end WLH 4 Aug 2000 - replace Samples by samples

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                      ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + 1]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                   (1.0f-cp) * auxValues[j][pt + ydim];
*/
              }

              P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0020) != 0) )    /* cube vertex 1-5 */
/* WLH 24 Oct 97
      {   if (!(iy != 0) && vnode5 < INV_VAL && vnode1 < INV_VAL)
      {   if (!(iy != 0) && !Float.isNaN(vnode5) && !Float.isNaN(vnode1))
*/
      // test for not missing
      {   if (!(iy != 0) && vnode5 == vnode5 && vnode1 == vnode1)
        {
/* WLH 26 Oct 97
              nodeDiff = vnode5 - vnode1;
              cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iz;
              VX[0][nvet] = ix+1;
              VY[0][nvet] = iy;
              VZ[0][nvet] = cp;
*/
              cp = ( ( isovalue - vnode1 ) / ( vnode5 - vnode1 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + ydim];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + ydim];
*/
              }

              P_array[ 2*xx + 2*yy + front*zz + iy ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0040) != 0) )     /* cube vertex 2-3 */
/* WLH 24 Oct 97
      {   if (!(iz != 0) && vnode3 < INV_VAL && vnode2 < INV_VAL)
      {   if (!(iz != 0) && !Float.isNaN(vnode3) && !Float.isNaN(vnode2))
*/
      // test for not missing
      {   if (!(iz != 0) && vnode3 == vnode3 && vnode2 == vnode2)
        {
/* WLH 26 Oct 97
              nodeDiff = vnode3 - vnode2;
              cp = ( ( isovalue - vnode2 ) / nodeDiff ) + ix;
              VX[0][nvet] = cp;
              VY[0][nvet] = iy+1;
              VZ[0][nvet] = iz;
*/
              cp = ( ( isovalue - vnode2 ) / ( vnode3 - vnode2 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + ydim + 1] +
                         (1.0f-cp) * samples[0][pt + 1];
              VY[0][nvet] = (float) cp * samples[1][pt + ydim + 1] +
                         (1.0f-cp) * samples[1][pt + 1];
              VZ[0][nvet] = (float) cp * samples[2][pt + ydim + 1] +
                         (1.0f-cp) * samples[2][pt + 1];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + 1] < 0) ?
                      ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + 1]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                      ((float) auxValues[j][pt + 1]) + 256.0f :
                      ((float) auxValues[j][pt + 1]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + 1] +
                                   (1.0f-cp) * auxValues[j][pt + 1];
*/
              }

              P_array[ bellow*xx + ix*ydim + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0080) != 0) )  /* cube vertex 2-6 */
/* WLH 24 Oct 97
      {   if (!(ix != 0) && vnode6 < INV_VAL && vnode2 < INV_VAL)
      {   if (!(ix != 0) && !Float.isNaN(vnode6) && !Float.isNaN(vnode2))
*/
      // test for not missing
      {   if (!(ix != 0) && vnode6 == vnode6 && vnode2 == vnode2)
        {
/* WLH 26 Oct 97
              nodeDiff = vnode6 - vnode2;
              cp = ( ( isovalue - vnode2 ) / nodeDiff ) + iz;
              VX[0][nvet] = ix;
              VY[0][nvet] = iy+1;
              VZ[0][nvet] = cp;
*/
              cp = ( ( isovalue - vnode2 ) / ( vnode6 - vnode2 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + 1];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + 1];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + 1];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + 1] < 0) ?
                      ((float) auxValues[j][pt + 1]) + 256.0f :
                      ((float) auxValues[j][pt + 1]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + 1];
*/
              }

              P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0100) != 0) )     /* cube vertex 3-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode3 < INV_VAL)
      {   if (!Float.isNaN(vnode7) && !Float.isNaN(vnode3))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode3 == vnode3)
          {
/* WLH 26 Oct 97
              nodeDiff = vnode7 - vnode3;
              cp = ( ( isovalue - vnode3 ) / nodeDiff ) + iz;
              VX[0][nvet] = ix+1;
              VY[0][nvet] = iy+1;
              VZ[0][nvet] = cp;
*/
              cp = ( ( isovalue - vnode3 ) / ( vnode7 - vnode3 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + ydim + 1];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + ydim + 1];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + ydim + 1];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim + 1] < 0) ?
                      ((float) auxValues[j][pt + ydim + 1]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + 1]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + ydim + 1];
*/
              }

              P_array[ 2*xx + 2*yy + front*zz + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0200) != 0) )    /* cube vertex 4-5 */
/* WLH 24 Oct 97
      {   if (!(iy != 0) && vnode5 < INV_VAL && vnode4 < INV_VAL)
      {   if (!(iy != 0) && !Float.isNaN(vnode5) && !Float.isNaN(vnode4))
*/
      // test for not missing
      {   if (!(iy != 0) && vnode5 == vnode5 && vnode4 == vnode4)
        {
/* WLH 26 Oct 97
              nodeDiff = vnode5 - vnode4;
              cp = ( ( isovalue - vnode4 ) / nodeDiff ) + ix;
              VX[0][nvet] = cp;
              VY[0][nvet] = iy;
              VZ[0][nvet] = iz+1;
*/
              cp = ( ( isovalue - vnode4 ) / ( vnode5 - vnode4 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + xdim_x_ydim];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
              }

              P_array[ above*xx + ix*ydim + iy ] = nvet;
              nvet++;
          }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0400) != 0) )     /* cube vertex 4-6 */
/* WLH 24 Oct 97
      {   if (!(ix != 0) && vnode6 < INV_VAL && vnode4 < INV_VAL)
      {   if (!(ix != 0) && !Float.isNaN(vnode6) && !Float.isNaN(vnode4))
*/
      // test for not missing
      {   if (!(ix != 0) && vnode6 == vnode6 && vnode4 == vnode4)
          {
/* WLH 26 Oct 97
              nodeDiff = vnode6 - vnode4;
              cp = ( ( isovalue - vnode4 ) / nodeDiff ) + iy;
              VX[0][nvet] = ix;
              VY[0][nvet] = cp;
              VZ[0][nvet] = iz+1;
*/
              cp = ( ( isovalue - vnode4 ) / ( vnode6 - vnode4 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + xdim_x_ydim];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + xdim_x_ydim];
*/
              }

              P_array[ 2*xx + above*yy + iy*xdim + ix ] = nvet;
              nvet++;
          }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0800) != 0) )     /* cube vertex 5-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode5 < INV_VAL)
      {   if (!Float.isNaN(vnode7) && !Float.isNaN(vnode5))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode5 == vnode5)
        {
/* WLH 26 Oct 97
              nodeDiff = vnode7 - vnode5;
              cp = ( ( isovalue - vnode5 ) / nodeDiff ) + iy;
              VX[0][nvet] = ix+1;
              VY[0][nvet] = cp;
              VZ[0][nvet] = iz+1;
*/
              cp = ( ( isovalue - vnode5 ) / ( vnode7 - vnode5 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + ydim + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + ydim + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + ydim + xdim_x_ydim];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + ydim + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + ydim + xdim_x_ydim];
*/
              }

              P_array[ 2*xx + above*yy + iy*xdim + (ix+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x1000) != 0) )     /* cube vertex 6-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode6 < INV_VAL)
      {   if (!Float.isNaN(vnode7) && !Float.isNaN(vnode6))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode6 == vnode6)
        {
/* WLH 26 Oct 97
              nodeDiff = vnode7 - vnode6;
              cp = ( ( isovalue - vnode6 ) / nodeDiff ) + ix;
              VX[0][nvet] = cp;
              VY[0][nvet] = iy+1;
              VZ[0][nvet] = iz+1;
*/
              cp = ( ( isovalue - vnode6 ) / ( vnode7 - vnode6 ) );
              VX[0][nvet] = (float) cp * samples[0][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[0][pt + 1 + xdim_x_ydim];
              VY[0][nvet] = (float) cp * samples[1][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[1][pt + 1 + xdim_x_ydim];
              VZ[0][nvet] = (float) cp * samples[2][pt + 1 + ydim + xdim_x_ydim] +
                         (1.0f-cp) * samples[2][pt + 1 + xdim_x_ydim];

              for (int j=0; j<naux; j++) {
                t = (int) ( cp * ((auxValues[j][pt + 1 + ydim + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + ydim + xdim_x_ydim]) ) +
                    (1.0f - cp) * ((auxValues[j][pt + 1 + xdim_x_ydim] < 0) ?
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) + 256.0f :
                      ((float) auxValues[j][pt + 1 + xdim_x_ydim]) ) );
                tempaux[j][nvet] = (byte)
                  ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                tempaux[j][nvet] = (float) cp * auxValues[j][pt + 1 + ydim + xdim_x_ydim] +
                                   (1.0f-cp) * auxValues[j][pt + 1 + xdim_x_ydim];
*/
              }

              P_array[ above*xx + ix*ydim + (iy+1) ] = nvet;
              nvet++;
        }
      }
     }
        /* end  find_vertex_invalid_cube(ncube); */

                        }
                    } /* end  if (exist_polygon_in_cube(ncube)) */
                    ncube++; pt++;
                } /* end  for ( iy = 0; iy < ydim - 1; iy++ ) */
             /* swap_planes(Z,rear,front); */
                caseA = rear;
                rear = front;
                front = caseA;
                pt++;
             /* end  swap_planes(Z,rear,front); */
            } /* end  for ( ix = 0; ix < xdim - 1; ix++ ) */
           /*  swap_planes(XY,bellow,above); */
               caseA = bellow;
               bellow = above;
               above = caseA;
            pt += ydim;
           /* end  swap_planes(XY,bellow,above); */
        } /* end  for ( iz = 0; iz < zdim - 1; iz++ ) */

    // copy tempaux array into auxLevels array
    for (int i=0; i<naux; i++) {
      auxLevels[i] = new byte[nvet];
      System.arraycopy(tempaux[i], 0, auxLevels[i], 0, nvet);
    }

    return nvet;
  }

  public static void make_normals( float[] VX, float[] VY, float[] VZ,
                     float[] NX, float[] NY, float[] NZ, int nvertex,
                     int npolygons, float[] Pnx, float[] Pny, float[] Pnz,
                     float[] NxA, float[] NxB, float[] NyA, float[] NyB,
                     float[] NzA, float[] NzB,
                     int[] Pol_f_Vert, int[] Vert_f_Pol)
         throws VisADException {

   int   i, k,  n;
   int   i1, i2, ix, iy, iz, ixb, iyb, izb;
   int   max_vert_per_pol, swap_flag;
   float x, y, z, a, minimum_area, len;

   int iv[] = new int[3];


   for ( i = 0; i < nvertex; i++ ) {
      NX[i] = 0;
      NY[i] = 0;
      NZ[i] = 0;
   }

   // WLH 12 Nov 2001
   // minimum_area = (float) ((1.e-4 > EPS_0) ? 1.e-4 : EPS_0);
   minimum_area = Float.MIN_VALUE;

   /* Calculate maximum number of vertices per polygon */
   k = 6;    n = 7*npolygons;
   while ( TRUE )
   {   for (i=k+7; i<n; i+=7)
           if (Vert_f_Pol[i] > Vert_f_Pol[k]) break;
       if (i >= n) break;    k = i;
   }
   max_vert_per_pol = Vert_f_Pol[k];

   /* Calculate the Normals vector components for each Polygon */
   /*$dir vector */
   for ( i=0; i<npolygons; i++) {  /* Vectorized */
      if (Vert_f_Pol[6+i*7]>0) {  /* check for valid polygon added by BEP 2-13-92 */
         NxA[i] = VX[Vert_f_Pol[1+i*7]] - VX[Vert_f_Pol[0+i*7]];
         NyA[i] = VY[Vert_f_Pol[1+i*7]] - VY[Vert_f_Pol[0+i*7]];
         NzA[i] = VZ[Vert_f_Pol[1+i*7]] - VZ[Vert_f_Pol[0+i*7]];
      }
   }

   swap_flag = 0;
   for ( k = 2; k < max_vert_per_pol; k++ )
   {

      if (swap_flag==0) {
         /*$dir no_recurrence */        /* Vectorized */
         for ( i=0; i<npolygons; i++ ) {
            if ( Vert_f_Pol[k+i*7] >= 0 ) {
               NxB[i]  = VX[Vert_f_Pol[k+i*7]] - VX[Vert_f_Pol[0+i*7]];
               NyB[i]  = VY[Vert_f_Pol[k+i*7]] - VY[Vert_f_Pol[0+i*7]];
               NzB[i]  = VZ[Vert_f_Pol[k+i*7]] - VZ[Vert_f_Pol[0+i*7]];
               Pnx[i] = NyA[i]*NzB[i] - NzA[i]*NyB[i];
               Pny[i] = NzA[i]*NxB[i] - NxA[i]*NzB[i];
               Pnz[i] = NxA[i]*NyB[i] - NyA[i]*NxB[i];
               NxA[i] = Pnx[i]*Pnx[i] + Pny[i]*Pny[i] + Pnz[i]*Pnz[i];
               if (NxA[i] > minimum_area) {
                  Pnx[i] /= NxA[i];
                  Pny[i] /= NxA[i];
                  Pnz[i] /= NxA[i];
               }
            }
         }
      }
      else {  /* swap_flag!=0 */
         /*$dir no_recurrence */        /* Vectorized */
         for ( i=0; i<npolygons; i++ ) {
            if ( Vert_f_Pol[k+i*7] >= 0 ) {
               NxA[i]  = VX[Vert_f_Pol[k+i*7]] - VX[Vert_f_Pol[0+i*7]];
               NyA[i]  = VY[Vert_f_Pol[k+i*7]] - VY[Vert_f_Pol[0+i*7]];
               NzA[i]  = VZ[Vert_f_Pol[k+i*7]] - VZ[Vert_f_Pol[0+i*7]];
               Pnx[i] = NyB[i]*NzA[i] - NzB[i]*NyA[i];
               Pny[i] = NzB[i]*NxA[i] - NxB[i]*NzA[i];
               Pnz[i] = NxB[i]*NyA[i] - NyB[i]*NxA[i];
               NxB[i] = Pnx[i]*Pnx[i] + Pny[i]*Pny[i] + Pnz[i]*Pnz[i];
               if (NxB[i] > minimum_area) {
                  Pnx[i] /= NxB[i];
                  Pny[i] /= NxB[i];
                  Pnz[i] /= NxB[i];
               }
            }
         }
      }

       /* This Loop <CAN'T> be Vectorized */
       for ( i=0; i<npolygons; i++ )
       {   if (Vert_f_Pol[k+i*7] >= 0)
           {   iv[0] = Vert_f_Pol[0+i*7];
               iv[1] = Vert_f_Pol[(k-1)+i*7];
               iv[2] = Vert_f_Pol[k+i*7];
                 x = Pnx[i];   y = Pny[i];   z = Pnz[i];

               // Update the origin vertex
                  NX[iv[0]] += x;   NY[iv[0]] += y;   NZ[iv[0]] += z;

               // Update the vertex that defines the first vector
                  NX[iv[1]] += x;   NY[iv[1]] += y;   NZ[iv[1]] += z;

               // Update the vertex that defines the second vector
                  NX[iv[2]] += x;   NY[iv[2]] += y;   NZ[iv[2]] += z;
           }
       }

       swap_flag = ( (swap_flag != 0) ? 0 : 1 );
    }

    /* Normalize the Normals */
    for ( i=0; i<nvertex; i++ )  /* Vectorized */
    {   len = (float) Math.sqrt(NX[i]*NX[i] + NY[i]*NY[i] + NZ[i]*NZ[i]);
        if (len > EPS_0) {
            NX[i] /= len;
            NY[i] /= len;
            NZ[i] /= len;
        }
    }

  }

  public static int poly_triangle_stripe( int[] vet_pol, int[] Tri_Stripe,
                            int nvertex, int npolygons, int[] Pol_f_Vert,
                            int[] Vert_f_Pol ) throws VisADException {
   int  i, j, k, m, ii, npol, cpol, idx, off, Nvt,
        vA, vB, ivA, ivB, iST, last_pol;
   boolean f_line_conection = false;

    last_pol = 0;
    npol = 0;
    iST = 0;
    ivB = 0;

    for (i=0; i<npolygons; i++) vet_pol[i] = 1;  /* Vectorized */

    while (TRUE)
    {
        /* find_unselected_pol(cpol); */
        for (cpol=last_pol; cpol<npolygons; cpol++) {
           if ( (vet_pol[cpol] != 0) ) break;
        }
        if (cpol == npolygons) {
            cpol = -1;
        }
        else {
            last_pol = cpol;
        }
        /* end  find_unselected_pol(cpol); */

        if (cpol < 0) break;
/*      update_polygon            */
        vet_pol[cpol] = 0;
/* end     update_polygon            */

/*      get_vertices_of_pol(cpol,Vt,Nvt); {    */
            Nvt = Vert_f_Pol[(j=cpol*7)+6];
            off = j;
/*      }                                      */
/* end      get_vertices_of_pol(cpol,Vt,Nvt); {    */


        for (ivA=0; ivA<Nvt; ivA++) {
            ivB = (((ivA+1)==Nvt) ? 0:(ivA+1));
/*          get_pol_vert(Vt[ivA],Vt[ivB],npol) { */
               npol = -1;
               if (Vert_f_Pol[ivA+off]>=0 && Vert_f_Pol[ivB+off]>=0) {
                  i=Vert_f_Pol[ivA+off]*9;
                  k=i+Pol_f_Vert [i+8];
                  j=Vert_f_Pol[ivB+off]*9;
                  m=j+Pol_f_Vert [j+8];
                  while (i>0 && j>0 && i<k && j <m ) {
                     if (Pol_f_Vert [i] == Pol_f_Vert [j] &&
                         (vet_pol[Pol_f_Vert[i]] != 0) ) {
                        npol=Pol_f_Vert [i];
                        break;
                     }
                     else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                          i++;
                     else
                          j++;
                  }
               }
/*          }                                   */
/* end          get_pol_vert(Vt[ivA],Vt[ivB],npol) { */
            if (npol >= 0) break;
        }
        /* insert polygon alone */
        if (npol < 0)
        { /*ptT = NTAB + STAB[Nvt-3];*/
            idx = STAB[Nvt-3];
            if (iST > 0)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx]+off];
            }
            else f_line_conection = true; /* WLH 3-9-95 added */
            for (ii=0; ii< ((Nvt < 6) ? Nvt:6); ii++) {
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx++]+off];
              }
            continue;
        }

        if (( (ivB != 0) && ivA==(ivB-1)) || ( !(ivB != 0) && ivA==Nvt-1)) {
         /* ptT = ITAB + STAB[Nvt-3] + (ivB+1)*Nvt; */
            idx = STAB[Nvt-3] + (ivB+1)*Nvt;

            if (f_line_conection)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
                Tri_Stripe[iST++] = Vert_f_Pol[ITAB[idx-1]+off];
                f_line_conection = false;
            }
            for (ii=0; ii<((Nvt < 6) ? Nvt:6); ii++) {
                Tri_Stripe[iST++] = Vert_f_Pol[ITAB[--idx]+off];
            }

        }
        else {
         /* ptT = NTAB + STAB[Nvt-3] + (ivB+1)*Nvt; */
            idx = STAB[Nvt-3] + (ivB+1)*Nvt;

            if (f_line_conection)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx-1]+off];
                f_line_conection = false;
            }
            for (ii=0; ii<((Nvt < 6) ? Nvt:6); ii++) {
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[--idx]+off];
            }

        }

        vB = Tri_Stripe[iST-1];
        vA = Tri_Stripe[iST-2];
        cpol = npol;

        while (TRUE)
        {
/*          get_vertices_of_pol(cpol,Vt,Nvt)  {   */
                Nvt = Vert_f_Pol [(j=cpol*7)+6];
                off = j;
/*          }                                     */


/*          update_polygon(cpol)                  */
            vet_pol[cpol] = 0;
            for (ivA=0; ivA<Nvt && Vert_f_Pol[ivA+off]!=vA; ivA++);
            for (ivB=0; ivB<Nvt && Vert_f_Pol[ivB+off]!=vB; ivB++);
                 if (( (ivB != 0) && ivA==(ivB-1)) || (!(ivB != 0) && ivA==Nvt-1)) {
                /* ptT = NTAB + STAB[Nvt-3] + ivA*Nvt + 2; */
                    idx = STAB[Nvt-3] + ivA*Nvt + 2;

                    for (ii=2; ii<((Nvt < 6) ? Nvt:6); ii++)
                        Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx++]+off];
                 }
                 else {
                /*  ptT = ITAB + STAB[Nvt-3] + ivA*Nvt + 2; */
                    idx = STAB[Nvt-3] + ivA*Nvt + 2;

                    for (ii=2; ii<((Nvt < 6) ? Nvt:6); ii++)
                        Tri_Stripe[iST++] = Vert_f_Pol[ITAB[idx++]+off];
                 }

            vB = Tri_Stripe[iST-1];
            vA = Tri_Stripe[iST-2];

/*          get_pol_vert(vA,vB,cpol) {     */
               cpol = -1;
               if (vA>=0 && vB>=0) {
                 i=vA*9;
                 k=i+Pol_f_Vert [i+8];
                 j=vB*9;
                 m=j+Pol_f_Vert [j+8];
                 while (i>0 && j>0 && i<k && j<m) {
                    if (Pol_f_Vert [i] == Pol_f_Vert [j] && (vet_pol[Pol_f_Vert[i]] != 0) ) {
                      cpol=Pol_f_Vert[i];
                      break;
                    }
                    else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                      i++;
                    else
                      j++;
                 }
               }
/*         }                               */

            if (cpol < 0) {

                vA = Tri_Stripe[iST-3];
/*          get_pol_vert(vA,vB,cpol) {   */
               cpol = -1;
               if (vA>=0 && vB>=0) {
                 i=vA*9;
                 k=i+Pol_f_Vert [i+8];
                 j=vB*9;
                 m=j+Pol_f_Vert [j+8];
                 while (i>0 && j>0 && i<k && j<m) {
                    if (Pol_f_Vert [i] == Pol_f_Vert [j] &&
                        (vet_pol[Pol_f_Vert[i]] != 0) ) {
                      cpol=Pol_f_Vert[i];
                      break;
                    }
                    else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                      i++;
                    else
                      j++;
                 }
               }

/*          }                            */
                if (cpol < 0) {
                    f_line_conection  = true;
                    break;
                }
                else {
                    // WLH 5 May 2004 - fix bug vintage 1990 or 91
                    if (iST > 0) {
                      Tri_Stripe[iST] = Tri_Stripe[iST-1];
                      iST++;
                    }
                    Tri_Stripe[iST++] = vA;
                    i = vA;
                    vA = vB;
                    vB = i;
                }
            }
        }
    }

    return iST;
  }

  public static float[] makeNormals(float[] coordinates, int LengthX,
                                    int LengthY) {
    int Length = LengthX * LengthY;

    float[] normals = new float[3 * Length];
    int k = 0;
    int ki, kj;
    int LengthX3 = 3 * LengthX;
    for (int i=0; i<LengthY; i++) {
      for (int j=0; j<LengthX; j++) {
        float c0 = coordinates[k];
        float c1 = coordinates[k+1];
        float c2 = coordinates[k+2];
        float n0 = 0.0f;
        float n1 = 0.0f;
        float n2 = 0.0f;
        float n, m, m0, m1, m2, q0, q1, q2;
        for (int ip = -1; ip<=1; ip += 2) {
          for (int jp = -1; jp<=1; jp += 2) {
            int ii = i + ip;
            int jj = j + jp;
            if (0 <= ii && ii < LengthY && 0 <= jj && jj < LengthX) {
              ki = k + ip * LengthX3;
              kj = k + jp * 3;
              m0 = (coordinates[kj+2] - c2) * (coordinates[ki+1] - c1) -
                   (coordinates[kj+1] - c1) * (coordinates[ki+2] - c2);
              m1 = (coordinates[kj] - c0) * (coordinates[ki+2] - c2) -
                   (coordinates[kj+2] - c2) * (coordinates[ki] - c0);
              m2 = (coordinates[kj+1] - c1) * (coordinates[ki] - c0) -
                   (coordinates[kj] - c0) * (coordinates[ki+1] - c1);
              m = (float) Math.sqrt(m0 * m0 + m1 * m1 + m2 * m2);
              if (ip == jp) {
                q0 = m0 / m;
                q1 = m1 / m;
                q2 = m2 / m;
              }
              else {
                q0 = -m0 / m;
                q1 = -m1 / m;
                q2 = -m2 / m;
              }
              if (q0 == q0) {
                n0 += q0;
                n1 += q1;
                n2 += q2;
              }
              else {
/*
System.out.println("m = " + m + " " + m0 + " " + m1 + " " + m2 + " " +
                   n0 + " " + n1 + " " + n2 + " ip, jp = " + ip + " " + jp);
System.out.println("k = " + k + " " + ki + " " + kj);
System.out.println("c = " + c0 + " " + c1 + " " + c2);
System.out.println("coordinates[ki] = " + coordinates[ki] + " " +
                   coordinates[ki+1] + " " + coordinates[ki+2]); // == c ??
System.out.println("coordinates[kj] = " + coordinates[kj] + " " +
                   coordinates[kj+1] + " " + coordinates[kj+2]);
System.out.println("LengthX = " + LengthX + " " + LengthY + " " +
                   LengthX3);
*/
              }
            }
          }
        }
        n = (float) Math.sqrt(n0 * n0 + n1 * n1 + n2 * n2);
        normals[k] = n0 / n;
        normals[k+1] = n1 / n;
        normals[k+2] = n2 / n;
        if (normals[k] != normals[k]) {
          normals[k] = 0.0f;
          normals[k+1] = 0.0f;
          normals[k+2] = -1.0f;
        }
/*
System.out.println("makeNormals " + k + " " + normals[k] + " " + normals[k+1] + " " +
                   normals[k+2]);
*/
        k += 3;
      } // end for (int j=0; j<LengthX; j++)
    } // end for (int i=0; i<LengthY; i++)
    return normals;
  }


  /** create a 2-D GeometryArray from this Set and color_values */
  public VisADGeometryArray make2DGeometry(byte[][] color_values,
         boolean indexed) throws VisADException {
    if (DomainDimension != 3) {
      throw new SetException("Gridded3DSet.make2DGeometry: " +
                              "DomainDimension must be 3");
    }
    if (ManifoldDimension != 2) {
      throw new SetException("Gridded3DSet.make2DGeometry: " +
                              "ManifoldDimension must be 2");
    }
    if (LengthX < 2 || LengthY < 2) {
/* WLH 26 June 98
      throw new SetException("Gridded3DSet.make2DGeometry: " +
                              "LengthX and LengthY must be at least 2");
*/
      VisADPointArray array = new VisADPointArray();
      setGeometryArray(array, 4, color_values);
      return array;
    }

    if (indexed) {
      VisADIndexedTriangleStripArray array =
        new VisADIndexedTriangleStripArray();

      // set up indices into 2-D grid
      array.indexCount = (LengthY - 1) * (2 * LengthX);
      int[] indices = new int[array.indexCount];
      array.stripVertexCounts = new int[LengthY - 1];
      int k = 0;
      for (int i=0; i<LengthY-1; i++) {
        int m = i * LengthX;
        array.stripVertexCounts[i] = 2 * LengthX;
        for (int j=0; j<LengthX; j++) {
          indices[k++] = m;
          indices[k++] = m + LengthX;
          m++;
        }
      }
      array.indices = indices;
      // take the garbage out
      indices = null;

      // set coordinates and colors
      setGeometryArray(array, 4, color_values);

      // calculate normals
      float[] coordinates = array.coordinates;
      float[] normals = makeNormals(coordinates, LengthX, LengthY);
      array.normals = normals;
      return array;
    }
    else { // if (!indexed)
      VisADTriangleStripArray array = new VisADTriangleStripArray();
      float[][] samples = getSamples(false);

      array.stripVertexCounts = new int[LengthY - 1];
      for (int i=0; i<LengthY-1; i++) {
        array.stripVertexCounts[i] = 2 * LengthX;
      }
      int len = (LengthY - 1) * (2 * LengthX);
      array.vertexCount = len;

      // calculate normals
      float[] normals = new float[3 * Length];
      int k = 0;
      int k3 = 0;
      int ki, kj;
      for (int i=0; i<LengthY; i++) {
        for (int j=0; j<LengthX; j++) {
          float c0 = samples[0][k3];
          float c1 = samples[1][k3];
          float c2 = samples[2][k3];
          float n0 = 0.0f;
          float n1 = 0.0f;
          float n2 = 0.0f;
          float n, m, m0, m1, m2;
          boolean any = false;
          for (int ip = -1; ip<=1; ip += 2) {
            for (int jp = -1; jp<=1; jp += 2) {
              int ii = i + ip;
              int jj = j + jp;
              ki = k3 + ip * LengthX;
              kj = k3 + jp;
              if (0 <= ii && ii < LengthY && 0 <= jj && jj < LengthX &&
                  samples[0][ki] == samples[0][ki] &&
                  samples[1][ki] == samples[1][ki] &&
                  samples[2][ki] == samples[2][ki] &&
                  samples[0][kj] == samples[0][kj] &&
                  samples[1][kj] == samples[1][kj] &&
                  samples[2][kj] == samples[2][kj]) {
                any = true;
                m0 = (samples[2][kj] - c2) * (samples[1][ki] - c1) -
                     (samples[1][kj] - c1) * (samples[2][ki] - c2);
                m1 = (samples[0][kj] - c0) * (samples[2][ki] - c2) -
                     (samples[2][kj] - c2) * (samples[0][ki] - c0);
                m2 = (samples[1][kj] - c1) * (samples[0][ki] - c0) -
                     (samples[0][kj] - c0) * (samples[1][ki] - c1);
                m = (float) Math.sqrt(m0 * m0 + m1 * m1 + m2 * m2);
                if (ip == jp) {
                  n0 += m0 / m;
                  n1 += m1 / m;
                  n2 += m2 / m;
                }
                else {
                  n0 -= m0 / m;
                  n1 -= m1 / m;
                  n2 -= m2 / m;
                }
              }
            }
          }
          n = (float) Math.sqrt(n0 * n0 + n1 * n1 + n2 * n2);
          if (any) {
            normals[k] = n0 / n;
            normals[k+1] = n1 / n;
            normals[k+2] = n2 / n;
          }
          else {
            normals[k] = 0.0f;
            normals[k+1] = 0.0f;
            normals[k+2] = 1.0f;
          }
          k += 3;
          k3++;
        } // end for (int j=0; j<LengthX; j++)
      } // end for (int i=0; i<LengthY; i++)

      array.normals = new float[3 * len];

      // shuffle normals into array.normals
      k = 0;
      int LengthX3 = 3 * LengthX;
      for (int i=0; i<LengthY-1; i++) {
        int m = i * LengthX3;
        for (int j=0; j<LengthX; j++) {
          array.normals[k] = normals[m];
          array.normals[k+1] = normals[m+1];
          array.normals[k+2] = normals[m+2];
          array.normals[k+3] = normals[m+LengthX3];
          array.normals[k+4] = normals[m+LengthX3+1];
          array.normals[k+5] = normals[m+LengthX3+2];
          k += 6;
          m += 3;
        }
      }
      normals = null;
/*
int nmiss = 0;
int nsmall = 0;
*/
      array.coordinates = new float[3 * len];
      // shuffle samples into array.coordinates
      k = 0;
      for (int i=0; i<LengthY-1; i++) {
        int m = i * LengthX;
        for (int j=0; j<LengthX; j++) {
          array.coordinates[k] = samples[0][m];
          array.coordinates[k+1] = samples[1][m];
          array.coordinates[k+2] = samples[2][m];
          array.coordinates[k+3] = samples[0][m+LengthX];
          array.coordinates[k+4] = samples[1][m+LengthX];
          array.coordinates[k+5] = samples[2][m+LengthX];
/*
if (samples[0][m] != samples[0][m] ||
    samples[1][m] != samples[1][m] ||
    samples[2][m] != samples[2][m]) nmiss++;
double size = Math.sqrt(samples[0][m] * samples[0][m] +
                        samples[1][m] * samples[1][m] +
                        samples[2][m] * samples[2][m]);
if (size < 0.2) nsmall++;
*/
          k += 6;
          m++;
        }
      }

// System.out.println("make2DGeometry nmiss = " + nmiss + " nsmall = " + nsmall);

      if (color_values != null) {
        int color_length = color_values.length;
        array.colors = new byte[color_length * len];
        // shuffle samples into array.coordinates
        k = 0;
        if (color_length == 4) {
          for (int i=0; i<LengthY-1; i++) {
            int m = i * LengthX;
            for (int j=0; j<LengthX; j++) {
              array.colors[k] = color_values[0][m];
              array.colors[k+1] = color_values[1][m];
              array.colors[k+2] = color_values[2][m];
              array.colors[k+3] = color_values[3][m];
              k += color_length;
              array.colors[k] = color_values[0][m+LengthX];
              array.colors[k+1] = color_values[1][m+LengthX];
              array.colors[k+2] = color_values[2][m+LengthX];
              array.colors[k+3] = color_values[3][m+LengthX];
              k += color_length;
              m++;
            }
          }
        }
        else { // if (color_length == 3)
          for (int i=0; i<LengthY-1; i++) {
            int m = i * LengthX;
            for (int j=0; j<LengthX; j++) {
              array.colors[k] = color_values[0][m];
              array.colors[k+1] = color_values[1][m];
              array.colors[k+2] = color_values[2][m];
              k += color_length;
              array.colors[k] = color_values[0][m+LengthX];
              array.colors[k+1] = color_values[1][m+LengthX];
              array.colors[k+2] = color_values[2][m+LengthX];
              k += color_length;
              m++;
            }
          }
        }
      }
      return array;
    } // end if (!indexed)
  }

  public Object cloneButType(MathType type) throws VisADException {
    if (ManifoldDimension == 3) {
      return new Gridded3DSet(type, Samples, LengthX, LengthY, LengthZ,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else if (ManifoldDimension == 2) {
      return new Gridded3DSet(type, Samples, LengthX, LengthY,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    else {
      return new Gridded3DSet(type, Samples, LengthX,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
  }

  /* run 'java visad.Gridded3DSet < formatted_input_stream'
     to test the Gridded3DSet class */
  public static void main(String[] argv) throws VisADException {

    // Define input stream
    InputStreamReader inStr = new InputStreamReader(System.in);

    // Define temporary integer array
    int[] ints = new int[80];
    try {
      ints[0] = inStr.read();
    }
    catch(Exception e) {
      throw new SetException("Gridded3DSet: "+e);
    }
    int l = 0;
    while (ints[l] != 10) {
      try {
        ints[++l] = inStr.read();
      }
      catch (Exception e) {
        throw new SetException("Gridded3DSet: "+e);
      }
    }
    // convert array of integers to array of characters
    char[] chars = new char[l];
    for (int i=0; i<l; i++) {
      chars[i] = (char) ints[i];
    }
    int num_coords = Integer.parseInt(new String(chars));

    // num_coords should be a nice round number
    if (num_coords % 9 != 0) {
      throw new SetException("Gridded3DSet: input coordinates"
         +" must be divisible by 9 for main function testing routines.");
    }

    // Define size of Samples array
    float[][] samp = new float[3][num_coords];
    System.out.println("num_dimensions = 3, num_coords = "+num_coords+"\n");

    // Skip blank line
    try {
      ints[0] = inStr.read();
    }
    catch (Exception e) {
      throw new SetException("Gridded3DSet: "+e);
    }

    for (int c=0; c<num_coords; c++) {
      for (int d=0; d<3; d++) {
        l = 0;
        try {
          ints[0] = inStr.read();
        }
        catch (Exception e) {
          throw new SetException("Gridded3DSet: "+e);
        }
        while ( (ints[l] != 32) && (ints[l] != 10) ) {
          try {
            ints[++l] = inStr.read();
          }
          catch (Exception e) {
            throw new SetException("Gridded3DSet: "+e);
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
      throw new SetException("Gridded3DSet: "+e);
    }

    // Set up instance of Gridded3DSet
    RealType vis_xcoord = RealType.getRealType("xcoord");
    RealType vis_ycoord = RealType.getRealType("ycoord");
    RealType vis_zcoord = RealType.getRealType("zcoord");
    RealType[] vis_array = {vis_xcoord, vis_ycoord, vis_zcoord};
    RealTupleType vis_tuple = new RealTupleType(vis_array);
    Gridded3DSet gSet3D = new Gridded3DSet(vis_tuple, samp,
                                           3, 3, num_coords/9);

    System.out.println("Lengths = 3 3 " + num_coords/9 + " wedge = ");
    int[] wedge = gSet3D.getWedge();
    for (int i=0; i<wedge.length; i++) System.out.println(" " + wedge[i]);

    // print out Samples information
    System.out.println("Samples ("+gSet3D.LengthX+" x "+gSet3D.LengthY
                                                 +" x "+gSet3D.LengthZ+"):");
    for (int i=0; i<gSet3D.LengthX*gSet3D.LengthY*gSet3D.LengthZ; i++) {
      System.out.println("#"+i+":\t"+gSet3D.Samples[0][i]
                               +", "+gSet3D.Samples[1][i]
                               +", "+gSet3D.Samples[2][i]);
    }

    // Test gridToValue function
    System.out.println("\ngridToValue test:");
    int myLengthX = gSet3D.LengthX+1;
    int myLengthY = gSet3D.LengthY+1;
    int myLengthZ = gSet3D.LengthZ+1;
    float[][] myGrid = new float[3][myLengthX*myLengthY*myLengthZ];
    for (int k=0; k<myLengthZ; k++) {
      for (int j=0; j<myLengthY; j++) {
        for (int i=0; i<myLengthX; i++) {
          int index = k*myLengthY*myLengthX+j*myLengthX+i;
          myGrid[0][index] = i-0.5f;
          myGrid[1][index] = j-0.5f;
          myGrid[2][index] = k-0.5f;
          if (myGrid[0][index] < 0) myGrid[0][index] += 0.1;
          if (myGrid[1][index] < 0) myGrid[1][index] += 0.1;
          if (myGrid[2][index] < 0) myGrid[2][index] += 0.1;
          if (myGrid[0][index] > gSet3D.LengthX-1) myGrid[0][index] -= 0.1;
          if (myGrid[1][index] > gSet3D.LengthY-1) myGrid[1][index] -= 0.1;
          if (myGrid[2][index] > gSet3D.LengthZ-1) myGrid[2][index] -= 0.1;
        }
      }
    }
    float[][] myValue = gSet3D.gridToValue(myGrid);
    for (int i=0; i<myLengthX*myLengthY*myLengthZ; i++) {
      System.out.println("("+((float) Math.round(1000000
                                      *myGrid[0][i]) /1000000)+", "
                            +((float) Math.round(1000000
                                      *myGrid[1][i]) /1000000)+", "
                            +((float) Math.round(1000000
                                      *myGrid[2][i]) /1000000)+")  \t-->  "
                            +((float) Math.round(1000000
                                      *myValue[0][i]) /1000000)+", "
                            +((float) Math.round(1000000
                                      *myValue[1][i]) /1000000)+", "
                            +((float) Math.round(1000000
                                      *myValue[2][i]) /1000000));
    }

    // Test valueToGrid function
    System.out.println("\nvalueToGrid test:");
    float[][] gridTwo = gSet3D.valueToGrid(myValue);
    for (int i=0; i<gridTwo[0].length; i++) {
      System.out.print(((float) Math.round(1000000
                                *myValue[0][i]) /1000000)+", "
                      +((float) Math.round(1000000
                                *myValue[1][i]) /1000000)+", "
                      +((float) Math.round(1000000
                                *myValue[2][i]) /1000000)+"\t-->  (");
      if (Float.isNaN(gridTwo[0][i])) {
        System.out.print("NaN, ");
      }
      else {
        System.out.print(((float) Math.round(1000000
                                  *gridTwo[0][i]) /1000000)+", ");
      }
      if (Float.isNaN(gridTwo[1][i])) {
        System.out.print("NaN, ");
      }
      else {
        System.out.print(((float) Math.round(1000000
                                  *gridTwo[1][i]) /1000000)+", ");
      }
      if (Float.isNaN(gridTwo[2][i])) {
        System.out.println("NaN)");
      }
      else {
        System.out.println(((float) Math.round(1000000
                                    *gridTwo[2][i]) /1000000)+")");
      }
    }
    System.out.println();
  }

/* Here's the output with sample file Gridded3D.txt:

iris 28% java visad.Gridded3DSet < Gridded3D.txt
num_dimensions = 3, num_coords = 27

Lengths = 3 3 3 wedge =
 0
 1
 2
 5
 4
 3
. . .

Samples (3 x 3 x 3):
#0:     18.629837, 8.529864, 10.997844
#1:     42.923097, 10.123978, 11.198275
. . .
#25:    32.343298, 39.600872, 36.238975
#26:    49.919754, 40.119875, 36.018752

gridToValue test:
(-0.4, -0.4, -0.4)      -->  10.819755, -0.172592, 5.179111
(0.5, -0.4, -0.4)       -->  32.683689, 1.262111, 5.359499
. . .
(1.5, 2.4, 2.4)         -->  43.844996, 48.904708, 40.008508
(2.4, 2.4, 2.4)         -->  59.663807, 49.37181, 39.810308

valueToGrid test:
10.819755, -0.172592, 5.179111  -->  (-0.4, -0.4, -0.4)
32.683689, 1.262111, 5.359499   -->  (0.5, -0.4, -0.4)
. . .
43.844996, 48.904708, 40.008508 -->  (1.5, 2.4, 2.4)
59.663807, 49.37181, 39.810308  -->  (2.4, 2.4, 2.4)

iris 29%

*/

}

