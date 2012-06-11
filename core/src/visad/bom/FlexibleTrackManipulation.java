//
// FlexibleTrackManipulation.java
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

package visad.bom;

import visad.*;
import visad.util.*;
import visad.java3d.*;

import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;

/**
   FlexibleTrackManipulation is the VisAD class for
   manipulation of flexible storm tracks (not straight lines)
*/
public class FlexibleTrackManipulation extends Object {

  private Object data_lock = new Object();

  private int ntimes = 0;
  private Tuple[] tuples;
  private boolean[] tuple_change;
  private int which_time = -1;

  private Set time_set = null;

  private float[] lats;
  private float[] lons;
  private float[] shapes;
  private float[] old_lats;
  private float[] old_lons;

  private DataReferenceImpl track_ref;
  private DataReferenceImpl[] track_refs;
  private DirectManipulationRendererJ3D[] direct_manipulation_renderers;
// abcd 23 Aug 2001
  private ConstantMap[][] constant_maps;
  private TrackMonitor[] track_monitors;

  private AnimationControl acontrol = null;
  private ShapeControl shape_control1 = null;
  private ShapeControl shape_control2 = null;
  private ProjectionControl pcontrol = null;

  private DisplayImplJ3D display;
  private FieldImpl storm_track;
  private FunctionType storm_track_type = null;

  private DataMonitor data_monitor = null;

  private int shape_index;
  private int lat_index;
  private int lon_index;
  private float[] shapeColour;	// In RGB order

  private int last_time = -1;

  /**
   * constructor
   * Uses default size of 0.1 and default cyclone symbol geometry
   */
  public FlexibleTrackManipulation(DataReferenceImpl tr, DisplayImplJ3D d,
                                   ScalarMap shape_map1, ScalarMap shape_map2,
                                   boolean need_monitor)
         throws VisADException, RemoteException
  {
    final float size = 0.1f;

    // construct symbols
    int nv = 16;
    VisADGeometryArray[][] ga = makeStormShapes(nv, size);
    shapeColour = new float[] {1.0f, 1.0f, 1.0f};

    init(tr, d, shape_map1, shape_map2, need_monitor, size, ga);
  }


  /**
   * Constructor - Use default cyclone shape geometry
   */
  public FlexibleTrackManipulation(DataReferenceImpl tr, DisplayImplJ3D d,
                                   ScalarMap shape_map1, ScalarMap shape_map2,
                                   boolean need_monitor, float size)
         throws VisADException, RemoteException
  {
    // construct symbols
    int nv = 16;
    VisADGeometryArray[][] ga = makeStormShapes(nv, size);
    shapeColour = new float[] {1.0f, 1.0f, 1.0f};

    init(tr, d, shape_map1, shape_map2, need_monitor, size, ga);
  }


  /**
   * Construct the FTM stuff
   *
   * @author 30/07/2001 modified by abcd
   *
   * @param tr A DataReferenceImpl, The visad data for the cyclone track
   *	tr.getData() should have MathType: *    (Time -> tuple))
   *	where tuple is flat [e.g., (Latitude, Longitude, shape_index)]
   *	and must include RealTypes Latitude and Longitude plus
   *	a RealType mapped to Shape in the DisplayImpl d;
   *	Time may or may not be mapped to Animation
   *
   * @param d A DisplayImplJ3D, - The Display to add the FTM to
   *
   * @param shape_map1 First ScalarMap of RealTypes
   *	in tr.getData()
   *
   * @param shape_map2 Second ScalarMap of RealTypes
   *	in tr.getData()
   *
   * @param need_monitor - Need to monitor tr to maintain external
   *	changes to it.
   *
   * @param size - storm symbol size
   *
   * @param ga A VisADGeometryArray[][] - two arrays of geometry objects
   *	which represent the cyclone symbols in the order:
   *	none, low, depresion1, depresion2, s-cyclone1, s-cyclone2,
   *	n-cyclone1, n-cyclone2.
   *	There are two arrays so you can combine shapes (ie: circle & lines)
   *    The geometry can be built with makeStormShapes() or an application
   *	defined method.
   *
   * @param shapeColour - colour of symbols
   *
   * TODO: Have a setCycloneGeometry(VisADGeometryArray[][] ga) method
   */
  public FlexibleTrackManipulation(DataReferenceImpl tr, DisplayImplJ3D d,
                                   ScalarMap shape_map1, ScalarMap shape_map2,
                                   boolean need_monitor, float size,
                                   VisADGeometryArray[][] ga,
                                   float shapeColour[])
         throws VisADException, RemoteException
  {
	this.shapeColour = shapeColour;
	init(tr, d, shape_map1, shape_map2, need_monitor, size, ga);
  }


