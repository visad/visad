
//
// GraphicsModeControl.java
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

import javax.media.j3d.*;
import java.vecmath.*;

/**
   GraphicsModeControl is the VisAD class for controlling various
   mode settings for rendering.<P>

   A GraphicsModeControl is not linked to any DisplayRealType or
   ScalarMap.  It is linked to a DisplayImpl.<P>
*/
public class GraphicsModeControl extends Control {

  private float lineWidth;
  private float pointSize;
  private boolean pointMode;
  private int transparencyMode;

  static final GraphicsModeControl prototype = new GraphicsModeControl();

  public GraphicsModeControl(DisplayImpl d) {
    super(d);
    lineWidth = 1.0f;
    pointSize = 1.0f;
    pointMode = false;
    transparencyMode = TransparencyAttributes.SCREEN_DOOR;
  }
 
  GraphicsModeControl() {
    this(null);
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public void setLineWidth(float width) {
    lineWidth = width;
    changeControl();
  }

  public float getPointSize() {
    return pointSize;
  }

  public void setPointSize(float size) {
    pointSize = size;
    changeControl();
  }

  public boolean getPointMode() {
    return pointMode;
  }

  public void setPointMode(boolean mode) {
    pointMode = mode;
    changeControl();
  }

  public int getTransparencyMode() {
    return transparencyMode;
  }

  public void setTransparencyMode(int mode) throws VisADException {
    if (mode == TransparencyAttributes.SCREEN_DOOR ||
        mode == TransparencyAttributes.BLENDED ||
        mode == TransparencyAttributes.NONE ||
        mode == TransparencyAttributes.FASTEST ||
        mode == TransparencyAttributes.NICEST) {
      transparencyMode = mode;
      changeControl();
    }
    else {
      throw new DisplayException("GraphicsModeControl." +
                                 "setTransparencyMode: bad mode");
    }
  }

  public Control cloneButContents(DisplayImpl d) {
    GraphicsModeControl control = new GraphicsModeControl(d);
    return control;
  }

}

