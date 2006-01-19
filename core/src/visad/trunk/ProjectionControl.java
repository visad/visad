//
// ProjectionControl.java
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

package visad;

import java.rmi.*;
import java.util.StringTokenizer;

import visad.browser.Convert;
import visad.util.Util;

/**
   ProjectionControl is the VisAD interface for controlling the Projection
   from 3-D to 2-D.<P>
*/
public abstract class ProjectionControl extends Control {
  /** matrix[] shouldn't be used by non-ProjectionControl classes */
  protected double[] matrix = null;
  private double[] savedProjectionMatrix = null;

  private double[] asp = {1.0, 1.0, 1.0}; // WLH 24 Nov 2000

  /** Length of a 2D matrix */
  public static final int MATRIX2D_LENGTH = 6;
  /** Major dimension of the 2D matrix */
  public static final int MATRIX2D_MAJOR = 3;
  /** Minor dimension of the 2D matrix */
  public static final int MATRIX2D_MINOR = 2;

  /** Length of a 2D matrix */
  public static final int MATRIX3D_LENGTH = 16;
  /** Major dimension of the 3D matrix */
  public static final int MATRIX3D_MAJOR = 4;
  /** Minor dimension of the 3D matrix */
  public static final int MATRIX3D_MINOR = 4;

  /**
   * Construct a ProjectionControl for the display in question.
   * @param d  display to control
   * @throws VisADException  d already has a ProjectionControl or some other
   *                         VisAD Error
   */
  public ProjectionControl(DisplayImpl d) throws VisADException {
    super(d);
    if (d.getProjectionControl() != null) {
      throw new DisplayException("display already has a ProjectionControl");
    }
  }

  /**
   * Returns a copy of the graphics projection matrix.  The matrix has
   * 6 elements in the 2-D case and 16 elements in the 3-D case.
   *
   * @return			A copy of the graphics projection matrix.
   */
  public double[] getMatrix() {
    double[] c = new double[matrix.length];
    System.arraycopy(matrix, 0, c, 0, matrix.length);
    return c;
  }

  /** 
   * Set the matrix that defines the graphics projection 
   * @param m array of the matrix values (16 elements in Java3D case, 
   *          6 elements in Java2D case) 
   * @throws VisADException   invalid matrix length
   * @throws RemoteException  Java RMI failure.
   */
  public void setMatrix(double[] m)
         throws VisADException, RemoteException {
    if (m == null) return;
    if (m.length != matrix.length) {
      throw new DisplayException("setMatrix: input length must be " +
                                 matrix.length);
    }
    System.arraycopy(m, 0, matrix, 0, matrix.length);
  }

  /** 
   * Get a string that can be used to reconstruct this control later 
   * @return String representation of this object that can be used for
   *         reconstruction.
   */
  public String getSaveString() {
    final int len = matrix.length;

    final int major, minor;
    if (len == MATRIX2D_LENGTH) {
      major = MATRIX2D_MAJOR;
      minor = MATRIX2D_MINOR;
    } else if (len == MATRIX3D_LENGTH) {
      major = MATRIX3D_MAJOR;
      minor = MATRIX3D_MINOR;
    } else {
      major = len;
      minor = 1;
    }

    StringBuffer sb = new StringBuffer(25 * len);
    sb.append(major);
    if (minor > 1) {
      sb.append(" x ");
      sb.append(minor);
    }
    sb.append('\n');

    for (int j=0; j<minor; j++) {
      for (int i=0; i<major; i++) {
        if (i > 0) {
          sb.append(' ');
        }
        sb.append(matrix[major * j + i]);
      }
      sb.append('\n');
    }

    return sb.toString();
  }

