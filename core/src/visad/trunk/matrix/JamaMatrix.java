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

import java.lang.reflect.*;
import java.io.*;
import java.rmi.RemoteException;
import java.text.NumberFormat;

import visad.*;
import visad.formula.FormulaUtil;

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

  private static final Class matrixClass = constructMatrixClass();

  private static Class constructMatrixClass() {
    try {
      Class c = Class.forName("Jama.Matrix");
      return c;
    }
    catch (ClassNotFoundException e) {
      return null;
    }
  }

  /** This matrix's associated JAMA Matrix object */
  private Object matrix;

  /** useful methods from Jama.Matrix class */
  private static final Method[] methods =
    constructMethods();

  private static Method[] constructMethods() {
    Method[] ms = new Method[14];
    try {
      Class[] param = new Class[] {};
      ms[0] = matrixClass.getMethod("getColumnDimension", param);
      param = new Class[] {};
      ms[1] = matrixClass.getMethod("getRowDimension", param);
      param = new Class[] {};
      ms[2] = matrixClass.getMethod("getArray", param);
      param = new Class[] {int.class, int.class};
      ms[3] = matrixClass.getMethod("get", param);
      param = new Class[] {int.class, int.class, int.class, int.class};
      ms[4] = matrixClass.getMethod("getMatrix", param);
      param = new Class[] {int[].class, int[].class};
      ms[5] = matrixClass.getMethod("getMatrix", param);
      param = new Class[] {int.class, int.class, int[].class};
      ms[6] = matrixClass.getMethod("getMatrix", param);
      param = new Class[] {int[].class, int.class, int.class};
      ms[7] = matrixClass.getMethod("getMatrix", param);
      param = new Class[] {int.class, int.class, double.class};
      ms[8] = matrixClass.getMethod("set", param);
      param = new Class[] {int.class, int.class, int.class, int.class,
                           matrixClass};
      ms[9] = matrixClass.getMethod("setMatrix", param);
      param = new Class[] {int[].class, int[].class, matrixClass};
      ms[10] = matrixClass.getMethod("setMatrix", param);
      param = new Class[] {int.class, int.class, int[].class, matrixClass};
      ms[11] = matrixClass.getMethod("setMatrix", param);
      param = new Class[] {int[].class, int.class, int.class, matrixClass};
      ms[12] = matrixClass.getMethod("setMatrix", param);
      param = new Class[] {matrixClass};
      ms[13] = matrixClass.getMethod("plus", param);
    }
    catch (NoSuchMethodException e) {
    }
    return ms;
  }

  private static final Method getColumnDimension = methods[0];
  private static final Method getRowDimension = methods[1];
  private static final Method getArray = methods[2];
  private static final Method get = methods[3];
  private static final Method getMatrix1 = methods[4];
  private static final Method getMatrix2 = methods[5];
  private static final Method getMatrix3 = methods[6];
  private static final Method getMatrix4 = methods[7];
  private static final Method set = methods[8];
  private static final Method setMatrix1 = methods[9];
  private static final Method setMatrix2 = methods[10];
  private static final Method setMatrix3 = methods[11];
  private static final Method setMatrix4 = methods[12];
  private static final Method plus = methods[13];

  /** constructors from Jama.Matrix class */
  private static final Constructor[] constructors =
    constructConstructors();

  private static Constructor[] constructConstructors() {
    Constructor[] cs = new Constructor[2];
    try {
      Class[] param = new Class[] {int.class, int.class};
      cs[0] = matrixClass.getConstructor(param);
      param = new Class[] {double[][].class};
      cs[1] = matrixClass.getConstructor(param);
    }
    catch (NoSuchMethodException e) {
    }
    return cs;
  }

  private static final Constructor intintMatrix = constructors[0];
  private static final Constructor doubleMatrix = constructors[1];

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
    catch (Exception exc) {
      return null;
    }
  }

  /**
   * Test the JamaMatrix class.
   */
  public static void main(String[] args)
         throws VisADException, RemoteException {
    double[][] e1 = { {3, 4, 5},
                      {10, 8, 6},
                      {2, 1, 0} };
    double[][] e2 = { {6, 4, 2},
                      {-4, -3, -2},
                      {1, 1, 1} };
    JamaMatrix m1 = null;
    JamaMatrix m2 = null;
    JamaMatrix m3 = null;
    JamaMatrix m4 = null;
    try {
System.out.println("get = " + get);
System.out.println("getMatrix2 = " + getMatrix2);
      m1 = new JamaMatrix(e1);
      m2 = new JamaMatrix(e2);
      m3 = convertToMatrix(m1.add(m2));
      m4 = m1.plus(m2);
System.out.println("m1.get(1, 1) = " + m1.get(1, 1));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("m1:\n" + m1);
    // m1.print(1, 0);
    System.out.println("m2:\n" + m2);
    // m2.print(1, 0);
    System.out.println("m3 = m1 + m2 (VisAD):\n" + m3);
    // m3.print(1, 0);
    System.out.println("m4 = m1 + m2 (JAMA):\n" + m4);
    // m4.print(1, 0);
  }


  // Constructors

  /**
   * Constructs a domain set appropriate for the given matrix.
   * @return An Integer2DSet with dimensions equal to those of the matrix
   */
  private static Integer2DSet getDomainSet(Object matrix) {
    if (matrixClass == null || !matrixClass.isInstance(matrix)) return null;
    try {
      int rows = ((Integer)
        getRowDimension.invoke(matrix, new Object[] {})).intValue();
      int cols = ((Integer)
        getColumnDimension.invoke(matrix, new Object[] {})).intValue();
      return new Integer2DSet(rows, cols);
    }
    catch (Exception exc) {
      return null;
    }
  }

  /**
   * Construct a new JamaMatrix from the given matrix dimensions.
   */
  public JamaMatrix(int rows, int cols)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(intintMatrix.newInstance(new Object[] {new Integer(rows),
                                                new Integer(cols)}),
         null, null, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the given matrix entries.
   */
  public JamaMatrix(double[][] entries)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(doubleMatrix.newInstance(new Object[] {entries}),
         null, null, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the given JAMA Matrix.
   */
  public JamaMatrix(Object matrix) throws VisADException {
    this(matrix, null, null, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the given JAMA Matrix,
   * MathType and domain set.
   */
  public JamaMatrix(Object matrix, FunctionType type, Gridded2DSet domain_set)
    throws VisADException
  {
    this(matrix, type, domain_set, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the specified JAMA Matrix,
   * coordinate systems, range sets and units.
   */
  public JamaMatrix(Object matrix, CoordinateSystem range_coord_sys,
    CoordinateSystem[] range_coord_syses, Set[] range_sets, Unit[] units)
    throws VisADException
  {
    this(matrix, null, null, range_coord_sys, range_coord_syses, range_sets,
      units);
  }

  /**
   * Construct a new JamaMatrix from the specified JAMA Matrix, MathType,
   * domain set, coordinate systems, range sets and units.
   */
  public JamaMatrix(Object matrix, FunctionType type, Gridded2DSet domain_set,
    CoordinateSystem range_coord_sys, CoordinateSystem[] range_coord_syses,
    Set[] range_sets, Unit[] units) throws VisADException
  {
    super(type == null ? matrixType : type,
      domain_set == null ? getDomainSet(matrix) : domain_set,
      range_coord_sys, range_coord_syses, range_sets, units);
    if (type != null && !matrixType.equalsExceptName(type)) {
      throw new VisADException("JamaMatrix: " +
        "MathType must be of the form ((x, y) -> z)");
    }
    setMatrix(matrix);
  }


  // New methods

  /**
   * Return the associated JAMA Matrix object.
   */
  public Object getMatrix() {
    return matrix;
  }

  /**
   * Set this matrix's samples to correspond to those of the given
   * JAMA Matrix.
   */
  public void setMatrix(Object matrix) throws VisADException {
    // convert matrix entries into range sample values
    if (matrixClass == null || !matrixClass.isInstance(matrix)) return;
    int rows, cols;
    double[][] entries = null;
    try {
      rows = ((Integer)
        getRowDimension.invoke(matrix, new Object[] {})).intValue();
      cols = ((Integer)
        getColumnDimension.invoke(matrix, new Object[] {})).intValue();
      entries =
        (double[][]) getArray.invoke(matrix, new Object[] {});
    }
    catch (Exception e) {
      return;
    }
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
    try {
      setMatrix(doubleMatrix.newInstance(new Object[] {entries}));
    }
    catch (Exception e) {
    }
  }


  // Method wrappers for JAMA Matrix functionality

  /**
   * Get row dimension.
   * @return     The number of rows
   */
  public int getRowDimension() {
    try {
      int rows = ((Integer)
        getRowDimension.invoke(matrix, new Object[] {})).intValue();
      return rows;
    }
    catch (Exception e) {
      return 0;
    }
  }

  /**
   * Get column dimension.
   * @return     The number of columns
   */
  public int getColumnDimension() {
    try {
      int cols = ((Integer)
        getColumnDimension.invoke(matrix, new Object[] {})).intValue();
      return cols;
    }
    catch (Exception e) {
      return 0;
    }
  }

  /**
   * Get a single element.
   * @param i    Row index
   * @param j    Column index
   * @return     A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   */
  public double get(int i, int j) {
    try {
      double val = ((Double)
        get.invoke(matrix, new Object[] {new Integer(i),
                                         new Integer(j)})).doubleValue();
      return val;
    }
    catch (Exception e) {
      return 0;
    }
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
    try {
      Object m = getMatrix1.invoke(matrix, new Object[] {new Integer(i0),
                     new Integer(i1), new Integer(j0), new Integer(j1)});
      return new JamaMatrix(m);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Get a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @return     A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public JamaMatrix getMatrix(int[] r, int[] c) throws VisADException {
    try {
      Object m = getMatrix2.invoke(matrix, new Object[] {r, c});
      return new JamaMatrix(m);
    }
    catch (Exception e) {
      return null;
    }
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
    try {
      Object m = getMatrix1.invoke(matrix, new Object[] {new Integer(i0),
                                                new Integer(i1), c});
      return new JamaMatrix(m);
    }
    catch (Exception e) {
      return null;
    }
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
    try {
      Object m = getMatrix1.invoke(matrix, new Object[] {r, new Integer(j0),
                                                         new Integer(j1)});
      return new JamaMatrix(m);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Set a single element.
   * @param i    Row index
   * @param j    Column index
   * @param s    A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   */
  public void set(int i, int j, double s) throws VisADException {
    try {
      set.invoke(matrix, new Object[] {new Integer(i),
                                       new Integer(j),
                                       new Double(s)});
      setMatrix(matrix);
    }
    catch (Exception e) {
      return;
    }
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
    try {
      setMatrix1.invoke(matrix, new Object[] {new Integer(i0),
                     new Integer(i1), new Integer(j0), new Integer(j1),
                     X.getMatrix()});
      setMatrix(matrix);
    }
    catch (Exception e) {
      return;
    }
  }

  /**
   * Set a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @param X    A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public void setMatrix(int[] r, int[] c, JamaMatrix X) throws VisADException {
    try {
      setMatrix2.invoke(matrix, new Object[] {r, c, X.getMatrix()});
      setMatrix(matrix);
    }
    catch (Exception e) {
      return;
    }
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
    try {
      setMatrix3.invoke(matrix, new Object[] {r, new Integer(j0),
                     new Integer(j1), X.getMatrix()});
      setMatrix(matrix);
    }
    catch (Exception e) {
      return;
    }
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
    try {
      setMatrix4.invoke(matrix, new Object[] {new Integer(i0),
                     new Integer(i1), c, X.getMatrix()});
      setMatrix(matrix);
    }
    catch (Exception e) {
      return;
    }
  }

  /**
   * Matrix transpose.
   * @return    A'
   */
/*
  public JamaMatrix transpose() throws VisADException {
    Matrix m = matrix.transpose();
    return new JamaMatrix(m);
  }
*/

  /**
   * One norm.
   * @return    maximum column sum
   */
/*
  public double norm1() {
    return matrix.norm1();
  }
*/

  /**
   * Two norm.
   * @return    maximum singular value
   */
/*
  public double norm2() {
    return matrix.norm2();
  }
*/

  /**
   * Infinity norm.
   * @return    maximum row sum
   */
/*
  public double normInf() {
    return matrix.normInf();
  }
*/

  /**
   * Frobenius norm.
   * @return    sqrt of sum of squares of all elements
   */
/*
  public double normF() {
    return matrix.normF();
  }
*/

  /**
   * Unary minus.
   * @return    -A
   */
/*
  public JamaMatrix uminus() throws VisADException {
    Matrix m = matrix.uminus();
    return new JamaMatrix(m);
  }
*/

  /**
   * C = A + B
   * @param B    another matrix
   * @return     A + B
   */
  public JamaMatrix plus(JamaMatrix B) throws VisADException {
    try {
      Object m = plus.invoke(matrix, new Object[] {B.getMatrix()});
      return new JamaMatrix(m);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * A = A + B
   * @param B    another matrix
   * @return     A + B
   */
/*
  public JamaMatrix plusEquals(JamaMatrix B) throws VisADException {
    matrix.plusEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }
*/

  /**
   * C = A - B
   * @param B    another matrix
   * @return     A - B
   */
/*
  public JamaMatrix minus(JamaMatrix B) throws VisADException {
    Matrix m = matrix.minus(B.getMatrix());
    return new JamaMatrix(m);
  }
*/

  /**
   * A = A - B
   * @param B    another matrix
   * @return     A - B
   */
/*
  public JamaMatrix minusEquals(JamaMatrix B) throws VisADException {
    matrix.minusEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }
*/

  /**
   * Element-by-element multiplication, C = A.*B
   * @param B    another matrix
   * @return     A.*B
   */
/*
  public JamaMatrix arrayTimes(JamaMatrix B) throws VisADException {
    Matrix m = matrix.arrayTimes(B.getMatrix());
    return new JamaMatrix(m);
  }
*/

  /**
   * Element-by-element multiplication in place, A = A.*B
   * @param B    another matrix
   * @return     A.*B
   */
/*
  public JamaMatrix arrayTimesEquals(JamaMatrix B) throws VisADException {
    matrix.arrayTimesEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }
*/

  /**
   * Element-by-element right division, C = A./B
   * @param B    another matrix
   * @return     A./B
   */
/*
  public JamaMatrix arrayRightDivide(JamaMatrix B) throws VisADException {
    Matrix m = matrix.arrayRightDivide(B.getMatrix());
    return new JamaMatrix(m);
  }
*/

  /**
   * Element-by-element right division in place, A = A./B
   * @param B    another matrix
   * @return     A./B
   */
/*
  public JamaMatrix arrayRightDivideEquals(JamaMatrix B)
    throws VisADException
  {
    matrix.arrayRightDivideEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }
*/

  /**
   * Element-by-element left division, C = A.\B
   * @param B    another matrix
   * @return     A.\B
   */
/*
  public JamaMatrix arrayLeftDivide(JamaMatrix B) throws VisADException {
    Matrix m = matrix.arrayLeftDivide(B.getMatrix());
    return new JamaMatrix(m);
  }
*/

  /**
   * Element-by-element left division in place, A = A.\B
   * @param B    another matrix
   * @return     A.\B
   */
/*
  public JamaMatrix arrayLeftDivideEquals(JamaMatrix B) throws VisADException {
    matrix.arrayLeftDivideEquals(B.getMatrix());
    setMatrix(matrix);
    return this;
  }
*/

  /**
   * Multiply a matrix by a scalar, C = s*A
   * @param s    scalar
   * @return     s*A
   */
/*
  public JamaMatrix times(double s) throws VisADException {
    Matrix m = matrix.times(s);
    return new JamaMatrix(m);
  }
*/

  /**
   * Multiply a matrix by a scalar in place, A = s*A
   * @param s    scalar
   * @return     replace A by s*A
   */
/*
  public JamaMatrix timesEquals(double s) throws VisADException {
    matrix.times(s);
    setMatrix(matrix);
    return this;
  }
*/

  /**
   * Linear algebraic matrix multiplication, A * B
   * @param B    another matrix
   * @return     Matrix product, A * B
   * @exception  IllegalArgumentException Matrix inner dimensions must agree
   */
/*
  public JamaMatrix times(JamaMatrix B) throws VisADException {
    Matrix m = matrix.times(B.getMatrix());
    return new JamaMatrix(m);
  }
*/

  /**
   * Solve A*X = B
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
/*
  public JamaMatrix solve(JamaMatrix B) throws VisADException {
    Matrix m = matrix.solve(B.getMatrix());
    return new JamaMatrix(m);
  }
*/

  /**
   * Solve X*A = B, which is also A'*X' = B'
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
/*
  public JamaMatrix solveTranspose(JamaMatrix B) throws VisADException {
    Matrix m = matrix.solveTranspose(B.getMatrix());
    return new JamaMatrix(m);
  }
*/

  /**
   * Matrix inverse or pseudoinverse.
   * @return     inverse(A) if A is square, pseudoinverse otherwise
   */
/*
  public JamaMatrix inverse() throws VisADException {
    Matrix m = matrix.inverse();
    return new JamaMatrix(m);
  }
*/

  /**
   * Matrix determinant.
   * @return     determinant
   */
/*
  public double det() {
    return matrix.det();
  }
*/

  /**
   * Matrix rank.
   * @return     effective numerical rank, obtained from SVD
   */
/*
  public int rank() {
    return matrix.rank();
  }
*/

  /**
   * Matrix condition (2 norm).
   * @return     ratio of largest to smallest singular value
   */
/*
  public double cond() {
    return matrix.cond();
  }
*/

  /**
   * Matrix trace.
   * @return     sum of the diagonal elements
   */
/*
  public double trace() {
    return matrix.trace();
  }
*/

  /**
   * Generate matrix with random elements.
   * @param m    Number of rows
   * @param n    Number of colums
   * @return     An m-by-n matrix with uniformly distributed random elements
   */
/*
  public static JamaMatrix random(int m, int n) throws VisADException {
    Matrix random = Matrix.random(m, n);
    return new JamaMatrix(random);
  }
*/

  /**
   * Generate identity matrix.
   * @param m    Number of rows
   * @param n    Number of colums
   * @return     An m-by-n matrix with ones on the diagonal and zeros elsewhere
   */
/*
  public static JamaMatrix identity(int m, int n) throws VisADException {
    Matrix identity = Matrix.identity(m, n);
    return new JamaMatrix(identity);
  }
*/

  /**
   * Print the matrix to stdout.   Line the elements up in columns
   * with a Fortran-like 'Fw.d' style format
   * @param w    Column width
   * @param d    Number of digits after the decimal
   */
/*
  public void print(int w, int d) {
    matrix.print(w, d);
  }
*/

  /**
   * Print the matrix to the output stream.   Line the elements up in
   * columns with a Fortran-like 'Fw.d' style format.
   * @param output Output stream
   * @param w      Column width
   * @param d      Number of digits after the decimal
   */
/*
  public void print(PrintWriter output, int w, int d) {
    matrix.print(output, w, d);
  }
*/

  /**
   * Print the matrix to stdout.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param format Formatting object for individual elements
   * @param width  Field width for each column
   */
/*
  public void print(NumberFormat format, int width) {
    matrix.print(format, width);
  }
*/

  /**
   * Print the matrix to the output stream.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param output the output stream
   * @param format A formatting object to format the matrix elements 
   * @param width  Column width
   */
/*
  public void print(PrintWriter output, NumberFormat format, int width) {
    matrix.print(output, format, width);
  }
*/

  /**
   * Read a matrix from a stream.  The format is the same the print method,
   * so printed matrices can be read back in.  Elements are separated by
   * whitespace, all the elements for each row appear on a single line,
   * the last row is followed by a blank line.
   * @param input the input stream
   */
/*
  public static JamaMatrix read(BufferedReader input)
    throws IOException, VisADException
  {
    Matrix m = Matrix.read(input);
    return new JamaMatrix(m);
  }
*/

}

