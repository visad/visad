//
// FFT.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.math;

import visad.*;
import visad.util.*;
import visad.data.mcidas.*;
import visad.java3d.*;
import visad.bom.*;

import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 FFT is the VisAD class for Fourier Transforms, using
 the Fast Fourier Transform when the domain length is
 a power of two.<p>
*/

public class FFT {

  /** 
   * for use by SpreadSheet only - ordinary applications
   * should use other method signatures;
   * invoke in SpreadSheet by:
   * link(visad.math.FFT.forwardFT(A1))
  */
  public static FlatField forwardFT(Data[] datums) {
    FlatField result = null;
    try {
      result = fourierTransform((Field) datums[0], true);
    }
    catch (VisADException e) {
      e.printStackTrace();
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    if (result == null) {
      System.out.println("result == null");
    }
    return result;
  }

  /**
   * for use by SpreadSheet only - ordinary applications
   * should use other method signatures;
   * invoke in SpreadSheet by:
   * link(visad.math.FFT.backwardFT(A1))
  */
  public static FlatField backwardFT(Data[] datums) {
    FlatField result = null;
    try {
      result = fourierTransform((Field) datums[0], false);
    }
    catch (VisADException e) {
      e.printStackTrace();
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    if (result == null) {
      System.out.println("result == null");
    }
    return result;
  }

  /**
   * return Fourier Transform of field, use FFT if domain dimension(s)
   * are powers of 2
   * @param field Field with domain dimension = 1 (1-D FT) or 2 (2-D FT)
   *              and 1 (real part) or 2 (real & imaginary) range RealTypes
   * @param forward true for forward and false for backward
   * @return Fourier transform of field
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public static FlatField fourierTransform(Field field, boolean forward)
         throws VisADException, RemoteException {
    return fourierTransform(field, forward, null, null, null, null, null);
  }

  /**
   * return Fourier Transform of field, use FFT if domain dimension(s)
   * are powers of 2
   * @param field Field with domain dimension = 1 (1-D FT) or 2 (2-D FT)
   *              and 1 (real part) or 2 (real & imaginary) range RealTypes
   * @param forward true for forward and false for backward
   * @param ftype use for return Field (may be null)
   * @param domain_set use for return Field (may be null)
   * @param range_coord_sys use for return Field (may be null)
   * @param range_sets use for return Field (may be null)
   * @param units use for return Field (may be null)
   * @return Fourier transform of field
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public static FlatField fourierTransform(Field field, boolean forward,
                     FunctionType ftype, GriddedSet domain_set,
                     CoordinateSystem range_coord_sys,
                     Set[] range_sets, Unit[] units)
         throws VisADException, RemoteException {
    if (field == null) return null;
    FunctionType type = (FunctionType) field.getType();
    RealTupleType dtype = type.getDomain();
    int ddim = dtype.getDimension();
    MathType rtype = type.getRange();
    RealType[] realComponents = null;
    int rdim = 0;
    if (rtype instanceof RealType) {
      realComponents = new RealType[] {(RealType) rtype};
      rdim = 1;
    }
    else if (rtype instanceof RealTupleType) {
      realComponents = ((RealTupleType) rtype).getRealComponents();
      rdim = realComponents.length;
    }
    else {
      throw new FieldException("bad range type " + rtype);
    }
    if (ddim != 1 && ddim != 2) {
      throw new FieldException("bad domain dimension " + dtype);
    }
    if (rdim != 1 && rdim != 2) {
      throw new FieldException("bad range dimension " + rtype);
    }

    if (units == null || units.length == 0) {
      units = new Unit[] {null, null};
    }
    else if (units.length == 1) {
      units = new Unit[] {units[0], null};
    }

    if (ftype == null) {
      RealType[] newReals = new RealType[2];
      if (rdim == 2) {
        if (forward) {
          String name = realComponents[0].getName() + "_FT";
          newReals[0] = RealType.getRealType(name, units[0], null);
          name = realComponents[1].getName() + "_FT";
          newReals[1] = RealType.getRealType(name, units[1], null);
        }
        else {
          String name = realComponents[0].getName();
          if (name.endsWith("_FT")) {
            name = name.substring(0, name.length() - 3);
          }
          else if (name.endsWith("_FT_real")) { 
            name = name.substring(0, name.length() - 8);
          }
          else if (name.endsWith("_RFT_real")) { 
            name = name.substring(0, name.length() - 9);
          }
          newReals[0] = RealType.getRealType(name, units[0], null);
          name = realComponents[1].getName();
          if (name.endsWith("_FT")) {
            name = name.substring(0, name.length() - 3);
          }
          else if (name.endsWith("_FT_imag")) { 
            name = name.substring(0, name.length() - 8) + "_imag";
          }
          else if (name.endsWith("_RFT_imag")) { 
            name = name.substring(0, name.length() - 9) + "_imag";
          }
          newReals[1] = RealType.getRealType(name, units[1], null);
        }
      }
      else { // rdim == 1
        if (forward) {
          String name = realComponents[0].getName() + "_FT";
          newReals[0] = RealType.getRealType(name + "_real", units[0], null);
          newReals[1] = RealType.getRealType(name + "_imag", units[1], null);
        }
        else {
          String name = realComponents[0].getName() + "_RFT";
          newReals[0] = RealType.getRealType(name + "_real", units[0], null);
          newReals[1] = RealType.getRealType(name + "_imag", units[1], null);
        }
      }
      RealTupleType rftype = new RealTupleType(newReals, range_coord_sys, null);
      ftype = new FunctionType(dtype, rftype);
    }
    else { // ftype != null
      RealTupleType dftype = ftype.getDomain();
      if (dftype.getDimension() != ddim) {
        throw new FieldException("bad domain dimension " + dftype);
      }
      MathType rftype = ftype.getRange();
      if (!(rftype instanceof RealTupleType) ||
          ((RealTupleType) rftype).getDimension() != 2) {
        throw new FieldException("bad range type " + rftype);
      }
    }

    Set field_set = field.getDomainSet();
    if (!(field_set instanceof GriddedSet)) {
      throw new FieldException("field domain set must be Gridded2DSet " + field_set);
    }

    int[] field_lens = ((GriddedSet) field_set).getLengths();
    if (domain_set == null) {
      domain_set = (GriddedSet) field_set;
    }
    else {
      if (domain_set.getDimension() != ddim) {
        throw new FieldException("domain_set bad dimension " + domain_set);
      }
      int[] domain_lens = domain_set.getLengths();
      for (int i=0; i<ddim; i++) {
        if (field_lens[i] != domain_lens[i]) {
          throw new FieldException("domain_set size must match field domain " +
                                   "set " + domain_set + "\n" + field_set);
        }
      }
    }

    boolean use_double = true;
    if (field instanceof FlatField) {
      use_double = false;
      Set[] fsets = ((FlatField) field).getRangeSets();
      for (int i=0; i<fsets.length; i++) {
        if (fsets[i] instanceof DoubleSet) use_double = true;
      }
    }
    boolean doub = false;
    if (range_sets != null) {
      if (range_sets.length != 2) {
        throw new FieldException("bad range_sets length" + range_sets.length);
      }
      for (int i=0; i<2; i++) {
        if (range_sets[i] instanceof DoubleSet) doub = true;
      }
    }
    use_double = use_double && doub;

    FlatField new_field = new FlatField(ftype, domain_set, range_coord_sys,
                                        null, range_sets, units);
    if (use_double) {
      double[][] values = field.getValues(false);
      if (values.length == 1) {
        int n = values[0].length;
        double[][] new_values = new double[2][n];
        System.arraycopy(values[0], 0, new_values[0], 0, n);
        for (int i=0; i<n; i++) new_values[1][i] = 0.0;
        values = new_values;
      }
      if (ddim == 1) {
        values = FT1D(values, forward);
      }
      else { // ddim == 2
        values = FT2D(field_lens[0], field_lens[1], values, forward);
      }
      new_field.setSamples(values, false);
    }
    else { // !use_double
      float[][] values = field.getFloats(false);
      if (values.length == 1) {
        int n = values[0].length;
        float[][] new_values = new float[2][n];
        System.arraycopy(values[0], 0, new_values[0], 0, n);
        for (int i=0; i<n; i++) new_values[1][i] = 0.0f;
        values = new_values;
      }
      if (ddim == 1) {
        values = FT1D(values, forward);
      }
      else { // ddim == 2
        values = FT2D(field_lens[0], field_lens[1], values, forward);
      }
      new_field.setSamples(values, false);
    }
    return new_field;
  }


  /**
   * compute 2-D Fourier transform, calling 1-D FT twice
   * use FFT if rows and cols are powers of 2
   * @param rows first dimension for 2-D
   * @param cols second dimension for 2-D
   * @param x array for take Fourier transform of, dimensioned
   *          [2][length] where length = rows * cols, and the
   *          first index (2) is over real & imaginary parts
   * @param forward true for forward and false for backward
   * @return Fourier transform of x
   * @throws VisADException  a VisAD error occurred
   */
  public static float[][] FT2D(int rows, int cols, float[][] x,
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
      z = FT1D(z, forward);
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
      v = FT1D(v, forward);
      for (int c=0; c<cols; c++) {
        u[0][i + c * rows] = v[0][c];
        u[1][i + c * rows] = v[1][c];
      }
    }
    return u;
  }

  /**
   * compute 2-D Fourier transform, calling 1-D FT twice
   * use FFT if rows and cols are powers of 2
   * @param rows first dimension for 2-D
   * @param cols second dimension for 2-D
   * @param x array for take Fourier transform of, dimensioned
   *          [2][length] where length = rows * cols, and the
   *          first index (2) is over real & imaginary parts
   * @param forward true for forward and false for backward
   * @return Fourier transform of x
   * @throws VisADException  a VisAD error occurred
   */
  public static double[][] FT2D(int rows, int cols, double[][] x,
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
      z = FT1D(z, forward);
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
      v = FT1D(v, forward);
      for (int c=0; c<cols; c++) {
        u[0][i + c * rows] = v[0][c];
        u[1][i + c * rows] = v[1][c];
      }
    }
    return u;
  }

