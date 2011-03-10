//
// Stream2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
                          int nrend, int ncend, float row, float col, float stepFactor,
                          float rowlength, float collength, int irend, int icend,
                          Gridded2DSet spatial_set, float[][] spatial_values, int[] offgrid, float cell_fraction)
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

  float[] vec_row = new float[2];
  float[] vec_col = new float[2];

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

    float[][] loc = new float[2][1];
    loc[1][0] = row;
    loc[0][0] = col;

    float mag = (float) Math.sqrt((double)(u*u + v*v));
    int[] lens = spatial_set.getLengths();
    int lenX = lens[0];
    int ii = ((int)row)*lenX + (int)col;

    float del_x = spatial_values[0][ii+1] - spatial_values[0][ii];
    float del_y = spatial_values[1][ii+1] - spatial_values[1][ii];
    if (((del_x != del_x) || (del_y != del_y)) || ((u!=u || v!=v))) {
      break;
    }
    float mag_col = (float) Math.sqrt((double)(del_x*del_x + del_y*del_y));
    vec_col[0] = del_x/mag_col;
    vec_col[1] = del_y/mag_col;

    del_x = spatial_values[0][ii+lenX] - spatial_values[0][ii];
    del_y = spatial_values[1][ii+lenX] - spatial_values[1][ii];
    float mag_row = (float) Math.sqrt((double)(del_x*del_x + del_y*del_y));
    vec_row[0] = del_x/mag_row;
    vec_row[1] = del_y/mag_row;

    float dist = 1f;
    float step = stepFactor*cell_fraction*(dist/mag);

    loc[0][0] += step*dir*(u*vec_col[0] + v*vec_col[1]);
    loc[1][0] += step*dir*(u*vec_row[0] + v*vec_row[1]);


    row = loc[1][0];
    col = loc[0][0];


    /* terminate stream if out of grid
    -----------------------------------*/
    if (row < 0 || col < 0 || row >= nr-1 || col >= nc-1 || row != row || col != col) {
      offgrid[0]++;
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
    if (nend > 2.5*(nr/(cell_fraction*nrend)) ) {
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
      vl *= 3;
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
                   Gridded2DSet spatial_set, float packingFactor, float cntrWeight, int n_pass, float reduction)
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
  float cell_fraction = 0.05f;

  byte[] markarrow;
  byte[] markstart;
  byte[] markend;

  int n_lines = 0;

  /* initialize vertex counts */
  int[] num = new int[1];


  /* density calculations */
  if (density < 0.5) density = 0.5f;

  // WLH 30 March 2006
  // if (density > 2.0) density = 2.0f;
  if (density > nr/15f) density = nr/15f;
  if (density > nc/15f) density = nc/15f;

  nrarrow = (int)(15f*density);
  ncarrow = (int)(15f*density);
  nrstart = (int)(15f*density);
  ncstart = (int)(15f*density);

  // WLH 30 March 2006
  if (nrarrow > nr) nrarrow = nr;
  if (ncarrow > nc) ncarrow = nc;
  if (nrstart > nr) nrstart = nr;
  if (ncstart > nc) ncstart = nc;

  nrend = 4*nrstart;
  ncend = 4*ncstart;

  // WLH 30 March 2006
  if (nrend > nr) nrend = nr;
  if (ncend > nc) ncend = nc;


  //-TDR ----------------------------
  float n_start_per_cell = 0.50f;
  float n_end_per_cell   = 4.00f;
  n_start_per_cell = 30f*density/((nc+nr)/2);
  n_end_per_cell  = packingFactor*8f*n_start_per_cell;


  nrstart = (int) (n_start_per_cell * nr);
  nrarrow = nrstart;
  ncstart = (int) (n_start_per_cell * nc);
  ncarrow = ncstart;
  nrend   = (int) (n_end_per_cell * nr);
  ncend   = (int) (n_end_per_cell * nc);


  rowlength = LENGTH * nr / density;
  collength = LENGTH * nc / density;
  rowlength *= arrowScale;
  collength *= arrowScale;


  numv[0] = new int[50];
  vr[0]   = new float[50][];
  vc[0]   = new float[50][];

  /*- allocate mark arrays */
  markarrow = new byte[nrarrow*ncarrow];
  markstart = new byte[nrstart*ncstart];
  markend   = new byte[nrend*ncend];

  /*- initialize mark array */
  for ( int kk = 0; kk < nrstart*ncstart; kk++ ) {
    markstart[kk] = 0;
    markarrow[kk] = 1;
  }
  for ( int kk = 0; kk < nrend*ncend; kk++ ) {
    markend[kk] = 0;
  }

  /*- only draw arrows in every ninth box */
  for (ir = 1; ir<nrarrow; ir+=3) {
    for (ic = 1; ic<ncarrow; ic+=3) {
      markarrow[ic*nrarrow + ir] = 0;
    }
  }


  step = 1;
  float[][] spatial_values = spatial_set.getSamples(false);


  //-TDR, smoothing
  if (cntrWeight > 6f) cntrWeight = 6;
  float ww = (6f - cntrWeight)/4f;
  for (int pass=0; pass<n_pass; pass++) {
    float[] new_ugrid = new float[ugrid.length];
    float[] new_vgrid = new float[vgrid.length];
    System.arraycopy(ugrid, 0, new_ugrid, 0, new_ugrid.length);
    System.arraycopy(vgrid, 0, new_vgrid, 0, new_vgrid.length);
    for (int iir=1; iir < nr-1; iir++) {
      for (int iic=1; iic < nc-1; iic++) {
        int kk = iir*nc + iic;
        float suu = ugrid[kk];
        float sua = ugrid[kk-1];
        float sub = ugrid[kk+1];
        float suc = ugrid[kk+nc];
        float sud = ugrid[kk-nc];
        new_ugrid[kk] = (cntrWeight*suu + ww*sua + ww*sub + ww*suc + ww*sud)/6;
        float svv = vgrid[kk];
        float sva = vgrid[kk-1];
        float svb = vgrid[kk+1];
        float svc = vgrid[kk+nc];
        float svd = vgrid[kk-nc];
        new_vgrid[kk] = (cntrWeight*svv + ww*sva + ww*svb + ww*svc + ww*svd)/6;
      }
    }
    ugrid = new_ugrid;
    vgrid = new_vgrid;
  }

  int cnt = 0;
  int[] offgrid = new int[1];

  /* iterate over start boxes */
  for (icstart=0; icstart<ncstart; icstart++) {
    for (irstart=0; irstart<nrstart; irstart++) {

      cnt = 0;
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
        offgrid[0] = 0;

        stream_trace( ugrid, vgrid, nr, nc, dir,
                      vr2, vc2,
                      num,
                      markarrow, markstart, markend, nrarrow, ncarrow,
                      nrstart, ncstart, nrend, ncend, row, col, stepFactor,
                      rowlength, collength, irend, icend, spatial_set,
                      spatial_values, offgrid, cell_fraction);

        vr[0][n_lines] = vr2[0];
        vc[0][n_lines] = vc2[0];


        cnt += num[0];
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


        /* trace streamline backward */
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
                      nrstart, ncstart, nrend, ncend, row, col, stepFactor,
                      rowlength, collength, irend, icend, spatial_set,
                      spatial_values, offgrid, cell_fraction);

        vr[0][n_lines] = vr2[0];
        vc[0][n_lines] = vc2[0];

        cnt += num[0];
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

        //-TDR
        if ((cnt < (((nr+nc)/2)/cell_fraction)*reduction ) && reduction != 1f) {
          if (offgrid[0] == 0) {
            n_lines -= 2;
          }
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
