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
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_EHchkfid
( JNIEnv *env,
  jclass class,
  jint file_id,
  jstring struct_name,
  jintArray HDFfid,
  jintArray sdInterfaceId,
  jbyteArray access
                     )
{

  char *f_name;
  int32 stat;

  jint *j_fid;
  jint *j_sid;
  jbyte *j_acc;
  jboolean bb;

     j_fid = (jint *) (*env)->GetIntArrayElements( env, HDFfid, &bb );
     j_sid = (jint *) (*env)->GetIntArrayElements( env, sdInterfaceId, &bb );
     j_acc = (jbyte *) (*env)->GetByteArrayElements( env, access, &bb );


     f_name = (char *) (*env)->GetStringUTFChars( env, struct_name, 0);

     stat = EHchkfid( (int32)file_id, (char *)f_name, (int32 *)j_fid, (int32 *)j_sid, (uint8 *)j_acc );

     (*env)->ReleaseStringUTFChars(env, struct_name, f_name );
     (*env)->ReleaseIntArrayElements( env, HDFfid, j_fid, JNI_COMMIT);
     (*env)->ReleaseIntArrayElements( env, sdInterfaceId, j_sid, JNI_COMMIT);
     (*env)->ReleaseByteArrayElements( env, access, j_acc, JNI_COMMIT);


   return stat;
}
