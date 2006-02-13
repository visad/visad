//
// JamaEigenvalueDecomposition.java
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

package visad.matrix;

import java.lang.reflect.*;
import java.rmi.RemoteException;

import visad.*;

/**
 * JamaEigenvalueDecomposition is a VisAD wrapper for JAMA
 * EigenvalueDecompositions.
 * This class requires the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA package</a>.
 */
public class JamaEigenvalueDecomposition extends Tuple {

  private static final RealType eigenV_row =
    RealType.getRealType("eigenV_row");

  private static final RealType eigenV_column =
    RealType.getRealType("eigenV_column");

  private static final RealType eigenV_value =
    RealType.getRealType("eigenV_value");

  private static final FunctionType eigenVType = constructEVFunction();

  private static FunctionType constructEVFunction() {
    try {
      RealTupleType tuple = new RealTupleType(eigenV_row, eigenV_column);
      FunctionType function = new FunctionType(tuple, eigenV_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType eigen_domain =
    RealType.getRealType("eigen_domain");

  private static final RealType eigen_real =
    RealType.getRealType("eigen_real");

  private static final RealType eigen_imaginary =
    RealType.getRealType("eigen_imaginary");

  private static final FunctionType eigenRType = constructERFunction();

  private static FunctionType constructERFunction() {
    try {
      FunctionType function = new FunctionType(eigen_domain, eigen_real);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final FunctionType eigenIType = constructEIFunction();

  private static FunctionType constructEIFunction() {
    try {
      FunctionType function = new FunctionType(eigen_domain, eigen_imaginary);
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

  /** associated JAMA EigenvalueDecomposition object */
  private Object ed;

  /** useful methods from Jama.EigenvalueDecomposition class */
  private static final Method[] methods =
    constructMethods();

  private static Method[] constructMethods() {
    Method[] ms = new Method[4];
    try {
      Class[] param = new Class[] {};
      ms[0] = classEigenvalueDecomposition.getMethod("getD", param);
      ms[1] = classEigenvalueDecomposition.getMethod("getV", param);
      ms[2] =
        classEigenvalueDecomposition.getMethod("getImagEigenvalues", param);
      ms[3] =
        classEigenvalueDecomposition.getMethod("getRealEigenvalues", param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ms;
  }

  private static final Method getD = methods[0];
  private static final Method getV = methods[1];
  private static final Method getImagEigenvalues = methods[2];
  private static final Method getRealEigenvalues = methods[3];

  private static final Constructor matrixEigenvalueDecomposition =
    constructConstructor();

  private static Constructor constructConstructor() {
    try {
      Class[] param = new Class[] {classMatrix};
      return classEigenvalueDecomposition.getConstructor(param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Constructors

  /**
   * Construct a new JamaEigenvalueDecomposition from a JamaMatrix.
   */
  public JamaEigenvalueDecomposition(JamaMatrix matrix)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrixEigenvalueDecomposition.newInstance(new Object[] {matrix.getMatrix()}),
         false);
  }

  JamaEigenvalueDecomposition(Object e, boolean copy)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    super(makeDatums(e), copy);
    ed = ((JamaMatrix) getComponent(0)).getStash();
  }

  private static Data[] makeDatums(Object e)
          throws VisADException, RemoteException, IllegalAccessException,
                 InstantiationException, InvocationTargetException {
    Object m = getV.invoke(e, new Object[] {});
    JamaMatrix jm =
      new JamaMatrix(m, eigenVType, null, null, null, null, null);
    jm.setStash(e);

    double[] rs = (double[]) getRealEigenvalues.invoke(e, new Object[] {});
    FlatField rf = new FlatField(eigenRType, new Integer1DSet(rs.length));
    rf.setSamples(new double[][] {rs});
    double[] ims = (double[]) getImagEigenvalues.invoke(e, new Object[] {});
    FlatField imf = new FlatField(eigenIType, new Integer1DSet(ims.length));
    imf.setSamples(new double[][] {ims});

    return new Data[] {jm, rf, imf};
  }


  // New methods

  /**
   * Return the associated JAMA EigenvalueDecomposition object.
   */
  public Object getEigenvalueDecomposition() {
    return ed;
  }

  // Method wrappers for JAMA Matrix functionality

  /**
   * Get V
   * @return     V matrix
   */
  public JamaMatrix getV() throws VisADException, RemoteException {
    if (classEigenvalueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(0);
  }

  /**
   * Get D
   * @return     D matrix
   */
  public JamaMatrix getD()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classEigenvalueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = getD.invoke(ed, new Object[] {});
    return new JamaMatrix(m);
  }

  public double[] getRealEigenvalues() throws VisADException, RemoteException {
    if (classEigenvalueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    FlatField rf = (FlatField) getComponent(1);
    double[][] v = rf.getValues(false);
    return v[0];
  }

  public double[] getImagEigenvalues() throws VisADException, RemoteException {
    if (classEigenvalueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    FlatField imf = (FlatField) getComponent(2);
    double[][] v = imf.getValues(false);
    return v[0];
  }

}

