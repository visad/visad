
//
// MouseHelper.java
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
 
import java.awt.event.*;

import java.rmi.*;
import java.awt.*;
import java.util.*;

/**
   MouseHelper is the VisAD helper class for MouseBehaviorJ3D
   and MouseBehaviorJ2D.
*/
public class MouseHelper {

  MouseBehavior behavior;

  /** DisplayRenderer for Display */
  DisplayRenderer display_renderer;
  DisplayImpl display;
  /** ProjectionControl for Display */
  private ProjectionControl proj;

  DataRenderer direct_renderer = null;

  /** matrix from ProjectionControl when mousePressed1 (left) */
  private double[] tstart;

  /** screen location when mousePressed1 or mousePressed3 */
  private int start_x, start_y;

  /** mouse in window */
  private boolean mouseEntered;
  /** left, middle or right mouse button pressed in window */
  private boolean mousePressed1, mousePressed2, mousePressed3;
  /** combinations of Mouse Buttons and keys pressed;
      z -- SHIFT, t -- CONTROL */
  private boolean z1Pressed, t1Pressed, z2Pressed, t2Pressed;

  /** flag for 2-D mode */
  private boolean mode2D;

  public MouseHelper(DisplayRenderer r, MouseBehavior b) {
/*
    System.out.println("MouseHelper constructed ");
*/
    behavior = b;
    display_renderer = r;
    display = display_renderer.getDisplay();
    proj = display.getProjectionControl();
    mode2D = display_renderer.getMode2D();

    // initialize flags
    mouseEntered = false;
    mousePressed1 = false;
    mousePressed2 = false;
    mousePressed3 = false;
    z1Pressed = false;
    t1Pressed = false;
    z2Pressed = false;
    t2Pressed = false;

  }

