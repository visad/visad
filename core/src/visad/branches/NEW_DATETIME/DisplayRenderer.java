
//
// DisplayRenderer.java
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

package visad;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.util.*;


/**
   DisplayRenderer is the VisAD abstract super-class for background and
   metadata rendering algorithms.  These complement depictions of Data
   objects created by DataRenderer objects.<P>

   DisplayRenderer also manages the overall relation of DataRenderer
   output to the graphics library.<P>

   DisplayRenderer is not Serializable and should not be copied
   between JVMs.<P>
*/
public abstract class DisplayRenderer extends Object {

  /** DisplayImpl this DisplayRenderer is attached to */
  private DisplayImpl display;

  /** vector of Strings describing cursor location */
  private Vector cursorStringVector = new Vector();

  String[] animationString = {null, null};

  private int[] axisOrdinals = {-1, -1, -1};

  private boolean waitFlag = false;

  public DisplayRenderer () {
  }

  public void setDisplay(DisplayImpl d) throws VisADException {
    if (display != null) {
      throw new DisplayException("DisplayRenderer.setDisplay: " +
                                 "display already set");
    }
    display = d;
  }

  /** return the DisplayImpl that this DisplayRenderer is attached to */
  public DisplayImpl getDisplay() {
    return display;
  }

  public void setWaitFlag(boolean b) {
    waitFlag = b;
  }

  public boolean getWaitFlag() {
    return waitFlag;
  }

  int getAxisOrdinal(int axis) {
    synchronized (axisOrdinals) {
      axisOrdinals[axis]++;
      return axisOrdinals[axis];
    }
  }

  void clearAxisOrdinals() {
    synchronized (axisOrdinals) {
      axisOrdinals[0] = -1;
      axisOrdinals[1] = -1;
      axisOrdinals[2] = -1;
    }
    clearScales();
  }

  public abstract BufferedImage getImage();

  public abstract void setScale(int axis, int axis_ordinal,
                  VisADLineArray array, float[] scale_color)
         throws VisADException;

  public abstract void clearScales();

  public abstract void setScaleOn(boolean on);

  /** return true is this is a 2-D DisplayRenderer */
  public boolean getMode2D() {
    return false;
  }

  public abstract void setBackgroundColor(float r, float g, float b);

  public abstract void setBoxColor(float r, float g, float b);

  public abstract void setCursorColor(float r, float g, float b);

  /** factory for constructing a subclass of Control appropriate
  /** factory for constructing a subclass of Control appropriate
  /** factory for constructing a subclass of Control appropriate
      for the graphics API and for this DisplayRenderer;
      invoked by ScalarMap when it is 'addMap'ed to a Display */
  public abstract Control makeControl(ScalarMap map);

  /** factory for constructing the default subclass of
      DataRenderer for this DisplayRenderer */
  public abstract DataRenderer makeDefaultRenderer();

  public abstract boolean legalDataRenderer(DataRenderer renderer);

  public String[] getAnimationString() {
    return animationString;
  }

  public void setAnimationString(String[] animation) {
    animationString[0] = animation[0];
    animationString[1] = animation[1];
  }

  /** return a double[3] array giving the cursor location in
      (XAxis, YAxis, ZAxis) coordinates */
  public abstract double[] getCursor();

  public abstract void setCursorOn(boolean on);

  public abstract void setBoxOn(boolean on);

  public abstract void depth_cursor(VisADRay ray);

  public abstract void drag_cursor(VisADRay ray, boolean first);

  public abstract void setDirectOn(boolean on);

  public abstract void drag_depth(float diff);

  public abstract boolean anyDirects();

  /** actually returns a direct manipulation renderer */
  public abstract DataRenderer findDirect(VisADRay ray);

  /** return Vector of Strings describing the cursor location */
  public Vector getCursorStringVector() {
    return (Vector) cursorStringVector.clone();
  }

  /** set vector of Strings describing the cursor location by copy;
      this is invoked by direct manipulation renderers */
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

  /** set vector of Strings describing the cursor location
      from the cursor location;
      this is invoked when the cursor location changes or
      the cursor display status changes */
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
            double[] dval = map.inverseScaleValues(fval);
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

  /** return true if type is legal for this DisplayRenderer;
      for example, 2-D DisplayRenderers use this to disallow
      mappings to ZAxis and Latitude */
  public boolean legalDisplayScalar(DisplayRealType type) {
    for (int i=0; i<Display.DisplayRealArray.length; i++) {
      if (Display.DisplayRealArray[i].equals(type)) return true;
    }
    return false;
  }

}

