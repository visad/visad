#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_GDattach
( JNIEnv *env, jobject obj, jint file_id, jstring grid_name )  {

  char *f_name;
  int32 grid_id;

     f_name = (char *) (*env)->GetStringUTFChars( env, grid_name, 0);

     grid_id = GDattach( (int32)file_id, (char *)f_name );

     (*env)->ReleaseStringUTFChars(env, grid_name, f_name );

   return grid_id; 

  }
