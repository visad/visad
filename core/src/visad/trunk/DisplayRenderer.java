//
// DisplayRenderer.java
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

package visad;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

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
public abstract class DisplayRenderer extends Object {

  /** DisplayImpl this renderer is attached to. */
  private DisplayImpl display;

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

  /**
   * Construct a new <CODE>DisplayRenderer</CODE>.
   */
  public DisplayRenderer () {
  }

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
  }

  /**
   * Get the <CODE>Display</CODE> associated with this renderer.
   * @return The Display being rendered.
   */
  public DisplayImpl getDisplay() {
    return display;
  }

  /**
   * Set the <I>wait flag</I> to the specified value.
   * (When the <I>wait flag</I> is enabled, the user is informed
   *  that the application is busy, typically by displaying a
   *  <B><TT>Please wait . . .</TT></B> message at the bottom of
   *  the <CODE>Display</CODE>.)
   * @param b Boolean value to which <I>wait flag</I> is set.
   */
  public void setWaitFlag(boolean b) {
    waitFlag = b;
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
   * @param axis Axis for which ordinal is returned.
   * @return The new ordinal number.
   */
  int getAxisOrdinal(int axis) {
    synchronized (axisOrdinals) {
      axisOrdinals[axis]++;
      return axisOrdinals[axis];
    }
  }

  /**
   * Reset all the axis ordinals.
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

  public abstract void setScale(int axis, int axis_ordinal,
                  VisADLineArray array, float[] scale_color)
         throws VisADException;

  public abstract void clearScales();

  public abstract void setScaleOn(boolean on);

  /**
   * Return <CODE>true</CODE> if this is a 2-D <CODE>DisplayRenderer</CODE>.
   * @return <CODE>true</CODE> if this is a 2-D renderer.
   */
  public boolean getMode2D() {
    return false;
  }

  public abstract void setBackgroundColor(float r, float g, float b);

  public abstract void setBoxColor(float r, float g, float b);

  public abstract void setCursorColor(float r, float g, float b);

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

  public abstract boolean legalDataRenderer(DataRenderer renderer);

  public String[] getAnimationString() {
    return animationString;
  }

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

  public abstract void setCursorOn(boolean on);

  /**
   * Make the surrounding box visible if <CODE>on</CODE> is <CODE>true</CODE>.
   * @param on Specifies whether or not the box should be visible.
   */
  public abstract void setBoxOn(boolean on);

  public abstract void depth_cursor(VisADRay ray);

  public abstract void drag_cursor(VisADRay ray, boolean first);

  public abstract void setDirectOn(boolean on);

  public abstract void drag_depth(float diff);

  public abstract boolean anyDirects();

  /**
   * Returns a direct manipulation renderer if one is close to
   * the specified ray.
   * @param ray The ray used to look for a nearby direct manipulation
   *            renderer.
   * @return DataRenderer or <CODE>null</CODE>.
   */
  public abstract DataRenderer findDirect(VisADRay ray);

  public void setCursorStringOn(boolean on) {
    cursor_string = on;
  }

  /**
   * Return <CODE>Vector</CODE> of <CODE>String</CODE>s describing the
   * cursor location.
   * @return The cursor location description.
   */
  public Vector getCursorStringVector() {
    if (cursor_string) {
      return (Vector) cursorStringVector.clone();
    }
    else {
      return new Vector();
    }
  }

  public double getDirectAxisValue(RealType type) {
    synchronized (cursorStringVector) {
      if (cursorStringVector != null) {
        String name = type.getName();
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
	    String valueString = new Real(real, dval[0]).toValueString();
            String s = real.getName() + " = " + valueString;
            cursorStringVector.addElement(s);
          } // end if (tuple != null && ...)
        }
        catch (VisADException e) {
        }
      } // end while(maps.hasMoreElements())
    } // end synchronized (cursorStringVector)
    render_trigger();
  }

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
    for (int i=0; i<Display.DisplayRealArray.length; i++) {
      if (Display.DisplayRealArray[i].equals(type)) return true;
    }
    return false;
  }

}
