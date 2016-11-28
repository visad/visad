/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visad;

import java.util.ArrayList;
import java.rmi.RemoteException;
import org.jogamp.java3d.*;


public class FixGeomSizeAppearance implements ControlListener {

  /**  */
  ArrayList<Object> FSTarray = new ArrayList<Object>();

  /**  */
  ProjectionControl p_cntrl = null;

  /**  */
  double last_scale;

  /**  */
  protected double first_scale = Double.NaN;
  
  double baseScale = Double.NaN;
  
  double rescaleThreshold = 1.15;

  /**  */
  int cnt = 0;
  
  VisADGeometryArray array;
  
  Object topBranch;
  
  ArrayList<float[]> anchors;
  
  protected ShadowType shadow;
  
  GraphicsModeControl mode;
  
  float constant_alpha;
  
  float[] constant_color;
  
  private boolean locked = false;
  
  MouseBehavior mouseBehav;
  
  /**
   *
   *
   * @param p_cntrl
   */
  public FixGeomSizeAppearance(ProjectionControl p_cntrl, ShadowType shadow, MouseBehavior mouseBehav) {
    this.p_cntrl = p_cntrl;
    this.shadow = shadow;
    this.mouseBehav = mouseBehav;
    double[] matrix = p_cntrl.getMatrix();
    double[] rot_a = new double[3];
    double[] trans_a = new double[3];
    double[] scale_a = new double[1];
    mouseBehav.instance_unmake_matrix(rot_a, scale_a, trans_a, matrix);
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

    mouseBehav.instance_unmake_matrix(rot_a, scale_a, trans_a, matrix);

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
          rescaleAndReplace(scale_a[0], topBranch, array, anchors, mode, constant_alpha, constant_color);
        }
        last_scale = scale_a[0];
      }
    }
  }
  
  public void rescaleAndReplace(double scale, Object topBranch, VisADGeometryArray array, ArrayList<float[]> anchors, GraphicsModeControl mode, float constant_alpha, float[] constant_color) 
       throws VisADException {
    throw new VisADException("rescaleAndReplace unimplemented");
  }
  
  public void add(Object branch, VisADGeometryArray array, ArrayList<float[]> anchors, GraphicsModeControl mode, float constant_alpha, float[] constant_color) {
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
   Object branch;
   VisADGeometryArray array;
   ArrayList<float[]> anchors;
   GraphicsModeControl mode;
   float constant_alpha;
   float[] constant_color;
}
