//
// FlexibleTrackManipulation.java
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
   FlexibleTrackManipulation is the VisAD class for
   manipulation of flexible storm tracks (not straight lines)
*/
public class FlexibleTrackManipulation extends Object
  implements ControlListener {

  private int ntimes = 0;
  private Tuple[] tuples;
  private int which_time = -1;

  private Set time_set = null;
  private double[] times;

  private float[] lats;
  private float[] lons;
  private float[] old_lats;
  private float[] old_lons;

  private DataReferenceImpl track_ref;
  private DataReferenceImpl[] track_refs;
  private DirectManipulationRendererJ3D[] direct_manipulation_renderers;
  private TrackMonitor[] track_monitors;

  private AnimationControl control = null;

  private DisplayImplJ3D display;
  private FieldImpl storm_track;

  private int shape_index;
  private int lat_index;
  private int lon_index;

  private int last_time = -1;

  /**
     wf should have MathType:
       (Time -> tuple))
     where tuple is flat
       [e.g., (Latitude, Longitude, shape_index)]
     and must include RealTypes Latitude and Longitude plus
     a RealType mapped to Shape in the DisplayImpl d;

     Time may or may not be mapped to Animation
  */
  public FlexibleTrackManipulation(FlatField st, DisplayImplJ3D d)
         throws VisADException, RemoteException {
    storm_track = st;
    display = d;

    control = (AnimationControl) display.getControl(AnimationControl.class);
    // use a ControlListener on Display.Animation to fake
    // animation of the track
    if (control != null) {
      control.addControlListener(this);
    }

    FunctionType storm_track_type = (FunctionType) storm_track.getType();
    TupleType storm_type = null;

    try {
      RealType time =
        (RealType) storm_track_type.getDomain().getComponent(0);
      if (!RealType.Time.equals(time)) {
        throw new DisplayException("storm_track bad MathType: " +
                     time + " must be RealType.Time");
      }
      storm_type = (TupleType) storm_track_type.getRange();
      if (!storm_type.getFlat()) {
        throw new DisplayException("storm_track bad MathType: " +
                      storm_type + " must be flat");
      }
    }
    catch (ClassCastException e) {
      throw new DisplayException("storm_track bad MathType: " +
                     storm_track_type);
    }

    shape_index = -1;
    lat_index = -1;
    lon_index = -1;
    Vector scalar_map_vector = display.getMapVector();
    int tuple_dim = storm_type.getDimension();
    RealType[] real_types = storm_type.getRealComponents();
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
            if (Display.Shape.equals(dreal)) {
              shape_index = i;
            }
          }
        }
      }
    } // for (int i=0; i<n; i++) {
    if (lat_index < 0 || lon_index < 0 || shape_index < 0) {
      throw new DisplayException("storm track data must include Latitude " +
               "and Longitude and a RealType mapped to Shape " +
               lat_index + " " + lon_index + " " + shape_index);
    }

    try {
      ntimes = storm_track.getLength();
      tuples = new Tuple[ntimes];
      lats = new float[ntimes];
      lons = new float[ntimes];
      old_lats = new float[ntimes];
      old_lons = new float[ntimes];
      time_set = storm_track.getDomainSet();
      for (int j=0; j<ntimes; j++) {
        tuples[j] = (Tuple) storm_track.getSample(j);
        Real[] reals = tuples[j].getRealComponents();
        lats[j] = (float) reals[lat_index].getValue();
        lons[j] = (float) reals[lon_index].getValue();
        old_lats[j] = lats[j];
        old_lons[j] = lons[j];
      }
    }
    catch (ClassCastException e) {
      throw new DisplayException("storm track bad MathType: " +
                     storm_track_type);
    }


    track_ref = new DataReferenceImpl("track_ref");
    track_ref.setData(storm_track);
    display.addReference(track_ref);
    which_time = -1;
    if (control == null) {
      track_refs = new DataReferenceImpl[ntimes];
      direct_manipulation_renderers = new DirectManipulationRendererJ3D[ntimes];
      track_monitors = new TrackMonitor[ntimes];
      for (int i=0; i<ntimes; i++) {
        track_refs[i] = new DataReferenceImpl("station_ref" + i);
        track_refs[i].setData(tuples[i]);
        direct_manipulation_renderers[i] = new DirectManipulationRendererJ3D();
        display.addReferences(direct_manipulation_renderers[i], track_refs[i]);
        track_monitors[i] = new TrackMonitor(track_refs[i], i);
        track_monitors[i].addReference(track_refs[i]);
      }
    }
    else {
      track_refs = new DataReferenceImpl[1];
      direct_manipulation_renderers = new DirectManipulationRendererJ3D[1];
      track_monitors = new TrackMonitor[1];
      track_refs[0] = new DataReferenceImpl("station_ref");
      track_refs[0].setData(tuples[0]);
      direct_manipulation_renderers[0] = new DirectManipulationRendererJ3D();
      display.addReferences(direct_manipulation_renderers[0], track_refs[0]);
      track_monitors[0] = new TrackMonitor(track_refs[0], 0);
      track_monitors[0].addReference(track_refs[0]);
    }

    if (control != null) control.setCurrent(0);
  }

  public void endManipulation()
         throws VisADException, RemoteException {
    for (int i=0; i<track_refs.length; i++) {
      display.removeReference(track_refs[i]);
    }
    display.addReference(track_ref);
  }

  private boolean first = true;

  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
    which_time = -1;
    if (direct_manipulation_renderers == null) return;
    if (direct_manipulation_renderers[0] == null) return;
    direct_manipulation_renderers[0].stop_direct();

    Set ts = control.getSet();
    if (ts == null) return;
    if (!time_set.equals(ts)) {
      throw new CollectiveBarbException("time Set changed");
    }

    int current = control.getCurrent();
    if (current < 0) return;
    which_time = current;

    track_refs[0].setData(tuples[current]);

    if (first) {
      first = false;
      display.removeReference(track_ref);
    }
  }

  class TrackMonitor extends CellImpl {
    DataReferenceImpl ref;
    int this_time;
  
    public TrackMonitor(DataReferenceImpl r, int t) {
      ref = r;
      this_time = t;
    }
  
    private final static float EPS = 0.01f;

    public void doAction() throws VisADException, RemoteException {
      int time_index = this_time;
      if (control != null) time_index = which_time;
      if (time_index < 0) return;

      Tuple storm = (Tuple) ref.getData();
      Real[] reals = storm.getRealComponents();
      float new_lat = (float) reals[lat_index].getValue();
      float new_lon = (float) reals[lon_index].getValue();
      // filter out barb changes due to other doAction calls
      if (visad.util.Util.isApproximatelyEqual(new_lat,
                 lats[time_index], EPS) &&
          visad.util.Util.isApproximatelyEqual(new_lon,
                 lons[time_index], EPS)) return;

      if (first) {
        first = false;
        display.removeReference(track_ref);
      }

      if (last_time != time_index) {
        last_time = time_index;
        for (int j=0; j<ntimes; j++) {
          old_lats[j] = lats[j];
          old_lons[j] = lons[j];
        }
      }

      float diff_lat = new_lat - old_lats[time_index];
      float diff_lon = new_lon - old_lons[time_index];

      for (int j=time_index; j<ntimes; j++) {

        double lat = old_lats[j] + diff_lat;
        double lon = old_lons[j] + diff_lon;

        Tuple old_storm = tuples[j];
        if (old_storm instanceof RealTuple) {
          reals = old_storm.getRealComponents();
          reals[lat_index] = reals[lat_index].cloneButValue(lat);
          reals[lon_index] = reals[lon_index].cloneButValue(lon);
          storm = new RealTuple((RealTupleType) old_storm.getType(), reals,
                           ((RealTuple) old_storm).getCoordinateSystem());
        }
        else { // old_storm instanceof Tuple
          int n = old_storm.getDimension();
          int k = 0;
          Data[] components = new Data[n];
          for (int c=0; c<n; c++) {
            components[c] = old_storm.getComponent(c);
            if (components[c] instanceof Real) {
              if (k == lat_index) {
                components[c] =
                  ((Real) components[c]).cloneButValue(lat);
              }
              if (k == lon_index) {
                components[c] =
                  ((Real) components[c]).cloneButValue(lon);
              }
              k++;
            }
            else { // (components[c] instanceof RealTuple)
              int m = ((RealTuple) components[c]).getDimension();
              if ((k <= lat_index && lat_index < k+m) ||
                  (k <= lon_index && lon_index < k+m)) {
                reals = ((RealTuple) components[c]).getRealComponents();
                if (k <= lat_index && lat_index < k+m) {
                  reals[lat_index - k] =
                    reals[lat_index - k].cloneButValue(lat);
                }
                if (k <= lon_index && lon_index < k+m) {
                  reals[lon_index - k] =
                    reals[lon_index - k].cloneButValue(lon);
                }
                components[c] =
                  new RealTuple((RealTupleType) components[c].getType(),
                                reals,
                       ((RealTuple) components[c]).getCoordinateSystem());
              }
              k += m;
            } // end if (components[c] instanceof RealTuple)
          } // end for (int c=0; c<n; c++)
          storm = new Tuple((TupleType) old_storm.getType(), components,
                           false);
        } // end if (old_storm instanceof Tuple)

        lats[j] = (float) lat;
        lons[j] = (float) lon;
        tuples[j] = storm;
        storm_track.setSample(j, storm);
        if (control == null) {
          track_refs[j].setData(tuples[j]);
        }
      } // end for (int j=time_index+1; j<ntimes; j++)
    }
  }

  private static final int NTIMES = 6;

  public static void main(String args[])
         throws VisADException, RemoteException {

    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType shape = new RealType("shape");

    RealType time = RealType.Time;
    double start = new DateTime(1999, 122, 57060).getValue();
    Set time_set = new Linear1DSet(time, start, start + 3000.0, NTIMES);

    RealTupleType tuple_type = null;
    tuple_type = new RealTupleType(lon, lat, shape);

    FunctionType track_type = new FunctionType(time, tuple_type);

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

    VisADLineArray circle = new VisADLineArray();
    int nv = 8;
    float size = 0.1f;
    circle.vertexCount = 2 * nv;
    float[] coordinates = new float[3 * circle.vertexCount];
    int m = 0;
    for (int i=0; i<nv; i++) {
      double b = 2.0 * Math.PI * i / nv;
      coordinates[m++] = size * ((float) Math.cos(b));
      coordinates[m++] = size * ((float) Math.sin(b));
      coordinates[m++] = 0.0f;
      double c = 2.0 * Math.PI * (i + 1) / nv;
      coordinates[m++] = size * ((float) Math.cos(c));
      coordinates[m++] = size * ((float) Math.sin(c));
      coordinates[m++] = 0.0f;
    }
    circle.coordinates = coordinates;

    ShapeControl shape_control;
    ScalarMap shape_map = new ScalarMap(shape, Display.Shape);
    display.addMap(shape_map);
    shape_control = (ShapeControl) shape_map.getControl();
    shape_control.setShapeSet(new Integer1DSet(6));
    shape_control.setShape(0, circle);

    ScalarMap amap = null;
    if (args.length > 0) {
      amap = new ScalarMap(time, Display.Animation);
      display.addMap(amap);
      AnimationControl acontrol = (AnimationControl) amap.getControl();
      acontrol.setStep(500);
    }

    FlatField ff = new FlatField(track_type, time_set);
    double[][] values = new double[3][NTIMES];
    for (int k=0; k<NTIMES; k++) {
      // each track record is a Tuple (lon, lat, shape)
      values[0][k] = 2.0 * k - 8.0;
      values[1][k] = 2.0 * k - 48.0;
      values[2][k] = 0.0;
    }
    ff.setSamples(values);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test FlexibleTrackManipulation");
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
    if (amap != null) panel.add(new AnimationWidget(amap));

    FlexibleTrackManipulation ftm =
      new FlexibleTrackManipulation(ff, display);

    JButton end = new JButton("end manip");
    end.addActionListener(new EndManip(ftm));
    end.setActionCommand("end");
    panel.add(end);

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);
  }
}

class EndManip implements ActionListener {
  FlexibleTrackManipulation ftm;

  EndManip(FlexibleTrackManipulation f) {
    ftm = f;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("end")) {
      try {
        ftm.endManipulation();
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
  }
}

