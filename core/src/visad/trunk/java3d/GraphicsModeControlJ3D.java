//
// GraphicsModeControlJ3D.java
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

package visad.java3d;
 
import visad.*;

import java.rmi.*;
import java.util.Enumeration;

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
  /** PolygonAttributes.POLYGON_FILL, PolygonAttributes.POLYGON_LINE
      or PolygonAttributes.POLYGON_POINT */
  private int polygonMode;

  private boolean missingTransparent = false;
  private int curvedSize = 10;

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
    polygonMode = PolygonAttributes.POLYGON_FILL;

    projectionPolicy = View.PERSPECTIVE_PROJECTION;
    DisplayRendererJ3D displayRenderer =
      (DisplayRendererJ3D) getDisplayRenderer();
    if (displayRenderer != null) {
      if (displayRenderer.getMode2D()) {
        projectionPolicy = View.PARALLEL_PROJECTION;
        // for some strange reason, if we set PERSPECTIVE_PROJECTION at this
        // point, we can never set PARALLEL_PROJECTION
        displayRenderer.getView().setProjectionPolicy(projectionPolicy);
      }
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
    if (width < 1.0f) width = 1.0f;
    lineWidth = width;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  public void setLineWidth(float width, boolean dummy) {
    if (width < 1.0f) width = 1.0f;
    lineWidth = width;
  }

  public float getPointSize() {
    return pointSize;
  }

  public void setPointSize(float size)
         throws VisADException, RemoteException {
    if (size < 1.0f) size = 1.0f;
    pointSize = size;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  public void setPointSize(float size, boolean dummy) {
    if (size < 1.0f) size = 1.0f;
    pointSize = size;
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

  public void setPolygonMode(int mode)
         throws VisADException, RemoteException {
    if (mode == PolygonAttributes.POLYGON_FILL ||
        mode == PolygonAttributes.POLYGON_LINE ||
        mode == PolygonAttributes.POLYGON_POINT) {
      polygonMode = mode;
      changeControl(true);
      getDisplay().reDisplayAll();
    }
    else {
      throw new DisplayException("GraphicsModeControlJ3D." +
                                 "setPolygonMode: bad mode");
    }
  }

  public int getPolygonMode() {
    return polygonMode;
  }

  public boolean getMissingTransparent() {
    return missingTransparent;
  }

  public void setMissingTransparent(boolean missing) {
    missingTransparent = missing;
  }

  public int getCurvedSize() {
    return curvedSize;
  }
 
  public void setCurvedSize(int curved_size) {
    curvedSize = curved_size;
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
    mode.missingTransparent = missingTransparent;
    mode.polygonMode = polygonMode;
    mode.curvedSize = curvedSize;
    return mode;
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof GraphicsModeControlJ3D)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    GraphicsModeControlJ3D rmtCtl = (GraphicsModeControlJ3D )rmt;

    boolean changed = false;
    boolean redisplay = false;

    if (Math.abs(lineWidth - rmtCtl.lineWidth) > 0.0001) {
      changed = true;
      redisplay = true;
      lineWidth = rmtCtl.lineWidth;
    }
    if (Math.abs(pointSize - rmtCtl.pointSize) > 0.0001) {
      changed = true;
      redisplay = true;
      pointSize = rmtCtl.pointSize;
    }

    if (pointMode != rmtCtl.pointMode) {
      changed = true;
      redisplay = true;
      pointMode = rmtCtl.pointMode;
    }
    if (textureEnable != rmtCtl.textureEnable) {
      changed = true;
      redisplay = true;
      textureEnable = rmtCtl.textureEnable;
    }
    if (scaleEnable != rmtCtl.scaleEnable) {
      changed = true;
      getDisplayRenderer().setScaleOn(scaleEnable);
      scaleEnable = rmtCtl.scaleEnable;
    }

    if (transparencyMode != rmtCtl.transparencyMode) {
      changed = true;
      redisplay = true;
      transparencyMode = rmtCtl.transparencyMode;
    }
    if (projectionPolicy != rmtCtl.projectionPolicy) {
      changed = true;
      redisplay = true;
      projectionPolicy = rmtCtl.projectionPolicy;

      DisplayRendererJ3D displayRenderer;
      displayRenderer = (DisplayRendererJ3D) getDisplayRenderer();
      if (displayRenderer != null) {
        displayRenderer.getView().setProjectionPolicy(projectionPolicy);
      }
    }
    if (polygonMode != rmtCtl.polygonMode) {
      changed = true;
      polygonMode = rmtCtl.polygonMode;
    }

    if (missingTransparent != rmtCtl.missingTransparent) {
      changed = true;
      missingTransparent = rmtCtl.missingTransparent;
    }

    if (curvedSize != rmtCtl.curvedSize) {
      changed = true;
      curvedSize = rmtCtl.curvedSize;
    }

    if (changed) {
      try {
        changeControl(true);
      } catch (RemoteException re) {
        throw new VisADException("Could not indicate that control" +
                                 " changed: " + re.getMessage());
      }
    }
    if (redisplay) {
      getDisplay().reDisplayAll();
    }
  }

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    GraphicsModeControlJ3D gmc = (GraphicsModeControlJ3D )o;

    boolean changed = false;

    if (Math.abs(lineWidth - gmc.lineWidth) > 0.0001) {
      return false;
    }
    if (Math.abs(pointSize - gmc.pointSize) > 0.0001) {
      return false;
    }

    if (pointMode != gmc.pointMode) {
      return false;
    }
    if (textureEnable != gmc.textureEnable) {
      return false;
    }
    if (scaleEnable != gmc.scaleEnable) {
      return false;
    }

    if (transparencyMode != gmc.transparencyMode) {
      return false;
    }
    if (projectionPolicy != gmc.projectionPolicy) {
      return false;
    }
    if (polygonMode != gmc.polygonMode) {
      return false;
    }

    if (missingTransparent != gmc.missingTransparent) {
      return false;
    }

    if (curvedSize != gmc.curvedSize) {
      return false;
    }

    return true;
  }
}
