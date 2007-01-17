//
// ShadowTypeJ3D.java
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

package visad.java3d;

import visad.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.Vector;
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

  ProjectionControlListener projListener = null;

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
  public void preProcess() throws VisADException {
  }

  /** transform data into a Java3D scene graph;
      add generated scene graph components as children of group;
      value_array are inherited valueArray values;
      default_values are defaults for each display.DisplayRealTypeVector;
      return true if need post-process;
      this is default (for ShadowTextType) */
  public boolean doTransform(Object group, Data data, float[] value_array,
                      float[] default_values, DataRenderer renderer)
          throws VisADException, RemoteException {
    return false;
  }

  /** render accumulated Vector of value_array-s to
      and add to group; then clear AccumulationVector */
  public void postProcess(Object group) throws VisADException {
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
                               ShadowRealType[] reals, boolean copy) throws VisADException {
    ShadowType.mapValues(display_values, values, reals, copy);
  }

  public static VisADGeometryArray makePointGeometry(float[][] spatial_values,
                byte[][] color_values) throws VisADException {
    return ShadowType.makePointGeometry(spatial_values, color_values);
  }

  public Appearance makeAppearance(GraphicsModeControl mode,
                      TransparencyAttributes constant_alpha,
                      ColoringAttributes constant_color,
                      GeometryArray geometry, boolean no_material) {
    return ShadowTypeJ3D.staticMakeAppearance(mode, constant_alpha,
                           constant_color, geometry, no_material);
  }

  /** 
   * Construct an Appearance object from a GeometryArray
   * @param  mode  GraphicsModeControl
   * @param  constant_alpha  transparency attributes
   * @param  constant_color  color to use
   * @param  geometry   geometry to use for the appearance
   * @param  no_material  true to not use a Material for illumination, 
   *                      false to use it for 2-D geometries
   */
  public static Appearance staticMakeAppearance(GraphicsModeControl mode,
                      TransparencyAttributes constant_alpha,
                      ColoringAttributes constant_color,
                      GeometryArray geometry, boolean no_material) {
    Appearance appearance = new Appearance();
    appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
    appearance.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TEXGEN_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
    // appearance.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_READ);
    appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);

    LineAttributes line = new LineAttributes();
    line.setCapability(LineAttributes.ALLOW_ANTIALIASING_READ);
    line.setCapability(LineAttributes.ALLOW_PATTERN_READ);
    line.setCapability(LineAttributes.ALLOW_WIDTH_READ);
    line.setLineWidth(mode.getLineWidth());
    int pattern = GraphicsModeControlJ3D.LINE_PATTERN[mode.getLineStyle()];
    line.setLinePattern(pattern);
    appearance.setLineAttributes(line);

    PointAttributes point = new PointAttributes();
    point.setCapability(PointAttributes.ALLOW_ANTIALIASING_READ);
    point.setCapability(PointAttributes.ALLOW_SIZE_READ);
    point.setPointSize(mode.getPointSize());
    appearance.setPointAttributes(point);

    PolygonAttributes polygon = new PolygonAttributes();
    polygon.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
    polygon.setCapability(PolygonAttributes.ALLOW_MODE_READ);
    polygon.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_READ);
    polygon.setCapability(PolygonAttributes.ALLOW_OFFSET_READ);
    polygon.setCullFace(PolygonAttributes.CULL_NONE);
    polygon.setPolygonMode(mode.getPolygonMode());

    try {
      float polygonOffset = mode.getPolygonOffset();
      if (polygonOffset == polygonOffset) polygon.setPolygonOffset(polygonOffset);
    }
    catch (Exception e) {
    }

    //- TDR, use reflection since setPolygonOffsetFactor is not available in
    //       earlier versions of Java3D.
    try {
      java.lang.reflect.Method method = polygon.getClass().getMethod("setPolygonOffsetFactor", new Class[] {float.class});
      float polygonOffsetFactor = mode.getPolygonOffsetFactor();
      if (polygonOffsetFactor == polygonOffsetFactor) {
        method.invoke(polygon, new Object[] {new Float(polygonOffsetFactor)});
      }
    }
    catch (Exception e) {
    }


    appearance.setPolygonAttributes(polygon);

    RenderingAttributes rendering = new RenderingAttributes();
    rendering.setCapability(RenderingAttributes.ALLOW_ALPHA_TEST_FUNCTION_READ);
    rendering.setCapability(RenderingAttributes.ALLOW_ALPHA_TEST_VALUE_READ);
    rendering.setCapability(RenderingAttributes.ALLOW_DEPTH_ENABLE_READ);
 
    // rendering.setCapability(RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_READ);
    // rendering.setCapability(RenderingAttributes.ALLOW_RASTER_OP_READ);
    // rendering.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
    rendering.setDepthBufferEnable(true);
    appearance.setRenderingAttributes(rendering);

    if (constant_color != null) {
      constant_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
      constant_color.setCapability(ColoringAttributes.ALLOW_SHADE_MODEL_READ);
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
        material.setCapability(Material.ALLOW_COMPONENT_READ);
        material.setSpecularColor(0.0f, 0.0f, 0.0f);

        // no lighting in 2-D mode
        if (!mode.getMode2D()) material.setLightingEnable(true);
        appearance.setMaterial(material);
      }
      if (constant_alpha != null) {
        // constant_alpha.setCapability(TransparencyAttributes.ALLOW_BLEND_FUNCTION_READ);
        constant_alpha.setCapability(TransparencyAttributes.ALLOW_MODE_READ);
        constant_alpha.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
        appearance.setTransparencyAttributes(constant_alpha);
      }
    }

    return appearance;
  }
