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

  private static final RealType coefficient =
    RealType.getRealType("coefficient");

  private static final FunctionType matrixType = constructFunction();

  private static FunctionType constructFunction() {
    try {
      RealTupleType tuple = new RealTupleType(wavelength, principal_component);
      FunctionType function = new FunctionType(tuple, coefficient);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  /**
   * This matrix's associated JAMA Matrix object.
   */
  private Matrix matrix;


  // Static methods

  /**
   * Attempt to convert the given VisAD Data object to a VisAD JamaMatrix
   * Data object.
   * @return The converted object, or null if it could not be converted
   */
  public static JamaMatrix convertToMatrix(Data data) {
    // if data is already a JamaMatrix the task is trivial
    if (data instanceof JamaMatrix) return (JamaMatrix) data;

    // data must be a flat field
    if (!(data instanceof FlatField)) return null;
    FlatField field = (FlatField) data;

    // get domain set (must be 2-D ordered samples)
    Set set = field.getDomainSet();
    if (!(set instanceof Gridded2DSet)) return null;
    Gridded2DSet grid = (Gridded2DSet) set;

    // construct matrix entry array
    int[] dims = grid.getLengths();
    int rows = dims[0];
    int cols = dims[1];
    double[][] entries = new double[rows][cols];

    // get range values
    double[][] range;
    try {
      range = field.getValues(false);
    }
    catch (VisADException exc) {
      return null;
    }

    // unrasterize range values into matrix entry array
    for (int i=0; i<rows; i++) {
      for (int j=0; j<cols; j++) {
        entries[i][j] = range[0][cols * i + j];
      }
    }

    // construct the new JamaMatrix object and return it
    try {
      return new JamaMatrix(entries);
    }
    catch (VisADException exc) {
      return null;
    }
  }

  /**
   * Test the JamaMatrix class.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException
  {
    double[][] e1 = { {4, 4, 4},
                      {3, 3, 3},
                      {2, 2, 2} };
    JamaMatrix m1 = new JamaMatrix(e1);
    double[][] e2 = { {5, 5, 5},
                      {6, 6, 6},
                      {7, 7, 7} };
    JamaMatrix m2 = new JamaMatrix(e2);
    JamaMatrix m3 = convertToMatrix(m1.add(m2));
    JamaMatrix m4 = m1.plus(m2);
    System.out.print("m1:");
    m1.print(1, 0);
    System.out.print("m2:");
    m2.print(1, 0);
    System.out.print("m3 = m1 + m2 (VisAD):");
    m3.print(1, 0);
    System.out.print("m4 = m1 + m2 (JAMA):");
    m4.print(1, 0);
  }


  // Constructors

  /**
   * Constructs a domain set appropriate for the given matrix.
   * @return An Integer2DSet with dimensions equal to those of the matrix
   */
  private static Integer2DSet getDomainSet(Matrix matrix) {
    int rows = matrix.getRowDimension();
    int cols = matrix.getColumnDimension();
    try {
      return new Integer2DSet(rows, cols);
    }
    catch (VisADException exc) {
      return null;
    }
  }

  /**
   * Construct a new JamaMatrix from the given matrix dimensions.
   */
  public JamaMatrix(int rows, int cols) throws VisADException {
    this(new Matrix(rows, cols), null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the given matrix entries.
   */
  public JamaMatrix(double[][] entries) throws VisADException {
    this(new Matrix(entries), null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the given JAMA Matrix.
   */
  public JamaMatrix(Matrix matrix) throws VisADException {
    this(matrix, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix with the specified domain set,
   * coordinate systems, range sets and units.
   */
  public JamaMatrix(Matrix matrix, CoordinateSystem range_coord_sys,
    CoordinateSystem[] range_coord_syses, Set[] range_sets, Unit[] units)
    throws VisADException
  {
    super(matrixType, getDomainSet(matrix), range_coord_sys,
      range_coord_syses, range_sets, units);
    setMatrix(matrix);
  }


  // New methods

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
    // convert matrix entries into range sample values
    int rows = matrix.getRowDimension();
    int cols = matrix.getColumnDimension();
    double[][] entries = matrix.getArray();
    double[][] range = new double[1][rows * cols];
    for (int i=0; i<rows; i++) {
      for (int j=0; j<cols; j++) {
        range[0][cols * i + j] = entries[i][j];
      }
    }

    // set range samples to new values
    try {
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


  // Method wrappers for JAMA Matrix functionality

  /**
   * Get row dimension.
   * @return     The number of rows
   */
  public int getRowDimension() {
    return matrix.getRowDimension();
  }

  /**
   * Get column dimension.
   * @return     The number of columns
   */
  public int getColumnDimension() {
    return matrix.getColumnDimension();
  }

  /**
   * Get a single element.
   * @param i    Row index
   * @param j    Column index
   * @return     A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   */
  public double get(int i, int j) {
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
   */
  public JamaMatrix getMatrix(int i0, int i1, int j0, int j1)
    throws VisADException
  {
    Matrix m = matrix.getMatrix(i0, i1, j0, j1);
    return new JamaMatrix(m);
  }

  /**
   * Get a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @return     A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public JamaMatrix getMatrix(int[] r, int[] c) throws VisADException {
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
   */
  public JamaMatrix getMatrix(int i0, int i1, int[] c) throws VisADException {
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
   */
  public JamaMatrix getMatrix(int[] r, int j0, int j1) throws VisADException {
    Matrix m = matrix.getMatrix(r, j0, j1);
    return new JamaMatrix(m);
  }

  /**
   * Set a single element.
   * @param i    Row index
   * @param j    Column index
   * @param s    A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   */
  public void set(int i, int j, double s) throws VisADException {
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
   */
  public void setMatrix(int i0, int i1, int j0, int j1, JamaMatrix X)
    throws VisADException
  {
    matrix.setMatrix(i0, i1, j0, j1, X.getMatrix());
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @param X    A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public void setMatrix(int[] r, int[] c, JamaMatrix X) throws VisADException {
    matrix.setMatrix(r, c, X.getMatrix());
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param r    Array of row indices
   * @param j0   Initial column index
   * @param j1   Final column index
   * @param X    A(r(:),j0:j1)
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public void setMatrix(int[] r, int j0, int j1, JamaMatrix X)
    throws VisADException
  {
    matrix.setMatrix(r, j0, j1, X.getMatrix());
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param i0   Initial row index
   * @param i1   Final row index
   * @param c    Array of column indices
   * @param X    A(i0:i1,c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public void setMatrix(int i0, int i1, int[] c, JamaMatrix X)
    throws VisADException
  {
    matrix.setMatrix(i0, i1, c, X.getMatrix());
    setMatrix(matrix);
  }

  /**
   * Matrix transpose.
   * @return    A'
   */
  public JamaMatrix transpose() throws VisADException {
    Matrix m = matrix.transpose();
    return new JamaMatrix(m);
  }

  /**
   * One norm.
   * @return    maximum column sum
   */
  public double norm1() {
    return matrix.norm1();
  }

  /**
   * Two norm.
   * @return    maximum singular value
   */
  public double norm2() {
    return matrix.norm2();
  }

  /**
   * Infinity norm.
   * @return    maximum row sum
   */
  public double normInf() {
    return matrix.normInf();
  }

  /**
   * Frobenius norm.
   * @return    sqrt of sum of squares of all elements
   */

  public double normF() {
    return matrix.normF();
  }

  /**
   * Unary minus.
   * @return    -A
   */
  public JamaMatrix uminus() throws VisADException {
    Matrix m = matrix.uminus();
    return new JamaMatrix(m);
  }

  /**
   * C = A + B
   * @param B    another matrix
   * @return     A + B
   */
  public JamaMatrix plus(JamaMatrix B) throws VisADException {
    Matrix m = matrix.plus(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * A = A + B
   * @param B    another matrix
   * @return     A + B
   */
  public JamaMatrix plusEquals(JamaMatrix B) throws VisADException {
    matrix.plusEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }

  /**
   * C = A - B
   * @param B    another matrix
   * @return     A - B
   */
  public JamaMatrix minus(JamaMatrix B) throws VisADException {
    Matrix m = matrix.minus(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * A = A - B
   * @param B    another matrix
   * @return     A - B
   */
  public JamaMatrix minusEquals(JamaMatrix B) throws VisADException {
    matrix.minusEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element multiplication, C = A.*B
   * @param B    another matrix
   * @return     A.*B
   */
  public JamaMatrix arrayTimes(JamaMatrix B) throws VisADException {
    Matrix m = matrix.arrayTimes(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element multiplication in place, A = A.*B
   * @param B    another matrix
   * @return     A.*B
   */
  public JamaMatrix arrayTimesEquals(JamaMatrix B) throws VisADException {
    matrix.arrayTimesEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element right division, C = A./B
   * @param B    another matrix
   * @return     A./B
   */
  public JamaMatrix arrayRightDivide(JamaMatrix B) throws VisADException {
    Matrix m = matrix.arrayRightDivide(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element right division in place, A = A./B
   * @param B    another matrix
   * @return     A./B
   */
  public JamaMatrix arrayRightDivideEquals(JamaMatrix B)
    throws VisADException
  {
    matrix.arrayRightDivideEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element left division, C = A.\B
   * @param B    another matrix
   * @return     A.\B
   */
  public JamaMatrix arrayLeftDivide(JamaMatrix B) throws VisADException {
    Matrix m = matrix.arrayLeftDivide(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element left division in place, A = A.\B
   * @param B    another matrix
   * @return     A.\B
   */
  public JamaMatrix arrayLeftDivideEquals(JamaMatrix B) throws VisADException {
    matrix.arrayLeftDivideEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }

  /**
   * Multiply a matrix by a scalar, C = s*A
   * @param s    scalar
   * @return     s*A
   */
  public JamaMatrix times(double s) throws VisADException {
    Matrix m = matrix.times(s);
    return new JamaMatrix(m);
  }

  /**
   * Multiply a matrix by a scalar in place, A = s*A
   * @param s    scalar
   * @return     replace A by s*A
   */
  public JamaMatrix timesEquals(double s) throws VisADException {
    matrix.times(s);
    setMatrix(matrix);
    return this;
  }

  /**
   * Linear algebraic matrix multiplication, A * B
   * @param B    another matrix
   * @return     Matrix product, A * B
   * @exception  IllegalArgumentException Matrix inner dimensions must agree
   */
  public JamaMatrix times(JamaMatrix B) throws VisADException {
    Matrix m = matrix.times(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * Solve A*X = B
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
  public JamaMatrix solve(JamaMatrix B) throws VisADException {
    Matrix m = matrix.solve(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * Solve X*A = B, which is also A'*X' = B'
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
  public JamaMatrix solveTranspose(JamaMatrix B) throws VisADException {
    Matrix m = matrix.solveTranspose(B.getMatrix());
    return new JamaMatrix(m);
  }

  /**
   * Matrix inverse or pseudoinverse.
   * @return     inverse(A) if A is square, pseudoinverse otherwise
   */
  public JamaMatrix inverse() throws VisADException {
    Matrix m = matrix.inverse();
    return new JamaMatrix(m);
  }

  /**
   * Matrix determinant.
   * @return     determinant
   */
  public double det() {
    return matrix.det();
  }

  /**
   * Matrix rank.
   * @return     effective numerical rank, obtained from SVD
   */
  public int rank() {
    return matrix.rank();
  }

  /**
   * Matrix condition (2 norm).
   * @return     ratio of largest to smallest singular value
   */
  public double cond() {
    return matrix.cond();
  }

  /**
   * Matrix trace.
   * @return     sum of the diagonal elements
   */
  public double trace() {
    return matrix.trace();
  }

  /**
   * Generate matrix with random elements.
   * @param m    Number of rows
   * @param n    Number of colums
   * @return     An m-by-n matrix with uniformly distributed random elements
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
   */
  public void print(int w, int d) {
    matrix.print(w, d);
  }

  /**
   * Print the matrix to the output stream.   Line the elements up in
   * columns with a Fortran-like 'Fw.d' style format.
   * @param output Output stream
   * @param w      Column width
   * @param d      Number of digits after the decimal
   */
  public void print(PrintWriter output, int w, int d) {
    matrix.print(output, w, d);
  }

  /**
   * Print the matrix to stdout.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param format Formatting object for individual elements
   * @param width  Field width for each column
   */
  public void print(NumberFormat format, int width) {
    matrix.print(format, width);
  }

  /**
   * Print the matrix to the output stream.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param output the output stream
   * @param format A formatting object to format the matrix elements 
   * @param width  Column width
   */
  public void print(PrintWriter output, NumberFormat format, int width) {
    matrix.print(output, format, width);
  }

  /**
   * Read a matrix from a stream.  The format is the same the print method,
   * so printed matrices can be read back in.  Elements are separated by
   * whitespace, all the elements for each row appear on a single line,
   * the last row is followed by a blank line.
   * @param input the input stream
   */
  public static JamaMatrix read(BufferedReader input)
    throws IOException, VisADException
  {
    Matrix m = Matrix.read(input);
    return new JamaMatrix(m);
  }

}

