
//
// Nesti.java
//

/*

TO DO list:

1. click on pixel and see spectrum
2. click on pixel and do retrieval
3. display linear combinations of channels
4. Fortran retrieval subroutine takes
   arrays of radiances and wave numbers, or ??
5. change which channels are in retrieval
6. compare selected-channel retrieval with
   standard retrieval
7. see weighting functions associated with
   each channel - possibly in 3-D like Aune's
   (turning satellite images on their side)
8. compare area of overlap of weighting
   functions of selected channels, with
   area of non-overlap

*/

package visad.paoloa.nesti;

// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.util.VisADSlider;
import visad.util.LabeledRGBWidget;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.rmi.NotBoundException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.net.MalformedURLException;


public class Nesti {

  RemoteServerImpl server_server;
  RemoteServer client_server;
  boolean client;
  boolean raob = false;
  Real nothing = new Real(-1000.0);
  // number of times in file
  int ntimes;
  // size of image array generated from file
  int nlines;
  int nelements;
  // pointers from time index to image sample index
  int[] time_to_sample;
  // pointers from image line, element to time index
  int[][] sample_to_time;
  // VisAD Field data object created from file
  Field spectrum_field;
  // observation spectrum
  Field obs_spectrum;
  // index of spectrum in nasti range tuple
  int spectrum_index;
  int n_wnum;

  // flag to use Java2D
  boolean java2d = true;

  // RealTypes for data
  RealType time;
  RealType wnum1;
  RealType atmosphericRadiance;
  RealType image_line;
  RealType image_element;
  RealType pressure;
  RealType watervapor;
  RealType temperature;
  RealType ozone;

  // range of wave numbers form file
  float wnum_low;
  float wnum_hi;
  float wnum_low_0;
  float wnum_hi_0;
  float wnum_mp_0;
  int wnum_low_idx;
  int wnum_hi_idx;

  // sample set for image pixels
  Linear2DSet image_set;
  // MathTypes for image
  RealTupleType image_domain;
  FunctionType image_type;

  Gridded1DSet p_domain;

  // MathType for red_bar overlaid on spectrum display
  FunctionType red_bar_type;

  RealTupleType scatter_tuple;
  ScalarMap wnum_map;
  ScalarMap wnum_map_diff;
  FlatField field_tt;
  FlatField field_wv;
  FlatField field_oz;
  FlatField field_tt_r;
  FlatField field_wv_r;
  FlatField field_oz_r;
  FlatField field_rr;
  FlatField rr_diff;
  int type = 1;
  int p_flg;
  int[] nbuse = new int[3];
  float[] tskin = new float[1];
  float[] psfc = new float[1];
  int[] lsfc = new int[1];
  float[] azen = new float[1];
  float[] p = new float[ 40 ];
  float[] tt = new float[ 40 ];
  float[] tt_r = new float[ 40 ];
  float[][] tt_raob = new float[1][ 40 ];
  float[] wv = new float[ 40 ];
  float[] wv_r = new float[ 40 ];
  float[][] wv_raob = new float[1][ 40 ];
  float[] oz = new float[ 40 ];
  float[] oz_r = new float[ 40 ];
  float[][] oz_raob = new float[1][ 40 ];
  double[] vn = new double[9127];
  double[] tb = new double[9127];
  double[] rr = new double[9127];
  double[][] rr_values = new double[1][9127];

  float[][] tt_values = new float[1][40];
  float[][] wv_values = new float[1][40];
  float[][] oz_values = new float[1][40];
  float[][] tt_r_values = new float[1][40];
  float[][] wv_r_values = new float[1][40];
  float[][] oz_r_values = new float[1][40];
  float[][] tt_rtvl = new float[1][40];
  float[][] wv_rtvl = new float[1][40];
  float[][] pp_rtvl = new float[1][40];


  //-- declare DataReferences ---
  DataReference image_ref;
  DataReference white_cursor_ref;
  DataReference red_cursor_ref;
  DataReference red_bar_ref;
  DataReference field_ttRef;
  DataReference field_wvRef;
  DataReference field_ozRef;
  DataReference spectrum_ref;
  DataReference spectrum_ref_s;
  DataReference field_rrRef;
  DataReference rtvl_ttRef;
  DataReference rtvl_wvRef;
  DataReference gamt_ref;
  DataReference gamw_ref;
  DataReference gamts_ref;
  DataReference emis_ref;
  DataReference foward_radiance_ref;
  DataReference retrieval_ref;
  DataReference spectrum_field_ref;
  DataReference wnum_last_ref;
  DataReference wnum_low_ref;
  DataReference wnum_hi_ref;
  DataReference setBand_ref;
  DataReference recenter_ref;
  DataReference reset_ref;
  DataReference rtvl_option_ref;
  DataReference zoom_ref;
  DataReference rtvl_obs_sim_ref;
  DataReference rtvl_diff_ref;
  DataReference prof_opt_ref;
  DataReference field_tt_rRef;
  DataReference field_wv_rRef;
  DataReference field_oz_rRef;

  int n_refs = 33;  //- # of above ---


  FunctionType press_tt;
  FunctionType press_wv;
  FunctionType press_oz;
  FunctionType press_tt_r;
  FunctionType press_wv_r;
  FunctionType press_oz_r;

  //- record number index of profile in file
  int rec;


  JButton diff_button;
  JButton obs_sim_button;
  JButton raob_button;

  // type 'java Nesti' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    Nesti nesti = new Nesti(args);

