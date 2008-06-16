/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.data.netcdf.*;
import visad.bom.WindPolarCoordinateSystem;
import visad.data.mcidas.BaseMapAdapter;
import visad.data.visad.VisADSerialForm;
import visad.data.mcidas.AreaAdapter;
import visad.meteorology.ImageSequenceManager;
import visad.meteorology.NavigatedImage;
import visad.meteorology.ImageSequence;
import java.rmi.RemoteException;
import java.io.IOException;
import java.io.File;

// JFC packages
import javax.swing.*;

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
  RealType shape;

  RealTupleType wind_aeri;
  RealTupleType div_qV_comps;
  FunctionType alt_to_wind_aeri;
  FunctionType time_to_alt_to_wind_aeri;
  FunctionType alt_to_divqV;
  FunctionType time_to_alt_to_divqV;

  FieldImpl advect_field;
  FieldImpl stations_field;
  FieldImpl divqV_field;
  ImageSequence image_seq;
  Set timeDomain;

  int n_stations = 3;

  double[] station_lat;
  double[] station_lon;
  double[] station_alt;
  double[] station_id;
  double[] stat_xoffset = new double[n_stations];
  double[] stat_yoffset = new double[n_stations];
  double[][] centroid_ll;

  BaseMapAdapter baseMap;
  DataReference map_ref;

  ScalarMap xmap;
  ScalarMap ymap;
  ScalarMap zmap;
  ScalarMap img_map;
  boolean xmapEvent = false;
  boolean ymapEvent = false;
  boolean imgEvent = false;
  boolean firstEvent = false;
  boolean first_img_Event = false;

  float latmin, latmax;
  float lonmin, lonmax;
  float del_lat, del_lon;
  double[] x_range, y_range;

  int height_limit = 4000;     //- meters
  int time_intrvl  =  900;     //- seconds
  boolean rh = false;
  boolean tm = false;
  boolean pt = false;
  boolean ept = false;

  int start_date = 0;
  double start_time = 0.0;

  double[] scale_offset_x = new double[2];
  double[] scale_offset_y = new double[2];

  AnimationControl ani_cntrl;

  int   alt_factor = 8;
  int   n_hres_alt_samples;
  float alt_min;
  float alt_max;

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

    try {
      String fs = System.getProperty("file.separator");
      image_seq = Qdiv.init_images("."+fs+"data"+fs+"image"+fs+baseDate);
      band1 = (RealType)
         ((RealTupleType)
          ((FunctionType)
           ((FunctionType)image_seq.getType()).getRange()).getRange()).getComponent(0);
    }
    catch ( Exception e ) {
      System.out.println("no AREA image data");
      image_seq = null;
    }

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

    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));

    DisplayImpl display = makeDisplay(panel, panel2);

    int WIDTH = 1200;
    int HEIGHT = 800;

    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);
    frame.setVisible(true);

    //-makeDisplay2(panel2);

    JFrame frame2 = new JFrame("image color");
    frame2.setSize(400, 200);
    frame2.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    frame2.getContentPane().add(panel2);
 //-frame2.setVisible(true);
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
    RH = RealType.getRealType("RH", SI.second);
    stn_idx = RealType.getRealType("stn_idx");
    theta = RealType.getRealType("theta");
    thetaE = RealType.getRealType("thetaE");
    u_wind = RealType.getRealType("u_wind");
    v_wind = RealType.getRealType("v_wind");
    wvmr_u = RealType.getRealType("wvmr_u");
    wvmr_v = RealType.getRealType("wvmr_v");
    div_qV = RealType.getRealType("div_qV");
    q_divV = RealType.getRealType("q_divV");
    qAdvct = RealType.getRealType("qAdvct");
    shape = RealType.getRealType("shape");

    String[] wind_files = new String[n_stations];
    String[] rtvl_files = new String[n_stations];

    String truncatedDate = baseDate;

    wind_files[0] = "./data/" + baseDate + "_lamont_windprof.cdf";
    wind_files[1] = "./data/" + baseDate + "_vici_windprof.cdf";
    wind_files[2] = "./data/" + baseDate + "_purcell_windprof.cdf";

    rtvl_files[0] = "./data/lamont_" + truncatedDate + "AG.cdf";
    rtvl_files[1] = "./data/vici_" + truncatedDate + "AG.cdf";
    rtvl_files[2] = "./data/purcell_" + truncatedDate + "AG.cdf";

    File file = new File("./data/lamont_" + truncatedDate + "AP.cdf");

    if (file.exists()) {
      rtvl_files[0] = "./data/lamont_" + truncatedDate + "AP.cdf";
    }
    else {
      rtvl_files[0] = "./data/lamont_" + truncatedDate + "AG.cdf";
    }

    file = new File("./data/vici_" + truncatedDate + "AP.cdf");
    if (file.exists()) {
      rtvl_files[1] = "./data/vici_" + truncatedDate + "AP.cdf";
    }
    else {
      rtvl_files[1] = "./data/vici_" + truncatedDate + "AG.cdf";
    }

        file = new File("./data/purcell_" + truncatedDate + "AP.cdf");
    if (file.exists()) {
      rtvl_files[2] = "./data/purcell_" + truncatedDate + "AP.cdf";
    }
    else {
      rtvl_files[2] = "./data/purcell_" + truncatedDate + "AG.cdf";
    }

    FieldImpl[] winds = makeWinds(wind_files);
       System.out.println(winds[0].getType().prettyString());

    FieldImpl[] rtvls = makeAeri(rtvl_files);
       System.out.println(rtvls[0].getType().prettyString());

    RealType[] r_types = {u_wind, v_wind, temp, dwpt, wvmr, wvmr_u, wvmr_v, thetaE};
    wind_aeri = new RealTupleType(r_types);
    alt_to_wind_aeri = new FunctionType(altitude, wind_aeri);
    time_to_alt_to_wind_aeri = new FunctionType(RealType.Time, alt_to_wind_aeri);
    div_qV_comps = new RealTupleType(new RealType[] {div_qV, thetaE, shape});
    alt_to_divqV = new FunctionType(altitude, div_qV_comps);
    time_to_alt_to_divqV = new FunctionType(RealType.Time, alt_to_divqV);
    
    spatial_domain = new RealTupleType(longitude, latitude, altitude);

    stations_field = make_wind_aeri(winds, rtvls);
    System.out.println(stations_field.getType().prettyString());

    divqV_field = makeDivqV(stations_field);
    System.out.println("makeDivqV:  Done");
  }

  void init_from_vad( String vad_file )
       throws VisADException, RemoteException, IOException
  {
    VisADSerialForm vad_form = new VisADSerialForm();
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

  public static ImageSequence init_images(String image_directory)
         throws VisADException, RemoteException, IOException
  {
    String fs = System.getProperty("file.separator");

    if ( image_directory == null ) {
   //-image_directory = "."+fs+"data"+fs+"image"+fs+"vis";
      image_directory = "."+fs+"data"+fs+"image"+fs+"ir_display";
   //-image_directory = "."+fs+"data"+fs+"image"+fs+"ir";
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

    ImageSequenceManager img_manager = 
      new ImageSequenceManager(nav_images);

    return img_manager.getImageSequence();
  }

  DisplayImpl makeDisplay(JPanel panel, JPanel panel2)
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

    float rad = 0.0600f;
  //float len = 0.01165f;
  //float len = 0.01045f;
    float len = 0.0078375f;
    ScalarMap shape_map = new ScalarMap(shape, Display.Shape);
    display.addMap(shape_map);
 //-VisADTriangleStripArray cyl = visad.aeri.Cylinder.makeCylinder(16, rad, len);
    VisADTriangleStripArray cyl = makeCylinder(14, rad, len);
    System.out.println("makeCylinder done");
    VisADGeometryArray[] shapes;
    shapes = new VisADGeometryArray[] {cyl};
    ShapeControl shape_control = (ShapeControl) shape_map.getControl();
    shape_control.setShapeSet(new Integer1DSet(1));
    shape_control.setShapes(shapes);

 //-ScalarMap flowx = new ScalarMap(u_wind, Display.Flow1X);
 //-ScalarMap flowy = new ScalarMap(v_wind, Display.Flow1Y);
    ScalarMap flowx = new ScalarMap(wvmr_u, Display.Flow1X);
    ScalarMap flowy = new ScalarMap(wvmr_v, Display.Flow1Y);
    display.addMap(flowx);
    display.addMap(flowy);
    FlowControl flow_cntrl = (FlowControl) flowx.getControl();
    flow_cntrl.setFlowScale(0.5f);
    flow_cntrl = (FlowControl) flowy.getControl();
    flow_cntrl.setFlowScale(0.5f);


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
    display.addMap(cmap);

    ScalarMap cmap2 = new ScalarMap(thetaE, Display.RGB);
 //-ScalarMap cmap2 = new ScalarMap(div_qV, Display.RGB);
    display.addMap(cmap2);

    ColorMapWidget cmw = new ColorMapWidget(cmap, null, true, false);
    LabeledColorWidget cwidget = new LabeledColorWidget(cmw);
    ScalarMap tmap = new ScalarMap(RealType.Time, Display.Animation);
    display.addMap(tmap);
    ani_cntrl = (AnimationControl) tmap.getControl();
    ani_cntrl.setStep(200);
    AnimationWidget awidget = new AnimationWidget(tmap);

    zmap.setRange(0.0, hgt_max);

    img_map = new ScalarMap(band1, Display.RGB);
    img_map.addScalarMapListener(this);
    display.addMap(img_map);
 //-ColorMapWidget cw = new ColorMapWidget(img_map, null, true, false);
 //-LabeledColorWidget img_widget = new LabeledColorWidget(cw);
    ColorControl cc = (ColorControl) img_map.getControl();
    cc.initGreyWedge();
 //-panel2.add(img_widget);

    ConstantMap[] map_constMap =
      new ConstantMap[]
    {
      new ConstantMap(1., Display.Red),
      new ConstantMap(1., Display.Green),
      new ConstantMap(1., Display.Blue),
      new ConstantMap(-.98, Display.ZAxis)
    };

    display.addReference(poles_ref);
    display.addReference(map_ref, map_constMap);
 
    ConstantMap[] c_maps = new ConstantMap[2];
    for ( int kk = 0; kk < n_stations; kk++ ) {
      double display_x = station_lon[kk]*scale_offset_x[0] + scale_offset_x[1];
      c_maps[0] = new ConstantMap( display_x, Display.XAxis);
      double display_y = station_lat[kk]*scale_offset_y[0] + scale_offset_y[1];
      c_maps[1] = new ConstantMap( display_y, Display.YAxis);
      DataReference station_ref = new DataReferenceImpl("station: "+kk);
      station_ref.setData(stations_field.getSample(kk));
      display.addReference(station_ref, c_maps);
    }

    double display_x = (centroid_ll[0][0]*Data.RADIANS_TO_DEGREES)*
                        scale_offset_x[0] + scale_offset_x[1];
    double display_y = (centroid_ll[1][0]*Data.RADIANS_TO_DEGREES)*
                        scale_offset_y[0] + scale_offset_y[1];
    ConstantMap[] c_maps2 = new ConstantMap[] 
    {
      new ConstantMap( display_x, Display.XAxis),
      new ConstantMap( display_y, Display.YAxis)
  //- new ConstantMap( 20.0, Display.LineWidth)
    };
    DataReference centroid_ref = new DataReferenceImpl("centroid");
    centroid_ref.setData(divqV_field);
    display.addReference(centroid_ref, c_maps2);

    ConstantMap[] img_constMap =
      new ConstantMap[] {new ConstantMap(-.99, Display.ZAxis)};

    if ( image_seq != null ) {
      DataReference img_ref = new DataReferenceImpl("image");
      img_ref.setData(image_seq);
      display.addReference(img_ref, img_constMap);
    }

    JPanel dpanel = new JPanel();
    dpanel.setLayout(new BoxLayout(dpanel, BoxLayout.Y_AXIS));
    dpanel.add(display.getComponent());

    JPanel wpanel = new JPanel();
    wpanel.setLayout(new BoxLayout(wpanel, BoxLayout.Y_AXIS));
 //-cwidget.setMaximumSize(new Dimension(400, 200));
 //-awidget.setMaximumSize(new Dimension(400, 400));
    cwidget.setMaximumSize(new Dimension(400, 100));
    awidget.setMaximumSize(new Dimension(400, 200));
    wpanel.add(cwidget);
    wpanel.add(awidget);
    JPanel hpanel = new JPanel();
    hpanel.setLayout(new BoxLayout(hpanel, BoxLayout.X_AXIS));
    makeDisplay2(hpanel);
    wpanel.add(hpanel);
    Dimension d = new Dimension(400, 800);
    wpanel.setMaximumSize(d);
    panel.add(dpanel);
    panel.add(wpanel);

    return display;
  }

  void makeDisplay2( JPanel panel )
       throws VisADException, RemoteException, IOException
  {
    DisplayImpl display = new DisplayImplJ3D("components", 
                                              new TwoDDisplayRendererJ3D());

    DisplayRenderer dr = display.getDisplayRenderer();
    dr.setBoxOn(false);

    final ScalarMap xmap = new ScalarMap(RealType.Time, Display.XAxis);
    final ScalarMap ymap = new ScalarMap(altitude, Display.YAxis);
    final ScalarMap cmap = new ScalarMap(div_qV, Display.RGB);
    display.addMap(xmap);
    display.addMap(ymap);
    display.addMap(cmap);

    class Listener implements ControlListener 
    {
      double[][] value;
      public void controlChanged(ControlEvent ce) {
        int step = ani_cntrl.getCurrent();
        try {
          value = timeDomain.indexToDouble(new int[] {step});
          xmap.setRange((value[0][0] - 14400), value[0][0]);
        }
        catch (VisADException e) {
        }
        catch (RemoteException e) {
        }
      }
    }
    ani_cntrl.addControlListener(new Listener());

    DataReference time_height_ref =
      new DataReferenceImpl("time_height_ref");

    time_height_ref.setData(divqV_field.domainMultiply());
    display.addReference(time_height_ref);
    GraphicsModeControl mode = display.getGraphicsModeControl(); 
    mode.setScaleEnable(true);

    panel.add(display.getComponent());
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
    else if ( img_map.equals(e.getScalarMap()) ) {
      imgEvent = true;
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
    if ( imgEvent && !first_img_Event ) {
      double[] i_range = img_map.getRange();
      System.out.println(i_range[0]+" "+i_range[1]);
      first_img_Event = true;
      img_map.setRange(i_range[1], i_range[0]);
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
 //-time = domain_type;
    time = RealType.Time;
    FunctionType new_type = new FunctionType(RealType.Time, alt_to_uv);
 //-FunctionType new_type = new FunctionType(time, alt_to_uv);

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
      double[][] times = new double[1][length];
      time_offset = new double[1][length];
      FlatField[] range_data = new FlatField[length];

      double[][] samples = null; // WLH
      int n_not_all_miss = 0;
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
        int n_levels = values[0].length;
        int[] not_miss = new int[n_levels];
        for ( int mm = 0; mm < n_levels; mm++ )
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
       //-System.out.println("("+mm+", "+jj+") "+new_values[0][mm]+",  "+new_values[1][mm]);

          if (new_values[0][mm] == new_values[0][mm] &&
              new_values[1][mm] == new_values[1][mm]) {
            not_miss[n_not_miss] = mm;
            n_not_miss++;
          }
        }
     //-if ( (n_levels - 3) < n_not_miss && n_not_miss < n_levels) {
        if ( (n_levels - 7) < n_not_miss && n_not_miss <= n_levels) {
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

          range_data[n_not_all_miss] = new_ff;
          times[0][n_not_all_miss] = base_time[0] + time_offset[0][jj];
          n_not_all_miss++;
        }
        else {
       //-new_ff.setSamples(cs.toReference(new_values));
        }
/* end WLH - fill in missing winds */

     //-range_data[jj] = new_ff;
      }

/* resample() doesn't work for doubles ? */
      double[][] new_times = new double[1][n_not_all_miss];
      Data[] new_range_data = new Data[n_not_all_miss];
      System.arraycopy(times[0], 0, new_times[0], 0, n_not_all_miss);
      System.arraycopy(range_data, 0, new_range_data, 0, n_not_all_miss);
      System.out.println("n_not_all_miss: "+n_not_all_miss);
      Gridded1DSet domain_set =
        new Gridded1DSet(RealType.Time, Set.doubleToFloat(new_times), n_not_all_miss);

      winds[ii] = new FieldImpl(new_type, domain_set);
      winds[ii].setSamples(new_range_data, false);
    }
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
    RealType pres = RealType.getRealType("press");
    temp = RealType.getRealType("temp");
    dwpt = RealType.getRealType("dwpt");
    wvmr = RealType.getRealType("wvmr");
    RealType[] r_types = {pres, temp, dwpt, wvmr};
    f_type1 = new FunctionType(f_type1.getDomain(), new RealTupleType(r_types));


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

      int length = time_field[ii].getLength();
      time_offset = new double[1][length];
      Data[] range_data = new Data[length];
      double[][] times = new double[1][length];
      int not_all_missing = 0;

      for ( int jj = 0; jj < length; jj++ )
      {
        Tuple range = (Tuple) time_field[ii].getSample(jj);
        time_offset[0][jj] = (double)((Real)range.getComponent(0)).getValue();

        FlatField p_field = (FlatField) range.getComponent(1);
        double[][] values = p_field.getValues();
        double[][] new_values = new double[4][values[0].length];
        int num_missing = 0;
        int n_levels = values[0].length;

        for ( int mm = 0; mm < n_levels; mm++ )
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
            num_missing++;  //- if one range component missing, probably all missing
          }
          else {
            new_values[3][mm] = values[3][mm];
          }
        }

     //-if ( n_levels != num_missing )
        if ( num_missing < 3 )
        {
          FlatField new_ff = new FlatField(f_type1, p_field.getDomainSet());
          new_ff.setSamples(new_values);
          range_data[not_all_missing] = new_ff;
          times[0][not_all_missing] = base_time[0] + time_offset[0][jj];
          not_all_missing++;
        }
      }

      System.out.println("not_all_missing: "+not_all_missing);

      double[][] new_times = new double[1][not_all_missing];
      Data[] new_range_data = new Data[not_all_missing];
      System.arraycopy(times[0], 0, new_times[0], 0, not_all_missing);
      System.arraycopy(range_data, 0, new_range_data, 0, not_all_missing);

      Gridded1DSet domain_set =
        new Gridded1DSet(RealType.Time, Set.doubleToFloat(new_times), not_all_missing);

      rtvls[ii] = new FieldImpl(new_type, domain_set);
      rtvls[ii].setSamples(new_range_data, false);
    }
    return rtvls;
  }

  FieldImpl make_wind_aeri( FieldImpl[] winds, FieldImpl[] rtvls )
            throws VisADException, RemoteException, IOException
  {
    float wind_time;
    float[][] value_s = new float[1][1];
    int[] index_s = new int[1];
 //-FlatField alt_to_wind;
    FieldImpl alt_to_wind;
    FieldImpl wind_to_timeDomain;
    FieldImpl rtvl_to_timeDomain;
 //-FlatField alt_to_rtvl;
    FieldImpl alt_to_rtvl;
    FlatField advect;
    FieldImpl advect_field;
    FieldImpl rtvl_on_wind;
    FieldImpl wind_on_wind;
    int n_samples;
    double[][] dir_spd;
    double[][] uv_wind;
    int idx = 0;
    float alt;
    Set rtvls_domain;
    double[][] new_values = null;

    FieldImpl stations_field =
        new FieldImpl(new FunctionType( stn_idx, time_to_alt_to_wind_aeri),
                                        new Integer1DSet( stn_idx, n_stations,
                                                          null, null, null));

    double[] lows = new double[n_stations*2];
    double[] his = new double[n_stations*2];

    for ( int kk = 0; kk < n_stations; kk++ ) 
    {
      Set d_set = winds[kk].getDomainSet();
      lows[kk] = (double) (((SampledSet)d_set).getLow())[0];
      his[kk] = (double) (((SampledSet)d_set).getHi())[0];
      d_set = rtvls[kk].getDomainSet();
      lows[n_stations+kk] = (double) (((SampledSet)d_set).getLow())[0];
      his[n_stations+kk] = (double) (((SampledSet)d_set).getHi())[0];
    }

    double[] minmax = getArrayMinMax(lows);
    double hi_low = minmax[1];
    minmax = getArrayMinMax(his);
    double low_hi = minmax[0];
    System.out.println("hi_low: "+hi_low);
    System.out.println("low_hi: "+low_hi);
  
    timeDomain = new Linear1DSet(time,
                                 hi_low, low_hi,
                                 (int) (low_hi - hi_low)/time_intrvl );

    for ( int kk = 0; kk < n_stations; kk++ )
    {
      //- time(rtvl) -> (alt) -> (U,V,T,TD,WV)
      //
      FieldImpl time_wind_aeri = new FieldImpl(time_to_alt_to_wind_aeri, timeDomain);

      //- resample winds to timeDomain
      //

      wind_to_timeDomain = (FieldImpl)
         winds[kk].resample(timeDomain,
                            Data.WEIGHTED_AVERAGE,
                            Data.NO_ERRORS );

      //- resample rtvls to timeDomain

      rtvl_to_timeDomain = (FieldImpl)
         rtvls[kk].resample(timeDomain,
                            Data.WEIGHTED_AVERAGE,
                            Data.NO_ERRORS );

   /**
      rtvl_to_timeDomain = linearInterp(rtvls[kk], timeDomain);
    **/

      //- resample rtvls domain (altitude) to winds at each rtvl time
      //
      int dim =
        ((RealTupleType)
          ((FunctionType)time_to_alt_to_wind_aeri.getRange()).getRange()).getDimension(); 
      int n_times = timeDomain.getLength();
      Set ds = null;
      int nn = 0;
      for ( int tt = 0; tt < n_times; tt++ )
      {
        alt_to_rtvl = (FieldImpl) rtvl_to_timeDomain.getSample(tt);
        alt_to_wind = (FieldImpl) wind_to_timeDomain.getSample(tt);


        if ( tt == 0 ) {  //- these won't change over time

        ds = alt_to_wind.getDomainSet();

/**--- WLH height limit  ---*/
        float[][] samples = ds.getSamples();
        float[] ns = new float[samples[0].length];
        for (int i=0; i<samples[0].length; i++) {
          if ((samples[0][i] - station_alt[kk]) < height_limit) {
            ns[nn] = samples[0][i] - (float)station_alt[kk];
            nn++;
          }
        }
        float[][] new_samples = new float[1][nn];
        System.arraycopy(ns, 0, new_samples[0], 0, nn);
        ds = new Gridded1DSet(ds.getType(), new_samples, nn);
        new_values = new double[dim][nn];

        }


        rtvl_on_wind = (FieldImpl)
          alt_to_rtvl.resample( ds,
                                Data.WEIGHTED_AVERAGE,
                                Data.NO_ERRORS );

        double[][] uv_values = alt_to_wind.getValues();
        double[][] rtvl_values = rtvl_on_wind.getValues();

        for (int ii = 0; ii < nn; ii++) {
          new_values[0][ii] = uv_values[0][ii];
          new_values[1][ii] = uv_values[1][ii];
          new_values[2][ii] = rtvl_values[1][ii];
          new_values[3][ii] = rtvl_values[2][ii];
          new_values[4][ii] = rtvl_values[3][ii];
          new_values[5][ii] = uv_values[0][ii]*rtvl_values[3][ii];
          new_values[6][ii] = uv_values[1][ii]*rtvl_values[3][ii];
          new_values[7][ii] = Aeri.equivPotentialTemperatureStar(
                Aeri.potentialTemperature(rtvl_values[1][ii], rtvl_values[0][ii]),
                rtvl_values[3][ii],
                rtvl_values[1][ii] );
        }

        FlatField ff = new FlatField(alt_to_wind_aeri, ds);
        ff.setSamples(new_values);

        time_wind_aeri.setSample(tt, ff);
      }
      stations_field.setSample(kk, time_wind_aeri, false);
    }

    return stations_field;
  }

  FieldImpl makeDivqV( FieldImpl stations )
            throws VisADException, RemoteException
  {
    double[][] uv_comps = new double[2][3];

    double[][] lonlat = new double[2][3];
    lonlat[0][0] = station_lon[0]*Data.DEGREES_TO_RADIANS;
    lonlat[0][1] = station_lon[1]*Data.DEGREES_TO_RADIANS;
    lonlat[0][2] = station_lon[2]*Data.DEGREES_TO_RADIANS;
    lonlat[1][0] = station_lat[0]*Data.DEGREES_TO_RADIANS;
    lonlat[1][1] = station_lat[1]*Data.DEGREES_TO_RADIANS;
    lonlat[1][2] = station_lat[2]*Data.DEGREES_TO_RADIANS;

    LinearVectorPointMethod lvpm =
      new LinearVectorPointMethod(lonlat);

    centroid_ll = lvpm.getCentroid();

    FieldImpl station0 = (FieldImpl) stations.getSample(0);
    FieldImpl station1 = (FieldImpl) stations.getSample(1);
    FieldImpl station2 = (FieldImpl) stations.getSample(2);

    FlatField ff = (FlatField) station0.getSample(0);
    Set alt_set = ff.getDomainSet();
    int alt_len = alt_set.getLength();
    alt_min = (((SampledSet)alt_set).getLow())[0];
    alt_max = (((SampledSet)alt_set).getHi())[0];
    n_hres_alt_samples = alt_len*alt_factor;
    Set hres_alt_set = 
      new Linear1DSet( alt_set.getType(),
                       (double)alt_min, (double)alt_max, n_hres_alt_samples);

    FieldImpl new_field = new FieldImpl(time_to_alt_to_divqV, station0.getDomainSet());

    for (int tt = 0; tt < station0.getLength(); tt++)
    {
      FlatField field0 = (FlatField) station0.getSample(tt);
      FlatField field1 = (FlatField) station1.getSample(tt);
      FlatField field2 = (FlatField) station2.getSample(tt);

      double[][] values0 = field0.getValues(false);
      double[][] values1 = field1.getValues(false);
      double[][] values2 = field2.getValues(false);

      FlatField new_ff = new FlatField(alt_to_divqV, alt_set);
      double[][] new_values = new double[3][alt_len];

      for (int kk = 0; kk < alt_len; kk++ )
      {
        boolean any_missing = false;

        uv_comps[0][0] = values0[0][kk];
        uv_comps[1][0] = values0[1][kk];
      //uv_comps[0][0] = values0[5][kk];
      //uv_comps[1][0] = values0[6][kk];

        uv_comps[0][1] = values1[0][kk];
        uv_comps[1][1] = values1[1][kk];
      //uv_comps[0][1] = values1[5][kk];
      //uv_comps[1][1] = values1[6][kk];

        uv_comps[0][2] = values2[0][kk];
        uv_comps[1][2] = values2[1][kk];
      //uv_comps[0][2] = values2[5][kk];
      //uv_comps[1][2] = values2[6][kk];

        if (Double.isNaN(uv_comps[0][0]) || Double.isNaN(uv_comps[1][0])) {
          any_missing = true;
        }
        else if (Double.isNaN(uv_comps[0][1]) || Double.isNaN(uv_comps[1][1])) {
          any_missing = true;
        }
        else if (Double.isNaN(uv_comps[0][2]) || Double.isNaN(uv_comps[1][2])) {
          any_missing = true;
        }

        if ( ! any_missing ) {
          double[] kinematics = lvpm.getKinematics(uv_comps);
          new_values[0][kk] = kinematics[4];
          new_values[1][kk] = (values0[7][kk] + values1[7][kk] + values2[7][kk])/3;
        }
        else {
          new_values[0][kk] = Double.NaN;
          new_values[1][kk] = Double.NaN;
        }
      }
      new_ff.setSamples(new_values);
      new_field.setSample(tt, 
                          new_ff.resample(hres_alt_set, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS), 
                          false);
    }
    return new_field;
  }

  public static VisADTriangleStripArray makeCylinder( int n_faces, float rad, float len)
  {
    float[][] xy_points = new float[2][n_faces];

    double del_theta = (2*Math.PI)/n_faces;
    double theta = del_theta/2;
    int[] index = new int[n_faces+1];

    for ( int kk = 0; kk < n_faces; kk++ ) {
      xy_points[0][kk] = (float) Math.cos(theta);
      xy_points[1][kk] = (float) Math.sin(theta);
      theta += del_theta;
      index[kk] = kk;
    }
    index[n_faces] = 0;

    VisADTriangleStripArray cyl = new VisADTriangleStripArray();
    cyl.vertexCount = 2*(n_faces+1);
    cyl.coordinates = new float[cyl.vertexCount*3];
    cyl.normals = new float[cyl.vertexCount*3];
    cyl.stripVertexCounts = new int[1];
    cyl.stripVertexCounts[0] = cyl.vertexCount;

    for ( int kk = 0; kk < n_faces+1; kk++ ) {
      int ii = kk*6;
      cyl.coordinates[ii] = rad*xy_points[0][index[kk]];
      cyl.coordinates[ii+1] = rad*xy_points[1][index[kk]];
      cyl.coordinates[ii+2] = len;
      ii += 3;

      cyl.coordinates[ii] = rad*xy_points[0][index[kk]];
      cyl.coordinates[ii+1] = rad*xy_points[1][index[kk]];
      cyl.coordinates[ii+2] = -len;
    }

    for ( int kk = 0; kk < n_faces+1; kk++ ) {
      int ii = kk*6;
      cyl.normals[ii] = xy_points[0][index[kk]];
      cyl.normals[ii+1] = xy_points[1][index[kk]];
      cyl.normals[ii+2] = 0;
      ii += 3;

      cyl.normals[ii] = xy_points[0][index[kk]];
      cyl.normals[ii+1] = xy_points[1][index[kk]];
      cyl.normals[ii+2] = 0;
    }
    return cyl;
  }
}
