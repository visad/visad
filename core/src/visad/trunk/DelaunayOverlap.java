//
// DelaunayOverlap.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

// AWT is used in main method, for testing
import java.awt.*;
import java.awt.event.*;

/**
   DelaunayOverlap quickly constructs a triangulation of a series
   of partially overlapping two-dimensional gridded sets.<P>

   Grids should be aligned so that they overlap in a roughly
   vertical column (i.e., as samples[*][1] increases or decreases,
   the grid number increases or decreases respectively).
   DelaunayOverlap does not handle grids which overlap across a
   horizontal row.<P>
*/
public class DelaunayOverlap extends Delaunay {

  /* This method could result in overlapping triangles in the
     triangulation in the intersecting section of two grids.
     For "bow-tie" shaped data, this problem is unlikely to
     occur, but for data with the left and right bowed inwards,
     overlapping triangles are more likely.  The latter type of
     data is also more likely to have a greater number of
     Type 3 lost points. */

  // topology signature of grid boxes
  private boolean sig;

  /**
   * Construct a Delaunay triangulation of the points in the
   * samples array, which are a sequence of 2-D grids of size
   * lenx * leny, and which may overlap with each other. This
   * situation can arise when each grid is a scan from a polar
   * orbiting satellite and the scans are perpendicular to the
   * direction of travel of the satellite. The scan grids widen
   * away from the satellite sub-point and can overlap.
   * @param samples locations of points for topology - dimensioned
   *                float[dimension][number_of_points]
   * @param lenx x size of scan grid
   * @param leny y size of scan grid
   * @throws VisADException a VisAD error occurred
   */
  public DelaunayOverlap(float[][] samples, int lenx, int leny)
                                            throws VisADException {
    if (samples.length < 2) {
      throw new SetException("DelaunayOverlap: bad dimension");
    }
    if (lenx < 2 || leny < 2) {
      throw new SetException("DelaunayOverlap: must have at least "
                            +"two points per dimension");
    }
    if (samples[0].length < lenx*leny) {
      throw new SetException("DelaunayOverlap: not enough samples");
    }
    int lenp = lenx*leny;
    int leng = (lenx-1)*(leny-1);
    int numgrids = samples[0].length/lenp;
    if (numgrids < 2) {
      throw new SetException("DelaunayOverlap: not enough grids "
                            +"(try Gridded2DSet instead)");
    }

    // samples consistency check for each grid
    sig = ( (samples[0][1]-samples[0][0])
           *(samples[1][lenx+1]-samples[1][1])
          - (samples[1][1]-samples[1][0])
           *(samples[0][lenx+1]-samples[0][1]) > 0);
    for (int curgrid=0; curgrid<numgrids; curgrid++) {
      for (int gy=0; gy<leny-1; gy++) {
        for (int gx=0; gx<lenx-1; gx++) {
          float v00x, v00y, v10x, v10y, v01x, v01y, v11x, v11y;
          int q = curgrid*lenp+gy*lenx+gx;
          v00x = samples[0][q];
          v00y = samples[1][q];
          v10x = samples[0][q+1];
          v10y = samples[1][q+1];
          v01x = samples[0][q+lenx];
          v01y = samples[1][q+lenx];
          v11x = samples[0][q+lenx+1];
          v11y = samples[1][q+lenx+1];
          if (  ( (v10x-v00x)*(v11y-v10y)
                - (v10y-v00y)*(v11x-v10x) > 0 != sig)
             || ( (v11x-v10x)*(v01y-v11y)
                - (v11y-v10y)*(v01x-v11x) > 0 != sig)
             || ( (v01x-v11x)*(v00y-v01y)
                - (v01y-v11y)*(v00x-v01x) > 0 != sig)
             || ( (v00x-v01x)*(v10y-v00y)
                - (v00y-v01y)*(v10x-v00x) > 0 != sig)  ) {
            throw new SetException("DelaunayOverlap: samples from grid "
                                   +curgrid+" do not form a valid grid "
                                   +"("+gx+","+gy+")");
          }
        }
      }
    }

    // arrays for keeping track of which points fall in which grid boxes
    int ngrid = (numgrids-1)*(lenx-1)*(leny-1);
    int[][] grid = new int[ngrid][5];
    int[] gsize = new int[ngrid];
    int[] glen = new int[ngrid];
    for (int g=0; g<numgrids-1; g++) {
      for (int gy=0; gy<leny-1; gy++) {
        for (int gx=0; gx<lenx-1; gx++) {
          int qg = leng*g + (lenx-1)*gy + gx;
          gsize[qg] = 4;
          glen[qg] = 0;
        }
      }
    }

    // Broken grid boxes (severed from a previous grid by a line)
    //   broken[*][0] = left edge break point
    //   broken[*][X] = break point of X'th grid box column
    //   broken[*][lenx] = right edge break point
    int[][] broken = new int[numgrids][lenx+1];
    for (int g=0; g<numgrids; g++) {
      for (int pix=0; pix<lenx+1; pix++) {
        broken[g][pix] = -1;
      }
    }

    // left and right outer hulls
    int[] leftedge = new int[numgrids*leny];
    int ledges = 0;
    int[] rightedge = new int[numgrids*leny];
    int redges = 0;

    // the trinum arrays keep track of the triangle
    // numbers that border the edge arrays;
    // the triedge arrays keep track of the edge numbers
    // of the triangles that border the edge arrays
    int[] ltrinum = new int[numgrids*leny];
    int[] ltriedge = new int[numgrids*leny];
    int[] rtrinum = new int[numgrids*leny];
    int[] rtriedge = new int[numgrids*leny];

    // arrays for saving previous values of edge information
    int[] old_leftedge;
    int[] old_rightedge;
    int old_ledges;
    int old_redges;
    int[] old_ltrinum;
    int[] old_ltriedge;
    int[] old_rtrinum;
    int[] old_rtriedge;

    // boolean matrix of whether each point fell into a grid
    boolean[] ptfell = new boolean[numgrids*lenx*leny];
    for (int g=0; g<numgrids; g++) {
      for (int line=0; line<leny; line++) {
        for (int pix=0; pix<lenx; pix++) {
          ptfell[lenp*g+lenx*line+pix] = false;
        }
      }
    }

    // walk construction helper arrays, for use during box triangulation
    int[] bottom = new int[lenx-1];
    for (int i=0; i<lenx-1; i++) bottom[i] = -1;
    // array for saving previous values of bottom
    int[] old_bottom;

    // more walk helper arrays, for use during the zipping algorithm.
    //   tri[*][0] = right line of grid box
    //   tri[*][1] = bottom line of grid box
    //   tri[*][2] = left line of grid box
    //   tri[*][istwo[boxnum] ? 2 : 0] = top line of grid box
    //   tlr[*][0] = triangle containing top line
    //   tlr[*][1] = triangle containing left line
    //   tlr[*][2] = triangle containing right line
    int[][] tlr = new int[leng][3];
    boolean[] istwo = new boolean[leng];

    // expandable triangle storage arrays
    int[][] tri;
    int[][] walk;
    int trisize = 0;
    int trilen = 0;

    int curgrid;
    int prevgrid = 0;


    // ******************** MAIN ALGORITHM ******************** //

    // *** PHASE 1: *** Pre-triangulation preparation

    // *** PHASE 1a: *** Figure out where all the points lie
    //                   (i.e., locate containing grid boxes).

    // array of pointers to last correct grid box of each grid
    int[] foundx = new int[numgrids];
    int[] foundy = new int[numgrids];

    // center grid box of a grid
    int cenx = lenx/2 - 1;
    int ceny = leny/2 - 1;

    // current point being examined in 1-D rasterization
    int curpt;

    // examine all grids except the first one
    for (curgrid=1; curgrid<numgrids; curgrid++) {

      // initialize found arrays to center of each grid
      for (int g=0; g<numgrids; g++) {
        foundx[g] = cenx;
        foundy[g] = ceny;
      }

      // keeps track of whether entire scan line is outside previous grid
      int lfound;
      // keeps track of whether entire grid is outside previous grid
      int gfound;

      // examine every point in the current grid
      gfound = curgrid;
      for (int line=0; line<leny; line++) {
        lfound = curgrid;
        for (int pix=0; pix<lenx; pix++) {

          // the point to be located
          curpt = lenp*curgrid + lenx*line + pix;
          float x = samples[0][curpt];
          float y = samples[1][curpt];

          // look through all applicable previous grids until box found
          boolean found = false;
          for (int g=prevgrid; g<curgrid; g++) {

            // search grid #g for containing grid box of current point
            int gx = foundx[g];
            int gy = foundy[g];
            int ogx, ogy;
            int q, qg;
            float ax, ay, bx, by, cx, cy, dx, dy;

            // marching boxes--find correct grid box
            while (!found) {
              q = lenp*g + lenx*gy + gx;       // index into samples
              ax = samples[0][q];
              ay = samples[1][q];
              bx = samples[0][q+1];
              by = samples[1][q+1];
              cx = samples[0][q+lenx];
              cy = samples[1][q+lenx];
              dx = samples[0][q+lenx+1];
              dy = samples[1][q+lenx+1];
              ogx = gx;
              ogy = gy;

              // determine marching direction
              switch (((ax - bx)*(y - by)
                     - (ay - by)*(x - bx) < 0 == sig ? 0 : 1)
                    + ((bx - dx)*(y - dy)
                     - (by - dy)*(x - dx) < 0 == sig ? 0 : 2)
                    + ((dx - cx)*(y - cy)
                     - (dy - cy)*(x - cx) < 0 == sig ? 0 : 4)
                    + ((cx - ax)*(y - ay)
                     - (cy - ay)*(x - ax) < 0 == sig ? 0 : 8)) {

                case 0: // inside this box
                        qg = leng*g + (lenx-1)*gy + gx;  // index into grid
                        found = true;
                        if (lfound > g) lfound = g;
                        if (gfound > g) gfound = g;
                        foundx[g] = gx;
                        foundy[g] = gy;

                        // mark grid box as containing current point
                        grid[qg][glen[qg]++] = curpt;

                        // expand grid array as necessary
                        if (glen[qg] > gsize[qg]) {
                          int[] newg = new int[gsize[q]+gsize[q]+1];
                          System.arraycopy(grid[qg], 0, newg, 0, gsize[qg]);
                          grid[qg] = newg;
                          gsize[qg] += gsize[qg];
                        }

                        // mark point as being found
                        ptfell[curpt] = true;

                        // break box to the lower left of point
                        if (broken[curgrid][pix] < line) {
                          broken[curgrid][pix] = line;
                        }

                        // break box to the lower right of point
                        if (broken[curgrid][pix+1] < line) {
                          broken[curgrid][pix+1] = line;
                        }
                        break;

                case 1: case 11: // up from this box
                        gy--;
                        break;
                case 2: case 7: // right from this box
                        gx++;
                        break;
                case 3: // up and right from this box
                        gx++;
                        gy--;
                        break;
                case 4: case 14: // down from this box
                        gy++;
                        break;
                case 6: // down and right from this box
                        gx++;
                        gy++;
                        break;
                case 8: case 13: // left from this box
                        gx--;
                        break;
                case 9: // up and left from this box
                        gx--;
                        gy--;
                        break;
                case 12: // down and left from this box
                        gx--;
                        gy++;
                        break;

                default: // theoretically, should never occur
                        throw new SetException("DelaunayOverlap: "
                                              +"pathological grid");
              }

              if (gx > lenx-2) gx = lenx-2;
              if (gx < 0) gx = 0;
              if (gy > leny-2) gy = leny-2;
              if (gy < 0) gy = 0;

              // check if point is outside the grid
              if (ogx == gx && ogy == gy && !found) break;
            }

            // no need to check remaining grids if a grid box was located
            if (found) break;
          }
        }

        // if entire scan line wasn't in a previous grid, advance prevgrid
        prevgrid = lfound;
      }
      // if entire grid wasn't in a previous grid, advance prevgrid
      prevgrid = gfound;
    }

    // *** PHASE 1b: *** Break boxes that contain a point from
    //                   the bottom line of the previous grid.
    //                   Search logic from above is duplicated
    //                   to maximize efficiency of the search.

    // offset of the bottom scan line of a grid
    int z = lenp-lenx;

    // re-initialize arrays to center of each grid (old values are
    // irrelevant now; center is actually a better starting guess)
    for (int g=0; g<numgrids; g++) {
      foundx[g] = cenx;
      foundy[g] = ceny;
    }

    // search all grids but the last one
    for (curgrid=0; curgrid<numgrids-1; curgrid++) {

      // search every point in the bottom scan line of curgrid
      for (int pix=0; pix<lenx; pix++) {

        // the point to be located
        curpt = lenp*curgrid + z + pix;
        float x = samples[0][curpt];
        float y = samples[1][curpt];
        int g = curgrid+1;

        // search grid #g for containing grid box of current point
        int gx = foundx[g];
        int gy = foundy[g];
        int ogx, ogy, q;
        boolean found = false;
        float ax, ay, bx, by, cx, cy, dx, dy;

        // marching boxes--find correct grid box
        while (!found) {
          q = lenp*g + lenx*gy + gx;
          ax = samples[0][q];
          ay = samples[1][q];
          bx = samples[0][q+1];
          by = samples[1][q+1];
          cx = samples[0][q+lenx];
          cy = samples[1][q+lenx];
          dx = samples[0][q+lenx+1];
          dy = samples[1][q+lenx+1];
          ogx = gx;
          ogy = gy;

          // determine marching direction
          switch (((ax - bx)*(y - by)
                 - (ay - by)*(x - bx) < 0 == sig ? 0 : 1)
                + ((bx - dx)*(y - dy)
                 - (by - dy)*(x - dx) < 0 == sig ? 0 : 2)
                + ((dx - cx)*(y - cy)
                 - (dy - cy)*(x - cx) < 0 == sig ? 0 : 4)
                + ((cx - ax)*(y - ay)
                 - (cy - ay)*(x - ax) < 0 == sig ? 0 : 8)) {

            case 0: // inside this box
                    found = true;
                    foundx[g] = gx;
                    foundy[g] = gy;

                    // break containing box
                    if (broken[g][gx+1] < gy) broken[g][gx+1] = gy;
                    break;

            case 1: case 11: // up from this box
                    gy--;
                    break;
            case 2: case 7: // right from this box
                    gx++;
                    break;
            case 3: // up and right from this box
                    gx++;
                    gy--;
                    break;
            case 4: case 14: // down from this box
                    gy++;
                    break;
            case 6: // down and right from this box
                    gx++;
                    gy++;
                    break;
            case 8: case 13: // left from this box
                    gx--;
                    break;
            case 9: // up and left from this box
                    gx--;
                    gy--;
                    break;
            case 12: // down and left from this box
                    gx--;
                    gy++;
                    break;

            default: // theoretically, should never occur
                    throw new SetException("DelaunayOverlap: "
                                          +"pathological grid");
          }
          if (gx > lenx-2) gx = lenx-2;
          if (gx < 0) gx = 0;
          if (gy > leny-2) gy = leny-2;
          if (gy < 0) gy = 0;

          // check if point is outside the grid
          if (ogx == gx && ogy == gy && !found) break;
        }
      }
    }

    // *** PHASE 2: *** triangulate points inside grid boxes

    // set leftedge to the first column of grid 0
    leftedge[0] = 0;
    for (int i=1; i<leny; i++) {
      leftedge[i] = leftedge[i-1] + lenx;
    }
    ledges += leny;

    // set rightedge to the last column of grid 0
    rightedge[0] = lenx-1;
    for (int i=1; i<leny; i++) {
      rightedge[i] = rightedge[i-1] + lenx;
    }
    redges += leny;

    // determine necessary tri array size
    // Note: This memory allocation strategy could be more precise.
    //       It should loop through only unbroken boxes, and figure
    //       out the number of triangles in the zipped sections and
    //       the number of lost points.  If these steps were
    //       implemented, then the tri and walk arrays would not be
    //       needed, and triangulation data could be stored directly
    //       in the Tri and Walk arrays.
    for (curgrid=0; curgrid<numgrids; curgrid++) {
      for (int gy=0; gy<leny-1; gy++) {
        for (int gx=0; gx<lenx-1; gx++) {
          int npts = (curgrid == numgrids-1)
                   ? 0 : glen[leng*curgrid + (lenx-1)*gy + gx];
          trisize += 2*npts+2;
        }
      }
    }
    // probably won't be more than 2*leny Type 2 lost points per grid
    trisize += 2*leny*numgrids;

    // allocate memory
    tri = new int[trisize+2][3];
    walk = new int[trisize+2][3];
    for (int i=0; i<trisize+2; i++) {
      walk[i][0] = -1;
      walk[i][1] = -1;
      walk[i][2] = -1;
    }

    // examine every grid
    for (curgrid=0; curgrid<numgrids; curgrid++) {

      // save old bottom values
      old_bottom = bottom;
      bottom = new int[lenx-1];
      for (int i=0; i<lenx-1; i++) bottom[i] = -1;

      // triangulate all unbroken boxes in this grid
      for (int gx=0; gx<lenx-1; gx++) {
        for (int gy=broken[curgrid][gx+1]+1; gy<leny-1; gy++) {

          int q = lenp*curgrid + lenx*gy + gx;
          int qmg = (lenx-1)*gy + gx;
          int qg = leng*curgrid + qmg;
          int A = q;
          int B = q + 1;
          int C = q + lenx;
          int D = q + lenx + 1;

          float ax, ay, bx, by, cx, cy, dx, dy;
          int G1, G2, G3;

          switch (curgrid == numgrids-1 ? 0 : glen[qg]) {
            case 0: // find the best diagonal of the box
                    ax = samples[0][A];
                    ay = samples[1][A];
                    bx = samples[0][B];
                    by = samples[1][B];
                    cx = samples[0][C];
                    cy = samples[1][C];
                    dx = samples[0][D];
                    dy = samples[1][D];
                    float abx = ax - bx;
                    float aby = ay - by;
                    float acx = ax - cx;
                    float acy = ay - cy;
                    float dbx = dx - bx;
                    float dby = dy - by;
                    float dcx = dx - cx;
                    float dcy = dy - cy;
                    float Q = abx*acx + aby*acy;
                    float R = dbx*abx + dby*aby;
                    float S = acx*dcx + acy*dcy;
                    float T = dbx*dcx + dby*dcy;
                    boolean diag;
                    if (Q < 0 && T < 0 || R > 0 && S > 0) diag = true;
                    else if (R < 0 && S < 0 || Q > 0 && T > 0) diag = false;
                    else if ((Q < 0 ? Q : T) < (R < 0 ? R : S)) diag = true;
                    else diag = false;

                    if (diag) {
                      tri[trilen][0] = B;
                      tri[trilen][1] = D;
                      tri[trilen][2] = A;
                      walk[trilen][2] = bottom[gx];
                      if (bottom[gx] >= 0) walk[bottom[gx]][1] = trilen;
                      walk[trilen][1] = trilen+1;
                      trilen++;
                      tri[trilen][0] = A;
                      tri[trilen][1] = D;
                      tri[trilen][2] = C;
                      if (gx > 0 && broken[curgrid][gx] < gy) {
                        walk[trilen][2] = tlr[qmg-1][2];
                        walk[tlr[qmg-1][2]][0] = trilen;
                      }
                      else walk[trilen][2] = -1;
                      walk[trilen][0] = trilen-1;
                      trilen++;
                      bottom[gx] = trilen-1;
                      tlr[qmg][0] = trilen-2;
                      tlr[qmg][1] = trilen-1;
                      tlr[qmg][2] = trilen-2;
                      istwo[qmg] = true;
                    }
                    else {
                      tri[trilen][0] = A;
                      tri[trilen][1] = B;
                      tri[trilen][2] = C;
                      walk[trilen][0] = bottom[gx];
                      if (bottom[gx] >= 0) walk[bottom[gx]][1] = trilen;
                      if (gx > 0 && broken[curgrid][gx] < gy) {
                        walk[trilen][2] = tlr[qmg-1][2];
                        walk[tlr[qmg-1][2]][0] = trilen;
                      }
                      else walk[trilen][2] = -1;
                      walk[trilen][1] = trilen+1;
                      trilen++;
                      tri[trilen][0] = B;
                      tri[trilen][1] = D;
                      tri[trilen][2] = C;
                      walk[trilen][2] = trilen-1;
                      trilen++;
                      bottom[gx] = trilen-1;
                      tlr[qmg][0] = trilen-2;
                      tlr[qmg][1] = trilen-2;
                      tlr[qmg][2] = trilen-1;
                      istwo[qmg] = false;
                    }
                    break;

            case 1: // draw the four triangles of the point
                    G1 = grid[qg][0];
                    tri[trilen][0] = B;
                    tri[trilen][1] = G1;
                    tri[trilen][2] = A;
                    walk[trilen][2] = bottom[gx];
                    if (bottom[gx] >= 0) walk[bottom[gx]][1] = trilen;
                    walk[trilen][0] = trilen+1;
                    walk[trilen][1] = trilen+3;
                    trilen++;
                    tri[trilen][0] = B;
                    tri[trilen][1] = D;
                    tri[trilen][2] = G1;
                    walk[trilen][1] = trilen+1;
                    walk[trilen][2] = trilen-1;
                    trilen++;
                    tri[trilen][0] = G1;
                    tri[trilen][1] = D;
                    tri[trilen][2] = C;
                    walk[trilen][0] = trilen-1;
                    walk[trilen][2] = trilen+1;
                    trilen++;
                    tri[trilen][0] = A;
                    tri[trilen][1] = G1;
                    tri[trilen][2] = C;
                    if (gx > 0 && broken[curgrid][gx] < gy) {
                      walk[trilen][2] = tlr[qmg-1][2];
                      walk[tlr[qmg-1][2]][0] = trilen;
                    }
                    else walk[trilen][2] = -1;
                    walk[trilen][0] = trilen-3;
                    walk[trilen][1] = trilen-1;
                    trilen++;
                    bottom[gx] = trilen-2;
                    tlr[qmg][0] = trilen-4;
                    tlr[qmg][1] = trilen-1;
                    tlr[qmg][2] = trilen-3;
                    istwo[qmg] = true;
                    break;

           default: // triangulate first two points
                    // use a diagonal comparison
                    G1 = grid[qg][0];
                    G2 = grid[qg][1];
                    float Gdx = samples[0][G2] - samples[0][G1];
                    float Gdy = samples[1][G2] - samples[1][G1];
                    ax = samples[0][A];
                    ay = samples[1][A];
                    bx = samples[0][B];
                    by = samples[1][B];
                    cx = samples[0][C];
                    cy = samples[1][C];
                    dx = samples[0][D];
                    dy = samples[1][D];
                    float val1 = Gdx*(ax-dx) + Gdy*(ay-dy);
                    if (val1 < 0) val1 = -val1;
                    float val2 = Gdx*(bx-cx) + Gdy*(by-cy);
                    if (val2 < 0) val2 = -val2;

                    if (val1 > val2) {
                      float Qx1 = ax - samples[0][G1];
                      float Qy1 = ay - samples[1][G1];
                      float Qx2 = ax - samples[0][G2];
                      float Qy2 = ay - samples[1][G2];
                      if (Qx1*Qx1 + Qy1*Qy1 < Qx2*Qx2 + Qy2*Qy2) {
                        // G1 is closer to A than G2
                        tri[trilen][0] = B;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = A;
                        walk[trilen][2] = bottom[gx];
                        if (bottom[gx] >= 0) walk[bottom[gx]][1] = trilen;
                        walk[trilen][0] = trilen+5;
                        walk[trilen][1] = trilen+1;
                        trilen++;
                        tri[trilen][0] = A;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = C;
                        if (gx > 0 && broken[curgrid][gx] < gy) {
                          walk[trilen][2] = tlr[qmg-1][2];
                          walk[tlr[qmg-1][2]][0] = trilen;
                        }
                        else walk[trilen][2] = -1;
                        walk[trilen][0] = trilen-1;
                        walk[trilen][1] = trilen+3;
                        trilen++;
                        tri[trilen][0] = G2;
                        tri[trilen][1] = D;
                        tri[trilen][2] = C;
                        walk[trilen][0] = trilen+1;
                        walk[trilen][2] = trilen+2;
                        trilen++;
                        tri[trilen][0] = B;
                        tri[trilen][1] = D;
                        tri[trilen][2] = G2;
                        walk[trilen][1] = trilen-1;
                        walk[trilen][2] = trilen+2;
                        trilen++;
                        tri[trilen][0] = C;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = G2;
                        walk[trilen][0] = trilen-3;
                        walk[trilen][1] = trilen+1;
                        walk[trilen][2] = trilen-2;
                        trilen++;
                        tri[trilen][0] = B;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = G1;
                        walk[trilen][0] = trilen-2;
                        walk[trilen][1] = trilen-1;
                        walk[trilen][2] = trilen-5;
                        trilen++;
                        bottom[gx] = trilen-4;
                        tlr[qmg][0] = trilen-6;
                        tlr[qmg][1] = trilen-5;
                        tlr[qmg][2] = trilen-3;
                        istwo[qmg] = true;
                      }
                      else {
                        // G2 is closer to A than G1
                        tri[trilen][0] = B;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = A;
                        walk[trilen][2] = bottom[gx];
                        if (bottom[gx] >= 0) walk[bottom[gx]][1] = trilen;
                        walk[trilen][0] = trilen+5;
                        walk[trilen][1] = trilen+1;
                        trilen++;
                        tri[trilen][0] = A;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = C;
                        if (gx > 0 && broken[curgrid][gx] < gy) {
                          walk[trilen][2] = tlr[qmg-1][2];
                          walk[tlr[qmg-1][2]][0] = trilen;
                        }
                        else walk[trilen][2] = -1;
                        walk[trilen][0] = trilen-1;
                        walk[trilen][1] = trilen+3;
                        trilen++;
                        tri[trilen][0] = G1;
                        tri[trilen][1] = D;
                        tri[trilen][2] = C;
                        walk[trilen][0] = trilen+1;
                        walk[trilen][2] = trilen+2;
                        trilen++;
                        tri[trilen][0] = B;
                        tri[trilen][1] = D;
                        tri[trilen][2] = G1;
                        walk[trilen][1] = trilen-1;
                        walk[trilen][2] = trilen+2;
                        trilen++;
                        tri[trilen][0] = C;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = G1;
                        walk[trilen][0] = trilen-3;
                        walk[trilen][1] = trilen+1;
                        walk[trilen][2] = trilen-2;
                        trilen++;
                        tri[trilen][0] = B;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = G2;
                        walk[trilen][0] = trilen-2;
                        walk[trilen][1] = trilen-1;
                        walk[trilen][2] = trilen-5;
                        trilen++;
                        bottom[gx] = trilen-4;
                        tlr[qmg][0] = trilen-6;
                        tlr[qmg][1] = trilen-5;
                        tlr[qmg][2] = trilen-3;
                        istwo[qmg] = true;
                      }
                    }
                    else {
                      float Qx1 = bx - samples[0][G1];
                      float Qy1 = by - samples[1][G1];
                      float Qx2 = bx - samples[0][G2];
                      float Qy2 = by - samples[1][G2];
                      if (Qx1*Qx1 + Qy1*Qy1 < Qx2*Qx2 + Qy2*Qy2) {
                        // G1 is closer to B than G2
                        tri[trilen][0] = B;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = A;
                        walk[trilen][2] = bottom[gx];
                        if (bottom[gx] >= 0) walk[bottom[gx]][1] = trilen;
                        walk[trilen][0] = trilen+3;
                        walk[trilen][1] = trilen+4;
                        trilen++;
                        tri[trilen][0] = A;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = C;
                        if (gx > 0 && broken[curgrid][gx] < gy) {
                          walk[trilen][2] = tlr[qmg-1][2];
                          walk[tlr[qmg-1][2]][0] = trilen;
                        }
                        else walk[trilen][2] = -1;
                        walk[trilen][0] = trilen+3;
                        walk[trilen][1] = trilen+1;
                        trilen++;
                        tri[trilen][0] = G2;
                        tri[trilen][1] = D;
                        tri[trilen][2] = C;
                        walk[trilen][0] = trilen+3;
                        walk[trilen][2] = trilen-1;
                        trilen++;
                        tri[trilen][0] = B;
                        tri[trilen][1] = D;
                        tri[trilen][2] = G1;
                        walk[trilen][1] = trilen+2;
                        walk[trilen][2] = trilen-3;
                        trilen++;
                        tri[trilen][0] = A;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = G2;
                        walk[trilen][0] = trilen-4;
                        walk[trilen][1] = trilen+1;
                        walk[trilen][2] = trilen-3;
                        trilen++;
                        tri[trilen][0] = D;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = G1;
                        walk[trilen][0] = trilen-3;
                        walk[trilen][1] = trilen-1;
                        walk[trilen][2] = trilen-2;
                        trilen++;
                        bottom[gx] = trilen-4;
                        tlr[qmg][0] = trilen-6;
                        tlr[qmg][1] = trilen-5;
                        tlr[qmg][2] = trilen-3;
                        istwo[qmg] = true;
                      }
                      else {
                        // G2 is closer to B than G1
                        tri[trilen][0] = B;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = A;
                        walk[trilen][2] = bottom[gx];
                        if (bottom[gx] >= 0) walk[bottom[gx]][1] = trilen;
                        walk[trilen][0] = trilen+3;
                        walk[trilen][1] = trilen+4;
                        trilen++;
                        tri[trilen][0] = A;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = C;
                        if (gx > 0 && broken[curgrid][gx] < gy) {
                          walk[trilen][2] = tlr[qmg-1][2];
                          walk[tlr[qmg-1][2]][0] = trilen;
                        }
                        else walk[trilen][2] = -1;
                        walk[trilen][0] = trilen+3;
                        walk[trilen][1] = trilen+1;
                        trilen++;
                        tri[trilen][0] = G1;
                        tri[trilen][1] = D;
                        tri[trilen][2] = C;
                        walk[trilen][0] = trilen+3;
                        walk[trilen][2] = trilen-1;
                        trilen++;
                        tri[trilen][0] = B;
                        tri[trilen][1] = D;
                        tri[trilen][2] = G2;
                        walk[trilen][1] = trilen+2;
                        walk[trilen][2] = trilen-3;
                        trilen++;
                        tri[trilen][0] = A;
                        tri[trilen][1] = G2;
                        tri[trilen][2] = G1;
                        walk[trilen][0] = trilen-4;
                        walk[trilen][1] = trilen+1;
                        walk[trilen][2] = trilen-3;
                        trilen++;
                        tri[trilen][0] = D;
                        tri[trilen][1] = G1;
                        tri[trilen][2] = G2;
                        walk[trilen][0] = trilen-3;
                        walk[trilen][1] = trilen-1;
                        walk[trilen][2] = trilen-2;
                        trilen++;
                        bottom[gx] = trilen-4;
                        tlr[qmg][0] = trilen-6;
                        tlr[qmg][1] = trilen-5;
                        tlr[qmg][2] = trilen-3;
                        istwo[qmg] = true;
                      }
                    }

                    // subtriangulate remaining points
                    int starttri = trilen-1;
                    int maxit = 2*glen[qg]+2;
                    for (int i=2; i<glen[qg]; i++) {
                      int pt = grid[qg][i];

                      // find containing triangle of point pt
                      int curtri = starttri;
                      int p0 = -1;
                      int p1 = -1;
                      int p2 = -1;
                      int w0 = -1;
                      int w1 = -1;
                      int w2 = -1;
                      boolean found = false;
                      int itnum;
                      for (itnum=0; itnum<maxit && !found; itnum++) {
                        p0 = tri[curtri][0];
                        p1 = tri[curtri][1];
                        p2 = tri[curtri][2];
                        w0 = walk[curtri][0];
                        w1 = walk[curtri][1];
                        w2 = walk[curtri][2];
                        float ptx = samples[0][pt];
                        float pty = samples[1][pt];
                        float p0x = samples[0][p0];
                        float p0y = samples[1][p0];
                        float p1x = samples[0][p1];
                        float p1y = samples[1][p1];
                        float p2x = samples[0][p2];
                        float p2y = samples[1][p2];
                        float p01x = p0x-p1x;
                        float p01y = p0y-p1y;
                        float p02x = p0x-p2x;
                        float p02y = p0y-p2y;
                        float p12x = p1x-p2x;
                        float p12y = p1y-p2y;
                        float c0 = p01x*(p0y-pty) - p01y*(p0x-ptx);
                        float c1 = p12x*(p1y-pty) - p12y*(p1x-ptx);
                        float c2 = p02x*(pty-p2y) - p02y*(ptx-p2x);
                        boolean t0 = ( c0 == 0 ||
                                       c0 > 0 == p01x*p02y - p01y*p02x > 0 );
                        boolean t1 = ( c1 == 0 ||
                                       c1 > 0 == p12x*p01y - p12y*p01x > 0 );
                        boolean t2 = ( c2 == 0 ||
                                       c2 > 0 == p02x*p12y - p02y*p12x > 0 );

                        if (!t0 && !t1 && !t2) {
                          throw new SetException("DelaunayOverlap: "
                                           +"subtriangulation error");
                        }
                        else if (!t0) {
                          if (curtri != tlr[qmg][2]) curtri = w0;
                          else throw new SetException("DelaunayOverlap: "
                                                +"subtriangulation error");
                        }
                        else if (!t1) {
                          if (curtri != bottom[gx]) curtri = w1;
                          else throw new SetException("DelaunayOverlap: "
                                                +"subtriangulation error");
                        }
                        else if (!t2) {
                          if (curtri != tlr[qmg][0] && curtri != tlr[qmg][1]) {
                            curtri = w2;
                          }
                          else throw new SetException("DelaunayOverlap: "
                                                +"subtriangulation error");
                        }
                        else found = true;
                      }

                      // should never happen
                      if (!found) throw new SetException("DelaunayOverlap: "
                                                   +"subtriangulation error");
                      else if (curtri == bottom[gx]) {
                        // curtri is on bottom edge of grid box

                        // find adjoining triangles' connecting edges
                        int we0 = -1;
                        int we2 = -1;
                        for (int w=0; w<3; w++) {
                          if (walk[w0][w] == curtri) we0 = w;
                          if (walk[w2][w] == curtri) we2 = w;
                        }
                        if (we0 < 0 || we2 < 0) {
                          throw new SetException("DelaunayOverlap: "
                                           +"subtriangulation error");
                        }

                        // define three new triangles
                        // the first new triangle overwrites the old triangle
                        tri[curtri][0] = pt;
                        // tri[curtri][1] = p1;             <-- already true
                        // tri[curtri][2] = p2;             <-- already true
                        walk[curtri][0] = trilen;
                        // walk[curtri][1] = w1;            <-- already true
                        // walk[w1][we1] = curtri;          <-- already true
                        walk[curtri][2] = trilen+1;

                        tri[trilen][0] = p1;
                        tri[trilen][1] = pt;
                        tri[trilen][2] = p0;
                        walk[trilen][0] = curtri;
                        walk[trilen][1] = trilen+1;
                        walk[trilen][2] = w0;
                        walk[w0][we0] = trilen;
                        trilen++;

                        tri[trilen][0] = p0;
                        tri[trilen][1] = pt;
                        tri[trilen][2] = p2;
                        walk[trilen][0] = trilen-1;
                        walk[trilen][1] = curtri;
                        walk[trilen][2] = w2;
                        walk[w2][we2] = trilen;
                        trilen++;
                      }
                      else if (curtri == tlr[qmg][0] || curtri == tlr[qmg][1]) {
                        // tlr[qmg][0]: curtri is on top edge of grid box
                        // tlr[qmg][1]: curtri is on left edge of grid box

                        // find adjoining triangles' connecting edges
                        int we0 = -1;
                        int we1 = -1;
                        for (int w=0; w<3; w++) {
                          if (walk[w0][w] == curtri) we0 = w;
                          if (walk[w1][w] == curtri) we1 = w;
                        }
                        if (we0 < 0 || we1 < 0) {
                          throw new SetException("DelaunayOverlap: "
                                           +"subtriangulation error");
                        }

                        // define three new triangles
                        // the first new triangle overwrites the old triangle
                        // tri[curtri][0] = p0;             <-- already true
                        tri[curtri][1] = pt;
                        // tri[curtri][2] = p2;             <-- already true
                        walk[curtri][0] = trilen;
                        walk[curtri][1] = trilen+1;
                        // walk[curtri][2] = w2;            <-- already true
                        // walk[w2][we2] = curtri;          <-- already true

                        tri[trilen][0] = p0;
                        tri[trilen][1] = p1;
                        tri[trilen][2] = pt;
                        walk[trilen][0] = w0;
                        walk[w0][we0] = trilen;
                        walk[trilen][1] = trilen+1;
                        walk[trilen][2] = curtri;
                        trilen++;

                        tri[trilen][0] = pt;
                        tri[trilen][1] = p1;
                        tri[trilen][2] = p2;
                        walk[trilen][0] = trilen-1;
                        walk[trilen][1] = w1;
                        walk[w1][we1] = trilen;
                        walk[trilen][2] = curtri;
                        trilen++;
                      }
                      else {
                        // if curtri == tlr[qmg][2],
                        // then curtri is on right edge of grid box,
                        // otherwise, curtri is not on border of grid box

                        // find adjoining triangles' connecting edges
                        int we1 = -1;
                        int we2 = -1;
                        for (int w=0; w<3; w++) {
                          if (walk[w1][w] == curtri) we1 = w;
                          if (walk[w2][w] == curtri) we2 = w;
                        }
                        if (we1 < 0 || we2 < 0) {
                          throw new SetException("DelaunayOverlap: "
                                           +"subtriangulation error");
                        }

                        // define three new triangles
                        // the first new triangle overwrites the old triangle
                        // tri[curtri][0] = p0;             <-- already true
                        // tri[curtri][1] = p1;             <-- already true
                        tri[curtri][2] = pt;
                        // walk[curtri][0] = w0;            <-- already true
                        // walk[w0][we0] = curtri;          <-- already true
                        walk[curtri][1] = trilen;
                        walk[curtri][2] = trilen+1;

                        tri[trilen][0] = pt;
                        tri[trilen][1] = p1;
                        tri[trilen][2] = p2;
                        walk[trilen][0] = curtri;
                        walk[trilen][1] = w1;
                        walk[w1][we1] = trilen;
                        walk[trilen][2] = trilen+1;
                        trilen++;

                        tri[trilen][0] = p0;
                        tri[trilen][1] = pt;
                        tri[trilen][2] = p2;
                        walk[trilen][0] = curtri;
                        walk[trilen][1] = trilen-1;
                        walk[trilen][2] = w2;
                        walk[w2][we2] = trilen;
                        trilen++;
                      }
                    }
                    break;
          }

        }
      }

      // *** PHASE 3: *** triangulate connection between grids

      if (curgrid == 0) {
        // construct ltrinum, ltriedge, rtrinum, and rtriedge
        for (int i=0; i<ledges-1; i++) {
          ltrinum[i] = tlr[i*(lenx-1)][1];
          ltriedge[i] = 2;
        }
        for (int i=0; i<redges-1; i++) {
          rtrinum[i] = tlr[(i+1)*(lenx-1)-1][2];
          rtriedge[i] = 0;
        }
      }
      else {
        // starting and ending indices of grid connection
        int start1;    // index into samples (left edge of curgrid)
        int start2;    // index into leftedge
        int end1;      // index into samples (right edge of curgrid)
        int end2;      // index into rightedge

        // find start1 and start2
        int tmup = curgrid*lenp + lenx*(broken[curgrid][0]+1);
        float tx = samples[0][tmup];
        float ty = samples[1][tmup];
        boolean sig = samples[1][leftedge[0]]
                    < samples[1][leftedge[ledges-1]];

        if (samples[1][leftedge[ledges-1]] < ty != sig
         && samples[1][leftedge[ledges-1]] != ty) {
          // use a binary search to find prospective start2
          int min = 0;
          int max = ledges-1;
          int sg = (min+max)/2;
          int itnum = 0;
          while (samples[1][leftedge[sg+1]] < ty
                == samples[1][leftedge[sg]] < ty && itnum < ledges) {
            if (samples[1][leftedge[sg]] < ty == sig) {
              // point is closer to ledges-1
              min = sg;
            }
            else {
              // point is closer to 0
              if (sg == 0) {
                throw new SetException("DelaunayOverlap: "
                                      +"illegal grid overlap");
              }
              max = sg;
            }
            sg = (min+max)/2;
            if (sg == ledges-1) {
              throw new SetException("DelaunayOverlap: "
                                    +"pathological grid overlap");
            }
            itnum++;
          }
          if (itnum >= ledges) {
            throw new SetException("DelaunayOverlap: corrupt "
                                  +"left edge structure");
          }
          int clep = leftedge[sg];
          float cx = samples[0][clep];
          float cy = samples[1][clep];
          int clep1 = leftedge[sg+1];
          float c1x = samples[0][clep1];
          float c1y = samples[1][clep1];

          // perform cross-product test to verify that points are correct
          int pt = curgrid*lenp - lenx + 1;
          float px = samples[0][pt];
          float py = samples[1][pt];

          if ( (tx-cx)*(ty-c1y) - (ty-cy)*(tx-c1x) > 0
            == (px-cx)*(py-c1y) - (py-cy)*(px-c1x) > 0 ) {
            // inverted overlap, must adopt slightly different strategy
            start2 = ledges-1;

            // use a binary search to find start1
            min = 0;
            max = leny-1;
            sg = (min+max)/2;
            itnum = 0;
            float ll = samples[1][leftedge[ledges-1]];
            int offst = curgrid*lenp;
            int offst2 = offst+lenx;
            while (samples[1][lenx*sg+offst] < ll
                == samples[1][lenx*sg+offst2] < ll && itnum < leny) {
              if (ll > samples[1][lenx*sg+offst]) {
                // point is closer to leny-1
                min = sg;
              }
              else {
                // point is closer to 0
                if (sg == 0) {
                  throw new SetException("DelaunayOverlap: "
                                        +"illegal grid overlap");
                }
                max = sg;
              }
              sg = (min+max)/2;
              if (sg == leny-1) {
                throw new SetException("DelaunayOverlap: "
                                      +"pathological grid overlap");
              }
              itnum++;
            }
            if (itnum >= leny) {
              throw new SetException("DelaunayOverlap:  corrupt "
                                    +"grid structure");
            }
            start1 = lenx*sg+offst2;
          }
          else {
            // normal overlap
            start1 = tmup;
            start2 = sg;
          }
        }
        else {
          // tmup is outside range of leftedge, use last leftedge pt
          start1 = tmup;
          start2 = ledges-1;
        }

        // find end1 and end2
        tmup = curgrid*lenp + lenx*(broken[curgrid][lenx]+2) - 1;
        tx = samples[0][tmup];
        ty = samples[1][tmup];
        sig = samples[1][rightedge[0]]
            < samples[1][rightedge[redges-1]];

        if (samples[1][rightedge[redges-1]] < ty != sig
         && samples[1][rightedge[redges-1]] != ty) {
          // use a binary search to find prospective end2
          int min = 0;
          int max = redges-1;
          int sg = (min+max)/2;
          int itnum = 0;
          while (samples[1][rightedge[sg+1]] < ty
                == samples[1][rightedge[sg]] < ty && itnum < redges) {
            if (samples[1][rightedge[sg]] < ty == sig) {
              // point is closer to redges-1
              min = sg;
            }
            else {
              // point is closer to 0
              if (sg == 0) {
                throw new SetException("DelaunayOverlap: "
                                      +"illegal grid overlap");
              }
              max = sg;
            }
            sg = (min+max)/2;
            if (sg == redges-1) {
              throw new SetException("DelaunayOverlap: "
                                    +"pathological grid overlap");
            }
            itnum++;
          }
          if (itnum >= redges) {
            throw new SetException("DelaunayOverlap: corrupt "
                                  +"right edge structure");
          }
          int crep = rightedge[sg];
          float cx = samples[0][crep];
          float cy = samples[1][crep];
          int crep1 = rightedge[sg+1];
          float c1x = samples[0][crep1];
          float c1y = samples[1][crep1];

          // perform cross-product test to verify that points are correct
          int pt = curgrid*lenp - 2;
          float px = samples[0][pt];
          float py = samples[1][pt];

          if ( (tx-cx)*(ty-c1y) - (ty-cy)*(tx-c1x) > 0
            == (px-cx)*(py-c1y) - (py-cy)*(px-c1x) > 0 ) {
            // inverted overlap, must adopt slightly different strategy
            end2 = redges-1;

            // use a binary search to find end1
            min = 0;
            max = leny-1;
            sg = (min+max)/2;
            itnum = 0;
            float ll = samples[1][rightedge[redges-1]];
            int offst = curgrid*lenp+lenx-1;
            int offst2 = offst+lenx;
            while (samples[1][lenx*sg+offst] < ll
                == samples[1][lenx*sg+offst2] < ll && itnum < leny) {
              if (ll > samples[1][lenx*sg+offst]) {
                // point is closer to leny-1
                min = sg;
              }
              else {
                // point is closer to 0
                if (sg == 0) {
                  throw new SetException("DelaunayOverlap: "
                                        +"illegal grid overlap");
                }
                max = sg;
              }
              sg = (min+max)/2;
              if (sg == leny-1) {
                throw new SetException("DelaunayOverlap: "
                                      +"pathological grid overlap");
              }
              itnum++;
            }
            if (itnum >= leny) {
              throw new SetException("DelaunayOverlap:  corrupt "
                                    +"grid structure");
            }
            end1 = lenx*sg+offst2;
          }
          else {
            // normal overlap
            end1 = tmup;
            end2 = sg;
          }
        }
        else {
          // tmup is outside range of rightedge, use last rightedge pt
          end1 = tmup;
          end2 = redges-1;
        }

        // save old edge values
        old_leftedge = leftedge;
        old_rightedge = rightedge;
        old_ledges = ledges;
        old_redges = redges;
        old_ltrinum = ltrinum;
        old_ltriedge = ltriedge;
        old_rtrinum = rtrinum;
        old_rtriedge = rtriedge;

        // update leftedge and rightedge arrays
        leftedge = new int[numgrids*leny];
        ltrinum = new int[numgrids*leny];
        ltriedge = new int[numgrids*leny];
        ledges = 0;
        rightedge = new int[numgrids*leny];
        rtrinum = new int[numgrids*leny];
        rtriedge = new int[numgrids*leny];
        redges = 0;
        System.arraycopy(old_leftedge, 0, leftedge, 0, start2+1);
        System.arraycopy(old_ltrinum, 0, ltrinum, 0, start2);
        System.arraycopy(old_ltriedge, 0, ltriedge, 0, start2);
        ledges += start2+1;
        System.arraycopy(old_rightedge, 0, rightedge, 0, end2+1);
        System.arraycopy(old_rtrinum, 0, rtrinum, 0, end2);
        System.arraycopy(old_rtriedge, 0, rtriedge, 0, end2);
        redges += end2+1;
        for (int i=start1; i<=(curgrid+1)*lenp-lenx; i+=lenx) {
          leftedge[ledges++] = i;
        }
        for (int i=end1; i<(curgrid+1)*lenp; i+=lenx) {
          rightedge[redges++] = i;
        }
        int curledge = start2;
        int curredge = redges - leny + broken[curgrid][lenx-1];

        // indices into current quadrilateral's points
        int base1, oneUp1;
        int base2, oneUp2;

        // x and y coordinates of base1 and oneUp1
        int base1x, base1y, oneUp1x, oneUp1y;

        // flag indicating which array base2 & oneUp2 reference
        // b2lbr = 0  -->  base2 & oneUp2 reference old_leftedge array
        // b2lbr = 1  -->  base2 & oneUp2 reference samples array
        //                                          (bottom of curgrid-1)
        // b2lbr = 2  -->  base2 & oneUp2 reference old_rightedge array
        int b2lbr = 0;

        // initialize base1 and base2
        base1 = start1;
        base2 = start2;
        if (base2 == old_ledges-1) {
          base2 = curgrid*lenp - lenx;
          b2lbr = 1;
        }
        base1x = 0;
        base1y = base1 % lenp / lenx;
        boolean down = false;

        // initialize oneUp1 and oneUp2
        if (base1y > 0 && broken[curgrid][0] < base1y - 1) {
          // move up
          oneUp1x = 0;
          oneUp1y = base1y - 1;
        }
        else if (base1y == leny-1 || broken[curgrid][1] < base1y) {
          // move right
          oneUp1x = 1;
          oneUp1y = base1y;
        }
        else {
          // move down
          oneUp1x = 0;
          oneUp1y = base1y + 1;
          down = true;
        }
        oneUp1 = curgrid*lenp + oneUp1y*lenx + oneUp1x;
        oneUp2 = base2 + 1;

        boolean diag = false;
        boolean firsttri = true;
        while (base1 != end1 || base2 != end2 || b2lbr != 2) {

          // determine which diagonal to take
          if (base1 == end1) diag = true;
          else if (base2 == end2 && b2lbr == 2) diag = false;
          else {
            // set up variables
            float ax = samples[0][base1];
            float ay = samples[1][base1];
            float cx = samples[0][oneUp1];
            float cy = samples[1][oneUp1];
            float bx, by, dx, dy;
            if (b2lbr == 0) {
              bx = samples[0][old_leftedge[base2]];
              by = samples[1][old_leftedge[base2]];
              dx = samples[0][old_leftedge[oneUp2]];
              dy = samples[1][old_leftedge[oneUp2]];
            }
            else if (b2lbr == 1) {
              bx = samples[0][base2];
              by = samples[1][base2];
              dx = samples[0][oneUp2];
              dy = samples[1][oneUp2];
            }
            else {
              bx = samples[0][old_rightedge[base2]];
              by = samples[1][old_rightedge[base2]];
              dx = samples[0][old_rightedge[oneUp2]];
              dy = samples[1][old_rightedge[oneUp2]];
            }

            // perform diagonal test
            float abx = ax - bx;
            float aby = ay - by;
            float acx = ax - cx;
            float acy = ay - cy;
            float dbx = dx - bx;
            float dby = dy - by;
            float dcx = dx - cx;
            float dcy = dy - cy;
            float Q = abx*acx + aby*acy;
            float R = dbx*abx + dby*aby;
            float S = acx*dcx + acy*dcy;
            float T = dbx*dcx + dby*dcy;
            boolean QD = abx*acy - aby*acx > 0;
            boolean RD = dbx*aby - dby*abx > 0;
            boolean SD = acx*dcy - acy*dcx > 0;
            boolean TD = dcx*dby - dcy*dbx > 0;
            boolean sigD = (QD ? 1 : 0) + (RD ? 1 : 0)
                         + (SD ? 1 : 0) + (TD ? 1 : 0) < 2;
            if (QD == sigD) diag = true;
            else if (RD == sigD || SD == sigD) diag = false;
            else if (TD == sigD) diag = true;
            else if (Q < 0 && T < 0 || R > 0 && S > 0) diag = true;
            else if (R < 0 && S < 0 || Q > 0 && T > 0) diag = false;
            else if ((Q < 0 ? Q : T) < (R < 0 ? R : S)) diag = true;
            else diag = false;
          }

          // walk[*][0]             -->  NEXT
          // walk[*][diag ? 1 : 2]  -->  PREVIOUS
          // walk[*][diag ? 2 : 1]  -->  OUTSIDE

          if (diag) {  // diagonal goes from base1 to oneUp2

            // update tri array
            if (b2lbr == 0) {
              tri[trilen][0] = old_leftedge[oneUp2];
              tri[trilen][1] = base1;
              tri[trilen][2] = old_leftedge[base2];
            }
            else if (b2lbr == 1) {
              tri[trilen][0] = oneUp2;
              tri[trilen][1] = base1;
              tri[trilen][2] = base2;
            }
            else {
              tri[trilen][0] = old_rightedge[oneUp2];
              tri[trilen][1] = base1;
              tri[trilen][2] = old_rightedge[base2];
            }

            // update walk array

            // current --> next
            walk[trilen][0] = -1;

            // current <--> previous
            if (firsttri) {
              ltrinum[curledge] = trilen;
              ltriedge[curledge] = 2;
              curledge++;
              firsttri = false;
            }
            else {
              walk[trilen][1] = trilen-1;
              walk[trilen-1][0] = trilen;
            }

            // current <--> outside
            if (b2lbr == 0) {
              int x = old_ltrinum[base2];
              walk[trilen][2] = x;
              walk[x][old_ltriedge[base2]] = trilen;
            }
            else if (b2lbr == 1) {
              int x = old_bottom[base2 % lenx];
              walk[trilen][2] = x;
              walk[x][1] = trilen;
            }
            else {
              int x = old_rtrinum[oneUp2];
              walk[trilen][2] = x;
              walk[x][old_rtriedge[oneUp2]] = trilen;
            }

            // update base2
            base2 = oneUp2;
            if (b2lbr == 0 && base2 == old_ledges-1) {
              b2lbr = 1;
              base2 = curgrid*lenp - lenx;
            }
            if (b2lbr == 1 && base2 == curgrid*lenp - 1) {
              b2lbr = 2;
              base2 = old_redges-1;
            }

            // update oneUp2
            oneUp2 = base2 + (b2lbr == 2 ? -1 : 1);
          }
          else {       // diagonal goes from base2 to oneUp1

            // update tri array
            if (b2lbr == 0) {
              tri[trilen][0] = old_leftedge[base2];
              tri[trilen][1] = oneUp1;
              tri[trilen][2] = base1;
            }
            else if (b2lbr == 1) {
              tri[trilen][0] = base2;
              tri[trilen][1] = oneUp1;
              tri[trilen][2] = base1;
            }
            else {
              tri[trilen][0] = old_rightedge[base2];
              tri[trilen][1] = oneUp1;
              tri[trilen][2] = base1;
            }

            // update walk array

            // current --> next
            walk[trilen][0] = -1;

            // current <--> previous
            if (firsttri) {
              ltrinum[curledge] = trilen;
              ltriedge[curledge] = 1;
              curledge++;
              firsttri = false;
            }
            else {
              walk[trilen][2] = trilen-1;
              walk[trilen-1][0] = trilen;
            }

            // current <--> outside
            if (oneUp1 - base1 == -lenx) {
              // base1 -> oneUp1 = up
              if (base1x < lenx-1) {
                int x = tlr[(lenx-1)*oneUp1y+oneUp1x][1];
                walk[trilen][1] = x;
                walk[x][2] = trilen;
              }
              else {
                // base1 is on the right edge of current grid
                walk[trilen][1] = -1;

                // update right edge triangle structure
                rtrinum[curredge] = trilen;
                rtriedge[curredge] = 1;
                curredge--;
              }
            }
            else if (oneUp1 - base1 == 1) {
              // base1 -> oneUp1 = right
              if (base1y < leny-1) {
                int inx = (lenx-1)*base1y+base1x;
                int x = tlr[inx][0];
                walk[trilen][1] = x;
                walk[x][istwo[inx] ? 2 : 0] = trilen;
              }
              else {
                // base1 is on the bottom edge of current grid
                walk[trilen][1] = -1;
                bottom[base1x] = trilen;
              }
            }
            else {
              // base1 -> oneUp1 = down
              if (base1x > 0) {
                int x = tlr[(lenx-1)*base1y+base1x-1][2];
                walk[trilen][1] = x;
                walk[x][0] = trilen;
              }
              else {
                // base1 is on the left edge of current grid
                walk[trilen][1] = -1;
                ltrinum[curledge] = trilen;
                ltriedge[curledge] = 1;
                curledge++;
              }
            }

            // update base1
            base1 = oneUp1;
            base1x = oneUp1x;
            base1y = oneUp1y;

            // update oneUp1
            if (broken[curgrid][base1x == 0 ? 0 : base1x+1] < base1y - 1
                                       && base1y > 0 && !down) {
              // move up
              oneUp1x = base1x;
              oneUp1y = base1y - 1;
            }
            else if ( base1y == leny-1 || (base1x < lenx-1
                             && broken[curgrid][base1x+1] < base1y) ) {
              // move right
              oneUp1x = base1x+1;
              oneUp1y = base1y;
              down = false;
            }
            else {
              // move down
              oneUp1x = base1x;
              oneUp1y = base1y + 1;
              down = true;
            }
            oneUp1 = curgrid*lenp + oneUp1y*lenx + oneUp1x;
          }

          trilen++;

          // expand tri array if necessary
          if (trilen > trisize) {
            trisize += trisize;
            int[][] newtri = new int[trisize+2][3];
            int[][] newwalk = new int[trisize+2][3];
            for (int i=0; i<trilen; i++) {
              newtri[i][0] = tri[i][0];
              newtri[i][1] = tri[i][1];
              newtri[i][2] = tri[i][2];
              newwalk[i][0] = walk[i][0];
              newwalk[i][1] = walk[i][1];
              newwalk[i][2] = walk[i][2];
            }
            for (int i=trilen; i<trisize+2; i++) {
              newwalk[i][0] = -1;
              newwalk[i][1] = -1;
              newwalk[i][2] = -1;
            }
            tri = newtri;
            walk = newwalk;
          }
        }

        // add triangle number (trilen-1) to rtrinum and rtriedge
        rtrinum[curredge] = trilen-1;
        rtriedge[curredge] = (diag ? 2 : 1);
        curredge = redges - leny + broken[curgrid][lenx-1] + 1;

        // finish updating ltrinum, ltriedge, rtrinum, and rtriedge
        int x = broken[curgrid][1]+1;
        for (int i=x; i<leny-1; i++) {
          ltrinum[curledge] = tlr[(lenx-1)*i][1];
          ltriedge[curledge] = 2;
          curledge++;
        }
        x = broken[curgrid][lenx-1]+1;
        for (int i=x; i<leny-1; i++) {
          rtrinum[curredge] = tlr[(lenx-1)*(i+1)-1][2];
          rtriedge[curredge] = 0;
          curredge++;
        }
      }
    }

    // *** PHASE 4: *** sub-triangulate lost grid points

    for (curgrid=0; curgrid<numgrids; curgrid++) {

      // *** PHASE 4a: *** sub-triangulate Type 1 lost grid points

      // find all Type 1 lost grid points and deal with them.
      // Type 1 lost grid points are those that fall into a broken box

      // The final grid's boxes cannot contain any points, and
      // thus cannot contain any Type 1 lost points
      if (curgrid < numgrids-1) {
        int curtri = trilen/2;   // a reasonable starting guess
        for (int gx=0; gx<lenx-1; gx++) {
          for (int gy=0; gy<=broken[curgrid][gx+1]; gy++) {

            // subtriangulate all points within broken grid boxes
            int qg = leng*curgrid + (lenx-1)*gy + gx;

            if (curtri < 0) curtri = trilen/2;
            for (int pt=0; pt<glen[qg]; pt++) {

              // point (px, py) is broken
              float Px = samples[0][grid[qg][pt]];
              float Py = samples[1][grid[qg][pt]];

              // locate containing triangle of (px, py)
              boolean located = false;
              int itnum;
              for (itnum=0; itnum<trilen && !located; itnum++) {
                // define data
                int t0 = tri[curtri][0];
                int t1 = tri[curtri][1];
                int t2 = tri[curtri][2];
                float Ax = samples[0][t0];
                float Ay = samples[1][t0];
                float Bx = samples[0][t1];
                float By = samples[1][t1];
                float Cx = samples[0][t2];
                float Cy = samples[1][t2];

                // test whether point is contained in current triangle
                float tval0 = (Bx-Ax)*(Py-Ay) - (By-Ay)*(Px-Ax);
                float tval1 = (Cx-Bx)*(Py-By) - (Cy-By)*(Px-Bx);
                float tval2 = (Ax-Cx)*(Py-Cy) - (Ay-Cy)*(Px-Cx);
                boolean test0 = (tval0 == 0) || ( (tval0 > 0) == (
                                (Bx-Ax)*(Cy-Ay) - (By-Ay)*(Cx-Ax) > 0) );
                boolean test1 = (tval1 == 0) || ( (tval1 > 0) == (
                                (Cx-Bx)*(Ay-By) - (Cy-By)*(Ax-Bx) > 0) );
                boolean test2 = (tval2 == 0) || ( (tval2 > 0) == (
                                (Ax-Cx)*(By-Cy) - (Ay-Cy)*(Bx-Cx) > 0) );

                // figure out which triangle to go to next
                if (!test0 && !test1 && !test2) {
                  throw new SetException("DelaunayOverlap: corrupt "
                                        +"triangle structure");
                }
                if (!test0 && !test1) {
                  int tri0 = walk[curtri][0];
                  int tri1 = walk[curtri][1];
                  if (tri0 == -1) curtri = tri1;
                  else curtri = tri0;
                }
                else if (!test0 && !test2) {
                  int tri0 = walk[curtri][0];
                  int tri2 = walk[curtri][2];
                  if (tri0 == -1) curtri = tri2;
                  else curtri = tri0;
                }
                else if (!test1 && !test2) {
                  int tri1 = walk[curtri][1];
                  int tri2 = walk[curtri][2];
                  if (tri1 == -1) curtri = tri2;
                  else curtri = tri1;
                }
                else if (!test0) curtri = walk[curtri][0];
                else if (!test1) curtri = walk[curtri][1];
                else if (!test2) curtri = walk[curtri][2];
                else located = true;

                // point is outside current triangulation
                if (curtri < 0) itnum = trilen;
              }

              // if itnum == trilen, the point is permanently lost
              if (itnum < trilen) {
                // subtriangulate point grid[qg][pt]
                // (curtri is the containing triangle)
                // get curtri's point information
                int q = grid[qg][pt];
                int ct0 = tri[curtri][0];
                int ct1 = tri[curtri][1];
                int ct2 = tri[curtri][2];

                // get curtri's walk information
                int T0 = walk[curtri][0];
                int T1 = walk[curtri][1];
                int T2 = walk[curtri][2];
                int TE0, TE1, TE2;
                if (T0 == -1) TE0 = -1;
                else if (walk[T0][0] == curtri) TE0 = 0;
                else if (walk[T0][1] == curtri) TE0 = 1;
                else if (walk[T0][2] == curtri) TE0 = 2;
                else throw new SetException("DelaunayOverlap: corrupt "
                                         +"walk structure");
                if (T1 == -1) TE1 = -1;
                else if (walk[T1][0] == curtri) TE1 = 0;
                else if (walk[T1][1] == curtri) TE1 = 1;
                else if (walk[T1][2] == curtri) TE1 = 2;
                else throw new SetException("DelaunayOverlap: corrupt "
                                         +"walk structure");
                if (T2 == -1) TE2 = -1;
                else if (walk[T2][0] == curtri) TE2 = 0;
                else if (walk[T2][1] == curtri) TE2 = 1;
                else if (walk[T2][2] == curtri) TE2 = 2;
                else throw new SetException("DelaunayOverlap: corrupt "
                                         +"walk structure");

                // define three new triangles
                // the first new triangle overwrites the old triangle
                // tri[curtri][0] = ct0;                  <-- already true
                // tri[curtri][1] = ct1;                  <-- already true
                tri[curtri][2] = q;
                // walk[curtri][0] = T0;                  <-- already true
                // if (TE0 >= 0) walk[T0][TE0] = curtri;  <-- already true
                walk[curtri][1] = trilen;
                walk[curtri][2] = trilen+1;

                tri[trilen][0] = q;
                tri[trilen][1] = ct1;
                tri[trilen][2] = ct2;
                walk[trilen][0] = curtri;
                walk[trilen][1] = T1;
                if (TE1 >= 0) walk[T1][TE1] = trilen;
                walk[trilen][2] = trilen+1;
                trilen++;

                tri[trilen][0] = ct0;
                tri[trilen][1] = q;
                tri[trilen][2] = ct2;
                walk[trilen][0] = curtri;
                walk[trilen][1] = trilen-1;
                walk[trilen][2] = T2;
                if (TE2 >= 0) walk[T2][TE2] = trilen;
                trilen++;

                // expand tri array if necessary
                if (trilen > trisize) {
                  trisize += trisize;
                  int[][] newtri = new int[trisize+2][3];
                  int[][] newwalk = new int[trisize+2][3];
                  for (int i=0; i<trilen; i++) {
                    newtri[i][0] = tri[i][0];
                    newtri[i][1] = tri[i][1];
                    newtri[i][2] = tri[i][2];
                    newwalk[i][0] = walk[i][0];
                    newwalk[i][1] = walk[i][1];
                    newwalk[i][2] = walk[i][2];
                  }
                  for (int i=trilen; i<trisize+2; i++) {
                    newwalk[i][0] = -1;
                    newwalk[i][1] = -1;
                    newwalk[i][2] = -1;
                  }
                  tri = newtri;
                  walk = newwalk;
                }

              }
            }
          }
        }
      }

      // *** PHASE 4b: *** sub-triangulate Type 2 lost grid points

      // find all Type 2 lost grid points and deal with them.
      // Type 2 lost grid points are those that did not fall into any
      // box, but which have both grid boxes below them broken.
      int curtri = trilen/2;
      for (int line=0; line<leny-1; line++) {
        for (int pix=1; pix<lenx-1; pix++) {

          // find out if point is lost
          int q = lenp*curgrid + lenx*line + pix;
          if (!ptfell[q] && broken[curgrid][pix] >= line
                         && broken[curgrid][pix+1] >= line) {

            // point (px, py) is lost
            float Px = samples[0][q];
            float Py = samples[1][q];

            // locate containing triangle of (px, py)
            boolean located = false;
            int itnum;
            for (itnum=0; itnum<trilen && !located; itnum++) {
              // define data
              int t0 = tri[curtri][0];
              int t1 = tri[curtri][1];
              int t2 = tri[curtri][2];
              float Ax = samples[0][t0];
              float Ay = samples[1][t0];
              float Bx = samples[0][t1];
              float By = samples[1][t1];
              float Cx = samples[0][t2];
              float Cy = samples[1][t2];

              // test whether point is contained in current triangle
              float tval0 = (Bx-Ax)*(Py-Ay) - (By-Ay)*(Px-Ax);
              float tval1 = (Cx-Bx)*(Py-By) - (Cy-By)*(Px-Bx);
              float tval2 = (Ax-Cx)*(Py-Cy) - (Ay-Cy)*(Px-Cx);
              boolean test0 = (tval0 == 0) || ( (tval0 > 0) == (
                              (Bx-Ax)*(Cy-Ay) - (By-Ay)*(Cx-Ax) > 0) );
              boolean test1 = (tval1 == 0) || ( (tval1 > 0) == (
                              (Cx-Bx)*(Ay-By) - (Cy-By)*(Ax-Bx) > 0) );
              boolean test2 = (tval2 == 0) || ( (tval2 > 0) == (
                              (Ax-Cx)*(By-Cy) - (Ay-Cy)*(Bx-Cx) > 0) );

              // figure out which triangle to go to next
              if (!test0 && !test1 && !test2) {
                throw new SetException("DelaunayOverlap: corrupt "
                                      +"triangle structure");
              }
              if (!test0 && !test1) {
                int tri0 = walk[curtri][0];
                int tri1 = walk[curtri][1];
                if (tri0 == -1) curtri = tri1;
                else curtri = tri0;
              }
              else if (!test0 && !test2) {
                int tri0 = walk[curtri][0];
                int tri2 = walk[curtri][2];
                if (tri0 == -1) curtri = tri2;
                else curtri = tri0;
              }
              else if (!test1 && !test2) {
                int tri1 = walk[curtri][1];
                int tri2 = walk[curtri][2];
                if (tri1 == -1) curtri = tri2;
                else curtri = tri1;
              }
              else if (!test0) curtri = walk[curtri][0];
              else if (!test1) curtri = walk[curtri][1];
              else if (!test2) curtri = walk[curtri][2];
              else located = true;

              // point is outside current triangulation
              // (this point is permanently lost)
              if (curtri < 0) itnum = trilen;
            }

            // if itnum == trilen, the point is permanently lost
            if (itnum < trilen) {
              // subtriangulate point q
              // (curtri is the containing triangle)
              // get curtri's point information
              int ct0 = tri[curtri][0];
              int ct1 = tri[curtri][1];
              int ct2 = tri[curtri][2];

              // get curtri's walk information
              int T0 = walk[curtri][0];
              int T1 = walk[curtri][1];
              int T2 = walk[curtri][2];
              int TE0, TE1, TE2;
              if (T0 == -1) TE0 = -1;
              else if (walk[T0][0] == curtri) TE0 = 0;
              else if (walk[T0][1] == curtri) TE0 = 1;
              else if (walk[T0][2] == curtri) TE0 = 2;
              else throw new SetException("DelaunayOverlap: corrupt "
                                         +"walk structure");
              if (T1 == -1) TE1 = -1;
              else if (walk[T1][0] == curtri) TE1 = 0;
              else if (walk[T1][1] == curtri) TE1 = 1;
              else if (walk[T1][2] == curtri) TE1 = 2;
              else throw new SetException("DelaunayOverlap: corrupt "
                                         +"walk structure");
              if (T2 == -1) TE2 = -1;
              else if (walk[T2][0] == curtri) TE2 = 0;
              else if (walk[T2][1] == curtri) TE2 = 1;
              else if (walk[T2][2] == curtri) TE2 = 2;
              else throw new SetException("DelaunayOverlap: corrupt "
                                         +"walk structure");

              // define three new triangles
              // the first new triangle overwrites the old triangle
              // tri[curtri][0] = ct0;                  <-- already true
              // tri[curtri][1] = ct1;                  <-- already true
              tri[curtri][2] = q;
              // walk[curtri][0] = T0;                  <-- already true
              // if (TE0 >= 0) walk[T0][TE0] = curtri;  <-- already true
              walk[curtri][1] = trilen;
              walk[curtri][2] = trilen+1;

              tri[trilen][0] = q;
              tri[trilen][1] = ct1;
              tri[trilen][2] = ct2;
              walk[trilen][0] = curtri;
              walk[trilen][1] = T1;
              if (TE1 >= 0) walk[T1][TE1] = trilen;
              walk[trilen][2] = trilen+1;
              trilen++;

              tri[trilen][0] = ct0;
              tri[trilen][1] = q;
              tri[trilen][2] = ct2;
              walk[trilen][0] = curtri;
              walk[trilen][1] = trilen-1;
              walk[trilen][2] = T2;
              if (TE2 >= 0) walk[T2][TE2] = trilen;
              trilen++;

              // expand tri array if necessary
              if (trilen > trisize) {
                trisize += trisize;
                int[][] newtri = new int[trisize+2][3];
                int[][] newwalk = new int[trisize+2][3];
                for (int i=0; i<trilen; i++) {
                  newtri[i][0] = tri[i][0];
                  newtri[i][1] = tri[i][1];
                  newtri[i][2] = tri[i][2];
                  newwalk[i][0] = walk[i][0];
                  newwalk[i][1] = walk[i][1];
                  newwalk[i][2] = walk[i][2];
                }
                for (int i=trilen; i<trisize+2; i++) {
                  newwalk[i][0] = -1;
                  newwalk[i][1] = -1;
                  newwalk[i][2] = -1;
                }
                tri = newtri;
                walk = newwalk;
              }

            }
          }
        }
      }

      // There is a third type of lost grid point.  If a point on
      // the left or right edge of a grid is cut off from the rest
      // of its grid by a zigzagging grid edge from a previous grid,
      // that point is Type 3 lost and will not be incorporated into
      // the triangulation.  This case is not yet handled by
      // DelaunayOverlap's algorithm, but will be added to the
      // constructor if data sets exhibit properties which cause
      // the loss of many points.
    }

    // *** PHASE 5: *** finish the triangulation
    // copy information into Tri and Walk arrays
    Tri = new int[trilen][3];
    Walk = new int[trilen][3];
    for (int i=0; i<trilen; i++) {
      Tri[i][0] = tri[i][0];
      Tri[i][1] = tri[i][1];
      Tri[i][2] = tri[i][2];
      Walk[i][0] = walk[i][0];
      Walk[i][1] = walk[i][1];
      Walk[i][2] = walk[i][2];
    }

    // call more generic method for constructing Vertices and Edges arrays
    finish_triang(samples);
  }

