
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

  public ProjectionControlJ2D(DisplayImpl d) throws VisADException {
    super(d);
/* WLH 5 April 99
    Matrix = new AffineTransform();
*/
    Matrix = init();
    matrix = new double[6];
    Matrix.getMatrix(matrix);
    ((DisplayRendererJ2D) getDisplayRenderer()).setTransform2D(Matrix);
    canvas = null;
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
    DisplayRendererJ2D dr = (DisplayRendererJ2D) getDisplayRenderer();
    dr.setTransform2D(Matrix);
    if (canvas == null) {
      canvas = dr.getCanvas();
    }
    canvas.scratchImages();
    changeControl(true);
  }

  public void setAspect(double[] aspect)
         throws VisADException, RemoteException {
    if (aspect == null || aspect.length != 2) {
      throw new DisplayException("aspect array must be length = 2");
    }
    AffineTransform transform = new AffineTransform();
    transform.setToScale(aspect[0], aspect[1]);
    double[] mult = new double[6];
    transform.getMatrix(mult);
    AffineTransform mat = init();
    double[] m = new double[6];
    mat.getMatrix(m);
    setMatrix(getDisplay().multiply_matrix(mult, m));
  }

  private AffineTransform init() {
    AffineTransform mat = new AffineTransform();
    // SWAP flip y
    AffineTransform t1 = new AffineTransform(1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
    mat.concatenate(t1);
    return mat;
  }

}

