//
//  KeyboardBehaviorJ2D.java
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

package visad.java2d;

import java.awt.event.*;
import java.awt.AWTEvent;
import java.util.Enumeration;
import java.rmi.RemoteException;
import visad.*;


/**
 *  KeyboardBehaviorJ2D is the VisAD class for keyboard control of
 *  translate (pan) and zoom in Java2d.
 *  @author Troy Sandblom   NCAR/RAP May, 2000
 *  @author Don Murray (adapted to VisAD)
 */
public class KeyboardBehaviorJ2D {

    private ProjectionControl proj;
    private DisplayRenderer displayRenderer;
    private MouseBehavior mouseBehavior;

    private double rotateAmount = 5.0;
    private double scaleAmount = .05;
    private double transAmount = .1;

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
     * <LI>Shift + Up arrow - zoom in
     * <LI>Shift + Down arrow - zoom out
     * </UL>
     * Rotate
     * <UL>
     * <LI>Shift + Left arrow - rotate left
     * <LI>Shift + Right arrow - rotate right
     * </UL>
     * Reset
     * <UL>
     * <LI>R key - reset to original projection
     * </UL>
     * <P>
     * @see  DisplayRendererJ2D#addKeyboardBehavior(KeyboardBehaviorJ2D behavior)
     * @param  r  DisplayRenderer to use.
     */
    public KeyboardBehaviorJ2D(DisplayRendererJ2D r) {
        displayRenderer = r;
        proj = displayRenderer.getDisplay().getProjectionControl();
        mouseBehavior = displayRenderer.getMouseBehavior();
    }

    /**
     *  Process a key event.  This is where the real action takes
     *  place.
     *  @param  even  KeyEvent stimulus
     */
    public void processKeyEvent(KeyEvent event) {

        boolean shiftOn = false;
        
        int modifiers = event.getModifiers();
        if ((modifiers & InputEvent.SHIFT_MASK) != 0)
            shiftOn = true;

        int keyCode = event.getKeyCode();
        double [] t1 = null;
        double [] tstart = proj.getMatrix();

        if (!shiftOn) {

            // translate
            double transx = 0.0;
            double transy = 0.0;
            
            switch (keyCode) {  // NB: down is up in Java2D compared with Java3D
            case KeyEvent.VK_DOWN:
                transy += transAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
                break;
            case KeyEvent.VK_UP:
                transy -= transAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
                break;
            case KeyEvent.VK_LEFT:
                transx -= transAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
                break;
            case KeyEvent.VK_RIGHT:
                transx += transAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
                break;
            case KeyEvent.VK_R:  // reset
                tstart = proj.getSavedProjectionMatrix();
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
                break;
            default:
                break;
            }


        } else {   // shiftOn

            // zoom
            double scale = 1.0;
            double anglez = 0.0;
            
            switch (keyCode) {
            case KeyEvent.VK_UP:
                scale += scaleAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_DOWN:
                scale -= scaleAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_RIGHT:
                anglez -= rotateAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, anglez, 1.0, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_LEFT:
                anglez += rotateAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, anglez, 1.0, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_R:  // reset
                tstart = proj.getSavedProjectionMatrix();
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
                break;
            default:
                break;
            }

        }   // No rotate in Java2D 2D

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