  private static final float[][] m_samples = {  // grid 1 x-coordinates
                               { 65, 142, 215, 315, 373, 435,  39, 118,
                                202, 320, 373, 455,  40, 114, 206, 307,
                                384, 457,  66, 128, 208, 308, 380, 436,
                                // grid 2 x-coordinates
                                 83, 144, 201, 293, 354, 433,  60, 135,
                                202, 285, 355, 456,  59, 136, 204, 284,
                                362, 456,  75, 138, 207, 285, 363, 438,
                                // grid 3 x-coordinates
                                 90, 153, 217, 292, 358, 441,  61, 145,
                                216, 292, 366, 452,  55, 143, 213, 295,
                                373, 463,  80, 148, 217, 295, 375, 444},
                                // grid 1 y-coordinates
                               { 67,  87, 103, 104,  72,  42, 122, 148,
                                160, 165, 156, 109, 212, 211, 203, 200,
                                204, 211, 282, 263, 248, 243, 256, 287,
                                // grid 2 y-coordinates
                                166, 187, 201, 207, 185, 155, 230, 235,
                                240, 241, 235, 210, 283, 275, 270, 267,
                                273, 280, 338, 310, 299, 297, 305, 331,
                                // grid 3 y-coordinates
                                247, 270, 277, 276, 265, 233, 295, 319,
                                325, 322, 306, 281, 368, 371, 368, 362,
                                372, 376, 464, 431, 417, 418, 424, 455} };