    if (nesti.client_server != null) {
      nesti.setupClient();
    }
    else if (nesti.server_server != null) {
      System.loadLibrary("Nesti");
      nesti.setupServer();
    }
    else {
      System.loadLibrary("Nesti");
      nesti.setupServer();
    }
  }

  public Nesti(String[] args)
         throws VisADException, RemoteException, IOException {

    if (args.length > 0) {
      // this is a client

      // try to connect to RemoteServer
      String domain = "//" + args[0] + "/Nesti";
      try {
        client_server = (RemoteServer) Naming.lookup(domain);
      }
      catch (MalformedURLException e) {
        System.out.println("Cannot connect to server");
        System.exit(0);
      }
      catch (NotBoundException e) {
        System.out.println("Cannot connect to server");
        System.exit(0);
      }
      catch (AccessException e) {
        System.out.println("Cannot connect to server");
        System.exit(0);
      }
      catch (RemoteException e) {
        System.out.println("Cannot connect to server");
        System.exit(0);
      }
    }
    else { // args.length == 0
      // this is a server

      // try to set up a RemoteServer
      server_server = new RemoteServerImpl();
      try {
        Naming.rebind("///Nesti", server_server);
      }
      catch (MalformedURLException e) {
        System.out.println("Cannot set up server - running as stand-alone");
        server_server = null;
      }
      catch (AccessException e) {
        System.out.println("Cannot set up server - running as stand-alone");
        server_server = null;
      }
      catch (RemoteException e) {
        System.out.println("Cannot set up server - running as stand-alone");
        server_server = null;
      }
    }
  }//- end: contructor Nesti

  void setupServer() throws VisADException, RemoteException, IOException
  {

    //- create DataReferenceImpls ---
    white_cursor_ref = new DataReferenceImpl("white_cursor_ref");
    red_cursor_ref = new DataReferenceImpl("red_cursor_ref");
    spectrum_ref = new DataReferenceImpl("spectrum_ref");
    spectrum_ref_s = new DataReferenceImpl("spectrum_ref_s");
    red_bar_ref = new DataReferenceImpl("red_bar_ref");
    image_ref = new DataReferenceImpl("image_ref");
    field_ttRef = new DataReferenceImpl("tt_profile_ref");
    field_wvRef = new DataReferenceImpl("wv_profile_ref");
    field_ozRef = new DataReferenceImpl("oz_profile_ref");
    field_rrRef = new DataReferenceImpl("field_rrRef");
    rtvl_ttRef = new DataReferenceImpl("rtvl_ttRef");
    rtvl_wvRef = new DataReferenceImpl("rtvl_wvRef");
    gamt_ref = new DataReferenceImpl("gamt_ref");
    gamw_ref = new DataReferenceImpl("gamw_ref");
    gamts_ref = new DataReferenceImpl("gamts_ref");
    emis_ref = new DataReferenceImpl("emis_ref");
    foward_radiance_ref = new DataReferenceImpl("foward_radiance_ref");
    retrieval_ref = new DataReferenceImpl("retrieval_ref");
    spectrum_field_ref = new DataReferenceImpl("spectrum_field_ref");
    wnum_last_ref = new DataReferenceImpl("wnum_last_ref");
    wnum_low_ref = new DataReferenceImpl("wnum_low_ref");
    wnum_hi_ref = new DataReferenceImpl("wnum_hi_ref");
    setBand_ref = new DataReferenceImpl("setBand_ref");
    recenter_ref = new DataReferenceImpl("recenter_ref");
    reset_ref = new DataReferenceImpl("reset_ref");
    rtvl_option_ref = new DataReferenceImpl("rtvl_option_ref");
    zoom_ref = new DataReferenceImpl("zoom_ref");
    rtvl_obs_sim_ref = new DataReferenceImpl("rtvl_obs_sim_ref");
    rtvl_diff_ref = new DataReferenceImpl("rtvl_diff_ref");
    prof_opt_ref = new DataReferenceImpl("prof_opt_ref");
    field_tt_rRef = new DataReferenceImpl("tt_profile_r_ref");
    field_wv_rRef = new DataReferenceImpl("wv_profile_r_ref");
    field_oz_rRef = new DataReferenceImpl("oz_profile_r_ref");


//------ Initialize, File I/O  ------------------------

    String[] filename_s = new String[3];
    filename_s[0] = "outputC1.nc";
    filename_s[1] = "outputC2.nc";
    filename_s[2] = "outputC3.nc";

    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing a NAST-I file
    Tuple nasti_tuple = (Tuple) plain.open(filename_s[0]);

    // extract the time sequence of spectra
    Field nasti = (Field) nasti_tuple.getComponent(2);
    Field[] nasti_a = new Field[3];
    nasti_a[0] = nasti;
    nasti_a[1] = (Field) ((Tuple)plain.open(filename_s[1])).getComponent(2);
    nasti_a[2] = (Field) ((Tuple)plain.open(filename_s[2])).getComponent(2);

    plain = null;

    // extract the type of image and use
    // it to determine how images are displayed
    FunctionType nasti_type = (FunctionType) nasti.getType();
    TupleType nasti_range_type = (TupleType) nasti_type.getRange();
    int angle_index = nasti_range_type.getIndex("sceneMirrorAngle");
    time = (RealType) ((RealTupleType) nasti_type.getDomain()).getComponent(0);

    // compute how times map to image pixels
    Gridded1DSet time_set = (Gridded1DSet) nasti.getDomainSet();
    float[][] t = time_set.getSamples();
    float[] times = t[0];
    ntimes = time_set.getLength();
    double[] angles = new double[ntimes];
    int[] elements = new int[ntimes];
    int[] lines = new int[ntimes];
    int line = 0;
    int max_element = 0;
    for (int i=0; i<ntimes; i++) {
      // sceneMirrorAngle, scans between -45 and +45 and definme scan lines
      angles[i] =
        ((Real) ((Tuple) nasti.getSample(i)).getComponent(angle_index)).getValue();
/*
      System.out.println("sceneMirrorAngle[" + i + "] = " + angles[i] +
                         " time = " + times[i]);
*/
      if (i > 0 && angles[i] < angles[i-1]) line++;
      lines[i] = line;
      elements[i] = (int) Math.round((angles[i] + 45.0) / 7.5);
      if (elements[i] < 0) {
        System.out.println("negative element " + elements[i]);
        System.exit(0);
      }
      if (elements[i] > 12) {
        System.out.println("element > 12: " + elements[i]);
        System.exit(0);
      }
      if (elements[i] > max_element) max_element = elements[i];
    }
    if (max_element > 12) {
      System.out.println("max_element = " + max_element + " too large");
      System.exit(0);
    }
    // size of image
    nlines = line + 1;
    nelements = 13;

System.out.println("nlines = " + nlines + " nelements = " + nelements);

    // set up mappings between times and image
    time_to_sample = new int[ntimes];
    sample_to_time = new int[nlines][nelements];
    for (int i=0; i<nlines; i++) {
      for (int j=0; j<nelements; j++) {
        sample_to_time[i][j] = -1;
      }
    }
    for (int i=0; i<ntimes; i++) {
      time_to_sample[i] = elements[i] + nelements * lines[i];
      sample_to_time[lines[i]][elements[i]] = i;
    }

    // get spectrum and types
 //-spectrum_index = 86;
    spectrum_index = 8;
    FunctionType spectrum_type =
      (FunctionType) nasti_range_type.getComponent(spectrum_index);
    wnum1 = (RealType) ((RealTupleType) spectrum_type.getDomain()).getComponent(0);
    atmosphericRadiance = (RealType) spectrum_type.getRange();
    scatter_tuple = new RealTupleType( wnum1, atmosphericRadiance );

    // build red_bar_type
    red_bar_type = new FunctionType(atmosphericRadiance, wnum1);

    // get first spectrum and its sampling
 //-Field spectrum0 =
 //-   (Field) ((Tuple) nasti.getSample(0)).getComponent(spectrum_index);
 //-Gridded1DSet spectrum_set = (Gridded1DSet) spectrum0.getDomainSet();
    Gridded1DSet spectrum_set = null;
    // System.out.println("spectrum_set = " + spectrum_set);

//*----------
    p_flg = 1;
    readProf_c(type, p_flg, tskin, psfc, lsfc, azen, p,
               tt_raob[0], wv_raob[0], oz_raob[0] );

    pressure = new RealType("pressure_1", null, null);
    temperature = new RealType("temperature_1", null, null);
    watervapor = new RealType("watervapor_1", null, null);
    ozone = new RealType("ozone_1", null, null);

    float[][] samples = new float[1][40];
    samples[0] = p;
    int n_samples = 40;
    p_domain = new Gridded1DSet( pressure, samples, n_samples );

    tt_r = tt_raob[0];
    wv_r = wv_raob[0];
    oz_r = oz_raob[0];

    press_tt_r = new FunctionType( pressure, temperature );
    press_wv_r = new FunctionType( pressure, watervapor );
    press_oz_r = new FunctionType( pressure, ozone );

    field_tt_r = new FlatField( press_tt_r, p_domain );
    field_wv_r = new FlatField( press_wv_r, p_domain );
    field_oz_r = new FlatField( press_oz_r, p_domain );

    tt_r_values[0] = tt_r;
    wv_r_values[0] = wv_r;
    oz_r_values[0] = oz_r;

    field_tt_r.setSamples( tt_r_values );
    field_wv_r.setSamples( wv_r_values );
    field_oz_r.setSamples( oz_r_values );

    field_tt_rRef.setData(field_tt_r);
    field_wv_rRef.setData(field_wv_r);
    field_oz_rRef.setData(field_oz_r);

    p_flg = 0;
    readProf_c(type, p_flg, tskin, psfc, lsfc, azen, p, tt, wv, oz);

    nbuse[0] = 1;
    nbuse[1] = 1;
    nbuse[2] = 1;
    nastirte_c( tskin[0], psfc[0], lsfc[0], azen[0], p, tt, wv, oz,
                nbuse, vn, tb, rr );

    press_tt = new FunctionType( pressure, temperature );
    press_wv = new FunctionType( pressure, watervapor );
    press_oz = new FunctionType( pressure, ozone );
    FunctionType wave_rad = new FunctionType( wnum1, atmosphericRadiance );

    field_tt = new FlatField( press_tt, p_domain );
    field_wv = new FlatField( press_wv, p_domain );
    field_oz = new FlatField( press_oz, p_domain );

    int n_wnum = 9127;
    samples = new float[1][n_wnum];
    double[][] vn_a = new double[1][n_wnum];
    System.arraycopy(vn, 0, vn_a[0], 0, n_wnum);
    samples = Set.doubleToFloat(vn_a);
    Gridded1DSet domain = new Gridded1DSet( wnum1, samples, n_wnum );
    field_rr = new FlatField( wave_rad, domain );

    tt_values[0] = tt;
    wv_values[0] = wv;
    oz_values[0] = oz;
    System.arraycopy(rr, 0, rr_values[0], 0, n_wnum);

    field_tt.setSamples( tt_values );
    field_wv.setSamples( wv_values );
    field_oz.setSamples( oz_values );
    field_rr.setSamples( rr_values );

    field_ttRef.setData(field_tt);
    field_wvRef.setData(field_wv);
    field_ozRef.setData(field_oz);


    FunctionType f_type = new FunctionType( time, spectrum_type );
    spectrum_field = new FieldImpl( f_type, time_set );
    double[][] range_values;

    int n_wnum1 = 2199;
    int n_wnum2 = 3858;
    int n_wnum3 = 3070;
    int n_wnum_obs = 8504;
    float[][] samples_1 = new float[1][n_wnum];
    float[][] ranges_1 = new float[1][n_wnum];
    float[][] samples_2 = new float[1][n_wnum];
    float[][] ranges_2 = new float[1][n_wnum];
    float[][] samples_3 = new float[1][n_wnum];
    float[][] ranges_3 = new float[1][n_wnum];
    float[][] new_spectrum = new float[1][n_wnum_obs];
    float[][] new_range = new float[1][n_wnum_obs];

    float band1_lo = 650.0496f;
    float band1_hi = 1299.9677f;
    float band2_lo = 1300.2206f;
    float band2_hi = 1999.7985f;
    float band3_lo = 2000.1323f;
    float band3_hi = 2699.9512f;
    boolean band1 = false;
    boolean band2 = false;
    boolean band3 = false;

    int cnt, cnt_1, cnt_2, cnt_3;
    for ( int i = 0; i < ntimes; i++ )
    {
    cnt_1 = 0;
    cnt_2 = 0;
    cnt_3 = 0;
    for ( int k = 0; k < 3; k++ )
    {
      nasti = nasti_a[k];
      Field spectrum0 =
         (Field) ((Tuple) nasti.getSample(i)).getComponent(spectrum_index);
      spectrum_set = (Gridded1DSet) spectrum0.getDomainSet();
      int len = spectrum_set.getLength();
      float[] lo = spectrum_set.getLow();
      float[] hi = spectrum_set.getHi();
      float[][] spectrum_samples = spectrum_set.getSamples(false);
      range_values = spectrum0.getValues();

      if ((band1_lo >= lo[0])&&(band1_hi <= hi[0])) band1 = true;
      if ((band2_lo >= lo[0])&&(band2_hi <= hi[0])) band2 = true;
      if ((band3_lo >= lo[0])&&(band3_hi <= hi[0])) band3 = true;

      if ( band1 )
      {
        for ( int ii = 0; ii < spectrum_samples[0].length; ii++ )
        {
          if (( spectrum_samples[0][ii] >= band1_lo ) &&
              ( spectrum_samples[0][ii] <= band1_hi))
          {
            samples_1[0][cnt_1] = spectrum_samples[0][ii];
            ranges_1[0][cnt_1] = (float) range_values[0][ii];
            cnt_1++;
          }
        }
        band1 = false;
      }
      else if ( band2 )
      {
        for ( int ii = 0; ii < spectrum_samples[0].length; ii++ )
        {
          if (( spectrum_samples[0][ii] >= band2_lo ) &&
              ( spectrum_samples[0][ii] <= band2_hi ))
          {
            samples_2[0][cnt_2] = spectrum_samples[0][ii];
            ranges_2[0][cnt_2] = (float) range_values[0][ii];
            cnt_2++;
          }
        }
        band2 = false;
      }
      else if ( band3 )
      {
        for ( int ii = 0; ii < spectrum_samples[0].length; ii++ )
        {
          if (( spectrum_samples[0][ii] >= band3_lo ) &&
              ( spectrum_samples[0][ii] <= band3_hi ))
          {
            samples_3[0][cnt_3] = spectrum_samples[0][ii];
            ranges_3[0][cnt_3] = (float) range_values[0][ii];
            cnt_3++;
          }
        }
        band3 = false;
      }

      System.arraycopy( samples_1[0], 0, new_spectrum[0], 0, cnt_1);
      System.arraycopy( samples_2[0], 0, new_spectrum[0], cnt_1, cnt_2);
      System.arraycopy( samples_3[0], 0, new_spectrum[0], (cnt_1+cnt_2), cnt_3);
      System.arraycopy( ranges_1[0], 0, new_range[0], 0, cnt_1);
      System.arraycopy( ranges_2[0], 0, new_range[0], cnt_1, cnt_2);
      System.arraycopy( ranges_3[0], 0, new_range[0], (cnt_1+cnt_2), cnt_3);
    }
      spectrum_set = new Gridded1DSet(spectrum_set.getType(),
                         new_spectrum, n_wnum_obs );
      FlatField f_field = new FlatField( spectrum_type, spectrum_set );
      f_field.setSamples( new_range );
      spectrum_field.setSample(i, f_field);
    }

    spectrum_field_ref.setData(spectrum_field);

    samples_1 = null;
    samples_2 = null;
    samples_3 = null;
    ranges_1 = null;
    ranges_2 = null;
    ranges_3 = null;
//*----------------------

    float[] lows = spectrum_set.getLow();
    float[] his = spectrum_set.getHi();
    int spectrum_set_length = spectrum_set.getLength();

    // range of wave numbers
    wnum_low = lows[0];
    wnum_hi = his[0];
    System.out.println(wnum_low);
    System.out.println(wnum_hi);
    System.out.println(spectrum_set_length);

    // set up image
    // image_set = new Integer2DSet(nelements, nlines);
    image_set = new Linear2DSet(-48.75, 48.75, 13,
                                -0.5, (double) (nlines - 0.5), nlines);
    image_line = new RealType("image_line");
    image_element = new RealType("image_element");
    image_domain = new RealTupleType(image_element, image_line);
    image_type = new FunctionType(image_domain, atmosphericRadiance);

    // create image data object for display and initialize radiance
    // array to missing
    FlatField image = new FlatField(image_type, image_set);
    double[][] radiances = new double[1][nelements * nlines];
    for (int i=0; i<nelements * nlines; i++) {
      radiances[0][i] = Double.NaN;
    }
    image_ref.setData(image);

    rtvl_ttRef.setData(new FlatField( press_tt, p_domain));
    rtvl_wvRef.setData(new FlatField( press_wv, p_domain));

    // initial wave number in middle of spectrum
    float wnum_last = (wnum_low + wnum_hi) / 2.0f;
    wnum_last_ref.setData(new Real(wnum1, wnum_last));

    wnum_low_ref.setData(new Real(wnum1, wnum_low));
    wnum_hi_ref.setData(new Real(wnum1, wnum_hi));

    rtvl_obs_sim_ref.setData(new Real(1));
    rtvl_diff_ref.setData(new Real(1));
    prof_opt_ref.setData(new Real(1));

    wnum_low_0 = wnum_low;
    wnum_hi_0 = wnum_hi;

//-------- Done: Initialize -------------

    CellImpl foward_radiance_cell = new CellImpl() {
      double[][] tt_last;
      double[][] wv_last;
      double[][] oz_last;
      float[][] tt_last_f;
      float[][] wv_last_f;
      float[][] oz_last_f;
      FlatField field_tt;
      FlatField field_wv;
      FlatField field_oz;
      double[][] rr_values_a = new double[1][9127];
      boolean first = true;

      public void doAction() throws VisADException, RemoteException
      {
        if (! first )
        {
          field_tt = (FlatField) field_ttRef.getData();
          field_wv = (FlatField) field_wvRef.getData();
          field_oz = (FlatField) field_ozRef.getData();
          try {
            tt_last = field_tt.getValues();
            tt_last_f = Set.doubleToFloat(tt_last);
            wv_last = field_wv.getValues();
            wv_last_f = Set.doubleToFloat(wv_last);
            oz_last = field_oz.getValues();
            oz_last_f = Set.doubleToFloat(oz_last);
          }
          catch ( VisADException e1 ) {
            System.out.println(e1.getMessage());
          }

          nastirte_c( tskin[0], psfc[0], lsfc[0], azen[0], p, tt_last_f[0],
                      wv_last_f[0], oz_last_f[0], nbuse, vn, tb, rr );
          System.arraycopy(rr, 0, rr_values_a[0], 0, 9127);
          try
          {
            field_rr.setSamples(rr_values_a);
            field_rrRef.setData( obs_spectrum.subtract(
                                 field_rr,
                                 Data.WEIGHTED_AVERAGE,
                                 Data.NO_ERRORS ));
          }
          catch ( VisADException e2 ) {
            System.out.println( e2.getMessage() );
          }
          catch ( RemoteException e3 ) {
            System.out.println( e3.getMessage() );
          }
        }
        else {
          first = false;
        }
      }
    };
    foward_radiance_cell.addReference(foward_radiance_ref);

    CellImpl retrieval_cell = new CellImpl() {
      float[] p_out = new float[40*3+25];
      float[] tair = new float[40*3+25];
      double[][] tt_values_0;
      double[][] wv_values_0;
      double[][] oz_values_0;
      boolean first = true;
      int opt;
      int opt2;
      int opt3;
      float[][] rr_f_a;
      float[] dum = new float[50];
      float[][] tt = new float[1][40];
      float[][] wv = new float[1][40];
      public void doAction() throws VisADException, RemoteException
      {
        if (! first )
        {
          float gamt = (float) (((Real)(gamt_ref.getData())).getValue());
          float gamw = (float) (((Real)(gamw_ref.getData())).getValue());
          float gamts = (float) (((Real)(gamts_ref.getData())).getValue());
          float emis = (float) (((Real)(emis_ref.getData())).getValue());
          opt2 = (int) (((Real)(rtvl_obs_sim_ref.getData())).getValue());
          opt3 = (int) (((Real)(rtvl_diff_ref.getData())).getValue());
          gamt = (float)Math.pow(10d, (double)gamt);
          gamts = (float)Math.pow(10d, (double)gamts);
          gamw = (float)Math.pow(10d, (double)gamw);

          tt_values_0 = ((FlatField)field_ttRef.getData()).getValues();
          wv_values_0 = ((FlatField)field_wvRef.getData()).getValues();
          oz_values_0 = ((FlatField)field_ozRef.getData()).getValues();

          for ( int ii = 0; ii < 40; ii++ )
          {
            tair[ii] = (float) tt_values_0[0][ii];
            tair[ii+40] = (float) wv_values_0[0][ii];
            tair[ii+2*40] = (float) oz_values_0[0][ii];
          }
          tair[40+2*40] = tskin[0];

          opt = 1;
          if ( opt == -1 ) {
            double[][] rr_a = new double[1][];
            rr_a[0] = rr;
            rr_f_a = Set.doubleToFloat(rr_a);
          }
          nasti_retrvl_c( opt, opt2, rec, gamt, gamw, gamts, emis, tair, dum, p_out);

          for ( int i = 0; i < 40; i++ ) {
            tt_rtvl[0][i] = p_out[i];
            wv_rtvl[0][i] = p_out[40 + i];
          }

          try
          {
            if ( opt3 == 1 ) {
              ((FlatField)rtvl_ttRef.getData()).setSamples(tt_rtvl);
              ((FlatField)rtvl_wvRef.getData()).setSamples(wv_rtvl);
            }
            else {
              for ( int i = 0; i < 40; i++ ) {
                tt[0][i] = tt_rtvl[0][i] - tt_raob[0][i];
                wv[0][i] = wv_rtvl[0][i] - wv_raob[0][i];
              }
              ((FlatField)rtvl_ttRef.getData()).setSamples(tt);
              ((FlatField)rtvl_wvRef.getData()).setSamples(wv);
            }
          }
          catch ( VisADException e2 ) {
            System.out.println( e2.getMessage() );
          }
          catch ( RemoteException e3 ) {
            System.out.println( e3.getMessage() );
          }
        }
        else {
          first = false;
        }
      }
    };
    retrieval_cell.addReference(retrieval_ref);

    CellImpl do_image_cell = new CellImpl() {
      double[][] radiances;
      boolean first = true;
      public void doAction() throws VisADException, RemoteException
      {
        if (! first ) {
        double radiance;
        radiances = new double[1][nelements * nlines];
        for (int i=0; i<ntimes; i++) {
          Field spectrum =
            (Field) spectrum_field.getSample(i);
          try {
            radiance =
              ((Real) spectrum.evaluate((Real)wnum_last_ref.getData())).getValue();
          }
          catch (VisADException e1) {
            radiance = Double.NaN;
          }
          radiances[0][time_to_sample[i]] = radiance;
        }
        ((FlatField)image_ref.getData()).setSamples(radiances);
        }
        else {
          first = false;
        }
      }
    };
    do_image_cell.addReference(wnum_last_ref);

      // CellImpl to change spectrum when user moves white_cursor
      CellImpl white_cursor_cell = new CellImpl() {
        int line;
        Set domain;
        float[][] samples;
        double[][] new_range;
        FunctionType f_type;
        boolean first = true;
        public void doAction() throws VisADException, RemoteException {
        if ( ! first )
        {
          int i;
          red_bar_ref.setData(null);
          RealTuple white_cursor = (RealTuple) white_cursor_ref.getData();
          float elem = (float) ((Real) white_cursor.getComponent(0)).getValue();
          int element =
            (int) Math.round((elem + 45.0) / 7.5);
          line =
            (int) Math.round( ((Real) white_cursor.getComponent(1)).getValue() );
          if (0 <= line && line < nlines && 0 <= element && element < nelements) {
            i = sample_to_time[line][element];
          }
          else {
            i = -1;
          }
          if (i >= 0) {
            obs_spectrum =
            (Field)spectrum_field.getSample(i);

            domain = obs_spectrum.getDomainSet();
            int length = domain.getLength();
            samples = domain.getSamples(false);
            double[][] samples_d = Set.floatToDouble(samples);
            double[][] range = obs_spectrum.getValues();
            new_range = new double[2][length];
            Set scatter_domain = new Linear1DSet(0d, (double)length, length);
            System.arraycopy(samples_d[0], 0, new_range[0], 0, length);
            System.arraycopy(range[0], 0, new_range[1], 0, length);
            f_type = new FunctionType(
                         ((SetType)scatter_domain.getType()).getDomain(),
                         scatter_tuple);
            FlatField scatter_field = new FlatField(f_type, scatter_domain);
            scatter_field.setSamples(new_range);

            spectrum_ref_s.setData(scatter_field);

            field_rrRef.setData( obs_spectrum.subtract(
                                 field_rr,
                                 Data.WEIGHTED_AVERAGE,
                                 Data.NO_ERRORS) );
          }
          else {
            System.out.println("------------index: "+i);
            spectrum_ref.setData(null);
            spectrum_ref_s.setData(null);
          }
          rec = i;
         }
         else {
           first = false;
         }
        }
      };
      // link white_cursor to white_cursor_cell
      white_cursor_cell.addReference(white_cursor_ref);

    CellImpl reset_cell = new CellImpl() {
      boolean first = true;
      FlatField field_tt, field_wv, field_oz;
      public void doAction()
      {
        if (! first) {
        try
        {
          field_tt = new FlatField(press_tt, p_domain);
          field_wv = new FlatField(press_wv, p_domain);
          field_oz = new FlatField(press_oz, p_domain);
          field_tt.setSamples(tt_values);
          field_wv.setSamples(wv_values);
          field_oz.setSamples(oz_values);

          field_ttRef.setData(field_tt);
          field_wvRef.setData(field_wv);
          field_ozRef.setData(field_oz);

          foward_radiance_ref.setData(new Real(0.0));
        }
        catch ( VisADException ex ) {
          System.out.println( ex.getMessage() );
        }
        catch ( RemoteException ex ) {
          System.out.println( ex.getMessage() );
        }
        }
        else {
          first = false;
        }
      }
    };
    reset_cell.addReference(reset_ref);

    CellImpl diff_cell = new CellImpl() {
      boolean first = true;
      float[][] tt = new float[1][40];
      float[][] wv = new float[1][40];
      int opt;
      public void doAction()
      {
        if (! first) {
        try
        {
          opt = (int) (((Real)(rtvl_diff_ref.getData())).getValue());
          if ( opt == 1 )
          {
            for ( int ii = 0; ii < 40; ii++) {
              tt[0][ii] = tt_rtvl[0][ii];
              wv[0][ii] = wv_rtvl[0][ii];
            }
          }
          else
          {
            for ( int ii = 0; ii < 40; ii++ ) {
              tt[0][ii] = tt_rtvl[0][ii] - tt_raob[0][ii];
              wv[0][ii] = wv_rtvl[0][ii] - wv_raob[0][ii];
            }
          }

          ((FlatField)rtvl_ttRef.getData()).setSamples(tt);
          ((FlatField)rtvl_wvRef.getData()).setSamples(wv);
        }
        catch ( VisADException ex ) {
          System.out.println( ex.getMessage() );
        }
        catch ( RemoteException ex ) {
          System.out.println( ex.getMessage() );
        }
        }
        else {
          first = false;
        }
      }
    };
    diff_cell.addReference(rtvl_diff_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("NAST-I VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    ChannelImage channel_image1 = new ChannelImage();

    frame.getContentPane().add(channel_image1);

    // set size of JFrame and make it visible
    // frame.setSize(400, 900);
    frame.setSize(1200, 900);
    frame.setVisible(true);

    if (server_server != null)
    {
      RemoteDataReferenceImpl[] refs = new RemoteDataReferenceImpl[n_refs];

      refs[0] = new RemoteDataReferenceImpl((DataReferenceImpl)image_ref);
      refs[1] = new RemoteDataReferenceImpl((DataReferenceImpl)white_cursor_ref);
      refs[2] = new RemoteDataReferenceImpl((DataReferenceImpl)red_cursor_ref);
      refs[3] = new RemoteDataReferenceImpl((DataReferenceImpl)red_bar_ref);
      refs[4] = new RemoteDataReferenceImpl((DataReferenceImpl)field_ttRef);
      refs[5] = new RemoteDataReferenceImpl((DataReferenceImpl)field_wvRef);
      refs[6] = new RemoteDataReferenceImpl((DataReferenceImpl)field_ozRef);
      refs[7] = new RemoteDataReferenceImpl((DataReferenceImpl)spectrum_ref);
      refs[8] = new RemoteDataReferenceImpl((DataReferenceImpl)spectrum_ref_s);
      refs[9] = new RemoteDataReferenceImpl((DataReferenceImpl)field_rrRef);
      refs[10] = new RemoteDataReferenceImpl((DataReferenceImpl)rtvl_ttRef);
      refs[11] = new RemoteDataReferenceImpl((DataReferenceImpl)rtvl_wvRef);
      refs[12] = new RemoteDataReferenceImpl((DataReferenceImpl)gamt_ref);
      refs[13] = new RemoteDataReferenceImpl((DataReferenceImpl)gamw_ref);
      refs[14] = new RemoteDataReferenceImpl((DataReferenceImpl)foward_radiance_ref);
      refs[15] = new RemoteDataReferenceImpl((DataReferenceImpl)retrieval_ref);
      refs[16] = new RemoteDataReferenceImpl((DataReferenceImpl)spectrum_field_ref);
      refs[17] = new RemoteDataReferenceImpl((DataReferenceImpl)wnum_last_ref);
      refs[18] = new RemoteDataReferenceImpl((DataReferenceImpl)wnum_low_ref);
      refs[19] = new RemoteDataReferenceImpl((DataReferenceImpl)wnum_hi_ref);
      refs[20] = new RemoteDataReferenceImpl((DataReferenceImpl)setBand_ref);
      refs[21] = new RemoteDataReferenceImpl((DataReferenceImpl)recenter_ref);
      refs[22] = new RemoteDataReferenceImpl((DataReferenceImpl)reset_ref);
      refs[23] = new RemoteDataReferenceImpl((DataReferenceImpl)gamts_ref);
      refs[24] = new RemoteDataReferenceImpl((DataReferenceImpl)emis_ref);
      refs[25] = new RemoteDataReferenceImpl((DataReferenceImpl)rtvl_option_ref);
      refs[26] = new RemoteDataReferenceImpl((DataReferenceImpl)zoom_ref);
      refs[27] = new RemoteDataReferenceImpl((DataReferenceImpl)rtvl_obs_sim_ref);
      refs[28] = new RemoteDataReferenceImpl((DataReferenceImpl)rtvl_diff_ref);
      refs[29] = new RemoteDataReferenceImpl((DataReferenceImpl)prof_opt_ref);
      refs[30] = new RemoteDataReferenceImpl((DataReferenceImpl)field_tt_rRef);
      refs[31] = new RemoteDataReferenceImpl((DataReferenceImpl)field_wv_rRef);
      refs[32] = new RemoteDataReferenceImpl((DataReferenceImpl)field_oz_rRef);

      server_server.setDataReferences(refs);
    }

  }//- end: setupServer

  void setupClient() throws VisADException, RemoteException
  {
    client = true;
    RemoteDataReference[] refs = client_server.getDataReferences();
    if (refs == null) {
      System.out.println("Cannot connect to server");
      System.exit(0);
    }

    image_ref = refs[0];
    white_cursor_ref = refs[1];
    red_cursor_ref = refs[2];
    red_bar_ref = refs[3];
    field_ttRef = refs[4];
    field_wvRef = refs[5];
    field_ozRef = refs[6];
    spectrum_ref = refs[7];
    spectrum_ref_s = refs[8];
    field_rrRef = refs[9];
    rtvl_ttRef = refs[10];
    rtvl_wvRef = refs[11];
    gamt_ref = refs[12];
    gamw_ref = refs[13];
    foward_radiance_ref = refs[14];
    retrieval_ref = refs[15];
    spectrum_field_ref = refs[16];
    wnum_last_ref = refs[17];
    wnum_low_ref = refs[18];
    wnum_hi_ref = refs[19];
    setBand_ref = refs[20];
    recenter_ref = refs[21];
    reset_ref = refs[22];
    gamts_ref = refs[23];
    emis_ref = refs[24];
    rtvl_option_ref = refs[25];
    zoom_ref = refs[26];
    rtvl_obs_sim_ref = refs[27];
    rtvl_diff_ref = refs[28];
    prof_opt_ref = refs[29];
    field_tt_rRef = refs[30];
    field_wv_rRef = refs[31];
    field_oz_rRef = refs[32];

    RealTupleType rt_type = ((FunctionType)image_ref.getType()).getDomain();
    image_element = (RealType)rt_type.getComponent(0);
    image_line = (RealType)rt_type.getComponent(1);

    rt_type = ((FunctionType)field_rrRef.getType()).getDomain();
    wnum1 = (RealType)rt_type.getComponent(0);
    atmosphericRadiance = (RealType)((FunctionType)field_rrRef.getType()).getRange();
    red_bar_type = (FunctionType)red_bar_ref.getType();

    rt_type = ((FunctionType)field_ttRef.getType()).getDomain();
    pressure = (RealType)rt_type.getComponent(0);
    temperature = (RealType)((FunctionType)field_ttRef.getType()).getRange();
    watervapor = (RealType)((FunctionType)field_wvRef.getType()).getRange();
    ozone = (RealType)((FunctionType)field_ozRef.getType()).getRange();

    wnum_low = (float)((Real)wnum_low_ref.getData()).getValue();
    wnum_hi = (float)((Real)wnum_hi_ref.getData()).getValue();
    wnum_low_0 = wnum_low;
    wnum_hi_0 = wnum_hi;

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("NAST-I VisAD Application");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    ChannelImage channel_image1 = new ChannelImage();

    frame.getContentPane().add(channel_image1);

    // set size of JFrame and make it visible
    // frame.setSize(400, 900);
    frame.setSize(1200, 900);
    frame.setVisible(true);

  }//- end: setupClient


  /** this make an image of one NAST-I channel, with a JTextField
      for channel selection, a LabeledRGBWidget for pixel colors
      and a spectrum display */
  class ChannelImage extends JPanel
        implements ActionListener, ItemListener, ScalarMapListener {
    // array for image radiances
    double[][] radiances;
    // image data object for display
    FlatField image;

    // ScalarMap for atmosphericRadiance in the spectrum display
    ScalarMap radiance_map2;

    // some GUI components
    JPanel wpanel, s_panel;
    JLabel wnum_label;
    JTextField wnum_field;
    JPanel zpanel;
    JCheckBox wnum_zoom;
    JButton recenter;

    // last valid wave number from text field
    float wnum_last;

    // true to zoom whum1 range in spectrum display
    boolean wzoom;

    // flag to skip one red_cursor_cell event
    boolean skip_red = false;

    // cursor display coordinates
    double[] cur = null;
    double[] scale_offset = new double[2];
    double[][] scale_s = new double[2][2];
    double[] dum_1 = new double[2];
    double[] dum_2 = new double[2];

    DisplayImpl img_display;
    DisplayImpl spectrumDisplay;
    DisplayImpl spectrum_diff_display;
    DisplayImpl raobDisplay;
    DisplayImpl rtvl_display;
    RealTuple init_white_cursor;

    double[][] rr_values_a;

    ConstantMap[] red = new ConstantMap[3];
    ConstantMap[] green = new ConstantMap[3];
    ConstantMap[] blue = new ConstantMap[3];
    ConstantMap[] yellow = new ConstantMap[3];
    ConstantMap[] orange = new ConstantMap[3];
    ConstantMap[] purple = new ConstantMap[3];

    // construct a image-spectrum interface
    ChannelImage() throws VisADException, RemoteException {

      rr_values_a = new double[1][9127];
      if (java2d) {
        cur = new double[2];
      }
      else {
        cur = new double[3];
      }

      Border etchedBorder5 =
        new CompoundBorder(new EtchedBorder(),
                           new EmptyBorder(5, 5, 5, 5));

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setAlignmentY(JPanel.TOP_ALIGNMENT);
      setAlignmentX(JPanel.LEFT_ALIGNMENT);

      JPanel l_panel = new JPanel();
      l_panel.setLayout(new BoxLayout(l_panel, BoxLayout.Y_AXIS));
      l_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      l_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
      add(l_panel);

      JPanel c_panel = new JPanel();
      c_panel.setLayout(new BoxLayout(c_panel, BoxLayout.Y_AXIS));
      c_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      c_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
      add(c_panel);

      JPanel r_panel = new JPanel();
      r_panel.setLayout(new BoxLayout(r_panel, BoxLayout.Y_AXIS));
      r_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      r_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
      add(r_panel);

      JPanel s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.Y_AXIS));
   //-s_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      s_panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      s_panel.setBorder(etchedBorder5);

      s_panel.add(new JLabel("NAST-I Foward Radiance and Retrieval Application"));
      s_panel.add(new JLabel("using VisAD  -  see:"));
      s_panel.add(new JLabel("  "));
      s_panel.add(new JLabel("  http://www.ssec.wisc.edu/~billh/visad.html"));
      s_panel.add(new JLabel("  "));
      s_panel.add(new JLabel("for more information about VisAD."));
      s_panel.add(new JLabel("  "));
      s_panel.add(new JLabel("William Hibbard, Paolo Antonelli and Tom Rink"));
      s_panel.add(new JLabel("Space Science and Engineering Center"));
      s_panel.add(new JLabel("University of Wisconsin - Madison"));
      s_panel.add(new JLabel("  "));
      s_panel.add(new JLabel("  "));
      s_panel.add(new JLabel("  "));

   //-l_panel.add(Box.createRigidArea(new Dimension(0,21)));
      l_panel.add(s_panel);

      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.Y_AXIS));
   //-s_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      s_panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      s_panel.setBorder(etchedBorder5);
      VisADSlider gamt_slider =
        new VisADSlider(gamt_ref, -1f, 5f, 1f, RealType.Generic, "gamt");
      s_panel.add(gamt_slider);

      VisADSlider gamw_slider =
        new VisADSlider(gamw_ref, -1f, 5f, 1f, RealType.Generic, "gamw");
      s_panel.add(gamw_slider);

      VisADSlider gamts_slider =
        new VisADSlider(gamts_ref, -3f, 3f, -3f, RealType.Generic, "gamts");
      s_panel.add(gamts_slider);

      VisADSlider emis_slider =
        new VisADSlider(emis_ref, 0f, 1f, 1f, RealType.Generic, "emis");
      s_panel.add(emis_slider);
      l_panel.add(s_panel);

