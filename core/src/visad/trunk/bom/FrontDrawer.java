
//
// FrontDrawer.java
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

/*
find front points inside boundary
highlight needs sharp boundary, which means different graphics
arrays, but with matching shapes

do shape variations as periodic functions for front and back
profiles of shape; bend these along low-pass filter of user
drawn curve and draw custom Shape; local changes as distinct
Shapes
*/

/**
   FrontDrawer is the VisAD class for manipulation of fronts
*/
public class FrontDrawer extends Object {

  private static boolean debug = true;

  private Object data_lock = new Object();

  private DataReferenceImpl front_ref;
  private DefaultRendererJ3D front_renderer;
  private DataReferenceImpl curve_ref;
  private FrontManipulationRendererJ3D front_manipulation_renderer;
  private CurveMonitor curve_monitor;

  private ReleaseCell release_cell;
  private DataReferenceImpl release_ref;

  private ProjectionControl pcontrol = null;

  private DisplayImplJ3D display;
  private ScalarMap lat_map = null;
  private ScalarMap lon_map = null;

  private static Object type_lock = new Object();

  private UnionSet init_curve = null; // manifold dimension = 1
  private static SetType curve_type = null; // Set(Latitude, Longitude)
  private int lat_index = 0;
  private int lon_index = 1;

  private FieldImpl front = null; //
  // (front_index -> ((Latitude, Longitude) -> (front_red, front_green, front_blue)))
  private static FunctionType front_type = null;
  private static FunctionType front_inner = null;
  private static RealType front_index = null;
  private static RealType front_red = null;
  private static RealType front_green = null;
  private static RealType front_blue = null;

  // shapes for first segment of front
  private int nfshapes = -1;
  private float[][][] first_shapes = null;
  private int[][][] first_tris = null;
  private float[] first_red = null;
  private float[] first_green = null;
  private float[] first_blue = null;

  // shapes for repeating segments of front, after first
  private int nrshapes = -1;
  private float[][][] repeat_shapes = null;
  private int[][][] repeat_tris = null;
  private float[] repeat_red = null;
  private float[] repeat_green = null;
  private float[] repeat_blue = null;

  // length of each repeating segment in graphics coordinates
  private float segment_length;

  // number of intervals in curve for each segment
  private int profile_length = -1;

  // size of filter window for smoothing curve
  private int filter_window = 1;

  public static final int COLD_FRONT = 0;
  public static final int WARM_FRONT = 1;
  public static final int OCCLUDED_FRONT = 2;
  public static final int STATIONARY_FRONT = 3;
  public static final int CONVERGENCE = 4;
  public static final int FRONTOGENESIS = 5;
  public static final int FRONTOLYSIS = 6;
  public static final int UPPER_COLD_FRONT = 7;
  public static final int UPPER_WARM_FRONT = 8;
  public static final int TROUGH = 9;
  public static final int RIDGE = 10;
  public static final int MOISTURE = 11;
  public static final int LOW_LEVEL_JET = 12;
  public static final int UPPER_LEVEL_JET = 13;
  public static final int DRY_LINE = 14;
  public static final int TOTAL_TOTALS = 15;
  public static final int LIFTED_INDEX = 16;
  public static final int ISOTHERMS = 17;
  public static final int THICKNESS_RIDGE = 18;
  public static final int LOWER_THERMAL_TROUGH = 19;
  public static final int UPPER_THERMAL_TROUGH = 20;

  private static final float[] segmentarray = {
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f
  };

