
/* GoesCollaborationImp.c */

#include <jni.h>
#include "visad_paoloa_GoesCollaboration.h"

#ifdef DOUBLE_DASH
#define FORTNAME(foo) foo##__
#else
#define FORTNAME(foo) foo##_
#endif

/*
 * Class:     visad_paoloa_GoesCollaboration
 * Method:    re_read_1_c
 * Signature: (I[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_GoesCollaboration_re_1read_11_1c
    (JNIEnv *env, jobject obj, jint i, jfloatArray data_b_j) {

    int ia[1];

    jfloat *data_b = (*env)->GetFloatArrayElements(env, data_b_j, 0);
    FORTNAME(re_read_1)(&i, data_b);
    (*env)->ReleaseFloatArrayElements(env, data_b_j, data_b, 0);
  }



/*
 * Class:     visad_paoloa_GoesCollaboration
 * Method:    goesrte_2_c
 * Signature: (FF[F[F[F[F[F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_GoesCollaboration_goesrte_12_1c
    (JNIEnv *env, jobject obj, jfloat gzen, jfloat tskin,
     jfloatArray t_j, jfloatArray w_j, jfloatArray c_j,
     jfloatArray p_j, jfloatArray wfn_j, jfloatArray tbcx_j) {

    jfloat *t = (*env)->GetFloatArrayElements(env, t_j, 0);
    jfloat *w = (*env)->GetFloatArrayElements(env, w_j, 0);
    jfloat *c = (*env)->GetFloatArrayElements(env, c_j, 0);
    jfloat *p = (*env)->GetFloatArrayElements(env, p_j, 0);
    jfloat *wfn = (*env)->GetFloatArrayElements(env, wfn_j, 0);
    jfloat *tbcx = (*env)->GetFloatArrayElements(env, tbcx_j, 0);
    FORTNAME(goesrte_2)(&gzen, &tskin, t, w, c, p, wfn, tbcx);
    (*env)->ReleaseFloatArrayElements(env, t_j, t, 0);
    (*env)->ReleaseFloatArrayElements(env, w_j, w, 0);
    (*env)->ReleaseFloatArrayElements(env, c_j, c, 0);
    (*env)->ReleaseFloatArrayElements(env, p_j, p, 0);
    (*env)->ReleaseFloatArrayElements(env, wfn_j, wfn, 0);
    (*env)->ReleaseFloatArrayElements(env, tbcx_j, tbcx, 0);
  }



/*
 * Class:     visad_paoloa_GoesCollaboration
 * Method:    get_profil_c
 * Signature: (FI[F[F[F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_GoesCollaboration_get_1profil_1c
    (JNIEnv *env, jobject obj, jfloat rlat, jint imon, jfloatArray tpro_j,
     jfloatArray wpro_j, jfloatArray opro_j, jfloatArray pref_j) {

    jfloat *tpro = (*env)->GetFloatArrayElements(env, tpro_j, 0);
    jfloat *wpro = (*env)->GetFloatArrayElements(env, wpro_j, 0);
    jfloat *opro = (*env)->GetFloatArrayElements(env, opro_j, 0);
    jfloat *pref = (*env)->GetFloatArrayElements(env, pref_j, 0);
    FORTNAME(get_profil)(&rlat, &imon, tpro, wpro, opro, pref);
    (*env)->ReleaseFloatArrayElements(env, tpro_j, tpro, 0);
    (*env)->ReleaseFloatArrayElements(env, wpro_j, wpro, 0);
    (*env)->ReleaseFloatArrayElements(env, opro_j, opro, 0);
    (*env)->ReleaseFloatArrayElements(env, pref_j, pref, 0);
  }



/*
 * Class:     visad_paoloa_GoesCollaboration
 * Method:    change_profil_c
 * Signature: (IFIFFFF[F[F[F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_GoesCollaboration_change_1profil_1c
    (JNIEnv *env, jobject obj, jint pt, jfloat dt, jint pw, jfloat dw,
     jfloat o_w, jfloat t_w, jfloat w_w, jfloatArray t_j, jfloatArray w_j,
     jfloatArray o_j, jfloatArray p_j) {

    jfloat *t = (*env)->GetFloatArrayElements(env, t_j, 0);
    jfloat *w = (*env)->GetFloatArrayElements(env, w_j, 0);
    jfloat *o = (*env)->GetFloatArrayElements(env, o_j, 0);
    jfloat *p = (*env)->GetFloatArrayElements(env, p_j, 0);

/*
    jsize t_len = (*env)->GetArrayLength(env, t_j);
    jsize w_len = (*env)->GetArrayLength(env, w_j);
    jsize o_len = (*env)->GetArrayLength(env, o_j);
    jsize p_len = (*env)->GetArrayLength(env, p_j);

    printf("change_profil_c %d %d %d %d\n", t_len, w_len, o_len, p_len);
*/

    FORTNAME(change_profil)(&pt, &dt, &pw, &dw, &o_w, &t_w, &w_w, t, w, o, p);
    (*env)->ReleaseFloatArrayElements(env, t_j, t, 0);
    (*env)->ReleaseFloatArrayElements(env, w_j, w, 0);
    (*env)->ReleaseFloatArrayElements(env, o_j, o, 0);
    (*env)->ReleaseFloatArrayElements(env, p_j, p, 0);
  }



