//
// GraphicsModeControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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


import java.rmi.*;

import java.util.StringTokenizer;

import visad.browser.Convert;


/**
 * GraphicsModeControl is the VisAD interface class for controlling various
 * mode settings for rendering.<P>

 * A GraphicsModeControl is not linked to any DisplayRealType or
 * ScalarMap.  It is linked to a DisplayImpl.<P>
 */
public abstract class GraphicsModeControl extends Control implements Cloneable {

  /** Solid line style for Display.LineStyle mapping */
  public static final int SOLID_STYLE = 0;

  /** Dash line style for Display.LineStyle mapping */
  public static final int DASH_STYLE = 1;

  /** Dot line style for Display.LineStyle mapping */
  public static final int DOT_STYLE = 2;

  /** Dash-Dot line style for Display.LineStyle mapping */
  public static final int DASH_DOT_STYLE = 3;

  /** Average color style for merging color maps */
  public static final int AVERAGE_COLOR_MODE = 0;

  /** Sum color style for merging color maps */
  public static final int SUM_COLOR_MODE = 1;

  /** 2D stack type for volume rendering */
  public static final int STACK2D = 0;

  /** 3D texture type for volume rendering */
  public static final int TEXTURE3D = 1;

  /**
   * Create a GraphicsModeControl for the display.
   *
   * @param d  DisplayImpl to use
   */
  public GraphicsModeControl(DisplayImpl d) {
    super(d);
  }

  /**
   * Get the 2D mode of the display.
   *
   * @return true if display has a 2D mode.
   */
  public abstract boolean getMode2D();

  /**
   * Get the width of line rendering.
   *
   * @return line width
   */
  public abstract float getLineWidth();

  /**
   * Set the width of line rendering; this is over-ridden by
   * ConstantMaps to Display.LineWidth.
   *
   * @param width      line width to use
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  public abstract void setLineWidth(float width)
    throws VisADException, RemoteException;

  /**
   * Set the width of line rendering, do not update the display.
   *
   * @param width      line width to use
   * @param noChange   dummy flag
   */
  public abstract void setLineWidth(float width, boolean noChange);

  /**
   * Get the size for point rendering
   *
   * @return point size
   */
  public abstract float getPointSize();

  /**
   * Set the size for point rendering; this is over-ridden by
   * ConstantMaps to Display.PointSize.
   *
   * @param size   point size
   *
   * @throws  VisADException   unable to set point size
   * @throws  RemoteException  unable to set point size on remote display
   */
  public abstract void setPointSize(float size)
    throws VisADException, RemoteException;

  /**
   * Set the size for point rendering, does not update the display.
   *
   * @param size   point size
   * @param noChange   dummy flag
   */
  public abstract void setPointSize(float size, boolean noChange);

  /**
   * Get the line style
   *
   * @return line style
   */
  public abstract int getLineStyle();

  /**
   * set the style of line rendering; this is over-ridden by
   * ConstantMaps to Display.LineStyle
   *
   * @param style  The line styles are:
   *                        <ul>
   *                        <li>GraphicsModeControl.SOLID_STYLE
   *                        <li>GraphicsModeControl.DASH_STYLE
   *                        <li>GraphicsModeControl.DOT_STYLE
   *                        <li>GraphicsModeControl.DASH_DOT_STYLE
   *                        </ul>
   * @throws  VisADException   unable to set line style
   * @throws  RemoteException  unable to set line style on remote display
   */
  public abstract void setLineStyle(int style)
    throws VisADException, RemoteException;

  /**
   * Set the style of line rendering, does not update the display.
   *
   * @param style  The line styles are:
   *                        <ul>
   *                        <li>GraphicsModeControl.SOLID_STYLE
   *                        <li>GraphicsModeControl.DASH_STYLE
   *                        <li>GraphicsModeControl.DOT_STYLE
   *                        <li>GraphicsModeControl.DASH_DOT_STYLE
   *                        </ul>
   * @param noChange   dummy flag
   */
  public abstract void setLineStyle(int style, boolean noChange);

