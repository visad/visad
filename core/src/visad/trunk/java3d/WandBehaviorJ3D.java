//
// WandBehaviorJ3D.java
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
   WandBehaviorJ3D is the VisAD class for mouse behaviors for Java3D
*/

/*
   On MOUSE_PRESSED event with left button,
   ((InputEvent) events[i]).getModifiers() returns 0 and
   should return 16 ( = InputEvent.BUTTON1_MASK ).
   ((InputEvent) events[i]).getModifiers() correctly
   returns 16 on MOUSE_RELEASED with left button.
*/
public class WandBehaviorJ3D extends MouseBehaviorJ3D
       implements MouseBehavior {

  /** wakeup condition for WandBehaviorJ3D */
  private WakeupOr wakeup;
  /** DisplayRenderer for Display */
  DisplayRendererJ3D display_renderer;
  DisplayImpl display;

  MouseHelper helper = null;

  public WandBehaviorJ3D(DisplayRendererJ3D r) {
    super();
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
        System.out.println("WandBehaviorJ3D.processStimulus: non-" +
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

}