  /** 
   * Set the properties of this control using the specified save string 
   * @param save  String to use for setting the properties.
   * @throws VisADException   VisAD failure.
   * @throws RemoteException  Java RMI failure.
   */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    int eol = save.indexOf('\n');
    if (eol < 0) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save.substring(0, eol));
    int numTokens = st.countTokens();

    // determine matrix size
    int size = -1;
    if (numTokens == 3) {
      int len = Convert.getInt(st.nextToken());
      if (len < 1) {
        throw new VisADException("First matrix dimension is not positive");
      }
      if (!st.nextToken().equalsIgnoreCase("x")) {
        throw new VisADException("Invalid save string");
      }
      int len0 = Convert.getInt(st.nextToken());
      if (len0 < 1) {
        throw new VisADException("Second matrix dimension is not positive");
      }
      size = len * len0;
    }
    else if (numTokens == 1) {
      size = Convert.getInt(st.nextToken());
      if (size < 1) {
        throw new VisADException("Matrix size is not positive");
      }
    }
    else throw new VisADException("Cannot determine matrix size");

    // get matrix entries
    st = new StringTokenizer(save.substring(eol + 1));
    numTokens = st.countTokens();
    if (numTokens < size) {
      throw new VisADException("Not enough matrix entries");
    }
    double[] m = new double[size];
    for (int i=0; i<size; i++) m[i] = Convert.getDouble(st.nextToken());
    setMatrix(m);
  }

  /** 
   * Set aspect ratio of axes
   * @param aspect ratios; 3 elements for Java3D, 2 for Java2D 
   * @throws VisADException   VisAD failure.
   * @throws RemoteException  Java RMI failure.
   */
  public abstract void setAspect(double[] aspect)
         throws VisADException, RemoteException;

  // WLH 24 Nov 2000
  /** 
   * Set aspect ratio of axes, in ScalarMaps rather than matrix
   * @param aspect ratios; 3 elements for Java3D, 2 for Java2D 
   * @throws VisADException   VisAD failure.
   * @throws RemoteException  Java RMI failure.
   */
  public void setAspectCartesian(double[] aspect)
         throws VisADException, RemoteException {
    if (aspect != null) {
      for (int i=0; i<aspect.length; i++) {
        if (aspect[i] <= 0.0) {
          throw new DisplayException("aspect must be positive");
        }
        asp[i] = aspect[i];
      }
    }
    getDisplay().setAspectCartesian(asp);
  }

  public double[] getAspectCartesian() {
    return (double[]) asp.clone();
  }

  /**
   * Saves the current display projection matrix.  The projection may 
   * later be restored by the method <code>resetProjection()</code>.
   * @see #resetProjection()
   */
  public void saveProjection()
  {
    savedProjectionMatrix = getMatrix();
  }

  /** 
   * Get the matrix that defines the saved graphics projection 
   * @return array of the matrix values (16 elements in Java3D case, 
   *          6 elements in Java2D case) 
   */
  public double[] getSavedProjectionMatrix() {
    double[] c = new double[savedProjectionMatrix.length];
    System.arraycopy(
      savedProjectionMatrix, 0, c, 0, savedProjectionMatrix.length);
    return c;
  }
  /**
   * Restores to projection matrix at time of last <code>saveProjection()</code>
   * call -- if one was made -- or to initial projection otherwise.
   * @see #saveProjection()
   * @throws VisADException   VisAD failure.
   * @throws RemoteException  Java RMI failure.
   */
  public void resetProjection()
         throws VisADException, RemoteException
  {
    setMatrix(savedProjectionMatrix);
  }

  /** Default scaling factor for 2D matrix */
  public static final double SCALE2D = 0.65;
  /** Inverse of  SCALE2D */
  public static final double INVSCALE2D = 1.0 / SCALE2D;

  /**
   * Convert a 2D matrix to a 3D matrix, retaining the scale and aspect
   * of the 2D matrix.
   * @param matrix  2D matrix to convert
   * @throws  VisADException  wrong length for matrix (not MATRIX2D_LENGTH)
   */
  public static double[] matrix2DTo3D(double[] matrix)
         throws VisADException {
    if (matrix.length != MATRIX2D_LENGTH) {
      throw new DisplayException("matrix2DTo3D: input length must be " +
                                 MATRIX2D_LENGTH);
    }
    double[] mat = new double[MATRIX3D_LENGTH];
    for (int i=0; i<MATRIX3D_LENGTH; i++) mat[i] = 0.0;
    mat[0] = SCALE2D * matrix[0];
    mat[1] = SCALE2D * matrix[2];
    mat[3] = matrix[4];
    mat[4] = SCALE2D * matrix[1];
    mat[5] = -SCALE2D * matrix[3];
    mat[7] = -matrix[5];
    mat[10] = 1.0;
    mat[15] = 1.0;
    return mat;
  }

  /**
   * Convert a 3D matrix to a 2D matrix, retaining the scale and aspect
   * of the 3D matrix.
   * @param matrix  3D matrix to convert
   * @throws  VisADException  wrong length for matrix (not MATRIX3D_LENGTH)
   */
  public static double[] matrix3DTo2D(double[] matrix)
         throws VisADException {
    if (matrix.length != MATRIX3D_LENGTH) {
      throw new DisplayException("matrix3DTo2D: input length must be " +
                                 MATRIX3D_LENGTH);
    }
    double[] mat = new double[MATRIX2D_LENGTH];
    mat[0] = INVSCALE2D * matrix[0];
    mat[1] = INVSCALE2D * matrix[4];
    mat[2] = INVSCALE2D * matrix[1];
    mat[3] = -INVSCALE2D * matrix[5];
    mat[4] = matrix[3];
    mat[5] = -matrix[7];
    return mat;
  }

  /**
   * Convert a 3D matrix to a 2D matrix or vice-versa, retaining the scale 
   * and aspect of the original matrix.  Helper interface to pass an unknown
   * matrix to <CODE>matrix3DTo2D</CODE> or <CODE>matrix2DTo3D</CODE>.
   * @see #matrix3DTo2D
   * @see #matrix2DTo3D
   * @param matrix  matrix to convert
   * @throws  VisADException  wrong length for matrix (not MATRIX3D_LENGTH)
   */
  public static double[] matrixDConvert(double[] matrix)
         throws VisADException {
    if (matrix.length == MATRIX3D_LENGTH) {
      return matrix3DTo2D(matrix);
    }
    if (matrix.length == MATRIX2D_LENGTH) {
      return matrix2DTo3D(matrix);
    }
    throw new DisplayException("matrixDConvert: input length must be " +
                               MATRIX3D_LENGTH + " or " + MATRIX2D_LENGTH);
  }

  /** clear all 'pairs' in switches that involve re */
  public void clearSwitches(DataRenderer re) {
  }

  private boolean matrixEquals(double[] newMatrix)
  {
    if (matrix == null) {
      if (newMatrix != null) {
        return false;
      }
    } else if (newMatrix == null) {
      return false;
    } else {
      if (matrix.length != newMatrix.length) {
        return false;
      }

      for (int i = 0; i < matrix.length; i++) {
        if (!Util.isApproximatelyEqual(matrix[i], newMatrix[i])) {
          return false;
        }
      }
    }

    return true;
  }

  // WLH 24 Nov 2000
  private boolean aspEquals(double[] newAsp)
  {
    if (asp == null) {
      if (newAsp != null) {
        return false;
      }
    } else if (newAsp == null) {
      return false;
    } else {
      if (asp.length != newAsp.length) {
        return false;
      }

      for (int i = 0; i < asp.length; i++) {
        if (!Util.isApproximatelyEqual(asp[i], newAsp[i])) {
          return false;
        }
      }
    }

    return true;
  }

  /** 
   * Copy the state of a remote control to this control 
   * @param  rmt  remote control
   * @throws VisADException  rmt is null or not a ProjectionControl or
   *                         some other VisAD error
   */
  public void syncControl(Control rmt)
        throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof ProjectionControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    ProjectionControl pc = (ProjectionControl )rmt;

    if (!matrixEquals(pc.matrix)) {
      try {
        setMatrix(pc.matrix);
      } catch (RemoteException re) {
        throw new VisADException("Could not set matrix: " + re.getMessage());
      }
    }
    // WLH 24 Nov 2000
    if (!aspEquals(pc.asp)) {
      try {
        setAspectCartesian(pc.asp);
      } catch (RemoteException re) {
        throw new VisADException("Could not setAspectCartesian: " + re.getMessage());
      }
    }

  }

  /**
   * Check to see if the object in question is equal to this ProjectionControl.
   * The two are equal if they are both ProjectionControls and their projection
   * matrices are equal.
   * @param o  object in question.
   */
  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    ProjectionControl pc = (ProjectionControl )o;

    if (!matrixEquals(pc.matrix)) {
      return false;
    }

    // WLH 24 Nov 2000
    if (!aspEquals(pc.asp)) {
      return false;
    }

    return true;
  }

  /**
   * Create a clone of this ProjectionControl.
   * @return  clone
   */
  public Object clone()
  {
    ProjectionControl pc = (ProjectionControl )super.clone();
    if (matrix != null) {
      pc.matrix = (double[] )matrix.clone();
    }
    // WLH 24 Nov 2000
    if (asp != null) {
      pc.asp = (double[] )asp.clone();
    }

    return pc;
  }

  /**
   * A string representation of this ProjectionControl.
   * @return human readable string that tells the properties of this control.
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer("ProjectionControl[");
    if (matrix == null) {
      buf.append("null");
    } else {
      int major, minor;
      if (matrix.length == MATRIX2D_LENGTH) {
        major = MATRIX2D_MAJOR;
        minor = MATRIX2D_MINOR;
      } else if (matrix.length == MATRIX3D_LENGTH) {
        major = MATRIX3D_MAJOR;
        minor = MATRIX3D_MINOR;
      } else {
        major = 1;
        minor = matrix.length;
      }

      int offset = 0;
      for (int i = 0; i < major; i++) {
        if (i > 0) {
          buf.append(',');
        }
        for (int j = 0; j < minor; j++) {
          buf.append(j == 0 ? '(' : ',');
          buf.append(matrix[offset + j]);
        }
        buf.append(')');
        offset += minor;
      }
    }

    buf.append(']');
    return buf.toString();
  }
}

