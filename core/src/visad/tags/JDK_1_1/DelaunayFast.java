
//
// DelaunayFast.java
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
   DelaunayFast is a method of finding an imperfect triangulation
   or tetrahedralization of a set of samples of R^2 or R^3.
   It provides a substantial speed increase over
   the true Delaunay triangulation algorithms.<P>
*/
public class DelaunayFast extends Delaunay {

  // <<< Modified quick sort routine >>>
  private final void qsort(int[] array, float[][] samples,
                           int sIndex, int lo, int hi) {
    if (lo < hi) {
      int pivot = (lo + hi)/2;
      int swap = array[lo];
      array[lo] = array[pivot];
      array[pivot] = swap;

      pivot = lo;
      for (int i=lo+1; i<=hi; i++)
        if (samples[sIndex][array[i]] < samples[sIndex][array[lo]]) {
          swap = array[i];
          array[i] = array[++pivot];
          array[pivot] = swap;
        }

      swap = array[lo];
      array[lo] = array[pivot];
      array[pivot] = swap;

      if (lo < pivot-1) qsort(array, samples, sIndex, lo, pivot-1);
      if (pivot+1 < hi) qsort(array, samples, sIndex, pivot+1, hi);
    }
  }

  // <<< Constructor >>>
  public DelaunayFast(float[][] samples) throws VisADException {
    if (samples.length < 2 || samples.length > 3) {
      throw new VisADException("DelaunayFast: dimension must be 2 or 3");
    }
    if (samples.length == 3) {
      throw new UnimplementedException("DelaunayFast: "
                                      +"only two dimensions for now");
    }
    int numpts = Math.min(samples[0].length, samples[1].length);
    if (numpts < 3) {
      throw new VisADException("DelaunayFast: triangulation is "
                              +"futile with less than 3 samples");
    }
    float[][] samp = new float[2][numpts];
    System.arraycopy(samples[0], 0, samp[0], 0, numpts);
    System.arraycopy(samples[1], 0, samp[1], 0, numpts);

    // misc. variables
    int ntris = 0;
    int tsize = (int) (2f/3f*numpts) + 10;
    int[][][] tris = new int[tsize][3][];
    int tp = 0;
    int[] nverts = new int[numpts];
    for (int i=0; i<numpts; i++) nverts[i] = 0;

    // set up the stack
    int ssize = 20;                         // "stack size"
    int[] ss = new int[ssize+2];            // "stack start"
    int[] se = new int[ssize+2];            // "stack end"
    boolean[] vh = new boolean[ssize+2];    // "vertical/horizontal"
    boolean[] mp = new boolean[ssize+2];    // "merge points"
    int sp = 0;                             // "stack pointer"
    int hsize = 10;                         // "hull stack size"
    int[][] hs = new int[hsize+2][];        // "hull stack"
    int hsp = 0;                            // "hull stack pointer"

    // set up initial conditions
    int[] indices = new int[numpts];
    for (int i=0; i<numpts; i++) indices[i] = i;

    // add initial conditions to stack
    sp++;
    ss[0] = 0;
    se[0] = numpts-1;
    vh[0] = false;
    mp[0] = false;

    // stack loop variables
    int css;
    int cse;
    boolean cvh;
    boolean cmp;
    
    // stack loop
    while (sp != 0) {
      if (hsp > hsize) {
        // expand hull stack if necessary
        hsize += hsize;
        int newhs[][] = new int[hsize+2][];
        System.arraycopy(hs, 0, newhs, 0, hs.length);
        hs = newhs;
      }
      if (sp > ssize) {
        // expand stack if necessary
        ssize += ssize;
        int[] newss = new int[ssize+2];
        int[] newse = new int[ssize+2];
        boolean[] newvh = new boolean[ssize+2];
        boolean[] newmp = new boolean[ssize+2];
        System.arraycopy(ss, 0, newss, 0, ss.length);
        System.arraycopy(se, 0, newse, 0, se.length);
        System.arraycopy(vh, 0, newvh, 0, vh.length);
        System.arraycopy(mp, 0, newmp, 0, mp.length);
        ss = newss;
        se = newse;
        vh = newvh;
        mp = newmp;
      }

      // pop action from stack
      sp--;
      css = ss[sp];
      cse = se[sp];
      cvh = vh[sp];
      cmp = mp[sp];

      if (!cmp) {
        // division step
        if (cse - css >= 3) {
          // sort step
          qsort(indices, samp, cvh ? 0 : 1, css, cse);

          // push merge action onto stack
          ss[sp] = css;
          se[sp] = cse;
          vh[sp] = cvh;
          mp[sp] = true;
          sp++;

          // divide, and push two halves onto stack
          int mid = (css + cse)/2;
          ss[sp] = css;
          se[sp] = mid;
          vh[sp] = !cvh;
          mp[sp] = false;
          sp++;
          ss[sp] = mid+1;
          se[sp] = cse;
          vh[sp] = !cvh;
          mp[sp] = false;
          sp++;
        }
        else {
          // connect step, also push hulls onto hull stack
          int[] hull;
          if (cse - css + 1 == 3) {
            hull = new int[3];
            hull[0] = indices[css];
            hull[1] = indices[css+1];
            hull[2] = indices[cse];
            float a0x = samp[0][hull[0]];
            float a0y = samp[1][hull[0]];
            if ( (samp[0][hull[1]]-a0x)*(samp[1][hull[2]]-a0y)
               - (samp[1][hull[1]]-a0y)*(samp[0][hull[2]]-a0x) > 0) {
              // adjust step, hull must remain clockwise
              hull[1] = indices[cse];
              hull[2] = indices[css+1];
            }
            tris[tp][0] = new int[1];
            tris[tp][1] = new int[1];
            tris[tp][2] = new int[1];
            tris[tp][0][0] = hull[0];
            tris[tp][1][0] = hull[1];
            tris[tp][2][0] = hull[2];
            tp++;
            ntris++;
            nverts[indices[css]]++;
            nverts[indices[cse]]++;
            nverts[indices[css+1]]++;
          }
          else {
            hull = new int[2];
            hull[0] = indices[css];
            hull[1] = indices[cse];
          }
          hs[hsp++] = hull;
        }
      }
      else {
        // merge step
        int coord = cvh ? 1 : 0;

        // pop hull arrays from stack
        int[] hull1, hull2;
        hsp -= 2;
        hull2 = cvh ? hs[hsp+1] : hs[hsp];
        hull1 = cvh ? hs[hsp] : hs[hsp+1];
        hs[hsp+1] = null;
        hs[hsp] = null;

        // find upper and lower convex hull additions
        int upp1 = 0;
        int upp2 = 0;
        int low1 = 0;
        int low2 = 0;

        // find initial upper and lower hull indices for later optimization
        for (int i=1; i<hull1.length; i++) {
          if (samp[coord][hull1[i]] < samp[coord][hull1[upp1]]) upp1 = i;
          if (samp[coord][hull1[i]] > samp[coord][hull1[low1]]) low1 = i;
        }
        for (int i=1; i<hull2.length; i++) {
          if (samp[coord][hull2[i]] < samp[coord][hull2[upp2]]) upp2 = i;
          if (samp[coord][hull2[i]] > samp[coord][hull2[low2]]) low2 = i;
        }

        // optimize upp1
        int bob = (upp1+1)%hull1.length;
        float ax = samp[0][hull2[upp2]];
        float ay = samp[1][hull2[upp2]];
        float bamx = samp[0][hull1[bob]] - ax;
        float bamy = samp[1][hull1[bob]] - ay;
        float camx = samp[0][hull1[upp1]] - ax;
        float camy = samp[1][hull1[upp1]] - ay;
        float u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                        : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
        float v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                        : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        boolean plus_dir = (u < v);
        if (!plus_dir) {
          bob = upp1;
          u = 0;
          v = 1;
        }
        while (u < v) {
          upp1 = bob;
          bob = plus_dir ? (upp1+1)%hull1.length
                         : (upp1+hull1.length-1)%hull1.length;
          bamx = samp[0][hull1[bob]] - ax;
          bamy = samp[1][hull1[bob]] - ay;
          camx = samp[0][hull1[upp1]] - ax;
          camy = samp[1][hull1[upp1]] - ay;
          u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                    : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
          v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                    : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        }

        // optimize upp2
        bob = (upp2+1)%hull2.length;
        ax = samp[0][hull1[upp1]];
        ay = samp[1][hull1[upp1]];
        bamx = samp[0][hull2[bob]] - ax;
        bamy = samp[1][hull2[bob]] - ay;
        camx = samp[0][hull2[upp2]] - ax;
        camy = samp[1][hull2[upp2]] - ay;
        u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                  : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
        v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                  : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        plus_dir = (u < v);
        if (!plus_dir) {
          bob = upp2;
          u = 0;
          v = 1;
        }
        while (u < v) {
          upp2 = bob;
          bob = plus_dir ? (upp2+1)%hull2.length
                         : (upp2+hull2.length-1)%hull2.length;
          bamx = samp[0][hull2[bob]] - ax;
          bamy = samp[1][hull2[bob]] - ay;
          camx = samp[0][hull2[upp2]] - ax;
          camy = samp[1][hull2[upp2]] - ay;
          u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                    : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
          v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                    : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        }

        // optimize low1
        bob = (low1+1)%hull1.length;
        ax = samp[0][hull2[low2]];
        ay = samp[1][hull2[low2]];
        bamx = samp[0][hull1[bob]] - ax;
        bamy = samp[1][hull1[bob]] - ay;
        camx = samp[0][hull1[low1]] - ax;
        camy = samp[1][hull1[low1]] - ay;
        u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                  : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
        v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                  : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        plus_dir = (u > v);
        if (!plus_dir) {
          bob = low1;
          u = 1;
          v = 0;
        }
        while (u > v) {
          low1 = bob;
          bob = plus_dir ? (low1+1)%hull1.length
                         : (low1+hull1.length-1)%hull1.length;
          bamx = samp[0][hull1[bob]] - ax;
          bamy = samp[1][hull1[bob]] - ay;
          camx = samp[0][hull1[low1]] - ax;
          camy = samp[1][hull1[low1]] - ay;
          u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                    : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
          v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                    : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        }

        // optimize low2
        bob = (low2+1)%hull2.length;
        ax = samp[0][hull1[low1]];
        ay = samp[1][hull1[low1]];
        bamx = samp[0][hull2[bob]] - ax;
        bamy = samp[1][hull2[bob]] - ay;
        camx = samp[0][hull2[low2]] - ax;
        camy = samp[1][hull2[low2]] - ay;
        u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                  : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
        v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                  : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        plus_dir = (u > v);
        if (!plus_dir) {
          bob = low2;
          u = 1;
          v = 0;
        }
        while (u > v) {
          low2 = bob;
          bob = plus_dir ? (low2+1)%hull2.length
                         : (low2+hull2.length-1)%hull2.length;
          bamx = samp[0][hull2[bob]] - ax;
          bamy = samp[1][hull2[bob]] - ay;
          camx = samp[0][hull2[low2]] - ax;
          camy = samp[1][hull2[low2]] - ay;
          u = (cvh) ? (float) (bamy/Math.sqrt(bamx*bamx + bamy*bamy))
                    : (float) (bamx/Math.sqrt(bamx*bamx + bamy*bamy));
          v = (cvh) ? (float) (camy/Math.sqrt(camx*camx + camy*camy))
                    : (float) (camx/Math.sqrt(camx*camx + camy*camy));
        }

        // calculate number of points in inner hull
        int nih1, nih2;
        int noh1, noh2;
        int h1ups, h2ups;
        if (low1 == upp1) {
          nih1 = hull1.length;
          noh1 = 1;
          h1ups = 0;
        }
        else {
          nih1 = low1-upp1+1;
          if (nih1 <= 0) nih1 += hull1.length;
          noh1 = hull1.length-nih1+2;
          h1ups = 1;
        }
        if (low2 == upp2) {
          nih2 = hull2.length;
          noh2 = 1;
          h2ups = 0;
        }
        else {
          nih2 = upp2-low2+1;
          if (nih2 <= 0) nih2 += hull2.length;
          noh2 = hull2.length-nih2+2;
          h2ups = 1;
        }

        // copy hull1 & hull2 info into merged hull array
        int[] hull = new int[noh1+noh2];
        int hullnum = 0;
        int spot;

        // go clockwise until upp1 is reached
        for (spot=low1; spot!=upp1; hullnum++, spot=(spot+1)%hull1.length) {
          hull[hullnum] = hull1[spot];
        }

        // append upp1
        hull[hullnum++] = hull1[upp1];

        // go clockwise until low2 is reached
        for (spot=upp2; spot!=low2; hullnum++, spot=(spot+1)%hull2.length) {
          hull[hullnum] = hull2[spot];
        }

        // append low2
        hull[hullnum++] = hull2[low2];

        // now push the new, completed hull onto the hull stack
        hs[hsp++] = hull;

        // stitch a connection between the two triangulations
        int base1 = low1;
        int base2 = low2;
        int oneUp1 = (base1+hull1.length-1)%hull1.length;
        int oneUp2 = (base2+1)%hull2.length;

        // when both sides reach the top the merge is complete
        int ntd = (noh1 == 1 || noh2 == 1) ? nih1+nih2-1 : nih1+nih2-2;
        tris[tp][0] = new int[ntd];
        tris[tp][1] = new int[ntd];
        tris[tp][2] = new int[ntd];
        for (int t=0; t<ntd; t++) {

          // special case if side 1 has reached the top
          if (h1ups == nih1) {
            oneUp2 = (base2+1)%hull2.length;
            tris[tp][0][t] = hull2[base2];
            tris[tp][1][t] = hull1[base1];
            tris[tp][2][t] = hull2[oneUp2];
            ntris++;
            nverts[hull1[base1]]++;
            nverts[hull2[base2]]++;
            nverts[hull2[oneUp2]]++;
            base2 = oneUp2;
            h2ups++;
          }

          // special case if side 2 has reached the top
          else if (h2ups == nih2) {
            oneUp1 = (base1+hull1.length-1)%hull1.length;
            tris[tp][0][t] = hull2[base2];
            tris[tp][1][t] = hull1[base1];
            tris[tp][2][t] = hull1[oneUp1];
            ntris++;
            nverts[hull1[base1]]++;
            nverts[hull2[base2]]++;
            nverts[hull1[oneUp1]]++;
            base1 = oneUp1;
            h1ups++;
          }

          // neither side has reached the top yet
          else {
            boolean b1o2diag;
            float Ax = samp[0][hull1[oneUp1]] - samp[0][hull1[base1]];
            float Ay = samp[1][hull1[oneUp1]] - samp[1][hull1[base1]];
            float Bx = samp[0][hull2[base2]] - samp[0][hull1[base1]];
            float By = samp[1][hull2[base2]] - samp[1][hull1[base1]];
            float Cx = samp[0][hull2[base2]] - samp[0][hull2[oneUp2]];
            float Cy = samp[1][hull2[base2]] - samp[1][hull2[oneUp2]];
            float Dx = samp[0][hull1[oneUp1]] - samp[0][hull2[oneUp2]];
            float Dy = samp[1][hull1[oneUp1]] - samp[1][hull2[oneUp2]];

            /* Check for greater than 180 degree angles.
               -------------------------------------------
               Note: base angles are checked first because
               ----  oneUp angles could be false alarms
                     (on an inside-out quadrilateral,
                      angles less than zero return
                      "positive" on a dot-product test).
               ------------------------------------------- */
            if (Ax*By - Ay*Bx > 0) b1o2diag = true;
            else if (Bx*Cy - By*Cx > 0) b1o2diag = false;
            else if (Cx*Dy - Cy*Dx > 0) b1o2diag = true;
            else if (Dx*Ay - Dy*Ax > 0) b1o2diag = false;

            // use slopes to find larger angle sum
            else b1o2diag = (Ax/Ay + By/Bx + Dy/Dx + Cx/Cy < 0);

            if (b1o2diag) {
              tris[tp][0][t] = hull2[base2];
              tris[tp][1][t] = hull1[base1];
              tris[tp][2][t] = hull2[oneUp2];
              ntris++;
              nverts[hull1[base1]]++;
              nverts[hull2[base2]]++;
              nverts[hull2[oneUp2]]++;

              // use diagonal (base1, oneUp2) as new base
              base2 = oneUp2;
              h2ups++;
              oneUp2 = (base2+1)%hull2.length;
            }
            else {
              tris[tp][0][t] = hull2[base2];
              tris[tp][1][t] = hull1[base1];
              tris[tp][2][t] = hull1[oneUp1];
              ntris++;
              nverts[hull1[base1]]++;
              nverts[hull2[base2]]++;
              nverts[hull1[oneUp1]]++;

              // use diagonal (base2, oneUp1) as new base
              base1 = oneUp1;
              h1ups++;
              oneUp1 = (base1+hull1.length-1)%hull1.length;
            }
          }
        }
        tp++;
      }
    }

    // build Tri component
    Tri = new int[ntris][3];
    int tr = 0;
    for (int i=0; i<tp; i++) {
      for (int j=0; j<tris[i][0].length; j++) {
        Tri[tr][0] = tris[i][0][j];
        Tri[tr][1] = tris[i][1][j];
        Tri[tr][2] = tris[i][2][j];
        tr++;
      }
    }

    // build Vertices component
    Vertices = new int[numpts][];
    for (int i=0; i<numpts; i++) {
      Vertices[i] = new int[nverts[i]];
      nverts[i] = 0;
    }
    int a, b, c;
    for (int i=0; i<ntris; i++) {
      a = Tri[i][0];
      b = Tri[i][1];
      c = Tri[i][2];
      Vertices[a][nverts[a]++] = i;
      Vertices[b][nverts[b]++] = i;
      Vertices[c][nverts[c]++] = i;
    }

    // call more generic method for constructing Walk and Edges arrays
    super.finish_triang(samples);
  }

}

