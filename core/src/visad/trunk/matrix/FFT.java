
//
// FFT.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.matrix;

import visad.*;

/**
 * FFT is the VisAD class for Fast Fourier Transforms
*/

public class FFT {

  public static float[][] FFT2D(int rows, int cols, float[][] x,
                                boolean forward)
         throws VisADException {
    if (x == null) return null;
    if (x.length != 2 || x[0].length != x[1].length) {
      throw new FieldException("bad x lengths");
    }
    int n = x[0].length;
    if (rows * cols != n) {
      throw new FieldException(rows + " * " + cols + " must equal " + n);
    }
    float[][] y = new float[2][n];
    float[][] z = new float[2][rows];
    for (int c=0; c<cols; c++) {
      int i = c * rows;
      for (int r=0; r<rows; r++) {
        z[0][r] = x[0][i + r];
        z[1][r] = x[1][i + r];
      }
      z = FFT1D(z, forward);
      for (int r=0; r<rows; r++) {
        y[0][i + r] = z[0][r];
        y[1][i + r] = z[1][r];
      }
    }

    float[][] u = new float[2][n];
    float[][] v = new float[2][cols];
    for (int r=0; r<rows; r++) {
      int i = r;
      for (int c=0; c<cols; c++) {
        v[0][c] = y[0][i + c * rows];
        v[1][c] = y[1][i + c * rows];
      }
      v = FFT1D(v, forward);
      for (int c=0; c<cols; c++) {
        u[0][i + c * rows] = v[0][c];
        u[1][i + c * rows] = v[1][c];
      }
    }
    return u;
  }

  public static double[][] FFT2D(int rows, int cols, double[][] x,
                                boolean forward)
         throws VisADException {
    if (x == null) return null;
    if (x.length != 2 || x[0].length != x[1].length) {
      throw new FieldException("bad x lengths");
    }
    int n = x[0].length;
    if (rows * cols != n) {
      throw new FieldException(rows + " * " + cols + " must equal " + n);
    }
    double[][] y = new double[2][n];
    double[][] z = new double[2][rows];
    for (int c=0; c<cols; c++) {
      int i = c * rows;
      for (int r=0; r<rows; r++) {
        z[0][r] = x[0][i + r];
        z[1][r] = x[1][i + r];
      }
      z = FFT1D(z, forward);
      for (int r=0; r<rows; r++) {
        y[0][i + r] = z[0][r];
        y[1][i + r] = z[1][r];
      }
    }

    double[][] u = new double[2][n];
    double[][] v = new double[2][cols];
    for (int r=0; r<rows; r++) {
      int i = r;
      for (int c=0; c<cols; c++) {
        v[0][c] = y[0][i + c * rows];
        v[1][c] = y[1][i + c * rows];
      }
      v = FFT1D(v, forward);
      for (int c=0; c<cols; c++) {
        u[0][i + c * rows] = v[0][c];
        u[1][i + c * rows] = v[1][c];
      }
    }
    return u;
  }

  public static float[][] FFT1D(float[][] x, boolean forward)
         throws VisADException {
    if (x == null) return null;
    if (x.length != 2 || x[0].length != x[1].length) {
      throw new FieldException("bad x lengths");
    }
    int n = x[0].length;
    int n2 = 1;
    while (n2 < n) {
      n2 *= 2;
      if (n2 > n) {
        throw new FieldException("x length must be power of 2");
      }
    }
    n2 = n/2;
    float[][] temp = new float[2][n2];
    float angle = (float) (-2*Math.PI/(float)n);
    if (!forward) angle = -angle;
    for (int i=0; i<n2; i++) { 
      temp[0][i] = (float) Math.cos(i * angle);
      temp[1][i] = (float) Math.sin(i * angle);
    }
    float[][] y = FFT1D(x, temp);
    if (!forward) {
      for(int i=0; i<n; i++) {
        y[0][i] /= n;
        y[1][i] /= n; 
      }
    }
    return y; 
  }

  private static float[][] FFT1D(float[][] x, float[][] temp) {
    int n = x[0].length;
    int n2 = n/2;
    int k=0;
    int butterfly;
    int buttered=0; 
    if (n==1) {
      float[][] z1 = {{x[0][0]}, {x[1][0]}};
      return z1;
    }

    butterfly= (temp[0].length/n2);

    float[][] z = new float[2][n2];
    float[][] w = new float[2][n2];

    for (k=0; k<n/2; k++) {  
      int k2 = 2*k;
      z[0][k] = x[0][k2];
      z[1][k] = x[1][k2];
      w[0][k] = x[0][k2 + 1];
      w[1][k] = x[1][k2 + 1];
    }

    z = FFT1D(z, temp);
    w = FFT1D(w, temp);

    float[][] y = new float[2][n];
    for (k=0; k<n2;k++) {
      y[0][k] = z[0][k];
      y[1][k] = z[1][k];

      float re = w[0][k] * temp[0][buttered] - w[1][k] * temp[1][buttered];
      float im = w[0][k] * temp[1][buttered] + w[1][k] * temp[0][buttered];
      w[0][k] = re;
      w[1][k] = im;

      y[0][k] += w[0][k];
      y[1][k] += w[1][k];
      y[0][k + n2] = z[0][k] - w[0][k];
      y[1][k + n2] = z[1][k] - w[1][k];
      buttered += butterfly;
    }
    return y;
  }

