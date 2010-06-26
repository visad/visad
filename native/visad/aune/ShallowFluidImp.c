
/* ShallowFluidImp.c */

#include <jni.h>
#include "visad_aune_ShallowFluid.h"

/*
 * Class:     visad_aune_ShallowFluid
 * Method:    shalstep_c
 * Signature: (I[F[F[F[F[F[F[F[F[F[F[F[FIIFFFFFFFFFF)V
 */
  JNIEXPORT void JNICALL Java_visad_aune_ShallowFluid_shalstep_1c
    (JNIEnv *env, jobject obj, jint step, jfloatArray oldest_x0_j,
     jfloatArray oldest_x1_j, jfloatArray oldest_x2_j,
     jfloatArray oldest_x3_j, jfloatArray old_x0_j, jfloatArray old_x1_j,
     jfloatArray old_x2_j, jfloatArray old_x3_j, jfloatArray new_x0_j,
     jfloatArray new_x1_j, jfloatArray new_x2_j, jfloatArray new_x3_j,
     jint iopt, jint ibc, jfloat gravity, jfloat alat, jfloat ubar1,
     jfloat vbar1, jfloat hprm11, jfloat hprm12, jfloat delt, jfloat eps,
     jfloat adiff, jfloat tfilt) {

    jfloat *oldest_x0 = (*env)->GetFloatArrayElements(env, oldest_x0_j, 0);
    jfloat *oldest_x1 = (*env)->GetFloatArrayElements(env, oldest_x1_j, 0);
    jfloat *oldest_x2 = (*env)->GetFloatArrayElements(env, oldest_x2_j, 0);
    jfloat *oldest_x3 = (*env)->GetFloatArrayElements(env, oldest_x3_j, 0);
    jfloat *old_x0 = (*env)->GetFloatArrayElements(env, old_x0_j, 0);
    jfloat *old_x1 = (*env)->GetFloatArrayElements(env, old_x1_j, 0);
    jfloat *old_x2 = (*env)->GetFloatArrayElements(env, old_x2_j, 0);
    jfloat *old_x3 = (*env)->GetFloatArrayElements(env, old_x3_j, 0);
    jfloat *new_x0 = (*env)->GetFloatArrayElements(env, new_x0_j, 0);
    jfloat *new_x1 = (*env)->GetFloatArrayElements(env, new_x1_j, 0);
    jfloat *new_x2 = (*env)->GetFloatArrayElements(env, new_x2_j, 0);
    jfloat *new_x3 = (*env)->GetFloatArrayElements(env, new_x3_j, 0);
    shalstep_(&step, oldest_x0, oldest_x1, oldest_x2, oldest_x3,
              old_x0, old_x1, old_x2, old_x3, new_x0, new_x1, new_x2,
              new_x3, &iopt, &ibc, &gravity, &alat, &ubar1, &vbar1,
              &hprm11, &hprm12, &delt, &eps, &adiff, &tfilt);
    (*env)->ReleaseFloatArrayElements(env, oldest_x0_j, oldest_x0, 0);
    (*env)->ReleaseFloatArrayElements(env, oldest_x1_j, oldest_x1, 0);
    (*env)->ReleaseFloatArrayElements(env, oldest_x2_j, oldest_x2, 0);
    (*env)->ReleaseFloatArrayElements(env, oldest_x3_j, oldest_x3, 0);
    (*env)->ReleaseFloatArrayElements(env, old_x0_j, old_x0, 0);
    (*env)->ReleaseFloatArrayElements(env, old_x1_j, old_x1, 0);
    (*env)->ReleaseFloatArrayElements(env, old_x2_j, old_x2, 0);
    (*env)->ReleaseFloatArrayElements(env, old_x3_j, old_x3, 0);
    (*env)->ReleaseFloatArrayElements(env, new_x0_j, new_x0, 0);
    (*env)->ReleaseFloatArrayElements(env, new_x1_j, new_x1, 0);
    (*env)->ReleaseFloatArrayElements(env, new_x2_j, new_x2, 0);
    (*env)->ReleaseFloatArrayElements(env, new_x3_j, new_x3, 0);
  }