  /**
   * Get the color mode.
   * @return color mode
   */
  public abstract int getColorMode();

  /**
   * Set the mode for merging color mappings.
   *
   * @param  mode     The color modes are:
   *                       <ul>
   *                       <li>GraphicsModeControl.AVERAGE_COLOR_MODE
   *                       <li>GraphicsModeControl.SUM_COLOR_MODE
   *                       </ul>
   * @throws  VisADException   unable to set color mode
   * @throws  RemoteException  unable to set color mode on remote display
   */
  public abstract void setColorMode(int mode)
    throws VisADException, RemoteException;

  /**
   * Get the point mode.
   *
   * @return point mode
   */
  public abstract boolean getPointMode();

  /**
   * Set the point rendering mode.
   *
   * @param mode  if true, this will cause some rendering as points
   *              rather than lines or surfaces.
   *
   * @throws  VisADException   unable to enable point mode
   * @throws  RemoteException  unable to enable point mode on remote display
   */
  public abstract void setPointMode(boolean mode)
    throws VisADException, RemoteException;

  /**
   * Get the use of texture mapping.
   * @return if true this the use of texture mapping is enabled
   */
  public abstract boolean getTextureEnable();

  /**
   * Set the use of texture mapping.
   *
   * @param enable  if true this will enable the use of texture
   *                mapping, where appropriate
   *
   * @throws  VisADException   unable to enable texture mapping
   * @throws  RemoteException  unable to enable texture mapping on remote display
   */
  public abstract void setTextureEnable(boolean enable)
    throws VisADException, RemoteException;

  /**
   * Get the use of numerical scales along display axes.
   *
   * @return true if numerical scales are enabled along display spatial axes
   */
  public abstract boolean getScaleEnable();

  /**
   * Set the use of numerical scales along display axes.
   *
   * @param enable     if true, this will enable numerical
   *                   scales along display spatial axes
   *
   * @throws  VisADException   unable to enable scales
   * @throws  RemoteException  unable to enable scales on remote display
   */
  public abstract void setScaleEnable(boolean enable)
    throws VisADException, RemoteException;

  /**
   * Gets the graphics-API-specific transparency mode (e.g.,
   * SCREEN_DOOR, BLENDED) used in the display
   *
   * @return the graphics-API-specific transparency mode
   */
  public abstract int getTransparencyMode();

  /**
   * Sets a graphics-API-specific transparency mode (e.g.,
   * SCREEN_DOOR, BLENDED) on the display.
   *
   * @param mode  graphics-API-specific transparency mode
   *
   * @throws  VisADException   Unable to change transparency mode
   * @throws  RemoteException  can't change transparency mode on remote display
   */
  public abstract void setTransparencyMode(int mode)
    throws VisADException, RemoteException;

  /**
   * Sets a graphics-API-specific projection policy (e.g.,
   * PARALLEL_PROJECTION, PERSPECTIVE_PROJECTION) for the display.
   *
   * @param   policy      policy to be used
   *
   * @throws  VisADException   bad policy or can't create the necessary VisAD
   *                           object
   * @throws  RemoteException  change policy on remote display
   */
  public abstract void setProjectionPolicy(int policy)
    throws VisADException, RemoteException;

  /**
   * Get the current graphics-API-specific projection policy for the display.
   *
   * @return  policy
   */
  public abstract int getProjectionPolicy();

  /**
   * Sets the graphics-API-specific polygon mode and updates the display
   *
   * @param  mode   the polygon mode to be used
   *
   * @throws  VisADException   bad mode or can't create the necessary VisAD
   *                           object
   * @throws  RemoteException  can't change mode on remote display
   */
  public abstract void setPolygonMode(int mode)
    throws VisADException, RemoteException;

  /**
   * Sets the graphics-API-specific polygon mode.  Does not update the display.
   *
   * @param  mode   the polygon mode to be used
   * @param noChange   dummy flag
   *
   * @throws  VisADException   bad mode or can't create the necessary VisAD
   *                           object
   * @throws  RemoteException  can't change mode on remote display
   */
  public abstract void setPolygonMode(int mode, boolean noChange)
    throws VisADException, RemoteException;

