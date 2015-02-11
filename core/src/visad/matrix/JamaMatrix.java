//
// JamaMatrix.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.text.NumberFormat;

import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DSet;
import visad.Gridded2DSet;
import visad.GriddedSet;
import visad.Integer2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.SetType;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;

/**
 * JamaMatrix is a VisAD wrapper for JAMA matrices. This class requires the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA package</a>.
 */
public class JamaMatrix extends FlatField {

  private static final RealType matrix_row =
    RealType.getRealType("matrix_row");

  private static final RealType matrix_column =
    RealType.getRealType("matrix_column");

  private static final RealType matrix_value =
    RealType.getRealType("matrix_value");

  private static final FunctionType matrixType = constructFunction();

  private static FunctionType constructFunction() {
    try {
      RealTupleType tuple = new RealTupleType(matrix_row, matrix_column);
      FunctionType function = new FunctionType(tuple, matrix_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final Class[] classes = constructClasses();

  private static Class[] constructClasses() {
    Class[] cs = new Class[6];
    try {
      cs[0] = Class.forName("Jama.Matrix");
      cs[1] = Class.forName("Jama.CholeskyDecomposition");
      cs[2] = Class.forName("Jama.EigenvalueDecomposition");
      cs[3] = Class.forName("Jama.LUDecomposition");
      cs[4] = Class.forName("Jama.QRDecomposition");
      cs[5] = Class.forName("Jama.SingularValueDecomposition");
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("you need to install Jama from " +
                                 "http://math.nist.gov/javanumerics/jama/");
    }
    return cs;
  }

  private static final Class classMatrix = classes[0];
  private static final Class classCholeskyDecomposition = classes[1];
  private static final Class classEigenvalueDecomposition = classes[2];
  private static final Class classLUDecomposition = classes[3];
  private static final Class classQRDecomposition = classes[4];
  private static final Class classSingularValueDecomposition = classes[5];

  private static Class constructMatrixClass() {
    try {
      Class c = Class.forName("Jama.Matrix");
      return c;
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("you need to install Jama from " +
                                 "http://math.nist.gov/javanumerics/jama/");
    }
  }

  /** This matrix's associated JAMA Matrix object */
  private Object matrix;

  /** useful methods from Jama.Matrix class */
  private static final Method[] methods =
    constructMethods();

  private static Method[] constructMethods() {
    Method[] ms = new Method[51];
    try {
      Class[] param = new Class[] {};
      ms[0] = classMatrix.getMethod("getColumnDimension", param);
      ms[1] = classMatrix.getMethod("getRowDimension", param);
      ms[2] = classMatrix.getMethod("getArray", param);
      param = new Class[] {int.class, int.class};
      ms[3] = classMatrix.getMethod("get", param);
      param = new Class[] {int.class, int.class, int.class, int.class};
      ms[4] = classMatrix.getMethod("getMatrix", param);
      param = new Class[] {int[].class, int[].class};
      ms[5] = classMatrix.getMethod("getMatrix", param);
      param = new Class[] {int.class, int.class, int[].class};
      ms[6] = classMatrix.getMethod("getMatrix", param);
      param = new Class[] {int[].class, int.class, int.class};
      ms[7] = classMatrix.getMethod("getMatrix", param);
      param = new Class[] {int.class, int.class, double.class};
      ms[8] = classMatrix.getMethod("set", param);
      param = new Class[] {int.class, int.class, int.class, int.class,
                           classMatrix};
      ms[9] = classMatrix.getMethod("setMatrix", param);
      param = new Class[] {int[].class, int[].class, classMatrix};
      ms[10] = classMatrix.getMethod("setMatrix", param);
      param = new Class[] {int.class, int.class, int[].class, classMatrix};
      ms[11] = classMatrix.getMethod("setMatrix", param);
      param = new Class[] {int[].class, int.class, int.class, classMatrix};
      ms[12] = classMatrix.getMethod("setMatrix", param);
      param = new Class[] {};
      ms[13] = classMatrix.getMethod("transpose", param);
      ms[14] = classMatrix.getMethod("norm1", param);
      ms[15] = classMatrix.getMethod("norm2", param);
      ms[16] = classMatrix.getMethod("normInf", param);
      ms[17] = classMatrix.getMethod("normF", param);
      ms[18] = classMatrix.getMethod("uminus", param);
      param = new Class[] {classMatrix};
      ms[19] = classMatrix.getMethod("plus", param);
      ms[20] = classMatrix.getMethod("plusEquals", param);
      ms[21] = classMatrix.getMethod("minus", param);
      ms[22] = classMatrix.getMethod("minusEquals", param);
      ms[23] = classMatrix.getMethod("arrayTimes", param);
      ms[24] = classMatrix.getMethod("arrayTimesEquals", param);
      ms[25] = classMatrix.getMethod("arrayRightDivide", param);
      ms[26] = classMatrix.getMethod("arrayRightDivideEquals", param);
      ms[27] = classMatrix.getMethod("arrayLeftDivide", param);
      ms[28] = classMatrix.getMethod("arrayLeftDivideEquals", param);
      param = new Class[] {double.class};
      ms[29] = classMatrix.getMethod("times", param);
      ms[30] = classMatrix.getMethod("timesEquals", param);
      param = new Class[] {classMatrix};
      ms[31] = classMatrix.getMethod("times", param);
      ms[32] = classMatrix.getMethod("solve", param);
      ms[33] = classMatrix.getMethod("solveTranspose", param);
      param = new Class[] {};
      ms[34] = classMatrix.getMethod("inverse", param);
      ms[35] = classMatrix.getMethod("det", param);
      ms[36] = classMatrix.getMethod("rank", param);
      ms[37] = classMatrix.getMethod("cond", param);
      ms[38] = classMatrix.getMethod("trace", param);
      param = new Class[] {int.class, int.class};
      ms[39] = classMatrix.getMethod("random", param);
      ms[40] = classMatrix.getMethod("identity", param);
      ms[41] = classMatrix.getMethod("print", param);
      param = new Class[] {PrintWriter.class, int.class, int.class};
      ms[42] = classMatrix.getMethod("print", param);
      param = new Class[] {NumberFormat.class, int.class};
      ms[43] = classMatrix.getMethod("print", param);
      param = new Class[] {PrintWriter.class, NumberFormat.class, int.class};
      ms[44] = classMatrix.getMethod("print", param);
      param = new Class[] {BufferedReader.class};
      ms[45] = classMatrix.getMethod("read", param);
      param = new Class[] {};
      ms[46] = classMatrix.getMethod("chol", param);
      ms[47] = classMatrix.getMethod("eig", param);
      ms[48] = classMatrix.getMethod("lu", param);
      ms[49] = classMatrix.getMethod("qr", param);
      ms[50] = classMatrix.getMethod("svd", param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
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
  private static final Method transpose = methods[13];
  private static final Method norm1 = methods[14];
  private static final Method norm2 = methods[15];
  private static final Method normInf = methods[16];
  private static final Method normF = methods[17];
  private static final Method uminus = methods[18];
  private static final Method plus = methods[19];
  private static final Method plusEquals = methods[20];
  private static final Method minus = methods[21];
  private static final Method minusEquals = methods[22];
  private static final Method arrayTimes = methods[23];
  private static final Method arrayTimesEquals = methods[24];
  private static final Method arrayRightDivide = methods[25];
  private static final Method arrayRightDivideEquals = methods[26];
  private static final Method arrayLeftDivide = methods[27];
  private static final Method arrayLeftDivideEquals = methods[28];
  private static final Method times1 = methods[29];
  private static final Method timesEquals = methods[30];
  private static final Method times2 = methods[31];
  private static final Method solve = methods[32];
  private static final Method solveTranspose = methods[33];
  private static final Method inverse = methods[34];
  private static final Method det = methods[35];
  private static final Method rank = methods[36];
  private static final Method cond = methods[37];
  private static final Method trace = methods[38];
  private static final Method random = methods[39];
  private static final Method identity = methods[40];
  private static final Method print1 = methods[41];
  private static final Method print2 = methods[42];
  private static final Method print3 = methods[43];
  private static final Method print4 = methods[44];
  private static final Method read = methods[45];
  private static final Method chol = methods[46];
  private static final Method eig = methods[47];
  private static final Method lu = methods[48];
  private static final Method qr = methods[49];
  private static final Method svd = methods[50];

  /** constructors from Jama.Matrix class */
  private static final Constructor[] constructors =
    constructConstructors();

  private static Constructor[] constructConstructors() {
    Constructor[] cs = new Constructor[2];
    try {
      Class[] param = new Class[] {int.class, int.class};
      cs[0] = classMatrix.getConstructor(param);
      param = new Class[] {double[][].class};
      cs[1] = classMatrix.getConstructor(param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return cs;
  }

  private static final Constructor intintMatrix = constructors[0];
  private static final Constructor doubleMatrix = constructors[1];

  // Static methods

  /**
   * Attempt to convert the given VisAD FlatField to a VisAD JamaMatrix
   * Data object.
   * @return The converted object, or null if it could not be converted
   */
  public static JamaMatrix convertToMatrix(FlatField field)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }

    // if field is already a JamaMatrix the task is trivial
    if (field instanceof JamaMatrix) return (JamaMatrix) field;

    FunctionType ftype = (FunctionType) field.getType();

    // get domain set (must be 2-D ordered samples)
    Set set = field.getDomainSet();
    Gridded2DSet grid = null;
    if (set instanceof Gridded2DSet) {
      grid = (Gridded2DSet) set;
    }
    else if (set instanceof GriddedSet &&
             set.getDimension() == 2) {
      int[] lengths = ((GriddedSet) set).getLengths();
      float[][] samples = set.getSamples(false);
      grid = new Gridded2DSet(set.getType(), samples, lengths[0],
                              lengths[1], set.getCoordinateSystem(),
                              set.getSetUnits(), set.getSetErrors());
    }
    else if (set instanceof Gridded1DSet) {
      int[] lengths = ((Gridded1DSet) set).getLengths();
      float[][] samples = set.getSamples(false);
      float[] dummies = new float[lengths[0]];
      for (int i=0; i<lengths[0]; i++) dummies[i] = 0.0f;
      samples = new float[][] {samples[0], dummies};
      RealType rt1 =
        (RealType) ((SetType) set.getType()).getDomain().getComponent(0);
      RealType rt0 = RealType.getRealType("dummy");
      RealTupleType rtt = new RealTupleType(rt1, rt0);
      grid = new Gridded2DSet(rtt, samples, lengths[0], 1, null, null, null);
      ftype = new FunctionType(rtt, ftype.getRange());
    }
    else {
      return null;
    }

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

    CoordinateSystem rc = null;
    try {
      CoordinateSystem[] r = field.getRangeCoordinateSystem();
      rc = r[0];
    }
    catch (Exception e) {
    }
    CoordinateSystem[] rcs = null;
    try {
      int n = ((TupleType) ftype.getRange()).getDimension();
      rcs = new CoordinateSystem[n];
      for (int i=0; i<n; i++) {
        CoordinateSystem[] r = field.getRangeCoordinateSystem(i);
        rcs[i] = r[0];
      }
    }
    catch (Exception e) {
    }
    Set[] rangeSets = field.getRangeSets();
    Unit[] units = null;
    try {
      Unit[][] us = field.getRangeUnits();
      if (us != null) {
        int n = us.length;
        for (int i=0; i<n; i++) {
          units[i] = us[i][0];
        }
      }
    }
    catch (Exception e) {
    }

    Object m = doubleMatrix.newInstance(new Object[] {entries});
    return new JamaMatrix(m, ftype, grid, rc, rcs, rangeSets, units);
  }

  /**
   * Test the JamaMatrix class.
   */
  public static void main(String[] args)
         throws VisADException, RemoteException {
    double[][] e1 = { {3, 4, 5},
                      {10, 18, 6},
                      {2, -1, 0} };
    double[][] e2 = { {6, 4, 2},
                      {-4, -3, -2},
                      {1, 1, 1} };
    try {
      System.out.println("get = " + get);
      System.out.println("getMatrix2 = " + getMatrix2);
      JamaMatrix m1 = new JamaMatrix(e1);
      JamaMatrix m2 = new JamaMatrix(e2);
      JamaMatrix m3 = convertToMatrix((FlatField) m1.add(m2));
      JamaMatrix m4 = m1.plus(m2);
      System.out.println("m1.get(1, 1) = " + m1.get(1, 1));
      System.out.println("m1:");
      m1.print(1, 0);
      System.out.println("m2:");
      m2.print(1, 0);
      System.out.println("m3 = m1 + m2 (VisAD):");
      m3.print(1, 0);
      System.out.println("m4 = m1 + m2 (JAMA):");
      m4.print(1, 0);
      System.out.println("m4 = " + m4);

      JamaSingularValueDecomposition svd4 = m4.svd();
      System.out.println("m4 svd U:");
      svd4.getU().print(1, 0);
      System.out.println("m4 svd S:");
      svd4.getS().print(1, 0);
      System.out.println("m4 svd V:");
      svd4.getV().print(1, 0);

      JamaQRDecomposition qr4 = m4.qr();
      System.out.println("m4 qr Q:");
      qr4.getQ().print(1, 0);
      System.out.println("m4 qr R:");
      qr4.getR().print(1, 0);

      JamaLUDecomposition lu4 = m4.lu();
      System.out.println("m4 lu L:");
      lu4.getL().print(1, 0);
      System.out.println("m4 lu U:");
      lu4.getU().print(1, 0);

      JamaEigenvalueDecomposition ev4 = m4.eig();
      System.out.println("m4 eig D:");
      ev4.getD().print(1, 0);
      System.out.println("m4 eig V:");
      ev4.getV().print(1, 0);

      JamaCholeskyDecomposition chol4 = m4.chol();
      System.out.println("m4 chol L:");
      chol4.getL().print(1, 0);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  // Constructors

  /**
   * Constructs a domain set appropriate for the given matrix.
   * @return An Integer2DSet with dimensions equal to those of the matrix
   */
  private static Integer2DSet getDomainSet(Object matrix)
          throws VisADException, IllegalAccessException,
                 InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    if (!classMatrix.isInstance(matrix)) {
      throw new VisADException("matrix must be an instance of Jama.Matrix");
    }
    int rows = ((Integer)
      getRowDimension.invoke(matrix, new Object[] {})).intValue();
    int cols = ((Integer)
      getColumnDimension.invoke(matrix, new Object[] {})).intValue();
    return new Integer2DSet(rows, cols);
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
  public JamaMatrix(Object matrix)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrix, null, null, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the given JAMA Matrix,
   * MathType and domain set.
   */
  public JamaMatrix(Object matrix, FunctionType type, Gridded2DSet domain_set)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrix, type, domain_set, null, null, null, null);
  }

  /**
   * Construct a new JamaMatrix from the specified JAMA Matrix,
   * coordinate systems, range sets and units.
   */
  public JamaMatrix(Object matrix, CoordinateSystem range_coord_sys,
                    CoordinateSystem[] range_coord_syses, Set[] range_sets,
                    Unit[] units)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrix, null, null, range_coord_sys, range_coord_syses, range_sets,
      units);
  }

  /**
   * Construct a new JamaMatrix from the specified JAMA Matrix, MathType,
   * domain set, coordinate systems, range sets and units.
   */
  public JamaMatrix(Object matrix, FunctionType type, Gridded2DSet domain_set,
                    CoordinateSystem range_coord_sys,
                    CoordinateSystem[] range_coord_syses,
                    Set[] range_sets, Unit[] units)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    super(type == null ? matrixType : type,
      domain_set == null ? getDomainSet(matrix) : domain_set,
      range_coord_sys, range_coord_syses, range_sets, units);
    if (type != null && !matrixType.equalsExceptName(type)) {
      throw new VisADException("JamaMatrix: " +
        "MathType must be of the form ((x, y) -> z)");
    }
    setMatrix(matrix);
  }

  // place for JamaCholeskyDecomposition, etc to store their
  // CholeskyDecomposition during construction
  private Object stash = null;

  void setStash(Object s) {
    stash = s;
  }

  Object getStash() {
    return stash;
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
  public void setMatrix(Object matrix)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    if (!classMatrix.isInstance(matrix)) {
      throw new VisADException("matrix must be an instance of Jama.Matrix");
    }
    // convert matrix entries into range sample values
    int rows = ((Integer)
      getRowDimension.invoke(matrix, new Object[] {})).intValue();
    int cols = ((Integer)
      getColumnDimension.invoke(matrix, new Object[] {})).intValue();
    double[][] entries =
      (double[][]) getArray.invoke(matrix, new Object[] {});
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
  public void setMatrix(double[][] entries)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    setMatrix(doubleMatrix.newInstance(new Object[] {entries}));
  }


  // Method wrappers for JAMA Matrix functionality

  /**
   * Get row dimension.
   * @return     The number of rows
   */
  public int getRowDimension()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    int rows = ((Integer)
      getRowDimension.invoke(matrix, new Object[] {})).intValue();
    return rows;
  }

  /**
   * Get column dimension.
   * @return     The number of columns
   */
  public int getColumnDimension()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    int cols = ((Integer)
      getColumnDimension.invoke(matrix, new Object[] {})).intValue();
    return cols;
  }

  /**
   * Get a single element.
   * @param i    Row index
   * @param j    Column index
   * @return     A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   */
  public double get(int i, int j)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      get.invoke(matrix, new Object[] {new Integer(i),
                                       new Integer(j)})).doubleValue();
    return val;
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
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = getMatrix1.invoke(matrix, new Object[] {new Integer(i0),
                   new Integer(i1), new Integer(j0), new Integer(j1)});
    return new JamaMatrix(m);
  }

  /**
   * Get a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @return     A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public JamaMatrix getMatrix(int[] r, int[] c)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = getMatrix2.invoke(matrix, new Object[] {r, c});
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
  public JamaMatrix getMatrix(int i0, int i1, int[] c)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = getMatrix1.invoke(matrix, new Object[] {new Integer(i0),
                                              new Integer(i1), c});
    return new JamaMatrix(m);
  }

  /**
   * Get a submatrix.
   * @param r    Array of row indices
   * @param j0   Initial column index
   * @param j1   Final column index
   * @return     A(r(:),j0:j1)
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  
  public JamaMatrix getMatrix(int[] r, int j0, int j1)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = getMatrix1.invoke(matrix, new Object[] {r, new Integer(j0),
                                                       new Integer(j1)});
    return new JamaMatrix(m);
  }

  /**
   * Set a single element.
   * @param i    Row index
   * @param j    Column index
   * @param s    A(i,j)
   * @exception  ArrayIndexOutOfBoundsException
   */
  public void set(int i, int j, double s)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    set.invoke(matrix, new Object[] {new Integer(i),
                                     new Integer(j),
                                     new Double(s)});
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
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    setMatrix1.invoke(matrix, new Object[] {new Integer(i0),
                   new Integer(i1), new Integer(j0), new Integer(j1),
                   X.getMatrix()});
    setMatrix(matrix);
  }

  /**
   * Set a submatrix.
   * @param r    Array of row indices
   * @param c    Array of column indices
   * @param X    A(r(:),c(:))
   * @exception  ArrayIndexOutOfBoundsException Submatrix indices
   */
  public void setMatrix(int[] r, int[] c, JamaMatrix X)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    setMatrix2.invoke(matrix, new Object[] {r, c, X.getMatrix()});
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
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    setMatrix3.invoke(matrix, new Object[] {r, new Integer(j0),
                   new Integer(j1), X.getMatrix()});
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
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    setMatrix4.invoke(matrix, new Object[] {new Integer(i0),
                   new Integer(i1), c, X.getMatrix()});
    setMatrix(matrix);
  }

  /**
   * Matrix transpose.
   * @return    A'
   */
  public JamaMatrix transpose()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = transpose.invoke(matrix, new Object[] {});
    return new JamaMatrix(m);
  }

  /**
   * One norm.
   * @return    maximum column sum
   */
  public double norm1()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      norm1.invoke(matrix, new Object[] {})).doubleValue();
    return val;
  }

  /**
   * Two norm.
   * @return    maximum singular value
   */
  public double norm2()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      norm2.invoke(matrix, new Object[] {})).doubleValue();
    return val;
  }

  /**
   * Infinity norm.
   * @return    maximum row sum
   */
  public double normInf()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      normInf.invoke(matrix, new Object[] {})).doubleValue();
    return val;
  }

  /**
   * Frobenius norm.
   * @return    sqrt of sum of squares of all elements
   */
  public double normF()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      normF.invoke(matrix, new Object[] {})).doubleValue();
    return val;
  }

  /**
   * Unary minus.
   * @return    -A
   */
  public JamaMatrix uminus()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = uminus.invoke(matrix, new Object[] {});
    return new JamaMatrix(m);
  }

  /**
   * C = A + B
   * @param B    another matrix
   * @return     A + B
   */
  public JamaMatrix plus(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = plus.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * A = A + B
   * @param B    another matrix
   * @return     A + B
   */
  public JamaMatrix plusEquals(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    plusEquals.invoke(matrix, new Object[] {B.getMatrix()});
    setMatrix(matrix);
    return this;
  }

  /**
   * C = A - B
   * @param B    another matrix
   * @return     A - B
   */
  public JamaMatrix minus(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = minus.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * A = A - B
   * @param B    another matrix
   * @return     A - B
   */
  public JamaMatrix minusEquals(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    minusEquals.invoke(matrix, new Object[] {B.getMatrix()});
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element multiplication, C = A.*B
   * @param B    another matrix
   * @return     A.*B
   */
  public JamaMatrix arrayTimes(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = arrayTimes.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element multiplication in place, A = A.*B
   * @param B    another matrix
   * @return     A.*B
   */
  public JamaMatrix arrayTimesEquals(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    arrayTimesEquals.invoke(matrix, new Object[] {B.getMatrix()});
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element right division, C = A./B
   * @param B    another matrix
   * @return     A./B
   */
  public JamaMatrix arrayRightDivide(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = arrayRightDivide.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element right division in place, A = A./B
   * @param B    another matrix
   * @return     A./B
   */
  public JamaMatrix arrayRightDivideEquals(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    arrayRightDivideEquals.invoke(matrix, new Object[] {B.getMatrix()});
    setMatrix(matrix);
    return this;
  }

  /**
   * Element-by-element left division, C = A.\B
   * @param B    another matrix
   * @return     A.\B
   */
  public JamaMatrix arrayLeftDivide(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = arrayLeftDivide.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * Element-by-element left division in place, A = A.\B
   * @param B    another matrix
   * @return     A.\B
   */
  public JamaMatrix arrayLeftDivideEquals(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    arrayLeftDivideEquals.invoke(matrix, new Object[] {B.getMatrix()});
    setMatrix(matrix);
    return this;
  }

  /**
   * Multiply a matrix by a scalar, C = s*A
   * @param s    scalar
   * @return     s*A
   */
  public JamaMatrix times(double s)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = times1.invoke(matrix, new Object[] {new Double(s)});
    return new JamaMatrix(m);
  }

  /**
   * Multiply a matrix by a scalar in place, A = s*A
   * @param s    scalar
   * @return     replace A by s*A
   */
  public JamaMatrix timesEquals(double s)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    timesEquals.invoke(matrix, new Object[] {new Double(s)});
    setMatrix(matrix);
    return this;
  }

  /**
   * Linear algebraic matrix multiplication, A * B
   * @param B    another matrix
   * @return     Matrix product, A * B
   * @exception  IllegalArgumentException Matrix inner dimensions must agree
   */
  public JamaMatrix times(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = times2.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * Solve A*X = B
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
  public JamaMatrix solve(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }

    Object m = solve.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * Solve X*A = B, which is also A'*X' = B'
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
  public JamaMatrix solveTranspose(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = solveTranspose.invoke(matrix, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

  /**
   * Matrix inverse or pseudoinverse.
   * @return     inverse(A) if A is square, pseudoinverse otherwise
   */
  public JamaMatrix inverse()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = inverse.invoke(matrix, new Object[] {});
    return new JamaMatrix(m);
  }

  /**
   * Matrix determinant.
   * @return     determinant
   */
  public double det()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      det.invoke(matrix, new Object[] {})).doubleValue();
    return val;
  }

  /**
   * Matrix rank.
   * @return     effective numerical rank, obtained from SVD
   */
  public int rank()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    int val = ((Integer)
      rank.invoke(matrix, new Object[] {})).intValue();
    return val;
  }

  /**
   * Matrix condition (2 norm).
   * @return     ratio of largest to smallest singular value
   */
  public double cond()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      cond.invoke(matrix, new Object[] {})).doubleValue();
    return val;
  }

  /**
   * Matrix trace.
   * @return     sum of the diagonal elements
   */
  public double trace()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double)
      trace.invoke(matrix, new Object[] {})).doubleValue();
    return val;
  }

  /**
   * Generate matrix with random elements.
   * @param m    Number of rows
   * @param n    Number of colums
   * @return     An m-by-n matrix with uniformly distributed random elements
   */
  public static JamaMatrix random(int m, int n)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object mat = random.invoke(null, new Object[] {new Integer(m),
                                                   new Integer(n)});
    return new JamaMatrix(mat);
  }

  /**
   * Generate identity matrix.
   * @param m    Number of rows
   * @param n    Number of colums
   * @return     An m-by-n matrix with ones on the diagonal and zeros elsewhere
   */
  public static JamaMatrix identity(int m, int n)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object mat = identity.invoke(null, new Object[] {new Integer(m),
                                                     new Integer(n)});
    return new JamaMatrix(mat);
  }

  /**
   * Print the matrix to stdout.   Line the elements up in columns
   * with a Fortran-like 'Fw.d' style format
   * @param w    Column width
   * @param d    Number of digits after the decimal
   */
  public void print(int w, int d)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    print1.invoke(matrix, new Object[] {new Integer(w), new Integer(d)});
  }

  /**
   * Print the matrix to the output stream.   Line the elements up in
   * columns with a Fortran-like 'Fw.d' style format.
   * @param output Output stream
   * @param w      Column width
   * @param d      Number of digits after the decimal
   */
  public void print(PrintWriter output, int w, int d)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    print2.invoke(matrix, new Object[] {output, new Integer(w), new Integer(d)});
  }

  /**
   * Print the matrix to stdout.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param format Formatting object for individual elements
   * @param width  Field width for each column
   */
  public void print(NumberFormat format, int width)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    print3.invoke(matrix, new Object[] {format, new Integer(width)});
  }

  /**
   * Print the matrix to the output stream.  Line the elements up in columns.
   * Use the format object, and right justify within columns of width
   * characters.
   * @param output the output stream
   * @param format A formatting object to format the matrix elements 
   * @param width  Column width
   */
  public void print(PrintWriter output, NumberFormat format, int width)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    print4.invoke(matrix, new Object[] {output, format, new Integer(width)});
  }

  /**
   * Read a matrix from a stream.  The format is the same the print method,
   * so printed matrices can be read back in.  Elements are separated by
   * whitespace, all the elements for each row appear on a single line,
   * the last row is followed by a blank line.
   * @param input the input stream
   */
  public static JamaMatrix read(BufferedReader input)
         throws IOException, VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = read.invoke(null, new Object[] {input});
    return new JamaMatrix(m);
  }

  public JamaCholeskyDecomposition chol()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object c = chol.invoke(matrix, new Object[] {});
    return new JamaCholeskyDecomposition(c, false);
  }

  public JamaEigenvalueDecomposition eig()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object e = eig.invoke(matrix, new Object[] {});
    return new JamaEigenvalueDecomposition(e, false);
  }

  public JamaLUDecomposition lu()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object l = lu.invoke(matrix, new Object[] {});
    return new JamaLUDecomposition(l, false);
  }

  public JamaQRDecomposition qr()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object q = qr.invoke(matrix, new Object[] {});
    return new JamaQRDecomposition(q, false);
  }

  public JamaSingularValueDecomposition svd()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classMatrix == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object s = svd.invoke(matrix, new Object[] {});
    return new JamaSingularValueDecomposition(s, false);
  }

}

