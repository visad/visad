
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

  ShadowFunctionOrSetTypeJ3D(MathType t, DataDisplayLink link, ShadowType parent)
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
         throws VisADException, RemoteException { // J3D

    if (data.isMissing()) return false;

    // if transform has taken more than 500 milliseconds and there is
    // a flag requesting re-transform, throw a DisplayInterruptException
    boolean time_flag = false;
    if (renderer instanceof DefaultRendererJ3D) {
      if (((DefaultRendererJ3D) renderer).time_flag) {
        time_flag = true;
      }
      else {
        if (500 < System.currentTimeMillis() -
                  ((DefaultRendererJ3D) renderer).start_time) {
          ((DefaultRendererJ3D) renderer).time_flag = true;
          time_flag = true;
        }
      }
    }
    if (time_flag) {
      DataDisplayLink link = ((DefaultRendererJ3D) renderer).link;
      if (link.peekTicks()) {
        throw new DisplayInterruptException("please wait . . .");
      }
      Enumeration maps = link.getSelectedMapVector().elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        if (map.peekTicks(renderer, link)) {
          throw new DisplayInterruptException("please wait . . .");
        }
      }
    }

    boolean anyContour =
      ((ShadowFunctionOrSetType) adaptedShadowType).getAnyContour();
    boolean anyFlow =
      ((ShadowFunctionOrSetType) adaptedShadowType).getAnyFlow();
    boolean anyShape =
      ((ShadowFunctionOrSetType) adaptedShadowType).getAnyShape();

    if (anyShape) {
      throw new UnimplementedException("ShadowFunctionOrSetTypeJ3D.doTransform" +
                                       "Shape not yet supported");
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
 
    // get values inherited from parent;
    // assume these do not include SelectRange, SelectValue
    // or Animation values - see temporary hack in
    // DataRenderer.isTransformControl
    int[] inherited_values =
      ((ShadowFunctionOrSetType) adaptedShadowType).getInheritedValues();
    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0) {
        display_values[i] = new float[1];
        display_values[i][0] = value_array[i];
      }
    }

    // check for only contours and only disabled contours
    if (adaptedShadowType.getIsTerminal() && anyContour && !anyFlow) {
      boolean any_enabled = false;
      for (int i=0; i<valueArrayLength; i++) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        if (real.equals(Display.IsoContour) && inherited_values[i] == 0) {
          // non-inherited IsoContour, so generate contours
          ContourControl control = (ContourControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
          boolean[] bvalues = new boolean[2];
          float[] fvalues = new float[5];
          control.getMainContours(bvalues, fvalues);
          if (bvalues[0]) any_enabled = true;
        }
      }
      if (!any_enabled) return false;
    }

    Set domain_set = null;
    Unit[] dataUnits = null;
    CoordinateSystem dataCoordinateSystem = null;
    if (this instanceof ShadowFunctionTypeJ3D) {
      // currently only implemented for Field
      // must eventually extend to Function
      if (!(data instanceof Field)) {
        throw new UnimplementedException("ShadowFunctionOrSetType.doTransform: " +
                                         "data must be Field");
      }
      domain_set = ((Field) data).getDomainSet();
      dataUnits = ((Function) data).getDomainUnits();
      dataCoordinateSystem = ((Function) data).getDomainCoordinateSystem();
    }
    else if (this instanceof ShadowSetTypeJ3D) {
      domain_set = (Set) data;
      dataUnits = ((Set) data).getSetUnits();
      dataCoordinateSystem = ((Set) data).getCoordinateSystem();
    }
    else {
      throw new DisplayException("ShadowFunctionOrSetTypeJ3D.doTransform: " +
                          "must be ShadowFunctionType or ShadowSetType");
    }

    float[][] domain_values = null;
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length = domain_set.getLength();

    // ShadowRealTypes of Domain
    ShadowRealType[] DomainComponents =
      ((ShadowFunctionOrSetType) adaptedShadowType).getDomainComponents();

    int alpha_index = display.getDisplayScalarIndex(Display.Alpha);

    boolean isTextureMap = adaptedShadowType.getIsTextureMap() &&
                           default_values[alpha_index] > 0.99 &&
                           (domain_set instanceof Linear2DSet ||
                            (domain_set instanceof LinearNDSet &&
                             domain_set.getDimension() == 2));
