#include <jni.h>
#include "visad_data_hdfeos_hdfeosc_HdfeosLib.h"
#include <stdio.h>
#include "mfhdf.h"
#include "HdfEosDef.h"

JNIEXPORT jint JNICALL 
Java_visad_data_hdfeos_hdfeosc_HdfeosLib_EHgetcal
( JNIEnv *env, 
  jclass class, 
  jint sd_id, 
  jint sds_idx,
  jdoubleArray cal,
  jdoubleArray cal_err,
  jdoubleArray off,
  jdoubleArray off_err,
  jintArray type
                     ) 
{

  int32 sds_id;
  int32 status;

  jdouble *j_cal;
  jdouble *j_cal_err;
  jdouble *j_off;
  jdouble *j_off_err;
  jint *j_type;
  jboolean bb;

     j_cal = (jdouble *) (*env)->GetDoubleArrayElements( env, cal, &bb );
     j_cal_err = (jdouble *) (*env)->GetDoubleArrayElements( env, cal_err, &bb );
     j_off = (jdouble *) (*env)->GetDoubleArrayElements( env, off, &bb );
     j_off_err = (jdouble *) (*env)->GetDoubleArrayElements( env, off_err, &bb );
     j_type = (jint *) (*env)->GetIntArrayElements( env, type, &bb );

     sds_id = SDselect( (int32)sd_id, (int32)sds_idx );
   
     status = SDgetcal( (int32)sds_id, (double *)j_cal, (double *)j_cal_err, 
                                       (double *)j_off, (double *)j_off_err, (int32 *)j_type );

     (*env)->ReleaseDoubleArrayElements( env, cal, j_cal, JNI_COMMIT);
     (*env)->ReleaseDoubleArrayElements( env, cal_err, j_cal_err, JNI_COMMIT);
     (*env)->ReleaseDoubleArrayElements( env, off, j_off, JNI_COMMIT);
     (*env)->ReleaseDoubleArrayElements( env, off_err, j_off_err, JNI_COMMIT);
     (*env)->ReleaseIntArrayElements( env, type, j_type, JNI_COMMIT);


   return status; 
}
