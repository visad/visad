
//
// ProjectionControlJ2D.java
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

package visad.java2d;
 
import visad.*;

import java.awt.geom.AffineTransform;

import java.rmi.*;

/**
   ProjectionControlJ2D is the VisAD class for controlling the Projection
   from 3-D to 2-D.  It manipulates a TransformGroup node in the
   scene graph.<P>
*/
public class ProjectionControlJ2D extends ProjectionControl {

  private transient AffineTransform Matrix;
  private double[] matrix;

  private VisADCanvasJ2D canvas;

  public ProjectionControlJ2D(DisplayImpl d) {
    super(d);
    Matrix = new AffineTransform();
    matrix = new double[6];
    Matrix.getMatrix(matrix);
    if (d != null) {
      canvas = ((DisplayRendererJ2D) d.getDisplayRenderer()).getCanvas();
    }
  }
 
  public double[] getMatrix() {
    double[] c = new double[6];
    System.arraycopy(matrix, 0, c, 0, 6);
    return c;
  }

  public void setMatrix(double[] m)
         throws VisADException, RemoteException {
    System.arraycopy(m, 0, matrix, 0, 6);
    Matrix = new AffineTransform(matrix);
    ((DisplayRendererJ2D) getDisplayRenderer()).setTransform2D(Matrix);
    canvas.scratchImages();
    changeControl(true);
  }

}

