//
// ShadowTextureFillSetTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bom;

import visad.*;
import visad.java3d.*;

import org.jogamp.java3d.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferInt;

/**
   The ShadowTextureFillSetTypeJ3D class shadows the FunctionType class for
   TextureFillRendererJ3D, within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowTextureFillSetTypeJ3D extends ShadowSetTypeJ3D {

  private static final int MISSING1 = Byte.MIN_VALUE;      // least byte

  private DisplayImplJ3D display = null;

  public ShadowTextureFillSetTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
    display = (DisplayImplJ3D) link.getDisplay();
  }

  // transform data into a depiction under group
  public boolean doTransform(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {

    DataDisplayLink link = renderer.getLink();

// System.out.println("start doTransform " + (System.currentTimeMillis() - link.start_time));

    // return if data is missing or no ScalarMaps
    if (data.isMissing()) {
      ((ImageRendererJ3D) renderer).markMissingVisADBranch();
      return false;
    }
    if (getLevelOfDifficulty() == NOTHING_MAPPED) return false;

    ShadowFunctionOrSetType adaptedShadowType =
      (ShadowFunctionOrSetType) getAdaptedShadowType();
    DisplayImpl display = getDisplay();
    GraphicsModeControl mode = (GraphicsModeControl)
      display.getGraphicsModeControl().clone();

    // get 'shape' flags
    boolean anyContour = adaptedShadowType.getAnyContour();
    boolean anyFlow = adaptedShadowType.getAnyFlow();
    boolean anyShape = adaptedShadowType.getAnyShape();
    boolean anyText = adaptedShadowType.getAnyText();

    if (anyContour || anyFlow || anyShape || anyText) {
      throw new BadMappingException("no contour, flow, shape or text allowed");
    }

    // get some precomputed values useful for transform
    // length of ValueArray
    int valueArrayLength = display.getValueArrayLength();
    // mapping from ValueArray to DisplayScalar
    int[] valueToScalar = display.getValueToScalar();
    // mapping from ValueArray to MapVector
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

    // array to hold values for various mappings
    float[][] display_values = new float[valueArrayLength][];

    int[] inherited_values = adaptedShadowType.getInheritedValues();

    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0) {
        display_values[i] = new float[1];
        display_values[i][0] = value_array[i];
      }
    }

    // analyze data's domain (its a Field)
    Set domain_set = (Set) data;
    Unit[] dataUnits = domain_set.getSetUnits();
    CoordinateSystem dataCoordinateSystem = domain_set.getCoordinateSystem();

    float[][] domain_values = null;
    double[][] domain_doubles = null;
    ShadowRealTupleType Domain = adaptedShadowType.getDomain();
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length;
    int domain_dimension;
    try {
      domain_length = domain_set.getLength();
      domain_dimension = domain_set.getDimension();
    }
    catch (SetException e) {
      return false;
    }

    // ShadowRealTypes of Domain
    ShadowRealType[] DomainComponents = adaptedShadowType.getDomainComponents();

    if (!adaptedShadowType.getIsTerminal()) {
      throw new DisplayException("not Terminal");
    }

    // check domain and determine whether it is square or curved texture
    if (!Domain.getAllSpatial() || Domain.getMultipleDisplayScalar()) {
      throw new BadMappingException("domain must be only spatial");
    }

    // get and process domain values
    float[][] spline_domain = domain_set.getSamples();

    spline_domain = 
        Unit.convertTuple(spline_domain, dataUnits, domain_units, false);

    // transform for any CoordinateSystem in data (Field) Domain
    ShadowRealTupleType domain_reference = Domain.getReference();

    ShadowRealType[] DC = DomainComponents;
    if (domain_reference != null &&
        domain_reference.getMappedDisplayScalar()) {
      RealTupleType ref = (RealTupleType) domain_reference.getType();
      renderer.setEarthSpatialData(Domain, domain_reference, ref,
                  ref.getDefaultUnits(), (RealTupleType) Domain.getType(),
                  new CoordinateSystem[] {dataCoordinateSystem},
                  domain_units);

      spline_domain =
        CoordinateSystem.transformCoordinates(
          ref, null, ref.getDefaultUnits(), null,
          (RealTupleType) Domain.getType(), dataCoordinateSystem,
          domain_units, null, spline_domain);
      // ShadowRealTypes of DomainReference
      DC = adaptedShadowType.getDomainReferenceComponents();
    }
    else {
      RealTupleType ref = (domain_reference == null) ? null :
                          (RealTupleType) domain_reference.getType();
      Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
      renderer.setEarthSpatialData(Domain, domain_reference, ref,
                  ref_units, (RealTupleType) Domain.getType(),
                  new CoordinateSystem[] {dataCoordinateSystem},
                  domain_units);
    }

    int[] tuple_index = new int[3];
    int[] spatial_value_indices = {-1, -1, -1};
    ScalarMap[] spatial_maps = new ScalarMap[3];

    DisplayTupleType spatial_tuple = null;
    for (int i=0; i<DC.length; i++) {
      Enumeration maps =
        DC[i].getSelectedMapVector().elements();
      ScalarMap map = (ScalarMap) maps.nextElement();
      DisplayRealType real = map.getDisplayScalar();
      spatial_tuple = real.getTuple();
      if (spatial_tuple == null) {
        throw new DisplayException("texture with bad tuple: " +
                                   "ShadowTextureFillSetTypeJ3D.doTransform");
      }
      // get spatial index
      tuple_index[i] = real.getTupleIndex();
      spatial_value_indices[tuple_index[i]] = map.getValueIndex();
      spatial_maps[tuple_index[i]] = map;
      if (maps.hasMoreElements()) {
        throw new DisplayException("texture with multiple spatial: " +
                                   "ShadowTextureFillSetTypeJ3D.doTransform");
      }
    } // end for (int i=0; i<DC.length; i++)
    // get spatial index not mapped from domain_set
    tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
    DisplayRealType real =
      (DisplayRealType) spatial_tuple.getComponent(tuple_index[2]);
    int value2_index = display.getDisplayScalarIndex(real);
    float value2 = default_values[value2_index];
    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0 &&
          real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
        value2 = value_array[i];
        break;
      }
    }

    float[][] spatial_values = new float[3][];
    spatial_values[tuple_index[0]] = spline_domain[0];
    spatial_values[tuple_index[1]] = spline_domain[1];
    spatial_values[tuple_index[2]] = new float[domain_length];
    for (int i=0; i<domain_length; i++) spatial_values[tuple_index[2]][i] = value2;

    for (int i=0; i<3; i++) {
      if (spatial_maps[i] != null) {
        spatial_values[i] = spatial_maps[i].scaleValues(spatial_values[i]);
      }
    }

    float scale = ((TextureFillRendererJ3D) renderer).getScale();
    // compute texture coordinates from Cartesian spatial coordinates
    float[][] tex_values = new float[3][domain_length];
    for (int i = 0; i<domain_length; i++) {
      tex_values[0][i] = scale * spatial_values[tuple_index[0]][i];
      tex_values[1][i] = scale * spatial_values[tuple_index[1]][i];
      tex_values[2][i] = scale * spatial_values[tuple_index[2]][i];
    }

    if (spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
// inside 'if (anyFlow) {}' in ShadowType.assembleSpatial()
      renderer.setEarthSpatialDisplay(null, spatial_tuple, display,
               spatial_value_indices, default_values, null);
    }
    else {
      CoordinateSystem coord = spatial_tuple.getCoordinateSystem();
      spatial_values = coord.toReference(spatial_values);
      // float[][] new_spatial_values = coord.toReference(spatial_values);
      // for (int i=0; i<3; i++) spatial_values[i] = new_spatial_values[i];

// inside 'if (anyFlow) {}' in ShadowType.assembleSpatial()
      renderer.setEarthSpatialDisplay(coord, spatial_tuple, display,
               spatial_value_indices, default_values, null);
    }

    // make spatial set for making VisADGeometryArray using make2DGeometry()
    SetType type = new SetType(Display.DisplaySpatialCartesianTuple);
    Set spatial_set = makeSpatialSet(domain_set, type, spatial_values);

    // make spatial set with texture coordinates for making a VisADGeometryArray
    // so texture coordinates can line up with coordinates
    Set tex_set = makeSpatialSet(domain_set, type, tex_values);

    boolean indexed = wantIndexed();
    byte[][] color_values = null;
    VisADGeometryArray array = spatial_set.make2DGeometry(color_values, indexed);
    VisADGeometryArray tex_array = tex_set.make2DGeometry(color_values, indexed);

    float[] coordinates = array.coordinates;
    float[] tex = tex_array.coordinates;
    int nn = coordinates.length / 3;
    float[] texCoords = new float[2 * nn];
    boolean spatial_all_select = true;
    for (int i=0; i<3*nn; i++) {
      if (coordinates[i] != coordinates[i]) spatial_all_select = false;
    }
    int j = 0;
    for (int i=0; i<3*nn; i+=3) {
      texCoords[j] = tex[i];
      texCoords[j+1] = tex[i+1];
      j += 2;
    }
    array.texCoords = texCoords;

    // do surgery to remove any missing spatial coordinates in texture
    if (!spatial_all_select) {
      array = (VisADTriangleStripArray) array.removeMissing();
    }

    // do surgery along any longitude split (e.g., date line) in texture
    if (adaptedShadowType.getAdjustProjectionSeam()) {
      array = (VisADTriangleStripArray) array.adjustLongitude(renderer);
      array = (VisADTriangleStripArray) array.adjustSeam(renderer);
    }

// System.out.println("start createImage " + (System.currentTimeMillis() - link.start_time));

    int texture_width = ((TextureFillRendererJ3D) renderer).getTextureWidth();
    int texture_height = ((TextureFillRendererJ3D) renderer).getTextureHeight();
    int[] color_ints = ((TextureFillRendererJ3D) renderer).getTexture();
    // create BufferedImage for texture from color_ints
    BufferedImage image = createImage(texture_width, texture_height, texture_width,
                                      texture_height, color_ints);

// System.out.println("start textureToGroup " + (System.currentTimeMillis() - link.start_time));

    // add texture as sub-node of group in scene graph
    textureToGroup(group, array, image, mode, texture_width, texture_height, renderer);

// System.out.println("end curved texture " + (System.currentTimeMillis() - link.start_time));


    ensureNotEmpty(group);
    return false;
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            int texture_width, int texture_height,
                            DataRenderer renderer)
         throws VisADException {
    GeometryArray geometry = display.makeGeometry(array);
    // System.out.println("texture geometry");
    // create basic Appearance
    Appearance appearance =
      makeAppearance(mode, null, null, geometry, false);
    // create TextureAttributes
    TextureAttributes texture_attributes = new TextureAttributes();

    // WLH 20 June 2001
    texture_attributes.setTextureMode(TextureAttributes.REPLACE);
    // texture_attributes.setTextureMode(TextureAttributes.MODULATE);

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
    texture.setCapability(Texture.ALLOW_IMAGE_READ);
    ImageComponent2D image2d =
      new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
    image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
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
    if (((TextureFillRendererJ3D) renderer).getSmooth()) {
      // for interpolation:
      texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
      texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
    }
    else {
      // for non-interpolation:
      texture.setMinFilter(Texture.BASE_LEVEL_POINT);
      texture.setMagFilter(Texture.BASE_LEVEL_POINT);
    }

    texture.setBoundaryModeS(Texture.WRAP);
    texture.setBoundaryModeT(Texture.WRAP);
    texture.setEnable(true);
    // end of from TextureLoader
    //
    Shape3D shape = new Shape3D(geometry, appearance);
    shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    appearance.setTexture(texture);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);

    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    branch.addChild(shape);
    if (((Group) group).numChildren() > 0) {
      ((Group) group).setChild(branch, 0);
    }
    else {
      ((Group) group).addChild(branch);
    }
  }

  public BufferedImage createImage(int data_width, int data_height,
                       int texture_width, int texture_height, int[] color_ints) {
    BufferedImage image = null;
    ColorModel colorModel = ColorModel.getRGBdefault();
    WritableRaster raster =
      colorModel.createCompatibleWritableRaster(texture_width, texture_height);
    image = new BufferedImage(colorModel, raster, false, null);
    int[] intData = ((DataBufferInt)raster.getDataBuffer()).getData();
    int k = 0;
    int m = 0;
    int r, g, b, a;
    for (int j=0; j<data_height; j++) {
      for (int i=0; i<data_width; i++) {
        intData[m++] = color_ints[k++];
      }
      for (int i=data_width; i<texture_width; i++) {
        intData[m++] = 0;
      }
    }
    for (int j=data_height; j<texture_height; j++) {
      for (int i=0; i<texture_width; i++) {
        intData[m++] = 0;
      }
    }
    return image;
  }

}