  /**
   * Get the Polygon mode
   *
   * @return  the polygon mode
   */
  public abstract int getPolygonMode();

  /**
   * Sets the polygon offset and updates the display.
   *
   * @param  polygonOffset   the polygon offset to be used
   *
   * @throws  VisADException   Unable to change offset
   * @throws  RemoteException  can't change offset on remote display
   */
  public abstract void setPolygonOffset(float polygonOffset)
    throws VisADException, RemoteException;

  /**
   * Sets the polygon offset.  Does not update the display.
   *
   * @param  polygonOffset   the polygon offset to be used
   * @param  noChange   dummy variable
   */
  public abstract void setPolygonOffset(float polygonOffset,
                                        boolean noChange);

  /**
   * Get the current polygon offset.
   *
   * @return  offset
   */
  public abstract float getPolygonOffset();

  /**
   * Sets the polygon offset factor and updates the display.
   *
   * @param factor  the polygon offset factor to be used
   *
   * @throws  VisADException   Unable to change offset factor
   * @throws  RemoteException  can't change offset factor on remote display
   */
  public abstract void setPolygonOffsetFactor(float factor)
    throws VisADException, RemoteException;

  /**
   * Sets the polygon offset factor, does not update display.
   *
   * @param factor  the polygon offset to be used
   * @param  noChange   dummy variable
   */
  public abstract void setPolygonOffsetFactor(float factor, boolean noChange);

  /**
   * Get the current polygon offset factor.
   *
   * @return  offset factor
   */
  public abstract float getPolygonOffsetFactor();
  
  /**
   * Set the transparency of missing values.
   *
   * @param  missing   true if missing values should be rendered transparent.
   *
   * @throws  VisADException   Unable to change missing transparent
   * @throws  RemoteException  can't change missing transparent on remote display
   */
  public abstract void setMissingTransparent(boolean missing)
    throws VisADException, RemoteException;

  /**
   * See whether missing values are rendered as transparent or not.
   *
   * @return  true if missing values are transparent.
   */
  public abstract boolean getMissingTransparent();

  /**
   * Set whether or not to call methods to adjust the projection seam
   * (VisADGeometryArray.adjustLongitude/adjustSeam);
   *
   * @param  adjust   true if seams should be adjusted
   *
   * @throws  VisADException   Unable to change adjust seam property
   * @throws  RemoteException  can't change adjust seam property on remote display
   */
  public abstract void setAdjustProjectionSeam(boolean adjust)
    throws VisADException, RemoteException;

  /**
   * See whether or not to call methods to adjust the projection seam
   * (VisADGeometryArray.adjustLongitude/adjustSeam);
   *
   * @return  true if adjust seam methods should be called
   */
  public abstract boolean getAdjustProjectionSeam();

  /**
   * Set the mode for Texture3D for volume rendering
   *
   * @param  mode   mode for Texture3D (STACK2D or TEXTURE3D)
   *
   * @throws  VisADException   Unable to change Texture3D mode
   * @throws  RemoteException  can't change Texture3D mode on remote display
   */
  public abstract void setTexture3DMode(int mode)
    throws VisADException, RemoteException;

  /**
   * Get the mode for Texture3D for volume rendering
   *
   * @return  mode for Texture3D (e.g., STACK2D or TEXTURE3D)
   */
  public abstract int getTexture3DMode();

  /**
   * Set the undersampling factor of surface shape for curved texture maps
   *
   * @param  curved_size  undersampling factor (default 10)
   *
   * @throws  VisADException   Unable to change curved size
   * @throws  RemoteException  can't change curved size on remote display
   */
  public abstract void setCurvedSize(int curved_size);

  /**
   * Get the undersampling factor of surface shape for curved texture maps
   *
   * @return  undersampling factor (default 10)
   */
  public abstract int getCurvedSize();

  /**
   * Set whether the Appearances are reused
   *
   * @param  cache   true to cache and reuse appearances
   */
  public abstract void setCacheAppearances(boolean cache);

