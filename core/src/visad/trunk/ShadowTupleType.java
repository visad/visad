
//
// ShadowTupleType.java
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
   The ShadowTupleType class shadows the TupleType class,
   within a DataDisplayLink.<P>
*/
public class ShadowTupleType extends ShadowType {

  ShadowType[] tupleComponents;

  private Vector AccumulationVector = new Vector();

  /** true if no component with mapped Scalar components is a
      ShadowSetType, ShadowFunctionType or ShadowTupleType;
      not the same as TupleType.Flat */
  private boolean Flat;

  ShadowTupleType(MathType t, DataDisplayLink link, ShadowType parent)
                  throws VisADException, RemoteException {
    super(t, link, parent);
    int n = ((TupleType) t).getDimension();
    tupleComponents = new ShadowType[n];
    Flat = true;
    ShadowType[] components = new ShadowType[n];
    MultipleDisplayScalar = false;
    // compute Flat, DisplayIndices and ValueIndices
    for (int i=0; i<n; i++) {
      ShadowType shadow =
        ((TupleType) Type).getComponent(i).buildShadowType(Link, this);
      tupleComponents[i] = shadow;
      MultipleDisplayScalar |= tupleComponents[i].getMultipleDisplayScalar();
      boolean mappedComponent = tupleComponents[i].getMappedDisplayScalar();
      MappedDisplayScalar |= mappedComponent;
      if (shadow instanceof ShadowFunctionType ||
          shadow instanceof ShadowSetType ||
          (shadow instanceof ShadowTupleType &&
           !(shadow instanceof ShadowRealTupleType)) ) {
        if (mappedComponent) Flat = false;
      }
      else if (shadow instanceof ShadowRealType ||
               shadow instanceof ShadowRealTupleType) {
        // treat ShadowRealTupleType component as
        // a set of ShadowRealType components
        DisplayIndices = addIndices(DisplayIndices, shadow.getDisplayIndices());
        ValueIndices = addIndices(ValueIndices, shadow.getValueIndices());
      }
    }
  }

  // copy and increment indices for each ShadowRealType component and
  // each ShadowRealType component of a ShadowRealTupleType component
  int[] sumIndices(int[] indices) {
    int[] local_indices = copyIndices(indices);
    int n = tupleComponents.length;
    for (int j=0; j<n; j++) {
      ShadowType shadow = (ShadowType) tupleComponents[j];
      if (shadow instanceof ShadowRealType) {
        ((ShadowRealType) shadow).incrementIndices(local_indices);
      }
      else if (shadow instanceof ShadowRealTupleType) {
        // treat ShadowRealTupleType component as
        // a set of ShadowRealType components
        local_indices =
          ((ShadowRealTupleType) shadow).sumIndices(local_indices);
      }
    }
    return local_indices;
  }

  /** add DisplayIndices (from each ShadowRealType and
      ShadowRealTupleType component) */
  int[] sumDisplayIndices(int[] display_indices) throws VisADException {
    return addIndices(display_indices, DisplayIndices);
  }

  /** add ValueIndices (from each ShadowRealType and
      ShadowRealTupleType component) */
  int[] sumValueIndices(int[] value_indices) throws VisADException {
    return addIndices(value_indices, ValueIndices);
  }

  /*
    if (Flat) {
      terminal, so check condition 5
    }
    else {
      check condition 2 on Flat components
      pass levelOfDifficulty down to checkIndices on non-Flat components
    }
  */
  int checkIndices(int[] indices, int[] display_indices, int[] value_indices,
                   boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from RealType and
    // RealTupleType components
    int[] local_indices = sumIndices(indices);
    int[] local_display_indices = sumDisplayIndices(display_indices);
    int[] local_value_indices = sumValueIndices(value_indices);

    markTransform(isTransform);

    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(DisplayIndices)) {
        levelOfDifficulty = NESTED;
      }
    }

    if (Flat) {
      // test legality of Animation and SelectValue
      if (checkAnimationOrValue(DisplayIndices)) {
        throw new BadMappingException("ShadowTupleType.checkIndices: " +
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
    }
    else { // !Flat
      if (levelOfDifficulty == NESTED) {
        if (!checkNested(DisplayIndices)) {
          levelOfDifficulty = LEGAL;
        }
      }
      int minLevelOfDifficulty = ShadowType.NOTHING_MAPPED;
      for (int j=0; j<tupleComponents.length; j++) {
        ShadowType shadow = (ShadowType) tupleComponents[j];
        // treat ShadowRealTupleType component as a set of
        // ShadowRealType components (i.e., not terminal)
        if (shadow instanceof ShadowFunctionType ||
            shadow instanceof ShadowSetType ||
            (shadow instanceof ShadowTupleType &&
             !(shadow instanceof ShadowRealTupleType)) ) {
          int level = shadow.checkIndices(local_indices, local_display_indices,
                                          local_value_indices, isTransform,
                                          levelOfDifficulty);
          if (level < minLevelOfDifficulty) {
            minLevelOfDifficulty = level;
          }
        }
      }
      LevelOfDifficulty = minLevelOfDifficulty;
    }
    return LevelOfDifficulty;
  }

  /** get number of components */
  public int getDimension() {
    return tupleComponents.length;
  }

  public ShadowType getComponent(int i) {
    return tupleComponents[i];
  }

  /** mark Control-s as needing re-Transform */
  void markTransform(boolean[] isTransform) {
    for (int i=0; i<tupleComponents.length; i++) {
      tupleComponents[i].markTransform(isTransform);
    }
  }

  boolean isFlat() {
    return Flat;
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
/*
    for (int i=0; i<num_components; i++) {
      component_type.preProcess();
    }
*/
  }

  /** transform data into a Java3D scene graph;
      return true if need post-process */
  public boolean doTransform(Group group, Data data, double[] value_array)
         throws VisADException {
    if (isTerminal) {
      if (LevelOfDifficulty == LEGAL) {
        // add values to value_array according to SelectedMapVector-s
        // of RealType-s in components (including Reference)
        //
        // accumulate Vector of value_array-s at this ShadowType,
        // to be rendered in a post-process to scanning data
/*
        return true;
*/
        throw new UnimplementedException("ShadowTupleType.doTransform: " +
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
        throw new UnimplementedException("ShadowTupleType.doTransform: " +
                                         "terminal SIMPLE_TUPLE");
      }
    }
    else {
      boolean post = false;
      // add values to value_array according to SelectedMapVector-s
      // of RealType-s in components (including Reference), and
      // recursively call doTransform on other components
/*
      for (int i=0; i<num_components; i++) {
        post |= component_type.doTransform(group, component_data, value_array);
      }
      return post;
*/
      throw new UnimplementedException("ShadowTupleType.doTransform: " +
                                       "not terminal");
    }
/*
    return false;
*/
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
        throw new UnimplementedException("ShadowTupleType.postProcess: " +
                                         "terminal LEGAL");
      }
      else {
        // nothing to do
      }
    }
    else {
/*
      for (int i=0; i<num_components; i++) {
        component_type.postProcess(group);
      }
*/
    }
    AccumulationVector.removeAllElements();
  }

}

