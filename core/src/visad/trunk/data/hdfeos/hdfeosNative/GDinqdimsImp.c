#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_GDinqdims
( JNIEnv *env, 
  jobject obj, 
  jint grid_id, 
  jint strbufsize,
  jstring dimList, 
  jintArray dimLengths ) {

  int32  n_dims;
  jint *j_lengths;
  jboolean bb;
  jstring j_new;
  char *c_ptr;
  char *attr_list;
  char *bufalloc;

     c_ptr = (char *)malloc((size_t)strbufsize+1);
     
     j_lengths = (jint *) (*env)->GetIntArrayElements( env, dimLengths, &bb );

     n_dims = GDinqdims( (int32)grid_id, (char *)c_ptr, (int32 *)j_lengths );

       j_new = (*env)->NewStringUTF(env, c_ptr );
       free( c_ptr );
       
       (*env)->SetObjectArrayElement(env, dimList, 0, (jobject)j_new);
       (*env)->ReleaseIntArrayElements( env, dimLengths, j_lengths, JNI_COMMIT); 

   return (jint) n_dims;
  }
