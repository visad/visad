//
// DiscoverableZoom.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import visad.*;
import visad.util.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;


/**
   DiscoverableZoom is the VisAD class for
   discoverable zoom of displayed data.
*/
public class DiscoverableZoom extends Object
       implements ControlListener {

  private boolean pfirst = true;

  private double base_scale = 1.0;
  private float last_cscale = 1.0f;

  private float distance;
  private DataRenderer[] renderers = null;
  private int nrenderers = -1;
  private float[] lons = null;
  private float[] lats = null;

  public void setRenderers(DataRenderer[] rs, float d)
         throws VisADException, RemoteException {
    distance = d;
    renderers = rs;
    if (renderers != null) {
      nrenderers = renderers.length;
      if (nrenderers == 0) {
        nrenderers = -1;
        return;
      }
      lons = new float[nrenderers];
      lats = new float[nrenderers];
      for (int i=0; i<nrenderers; i++) {
        DataDisplayLink[] links = renderers[i].getLinks();
        if (links == null || links.length == 0) {
          lons[i] = Float.NaN;
          lats[i] = Float.NaN;
          continue;
        }
        Data data = links[0].getData();
        if (data == null || !(data instanceof RealTuple)) {
          lons[i] = Float.NaN;
          lats[i] = Float.NaN;
          continue;
        }

      }
    }
    else {
      nrenderers = -1;
    }
  }

  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
    ProjectionControl pcontrol = (ProjectionControl) e.getControl();
    double[] matrix = pcontrol.getMatrix();
    double[] rot = new double[3];
    double[] scale = new double[1];
    double[] trans = new double[3];
    MouseBehaviorJ3D.unmake_matrix(rot, scale, trans, matrix);

    if (pfirst) {
      pfirst = false;
      base_scale = scale[0];
      last_cscale = 1.0f;
    }
    else {
      float cscale = (float) (base_scale / scale[0]);
      float ratio = cscale / last_cscale;
      if (ratio < 0.95f || 1.05f < ratio) {
        last_cscale = cscale;
        // shape_control1.setScale(cscale);
        // shape_control2.setScale(cscale);
      }
    }
  }
}

