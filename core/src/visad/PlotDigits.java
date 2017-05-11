//
// PlotDigits.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

/**
   PlotDigits calculates an array of points to be plotted to
   the screen as vector pairs, given a number and a bounding
   rectangle, for use as a label on a contour R^2.<P>

*/
public class PlotDigits {

  // these variables are filled in by the plotdigits method
  public float[] Vx;   // x coordinates of label's digits
  public float[] Vy;   // y coordinates of label's digits
  public float[] VxB;  // x coordinates of label's digits in reverse display
  public float[] VyB;  // y coordinates of label's digits in reverse display
  /*
   * VxB and VyB can be combined with Vx and Vy to make any combination of
   * normal writing, backwards writing, upside-down writing, and
   * backwards and upside-down writing, for use in 3D rotation.
   */
  public int NumVerts;  // number of vertices put into Vx, Vy
  public float Number; // number to plot

  /*
   * Plot the digits for a contour label in a vector font format.
   *     Note: ROWS = XK TO XM, COLS = YK TO YM
   * Input:  gg - label value
   *         xk, yk, xm, ym - bounds for the label.
   * Output:  Vx, Vy, VxB, VyB - the vertices of the label's digits.
   * Return:  number of vertices put into vx,vy.
   */
  public void plotdigits(float gg, float xk, float yk,
                         float xm, float ym, int max, boolean[] swap)
         throws VisADException {
    int[] lb = { 0,   // 91 elements
      105,102,80,20,02,05,27,87,105,85,103,3,1,5,87,105,102,80,
      60,7,0,87,105,102,80,70,52,54,52,30,20,2,5,27,104,57,50,100,0,
      100,107,67,62,40,20,2,5,27,80,102,105,87,27,5,2,20,30,52,57,
      107,100,4,105,102,80,70,52,55,37,27,5,2,20,30,52,55,77,87,105,
      27, 5,2,20,80,102,105,87,77,55,50 };
    int[] lt = { 0,   // 12 elements
      1,10,15,22,35,40,49,60,63,80,91 };
    float xmk, ymk, hgt, h, dig;
    float row, col, hl, he;
    float rs, cs;
    int jg, j1, j2, j3, isign;
    int ib, ie, llin = 0, llel = 0, m;
    int i;
    NumVerts = 0;

    // extract digits from gg:
    // jg - integer to left of decimal of float gg
    // j1, j2, j3 - integers to right of decimal of float gg
    // dig - number of digits to plot
    // isign - sign of gg
    jg = (int) gg;
    if (gg < 0) {
      jg = -jg;
      gg = -gg;
      isign = -1;
      dig = 0.5f;
    }
    else {
      isign = 1;
      dig = 0.0f;
    }

    j1 = ( (int) (gg * 10.0) ) % 10;
    j2 = ( (int) (gg * 100.0) ) % 10;
    j3 = ( (int) (gg * 1000.0) ) % 10;

    // examine digits to left of decimal point
    if (jg>=100) {
      j1 = j2 = j3 = 0;
      dig += 3.0;
    }
    else if (jg>=10) {
      j3 = 0;
      dig += 4.5;
      if (j2==0) {
        dig -= 1.0;
        if (j1==0) dig -= 1.0f;
      }
    }
    else {
      dig += 4.5;
      if (j3==0) {
        dig -= 1.0;
        if (j2==0) {
          dig -= 1.0;
          if (j1==0) dig -= 1.0f;
        }
      }
    }
    if (dig<2.0) dig = 2.0f;
    // end extract digits routine

    xmk = xm-xk;
    if (xmk < 0) xmk = -xmk;
    ymk = ym-yk;
    if (ymk < 0) ymk = -ymk;

    if (swap[0]) {
      hgt = ymk/1.2f;
      h = xmk/(dig+0.2f);
      if (h < hgt) hgt=h;
      row = (xm > xk ? xm : xk)-0.5f*(xmk-dig*hgt);
      col = (ym > yk ? ym : yk)-0.5f*(ymk-hgt);
    }
    else {
      hgt = xmk/1.2f;
      h = ymk/(dig+0.2f);
      if (h < hgt) hgt=h;
      row = (xm > xk ? xm : xk)-0.5f*(xmk-hgt);
      col = (ym > yk ? ym : yk)-0.5f*(ymk-dig*hgt);
    }
    h = hgt/10.0f;

    rs = cs = 0.0f;

    Vx = new float[max];
    Vy = new float[max];

    // PLOT 1000THS
    if (j3 != 0) {
      ib = lt[j3+1];
      ie = lt[j3+2]-1;
      for (i=ib;i<=ie;i++) {
        if (swap[0]) {
          llel = lb[i]/10;
          llin = lb[i]-llel*10;
        }
        else {
          llin = lb[i]/10;
          llel = lb[i]-llin*10;
        }
        hl = h*llin;
        he = h*llel;
        if (i != ib) {
          Vx[NumVerts] = rs;
          Vy[NumVerts] = cs;
          NumVerts++;
          Vx[NumVerts] = row-hl;
          Vy[NumVerts] = col-he;
          NumVerts++;
        }
        rs = row-hl;
        cs = col-he;
      }
      // SPACE FOR COLUMN OF DIGIT
      if (swap[0]) {
        row = row-hgt;
      }
      else {
        col = col-hgt;
      }
    }

    // PLOT 100THS
    if (j2 != 0 || j3 != 0) {
      ib = lt[j2+1];
      ie = lt[j2+2]-1;
      for (i=ib;i<=ie;i++) {
        if (swap[0]) {
          llel = lb[i]/10;
          llin = lb[i]-llel*10;
        }
        else {
          llin = lb[i]/10;
          llel = lb[i]-llin*10;
        }
        hl = h*llin;
        he = h*llel;
        if (i != ib) {
          Vx[NumVerts] = rs;
          Vy[NumVerts] = cs;
          NumVerts++;
          Vx[NumVerts] = row-hl;
          Vy[NumVerts] = col-he;
          NumVerts++;
        }
        rs = row-hl;
        cs = col-he;
      }
      // space for column of digit
      if (swap[0]) {
        row = row-hgt;
      }
      else {
        col = col-hgt;
      }
    }

    // PLOT 10THS
    if (j1 != 0 || j2 != 0 || j3 != 0) {
      // PLOT DIGIT RIGHT OF DECIMAL
      ib = lt[j1+1];
      ie = lt[j1+2]-1;
      for (i=ib;i<=ie;i++) {
        if (swap[0]) {
          llel = lb[i]/10;
          llin = lb[i]-llel*10;
        }
        else {
          llin = lb[i]/10;
          llel = lb[i]-llin*10;
        }
        hl = h*llin;
        he = h*llel;
        if (i != ib) {
          Vx[NumVerts] = rs;
          Vy[NumVerts] = cs;
          NumVerts++;
          Vx[NumVerts] = row-hl;
          Vy[NumVerts] = col-he;
          NumVerts++;
        }
        rs = row-hl;
        cs = col-he;
      }

      // space for column of digit
      if (swap[0]) {
        row = row-hgt;
      }
      else {
        col = col-hgt;
      }

      // plot decimal cross
      if (swap[0]) {
        Vx[NumVerts] = row-0.2f*hgt;
        Vy[NumVerts] = col-0.1f*hgt;
        NumVerts++;
        Vx[NumVerts] = row-0.3f*hgt;
        Vy[NumVerts] = col-0.2f*hgt;
        NumVerts++;
        Vx[NumVerts] = row-0.2f*hgt;
        Vy[NumVerts] = col-0.2f*hgt;
        NumVerts++;
        Vx[NumVerts] = row-0.3f*hgt;
        Vy[NumVerts] = col-0.1f*hgt;
        NumVerts++;
      }
      else {
        Vx[NumVerts] = row-0.1f*hgt;
        Vy[NumVerts] = col-0.2f*hgt;
        NumVerts++;
        Vx[NumVerts] = row-0.2f*hgt;
        Vy[NumVerts] = col-0.3f*hgt;
        NumVerts++;
        Vx[NumVerts] = row-0.2f*hgt;
        Vy[NumVerts] = col-0.2f*hgt;
        NumVerts++;
        Vx[NumVerts] = row-0.1f*hgt;
        Vy[NumVerts] = col-0.3f*hgt;
        NumVerts++;
      }

      // half space for column of decimal cross
      if (swap[0]) {
        row = row-0.5f*hgt;
      }
      else {
        col = col-0.5f*hgt;
      }
    }

    // PLOT DIGITS LEFT OF DECIMAL
    // 100:
    do {
      m = jg-(jg/10)*10;
      ib = lt[m+1];
      ie = lt[m+2]-1;
      for (i=ib;i<=ie;i++) {
        if (swap[0]) {
          llel = lb[i]/10;
          llin = lb[i]-llel*10;
        }
        else {
          llin = lb[i]/10;
          llel = lb[i]-llin*10;
        }
        hl = h*llin;
        he = h*llel;
        if (i != ib) {
          Vx[NumVerts] = rs;
          Vy[NumVerts] = cs;
          NumVerts++;
          Vx[NumVerts] = row-hl;
          Vy[NumVerts] = col-he;
          NumVerts++;
        }
        rs = row-hl;
        cs = col-he;
      }
      jg = jg/10;
      // SPACE FOR COLUMN OF DIGIT
      if (swap[0]) {
        row = row-hgt;
      }
      else {
        col = col-hgt;
      }
    } while (jg != 0);


    if (isign < 0) {
      // PLOT MINUS SIGN
      if (swap[0]) {
        Vx[NumVerts] = row-0.4f*hgt;
        Vy[NumVerts] = col-0.5f*hgt;
        NumVerts++;
        Vx[NumVerts] = row;
        Vy[NumVerts] = col-0.5f*hgt;
        NumVerts++;
      }
      else {
        Vx[NumVerts] = row-0.5f*hgt;
        Vy[NumVerts] = col-0.4f*hgt;
        NumVerts++;
        Vx[NumVerts] = row-0.5f*hgt;
        Vy[NumVerts] = col;
        NumVerts++;
      }
    }
    VxB = new float[max];
    VyB = new float[max];
    for (int r=0; r<NumVerts; r++) {
      VxB[r] = (xm+xk)-Vx[r];
      VyB[r] = (ym+yk)-Vy[r];
    }
    if (swap[0]) {
      float[] temp = VyB;
      VyB = Vy;
      Vy = temp;
      temp = VxB;
      VxB = Vx;
      Vx = temp;
    }
    if (swap[1]) {
      float[] temp = VxB;
      VxB = Vx;
      Vx = temp;
    }
    if (swap[2]) {
      float[] temp = VyB;
      VyB = Vy;
      Vy = temp;
    }
  }

}

