#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDnentries
( JNIEnv *env, 
  jclass class, 
  jint grid_id, 
  jint HDFE_mode, 
  jintArray strbufsize ) {

  int32  size;
  int32  n_entries;
  jint *body;
  jboolean bb;

     n_entries = GDnentries( (int32)grid_id, (int32)HDFE_mode, (int32 *)&size );

       body = (jint *) (*env)->GetIntArrayElements( env, strbufsize, &bb); 
       body[0] = size;
      
       (*env)->ReleaseIntArrayElements( env, strbufsize, body, JNI_COMMIT); 

   return (jint) n_entries;
  }
