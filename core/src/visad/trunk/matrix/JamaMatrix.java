//
// JamaMatrix.java
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

import Jama.Matrix;
import java.io.*;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import visad.*;

/**
 * JamaMatrix is a VisAD wrapper for JAMA matrices. This class requires the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA package</a>.
 */
public class JamaMatrix extends FlatField {

  private static final RealType wavelength =
    RealType.getRealType("wavelength");

  private static final RealType principal_component =
    RealType.getRealType("principal_component");

  private static final FunctionType matrixType = constructFunction();

  private static FunctionType constructFunction() {
    try {
      return new FunctionType(wavelength, principal_component);
    }
    catch (VisADException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This matrix's associated JAMA Matrix object.
   */
  private Matrix matrix;

  /**
   * Attempt to convert the given VisAD Data object to a VisAD JamaMatrix
   * Data object.
   *
   * @return The converted object, or null if it could not be converted
   */
  public static JamaMatrix convertToMatrix(Data data) {
    // TODO
    // check all sorts of crap, like domain dimensions, etc.
    // then unrasterize range values into two dimensions and pass
    // them to the Jama.Matrix constructor.  Have fun!
    if (!(data instanceof FlatField)) return null;
    if (data instanceof JamaMatrix) return (JamaMatrix) data;
    FlatField field = (FlatField) data;

    double[][] range = null;
    JamaMatrix m = null;
    try {
      range = field.getValues();
      m = new JamaMatrix();
    }
    catch (VisADException exc) { }

    return m;
  }

  /**
   * Construct a new JamaMatrix from the given JAMA Matrix.
   */
  public JamaMatrix(Matrix matrix) throws VisADException {
    this(null, null, null, null, null);
    setMatrix(matrix);
  }

  /**
   * Construct a new JamaMatrix from the given matrix entries.
   */
  public JamaMatrix(double[][] entries) throws VisADException {
    this(null, null, null, null, null);
    setMatrix(entries);
  }

  /**
   * Construct a new JamaMatrix with default domain set,
   * coordinate systems, range sets and units.
   */
  public JamaMatrix() throws VisADException {
    this(null, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix with the specified domain set,
   * coordinate systems, range sets and units.
   */
  public JamaMatrix(Set domain_set, CoordinateSystem range_coord_sys,
    CoordinateSystem[] range_coord_syses, Set[] range_sets, Unit[] units)
    throws VisADException
  {
    super(matrixType, domain_set, range_coord_sys, range_coord_syses,
      range_sets, units);
  }

  /**
   * Return the associated JAMA Matrix object.
   */
  public Matrix getMatrix() {
    return matrix;
  }

  /**
   * Set this matrix's samples to correspond to those of the given
   * JAMA Matrix.
   */
  public void setMatrix(Matrix matrix) throws VisADException {
    int rows = matrix.getRowDimension();
    int cols = matrix.getColumnDimension();
    double[][] entries = matrix.getArray();
    double[][] range = new double[1][rows * cols];
    for (int i=0; i<rows; i++) {
      for (int j=0; j<cols; j++) {
        range[0][rows * j + i] = entries[i][j];
      }
    }
    Integer2DSet set = new Integer2DSet(rows, cols);
    try {
      resample(set, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
      setSamples(range, false);
    }
    catch (RemoteException exc) { }
    this.matrix = matrix;
  }

  /**
   * Set this matrix's samples to correspond to the given entries.
   */
  public void setMatrix(double[][] entries) throws VisADException {
    setMatrix(new Matrix(entries));
  }


  // Overridden methods

  /**
   * Do not use setSamples; use setMatrix instead.
   * @see #setMatrix
   */
  public void setSamples(double[][] range, ErrorEstimate[] errors,
    boolean copy) throws VisADException, RemoteException
  {
    super.setSamples(range, errors, copy);
    matrix = null;
  }

  /**
   * Do not use setSamples; use setMatrix instead.
   * @see #setMatrix
   */
  public void setSamples(float[][] range, ErrorEstimate[] errors,
    boolean copy) throws VisADException, RemoteException
  {
    super.setSamples(range, errors, copy);
    matrix = null;
  }


  // Method wrappers for JAMA Matrix functionality

  /**
   * Get row dimension.
   * @return     The number of rows
   * @exception  VisADException No matrix
   */
  public int getRowDimension() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.getRowDimension();
  }

  /**
   * Get column dimension.
   * @return     The number of columns
   * @exception  VisADException No matrix
   */
  public int getColumnDimension() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.getColumnDimension();
  }

  /**
   * Get a single element.
   * @param i    Row index
   * @param j    Column index
   * @return     A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   * @exception  VisADException No matrix
   */
  public double get(int i, int j) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.get(i, j);
  }

  /**
   * Get a submatrix.
   * @param i0   Initial row index
   * @param i1   Final row index
   * @param j0   Initial column index
   * @param j1   Final column index
   * @return     A(i0:i1,j0:j1)
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public JamaMatrix getMatrix(int i0, int i1, int j0, int j1)
    throws VisADException
  {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix m = matrix.getMatrix(i0, i1, j0, j1);
    return new JamaMatrix(m);
  }

  /**
   * Get a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @return     A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public JamaMatrix getMatrix(int[] r, int[] c) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix m = matrix.getMatrix(r, c);
    return new JamaMatrix(m);
  }

  /**
   * Get a submatrix.
   * @param i0   Initial row index
   * @param i1   Final row index
   * @param c    Array of column indices
   * @return     A(i0:i1,c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public JamaMatrix getMatrix(int i0, int i1, int[] c) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix m = matrix.getMatrix(i0, i1, c);
    return new JamaMatrix(m);
  }

  /**
   * Get a submatrix.
   * @param r    Array of row indices
   * @param i0   Initial column index
   * @param i1   Final column index
   * @return     A(r(:),j0:j1)
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public JamaMatrix getMatrix(int[] r, int j0, int j1) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix!");
    Matrix m = matrix.getMatrix(r, j0, j1);
    return new JamaMatrix(m);
  }

  /**
   * Set a single element.
   * @param i    Row index
   * @param j    Column index
   * @param s    A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   * @exception  VisADException No matrix
   */
  public void set(int i, int j, double s) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    matrix.set(i, j, s);
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param i0   Initial row index
   * @param i1   Final row index
   * @param j0   Initial column index
   * @param j1   Final column index
   * @param X    A(i0:i1,j0:j1)
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public void setMatrix(int i0, int i1, int j0, int j1, JamaMatrix X)
    throws VisADException
  {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Xm = X.getMatrix();
    if (Xm == null) throw new VisADException("X: No matrix");
    matrix.setMatrix(i0, i1, j0, j1, Xm);
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @param X    A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public void setMatrix(int[] r, int[] c, JamaMatrix X) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Xm = X.getMatrix();
    if (Xm == null) throw new VisADException("X: No matrix");
    matrix.setMatrix(r, c, Xm);
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param r    Array of row indices
   * @param j0   Initial column index
   * @param j1   Final column index
   * @param X    A(r(:),j0:j1)
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public void setMatrix(int[] r, int j0, int j1, JamaMatrix X)
    throws VisADException
  {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Xm = X.getMatrix();
    if (Xm == null) throw new VisADException("X: No matrix");
    matrix.setMatrix(r, j0, j1, Xm);
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param i0   Initial row index
   * @param i1   Final row index
   * @param c    Array of column indices
   * @param X    A(i0:i1,c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   * @exception  VisADException No matrix
   */
  public void setMatrix(int i0, int i1, int[] c, JamaMatrix X)
    throws VisADException
  {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Xm = X.getMatrix();
    if (Xm == null) throw new VisADException("X: No matrix");
    matrix.setMatrix(i0, i1, c, Xm);
    setMatrix(matrix);
  }

  /**
   * Matrix transpose.
   * @return    A'
   * @exception VisADException No matrix
   */
  public JamaMatrix transpose() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix m = matrix.transpose();
    return new JamaMatrix(m);
  }

  /**
   * One norm.
   * @return    maximum column sum
   * @exception VisADException No matrix
   */
  public double norm1() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.norm1();
  }

  /**
   * Two norm.
   * @return    maximum singular value
   * @exception VisADException No matrix
   */
  public double norm2() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.norm2();
  }

  /**
   * Infinity norm.
   * @return    maximum row sum
   * @exception VisADException No matrix
   */
  public double normInf() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.normInf();
  }

  /**
   * Frobenius norm.
   * @return    sqrt of sum of squares of all elements
   * @exception VisADException No matrix
   */

  public double normF() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.normF();
  }

