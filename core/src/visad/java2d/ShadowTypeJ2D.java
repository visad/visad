//
// ShadowTypeJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java2d;

import visad.*;

import java.util.Vector;
import java.rmi.*;

/**
   The ShadowTypeJ2D hierarchy shadows the MathType hierarchy,
   within a DataDisplayLink, under Java2D.<P>
*/
public abstract class ShadowTypeJ2D extends ShadowType {

  /** basic information about this ShadowTypeJ2D */
  MathType Type; // MathType being shadowed
  transient DataDisplayLink Link;
  transient DisplayImplJ2D display;
  transient private Data data; // from Link.getData()
  private ShadowTypeJ2D Parent;

  // String and TextControl to pass on to children
  String inheritedText = null;
  TextControl inheritedTextControl = null;

  ShadowType adaptedShadowType;

  public ShadowTypeJ2D(MathType type, DataDisplayLink link,
                       ShadowType parent)
         throws VisADException, RemoteException {
    super(type, link, getAdaptedParent(parent));
    Type = type;
    Link = link;
    display = (DisplayImplJ2D) link.getDisplay();
    Parent = (ShadowTypeJ2D) parent;
    data = link.getData();
  }

  public static ShadowType getAdaptedParent(ShadowType parent) {
    if (parent == null) return null;
    else return parent.getAdaptedShadowType();
  }

  public ShadowType getAdaptedShadowType() {
    return adaptedShadowType;
  }

  public ShadowRealType[] getComponents(ShadowType type, boolean doRef)
          throws VisADException {
    return adaptedShadowType.getComponents(type, doRef);
  }

  public String getParentText() {
    if (Parent != null && Parent.inheritedText != null &&
        Parent.inheritedTextControl != null) {
      return Parent.inheritedText;
    }
    else {
      return null;
    }
  }

  public TextControl getParentTextControl() {
    if (Parent != null && Parent.inheritedText != null &&
        Parent.inheritedTextControl != null) {
      return Parent.inheritedTextControl;
    }
    else {
      return null;
    }
  }

  public void setText(String text, TextControl control) {
    inheritedText = text;
    inheritedTextControl = control;
  }

  public Data getData() {
    return data;
  }

  public DisplayImpl getDisplay() {
    return display;
  }

  public MathType getType() {
    return Type;
  }

  public int getLevelOfDifficulty() {
    return adaptedShadowType.getLevelOfDifficulty();
  }

  public boolean getMultipleDisplayScalar() {
    return adaptedShadowType.getMultipleDisplayScalar();
  }

  public boolean getMappedDisplayScalar() {
    return adaptedShadowType.getMappedDisplayScalar();
  }

  public int[] getDisplayIndices() {
    return adaptedShadowType.getDisplayIndices();
  }

  public int[] getValueIndices() {
    return adaptedShadowType.getValueIndices();
  }

  /** checkIndices: check for rendering difficulty, etc */
  public int checkIndices(int[] indices, int[] display_indices,
             int[] value_indices, boolean[] isTransform, int levelOfDifficulty)
      throws VisADException, RemoteException {
    return adaptedShadowType.checkIndices(indices, display_indices, value_indices,
                                          isTransform, levelOfDifficulty);
  }

  /** clear AccumulationVector */
  void preProcess() throws VisADException {
  }

