//
// ArrowManipulationRendererJ3D.java
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
   ArrowManipulationRendererJ3D is the VisAD class for direct
   manipulation rendering of wind barbs under Java3D
*/
// public class ArrowManipulationRendererJ3D extends DirectManipulationRendererJ3D {
public class ArrowManipulationRendererJ3D extends BarbManipulationRendererJ3D {

  /** this DataRenderer supports direct manipulation for Tuple
      representations of wind barbs; two of the Tuple's Real components
      must be mapped to Flow1X and Flow1Y, or Flow2X and Flow2Y */
  public ArrowManipulationRendererJ3D () {
    super();
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

  /** draw swell, f0 and f1 in meters */
  float[] makeVector(boolean south, float x, float y, float z,
                          float scale, float pt_size, float f0, float f1,
                          float[] vx, float[] vy, float[] vz, int[] numv,
                          float[] tx, float[] ty, float[] tz, int[] numt) {

    float d, xd, yd;
    float x0, y0, x1, y1, x2, y2, x3, y3, x4, y4, x5, y5;
    float sscale = 0.75f * scale;

    float[] mbarb = new float[4];
    mbarb[0] = x;
    mbarb[1] = y;

    float swell_height = (float) Math.sqrt(f0 * f0 + f1 * f1);
    if (swell_height < EPS) swell_height = EPS;

    int lenv = vx.length;
    int nv = numv[0];

    // normalize direction
    x0 = -f0 / swell_height;
    y0 = -f1 / swell_height;

    float start_arrow = 0.9f * sscale;
    float end_arrow = 1.9f * sscale;
    float arrow_head = 0.3f * sscale;
    x1 = (x + x0 * start_arrow);
    y1 = (y + y0 * start_arrow);
    x2 = (x + x0 * end_arrow);
    y2 = (y + y0 * end_arrow);

    float real_end = swell_height * sscale;
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


  static final int N = 5;

  /** test ArrowManipulationRendererJ3D */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct RealTypes for swell record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType red = new RealType("red");
    RealType green = new RealType("green");
    RealType swell_degree = new RealType("swell_degree",
                          CommonUnit.degree, null);
    RealType swell_height = new RealType("swell_height",
                          CommonUnit.meter, null);

    // construct Java3D display and mappings that govern
    // how swell records are displayed
    DisplayImpl display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    ScalarMap swella_map = new ScalarMap(swell_degree, Display.Flow1Azimuth);
    display.addMap(swella_map);
    swella_map.setRange(0.0, 360.0); // do this for swell rendering
    ScalarMap swellh_map = new ScalarMap(swell_height, Display.Flow1Radial);
    display.addMap(swellh_map);
    swellh_map.setRange(0.0, 1.0); // do this for swell rendering
    FlowControl flow_control = (FlowControl) swellh_map.getControl();
    flow_control.setFlowScale(0.15f); // this controls size of barbs
    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    DataReferenceImpl[] refs = new DataReferenceImpl[N * N];
    int k = 0;
    // create an array of N by N swells
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        double u = 2.0 * i / (N - 1.0) - 1.0;
        double v = 2.0 * j / (N - 1.0) - 1.0;

        if (u == 0.0 && v == 0.0) {
          u = 0.00001f;
          v = 0.00001f;
        }

        double fx = 3.0 * u;
        double fy = 3.0 * v;
        double fa = Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy);
        double fh = Math.sqrt(fx * fx + fy * fy);

        // each swell record is a RealTuple (lon, lat,
        //   swell_degree, swell_height, red, green)
        // set colors by swell components, just for grins
        RealTuple tuple = new RealTuple(new Real[]
          {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
           new Real(swell_degree, fa), new Real(swell_height, fh),
           new Real(red, u), new Real(green, v)});

        // construct reference for swell record
        refs[k] = new DataReferenceImpl("ref_" + k);
        refs[k].setData(tuple);

        // link swell record to display via ArrowManipulationRendererJ3D
        // so user can change barb by dragging it
        // drag with right mouse button and shift to change direction
        // drag with right mouse button and no shift to change speed
        display.addReferences(new ArrowManipulationRendererJ3D(), refs[k]);

        // link swell record to a CellImpl that will listen for changes
        // and print them
        ArrowGetterJ3D cell = new ArrowGetterJ3D(flow_control, refs[k]);
        cell.addReference(refs[k]);

        k++;
      }
    }

    // instead of linking the wind record "DataReferenceImpl refs" to
    // the ArrowGetterJ3Ds, you can have some user interface event (e.g.,
    // the user clicks on "DONE") trigger code that does a getData() on
    // all the refs and stores the records in a file.

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test ArrowManipulationRendererJ3D");
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

class ArrowGetterJ3D extends CellImpl {
  DataReferenceImpl ref;

  float scale = 0.15f;
  int count = 20;
  FlowControl flow_control;

  public ArrowGetterJ3D(FlowControl f, DataReferenceImpl r) {
    ref = r;
    flow_control = f;
  }

  public void doAction() throws VisADException, RemoteException {
    RealTuple tuple = (RealTuple) ref.getData();
    float lon = (float) ((Real) tuple.getComponent(0)).getValue();
    float lat = (float) ((Real) tuple.getComponent(1)).getValue();
    float dir = (float) ((Real) tuple.getComponent(2)).getValue();
    float height = (float) ((Real) tuple.getComponent(3)).getValue();
    System.out.println("swell = (" + dir + ", " + height + ") at (" +
                       + lat + ", " + lon +")");
  }

}

