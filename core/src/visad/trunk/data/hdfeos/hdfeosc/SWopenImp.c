#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_SWopen
  ( JNIEnv *env, jobject obj, jstring filename, jint access )  {

  int32 file_id;
  char *f_name;

     f_name = (char *) (*env)->GetStringUTFChars( env, filename, 0);

     file_id = SWopen( (char *)f_name, (int32)access );

     (*env)->ReleaseStringUTFChars(env, filename, f_name );


   return file_id;

  }
