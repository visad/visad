//
// ShadowImageFunctionTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.mcidas.BaseMapAdapter;
import visad.data.mcidas.AreaAdapter;
import visad.data.gif.GIFForm;

import javax.media.j3d.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

import java.net.URL;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferInt;

/**
   The ShadowImageFunctionTypeJ3D class shadows the FunctionType class for
   ImageRendererJ3D, within a DataDisplayLink, under Java3D.<P>
*/
public class ShadowImageFunctionTypeJ3D extends ShadowFunctionTypeJ3D {

  private static final int MISSING1 = Byte.MIN_VALUE;      // least byte

  public ShadowImageFunctionTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
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

    if (!mode.getTextureEnable()) {
      return super.doTransform(group, data, value_array, default_values,
                               renderer);
    }

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
    Set domain_set = ((Field) data).getDomainSet();
    Unit[] dataUnits = ((Function) data).getDomainUnits();
    CoordinateSystem dataCoordinateSystem =
      ((Function) data).getDomainCoordinateSystem();

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

    // check whether this ShadowImageFunctionTypeJ3D is for an
    // image (terminal - no recursive calls to doTransform) or an
    // image sequence (non-terminal - recursive calls to doTransform
    // for each image in the sequence
    if (adaptedShadowType.getIsTerminal()) {
      // terminal, so this is an image

// System.out.println("start colors " + (System.currentTimeMillis() - link.start_time));

      float constant_alpha = Float.NaN;
      float[] constant_color = null;

      // check that range is single RealType mapped to RGB only
      ShadowRealType[] RangeComponents = adaptedShadowType.getRangeComponents();
      int rangesize = RangeComponents.length;
      if (rangesize != 1 && rangesize != 3) {
        throw new BadMappingException("image values must single or triple");
      }
      ScalarMap cmap  = null;
      ScalarMap[] cmaps = null;
      int[] permute = {-1, -1, -1};
      int color_length = 3;
      if (rangesize == 1) {
        Vector mvector = RangeComponents[0].getSelectedMapVector();
        if (mvector.size() != 1) {
          throw new BadMappingException("image values must be mapped to RGB only");
        }
        cmap = (ScalarMap) mvector.elementAt(0);
        if (Display.RGB.equals(cmap.getDisplayScalar())) {
          color_length = 3;
        }
        else if (Display.RGBA.equals(cmap.getDisplayScalar())) {
          color_length = 4;
        }
        else {
          throw new BadMappingException("image values must be mapped to RGB or RGBA");
        }
      }
      else {
        cmaps = new ScalarMap[3];
        for (int i=0; i<3; i++) {
          Vector mvector = RangeComponents[i].getSelectedMapVector();
          if (mvector.size() != 1) {
            throw new BadMappingException("image values must be mapped to color only");
          }
          cmaps[i] = (ScalarMap) mvector.elementAt(0);
          if (Display.Red.equals(cmaps[i].getDisplayScalar())) {
            permute[0] = i;
          }
          else if (Display.Green.equals(cmaps[i].getDisplayScalar())) {
            permute[1] = i;
          }
          else if (Display.Blue.equals(cmaps[i].getDisplayScalar())) {
            permute[2] = i;
          }
          else {
            throw new BadMappingException("image values must be mapped to Red, " +
                                          "Green or Blue only");
          }
        }
        if (permute[0] < 0 || permute[1] < 0 || permute[2] < 0) {
          throw new BadMappingException("image values must be mapped to Red, " +
                                        "Green and Blue");
        }
      }

      constant_alpha =
        default_values[display.getDisplayScalarIndex(Display.Alpha)];

      int[] color_ints = new int[domain_length];
      if (cmap != null) {
        // build texture colors in color_ints array
        BaseColorControl control = (BaseColorControl) cmap.getControl();
        float[][] table = control.getTable();
        byte[][] bytes = null;
        Set rset = null;
        boolean is_default_unit = false;
        if (data instanceof FlatField) {
          // for fast byte color lookup, need:
          // 1. range data values are packed in bytes
          bytes = ((FlatField) data).grabBytes();
          // 2. range set is Linear1DSet
          Set[] rsets = ((FlatField) data). getRangeSets();
          if (rsets != null) rset = rsets[0];
          // 3. data Unit equals default Unit
          RealType rtype = (RealType) RangeComponents[0].getType();
          Unit def_unit = rtype.getDefaultUnit();
          if (def_unit == null) {
            is_default_unit = true;
          }
          else {
            Unit[][] data_units = ((FlatField) data).getRangeUnits();
            Unit data_unit = (data_units == null) ? null : data_units[0][0];
            is_default_unit = def_unit.equals(data_unit);
          }
        }
        if (table != null) {
          // combine color table RGB components into ints
          int[] itable = new int[table[0].length];
          // int r, g, b, a = 255;
          int r, g, b;
          int c = (int) (255.0 * (1.0f - constant_alpha));
          int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          for (int j=0; j<table[0].length; j++) {
            c = (int) (255.0 * table[0][j]);
            r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
            c = (int) (255.0 * table[1][j]);
            g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
            c = (int) (255.0 * table[2][j]);
            b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
            if (color_length == 4) {
              c = (int) (255.0 * table[3][j]);
              a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
            }
            itable[j] = ((a << 24) | (r << 16) | (g << 8) | b);
          }
          int tblEnd = table[0].length - 1;
          // get scale for color table
          double table_scale = (double) table[0].length;
  
          if (bytes != null && bytes[0] != null && is_default_unit &&
              rset != null && rset instanceof Linear1DSet) {
            // fast since FlatField with bytes, data Unit equals default
            // Unit and range set is Linear1DSet
            // get "scale and offset" for Linear1DSet
            double first = ((Linear1DSet) rset).getFirst();
            double step = ((Linear1DSet) rset).getStep();
            // get scale and offset for ScalarMap
            double[] so = new double[2];
            double[] da = new double[2];
            double[] di = new double[2];
            cmap.getScale(so, da, di);
            double scale = so[0];
            double offset = so[1];
            // combine scales and offsets for Set, ScalarMap and color table
            float mult = (float) (table_scale * scale * step);
            float add = (float) (table_scale * (offset + scale * first));
  
            // build table for fast color lookup
            int[] fast_table = new int[256];
            for (int j=0; j<256; j++) {
              int index = j - 1;
              if (index < 0) {
                fast_table[j] = 0; // missing
              }
              else {
                int k = (int) (add + mult * index);
                // clip to table
                fast_table[j] =
                  (k < 0) ? itable[0] : ((k > tblEnd) ? itable[tblEnd] : itable[k]);
              }
            }
  
            // now do fast lookup from byte values to color ints
            byte[] bytes0 = bytes[0];
            for (int i=0; i<domain_length; i++) {
              color_ints[i] = fast_table[((int) bytes0[i]) - MISSING1];
            }
            bytes = null; // take out the garbage
          }
          else {
            // medium speed way to build texture colors
            bytes = null; // take out the garbage
            float[][] values = ((Field) data).getFloats(false);
            values[0] = cmap.scaleValues(values[0]);
  
            // now do fast lookup from byte values to color ints
            float[] values0 = values[0];
            for (int i=0; i<domain_length; i++) {
              if (values0[i] != values0[i]) {
                color_ints[i] = 0; // missing
              }
              else {
                int j = (int) (table_scale * values0[i]);
                // clip to table
                color_ints[i] =
                  (j < 0) ? itable[0] : ((j > tblEnd) ? itable[tblEnd] : itable[j]);
              }
            }
            values = null; // take out the garbage
          }
        }
        else { // if (table == null)
          // slower, more general way to build texture colors
          bytes = null; // take out the garbage
          float[][] values = ((Field) data).getFloats(false);
          values[0] = cmap.scaleValues(values[0]);
          // call lookupValues which will use function since table == null
          float[][] color_values = control.lookupValues(values[0]);
          // combine color RGB components into ints
          // int r, g, b, a = 255;
          int r, g, b;
          int c = (int) (255.0 * (1.0f - constant_alpha));
          int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
          for (int i=0; i<domain_length; i++) {
            if (values[0][i] != values[0][i]) {
              color_ints[i] = 0; // missing
            }
            else {
              c = (int) (255.0 * color_values[0][i]);
              r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
              c = (int) (255.0 * color_values[1][i]);
              g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
              c = (int) (255.0 * color_values[2][i]);
              b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
              if (color_length == 4) {
                c = (int) (255.0 * color_values[3][i]);
                a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
              }
              color_ints[i] = ((a << 24) | (r << 16) | (g << 8) | b);
            }
          }
          // take out the garbage
          values = null;
          color_values = null;
        }
      }
      else if (cmaps != null) {
        float[][] values = ((Field) data).getFloats(false);
        float[][] new_values = new float[3][];
        new_values[0] = cmaps[permute[0]].scaleValues(values[permute[0]]);
        new_values[1] = cmaps[permute[1]].scaleValues(values[permute[1]]);
        new_values[2] = cmaps[permute[2]].scaleValues(values[permute[2]]);
        values = new_values;
        int r, g, b;
        int c = (int) (255.0 * (1.0f - constant_alpha));
        int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
        for (int i=0; i<domain_length; i++) {
          if (values[0][i] != values[0][i] ||
              values[1][i] != values[1][i] ||
              values[2][i] != values[2][i]) {
            color_ints[i] = 0; // missing
          }
          else {
            c = (int) (255.0 * values[0][i]);
            r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
            c = (int) (255.0 * values[1][i]);
            g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
            c = (int) (255.0 * values[2][i]);
            b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
            color_ints[i] = ((a << 24) | (r << 16) | (g << 8) | b);
          }
        }
        // take out the garbage
        values = null;
      }
      else {
        throw new BadMappingException("cmap == null and cmaps == null ??");
      }

// System.out.println("end colors " + (System.currentTimeMillis() - link.start_time));

      // check domain and determine whether it is square or curved texture
      if (!Domain.getAllSpatial() || Domain.getMultipleDisplayScalar()) {
        throw new BadMappingException("domain must be only spatial");
      }

      boolean isTextureMap = adaptedShadowType.getIsTextureMap() &&
                             (domain_set instanceof Linear2DSet ||
                              (domain_set instanceof LinearNDSet &&
                               domain_set.getDimension() == 2)) &&
                             (domain_set.getManifoldDimension() == 2);

      // DRM 2003-08-21
      //int curved_size = display.getGraphicsModeControl().getCurvedSize();
      int cMapCurveSize = (int)
        default_values[display.getDisplayScalarIndex(Display.CurvedSize)];
      int curved_size =  
        (cMapCurveSize > 0)
           ? cMapCurveSize
           : display.getGraphicsModeControl().getCurvedSize();
      boolean curvedTexture = adaptedShadowType.getCurvedTexture() &&
                              !isTextureMap &&
                              curved_size > 0 &&
                              (domain_set instanceof Gridded2DSet ||
                               (domain_set instanceof GriddedSet &&
                                domain_set.getDimension() == 2)) &&
                              (domain_set.getManifoldDimension() == 2);

      float[] coordinates = null;
      float[] texCoords = null;
      float[] normals = null;
      byte[] colors = null;
      int data_width = 0;
      int data_height = 0;
      int texture_width = 1;
      int texture_height = 1;
      float[] coordinatesX = null;
      float[] texCoordsX = null;
      float[] normalsX = null;
      byte[] colorsX = null;
      float[] coordinatesY = null;
      float[] texCoordsY = null;
      float[] normalsY = null;
      byte[] colorsY = null;

      if (color_length == 4) constant_alpha = Float.NaN; // WLH 6 May 2003

      if (isTextureMap) {

// System.out.println("start texture map " + (System.currentTimeMillis() - link.start_time));

        Linear1DSet X = null;
        Linear1DSet Y = null;
        if (domain_set instanceof Linear2DSet) {
          X = ((Linear2DSet) domain_set).getX();
          Y = ((Linear2DSet) domain_set).getY();
        }
        else {
          X = ((LinearNDSet) domain_set).getLinear1DComponent(0);
          Y = ((LinearNDSet) domain_set).getLinear1DComponent(1);
        }
        float[][] limits = new float[2][2];
        limits[0][0] = (float) X.getFirst();
        limits[0][1] = (float) X.getLast();
        limits[1][0] = (float) Y.getFirst();
        limits[1][1] = (float) Y.getLast();

        // get domain_set sizes
        data_width = X.getLength();
        data_height = Y.getLength();
        // texture sizes must be powers of two
        texture_width = textureWidth(data_width);
        texture_height = textureHeight(data_height);


        // WLH 27 Jan 2003
        float half_width = 0.5f / ((float) (data_width - 1));
        float half_height = 0.5f / ((float) (data_height - 1));
        half_width = (limits[0][1] - limits[0][0]) * half_width;
        half_height = (limits[1][1] - limits[1][0]) * half_height;
        limits[0][0] -= half_width;
        limits[0][1] += half_width;
        limits[1][0] -= half_height;
        limits[1][1] += half_height;


        // convert values to default units (used in display)
        limits = Unit.convertTuple(limits, dataUnits, domain_units);

        int[] tuple_index = new int[3];
        if (DomainComponents.length != 2) {
          throw new DisplayException("texture domain dimension != 2:" +
                                     "ShadowFunctionOrSetType.doTransform");
        }

        // find the spatial ScalarMaps
        for (int i=0; i<DomainComponents.length; i++) {
          Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();
          ScalarMap map = (ScalarMap) maps.nextElement();
          // scale values
          limits[i] = map.scaleValues(limits[i]);
          DisplayRealType real = map.getDisplayScalar();
          DisplayTupleType tuple = real.getTuple();
          if (tuple == null ||
              !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
            throw new DisplayException("texture with bad tuple: " +
                                       "ShadowFunctionOrSetType.doTransform");
          }
          // get spatial index
          tuple_index[i] = real.getTupleIndex();
          if (maps.hasMoreElements()) {
            throw new DisplayException("texture with multiple spatial: " +
                                       "ShadowFunctionOrSetType.doTransform");
          }
        } // end for (int i=0; i<DomainComponents.length; i++)
        // get spatial index not mapped from domain_set
        tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
        DisplayRealType real = (DisplayRealType)
          Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
        int value2_index = display.getDisplayScalarIndex(real);
        float value2 = default_values[value2_index];
        for (int i=0; i<valueArrayLength; i++) {
          if (inherited_values[i] > 0 &&
              real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
            // value for unmapped spatial dimension
            value2 = value_array[i];
            break;
          }
        }

        // create VisADQuadArray that texture is mapped onto
        coordinates = new float[12];
        // corner 0
        coordinates[tuple_index[0]] = limits[0][0];
        coordinates[tuple_index[1]] = limits[1][0];
        coordinates[tuple_index[2]] = value2;
        // corner 1
        coordinates[3 + tuple_index[0]] = limits[0][1];
        coordinates[3 + tuple_index[1]] = limits[1][0];
        coordinates[3 + tuple_index[2]] = value2;
        // corner 2
        coordinates[6 + tuple_index[0]] = limits[0][1];
        coordinates[6 + tuple_index[1]] = limits[1][1];
        coordinates[6 + tuple_index[2]] = value2;
        // corner 3
        coordinates[9 + tuple_index[0]] = limits[0][0];
        coordinates[9 + tuple_index[1]] = limits[1][1];
        coordinates[9 + tuple_index[2]] = value2;

        // move image back in Java3D 2-D mode
        adjustZ(coordinates);

        texCoords = new float[8];
        float ratiow = ((float) data_width) / ((float) texture_width);
        float ratioh = ((float) data_height) / ((float) texture_height);
        setTexCoords(texCoords, ratiow, ratioh);

        normals = new float[12];
        float n0 = ((coordinates[3+2]-coordinates[0+2]) *
                    (coordinates[6+1]-coordinates[0+1])) -
                   ((coordinates[3+1]-coordinates[0+1]) *
                    (coordinates[6+2]-coordinates[0+2]));
        float n1 = ((coordinates[3+0]-coordinates[0+0]) *
                    (coordinates[6+2]-coordinates[0+2])) -
                   ((coordinates[3+2]-coordinates[0+2]) *
                    (coordinates[6+0]-coordinates[0+0]));
        float n2 = ((coordinates[3+1]-coordinates[0+1]) *
                    (coordinates[6+0]-coordinates[0+0])) -
                   ((coordinates[3+0]-coordinates[0+0]) *
                    (coordinates[6+1]-coordinates[0+1]));

        float nlen = (float) Math.sqrt(n0 *  n0 + n1 * n1 + n2 * n2);
        n0 = n0 / nlen;
        n1 = n1 / nlen;
        n2 = n2 / nlen;

        // corner 0
        normals[0] = n0;
        normals[1] = n1;
        normals[2] = n2;
        // corner 1
        normals[3] = n0;
        normals[4] = n1;
        normals[5] = n2;
        // corner 2
        normals[6] = n0;
        normals[7] = n1;
        normals[8] = n2;
        // corner 3
        normals[9] = n0;
        normals[10] = n1;
        normals[11] = n2;

        colors = new byte[12];
        for (int i=0; i<12; i++) colors[i] = (byte) 127;

        VisADQuadArray qarray = new VisADQuadArray();
        qarray.vertexCount = 4;
        qarray.coordinates = coordinates;
        qarray.texCoords = texCoords;
        qarray.colors = colors;
        qarray.normals = normals;

// System.out.println("start createImage " + (System.currentTimeMillis() - link.start_time));

        // create BufferedImage for texture from color_ints
        BufferedImage image = createImage(data_width, data_height, texture_width,
                                          texture_height, color_ints);

// System.out.println("start textureToGroup " + (System.currentTimeMillis() - link.start_time));

        // add texture as sub-node of group in scene graph
        textureToGroup(group, qarray, image, mode, constant_alpha,
                       constant_color, texture_width, texture_height);

// System.out.println("end texture map " + (System.currentTimeMillis() - link.start_time));

      } // end if (isTextureMap)
      else if (curvedTexture) {

// System.out.println("start curved texture " + (System.currentTimeMillis() - link.start_time));

        // get domain_set sizes
        int[] lengths = ((GriddedSet) domain_set).getLengths();
        data_width = lengths[0];
        data_height = lengths[1];
        // texture sizes must be powers of two
        texture_width = textureWidth(data_width);
        texture_height = textureHeight(data_height);

        // compute size of triangle array to mapped texture onto
        int size = (data_width + data_height) / 2;
        curved_size = Math.max(2, Math.min(curved_size, size / 32));

        int nwidth = 2 + (data_width - 1) / curved_size;
        int nheight = 2 + (data_height - 1) / curved_size;

        // compute locations of triangle vertices in texture
        int nn = nwidth * nheight;
        int[] is = new int[nwidth];
        int[] js = new int[nheight];
        for (int i=0; i<nwidth; i++) {
          is[i] = Math.min(i * curved_size, data_width - 1);
        }
        for (int j=0; j<nheight; j++) {
          js[j] = Math.min(j * curved_size, data_height - 1);
        }

        // get spatial coordinates at triangle vertices
        int[] indices = new int[nn];
        int k=0;
        for (int j=0; j<nheight; j++) {
          for (int i=0; i<nwidth; i++) {
            indices[k] = is[i] + data_width * js[j];
            k++;
          }
        }
        float[][] spline_domain = domain_set.indexToValue(indices);
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
                                       "ShadowImageFunctionTypeJ3D.doTransform");
          }
          // get spatial index
          tuple_index[i] = real.getTupleIndex();
          spatial_value_indices[tuple_index[i]] = map.getValueIndex();
          spatial_maps[tuple_index[i]] = map;
          if (maps.hasMoreElements()) {
            throw new DisplayException("texture with multiple spatial: " +
                                       "ShadowImageFunctionTypeJ3D.doTransform");
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
        spatial_values[tuple_index[2]] = new float[nn];
        for (int i=0; i<nn; i++) spatial_values[tuple_index[2]][i] = value2;

        for (int i=0; i<3; i++) {
          if (spatial_maps[i] != null) {
            spatial_values[i] = spatial_maps[i].scaleValues(spatial_values[i]);
          }
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

        // break from ShadowFunctionOrSetType

        coordinates = new float[3 * nn];
        k = 0;
        for (int i=0; i<nn; i++) {
          coordinates[k++] = spatial_values[0][i];
          coordinates[k++] = spatial_values[1][i];
          coordinates[k++] = spatial_values[2][i];
        }

        boolean spatial_all_select = true;
        for (int i=0; i<3*nn; i++) {
          if (coordinates[i] != coordinates[i]) spatial_all_select = false;
        }

        normals = Gridded3DSet.makeNormals(coordinates, nwidth, nheight);
        colors = new byte[3 * nn];
        for (int i=0; i<3*nn; i++) colors[i] = (byte) 127;

        float ratiow = ((float) data_width) / ((float) texture_width);
        float ratioh = ((float) data_height) / ((float) texture_height);

        // WLH 27 Jan 2003
        float half_width = 0.5f / ((float) texture_width);
        float half_height = 0.5f / ((float) texture_height);
        float width = 1.0f / ((float) texture_width);
        float height = 1.0f / ((float) texture_height);

        int mt = 0;
        texCoords = new float[2 * nn];
        for (int j=0; j<nheight; j++) {
          for (int i=0; i<nwidth; i++) {
            //texCoords[mt++] = ratiow * is[i] / (data_width - 1.0f);
            //texCoords[mt++] = 1.0f - ratioh * js[j] / (data_height - 1.0f);

            // WLH 27 Jan 2003
            float isfactor = is[i] / (data_width - 1.0f);
            float jsfactor = js[j] / (data_height - 1.0f);
            texCoords[mt++] = (ratiow - width) * isfactor + half_width;
            texCoords[mt++] = 1.0f - (ratioh - height) * jsfactor - half_height;
          }
        }

        VisADTriangleStripArray tarray = new VisADTriangleStripArray();
        tarray.stripVertexCounts = new int[nheight - 1];
        for (int i=0; i<nheight - 1; i++) {
          tarray.stripVertexCounts[i] = 2 * nwidth;
        }
        int len = (nheight - 1) * (2 * nwidth);
        tarray.vertexCount = len;
        tarray.normals = new float[3 * len];
        tarray.coordinates = new float[3 * len];
        tarray.colors = new byte[3 * len];
        tarray.texCoords = new float[2 * len];

        // shuffle normals into tarray.normals, etc
        k = 0;
        int kt = 0;
        int nwidth3 = 3 * nwidth;
        int nwidth2 = 2 * nwidth;
        for (int i=0; i<nheight-1; i++) {
          int m = i * nwidth3;
          mt = i * nwidth2;
          for (int j=0; j<nwidth; j++) {
            tarray.coordinates[k] = coordinates[m];
            tarray.coordinates[k+1] = coordinates[m+1];
            tarray.coordinates[k+2] = coordinates[m+2];
            tarray.coordinates[k+3] = coordinates[m+nwidth3];
            tarray.coordinates[k+4] = coordinates[m+nwidth3+1];
            tarray.coordinates[k+5] = coordinates[m+nwidth3+2];

            tarray.normals[k] = normals[m];
            tarray.normals[k+1] = normals[m+1];
            tarray.normals[k+2] = normals[m+2];
            tarray.normals[k+3] = normals[m+nwidth3];
            tarray.normals[k+4] = normals[m+nwidth3+1];
            tarray.normals[k+5] = normals[m+nwidth3+2];

            tarray.colors[k] = colors[m];
            tarray.colors[k+1] = colors[m+1];
            tarray.colors[k+2] = colors[m+2];
            tarray.colors[k+3] = colors[m+nwidth3];
            tarray.colors[k+4] = colors[m+nwidth3+1];
            tarray.colors[k+5] = colors[m+nwidth3+2];

            tarray.texCoords[kt] = texCoords[mt];
            tarray.texCoords[kt+1] = texCoords[mt+1];
            tarray.texCoords[kt+2] = texCoords[mt+nwidth2];
            tarray.texCoords[kt+3] = texCoords[mt+nwidth2+1];

            k += 6;
            m += 3;
            kt += 4;
            mt += 2;
          }
        }

        // do surgery to remove any missing spatial coordinates in texture
        if (!spatial_all_select) {
          tarray = (VisADTriangleStripArray) tarray.removeMissing();
        }

        // do surgery along any longitude split (e.g., date line) in texture
        if (adaptedShadowType.getAdjustProjectionSeam()) {
          tarray = (VisADTriangleStripArray) tarray.adjustLongitude(renderer);
          tarray = (VisADTriangleStripArray) tarray.adjustSeam(renderer);
        }

// System.out.println("start createImage " + (System.currentTimeMillis() - link.start_time));

        // create BufferedImage for texture from color_ints
        BufferedImage image = createImage(data_width, data_height, texture_width,
                                          texture_height, color_ints);

// System.out.println("start textureToGroup " + (System.currentTimeMillis() - link.start_time));

        // add texture as sub-node of group in scene graph
        textureToGroup(group, tarray, image, mode, constant_alpha,
                       constant_color, texture_width, texture_height);

// System.out.println("end curved texture " + (System.currentTimeMillis() - link.start_time));

      } // end if (curvedTexture)
      else { // !isTextureMap && !curvedTexture
        throw new BadMappingException("must be texture map or curved texture map");
      }
    }
    else { // !adaptedShadowType.getIsTerminal()
      Vector domain_maps = DomainComponents[0].getSelectedMapVector();
      ScalarMap amap = null;
      if (domain_set.getDimension() == 1 && domain_maps.size() == 1) {
        ScalarMap map = (ScalarMap) domain_maps.elementAt(0);
        if (Display.Animation.equals(map.getDisplayScalar())) {
          amap = map;
        }
      }
      if (amap == null) {
        throw new BadMappingException("time must be mapped to Animation");
      }
      AnimationControlJ3D control = (AnimationControlJ3D) amap.getControl();

      // get any usable frames from the old scene graph
      Switch old_swit = null;
      BranchGroup[] old_nodes = null;
      double[] old_times = null;
      boolean[] old_mark = null;
      int old_len = 0;
      boolean reuse = ((ImageRendererJ3D) renderer).getReUseFrames();
      if (group instanceof BranchGroup &&
          ((BranchGroup) group).numChildren() > 0) {
        Node g = ((BranchGroup) group).getChild(0);
        if (g instanceof Switch) {
          old_swit = (Switch) g;

          old_len = old_swit.numChildren();
          if (old_len > 0) {
            old_nodes = new BranchGroup[old_len];
            for (int i=0; i<old_len; i++) {
              old_nodes[i] = (BranchGroup) old_swit.getChild(i);
            }
            // remove old_nodes from old_swit
            for (int i=0; i<old_len; i++) {
              old_nodes[i].detach();
            }
            old_times = new double[old_len];
            old_mark = new boolean[old_len];
            for (int i=0; i<old_len; i++) {
              old_mark[i] = false;
              if (old_nodes[i] instanceof VisADBranchGroup && reuse) {
                old_times[i] = ((VisADBranchGroup) old_nodes[i]).getTime();
              }
              else {
                old_times[i] = Double.NaN;
              }
            }
          }
        }
      } // end if (((BranchGroup) group).numChildren() > 0)

      // create frames for new scene graph
      // Set aset = control.getSet();
      // double[][] values = aset.getDoubles();
      double[][] values = domain_set.getDoubles();
      double[] times = values[0];
      int len = times.length;
      double delta = Math.abs((times[len-1] - times[0]) / (1000.0 * len));

      // create new Switch and make live
      // control.clearSwitches(this); // already done in DataRenderer.doAction
      Switch swit = null;
      if (old_swit != null) {
        swit = old_swit;
        ((AVControlJ3D) control).addPair((Switch) swit, domain_set, renderer);
        ((AVControlJ3D) control).init();
      }
      else {
        swit = (Switch) makeSwitch();
        swit.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        swit.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        swit.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

        addSwitch(group, swit, control, domain_set, renderer);
      }

      // insert old frames into new scene graph, and make
      // new (blank) VisADBranchGroups for rendering new frames
      VisADBranchGroup[] nodes = new VisADBranchGroup[len];
      boolean[] mark = new boolean[len];
      for (int i=0; i<len; i++) {
        for (int j=0; j<old_len; j++) {
          if (!old_mark[j] && Math.abs(times[i] - old_times[j]) < delta) {
            old_mark[j] = true;
            nodes[i] = (VisADBranchGroup) old_nodes[j];
            break;
          }
        }
        if (nodes[i] != null) {
          mark[i] = true;
        }
        else {
          mark[i] = false;
          nodes[i] = new VisADBranchGroup(times[i]);
          nodes[i].setCapability(BranchGroup.ALLOW_DETACH);
          nodes[i].setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
          nodes[i].setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          nodes[i].setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
          ensureNotEmpty(nodes[i]);
        }
        addToSwitch(swit, nodes[i]);
      }
      for (int j=0; j<old_len; j++) {
        if (!old_mark[j]) {
          ((RendererJ3D) renderer).flush(old_nodes[j]);
        }
      }
      // make sure group is live
      if (group instanceof BranchGroup) {
        ((ImageRendererJ3D) renderer).setBranchEarly((BranchGroup) group);
      }
      // change animation sampling, but don't trigger re-transform
      if (((ImageRendererJ3D) renderer).getReUseFrames() &&
          ((ImageRendererJ3D) renderer).getSetSetOnReUseFrames()) {
        control.setSet(domain_set, true);
      }

      // render new frames
      for (int i=0; i<len; i++) {
/*
// this is to test setBranchEarly()
if (i == (len / 2)) {
  System.out.println("doTransform delay");
  new Delay(5000);
}
*/
        if (!mark[i]) {
          // not necessary, but perhaps if this is modified
          // int[] lat_lon_indices = renderer.getLatLonIndices();
          BranchGroup branch = (BranchGroup) makeBranch();
          ((ImageRendererJ3D) renderer).setVisADBranch(nodes[i]);
          recurseRange(branch, ((Field) data).getSample(i),
                       value_array, default_values, renderer);
          ((ImageRendererJ3D) renderer).setVisADBranch(null);
          nodes[i].addChild(branch);
          // not necessary, but perhaps if this is modified
          // renderer.setLatLonIndices(lat_lon_indices);
        }
      }
    }


