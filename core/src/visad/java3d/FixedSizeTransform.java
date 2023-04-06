/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visad.java3d;

import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Vector3f;
import visad.ProjectionControl;
import visad.VisADGeometryArray;

/**
 * Class LabelTransform
 */
public class FixedSizeTransform {

  /**  */
  TransformGroup trans;

  /**  */
  Transform3D t3d;

  /**  */
  ProjectionControl proj;

  /**  */
  double[] matrix;

  /**  */
  double last_scale;

  /**  */
  double first_scale;

  /**  */
  float[] anchr_vertex;

  /**  */
  double[] rot_a;

  /**  */
  double[] trans_a;

  /**  */
  double[] scale_a;

  /**
   *
   *
   * @param trans
   * @param proj
   */
  FixedSizeTransform(TransformGroup trans, ProjectionControl proj, float[] anchr_vertex) {
    this.trans = trans;
    this.proj = proj;

    t3d = new Transform3D();
    matrix = proj.getMatrix();
    rot_a = new double[3];
    trans_a = new double[3];
    scale_a = new double[1];
    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);
    last_scale = scale_a[0];
    first_scale = last_scale;

    this.anchr_vertex = anchr_vertex;
  }

  /**
   *
   *
   * @param first_scale
   * @param scale_a
   */
  public void updateTransform(double first_scale, double[] scale_a) {
    trans.getTransform(t3d);

    double factor = 0;
    float f_scale = 0;

    double k = first_scale; // - final scale
    factor = k / scale_a[0];
    f_scale = (float) ((scale_a[0] - k) / scale_a[0]);
      
    Vector3f trans_vec = new Vector3f(f_scale * anchr_vertex[0], f_scale
        * anchr_vertex[1], f_scale * anchr_vertex[2]);

    //  These can't all be zero: non-affine transform
    if (!(factor == 0.0 && (trans_vec.x == 0.0 && trans_vec.y == 0.0 && trans_vec.z == 0.0))) {
      t3d.set((float) factor, trans_vec);
      trans.setTransform(t3d);
    }
  }
}
