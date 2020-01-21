//
//  KeyboardBehavior.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.event.KeyEvent;

public interface KeyboardBehavior {

  // standard keyboard functions
  /** Identifier for function to translate the display upwards */
  int TRANSLATE_UP = 0;

  /** Identifier for function to translate the display downwards */
  int TRANSLATE_DOWN = 1;

  /** Identifier for function to translate the display to the left */
  int TRANSLATE_LEFT = 2;

  /** Identifier for function to translate the display to the right */
  int TRANSLATE_RIGHT = 3;

  /** Identifier for function to zoom in the display */
  int ZOOM_IN = 4;

  /** Identifier for function to zoom out the display */
  int ZOOM_OUT = 5;

  /** 
   * Identifier for function to reset the display to the original projection
   * or last saved projection 
   * @see visad.ProjectionControl#resetProjection()
   */
  int RESET = 6;

  /** 
   * Mask to indicate there are no modifiers for this key.
   * @see #mapKeyToFunction(int function, int keycode, int modifiers)
   */
  int NO_MASK = 0;

  /**
   * Maps key represented by keycode & modifiers to the given function.
   * Each function can only have one key/modifier combination assigned 
   * to it at a time.
   * @see java.awt.event.KeyEvent
   * @see java.awt.event.InputEvent
   * @param  function  keyboard function (TRANSLATE_UP, ZOOM_IN, etc)
   * @param  keycode   <CODE>KeyEvent</CODE> virtual keycodes 
   * @param  modifiers <CODE>InputEvent</CODE> key mask
   */
  void mapKeyToFunction(int function, int keycode, int modifiers);

  /**
   *  Process a key event.  Determines whether a meaningful key was pressed.
   *  place.
   *  @param  event  KeyEvent stimulus
   */
  void processKeyEvent(KeyEvent event);

  /** 
   * Executes the given function. 
   * @param  function   function to perform (TRANSLATE_UP, ZOOM_IN, etc)
   */
  void execFunction(int function);
}
