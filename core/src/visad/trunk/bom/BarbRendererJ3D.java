//
// BarbRendererJ3D.java
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
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


/**
   BarbRendererJ3D is the VisAD class for rendering of
   wind barbs under Java3D - otherwise it behaves just
   like DefaultRendererJ3D
*/
public class BarbRendererJ3D extends DefaultRendererJ3D
       implements BarbRenderer {

  private BarbManipulationRendererJ3D bmr;

  /** this DataRenderer supports direct manipulation for RealTuple
      representations of wind barbs; four of the RealTuple's Real
      components must be mapped to XAxis, YAxis, Flow1X and Flow1Y */
  public BarbRendererJ3D () {
    super();
    bmr = new BarbManipulationRendererJ3D();
  }

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbFunctionTypeJ3D(type, link, parent);
  }

  public ShadowType makeShadowRealTupleType(
         RealTupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbRealTupleTypeJ3D(type, link, parent);
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

  public ShadowType makeShadowTupleType(
         TupleType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowBarbTupleTypeJ3D(type, link, parent);
  }

  public void setKnotsConvert(boolean enable) {
    bmr.setKnotsConvert(enable);
  } 
  
  public boolean getKnotsConvert() {
    return bmr.getKnotsConvert();
  } 

  public float[] makeVector(boolean south, float x, float y, float z,
                          float scale, float pt_size, float f0, float f1,
                          float[] vx, float[] vy, float[] vz, int[] numv,
                          float[] tx, float[] ty, float[] tz, int[] numt) { 
    return bmr.makeVector(south, x, y, z, scale, pt_size, f0, f1, vx, vy, vz,
                          numv, tx, ty, tz, numt);
  }

  public Object clone() {
    return new BarbRendererJ3D();
  }

  static final int N = 5;

  /** run 'java visad.bom.BarbRendererJ3D middle_latitude'
          to test with Cartesian winds
      run 'java visad.bom.BarbRendererJ3D middle_latitude x'
          to test with polar winds
      adjust middle_latitude for south or north barbs */
  public static void main(String args[])
         throws VisADException, RemoteException {
    double mid_lat = -10.0;
    if (args.length > 0) {
      try {
        mid_lat = Double.valueOf(args[0]).doubleValue();
      }
      catch(NumberFormatException e) { }
    }
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType flowx = RealType.getRealType("flowx",
                          CommonUnit.meterPerSecond);
    RealType flowy = RealType.getRealType("flowy",
                          CommonUnit.meterPerSecond);
    RealType red = RealType.getRealType("red");
    RealType green = RealType.getRealType("green");
    RealType index = RealType.getRealType("index");
    EarthVectorType flowxy = new EarthVectorType(flowx, flowy);
    TupleType range = null;
    RealType flow_degree = RealType.getRealType("flow_degree",
                          CommonUnit.degree);
    RealType flow_speed = RealType.getRealType("flow_speed",
                          CommonUnit.meterPerSecond);
    if (args.length > 1) {
      System.out.println("polar winds");
      RealTupleType flowds =
        new RealTupleType(new RealType[] {flow_degree, flow_speed},
        new WindPolarCoordinateSystem(flowxy), null);
      range = new TupleType(new MathType[] {lon, lat, flowds, red, green});
    }
    else {
      System.out.println("Cartesian winds");
      range = new TupleType(new MathType[] {lon, lat, flowxy, red, green});
    }
    FunctionType flow_field = new FunctionType(index, range);

    DisplayImpl display = new DisplayImplJ3D("display1");
    ScalarMap xmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(xmap);
    ScalarMap ymap = new ScalarMap(lat, Display.YAxis);
    display.addMap(ymap);
    if (args.length > 1) {
      ScalarMap flowd_map = new ScalarMap(flow_degree, Display.Flow1Azimuth);
      display.addMap(flowd_map);
      flowd_map.setRange(0.0, 360.0);
      ScalarMap flows_map = new ScalarMap(flow_speed, Display.Flow1Radial);
      display.addMap(flows_map);
      flows_map.setRange(0.0, 1.0);
      FlowControl flow_control = (FlowControl) flows_map.getControl();
      flow_control.setFlowScale(0.1f);
    }
    else {
      ScalarMap flowx_map = new ScalarMap(flowx, Display.Flow1X);
      display.addMap(flowx_map);
      flowx_map.setRange(-1.0, 1.0);
      ScalarMap flowy_map = new ScalarMap(flowy, Display.Flow1Y);
      display.addMap(flowy_map);
      flowy_map.setRange(-1.0, 1.0);
      FlowControl flow_control = (FlowControl) flowy_map.getControl();
      flow_control.setFlowScale(0.1f);
    }
    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    Integer1DSet set = new Integer1DSet(N * N);
    double[][] values = new double[6][N * N];
    int m = 0;
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        double u = 2.0 * i / (N - 1.0) - 1.0;
        double v = 2.0 * j / (N - 1.0) - 1.0;
        values[0][m] = 10.0 * u;
        values[1][m] = 10.0 * v + mid_lat;
        double fx = 30.0 * u;
        double fy = 30.0 * v;
        if (args.length > 1) {
          values[2][m] =
            Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy);
          values[3][m] = Math.sqrt(fx * fx + fy * fy);
        }
        else {
          values[2][m] = fx;
          values[3][m] = fy;
        }
        values[4][m] = u;
        values[5][m] = v;
        m++;
      }
    }
    FlatField field = new FlatField(flow_field, set);
    field.setSamples(values);
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(field);
    BarbRendererJ3D renderer = new BarbRendererJ3D();
    renderer.setKnotsConvert(true);
    display.addReferences(renderer, ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test BarbRendererJ3D");
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

