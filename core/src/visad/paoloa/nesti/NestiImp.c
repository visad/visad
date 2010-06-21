
/* NestiImp.c */

#include <jni.h>
#include "visad_paoloa_nesti_Nesti.h"

/*
 * Class:     visad_paoloa_GoesRetrieval
 * Method:    get_profil_c
 * Signature: (FI[F[F[F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_nesti_Nesti_readProf_1c
    (JNIEnv *env, jobject obj, jint imon_j, jint pflg_j, jfloatArray tskin_j,
     jfloatArray psfc_j, jintArray lsfc_j, jfloatArray azen_j, jfloatArray pp_j,
     jfloatArray tpro_j,
     jfloatArray wpro_j, jfloatArray opro_j) {

    jfloat *tskin = (*env)->GetFloatArrayElements(env, tskin_j, 0);
    jfloat *psfc = (*env)->GetFloatArrayElements(env, psfc_j, 0);
    jint *lsfc = (*env)->GetIntArrayElements(env, lsfc_j, 0);
    jfloat *azen = (*env)->GetFloatArrayElements(env, azen_j, 0);

    jfloat *pp = (*env)->GetFloatArrayElements(env, pp_j, 0);
    jfloat *tpro = (*env)->GetFloatArrayElements(env, tpro_j, 0);
    jfloat *wpro = (*env)->GetFloatArrayElements(env, wpro_j, 0);
    jfloat *opro = (*env)->GetFloatArrayElements(env, opro_j, 0);
    read_prof_(&imon_j, &pflg_j, tskin, psfc, lsfc, azen, pp, tpro, wpro, opro);
    (*env)->ReleaseFloatArrayElements(env, tskin_j, tskin, 0);
    (*env)->ReleaseFloatArrayElements(env, psfc_j, psfc, 0);
    (*env)->ReleaseIntArrayElements(env, lsfc_j, lsfc, 0);
    (*env)->ReleaseFloatArrayElements(env, azen_j, azen, 0);

    (*env)->ReleaseFloatArrayElements(env, pp_j, pp, 0);
    (*env)->ReleaseFloatArrayElements(env, tpro_j, tpro, 0);
    (*env)->ReleaseFloatArrayElements(env, wpro_j, wpro, 0);
    (*env)->ReleaseFloatArrayElements(env, opro_j, opro, 0);
  }

  JNIEXPORT void JNICALL Java_visad_paoloa_nesti_Nesti_nastirte_1c
    (JNIEnv *env, jobject obj, jfloat tskin_j, jfloat psfc_j, jint lsfc_j,
     jfloat azen_j, jfloatArray p_j, jfloatArray t_j, jfloatArray w_j,
     jfloatArray o_j, jintArray u_j, jdoubleArray vn_j, jdoubleArray tb_j,
     jdoubleArray rr_j) {

    jfloat *pp = (*env)->GetFloatArrayElements(env, p_j, 0);
    jfloat *tt = (*env)->GetFloatArrayElements(env, t_j, 0);
    jfloat *ww = (*env)->GetFloatArrayElements(env, w_j, 0);
    jfloat *oo = (*env)->GetFloatArrayElements(env, o_j, 0);

    jint *uu = (*env)->GetIntArrayElements(env, u_j, 0);
    jdouble *vn = (*env)->GetDoubleArrayElements(env, vn_j, 0);
    jdouble *tb = (*env)->GetDoubleArrayElements(env, tb_j, 0);
    jdouble *rr = (*env)->GetDoubleArrayElements(env, rr_j, 0);

    nast_i_rte_( &tskin_j, &psfc_j, &lsfc_j, &azen_j, pp, tt, ww, oo,
                 uu, vn, tb, rr );

    (*env)->ReleaseFloatArrayElements(env, p_j, pp, 0);
    (*env)->ReleaseFloatArrayElements(env, t_j, tt, 0);
    (*env)->ReleaseFloatArrayElements(env, w_j, ww, 0);
    (*env)->ReleaseFloatArrayElements(env, o_j, oo, 0);

    (*env)->ReleaseIntArrayElements(env, u_j, uu, 0);
    (*env)->ReleaseDoubleArrayElements(env, vn_j, vn, 0);
    (*env)->ReleaseDoubleArrayElements(env, tb_j, tb, 0);
    (*env)->ReleaseDoubleArrayElements(env, rr_j, rr, 0);
  }
JNIEXPORT void JNICALL Java_visad_paoloa_nesti_Nesti_nasti_1retrvl_1c
  (JNIEnv *env, jobject obj, jint opt_j, jint opt2_j, jint rec_j, jfloat gt_j, jfloat gw_j,
   jfloat gts_j, jfloat e_j, jfloatArray tair_j, jfloatArray rr_j, jfloatArray pout_j)
{
  jfloat *pout = (*env)->GetFloatArrayElements(env, pout_j, 0);
  jfloat *tair = (*env)->GetFloatArrayElements(env, tair_j, 0);
  jfloat *rr = (*env)->GetFloatArrayElements(env, rr_j, 0);

  nastimlretrwlmsx_(&opt_j, &opt2_j, &rec_j, &gt_j, &gw_j, &gts_j, &e_j, tair, rr, pout);

  (*env)->ReleaseFloatArrayElements(env, pout_j, pout, 0);
  (*env)->ReleaseFloatArrayElements(env, tair_j, tair, 0);
  (*env)->ReleaseFloatArrayElements(env, rr_j, rr, 0);
}
