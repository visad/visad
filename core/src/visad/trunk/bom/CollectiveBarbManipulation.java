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
  private Tuple[][] tuples2;
  private FlatField[] wind_stations;
  private Set[] time_sets;
  private double[][] times;
  private int which_time = -1;
  private int[] which_times;

  private Set time_set = null;
  private int global_ntimes = 0;
  private double[] global_times;
  private int[][] global_to_station; // [nindex][global_ntimes]
  private int[][] station_to_global; // [nindex][ntimes[i]]

  private float[] lats;
  private float[] lons;

  private DataReferenceImpl stations_ref;
  private DataReferenceImpl[] station_refs;
  private DataRenderer barb_renderer;
  private DataRenderer[] barb_manipulation_renderers;
  private BarbMonitor[] barb_monitors;

  private WindMonitor wind_monitor = null;

  private AnimationControl control = null;

  private DisplayImplJ3D display1;
  private DisplayImplJ3D display2;
  private FieldImpl wind_field;
  private boolean absolute;
  private float inner_distance;
  private float outer_distance;
  private double inner_time;
  private double outer_time;
  private DataReference curve_ref = null;

  private boolean barbs;
  private boolean force_station;
  private boolean knots;

  private ConstantMap[] cmaps;

  private int azimuth_index;
  private int radial_index;
  private int azimuth_index2;
  private int radial_index2;
  private int lat_index;
  private int lon_index;

  private int last_sta = -1;
  private int last_time = -1;
  private int last_display = -1;
  private boolean last_curve = true;

  private float[][] azimuths;
  private float[][] radials;
  private float[][] old_azimuths;
  private float[][] old_radials;

  // data for display2
  private int station = -1;
  private DataReferenceImpl[] time_refs = null;
  private DataRenderer[] barb_manipulation_renderers2 = null;
  private BarbMonitor2[] barb_monitors2 = null;

  private Object data_lock = new Object();

  private boolean ended = false; // manipulation ended

  private int time_dir = 0; // < 0 for backward only, > 0 for forward only,
                            // == 0 for both directions

  private static final int NCIRCLE = 24;
  private DataRenderer[] circle_renderer = null;
  private DataReferenceImpl[] circle_ref = null;
  private boolean circle_enable = false;

  private Releaser releaser = null;
  private DataReferenceImpl releaser_ref = null;

  /**
     wf should have MathType:
       (station_index -> (Time -> tuple))
     where tuple is flat
       [e.g., (Latitude, Longitude, (flow_dir, flow_speed))]
     and must include RealTypes Latitude and Longitude plus
     RealTypes mapped to Flow1Azimuth and Flow1Radial, or to
     Flow2Azimuth and Flow2Radial, in the DisplayImplJ3Ds d1
     and d2 (unless they are not null);
     d1 must have Time mapped to Animation, and d2 must not;

     abs indicates absolute or relative value adjustment
     id and od are inner and outer distances in meters
     it and ot are inner and outer times in seconds
     influence is 1.0 inside inner, 0.0 outside outer and
     linear between distance and time influences multiply;

     cms are ConstantMap's used for rendering barbs

     each time the user clicks the right mouse button to
     manipulate a wind barb, the "reference" values for all
     wind barbs are set - thus repeatedly adjusting the same
     barb will magnify its influence over its neighbors;

     sta is index of station for display2;

     need_monitor is true if wf might be changed externally
     during manipulation

     brbs is true to indicate Barb*RendererJ3D, false to
     indicate Swell*RendererJ3D

     fs is true to indicate that d2 should switch to whatever
     station is being manipulated

     kts is false to indicate no m/s to knots conversion in
     wind barb renderers

     inner_circle_color is array of RGB colors for an inner circle
     of influence, or null for no inner circles
     inner_circle_width is the line width for the inner circle

     outer_circle_color is array of RGB colors for an outer circle
     of influence, or null for no outer circles
     outer_circle_width is the line width for the outer circle
  */
  public CollectiveBarbManipulation(FieldImpl wf,
                 DisplayImplJ3D d1, DisplayImplJ3D d2, ConstantMap[] cms,
                 boolean abs, float id, float od, float it, float ot, int sta,
                 boolean need_monitor, boolean brbs, boolean fs, boolean kts,
                 double[] inner_circle_color, int inner_circle_width,
                 double[] outer_circle_color, int outer_circle_width)
         throws VisADException, RemoteException {
    wind_field = wf;
    display1 = d1;
    display2 = d2;
    cmaps = cms;
    absolute = abs;
    inner_distance = id;
    outer_distance = od;
    inner_time = it;
    outer_time = ot;
    curve_ref = null;
    barbs = brbs;
    force_station = fs;
    knots = kts;

    station = sta;

    if (display1 == null && display2 == null) {
      throw new CollectiveBarbException("display1 and display2 cannot " +
                  "both be null");
    }

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
 
    lat_index = -1;
    lon_index = -1;
    // int tuple_dim = wind_type.getDimension();
    RealType[] real_types = wind_type.getRealComponents();
    int tuple_dim = real_types.length;
    for (int i=0; i<tuple_dim; i++) {
      RealType real = real_types[i];
      if (RealType.Latitude.equals(real)) {
        lat_index = i;
      }
      else if (RealType.Longitude.equals(real)) {
        lon_index = i;
      }
    }

    if (lat_index < 0 || lon_index < 0) {
      throw new CollectiveBarbException("wind data must include Latitude " +
               "and Longitude " + lat_index + " " + lon_index);
    }

    try {
      nindex = wind_field.getLength();
      ntimes = new int[nindex];
      tuples = new Tuple[nindex][];
      tuples2 = new Tuple[nindex][];
      which_times = new int[nindex];
      wind_stations = new FlatField[nindex];
      time_sets = new Set[nindex];
      times = new double[nindex][];
      azimuths = new float[nindex][];
      radials = new float[nindex][];
      old_azimuths = new float[nindex][];
      old_radials = new float[nindex][];
      for (int i=0; i<nindex; i++) {
        wind_stations[i] = (FlatField) wind_field.getSample(i);
        ntimes[i] = wind_stations[i].getLength();
        time_sets[i] = wind_stations[i].getDomainSet();
        double[][] dummy = Set.floatToDouble(time_sets[i].getSamples());
        times[i] = dummy[0];
        time_set = (i == 0) ? time_sets[i] : time_set.merge1DSets(time_sets[i]);
        tuples[i] = new Tuple[ntimes[i]];
        tuples2[i] = new Tuple[ntimes[i]];
        azimuths[i] = new float[ntimes[i]];
        radials[i] = new float[ntimes[i]];
        old_azimuths[i] = new float[ntimes[i]];
        old_radials[i] = new float[ntimes[i]];
        Enumeration e = wind_stations[i].domainEnumeration();
        for (int j=0; j<ntimes[i]; j++) {
          tuples[i][j] = (Tuple) wind_stations[i].getSample(j);
          int n = tuples[i][j].getDimension();
          Data[] components = new Data[n + 1];
          for (int k=0; k<n; k++) {
            components[k] = tuples[i][j].getComponent(k);
          }
          components[n] = ((RealTuple) e.nextElement()).getComponent(0);
          tuples2[i][j] = new Tuple(components);
        }
      }
    }
    catch (ClassCastException e) {
      throw new CollectiveBarbException("wind_field bad MathType: " +
                     wind_field_type);
    }
 
    global_ntimes = time_set.getLength();
    global_times = new double[global_ntimes];
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
      global_times[j] = fvalues[0][0];
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

    lats = new float[nindex];
    lons = new float[nindex];
    for (int i=0; i<nindex; i++) {
      float[][] values = wind_stations[i].getFloats(false);
      lats[i] = values[lat_index][0];
      lons[i] = values[lon_index][0];
    }

    azimuth_index = -1;
    radial_index = -1;
    int azimuth12 = -1;
    int radial12 = -2;
    if (display1 != null) {
      Vector scalar_map_vector = display1.getMapVector();
      for (int i=0; i<tuple_dim; i++) {
        RealType real = real_types[i];
        Enumeration enum = scalar_map_vector.elements();
        while (enum.hasMoreElements()) {
          ScalarMap map = (ScalarMap) enum.nextElement();
          if (real.equals(map.getScalar())) {
            DisplayRealType dreal = map.getDisplayScalar();
            if (Display.Flow1Azimuth.equals(dreal)) {
              azimuth_index = i;
              azimuth12 = 1;
            }
            else if (Display.Flow2Azimuth.equals(dreal)) {
              azimuth_index = i;
              azimuth12 = 2;
            }
            else if (Display.Flow1Radial.equals(dreal)) {
              radial_index = i;
              radial12 = 1;
            }
            else if (Display.Flow2Radial.equals(dreal)) {
              radial_index = i;
              radial12 = 2;
            }
          }
        }
      } // for (int i=0; i<n; i++) {
      if (azimuth_index < 0 || radial_index < 0 || azimuth12 != radial12) {
        throw new CollectiveBarbException("wind data must include two " +
                 "RealTypes mapped to Flow1Azimuth and Flow1Radial, or to " +
                 "Flow2Azimuth and Flow2Radial, in " +
                 "display1 " + azimuth_index + " " + radial_index);
      }
    }

    azimuth_index2 = -1;
    radial_index2 = -1;
    azimuth12 = -1;
    radial12 = -2;
    if (display2 != null) {
      Vector scalar_map_vector = display2.getMapVector();
      for (int i=0; i<tuple_dim; i++) {
        RealType real = real_types[i];
        Enumeration enum = scalar_map_vector.elements();
        while (enum.hasMoreElements()) {
          ScalarMap map = (ScalarMap) enum.nextElement();
          if (real.equals(map.getScalar())) {
            DisplayRealType dreal = map.getDisplayScalar();
            if (Display.Flow1Azimuth.equals(dreal)) {
              azimuth_index2 = i;
              azimuth12 = 1;
            }
            else if (Display.Flow2Azimuth.equals(dreal)) {
              azimuth_index2 = i;
              azimuth12 = 2;
            }
            else if (Display.Flow1Radial.equals(dreal)) {
              radial_index2 = i;
              radial12 = 1;
            }
            else if (Display.Flow2Radial.equals(dreal)) {
              radial_index2 = i;
              radial12 = 2;
            }
          }
        }
      } // for (int i=0; i<n; i++) {
      if (azimuth_index2 < 0 || radial_index2 < 0 || azimuth12 != radial12) {
        throw new CollectiveBarbException("wind data must include two " +
                 "RealTypes mapped to Flow1Azimuth and Flow1Radial, or to " +
                 "Flow2Azimuth and Flow2Radial, in " +
                 "display2 " + azimuth_index2 + " " + radial_index2);
      }
      if (display1 != null) {
        if (azimuth_index2 != azimuth_index) {
          throw new CollectiveBarbException("same RealTypes must be mapped " +
                   "to Flow1Azimuth in display1 and display2 " +
                   real_types[azimuth_index] + " " + real_types[azimuth_index2]);
        }
        if (radial_index2 != radial_index) {
          throw new CollectiveBarbException("same RealTypes must be mapped " +
                   "to Flow1Radial in display1 and display2 " +
                   real_types[radial_index] + " " + real_types[radial_index2]);
        }
      }
      else {
        azimuth_index = azimuth_index2;
        radial_index = radial_index2;
      }
    }

    for (int i=0; i<nindex; i++) {
      for (int j=0; j<ntimes[i]; j++) {
        Real[] reals = tuples[i][j].getRealComponents();
        azimuths[i][j] = (float) reals[azimuth_index].getValue();
        radials[i][j] = (float) reals[radial_index].getValue();
        old_azimuths[i][j] = azimuths[i][j];
        old_radials[i][j] = radials[i][j];
      }
    }
 
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

    if (display1 != null) {
      control = (AnimationControl) display1.getControl(AnimationControl.class);
      if (control == null) {
        throw new CollectiveBarbException("display must include " +
                       "ScalarMap to Animation");
      }
      // use a ControlListener on Display.Animation to fake
      // animation of manipulable barbs
      control.addControlListener(this);

      stations_ref = new DataReferenceImpl("stations_ref");
      stations_ref.setData(wind_field);
      if (barbs) {
        barb_renderer = new BarbRendererJ3D();
      }
      else {
        barb_renderer = new SwellRendererJ3D();
      }
      ((BarbRenderer) barb_renderer).setKnotsConvert(knots);
      display1.addReferences(barb_renderer, stations_ref, constantMaps());
      which_time = -1;
      station_refs = new DataReferenceImpl[nindex];
      barb_manipulation_renderers = new DataRenderer[nindex];
      barb_monitors = new BarbMonitor[nindex];
      for (int i=0; i<nindex; i++) {
        station_refs[i] = new DataReferenceImpl("station_ref" + i);
        station_refs[i].setData(tuples[i][0]);
        which_times[i] = -1;
        if (barbs) {
          barb_manipulation_renderers[i] = new CBarbManipulationRendererJ3D(this);
        }
        else {
          barb_manipulation_renderers[i] = new CSwellManipulationRendererJ3D(this);
        }
        ((BarbRenderer) barb_manipulation_renderers[i]).setKnotsConvert(knots);
        display1.addReferences(barb_manipulation_renderers[i], station_refs[i],
                               constantMaps());
        barb_monitors[i] = new BarbMonitor(station_refs[i], i);
        barb_monitors[i].addReference(station_refs[i]);
      }

      if (need_monitor) {
        wind_monitor = new WindMonitor();
        wind_monitor.addReference(stations_ref);
      }
  
      control.setCurrent(0);
    } // end if (display1 != null)

    if (display2 != null) {
      setStation(sta);
    }

    time_dir = 0;

    circle_ref = new DataReferenceImpl[] {null, null};
    circle_renderer = new DefaultRendererJ3D[] {null, null};
    if (display1 != null && inner_circle_color != null) {
      double cw = (inner_circle_width > 0) ? inner_circle_width : 1;
      ConstantMap[] color_maps =
        {new ConstantMap(inner_circle_color[0], Display.Red),
         new ConstantMap(inner_circle_color[1], Display.Green),
         new ConstantMap(inner_circle_color[2], Display.Blue),
         new ConstantMap(cw, Display.LineWidth)};
      circle_ref[0] = new DataReferenceImpl("inner_circle");
      circle_ref[0].setData(null);
      circle_renderer[0] = new DefaultRendererJ3D();
      display1.addReferences(circle_renderer[0], circle_ref[0], color_maps);
      circle_renderer[0].toggle(false);
      circle_renderer[0].suppressExceptions(true);
    }
    if (display1 != null && outer_circle_color != null) {
      double cw = (outer_circle_width > 0) ? outer_circle_width : 1;
      ConstantMap[] color_maps =
        {new ConstantMap(outer_circle_color[0], Display.Red),
         new ConstantMap(outer_circle_color[1], Display.Green),
         new ConstantMap(outer_circle_color[2], Display.Blue),
         new ConstantMap(cw, Display.LineWidth)};
      circle_ref[1] = new DataReferenceImpl("outer_circle");
      circle_ref[1].setData(null);
      circle_renderer[1] = new DefaultRendererJ3D();
      display1.addReferences(circle_renderer[1], circle_ref[1], color_maps);
      circle_renderer[1].toggle(false);
      circle_renderer[1].suppressExceptions(true);
    }

    releaser_ref = new DataReferenceImpl("releaser");
    releaser = new Releaser();
    releaser.addReference(releaser_ref);

  }

  // public void addStation(FlatField station)
  public void addStation(float lat, float lon)
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      // construct FlatField station for global times at (lat, lon)
      // all winds = 0 (?)
      FlatField station = null;
      addStation(station);
    }
  }

  public void addStation(FlatField station)
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      // check MathType of station
      // add station to wind_field
      // increment nindex
      // fix everything else

