//
// ShadowNodeFunctionTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.cluster;

import visad.*;
import visad.java3d.*;
import visad.java2d.*;

import java.rmi.*;

import java.awt.*;
import java.awt.image.*;

/**
   The ShadowNodeFunctionTypeJ3D class shadows the FunctionType class for
   NodeRendererJ3D, within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowNodeFunctionTypeJ3D extends ShadowFunctionTypeJ3D {

  public ShadowNodeFunctionTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height)
         throws VisADException {
    // create basic Appearance
    VisADAppearance appearance =
      makeAppearance(mode, constant_alpha, constant_color, array);

// must encode image as Serializable rather than as Image

    appearance.image = image;
    ((VisADGroup) group).addChild(appearance);
  }

  public void texture3DToGroup(Object group, VisADGeometryArray arrayX,
                    VisADGeometryArray arrayY, VisADGeometryArray arrayZ,
                    VisADGeometryArray arrayXrev,
                    VisADGeometryArray arrayYrev,
                    VisADGeometryArray arrayZrev,
                    BufferedImage[] images, GraphicsModeControl mode,
                    float constant_alpha, float[] constant_color,
                    int texture_width, int texture_height,
                    int texture_depth, DataRenderer renderer)
         throws VisADException {
// ??
  }

  public void textureStackToGroup(Object group, VisADGeometryArray arrayX,
                    VisADGeometryArray arrayY, VisADGeometryArray arrayZ,
                    VisADGeometryArray arrayXrev,
                    VisADGeometryArray arrayYrev,
                    VisADGeometryArray arrayZrev,
                    BufferedImage[] imagesX,
                    BufferedImage[] imagesY,
                    BufferedImage[] imagesZ,
                    GraphicsModeControl mode,
                    float constant_alpha, float[] constant_color,
                    int texture_width, int texture_height,
                    int texture_depth, DataRenderer renderer)
         throws VisADException {
// ??
  }


  public Object makeSwitch() {
    return new VisADSwitch();
  }

  public Object makeBranch() {
    VisADGroup branch = new VisADGroup();
    return branch;
  }

  public void addToGroup(Object group, Object branch)
         throws VisADException {
    ((VisADGroup) group).addChild((VisADGroup) branch);
  }

  public void addToSwitch(Object swit, Object branch)
         throws VisADException {
    ((VisADSwitch) swit).addChild((VisADGroup) branch);
  }

  public void addSwitch(Object group, Object swit, Control control,
                        Set domain_set, DataRenderer renderer)
         throws VisADException {
    ((VisADSwitch) swit).setSet(domain_set); // Serialize domain_set with swit
    ((VisADGroup) group).addChild((VisADSwitch) swit);
  }

// NOT true in ShadowFunctionOrSetTypeJ3D, but true here for
// better Serializable compression ??
  public boolean wantIndexed() {
    return true;
  }

  public boolean addToGroup(Object group, VisADGeometryArray array,
                            GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color)
         throws VisADException {
    return ShadowNodeFunctionTypeJ3D.staticAddToGroup(group, array, mode,
                                                      constant_alpha, constant_color);
  }

  public static boolean staticAddToGroup(Object group, VisADGeometryArray array,
                                         GraphicsModeControl mode,
                                         float constant_alpha, float[] constant_color)
         throws VisADException {
    if (array != null) {
      VisADAppearance appearance =
        makeAppearance(mode, constant_alpha, constant_color, array);
      ((VisADGroup) group).addChild(appearance);
      return true;
    }
    else {
      return false;
    }
  }

  /** construct an VisADAppearance object */
  static VisADAppearance makeAppearance(GraphicsModeControl mode,
                      float constant_alpha,
                      float[] constant_color,
                      VisADGeometryArray array) {
    VisADAppearance appearance = new VisADAppearance();
    appearance.pointSize = mode.getPointSize();
    appearance.lineWidth = mode.getLineWidth();

    appearance.alpha = constant_alpha; // may be Float.NaN
    if (constant_color != null && constant_color.length == 3) {
      appearance.color_flag = true;
      appearance.red = constant_color[0];
      appearance.green = constant_color[1];
      appearance.blue = constant_color[2];
    }
    appearance.array = array; // may be null
    return appearance;
  }

}