  /**
   * compute 1-D Fourier transform
   * use FFT if length (2nd dimension of x) is a power of 2
   * @param x array for take Fourier transform of, dimensioned
   *          [2][length], the first index (2) is over real &
   *          imaginary parts
   * @param forward true for forward and false for backward
   * @return Fourier transform of x
   * @throws VisADException  a VisAD error occurred
   */
  public static float[][] FT1D(float[][] x, boolean forward)
         throws VisADException {
    if (x == null) return null;
    if (x.length != 2 || x[0].length != x[1].length) {
      throw new FieldException("bad x lengths");
    }
    int n = x[0].length;
    int n2 = 1;
    boolean fft = true;
    while (n2 < n) {
      n2 *= 2;
      if (n2 > n) {
        fft = false;
      }
    }
    if (fft) return FFT1D(x, forward);

    float[][] temp = new float[2][n];
    float angle = (float) (-2.0 * Math.PI / n);
    if (!forward) angle = -angle;
    for (int i=0; i<n; i++) {
      temp[0][i] = (float) Math.cos(i * angle);
      temp[1][i] = (float) Math.sin(i * angle);
    }
    float[][] y = new float[2][n];
    for (int i=0; i<n; i++) {
      float re = 0.0f;
      float im = 0.0f;
      for (int j=0; j<n; j++) {
        int m = (i * j) % n;
        re += x[0][j] * temp[0][m] - x[1][j] * temp[1][m];
        im += x[0][j] * temp[1][m] + x[1][j] * temp[0][m];
      }
      y[0][i] = re;
      y[1][i] = im;
    }
    if (!forward) {
      for(int i=0; i<n; i++) {
        y[0][i] /= n;
        y[1][i] /= n;
      }
    }
    return y;
  }

