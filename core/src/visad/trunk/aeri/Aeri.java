package visad.aeri;

import visad.*;
import visad.DisplayImpl;
import visad.java3d.DisplayImplJ3D;
import visad.data.netcdf.*;
import visad.bom.WindPolarCoordinateSystem;
import java.rmi.RemoteException;
import java.io.IOException;


public class Aeri 
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
  RealType advAge;

  //- (T,TD,WV,AGE)
  //
  RealTupleType advect_range;
 
  FunctionType advect_type;
  FunctionType advect_field_type;

  FieldImpl advect_field;
  FieldImpl stations_field;

  int n_stations = 1;

  double[] station_lat;
  double[] station_lon;
  double[] station_alt;
  double[] station_id;

                
  public static void main(String args[])
         throws VisADException, RemoteException, IOException
  {
    Aeri aeri = new Aeri(args);
  }

  public Aeri(String[] args) 
         throws VisADException, RemoteException, IOException
  {
    station_lat = new double[n_stations];
    station_lon = new double[n_stations];
    station_alt = new double[n_stations];
    station_id = new double[n_stations];

    longitude = RealType.Longitude;
    latitude = RealType.Latitude;
    advAge = new RealType("age", SI.second, null);
    stn_idx = new RealType("stn_idx", null, null);

    String[] wind_files = new String[n_stations];
    String[] rtvl_files = new String[n_stations];

    wind_files[0] = "./data/19991216_lamont_windprof.cdf";
    /**
    wind_files[1] = "./data/19991216_hillsboro_windprof.cdf";
    wind_files[2] = "./data/19991216_morris_windprof.cdf";
    wind_files[3] = "./data/19991216_purcell_windprof.cdf";
    wind_files[4] = "./data/19991216_vici_windprof.cdf";
    **/

    rtvl_files[0] = "./data/lamont_991216AG.cdf";
    /**
    rtvl_files[1] = "./data/hillsboro_991216AG.cdf";
    rtvl_files[2] = "./data/morris_991216AG.cdf";
    rtvl_files[3] = "./data/purcell_991216AG.cdf";
    rtvl_files[4] = "./data/vici_991216AG.cdf";
    **/

    FieldImpl[] winds = makeWinds(wind_files);

    FieldImpl[] rtvls = makeAeri(rtvl_files);

    System.out.println(winds[0].getType().prettyString());
    System.out.println(rtvls[0].getType().prettyString());

    spatial_domain = new RealTupleType(longitude, latitude, altitude);
    advect_range = new RealTupleType(temp, dwpt, wvmr, advAge);
    advect_type = new FunctionType(spatial_domain, advect_range);
    advect_field_type = new FunctionType(time, advect_type);
    
  /**
    FieldImpl stations_field = 
        new FieldImpl(new FunctionType( stn_idx, advect_field_type),
                                        new Integer1DSet( stn_idx, n_stations, 
                                                          null, null, null));
   **/

    for ( int kk = 0; kk < n_stations; kk++ )
    {
      advect_field = makeAdvect(winds[kk], rtvls[kk], kk);
   //-stations_field.setSample(kk, advect_field);
    }
    System.out.println(advect_field.getType().prettyString());

    makeDisplay();
  }

  void makeDisplay()
       throws VisADException, RemoteException, IOException
  {

    DisplayImpl display = new DisplayImplJ3D("aeri", DisplayImplJ3D.APPLETFRAME);

    display.addMap(new ScalarMap(longitude, Display.XAxis));
    display.addMap(new ScalarMap(latitude, Display.YAxis));
    display.addMap(new ScalarMap(altitude, Display.ZAxis));
    display.addMap(new ScalarMap(wvmr, Display.RGB));
    ScalarMap map = new ScalarMap(time, Display.Animation);
    display.addMap(map);
    AnimationControl control = (AnimationControl) map.getControl();
    control.setStep(100);
    display.addMap(new ScalarMap(advAge, Display.Alpha));

    DataReference advect_ref = new DataReferenceImpl("advect_ref");
    advect_ref.setData(advect_field);
 //-advect_ref.setData(stations_field);

    display.addReference(advect_ref);
  }

  FieldImpl[] makeWinds(String[] files)
              throws VisADException, RemoteException, IOException
  {
    int n_stations = files.length;
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
    FunctionType f_type0 = (FunctionType)((TupleType)file_type).getComponent(2);
    FunctionType f_type1 = (FunctionType)((TupleType)f_type0.getRange()).getComponent(10);
    altitude = (RealType)((RealTupleType)f_type1.getRange()).getComponent(0);
    spd = (RealType)((RealTupleType)f_type1.getRange()).getComponent(4);
    dir = (RealType)((RealTupleType)f_type1.getRange()).getComponent(3);
    RealType[] r_types = { dir, spd };
 
    CoordinateSystem cs = new WindPolarCoordinateSystem(RealTupleType.SpatialEarth2DTuple);

    RealTupleType ds = new RealTupleType(r_types, cs, null);

    RealType[] uv_types = { u_wind, v_wind }; 
    RealTupleType uv = new RealTupleType(uv_types);

    FunctionType alt_to_ds = new FunctionType(altitude, ds);
    FunctionType alt_to_uv = new FunctionType(altitude, uv);

    RealType domain_type = (RealType) ((TupleType)f_type0.getRange()).getComponent(0);
    time = domain_type;
    FunctionType new_type = new FunctionType(domain_type, alt_to_uv);

    FieldImpl[] winds = new FieldImpl[n_stations];
    
    for ( int ii = 0; ii < n_stations; ii++ )
    {
      base_time[ii] = (double) ((Real)((Tuple)file_data[ii]).getComponent(1)).getValue();
      time_field[ii] = (FieldImpl) ((Tuple)file_data[ii]).getComponent(2);
      station_lat[ii] = ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(6)).getValue();
      station_lon[ii] = ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(7)).getValue();
      station_alt[ii] = ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(8)).getValue();
      station_id[ii] = ((Real)((Tuple)time_field[ii].getSample(0)).getComponent(9)).getValue();

      int length = time_field[ii].getLength();
      time_offset = new double[1][length];
      FlatField[] range_data = new FlatField[length];

      for ( int jj = 0; jj < length; jj++ )
      {
        Tuple range = (Tuple) time_field[ii].getSample(jj);
        time_offset[0][jj] = (double)((Real)range.getComponent(0)).getValue();  

        FlatField p_field = (FlatField) range.getComponent(10);
        double[][] values = p_field.getValues();
        double[][] new_values = new double[2][values[0].length];

        if ( jj == 0 )  //- only do this once, vertical range gates don't change
        {
          double[][] samples = new double[1][values[0].length];
          System.arraycopy(values[0], 0, samples[0], 0, samples[0].length);
          d_set = new Gridded1DSet(altitude, Set.doubleToFloat(samples), samples[0].length);
        }
        new_ff = new FlatField(alt_to_uv, d_set);

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
        }
        new_ff.setSamples(cs.toReference(new_values));
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
    FunctionType f_type0 = (FunctionType)((TupleType)file_type).getComponent(1);
    FunctionType f_type1 = (FunctionType)((TupleType)f_type0.getRange()).getComponent(1);

    RealTupleType rtt = (RealTupleType) f_type1.getRange();
    temp = (RealType) rtt.getComponent(1);
    dwpt = (RealType) rtt.getComponent(2);
    wvmr = (RealType) rtt.getComponent(3);
   

    RealType domain_type = (RealType) ((TupleType)f_type0.getRange()).getComponent(0);
    FunctionType new_type = new FunctionType(domain_type, f_type1);

    FieldImpl[] rtvls = new FieldImpl[n_stations];


    for ( int ii = 0; ii < n_stations; ii++ )
    {
      base_time[ii] = (double) ((Real)((Tuple)file_data[ii]).getComponent(0)).getValue();
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
    double factor = .5*(1d/111000d);  //-knt to ms, m to degree

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
    for ( int tt = 0; tt < len; tt++ )
    {
      alt_to_rtvl = (FieldImpl) rtvls.getSample(tt);
      alt_to_wind = (FieldImpl) wind_to_rtvl_time.getSample(tt);
      rtvl_on_wind[tt] = (FlatField) alt_to_rtvl.resample( alt_to_wind.getDomainSet(),
                                               Data.WEIGHTED_AVERAGE,
                                               Data.NO_ERRORS );
    }
                                   
    //- get rtvls time domain samples
    //
    float[][] f_array = rtvls_domain.getSamples();
    rtvl_times = f_array[0];
    
    //- loop over rtvl sampling in time   -*
    //
    for ( int tt = n_advect_pts; tt < rtvls.getLength(); tt++ )
    {
      rtvl_idx = tt;
      alt_to_wind = (FieldImpl)wind_to_rtvl_time.getSample(tt);
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

          advect_locs[0][n_samples] = (float) (uv_wind[0][jj]*age*factor + station_lon[stn_idx]);
          advect_locs[1][n_samples] = (float) (uv_wind[1][jj]*age*factor + station_lat[stn_idx]);
          advect_locs[2][n_samples] = alt;

          double[][] vals = rtvl_on_wind[rtvl_idx - ii].getValues();

          rtvl_vals[0][n_samples] = (float) vals[1][jj];
          rtvl_vals[1][n_samples] = (float) vals[2][jj];
          rtvl_vals[2][n_samples] = (float) vals[3][jj];
          rtvl_vals[3][n_samples] = (float) age;

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
}