/*
  public static Appearance staticMakeAppearance(GraphicsModeControl mode,
                      TransparencyAttributes constant_alpha,
                      ColoringAttributes constant_color,
                      GeometryArray geometry, boolean no_material) {
    Appearance appearance = new Appearance();
    appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
    appearance.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TEXGEN_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_READ);
    appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);

    LineAttributes line = new LineAttributes();
    line.setLineWidth(mode.getLineWidth());
    int pattern = GraphicsModeControlJ3D.LINE_PATTERN[mode.getLineStyle()];
    line.setLinePattern(pattern);
    appearance.setLineAttributes(line);

    PointAttributes point = new PointAttributes();
    point.setPointSize(mode.getPointSize());
    appearance.setPointAttributes(point);

    PolygonAttributes polygon = new PolygonAttributes();
    polygon.setCullFace(PolygonAttributes.CULL_NONE);
    polygon.setPolygonMode(mode.getPolygonMode());

    try {
      float polygonOffset = mode.getPolygonOffset();
      if (polygonOffset == polygonOffset) polygon.setPolygonOffset(polygonOffset);
    }
    catch (Exception e) {
    }

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
        //TEST
        appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
      }
      if (constant_alpha != null) {
        appearance.setTransparencyAttributes(constant_alpha);
      }
    }

    return appearance;
  }
*/

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
                       ShadowType shadow_api, ShadowRealTupleType Domain, ShadowRealType[] DomainReferenceComponents,
                       Set domain_set, Unit[] domain_units, CoordinateSystem dataCoordinateSystem)
         throws VisADException {
    return adaptedShadowType.makeContour(valueArrayLength, valueToScalar,
                       display_values, inherited_values, MapVector, valueToMap,
                       domain_length, range_select, spatialManifoldDimension,
                       spatial_set, color_values, indexed, group, mode,
                       swap, constant_alpha, constant_color, shadow_api, Domain, DomainReferenceComponents,
                       domain_set, domain_units, dataCoordinateSystem);
  }


  /* (non-Javadoc)
   * @see visad.ShadowType#addLabelsToGroup(java.lang.Object, visad.VisADGeometryArray[][], visad.GraphicsModeControl, visad.ContourControl, visad.ProjectionControl, int[], float, float[], float[][][])
   * BMF 2006-10-11 seperated code for stretchy lines from label code
   */
  public void addLabelsToGroup(Object group, VisADGeometryArray[][] arrays,
                               GraphicsModeControl mode, ContourControl control,
                               ProjectionControl p_cntrl, int[] cnt_a,
                               float constant_alpha, float[] constant_color,
                               float[][][] f_array)
         throws VisADException {

    int cnt = cnt_a[0];

    if (cnt == 0) {
      projListener = new ProjectionControlListener(p_cntrl, control);
    }

    VisADGeometryArray[] lbl_arrays = new VisADGeometryArray[2];
    VisADGeometryArray[] seg_arrays = new VisADGeometryArray[4];

    int n_labels = arrays[2].length/2;

    // add the stretchy line segments if we are not filling
    if (!control.contourFilled()) 
    {
      projListener.LT_array[cnt] = new LabelTransform[3][n_labels];
      
      for ( int ii = 0; ii < n_labels; ii++ ) {
        
        seg_arrays[0] = arrays[3][ii*4];
        seg_arrays[1] = arrays[3][ii*4+1];
        seg_arrays[2] = arrays[3][ii*4+2];
        seg_arrays[3] = arrays[3][ii*4+3];
        
        TransformGroup segL_trans_group = new TransformGroup();
        TransformGroup segR_trans_group = new TransformGroup();
        
        segL_trans_group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        segL_trans_group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        segL_trans_group.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        segR_trans_group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        segR_trans_group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        segR_trans_group.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        
        if (control.getAutoSizeLabels())
        {

          LabelTransform lbl_trans =
            new LabelTransform(segL_trans_group, p_cntrl,
              new VisADGeometryArray[] {seg_arrays[0], seg_arrays[1]},
                new float[] {f_array[0][ii][0], f_array[0][ii][1]}, 1);
          projListener.LT_array[cnt][1][ii] = lbl_trans;

          lbl_trans =
            new LabelTransform(segR_trans_group, p_cntrl,
              new VisADGeometryArray[] {seg_arrays[2], seg_arrays[3]},
                new float[] {f_array[0][ii][2], f_array[0][ii][3]}, 1);
          projListener.LT_array[cnt][2][ii] = lbl_trans;
         }
        
        ((Group)group).addChild(segL_trans_group);
        ((Group)group).addChild(segR_trans_group);
        
        addToGroup(segL_trans_group, seg_arrays[0], mode, constant_alpha, constant_color);
        addToGroup(segR_trans_group, seg_arrays[2], mode, constant_alpha, constant_color);
        
      }
      
    } else {
      projListener.LT_array[cnt] = new LabelTransform[1][n_labels];
    }
    
    cnt = cnt_a[0];
    
    for ( int ii = 0; ii < n_labels; ii++ )
    {
      lbl_arrays[0] = arrays[2][ii*2];
      lbl_arrays[1] = arrays[2][ii*2+1];

      TransformGroup lbl_trans_group  = new TransformGroup();
      lbl_trans_group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      lbl_trans_group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      lbl_trans_group.setCapability(TransformGroup.ALLOW_CHILDREN_READ);

      if (control.getAutoSizeLabels())
      {
        LabelTransform lbl_trans =
          new LabelTransform(lbl_trans_group, p_cntrl,
            lbl_arrays, f_array[0][ii], 0);
        projListener.LT_array[cnt][0][ii] = lbl_trans;
      }

       ((Group)group).addChild(lbl_trans_group);

       addToGroup(lbl_trans_group, lbl_arrays[0], mode, constant_alpha, constant_color);

    }
    
    cnt++;
    projListener.cnt = cnt;
    cnt_a[0] = cnt;
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

  public boolean terminalTupleOrScalar(Object group, float[][] display_values,
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
  public void ensureNotEmpty(Object obj) {
    ensureNotEmpty(obj, display);
  }

  public static void ensureNotEmpty(Object obj, DisplayImpl display) {
    if (!(obj instanceof Group)) return;
    Group group = (Group) obj;
    if (group.numChildren() > 0) return;
    GeometryArray geometry =
      new PointArray(1, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
    geometry.setCapability(GeometryArray.ALLOW_COLOR_READ);
    geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
    geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
    geometry.setCapability(GeometryArray.ALLOW_NORMAL_READ);
    // geometry.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
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
      staticMakeAppearance(display.getGraphicsModeControl(), null, null,
                           geometry, false);
    Shape3D shape = new Shape3D(geometry, appearance);
    shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    group.addChild(shape);
  }

  public boolean addToGroup(Object group, VisADGeometryArray array,
                            GraphicsModeControl mode,
                            float constant_alpha, float[] constant_color)
         throws VisADException {
    if (array != null && array.vertexCount > 0) {
      float af = 0.0f;
      TransparencyAttributes c_alpha = null;
      if (constant_alpha == 1.0f) {
        // constant opaque alpha = NONE
        c_alpha = new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
      }
      else if (constant_alpha == constant_alpha) {
        c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
                                             constant_alpha);
        af = constant_alpha;
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

        // WLH 16 Oct 2001
        if (!(array instanceof VisADLineArray ||
              array instanceof VisADPointArray ||
              array instanceof VisADLineStripArray) &&
            array.colors == null) {
          int color_len = 3;
          if (af != 0.0f) {
            color_len = 4;
          }
          byte r = ShadowType.floatToByte(constant_color[0]);
          byte g = ShadowType.floatToByte(constant_color[1]);
          byte b = ShadowType.floatToByte(constant_color[2]);
          int len = array.vertexCount * color_len;
          byte[] colors = new byte[len];
          int k = 0;
          if (color_len == 3) {
            for (int i=0; i<len; i+=3) {
              colors[i] = r;
              colors[i+1] = g;
              colors[i+2] = b;
            }
          }
          else {
            byte a = ShadowType.floatToByte(af);
            for (int i=0; i<len; i+=4) {
              colors[i] = r;
              colors[i+1] = g;
              colors[i+2] = b;
              colors[i+3] = a;
            }
          }
          array.colors = colors;
        }

      }
      // MEM - for coordinates if mode2d
      GeometryArray geometry = display.makeGeometry(array);
      Appearance appearance =
        makeAppearance(mode, c_alpha, c_color, geometry, false);
      Shape3D shape = new Shape3D(geometry, appearance);
      shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      ((Group) group).addChild(shape);
      return true;
    }
    else {
      return false;
    }
  }

  public boolean addTextToGroup(Object group, VisADGeometryArray array,
                                GraphicsModeControl mode,
                                float constant_alpha, float[] constant_color)
         throws VisADException {
    if (array != null && array.vertexCount > 0) {
      float af = 0.0f;
      TransparencyAttributes c_alpha = null;
      if (constant_alpha == 1.0f) {
        // constant opaque alpha = NONE
        c_alpha = new TransparencyAttributes(TransparencyAttributes.NONE, 0.0f);
      }
      else if (constant_alpha == constant_alpha) {
        c_alpha = new TransparencyAttributes(mode.getTransparencyMode(),
                                             constant_alpha);
        af = constant_alpha;
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

        // WLH 16 Oct 2001 (really 10 Dec 2001)
        if (!(array instanceof VisADLineArray ||
              array instanceof VisADPointArray ||
              array instanceof VisADLineStripArray) &&
            array.colors == null) {
          int color_len = 3;
          if (af != 0.0f) {
            color_len = 4;
          }
          byte r = ShadowType.floatToByte(constant_color[0]);
          byte g = ShadowType.floatToByte(constant_color[1]);
          byte b = ShadowType.floatToByte(constant_color[2]);
          int len = array.vertexCount * color_len;
          byte[] colors = new byte[len];
          int k = 0;
          if (color_len == 3) {
            for (int i=0; i<len; i+=3) {
              colors[i] = r;
              colors[i+1] = g;
              colors[i+2] = b;
            }
          }
          else {
            byte a = ShadowType.floatToByte(af);
            for (int i=0; i<len; i+=4) {
              colors[i] = r;
              colors[i+1] = g;
              colors[i+2] = b;
              colors[i+3] = a;
            }
          }
          array.colors = colors;
        }

      }
      // MEM - for coordinates if mode2d
      GeometryArray geometry = display.makeGeometry(array);
      Appearance appearance =
        makeAppearance(mode, c_alpha, c_color, geometry, false);
      Shape3D shape = new Shape3D(geometry, appearance);
      shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      ((Group) group).addChild(shape);

      if (array instanceof VisADTriangleArray) {
        GeometryArray geometry2 = display.makeGeometry(array);
        Appearance appearance2 =
          makeAppearance(mode, c_alpha, c_color, geometry2, false);
        // LineAttributes la = appearance2.getLineAttributes();
        // better without anti-aliasing
        // la.setLineAntialiasingEnable(true);
        PolygonAttributes pa = appearance2.getPolygonAttributes();
        pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        Shape3D shape2 = new Shape3D(geometry2, appearance2);
        shape2.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        shape2.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        ((Group) group).addChild(shape2);
      }

      return true;
    }
    else {
      return false;
    }
  }

  public boolean allowConstantColorSurfaces() {
    return false;
  }

  public String toString() {
    return adaptedShadowType.toString();
  }

}

class ProjectionControlListener implements ControlListener
{
  LabelTransform[][][] LT_array = null;
  ProjectionControl p_cntrl = null;
  ContourControl c_cntrl = null;
  double last_scale;
  double first_scale;
  int cnt = 0;
  double last_time;


  ProjectionControlListener(ProjectionControl p_cntrl, ContourControl c_cntrl)
  {
    this.p_cntrl = p_cntrl;
    this.c_cntrl = c_cntrl;
    double[] matrix  = p_cntrl.getMatrix();
    double[] rot_a   = new double[3];
    double[] trans_a = new double[3];
    double[] scale_a = new double[1];
    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);
    last_scale  = scale_a[0];
    first_scale = last_scale;
    LT_array = new LabelTransform[1000][][];
    last_time = System.currentTimeMillis();
    p_cntrl.addControlListener(this);
    c_cntrl.addProjectionControlListener(this, p_cntrl);
  }
  public synchronized void controlChanged(ControlEvent e)
         throws VisADException, RemoteException
  {
    double[] matrix  = p_cntrl.getMatrix();
    double[] rot_a   = new double[3];
    double[] trans_a = new double[3];
    double[] scale_a = new double[1];

    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);

    //- identify scale change events.
    if (!visad.util.Util.isApproximatelyEqual(scale_a[0], last_scale))
    {
    double current_time = System.currentTimeMillis();
    if (scale_a[0]/last_scale > 1.15 ||
        scale_a[0]/last_scale < 1/1.15)
    {
      //BMF 2006-10-11 added loop for iterating controlChanged(...) calls to 
      // avoid null pointer exceptions
      if (current_time - last_time < 3000)
      {
        if (LT_array != null) {
          for (int ii = 0; ii < cnt; ii++) {
            for (int kk = 0; kk < LT_array[ii][0].length; kk++) {
              for (int jj = 0; jj < LT_array[0].length; jj ++)
                LT_array[ii][jj][kk].controlChanged(first_scale, scale_a);
            }
          }
        }
      }
      else {
        if (LT_array != null) {
          for (int ii = 0; ii < cnt; ii++) {
            for (int kk = 0; kk < LT_array[ii][0].length; kk++) {
              for (int jj = 0; jj < LT_array[0].length; jj++)
                LT_array[ii][jj][kk].controlChanged(first_scale, scale_a);
            }
          }
        }
        c_cntrl.reLabel();
      }
      last_scale = scale_a[0];
    }
    last_time = current_time;
    }
  }
}

