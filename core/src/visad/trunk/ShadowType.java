
//
// ShadowType.java
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   The ShadowType hierarchy shadows the MathType hierarchy,
   within a DataDisplayLink.<P>
*/
public abstract class ShadowType extends Object
       implements java.io.Serializable {

  /** possible values for LevelOfDifficulty */
  public static final int NOTHING_MAPPED = 6;
  public static final int SIMPLE_TUPLE = 5;
  public static final int SIMPLE_ANIMATE_FIELD = 4;
  public static final int SIMPLE_FIELD = 3;
  public static final int NESTED = 2;
  public static final int LEGAL = 1;

  /** basic information about this ShadowType */
  MathType Type; // MathType being shadowed
  transient DataDisplayLink Link;
  transient DisplayImpl display;
  transient private Data data; // from Link.getData()
  private ShadowType Parent;

  /** information calculated by constructors */
  /** count of occurences of DisplayRealType-s
      ShadowScalarType: set for mappings to DisplayRealType-s
      ShadowTupleType (incl ShadowRealTupleType): set to
        sum for ShadowScalarType & ShadowRealTupleType components
      ShadowRealTupleType: add contribution from Reference */
  int[] DisplayIndices;
  /** ValueIndices is like DisplayIndices, but distinguishes
      different ScalarMap-s of non-Single DisplayRealTypes */
  int[] ValueIndices;
  /** MultipleSpatialDisplayScalar is true if any RealType component is
      mapped to multiple spatial DisplayRealType-s */
  boolean MultipleSpatialDisplayScalar;
  /** MultipleDisplayScalar is true if any RealType component is mapped
      to multiple DisplayRealType-s, or if any RealTupleType component
      and its Reference are both mapped */
  boolean MultipleDisplayScalar;
  /** MappedDisplayScalar is true if any RealType component is mapped
      to any DisplayRealType-s, including via a RealTupleType.Reference */
  boolean MappedDisplayScalar;

  /** information calculated by checkIndices & testIndices */
  boolean isTerminal;
  int LevelOfDifficulty;
  boolean isTextureMap;

  /** Dtype and Rtype used only with ShadowSetType and
      Flat ShadowFunctionType */
  int Dtype; // Domain Type: D0, D1, D2, D3, D4 or Dbad
  int Rtype; // Range Type: R0, R1, R2, R3, R4 or Rbad
  /** possible values for Dtype */
  static final int D0 = 0; // (Unmapped)*
  static final int D1 = 1; // allSpatial + (SpatialOffset, IsoContour, Flow,
                           // Text, Color, Alpha, Range, Unmapped)*
  static final int D2 = 2; // (SpatialOffset, Spatial, Color, Alpha,
                           //  Range, Unmapped)*
  static final int D3 = 3; // (Color, Alpha, Range, Unmapped)*
  static final int D4 = 4; // (Animation, Value)*
  static final int Dbad = 5;
  /** possible values for Rtype */
  static final int R0 = 0; // (Unmapped)*
  static final int R1 = 1; // (Color, Alpha, Range, Unmapped)*
  static final int R2 = 2; // (Spatial, SpatialOffset,  Color, Alpha,
                           //  Range, Unmapped)*
  static final int R3 = 3; // (IsoContour, Flow, Text, Color, Alpha, Range,
                           //  Unmapped)*
  static final int R4 = 4; // (Spatial, SpatialOffset, IsoContour, Flow,
                           //  Text, Color, Alpha, Range, Unmapped)*
  static final int Rbad = 5;

  /** spatial DisplayTupleType at terminal nodes */
  DisplayTupleType spatialTuple = null;
  /** number of spatial DisplayRealType components at terminal nodes */
  int spatialDimension;
  /** flags for any IsoContour or Flow at terminal nodes */
  boolean anyContour;
  boolean anyFlow;
  boolean anyShape;
  boolean anyText;


  /** used by getComponents to record RealTupleTypes
      with coordinate transforms */
  int[] refToComponent;
  ShadowRealTupleType[] componentWithRef;
  int[] componentIndex;

  public ShadowType(MathType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    Type = type;
    Link = link;
    display = link.getDisplay();
    Parent = parent;
    data = link.getData();
    DisplayIndices = zeroIndices(display.getDisplayScalarCount());
    ValueIndices = zeroIndices(display.getValueArrayLength());
    isTerminal = false;
    isTextureMap = false;
    LevelOfDifficulty = NOTHING_MAPPED;
    MultipleSpatialDisplayScalar = false;
    MultipleDisplayScalar = false;
    MappedDisplayScalar = false;
  }

  public int getLevelOfDifficulty() {
    return LevelOfDifficulty;
  }

  public boolean getIsTerminal() {
    return isTerminal;
  }

  public boolean getIsTextureMap() {
    return isTextureMap;
  }

  public int[] getRefToComponent() {
    return refToComponent;
  }

  public ShadowRealTupleType[] getComponentWithRef() {
    return componentWithRef;
  }

  public int[] getComponentIndex() {
    return componentIndex;
  }

  public ShadowRealType[] getComponents(ShadowType type, boolean doRef)
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
      }
    }
    else {
      reals = null;
    }
    return reals;
  }

  public Data getData() {
    return data;
  }

  public ShadowType getAdaptedShadowType() {
    return this;
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

  public boolean getAnyContour() {
    return anyContour;
  }
 
  public boolean getAnyFlow() {
    return anyFlow;
  }
 
  public boolean getAnyShape() {
    return anyShape;
  }

  public boolean getAnyText() {
    return anyText;
  }

  /** test for display_indices in
      (Spatial, SpatialOffset, Color, Alpha, Animation, Range, Value,
       Flow, Text, Unmapped) */
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
      // SpatialOffset
      if (Display.DisplaySpatialOffsetTuple.equals(tuple)) continue;
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (Display.DisplayFlow1Tuple.equals(tuple)) continue;  // Flow1
      if (Display.DisplayFlow2Tuple.equals(tuple)) continue;  // Flow2
      if (real.equals(Display.RGB) ||
          real.equals(Display.RGBA) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.Animation) ||
          real.equals(Display.SelectValue) ||
          real.equals(Display.SelectRange) ||
          real.equals(Display.Text)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in
      (Spatial, SpatialOffset, IsoContour, Color, Alpha, Flow,
       Text, Range, Unmapped) */
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
      // SpatialOffset
      if (Display.DisplaySpatialOffsetTuple.equals(tuple)) continue;
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (Display.DisplayFlow1Tuple.equals(tuple)) continue;  // Flow1
      if (Display.DisplayFlow2Tuple.equals(tuple)) continue;  // Flow2
      if (real.equals(Display.RGB) ||
          real.equals(Display.RGBA) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.SelectRange) ||
          real.equals(Display.Text) ||
          real.equals(Display.IsoContour)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in
      (IsoContour, Color, Alpha, Flow, Text, Range, Unmapped) */
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
      if (Display.DisplayFlow1Tuple.equals(tuple)) continue;  // Flow1
      if (Display.DisplayFlow2Tuple.equals(tuple)) continue;  // Flow2
      if (real.equals(Display.RGB) ||
          real.equals(Display.RGBA) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.SelectRange) ||
          real.equals(Display.Text) ||
          real.equals(Display.IsoContour)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Color, Alpha, Flow, Range, Unmapped) */
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
      if (Display.DisplayFlow1Tuple.equals(tuple)) continue;  // Flow1
      if (Display.DisplayFlow2Tuple.equals(tuple)) continue;  // Flow2
      if (real.equals(Display.RGB) ||
          real.equals(Display.RGBA) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.Text) ||
          real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in
      (Spatial, SpatialOffset, Color, Alpha, Flow, Range, Unmapped) */
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
      // SpatialOffset
      if (Display.DisplaySpatialOffsetTuple.equals(tuple)) continue;
      if (tuple != null &&
          (tuple.equals(Display.DisplayRGBTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplayRGBTuple)))) continue;  // Color
      if (Display.DisplayFlow1Tuple.equals(tuple)) continue;  // Flow1
      if (Display.DisplayFlow2Tuple.equals(tuple)) continue;  // Flow2
      if (real.equals(Display.RGB) ||
          real.equals(Display.RGBA) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha) ||
          real.equals(Display.Text) ||
          real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for any Animation or Value in display_indices */
  int checkAnimationOrValue(int[] display_indices)
      throws RemoteException {
    int count = 0;
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      if (real.equals(Display.Animation) ||
          real.equals(Display.SelectValue)) count++;
    }
    return count;
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
      if (Display.DisplayFlow1Tuple.equals(tuple)) return true;  // Flow1
      if (Display.DisplayFlow2Tuple.equals(tuple)) return true;  // Flow2
    }
    return false;
  }

  /** test for any Shape in display_indices */
  boolean checkShape(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      if (real.equals(Display.Shape)) return true;
    }
    return false;
  }

  /** test for any Text in display_indices */
  boolean checkText(int[] display_indices) throws RemoteException {
    for (int i=0; i<display_indices.length; i++) {
/*
System.out.println("checkText: display_indices[" + i + "] = " +
                   display_indices[i] + " real = " +
                   ((DisplayRealType) display.getDisplayScalar(i)).getName());
*/
      if (display_indices[i] == 0) continue;
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
/*
System.out.println("checkText: real = " + real.getName());
*/
      if (real.equals(Display.Text)) return true;
    }
    return false;
  }

  /** test for display_indices in (Color, Unmapped) */
  boolean checkColor(int[] display_indices) throws RemoteException {
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
          real.equals(Display.RGBA) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      return false;
    }
    return true;
  }

  /** test for display_indices in (Color, Alpha, Unmapped) */
  boolean checkColorOrAlpha(int[] display_indices) throws RemoteException {
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
          real.equals(Display.RGBA) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.Alpha)) continue;
      return false;
    }
    return true;
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
      if (indices[i] > 1) {
        ScalarType real = display.getScalar(i);
        throw new BadMappingException("RealType " + real.getName() +
                                      " occurs more than once: " +
                                      "ShadowType.testIndices");
      }
    }

    // test whether any DisplayRealType occurs at least once;
    // test whether any Single DisplayRealType occurs more than once
    for (int i=0; i<display_indices.length; i++) {
      DisplayRealType real = (DisplayRealType) display.getDisplayScalar(i);
      if (display_indices[i] > 0) isTerminal = true;
      if (display_indices[i] > 1 && real.isSingle()) {
        throw new BadMappingException("Single " + "DisplayRealType " +
                                      real.getName() +
                                      " occurs more than once: " +
                                      "ShadowType.testIndices");
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
              throw new BadMappingException("DisplayRealType-s occur from " +
                                            "multiple spatial DisplayTupleType-s: " +
                                            "ShadowType.testIndices");
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
/*
System.out.println("testIndices: LevelOfDifficulty = " + LevelOfDifficulty +
                   " isTerminal = " + isTerminal +
                   " Type = " + Type.prettyString());
*/
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
  public int checkIndices(int[] indices, int[] display_indices,
             int[] value_indices, boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    LevelOfDifficulty = testIndices(indices, display_indices, levelOfDifficulty);
    return LevelOfDifficulty;
  }

  public DisplayImpl getDisplay() {
    return display;
  }
 
  public MathType getType() {
    return Type;
  }

  public boolean getMultipleDisplayScalar() {
    return MultipleDisplayScalar;
  }

  public boolean getMultipleSpatialDisplayScalar() {
    return MultipleSpatialDisplayScalar;
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


  /** helpers for doTransform; they are in ShadowType
      because they are independent of graphics library */

  /** map values into display_values according to ScalarMap-s in reals */
  public static void mapValues(float[][] display_values, double[][] values,
                               ShadowRealType[] reals) throws VisADException {
    int n = values.length;
    if (n != reals.length) {
      throw new DisplayException("lengths don't match " +
                                 n + " != " + reals.length + ": " +
                                 "ShadowType.mapValues");
    }
    for (int i=0; i<n; i++) {
      Enumeration maps = reals[i].getSelectedMapVector().elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        int value_index = map.getValueIndex();
/*
double[] range = map.getRange();
System.out.println(map.getScalar() + " -> " + map.getDisplayScalar() + " : " +
                   range[0] + " " + range[1] + "  value_index = " + value_index);
*/
        // MEM
        display_values[value_index] = map.scaleValues(values[i]);
/*
int m = values[i].length;
for (int j=0; j<m; j++) System.out.println("values["+i+"]["+j+"] = " + values[i][j] +
" display_values["+value_index+"]["+j+"] = " + display_values[value_index][j]);
*/
/*
      int total = 0;
      int missing = 0;
      total = display_values[value_index].length;
      for (int j=0; j<display_values[value_index].length; j++) {
        if (display_values[value_index][j] != display_values[value_index][j]) {
          missing++;
        }
      }
      System.out.println("  total = " + total + " missing = " + missing);
*/
      }
    }
  }

  /** map values into display_values according to ScalarMap-s in reals */
  public static void mapValues(float[][] display_values, float[][] values,
                               ShadowRealType[] reals) throws VisADException {
    int n = values.length;
    if (n != reals.length) {
      throw new DisplayException("lengths don't match: ShadowType.mapValues");
    }
    for (int i=0; i<n; i++) {
      Enumeration maps = reals[i].getSelectedMapVector().elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        int value_index = map.getValueIndex();
/*
double[] range = map.getRange();
System.out.println(map.getScalar() + " -> " + map.getDisplayScalar() + " : " +
                   range[0] + " " + range[1] + "  value_index = " + value_index);
*/
        // MEM
        display_values[value_index] = map.scaleValues(values[i]);
/*
int m = values[i].length;
for (int j=0; j<m; j++) System.out.println("values["+i+"]["+j+"] = " + values[i][j] +
" display_values["+value_index+"]["+j+"] = " + display_values[value_index][j]);
*/
/*
      int total = 0;
      int missing = 0;
      total = display_values[value_index].length;
      for (int j=0; j<display_values[value_index].length; j++) {
        if (display_values[value_index][j] != display_values[value_index][j]) missing++;
      }
      System.out.println("  total = " + total + " missing = " + missing);
*/
      }
    }
  }

  public static VisADGeometryArray makePointGeometry(float[][] spatial_values,
                float[][] color_values) throws VisADException {
    if (spatial_values == null) {
      throw new DisplayException("bad spatial_values: " +
                                 "ShadowType.makePointGeometry: bad");
    }
    VisADPointArray array = new VisADPointArray();
 
    // set coordinates and colors
    // MEM
    SampledSet.setGeometryArray(array, spatial_values, 3, color_values);
    return array;
  }

  /** collect and transform Shape DisplayRealType values from display_values;
      offset by spatial_values, colored by color_values and selected by
      range_select */
  public static VisADGeometryArray[] assembleShape(float[][] display_values,
                int valueArrayLength, int[] valueToMap, Vector MapVector,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                float[][] spatial_values, float[][] color_values,
                float[][] range_select)
         throws VisADException, RemoteException {
    float[] values = null;
    ShapeControl control = null;
    for (int i=0; i<valueArrayLength; i++) {
      if (display_values[i] != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);

        if (real.equals(Display.Shape)) {
          values = display_values[i];
          control = (ShapeControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
        }
      }
    }
    if (values == null || control == null) return null;
    int len = values.length;

    // color ??
    int color_length = 0;
    float r = 0.0f;
    float g = 0.0f;
    float b = 0.0f;
    float a = 0.0f;
    if (color_values != null) {
      if (color_values[0].length > len) len = color_values[0].length;
      color_length = color_values.length;
      r = color_values[0][0];
      g = color_values[1][0];
      b = color_values[2][0];
      if (color_length > 3) a = color_values[3][0];
    }

    if (spatial_values[0].length > len) len = spatial_values[0].length;
    if (values.length < len) {
      float[] new_values = new float[len];
      for (int i=0; i<len; i++) new_values[i] = values[0];
      values = new_values;
    }
    VisADGeometryArray[] arrays = control.getShapes(values);
    float x = spatial_values[0][0];
    float y = spatial_values[1][0];
    float z = spatial_values[2][0];
    for (int i=0; i<arrays.length; i++) {
      VisADGeometryArray array = arrays[i];
      if (range_select[0] != null) {
        if (range_select[0].length == 1) {
          if (range_select[0][0] != range_select[0][0]) array = null;
        }
        else {
          if (range_select[0][i] != range_select[0][i]) array = null;
        }
      }
      if (array != null) {
        if (spatial_values[0].length > 1) {
          x = spatial_values[0][i];
          y = spatial_values[1][i];
          z = spatial_values[2][i];
        }
        int npts = array.coordinates.length / 3;
        // offset shape location by spatial values
        for (int j=0; j<array.coordinates.length; j+=3) {
          array.coordinates[j] += x;
          array.coordinates[j+1] += y;
          array.coordinates[j+2] += z;
        }

        // color ??
        if (array.colors == null && color_values != null) {
          array.colors = new float[color_length * npts];
          if (color_values[0].length > 1) {
            r = color_values[0][i];
            g = color_values[1][i];
            b = color_values[2][i];
            if (color_length > 3) a = color_values[3][i];
          }
          for (int j=0; j<array.coordinates.length; j+=color_length) {
            array.colors[j] = r;
            array.colors[j+1] = g;
            array.colors[j+2] = b;
            if (color_length > 3) array.colors[j+3] = a;
          }
        }

      }
    } // end for (int i=0; i<arrays.length; i++)
    return arrays;
  }

  /** collect and transform spatial DisplayRealType values from display_values;
      add spatial offset DisplayRealType values;
      adjust flow1_values and flow2_values for any coordinate transform;
      if needed, return a spatial Set from spatial_values, with the same topology
      as domain_set (or an appropriate Irregular topology);
      domain_set = null and allSpatial = false if not called from
      ShadowFunctionType */
  public static Set assembleSpatial(float[][] spatial_values,
                float[][] display_values, int valueArrayLength,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                Set domain_set, boolean allSpatial, boolean set_for_shape,
                int[] spatialDimensions, float[][] range_select,
                float[][] flow1_values, float[][] flow2_values,
                float[] flowScale, boolean[] swap)
         throws VisADException, RemoteException {
    DisplayTupleType spatial_tuple = null;
    // number of spatial samples, default is 1
    int len = 1;
    // number of non-inherited spatial dimensions
    int spatialDimension = 0;
    // temporary holder for non-inherited tuple_index values
    int[] tuple_indices = new int[3];
    spatialDimensions[0] = 0; // spatialDomainDimension
    spatialDimensions[1] = 0; // spatialManifoldDimension
    // holder for SpatialOffset values
    float[][] offset_values = new float[3][];
    boolean[] offset_copy = {false, false, false};

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
          if (spatial_tuple != null && !spatial_tuple.equals(tuple)) {
            throw new DisplayException("multiple spatial display tuples: " +
                                       "ShadowType.assembleSpatial");
          }
          spatial_tuple = tuple;
          int tuple_index = real.getTupleIndex();
          spatial_values[tuple_index] = display_values[i];
          len = Math.max(len, display_values[i].length);
          spatialDimensions[0]++; // spatialDomainDimension
          if (inherited_values[i] == 0) {
            // don't count inherited spatial dimensions
            tuple_indices[spatialDimension] = tuple_index;
            spatialDimension++; // # non-inherited spatial dimensions
          }
        }
      } // end if (display_values[i] != null)
    } // end for (int i=0; i<valueArrayLength; i++)
    if (spatial_tuple == null) {
      spatial_tuple = Display.DisplaySpatialCartesianTuple;
    }

    if (spatialDimension == 0) {
      // len = 1 in this case
      spatialDimensions[1] = 0; // spatialManifoldDimension
    }