/*
     wf should have MathType:
       (station_index -> (Time -> tuple))
     where tuple is flat
       [e.g., (Latitude, Longitude, (flow_dir, flow_speed))]
     if (nindex != wind_field.getLength()) {
*/

    }
  }

  /** set values that govern collective barb adjustment
     and disable any DataReference to a spatial curve;
     abs indicates absolute or relative value adjustment
     id and od are inner and outer distances in meters
     it and ot are inner and outer times in seconds
     influence is 1.0 inside inner, 0.0 outside outer and
     linear between distance and time influences multiply */
  public void setCollectiveParameters(boolean abs, float id, float od,
                                      float it, float ot)
         throws VisADException, RemoteException {
    absolute = abs;
    if (it < 0.0 || ot < it) {
      throw new CollectiveBarbException("outer_time must be " +
                                   "greater than inner_time");
    }
    if (id < 0.0 || od < id) {
      throw new CollectiveBarbException("outer_distance must be " +
                                   "greater than distance_time");
    }
    inner_distance = id;
    outer_distance = od;
    curve_ref = null;
    inner_time = it;
    outer_time = ot;
  }

  /** set values that govern collective barb adjustment
      but don't change spatial parameters or curve_ref;
     abs indicates absolute or relative value adjustment
     it and ot are inner and outer times in seconds
     influence is 1.0 inside inner, 0.0 outside outer and
     linear between distance and time influences multiply */
  public void setCollectiveTimeParameters(boolean abs, float it, float ot)
         throws VisADException, RemoteException {
    absolute = abs;
    if (it < 0.0 || ot < it) {
      throw new CollectiveBarbException("outer_time must be " +
                                   "greater than inner_time");
    }
    inner_time = it;
    outer_time = ot;
  }

  /** set value that governs whether collective changes are
      propagated only forward in time (dir > 0), only backward
      (dir < 0), or both (dir == 0) */
  public void setTimeDir(int dir) {
    time_dir = dir;
  }

  /** set a DataReference to a curve (typically from a
      CurveManipulationRendererJ3D) to replace distance
      parameters; also set time parameters */
  public void setCollectiveCurve(boolean abs, DataReference r,
                                 float it, float ot)
         throws VisADException, RemoteException {
    absolute = abs;
    if (it < 0.0 || ot < it) {
      throw new CollectiveBarbException("outer_time must be " +
                                   "greater than inner_time");
    }
    curve_ref = r;
    inner_time = it;
    outer_time = ot;
  }

  /** called by the application to select which station is selected
      in display2 */
  public void setStation(int sta)
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      if (display2 == null) {
        throw new CollectiveBarbException("display2 cannot be null");
      }
      if (sta < 0 || sta >= nindex) {
        throw new CollectiveBarbException("bad station index " + sta);
      }
      station = sta;
  
      if (time_refs != null && time_refs.length != ntimes[station]) {
        int n = time_refs.length;
        for (int i=0; i<n; i++) {
          display2.removeReference(time_refs[i]);
          barb_monitors2[i].removeReference(time_refs[i]);
          barb_monitors2[i].stop();
        }
        time_refs = null;
      }
  
      if (time_refs == null) {
        int n = ntimes[station];
        time_refs = new DataReferenceImpl[n];
        barb_manipulation_renderers2 = new DataRenderer[n];
        barb_monitors2 = new BarbMonitor2[n];
        for (int i=0; i<n; i++) {
          time_refs[i] = new DataReferenceImpl("time_ref" + i);
          time_refs[i].setData(tuples2[station][i]);
          if (ended) {
            if (barbs) {
              barb_manipulation_renderers2[i] = new BarbRendererJ3D();
            }
            else {
              barb_manipulation_renderers2[i] = new SwellRendererJ3D();
            }
          }
          else {
            if (barbs) {
              barb_manipulation_renderers2[i] = new BarbManipulationRendererJ3D();
            }
            else {
              barb_manipulation_renderers2[i] = new SwellManipulationRendererJ3D();
            }
          }
          ((BarbRenderer) barb_manipulation_renderers2[i]).setKnotsConvert(knots);
          display2.addReferences(barb_manipulation_renderers2[i], time_refs[i],
                                 constantMaps());
          barb_monitors2[i] = new BarbMonitor2(time_refs[i], i);
          barb_monitors2[i].addReference(time_refs[i]);
        }
      }
      else {
        int n = ntimes[station];
        for (int i=0; i<n; i++) {
          time_refs[i].setData(tuples2[station][i]);
        }
      }
    }
  }

  public DataRenderer[] getManipulationRenderers() {
    return barb_manipulation_renderers;
  }

  public DataRenderer[] getManipulationRenderers2() {
    return barb_manipulation_renderers2;
  }

  private ConstantMap[] constantMaps() {
    if (cmaps == null || cmaps.length == 0) return null;
    int n = cmaps.length;
    ConstantMap[] cms = new ConstantMap[n];
    for (int i=0; i<n; i++) {
      cms[i] = (ConstantMap) cmaps[i].clone();
    }
    return cms;
  }

  /** called by the application to end manipulation;
      returns the final wind field */
  public FieldImpl endManipulation()
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      ended = true;
      if (display1 != null) {
        for (int i=0; i<nindex; i++) {
          display1.removeReference(station_refs[i]);
          barb_monitors[i].removeReference(station_refs[i]);
          barb_monitors[i].stop();
        }
        if (barbs) {
          barb_renderer = new BarbRendererJ3D();
        }
        else {
          barb_renderer = new SwellRendererJ3D();
        }
        ((BarbRenderer) barb_renderer).setKnotsConvert(knots);
        display1.addReferences(barb_renderer, stations_ref, constantMaps());
      }
      if (display2 != null && time_refs != null) {
        int n = time_refs.length;
        for (int i=0; i<n; i++) {
          display2.removeReference(time_refs[i]);
          barb_monitors2[i].removeReference(time_refs[i]);
          barb_monitors2[i].stop();
        }
        time_refs = null;
        setStation(station);
      }
      return wind_field;
    }
  }

  private boolean first = true;

  // for AnimationControl steps
  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      which_time = -1;
      if (barb_manipulation_renderers == null) return;
      for (int i=0; i<nindex; i++) {
        which_times[i] = -1;
        if (barb_manipulation_renderers[i] == null) return;
        barb_manipulation_renderers[i].stop_direct();
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
        display1.removeReference(stations_ref);
      }
    }
  }

  private final static float DEGREE_EPS = 0.1f;
  private final static float MPS_EPS = 0.01f;

  class WindMonitor extends CellImpl {
    public void doAction() throws VisADException, RemoteException {
      synchronized (data_lock) {
        if (nindex != wind_field.getLength()) {
          throw new CollectiveBarbException("number of stations changed");
        }
        for (int i=0; i<nindex; i++) {
          FlatField ff = (FlatField) wind_field.getSample(i);
          if (ntimes[i] != ff.getLength()) {
            throw new CollectiveBarbException("number of times changed");
          }
          Enumeration e = ff.domainEnumeration();
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
              if (station_refs != null && j == which_times[i]) {
                station_refs[i].setData(tuples[i][j]);
              }
  
              int n = tuple.getDimension();
              Data[] components = new Data[n + 1];
              for (int k=0; k<n; k++) {
                components[k] = tuple.getComponent(k);
              }
              components[n] = ((RealTuple) e.nextElement()).getComponent(0);
              tuples2[i][j] = new Tuple(components);
              if (time_refs != null && i == station) {
                time_refs[j].setData(tuples2[i][j]);
              }
            }
          }
        }
      }
    }
  }

  private Tuple modifyWind(Tuple old_wind, double azimuth, double radial)
          throws VisADException, RemoteException {
    Tuple wind = null;
    Real[] reals = null;
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
    return wind;
  }

  private void collectiveAdjust(int sta_index, int time_index,
                               DataReferenceImpl ref, int display_index)
          throws VisADException, RemoteException {

    Tuple new_wind = (Tuple) ref.getData();
    Real[] reals = new_wind.getRealComponents();
    float new_azimuth = (float) reals[azimuth_index].getValue();
    float new_radial = (float) reals[radial_index].getValue();

    // filter out barb changes due to other doAction calls
    if (visad.util.Util.isApproximatelyEqual(new_azimuth,
               azimuths[sta_index][time_index], DEGREE_EPS) &&
        visad.util.Util.isApproximatelyEqual(new_radial,
               radials[sta_index][time_index], MPS_EPS)) return;

    if (display_index == 1 && curve_ref == null) makeCircles(sta_index);

    boolean curve = (curve_ref != null);
    if (last_sta != sta_index || last_time != time_index ||
        last_display != display_index || last_curve != curve) {
      if (force_station && sta_index != station) setStation(sta_index);

      last_sta = sta_index;
      last_time = time_index;
      last_display = display_index;
      last_curve = curve;
      for (int i=0; i<nindex; i++) {
        for (int j=0; j<ntimes[i]; j++) {
          old_azimuths[i][j] = azimuths[i][j];
          old_radials[i][j] = radials[i][j];
        }
      }
    }

    if (wind_monitor != null) wind_monitor.disableAction();

    float diff_azimuth = new_azimuth - old_azimuths[sta_index][time_index];
    float diff_radial = new_radial - old_radials[sta_index][time_index];
    float lat = lats[sta_index];
    float lon = lons[sta_index];
    double time = times[sta_index][time_index];
    for (int i=0; i<nindex; i++) {
      double dist_mult = 0.0;
      if (curve_ref != null) {
        try {
          Data data = curve_ref.getData();
          Gridded2DSet set = null;
          if (data instanceof Gridded2DSet) {
            set = (Gridded2DSet) data;
          }
          else if (data instanceof UnionSet) {
            UnionSet us = (UnionSet) curve_ref.getData();
            SampledSet[] sets = us.getSets();
            if (sets.length > 0 &&
                sets[sets.length - 1] instanceof Gridded2DSet) {
              set = (Gridded2DSet) sets[sets.length - 1];
            }
          }
          if (set != null) {
            float[][] samples = set.getSamples();
            if (DelaunayCustom.inside(samples, lat, lon) &&
                DelaunayCustom.inside(samples, lats[i], lons[i])) {
              dist_mult = 1.0;
            }
          }
        }
        catch (VisADException ex) {
          dist_mult = 0.0;
        }
      }
      else {
        double lat_diff = Math.abs(lat - lats[i]);
        double mid_lat = 0.5 * (lat + lats[i]);
        double coslat = Math.cos(Data.DEGREES_TO_RADIANS * mid_lat);
        double lon_diff = Math.abs(lon - lons[i]) * coslat;
        double dist = ShadowType.METERS_PER_DEGREE *
          Math.sqrt(lon_diff * lon_diff + lat_diff * lat_diff);
        if (dist > outer_distance) continue;
        dist_mult = (dist <= inner_distance) ? 1.0 :
          (outer_distance - dist) / (outer_distance - inner_distance);
      }
      for (int j=0; j<ntimes[i]; j++) {
        int ix = station_to_global[i][j];
        double time_diff = Math.abs(time - times[i][j]);
        if (time_diff > outer_time) continue;
        double time_mult = (time_diff <= inner_time) ? 1.0 :
          (outer_time - time_diff) / (outer_time - inner_time);

        // WLH 13 July 2000
        if ((time_dir > 0 && times[i][j] < time) ||
            (time_dir < 0 && time < times[i][j])) {
          continue;
        }

        double mult = dist_mult * time_mult;
        if (i == sta_index && j == time_index) mult = 1.0;
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

        Tuple wind = modifyWind(tuples[i][j], azimuth, radial);
        Tuple wind2 = modifyWind(tuples2[i][j], azimuth, radial);

        azimuths[i][j] = (float) azimuth;
        radials[i][j] = (float) radial;
        wind_stations[i].setSample(j, wind);
        tuples[i][j] = wind;
        if (station_refs != null && j == which_times[i]) {
          station_refs[i].setData(tuples[i][j]);
        }
        tuples2[i][j] = wind2;
        if (time_refs != null && i == station) {
          time_refs[j].setData(tuples2[i][j]);
        }
      } // end for (int j=0; j<ntimes[i]; j++)
    } // end for (int i=0; i<nindex; i++)
    if (wind_monitor != null) wind_monitor.enableAction();
  }

  private RealTupleType circle_type = null;

  private void makeCircles(int sta_index) {
    if ((circle_ref[0] == null && circle_ref[1] == null) ||
        !circle_enable) return;
    try {
      if (circle_type == null) {
        circle_type = new RealTupleType(RealType.Latitude, RealType.Longitude);
      }
      float lat = lats[sta_index];
      float lon = lons[sta_index];
      float[] dists =
        {inner_distance / ShadowType.METERS_PER_DEGREE,
         outer_distance / ShadowType.METERS_PER_DEGREE};
      float[][] circle = new float[2][NCIRCLE+1];
      float coslat = (float) Math.cos(Data.DEGREES_TO_RADIANS * lat);
      for (int io=0; io<2; io++) {
        if (circle_ref[io] != null) {
          for (int i=0; i<=NCIRCLE; i++) {
            double angle = Data.DEGREES_TO_RADIANS * 360.0 * i / NCIRCLE;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            circle[0][i] = lat + dists[io] * cos;
            circle[1][i] = lon + dists[io] * sin / coslat;
          }
          Gridded2DSet set = new Gridded2DSet(circle_type, circle, NCIRCLE + 1);
          circle_renderer[io].toggle(true);
          circle_ref[io].setData(set);
        }
      }
    }
    catch (VisADException e) {
      System.out.println("makeCircles " + e);
    }
    catch (RemoteException e) {
      System.out.println("makeCircles " + e);
    }
  }

  void circleEnable() {
    circle_enable = true;
  }

  public void release() {
    circle_enable = false;
    try {
      releaser_ref.setData(null);
    }
    catch (VisADException e) {
    }
    catch (RemoteException e) {
    }
/* WLH 14 Feb 2001
    // move the following to a CellImpl?
    for (int io=0; io<2; io++) {
      if (circle_ref[io] != null) {
        circle_renderer[io].toggle(false);
        circle_ref[io].setData(null);
      }
    }
*/
  }

  class Releaser extends CellImpl {
    public void doAction() throws VisADException, RemoteException {
      for (int io=0; io<2; io++) {
        if (circle_ref[io] != null) {
          circle_renderer[io].toggle(false);
          circle_ref[io].setData(null); // ??
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
      synchronized (data_lock) {
        int time_index = which_times[sta_index];
        if (time_index < 0) return;
        collectiveAdjust(sta_index, time_index, ref, 1);
      }
    }
  }

  class BarbMonitor2 extends CellImpl {
    DataReferenceImpl ref;
    int time_index;
 
    public BarbMonitor2(DataReferenceImpl r, int index) {
      ref = r;
      time_index = index;
    }
  
    public void doAction() throws VisADException, RemoteException {
      synchronized (data_lock) {
        int sta_index = station;
        if (sta_index < 0) return;
        collectiveAdjust(sta_index, time_index, ref, 2);
      }
    }
  }


  private static final int NSTAS = 5; // actually NSTAS * NSTAS
  private static final int NTIMES = 10;

  public static void main(String args[])
         throws VisADException, RemoteException {

    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealTupleType earth = new RealTupleType(lat, lon);
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

    // construct first Java3D display and mappings that govern
    // how wind records are displayed
    DisplayImplJ3D display1 =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display1.addMap(lonmap);
    lonmap.setRange(-10.0, 10.0);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display1.addMap(latmap);
    latmap.setRange(-50.0, -30.0);

    ScalarMap winds_map = new ScalarMap(wind_speed, Display.Flow1Radial);
    display1.addMap(winds_map);
    winds_map.setRange(0.0, 1.0); // do this for barb rendering
    ScalarMap windd_map = new ScalarMap(wind_dir, Display.Flow1Azimuth);
    display1.addMap(windd_map);
    windd_map.setRange(0.0, 360.0); // do this for barb rendering
    FlowControl flow_control = (FlowControl) windd_map.getControl();
    flow_control.setFlowScale(0.15f); // this controls size of barbs

    ScalarMap amap = new ScalarMap(time, Display.Animation);
    display1.addMap(amap);
    AnimationControl acontrol = (AnimationControl) amap.getControl();
    acontrol.setStep(1000);

    // construct second Java3D display and mappings that govern
    // how wind records are displayed
    DisplayImplJ3D display2 =
      new DisplayImplJ3D("display2", new TwoDDisplayRendererJ3D());

    ScalarMap winds_map2 = new ScalarMap(wind_speed, Display.Flow1Radial);
    display2.addMap(winds_map2);
    winds_map2.setRange(0.0, 1.0); // do this for barb rendering
    ScalarMap windd_map2 = new ScalarMap(wind_dir, Display.Flow1Azimuth);
    display2.addMap(windd_map2);
    windd_map2.setRange(0.0, 360.0); // do this for barb rendering
    FlowControl flow_control2 = (FlowControl) windd_map2.getControl();
    flow_control2.setFlowScale(0.15f); // this controls size of barbs

    ScalarMap tmap = new ScalarMap(time, Display.XAxis);
    display2.addMap(tmap);
    tmap.setRange(start, start + 3000.0);

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

    ConstantMap[] cmaps = {
      new ConstantMap(1.0, Display.Red),
      new ConstantMap(0.0, Display.Green),
      new ConstantMap(0.0, Display.Blue)};

    final CollectiveBarbManipulation cbm =
      new CollectiveBarbManipulation(field, display1, display2, cmaps, false,
                                     500000.0f, 1000000.0f, 0.0f, 1000.0f,
                                     0, false, (args.length == 0), true, false,
                                     new double[] {0.0, 0.5, 0.5}, 1,
                                     new double[] {0.5, 0.5, 0.0}, 2);

    // construct invisible starter set
    Gridded2DSet set1 =
      new Gridded2DSet(earth, new float[][] {{0.0f}, {0.0f}}, 1);
    Gridded2DSet[] sets = {set1};
    UnionSet set = new UnionSet(earth, sets);

    final DataReferenceImpl set_ref = new DataReferenceImpl("set_ref");
    set_ref.setData(set);
    int mask = InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK;
    display1.addReferences(new CurveManipulationRendererJ3D(mask, mask, true),
                           set_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test CollectiveBarbManipulation");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    // panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    JPanel display_panel = new JPanel();
    display_panel.setLayout(new BoxLayout(display_panel, BoxLayout.Y_AXIS));

    // add displays to JPanel
    JPanel panel1 = (JPanel) display1.getComponent();
    JPanel panel2 = (JPanel) display2.getComponent();
    Border etchedBorder5 =
      new CompoundBorder(new EtchedBorder(),
                         new EmptyBorder(5, 5, 5, 5));
    panel1.setBorder(etchedBorder5);
    panel2.setBorder(etchedBorder5);
    display_panel.add(panel1);
    display_panel.add(panel2);
    display_panel.setMaximumSize(new Dimension(400, 800));

    JPanel widget_panel = new JPanel();
    widget_panel.setLayout(new BoxLayout(widget_panel, BoxLayout.Y_AXIS));

    widget_panel.add(new AnimationWidget(amap));

    final DataReferenceImpl station_select_ref =
      new DataReferenceImpl("station_select_ref");
    VisADSlider station_select_slider =
      new VisADSlider("station", 0, NSTAS * NSTAS - 1, 0, 1.0,
                      station_select_ref, RealType.Generic);
    widget_panel.add(station_select_slider);
    CellImpl cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        int sta = (int) ((Real) station_select_ref.getData()).getValue();
        if (0 <= sta && sta < NSTAS * NSTAS) cbm.setStation(sta);
      }
    };
    cell.addReference(station_select_ref);

    CellImpl cell2 = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        UnionSet cset = (UnionSet) set_ref.getData();
        SampledSet[] csets = cset.getSets();
        if (csets.length > 0) {
          float[][] samples = csets[csets.length - 1].getSamples();
          if (samples != null && samples[0].length > 2) {
            cbm.setCollectiveCurve(false, set_ref, 0.0f, 1000.0f);
          }
        }
      }
    };
    cell2.addReference(set_ref);

    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
    button_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    button_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    EndManipCBM emc = new EndManipCBM(cbm, set_ref);
    JButton end = new JButton("end manip");
    end.addActionListener(emc);
    end.setActionCommand("end");
    button_panel.add(end);
    JButton del = new JButton("delete curve");
    del.addActionListener(emc);
    del.setActionCommand("del");
    button_panel.add(del);
    JButton dir = new JButton("time dir");
    dir.addActionListener(emc);
    dir.setActionCommand("dir");
    button_panel.add(dir);

    widget_panel.add(button_panel);
    widget_panel.setMaximumSize(new Dimension(400, 800));

    panel.add(display_panel);
    panel.add(widget_panel);
    frame.getContentPane().add(panel);

    // set size of JFrame and make it visible
    frame.setSize(800, 800);
    frame.setVisible(true);
  }
}