class LabelTransform
{
  TransformGroup trans;
  Transform3D t3d;
  ProjectionControl proj;
  VisADGeometryArray label_array;
  VisADGeometryArray anchr_array;
  double[] matrix;
  double last_scale;
  double first_scale;
  float[] vertex;
  float[] anchr_vertex;
  double[] rot_a;
  double[] trans_a;
  double[] scale_a;
  int flag;
  float[] f_array;

  LabelTransform(TransformGroup trans,
                 ProjectionControl proj,
                 VisADGeometryArray[] label_array, float[] f_array, int flag)
  {
    this.trans        = trans;
    this.proj         = proj;
    this.label_array  = label_array[0];
    this.anchr_array  = label_array[1];
    this.flag         = flag;
    this.f_array      = f_array;

    t3d     = new Transform3D();
    matrix  = proj.getMatrix();
    rot_a   = new double[3];
    trans_a = new double[3];
    scale_a = new double[1];
    MouseBehaviorJ3D.unmake_matrix(rot_a, scale_a, trans_a, matrix);
    last_scale  = scale_a[0];
    first_scale = last_scale;

    vertex          = this.label_array.coordinates;
    anchr_vertex    = this.anchr_array.coordinates;
    int vertexCount = this.label_array.vertexCount;
  }



  public void controlChanged(double first_scale, double[] scale_a)
  {
    trans.getTransform(t3d);

    double factor = 0;
    float f_scale = 0;

    if (flag == 0) { //-- label
      double k = first_scale;  //- final scale
      factor   = k/scale_a[0];
      f_scale  = (float) ((scale_a[0] - k)/scale_a[0]);
    }
    else {           //-- expanding line segments
      double k = (f_array[0]/(f_array[1]-f_array[0]))*
        (f_array[1]/f_array[0] - first_scale/scale_a[0])*scale_a[0];
      factor   = k/scale_a[0];
      f_scale  = (float) ((scale_a[0] - k)/scale_a[0]);
    }

    Vector3f trans_vec =
      new Vector3f(f_scale*anchr_vertex[0],
                   f_scale*anchr_vertex[1],
                   f_scale*anchr_vertex[2]);

    t3d.set((float)factor, trans_vec);

    trans.setTransform(t3d);
  }
}
