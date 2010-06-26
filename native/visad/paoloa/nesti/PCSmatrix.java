//
// PCS.java
//

package visad.paoloa.nesti;

// import needed classes
import java.lang.reflect.InvocationTargetException;
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.util.VisADSlider;
import visad.util.RangeSlider;
import visad.data.netcdf.Plain;
import visad.data.mcidas.BaseMapAdapter;
import visad.matrix.*;
import Jama.*;
import java.rmi.RemoteException;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;

public class PCSmatrix
       implements ActionListener, ScalarMapListener, DisplayListener
{
  /** the width and height of the UI frame */
  public static int WIDTH = 900;
  public static int HEIGHT = 600;

  // number of times
  int ntimes;
  // number of bands
  int nbands;
  // number of principal components
  int npcs;
  // number of channels
  int nchannels;
  // number of levels
  int nlevels;

  // VisAD Tuple data object created from file
  Tuple file_data;
  FieldImpl time_series;
  FlatField eigen_vectors;
  FlatField means;
  FlatField pressures;

  // RealTypes for data
  RealType time;
  RealType band;
  RealType band1;
  RealType band2;
  RealType band3;
  RealType noise_band1;
  RealType noise_band2;
  RealType noise_band3;
  RealType latitude;
  RealType longitude;
  RealType levels;
  RealType temperature;
  RealType watervapor;
  RealType numpcs;
  RealType channels;
  RealType princ_comp;
  RealType band1_nu;
  RealType band2_nu;
  RealType band3_nu;
  RealType band1_mean;
  RealType band2_mean;
  RealType band3_mean;
  RealType band1_kur;
  RealType band2_kur;
  RealType band3_kur;
  RealType pressure;


  // Sets
  Linear1DSet time_set;
  Linear1DSet band_set;
  Linear2DSet eigen_set;
  Linear1DSet levels_set;
  Gridded1DSet band1_set;
  Gridded1DSet band2_set;
  Gridded1DSet band3_set;

  // DataReferenceImpls for VisADSliders
  DataReferenceImpl time_ref;
  DataReferenceImpl num_eigen_ref;

  // DataReferenceImpls for displays
  // bands versus reconstructions
  DataReferenceImpl b1_ref;
  DataReferenceImpl b1r_ref;
  DataReferenceImpl b1d_ref;
  DataReferenceImpl b2_ref;
  DataReferenceImpl b2r_ref;
  DataReferenceImpl b2d_ref;
  DataReferenceImpl b3_ref;
  DataReferenceImpl b3r_ref;
  DataReferenceImpl b3d_ref;

  // noise versus reconstructions
  DataReferenceImpl n1_ref;
  DataReferenceImpl n1r_ref;
  DataReferenceImpl n2_ref;
  DataReferenceImpl n2r_ref;
  DataReferenceImpl n3_ref;
  DataReferenceImpl n3r_ref;

  // sounding lat / lons, and selected sounding
  DataReferenceImpl ll_ref;
  DataReferenceImpl select_ll_ref;
  DataReferenceImpl map_ref;

  // temperature and water vapor profiles
  DataReferenceImpl temp_ref;
  DataReferenceImpl wv_ref;

  // MathTypes for displayed data
  FunctionType b1_func;
  FunctionType b2_func;
  FunctionType b3_func;
  FunctionType n1_func;
  FunctionType n2_func;
  FunctionType n3_func;
  RealTupleType latlon;
  FunctionType latlon_func;
  FunctionType temp_profile;
  FunctionType wv_profile;

  // precomputed helper values for slider_cell
  float[][] eigen_values;
  float[][] mean_values;
  Gridded1DSet pressureSet;
  FlatField ll_field;
  RealTuple[] ll_select;
  FlatField[] tp;
  FlatField[] wvp;

  // display objects
  DisplayImpl displayb1;
  DisplayImpl displayb2;
  DisplayImpl displayb3;
  DisplayImpl displayll;

  Vector sMaps_b1;
  Vector cMaps_b1;
  Vector sMaps_b2;
  Vector cMaps_b2;
  Vector sMaps_b3;
  Vector cMaps_b3;

  int toggle;
  float band1_lo;
  float band1_hi;
  float band2_lo;
  float band2_hi;
  float band3_lo;
  float band3_hi;

  float[][] lls;
  RealTupleType scatter_range_b1;
  RealTupleType scatter_range_b2;
  RealTupleType scatter_range_b3;
  RealType scatter_index;
  FunctionType scatter_type_b1;
  FunctionType scatter_type_b2;
  FunctionType scatter_type_b3;
  final ScalarMap kur1_rgb;
  final ScalarMap kur2_rgb;
  final ScalarMap kur3_rgb;

  DataReferenceImpl zero_b1_ref;
  DataReferenceImpl zero_b2_ref;
  DataReferenceImpl zero_b3_ref;

  ScalarMap lonmap;
  ScalarMap latmap;

  JamaMatrix evectors;


  // type 'java visad.paoloa.PCS file.nc' to run this application
  public static void main(String args[]) throws Exception {
    if (args.length < 1) {

      /* CTR: 29 September 1998 */
      System.out.println("To run this program, type " +
                         "\"java visad.paoloa.PCS file.nc\"");
      System.out.println("where file.nc is a netCDF file containing " +
                         "GIFTS spectra and eigenvector");
      return;
    }
    String evd_file = null;
    boolean make_evd = false;
    for (int ii = 1; ii < args.length; ii++) 
    {
      if ( args[ii].equals("-evd") ) {
        evd_file = args[++ii];
      }
      if ( args[ii].equals("-make") ) {
        make_evd = true;
      }
    }

    PCSmatrix pcs = new PCSmatrix(args[0], evd_file, make_evd);
  }

  public PCSmatrix(String data_file, String evd_file, boolean make_evd)
         throws VisADException, RemoteException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing a NAST-I file
    file_data = (Tuple) plain.open(data_file);
    plain = null;
    System.out.println(file_data.getType());

    // extract the time sequence of spectra
    time_series = (FieldImpl) file_data.getComponent(0);
    eigen_vectors = (FlatField) file_data.getComponent(1);
    means = (FlatField) file_data.getComponent(2);
    pressures = (FlatField) file_data.getComponent(3);

    // extract RealTypes and use them to determine how data are displayed
    FunctionType time_type = (FunctionType) time_series.getType();
    FunctionType eigen_type = (FunctionType) eigen_vectors.getType();
    FunctionType means_type = (FunctionType) means.getType();
    FunctionType pres_type = (FunctionType) pressures.getType();
    time = (RealType)
      ((RealTupleType) time_type.getDomain()).getComponent(0);
    TupleType time_range = (TupleType) time_type.getRange();
    FunctionType tr0 = (FunctionType) time_range.getComponent(0);
    FunctionType tr1 = (FunctionType) time_range.getComponent(1);
    FunctionType tr2 = (FunctionType) time_range.getComponent(2);
    band = (RealType)
      ((RealTupleType) tr0.getDomain()).getComponent(0);
    RealTupleType bands = (RealTupleType) tr0.getRange();
    band1 = (RealType) bands.getComponent(0);
    band2 = (RealType) bands.getComponent(1);
    band3 = (RealType) bands.getComponent(2);
    noise_band1 = (RealType) bands.getComponent(3);
    noise_band2 = (RealType) bands.getComponent(4);
    noise_band3 = (RealType) bands.getComponent(5);
    latlon = (RealTupleType) tr1.getRange();
    latitude = (RealType) latlon.getComponent(0);
    longitude = (RealType) latlon.getComponent(1);
    levels = (RealType)
      ((RealTupleType) tr2.getDomain()).getComponent(0);
    RealTupleType tuplewv = (RealTupleType) tr2.getRange();
    temperature = (RealType) tuplewv.getComponent(0);
    watervapor = (RealType) tuplewv.getComponent(1);

    RealTupleType ed = (RealTupleType) eigen_type.getDomain();
    numpcs = (RealType) ed.getComponent(0);
    channels = (RealType) ed.getComponent(1);
    princ_comp = (RealType) eigen_type.getRange();

    RealTupleType mr = (RealTupleType) means_type.getRange();
    band1_nu = (RealType) mr.getComponent(0);
    band2_nu = (RealType) mr.getComponent(1);
    band3_nu = (RealType) mr.getComponent(2);
    band1_mean = (RealType) mr.getComponent(3);
    band2_mean = (RealType) mr.getComponent(4);
    band3_mean = (RealType) mr.getComponent(5);
    band1_kur = (RealType) mr.getComponent(6);
    band2_kur = (RealType) mr.getComponent(7);
    band3_kur = (RealType) mr.getComponent(8);
    pressure = (RealType) pres_type.getRange();

    scatter_range_b1 = new RealTupleType(band,band1,band1_kur);
    scatter_range_b2 = new RealTupleType(band,band2,band2_kur);
    scatter_range_b3 = new RealTupleType(band,band3,band3_kur);
    scatter_index = new RealType("scatter_index");
    scatter_type_b1 = new FunctionType(scatter_index, scatter_range_b1);
    scatter_type_b2 = new FunctionType(scatter_index, scatter_range_b2);
    scatter_type_b3 = new FunctionType(scatter_index, scatter_range_b3);

    // construct MathTypes for display data
    b1_func = new FunctionType(band, band1);
    b2_func = new FunctionType(band, band2);
    b3_func = new FunctionType(band, band3);
    n1_func = new FunctionType(band, noise_band1);
    n2_func = new FunctionType(band, noise_band2);
    n3_func = new FunctionType(band, noise_band3);

    latlon_func = new FunctionType(time, latlon);
    temp_profile = new FunctionType(pressure, temperature);
    wv_profile = new FunctionType(pressure, watervapor);

    // get Sets
    Tuple tt = (Tuple) time_series.getSample(0);
    time_set = (Linear1DSet) time_series.getDomainSet();
    band_set = (Linear1DSet) ((Field) tt.getComponent(0)).getDomainSet();
    eigen_set = (Linear2DSet) eigen_vectors.getDomainSet();
    levels_set = (Linear1DSet) ((Field) tt.getComponent(2)).getDomainSet();

    // get numbers of various things
    ntimes = time_set.getLength();
    nbands = band_set.getLength();
    int[] lens = eigen_set.getLengths();
    npcs = lens[0];
    nchannels = lens[1];
    if (nchannels != 3 * nbands) {
      throw new VisADException("nchannels " + nchannels + " != 3 * " +
                               nbands + " nbands");
    }
    nlevels = levels_set.getLength();

    //- make zero line for diff/kurtosis display -------------
    //
    float[][] f_array = new float[1][nbands];
    for ( int ii = 0; ii < nbands; ii++) {
      f_array[0][ii] = (float) 0.0;
    }
    FlatField zero_b1 = new FlatField(b1_func, band_set);
    FlatField zero_b2 = new FlatField(b2_func, band_set);
    FlatField zero_b3 = new FlatField(b3_func, band_set);
    zero_b1.setSamples(f_array, false);
    zero_b2.setSamples(f_array, false);
    zero_b3.setSamples(f_array, false);

    //- make wavelength domain set for each band  -------------
    //
    double[][] d_array = means.getValues();
    double[][] nu_vals = new double[1][nbands];
    nu_vals[0] = d_array[0];
    band1_set = new Gridded1DSet(band, Set.doubleToFloat(nu_vals), nbands);
    band1_lo = band1_set.getLowX();
    band1_hi = band1_set.getHiX();
    nu_vals[0] = d_array[1];
    band2_set = new Gridded1DSet(band, Set.doubleToFloat(nu_vals), nbands);
    band2_lo = band2_set.getLowX();
    band2_hi = band2_set.getHiX();
    nu_vals[0] = d_array[2];
    band3_set = new Gridded1DSet(band, Set.doubleToFloat(nu_vals), nbands);
    band3_lo = band3_set.getLowX();
    band3_hi = band3_set.getHiX();


    // System.out.println(ntimes + " " + nbands + " " + npcs + " " + nlevels);

    b1_ref = new DataReferenceImpl("b1_ref");
    b1r_ref = new DataReferenceImpl("b1r_ref");
    b1d_ref = new DataReferenceImpl("b1d_ref");
    b2_ref = new DataReferenceImpl("b2_ref");
    b2r_ref = new DataReferenceImpl("b2r_ref");
    b2d_ref = new DataReferenceImpl("b2d_ref");
    b3_ref = new DataReferenceImpl("b3_ref");
    b3r_ref = new DataReferenceImpl("b3r_ref");
    b3d_ref = new DataReferenceImpl("b3d_ref");
    n1_ref = new DataReferenceImpl("n1_ref");
    n1r_ref = new DataReferenceImpl("n1r_ref");
    n2_ref = new DataReferenceImpl("n2_ref");
    n2r_ref = new DataReferenceImpl("n2r_ref");
    n3_ref = new DataReferenceImpl("n3_ref");
    n3r_ref = new DataReferenceImpl("n3r_ref");
    ll_ref =  new DataReferenceImpl("ll_ref");
    select_ll_ref =  new DataReferenceImpl("select_ll_ref");
    map_ref =  new DataReferenceImpl("map_ref");
    wv_ref =  new DataReferenceImpl("wv_ref");
    temp_ref =  new DataReferenceImpl("temp_ref");
    time_ref =  new DataReferenceImpl("time_ref");
    num_eigen_ref =  new DataReferenceImpl("num_eigen_ref");
    zero_b1_ref = new DataReferenceImpl("zero_b1_ref");
    zero_b2_ref = new DataReferenceImpl("zero_b2_ref");
    zero_b3_ref = new DataReferenceImpl("zero_b3_ref");

    zero_b1_ref.setData(zero_b1);
    zero_b2_ref.setData(zero_b2);
    zero_b3_ref.setData(zero_b3);

    toggle = 0;

    //-- make band_1 display ---------------------------
    //
    sMaps_b1 = new Vector();
    cMaps_b1 = new Vector();
    kur1_rgb = new ScalarMap(band1_kur, Display.RGB);

    displayb1 =
      new DisplayImplJ3D("displayb1", new TwoDDisplayRendererJ3D());

    final ScalarMap bmapb1 = new ScalarMap(band, Display.XAxis);
    bmapb1.addScalarMapListener(this);
    sMaps_b1.addElement(bmapb1);
    displayb1.addMap(bmapb1);

    final ScalarMap smap = new ScalarMap(band1, Display.YAxis);
    sMaps_b1.addElement(smap);
    displayb1.addMap(smap);

    GraphicsModeControl modeb1 = displayb1.getGraphicsModeControl();
    modeb1.setScaleEnable(true);

    ConstantMap[] yellow =
      new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    ConstantMap[] cyan =
      new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    cMaps_b1.addElement(yellow);
    cMaps_b1.addElement(cyan);
    displayb1.setAlwaysAutoScale(true);
    displayb1.addReference(b1_ref, yellow);
    displayb1.addReference(b1r_ref, cyan);

    //-- make band_2 display ---------------------------
    //
    sMaps_b2 = new Vector();
    cMaps_b2 = new Vector();
    kur2_rgb = new ScalarMap(band2_kur, Display.RGB);

    displayb2 =
      new DisplayImplJ3D("displayb2", new TwoDDisplayRendererJ3D());

    final ScalarMap bmapb2 = new ScalarMap(band, Display.XAxis);
    sMaps_b2.addElement(bmapb2);
    displayb2.addMap(bmapb2);

    final ScalarMap smap2 = new ScalarMap(band2, Display.YAxis);
    sMaps_b2.addElement(smap2);
    displayb2.addMap(smap2);

    GraphicsModeControl modeb2 = displayb2.getGraphicsModeControl();
    modeb2.setScaleEnable(true);

    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    cMaps_b2.addElement(yellow);
    cMaps_b2.addElement(cyan);
    displayb2.setAlwaysAutoScale(true);
    displayb2.addReference(b2_ref, yellow);
    displayb2.addReference(b2r_ref, cyan);

    //-- make band_3 display ---------------------------
    //
    sMaps_b3 = new Vector();
    cMaps_b3 = new Vector();
    kur3_rgb = new ScalarMap(band3_kur, Display.RGB);

    displayb3 =
      new DisplayImplJ3D("displayb3", new TwoDDisplayRendererJ3D());

    final ScalarMap bmapb3 = new ScalarMap(band, Display.XAxis);
    sMaps_b3.addElement(bmapb3);
    displayb3.addMap(bmapb3);

    ScalarMap smap3 = new ScalarMap(band3, Display.YAxis);
    sMaps_b3.addElement(smap3);
    displayb3.addMap(smap3);

    GraphicsModeControl modeb3 = displayb3.getGraphicsModeControl();
    modeb3.setScaleEnable(true);

    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    cMaps_b3.addElement(yellow);
    cMaps_b3.addElement(cyan);
    displayb3.setAlwaysAutoScale(true);
    displayb3.addReference(b3_ref, yellow);
    displayb3.addReference(b3r_ref, cyan);

    //-- make band_1 noise display -----------------------
    //
    DisplayImpl displayn1 =
      new DisplayImplJ3D("displayn1", new TwoDDisplayRendererJ3D());
    final ScalarMap bmapn1 = new ScalarMap(band, Display.XAxis);
    displayn1.addMap(bmapn1);
    displayn1.addMap(new ScalarMap(noise_band1, Display.YAxis));
    GraphicsModeControl moden1 = displayn1.getGraphicsModeControl();
    moden1.setScaleEnable(true);
    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    displayn1.addReference(n1_ref, yellow);
    displayn1.addReference(n1r_ref, cyan);

    //-- make band_2 noise display -----------------------
    //
    DisplayImpl displayn2 =
      new DisplayImplJ3D("displayn2", new TwoDDisplayRendererJ3D());
    final ScalarMap bmapn2 = new ScalarMap(band, Display.XAxis);
    displayn2.addMap(bmapn2);
    displayn2.addMap(new ScalarMap(noise_band2, Display.YAxis));
    GraphicsModeControl moden2 = displayn2.getGraphicsModeControl();
    moden2.setScaleEnable(true);
    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    displayn2.addReference(n2_ref, yellow);
    displayn2.addReference(n2r_ref, cyan);

    //-- make band_3 noise display -----------------------
    //
    DisplayImpl displayn3 =
      new DisplayImplJ3D("displayn3", new TwoDDisplayRendererJ3D());
    final ScalarMap bmapn3 = new ScalarMap(band, Display.XAxis);
    displayn3.addMap(bmapn3);
    displayn3.addMap(new ScalarMap(noise_band3, Display.YAxis));
    GraphicsModeControl moden3 = displayn3.getGraphicsModeControl();
    moden3.setScaleEnable(true);
    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    displayn3.addReference(n3_ref, yellow);
    displayn3.addReference(n3r_ref, cyan);

    //-- make map display --------------------------------
    //
    displayll =
      new DisplayImplJ3D("displayll", new TwoDDisplayRendererJ3D());
    displayll.addDisplayListener(this);
      // new DisplayImplJ3D("displayll");
    lonmap = new ScalarMap(longitude, Display.XAxis);
    // ScalarMap lonmap = new ScalarMap(longitude, Display.Longitude);
    displayll.addMap(lonmap);
    latmap = new ScalarMap(latitude, Display.YAxis);
    // ScalarMap latmap = new ScalarMap(latitude, Display.Latitude);
    displayll.addMap(latmap);
    GraphicsModeControl modell = displayll.getGraphicsModeControl();
    modell.setScaleEnable(true);
    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue),
                                new ConstantMap(5.0, Display.PointSize)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red),
                              new ConstantMap(0.5, Display.Green),
                              new ConstantMap(0.5, Display.Blue),
                              new ConstantMap(5.0, Display.PointSize)};
    displayll.addReference(select_ll_ref, yellow);
    displayll.addReference(ll_ref, cyan);
    displayll.addReference(map_ref);

    //-- make atmos profile display -------------------------
    //
    DisplayImpl displayprof =
      new DisplayImplJ3D("displayprof", new TwoDDisplayRendererJ3D());
    displayprof.addMap(new ScalarMap(temperature, Display.XAxis));
    displayprof.addMap(new ScalarMap(watervapor, Display.XAxis));
    ScalarMap pmap = new ScalarMap(pressure, Display.YAxis);
    pmap.setRange(1050.0, 0.0);
    displayprof.addMap(pmap);
    GraphicsModeControl modeprof = displayprof.getGraphicsModeControl();
    modeprof.setScaleEnable(true);
    ConstantMap[] red = {new ConstantMap(0.0, Display.Blue),
                         new ConstantMap(0.0, Display.Green)};
    ConstantMap[] green = {new ConstantMap(0.0, Display.Blue),
                           new ConstantMap(0.0, Display.Red)};
    displayprof.addReference(wv_ref, green);
    displayprof.addReference(temp_ref, red);

    // create a JFrame
    JFrame frame = new JFrame("Principal Components");
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    };
    frame.addWindowListener(l);
    frame.setSize(WIDTH, HEIGHT);
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);

    // create big_panel JPanel in frame
    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.Y_AXIS));
    big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(big_panel);

    // create sliders JPanel
    JPanel sliders = new JPanel();
    sliders.setName("PCS Sliders");
    sliders.setFont(new Font("Dialog", Font.PLAIN, 12));
    sliders.setLayout(new BoxLayout(sliders, BoxLayout.X_AXIS));
    sliders.setAlignmentY(JPanel.TOP_ALIGNMENT);
    sliders.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(sliders);
    // construct VisADSliders linked to Real Data objects and embedded
    // in sliders JPanel
    /*
    sliders.add(new VisADSlider("time", 1, ntimes, 1, 1.0, time_ref,
                                 time));
     */
    time_ref.setData(new Real(time, 1));
    // sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("neigen", 0, npcs, 0, 1.0,
                                 num_eigen_ref,  numpcs));

    JButton button = new JButton("kurtosis");
    button.addActionListener(this);
    button.setActionCommand("kurtosis");
    sliders.add(button);

    RangeSlider rs = new RangeSlider("band range", 0.0f, (float) nbands) {
      public void valuesUpdated() {
        float b_lo;
        float b_hi;
        float scale1 = (band1_hi - band1_lo)/nbands;
        float scale2 = (band2_hi - band2_lo)/nbands;
        float scale3 = (band3_hi - band3_lo)/nbands;

        float[] minmax = getMinMaxValues();
        float min = minmax[0];
        float max = minmax[1];

        try {
          b_lo = band1_lo + scale1*min;
          b_hi = band1_hi - scale1*(nbands-max);
          bmapb1.setRange(b_lo, b_hi);
          bmapn1.setRange(b_lo, b_hi);

          b_lo = band2_lo + scale2*min;
          b_hi = band2_hi - scale2*(nbands-max);
          bmapb2.setRange(b_lo, b_hi);
          bmapn2.setRange(b_lo, b_hi);

          b_lo = band3_lo + scale3*min;
          b_hi = band3_hi - scale3*(nbands-max);
          bmapb3.setRange(b_lo, b_hi);
          bmapn3.setRange(b_lo, b_hi);
        }
        catch (VisADException exc) { }
        catch (RemoteException exc ) { }
      }
    };
    sliders.add(rs);

    // create top display JPanel
    JPanel top = new JPanel();
    top.setName("PCS Sliders");
    top.setFont(new Font("Dialog", Font.PLAIN, 12));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    top.setAlignmentY(JPanel.TOP_ALIGNMENT);
    top.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(top);

    // get Display panels
    JPanel panel1 = (JPanel) displayb1.getComponent();
    JPanel panel2 = (JPanel) displayb2.getComponent();
    JPanel panel3 = (JPanel) displayb3.getComponent();
    JPanel panel4 = (JPanel) displayll.getComponent();

    // make borders for Displays and embed in display_panel JPanel
    Border etchedBorder5 =
      new CompoundBorder(new EtchedBorder(),
                         new EmptyBorder(5, 5, 5, 5));
    panel1.setBorder(etchedBorder5);
    panel2.setBorder(etchedBorder5);
    panel3.setBorder(etchedBorder5);
    panel4.setBorder(etchedBorder5);

    top.add(panel1);
    top.add(panel2);
    top.add(panel3);
    top.add(panel4);

    // create bottom display JPanel
    JPanel bottom = new JPanel();
    bottom.setName("PCS Sliders");
    bottom.setFont(new Font("Dialog", Font.PLAIN, 12));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.setAlignmentY(JPanel.TOP_ALIGNMENT);
    bottom.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(bottom);

    JPanel panel5 = (JPanel) displayn1.getComponent();
    JPanel panel6 = (JPanel) displayn2.getComponent();
    JPanel panel7 = (JPanel) displayn3.getComponent();
    JPanel panel8 = (JPanel) displayprof.getComponent();

    // make borders for Displays and embed in display_panel JPanel
    panel5.setBorder(etchedBorder5);
    panel6.setBorder(etchedBorder5);
    panel7.setBorder(etchedBorder5);
    panel8.setBorder(etchedBorder5);

    bottom.add(panel5);
    bottom.add(panel6);
    bottom.add(panel7);
    bottom.add(panel8);

    // precompute some values
    // compute mean_values
    float[][] values = means.getFloats(false);
    mean_values = new float[3][nbands];
    mean_values[0] = values[3];
    mean_values[1] = values[4];
    mean_values[2] = values[5];

    // compute eigen_values

    if ( evd_file == null )
    {
      values = eigen_vectors.getFloats(false);
      eigen_values = new float[npcs][nchannels];
      for (int j=0; j<npcs; j++) {
        double mag = 0.0;
        double m = 0;
        for (int i=0; i<nchannels; i++) {
          eigen_values[j][i] = values[0][j + npcs * i];
          m += npcs;
          mag += eigen_values[j][i] * eigen_values[j][i];
        }
        float invmag = (float) (1.0 / Math.sqrt(mag));
        for (int i=0; i<nchannels; i++) {
          eigen_values[j][i] *= invmag;
        }
      }
      evectors = new JamaMatrix(Set.floatToDouble(eigen_values));
      values = null;
    }
    else if ( make_evd == false ) 
    {
      FileInputStream fis = new FileInputStream(evd_file);

      ObjectInputStream ois = new ObjectInputStream(fis);
      EigenvalueDecomposition evd = (EigenvalueDecomposition) ois.readObject();

      double[][] tmp_values = (evd.getV()).getArray();
      double[][] d_eigen_values = new double[npcs][nchannels];
      for ( int j=0; j<npcs; j++) {
        for ( int i=0; i<nchannels; i++) { 
          d_eigen_values[j][i] = tmp_values[i][(nchannels-1)-j];
        }
      }

      fis = null;
      ois = null;
      evd = null;
      tmp_values = null;
      Runtime.getRuntime().gc();
      evectors = new JamaMatrix(d_eigen_values);
    }
    else 
    {
      float[][] d_vectors = new float[ntimes][];
      for ( int v = 0; v < ntimes; v++ ) 
      {
        Tuple tp = (Tuple) time_series.getSample(v);
        FlatField ff = (FlatField) tp.getComponent(0);
        float[][] cvalues = ff.getFloats(false);
        float[] b = new float[nchannels];
        for (int k=0; k<3; k++) {
          int kb = k * nbands;
          for (int i=0; i<nbands; i++) {
            b[kb + i] = cvalues[k][i];
          }
        }
        d_vectors[v] = b;
      }
      Matrix c = makeCovarianceMatrix(Set.floatToDouble(d_vectors));
      EigenvalueDecomposition evd = new EigenvalueDecomposition(c);
      FileOutputStream fos = new FileOutputStream(evd_file);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(evd);
      System.exit(0);
    }
    
    // compute pressureSet
    values = pressures.getFloats(false);
    Gridded1DSet pressureSet = new Gridded1DSet(pressure, values, nlevels);
    // compute ll_field and ll_select
    float latmin = Float.MAX_VALUE;
    float latmax = -Float.MAX_VALUE;
    float lonmin = Float.MAX_VALUE;
    float lonmax = -Float.MAX_VALUE;
    float del_lat = 1.0f;
    float del_lon = 1.0f;
    lls = new float[2][ntimes];
    ll_select = new RealTuple[ntimes];
    for (int i=0; i<ntimes; i++) {
      Tuple tup = (Tuple) time_series.getSample(i);
      FlatField ll = (FlatField) tup.getComponent(1);
      values = ll.getFloats(false);
      lls[0][i] = values[0][0];
      lls[1][i] = values[1][0];
      if (lls[0][i] < latmin) latmin = lls[0][i];
      if (lls[0][i] > latmax) latmax = lls[0][i];
      if (lls[1][i] < lonmin) lonmin = lls[1][i];
      if (lls[1][i] > lonmax) lonmax = lls[1][i];
      double[] vals = {values[0][0], values[1][0]};
      ll_select[i] = new RealTuple(latlon, vals);
    }
    ll_field = new FlatField(latlon_func, new Integer1DSet(time, ntimes));
    ll_field.setSamples(lls);
    ll_ref.setData(ll_field);
    // adjust map boundaries
    lonmap.setRange(lonmax, lonmin);
    latmap.setRange(latmin, latmax);
    // get map
    BaseMapAdapter baseMap = new BaseMapAdapter("OUTLSUPW");
    if ( baseMap.isEastPositive() ) {
      baseMap.setEastPositive(false);
    }

    baseMap.setLatLonLimits(latmin-del_lat, latmax+del_lat,
                            lonmin-del_lon, lonmax+del_lon);
    DataImpl map = baseMap.getData();
    map_ref.setData(map);

    // compute tp and wvp
    tp = new FlatField[ntimes];
    wvp = new FlatField[ntimes];
    for (int i=0; i<ntimes; i++) {
      Tuple tup = (Tuple) time_series.getSample(i);
      FlatField twv = (FlatField) tup.getComponent(2);
      values = twv.getFloats(false);
      float[][] t_values = {values[0]};
      tp[i] = new FlatField(temp_profile, pressureSet);
      tp[i].setSamples(t_values, false);
      float[][] wv_values = {values[1]};
      wvp[i] = new FlatField(wv_profile, pressureSet);
      wvp[i].setSamples(wv_values, false);
    }


    // CellImpl to change displays when user moves sliders
    CellImpl slider_cell = new CellImpl() {
      int last_t = -1;
      Tuple tup = null;
      FlatField bn = null;
      float[][] cvalues = null;
      float[] b = null;
      float[] n = null;
      float[] bm = null;
      float[] rb = new float[nchannels];
      float[] rn = new float[nchannels];

      JamaMatrix data_vector = null;
      JamaMatrix noise_vector = null;
      JamaMatrix mean_vector = null;

      public void doAction() throws VisADException, RemoteException 
      {
        Runtime run = Runtime.getRuntime();

        JamaMatrix r_data_vector = null;
        JamaMatrix r_noise_vector = null;
        JamaMatrix evectors_ne = null;
        JamaMatrix trans_data_vector = null;
        JamaMatrix trans_noise_vector = null;

        int t = (int) ((Real) time_ref.getData()).getValue() - 1;
        int n_eigen = (int) ((Real) num_eigen_ref.getData()).getValue();
        if (t < 0 || ntimes <= t || n_eigen < 0 || npcs < n_eigen) {
          System.out.println("time " + t + " or neigens " +
                             n_eigen + " out of bounds");
          return;
        }

        if ( t != last_t ) 
        {
          tup = (Tuple) time_series.getSample(t);
          bn = (FlatField) tup.getComponent(0);
          cvalues = bn.getFloats(false);
          b = new float[nchannels];
          n = new float[nchannels];
          bm = new float[nchannels];
          for (int k=0; k<3; k++) {
            int kb = k * nbands;
            for (int i=0; i<nbands; i++) {
              b[kb + i] = cvalues[k][i];
              n[kb + i] = cvalues[3 + k][i];
              bm[kb + i] = mean_values[k][i];
            }
          }
 
          float[][] b_2 = new float[1][];
          b_2[0] = b;
          float[][] bm_2 = new float[1][];
          bm_2[0] = bm;
          float[][] n_2 = new float[1][];
          n_2[0] = n;

          try {
            data_vector = new JamaMatrix(Set.floatToDouble(b_2));
            noise_vector = new JamaMatrix(Set.floatToDouble(n_2));
            mean_vector = new JamaMatrix(Set.floatToDouble(bm_2));
          } catch (Exception e) {
            e.printStackTrace();
            return;
          }
          select_ll_ref.setData(ll_select[t]);
          last_t = t;
        }

        JamaMatrix tmp_vector = null;
        try {
          if (n_eigen > 0 )
          {
            evectors_ne= evectors.getMatrix(0, n_eigen-1, 0, nchannels-1);
            tmp_vector = data_vector.minus(mean_vector);
            trans_data_vector = evectors_ne.times(tmp_vector.transpose());
         //-trans_noise_vector = evectors_ne.times(noise_vector.transpose());
            r_data_vector =
              (evectors_ne.transpose()).times(trans_data_vector);
            r_data_vector = r_data_vector.plusEquals(mean_vector.transpose());
         //-r_noise_vector = (evectors_ne.transpose()).times(trans_noise_vector);
            tmp_vector = null;
            evectors_ne = null;
            trans_data_vector = null;
         //-trans_noise_vector = null;
          }
          else {
            r_data_vector = mean_vector.transpose();
          }
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }

        double[][] d_values = r_data_vector.getValues();
     //-double[][] n_values = r_noise_vector.getValues();
        for ( int kk = 0; kk < rb.length; kk++ ) {
          rb[kk] = (float) d_values[0][kk];
       //-rn[kk] = (float) n_values[0][kk];
        }
        r_data_vector = null;
        r_noise_vector = null;

        float[][] rvalues = new float[6][nbands];
        for (int k=0; k<3; k++) {
          int kb = k * nbands;
          for (int i=0; i<nbands; i++) {
            rvalues[k][i] = rb[kb + i];
            rvalues[3 + k][i] = rn[kb + i];
          }
        }

        //- retrieve statistics fields
        //
        FlatField stat_field = (FlatField)file_data.getComponent(2);
        double[][] stats = stat_field.getValues();

        float[][] vals = {cvalues[0]};
        FlatField b1 = new FlatField(b1_func, band1_set);
        b1.setSamples(vals, false);
        b1_ref.setData(b1);
        vals = new float[][] {rvalues[0]};
        FlatField b1r = new FlatField(b1_func, band1_set);
        b1r.setSamples(vals, false);
        b1r_ref.setData(b1r);

        //- compute (band1 - band1_reconstructed) scatter plots
        //
        double[][] diff_vals = ((FlatField)b1.subtract(b1r)).getValues();
        float[][] new_vals = new float[3][];
        Set set = b1.getDomainSet();
        float[][] samps = set.getSamples();
        new_vals[0] = samps[0];
        new_vals[1] = (Set.doubleToFloat(diff_vals))[0];
        new_vals[2] = (Set.doubleToFloat(stats))[6];
        Integer1DSet iset = new Integer1DSet(scatter_index, nbands);
        FlatField scatter_field = new FlatField(scatter_type_b1, iset);
        scatter_field.setSamples(new_vals, false);
        b1d_ref.setData(scatter_field);

        vals = new float[][] {cvalues[1]};
        FlatField b2 = new FlatField(b2_func, band2_set);
        b2.setSamples(vals, false);
        b2_ref.setData(b2);
        vals = new float[][] {rvalues[1]};
        FlatField b2r = new FlatField(b2_func, band2_set);
        b2r.setSamples(vals, false);
        b2r_ref.setData(b2r);

        //- compute (band2 - band2_reconstructed) scatter plots
        //
        diff_vals = ((FlatField)b2.subtract(b2r)).getValues();
        new_vals = new float[3][];
        set = b2.getDomainSet();
        samps = set.getSamples();
        new_vals[0] = samps[0];
        new_vals[1] = (Set.doubleToFloat(diff_vals))[0];
        new_vals[2] = (Set.doubleToFloat(stats))[7];
        iset = new Integer1DSet(scatter_index, nbands);
        scatter_field = new FlatField(scatter_type_b2, iset);
        scatter_field.setSamples(new_vals, false);
        b2d_ref.setData(scatter_field);

        vals = new float[][] {cvalues[2]};
        FlatField b3 = new FlatField(b3_func, band3_set);
        b3.setSamples(vals, false);
        b3_ref.setData(b3);
        vals = new float[][] {rvalues[2]};
        FlatField b3r = new FlatField(b3_func, band3_set);
        b3r.setSamples(vals, false);
        b3r_ref.setData(b3r);

        //- compute (band3 - band1_reconstructed) scatter plots
        //
        diff_vals = ((FlatField)b3.subtract(b3r)).getValues();
        new_vals = new float[3][];
        set = b3.getDomainSet();
        samps = set.getSamples();
        new_vals[0] = samps[0];
        new_vals[1] = (Set.doubleToFloat(diff_vals))[0];
        new_vals[2] = (Set.doubleToFloat(stats))[8];
        iset = new Integer1DSet(scatter_index, nbands);
        scatter_field = new FlatField(scatter_type_b3, iset);
        scatter_field.setSamples(new_vals, false);
        b3d_ref.setData(scatter_field);

        //-- noise band1  ----------------------------------
        //
        vals = new float[][] {cvalues[3]};
        FlatField n1 = new FlatField(n1_func, band1_set);
        n1.setSamples(vals, false);
        n1_ref.setData(n1);
        vals = new float[][] {rvalues[3]};
        FlatField n1r = new FlatField(n1_func, band1_set);
        n1r.setSamples(vals, false);
        n1r_ref.setData(n1r);

        //-- noise band2  -----------------------------------
        //
        vals = new float[][] {cvalues[4]};
        FlatField n2 = new FlatField(n2_func, band2_set);
        n2.setSamples(vals, false);
        n2_ref.setData(n2);
        vals = new float[][] {rvalues[4]};
        FlatField n2r = new FlatField(n2_func, band2_set);
        n2r.setSamples(vals, false);
        n2r_ref.setData(n2r);

        //- noise band3  ------------------------------------
        //
        vals = new float[][] {cvalues[5]};
        FlatField n3 = new FlatField(n3_func, band3_set);
        n3.setSamples(vals, false);
        n3_ref.setData(n3);
        vals = new float[][] {rvalues[5]};
        FlatField n3r = new FlatField(n3_func, band3_set);
        n3r.setSamples(vals, false);
        n3r_ref.setData(n3r);

     //-select_ll_ref.setData(ll_select[t]);

        temp_ref.setData(tp[t]);
        wv_ref.setData(wvp[t]);
     //-Runtime.getRuntime().gc();
      }
    };
    // link sliders to slider_cell
    slider_cell.addReference(time_ref);
    slider_cell.addReference(num_eigen_ref);


    // make the JFrame visible
    frame.setVisible(true);
  }

  public void actionPerformed(ActionEvent e)
  {
    String cmd = e.getActionCommand();

    if ( cmd.equals("kurtosis") ) {
      if ( toggle == 0 ) {
        try {
          displayb1.removeAllReferences();
          displayb1.clearMaps();
          displayb1.addMap((ScalarMap)sMaps_b1.elementAt(0));
          displayb1.addMap((ScalarMap)sMaps_b1.elementAt(1));
          displayb1.addMap(kur1_rgb);
          displayb1.addReference(b1d_ref);

          displayb2.removeAllReferences();
          displayb2.clearMaps();
          displayb2.addMap((ScalarMap)sMaps_b2.elementAt(0));
          displayb2.addMap((ScalarMap)sMaps_b2.elementAt(1));
          displayb2.addMap(kur2_rgb);
          displayb2.addReference(b2d_ref);

          displayb3.removeAllReferences();
          displayb3.clearMaps();
          displayb3.addMap((ScalarMap)sMaps_b3.elementAt(0));
          displayb3.addMap((ScalarMap)sMaps_b3.elementAt(1));
          displayb3.addMap(kur3_rgb);
          displayb3.addReference(b3d_ref);
        }
        catch (VisADException exc) {
          System.out.println(exc.getMessage());
        }
        catch (RemoteException exc) {
          System.out.println(exc.getMessage());
        }
        toggle = 1;
      }
      else {
        try {
          displayb1.removeAllReferences();
          displayb1.clearMaps();
          displayb1.addMap((ScalarMap)sMaps_b1.elementAt(0));
          displayb1.addMap((ScalarMap)sMaps_b1.elementAt(1));
          displayb1.addReference(b1_ref, (ConstantMap[])cMaps_b1.elementAt(0));
          displayb1.addReference(b1r_ref, (ConstantMap[])cMaps_b1.elementAt(1));

          displayb2.removeAllReferences();
          displayb2.clearMaps();
          displayb2.addMap((ScalarMap)sMaps_b2.elementAt(0));
          displayb2.addMap((ScalarMap)sMaps_b2.elementAt(1));
          displayb2.addReference(b2_ref, (ConstantMap[])cMaps_b2.elementAt(0));
          displayb2.addReference(b2r_ref, (ConstantMap[])cMaps_b2.elementAt(1));

          displayb3.removeAllReferences();
          displayb3.clearMaps();
          displayb3.addMap((ScalarMap)sMaps_b3.elementAt(0));
          displayb3.addMap((ScalarMap)sMaps_b3.elementAt(1));
          displayb3.addReference(b3_ref, (ConstantMap[])cMaps_b3.elementAt(0));
          displayb3.addReference(b3r_ref, (ConstantMap[])cMaps_b3.elementAt(1));
        }
        catch (VisADException exc) {
          System.out.println(exc.getMessage());
        }
        catch (RemoteException exc) {
          System.out.println(exc.getMessage());
        }
        toggle = 0;
      }
    }
  }

  public void mapChanged(ScalarMapEvent evt)
         throws VisADException, RemoteException
  {
    ScalarMap smap = evt.getScalarMap();
  }

  public void controlChanged(ScalarMapControlEvent evt)
  {

  }

  public void displayChanged(DisplayEvent evt)
         throws VisADException, RemoteException
  {
    if (evt.getId() == DisplayEvent.MOUSE_PRESSED_CENTER)
    {
       double lon, lat;
       double[] scale_offset = new double[2];
       double[] data = new double[2];
       double[] display = new double[2];
       double[] cur = null;
       int tt;
       double del_lon = 3.0;  //- need to determine these more precisely
       double del_lat = 3.0;  //- from scale info below

       cur = displayll.getDisplayRenderer().getCursor();

       lonmap.getScale(scale_offset, data, display);
       lon = (cur[0] - scale_offset[1])/scale_offset[0];

       latmap.getScale(scale_offset, data, display);
       lat = (cur[1] - scale_offset[1])/scale_offset[0];

       for ( tt = 0; tt < ntimes; tt++ ) //- linear search OK for small # of pts.
       {
         if ( ((lls[1][tt] < lon+del_lon) && (lls[1][tt] > lon-del_lon)) &&
              ((lls[0][tt] < lat+del_lat) && (lls[0][tt] > lat-del_lat)) )
         {
           time_ref.setData(new Real(time, tt+1));
           break;
         }
       }
    }
  }

  public static Matrix makeCovarianceMatrix( double[][] data_vectors )
  {
    int dim = data_vectors[0].length;
    int n_vectors = data_vectors.length;
    double[] mean_vector = new double[dim];

    for ( int jj = 0; jj < dim; jj++ ) 
    {
      double sum = 0;
      for ( int kk = 0; kk < n_vectors; kk++ ) {
         sum += data_vectors[kk][jj];
      }
      mean_vector[jj] = sum/n_vectors;
    }

    double[][] cv = new double[dim][dim];
     
    for ( int jj = 0; jj < dim; jj++ ) {
      for ( int ii = jj; ii < dim; ii++ ) {
        double sum = 0;
        for ( int kk = 0; kk < n_vectors; kk++ ) {
          sum += (data_vectors[kk][jj] - mean_vector[jj])*
                 (data_vectors[kk][ii] - mean_vector[ii]);
        }
        cv[jj][ii] = sum/n_vectors;
        cv[ii][jj] = cv[jj][ii];
      }
    }
    
    Matrix cov_matrix = new Matrix(cv);

    return cov_matrix;
  }
}
