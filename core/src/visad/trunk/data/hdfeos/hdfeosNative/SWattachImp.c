#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_SWattach
( JNIEnv *env, jobject obj, jint file_id, jstring swath_name )  {

  char *f_name;
  int32 swath_id;

     f_name = (char *) (*env)->GetStringUTFChars( env, swath_name, 0);

     swath_id = SWattach( (int32)file_id, (char *)f_name );

     (*env)->ReleaseStringUTFChars(env, swath_name, f_name );

   return swath_id; 

  }
