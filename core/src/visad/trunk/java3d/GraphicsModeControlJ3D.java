
//
// GraphicsModeControlJ3D.java
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

package visad.java3d;
 
import visad.*;

import javax.media.j3d.*;

/**
   GraphicsModeControlJ3D is the VisAD class for controlling various
   mode settings for rendering.<P>

   A GraphicsModeControlJ3D is not linked to any DisplayRealType or
   ScalarMap.  It is linked to a DisplayImpl.<P>
*/
public class GraphicsModeControlJ3D extends Control
       implements GraphicsModeControl {

  private float lineWidth; // for LineAttributes; >= 1.0
  private float pointSize; // for PointAttributes; >= 1.0
  private boolean pointMode; // true => points in place of lines and surfaces
  /** for TransparencyAttributes; see list below in setTransparencyMode */
  private int transparencyMode;
  /** View.PARALLEL_PROJECTION or View.PERSPECTIVE_PROJECTION */
  private int projectionPolicy;

  public GraphicsModeControlJ3D(DisplayImpl d) {
    super(d);
    lineWidth = 1.0f;
    pointSize = 1.0f;
    pointMode = false;
    // note SCREEN_DOOR does not seem to work with variable transparency
    transparencyMode = TransparencyAttributes.FASTEST; // J3D
  }
 
  public boolean getMode2D() {
    return getDisplayRenderer().getMode2D();
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public void setLineWidth(float width) throws VisADException {
    if (width < 1.0f) {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setLineWidth: width < 1.0");
    }
    lineWidth = width;
    changeControl();
  }

  public float getPointSize() {
    return pointSize;
  }

  public void setPointSize(float size) throws VisADException {
    if (size < 1.0f) {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setPointSize: size < 1.0");
    }
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

  public void setTransparencyMode(int mode) throws VisADException { // J3D
    if (mode == TransparencyAttributes.SCREEN_DOOR ||
        mode == TransparencyAttributes.BLENDED ||
        mode == TransparencyAttributes.NONE ||
        mode == TransparencyAttributes.FASTEST ||
        mode == TransparencyAttributes.NICEST) {
      transparencyMode = mode;
      changeControl();
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setTransparencyMode: bad mode");
    }
  }

  public void setProjectionPolicy(int policy) throws VisADException { // J3D
    if (policy == View.PARALLEL_PROJECTION ||
        policy == View.PERSPECTIVE_PROJECTION) {
      projectionPolicy = policy;
      DisplayRendererJ3D displayRenderer =
        (DisplayRendererJ3D) getDisplayRenderer();
      if (displayRenderer != null) {
        displayRenderer.getView().setProjectionPolicy(projectionPolicy);
      }
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setProjectionPolicy: bad policy");
    }
  }

}

