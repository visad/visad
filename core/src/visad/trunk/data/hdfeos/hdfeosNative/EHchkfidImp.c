#include <jni.h>
#include "hdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosNative_hdfeosLib_EHchkfid
( JNIEnv *env, 
  jobject obj, 
  jint file_id, 
  jstring struct_name,
  jintArray HDFfid,
  jintArray sdInterfaceId,
  jbyteArray access 
                     ) 
{

  char *f_name;
  int32 stat;

  jint *j_fid;
  jint *j_sid;
  jbyte *j_acc;
  jboolean bb;

     j_fid = (jint *) (*env)->GetIntArrayElements( env, HDFfid, &bb );
     j_sid = (jint *) (*env)->GetIntArrayElements( env, sdInterfaceId, &bb );
     j_acc = (jbyte *) (*env)->GetByteArrayElements( env, access, &bb );


     f_name = (char *) (*env)->GetStringUTFChars( env, struct_name, 0);

     stat = EHchkfid( (int32)file_id, (char *)f_name, (int32 *)j_fid, (int32 *)j_sid, (uint8 *)j_acc );

     (*env)->ReleaseStringUTFChars(env, struct_name, f_name );
     (*env)->ReleaseIntArrayElements( env, HDFfid, j_fid, JNI_COMMIT);
     (*env)->ReleaseIntArrayElements( env, sdInterfaceId, j_sid, JNI_COMMIT);
     (*env)->ReleaseByteArrayElements( env, access, j_acc, JNI_COMMIT);


   return stat; 
}
