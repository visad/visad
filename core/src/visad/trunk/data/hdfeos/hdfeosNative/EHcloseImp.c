#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_EHclose
( JNIEnv *env, 
  jobject obj, 
  jint file_id
               ) 
{ 

  int32 status;

  status = EHclose( (int32)file_id );

  return status;

 }
