//
// ShadowTupleType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

import java.text.*;
import java.util.*;
import java.rmi.*;

/**
   The ShadowTupleType class shadows the TupleType class,
   within a DataDisplayLink.<P>
*/
public class ShadowTupleType extends ShadowType {

  ShadowType[] tupleComponents;

  private ShadowRealType[] RealComponents;

  /** value_indices from parent */
  private int[] inherited_values;

  /** true if no component with mapped Scalar components is a
      ShadowSetType, ShadowFunctionType or ShadowTupleType;
      not the same as TupleType.Flat */
  private boolean Flat;

  public ShadowTupleType(MathType t, DataDisplayLink link, ShadowType parent,
                         ShadowType[] tcs) throws VisADException, RemoteException {
    super(t, link, parent);
    tupleComponents = tcs;
    int n = tupleComponents.length;
    Flat = true;
    ShadowType[] components = new ShadowType[n];
    MultipleSpatialDisplayScalar = false;
    MultipleDisplayScalar = false;
    // compute Flat, DisplayIndices and ValueIndices
    for (int i=0; i<n; i++) {
      ShadowType shadow = tupleComponents[i];
      MultipleSpatialDisplayScalar |=
        tupleComponents[i].getMultipleSpatialDisplayScalar();
      MultipleDisplayScalar |= tupleComponents[i].getMultipleDisplayScalar();
      boolean mappedComponent = tupleComponents[i].getMappedDisplayScalar();
      MappedDisplayScalar |= mappedComponent;
      if (shadow instanceof ShadowFunctionType ||
          shadow instanceof ShadowSetType ||
          (shadow instanceof ShadowTupleType &&
           !(shadow instanceof ShadowRealTupleType)) ) {
        if (mappedComponent) Flat = false;
      }
      else if (shadow instanceof ShadowScalarType ||
               shadow instanceof ShadowRealTupleType) {
        // treat ShadowRealTupleType component as
        // a set of ShadowRealType components
        DisplayIndices = addIndices(DisplayIndices, shadow.getDisplayIndices());
        ValueIndices = addIndices(ValueIndices, shadow.getValueIndices());
      }
    }
    RealComponents = getComponents(this, true);
  }

  public ShadowRealType[] getRealComponents() {
    return RealComponents;
  }

