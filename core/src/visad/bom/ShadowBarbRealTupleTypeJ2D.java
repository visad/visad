//
// ShadowBarbRealTupleTypeJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java2d.*;

import java.rmi.*;

/**
   The ShadowBarbRealTupleTypeJ2D class shadows the RealTupleType class
   for BarbManipulationRendererJ2D and BarbRendererJ2D, within a
   DataDisplayLink, under Java2D.<P>
*/
public class ShadowBarbRealTupleTypeJ2D extends ShadowRealTupleTypeJ2D {

  public ShadowBarbRealTupleTypeJ2D(MathType t, DataDisplayLink link,
                                ShadowType parent)
         throws VisADException, RemoteException {
    super(t, link, parent);
  }

  public VisADGeometryArray[] makeFlow(int which, float[][] flow_values,
                float flowScale, float arrowScale, float[][] spatial_values,
                byte[][] color_values, boolean[][] range_select)
         throws VisADException {

    DataRenderer renderer = getLink().getRenderer();
    boolean direct = renderer.getIsDirectManipulation();
    if (direct && renderer instanceof BarbManipulationRendererJ2D) {
      return ShadowBarbRealTupleTypeJ2D.staticMakeFlow(getDisplay(), which,
                 flow_values, flowScale, spatial_values, color_values,
                 range_select, renderer, true);
    }
    else {
      return ShadowBarbRealTupleTypeJ2D.staticMakeFlow(getDisplay(), which,
                 flow_values, flowScale, spatial_values, color_values,
                 range_select, renderer, false);
    }
  }