  public static double[][] FFT1D(double[][] x, boolean forward)
         throws VisADException {
    if (x == null) return null;
    if (x.length != 2 || x[0].length != x[1].length) {
      throw new FieldException("bad x lengths");
    }
    int n = x[0].length;
    int n2 = 1;
    while (n2 < n) {
      n2 *= 2;
      if (n2 > n) {
        throw new FieldException("x length must be power of 2");
      }
    }
    n2 = n/2;
    double[][] temp = new double[2][n2];
    double angle = (double) (-2*Math.PI/(double)n);
    if (!forward) angle = -angle;
    for (int i=0; i<n2; i++) {
      temp[0][i] = (double) Math.cos(i * angle);
      temp[1][i] = (double) Math.sin(i * angle);
    }
    double[][] y = FFT1D(x, temp);
    if (!forward) {
      for(int i=0; i<n; i++) {
        y[0][i] /= n;
        y[1][i] /= n; 
      }
    }
    return y; 
  }

  private static double[][] FFT1D(double[][] x, double[][] temp) {
    int n = x[0].length;
    int n2 = n/2;
    int k=0;
    int butterfly;
    int buttered=0; 
    if (n==1) {
      double[][] z1 = {{x[0][0]}, {x[1][0]}};
      return z1;
    }

    butterfly= (temp[0].length/n2);

    double[][] z = new double[2][n2];
    double[][] w = new double[2][n2];

    for (k=0; k<n2; k++) {
      int k2 = 2 * k;
      z[0][k] = x[0][k2];
      z[1][k] = x[1][k2];
      w[0][k] = x[0][k2 + 1];
      w[1][k] = x[1][k2 + 1];
    }

    z = FFT1D(z, temp);
    w = FFT1D(w, temp);

    double[][] y = new double[2][n];
    for (k=0; k<n2;k++) {
      y[0][k] = z[0][k];
      y[1][k] = z[1][k];

      double re = w[0][k] * temp[0][buttered] - w[1][k] * temp[1][buttered];
      double im = w[0][k] * temp[1][buttered] + w[1][k] * temp[0][buttered];
      w[0][k] = re;
      w[1][k] = im;

      y[0][k] += w[0][k];
      y[1][k] += w[1][k];
      y[0][k + n2] = z[0][k] - w[0][k];
      y[1][k + n2] = z[1][k] - w[1][k];
      buttered += butterfly;
    }
    return y;
  }

  public static void main(String args[])
         throws VisADException {
    int n = 16;
    int rows = 1, cols = 1;
    boolean twod = false;

    if (args.length > 0) n = Integer.valueOf(args[0]).intValue();
    if (args.length > 1) {
      rows = Integer.valueOf(args[0]).intValue();
      cols = Integer.valueOf(args[1]).intValue();
      n = rows * cols;
      twod = true;
    }

    float[][] x = new float[2][n];
    // double[][] x = new double[2][n];
    System.out.println("  initial values");
    if (twod) {
      int i = 0;
      for (int c=0; c<cols; c++) {
        for (int r=0; r<rows; r++) {
          x[0][i] = (float) (Math.sin(2 * Math.PI * r / rows) *
                             Math.sin(2 * Math.PI * c / cols));
          x[1][i] = 0.0f;
          // x[0][i] = (Math.sin(2 * Math.PI * r / rows) *
          //            Math.sin(2 * Math.PI * c / cols));
          // x[1][i] = 0.0;
          System.out.println("x[" + r + "][" + c + "] = " +
                             PlotText.shortString(x[0][i]) + " " + 
                             PlotText.shortString(x[1][i]));
          i++;
        }
      }
    }
    else {
      for (int i=0; i<n; i++) {
        x[0][i] = (float) Math.sin(2 * Math.PI * i / n);
        x[1][i] = 0.0f;
        // x[0][i] = Math.sin(2 * Math.PI * i / n);
        // x[1][i] = 0.0;
        System.out.println("x[" + i + "] = " +
                           PlotText.shortString(x[0][i]) + " " +
                           PlotText.shortString(x[1][i]));
      }
    }
    x = twod ? FFT2D(rows, cols, x, true) : FFT1D(x, true);
    System.out.println("\n  fft");
    if (twod) {
      int i = 0;
      for (int c=0; c<cols; c++) {
        for (int r=0; r<rows; r++) {
          System.out.println("x[" + r + "][" + c + "] = " +
                             PlotText.shortString(x[0][i]) + " " + 
                             PlotText.shortString(x[1][i]));
          i++;
        }
      }
    }
    else {
      for (int i=0; i<n; i++) {
        System.out.println("x[" + i + "] = " +
                           PlotText.shortString(x[0][i]) + " " +
                           PlotText.shortString(x[1][i]));
      }
    }
    x = twod ? FFT2D(rows, cols, x, false) : FFT1D(x, false);
    System.out.println("\n  back fft");
    if (twod) {
      int i = 0;
      for (int c=0; c<cols; c++) {
        for (int r=0; r<rows; r++) {
          System.out.println("x[" + r + "][" + c + "] = " +
                             PlotText.shortString(x[0][i]) + " " + 
                             PlotText.shortString(x[1][i]));
          i++;
        }
      }
    }
    else {
      for (int i=0; i<n; i++) {
        System.out.println("x[" + i + "] = " +
                           PlotText.shortString(x[0][i]) + " " +
                           PlotText.shortString(x[1][i]));
      }
    }
  }
}

