
//
// ShadowFunctionOrSetTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.java3d;
 
import visad.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

import java.awt.*;
import java.awt.image.*;

/**
   The ShadowFunctionOrSetTypeJ3D is an abstract parent for
   ShadowFunctionTypeJ3D and ShadowSetTypeJ3D.<P>
*/
public class ShadowFunctionOrSetTypeJ3D extends ShadowTypeJ3D {

  ShadowRealTupleTypeJ3D Domain;
  ShadowTypeJ3D Range; // null for ShadowSetTypeJ3D

  private Vector AccumulationVector = new Vector();

  public ShadowFunctionOrSetTypeJ3D(MathType t, DataDisplayLink link,
                                    ShadowType parent)
      throws VisADException, RemoteException {
    super(t, link, parent);
    if (this instanceof ShadowFunctionTypeJ3D) {
      Domain = (ShadowRealTupleTypeJ3D)
               ((FunctionType) Type).getDomain().buildShadowType(link, this);
      Range = (ShadowTypeJ3D)
              ((FunctionType) Type).getRange().buildShadowType(link, this);
      adaptedShadowType =
        new ShadowFunctionType(t, link, getAdaptedParent(parent),
                       (ShadowRealTupleType) Domain.getAdaptedShadowType(),
                       Range.getAdaptedShadowType());
    }
    else {
      Domain = (ShadowRealTupleTypeJ3D)
               ((SetType) Type).getDomain().buildShadowType(Link, this);
      Range = null;
      adaptedShadowType =
        new ShadowSetType(t, link, getAdaptedParent(parent),
                       (ShadowRealTupleType) Domain.getAdaptedShadowType());
    }
  }

  public ShadowRealTupleTypeJ3D getDomain() {
    return Domain;
  }

  public ShadowTypeJ3D getRange() {
    return Range;
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
    if (this instanceof ShadowFunctionTypeJ3D) {
      Range.preProcess();
    }
  }

