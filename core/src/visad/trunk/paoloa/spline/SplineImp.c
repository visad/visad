
/* SplineImp.c */

#include <jni.h>
#include "visad_paoloa_spline_Spline.h"

/*
 * Class:     visad_paoloa_spline_Spline
 * Method:    getspline_c
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_visad_paoloa_spline_Spline_getspline_1c
  (JNIEnv *env, jobject obj, jdoubleArray y_j, jdoubleArray ys0_j,
   jdouble val_j, jint mode_j) {

    jdouble *y = (*env)->GetDoubleArrayElements(env, y_j, 0);
    jdouble *ys0 = (*env)->GetDoubleArrayElements(env, ys0_j, 0);
    getspline_( y, ys0, &val_j, &mode_j );
    (*env)->ReleaseDoubleArrayElements(env, y_j, y, 0);
    (*env)->ReleaseDoubleArrayElements(env, ys0_j, ys0, 0);
}
