//
// GraphicsModeControlJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java2d;

import java.rmi.*;

import visad.*;

import visad.util.Util;

/**
   GraphicsModeControlJ2D is the VisAD class for controlling various
   mode settings for rendering.<P>

   A GraphicsModeControlJ2D is not linked to any DisplayRealType or
   ScalarMap.  It is linked to a DisplayImpl.<P>
*/
public class GraphicsModeControlJ2D extends GraphicsModeControl {

  static final String[] LINE_STYLE = {
    "solid", "dash", "dot", "dash-dot"
  };

  private float lineWidth; // for LineAttributes; >= 1.0
  private float pointSize; // for PointAttributes; >= 1.0
  private int lineStyle; // for LineAttributes
  private boolean pointMode; // true => points in place of lines and surfaces
  private boolean textureEnable; // true => allow use of texture mapping
  private boolean scaleEnable; // true => display X, Y and Z scales

  private int transparencyMode = 0;
  private int projectionPolicy = 0;
  private int polygonMode = 0;

  private boolean missingTransparent = true;
  private int curvedSize = 10;

  public GraphicsModeControlJ2D(DisplayImpl d) {
    super(d);
    lineWidth = 1.0f;
    pointSize = 1.0f;
    lineStyle = SOLID_STYLE;
    pointMode = false;
    textureEnable = true;
    scaleEnable = false;
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
      throw new DisplayException("GraphicsModeControlJ2D." +
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
      throw new DisplayException("GraphicsModeControlJ2D." +
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

  public int getLineStyle() {
    return lineStyle;
  }

  public void setLineStyle(int style)
         throws VisADException, RemoteException {
    if (style != SOLID_STYLE && style != DASH_STYLE &&
      style != DOT_STYLE && style != DASH_DOT_STYLE)
    {
      style = SOLID_STYLE;
    }
    lineStyle = style;
    changeControl(true);
    getDisplay().reDisplayAll();
  }

  public void setLineStyle(int style, boolean dummy) {
    if (style != SOLID_STYLE && style != DASH_STYLE &&
      style != DOT_STYLE && style != DASH_DOT_STYLE)
    {
      style = SOLID_STYLE;
    }
    lineStyle = style;
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
    if (mode == 0) {
      transparencyMode = mode;
    }
    else {
      throw new DisplayException("GraphicsModeControlJ2D." +
                                 "setTransparencyMode: bad mode");
    }
  }

  public void setProjectionPolicy(int policy)
         throws VisADException, RemoteException {
    if (policy == 0) {
      projectionPolicy = policy;
    }
    else {
      throw new DisplayException("GraphicsModeControlJ2D." +
                                 "setProjectionPolicy: bad policy");
    }
  }

  public int getProjectionPolicy() {
    return projectionPolicy;
  }

  public void setPolygonMode(int mode)
         throws VisADException, RemoteException {
    if (mode == 0) {
      polygonMode = mode;
    }
    else {
      throw new DisplayException("GraphicsModeControlJ2D." +
                                 "setPolygonMode: bad mode");
    }
  }

  public int getPolygonMode() {
    return polygonMode;
  }

  public boolean getMissingTransparent() {
    return missingTransparent;
  }

  public void setMissingTransparent(boolean missing)
         throws VisADException, RemoteException {
    if (!missing) {
      missingTransparent = missing;
    }
    else {
      throw new DisplayException("GraphicsModeControlJ2D." +
                                 "setMissingTransparent: must be false");
    }
  }

  public int getCurvedSize() {
    return curvedSize;
  }

  public void setCurvedSize(int curved_size) {
    curvedSize = curved_size;
  }

  public Object clone() {
    GraphicsModeControlJ2D mode =
      new GraphicsModeControlJ2D(getDisplay());
    mode.lineWidth = lineWidth;
    mode.pointSize = pointSize;
    mode.lineStyle = lineStyle;
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

    if (!(rmt instanceof GraphicsModeControlJ2D)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    GraphicsModeControlJ2D rmtCtl = (GraphicsModeControlJ2D )rmt;

    boolean changed = false;
    boolean redisplay = false;

    if (!Util.isApproximatelyEqual(lineWidth, rmtCtl.lineWidth)) {
      changed = true;
      redisplay = true;
      lineWidth = rmtCtl.lineWidth;
    }
    if (!Util.isApproximatelyEqual(pointSize, rmtCtl.pointSize)) {
      changed = true;
      redisplay = true;
      pointSize = rmtCtl.pointSize;
    }
    if (lineStyle != rmtCtl.lineStyle) {
      changed = true;
      redisplay = true;
      lineStyle = rmtCtl.lineStyle;
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
      scaleEnable = rmtCtl.scaleEnable;
      getDisplayRenderer().setScaleOn(scaleEnable);
    }

    if (transparencyMode != rmtCtl.transparencyMode) {
      changed = true;
      transparencyMode = rmtCtl.transparencyMode;
    }
    if (projectionPolicy != rmtCtl.projectionPolicy) {
      changed = true;
      projectionPolicy = rmtCtl.projectionPolicy;
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

    GraphicsModeControlJ2D gmc = (GraphicsModeControlJ2D )o;

    boolean changed = false;

    if (!Util.isApproximatelyEqual(lineWidth, gmc.lineWidth)) {
      return false;
    }
    if (!Util.isApproximatelyEqual(pointSize, gmc.pointSize)) {
      return false;
    }
    if (lineStyle != gmc.lineStyle) {
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

  public String toString()
  {
    StringBuffer buf = new StringBuffer("GraphicsModeControlJ3D[");

    buf.append("lw ");
    buf.append(lineWidth);
    buf.append(",ps ");
    buf.append(pointSize);
    buf.append(",ls ");
    buf.append(LINE_STYLE[lineStyle]);

    buf.append(pointMode ? "pm" : "!pm");
    buf.append(textureEnable ? "te" : "!te");
    buf.append(scaleEnable ? "se" : "!se");
    buf.append(missingTransparent ? "mt" : "!mt");

    buf.append(",tm ");
    buf.append(transparencyMode);
    buf.append(",pp ");
    buf.append(projectionPolicy);
    buf.append(",pm ");
    buf.append(polygonMode);
    buf.append(",cs ");
    buf.append(curvedSize);

    buf.append(']');
    return buf.toString();
  }
}
