//
// TestROMS.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.cluster;

import visad.*;
import visad.java3d.*;
import visad.util.*;
import visad.data.netcdf.Plain;
import visad.data.mcidas.*;

import java.rmi.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;

/**
cd /home1/billh/mead
java -mx128m visad.cluster.TestROMS NE_Pacific_2.nc roms_his_NEP2.nc
*/
public class TestROMS {

  private RemoteDataReferenceImpl remote_ref = null;

  public TestROMS(String[] args)
         throws VisADException, RemoteException, IOException {

    if (args == null || args.length < 2) {
      System.out.println(
        "usage: 'java visad.cluster.TestROMS grid_file history_file'");
    }

    // read grid and history files
    Plain plain = new Plain();
    Tuple grid_file = (Tuple) plain.open(args[0]);
    if (grid_file == null) {
      System.out.println("cannot open " + args[0]);
      return;
    }
    Tuple history_file = (Tuple) plain.open(args[1]);
    if (history_file == null) {
      System.out.println("cannot open " + args[1]);
      return;
    }

    // extract lat-lon arrays from grid file
    FlatField rho_ff = (FlatField) grid_file.getComponent(16);
    float[][] rho_floats = rho_ff.getFloats(false);
    float[][] rho_latlon = {rho_floats[8], rho_floats[9]};

    int n = rho_latlon[1].length;
/* not needed: adjustLongitude() does this implicitly
    for (int i=0 ;i<n; i++) {
      if (rho_latlon[1][i] < 0.0f) rho_latlon[1][i] += 360.0f;
    }
*/

    int[] rho_lengths = ((GriddedSet) rho_ff.getDomainSet()).getLengths();
    rho_ff = null;
    rho_floats = null;
    FlatField psi_ff = (FlatField) grid_file.getComponent(17);
    float[][] psi_floats = psi_ff.getFloats(false);
    float[][] psi_latlon = {psi_floats[2], psi_floats[3]};
    int[] psi_lengths = ((GriddedSet) psi_ff.getDomainSet()).getLengths();
    psi_ff = null;
    psi_floats = null;
    FlatField u_ff = (FlatField) grid_file.getComponent(18);
    float[][] u_floats = u_ff.getFloats(false);
    float[][] u_latlon = {u_floats[2], u_floats[3]};
    int[] u_lengths = ((GriddedSet) u_ff.getDomainSet()).getLengths();
    u_ff = null;
    u_floats = null;
    FlatField v_ff = (FlatField) grid_file.getComponent(19);
    float[][] v_floats = v_ff.getFloats(false);
    float[][] v_latlon = {v_floats[2], v_floats[3]};
    int[] v_lengths = ((GriddedSet) v_ff.getDomainSet()).getLengths();
    v_ff = null;
    v_floats = null;
    grid_file = null;

    // extract history data
    FieldImpl t1 = (FieldImpl) history_file.getComponent(31);
    Set time_set = t1.getDomainSet();
    int ntimes = time_set.getLength();
System.out.println("ntimes = " + ntimes);
    FunctionType t1type = (FunctionType) t1.getType();
    RealType time = (RealType) (t1type.getDomain()).getComponent(0);
    TupleType t1range = (TupleType) t1type.getRange();

    // FunctionType grid_type = (FunctionType) t1range.getComponent(6);
    // grid_type = ((xi_rho, eta_rho, s_rho) -> (w, temp, salt, rho))

    FunctionType grid_type = (FunctionType) t1range.getComponent(1);
    // grid_type = ((xi_rho, eta_rho) -> zeta)
    RealType zeta = (RealType) grid_type.getRange();
    RealTupleType grid_domain = grid_type.getDomain();
    RealType xi_rho = (RealType) grid_domain.getComponent(0);
    RealType eta_rho = (RealType) grid_domain.getComponent(1);

    RealTupleType latlon_type = RealTupleType.LatitudeLongitudeTuple;
    FunctionType grid_latlon_type =
      new FunctionType(latlon_type, zeta);

    FunctionType history_type = new FunctionType(time, grid_latlon_type);
    // history_type = (Time -> ((Latitude, Longitude) -> zeta))
    // FunctionType history_type = new FunctionType(time, grid_type);
    // history_type = (Time -> ((xi_rho, eta_rho) -> zeta))
    Real[] ocean_times = new Real[ntimes];
    double[][] times = new double[1][ntimes];
    for (int itime=0; itime<ntimes; itime++) {
      Tuple t1tuple = (Tuple) t1.getSample(itime);
      ocean_times[itime] = (Real) t1tuple.getComponent(0);
      times[0][itime] = ocean_times[itime].getValue();
    }
    Unit[] tunits = {ocean_times[0].getUnit()};
    Gridded1DDoubleSet dset =
      new Gridded1DDoubleSet(time, times, ntimes, null,
                             tunits, null, false);
    FieldImpl history = new FieldImpl(history_type, dset);

    Gridded2DSet grid_set =
      new Gridded2DSet(latlon_type, rho_latlon,
                       rho_lengths[0], rho_lengths[1], null, null,
                       null, false, false);

    for (int itime=0; itime<ntimes; itime++) {
      Tuple t1tuple = (Tuple) t1.getSample(itime);
      Real ocean_time = (Real) t1tuple.getComponent(0);
      FlatField grid = (FlatField) t1tuple.getComponent(1);
      FlatField grid_latlon = new FlatField(grid_latlon_type, grid_set);
      grid_latlon.setSamples(grid.getFloats(false), false);
      history.setSample(itime, grid_latlon);
    }

    // create the display
    DisplayImpl display = new DisplayImplJ3D("TestROMS");
    ScalarMap tmap = new ScalarMap(time, Display.Animation);
    display.addMap(tmap);
    ScalarMap xmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    display.addMap(xmap);
    xmap.setRange(160.0, 245.0);
    ScalarMap ymap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(ymap);
    ymap.setRange(20.0, 80.0);
    ScalarMap zmap = new ScalarMap(zeta, Display.ZAxis);
    display.addMap(zmap);
    ScalarMap rgbmap = new ScalarMap(zeta, Display.RGB);
    display.addMap(rgbmap);

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);
    mode.setTextureEnable(false);
    mode.setProjectionPolicy(DisplayImplJ3D.PARALLEL_PROJECTION);

