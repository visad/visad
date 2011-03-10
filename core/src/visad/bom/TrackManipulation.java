//
// TrackManipulation.java
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
import visad.java3d.*;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.rmi.*;


/**
   TrackManipulation is the VisAD class for direct
   manipulation rendering of storm tracks under Java3D
*/
public class TrackManipulation extends Object {

  private DisplayImplJ3D display;
  private DataReference track1_ref;
  private DataReference track2_ref;
  private DataReference track3_ref;

  private RealTupleType latlonshape = null;

  private ScalarMap latmap = null;
  private ScalarMap lonmap = null;
  private CoordinateSystem coord = null;
  private int latindex = -1;
  private int lonindex = -1;
  private int otherindex = -1;
  private float othervalue = 0.0f;

  private ScalarMap shapemap = null;
  private ShapeControl shapecontrol = null;

  private float x_size;
  private float y_size;
  private float angle;

  private static int NE = 32;
  private float[] x_ellipse = new float[NE + 1];
  private float[] y_ellipse = new float[NE + 1];

  /** (lat1, lon1) start of track
      (lat2, lon2) end of track
      d is a DisplayImplJ3D that has ScalarMaps of Latitude and
      Longitude but is not linked yet to any DataReferences;
      this constructor will add another ScalarMap to Shape and
      link the DisplayImplJ3D d to three data objects, two by
      direct manipulation */
  public TrackManipulation(float lat1, float lon1, float lat2, float lon2,
                           DisplayImplJ3D d, float xs, float ys, float ang)
         throws VisADException, RemoteException {

    // construct RealTuple objects to be manipulated at ends of track
    RealTupleType latlon =
      new RealTupleType(RealType.Latitude, RealType.Longitude);
    RealTuple track1 = new RealTuple(latlon, new double[] {lat1, lon1});
    RealTuple track2 = new RealTuple(latlon, new double[] {lat2, lon2});

    // construct RealTuple with RealType mapped to Shape that
    // creates storm track depiction
    RealType shape = RealType.getRealType("shape");
    latlonshape =
      new RealTupleType(RealType.Latitude, RealType.Longitude, shape);
    RealTuple track3 = new RealTuple(latlonshape, new double[] {lat1, lon1, 0.0});

    // construct DataReferences for these three RealTuples
    track1_ref = new DataReferenceImpl("track1_ref");
    track2_ref = new DataReferenceImpl("track2_ref");
    track3_ref = new DataReferenceImpl("track3_ref");
    track1_ref.setData(track1);
    track2_ref.setData(track2);
    track3_ref.setData(track3);

    // copy reference to DisplayImplJ3D
    display = d;

    // compute basic ellipse shape according to constructor
    // arguments
    x_size = (float) Math.abs(xs);
    y_size = (float) Math.abs(ys);
    angle = ang;
    float sa = (float) Math.sin(ang * Data.DEGREES_TO_RADIANS);
    float ca = (float) Math.cos(ang * Data.DEGREES_TO_RADIANS);
    for (int i=0; i<NE+1; i++) {
      double b = 2.0 * Math.PI * i / NE;
      float xe = (float) (x_size * Math.cos(b));
      float ye = (float) (y_size * Math.sin(b));
      x_ellipse[i] = ca * xe + sa * ye;
      y_ellipse[i] = ca * ye - sa * xe;
    }

    // find ScalarMaps of Latitude and Longitude
    Vector scalar_map_vector = display.getMapVector();
    Enumeration en = scalar_map_vector.elements();
    while (en.hasMoreElements()) {
      ScalarMap map = (ScalarMap) en.nextElement();
      if (RealType.Latitude.equals(map.getScalar())) {
        latmap = map;
      }
      else if (RealType.Longitude.equals(map.getScalar())) {
        lonmap = map;
      }
    }
    if (latmap == null || lonmap == null) {
      throw new DisplayException("Latitude and Longitude must be mapped " +
                                 "in display");
    }

    // get information from latmap and lonmap needed to
    // compute display locations from manipulated RealTuple
    // values
    DisplayRealType latreal = latmap.getDisplayScalar();
    DisplayRealType lonreal = lonmap.getDisplayScalar();
    DisplayTupleType lattuple = latreal.getTuple();
    DisplayTupleType lontuple = lonreal.getTuple();
    if (lattuple == null || !lattuple.equals(lontuple)) {
      throw new DisplayException("Latitude and Longitude must be mapped " +
                                 "to spatial in display(1)");
    }
    latindex = latreal.getTupleIndex();
    lonindex = lonreal.getTupleIndex();
    otherindex = 3 - (latindex + lonindex);
    DisplayRealType othertype =
      (DisplayRealType) lattuple.getComponent(otherindex);
    othervalue = (float) othertype.getDefaultValue();
    coord = lattuple.getCoordinateSystem();
    if (!lattuple.equals(Display.DisplaySpatialCartesianTuple) &&
        !(coord != null &&
          coord.getReference().equals(Display.DisplaySpatialCartesianTuple))) {
      throw new DisplayException("Latitude and Longitude must be mapped " +
                                 "to spatial in display(2)");
    }

    // construct ScalarMap to Shape, with a single 'shape'
    shapemap = new ScalarMap(shape, Display.Shape);
    display.addMap(shapemap);
    shapecontrol = (ShapeControl) shapemap.getControl();
    shapecontrol.setShapeSet(new Integer1DSet(1));

    // link RealTuples to display
    display.addReferences(new DirectManipulationRendererJ3D(), track1_ref);
    display.addReferences(new DirectManipulationRendererJ3D(), track2_ref);
    display.addReference(track3_ref);

    // construct CellImpl that computes storm track shape in
    // response to user manipulation of track end points, and
    // link it to those manipulable RealTuples
    TrackGetter tracker = new TrackGetter();
    tracker.addReference(track1_ref);
    tracker.addReference(track2_ref);
  }

