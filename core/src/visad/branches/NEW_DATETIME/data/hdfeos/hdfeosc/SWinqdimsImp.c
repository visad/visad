#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_SWinqdims
( JNIEnv *env, 
  jclass class, 
  jint swath_id, 
  jint strbufsize,
  jstring dimList, 
  jintArray dimLengths ) {

  int32  n_dims;
  jint *j_lengths;
  jboolean bb;
  jstring j_new;
  char *c_ptr;
  char *swath_name;
  char *attr_list;
  char *bufalloc;
  char *Dfield_list;
  char *Gfield_list;

     c_ptr = (char *)malloc((size_t)strbufsize+1);
     
     j_lengths = (jint *) (*env)->GetIntArrayElements( env, dimLengths, &bb );

     n_dims = SWinqdims( (int32)swath_id, (char *)c_ptr, (int32 *)j_lengths );

       j_new = (*env)->NewStringUTF(env, c_ptr );
       free( c_ptr );
       
       (*env)->SetObjectArrayElement(env, dimList, 0, (jobject)j_new);
       (*env)->ReleaseIntArrayElements( env, dimLengths, j_lengths, JNI_COMMIT); 

   return (jint) n_dims;
  }
