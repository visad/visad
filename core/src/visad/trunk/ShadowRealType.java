
//
// ShadowRealType.java
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
   The ShadowRealType class shadows the RealType class,
   within a DataDisplayLink.<P>
*/
public class ShadowRealType extends ShadowType {

  // index into Display.RealTypeVector, or -1
  int Index;

  // Vector of ScalarMap-s applying to this RealType
  private Vector SelectedMapVector;

  // set DisplaySpatialTuple if this is mapped to a
  // component of Display.DisplaySpatialCartesianTuple
  // or to a component of a DisplayTupleType whose
  // Reference is Display.DisplaySpatialCartesianTuple
  // implicit anySpatial = (DisplaySpatialTuple != null)
  //
  // if this is mapped to multiple components of DisplaySpatialTuple
  // then DisplaySpatialTuple != null && DisplaySpatialTupleIndexIndex > 1
  //
  // if this is mapped to components of different spatial DisplayTupleType-s
  // then throw BadMappingException if mapped to components of different
  private DisplayTupleType DisplaySpatialTuple;
  // index in DisplaySpatialTuple
  private int[] DisplaySpatialTupleIndex;
  private int DisplaySpatialTupleIndexIndex;

  private Vector AccumulationVector = new Vector();

  ShadowRealType(MathType type, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(type, link, parent);

    DisplaySpatialTuple = null;
    DisplaySpatialTupleIndex = new int[3];
    for (int k=0; k<3; k++) DisplaySpatialTupleIndex[k] = -1;
    DisplaySpatialTupleIndexIndex = 0;

    Index = -1;
    SelectedMapVector = new Vector();
    Enumeration maps = display.getMapVector().elements();
    // determine which ScalarMap-s apply to this ShadowRealType
    while(maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      if (map.getScalar().equals(Type)) {
        Index = map.getScalarIndex();
        SelectedMapVector.addElement(map);
        Link.addSelectedMapVector(map);
        DisplayIndices[map.getDisplayScalarIndex()]++;
        ValueIndices[map.getValueIndex()]++;
        // could test here for any DisplayIndices[.] > 1
        // i.e., multiple maps between same RealType and DisplayRealType
        // actually tested in DisplayImpl.addMap
        //
        // compute DisplaySpatialTuple and DisplaySpatialTupleIndex
        DisplayTupleType tuple =
          (DisplayTupleType) map.getDisplayScalar().getTuple();
        if (tuple != null &&
            tuple != Display.DisplaySpatialCartesianTuple) {
          CoordinateSystem coord_sys = tuple.getCoordinateSystem();
          if (coord_sys == null ||
              !coord_sys.getReference().equals(
               Display.DisplaySpatialCartesianTuple)) {
            tuple = null; // tuple not spatial, no worries
          }
        }
        if (tuple != null) {
          if (DisplaySpatialTuple != null) {
            if (!tuple.equals(DisplaySpatialTuple)) {
              // this mapped to multiple spatial DisplayTupleType-s
              DisplayRealType real = (DisplayRealType) Type;
              throw new BadMappingException("ShadowRealType: " + real.getName() +
                         " mapped to multiple spatial DisplayTupleType-s");
            }
          }
          else { // DisplaySpatialTuple == null
            DisplaySpatialTuple = tuple;
          }
          DisplaySpatialTupleIndex[DisplaySpatialTupleIndexIndex] = 
            map.getDisplayScalar().getTupleIndex();
          DisplaySpatialTupleIndexIndex++;
        }
      } // end if (map.Scalar.equals(Type)) {
    } // end while(maps.hasMoreElements()) {
    MultipleDisplayScalar = (SelectedMapVector.size() > 1);
    MappedDisplayScalar = (SelectedMapVector.size() > 0);
  }

  /** increment indices for 'shadowed' RealType */
  void incrementIndices(int[] indices) {
    // this test allows multiple occurences of a Scalar
    // as long as it is not mapped
    if (MappedDisplayScalar) indices[Index]++;
  }

  /** increment indices for ShadowRealType
      and then test as possible terminal node */
  int checkIndices(int[] indices, int[] display_indices, int[] value_indices,
                   boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from RealType
    int[] local_indices = copyIndices(indices);
    incrementIndices(local_indices);
    int[] local_display_indices = addIndices(display_indices, DisplayIndices);
    int[] local_value_indices = addIndices(value_indices, ValueIndices);

    markTransform(isTransform);

    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(DisplayIndices)) {
        levelOfDifficulty = NESTED;
      }
    }

    // test legality of Animation and SelectValue
    if (checkAnimationOrValue(DisplayIndices)) {
      throw new BadMappingException("ShadowRealType.checkIndices: " +
                                    "Animation and SelectValue may not " +
                                    "occur in range");
    }

    LevelOfDifficulty =
      testIndices(local_indices, local_display_indices, levelOfDifficulty);
    if (LevelOfDifficulty == NESTED) {
      if (checkR2D2(DisplayIndices)) {
        LevelOfDifficulty = SIMPLE_TUPLE;
      }
      else {
        LevelOfDifficulty = LEGAL;
      }
    }
    return LevelOfDifficulty;
  }

  public boolean getMappedDisplayScalar() {
    return MappedDisplayScalar;
  }

  public DisplayTupleType getDisplaySpatialTuple() {
    return DisplaySpatialTuple;
  }

  public int[] getDisplaySpatialTupleIndex() {
    return DisplaySpatialTupleIndex;
  }

  public int getIndex() {
    return Index;
  }

  Vector getSelectedMapVector() {
    return SelectedMapVector;
  }

  /** mark Control-s as needing re-Transform */
  void markTransform(boolean[] isTransform) {
    Enumeration maps = SelectedMapVector.elements();
    while(maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
      DisplayRealType real = map.getDisplayScalar();
      if (real.equals(Display.Animation) ||
          real.equals(Display.SelectValue) ||
          real.equals(Display.SelectRange)) {
        Control control = map.getControl();
        if (control != null) {
          isTransform[control.getIndex()] = true;
        }
      }
    }
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
  }

  /** transform data into a Java3D scene graph;
      return true if need post-process */
  public boolean doTransform(Group group, Data data, double[] value_array)
         throws VisADException {
    // add values to value_array according to SelectedMapVector
    if (isTerminal) {
      // cannot be any Reference when RealType is terminal
      if (LevelOfDifficulty == LEGAL) {
        // accumulate Vector of value_array-s at this ShadowType,
        // to be rendered in a post-process to scanning data
/*
        return true;
*/
        throw new UnimplementedException("ShadowRealType.doTransform: " +
                                         "terminal LEGAL");
      }
      else {
        // must be LevelOfDifficulty == SIMPLE_TUPLE
        // only manage Spatial, Color and Alpha here
        // i.e., the 'dots'
/*
        Group data_group = null;
        group.addChild(data_group);
*/
        throw new UnimplementedException("ShadowRealType.doTransform: " +
                                         "terminal SIMPLE_TUPLE");
      }
    }
    else {
      // nothing to render at a non-terminal RealType
    }
    return false;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Group group) throws VisADException {
    if (isTerminal) {
      if (LevelOfDifficulty == LEGAL) {
/*
        Group data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("ShadowRealType.postProcess: " +
                                         "terminal LEGAL");
      }
      else {
        // nothing to do
      }
    }
    else {
      // nothing to do
    }
    AccumulationVector.removeAllElements();
  }

}

