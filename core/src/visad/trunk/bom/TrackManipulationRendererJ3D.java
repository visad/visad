//
// TrackManipulationRendererJ3D.java
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

package visad.bom;

import visad.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


/**
   TrackManipulationRendererJ3D is the VisAD class for direct
   manipulation rendering of storm tracks under Java3D
*/
// public class TrackManipulationRendererJ3D extends DirectManipulationRendererJ3D {
public class TrackManipulationRendererJ3D extends BarbManipulationRendererJ3D {

  float x_size;
  float y_size;
  float angle;
  private static int NE = 32;
  float[] x_ellipse = new float[NE + 1];
  float[] y_ellipse = new float[NE + 1];

  /** this DataRenderer supports direct manipulation for Tuple
      representations of storm tracks; two of the Tuple's Real components
      must be mapped to Flow1X and Flow1Y, or Flow2X and Flow2Y */
  public TrackManipulationRendererJ3D (float xs, float ys, float ang) {
    super();
    x_size = (float) Math.abs(xs);
    y_size = (float) Math.abs(ys);
    angle = ang;
    float sa = (float) Math.sin(ang * Data.DEGREES_TO_RADIANS);
    float ca = (float) Math.cos(ang * Data.DEGREES_TO_RADIANS);
    for (int i=0; i<NE+1; i++) {
      double b = 2.0 * Math.PI * i / NE;
      float xe = (float) (x_size * Math.cos(b));
      float ye = (float) (y_size * Math.sin(b));
      x_ellipse[i] = ca * xe + sa * ye;
      y_ellipse[i] = ca * ye - sa * xe;
    }
  }

  private float step(float x, float y) {
    double dist = 2.0 * (x_size + y_size);
    float step = -1.0f;
    for (int i=0; i<NE+1; i++) {
      double d = Math.abs(x * y_ellipse[i] - y * x_ellipse[i]);
      if (d < dist) {
        dist = d;
        step = (float) Math.sqrt(x_ellipse[i] * x_ellipse[i] +
                                 y_ellipse[i] * y_ellipse[i]);
      }
    }
    return step;
  }

  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTupleTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbTupleTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbFunctionTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowRealType(
         RealType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowSetType(
         SetType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbSetTypeJ3D(type, link, parent);
  }

  private static final float EPS = 0.00001f;

  /** draw track, f0 and f1 in meters */
  public float[] makeVector(boolean south, float x, float y, float z,
                          float scale, float pt_size, float f0, float f1,
                          float[] vx, float[] vy, float[] vz, int[] numv,
                          float[] tx, float[] ty, float[] tz, int[] numt) {

    float d, xd, yd;
    float x0, y0, x1, y1, x2, y2, x3, y3, x4, y4, x5, y5;
    float sscale = 0.75f * scale;

    float[] mbarb = new float[4];
    mbarb[0] = x;
    mbarb[1] = y;

    float track_length = (float) Math.sqrt(f0 * f0 + f1 * f1);
    if (track_length < EPS) track_length = EPS;

    int lenv = vx.length;
    int nv = numv[0];

    // normalize direction
    x0 = -f0 / track_length;
    y0 = -f1 / track_length;

    float start_arrow = 0.9f * sscale;
    float end_arrow = 1.9f * sscale;
    float arrow_head = 0.3f * sscale;
    x1 = (x + x0 * start_arrow);
    y1 = (y + y0 * start_arrow);
    x2 = (x + x0 * end_arrow);
    y2 = (y + y0 * end_arrow);

    float real_end = track_length * sscale;
    x5 = (x + x0 * real_end);
    y5 = (y + y0 * real_end);

    // draw arrow shaft
    vx[nv] = x;
    vy[nv] = y;
    vz[nv] = z;
    nv++;
    vx[nv] = x5;
    vy[nv] = y5;
    vz[nv] = z;
    nv++;

    mbarb[2] = x5;
    mbarb[3] = y5;

    xd = x2 - x1;
    yd = y2 - y1;

    x3 = x5 - 0.3f * (xd - yd);
    y3 = y5 - 0.3f * (yd + xd);
    x4 = x5 - 0.3f * (xd + yd);
    y4 = y5 - 0.3f * (yd - xd);

    // draw arrow head
    vx[nv] = x5;
    vy[nv] = y5;
    vz[nv] = z;
    nv++;
    vx[nv] = x3;
    vy[nv] = y3;
    vz[nv] = z;
    nv++;

    vx[nv] = x5;
    vy[nv] = y5;
    vz[nv] = z;
    nv++;
    vx[nv] = x4;
    vy[nv] = y4;
    vz[nv] = z;
    nv++;

    numv[0] = nv;
    return mbarb;
  }


  /** test TrackManipulationRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct RealTypes for track record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType red = new RealType("red");
    RealType green = new RealType("green");
    RealType track_degree = new RealType("track_degree",
                          CommonUnit.degree, null);
    RealType track_length = new RealType("track_length",
                          CommonUnit.meter, null);

    // construct Java3D display and mappings that govern
    // how track records are displayed
    DisplayImpl display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    lonmap.setRange(-10.0, 10.0);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(-50.0, -30.0);
    ScalarMap tracka_map = new ScalarMap(track_degree, Display.Flow1Azimuth);
    display.addMap(tracka_map);
    tracka_map.setRange(0.0, 360.0); // do this for track rendering
    ScalarMap trackh_map = new ScalarMap(track_length, Display.Flow1Radial);
    display.addMap(trackh_map);
    trackh_map.setRange(0.0, 1.0); // do this for track rendering
    FlowControl flow_control = (FlowControl) trackh_map.getControl();
    flow_control.setFlowScale(0.15f); // this controls size of barbs
    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    double u = 0.0;
    double v = 0.0;


    double fx = -3.0;
    double fy = -3.0;
    double fa = Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy);
    double fh = Math.sqrt(fx * fx + fy * fy);

    // track record is a RealTuple (lon, lat,
    // track_degree, track_length, red, green)
    // set colors by track components, just for grins
    RealTuple tuple = new RealTuple(new Real[]
      {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
       new Real(track_degree, fa), new Real(track_length, fh),
       new Real(red, u), new Real(green, v)});

    // construct reference for track record
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(tuple);

    // link track record to display via TrackManipulationRendererJ3D
    // so user can change barb by dragging it
    // drag with right mouse button and shift to change direction
    // drag with right mouse button and no shift to change speed
    TrackManipulationRendererJ3D renderer =
      new TrackManipulationRendererJ3D(0.2f, 0.1f, 0.0f);
    display.addReferences(renderer, ref);

    // link track record to a CellImpl that will listen for changes
    // and print them
    TrackGetterJ3D cell = new TrackGetterJ3D(flow_control, ref);
    cell.addReference(ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test TrackManipulationRendererJ3D");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    // add display to JPanel
    panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}

class TrackGetterJ3D extends CellImpl {
  DataReferenceImpl ref;

  float scale = 0.15f;
  int count = 20;
  FlowControl flow_control;

  public TrackGetterJ3D(FlowControl f, DataReferenceImpl r) {
    ref = r;
    flow_control = f;
  }

  public void doAction() throws VisADException, RemoteException {
    RealTuple tuple = (RealTuple) ref.getData();
    float lon = (float) ((Real) tuple.getComponent(0)).getValue();
    float lat = (float) ((Real) tuple.getComponent(1)).getValue();
    float dir = (float) ((Real) tuple.getComponent(2)).getValue();
    float length = (float) ((Real) tuple.getComponent(3)).getValue();
    System.out.println("track = (" + dir + ", " + length + ") at (" +
                       + lat + ", " + lon +")");
  }

}

