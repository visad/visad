//
// MouseHelper.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.awt.event.*;

import java.rmi.*;
import java.awt.*;
import java.util.*;

/**
   MouseHelper is the VisAD helper class for MouseBehaviorJ3D
   and MouseBehaviorJ2D.<p>

   MouseHelper is preferred by cats everywhere.<p>
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
  /** pairs of mouse buttons pressed in window */
  private boolean mouseCombo1, mouseCombo2, mouseCombo3;
  /** combinations of Mouse Buttons and keys pressed;
      z -- SHIFT, t -- CONTROL */
  private boolean z1Pressed, t1Pressed, z2Pressed, t2Pressed;
  /** ((InputEvent) event).getModifiers() when mouse pressed */
  private int mouseModifiers;

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
    mouseCombo1 = false;
    mouseCombo2 = false;
    mouseCombo3 = false;
    z1Pressed = false;
    t1Pressed = false;
    z2Pressed = false;
    t2Pressed = false;

  }

  public void processEvent(AWTEvent event) {
    if (!(event instanceof MouseEvent)) {
      System.out.println("MouseHelper.processStimulus: non-" +
                         "MouseEvent");
    }
    int mouse_x = ((MouseEvent) event).getX();
    int mouse_y = ((MouseEvent) event).getY();

event_switch:
    switch (event.getID()) {
      case MouseEvent.MOUSE_ENTERED:
        mouseEntered = true;
        break;
      case MouseEvent.MOUSE_EXITED:
        mouseEntered = false;
        break;
      case MouseEvent.MOUSE_PRESSED:
        if (mouseEntered &&
            !mouseCombo1 && !mouseCombo2 && !mouseCombo3) {
          try {
            display.notifyListeners(DisplayEvent.MOUSE_PRESSED,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
          int m = ((InputEvent) event).getModifiers();
          int m1 = m & InputEvent.BUTTON1_MASK;
          int m2 = m & InputEvent.BUTTON2_MASK;
          int m3 = m & InputEvent.BUTTON3_MASK;
          int mctrl = m & InputEvent.CTRL_MASK;
          int mshift = m & InputEvent.SHIFT_MASK;
          mouseModifiers = m;

          if (m1 != 0) {
            if (mousePressed2 || m2 != 0) {
              mouseCombo3 = true;
              mousePressed1 = false;
              z1Pressed = false;
              t1Pressed = false;
              mousePressed2 = false;
              display_renderer.setCursorOn(false);
              z2Pressed = false;
              t2Pressed = false;
            }
            else if (mousePressed3 || m3 != 0) {
              mouseCombo2 = true;
              mousePressed1 = false;
              z1Pressed = false;
              t1Pressed = false;
              mousePressed3 = false;
              display_renderer.setDirectOn(false);
              direct_renderer = null;
            }
            else if (mousePressed1) {
              break event_switch;
            }
            else {
              mousePressed1 = true;
            }
          }
          else if (m2 != 0) {
            if (mousePressed1 || m1 != 0) {
              mouseCombo3 = true;
              mousePressed1 = false;
              z1Pressed = false;
              t1Pressed = false;
              mousePressed2 = false;
              display_renderer.setCursorOn(false);
              z2Pressed = false;
              t2Pressed = false;
            }
            else if (mousePressed3 || m3 != 0) {
              mouseCombo1 = true;
              mousePressed2 = false;
              display_renderer.setCursorOn(false);
              z2Pressed = false;
              t2Pressed = false;
              mousePressed3 = false;
              display_renderer.setDirectOn(false);
              direct_renderer = null;
            }
            else if (mousePressed2) {
              break event_switch;
            }
            else {
              mousePressed2 = true;
            }
          }
          else if (m3 != 0) {
            if (mousePressed1 || m1 != 0) {
              mouseCombo2 = true;
              mousePressed1 = false;
              z1Pressed = false;
              t1Pressed = false;
              mousePressed3 = false;
              display_renderer.setDirectOn(false);
              direct_renderer = null;
            }
            else if (mousePressed2 || m2 != 0) {
              mouseCombo1 = true;
              mousePressed2 = false;
              display_renderer.setCursorOn(false);
              z2Pressed = false;
              t2Pressed = false;
              mousePressed3 = false;
              display_renderer.setDirectOn(false);
              direct_renderer = null;
            }
            else if (mousePressed3) {
              break event_switch;
            }
            else {
              mousePressed3 = true;
            }
          }

/* WLH 22 Aug 98
          // special hack for BUTTON1 error in getModifiers
          if (m2 == 0 && m3 == 0 &&
              !mousePressed2 && !mousePressed3) {
*/
          if (mousePressed1 || mouseCombo1) {
            start_x = ((MouseEvent) event).getX();
            start_y = ((MouseEvent) event).getY();
            tstart = proj.getMatrix();

            if (mshift != 0) {
              z1Pressed = true;
            }
            else if (mctrl != 0 || mode2D) {
              t1Pressed = true;
            }
            // WLH 19 July 99
            if (mctrl == 0 && !z1Pressed) {
              try {
                display.notifyListeners(DisplayEvent.MOUSE_PRESSED_LEFT,
                                        mouse_x, mouse_y);
              }
              catch (VisADException e) {
              }
              catch (RemoteException e) {
              }
            }
          }
          else if (mousePressed2 || mouseCombo2) {
            // turn cursor on whenever mouse button2 pressed
            display_renderer.setCursorOn(true);

            start_x = ((MouseEvent) event).getX();
            start_y = ((MouseEvent) event).getY();
            tstart = proj.getMatrix();

            if (mshift != 0) {
              z2Pressed = true;
              if (!mode2D) {
                // don't do cursor Z in 2-D mode
                // current_y -> 3-D cursor Z
                VisADRay cursor_ray =
                  behavior.cursorRay(display_renderer.getCursor());
                display_renderer.depth_cursor(cursor_ray);
              }
            }
            else if (mctrl != 0) {
              t2Pressed = true;
            }
            else {
              VisADRay cursor_ray = behavior.findRay(start_x, start_y);
              if (cursor_ray != null) {
                display_renderer.drag_cursor(cursor_ray, true);
              }
            }
            //- TDR, Oct. 1998
            if (!t2Pressed && !z2Pressed) {
              try {
                display.notifyListeners(DisplayEvent.MOUSE_PRESSED_CENTER,
                                        mouse_x, mouse_y);
              }
              catch (VisADException e) {
              }
              catch (RemoteException e) {
              }
            }
            //--
          }
          else if (mousePressed3 || mouseCombo3) {
            if (display_renderer.anyDirects()) {
              int current_x = ((MouseEvent) event).getX();
              int current_y = ((MouseEvent) event).getY();
              VisADRay direct_ray =
                behavior.findRay(current_x, current_y);
              if (direct_ray != null) {
                direct_renderer =
                  display_renderer.findDirect(direct_ray);
                if (direct_renderer != null) {
                  display_renderer.setDirectOn(true);
                  direct_renderer.setLastMouseModifiers(mouseModifiers);
                  direct_renderer.drag_direct(direct_ray, true, mouseModifiers);
                }
              }
            }
            // WLH 19 July 99
            try {
              display.notifyListeners(DisplayEvent.MOUSE_PRESSED_RIGHT,
                                      mouse_x, mouse_y);
            }
            catch (VisADException e) {
            }
            catch (RemoteException e) {
            }
          }
        }
        break;
      case MouseEvent.MOUSE_RELEASED:
        int m = ((InputEvent) event).getModifiers();
        int m1 = m & InputEvent.BUTTON1_MASK;
        int m2 = m & InputEvent.BUTTON2_MASK;
        int m3 = m & InputEvent.BUTTON3_MASK;

        // DRM add 17 Sep 1999
        if (mousePressed1 || mousePressed2 || mousePressed3 ||
            mouseCombo1 || mouseCombo2 || mouseCombo3) {
          try {
            display.notifyListeners(DisplayEvent.MOUSE_RELEASED,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }

        if (mousePressed3 || mouseCombo3) {
          if (direct_renderer != null) {
            direct_renderer.release_direct();
          }
        }

        if (m1 != 0 && mousePressed1) {
          mousePressed1 = false;
          z1Pressed = false;
          t1Pressed = false;
          // DRM add 17 Sep 1999
          try {
            display.notifyListeners(DisplayEvent.MOUSE_RELEASED_LEFT,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }
        else if ((m2 != 0 || m3 != 0) && mouseCombo1) {
          mouseCombo1 = false;
          z1Pressed = false;
          t1Pressed = false;
          // DRM add 17 Sep 1999
          try {
            display.notifyListeners(DisplayEvent.MOUSE_RELEASED_LEFT,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }
        else if (m2 != 0 && mousePressed2) {
          mousePressed2 = false;
          display_renderer.setCursorOn(false);
          z2Pressed = false;
          t2Pressed = false;
          // DRM add 17 Sep 1999
          try {
            display.notifyListeners(DisplayEvent.MOUSE_RELEASED_CENTER,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }
        else if ((m1 != 0 || m3 != 0) && mouseCombo2) {
          mouseCombo2 = false;
          display_renderer.setCursorOn(false);
          z2Pressed = false;
          t2Pressed = false;
          // DRM add 17 Sep 1999
          try {
            display.notifyListeners(DisplayEvent.MOUSE_RELEASED_CENTER,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }
        else if (m3 != 0 && mousePressed3) {
          mousePressed3 = false;
          display_renderer.setDirectOn(false);
          direct_renderer = null;
          // DRM add 17 Sep 1999
          try {
            display.notifyListeners(DisplayEvent.MOUSE_RELEASED_RIGHT,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }
        else if ((m1 != 0 || m2 != 0) && mouseCombo3) {
          mouseCombo3 = false;
          display_renderer.setDirectOn(false);
          direct_renderer = null;
          // DRM add 17 Sep 1999
          try {
            display.notifyListeners(DisplayEvent.MOUSE_RELEASED_RIGHT,
                                    mouse_x, mouse_y);
          }
          catch (VisADException e) {
          }
          catch (RemoteException e) {
          }
        }
        mouseModifiers = 0;
        break;
      case MouseEvent.MOUSE_DRAGGED:
        if (mousePressed1 || mousePressed2 || mousePressed3 ||
            mouseCombo1 || mouseCombo2 || mouseCombo3) {
          Dimension d = ((MouseEvent) event).getComponent().getSize();
          int current_x = ((MouseEvent) event).getX();
          int current_y = ((MouseEvent) event).getY();
          if (mousePressed1 || mouseCombo1) {
            //
            // TO_DO
            // modify to use rotX, rotY, rotZ, setTranslation & setScale
            //
            double[] t1 = null;
            if (z1Pressed) {
              // current_y -> scale
              double scale =
                Math.exp((start_y-current_y) / (double) d.height);
              t1 = behavior.make_matrix(0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0);
            }
            else if (t1Pressed) {
              // current_x, current_y -> translate
              double transx =
                (start_x - current_x) * -2.0 / (double) d.width;
              double transy =
                (start_y - current_y) * 2.0 / (double) d.height;
              t1 = behavior.make_translate(transx, transy);
            }
            else {
              if (!mode2D) {
                // don't do 3-D rotation in 2-D mode
                double angley =
                  - (current_x - start_x) * 100.0 / (double) d.width;
                double anglex =
                  - (current_y - start_y) * 100.0 / (double) d.height;
                t1 = behavior.make_matrix(anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
              }
            }
            if (t1 != null) {
              t1 = behavior.multiply_matrix(t1, tstart);
              try {
                proj.setMatrix(t1);
              }
              catch (VisADException e) {
              }
              catch (RemoteException e) {
              }
            }
          }
          else if (mousePressed2 || mouseCombo2) {
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
                  behavior.make_matrix(anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                t1 = behavior.multiply_matrix(t1, tstart);
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
              if (cursor_ray != null) {
                display_renderer.drag_cursor(cursor_ray, false);
              }
            }
          }
          else if (mousePressed3 || mouseCombo3) {
            if (direct_renderer != null) {
              VisADRay direct_ray = behavior.findRay(current_x, current_y);
              if (direct_ray != null) {
                direct_renderer.setLastMouseModifiers(mouseModifiers);
                direct_renderer.drag_direct(direct_ray, false, mouseModifiers);
              }
            }
          }
        }
        break;
      default:
        System.out.println("MouseHelper.processStimulus: event type" +
                           "not recognized " + event.getID());
        break;
    }
  }

}