class EndManipCBM implements ActionListener {
  CollectiveBarbManipulation cbm;
  DataReferenceImpl set_ref;
  int dir = 0;

  EndManipCBM(CollectiveBarbManipulation c, DataReferenceImpl r) {
    cbm = c;
    set_ref = r;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("end")) {
      try {
        FieldImpl final_field = cbm.endManipulation();
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
    else if (cmd.equals("del")) {
      try {
        UnionSet set = (UnionSet) set_ref.getData();
        SampledSet[] sets = set.getSets();
        int n = sets.length - 1;
        SampledSet[] new_sets = null;
        if (n > 0) {
          new_sets = new SampledSet[n];
          System.arraycopy(sets, 0, new_sets, 0, n);
        }
        else {
          new_sets = new SampledSet[] {new Gridded2DSet(set.getType(),
                                new float[][] {{0.0f}, {0.0f}}, 1)};
        }
        set_ref.setData(new UnionSet(set.getType(), new_sets));
        cbm.setCollectiveParameters(false, 500000.0f, 1000000.0f, 0.0f, 1000.0f);
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
    else if (cmd.equals("dir")) {
      dir = (dir == 0) ? +1 : ((dir > 0) ? -1 : 0);
      cbm.setTimeDir(dir);
    }
  }
}

class CBarbManipulationRendererJ3D extends BarbManipulationRendererJ3D {

  CollectiveBarbManipulation cbm;

  CBarbManipulationRendererJ3D(CollectiveBarbManipulation c) {
    super();
    cbm = c;
  }

  /** mouse button released, ending direct manipulation */
  public void release_direct() {
    cbm.release();
  }

  public void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    if (first) cbm.circleEnable();
    super.drag_direct(ray, first, mouseModifiers);
  }
}

class CSwellManipulationRendererJ3D extends SwellManipulationRendererJ3D {

  CollectiveBarbManipulation cbm;

  CSwellManipulationRendererJ3D(CollectiveBarbManipulation c) {
    super();
    cbm = c;
  }

  /** mouse button released, ending direct manipulation */
  public void release_direct() {
    cbm.release();
  }

  public void drag_direct(VisADRay ray, boolean first,
                                       int mouseModifiers) {
    if (first) cbm.circleEnable();
    super.drag_direct(ray, first, mouseModifiers);
  }
}