    ensureNotEmpty(group);
    return false;
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


  // test program
  private static DisplayImpl display;
  private static BaseMapAdapter baseMap;
  private static ScalarMap lat_map;
  private static ScalarMap lon_map;
  private static ScalarMap xaxis;
  private static ScalarMap yaxis;

  // run 'java visad.bom.ShadowImageFunctionTypeJ3D' for globe display
  // run 'java visad.bom.ShadowImageFunctionTypeJ3D X remap'
  //                     for remapped globe display
  // run 'java visad.bom.ShadowImageFunctionTypeJ3D X 2D' for flat display
  public static void main (String[] args) {

    String mapFile = "OUTLSUPW";
    String areaFile = "AREA2001";
    boolean threeD = true;
    boolean remap = false;

    JFrame frame = new JFrame("Map Display");
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    });

    if (args.length > 0 && !args[0].equals("X")) {
       areaFile = args[0];
       // mapFile = args[0];
    }
    if (args.length == 2) {
       threeD = (args[1].indexOf("2") >= 0) ? false : true;
       remap = (args[1].indexOf("2") >= 0) ? false : true;
    }

    boolean gif = areaFile.endsWith("gif") || areaFile.endsWith("GIF") ||
                  areaFile.endsWith("jpg") || areaFile.endsWith("JPG");

    try {

      if (mapFile.indexOf("://") > 0) {
        baseMap = new BaseMapAdapter(new URL(mapFile) );
      } else {
        baseMap = new BaseMapAdapter(mapFile);
      }

      //--- map data to display ---//
      if (gif) {
        display = new DisplayImplJ3D("display",
                                     new TwoDDisplayRendererJ3D());
        lat_map = new ScalarMap(RealType.getRealType("ImageLine"), Display.YAxis);
        lon_map = new ScalarMap(RealType.getRealType("ImageElement"), Display.XAxis);
      }
      else if (threeD)
      {
        display = new DisplayImplJ3D("display");
        lat_map = new ScalarMap(RealType.Latitude, Display.Latitude);
        lon_map = new ScalarMap(RealType.Longitude, Display.Longitude);
      }
      else
      {
        display = new DisplayImplJ3D("display",
                                     new TwoDDisplayRendererJ3D());
        lat_map = new ScalarMap(RealType.Latitude, Display.YAxis);
        lon_map = new ScalarMap(RealType.Longitude, Display.XAxis);
      }

      display.addMap(lat_map);
      display.addMap(lon_map);

      if (!gif) {
        lat_map.setRange(-90.0, 90.0);
        lon_map.setRange(-180.0, 180.0);
      }

      DataReference maplines_ref = new DataReferenceImpl("MapLines");
      maplines_ref.setData(baseMap.getData());

      ConstantMap[] colMap;
      colMap = new ConstantMap[4];
      colMap[0] = new ConstantMap(0., Display.Blue);
      colMap[1] = new ConstantMap(1., Display.Red);
      colMap[2] = new ConstantMap(0., Display.Green);
      colMap[3] = new ConstantMap(1.001, Display.Radius); // map lines above image

      FlatField imaget = null;
      if (gif) {
        GIFForm gif_form = new GIFForm();
        imaget = (FlatField) gif_form.open(areaFile);
      }
      else {
        AreaAdapter aa = new AreaAdapter(areaFile);
        imaget = aa.getData();
      }

      FunctionType ftype = (FunctionType) imaget.getType();
      RealTupleType dtype = ftype.getDomain();
      RealTupleType rtype = (RealTupleType)ftype.getRange();

      if (remap) {
        int SIZE = 256;
        RealTupleType lat_lon =
          ((CoordinateSystem) dtype.getCoordinateSystem()).getReference();
        Linear2DSet dset = new Linear2DSet(lat_lon, -4.0, 70.0, SIZE,
                                           -150.0, 5.0, SIZE);
        imaget = (FlatField)
          imaget.resample(dset, Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
        ftype = (FunctionType) imaget.getType();
        dtype = ftype.getDomain();
      }

      if (gif) {
        ScalarMap rmap = new ScalarMap( (RealType) rtype.getComponent(0),
                                        Display.Red);
        display.addMap(rmap);
        ScalarMap gmap = new ScalarMap( (RealType) rtype.getComponent(1),
                                        Display.Green);
        display.addMap(gmap);
        ScalarMap bmap = new ScalarMap( (RealType) rtype.getComponent(2),
                                        Display.Blue);
        display.addMap(bmap);
      }
      else {
        // select which band to show...
        ScalarMap rgbmap = new ScalarMap( (RealType) rtype.getComponent(0),
                                          Display.RGBA);
        display.addMap(rgbmap);
        BaseColorControl control = (BaseColorControl) rgbmap.getControl();
        control.initGreyWedge();
/* test for RGBA */
        float[][] table = control.getTable();
        for (int i=0; i<table[3].length; i++) {
          table[3][i] = table[0][i];
        }
        control.setTable(table);
/* end test for RGBA */
      }

      DataReferenceImpl ref_image = new DataReferenceImpl("ref_image");

/* start modify imaget to be packed bytes */
      Set[] range_sets = gif ? new Set[] {new Linear1DSet(0.0, 255.0, 255),
                                          new Linear1DSet(0.0, 255.0, 255),
                                          new Linear1DSet(0.0, 255.0, 255)} :
                               new Set[] {new Integer1DSet(255)};
      FlatField new_field =
        new FlatField(ftype, imaget.getDomainSet(), null, null, range_sets, null);
      float[][] values = imaget.getFloats(false);
      new_field.setSamples(values);
      imaget = new_field;
/* end modify imaget */

      ref_image.setData(imaget);

      display.disableAction();
      display.addReferences(new ImageRendererJ3D(), ref_image);
      // display.addReference(ref_image);
      display.addReference(maplines_ref, colMap);
      display.enableAction();
    } catch (Exception ne) {ne.printStackTrace(); System.exit(1); }

    frame.getContentPane().add(display.getComponent());
    frame.setSize(500, 500);
    frame.setVisible(true);
  }

}

