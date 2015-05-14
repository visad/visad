//
// ProjectionControlJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java3d;

import visad.*;

import java.rmi.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.Vector;
import java.util.Enumeration;

import java.lang.reflect.*;

/**
   ProjectionControlJ3D is the VisAD class for controlling the Projection
   from 3-D to 2-D.  It manipulates a TransformGroup node in the
   scene graph.<P>
*/
/* WLH 17 June 98
public class ProjectionControlJ3D extends Control
       implements ProjectionControl {
*/
public class ProjectionControlJ3D extends ProjectionControl {

  private transient Transform3D Matrix;

  // Vector of Switch nodes for volume rendering
  transient Vector switches = new Vector();
  int which_child = 2; // initial view along Z axis (?)

  // DRM 6 Nov 2000
  /** View of the postive X face of the display cube */
  public static final int X_PLUS = 0;
  /** View of the negative X face of the display cube */
  public static final int X_MINUS = 1;
  /** View of the postive Y face of the display cube */
  public static final int Y_PLUS = 2;
  /** View of the negative Y face of the display cube */
  public static final int Y_MINUS = 3;
  /** View of the postive Z face of the display cube */
  public static final int Z_PLUS = 4;
  /** View of the negative Z face of the display cube */
  public static final int Z_MINUS = 5;

  /**
   * Construct a new ProjectionControl for the display.  The initial
   * projection is saved so it can be reset with resetProjection().
   * @see #resetProjection().
   * @param  d  display whose projection will be controlled by this
   * @throws VisADException
   */
  public ProjectionControlJ3D(DisplayImpl d) throws VisADException {
    super(d);
    // WLH 8 April 99
    // Matrix = new Transform3D();
    Matrix = init();
    matrix = new double[MATRIX3D_LENGTH];
    Matrix.get(matrix);
    saveProjection();   // DRM 6 Nov 2000
/* WLH 8 April 99
    Matrix = init();
    matrix = new double[MATRIX3D_LENGTH];
    Matrix.get(matrix);
    ((DisplayRendererJ3D) getDisplayRenderer()).setTransform3D(Matrix);
*/
  }

  /**
   * Set the projection matrix.
   * @param m new projection matrix
   * @throws VisADException  VisAD error
   * @throws RemoteException  remote error
   */
  public void setMatrix(double[] m)
         throws VisADException, RemoteException {
    super.setMatrix(m);
    Matrix = new Transform3D(matrix);

    VisADCanvasJ3D canvas =
      ((DisplayRendererJ3D) getDisplayRenderer()).getCanvas();
    if (canvas != null && canvas.getOffscreen()) {
      try {
        Method renderMethod =
          Canvas3D.class.getMethod("renderOffScreenBuffer",
                                   new Class[] {});
        renderMethod.invoke(canvas, new Object[] {});
        Method waitMethod =
          Canvas3D.class.getMethod("waitForOffScreenRendering",
                                   new Class[] {});
        waitMethod.invoke(canvas, new Object[] {});
      }
      catch (NoSuchMethodException e) {
        // System.out.println(e);
      }
      catch (IllegalAccessException e) {
        // System.out.println(e);
      }
      catch (InvocationTargetException e) {
        // System.out.println(e + "\n" +
        //    ((InvocationTargetException) e).getTargetException());
      }
      // canvas.renderOffScreenBuffer();
    }

    ((DisplayRendererJ3D) getDisplayRenderer()).setTransform3D(Matrix);
    if (!switches.isEmpty()) selectSwitches();
    // WLH 5 May 2000
    // changeControl(true);
    changeControl(false);
  }

  /**
   * Set the aspect for the axes.  Default upon initialization is 
   * 1.0, 1.0, 1.0.  Invokes saveProjection to set this as the new default.
   * @see #saveProjection()
   * @param aspect  ratios (dimension 3) for the X, Y, and Z axes
   * @throws VisADException  aspect is null or wrong dimension or other error
   * @throws RemoteException  remote error
   */
  public void setAspect(double[] aspect)
         throws VisADException, RemoteException {
    if (aspect == null || aspect.length != 3) {
      throw new DisplayException("aspect array must be length = 3");
    }
    Transform3D transform = new Transform3D();
    transform.setScale(new javax.vecmath.Vector3d(aspect[0], aspect[1], aspect[2]));
    double[] mult = new double[MATRIX3D_LENGTH];
    transform.get(mult);
    Transform3D mat = init();
    double[] m = new double[MATRIX3D_LENGTH];
    mat.get(m);
    setMatrix(getDisplay().multiply_matrix(mult, m));
    saveProjection();   // DRM 6 Nov 2000
  }

