//
// ClientDisplayRendererJ3D.java
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

package visad.cluster;

import visad.*;
import visad.java3d.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.rmi.RemoteException;

import java.util.*;


/**
 * <CODE>ClientDisplayRendererJ3D</CODE> is the DisplayRenderer
 * for cluster clients.<P>
 */
public class ClientDisplayRendererJ3D extends DefaultDisplayRendererJ3D {

  private boolean cluster = true;

  /**
   * This is the <CODE>DisplayRenderer</CODE> used for cluster clients.
   */
  public ClientDisplayRendererJ3D () {
    super();
  }

  public DataRenderer makeDefaultRenderer() {
    return new ClientRendererJ3D();
  }

  public boolean legalDataRenderer(DataRenderer renderer) {
    return (renderer instanceof ClientRendererJ3D);
  }

  void setCluster(boolean cl) {
    cluster = cl;
  }

  public void autoscale(Vector temp, Vector tmap, boolean go,
                        boolean initialize) 
         throws VisADException, RemoteException {

    if (!cluster) super.autoscale(temp, tmap, go, initialize);

    DataShadow shadow = null;
    Enumeration renderers = temp.elements();
    while (renderers.hasMoreElements()) {
      DataRenderer renderer = (DataRenderer) renderers.nextElement();
      shadow = renderer.prepareAction(go, initialize, shadow);
    }

    if (shadow != null) {
      // apply RealType ranges and animationSampling
      Enumeration maps = tmap.elements();
      while(maps.hasMoreElements()) {
        ScalarMap map = ((ScalarMap) maps.nextElement());
        map.setRange(shadow);
      }
    }

    ScalarMap.equalizeFlow(tmap, Display.DisplayFlow1Tuple);
    ScalarMap.equalizeFlow(tmap, Display.DisplayFlow2Tuple);
  }

}