  /** transform data into a Java3D scene graph;
      add generated scene graph components as children of group;
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process */
  public boolean doTransform(Group group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {

    boolean post = ((ShadowFunctionOrSetType) adaptedShadowType).
                        doTransform(group, data, value_array,
                                    default_values, renderer, this);
    ensureNotEmpty(group);
    return post;
  }

  public int textureWidth(int data_width) {
    // must be a power of 2 in Java3D
    int texture_width = 1;
    while (texture_width < data_width) texture_width *= 2;
    return texture_width;
  }
 
  public int textureHeight(int data_height) {
    // must be a power of 2 in Java3D
    int texture_height = 1;
    while (texture_height < data_height) texture_height *= 2;
    return texture_height;
  }

  public int textureDepth(int data_depth) {
    // must be a power of 2 in Java3D
    int texture_depth = 1;
    while (texture_depth < data_depth) texture_depth *= 2;
    return texture_depth;
  }

  public void adjustZ(float[] coordinates) {
    if (display.getDisplayRenderer().getMode2D()) {
      for (int i=2; i<coordinates.length; i+=3) {
        coordinates[i] = DisplayImplJ3D.BACK2D;
      }
    }
  }

  public void setTexCoords(float[] texCoords, float ratiow, float ratioh) {
    // corner 0
    texCoords[0] = 0.0f;
    texCoords[1] = 1.0f;
    // corner 1
    texCoords[2] = ratiow;
    texCoords[3] = 1.0f;
    // corner 2
    texCoords[4] = ratiow;
    texCoords[5] = 1.0f - ratioh;
    // corner 3
    texCoords[6] = 0.0f;
    texCoords[7] = 1.0f - ratioh;
  }

  public void setTex3DCoords(float[] texCoords, int axis, float ratiow,
                             float ratioh, float ratiod) {
    int length = texCoords.length / 12;
    if (axis == 2) {
      for (int i=0; i<length; i++) {
        int i12 = i * 12;
        float depth = 0.0f + (ratiod - 0.0f) * i / (length - 1.0f);
        // corner 0
        texCoords[i12] = 0.0f;
        texCoords[i12 + 1] = 1.0f;
        texCoords[i12 + 2] = depth;
        // corner 1
        texCoords[i12 + 3] = ratiow;
        texCoords[i12 + 4] = 1.0f;
        texCoords[i12 + 5] = depth;
        // corner 2
        texCoords[i12 + 6] = ratiow;
        texCoords[i12 + 7] = 1.0f - ratioh;
        texCoords[i12 + 8] = depth;
        // corner 3
        texCoords[i12 + 9] = 0.0f;
        texCoords[i12 + 10] = 1.0f - ratioh;
        texCoords[i12 + 11] = depth;
      }
    }
    else if (axis == 1) {
      for (int i=0; i<length; i++) {
        int i12 = i * 12;
        float height = 1.0f - ratioh * i / (length - 1.0f);
        // corner 0
        texCoords[i12] = 0.0f;
        texCoords[i12 + 1] = height;
        texCoords[i12 + 2] = 0.0f;
        // corner 1
        texCoords[i12 + 3] = ratiow;
        texCoords[i12 + 4] = height;
        texCoords[i12 + 5] = 0.0f;
        // corner 2
        texCoords[i12 + 6] = ratiow;
        texCoords[i12 + 7] = height;
        texCoords[i12 + 8] = ratiod;
        // corner 3
        texCoords[i12 + 9] = 0.0f;
        texCoords[i12 + 10] = height;
        texCoords[i12 + 11] = ratiod;
      }
    }
    else if (axis == 0) {
      for (int i=0; i<length; i++) {
        int i12 = i * 12;
        float width = 0.0f + (ratiow - 0.0f) * i / (length - 1.0f);
        // corner 0
        texCoords[i12] = width;
        texCoords[i12 + 1] = 1.0f;
        texCoords[i12 + 2] = 0.0f;
        // corner 1
        texCoords[i12 + 3] = width;
        texCoords[i12 + 4] = 1.0f - ratioh;
        texCoords[i12 + 5] = 0.0f;
        // corner 2
        texCoords[i12 + 6] = width;
        texCoords[i12 + 7] = 1.0f - ratioh;
        texCoords[i12 + 8] = ratiod;
        // corner 3
        texCoords[i12 + 9] = width;
        texCoords[i12 + 10] = 1.0f;
        texCoords[i12 + 11] = ratiod;
      }
    }
  }

  public Vector getTextMaps(int i, int[] textIndices) {
    if (i < 0) {
      return ((ShadowTextTypeJ3D) Range).getSelectedMapVector();
    }
    else {
      ShadowTextTypeJ3D text = (ShadowTextTypeJ3D)
        ((ShadowTupleTypeJ3D) Range).getComponent(textIndices[i]);
      return text.getSelectedMapVector();
    }
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height)
         throws VisADException {
    GeometryArray geometry = display.makeGeometry(array);
    // System.out.println("texture geometry");
    // create basic Appearance
    TransparencyAttributes c_alpha = null;

    if (constant_alpha == 1.0f) {
      // constant opaque alpha = NONE
      c_alpha = null;
    }
    else if (constant_alpha == constant_alpha) {
      // c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
      c_alpha = new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                           constant_alpha);
    }
    else {
      c_alpha = new TransparencyAttributes();
      c_alpha.setTransparencyMode(TransparencyAttributes.BLENDED);
    }
    ColoringAttributes c_color = null;
    if (constant_color != null && constant_color.length == 3) {
      c_color = new ColoringAttributes();
      c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
    }
    Appearance appearance =
      makeAppearance(mode, c_alpha, null, geometry, false);
    // create TextureAttributes
    TextureAttributes texture_attributes = new TextureAttributes();
    // texture_attributes.setTextureMode(TextureAttributes.REPLACE);
    texture_attributes.setTextureMode(TextureAttributes.MODULATE);
    texture_attributes.setPerspectiveCorrectionMode(
                          TextureAttributes.NICEST);
    appearance.setTextureAttributes(texture_attributes);
    // create Texture2D
// TextureLoader uses 1st argument = 1
/*
System.out.println("Texture.BASE_LEVEL = " + Texture.BASE_LEVEL); // 1
System.out.println("Texture.RGBA = " + Texture.RGBA); // 6
*/
    Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                                      texture_width, texture_height);
    ImageComponent2D image2d =
      new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
    texture.setImage(0, image2d);
    //
    // from TextureLoader
    // TextureLoader uses 3 for both setMinFilter and setMagFilter
/*
System.out.println("Texture.FASTEST = " + Texture.FASTEST); // 0
System.out.println("Texture.NICEST = " + Texture.NICEST); // 1
System.out.println("Texture.BASE_LEVEL_POINT = " + Texture.BASE_LEVEL_POINT); // 2
System.out.println("Texture.BASE_LEVEL_LINEAR = " + Texture.BASE_LEVEL_LINEAR); // 3
*/
/* for interpolation:
    texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
    texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
*/
    texture.setMinFilter(Texture.BASE_LEVEL_POINT);
    texture.setMagFilter(Texture.BASE_LEVEL_POINT);
    texture.setEnable(true);
    // end of from TextureLoader
    //
    Shape3D shape = new Shape3D(geometry, appearance);
    appearance.setTexture(texture);
    ((Group) group).addChild(shape);
  }

  public void texture3DToGroup(Object group, VisADGeometryArray arrayX,
                    VisADGeometryArray arrayY, VisADGeometryArray arrayZ,
                    BufferedImage[] images, GraphicsModeControl mode,
                    float constant_alpha, float[] constant_color,
                    int texture_width, int texture_height,
                    int texture_depth, DataRenderer renderer)
         throws VisADException {

    GeometryArray geometryX = display.makeGeometry(arrayX);
    GeometryArray geometryY = display.makeGeometry(arrayY);
    GeometryArray geometryZ = display.makeGeometry(arrayZ);
    // System.out.println("texture geometry");
    // create basic Appearance
    TransparencyAttributes c_alpha = null;

    if (constant_alpha == 1.0f) {
      // constant opaque alpha = NONE
      c_alpha = null;
    }
    else if (constant_alpha == constant_alpha) {
      // c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
      c_alpha = new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                           constant_alpha);
    }
    else {
      c_alpha = new TransparencyAttributes();
      c_alpha.setTransparencyMode(TransparencyAttributes.BLENDED);
    }
    ColoringAttributes c_color = null;
    if (constant_color != null && constant_color.length == 3) {
      c_color = new ColoringAttributes();
      c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
    }
    Appearance appearance =
      makeAppearance(mode, c_alpha, null, geometryX, true);
    // create TextureAttributes
    TextureAttributes texture_attributes = new TextureAttributes();
    // texture_attributes.setTextureMode(TextureAttributes.REPLACE);
    texture_attributes.setTextureMode(TextureAttributes.MODULATE);
    texture_attributes.setPerspectiveCorrectionMode(
                          TextureAttributes.NICEST);
    appearance.setTextureAttributes(texture_attributes);
    // create Texture2D
