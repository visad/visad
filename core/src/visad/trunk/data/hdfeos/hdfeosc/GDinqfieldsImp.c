#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDinqfields
(JNIEnv *env, 
 jobject obj, 
 jint grid_id, 
 jint strbufsize,
 jstring F_List, 
 jintArray F_ranks, 
 jintArray F_types  )  {

  int32  n_fields;
  jint *bodyA;
  jint *bodyB;
  jboolean bb;
  jstring j_new;
  char *c_ptr;
  char *attr_list;
  char *bufalloc;
  char *Dfield_list;
  char *Gfield_list;
  char *name;
  int32 *ranks;
  int32 rank;
  int32 type;
  int32 *types;
  int32 ii;


     c_ptr = (char *)malloc((size_t)strbufsize+1);
     
     bodyA = (jint *) (*env)->GetIntArrayElements( env, F_ranks, &bb); 
     bodyB = (jint *) (*env)->GetIntArrayElements( env, F_types, &bb); 

     n_fields = GDinqfields( (int32)grid_id, (char *)c_ptr, (int32 *)bodyA, (int32 *)bodyB );

       j_new = (*env)->NewStringUTF(env, c_ptr );
       free( c_ptr );
      
       (*env)->SetObjectArrayElement(env, F_List, 0, (jobject)j_new);
       (*env)->ReleaseIntArrayElements( env, F_ranks, bodyA, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, F_types, bodyB, JNI_COMMIT); 

   return (jint) n_fields;
  }
