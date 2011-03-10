//
// GraphicsModeControlJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;

import javax.media.j3d.*;

import visad.util.Util;

/**
   GraphicsModeControlJ3D is the VisAD class for controlling various
   mode settings for rendering.<P>

   A GraphicsModeControlJ3D is not linked to any DisplayRealType or
   ScalarMap.  It is linked to a DisplayImpl.<P>
*/
public class GraphicsModeControlJ3D extends GraphicsModeControl {

  /** maps GraphicsModeControl line styles to LineAttributes line patterns */
  static final int[] LINE_PATTERN = {
    LineAttributes.PATTERN_SOLID, LineAttributes.PATTERN_DASH,
    LineAttributes.PATTERN_DOT, LineAttributes.PATTERN_DASH_DOT
  };

  /** for LineAttributes; >= 1.0  @serial */
  private float lineWidth;
  /** for PointAttributes; >= 1.0  @serial */
  private float pointSize;
  /** for LineAttributes; = solid, dash, dot or dash-dot  @serial */
  private int lineStyle;

  /** color combination mode for multiple color mappings */
  private int colorMode;
  /** true => points in place of lines and surfaces, @serial */
  private boolean pointMode;
  /** true => allow use of texture mapping @serial*/
  private boolean textureEnable;
  /** true => display X, Y and Z scales @serial*/
  private boolean scaleEnable;

  /** for TransparencyAttributes; see list below in setTransparencyMode
      @serial */
  private int transparencyMode;
  /** View.PARALLEL_PROJECTION or View.PERSPECTIVE_PROJECTION @serial */
  private int projectionPolicy;
  private boolean anti_alias_flag = false;
  /** PolygonAttributes.POLYGON_FILL, PolygonAttributes.POLYGON_LINE
      or PolygonAttributes.POLYGON_POINT @serial */
  private int polygonMode;

  private float polygonOffset;

  private float polygonOffsetFactor;

  /** for rendering missing data as transparent  @serial */
  private boolean missingTransparent = true;
  /** for undersampling of curved texture maps @serial */
  private int curvedSize = 10;

  /** true to adjust projection seam */
  private boolean adjustProjectionSeam;

  /** mode for Texture3D */
  private int texture3DMode;

  /** for caching Appearances*/
  private boolean cacheAppearances = false;

  /** for merging geometries */
  private boolean mergeGeometries = false;

  /**
   * Construct a GraphicsModeControlJ3D associated with the input display
   *
   * @param  d  display associated with this GraphicsModeControlJ3D
   */
  public GraphicsModeControlJ3D(DisplayImpl d) {
    super(d);
    lineWidth = 1.0f;
    pointSize = 1.0f;
    lineStyle = SOLID_STYLE;
    pointMode = false;
    textureEnable = true;
    scaleEnable = false;
    // NICEST, FASTEST and BLENDED do not solve the depth precedence problem
    // note SCREEN_DOOR does not seem to work with variable transparency
    // transparencyMode = TransparencyAttributes.NICEST;
    transparencyMode = TransparencyAttributes.FASTEST;
    // transparencyMode = TransparencyAttributes.BLENDED;
    // transparencyMode = TransparencyAttributes.SCREEN_DOOR;
    polygonMode = PolygonAttributes.POLYGON_FILL;
    polygonOffset = Float.NaN;
    polygonOffsetFactor = 0f;
    adjustProjectionSeam = true;
    texture3DMode = STACK2D;

    projectionPolicy = View.PERSPECTIVE_PROJECTION;
    DisplayRendererJ3D displayRenderer =
      (DisplayRendererJ3D) getDisplayRenderer();
    if (displayRenderer != null) {
      if (displayRenderer.getMode2D()) {
        projectionPolicy = View.PARALLEL_PROJECTION;
        // for some strange reason, if we set PERSPECTIVE_PROJECTION at this
        // point, we can never set PARALLEL_PROJECTION
        displayRenderer.getView().setProjectionPolicy(projectionPolicy);
      }
    }
  }

  /**
   * See if the display is being rendered in 2D mode
   * @see visad.DisplayRenderer#getMode2D
   *
   * @return  true if display is rendered as 2D
   */
  public boolean getMode2D() {
    return getDisplayRenderer().getMode2D();
  }

