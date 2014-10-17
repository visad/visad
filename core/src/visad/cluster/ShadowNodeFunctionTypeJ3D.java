//
// ShadowNodeFunctionTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

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

  public boolean doTransform(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    Data new_data = data;
    if (renderer instanceof NodeRendererJ3D &&
        data instanceof RemoteNodePartitionedFieldImpl) {
      int resolution = ((NodeRendererJ3D) renderer).getResolution();
      if (resolution > 1) {
        FieldImpl adaptedField =
          ((RemoteNodePartitionedFieldImpl) data).getAdaptedField();
        Set set = adaptedField.getDomainSet();
        if (set instanceof Gridded3DSet &&
            set.getManifoldDimension() == 3) {
          Gridded3DSet domain_set = (Gridded3DSet) set;
          float[][] samples = domain_set.getSamples(false);
          int x_len = domain_set.getLength(0);
          int y_len = domain_set.getLength(1);
          int z_len = domain_set.getLength(2);
          int len = domain_set.getLength();
          int new_x_len = 1 + (x_len - 1) / resolution;
          int new_y_len = 1 + (y_len - 1) / resolution;
          int new_z_len = 1 + (z_len - 1) / resolution;
          int new_len = new_x_len * new_y_len * new_z_len;
          float[][] new_samples = new float[3][new_len];
          for (int x=0; x<new_x_len; x++) {
            int i = x * resolution;
            for (int y=0; y<new_y_len; y++) {
              int j = y * resolution;
              for (int z=0; z<new_z_len; z++) {
                int k = z * resolution;
                int ijk = i + x_len * (j + y_len * k);
                int xyz = x + new_x_len * (y + new_y_len * z);
                new_samples[0][xyz] = samples[0][ijk];
                new_samples[1][xyz] = samples[1][ijk];
                new_samples[2][xyz] = samples[2][ijk];
              }
            }
          }
          SetType domain_type = (SetType) domain_set.getType();
          Gridded3DSet new_domain_set =
            new Gridded3DSet(domain_type, new_samples,
                             new_x_len, new_y_len, new_z_len,
                             domain_set.getCoordinateSystem(),
                             domain_set.getSetUnits(), null);
          FieldImpl newAdaptedField = (FieldImpl)
            adaptedField.resample(new_domain_set);
          new_data = new RemoteNodePartitionedFieldImpl(newAdaptedField);
// System.out.println("resolution = " + resolution + " " +
//                    new_len + " out of " + len);
        }
      }
    }
    return super.doTransform(group, new_data, value_array,
                             default_values, renderer);
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
    // appearance.image = image;
    appearance.image = null;
    appearance.image_type = image.getType();
    appearance.image_width = image.getWidth();
    appearance.image_height = image.getHeight();
    appearance.image_pixels =
      image.getRGB(0, 0, appearance.image_width, appearance.image_height, null,
                   0, appearance.image_width);
    ((VisADGroup) group).addChild(appearance);
    appearance.texture_width = texture_width;
    appearance.texture_height = texture_height;
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
    // not used now, so do nothing
  }

  /** client must process the VisADSwitch this makes in order to insert
      in a Java3D scene graph */
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
    VisADGeometryArray[] geometryX = makeVisADGeometrys(arrayX);
    VisADGeometryArray[] geometryY = makeVisADGeometrys(arrayY);
    VisADGeometryArray[] geometryZ = makeVisADGeometrys(arrayZ);
