//
//  KeyboardBehaviorJ3D.java
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

package visad.java3d;

import java.awt.event.*;
import java.awt.AWTEvent;
import java.util.Enumeration;
import javax.media.j3d.*;
import java.rmi.RemoteException;
import javax.vecmath.*;
import visad.*;


/**
 *  KeyboardBehaviorJ3D is the VisAD class for keyboard control of
 *  translate, zoom and pan.
 *  @author Troy Sandblom   NCAR/RAP May, 2000
 *  @author Don Murray (adapted to VisAD)
 */
public class KeyboardBehaviorJ3D extends Behavior {

    private ProjectionControl proj;
    private DisplayRenderer displayRenderer;
    private MouseBehavior mouseBehavior;

    private double rotateAmount = 5.0;
    private double scaleAmount = .05;
    private double transAmount = .1;

    /**
     *  Condition that causes this Behavior to wake up.
     */
    protected WakeupCondition wakeupCondition = null;
    
    /**
     * Construct a new keyboard behavior for the DisplayRenderer.  You
     * need to add the behavior to the DisplayRenderer if you want to 
     * use it.
     * @see  DisplayRendererJ3D#addKeyboardBehavior(KeyboardBehaviorJ3D behavior)
     * @param  r  DisplayRenderer to use.
     */
    public KeyboardBehaviorJ3D(DisplayRendererJ3D r) {
        displayRenderer = r;
        proj = displayRenderer.getDisplay().getProjectionControl();
        mouseBehavior = displayRenderer.getMouseBehavior();
        
        WakeupOnAWTEvent wakeup = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
        WakeupCriterion[] wakeupCriteria = { wakeup };
        wakeupCondition = new WakeupOr(wakeupCriteria);

        Bounds bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                                           2000000.0);
        this.setSchedulingBounds(bounds);
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
     *  Process a key event.  This is where the real action takes
     *  place.
     *  @param  even  KeyEvent stimulus
     */
    protected void processKeyEvent(KeyEvent event) {
        boolean controlOn = false;
        boolean shiftOn = false;
        
        int modifiers = event.getModifiers();
        if ((modifiers & InputEvent.CTRL_MASK) != 0)
            controlOn = true;
        if ((modifiers & InputEvent.SHIFT_MASK) != 0)
            shiftOn = true;
        // handle 2D in Java3D (no modifier is same as control)
        if ( !(shiftOn) && displayRenderer.getMode2D())  
            controlOn = true;

        int keyCode = event.getKeyCode();
        double [] t1 = null;
        double [] tstart = proj.getMatrix();

        // translate
        if (controlOn) {           
            double transx = 0.0;
            double transy = 0.0;
            
            switch (keyCode) {
            case KeyEvent.VK_UP:
                transy += transAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, transx, transy, 0.0);
                break;
            case KeyEvent.VK_DOWN:
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


        // zoom
        } else if (shiftOn) {

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
            case KeyEvent.VK_LEFT:
                anglez -= rotateAmount;
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, anglez, 1.0, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_RIGHT:
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

        //  Rotate 
        } else {

            double angley = 0.0;
            double anglex = 0.0;
            
            switch (keyCode) {
            case KeyEvent.VK_UP:
                anglex += rotateAmount;
                t1 = mouseBehavior.make_matrix(
                                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_DOWN:
                anglex -= rotateAmount;
                t1 = mouseBehavior.make_matrix(
                                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_LEFT:
                angley += rotateAmount;
                t1 = mouseBehavior.make_matrix(
                                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_RIGHT:
                angley -= rotateAmount;
                t1 = mouseBehavior.make_matrix(
                                  anglex, angley, 0.0, 1.0, 0.0, 0.0, 0.0);
                break;
            case KeyEvent.VK_R:  // reset
                tstart = proj.getSavedProjectionMatrix();
                t1 = mouseBehavior.make_matrix(
                                  0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
                break;
            default:
                break;
            }

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
