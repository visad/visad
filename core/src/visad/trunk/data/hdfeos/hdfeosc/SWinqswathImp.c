#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_SWinqswath
  ( JNIEnv *env, jobject obj, jstring filename, jstring name_list )  {

  int32 n_swaths;
  char *swath_names;
  int32 strbufsize;
  char *f_name;
  jstring j_new;

     f_name = (char *) (*env)->GetStringUTFChars( env, filename, 0);

     n_swaths = SWinqswath( (char *)f_name, NULL, (int32 *)&strbufsize );

     swath_names = (char *)malloc((size_t)strbufsize+1);

     n_swaths = SWinqswath( (char *)f_name, (char *)swath_names, (int32 *)&strbufsize );

     swath_names[ strbufsize ] = '\0';

     j_new = (*env)->NewStringUTF( env, swath_names );

     free( swath_names );

    (*env)->SetObjectArrayElement(env, name_list,0,(jobject)j_new );

    (*env)->ReleaseStringUTFChars(env, filename, f_name );


   return n_swaths;

  }
