#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_SDattrinfo
( JNIEnv *env, 
  jclass class, 
  jint sd_id, 
  jstring sds_name,
  jstring attr_name
                     ) 
{

  int32 sds_id;
  int32 sds_idx;
  int32 status;
  int32 attr_idx;
  char *a_name;
  char *s_name;
  void *data;
  int32 n_type[1];
  int32 count[1];

  jdouble *j_attr;
  jboolean bb;

     a_name = (char *) (*env)->GetStringUTFChars( env, attr_name, 0);
     s_name = (char *) (*env)->GetStringUTFChars( env, sds_name, 0);

     sds_idx = SDnametoindex( (int32)sd_id, s_name );
       if ( sds_idx < 0 ) {
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


     (*env)->ReleaseStringUTFChars( env, attr_name, a_name );
     (*env)->ReleaseStringUTFChars( env, sds_name, s_name );


   return ( count[0] ); 
}