  private static final int NUM = 512;

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
          "ShadowBarbRealTupleTypeJ2D: Unable to get FlowControl");
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
    float[] tx = new float[NUM];
    float[] ty = new float[NUM];
    byte[] vred = null;
    byte[] vgreen = null;
    byte[] vblue = null;
    byte[] valpha = null;
    byte[] tred = null;
    byte[] tgreen = null;
    byte[] tblue = null;
    byte[] talpha = null;
    int numColors = color_values != null ? color_values.length : 3;
    if (color_values != null) {
      vred = new byte[NUM];
      vgreen = new byte[NUM];
      vblue = new byte[NUM];
      if (numColors == 4) valpha = new byte[NUM];
      tred = new byte[NUM];
      tgreen = new byte[NUM];
      tblue = new byte[NUM];
      if (numColors == 4) talpha = new byte[NUM];
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

        if (numv[0] + NUM/2 > vx.length) {
          float[] cx = vx;
          float[] cy = vy;
          int l = 2 * vx.length;
          vx = new float[l];
          vy = new float[l];
          System.arraycopy(cx, 0, vx, 0, cx.length);
          System.arraycopy(cy, 0, vy, 0, cy.length);
          if (color_values != null) {
            byte[] cred = vred;
            byte[] cgreen = vgreen;
            byte[] cblue = vblue;
            byte[] calpha = valpha;
            vred = new byte[l];
            vgreen = new byte[l];
            vblue = new byte[l];
            if (calpha != null) valpha = new byte[l];
            System.arraycopy(cred, 0, vred, 0, cred.length);
            System.arraycopy(cgreen, 0, vgreen, 0, cgreen.length);
            System.arraycopy(cblue, 0, vblue, 0, cblue.length);
            if (calpha != null) System.arraycopy(calpha, 0, valpha, 0, calpha.length);
          }
        }
        if (numt[0] + NUM/2 > tx.length) {
          float[] cx = tx;
          float[] cy = ty;
          int l = 2 * tx.length;
          tx = new float[l];
          ty = new float[l];
          System.arraycopy(cx, 0, tx, 0, cx.length);
          System.arraycopy(cy, 0, ty, 0, cy.length);
          if (color_values != null) {
            byte[] cred = tred;
            byte[] cgreen = tgreen;
            byte[] cblue = tblue;
            byte[] calpha = talpha;
            tred = new byte[l];
            tgreen = new byte[l];
            tblue = new byte[l];
            if (calpha != null) talpha = new byte[l];
            System.arraycopy(cred, 0, tred, 0, cred.length);
            System.arraycopy(cgreen, 0, tgreen, 0, cgreen.length);
            System.arraycopy(cblue, 0, tblue, 0, cblue.length);
            if (calpha != null) System.arraycopy(calpha, 0, talpha, 0, talpha.length);
          }
        }
        int oldnv = numv[0];
        int oldnt = numt[0];
        float mbarb[] =
          makeBarb(south[j], spatial_values[0][j], spatial_values[1][j],
                   scale, pt_size, f0, f1, vx, vy, numv, tx, ty, numt,
                   renderer);
        if (direct) {
          ((BarbManipulationRendererJ2D) renderer).
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
              if (numColors == 4) valpha[i] = color_values[3][j];
            }
            for (int i=oldnt; i<nt; i++) {
              tred[i] = color_values[0][j];
              tgreen[i] = color_values[1][j];
              tblue[i] = color_values[2][j];
              if (numColors == 4) talpha[i] = color_values[3][j];
            }
          }
          else {  // if (color_values[0].length == 1)
            for (int i=oldnv; i<nv; i++) {
              vred[i] = color_values[0][0];
              vgreen[i] = color_values[1][0];
              vblue[i] = color_values[2][0];
              if (numColors == 4) valpha[i] = color_values[3][0];
            }
            for (int i=oldnt; i<nt; i++) {
              tred[i] = color_values[0][0];
              tgreen[i] = color_values[1][0];
              tblue[i] = color_values[2][0];
              if (numColors == 4) talpha[i] = color_values[3][0];
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
      coordinates[m++] = 0.0f;
    }
    array.coordinates = coordinates;

    byte[] colors = null;
    if (color_values != null) {
      colors = new byte[numColors * nv];
      m = 0;
      for (int i=0; i<nv; i++) {
        colors[m++] = vred[i];
        colors[m++] = vgreen[i];
        colors[m++] = vblue[i];
        if (numColors == 4) colors[m++] = valpha[i];
      }
      array.colors = colors;
    }

    VisADTriangleArray tarray = null;
    if (nt > 0) {
      tarray = new VisADTriangleArray();
      tarray.vertexCount = nt;

      coordinates = new float[3 * nt];
      // float[][] normals = new float[3 * nt]; // not used in Java2D

      m = 0;
      for (int i=0; i<nt; i++) {
        coordinates[m++] = tx[i];
        coordinates[m++] = ty[i];
        coordinates[m++] = 0.0f;
      }
      tarray.coordinates = coordinates;

      if (color_values != null) {
        colors = new byte[numColors * nt];
        m = 0;
        for (int i=0; i<nt; i++) {
          colors[m++] = tred[i];
          colors[m++] = tgreen[i];
          colors[m++] = tblue[i];
          if (numColors == 4) colors[m++] = talpha[i];
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


  /** adapted from Justin Baker's WindBarb, which is adapted from
      Mark Govett's barbs.pro IDL routine
  */
  static float[] makeBarb(boolean south, float x, float y, float scale,
                          float pt_size, float f0, float f1,
                          float[] vx, float[] vy, int[] numv,
                          float[] tx, float[] ty, int[] numt,
                          DataRenderer renderer) {

    float wsp25,slant,barb,d,c195,s195;
    float x0,y0;
    float x1,y1,x2,y2,x3,y3;
    int nbarb50,nbarb10,nbarb5;

    float[] mbarb = new float[4];
    mbarb[0] = x;
    mbarb[1] = y;

    if (!(renderer instanceof BarbRenderer) ||
        ((BarbRenderer) renderer).getKnotsConvert()) {
      // convert meters per second to knots
      f0 *= (3600.0 / 1853.248);
      f1 *= (3600.0 / 1853.248);
    }

    float wnd_spd = (float) Math.sqrt(f0 * f0 + f1 * f1);
    int lenv = vx.length;
    int lent = tx.length;
    int nv = numv[0];
    int nt = numt[0];

    //determine the initial (minimum) length of the flag pole
    if (wnd_spd >= 2.5) {

      wsp25 = (float) Math.max(wnd_spd + 2.5, 5.0);
      slant = 0.15f * scale;
      barb = 0.4f * scale;
      // WLH 6 Aug 99 - barbs point the other way (duh)
      x0 = -f0 / wnd_spd;
      y0 = -f1 / wnd_spd;

      //plot the flag pole
      // lengthen to 'd = 3.0f * barb'
      // was 'd = barb' in original BOM code
      d = 3.0f * barb;
      x1 = (x +x0*d);
      y1 = (y +y0*d);

/*
      // commented out in original BOM code
      vx[nv] = x;
      vy[nv] = y;
      nv++;
      vx[nv] = x1;
      vy[nv] = y1;
      nv++;
      // g.drawLine(x,y,x1,y1);
*/

      //determine number of wind barbs needed for 10 and 50 kt winds
      nbarb50 = (int)(wsp25/50.f);
      nbarb10 = (int)((wsp25 - (nbarb50 * 50.f))/10.f);
      nbarb5 =  (int)((wsp25 - (nbarb50 * 50.f) - (nbarb10 * 10.f))/5.f);

      //2.5 to 7.5 kt winds are plotted with the barb part way done the pole
      if (nbarb5 == 1) {
        barb = barb * 0.4f;
        slant = slant * 0.4f;
        x1 = (x + x0 * d);
        y1 = (y + y0 * d);

        if (south) {
          x2 = (x + x0 * (d + slant) - y0 * barb);
          y2 = (y + y0 * (d + slant) + x0 * barb);
        }
        else {
          x2 = (x + x0 * (d + slant) + y0 * barb);
          y2 = (y + y0 * (d + slant) - x0 * barb);
        }

        vx[nv] = x1;
        vy[nv] = y1;
        nv++;
        vx[nv] = x2;
        vy[nv] = y2;
        nv++;
// System.out.println("barb5 " + x1 + " " + y1 + "" + x2 + " " + y2);
        // g.drawLine(x1, y1, x2, y2);
      }

      //add a little more pole
      if (wsp25 >= 5.0f && wsp25 < 10.0f) {
        d = d + 0.125f * scale;
        x1=(x + x0 * d);
        y1=(y + y0 * d);
/* WLH 24 April 99
        vx[nv] = x;
        vy[nv] = y;
        nv++;
        vx[nv] = x1;
        vy[nv] = y1;
        nv++;
*/
// System.out.println("wsp25 " + x + " " + y + "" + x1 + " " + y1);
        // g.drawLine(x, y, x1, y1);
      }

      //now plot any 10 kt wind barbs
      barb = 0.4f * scale;
      slant = 0.15f * scale;
      for (int j=0; j<nbarb10; j++) {
        d = d + 0.125f * scale;
        x1=(x + x0 * d);
        y1=(y + y0 * d);
        if (south) {
          x2 = (x + x0 * (d + slant) - y0 * barb);
          y2 = (y + y0 * (d + slant) + x0 * barb);
        }
        else {
          x2 = (x + x0 * (d + slant) + y0 * barb);
          y2 = (y + y0 * (d + slant) - x0 * barb);
        }

        vx[nv] = x1;
        vy[nv] = y1;
        nv++;
        vx[nv] = x2;
        vy[nv] = y2;
        nv++;
// System.out.println("barb10 " + j + " " + x1 + " " + y1 + "" + x2 + " " + y2);
        // g.drawLine(x1,y1,x2,y2);
      }
/* WLH 24 April 99
      vx[nv] = x;
      vy[nv] = y;
      nv++;
      vx[nv] = x1;
      vy[nv] = y1;
      nv++;
*/
// System.out.println("line " + x + " " + y + "" + x1 + " " + y1);
      // g.drawLine(x,y,x1,y1);

      //lengthen the pole to accomodate the 50 knot barbs
      if (nbarb50 > 0) {
        d = d +0.125f * scale;
        x1 = (x + x0 * d);
        y1 = (y + y0 * d);
/* WLH 24 April 99
        vx[nv] = x;
        vy[nv] = y;
        nv++;
        vx[nv] = x1;
        vy[nv] = y1;
        nv++;
*/
// System.out.println("line50 " + x + " " + y + "" + x1 + " " + y1);
        // g.drawLine(x,y,x1,y1);
      }

      //plot the 50 kt wind barbs
/* WLH 5 Nov 99
      s195 = (float) Math.sin(195 * Data.DEGREES_TO_RADIANS);
      c195 = (float) Math.cos(195 * Data.DEGREES_TO_RADIANS);
*/
      for (int j=0; j<nbarb50; j++) {
        x1 = (x + x0 * d);
        y1 = (y + y0 * d);
        d = d + 0.3f * scale;
        x3 = (x + x0 * d);
        y3 = (y + y0 * d);
/* WLH 5 Nov 99
        if (south) {
          x2 = (x3+barb*(x0*s195+y0*c195));
          y2 = (y3-barb*(x0*c195-y0*s195));
        }
        else {
          x2 = (x3-barb*(x0*s195+y0*c195));
          y2 = (y3+barb*(x0*c195-y0*s195));
        }
*/
        if (south) {
          x2 = (x + x0 * (d + slant) - y0 * barb);
          y2 = (y + y0 * (d + slant) + x0 * barb);
        }
        else {
          x2 = (x + x0 * (d + slant) + y0 * barb);
          y2 = (y + y0 * (d + slant) - x0 * barb);
        }

        float[] xp = {x1,x2,x3};
        float[] yp = {y1,y2,y3};

        tx[nt] = x1;
        ty[nt] = y1;
        nt++;
        tx[nt] = x2;
        ty[nt] = y2;
        nt++;
        tx[nt] = x3;
        ty[nt] = y3;
        nt++;
/*
System.out.println("barb50 " + x1 + " " + y1 + "" + x2 + " " + y2 +
                 "  " + x3 + " " + y3);
*/
        // g.fillPolygon(xp,yp,3);
        //start location for the next barb
        x1=x3;
        y1=y3;
      }

      // WLH 24 April 99 - now plot the pole
      vx[nv] = x;
      vy[nv] = y;
      nv++;
      vx[nv] = x1;
      vy[nv] = y1;
      nv++;

      mbarb[2] = x1;
      mbarb[3] = y1;
    }
    else { // if (wnd_spd < 2.5)

      // wind < 2.5 kts.  Plot a circle
      float rad = (0.7f * pt_size);

      // draw 8 segment circle, center = (x, y), radius = rad
      // 1st segment
      vx[nv] = x - rad;
      vy[nv] = y;
      nv++;
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      nv++;
      // 2nd segment
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      nv++;
      vx[nv] = x;
      vy[nv] = y + rad;
      nv++;
      // 3rd segment
      vx[nv] = x;
      vy[nv] = y + rad;
      nv++;
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      nv++;
      // 4th segment
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y + 0.7f * rad;
      nv++;
      vx[nv] = x + rad;
      vy[nv] = y;
      nv++;
      // 5th segment
      vx[nv] = x + rad;
      vy[nv] = y;
      nv++;
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      nv++;
      // 6th segment
      vx[nv] = x + 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      nv++;
      vx[nv] = x;
      vy[nv] = y - rad;
      nv++;
      // 7th segment
      vx[nv] = x;
      vy[nv] = y - rad;
      nv++;
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      nv++;
      // 8th segment
      vx[nv] = x - 0.7f * rad;
      vy[nv] = y - 0.7f * rad;
      nv++;
      vx[nv] = x - rad;
      vy[nv] = y;
      nv++;
// System.out.println("circle " + x + " " + y + "" + rad);
      // g.drawOval(x-rad,y-rad,2*rad,2*rad);

      mbarb[2] = x;
      mbarb[3] = y;
    }

    numv[0] = nv;
    numt[0] = nt;
    return mbarb;
  }

}

