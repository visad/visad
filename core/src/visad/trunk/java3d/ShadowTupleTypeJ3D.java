
//
// ShadowTupleTypeJ3D.java
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

package visad.java3d;
 
import visad.*;

import javax.media.j3d.*;

import java.util.*;
import java.rmi.*;

/**
   The ShadowTupleTypeJ3D class shadows the TupleType class,
   within a DataDisplayLink.<P>
*/
public class ShadowTupleTypeJ3D extends ShadowTypeJ3D {

  ShadowTypeJ3D[] tupleComponents;
  private Vector AccumulationVector = new Vector();

  public ShadowTupleTypeJ3D(MathType t, DataDisplayLink link,
                            ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
    if (this instanceof ShadowRealTupleTypeJ3D) return;

    int n = ((TupleType) t).getDimension();
    tupleComponents = new ShadowTypeJ3D[n];
    ShadowType[] components = new ShadowType[n];
    for (int i=0; i<n; i++) {
      ShadowTypeJ3D shadow = (ShadowTypeJ3D)
        ((TupleType) Type).getComponent(i).buildShadowType(Link, this);
      tupleComponents[i] = shadow;
      components[i] = shadow.getAdaptedShadowType();
    }
    adaptedShadowType =
      new ShadowTupleType(t, link, getAdaptedParent(parent),
                          components);
  }

  /** get number of components */
  public int getDimension() {
    return tupleComponents.length;
  }

  public ShadowTypeJ3D getComponent(int i) {
    return tupleComponents[i];
  }

  boolean isFlat() {
    return ((ShadowTupleType) adaptedShadowType).isFlat();
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
  public boolean doTransform(Group group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {

    if (data.isMissing()) return false;
    int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();
    if (LevelOfDifficulty == NOTHING_MAPPED) return false;

    if (!(data instanceof Tuple)) {
      throw new DisplayException("ShadowTupleTypeJ3D.doTransform: " +
                                 "data must be Tuple");
    }

    // get some precomputed values useful for transform
    // length of ValueArray
    int valueArrayLength = display.getValueArrayLength();
    // mapping from ValueArray to DisplayScalar
    int[] valueToScalar = display.getValueToScalar();
    // mapping from ValueArray to MapVector
    int[] valueToMap = display.getValueToMap();
    Vector MapVector = display.getMapVector();

    // array to hold values for various mappings
    float[][] display_values = new float[valueArrayLength][];

    // get values inherited from parent;
    // assume these do not include SelectRange, SelectValue
    // or Animation values - see temporary hack in
    // DataRenderer.isTransformControl
    int[] inherited_values =
      ((ShadowTupleType) adaptedShadowType).getInheritedValues();
    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0) {
        display_values[i] = new float[1];
        display_values[i][0] = value_array[i];
      }
    }

    Tuple tuple = (Tuple) data;
    RealType[] realComponents = ((TupleType) data.getType()).getRealComponents();
    int length = realComponents.length;
    if (length > 0) {
      double[][] value = new double[length][1];
      Unit[] value_units = new Unit[length];
      int j = 0;
      for (int i=0; i<tuple.getDimension(); i++) {
        Data component = tuple.getComponent(i);
        if (component instanceof Real) {
          value_units[j] = realComponents[j].getDefaultUnit();
          value[j][0] =
            ((Real) component).getValue(value_units[j]);
          j++;
        }
        else if (component instanceof RealTuple) {
          for (int k=0; k<((RealTuple) component).getDimension(); k++) {
            value_units[j] = realComponents[j].getDefaultUnit();
            value[j][0] =
              ((Real) ((RealTuple) component).getComponent(k)).
                                              getValue(value_units[j]);
            j++;
          }
        }
      }
      ShadowRealType[] RealComponents =
        ((ShadowTupleType) adaptedShadowType).getRealComponents();
      mapValues(display_values, value, RealComponents);

      int[] refToComponent = adaptedShadowType.getRefToComponent();
      ShadowRealTupleType[] componentWithRef =
         adaptedShadowType.getComponentWithRef();
      int[] componentIndex = adaptedShadowType.getComponentIndex();

      if (refToComponent != null) {

        // TO_DO

        for (int i=0; i<refToComponent.length; i++) {
          int n = componentWithRef[i].getDimension();
          int start = refToComponent[i];
          double[][] values = new double[n][];
          for (j=0; j<n; j++) values[j] = value[j + start];
          ShadowRealTupleType component_reference =
            componentWithRef[i].getReference();
          RealTupleType ref = (RealTupleType) component_reference.getType();
          Unit[] range_units;
          CoordinateSystem range_coord_sys;
          if (i == 0 && componentWithRef[i].equals(this)) {
            range_units = value_units;
            range_coord_sys = ((RealTuple) data).getCoordinateSystem();
          }
          else {
            range_units = new Unit[n];
            for (j=0; j<n; j++) range_units[j] = value_units[j + start];
            range_coord_sys = ((RealTuple) ((Tuple) data).
                    getComponent(componentIndex[i])).getCoordinateSystem();
          }
 
          // MEM
          double[][] reference_values =
            CoordinateSystem.transformCoordinates(
              ref, null, ref.getDefaultUnits(), null,
              (RealTupleType) componentWithRef[i].getType(),
              range_coord_sys, range_units, null, value);
 
          // map reference_values to appropriate DisplayRealType-s
          // MEM
          mapValues(display_values, reference_values,
                    getComponents(componentWithRef[i], false));
          // FREE
          reference_values = null;
          // FREE (redundant reference to range_values)
          values = null;
        } // end for (int i=0; i<refToComponent.length; i++)
      } // end if (refToComponent != null)
    } // end if (length > 0)

    float[][] range_select =
      assembleSelect(display_values, 1, valueArrayLength,
                     valueToScalar, display);

    if (range_select[0] != null && range_select[0][0] != range_select[0][0]) {
      // data not selected
      return false;
    }

    if (adaptedShadowType.getIsTerminal()) {
      return terminalTupleOrReal(group, display_values, valueArrayLength,
                                 valueToScalar, default_values,
                                 inherited_values, renderer); // J3D
    }
    else { // if (!isTerminal)
      boolean post = false;
      // add values to value_array according to SelectedMapVector-s
      // of RealType-s in components (including Reference), and
      // recursively call doTransform on other components
      for (int i=0; i<valueArrayLength; i++) {
        if (display_values[i] != null) {
          value_array[i] = display_values[i][0];
        }
      }

      for (int i=0; i<tuple.getDimension(); i++) {
        Data component = tuple.getComponent(i);
        if (!(component instanceof Real) &&
            !(component instanceof RealTuple)) {
          ShadowTypeJ3D component_type = (ShadowTypeJ3D) getComponent(i);
          post |= component_type.doTransform(group, component, value_array,
                                             default_values, renderer);
        }
      }
      return post;
    }
  }
 
  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Group group) throws VisADException {
    if (adaptedShadowType.getIsTerminal()) {
      int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
/*
        Group data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("ShadowTupleTypeJ3D.postProcess: " +
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

