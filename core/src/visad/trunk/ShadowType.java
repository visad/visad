
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
  static final int D1 = 1; // allSpatial +
                           // (IsoContour, Flow, Color, Alpha, Unmapped)*
  static final int D2 = 2; // (Spatial, Color, Alpha, Unmapped)*
  static final int D3 = 3; // (Color, Alpha, Unmapped)*
  static final int Dbad = 4;
  /** possible values for Rtype */
  static final int R0 = 0; // (Unmapped)*
  static final int R1 = 1; // (Color, Alpha, Unmapped)*
  static final int R2 = 2; // (Spatial, Color, Alpha, Unmapped)*
  static final int R3 = 3; // (IsoContour, Flow, Color, Alpha, Unmapped)*
  static final int R4 = 4; // (Spatial, IsoContour, Flow, Color, Alpha, Unmapped)*
  static final int Rbad = 5;

  /** spatial DisplayTupleType at terminal nodes */
  DisplayTupleType spatialTuple = null;
  /** number of spatial DisplayRealType components at terminal nodes */
  int spatialDimension;

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
          real.equals(Display.HSB) ||
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
      (Spatial, IsoContour, Flow, Color, Alpha, Unmapped) */
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
          real.equals(Display.HSB) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.IsoContour)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in
      (IsoContour, Flow, Color, Alpha, Unmapped) */
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
          real.equals(Display.HSB) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.IsoContour)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Color, Alpha, Unmapped) */
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
          real.equals(Display.HSB) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Spatial, Color, Alpha, Unmapped) */
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
          real.equals(Display.HSB) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha)) continue;
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
  boolean doTransform(Group group, Data data, double[] value_array)
          throws VisADException, RemoteException {
    return false;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Group group) throws VisADException {
  }

  public String toString() {
    return " LevelOfDifficulty = " + LevelOfDifficulty +
           " isDirectManipulation = " + isDirectManipulation;
  }

}

