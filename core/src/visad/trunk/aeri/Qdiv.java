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

/*
RH looks interesting up to 10000 m
MR better near boundary layer (but tighter color range)
*/

package visad.aeri;

import visad.*;
import visad.util.*;
import visad.DisplayImpl;
import visad.java3d.DisplayImplJ3D;
import visad.data.netcdf.*;
import visad.bom.WindPolarCoordinateSystem;
import visad.data.mcidas.BaseMapAdapter;
import visad.data.mcidas.AreaForm;
import visad.data.visad.VisADForm;
import visad.data.hdfeos.PolarStereographic;
import visad.data.mcidas.AreaAdapter;
import visad.meteorology.ImageSequenceManager;
import visad.meteorology.NavigatedImage;
import visad.meteorology.ImageSequence;
import visad.meteorology.SingleBandedImage;
import java.rmi.RemoteException;
import java.io.IOException;
import java.lang.Math;
import java.io.File;

// JFC packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

public class Qdiv
       implements ScalarMapListener
{
  RealType latitude;
  RealType longitude;
  RealType altitude;

  //- (lon,lat,alt)
  //
  RealTupleType spatial_domain;
  RealType time;
  RealType stn_idx;

  RealType temp;
  RealType dwpt;
  RealType wvmr;
  RealType RH;
  RealType theta;
  RealType thetaE;
  RealType u_wind;
  RealType v_wind;
  RealType wvmr_u;
  RealType wvmr_v;
  RealType band1;
  RealType div_qV;
  RealType q_divV;
  RealType qAdvct;

  RealTupleType wind_aeri;
  FunctionType alt_to_wind_aeri;
  FunctionType time_to_alt_to_wind_aeri;
  FunctionType alt_to_divqV;
  FunctionType time_to_alt_to_divqV;

  FieldImpl advect_field;
  FieldImpl stations_field;
  FieldImpl qfluxDiv;
  ImageSequence image_seq;
  FlatField test_image;

  int n_stations = 3;

  double[] station_lat;
  double[] station_lon;
  double[] station_alt;
  double[] station_id;
  double[] stat_xoffset = new double[n_stations];
  double[] stat_yoffset = new double[n_stations];

  BaseMapAdapter baseMap;
  DataReference map_ref;

  ScalarMap xmap;
  ScalarMap ymap;
  ScalarMap zmap;
  boolean xmapEvent = false;
  boolean ymapEvent = false;
  boolean firstEvent = false;

  float latmin, latmax;
  float lonmin, lonmax;
  float del_lat, del_lon;
  double[] x_range, y_range;

  int height_limit = 3000; // meters
  boolean rh = false;
  boolean tm = false;
  boolean pt = false;
  boolean ept = false;

  int start_date = 0;
  double start_time = 0.0;

  double[] scale_offset_x = new double[2];
  double[] scale_offset_y = new double[2];

  public static void main(String args[])
         throws VisADException, RemoteException, IOException
  {
    Qdiv qdiv = new Qdiv(args);
  }

  public Qdiv(String[] args)
         throws VisADException, RemoteException, IOException
  {
    String vadfile = null;
    String baseDate = "19991226";
    for (int i=0; i<args.length; i++) {
      if (args[i] == null) {
      }
      else if (args[i].endsWith(".vad")) {
        vadfile = args[i];
      }
      else if (args[i].equals("-limit") && (i+1) < args.length) {
        try {
          height_limit = Integer.parseInt(args[i+1]);
        }
        catch (NumberFormatException e) {
          System.out.println("bad height limit: " + args[i+1]);
        }
        i++;
      }
      else if (args[i].equals("-date") && (i+1) < args.length) {
        baseDate = args[i+1];
        i++;
      }
      else if (args[i].equals("-rh")) {
        rh = true;
        tm = false;
      }
      else if (args[i].equals("-temp")) {
        tm = true;
        rh = false;
      }
      else if (args[i].equals("-theta")) {
        pt = true;
      }
      else if (args[i].equals("-thetaE")) {
        ept = true;
      }
    }
    if (vadfile != null) {
      init_from_vad(vadfile);
    }
    else {
      init_from_cdf(baseDate);
    }
    wvmr.alias("MR");
    temp.alias("T");

    init_images(null);

    JFrame frame = new JFrame("VisAD AERI/QDIV Viewer");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    // panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    // panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    DisplayImpl display = makeDisplay(panel);

    int WIDTH = 1000;
    int HEIGHT = 600;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);
  }

  void init_from_cdf(String baseDate)
       throws VisADException, RemoteException, IOException
  {
    station_lat = new double[n_stations];
    station_lon = new double[n_stations];
    station_alt = new double[n_stations];
    station_id = new double[n_stations];

    longitude = RealType.Longitude;
    latitude = RealType.Latitude;
    RH = new RealType("RH", SI.second, null);
    stn_idx = new RealType("stn_idx", null, null);
    theta = new RealType("theta", null, null);
    thetaE = new RealType("thetaE", null, null);
    u_wind = new RealType("u_wind", null, null);
    v_wind = new RealType("v_wind", null, null);
    wvmr_u = new RealType("wvmr_u", null, null);
    wvmr_v = new RealType("wvmr_v", null, null);
    div_qV = new RealType("div_qV", null, null);
    q_divV = new RealType("q_divV", null, null);
    qAdvct = new RealType("qAdvct", null, null);

    String[] wind_files = new String[n_stations];
    String[] rtvl_files = new String[n_stations];

    String truncatedDate = baseDate;
    /**
    wind_files[0] = "./data/" + baseDate + "_415wind_lamont.cdf";
    wind_files[1] = "./data/" + baseDate + "_415wind_vici.cdf";
    wind_files[2] = "./data/" + baseDate + "_415wind_purcell.cdf";
    **/
    wind_files[0] = "./data/" + baseDate + "_lamont_windprof.cdf";
    wind_files[1] = "./data/" + baseDate + "_vici_windprof.cdf";
    wind_files[2] = "./data/" + baseDate + "_purcell_windprof.cdf";
    /**
    wind_files[3] = "./data/" + baseDate + "_hillsboro_windprof.cdf";
    wind_files[4] = "./data/" + baseDate + "_morris_windprof.cdf";
    **/

    rtvl_files[0] = "./data/lamont_" + truncatedDate + "AG.cdf";
    rtvl_files[1] = "./data/vici_" + truncatedDate + "AG.cdf";
    rtvl_files[2] = "./data/purcell_" + truncatedDate + "AG.cdf";
    /**
    rtvl_files[3] = "./data/hillsboro_" + truncatedDate + "AG.cdf";
    rtvl_files[4] = "./data/morris_" + truncatedDate + "AG.cdf";
    **/

    FieldImpl[] winds = makeWinds(wind_files);

    FieldImpl[] rtvls = makeAeri(rtvl_files);

       System.out.println(winds[0].getType().prettyString());
       System.out.println(rtvls[0].getType().prettyString());

    RealType[] r_types = {u_wind, v_wind, temp, dwpt, wvmr, wvmr_u, wvmr_v, div_qV, qAdvct};
    wind_aeri = new RealTupleType(r_types);
    alt_to_wind_aeri = new FunctionType(altitude, wind_aeri);
    time_to_alt_to_wind_aeri = new FunctionType(RealType.Time, alt_to_wind_aeri);
    spatial_domain = new RealTupleType(longitude, latitude, altitude);

    RealType[] qv_types = {div_qV, q_divV};
    alt_to_divqV = new FunctionType(altitude, new RealTupleType(qv_types));
    time_to_alt_to_divqV = new FunctionType(RealType.Time, alt_to_divqV);

    stations_field =
        new FieldImpl(new FunctionType( stn_idx, time_to_alt_to_wind_aeri),
                                        new Integer1DSet( stn_idx, n_stations,
                                                          null, null, null));
       System.out.println("----------------");

    for ( int kk = 0; kk < n_stations; kk++ )
    {
      FieldImpl field = make_wind_aeri(winds[kk], rtvls[kk]);
      stations_field.setSample(kk, field);
    }
    System.out.println(stations_field.getType().prettyString());

    qfluxDiv = makeDivqV(stations_field);
    System.out.println("makeDivqV: Done");
    

  /**-
    VisADForm vad_form = new VisADForm();
    vad_form.save("aeri_winds_" + baseDate + "." +
                  height_limit + ".vad", stations_field, true);
   **/
  }

  void init_from_vad( String vad_file )
       throws VisADException, RemoteException, IOException
  {
    VisADForm vad_form = new VisADForm();
    stations_field = (FieldImpl) vad_form.open( vad_file );

    MathType file_type = stations_field.getType();

    stn_idx = (RealType)
      ((RealTupleType)((FunctionType)file_type).getDomain()).getComponent(0);
    FunctionType f_type0 = (FunctionType) ((FunctionType)file_type).getRange();
    time = (RealType) ((RealTupleType)f_type0.getDomain()).getComponent(0);
    FunctionType f_type1 = (FunctionType) f_type0.getRange();
    spatial_domain = (RealTupleType) f_type1.getDomain();
    longitude = (RealType) spatial_domain.getComponent(0);
    latitude = (RealType) spatial_domain.getComponent(1);
    altitude = (RealType) spatial_domain.getComponent(2);

    RealTupleType rtt = (RealTupleType) f_type1.getRange();
    temp = (RealType) rtt.getComponent(0);
    dwpt = (RealType) rtt.getComponent(1);
    wvmr = (RealType) rtt.getComponent(2);
    RH = (RealType) rtt.getComponent(3);
    theta = (RealType) rtt.getComponent(4);
    thetaE = (RealType) rtt.getComponent(5);
  }

  void init_images(String image_directory)
       throws VisADException, RemoteException, IOException
  {
    String fs = System.getProperty("file.separator");

    if ( image_directory == null ) {
      image_directory = "."+fs+"data"+fs+"image";
    }

    File file = new File(image_directory);
    String[] image_files = file.list();
    int n_images = image_files.length;
    NavigatedImage[] nav_images = new NavigatedImage[n_images];

    for ( int ii = 0; ii < n_images; ii++ ) {
      AreaAdapter area = new AreaAdapter(image_directory+fs+image_files[ii]);
      FlatField image = area.getData();
      DateTime img_start = area.getImageStartTime();
      nav_images[ii] = new NavigatedImage(image, img_start, "AREA");
    }
    test_image = nav_images[0];

    ImageSequenceManager img_manager = 
      new ImageSequenceManager(nav_images);

    band1 = (RealType) 
     ((RealTupleType)((FunctionType)nav_images[0].getType()).getRange()).getComponent(0);

    image_seq = img_manager.getImageSequence();
  }

  DisplayImpl makeDisplay(JPanel panel)
       throws VisADException, RemoteException, IOException
  {
    del_lon = 8.0f;
    del_lat = 8.0f;

    baseMap = new BaseMapAdapter("OUTLUSAM");
    map_ref = new DataReferenceImpl("map");

    if ( ! baseMap.isEastPositive() )
    {
      baseMap.setEastPositive(true);
    }

    //- make barber poles for each station
    //
    DataImpl poles = makePoles();
    DataReference poles_ref = new DataReferenceImpl("poles");
    poles_ref.setData(poles);

    DisplayImpl display = new DisplayImplJ3D("aeri");
    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);
    mode.setLineWidth(1.5f);
    DisplayRenderer dr = display.getDisplayRenderer();
    dr.setBoxOn(false);

    xmap = new ScalarMap(longitude, Display.XAxis);
    xmap.setScaleEnable(false);
    xmap.addScalarMapListener(this);

    ymap = new ScalarMap(latitude, Display.YAxis);
    ymap.setScaleEnable(false);
    ymap.addScalarMapListener(this);

    double[] lon_mm = getArrayMinMax(station_lon);
    double[] lat_mm = getArrayMinMax(station_lat);
    baseMap.setLatLonLimits((float)(lat_mm[0]-del_lat), (float)(lat_mm[1]+del_lat),
                            (float)(lon_mm[0]-del_lon), (float)(lon_mm[1]+del_lon));
    DataImpl map = baseMap.getData();
    map_ref.setData(map);

    zmap = new ScalarMap(altitude, Display.ZAxis);

    display.addMap(xmap);
    display.addMap(ymap);
    display.addMap(zmap);

    ScalarMap flowx = new ScalarMap(u_wind, Display.Flow1X);
    ScalarMap flowy = new ScalarMap(v_wind, Display.Flow1Y);
 //-ScalarMap flowx = new ScalarMap(wvmr_u, Display.Flow1X);
 //-ScalarMap flowy = new ScalarMap(wvmr_v, Display.Flow1Y);
    display.addMap(flowx);
    display.addMap(flowy);
    FlowControl flow_cntrl = (FlowControl) flowx.getControl();
    flow_cntrl.setFlowScale(0.5f);
    flow_cntrl = (FlowControl) flowy.getControl();
    flow_cntrl.setFlowScale(0.5f);

    ScalarMap img_map = new ScalarMap(band1, Display.RGB);
    display.addMap(img_map);
    ColorControl cc = (ColorControl) img_map.getControl();
    cc.initGreyWedge();

    // note RH bonces around because temperature does
    ScalarMap cmap = null;
    if (rh) {
      cmap = new ScalarMap(RH, Display.RGB);
    }
    else if (tm) {
      cmap = new ScalarMap(temp, Display.RGB);
    }
    else if (pt) {
      cmap = new ScalarMap(theta, Display.RGB);
    }
    else if (ept) {
      cmap = new ScalarMap(thetaE, Display.RGB);
    }
    else {
      cmap = new ScalarMap(wvmr, Display.RGB);
    }
    cmap = new ScalarMap(div_qV, Display.RGB);
    display.addMap(cmap);

    ColorMapWidget cmw = new ColorMapWidget(cmap, null, true, false);
    LabeledColorWidget cwidget = new LabeledColorWidget(cmw);
    ScalarMap tmap = new ScalarMap(RealType.Time, Display.Animation);
    display.addMap(tmap);
    AnimationControl control = (AnimationControl) tmap.getControl();
    control.setStep(50);
    AnimationWidget awidget = new AnimationWidget(tmap);

    zmap.setRange(0.0, hgt_max);

    ConstantMap[] map_constMap =
      new ConstantMap[]
    {
      new ConstantMap(1., Display.Red),
      new ConstantMap(1., Display.Green),
      new ConstantMap(1., Display.Blue),
      new ConstantMap(-.98, Display.ZAxis)
    };

    ConstantMap[] img_constMap = 
      new ConstantMap[] {new ConstantMap(-.99, Display.ZAxis)};

 //-display.disableAction();
    display.addReference(poles_ref);
    display.addReference(map_ref, map_constMap);
 
    ConstantMap[] c_maps = new ConstantMap[2];
    for ( int kk = 0; kk < n_stations; kk++ ) {
      double display_x = station_lon[kk]*scale_offset_x[0] + scale_offset_x[1];
      c_maps[0] = new ConstantMap( display_x, Display.XAxis);
      double display_y = station_lat[kk]*scale_offset_y[0] + scale_offset_y[1];
      c_maps[1] = new ConstantMap( display_y, Display.YAxis);
      DataReference station_ref = new DataReferenceImpl("station: "+kk);
   //-station_ref.setData(stations_field.getSample(kk));
      station_ref.setData(qfluxDiv.getSample(kk));
      display.addReference(station_ref, c_maps);
    }

    DataReference img_ref = new DataReferenceImpl("image");
    img_ref.setData(image_seq.getImage(0));
 //-img_ref.setData(test_image);
    display.addReference(img_ref, img_constMap);

 //-display.enableAction();

    JPanel dpanel = new JPanel();
    dpanel.setLayout(new BoxLayout(dpanel, BoxLayout.Y_AXIS));
    dpanel.add(display.getComponent());

    JPanel wpanel = new JPanel();
    wpanel.setLayout(new BoxLayout(wpanel, BoxLayout.Y_AXIS));
    cwidget.setMaximumSize(new Dimension(400, 200));
    awidget.setMaximumSize(new Dimension(400, 400));
    wpanel.add(cwidget);
    wpanel.add(awidget);
    Dimension d = new Dimension(400, 600);
    wpanel.setMaximumSize(d);
    panel.add(wpanel);
    panel.add(dpanel);

    return display;
  }

  public static double[] getArrayMinMax(double[] array)
  {
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    double[] min_max = new double[2];

    for (int ii = 0; ii < array.length; ii++) {
     if (array[ii] > max) max = array[ii];
     if (array[ii] < min) min = array[ii];
    }
    min_max[0] = min;
    min_max[1] = max;
    
    return min_max;
  }

  double hgt_max = -Double.MAX_VALUE;

  DataImpl makePoles()
           throws VisADException, RemoteException
  {
    SampledSet[] set_s = new SampledSet[n_stations];
    int ii = 0;
    float[][] locs = new float[3][2];

    for ( int kk = 0; kk < n_stations; kk++ ) {
      boolean any = false;
      float hgt = -Float.MAX_VALUE;
      FieldImpl station = (FieldImpl) stations_field.getSample(kk);
      if (station == null) continue;
      int len = station.getLength();
      for (int i=0; i<len; i++) {
        FieldImpl pole = (FieldImpl) station.getSample(i);
        if (pole == null || pole.getLength() < 2) continue;
        Set set = pole.getDomainSet();
        float[][] samples = set.getSamples(false);
        // float[] lo = ((SampledSet)set).getLow();
        float[] hi = ((SampledSet)set).getHi();
        if (hi[0] > hgt) hgt = hi[0];
        if (!any && samples[0][0] == samples[0][0])
        {
          any = true;
          locs[0][0] = (float) station_lon[kk];
          locs[1][0] = (float) station_lat[kk];
          locs[0][1] = locs[0][0];
          locs[1][1] = locs[1][0];
        }
      }
      if (any) {
        locs[2][0] = 0.0f;
        locs[2][1] = hgt;
        set_s[ii++] = new Gridded3DSet(spatial_domain, locs, 2, null, null, null);
// System.out.println("set_s[" + kk + "] = " + set_s[kk]);
        if (hgt > hgt_max) hgt_max = hgt;
      }
    }
    SampledSet[] set_ss = new SampledSet[ii];
    System.arraycopy(set_s, 0, set_ss, 0, ii);
    return new UnionSet(spatial_domain, set_ss);
  }

  public void mapChanged(ScalarMapEvent e)
       throws VisADException, RemoteException
  {
    if ( xmap.equals(e.getScalarMap()) ) {
      xmapEvent = true;
    }
    else if ( ymap.equals(e.getScalarMap()) ) {
      ymapEvent = true;
    }
    if (( xmapEvent && ymapEvent ) && !(firstEvent) ) {
      double[] minmax = getArrayMinMax(station_lat);
      latmin = (float)minmax[0];
      latmax = (float)minmax[1];

      minmax = getArrayMinMax(station_lon);
      lonmin = (float) minmax[0];
      lonmax = (float) minmax[1];

      firstEvent = true;
      xmap.setRange(lonmin, lonmax);
      ymap.setRange(latmin, latmax);
      double[] so = new double[2];
      double[] data = new double[2];
      double[] display = new double[2];
      xmap.getScale(so, data, display);
      scale_offset_x[0] = so[0];
      scale_offset_x[1] = so[1];
      ymap.getScale(so, data, display);
      scale_offset_y[0] = so[0];
      scale_offset_y[1] = so[1];
    }
  }

  public void controlChanged(ScalarMapControlEvent e)
  {
  }

  FieldImpl[] makeWinds(String[] files)
              throws VisADException, RemoteException, IOException
  {
    DataImpl[] file_data = new DataImpl[n_stations];
    FieldImpl[] time_field = new FieldImpl[n_stations];
    double[][] time_offset = null;
    double[] base_date = new double[n_stations];
    double[] base_time = new double[n_stations];
    Gridded1DSet d_set = null;
    FlatField new_ff = null;

    RealType alt;
    RealType spd;
    RealType dir;

    //- create a new netcdf reader
    Plain plain = new Plain();

    //- retrieve file data objects
    for ( int kk = 0; kk < n_stations; kk++) {
      file_data[kk] = plain.open(files[kk]);
    }

    //- make sub mathtype for file objects
    MathType file_type = file_data[0].getType();
    System.out.println(file_type.prettyString());
    System.out.println();

    FunctionType f_type0 =
      (FunctionType)((TupleType)file_type).getComponent(2);

    int n_comps = ((TupleType)f_type0.getRange()).getDimension();
    FunctionType f_type1 =
      (FunctionType)((TupleType)f_type0.getRange()).getComponent(n_comps-1);

    RealTupleType rt_type = (RealTupleType) f_type1.getRange();
    int alt_idx = rt_type.getIndex("Altitude");
    altitude = (RealType) rt_type.getComponent(alt_idx);
    int ws_idx = rt_type.getIndex("windSpeed");
    spd = (RealType) rt_type.getComponent(ws_idx);
    int wd_idx = rt_type.getIndex("windDir");
    dir = (RealType) rt_type.getComponent(wd_idx);

    RealType[] r_types = { dir, spd };

    /* WLH 28 Dec 99 */
    RealType[] uv_types = { u_wind, v_wind };
    // EarthVectorType uv = new EarthVectorType(uv_types); WLH meeds m/s
    RealTupleType uv = new RealTupleType(uv_types);
    CoordinateSystem cs = new WindPolarCoordinateSystem(uv);
    RealTupleType ds = new RealTupleType(r_types, cs, null);


    FunctionType alt_to_ds = new FunctionType(altitude, ds);
    FunctionType alt_to_uv = new FunctionType(altitude, uv);

    RealType domain_type = (RealType)
      ((TupleType)f_type0.getRange()).getComponent(0);
    time = domain_type;
    FunctionType new_type = new FunctionType(RealType.Time, alt_to_uv);

    FieldImpl[] winds = new FieldImpl[n_stations];

    for ( int ii = 0; ii < n_stations; ii++ )
    {
      base_date[ii] = (double)
        ((Real)((Tuple)file_data[ii]).getComponent(0)).getValue();
      base_time[ii] = (double)
        ((Real)((Tuple)file_data[ii]).getComponent(1)).getValue();
      time_field[ii] = (FieldImpl)
        ((Tuple)file_data[ii]).getComponent(2);
      station_lat[ii] =
        ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(6)).getValue();
      station_lon[ii] =
        -((Real)((Tuple)time_field[ii].getSample(0)).getComponent(7)).getValue();
      station_alt[ii] =
        ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(8)).getValue();
      station_id[ii] =
        ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(9)).getValue();
      if (ii == 0) {
        start_time = base_time[0];
        start_date = (int) base_date[0];
      }

