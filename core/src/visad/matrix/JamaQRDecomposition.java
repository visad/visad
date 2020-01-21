//
// JamaQRDecomposition.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
 * JamaQRDecomposition is a VisAD wrapper for JAMA QRDecompositions.
 * This class requires the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA package</a>.
 */
public class JamaQRDecomposition extends Tuple {

  private static final RealType QRQ_row =
    RealType.getRealType("QR_Q_row");

  private static final RealType QRQ_column =
    RealType.getRealType("QR_Q_column");

  private static final RealType QRQ_value =
    RealType.getRealType("QR_Q_value");

  private static final FunctionType QRQType = constructQFunction();

  private static FunctionType constructQFunction() {
    try {
      RealTupleType tuple = new RealTupleType(QRQ_row, QRQ_column);
      FunctionType function = new FunctionType(tuple, QRQ_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType QRR_row =
    RealType.getRealType("QR_R_row");

  private static final RealType QRR_column =
    RealType.getRealType("QR_R_column");

  private static final RealType QRR_value =
    RealType.getRealType("QR_R_value");

  private static final FunctionType QRRType = constructRFunction();

  private static FunctionType constructRFunction() {
    try {
      RealTupleType tuple = new RealTupleType(QRR_row, QRR_column);
      FunctionType function = new FunctionType(tuple, QRR_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType QRH_row =
    RealType.getRealType("QR_H_row");

  private static final RealType QRH_column =
    RealType.getRealType("QR_H_column");

  private static final RealType QRH_value =
    RealType.getRealType("QR_H_value");

  private static final FunctionType QRHType = constructHFunction();

  private static FunctionType constructHFunction() {
    try {
      RealTupleType tuple = new RealTupleType(QRH_row, QRH_column);
      FunctionType function = new FunctionType(tuple, QRH_value);
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

  /** associated JAMA QRDecomposition object */
  private Object qrd;

  /** useful methods from Jama.QRDecomposition class */
  private static final Method[] methods =
    constructMethods();

  private static Method[] constructMethods() {
    Method[] ms = new Method[5];
    try {
      Class[] param = new Class[] {};
      ms[0] = classQRDecomposition.getMethod("getH", param);
      ms[1] = classQRDecomposition.getMethod("getQ", param);
      ms[2] = classQRDecomposition.getMethod("getR", param);
      ms[3] = classQRDecomposition.getMethod("isFullRank", param);
      param = new Class[] {classMatrix};
      ms[4] = classQRDecomposition.getMethod("solve", param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ms;
  }

  private static final Method getH = methods[0];
  private static final Method getQ = methods[1];
  private static final Method getR = methods[2];
  private static final Method isFullRank = methods[3];
  private static final Method solve = methods[4];

  private static final Constructor matrixQRDecomposition =
    constructConstructor();

  private static Constructor constructConstructor() {
    try {
      Class[] param = new Class[] {classMatrix};
      return classQRDecomposition.getConstructor(param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Constructors

  /**
   * Construct a new JamaQRDecomposition from a JamaMatrix.
   */
  public JamaQRDecomposition(JamaMatrix matrix)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrixQRDecomposition.newInstance(new Object[] {matrix.getMatrix()}),
         false);
  }

  JamaQRDecomposition(Object qr, boolean copy)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    super(makeDatums(qr), copy);
    qrd = ((JamaMatrix) getComponent(0)).getStash();
  }

  private static Data[] makeDatums(Object qr)
          throws VisADException, RemoteException, IllegalAccessException,
                 InstantiationException, InvocationTargetException {
    Object q = getQ.invoke(qr, new Object[] {});
    JamaMatrix jq =
      new JamaMatrix(q, QRQType, null, null, null, null, null);
    jq.setStash(qr);

    Object r = getR.invoke(qr, new Object[] {});
    JamaMatrix jr =
      new JamaMatrix(r, QRQType, null, null, null, null, null);

    return new Data[] {jq, jr};
  }


  // New methods

  /**
   * Return the associated JAMA QRDecomposition object.
   */
  public Object getQRDecomposition() {
    return qrd;
  }

  // Method wrappers for JAMA Matrix functionality

  /**
   * Get Q
   * @return     Q matrix
   */
  public JamaMatrix getQ() throws VisADException, RemoteException {
    if (classQRDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(0);
  }

  /**
   * Get R
   * @return     R matrix
   */
  public JamaMatrix getR() throws VisADException, RemoteException {
    if (classQRDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(1);
  }

  public JamaMatrix getH()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classQRDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = getH.invoke(qrd, new Object[] {});
    return new JamaMatrix(m, QRHType, null, null, null, null, null);
  }

  public boolean isFullRank()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classQRDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    boolean fr =
      ((Boolean) isFullRank.invoke(qrd, new Object[] {})).booleanValue();
    return fr;
  }

  /**
   * Solve A*X = B
   * @param B    right hand side
   * @return     solution if A is square, least squares solution otherwise
   */
  public JamaMatrix solve(JamaMatrix B)
         throws VisADException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classQRDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = solve.invoke(qrd, new Object[] {B.getMatrix()});
    return new JamaMatrix(m);
  }

}

