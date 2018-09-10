//
// RendererControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Color;

import java.rmi.RemoteException;

import java.util.Enumeration;

import visad.util.Util;

/**
 * <CODE>RendererControl</CODE> is the VisAD class for controlling
 * <CODE>DisplayRenderer</CODE> data.<P>
 */
public class RendererControl
  extends Control
{
  //private transient DisplayRenderer renderer = null;  not needed DRM 25-May-01

  private float[] backgroundColor = new float[] { 0.0f, 0.0f, 0.0f};
  private float[] boxColor = new float[] { 1.0f, 1.0f, 1.0f};
  private float[] cursorColor = new float[] { 1.0f, 1.0f, 1.0f};
  private float[] foregroundColor = new float[] { 1.0f, 1.0f, 1.0f};

  private boolean boxOn = false;

  /**
   * Construct a renderer control.
   * @param dpy Display with which this control is associated.
   */
  public RendererControl(DisplayImpl dpy)
  {
    super(dpy);
  }

  /**
   * Get the background color.
   * @return A 3 element array of <CODE>float</CODE> values
   *         in the range <CODE>[0.0f - 1.0f]</CODE>
   *         in the order <I>(Red, Green, Blue)</I>.
   */
  public float[] getBackgroundColor()
  {
    return backgroundColor;
  }

  /**
   * Set the background color.
   * @param color background color
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
   * Set the background color.  All specified values should be in the range
   * <CODE>[0.0f - 1.0f]</CODE>.
   * @param r Red value.
   * @param g Green value.
   * @param b Blue value.
   */
  public void setBackgroundColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    backgroundColor[0] = r;
    backgroundColor[1] = g;
    backgroundColor[2] = b;
    changeControl(true);
  }

  /**
   * Get the foreground color set using 
   * {@link #setForegroundColor(float, float, float)}.
   * <B>NOTE</B>:  The values returned may not be
   * indicative of the actual color of any of the components of the foreground
   * (box, cursor, axes) since the color of each of these can be set 
   * individually.
   * @return A 3 element array of <CODE>float</CODE> values
   *         in the range <CODE>[0.0f - 1.0f]</CODE>
   *         in the order <I>(Red, Green, Blue)</I>.
   * @see #setForegroundColor(float, float, float)
   */
  public float[] getForegroundColor()
  {
    return foregroundColor;
  }

  /**
   * Convenience method to set the foreground color (box, cursor and axes).   
   * Overrides any previous calls to setCursorColor, setBoxColor and 
   * ScalarMap.setScaleColor().
   * @param color foreground color
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
   * Convenience method to set the foreground color (box, cursor and axes).   
   * Overrides any previous calls to setCursorColor, setBoxColor and 
   * ScalarMap.setScaleColor().
   * All specified values should be in the range
   * <CODE>[0.0f - 1.0f]</CODE>.
   * @param r Red value.
   * @param g Green value.
   * @param b Blue value.
   * @see #getForegroundColor()
   * @see #setCursorColor(float, float, float)
   * @see #setBoxColor(float, float, float)
   * @see ScalarMap#setScaleColor(float[])
   */
  public void setForegroundColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    setCursorColor(r,g,b);
    setBoxColor(r,g,b);
    foregroundColor[0] = r;
    foregroundColor[1] = g;
    foregroundColor[2] = b;
    if (getDisplayRenderer() != null) {
      DisplayImpl dpy = getDisplayRenderer().getDisplay();
      if (dpy != null) {
        for (Enumeration e = display.getMapVector().elements(); 
              e.hasMoreElements();)
        {
          ScalarMap map = (ScalarMap) e.nextElement();
          if (map.getAxisScale() != null) map.setScaleColor(foregroundColor);
        }
      }
    }
  }

  /**
   * Get the box color.
   * @return A 3 element array of <CODE>float</CODE> values
   *         in the range <CODE>[0.0f - 1.0f]</CODE>
   *         in the order <I>(Red, Green, Blue)</I>.
   */
  public float[] getBoxColor()
  {
    return boxColor;
  }

  /**
   * Get the box visibility.
   * @return <CODE>true</CODE> if the box is visible.
   */
  public boolean getBoxOn()
  {
    return boxOn;
  }

  /**
   * Set the box color.
   * @param color box color
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
   */
  public void setBoxColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    boxColor[0] = r;
    boxColor[1] = g;
    boxColor[2] = b;
    changeControl(true);
  }

  /**
   * Set the box visibility.
   * @param on <CODE>true</CODE> if the box should be visible.
   */
  public void setBoxOn(boolean on)
    throws RemoteException, VisADException
  {
    boxOn = on;
    changeControl(true);
  }

  /**
   * Get the cursor color.
   * @return A 3 element array of <CODE>float</CODE> values
   *         in the range <CODE>[0.0f - 1.0f]</CODE>
   *         in the order <I>(Red, Green, Blue)</I>.
   */
  public float[] getCursorColor()
  {
    return cursorColor;
  }

  /**
   * Set the cursor color.
   * @param color cursor color
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
   */
  public void setCursorColor(float r, float g, float b)
    throws RemoteException, VisADException
  {
    cursorColor[0] = r;
    cursorColor[1] = g;
    cursorColor[2] = b;
    changeControl(true);
  }

  /**
   * Utility array used to compare to <CODE>float[]</CODE> arrays.
   * @param one The first array.
   * @param two The second array.
   * @return <CODE>true</CODE> if the arrays are equal.
   */
  private static boolean floatArrayEquals(float[] one, float[] two)
  {
    if (one == null) {
      if (two != null) {
        return false;
      }
    } else if (two == null) {
      return false;
    } else if (one.length != two.length) {
      return false;
    } else {
      for (int i = 0; i < one.length; i++) {
        if (!Util.isApproximatelyEqual(one[i], two[i])) {
          return false;
        }
      }
    }

    return true;
  }

  /** get a string that can be used to reconstruct this control later */
  public String getSaveString() {
    return null;
  }

  /** reconstruct this control using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    throw new UnimplementedException(
      "Cannot setSaveString on this type of control");
  }

  /**
   * Copy the state of the specified control.
   * @param ctl <CODE>Control</CODE> to copy.
   */
  public void syncControl(Control ctl)
    throws VisADException
  {
    if (ctl == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(ctl instanceof RendererControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + ctl.getClass().getName());
    }

    RendererControl rc = (RendererControl )ctl;

    boolean changed = false;

    if (!floatArrayEquals(backgroundColor, rc.backgroundColor)) {
      changed = true;
      backgroundColor = rc.backgroundColor;
    }
    if (!floatArrayEquals(foregroundColor, rc.foregroundColor)) {
      changed = true;
      foregroundColor = rc.foregroundColor;
    }
    if (!floatArrayEquals(boxColor, rc.boxColor)) {
      changed = true;
      boxColor = rc.boxColor;
    }
    if (!floatArrayEquals(cursorColor, rc.cursorColor)) {
      changed = true;
      cursorColor = rc.cursorColor;
    }

    if (boxOn != rc.boxOn) {
      changed = true;
      boxOn = rc.boxOn;
    }

    if (changed) {
      try {
        changeControl(true);
      } catch (RemoteException re) {
        throw new VisADException("Could not indicate that control" +
                                 " changed: " + re.getMessage());
      }
    }
  }

  /**
   * Compare this object to another object.
   * @param o Object to compare.
   * @return <CODE>true</CODE> if this  object is "equal" to the
   *         specified object.
   */
  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    RendererControl rc = (RendererControl )o;

    if (!floatArrayEquals(backgroundColor, rc.backgroundColor)) {
      return false;
    }
    if (!floatArrayEquals(foregroundColor, rc.foregroundColor)) {
      return false;
    }
    if (!floatArrayEquals(boxColor, rc.boxColor)) {
      return false;
    }
    if (!floatArrayEquals(cursorColor, rc.cursorColor)) {
      return false;
    }

    if (boxOn != rc.boxOn) {
      return false;
    }

    return true;
  }

  public Object clone()
  {
    RendererControl rc = (RendererControl )super.clone();
    if (backgroundColor != null) {
      rc.backgroundColor = (float[] )backgroundColor.clone();
    }
    if (foregroundColor != null) {
      rc.foregroundColor = (float[] )foregroundColor.clone();
    }
    if (boxColor != null) {
      rc.boxColor = (float[] )boxColor.clone();
    }
    if (cursorColor != null) {
      rc.cursorColor = (float[] )cursorColor.clone();
    }

    return rc;
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer("RendererControl[");

    buf.append("bg=");
    buf.append(backgroundColor[0]);
    buf.append('/');
    buf.append(backgroundColor[1]);
    buf.append('/');
    buf.append(backgroundColor[2]);

    buf.append(",fg=");
    buf.append(foregroundColor[0]);
    buf.append('/');
    buf.append(foregroundColor[1]);
    buf.append('/');
    buf.append(foregroundColor[2]);

    buf.append(",cursor=");
    buf.append(cursorColor[0]);
    buf.append('/');
    buf.append(cursorColor[1]);
    buf.append('/');
    buf.append(cursorColor[2]);

    buf.append(",box=");
    buf.append(boxColor[0]);
    buf.append('/');
    buf.append(boxColor[1]);
    buf.append('/');
    buf.append(boxColor[2]);

    buf.append(',');
    if (!boxOn) buf.append('!');
    buf.append("boxOn");

    buf.append(']');
    return buf.toString();
  }
}