/* WLH 21 Aug 98
    else if (!allSpatial) {
*/
    else if (!allSpatial || domain_set == null) {
      spatialDimensions[1] = spatialDimension; // spatialManifoldDimension
      if (domain_set != null &&
          (set_for_shape || spatialDimensions[0] < 2)) {
        // cannot inherit Set topology from Field domain, so
        // construct IrregularSet topology of appropriate dimension
        RealType[] reals = new RealType[spatialDimension];
        float[][] samples = new float[spatialDimension][];
        for (int i=0; i<spatialDimension; i++) {
          reals[i] = RealType.Generic;
          samples[i] = spatial_values[tuple_indices[i]];
        }
        RealTupleType tuple_type = new RealTupleType(reals);
        // MEM
        switch (spatialDimension) {
          case 1:
            domain_set =
              new Irregular1DSet(tuple_type, samples, null,
                                 null, null, false);
            break;
          case 2:
            domain_set =
              new Irregular2DSet(tuple_type, samples, null,
                                 null, null, null, false);
            break;
          case 3:
            domain_set =
              new Irregular3DSet(tuple_type, samples, null,
                                 null, null, null, false);
            break;
        }
        // System.out.println("IrregularSet done");
      } // end if domain_set != null && (set_for_shape || spatialDimensions[0] < 2)
    }
    else { // spatialDimension > 0 && allSpatial
      // spatialManifoldDimension
      spatialDimensions[1] = domain_set.getManifoldDimension();
    }

    //
    // need a spatial Set for shape (e.g., contour)
    // or spatialManifoldDimension < 3
    // NOTE - 3-D volume rendering may eventually need a spatial Set
    //
    boolean set_needed =
      domain_set != null && (set_for_shape || spatialDimensions[1] < 3);

    boolean[] missing_checked = {false, false, false};
    for (int i=0; i<3; i++) {
      if (spatial_values[i] == null) {
        // fill any null spatial value arrays with default values
        // MEM
        spatial_values[i] = new float[len];
        int default_index = display.getDisplayScalarIndex(
          ((DisplayRealType) spatial_tuple.getComponent(i)) );
        float default_value = default_values[default_index];
        for (int j=0; j<len; j++) {
          spatial_values[i][j] = default_value;
        }
        missing_checked[i] = true;
      }
      else if (spatial_values[i].length == 1) {
        // check solitary spatial value array for missing
        float v = spatial_values[i][0];
        missing_checked[i] = true;
        if (v != v || Float.isInfinite(v)) {
          // missing with length = 1, so nothing to render
          range_select[0] = new float[1];
          range_select[0][0] = Float.NaN;
          return null;
        }
        if (len > 1) {
          // expand solitary spatial value array
          // MEM
          spatial_values[i] = new float[len];
          for (int j=0; j<len; j++) spatial_values[i][j] = v;
        }
      }
    } // end for (int i=0; i<3; i++)

    // calculate if need to swap rows and cols in contour line labels
    swap[0] = false;
    if (allSpatial && spatialDimensions[1] == 2 && len > 1) {
      if (spatial_tuple == Display.DisplaySpatialCartesianTuple) {
        float simax = 0.0f;
        float max = -1.0f;
        int imax = -1;
        for (int i=0; i<3; i++) {
          float sdiff = spatial_values[i][1] - spatial_values[i][0];
          float diff = Math.abs(sdiff);
          if (diff > max) {
            simax = sdiff;
            max = diff;
            imax = i;
          }
        }
        float sjmax = 0.0f;
        max = -1.0f;
        int jmax = -1;
        for (int i=0; i<3; i++) {
          if (i != imax) {
            float sdiff = spatial_values[i][len-1] - spatial_values[i][0];
            float diff = Math.abs(sdiff);
            if (diff > max) {
              sjmax = sdiff;
              max = diff;
              jmax = i;
            }
          }
        } // end for (int i=0; i<3; i++)
        if (imax == 0) {
          swap[0] = true;
          swap[1] = (simax < 0.0f);
          swap[2] = (sjmax < 0.0f);
        }
        else if (imax == 1) {
          swap[1] = (sjmax < 0.0f);
          swap[2] = (simax < 0.0f);
        }
        else { // imax == 2
          if (jmax == 1) {
            swap[0] = true;
            swap[1] = (simax < 0.0f);
            swap[2] = (sjmax < 0.0f);
          }
          else {
            swap[1] = (sjmax < 0.0f);
            swap[2] = (simax < 0.0f);
          }
        }
/* WLH 20 Aug 98
        swap[0] = (imax == 0 || (imax == 2 && jmax == 1));
*/
      } // end if (spatial_tuple == Display.DisplaySpatialCartesianTuple)
    }

    if (!spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
      // transform tuple_values to DisplaySpatialCartesianTuple
      CoordinateSystem coord = spatial_tuple.getCoordinateSystem();

      // adjust Flow values for coordinate transform
      // first equalize lengths of flow*_values and spatial_values
      int[] flen = {0, 0};
      float[][][] ff_values = {flow1_values, flow2_values};
      for (int k=0; k<2; k++) {
        for (int i=0; i<3; i++) {
          if (ff_values[k][i] != null) {
            flen[k] = Math.max(flen[k], ff_values[k][i].length);
          }
        }
      }
      len = Math.max(len, Math.max(flen[0], flen[1]));
      fillOut(spatial_values, len);
      if (flen[0] > 0) fillOut(flow1_values, len);
      if (flen[1] > 0) fillOut(flow2_values, len);

      // compute and transform 'end points' of flow vectors
      float[][][] vector_ends = new float[2][][];
      for (int k=0; k<2; k++) {
        if (flen[k] > 0) {
          vector_ends[k] = new float[3][len];
          for (int i=0; i<3; i++) {
            if (ff_values[k][i] != null) {
              for (int j=0; j<len; j++) {
                vector_ends[k][i][j] =
                  spatial_values[i][j] + flowScale[k] * ff_values[k][i][j];
              }
            }
            else { // (ff_values[k][i] == null)
              for (int j=0; j<len; j++) {
                vector_ends[k][i][j] = spatial_values[i][j];
              }
            }
          } // end for (int i=0; i<3; i++)
          vector_ends[k] = coord.toReference(vector_ends[k]);
        } // end if (flen[k] > 0)
      } // end for (int k=0; k<2; k++)
/*
System.out.println("\nbefore tranform spatial_values[" + spatial_values.length +
                   "][" + spatial_values[0].length + "]");
for (int p=0; p<spatial_values[0].length; p++) {
  System.out.println(" " + p + " " + spatial_values[0][p] + " " +
                     spatial_values[1][p] + " " + spatial_values[2][p]);
}
*/
      // transform spatial_values
      float[][] new_spatial_values = coord.toReference(spatial_values);
      for (int i=0; i<3; i++) spatial_values[i] = new_spatial_values[i];
/*
System.out.println("\nafter tranform spatial_values[" + spatial_values.length +
                   "][" + spatial_values[0].length + "]");
for (int p=0; p<spatial_values[0].length; p++) {
  System.out.println(" " + p + " " + spatial_values[0][p] + " " +
                     spatial_values[1][p] + " " + spatial_values[2][p]);
}
System.out.println(" ");
*/
      // subtract transformed spatial_values from transformed flow vectors
      for (int k=0; k<2; k++) {
        if (flen[k] > 0) {
          for (int i=0; i<3; i++) {
            for (int j=0; j<len; j++) {
              vector_ends[k][i][j] =
                (vector_ends[k][i][j] - spatial_values[i][j]) / flowScale[k];
            }
            ff_values[k][i] = vector_ends[k][i];
          }
        }
      }
      missing_checked = new boolean[] {false, false, false};
    } // end if (!spatial_tuple.equals(Display.DisplaySpatialCartesianTuple))

    // assemble SpatialOffsets
    int offset_len = len;
    for (int i=0; i<valueArrayLength; i++) {
      if (display_values[i] != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        DisplayTupleType tuple = real.getTuple();
        if (Display.DisplaySpatialOffsetTuple.equals(tuple)) {
          int tuple_index = real.getTupleIndex();
          if (offset_values[tuple_index] == null) {
            offset_values[tuple_index] = display_values[i];
          }
          else {
            int leno = offset_values[tuple_index].length;
            int lend = display_values[i].length;
            if (leno > lend) {
              // assume lend == 1
              float[] off;
              if (offset_copy[tuple_index]) {
                off = offset_values[tuple_index];
              }
              else {
                off = new float[leno];
                offset_copy[tuple_index] = true;
              }
              for (int j=0; j<leno; j++) {
                off[j] = offset_values[tuple_index][j] + display_values[i][0];
              }
              offset_values[tuple_index] = off;
              off = null;
            }
            else if (leno < lend) {
              // assume leno == 1
              float[] off = new float[lend];
              for (int j=0; j<lend; j++) {
                off[j] = offset_values[tuple_index][0] + display_values[i][j];
              }
              offset_values[tuple_index] = off;
              off = null;
              offset_copy[tuple_index] = true;
            }
            else {
              float[] off;
              if (offset_copy[tuple_index]) {
                off = offset_values[tuple_index];
              }
              else {
                off = new float[leno];
                offset_copy[tuple_index] = true;
              }
              for (int j=0; j<leno; j++) {
                off[j] = offset_values[tuple_index][j] + display_values[i][j];
              }
              offset_values[tuple_index] = off;
              off = null;
            }
          }
          offset_len = Math.max(offset_len, offset_values[tuple_index].length);
        } // end if (Display.DisplaySpatialOffsetTuple.equals(tuple))
      } // end if (display_values[i] != null)
    } // end for (int i=0; i<valueArrayLength; i++)

    boolean[] offset_missing_checked = {false, false, false};
    for (int i=0; i<3; i++) {
      if (offset_values[i] == null) {
        offset_missing_checked[i] = true;
      }
      else if (offset_values[i].length == 1) {
        offset_missing_checked[i] = true;
        if (offset_values[i][0] != offset_values[i][0] ||
            Float.isInfinite(offset_values[i][0])) {
          // missing with length = 1, so nothing to render
          range_select[0] = new float[1];
          range_select[0][0] = Float.NaN;
          return null;
        }
      }
    }

    // spatial offsets longer than spatial, so increase len
    if (offset_len > len) {
      // assume len == 1
      for (int i=0; i<3; i++) {
        float[] s = new float[offset_len];
        for (int k=0; k<offset_len; k++) s[k] = spatial_values[i][0];
        spatial_values[i] = s;
        s = null;
      }
      len = offset_len;
    }

    // add any spatial offsets to spatial values
    for (int i=0; i<3; i++) {
      if (offset_values[i] != null) {
        int leno = offset_values[i].length;
        if (leno < len) {
          // assume leno == 1
          for (int k=0; k<offset_len; k++) {
            spatial_values[i][k] += offset_values[i][0];
          }
        }
        else {
          // assume leno == len
          for (int k=0; k<offset_len; k++) {
            spatial_values[i][k] += offset_values[i][k];
          }
        }
        offset_values[i] = null;
        missing_checked[i] = missing_checked[i] && offset_missing_checked[i];
      }

      if (!missing_checked[i]) {
        for (int j=0; j<len; j++) {
          if (spatial_values[i][j] != spatial_values[i][j] ||
              Float.isInfinite(spatial_values[i][j])) {
            if (range_select[0] == null) {
              range_select[0] = new float[len];
              for (int k=0; k<len; k++) range_select[0][k] = 0.0f;
            }
            else if (range_select[0].length < len) {
              // assume range_select[0].length == 1
              float[] r = new float[len];
              for (int k=0; k<len; k++) r[k] = range_select[0][0];
              range_select[0] = r;
            }
            range_select[0][j] = Float.NaN;
            spatial_values[i][j] = 0.0f;
          }
        }
      }
    } // end for (int i=0; i<3; i++)

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

  private static void fillOut(float[][] values, int flen) {
    for (int i=0; i<values.length; i++) {
      if (values[i] != null) {
        int len = values[i].length;
        if (len < flen) {
          // assume len == 1
          float[] s = new float[flen];
          float v = values[i][0];
          for (int k=0; k<flen; k++) s[k] = v;
          values[i] = s;
        }
      }
    }
  }

  /** assemble Flow components;
      Flow components are 'single', so no compositing is required */
  public static void assembleFlow(float[][] flow1_values,
                float[][] flow2_values, float[] flowScale,
                float[][] display_values, int valueArrayLength,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, float[][] range_select)
         throws VisADException, RemoteException {

    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

    int[] flen = {0, 0};
    float[][][] ff_values = {flow1_values, flow2_values};
    DisplayTupleType[] flow_tuple =
      {Display.DisplayFlow1Tuple, Display.DisplayFlow2Tuple};

    for (int i=0; i<valueArrayLength; i++) {
      if (display_values[i] != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        DisplayTupleType tuple = real.getTuple();
        for (int k=0; k<2; k++) {
          if (flow_tuple[k].equals(tuple)) {

            FlowControl control = (FlowControl)
              ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
            flowScale[k] = control.getFlowScale();
            int flow_index = real.getTupleIndex();
            ff_values[k][flow_index] = display_values[i];
            flen[k] = Math.max(flen[k], display_values[i].length);
          }
        }
      }
    }

    //
    // TO_DO
    // this should all happen in flow rendering method
    //
    for (int k=0; k<2; k++) {
      boolean[] missing_checked = {false, false, false};
      if (flen[k] > 0) {
        for (int i=0; i<3; i++) {
          if (ff_values[k][i] == null) {
            // fill any null flow value arrays with default values
            // MEM
            ff_values[k][i] = new float[flen[k]];
            int default_index = display.getDisplayScalarIndex(
              ((DisplayRealType) flow_tuple[k].getComponent(i)) );
            float default_value = default_values[default_index];
            for (int j=0; j<flen[k]; j++) {
              ff_values[k][i][j] = default_value;
            }
            missing_checked[i] = true;
          }
          else if (ff_values[k][i].length == 1) {
            // check solitary spatial value array for missing
            float v = ff_values[k][i][0];
            missing_checked[i] = true;
            if (v != v) {
              // missing with length = 1, so nothing to render
              range_select[0] = new float[1];
              range_select[0][0] = Float.NaN;
              return;
            }
            if (flen[k] > 1) {
              // expand solitary flow value array
              ff_values[k][i] = new float[flen[k]];
              for (int j=0; j<flen[k]; j++) {
                ff_values[k][i][j] = v;
              }
            }
          } // end if (ff_values[k][i].length == 1)
          if (!missing_checked[i]) {
            for (int j=0; j<flen[k]; j++) {
              if (ff_values[k][i][j] != ff_values[k][i][j]) {
                if (range_select[0] == null) {
                  range_select[0] = new float[flen[k]];
                  for (int m=0; m<flen[k]; m++) range_select[0][m] = 0.0f;
                }
                else if (range_select[0].length < flen[k]) {
                  // assume range_select[0].length == 1
                  float[] r = new float[flen[k]];
                  for (int m=0; m<flen[k]; m++) r[m] = range_select[0][0];
                  range_select[0] = r;
                }
                range_select[0][j] = Float.NaN;
                ff_values[k][i][j] = 0.0f;
              }
            } // end for (int j=0; j<flen[k]; j++)
          } // end if (!missing_checked[i])
        } // end for (int i=0; i<3; i++)
      } // end if (flen[k] > 0)
    } // end for (int k=0; k<2; k++)
    // end of 'this should all happen in flow rendering method'
  }

  private static final float BACK_SCALE = -0.15f;
  private static final float PERP_SCALE = 0.15f;

  public static VisADGeometryArray makeFlow(float[][] flow_values,
                float flowScale, float[][] spatial_values,
                float[][] color_values, float[][] range_select)
         throws VisADException {
    if (flow_values[0] == null) return null;
    VisADLineArray array = new VisADLineArray();
 
    int len = spatial_values[0].length;
    int flen = flow_values[0].length;
    int rlen = 0; // number of non-missing values
    if (range_select[0] == null) {
      rlen = len;
    }
    else {
      for (int j=0; j<range_select[0].length; j++) {
        if (range_select[0][j] == range_select[0][j]) rlen++;
      }
    }
    if (rlen == 0) return null;

    array.vertexCount = 4 * rlen;

    float[] coordinates = new float[12 * rlen];
    int m = 0;
    // flow vector
    float f0 = 0.0f, f1 = 0.0f, f2 = 0.0f;
    // arrow head vector
    float a0 = 0.0f, a1 = 0.0f, a2 = 0.0f;
    for (int j=0; j<len; j++) {
      if (range_select[0] == null ||
          range_select[0][j] == range_select[0][j]) {
        if (flen == 1) {
          f0 = flowScale * flow_values[0][0];
          f1 = flowScale * flow_values[1][0];
          f2 = flowScale * flow_values[2][0];
        }
        else {
          f0 = flowScale * flow_values[0][j];
          f1 = flowScale * flow_values[1][j];
          f2 = flowScale * flow_values[2][j];
        }
        int k = m;
        // base point of flow vector
        coordinates[m++] = spatial_values[0][j];
        coordinates[m++] = spatial_values[1][j];
        coordinates[m++] = spatial_values[2][j];
        int n = m;
        // end point of flow vector
        coordinates[m++] = coordinates[k++] + f0;
        coordinates[m++] = coordinates[k++] + f1;
        coordinates[m++] = coordinates[k++] + f2;
        k = n;
        // repeat end point of flow vector as
        // first point of arrow head
        coordinates[m++] = coordinates[n++];
        coordinates[m++] = coordinates[n++];
        coordinates[m++] = coordinates[n++];
        float fmin = Math.min(f0, Math.min(f1, f2));
        if (f0 == fmin) {
          a0 = BACK_SCALE * f0;
          a1 = PERP_SCALE * (f2 - f1);
          a2 = PERP_SCALE * (-f1 - f2);
        }
        else if (f1 == fmin) {
          a1 = BACK_SCALE * f1;
          a2 = PERP_SCALE * (f0 - f2);
          a0 = PERP_SCALE * (-f2 - f0);
        }
        else { // f2 == fmin
          a2 = BACK_SCALE * f2;
          a0 = PERP_SCALE * (f1 - f0);
          a1 = PERP_SCALE * (-f0 - f1);
        }
        // second point of arrow head
        coordinates[m++] = coordinates[n++] + a0;
        coordinates[m++] = coordinates[n++] + a1;
        coordinates[m++] = coordinates[n++] + a2;
      }
    }
    array.coordinates = coordinates;
    // array.vertexFormat = COORDINATES;

    if (color_values != null) {
      float[] colors = new float[12 * rlen];
      m = 0;
      float c0 = 0.0f, c1 = 0.0f, c2 = 0.0f;
      for (int j=0; j<len; j++) {
        if (range_select[0] == null ||
            range_select[0][j] == range_select[0][j]) {
          int k1 = m;
          int k2 = m;
          int k3 = m;
          // repeat color 4 times
          colors[m++] = color_values[0][j];
          colors[m++] = color_values[1][j];
          colors[m++] = color_values[2][j];
          colors[m++] = colors[k1++];
          colors[m++] = colors[k1++];
          colors[m++] = colors[k1++];
          colors[m++] = colors[k2++];
          colors[m++] = colors[k2++];
          colors[m++] = colors[k2++];
          colors[m++] = colors[k3++];
          colors[m++] = colors[k3++];
          colors[m++] = colors[k3++];
        }
      }
      array.colors = colors;
      // array.vertexFormat |= COLOR_3;
    }
    return array;
  }

  private static final double FONT = 0.07;

  public static VisADGeometryArray makeText(String[] text_values,
                TextControl text_control, float[][] spatial_values,
                float[][] color_values, float[][] range_select)
         throws VisADException { 
    if (text_values == null || text_values.length == 0 ||
        text_control == null) return null;
    int n = text_values.length;
    VisADLineArray[] as = new VisADLineArray[n];
    boolean center = text_control.getCenter();
    double size = text_control.getSize();
    double[] start = new double[3];
    double[] base = new double[] {size * FONT, 0.0, 0.0};
    double[] up = new double[] {0.0, size * FONT, 0.0};
    int k = 0;
    for (int i=0; i<n; i++) {
      if (range_select[0] == null || range_select[0].length == 1 ||
          range_select[0][i] == range_select[0][i]) {
/*
System.out.println("makeText, i = " + i + " text = " + text_values[i] +
                   " spatial_values = " + spatial_values[0][i] + " " +
                   spatial_values[1][i] + " " + spatial_values[2][i]);
*/
        start = new double[] {spatial_values[0][i],
                              spatial_values[1][i],
                              spatial_values[2][i]};
        as[k] = PlotText.render_label(text_values[i], start, base, up, center);
        k++;
      }
    }
    if (k == 0) return null;
    VisADLineArray[] arrays = new VisADLineArray[k];
    for (int i=0; i<k; i++) arrays[i] = as[i];
    return VisADLineArray.merge(arrays);
  }

  /** composite and transform color and Alpha DisplayRealType values
      from display_values, and return as (Red, Green, Blue, Alpha) */
  public static float[][] assembleColor(float[][] display_values,
                int valueArrayLength, int[] valueToScalar,
                DisplayImpl display, float[] default_values,
                float[][] range_select)
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
            throw new DisplayException("unrecognized color CoordinateSsystem: " +
                                       "ShadowType.assembleColor");
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
        if (real.equals(Display.RGBA)) {
          ColorAlphaControl control = (ColorAlphaControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
          float[][] color_values = control.lookupValues(values);
          if (len == 1) {
            for (int index = 0; index<4; index++) {
              rgba_singles[index] += color_values[index][0];
              rgba_single_counts[index]++;
            }
          }
          else { // (len != 1)
            colorComposite(rgba_values, rgba_value_counts, color_values);

            for (int index = 0; index<4; index++) {
              singleComposite(index, rgba_values, rgba_value_counts,
                              color_values[index]);
              // FREE
              color_values[index] = null;
            }
          }
        } // end if (real.equals(Display.RGBA))
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
          int default_index = getDefaultColorIndex(display, index);
/* WLH 7 Feb 98
          int default_index = 
            index == 0 ? display.getDisplayScalarIndex(Display.Red) :
            (index == 1 ? display.getDisplayScalarIndex(Display.Green) :
             (index == 2 ? display.getDisplayScalarIndex(Display.Blue) :
              display.getDisplayScalarIndex(Display.Alpha) ) );
*/
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

    // test for any missing values
    int big_len = rgba_values[0].length;
    for (int i=0; i<4; i++) {
      int len = rgba_values[i].length;
      for (int j=0; j<len; j++) {
        if (rgba_values[i][j] != rgba_values[i][j]) {
          if (range_select[0] == null) {
            range_select[0] = new float[big_len];
            for (int k=0; k<big_len; k++) range_select[0][k] = 0.0f;
          }
          if (len > 1) {
            range_select[0][j] = Float.NaN;
            rgba_values[i][j] = 0.0f;
          }
          else {
            for (int k=0; k<big_len; k++) range_select[0][k] = Float.NaN;
            // leave any single color value missing -
            // this will prevent anything from being rendered
          }
        }
      } // end for (int j=0; j<len; j++)
    } // end for (int i=0; i<4; i++)

    //
    // TO_DO
    // should colors be clamped to range (0.0f, 1.0f)?
    //

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

  public static int getDefaultColorIndex(DisplayImpl display, int index) {
    return index == 0 ? display.getDisplayScalarIndex(Display.Red) :
           (index == 1 ? display.getDisplayScalarIndex(Display.Green) :
            (index == 2 ? display.getDisplayScalarIndex(Display.Blue) :
             display.getDisplayScalarIndex(Display.Alpha) ) );
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
/*
        System.out.println("default color " + index + " is " +
                           default_values[default_index]);
*/
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
          throw new DisplayException("bad length: ShadowType.equalizeAndDefault");
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
        throw new DisplayException("bad length: ShadowType.singleComposite");
      }
    }
  }

  /** return a composite of SelectRange DisplayRealType values from
      display_values, as 0.0 for select and Double.Nan for no select
      (these values can be added to other DisplayRealType values) */
  public static float[][] assembleSelect(float[][] display_values, int domain_length,
                                        int valueArrayLength, int[] valueToScalar,
                                        DisplayImpl display) throws VisADException {
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();
    float[][] range_select = new float[1][];
    boolean anySelect = false;
    for (int i=0; i<valueArrayLength; i++) {
      float[] values = display_values[i];
      if (values != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        if (real.equals(Display.SelectRange)) {
          if (range_select[0] == null) {
            // MEM
            range_select[0] = new float[domain_length];
            for (int j=0; j<domain_length; j++) {
              range_select[0][j] = 0.0f;
            }
          }
          RangeControl control = (RangeControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
          float[] range = control.getRange();
          if (values.length == 1) {
            if (values[0] < range[0] || range[1] < values[0]) {
              for (int j=0; j<domain_length; j++) {
                range_select[0][j] = Float.NaN;
              }
              anySelect = true;
            }
          }
          else {
            for (int j=0; j<values.length; j++) {
              if (values[j] < range[0] || range[1] < values[j]) {
                range_select[0][j] = Float.NaN;
                anySelect = true;
              }
            }
          }
        } // end if (real.equals(Display.SelectRange))
      } // end if (values != null)
    } // end for (int i=0; i<valueArrayLength; i++)
    if (range_select[0] != null && !anySelect) range_select[0] = null;
    return range_select;
  }

  public String toString() {
    return getClass() + " for \n  " + Type.toString();
    // return " LevelOfDifficulty = " + LevelOfDifficulty;
  }

}

