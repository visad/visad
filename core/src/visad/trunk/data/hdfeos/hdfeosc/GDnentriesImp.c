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
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDnentries
( JNIEnv *env,
  jclass class,
  jint grid_id,
  jint HDFE_mode,
  jintArray strbufsize ) {

  int32  size;
  int32  n_entries;
  jint *body;
  jboolean bb;

     n_entries = GDnentries( (int32)grid_id, (int32)HDFE_mode, (int32 *)&size );

       body = (jint *) (*env)->GetIntArrayElements( env, strbufsize, &bb);
       body[0] = size;

       (*env)->ReleaseIntArrayElements( env, strbufsize, body, JNI_COMMIT);

   return (jint) n_entries;
  }