//---------- range control buttons --------

      s_panel = new JPanel();
      s_panel.setLayout( new BoxLayout(s_panel, BoxLayout.X_AXIS) );
      s_panel.setBorder( etchedBorder5 );
      JButton all = new JButton("ALL");
      all.addActionListener(this);
      all.setActionCommand("ALL");
      s_panel.add( all );
      JButton co2_1 = new JButton("CO2_1");
      co2_1.addActionListener(this);
      co2_1.setActionCommand("CO2_1");
      s_panel.add( co2_1 );
      JButton o3 = new JButton("O3");
      o3.addActionListener(this);
      o3.setActionCommand("O3");
      s_panel.add( o3 );
      JButton h2o = new JButton("H2O");
      h2o.addActionListener(this);
      h2o.setActionCommand("H2O");
      s_panel.add( h2o );
      JButton co2_2 = new JButton("CO2_2");
      co2_2.addActionListener(this);
      co2_2.setActionCommand("CO2_2");
      s_panel.add( co2_2 );

      l_panel.add(s_panel);

      // initial wave number in middle of spectrum
      wnum_last = (float)((Real)wnum_last_ref.getData()).getValue();

    if(!client)
    {
      // white_cursor in image display for selecting spectrum
      init_white_cursor =
        new RealTuple(new Real[] {new Real(image_element, 0.0),
                                  new Real(image_line, 0.0)});
      white_cursor_ref.setData(init_white_cursor);

      // create red_cursor in spectrum display for setting wave number
      red_cursor_ref.setData(wnum_last_ref.getData());
    }

      // create image Display using Java3D in 2-D mode
      if (!java2d) {
        try {
          img_display = new DisplayImplJ3D("image display",
                                            new TwoDDisplayRendererJ3D());
        }
        catch (UnsatisfiedLinkError e) {
          java2d = true;
        }
      }
      if (java2d) {
        img_display = new DisplayImplJ2D("image display");
      }

      GraphicsModeControl mode = img_display.getGraphicsModeControl();
      mode.setLineWidth(1.0f);
      mode.setPointSize(1.0f);

      ScalarMap line_map = new ScalarMap(image_line, Display.YAxis);
      img_display.addMap(line_map);
      line_map.setRange(12.5, -0.5);
      ScalarMap element_map = new ScalarMap(image_element, Display.XAxis);
      img_display.addMap(element_map);
      element_map.setRange(-48.75, 48.75);
      ScalarMap radiance_map1 = new ScalarMap(atmosphericRadiance, Display.RGB);
      img_display.addMap(radiance_map1);

      line_map.getScale( scale_offset, dum_1, dum_2 );
      scale_s[1][0] = scale_offset[0];
      scale_s[1][1] = scale_offset[1];
      element_map.getScale( scale_offset, dum_1, dum_2 );
      scale_s[0][0] = scale_offset[0];
      scale_s[0][1] = scale_offset[1];

      // always autoscale color map to range of radiances
      img_display.setAlwaysAutoScale(true);

      // turn on scales for image line and element
      GraphicsModeControl mode1 = img_display.getGraphicsModeControl();
      // mode1.setScaleEnable(true);

      // make white_cursor and link to display with direct manipulation
      // (so white_cursor can select spectrum)
      ConstantMap[] wmaps = {new ConstantMap(1.0, Display.Blue),
                             new ConstantMap(1.0, Display.Red),
                             new ConstantMap(1.0, Display.Green),
                             new ConstantMap(4.0, Display.PointSize)};

      img_display.addDisplayListener( new CursorClick() );

      if (client) {
        RemoteDisplayImpl remote_img_display = new RemoteDisplayImpl(img_display);
        remote_img_display.addReference(image_ref);
        remote_img_display.addReference(white_cursor_ref, wmaps);
      }
      else {
        img_display.addReference(image_ref);
        img_display.addReference(white_cursor_ref, wmaps);
      }

      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
   //-s_panel.setMaximumSize(new Dimension(750,125));
      s_panel.add(img_display.getComponent());
   //-s_panel.add(Box.createHorizontalStrut(0));
      s_panel.setBorder(etchedBorder5);
      l_panel.add(s_panel);

