//
// ShadowTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java3d;
 
import visad.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

/**
   The ShadowTypeJ3D hierarchy shadows the MathType hierarchy,
   within a DataDisplayLink, under Java3D.<P>
*/
public abstract class ShadowTypeJ3D extends ShadowType {

  /** basic information about this ShadowTypeJ3D */
  MathType Type; // MathType being shadowed
  transient DataDisplayLink Link;
  transient DisplayImplJ3D display;
  transient private Data data; // from Link.getData()
  private ShadowTypeJ3D Parent;

  // String and TextControl to pass on to children
  String inheritedText = null;
  TextControl inheritedTextControl = null;

  ShadowType adaptedShadowType;

  public ShadowTypeJ3D(MathType type, DataDisplayLink link,
                       ShadowType parent)
         throws VisADException, RemoteException {
    super(type, link, getAdaptedParent(parent));
    Type = type;
    Link = link;
    display = (DisplayImplJ3D) link.getDisplay();
    Parent = (ShadowTypeJ3D) parent;
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

  /** transform data into a Java3D scene graph;
      add generated scene graph components as children of group;
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process;
      this is default (for ShadowTextType) */
  public boolean doTransform(Group group, Data data, float[] value_array,
                      float[] default_values, DataRenderer renderer)
          throws VisADException, RemoteException {
    return false;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  void postProcess(Group group) throws VisADException { // J3D
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
    ShadowType.mapValues(display_values, values, reals);
  }

  public static VisADGeometryArray makePointGeometry(float[][] spatial_values,
                byte[][] color_values) throws VisADException {
    return ShadowType.makePointGeometry(spatial_values, color_values);
  }

  /** construct an Appearance object */
  static Appearance makeAppearance(GraphicsModeControl mode,
                      TransparencyAttributes constant_alpha,
                      ColoringAttributes constant_color,
                      GeometryArray geometry, boolean no_material) {
    Appearance appearance = new Appearance();

    LineAttributes line = new LineAttributes();
    line.setLineWidth(mode.getLineWidth());
    appearance.setLineAttributes(line);

    PointAttributes point = new PointAttributes();
    point.setPointSize(mode.getPointSize());
    appearance.setPointAttributes(point);

    PolygonAttributes polygon = new PolygonAttributes();
    polygon.setCullFace(PolygonAttributes.CULL_NONE);
    polygon.setPolygonMode(mode.getPolygonMode());
    appearance.setPolygonAttributes(polygon);

    RenderingAttributes rendering = new RenderingAttributes();
    rendering.setDepthBufferEnable(true);
    appearance.setRenderingAttributes(rendering);

    if (constant_color != null) {
      appearance.setColoringAttributes(constant_color);
    }
    // only do Material if geometry is 2-D (not 0-D points or 1-D lines)
    if (!(geometry instanceof LineArray ||
          geometry instanceof PointArray ||
          geometry instanceof IndexedLineArray ||
          geometry instanceof IndexedPointArray ||
          geometry instanceof IndexedLineStripArray ||
          geometry instanceof LineStripArray)) {
      if (!no_material) {
        Material material = new Material();
        material.setSpecularColor(0.0f, 0.0f, 0.0f);
        // no lighting in 2-D mode
        if (!mode.getMode2D()) material.setLightingEnable(true);
        appearance.setMaterial(material);
      }
      if (constant_alpha != null) {
        appearance.setTransparencyAttributes(constant_alpha);
      }
    }

    return appearance;
  }

  /** collect and transform Shape DisplayRealType values from display_values;
      offset by spatial_values, selected by range_select */
  public static VisADGeometryArray[] assembleShape(float[][] display_values,
                int valueArrayLength, int[] valueToMap, Vector MapVector,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                float[][] spatial_values, byte[][] color_values,
                boolean[][] range_select, int index)
         throws VisADException, RemoteException {
    return ShadowType.assembleShape(display_values, valueArrayLength,
           valueToMap, MapVector, valueToScalar, display, default_values,
           inherited_values, spatial_values, color_values, range_select, index);
  }

  /** collect and transform spatial DisplayRealType values from display_values;
      add spatial offset DisplayRealType values;
      adjust flow1_values and flow2_values for any coordinate transform;
      if needed, return a spatial Set from spatial_values, with the same topology
      as domain_set (or an appropriate Irregular topology);
      domain_set = null and allSpatial = false if not called from
      ShadowFunctionType */
  public static Set assembleSpatial(float[][] spatial_values,
                float[][] display_values, int valueArrayLength,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, int[] inherited_values,
                Set domain_set, boolean allSpatial, boolean set_for_shape,
                int[] spatialDimensions, boolean[][] range_select,
                float[][] flow1_values, float[][] flow2_values,
                float[] flowScale, boolean[] swap, DataRenderer renderer)
         throws VisADException, RemoteException {
    return ShadowType.assembleSpatial(spatial_values, display_values,
           valueArrayLength, valueToScalar, display, default_values,
           inherited_values, domain_set, allSpatial, set_for_shape,
           spatialDimensions, range_select, flow1_values, flow2_values,
           flowScale, swap, renderer);
  }

  /** assemble Flow components;
      Flow components are 'single', so no compositing is required */
  public static void assembleFlow(float[][] flow1_values,
                float[][] flow2_values, float[] flowScale,
                float[][] display_values, int valueArrayLength,
                int[] valueToScalar, DisplayImpl display,
                float[] default_values, boolean[][] range_select,
                DataRenderer renderer)
         throws VisADException, RemoteException {
    ShadowType.assembleFlow(flow1_values, flow2_values, flowScale,
                      display_values, valueArrayLength, valueToScalar,
                      display, default_values, range_select, renderer);
  }

  public VisADGeometryArray[] makeFlow(int which, float[][] flow_values,
                float flowScale, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
         throws VisADException {
    return adaptedShadowType.makeFlow(which, flow_values, flowScale,
           spatial_values, color_values, range_select);
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
  public static byte[][] assembleColor(float[][] display_values,
                int valueArrayLength, int[] valueToScalar,
                DisplayImpl display, float[] default_values,
                boolean[][] range_select, boolean[] single_missing)
         throws VisADException, RemoteException {
    return ShadowType.assembleColor(display_values, valueArrayLength,
           valueToScalar, display, default_values, range_select,
           single_missing);
  }

  /** return a composite of SelectRange DisplayRealType values from
      display_values, as 0.0 for select and Double.Nan for no select
      (these values can be added to other DisplayRealType values) */
  public static boolean[][] assembleSelect(float[][] display_values,
                             int domain_length, int valueArrayLength,
                             int[] valueToScalar, DisplayImpl display)
         throws VisADException {
    return ShadowType.assembleSelect(display_values, domain_length,
           valueArrayLength, valueToScalar, display);
  }

  public boolean terminalTupleOrScalar(Group group, float[][] display_values,
                                String text_value, TextControl text_control,
                                int valueArrayLength, int[] valueToScalar,
                                float[] default_values, int[] inherited_values,
                                DataRenderer renderer)
          throws VisADException, RemoteException {

    boolean post = adaptedShadowType.terminalTupleOrScalar(group, display_values,
                       text_value, text_control, valueArrayLength, valueToScalar,
                       default_values, inherited_values, renderer, this);
    ensureNotEmpty(group);
    return post;
  }

  /** this is a work-around for the NullPointerException at
      javax.media.j3d.Shape3DRetained.setLive(Shape3DRetained.java:448) */
  public void ensureNotEmpty(Group group) {
    if (group.numChildren() > 0) return;
    GeometryArray geometry =
      new PointArray(1, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
    float[] coordinates = new float[3];
    coordinates[0] = 1000000.0f;
    coordinates[1] = 1000000.0f;
    coordinates[2] = 1000000.0f;
    geometry.setCoordinates(0, coordinates);
    float[] colors = new float[3];
    colors[0] = 0.0f;
    colors[1] = 0.0f;
    colors[2] = 0.0f;
    geometry.setColors(0, colors);
    Appearance appearance =
      makeAppearance(display.getGraphicsModeControl(), null, null, geometry, false);
    Shape3D shape = new Shape3D(geometry, appearance);
    group.addChild(shape);
  }

  public boolean addToGroup(Object group, VisADGeometryArray array,
                            GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color)
         throws VisADException {
    if (array != null && array.vertexCount > 0) {
      // MEM - for coordinates if mode2d
      GeometryArray geometry = display.makeGeometry(array);
      TransparencyAttributes c_alpha = null;
      if (constant_alpha == 1.0f) {
        // constant opaque alpha = NONE
        c_alpha = new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
      }
      else if (constant_alpha == constant_alpha) {
        c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
                                             constant_alpha);
      }
      else {
        // WLH - 18 Aug 99 - how could this have gone undetected for so long?
        // c_alpha = new TransparencyAttributes(mode.getTransparencyMode(), 1.0f);
        c_alpha = new TransparencyAttributes(mode.getTransparencyMode(), 0.0f);
      }
      ColoringAttributes c_color = null;
      if (constant_color != null && constant_color.length == 3) {
        c_color = new ColoringAttributes();
        c_color.setColor(constant_color[0], constant_color[1], constant_color[2]);
      }
      Appearance appearance =
        makeAppearance(mode, c_alpha, c_color, geometry, false);
      Shape3D shape = new Shape3D(geometry, appearance);
      ((Group) group).addChild(shape);
      return true;
    }
    else {
      return false;
    }
  }

  public String toString() {
    return adaptedShadowType.toString();
  }

}

