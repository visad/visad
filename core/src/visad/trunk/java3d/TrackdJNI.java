//
// TrackdJNI.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

/**
   TrackdJNI is the VisAD class for connecting WandBehaviorJ3D
   to the trackd native methods.  It is in a seperate class because javah
   cannot find the Java3D classes that are imported by WandBehaviorJ3D.
*/

public class TrackdJNI {

  public TrackdJNI(int tracker_shmkey, int controller_shmkey)
         throws VisADException {
    System.loadLibrary("TrackdAPI");
    int[] status = {0};
    init_trackd_c(tracker_shmkey, controller_shmkey, status);
    if (status[0] != 0) {
      throw new DisplayException("unable to connect to trackd " + status[0]);
    }
  }

  public void getTrackd(int[] number_of_sensors, float[] sensor_positions,
                        float[] sensor_angles, float[] sensor_matrices,
                        int[] number_of_buttons, int[] button_states) {
    get_trackd_c(number_of_sensors, sensor_positions, sensor_angles,
                 sensor_matrices, number_of_buttons, button_states);
  }

  private native void init_trackd_c(int tracker_shmkey,
                                    int controller_shmkey,
                                    int[] status);

  private native void get_trackd_c(int[] number_of_sensors,
                                   float[] sensor_positions,
                                   float[] sensor_angles,
                                   float[] sensor_matrices,
                                   int[] number_of_buttons,
                                   int[] button_states);

}

