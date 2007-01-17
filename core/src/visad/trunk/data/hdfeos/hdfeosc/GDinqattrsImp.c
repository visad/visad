 * /*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDinqattrs
( JNIEnv *env,
  jclass class,
  jint grid_id,
  jstring attr_list
                    )
{

  int32 n_attrs;
  char *grid_names;
  int32 strbufsize;
  jstring j_new;


    n_attrs = GDinqattrs( (int32)grid_id, NULL, (int32 *)&strbufsize );

    grid_names = (char *)malloc((size_t)strbufsize+1);

    n_attrs = GDinqattrs( (int32)grid_id, (char *)grid_names, (int32 *)&strbufsize );

    grid_names[ strbufsize ] = '\0';

    j_new = (*env)->NewStringUTF( env, grid_names );

    free( grid_names );

    (*env)->SetObjectArrayElement(env, attr_list, 0, (jobject)j_new );


   return n_attrs;

  }
