
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

  private Vector AccumulationVector = new Vector();

  /** value_indices from parent */
  private int[] inherited_values;


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
    RangeComponents = getComponents(Range, (FunctionType) Type);
  }

  private static ShadowRealType[] getComponents(ShadowType type, FunctionType ftype)
                 throws VisADException {
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
    }
    else if (type instanceof ShadowTupleType) {
      int n = ftype.getRealComponents().length;
      reals = new ShadowRealType[n];
      int j = 0;
      int m = ((ShadowTupleType) type).getDimension();
      for (int i=0; i<m; i++) {
        ShadowType component = ((ShadowTupleType) type).getComponent(i);
        if (component instanceof ShadowRealType ||
            component instanceof ShadowRealTupleType) {
          ShadowRealType[] r = getComponents(component, null);
          for (int k=0; k<r.length; k++) {
            reals[j] = r[k];
            j++;
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
          getComponents(((ShadowTupleType) type).getComponent(i), null);
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

  /*
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

      if (((ShadowRealTupleType) Range).getDisplaySpatialTuple().equals(
          Display.DisplaySpatialCartesianTuple)) {
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
  public boolean doTransform(Group group, Data data, double[] value_array)
         throws VisADException, RemoteException {

    int valueArrayLength = display.getValueArrayLength();
    // mapping from ValueArray to DisplayScalar
    int[] valueToScalar = display.getValueToScalar();
    Set domain_set = new Integer2DSet(((FunctionType) data.getType()).getDomain(),
                                      4, 4);
    // Set domain_set = ((Field) data).getDomainSet();

/* 
demedici% java visad.RemoteClientTestImpl
FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
FlatField  missing
 
Display
    ScalarMap: Latitude -> DisplayXAxis
    ScalarMap: Longitude -> DisplayYAxis
    ScalarMap: ir_radiance -> DisplayZAxis
    ScalarMap: vis_radiance -> DisplayRGB
    ConstantMap: 0.5 -> DisplayAlpha
 
RemoteClientTestImpl.main: begin remote activity
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
java.rmi.UnmarshalException: Error unmarshaling return; nested exception is: 
        java.io.EOFException: Expecting code
        at visad.RemoteFieldImpl_Stub.getDomainSet(RemoteFieldImpl_Stub.java:1691)
        at visad.ShadowFunctionType.doTransform(ShadowFunctionType.java:305)
        at visad.DefaultRenderer.doTransform(DefaultRenderer.java:67)
        at visad.Renderer.doAction(Renderer.java:141)
        at visad.DisplayImpl.doAction(DisplayImpl.java:327)
        at visad.ActionImpl.run(ActionImpl.java:80)
        at java.lang.Thread.run(Thread.java)
visad.VisADError: Action.run: java.rmi.UnmarshalException: Error unmarshaling return; nested exception is: 
        java.io.EOFException: Expecting code
        at visad.ActionImpl.run(ActionImpl.java:102)
        at java.lang.Thread.run(Thread.java)
 
delay
 
 
delay
 
demedici% 
*/

    double[][] domain_values = null;
    Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
    int domain_length = domain_set.getLength();

    // NOTE - may defer this until needed
    if (domain_values == null) {
      domain_values = domain_set.indexToValue(domain_set.getWedge());
      // convert values to default units (used in display)
      domain_values =
        Unit.convertTuple(domain_values, ((Field) data).getDomainUnits(),
                          domain_units);
/*
  hopefully fixed by adding equals method to BaseUnit
/*
demedici% java visad.RemoteClientTestImpl
FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
FlatField  missing
 
Display
    ScalarMap: Latitude -> DisplayXAxis
    ScalarMap: Longitude -> DisplayYAxis
    ScalarMap: ir_radiance -> DisplayZAxis
    ScalarMap: vis_radiance -> DisplayRGB
    ConstantMap: 0.5 -> DisplayAlpha
 
RemoteClientTestImpl.main: begin remote activity
LevelOfDifficulty = 2 Type = FunctionType (Real): (Latitude, Longitude) -> (vis_radiance, ir_radiance)
 LevelOfDifficulty = 3 isDirectManipulation = true
visad.UnitException: Attempt to convert from unit "radian" to unit "radian"
        at visad.DerivedUnit.toThis(DerivedUnit.java:490)
        at visad.ScaledUnit.toThis(ScaledUnit.java:262)
        at visad.Unit.toThis(Unit.java:310)
        at visad.Unit.convertTuple(Unit.java:50)
        at visad.ShadowFunctionType.doTransform(ShadowFunctionType.java:355)
        at visad.DefaultRenderer.doTransform(DefaultRenderer.java:67)
        at visad.Renderer.doAction(Renderer.java:141)
        at visad.DisplayImpl.doAction(DisplayImpl.java:327)
        at visad.ActionImpl.run(ActionImpl.java:80)
        at java.lang.Thread.run(Thread.java)
visad.VisADError: Action.run: visad.UnitException: Attempt to convert from unit "radian" to unit "radian"
        at visad.ActionImpl.run(ActionImpl.java:98)
        at java.lang.Thread.run(Thread.java)
 
delay
 
 
delay
 
demedici% 
*/
    }
    //
    // TO_DO
    // map domain_values to appropriate DisplayRealType-s

    ShadowRealTupleType domain_reference = Domain.getReference();
    if (domain_reference != null && domain_reference.getMappedDisplayScalar()) {
      // apply coordinate transform to domain values
      RealTupleType ref = (RealTupleType) domain_reference.getType();
      double[][] reference_values =
        CoordinateSystem.transformCoordinates(
          ref, null, ref.getDefaultUnits(), null,
          (RealTupleType) Domain.getType(),
          ((Field) data).getDomainCoordinateSystem(),
          domain_units, null, domain_values);

      //
      // TO_DO
      // map reference_values to appropriate DisplayRealType-s

    }

    // ****
    // NOTE - double[][] Field.getValues() in defaultUnits for Type
    // ****

    //
    // TO_DO
    // get range_values  for RealType and RealTupleType
    // components: may just call getValues, then convert
    // Unit-s and transform CoordinateSsystem-s
    //
    // TO_DO
    // map range_values to appropriate DisplayRealType-s

    if (Flat) {
      if (!isTerminal) return false;


      //
      // TO_DO
      // assemble arrays of spatialTuple values, then possibly
      // transform to Display.DisplaySpatialCartesianTuple values
      double[][] spatial_values; // double[spatialDimension][domain_length]
   
      if (Domain.getAllSpatial()) {
        //
        // TO_DO
        // create a spatial Set by copying topology from domain_set;
        // will have same ManifoldDimension as domain_set, but perhaps 
        // greater DomainDimension (e.g., Irregular3DSet with
        // ManifoldDimension = 2 - valueToIndex and valueToInterp
        // return UnimplementedException);
        // requires permutation;
        //
        // ****
        // NOTE - requires new Set methods;
        // ****
        //
        if (domain_reference != null && domain_reference.getAllSpatial()) {
        }
        else {
        }
      }
      else {
        //
        // TO_DO
        // create a spatial Set with irregular topology and spatialDimension
        //
        // if spatialTuple != Display.DisplaySpatialCartesianTuple then
        // create a Set in Display.DisplaySpatialCartesianTuple by copying
        // topology from Set in spatialTuple
        //
      }


      if (LevelOfDifficulty == LEGAL) {
        // add values to value_array according to SelectedMapVector-s
        // of RealType-s in Domain (including Reference) and Range
        //
        // accumulate Vector of value_array-s at this ShadowType,
        // to be rendered in a post-process to scanning data
/*
        return true;
*/
        throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                         "terminal LEGAL");
      }
      else {
        // must be LevelOfDifficulty == SIMPLE_FIELD
        // only manage Spatial, Contour, Flow, Color and Alpha here
        // (account for Domain Reference)
        //
        // Flow will be tricky - FlowControl must contain trajectory
        // start points
/*
        Group data_group = null;
        group.addChild(data_group);

        DO_ME

        May inherit Spatial, Color & Alpha values from parent nodes in:

        inherited_values

        Dtype, Domain.getDisplayIndices()
        Rtype, Range.getDisplayIndices()

        Domain.getValueIndices(), Range.getValueIndices()
        Domain.getReference()

        if (!(data instanceof Field)) {
          throw new TypeException("ShadowFunctionType.doTransform: " +
                                  "terminal SIMPLE_FIELD must be Field");
        }

        Set domain_set = ((Field) data).getDomainSet();
        int[] domain_wedge = domain_set.getWedge();
        double[][] domain_values = domain_setindexToValue(domain_wedge);

        double[][] range_values = ((Field) data).getValues();
        int n = Range.getDimension();
        for (int i=0; i<n; i++) {
          ShadowType component = Range.getComponent(1);
          if (component instanceof ShadowRealTupleType) {
            ShadowRealTupleType reference =
              ((ShadowRealTupleType) component).getReference();
          }
          else if (component instanceof ShadowRealType) {
          }
          else {
          }
        }
*/

        throw new UnimplementedException("ShadowFunctionType.doTransform: " +
                                         "terminal SIMPLE_FIELD");
      }
    }
    else { // !Flat
      boolean post = false;
      // add values to value_array according to SelectedMapVector-s
      // of RealType-s in Domain (including Reference), and
      // recursively call doTransform on Range values
/*
      for (int i=0; i<num_samples; i++) {
        post |= Range.doTransform(group, range_data, value_array);
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

