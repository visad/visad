//
// CollectiveBarbManipulation.java
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
   CollectiveBarbManipulation is the VisAD class for
   manipulation of collections of wind barbs
*/
public class CollectiveBarbManipulation extends Object
  implements ControlListener {

  /**
     wind_field should have MathType:
       (station_index -> (Time -> real_tuple))
     where RealTuple includes RealTypes mapped to:
       Flow1Azimuth
       Flow1Radial
       Latitude
       Longitude
  */
  public CollectiveBarbManipulation (FieldImpl wind_field,
                                     DisplayImpl display)
         throws VisADException, RemoteException {
    AnimationControl control =
      (AnimationControl) display.getControl(AnimationControl.class);
    if (control != null) {
      // use a ControlListener on Display.Animation to fake
      // animation of manipulable barbs
      control.addControlListener(this);
    }
    else {
      // or just fake it using
      // display.removeReference(refs[oldk]) and
      // display.addReference(new BarbManipulationRendererJ*D(), refs[k]);


    }

    FunctionType wind_field_type = (FunctionType) wind_field.getType();
    RealTupleType wind_type = null;
    try {
      RealType station_index =
        (RealType) wind_field_type.getDomain().getComponent(0);
      if (RealType.Time.equals(station_index)) {
        throw new VisADException("wind_field bad MathType: " + station_index +
                                 " cannot be RealType.Time");
      }
      FunctionType wind_station_type =
        (FunctionType) wind_field_type.getRange();
      RealType time =
        (RealType) wind_station_type.getDomain().getComponent(0);
      if (!RealType.Time.equals(time)) {
        throw new VisADException("wind_field bad MathType: " + time +
                                 " must be RealType.Time");
      }
      wind_type = (RealTupleType) wind_station_type.getRange();
    }
    catch (ClassCastException e) {
      throw new VisADException("wind_field bad MathType: " + wind_field_type);
    }

    int azimuth_index = -1;
    int radial_index = -1;
    int lat_index = -1;
    int lon_index = -1;
    Vector scalar_map_vector = display.getMapVector();
    int tuple_dim = wind_type.getDimension();
    for (int i=0; i<tuple_dim; i++) {
      RealType real = (RealType) wind_type.getComponent(i);
      if (RealType.Latitude.equals(real)) {
        lat_index = i;
      }
      else if (RealType.Latitude.equals(real)) {
        lon_index = i;
      }
      else {
        Enumeration enum = scalar_map_vector.elements();
        while (enum.hasMoreElements()) {
          ScalarMap map = (ScalarMap) enum.nextElement();
          if (real.equals(map.getScalar())) {
            DisplayRealType dreal = map.getDisplayScalar();
            if (Display.Flow1Azimuth.equals(dreal)) {
              azimuth_index = i;
            }
            else if (Display.Flow1Radial.equals(dreal)) {
              radial_index = i;
            }
          }
        }
      }
    } // for (int i=0; i<n; i++) {
    if (lat_index < 0 || lon_index < 0 ||
        azimuth_index < 0 || radial_index < 0) {
      throw new VisADException("wind data must include Latitude and " +
               "Longitude and two RealTypes mapped to Flow1Azimuth " +
               "and Flow1Radial");
    }




  }

  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
  }

  private static final int N = 5;

  public static void main(String args[])
         throws VisADException, RemoteException {


    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType windx = new RealType("windx",
                          CommonUnit.meterPerSecond, null);     
    RealType windy = new RealType("windy",
                          CommonUnit.meterPerSecond, null);     
    RealType red = new RealType("red");
    RealType green = new RealType("green");

    // EarthVectorType extends RealTupleType and says that its
    // components are vectors in m/s with components parallel
    // to Longitude (positive east) and Latitude (positive north)
    EarthVectorType windxy = new EarthVectorType(windx, windy);

    RealType wind_dir = new RealType("wind_dir",
                          CommonUnit.degree, null);
    RealType wind_speed = new RealType("wind_speed",
                          CommonUnit.meterPerSecond, null);
    RealTupleType windds = null;
    if (args.length > 0) {
      System.out.println("polar winds");
      windds =
        new RealTupleType(new RealType[] {wind_dir, wind_speed},
        new WindPolarCoordinateSystem(windxy), null);
    }

    // construct Java3D display and mappings that govern
    // how wind records are displayed
    DisplayImpl display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    lonmap.setRange(-10.0, 10.0);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(-50.0, -30.0);

    FlowControl flow_control;
    if (args.length > 0) {
      ScalarMap winds_map = new ScalarMap(wind_speed, Display.Flow1Radial);
      display.addMap(winds_map);
      winds_map.setRange(0.0, 1.0); // do this for barb rendering
      ScalarMap windd_map = new ScalarMap(wind_dir, Display.Flow1Azimuth);
      display.addMap(windd_map);
      windd_map.setRange(0.0, 360.0); // do this for barb rendering
      flow_control = (FlowControl) windd_map.getControl();
      flow_control.setFlowScale(0.15f); // this controls size of barbs
    }
    else {
      ScalarMap windx_map = new ScalarMap(windx, Display.Flow1X);
      display.addMap(windx_map);
      windx_map.setRange(-1.0, 1.0); // do this for barb rendering
      ScalarMap windy_map = new ScalarMap(windy, Display.Flow1Y);
      display.addMap(windy_map);
      windy_map.setRange(-1.0, 1.0); // do this for barb rendering
      flow_control = (FlowControl) windy_map.getControl();
      flow_control.setFlowScale(0.15f); // this controls size of barbs
    }

    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    DataReferenceImpl[] refs = new DataReferenceImpl[N * N];
    int k = 0;
    // create an array of N by N winds
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        double u = 2.0 * i / (N - 1.0) - 1.0;
        double v = 2.0 * j / (N - 1.0) - 1.0;

        // each wind record is a Tuple (lon, lat, (windx, windy), red, green)
        // set colors by wind components, just for grins
        Tuple tuple;
        double fx = 30.0 * u;
        double fy = 30.0 * v;
        if (args.length > 0) {
          double fd = Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy);
          double fs = Math.sqrt(fx * fx + fy * fy);
          tuple = new Tuple(new Data[]
            {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
             new RealTuple(windds, new double[] {fd, fs}),
             new Real(red, u), new Real(green, v)});
        }
        else {
          tuple = new Tuple(new Data[]
            {new Real(lon, 10.0 * u), new Real(lat, 10.0 * v - 40.0),
             new RealTuple(windxy, new double[] {fx, fy}),
             new Real(red, u), new Real(green, v)});
        }

        // construct reference for wind record
        refs[k] = new DataReferenceImpl("ref_" + k);
        refs[k].setData(tuple);

        // link wind record to display via CollectiveBarbManipulation
        // so user can change barb by dragging it
        // drag with right mouse button and shift to change direction
        // drag with right mouse button and no shift to change speed
        // WLH
        // display.addReferences(new BarbManipulationRendererJ3D(), refs[k]);


        // link wind record to a CellImpl that will listen for changes
        // and print them
        WindGetter cell = new WindGetter(flow_control, refs[k]);
        cell.addReference(refs[k]);

        k++;
      }
    }

    // instead of linking the wind record "DataReferenceImpl refs" to
    // the WindGetters, you can have some user interface event (e.g.,
    // the user clicks on "DONE") trigger code that does a getData() on
    // all the refs and stores the records in a file.

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test CollectiveBarbManipulation");
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

    int oldk = 0;
    while (true) {
      for (k=0; k<N*N; k++) {
        // **** this actually works ****
        display.removeReference(refs[oldk]);
        display.addReferences(new BarbManipulationRendererJ3D(), refs[k]);
        oldk = k;
        new visad.util.Delay(2000);
      }
    }

  }
}

class WindGetter extends CellImpl {
  DataReferenceImpl ref;

  float scale = 0.15f;
  int count = 20;
  FlowControl flow_control;

  public WindGetter(FlowControl f, DataReferenceImpl r) {
    ref = r;
    flow_control = f;
  }

  public void doAction() throws VisADException, RemoteException {
    Tuple tuple = (Tuple) ref.getData();
    float lon = (float) ((Real) tuple.getComponent(0)).getValue();
    float lat = (float) ((Real) tuple.getComponent(1)).getValue();
    RealTuple wind = (RealTuple) tuple.getComponent(2);
    float windx = (float) ((Real) wind.getComponent(0)).getValue();
    float windy = (float) ((Real) wind.getComponent(1)).getValue();
    System.out.println("wind = (" + windx + ", " + windy + ") at (" +
                       + lat + ", " + lon +")");
/* a testing hack
    count--;
    if (count < 0) {
      count = 20;
      scale = 0.15f * 0.3f / scale;
      flow_control.setFlowScale(scale);
    }
*/
  }

}

