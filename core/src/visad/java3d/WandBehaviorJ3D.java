//
// WandBehaviorJ3D.java
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

package visad.java3d;

import visad.*;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;

import java.util.*;

/**
   WandBehaviorJ3D is the VisAD class for wand behaviors for Java3D
   it works with ImmersaDeskDisplayRendererJ3D
*/

/* extend MouseBehaviorJ3D to inherit multiply_matrix, make_matrix, etc */
public class WandBehaviorJ3D extends MouseBehaviorJ3D
       implements Runnable, MouseBehavior {

  /** DisplayRenderer for Display */
  ImmersaDeskDisplayRendererJ3D display_renderer;
  DisplayImpl display;

  private Thread wandThread;

  // use vpTrans for head motion from tracker,
  // and for left-button wand motion
  private TransformGroup vpTrans;

  // initial Transform3D from vpTrans
  private Transform3D init_trans;

  private float[] head_position = new float[3];
  private float[] wand_position = new float[3];
  private float[] wand_vector = new float[3];
  // integral of wand_vector during left == true
  private float[] travel_position = new float[3];
  // wand button states
  private boolean left = false, center = false, right = false;
  // previous right wand button state
  private boolean last_right;

  private DataRenderer direct_renderer = null;

  private TrackdJNI hack;

  public WandBehaviorJ3D(ImmersaDeskDisplayRendererJ3D r,
                         int tracker_shmkey, int controller_shmkey)
         throws VisADException {
    super();
    display_renderer = r;
    display = display_renderer.getDisplay();

    hack = new TrackdJNI(tracker_shmkey, controller_shmkey);

    last_right = false;
  }

  /* override MouseBehaviorJ3D.initialize() to do start Thread */
  public void initialize() {
    wandThread = new Thread(this);
    wandThread.start();
    vpTrans = display_renderer.getViewTrans();

    init_trans = new Transform3D();
    vpTrans.getTransform(init_trans);
  }

  /* override MouseBehaviorJ3D.processStimulus() to do nothing */
  public void processStimulus(Enumeration criteria) {
  }

  public void stop() {
    wandThread = null;
  }


  // QUESTION? values of these BOLD variables QUESTION?
  private static int DELAY = 50; // ms
  private static int NSENSORS = 4;
  private static int NBUTTONS = 3;

  // sensor numbers
  private static int HEAD = 0;
  private static int WAND = 1;

  // button numbers
  private static int LEFT = 0;
  private static int CENTER = 1;
  private static int RIGHT = 2;

  // angle numbers
  private static int ELEVATION = 0;
  private static int AZIMUTH = 1;
  private static int ROLL = 2;

  // graphics distance (feet?) per second
  private float TRAVEL_SPEED = 4.0f;

  // scale factors for head / wand translation (negative?)
  // NOTE - you may need to adjust these for your ImmersaDesk
  private float HEAD_SCALE = 0.10f;
  private float WAND_SCALE = 1.5f;

  // offsets for head / wand translation
  // NOTE - you may need to adjust these for your ImmersaDesk
  private float HEADX_OFFSET = 0.0f;
  private float HEADY_OFFSET = -3.0f;
  private float HEADZ_OFFSET = 5.0f;
  private float WANDX_OFFSET = -0.5f;
  private float WANDY_OFFSET = -2.0f;
  private float WANDZ_OFFSET = 0.0f;

  // length of direct manipulation ray
  private float RAY_LENGTH = 2.0f;


  public void run() {
    Thread me = Thread.currentThread();
    int[] number_of_sensors = new int[1];
    float[] sensor_positions = new float[NSENSORS * 3];
    float[] sensor_angles = new float[NSENSORS * 3];
    float[] sensor_matrices = new float[NSENSORS * 4 * 4];
    int[] number_of_buttons = new int[1];
    int[] button_states = new int[NBUTTONS];
    int nprint = 1000 / DELAY;
    travel_position[0] = 0.0f;
    travel_position[1] = 0.0f;
    travel_position[2] = 0.0f;
    while (wandThread == me) {
      try {
        synchronized (this) {
          wait(DELAY);
        }
      }
      catch(InterruptedException e) {
        // control doesn't normally come here
      }
      number_of_sensors[0] = NSENSORS;
      number_of_buttons[0] = NBUTTONS;
      hack.getTrackd(number_of_sensors, sensor_positions, sensor_angles,
                     sensor_matrices, number_of_buttons, button_states);
/*
      nprint--;
      if (nprint <= 0) {
        nprint = 1000 / DELAY;
        for (int i=0; i<number_of_sensors[0]; i++) {
          int i3 = i * 3;
          System.out.println("sensor " + i + " " + sensor_positions[i3] + " " +
                             sensor_positions[i3 + 1] + " " +
                             sensor_positions[i3 + 2] + " " +
                             sensor_angles[i3] + " " + sensor_angles[i3 + 1] + " " +
                             sensor_angles[i3 + 2]);
        }
        System.out.println(number_of_buttons[0] + " buttons: " + button_states[0] +
                           " " + button_states[1] + " " + button_states[2]);
      }
*/

      last_right = right;
      left = (button_states[LEFT] != 0);
      center = (button_states[CENTER] != 0);
      right = (button_states[RIGHT] != 0);

      head_position[0] = sensor_positions[3 * HEAD];
      head_position[1] = sensor_positions[3 * HEAD + 1];
      head_position[2] = sensor_positions[3 * HEAD + 2];
      wand_position[0] = sensor_positions[3 * WAND];
      wand_position[1] = sensor_positions[3 * WAND + 1];
      wand_position[2] = sensor_positions[3 * WAND + 2];

      float elevation = (float)
        (sensor_angles[3 * WAND + ELEVATION] * Data.DEGREES_TO_RADIANS);
      float azimuth = (float)
        (sensor_angles[3 * WAND + AZIMUTH] * Data.DEGREES_TO_RADIANS);
      float roll = (float)
        (sensor_angles[3 * WAND + ROLL] * Data.DEGREES_TO_RADIANS);

      // QUESTION? angles all 0.0 == wand pointed forward QUESTION?
      // start with unit vector in (0, 0, 0) wand orientation
      float x = 0.0f;
      float y = 0.0f;
      float z = -1.0f;

      // QUESTION? is order of rotations: x, y then z QUESTION?
      // rotate elevation
      float xx = x;
      float yy = (float) (Math.cos(elevation) * y - Math.sin(elevation) * z);
      float zz = (float) (Math.cos(elevation) * z + Math.sin(elevation) * y);
      // rotate azimuth
      x = (float) (Math.cos(azimuth) * xx + Math.sin(azimuth) * zz);
      y = yy;
      z = (float) (Math.cos(azimuth) * zz - Math.sin(azimuth) * xx);
      // don't rotate roll
      wand_vector[0] = x;
      wand_vector[1] = y;
      wand_vector[2] = z;

      // move along wand direction when left button pressed
      if (left) {
        float increment = TRAVEL_SPEED * DELAY / 1000.0f;
        travel_position[0] += increment * wand_vector[0];
        travel_position[1] += increment * wand_vector[1];
        travel_position[2] += increment * wand_vector[2];
      }

      // change vpTrans based on head_position and travel_position
      double headx =
        HEAD_SCALE * (head_position[0] + travel_position[0] + HEADX_OFFSET);
      double heady =
        HEAD_SCALE * (head_position[1] + travel_position[1] + HEADY_OFFSET);
      double headz =
        HEAD_SCALE * (head_position[2] + travel_position[2] + HEADZ_OFFSET);
      double[] matrix =
        MouseBehaviorJ3D.static_make_matrix(0.0, 0.0, 0.0, 1.0, headx, heady, headz);

      Transform3D temp = new Transform3D(init_trans);
      Transform3D tm = new Transform3D(matrix);
      temp.mul(tm);
      vpTrans.setTransform(temp);

      Transform3D t3d = new Transform3D();
      display_renderer.getTrans().getTransform(t3d);

      // QUESTION? + or - travel_position QUESTION?
      float wandx =
        WAND_SCALE * (wand_position[0] + travel_position[0] + WANDX_OFFSET);
      float wandy =
        WAND_SCALE * (wand_position[1] + travel_position[1] + WANDY_OFFSET);
      float wandz =
        WAND_SCALE * (wand_position[2] + travel_position[2] + WANDZ_OFFSET);

      Point3f p3f = new Point3f(wandx, wandy, wandz);
      t3d.transform(p3f);
      wandx = p3f.x;
      wandy = p3f.y;
      wandz = p3f.z;

      display_renderer.setCursorOn(center);
      if (center) {
        display_renderer.setCursorLoc(wandx, wandy, wandz);
      }

      if (right && display_renderer.anyDirects()) {
        float wand_endx = wandx +
          WAND_SCALE * (RAY_LENGTH * wand_vector[0]);
        float wand_endy = wandy +
          WAND_SCALE * (RAY_LENGTH * wand_vector[1]);
        float wand_endz = wandz +
          WAND_SCALE * (RAY_LENGTH * wand_vector[2]);

        p3f = new Point3f(wand_endx, wand_endy, wand_endz);
        t3d.transform(p3f);
        wand_endx = p3f.x;
        wand_endy = p3f.y;
        wand_endz = p3f.z;

        float[] ray_verts = {wandx, wandy, wandz, wand_endx, wand_endy, wand_endz};
        display_renderer.setRayOn(true, ray_verts);
        VisADRay direct_ray = new VisADRay();
        direct_ray.position[0] = wandx;
        direct_ray.position[1] = wandy;
        direct_ray.position[2] = wandz;
        // QUESTION? should ray.vector simply equal wand_vector QUESTION?
        direct_ray.vector[0] = wand_vector[0];
        direct_ray.vector[1] = wand_vector[1];
        direct_ray.vector[2] = wand_vector[2];

        if (!last_right) {
          // first point
          direct_renderer = display_renderer.findDirect(direct_ray, 0);
          if (direct_renderer != null) {
            display_renderer.setDirectOn(true);
            direct_renderer.drag_direct(direct_ray, true, 0);
          }
        }
        else {
          if (direct_renderer != null) {
            direct_renderer.drag_direct(direct_ray, false, 0);
          }
        }
      }
      else { // !right || !display_renderer.anyDirects()
        display_renderer.setRayOn(false, null);
        direct_renderer = null;
      }

    } // end while (wandThread == me)
  }

}

