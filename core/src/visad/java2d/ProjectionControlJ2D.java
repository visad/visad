//
// ProjectionControlJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

  private transient VisADCanvasJ2D canvas;

  /** 
   * Construct a new ProjectionControl for the display in question.
   * @param d  display to control.
   * @throws  VisADException    some VisAD error
   */
  public ProjectionControlJ2D(DisplayImpl d) throws VisADException {
    super(d);
/* WLH 5 April 99
    Matrix = new AffineTransform();
*/
    Matrix = init();
    matrix = new double[MATRIX2D_LENGTH];
    Matrix.getMatrix(matrix);
    ((DisplayRendererJ2D) getDisplayRenderer()).setTransform2D(Matrix);
    canvas = null;
    saveProjection();
  }

  /** 
   * Set the matrix that defines the graphics projection 
   * @param m array of the matrix values (6 elements in Java2D case) 
   * @throws VisADException   some VisAD error
   * @throws RemoteException  Java RMI failure.
   */
  public void setMatrix(double[] m)
         throws VisADException, RemoteException {
    super.setMatrix(m);
    Matrix = new AffineTransform(matrix);
    DisplayRendererJ2D dr = (DisplayRendererJ2D) getDisplayRenderer();
    dr.setTransform2D(Matrix);
    if (canvas == null) {
      canvas = dr.getCanvas();
    }
    canvas.scratchImages();
    // WLH 5 May 2000
    // changeControl(true);
    changeControl(false);
  }

  /** 
   * Set aspect ratio of axes.
   * @param aspect ratios; 2 elements for Java2D 
   * @throws VisADException   invalid array length or some other VisAD failure.
   * @throws RemoteException  Java RMI failure.
   */
  public void setAspect(double[] aspect)
         throws VisADException, RemoteException {
    if (aspect == null || aspect.length != 2) {
      throw new DisplayException("aspect array must be length = 2");
    }
    AffineTransform transform = new AffineTransform();
    transform.setToScale(aspect[0], aspect[1]);
    double[] mult = new double[MATRIX2D_LENGTH];
    transform.getMatrix(mult);
    AffineTransform mat = init();
    double[] m = new double[MATRIX2D_LENGTH];
    mat.getMatrix(m);
    setMatrix(getDisplay().multiply_matrix(mult, m));
    saveProjection();
  }

  private AffineTransform init() {
    AffineTransform mat = new AffineTransform();
    // SWAP flip y
    AffineTransform t1 = new AffineTransform(1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
    mat.concatenate(t1);
    return mat;
  }

}