  /**
   * compute 1-D FFT transform
   * length (2nd dimension of x) must be a power of 2
   * @param x array for take Fourier transform of, dimensioned
   *          [2][length], the first index (2) is over real &
   *          imaginary parts
   * @param forward true for forward and false for backward
   * @return Fourier transform of x
   * @throws VisADException  a VisAD error occurred
   */
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
    float angle = (float) (-2.0 * Math.PI / n);
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

  /** inner function for 1-D Fast Fourier Transform */
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

  /**
   * compute 1-D Fourier transform
   * use FFT if length (2nd dimension of x) is a power of 2
   * @param x array for take Fourier transform of, dimensioned
   *          [2][length], the first index (2) is over real &
   *          imaginary parts
   * @param forward true for forward and false for backward
   * @return Fourier transform of x
   * @throws VisADException  a VisAD error occurred
   */
  public static double[][] FT1D(double[][] x, boolean forward)
         throws VisADException {
    if (x == null) return null;
    if (x.length != 2 || x[0].length != x[1].length) {
      throw new FieldException("bad x lengths");
    }
    int n = x[0].length;
    int n2 = 1;
    boolean fft = true;
    while (n2 < n) {
      n2 *= 2;
      if (n2 > n) {
        fft = false;
      }
    }
    if (fft) return FFT1D(x, forward);

    double[][] temp = new double[2][n];
    double angle = -2.0 * Math.PI / n;
    if (!forward) angle = -angle;
    for (int i=0; i<n; i++) {
      temp[0][i] = Math.cos(i * angle);
      temp[1][i] = Math.sin(i * angle);
    }
    double[][] y = new double[2][n];
    for (int i=0; i<n; i++) {
      double re = 0.0;
      double im = 0.0;
      for (int j=0; j<n; j++) {
        int m = (i * j) % n;
        re += x[0][j] * temp[0][m] - x[1][j] * temp[1][m];
        im += x[0][j] * temp[1][m] + x[1][j] * temp[0][m];
      }
      y[0][i] = re;
      y[1][i] = im;
    }
    if (!forward) {
      for(int i=0; i<n; i++) {
        y[0][i] /= n;
        y[1][i] /= n;
      }
    }
    return y;
  }

