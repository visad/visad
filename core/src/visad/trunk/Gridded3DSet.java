
//
// Gridded3DSet.java
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

import java.io.*;

/**
   Gridded3DSet represents a finite set of samples of R^3.<P>
*/
public class Gridded3DSet extends GriddedSet {

  int LengthX, LengthY, LengthZ;
  double LowX, HiX, LowY, HiY, LowZ, HiZ;

  public Gridded3DSet(MathType type, double[][] samples, int lengthX,
                      int lengthY, int lengthZ) throws VisADException {
    this(type, samples, lengthX, lengthY, lengthZ, null, null, null);
  }

  public Gridded3DSet(MathType type, double[][] samples,
                      int lengthX, int lengthY, int lengthZ,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    super(type, samples, make_lengths(lengthX, lengthY, lengthZ),
          coord_sys, units, errors);
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
      // Samples consistency test
      double[] t000 = new double[3];
      double[] t100 = new double[3];
      double[] t010 = new double[3];
      double[] t001 = new double[3];
      double[] t110 = new double[3];
      double[] t101 = new double[3];
      double[] t011 = new double[3];
      double[] t111 = new double[3];
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
      for (int k=0; k<LengthZ-1; k++) {
        for (int j=0; j<LengthY-1; j++) {
          for (int i=0; i<LengthX-1; i++) {
            double[] v000 = new double[3];
            double[] v100 = new double[3];
            double[] v010 = new double[3];
            double[] v001 = new double[3];
            double[] v110 = new double[3];
            double[] v101 = new double[3];
            double[] v011 = new double[3];
            double[] v111 = new double[3];
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
                    - (v110[1]-v111[0])*(v010[0]-v110[0]) )
                     *(v100[2]-v110[2])  ) > 0 != Pos)
             || ((  ( (v111[1]-v011[1])*(v110[2]-v111[2])    // test 6
                    - (v111[2]-v011[2])*(v110[1]-v111[1]) )
                     *(v101[0]-v111[0])  )
               + (  ( (v111[2]-v011[2])*(v110[0]-v111[0])
                    - (v111[0]-v011[0])*(v110[2]-v111[2]) )
                     *(v101[1]-v111[1])  )
               + (  ( (v111[0]-v011[0])*(v110[1]-v111[1])
                    - (v111[1]-v011[0])*(v110[0]-v111[0]) )
                     *(v101[2]-v111[2])  ) > 0 != Pos)
             || ((  ( (v011[1]-v010[1])*(v111[2]-v011[2])    // test 7
                    - (v011[2]-v010[2])*(v111[1]-v011[1]) )
                     *(v001[0]-v011[0])  )
               + (  ( (v011[2]-v010[2])*(v111[0]-v011[0])
                    - (v011[0]-v010[0])*(v111[2]-v011[2]) )
                     *(v001[1]-v011[1])  )
               + (  ( (v011[0]-v010[0])*(v111[1]-v011[1])
                    - (v011[1]-v010[0])*(v111[0]-v011[0]) )
                     *(v001[2]-v011[2])  ) > 0 != Pos)
             || ((  ( (v010[1]-v110[1])*(v011[2]-v010[2])    // test 8
                    - (v010[2]-v110[2])*(v011[1]-v010[1]) )
                     *(v000[0]-v010[0])  )
               + (  ( (v010[2]-v110[2])*(v011[0]-v010[0])
                    - (v010[0]-v110[0])*(v011[2]-v010[2]) )
                     *(v000[1]-v010[1])  )
               + (  ( (v010[0]-v110[0])*(v011[1]-v010[1])
                    - (v010[1]-v110[0])*(v011[0]-v010[0]) )
                     *(v000[2]-v010[2])  ) > 0 != Pos)) {
              throw new SetException("Gridded3DSet: samples do not form "
                                     +"a valid grid ("+i+","+j+","+k+")");
            }
          }
        }
      }
    }
  }

  static int[] make_lengths(int lengthX, int lengthY, int lengthZ) {
    int[] lens = new int[3];
    lens[0] = lengthX;
    lens[1] = lengthY;
    lens[2] = lengthZ;
    return lens;
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public double[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear3DSet.indexToValue
      int indexX, indexY, indexZ;
      int k;
      double[][] grid = new double[3][length];
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
        grid[0][i] = (double) indexX;
        grid[1][i] = (double) indexY;
        grid[2][i] = (double) indexZ;
      }
      return gridToValue(grid);
    }
    else {
      double[][] values = new double[3][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
          values[1][i] = Samples[1][index[i]];
          values[2][i] = Samples[2][index[i]];
        }
        else {
          values[0][i] = Double.NaN;
          values[1][i] = Double.NaN;
          values[2][i] = Double.NaN;
        }
      }
      return values;
    }
  }

  /** convert an array of values in R^DomainDimension to an array of 1-D indices */
  public int[] valueToIndex(double[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded3DSet.valueToIndex: bad dimension");
    }
    int length = value[0].length;
    int[] index = new int[length];

    double[][] grid = valueToGrid(value);
    double[] grid0 = grid[0];
    double[] grid1 = grid[1];
    double[] grid2 = grid[2];
    double g0, g1, g2;
    for (int i=0; i<length; i++) {
      g0 = grid0[i];
      g1 = grid1[i];
      g2 = grid2[i];
      index[i] = (Double.isNaN(g0)
               || Double.isNaN(g1)
               || Double.isNaN(g2)) ? -1 :
                 ((int) (g0 + 0.5)) + LengthX*( ((int) (g1 + 0.5)) +
                  LengthY*((int) (g2 + 0.5)));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public double[][] gridToValue(double[][] grid) throws VisADException {
    if (grid.length < DomainDimension) {
      throw new SetException("Gridded3DSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2) {
      throw new SetException("Gridded3DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    // avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(grid[0].length, grid[1].length);
    length = Math.min(length, grid[2].length);
    double[][] value = new double[3][length];
    for (int i=0; i<length; i++) {
      // let gx, gy, and gz be the current grid values
      double gx = grid[0][i];
      double gy = grid[1][i];
      double gz = grid[2][i];
      if ( (gx < -0.5)        || (gy < -0.5)        || (gz < -0.5) ||
           (gx > LengthX-0.5) || (gy > LengthY-0.5) || (gz > LengthZ-0.5) ) {
        value[0][i] = value[1][i] = value[2][i] = Double.NaN;
        continue;
      }
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
      double s, t, u;
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
      double[] A = new double[3];
      double[] B = new double[3];
      double[] C = new double[3];
      double[] D = new double[3];
      double[] E = new double[3];
      double[] F = new double[3];
      double[] G = new double[3];
      double[] H = new double[3];
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
    return value;
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public double[][] valueToGrid(double[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded3DSet.valueToGrid: bad dimension");
    }
    if (Lengths[0] < 2 || Lengths[1] < 2 || Lengths[2] < 2) {
      throw new SetException("Gridded3DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    // Avoid any ArrayOutOfBounds exceptions by taking the shortest length
    int length = Math.min(value[0].length, value[1].length);
    length = Math.min(length, value[2].length);
    double[][] grid = new double[3][length];
    // (gx, gy, gz) is the current grid box guess
    int gx = (LengthX-1)/2;
    int gy = (LengthY-1)/2;
    int gz = (LengthZ-1)/2;
    for (int i=0; i<length; i++) {
      // a flag indicating whether point is off the grid
      boolean offgrid = false;
      // the first guess should be the last box unless there was no solution
      if ( (i != 0) && (Double.isNaN(grid[0][i-1])) ) {
        gx = (LengthX-1)/2;
        gy = (LengthY-1)/2;
        gz = (LengthZ-1)/2;
      } 
      int tetnum = 5;  // Tetrahedron number in which to start search
      // if the iteration loop fails, the result should be NaN
      grid[0][i] = grid[1][i] = grid[2][i] = Double.NaN;
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
        double[] A = new double[3];
        double[] B = new double[3];
        double[] C = new double[3];
        double[] D = new double[3];
        double[] E = new double[3];
        double[] F = new double[3];
        double[] G = new double[3];
        double[] H = new double[3];
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
        double tval1, tval2, tval3, tval4;
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
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

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
            // solve point
            double[] M = new double[3];
            double[] N = new double[3];
            double[] O = new double[3];
            double[] P = new double[3];
            double[] X = new double[3];
            double[] Y = new double[3];
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
            double s, t, u;
            // these if statements handle skewed grids
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
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

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
            // solve point
            double[] M = new double[3];
            double[] N = new double[3];
            double[] O = new double[3];
            double[] P = new double[3];
            double[] X = new double[3];
            double[] Y = new double[3];
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
            double s, t, u;
            // these if statements handle skewed grids
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
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

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
            // solve point
            double[] M = new double[3];
            double[] N = new double[3];
            double[] O = new double[3];
            double[] P = new double[3];
            double[] X = new double[3];
            double[] Y = new double[3];
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
            double s, t, u;
            // these if statements handle skewed grids
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
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);

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
            // solve point
            double[] M = new double[3];
            double[] N = new double[3];
            double[] O = new double[3];
            double[] P = new double[3];
            double[] X = new double[3];
            double[] Y = new double[3];
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
            double s, t, u;
            // these if statements handle skewed grids
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
          test1 = (tval1 == 0) || ((tval1 > 0) == (!evencube)^Pos);
          test2 = (tval2 == 0) || ((tval2 > 0) == (!evencube)^Pos);
          test3 = (tval3 == 0) || ((tval3 > 0) == (!evencube)^Pos);
          test4 = (tval4 == 0) || ((tval4 > 0) == (!evencube)^Pos);

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
            double[] Q = new double[3];
            for (int j=0; j<3; j++) {
              Q[j] = (H[j] + F[j] + A[j] - C[j])/2;
            }
            double[] M = new double[3];
            double[] N = new double[3];
            double[] O = new double[3];
            double[] P = new double[3];
            double[] X = new double[3];
            double[] Y = new double[3];
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
            double s, t, u;
            // these if statements handle skewed grids
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
      if ( (grid[0][i] < -0.5) || (grid[0][i] > LengthX-0.5)
        || (grid[1][i] < -0.5) || (grid[1][i] > LengthY-0.5) 
        || (grid[2][i] < -0.5) || (grid[2][i] > LengthZ-0.5) ) {
        grid[0][i] = grid[1][i] = grid[2][i] = Double.NaN;
      }
    }
    return grid;
  }

  public Object clone() {
    try {
      return new Gridded3DSet(Type, Samples, LengthX, LengthY, LengthZ,
                              DomainCoordinateSystem, SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Gridded3DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded3DSet(type, Samples, LengthX, LengthY, LengthZ,
                            DomainCoordinateSystem, SetUnits, SetErrors);
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
    double[][] samp = new double[3][num_coords];
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
        samp[d][c] = (Double.valueOf(new String(chars))).doubleValue();
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
    RealType vis_xcoord = new RealType("xcoord", null, null);
    RealType vis_ycoord = new RealType("ycoord", null, null);
    RealType vis_zcoord = new RealType("zcoord", null, null);
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
    double[][] myGrid = new double[3][myLengthX*myLengthY*myLengthZ];
    for (int k=0; k<myLengthZ; k++) {
      for (int j=0; j<myLengthY; j++) {
        for (int i=0; i<myLengthX; i++) {
          int index = k*myLengthY*myLengthX+j*myLengthX+i;
          myGrid[0][index] = i-0.5;
          myGrid[1][index] = j-0.5;
          myGrid[2][index] = k-0.5;
          if (myGrid[0][index] < 0) myGrid[0][index] += 0.1;
          if (myGrid[1][index] < 0) myGrid[1][index] += 0.1;
          if (myGrid[2][index] < 0) myGrid[2][index] += 0.1;
          if (myGrid[0][index] > gSet3D.LengthX-1) myGrid[0][index] -= 0.1;
          if (myGrid[1][index] > gSet3D.LengthY-1) myGrid[1][index] -= 0.1;
          if (myGrid[2][index] > gSet3D.LengthZ-1) myGrid[2][index] -= 0.1;
        }
      }
    }
    double[][] myValue = gSet3D.gridToValue(myGrid);
    for (int i=0; i<myLengthX*myLengthY*myLengthZ; i++) {
      System.out.println("("+((double) Math.round(1000000
                                      *myGrid[0][i]) /1000000)+", "
                            +((double) Math.round(1000000
                                      *myGrid[1][i]) /1000000)+", "
                            +((double) Math.round(1000000
                                      *myGrid[2][i]) /1000000)+")  \t-->  "
                            +((double) Math.round(1000000
                                      *myValue[0][i]) /1000000)+", "
                            +((double) Math.round(1000000
                                      *myValue[1][i]) /1000000)+", "
                            +((double) Math.round(1000000
                                      *myValue[2][i]) /1000000));
    }

    // Test valueToGrid function
    System.out.println("\nvalueToGrid test:");
    double[][] gridTwo = gSet3D.valueToGrid(myValue);
    for (int i=0; i<gridTwo[0].length; i++) {
      System.out.print(((double) Math.round(1000000
                                *myValue[0][i]) /1000000)+", "
                      +((double) Math.round(1000000
                                *myValue[1][i]) /1000000)+", "
                      +((double) Math.round(1000000
                                *myValue[2][i]) /1000000)+"\t-->  (");
      if (Double.isNaN(gridTwo[0][i])) {
        System.out.print("NaN, ");
      }
      else {
        System.out.print(((double) Math.round(1000000
                                  *gridTwo[0][i]) /1000000)+", ");
      }
      if (Double.isNaN(gridTwo[1][i])) {
        System.out.print("NaN, ");
      }
      else {
        System.out.print(((double) Math.round(1000000
                                  *gridTwo[1][i]) /1000000)+", ");
      }
      if (Double.isNaN(gridTwo[2][i])) {
        System.out.println("NaN)");
      }
      else {
        System.out.println(((double) Math.round(1000000
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

