
//
// ShadowRealTypeJ2D.java
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

package visad.java2d;
 
import visad.*;

import java.util.*;
import java.rmi.*;

/**
   The ShadowRealTypeJ2D class shadows the RealType class,
   within a DataDisplayLink.<P>
*/
public class ShadowRealTypeJ2D extends ShadowScalarTypeJ2D {

  private Vector AccumulationVector = new Vector();

  public ShadowRealTypeJ2D(MathType type, DataDisplayLink link,
                           ShadowType parent)
         throws VisADException, RemoteException {
    super(type, link, parent);
    adaptedShadowType =
      new ShadowRealType(type, link, getAdaptedParent(parent));
  }

  /** clear AccumulationVector */
  void preProcess() throws VisADException {
    AccumulationVector.removeAllElements();
  }

  /** transform data into a Java2D VisADSceneGraphObject;
      return true if need post-process */
  boolean doTransform(VisADGroup group, Data data, float[] value_array,
                      float[] default_values, DataRenderer renderer)
         throws VisADException, RemoteException {

    if (data.isMissing()) return false;
    int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();
    if (LevelOfDifficulty == NOTHING_MAPPED) return false;

    if (!(data instanceof Real)) {
      throw new DisplayException("data must be Real: " +
                                 "ShadowRealTypeJ2D.doTransform");
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
      ((ShadowRealType) adaptedShadowType).getInheritedValues();
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
    ShadowRealType[] RealComponents = {(ShadowRealType) adaptedShadowType};
    mapValues(display_values, value, RealComponents);

    // get any text String and TextControl inherited from parent
    String text_value = getParentText();
    TextControl text_control = getParentTextControl();

    float[][] range_select =
      assembleSelect(display_values, 1, valueArrayLength,
                     valueToScalar, display);
 
    if (range_select[0] != null && range_select[0][0] != range_select[0][0]) {
      // data not selected
      return false;
    }

    // add values to value_array according to SelectedMapVector
    if (adaptedShadowType.getIsTerminal()) {
      // cannot be any Reference when RealType is terminal
      return terminalTupleOrScalar(group, display_values, text_value,
                                   text_control, valueArrayLength,
                                   valueToScalar, default_values,
                                   inherited_values, renderer);
    }
    else {
      // nothing to render at a non-terminal RealType
    }
    return false;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  void postProcess(VisADGroup group) throws VisADException {
    if (adaptedShadowType.getIsTerminal()) {
      int LevelOfDifficulty = adaptedShadowType.getLevelOfDifficulty();
      if (LevelOfDifficulty == LEGAL) {
/*
        VisADGroup data_group = null;
        // transform AccumulationVector
        group.addChild(data_group);
*/
        throw new UnimplementedException("terminal LEGAL unimplemented: " +
                                         "ShadowRealTypeJ2D.doTransform");
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

