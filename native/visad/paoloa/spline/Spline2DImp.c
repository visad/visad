
/* Spline2DImp.c */

#include <jni.h>
#include "visad_paoloa_spline_Spline2D.h"

/*
 * Class:     visad_paoloa_spline_Spline2D
 * Method:    tpspline_c
 * Signature: ([D[D[D[D[D[I)V
 */
JNIEXPORT void JNICALL Java_visad_paoloa_spline_Spline2D_tpspline_1c
  (JNIEnv *env, jobject obj, jdoubleArray x_array_j, jdoubleArray y_array_j,
   jdoubleArray s_array_j, jdoubleArray ytrue_j, jdoubleArray y_j,
   jintArray dimen_j) {

    jdouble *x_array = (*env)->GetDoubleArrayElements(env, x_array_j, 0);
    jdouble *y_array = (*env)->GetDoubleArrayElements(env, y_array_j, 0);
    jdouble *s_array = (*env)->GetDoubleArrayElements(env, s_array_j, 0);
    jdouble *ytrue = (*env)->GetDoubleArrayElements(env, ytrue_j, 0);
    jdouble *y = (*env)->GetDoubleArrayElements(env, y_j, 0);
    jint *dimen = (*env)->GetIntArrayElements(env, dimen_j, 0);

    tpspline_(x_array, y_array, s_array, ytrue, y, dimen);

    (*env)->ReleaseDoubleArrayElements(env, x_array_j, x_array, 0);
    (*env)->ReleaseDoubleArrayElements(env, y_array_j, y_array, 0);
    (*env)->ReleaseDoubleArrayElements(env, s_array_j, s_array, 0);
    (*env)->ReleaseDoubleArrayElements(env, ytrue_j, ytrue, 0);
    (*env)->ReleaseDoubleArrayElements(env, y_j, y, 0);
    (*env)->ReleaseIntArrayElements(env, dimen_j, dimen, 0);
}

