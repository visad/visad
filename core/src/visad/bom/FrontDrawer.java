//
// FrontDrawer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
public class FrontDrawer extends Object implements ControlListener {

  private static boolean debug = false;

  private Object data_lock = new Object();

  private DataReferenceImpl front_ref;
  private DefaultRendererJ3D front_renderer;
  private DataReferenceImpl curve_ref = null;
  private FrontManipulationRendererJ3D front_manipulation_renderer;

  private ReleaseCell release_cell;
  private DataReferenceImpl release_ref;

  private ZoomCell zoom_cell;
  private DataReferenceImpl zoom_ref;

  private ProjectionControl pcontrol = null;
  private ProjectionControlListener pcl = null;
  private float zoom = 1.0f;

  private AnimationControl acontrol = null;

  private DisplayImplJ3D display;
  private ScalarMap lat_map = null;
  private ScalarMap lon_map = null;

  private static Object type_lock = new Object();

  private int ntimes = 0;
  private int current_time_step = -1;

  private UnionSet init_curve = null; // manifold dimension = 1
  private static SetType curve_type = null; // Set(Latitude, Longitude)
  private int lat_index = 0;
  private int lon_index = 1;
  private float[][][] curves = null;
  private boolean[] flips = null;

  private FieldImpl fronts = null;
  // (RealType.Time -> (front_index ->
  //       ((Latitude, Longitude) -> (front_red, front_green, front_blue))))
  private static FunctionType fronts_type = null;
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

  // length of first segment in graphics coordinates
  private float fsegment_length;
  // length of each repeating segment in graphics coordinates
  private float rsegment_length;

  // number of intervals in curve for first segment
  private int fprofile_length = -1;
  // number of intervals in curve for each repeating segment
  private int rprofile_length = -1;

  // size of filter window for smoothing curve
  private int filter_window = 1;

  // copy of cs argument
  float[][][] ccs = null;
  // copy of fs argument
  FieldImpl ffs = null;

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
  public static final int UNEVEN_LOW_LEVEL_JET = 21;

  private static final float[] rsegmentarray = {
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.05f, // TROUGH = 9
    0.1f, // RIDGE = 10
    0.05f, // MOISTURE = 11
    0.2f, // LOW_LEVEL_JET = 12
    0.2f, // UPPER_LEVEL_JET = 13
    0.1f, // DRY_LINE = 14
    0.05f, // TOTAL_TOTALS = 15
    0.1f, // LIFTED_INDEX = 16
    0.15f, // ISOTHERMS = 17
    0.1f, // THICKNESS_RIDGE = 18
    0.05f, // LOWER_THERMAL_TROUGH = 19
    0.1f, // UPPER_THERMAL_TROUGH = 20
    0.1f // UNEVEN_LOW_LEVEL_JET = 21
  };

  private static final float[] fsegmentarray = {
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.2f,
    0.05f, // TROUGH = 9
    0.1f, // RIDGE = 10
    0.05f, // MOISTURE = 11
    0.2f, // LOW_LEVEL_JET = 12
    0.2f, // UPPER_LEVEL_JET = 13
    0.1f, // DRY_LINE = 14
    0.05f, // TOTAL_TOTALS = 15
    0.1f, // LIFTED_INDEX = 16
    0.15f, // ISOTHERMS = 17
    0.1f, // THICKNESS_RIDGE = 18
    0.05f, // LOWER_THERMAL_TROUGH = 19
    0.1f, // UPPER_THERMAL_TROUGH = 20
    0.2f // UNEVEN_LOW_LEVEL_JET = 21
  };