  /** transform data into a VisADSceneGraphObject;
      add generated scene graph components as children of group;
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process;
      this is default (for ShadowTextType) */
  boolean doTransform(VisADGroup group, Data data, float[] value_array,
                      float[] default_values, DataRenderer renderer)
          throws VisADException, RemoteException {
    return false;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  void postProcess(VisADGroup group) throws VisADException {
  }


  /* helpers for doTransform */

  /** map values into display_values according to ScalarMap-s in reals */
  public  static void mapValues(float[][] display_values, double[][] values,
                               ShadowRealType[] reals) throws VisADException {
    ShadowType.mapValues(display_values, values, reals);
  }

  /** map values into display_values according to ScalarMap-s in reals */
  public static void mapValues(float[][] display_values, float[][] values,
                               ShadowRealType[] reals) throws VisADException {
    mapValues(display_values, values, reals, true);
  }

  /** map values into display_values according to ScalarMap-s in reals */
  public static void mapValues(float[][] display_values, float[][] values,
                               ShadowRealType[] reals, boolean copy) 
                               throws VisADException {
    ShadowType.mapValues(display_values, values, reals, copy);
  }

  public static VisADGeometryArray makePointGeometry(float[][] spatial_values,
                byte[][] color_values) throws VisADException {
    return ShadowType.makePointGeometry(spatial_values, color_values);
  }

  /** construct an VisADAppearance object */
  static VisADAppearance makeAppearance(GraphicsModeControl mode,
                      float constant_alpha,
                      float[] constant_color,
                      VisADGeometryArray array) {
    VisADAppearance appearance = new VisADAppearance();
    appearance.pointSize = mode.getPointSize();
    appearance.lineWidth = mode.getLineWidth();
    appearance.lineStyle = mode.getLineStyle();

    appearance.alpha = constant_alpha; // may be Float.NaN
    if (constant_color != null && constant_color.length == 3) {
      appearance.color_flag = true;
      appearance.red = constant_color[0];
      appearance.green = constant_color[1];
      appearance.blue = constant_color[2];
    }
    appearance.array = array; // may be null
    return appearance;
  }

  /** collect and transform Shape DisplayRealType values from display_values;
      offset by spatial_values, selected by range_select */
  public VisADGeometryArray[] assembleShape(float[][] display_values,
                int valueArrayLength, int[] valueToMap, Vector MapVector,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                float[][] spatial_values, byte[][] color_values,
                boolean[][] range_select, int index, ShadowType shadow_api)
         throws VisADException, RemoteException {
    return adaptedShadowType.assembleShape(display_values, valueArrayLength,
           valueToMap, MapVector, valueToScalar, display, default_values,
           inherited_values, spatial_values, color_values, range_select, index,
           shadow_api);
  }

  /** collect and transform spatial DisplayRealType values from display_values;
      add spatial offset DisplayRealType values;
      adjust flow1_values and flow2_values for any coordinate transform;
      if needed, return a spatial Set from spatial_values, with the same topology
      as domain_set (or an appropriate Irregular topology);
      domain_set = null and allSpatial = false if not called from
      ShadowFunctionType */
  public Set assembleSpatial(float[][] spatial_values,
                float[][] display_values, int valueArrayLength,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                Set domain_set, boolean allSpatial, boolean set_for_shape,
                int[] spatialDimensions, boolean[][] range_select,
                float[][] flow1_values, float[][] flow2_values,
                float[] flowScale, boolean[] swap, DataRenderer renderer,
                ShadowType shadow_api)
         throws VisADException, RemoteException {
    return adaptedShadowType.assembleSpatial(spatial_values, display_values,
           valueArrayLength, valueToScalar, display, default_values,
           inherited_values, domain_set, allSpatial, set_for_shape,
           spatialDimensions, range_select, flow1_values, flow2_values,
           flowScale, swap, renderer, shadow_api);
  }

  /** assemble Flow components;
      Flow components are 'single', so no compositing is required */
  public void assembleFlow(float[][] flow1_values,
                float[][] flow2_values, float[] flowScale,
                float[][] display_values, int valueArrayLength,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, boolean[][] range_select,
                DataRenderer renderer, ShadowType shadow_api)
         throws VisADException, RemoteException {
    adaptedShadowType.assembleFlow(flow1_values, flow2_values, flowScale,
                      display_values, valueArrayLength, valueToScalar,
                      display, default_values, range_select, renderer,
                      shadow_api);
  }

  public VisADGeometryArray[] makeFlow(int which, float[][] flow_values,
                float flowScale, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
         throws VisADException {
    return adaptedShadowType.makeFlow(which, flow_values, flowScale,
           spatial_values, color_values, range_select);
  }

  public VisADGeometryArray[] makeStreamline(int which, float[][] flow_values,
                float flowScale, float[][] spatial_values, Set spatial_set,
                int spatialManifoldDimension,
                byte[][] color_values, boolean[][] range_select,
                int valueArrayLength, int[] valueToMap, Vector MapVector)
         throws VisADException {
    return adaptedShadowType.makeStreamline(which, flow_values, flowScale,
           spatial_values, spatial_set, spatialManifoldDimension,
           color_values, range_select, 
           valueArrayLength, valueToMap, MapVector);
  }

  public boolean makeContour(int valueArrayLength, int[] valueToScalar,
                       float[][] display_values, int[] inherited_values,
                       Vector MapVector, int[] valueToMap, int domain_length,
                       boolean[][] range_select, int spatialManifoldDimension,
                       Set spatial_set, byte[][] color_values, boolean indexed,
                       Object group, GraphicsModeControl mode, boolean[] swap,
                       float constant_alpha, float[] constant_color,
                       ShadowType shadow_api, ShadowRealTupleType Domain, 
                       ShadowRealType[] DomainReferenceComponents,
                       Set domain_set, Unit[] domain_units, CoordinateSystem dataCoordinateSystem)
         throws VisADException {
    return adaptedShadowType.makeContour(valueArrayLength, valueToScalar,
                       display_values, inherited_values, MapVector, valueToMap,
                       domain_length, range_select, spatialManifoldDimension,
                       spatial_set, color_values, indexed, group, mode,
                       swap, constant_alpha, constant_color, shadow_api, Domain, 
                       DomainReferenceComponents,
                       domain_set, domain_units, dataCoordinateSystem);
  }

  public void addLabelsToGroup(Object group, VisADGeometryArray[] arrays,
                               GraphicsModeControl mode, ContourControl control,
                               ProjectionControl p_cntrl, int[] cnt_a,
                               float constant_alpha, float[] constant_color)
         throws VisADException 
  {
    int n_labels = arrays.length;

    for ( int ii = 0; ii < n_labels; ii++ )
    {
      addToGroup(group, ((ContourLabelGeometry)arrays[ii]).label, mode,
                 constant_alpha, constant_color);
    }
  }

  public VisADGeometryArray makeText(String[] text_values,
                TextControl text_control, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
         throws VisADException {
    return adaptedShadowType.makeText(text_values, text_control, spatial_values,
                                      color_values, range_select);
  }

  /** composite and transform color and Alpha DisplayRealType values
      from display_values, and return as (Red, Green, Blue, Alpha) */
  public byte[][] assembleColor(float[][] display_values,
                int valueArrayLength, int[] valueToScalar,
                DisplayImpl display, float[] default_values,
                boolean[][] range_select, boolean[] single_missing,
                ShadowType shadow_api)
         throws VisADException, RemoteException {
    return adaptedShadowType.assembleColor(display_values, valueArrayLength,
           valueToScalar, display, default_values, range_select,
           single_missing, shadow_api);
  }

  /** return a composite of SelectRange DisplayRealType values from
      display_values, as 0.0 for select and Double.Nan for no select
      (these values can be added to other DisplayRealType values) */
  public boolean[][] assembleSelect(float[][] display_values,
                             int domain_length, int valueArrayLength,
                             int[] valueToScalar, DisplayImpl display,
                             ShadowType shadow_api)
         throws VisADException {
    return adaptedShadowType.assembleSelect(display_values, domain_length,
           valueArrayLength, valueToScalar, display, shadow_api);
  }

  public boolean terminalTupleOrScalar(VisADGroup group, float[][] display_values,
                                String text_value, TextControl text_control,
                                int valueArrayLength, int[] valueToScalar,
                                float[] default_values, int[] inherited_values,
                                DataRenderer renderer)
          throws VisADException, RemoteException {

    return adaptedShadowType.terminalTupleOrScalar(group, display_values,
                       text_value, text_control, valueArrayLength, valueToScalar,
                       default_values, inherited_values, renderer, this);
  }

  public boolean addToGroup(Object group, VisADGeometryArray array,
                            GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color)
         throws VisADException {
    if (array != null) {
      VisADAppearance appearance =
        makeAppearance(mode, constant_alpha, constant_color, array);
      ((VisADGroup) group).addChild(appearance);
      return true;
    }
    else {
      return false;
    }
  }

  public boolean allowCurvedTexture() {
    return false;
  }

  public String toString() {
    return (adaptedShadowType == null ? null : adaptedShadowType.toString());
  }

}

