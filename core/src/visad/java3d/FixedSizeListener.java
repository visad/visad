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
import visad.VisADGeometryArray;
import visad.GraphicsModeControl;
import visad.TrajectoryManager;
import visad.ShadowType;
import javax.media.j3d.*;


public class FixedSizeListener implements ControlListener {

  /**  */
  ArrayList<Object> FSTarray = new ArrayList<Object>();

  /**  */
  ProjectionControl p_cntrl = null;

  /**  */
  double last_scale;

  /**  */
  double first_scale = Double.NaN;
  
  double baseScale = Double.NaN;
  
  double rescaleThreshold = 1.15;

  /**  */
  int cnt = 0;
  
  VisADGeometryArray array;
  
  BranchGroup topBranch;
  
  ArrayList<float[]> anchors;
  
  ShadowType shadow;
  
  GraphicsModeControl mode;
  
  float constant_alpha;
  
  float[] constant_color;
  
  private boolean locked = false;
  
  /**
   *
   *
   * @param p_cntrl
   */
  FixedSizeListener(ProjectionControl p_cntrl, ShadowType shadow) {
    this.p_cntrl = p_cntrl;
    this.shadow = shadow;
    double[] matrix = p_cntrl.getMatrix();
    double[] rot_a = new double[3];
    double[] trans_a = new double[3];
    double[] scale_a = new double[1];
    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);
    last_scale = scale_a[0];
    first_scale = last_scale;
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
    if (locked) {
       return;
    }
    update();
  }
  
  public void update() throws VisADException {
    double[] matrix = p_cntrl.getMatrix();
    double[] rot_a = new double[3];
    double[] trans_a = new double[3];
    double[] scale_a = new double[1];

    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);

    // - identify scale change events.
    if (!visad.util.Util.isApproximatelyEqual(scale_a[0], last_scale)) {
      if (scale_a[0] / last_scale > rescaleThreshold || scale_a[0] / last_scale < 1 / rescaleThreshold) {
        for (int k=0; k<FSTarray.size(); k++) {
          Info info = (Info) FSTarray.get(k);
          topBranch = info.branch;
          array = info.array;
          anchors = info.anchors;
          mode = info.mode;
          constant_alpha = info.constant_alpha;
          constant_color = info.constant_color;
          BranchGroup branch = new BranchGroup();
          branch.setCapability(BranchGroup.ALLOW_DETACH);
          array = TrajectoryManager.scaleGeometry(array, anchors, (float)(first_scale/scale_a[0]));
          shadow.addToGroup(branch, array, mode, constant_alpha, constant_color);
          try {
            ((BranchGroup)topBranch.getChild(0)).detach();
            topBranch.addChild(branch);
          }
          catch (Exception exc) {
             System.out.println(exc);
          }
        }
        last_scale = scale_a[0];
      }
    }
  }
  
  public void add(BranchGroup branch, VisADGeometryArray array, ArrayList<float[]> anchors, GraphicsModeControl mode, float constant_alpha, float[] constant_color) {
     Info info = new Info();
     info.branch = branch;
     info.array = array;
     info.anchors = anchors;
     info.mode = mode;
     info.constant_alpha = constant_alpha;
     info.constant_color = constant_color;
     FSTarray.add(info);
  }
  
  public synchronized void lock() {
     locked = true;
  }
  
  public synchronized void unlock() {
     locked = false;
  }
  
  public boolean isLocked() {
     return locked;
  }
}

class Info {
   BranchGroup branch;
   VisADGeometryArray array;
   ArrayList<float[]> anchors;
   GraphicsModeControl mode;
   float constant_alpha;
   float[] constant_color;
}
