
//
// Contour2D.java
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
  protected double[] vx1, vy1, vx2, vy2, vx3, vy3, vx4, vy4;

  /*
   * Compute contour lines for a 2-D array.  If the interval is negative,
   * then negative contour lines will be drawn as dashed lines.
   * The contour lines will be computed for all V such that:
   *           lowlimit <= V <= highlimit
   *     and   V = base + n*interval  for some integer n
   * Note that the input array, g, should be in column-major (FORTRAN) order.
   *
   * Input:  g - the 2-D array to contour.
   *         nr, nc - size of 2-D array in rows and columns.
   *         interval - the interval between contour lines.
   *         lowlimit - the lower limit on values to contour.
   *         highlimit - the upper limit on values to contour.
   *         base - base value to start contouring at.
   *         vx1, vy1 - arrays to put contour line vertices
   *         maxv1 - size of vx1, vy1 arrays
   *         numv1 - pointer to int to return number of vertices in vx1,vy1
   *         vx2, vy2 - arrays to put 'hidden' contour line vertices
   *         maxv2 - size of vx2, vy2 arrays
   *         numv2 - pointer to int to return number of vertices in vx2,vy2
   *         vx3, vy3 - arrays to put contour label vertices
   *         maxv3 - size of vx3, vy3 arrays
   *         numv3 - pointer to int to return number of vertices in vx3,vy3
   *         vx4, vy4 - arrays to put contour label vertices, inverted
   *                    ** see note for VxB and VyB in PlotDigits.java **
   *         maxv4 - size of vx4, vy4 arrays
   *         numv4 - pointer to int to return number of vertices in vx4,vy4
   * Return:  1 = ok
   *          0 = error  (interval==0.0)
   */
  public int contour( double g[], int nr, int nc, double interval,
                      double lowlimit, double highlimit, double base,
                      double vx1[], double vy1[],  int maxv1, int[] numv1,
                      double vx2[], double vy2[],  int maxv2, int[] numv2,
                      double vx3[], double vy3[],  int maxv3, int[] numv3,
                      double vx4[], double vy4[],  int maxv4, int[] numv4 )
                          throws VisADException {
    PlotDigits plot = new PlotDigits();
    int ir, ic;
    int nrm, ncm, idash;
    int numc, il;
    int lr, lc, lc2, lrr, lr2, lcc;
    double xd, yd ,xx, yy;
    double clow, chi;
    double gg;
    int maxsize = maxv1+maxv2;
    double[] vx = new double[maxsize];
    double[] vy = new double[maxsize];
    int[] ipnt = new int[2*maxsize];
    int nump, ip;
    int numv;

    // initialize vertex counts
    numv1[0] = 0;
    numv2[0] = 0;
    numv3[0] = 0;
    numv4[0] = 0;

    // deduct 100 vertices from maxv3/maxv4 now to save a later computation
    maxv3 -= 100;
    maxv4 -= 100;

    // check for bad contour interval
    if (interval==0.0) return 0;
    if (interval<0.0) {
      // draw negative contour lines as dashed lines
      interval = -interval;
      idash = 1;
    }
    else {
      idash = 0;
    }
  
    nrm = nr-1;
    ncm = nc-1;
  
    xd = ((nr-1)-0.0)/(nr-1);
    yd = ((nc-1)-0.0)/(nc-1);
  
    /*
     * set up mark array
     * mark= 0 if avail for label center,
     *       2 if in label, and
     *       1 if not available and not in label
     *
     * lr and lc give label size in grid boxes
     * lrr and lcc give unavailable radius
     */
    lr = 1+(nr-2)/50;
    lc = 1+(nc-2)/10;
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
    for (ir=0; ir<nrm && numv<maxsize-8 && nump<2*maxsize; ir++) {
      xx = xd*ir+0.0;
      for (ic=0; ic<ncm && numv<maxsize-8 && nump<2*maxsize; ic++) {
        double ga, gb, gc, gd;
        double gv, gn, gx;
        double tmp1, tmp2;

        // save index of first vertex in this grid box
        ipnt[nump++] = numv;

        yy = yd*ic+0.0;

        // get 4 corner values, skip box if any are missing
        ga = ( g[ (ic) * nr + (ir) ] );
/* WLH 24 Oct 97
        if (Double.isNaN(ga)) continue;
*/
        // test for missing
        if (ga != ga) continue;
        gb = ( g[ (ic) * nr + (ir+1) ] );
/* WLH 24 Oct 97
        if (Double.isNaN(gb)) continue;
*/
        // test for missing
        if (gb != gb) continue;
        gc = ( g[ (ic+1) * nr + (ir) ] );
/* WLH 24 Oct 97
        if (Double.isNaN(gc)) continue;
*/
        // test for missing
        if (gc != gc) continue;
        gd = ( g[ (ic+1) * nr + (ir+1) ] );
/* WLH 24 Oct 97
        if (Double.isNaN(gd)) continue;
*/
        // test for missing
        if (gd != gd) continue;

        // find average, min, and max of 4 corner values
        gv = (ga+gb+gc+gd)/4.0;

        // gn = MIN4(ga,gb,gc,gd);
        tmp1 = ( (ga) < (gb) ? (ga) : (gb) );
        tmp2 = ( (gc) < (gd) ? (gc) : (gd) );
        gn = ( (tmp1) < (tmp2) ? (tmp1) : (tmp2) );

        // gx = MAX4(ga,gb,gc,gd);
        tmp1 = ( (ga) > (gb) ? (ga) : (gb) );
        tmp2 = ( (gc) > (gd) ? (gc) : (gd) );
        gx = ( (tmp1) > (tmp2) ? (tmp1) : (tmp2) );
  
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

        for (il=0; il<numc && numv+8<maxsize; il++, gg += interval) {
          double gba, gca, gdb, gdc;
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
            double xk, yk, xm, ym, value;

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

            xk = xd*kr+0.0;
            yk = yd*kc+0.0;
            xm = xd*(mr+1.0)+0.0;
            ym = yd*(mc+1.0)+0.0;
            value = gg;

            if (numv3[0] < maxv3 || numv4[0] < maxv4) {
              // if there's room in either array, plot the labels
              plot.plotdigits( value, xk, yk, xm, ym, maxsize);
            }
            if (numv3[0] < maxv3) {
              // if there's room in the array, store the label
              System.arraycopy(plot.Vx, 0, vx3, numv3[0], plot.NumVerts);
              System.arraycopy(plot.Vy, 0, vy3, numv3[0], plot.NumVerts);
              numv3[0] += plot.NumVerts;
            }
            if (numv4[0] < maxv4) {
              // if there's room in the array, store the label
              System.arraycopy(plot.VxB, 0, vx4, numv4[0], plot.NumVerts);
              System.arraycopy(plot.VyB, 0, vy4, numv4[0], plot.NumVerts);
              numv4[0] += plot.NumVerts;
            }
          }

          switch (ii) {
            case 1:
              gba = gb-ga;
              gca = gc-ga;
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
          if (gg < 0.0 && idash==1) {
            double vxa, vya, vxb, vyb;
            vxa = vx[numv-2];
            vya = vy[numv-2];
            vxb = vx[numv-1];
            vyb = vy[numv-1];
            vx[numv-2] = (3.0*vxa+vxb) * 0.25;
            vy[numv-2] = (3.0*vya+vyb) * 0.25;
            vx[numv-1] = (vxa+3.0*vxb) * 0.25;
            vy[numv-1] = (vya+3.0*vyb) * 0.25;
          }

        }  // for il       -- NOTE:  gg incremented in for statement
      }  // for ic
     }  // for ir
  
     ipnt[nump] = numv;
  
     // copy vertices from vx, vy arrays to either v1 or v2 arrays
     ip = 0;
     for (ir=0;ir<nrm && ip<2*maxsize;ir++) {
       for (ic=0;ic<ncm && ip<2*maxsize;ic++) {
         int start, len;
         start = ipnt[ip];
         len = ipnt[ip+1] - start;
         if (len>0) {
           if (( mark[ (ic) * nr + (ir) ] )==2) {
             if (numv2[0]+len<maxv2) {
               for (il=0;il<len;il++) {
                 vx2[numv2[0]+il] = vx[start+il];
                 vy2[numv2[0]+il] = vy[start+il];
               }
               numv2[0] += len;
             }
           }
           else {
             if (numv1[0]+len<maxv1) {
               for (il=0; il<len; il++) {
                 vx1[numv1[0]+il] = vx[start+il];
                 vy1[numv1[0]+il] = vy[start+il];
               }
               numv1[0] += len;
             }
           }

         }
         ip++;
       }
     }
  
     return 1;
  }

  // APPLET SECTION

  /* run 'appletviewer contour.html' to test the Contour2D class. */
  public void init() {
    this.addMouseListener(this);
    con = new Contour2D();
    con.rows = 0;
    con.cols = 0;
    con.scale = 0;
    double intv = 0;
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
      intv = Double.valueOf(getParameter("interval")).doubleValue();
      mxv1 = Integer.parseInt(getParameter("capacity1"));
      mxv2 = Integer.parseInt(getParameter("capacity2"));
      mxv3 = Integer.parseInt(getParameter("capacity3"));
      mxv4 = Integer.parseInt(getParameter("capacity4"));
    }
    catch (Exception e) {
      System.out.println("Contour2D.paint: applet parameter error: "+e);
      System.exit(1);
    }
    double[] g = new double[con.rows*con.cols];
    double mr = con.rows/2;
    double mc = con.cols/2;
    for (int i=0; i<con.rows; i++) {
      for (int j=0; j<con.cols; j++) {
        g[con.rows*j+i] = Math.sqrt((i-mr)*(i-mr) + (j-mc)*(j-mc));
      }
    }
    double low = 0;
    double high = 100;
    double base = 1;
    con.num1 = new int[1];
    con.num2 = new int[1];
    con.num3 = new int[1];
    con.num4 = new int[1];
    con.vx1 = new double[mxv1];
    con.vy1 = new double[mxv1];
    con.vx2 = new double[mxv2];
    con.vy2 = new double[mxv2];
    con.vx3 = new double[mxv3];
    con.vy3 = new double[mxv3];
    con.vx4 = new double[mxv4];
    con.vy4 = new double[mxv4];
    try {
      con.contour(g, con.rows, con.cols, intv, low, high, base,
                  con.vx1, con.vy1, mxv1, con.num1,
                  con.vx2, con.vy2, mxv2, con.num2,
                  con.vx3, con.vy3, mxv3, con.num3,
                  con.vx4, con.vy4, mxv4, con.num4);
    }
    catch (VisADException VE) {
      System.out.println("Contour2D.init: "+VE);
      System.exit(1);
    }
  }

  public void mouseClicked(MouseEvent e) {
    // cycle between hidden contours, labels, and backwards labels
    con.whichlabels = (con.whichlabels+1)%5;
    paint(this.getGraphics());
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
      int v1 = (int) (con.scale*con.vy1[i]);
      int v2 = (int) (con.scale*con.vx1[i]);
      int v3 = (int) (con.scale*con.vy1[(i+1)%con.num1[0]]);
      int v4 = (int) (con.scale*con.vx1[(i+1)%con.num1[0]]);
      gr.drawLine(v1, v2, v3, v4);
    }
    for (int ix=-1; ix<1; ix++) {
      if (ix<0) gr.setColor(Color.white); else gr.setColor(Color.black);
      switch ((con.whichlabels+ix+5)%5) {
        case 0: // hidden contours are exposed
          for (int i=0; i<con.num2[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy2[i]);
            int v2 = (int) (con.scale*con.vx2[i]);
            int v3 = (int) (con.scale*con.vy2[(i+1)%con.num2[0]]);
            int v4 = (int) (con.scale*con.vx2[(i+1)%con.num2[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 1: // numbers cover hidden contours
          for (int i=0; i<con.num3[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy3[i]);
            int v2 = (int) (con.scale*con.vx3[i]);
            int v3 = (int) (con.scale*con.vy3[(i+1)%con.num3[0]]);
            int v4 = (int) (con.scale*con.vx3[(i+1)%con.num3[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 2: // numbers cover hidden contours, backwards
          for (int i=0; i<con.num4[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy4[i]);
            int v2 = (int) (con.scale*con.vx3[i]);
            int v3 = (int) (con.scale*con.vy4[(i+1)%con.num4[0]]);
            int v4 = (int) (con.scale*con.vx3[(i+1)%con.num3[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 3: // numbers cover hidden contours, upside-down
          for (int i=0; i<con.num3[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy3[i]);
            int v2 = (int) (con.scale*con.vx4[i]);
            int v3 = (int) (con.scale*con.vy3[(i+1)%con.num3[0]]);
            int v4 = (int) (con.scale*con.vx4[(i+1)%con.num4[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
          break;
        case 4: // numbers cover hidden contours, upside-down & backwards
          for (int i=0; i<con.num3[0]; i+=2) {
            int v1 = (int) (con.scale*con.vy4[i]);
            int v2 = (int) (con.scale*con.vx4[i]);
            int v3 = (int) (con.scale*con.vy4[(i+1)%con.num4[0]]);
            int v4 = (int) (con.scale*con.vx4[(i+1)%con.num4[0]]);
            gr.drawLine(v1, v2, v3, v4);
          }
      } // end switch
    }
  }

} // end class

