#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDfieldinfo
(JNIEnv *env, 
 jobject obj, 
 jint grid_id, 
 jstring fieldname,
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
  char c_array[1024];
  char *f_name;
  char *name;
  int32 ii;
  int32 len = 0;

      
     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);
     
     j_rank = (jint *) (*env)->GetIntArrayElements( env, rank, &bb); 
     j_type = (jint *) (*env)->GetIntArrayElements( env, type, &bb); 
     j_lengths = (jint *) (*env)->GetIntArrayElements( env, lengths, &bb); 

     status = GDfieldinfo( (int32)grid_id, (char *)f_name, (int32 *)j_rank,
                           (int32 *)j_lengths, (int32 *)j_type, (char *)c_array );

       
       j_new = (*env)->NewStringUTF(env, c_array );

       (*env)->SetObjectArrayElement(env, D_List, 0, (jobject)j_new);

       (*env)->ReleaseIntArrayElements( env, rank, j_rank, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, type, j_type, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, lengths, j_lengths, JNI_COMMIT); 

       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );

   return (jint) status;
  }
