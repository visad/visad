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
import visad.util.*;
import visad.java3d.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;


/**
   CollectiveBarbManipulation is the VisAD class for
   manipulation of collections of wind barbs
*/
public class CollectiveBarbManipulation extends Object
  implements ControlListener {

  private int nindex = 0;
  private int[] ntimes;
  private int max_ntimes = 0;
  private Tuple[][] tuples;
  private FlatField[] wind_stations;
  private Set[] time_sets;
  private int which_time = -1;
  private int[] which_times;

  private DataReferenceImpl stations_ref;
  private DataReferenceImpl[] station_refs;
  private BarbRendererJ3D barb_renderer;
  private BarbManipulationRendererJ3D[] barb_manipulation_renderers;

  private AnimationControl control = null;

  /**
     wind_field should have MathType:
       (station_index -> (Time -> tuple))
     where tuple is flat and includes RealTypes mapped to:
       Flow1Azimuth
       Flow1Radial
       Latitude
       Longitude
  */
  public CollectiveBarbManipulation(FieldImpl wind_field,
                                     DisplayImpl display)
         throws VisADException, RemoteException {
    control = (AnimationControl) display.getControl(AnimationControl.class);
    if (control == null) {
      throw new CollectiveBarbException("display must include " +
                     "ScalarMap to Animation");
    }
    // use a ControlListener on Display.Animation to fake
    // animation of manipulable barbs
    control.addControlListener(this);

    FunctionType wind_field_type = (FunctionType) wind_field.getType();
    TupleType wind_type = null;
    RealType station_index = null;

    try {
      station_index =
        (RealType) wind_field_type.getDomain().getComponent(0);
      if (RealType.Time.equals(station_index)) {
        throw new CollectiveBarbException("wind_field bad MathType: " +
                     station_index + " cannot be RealType.Time");
      }
      FunctionType wind_station_type =
        (FunctionType) wind_field_type.getRange();
      RealType time =
        (RealType) wind_station_type.getDomain().getComponent(0);
      if (!RealType.Time.equals(time)) {
        throw new CollectiveBarbException("wind_field bad MathType: " +
                     time + " must be RealType.Time");
      }
      wind_type = (TupleType) wind_station_type.getRange();
      if (!wind_type.getFlat()) {
        throw new CollectiveBarbException("wind_field bad MathType: " +
                      wind_type + " must be flat");
      }

      nindex = wind_field.getLength();
      ntimes = new int[nindex];
      max_ntimes = 0;
      tuples = new Tuple[nindex][];
      which_times = new int[nindex];
      wind_stations = new FlatField[nindex];
      time_sets = new Set[nindex];
      for (int i=0; i<nindex; i++) {
        wind_stations[i] = (FlatField) wind_field.getSample(i);
        ntimes[i] = wind_stations[i].getLength();
        time_sets[i] = wind_stations[i].getDomainSet();
        if (ntimes[i] > max_ntimes) max_ntimes = ntimes[i];
        tuples[i] = new Tuple[ntimes[i]];
        for (int j=0; j<ntimes[i]; j++) {
          tuples[i][j] = (Tuple) wind_stations[i].getSample(j);
        }
      }
    }
    catch (ClassCastException e) {
      throw new CollectiveBarbException("wind_field bad MathType: " +
                     wind_field_type);
    }

    int azimuth_index = -1;
    int radial_index = -1;
    int lat_index = -1;
    int lon_index = -1;
    Vector scalar_map_vector = display.getMapVector();
    int tuple_dim = wind_type.getDimension();
    RealType[] reals = wind_type.getRealComponents();
    for (int i=0; i<tuple_dim; i++) {
      RealType real = reals[i];
      if (RealType.Latitude.equals(real)) {
        lat_index = i;
      }
      else if (RealType.Longitude.equals(real)) {
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
      throw new CollectiveBarbException("wind data must include Latitude " +
               "and Longitude and two RealTypes mapped to Flow1Azimuth " +
               "and Flow1Radial " + lat_index + " " + lon_index +
               " " + azimuth_index + " " + radial_index);
    }

    stations_ref = new DataReferenceImpl("stations_ref");
    stations_ref.setData(wind_field);
    barb_renderer = new BarbRendererJ3D();
    display.addReferences(barb_renderer, stations_ref);
    barb_renderer.toggle(false);
    which_time = -1;
    station_refs = new DataReferenceImpl[nindex];
    barb_manipulation_renderers = new BarbManipulationRendererJ3D[nindex];
    for (int i=0; i<nindex; i++) {
      station_refs[i] = new DataReferenceImpl("station_ref" + i);
      station_refs[i].setData(null);
      which_times[i] = -1;
      barb_manipulation_renderers[i] = new BarbManipulationRendererJ3D();
      display.addReferences(barb_manipulation_renderers[i], station_refs[i]);
    }


    control.setCurrent(0);
  }

  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
    which_time = -1;
    if (barb_manipulation_renderers == null) return;
    for (int i=0; i<nindex; i++) {
      which_times[i] = -1;
      if (barb_manipulation_renderers[i] == null) return;
      barb_manipulation_renderers[i].stop_direct();
    }

    Set time_set = control.getSet();
    if (time_set == null) return;
    int current = control.getCurrent();
    if (current < 0) return;
    which_time = current;
    int[] indices = {current};
    double[][] fvalues = time_set.indexToDouble(indices);
    double value = fvalues[0][0];
    if (value != value) return;
    RealTupleType in = ((SetType) time_set.getType()).getDomain();

    for (int i=0; i<nindex; i++) {
      RealTupleType out = ((SetType) time_sets[i].getType()).getDomain();
      double[][] values = CoordinateSystem.transformCoordinates(
                               out, time_sets[i].getCoordinateSystem(),
                               time_sets[i].getSetUnits(), null /* errors */,
                               in, time_set.getCoordinateSystem(),
                               time_set.getSetUnits(),
                               null /* errors */, fvalues);
      if (time_sets[i].getLength() == 1) {
        indices = new int[] {0};
      }
      else {
        indices = time_sets[i].doubleToIndex(values);
      }
      int index = indices[0];
      if (index < tuples[i].length) {
        station_refs[i].setData(tuples[i][index]);
        which_times[i] = index;
      }
// System.out.println("station " + i + " ref " + index);

    } // end for (int i=0; i<nindex; i++)
  }

  private static final int N = 3;
  private static final int NTIMES = 10;

  public static void main(String args[])
         throws VisADException, RemoteException {

    boolean polar = true;

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
    if (polar) {
      System.out.println("polar winds");
      windds =
        new RealTupleType(new RealType[] {wind_dir, wind_speed},
        new WindPolarCoordinateSystem(windxy), null);
    }

    RealType stn = new RealType("station");
    Set stn_set = new Integer1DSet(stn, N * N);
    RealType time = RealType.Time;
    double start = new DateTime(1999, 122, 57060).getValue();
    Set time_set = new Linear1DSet(time, start, start + 3000.0, NTIMES);

    TupleType tuple_type = null;
    if (polar) {
      tuple_type = new TupleType(new MathType[]
            {lon, lat, windds, red, green});
    }
    else {
      tuple_type = new TupleType(new MathType[]
            {lon, lat, windxy, red, green});
    }

    FunctionType station_type = new FunctionType(time, tuple_type);
    FunctionType stations_type = new FunctionType(stn, station_type);

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
    if (polar) {
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

    ScalarMap amap = new ScalarMap(time, Display.Animation);
    display.addMap(amap);
    AnimationControl acontrol = (AnimationControl) amap.getControl();
    acontrol.setStep(2000);

    // create an array of N by N winds
    FieldImpl field = new FieldImpl(stations_type, stn_set);
    int m = 0;
    for (int i=0; i<N; i++) {
      for (int j=0; j<N; j++) {
        FlatField ff = new FlatField(station_type, time_set);
        double[][] values = new double[6][NTIMES];
        for (int k=0; k<NTIMES; k++) {
          double u = 2.0 * i / (N - 1.0) - 1.0;
          double v = 2.0 * j / (N - 1.0) - 1.0;
  
          // each wind record is a Tuple (lon, lat, (windx, windy), red, green)
          // set colors by wind components, just for grins
          values[0][k] = 10. * u;
          values[1][k] = 10.0 * v - 40.0;
          double fx = 30.0 * u;
          double fy = 30.0 * v;
          if (polar) {
            double fd = Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy);
            double fs = Math.sqrt(fx * fx + fy * fy);
            values[2][k] = fd;
            values[3][k] = fs;
          }
          else {
            values[2][k] = fx;
            values[3][k] = fy;
          }
          values[4][k] = u;
          values[5][k] = v;
        }
        ff.setSamples(values);
        field.setSample(m, ff);
        m++;
      }
    }

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
    panel.add(new AnimationWidget(amap));

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);
    CollectiveBarbManipulation cbm =
      new CollectiveBarbManipulation(field, display);
  }
}

class Hey implements ControlListener {
  AnimationControl control;
  DisplayImpl display;
  DataReferenceImpl[] refs;
  DataReferenceImpl ref;
  int oldk = -1;

  Hey(AnimationControl c, DisplayImpl d, DataReferenceImpl[] rs,
      DataReferenceImpl r) {
    control = c;
    display = d;
    refs = rs;
    ref = r;
  }

  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
    int num = control.getCurrent();
    ref.setData(refs[num].getData());
/*
    if (oldk >= 0) display.removeReference(refs[oldk]);
    display.removeReference(refs[num]);
    display.addReferences(new BarbManipulationRendererJ3D(), refs[num]);
    oldk = num;
*/
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

