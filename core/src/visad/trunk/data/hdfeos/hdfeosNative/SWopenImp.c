#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_SWopen
  ( JNIEnv *env, jobject obj, jstring filename, jint access )  {

  int32 file_id;
  char *f_name;

     f_name = (char *) (*env)->GetStringUTFChars( env, filename, 0);

     file_id = SWopen( (char *)f_name, (int32)access );

     (*env)->ReleaseStringUTFChars(env, filename, f_name );


   return file_id;

  }