  private static final int m_lenx = 6;
  private static final int m_leny = 4;
  private static final int m_numgrids = 3;

  private static final Color[] m_col =
    {Color.black, Color.gray, Color.blue};

  private static DelaunayOverlap delO = null;

  /**
   * run 'java visad.DelaunayOverlap' to test the DelaunayOverlap class
   * @param argv command line arguments
   * @throws VisADException a VisAD error occurred
   */
  public static void main(String[] argv) throws VisADException {

    Frame frame = new Frame("DelaunayOverlap");
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    frame.addWindowListener(l);
    frame.setSize(500, 600);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - 250,
                      screenSize.height/2 - 300);

    Panel big_panel = new Panel();
    big_panel.setLayout(new GridLayout(1, 1));
    frame.add(big_panel);

    Canvas gcan = new Canvas() {
      public void paint(Graphics gr) {
        // draw triangulation
        if (delO != null) {
          gr.setColor(Color.red);
          for (int t=0; t<delO.Tri.length; t++) {
            for (int e=0; e<3; e++) {
              int gx1 = (int)m_samples[0][delO.Tri[t][e]];
              int gy1 = (int)m_samples[1][delO.Tri[t][e]];
              int gx2 = (int)m_samples[0][delO.Tri[t][(e+1)%3]];
              int gy2 = (int)m_samples[1][delO.Tri[t][(e+1)%3]];
              gr.drawLine(gx1, gy1, gx2, gy2);
            }
          }
        }

        // plot points
        int colnum = 0;
        for (int p=0; p<m_numgrids*m_lenx*m_leny; p++) {
          int x = (int)m_samples[0][p];
          int y = (int)m_samples[1][p];
          if (p % (m_lenx*m_leny) == 0) {
            gr.setColor(m_col[colnum++]);
          }
          gr.fillRect(x-2, y-2, 4, 4);
        }
      }
    };
    big_panel.add(gcan);

    System.out.println("Constructing triangulation...");
    long startTime = System.currentTimeMillis();
    delO = new DelaunayOverlap(m_samples, m_lenx, m_leny);
    long endTime = System.currentTimeMillis();
    frame.setVisible(true);
    System.out.println("Algorithm completed successfully.");
    float algTime = (float)(endTime-startTime) / 1000;
    System.out.println("Triangulation took "+algTime+" seconds.");

    // Test the triangulation for errors
    System.out.println("Testing triangulation...");
    boolean good = delO.test(m_samples);
    if (!good) System.out.println("Errors in triangulation!");
    else System.out.println("Triangulation was constructed correctly.");
  }

}