  // compute approximate radius of ellipse in direction (x, y)
  private float getStep(float x, float y) {
    double dist = 2.0 * (x_size + y_size);
    float step = -1.0f;
    for (int i=0; i<NE+1; i++) {
      double d = Math.abs(x * y_ellipse[i] - y * x_ellipse[i]);
      if (d < dist) {
        dist = d;
        step = (float) Math.sqrt(x_ellipse[i] * x_ellipse[i] +
                                 y_ellipse[i] * y_ellipse[i]);
      }
    }
    return step;
  }

  // maximum size of shape geometry
  private static final int NUM = 4096;

  /** construct a VisADLineArray in the shape of a storm track
      with end points given in the values array */
  private VisADLineArray makeTrack(float[][] values) {
    float d, xd, yd;
    float x, y, z, x0, y0, x3, y3, x4, y4, x5, y5;
    float sscale = 0.75f * 0.15f;

    // start of arrow is located at first manipulable RealTuple
    x = 0.0f;
    y = 0.0f;
    z = values[2][0];
    // head of arrow is located at second manipulable RealTuple
    x5 = values[0][1] - values[0][0];
    y5 = values[1][1] - values[1][0];

    // get arrow vector and length
    float xdir = x5 - x;
    float ydir = y5 - y; 
    float dist = (float) Math.sqrt(xdir * xdir + ydir * ydir);

    // normalize direction
    x0 = xdir / dist;
    y0 = ydir / dist;

    // running count of geometry size
    int nv = 0;

    // allocate arrays for geometry
    float[] vx = new float[NUM];
    float[] vy = new float[NUM];
    float[] vz = new float[NUM];
    int lenv = vx.length;

    // draw arrow shaft
    vx[nv] = x;
    vy[nv] = y;
    vz[nv] = z;
    nv++;
    vx[nv] = x5;
    vy[nv] = y5;
    vz[nv] = z;
    nv++;

    // computation for arrow head
    xd = sscale * x0;
    yd = sscale * y0;
    x3 = x5 - 0.3f * (xd - yd);
    y3 = y5 - 0.3f * (yd + xd);
    x4 = x5 - 0.3f * (xd + yd);
    y4 = y5 - 0.3f * (yd - xd);

    // draw arrow head
    vx[nv] = x5;
    vy[nv] = y5;
    vz[nv] = z;
    nv++;
    vx[nv] = x3;
    vy[nv] = y3;
    vz[nv] = z;
    nv++;

    vx[nv] = x5;
    vy[nv] = y5;
    vz[nv] = z;
    nv++;
    vx[nv] = x4;
    vy[nv] = y4;
    vz[nv] = z;
    nv++;

    // compute number of ellipses and spacing between them
    float step = getStep(xdir, ydir); 
    int nsteps = (int) (dist / step);
    if (nsteps < 1) nsteps = 1;
    int lim = (vx.length - nv) / (2 * NE);
    if (nsteps < 1) nsteps = 1;
    if (nsteps > lim) nsteps = lim;
    float xstep = xdir / nsteps;
    float ystep = ydir / nsteps;

    // compute which ellipse points are 'outside' previous
    // ellipse (and hence visible)
    boolean[] outside = new boolean[NE + 1];
    for (int i=0; i<NE+1; i++) {
      float xs = x_ellipse[i] + xstep;
      float ys = y_ellipse[i] + ystep;
      float radius = getStep(xs, ys);
      float len = (float) Math.sqrt(xs * xs + ys * ys);
      outside[i] = (len > radius);
    }

    // construct geometry for visible points of one ellipse
    float[] xe = new float[2 * NE];
    float[] ye = new float[2 * NE];
    int ne = 0;
    for (int i=0; i<NE; i++) {
      if (outside[i] && outside[i + 1]) {
        xe[ne] = x_ellipse[i];
        ye[ne] = y_ellipse[i];
        ne++;
        xe[ne] = x_ellipse[i+1];
        ye[ne] = y_ellipse[i+1];
        ne++;
      }
    }

    // draw first ellipse
    float xcenter = x;
    float ycenter = y;
    for (int i=0; i<NE; i++) {
      vx[nv] = x_ellipse[i];
      vy[nv] = y_ellipse[i];
      vz[nv] = z;
      nv++;
      vx[nv] = x_ellipse[i+1];
      vy[nv] = y_ellipse[i+1];
      vz[nv] = z;
      nv++;
    }

    // add sequence of ellipses to storm track geometry
    for (int i=0; i<nsteps; i++) {
      xcenter += xstep;
      ycenter += ystep;
      for (int j=0; j<ne; j++) {
        vx[nv] = xcenter + xe[j];
        vy[nv] = ycenter + ye[j];
        vz[nv] = z;
        nv++;
      }
    }

    // construct and return VisADLineArray from geometry
    VisADLineArray array = new VisADLineArray();
    array.vertexCount = nv;
    float[] coordinates = new float[3 * nv];
    int m = 0;
    for (int i=0; i<nv; i++) {
      coordinates[m++] = vx[i];
      coordinates[m++] = vy[i];
      coordinates[m++] = vz[i];
    }
    array.coordinates = coordinates;
    return array;
  }

