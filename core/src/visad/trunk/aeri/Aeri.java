/*
Bob Knuteson's comments:
Bill Smith looks at RH
look at lowest 3 KM for mixing ratio
use RH if looking above 3 KM
might use (T - TD) instead of RH

height changes (errors) are causing RH "changes"

limit to 300 MB (9500 m) or 500 MB (5500 m)
MR looks interesting up to 10000 m
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
import java.rmi.RemoteException;
import java.io.IOException;
import visad.data.visad.VisADForm;

// JFC packages
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

public class Aeri 
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

  //- (T,TD,WV,AGE)
  //
  RealTupleType advect_range;
 
  FunctionType advect_type;
  FunctionType advect_field_type;

  FieldImpl advect_field;
  FieldImpl stations_field;

  int n_stations = 5;

  double[] station_lat;
  double[] station_lon;
  double[] station_alt;
  double[] station_id;

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

  float height_limit = 5500; // roughly 500 MB

  public static void main(String args[])
         throws VisADException, RemoteException, IOException
  {
    Aeri aeri = new Aeri(args);
  }

  public Aeri(String[] args) 
         throws VisADException, RemoteException, IOException
  {

    String vadfile = null;
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
      }
    }
    if (vadfile != null) {
      init_from_vad(vadfile);
    } 
    else { 
      init_from_cdf();
    }
    wvmr.alias("MR");

    JFrame frame = new JFrame("VisAD HSV Color Coordinates");
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

  void init_from_cdf()
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

    String[] wind_files = new String[n_stations];
    String[] rtvl_files = new String[n_stations];

    wind_files[0] = "./data/19991226_lamont_windprof.cdf";
    wind_files[1] = "./data/19991226_hillsboro_windprof.cdf";
    wind_files[2] = "./data/19991226_morris_windprof.cdf";
    wind_files[3] = "./data/19991226_purcell_windprof.cdf";
    wind_files[4] = "./data/19991226_vici_windprof.cdf";

    rtvl_files[0] = "./data/lamont_991226AG.cdf";
    rtvl_files[1] = "./data/hillsboro_991226AG.cdf";
    rtvl_files[2] = "./data/morris_991226AG.cdf";
    rtvl_files[3] = "./data/purcell_991226AG.cdf";
    rtvl_files[4] = "./data/vici_991226AG.cdf";

    FieldImpl[] winds = makeWinds(wind_files);
    System.out.println(winds[0].getType().prettyString());

    FieldImpl[] rtvls = makeAeri(rtvl_files);
    System.out.println(rtvls[0].getType().prettyString());

    spatial_domain = new RealTupleType(longitude, latitude, altitude);
    advect_range = new RealTupleType(temp, dwpt, wvmr, RH);
    advect_type = new FunctionType(spatial_domain, advect_range);
    advect_field_type = new FunctionType(time, advect_type);

    stations_field =
        new FieldImpl(new FunctionType( stn_idx, advect_field_type),
                                        new Integer1DSet( stn_idx, n_stations,
                                                          null, null, null));

    for ( int kk = 0; kk < n_stations; kk++ )
    {
      advect_field = makeAdvect(winds[kk], rtvls[kk], kk);
      stations_field.setSample(kk, advect_field);
    }


    VisADForm vad_form = new VisADForm();
    vad_form.save("aeri-winds_122699.vad", stations_field, true);

    System.out.println(stations_field.getType().prettyString());
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
  }

  DisplayImpl makeDisplay(JPanel panel)
       throws VisADException, RemoteException, IOException
  {
    del_lon = 5f;
    del_lat = 1.5f;

    baseMap = new BaseMapAdapter("OUTLUSAM");
    map_ref = new DataReferenceImpl("map");

    if ( baseMap.isEastPositive() )
    {
      baseMap.setEastPositive(false);
    }

    //- make barber poles for each station
    //
    DataImpl poles = makePoles();
    DataReference poles_ref = new DataReferenceImpl("poles");
    poles_ref.setData(poles);

    DisplayImpl display = new DisplayImplJ3D("aeri");
    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setScaleEnable(true);
    DisplayRenderer dr = display.getDisplayRenderer();
    dr.setBoxOn(false);

    xmap = new ScalarMap(longitude, Display.XAxis);
    xmap.setScaleEnable(false);
    ymap = new ScalarMap(latitude, Display.YAxis);
    ymap.setScaleEnable(false);
    zmap = new ScalarMap(altitude, Display.ZAxis);

    display.addMap(xmap);
    display.addMap(ymap);
    display.addMap(zmap);
    // note RH bonces around because temperature does
    // ScalarMap cmap = new ScalarMap(RH, Display.RGB);
    ScalarMap cmap = new ScalarMap(wvmr, Display.RGB);
    display.addMap(cmap);
    LabeledColorWidget cwidget = new LabeledColorWidget(cmap);
    ScalarMap tmap = new ScalarMap(time, Display.Animation);
    display.addMap(tmap);
    AnimationControl control = (AnimationControl) tmap.getControl();
    control.setStep(50);
    AnimationWidget awidget = new AnimationWidget(tmap);

    // xmap.setRange(lon_min, lon_max);
    // ymap.setRange(lat_min, lat_max);
    zmap.setRange(0.0, hgt_max);

    DataReference advect_ref = new DataReferenceImpl("advect_ref");
    advect_ref.setData(stations_field);

    ConstantMap[] map_constMap = 
      new ConstantMap[] 
    {
      new ConstantMap(1., Display.Red),
      new ConstantMap(1., Display.Green),
      new ConstantMap(1., Display.Blue), 
      new ConstantMap(-.99, Display.ZAxis)
    };

    display.disableAction();
    display.addReference(poles_ref);
    display.addReference(advect_ref);
    display.addReference(map_ref, map_constMap);

    xmap.addScalarMapListener(this);
    ymap.addScalarMapListener(this);
    display.enableAction();
    JPanel dpanel = new JPanel();
    dpanel.setLayout(new BoxLayout(dpanel, BoxLayout.Y_AXIS));
    // dpanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    // dpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    dpanel.add(display.getComponent());
    JPanel wpanel = new JPanel();
    wpanel.setLayout(new BoxLayout(wpanel, BoxLayout.Y_AXIS));
    // wpanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    // wpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
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

  double lon_min = Double.MAX_VALUE;
  double lon_max = Double.MIN_VALUE;
  double lat_min = Double.MAX_VALUE;
  double lat_max = Double.MIN_VALUE;
  double hgt_max = Double.MIN_VALUE;

  DataImpl makePoles()
           throws VisADException, RemoteException
  {
    SampledSet[] set_s = new SampledSet[n_stations];
    float[][] locs = new float[3][2];

    for ( int kk = 0; kk < n_stations; kk++ ) {
      Set set = ((FieldImpl)((FieldImpl)
        stations_field.getSample(kk)).getSample(20)).getDomainSet();
      float[][] samples = set.getSamples(false);
      float[] lo = ((SampledSet)set).getLow();
      float[] hi = ((SampledSet)set).getHi();

      locs[0][0] = samples[0][0];
      locs[1][0] = samples[1][0];
      // locs[2][0] = lo[2];
      locs[2][0] = 0.0f;

      locs[0][1] = samples[0][0];
      locs[1][1] = samples[1][0];
      // locs[2][1] = hi[2];
      locs[2][1] = height_limit;

      if (hi[2] > hgt_max) hgt_max = hi[2];
      if (samples[0][0] > lat_max) lat_max = samples[0][0];
      if (samples[0][0] < lat_min) lat_min = samples[0][0];
      if (samples[1][0] > lon_max) lon_max = samples[1][0];
      if (samples[1][0] < lon_min) lon_min = samples[1][0];

      set_s[kk] = new Gridded3DSet(spatial_domain, locs, 2, null, null, null);

// System.out.println("set_s[" + kk + "] = " + set_s[kk]);
    }
    return new UnionSet(spatial_domain, set_s);
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
      x_range = xmap.getRange();
      y_range = ymap.getRange();
      latmin = (float)y_range[0];
      latmax = (float)y_range[1];
      lonmin = (float)x_range[0];
      lonmax = (float)x_range[1];
      baseMap.setLatLonLimits(latmin-del_lat, latmax+del_lat, 
                              lonmin-del_lon, lonmax+del_lon);
      DataImpl map = baseMap.getData();
      map_ref.setData(map);
      firstEvent = true;
      xmap.setRange(lonmax, lonmin);
      ymap.setRange(latmin, latmax);
/*
System.out.println("lon = " + lonmin + " " + lonmax +
                   " lat = " + latmin + " " + latmax);
*/
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
    double[] base_time = new double[n_stations];
    Gridded1DSet d_set = null;
    FlatField new_ff = null;

    RealType alt;
    RealType spd;
    RealType dir;
    
    RealType u_wind = new RealType("u_wind", null, null);
    RealType v_wind = new RealType("v_wind", null, null);

    //- create a new netcdf reader
    Plain plain = new Plain();

    //- retrieve file data objects
    for ( int kk = 0; kk < n_stations; kk++) {
      file_data[kk] = plain.open(files[kk]);
    }

    //- make sub mathtype for file objects
    MathType file_type = file_data[0].getType();
    FunctionType f_type0 =
      (FunctionType)((TupleType)file_type).getComponent(2);
    FunctionType f_type1 =
      (FunctionType)((TupleType)f_type0.getRange()).getComponent(10);
    altitude = (RealType)((RealTupleType)f_type1.getRange()).getComponent(0);
    spd = (RealType)((RealTupleType)f_type1.getRange()).getComponent(4);
    dir = (RealType)((RealTupleType)f_type1.getRange()).getComponent(3);
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
    FunctionType new_type = new FunctionType(domain_type, alt_to_uv);

    FieldImpl[] winds = new FieldImpl[n_stations];
    
    for ( int ii = 0; ii < n_stations; ii++ )
    {
      base_time[ii] = (double)
        ((Real)((Tuple)file_data[ii]).getComponent(1)).getValue();
      time_field[ii] = (FieldImpl)
        ((Tuple)file_data[ii]).getComponent(2);
      station_lat[ii] =
        ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(6)).getValue();
      station_lon[ii] =
        ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(7)).getValue();
      station_alt[ii] =
        ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(8)).getValue();
      station_id[ii] =
        ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(9)).getValue();

      int length = time_field[ii].getLength();
      time_offset = new double[1][length];
      FlatField[] range_data = new FlatField[length];

      double[][] samples = null; // WLH
      for ( int jj = 0; jj < length; jj++ )
      {
        Tuple range = (Tuple) time_field[ii].getSample(jj);
        time_offset[0][jj] = (double)((Real)range.getComponent(0)).getValue();  

        FlatField p_field = (FlatField) range.getComponent(10);
        double[][] values =
          p_field.getValues(); // WLH - (alt, ?, ?, dir, spd, ???)
        double[][] new_values = new double[2][values[0].length];

        if ( jj == 0 )  //- only once, vertical range gates don't change
        {
          samples = new double[1][values[0].length]; // WLH
          System.arraycopy(values[0], 0, samples[0], 0, samples[0].length);
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
          if ( values[3][mm] == -9999 ) {
            new_values[0][mm] = Float.NaN;
          }
          else {
            new_values[0][mm] = values[3][mm];
          }

          if ( values[4][mm] == -9999 ) {
            new_values[1][mm] = Float.NaN;
          }
          else {
            new_values[1][mm] = values[4][mm];
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
    double[] station_lat = new double[n_stations];
    double[] station_lon = new double[n_stations];
    double[] station_alt = new double[n_stations];
    double[] station_id = new double[n_stations];
    double[][] time_offset = null;
    double[] base_time = new double[n_stations];

    //- create a new netcdf reader
    Plain plain = new Plain();

    //- retrieve file data objects
    for ( int kk = 0; kk < n_stations; kk++ ) {
      file_data[kk] = plain.open(files[kk]);
    }

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
    FunctionType new_type = new FunctionType(domain_type, f_type1);

    FieldImpl[] rtvls = new FieldImpl[n_stations];


    for ( int ii = 0; ii < n_stations; ii++ )
    {
      base_time[ii] = (double)
        ((Real)((Tuple)file_data[ii]).getComponent(0)).getValue();
      time_field[ii] = (FieldImpl) ((Tuple)file_data[ii]).getComponent(1);

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
      rtvls[ii] = new FieldImpl(new_type, domain_set);
      rtvls[ii].setSamples(range_data, false);
    }
    return rtvls;
  }

  FieldImpl makeAdvect( FieldImpl winds, FieldImpl rtvls, int stn_idx )
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
    FlatField[] rtvl_on_wind;
    FieldImpl[] wind_on_wind;
    int n_samples;
    double age;
    double age_max = 3600;
    double rtvl_intvl;
    double rtvl_time;
    double rtvl_time_0;
    double rtvl_intvl_min = 476;
    int n_advect_pts = 10;
    int n_levels_max = 65;
    float[][] advect_locs = new float[3][n_advect_pts*n_levels_max];
    float[][] rtvl_vals = new float[4][n_advect_pts*n_levels_max];
    CoordinateSystem cs;
    double[][] dir_spd;
    double[][] uv_wind;
    int idx = 0;
    float alt;
    Set rtvls_domain;
    float[] rtvl_times;
    double factor = .5*(1.0/111000.0);  //-knt to ms, m to degree

    //- time(rtvl) -> (lon,lat,alt) -> (T,TD,WV,AGE)
    //
    advect_field = new FieldImpl(advect_field_type, rtvls.getDomainSet()); 

    //- resample winds domain (time) to rtvls
    //

    rtvls_domain = rtvls.getDomainSet();
    wind_to_rtvl_time = (FieldImpl)
      winds.resample( rtvls_domain,
                      Data.WEIGHTED_AVERAGE,
                      Data.NO_ERRORS );


    //- resample rtvls domain (altitude) to winds at each rtvl time
    //
    int len = rtvls.getLength();
    rtvl_on_wind = new FlatField[len];
    wind_on_wind = new FieldImpl[len];
    for ( int tt = 0; tt < len; tt++ ) {
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
      wind_on_wind[tt] = (FieldImpl)
        alt_to_wind.resample( ds,
                              Data.WEIGHTED_AVERAGE,
                              Data.NO_ERRORS );
// end WLH height limit
      rtvl_on_wind[tt] = (FlatField)
        alt_to_rtvl.resample( ds,
                              Data.WEIGHTED_AVERAGE,
                              Data.NO_ERRORS );
    }
                                   
    //- get rtvls time domain samples
    //
    float[][] f_array = rtvls_domain.getSamples();
    rtvl_times = f_array[0];
    
    //- loop over rtvl sampling in time   -*
    //
    for ( int tt = n_advect_pts; tt < len; tt++ )
    {
      rtvl_idx = tt;
      // alt_to_wind = (FieldImpl)wind_to_rtvl_time.getSample(tt);
      alt_to_wind = wind_on_wind[tt];
      int alt_len = alt_to_wind.getLength();

      uv_wind = alt_to_wind.getValues();

      //- get wind data height sampling 
      //
      float[][] heights = alt_to_wind.getDomainSet().getSamples();
     
      n_samples = 0;
      rtvl_time_0 = rtvl_times[tt];
      
      //- loop over wind profiler vertical range  -*
      //
      for ( int jj = 0; jj < alt_len; jj++ )
      {
        alt = heights[0][jj];

        //- loop over rtvl time samples   -*
        //
        for ( int ii = 0; ii < n_advect_pts; ii++ )
        {
          rtvl_time = rtvl_times[rtvl_idx - ii];
          age = rtvl_time - rtvl_time_0;

// WLH - adjust for shortening of longitude with increasing latitude
          double lat_radians = (Math.PI/180.0)*station_lat[stn_idx];
          advect_locs[0][n_samples] = (float)
            (uv_wind[0][jj]*age*factor/Math.cos(lat_radians) +
             station_lon[stn_idx]);
          advect_locs[1][n_samples] = (float)
            (uv_wind[1][jj]*age*factor + station_lat[stn_idx]);

          advect_locs[2][n_samples] = alt;

          double[][] vals = rtvl_on_wind[rtvl_idx - ii].getValues();

          rtvl_vals[0][n_samples] = (float) vals[1][jj];
          rtvl_vals[1][n_samples] = (float) vals[2][jj];
          rtvl_vals[2][n_samples] = (float) vals[3][jj];
/*
          rtvl_vals[3][n_samples] = (float) (-age*age);
          // rtvl_vals[3][n_samples] = (float) age;
*/
          rtvl_vals[3][n_samples] = (float)
            relativeHumidity(vals[1][jj], vals[2][jj]);

          n_samples++;
        }
      }
      int lengthX = n_samples/alt_len;
      int lengthY = alt_len;
     
      float[][] samples = new float[3][lengthX*lengthY];
      System.arraycopy(advect_locs[0], 0, samples[0], 0, n_samples);
      System.arraycopy(advect_locs[1], 0, samples[1], 0, n_samples);
      System.arraycopy(advect_locs[2], 0, samples[2], 0, n_samples);

      float[][] range = new float[4][n_samples];
      System.arraycopy(rtvl_vals[0], 0, range[0], 0, n_samples);
      System.arraycopy(rtvl_vals[1], 0, range[1], 0, n_samples);
      System.arraycopy(rtvl_vals[2], 0, range[2], 0, n_samples);
      System.arraycopy(rtvl_vals[3], 0, range[3], 0, n_samples);

      Gridded3DSet g3d_set = 
         new Gridded3DSet(spatial_domain, samples, lengthX, lengthY); 

      advect = new FlatField(advect_type, g3d_set);
      advect.setSamples(range);
      advect_field.setSample(tt, advect, false);
    }
    return advect_field;
  }

  /** saturation vapor pressure over water.  t in kelvin.
  *
  */
  public static double satVapPres(double t) {
    double coef[]={6.1104546,0.4442351,1.4302099e-2, 2.6454708e-4,
              3.0357098e-6, 2.0972268e-8, 6.0487594e-11,-1.469687e-13};

    // sat vap pressures every 5C from -50 to -200
    double escold[] = {
      0.648554685769663908E-01, 0.378319512256073479E-01,
      0.222444934288790197E-01, 0.131828928424683120E-01,
      0.787402077141244848E-02, 0.473973049488473318E-02,
      0.287512035504357928E-02, 0.175743037675810294E-02,
      0.108241739518850975E-02, 0.671708939185605941E-03,
      0.419964702632039404E-03, 0.264524363863469876E-03,
      0.167847963736813220E-03, 0.107285397631620379E-03,
      0.690742634496135612E-04, 0.447940489768084267E-04,
      0.292570419563937303E-04, 0.192452912634994161E-04,
      0.127491372410747951E-04, 0.850507010275505138E-05,
      0.571340025334971129E-05, 0.386465029673876238E-05,
      0.263210971965005286E-05, 0.180491072930570428E-05,
      0.124607850555816049E-05, 0.866070571346870824E-06,
      0.605982217668895538E-06, 0.426821197943242768E-06,
      0.302616508514379476E-06, 0.215963854234913987E-06,
      0.155128954578336869E-06};

    double temp = t - 273.16;
    double retval;

    if (temp != temp) {
      retval = Double.NaN;
    }
    else if (temp > -50.) {
      retval = ( coef[0] + temp*(coef[1] + temp*(coef[2] + temp*(coef[3] +
      temp*(coef[4] + temp*(coef[5] + temp*(coef[6] + temp*coef[7])))))) );
    }
    else {
       double tt = (-temp - 50.)/5.;
       int inx = (int) tt;
       if (inx < escold.length) {
         retval = escold[inx] + (tt % 1.)*(escold[inx+1]-escold[inx]);
       } else {
         retval = 1e-7;
       }
    }
    return retval;
  }

  /** mixing ratio
  *
  */
  public static double mixingRatio(double t, double p) {
    double e = satVapPres(t);
    return ( 621.97*e/(p - e) );
  }

  /** relative humidity
  *
  */
  public static double relativeHumidity(double t, double td) {
    return (satVapPres(td) / satVapPres(t));
  }

}
