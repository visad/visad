/*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDfieldinfo
(JNIEnv *env,
 jclass class,
 jint grid_id,
 jstring fieldname,
 jstring D_List,
 jintArray rank,
 jintArray lengths,
 jintArray type  )  {

  int32  status;
  jint *j_rank;
  jint *j_type;
  jint *j_lengths;
  jboolean bb;
  jstring j_new;
  char c_array[1024];
  char *f_name;
  char *name;
  int32 ii;
  int32 len = 0;


     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);

     j_rank = (jint *) (*env)->GetIntArrayElements( env, rank, &bb);
     j_type = (jint *) (*env)->GetIntArrayElements( env, type, &bb);
     j_lengths = (jint *) (*env)->GetIntArrayElements( env, lengths, &bb);

     status = GDfieldinfo( (int32)grid_id, (char *)f_name, (int32 *)j_rank,
                           (int32 *)j_lengths, (int32 *)j_type, (char *)c_array );


       j_new = (*env)->NewStringUTF(env, c_array );

       (*env)->SetObjectArrayElement(env, D_List, 0, (jobject)j_new);

       (*env)->ReleaseIntArrayElements( env, rank, j_rank, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, type, j_type, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, lengths, j_lengths, JNI_COMMIT);

       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );

   return (jint) status;
  }