  /**
   * Get whether Appearances are cached or not
   *
   * @return  true if caching
   */
  public abstract boolean getCacheAppearances();

  /**
   * Set whether Geometries for shapes should be merged into Group if
   * possible to reduce memory use.
   *
   * @param  merge   true to merge geometries if possible
   */
  public abstract void setMergeGeometries(boolean merge);

  /**
   * Set whether Geometries for shapes should be merged into Group
   *
   * @return  true if merging is used
   */
  public abstract boolean getMergeGeometries();

  /**
   * Get a string that can be used to reconstruct this control later
   * @return save string
   */
  public String getSaveString() {
    return "" + getLineWidth() + " " 
           + getPointSize() + " " +
           getPointMode() + " " + 
           getTextureEnable() + " " +
           getScaleEnable() + " " + 
           getTransparencyMode() + " " +
           getProjectionPolicy() + " " + 
           getPolygonMode() + " " +
           getMissingTransparent() + " " + 
           getCurvedSize() + " " +
           getLineStyle() + " " + 
           getColorMode() + " " + 
           getPolygonOffset() + " " + 
           getPolygonOffsetFactor() + " " + 
           getAdjustProjectionSeam() + " " + 
           getCacheAppearances() + " " + 
           getMergeGeometries();
  }

  /**
   * Reconstruct this control using the specified save string
   * @param save  save string
   *
   * @throws RemoteException 
   * @throws VisADException 
   */
  public void setSaveString(String save)
          throws VisADException, RemoteException {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    int numTokens = st.countTokens();
    if (numTokens < 10) throw new VisADException("Invalid save string");

    // determine graphics mode settings
    float lw = Convert.getFloat(st.nextToken());
    float ps = Convert.getFloat(st.nextToken());
    boolean pm = Convert.getBoolean(st.nextToken());
    boolean te = Convert.getBoolean(st.nextToken());
    boolean se = Convert.getBoolean(st.nextToken());
    int tm = Convert.getInt(st.nextToken());
    int pp = Convert.getInt(st.nextToken());
    int pm2 = Convert.getInt(st.nextToken());
    boolean mt = Convert.getBoolean(st.nextToken());
    int cs = Convert.getInt(st.nextToken());

    int ls = st.hasMoreTokens()
             ? Convert.getInt(st.nextToken())
             : SOLID_STYLE;
    int cm = st.hasMoreTokens()
             ? Convert.getInt(st.nextToken())
             : 0;
    float po = st.hasMoreTokens()
               ? Convert.getFloat(st.nextToken())
               : Float.NaN;
    float pof = st.hasMoreTokens()
                ? Convert.getFloat(st.nextToken())
                : 0;

    boolean as = st.hasMoreTokens()
                 ? Convert.getBoolean(st.nextToken())
                 : getAdjustProjectionSeam();
    int t3dm = st.hasMoreTokens()
               ? Convert.getInt(st.nextToken())
               : 0;
    boolean ca = st.hasMoreTokens()
                 ? Convert.getBoolean(st.nextToken())
                 : getCacheAppearances();
    boolean mg = st.hasMoreTokens()
                 ? Convert.getBoolean(st.nextToken())
                 : getMergeGeometries();


    // reset graphics mode settings
    setLineWidth(lw);
    setPointSize(ps);
    setLineStyle(ls);
    setPointMode(pm);
    setTextureEnable(te);
    setScaleEnable(se);
    setTransparencyMode(tm);
    setProjectionPolicy(pp);
    setPolygonMode(pm2);
    setMissingTransparent(mt);
    setCurvedSize(cs);
    setColorMode(cm);
    setPolygonOffset(po);
    setPolygonOffsetFactor(pof);
    setAdjustProjectionSeam(as);
    setTexture3DMode(t3dm);
    setCacheAppearances(ca);
    setMergeGeometries(mg);
  }

  /**
   * A method to copy this object
   *
   * @return a copy of this object
   */
  public abstract Object clone();

}

