//
// Stream2D.java
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

import visad.*;
import visad.java3d.DisplayImplJ3D;
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

static int stream_trace( float[] ugrid, float[] vgrid, int nr, int nc,
                         float dir, float[] vr, float[] vc, int maxv, int[] numv,
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


    /* test for too many line segments 
    ------------------------------------*/
    if (num > maxv-2) {
      numv[0] = num;
      //-System.out.println("Stream2D: too many line segments");
      return 0;
    }

    /* propogate streamline 
    ------------------------------------*/
    prevrow = row;
    prevcol = col;

    float[][] loc = spatial_set.gridToValue(new float[][] {{prevcol}, {prevrow}});
     //float[][] loc_0 = spatial_set.gridToValue(new float[][] {{col}, {row}});
     //System.out.println("prev: "+loc[0][0]+", "+loc[1][0]+": "+loc_0[0][0]+", "+loc_0[1][0]);

    loc[1][0] += step*dir*v;
    loc[0][0] += step*dir*u;
     //System.out.println("new loc: "+loc[0][0]+", "+loc[1][0]);

    float[][] grid = spatial_set.valueToGrid(loc);
    row = grid[1][0];
    col = grid[0][0];
     //System.out.println("row: "+row+", col: "+col+" : "+grid[1][0]+", "+grid[0][0]);

    /* terminate stream if out of grid
    -----------------------------------*/
    if (row < 0 || col < 0 || row >= nr-1 || col >= nc-1) {
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
      if (irend < 0 || irend >= nrend || icend < 0 || icend >= ncend) {
       /*- System.out.println("bad 2:  irend = %d  icend = %d\n", irend, icend);*/
      }
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

    if (irs < 0 || irs >= nrstart || ics < 0 || ics >= ncstart) {
      /*- System.out.println("bad 3:  irs = %d  ics = %d\n", irs, ics); */
    }
    if (markstart[ (ics) * nrstart + (irs) ] == 0) {
      markstart[ (ics) * nrstart + (irs) ] = 1;
    }


    /*- check for need to draw arrow head 
    --------------------------------------*/
    ira = ( (int) (nrarrow * (row) / ((float) nr-1.0) ) );
    ica = ( (int) (ncarrow * (col) / ((float) nc-1.0) ) );

    if (markarrow[ica*nrstart + ira] == 0) {
      double rv, cv, vl;
      // test for too many line segments
      if (num > maxv-4) {
        return 0;
      }
      markarrow[ica*nrstart + ira] = 1;
      rv = dir * (row - prevrow);
      cv = dir * (col - prevcol);
      vl =  Math.sqrt(rv*rv + cv*cv);
      if (vl > 0.000000001) {
        rv = rv / vl;
        cv = cv / vl;
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
  return 1;
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

static int stream( float[] ugrid, float[] vgrid, int nr, int nc,
                   float density, float stepFactor, float arrowScale,
                   float[][] vr, float[][] vc, int max_lines,
                   int maxv, int[] numv, int[] numl,
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
  int[][] num = new int[max_lines][1];

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

  /*-- WLH - use shorter step for higher density
  step = ctx->TrajStep / density; 
  -----------------------------------------------*/

  /* allocate mark arrays */
  markarrow = new byte[nrstart*ncstart];
  markstart = new byte[nrstart*ncstart];
  markend = new byte[nrend*ncend];


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

  float[][] spatial_values = spatial_set.getSamples(false);
  int[] lens = spatial_set.getLengths();
  int lenX = lens[0];
  int lenY = lens[1];
  float dist_x = Math.abs(spatial_values[0][0] - spatial_values[0][1]);
  float dist_y = Math.abs(spatial_values[1][0] - spatial_values[1][lenX]);
  step = stepFactor*((dist_x + dist_y)/2);


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

        if (irend < 0 || irend >= nrend || icend < 0 || icend >= ncend)
        {
         /*-printf("bad 1:  irend = %d  icend = %d\n", irend, icend);*/
        }
        markend[icend*nrend + irend] = 1;

        dir = 1f;
        if (stream_trace( ugrid, vgrid, nr, nc, dir, 
                          vr[n_lines], vc[n_lines], maxv, num[n_lines],
                          markarrow, markstart, markend, nrarrow, ncarrow,
                          nrstart, ncstart, nrend, ncend, row, col, step,
                          rowlength, collength, irend, icend, spatial_set,
                          spatial_values) == 0)
        {
          numv[n_lines] = num[n_lines][0];
          //-System.out.println("1: return 1");
          //-return 1;
        }


        if (num[n_lines][0] > 0) {
          //-System.out.println("foward: "+n_lines+", n_verts: "+num[n_lines][0]);
          numv[n_lines] = num[n_lines][0];
          n_lines++;
        }

        /**- now trace streamline backward - shouldn't need to do this again
        row = ( ((float) nr-1f) * ((float) (irstart)+0.5f) / (float) nrstart );
        col = ( ((float) nc-1f) * ((float) (icstart)+0.5f) / (float) ncstart );
        irend = ( (int) (nrend * (row) / ((float) nr-1f) ) );
        icend = ( (int) (ncend * (col) / ((float) nc-1f) ) );
         */

        if (irend < 0 || irend >= nrend || icend < 0 || icend >= ncend)
        {
         //-printf("bad 3:  irend = %d  icend = %d\n", irend, icend);
        }
        markend[icend*nrend + irend] = 1;

        dir = -1f;
        if (stream_trace( ugrid, vgrid, nr, nc, dir,
                          vr[n_lines], vc[n_lines], maxv, num[n_lines],
                          markarrow, markstart, markend, nrarrow, ncarrow,
                          nrstart, ncstart, nrend, ncend, row, col, step,
                          rowlength, collength, irend, icend, spatial_set,
                          spatial_values) == 0)
        {
          numv[n_lines] = num[n_lines][0];
          //-System.out.println("2: return 1");
          //-return 1;
        }

        if (num[n_lines][0] > 0) {
          //-System.out.println("backward: "+n_lines+", n_verts: "+num[n_lines][0]);
          numv[n_lines] = num[n_lines][0];
          n_lines++;
        }


      } /* end if */
    } /* end for */
  } /* end for */


  numv[n_lines] = num[n_lines][0];
  numl[0] = n_lines;

  return 1;
}

public static void main(String[] args)
       throws VisADException, RemoteException
{
  int nr = 50;
  int nc = 50;

  DisplayImpl dpy = new DisplayImplJ3D("display");

  RealType u_wind = RealType.getRealType("u_wind");
  RealType v_wind = RealType.getRealType("v_wind");

  RealTupleType uv = new RealTupleType(u_wind, v_wind);


  FunctionType f_type =
    new FunctionType(RealTupleType.SpatialCartesian2DTuple, uv);

  Integer2DSet d_set =
    new Integer2DSet(RealTupleType.SpatialCartesian2DTuple, nr, nc);

  FlatField uv_field =
    new FlatField(f_type, d_set);

  float[][] uv_values = new float[2][nr*nc];
  
  double ang = 2*Math.PI/nr;
  /**
  for ( int jj = 0; jj < nc; jj++ ) {
    for ( int ii = 0; ii < nr; ii++ ) {
      int idx = jj*nr + ii;
      uv_values[0][idx] = 10f;
      uv_values[1][idx] = 10f*((float) Math.cos(2*ang*ii));
    }
  }
  **/
  for ( int jj = 0; jj < nc; jj++ ) {
    for ( int ii = 0; ii < nr; ii++ ) {
      int idx = jj*nr + ii;
      uv_values[0][idx] = 5 + -20f*((float) Math.cos(0.5*ang*ii));
      uv_values[1][idx] = -10f*((float) Math.cos(0.5*ang*ii));
    }
  }

  uv_field.setSamples(uv_values, false);
                                      


  int[] numl = new int[1];
  int maxv = 1000;
  int max_lines = 100;
  int[] n_verts = new int[max_lines];
  float[][] vr = new float[max_lines][maxv];
  float[][] vc = new float[max_lines][maxv];


  stream(uv_values[0], uv_values[1], nr, nc, 1f, 1, 1f, vr, vc, max_lines, maxv, n_verts, numl, null);

  
  ScalarMap xmap = new ScalarMap(RealType.XAxis, Display.XAxis);
  ScalarMap ymap = new ScalarMap(RealType.YAxis, Display.YAxis);
  dpy.addMap(xmap);
  dpy.addMap(ymap);

  ScalarMap flowx = new ScalarMap(u_wind, Display.Flow1X);
  ScalarMap flowy = new ScalarMap(v_wind, Display.Flow1Y);
  dpy.addMap(flowx);
  dpy.addMap(flowy);

  FlowControl flow_cntrl = (FlowControl) flowx.getControl();
  flow_cntrl.setFlowScale(0.04f);

  flow_cntrl = (FlowControl) flowy.getControl();
  flow_cntrl.setFlowScale(0.04f);

  DataReferenceImpl ref = new DataReferenceImpl("wind");
  ref.setData(uv_field);

  dpy.addReference(ref);


  Gridded2DSet[] gsets = new Gridded2DSet[numl[0]];
  for ( int s_idx = 0; s_idx < numl[0]; s_idx++ ) {
    float[][] strm_values = new float[2][n_verts[s_idx]];
    System.arraycopy(vc[s_idx], 0, strm_values[0], 0, n_verts[s_idx]);
    System.arraycopy(vr[s_idx], 0, strm_values[1], 0, n_verts[s_idx]);

    gsets[s_idx] =
      new Gridded2DSet(RealTupleType.SpatialCartesian2DTuple, strm_values, n_verts[s_idx]);
  }

  UnionSet uset = new UnionSet(gsets);
  DataReferenceImpl strm_ref = new DataReferenceImpl("stream");
  strm_ref.setData(uset);

  ConstantMap[] strm_cm =
    new ConstantMap[]
  {
    new ConstantMap(0.1, Display.Red),
    new ConstantMap(0.8, Display.Green),
    new ConstantMap(0.1, Display.Blue),
    new ConstantMap(1.5, Display.LineWidth)
  };

  dpy.addReference(strm_ref, strm_cm);


  JFrame jframe  = new JFrame();
  jframe.addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) {System.exit(0);}
  });

  jframe.setContentPane((JPanel) dpy.getComponent());
  jframe.setVisible(true);
}

}
