
//
// MouseBehaviorJ2D.java
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
 
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import java.rmi.*;
import java.awt.*;
import java.util.*;

/**
   MouseBehaviorJ2D is the VisAD class for mouse behaviors for Java2D
*/

public class MouseBehaviorJ2D implements MouseBehavior {

  /** DisplayRenderer for Display */
  DisplayRendererJ2D display_renderer;
  DisplayImpl display;

  private MouseHelper helper = null;

  public MouseBehaviorJ2D(DisplayRendererJ2D r) {
    helper = new MouseHelper(r, this);
    display_renderer = r;
    display = display_renderer.getDisplay();
  }

  public MouseHelper getMouseHelper() {
    return helper;
  }

  public VisADRay findRay(int screen_x, int screen_y) {
    // System.out.println("findRay " + screen_x + " " + screen_y);
    VisADCanvasJ2D canvas = display_renderer.getCanvas();
    AffineTransform trans = canvas.getTransform();

    if (trans == null) return null;

    double[] coords = {(float) screen_x, (float) screen_y};
    double[] newcoords = new double[2];
    try {
      trans.inverseTransform(coords, 0, newcoords, 0, 1);
    }
    catch (NoninvertibleTransformException e) {
      throw new VisADError("MouseBehaviorJ2D.findRay: " +
                           "non-invertable transform");
    }

    VisADRay ray = new VisADRay();
    ray.position[0] = newcoords[0];
    ray.position[1] = newcoords[1];
    ray.position[2] = 0.0;
    ray.vector[0] = 0.0;
    ray.vector[1] = 0.0;
    ray.vector[2] = -1.0;
    return ray;
  }

  public VisADRay cursorRay(double[] cursor) {
    VisADRay ray = new VisADRay();
    ray.position[0] = cursor[0];
    ray.position[1] = cursor[1];
    ray.position[2] = 0.0;
    ray.vector[0] = 0.0;
    ray.vector[1] = 0.0;
    ray.vector[2] = -1.0;
    return ray;
  }

  public double[] multiply_matrix(double[] a, double[] b) {
    AffineTransform ta = new AffineTransform(a);
    AffineTransform tb = new AffineTransform(b);
    ta.concatenate(tb);
    double[] c = new double[6];
    ta.getMatrix(c);
    return c;
  }

  /*** make_matrix ******************************************************
     Make a transformation matrix to perform the given rotation, scale and
     translation.  This function uses the fast matrix post-concatenation
     techniques from Graphics Gems.
  **********************************************************************/
  public double[] make_matrix(double rotx, double roty, double rotz,
         double scale, double transx, double transy, double transz) {
    double sx, sy, sz, cx, cy, cz, t;
    int i, j, k;
    double deg2rad = 1.0 / 57.2957;
    double[] matrix = new double[6];
    double[][] mat = new double[4][4];
  
    /* Get sin and cosine values */
    sx = Math.sin(rotx * deg2rad);
    cx = Math.cos(rotx * deg2rad);
    sy = Math.sin(roty * deg2rad);
    cy = Math.cos(roty * deg2rad);
    sz = Math.sin(rotz * deg2rad);
    cz = Math.cos(rotz * deg2rad);
  
    /* Start with identity matrix */
    mat[0][0] = 1.0;  mat[0][1] = 0.0;  mat[0][2] = 0.0;  mat[0][3] = 0.0;
    mat[1][0] = 0.0;  mat[1][1] = 1.0;  mat[1][2] = 0.0;  mat[1][3] = 0.0;
    mat[2][0] = 0.0;  mat[2][1] = 0.0;  mat[2][2] = 1.0;  mat[2][3] = 0.0;
    mat[3][0] = 0.0;  mat[3][1] = 0.0;  mat[3][2] = 0.0;  mat[3][3] = 1.0;
  
    /* Z Rotation */
    for (i=0;i<4;i++) {
      t = mat[i][0];
      mat[i][0] = t*cz - mat[i][1]*sz;
      mat[i][1] = t*sz + mat[i][1]*cz;
    }
 
    /* X rotation */
    for (i=0;i<4;i++) {
      t = mat[i][1];
      mat[i][1] = t*cx - mat[i][2]*sx;
      mat[i][2] = t*sx + mat[i][2]*cx;
    }
 
    /* Y Rotation */
    for (i=0;i<4;i++) {
      t = mat[i][0];
      mat[i][0] = mat[i][2]*sy + t*cy;
      mat[i][2] = mat[i][2]*cy - t*sy;
    }
 
    /* Scale */
    for (i=0;i<3;i++) {
      mat[i][0] *= scale;
      mat[i][1] *= scale;
      mat[i][2] *= scale;
    }
 
    /* Translation */
    mat[0][3] = transx;
    mat[1][3] = transy;
    mat[2][3] = transz;

    matrix[0] = mat[0][0];
    matrix[1] = mat[1][0];
    matrix[2] = mat[0][1];
    matrix[3] = mat[1][1];
    matrix[4] = mat[0][3];
    matrix[5] = mat[1][3];
    return matrix;
  }

}

