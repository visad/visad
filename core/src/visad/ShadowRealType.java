//
// ShadowRealType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
import java.text.*;
import java.rmi.*;

/**
   The ShadowRealType class shadows the RealType class,
   within a DataDisplayLink.<P>
*/
public class ShadowRealType extends ShadowScalarType {

  public ShadowRealType(MathType type, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(type, link, parent);
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

    if (!(data instanceof Real)) {
      throw new DisplayException("data must be Real: " +
                                 "ShadowRealType.doTransform");
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

    Real real = (Real) data;

    RealType rtype = (RealType) getType();
    RealType[] realComponents = {rtype};
    double[][] value = new double[1][1];
    value[0][0] = real.getValue(rtype.getDefaultUnit());
    ShadowRealType[] RealComponents = {this};
    mapValues(display_values, value, RealComponents);

    // get any text String and TextControl inherited from parent
    String text_value = getParentText();
    TextControl text_control = getParentTextControl();

    if (getAnyText() && text_value == null) {
      // get any text String and TextControl from this
      Enumeration maps = getSelectedMapVector().elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        DisplayRealType dreal = map.getDisplayScalar();
        if (dreal.equals(Display.Text)) {
          text_control = (TextControl) map.getControl();
          NumberFormat format = text_control.getNumberFormat();
          if (value[0][0] != value[0][0]) {
            text_value = "";
          }
          else if (format == null) {
            text_value = PlotText.shortString(value[0][0]);
          }
          else {
            text_value = format.format(value[0][0]);
          }
          break;
        }
      }
    }

    boolean[][] range_select =
      shadow_api.assembleSelect(display_values, 1, valueArrayLength,
                     valueToScalar, display, shadow_api);

    if (range_select[0] != null && !range_select[0][0]) {
      // data not selected
      return false;
    }

    // add values to value_array according to SelectedMapVector
    if (getIsTerminal()) {
      // cannot be any Reference when RealType is terminal
      return terminalTupleOrScalar(group, display_values, text_value,
                                   text_control, valueArrayLength,
                                   valueToScalar, default_values,
                                   inherited_values, renderer, shadow_api);
    }
    else {
      // nothing to render at a non-terminal RealType
    }
    return false;
  }

}

