//
// ShadowBarbRealTupleTypeJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java3d.*;

import java.rmi.*;

/**
   The ShadowBarbRealTupleTypeJ3D class shadows the RealTupleType class
   for BarbManipulationRendererJ3D and BarbRendererJ3D, within a
   DataDisplayLink, under Java3D.<P>
*/
public class ShadowBarbRealTupleTypeJ3D extends ShadowRealTupleTypeJ3D {

  public ShadowBarbRealTupleTypeJ3D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  public VisADGeometryArray[] makeFlow(int which, float[][] flow_values,
                float flowScale, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
         throws VisADException {

    DataRenderer renderer = getLink().getRenderer();
    boolean direct = renderer.getIsDirectManipulation();
    if (direct && renderer instanceof BarbManipulationRendererJ3D) {
      return ShadowBarbRealTupleTypeJ3D.staticMakeFlow(getDisplay(), which,
                 flow_values, flowScale, spatial_values, color_values,
                 range_select, renderer, true);
    }
    else {
      return ShadowBarbRealTupleTypeJ3D.staticMakeFlow(getDisplay(), which,
                 flow_values, flowScale, spatial_values, color_values,
                 range_select, renderer, false);
    }
  }


  private static final int NUM = 1024;

  public static VisADGeometryArray[] staticMakeFlow(DisplayImpl display,
               int which, float[][] flow_values, float flowScale,
               float[][] spatial_values, byte[][] color_values,
               boolean[][] range_select, DataRenderer renderer, boolean direct)
         throws VisADException {
    if (flow_values[0] == null) return null;
    if (spatial_values[0] == null) return null;

    int len = spatial_values[0].length;
    int flen = flow_values[0].length;
    int rlen = 0; // number of non-missing values
    if (range_select[0] == null) {
      rlen = len;
    }
    else {
      for (int j=0; j<range_select[0].length; j++) {
        if (range_select[0][j]) rlen++;
      }
    }
    if (rlen == 0) return null;

    // WLH 3 June 99
    boolean[] south = new boolean[len];
    float[][] earth_locs = renderer.spatialToEarth(spatial_values);
    if (earth_locs != null) {
      for (int i=0; i<len; i++) south[i] = (earth_locs[0][i] < 0.0f);
    }
    else {
      // if no latitude information is available, get value set in FlowControl
      // default = south  where BOM is
      FlowControl fcontrol = null;
      if (which == 0) {
        fcontrol = (FlowControl) display.getControl(Flow1Control.class);
      }
      else if (which == 1) {
        fcontrol = (FlowControl) display.getControl(Flow2Control.class);
      }
      if (fcontrol == null) {
        throw new VisADException(
          "ShadowBarbRealTupleTypeJ3D: Unable to get FlowControl");
      }
      boolean isSouth =
        (fcontrol.getBarbOrientation() == FlowControl.SH_ORIENTATION);
      for (int i=0; i<len; i++) south[i] = isSouth;
    }

    // use default flowScale = 0.02f here, since flowScale for barbs is
    // just for barb size
    flow_values = adjustFlowToEarth(which, flow_values, spatial_values,
                                    0.02f, renderer);

    float[] vx = new float[NUM];
    float[] vy = new float[NUM];
    float[] vz = new float[NUM];
    float[] tx = new float[NUM];
    float[] ty = new float[NUM];
    float[] tz = new float[NUM];
    byte[] vred = null;
    byte[] vgreen = null;
    byte[] vblue = null;
    byte[] tred = null;
    byte[] tgreen = null;
    byte[] tblue = null;
    if (color_values != null) {
      vred = new byte[NUM];
      vgreen = new byte[NUM];
      vblue = new byte[NUM];
      tred = new byte[NUM];
      tgreen = new byte[NUM];
      tblue = new byte[NUM];
    }
    int[] numv = {0};
    int[] numt = {0};

    float scale = flowScale; // ????
    float pt_size = 0.25f * flowScale; // ????

    // flow vector
    float f0 = 0.0f, f1 = 0.0f, f2 = 0.0f;
    for (int j=0; j<len; j++) {
      if (range_select[0] == null || range_select[0][j]) {
// NOTE - must scale to knots
        if (flen == 1) {
          f0 = flow_values[0][0];
          f1 = flow_values[1][0];
          f2 = flow_values[2][0];
        }
        else {
          f0 = flow_values[0][j];
          f1 = flow_values[1][j];
          f2 = flow_values[2][j];
        }

        if (numv[0] + NUM/4 > vx.length) {
          float[] cx = vx;
          float[] cy = vy;
          float[] cz = vz;
          int l = 2 * vx.length;
          vx = new float[l];
          vy = new float[l];
          vz = new float[l];
          System.arraycopy(cx, 0, vx, 0, cx.length);
          System.arraycopy(cy, 0, vy, 0, cy.length);
          System.arraycopy(cz, 0, vz, 0, cz.length);
          if (color_values != null) {
            byte[] cred = vred;
            byte[] cgreen = vgreen;
            byte[] cblue = vblue;
            vred = new byte[l];
            vgreen = new byte[l];
            vblue = new byte[l];
            System.arraycopy(cred, 0, vred, 0, cred.length);
            System.arraycopy(cgreen, 0, vgreen, 0, cgreen.length);
            System.arraycopy(cblue, 0, vblue, 0, cblue.length);
          }
        }
        if (numt[0] + NUM/4 > tx.length) {
          float[] cx = tx;
          float[] cy = ty;
          float[] cz = tz;
          int l = 2 * tx.length;
          tx = new float[l];
          ty = new float[l];
          tz = new float[l];
          System.arraycopy(cx, 0, tx, 0, cx.length);
          System.arraycopy(cy, 0, ty, 0, cy.length);
          System.arraycopy(cz, 0, tz, 0, cz.length);
          if (color_values != null) {
            byte[] cred = tred;
            byte[] cgreen = tgreen;
            byte[] cblue = tblue;
            tred = new byte[l];
            tgreen = new byte[l];
            tblue = new byte[l];
            System.arraycopy(cred, 0, tred, 0, cred.length);
            System.arraycopy(cgreen, 0, tgreen, 0, cgreen.length);
            System.arraycopy(cblue, 0, tblue, 0, cblue.length);
          }
        }
        int oldnv = numv[0];
        int oldnt = numt[0];
        float mbarb[] =
          ((BarbRenderer) renderer).makeVector(south[j],
                   spatial_values[0][j], spatial_values[1][j],
                   spatial_values[2][j], scale, pt_size, f0, f1, vx, vy, vz,
                   numv, tx, ty, tz, numt);
        if (direct) {
          ((BarbManipulationRendererJ3D) renderer).
            setVectorSpatialValues(mbarb, which);
        }
        int nv = numv[0];
        int nt = numt[0];
        if (color_values != null) {
          if (color_values[0].length > 1) {
            for (int i=oldnv; i<nv; i++) {
              vred[i] = color_values[0][j];
              vgreen[i] = color_values[1][j];
              vblue[i] = color_values[2][j];
            }
            for (int i=oldnt; i<nt; i++) {
              tred[i] = color_values[0][j];
              tgreen[i] = color_values[1][j];
              tblue[i] = color_values[2][j];
            }
          }
          else {  // if (color_values[0].length == 1)
            for (int i=oldnv; i<nv; i++) {
              vred[i] = color_values[0][0];
              vgreen[i] = color_values[1][0];
              vblue[i] = color_values[2][0];
            }
            for (int i=oldnt; i<nt; i++) {
              tred[i] = color_values[0][0];
              tgreen[i] = color_values[1][0];
              tblue[i] = color_values[2][0];
            }
          }
        }
      } // end if (range_select[0] == null || range_select[0][j])
    } // end for (int j=0; j<len; j++)

    int nv = numv[0];
    int nt = numt[0];
    if (nv == 0) return null;

    VisADGeometryArray[] arrays = null;
    VisADLineArray array = new VisADLineArray();
    array.vertexCount = nv;

    float[] coordinates = new float[3 * nv];

    int m = 0;
    for (int i=0; i<nv; i++) {
      coordinates[m++] = vx[i];
      coordinates[m++] = vy[i];
      coordinates[m++] = vz[i];
    }
    array.coordinates = coordinates;

    byte[] colors = null;
    if (color_values != null) {
      colors = new byte[3 * nv];
      m = 0;
      for (int i=0; i<nv; i++) {
        colors[m++] = vred[i];
        colors[m++] = vgreen[i];
        colors[m++] = vblue[i];
      }
      array.colors = colors;
    }

    VisADTriangleArray tarray = null;
    if (nt > 0) {
      tarray = new VisADTriangleArray();
      tarray.vertexCount = nt;

      coordinates = new float[3 * nt];
      float[] normals = new float[3 * nt];

      m = 0;
      for (int i=0; i<nt; i++) {
        coordinates[m++] = tx[i];
        coordinates[m++] = ty[i];
        coordinates[m++] = tz[i];
      }
      tarray.coordinates = coordinates;

      m = 0;
      for (int i=0; i<nt; i++) {
        normals[m++] = 0.0f;
        normals[m++] = 0.0f;
        normals[m++] = 1.0f;
      }
      tarray.normals = normals;

      if (color_values != null) {
        colors = new byte[3 * nt];
        m = 0;
        for (int i=0; i<nt; i++) {
          colors[m++] = tred[i];
          colors[m++] = tgreen[i];
          colors[m++] = tblue[i];
        }
        tarray.colors = colors;
      }

      // WLH 30 May 2002
      array = (VisADLineArray) array.adjustLongitudeBulk(renderer);
      tarray = (VisADTriangleArray) tarray.adjustLongitudeBulk(renderer);

      arrays = new VisADGeometryArray[] {array, tarray};
    }
    else {

      // WLH 30 May 2002
      array = (VisADLineArray) array.adjustLongitudeBulk(renderer);

      arrays = new VisADGeometryArray[] {array};
    }

    return arrays;
  }

}

