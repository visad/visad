//
// MouseBehaviorJ3D.java
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

package visad.java3d;
 
import visad.*;
 
import java.awt.event.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.rmi.*;
import java.awt.*;
import java.util.*;

/**
   MouseBehaviorJ3D is the VisAD class for mouse behaviors for Java3D
*/

/*
   On MOUSE_PRESSED event with left button,
   ((InputEvent) events[i]).getModifiers() returns 0 and
   should return 16 ( = InputEvent.BUTTON1_MASK ).
   ((InputEvent) events[i]).getModifiers() correctly
   returns 16 on MOUSE_RELEASED with left button.
*/
public class MouseBehaviorJ3D extends Behavior
       implements MouseBehavior {

  /** wakeup condition for MouseBehaviorJ3D */
  private WakeupOr wakeup;
  /** DisplayRenderer for Display */
  DisplayRendererJ3D display_renderer;
  DisplayImpl display;

  MouseHelper helper = null;

  public MouseBehaviorJ3D() {
  }

  public MouseBehaviorJ3D(DisplayRendererJ3D r) {
    helper = new MouseHelper(r, this);
    display_renderer = r;
    display = display_renderer.getDisplay();

    WakeupCriterion[] conditions = new WakeupCriterion[5];
    conditions[0] = new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
    conditions[1] = new WakeupOnAWTEvent(MouseEvent.MOUSE_ENTERED);
    conditions[2] = new WakeupOnAWTEvent(MouseEvent.MOUSE_EXITED);
    conditions[3] = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
    conditions[4] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
    wakeup = new WakeupOr(conditions);
  }

  public MouseHelper getMouseHelper() {
    return helper;
  }

  public void initialize() {
    setWakeup();
  }

  public void processStimulus(Enumeration criteria) {
    while (criteria.hasMoreElements()) {
      WakeupCriterion wakeup = (WakeupCriterion) criteria.nextElement();
      if (!(wakeup instanceof WakeupOnAWTEvent)) {
        System.out.println("MouseBehaviorJ3D.processStimulus: non-" +
                            "WakeupOnAWTEvent");
      }
      else {
        AWTEvent[] events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
        for (int i=0; i<events.length; i++) {
          helper.processEvent(events[i]);
        }
      }
    }
    setWakeup();
  }

  public VisADRay findRay(int screen_x, int screen_y) {
    // System.out.println("findRay " + screen_x + " " + screen_y);
    View view = display_renderer.getView();
    Canvas3D canvas = display_renderer.getCanvas();
    Point3d position = new Point3d();
    canvas.getPixelLocationInImagePlate(screen_x, screen_y, position);
    Point3d eye_position = new Point3d();
    canvas.getCenterEyeInImagePlate(eye_position);
    Transform3D t = new Transform3D();
    canvas.getImagePlateToVworld(t);
    t.transform(position);
    t.transform(eye_position);
 
    if (display.getGraphicsModeControl().getProjectionPolicy() ==
        View.PARALLEL_PROJECTION) {
      eye_position = new Point3d(position.x, position.y,
                                 position.z + 1.0f);
    }
 
    TransformGroup trans = display_renderer.getTrans();
    Transform3D tt = new Transform3D();
    trans.getTransform(tt);
    tt.invert();
    tt.transform(position);
    tt.transform(eye_position);
 
    // new eye_position = 2 * position - old eye_position
    Vector3d vector = new Vector3d(position.x - eye_position.x,
                                   position.y - eye_position.y,
                                   position.z - eye_position.z);
    vector.normalize();
    VisADRay ray = new VisADRay();
    ray.position[0] = position.x;
    ray.position[1] = position.y;
    ray.position[2] = position.z;
    ray.vector[0] = vector.x;
    ray.vector[1] = vector.y;
    ray.vector[2] = vector.z;
    // PickRay ray = new PickRay(position, vector);
    return ray;
  }

  public VisADRay cursorRay(double[] cursor) {
    View view = display_renderer.getView();
    Canvas3D canvas = display_renderer.getCanvas();
    // note position already in Vworld coordinates
    Point3d position = new Point3d(cursor[0], cursor[1], cursor[2]);
    Point3d eye_position = new Point3d();
    canvas.getCenterEyeInImagePlate(eye_position);
    Transform3D t = new Transform3D();
    canvas.getImagePlateToVworld(t);
    t.transform(eye_position);
 
    TransformGroup trans = display_renderer.getTrans();
    Transform3D tt = new Transform3D();
    trans.getTransform(tt);
    tt.transform(position);

    if (display.getGraphicsModeControl().getProjectionPolicy() ==
        View.PARALLEL_PROJECTION) {
      eye_position = new Point3d(position.x, position.y,
                                 position.z + 1.0f);
    }

    tt.invert();
    tt.transform(position);
    tt.transform(eye_position);
 
    // new eye_position = 2 * position - old eye_position
    Vector3d vector = new Vector3d(position.x - eye_position.x,
                                   position.y - eye_position.y,
                                   position.z - eye_position.z);
    vector.normalize();
    VisADRay ray = new VisADRay();
    ray.position[0] = eye_position.x;
    ray.position[1] = eye_position.y;
    ray.position[2] = eye_position.z;
    ray.vector[0] = vector.x;
    ray.vector[1] = vector.y;
    ray.vector[2] = vector.z;
    // PickRay ray = new PickRay(eye_position, vector);
    return ray;
  }

  void setWakeup() {
    wakeupOn(wakeup);
  }

  public double[] make_translate(double transx, double transy) {
    return make_matrix(0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
  }

  public double[] multiply_matrix(double[] a, double[] b) {
    return static_multiply_matrix(a, b);
  }

  public static double[] static_multiply_matrix(double[] a, double[] b) {
    Transform3D ta = new Transform3D(a);
    Transform3D tb = new Transform3D(b);
    ta.mul(tb);
    double[] c = new double[16];
    ta.get(c);
    return c;
  }

  /*** make_matrix ******************************************************
     Make a transformation matrix to perform the given rotation, scale and
     translation.  This function uses the fast matrix post-concatenation
     techniques from Graphics Gems.
  **********************************************************************/
  public double[] make_matrix(double rotx, double roty, double rotz,
         double scale, double transx, double transy, double transz) {
    return static_make_matrix(rotx, roty, rotz, scale, transx, transy, transz);
  }

  public static double[] static_make_matrix(double rotx, double roty, double rotz,
         double scale, double transx, double transy, double transz) {
    double sx, sy, sz, cx, cy, cz, t;
    int i, j, k;
    double deg2rad = 1.0 / 57.2957;
    double[] matrix = new double[16];
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
/* should be mat[0][3], mat[1][3], mat[2][3]
   WLH 22 Dec 97 */
    mat[0][3] = transx;
    mat[1][3] = transy;
    mat[2][3] = transz;
/*
    mat[3][0] = transx;
    mat[3][1] = transy;
    mat[3][2] = transz;
*/
    k = 0;
    for (i=0; i<4; i++) {
      for (j=0; j<4; j++) {
        matrix[k] = mat[i][j];
        k++;
      }
    }
    return matrix;
  }

  static final double EPS = 0.000001;

  /* doesn't work for non-uniform scale */
  public static void unmake_matrix(double[] rot, double[] scale,
                                   double[] trans, double[] matrix) {
    double  sx, sy, sz, cx, cy, cz;
    int i, j;
    double[][] mat = new double[4][4];
    double[][] nat = new double[4][4];
  
    double scalex, scaley, scalez, scaleinv, cxa, cxb, cxinv;

    if (rot == null || rot.length != 3) return;
    if (scale == null || scale.length != 1) return;
    if (trans == null || trans.length != 3) return;
    if (matrix == null || matrix.length != 16) return;
  
    int k = 0;
    for (i=0; i<4; i++) {
      for (j=0; j<4; j++) {
        mat[i][j] = matrix[k];
        k++;
      }
    }

    /* translation */
    trans[0] = mat[3][0];
    trans[1] = mat[3][1];
    trans[2] = mat[3][2];
  
    /* scale */
    scalex = scaley = scalez = 0.0;
    for (i=0; i<3; i++) {
      scalex += mat[0][i] * mat[0][i];
      scaley += mat[1][i] * mat[1][i];
      scalez += mat[2][i] * mat[2][i];
    }
    if (Math.abs(scalex - scaley) > EPS || Math.abs(scalex - scalez) > EPS) {
      System.out.println("problem " + scalex + " " + scaley + " " + scalez);
    }
    scale[0] = Math.sqrt((scalex + scaley + scalez)/3.0);
    scaleinv = Math.abs(scale[0]) > EPS ? 1.0 / scale[0] : 1.0 / EPS;
  
    for (i=0; i<3; i++) {
      for (j=0; j<3; j++) {
        nat[j][i] = scaleinv * mat[j][i];
      }
    }
  
    /* rotation */
    sx = -nat[2][1];
  
    cxa = Math.sqrt(nat[2][0]*nat[2][0] + nat[2][2]*nat[2][2]);
    cxb = Math.sqrt(nat[0][1]*nat[0][1] + nat[1][1]*nat[1][1]);
  
    if (Math.abs(cxa - cxb) > EPS) {
      // System.out.println("problem2 " + cxa + " " + cxb);
    }
    /* the sign of cx does not matter;
       it is an ambiguity in 3-D rotations:
       (rotz, rotx, roty) = (180+rotz, 180-rotx, 180+roty) */
    cx = (cxa + cxb) / 2.0;
    if (Math.abs(cx) > EPS) {
      cxinv = 1.0 / cx;
      sy = nat[2][0] * cxinv;
      cy = nat[2][2] * cxinv;
      sz = nat[0][1] * cxinv;
      cz = nat[1][1] * cxinv;
    }
    else {
      /* if cx == 0 then roty and rotz are ambiguous:
         assume rotx = 0.0 */
      sy = 0.0;
      cy = 1.0;
      sz = nat[0][2];
      cz = nat[1][2];
    }
  
    rot[0] = 57.2957 * Math.atan2(sx, cx);
    rot[1] = 57.2957 * Math.atan2(sy, cy);
    rot[2] = 57.2957 * Math.atan2(sz, cz);
    return;
  }


/*
   homogeneous coordinates:
   point (x, y, z) is represented as (w*x, w*y, w*z, w) for w != 0
   point (x, y, z, 1) transforms to
         x' = x*mat[0]  + y*mat[1]  + z*mat[2]  + mat[3]
         y' = x*mat[4]  + y*mat[5]  + z*mat[6]  + mat[7]
         z' = x*mat[8]  + y*mat[9]  + z*mat[10] + mat[11]
         w' = x*mat[12] + y*mat[13] + z*mat[14] + mat[15]
*/

/*
   see ViewPlatform.TransformGroup in UniverseBuilderJ3D constructor
 
   View.setProjectionPolicy
     default is View.PERSPECTIVE_PROJECTION (vs PARALLEL_PROJECTION)
   View.getVpcToEc(Transform3D vpcToEc)
     "Compatibility mode method that retrieves the current ViewPlatform
     Coordinates (VPC) system to Eye Coordinates (EC) transform and copies 
     it into the specified object."
   View.getLeftProjection(Transform3D projection)
   View.getRightProjection(Transform3D projection)
     default is View.CYCLOPEAN_EYE_VIEW
     require:
   View.setCompatibilityModeEnable(true)
     default is disabled
   View.getLeftProjection(Transform3D projection)
     "Compatibility mode method that specifies a viewing frustum for the
     left eye that transforms points in Eye Coordinates (EC) to Clipping 
     Coordinates (CC)."
 
   NOTE have View in UniverseBuilderJ3D

   Canvas3D.getImagePlateToVworld(Transform3D t)
   Canvas3D.getPixelLocationInImagePlate(int x, int y, Point3d position)
   Canvas3D.getVworldToImagePlate(Transform3D t)
   Canvas3D.getCenterEyeInImagePlate(Point3d position)
 
  spec page 176: "final transform for rendering shapes to Canvas3D is
    P * E * inv(ViewPlatform.TransformGroup) * trans
    use Transform3D.invert for inv
  where P = projection matrix from eye coords to clipping coords
    i.e., projection
  and E = matrix from ViewPlatform to eye coords
    i.e., vpcToEc
*/

/*
   is the mouse a Sensor or InputDevice?
   if so, see spec page 369 for sensor to World transform
*/

/*
import javax.vecmath.*;
import javax.media.j3d.*;

public class TestTransform3D extends Object {

  public static void main(String args[]) {
    Transform3D trans_frustum = new Transform3D();
    Transform3D trans_perspective = new Transform3D();
    Transform3D trans_ortho = new Transform3D();
    Transform3D trans_ortho2 = new Transform3D();

    System.out.println("initial trans_frustum =\n" + trans_frustum);

    trans_frustum.frustum(-1.0, 1.0, -1.0, 1.0, 1.0, 2.0);
    System.out.println("trans_frustum =\n" + trans_frustum);

    trans_perspective.perspective(0.5, 1.0, 1.0, 2.0);
    System.out.println("trans_perspective =\n" + trans_perspective);

    trans_ortho.ortho(-1.0, 1.0, -1.0, 1.0, 1.0, 2.0);
    System.out.println("trans_ortho =\n" + trans_ortho);

    trans_ortho2.ortho(-1.0, 1.0, -1.0, 1.0, 1.0, 3.0);
    System.out.println("trans_ortho2 =\n" + trans_ortho2);

    Transform3D trans_translate_scale =
      new Transform3D(new Matrix3d(1.0, 1.1, 1.2,
                                   1.3, 1.4, 1.5,
                                   1.6, 1.7, 1.8),
                      new Vector3d(2.1, 2.2, 2.3), 3.0);
    System.out.println("trans_translate_scale =\n" + trans_translate_scale);

    double[] matrix = {1.0, 1.1, 1.2, 1.3,
                       1.4, 1.5, 1.6, 1.7,
                       1.8, 1.9, 2.0, 2.1,
                       2.2, 2.3, 2.4, 2.5};
    Transform3D trans_matrix = new Transform3D(matrix);
    System.out.println("trans_matrix =\n" + trans_matrix);
  }

// here's the output:
verner 25% java TestTransform3D
initial trans_frustum =
1.0, 0.0, 0.0, 0.0
0.0, 1.0, 0.0, 0.0
0.0, 0.0, 1.0, 0.0
0.0, 0.0, 0.0, 1.0
 
trans_frustum =
1.0, 0.0, 0.0, 0.0
0.0, 1.0, 0.0, 0.0
0.0, 0.0, 3.0, 4.0
0.0, 0.0, -1.0, 0.0
 
trans_perspective =
3.91631736464594, 0.0, 0.0, 0.0
0.0, 3.91631736464594, 0.0, 0.0
0.0, 0.0, 3.0, 4.0
0.0, 0.0, -1.0, 0.0
 
trans_ortho =
1.0, 0.0, 0.0, -0.0
0.0, 1.0, 0.0, -0.0
0.0, 0.0, 2.0, 3.0
0.0, 0.0, 0.0, 1.0
 
trans_ortho2 =
1.0, 0.0, 0.0, -0.0
0.0, 1.0, 0.0, -0.0
0.0, 0.0, 1.0, 2.0
0.0, 0.0, 0.0, 1.0

trans_translate_scale =
3.0, 3.3000000000000003, 3.5999999999999996, 2.1
3.9000000000000004, 4.199999999999999, 4.5, 2.2
4.800000000000001, 5.1, 5.4, 2.3
0.0, 0.0, 0.0, 1.0
 
trans_matrix =
1.0, 1.1, 1.2, 1.3
1.4, 1.5, 1.6, 1.7
1.8, 1.9, 2.0, 2.1
2.2, 2.3, 2.4, 2.5

verner 26% 
//

}

*/

}

