//
// FlowTest.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import visad.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


public class FlowTest extends Object {

  public FlowTest () {
    super();
  }

  static final int N = 6;

  /** run 'java FlowTest middle_latitude'
          to test with (lat, lon)
      run 'java FlowTest middle_latitude x'
          to test with (lon, lat)
      adjust middle_latitude for south or north */
  public static void main(String args[])
         throws VisADException, RemoteException {
    double mid_lat = -10.0;
    if (args.length > 0) {
      try {
        mid_lat = Double.valueOf(args[0]).doubleValue();
      }
      catch(NumberFormatException e) { }
    }
    boolean swap = (args.length > 1);
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType[] types;
    if (swap) {
      types = new RealType[] {lon, lat};
    }
    else {
      types = new RealType[] {lat, lon};
    }
    RealTupleType earth_location = new RealTupleType(types);
    System.out.println("earth_location = " + earth_location +
                       " mid_lat = " + mid_lat);

    RealType flowx = RealType.getRealType("flowx",
                          CommonUnit.meterPerSecond);
    RealType flowy = RealType.getRealType("flowy",
                          CommonUnit.meterPerSecond);
    RealType red = RealType.getRealType("red");
    RealType green = RealType.getRealType("green");
    EarthVectorType flowxy = new EarthVectorType(flowx, flowy);
    TupleType range = null;

    range = new TupleType(new MathType[] {flowxy, red, green});
    FunctionType flow_field = new FunctionType(earth_location, range);

    DisplayImpl display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap xmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(xmap);
    ScalarMap ymap = new ScalarMap(lat, Display.YAxis);
    display.addMap(ymap);
    ScalarMap flowx_map = new ScalarMap(flowx, Display.Flow1X);
    display.addMap(flowx_map);
    flowx_map.setRange(-10.0, 10.0);
    ScalarMap flowy_map = new ScalarMap(flowy, Display.Flow1Y);
    display.addMap(flowy_map);
    flowy_map.setRange(-10.0, 10.0);
    FlowControl flow_control = (FlowControl) flowy_map.getControl();
    flow_control.setFlowScale(0.05f);
    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    double lonlow = -10.0;
    double lonhi = 10.0;
    double latlow = mid_lat - 10.0;
    double lathi = mid_lat + 10.0;
    Linear2DSet set;
    if (swap) {
      set = new Linear2DSet(earth_location, lonlow, lonhi, N,
                                            latlow, lathi, N);
    }
    else {
      set = new Linear2DSet(earth_location, latlow, lathi, N,
                                            lonlow, lonhi, N);
    }
    double[][] values = new double[4][N * N];
    int m = 0;
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        int k = i;
        int l = j;
        if (swap) {
          k = j;
          l = i;
        }
        double u = (N - 1.0) / 2.0 - l;
        double v = k - (N - 1.0) / 2.0;
        // double u = 2.0 * k / (N - 1.0) - 1.0;
        // double v = 2.0 * l / (N - 1.0);
        double fx = 6.0 * u;
        double fy = 6.0 * v;
        values[0][m] = fx;
        values[1][m] = fy;
        values[2][m] = u;
        values[3][m] = v;
        m++;
      }
    }
    FlatField field = new FlatField(flow_field, set);
    field.setSamples(values);
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(field);
    display.addReference(ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test FlowTest");
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

