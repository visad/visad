/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visad.java3d;

import java.util.ArrayList;
import java.rmi.RemoteException;
import visad.ControlEvent;
import visad.ControlListener;
import visad.ProjectionControl;
import visad.VisADException;


public class FixedSizeListener implements ControlListener {

  /**  */
  ArrayList<FixedSizeTransform> FSTarray = new ArrayList<FixedSizeTransform>();

  /**  */
  ProjectionControl p_cntrl = null;

  /**  */
  double last_scale;

  /**  */
  double first_scale;

  /**  */
  int cnt = 0;

  /**
   *
   *
   * @param p_cntrl
   */
  FixedSizeListener(ProjectionControl p_cntrl) {
    this.p_cntrl = p_cntrl;
    double[] matrix = p_cntrl.getMatrix();
    double[] rot_a = new double[3];
    double[] trans_a = new double[3];
    double[] scale_a = new double[1];
    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);
    last_scale = scale_a[0];
    first_scale = last_scale;
    p_cntrl.addControlListener(this);
  }

  /**
   *
   *
   * @param e
   *
   * @throws RemoteException
   * @throws VisADException
   */
  public synchronized void controlChanged(ControlEvent e)
      throws VisADException, RemoteException {
    double[] matrix = p_cntrl.getMatrix();
    double[] rot_a = new double[3];
    double[] trans_a = new double[3];
    double[] scale_a = new double[1];

    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);

    // - identify scale change events.
    if (!visad.util.Util.isApproximatelyEqual(scale_a[0], last_scale)) {
      if (scale_a[0] / last_scale > 1.15 || scale_a[0] / last_scale < 1 / 1.15) {
        for (int k=0; k<FSTarray.size(); k++) {
            FSTarray.get(k).updateTransform(first_scale, scale_a);
        }
        last_scale = scale_a[0];
      }
    }
  }
  
  public void add(FixedSizeTransform fst) {
      FSTarray.add(fst);
  }
}