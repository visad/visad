#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_SWfdims
( JNIEnv *env, 
  jclass class, 
  jint swath_id, 
  jstring fieldtype,
  jstring fieldname,
  jintArray strbufsize ) 
{

  int32  size;
  int32  n_dims;
  char *f_name;
  char *f_type;
  jint *body;
  jboolean bb;

     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);
     f_type = (char *) (*env)->GetStringUTFChars( env, fieldtype, 0);

     n_dims = SWfdims( (int32)swath_id, (char *)f_type, (char *)f_name, (int32 *)&size );

       body = (jint *) (*env)->GetIntArrayElements( env, strbufsize, &bb); 
       body[0] = size;
      
       (*env)->ReleaseIntArrayElements( env, strbufsize, body, JNI_COMMIT); 
       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );
       (*env)->ReleaseStringUTFChars( env, fieldtype, f_type );

   return (jint) n_dims;
}
