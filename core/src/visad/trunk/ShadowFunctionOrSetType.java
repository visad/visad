
//
// ShadowFunctionOrSetType.java
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

import java.util.*;
import java.rmi.*;

/**
   The ShadowFunctionOrSetType class is an abstract parent for
   classes that implement ShadowFunctionType or ShadowSetType.<P>
*/
public abstract class ShadowFunctionOrSetType extends ShadowType {

  ShadowRealTupleType Domain;
  ShadowType Range; // null for ShadowSetType

  /** this is an array of ShadowRealType-s that are ShadowRealType
      components of Range or ShadowRealType components of
      ShadowRealTupleType components of Range;
      a non-ShadowRealType and non-ShadowTupleType Range is marked
      by null;
      components of a ShadowTupleType Range that are neither
      ShadowRealType nor ShadowRealTupleType are ignored */
  ShadowRealType[] RangeComponents; // null for ShadowSetType
  ShadowRealType[] DomainComponents;
  ShadowRealType[] DomainReferenceComponents;
 
  /** true if range is ShadowRealType or Flat ShadowTupleType
      not the same as FunctionType.Flat;
      also true for ShadowSetType */
  boolean Flat;

  /** value_indices from parent */
  int[] inherited_values;

  /** this constructor is a bit of a kludge to get around
      single inheritance problems */
  public ShadowFunctionOrSetType(MathType t, DataDisplayLink link, ShadowType parent,
                                 ShadowRealTupleType domain, ShadowType range)
      throws VisADException, RemoteException {
    super(t, link, parent);
    Domain = domain;
    Range = range;
    if (this instanceof ShadowFunctionType) {
      Flat = (Range instanceof ShadowRealType) ||
             (Range instanceof ShadowTupleType &&
              ((ShadowTupleType) Range).isFlat());
      MultipleDisplayScalar = Domain.getMultipleDisplayScalar() ||
                              Range.getMultipleDisplayScalar();
      MappedDisplayScalar = Domain.getMappedDisplayScalar() ||
                              Range.getMappedDisplayScalar();
      RangeComponents = getComponents(Range, true);
    }
    else if (this instanceof ShadowSetType) {
      Flat = true;
      MultipleDisplayScalar = Domain.getMultipleDisplayScalar();
      MappedDisplayScalar = Domain.getMappedDisplayScalar();
      RangeComponents = null;
    }
    else {
      throw new DisplayException("ShadowFunctionOrSetType: must be " +
                                 "ShadowFunctionType or ShadowSetType");
    }
    DomainComponents = getComponents(Domain, false);
    DomainReferenceComponents = getComponents(Domain.getReference(), false);
  }

  public boolean getAnyContour() {
    return anyContour;
  }

  public boolean getAnyFlow() {
    return anyFlow;
  }

  public boolean getFlat() {
    return Flat;
  }

  public ShadowRealType[] getRangeComponents() {
    return RangeComponents;
  }

  public ShadowRealType[] getDomainComponents() {
    return DomainComponents;
  }

  public ShadowRealType[] getDomainReferenceComponents() {
    return DomainReferenceComponents;
  }

  /** used by FlatField.computeRanges */
  int[] getRangeDisplayIndices() throws VisADException {
    if (!(this instanceof ShadowFunctionType)) {
      throw new DisplayException("ShadowFunctionOrSetType.getRangeDisplay" +
                                 "Indices: must be ShadowFunctionType");
    }
    int n = RangeComponents.length;
    int[] indices = new int[n];
    for (int i=0; i<n; i++) {
      indices[i] = RangeComponents[i].getIndex();
    }
    return indices;
  }

  public int[] getInheritedValues() {
    return inherited_values;
  }

  /** checkIndices: check for rendering difficulty, etc */
  public int checkIndices(int[] indices, int[] display_indices,
             int[] value_indices, boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from Domain
    int[] local_indices = Domain.sumIndices(indices);
    int[] local_display_indices = Domain.sumDisplayIndices(display_indices);
    int[] local_value_indices = Domain.sumValueIndices(value_indices);

    if (Domain.testTransform()) Domain.markTransform(isTransform);
    if (this instanceof ShadowFunctionType) {
      Range.markTransform(isTransform);
    }

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
        throw new BadMappingException("ShadowFunctionOrSetType.checkIndices: " +
                                    "Animation and SelectValue may only occur " +
                                    "in 1-D Function domain");
      }
      else {
        // eventually ShadowType.testTransform is used to mark Animation,
        // Value or Range as isTransform when multiple occur in Domain;
        // however, temporary hack in Renderer.isTransformControl requires
        // multiple occurence of Animation and Value to throw an Exception
        if (avCount > 1) {
          throw new BadMappingException("ShadowFunctionOrSetType.checkIndices: " +
                                    "only one Animation and SelectValue may " +
                                    "occur Set domain");
        }
      }
    }

    if (Flat || this instanceof ShadowSetType) {
      if (this instanceof ShadowFunctionType) {
        if (Range instanceof ShadowTupleType) {
          local_indices =
            ((ShadowTupleType) Range).sumIndices(local_indices);
          local_display_indices =
            ((ShadowTupleType) Range).sumDisplayIndices(local_display_indices);
          local_value_indices =
            ((ShadowTupleType) Range).sumValueIndices(local_value_indices);
        }
        else if (Range instanceof ShadowRealType) {
          ((ShadowRealType) Range).incrementIndices(local_indices);
          local_display_indices = addIndices(local_display_indices, 
            ((ShadowRealType) Range).getDisplayIndices());
          local_value_indices = addIndices(local_value_indices,
            ((ShadowRealType) Range).getValueIndices());
        }

        // test legality of Animation and SelectValue in Range
        if (checkAnimationOrValue(Range.getDisplayIndices()) > 0) {
          throw new BadMappingException("ShadowFunctionOrSetTypeJ3D.checkIndices: " +
                                        "Animation and SelectValue may not " +
                                        "occur in Function range");
        }
      }
      anyContour = checkContour(local_display_indices);
      anyFlow = checkFlow(local_display_indices);

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

      if (this instanceof ShadowFunctionType) {
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
      }
      else { // this instanceof ShadowSetType
        Rtype = R0; // implicit - Set has no range
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
    else { // !Flat && this instanceof ShadowFunctionType
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

  public ShadowRealTupleType getDomain() {
    return Domain;
  }

  public ShadowType getRange() {
    return Range;
  }

  /** mark Control-s as needing re-Transform */
  void markTransform(boolean[] isTransform) {
    if (Range != null) Range.markTransform(isTransform);
  }

}

