#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_GDinqgrid
( JNIEnv *env, jobject obj, jstring filename, jstring name_list )  {

  int32 n_grids;
  char *grid_names;
  int32 strbufsize;
  char *f_name;
  jstring j_new;

     f_name = (char *) (*env)->GetStringUTFChars( env, filename, 0);

     n_grids = GDinqgrid( (char *)f_name, NULL, (int32 *)&strbufsize );

     grid_names = (char *)malloc((size_t)strbufsize+1);

     n_grids = GDinqgrid( (char *)f_name, (char *)grid_names, (int32 *)&strbufsize );

     grid_names[ strbufsize ] = '\0';

     j_new = (*env)->NewStringUTF( env, grid_names );

     free( grid_names );

    (*env)->SetObjectArrayElement(env, name_list,0,(jobject)j_new );

    (*env)->ReleaseStringUTFChars(env, filename, f_name );


   return n_grids;

  }
