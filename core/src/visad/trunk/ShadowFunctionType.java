
//
// ShadowFunctionType.java
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

package visad;

import javax.media.j3d.*;
import java.vecmath.*;

import java.util.*;
import java.rmi.*;

/**
   The ShadowFunctionType class shadows the FunctionType class,
   within a DataDisplayLink.<P>
*/
public class ShadowFunctionType extends ShadowType {

  private ShadowRealTupleType Domain;
  private ShadowType Range;
  /** this is an array of ShadowRealType-s that are ShadowRealType
      components of Range or ShadowRealType components of
      ShadowRealTupleType components of Range;
      a non-ShadowRealType and non-ShadowTupleType Range is marked
      by null;
      components of a ShadowTupleType Range that are neither
      ShadowRealType nor ShadowRealTupleType are ignored */
  private ShadowRealType[] RangeComponents;
  private ShadowRealType[] DomainComponents;
  private ShadowRealType[] DomainReferenceComponents;

  private Vector AccumulationVector = new Vector();

  /** value_indices from parent */
  private int[] inherited_values;

  /** used by getComponents to record RealTupleTypes in range
      with coordinate transforms */
  int[] refToComponent;
  ShadowRealTupleType[] componentWithRef;
  int[] componentIndex;

  /** true if range is ShadowRealType or Flat ShadowTupleType
      not the same as FunctionType.Flat */
  private boolean Flat;

  ShadowFunctionType(MathType t, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(t, link, parent);
    Domain = (ShadowRealTupleType)
             ((FunctionType) Type).getDomain().buildShadowType(link, this);
    Range = ((FunctionType) Type).getRange().buildShadowType(link, this);
    Flat = (Range instanceof ShadowRealType) ||
           (Range instanceof ShadowTupleType && ((ShadowTupleType) Range).isFlat());
    MultipleDisplayScalar = Domain.getMultipleDisplayScalar() ||
                            Range.getMultipleDisplayScalar();
    MappedDisplayScalar = Domain.getMappedDisplayScalar() ||
                            Range.getMappedDisplayScalar();
    RangeComponents = getComponents(Range, true);
    DomainComponents = getComponents(Domain, false);
    DomainReferenceComponents = getComponents(Domain.getReference(), false);
  }