  /**
   * compute 1-D FFT transform
   * length (2nd dimension of x) must be a power of 2
   * @param x array for take Fourier transform of, dimensioned
   *          [2][length], the first index (2) is over real &
   *          imaginary parts
   * @param forward true for forward and false for backward
   * @return Fourier transform of x
   * @throws VisADException  a VisAD error occurred
   */
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
    double angle = (double) (-2.0 * Math.PI / n);
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

  /** inner function for 1-D Fast Fourier Transform */
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

  /** test Fourier Transform methods */
  public static void main(String args[])
         throws VisADException {
    int n = 16;
    int rows = 1, cols = 1;
    boolean twod = false;

/*
    if (args.length > 0 && args[0].startsWith("AREA")) {
      DisplayImpl display1 = new DisplayImplJ3D("display");
      DisplayImpl display1 = new DisplayImplJ3D("display");
      AreaAdapter areaAdapter = new AreaAdapter(args[0]);
      Data image = areaAdapter.getData();
      FunctionType imageFunctionType = (FunctionType) image.getType();
      RealType radianceType = (RealType)
        ((RealTupleType) imageFunctionType.getRange()).getComponent(0);
      display.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
      display.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
      ScalarMap rgbMap = new ScalarMap(radianceType, Display.RGB);
      display.addMap(rgbMap);

    }
*/

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
    x = twod ? FT2D(rows, cols, x, true) : FT1D(x, true);
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
    x = twod ? FT2D(rows, cols, x, false) : FT1D(x, false);
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

