//
// JamaLUDecomposition.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.RemoteException;

import visad.*;

/**
 * JamaLUDecomposition is a VisAD wrapper for JAMA LUDecompositions.
 * This class requires the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA package</a>.
 */
public class JamaLUDecomposition extends Tuple {

  private static final RealType LUL_row =
    RealType.getRealType("LU_L_row");

  private static final RealType LUL_column =
    RealType.getRealType("LU_L_column");

  private static final RealType LUL_value =
    RealType.getRealType("LU_L_value");

  private static final FunctionType LULType = constructLFunction();

  private static FunctionType constructLFunction() {
    try {
      RealTupleType tuple = new RealTupleType(LUL_row, LUL_column);
      FunctionType function = new FunctionType(tuple, LUL_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType LUU_row =
    RealType.getRealType("LU_U_row");

  private static final RealType LUU_column =
    RealType.getRealType("LU_U_column");

  private static final RealType LUU_value =
    RealType.getRealType("LU_U_value");

  private static final FunctionType LUUType = constructUFunction();

  private static FunctionType constructUFunction() {
    try {
      RealTupleType tuple = new RealTupleType(LUU_row, LUU_column);
      FunctionType function = new FunctionType(tuple, LUU_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType pivot_domain =
    RealType.getRealType("pivot_domain");

  private static final RealType pivot_value =
    RealType.getRealType("pivot_value");

  private static final FunctionType pivotType = constructPFunction();

  private static FunctionType constructPFunction() {
    try {
      FunctionType function = new FunctionType(pivot_domain, pivot_value);
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

  /** associated JAMA LUDecomposition object */
  private Object lud;

  /** useful methods from Jama.LUDecomposition class */
  private static final Method[] methods =
    constructMethods();

  private static Method[] constructMethods() {
    Method[] ms = new Method[7];
    try {
      Class[] param = new Class[] {};
      ms[0] = classLUDecomposition.getMethod("getL", param);
      ms[1] = classLUDecomposition.getMethod("getU", param);
      ms[2] = classLUDecomposition.getMethod("getPivot", param);
      ms[3] = classLUDecomposition.getMethod("getDoublePivot", param);
      ms[4] = classLUDecomposition.getMethod("det", param);
      ms[5] = classLUDecomposition.getMethod("isNonsingular", param);
      param = new Class[] {classMatrix};
      ms[6] = classLUDecomposition.getMethod("solve", param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ms;
  }

  private static final Method getL = methods[0];
  private static final Method getU = methods[1];
  private static final Method getPivot = methods[2];
  private static final Method getDoublePivot = methods[3];
  private static final Method det = methods[4];
  private static final Method isNonsingular = methods[5];
  private static final Method solve = methods[6];

  private static final Constructor matrixLUDecomposition =
    constructConstructor();

  private static Constructor constructConstructor() {
    try {
      Class[] param = new Class[] {classMatrix};
      return classLUDecomposition.getConstructor(param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Constructors

  /**
   * Construct a new JamaLUDecomposition from a JamaMatrix.
   */
  public JamaLUDecomposition(JamaMatrix matrix)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrixLUDecomposition.newInstance(new Object[] {matrix.getMatrix()}),
         false);
  }

  JamaLUDecomposition(Object lu, boolean copy)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    super(makeDatums(lu), copy);
    lud = ((JamaMatrix) getComponent(0)).getStash();
  }

  private static Data[] makeDatums(Object lu)
          throws VisADException, RemoteException, IllegalAccessException,
                 InstantiationException, InvocationTargetException {
    Object l = getL.invoke(lu, new Object[] {});
    JamaMatrix jl =
      new JamaMatrix(l, LULType, null, null, null, null, null);
    jl.setStash(lu);

    Object u = getU.invoke(lu, new Object[] {});
    JamaMatrix ju =
      new JamaMatrix(u, LUUType, null, null, null, null, null);


    double[] pivot = (double[]) getDoublePivot.invoke(lu, new Object[] {});
    FlatField pf = new FlatField(pivotType, new Integer1DSet(pivot.length));
    pf.setSamples(new double[][] {pivot});

    return new Data[] {jl, ju, pf};
  }


  // New methods

  /**
   * Return the associated JAMA LUDecomposition object.
   */
  public Object getLUDecomposition() {
    return lud;
  }

  // Method wrappers for JAMA Matrix functionality

  /**
   * Get L
   * @return     L matrix
   */
  public JamaMatrix getL() throws VisADException, RemoteException {
    if (classLUDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(0);
  }

  /**
   * Get U
   * @return     U matrix
   */
  public JamaMatrix getU() throws VisADException, RemoteException {
    if (classLUDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(1);
  }

  public double det()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classLUDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val = ((Double) det.invoke(lud, new Object[] {})).doubleValue();
    return val;
  }

  public double[] getDoublePivot() throws VisADException, RemoteException {
    if (classLUDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    FlatField pf = (FlatField) getComponent(2);
    double[][] p = pf.getValues(false);
    return p[0];
  }

  public int[] getPivot()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classLUDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    int[] p = (int[]) getPivot.invoke(lud, new Object[] {});
    return p;
  }

  public boolean isNonsingular()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classLUDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    boolean ns =
      ((Boolean) isNonsingular.invoke(lud, new Object[] {})).booleanValue();
    return ns;
  }

  /**
   * Solve A*X = B
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
  public JamaMatrix solve(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classLUDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = solve.invoke(lud, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

}