  private ShadowRealType[] getComponents(ShadowType type, boolean doRef)
          throws VisADException {
    if (type == null) return null;
    if (doRef) {
      refToComponent = null;
      componentWithRef = null;
      componentIndex = null;
    }
    ShadowRealType[] reals;
    if (type instanceof ShadowRealType) {
      ShadowRealType[] r = {(ShadowRealType) type};
      return r;
    }
    else if (type instanceof ShadowRealTupleType) {
      int n = ((ShadowRealTupleType) type).getDimension();
      reals = new ShadowRealType[n];
      for (int i=0; i<n; i++) {
        reals[i] = (ShadowRealType) ((ShadowRealTupleType) type).getComponent(i);
      }
      if (doRef) {
        ShadowRealTupleType ref =
          ((ShadowRealTupleType) type).getReference();
        if (ref != null && ref.getMappedDisplayScalar()) {
          refToComponent = new int[1];
          componentWithRef = new ShadowRealTupleType[1];
          componentIndex = new int[1];
          refToComponent[0] = 0;
          componentWithRef[0] = (ShadowRealTupleType) type;
          componentIndex[0] = 0;
        }
      }
    }
    else if (type instanceof ShadowTupleType) {
      int m = ((ShadowTupleType) type).getDimension();
      int n = 0;
      int nref = 0;
      for (int i=0; i<m; i++) {
        ShadowType component = ((ShadowTupleType) type).getComponent(i);
        if (component instanceof ShadowRealType) {
          n++;
        }
        else if (component instanceof ShadowRealTupleType) {
          n += getComponents(component, false).length;
          if (doRef) {
            ShadowRealTupleType ref =
              ((ShadowRealTupleType) component).getReference();
            if (ref != null && ref.getMappedDisplayScalar()) nref++;
          }
        }
        else {
          n++;
        }
      }
      reals = new ShadowRealType[n];
      int j = 0;
      if (nref == 0) doRef = false;
      if (doRef) {
        refToComponent = new int[nref];
        componentWithRef = new ShadowRealTupleType[nref];
        componentIndex = new int[nref];
      }
      int rj = 0;
      for (int i=0; i<m; i++) {
        ShadowType component = ((ShadowTupleType) type).getComponent(i);
        if (component instanceof ShadowRealType ||
            component instanceof ShadowRealTupleType) {
          ShadowRealType[] r = getComponents(component, false);
          for (int k=0; k<r.length; k++) {
            reals[j] = r[k];
            j++;
          }
          if (doRef && component instanceof ShadowRealTupleType) {
            ShadowRealTupleType ref = 
              ((ShadowRealTupleType) component).getReference();
            if (ref != null && ref.getMappedDisplayScalar()) {
              refToComponent[rj] = j;
              componentWithRef[rj] = (ShadowRealTupleType) component;
              componentIndex[rj] = i;
              rj++;
            }
          }
        }
        else {
          reals[j] = null;
          j++;
        }
      }
    }
    else {
      reals = null;
    }
    return reals;
/* WLH 16 Oct 97
    if (type instanceof ShadowRealTupleType) {
      int n = ((ShadowRealTupleType) type).getDimension();
      reals = new ShadowRealType[n];
      for (int i=0; i<n; i++) {
        reals[i] = (ShadowRealType) ((ShadowRealTupleType) type).getComponent(i);
      }
    }
    else if (type instanceof ShadowRealType) {
      ShadowRealType[] r = {(ShadowRealType) type};
      return r;
    }
    else {
      int n = ftype.getFlatRange().getDimension();
      reals = new ShadowRealType[n];
      int j = 0;
      int m = ((ShadowTupleType) type).getDimension();
      for (int i=0; i<m; i++) {
        ShadowRealType[] r =
          getComponents(((ShadowTupleType) type).getComponent(i), false);
        for (int k=0; k<r.length; k++) {
          reals[j] = r[k];
          j++;
        }
      }
    }
    return reals;
*/
  }

  /** used by FlatField.computeRanges */
  int[] getRangeDisplayIndices() {
    int n = RangeComponents.length;
    int[] indices = new int[n];
    for (int i=0; i<n; i++) {
      indices[i] = RangeComponents[i].getIndex();
    }
    return indices;
  }

