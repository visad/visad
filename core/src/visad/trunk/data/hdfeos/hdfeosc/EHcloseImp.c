#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_EHclose
( JNIEnv *env, 
  jclass class, 
  jint file_id
               ) 
{ 

  int32 status;

  status = EHclose( (int32)file_id );

  return status;

 }
