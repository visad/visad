
//
// DisplayRenderer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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
  public DisplayImpl display;

//
// TO_DO
// make this less public
//
  /** vector of Strings describing cursor location */
  public Vector cursorStringVector = new Vector();

  public DisplayRenderer () {
  }

  public void setDisplay(DisplayImpl d) throws VisADException {
    if (display != null) {
      throw new DisplayException("DisplayRenderer.setDisplay: " +
                                 "display already set");
    }
    display = d;
  }

  public DisplayImpl getDisplay() {
    return display;
  }

  public boolean getMode2D() {
    return false;
  }

  public abstract Control makeControl(DisplayRealType type);

  public abstract double[] getCursor();

  /** copy Strings in vect to cursorStringVector */
  public void setCursorStringVector(Vector vect) {
    synchronized (cursorStringVector) {
      cursorStringVector.removeAllElements();
      Enumeration strings = vect.elements();
      while(strings.hasMoreElements()) {
        cursorStringVector.addElement(strings.nextElement());
      }
    }
  }

  /** create Strings in cursorStringVector from cursor location */
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
            float f = (float) dval[0];
            RealType real = map.getScalar();
            cursorStringVector.addElement(real.getName() + " = " + f);
          } // end if (tuple != null && ...)
        }
        catch (VisADException e) {
        }
      } // end while(maps.hasMoreElements())
    } // end synchronized (cursorStringVector)
  }

  public boolean legalDisplayScalar(DisplayRealType type) {
    for (int i=0; i<Display.DisplayRealArray.length; i++) {
      if (Display.DisplayRealArray[i].equals(type)) return true;
    }
    return false;
  }

}

