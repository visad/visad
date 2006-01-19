//
// DiscoverableZoom.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

  private float latmul, lonmul;
  private DataRenderer[] renderers = null;
  private boolean[] enabled = null;
  private int nrenderers = -1;
  private float[] lons = null;
  private float[] lats = null;

  /** the DataRenderers in the rs array are assumed to each link to
      a Tuple data object that includes Real fields for Latitude and
      Longitude, and that Latitude and Longitude are mapped to spatial
      DisplayRealTypes; the DataRenderers in rs are enabled or disabled
      to maintain a roughly constant spacing among their visible
      depictions;
      distance is the scale for distance;
      in order to work, this DiscoverableZoom must be added as a
      Control Listener to the ProjectionControl of the DisplayImpl
      linked to the DataRenderer array rs;
      see CollectiveBarbManipulation.java for an example of use */
  public void setRenderers(DataRenderer[] rs, float distance)
         throws VisADException, RemoteException {
    renderers = rs;
    if (renderers != null) {
      nrenderers = renderers.length;
      if (nrenderers == 0) {
        nrenderers = -1;
        return;
      }
      lons = new float[nrenderers];
      lats = new float[nrenderers];
      enabled = new boolean[nrenderers];
      for (int i=0; i<nrenderers; i++) {
        lons[i] = Float.NaN;
        lats[i] = Float.NaN;
        enabled[i] = true;
        DataDisplayLink[] links = renderers[i].getLinks();
        if (links == null || links.length == 0) continue;
        Data data = links[0].getData();
        if (data == null || !(data instanceof Tuple)) continue;
        Real[] reals = ((Tuple) data).getRealComponents();
        if (reals == null || reals.length == 0) continue;
        for (int j=0; j<reals.length; j++) {
          if (RealType.Latitude.equals(reals[j].getType())) {
            lats[i] = (float) reals[j].getValue();
          }
          else if (RealType.Longitude.equals(reals[j].getType())) {
            lons[i] = (float) reals[j].getValue();
          }
        }
        if (lats[i] != lats[i] || lons[i] != lons[i]) {
          lons[i] = Float.NaN; 
          lats[i] = Float.NaN;
        }
      }
      DisplayImpl display = renderers[0].getDisplay();
      if (display == null) {
        nrenderers = -1;
        return;
      }

      ScalarMap latmap = null;
      ScalarMap lonmap = null;
      Vector mapVector = display.getMapVector();
      Enumeration maps = mapVector.elements();
      while (maps.hasMoreElements()) {
        ScalarMap map = (ScalarMap) maps.nextElement();
        ScalarType real = map.getScalar();
        DisplayRealType dreal = map.getDisplayScalar();
        DisplayTupleType rtuple = dreal.getTuple();
        if (rtuple != null) {
          if (rtuple.equals(Display.DisplaySpatialCartesianTuple) ||
              (rtuple.getCoordinateSystem() != null &&
               rtuple.getCoordinateSystem().getReference().equals(
               Display.DisplaySpatialCartesianTuple))) {
            // dreal is spatial
            if (RealType.Latitude.equals(real)) {
              latmap = map;
            }
            else if (RealType.Longitude.equals(real)) {
              lonmap = map;
            }
          }
        }
      } // end while (enum.hasMoreElements())
      if (latmap == null || lonmap == null) {
        nrenderers = -1;
        return;
      }
      double[] latrange = latmap.getRange();
      double[] lonrange = lonmap.getRange();
      latmul = (float) (1.0 / (Math.abs(latrange[1] - latrange[0]) * distance));
      lonmul = (float) (1.0 / (Math.abs(lonrange[1] - lonrange[0]) * distance));
      if (latmul != latmul || lonmul != lonmul) {
        nrenderers = -1;
        return;
      }
    }
    else { // renderers == null
      nrenderers = -1;
      return;
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
      if (nrenderers < 0) return;
      float cscale = (float) (base_scale / scale[0]);
      float ratio = cscale / last_cscale;
      if (ratio < 0.95f || 1.05f < ratio) {
// System.out.println(latmul + " " + lonmul + " " + cscale);
        last_cscale = cscale;
        for (int i=0; i<nrenderers; i++) {
          boolean enable = true;
          if (lats[i] == lats[i] && lons[i] == lons[i]) {
            for (int j=0; j<i; j++) {
              if (enabled[j] && lats[j] == lats[j] && lons[j] == lons[j]) {
                float latd = latmul * (lats[j] - lats[i]) / cscale;
                float lond = lonmul * (lons[j] - lons[i]) / cscale;
                float distsq = (latd * latd + lond * lond);
// System.out.println(i + " " + lats[i] + " " + lons[i] + " " +
//                    j + " " + lats[j] + " " + lons[j] + " " +
//                    latd + " " + lond + " " + distsq);
                if (distsq < 1.0f) {
                  enable = false;
                  break;
                }
              }
            }
          }
// System.out.println("enabled[" + i + "] = " + enable);
          enabled[i] = enable;
          renderers[i].toggle(enable);
        }
      }
    }
  }
}

