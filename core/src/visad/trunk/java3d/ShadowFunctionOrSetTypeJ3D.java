
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

  public void adjustZ(float[] coordinates) {
    if (display.getDisplayRenderer().getMode2D()) {
      for (int i=2; i<coordinates.length; i+=3) {
        coordinates[i] = DisplayImplJ3D.BACK2D;
      }
/* WLH 27 March 99
      coordinates[2] = DisplayImplJ3D.BACK2D;
      coordinates[5] = DisplayImplJ3D.BACK2D;
      coordinates[8] = DisplayImplJ3D.BACK2D;
      coordinates[11] = DisplayImplJ3D.BACK2D;
*/
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

  public boolean addToGroup(Object group, VisADGeometryArray array,
                            GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color)
         throws VisADException {
    if (array != null && array.vertexCount > 0) {
      // MEM - for coordinates if mode2d
      GeometryArray geometry = display.makeGeometry(array);
      TransparencyAttributes c_alpha = null;
      if (constant_alpha == 0.0f) {
        // constant opaque alpha = NONE
        c_alpha = new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
      }
      else if (constant_alpha == constant_alpha) {
        c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
                                             constant_alpha);
      }
      else {
        c_alpha = new TransparencyAttributes(mode.getTransparencyMode(), 1.0f);
      }
      ColoringAttributes c_color = null;
      if (constant_color != null && constant_color.length == 3) {
        c_color = new ColoringAttributes();
        c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
      }
      Appearance appearance = makeAppearance(mode, c_alpha, c_color, geometry);
      Shape3D shape = new Shape3D(geometry, appearance);
      ((Group) group).addChild(shape);
      return true;
    }
    else {
      return false;
    }
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height)
         throws VisADException {
    GeometryArray geometry = display.makeGeometry(array);
/*
    int vertexFormat = GeometryArray.COORDINATES |
                       GeometryArray.NORMALS |
                       GeometryArray.COLOR_3 |
                       GeometryArray.TEXTURE_COORDINATE_2;
    QuadArray geometry = new QuadArray(4, vertexFormat);
    geometry.setCoordinates(0, coordinates);
    geometry.setNormals(0, normals);
    geometry.setTextureCoordinates(0, texCoords);
    geometry.setColors(0, colors);
*/
    // System.out.println("texture geometry");
    // create basic Appearance
    TransparencyAttributes c_alpha = null;
    if (constant_alpha == 0.0f) {
      // constant opaque alpha = NONE
      c_alpha = new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
    }
    else if (constant_alpha == constant_alpha) {
      c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
                                           constant_alpha);
    }
    ColoringAttributes c_color = null;
    if (constant_color != null && constant_color.length == 3) {
      c_color = new ColoringAttributes();
      c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
    }
    Appearance appearance =
      makeAppearance(mode, c_alpha, c_color, geometry);
    // appearance = makeAppearance(mode, null, null, geometry);
    // create TextureAttributes
    TextureAttributes texture_attributes = new TextureAttributes();
    texture_attributes.setTextureMode(TextureAttributes.REPLACE);
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

