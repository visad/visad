
//
// ShadowFunctionOrSetTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

import java.util.*;
import java.rmi.*;

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
    Range.preProcess();
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

    // get some precomputed values useful for transform
    // length of ValueArray
    int valueArrayLength = display.getValueArrayLength();
    // mapping from ValueArray to DisplayScalar
    int[] valueToScalar = display.getValueToScalar();
    // mapping from ValueArray to MapVector
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

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

/** kludge to avoid UnmarshalException
    Set domain_set = new Integer2DSet(((FunctionType) data.getType()).getDomain(),
                                      4, 4);
From uwvax!uwm.edu!vixen.cso.uiuc.edu!howland.erols.net!newsfeed.internetmci.com!193.
174.75.126!news-was.dfn.de!news-fra1.dfn.de!news-ber1.dfn.de!news-lei1.dfn.de!news-nu
e1.dfn.de!uni-erlangen.de!lrz-muenchen.de!not-for-mail Sun Nov 30 11:10:19 CST 1997
Article: 111408 of comp.lang.java.programmer
Path: uwvax!uwm.edu!vixen.cso.uiuc.edu!howland.erols.net!newsfeed.internetmci.com!193
.174.75.126!news-was.dfn.de!news-fra1.dfn.de!news-ber1.dfn.de!news-lei1.dfn.de!news-n
ue1.dfn.de!uni-erlangen.de!lrz-muenchen.de!not-for-mail
From: "Rainer Frömming" <froemmin@informatik.tu-muenchen.de>
Newsgroups: comp.lang.java.programmer
Subject: RMI Unmarshaling Exception
Date: Thu, 27 Nov 1997 17:45:19 +0100
Organization: [posted via] Leibniz-Rechenzentrum, Muenchen (Germany)
Lines: 20
Distribution: world
Message-ID: <65k7u3$h66$1@sparcserver.lrz-muenchen.de>
NNTP-Posting-Host: dial024.lrz-muenchen.de
X-Newsreader: Microsoft Outlook Express 4.71.1712.3
X-MimeOLE: Produced By Microsoft MimeOLE V4.71.1712.3
 
Hi,
 
I am using RMI-Calls and I get an unmarshaling Eception if the
Return-Parameters get to big.
 
Eg. I return a 200kb String and the program crashes.
If the string is about 20kb it works...   Is there a workaround for this?
 
I need this, because I want to transfer VRML-Files to add them to a Scene
via createVrmlFromString().
Is it possible to use createVrmlFromUrl for -adding- Vrml_objects to a
scene, or does it create a new scene ?
 
 
Please Help,
 
Ray

*/

    float[][] domain_values = null;
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length = domain_set.getLength();

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

    // get values from Function Domain
    // NOTE - may defer this until needed, if needed
    if (domain_values == null) {
      domain_values = domain_set.getSamples(false);
      // convert values to default units (used in display)
      domain_values = Unit.convertTuple(domain_values, dataUnits, domain_units);
    }
 
    // System.out.println("got domain_values");

    // map domain_values to appropriate DisplayRealType-s
    // MEM
    ShadowRealType[] DomainComponents =
      ((ShadowFunctionOrSetType) adaptedShadowType).getDomainComponents();
    mapValues(display_values, domain_values, DomainComponents);
 
    // System.out.println("mapped domain_values");

    ShadowRealTupleType domain_reference = Domain.getReference();
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
        ((ShadowFunctionOrSetType) adaptedShadowType).getDomainReferenceComponents();
      mapValues(display_values, reference_values, DomainReferenceComponents);
      // FREE
      reference_values = null;
    }
    // FREE
    domain_values = null;

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

      // assemble an array of Display.DisplaySpatialCartesianTuple values
      // and possibly spatial_set
      float[][] spatial_values = new float[3][];

      // spatialDimensions[0] = spatialDomainDimension and
      // spatialDimensions[1] = spatialManifoldDimension
      int[] spatialDimensions = new int[2];

      boolean anyContour =
        ((ShadowFunctionOrSetType) adaptedShadowType).getAnyContour();
      boolean anyFlow =
        ((ShadowFunctionOrSetType) adaptedShadowType).getAnyFlow();

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

      // System.out.println("assembleColor, color_length = " + color_length);

      Appearance appearance; // J3D
      TransparencyAttributes constant_alpha = new TransparencyAttributes(); // J3D
      constant_alpha.setTransparencyMode(mode.getTransparencyMode());
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
          constant_alpha =
            new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
        }
        else {
          // note transparency 0.0 = opaque, 1.0 = clear
          constant_alpha.setTransparency(1.0f - color_values[3][0]);
        }
        // remove alpha from color_values
        float[][] c = new float[3][];
        c[0] = color_values[0];
        c[1] = color_values[1];
        c[2] = color_values[2];
        color_values = c;
      }
      if (color_length == 1) {
        if (color_values[0][0] != color_values[0][0] ||
            color_values[1][0] != color_values[1][0] ||
            color_values[2][0] != color_values[2][0]) {
          // System.out.println("single missing color");
          // a single missing color value, so render nothing
          return false;
        }
/*
        System.out.println("single color " + color_values[0][0] + " " +
                           color_values[1][0] + " " + color_values[2][0]);
*/
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
          //
          // TO_DO
          // test Contour
          //
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
                  array = spatial_set.makeIsoSurface(fvalues[0],
                              display_values[i], color_values);
                  // System.out.println("makeIsoSurface");
                  // MEM
                  GeometryArray geometry = display.makeGeometry(array);
                  //  FREE
                  array = null;
                  appearance = makeAppearance(mode, constant_alpha,
                                              constant_color, geometry);
                  Shape3D shape = new Shape3D(geometry, appearance);
                  group.addChild(shape);
                  anyContourCreated = true;
                }
                else if (spatialManifoldDimension == 2) {
                  VisADGeometryArray[] arrays =
                    spatial_set.makeIsoLines(fvalues[1], fvalues[2], fvalues[3],
                                  fvalues[4], display_values[i], color_values);
                  // MEM
                  GeometryArray geometry = display.makeGeometry(arrays[0]);
                  //  FREE
                  arrays[0] = null;
                  appearance = makeAppearance(mode, constant_alpha,
                                              constant_color, geometry);
                  Shape3D shape = new Shape3D(geometry, appearance);
                  group.addChild(shape);
                  if (bvalues[1]) {
                    // System.out.println("makeIsoLines with labels");
                    // draw labels
                    // MEM
                    geometry = display.makeGeometry(arrays[2]);
                    //  FREE
                    arrays = null;
                  }
                  else {
                    // System.out.println("makeIsoLines without labels");
                    // fill in contour lines in place of labels
                    // MEM
                    geometry = display.makeGeometry(arrays[1]);
                    //  FREE
                    arrays = null;
                  }
                  appearance = makeAppearance(mode, constant_alpha,
                                              constant_color, geometry);
                  shape = new Shape3D(geometry, appearance);
                  group.addChild(shape);
                  anyContourCreated = true;
                } // end if (spatialManifoldDimension == 3 or 2)
              } // end if (bvalues[0])
            } // end if (real.equals(Display.IsoContour) && not inherited)
          } // end for (int i=0; i<valueArrayLength; i++)
        } // end if (anyContour)
        if (!anyContourCreated && !anyFlowCreated) {
          // MEM
          if (range_select[0] != null) {
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
        } // end if (!anyContour)

        return false;
/*
        throw new UnimplementedException("ShadowFunctionOrSetType.doTransform: " +
                                         "terminal SIMPLE_FIELD");
*/
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
        if (range_select[0][i] == range_select[0][i]) {
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
  public void postProcess(Group group) throws VisADException { // J3D
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
      Range.postProcess(group);
    }
    AccumulationVector.removeAllElements();
  }

}