//---------- RGBWidget

      // create color widget for atmosphericRadiance
      LabeledRGBWidget lw = new LabeledRGBWidget(radiance_map1);
      Dimension d = new Dimension(400, 150);
      lw.setMaximumSize(d);
      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      s_panel.add(lw);
      s_panel.setMaximumSize(new Dimension(400,150));
      s_panel.setBorder(etchedBorder5);
      l_panel.add(s_panel);

//------------------

      // create text field for entering wave number
      wpanel = new JPanel();
      wpanel.setLayout(new BoxLayout(wpanel, BoxLayout.X_AXIS));
      wnum_label = new JLabel("wave number:");
      wnum_field = new JTextField("---");

      //- Bill's suggested hack
      Dimension msize = wnum_field.getMaximumSize();
      Dimension psize = wnum_field.getPreferredSize();
      msize.height = psize.height;
      msize.width = 75;
      wnum_field.setMaximumSize(msize);

      wnum_field.addActionListener(this);
      wnum_field.setActionCommand("wavenum");
      wnum_field.setEnabled(true);
      wpanel.add(wnum_label);
      wpanel.add(wnum_field);
  //- wpanel.add(Box.createRigidArea(new Dimension(10, 0)));
      c_panel.add(wpanel);

      wnum_field.setText(PlotText.shortString(wnum_last));