/*
System.out.println("wind " + ii + " date: " + base_date[ii] +
                   " time: " + base_time[ii]);
wind 0 date: 1.9991226E7 time: 9.461664E8
wind 1 date: 1.9991226E7 time: 9.461664E8
wind 2 date: 1.9991226E7 time: 9.461664E8
wind 3 date: 1.9991226E7 time: 9.461664E8
wind 4 date: 1.9991226E7 time: 9.461664E8

looks like 19991226 and seconds since 1-1-70
time_offset looks like seconds since 0Z
*/

      int length = time_field[ii].getLength();
      time_offset = new double[1][length];
      FlatField[] range_data = new FlatField[length];

      double[][] samples = null; // WLH
      for ( int jj = 0; jj < length; jj++ )
      {
        Tuple range = (Tuple) time_field[ii].getSample(jj);
        time_offset[0][jj] = (double)((Real)range.getComponent(0)).getValue();

        FlatField p_field = (FlatField) range.getComponent(n_comps-1);
        double[][] values =
          p_field.getValues(); // WLH - (alt, ?, ?, dir, spd, ???)
        double[][] new_values = new double[2][values[0].length];

        if ( jj == 0 )  //- only once, vertical range gates don't change
        {
          samples = new double[1][values[0].length]; // WLH
          System.arraycopy(values[alt_idx], 0, samples[0], 0, samples[0].length);
          d_set = new Gridded1DSet(altitude, Set.doubleToFloat(samples),
                                   samples[0].length);
        }
        new_ff = new FlatField(alt_to_uv, d_set);

/* WLH - fill in missing winds - this also extrapolates to missing
start or end altitudes - but it does nothing if all winds are missing */
        int n_not_miss = 0;
        int[] not_miss = new int[values[0].length];
        for ( int mm = 0; mm < values[0].length; mm++ )
        {
          if ( values[wd_idx][mm] <= -9999 ) {
            new_values[0][mm] = Float.NaN;
          }
          else {
            new_values[0][mm] = values[wd_idx][mm];
          }

          if ( values[ws_idx][mm] <= -9999 ) {
            new_values[1][mm] = Float.NaN;
          }
          else {
            new_values[1][mm] = values[ws_idx][mm];
          }

          if (new_values[0][mm] == new_values[0][mm] &&
              new_values[1][mm] == new_values[1][mm]) {
            not_miss[n_not_miss] = mm;
            n_not_miss++;
          }
        }
        if (0 < n_not_miss && n_not_miss < values[0].length) {
          int nn = n_not_miss;
          if (not_miss[0] > 0) nn += not_miss[0];
          int endlen = values[0].length - (not_miss[n_not_miss-1]+1);
          if (endlen > 0) nn += endlen;

          float[][] newer_values = new float[2][nn];
          float[][] newer_samples = new float[1][nn];
          // fill in non-missing values
          for (int i=0; i<n_not_miss; i++) {
            newer_values[0][not_miss[0] + i] =
              (float) new_values[0][not_miss[i]];
            newer_values[1][not_miss[0] + i] =
              (float) new_values[1][not_miss[i]];
            newer_samples[0][not_miss[0] + i] =
              (float) samples[0][not_miss[i]];
          }
          // extrapolate if necessary for starting values
          for (int i=0; i<not_miss[0]; i++) {
            newer_values[0][i] = (float) new_values[0][not_miss[0]];
            newer_values[1][i] = (float) new_values[1][not_miss[0]];
            newer_samples[0][i] = (float) samples[0][not_miss[0]];
          }
          // extrapolate if necessary for ending values
          for (int i=0; i<endlen; i++) {
            newer_values[0][not_miss[0] + n_not_miss + i] =
              (float) new_values[0][not_miss[n_not_miss-1]];
            newer_values[1][not_miss[0] + n_not_miss + i] =
              (float) new_values[1][not_miss[n_not_miss-1]];
            newer_samples[0][not_miss[0] + n_not_miss + i] =
              (float) samples[0][not_miss[n_not_miss-1]];
          }
          Gridded1DSet newer_d_set =
            new Gridded1DSet(altitude, newer_samples, nn);
          FlatField newer_ff = new FlatField(alt_to_uv, newer_d_set);
          newer_ff.setSamples(cs.toReference(newer_values));
          new_ff = (FlatField) newer_ff.resample(d_set,
                                                 Data.WEIGHTED_AVERAGE,
                                                 Data.NO_ERRORS );
        }
        else {
          new_ff.setSamples(cs.toReference(new_values));
        }
/* end WLH - fill in missing winds */

        range_data[jj] = new_ff;
      }


      Gridded1DSet domain_set = new Gridded1DSet(domain_type,
                                    Set.doubleToFloat(time_offset), length);

