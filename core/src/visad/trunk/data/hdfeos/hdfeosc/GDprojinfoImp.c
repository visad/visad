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

#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDprojinfo
(JNIEnv *env,
 jclass class,
 jint grid_id,
 jintArray projcode,
 jintArray zonecode,
 jintArray spherecode,
 jdoubleArray projparms  )  {

  int32  stat;
  jint *j_proj;
  jint *j_zone;
  jint *j_sphr;
  jdouble *j_parm;
  jboolean bb;

     j_proj = (jint *) (*env)->GetIntArrayElements( env, projcode, &bb );
     j_zone = (jint *) (*env)->GetIntArrayElements( env, zonecode, &bb );
     j_sphr = (jint *) (*env)->GetIntArrayElements( env, spherecode, &bb );
     j_parm = (jdouble *) (*env)->GetDoubleArrayElements( env, projparms, &bb );

     stat = GDprojinfo( (int32)grid_id, (int32 *)j_proj, (int32 *)j_zone,
                                        (int32 *)j_sphr, (double *)j_parm );


       (*env)->ReleaseIntArrayElements( env, projcode, j_proj, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, zonecode, j_zone, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, spherecode, j_sphr, JNI_COMMIT);
       (*env)->ReleaseDoubleArrayElements( env, projparms, j_parm, JNI_COMMIT);

   return (jint) stat;
  }
