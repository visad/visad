#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_GDgridinfo
(JNIEnv *env, 
 jobject obj, 
 jint grid_id, 
 jintArray xdimsize,
 jintArray ydimsize, 
 jdoubleArray upleftpt, 
 jdoubleArray lowrightpt )  {

  int32  stat;
  jint *j_xsiz;
  jint *j_ysiz;
  jdouble *j_uprR;
  jdouble *j_lwrL;
  jboolean bb;

     j_xsiz = (jint *) (*env)->GetIntArrayElements( env, xdimsize, &bb );
     j_ysiz = (jint *) (*env)->GetIntArrayElements( env, ydimsize, &bb );
     j_uprR = (jdouble *) (*env)->GetDoubleArrayElements( env, upleftpt, &bb );
     j_lwrL = (jdouble *) (*env)->GetDoubleArrayElements( env, lowrightpt, &bb );
     
     stat = GDgridinfo( (int32)grid_id, (int32 *)j_xsiz, (int32 *)j_ysiz, 
                                        (double *)j_uprR, (double *)j_lwrL );

       
       (*env)->ReleaseIntArrayElements( env, xdimsize, j_xsiz, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, ydimsize, j_ysiz, JNI_COMMIT); 
       (*env)->ReleaseDoubleArrayElements( env, upleftpt, j_uprR, JNI_COMMIT); 
       (*env)->ReleaseDoubleArrayElements( env, lowrightpt, j_lwrL, JNI_COMMIT); 

   return (jint) stat;
  }