  public void processEvents(AWTEvent[] events) {
    for (int i=0; i<events.length; i++) {
      if (!(events[i] instanceof MouseEvent)) {
        System.out.println("MouseHelper.processStimulus: non-" +
                           "MouseEvent");
      }
      switch (events[i].getID()) {
        case MouseEvent.MOUSE_ENTERED:
          mouseEntered = true;
          break;
        case MouseEvent.MOUSE_EXITED:
          mouseEntered = false;
          break;
        case MouseEvent.MOUSE_PRESSED:
          if (mouseEntered) {
            int m = ((InputEvent) events[i]).getModifiers();
            int m1 = m & InputEvent.BUTTON1_MASK;
            int m2 = m & InputEvent.BUTTON2_MASK;
            int m3 = m & InputEvent.BUTTON3_MASK;
            // special hack for BUTTON1 error in getModifiers
            if (m2 == 0 && m3 == 0 &&
                !mousePressed2 && !mousePressed3) {
              mousePressed1 = true;

              start_x = ((MouseEvent) events[i]).getX();
              start_y = ((MouseEvent) events[i]).getY();
              tstart = proj.getMatrix();

              if ((m & InputEvent.SHIFT_MASK) != 0) {
                z1Pressed = true;
              }
              else if ((m & InputEvent.CTRL_MASK) != 0) {
                t1Pressed = true;
              }
            }
            else if (m2 != 0 && !mousePressed1 && !mousePressed3) {
              mousePressed2 = true;
              // turn cursor on whenever mouse button2 pressed
              display_renderer.setCursorOn(true);

              start_x = ((MouseEvent) events[i]).getX();
              start_y = ((MouseEvent) events[i]).getY();
              tstart = proj.getMatrix();

              if ((m & InputEvent.SHIFT_MASK) != 0) {
                z2Pressed = true;
                if (!mode2D) {
                  // don't do cursor Z in 2-D mode
                  // current_y -> 3-D cursor Z
                  VisADRay cursor_ray =
                    behavior.cursorRay(display_renderer.getCursor());
                  display_renderer.depth_cursor(cursor_ray);
                }
              }
              else if ((m & InputEvent.CTRL_MASK) != 0) {
                t2Pressed = true;
              }
              else {
                VisADRay cursor_ray = behavior.findRay(start_x, start_y);
                display_renderer.drag_cursor(cursor_ray, true);
              }
            }
            else if (m3 != 0 && !mousePressed1 && !mousePressed2) {
              mousePressed3 = true;
              if (display_renderer.anyDirects()) {
                int current_x = ((MouseEvent) events[i]).getX();
                int current_y = ((MouseEvent) events[i]).getY();
                VisADRay direct_ray =
                  behavior.findRay(current_x, current_y);
                direct_renderer =
                  display_renderer.findDirect(direct_ray);
                if (direct_renderer != null) {
                  display_renderer.setDirectOn(true);
                  direct_renderer.drag_direct(direct_ray, true);
                }
              }
            }
          }
          break;
        case MouseEvent.MOUSE_RELEASED:
          int m = ((InputEvent) events[i]).getModifiers();
          int m1 = m & InputEvent.BUTTON1_MASK;
          int m2 = m & InputEvent.BUTTON2_MASK;
          int m3 = m & InputEvent.BUTTON3_MASK;
          // special hack for BUTTON1 error in getModifiers
          if (m2 == 0 && m3 == 0 && mousePressed1) {
            mousePressed1 = false;
            z1Pressed = false;
            t1Pressed = false;
          }
          else if (m2 != 0 && mousePressed2) {
            mousePressed2 = false;
            display_renderer.setCursorOn(false);
            z2Pressed = false;
            t2Pressed = false;
          }
          else if (m3 != 0 && mousePressed3) {
            mousePressed3 = false;
            display_renderer.setDirectOn(false);
            direct_renderer = null;
          }
          break;
        case MouseEvent.MOUSE_DRAGGED:
          if (mousePressed1 || mousePressed2 || mousePressed3) {
            Dimension d = ((MouseEvent) events[i]).getComponent().getSize();
            int current_x = ((MouseEvent) events[i]).getX();
            int current_y = ((MouseEvent) events[i]).getY();
            if (mousePressed1) {
              //
              // TO_DO
              // modify to use rotX, rotY, rotZ, setTranslation & setScale
              //
              double[] t1 = null;
              if (z1Pressed) {
                // current_y -> scale
                double scale =
                  Math.exp((start_y-current_y) / (double) d.height);
                t1 = make_matrix(0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0);
              }
              else if (t1Pressed) {
                // current_x, current_y -> translate
                double transx =
                  (start_x - current_x) * -2.0 / (double) d.width;
                double transy =
                  (start_y - current_y) * 2.0 / (double) d.height;
                t1 = make_matrix(0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
              }
              else {
                if (!mode2D) {
                  // don't do 3-D rotation in 2-D mode
                  double angley =
                    - (current_x - start_x) * 100.0 / (double) d.width;
                  double anglex =
                    - (current_y - start_y) * 100.0 / (double) d.height;
                  t1 = make_matrix(anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                }
              }
              if (t1 != null) {
                t1 = behavior.multiply_matrix(t1, tstart);
/*
                t1.mul(tstart);
                double[] matrix = new double[16];
                t1.get(matrix);
*/
                try {
                  proj.setMatrix(t1);
                }
                catch (VisADException e) {
                }
                catch (RemoteException e) {
                }
              }
            }
            else if (mousePressed2) {
              if (z2Pressed) {
                if (!mode2D) {
                  // don't do cursor Z in 2-D mode
                  // current_y -> 3-D cursor Z
                  float diff =
                    (start_y - current_y) * 4.0f / (float) d.height;
                  display_renderer.drag_depth(diff);
                }
              }
              else if (t2Pressed) {
                if (!mode2D) {
                  // don't do 3-D rotation in 2-D mode
                  double angley =
                    - (current_x - start_x) * 100.0 / (double) d.width;
                  double anglex =
                    - (current_y - start_y) * 100.0 / (double) d.height;
                  double[] t1 =
                    make_matrix(anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                  t1 = behavior.multiply_matrix(t1, tstart);
/*
                  t1.mul(tstart);
                  double[] matrix = new double[16];
                  t1.get(matrix);
*/
                  try {
                    proj.setMatrix(t1);
                  }
                  catch (VisADException e) {
                  }
                  catch (RemoteException e) {
                  }
                }
              }
              else {
                // current_x, current_y -> 3-D cursor X and Y
                VisADRay cursor_ray = behavior.findRay(current_x, current_y);
                display_renderer.drag_cursor(cursor_ray, false);
              }
            }
            else if (mousePressed3) {
              if (direct_renderer != null) {
                VisADRay direct_ray = behavior.findRay(current_x, current_y);
                direct_renderer.drag_direct(direct_ray, false);
              }
            }
          }
          break;
        default:
          System.out.println("MouseHelper.processStimulus: event type" +
                             "not recognized " + events[i].getID());
          break;
      }
    }
  }

/*
  public static double[] multiply_matrix(double[] a, double[] b) {
    double[] c = new double[16];
    int m = 0;
    for (int i=0; i<4; i++) {
      for (int j=0; j<4; j++) {
        // m = 4 * i + j;
        c[m] = 0;
        for (int k=0; k<4; k++) {
          c[m] += a[4 * i + k] * b[4 * k + j];
        }
      }
    }
    return c;
  }
*/

  /*** make_matrix ******************************************************
     Make a transformation matrix to perform the given rotation, scale and
     translation.  This function uses the fast matrix post-concatenation
     techniques from Graphics Gems.
  **********************************************************************/
  public static double[] make_matrix(double rotx, double roty, double rotz,
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

}