  /**
   * Unary minus.
   * @return    -A
   * @exception VisADException No matrix
   */
  public JamaMatrix uminus() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix m = matrix.uminus();
    return new JamaMatrix(m);
  }

  /**
   * C = A + B
   * @param B    another matrix
   * @return     A + B
   * @exception  VisADException No matrix
   */
  public JamaMatrix plus(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.plus(Bm);
    return new JamaMatrix(m);
  }

  /**
   * A = A + B
   * @param B    another matrix
   * @return     A + B
   * @exception  VisADException No matrix
   */
  public JamaMatrix plusEquals(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    matrix.plusEquals(Bm);
    setMatrix(matrix);
    return this;
  }

  /**
   * C = A - B
   * @param B    another matrix
   * @return     A - B
   * @exception  VisADException No matrix
   */
  public JamaMatrix minus(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.minus(Bm);
    return new JamaMatrix(m);
  }

  /**
   * A = A - B
   * @param B    another matrix
   * @return     A - B
   * @exception  VisADException No matrix
   */
  public JamaMatrix minusEquals(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    matrix.minusEquals(Bm);
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element multiplication, C = A.*B
   * @param B    another matrix
   * @return     A.*B
   * @exception  VisADException No matrix
   */
  public JamaMatrix arrayTimes(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.arrayTimes(Bm);
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element multiplication in place, A = A.*B
   * @param B    another matrix
   * @return     A.*B
   * @exception  VisADException No matrix
   */
  public JamaMatrix arrayTimesEquals(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    matrix.arrayTimesEquals(Bm);
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element right division, C = A./B
   * @param B    another matrix
   * @return     A./B
   * @exception  VisADException No matrix
   */
  public JamaMatrix arrayRightDivide(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.arrayRightDivide(Bm);
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element right division in place, A = A./B
   * @param B    another matrix
   * @return     A./B
   * @exception  VisADException No matrix
   */
  public JamaMatrix arrayRightDivideEquals(JamaMatrix B)
    throws VisADException
  {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    matrix.arrayRightDivideEquals(Bm);
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element left division, C = A.\B
   * @param B    another matrix
   * @return     A.\B
   * @exception  VisADException No matrix
   */
  public JamaMatrix arrayLeftDivide(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.arrayLeftDivide(Bm);
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element left division in place, A = A.\B
   * @param B    another matrix
   * @return     A.\B
   * @exception  VisADException No matrix
   */
  public JamaMatrix arrayLeftDivideEquals(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    matrix.arrayLeftDivideEquals(Bm);
    setMatrix(matrix);
    return this;
  }

  /**
   * Multiply a matrix by a scalar, C = s*A
   * @param s    scalar
   * @return     s*A
   * @exception  VisADException No matrix
   */
  public JamaMatrix times(double s) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix m = matrix.times(s);
    return new JamaMatrix(m);
  }

  /**
   * Multiply a matrix by a scalar in place, A = s*A
   * @param s    scalar
   * @return     replace A by s*A
   * @exception  VisADException No matrix
   */
  public JamaMatrix timesEquals(double s) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    matrix.times(s);
    setMatrix(matrix);
    return this;
  }

  /**
   * Linear algebraic matrix multiplication, A * B
   * @param B    another matrix
   * @return     Matrix product, A * B
   * @exception  IllegalArgumentException Matrix inner dimensions must agree
   * @exception  VisADException No matrix
   */
  public JamaMatrix times(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.times(Bm);
    return new JamaMatrix(m);
  }

  /**
   * Solve A*X = B
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   * @exception  VisADException No matrix
   */
  public JamaMatrix solve(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.solve(Bm);
    return new JamaMatrix(m);
  }

  /**
   * Solve X*A = B, which is also A'*X' = B'
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   * @exception  VisADException No matrix
   */
  public JamaMatrix solveTranspose(JamaMatrix B) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix Bm = B.getMatrix();
    if (Bm == null) throw new VisADException("B: No matrix");
    Matrix m = matrix.solveTranspose(Bm);
    return new JamaMatrix(m);
  }

  /**
   * Matrix inverse or pseudoinverse.
   * @return     inverse(A) if A is square, pseudoinverse otherwise
   * @exception  VisADException No matrix
   */
  public JamaMatrix inverse() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    Matrix m = matrix.inverse();
    return new JamaMatrix(m);
  }

  /**
   * Matrix determinant.
   * @return     determinant
   * @exception  VisADException No matrix
   */
  public double det() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.det();
  }

  /**
   * Matrix rank.
   * @return     effective numerical rank, obtained from SVD
   * @exception  VisADException No matrix
   */
  public int rank() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.rank();
  }

  /**
   * Matrix condition (2 norm).
   * @return     ratio of largest to smallest singular value
   * @exception  VisADException No matrix
   */
  public double cond() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.cond();
  }

  /**
   * Matrix trace.
   * @return     sum of the diagonal elements
   * @exception  VisADException No matrix
   */
  public double trace() throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    return matrix.trace();
  }

  /**
   * Generate matrix with random elements.
   * @param m    Number of rows
   * @param n    Number of colums
   * @return     An m-by-n matrix with uniformly distributed random elements
   * @exception  VisADException Problem constructing JamaMatrix
   */
  public static JamaMatrix random(int m, int n) throws VisADException {
    Matrix random = Matrix.random(m, n);
    return new JamaMatrix(random);
  }

  /**
   * Generate identity matrix.
   * @param m    Number of rows
   * @param n    Number of colums
   * @return     An m-by-n matrix with ones on the diagonal and zeros elsewhere
   * @exception  VisADException Problem constructing JamaMatrix
   */
  public static JamaMatrix identity(int m, int n) throws VisADException {
    Matrix identity = Matrix.identity(m, n);
    return new JamaMatrix(identity);
  }

  /**
   * Print the matrix to stdout.   Line the elements up in columns
   * with a Fortran-like 'Fw.d' style format
   * @param w    Column width
   * @param d    Number of digits after the decimal
   * @exception  VisADException No matrix
   */
  public void print(int w, int d) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    matrix.print(w, d);
  }

  /**
   * Print the matrix to the output stream.   Line the elements up in
   * columns with a Fortran-like 'Fw.d' style format.
   * @param output Output stream
   * @param w      Column width
   * @param d      Number of digits after the decimal
   * @exception    VisADException No matrix
   */
  public void print(PrintWriter output, int w, int d) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    matrix.print(output, w, d);
  }

  /**
   * Print the matrix to stdout.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param format Formatting object for individual elements
   * @param width  Field width for each column
   * @exception    VisADException No matrix
   */
  public void print(NumberFormat format, int width) throws VisADException {
    if (matrix == null) throw new VisADException("No matrix");
    matrix.print(format, width);
  }

  /**
   * Print the matrix to the output stream.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param output the output stream
   * @param format A formatting object to format the matrix elements 
   * @param width  Column width
   * @exception    VisADException No matrix
   */
  public void print(PrintWriter output, NumberFormat format, int width)
    throws VisADException
  {
    if (matrix == null) throw new VisADException("No matrix");
    matrix.print(output, format, width);
  }

  /**
   * Read a matrix from a stream.  The format is the same the print method,
   * so printed matrices can be read back in.  Elements are separated by
   * whitespace, all the elements for each row appear on a single line,
   * the last row is followed by a blank line.
   * @param input the input stream
   * @exception   VisADException Problem constructing JamaMatrix
   */
  public static JamaMatrix read(BufferedReader input)
    throws IOException, VisADException
  {
    Matrix m = Matrix.read(input);
    return new JamaMatrix(m);
  }

}

