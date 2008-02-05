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
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_SWinqswath
( JNIEnv *env,
  jclass class,
  jstring filename,
  jstring name_list )
{

  int32 n_swaths;
  char *swath_names;
  int32 strbufsize;
  char *f_name;
  jstring j_new;

     f_name = (char *) (*env)->GetStringUTFChars( env, filename, 0);

     n_swaths = SWinqswath( (char *)f_name, NULL, (int32 *)&strbufsize );

     swath_names = (char *)malloc((size_t)strbufsize+1);

     n_swaths = SWinqswath( (char *)f_name, (char *)swath_names, (int32 *)&strbufsize );

     swath_names[ strbufsize ] = '\0';

     j_new = (*env)->NewStringUTF( env, swath_names );

     free( swath_names );

    (*env)->SetObjectArrayElement(env, name_list,0,(jobject)j_new );

    (*env)->ReleaseStringUTFChars(env, filename, f_name );


   return n_swaths;

}
