//
// Stream2D.java
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

import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Stream2D 
{

/*
 * trace one stream line for 2-D u and v arrays.
 * Note that the input arrays ugrid & vgrid should be in
 * column-major (FORTRAN) order.
 *
 * Input:  ugrid, vgrid - the 2-D wind arrays.
 *         nr, nc - size of 2-D array in rows and columns.
 *         vr, vc - arrays to put streamline vertices
 *         dir - direction 1.0=forward, -1.0=backward
 *         maxv - size of vx, vy arrays
 *         numv - pointer to int to return number of vertices in vx, vy
 *         markarrow - mark array for arrows
 *         markstart - mark array for starting streamlines
 *         markend - mark array for ending streamlines
 *         nrarrow, ncarrow - size of markarrow
 *         nrstart, ncstart - size of markstart
 *         nrend, ncend - size of markend
 *         row, col - start location for streamline
 *         step - step size for streamline
 *         rowlength, collength - scale constants for arrows
 *         irend, icend - location of most recent end box
 * Return:  1 = ok
 *          0 = no more memory for streamlines
 */

static float LENGTH = 0.015f;

static void stream_trace( float[] ugrid, float[] vgrid, int nr, int nc,
                          float dir, float[][] vr2, float[][] vc2, int[] numv,
                          byte[] markarrow, byte[] markstart, byte[] markend,
                          int nrarrow, int ncarrow, int nrstart, int ncstart,
                          int nrend, int ncend, float row, float col, float step,
                          float rowlength, float collength, int irend, int icend,
                          Gridded2DSet spatial_set, float[][] spatial_values)
       throws VisADException
{

  int irstart, icstart, ir, ic, ire, ice;
  int ira, ica, irs, ics;
  int nend, num;
  float prevrow, prevcol;
  float a, c, ac, ad, bc, bd;
  float u, v;

  num = numv[0];
  nend = 0;

  float[] vr = vr2[0];
  float[] vc = vc2[0];

  while (true) {
    float ubd, ubc, uad, uac, vbd, vbc, vad, vac;


    /*- interpolate velocity at row, col
    --------------------------------------*/

    ir = (int) row;
    ic = (int) col;
    a = row - (float)ir;
    c = col - (float)ic;
    ac = a*c;
    ad = a*(1f-c);
    bc = (1f-a)*c;
    bd = (1f-a)*(1f-c);

    ubd = ugrid[ir*nc + ic];
    ubc = ugrid[ir*nc + ic+1];
    uad = ugrid[(ir+1)*nc + ic];
    uac = ugrid[(ir+1)*nc + (ic+1)];
    vbd = vgrid[ir*nc + ic];
    vbc = vgrid[ir*nc + (ic+1)];
    vad = vgrid[(ir+1)*nc + ic];
    vac = vgrid[(ir+1)*nc + (ic+1)];


    /* terminate stream if missing data
    ------------------------------------*/
    if (ubd != ubd || ubc != ubc ||
        uad != uad || uac != uac ||
        vbd != vbd || vbc != vbc ||
        vad != vad || vac != vac)
    {
       break;
    }

    u = bd * ubd + bc * ubc +
        ad * uad + ac * uac;
    v = bd * vbd + bc * vbc +
        ad * vad + ac * vac;


    /* propogate streamline
    ------------------------------------*/
    prevrow = row;
    prevcol = col;

    float[][] loc = spatial_set.gridToValue(new float[][] {{prevcol}, {prevrow}});

      /**-
       float[][] loc_0 =
         spatial_set.gridToValue(new float[][] {{col}, {row}});
       System.out.println("prev: "+loc[0][0]+", "+loc[1][0]+": "
         +loc_0[0][0]+", "+loc_0[1][0]);
       */

    loc[1][0] += step*dir*v;
    loc[0][0] += step*dir*u;

      //System.out.println("new loc: "+loc[0][0]+", "+loc[1][0]);

    float[][] grid = spatial_set.valueToGrid(loc);
    row = grid[1][0];
    col = grid[0][0];

    //System.out.println("row: "+row+", col: "+col+" : "+grid[1][0]+", "+grid[0][0]);

    /* terminate stream if out of grid
    -----------------------------------*/
    if (row < 0 || col < 0 || row >= nr-1 || col >= nc-1 || row != row || col != col) {
      break;
    }

    ire = ( (int) (nrend * (row) / ((float) nr-1.0) ) );
    ice = ( (int) (ncend * (col) / ((float) nc-1.0) ) );

    /* terminate stream if enters marked end box
    ---------------------------------------------*/
    if (ire != irend || ice != icend)
    {
      irend = ire;
      icend = ice;
      if (markend[icend*nrend + irend] == 1) {
        break;
      }
      markend[icend*nrend + irend] = 1;
      nend = 0;
    }

    /* terminate stream if too many steps in one end box
    -----------------------------------------------------*/
    nend++;
    if (nend > 100) {
      //-System.out.println("Stream2D: too many steps in one end box");
      break;
    }


    /*- check vertex array length, expand if necessary
    ---------------------------------------------------*/
    if (num >= vr.length - 4) {
      float[] tmp = new float[vr.length + 50];
      System.arraycopy(vr, 0, tmp, 0, vr.length);
      vr = tmp;
      tmp = new float[vc.length + 50];
      System.arraycopy(vc, 0, tmp, 0, vc.length);
      vc = tmp;
    }

    /*- make line segment 
    ----------------------*/
    vr[num]   = prevrow;
    vc[num++] = prevcol;
    vr[num]   = row;
    vc[num++] = col;

    /* mark start box 
    ---------------------*/
    irs = ( (int) (nrstart * (row) / ((float) nr-1.0) ) );
    ics = ( (int) (ncstart * (col) / ((float) nc-1.0) ) );

    if (markstart[ (ics) * nrstart + (irs) ] == 0) {
      markstart[ (ics) * nrstart + (irs) ] = 1;
    }


    /*- check for need to draw arrow head 
    --------------------------------------*/
    ira = ( (int) (nrarrow * (row) / ((float) nr-1.0) ) );
    ica = ( (int) (ncarrow * (col) / ((float) nc-1.0) ) );

    if (markarrow[ica*nrstart + ira] == 0) {
      double rv, cv, vl;
      markarrow[ica*nrstart + ira] = 1;
      rv = dir * (row - prevrow);
      cv = dir * (col - prevcol);
      vl =  Math.sqrt(rv*rv + cv*cv);
      if (vl > 0.000000001) {
        rv = rv / vl;
        cv = cv / vl;
      }

      /*- check vertex array length, expand if necessary
      ---------------------------------------------------*/
      if ( num >= vr.length - 6) {
        float[] tmp = new float[vr.length + 50];
        System.arraycopy(vr, 0, tmp, 0, vr.length);
        vr = tmp;
        tmp = new float[vc.length + 50];
        System.arraycopy(vc, 0, tmp, 0, vc.length);
        vc = tmp;
      }
      vr[num]   = row;
      vc[num++] = col;
      vr[num]   = row - ((float)(rv + cv)) * rowlength;
      vc[num++] = col + ((float)(rv - cv)) * collength;
      vr[num]   = row;
      vc[num++] = col;
      vr[num]   = row + ((float)(cv - rv)) * rowlength;
      vc[num++] = col - ((float)(cv + rv)) * collength;
    }

  } /* end while */

  numv[0] = num;
  vr2[0] = vr;
  vc2[0] = vc;
}



/*
 * Compute stream lines for 2-D u and v arrays.
 * Note that the input arrays ugrid & vgrid should be in
 * row-major order.
 *
 * Input:  ugrid, vgrid - the 2-D wind arrays.
 *         nr, nc - size of 2-D array in rows and columns.
 *         density - relative density of streamlines.
 *         vr, vc - arrays to put streamline vertices
 *         maxv - size of vx, vy arrays
 *         numv - pointer to int to return number of vertices in vx, vy
 * Return:  1 = ok
 *          0 = error  (out of memory)
 */

public static int stream( float[] ugrid, float[] vgrid, int nr, int nc,
                   float density, float stepFactor, float arrowScale,
                   float[][][] vr, float[][][] vc,
                   int[][] numv, int[] numl,
                   Gridded2DSet spatial_set)
       throws VisADException
{
  int irstart, icstart, irend, icend, ir, ic, ire, ice;
  int ira, ica, irs, ics;
  int nrarrow, ncarrow, nrstart, ncstart, nrend, ncend;
  int nend;
  float row, col, prevrow, prevcol;
  float a, c, ac, ad, bc, bd;
  float u, v, step, rowlength, collength;
  float dir;

  byte[] markarrow;
  byte[] markstart;
  byte[] markend;

  int n_lines = 0;

  /* initialize vertex counts */
  //-int[][] num = new int[max_lines][1];
  int[] num = new int[1];

  /* density calculations */
  if (density < 0.5) density = 0.5f;
  if (density > 2.0) density = 2.0f;

  nrarrow = (int)(15f*density);
  ncarrow = (int)(15f*density);
  nrstart = (int)(15f*density);
  ncstart = (int)(15f*density);

  nrend = 4*nrstart;
  ncend = 4*ncstart;

  rowlength = LENGTH * nr / density;
  collength = LENGTH * nc / density;
  rowlength *= arrowScale;
  collength *= arrowScale;

  numv[0] = new int[50];
  vr[0]   = new float[50][];
  vc[0]   = new float[50][];

  /* allocate mark arrays */
  markarrow = new byte[nrstart*ncstart];
  markstart = new byte[nrstart*ncstart];
  markend   = new byte[nrend*ncend];


  /* initialize mark array */
  for ( int kk = 0; kk < nrstart*ncstart; kk++ ) {
    markstart[kk] = 0;
    markarrow[kk] = 1;
  }
  for ( int kk = 0; kk < nrend*ncend; kk++ ) {
    markend[kk] = 0;
  }

  /* only draw arrows in every ninth box */
  for (ir = 1; ir<nrarrow; ir+=3) {
    for (ic = 1; ic<ncarrow; ic+=3) {
      markarrow[ic*nrarrow + ir] = 0;
    }
  }


  /*-- compute propagation step
  -----------------------------------------------*/
  float umax = Float.MIN_VALUE;
  float vmax = Float.MIN_VALUE;
  for (int kk = 0; kk < ugrid.length; kk++) {
    if ( ugrid[kk] > umax ) umax = ugrid[kk];
    if ( vgrid[kk] > vmax ) vmax = vgrid[kk];
  }
  float smax = (float) Math.sqrt((double)(umax*umax + vmax*vmax));
  float[][] spatial_values = spatial_set.getSamples(false);
  int[] lens = spatial_set.getLengths();
  int lenX = lens[0];
  float dist_x = Math.abs(spatial_values[0][lenX+1] - spatial_values[0][0]);
  float dist_y = Math.abs(spatial_values[1][lenX+1] - spatial_values[1][0]);
  float dist = (dist_x + dist_y)/2;
  step = stepFactor*(dist/smax);


  /* iterate over start boxes */
  for (icstart=0; icstart<ncstart; icstart++) {
    for (irstart=0; irstart<nrstart; irstart++) {
      if (markstart[icstart*nrstart + irstart] == 0) {
        markstart[ icstart*nrstart + irstart ] = 1;

        /* trace streamline forward */
        row = ( ((float) nr-1f) * ((float) (irstart)+0.5f) / (float) nrstart );
        col = ( ((float) nc-1f) * ((float) (icstart)+0.5f) / (float) ncstart );
        irend = ( (int) (nrend * (row) / ((float) nr-1f) ) );
        icend = ( (int) (ncend * (col) / ((float) nc-1f) ) );

        markend[icend*nrend + irend] = 1;
        
        if (n_lines == vr[0].length) {
          float[][] f_tmp = new float[vr[0].length + 50][];
          System.arraycopy(vr[0], 0, f_tmp, 0, vr[0].length);
          vr[0] = f_tmp;

          f_tmp = new float[vc[0].length + 50][];
          System.arraycopy(vc[0], 0, f_tmp, 0, vc[0].length);
          vc[0] = f_tmp;
        }


        float[][] vr2 = new float[1][];
        float[][] vc2 = new float[1][];
        vr2[0] = new float[200];
        vc2[0] = new float[200];
        vr[0][n_lines] = vr2[0];
        vc[0][n_lines] = vc2[0];

        dir = 1f;
        num[0] = 0;

        stream_trace( ugrid, vgrid, nr, nc, dir,
                      vr2, vc2,
                      num,
                      markarrow, markstart, markend, nrarrow, ncarrow,
                      nrstart, ncstart, nrend, ncend, row, col, step,
                      rowlength, collength, irend, icend, spatial_set,
                      spatial_values);

        vr[0][n_lines] = vr2[0];
        vc[0][n_lines] = vc2[0];


        if (num[0] > 0) {

          if ( n_lines == numv[0].length ) {
            int[] tmp = new int[numv[0].length + 50];
            System.arraycopy(numv[0], 0, tmp, 0, numv[0].length);
            numv[0] = tmp;
          }

          float[] ftmp = new float[num[0]];
          System.arraycopy(vr[0][n_lines], 0, ftmp, 0, ftmp.length);
          vr[0][n_lines] = ftmp;
          ftmp = new float[num[0]];
          System.arraycopy(vc[0][n_lines], 0, ftmp, 0, ftmp.length);
          vc[0][n_lines] = ftmp;

          numv[0][n_lines] = num[0];
          n_lines++;
        }


        markend[icend*nrend + irend] = 1;

        if (n_lines == vr[0].length) {
          float[][] f_tmp = new float[vr[0].length + 50][];
          System.arraycopy(vr[0], 0, f_tmp, 0, vr[0].length);
          vr[0] = f_tmp;

          f_tmp = new float[vc[0].length + 50][];
          System.arraycopy(vc[0], 0, f_tmp, 0, vc[0].length);
          vc[0] = f_tmp;
        }

        vr2 = new float[1][];
        vc2 = new float[1][];
        vr2[0] = new float[200];
        vc2[0] = new float[200];
        vr[0][n_lines] = vr2[0];
        vc[0][n_lines] = vc2[0];

        dir = -1f;
        num[0] = 0;

        stream_trace( ugrid, vgrid, nr, nc, dir,
                      vr2, vc2,
                      num,
                      markarrow, markstart, markend, nrarrow, ncarrow,
                      nrstart, ncstart, nrend, ncend, row, col, step,
                      rowlength, collength, irend, icend, spatial_set,
                      spatial_values);

        vr[0][n_lines] = vr2[0];
        vc[0][n_lines] = vc2[0];

        if (num[0] > 0) {

          if ( n_lines == numv[0].length ) {
            int[] tmp = new int[numv[0].length + 50];
            System.arraycopy(numv[0], 0, tmp, 0, numv[0].length);
            numv[0] = tmp;
          }

          float[] ftmp = new float[num[0]];
          System.arraycopy(vr[0][n_lines], 0, ftmp, 0, ftmp.length);
          vr[0][n_lines] = ftmp;
          ftmp = new float[num[0]];
          System.arraycopy(vc[0][n_lines], 0, ftmp, 0, ftmp.length);
          vc[0][n_lines] = ftmp;
          
          numv[0][n_lines] = num[0];
          n_lines++;
        }


      } /* end if */
    } /* end for */
  } /* end for */


  int[] tmp = new int[n_lines];
  System.arraycopy(numv[0], 0, tmp, 0, n_lines);
  numv[0] = tmp;

  numl[0] = n_lines;

  return 1;
}

}

