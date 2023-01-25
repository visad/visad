//
//  KeyboardBehaviorJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.AWTEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Point3d;

import visad.DisplayEvent;
import visad.DisplayImpl;
import visad.DisplayRenderer;
import visad.KeyboardBehavior;
import visad.MouseBehavior;
import visad.ProjectionControl;
import visad.VisADException;

/**
 *  KeyboardBehaviorJ3D is the VisAD class for keyboard control of
 *  rotation, translation, and zoom of display in Java3D.
 *  @author Troy Sandblom   NCAR/RAP May, 2000
 *  @author Don Murray (adapted to VisAD with input from Curtis)
 */
public class KeyboardBehaviorJ3D extends Behavior 
  implements KeyboardBehavior 
{

  private ProjectionControl proj;
  private DisplayRenderer displayRenderer;
  private MouseBehavior mouseBehavior;

  private double rotateAmount = 5.0;
  private double scaleAmount = .05;
  private double transAmount = .1;

  /** Identifier for function to rotate positively around the Z viewing axis*/
  public static final int ROTATE_Z_POS = 7;

  /** Identifier for function to rotate negatively around the Z viewing axis*/
  public static final int ROTATE_Z_NEG = 8;

  /** Identifier for function to rotate positively around the X viewing axis*/
  public static final int ROTATE_X_POS = 9;

  /** Identifier for function to rotate negatively around the X viewing axis*/
  public static final int ROTATE_X_NEG = 10;

  /** Identifier for function to rotate positively around the Y viewing axis*/
  public static final int ROTATE_Y_POS = 11;

  /** Identifier for function to rotate negatively around the Y viewing axis*/
  public static final int ROTATE_Y_NEG = 12;

/* WLH 19 Feb 2001
  // Maximum number of functions for this behavior
  private final int MAX_FUNCTIONS = 13;

  private int[] functionKeys = new int[MAX_FUNCTIONS];
  private int[] functionMods = new int[MAX_FUNCTIONS];
*/
  private int MAX_FUNCTIONS;
  private int[] functionKeys = null;
  private int[] functionMods = null;
 
  /**
   *  Condition that causes this Behavior to wake up.
   */
  protected WakeupCondition wakeupCondition = null;
    
  /**
   * Construct a new keyboard behavior for the DisplayRenderer.  You
   * need to add the behavior to the DisplayRenderer if you want to 
   * use it.  Default key assignments use the arrow keys to simulate
   * the default VisAD mouse behavior.<P>
   * Rotation (3D renderer only):
   * <UL>
   * <LI>Arrow keys 
   *     - rotate up (X_NEG), down(X_POS), left(Y_NEG), right(Y_POS)
   * </UL>
   * Zoom:
   * <UL>
   * <LI>Shift + Up arrow - zoom in (ZOOM_IN)
   * <LI>Shift + Down arrow - zoom out  (ZOOM_OUT)
   * </UL>
   * Rotate:
   * <UL>
   * <LI>Shift + Left arrow - rotate left around axis perpendicular to screen
   *                          (Z_POS)
   * <LI>Shift + Right arrow - rotate right around axis perpendicular 
   *                           to screen (Z_NEG)
   * </UL>
   * Translate:
   * <UL>
   * <LI>Ctrl + Arrow keys - translate up, down, left, right
   * </UL>
   * Reset:
   * <UL>
   * <LI>Ctrl + R key - reset to original projection  (RESET)
   * </UL>
   * <P>
   * @see  DisplayRendererJ3D#addKeyboardBehavior(KeyboardBehaviorJ3D behavior)
   * @see  #mapKeyToFunction(int function, int keycode, int modifiers)  to 
   *       change default key to function mappings
   * @param  r  DisplayRenderer to use.
   */
  public KeyboardBehaviorJ3D(DisplayRendererJ3D r) {
    displayRenderer = r;
    boolean mode2D = displayRenderer.getMode2D();
    proj = displayRenderer.getDisplay().getProjectionControl();
    mouseBehavior = displayRenderer.getMouseBehavior();
    
    WakeupCriterion[] wakeupCriteria = {
      new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED),
      new WakeupOnAWTEvent(KeyEvent.KEY_RELEASED),
    };
    wakeupCondition = new WakeupOr(wakeupCriteria);

    Bounds bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                                       2000000.0);
    this.setSchedulingBounds(bounds);

    // WLH 19 Feb 2001
    MAX_FUNCTIONS = 13;
    functionKeys = new int[MAX_FUNCTIONS];
    functionMods = new int[MAX_FUNCTIONS];

    // initialize array functions
    mapKeyToFunction(
      TRANSLATE_UP, KeyEvent.VK_UP, 
        (mode2D == true) ? NO_MASK : InputEvent.CTRL_MASK);
    mapKeyToFunction(
      TRANSLATE_DOWN, KeyEvent.VK_DOWN, 
        (mode2D == true) ? NO_MASK : InputEvent.CTRL_MASK);
    mapKeyToFunction(
      TRANSLATE_LEFT, KeyEvent.VK_LEFT, 
        (mode2D == true) ? NO_MASK : InputEvent.CTRL_MASK);
    mapKeyToFunction(
      TRANSLATE_RIGHT, KeyEvent.VK_RIGHT, 
        (mode2D == true) ? NO_MASK : InputEvent.CTRL_MASK);
    mapKeyToFunction(ZOOM_IN, KeyEvent.VK_UP, InputEvent.SHIFT_MASK);
    mapKeyToFunction(ZOOM_OUT, KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK);
    mapKeyToFunction(RESET, KeyEvent.VK_R, InputEvent.CTRL_MASK);
    mapKeyToFunction(ROTATE_X_POS, KeyEvent.VK_DOWN, NO_MASK);
    mapKeyToFunction(ROTATE_X_NEG, KeyEvent.VK_UP, NO_MASK);
    mapKeyToFunction(ROTATE_Y_POS, KeyEvent.VK_LEFT, NO_MASK);
    mapKeyToFunction(ROTATE_Y_NEG, KeyEvent.VK_RIGHT, NO_MASK);
    mapKeyToFunction(
      ROTATE_Z_POS, KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK);
    mapKeyToFunction(
      ROTATE_Z_NEG, KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK);
  }

  // WLH 19 Feb 2001
  public KeyboardBehaviorJ3D(DisplayRendererJ3D r, int num_functions) {
    displayRenderer = r;
    boolean mode2D = displayRenderer.getMode2D();
    proj = displayRenderer.getDisplay().getProjectionControl();
    mouseBehavior = displayRenderer.getMouseBehavior();

    WakeupCriterion[] wakeupCriteria = {
      new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED),
      new WakeupOnAWTEvent(KeyEvent.KEY_RELEASED),
    };
    wakeupCondition = new WakeupOr(wakeupCriteria);

    Bounds bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                                       2000000.0);
    this.setSchedulingBounds(bounds);

    MAX_FUNCTIONS = num_functions;
    functionKeys = new int[MAX_FUNCTIONS];
    functionMods = new int[MAX_FUNCTIONS];
  }

  /**
   * Initialize this behavior. NOTE: Applications should not call 
   * this method. It is called by the Java 3D behavior scheduler.
   */
  public void initialize() {
    wakeupOn(wakeupCondition);
  }

  /**
   * Process a stimulus meant for this behavior.  This method is
   * invoked when a key is pressed. NOTE: Applications should not 
   * call this method. It is called by the Java 3D behavior scheduler.
   * @param criteria  an enumeration of triggered wakeup criteria
   */
  public void processStimulus(Enumeration criteria) {
    WakeupOnAWTEvent event;
    WakeupCriterion genericEvent;
    AWTEvent[] events;

    while (criteria.hasMoreElements()) {
      genericEvent = (WakeupCriterion) criteria.nextElement();
      if (genericEvent instanceof WakeupOnAWTEvent) {
        event = (WakeupOnAWTEvent) genericEvent;
        events = event.getAWTEvent();

        //  Process each event
        for (int i = 0; i < events.length; i++) {
          if (events[i] instanceof KeyEvent)
            processKeyEvent((KeyEvent)events[i]);
        }
      }
      wakeupOn(wakeupCondition);
    }
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
    double anglex = 0.0;
    double angley = 0.0;
    double anglez = 0.0;

    double [] t1 = null;
    double [] tstart = proj.getMatrix();

    double[] transA         = { 0.0, 0.0, 0.0 };
    double[] rotA           = { 0.0, 0.0, 0.0 };
    double[] scaleA         = { 0.0, 0.0, 0.0 };

    MouseBehaviorJ3D.unmake_matrix(rotA,  scaleA,
                                   transA,tstart);

    double rotateAngle = rotateAmount;
    if(displayRenderer.getScaleRotation()) {
    //Scale down the rotation angle when we are zoomed in
        rotateAngle = rotateAmount/scaleA[0];
    }



    boolean wasRotate = false;
    switch (function) {

      case ROTATE_X_NEG:
        if (displayRenderer.getMode2D()) break;
        wasRotate = true;
        anglex += rotateAngle;
        t1 = mouseBehavior.make_matrix(
                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case ROTATE_X_POS:
        if (displayRenderer.getMode2D()) break;
        wasRotate = true;
        anglex -= rotateAngle;
        t1 = mouseBehavior.make_matrix(
                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case ROTATE_Y_POS:
        if (displayRenderer.getMode2D()) break;
        wasRotate = true;
        angley += rotateAngle;
        t1 = mouseBehavior.make_matrix(
                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case ROTATE_Y_NEG:
        if (displayRenderer.getMode2D()) break;
        wasRotate = true;
        angley -= rotateAngle;
        t1 = mouseBehavior.make_matrix(
                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      case ROTATE_Z_POS:
        wasRotate = true;
        anglez -= rotateAngle;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, anglez, 1.0, 0.0, 0.0, 0.0);
        break;
      case ROTATE_Z_NEG:
        wasRotate = true;
        anglez += rotateAngle;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, anglez, 1.0, 0.0, 0.0, 0.0);
        break;
      case TRANSLATE_UP:
        transy += transAmount;
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
        break;
      case TRANSLATE_DOWN:
        transy -= transAmount;
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
      case RESET:  
        tstart = proj.getSavedProjectionMatrix();
        t1 = mouseBehavior.make_matrix(
                  0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
        break;
      default:
        break;
    }


    //        t1 = mouseBehavior.make_matrix(
    //                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);


    double[] t2 = null;


    if (t1 != null) {

        if(displayRenderer.getRotateAboutCenter() && wasRotate) {
            t2 = mouseBehavior.make_translate(-transA[0], -transA[1], -transA[2]);
            tstart = mouseBehavior.multiply_matrix(t2, tstart);
            t2 = mouseBehavior.make_translate(transA[0], transA[1], transA[2]);
        }


        t1 = mouseBehavior.multiply_matrix(t1, tstart);


        if(t2!=null) {
            t1 = mouseBehavior.multiply_matrix(t2,t1);
        }


        try {
            proj.setMatrix(t1);
        } catch (VisADException e) {
        } catch (RemoteException e) {
        }
    }
    
  }
}