  /** checkIndices:
    if (Flat) {
      terminal, so check condition 4
    }
    else {
      check condition 2 on domain
      pass levelOfDifficulty down to range.checkIndices
    }
  */
  int checkIndices(int[] indices, int[] display_indices, int[] value_indices,
                   boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from Domain
    int[] local_indices = Domain.sumIndices(indices);
    int[] local_display_indices = Domain.sumDisplayIndices(display_indices);
    int[] local_value_indices = Domain.sumValueIndices(value_indices);

    if (Domain.testTransform()) Domain.markTransform(isTransform);
    Range.markTransform(isTransform);

    // get value_indices arrays used by doTransform
    inherited_values = copyIndices(value_indices);

    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(local_display_indices)) {
        levelOfDifficulty = NESTED;
      }
    }

    // test legality of Animation and SelectValue in Domain
    if (Domain.getDimension() != 1 &&
        checkAnimationOrValue(Domain.getDisplayIndices())) {
      throw new BadMappingException("ShadowFunctionType.checkIndices: " +
                                    "Animation and SelectValue may only occur " +
                                    "in 1-D Function domain");
    }

    anyContour = checkContour(local_display_indices);
    anyFlow = checkFlow(local_display_indices);

    if (Flat) {
      // test legality of Animation and SelectValue in Range
      if (checkAnimationOrValue(Range.getDisplayIndices())) {
        throw new BadMappingException("ShadowFunctionType.checkIndices: " +
                                      "Animation and SelectValue may not " +
                                      "occur in Function range");
      }

      LevelOfDifficulty =
        testIndices(local_indices, local_display_indices, levelOfDifficulty);

      // compute Domain type
      if (!Domain.getMappedDisplayScalar()) {
        Dtype = D0;
      }
      else if (Domain.getAllSpatial() && checkR4(Domain.getDisplayIndices())) {
        Dtype = D1;
      }
      else if (checkR1D3(Domain.getDisplayIndices())) {
        Dtype = D3;
      }
      else if (checkR2D2(Domain.getDisplayIndices())) {
        Dtype = D2;
      }
      else {
        Dtype = Dbad;
      }

      // compute Range type
      if (!Range.getMappedDisplayScalar()) {
        Rtype = R0;
      }
      else if (checkR1D3(Range.getDisplayIndices())) {
        Rtype = R1;
      }
      else if (checkR2D2(Range.getDisplayIndices())) {
        Rtype = R2;
      }
      else if (checkR3(Range.getDisplayIndices())) {
        Rtype = R3;
      }
      else if (checkR4(Range.getDisplayIndices())) {
        Rtype = R4;
      }
      else {
        Rtype = Rbad;
      }

      if (LevelOfDifficulty == NESTED) {
        if (Dtype != Dbad && Rtype != Rbad) {
          LevelOfDifficulty = SIMPLE_FIELD;
        }
        else {
          LevelOfDifficulty = LEGAL;
        }
      }
    }
    else { // !Flat
      if (levelOfDifficulty == NESTED) {
        if (!checkNested(Domain.getDisplayIndices())) {
          levelOfDifficulty = LEGAL;
        }
      }
      LevelOfDifficulty = Range.checkIndices(local_indices, local_display_indices,
                                             local_value_indices, isTransform,
                                             levelOfDifficulty);
    } 
    return LevelOfDifficulty;
  }

  void checkDirect() {
    if (!((FunctionType) Type).getFlat()) return; // OR should this be !Real ??
    if (LevelOfDifficulty == SIMPLE_FIELD && !MultipleDisplayScalar) {
      if (Range instanceof ShadowRealTupleType &&
          Display.DisplaySpatialCartesianTuple.equals(
          ((ShadowRealTupleType) Range).getDisplaySpatialTuple() )) {
        // at least one RealType in Range is mapped to a spatial
        // DisplayRealType (i.e., DisplaySpatialTuple != null)
        // and is not mapped through any (Display) CoordinateSystem
        // (i.e., DisplaySpatialTuple.equals(DisplaySpatialCartesianTuple) )
        isDirectManipulation = true;
      }
      else if (Range instanceof ShadowRealType &&
          Display.DisplaySpatialCartesianTuple.equals(
          ((ShadowRealType) Range).getDisplaySpatialTuple() )) {
        // at least one RealType in Range is mapped to a spatial
        // DisplayRealType (i.e., DisplaySpatialTuple != null)
        // and is not mapped through any (Display) CoordinateSystem
        // (i.e., DisplaySpatialTuple.equals(DisplaySpatialCartesianTuple) )
        isDirectManipulation = true;
      }
    }
  }

  public ShadowRealTupleType getDomain() {
    return Domain;
  }

  public ShadowType getRange() {
    return Range;
  }

  /** mark Control-s as needing re-Transform */
  void markTransform(boolean[] isTransform) {
    Range.markTransform(isTransform);
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
    Range.preProcess();
  }

  /** transform data into a Java3D scene graph;
      return true if need post-process */
  public boolean doTransform(Group group, Data data, float[] value_array,
                             float[] default_values)
         throws VisADException, RemoteException {

    int valueArrayLength = display.getValueArrayLength();
    // mapping from ValueArray to DisplayScalar
    int[] valueToScalar = display.getValueToScalar();
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();


    if (!(data instanceof Field)) {
      throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                       "data must be Field");
    }
    Set domain_set = ((Field) data).getDomainSet();

