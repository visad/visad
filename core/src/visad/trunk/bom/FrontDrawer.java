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

  private ProjectionControl pcontrol = null;

  private DisplayImplJ3D display;
  private ScalarMap lat_map = null;
  private ScalarMap lon_map = null;

  private Gridded2DSet curve = null; // manifold dimension = 1
  private Gridded2DSet front = null; // manifold dimension = 2

  private SetType front_type = null;
  private int lat_index = 0;
  private int lon_index = 1;

  private int profile_length = -1;
  private float[] front_profile_bot = null;
  private float[] front_profile_top = null;
  private float segment_length;

  private int filter_window = 1;

  /**
     cr should be null or cr.getData() should have MathType:
       Set(RealType.Latitude, RealType.Longitude)
  */
  public FrontDrawer(DataReferenceImpl cr, DisplayImplJ3D d,
                     float[] profile_bot, float[] profile_top, float segment,
                     int fw)
         throws VisADException, RemoteException {
    front_type =
      new SetType(new RealTupleType(RealType.Latitude, RealType.Longitude));
    if (cr == null) {
      curve_ref = new DataReferenceImpl("curve_ref");
    }
    else {
      curve_ref = cr;
    }
    Data data = curve_ref.getData();
    if (data == null || !(data instanceof Gridded2DSet)) {
      Gridded2DSet curve_set =
        new Gridded2DSet(front_type, new float[][] {{0.0f}, {0.0f}}, 1); // ??
      curve_ref.setData(curve_set);
    }
    else {
      Gridded2DSet curve_set = (Gridded2DSet) data;
      SetType st = (SetType) curve_set.getType();
      if (!st.equals(front_type)) {
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

    if (profile_bot == null || profile_top == null ||
        profile_bot.length != profile_top.length) {
      throw new VisADException("bad profile");
    }
    segment_length = segment;
    profile_length = profile_bot.length;
    front_profile_bot = new float[profile_length];
    front_profile_top = new float[profile_length];
    System.arraycopy(profile_bot, 0, front_profile_bot, 0, profile_length);
    System.arraycopy(profile_top, 0, front_profile_top, 0, profile_length);

    filter_window = fw;

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

  }

  // FrontManipulationRendererJ3D button release
  public void release() {
    Data data = curve_ref.getData();
    if (data == null || !(data instanceof Gridded2DSet)) {
      if (debug) System.out.println("data null or not Gridded2DSet");
      return;
    }
    Gridded2DSet curve_set = (Gridded2DSet) data;
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
/* ??
    float lat_mul = (float) ((lat_range[1] - lat_range[0]) / 2.0);
    float lon_mul = (float) ((lon_range[1] - lon_range[0]) / 2.0);
*/

    // transform curve to graphics coordinates
    // in order to "draw" front in graphics coordinates, then
    // transform back to (lat, lon)
    float[][] curve = new float[2][];
    curve[0] = lat_map.scaleValues(curve_samples[lat_index]);
    curve[1] = lon_map.scaleValues(curve_samples[lon_index]);
    // inverseScaleValues

    // resample curve uniformly along length
    float increment = segment_length / profile_length;
    curve = resample_curve(curve, increment);

    // lowpass filter curve
    curve = smooth_curve(curve, filter_window);

    // resample smoothed curve
    curve = resample_curve(curve, increment);



/*
  private int profile_length = -1;
  private float[] front_profile_bot = null;
  private float[] front_profile_top = null;
  private float segment_length;
*/
  }

  public float[][] smooth_curve(float[][] curve, int window) {
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
  public float[][] resample_curve(float[][] curve, float increment) {
    int len = curve[0].length;
    float curve_length = 0.0f;
    float[] seg_length = new float[len-1];
    for (int i=0; i<len-1; i++) {
      seg_length[i] = (float) Math.sqrt( 
        ((curve[0][i+1] - curve[0][i]) * (curve[0][i+1] - curve[0][i])) +
        ((curve[1][i+1] - curve[1][i]) * (curve[1][i+1] - curve[1][i])));
      curve_length += seg_length[i];
    }
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

    float[] bot_profile = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    float[] top_profile = {0.025f, 0.025f, 0.050f, 0.025f, 0.025f};
    FrontDrawer fd =
      new FrontDrawer(curve_ref, display, bot_profile, top_profile, 0.1f, 2);

    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));
    button_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    button_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    FrontActionListener fal = new FrontActionListener(fd, curve_ref);
    JButton end = new JButton("end manip");
    end.addActionListener(fal);
    end.setActionCommand("end");
    button_panel.add(end);
    JButton add = new JButton("add to track");
    add.addActionListener(fal);
    add.setActionCommand("add");
    button_panel.add(add);
    panel.add(button_panel);

    // set size of JFrame and make it visible
    frame.setSize(500, 700);
    frame.setVisible(true);
  }
}

class FrontActionListener implements ActionListener {
  FrontDrawer fd;
  DataReferenceImpl track_ref;

  FrontActionListener(FrontDrawer f, DataReferenceImpl tr) {
    fd = f;
    track_ref = tr;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("end")) {
/*
      try {
        // change
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
*/
    }
    else if (cmd.equals("add")) {
/*
      try {
        // change
      }
      catch (VisADException ex) {
        ex.printStackTrace();
      }
      catch (RemoteException ex) {
        ex.printStackTrace();
      }
*/
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
  public synchronized void release_direct() {
    fd.release();
  }
}

