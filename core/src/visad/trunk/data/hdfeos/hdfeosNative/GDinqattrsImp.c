#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_GDinqattrs
( JNIEnv *env, 
  jobject obj, 
  jint grid_id,
  jstring attr_list 
                    ) 
{

  int32 n_attrs;
  char *grid_names;
  int32 strbufsize;
  jstring j_new;


    n_attrs = GDinqattrs( (int32)grid_id, NULL, (int32 *)&strbufsize );

    grid_names = (char *)malloc((size_t)strbufsize+1);

    n_attrs = GDinqattrs( (int32)grid_id, (char *)grid_names, (int32 *)&strbufsize );

    grid_names[ strbufsize ] = '\0';

    j_new = (*env)->NewStringUTF( env, grid_names );

    free( grid_names );

    (*env)->SetObjectArrayElement(env, attr_list, 0, (jobject)j_new );


   return n_attrs;

  }
