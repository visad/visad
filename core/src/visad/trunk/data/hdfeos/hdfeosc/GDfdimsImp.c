#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDfdims
( JNIEnv *env, 
  jobject obj, 
  jint grid_id, 
  jstring fieldname,
  jintArray strbufsize ) {

  int32  size;
  int32  n_dims;
  char *f_name;
  jint *body;
  jboolean bb;

     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);

     n_dims = GDfdims( (int32)grid_id, (char *)f_name, (int32 *)&size );

       body = (jint *) (*env)->GetIntArrayElements( env, strbufsize, &bb); 
       body[0] = size;
      
       (*env)->ReleaseIntArrayElements( env, strbufsize, body, JNI_COMMIT); 
       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );

   return (jint) n_dims;
  }
