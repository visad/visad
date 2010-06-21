//
// JamaSingularValueDecomposition.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
 * JamaSingularValueDecomposition is a VisAD wrapper for JAMA
 * SingularValueDecompositions.
 * This class requires the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA package</a>.
 */
public class JamaSingularValueDecomposition extends Tuple {

  private static final RealType SVS_row =
    RealType.getRealType("SV_S_row");

  private static final RealType SVS_column =
    RealType.getRealType("SV_S_column");

  private static final RealType SVS_value =
    RealType.getRealType("SV_S_value");

  private static final FunctionType SVSType = constructSFunction();

  private static FunctionType constructSFunction() {
    try {
      RealTupleType tuple = new RealTupleType(SVS_row, SVS_column);
      FunctionType function = new FunctionType(tuple, SVS_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType SVV_row =
    RealType.getRealType("SV_V_row");

  private static final RealType SVV_column =
    RealType.getRealType("SV_V_column");

  private static final RealType SVV_value =
    RealType.getRealType("SV_V_value");

  private static final FunctionType SVVType = constructVFunction();

  private static FunctionType constructVFunction() {
    try {
      RealTupleType tuple = new RealTupleType(SVV_row, SVV_column);
      FunctionType function = new FunctionType(tuple, SVV_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType SVU_row =
    RealType.getRealType("SV_H_row");

  private static final RealType SVU_column =
    RealType.getRealType("SV_H_column");

  private static final RealType SVU_value =
    RealType.getRealType("SV_H_value");

  private static final FunctionType SVUType = constructHFunction();

  private static FunctionType constructHFunction() {
    try {
      RealTupleType tuple = new RealTupleType(SVU_row, SVU_column);
      FunctionType function = new FunctionType(tuple, SVU_value);
      return function;
    }
    catch (VisADException exc) {
      exc.printStackTrace();
      return null;
    }
  }

  private static final RealType singular_domain =
    RealType.getRealType("singular_domain");

  private static final RealType singular_value =
    RealType.getRealType("singular_value");

  private static final FunctionType singularType = constructSVFunction();

  private static FunctionType constructSVFunction() {
    try {
      FunctionType function = new FunctionType(singular_domain, singular_value);
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

  /** associated JAMA SingularValueDecomposition object */
  private Object svd;

  /** useful methods from Jama.SVDecomposition class */
  private static final Method[] methods =
    constructMethods();

  private static Method[] constructMethods() {
    Method[] ms = new Method[7];
    try {
      Class[] param = new Class[] {};
      ms[0] = classSingularValueDecomposition.getMethod("getU", param);
      ms[1] = classSingularValueDecomposition.getMethod("getS", param);
      ms[2] = classSingularValueDecomposition.getMethod("getV", param);
      ms[3] = classSingularValueDecomposition.getMethod("getSingularValues", param);
      ms[4] = classSingularValueDecomposition.getMethod("cond", param);
      ms[5] = classSingularValueDecomposition.getMethod("norm2", param);
      ms[6] = classSingularValueDecomposition.getMethod("rank", param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ms;
  }

  private static final Method getU = methods[0];
  private static final Method getS = methods[1];
  private static final Method getV = methods[2];
  private static final Method getSingularValues = methods[3];
  private static final Method cond = methods[4];
  private static final Method norm2 = methods[4];
  private static final Method rank = methods[4];

  private static final Constructor matrixSVDecomposition =
    constructConstructor();

  private static Constructor constructConstructor() {
    try {
      Class[] param = new Class[] {classMatrix};
      return classSingularValueDecomposition.getConstructor(param);
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Constructors

  /**
   * Construct a new JamaSingularValueDecomposition from a JamaMatrix.
   */
  public JamaSingularValueDecomposition(JamaMatrix matrix)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    this(matrixSVDecomposition.newInstance(new Object[] {matrix.getMatrix()}),
         false);
  }

  JamaSingularValueDecomposition(Object sv, boolean copy)
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    super(makeDatums(sv), copy);
    svd = ((JamaMatrix) getComponent(0)).getStash();
  }

  private static Data[] makeDatums(Object sv)
          throws VisADException, RemoteException, IllegalAccessException,
                 InstantiationException, InvocationTargetException {
    Object u = getU.invoke(sv, new Object[] {});
    JamaMatrix ju =
      new JamaMatrix(u, SVUType, null, null, null, null, null);
    ju.setStash(sv);

    Object v = getV.invoke(sv, new Object[] {});
    JamaMatrix jv =
      new JamaMatrix(v, SVVType, null, null, null, null, null);

    double[] singular = (double[]) getSingularValues.invoke(sv, new Object[] {});
    FlatField sf = new FlatField(singularType, new Integer1DSet(singular.length));
    sf.setSamples(new double[][] {singular});

    return new Data[] {ju, jv, sf};
  }


  // New methods

  /**
   * Return the associated JAMA SVDecomposition object.
   */
  public Object getSVDecomposition() {
    return svd;
  }

  // Method wrappers for JAMA Matrix functionality

  /**
   * Get U
   * @return     U matrix
   */
  public JamaMatrix getU() throws VisADException, RemoteException {
    if (classSingularValueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(0);
  }

  /**
   * Get V
   * @return     V matrix
   */
  public JamaMatrix getV() throws VisADException, RemoteException {
    if (classSingularValueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    return (JamaMatrix) getComponent(1);
  }

  public JamaMatrix getS()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classSingularValueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    Object m = getS.invoke(svd, new Object[] {});
    return new JamaMatrix(m, SVSType, null, null, null, null, null);
  }

  public double[] getSingularValues() throws VisADException, RemoteException {
    if (classSingularValueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    FlatField sf = (FlatField) getComponent(2);
    double[][] s = sf.getValues(false);
    return s[0];
  }

  public double cond()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classSingularValueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val =
      ((Double) cond.invoke(svd, new Object[] {})).doubleValue();
    return val;
  }

  public double norm2() 
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classSingularValueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    double val =
      ((Double) norm2.invoke(svd, new Object[] {})).doubleValue();
    return val;
  }

  public int rank()
         throws VisADException, RemoteException, IllegalAccessException,
                InstantiationException, InvocationTargetException {
    if (classSingularValueDecomposition == null) {
      throw new VisADException("you need to install Jama from " +
                               "http://math.nist.gov/javanumerics/jama/");
    }
    int val =
      ((Integer) rank.invoke(svd, new Object[] {})).intValue();
    return val;
  }

}

