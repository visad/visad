 * /*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
 * Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
 * Tommy Jasmin.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA
 */

/* TrackdAPI.c */

#include <jni.h>
#include "visad_java3d_TrackdJNI.h"

#include "trackdAPI.h"

void *tracker;
void *controller;

/*
 * Class:     visad_java3d_TrackdJNI
 * Method:    get_trackd_c
 * Signature: ([I[F[F[F[I[I)V
 */
  JNIEXPORT void JNICALL Java_visad_java3d_TrackdJNI_get_1trackd_1c
    (JNIEnv *env, jobject obj, jintArray number_of_sensors_j,
     jfloatArray sensor_positions_j, jfloatArray sensor_angles_j,
     jfloatArray sensor_matrices_j, jintArray number_of_buttons_j,
     jintArray button_states_j) {

     int ns, nb, is, ib, i, j, k;
     float pos[3];
     float mat[4][4];

     jint *number_of_sensors =
       (*env)->GetIntArrayElements(env, number_of_sensors_j, 0);
     jfloat *sensor_positions =
       (*env)->GetFloatArrayElements(env, sensor_positions_j, 0);
     jfloat *sensor_angles =
       (*env)->GetFloatArrayElements(env, sensor_angles_j, 0);
     jfloat *sensor_matrices =
       (*env)->GetFloatArrayElements(env, sensor_matrices_j, 0);
     jint *number_of_buttons =
       (*env)->GetIntArrayElements(env, number_of_buttons_j, 0);
     jint *button_states =
       (*env)->GetIntArrayElements(env, button_states_j, 0);

     ns = trackdGetNumberOfSensors(tracker);
     if (ns > number_of_sensors[0]) ns = number_of_sensors[0];
     number_of_sensors[0] = ns;
     for (is=0; is<ns; is++) {
       trackdGetPosition(tracker, is, pos);
       sensor_positions[3*is] = pos[0];
       sensor_positions[3*is+1] = pos[1];
       sensor_positions[3*is+2] = pos[2];
       trackdGetEulerAngles(tracker, is, pos);
       sensor_angles[3*is] = pos[0];
       sensor_angles[3*is+1] = pos[1];
       sensor_angles[3*is+2] = pos[2];
       trackdGetMatrix(tracker, is, mat);
       k = 0;
       for (i=0; i<4; i++) {
         for (j=0; j<4; j++) {
           sensor_matrices[16*is+k] = mat[i][j];
           k++;
         }
       }
     }

     nb = trackdGetNumberOfButtons(controller);
     if (nb > number_of_buttons[0]) nb = number_of_buttons[0];
     number_of_buttons[0] = nb;
     for (ib=0; ib<nb; ib++) {
       button_states[ib] = trackdGetButton(controller, ib);
     }
     (*env)->ReleaseIntArrayElements(env, number_of_sensors_j, number_of_sensors, 0);
     (*env)->ReleaseFloatArrayElements(env, sensor_positions_j, sensor_positions, 0);
     (*env)->ReleaseFloatArrayElements(env, sensor_angles_j, sensor_angles, 0);
     (*env)->ReleaseFloatArrayElements(env, sensor_matrices_j, sensor_matrices, 0);
     (*env)->ReleaseIntArrayElements(env, number_of_buttons_j, number_of_buttons, 0);
     (*env)->ReleaseIntArrayElements(env, button_states_j, button_states, 0);
  }


/*
 * Class:     visad_java3d_TrackdJNI
 * Method:    init_trackd_c
 * Signature: (II[I)V
 */
  JNIEXPORT void JNICALL Java_visad_java3d_TrackdJNI_init_1trackd_1c
    (JNIEnv *env, jobject obj, jint tracker_shmkey,
     jint controller_shmkey, jintArray status_j) {

    jint *status = (*env)->GetIntArrayElements(env, status_j, 0);
    tracker = (void *) trackdInitTrackerReader(tracker_shmkey);
    controller = (void *) trackdInitControllerReader(controller_shmkey);
    status[0] = 0;
    if (tracker == NULL) status[0] += 1;
    if (controller == NULL) status[0] += 2;
    (*env)->ReleaseIntArrayElements(env, status_j, status, 0);
  }

