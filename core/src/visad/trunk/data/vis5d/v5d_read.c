/*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

/* v5d_read.c */

#include <jni.h>
#include "visad_data_vis5d_Vis5DForm.h"
#include "binio.h"
#include "v5d.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

int maxNl;
v5dstruct v;

/*
 * Class:     visad_data_vis5d_Vis5DForm
 * Method:    v5d_open
 * Signature: ([BI[I[B[F)V
 */
  JNIEXPORT void JNICALL Java_visad_data_vis5d_Vis5DForm_v5d_1open
    (JNIEnv *env, jobject obj, jbyteArray name_j, jint name_length,
     jintArray sizes_j, jbyteArray varnames_j, jfloatArray times_j) {

    int i, j, k;
    int day, time, first_day, first_time;
    char filename[200];
    v5dstruct *status;

    jbyte *name = (*env)->GetByteArrayElements(env, name_j, 0);
    jint *sizes = (*env)->GetIntArrayElements(env, sizes_j, 0);
    jbyte *varnames = (*env)->GetByteArrayElements(env, varnames_j, 0);
    jfloat *times = (*env)->GetFloatArrayElements(env, times_j, 0);

    /* open file */
    for (i=0; i<name_length; i++) {
      filename[i] = name[i];
    }
    filename[name_length] = 0;
    status = v5dOpenFile(filename, &v);   

    if (status != NULL) {
      /* get basic sizes */
      sizes[0] = v.Nr;
      sizes[1] = v.Nc;
      sizes[3] = v.NumTimes;
      sizes[4] = v.NumVars;
  
      /* compute maximum level */
      /* actually, make sure all levels are equal */
      maxNl = v.Nl[0];
      for (i=0; i<v.NumVars; i++) {
        /* if (v.Nl[i] > maxNl) maxNl = v.Nl[i]; */
        if (v.Nl[i] != maxNl) sizes[0] = -1;
      }
      sizes[2] = maxNl;
  
      /* compute varnames */
      for (j=0; j<v.NumVars; j++) {
        k = 10 * j;
        for (i=0; i<10; i++) {
          if (v.VarName[j][i] != 0 && i<9) {
            varnames[k + i] = v.VarName[j][i];
          }
          else {
            varnames[k + i] = 0;
            break;
          }
        }
      }
  
      /* compute times */
      first_day = v5dYYDDDtoDays(v.DateStamp[0]);
      first_time = v5dHHMMSStoSeconds(v.TimeStamp[0]);
      for (i=0; i<v.NumTimes; i++) {
        day = v5dYYDDDtoDays(v.DateStamp[i]);
        time = v5dHHMMSStoSeconds(v.TimeStamp[i]);
        times[i] = (day - first_day) * 24*60*60 + (time - first_time);
      }
    }
    else { /* status == null */
      sizes[0] = -1;
    }

    (*env)->ReleaseByteArrayElements(env, name_j, name, 0);
    (*env)->ReleaseIntArrayElements(env, sizes_j, sizes, 0);
    (*env)->ReleaseByteArrayElements(env, varnames_j, varnames, 0);
    (*env)->ReleaseFloatArrayElements(env, times_j, times, 0);
  }
 
/*
 * Class:     visad_data_vis5d_Vis5DForm
 * Method:    v5d_read
 * Signature: (II[F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_data_vis5d_Vis5DForm_v5d_1read
    (JNIEnv *env, jobject obj, jint time, jint var, jfloatArray ranges_j,
     jfloatArray data_j) {

    int status;

    jfloat *ranges = (*env)->GetFloatArrayElements(env, ranges_j, 0);
    jfloat *data = (*env)->GetFloatArrayElements(env, data_j, 0);

    ranges[0] = v.MinVal[var];
    ranges[1] = v.MaxVal[var];
    status = v5dReadGrid(&v, time, var, data);
    if (status == 0) {
      ranges[0] = 1.0;
      ranges[1] = -1.0;
    }

    (*env)->ReleaseFloatArrayElements(env, ranges_j, ranges, 0);
    (*env)->ReleaseFloatArrayElements(env, data_j, data, 0);
  }

