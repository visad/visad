
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

import java.rmi.*;

import javax.media.j3d.*;

/**
   GraphicsModeControlJ3D is the VisAD class for controlling various
   mode settings for rendering.<P>

   A GraphicsModeControlJ3D is not linked to any DisplayRealType or
   ScalarMap.  It is linked to a DisplayImpl.<P>
*/
public class GraphicsModeControlJ3D extends GraphicsModeControl {

  private float lineWidth; // for LineAttributes; >= 1.0
  private float pointSize; // for PointAttributes; >= 1.0
  private boolean pointMode; // true => points in place of lines and surfaces
  private boolean textureEnable; // true => allow use of texture mapping
  private boolean scaleEnable; // true => display X, Y and Z scales

  /** for TransparencyAttributes; see list below in setTransparencyMode */
  private int transparencyMode;
  /** View.PARALLEL_PROJECTION or View.PERSPECTIVE_PROJECTION */
  private int projectionPolicy;

  public GraphicsModeControlJ3D(DisplayImpl d) {
    super(d);
    lineWidth = 1.0f;
    pointSize = 1.0f;
    pointMode = false;
    textureEnable = true;
    scaleEnable = false;
    // NICEST, FASTEST and BLENDED do not solve the depth precedence problem
    // note SCREEN_DOOR does not seem to work with variable transparency
    // transparencyMode = TransparencyAttributes.NICEST;
    transparencyMode = TransparencyAttributes.FASTEST;
    // transparencyMode = TransparencyAttributes.BLENDED;
    // transparencyMode = TransparencyAttributes.SCREEN_DOOR;

    projectionPolicy = View.PERSPECTIVE_PROJECTION;
    DisplayRendererJ3D displayRenderer =
      (DisplayRendererJ3D) getDisplayRenderer();
    if (displayRenderer != null) {
      if (displayRenderer.getMode2D()) {
        projectionPolicy = View.PARALLEL_PROJECTION;
      }
      displayRenderer.getView().setProjectionPolicy(projectionPolicy);
    }
  }
 
  public boolean getMode2D() {
    return getDisplayRenderer().getMode2D();
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public void setLineWidth(float width)
         throws VisADException, RemoteException {
    if (width < 1.0f) {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setLineWidth: width < 1.0");
    }
    lineWidth = width;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  public void setLineWidth(float width, boolean dummy) {
    if (width >= 1.0f) {
      lineWidth = width;
    }
  }

  public float getPointSize() {
    return pointSize;
  }

  public void setPointSize(float size)
         throws VisADException, RemoteException {
    if (size < 1.0f) {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setPointSize: size < 1.0");
    }
    pointSize = size;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  public void setPointSize(float size, boolean dummy) {
    if (size >= 1.0f) {
      pointSize = size;
    }
  }

  public boolean getPointMode() {
    return pointMode;
  }

  public void setPointMode(boolean mode)
         throws VisADException, RemoteException {
    pointMode = mode;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  public void setTextureEnable(boolean enable)
         throws VisADException, RemoteException {
    textureEnable = enable;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  public boolean getTextureEnable() {
    return textureEnable;
  }

  public void setScaleEnable(boolean enable)
         throws VisADException, RemoteException {
    scaleEnable = enable;
    getDisplayRenderer().setScaleOn(enable);
    changeControl(true);
  }
 
  public boolean getScaleEnable() {
    return scaleEnable;
  }

  public int getTransparencyMode() {
    return transparencyMode;
  }

  public void setTransparencyMode(int mode)
         throws VisADException, RemoteException {
    if (mode == TransparencyAttributes.SCREEN_DOOR ||
        mode == TransparencyAttributes.BLENDED ||
        mode == TransparencyAttributes.NONE ||
        mode == TransparencyAttributes.FASTEST ||
        mode == TransparencyAttributes.NICEST) {
      transparencyMode = mode;
      changeControl(true);
      getDisplay().reDisplayAll();
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setTransparencyMode: bad mode");
    }
  }

  public void setProjectionPolicy(int policy)
         throws VisADException, RemoteException {
    if (policy == View.PARALLEL_PROJECTION ||
        policy == View.PERSPECTIVE_PROJECTION) {
      projectionPolicy = policy;
      DisplayRendererJ3D displayRenderer =
        (DisplayRendererJ3D) getDisplayRenderer();
      if (displayRenderer != null) {
        displayRenderer.getView().setProjectionPolicy(projectionPolicy);
      }
      changeControl(true);
      getDisplay().reDisplayAll();
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setProjectionPolicy: bad policy");
    }
  }

  public int getProjectionPolicy() {
    return projectionPolicy;
  }

  public Object clone() {
    GraphicsModeControlJ3D mode =
      new GraphicsModeControlJ3D(getDisplay());
    mode.lineWidth = lineWidth;
    mode.pointSize = pointSize;
    mode.pointMode = pointMode;
    mode.textureEnable = textureEnable;
    mode.scaleEnable = scaleEnable;
    mode.transparencyMode = transparencyMode;
    mode.projectionPolicy = projectionPolicy;
    return mode;
  }

}