/*
System.out.println("isTextureMap = " + isTextureMap + " " +
                   adaptedShadowType.getIsTextureMap() + " " +
                   (default_values[alpha_index] > 0.99) + " " +
                   (domain_set instanceof Linear2DSet) + " " +
                   (domain_set instanceof LinearNDSet) + " " +
                   (domain_set.getDimension() == 2));
*/
    float[] coordinates = null;
    float[] texCoords = null;
    float[] normals = null;
    float[] colors = null;
    int data_width = 0;
    int data_height = 0;
    int texture_width = 1;
    int texture_height = 1;
    if (isTextureMap) {
      if (renderer instanceof DirectManipulationRendererJ3D) {
        throw new DisplayException("ShadowFunctionOrSetTypeJ3D.doTransform" +
                                   " DirectManipulationRendererJ3D");
      }
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

      float value2 = 0.0f;
      // convert values to default units (used in display)
      limits = Unit.convertTuple(limits, dataUnits, domain_units);

      // get domain_set sizes
      data_width = X.getLength();
      data_height = Y.getLength();
      // texture sizes must be powers of 2
      while (texture_width < data_width) texture_width *= 2; 
      while (texture_height < data_height) texture_height *= 2; 

      int[] tuple_index = new int[3];
      if (DomainComponents.length != 2) {
        throw new DisplayException("ShadowFunctionOrSetTypeJ3D.doTransform" +
                                   " domain dimension != 2");
      }
      for (int i=0; i<DomainComponents.length; i++) {
        Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();
        ScalarMap map = (ScalarMap) maps.nextElement();
        // scale values
        limits[i] = map.scaleValues(limits[i]);
        DisplayRealType real = map.getDisplayScalar();
        DisplayTupleType tuple = real.getTuple();
        if (tuple == null ||
            !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
          throw new DisplayException("ShadowFunctionOrSetTypeJ3D.doTransform" +
                                     " isTextureMap with bad tuple");
        }
        // get spatial index
        tuple_index[i] = real.getTupleIndex();
        if (maps.hasMoreElements()) {
          throw new DisplayException("ShadowFunctionOrSetTypeJ3D.doTransform" +
                                     " isTextureMap with multiple");
        }
      } // end for (int i=0; i<DomainComponents.length; i++)
      // get spatial index not mapped from domain_set
      tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
      DisplayRealType real = (DisplayRealType)
        Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
      for (int i=0; i<valueArrayLength; i++) {
        if (inherited_values[i] > 0 &&
            real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
          value2 = value_array[i];
          break;
        }
      }
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

      texCoords = new float[8];
      float ratiow = ((float) data_width) / ((float) texture_width);
      float ratioh = ((float) data_height) / ((float) texture_height);
      // corner 0
      texCoords[0] = 0.0f;
      texCoords[1] = 1.0f - ratioh;
      // corner 1
      texCoords[2] = ratiow;
      texCoords[3] = 1.0f - ratioh;
      // corner 2
      texCoords[4] = ratiow;
      texCoords[5] = 1.0f;
      // corner 3
      texCoords[6] = 0.0f;
      texCoords[7] = 1.0f;

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

      colors = new float[12];
      for (int i=0; i<12; i++) colors[i] = 0.5f;
/*
for (int i=0; i < 4; i++) {
  System.out.println("i = " + i + " texCoords = " + texCoords[2 * i] + " " +
                     texCoords[2 * i + 1]);
  System.out.println(" coordinates = " + coordinates[3 * i] + " " +
                     coordinates[3 * i + 1] + " " + coordinates[3 * i + 2]);
  System.out.println(" normals = " + normals[3 * i]  + " " + normals[3 * i + 1] +
                     " " + normals[3 * i + 2]);
}
*/
    }
    else { // !isTextureMap
      // get values from Function Domain
      // NOTE - may defer this until needed, if needed
      domain_values = domain_set.getSamples(false);

      // convert values to default units (used in display)
      domain_values = Unit.convertTuple(domain_values, dataUnits, domain_units);
      // System.out.println("got domain_values: domain_length = " + domain_length);

      // map domain_values to appropriate DisplayRealType-s
      // MEM
      mapValues(display_values, domain_values, DomainComponents);
   
      // System.out.println("mapped domain_values");
  
      ShadowRealTupleType domain_reference = Domain.getReference();

/*
      System.out.println("domain_reference = " + domain_reference);
      if (domain_reference != null) {
        System.out.println("getMappedDisplayScalar = " +
                           domain_reference.getMappedDisplayScalar());
      }
*/
      if (domain_reference != null && domain_reference.getMappedDisplayScalar()) {
        // apply coordinate transform to domain values
        RealTupleType ref = (RealTupleType) domain_reference.getType();
        // MEM
        float[][] reference_values =
          CoordinateSystem.transformCoordinates(
            ref, null, ref.getDefaultUnits(), null,
            (RealTupleType) Domain.getType(), dataCoordinateSystem,
            domain_units, null, domain_values);
 
        //
        // TO_DO
        // adjust any RealVectorTypes in range
        // see FlatField.resample and FieldImpl.resample
        //
  
        // map reference_values to appropriate DisplayRealType-s
        // MEM
        ShadowRealType[] DomainReferenceComponents =
          ((ShadowFunctionOrSetType) adaptedShadowType).
                                     getDomainReferenceComponents();
        mapValues(display_values, reference_values, DomainReferenceComponents);
/*
for (int i=0; i<DomainReferenceComponents.length; i++) {
  System.out.println("DomainReferenceComponents[" + i + "] = " +
                     DomainReferenceComponents[i]);
  System.out.println("reference_values[" + i + "].length = " +
                     reference_values[i].length);
}
        System.out.println("mapped domain_reference values");
*/
        // FREE
        reference_values = null;
      }
      // FREE
      domain_values = null;
    } // end if (!isTextureMap)

    if (this instanceof ShadowFunctionTypeJ3D) {

      // get range_values for RealType and RealTupleType
      // components, in defaultUnits for RealType-s
      // MEM
      double[][] range_values = ((Field) data).getValues();
  
      // System.out.println("got range_values");
  
      if (range_values != null) {
        // map range_values to appropriate DisplayRealType-s
        // MEM
        ShadowRealType[] RangeComponents =
          ((ShadowFunctionOrSetType) adaptedShadowType).getRangeComponents();
        mapValues(display_values, range_values, RangeComponents);
   
        // System.out.println("mapped range_values");
  
        //
        // transform any range CoordinateSystem-s
        // into display_values, then mapValues
        //
        int[] refToComponent = adaptedShadowType.getRefToComponent();
        ShadowRealTupleType[] componentWithRef =
          adaptedShadowType.getComponentWithRef();
        int[] componentIndex = adaptedShadowType.getComponentIndex();

        if (refToComponent != null) {
  
          for (int i=0; i<refToComponent.length; i++) {
            int n = componentWithRef[i].getDimension();
            int start = refToComponent[i];
            double[][] values = new double[n][];
            for (int j=0; j<n; j++) values[j] = range_values[j + start];
            ShadowRealTupleType component_reference =
              componentWithRef[i].getReference();
            RealTupleType ref = (RealTupleType) component_reference.getType();
            Unit[] range_units;
            CoordinateSystem[] range_coord_sys;
            if (i == 0 && componentWithRef[i].equals(Range)) {
              range_units = ((Field) data).getDefaultRangeUnits();
              range_coord_sys = ((Field) data).getRangeCoordinateSystem();
            }
            else {
              Unit[] dummy_units = ((Field) data).getDefaultRangeUnits();
              range_units = new Unit[n];
              for (int j=0; j<n; j++) range_units[j] = dummy_units[j + start];
              range_coord_sys =
                ((Field) data).getRangeCoordinateSystem(componentIndex[i]);
            }
  
            double[][] reference_values = null;
            if (range_coord_sys.length == 1) {
              // MEM
              reference_values =
                CoordinateSystem.transformCoordinates(
                  ref, null, ref.getDefaultUnits(), null,
                  (RealTupleType) componentWithRef[i].getType(),
                  range_coord_sys[0], range_units, null, values);
            }
            else {
              reference_values = new double[n][domain_length];
              double[][] temp = new double[n][1];
              for (int j=0; j<domain_length; j++) {
                for (int k=0; k<n; k++) temp[k][0] = values[k][j];
                temp =
                  CoordinateSystem.transformCoordinates(
                    ref, null, ref.getDefaultUnits(), null,
                    (RealTupleType) componentWithRef[i].getType(),
                    range_coord_sys[j], range_units, null, temp);
                for (int k=0; k<n; k++) reference_values[k][j] = temp[k][0];
              }
            }
   
            // map reference_values to appropriate DisplayRealType-s
            // MEM
            mapValues(display_values, reference_values,
                      getComponents(componentWithRef[i], false));
            // FREE
            reference_values = null;
            // FREE (redundant reference to range_values)
            values = null;
          } // end for (int i=0; i<refToComponent.length; i++)
        } // end (refToComponent != null)
        // FREE
        range_values = null;
      } // end if (range_values != null)
    } // end if (this instanceof ShadowFunctionTypeJ3D)

    //
    // NOTE -
    // currently assuming SelectRange changes require Transform
    // see DataRenderer.isTransformControl
    //
    // get array that composites SelectRange components
    // range_select is null if all selected
    // MEM
    float[][] range_select =
      assembleSelect(display_values, domain_length, valueArrayLength,
                     valueToScalar, display);

    if (range_select[0] != null && range_select[0].length == 1 &&
        range_select[0][0] != range_select[0][0]) {
      // single missing value in range_select[0], so render nothing
      return false;
    }

    // System.out.println("assembleSelect");
 
    if (adaptedShadowType.getIsTerminal()) {
      if (!((ShadowFunctionOrSetType) adaptedShadowType).getFlat()) {
        throw new DisplayException("terminal but not Flat");
      }

      GraphicsModeControl mode = display.getGraphicsModeControl();
      boolean pointMode = mode.getPointMode();

      float[][] flow1_values = new float[3][];
      float[][] flow2_values = new float[3][];
      float[] flowScale = new float[2];
      assembleFlow(flow1_values, flow2_values, flowScale,
                   display_values, valueArrayLength, valueToScalar,
                   display, default_values, range_select);
 
      if (range_select[0] != null && range_select[0].length == 1 &&
          range_select[0][0] != range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }

      // System.out.println("assembleFlow");

      // assemble an array of Display.DisplaySpatialCartesianTuple values
      // and possibly spatial_set
      float[][] spatial_values = new float[3][];

      // spatialDimensions[0] = spatialDomainDimension and
      // spatialDimensions[1] = spatialManifoldDimension
      int[] spatialDimensions = new int[2];

      // MEM
      Set spatial_set = 
        assembleSpatial(spatial_values, display_values, valueArrayLength,
                        valueToScalar, display, default_values,
                        inherited_values, domain_set,
          ((ShadowRealTupleType) Domain.adaptedShadowType).getAllSpatial(),
                        anyContour, spatialDimensions, range_select,
                        flow1_values, flow2_values, flowScale);

      if (range_select[0] != null && range_select[0].length == 1 &&
          range_select[0][0] != range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }

      int spatialDomainDimension = spatialDimensions[0];
      int spatialManifoldDimension = spatialDimensions[1];

      // System.out.println("assembleSpatial");
 
      int spatial_length = Math.min(domain_length, spatial_values[0].length);

      // assemble an array of RGBA values
      // MEM
      float[][] color_values =
        assembleColor(display_values, valueArrayLength, valueToScalar,
                      display, default_values, range_select);

      if (range_select[0] != null && range_select[0].length == 1 &&
          range_select[0][0] != range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }

      int color_length = Math.min(domain_length, color_values[0].length);
      int alpha_length = color_values[3].length;
/*
      System.out.println("assembleColor, color_length = " + color_length +
                         "  " + color_values.length);
*/
      Appearance appearance;
      TransparencyAttributes constant_alpha =
        new TransparencyAttributes(mode.getTransparencyMode(), 1.0f);
      ColoringAttributes constant_color = null;
      // note alpha_length <= color_length
      if (alpha_length == 1) {
        if (color_values[3][0] != color_values[3][0]) {
          // a single missing alpha value, so render nothing
          // System.out.println("single missing alpha");
          return false;
        }
        // System.out.println("single alpha " + color_values[3][0]);
        // constant alpha, so put it in appearance
        if (color_values[3][0] > 0.999999f) {
          // constant opaque alpha = NONE
          constant_alpha =
            new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
          /* broken alpha */
          // remove alpha from color_values
          float[][] c = new float[3][];
          c[0] = color_values[0];
          c[1] = color_values[1];
          c[2] = color_values[2];
          color_values = c;
        }
        else {
/* TransparencyAttributes with constant alpha seems to have broken
          // note transparency 0.0 = opaque, 1.0 = clear
          constant_alpha =
            new TransparencyAttributes(TransparencyAttributes.NICEST,
                                       1.0f - color_values[3][0]);
so expand constant alpha to variable alpha:
*/
          float v = color_values[3][0];
          color_values[3] = new float[color_values[0].length];
          for (int i=0; i<color_values[0].length; i++) {
            color_values[3][i] = v;
          }
/*
System.out.println("replicate alpha = " + v + " " + constant_alpha +
                   " " + color_values[0].length + " " +
                   color_values[3].length);
*/
        }
/*
  broken alpha
        // remove alpha from color_values
        float[][] c = new float[3][];
        c[0] = color_values[0];
        c[1] = color_values[1];
        c[2] = color_values[2];
        color_values = c;
*/
      }
      if (color_length == 1) {
        if (color_values[0][0] != color_values[0][0] ||
            color_values[1][0] != color_values[1][0] ||
            color_values[2][0] != color_values[2][0]) {
          // System.out.println("single missing color");
          // a single missing color value, so render nothing
          return false;
        }
        // constant color, so put it in appearance
        constant_color = new ColoringAttributes(); // J3D
        constant_color.setColor(color_values[0][0], color_values[1][0],
                                color_values[2][0]);
        color_values = null;
      }

      if (range_select[0] != null && range_select[0].length == 1 &&
          range_select[0][0] != range_select[0][0]) {
        // single missing value in range_select[0], so render nothing
        return false;
      }

      int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();

      if (LevelOfDifficulty == SIMPLE_FIELD) {
        // only manage Spatial, Contour, Flow, Color, Alpha and
        // SelectRange here
        //
        // TO_DO
        // Flow rendering
        // Flow will be tricky - FlowControl must contain trajectory
        // start points
        //

/* MISSING TEST
        for (int i=0; i<spatial_values[0].length; i+=3) {
          spatial_values[0][i] = Float.NaN;
        }
END MISSING TEST */

        //
        // TO_DO
        // missing color_values and select_values
        //
        // NaN color component values are rendered as 1.0
        // NaN spatial component values of points are NOT rendered
        // NaN spatial component values of lines are rendered at infinity
        // NaN spatial component values of triangles are a mess ??
        //
/*
        System.out.println("spatialDomainDimension = " +
                           spatialDomainDimension +
                           " spatialManifoldDimension = " +
                           spatialManifoldDimension +
                           " anyContour = " + anyContour +
                           " pointMode = " + pointMode);
*/
        VisADGeometryArray array;
        boolean anyFlowCreated = false;
        if (anyFlow) {
          // try Flow1
          array = makeFlow(flow1_values, flowScale[0], spatial_values,
                           color_values, range_select);
          if (array != null) {
            GeometryArray geometry = display.makeGeometry(array);
            appearance = makeAppearance(mode, constant_alpha,
                                        constant_color, geometry);
            Shape3D shape = new Shape3D(geometry, appearance);
            group.addChild(shape);
            anyFlowCreated = true;
          }

          // try Flow2
          array = makeFlow(flow2_values, flowScale[1], spatial_values,
                           color_values, range_select);
          if (array != null) {
            GeometryArray geometry = display.makeGeometry(array);
            appearance = makeAppearance(mode, constant_alpha,
                                        constant_color, geometry);
            Shape3D shape = new Shape3D(geometry, appearance);
            group.addChild(shape);
            anyFlowCreated = true;
          }
        }
        boolean anyContourCreated = false;
        if (anyContour) {
          for (int i=0; i<valueArrayLength; i++) {
            int displayScalarIndex = valueToScalar[i];
            DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
            if (real.equals(Display.IsoContour) && inherited_values[i] == 0) {
              // non-inherited IsoContour, so generate contours
              array = null;
              ContourControl control = (ContourControl)
                ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
              boolean[] bvalues = new boolean[2];
              float[] fvalues = new float[5];
              control.getMainContours(bvalues, fvalues);
              if (bvalues[0]) {
                if (range_select[0] != null) {
                  int len = range_select[0].length;
                  if (len == 1 || display_values[i].length == 1) break;
                  for (int j=0; j<len; j++) {
                    // range_select[0][j] is either 0.0f or Float.NaN -
                    display_values[i][j] += range_select[0][j];
                  }
                }
                if (spatialManifoldDimension == 3) {
                  if (fvalues[0] == fvalues[0]) {
                    // System.out.println("makeIsoSurface at " + fvalues[0]);
                    array = spatial_set.makeIsoSurface(fvalues[0],
                                display_values[i], color_values);
                    // System.out.println("makeIsoSurface " + array.vertexCount);
                    if (array != null) {
                      // MEM
                      GeometryArray geometry = display.makeGeometry(array);
                      //  FREE
                      array = null;
                      appearance = makeAppearance(mode, constant_alpha,
                                                  constant_color, geometry);
                      Shape3D shape = new Shape3D(geometry, appearance);
                      group.addChild(shape);
                    }
                  }
                  anyContourCreated = true;
                }
                else if (spatialManifoldDimension == 2) {
                  VisADGeometryArray[] arrays =
                    spatial_set.makeIsoLines(fvalues[1], fvalues[2], fvalues[3],
                                  fvalues[4], display_values[i], color_values);
                  if (arrays != null && arrays.length != 0 && arrays[0] != null) {
                    // MEM
                    GeometryArray geometry = display.makeGeometry(arrays[0]);
                    //  FREE
                    arrays[0] = null;
                    appearance = makeAppearance(mode, constant_alpha,
                                                constant_color, geometry);
                    Shape3D shape = new Shape3D(geometry, appearance);
                    group.addChild(shape);
                    if (bvalues[1] && arrays[2] != null) {
                      // System.out.println("makeIsoLines with labels");
                      // draw labels
                      // MEM
                      geometry = display.makeGeometry(arrays[2]);
                      //  FREE
                      arrays = null;
                    }
                    else if ((!bvalues[1]) && arrays[1] != null) {
                      // System.out.println("makeIsoLines without labels");
                      // fill in contour lines in place of labels
                      // MEM
                      geometry = display.makeGeometry(arrays[1]);
                      //  FREE
                      arrays = null;
                    }
                    else {
                      geometry = null;
                    }
                    if (geometry != null) {
                      appearance = makeAppearance(mode, constant_alpha,
                                                  constant_color, geometry);
                      shape = new Shape3D(geometry, appearance);
                      group.addChild(shape);
                    }
                  }
                  anyContourCreated = true;
                } // end if (spatialManifoldDimension == 3 or 2)
              } // end if (bvalues[0])
            } // end if (real.equals(Display.IsoContour) && not inherited)
          } // end for (int i=0; i<valueArrayLength; i++)
        } // end if (anyContour)
        if (!anyContourCreated && !anyFlowCreated) {
          // MEM
          if (isTextureMap) {
            if (color_values == null) {
              throw new DisplayException("ShadowFunctionOrSetTypeJ3D." +
                               ".doTransform: no color or alpha values");
            }
            if (range_select[0] != null && range_select[0].length > 1) {
              int len = range_select[0].length;
              float alpha =
                default_values[display.getDisplayScalarIndex(Display.Alpha)];
              if (constant_alpha != null) {
                alpha = constant_alpha.getTransparency();
              }
/* ????
              if (color_values == null) {
                float[] colf = null;
                if (constant_color != null) {
                  Color3f color = null;
                  constant_color.getColor(color);
                  colf = new float[3];
                  colf[0] = color.x;
                  colf[1] = color.y;
                  colf[2] = color.z;
                }
                color_values = new float[4][len];
                for (int j=0; j<3; j++) {
                  float def_val = (colf != null) ? colf[j] :
                    default_values[getDefaultColorIndex(display, j)];
                  for (int i=0; i<len; i++) color_values[j][i] = def_val;
                }
                for (int i=0; i<len; i++) color_values[3][i] = alpha;
                constant_color = null;
                constant_alpha = null;
              }
              else if (color_values.length < 4) {
*/
              if (color_values.length < 4) {
                float[][] c = new float[4][];
                c[0] = color_values[0];
                c[1] = color_values[1];
                c[2] = color_values[2];
                for (int i=0; i<len; i++) c[3][i] = alpha;
                constant_alpha = null;
                color_values = c;
              }
              for (int i=0; i<len; i++) {
                if (range_select[0][i] != range_select[0][i]) {
                  // make missing pixel invisible (transparent)
                  color_values[3][i] = 0.0f;
                }
              }
            } // end if (range_select[0] != null)

            int vertexFormat = GeometryArray.COORDINATES |
                               GeometryArray.NORMALS |
                               GeometryArray.COLOR_3 |
                               GeometryArray.TEXTURE_COORDINATE_2;

            // MEM
            QuadArray geometry = new QuadArray(4, vertexFormat);
            geometry.setCoordinates(0, coordinates);
            geometry.setNormals(0, normals);
            geometry.setTextureCoordinates(0, texCoords);
            geometry.setColors(0, colors);

            // System.out.println("texture geometry");
   
            // crreate basic Appearance
            appearance = makeAppearance(mode, constant_alpha,
                                        constant_color, geometry);
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
            // Component component = display.getComponent();


/* JDK 1.2 stuff */
            BufferedImage image = null;
            ImageComponent2D image2d = null;
            int[] rgbArray = new int[texture_width * texture_height];
            if (color_values.length > 3) {
              int k = 0;
              int r, g, b, a;
              image = new BufferedImage(texture_width, texture_height,
                                        BufferedImage.TYPE_INT_ARGB);
              for (int j=0; j<data_height; j++) {
                for (int i=0; i<data_width; i++) {
                  r = (int) (color_values[0][k] * 255.0);
                  r = (r < 0) ? 0 : (r > 255) ? 255 : r;
                  g = (int) (color_values[1][k] * 255.0);
                  g = (g < 0) ? 0 : (g > 255) ? 255 : g;
                  b = (int) (color_values[2][k] * 255.0);
                  b = (b < 0) ? 0 : (b > 255) ? 255 : b;
                  a = (int) (color_values[3][k] * 255.0);
                  a = (a < 0) ? 0 : (a > 255) ? 255 : a;
                  image.setRGB(i, j, ((r << 24) | (g << 16) | (b << 8) | a));
                  k++;
                }
                for (int i=data_width; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }
              for (int j=data_height; j<texture_height; j++) {
                for (int i=0; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }
              // transparency does not work yet
              image2d = new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
            }
            else { // (color_values.length == 3)
              int k = 0;
              int r, g, b, a;
              image = new BufferedImage(texture_width, texture_height,
                                        BufferedImage.TYPE_INT_ARGB);
              for (int j=0; j<data_height; j++) {
                for (int i=0; i<data_width; i++) {
                  r = (int) (color_values[0][k] * 255.0);
                  r = (r < 0) ? 0 : (r > 255) ? 255 : r;
                  g = (int) (color_values[1][k] * 255.0);
                  g = (g < 0) ? 0 : (g > 255) ? 255 : g;
                  b = (int) (color_values[2][k] * 255.0);
                  b = (b < 0) ? 0 : (b > 255) ? 255 : b;
                  a = 255;
                  image.setRGB(i, j, ((r << 24) | (g << 16) | (b << 8) | a));
                  k++;
                }
                for (int i=data_width; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }
              for (int j=data_height; j<texture_height; j++) {
                for (int i=0; i<texture_width; i++) {
                  image.setRGB(i, j, 0);
                }
              }
              image2d = new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
//
// this doesn't work - why not?
//            image.setRGB(0, 0, texture_width, texture_height,
//                         rgbArray, 0, texture_width);
//
// this doesn't work either - why not?
//            for (int j=0; j<texture_height; j++) {
//              image.setRGB(0, j, texture_width, 1,
//                           rgbArray, j*texture_width, texture_width);
//            }
//

              image2d = new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
            } // end if (color_values.length == 3)
/* */

/* this doesn't work - setRGB must have a bug

            BufferedImage image = null;
            ImageComponent2D image2d = null;
            int[] rgbArray = new int[texture_width];
            if (color_values.length > 3) {
              image = new BufferedImage(texture_width, texture_height,
                                        BufferedImage.TYPE_INT_ARGB);
              int k = 0;
              for (int j=0; j<texture_height; j++) {
                for (int i=0; i<texture_width; i++) {
                  int r, g, b, a;
                  r = Math.min(255, Math.max(0, (int) (color_values[0][k] * 255.0)));
                  g = Math.min(255, Math.max(0, (int) (color_values[1][k] * 255.0)));
                  b = Math.min(255, Math.max(0, (int) (color_values[2][k] * 255.0)));
                  a = Math.min(255, Math.max(0, (int) (color_values[3][k] * 255.0)));
                  rgbArray[i] = (r << 24) | (g << 16) | (b << 8) | a;
                }
                
              }
              // transparency does not work yet
              image2d = new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
            }
            else { // !(color_values.length > 3)
              image = new BufferedImage(texture_width, texture_height,
                                        BufferedImage.TYPE_INT_ARGB);
              int k = 0;
              for (int j=0; j<texture_height; j++) {
                for (int i=0; i<texture_width; i++) {
                  int r, g, b, a;
                  r = Math.min(255, Math.max(0, (int) (color_values[0][k] * 255.0)));
                  g = Math.min(255, Math.max(0, (int) (color_values[1][k] * 255.0)));
                  b = Math.min(255, Math.max(0, (int) (color_values[2][k] * 255.0)));
                  a = 255; // TO_DO  does 255 == opaque ??  ??  ??
                  rgbArray[i] = (r << 24) | (g << 16) | (b << 8) | a; 
                }
                image.setRGB(0, j, texture_width, 1, rgbArray, 0, texture_width);
              }
              image2d = new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
            }
*/


/* this doesn't work - but is deep in the intricacies of java.awt.image
   so who would expect it to
            DirectColorModel cm =
              new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000);

            int[] bitMasks = {0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000};
            // int dataType = BufferedImage.TYPE_INT_ARGB;
            int dataType = DataBuffer.INT_DATA;
            SampleModel sampleModel =
              new MultiBandPackedSampleModel(dataType, texture_width,
                                             texture_height, bitMasks);

            WritableRaster raster = new myWritableRaster(sampleModel, new Point(0, 0));
 
            int[] rgbArray = new int[texture_width * texture_height];
            if (color_values.length > 3) {
              for (int i=0; i<domain_length; i++) {
                int r, g, b, a;
                r = Math.min(255, Math.max(0, (int) (color_values[0][i] * 255.0)));
                g = Math.min(255, Math.max(0, (int) (color_values[1][i] * 255.0)));
                b = Math.min(255, Math.max(0, (int) (color_values[2][i] * 255.0)));
                a = Math.min(255, Math.max(0, (int) (color_values[3][i] * 255.0)));
                rgbArray[i] = (r << 24) | (g << 16) | (b << 8) | a;
              }
            }
            else {
              for (int i=0; i<domain_length; i++) {
                int r, g, b, a;
                r = Math.min(255, Math.max(0, (int) (color_values[0][i] * 255.0)));
                g = Math.min(255, Math.max(0, (int) (color_values[1][i] * 255.0)));
                b = Math.min(255, Math.max(0, (int) (color_values[2][i] * 255.0)));
                a = 255;
                rgbArray[i] = (r << 24) | (g << 16) | (b << 8) | a;
              }
            }
 
            raster.setPixel(0, 0, texture_width, texture_height, rgbArray);

java.lang.ArrayIndexOutOfBoundsException: 4096
        at java.awt.image.MultiBandPackedSampleModel.setPixel(MultiBandPackedSampleModel.java:541)
        at java.awt.image.WritableRaster.setPixel(WritableRaster.java:338)
        at visad.java3d.ShadowFunctionOrSetTypeJ3D.doTransform(ShadowFunctionOrSetTypeJ3D.java:959)
        at visad.java3d.DefaultRendererJ3D.doTransform(DefaultRendererJ3D.java:71)
        at visad.java3d.RendererJ3D.doAction(RendererJ3D.java:153)
        at visad.DisplayImpl.doAction(DisplayImpl.java:355)
        at visad.ActionImpl.run(ActionImpl.java:145)
        at java.lang.Thread.run(Thread.java:484)

 
            boolean isRasterPremultiplied = false;
 
            BufferedImage image =
              new BufferedImage(cm, raster, isRasterPremultiplied);
            ImageComponent2D image2d =
              new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
*/
/* ditto for this
            // DataBuffer dataBuffer =
            //   new DataBuffer(dataType, texture_width * texture_height);

            DataBuffer dataBuffer = sampleModel.createCompatibleDataBuffer();

            if (color_values.length > 3) {
              for (int i=0; i<domain_length; i++) {
                int r, g, b, a;
                r = Math.min(255, Math.max(0, (int) (color_values[0][i] * 255.0)));
                g = Math.min(255, Math.max(0, (int) (color_values[1][i] * 255.0)));
                b = Math.min(255, Math.max(0, (int) (color_values[2][i] * 255.0)));
                a = Math.min(255, Math.max(0, (int) (color_values[3][i] * 255.0)));
                dataBuffer.setElem(i, (r << 24) | (g << 16) | (b << 8) | a);
              }
            }
            else {
              for (int i=0; i<domain_length; i++) {
                int r, g, b, a;
                r = Math.min(255, Math.max(0, (int) (color_values[0][i] * 255.0)));
                g = Math.min(255, Math.max(0, (int) (color_values[1][i] * 255.0)));
                b = Math.min(255, Math.max(0, (int) (color_values[2][i] * 255.0)));
                a = 255;
                dataBuffer.setElem(i, (r << 24) | (g << 16) | (b << 8) | a);
              }
            }

            WritableRaster raster =
              new WritableRaster(sampleModel, dataBuffer, new Point(0, 0));
*/

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
            texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
            texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
            texture.setEnable(true);
            // end of from TextureLoader
            //


            Shape3D shape = new Shape3D(geometry, appearance);
            appearance.setTexture(texture);

            group.addChild(shape);

            // System.out.println("isTextureMap done");

            return false;
          }
          else if (range_select[0] != null) {
            int len = range_select[0].length;
            if (len == 1 || spatial_values[0].length == 1) return false;
            for (int j=0; j<len; j++) {
              // range_select[0][j] is either 0.0f or Float.NaN -
              // adding Float.NaN will move the point off the screen
              spatial_values[0][j] += range_select[0][j];
            }
            array = makePointGeometry(spatial_values, color_values);
            // System.out.println("makePointGeometry for some missing");
          }
          else if (pointMode) {
            array = makePointGeometry(spatial_values, color_values);
            // System.out.println("makePointGeometry for pointMode");
          }
          else if (spatial_set == null) {
            array = makePointGeometry(spatial_values, color_values);
            // System.out.println("makePointGeometry for spatial_set == null");
          }
          else if (spatialManifoldDimension == 1) {
            //
            // TO_DO
            // test make1DGeometry (I think I did already,
            //   but test it again just to make sure)
            //
            array = spatial_set.make1DGeometry(color_values);
            // System.out.println("make1DGeometry");
          }
          else if (spatialManifoldDimension == 2) {
            array = spatial_set.make2DGeometry(color_values);
            // System.out.println("make2DGeometry");
          }
          else if (spatialManifoldDimension == 3) {
            array = makePointGeometry(spatial_values, color_values);
            // System.out.println("makePointGeometry  for 3D");
            //
            // when make3DGeometry is implemented:
            // array = spatial_set.make3DGeometry(color_values);
            //
          }
          else if (spatialManifoldDimension == 0) {
            array = spatial_set.makePointGeometry(color_values);
            // System.out.println("makePointGeometry  for 0D");
          }
          else {
            throw new DisplayException("ShadowFunctionOrSetType.doTransform: " +
                                       "bad spatialManifoldDimension");
          }
  
          if (array != null) {
            // MEM
            GeometryArray geometry = display.makeGeometry(array);
            // System.out.println("array.makeGeometry");
            //  FREE
            array = null;
            appearance = makeAppearance(mode, constant_alpha,
                                        constant_color, geometry);
            Shape3D shape = new Shape3D(geometry, appearance);
            group.addChild(shape);
            if (renderer instanceof DirectManipulationRendererJ3D) {
              ((DirectManipulationRendererJ3D) renderer).
                                   setSpatialValues(spatial_values);
            }
          }
        } // end if (!anyContourCreated && !anyFlowCreated)

        return false;
      }
      else { // must be LevelOfDifficulty == LEGAL
        // add values to value_array according to SelectedMapVector-s
        // of RealType-s in Domain (including Reference) and Range
        //
        // accumulate Vector of value_array-s at this ShadowType,
        // to be rendered in a post-process to scanning data
        //
        // ** OR JUST EACH FIELD INDEPENDENTLY **
        //
/*
        return true;
*/
        throw new UnimplementedException("ShadowFunctionOrSetType.doTransform: " +
                                         "terminal LEGAL");
      }
    }
    else { // !isTerminal
      // domain_values and range_values, as well as their References,
      // already converted to default Units and added to display_values

      // add values to value_array according to SelectedMapVector-s
      // of RealType-s in Domain (including Reference), and
      // recursively call doTransform on Range values

      //
      // TO_DO
      // SelectRange (use float[] range_select from assembleSelect),
      //   SelectValue, Animation
      // DataRenderer.isTransformControl temporary hack:
      // SelectRange.isTransform,
      // !SelectValue.isTransform, !Animation.isTransform
      //
      // may need to split ShadowType.checkAnimationOrValue
      // Display.Animation has range (0.0, 1.0), is single
      // Display.Value has no range, is not single
      //
      // see Set.merge1DSets

      boolean post = false;

      AVControlJ3D control = null;
      Switch swit = null;
      int index = -1;

      for (int i=0; i<valueArrayLength; i++) {
        float[] values = display_values[i];
        if (values != null) {
          int displayScalarIndex = valueToScalar[i];
          DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
          if (real.equals(Display.Animation) ||
              real.equals(Display.SelectValue)) {
            swit = new Switch(); // J3D
            swit.setCapability(Switch.ALLOW_SWITCH_READ);
            swit.setCapability(Switch.ALLOW_SWITCH_WRITE);
            swit.setCapability(BranchGroup.ALLOW_DETACH);
            swit.setCapability(Group.ALLOW_CHILDREN_READ);
            swit.setCapability(Group.ALLOW_CHILDREN_WRITE);
            index = i;
            control = (AVControlJ3D)
              ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
            break;
          }
        } // end if (values != null)
      } // end for (int i=0; i<valueArrayLength; i++)

      if (control != null) {
        group.addChild(swit);
        control.addPair(swit, domain_set, renderer);
      }

      float[] range_value_array = new float[valueArrayLength];
      for (int j=0; j<display.getValueArrayLength(); j++) {
        range_value_array[j] = Float.NaN;
      }
      for (int i=0; i<domain_length; i++) {
        if (range_select[0] == null || range_select[0].length == 1 ||
            range_select[0][i] == range_select[0][i]) {
          for (int j=0; j<valueArrayLength; j++) {
            if (display_values[j] != null) {
              if (display_values[j].length == 1) {
                range_value_array[j] = display_values[j][0];
              }
              else {
                range_value_array[j] = display_values[j][i];
              }
            }
          }
          if (control != null) {
            BranchGroup branch = new BranchGroup(); // J3D
            branch.setCapability(BranchGroup.ALLOW_DETACH);
            swit.addChild(branch);
            post |= Range.doTransform(branch, ((Field) data).getSample(i),
                                      range_value_array, default_values, renderer);
            // System.out.println("addChild " + i + " of " + domain_length);
          }
          else {
            post |= Range.doTransform(group, ((Field) data).getSample(i),
                                      range_value_array, default_values, renderer);
          }
        }
        else { // (range_select[0][i] != range_select[0][i])
          if (control != null) {
            // add null Sjape3D as child to maintain order
            BranchGroup branch = new BranchGroup(); // J3D
            branch.setCapability(BranchGroup.ALLOW_DETACH);
            swit.addChild(branch);
            branch.addChild(new Shape3D());
            // System.out.println("addChild " + i + " of " + domain_length +
            //                    " MISSING");
          }
        }
      }

      if (control != null) {
        // initialize swit child selection
        control.init();
      }

      return post;
/*
      throw new UnimplementedException("ShadowFunctionOrSetType.doTransform: " +
                                       "not terminal");
*/
    } // end if (!isTerminal)
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
        throw new UnimplementedException("ShadowFunctionOrSetType.postProcess: " +
                                         "terminal LEGAL");
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

  private class myWritableRaster extends WritableRaster {
    myWritableRaster(SampleModel sm, Point p) {
      super(sm, p);
    }
  }

}

