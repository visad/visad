
//
// ProjectionControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.rmi.*;

/**
   ProjectionControl is the VisAD interface for controlling the Projection
   from 3-D to 2-D.<P>
*/
public abstract class ProjectionControl extends Control {

  public ProjectionControl(DisplayImpl d) throws VisADException {
    super(d);
    if (d.getProjectionControl() != null) {
      throw new DisplayException("display already has a ProjectionControl");
    }
  }

  /** get matrix (16 elements in Java3D case, 6 elements in
      Java2D case) that defines the graphics projection */
  public abstract double[] getMatrix();

  /** set matrix (16 elements in Java3D case, 6 elements in
      Java2D case) that defines the graphics projection */
  public abstract void setMatrix(double[] m)
         throws VisADException, RemoteException;

  /** set aspect ratio; 3 elements for Java3D, 2 for Java2D */
  public abstract void setAspect(double[] aspect)
         throws VisADException, RemoteException;

  public static double[] matrix2DTo3D(double[] matrix)
         throws VisADException {
    if (matrix.length != 6) {
      throw new DisplayException("matrix2DTo3D: input length must be 6");
    }
    double[] mat = new double[16];
    for (int i=0; i<16; i++) mat[i] = 0.0;
    mat[0] = matrix[0];
    mat[4] = matrix[1];
    mat[1] = matrix[2];
    mat[5] = matrix[3];
    mat[2] = matrix[4];
    mat[6] = matrix[5];
    mat[10] = 1.0;
    mat[15] = 1.0;
    return mat;
  }

  public static double[] matrix3DTo2D(double[] matrix) 
         throws VisADException {
    if (matrix.length != 16) {
      throw new DisplayException("matrix3DTo2D: input length must be 6");
    }
    double[] mat = new double[6];
    mat[0] = matrix[0];
    mat[1] = matrix[4];
    mat[2] = matrix[1];
    mat[3] = matrix[5];
    mat[4] = matrix[1];
    mat[5] = matrix[6];
    return mat;
  }

}