  private static final float[][][][] rshapesarray = {

    // COLD_FRONT =0
    {{{0.0f, 0.025f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.025f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.04f, 0.01f}}},

    // WARM_FRONT = 1
    {{{0.0f, 0.035f, 0.07f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.07f, 0.0525f, 0.035f, 0.0175f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.03f, 0.037f, 0.03f, 0.01f}}},

    // OCCLUDED_FRONT = 2
    {{{0.0f, 0.025f, 0.05f, 0.07f, 0.105f, 0.14f, 0.17f, 0.2f,
       0.2f, 0.17f, 0.14f, 0.1225f, 0.105f, 0.0875f, 0.07f, 0.05f, 0.025f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.03f, 0.037f, 0.03f, 0.01f, 0.01f, 0.04f, 0.01f}}},

    // STATIONARY_FRONT = 3
    {{{0.09f, 0.11f, 0.1275f, 0.145f, 0.1625f, 0.18f, 0.2f,
       0.2f, 0.1775f, 0.155f, 0.1175f, 0.09f},
      {0.0f, 0.0f, -0.02f, -0.027f, -0.02f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.01f}},
     {{0.0f, 0.02f, 0.045f, 0.07f, 0.09f,
       0.09f, 0.07f, 0.045f, 0.02f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.04f, 0.01f, 0.0f}}},

    // CONVERGENCE = 4
    {{{0.0f, 0.03f, 0.035f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.11f, 0.135f, 0.13f, 0.1f, 0.05f, 0.0f},
      {0.01f, 0.04f, 0.035f, 0.01f, 0.01f, 0.01f, 0.01f, 0.01f,
       0.0f, 0.0f, 0.0f, -0.025f, -0.03f, 0.0f, 0.0f, 0.0f}}},

    // FRONTOGENESIS = 5
    {{{0.0f, 0.035f, 0.07f, 0.1f, 0.15f,
       0.15f, 0.1f, 0.0875f, 0.075f, 0.0625f, 0.05f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.025f, 0.035f, 0.025f, 0.01f, 0.01f}},
     {{0.16f, 0.19f,
       0.19f, 0.16f},
      {-0.005f, -0.005f,
       0.015f, 0.015f}}},

    // FRONTOLYSIS = 6
    {{{0.0f, 0.035f, 0.07f, 0.1f, 0.15f,
       0.15f, 0.1f, 0.0875f, 0.075f, 0.0625f, 0.05f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.025f, 0.035f, 0.025f, 0.01f, 0.01f}},
     {{0.16f, 0.17f, 0.17f, 0.18f, 0.18f, 0.19f,
       0.19f, 0.18f, 0.18f, 0.17f, 0.17f, 0.16f},
      {0.0f, 0.0f, -0.01f, -0.01f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.02f, 0.02f, 0.01f, 0.01f}}},

    // UPPER_COLD_FRONT = 7
    {{{0.0f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.01f}},
     {{0.0f, 0.03f, 0.06f,
       0.05f, 0.03f, 0.01f},
      {0.01f, 0.04f, 0.01f,
       0.01f, 0.03f, 0.01f}}},

    // UPPER_WARM_FRONT = 8
    {{{0.0f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.01f}},
     {{0.0f, 0.015f, 0.03f, 0.045f, 0.06f,
       0.05f, 0.04f, 0.03f, 0.02f, 0.01f},
      {0.01f, 0.03f, 0.037f, 0.03f, 0.01f,
       0.01f, 0.023f, 0.027f, 0.023f, 0.01f}}},

    // TROUGH = 9
    {{{0.0f, 0.035f,
       0.035f, 0.0f},
      {0.0f, 0.0f,
       0.01f, 0.01f}}},

    // RIDGE = 10
    {{{0.0f, 0.05f, 0.1f,
       0.1f, 0.05f, 0.0f},
      {0.04f, -0.06f, 0.04f,
       0.06f, -0.04f, 0.06f}}},

    // MOISTURE = 11
    {{{0.0f, 0.0f, 0.01f, 0.01f, 0.05f,
       0.05f, 0.0f},
      {0.01f, 0.05f, 0.05f, 0.01f, 0.01f,
       0.0f, 0.0f}}},

    // LOW_LEVEL_JET = 12
    {{{0.0f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.0f},
      {0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.01f}}},

    // UPPER_LEVEL_JET = 13
    {{{0.0f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.0f},
      {-0.01f, -0.01f, -0.01f, -0.01f, -0.01f,
       0.02f, 0.02f, 0.02f, 0.02f, 0.02f}}},

    // DRY_LINE = 14
    {{{0.0f, 0.05f,
       0.05f, 0.0f},
      {0.0f, 0.0f,
       0.01f, 0.01f}},
     {{0.06f, 0.09f,
       0.09f, 0.06f},
      {-0.005f, -0.005f,
       0.015f, 0.015f}}},

    // TOTAL_TOTALS = 15
    {{{0.0f, 0.035f,
       0.035f, 0.0f},
      {0.0f, 0.0f,
       0.01f, 0.01f}}},

    // LIFTED_INDEX = 16
    {{{0.0f, 0.05f,
       0.05f, 0.0f},
      {0.0f, 0.0f,
       0.01f, 0.01f}},
     {{0.06f, 0.09f,
       0.09f, 0.06f},
      {-0.005f, -0.005f,
       0.015f, 0.015f}}},

    // ISOTHERMS = 17
    {{{0.0f, 0.0f, 0.04f, 0.08f,
       0.08f, 0.04f, 0.0f, 0.0f,
       0.02f, 0.02f, 0.06f,
       0.06f, 0.02f, 0.02f},
      {0.0f, -0.02f, -0.02f, -0.02f,
       0.02f, 0.02f, 0.02f, 0.0f,
       0.0f, 0.01f, 0.01f,
       -0.01f, -0.01f, 0.0f}}},

    // THICKNESS_RIDGE = 18
    {{{0.0f, 0.05f, 0.1f,
       0.1f, 0.05f, 0.0f},
      {0.01f, -0.06f, 0.01f,
       0.06f, -0.01f, 0.06f}}},

    // LOWER_THERMAL_TROUGH = 19
    {{{0.0f, 0.045f,
       0.045f, 0.0f},
      {-0.01f, -0.01f,
       0.02f, 0.02f}}},

    // UPPER_THERMAL_TROUGH = 20
    {{{0.0f, 0.04f, 0.02f},
      {0.0f, 0.0f, 0.04f}}},

    // UNEVEN_LOW_LEVEL_JET = 21
    {{{0.0f, 0.05f, 0.1f,
       0.1f, 0.05f, 0.0f},
      {0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f}}},

  };