/* resample() doesn't work for doubles
      double[][] times = new double[1][length];
      for (int i=0; i<length; i++) {
        times[0][i] = base_time[0] + time_offset[0][i];
      }
      Gridded1DDoubleSet domain_set =
        new Gridded1DDoubleSet(domain_type, times, length);
*/

      winds[ii] = new FieldImpl(new_type, domain_set);
      winds[ii].setSamples(range_data, false);
    }
    plain = null;
    return winds;
  }

  FieldImpl[] makeAeri(String[] files)
              throws VisADException, RemoteException, IOException
  {
    DataImpl[] file_data = new DataImpl[n_stations];
    FieldImpl[] time_field = new FieldImpl[n_stations];
    double[][] time_offset = null;
    double[] base_date = new double[n_stations];
    double[] base_time = new double[n_stations];

    //- create a new netcdf reader
    Plain plain = new Plain();

    //- retrieve file data objects
    for ( int kk = 0; kk < n_stations; kk++ ) {
      file_data[kk] = plain.open(files[kk]);
    }
    System.out.println(file_data[0].getType().prettyString());

    //- make sub mathtype for file objects
    MathType file_type = file_data[0].getType();
    FunctionType f_type0 = (FunctionType)
      ((TupleType)file_type).getComponent(1);
    FunctionType f_type1 = (FunctionType)
      ((TupleType)f_type0.getRange()).getComponent(1);

    RealTupleType rtt = (RealTupleType) f_type1.getRange();
    temp = (RealType) rtt.getComponent(1);
    dwpt = (RealType) rtt.getComponent(2);
    wvmr = (RealType) rtt.getComponent(3);


    RealType domain_type = (RealType)
      ((TupleType)f_type0.getRange()).getComponent(0);
    FunctionType new_type = new FunctionType(RealType.Time, f_type1);

    FieldImpl[] rtvls = new FieldImpl[n_stations];


    for ( int ii = 0; ii < n_stations; ii++ )
    {
      base_time[ii] = (double)
        ((Real)((Tuple)file_data[ii]).getComponent(0)).getValue();
      time_field[ii] = (FieldImpl) ((Tuple)file_data[ii]).getComponent(1);
      base_date[ii] = (double)
        ((Real)((Tuple)file_data[ii]).getComponent(2)).getValue();
/*
System.out.println("aeri " + ii + " date: " + base_date[ii] +
                   " time: " + base_time[ii]);
aeri 0 date: 991226.0 time: 9.46166724E8
aeri 1 date: 991226.0 time: 9.46167982E8
aeri 2 date: 991226.0 time: 9.4616806E8
aeri 3 date: 991226.0 time: 9.46168061E8
aeri 4 date: 991226.0 time: 9.4616807E8

looks like 991226 and seconds since 1-1-70
time_offset looks like seconds since 0Z
*/

      int length = time_field[ii].getLength();
      time_offset = new double[1][length];
      Data[] range_data = new Data[length];

      for ( int jj = 0; jj < length; jj++ )
      {
        Tuple range = (Tuple) time_field[ii].getSample(jj);
        time_offset[0][jj] = (double)((Real)range.getComponent(0)).getValue();

        FlatField p_field = (FlatField) range.getComponent(1);
        double[][] values = p_field.getValues();
        double[][] new_values = new double[4][values[0].length];

        for ( int mm = 0; mm < values[0].length; mm++ )
        {
          if ( values[0][mm] == -9999 ) {
            new_values[0][mm] = Float.NaN;
          }
          else {
            new_values[0][mm] = values[0][mm];
          }

          if ( values[1][mm] == -9999 ) {
            new_values[1][mm] = Float.NaN;
          }
          else {
            new_values[1][mm] = values[1][mm];
          }

          if ( values[2][mm] == -9999 ) {
            new_values[2][mm] = Float.NaN;
          }
          else {
            new_values[2][mm] = values[2][mm];
          }

          if ( values[3][mm] == -9999 ) {
            new_values[3][mm] = Float.NaN;
          }
          else {
            new_values[3][mm] = values[3][mm];
          }
        }
        p_field.setSamples(new_values);
        if (!f_type1.equals(p_field.getType())) {
          p_field = (FlatField) p_field.changeMathType(f_type1);
        }
        range_data[jj] = p_field;
      }

      Gridded1DSet domain_set = new Gridded1DSet(domain_type,
                                    Set.doubleToFloat(time_offset), length);
/* resample() doesn't work for doubles
      double[][] times = new double[1][length];
      for (int i=0; i<length; i++) {
        times[0][i] = base_time[0] + time_offset[0][i];
      }
      Gridded1DDoubleSet domain_set =
        new Gridded1DDoubleSet(domain_type, times, length);
*/

      rtvls[ii] = new FieldImpl(new_type, domain_set);
      rtvls[ii].setSamples(range_data, false);
    }
    return rtvls;
  }

  FieldImpl make_wind_aeri( FieldImpl winds, FieldImpl rtvls )
            throws VisADException, RemoteException, IOException
  {
    float wind_time;
    float[][] value_s = new float[1][1];
    int[] index_s = new int[1];
    int rtvl_idx;
 //-FlatField alt_to_wind;
    FieldImpl alt_to_wind;
    FieldImpl wind_to_rtvl_time;
 //-FlatField alt_to_rtvl;
    FieldImpl alt_to_rtvl;
    FlatField advect;
    FieldImpl advect_field;
    FlatField rtvl_on_wind;
    FieldImpl wind_on_wind;
    int n_samples;
    double age;
    double age_max = 3600;
    double rtvl_intvl;
    double rtvl_time;
    double rtvl_time_0;
    double rtvl_intvl_min = 476;
    double[][] dir_spd;
    double[][] uv_wind;
    int idx = 0;
    float alt;
    Set rtvls_domain;
    float[] rtvl_times;
    double[][] new_values;

    //- time(rtvl) -> (alt) -> (U,V,T,TD,WV)
    //
    rtvls_domain = rtvls.getDomainSet();
    FieldImpl time_wind_aeri = new FieldImpl(time_to_alt_to_wind_aeri, rtvls_domain);
    

    //- resample winds domain (time) to rtvls
    //

    wind_to_rtvl_time = (FieldImpl)
      winds.resample( rtvls_domain,
                      Data.WEIGHTED_AVERAGE,
                      Data.NO_ERRORS );


    //- resample rtvls domain (altitude) to winds at each rtvl time
    //
    int dim = 
      ((RealTupleType)
       ((FunctionType)time_to_alt_to_wind_aeri.getRange()).getRange()).getDimension(); 
    new_values = new double[dim][];
    int len = rtvls.getLength();
    for ( int tt = 0; tt < len; tt++ ) 
    {
      alt_to_rtvl = (FieldImpl) rtvls.getSample(tt);
      alt_to_wind = (FieldImpl) wind_to_rtvl_time.getSample(tt);
      Set ds = alt_to_wind.getDomainSet();
// WLH height limit
      float[][] samples = ds.getSamples();
      float[] ns = new float[samples[0].length];
      int nn = 0;
      for (int i=0; i<samples[0].length; i++) {
        if (samples[0][i] < height_limit) {
          ns[nn] = samples[0][i];
          nn++;
        }
      }
      float[][] new_samples = new float[1][nn];
      System.arraycopy(ns, 0, new_samples[0], 0, nn);
      ds = new Gridded1DSet(ds.getType(), new_samples, nn);
      wind_on_wind = (FieldImpl)
        alt_to_wind.resample( ds,
                              Data.WEIGHTED_AVERAGE,
                              Data.NO_ERRORS );
// end WLH height limit
      rtvl_on_wind = (FlatField)
        alt_to_rtvl.resample( ds,
                              Data.WEIGHTED_AVERAGE,
                              Data.NO_ERRORS );

      double[][] uv_values = wind_on_wind.getValues();
      double[][] rtvl_values = rtvl_on_wind.getValues();

      double[][] uv_wvmr = new double[2][nn];
      double[] dum1 = new double[nn];
      double[] dum2 = new double[nn];
      for (int ii = 0; ii < nn; ii++) {
        uv_wvmr[0][ii] = uv_values[0][ii]*rtvl_values[3][ii];
        uv_wvmr[1][ii] = uv_values[1][ii]*rtvl_values[3][ii];
        dum1[ii] = Double.NaN;
        dum2[ii] = Double.NaN;
      }

      new_values[0] = uv_values[0];
      new_values[1] = uv_values[1];
      new_values[2] = rtvl_values[1];
      new_values[3] = rtvl_values[2];
      new_values[4] = rtvl_values[3];
      new_values[5] = uv_wvmr[0];
      new_values[6] = uv_wvmr[1];
      new_values[7] = dum1;
      new_values[8] = dum2;

      FlatField ff = new FlatField(alt_to_wind_aeri, ds);
      ff.setSamples(new_values);

      time_wind_aeri.setSample(tt, ff);
    }

    return time_wind_aeri;
  }

  FieldImpl makeDivqV( FieldImpl stations )
            throws VisADException, RemoteException
  {
    double[][] uv_comps = new double[2][3];
    Set time_domain;

    double[][] lonlat = new double[2][3];
    lonlat[0][0] = station_lon[0]*Data.DEGREES_TO_RADIANS;
    lonlat[0][1] = station_lon[1]*Data.DEGREES_TO_RADIANS;
    lonlat[0][2] = station_lon[2]*Data.DEGREES_TO_RADIANS;
    lonlat[1][0] = station_lat[0]*Data.DEGREES_TO_RADIANS;
    lonlat[1][1] = station_lat[1]*Data.DEGREES_TO_RADIANS;
    lonlat[1][2] = station_lat[2]*Data.DEGREES_TO_RADIANS;

    LinearVectorPointMethod lvpm =
      new LinearVectorPointMethod(lonlat);

    FieldImpl[] stations_array = new FieldImpl[3];
    for (int kk = 0; kk < 3; kk++) {
      stations_array[kk] = (FieldImpl) stations.getSample(kk);
    }

    FieldImpl new_stations = new FieldImpl((FunctionType)stations.getType(),
                                            stations.getDomainSet());

    Set time_domain0 = stations_array[0].getDomainSet();
    float[] low0 = ((SampledSet)time_domain0).getLow();
    float[] hi0 = ((SampledSet)time_domain0).getHi();
    int length0 = time_domain0.getLength();
    System.out.println("low0: "+low0[0]+"  hi0: "+hi0[0]+" len0: "+length0);

    Set time_domain1 = stations_array[1].getDomainSet();
    float[] low1 = ((SampledSet)time_domain1).getLow();
    float[] hi1 = ((SampledSet)time_domain1).getHi();
    int length1 = time_domain1.getLength();
    System.out.println("low1: "+low1[0]+"  hi1: "+hi1[0]+" len1: "+length1);

    Set time_domain2 = stations_array[2].getDomainSet();
    float[] low2 = ((SampledSet)time_domain2).getLow();
    float[] hi2 = ((SampledSet)time_domain2).getHi();
    int length2 = time_domain2.getLength();
    System.out.println("low2: "+low2[0]+"  hi2: "+hi2[0]+" len2: "+length2);

    float lowest_hi = Math.min(Math.min(hi0[0], hi1[0]), hi2[0]);
    System.out.println("lowest_hi: "+lowest_hi);

    //-- find time domain with highest_lo
    int stn_idx = 2;
    int[] other = new int[2]; 
    other[0] = 0;
    other[1] = 1;

    if ((low0[0] > low2[0]) || (low1[0] > low2[0])) {
      if ( low0[0] >= low1[0] ) {
        stn_idx = 0;
        other[0] = 1;
        other[1] = 2;
      }
      else {
        stn_idx = 1;
        other[0] = 0;
        other[1] = 2;
      }
    }
    System.out.println("highest_lo index: "+stn_idx);

    Set d_set = stations_array[stn_idx].getDomainSet();
    float[][] times = d_set.getSamples();
    int cnt = 0;
    for ( int tt = 0; tt < times[0].length; tt++ ) {
      if ( times[0][tt] <= lowest_hi ) cnt++;
    }
    System.out.println("cnt: "+cnt);
    float[][] new_times = new float[1][cnt];
    System.arraycopy(times[0], 0, new_times[0], 0, cnt);
    MathType m_type = d_set.getType();
    RealType time = (RealType) (((SetType)m_type).getDomain()).getComponent(0);
    time_domain = new Gridded1DSet(m_type, new_times, new_times[0].length);

    FunctionType f_type = (FunctionType) stations_array[0].getType();
    FieldImpl station0 = new FieldImpl(f_type, time_domain);
    new_stations.setSample(0, station0, false);

    FieldImpl station1 = new FieldImpl(f_type, time_domain);
    new_stations.setSample(1, station1, false);

    FieldImpl station2 = new FieldImpl(f_type, time_domain);
    new_stations.setSample(2, station2, false);

    for ( int tt = 0; tt < time_domain.getLength(); tt++ )
    {
      Real real = new Real(time, times[0][tt]);
      System.out.println(times[0][tt]);

      FlatField field0 = (FlatField) stations_array[stn_idx].getSample(tt);
      System.out.println(field0.getDomainSet().getLength());

   //-FieldImpl field1 = (FieldImpl) stations_array[other[0]].evaluate(real);
      FieldImpl field1 = (FieldImpl) stations_array[other[0]].getSample(tt);
      System.out.println(field1.getDomainSet().getLength());

   //-FieldImpl field2 = (FieldImpl) stations_array[other[1]].evaluate(real);
      FieldImpl field2 = (FieldImpl) stations_array[other[1]].getSample(tt);
      System.out.println(field2.getDomainSet().getLength());

      double[][] values0 = field0.getValues(false);
      double[][] values1 = field1.getValues(false);
      double[][] values2 = field2.getValues(false);
      int len0 = values0[5].length;
      int len1 = values1[5].length;
      int len2 = values2[5].length;

      for ( int kk = 0; kk < Math.min(Math.min(len0,len1),len2); kk++ )
      {
        boolean any_missing = false;

        uv_comps[0][0] = values0[0][kk];
        uv_comps[1][0] = values0[1][kk];

        uv_comps[0][1] = values1[0][kk];
        uv_comps[1][1] = values1[1][kk];

        uv_comps[0][2] = values2[0][kk];
        uv_comps[1][2] = values2[1][kk];

        if (Double.isNaN(uv_comps[0][0]) || Double.isNaN(uv_comps[1][0])) {
          System.out.println(uv_comps[0][0]+"  "+uv_comps[1][0]);
          any_missing = true;
        }
        else if (Double.isNaN(uv_comps[0][1]) || Double.isNaN(uv_comps[1][1])) {
          System.out.println(uv_comps[0][1]+"  "+uv_comps[1][1]);
          any_missing = true;
        }
        else if (Double.isNaN(uv_comps[0][2]) || Double.isNaN(uv_comps[1][2])) {
          System.out.println(uv_comps[0][2]+"  "+uv_comps[1][2]);
          any_missing = true;
        }

        if ( ! any_missing ) {
          double[] kinematics = lvpm.getKinematics(uv_comps);
          values0[7][kk] = kinematics[4];
          values1[7][kk] = kinematics[4];
          values2[7][kk] = kinematics[4];
       //-System.out.println("kk: "+kk+"  "+kinematics[4]);
        }
        else {
        /*
          System.out.println("tt: "+tt);
          System.out.println("len0: "+len0);
          System.out.println("len1: "+len1);
          System.out.println("len2: "+len2);
         */
        }
      }

      field0.setSamples(values0);
      field1.setSamples(values1);
      field2.setSamples(values2);

      station0.setSample(tt, field0, false);
      station1.setSample(tt, field1, false);
      station2.setSample(tt, field2, false);
    }
    return new_stations;
  }
}
