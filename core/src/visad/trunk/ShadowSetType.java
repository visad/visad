
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
public class ShadowSetType extends ShadowFunctionOrSetType {

  ShadowSetType(MathType t, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(t, link, parent);
    Domain = (ShadowRealTupleType)
                  ((SetType) Type).getDomain().buildShadowType(Link, this);
    Range = null;
    Flat = true;
    MultipleDisplayScalar = Domain.getMultipleDisplayScalar();
    MappedDisplayScalar = Domain.getMappedDisplayScalar();
    RangeComponents = null;
    DomainComponents = getComponents(Domain, false);
    DomainReferenceComponents = getComponents(Domain.getReference(), false);
  }

  int checkIndices(int[] indices, int[] display_indices, int[] value_indices,
                   boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from Domain
    int[] local_indices = Domain.sumIndices(indices);
    int[] local_display_indices = Domain.sumDisplayIndices(display_indices);
    int[] local_value_indices = Domain.sumValueIndices(value_indices);

    if (Domain.testTransform()) Domain.markTransform(isTransform);

    // get value_indices arrays used by doTransform
    inherited_values = copyIndices(value_indices);

    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(local_display_indices)) {
        levelOfDifficulty = NESTED;
      }
    }

    // test legality of Animation and SelectValue in Domain
    int avCount = checkAnimationOrValue(Domain.getDisplayIndices());
    if (Domain.getDimension() != 1) {
      if (avCount > 0) {
        throw new BadMappingException("ShadowSetType.checkIndices: " +
                                    "Animation and SelectValue may only occur " +
                                    "in 1-D Set domain");
      }
    }
    else {
      // eventually ShadowType.testTransform is used to mark Animation,
      // Value or Range as isTransform when multiple occur in Domain;
      // however, temporary hack in Renderer.isTransformControl requires 
      // multiple occurence of Animation and Value to throw an Exception
      if (avCount > 1) {
        throw new BadMappingException("ShadowSetType.checkIndices: " +
                                    "only one Animation and SelectValue may " +
                                    "occur Set domain");
      }
    }

    anyContour = checkContour(local_display_indices);
    anyFlow = checkFlow(local_display_indices);

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

}