  /**
   * Do the work to construct a FTM
   */
  private void init(DataReferenceImpl tr, DisplayImplJ3D d,
                                   ScalarMap shape_map1, ScalarMap shape_map2,
                                   boolean need_monitor, float size,
                                   VisADGeometryArray[][] ga)
         throws VisADException, RemoteException
  {
    track_ref = tr;
    storm_track = (FlatField) track_ref.getData();
    display = d;

    pcontrol = display.getProjectionControl();
    ProjectionControlListener pcl = new ProjectionControlListener();
    pcontrol.addControlListener(pcl);

    acontrol = (AnimationControl) display.getControl(AnimationControl.class);
    // use a ControlListener on Display.Animation to fake
    // animation of the track
    if (acontrol != null) {
      AnimationControlListener acl = new AnimationControlListener();
      acontrol.addControlListener(acl);
    }

    storm_track_type = (FunctionType) storm_track.getType();
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
        Enumeration en = scalar_map_vector.elements();
        while (en.hasMoreElements()) {
          ScalarMap map = (ScalarMap) en.nextElement();
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

    setupData(storm_track, true, true);

    // Store geometry array in shapes
    shape_control1 = (ShapeControl) shape_map1.getControl();
    shape_control1.setShapeSet(new Integer1DSet(8));
    shape_control1.setShapes(ga[0]);

    shape_control2 = (ShapeControl) shape_map2.getControl();
    shape_control2.setShapeSet(new Integer1DSet(8));
    shape_control2.setShapes(ga[1]);

    which_time = -1;

    if (need_monitor) {
      data_monitor = new DataMonitor();
      data_monitor.addReference(track_ref);
    }

    if (acontrol != null) {
      acontrol.setCurrent(0);
      display.addReference(track_ref);
    }
  }


  /**
   * Class to keep cyclone data in sync with external changes to it
   */
  class DataMonitor extends CellImpl {
    public void doAction() throws VisADException, RemoteException {
      synchronized (data_lock) {
        FieldImpl st = (FieldImpl) track_ref.getData();
        boolean length_change = false;
        boolean value_change = false;
        if (!storm_track_type.equals(st.getType())) {
          throw new VisADException("storm_track MathType may not change");
        }
        if (ntimes != st.getLength()) length_change = true;
        if (st.getLength() >= ntimes) {
          for (int j=0; j<ntimes; j++) {
            Real[] reals = ((Tuple) st.getSample(j)).getRealComponents();
            if (!visad.util.Util.isApproximatelyEqual(lats[j],
                           (float) reals[lat_index].getValue()) ||
                !visad.util.Util.isApproximatelyEqual(lons[j],
                           (float) reals[lon_index].getValue()) ||
                !visad.util.Util.isApproximatelyEqual(shapes[j],
                           (float) reals[shape_index].getValue())) {
              value_change = true;
              break;
            }
          }
        }
        else {
          value_change = true;
        }
        storm_track = st;
        if (length_change || value_change) {
          setupData(storm_track, length_change, value_change);
        }
        else {
          setupTuples(storm_track);
        }
      } // end synchronized (data_lock)
    }
  }


  private void setupData(FieldImpl storm_track, boolean length_change,
                         boolean value_change)
          throws VisADException, RemoteException {
    synchronized (data_lock) {
      if (storm_track_type == null) {
        storm_track_type = (FunctionType) storm_track.getType();
      }
      else {
        if (!storm_track_type.equals(storm_track.getType())) {
          throw new DisplayException("storm track MathType changed");
        }
      }

      boolean special = length_change && !value_change;
  
      if (track_refs != null && !special) {
        for (int i=0; i<track_refs.length; i++) {
          display.removeReference(track_refs[i]);
          track_monitors[i].removeReference(track_refs[i]);
          track_monitors[i].stop();
        }
      }
  
      int old_ntimes = ntimes;

      try {
        ntimes = storm_track.getLength();
        tuples = new Tuple[ntimes];
        tuple_change = new boolean[ntimes];
        lats = new float[ntimes];
        lons = new float[ntimes];
        old_lats = new float[ntimes];
        old_lons = new float[ntimes];
        shapes = new float[ntimes];
        time_set = storm_track.getDomainSet();
        for (int j=0; j<ntimes; j++) {
          tuples[j] = (Tuple) storm_track.getSample(j);
          tuple_change[j] = false;
          Real[] reals = tuples[j].getRealComponents();
          lats[j] = (float) reals[lat_index].getValue();
          lons[j] = (float) reals[lon_index].getValue();
          old_lats[j] = lats[j];
          old_lons[j] = lons[j];
          shapes[j] = (float) reals[shape_index].getValue();
        }
      }
      catch (ClassCastException e) {
        throw new DisplayException("storm track bad MathType: " +
                       storm_track_type);
      }
  
      if (acontrol == null) {

        DataReferenceImpl[] old_track_refs = track_refs;
        DirectManipulationRendererJ3D[] old_direct_manipulation_renderers =
          direct_manipulation_renderers;
        TrackMonitor[] old_track_monitors = track_monitors;
        ConstantMap[][] old_constant_maps = constant_maps;

        track_refs = new DataReferenceImpl[ntimes];
        direct_manipulation_renderers = new DirectManipulationRendererJ3D[ntimes];
        track_monitors = new TrackMonitor[ntimes];
        constant_maps = new ConstantMap[ntimes][];
        for (int i=0; i<ntimes; i++) {
          if (!special || i >= old_ntimes) {
            track_refs[i] = new DataReferenceImpl("station_ref" + i);
            track_refs[i].setData(tuples[i]);
            direct_manipulation_renderers[i] =
              new FTMDirectManipulationRendererJ3D(this);
            // abcd 23 August 2001
            ConstantMap[] foo = {
              new ConstantMap(shapeColour[0], Display.Red),
              new ConstantMap(shapeColour[1], Display.Green),
              new ConstantMap(shapeColour[2], Display.Blue)
            };
            constant_maps[i] = foo;
  
            display.addReferences(direct_manipulation_renderers[i],
  		track_refs[i], constant_maps[i]);
            track_monitors[i] = new TrackMonitor(track_refs[i], i);
            track_monitors[i].addReference(track_refs[i]);
          }
          else { // special && i < old_ntimes
            track_refs[i] = old_track_refs[i];
            direct_manipulation_renderers[i] = old_direct_manipulation_renderers[i];
            constant_maps[i] = old_constant_maps[i];
            track_monitors[i] = old_track_monitors[i];
          }
        }
      }
      else {
        if (!special) {
          track_refs = new DataReferenceImpl[1];
          direct_manipulation_renderers = new DirectManipulationRendererJ3D[1];
          track_monitors = new TrackMonitor[1];
          constant_maps = new ConstantMap[1][];
          track_refs[0] = new DataReferenceImpl("station_ref");
          track_refs[0].setData(tuples[0]);
          direct_manipulation_renderers[0] =
            new FTMDirectManipulationRendererJ3D(this);
          // abcd 23 August 2001
          constant_maps[0] = new ConstantMap[] {
            new ConstantMap(shapeColour[0], Display.Red),
            new ConstantMap(shapeColour[1], Display.Green),
            new ConstantMap(shapeColour[2], Display.Blue)
          };
          display.addReferences(direct_manipulation_renderers[0],
  		track_refs[0], constant_maps[0]);
          track_monitors[0] = new TrackMonitor(track_refs[0], 0);
          track_monitors[0].addReference(track_refs[0]);
        }
      }
    } // end synchronized (data_lock)
  }

  private void setupTuples(FieldImpl storm_track)
          throws VisADException, RemoteException {
    synchronized (data_lock) {
      for (int j=0; j<ntimes; j++) {
        tuples[j] = (Tuple) storm_track.getSample(j);
      }
    }
  }


  /**
   * Create the geometry array which contains the shapes for the
   * cyclone symbols.
   *
   * @param nv - The number of vertices?
   *
   * @param size - The size of all the symbols, apparently relative
   *	to the size of the window
   */
  
  public static VisADGeometryArray[][] makeStormShapes(int nv, float size)
         throws VisADException {
    VisADLineArray circle = new VisADLineArray();
    circle.vertexCount = 2 * nv;
    float[] coordinates = new float[3 * circle.vertexCount];
    int m = 0;
    for (int i=0; i<nv; i++) {
      double b = 2.0 * Math.PI * i / nv;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(b));
      coordinates[m++] = 0.5f * size * ((float) Math.sin(b));
      coordinates[m++] = 0.0f;
      double c = 2.0 * Math.PI * (i + 1) / nv;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(c));
      coordinates[m++] = 0.5f * size * ((float) Math.sin(c));
      coordinates[m++] = 0.0f;
    }
    circle.coordinates = coordinates;