  private static final float[][][][] rshapesarray = {
    // COLD_FRONT
    {{{0.0f, 0.025f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.025f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.04f, 0.01f}}},
    // WARM_FRONT
    {{{0.0f, 0.035f, 0.07f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.07f, 0.0525f, 0.035f, 0.0175f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.03f, 0.04f, 0.03f, 0.01f}}},
    // OCCLUDED_FRONT
    {{{0.0f, 0.025f, 0.05f, 0.07f, 0.105f, 0.14f, 0.17f, 0.2f,
       0.2f, 0.17f, 0.14f, 0.1225f, 0.105f, 0.0875f, 0.07f, 0.05f, 0.025f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.03f, 0.04f, 0.03f, 0.01f, 0.01f, 0.04f, 0.01f}}},
    // STATIONARY_FRONT
    {{{0.0f, 0.025f, 0.05f, 0.07f, 0.0875f, 0.105f, 0.1225f, 0.14f, 0.17f, 0.2f,
       0.2f, 0.17f, 0.14f, 0.105f, 0.07f, 0.05f, 0.025f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, -0.02f, -0.03f, -0.02f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.04f, 0.01f}}},
    // CONVERGENCE
    {{{0.0f, 0.03f, 0.035f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.11f, 0.135f, 0.13f, 0.1f, 0.05f, 0.0f},
      {0.01f, 0.04f, 0.035f, 0.01f, 0.01f, 0.01f, 0.01f, 0.01f,
       0.0f, 0.0f, 0.0f, -0.025f, -0.03f, 0.0f, 0.0f, 0.0f}}},

  };

