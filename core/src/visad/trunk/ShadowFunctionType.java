
//
// ShadowFunctionType.java
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
   The ShadowFunctionType class shadows the FunctionType class,
   within a DataDisplayLink.<P>
*/
public class ShadowFunctionType extends ShadowFunctionOrSetType {

  ShadowFunctionType(MathType t, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(t, link, parent);
    Domain = (ShadowRealTupleType)
             ((FunctionType) Type).getDomain().buildShadowType(link, this);
    Range = ((FunctionType) Type).getRange().buildShadowType(link, this);
    Flat = (Range instanceof ShadowRealType) ||
           (Range instanceof ShadowTupleType && ((ShadowTupleType) Range).isFlat());
    MultipleDisplayScalar = Domain.getMultipleDisplayScalar() ||
                            Range.getMultipleDisplayScalar();
    MappedDisplayScalar = Domain.getMappedDisplayScalar() ||
                            Range.getMappedDisplayScalar();
    RangeComponents = getComponents(Range, true);
    DomainComponents = getComponents(Domain, false);
    DomainReferenceComponents = getComponents(Domain.getReference(), false);
  }

  /** used by FlatField.computeRanges */
  int[] getRangeDisplayIndices() {
    int n = RangeComponents.length;
    int[] indices = new int[n];
    for (int i=0; i<n; i++) {
      indices[i] = RangeComponents[i].getIndex();
    }
    return indices;
  }

  /** checkIndices:
    if (Flat) {
      terminal, so check condition 4
    }
    else {
      check condition 2 on domain
      pass levelOfDifficulty down to range.checkIndices
    }
  */
  int checkIndices(int[] indices, int[] display_indices, int[] value_indices,
                   boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from Domain
    int[] local_indices = Domain.sumIndices(indices);
    int[] local_display_indices = Domain.sumDisplayIndices(display_indices);
    int[] local_value_indices = Domain.sumValueIndices(value_indices);

    if (Domain.testTransform()) Domain.markTransform(isTransform);
    Range.markTransform(isTransform);

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
        throw new BadMappingException("ShadowFunctionType.checkIndices: " +
                                    "Animation and SelectValue may only occur " +
                                    "in 1-D Function domain");
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
    }

    if (Flat) {
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
      anyContour = checkContour(local_display_indices);
      anyFlow = checkFlow(local_display_indices);

      // test legality of Animation and SelectValue in Range
      if (checkAnimationOrValue(Range.getDisplayIndices()) > 0) {
        throw new BadMappingException("ShadowFunctionType.checkIndices: " +
                                      "Animation and SelectValue may not " +
                                      "occur in Function range");
      }

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

      if (LevelOfDifficulty == NESTED) {
        if (Dtype != Dbad && Rtype != Rbad) {
          LevelOfDifficulty = SIMPLE_FIELD;
        }
        else {
          LevelOfDifficulty = LEGAL;
        }
      }
    }
    else { // !Flat
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

  void checkDirect(Data data) throws VisADException, RemoteException {
    // there is some redundancy among these conditions
    if (!((FunctionType) Type).getReal()) {
      whyNotDirect = notRealFunction;
      return;
    }
    else if (LevelOfDifficulty != SIMPLE_FIELD) {
      whyNotDirect = notSimpleTuple;
      return;
    }
    else if (MultipleDisplayScalar) {
      whyNotDirect = multipleMapping;
      return;
    }
    else if (Domain.getDimension() != 1) {
      whyNotDirect = domainDimension;
      return;
    }
    else if(!(Display.DisplaySpatialCartesianTuple.equals(
                         Domain.getDisplaySpatialTuple() ) ) ) {
      whyNotDirect = domainNotSpatial;
      return;
    }
    else if (Domain.getSpatialReference()) {
      whyNotDirect = viaReference;
      return;
    }
    DisplayTupleType tuple = null;
    if (Range instanceof ShadowRealTupleType) {
      tuple = ((ShadowRealTupleType) Range).getDisplaySpatialTuple();
    }
    else if (Range instanceof ShadowRealType) {
      tuple = ((ShadowRealType) Range).getDisplaySpatialTuple();
    }
    else {
      whyNotDirect = rangeType;
      return;
    }
    if (!Display.DisplaySpatialCartesianTuple.equals(tuple)) {
      whyNotDirect = rangeNotSpatial;
      return;
    }
    else if (Range instanceof ShadowRealTupleType &&
             ((ShadowRealTupleType) Range).getSpatialReference()) {
      whyNotDirect = viaReference;
      return;
    }
/* WLH 25 Dec 97
    else if(data.isMissing()) {
      whyNotDirect = dataMissing;
      return;
    }
*/
    else if (!(((Field) data).getDomainSet() instanceof Gridded1DSet)) {
      whyNotDirect = domainSet;
      return;
    }
    isDirectManipulation = true;

    domainAxis = -1;
    for (int i=0; i<3; i++) {
      axisToComponent[i] = -1;
      directMap[i] = null;
    }
 
    directManifoldDimension =
      setDirectMap((ShadowRealType) Domain.getComponent(0), -1, true);
    if (Range instanceof ShadowRealType) {
      directManifoldDimension +=
        setDirectMap((ShadowRealType) Range, 0, false);
    }
    else if (Range instanceof ShadowRealTupleType) {
      ShadowRealTupleType range = (ShadowRealTupleType) Range;
      for (int i=0; i<range.getDimension(); i++) {
        directManifoldDimension +=
          setDirectMap((ShadowRealType) range.getComponent(i), i, false);
      }
    }

    if (domainAxis == -1) {
      throw new DisplayException("ShadowFunctionType.checkDirect:" +
                                 "too few spatial domain");
    }
    if (directManifoldDimension < 2) {
      throw new DisplayException("ShadowFunctionType.checkDirect:" +
                                 "directManifoldDimension < 2");
    }
  }

  public ShadowType getRange() {
    return Range;
  }

  /** mark Control-s as needing re-Transform */
  void markTransform(boolean[] isTransform) {
    Range.markTransform(isTransform);
  }

}

