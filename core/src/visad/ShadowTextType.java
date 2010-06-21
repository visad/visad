//
// ShadowTextType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
   The ShadowTextType class shadows the TextType class,
   within a DataDisplayLink.<P>
*/
public class ShadowTextType extends ShadowScalarType {

  public ShadowTextType(MathType t, DataDisplayLink link, ShadowType parent)
                 throws VisADException, RemoteException {
    super(t, link, parent);
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

    if (!(data instanceof Text)) {
      throw new DisplayException("data must be Text: " +
                                 "ShadowTextType.doTransform");
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

// ????
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

    boolean[][] range_select =
      shadow_api.assembleSelect(display_values, 1, valueArrayLength,
                     valueToScalar, display, shadow_api);

    if (range_select[0] != null && !range_select[0][0]) {
      // data not selected
      return false;
    }

    // get any text String and TextControl inherited from parent
    String text_value = shadow_api.getParentText();
    TextControl text_control = shadow_api.getParentTextControl();
    boolean anyText = getAnyText();
    if (anyText && text_value == null) {
      // get any text String and TextControl from this
      Vector maps = getSelectedMapVector();
      if (!maps.isEmpty()) {
        text_value = ((Text) data).getValue();
        ScalarMap map = (ScalarMap) maps.firstElement();
        text_control = (TextControl) map.getControl();
      }
    }

//
// never renders text ????
//

    // add values to value_array according to SelectedMapVector
    if (getIsTerminal()) {
      // ????
      return terminalTupleOrScalar(group, display_values, text_value,
                                   text_control, valueArrayLength,
                                   valueToScalar, default_values,
                                   inherited_values, renderer, shadow_api);
    }
    else {
      // nothing to render at a non-terminal TextType
    }
    return false;
  }

}