  private static final float[][] rredarray = {
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f}
  };

  private static final float[][] rgreenarray = {
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f}
  };

  private static final float[][] rbluearray = {
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f}
  };

  private static final float[][][][] fshapesarray = {
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null
  };

  private static final float[][] fredarray = {
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null
  };

  private static final float[][] fgreenarray = {
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null
  };
       
  private static final float[][] fbluearray = {
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null
  };

  public FrontDrawer(DataReferenceImpl cr, DisplayImplJ3D d, int fw,
                     int front_type)
         throws VisADException, RemoteException {
    this(cr, d, fw, segmentarray[front_type],
         fshapesarray[front_type], fredarray[front_type],
         fgreenarray[front_type], fbluearray[front_type],
         rshapesarray[front_type], rredarray[front_type],
         rgreenarray[front_type], rbluearray[front_type]);
  }


  /**
     cr should be null or cr.getData() should have MathType:
       Set(RealType.Latitude, RealType.Longitude)
     fshapes is dimensioned [nfshapes][2][points_per_shape]
     fred, fgreen and fblue are dimensioned [nfshapes]
     rshapes is dimensioned [nrshapes][2][points_per_shape]
     rred, rgreen and rblue are dimensioned [nrshapes]
     segment is length in graphics coordinates of entire profile
     fshapes[*][0][*] and rshapes[*][0][*] generally in range 0.0f to segment
     fw is the filter window size for smoothing the curve
  */
  public FrontDrawer(DataReferenceImpl cr, DisplayImplJ3D d,
                     int fw, float segment,
                     float[][][] fshapes,
                     float[] fred, float[] fgreen, float[] fblue,
                     float[][][] rshapes,
                     float[] rred, float[] rgreen, float[] rblue)
         throws VisADException, RemoteException {
    try {
      initColormaps(d);
    }
    catch (VisADException e) {
      // if (debug) System.out.println("caught " + e.toString());
    }

    if (cr == null) {
      curve_ref = new DataReferenceImpl("curve_ref");
    }
    else {
      curve_ref = cr;
    }
    Gridded2DSet set =
      new Gridded2DSet(curve_type, new float[][] {{0.0f}, {0.0f}}, 1); // ??
    init_curve = new UnionSet(curve_type, new Gridded2DSet[] {set});

    Data data = curve_ref.getData();
    Gridded2DSet curve_set = null;
    if (data != null && data instanceof UnionSet) {
      SampledSet[] sets = ((UnionSet) data).getSets();
      if (sets[0] instanceof Gridded2DSet) {
        curve_set = (Gridded2DSet) sets[0];
      }
    }
    if (curve_set == null) {
      curve_ref.setData(init_curve);
    }
    else {
      SetType st = (SetType) curve_set.getType();
      if (!st.equals(curve_type)) {
        SetType rft =
          new SetType(new RealTupleType(RealType.Longitude, RealType.Latitude));
        if (!st.equals(rft)) {
          throw new SetException("cr data bad MathType");
        }
        lat_index = 1;
        lon_index = 0;
      }
    }

    display = d;
    filter_window = fw;
    segment_length = segment;

    if (rshapes == null) {
      throw new VisADException("bad rshapes");
    }
    nrshapes = rshapes.length;
    for (int i=0; i<nrshapes; i++) {
      if (rshapes[i] == null || rshapes[i].length != 2 ||
          rshapes[i][0] == null || rshapes[i][1] == null ||
          rshapes[i][0].length != rshapes[i][1].length) {
        throw new VisADException("bad rshapes[" + i + "]");
      }
    }
    if (rred == null || rred.length != nrshapes ||
        rgreen == null || rgreen.length != nrshapes || 
        rblue == null || rblue.length != nrshapes) {
      throw new VisADException("bad fcolors");
    }
    repeat_tris = new int[nrshapes][][];
    for (int i=0; i<nrshapes; i++) {
      repeat_tris[i] = DelaunayCustom.fill(rshapes[i]);
    }
    repeat_shapes = new float[nrshapes][2][];
    int rlen = 0;
    for (int i=0; i<nrshapes; i++) {
      int n = rshapes[i][0].length;
      rlen += n;
      repeat_shapes[i][0] = new float[n];
      repeat_shapes[i][1] = new float[n];
      System.arraycopy(rshapes[i][0], 0, repeat_shapes[i][0], 0, n);
      System.arraycopy(rshapes[i][1], 0, repeat_shapes[i][1], 0, n);
    }
    profile_length = rlen;
    repeat_red = new float[nrshapes];
    repeat_green = new float[nrshapes];
    repeat_blue = new float[nrshapes];
    System.arraycopy(rred, 0, repeat_red, 0, nrshapes);
    System.arraycopy(rgreen, 0, repeat_green, 0, nrshapes);
    System.arraycopy(rblue, 0, repeat_blue, 0, nrshapes);

    if (fshapes == null) {
      // if no different first shapes, just use repeat shapes
      nfshapes = nrshapes;
      first_tris = repeat_tris;
      first_shapes = repeat_shapes;
      first_red = repeat_red;
      first_green = repeat_green;
      first_blue = repeat_blue;
    }
    else {
      nfshapes = fshapes.length;
      for (int i=0; i<nfshapes; i++) {
        if (fshapes[i] == null || fshapes[i].length != 2 ||
            fshapes[i][0] == null || fshapes[i][1] == null ||
            fshapes[i][0].length != fshapes[i][1].length) {
          throw new VisADException("bad fshapes[" + i + "]");
        }
      }
      if (fred == null || fred.length != nfshapes ||
          fgreen == null || fgreen.length != nfshapes || 
          fblue == null || fblue.length != nfshapes) {
        throw new VisADException("bad fcolors");
      }
      first_tris = new int[nfshapes][][];
      for (int i=0; i<nfshapes; i++) {
        first_tris[i] = DelaunayCustom.fill(fshapes[i]);
      }
      first_shapes = new float[nfshapes][2][];
      int flen = 0;
      for (int i=0; i<nfshapes; i++) {
        int n = fshapes[i][0].length;
        flen += n;
        first_shapes[i][0] = new float[n];
        first_shapes[i][1] = new float[n];
        System.arraycopy(fshapes[i][0], 0, first_shapes[i][0], 0, n);
        System.arraycopy(fshapes[i][1], 0, first_shapes[i][1], 0, n);
      }
      if (profile_length < flen) profile_length = flen;
      first_red = new float[nfshapes];
      first_green = new float[nfshapes];
      first_blue = new float[nfshapes];
      System.arraycopy(fred, 0, first_red, 0, nfshapes);
      System.arraycopy(fgreen, 0, first_green, 0, nfshapes);
      System.arraycopy(fblue, 0, first_blue, 0, nfshapes);
    }
    if (profile_length < 5) profile_length = 5;

    pcontrol = display.getProjectionControl();
    ProjectionControlListener pcl = new ProjectionControlListener();
    pcontrol.addControlListener(pcl);

    // find spatial maps for Latitude and Longitude
    lat_map = null;
    lon_map = null;
    Vector scalar_map_vector = display.getMapVector();
    Enumeration enum = scalar_map_vector.elements();
    while (enum.hasMoreElements()) {
      ScalarMap map = (ScalarMap) enum.nextElement();
      DisplayRealType real = map.getDisplayScalar();
      DisplayTupleType tuple = real.getTuple();
      if (tuple != null &&
          (tuple.equals(Display.DisplaySpatialCartesianTuple) ||
           (tuple.getCoordinateSystem() != null &&
            tuple.getCoordinateSystem().getReference().equals(
            Display.DisplaySpatialCartesianTuple)))) { // Spatial
        if (RealType.Latitude.equals(map.getScalar())) {
          lat_map = map;
        }
        else if (RealType.Longitude.equals(map.getScalar())) {
          lon_map = map;
        }
      }
    }
    if (lat_map == null || lon_map == null) {
      throw new DisplayException("Latitude and Longitude must be mapped");
    }

    int mmm = 0;
    int mmv = 0;
    front_manipulation_renderer =
      new FrontManipulationRendererJ3D(this, mmm, mmv);
    display.addReferences(front_manipulation_renderer, curve_ref);

    front_ref = new DataReferenceImpl("front");
    front_renderer = new DefaultRendererJ3D();
    front_renderer.suppressExceptions(true);
    display.addReferences(front_renderer, front_ref);

    release_ref = new DataReferenceImpl("release");
    release_cell = new ReleaseCell();
    release_cell.addReference(release_ref);
  }

  public static void initColormaps(DisplayImplJ3D display)
         throws VisADException, RemoteException {
    setupTypes();
    ScalarMap rmap = new ScalarMap(front_red, Display.Red);
    rmap.setRange(0.0, 1.0);
    display.addMap(rmap);
    ScalarMap gmap = new ScalarMap(front_green, Display.Green);
    gmap.setRange(0.0, 1.0);
    display.addMap(gmap);
    ScalarMap bmap = new ScalarMap(front_blue, Display.Blue);
    bmap.setRange(0.0, 1.0);
    display.addMap(bmap);
  }

  private static void setupTypes() throws VisADException {
    synchronized (type_lock) {
      if (curve_type == null) {
        RealTupleType latlon =
          new RealTupleType(RealType.Latitude, RealType.Longitude);
        curve_type = new SetType(latlon);
        // (front_index -> 
        //    ((Latitude, Longitude) -> (front_red, front_green, front_blue)))
        front_index = RealType.getRealType("front_index");
        front_red = RealType.getRealType("front_red");
        front_green = RealType.getRealType("front_green");
        front_blue = RealType.getRealType("front_blue");
        RealTupleType rgb =
          new RealTupleType(front_red, front_green, front_blue);
        front_inner = new FunctionType(latlon, rgb);
        front_type = new FunctionType(front_index, front_inner);
      }
    }
  }

  class ReleaseCell extends CellImpl {

    public ReleaseCell() {
    }

    public void doAction() throws VisADException, RemoteException {
      synchronized (data_lock) {
        Data data = curve_ref.getData();
        if (data == null || !(data instanceof UnionSet)) {
          if (debug) System.out.println("data null or not UnionSet");
          return;
        }
        SampledSet[] sets = ((UnionSet) data).getSets();
        if (!(sets[0] instanceof Gridded2DSet)) {
          if (debug) System.out.println("data not Gridded2DSet");
          return;
        }
        Gridded2DSet curve_set = (Gridded2DSet) sets[0];
        if (curve_set.getManifoldDimension() != 1) {
          if (debug) System.out.println("ManifoldDimension != 1");
          return;
        }
        float[][] curve_samples = null;
        try {
          curve_samples = curve_set.getSamples(false);
        }
        catch (VisADException e) {
          if (debug) System.out.println("release " + e);
          return;
        }
    
        boolean flip = false;
        double[] lat_range = lat_map.getRange();
        double[] lon_range = lon_map.getRange();
        if (lat_range[1] < lat_range[0]) flip = !flip;
        if (lon_range[1] < lon_range[0]) flip = !flip;
        if (curve_samples[lat_index][0] < 0.0) flip = !flip;
        if (lon_index < lat_index) flip = !flip;
        // if (debug) System.out.println("flip = " + flip);
    
        // transform curve to graphics coordinates
        // in order to "draw" front in graphics coordinates, then
        // transform back to (lat, lon)
        float[][] curve = new float[2][];
        curve[0] = lat_map.scaleValues(curve_samples[lat_index]);
        curve[1] = lon_map.scaleValues(curve_samples[lon_index]);
        // inverseScaleValues
        // if (debug) System.out.println("curve length = " + curve[0].length);
    
        // resample curve uniformly along length
        float increment = segment_length / profile_length;
        float[][] old_curve = resample_curve(curve, increment);
  
        int fw = filter_window;
  
        for (int tries=0; tries<10; tries++) {
          // lowpass filter curve
          curve = smooth_curve(old_curve, fw);
    
          // resample smoothed curve
          curve = resample_curve(curve, increment);
    
          try {
            curveToFront(curve, flip);
            break;
          }
          catch (VisADException e) {
            fw = 2 * fw;
            // if (debug) System.out.println("retry filter window = " + fw + " " + e);
            if (tries == 9) {
              System.out.println("cannot smooth curve");
              front = null;
            }
          }
        }
  
        front_ref.setData(front);
        curve_ref.setData(init_curve);
      } // end synchronized (data_lock)
    }
  }

  // FrontManipulationRendererJ3D button release
  public void release() {
    try {
      release_ref.setData(null);
    }
    catch (VisADException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
    catch (RemoteException e) {
      if (debug) System.out.println("release fail: " + e.toString());
    }
  }

  /** called by the application to end manipulation;
      returns the final front */
  public FieldImpl endManipulation()
         throws VisADException, RemoteException {
    synchronized (data_lock) {

      display.removeReference(curve_ref);
      // display.removeReference(front_ref);

      // (front_index ->
      //     ((Latitude, Longitude) -> (front_red, front_green, front_blue)))
    }
    return front;
  }

  private static final float CLIP_DELTA = 0.001f;

  private void curveToFront(float[][] curve, boolean flip)
         throws VisADException, RemoteException {

    // compute various scaling factors
    int len = curve[0].length;
    if (len < 2) {
      front = null;
      return;
    }
    float[] seg_length = new float[len-1];
    float curve_length = curveLength(curve, seg_length);
    float delta = curve_length / (len - 1);
    // curve[findex] where
    // float findex = ibase + mul * repeat_shapes[shape][0][j]
    float mul = profile_length / segment_length;
    // curve_perp[][findex] * ratio * repeat_shapes[shape][1][j]
    float ratio = delta * mul;

    // compute unit perpendiculars to curve
    float[][] curve_perp = new float[2][len];
    for (int i=0; i<len; i++) {
      int im = i - 1;
      int ip = i + 1;
      if (im < 0) im = 0;
      if (ip > len - 1) ip = len - 1;
      float yp = curve[0][ip] - curve[0][im];
      float xp = curve[1][ip] - curve[1][im];
      xp = -xp;
      float d = (float) Math.sqrt(xp * xp + yp * yp);
      if (flip) d = -d;
      xp = xp / d;
      yp = yp / d;
      curve_perp[0][i] = xp;
      curve_perp[1][i] = yp;
    }

    // build Vector of FlatFields for each shape of each segment
    Vector inner_field_vector = new Vector();
    for (int segment=0; true; segment++) {
      // figure out if clipping is needed for this segment
      // only happens for last segment
      boolean clip = false;
      float xclip = 0.0f;
      int ibase = segment * profile_length;
      int iend = ibase + profile_length;
      if (ibase > len - 1) break;
      if (iend > len - 1) {
        clip = true;
        iend = len - 1;
        xclip = (iend - ibase) / mul;       
      }

      // set up shapes for first or repeating segment
      int nshapes = nrshapes;
      float[][][] shapes = repeat_shapes;
      int[][][] tris = repeat_tris;
      float[] red = repeat_red;
      float[] green = repeat_green;
      float[] blue = repeat_blue;
      if (segment == 0) {
        nshapes = nfshapes;
        shapes = first_shapes;
        tris = first_tris;
        red = first_red;
        green = first_green;
        blue = first_blue;
      }

      // iterate over shapes for segment
      for (int shape=0; shape<nshapes; shape++) {
        float[][] samples = shapes[shape];
        int [][] ts = tris[shape];
/*
        // if needed, clip shape
        if (clip) {
          float[][][] outs = new float[1][][];
          int[][][] outt = new int[1][][];
          DelaunayCustom.clip(samples, ts, 1.0f, 0.0f, xclip, outs, outt);
          samples = outs[0];
          ts = outt[0];
        }
*/
        if (samples == null || samples[0].length < 1) break;

        float[][] ss =
          mapShape(samples, len, ibase, mul, ratio, curve, curve_perp);

// **** get rid of previous calls to fill() ****
        ts = DelaunayCustom.fill(ss);

        // if needed, clip shape
        if (clip) {
          float[][] clip_samples = {{xclip, xclip, xclip - CLIP_DELTA},
                                    {CLIP_DELTA, -CLIP_DELTA, 0.0f}};
          float[][] clip_ss =
            mapShape(clip_samples, len, ibase, mul, ratio, curve, curve_perp);
          // now solve for:
          //   xc * clip_samples[0][0] + yc * clip_samples[1][0] = 1
          //   xc * clip_samples[0][1] + yc * clip_samples[1][1] = 1
          //   xc * clip_samples[0][2] + yc * clip_samples[1][2] < 1
          float det = (clip_samples[0][1] * clip_samples[1][0] -
                       clip_samples[0][0] * clip_samples[1][1]);
          float xc = (clip_samples[1][0] - clip_samples[1][1]) / det;
          float yc = (clip_samples[0][1] - clip_samples[0][0]) / det;
          float v = 1.0f;
          if (xc * clip_samples[0][2] + yc * clip_samples[1][2] > v) {
            xc = - xc;
            yc = - yc;
            v = -v;
          }

          float[][][] outs = new float[1][][];
          int[][][] outt = new int[1][][];
          DelaunayCustom.clip(ss, ts, xc, yc, v, outs, outt);
          ss = outs[0];
          ts = outt[0];
        }

        if (ss == null) break;
        int n = ss[0].length;

        // create color values for field
        float[][] values = new float[3][n];
        float r = red[shape];
        float g = green[shape];
        float b = blue[shape];
        for (int i=0; i<n; i++) {
          values[0][i] = r;
          values[1][i] = g;
          values[2][i] = b;
        }

        // construct set and field
        DelaunayCustom delaunay = new DelaunayCustom(ss, ts);
        Irregular2DSet set =
          new Irregular2DSet(curve_type, ss, null, null, null, delaunay);
        FlatField field = new FlatField(front_inner, set);
        field.setSamples(values, false);
        inner_field_vector.addElement(field);
// some crazy bug - see Gridded3DSet.makeNormals()
      }
    } // end for (int segment=0; true; segment++)

    int nfields = inner_field_vector.size();
    Integer1DSet iset = new Integer1DSet(front_index, nfields);
    front = new FieldImpl(front_type, iset);
    FlatField[] fields = new FlatField[nfields];
    for (int i=0; i<nfields; i++) {
      fields[i] = (FlatField) inner_field_vector.elementAt(i);
    }
    front.setSamples(fields, false);
  }

  private float[][] mapShape(float[][] samples, int len, int ibase, float mul,
                             float ratio, float[][] curve, float[][] curve_perp) {
    // map shape into "coordinate system" defined by curve segment
    int n = samples[0].length;
    float[][] ss = new float[2][n];
    for (int i=0; i<n; i++) {
      float findex = ibase + mul * samples[0][i];
      int il = (int) findex;
      int ih = il + 1;

      if (il < 0) {
        il = 0;
        ih = il + 1;
      }
      if (ih > len - 1) {
        ih = len - 1;
        il = ih - 1;
      }
      // if (il < 0) il = 0;
      // if (il > len - 1) il = len - 1;
      // if (ih < 0) ih = 0;
      // if (ih > len - 1) ih = len - 1;

      float a = findex - il;

      if (a < -1.0f) a = -1.0f;
      if (a > 2.0f) a = 2.0f;
      // if (a < 0.0f) a = 0.0f;
      // if (a > 1.0f) a = 1.0f;

      float b = 1.0f - a;
      float xl = curve[0][il] + ratio * samples[1][i] * curve_perp[0][il];
      float yl = curve[1][il] + ratio * samples[1][i] * curve_perp[1][il];
      float xh = curve[0][ih] + ratio * samples[1][i] * curve_perp[0][ih];
      float yh = curve[1][ih] + ratio * samples[1][i] * curve_perp[1][ih];
      ss[0][i] = b * xl + a * xh;
      ss[1][i] = b * yl + a * yh;
    }
    // map shape back into (lat, lon) coordinates
    ss[lat_index] = lat_map.inverseScaleValues(ss[0]);
    ss[lon_index] = lon_map.inverseScaleValues(ss[1]);
    return ss;
  }

  public static float[][] smooth_curve(float[][] curve, int window) {
    int len = curve[0].length;
    float[][] newcurve = new float[2][len];
    for (int i=0; i<len; i++) {
      int win = window;
      if (i < win) win = i;
      int ii = (len - 1) - i;
      if (ii < win) win = ii;
      float runx = 0.0f;
      float runy = 0.0f;
      for (int j=i-win; j<=i+win; j++) {
        runx += curve[0][j];
        runy += curve[1][j];
      }
      newcurve[0][i] = runx / (2 * win + 1);
      newcurve[1][i] = runy / (2 * win + 1);
    }
    return newcurve;
  }

  /** resmaple curve into segments approximately increment in length */
  public static float[][] resample_curve(float[][] curve, float increment) {
    int len = curve[0].length;
    float[] seg_length = new float[len-1];
    float curve_length = curveLength(curve, seg_length);
    int npoints = 1 + (int) (curve_length / increment);
    float delta = curve_length / (npoints - 1);
    float[][] newcurve = new float[2][npoints];
    newcurve[0][0] = curve[0][0];
    newcurve[1][0] = curve[1][0];
    if (npoints < 2) return newcurve;
    int k = 0;
    float old_seg = seg_length[k];
    for (int i=1; i<npoints-1; i++) {
      float new_seg = delta;
      while (true) {
        if (old_seg < new_seg) {
          new_seg -= old_seg;
          k++;
          if (k > len-2) {
            throw new VisADError("k = " + k + " i = " + i);
          }
          old_seg = seg_length[k];
        }
        else {
          old_seg -= new_seg;
          float a = old_seg / seg_length[k];
          newcurve[0][i] = a * curve[0][k] + (1.0f - a) * curve[0][k+1];
          newcurve[1][i] = a * curve[1][k] + (1.0f - a) * curve[1][k+1];
          break;
        }
      }
    }
    newcurve[0][npoints-1] = curve[0][len-1];
    newcurve[1][npoints-1] = curve[1][len-1];
    return newcurve;
  }

  /** assumes curve is float[2][len] and seg_length is float[len-1] */
  public static float curveLength(float[][] curve, float[] seg_length) {
    int len = curve[0].length;
    float curve_length = 0.0f;
    for (int i=0; i<len-1; i++) {
      seg_length[i] = (float) Math.sqrt(
        ((curve[0][i+1] - curve[0][i]) * (curve[0][i+1] - curve[0][i])) +
        ((curve[1][i+1] - curve[1][i]) * (curve[1][i+1] - curve[1][i])));
      curve_length += seg_length[i];
    }
    return curve_length;
  }

  private boolean pfirst = true;

  class ProjectionControlListener implements ControlListener {
    private double base_scale = 1.0;
    private float last_cscale = 1.0f;

    public void controlChanged(ControlEvent e)
           throws VisADException, RemoteException {
      double[] matrix = pcontrol.getMatrix();
      double[] rot = new double[3];
      double[] scale = new double[1];
      double[] trans = new double[3];
      MouseBehaviorJ3D.unmake_matrix(rot, scale, trans, matrix);

      if (pfirst) {
        pfirst = false;
        base_scale = scale[0];
        last_cscale = 1.0f;
      }
      else {
        float cscale = (float) (base_scale / scale[0]);
        float ratio = cscale / last_cscale;
        if (ratio < 0.95f || 1.05f < ratio) {
          last_cscale = cscale;
          // shape_control1.setScale(cscale);
        }
      }
    }
  }

  class CurveMonitor extends CellImpl {
    DataReferenceImpl ref;
  
    public CurveMonitor(DataReferenceImpl r) {
      ref = r;
    }
  
    private final static float EPS = 0.01f;

    public void doAction() throws VisADException, RemoteException {
      synchronized (data_lock) {
        Gridded2DSet curve = (Gridded2DSet) ref.getData();

        int mouseModifiers =
          front_manipulation_renderer.getLastMouseModifiers();
        int mctrl = mouseModifiers & InputEvent.CTRL_MASK;

      } // end synchronized (data_lock)
    }
  }

  public static void main(String args[])
         throws VisADException, RemoteException {

    // construct RealTypes for wind record components
    RealType lat = RealType.Latitude;
    RealType lon = RealType.Longitude;

    SetType curve_type = new SetType(new RealTupleType(lat, lon));

    // construct Java3D display and mappings
    DisplayImplJ3D display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(lon, Display.XAxis);
    display.addMap(lonmap);
    lonmap.setRange(-10.0, 10.0);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(-10.0, 10.0);

    initColormaps(display);

    DataReferenceImpl curve_ref = new DataReferenceImpl("curve_ref");
    curve_ref.setData(null); // change

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test FrontDrawer");
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

/*
    float[][][] rshapes =
      {{{0.0f, 0.025f, 0.05f, 0.075f, 0.1f, 0.1f, 0.075f, 0.05f, 0.025f, 0.0f},
        {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.015f, 0.015f, 0.090f, 0.015f, 0.015f}}};
    float[] red = {1.0f};
    float[] green = {1.0f};
    float[] blue = {1.0f};
    FrontDrawer fd =
      new FrontDrawer(curve_ref, display, 8, 0.1f, null, null, null, null,
                      rshapes, red, green, blue);
*/
    int front_type = FrontDrawer.COLD_FRONT;
    try {
      if (args.length > 0) front_type = Integer.parseInt(args[0]);
    }
    catch(NumberFormatException e) {
    }
    FrontDrawer fd = new FrontDrawer(curve_ref, display, 8, front_type);


    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
    button_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    button_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    FrontActionListener fal = new FrontActionListener(fd, curve_ref);
    JButton end = new JButton("end manip");
    end.addActionListener(fal);
    end.setActionCommand("end");
    button_panel.add(end);
    panel.add(button_panel);

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);
  }
}

class FrontActionListener implements ActionListener {
  FrontDrawer fd;
  DataReferenceImpl track_ref;
  private static boolean debug = true;

  FrontActionListener(FrontDrawer f, DataReferenceImpl tr) {
    fd = f;
    track_ref = tr;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("end")) {
      try {
        FieldImpl front = fd.endManipulation();
        if (debug) System.out.println("end " + front.getType());
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
  }
}

class FrontManipulationRendererJ3D extends CurveManipulationRendererJ3D {

  FrontDrawer fd;

  FrontManipulationRendererJ3D(FrontDrawer f, int mmm, int mmv) {
    super(mmm, mmv, true); // true for only one
    fd = f;
  }

  /** mouse button released, ending direct manipulation */
  public void release_direct() {
    fd.release();
  }
}

