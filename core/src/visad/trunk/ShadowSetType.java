
//
// ShadowSetType.java
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
   The ShadowSetType class shadows the SetType class,
   within a DataDisplayLink.<P>
*/
public class ShadowSetType extends ShadowType {

  private ShadowRealTupleType Domain; 

  private Vector AccumulationVector = new Vector();

  ShadowSetType(MathType t, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(t, link, parent);
    Domain = (ShadowRealTupleType)
                  ((SetType) Type).getDomain().buildShadowType(Link, this);
    MultipleDisplayScalar = Domain.getMultipleDisplayScalar();
    MappedDisplayScalar = Domain.getMappedDisplayScalar();
  }

  int checkIndices(int[] indices, int[] display_indices, int[] value_indices,
                   boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from Domain
    int[] local_indices = Domain.sumIndices(indices);
    int[] local_display_indices = Domain.sumDisplayIndices(display_indices);
    int[] local_value_indices = Domain.sumValueIndices(value_indices);

    if (Domain.testTransform()) Domain.markTransform(isTransform);

    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(local_display_indices)) {
        levelOfDifficulty = NESTED;
      }
    }

    // test legality of Animation and SelectValue in Domain
    if (Domain.getDimension() != 1 &&
        checkAnimationOrValue(Domain.getDisplayIndices())) {
      throw new BadMappingException("ShadowSetType.checkIndices: " +
                                    "Animation and SelectValue may only occur " +
                                    "in 1-D Set domain");
    }

    // ShadowSetType is implicitly Flat
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
    Rtype = R0; // implicit - Set has no range
 
    if (LevelOfDifficulty == NESTED) {
      if (Dtype != Dbad) {
        LevelOfDifficulty = SIMPLE_FIELD;
      }
      else {
        LevelOfDifficulty = LEGAL;
      }
    }
    return LevelOfDifficulty;
  }

  ShadowRealTupleType getDomain() {
    return Domain;
  }

  /** clear AccumulationVector */
  public void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
  }

  /** transform data into a Java3D scene graph;
      return true if need post-process */
  public boolean doTransform(Group group, Data data, double[] value_array)
         throws VisADException {
    if (isTerminal) {
      if (LevelOfDifficulty == LEGAL) {
        // add values to value_array according to SelectedMapVector-s
        // of RealType-s in Domain (including Reference)
        //
        // accumulate Vector of value_array-s at this ShadowType,
        // to be rendered in a post-process to scanning data
/*
        return true;
*/
        throw new UnimplementedException("ShadowSetType.doTransform: " +
                                         "terminal LEGAL");
      }
      else {
        // must be LevelOfDifficulty == SIMPLE_FIELD
        // only manage Spatial, Color and Alpha here
        // (account for Domain Reference)
/*
        Group data_group = null;
        group.addChild(data_group);
*/
        throw new UnimplementedException("ShadowSetType.doTransform: " +
                                         "terminal SIMPLE_FIELD");
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
        throw new UnimplementedException("ShadowSetType.postProcess: " +
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