// TextureLoader uses 1st argument = 1
/*
System.out.println("Texture.BASE_LEVEL = " + Texture.BASE_LEVEL); // 1
System.out.println("Texture.RGBA = " + Texture.RGBA); // 6
*/
    Texture3D texture = new Texture3D(Texture.BASE_LEVEL, Texture.RGBA,
                          texture_width, texture_height, texture_depth);
    ImageComponent3D image3d =
      new ImageComponent3D(ImageComponent.FORMAT_RGBA, texture_width,
                           texture_height, texture_depth);
    for (int i=0; i<texture_depth; i++) {
      image3d.set(i, images[i]);
      images[i] = null; // take out the garbage
    }
    texture.setImage(0, image3d);
    //
    // from TextureLoader
    // TextureLoader uses 3 for both setMinFilter and setMagFilter
/*
System.out.println("Texture.FASTEST = " + Texture.FASTEST); // 0
System.out.println("Texture.NICEST = " + Texture.NICEST); // 1
System.out.println("Texture.BASE_LEVEL_POINT = " + Texture.BASE_LEVEL_POINT); // 2
System.out.println("Texture.BASE_LEVEL_LINEAR = " + Texture.BASE_LEVEL_LINEAR); // 3
*/
/* for interpolation:
    texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
    texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
*/
    texture.setMinFilter(Texture.BASE_LEVEL_POINT);
    texture.setMagFilter(Texture.BASE_LEVEL_POINT);
    texture.setEnable(true);
    // end of from TextureLoader

    // OK to share appearance ??
    Shape3D shapeX = new Shape3D(geometryX, appearance);
    Shape3D shapeY = new Shape3D(geometryY, appearance);
    Shape3D shapeZ = new Shape3D(geometryZ, appearance);
    appearance.setTexture(texture);

    Switch swit = (Switch) makeSwitch();
    swit.addChild(shapeX);
    swit.addChild(shapeY);
    swit.addChild(shapeZ);
    ((Group) group).addChild(swit);

    ProjectionControlJ3D control =
      (ProjectionControlJ3D) display.getProjectionControl();
    control.addPair(swit, renderer);
  }

  public Object makeSwitch() {
    Switch swit = new Switch();
    swit.setCapability(Switch.ALLOW_SWITCH_READ);
    swit.setCapability(Switch.ALLOW_SWITCH_WRITE);
    swit.setCapability(BranchGroup.ALLOW_DETACH);
    swit.setCapability(Group.ALLOW_CHILDREN_READ);
    swit.setCapability(Group.ALLOW_CHILDREN_WRITE);
    return swit;
  }

  public Object makeBranch() {
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    return branch;
  }

  public void addToSwitch(Object swit, Object branch)
         throws VisADException {
/* WLH 18 Aug 98
   empty BranchGroup or Shape3D may cause NullPointerException
   from Shape3DRetained.setLive
*/
    ensureNotEmpty((BranchGroup) branch);
    ((Switch) swit).addChild((BranchGroup) branch);
  }

  public void addSwitch(Object group, Object swit, Control control,
                        Set domain_set, DataRenderer renderer)
         throws VisADException {
    ((AVControlJ3D) control).addPair((Switch) swit, domain_set, renderer);
    ((AVControlJ3D) control).init();
    ((Group) group).addChild((Switch) swit);
  }

  public boolean recurseRange(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    return Range.doTransform((Group) group, data, value_array,
                             default_values, renderer);
  }

  public boolean wantIndexed() {
/* doesn't seem to matter to memory use
    return true;
*/
    return false;
  }


  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Group group) throws VisADException {
    if (((ShadowFunctionOrSetType) adaptedShadowType).getFlat()) {
      int LevelOfDifficulty = getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
/*
        Group data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowFunctionOrSetTypeJ3D.postProcess");
      }
      else {
        // includes !isTerminal
        // nothing to do
      }
    }
    else {
      if (this instanceof ShadowFunctionTypeJ3D) {
        Range.postProcess(group);
      }
    }
    AccumulationVector.removeAllElements();
  }

}

