
//
// MouseBehavior.java
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
 
import java.awt.event.*;
import javax.media.j3d.*;
import java.vecmath.*;

import java.awt.*;
import java.util.*;

/**
   MouseBehavior is the VisAD class for mouse behaviors for Java3D
*/

/*
   On MOUSE_PRESSED event with left button,
   ((InputEvent) events[i]).getModifiers() returns 0 and
   should return 16 ( = InputEvent.BUTTON1_MASK ).
   ((InputEvent) events[i]).getModifiers() correctly
   returns 16 on MOUSE_RELEASED with left button.
*/
public class MouseBehavior extends Behavior {

  /** wakeup condition for MouseBehavior */
  private WakeupOr wakeup;
  /** DisplayRenderer for Display */
  DisplayRenderer display_renderer;
  /** ProjectionControl for Display */
  private ProjectionControl proj;
  /** root BranchGroup for direct manipulation Data depictions */
  private BranchGroup direct;

  /** Transform3D from ProjectionControl when mousePressed1 (left) */
  private Transform3D tstart;
  /** screen location when mousePressed1 or mousePressed3 */
  private int start_x, start_y;

  /** mouse in window */
  private boolean mouseEntered;
  /** left, middle or right mouse button pressed in window */
  private boolean mousePressed1, mousePressed2, mousePressed3;

  /** close direct_renderer when mousePressed3 */
  DirectManipulationRenderer direct_renderer = null;

  public MouseBehavior(DisplayRenderer r) {
/*
    System.out.println("MouseBehavior constructed ");
*/
    display_renderer = r;
    proj = display_renderer.getDisplay().getProjectionControl();
    direct = display_renderer.getDirect();
    mouseEntered = false;
    mousePressed1 = false;
    mousePressed2 = false;
    mousePressed3 = false;
    WakeupCriterion[] conditions = new WakeupCriterion[5];
    conditions[0] = new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
    conditions[1] = new WakeupOnAWTEvent(MouseEvent.MOUSE_ENTERED);
    conditions[2] = new WakeupOnAWTEvent(MouseEvent.MOUSE_EXITED);
    conditions[3] = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
    conditions[4] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
/* not needed ??
    conditions[5] = new WakeupOnAWTEvent(MouseEvent.MOUSE_MOVED);
*/
    wakeup = new WakeupOr(conditions);
  }

  public void initialize() {
/*
    System.out.println("MouseBehavior.initialize");
*/
    setWakeup();
  }

