
//
// ShadowType.java
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
   The ShadowType hierarchy shadows the MathType hierarchy,
   within a DataDisplayLink.<P>
*/
public abstract class ShadowType extends Object
       implements java.io.Serializable {

  // possible values for LevelOfDifficulty
  static final int NOTHING_MAPPED = 5;
  static final int SIMPLE_TUPLE = 4;
  static final int SIMPLE_FIELD = 3;
  static final int NESTED = 2;
  static final int LEGAL = 1;

  // basic information about this ShadowType
  MathType Type; // MathType being shadowed
  transient DataDisplayLink Link;
  transient DisplayImpl display;
  transient private Data data; // from Link.getData()
  private ShadowType Parent;

  // information calculated by constructors
  //
  // count of occurences of DisplayRealType-s
  // ShadowRealType: set for mappings to DisplayRealType-s
  // ShadowTupleType (incl ShadowRealTupleType): set to
  //   sum for ShadowRealType & ShadowRealTupleType components
  // ShadowRealTupleType: add contribution from Reference
  int[] DisplayIndices;
  // ValueIndices is like DisplayIndices, but distinguishes
  // different ScalarMap-s of non-Single DisplayRealTypes
  int[] ValueIndices;
  //
  // MultipleDisplayScalar is true if any RealType component is mapped
  // to multiple DisplayRealType-s, or if any RealTupleType component
  // and its Reference are both mapped
  boolean MultipleDisplayScalar;
  //
  // MappedDisplayScalar is true if any RealType component is mapped
  // to any DisplayRealType-s, including via a RealTupleType.Reference
  boolean MappedDisplayScalar;

  // information calculated by checkIndices & testIndices
  boolean isTerminal;
  int LevelOfDifficulty;

  // information calculated by checkDirect
  boolean isDirectManipulation;

  /** Dtype and Rtype used only with ShadowSetType and
      Flat ShadowFunctionType */
  int Dtype; // Domain Type: D0, D1, D2, D3 or Dbad
  int Rtype; // Range Type: R0, R1, R2, R3, R4 or Rbad
  /** possible values for Dtype */
  static final int D0 = 0; // (Unmapped)*
  static final int D1 = 1; // allSpatial + (IsoContour, Flow, Color, Alpha,
                           //               Range, Unmapped)*
  static final int D2 = 2; // (Spatial, Color, Alpha, Range, Unmapped)*
  static final int D3 = 3; // (Color, Alpha, Range, Unmapped)*
  static final int Dbad = 4;
  /** possible values for Rtype */
  static final int R0 = 0; // (Unmapped)*
  static final int R1 = 1; // (Color, Alpha, Range, Unmapped)*
  static final int R2 = 2; // (Spatial, Color, Alpha, Range, Unmapped)*
  static final int R3 = 3; // (IsoContour, Flow, Color, Alpha, Range,
                           //  Unmapped)*
  static final int R4 = 4; // (Spatial, IsoContour, Flow, Color, Alpha,
                           //  Range, Unmapped)*
  static final int Rbad = 5;

  /** spatial DisplayTupleType at terminal nodes */
  DisplayTupleType spatialTuple = null;
  /** number of spatial DisplayRealType components at terminal nodes */
  int spatialDimension;
  /** flags for any IsoContour or Flow at terminal nodes */
  boolean anyContour;
  boolean anyFlow;

  ShadowType(MathType type, DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    Type = type;
    Link = link;
    display = link.getDisplay();
    Parent = parent;
    data = link.getData();
    DisplayIndices = zeroIndices(display.getDisplayScalarCount());
    ValueIndices = zeroIndices(display.getValueArrayLength());
    isTerminal = false;
    LevelOfDifficulty = NOTHING_MAPPED;
    isDirectManipulation = false;
    MultipleDisplayScalar = false;
    MappedDisplayScalar = false;
  }

  ShadowType getParent() {
    return Parent;
  }

  Data getData() {
    return data;
  }

  /** create a zero'd array of indices (for each RealType or each DisplayRealType) */
  static int[] zeroIndices(int length) {
    int[] local_indices = new int[length];
    for (int i=0; i<length; i++) {
      local_indices[i] = 0;
    }
    return local_indices;
  }

  /** copy an array of indices (for each RealType or each DisplayRealType) */
  static int[] copyIndices(int[] indices) {
    int[] local_indices = new int[indices.length];
    for (int i=0; i<indices.length; i++) {
      local_indices[i] = indices[i];
    }
    return local_indices;
  }

  /** add arrays of indices (for each RealType or each DisplayRealType) */
  static int[] addIndices(int[] indices, int[] indices2) throws VisADException {
    if (indices.length != indices2.length) {
      throw new DisplayException("ShadowType.addIndices: lengths don't match");
    }
    int[] local_indices = new int[indices.length];
    for (int i=0; i<indices.length; i++) {
      local_indices[i] = indices[i] + indices2[i];
    }
    return local_indices;
  }

  /** test for display_indices in
      (Spatial, Color, Alpha, Animation, Range, Value, Unmapped) */
  boolean checkNested(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      DisplayTupleType tuple = real.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplaySpatialCartesianTuple)))) continue;  // Spatial
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (real.equals(Display.RGB) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.Animation) ||
          real.equals(Display.SelectValue) ||
          real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in
      (Spatial, IsoContour, Flow, Color, Alpha, Range, Unmapped) */
  boolean checkR4(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      DisplayTupleType tuple = real.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplaySpatialCartesianTuple)))) continue;  // Spatial
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (tuple != null &&
          (tuple.equals(Display.DisplayFlow1Tuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayFlow1Tuple)))) continue;  // Flow1
      if (tuple != null &&
          (tuple.equals(Display.DisplayFlow2Tuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayFlow2Tuple)))) continue;  // Flow2
      if (real.equals(Display.RGB) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.SelectRange) ||
          real.equals(Display.IsoContour)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in
      (IsoContour, Flow, Color, Alpha, Range, Unmapped) */
  boolean checkR3(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      DisplayTupleType tuple = real.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (tuple != null &&
          (tuple.equals(Display.DisplayFlow1Tuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayFlow1Tuple)))) continue;  // Flow1
      if (tuple != null &&
          (tuple.equals(Display.DisplayFlow2Tuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayFlow2Tuple)))) continue;  // Flow2
      if (real.equals(Display.RGB) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.SelectRange) ||
          real.equals(Display.IsoContour)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Color, Alpha, Range, Unmapped) */
  boolean checkR1D3(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      DisplayTupleType tuple = real.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (real.equals(Display.RGB) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Spatial, Color, Alpha, Range, Unmapped) */
  boolean checkR2D2(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      DisplayTupleType tuple = real.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplaySpatialCartesianTuple)))) continue;  // Spatial
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (real.equals(Display.RGB) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for any Animation or Value in display_indices */
  boolean checkAnimationOrValue(int[] display_indices)
          throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      if (real.equals(Display.Animation) ||
          real.equals(Display.SelectValue)) return true;
    }
    return false;
  }

  /** test for any IsoContour in display_indices */
  boolean checkContour(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      if (real.equals(Display.IsoContour)) return true;
    }
    return false;
  }

  /** test for any Flow in display_indices */
  boolean checkFlow(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      DisplayTupleType tuple = real.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplayFlow1Tuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayFlow1Tuple)))) return true;  // Flow1
      if (tuple != null &&
          (tuple.equals(Display.DisplayFlow2Tuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayFlow2Tuple)))) return true;  // Flow2
    }
    return false;
  }

  /** test for any non-zero display_indices */
  boolean checkAny(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] > 0) return true;
    }
    return false;
  }

  /** applied at terminal nodes */
  int testIndices(int[] indices, int[] display_indices, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // can apply ScalarMap-s as scan down MathType tree
    // make sure some DisplayRealType is mapped

    // test whether any RealType-s occur more than once
    for (int i=0; i<indices.length; i++) {
      RealType real = display.getScalar(i);
      if (indices[i] > 1) {
        throw new BadMappingException("ShadowType.testIndices: RealType " +
                                      real.getName() + " occurs more than once");
      }
    }

    // test whether any DisplayRealType occurs at least once;
    // test whether any Single DisplayRealType occurs more than once
    for (int i=0; i<display_indices.length; i++) {
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      if (display_indices[i] > 0) isTerminal = true;
      if (display_indices[i] > 1 && real.isSingle()) {
        throw new BadMappingException("ShadowType.testIndices: Single " +
                 "DisplayRealType " + real.getName() + " occurs more than once");
      }
    }

    // test whether DisplayRealType-s from multiple
    // spatial DisplayTupleType-s occur
    spatialTuple = null;
    spatialDimension = 0;
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] > 0) {
        DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
        DisplayTupleType rtuple = real.getTuple();
        if (rtuple != null) {
          if (rtuple.equals(Display.DisplaySpatialCartesianTuple) ||
              (rtuple.getCoordinateSystem() != null &&
               rtuple.getCoordinateSystem().getReference().equals(
               Display.DisplaySpatialCartesianTuple))) {
            if (spatialTuple != null && !spatialTuple.equals(rtuple)) {
              throw new BadMappingException("ShadowType.testIndices: " +
                "DisplayRealType-s occur from multiple spatial " +
                "DisplayTupleType-s");
            }
            spatialTuple = rtuple;
            spatialDimension++;
          }
        }
      }
    }

    if (isTerminal) {
      if (levelOfDifficulty == LEGAL) {
        LevelOfDifficulty = LEGAL;
      }
      else {
        LevelOfDifficulty = NESTED;
      }
    }
    else {
      // this is not illegal but also not a terminal node
      // (no DisplayRealType-s mapped)
      LevelOfDifficulty = NOTHING_MAPPED;
    }
    System.out.println("LevelOfDifficulty = " + LevelOfDifficulty +
                       " Type = " + Type);
    return LevelOfDifficulty;
  }

  /* DEPRECATE THIS, no longer needed because SetTypes, Flat
     FieldTypes and Flat TupleTypes are terminals:
     this defines the default logic for ShadowTextType and
     ShadowMissingType - which may occur as a Field Range and
     are treated as unmapped */
  /** scans ShadowType tree to determine display feasibility
      and precompute useful information for Data transform;
      indices & display_indices are counts (at leaves) of
      numbers of occurrences of RealTypes and DisplayRealTypes;
      isTransform flags for (Animation, Range, Value) re-transform;
      levelOfDifficulty passed down and up call chain */
  int checkIndices(int[] indices, int[] display_indices, int[] value_indices,
                   boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    LevelOfDifficulty = testIndices(indices, display_indices, levelOfDifficulty);
    return LevelOfDifficulty;
  }

  // default
  void checkDirect() {
    isDirectManipulation = false;
  }

  public boolean getMultipleDisplayScalar() {
    return MultipleDisplayScalar;
  }

  public boolean getMappedDisplayScalar() {
    return MappedDisplayScalar;
  }

  public int[] getDisplayIndices() {
    int[] ii = new int[DisplayIndices.length];
    for (int i=0; i<DisplayIndices.length; i++) ii[i] = DisplayIndices[i];
    return ii;
  }

  public int[] getValueIndices() {
    int[] ii = new int[ValueIndices.length];
    for (int i=0; i<ValueIndices.length; i++) ii[i] = ValueIndices[i];
    return ii;
  }

  /** return true if DisplayIndices include multiple
      Animation, SelectValue and SelectRange */
  boolean testTransform() {
    int count = 0;
    for (int i=0; i<DisplayIndices.length; i++) {
      if (DisplayIndices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      if (real.equals(Display.Animation) ||
          real.equals(Display.SelectValue) ||
          real.equals(Display.SelectRange)) {
        count++;
        if (count > 1) return true;
      }
    }
    return false;
  }

  /** mark Control-s as needing re-Transform;
      default for ShadowTextType and ShadowMissingType */
  void markTransform(boolean[] isTransform) {
  }

  public DisplayImpl getDisplay() {
    return display;
  }

  public MathType getType() {
    return Type;
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
  }

  /** transform data into a Java3D scene graph */
  public boolean doTransform(Group group, Data data, float[] value_array,
                             float[] default_values)
          throws VisADException, RemoteException {
    return false;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Group group) throws VisADException {
  }


  /* helpers for doTransform */

  /** map values into display_values according to ScalarMap-s in reals */
  public static void mapValues(float[][] display_values, double[][] values,
                               ShadowRealType[] reals) throws VisADException {
    int n = values.length;
    if (n != reals.length) {
      throw new DisplayException("ShadowFunctionType.mapValues: " +
                                 "lengths don't match");
    }
    for (int i=0; i<n; i++) {
      Enumeration maps = reals[i].getSelectedMapVector().elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        int value_index = map.getValueIndex();
        // MEM
        display_values[value_index] = map.scaleValues(values[i]);
      }
    }
  }

  /** map values into display_values according to ScalarMap-s in reals */
  public static void mapValues(float[][] display_values, float[][] values,
                               ShadowRealType[] reals) throws VisADException {
    int n = values.length;
    if (n != reals.length) {
      throw new DisplayException("ShadowFunctionType.mapValues: " +
                                 "lengths don't match");
    }
    for (int i=0; i<n; i++) {
      Enumeration maps = reals[i].getSelectedMapVector().elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        int value_index = map.getValueIndex();
        // MEM
        display_values[value_index] = map.scaleValues(values[i]);
      }
    }
  }

  /** construct a default Appearance object according to mode */
  public static Appearance makeDefaultAppearance(GraphicsModeControl mode) {
    Appearance appearance = new Appearance();

    LineAttributes line = new LineAttributes();
    line.setLineWidth(mode.getLineWidth());
    appearance.setLineAttributes(line);

    PointAttributes point = new PointAttributes();
    point.setPointSize(mode.getPointSize());
    appearance.setPointAttributes(point);

    PolygonAttributes polygon = new PolygonAttributes();
    polygon.setCullFace(PolygonAttributes.CULL_NONE);
    appearance.setPolygonAttributes(polygon);

    Material material = new Material();
    material.setLightingEnable(true);
    appearance.setMaterial(material);

    return appearance;
  }

  /** collect and transform spatial DisplayRealType values from display_values;
      if needed, return a spatial Set from spatial_values, with the same topology
      as domain_set (or an appropriate Irregular topology) */
  public static Set assembleSpatial(float[][] spatial_values,
                float[][] display_values, int valueArrayLength,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                Set domain_set, boolean allSpatial, boolean contour_or_flow,
                boolean pointMode, int[] spatialDimensions)
         throws VisADException, RemoteException {
    Set spatial_set = null;
    DisplayTupleType spatialTuple = null;
    // number of spatial samples, default is 1
    int len = 1;
    // number of non-inherited spatial dimensions
    int spatialDimension = 0;
    // temporary holder non-inherited for tuple_index values
    int[] tuple_indices = new int[3];
    spatialDimensions[0] = 0;
    spatialDimensions[1] = 0;

    for (int i=0; i<valueArrayLength; i++) {
      if (display_values[i] != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        DisplayTupleType tuple = real.getTuple();
        if (tuple != null &&
            (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
             (tuple.getCoordinateSystem() != null &&
              tuple.getCoordinateSystem().getReference().equals(
              Display.DisplaySpatialCartesianTuple)))) {
          if (spatialTuple != null && !spatialTuple.equals(tuple)) {
            throw new DisplayException("ShadowFunctionType.assembleSpatial: " +
                                       "multiple spatial display tuples");
          }
          spatialTuple = tuple;
          int tuple_index = real.getTupleIndex();
          spatial_values[tuple_index] = display_values[i];
          len = Math.max(len, display_values[i].length);
          spatialDimensions[0]++;
          if (inherited_values[i] == 0) {
            // don't count inherited spatial dimensions
            tuple_indices[spatialDimension] = tuple_index;
            spatialDimension++;
          }
        }
      } // end if (display_values[i] != null)
    } // end for (int i=0; i<valueArrayLength; i++)
    if (spatialDimension == 0) {
      // len = 1 in this case
      spatialTuple = Display.DisplaySpatialCartesianTuple;
      spatialDimensions[1] = 0;
    }
    else if (!allSpatial) {
      spatialDimensions[1] = spatialDimension;
      if (contour_or_flow || spatialDimensions[0] < 3) {
        // cannot inherit Set topology from Field domain, so
        // construct IrregularSet topology of appropriate dimension
        RealType[] reals = new RealType[spatialDimension];
        float[][] samples = new float[spatialDimension][];
        for (int i=0; i<spatialDimension; i++) {
          reals[i] = RealType.Generic;
          samples[i] = spatial_values[tuple_indices[i]];
        }
        RealTupleType tuple_type = new RealTupleType(reals);
        System.out.println("make IrregularSet, dimension = " + spatialDimension +
                           " # samples = " + len);
        // MEM
        switch (spatialDimension) {
          case 1:
            domain_set =
              new Irregular1DSet(tuple_type, samples, null, null, null, false);
            break;
          case 2:
            domain_set =
              new Irregular2DSet(tuple_type, samples, null, null, null, false);
            break;
          case 3:
            domain_set =
              new Irregular3DSet(tuple_type, samples, null, null, null, false);
            break;
        }
        System.out.println("IrregularSet done");
      } // end if (contour_or_flow)
    }
    else {
      spatialDimensions[1] = domain_set.getManifoldDimension();
    }
    //
    // need a spatial Set for contour, flow
    // or spatial ManifoldDimension < 3
    // NOTE - 3-D volume rendering may eventually need a spatial Set
    //
    boolean set_needed = (contour_or_flow || spatialDimensions[1] < 3);

    for (int i=0; i<3; i++) {
      if (spatial_values[i] == null) {
        // fill any null spatial value arrays with default values
        // MEM
        spatial_values[i] = new float[len];
        int default_index = display.getDisplayScalarIndex(
          ((DisplayRealType) spatialTuple.getComponent(i)) );
        float default_value = default_values[default_index];
        for (int j=0; j<len; j++) spatial_values[i][j] = (float) default_value;
      }
      else if (spatial_values[i].length == 1 && len > 1) {
        // expand any solitary spatial value arrays
        float v = spatial_values[i][0];
        // MEM
        spatial_values[i] = new float[len];
        for (int j=0; j<len; j++) spatial_values[i][j] = v;
      }
    } // end (int i=0; i<3; i++)
    if (!spatialTuple.equals(Display.DisplaySpatialCartesianTuple)) {
      // transform tuple_values to DisplaySpatialCartesianTuple
      CoordinateSystem coord = spatialTuple.getCoordinateSystem();
      spatial_values = coord.toReference(spatial_values);
    }
    if (set_needed) {
      if (spatialDimension == 0) {
        double[] values = new double[3];
        for (int i=0; i<3; i++) values[i] = spatial_values[i][0];
        RealTuple tuple =
          new RealTuple(Display.DisplaySpatialCartesianTuple, values);
        return new SingletonSet(tuple);
      }
      else {
        SetType type = new SetType(Display.DisplaySpatialCartesianTuple);
        // MEM
        return domain_set.makeSpatial(type, spatial_values);
      }
    }
    else {
      return null;
    }
  }

  public static VisADGeometryArray makePointGeometry(float[][] spatial_values,
                float[][] color_values) throws VisADException {
    if (spatial_values == null) {
      throw new DisplayException("ShadowType.makePointGeometry: bad " +
                                 "spatial_values");
    }
    VisADPointArray array = new VisADPointArray();
    // set coordinates and colors
    // MEM
    SampledSet.setGeometryArray(array, spatial_values, 3, color_values);
    return array;
  }

  /** composite and transform color and Alpha DisplayRealType values
      from display_values, and return as (Red, Green, Blue, Alpha) */
  public static float[][] assembleColor(float[][] display_values,
                int valueArrayLength, int[] valueToScalar,
                DisplayImpl display, float[] default_values)
         throws VisADException, RemoteException {
    float[][] rgba_values = new float[4][];
    float[] rgba_value_counts = {0.0f, 0.0f, 0.0f, 0.0f};
    float[] rgba_singles = new float[4];
    float[] rgba_single_counts = {0.0f, 0.0f, 0.0f, 0.0f};
    float[][] tuple_values = new float[3][];
    float[] tuple_value_counts = {0.0f, 0.0f, 0.0f};
    float[] tuple_singles = new float[3];
    float[] tuple_single_counts = {0.0f, 0.0f, 0.0f};

    // mark array to kkep track of which valueIndices have
    // contributed to display color_tuples
    boolean[] mark = new boolean[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) mark[i] = false;

    // loop to assemble values for each different
    // display color_tuple
    while (true) {
      DisplayTupleType color_tuple = null;
      for (int i=0; i<valueArrayLength; i++) {
        float[] values = display_values[i];
        if (values != null && !mark[i]) {
          int len = values.length;
          int displayScalarIndex = valueToScalar[i];
          DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
          DisplayTupleType tuple = real.getTuple();
          // check whether this real is part of a display color tuple
          if (tuple != null &&
              (tuple.equals(Display.DisplayRGBTuple) ||
               (tuple.getCoordinateSystem() != null &&
                tuple.getCoordinateSystem().getReference().equals(
                Display.DisplayRGBTuple)))) {
            if (color_tuple == null || color_tuple.equals(tuple)) {
              if (color_tuple == null) {
                // start a new color_tuple
                color_tuple = tuple;
                for (int j=0; j<3; j++) {
                  tuple_singles[j] = 0.0f;
                  tuple_single_counts[j] = 0.0f;
                  tuple_values[j] = null;
                  tuple_value_counts[j] = 0.0f;
                }
              }
              int index = real.getTupleIndex();
              if (len == 1) {
                tuple_singles[index] += values[0];
                tuple_single_counts[index]++;
              }
              else { // (len != 1)
                singleComposite(index, tuple_values, tuple_value_counts, values);
              }
              mark[i] = true;
            } // end if (color_tuple == null || color_tuple.equals(tuple))
          } // end if component of a color tuple
        } // end if (values != null && !mark[i])
      } // end for (int i=0; i<valueArrayLength; i++)
      if (color_tuple != null) {
        colorSum(3, tuple_values, tuple_value_counts, tuple_singles,
                 tuple_single_counts, display, color_tuple, default_values);
        if (!color_tuple.equals(Display.DisplayRGBTuple)) {
          // equalize all rgba_values[index] to same length
          // and fill with default values
          equalizeAndDefault(tuple_values, display, color_tuple, default_values);
          // transform tuple_values to DisplayRGBTuple
          CoordinateSystem coord = color_tuple.getCoordinateSystem();
          tuple_values = coord.toReference(tuple_values);
        }
        colorComposite(rgba_values, rgba_value_counts, tuple_values);
      }
      else { // if (color_tuple == null)
        // no new color_tuple found on this loop iteration
        break;
      }
    } // end while (true)

    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();
    for (int i=0; i<valueArrayLength; i++) {
      float[] values = display_values[i];
      if (values != null && !mark[i]) {
        int len = values.length;
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real =
          display.getDisplayScalar(displayScalarIndex);
        if (real.equals(Display.RGB) ||
            real.equals(Display.HSV) ||
            real.equals(Display.CMY)) {
          ColorControl control = (ColorControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
          float[][] color_values = control.lookupValues(values);
          if (real.equals(Display.HSV)) {
            // transform color_values to DisplayRGBTuple
            color_values =
              Display.DisplayHSVCoordSys.toReference(color_values);
          }
          else if (real.equals(Display.CMY)) {
            // transform color_values to DisplayRGBTuple
            color_values =
              Display.DisplayCMYCoordSys.toReference(color_values);
          }
          else if (real.equals(Display.RGB)) {
            // do nothing
          }
          else {
            throw new DisplayException("ShadowType.assembleColor: " +
                                  "unrecognized color CoordinateSsystem");
          }
          if (len == 1) {
            for (int index = 0; index<3; index++) {
              rgba_singles[index] += color_values[index][0];
              rgba_single_counts[index]++;
            }
          }
          else { // (len != 1)
            colorComposite(rgba_values, rgba_value_counts, color_values);
          }
        } // end if (real.equals(Display.RGB) || HSV || CMY)
        if (real.equals(Display.Alpha)) {
          if (len == 1) {
            rgba_singles[3] += values[0];
            rgba_single_counts[3]++;
          }
          else {
            singleComposite(3, rgba_values, rgba_value_counts, values);
          }
        } // end if (real.equals(Display.Alpha))
        // no need for 'mark[i] = true;' in this loop
      } // end if (values != null && !mark[i])
    } // end for (int i=0; i<valueArrayLength; i++)
    if (rgba_values[0] == null && rgba_values[1] == null &&
        rgba_values[2] == null && rgba_values[3] == null) {
      // no long color vectors, so try singles, then defaults
      for (int index=0; index<4; index++) {
        rgba_values[index] = new float[1];
        if (rgba_single_counts[index] > 0) {
          rgba_values[index][0] =
            rgba_singles[index] / rgba_single_counts[index];
        }
        else {
          // nothing mapped to this color component, so use default
          int default_index =
            index == 0 ? display.getDisplayScalarIndex(Display.Red) :
            (index == 1 ? display.getDisplayScalarIndex(Display.Green) :
             (index == 2 ? display.getDisplayScalarIndex(Display.Blue) :
              display.getDisplayScalarIndex(Display.Alpha) ) );
          rgba_values[index][0] = default_values[default_index];
        }
      }
    }
    else {
      colorSum(4, rgba_values, rgba_value_counts, rgba_singles,
               rgba_single_counts, display, Display.DisplayRGBTuple,
               default_values);
      // equalize all rgba_values[index] to same length
      // and fill with default values
      equalizeAndDefault(rgba_values, display, Display.DisplayRGBTuple,
                         default_values);
    }
    return rgba_values;
  }

  static void colorSum(int nindex, float[][] tuple_values,
                       float[] tuple_value_counts, float[] tuple_singles,
                       float[] tuple_single_counts, DisplayImpl display,
                       DisplayTupleType tuple, float[] default_values)
         throws VisADException {
    for (int index=nindex-1; index>=0; index--) {
      if (tuple_values[index] == null) {
        if (tuple_single_counts[index] > 0) {
          tuple_values[index] = new float[1];
          tuple_values[index][0] = tuple_singles[index];
          tuple_value_counts[index] = tuple_single_counts[index];
        }
      }
      else { // (tuple_values[index] != null)
        float inv_count =
          1.0f / (tuple_value_counts[index] + tuple_single_counts[index]);
        for (int j=0; j<tuple_values[index].length; j++) {
          tuple_values[index][j] =
            inv_count * (tuple_values[index][j] + tuple_singles[index]);
        }
      }
    } // end for (int index=0; index<nindex; index++)
  }

  /** equalize lengths and fill with default values */
  static void equalizeAndDefault(float[][] tuple_values, DisplayImpl display,
                                 DisplayTupleType tuple, float[] default_values)
         throws VisADException {
    int nindex = tuple_values.length;
    // fill any empty tuple_values[index] with default values
    for (int index=0; index<nindex; index++) {
      if (tuple_values[index] == null) {
        tuple_values[index] = new float[1];
        int default_index = (index < 3) ?
                            display.getDisplayScalarIndex(
                              ((DisplayRealType) tuple.getComponent(index)) ) :
                            display.getDisplayScalarIndex(Display.Alpha);
        tuple_values[index][0] = default_values[default_index];
System.out.println("default color " + index + " is " + default_values[default_index]);
      }
    }
    // compute maximum length of tuple_values[index]
    int len = 1;
    for (int index=0; index<nindex; index++) {
      len = Math.max(len, tuple_values[index].length);
    }
    // entend any tuple_values[index], except Alpha, to maximum length
    for (int index=0; index<3; index++) {
      int t_len = tuple_values[index].length;
      if (len > t_len) {
        if (t_len != 1) {
          throw new DisplayException("ShadowType.equalizeAndDefault: bad length");
        }
        float[] t = new float[len];
        float v = tuple_values[index][0];
        for (int i=0; i<len; i++) t[i] = v;
        tuple_values[index] = t;
      }
    }
  }

  /** composite tuple_values into rgba_values and
      rgba_value_counts, for index = 0, 1, 2 */
  static void colorComposite(float[][] rgba_values, float[] rgba_value_counts,
                             float[][] tuple_values) throws VisADException {
    for (int index = 0; index<3; index++) {
      singleComposite(index, rgba_values, rgba_value_counts,
                      tuple_values[index]);
      // FREE
      tuple_values[index] = null;
    }
  }

  /** composite values into rgba_values[index] and
      rgba_value_counts[index] */
  static void singleComposite(int index, float[][] rgba_values,
                              float[] rgba_value_counts,
                              float[] values) throws VisADException {
    if (values == null) return;
    if (rgba_values[index] == null) {
      rgba_values[index] = values;
      rgba_value_counts[index] = 1.0f;
    }
    else {
      rgba_value_counts[index]++;
      int rgba_len = rgba_values[index].length;
      int values_len = values.length;
      if (rgba_len == values_len) {
        for (int j=0; j<rgba_len; j++) {
          rgba_values[index][j] += values[j];
        }
      }
      else if (values_len == 1) {
        for (int j=0; j<rgba_len; j++) {
          rgba_values[index][j] += values[0];
        }
      }
      else if (rgba_len == 1) {
        for (int j=0; j<rgba_len; j++) {
          values[j] += rgba_values[index][0];
        }
        rgba_values[index] = values;
      }
      else {
        throw new DisplayException("ShadowType.singleComposite: " +
                                   "bad lengths");
      }
    }
  }

  /** return a composite of SelectRange DisplayRealType values from
      display_values, as 0.0 for select and Double.Nan for no select
      (these values can be added to other DisplayRealType values) */
  public static float[] assembleSelect(float[][] display_values, int domain_length,
                                        int valueArrayLength, int[] valueToScalar,
                                        DisplayImpl display) throws VisADException {
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();
    float[] range_select = null;
    boolean anySelect = false;
    for (int i=0; i<valueArrayLength; i++) {
      float[] values = display_values[i];
      if (values != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        if (real.equals(Display.SelectRange)) {
          if (range_select == null) {
            // MEM
            range_select = new float[domain_length];
            for (int j=0; j<domain_length; j++) range_select[j] = 0.0f;
          }
          RangeControl control = (RangeControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
          float[] range = control.getRange();
          for (int j=0; j<values.length; j++) {
            if (values[j] < range[0] || range[1] < values[j]) {
              range_select[j] = Float.NaN;
              anySelect = true;
            }
          }
        }
      }
    }
    if (range_select != null && !anySelect) range_select = null;
    return range_select;
  }

  public String toString() {
    return " LevelOfDifficulty = " + LevelOfDifficulty +
           " isDirectManipulation = " + isDirectManipulation;
  }

}