  /** this CellImpl computes storm track shapes from RealTuples
      at start and end of track */
  class TrackGetter extends CellImpl {

    public TrackGetter() {
    }

    public void doAction() throws VisADException, RemoteException {
      // get start and end locations, first in lat and lon
      float[] latvalues = new float[2];
      float[] lonvalues = new float[2];
      RealTuple tuple1 = (RealTuple) track1_ref.getData();
      latvalues[0] = (float)
        ((Real) tuple1.getComponent(0)).getValue(RealType.Latitude.getDefaultUnit());
      lonvalues[0] = (float)
        ((Real) tuple1.getComponent(1)).getValue(RealType.Longitude.getDefaultUnit());
      float lat0 = latvalues[0];
      float lon0 = lonvalues[0];
      RealTuple tuple2 = (RealTuple) track2_ref.getData();
      latvalues[1] = (float)
        ((Real) tuple2.getComponent(0)).getValue(RealType.Latitude.getDefaultUnit());
      lonvalues[1] = (float)
        ((Real) tuple2.getComponent(1)).getValue(RealType.Longitude.getDefaultUnit());
      // scale end locations to graphics coordiantes
      latvalues = latmap.scaleValues(latvalues);
      lonvalues = lonmap.scaleValues(lonvalues);

      float[][] values = new float[3][2];
      for (int k=0; k<2; k++) {
        values[latindex][k] = latvalues[k];
        values[lonindex][k] = lonvalues[k];
        values[otherindex][k] = othervalue;
      }
      // if necessary, convert to Cartesian graphics coordinates
      if (coord != null) {
        values = coord.toReference(values);
      }

      // disable display so it doesn't update twice
      display.disableAction();
      // first, base storm track shape at location of first
      // manipulable RealTuple
      RealTuple track3 = new RealTuple(latlonshape, new double[] {lat0, lon0, 0.0});
      track3_ref.setData(track3);
      // second, update shape based on end points
      VisADGeometryArray shape = makeTrack(values);
      shapecontrol.setShape(0, shape);
      // now let display update, just once
      display.enableAction();
    }

  }

  /** test TrackManipulation
      optional command line arguments:
      java visad.bom.TrackManipulation xsize ysize angle(degrees) */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // get command line arguments for ellipse shape
    float[] fargs = {0.2f, 0.1f, 0.0f};
    for (int i=0; i<args.length; i++) {
      try {
        fargs[i] = Float.parseFloat(args[i]);
      }
      catch (NumberFormatException exc) {
      }
    }

    // construct Java3D display with lat and lon mappings
    DisplayImplJ3D display =
      new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D());
    ScalarMap lonmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    display.addMap(lonmap);
    lonmap.setRange(-10.0, 10.0);
    ScalarMap latmap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(latmap);
    latmap.setRange(-10.0, 10.0);

    // construct a TrackManipulation object that sets up manipulation
    // of storm track shape
    TrackManipulation track = new TrackManipulation(0.0f, 0.0f, 3.0f, 3.0f,
                                       display, fargs[0], fargs[1], fargs[2]);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("test TrackManipulation");
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

