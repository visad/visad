//
// Contour2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

/**
   Contour2D is a class equipped with a 2-D contouring function.<P>
*/
public class Contour2D extends Applet implements MouseListener {

  // Applet variables
  protected Contour2D con;
  protected int whichlabels = 0;
  protected boolean showgrid;
  protected int rows, cols, scale;
  protected int[] num1, num2, num3, num4;
  protected float[][] vx1, vy1, vx2, vy2, vx3, vy3, vx4, vy4;

  /**
   * Compute contour lines for a 2-D array.  If the interval is negative,
   * then contour lines less than base will be drawn as dashed lines.
   * The contour lines will be computed for all V such that:<br>
   *           lowlimit <= V <= highlimit<br>
   *     and   V = base + n*interval  for some integer n<br>
   * Note that the input array, g, should be in column-major (FORTRAN) order.
   *
   * @param    g         the 2-D array to contour.
   * @param    nr        size of 2-D array in rows
   * @param    nc        size of 2-D array in columns.
   * @param    interval  contour interval
   * @param    lowlimit  the lower limit on values to contour.
   * @param    highlimit the upper limit on values to contour.
   * @param    base      base value to start contouring at.
   * @param    vx1       array to put contour line vertices (x value)
   * @param    vy1       array to put contour line vertices (y value)
   * @param    maxv1     size of vx1, vy1 arrays
   * @param    numv1     pointer to int to return number of vertices in vx1,vy1
   * @param    vx2       array to put 'hidden' contour line vertices (x value)
   * @param    vy2       array to put 'hidden' contour line vertices (y value)
   * @param    maxv2     size of vx2, vy2 arrays
   * @param    numv2     pointer to int to return number of vertices in vx2,vy2
   * @param    vx3       array to put contour label vertices (x value)
   * @param    vy3       array to put contour label vertices (y value)
   * @param    maxv3     size of vx3, vy3 arrays
   * @param    numv3     pointer to int to return number of vertices in vx3,vy3
   * @param    vx4       array to put contour label vertices, inverted (x value)
   * @param    vy4       array to put contour label vertices, inverted (y value)
   *                     <br>** see note for VxB and VyB in PlotDigits.java **
   * @param    maxv4     size of vx4, vy4 arrays
   * @param    numv4     pointer to int to return number of vertices in vx4,vy4
   */
  public static void contour( float g[], int nr, int nc, float interval,
                      float lowlimit, float highlimit, float base,
                      float vx1[][], float vy1[][],  int maxv1, int[] numv1,
                      float vx2[][], float vy2[][],  int maxv2, int[] numv2,
                      float vx3[][], float vy3[][],  int maxv3, int[] numv3,
                      float vx4[][], float vy4[][],  int maxv4, int[] numv4,
                      byte[][] auxValues, byte[][] auxLevels1,
                      byte[][] auxLevels2, byte[][] auxLevels3, boolean[] swap )
                          throws VisADException
  {
    boolean[] dashes = {false};
    float[] intervals =
      intervalToLevels(interval, lowlimit, highlimit, base, dashes);
    boolean dash = dashes[0];

    contour( g, nr, nc, intervals,
             lowlimit, highlimit, base, dash,
             vx1, vy1,  maxv1, numv1,
             vx2, vy2,  maxv2, numv2,
             vx3, vy3,  maxv3, numv3,
             vx4, vy4,  maxv4, numv4,
             auxValues, auxLevels1,
             auxLevels2, auxLevels3, swap );
  }

  /**
   * Returns an array of contour values and an indication on whether to use
   * dashed lines below the base value.
   *
   * @param interval		The contouring interval.  Must be non-zero.
   *				If the interval is negative, then contour lines
   *				less than the base will be drawn as dashed
   *				lines.  Must not be NaN.
   * @param low			The minimum contour value.  The returned array
   *				will not contain a value below this.  Must not
   *				be NaN.
   * @param high		The maximum contour value.  The returned array
   *				will not contain a value above this.  Must not
   *				be NaN.
   * @param ba			The base contour value.  The returned values
   *				will be integer multiples of the interval
   *				away from this this value. Must not be NaN.
   * dash			Whether or not contour lines less than the base
   *				should be drawn as dashed lines.  This is a
   *				computed and returned value.
   * @throws VisADException	The contour interval is zero or too small.
   */
  public static float[] intervalToLevels(float interval, float low,
                                         float high, float ba, boolean[] dash)
        throws VisADException {
    float clow, chi;
    float tmp1;
    float[] levs = null;

    if (interval == 0.0) {
      throw new VisADException("Contour interval cannot be zero");
    }

    dash[0] = false;
    if (interval < 0) {
        dash[0] = true;
        interval = -interval;
    }

    // compute list of contours
    // compute clow and chi, low and high contour values in the box
    tmp1 = (low - ba) / interval;
    clow = ba + interval * ((int) (tmp1 + (tmp1 >= 0 ? 0.5 : -0.5)) - 1);
    while (clow<low) {
      clow += interval;
    }

    tmp1 = (high - ba) / interval;
    chi = ba + interval * ((int) (tmp1 + (tmp1 >= 0 ? 0.5 : -0.5)) + 1);
    while (chi>high) {
      chi -= interval;
    }

    // how many contour lines are needed.
    tmp1 = (chi-clow) / interval;
    int numc = (int) (tmp1 + (tmp1 >= 0 ? 0.5 : -0.5)) + 1;
/*
    System.out.println("clow = " + clow + "chigh = " + chi +
                       "tmp1 = " + tmp1 + "numc = " + numc);
*/
    if (numc < 1) return levs;

    if (numc > 1000) {
      throw new VisADException("Contour interval too small");
    }

    try {
      levs = new float[numc];
    } catch (OutOfMemoryError e) {
      throw new VisADException("Contour interval too small");
    }

    levs[0] = clow;
    for (int i = 1; i < numc; i++) {
      levs[i] = levs[i-1] + interval;
    }
    return levs;
  }

