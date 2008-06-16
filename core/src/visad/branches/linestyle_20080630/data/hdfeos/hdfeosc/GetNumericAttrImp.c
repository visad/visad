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
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GetNumericAttr
( JNIEnv *env,
  jclass class,
  jint sd_id,
  jstring sds_name,
  jstring attr_name,
  jdoubleArray attr
                     )
{

  int32 sds_id;
  int32 sds_idx;
  int32 status;
  int32 attr_idx;
  char *a_name;
  char *s_name;
  void *data;
  float *f_data;
  double *d_data;
  int32 *i_data;
  int *l_data;
  int ii;
  short *s_data;
  int32 n_type[1];
  int32 count[1];

  jdouble *j_attr;
  jboolean bb;

     a_name = (char *) (*env)->GetStringUTFChars( env, attr_name, 0);
     s_name = (char *) (*env)->GetStringUTFChars( env, sds_name, 0);
     j_attr = (jdouble *) (*env)->GetDoubleArrayElements( env, attr, &bb );

     sds_idx = SDnametoindex( (int32)sd_id, s_name );
       if ( sds_idx < 0 )
       {
          return ( -4 );
       }
     sds_id = SDselect( (int32)sd_id, (int32)sds_idx );
       if ( sds_id < 0 ) {
         return ( -1 );
       }
     attr_idx = SDfindattr( (int32)sds_id, (char *)a_name );
       if ( attr_idx < 0 ) {
         return ( -2 );
       }
     status = SDattrinfo( (int32)sds_id, (int32)attr_idx, (char *)a_name, (int32 *)n_type, (int32 *)count );
       if ( status < 0 ) {
         return ( -3 );
       }


     if ( n_type[0] == 5 ) {
        f_data = ( float *) malloc( count[0]*sizeof( float ) );
        status = SDreadattr( sds_id, attr_idx, (void *)f_data );
        for ( ii = 0; ii < count[0]; ii++ ) {
          j_attr[ii] = (jdouble) f_data[ii];
        }
        free( f_data );
     }
     else if ( n_type[0] == 6 ) {
        d_data = ( double *) malloc( count[0]*sizeof( double ) );
        status = SDreadattr( sds_id, attr_idx, (void *)d_data );
        for ( ii = 0; ii < count[0]; ii++ ) {
          j_attr[ii] = (jdouble) d_data[ii];
        }
        free( d_data );
     }
     else if (( n_type[0] == 22) || ( n_type[0] == 23)) {
        s_data = ( short *) malloc( count[0]*sizeof( short ) );
        status = SDreadattr( sds_id, attr_idx, (void *)s_data );
        for ( ii = 0; ii < count[0]; ii++ ) {
          j_attr[ii] = (jdouble) s_data[ii];
        }
        free( s_data );
     }
     else if (( n_type[0] == 24) || ( n_type[0] == 25 )) {
        i_data = ( int32 *) malloc( count[0]*sizeof( int32 ) );
        status = SDreadattr( sds_id, attr_idx, (void *)i_data );
        for ( ii = 0; ii < count[0]; ii++ ) {
          j_attr[ii] = (jdouble) i_data[ii];
        }
        free( i_data );
     }
     else if (( n_type[0] == 26) || (n_type[0] == 27 )) {
        l_data = ( int *) malloc( count[0]*sizeof( int ) );
        status = SDreadattr( sds_id, attr_idx, (void *)l_data );
        for ( ii = 0; ii < count[0]; ii++ ) {
          j_attr[0] = (jdouble) l_data[0];
        }
        free( l_data );
     }
     else {
        return (-7);
     }


     (*env)->ReleaseDoubleArrayElements( env, attr, j_attr, JNI_COMMIT);

     (*env)->ReleaseStringUTFChars( env, attr_name, a_name );
     (*env)->ReleaseStringUTFChars( env, sds_name, s_name );


   return (status);
}
