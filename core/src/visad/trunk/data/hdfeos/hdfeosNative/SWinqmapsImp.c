#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_SWinqmaps
(JNIEnv *env, 
 jobject obj, 
 jint swath_id, 
 jint strbufsize,
 jstring M_List, 
 jintArray offsets, 
 jintArray increments  )  {

  int32  n_maps;
  jint *j_off;
  jint *j_inc;
  jboolean bb;
  jstring j_new;
  char *c_ptr;
  char *swath_name;
  char *name;
  int32 *ranks;
  int32 rank;
  int32 type;
  int32 *types;
  int32 ii;

     c_ptr = (char *)malloc((size_t)strbufsize+1);

     j_off = (jint *) (*env)->GetIntArrayElements( env, offsets, &bb );
     j_inc = (jint *) (*env)->GetIntArrayElements( env, increments, &bb );
     
     n_maps = SWinqmaps( (int32)swath_id, (char *)c_ptr, (int32 *)j_off, (int32 *)j_inc );

       j_new = (*env)->NewStringUTF(env, c_ptr );
       free( c_ptr );
       
       (*env)->SetObjectArrayElement(env, M_List, 0, (jobject)j_new);
       (*env)->ReleaseIntArrayElements( env, offsets, j_off, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, increments, j_inc, JNI_COMMIT); 

   return (jint) n_maps;
  }