  public void processStimulus(Enumeration criteria) {
/*
    System.out.println("MouseBehavior.processStimulus");
*/
    while (criteria.hasMoreElements()) {
      WakeupCriterion wakeup = (WakeupCriterion) criteria.nextElement();
      if (!(wakeup instanceof WakeupOnAWTEvent)) {
        System.out.println("MouseBehavior.processStimulus: non-" +
                            "WakeupOnAWTEvent");
      }
      AWTEvent[] events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
/*
      System.out.println("MouseBehavior.processStimulus event.length = " +
                         events.length);
*/
      for (int i=0; i<events.length; i++) {
        if (!(events[i] instanceof MouseEvent)) {
          System.out.println("MouseBehavior.processStimulus: non-" +
                             "MouseEvent");
        }
        switch (events[i].getID()) {
          case MouseEvent.MOUSE_ENTERED:
/*
            System.out.println("MouseBehavior.processStimulus(MOUSE_ENTERED)");
*/
            mouseEntered = true;
            break;
          case MouseEvent.MOUSE_EXITED:
/*
            System.out.println("MouseBehavior.processStimulus(MOUSE_EXITED)");
*/
            mouseEntered = false;
            break;
          case MouseEvent.MOUSE_PRESSED:
/*
            System.out.println("MouseBehavior.processStimulus(MOUSE_PRESSED)" +
              " " + ((InputEvent) events[i]).getModifiers() + " " +
              InputEvent.BUTTON3_MASK + " " + mouseEntered);
*/
            if (mouseEntered) {
              // special hack for BUTTON1 error in getModifiers
              if ( ((InputEvent) events[i]).getModifiers() == 0 ||
                    (((InputEvent) events[i]).getModifiers() &
                     InputEvent.BUTTON1_MASK) != 0) {
                mousePressed1 = true;
                start_x = ((MouseEvent) events[i]).getX();
                start_y = ((MouseEvent) events[i]).getY();
                tstart = new Transform3D(proj.getMatrix());
              }
              else if ( (((InputEvent) events[i]).getModifiers() &
                         InputEvent.BUTTON2_MASK) != 0) {
                mousePressed2 = true;
              }
              else if ( (((InputEvent) events[i]).getModifiers() &
                         InputEvent.BUTTON3_MASK) != 0) {
                mousePressed3 = true;
/*
System.out.println("MouseBehavior.processStimulus: mousePressed3 " +
                   display_renderer.anyDirects());
*/
                if (display_renderer.anyDirects()) {
                  int current_x = ((MouseEvent) events[i]).getX();
                  int current_y = ((MouseEvent) events[i]).getY();
                  PickRay direct_ray =
                    findRay(current_x, current_y, direct);
                  direct_renderer =
                    display_renderer.findDirect(direct_ray);
                  if (direct_renderer != null) {
                    direct_renderer.drag_direct(direct_ray, true);
                  }
                }
              }
            }
            break;
          case MouseEvent.MOUSE_RELEASED:
/*
            System.out.println("MouseBehavior.processStimulus(MOUSE_RELEASED)" +
              " " + ((InputEvent) events[i]).getModifiers() + " " +
              InputEvent.BUTTON1_MASK + " " + mousePressed1);
*/
            if ( (((InputEvent) events[i]).getModifiers() &
                  InputEvent.BUTTON1_MASK) != 0 && mousePressed1) {
              mousePressed1 = false;
            }
            else if ( (((InputEvent) events[i]).getModifiers() &
                       InputEvent.BUTTON2_MASK) != 0 && mousePressed2) {
              mousePressed2 = false;
            }
            else if ( (((InputEvent) events[i]).getModifiers() &
                       InputEvent.BUTTON3_MASK) != 0 && mousePressed3) {
              mousePressed3 = false;
              direct_renderer = null;
            }
            break;
          case MouseEvent.MOUSE_DRAGGED:
/*
            System.out.println("MouseBehavior.processStimulus(MOUSE_DRAGGED)" +
              mousePressed1 + " " + start_x + " " + start_y);
*/
            if (mousePressed1 || mousePressed2 || mousePressed3) {
              Dimension d = ((MouseEvent) events[i]).getComponent().getSize();
              int current_x = ((MouseEvent) events[i]).getX();
              int current_y = ((MouseEvent) events[i]).getY();
              if (mousePressed1) {
                double angley = - (current_x - start_x) * 100.0 / (double) d.width;
                double anglex = - (current_y - start_y) * 100.0 / (double) d.height;
/*
                System.out.println("(MOUSE_DRAGGED) " + d.width + " " + d.height +
                                   " " + current_x + " " + current_y + " " +
                                   anglex + " " + angley);
*/

/* WLH - modify to use rotX, rotY, rotZ, setTranslation & setScale */
                Transform3D t1 = make_matrix(anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                t1.mul(tstart);
                double[] matrix = new double[16];
                t1.get(matrix);
                proj.setMatrix(matrix);
              }
              else if (mousePressed2) {
              }
              else if (mousePressed3) {
                if (direct_renderer != null) {
                  PickRay direct_ray = findRay(current_x, current_y, direct);
                  direct_renderer.drag_direct(direct_ray, false);
                }
              }
            }
            break;
          default:
            System.out.println("MouseBehavior.processStimulus: event type" +
                               "not recognized " + events[i].getID());
            break;
        }
      }
    }
    setWakeup();
  }

  PickRay findRay(int screen_x, int screen_y, BranchGroup direct) {
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

/*
  eureka - it works
    LineArray array = new LineArray(2, LineArray.COORDINATES);
    array.setCoordinate(0, eye_position);
    array.setCoordinate(1, new Point3d(eye_position.x + 2.0f * vector.x,
                                       eye_position.y + 2.0f * vector.y,
                                       eye_position.z + 2.0f * vector.z));
    Appearance appearance = new Appearance();
    LineAttributes line = new LineAttributes();
    line.setLineWidth(4.0f);
    appearance.setLineAttributes(line);
    Shape3D shape = new Shape3D(array, appearance);
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.addChild(shape);
    direct.addChild(branch);
*/
    vector.normalize();
    PickRay ray = new PickRay(eye_position, vector);
    return ray;
  }

  /*** make_matrix ******************************************************
     Make a transformation matrix to perform the given rotation, scale and
     translation.  This function uses the fast matrix post-concatenation
     techniques from Graphics Gems.
  **********************************************************************/
  Transform3D make_matrix(double rotx, double roty, double rotz, double scale,
                          double transx, double transy, double transz) {
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
/* should be mat[0][3], mat[1][3], mat[2][3] */
/* WLH 22 Dec 97
    mat[0][3] = transx;
    mat[1][3] = transy;
    mat[2][3] = transz;
*/
    mat[3][0] = transx;
    mat[3][1] = transy;
    mat[3][2] = transz;
    k = 0;
    for (i=0; i<4; i++) {
      for (j=0; j<4; j++) {
        matrix[k] = mat[i][j];
        k++;
      }
    }
    return new Transform3D(matrix);
  }

  void setWakeup() {
    wakeupOn(wakeup);
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
   see ViewPlatform.TransformGroup in UniverseBuilder constructor
 
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
 
   NOTE have View in UniverseBuilder

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
import java.vecmath.*;
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

