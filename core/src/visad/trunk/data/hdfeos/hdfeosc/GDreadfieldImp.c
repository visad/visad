#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDreadfield__ILjava_lang_String_2_3I_3I_3I_3F
(JNIEnv *env, 
 jobject obj, 
 jint swath_id, 
 jstring fieldname, 
 jintArray start, 
 jintArray stride, 
 jintArray edge, 
 jfloatArray data )  {

  int32  status;
  jint *j_start;
  jint *j_stride;
  jint *j_edge;
  jfloat *j_data;
  jboolean bb;
  char *f_name;

     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);
     
     j_start = (jint *) (*env)->GetIntArrayElements( env, start, &bb); 
     j_stride = (jint *) (*env)->GetIntArrayElements( env, stride, &bb); 
     j_edge = (jint *) (*env)->GetIntArrayElements( env, edge, &bb); 
     j_data = (jfloat *) (*env)->GetFloatArrayElements( env, data, &bb);

     status = GDreadfield( (int32)swath_id, (char *)f_name, (int32 *)j_start,
                           (int32 *)j_stride, (int32 *)j_edge, (float *)j_data );


       (*env)->ReleaseIntArrayElements( env, start, j_start, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, stride, j_stride, JNI_COMMIT); 
       (*env)->ReleaseIntArrayElements( env, edge, j_edge, JNI_COMMIT); 
       (*env)->ReleaseFloatArrayElements( env, data, j_data, JNI_COMMIT); 

       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );

   return (jint) status;
  }

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDreadfield__ILjava_lang_String_2_3I_3I_3I_3D
(JNIEnv *env,
 jobject obj,
 jint swath_id,
 jstring fieldname,
 jintArray start,
 jintArray stride,
 jintArray edge,
 jdoubleArray data )  {


  int32  status;
  jint *j_start;
  jint *j_stride;
  jint *j_edge;
  jdouble *j_data;
  jboolean bb;
  char *f_name;

     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);

     j_start = (jint *) (*env)->GetIntArrayElements( env, start, &bb);
     j_stride = (jint *) (*env)->GetIntArrayElements( env, stride, &bb);
     j_edge = (jint *) (*env)->GetIntArrayElements( env, edge, &bb);
     j_data = (jdouble *) (*env)->GetDoubleArrayElements( env, data, &bb);

     status = GDreadfield( (int32)swath_id, (char *)f_name, (int32 *)j_start,
                           (int32 *)j_stride, (int32 *)j_edge, (double *)j_data );


       (*env)->ReleaseIntArrayElements( env, start, j_start, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, stride, j_stride, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, edge, j_edge, JNI_COMMIT);
       (*env)->ReleaseDoubleArrayElements( env, data, j_data, JNI_COMMIT);

       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );

   return (jint) status;
  }

JNIEXPORT jint JNICALL
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDreadfield__ILjava_lang_String_2_3I_3I_3I_3I
(JNIEnv *env,
 jobject obj,
 jint swath_id,
 jstring fieldname,
 jintArray start,
 jintArray stride,
 jintArray edge,
 jintArray data )  {


  int32  status;
  jint *j_start;
  jint *j_stride;
  jint *j_edge;
  jint *j_data;
  jboolean bb;
  char *f_name;

     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);

     j_start = (jint *) (*env)->GetIntArrayElements( env, start, &bb);
     j_stride = (jint *) (*env)->GetIntArrayElements( env, stride, &bb);
     j_edge = (jint *) (*env)->GetIntArrayElements( env, edge, &bb);
     j_data = (jint *) (*env)->GetIntArrayElements( env, data, &bb);

     status = GDreadfield( (int32)swath_id, (char *)f_name, (int32 *)j_start,
                           (int32 *)j_stride, (int32 *)j_edge, (int *)j_data );


       (*env)->ReleaseIntArrayElements( env, start, j_start, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, stride, j_stride, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, edge, j_edge, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, data, j_data, JNI_COMMIT);

       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );

   return (jint) status;
  }

JNIEXPORT jint JNICALL
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDreadfield__ILjava_lang_String_2_3I_3I_3I_3S
(JNIEnv *env,
 jobject obj,
 jint swath_id,
 jstring fieldname,
 jintArray start,
 jintArray stride,
 jintArray edge,
 jshortArray data )  {

  int32  status;
  jint *j_start;
  jint *j_stride;
  jint *j_edge;
  jshort *j_data;
  jboolean bb;
  char *f_name;

     f_name = (char *) (*env)->GetStringUTFChars( env, fieldname, 0);

     j_start = (jint *) (*env)->GetIntArrayElements( env, start, &bb);
     j_stride = (jint *) (*env)->GetIntArrayElements( env, stride, &bb);
     j_edge = (jint *) (*env)->GetIntArrayElements( env, edge, &bb);
     j_data = (jshort *) (*env)->GetShortArrayElements( env, data, &bb);

     status = GDreadfield( (int32)swath_id, (char *)f_name, (int32 *)j_start,
                           (int32 *)j_stride, (int32 *)j_edge, (short *)j_data );


       (*env)->ReleaseIntArrayElements( env, start, j_start, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, stride, j_stride, JNI_COMMIT);
       (*env)->ReleaseIntArrayElements( env, edge, j_edge, JNI_COMMIT);
       (*env)->ReleaseShortArrayElements( env, data, j_data, JNI_COMMIT);

       (*env)->ReleaseStringUTFChars( env, fieldname, f_name );

   return (jint) status;
}