//-------- observation spectrum display

      // create spectrum Display using Java3D in 2-D mode
      if (java2d) {
        spectrumDisplay = new DisplayImplJ2D("spectrum display");
      }
      else {
        spectrumDisplay = new DisplayImplJ3D("spectrum display",
                                      new TwoDDisplayRendererJ3D());
      }

      mode = spectrumDisplay.getGraphicsModeControl();
      mode.setLineWidth(1.0f);
      mode.setPointSize(1.0f);

      wnum_map = new ScalarMap(wnum1, Display.XAxis);
      spectrumDisplay.addMap(wnum_map);
      radiance_map2 = new ScalarMap(atmosphericRadiance, Display.YAxis);
      spectrumDisplay.addMap(radiance_map2);
      // get autoscale events for atmosphericRadiance, to set length
      // of red_bar
      radiance_map2.addScalarMapListener(this);

      // always autoscale YAxis to range of radiances
   //-spectrumDisplay.setAlwaysAutoScale(true);  (TDR: Dec. 21, 1998)

      // turn on scales for image line and element
      GraphicsModeControl mode2 = spectrumDisplay.getGraphicsModeControl();
      mode2.setScaleEnable(true);

      // link red_bar for display
      ConstantMap[] bmaps = {new ConstantMap(0.0, Display.Blue),
                             new ConstantMap(1.0, Display.Red),
                             new ConstantMap(0.0, Display.Green)};

      // link red_cursor to display with direct manipulation
      // (so red_cursor can select wave number)
      ConstantMap[] rmaps = {new ConstantMap(-1.0, Display.YAxis),
                             new ConstantMap(0.0, Display.Blue),
                             new ConstantMap(1.0, Display.Red),
                             new ConstantMap(0.0, Display.Green),
                             new ConstantMap(4.0, Display.PointSize)};

      if (client) {
        RemoteDisplayImpl remote_spectrumDisplay =
          new RemoteDisplayImpl(spectrumDisplay);
     //-remote_spectrumDisplay.addReference(spectrum_ref);
        remote_spectrumDisplay.addReference(spectrum_ref_s);
        remote_spectrumDisplay.addReference(red_bar_ref, bmaps);
        if (java2d) {
          remote_spectrumDisplay.addReferences(new DirectManipulationRendererJ2D(),
                                     red_cursor_ref, rmaps);
        }
        else {
          remote_spectrumDisplay.addReferences(new DirectManipulationRendererJ3D(),
                                     red_cursor_ref, rmaps);
        }
      }
      else {
     //-spectrumDisplay.addReference(spectrum_ref);
        spectrumDisplay.addReference(spectrum_ref_s);
        spectrumDisplay.addReference(red_bar_ref, bmaps);
        if (java2d) {
          spectrumDisplay.addReferences(new DirectManipulationRendererJ2D(),
                               red_cursor_ref, rmaps);
        }
        else {
          spectrumDisplay.addReferences(new DirectManipulationRendererJ3D(),
                               red_cursor_ref, rmaps);
        }
      }

      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      s_panel.add(spectrumDisplay.getComponent());
      s_panel.setBorder(etchedBorder5);
      c_panel.add(s_panel);

 //------- create button for forward Radiance compute
      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      JButton compute = new JButton("foward Radiance");
      compute.addActionListener(this);
      compute.setActionCommand("fowardRadiance");
      s_panel.add(compute);
      c_panel.add(s_panel);

