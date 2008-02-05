 * /*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDgridinfo
(JNIEnv *env,
 jclass class,
 jint grid_id,
 jintArray xdimsize,
 jintArray ydimsize,
 jdoubleArray upleftpt,
 jdoubleArray lowrightpt )  {

  int32  stat;
  jint *j_xsiz;
  jint *j_ysiz;
  jdouble *j_uprR;
  jdouble *j_lwrL;
  jboolean bb;

     j_xsiz = (jint *) (*env)->GetIntArrayElements( env, xdimsize, &bb );
     j_ysiz = (jint *) (*env)->GetIntArrayElements( env, ydimsize, &bb );
     j_uprR = (jdouble *) (*env)->GetDoubleArrayElements( env, upleftpt, &bb );
     j_lwrL = (jdouble *) (*env)->GetDoubleArrayElements( env, lowrightpt, &bb );

     stat = GDgridinfo( (int32)grid_id, (int32 *)j_xsiz, (int32 *)j_ysiz,
                                        (double *)j_uprR, (double *)j_lwrL );


       (*env)->ReleaseIntArrayElements( env, xdimsize, j_xsiz, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, ydimsize, j_ysiz, JNI_COMMIT);
       (*env)->ReleaseDoubleArrayElements( env, upleftpt, j_uprR, JNI_COMMIT);
       (*env)->ReleaseDoubleArrayElements( env, lowrightpt, j_lwrL, JNI_COMMIT);

   return (jint) stat;
  }