// cut and paste ShadowFunctionOrSetTypeJ3D.textureStackToGroup

    // client must treat branchX as ordered
    VisADGroup branchX = new VisADGroup();
    int data_depth = geometryX.length;
    for (int i=0; i<data_depth; i++) {
      // client must compute c_alpha from constant_alpha
      VisADAppearance appearance =
        makeAppearance(mode, constant_alpha, constant_color, geometryX[i]);
      // must encode image as Serializable rather than as Image
      // appearance.image = image;
      appearance.image = null;
      appearance.image_type = imagesX[i].getType();
      appearance.image_width = imagesX[i].getWidth();
      appearance.image_height = imagesX[i].getHeight();
      appearance.image_pixels =
        imagesX[i].getRGB(0, 0, appearance.image_width, appearance.image_height,
                          null, 0, appearance.image_width);
      // according to logic in ShadowFunctionOrSetTypeJ3D.textureStackToGroup()
      appearance.texture_width = imagesX[i].getWidth();
      appearance.texture_height = imagesX[i].getHeight();
      branchX.addChild(appearance);
    }
    // client must construct branchXrev from VisADAppearances in branchX

    // VisADGroup branchYrev = new VisADGroup();
    // client must treat branchY as ordered
    VisADGroup branchY = new VisADGroup();
    int data_height = geometryY.length;
    for (int i=0; i<data_height; i++) {
      // client must compute c_alpha from constant_alpha
      VisADAppearance appearance =
        makeAppearance(mode, constant_alpha, constant_color, geometryY[i]);
      // must encode image as Serializable rather than as Image
      // appearance.image = image;
      appearance.image = null;
      appearance.image_type = imagesY[i].getType();
      appearance.image_width = imagesY[i].getWidth();
      appearance.image_height = imagesY[i].getHeight();
      appearance.image_pixels =
        imagesY[i].getRGB(0, 0, appearance.image_width, appearance.image_height, 
                          null, 0, appearance.image_width);
      // according to logic in ShadowFunctionOrSetTypeJ3D.textureStackToGroup()
      appearance.texture_width = imagesY[i].getWidth();
      appearance.texture_height = imagesY[i].getHeight();
      branchY.addChild(appearance);
    }
    // client must construct branchYrev from VisADAppearances in branchY
    // VisADGroup branchYrev = new VisADGroup();

    // VisADGroup branchZrev = new VisADGroup();
    // client must treat branchZ as ordered
    VisADGroup branchZ = new VisADGroup();
    int data_width = geometryZ.length;
    for (int i=0; i<data_width; i++) {
      // client must compute c_alpha from constant_alpha
      VisADAppearance appearance =
        makeAppearance(mode, constant_alpha, constant_color, geometryZ[i]);
      // must encode image as Serializable rather than as Image
      // appearance.image = image;
      appearance.image = null;
      appearance.image_type = imagesZ[i].getType();
      appearance.image_width = imagesZ[i].getWidth();
      appearance.image_height = imagesZ[i].getHeight();
      appearance.image_pixels =
        imagesZ[i].getRGB(0, 0, appearance.image_width, appearance.image_height, 
                          null, 0, appearance.image_width);
      // according to logic in ShadowFunctionOrSetTypeJ3D.textureStackToGroup()
      appearance.texture_width = imagesZ[i].getWidth();
      appearance.texture_height = imagesZ[i].getHeight();
      branchZ.addChild(appearance);
    }
    // client must construct branchZrev from VisADAppearances in branchZ
    // VisADGroup branchZrev = new VisADGroup();

    VisADSwitch swit = (VisADSwitch) makeSwitch();
    swit.addChild(branchX);
    swit.addChild(branchY);
    swit.addChild(branchZ);
    // swit.addChild(branchXrev);
    // swit.addChild(branchYrev);
    // swit.addChild(branchZrev);
    swit.setSet(null); // to distinguish swit from a VisADSwitch for Animation

    VisADGroup branch = new VisADGroup();
    branch.addChild(swit);
    if (((VisADGroup) group).numChildren() > 0) {
      ((VisADGroup) group).setChild(branch, 0);
    }
    else {
      ((VisADGroup) group).addChild(branch);
    }
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

// NOT true in ShadowFunctionOrSetTypeJ3D
// could be true for better Serializable compression
// but false means client does not need to un-index it
  public boolean wantIndexed() {
    return false;
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