/** kludge to avoid UnmarshalException
    Set domain_set = new Integer2DSet(((FunctionType) data.getType()).getDomain(),
                                      4, 4);
*/

    float[][] domain_values = null;
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length = domain_set.getLength();

    // array to hold values for various mappings
    float[][] display_values = new float[valueArrayLength][];

    // get values inherited from parent
    // assume these do not include SelectRange, SelectValue
    // or Animation values
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
      Unit[] new_units = ((Function) data).getDomainUnits();
      domain_values = Unit.convertTuple(domain_values,
                        ((Function) data).getDomainUnits(), domain_units);
    }
 
    // didn't even get here with 'java visad.DisplayImpl'
    System.out.println("got domain_values");

    // map domain_values to appropriate DisplayRealType-s
    // MEM
    mapValues(display_values, domain_values, DomainComponents);
 
    System.out.println("mapped domain_values");

    ShadowRealTupleType domain_reference = Domain.getReference();
    if (domain_reference != null && domain_reference.getMappedDisplayScalar()) {
      // apply coordinate transform to domain values
      RealTupleType ref = (RealTupleType) domain_reference.getType();
      // MEM
      float[][] reference_values =
        CoordinateSystem.transformCoordinates(
          ref, null, ref.getDefaultUnits(), null,
          (RealTupleType) Domain.getType(),
          ((Function) data).getDomainCoordinateSystem(),
          domain_units, null, domain_values);

      // map reference_values to appropriate DisplayRealType-s
      // MEM
      mapValues(display_values, reference_values, DomainReferenceComponents);
      // FREE
      reference_values = null;
    }
    // FREE
    domain_values = null;

    if (!(data instanceof Field)) {
      throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                       "data must be Field");
    }
    // get range_values for RealType and RealTupleType
    // components, in defaultUnits for RealType-s
    // MEM
    double[][] range_values = ((Field) data).getValues();

    System.out.println("got range_values");

    if (range_values != null) {
      // map range_values to appropriate DisplayRealType-s
      // MEM
      mapValues(display_values, range_values, RangeComponents);
 
      System.out.println("mapped range_values");

      //
      // transform any range CoordinateSystem-s
      // into display_values, then mapValues
      //
      // NOTE - currently only works for FlatField;
      //        should probably hack FieldImpl.getValues
      //
      if (refToComponent != null) {
        if (!(data instanceof FlatField)) {
          throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                        "range coord transform for FieldImpl");
        }
        for (int i=0; i<refToComponent.length; i++) {
          int n = componentWithRef[i].getDimension();
          int start =   refToComponent[i];
          double[][] values = new double[n][];
          for (int j=0; j<n; j++) values[j] = range_values[j + start];
          ShadowRealTupleType component_reference =
            componentWithRef[i].getReference();
          RealTupleType ref = (RealTupleType) component_reference.getType();
          Unit[] range_units;
          CoordinateSystem range_coord_sys;
          if (i == 0 && componentWithRef[i].equals(Range)) {
            range_units = ((FlatField) data).getRangeUnits();
            range_coord_sys = ((FlatField) data).getRangeCoordinateSystem();
          }
          else {
            Unit[] dummy_units = ((FlatField) data).getRangeUnits();
            range_units = new Unit[n];
            for (int j=0; j<n; j++) range_units[j] = dummy_units[j + start];
            range_coord_sys =
              ((FlatField) data).getRangeCoordinateSystem(componentIndex[i]);
          }

          // MEM
          double[][] reference_values =
            CoordinateSystem.transformCoordinates(
              ref, null, ref.getDefaultUnits(), null,
              (RealTupleType) componentWithRef[i].getType(),
              range_coord_sys, range_units, null, values);
 
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

    if (Flat) {
      if (!isTerminal) return false;

      //
      // NOTE -
      // currently assuming SelectRange changes require Transform
      // see Renderer.isTransformControl
      //
      // get array that composites SelectRange components
      // MEM
      float[] range_select =
        assembleSelect(display_values, domain_length, valueArrayLength,
                       valueToScalar, display);

      System.out.println("assembleSelect");
 
      if (anyFlow) {
        throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                         "Flow rendering");
      }

      // assemble an array of Display.DisplaySpatialCartesianTuple values
      // and possibly spatial_set
      float[][] spatial_values = new float[3][];

      GraphicsModeControl mode = display.getGraphicsModeControl();
      boolean pointMode = mode.getPointMode();

      // spatialDomainDimension and spatialManifoldDimension
      int[] spatialDimensions = new int[2];

      // MEM
      Set spatial_set = 
        assembleSpatial(spatial_values, display_values, valueArrayLength,
                        valueToScalar, display, default_values,
                        inherited_values, domain_set, Domain.getAllSpatial(),
                        (anyContour || anyFlow), pointMode, spatialDimensions);

      int spatialDomainDimension = spatialDimensions[0];
      int spatialManifoldDimension = spatialDimensions[1];

      // got here with 'java -mx24m visad.DisplayImpl'
      System.out.println("assembleSpatial");
 
      int spatial_length = Math.min(domain_length, spatial_values[0].length);

      // assemble an array of RGBA values
      // MEM
      float[][] color_values =
        assembleColor(display_values, valueArrayLength, valueToScalar,
                      display, default_values);
      int color_length = Math.min(domain_length, color_values[0].length);
      int alpha_length = color_values[3].length;

      System.out.println("assembleColor, color_length = " + color_length);

      if (LevelOfDifficulty == SIMPLE_FIELD) {
        // only manage Spatial, Contour, Flow, Color, Alpha and
        // SelectRange here
        //
        // Flow will be tricky - FlowControl must contain trajectory
        // start points

        Appearance appearance = makeDefaultAppearance(mode);

        // note alpha_length <= color_length
        if (alpha_length == 1) {
          if (color_values[3][0] != color_values[3][0]) {
            // a single missing alpha value, so render nothing
            System.out.println("single missing alpha");
            return false;
          }
          System.out.println("single alpha " + color_values[3][0]);
          // constant alpha, so put it in appearance
          TransparencyAttributes constant_alpha = null;
          if (color_values[3][0] > 0.999999f) {
            constant_alpha =
              new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
          }
          else {
            // note transparency 0.0 = opaque, 1.0 = clear
            constant_alpha =
              new TransparencyAttributes(mode.getTransparencyMode(),
                                         1.0f - color_values[3][0]);
          }
          appearance.setTransparencyAttributes(constant_alpha);
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
            System.out.println("single missing alpha");
            // a single missing color value, so render nothing
            return false;
          }
          System.out.println("single color " + color_values[0][0] + " " +
                             color_values[1][0] + " " + color_values[2][0]);
          // constant color, so put it in appearance
          ColoringAttributes constant_color = new ColoringAttributes();
          constant_color.setColor(color_values[0][0], color_values[1][0],
                                  color_values[2][0]);
          appearance.setColoringAttributes(constant_color);
          color_values = null;
        }

/* MISSING TEST
        for (int i=0; i<color_length; i+=3) {
          spatial_values[0][i] = Float.NaN;
        }
END MISSING TEST*/

        //
        // TO_DO
        // Contour
        //
        // TO_DO
        // missing color_values and select_values
        //
        // NaN color component values are rendered as 1.0
        // NaN spatial component values of points are NOT rendered
        //   for PointArray - how about LineArray and TriangleArray ????
        //

        System.out.println("spatialDomainDimension = " +
                           spatialDomainDimension +
                           " spatialManifoldDimension = " +
                           spatialManifoldDimension +
                           " anyContour = " + anyContour +
                           " pointMode = " + pointMode);

        VisADGeometryArray array;
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
                if (spatialManifoldDimension == 3) {
                  array = spatial_set.makeIsoSurface(fvalues[0],
                                             display_values[i], color_values);
                  System.out.println("makeIsoSurface");
                  // MEM
                  GeometryArray geometry = array.makeGeometry();
                  //  FREE
                  array = null;
                  Shape3D shape = new Shape3D(geometry, appearance);
                  group.addChild(shape);
                  anyContourCreated = true;
                }
                else if (spatialManifoldDimension == 2) {
                  VisADGeometryArray[] arrays =
                    spatial_set.makeIsoLines(fvalues[1], fvalues[2], fvalues[3],
                                  fvalues[4], display_values[i], color_values);
                  // MEM
                  GeometryArray geometry = arrays[0].makeGeometry();
                  //  FREE
                  arrays[0] = null;
                  Shape3D shape = new Shape3D(geometry, appearance);
                  group.addChild(shape);
                  if (bvalues[1]) {
                    System.out.println("makeIsoLines with labels");
                    // draw labels
                    // MEM
                    geometry = arrays[2].makeGeometry();
                    //  FREE
                    arrays = null;
                  }
                  else {
                    System.out.println("makeIsoLines without labels");
                    // fill in contour lines in place of labels
                    // MEM
                    geometry = arrays[1].makeGeometry();
                    //  FREE
                    arrays = null;
                  }
                  shape = new Shape3D(geometry, appearance);
                  group.addChild(shape);
                  anyContourCreated = true;
                }
              }
            }
          } // end for (int i=0; i<valueArrayLength; i++)
        } // end if (anyContour)
        if (!anyContourCreated) {
          // MEM
          if (pointMode) {
            array = makePointGeometry(spatial_values, color_values);
            System.out.println("makePointGeometry  for pointMode");
          }
          else if (spatialManifoldDimension == 1) {
            array = spatial_set.make1DGeometry(color_values);
            System.out.println("make1DGeometry");
          }
          else if (spatialManifoldDimension == 2) {
            array = spatial_set.make2DGeometry(color_values);
            System.out.println("make2DGeometry");
          }
          else if (spatialManifoldDimension == 3) {
            array = makePointGeometry(spatial_values, color_values);
            System.out.println("makePointGeometry  for 3D");
            //
            // when make3DGeometry is implemented:
            // array = spatial_set.make3DGeometry(color_values);
            //
          }
          else if (spatialManifoldDimension == 0) {
            array = spatial_set.makePointGeometry(color_values);
            System.out.println("makePointGeometry  for 0D");
          }
          else {
            throw new DisplayException("ShadowFunctionType.doTransform: " +
                                       "bad spatialManifoldDimension");
          }
          // got here with 'java -mx32m visad.DisplayImpl'
  
          // MEM
          GeometryArray geometry = array.makeGeometry();
          System.out.println("array.makeGeometry");
  
          //  FREE
          array = null;
          Shape3D shape = new Shape3D(geometry, appearance);
          group.addChild(shape);
        } // end if (!anyContour)

        // got to here with 'java -mx40m visad.DisplayImpl'
        return false;