  private static final float[][] rredarray = {
    {0.0f},
    {1.0f},
    {1.0f},
    {1.0f, 0.0f},
    {1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {0.5f},
    {0.5f},
    {1.0f},
    {0.5f},
    {0.5f},
    {0.5f, 0.5f}, // DRY_LINE = 14
    {1.0f},
    {1.0f, 1.0f}, // LIFTED_INDEX = 16
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {0.5f}
  };

  private static final float[][] rgreenarray = {
    {0.0f},
    {0.0f},
    {0.0f},
    {0.0f, 0.0f},
    {1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {0.3f},
    {0.3f},
    {1.0f},
    {0.5f},
    {0.5f},
    {0.3f, 0.3f}, // DRY_LINE = 14
    {1.0f},
    {1.0f, 1.0f}, // LIFTED_INDEX = 16
    {1.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {0.5f}
  };

  private static final float[][] rbluearray = {
    {1.0f},
    {0.0f},
    {1.0f},
    {0.0f, 1.0f},
    {1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {1.0f, 1.0f},
    {0.0f},
    {0.0f},
    {1.0f},
    {1.0f},
    {1.0f},
    {0.0f, 0.0f}, // DRY_LINE = 14
    {1.0f},
    {1.0f, 1.0f}, // LIFTED_INDEX = 16
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

    // LOW_LEVEL_JET = 12
    {{{0.0f, 0.07f, 0.075f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.01f, 0.075f, 0.07f, 0.0f},
      {0.0f, -0.07f, -0.065f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.075f, 0.08f, 0.01f}}},

    // UPPER_LEVEL_JET = 13
    {{{0.0f, 0.06f, 0.077f, 0.04f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.04f, 0.077f, 0.06f, 0.0f},
      {-0.001f, -0.06f, -0.04f, -0.01f, -0.01f, -0.01f, -0.01f, -0.01f,
       0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.05f, 0.07f, 0.02f}}},

    null,
    null,
    null,
    null,
    null,
    null,
    null,

    // UNEVEN_LOW_LEVEL_JET = 21
    {{{0.0f, 0.07f, 0.075f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f,
       0.2f, 0.15f, 0.1f, 0.05f, 0.01f, 0.075f, 0.07f, 0.0f},
      {0.0f, -0.07f, -0.065f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
       0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.075f, 0.08f, 0.01f}}}

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
    {0.5f},
    {0.5f},
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    {0.5f}
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
    {0.5f},
    {0.5f},
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    {0.5f}
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
    {1.0f},
    {1.0f},
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    {1.0f}
  };

  /** manipulable front with predefined pattern front_kind and
      user specified color arrays */
  public FrontDrawer(FieldImpl fs, float[][][] cs,
                     DisplayImplJ3D d, int fw, int front_kind,
                     float[] fred, float[] fgreen, float[] fblue,
                     float[] rred, float[] rgreen, float[] rblue)
         throws VisADException, RemoteException {
    this(fs, cs, d, fw, fsegmentarray[front_kind], rsegmentarray[front_kind],
         fshapesarray[front_kind], fred, fgreen, fblue,
         rshapesarray[front_kind], rred, rgreen, rblue);
  }

  /** manipulable front with predefined pattern front_kind and
      default color arrays */
  public FrontDrawer(FieldImpl fs, float[][][] cs,
                     DisplayImplJ3D d, int fw, int front_kind)
         throws VisADException, RemoteException {
    this(fs, cs, d, fw, fsegmentarray[front_kind], rsegmentarray[front_kind],
         fshapesarray[front_kind], fredarray[front_kind],
         fgreenarray[front_kind], fbluearray[front_kind],
         rshapesarray[front_kind], rredarray[front_kind],
         rgreenarray[front_kind], rbluearray[front_kind]);
  }


  /**
     fs is null or has MathType
       (RealType.Time -> (front_index ->
           ((Latitude, Longitude) -> (front_red, front_green, front_blue))))
     cs is null or contains a time array of curves for fs
     fw is the filter window size for smoothing the curve
     segment is length in graphics coordinates of first and repeating profiles
     fshapes is dimensioned [nfshapes][2][points_per_shape]
     fred, fgreen and fblue are dimensioned [nfshapes]
     rshapes is dimensioned [nrshapes][2][points_per_shape]
     rred, rgreen and rblue are dimensioned [nrshapes]
     fshapes[*][0][*] and rshapes[*][0][*] generally in range 0.0f to segment
  */
  public FrontDrawer(FieldImpl fs, float[][][] cs,
                     DisplayImplJ3D d,
                     int fw, float segment,
                     float[][][] fshapes,
                     float[] fred, float[] fgreen, float[] fblue,
                     float[][][] rshapes,
                     float[] rred, float[] rgreen, float[] rblue)
         throws VisADException, RemoteException {
    this(fs, cs, d, fw, segment, segment, fshapes, fred, fgreen, fblue,
         rshapes, rred, rgreen, rblue);
  }


  /**
     fs is null or has MathType
       (RealType.Time -> (front_index ->
           ((Latitude, Longitude) -> (front_red, front_green, front_blue))))
     cs is null or contains a time array of curves for fs
     fw is the filter window size for smoothing the curve
     fsegment is length in graphics coordinates of first profile
     rsegment is length in graphics coordinates of repeating profile
     fshapes is dimensioned [nfshapes][2][points_per_shape]
     fred, fgreen and fblue are dimensioned [nfshapes]
     rshapes is dimensioned [nrshapes][2][points_per_shape]
     rred, rgreen and rblue are dimensioned [nrshapes]
     fshapes[*][0][*] and rshapes[*][0][*] generally in range 0.0f to segment
  */
  public FrontDrawer(FieldImpl fs, float[][][] cs,
                     DisplayImplJ3D d,
                     int fw, float fsegment, float rsegment,
                     float[][][] fshapes,
                     float[] fred, float[] fgreen, float[] fblue,
                     float[][][] rshapes,
                     float[] rred, float[] rgreen, float[] rblue)
         throws VisADException, RemoteException {
    try {
      initColormaps(d);
    }
    catch (VisADException e) {
      if (debug) System.out.println("caught " + e.toString());
    }
    ccs = cs;
    ffs = fs;

    curve_ref = new DataReferenceImpl("curve_ref");
    Gridded2DSet set = null;
    if (cs == null || cs[0] == null) {
      set = new Gridded2DSet(curve_type, new float[][] {{0.0f}, {0.0f}}, 1);
    }
    else {
      set = new Gridded2DSet(curve_type, cs[0], cs[0][0].length);
    }
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
    fsegment_length = fsegment;
    rsegment_length = rsegment;

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
      throw new VisADException("bad rcolors");
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
    rprofile_length = rlen;
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
      fprofile_length = flen;
      first_red = new float[nfshapes];
      first_green = new float[nfshapes];
      first_blue = new float[nfshapes];
      System.arraycopy(fred, 0, first_red, 0, nfshapes);
      System.arraycopy(fgreen, 0, first_green, 0, nfshapes);
      System.arraycopy(fblue, 0, first_blue, 0, nfshapes);
    }
    if (rprofile_length < 5) rprofile_length = 5;
    if (fprofile_length < 5) fprofile_length = 5;

    pcontrol = display.getProjectionControl();
    pcl = new ProjectionControlListener();
    pcontrol.addControlListener(pcl);

    acontrol = (AnimationControl) display.getControl(AnimationControl.class);
    if (acontrol == null) {
      throw new DisplayException("display must include " +
                     "ScalarMap to Animation");
    }
    Vector tmap = display.getMapVector();
    for (int i=0; i<tmap.size(); i++) {
      ScalarMap map = (ScalarMap) tmap.elementAt(i);
      Control c = map.getControl();
      if (acontrol.equals(c)) {
        if (!RealType.Time.equals(map.getScalar())) {
          throw new DisplayException("must be Time mapped to " +
                                     "Animation " + map.getScalar());
        }
      }
    }

    Set aset = acontrol.getSet();
    if (aset != null) {
      setupAnimationSet(aset);
    }
    else {
      acontrol.addControlListener(this);
    }

    // find spatial maps for Latitude and Longitude
    lat_map = null;
    lon_map = null;
    Vector scalar_map_vector = display.getMapVector();
    Enumeration en = scalar_map_vector.elements();
    while (en.hasMoreElements()) {
      ScalarMap map = (ScalarMap) en.nextElement();
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
    front_ref.setData(fronts);
    front_renderer = new DefaultRendererJ3D();
    front_renderer.suppressExceptions(true);
    display.addReferences(front_renderer, front_ref);

    release_ref = new DataReferenceImpl("release");
    release_cell = new ReleaseCell();
    release_cell.addReference(release_ref);

    zoom_ref = new DataReferenceImpl("zoom");
    zoom_cell = new ZoomCell();
    zoom_cell.addReference(zoom_ref);

    setScale();
  }

  private void setupAnimationSet(Set aset)
    throws VisADException {
    ntimes = aset.getLength();
    current_time_step = acontrol.getCurrent();
    if (ccs == null) {
      curves = new float[ntimes][][];
    }
    else {
      curves = ccs;
      if (ccs.length != ntimes) {
        throw new VisADException("cs bad number of times " +
                                 ccs.length + " != " + ntimes);
      }
    }
    flips = new boolean[ntimes];
    if (ffs == null) {
      fronts = new FieldImpl(fronts_type, aset);
    }
    else {
      fronts = ffs;
      if (!aset.equals(ffs.getDomainSet())) {
        throw new VisADException("fs bad time Set " +
                                 ffs.getDomainSet() + " != " + aset);
      }
    }
  }

  public void controlChanged(ControlEvent e) {
    if (fronts == null) {
      try {
        Set aset = acontrol.getSet();
        if (aset != null) {
          setupAnimationSet(aset);
        }
      }
      catch (VisADException ee) {
      }
    }
  }

  public DefaultRendererJ3D getFrontRenderer() {
    return front_renderer;
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
        fronts_type = new FunctionType(RealType.Time, front_type);
      }
    }
  }

  private Gridded2DSet last_curve_set = null;

  class ReleaseCell extends CellImpl {

    private boolean first = true;

    public ReleaseCell() {
    }

    public void doAction() throws VisADException, RemoteException {
      if (first) {
        first = false;
        return;
      }
      if (acontrol.getOn()) return;
      current_time_step = acontrol.getCurrent();
      if(debug) System.out.println("ReleaseCell " + current_time_step + " " + ntimes);
      if (current_time_step < 0 || current_time_step >= ntimes) return;

      synchronized (data_lock) {
        Data data = null;
        if (curve_ref != null) data = curve_ref.getData();
        Gridded2DSet curve_set = null;
        if (data == null || !(data instanceof UnionSet)) {
          if (debug) System.out.println("data null or not UnionSet");
          if (curve_ref != null) curve_ref.setData(init_curve);
          curve_set = last_curve_set;
        }
        else {
          SampledSet[] sets = ((UnionSet) data).getSets();
          if (sets == null || sets.length == 0 ||
              !(sets[0] instanceof Gridded2DSet)) {
            if (debug) System.out.println("data not Gridded2DSet");
            if (curve_ref != null) curve_ref.setData(init_curve);
            curve_set = last_curve_set;
          }
          else if (sets[0].getManifoldDimension() != 1) {
            if (debug) System.out.println("ManifoldDimension != 1");
            if (curve_ref != null) curve_ref.setData(init_curve);
            curve_set = last_curve_set;
          }
          else {
            curve_set = (Gridded2DSet) sets[0];
          }
        }
        if (curve_set == null) {
          if (debug) System.out.println("curve_set is null");
          if (curve_ref != null) curve_ref.setData(init_curve);
          return;
        }

        float[][] curve_samples = null;
        try {
          curve_samples = curve_set.getSamples(false);
          if (curve_samples == null || curve_samples[0].length < 2) {
            if (curve_ref != null) curve_ref.setData(init_curve);
            throw new VisADException("bad curve_samples");
          }
        }
        catch (VisADException e) {
          // if (debug) System.out.println("release " + e);
          if (curve_ref != null) curve_ref.setData(init_curve);
          curve_set = last_curve_set;
          try {
            if (curve_set != null) curve_samples = curve_set.getSamples(false);
            if (curve_samples == null || curve_samples[0].length < 2) {
              if (curve_ref != null) curve_ref.setData(init_curve);
              // throw new VisADException("bad last curve_samples");
            }
          }
          catch (VisADException ee) {
            if (debug) System.out.println("release " + ee);
            return;
          }
        }
        last_curve_set = curve_set;

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

        front = robustCurveToFront(curve, flip);
        curves[current_time_step] = curve;
        flips[current_time_step] = flip;
        if (front != null) fronts.setSample(current_time_step, front);

        // System.out.println("front_ref.setData in ReleaseCell");
        front_ref.setData(fronts);

        if (curve_ref != null) curve_ref.setData(init_curve);
      } // end synchronized (data_lock)
    }
  }

  class ZoomCell extends CellImpl {

    public ZoomCell() {
    }

    public void doAction() throws VisADException, RemoteException {
      synchronized (data_lock) {
        for (int i=0; i<ntimes; i++) {
          if (curves[i] != null) {
            front = robustCurveToFront(curves[i], flips[i]);
            if (front != null) fronts.setSample(i, front);
          }
        }
      } // end synchronized (data_lock)
    }
  }

  private FieldImpl robustCurveToFront(float[][] curve, boolean flip)
          throws RemoteException {
    // resample curve uniformly along length
    float increment = rsegment_length / (rprofile_length * zoom);
    float[][] old_curve = resample_curve(curve, increment);

    int fw = filter_window;

    for (int tries=0; tries<12; tries++) {
      // lowpass filter curve
      curve = smooth_curve(old_curve, fw);

      // resample smoothed curve
      curve = resample_curve(curve, increment);

      try {
        front = curveToFront(curve, flip);
        break;
      }
      catch (VisADException e) {
        old_curve = curve;
        if (tries > 4) {
          int n = old_curve[0].length;
          if (n > 2) {
            float[][] no = new float[2][n - 2];
            System.arraycopy(old_curve[0], 1, no[0], 0, n - 2);
            System.arraycopy(old_curve[1], 1, no[1], 0, n - 2);
            old_curve = no;
          }
        }
        if (tries > 8) fw = 2 * fw;
        // if (debug) System.out.println("retry filter window = " + fw + " " + e);
        if (tries == 9) {
          if(debug)System.out.println("cannot smooth curve");
          front = null;
        }
      }
    }
    return front;
  }


  public Gridded2DSet getCurve() {
    return last_curve_set;
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
  public void endManipulation()
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      if (curve_ref != null) display.removeReference(curve_ref);
      curve_ref = null;
    }
  }

  /** called by the application to end manipulation;
      returns the final front */
  public Vector endItAll()
         throws VisADException, RemoteException {
    synchronized (data_lock) {
      if (curve_ref != null) display.removeReference(curve_ref);
      curve_ref = null;
      if (front_ref != null) {
        display.removeReference(front_ref);
        pcontrol.removeControlListener(pcl);
        release_cell.removeReference(release_ref);
        zoom_cell.removeReference(zoom_ref);
      }
      front_ref = null;
    }
    Vector vector = new Vector();
    vector.addElement(fronts);
    vector.addElement(curves);
    return vector;
  }

  private static final float CLIP_DELTA = 0.001f;

  private FieldImpl curveToFront(float[][] curve, boolean flip)
         throws VisADException, RemoteException {

    // compute various scaling factors
    int len = curve[0].length;
    if (len < 2) {
      return null;
    }
    float[] seg_length = new float[len-1];
    float curve_length = curveLength(curve, seg_length);
    float delta = curve_length / (len - 1);
    // curve[findex] where
    // float findex = ibase + mul * repeat_shapes[shape][0][j]
    float mul = rprofile_length * zoom / rsegment_length;
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

      // curve[findex] where
      // float findex = ibase + mul * repeat_shapes[shape][0][j]
      float segment_length = (segment == 0) ? fsegment_length : rsegment_length;
      int profile_length = (segment == 0) ? fprofile_length : rprofile_length;
      mul = profile_length * zoom / segment_length;
      // curve_perp[][findex] * ratio * repeat_shapes[shape][1][j]
      // float ratio = delta * mul;


      // figure out if clipping is needed for this segment
      // only happens for last segment
      boolean clip = false;
      float xclip = 0.0f;
      // int ibase = segment * profile_length;
      int ibase = (segment == 0) ? 0 : fprofile_length + (segment - 1) * rprofile_length;
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
      } // end for (int shape=0; shape<nshapes; shape++)
    } // end for (int segment=0; true; segment++)

