//
// ProjectionControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

/**
   ProjectionControl is the VisAD interface for controlling the Projection
   from 3-D to 2-D.<P>
*/
public abstract class ProjectionControl extends Control {
  /** matrix[] shouldn't be used by non-ProjectionControl classes */
  protected double[] matrix = null;

  public static final int MATRIX2D_LENGTH = 6;
  public static final int MATRIX3D_LENGTH = 16;

  public ProjectionControl(DisplayImpl d) throws VisADException {
    super(d);
    if (d.getProjectionControl() != null) {
      throw new DisplayException("display already has a ProjectionControl");
    }
  }

  /** get matrix (16 elements in Java3D case, 6 elements in
      Java2D case) that defines the graphics projection */
  public double[] getMatrix() {
    double[] c = new double[matrix.length];
    System.arraycopy(matrix, 0, c, 0, matrix.length);
    return c;
  }

  /** set matrix (16 elements in Java3D case, 6 elements in
      Java2D case) that defines the graphics projection */
  public void setMatrix(double[] m)
         throws VisADException, RemoteException {
    if (m.length != matrix.length) {
      throw new DisplayException("setMatrix: input length must be " +
                                 matrix.length);
    }
    System.arraycopy(m, 0, matrix, 0, matrix.length);
  }

  /** get a String that can be used to reconstruct this
      ProjectionControl later */
  public String getSaveString() {
    int len = matrix.length;
    String s;
    if (len == 6) {
      s = "3 x 2\n";
      for (int j=0; j<2; j++) {
        for (int i=0; i<2; i++) s = s + matrix[3 * j + i] + " ";
        s = s + matrix[3 * j + 2] + "\n";
      }
    }
    else if (len == 16) {
      s = "4 x 4\n";
      for (int j=0; j<4; j++) {
        for (int i=0; i<3; i++) s = s + matrix[4 * j + i] + " ";
        s = s + matrix[4 * j + 3] + "\n";
      }
    }
    else {
      s = len + "\n";
      for (int i=0; i<len; i++) s = s + " " + matrix[i];
    }
    return s;
  }

  /** reconstruct this ProjectionControl using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    int eol = save.indexOf('\n');
    String size = save.substring(0, eol);
    int len = -1;
    if (size.equals("3 x 2")) len = 6;
    else if (size.equals("4 x 4")) len = 16;
    else {
      try {
        len = Integer.parseInt(size);
      }
      catch (NumberFormatException exc) { }
    }
    StringTokenizer st = new StringTokenizer(save.substring(eol));
    if (len < 0) len = st.countTokens();
    double[] m = new double[len];
    for (int i=0; i<len; i++) m[i] = Double.parseDouble(st.nextToken());
    setMatrix(m);
  }

  /** set aspect ratio; 3 elements for Java3D, 2 for Java2D */
  public abstract void setAspect(double[] aspect)
         throws VisADException, RemoteException;

  public static final double SCALE2D = 0.65;
  public static final double INVSCALE2D = 1.0 / SCALE2D;

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
        if (Math.abs(matrix[i] - newMatrix[i]) > 0.0001) {
          return false;
        }
      }
    }

    return true;
  }

  /** copy the state of a remote control to this control */
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
  }

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    ProjectionControl pc = (ProjectionControl )o;

    if (!matrixEquals(pc.matrix)) {
      return false;
    }

    return true;
  }

}

