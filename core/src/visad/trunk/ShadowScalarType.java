//
// ShadowScalarType.java
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

package visad;

import java.util.*;
import java.rmi.*;

/**
   The ShadowScalarType class shadows the ScalarType class,
   within a DataDisplayLink.<P>
*/
public class ShadowScalarType extends ShadowType {

  // index into Display.RealTypeVector, or -1
  int Index;

  // Vector of ScalarMap-s applying to this ScalarType
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

  /** value_indices from parent */
  private int[] inherited_values;

  public ShadowScalarType(MathType type, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(type, link, parent);

    DisplaySpatialTuple = null;
    DisplaySpatialTupleIndex = new int[3];
    for (int k=0; k<3; k++) DisplaySpatialTupleIndex[k] = -1;
    DisplaySpatialTupleIndexIndex = 0;

    int spatial_count = 0;
    Index = -1;
    SelectedMapVector = new Vector();
    Enumeration maps = display.getMapVector().elements();
    // determine which ScalarMap-s apply to this ShadowScalarType
    while(maps.hasMoreElements()) {
      ScalarMap map = (ScalarMap) maps.nextElement();
/*
System.out.println("map: " + map.getScalar().getName() + " -> " +
                   map.getDisplayScalar().getName());
*/
      if (map.getScalar().equals(Type)) {
/*
System.out.println("Type = " + ((ScalarType) Type).getName() +
                   " DisplayIndex = " + map.getDisplayScalarIndex());
*/
        Index = map.getScalarIndex();
        SelectedMapVector.addElement(map);
        Link.addSelectedMapVector(map);
        DisplayIndices[map.getDisplayScalarIndex()]++;
        ValueIndices[map.getValueIndex()]++;
        // could test here for any DisplayIndices[.] > 1
        // i.e., multiple maps between same ScalarType and DisplayRealType
        // actually tested in DisplayImpl.addMap
        //
        // compute DisplaySpatialTuple and DisplaySpatialTupleIndex
        DisplayTupleType tuple =
          (DisplayTupleType) map.getDisplayScalar().getTuple();
        if (tuple != null &&
            !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
          CoordinateSystem coord_sys = tuple.getCoordinateSystem();
          if (coord_sys == null ||
              !coord_sys.getReference().equals(
               Display.DisplaySpatialCartesianTuple)) {
            tuple = null; // tuple not spatial, no worries
          }
        }
        if (tuple != null) {
          spatial_count++;
          if (DisplaySpatialTuple != null) {
            if (!tuple.equals(DisplaySpatialTuple)) {
              // this mapped to multiple spatial DisplayTupleType-s
              ScalarType real = (ScalarType) Type;
              throw new BadMappingException(real.getName() +
                         " mapped to multiple spatial DisplayTupleType-s: " +
                                            "ShadowScalarType");
            }
          }
          else { // DisplaySpatialTuple == null
            DisplaySpatialTuple = tuple;
          }
          DisplaySpatialTupleIndex[DisplaySpatialTupleIndexIndex] =
            map.getDisplayScalar().getTupleIndex();
          DisplaySpatialTupleIndexIndex++;
        } // end if (tuple != null)
      } // end if (map.Scalar.equals(Type)) {
    } // end while(maps.hasMoreElements()) {
    MultipleSpatialDisplayScalar = (spatial_count > 1);
    MultipleDisplayScalar = (SelectedMapVector.size() > 1);
    MappedDisplayScalar = (SelectedMapVector.size() > 0);
  }

  /** increment indices for 'shadowed' ScalarType */
  void incrementIndices(int[] indices) {
    // this test allows multiple occurences of a Scalar
    // as long as it is not mapped
    if (MappedDisplayScalar && Index < indices.length) indices[Index]++;
  }

  /** increment indices for ShadowScalarType
      and then test as possible terminal node */
  public int checkIndices(int[] indices, int[] display_indices,
             int[] value_indices, boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from ScalarType
    int[] local_indices = copyIndices(indices);
    incrementIndices(local_indices);
    int[] local_display_indices = addIndices(display_indices, DisplayIndices);
    int[] local_value_indices = addIndices(value_indices, ValueIndices);

    markTransform(isTransform);

    // get value_indices arrays used by doTransform
    inherited_values = copyIndices(value_indices);

    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(DisplayIndices)) {
        levelOfDifficulty = NESTED;
      }
    }

    // test legality of Animation and SelectValue
    if (checkAnimationOrValue(DisplayIndices) > 0) {
      throw new BadMappingException("Animation and SelectValue may not " +
                                    "occur in range: " +
                                    "ShadowScalarType.checkIndices");
    }

    anyContour = checkContour(local_display_indices);
    anyFlow = checkFlow(local_display_indices);
    anyShape = checkShape(local_display_indices);
    anyText = checkText(local_display_indices);
    adjustProjectionSeam = getAdjustProjectionSeam();

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

  public int[] getInheritedValues() {
    return inherited_values;
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

  public Vector getSelectedMapVector() {
    return (Vector) SelectedMapVector.clone();
  }

  /** mark Control-s as needing re-Transform */
  void markTransform(boolean[] isTransform) {
    synchronized (SelectedMapVector) {
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
  }

}

