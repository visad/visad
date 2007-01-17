//
//  CBMKeyboardBehaviorJ3D.java
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

package visad.bom;

import java.awt.event.*;
import java.rmi.RemoteException;
import visad.*;
import visad.java3d.*;


public class CBMKeyboardBehaviorJ3D extends KeyboardBehaviorJ3D {

/*
  // Identifier for function to rotate positively around the Z viewing axis
  public static final int ROTATE_Z_POS = 7;
*/

  private static final int MAX_FUNCTIONS = 6;

  public static final int PLUS_ANGLE = 0; // ->
  public static final int MINUS_ANGLE = 1; // <-
  public static final int PLUS_SPEED = 2; // up
  public static final int MINUS_SPEED = 3; // down
  public static final int NEXT_WIND = 4; // page down
  public static final int PREVIOUS_WIND = 5; // page up

  private CollectiveBarbManipulation cbm = null;

  // WLH 19 Feb 2001
  public CBMKeyboardBehaviorJ3D(DisplayRendererJ3D r) {
    super(r, MAX_FUNCTIONS);

    mapKeyToFunction(PLUS_ANGLE, KeyEvent.VK_RIGHT, NO_MASK);
    mapKeyToFunction(MINUS_ANGLE, KeyEvent.VK_LEFT, NO_MASK);
    mapKeyToFunction(PLUS_SPEED, KeyEvent.VK_UP, NO_MASK);
    mapKeyToFunction(MINUS_SPEED, KeyEvent.VK_DOWN, NO_MASK);
    mapKeyToFunction(NEXT_WIND, KeyEvent.VK_PAGE_DOWN, NO_MASK);
    mapKeyToFunction(PREVIOUS_WIND, KeyEvent.VK_PAGE_UP, NO_MASK);
  }

  public void setWhichCBM(CollectiveBarbManipulation c) throws VisADException {
    cbm = c;
  }

  /** 
   * Executes the given function. 
   * @param  function   function to perform (TRANSLATE_UP, ZOOM_IN, etc)
   */
  public void execFunction(int function) {

    if (cbm == null) return;

// System.out.println("execFunction " + function);

    try {
      switch (function) {
        case PLUS_ANGLE:
          cbm.plusAngle();
          break;
        case MINUS_ANGLE:
          cbm.minusAngle();
          break;
        case PLUS_SPEED:
          cbm.plusSpeed();
          break;
        case MINUS_SPEED:
          cbm.minusSpeed();
          break;
        case NEXT_WIND:
          cbm.nextWind();
          break;
        case PREVIOUS_WIND:
          cbm.previousWind();
          break;
        default:
          break;
      }
    }
    catch (VisADException e) {
    }
    catch (RemoteException e) {
    }
  }
}
