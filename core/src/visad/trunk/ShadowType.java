
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
import java.awt.image.BufferedImage;

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
  boolean curvedTexture;

  /** Dtype and Rtype used only with ShadowSetType and
      Flat ShadowFunctionType */
  int Dtype; // Domain Type: D0, D1, D2, D3, D4 or Dbad
  int Rtype; // Range Type: R0, R1, R2, R3, R4 or Rbad
  /** possible values for Dtype */
  static final int D0 = 0; // (Unmapped)*
  static final int D1 = 1; // allSpatial + (SpatialOffset, IsoContour, Flow,
                           // Text, Shape, ShapeScale, Color, Alpha, Range,
                           // Unmapped)*
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
  static final int R3 = 3; // (IsoContour, Flow, Text, Shape, ShapeScale Color,
                           //  Alpha, Range, Unmapped)*
  static final int R4 = 4; // (Spatial, SpatialOffset, IsoContour, Flow,
                           //  Text, Shape, ShapeScale, Color, Alpha, Range,
                           //  Unmapped)*
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
    curvedTexture = false;
    LevelOfDifficulty = NOTHING_MAPPED;
    MultipleSpatialDisplayScalar = false;
    MultipleDisplayScalar = false;
    MappedDisplayScalar = false;
  }

  public DataDisplayLink getLink() {
    return Link;
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

  public boolean getCurvedTexture() {
    return curvedTexture;
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
/* WLH 17 April 99
          ShadowRealType[] r = getComponents(component, false);
          for (int k=0; k<r.length; k++) {
            reals[j] = r[k];
            j++;
          }
*/
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
          ShadowRealType[] r = getComponents(component, false);
          for (int k=0; k<r.length; k++) {
            reals[j] = r[k];
            j++;
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
          real.equals(Display.Shape) ||
          real.equals(Display.ShapeScale) ||
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
          real.equals(Display.Shape) ||
          real.equals(Display.ShapeScale) ||
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
          real.equals(Display.Shape) ||
          real.equals(Display.ShapeScale) ||
          real.equals(Display.Text) ||
          real.equals(Display.IsoContour)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Color, Alpha, Range, Flow, Shape,
      ShapeScale, Text, Unmapped) */
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
          real.equals(Display.Shape) ||
          real.equals(Display.ShapeScale) ||
          real.equals(Display.Text) ||
          real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Color, Range, Unmapped) */
  boolean checkColorRange(int[] display_indices) throws RemoteException {
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
      if (real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Color, Alpha, Range, Unmapped) */
  boolean checkColorAlphaRange(int[] display_indices) throws RemoteException {
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
          real.equals(Display.Alpha) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Spatial, SpatialOffset, Color, Alpha,
      Range, Flow, Shape, ShapeScale, Text, Unmapped) */
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
          real.equals(Display.Shape) ||
          real.equals(Display.ShapeScale) ||
          real.equals(Display.Text) ||
          real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Spatial, SpatialOffset, Color,
      Range, Unmapped) */
  boolean checkSpatialColorRange(int[] display_indices) throws RemoteException {
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
      if (real.equals(Display.RGB) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.SelectRange)) continue;
      return false;
    }
    return true;
  }

  /** test for display_indices in (Spatial, SpatialOffset, Color,
      Alpha, Range, Unmapped) */
  boolean checkSpatialColorAlphaRange(int[] display_indices) throws RemoteException {
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
      if (real.equals(Display.RGB) ||
          real.equals(Display.RGBA) ||
          real.equals(Display.Alpha) ||
          real.equals(Display.HSV) ||
          real.equals(Display.CMY)) continue;  // more Color
      if (real.equals(Display.SelectRange)) continue;
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

  /** map values to display_values according to ScalarMap-s in reals */
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

/* CTR: 13 Oct 1998 - BEGIN CHANGES */
  public static VisADGeometryArray makePointGeometry(float[][] spatial_values,
                byte[][] color_values) throws VisADException {
    return makePointGeometry(spatial_values, color_values, false);
  }

  public static VisADGeometryArray makePointGeometry(float[][] spatial_values,
                byte[][] color_values, boolean compress)
                                        throws VisADException {
    if (spatial_values == null) {
      throw new DisplayException("bad spatial_values: " +
                                 "ShadowType.makePointGeometry: bad");
    }
    VisADPointArray array = new VisADPointArray();
 
    if (compress) {
      // redimension arrays to eliminate Float.NaN values
      int len = spatial_values.length;
      int clen;
      if (color_values == null) clen = 0;
      else clen = color_values.length;
      float[] f = spatial_values[0];
      int flen = f.length;
      int nan = 0;
      for (int i=0; i<flen; i++) if (f[i] != f[i]) nan++;
      if (nan > 0) {
        float[][] new_s_values = new float[len][flen-nan];
        byte[][] new_c_values = color_values;
        if (clen > 0) new_c_values = new byte[len][flen-nan];
        int c = 0;
        for (int i=0; i<flen; i++) {
          if (f[i] == f[i]) {
            for (int j=0; j<len; j++) {
              new_s_values[j][c] = spatial_values[j][i];
            }
            for (int j=0; j<clen; j++) {
              new_c_values[j][c] = color_values[j][i];
            }
            c++;
          }
        }
        spatial_values = new_s_values;
        color_values = new_c_values;
      }
    }

    // set coordinates and colors
    // MEM
    SampledSet.setGeometryArray(array, spatial_values, 3, color_values);
    return array;
  }
/* CTR: 13 Oct 1998 - END CHANGES */

  /** collect and transform Shape DisplayRealType values from display_values;
      offset by spatial_values, colored by color_values and selected by
      range_select */
  public static VisADGeometryArray[] assembleShape(float[][] display_values,
                int valueArrayLength, int[] valueToMap, Vector MapVector,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                float[][] spatial_values, byte[][] color_values,
                boolean[][] range_select, int index)
         throws VisADException, RemoteException {

    int total_length = 0;
    Vector array_vector = new Vector();
    float x = spatial_values[0][0];
    float y = spatial_values[1][0];
    float z = spatial_values[2][0];
    byte r = 0;
    byte g = 0;
    byte b = 0;
    byte a = 0;
    int color_length = 0;
    if (color_values != null) {
      color_length = color_values.length;
      r = color_values[0][0];
      g = color_values[1][0];
      b = color_values[2][0];
      if (color_length > 3) a = color_values[3][0];
    }

    float[] scales = null;
    for (int i=0; i<valueArrayLength; i++) {
      if (display_values[i] != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        if (real.equals(Display.ShapeScale)) {
          if (index < 0) {
            scales = display_values[i];
            display_values[i] = null; // MEM_WLH 27 March 99
          }
          else {
            if (display_values[i].length == 1) {
              scales = new float[] {display_values[i][0]};
            }
            else {
              scales = new float[] {display_values[i][index]};
            }
          }
        }
      }
    }
    if (scales == null) {
      int default_index = display.getDisplayScalarIndex(Display.ShapeScale);
      float default_scale = default_values[default_index];
      scales = new float[] {default_scale};
    }

    float[] values = null;
    ShapeControl control = null;
    for (int j=0; j<valueArrayLength; j++) {
      if (display_values[j] != null) {
        int displayScalarIndex = valueToScalar[j];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);

        if (real.equals(Display.Shape)) {
          if (index < 0) {
            values = display_values[j];
            display_values[j] = null; // MEM_WLH 27 March 99
          }
          else {
            if (display_values[j].length == 1) {
              values = new float[] {display_values[j][0]};
            }
            else {
              values = new float[] {display_values[j][index]};
            }
          }
          control = (ShapeControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[j])).getControl();
          if (values == null || control == null) continue;

          // make len maximum of lengths of color_values,
          // spatial_values & scales
          int len = values.length;
          if (color_values != null) {
            if (color_values[0].length > len) len = color_values[0].length;
          }
          if (spatial_values[0].length > len) len = spatial_values[0].length;
          if (scales.length > len) len = scales.length;
          // expand values if necessary
          if (values.length < len) {
            float[] new_values = new float[len];
            for (int i=0; i<len; i++) new_values[i] = values[0];
            values = new_values;
          }
          VisADGeometryArray[] arrays = control.getShapes(values);
          for (int i=0; i<arrays.length; i++) {
            VisADGeometryArray array = arrays[i];
            if (range_select[0] != null) {
              if (range_select[0].length == 1) {
                if (!range_select[0][0]) array = null;
              }
              else {
                if (!range_select[0][i]) array = null;
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
              float scale = (scales.length == 1) ? scales[0] : scales[i];
              for (int k=0; k<array.coordinates.length; k+=3) {
                array.coordinates[k] = x + scale * array.coordinates[k];
                array.coordinates[k+1] = y + scale * array.coordinates[k+1];
                array.coordinates[k+2] = z + scale * array.coordinates[k+2];
              }
      
              if (array.colors == null && color_values != null) {
                array.colors = new byte[color_length * npts];
                if (color_values[0].length > 1) {
                  r = color_values[0][i];
                  g = color_values[1][i];
                  b = color_values[2][i];
                  if (color_length > 3) a = color_values[3][i];
                }
                for (int k=0; k<array.coordinates.length; k+=color_length) {
                  array.colors[k] = r;
                  array.colors[k+1] = g;
                  array.colors[k+2] = b;
                  if (color_length > 3) array.colors[k+3] = a;
                }
              }
      
            }
          } // end for (int i=0; i<arrays.length; i++)
          total_length += arrays.length;
          array_vector.addElement(arrays);
        } // end if (real.equals(Display.Shape))
      } // end if (display_values[i] != null)
    } // end for (int j=0; j<valueArrayLength; j++)

    if (total_length == 0) return null;
    VisADGeometryArray[] total_arrays =
      new VisADGeometryArray[total_length];
    Enumeration arrayses = array_vector.elements();
    int k = 0;
    while (arrayses.hasMoreElements()) {
      VisADGeometryArray[] arrays =
        (VisADGeometryArray[]) arrayses.nextElement();
      System.arraycopy(arrays, 0, total_arrays, k, arrays.length);
      k += arrays.length;
    }
    return total_arrays;
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
                int[] spatialDimensions, boolean[][] range_select,
                float[][] flow1_values, float[][] flow2_values,
                float[] flowScale, boolean[] swap, DataRenderer renderer)
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

    // spatial map getRange() results for flow adjustment
    double[] ranges = new double[] {Double.NaN, Double.NaN, Double.NaN};
    // some helpers for computing ranges for flow adjustment
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

    // indexed by tuple_index
    int[] spatial_value_indices = {-1, -1, -1};

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
          spatial_value_indices[tuple_index] = i;
          spatial_values[tuple_index] = display_values[i];
          len = Math.max(len, display_values[i].length);
          display_values[i] = null; // MEM_WLH 27 March 99
          spatialDimensions[0]++; // spatialDomainDimension
          if (inherited_values[i] == 0) {
            // don't count inherited spatial dimensions
            tuple_indices[spatialDimension] = tuple_index;
            spatialDimension++; // # non-inherited spatial dimensions
          }
          double[] map_range =
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getRange();
          ranges[tuple_index] = map_range[1] - map_range[0];
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
    else if (domain_set == null) {
      spatialDimensions[1] = spatialDimension; // spatialManifoldDimension
    }
    else if (!allSpatial) {
      spatialDimensions[1] = spatialDimension; // spatialManifoldDimension
      if (set_for_shape) {
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
        try {
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
        }
        catch (VisADException e) {
          domain_set = null;
        }
        // System.out.println("IrregularSet done");
      }
      else { // !set_for_shape
        domain_set = null;
      }
    }
    else { // spatialDimension > 0 && allSpatial && domain_set != null
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
          range_select[0] = new boolean[1];
          range_select[0][0] = false;
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
      } // end if (spatial_tuple == Display.DisplaySpatialCartesianTuple)
    }

    // first equalize lengths of flow*_values and spatial_values
    boolean anyFlow = false;
    int[] flen = {0, 0};
    float[][][] ff_values = {flow1_values, flow2_values};
    for (int k=0; k<2; k++) {
      for (int i=0; i<3; i++) {
        if (ff_values[k][i] != null) {
          anyFlow = true;
          flen[k] = Math.max(flen[k], ff_values[k][i].length);
        }
      }
    }
    len = Math.max(len, Math.max(flen[0], flen[1]));
    fillOut(spatial_values, len);
    if (flen[0] > 0) fillOut(flow1_values, len);
    if (flen[1] > 0) fillOut(flow2_values, len);

    // adjust flow for spatial setRange scaling
    double max_range = -1.0;
    for (int i=0; i<3; i++) {
      if (ranges[i] == ranges[i]) {
        double ar = Math.abs(ranges[i]);
        if (ar > max_range) max_range = ar;
      }
    }
    for (int i=0; i<3; i++) {
      if (ranges[i] == ranges[i]) {
        ranges[i] = ranges[i] / max_range;
      }
      else {
        ranges[i] = 1.0;
      }
    }
    for (int k=0; k<2; k++) {
      if (!(renderer.getRealVectorTypes(k) instanceof EarthVectorType)) {
        if (ff_values[k][0] != null ||
            ff_values[k][1] != null ||
            ff_values[k][2] != null) {
          for (int j=0; j<len; j++) {
            float old_speed = 0.0f;
            float new_speed = 0.0f;
            for (int i=0; i<3; i++) {
              if (ff_values[k][i] != null) {
                old_speed += ff_values[k][i][j] * ff_values[k][i][j];
                ff_values[k][i][j] *= ranges[i];
                new_speed += ff_values[k][i][j] * ff_values[k][i][j];
              }
            }
            // but don't change vector magnitude ??
            float ratio = (float) Math.sqrt(old_speed / new_speed);
            for (int i=0; i<3; i++) {
              if (ff_values[k][i] != null) {
                ff_values[k][i][j] *= ratio;
              }
            }
          }
        } // end if (ff_values[k][0] != null || ...)
      } // end if (!(renderer.getRealVectorTypes(k) instanceof EarthVectorType))
    } // end for (int k=0; k<2; k++)

    // adjust Flow values for coordinate transform
    if (spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
      if (anyFlow) {
        renderer.setEarthSpatialDisplay(null, spatial_tuple, display,
                 spatial_value_indices, display_values, default_values,
                 ranges);
      }
    }
    else {
      // transform tuple_values to DisplaySpatialCartesianTuple
      CoordinateSystem coord = spatial_tuple.getCoordinateSystem();

      float[][][] vector_ends = new float[2][][];
      if (anyFlow) {
        renderer.setEarthSpatialDisplay(coord, spatial_tuple, display,
                 spatial_value_indices, display_values, default_values,
                 ranges);

        // compute and transform 'end points' of flow vectors
        for (int k=0; k<2; k++) {
          if (!(renderer.getRealVectorTypes(k) instanceof EarthVectorType)) {
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
          } // end if (!(renderer.getRealVectorTypes(k) instanceof EarthVectorType))
        } // end for (int k=0; k<2; k++)
      }

      // transform spatial_values
      float[][] new_spatial_values = coord.toReference(spatial_values);
      for (int i=0; i<3; i++) spatial_values[i] = new_spatial_values[i];

      if (anyFlow) {
        // subtract transformed spatial_values from transformed flow vectors
        for (int k=0; k<2; k++) {
          if (!(renderer.getRealVectorTypes(k) instanceof EarthVectorType)) {
            if (flen[k] > 0) {
              for (int i=0; i<3; i++) {
                for (int j=0; j<len; j++) {
                  vector_ends[k][i][j] =
                    (vector_ends[k][i][j] - spatial_values[i][j]) / flowScale[k];
                }
                ff_values[k][i] = vector_ends[k][i];
              }
            }
          } // end if (!(renderer.getRealVectorTypes(k) instanceof EarthVectorType))
        } // end for (int k=0; k<2; k++)
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
          display_values[i] = null; // MEM_WLH 27 March 99
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
          range_select[0] = new boolean[1];
          range_select[0][0] = false;
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
              range_select[0] = new boolean[len];
              for (int k=0; k<len; k++) range_select[0][k] = true;
            }
            else if (range_select[0].length < len) {
              // assume range_select[0].length == 1
              boolean[] r = new boolean[len];
              for (int k=0; k<len; k++) r[k] = range_select[0][0];
              range_select[0] = r;
            }
            range_select[0][j] = false;
            spatial_values[i][j] = Float.NaN;
          }
        }
      }
    } // end for (int i=0; i<3; i++)

    if (set_needed) {
      try {
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
      catch (VisADException e) {
        return null;
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
                float[] default_values, boolean[][] range_select,
                DataRenderer renderer)
         throws VisADException, RemoteException {

    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

    int[] flen = {0, 0};
    float[][][] ff_values = {flow1_values, flow2_values};
    DisplayTupleType[] flow_tuple =
      {Display.DisplayFlow1Tuple, Display.DisplayFlow2Tuple};

    boolean anyFlow = false;
    ScalarMap[][] maps = new ScalarMap[2][3];

    for (int i=0; i<valueArrayLength; i++) {
      if (display_values[i] != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        DisplayTupleType tuple = real.getTuple();
        for (int k=0; k<2; k++) {
          if (flow_tuple[k].equals(tuple)) {
            ScalarMap map = (ScalarMap) MapVector.elementAt(valueToMap[i]);
            FlowControl control = (FlowControl) map.getControl();
            flowScale[k] = control.getFlowScale();
            int flow_index = real.getTupleIndex();
            ff_values[k][flow_index] = display_values[i];
            flen[k] = Math.max(flen[k], display_values[i].length);
            display_values[i] = null; // MEM_WLH 27 March 99
            maps[k][flow_index] = map;
            anyFlow = true;
          }
        }
      }
    }
    if (anyFlow) renderer.setFlowDisplay(maps, flowScale);

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
              range_select[0] = new boolean[1];
              range_select[0][0] = false;
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
                  range_select[0] = new boolean[flen[k]];
                  for (int m=0; m<flen[k]; m++) range_select[0][m] = true;
                }
                else if (range_select[0].length < flen[k]) {
                  // assume range_select[0].length == 1
                  boolean[] r = new boolean[flen[k]];
                  for (int m=0; m<flen[k]; m++) r[m] = range_select[0][0];
                  range_select[0] = r;
                }
                range_select[0][j] = false;
                ff_values[k][i][j] = 0.0f;
              }
            } // end for (int j=0; j<flen[k]; j++)
          } // end if (!missing_checked[i])
        } // end for (int i=0; i<3; i++)
      } // end if (flen[k] > 0)
    } // end for (int k=0; k<2; k++)
    // end of 'this should all happen in flow rendering method'
  }

  public static final float METERS_PER_DEGREE = 111137.0f;

  public static float[][] adjustFlowToEarth(int which, float[][] flow_values,
                                    float[][] spatial_values, float flowScale,
                                    DataRenderer renderer)
         throws VisADException {
    if (!(renderer.getRealVectorTypes(which) instanceof EarthVectorType)) {
      // only do this for EarthVectorType
      return flow_values;
    }

    int flen = flow_values[0].length;

    // get flow_values maximum
    float scale = 0.0f;
    for (int j=0; j<flen; j++) {
      if (Math.abs(flow_values[0][j]) > scale) {
        scale = (float) Math.abs(flow_values[0][j]);
      }
      if (Math.abs(flow_values[1][j]) > scale) {
        scale = (float) Math.abs(flow_values[1][j]);
      }
      if (Math.abs(flow_values[2][j]) > scale) {
        scale = (float) Math.abs(flow_values[2][j]);
      }
    }
    float inv_scale = 1.0f / scale;
    if (inv_scale != inv_scale) inv_scale = 1.0f;
/*
System.out.println("spatial_values = " + spatial_values[0][0] + " " +
                   spatial_values[1][0] + " " + spatial_values[2][0]);
*/
    // convert spatial DisplayRealType values to earth coordinates
    float[][] earth_locs = renderer.spatialToEarth(spatial_values);
    if (earth_locs == null) return flow_values;
    int elen = earth_locs.length; // 2 or 3
/*
System.out.println("earth_locs = " + earth_locs[0][0] + " " + earth_locs[1][0]);
*/
    // convert earth coordinate Units to (radian, radian, meter)
    boolean other_meters = false;
    Unit[] earth_units = renderer.getEarthUnits();
    if (earth_units != null) {
      if (Unit.canConvert(earth_units[0], CommonUnit.radian)) {
        earth_locs[0] =
          CommonUnit.radian.toThis(earth_locs[0], earth_units[0]);
      }
      if (Unit.canConvert(earth_units[1], CommonUnit.radian)) {
        earth_locs[1] =
          CommonUnit.radian.toThis(earth_locs[1], earth_units[1]);
      }
      if (elen == 3 && earth_units.length == 3 &&
          Unit.canConvert(earth_units[2], CommonUnit.meter)) {
        other_meters = true;
        earth_locs[2] =
          CommonUnit.meter.toThis(earth_locs[2], earth_units[2]);
      }
    }
/*
System.out.println("radian earth_locs = " + earth_locs[0][0] +
                   " " + earth_locs[1][0]);
*/
    // add scaled flow vector to earth location
    if (elen == 3) {
      // assume meters even if other_meters == false
      float factor_lat = (float) (inv_scale * 1000.0f *
                         Data.DEGREES_TO_RADIANS / METERS_PER_DEGREE);
      float factor_vert = inv_scale * 1000.0f;
      for (int j=0; j<flen; j++) {
        earth_locs[2][j] += factor_vert * flow_values[2][j];
        earth_locs[1][j] += factor_lat * flow_values[0][j] /
                            ((float) Math.cos(earth_locs[0][j]));
        earth_locs[0][j] += factor_lat * flow_values[1][j];
      }
    }
    else {
      float factor_lat = 0.00001f * inv_scale *
                         (0.5f * renderer.getLatLonRange());
      for (int j=0; j<flen; j++) {
        earth_locs[1][j] += factor_lat * flow_values[0][j] /
                            ((float) Math.cos(earth_locs[0][j]));
        earth_locs[0][j] += factor_lat * flow_values[1][j];
      }
    }
/*
System.out.println("flow earth_locs = " + earth_locs[0][0] +
                   " " + earth_locs[1][0]);
*/
    // convert earth coordinate Units from (radian, radian, meter)
    if (earth_units != null) {
      if (Unit.canConvert(earth_units[0], CommonUnit.radian)) {
        earth_locs[0] =
          CommonUnit.radian.toThat(earth_locs[0], earth_units[0]);
      }
      if (Unit.canConvert(earth_units[1], CommonUnit.radian)) {
        earth_locs[1] =
          CommonUnit.radian.toThat(earth_locs[1], earth_units[1]);
      }
      if (elen == 3 && earth_units.length == 3 &&
          Unit.canConvert(earth_units[2], CommonUnit.meter)) {
        earth_locs[2] =
          CommonUnit.meter.toThat(earth_locs[2], earth_units[2]);
      }
    }
/*
System.out.println("degree earth_locs = " + earth_locs[0][0] +
                   " " + earth_locs[1][0]);
*/
    // convert earth coordinates to spatial DisplayRealType values
    if (elen == 3) {
      earth_locs = renderer.earthToSpatial(earth_locs, null);
    }
    else {
      // apply vertical flow in earthToSpatial
      float factor_vert = 0.00001f * inv_scale;
      float[] vert = new float[flen];
      for (int j=0; j<flen; j++) {
        vert[j] = factor_vert * flow_values[2][j];
      }
      earth_locs = renderer.earthToSpatial(earth_locs, vert);
    }
/*
System.out.println("spatial earth_locs = " + earth_locs[0][0] + " " +
                   earth_locs[1][0] + " " + earth_locs[2][0]);
*/
    // flow = change in spatial_values
    for (int i=0; i<3; i++) {
      for (int j=0; j<flen; j++) {
        earth_locs[i][j] -= spatial_values[i][j];
      }
    }
/*
System.out.println("vector earth_locs = " + earth_locs[0][0] + " " +
                   earth_locs[1][0] + " " + earth_locs[2][0]);
*/
    // combine earth_locs direction with flow_values magnitude
    for (int j=0; j<flen; j++) {
      float mag =
        (float) Math.sqrt(flow_values[0][j] * flow_values[0][j] +
                          flow_values[1][j] * flow_values[1][j] +
                          flow_values[2][j] * flow_values[2][j]);
      float new_mag =
        (float) Math.sqrt(earth_locs[0][j] * earth_locs[0][j] +
                          earth_locs[1][j] * earth_locs[1][j] +
                          earth_locs[2][j] * earth_locs[2][j]);
      float ratio = mag / new_mag;
      flow_values[0][j] = ratio * earth_locs[0][j];
      flow_values[1][j] = ratio * earth_locs[1][j];
      flow_values[2][j] = ratio * earth_locs[2][j];
    }
/*
System.out.println("flow_values = " + flow_values[0][0] + " " +
                   flow_values[1][0] + " " + flow_values[2][0]);
*/
    return flow_values;
  }


  private static final float BACK_SCALE = -0.15f;
  private static final float PERP_SCALE = 0.15f;

  public VisADGeometryArray[] makeFlow(int which, float[][] flow_values,
                float flowScale, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
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
        if (range_select[0][j]) rlen++;
      }
    }
    if (rlen == 0) return null;

    DataRenderer renderer = getLink().getRenderer();
    flow_values = adjustFlowToEarth(which, flow_values, spatial_values,
                                    flowScale, renderer);

    array.vertexCount = 6 * rlen;

    float[] coordinates = new float[18 * rlen];
    int m = 0;
    // flow vector
    float f0 = 0.0f, f1 = 0.0f, f2 = 0.0f;
    // arrow head vector
    float a0 = 0.0f, a1 = 0.0f, a2 = 0.0f;
    float b0 = 0.0f, b1 = 0.0f, b2 = 0.0f;
    for (int j=0; j<len; j++) {
      if (range_select[0] == null || range_select[0][j]) {
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
        // k = orig m
        // m = orig m + 3
        // end point of flow vector
        coordinates[m++] = coordinates[k++] + f0;
        coordinates[m++] = coordinates[k++] + f1;
        coordinates[m++] = coordinates[k++] + f2;
        k = n;
        // n = orig m + 3
        // m = orig m + 6
        // repeat end point of flow vector as
        // first point of first arrow head
        coordinates[m++] = coordinates[n++];
        coordinates[m++] = coordinates[n++];
        coordinates[m++] = coordinates[n++];
        boolean mode2d = display.getDisplayRenderer().getMode2D();
        b0 = a0 = BACK_SCALE * f0;
        b1 = a1 = BACK_SCALE * f1;
        b2 = a2 = BACK_SCALE * f2;

        if (mode2d || (f2 <= f0 && f2 <= f1)) {
          a0 += PERP_SCALE * f1;
          a1 -= PERP_SCALE * f0;
          b0 -= PERP_SCALE * f1;
          b1 += PERP_SCALE * f0;
        }
        else if (f1 <= f0) {
          a0 += PERP_SCALE * f2;
          a2 -= PERP_SCALE * f0;
          b0 -= PERP_SCALE * f2;
          b2 += PERP_SCALE * f0;
        }
        else { // f0 is least
          a1 += PERP_SCALE * f2;
          a2 -= PERP_SCALE * f1;
          b1 -= PERP_SCALE * f2;
          b2 += PERP_SCALE * f1;
        }

        k = n;
        // n = orig m + 6
        // m = orig m + 9
        // second point of first arrow head
        coordinates[m++] = coordinates[n++] + a0;
        coordinates[m++] = coordinates[n++] + a1;
        coordinates[m++] = coordinates[n++] + a2;

        n = k;
        // k = orig m + 6
        // first point of second arrow head
        coordinates[m++] = coordinates[k++];
        coordinates[m++] = coordinates[k++];
        coordinates[m++] = coordinates[k++];

        // n = orig m + 6
        // second point of second arrow head
        coordinates[m++] = coordinates[n++] + b0;
        coordinates[m++] = coordinates[n++] + b1;
        coordinates[m++] = coordinates[n++] + b2;
      }
    }
    array.coordinates = coordinates;
    // array.vertexFormat = COORDINATES;

    if (color_values != null) {
      byte[] colors = new byte[18 * rlen];
      m = 0;
      float c0 = 0.0f, c1 = 0.0f, c2 = 0.0f;
      for (int j=0; j<len; j++) {
        if (range_select[0] == null || range_select[0][j]) {
          int k1 = m;
          int k2 = m;
          int k3 = m;
          int k4 = m;
          int k5 = m;
          // repeat color 6 times
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
          colors[m++] = colors[k4++];
          colors[m++] = colors[k4++];
          colors[m++] = colors[k4++];
          colors[m++] = colors[k5++];
          colors[m++] = colors[k5++];
          colors[m++] = colors[k5++];
        }
      }
      array.colors = colors;
      // array.vertexFormat |= COLOR_3;
    }
    return new VisADGeometryArray[] {array};
  }

  private static final double FONT = 0.07;

  public static VisADGeometryArray makeText(String[] text_values,
                TextControl text_control, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
         throws VisADException { 
    if (text_values == null || text_values.length == 0 ||
        text_control == null) return null;

    byte r = 0;
    byte g = 0;
    byte b = 0;
    if (color_values != null) {
      r = color_values[0][0];
      g = color_values[1][0];
      b = color_values[2][0];
    }

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
          range_select[0][i]) {
/*
System.out.println("makeText, i = " + i + " text = " + text_values[i] +
                   " spatial_values = " + spatial_values[0][i] + " " +
                   spatial_values[1][i] + " " + spatial_values[2][i]);
*/
        start = new double[] {spatial_values[0][i],
                              spatial_values[1][i],
                              spatial_values[2][i]};
        as[k] = PlotText.render_label(text_values[i], start, base, up, center);
        int len = (as[k] == null) ? 0 : as[k].coordinates.length;
        if (len > 0 && color_values != null) {
          if (color_values[0].length > 1) {
            r = color_values[0][k];
            g = color_values[1][k];
            b = color_values[2][k];
          }
          byte[] colors = new byte[len];
          for (int j=0; j<len; j+=3) {
            colors[j] = r;
            colors[j+1] = g;
            colors[j+2] = b;
          }
          as[k].colors = colors;
        }
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
  public static byte[][] assembleColor(float[][] display_values,
                int valueArrayLength, int[] valueToScalar,
                DisplayImpl display, float[] default_values,
                boolean[][] range_select, boolean[] single_missing)
         throws VisADException, RemoteException {
    float[][] rgba_values = new float[4][];
    float[] rgba_value_counts = {0.0f, 0.0f, 0.0f, 0.0f};
    float[] rgba_singles = new float[4];
    float[] rgba_single_counts = {0.0f, 0.0f, 0.0f, 0.0f};
    float[][] tuple_values = new float[3][];
    float[] tuple_value_counts = {0.0f, 0.0f, 0.0f};
    float[] tuple_singles = new float[3];
    float[] tuple_single_counts = {0.0f, 0.0f, 0.0f};

    // mark array to keep track of which valueIndices have
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
              // FREE
              display_values[i] = null; // MEM_WLH 27 March 99
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
/*
ScalarMap map = (ScalarMap) MapVector.elementAt(valueToMap[i]);
System.out.println("map = " + map);
int nummissing = 0;
for (int k=0; k<values.length; k++) {
  if (values[k] != values[k]) nummissing++;
}
System.out.println("values: nummissing = " + nummissing);
*/

          float[][] color_values = control.lookupValues(values);

/*
nummissing = 0;
for (int k=0; k<color_values[0].length; k++) {
  if (color_values[0][k] != color_values[0][k]) nummissing++;
}
System.out.println("color_values: nummissing = " + nummissing);
*/
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
          // FREE
          display_values[i] = null; // MEM_WLH 27 March 99
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
          // FREE
          display_values[i] = null; // MEM_WLH 27 March 99
        } // end if (real.equals(Display.RGBA))
        if (real.equals(Display.Alpha)) {
          if (len == 1) {
            rgba_singles[3] += values[0];
            rgba_single_counts[3]++;
          }
          else {
            singleComposite(3, rgba_values, rgba_value_counts, values);
          }
          // FREE
          display_values[i] = null; // MEM_WLH 27 March 99
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
            range_select[0] = new boolean[big_len];
            for (int k=0; k<big_len; k++) range_select[0][k] = true;
          }
          if (len > 1) {
            range_select[0][j] = false;
            rgba_values[i][j] = 0.0f;
          }
          else {
            for (int k=0; k<big_len; k++) range_select[0][k] = false;
            // leave any single color value missing -
            // this will prevent anything from being rendered
            // MEM_WLH
            rgba_values[i][j] = 0.0f;
            single_missing[i] = true;
          }
        }
      } // end for (int j=0; j<len; j++)
    } // end for (int i=0; i<4; i++)

    //
    // TO_DO
    // should colors be clamped to range (0.0f, 1.0f)?
    //

