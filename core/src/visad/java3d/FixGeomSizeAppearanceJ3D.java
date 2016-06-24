/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visad.java3d;

import java.util.ArrayList;
import visad.ControlListener;
import visad.ProjectionControl;
import visad.VisADException;
import visad.VisADGeometryArray;
import visad.GraphicsModeControl;
import visad.TrajectoryManager;
import visad.ShadowType;
import visad.FixGeomSizeAppearance;
import visad.MouseBehavior;
import javax.media.j3d.*;


public class FixGeomSizeAppearanceJ3D extends FixGeomSizeAppearance implements ControlListener {
  /**
   *
   *
   * @param p_cntrl
   */
  public FixGeomSizeAppearanceJ3D(ProjectionControl p_cntrl, ShadowType shadow, MouseBehavior mouseBehav) {
    super(p_cntrl, shadow, mouseBehav);
  }
  
  public void rescaleAndReplace(double scale, Object topBranch, VisADGeometryArray array, ArrayList<float[]> anchors, GraphicsModeControl mode, float constant_alpha, float[] constant_color) 
       throws VisADException {
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    array = TrajectoryManager.scaleGeometry(array, anchors, (float)(first_scale/scale));
    shadow.addToGroup(branch, array, mode, constant_alpha, constant_color);
    ((BranchGroup)((BranchGroup)topBranch).getChild(0)).detach();
    ((BranchGroup)topBranch).addChild(branch);
  }
}