/*
        if (anyContour) {
          // apply colors to contours
        }
        else {
          // render 1-D, 2-D or 3-D depending on
          // spatial_set.ManifoldDimension
        }
*/
/*
        throw new UnimplementedException("ShadowFunctionType.doTransform: " +
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
        throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                         "terminal LEGAL");
      }
    }
    else { // !Flat
      boolean post = false;
      // add values to value_array according to SelectedMapVector-s
      // of RealType-s in Domain (including Reference), and
      // recursively call doTransform on Range values

      //
      // TO_DO
      // SelectRange, SelectValue, Animation
      // Renderer.isTransformControl temporary hack:
      // SelectRange.isTransform,
      // !SelectValue.isTransform, !Animation.isTransform
      //

      // get array that composites SelectRange components
      // only if SelectRange components are 'isTransform'
      float[] range_select =
        assembleSelect(display_values, domain_length, valueArrayLength,
                       valueToScalar, display);

/*
      for (int i=0; i<num_samples; i++) {
        post |= Range.doTransform(group, range_data, value_array, default_values);
      }
      return post;
*/
      throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                       "not terminal");
    }
/*
    return false;
*/
  }
 
  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Group group) throws VisADException {
    if (Flat) {
      if (LevelOfDifficulty == LEGAL) {
/*
        Group data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("ShadowFunctionType.postProcess: " +
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

