#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_GDprojinfo
(JNIEnv *env, 
 jobject obj, 
 jint grid_id, 
 jintArray projcode,
 jintArray zonecode, 
 jintArray spherecode, 
 jdoubleArray projparms  )  {

  int32  stat;
  jint *j_proj;
  jint *j_zone;
  jint *j_sphr;
  jdouble *j_parm;
  jboolean bb;

     j_proj = (jint *) (*env)->GetIntArrayElements( env, projcode, &bb );
     j_zone = (jint *) (*env)->GetIntArrayElements( env, zonecode, &bb );
     j_sphr = (jint *) (*env)->GetIntArrayElements( env, spherecode, &bb );
     j_parm = (jdouble *) (*env)->GetDoubleArrayElements( env, projparms, &bb );
     
     stat = GDprojinfo( (int32)grid_id, (int32 *)j_proj, (int32 *)j_zone, 
                                        (int32 *)j_sphr, (double *)j_parm );

       
       (*env)->ReleaseIntArrayElements( env, projcode, j_proj, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, zonecode, j_zone, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, spherecode, j_sphr, JNI_COMMIT); 
       (*env)->ReleaseDoubleArrayElements( env, projparms, j_parm, JNI_COMMIT); 

   return (jint) stat;
  }
