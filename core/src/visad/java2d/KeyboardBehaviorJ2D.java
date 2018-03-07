//
//  KeyboardBehaviorJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;

import visad.DisplayEvent;
import visad.DisplayImpl;
import visad.DisplayRenderer;
import visad.KeyboardBehavior;
import visad.MouseBehavior;
import visad.ProjectionControl;
import visad.VisADException;

/**
 *  KeyboardBehaviorJ2D is the VisAD class for keyboard control of
 *  translate (pan) and zoom in Java2D.
 *  @author Troy Sandblom   NCAR/RAP May, 2000
 *  @author Don Murray (adapted to VisAD with help from Curtis)
 */
public class KeyboardBehaviorJ2D 
  implements KeyboardBehavior {

  private ProjectionControl proj;
  private DisplayRenderer displayRenderer;
  private MouseBehavior mouseBehavior;

  private double rotateAmount = 5.0;
  private double scaleAmount = .05;
  private double transAmount = .1;

  /** Identifier for function to rotate positively around the Z viewing axis
  (perpendicular to the screen plane) */
  public static final int ROTATE_Z_POS = 7;

  /** Identifier for function to rotate negatively around the Z viewing axis
  (perpendicular to the screen plane) */
  public static final int ROTATE_Z_NEG = 8;

  /** Maximum number of functions for this behavior */
  private final int MAX_FUNCTIONS = 9;

  // Should someday make an object that encapsulates both and use that
  // for checking.
  private int[] functionKeys = new int[MAX_FUNCTIONS];
  private int[] functionMods = new int[MAX_FUNCTIONS];

  /**
   * Construct a new keyboard behavior for the DisplayRenderer.  You
   * need to add the behavior to the DisplayRenderer if you want to 
   * use it.  Default keys for manipulations are as follows:<BR>
   * Translation
   * <UL>
   * <LI>Arrow keys - translate up, down, left, right
   * </UL>
   * Zoom
   * <UL>
   * <LI>Shift + Up arrow - zoom in (ZOOM_IN)
   * <LI>Shift + Down arrow - zoom out (ZOOM_OUT)
   * </UL>
   * Rotate
   * <UL>
   * <LI>Shift + Left arrow - rotate left  (ROTATE_Z_POS)
   * <LI>Shift + Right arrow - rotate right (ROTATE_Z_NEG)
   * </UL>
   * Reset
   * <UL>
   * <LI>Ctrl + R key - reset to original projection (RESET)
   * </UL>
   * <P>
   * @see  DisplayRendererJ2D#addKeyboardBehavior(KeyboardBehaviorJ2D behavior)
   * @param  r  DisplayRenderer to use.
   */
  public KeyboardBehaviorJ2D(DisplayRendererJ2D r) {
    displayRenderer = r;
    proj = displayRenderer.getDisplay().getProjectionControl();
    mouseBehavior = displayRenderer.getMouseBehavior();
    // initialize array functions
    mapKeyToFunction(TRANSLATE_UP, KeyEvent.VK_UP, NO_MASK);
    mapKeyToFunction(TRANSLATE_DOWN, KeyEvent.VK_DOWN, NO_MASK);
    mapKeyToFunction(TRANSLATE_LEFT, KeyEvent.VK_LEFT, NO_MASK);
    mapKeyToFunction(TRANSLATE_RIGHT, KeyEvent.VK_RIGHT, NO_MASK);
    mapKeyToFunction(ZOOM_IN, KeyEvent.VK_UP, InputEvent.SHIFT_MASK);
    mapKeyToFunction(ZOOM_OUT, KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK);
    mapKeyToFunction(RESET, KeyEvent.VK_R, InputEvent.CTRL_MASK);
    mapKeyToFunction(ROTATE_Z_POS, KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK);
    mapKeyToFunction(ROTATE_Z_NEG, KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK);
  }

  /**
   * Maps key represented by keycode & modifiers to the given function.
   * Each function can only have one key/modifier combination assigned 
   * to it at a time.
   * @see java.awt.event.KeyEvent
   * @see java.awt.event.InputEvent
   * @param  function  keyboard function (ROTATE_X_POS, ZOOM_IN, etc)
   * @param  keycode   <CODE>KeyEvent</CODE> virtual keycodes 
   * @param  modifiers <CODE>InputEvent</CODE> key mask
   */
  public void mapKeyToFunction(int function, int keycode, int modifiers) {
     if (function < 0 || function >= MAX_FUNCTIONS) return;
     functionKeys[function] = keycode;
     functionMods[function] = modifiers;
  }

  /**
   *  Process a key event.  Determines whether a meaningful key was pressed.
   *  @param  event  KeyEvent stimulus
   */
  
  public void processKeyEvent(KeyEvent event) {
    int id = event.getID();

    if (id == KeyEvent.KEY_PRESSED) {
      int modifiers = event.getModifiers();
      int keyCode = event.getKeyCode();

      // determine whether a meaningful key was pressed
      for (int i=0; i<MAX_FUNCTIONS; i++) {
        if (functionKeys[i] == keyCode && (modifiers == functionMods[i])) {
          execFunction(i);
          break;
        }
      }
    }

    // notify DisplayListeners of key event
    int d_id = -1;
    if (id == KeyEvent.KEY_PRESSED) d_id = DisplayEvent.KEY_PRESSED;
    else if (id == KeyEvent.KEY_RELEASED) d_id = DisplayEvent.KEY_RELEASED;
    if (d_id != -1) {
      try {
        DisplayImpl display = displayRenderer.getDisplay();
        DisplayEvent e = new DisplayEvent(display, d_id, event);
        display.notifyListeners(e);
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  /** 
   * Executes the given function. 
   * @param  function   function to perform (TRANSLATE_UP, ZOOM_IN, etc)
   */
  public void execFunction(int function) {

    double transx = 0.0;
    double transy = 0.0;
    double scale = 1.0;
    double anglez = 0.0;
    double [] t1 = null;
    double [] tstart = proj.getMatrix();

    switch (function) {

      case TRANSLATE_UP: // NB: down is up in Java2D compared with Java3D
        transy -= transAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
        break;
      case TRANSLATE_DOWN:
        transy += transAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
        break;
      case TRANSLATE_LEFT:
        transx -= transAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
        break;
      case TRANSLATE_RIGHT:
        transx += transAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
        break;
      case RESET:
        tstart = proj.getSavedProjectionMatrix();
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case ZOOM_IN:
        scale += scaleAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0);
        break;
      case ZOOM_OUT:
        scale -= scaleAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0);
        break;
      case ROTATE_Z_NEG:
        anglez -= rotateAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, anglez, 1.0, 0.0, 0.0, 0.0);
        break;
      case ROTATE_Z_POS:
        anglez += rotateAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, anglez, 1.0, 0.0, 0.0, 0.0);
        break;
      default:
        break;
    }

    if (t1 != null) {
      t1 = mouseBehavior.multiply_matrix(t1, tstart);
      try {
        proj.setMatrix(t1);
      } catch (VisADException e) {
      } catch (RemoteException e) {
      }
    }
    
  }
}