  // copy and increment indices for each ShadowScalarType component and
  // each ShadowRealType component of a ShadowRealTupleType component
  int[] sumIndices(int[] indices) {
    int[] local_indices = copyIndices(indices);
    int n = tupleComponents.length;
    for (int j=0; j<n; j++) {
      ShadowType shadow = (ShadowType) tupleComponents[j];
      if (shadow instanceof ShadowScalarType) {
        ((ShadowScalarType) shadow).incrementIndices(local_indices);
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

  /** add DisplayIndices (from each ShadowScalarType and
      ShadowRealTupleType component) */
  int[] sumDisplayIndices(int[] display_indices) throws VisADException {
    return addIndices(display_indices, DisplayIndices);
  }

  /** add ValueIndices (from each ShadowScalarType and
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
  public int checkIndices(int[] indices, int[] display_indices,
             int[] value_indices, boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    // add indices & display_indices from RealType and
    // RealTupleType components
    int[] local_indices = sumIndices(indices);
    int[] local_display_indices = sumDisplayIndices(display_indices);
    int[] local_value_indices = sumValueIndices(value_indices);

    anyContour = checkContour(local_display_indices);
    anyFlow = checkFlow(local_display_indices);
    anyShape = checkShape(local_display_indices);
    anyText = checkText(local_display_indices);
    adjustProjectionSeam = checkAdjustProjectionSeam();

    markTransform(isTransform);

    // get value_indices arrays used by doTransform
    inherited_values = copyIndices(value_indices);

    // check for any mapped
    if (levelOfDifficulty == NOTHING_MAPPED) {
      if (checkAny(DisplayIndices)) {
        levelOfDifficulty = NESTED;
      }
    }

    if (Flat) {
      // test legality of Animation and SelectValue
      if (checkAnimationOrValue(DisplayIndices) > 0) {
        throw new BadMappingException("Animation and SelectValue may not " +
                                      "occur in range: " +
                                      "ShadowTupleType.checkIndices");
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

  public int[] getInheritedValues() {
    return inherited_values;
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

  /** transform data into a (Java3D or Java2D) scene graph;
      add generated scene graph components as children of group;
      group is Group (Java3D) or VisADGroup (Java2D);
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process */
  public boolean doTransform(Object group, Data data, float[] value_array,
                             float[] default_values, DataRenderer renderer,
                             ShadowType shadow_api)
         throws VisADException, RemoteException {

    if (data.isMissing()) return false;
    if (LevelOfDifficulty == NOTHING_MAPPED) return false;

    if (!(data instanceof TupleIface)) {
      throw new DisplayException("data must be Tuple: " +
                                 "ShadowTupleType.doTransform");
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
    int[] inherited_values = getInheritedValues();
    for (int i=0; i<valueArrayLength; i++) {
      if (inherited_values[i] > 0) {
        display_values[i] = new float[1];
        display_values[i][0] = value_array[i];
      }
    }

    TupleIface tuple = (TupleIface) data;
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
      ShadowRealType[] RealComponents = getRealComponents();
      mapValues(display_values, value, RealComponents);

      int[] refToComponent = getRefToComponent();
      ShadowRealTupleType[] componentWithRef = getComponentWithRef();
      int[] componentIndex = getComponentIndex();

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
            range_coord_sys = ((RealTuple) ((TupleIface) data).
                    getComponent(componentIndex[i])).getCoordinateSystem();
          }

          // MEM
          double[][] reference_values =
            CoordinateSystem.transformCoordinates(
              ref, null, ref.getDefaultUnits(), null,
              (RealTupleType) componentWithRef[i].getType(),
              range_coord_sys, range_units, null, values);

          // WLH 13 March 2000
          // if (getAnyFlow()) {
            renderer.setEarthSpatialData(componentWithRef[i],
                      component_reference, ref, ref.getDefaultUnits(),
                      (RealTupleType) componentWithRef[i].getType(),
                      new CoordinateSystem[] {range_coord_sys}, range_units);
          // WLH 13 March 2000
          // }

          // map reference_values to appropriate DisplayRealType-s
          // MEM
          mapValues(display_values, reference_values,
                    getComponents(component_reference, false));
          // FREE
          reference_values = null;
          // FREE (redundant reference to range_values)
          values = null;
        } // end for (int i=0; i<refToComponent.length; i++)
      } // end if (refToComponent != null)

      // setEarthSpatialData calls when no CoordinateSystem
      // WLH 13 March 2000
      // if (this instanceof ShadowTupleType && getAnyFlow()) {
      if (this instanceof ShadowTupleType) {
        if (this instanceof ShadowRealTupleType) {
          Unit[] range_units = value_units;
          CoordinateSystem range_coord_sys =
            ((RealTuple) data).getCoordinateSystem();
/* WLH 23 May 99
          renderer.setEarthSpatialData((ShadowRealTupleType) this,
                    null, null, null, (RealTupleType) this.getType(),
                    new CoordinateSystem[] {range_coord_sys}, range_units);
*/
          ShadowRealTupleType component_reference =
            ((ShadowRealTupleType) this).getReference();
          RealTupleType ref = (component_reference == null) ? null :
                                (RealTupleType) component_reference.getType();
          Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
          renderer.setEarthSpatialData((ShadowRealTupleType) this,
                    component_reference, ref, ref_units,
                    (RealTupleType) this.getType(),
                    new CoordinateSystem[] {range_coord_sys}, range_units);
        }
        else { // if (!(this instanceof ShadowRealTupleType))
          int start = 0;
          int n = ((ShadowTupleType) this).getDimension();
          for (int i=0; i<n ;i++) {
            ShadowType component =
              ((ShadowTupleType) this).getComponent(i);
            if (component instanceof ShadowRealTupleType) {
              int m = ((ShadowRealTupleType) component).getDimension();
              Unit[] range_units = new Unit[m];
              for (j=0; j<m; j++) range_units[j] = value_units[j + start];
              CoordinateSystem range_coord_sys =
                ((RealTuple) ((TupleIface) data).getComponent(i)).getCoordinateSystem();
/* WLH 23 May 99
              renderer.setEarthSpatialData((ShadowRealTupleType)
                      component, null, null,
                      null, (RealTupleType) component.getType(),
                      new CoordinateSystem[] {range_coord_sys}, range_units);
*/
              ShadowRealTupleType component_reference =
                ((ShadowRealTupleType) component).getReference();
              RealTupleType ref = (component_reference == null) ? null :
                                  (RealTupleType) component_reference.getType();
              Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
              renderer.setEarthSpatialData((ShadowRealTupleType) component,
                      component_reference, ref, ref_units,
                      (RealTupleType) component.getType(),
                      new CoordinateSystem[] {range_coord_sys}, range_units);
              start += ((ShadowRealTupleType) component).getDimension();
            }
            else if (component instanceof ShadowRealType) {
              start++;
            }
          }
        } // end if (!(this instanceof ShadowRealTupleType))
      } // end if (this instanceof ShadowTupleType)

    } // end if (length > 0)

    // get any text String and TextControl inherited from parent
    String text_value = getParentText();
    TextControl text_control = getParentTextControl();
    boolean anyText = getAnyText();
    if (anyText && text_value == null) {
      for (int i=0; i<valueArrayLength; i++) {
        if (display_values[i] != null) {
          int displayScalarIndex = valueToScalar[i];
          ScalarMap map = (ScalarMap) MapVector.elementAt(valueToMap[i]);
          ScalarType real = map.getScalar();
          DisplayRealType dreal = display.getDisplayScalar(displayScalarIndex);
          if (dreal.equals(Display.Text) && real instanceof RealType) {
            text_control = (TextControl) map.getControl();
            NumberFormat format = text_control.getNumberFormat();
            if (display_values[i][0] != display_values[i][0]) {
              text_value = "";
            }
            else if (format == null) {
              text_value = PlotText.shortString(display_values[i][0]);
            }
            else {
              text_value = format.format(display_values[i][0]);
            }
            break;
          }
        }
      }

      if (text_value == null) {
        for (int i=0; i<tuple.getDimension(); i++) {
          Data component = tuple.getComponent(i);
          if (component instanceof Text) {
            ShadowTextType type = (ShadowTextType) tupleComponents[i];
            Vector maps = type.getSelectedMapVector();
            if (!maps.isEmpty()) {
              text_value = ((Text) component).getValue();
              ScalarMap map = (ScalarMap) maps.firstElement();
              text_control = (TextControl) map.getControl();
            }
          }
        }
      }
    } // end if (anyText && text_value == null)

    boolean[][] range_select =
      shadow_api.assembleSelect(display_values, 1, valueArrayLength,
                     valueToScalar, display, shadow_api);

    if (range_select[0] != null && !range_select[0][0]) {
      // data not selected
      return false;
    }

    if (getIsTerminal()) {
      return terminalTupleOrScalar(group, display_values, text_value,
                                   text_control, valueArrayLength,
                                   valueToScalar, default_values,
                                   inherited_values, renderer, shadow_api);
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

      if (text_value != null && text_control != null) {
        setText(text_value, text_control);
      }
      else {
        setText(null, null);
      }

      for (int i=0; i<tuple.getDimension(); i++) {
        Data component = tuple.getComponent(i);
        if (!(component instanceof Real) &&
            !(component instanceof RealTuple)) {
          // push lat_index and lon_index for flow navigation
          int[] lat_lon_indices = renderer.getLatLonIndices();
          post |= shadow_api.recurseComponent(i, group, component, value_array,
                                              default_values, renderer);
          // pop lat_index and lon_index for flow navigation
          renderer.setLatLonIndices(lat_lon_indices);
        }
      }
      return post;
    }
  }


  public boolean isFlat() {
    return Flat;
  }

}

