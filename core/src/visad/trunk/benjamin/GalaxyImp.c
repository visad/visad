
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

/*
 * Class:     visad_benjamin_Galaxy
 * Method:    profile_c
 * Signature: (IIFFF[F[F)V
 */
JNIEXPORT void JNICALL Java_visad_benjamin_Galaxy_profile_1c
  (JNIEnv *env, jobject obj, jint type_j, jint npts_j, jfloat x_j,
   jfloat y_j, jfloat z_j, jfloatArray xprof_j, jfloatArray yprof_j )
{
   jfloat *xprof = (*env)->GetFloatArrayElements(env, xprof_j, 0);
   jfloat *yprof = (*env)->GetFloatArrayElements(env, yprof_j, 0);

   profile_( &type_j, &npts_j, &x_j, &y_j, &z_j, xprof, yprof );

   (*env)->ReleaseFloatArrayElements(env, xprof_j, xprof, 0);
   (*env)->ReleaseFloatArrayElements(env, yprof_j, yprof, 0);
}

/*
 * Class:      visad_benjamin_Galaxy
 * Method:     galtosol
 * Signature: (FFF[F)V
 */
JNIEXPORT void JNICALL Java_visad_benjamin_Galaxy_galtosol
  (JNIEnv *env, jobject obj, jfloat x_j, jfloat y_j, jfloat z_j,
   jfloatArray lbd_j ) {

   jfloat *lbd = (*env)->GetFloatArrayElements(env, lbd_j, 0);
   galtosol_( &x_j, &y_j, &z_j, lbd );
   (*env)->ReleaseFloatArrayElements(env, lbd_j, lbd, 0);
}
