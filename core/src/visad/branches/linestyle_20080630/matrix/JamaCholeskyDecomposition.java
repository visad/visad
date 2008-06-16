//
// JamaCholeskyDecomposition.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
 * JamaCholeskyDecomposition is a VisAD wrapper for JAMA CholeskyDecompositions.
 * This class requires the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA package</a>.
 */
public class JamaCholeskyDecomposition extends Tuple {

  private static final RealType cholesky_row =
    RealType.getRealType("choleskyL_row");

  private static final RealType cholesky_column =
    RealType.getRealType("choleskyL_column");

  private static final RealType cholesky_value =
    RealType.getRealType("choleskyL_value");

  private static final FunctionType choleskyLType = constructFunction();

  private static FunctionType constructFunction() {
    try {
      RealTupleType tuple = new RealTupleType(cholesky_row, cholesky_column);
      FunctionType function = new FunctionType(tuple, cholesky_value);
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

  /** associated JAMA CholeskyDecomposition object */
  private Object cd;

  /** useful methods from Jama.CholeskyDecomposition class */
  private static final Method[] methods =
    constructMethods();

  private static Method[] constructMethods() {
    Method[] ms = new Method[3];
    try {
      Class[] param = new Class[] {};
      ms[0] = classCholeskyDecomposition.getMethod("getL", param);
      ms[1] = classCholeskyDecomposition.getMethod("isSPD", param);
      param = new Class[] {classMatrix};
      ms[2] = classCholeskyDecomposition.getMethod("solve", param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ms;
  }

  private static final Method getL = methods[0];
  private static final Method isSPD = methods[1];
  private static final Method solve = methods[2];

  private static final Constructor matrixCholeskyDecomposition =
    constructConstructor();

  private static Constructor constructConstructor() {
    try {
      Class[] param = new Class[] {classMatrix};
      return classCholeskyDecomposition.getConstructor(param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Constructors

  /**
   * Construct a new JamaCholeskyDecomposition from a JamaMatrix.
   */
  public JamaCholeskyDecomposition(JamaMatrix matrix)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrixCholeskyDecomposition.newInstance(new Object[] {matrix.getMatrix()}),
         false);
  }

  JamaCholeskyDecomposition(Object c, boolean copy)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    super(makeDatums(c), copy);
    cd = ((JamaMatrix) getComponent(0)).getStash();
  }

  private static Data[] makeDatums(Object c)
          throws VisADException, IllegalAccessException,
                 InstantiationException, InvocationTargetException {
    Object m = getL.invoke(c, new Object[] {});

    JamaMatrix jm =
      new JamaMatrix(m, choleskyLType, null, null, null, null, null);

    jm.setStash(c);
    return new Data[] {jm};
  }


  // New methods

  /**
   * Return the associated JAMA CholeskyDecomposition object.
   */
  public Object getCholeskyDecomposition() {
    return cd;
  }

  // Method wrappers for JAMA Matrix functionality

  /**
   * Get L
   * @return     L matrix
   */
  public JamaMatrix getL() throws VisADException, RemoteException {
    if (classCholeskyDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(0);
  }

  public boolean isSPD()
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classCholeskyDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    boolean spd = ((Boolean)
      isSPD.invoke(cd, new Object[] {})).booleanValue();
    return spd;
  }

  /**
   * Solve A*X = B
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
  public JamaMatrix solve(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classCholeskyDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = solve.invoke(cd, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

}