    VisADTriangleArray filled_circle = new VisADTriangleArray();
    filled_circle.vertexCount = 3 * nv;
    coordinates = new float[3 * filled_circle.vertexCount];
    m = 0;
    for (int i=0; i<nv; i++) {
      coordinates[m++] = 0.0f;
      coordinates[m++] = 0.0f;
      coordinates[m++] = 0.0f;
      double b = 2.0 * Math.PI * i / nv;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(b));
      coordinates[m++] = 0.5f * size * ((float) Math.sin(b));
      coordinates[m++] = 0.0f;
      double c = 2.0 * Math.PI * (i + 1) / nv;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(c));
      coordinates[m++] = 0.5f * size * ((float) Math.sin(c));
      coordinates[m++] = 0.0f;
    }
    filled_circle.coordinates = coordinates;
    float[] normals = new float[3 * filled_circle.vertexCount];
    m = 0;
    for (int i=0; i<3*nv; i++) {
      normals[m++] = 0.0f;
      normals[m++] = 0.0f;
      normals[m++] = 1.0f;
    }
    filled_circle.normals = normals;

    // L symbol for Tropical Low
    // 30/07/2001 abcd - halved the size
    VisADLineArray ell = new VisADLineArray();
    ell.vertexCount = 2 * 4;
    ell.coordinates = new float[] {
      -0.25f * size, 0.5f * size, 0.0f,   0.0f, 0.5f * size, 0.0f,
      -0.12f * size, 0.5f * size, 0.0f,   -0.12f * size, -0.5f * size, 0.0f,
      -0.12f * size, -0.5f * size, 0.0f,  0.5f * size, -0.5f * size, 0.0f,
      0.5f * size, -0.5f * size, 0.0f,    0.5f * size, -0.37f * size, 0.0f
    };

    VisADLineArray south = new VisADLineArray();
    south.vertexCount = 2 * nv;
    coordinates = new float[3 * south.vertexCount];
    m = 0;
    for (int i=0; i<nv/2; i++) {
      double b = Math.PI * i / nv;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(b));
      coordinates[m++] = size * ((float) Math.sin(b));
      coordinates[m++] = 0.0f;
      double c = Math.PI * (i + 1) / nv;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(c));
      coordinates[m++] = size * ((float) Math.sin(c));
      coordinates[m++] = 0.0f;
      coordinates[m++] = -0.5f * size * ((float) Math.cos(b));
      coordinates[m++] = -size * ((float) Math.sin(b));
      coordinates[m++] = 0.0f;
      coordinates[m++] = -0.5f * size * ((float) Math.cos(c));
      coordinates[m++] = -size * ((float) Math.sin(c));
      coordinates[m++] = 0.0f;
    }
    south.coordinates = coordinates;

    VisADLineArray north = new VisADLineArray();
    north.vertexCount = 2 * nv;
    coordinates = new float[3 * north.vertexCount];
    m = 0;
    for (int i=0; i<nv/2; i++) {
      double b = Math.PI * i / nv;
      coordinates[m++] = -0.5f * size * ((float) Math.cos(b));
      coordinates[m++] = size * ((float) Math.sin(b));
      coordinates[m++] = 0.0f;
      double c = Math.PI * (i + 1) / nv;
      coordinates[m++] = -0.5f * size * ((float) Math.cos(c));
      coordinates[m++] = size * ((float) Math.sin(c));
      coordinates[m++] = 0.0f;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(b));
      coordinates[m++] = -size * ((float) Math.sin(b));
      coordinates[m++] = 0.0f;
      coordinates[m++] = 0.5f * size * ((float) Math.cos(c));
      coordinates[m++] = -size * ((float) Math.sin(c));
      coordinates[m++] = 0.0f;
    }
    north.coordinates = coordinates;

    VisADLineArray south_circle =
      VisADLineArray.merge(new VisADLineArray[] {circle, south});
    VisADLineArray north_circle =
      VisADLineArray.merge(new VisADLineArray[] {circle, north});

    VisADGeometryArray[][] ga =
      {{null, ell, circle, null, south_circle, south, north_circle, north},
       {null, null, null, filled_circle, null, filled_circle, null, filled_circle}};
    return ga;
  }

  public void endManipulation()
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      for (int i=0; i<track_refs.length; i++) {
        display.removeReference(track_refs[i]);
      }
      display.addReference(track_ref);
    } // end synchronized (data_lock)
  }

  private boolean pfirst = true;

  class ProjectionControlListener implements ControlListener {
    private double base_scale = 0.65;
    private float last_cscale = 1.0f;

    public void controlChanged(ControlEvent e)
           throws VisADException, RemoteException {
      double[] matrix = pcontrol.getMatrix();
      double[] rot = new double[3];
      double[] scale = new double[1];
      double[] trans = new double[3];
      MouseBehaviorJ3D.unmake_matrix(rot, scale, trans, matrix);

/* WLH 10 Sept 2001
      if (pfirst) {
        pfirst = false;
        base_scale = scale[0];
// System.out.println("base_scale = " + base_scale);
        last_cscale = 1.0f;
      }
      else {
*/
        float cscale = (float) (base_scale / scale[0]);
        float ratio = cscale / last_cscale;
        if (ratio < 0.95f || 1.05f < ratio) {
          last_cscale = cscale;
          if (shape_control1 != null) shape_control1.setScale(cscale);
          if (shape_control2 != null) shape_control2.setScale(cscale);
        }
/* WLH 10 Sept 2001
      }
*/
    }
  }

  private boolean afirst = true;

  class AnimationControlListener implements ControlListener {
    public void controlChanged(ControlEvent e)
           throws VisADException, RemoteException {
      synchronized (data_lock) {
        which_time = -1;
        if (direct_manipulation_renderers == null) return;
        if (direct_manipulation_renderers[0] == null) return;
        direct_manipulation_renderers[0].stop_direct();
    
        Set ts = acontrol.getSet();
        if (ts == null) return;
        if (!time_set.equals(ts)) {
          throw new CollectiveBarbException("time Set changed");
        }
    
        int current = acontrol.getCurrent();
        if (current < 0) return;
        which_time = current;

        track_refs[0].setData(tuples[current]);
    
        if (afirst) {
          afirst = false;
          if (acontrol != null) display.removeReference(track_ref);
        }
      } // end synchronized (data_lock)
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
      synchronized (data_lock) {
        int time_index = this_time;
        if (acontrol != null) time_index = which_time;
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
  
        if (afirst) {
          afirst = false;
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
  
        int mouseModifiers =
          direct_manipulation_renderers[this_time].getLastMouseModifiers();
        int mctrl = mouseModifiers & InputEvent.CTRL_MASK;
        int high_time = (mctrl != 0) ? ntimes : time_index + 1;
  
        for (int j=time_index; j<high_time; j++) {
  
          double lat = old_lats[j] + diff_lat;
          double lon = old_lons[j] + diff_lon;
          int old_shape = (int) (shapes[j] + 0.01);
          double shape = old_shape;
          if (4 <= old_shape && old_shape < 6) {
            if (lat >= 0.0) shape = old_shape + 2;
          }
          else if (6 <= old_shape && old_shape < 8) {
            if (lat < 0.0) shape = old_shape - 2;
          }
  
          Tuple old_storm = tuples[j];
          if (old_storm instanceof RealTuple) {
            reals = old_storm.getRealComponents();
            reals[lat_index] = reals[lat_index].cloneButValue(lat);
            reals[lon_index] = reals[lon_index].cloneButValue(lon);
            reals[shape_index] = reals[shape_index].cloneButValue(shape);
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
                if (k == shape_index) {
                  components[c] =
                    ((Real) components[c]).cloneButValue(shape);
                }
                k++;
              }
              else { // (components[c] instanceof RealTuple)
                int m = ((RealTuple) components[c]).getDimension();
                if ((k <= lat_index && lat_index < k+m) ||
                    (k <= lon_index && lon_index < k+m) ||
                    (k <= shape_index && shape_index < k+m)) {
                  reals = ((RealTuple) components[c]).getRealComponents();
                  if (k <= lat_index && lat_index < k+m) {
                    reals[lat_index - k] =
                      reals[lat_index - k].cloneButValue(lat);
                  }
                  if (k <= lon_index && lon_index < k+m) {
                    reals[lon_index - k] =
                      reals[lon_index - k].cloneButValue(lon);
                  }
                  if (k <= shape_index && shape_index < k+m) {
                    reals[shape_index - k] =
                      reals[shape_index - k].cloneButValue(shape);
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
          shapes[j] = (float) shape;
          tuples[j] = storm;

// release
          // storm_track.setSample(j, tuples[j]);
          if (direct_on) {
            tuple_change[j] = true;
          }
          else {
            storm_track.setSample(j, tuples[j]);
          }

          if (acontrol == null) {
            track_refs[j].setData(tuples[j]);
          }
        } // end for (int j=time_index+1; j<ntimes; j++)
      } // end synchronized (data_lock)
    }
  }

  /**
   * Get access to the renderers.  Intended for the app to be
   * able to toggle() this data display.
   *
   * abcd 27 April 2001
   */
  public DataRenderer[] getManipulationRenderers() {
    return direct_manipulation_renderers;
  }

  private boolean direct_on = false;

  public void release() {
    synchronized (data_lock) {
      try {
        for (int i=0; i<ntimes; i++) {
          if (tuple_change[i]) {
            tuple_change[i] = false;
            storm_track.setSample(i, tuples[i]);
          }
        }
      }
      catch (VisADException e) {
      }
      catch (RemoteException e) {
      }
      direct_on = false;
    }
  }

  public void start() {
    synchronized (data_lock) {
      direct_on = true;
    }
  }

  class FTMDirectManipulationRendererJ3D extends DirectManipulationRendererJ3D {
    FlexibleTrackManipulation ftm;

    FTMDirectManipulationRendererJ3D(FlexibleTrackManipulation f) {
      super();
      ftm = f;
    }

    /** mouse button released, ending direct manipulation */
    public void release_direct() {
      ftm.release();
    }

    public void drag_direct(VisADRay ray, boolean first,
                                         int mouseModifiers) {
      if (first) {
        ftm.start();
      }
      super.drag_direct(ray, first, mouseModifiers);
    }

  }

  private static final int NTIMES = 8;

  public static void main(String args[])
         throws VisADException, RemoteException {

    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;
    RealType shape = RealType.getRealType("shape");

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
    latmap.setRange(-10.0, 10.0);

    ScalarMap shape_map1 = new ScalarMap(shape, Display.Shape);
    display.addMap(shape_map1);

    ScalarMap shape_map2 = new ScalarMap(shape, Display.Shape);
    display.addMap(shape_map2);

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
      values[1][k] = 2.0 * k - 8.0;
      int s =  k % 8;
      if (4 <= s && s < 6) {
        if (values[1][k] >= 0.0) s += 2;
      }
      else if (6 <= s && s < 8) {
        if (values[1][k] < 0.0) s -= 2;
      }
      values[2][k] = s;
    }
    ff.setSamples(values);
    DataReferenceImpl track_ref = new DataReferenceImpl("track_ref");
    track_ref.setData(ff);

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
      new FlexibleTrackManipulation(track_ref, display, shape_map1, shape_map2,
                                    true, 0.05f);

    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
    button_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    button_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    EndManipFTM emf = new EndManipFTM(ftm, track_ref);
    JButton end = new JButton("end manip");
    end.addActionListener(emf);
    end.setActionCommand("end");
    button_panel.add(end);
    JButton add = new JButton("add to track");
    add.addActionListener(emf);
    add.setActionCommand("add");
    button_panel.add(add);
    panel.add(button_panel);

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);
  }
}

class EndManipFTM implements ActionListener {
  FlexibleTrackManipulation ftm;
  DataReferenceImpl track_ref;

  EndManipFTM(FlexibleTrackManipulation f, DataReferenceImpl tr) {
    ftm = f;
    track_ref = tr;
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
    else if (cmd.equals("add")) {
      try {
        FlatField ff = (FlatField) track_ref.getData();
        int ntimes = ff.getLength();
        Linear1DSet time_set = (Linear1DSet) ff.getDomainSet();
        Linear1DSet new_time_set =
          new Linear1DSet(RealType.Time, time_set.getFirst(),
                          time_set.getLast() + time_set.getStep(),
                          ntimes + 1);
        double[][] values = ff.getValues();
        double[][] new_values = new double[3][ntimes + 1];
        System.arraycopy(values[0], 0, new_values[0], 0, ntimes);
        System.arraycopy(values[1], 0, new_values[1], 0, ntimes);
        System.arraycopy(values[2], 0, new_values[2], 0, ntimes);
        int k = ntimes;
        new_values[0][k] = 2.0 * k - 8.0;
        new_values[1][k] = 2.0 * k - 8.0;
        int s =  k % 8;
        if (4 <= s && s < 6) {
          if (new_values[1][k] >= 0.0) s += 2;
        }
        else if (6 <= s && s < 8) {
          if (new_values[1][k] < 0.0) s -= 2;
        }
        new_values[2][k] = s;
        FlatField new_ff = new FlatField((FunctionType) ff.getType(), new_time_set);
        new_ff.setSamples(new_values);
        track_ref.setData(new_ff);
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
    }
  }
}

