
//
// ProjectionControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

import javax.media.j3d.*;
import java.vecmath.*;

/**
   ProjectionControl is the VisAD class for controlling the Projection
   from 3-D to 2-D.  It manipulates a TransformGroup node in the
   scene graph.<P>
*/
public class ProjectionControl extends Control {

  private transient Transform3D Matrix;
  private double[] matrix;

  static final ProjectionControl prototype = new ProjectionControl();

  public ProjectionControl(DisplayImpl d) {
    super(d);
    Matrix = new Transform3D(); 
    matrix = new double[16];
    Matrix.get(matrix);
  }
 
  ProjectionControl() {
    this(null);
  }

  public double[] getMatrix() {
    return matrix;
  }

  public void setMatrix(double[] m) {
    System.arraycopy(m, 0, matrix, 0, 16);
    Matrix = new Transform3D(matrix);
    displayRenderer.setTransform3D(Matrix);
    changeControl();
  }

  public Control cloneButContents(DisplayImpl d) {
    ProjectionControl control = new ProjectionControl(d);
    return control;
  }

}

