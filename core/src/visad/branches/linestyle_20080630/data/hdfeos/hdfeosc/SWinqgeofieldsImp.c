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
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_SWinqgeofields
(JNIEnv *env,
 jclass class,
 jint swath_id,
 jint strbufsize,
 jstring F_List,
 jintArray F_ranks,
 jintArray F_types  )  {

  int32  *i_ptrA;
  int32  *i_ptrB;
  int32  n_fields;
  jint *bodyA;
  jint *bodyB;
  jboolean bb;
  jstring j_new;
  char *c_ptr;
  int32 *ranks;
  int32 rank;
  int32 type;
  int32 *types;
  int32 ii;

     c_ptr = (char *)malloc((size_t)strbufsize+1);

     bodyA = (jint *) (*env)->GetIntArrayElements( env, F_ranks, &bb);
     bodyB = (jint *) (*env)->GetIntArrayElements( env, F_types, &bb);

     n_fields = SWinqgeofields( (int32)swath_id, (char *)c_ptr, (int32 *)bodyA, (int32 *)bodyB );

       j_new = (*env)->NewStringUTF(env, c_ptr );
       free( c_ptr );

       (*env)->SetObjectArrayElement(env, F_List, 0, (jobject)j_new);
       (*env)->ReleaseIntArrayElements( env, F_ranks, bodyA, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, F_types, bodyB, JNI_COMMIT);

   return (jint) n_fields;
  }