/*
 * Class:     visad_paoloa_GoesCollaboration
 * Method:    so_read_1_c
 * Signature: (I[F[F[F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_GoesCollaboration_so_1read_11_1c
    (JNIEnv *env, jobject obj, jint i, jfloatArray t_j, jfloatArray td_j,
     jfloatArray p_j, jfloatArray z_j) {

    jfloat *t = (*env)->GetFloatArrayElements(env, t_j, 0);
    jfloat *td = (*env)->GetFloatArrayElements(env, td_j, 0);
    jfloat *p = (*env)->GetFloatArrayElements(env, p_j, 0);
    jfloat *z = (*env)->GetFloatArrayElements(env, z_j, 0);
    FORTNAME(so_read_1)(&i, t, td, p, z);
    (*env)->ReleaseFloatArrayElements(env, t_j, t, 0);
    (*env)->ReleaseFloatArrayElements(env, td_j, td, 0);
    (*env)->ReleaseFloatArrayElements(env, p_j, p, 0);
    (*env)->ReleaseFloatArrayElements(env, z_j, z, 0);
  }



/*
 * Class:     visad_paoloa_GoesCollaboration
 * Method:    ev_diff_prof_c
 * Signature: ([F[F[F[F[F[F[F[F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_GoesCollaboration_ev_1diff_1prof_1c
    (JNIEnv *env, jobject obj, jfloatArray ts_j, jfloatArray tds_j,
     jfloatArray ps_j, jfloatArray zs_j, jfloatArray to_j, jfloatArray wo_j,
     jfloatArray oo_j, jfloatArray po_j, jfloatArray diff_j) {

    jfloat *ts = (*env)->GetFloatArrayElements(env, ts_j, 0);
    jfloat *tds = (*env)->GetFloatArrayElements(env, tds_j, 0);
    jfloat *ps = (*env)->GetFloatArrayElements(env, ps_j, 0);
    jfloat *zs = (*env)->GetFloatArrayElements(env, zs_j, 0);
    jfloat *to = (*env)->GetFloatArrayElements(env, to_j, 0);
    jfloat *wo = (*env)->GetFloatArrayElements(env, wo_j, 0);
    jfloat *oo = (*env)->GetFloatArrayElements(env, oo_j, 0);
    jfloat *po = (*env)->GetFloatArrayElements(env, po_j, 0);
    jfloat *diff = (*env)->GetFloatArrayElements(env, diff_j, 0);
    FORTNAME(ev_diff_prof)(ts, tds, ps, zs, to, wo, oo, po, diff);
    (*env)->ReleaseFloatArrayElements(env, ts_j, ts, 0);
    (*env)->ReleaseFloatArrayElements(env, tds_j, tds, 0);
    (*env)->ReleaseFloatArrayElements(env, ps_j, ps, 0);
    (*env)->ReleaseFloatArrayElements(env, zs_j, zs, 0);
    (*env)->ReleaseFloatArrayElements(env, to_j, to, 0);
    (*env)->ReleaseFloatArrayElements(env, wo_j, wo, 0);
    (*env)->ReleaseFloatArrayElements(env, oo_j, oo, 0);
    (*env)->ReleaseFloatArrayElements(env, po_j, po, 0);
    (*env)->ReleaseFloatArrayElements(env, diff_j, diff, 0);
  }



/*
 * Class:     visad_paoloa_GoesCollaboration
 * Method:    dbdtgx_1_c
 * Signature: ([F[F)V
 */
  JNIEXPORT void JNICALL Java_visad_paoloa_GoesCollaboration_dbdtgx_11_1c
    (JNIEnv *env, jobject obj, jfloatArray tbb_j, jfloatArray dbdtgx_j) {

    jfloat *tbb = (*env)->GetFloatArrayElements(env, tbb_j, 0);
    jfloat *dbdtgx = (*env)->GetFloatArrayElements(env, dbdtgx_j, 0);
    FORTNAME(dbdtgx_1)(tbb, dbdtgx);
    (*env)->ReleaseFloatArrayElements(env, tbb_j, tbb, 0);
    (*env)->ReleaseFloatArrayElements(env, dbdtgx_j, dbdtgx, 0);
  }