//--------- Spectrum ( obs - foward Radiance)

    ScalarMap radiance_map;

      // create spectrum Display using Java3D in 2-D mode
      if (java2d) {
        spectrum_diff_display = new DisplayImplJ2D("spectrum_diff_display");
      }
      else {
        spectrum_diff_display = new DisplayImplJ3D("spectrum_diff_display",
                                      new TwoDDisplayRendererJ3D());
      }

      mode = spectrum_diff_display.getGraphicsModeControl();
      mode.setLineWidth(1.0f);
      mode.setPointSize(1.0f);

      (spectrum_diff_display.getGraphicsModeControl()).setPointMode(true);
      wnum_map_diff = new ScalarMap(wnum1, Display.XAxis);
      wnum_map_diff.setRange((double) wnum_low, (double) wnum_hi);
      spectrum_diff_display.addMap(wnum_map_diff);
      radiance_map = new ScalarMap(atmosphericRadiance, Display.YAxis);
      spectrum_diff_display.addMap(radiance_map);

      // always autoscale YAxis to range of radiances
      spectrum_diff_display.setAlwaysAutoScale(true);

      // turn on scales
      mode2 = spectrum_diff_display.getGraphicsModeControl();
      mode2.setScaleEnable(true);

      if (client) {
        RemoteDisplayImpl remote_spectrum_diff_display =
              new RemoteDisplayImpl(spectrum_diff_display);
        remote_spectrum_diff_display.addReference(field_rrRef);
      }
      else {
        spectrum_diff_display.addReference(field_rrRef);
      }

      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      s_panel.add(spectrum_diff_display.getComponent());
      s_panel.setBorder(etchedBorder5);
      c_panel.add(s_panel);

 //--------- create buttons for zooming and center spectrum
      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      wnum_zoom = new JCheckBox("wave number zoom", false);
      wnum_zoom.addItemListener(this);
      recenter = new JButton("Recenter");
      recenter.addActionListener(this);
      recenter.setActionCommand("recenter");
      s_panel.add(wnum_zoom);
      s_panel.add(recenter);
      c_panel.add(s_panel);

//----  observation profile (raob) Display  ----------

      red[0] = new ConstantMap(1.0, Display.Red);
      red[1] = new ConstantMap(0.0, Display.Green);
      red[2] = new ConstantMap(0.0, Display.Blue);
      green[0] = new ConstantMap(0.0, Display.Red);
      green[1] = new ConstantMap(1.0, Display.Green);
      green[2] = new ConstantMap(0.0, Display.Blue);
      blue[0] = new ConstantMap(0.0, Display.Red);
      blue[1] = new ConstantMap(0.0, Display.Green);
      blue[2] = new ConstantMap(1.0, Display.Blue);
      yellow[0] = new ConstantMap(1.0, Display.Red);
      yellow[1] = new ConstantMap(1.0, Display.Green);
      yellow[2] = new ConstantMap(0.0, Display.Blue);
      orange[0] = new ConstantMap(1.0, Display.Red);
      orange[1] = new ConstantMap(0.5, Display.Green);
      orange[2] = new ConstantMap(0.5, Display.Blue);
      purple[0] = new ConstantMap(1.0, Display.Red);
      purple[1] = new ConstantMap(0.0, Display.Green);
      purple[2] = new ConstantMap(1.0, Display.Blue);

      if ( java2d ) {
        raobDisplay = new DisplayImplJ2D("sounding display");
      }
      else {
        raobDisplay = new DisplayImplJ3D("sounding display",
                                       new TwoDDisplayRendererJ3D());
      }

      mode = raobDisplay.getGraphicsModeControl();
      mode.setLineWidth(1.0f);
      mode.setPointSize(1.0f);


      ScalarMap pres_Y = new ScalarMap( pressure, Display.YAxis );
      pres_Y.setRange( 1000., 50.);
      raobDisplay.addMap( pres_Y );
      ScalarMap temp_Y = new ScalarMap( temperature, Display.XAxis );
      temp_Y.setScaleColor(new float[] {1.0f, 0.0f, 0.0f});
      raobDisplay.addMap(temp_Y );
      ScalarMap wvap_Y = new ScalarMap( watervapor, Display.XAxis );
      wvap_Y.setScaleColor(new float[] {0.0f, 1.0f, 0.0f});
      raobDisplay.addMap( wvap_Y );
      ScalarMap ozone_Y = new ScalarMap( ozone, Display.XAxis );
      ozone_Y.setScaleColor(new float[] {0.0f, 0.0f, 1.0f});
      raobDisplay.addMap( ozone_Y );
      mode1 = raobDisplay.getGraphicsModeControl();
      mode1.setScaleEnable(true);

      if (!raob) {
         field_tt_rRef.setData(nothing);
         field_wv_rRef.setData(nothing);
         field_oz_rRef.setData(nothing);
       }
       else {
         field_tt_rRef.setData(field_tt_r);
         field_wv_rRef.setData(field_wv_r);
         field_oz_rRef.setData(field_oz_r);
       }


      if (client) {
        RemoteDisplayImpl remote_raobDisplay =
              new RemoteDisplayImpl( raobDisplay );
        if (java2d) {
          remote_raobDisplay.addReferences( new DirectManipulationRendererJ2D(),
                                     field_ttRef, red );
          remote_raobDisplay.addReferences( new DirectManipulationRendererJ2D(),
                                     field_wvRef, green );
          remote_raobDisplay.addReferences( new DirectManipulationRendererJ2D(),
                                     field_ozRef, blue );
          //remote_raobDisplay.addReference( field_tt_rRef, yellow );
          //remote_raobDisplay.addReference( field_wv_rRef, orange);
          //remote_raobDisplay.addReference( field_oz_rRef, purple);
        }
        else {
          remote_raobDisplay.addReferences( new DirectManipulationRendererJ3D(),
                                     field_ttRef, red );
          remote_raobDisplay.addReferences( new DirectManipulationRendererJ3D(),
                                     field_wvRef, green );
          remote_raobDisplay.addReferences( new DirectManipulationRendererJ3D(),
                                     field_ozRef, blue );
          //remote_raobDisplay.addReference( field_tt_rRef, yellow );
          //remote_raobDisplay.addReference( field_wv_rRef, orange );
          //remote_raobDisplay.addReference( field_oz_rRef, purple );
        }
      }
      else {
        if (java2d) {
          raobDisplay.addReferences( new DirectManipulationRendererJ2D(),
                                     field_ttRef, red );
          raobDisplay.addReferences( new DirectManipulationRendererJ2D(),
                                     field_wvRef, green );
          raobDisplay.addReferences( new DirectManipulationRendererJ2D(),
                                     field_ozRef, blue );
          raobDisplay.addReference( field_tt_rRef, yellow );
          raobDisplay.addReference( field_wv_rRef, orange );
          raobDisplay.addReference( field_oz_rRef, purple );
        }
        else {
          raobDisplay.addReferences( new DirectManipulationRendererJ3D(),
                                     field_ttRef, red );
          raobDisplay.addReferences( new DirectManipulationRendererJ3D(),
                                     field_wvRef, green );
          raobDisplay.addReferences( new DirectManipulationRendererJ3D(),
                                     field_ozRef, blue );
          raobDisplay.addReference( field_tt_rRef, yellow );
          raobDisplay.addReference( field_wv_rRef, orange );
          raobDisplay.addReference( field_oz_rRef, purple );
        }
      }

      // create panel for display with border
      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      s_panel.add(raobDisplay.getComponent());
   //-s_panel.add(Box.createHorizontalStrut(0));
      s_panel.setBorder(etchedBorder5);
      r_panel.add(Box.createRigidArea(new Dimension(0,21)));
      r_panel.add(s_panel);
   //-r_panel.add(Box.createRigidArea(new Dimension(0,26)));