  private Transform3D init() {
    Transform3D mat = new Transform3D();
    // initialize scale
    double scale = 0.5;
    if (getDisplayRenderer().getMode2D()) scale = ProjectionControl.SCALE2D;
    Transform3D t1 = new Transform3D(
      MouseBehaviorJ3D.static_make_matrix(0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0) );
    mat.mul(t1);
    return mat;
  }

  public void addPair(Switch sw, DataRenderer re) {
    switches.addElement(new SwitchProjection(sw, re));
    sw.setWhichChild(which_child);
  }

  private void selectSwitches() {
    int old_which_child = which_child;
    // calculate which axis is most parallel to eye direction
    Transform3D tt = new Transform3D(Matrix);
    tt.invert();
    Point3d origin = new Point3d(0.0, 0.0, 0.0);
    Point3d eye = new Point3d(0.0, 0.0, 1.0);
    tt.transform(origin);
    tt.transform(eye);
    double dx = eye.x - origin.x;
    double dy = eye.y - origin.y;
    double dz = eye.z - origin.z;
    double ax = Math.abs(dx);
    double ay = Math.abs(dy);
    double az = Math.abs(dz);
    if (az >= ay && az >= ax) {
      which_child = (dz > 0) ? 2 : 5;
    }
    else if (ay >= ax) {
      which_child = (dy > 0) ? 1 : 4;
    }
    else {
      which_child = (dx > 0) ? 0 : 3;
    }

    // axis did not change, so no need to change Switches
    if (old_which_child == which_child) return;
/*
System.out.println("which_child = " + which_child + "  " + dx +
                  " " + dy + " " + dz);
*/
    // axis changed, so change Switches
    Enumeration pairs = ((Vector) switches.clone()).elements();
    while (pairs.hasMoreElements()) {
      SwitchProjection ss = (SwitchProjection) pairs.nextElement();
      ss.swit.setWhichChild(which_child);
    }
  }

  /** clear all 'pairs' in switches that involve re */
  public void clearSwitches(DataRenderer re) {
    Enumeration pairs = ((Vector) switches.clone()).elements();
    while (pairs.hasMoreElements()) {
      SwitchProjection ss = (SwitchProjection) pairs.nextElement();
      if (ss.renderer.equals(re)) {
        switches.removeElement(ss);
      }
    }
  }

  /**
   * Set the projection so the requested view is displayed.
   * @param  view  one of the static view fields (X_PLUS, X_MINUS, etc).  This
   *               will set the view so the selected face is orthogonal to
   *               the display.
   * @throws VisADException   VisAD failure.
   * @throws RemoteException  Java RMI failure.
   */
  public void setOrthoView(int view)
    throws VisADException, RemoteException 
  {
    double[] viewMatrix;
    if (getDisplayRenderer().getMode2D()) return;
    switch (view)
    {
      case Z_PLUS: // Top
        viewMatrix = 
          getDisplay().make_matrix(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case Z_MINUS: // Bottom
        viewMatrix = 
          getDisplay().make_matrix(0.0, 180.0, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case Y_PLUS: // North
        viewMatrix = 
          getDisplay().make_matrix(-90.0, 180.0, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case Y_MINUS: // South
        viewMatrix = 
          getDisplay().make_matrix(90.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case X_PLUS: // East
        viewMatrix = 
          getDisplay().make_matrix(0.0, 90.0, 90.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case X_MINUS: // West
        viewMatrix = 
          getDisplay().make_matrix(0.0, -90.0, -90.0, 1.0, 0.0, 0.0, 0.0);
        break;
      default:   // no change
        viewMatrix = 
          getDisplay().make_matrix(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
   }
   setMatrix(
     getDisplay().multiply_matrix(viewMatrix, getSavedProjectionMatrix()));
 }

  /** SwitchProjection is an inner class of ProjectionControlJ3D for
      (Switch, DataRenderer) structures */
  private class SwitchProjection extends Object {
    Switch swit;
    DataRenderer renderer;

    SwitchProjection(Switch sw, DataRenderer re) {
      swit = sw;
      renderer = re;
    }
  }
}
