
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

import java.awt.*;
import java.util.*;

/**
   MouseBehavior is the VisAD class for mouse behaviors for Java3D
*/

/*

                       MUST USE ProjectionControl

*/

/*
   On MOUSE_PRESSED event with left button,
   ((InputEvent) events[i]).getModifiers() returns 0 and
   should return 16 ( = InputEvent.BUTTON1_MASK ).
   ((InputEvent) events[i]).getModifiers() correctly
   returns 16 on MOUSE_RELEASED with left button.
*/
public class MouseBehavior extends Behavior {

  private boolean mouseEntered;
  private boolean mousePressed;
  private int start_x, start_y;
  private Transform3D tstart;
  private WakeupOr wakeup;
  private ProjectionControl proj;

  public MouseBehavior(ProjectionControl p) {
/*
    System.out.println("MouseBehavior constructed ");
*/
    proj = p;
    mouseEntered = false;
    mousePressed = false;
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
              InputEvent.BUTTON1_MASK + " " + mouseEntered);
*/
            // special hack for BUTTON1 error in getModifiers
            if (( ((InputEvent) events[i]).getModifiers() == 0 ||
                  (((InputEvent) events[i]).getModifiers() & InputEvent.BUTTON1_MASK)
                   != 0) && mouseEntered) {
              mousePressed = true;
              start_x = ((MouseEvent) events[i]).getX();
              start_y = ((MouseEvent) events[i]).getY();
              tstart = new Transform3D(proj.getMatrix());
            }
            break;
          case MouseEvent.MOUSE_RELEASED:
/*
            System.out.println("MouseBehavior.processStimulus(MOUSE_RELEASED)" +
              " " + ((InputEvent) events[i]).getModifiers() + " " +
              InputEvent.BUTTON1_MASK + " " + mousePressed);
*/
            if ((((InputEvent) events[i]).getModifiers() & InputEvent.BUTTON1_MASK)
                 != 0 && mousePressed) {
              mousePressed = false;
            }
            break;
          case MouseEvent.MOUSE_DRAGGED:
/*
            System.out.println("MouseBehavior.processStimulus(MOUSE_DRAGGED)" +
              mousePressed + " " + start_x + " " + start_y);
*/
            if (mousePressed) {
              int current_x, current_y;
              double anglex, angley;

              Dimension d = ((MouseEvent) events[i]).getComponent().getSize();
              current_x = ((MouseEvent) events[i]).getX();
              current_y = ((MouseEvent) events[i]).getY();
              angley = - (current_x - start_x) * 100.0 / (double) d.width;
              anglex = - (current_y - start_y) * 100.0 / (double) d.height;
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

}

