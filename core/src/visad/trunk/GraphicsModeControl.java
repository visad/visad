//
// GraphicsModeControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;
import java.util.StringTokenizer;

/**
   GraphicsModeControl is the VisAD interface class for controlling various
   mode settings for rendering.<P>

   A GraphicsModeControl is not linked to any DisplayRealType or
   ScalarMap.  It is linked to a DisplayImpl.<P>
*/
public abstract class GraphicsModeControl extends Control
       implements Cloneable {

  public GraphicsModeControl(DisplayImpl d) {
    super(d);
  }

  public abstract boolean getMode2D();

  public abstract float getLineWidth();

  /** set the width of line rendering; this is over-ridden by
      ConstantMaps to Display.LineWidth */
  public abstract void setLineWidth(float width)
         throws VisADException, RemoteException;

  public abstract void setLineWidth(float width, boolean dummy);

  public abstract float getPointSize();

  /** set the size for point rendering; this is over-ridden by
      ConstantMaps to Display.PointSize */
  public abstract void setPointSize(float size)
         throws VisADException, RemoteException;

  public abstract void setPointSize(float size, boolean dummy);

  public abstract boolean getPointMode();

  /** if mode is true this will cause some rendering as points
      rather than lines or surfaces */
  public abstract void setPointMode(boolean mode)
         throws VisADException, RemoteException;

  public abstract boolean getTextureEnable();

  /** if enable is true this will enable use of texture
      mapping, where appropriate */
  public abstract void setTextureEnable(boolean enable)
         throws VisADException, RemoteException;

  public abstract boolean getScaleEnable();

  /** if enable is true this will enable numerical
      scales along display spatial axes */
  public abstract void setScaleEnable(boolean enable)
         throws VisADException, RemoteException;

  public abstract int getTransparencyMode();

  /** sets a graphics-API-specific transparency mode (e.g.,
      SCREEN_DOOR, BLENDED) */
  public abstract void setTransparencyMode(int mode)
         throws VisADException, RemoteException;

  /** sets a graphics-API-specific projection policy (e.g.,
      PARALLEL_PROJECTION, PERSPECTIVE_PROJECTION) */
  public abstract void setProjectionPolicy(int policy)
         throws VisADException, RemoteException;

  public abstract int getProjectionPolicy();

  public abstract void setPolygonMode(int mode)
         throws VisADException, RemoteException;

  public abstract int getPolygonMode();

  public abstract void setMissingTransparent(boolean missing)
         throws VisADException, RemoteException;

  public abstract boolean getMissingTransparent();

  public abstract void setCurvedSize(int curved_size);

  public abstract int getCurvedSize();

  /** get a String that can be used to reconstruct this
      GraphicsModeControl later */
  public String getSaveString() {
    return "" +
      getLineWidth() + ' ' +
      getPointSize() + ' ' +
      getPointMode() + ' ' +
      getTextureEnable() + ' ' +
      getScaleEnable() + ' ' +
      getTransparencyMode() + ' ' +
      getProjectionPolicy() + ' ' +
      getPolygonMode() + ' ' +
      getMissingTransparent() + ' ' +
      getCurvedSize();
  }

  /** reconstruct this GraphicsModeControl using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    int numTokens = st.countTokens();
    if (numTokens < 10) throw new VisADException("Invalid save string");

    // determine graphics mode settings
    float lw = toFloat(st.nextToken());
    float ps = toFloat(st.nextToken());
    boolean pm = toBoolean(st.nextToken());
    boolean te = toBoolean(st.nextToken());
    boolean se = toBoolean(st.nextToken());
    int tm = toInt(st.nextToken());
    int pp = toInt(st.nextToken());
    int pm2 = toInt(st.nextToken());
    boolean mt = toBoolean(st.nextToken());
    int cs = toInt(st.nextToken());

    // reset graphics mode settings
    setLineWidth(lw);
    setPointSize(ps);
    setPointMode(pm);
    setTextureEnable(te);
    setScaleEnable(se);
    setTransparencyMode(tm);
    setProjectionPolicy(pp);
    setPolygonMode(pm2);
    setMissingTransparent(mt);
    setCurvedSize(cs);
  }

  /** a method to copy any data object */
  public abstract Object clone();

}

