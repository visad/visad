
//
// ProjectionControlJ3D.java
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

package visad.java3d;
 
import visad.*;

import java.rmi.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.Vector;
import java.util.Enumeration;

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
  private double[] matrix;

  // Vector of Switch nodes for volume rendering
  transient Vector switches = new Vector();
  int which_child = 2; // initial view along Z axis (?)

  public ProjectionControlJ3D(DisplayImpl d) throws VisADException {
    super(d);
    // WLH 8 April 99
    Matrix = new Transform3D();
    matrix = new double[16];
    Matrix.get(matrix);
/* WLH 8 April 99
    Matrix = init();
    matrix = new double[16];
    Matrix.get(matrix);
    ((DisplayRendererJ3D) getDisplayRenderer()).setTransform3D(Matrix);
*/
  }
 
  public double[] getMatrix() {
    double[] c = new double[16];
    System.arraycopy(matrix, 0, c, 0, 16);
    return c;
  }

  public void setMatrix(double[] m)
         throws VisADException, RemoteException {
    System.arraycopy(m, 0, matrix, 0, 16);
    Matrix = new Transform3D(matrix);
    ((DisplayRendererJ3D) getDisplayRenderer()).setTransform3D(Matrix);
    if (!switches.isEmpty()) selectSwitches();
    changeControl(true);
  }

  public void setAspect(double[] aspect)
         throws VisADException, RemoteException {
    if (aspect == null || aspect.length != 3) {
      throw new DisplayException("aspect array must be length = 2");
    }
    Transform3D transform = new Transform3D();
    transform.setScale(new javax.vecmath.Vector3d(aspect[0], aspect[1], aspect[2]));
    double[] mult = new double[16];
    transform.get(mult);
    Transform3D mat = init();
    double[] m = new double[16];
    mat.get(m);
    setMatrix(getDisplay().multiply_matrix(mult, m));
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

  void addPair(Switch sw, DataRenderer re) {
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
    double dx = Math.abs(eye.x - origin.x);
    double dy = Math.abs(eye.y - origin.y);
    double dz = Math.abs(eye.z - origin.z);
    if (dz >= dy && dz >= dx) which_child = 2;
    else if (dy >= dx) which_child = 1;
    else which_child = 0;

    // axis did not change, so no need to change Switches
    if (old_which_child == which_child) return;

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

