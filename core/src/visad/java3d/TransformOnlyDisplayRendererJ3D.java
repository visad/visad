//
// TransformOnlyDisplayRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.java3d;

import visad.*;

import java.awt.image.BufferedImage;

import javax.media.j3d.*;

/**
 * <CODE>TransformOnlyDisplayRendererJ3D</CODE> is the VisAD class for the
 * transforming but not rendering under Java3D.<P>
 */
public class TransformOnlyDisplayRendererJ3D extends DisplayRendererJ3D {

  /**
   * This is the <CODE>DisplayRenderer</CODE> used for the
   * <CODE>TRANSFORM_ONLY</CODE> api.
   * It is used for only transforming data into VisADSceneGraphObject
   * but not rendering (and hence no interaction).
   */
  public TransformOnlyDisplayRendererJ3D () {
    super();
  }

  /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root;
   * create 3-D box, lights and <CODE>MouseBehaviorJ3D</CODE> for
   * embedded user interface.
   * @param v
   * @param vpt
   * @param c
   * @return Scene graph root.
   */
  public BranchGroup createSceneGraph(View v, TransformGroup vpt,
                                      VisADCanvasJ3D c) {
    return null;
  }

  public BufferedImage getImage() {
    return null;
  }

  void notifyCapture() {
  }

  public void setCursorOn(boolean on) {
  }

  public void setDirectOn(boolean on) {
  }

  public void addSceneGraphComponent(Group group) {
  }

  public void addDirectManipulationSceneGraphComponent(Group group,
                         DirectManipulationRendererJ3D renderer) {
  }

  public void clearScene(DataRenderer renderer) {
  }

  public void drag_depth(float diff) {
  }

  public void drag_cursor(VisADRay ray, boolean first) {
  }

  public void setCursorLoc(float x, float y, float z) {
  }

  public void drawCursorStringVector(VisADCanvasJ3D canvas) {
  }

  public DataRenderer findDirect(VisADRay ray, int mouseModifiers) {
    return null;
  }

  public boolean anyDirects() {
    return false;
  }

  public void setScaleOn(boolean on) {
  }

  public void setScale(int axis, int axis_ordinal,
              VisADLineArray array, float[] scale_color)
         throws VisADException {
  }

  public void setScale(int axis, int axis_ordinal,
              VisADLineArray array, VisADTriangleArray labels,
              float[] scale_color)
         throws VisADException {
  }

  public void clearScales() {
  }

  public void setTransform3D(Transform3D t) {
  }

  public void setBoxAspect(double[] aspect) {
  }


// must override these:


  public DataRenderer makeDefaultRenderer() {
    return new DefaultRendererJ3D();
  }

  public boolean legalDataRenderer(DataRenderer renderer) {
    return (renderer instanceof RendererJ3D);
  }

}