    int nfields = inner_field_vector.size();
    Integer1DSet iset = new Integer1DSet(front_index, nfields);
    FieldImpl front = new FieldImpl(front_type, iset);
    FlatField[] fields = new FlatField[nfields];
    for (int i=0; i<nfields; i++) {
      fields[i] = (FlatField) inner_field_vector.elementAt(i);
    }
    front.setSamples(fields, false);
    return front;
  }

  private float[][] mapShape(float[][] samples, int len, int ibase, float mul,
                             float ratio, float[][] curve, float[][] curve_perp) {
    // map shape into "coordinate system" defined by curve segment
    int n = samples[0].length;
    float[][] ss = new float[2][n];
    for (int i=0; i<n; i++) {
      float findex = ibase + mul * samples[0][i] / zoom;
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
      float xl =
        curve[0][il] + ratio * samples[1][i] * curve_perp[0][il] / zoom;
      float yl =
        curve[1][il] + ratio * samples[1][i] * curve_perp[1][il] / zoom;
      float xh =
        curve[0][ih] + ratio * samples[1][i] * curve_perp[0][ih] / zoom;
      float yh =
        curve[1][ih] + ratio * samples[1][i] * curve_perp[1][ih] / zoom;
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

    public void controlChanged(ControlEvent e)
           throws VisADException, RemoteException {
      setScale();
    }
  }

  private float last_zoom = 1.0f;

  private void setScale()
          throws VisADException, RemoteException {
    double[] matrix = pcontrol.getMatrix();
    double[] rot = new double[3];
    double[] scale = new double[1];
    double[] trans = new double[3];
    MouseBehaviorJ3D.unmake_matrix(rot, scale, trans, matrix);

    zoom = (float) scale[0];
    float ratio = zoom / last_zoom;
// System.out.println("setScale " + zoom + " " + last_zoom + " " + ratio);

    if (ratio < 0.95f || 1.05f < ratio) {
      last_zoom = zoom;
      if (zoom_ref != null) zoom_ref.setData(null);
      // if (release_ref != null) release_ref.setData(null);
// System.out.println("setScale call setData " + zoom + " " + last_zoom +
//                    " " + ratio);
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
    lonmap.setRange(0.0, 20.0);
    ScalarMap latmap = new ScalarMap(lat, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(-40.0, -20.0);

    ScalarMap timemap = new ScalarMap(RealType.Time, Display.Animation);
    display.addMap(timemap);
    AnimationControl acontrol = (AnimationControl) timemap.getControl();
/* WLH 3 Sept 2001 WHY?
    // acontrol.setSet(new Integer1DSet(RealType.Time, 4));
*/
    acontrol.setSet(new Integer1DSet(RealType.Time, 4));

    initColormaps(display);

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
    panel.add(new AnimationWidget(timemap));

    int front_kind = FrontDrawer.COLD_FRONT;
    try {
      if (args.length > 0) front_kind = Integer.parseInt(args[0]);
    }
    catch(NumberFormatException e) {
    }
    FrontDrawer fd = new FrontDrawer(null, null, display, 8, front_kind);

    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
    button_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    button_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    JButton end = new JButton("detach");
    FrontActionListener fal = new FrontActionListener(fd, end, display, front_kind);
    end.addActionListener(fal);
    end.setActionCommand("detach");
    button_panel.add(end);
    panel.add(button_panel);

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);

/* WLH 3 Sept 2001 WHY?
    new Delay(5000);
    acontrol.setSet(new Integer1DSet(RealType.Time, 4));
*/
  }
}

class FrontActionListener implements ActionListener {
  private FrontDrawer fd;
  private JButton end;
  private DisplayImplJ3D display;
  private int front_kind;

  private FieldImpl fronts = null;
  private float[][][] curves = null;

  FrontActionListener(FrontDrawer f, JButton e, DisplayImplJ3D d, int fk) {
    fd = f;
    end = e;
    display = d;
    front_kind = fk;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("detach")) {
      if (end.getText().equals("detach")) {
        end.setText("attach");
        Vector vector = null;
        try {
          vector = fd.endItAll();
        }
        catch (VisADException ex) {
        }
        catch (RemoteException ex) {
        }
        fronts = (FieldImpl) vector.elementAt(0);
        curves = (float[][][]) vector.elementAt(1);
      }
      else {
        end.setText("detach");
        try {
          // System.out.println("fronts " + fronts);
          // System.out.println("curves " + curves);
          fd = new FrontDrawer(fronts, curves, display, 8, front_kind);
        }
        catch (VisADException ex) {
        }
        catch (RemoteException ex) {
        }
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

