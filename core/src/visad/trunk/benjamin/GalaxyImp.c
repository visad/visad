
/* GalaxyImp.c */

#include <jni.h>
#include "visad_benjamin_Galaxy.h"

/*
 * Class:     visad_benjamin_Galaxy
 * Method:    getcon_c
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_visad_benjamin_Galaxy_getcon_1c
  (JNIEnv *env, jobject obj, jintArray constants_j) {

    jint *constants = (*env)->GetIntArrayElements(env, constants_j, 0);
    getcon_(constants);
    (*env)->ReleaseIntArrayElements(env, constants_j, constants, 0);
  }


/*
 * Class:     visad_benjamin_Galaxy
 * Method:    ismgsc_c
 * Signature: ([F[I[F[F[F[F)V
 */
JNIEXPORT void JNICALL Java_visad_benjamin_Galaxy_ismgsc_1c
  (JNIEnv *env, jobject obj, jfloatArray params_j, jintArray sizes_j,
   jfloatArray grid_a_j, jfloatArray image_a_j, jfloatArray lons_j,
   jfloatArray lats_j) {

    jfloat *params = (*env)->GetFloatArrayElements(env, params_j, 0);
    jint *sizes = (*env)->GetIntArrayElements(env, sizes_j, 0);
    jfloat *grid_a = (*env)->GetFloatArrayElements(env, grid_a_j, 0);
    jfloat *image_a = (*env)->GetFloatArrayElements(env, image_a_j, 0);
    jfloat *lons = (*env)->GetFloatArrayElements(env, lons_j, 0);
    jfloat *lats = (*env)->GetFloatArrayElements(env, lats_j, 0);
    ismgsc_(params, sizes, grid_a, image_a, lons, lats);
    (*env)->ReleaseFloatArrayElements(env, params_j, params, 0);
    (*env)->ReleaseIntArrayElements(env, sizes_j, sizes, 0);
    (*env)->ReleaseFloatArrayElements(env, grid_a_j, grid_a, 0);
    (*env)->ReleaseFloatArrayElements(env, image_a_j, image_a, 0);
    (*env)->ReleaseFloatArrayElements(env, lons_j, lons, 0);
    (*env)->ReleaseFloatArrayElements(env, lats_j, lats, 0);
  }