/* MEM_WLH
    return rgba_values;
*/
    // MEM_WLH
    // page 291 of Java3D book says byte colors are [0, 255] range
    byte[][] b = new byte[rgba_values.length][];
    for (int i=0; i<rgba_values.length; i++) {
      if (rgba_values[i] != null) {
        int len = rgba_values[i].length;
        b[i] = new byte[len];
        for (int j=0; j<len; j++) {
          int k = (int) (rgba_values[i][j] * 255.0);
          k = (k < 0) ? 0 : (k > 255) ? 255 : k;
          b[i][j] = (byte) ((k < 128) ? k : k - 256);
        }
      }
    }
    return b;
  }

  public static final float byteToFloat(byte b) {
    return (b < 0) ? (((float) b) + 256.0f) / 255.0f : ((float) b) / 255.0f;
    //
    //  no 255.0f divide:
    // return ((b < 0) ? ((float) b) + 256.0f : ((float) b));
  }

  public static final byte floatToByte(float f) {
/*
    int k = (int) (f * 255.0);
    k = (k < 0) ? 0 : (k > 255) ? 255 : k;
    return (byte) ((k < 128) ? k : k - 256);
*/
    int k = (int) (f * 255.0);
    return (byte) ( (k < 0) ? 0 : ((k > 255) ? -1 : ((k < 128) ? k : k - 256) ) );
    //
    // no 255.0f multiply:
    // return ((byte) ( ((int) f) < 0 ? 0 : ((int) f) > 255 ? -1 :
    //          ((int) f) < 128 ? ((byte) f) : ((byte) (f - 256.0f)) ));
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
  public static boolean[][] assembleSelect(float[][] display_values,
                             int domain_length, int valueArrayLength,
                             int[] valueToScalar, DisplayImpl display)
         throws VisADException {
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();
    boolean[][] range_select = new boolean[1][];
    boolean anySelect = false;
    for (int i=0; i<valueArrayLength; i++) {
      float[] values = display_values[i];
      if (values != null) {
        int displayScalarIndex = valueToScalar[i];
        DisplayRealType real = display.getDisplayScalar(displayScalarIndex);
        if (real.equals(Display.SelectRange)) {
          if (range_select[0] == null) {
            // MEM
            range_select[0] = new boolean[domain_length];
            for (int j=0; j<domain_length; j++) {
              range_select[0][j] = true;
            }
          }
          RangeControl control = (RangeControl)
            ((ScalarMap) MapVector.elementAt(valueToMap[i])).getControl();
          float[] range = control.getRange();
          if (values.length == 1) {
            if (values[0] < range[0] || range[1] < values[0]) {
              for (int j=0; j<domain_length; j++) {
                range_select[0][j] = false;
              }
              anySelect = true;
            }
          }
          else {
            for (int j=0; j<values.length; j++) {
              if (values[j] < range[0] || range[1] < values[j]) {
                range_select[0][j] = false;
                anySelect = true;
              }
            }
          }
          // FREE
          display_values[i] = null; // MEM_WLH 27 March 99
        } // end if (real.equals(Display.SelectRange))
      } // end if (values != null)
    } // end for (int i=0; i<valueArrayLength; i++)
    if (range_select[0] != null && !anySelect) range_select[0] = null;
    return range_select;
  }

  /** transform data into a (Java3D or Java2D) scene graph;
      add generated scene graph components as children of group;
      group is Group (Java3D) or VisADGroup (Java2D);
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process */
  public boolean terminalTupleOrScalar(Object group, float[][] display_values,
                                String text_value, TextControl text_control,
                                int valueArrayLength, int[] valueToScalar,
                                float[] default_values, int[] inherited_values,
                                DataRenderer renderer, ShadowType shadow_api)
          throws VisADException, RemoteException {
 
    GraphicsModeControl mode = (GraphicsModeControl)
      display.getGraphicsModeControl().clone();
    float pointSize = 
      default_values[display.getDisplayScalarIndex(Display.PointSize)];
    mode.setPointSize(pointSize, true);
    float lineWidth =
      default_values[display.getDisplayScalarIndex(Display.LineWidth)];
    mode.setLineWidth(lineWidth, true);
 
    float[][] flow1_values = new float[3][];
    float[][] flow2_values = new float[3][];
    float[] flowScale = new float[2];
    boolean[][] range_select = new boolean[1][];
    assembleFlow(flow1_values, flow2_values, flowScale,
                 display_values, valueArrayLength, valueToScalar,
                 display, default_values, range_select, renderer);
 
    if (range_select[0] != null && !range_select[0][0]) {
      // data not selected
      return false;
    }

    boolean[] swap = {false, false, false};
    int[] spatialDimensions = new int[2];
    float[][] spatial_values = new float[3][];
    assembleSpatial(spatial_values, display_values, valueArrayLength,
                    valueToScalar, display, default_values,
                    inherited_values, null, false, false,
                    spatialDimensions, range_select, flow1_values,
                    flow2_values, flowScale, swap, renderer);

    if (range_select[0] != null && !range_select[0][0]) {
      // data not selected
      return false;
    }
 
    boolean[] single_missing = {false, false, false, false};
    byte[][] color_values =
      assembleColor(display_values, valueArrayLength, valueToScalar,
                    display, default_values, range_select, single_missing);
 
    if (range_select[0] != null && !range_select[0][0]) {
      // data not selected
      return false;
    }
 
    int LevelOfDifficulty = getLevelOfDifficulty();
    if (LevelOfDifficulty == SIMPLE_TUPLE) {
      // only manage Spatial, Color and Alpha here
      // i.e., the 'dots'
 
      if (single_missing[0] || single_missing[1] ||
          single_missing[2]) {
        // System.out.println("single missing alpha");
        // a single missing color value, so render nothing
        return false;
      }
      // put single color in appearance
/*
      ColoringAttributes constant_color = new ColoringAttributes();
      constant_color.setColor(byteToFloat(color_values[0][0]),
                              byteToFloat(color_values[1][0]),
                              byteToFloat(color_values[2][0]));
*/
      float[] constant_color = new float[] {byteToFloat(color_values[0][0]),
                                            byteToFloat(color_values[1][0]),
                                            byteToFloat(color_values[2][0])};
      float constant_alpha = Float.NaN;


      VisADGeometryArray array;

      boolean anyShapeCreated = false;
      int[] valueToMap = display.getValueToMap();
      Vector MapVector = display.getMapVector();
      VisADGeometryArray[] arrays =
        assembleShape(display_values, valueArrayLength, valueToMap, MapVector,
                      valueToScalar, display, default_values, inherited_values,
                      spatial_values, color_values, range_select, -1);
      if (arrays != null) {
        for (int i=0; i<arrays.length; i++) {
          array = arrays[i];
          if (array != null) {
            shadow_api.addToGroup(group, array, mode,
                                  constant_alpha, constant_color);
/*
            geometry = display.makeGeometry(array);
            appearance = makeAppearance(mode, null, constant_color, geometry);
            shape = new Shape3D(geometry, appearance);
            group.addChild(shape);
*/
            if (renderer.getIsDirectManipulation()) {
              renderer.setSpatialValues(spatial_values);
            }
          }
        }
        anyShapeCreated = true;
      }

      boolean anyTextCreated = false;
      if (text_value != null && text_control != null) {
        String[] text_values = {text_value};
        array = makeText(text_values, text_control, spatial_values,
                         color_values, range_select);
        shadow_api.addToGroup(group, array, mode,
                              constant_alpha, constant_color);
/*
        if (array != null) {
          if (array.vertexCount > 0) {
            geometry = display.makeGeometry(array);
            appearance = makeAppearance(mode, null, constant_color, geometry);
            shape = new Shape3D(geometry, appearance);
            group.addChild(shape);
          }
        }
*/
        anyTextCreated = true;
      }

      boolean anyFlowCreated = false;
      // try Flow1
      arrays = shadow_api.makeFlow(0, flow1_values, flowScale[0], spatial_values,
                       color_values, range_select);
      if (arrays != null) {
        for (int i=0; i<arrays.length; i++) {
          if (arrays[i] != null) {
            shadow_api.addToGroup(group, arrays[i], mode,
                                  constant_alpha, constant_color);
/*
            if (arrays[i].vertexCount > 0) {
              geometry = display.makeGeometry(arrays[i]);
              appearance = makeAppearance(mode, null, constant_color, geometry);
              shape = new Shape3D(geometry, appearance);
              group.addChild(shape);
            }
*/
          }
        }
        anyFlowCreated = true;
      }
      // try Flow2
      arrays = shadow_api.makeFlow(1, flow2_values, flowScale[1], spatial_values,
                       color_values, range_select);
      if (arrays != null) {
        for (int i=0; i<arrays.length; i++) {
          if (arrays[i] != null) {
            shadow_api.addToGroup(group, arrays[i], mode,
                                  constant_alpha, constant_color);
/*
            if (arrays[i].vertexCount > 0) {
              geometry = display.makeGeometry(arrays[i]);
              appearance = makeAppearance(mode, null, constant_color, geometry);
              shape = new Shape3D(geometry, appearance);
              group.addChild(shape);
            }
*/
          }
        }
        anyFlowCreated = true;
      }

      if (!anyFlowCreated && !anyTextCreated && !anyShapeCreated) {
        array = makePointGeometry(spatial_values, null);
        if (array != null && array.vertexCount > 0) {
          shadow_api.addToGroup(group, array, mode,
                                constant_alpha, constant_color);
/*
          geometry = display.makeGeometry(array);
          appearance = makeAppearance(mode, null, constant_color, geometry);
          shape = new Shape3D(geometry, appearance);
          group.addChild(shape);
*/
          if (renderer.getIsDirectManipulation()) {
            renderer.setSpatialValues(spatial_values);
          }
        }
      }
      return false;
    }
    else { // if (!(LevelOfDifficulty == SIMPLE_TUPLE))
      // must be LevelOfDifficulty == LEGAL
      // add values to value_array according to SelectedMapVector-s
      // of RealType-s in components (including Reference)
      //
      // accumulate Vector of value_array-s at this ShadowTypeJ3D,
 
      // to be rendered in a post-process to scanning data
      throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                       "ShadowType.terminalTupleOrReal");
    }
  }

  public int textureWidth(int data_width) {
    return data_width;
  }

  public int textureHeight(int data_height) {
    return data_height;
  }

  public void adjustZ(float[] coordinates) {
  }

  public void setTexCoords(float[] texCoords, float ratiow, float ratioh) {
  }

  public Vector getTextMaps(int i, int[] textIndices) {
    return new Vector();
  }

  public boolean addToGroup(Object group, VisADGeometryArray array,
                            GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color)
         throws VisADException {
    return false;
  }

  public void textureToGroup(Object group, VisADGeometryArray array,
                            BufferedImage image, GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color,
                            int texture_width, int texture_height)
         throws VisADException {
  }

  public Object makeSwitch() {
    return null;
  }

  public Object makeBranch() {
    return null;
  }

  public void addToSwitch(Object swit, Object branch)
         throws VisADException {
  }

  public void addSwitch(Object group, Object swit, Control control,
                        Set domain_set, DataRenderer renderer)
         throws VisADException {
  }

  public boolean recurseRange(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    return false;
  }

  public boolean recurseComponent(int i, Object group, Data data,
             float[] value_array, float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {
    return false;
  }

  public boolean wantIndexed() {
    return false;
  }

  public TextControl getParentTextControl() {
    return null;
  }

  public String getParentText() {
    return null;
  }

  public void setText(String text, TextControl control) {
  }

  public String toString() {
    return getClass() + " for \n  " + Type.toString();
    // return " LevelOfDifficulty = " + LevelOfDifficulty;
  }

}