//------- create button for observation/simulation mode ----
      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      obs_sim_button = new JButton("obs > sim");
      obs_sim_button.addActionListener(this);
      obs_sim_button.setActionCommand("obs/sim");
      raob_button = new JButton("raob");
      raob_button.addActionListener(this);
      raob_button.setActionCommand("raob");
      s_panel.add(obs_sim_button);
      s_panel.add(raob_button);
      r_panel.add(s_panel);

//------- Computed (Retrieval) profile

      red[0] = new ConstantMap(1.0, Display.Red);
      red[1] = new ConstantMap(0.0, Display.Green);
      red[2] = new ConstantMap(0.0, Display.Blue);
      green[0] = new ConstantMap(0.0, Display.Red);
      green[1] = new ConstantMap(1.0, Display.Green);
      green[2] = new ConstantMap(0.0, Display.Blue);
      blue[0] = new ConstantMap(0.0, Display.Red);
      blue[1] = new ConstantMap(0.0, Display.Green);
      blue[2] = new ConstantMap(1.0, Display.Blue);

      if ( java2d ) {
        rtvl_display = new DisplayImplJ2D("retrieval display");
      }
      else {
        rtvl_display = new DisplayImplJ3D("retrieval display",
                                       new TwoDDisplayRendererJ3D());
      }

      mode = rtvl_display.getGraphicsModeControl();
      mode.setLineWidth(1.0f);
      mode.setPointSize(1.0f);

      pres_Y = new ScalarMap( pressure, Display.YAxis );
      pres_Y.setRange( 1000., 50.);
      rtvl_display.addMap( pres_Y );
      ScalarMap rtvl_temp_Y = new ScalarMap( temperature, Display.XAxis );
      rtvl_temp_Y.setScaleColor(new float[] {1.0f, 0.0f, 0.0f});
      rtvl_display.addMap( rtvl_temp_Y );
      ScalarMap rtvl_wvap_Y = new ScalarMap( watervapor, Display.XAxis );
      rtvl_wvap_Y.setScaleColor(new float[] {0.0f, 1.0f, 0.0f});
      rtvl_display.addMap( rtvl_wvap_Y );
      mode1 = rtvl_display.getGraphicsModeControl();
      mode1.setScaleEnable(true);

      rtvl_display.setAlwaysAutoScale(true);

      if (client) {
        RemoteDisplayImpl remote_rtvl_display =
          new RemoteDisplayImpl(rtvl_display);
        remote_rtvl_display.addReference( rtvl_ttRef, red );
        remote_rtvl_display.addReference( rtvl_wvRef, green );
      }
      else {
        rtvl_display.addReference( rtvl_ttRef, red );
        rtvl_display.addReference( rtvl_wvRef, green );
      }

      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
      s_panel.add(rtvl_display.getComponent());
      s_panel.setBorder(etchedBorder5);
      r_panel.add(s_panel);

      //- create retrieval controls panel
      s_panel = new JPanel();
      s_panel.setLayout(new BoxLayout(s_panel, BoxLayout.X_AXIS));
   //-s_panel.setBorder(etchedBorder5);
      r_panel.add(s_panel);

      // create button for retrieval compute
      JButton retrieval = new JButton("retrieval");
      retrieval.addActionListener(this);
      retrieval.setActionCommand("retrieval");
      s_panel.add(retrieval);

      JButton reset = new JButton("reset");
      reset.addActionListener(this);
      reset.setActionCommand("resetProfile");
      s_panel.add(reset);

      diff_button = new JButton("rtvl > diff");
      diff_button.addActionListener(this);
      diff_button.setActionCommand("diff");
      s_panel.add(diff_button);

   if (!client)
   {
      // CellImpl to change wave number when user moves red_cursor
      CellImpl red_cursor_cell = new CellImpl() {
        float wnum_low;
        float wnum_hi;
        public void doAction() throws VisADException, RemoteException {
          int i;
          if (skip_red) {
            skip_red = false;
            return;
          }
          Real red_cursor = (Real) red_cursor_ref.getData();
          wnum_low = (float)((Real)wnum_low_ref.getData()).getValue();
          wnum_hi = (float)((Real)wnum_hi_ref.getData()).getValue();
          if (red_cursor == null) return;
          float wnum = (float) red_cursor.getValue();

          if (wnum < wnum_low) {
            wnum = wnum_low;
          }
          if (wnum > wnum_hi) {
            wnum = wnum_hi;
          }
          try {
            wnum_last = wnum;
            wnum_last_ref.setData(red_cursor);
            do_red_bar(wnum);
          }
          catch (VisADException exc) {
            System.out.println(exc.getMessage());
          }
          catch (RemoteException exc) {
            System.out.println(exc.getMessage());
          }
        }
      };
      // link red_cursor to red_cursor_cell
      red_cursor_cell.addReference(red_cursor_ref);
    }

    CellImpl wnum_field_cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        float wnum = (float)((Real)wnum_last_ref.getData()).getValue();
        wnum_field.setText(PlotText.shortString(Math.abs(wnum)));
      }
    };

    CellImpl setBand_cell = new CellImpl() {
      boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (! first ) {
          String band = ((Text)setBand_ref.getData()).getValue();
          setBand(band);
        }
        else {
          first = false;
        }
      }
    };

    CellImpl recenter_cell = new CellImpl() {
      boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (! first ) {
          do_wzoom();
        }
        else {
          first = false;
        }
      }
    };

    CellImpl zoom_cell = new CellImpl() {
      boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if (! first ) {
          String state = ((Text)zoom_ref.getData()).getValue();
          if (state.equals("true")) {
            wzoom = true;
         //-wnum_zoom.setSelected(true);
          }
          else {
            wzoom = false;
         //-wnum_zoom.setSelected(true);
          }
          do_wzoom();
        }
        else {
          first = false;
        }
      }
    };

    CellImpl diff_button_cell = new CellImpl() {
      boolean first = true;
      int opt;
      public void doAction() throws VisADException, RemoteException {
        if (! first ) {
          opt = (int) (((Real)(rtvl_diff_ref.getData())).getValue());
          if ( opt == 1 )
          {
            diff_button.setText("rtvl > diff");
          }
          else
          {
            diff_button.setText("diff > rtvl");
          }
        }
        else {
          first = false;
        }
      }
    };

    CellImpl obs_sim_button_cell = new CellImpl() {
      boolean first = true;
      int opt;
      public void doAction() throws VisADException, RemoteException {
        if (! first ) {
          opt = (int) (((Real)(rtvl_obs_sim_ref.getData())).getValue());
          if ( opt == 1 )
          {
            obs_sim_button.setText("obs > sim");
          }
          else
          {
            obs_sim_button.setText("sim > obs");
          }
        }
        else {
          first = false;
        }
      }
    };

    if (!client) {
      wnum_field_cell.addReference(wnum_last_ref);
      setBand_cell.addReference(setBand_ref);
      recenter_cell.addReference(recenter_ref);
      zoom_cell.addReference(zoom_ref);
      diff_button_cell.addReference(rtvl_diff_ref);
      obs_sim_button_cell.addReference(rtvl_obs_sim_ref);
    }
    else {
      RemoteCellImpl remote_wnum_field_cell =
        new RemoteCellImpl(wnum_field_cell);
      remote_wnum_field_cell.addReference(wnum_last_ref);

      RemoteCellImpl remote_setBand_cell =
        new RemoteCellImpl(setBand_cell);
      remote_setBand_cell.addReference(setBand_ref);

      RemoteCellImpl remote_recenter_cell =
        new RemoteCellImpl(recenter_cell);
      remote_recenter_cell.addReference(recenter_ref);

      RemoteCellImpl remote_zoom_cell =
        new RemoteCellImpl(zoom_cell);
      remote_zoom_cell.addReference(zoom_ref);

      RemoteCellImpl remote_diff_button_cell =
        new RemoteCellImpl(diff_button_cell);
      remote_diff_button_cell.addReference(rtvl_diff_ref);

      RemoteCellImpl remote_obs_sim_button_cell =
        new RemoteCellImpl(obs_sim_button_cell);
      remote_obs_sim_button_cell.addReference(rtvl_obs_sim_ref);
    }
  } //- end constructor ChannelImage

    /** update red_bar based on wave number */
    synchronized void do_red_bar(float wnum)
                 throws VisADException, RemoteException {
      double[] rads = radiance_map2.getRange();
      if (rads[0] == rads[0] && rads[1] == rads[1]) {
        float[][] red_bar_set_samples = {{(float) rads[0], (float) rads[1]}};
        Gridded1DSet red_bar_set =
          new Gridded1DSet(atmosphericRadiance, red_bar_set_samples, 2);
        FlatField red_bar = new FlatField(red_bar_type, red_bar_set);
        float[][] red_bar_samples = {{wnum, wnum}};
        red_bar.setSamples(red_bar_samples);
        red_bar_ref.setData(red_bar);
      }
    }

    synchronized void do_wzoom() throws VisADException, RemoteException {
      if (wzoom) {
        float wnum_last = (float)((Real)wnum_last_ref.getData()).getValue();
        wnum_map.setRange((double) (wnum_last - 10.0),
                          (double) (wnum_last + 10.0));
        wnum_map_diff.setRange((double) (wnum_last - 10.0),
                               (double) (wnum_last + 10.0));
      }
      else {
        float wnum_low = (float)((Real)wnum_low_ref.getData()).getValue();
        float wnum_hi = (float)((Real)wnum_hi_ref.getData()).getValue();
        wnum_map.setRange((double) wnum_low, (double) wnum_hi);
        wnum_map_diff.setRange((double) wnum_low, (double) wnum_hi);
      }
    }

    /** respond to autoscale of atmosphericRadiance */
    public void mapChanged(ScalarMapEvent e) {
      float wnum_last;
      if (radiance_map2.equals(e.getScalarMap())) {
        try {
          wnum_last = (float)((Real)wnum_last_ref.getData()).getValue();
          do_red_bar(wnum_last);
        }
        catch (VisADException e2) {
        }
        catch (RemoteException e2) {
        }
      }
    }

    /** respond to user type-ins of wave number */
    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equals("wavenum")) {
        float wnum = Float.NaN;
        try {
          wnum = Float.valueOf(wnum_field.getText()).floatValue();
        }
        catch (NumberFormatException exc) {
          wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
        }

        if (wnum == wnum) {
          if (wnum < wnum_low_0) {
            wnum = wnum_low;
            wnum_field.setText(PlotText.shortString(Math.abs(wnum)));
          }
          if (wnum > wnum_hi_0) {
            wnum = wnum_hi;
            wnum_field.setText(PlotText.shortString(Math.abs(wnum)));
          }
          try {
            wnum_last = wnum;
            wnum_last_ref.setData(new Real(wnum1, wnum));
        //--do_red_bar(wnum);
            do_wzoom();

        //--skip_red = true;
            Real red_cursor = new Real(wnum1, (double) wnum_last);
            red_cursor_ref.setData(red_cursor);
          }
          catch (VisADException exc) {
            wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
          }
          catch (RemoteException exc) {
            wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
          }

        }
        else wnum_field.setText(PlotText.shortString(Math.abs(wnum_last)));
      } // end if (cmd.equals("wavenum"))
      if (cmd.equals("recenter")) {
        try {
          recenter_ref.setData(new Real(0.0));
       //-do_wzoom();
        }
        catch (VisADException exc) {
          System.out.println(exc.getMessage());
        }
        catch (RemoteException exc) {
          System.out.println(exc.getMessage());
        }
      }
      if (cmd.equals("retrieval"))
      {
        try {
          retrieval_ref.setData(new Real(0.0));
        }
        catch (VisADException exc) {
          System.out.println(exc.getMessage());
        }
        catch (RemoteException exc) {
          System.out.println(exc.getMessage());
        }
      }
      if (cmd.equals("fowardRadiance"))
      {
        try {
          foward_radiance_ref.setData(new Real(0.0));
        }
        catch (VisADException exc) {
          System.out.println(exc.getMessage());
        }
        catch (RemoteException exc) {
          System.out.println(exc.getMessage());
        }
      }
      if (cmd.equals("resetProfile"))
      {
        try {
          reset_ref.setData(new Real(0.0));
        }
        catch (VisADException exc) {
          System.out.println(exc.getMessage());
        }
        catch (RemoteException exc) {
          System.out.println(exc.getMessage());
        }
      }
      if (cmd.equals("CO2_1") || cmd.equals("O3") ||
          cmd.equals("H2O") || cmd.equals("CO2_2") ||
          cmd.equals("ALL") )
      {
        try {
          setBand_ref.setData(new Text(cmd));
        }
        catch ( VisADException e4 )  {
          System.out.println( e4.getMessage() );
        }
        catch ( RemoteException e5 ) {
          System.out.println( e5.getMessage() );
        }
      }
      if (cmd.equals("obs/sim"))
      {
        int opt = 1;
        try {
          opt = (int) ((Real)rtvl_obs_sim_ref.getData()).getValue();
        }
        catch ( VisADException e4 )  {
          System.out.println( e4.getMessage() );
        }
        catch ( RemoteException e5 ) {
          System.out.println( e5.getMessage() );
        }
        if (opt == 0) {
          opt = 1;
        }
        else {
          opt = 0;
        }
        try {
          rtvl_obs_sim_ref.setData(new Real(opt));
        }
        catch ( VisADException e6 )  {
          System.out.println( e6.getMessage() );
        }
        catch ( RemoteException e7 ) {
          System.out.println( e7.getMessage() );
        }
      }
      if (cmd.equals("raob")) {
//paolo
        try {
         if (!client) {
          if (raob) {
            raob = false;
            field_tt_rRef.setData(nothing);
            field_wv_rRef.setData(nothing);
            field_oz_rRef.setData(nothing);

          }
          else {
            raob = true;
            field_tt_rRef.setData(field_tt_r);
            field_wv_rRef.setData(field_wv_r);
            field_oz_rRef.setData(field_oz_r);
          }
         }
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }
      if (cmd.equals("diff"))
      {
        int opt = 1;
        try {
          opt = (int) ((Real)rtvl_diff_ref.getData()).getValue();
        }
        catch ( VisADException e4 )  {
          System.out.println( e4.getMessage() );
        }
        catch ( RemoteException e5 ) {
          System.out.println( e5.getMessage() );
        }
        if (opt == 0) {
          opt = 1;
        }
        else {
          opt = 0;
        }
        try {
          rtvl_diff_ref.setData(new Real(opt));
        }
        catch ( VisADException e6 )  {
          System.out.println( e6.getMessage() );
        }
        catch ( RemoteException e7 ) {
          System.out.println( e7.getMessage() );
        }
      }
      if (cmd.equals("prof"))
      {
        int opt = 1;
        try {
          opt = (int) ((Real)prof_opt_ref.getData()).getValue();
        }
        catch ( VisADException e4 )  {
          System.out.println( e4.getMessage() );
        }
        catch ( RemoteException e5 ) {
          System.out.println( e5.getMessage() );
        }
        if (opt == 0) {
          opt = 1;
        }
        else {
          opt = 0;
        }
        try {
          prof_opt_ref.setData(new Real(opt));
        }
        catch ( VisADException e6 )  {
          System.out.println( e6.getMessage() );
        }
        catch ( RemoteException e7 ) {
          System.out.println( e7.getMessage() );
        }
      }
    }

    public void itemStateChanged(ItemEvent e) {
      Object o = e.getItemSelectable();
      boolean on = (e.getStateChange() == ItemEvent.SELECTED);
      if (o == wnum_zoom) {
        try {
          if (on) {
            zoom_ref.setData(new Text("true"));
          }
          else {
            zoom_ref.setData(new Text("false"));
          }
        }
        catch (VisADException e2) {
        }
        catch (RemoteException e2) {
        }
      }
    }

    class CursorClick implements DisplayListener
    {
      RealTuple w_tuple;
      double real_x;
      double real_y;
      public void displayChanged( DisplayEvent e )
      {
        if ( e.getId() == DisplayEvent.MOUSE_PRESSED_CENTER )
        {
          cur = img_display.getDisplayRenderer().getCursor();
          real_x = (cur[0] - scale_s[0][1])/scale_s[0][0];
          real_y = (cur[1] - scale_s[1][1])/scale_s[1][0];
          try {
            w_tuple =
              new RealTuple(new Real[] {new Real(image_element, real_x),
                                        new Real(image_line, real_y)});
            white_cursor_ref.setData( w_tuple );
          }
          catch ( VisADException e3 ) {
          }
          catch ( RemoteException e3 ) {
          }
        }
      }
    }

    synchronized void setBand( String band )
 //-void setBand( String band )
      throws VisADException, RemoteException
    {
      double CO2_1_lo = 700;
      double CO2_1_hi = 800;
      double CO2_1_mp = 750;
      double CO2_2_lo = 2395;
      double CO2_2_hi = 2400;
      double CO2_2_mp = 2397.5;
      double O3_lo = 1025;
      double O3_hi = 1075;
      double O3_mp = 1050;
      double H2O_lo = 1200;
      double H2O_hi = 1600;
      double H2O_mp = 1400;

      double wnum_low = wnum_low_0;
      double wnum_hi = wnum_hi_0;
      double wnum_mp = wnum_hi_0;

      if ( band.equals("CO2_1") ) {
        wnum_map_diff.setRange( CO2_1_lo, CO2_1_hi );
        wnum_map.setRange( CO2_1_lo, CO2_1_hi );
        wnum_low = CO2_1_lo;
        wnum_hi = CO2_1_hi;
        wnum_mp = CO2_1_mp;
      }
      if ( band.equals("CO2_2") ) {
        wnum_map_diff.setRange( CO2_2_lo, CO2_2_hi );
        wnum_map.setRange( CO2_2_lo, CO2_2_hi );
        wnum_low = CO2_2_lo;
        wnum_hi = CO2_2_hi;
        wnum_mp = CO2_2_mp;
      }
      if ( band.equals("O3") ) {
        wnum_map_diff.setRange( O3_lo, O3_hi );
        wnum_map.setRange( O3_lo, O3_hi );
        wnum_low = O3_lo;
        wnum_hi = O3_hi;
        wnum_mp = O3_mp;
      }
      if ( band.equals("H2O") ) {
        wnum_map_diff.setRange( H2O_lo, H2O_hi );
        wnum_map.setRange( H2O_lo, H2O_hi );
        wnum_low = H2O_lo;
        wnum_hi = H2O_hi;
        wnum_mp = H2O_mp;
      }
      if ( band.equals("ALL") ) {
        wnum_map_diff.setRange( (double) wnum_low_0, (double) wnum_hi_0 );
        wnum_map.setRange( (double) wnum_low_0, (double) wnum_hi_0 );
        wnum_low = wnum_low_0;
        wnum_hi = wnum_hi_0;
        wnum_low_ref.setData(new Real(wnum1, wnum_low));
        wnum_hi_ref.setData(new Real(wnum1, wnum_hi));
      }
      else {
        wnum_low_ref.setData(new Real(wnum1, wnum_low));
        wnum_hi_ref.setData(new Real(wnum1, wnum_hi));
        red_cursor_ref.setData(new Real(wnum1, wnum_mp));
      }
    }
  } //- end class ChannelImage

  private native void readProf_c( int i, int i2, float[] a, float[] b, int[] c, float[] d,
                                  float[] p, float[] t, float[] wv, float[] o );

  private native void nastirte_c( float a, float b, int c, float d,
                                    float[] p, float[] t, float[] wv, float[] o,
                                    int[] u, double[] vn, double[] tb, double[] rr );

  private native void nasti_retrvl_c( int opt, int opt2, int rec,
                                      float gamt, float gamw, float gamts, float emis,
                                      float[] tair, float[] rr, float[] pout );
}//- end: Nesti
