//
// DisplayRenderer.java
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

import java.awt.*;
import java.awt.image.BufferedImage;

import java.rmi.RemoteException;

import java.util.*;


/**
 * <CODE>DisplayRenderer</CODE> is the VisAD abstract super-class for
 * background and metadata rendering algorithms.  These complement
 * depictions of <CODE>Data</CODE> objects created by
 * <CODE>DataRenderer</CODE> objects.<P>
 *
 * <CODE>DisplayRenderer</CODE> also manages the overall relation of
 * <CODE>DataRenderer</CODE> output to the graphics library.<P>
 *
 * <CODE>DisplayRenderer</CODE> is not <CODE>Serializable</CODE> and
 * should not be copied between JVMs.<P>
 */
public abstract class DisplayRenderer
  implements ControlListener
{

  /** DisplayImpl this renderer is attached to. */
  private transient DisplayImpl display;

  /** RendererControl holds the shared renderer data */
  private transient RendererControl rendererControl = null;

  /** Vector of Strings describing cursor location */
  private Vector cursorStringVector = new Vector();

  /** Strings to display during next frame of animation. */
  String[] animationString = {null, null};

  /** Number of scales allocated on each axis. */
  private int[] axisOrdinals = {-1, -1, -1};

  /** Set to true when the wait message should be displayed. */
  private boolean waitFlag = false;

  /** Set to true if the cursor location Strings should be displayed. */
  private boolean cursor_string = true;

  /** threshhold for direct manipulation picking */
  private float pickThreshhold = 0.05f;

  /** Set to true to make animation Strings visible on the display */
  private boolean aniStringVisible = true;

  /** Set to true to make please wait Strings visible on the display */
  private boolean waitMessageVisible = true;

  /**
   * Construct a new <CODE>DisplayRenderer</CODE>.
   */
  public DisplayRenderer () {
  }

  /**
   * @return the distance threshhold for picking in direct manipulation
   *         (distance in XAxis, YAxis, ZAxis coordinates)
   */
  public float getPickThreshhold() {
    return pickThreshhold;
  }

  /**
   * set the distance threshhold for picking in direct manipulation
   * @param pt distance (in XAxis, YAxis, ZAxis coordinates)
   */
  public void setPickThreshhold(float pt) {
    pickThreshhold = pt;
  }

  /**
   * set the aspect for the containing box
   * aspect double[3] array used to scale x, y and z box sizes
   */
  public abstract void setBoxAspect(double[] aspect);

  /**
   * Specify <CODE>DisplayImpl</CODE> to be rendered.
   * @param d <CODE>Display</CODE> to render.
   * @exception VisADException If a <CODE>DisplayImpl</CODE> has already
   *                           been specified.
   */
  public void setDisplay(DisplayImpl d) throws VisADException {
    if (display != null) {
      throw new DisplayException("DisplayRenderer.setDisplay: " +
                                 "display already set");
    }
    display = d;

    // reinitialize rendererControl
    if (rendererControl == null) {
      rendererControl = new RendererControl(display);
      initControl(rendererControl);
    } else {
      RendererControl rc = new RendererControl(display);
      rc.syncControl(rendererControl);
      rendererControl = rc;
    }
    rendererControl.addControlListener(this);
    display.addControl(rendererControl);
  }

  /**
   * Internal method used to initialize newly created
   * <CODE>RendererControl</CODE> with current renderer settings
   * before it is actually connected to the renderer.  This
   * means that changes will not generate <CODE>MonitorEvent</CODE>s.
   * @param ctl RendererControl to initialize
   */
  public abstract void initControl(RendererControl ctl);

  /**
   * Get the <CODE>Display</CODE> associated with this renderer.
   * @return The Display being rendered.
   */
  public DisplayImpl getDisplay() {
    return display;
  }

  /**
   * Get the <CODE>Control</CODE> which holds the "shared" data
   * for this renderer.
   * @return The renderer <CODE>Control</CODE>.
   */
  public RendererControl getRendererControl()
  {
    return rendererControl;
  }

  /**
   * Set the <I>wait flag</I> to the specified value.
   * (When the <I>wait flag</I> is enabled, the user is informed
   *  that the application is busy, typically by displaying a
   *  <B><TT>Please wait . . .</TT></B> message at the bottom of
   *  the <CODE>Display</CODE>.)  DisplayEvent.WAIT_ON and
   *  DisplayEvent.WAIT_OFF events are fired based on value of b.
   * @param b Boolean value to which <I>wait flag</I> is set.
   */
  public void setWaitFlag(boolean b) {
    waitFlag = b;
    try {
      DisplayEvent e = new DisplayEvent(display,
        (b == true) ? DisplayEvent.WAIT_ON 
                    : DisplayEvent.WAIT_OFF);
      display.notifyListeners(e);
    } 
    catch (VisADException e) { }
    catch (RemoteException e) { }
  }

  /**
   *  Get the <I>wait flag</I> state.
   * @return <CODE>true</CODE> if the <I>wait flag</I> is enabled.
   */
  public boolean getWaitFlag() {
    return waitFlag;
  }

  /**
   * Get a new ordinal number for this axis.
   * @param axis Axis for which ordinal is returned
   *        (XAxis=0, YAxis=1, ZAxis=2).
   * @return The new ordinal number.
   */
  int getAxisOrdinal(int axis) {
    synchronized (axisOrdinals) {
      axisOrdinals[axis]++;
      return axisOrdinals[axis];
    }
  }

  /**
   * Reset all the axis ordinals and remove all scales.
   */
  void clearAxisOrdinals() {
    synchronized (axisOrdinals) {
      axisOrdinals[0] = -1;
      axisOrdinals[1] = -1;
      axisOrdinals[2] = -1;
    }
    clearScales();
  }

  /**
   * Get a snapshot of the displayed image.
   * @return The current image being displayed.
   */
  public abstract BufferedImage getImage();

  /**
   * Set an axis scale.
   * @param  axisScale  AxisScale to set (it knows what axis
   *                    it is for)
   * @throws  VisADException  couldn't set the scale
   */
  public abstract void setScale(AxisScale axisScale)
         throws VisADException;

  /**
   * Set an axis scale.
   * @param  axis  axis for this scale (0 = XAxis, 1 = YAxis, 2 = ZAxis)
   * @param  axis_ordinal  position along the axis
   * @param  array   <CODE>VisADLineArray</CODE> representing the scale plot
   * @param  scale_color   float[3] array representing the red, green and
   *                       blue color values.
   * @throws  VisADException  couldn't set the scale
   */
  public abstract void setScale(int axis, int axis_ordinal,
                  VisADLineArray array, float[] scale_color)
         throws VisADException;

  /**
   * Set an axis scale.
   * @param  axis  axis for this scale (0 = XAxis, 1 = YAxis, 2 = ZAxis)
   * @param  axis_ordinal  position along the axis
   * @param  array   <CODE>VisADLineArray</CODE> representing the scale plot
   * @param  labels  <CODE>VisADTriangleArray</CODE> representing the labels
   *                 created using a font (can be null)
   * @param  scale_color   float[3] array representing the red, green and 
   *                       blue color values.
   * @throws  VisADException  couldn't set the scale
   */
  public abstract void setScale(int axis, int axis_ordinal,
                  VisADLineArray array, VisADTriangleArray labels, 
                  float[] scale_color)
         throws VisADException;

  /**
   * Remove all the scales being rendered.
   */
  public abstract void clearScales();

  /**
   * Remove a particular scale being rendered.
   * @param axisScale  scale to remove
   */
  public abstract void clearScale(AxisScale axisScale);

  /**
   * Enable scales to be displayed if they are set.  This should
   * not be called programmatically, since it does not update
   * collaborative displays.
   * Applications should use
   * {@link GraphicsModeControl#setScaleEnable(boolean)
   *  GraphicsModeControl.setScaleEnable}
   * instead of this method.
   * @param  on   true to turn them on, false to set them invisible
   */
  public abstract void setScaleOn(boolean on);

  /**
   * Return <CODE>true</CODE> if this is a 2-D <CODE>DisplayRenderer</CODE>.
   * @return <CODE>true</CODE> if this is a 2-D renderer.
   */
  public boolean getMode2D() {
    return false;
  }

  /**
   * Set the background color.
   * @param color background color
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setBackgroundColor(Color color)
    throws RemoteException, VisADException
  {
    final float r = (float )color.getRed() / 255.0f;
    final float g = (float )color.getGreen() / 255.0f;
    final float b = (float )color.getBlue() / 255.0f;
    setBackgroundColor(r, g, b);
  }

  /**
   * Set the background color.  All specified values should be in the
   * range <CODE>[0.0f - 1.0f]</CODE>.
   * @param r Red value.
   * @param g Green value.
   * @param b Blue value.
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setBackgroundColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    if (rendererControl == null) {
      throw new VisADException("DisplayRenderer not yet assigned to a Display");
    }
    rendererControl.setBackgroundColor(r, g, b);
  }

  /**
   * Set the foreground color (box, cursor and scales).
   * @param color foreground color
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setForegroundColor(Color color)
    throws RemoteException, VisADException
  {
    final float r = (float )color.getRed() / 255.0f;
    final float g = (float )color.getGreen() / 255.0f;
    final float b = (float )color.getBlue() / 255.0f;
    setForegroundColor(r, g, b);
  }

  /**
   * Set the foreground color (box, cursor and scales).  All specified 
   * values should be in the range <CODE>[0.0f - 1.0f]</CODE>.
   * @param r Red value.
   * @param g Green value.
   * @param b Blue value.
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setForegroundColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    if (rendererControl == null) {
      throw new VisADException("DisplayRenderer not yet assigned to a Display");
    }
    rendererControl.setForegroundColor(r, g, b);
  }

  /**
   * Get the box visibility.
   * @return <CODE>true</CODE> if the box is visible.
   */
  public boolean getBoxOn()
    throws RemoteException, VisADException
  {
    if (rendererControl == null) {
      throw new VisADException("DisplayRenderer not yet assigned to a Display");
    }
    return rendererControl.getBoxOn();
  }

  /**
   * Set the box color.
   * @param color box color
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setBoxColor(Color color)
    throws RemoteException, VisADException
  {
    final float r = (float )color.getRed() / 255.0f;
    final float g = (float )color.getGreen() / 255.0f;
    final float b = (float )color.getBlue() / 255.0f;
    setBoxColor(r, g, b);
  }

  /**
   * Set the box color.  All specified values should be in the range
   * <CODE>[0.0f - 1.0f]</CODE>.
   * @param r Red value.
   * @param g Green value.
   * @param b Blue value.
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setBoxColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    if (rendererControl == null) {
      throw new VisADException("DisplayRenderer not yet assigned to a Display");
    }
    rendererControl.setBoxColor(r, g, b);
  }

  /**
   * Set the box visibility.
   * @param on <CODE>true</CODE> if the box should be visible.
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setBoxOn(boolean on)
    throws RemoteException, VisADException
  {
    if (rendererControl == null) {
      throw new VisADException("DisplayRenderer not yet assigned to a Display");
    }
    rendererControl.setBoxOn(on);
  }

  /**
   * Get the cursor color.
   * @return A 3 element array of <CODE>float</CODE> values
   *         in the range <CODE>[0.0f - 1.0f]</CODE>
   *         in the order <I>(Red, Green, Blue)</I>.
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public float[] getCursorColor()
    throws RemoteException, VisADException
  {
    if (rendererControl == null) {
      throw new VisADException("DisplayRenderer not yet assigned to a Display");
    }
    return rendererControl.getCursorColor();
  }

  /**
   * Set the cursor color.
   * @param color cursor color
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setCursorColor(Color color)
    throws RemoteException, VisADException
  {
    final float r = (float )color.getRed() / 255.0f;
    final float g = (float )color.getGreen() / 255.0f;
    final float b = (float )color.getBlue() / 255.0f;
    setCursorColor(r, g, b);
  }

  /**
   * Set the cursor color.  All specified values should be in the range
   * <CODE>[0.0f - 1.0f]</CODE>.
   * @param r Red value.
   * @param g Green value.
   * @param b Blue value.
   * @exception RemoteException If there was a problem making this change
   *                            in a remote collaborative
   *                            <CODE>DisplayRenderer</CODE>.
   * @exception VisADException If this renderer as not yet been assigned
   *                           to a <CODE>Display</CODE>.
   */
  public void setCursorColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    if (rendererControl == null) {
      throw new VisADException("DisplayRenderer not yet assigned to a Display");
    }
    rendererControl.setCursorColor(r, g, b);
  }

  /**
   * Factory for constructing a subclass of <CODE>Control</CODE>
   * appropriate for the graphics API and for this
   * <CODE>DisplayRenderer</CODE>; invoked by <CODE>ScalarMap</CODE>
   * when it is <CODE>addMap()</CODE>ed to a <CODE>Display</CODE>.
   * @param map The <CODE>ScalarMap</CODE> for which a <CODE>Control</CODE>
   *            should be built.
   * @return The appropriate <CODE>Control</CODE>.
   */
  public abstract Control makeControl(ScalarMap map);

  /**
   * Factory for constructing the default subclass of
   * <CODE>DataRenderer</CODE> for this <CODE>DisplayRenderer</CODE>.
   * @return The default <CODE>DataRenderer</CODE>.
   */
  public abstract DataRenderer makeDefaultRenderer();

  /**
   * determine whether a DataRenderer is legal for this DisplayRenderer
   * @param renderer DisplayRenderer to test for legality
   * @return true if renderer is legal
   */
  public abstract boolean legalDataRenderer(DataRenderer renderer);

  /**
   * Set whether the animation info should be visible in the display
   * or not.
   * @param  visible  true to show the animation info
   */
  public void setAnimationStringVisible(boolean visible) {
    aniStringVisible = visible;
  }

  /**
   * Return whether the animation info should be visible in the display
   * or not.
   * @return   true if the animation info should be shown
   */
  public boolean getAnimationStringVisible() {
    return aniStringVisible;
  }

  /**
   * Set whether the "please wait" info should be visible in the display
   * or not.
   * @param  visible  true to show the please wait info
   */
  public void setWaitMessageVisible(boolean visible) {
    waitMessageVisible = visible;
  }

  /**
   * Return whether the please wait info should be visible in the display
   * or not.
   * @return   true if the "please wait" info should be shown
   */
  public boolean getWaitMessageVisible() {
    return waitMessageVisible;
  }

  /**
   * Return Array of <CODE>String</CODE>s describing the
   * animation sequence
   * @return The animation description
   */
  public String[] getAnimationString() {
    if (aniStringVisible) {
      return animationString;
    }
    else {
      return new String[] {null, null};
    }
  }

  /**
   * Set Array of <CODE>String</CODE>s describing the
   * animation sequence
   * @param animation a String[2] array describing the
   *                  animation sequence
   */
  public void setAnimationString(String[] animation) {
    animationString[0] = animation[0];
    animationString[1] = animation[1];
  }

  /**
   * Return an array giving the cursor location as
   * <I>(XAxis, YAxis, ZAxis)</I> coordinates
   * @return 3 element <CODE>double</CODE> array of cursor coordinates.
   */
  public abstract double[] getCursor();

  /**
   * set flag indicating whether the cursor should be displayed.
   * @param on value of flag to set
   */
  public abstract void setCursorOn(boolean on);

  /**
   * set a VisADRay along which to drag cursor in depth (in and out
   * of screen)
   * @param ray VisADRay to set
   */
  public abstract void depth_cursor(VisADRay ray);

  /**
   * drag cursor parallel to plane of screen
   * @param ray VisADRay that goes through new cursor location
   * @param first true to indicate this is first call to drag_cursor()
   *        for this drag
   */
  public abstract void drag_cursor(VisADRay ray, boolean first);

  /**
   * set flag indicating whether direct manipulation is active
   * @param on value of flag to set
   */
  public abstract void setDirectOn(boolean on);

  /**
   * drag cursor in depth (in and out of screen)
   * @param diff amount to move cursor in depth (0.0 corresponds
   *             to no movement)
   */
  public abstract void drag_depth(float diff);

  /**
   * @return flag indicating whether there are any direct manipulation
   *         DataRenderers linked to Display
   */
  public abstract boolean anyDirects();

  /**
   * @return the MouseBehavior for this display
   */
  public abstract MouseBehavior getMouseBehavior();

  /**
   * Returns a direct manipulation renderer if one is close to
   * the specified ray (within pick threshold).
   * @param ray The ray used to look for a nearby direct manipulation
   *            renderer.
   * @param mouseModifiers Value of InputEvent.getModifiers().
   * @return DataRenderer or <CODE>null</CODE>.
   */
  public abstract DataRenderer findDirect(VisADRay ray, int mouseModifiers);

  /**
   * set flag indicating whether the cursor location String
   * should be displayed.
   * @param on value of flag to set
   */
  public void setCursorStringOn(boolean on) {
    cursor_string = on;
  }

  /**
   * Return <CODE>Vector</CODE> of <CODE>String</CODE>s describing the
   * cursor location, if cursor location display is enabled.
   * @return The cursor location description as a Vector of Strings
   *         (an empty Vector if cursor location display is disabled).
   */
  public Vector getCursorStringVector() {
    if (cursor_string) {
      return (Vector) cursorStringVector.clone();
    }
    else {
      return new Vector();
    }
  }

  /**
   * Return <CODE>Vector</CODE> of <CODE>String</CODE>s describing the
   * cursor location, regardless of whether cursor location display is
   * enabled.
   * @return The cursor location description as a Vector of Strings.
   */
  public Vector getCursorStringVectorUnconditional() {
    return (Vector) cursorStringVector.clone();
  }

  /**
   * get the value of a RealType if it is included in the
   * cursor location
   * @param type RealType whose value to get
   * @return value of type, or NaN if it is not included in
   *         the cursor location
   */
  public double getDirectAxisValue(RealType type) {
    return getDirectAxisValue(type.getName());
  }

  /**
   * get the value of a named RealType if it is included in
   * the cursor location
   * @param name String name of RealType whose value to get
   * @return value of named RealType, or NaN if it is not
   *         included in the cursor location
   */
  public double getDirectAxisValue(String name) { 
    synchronized (cursorStringVector) {
      if (cursorStringVector != null) { 
        Enumeration strings = cursorStringVector.elements();
        while(strings.hasMoreElements()) {
          String s = (String) strings.nextElement();
          if (s.startsWith(name)) {
            String t = s.substring(s.indexOf("=") + 2);
            int i = t.indexOf(" ");
            if (i >= 0) t = t.substring(0, i);
            try {
              double v = Double.valueOf(t).doubleValue();
              return v;
            }
            catch (NumberFormatException e) {
              return Double.NaN;
            }
          }
        }
      }
    }
    return Double.NaN;
  } 

  /**
   * Set <CODE>Vector</CODE> of <CODE>String</CODE>s describing the
   * cursor location by copy; this is invoked by direct manipulation
   * renderers.
   * @param vect String descriptions of cursor location.
   */
  public void setCursorStringVector(Vector vect) {
    synchronized (cursorStringVector) {
      cursorStringVector.removeAllElements();
      if (vect != null) {
        Enumeration strings = vect.elements();
        while(strings.hasMoreElements()) {
          cursorStringVector.addElement(strings.nextElement());
        }
      }
    }
    render_trigger();
  }

  /**
   * Set <CODE>Vector</CODE> of <CODE>String</CODE>s describing the
   * cursor location from the cursor location; this is invoked when the
   * cursor location changes or the cursor display status changes
   */
  public void setCursorStringVector() {
    synchronized (cursorStringVector) {
      cursorStringVector.removeAllElements();
      float[][] cursor = new float[3][1];
      double[] cur = getCursor();
      cursor[0][0] = (float) cur[0];
      cursor[1][0] = (float) cur[1];
      cursor[2][0] = (float) cur[2];
      Enumeration maps = display.getMapVector().elements();
      while(maps.hasMoreElements()) {
        try {
          ScalarMap map = (ScalarMap) maps.nextElement();
          DisplayRealType dreal = map.getDisplayScalar();
          DisplayTupleType tuple = dreal.getTuple();
          int index = dreal.getTupleIndex();
          if (tuple != null &&
              (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
               (tuple.getCoordinateSystem() != null &&
                tuple.getCoordinateSystem().getReference().equals(
                Display.DisplaySpatialCartesianTuple)))) {
            float[] fval = new float[1];
            if (tuple.equals(Display.DisplaySpatialCartesianTuple)) {
              fval[0] = cursor[index][0];
            }
            else {
              float[][] new_cursor =
                tuple.getCoordinateSystem().fromReference(cursor);
              fval[0] = new_cursor[index][0];
            }
            float[] dval = map.inverseScaleValues(fval);
            RealType real = (RealType) map.getScalar();

            // WLH 31 Aug 2000
            Real r = new Real(real, dval[0]);
            Unit overrideUnit = map.getOverrideUnit();
            Unit rtunit = real.getDefaultUnit();
            // units not part of Time string
            // DRM 2003-08-19: don't check for equality since toString
            // may be different
            if (overrideUnit != null && //!overrideUnit.equals(rtunit) &&
                (!Unit.canConvert(rtunit, CommonUnit.secondsSinceTheEpoch) ||
                 rtunit.getAbsoluteUnit().equals(rtunit))) {
              dval[0] = (float)
                overrideUnit.toThis((double) dval[0], rtunit);
              r = new Real(real, dval[0], overrideUnit);
            }
            String valueString = r.toValueString();

            // WLH 27 Oct 2000
            String s = map.getScalarName() + " = " + valueString;
            // String s = real.getName() + " = " + valueString;

            cursorStringVector.addElement(s);
          } // end if (tuple != null && ...)
        }
        catch (VisADException e) {
        }
      } // end while(maps.hasMoreElements())
    } // end synchronized (cursorStringVector)
    render_trigger();
  }

  /**
   * trigger the graphics API to render the scene graph to the screen;
   * intended to be over-ridden by graphics-API-specific extensions of
   * DisplayRenderer
   */
  public void render_trigger() {
  }

  /**
   * Return <CODE>true</CODE> if <CODE>type</CODE> is legal for this
   * <CODE>DisplayRenderer</CODE>; for example, 2-D
   * <CODE>DisplayRenderer</CODE>s use this to disallow mappings to
   * <I>ZAxis</I> and <I>Latitude</I>.
   * @param type The mapping type to check.
   * @return <CODE>true</CODE> if <CODE>type</CODE> is legal.
   */
  public boolean legalDisplayScalar(DisplayRealType type) {
    // First check to see if it is a member of the default list
    for (int i=0; i<Display.DisplayRealArray.length; i++) {
      if (Display.DisplayRealArray[i].equals(type)) return true;
    }
    // if we get here, it's not one of the defaults.  See if it has
    // a CS that transforms to a default that we know how to handle
    if (type.getTuple() != null && 
        type.getTuple().getCoordinateSystem() != null) 
    {
        RealTupleType ref = 
          type.getTuple().getCoordinateSystem().getReference();
        if (ref.equals(Display.DisplaySpatialCartesianTuple) ||
            ref.equals(Display.DisplayRGBTuple) ||
            ref.equals(Display.DisplayFlow1Tuple) ||
            ref.equals(Display.DisplayFlow2Tuple)) return true;
    }
    return false;
  }

  /**
   * prepare for transforming Data into scene graph depictions,
   * including possible auto-scaling of ScalarMaps
   * @param temp Vector of DataRenderers
   * @param tmap Vector of ScalarMaps
   * @param go flag indicating whether Data transforms are requested
   * @param initialize flag indicating whether auto-scaling is
   *        requested
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  public void prepareAction(Vector temp, Vector tmap, boolean go,
                            boolean initialize)
         throws VisADException, RemoteException {
    DataShadow shadow = null;
    Enumeration renderers = temp.elements();
    while (renderers.hasMoreElements()) {
      DataRenderer renderer = (DataRenderer) renderers.nextElement();
      shadow = renderer.prepareAction(go, initialize, shadow);
    }

    if (shadow != null) {
      // apply RealType ranges and animationSampling
      Enumeration maps = tmap.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = ((ScalarMap) maps.nextElement());
        map.setRange(shadow);
      }
    }

    ScalarMap.equalizeFlow(tmap, Display.DisplayFlow1Tuple);
    ScalarMap.equalizeFlow(tmap, Display.DisplayFlow2Tuple);
  }

  public abstract int getTextureWidthMax() 
         throws VisADException; 

  public abstract int getTextureHeightMax()
         throws VisADException; 

}
