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
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
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
  private Tuple[][] tuples;
  private FlatField[] wind_stations;
  private Set[] time_sets;
  private int which_time = -1;
  private int[] which_times;

  private Set time_set = null;
  private int global_ntimes = 0;
  private double[] times;
  private int[][] global_to_station; // [nindex][global_ntimes]
  private int[][] station_to_global; // [nindex][ntimes[i]]

  private float[] lats;
  private float[] lons;

  private DataReferenceImpl stations_ref;
  private DataReferenceImpl[] station_refs;
  private BarbRendererJ3D barb_renderer;
  private BarbManipulationRendererJ3D[] barb_manipulation_renderers;
  private BarbMonitor[] barb_monitors;

  private WindMonitor wind_monitor = null;

  private AnimationControl control = null;

  private DisplayImplJ3D display;
  private FieldImpl wind_field;
  private boolean absolute;
  private float inner_distance;
  private float outer_distance;
  private double inner_time;
  private double outer_time;

  private int azimuth_index;
  private int radial_index;
  private int lat_index;
  private int lon_index;

  private int last_sta = -1;
  private int last_time = -1;

  private float[][] azimuths;
  private float[][] radials;
  private float[][] old_azimuths;
  private float[][] old_radials;

  /**
     wf should have MathType:
       (station_index -> (Time -> tuple))
     where tuple is flat
       [e.g., (Latitude, Longitude, (flow_dir, flow_speed))]
     and must include RealTypes Latitude and Longitude plus
     RealTypes mapped to Flow1Azimuth and Flow1Radial in the
     DisplayImpl d;

     absolute indicates absolute or relative value adjustment
     id and od are inner and outer distances in meters
     it and ot are inner and outer times in seconds
     influence is 1.0 inside inner, 0.0 outside outer and linear between
     distance and time influences multiply;

     each time the user clicks the right mouse button to
     manipulate a wind barb, the "reference" values for all
     wind barbs are set - thus repeatedly adjusting the same
     barb will magnify its influence over its neighbors
  */
  public CollectiveBarbManipulation(FieldImpl wf,
                 DisplayImplJ3D d, boolean abs,
                 float id, float od, float it, float ot)
         throws VisADException, RemoteException {
    wind_field = wf;
    display = d;
    absolute = abs;
    inner_distance = id;
    outer_distance = od;
    inner_time = it;
    outer_time = ot;
    if (inner_time < 0.0 ||
        outer_time < inner_time) {
      throw new CollectiveBarbException("outer_time must be " +
                                   "greater than inner_time");
    }
    if (inner_distance < 0.0 ||
        outer_distance < inner_distance) {
      throw new CollectiveBarbException("outer_distance must be " +
                                   "greater than distance_time");
    }

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
    }
    catch (ClassCastException e) {
      throw new CollectiveBarbException("wind_field bad MathType: " +
                     wind_field_type);
    }

    azimuth_index = -1;
    radial_index = -1;
    lat_index = -1;
    lon_index = -1;
    Vector scalar_map_vector = display.getMapVector();
    int tuple_dim = wind_type.getDimension();
    RealType[] real_types = wind_type.getRealComponents();
    for (int i=0; i<tuple_dim; i++) {
      RealType real = real_types[i];
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

    try {
      nindex = wind_field.getLength();
      ntimes = new int[nindex];
      tuples = new Tuple[nindex][];
      which_times = new int[nindex];
      wind_stations = new FlatField[nindex];
      time_sets = new Set[nindex];
      azimuths = new float[nindex][];
      radials = new float[nindex][];
      old_azimuths = new float[nindex][];
      old_radials = new float[nindex][];
      for (int i=0; i<nindex; i++) {
        wind_stations[i] = (FlatField) wind_field.getSample(i);
        ntimes[i] = wind_stations[i].getLength();
        time_sets[i] = wind_stations[i].getDomainSet();
        tuples[i] = new Tuple[ntimes[i]];
        azimuths[i] = new float[ntimes[i]];
        radials[i] = new float[ntimes[i]];
        old_azimuths[i] = new float[ntimes[i]];
        old_radials[i] = new float[ntimes[i]];
        for (int j=0; j<ntimes[i]; j++) {
          tuples[i][j] = (Tuple) wind_stations[i].getSample(j);
          Real[] reals = tuples[i][j].getRealComponents();
          azimuths[i][j] = (float) reals[azimuth_index].getValue();
          radials[i][j] = (float) reals[radial_index].getValue();
          old_azimuths[i][j] = azimuths[i][j];
          old_radials[i][j] = radials[i][j];
        }
      }
    }
    catch (ClassCastException e) {
      throw new CollectiveBarbException("wind_field bad MathType: " +
                     wind_field_type);
    }

    lats = new float[nindex];
    lons = new float[nindex];
    for (int i=0; i<nindex; i++) {
      float[][] values = wind_stations[i].getFloats(false);
      lats[i] = values[lat_index][0];
      lons[i] = values[lon_index][0];
    }

    stations_ref = new DataReferenceImpl("stations_ref");
    stations_ref.setData(wind_field);
    barb_renderer = new BarbRendererJ3D();
    display.addReferences(barb_renderer, stations_ref);
    which_time = -1;
    station_refs = new DataReferenceImpl[nindex];
    barb_manipulation_renderers = new BarbManipulationRendererJ3D[nindex];
    barb_monitors = new BarbMonitor[nindex];
    for (int i=0; i<nindex; i++) {
      station_refs[i] = new DataReferenceImpl("station_ref" + i);
      station_refs[i].setData(tuples[i][0]);
      which_times[i] = -1;
      barb_manipulation_renderers[i] = new BarbManipulationRendererJ3D();
      display.addReferences(barb_manipulation_renderers[i], station_refs[i]);
      barb_monitors[i] = new BarbMonitor(station_refs[i], i);
      barb_monitors[i].addReference(station_refs[i]);
    }

    wind_monitor = new WindMonitor();
    wind_monitor.addReference(stations_ref);

    control.setCurrent(0);
  }

  public void endManipulation()
         throws VisADException, RemoteException {
    for (int i=0; i<nindex; i++) {
      display.removeReference(station_refs[i]);
    }
    barb_renderer = new BarbRendererJ3D();
    display.addReferences(barb_renderer, stations_ref);
  }

  private boolean first = true;

  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
    which_time = -1;
    if (barb_manipulation_renderers == null) return;
    for (int i=0; i<nindex; i++) {
      which_times[i] = -1;
      if (barb_manipulation_renderers[i] == null) return;
      barb_manipulation_renderers[i].stop_direct();
    }

    Set ts = control.getSet();
    if (ts == null) return;
    if (time_set == null) {
      time_set = ts;
      global_ntimes = time_set.getLength();
      times = new double[global_ntimes];
      global_to_station = new int[nindex][global_ntimes];
      station_to_global = new int[nindex][];
      for (int i=0; i<nindex; i++) {
        station_to_global[i] = new int[ntimes[i]];
        for (int j=0; j<ntimes[i]; j++) station_to_global[i][j] = -1;
      }
      RealTupleType in = ((SetType) time_set.getType()).getDomain();
      for (int j=0; j<global_ntimes; j++) {
        int[] indices = {j};
        double[][] fvalues = time_set.indexToDouble(indices);
        times[j] = fvalues[0][0];
        for (int i=0; i<nindex; i++) {
          RealTupleType out = ((SetType) time_sets[i].getType()).getDomain();
          double[][] values = CoordinateSystem.transformCoordinates(
                                   out, time_sets[i].getCoordinateSystem(),
                                   time_sets[i].getSetUnits(),
                                   null /* errors */,
                                   in, time_set.getCoordinateSystem(),
                                   time_set.getSetUnits(),
                                   null /* errors */, fvalues);
          if (time_sets[i].getLength() == 1) {
            indices = new int[] {0};
          }
          else {
            indices = time_sets[i].doubleToIndex(values);
          }
          global_to_station[i][j] = indices[0];
          station_to_global[i][indices[0]] = j;
        } // end for (int i=0; i<nindex; i++)
      } // end for (int j=0; j<global_ntimes; j++)
    }
    else { // time_set != null
      if (!time_set.equals(ts)) {
        throw new CollectiveBarbException("time Set changed");
      }
    }

    int current = control.getCurrent();
    if (current < 0) return;
    which_time = current;

    for (int i=0; i<nindex; i++) {
      int index = global_to_station[i][current];
      if (index < tuples[i].length) {
        station_refs[i].setData(tuples[i][index]);
        which_times[i] = index;
      }
      // System.out.println("station " + i + " ref " + index);
    } // end for (int i=0; i<nindex; i++)

    if (first) {
      first = false;
      display.removeReference(stations_ref);
    }
  }

  private final static float DEGREE_EPS = 0.1f;
  private final static float MPS_EPS = 0.01f;

  class WindMonitor extends CellImpl {
    public void doAction() throws VisADException, RemoteException {
      if (nindex != wind_field.getLength()) {
        throw new CollectiveBarbException("number of stations changed");
      }
      for (int i=0; i<nindex; i++) {
        FlatField ff = (FlatField) wind_field.getSample(i);
        if (ntimes[i] != ff.getLength()) {
          throw new CollectiveBarbException("number of times changed");
        }
        for (int j=0; j<ntimes[i]; j++) {
          Tuple tuple = (Tuple) ff.getSample(j);
          Real[] reals = tuple.getRealComponents();
          float new_azimuth = (float) reals[azimuth_index].getValue();
          float new_radial = (float) reals[radial_index].getValue();
          if (!visad.util.Util.isApproximatelyEqual(new_azimuth,
                     azimuths[i][j], DEGREE_EPS) ||
              !visad.util.Util.isApproximatelyEqual(new_radial,
                     radials[i][j], MPS_EPS)) {
            azimuths[i][j] = new_azimuth;
            radials[i][j] = new_radial;
            tuples[i][j] = tuple;
            if (j == which_times[i]) {
              station_refs[i].setData(tuples[i][j]);
            }
          }
        }
      }
    }
  }

  class BarbMonitor extends CellImpl {
    DataReferenceImpl ref;
    int sta_index;
 
    public BarbMonitor(DataReferenceImpl r, int index) {
      ref = r;
      sta_index = index;
    }
  
    public void doAction() throws VisADException, RemoteException {
      int time_index = which_times[sta_index];
      if (time_index < 0) return;

      Tuple wind = (Tuple) ref.getData();
      Real[] reals = wind.getRealComponents();
      float new_azimuth = (float) reals[azimuth_index].getValue();
      float new_radial = (float) reals[radial_index].getValue();
/*
System.out.println("new " + new_azimuth + " " + new_radial +
                   "  old " + azimuths[sta_index][time_index] +
                   " " + radials[sta_index][time_index]);
*/
      // filter out barb changes due to other doAction calls
      if (visad.util.Util.isApproximatelyEqual(new_azimuth,
                 azimuths[sta_index][time_index], DEGREE_EPS) &&
          visad.util.Util.isApproximatelyEqual(new_radial,
                 radials[sta_index][time_index], MPS_EPS)) return;

      wind_monitor.disableAction();

      if (last_sta != sta_index || last_time != time_index) {
        last_sta = sta_index;
        last_time = time_index;
        for (int i=0; i<nindex; i++) {
          for (int j=0; j<ntimes[i]; j++) {
            old_azimuths[i][j] = azimuths[i][j];
            old_radials[i][j] = radials[i][j];
          }
        }
      }

      float diff_azimuth = new_azimuth - old_azimuths[sta_index][time_index];
      float diff_radial = new_radial - old_radials[sta_index][time_index];

      float lat = lats[sta_index];
      float lon = lons[sta_index];
      int global_time_index = station_to_global[sta_index][time_index];
      double time = times[global_time_index];
      for (int i=0; i<nindex; i++) {
        double lat_diff = Math.abs(lat - lats[i]);
        double mid_lat = 0.5 * (lat + lats[i]);
        double coslat = Math.cos(Data.DEGREES_TO_RADIANS * mid_lat);
        double lon_diff = Math.abs(lon - lons[i]) * coslat;
        double dist = ShadowType.METERS_PER_DEGREE *
          Math.sqrt(lon_diff * lon_diff + lat_diff * lat_diff);
        if (dist > outer_distance) continue;
        double dist_mult = (dist <= inner_distance) ? 1.0 :
          (outer_distance - dist) / (outer_distance - inner_distance);
        for (int j=0; j<ntimes[i]; j++) {
          int ix = station_to_global[i][j];
          double time_diff = Math.abs(time - times[ix]);
          if (time_diff > outer_time) continue;
          double time_mult = (time_diff <= inner_time) ? 1.0 :
            (outer_time - time_diff) / (outer_time - inner_time);
          double mult = dist_mult * time_mult;
/*
System.out.println("this " + sta_index + " " + time_index + " that " +
                   i + " " + j + " mult " + mult);
*/
          double azimuth_diff = 0.0;
          double radial_diff = 0.0;

          if (absolute) {
            azimuth_diff = new_azimuth - old_azimuths[i][j];
            radial_diff = new_radial - old_radials[i][j];
          }
          else {
            azimuth_diff = new_azimuth - old_azimuths[sta_index][time_index];
            radial_diff = new_radial - old_radials[sta_index][time_index];
          }
          if (azimuth_diff < -180.0) azimuth_diff += 360.0;
          if (azimuth_diff > 180.0) azimuth_diff -= 360.0;
          double azimuth = old_azimuths[i][j] + mult * (azimuth_diff);
          double radial = old_radials[i][j] + mult * (radial_diff);
          if (radial < 0.0) radial = 0.0;

          Tuple old_wind = tuples[i][j];
          if (old_wind instanceof RealTuple) {
            reals = old_wind.getRealComponents();
            reals[azimuth_index] = reals[azimuth_index].cloneButValue(azimuth);
            reals[radial_index] = reals[radial_index].cloneButValue(radial);
            wind = new RealTuple((RealTupleType) old_wind.getType(), reals,
                             ((RealTuple) old_wind).getCoordinateSystem());
          }
          else { // old_wind instanceof Tuple
            int n = old_wind.getDimension();
            int k = 0;
            Data[] components = new Data[n];
            for (int c=0; c<n; c++) {
              components[c] = old_wind.getComponent(c);
              if (components[c] instanceof Real) {
                if (k == azimuth_index) {
                  components[c] =
                    ((Real) components[c]).cloneButValue(azimuth);
                }
                if (k == radial_index) {
                  components[c] =
                    ((Real) components[c]).cloneButValue(radial);
                }
                k++;
              }
              else { // (components[c] instanceof RealTuple)
                int m = ((RealTuple) components[c]).getDimension();
                if ((k <= azimuth_index && azimuth_index < k+m) ||
                    (k <= radial_index && radial_index < k+m)) {
                  reals = ((RealTuple) components[c]).getRealComponents();
                  if (k <= azimuth_index && azimuth_index < k+m) {
                    reals[azimuth_index - k] =
                      reals[azimuth_index - k].cloneButValue(azimuth);
                  }
                  if (k <= radial_index && radial_index < k+m) {
                    reals[radial_index - k] =
                      reals[radial_index - k].cloneButValue(radial);
                  }
                  components[c] =
                    new RealTuple((RealTupleType) components[c].getType(),
                                  reals,
                         ((RealTuple) components[c]).getCoordinateSystem());
                }
                k += m;
              } // end if (components[c] instanceof RealTuple)
            } // end for (int c=0; c<n; c++)
            wind = new Tuple((TupleType) old_wind.getType(), components,
                             false);
          } // end if (old_wind instanceof Tuple)

          azimuths[i][j] = (float) azimuth;
          radials[i][j] = (float) radial;
          wind_stations[i].setSample(j, wind);
          tuples[i][j] = wind;
          if (i != sta_index && j == which_times[i]) {
            station_refs[i].setData(tuples[i][j]);
          }
        } // end for (int j=0; j<ntimes[i]; j++)
      } // end for (int i=0; i<nindex; i++)

      wind_monitor.enableAction();
    }
  }

  private static final int NSTAS = 5; // actually NSTAS * NSTAS
  private static final int NTIMES = 10;

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
    windds =
      new RealTupleType(new RealType[] {wind_dir, wind_speed},
      new WindPolarCoordinateSystem(windxy), null);

    RealType stn = new RealType("station");
    Set stn_set = new Integer1DSet(stn, NSTAS * NSTAS);
    RealType time = RealType.Time;
    double start = new DateTime(1999, 122, 57060).getValue();
    Set time_set = new Linear1DSet(time, start, start + 3000.0, NTIMES);

    TupleType tuple_type = null;
    tuple_type = new TupleType(new MathType[]
          {lon, lat, windds, red, green});

    FunctionType station_type = new FunctionType(time, tuple_type);
    FunctionType stations_type = new FunctionType(stn, station_type);

    // construct Java3D display and mappings that govern
    // how wind records are displayed
    DisplayImplJ3D display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    lonmap.setRange(-10.0, 10.0);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(-50.0, -30.0);

    FlowControl flow_control;
    ScalarMap winds_map = new ScalarMap(wind_speed, Display.Flow1Radial);
    display.addMap(winds_map);
    winds_map.setRange(0.0, 1.0); // do this for barb rendering
    ScalarMap windd_map = new ScalarMap(wind_dir, Display.Flow1Azimuth);
    display.addMap(windd_map);
    windd_map.setRange(0.0, 360.0); // do this for barb rendering
    flow_control = (FlowControl) windd_map.getControl();
    flow_control.setFlowScale(0.15f); // this controls size of barbs

    display.addMap(new ScalarMap(red, Display.Red));
    display.addMap(new ScalarMap(green, Display.Green));
    display.addMap(new ConstantMap(1.0, Display.Blue));

    ScalarMap amap = new ScalarMap(time, Display.Animation);
    display.addMap(amap);
    AnimationControl acontrol = (AnimationControl) amap.getControl();
    acontrol.setStep(2000);

    // create an array of NSTAS by NSTAS winds
    FieldImpl field = new FieldImpl(stations_type, stn_set);
    int m = 0;
    for (int i=0; i<NSTAS; i++) {
      for (int j=0; j<NSTAS; j++) {
        FlatField ff = new FlatField(station_type, time_set);
        double[][] values = new double[6][NTIMES];
        for (int k=0; k<NTIMES; k++) {
          double u = 2.0 * i / (NSTAS - 1.0) - 1.0;
          double v = 2.0 * j / (NSTAS - 1.0) - 1.0;
  
          // each wind record is a Tuple (lon, lat, (windx, windy), red, green)
          // set colors by wind components, just for grins
          values[0][k] = 10. * u;
          values[1][k] = 10.0 * v - 40.0;
          double fx = 30.0 * u;
          double fy = 30.0 * v;
          double fd =
            Data.RADIANS_TO_DEGREES * Math.atan2(-fx, -fy) + k * 15.0;
          double fs = Math.sqrt(fx * fx + fy * fy);
          values[2][k] = fd;
          values[3][k] = fs;
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

    CollectiveBarbManipulation cbm =
      new CollectiveBarbManipulation(field, display, false,
                                     0.0f, 1000000.0f, 0.0f, 1000.0f);

    JButton end = new JButton("end manip");
    end.addActionListener(new EndManipCBM(cbm));
    end.setActionCommand("end");
    panel.add(end);

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);
  }
}

class EndManipCBM implements ActionListener {
  CollectiveBarbManipulation cbm;

  EndManipCBM(CollectiveBarbManipulation c) {
    cbm = c;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("end")) {
      try {
        cbm.endManipulation();
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
  }
}

