#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_GDfieldinfo
(JNIEnv *env, 
 jobject obj, 
 jint grid_id, 
 jstring filename,
 jint strbufsize,
 jstring D_List, 
 jintArray rank, 
 jintArray lengths,
 jintArray type  )  {

  int32  status;
  jint *j_rank;
  jint *j_type;
  jint *j_lengths;
  jboolean bb;
  jstring j_new;
  char *c_ptr;
  char *f_name;
  char *name;
  int32 ii;


     f_name = (char *) (*env)->GetStringUTFChars( env, filename, 0);
     c_ptr = (char *)malloc((size_t)strbufsize+1);
     
     j_rank = (jint *) (*env)->GetIntArrayElements( env, rank, &bb); 
     j_type = (jint *) (*env)->GetIntArrayElements( env, type, &bb); 
     j_lengths = (jint *) (*env)->GetIntArrayElements( env, lengths, &bb); 

     status = GDfieldinfo( (int32)grid_id, (char *)f_name, (int32 *)j_rank,
                           (int32 *)j_lengths, (int32 *)j_type, (char *)c_ptr );

       j_new = (*env)->NewStringUTF(env, c_ptr );
       free( c_ptr );
      
       (*env)->SetObjectArrayElement(env, D_List, 0, (jobject)j_new);

       (*env)->ReleaseIntArrayElements( env, rank, j_rank, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, type, j_type, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, lengths, j_lengths, JNI_COMMIT); 

       (*env)->ReleaseStringUTFChars( env, filename, f_name );

   return (jint) status;
  }