  /**
   * Compute contour lines for a 2-D array.  If the interval is negative,
   * then contour lines less than base will be drawn as dashed lines.
   * The contour lines will be computed for all V such that:<br>
   *           lowlimit <= V <= highlimit<br>
   *     and   V = base + n*interval  for some integer n<br>
   * Note that the input array, g, should be in column-major (FORTRAN) order.
   *
   * @param    g         the 2-D array to contour.
   * @param    nr        size of 2-D array in rows
   * @param    nc        size of 2-D array in columns.
   * @param    values    the values to be plotted
   * @param    lowlimit  the lower limit on values to contour.
   * @param    highlimit the upper limit on values to contour.
   * @param    base      base value to start contouring at.
   * @param    dash      boolean to dash contours below base or not
   * @param    vx1       array to put contour line vertices (x value)
   * @param    vy1       array to put contour line vertices (y value)
   * @param    maxv1     size of vx1, vy1 arrays
   * @param    numv1     pointer to int to return number of vertices in vx1,vy1
   * @param    vx2       array to put 'hidden' contour line vertices (x value)
   * @param    vy2       array to put 'hidden' contour line vertices (y value)
   * @param    maxv2     size of vx2, vy2 arrays
   * @param    numv2     pointer to int to return number of vertices in vx2,vy2
   * @param    vx3       array to put contour label vertices (x value)
   * @param    vy3       array to put contour label vertices (y value)
   * @param    maxv3     size of vx3, vy3 arrays
   * @param    numv3     pointer to int to return number of vertices in vx3,vy3
   * @param    vx4       array to put contour label vertices, inverted (x value)
   * @param    vy4       array to put contour label vertices, inverted (y value)
   *                     <br>** see note for VxB and VyB in PlotDigits.java **
   * @param    maxv4     size of vx4, vy4 arrays
   * @param    numv4     pointer to int to return number of vertices in vx4,vy4
   */
  public static void contour( float g[], int nr, int nc, float[] values,
                      float lowlimit, float highlimit, float base, boolean dash,
                      float vx1[][], float vy1[][],  int maxv1, int[] numv1,
                      float vx2[][], float vy2[][],  int maxv2, int[] numv2,
                      float vx3[][], float vy3[][],  int maxv3, int[] numv3,
                      float vx4[][], float vy4[][],  int maxv4, int[] numv4,
                      byte[][] auxValues, byte[][] auxLevels1,
                      byte[][] auxLevels2, byte[][] auxLevels3, boolean[] swap )
                          throws VisADException {
/*
System.out.println("interval = " + values[0] + " lowlimit = " + lowlimit +
                   " highlimit = " + highlimit + " base = " + base);
boolean any = false;
boolean anymissing = false;
boolean anynotmissing = false;
*/

// System.out.println("contour: swap = " + swap[0] + " " + swap[1] + " " + swap[2]);

    PlotDigits plot = new PlotDigits();
    int ir, ic;
    int nrm, ncm;
    int numc, il;
    int lr, lc, lc2, lrr, lr2, lcc;
    float xd, yd ,xx, yy;
    float xdd, ydd;
//  float clow, chi;
    float gg;
    int maxsize = maxv1+maxv2;
    float[] vx = new float[maxsize];
    float[] vy = new float[maxsize];

    // WLH 21 April 2000
    // int[] ipnt = new int[2*maxsize];
    int[] ipnt = new int[nr*nc+4];

    int nump, ip;
    int numv;

/* DRM 1999-05-18, CTR 29 Jul 1999: values could be null */
    float[] myvals = null;
    if (values != null) {
      myvals = (float[]) values.clone();
      java.util.Arrays.sort(myvals);
    }
    int low;
    int hi;

    int t;

    byte[][] auxLevels = null;
    int naux = (auxValues != null) ? auxValues.length : 0;
    if (naux > 0) {
      if (auxLevels1 == null || auxLevels1.length != naux ||
          auxLevels2 == null || auxLevels2.length != naux ||
          auxLevels3 == null || auxLevels3.length != naux) {
        throw new SetException("Contour2D.contour: "
                              +"auxLevels length doesn't match");
      }
      for (int i=0; i<naux; i++) {
        if (auxValues[i].length != g.length) {
          throw new SetException("Contour2D.contour: "
                                +"auxValues lengths don't match");
        }
      }
      auxLevels = new byte[naux][maxsize];
    }
    else {
      if (auxLevels1 != null || auxLevels2 != null || auxLevels3 != null) {
        throw new SetException("Contour2D.contour: "
                              +"auxValues null but auxLevels not null");
      }
    }

    // initialize vertex counts
    numv1[0] = 0;
    numv2[0] = 0;
    numv3[0] = 0;
    numv4[0] = 0;

    if (values == null) return; // WLH 24 Aug 99

/*  DRM: 1999-05-19 - Not needed since dash is a boolean
    // check for bad contour interval
    if (interval==0.0) {
      throw new DisplayException("Contour2D.contour: interval cannot be 0");
    }
    if (!dash) {
      // draw negative contour lines as dashed lines
      interval = -interval;
      idash = 1;
    }
    else {
      idash = 0;
    }
*/

    nrm = nr-1;
    ncm = nc-1;

    xdd = ((nr-1)-0.0f)/(nr-1.0f); // = 1.0
    ydd = ((nc-1)-0.0f)/(nc-1.0f); // = 1.0
    xd = xdd - 0.0001f;
    yd = ydd - 0.0001f;

    /*
     * set up mark array
     * mark= 0 if avail for label center,
     *       2 if in label, and
     *       1 if not available and not in label
     *
     * lr and lc give label size in grid boxes
     * lrr and lcc give unavailable radius
     */
    if (swap[0]) {
      lr = 1+(nr-2)/10;
      lc = 1+(nc-2)/50;
    }
    else {
      lr = 1+(nr-2)/50;
      lc = 1+(nc-2)/10;
    }
    lc2 = lc/2;
    lr2 = lr/2;
    lrr = 1+(nr-2)/8;
    lcc = 1+(nc-2)/8;

    // allocate mark array
    char[] mark = new char[nr * nc];

    // initialize mark array to zeros
    for (int i=0; i<nr * nc; i++) mark[i] = 0;

    // set top and bottom rows to 1
    for (ic=0;ic<nc;ic++) {
      for (ir=0;ir<lr;ir++) {
        mark[ (ic) * nr + (ir) ] = 1;
        mark[ (ic) * nr + (nr-ir-2) ] = 1;
      }
    }

    // set left and right columns to 1
    for (ir=0;ir<nr;ir++) {
      for (ic=0;ic<lc;ic++) {
         mark[ (ic) * nr + (ir) ] = 1;
         mark[ (nc-ic-2) * nr + (ir) ] = 1;
      }
    }
    numv = nump = 0;

    // compute contours
    for (ir=0; ir<nrm; ir++) {
      xx = xdd*ir+0.0f; // = ir
      for (ic=0; ic<ncm; ic++) {
        float ga, gb, gc, gd;
        float gv, gn, gx;
        float tmp1, tmp2;

        // WLH 21 April 2000
        // if (numv+8 >= maxsize || nump+4 >= 2*maxsize) {
        if (numv+8 >= maxsize) {
          // allocate more space
          maxsize = 2 * maxsize;
/* WLH 21 April 2000
          int[] tt = ipnt;
          ipnt = new int[2 * maxsize];
          System.arraycopy(tt, 0, ipnt, 0, nump);
*/
          float[] tx = vx;
          float[] ty = vy;
          vx = new float[maxsize];
          vy = new float[maxsize];
          System.arraycopy(tx, 0, vx, 0, numv);
          System.arraycopy(ty, 0, vy, 0, numv);
          if (naux > 0) {
            byte[][] ta = auxLevels;
            auxLevels = new byte[naux][maxsize];
            for (int i=0; i<naux; i++) {
              System.arraycopy(ta[i], 0, auxLevels[i], 0, numv);
            }
          }
        }

        // save index of first vertex in this grid box
        ipnt[nump++] = numv;

        yy = ydd*ic+0.0f; // = ic
/*
ga = ( g[ (ic) * nr + (ir) ] );
gb = ( g[ (ic) * nr + (ir+1) ] );
gc = ( g[ (ic+1) * nr + (ir) ] );
gd = ( g[ (ic+1) * nr + (ir+1) ] );
boolean miss = false;
if (ga != ga || gb != gb || gc != gc || gd != gd) {
  miss = true;
  System.out.println("ic, ir = " + ic + "  " + ir + " gabcd = " +
                     ga + " " + gb + " " + gc + " " + gd);
}
*/
/*
if (ga != ga || gb != gb || gc != gc || gd != gd) {
  if (!anymissing) {
    anymissing = true;
    System.out.println("missing");
  }
}
else {
  if (!anynotmissing) {
    anynotmissing = true;
    System.out.println("notmissing");
  }
}
*/
        // get 4 corner values, skip box if any are missing
        ga = ( g[ (ic) * nr + (ir) ] );
        // test for missing
        if (ga != ga) continue;
        gb = ( g[ (ic) * nr + (ir+1) ] );
        // test for missing
        if (gb != gb) continue;
        gc = ( g[ (ic+1) * nr + (ir) ] );
        // test for missing
        if (gc != gc) continue;
        gd = ( g[ (ic+1) * nr + (ir+1) ] );
        // test for missing
        if (gd != gd) continue;

        byte[] auxa = null;
        byte[] auxb = null;
        byte[] auxc = null;
        byte[] auxd = null;
        if (naux > 0) {
          auxa = new byte[naux];
          auxb = new byte[naux];
          auxc = new byte[naux];
          auxd = new byte[naux];
          for (int i=0; i<naux; i++) {
            auxa[i] = auxValues[i][(ic) * nr + (ir)];
            auxb[i] = auxValues[i][(ic) * nr + (ir+1)];
            auxc[i] = auxValues[i][(ic+1) * nr + (ir)];
            auxd[i] = auxValues[i][(ic+1) * nr + (ir+1)];
          }
        }

        // find average, min, and max of 4 corner values
        gv = (ga+gb+gc+gd)/4.0f;

        // gn = MIN4(ga,gb,gc,gd);
        tmp1 = ( (ga) < (gb) ? (ga) : (gb) );
        tmp2 = ( (gc) < (gd) ? (gc) : (gd) );
        gn = ( (tmp1) < (tmp2) ? (tmp1) : (tmp2) );

        // gx = MAX4(ga,gb,gc,gd);
        tmp1 = ( (ga) > (gb) ? (ga) : (gb) );
        tmp2 = ( (gc) > (gd) ? (gc) : (gd) );
        gx = ( (tmp1) > (tmp2) ? (tmp1) : (tmp2) );

/*  remove for new signature, replace with code below
        // compute clow and chi, low and high contour values in the box
        tmp1 = (gn-base) / interval;
        clow = base + interval * (( (tmp1) >= 0 ? (int) ((tmp1) + 0.5)
                                                : (int) ((tmp1)-0.5) )-1);
        while (clow<gn) {
          clow += interval;
        }

        tmp1 = (gx-base) / interval;
        chi = base + interval * (( (tmp1) >= 0 ? (int) ((tmp1) + 0.5)
                                               : (int) ((tmp1)-0.5) )+1);
        while (chi>gx) {
          chi -= interval;
        }

        // how many contour lines in the box:
        tmp1 = (chi-clow) / interval;
        numc = 1+( (tmp1) >= 0 ? (int) ((tmp1) + 0.5) : (int) ((tmp1)-0.5) );

        // gg is current contour line value
        gg = clow;
*/

        low = 0;
        hi = myvals.length - 1;
        if (myvals[low] > gx || myvals[hi] < gn) // no contours
        {
            numc = 1;
        }
        else   // some inside the box
        {
            for (int i = 0; i < myvals.length; i++)
            {
                 if (i == 0 && myvals[i] >= gn) { low = i; }
                 else if (myvals[i] >= gn && myvals[i-1] < gn) { low = i; }
                 else if (myvals[i] > gx && myvals[i-1] < gn) { hi = i; }
            }
            numc = hi - low + 1;
        }
        gg = myvals[low];

/*
if (!any && numc > 0) {
  System.out.println("numc = " + numc + " clow = " + myvals[low] +
                     " chi = " + myvals[hi]);
  any = true;
}
*/

        for (il=0; il<numc; il++) {

          gg = myvals[low+il];

          // WLH 21 April 2000
          // if (numv+8 >= maxsize || nump+4 >= 2*maxsize) {
          if (numv+8 >= maxsize) {
            // allocate more space
            maxsize = 2 * maxsize;
/* WLH 21 April 2000
            int[] tt = ipnt;
            ipnt = new int[2 * maxsize];
            System.arraycopy(tt, 0, ipnt, 0, nump);
*/
            float[] tx = vx;
            float[] ty = vy;
            vx = new float[maxsize];
            vy = new float[maxsize];
            System.arraycopy(tx, 0, vx, 0, numv);
            System.arraycopy(ty, 0, vy, 0, numv);
            if (naux > 0) {
              byte[][] ta = auxLevels;
              auxLevels = new byte[naux][maxsize];
              for (int i=0; i<naux; i++) {
                System.arraycopy(ta[i], 0, auxLevels[i], 0, numv);
              }
            }
          }

          float gba, gca, gdb, gdc;
          int ii;

          // make sure gg is within contouring limits
          if (gg < gn) continue;
          if (gg > gx) break;
          if (gg < lowlimit) continue;
          if (gg > highlimit) break;

          // compute orientation of lines inside box
          ii = 0;
          if (gg > ga) ii = 1;
          if (gg > gb) ii += 2;
          if (gg > gc) ii += 4;
          if (gg > gd) ii += 8;
          if (ii > 7) ii = 15 - ii;
          if (ii <= 0) continue;

          // DO LABEL HERE
          if (( mark[ (ic) * nr + (ir) ] )==0) {
            int kc, kr, mc, mr, jc, jr;
            float xk, yk, xm, ym, value;

            // Insert a label

            // BOX TO AVOID
            kc = ic-lc2-lcc;
            kr = ir-lr2-lrr;
            mc = kc+2*lcc+lc-1;
            mr = kr+2*lrr+lr-1;
            // OK here
            for (jc=kc;jc<=mc;jc++) {
              if (jc >= 0 && jc < nc) {
                for (jr=kr;jr<=mr;jr++) {
                  if (jr >= 0 && jr < nr) {
                    if (( mark[ (jc) * nr + (jr) ] ) != 2) {
                      mark[ (jc) * nr + (jr) ] = 1;
                    }
                  }
                }
              }
            }

            // BOX TO HOLD LABEL
            kc = ic-lc2;
            kr = ir-lr2;
            mc = kc+lc-1;
            mr = kr+lr-1;
            for (jc=kc;jc<=mc;jc++) {
              if (jc >= 0 && jc < nc) {
                for (jr=kr;jr<=mr;jr++) {
                  if (jr >= 0 && jr < nr) {
                    mark[ (jc) * nr + (jr) ] = 2;
                  }
                }
              }
            }

            xk = xdd*kr+0.0f;
            yk = ydd*kc+0.0f;
            xm = xdd*(mr+1.0f)+0.0f;
            ym = ydd*(mc+1.0f)+0.0f;
            value = gg;

            if (numv4[0]+100 >= maxv4) {
              // allocate more space
              maxv4 = 2 * (numv4[0]+100);
              float[][] tx = new float[][] {vx4[0]};
              float[][] ty = new float[][] {vy4[0]};
              vx4[0] = new float[maxv4];
              vy4[0] = new float[maxv4];
              System.arraycopy(tx[0], 0, vx4[0], 0, numv4[0]);
              System.arraycopy(ty[0], 0, vy4[0], 0, numv4[0]);
            }

            if (numv3[0]+100 >= maxv3) {
              // allocate more space
              maxv3 = 2 * (numv3[0]+100);
              float[][] tx = new float[][] {vx3[0]};
              float[][] ty = new float[][] {vy3[0]};
              vx3[0] = new float[maxv3];
              vy3[0] = new float[maxv3];
              System.arraycopy(tx[0], 0, vx3[0], 0, numv3[0]);
              System.arraycopy(ty[0], 0, vy3[0], 0, numv3[0]);
              if (naux > 0) {
                byte[][] ta = auxLevels3;
                for (int i=0; i<naux; i++) {
                  byte[] taa = auxLevels3[i];
                  auxLevels3[i] = new byte[maxv3];
                  System.arraycopy(taa, 0, auxLevels3[i], 0, numv3[0]);
                }
              }
            }

            plot.plotdigits( value, xk, yk, xm, ym, maxsize, swap);
            System.arraycopy(plot.Vx, 0, vx3[0], numv3[0], plot.NumVerts);
            System.arraycopy(plot.Vy, 0, vy3[0], numv3[0], plot.NumVerts);
            if (naux > 0) {
              for (int i=0; i<naux; i++) {
                for (int j=numv3[0]; j<numv3[0]+plot.NumVerts; j++) {
                  auxLevels3[i][j] = auxa[i];
                }
              }
            }
            numv3[0] += plot.NumVerts;
            System.arraycopy(plot.VxB, 0, vx4[0], numv4[0], plot.NumVerts);
            System.arraycopy(plot.VyB, 0, vy4[0], numv4[0], plot.NumVerts);
            numv4[0] += plot.NumVerts;
          }

          switch (ii) {
            case 1:
              gba = gb-ga;
              gca = gc-ga;

              if (naux > 0) {
                float ratioba = (gg-ga)/gba;
                float ratioca = (gg-ga)/gca;
                for (int i=0; i<naux; i++) {
                  t = (int) ( (1.0f - ratioba) * ((auxa[i] < 0) ?
                        ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                      ratioba * ((auxb[i] < 0) ?
                        ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) );
                  auxLevels[i][numv] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
                  t = (int) ( (1.0f - ratioca) * ((auxa[i] < 0) ?
                        ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                      ratioca * ((auxc[i] < 0) ?
                        ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) );
                  auxLevels[i][numv+1] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv] = auxa[i] + (auxb[i]-auxa[i]) * ratioba;
                  auxLevels[i][numv+1] = auxa[i] + (auxc[i]-auxa[i]) * ratioca;
*/
                }
              }

              if (( (gba) < 0 ? -(gba) : (gba) ) < 0.0000001) {
                vx[numv] = xx;
              }
              else {
                vx[numv] = xx+xd*(gg-ga)/gba;
              }
              vy[numv] = yy;
              numv++;
              if (( (gca) < 0 ? -(gca) : (gca) ) < 0.0000001) {
                 vy[numv] = yy;
              }
              else {
                vy[numv] = yy+yd*(gg-ga)/gca;
              }
              vx[numv] = xx;
              numv++;
              break;

            case 2:
              gba = gb-ga;
              gdb = gd-gb;

              if (naux > 0) {
                float ratioba = (gg-ga)/gba;
                float ratiodb = (gg-gb)/gdb;
                for (int i=0; i<naux; i++) {
                  t = (int) ( (1.0f - ratioba) * ((auxa[i] < 0) ?
                        ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                      ratioba * ((auxb[i] < 0) ?
                        ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) );
                  auxLevels[i][numv] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
                  t = (int) ( (1.0f - ratiodb) * ((auxb[i] < 0) ?
                        ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) +
                      ratiodb * ((auxd[i] < 0) ?
                        ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                  auxLevels[i][numv+1] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv] = auxa[i] + (auxb[i]-auxa[i]) * ratioba;
                  auxLevels[i][numv+1] = auxb[i] + (auxd[i]-auxb[i]) * ratiodb;
*/
                }
              }

              if (( (gba) < 0 ? -(gba) : (gba) ) < 0.0000001)
                vx[numv] = xx;
              else
                vx[numv] = xx+xd*(gg-ga)/gba;
              vy[numv] = yy;
              numv++;
              if (( (gdb) < 0 ? -(gdb) : (gdb) ) < 0.0000001)
                vy[numv] = yy;
              else
                vy[numv] = yy+yd*(gg-gb)/gdb;
              vx[numv] = xx+xd;
              numv++;
              break;

            case 3:
              gca = gc-ga;
              gdb = gd-gb;

              if (naux > 0) {
                float ratioca = (gg-ga)/gca;
                float ratiodb = (gg-gb)/gdb;
                for (int i=0; i<naux; i++) {
                  t = (int) ( (1.0f - ratioca) * ((auxa[i] < 0) ?
                        ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                      ratioca * ((auxc[i] < 0) ?
                        ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) );
                  auxLevels[i][numv] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
                  t = (int) ( (1.0f - ratiodb) * ((auxb[i] < 0) ?
                        ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) +
                      ratiodb * ((auxd[i] < 0) ?
                        ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                  auxLevels[i][numv+1] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv] = auxa[i] + (auxc[i]-auxa[i]) * ratioca;
                  auxLevels[i][numv+1] = auxb[i] + (auxd[i]-auxb[i]) * ratiodb;
*/
                }
              }

              if (( (gca) < 0 ? -(gca) : (gca) ) < 0.0000001)
                vy[numv] = yy;
              else
                vy[numv] = yy+yd*(gg-ga)/gca;
              vx[numv] = xx;
              numv++;
              if (( (gdb) < 0 ? -(gdb) : (gdb) ) < 0.0000001)
                vy[numv] = yy;
              else
                vy[numv] = yy+yd*(gg-gb)/gdb;
              vx[numv] = xx+xd;
              numv++;
              break;

            case 4:
              gca = gc-ga;
              gdc = gd-gc;

              if (naux > 0) {
                float ratioca = (gg-ga)/gca;
                float ratiodc = (gg-gc)/gdc;
                for (int i=0; i<naux; i++) {
                  t = (int) ( (1.0f - ratioca) * ((auxa[i] < 0) ?
                        ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                      ratioca * ((auxc[i] < 0) ?
                        ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) );
                  auxLevels[i][numv] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
                  t = (int) ( (1.0f - ratiodc) * ((auxc[i] < 0) ?
                        ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) +
                      ratiodc * ((auxd[i] < 0) ?
                        ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                  auxLevels[i][numv+1] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv] = auxa[i] + (auxc[i]-auxa[i]) * ratioca;
                  auxLevels[i][numv+1] = auxc[i] + (auxd[i]-auxc[i]) * ratiodc;
*/
                }
              }

              if (( (gca) < 0 ? -(gca) : (gca) ) < 0.0000001)
                vy[numv] = yy;
              else
                vy[numv] = yy+yd*(gg-ga)/gca;
              vx[numv] = xx;
              numv++;
              if (( (gdc) < 0 ? -(gdc) : (gdc) ) < 0.0000001)
                vx[numv] = xx;
              else
                vx[numv] = xx+xd*(gg-gc)/gdc;
              vy[numv] = yy+yd;
              numv++;
              break;

            case 5:
              gba = gb-ga;
              gdc = gd-gc;

              if (naux > 0) {
                float ratioba = (gg-ga)/gba;
                float ratiodc = (gg-gc)/gdc;
                for (int i=0; i<naux; i++) {
                  t = (int) ( (1.0f - ratioba) * ((auxa[i] < 0) ?
                        ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                      ratioba * ((auxb[i] < 0) ?
                        ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) );
                  auxLevels[i][numv] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
                  t = (int) ( (1.0f - ratiodc) * ((auxc[i] < 0) ?
                        ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) +
                      ratiodc * ((auxd[i] < 0) ?
                        ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                  auxLevels[i][numv+1] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv] = auxa[i] + (auxb[i]-auxa[i]) * ratioba;
                  auxLevels[i][numv+1] = auxc[i] + (auxd[i]-auxc[i]) * ratiodc;
*/
                }
              }

              if (( (gba) < 0 ? -(gba) : (gba) ) < 0.0000001)
                vx[numv] = xx;
              else
                vx[numv] = xx+xd*(gg-ga)/gba;
              vy[numv] = yy;
              numv++;
              if (( (gdc) < 0 ? -(gdc) : (gdc) ) < 0.0000001)
                vx[numv] = xx;
              else
                vx[numv] = xx+xd*(gg-gc)/gdc;
              vy[numv] = yy+yd;
              numv++;
              break;

            case 6:
              gba = gb-ga;
              gdc = gd-gc;
              gca = gc-ga;
              gdb = gd-gb;

              if (naux > 0) {
                float ratioba = (gg-ga)/gba;
                float ratiodc = (gg-gc)/gdc;
                float ratioca = (gg-ga)/gca;
                float ratiodb = (gg-gb)/gdb;
                for (int i=0; i<naux; i++) {
                  t = (int) ( (1.0f - ratioba) * ((auxa[i] < 0) ?
                        ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                      ratioba * ((auxb[i] < 0) ?
                        ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) );
                  auxLevels[i][numv] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv] = auxa[i] + (auxb[i]-auxa[i]) * ratioba;
*/
                  if ( (gg>gv) ^ (ga<gb) ) {
                    t = (int) ( (1.0f - ratioca) * ((auxa[i] < 0) ?
                          ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                        ratioca * ((auxc[i] < 0) ?
                          ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) );
                    auxLevels[i][numv+1] = (byte)
                      ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256)));
                    t = (int) ( (1.0f - ratiodb) * ((auxb[i] < 0) ?
                          ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) +
                        ratiodb * ((auxd[i] < 0) ?
                          ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                    auxLevels[i][numv+2] = (byte)
                      ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256)));
/* MEM_WLH
                    auxLevels[i][numv+1] = auxa[i] + (auxc[i]-auxa[i]) * ratioca;
                    auxLevels[i][numv+2] = auxb[i] + (auxd[i]-auxb[i]) * ratiodb;
*/
                  }
                  else {
                    t = (int) ( (1.0f - ratiodb) * ((auxb[i] < 0) ?
                          ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) +
                        ratiodb * ((auxd[i] < 0) ?
                          ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                    auxLevels[i][numv+1] = (byte)
                      ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256)));
                    t = (int) ( (1.0f - ratioca) * ((auxa[i] < 0) ?
                          ((float) auxa[i]) + 256.0f : ((float) auxa[i]) ) +
                        ratioca * ((auxc[i] < 0) ?
                          ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) );
                    auxLevels[i][numv+2] = (byte)
                      ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256)));
/* MEM_WLH
                    auxLevels[i][numv+1] = auxb[i] + (auxd[i]-auxb[i]) * ratiodb;
                    auxLevels[i][numv+2] = auxa[i] + (auxc[i]-auxa[i]) * ratioca;
*/
                  }
                  t = (int) ( (1.0f - ratiodc) * ((auxc[i] < 0) ?
                        ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) +
                      ratiodc * ((auxd[i] < 0) ?
                        ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                  auxLevels[i][numv+3] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv+3] = auxc[i] + (auxd[i]-auxc[i]) * ratiodc;
*/
                }
              }

              if (( (gba) < 0 ? -(gba) : (gba) ) < 0.0000001)
                vx[numv] = xx;
              else
                vx[numv] = xx+xd*(gg-ga)/gba;
              vy[numv] = yy;
              numv++;
              // here's a brain teaser
              if ( (gg>gv) ^ (ga<gb) ) {  // (XOR)
                if (( (gca) < 0 ? -(gca) : (gca) ) < 0.0000001)
                  vy[numv] = yy;
                else
                  vy[numv] = yy+yd*(gg-ga)/gca;
                vx[numv] = xx;
                numv++;
                if (( (gdb) < 0 ? -(gdb) : (gdb) ) < 0.0000001)
                  vy[numv] = yy;
                else
                  vy[numv] = yy+yd*(gg-gb)/gdb;
                vx[numv] = xx+xd;
                numv++;
              }
              else {
                if (( (gdb) < 0 ? -(gdb) : (gdb) ) < 0.0000001)
                  vy[numv] = yy;
                else
                  vy[numv] = yy+yd*(gg-gb)/gdb;
                vx[numv] = xx+xd;
                numv++;
                if (( (gca) < 0 ? -(gca) : (gca) ) < 0.0000001)
                  vy[numv] = yy;
                else
                  vy[numv] = yy+yd*(gg-ga)/gca;
                vx[numv] = xx;
                numv++;
              }
              if (( (gdc) < 0 ? -(gdc) : (gdc) ) < 0.0000001)
                vx[numv] = xx;
              else
                vx[numv] = xx+xd*(gg-gc)/gdc;
              vy[numv] = yy+yd;
              numv++;
              break;

            case 7:
              gdb = gd-gb;
              gdc = gd-gc;

              if (naux > 0) {
                float ratiodb = (gg-gb)/gdb;
                float ratiodc = (gg-gc)/gdc;
                for (int i=0; i<naux; i++) {
                  t = (int) ( (1.0f - ratiodb) * ((auxb[i] < 0) ?
                        ((float) auxb[i]) + 256.0f : ((float) auxb[i]) ) +
                      ratiodb * ((auxd[i] < 0) ?
                        ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                  auxLevels[i][numv] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
                  t = (int) ( (1.0f - ratiodc) * ((auxc[i] < 0) ?
                        ((float) auxc[i]) + 256.0f : ((float) auxc[i]) ) +
                      ratiodc * ((auxd[i] < 0) ?
                        ((float) auxd[i]) + 256.0f : ((float) auxd[i]) ) );
                  auxLevels[i][numv+1] = (byte)
                    ( (t < 0) ? 0 : ((t > 255) ? -1 : ((t < 128) ? t : t - 256) ) );
/* MEM_WLH
                  auxLevels[i][numv] = auxb[i] + (auxb[i]-auxb[i]) * ratiodb;
                  auxLevels[i][numv+1] = auxc[i] + (auxd[i]-auxc[i]) * ratiodc;
*/
                }
              }

              if (( (gdb) < 0 ? -(gdb) : (gdb) ) < 0.0000001)
                vy[numv] = yy;
              else
                vy[numv] = yy+yd*(gg-gb)/gdb;
              vx[numv] = xx+xd;
              numv++;
              if (( (gdc) < 0 ? -(gdc) : (gdc) ) < 0.0000001)
                vx[numv] = xx;
              else
                vx[numv] = xx+xd*(gg-gc)/gdc;
              vy[numv] = yy+yd;
              numv++;
              break;

          } // switch

          // If contour level is negative, make dashed line
          if (gg < base && dash) {           /* DRM: 1999-05-19 */
            float vxa, vya, vxb, vyb;
            vxa = vx[numv-2];
            vya = vy[numv-2];
            vxb = vx[numv-1];
            vyb = vy[numv-1];
            vx[numv-2] = (3.0f*vxa+vxb) * 0.25f;
            vy[numv-2] = (3.0f*vya+vyb) * 0.25f;
            vx[numv-1] = (vxa+3.0f*vxb) * 0.25f;
            vy[numv-1] = (vya+3.0f*vyb) * 0.25f;
          }
/*
if ((20.0 <= vy[numv-2] && vy[numv-2] < 22.0) ||
    (20.0 <= vy[numv-1] && vy[numv-1] < 22.0)) {
  System.out.println("vy = " + vy[numv-1] + " " + vy[numv-2] +
                     " ic, ir = " + ic + " " + ir);
}
*/
        }  // for il       -- NOTE:  gg incremented in for statement
      }  // for ic
    }  // for ir

    ipnt[nump] = numv;

    // copy vertices from vx, vy arrays to either v1 or v2 arrays
    ip = 0;
    for (ir=0;ir<nrm && ip<ipnt.length-1;ir++) {
      for (ic=0;ic<ncm && ip<ipnt.length-1;ic++) {
        int start, len;
        start = ipnt[ip];
        len = ipnt[ip+1] - start;
        if (len>0) {
          if (( mark[ (ic) * nr + (ir) ] )==2) {

            if (numv2[0]+len >= maxv2) {
              // allocate more space
              maxv2 = 2 * (numv2[0]+len);
              float[][] tx = new float[][] {vx2[0]};
              float[][] ty = new float[][] {vy2[0]};
              vx2[0] = new float[maxv2];
              vy2[0] = new float[maxv2];
              System.arraycopy(tx[0], 0, vx2[0], 0, numv2[0]);
              System.arraycopy(ty[0], 0, vy2[0], 0, numv2[0]);
              if (naux > 0) {
                for (int i=0; i<naux; i++) {
                  byte[] ta = auxLevels2[i];
                  auxLevels2[i] = new byte[maxv2];
                  System.arraycopy(ta, 0, auxLevels2[i], 0, numv2[0]);
                }
              }
            }

            for (il=0;il<len;il++) {
              vx2[0][numv2[0]+il] = vx[start+il];
              vy2[0][numv2[0]+il] = vy[start+il];
            }
            if (naux > 0) {
              for (int i=0; i<naux; i++) {
                for (il=0;il<len;il++) {
                  auxLevels2[i][numv2[0]+il] = auxLevels[i][start+il];
                }
              }
            }
            numv2[0] += len;
          }
          else {

            if (numv1[0]+len >= maxv1) {
              // allocate more space
              maxv1 = 2 * (numv1[0]+len);
              float[][] tx = new float[][] {vx1[0]};
              float[][] ty = new float[][] {vy1[0]};
              vx1[0] = new float[maxv1];
              vy1[0] = new float[maxv1];
              System.arraycopy(tx[0], 0, vx1[0], 0, numv1[0]);
              System.arraycopy(ty[0], 0, vy1[0], 0, numv1[0]);
              if (naux > 0) {
                for (int i=0; i<naux; i++) {
                  byte[] ta = auxLevels1[i];
                  auxLevels1[i] = new byte[maxv1];
                  System.arraycopy(ta, 0, auxLevels1[i], 0, numv1[0]);
                }
              }
            }

            for (il=0; il<len; il++) {
              vx1[0][numv1[0]+il] = vx[start+il];
              vy1[0][numv1[0]+il] = vy[start+il];
            }
            if (naux > 0) {
              for (int i=0; i<naux; i++) {
                for (il=0;il<len;il++) {
                  auxLevels1[i][numv1[0]+il] = auxLevels[i][start+il];
                }
              }
            }
            numv1[0] += len;
          }

        }
        ip++;
      }
    }
  }

  // APPLET SECTION

  /* run 'appletviewer contour.html' to test the Contour2D class. */
  public void init() {
    this.addMouseListener(this);
    con = new Contour2D();
    con.rows = 0;
    con.cols = 0;
    con.scale = 0;
    float intv = 0;
    int mxv1 = 0;
    int mxv2 = 0;
    int mxv3 = 0;
    int mxv4 = 0;
    try {
      String temp = new String("true");
      con.showgrid = temp.equalsIgnoreCase(getParameter("showgrid"));
      con.rows = Integer.parseInt(getParameter("rows"));
      con.cols = Integer.parseInt(getParameter("columns"));
      con.scale = Integer.parseInt(getParameter("scale"));
      intv = Double.valueOf(getParameter("interval")).floatValue();
      mxv1 = Integer.parseInt(getParameter("capacity1"));
      mxv2 = Integer.parseInt(getParameter("capacity2"));
      mxv3 = Integer.parseInt(getParameter("capacity3"));
      mxv4 = Integer.parseInt(getParameter("capacity4"));
    }
    catch (Exception e) {
      System.out.println("Contour2D.paint: applet parameter error: "+e);
      System.exit(1);
    }
    float[] g = new float[con.rows*con.cols];
    float mr = con.rows/2;
    float mc = con.cols/2;
    for (int i=0; i<con.rows; i++) {
      for (int j=0; j<con.cols; j++) {
        g[con.rows*j+i] = (float) Math.sqrt((i-mr)*(i-mr) + (j-mc)*(j-mc));
      }
    }
    float low = 0;
    float high = 100;
    float base = 1;
    con.num1 = new int[1];
    con.num2 = new int[1];
    con.num3 = new int[1];
    con.num4 = new int[1];
    con.vx1 = new float[1][mxv1];
    con.vy1 = new float[1][mxv1];
    con.vx2 = new float[1][mxv2];
    con.vy2 = new float[1][mxv2];
    con.vx3 = new float[1][mxv3];
    con.vy3 = new float[1][mxv3];
    con.vx4 = new float[1][mxv4];
    con.vy4 = new float[1][mxv4];
    try {
      boolean[] swap = {false, false, false};
      float[] intervals = {.25f, .5f, 1.0f, 2.0f, 2.5f, 5.f, 10.f};
//    con.contour(g, con.rows, con.cols, intervals, low, high, base, true,
      con.contour(g, con.rows, con.cols, intv, low, high, base,
                  con.vx1, con.vy1, mxv1, con.num1,
                  con.vx2, con.vy2, mxv2, con.num2,
                  con.vx3, con.vy3, mxv3, con.num3,
                  con.vx4, con.vy4, mxv4, con.num4,
                  null, null, null, null, swap);
    }
    catch (VisADException VE) {
      System.out.println("Contour2D.init: "+VE);
      System.exit(1);
    }
  }

  public void mouseClicked(MouseEvent e) {
    // cycle between hidden contours, labels, and backwards labels
    con.whichlabels = (con.whichlabels+1)%5;
    Graphics g = getGraphics();
    if (g != null) {
      paint(g);
      g.dispose();
    }
  }

  public void mousePressed(MouseEvent e) {;}
  public void mouseReleased(MouseEvent e) {;}
  public void mouseEntered(MouseEvent e) {;}
  public void mouseExited(MouseEvent e) {;}

  public void paint(Graphics gr) {
    // draw grid dots if option is set
    if (con.showgrid) {
      gr.setColor(Color.blue);
      for (int i=0; i<con.cols; i++) {
        for (int j=0; j<con.rows; j++) {
          gr.drawRect(con.scale*i, con.scale*j, 2, 2);
        }
      }
    }
    // draw main contour lines
    gr.setColor(Color.black);
    for (int i=0; i<con.num1[0]; i+=2) {
      int v1 = (int) (con.scale*con.vy1[0][i]);
      int v2 = (int) (con.scale*con.vx1[0][i]);
      int v3 = (int) (con.scale*con.vy1[0][(i+1)%con.num1[0]]);
      int v4 = (int) (con.scale*con.vx1[0][(i+1)%con.num1[0]]);
      gr.drawLine(v1, v2, v3, v4);
    }
    for (int ix=-1; ix<1; ix++) {
      if (ix<0) gr.setColor(Color.white); else gr.setColor(Color.black);
      switch ((con.whichlabels+ix+5)%5) {
        case 0: // hidden contours are exposed
          for (int i=0; i<con.num2[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy2[0][i]);
            int v2 = (int) (con.scale*con.vx2[0][i]);
            int v3 = (int) (con.scale*con.vy2[0][(i+1)%con.num2[0]]);
            int v4 = (int) (con.scale*con.vx2[0][(i+1)%con.num2[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 1: // numbers cover hidden contours
          for (int i=0; i<con.num3[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy3[0][i]);
            int v2 = (int) (con.scale*con.vx3[0][i]);
            int v3 = (int) (con.scale*con.vy3[0][(i+1)%con.num3[0]]);
            int v4 = (int) (con.scale*con.vx3[0][(i+1)%con.num3[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 2: // numbers cover hidden contours, backwards
          for (int i=0; i<con.num4[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy4[0][i]);
            int v2 = (int) (con.scale*con.vx3[0][i]);
            int v3 = (int) (con.scale*con.vy4[0][(i+1)%con.num4[0]]);
            int v4 = (int) (con.scale*con.vx3[0][(i+1)%con.num3[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 3: // numbers cover hidden contours, upside-down
          for (int i=0; i<con.num3[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy3[0][i]);
            int v2 = (int) (con.scale*con.vx4[0][i]);
            int v3 = (int) (con.scale*con.vy3[0][(i+1)%con.num3[0]]);
            int v4 = (int) (con.scale*con.vx4[0][(i+1)%con.num4[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 4: // numbers cover hidden contours, upside-down & backwards
          for (int i=0; i<con.num3[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy4[0][i]);
            int v2 = (int) (con.scale*con.vx4[0][i]);
            int v3 = (int) (con.scale*con.vy4[0][(i+1)%con.num4[0]]);
            int v4 = (int) (con.scale*con.vx4[0][(i+1)%con.num4[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
      } // end switch
    }
  }

} // end class