  /**
   * Get the current line width used for LineAttributes.  The default
   * is 1.0.
   *
   * @return  line width (>= 1.0)
   */
  public float getLineWidth() {
    return lineWidth;
  }

  /**
   * Set the line width used for LineAttributes.  Calls changeControl
   * and resets the display.
   *
   * @param width  width to use (>= 1.0)
   *
   * @throws  VisADException   couldn't set the line width on local display
   * @throws  RemoteException  couldn't set the line width on remote display
   */
  public void setLineWidth(float width)
         throws VisADException, RemoteException {
    if (width < 1.0f) width = 1.0f;
    if (Util.isApproximatelyEqual(lineWidth, width)) return;
    lineWidth = width;

    // WLH 2 Dec 2002 in response to qomo2.txt
    DisplayRendererJ3D dr = (DisplayRendererJ3D) getDisplayRenderer();
    dr.setLineWidth(width);

    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Set the line width used for LineAttributes.   Does not update
   * display.
   *
   * @param width  width to use (>= 1.0)
   * @param noChange dummy variable
   */
  public void setLineWidth(float width, boolean noChange) {
    if (width < 1.0f) width = 1.0f;
    lineWidth = width;
  }

  /**
   * Get the current point size used for PointAttributes.  The default
   * is 1.0.
   *
   * @return  point size  (>= 1.0)
   */
  public float getPointSize() {
    return pointSize;
  }

  /**
   * Set the point size used for PointAttributes.  Calls changeControl
   * and updates the display.
   *
   * @param size  size to use (>= 1.0)
   *
   * @throws  VisADException   couldn't set the point size on local display
   * @throws  RemoteException  couldn't set the point size on remote display
   */
  public void setPointSize(float size)
         throws VisADException, RemoteException {
    if (size < 1.0f) size = 1.0f;
    if (Util.isApproximatelyEqual(size, pointSize)) return;
    pointSize = size;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Set the point size used for PointAttributes.  Doesn't update
   * the display.
   *
   * @param size  size to use (>= 1.0)
   * @param noChange dummy variable
   */
  public void setPointSize(float size, boolean noChange) {
    if (size < 1.0f) size = 1.0f;
    pointSize = size;
  }

  /**
   * Get the current line style used for LineAttributes.  The default
   * is GraphicsModeControl.SOLID_STYLE.
   *
   * @return  line style (SOLID_STYLE, DASH_STYLE, DOT_STYLE or DASH_DOT_STYLE)
   */
  public int getLineStyle() {
    return lineStyle;
  }

  /**
   * Set the line style used for LineAttributes.  Calls changeControl
   * and resets the display.
   *
   * @param style  style to use (SOLID_STYLE, DASH_STYLE,
   *               DOT_STYLE or DASH_DOT_STYLE)
   *
   * @throws  VisADException   couldn't set the line style on local display
   * @throws  RemoteException  couldn't set the line style on remote display
   */
  public void setLineStyle(int style)
         throws VisADException, RemoteException {
    if (style == lineStyle) return;
    if (style != SOLID_STYLE && style != DASH_STYLE &&
      style != DOT_STYLE && style != DASH_DOT_STYLE)
    {
      style = SOLID_STYLE;
    }
    lineStyle = style;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Set the line style used for LineAttributes.   Does not update display.
   *
   * @param style  style to use (SOLID_STYLE, DASH_STYLE,
   *               DOT_STYLE or DASH_DOT_STYLE)
   * @param noChange dummy variable
   */
  public void setLineStyle(int style, boolean noChange) {
    if (style != SOLID_STYLE && style != DASH_STYLE &&
      style != DOT_STYLE && style != DASH_DOT_STYLE)
    {
      style = SOLID_STYLE;
    }
    lineStyle = style;
  }

  /**
   * Get the color mode used for combining color values.  The default
   * is GraphicsModeControl.AVERAGE_COLOR_MODE.
   *
   * @return  color mode (AVERAGE_COLOR_MODE or SUM_COLOR_MODE)
   */
  public int getColorMode() {
    return colorMode;
  }

  /**
   * Set the color mode used for combining color values.
   *
   * @param mode  mode to use (AVERAGE_COLOR_MODE or SUM_COLOR_MODE)
   */
  public void setColorMode(int mode)
         throws VisADException, RemoteException {
    if (mode != AVERAGE_COLOR_MODE && mode != SUM_COLOR_MODE) {
      mode = AVERAGE_COLOR_MODE;
    }
    if (mode == colorMode) return;
    colorMode = mode;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Gets the point mode.
   *
   * @return  True if the display is using points rather than connected
   *          lines or surfaces for rendering.
   */
  public boolean getPointMode() {
    return pointMode;
  }

  /**
   * Sets the point mode and updates the display.
   *
   * @param mode         true if the display should use points rather
   *                     than connected lines or surfaces for rendering.
   */
  public void setPointMode(boolean mode)
         throws VisADException, RemoteException {
    if (mode == pointMode) return;
    pointMode = mode;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Set whether texture mapping should be used or not.
   *
   * @param  enable   true to use texture mapping (the default)
   */
  public void setTextureEnable(boolean enable)
         throws VisADException, RemoteException {
    if (enable == textureEnable) return;
    textureEnable = enable;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * See if texture mapping is enabled or not
   *
   * @return    true if texture mapping is enabled.
   */
  public boolean getTextureEnable() {
    return textureEnable;
  }

  /**
   *  Toggle the axis scales in the display
   *
   * @param  enable    true to enable, false to disable
   *
   * @throws  VisADException   couldn't change state of scale enablement
   * @throws  RemoteException  couldn't change state of scale enablement on
   *                           remote display
   */
  public void setScaleEnable(boolean enable)
         throws VisADException, RemoteException {
    if (enable == scaleEnable) return;
    scaleEnable = enable;
    getDisplayRenderer().setScaleOn(enable);
    changeControl(true);
  }

  /**
   * Get whether display scales are enabled or not
   *
   * @return  true if enabled, otherwise false
   */
  public boolean getScaleEnable() {
    return scaleEnable;
  }

  /**
   * Get the current transparency mode
   *
   * @return  DisplayImplJ3D.FASTEST, DisplayImplJ3D.NICEST
   */
  public int getTransparencyMode() {
    return transparencyMode;
  }

  /**
   * Sets the transparency mode.
   *
   * @param   mode   transparency mode to use.   Legal values are
   *                 DisplayImplJ3D.FASTEST, DisplayImplJ3D.NICEST
   * @throws  VisADException    bad mode or couldn't create necessary VisAD
   *                            object
   * @throws  RemoteException   couldn't create necessary remote object
   */
  public void setTransparencyMode(int mode)
         throws VisADException, RemoteException {
    if (mode == transparencyMode) return;
    if (mode == TransparencyAttributes.SCREEN_DOOR ||
        mode == TransparencyAttributes.BLENDED ||
        mode == TransparencyAttributes.NONE ||
        mode == TransparencyAttributes.FASTEST ||
        mode == TransparencyAttributes.NICEST) {
      transparencyMode = mode;
      changeControl(true);
      getDisplay().reDisplayAll();
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setTransparencyMode: bad mode");
    }
  }

  /**
   * Sets the projection policy for the display.  PARALLEL_PROJECTION will
   * display a parallel view while PERSPECTIVE_PROJECTION will create a
   * perspective view.  The default is a perspective view.
   *
   * @param   policy      policy to be used (DisplayImplJ3D.PARALLEL_PROJECTION
   *                      or DisplayImplJ3D.PERSPECTIVE_PROJECTION
   *
   * @throws  VisADException   bad policy or can't create the necessary VisAD
   *                           object
   * @throws  RemoteException  change policy on remote display
   */
  public void setProjectionPolicy(int policy)
         throws VisADException, RemoteException {
    if (policy == projectionPolicy) return;
    if (policy == View.PARALLEL_PROJECTION ||
        policy == View.PERSPECTIVE_PROJECTION) {
      projectionPolicy = policy;
      DisplayRendererJ3D displayRenderer =
        (DisplayRendererJ3D) getDisplayRenderer();
      if (displayRenderer != null) {
        displayRenderer.getView().setProjectionPolicy(projectionPolicy);
      }
      changeControl(true);
      getDisplay().reDisplayAll();
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setProjectionPolicy: bad policy");
    }
  }

  /**
   * Get the current projection policy for the display.
   *
   * @return  DisplayImplJ3D.PARALLEL_PROJECTION or
   *          DisplayImplJ3D.PERSPECTIVE_PROJECTION
   */
  public int getProjectionPolicy() {
    return projectionPolicy;
  }

  /**
   * Sets the antialiasing flag for the display.
   *
   * @param   flag      true to enable antialiasing
   *
   * @throws  VisADException   a VisAD error occurred
   * @throws  RemoteException  change policy on remote display
   */
  public void setSceneAntialiasingEnable(boolean flag)
         throws VisADException, RemoteException {
    if (flag == anti_alias_flag) return;
    anti_alias_flag = flag;
    DisplayRendererJ3D displayRenderer =
      (DisplayRendererJ3D) getDisplayRenderer();
    if (displayRenderer != null) {
      displayRenderer.getView().setSceneAntialiasingEnable(anti_alias_flag);
    }
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Get the current antialiasing flag for the display.
   *
   * @return  true or false
   */
  public boolean getSceneAntialiasingEnable() {
    return anti_alias_flag;
  }

  /**
   * Sets the polygon rasterization mode.
   *
   * @param  mode   the polygon rasterization mode to be used; one of
   *                DisplayImplJ3D.POLYGON_FILL, DisplayImplJ3D.POLYGON_LINE,
   *                or DisplayImplJ3D.POLYGON_POINT
   *
   * @throws  VisADException   bad mode or can't create the necessary VisAD
   *                           object
   * @throws  RemoteException  can't change mode on remote display
   */
  public void setPolygonMode(int mode)
         throws VisADException, RemoteException {
    if (mode == polygonMode) return;
    if (mode == PolygonAttributes.POLYGON_FILL ||
        mode == PolygonAttributes.POLYGON_LINE ||
        mode == PolygonAttributes.POLYGON_POINT) {
      polygonMode = mode;
      changeControl(true);
      getDisplay().reDisplayAll();
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setPolygonMode: bad mode");
    }
  }

  /**
   * Sets the polygon rasterization mode. Does not update display
   *
   * @param  mode   the polygon rasterization mode to be used; one of
   *                DisplayImplJ3D.POLYGON_FILL, DisplayImplJ3D.POLYGON_LINE,
   *                or DisplayImplJ3D.POLYGON_POINT
   * @param noChange dummy variable
   *
   * @throws  VisADException   bad mode or can't create the necessary VisAD
   *                           object
   * @throws  RemoteException  can't change mode on remote display
   */
  public void setPolygonMode(int mode, boolean noChange)
         throws VisADException, RemoteException {
    if (mode == polygonMode) return;
    if (mode == PolygonAttributes.POLYGON_FILL ||
        mode == PolygonAttributes.POLYGON_LINE ||
        mode == PolygonAttributes.POLYGON_POINT) {
      polygonMode = mode;
    }
  }

  /**
   * Get the current polygon rasterization mode.
   *
   * @return  DisplayImplJ3D.POLYGON_FILL, DisplayImplJ3D.POLYGON_LINE,
   *          or DisplayImplJ3D.POLYGON_POINT
   */
  public int getPolygonMode() {
    return polygonMode;
  }

  /**
   * Sets the polygon offset and updates the display.
   *
   * @param  polygonOffset   the polygon offset to be used
   *
   * @throws  VisADException   Unable to change offset
   * @throws  RemoteException  can't change offset on remote display
   */
  public void setPolygonOffset(float polygonOffset) 
         throws VisADException, RemoteException {
    if (Float.isNaN(polygonOffset)) return;
    if (Util.isApproximatelyEqual(this.polygonOffset, polygonOffset)) return;
    this.polygonOffset = polygonOffset;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Sets the polygon offset.  Does not update the display.
   *
   * @param  polygonOffset   the polygon offset to be used
   * @param  noChange   dummy variable
   *
   * @throws  VisADException   Unable to change offset
   * @throws  RemoteException  can't change offset on remote display
   */
  public void setPolygonOffset(float polygonOffset, boolean noChange) {
    this.polygonOffset = polygonOffset;
  }

  /**
   * Get the current polygon offset.
   *
   * @return  offset 
   */
  public float getPolygonOffset() {
     return polygonOffset;
  }

  /**
   * Sets the polygon offset factor and updates the display.
   *
   * @param  polygonOffsetFactor   the polygon offset factor to be used
   *
   * @throws  VisADException   Unable to change offset factor
   * @throws  RemoteException  can't change offset factor on remote display
   */
  public void setPolygonOffsetFactor(float polygonOffsetFactor) 
         throws VisADException, RemoteException {
    if (Float.isNaN(polygonOffsetFactor)) return;
    if (Util.isApproximatelyEqual(this.polygonOffsetFactor, polygonOffsetFactor)) return;
    this.polygonOffsetFactor = polygonOffsetFactor;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Sets the polygon offset factor, does not update display.
   *
   * @param  polygonOffsetFactor  the polygon offset to be used
   * @param  noChange   dummy variable
   *
   * @throws  VisADException   Unable to change offset factor
   * @throws  RemoteException  can't change offset factor on remote display
   */
  public void setPolygonOffsetFactor(float polygonOffsetFactor, boolean noChange)  {
    this.polygonOffsetFactor = polygonOffsetFactor;
  }

  /**
   * Get the current polygon offset factor.
   *
   * @return  offset factor
   */
  public float getPolygonOffsetFactor() {
    return polygonOffsetFactor;
  }


  /**
   * See whether missing values are rendered as transparent or not.
   *
   * @return  true if missing values are transparent.
   */
  public boolean getMissingTransparent() {
    return missingTransparent;
  }

  /**
   * Set the transparency of missing values.
   *
   * @param  missing   true if missing values should be rendered transparent.
   */
  public void setMissingTransparent(boolean missing) {
    missingTransparent = missing;
  }

  /**
   * Get the undersampling factor of surface shape for curved texture maps
   *
   * @return  undersampling factor (default 10)
   */
  public int getCurvedSize() {
    return curvedSize;
  }

  /**
   * Set the undersampling factor of surface shape for curved texture maps
   *
   * @param  curved_size  undersampling factor (default 10)
   */
  public void setCurvedSize(int curved_size) {
    curvedSize = curved_size;
  }

  /**
   * See whether VisADGeometryArray.adjustLongitude/Seam should be called.
   *
   * @return  true if adjust methods should be called
   */
  public boolean getAdjustProjectionSeam() {
    return adjustProjectionSeam;
  }

  /**
   * Set whether VisADGeometryArray.adjustLongitude/adjustSeam should be called.
   *
   * @param  adjust  true if adjust methods should be called
   */
  public void setAdjustProjectionSeam(boolean adjust) 
         throws VisADException, RemoteException {
    if (adjustProjectionSeam == adjust) return;
    adjustProjectionSeam = adjust;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Set the mode for Texture3D for volume rendering
   *
   * @param  mode   mode for Texture3D (STACK2D or TEXTURE3D)
   *
   * @throws  VisADException   Unable to change Texture3D mode
   * @throws  RemoteException  can't change Texture3D mode on remote display
   */
  public void setTexture3DMode(int mode)
         throws VisADException, RemoteException {
    if (texture3DMode == mode) return;
    texture3DMode = mode;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  /**
   * Get the mode for Texture3D for volume rendering
   *
   * @return  mode for Texture3D (e.g., STACK2D or TEXTURE3D)
   */
  public int getTexture3DMode() {
    return texture3DMode;
  }

  /**
   * Set whether the Appearances are reused
   *
   * @param  cache   true to cache and reuse appearances
   *
   * @throws  VisADException   Unable to change caching
   * @throws  RemoteException  can't change caching on remote display
   */
  public void setCacheAppearances(boolean cache) {
    cacheAppearances = cache;
  }

  /**
   * Get whether Appearances are cached or not
   *
   * @return  true if caching
   */
  public boolean getCacheAppearances() {
    return cacheAppearances;
  }

  /**
   * Set whether Geometries for shapes should be merged into Group if
   * possible to reduce memory use.
   *
   * @param  merge   true to merge geometries if possible
   *
   * @throws  VisADException   Unable to change caching
   * @throws  RemoteException  can't change caching on remote display
   */
  public void setMergeGeometries(boolean merge) {
    mergeGeometries = merge;
  }

  /**
   * Set whether Geometries for shapes should be merged into Group
   *
   * @return  true if merging is used
   */
  public boolean getMergeGeometries() {
    return mergeGeometries;
  }

  /** clone this GraphicsModeControlJ3D */
  public Object clone() {
    GraphicsModeControlJ3D mode =
      new GraphicsModeControlJ3D(getDisplay());
    mode.lineWidth = lineWidth;
    mode.pointSize = pointSize;
    mode.pointMode = pointMode;
    mode.textureEnable = textureEnable;
    mode.scaleEnable = scaleEnable;
    mode.transparencyMode = transparencyMode;
    mode.projectionPolicy = projectionPolicy;
    mode.missingTransparent = missingTransparent;
    mode.polygonMode = polygonMode;
    mode.polygonOffset = polygonOffset;
    mode.polygonOffsetFactor = polygonOffsetFactor;
    mode.curvedSize = curvedSize;
    mode.lineStyle = lineStyle;
    mode.anti_alias_flag = anti_alias_flag;
    mode.adjustProjectionSeam = adjustProjectionSeam;
    mode.texture3DMode = texture3DMode;
    mode.cacheAppearances = cacheAppearances;
    mode.mergeGeometries = mergeGeometries;
    return mode;
  }

  /**
   * Copy the state of a remote control to this control
   *
   * @param  rmt   remote control to sync with this one
   * @throws  VisADException  rmt == null or rmt is not a
   *                          GraphicsModeControlJ3D or couldn't tell if
   *                          control was changed.
   */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof GraphicsModeControlJ3D)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    GraphicsModeControlJ3D rmtCtl = (GraphicsModeControlJ3D )rmt;

    boolean changed = false;
    boolean redisplay = false;

    if (!Util.isApproximatelyEqual(lineWidth, rmtCtl.lineWidth)) {
      changed = true;
      redisplay = true;
      lineWidth = rmtCtl.lineWidth;
    }
    if (!Util.isApproximatelyEqual(pointSize, rmtCtl.pointSize)) {
      changed = true;
      redisplay = true;
      pointSize = rmtCtl.pointSize;
    }

    if (pointMode != rmtCtl.pointMode) {
      changed = true;
      redisplay = true;
      pointMode = rmtCtl.pointMode;
    }
    if (textureEnable != rmtCtl.textureEnable) {
      changed = true;
      redisplay = true;
      textureEnable = rmtCtl.textureEnable;
    }
    if (scaleEnable != rmtCtl.scaleEnable) {
      changed = true;
      getDisplayRenderer().setScaleOn(scaleEnable);
      scaleEnable = rmtCtl.scaleEnable;
    }

    if (transparencyMode != rmtCtl.transparencyMode) {
      changed = true;
      redisplay = true;
      transparencyMode = rmtCtl.transparencyMode;
    }

    if (adjustProjectionSeam != rmtCtl.adjustProjectionSeam) {
      changed = true;
      redisplay = true;
      adjustProjectionSeam = rmtCtl.adjustProjectionSeam;
    }

    if (texture3DMode != rmtCtl.texture3DMode) {
      changed = true;
      redisplay = true;
      texture3DMode = rmtCtl.texture3DMode;
    }

    if (cacheAppearances != rmtCtl.cacheAppearances) {
      changed = true;
      cacheAppearances = rmtCtl.cacheAppearances;
    }

    if (mergeGeometries != rmtCtl.mergeGeometries) {
      changed = true;
      mergeGeometries = rmtCtl.mergeGeometries;
    }

    if (projectionPolicy != rmtCtl.projectionPolicy) {
      changed = true;
      redisplay = true;
      projectionPolicy = rmtCtl.projectionPolicy;

      DisplayRendererJ3D displayRenderer;
      displayRenderer = (DisplayRendererJ3D) getDisplayRenderer();
      if (displayRenderer != null) {
        displayRenderer.getView().setProjectionPolicy(projectionPolicy);
      }
    }
    if (polygonMode != rmtCtl.polygonMode) {
      changed = true;
      polygonMode = rmtCtl.polygonMode;
    }

    if (missingTransparent != rmtCtl.missingTransparent) {
      changed = true;
      missingTransparent = rmtCtl.missingTransparent;
    }

    if (curvedSize != rmtCtl.curvedSize) {
      changed = true;
      curvedSize = rmtCtl.curvedSize;
    }

    if (!Util.isApproximatelyEqual(polygonOffset, rmtCtl.polygonOffset)) {
      changed = true;
      polygonOffset = rmtCtl.polygonOffset;
    }

    if (!Util.isApproximatelyEqual(polygonOffsetFactor, rmtCtl.polygonOffsetFactor)) {
      changed = true;
      polygonOffsetFactor = rmtCtl.polygonOffsetFactor;
    }

    if (anti_alias_flag != rmtCtl.anti_alias_flag) {
      changed = true;
      anti_alias_flag = rmtCtl.anti_alias_flag;
    }

    if (colorMode != rmtCtl.colorMode) {
      changed = true;
      colorMode = rmtCtl.colorMode;
    }

    if (lineStyle != rmtCtl.lineStyle) {
      changed = true;
      lineStyle = rmtCtl.lineStyle;
    }

    if (changed) {
      try {
        changeControl(true);
      } catch (RemoteException re) {
        throw new VisADException("Could not indicate that control" +
                                 " changed: " + re.getMessage());
      }
    }
    if (redisplay) {
      getDisplay().reDisplayAll();
    }
  }

  /**
   * Check to see if this GraphicsModeControlJ3D is equal to the object
   * in question.
   *
   * @param   o   object in question
   *
   * @return  false if the objects are not the same and/or their states
   *          are not equal.
   */
  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    GraphicsModeControlJ3D gmc = (GraphicsModeControlJ3D )o;

    boolean changed = false;

    if (!Util.isApproximatelyEqual(lineWidth, gmc.lineWidth)) {
      return false;
    }
    if (!Util.isApproximatelyEqual(pointSize, gmc.pointSize)) {
      return false;
    }

    if (pointMode != gmc.pointMode) {
      return false;
    }
    if (textureEnable != gmc.textureEnable) {
      return false;
    }
    if (scaleEnable != gmc.scaleEnable) {
      return false;
    }

    if (transparencyMode != gmc.transparencyMode) {
      return false;
    }
    if (projectionPolicy != gmc.projectionPolicy) {
      return false;
    }
    if (polygonMode != gmc.polygonMode) {
      return false;
    }

    if (missingTransparent != gmc.missingTransparent) {
      return false;
    }

    if (curvedSize != gmc.curvedSize) {
      return false;
    }

    if (anti_alias_flag != gmc.anti_alias_flag) {
      return false;
    }

    if (colorMode != colorMode) {
      return false;
    }

    if (lineStyle != lineStyle) {
      return false;
    }

    if (!Util.isApproximatelyEqual(polygonOffset, gmc.polygonOffset)) {
      return false;
    }

    if (!Util.isApproximatelyEqual(polygonOffsetFactor, gmc.polygonOffsetFactor)) {
      return false;
    }

    if (adjustProjectionSeam != gmc.adjustProjectionSeam) {
      return false;
    }

    if (texture3DMode != gmc.texture3DMode) {
      return false;
    }

    if (cacheAppearances != gmc.cacheAppearances) {
      return false;
    }

    if (mergeGeometries != gmc.mergeGeometries) {
      return false;
    }

    return true;
  }

  /**
   * Return a string representation of this GraphicsModeControlJ3D
   *
   * @return  string that represents the state of this object.
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer("GraphicsModeControlJ3D[");

    buf.append("lw ");
    buf.append(lineWidth);
    buf.append(",ps ");
    buf.append(pointSize);

    buf.append(pointMode ? "pm" : "!pm");
    buf.append(textureEnable ? "te" : "!te");
    buf.append(scaleEnable ? "se" : "!se");
    buf.append(missingTransparent ? "mt" : "!mt");

    buf.append(",tm ");
    buf.append(transparencyMode);
    buf.append(",pp ");
    buf.append(projectionPolicy);
    buf.append(",pm ");
    buf.append(polygonMode);
    buf.append(",cm ");
    buf.append(colorMode);
    buf.append(",cs ");
    buf.append(curvedSize);
    buf.append(",po ");
    buf.append(polygonOffset);
    buf.append(",pof ");
    buf.append(polygonOffsetFactor);
    buf.append(adjustProjectionSeam ? "as" : "!as");
    buf.append(",t3dm ");
    buf.append(texture3DMode);
    buf.append(",ca ");
    buf.append(cacheAppearances);
    buf.append(",mg ");
    buf.append(mergeGeometries);

    buf.append(']');
    return buf.toString();
  }
}