    BaseMapAdapter baseMapAdapter = new BaseMapAdapter("OUTLSUPW");
    // baseMapAdapter.setLatLonLimits(0.0f, 90.0f, -180.0f, 180.0f);
    Data map = baseMapAdapter.getData();
    DataReference maplinesRef = new DataReferenceImpl("MapLines");
    maplinesRef.setData(map);
    ConstantMap[] maplinesConstantMap = new ConstantMap[]
      {new ConstantMap(0.4, Display.ZAxis)};
    display.addReference(maplinesRef, maplinesConstantMap);

    DataReference dr = new DataReferenceImpl("history");
    dr.setData(history);
    display.addReference(dr);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("TestROMS");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    frame.getContentPane().add(panel);

    // add display to JPanel
    panel.add(display.getComponent());

    panel.add(new AnimationWidget(tmap, 1000));

    // set size of JFrame and make it visible
    frame.setSize(600, 900);
    frame.setVisible(true);
  }

  public static void main(String[] args)
         throws RemoteException, VisADException, IOException {
    TestROMS roms = new TestROMS(args);
  }

}

/*
java visad.jmet.DumpType NE_Pacific_2.nc
NE_Pacific_2.nc:
0  (xl,
1   el,
2   (two -> PLAT),
3   PLONG,
4   ROTA,
5   P1,
6   P2,
7   P3,
8   P4,
9   XOFF,
10  YOFF,
11  depthmin,
12  depthmax,
13  f0,
14  dfdy,
15  ((xi_rho, eta_rho, bath) -> hraw),
16  ((xi_rho, eta_rho) -> (h, f, pm, pn, dndx, dmde, x_rho, y_rho, lat_rho, lon_rho, mask_rho, Angle)),
17  ((xi_psi, eta_psi) -> (x_psi, y_psi, lat_psi, lon_psi, mask_psi)),
18  ((xi_u, eta_u) -> (x_u, y_u, lat_u, lon_u, mask_u)),
19  ((xi_v, eta_v) -> (x_v, y_v, lat_v, lon_v, mask_v)))
. . .


java visad.jmet.DumpType roms_his_NEP2.nc
roms_his_NEP2.nc: 
0  (ntimes,
1   ndtfast,
2   dt,
3   dtfast, 
4   dstart,
5   nhis,
6   nrst,
7   ntsavg,
8   navg,
9   (tracer -> (tnu2, Akt_bak, Tnudg)),
10  visc2,
11  Akv_bak,
12  rdrg,
13  rdrg2,
14  Zob,
15  Zos,
16  Znudg,
17  M2nudg,
18  M3nudg,
19  (boundary -> (FSobc_in, FSobc_out, M2obc_in, M2obc_out, M3obc_in, M3obc_out)),
20  ((boundary, tracer) -> (Tobc_in, Tobc_out)),
21  rho0,
22  gamma2,
23  xl,
24  el,
25  theta_s,
26  theta_b,
27  Tcline,
28  hc,
29  (s_rho -> (sc_r, Cs_r, Lev)), 
30  (s_w -> (sc_w, Cs_w)),
31  (Time -> (ocean_time,
  1           ((xi_rho, eta_rho) -> zeta), 
  2           ((xi_u, eta_u) -> (ubar, sustr)),
  3           ((xi_v, eta_v) -> (vbar, svstr)),
  4           ((xi_u, eta_u, s_rho) -> u),
  5           ((xi_v, eta_v, s_rho) -> v),
  6           ((xi_rho, eta_rho, s_rho) -> (w, temp, salt, rho)),
  7           ((xi_rho, eta_rho, s_w) -> omega))))

*/

