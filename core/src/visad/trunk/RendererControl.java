//
// RendererControl.java
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

import java.rmi.RemoteException;

/**
 * <CODE>RendererControl</CODE> is the VisAD class for controlling
 * <CODE>DisplayRenderer</CODE> data.<P>
 */
public class RendererControl
  extends Control
{
  private transient DisplayRenderer renderer = null;

  private float[] backgroundColor = new float[] { 0.0f, 0.0f, 0.0f};
  private float[] boxColor = new float[] { 1.0f, 1.0f, 1.0f};
  private float[] cursorColor = new float[] { 1.0f, 1.0f, 1.0f};

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
        if (Math.abs(one[i] - two[i]) > 0.0001) {
          return false;
        }
      }
    }

    return true;
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
   * @param obj Object to compare.
   * @return <CODE>true</CODE> if this  object is "equal" to the
   *         specified object.
   */
  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof RendererControl)) {
      return false;
    }

    RendererControl rc = (RendererControl )o;

    if (!floatArrayEquals(backgroundColor, rc.backgroundColor)) {
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
}
