
//
// PCS.java
//

package visad.paoloa;

// import needed classes
import visad.*;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.util.VisADSlider;
import visad.util.RangeSlider;
import visad.util.LabeledColorWidget;
import visad.data.netcdf.Plain;
import visad.data.mcidas.BaseMapAdapter;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class PCS {

  /** the width and height of the UI frame */
  public static int WIDTH = 1200;
  public static int HEIGHT = 800;

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
  RealType pressure;


  // Sets
  Linear1DSet time_set;
  Linear1DSet band_set;
  Linear2DSet eigen_set;
  Linear1DSet levels_set;

  // DataReferenceImpls for VisADSliders
  DataReferenceImpl time_ref;
  DataReferenceImpl num_eigen_ref;

  // DataReferenceImpls for displays
  // bands versus reconstructions
  DataReferenceImpl b1_ref;
  DataReferenceImpl b1r_ref;
  DataReferenceImpl b2_ref;
  DataReferenceImpl b2r_ref;
  DataReferenceImpl b3_ref;
  DataReferenceImpl b3r_ref;

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


  // type 'java visad.paoloa.PCS file.nc' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args.length < 1) {

      /* CTR: 29 September 1998 */
      System.out.println("To run this program, type " +
                         "\"java visad.paoloa.PCS file.nc\"");
      System.out.println("where file.nc is a netCDF file containing " +
                         "GIFTS spectra and eigenvector");

      return;
    }
    PCS pcs = new PCS(args[0]);
  }

  public PCS(String filename)
         throws VisADException, RemoteException, IOException {
    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing a NAST-I file
    file_data = (Tuple) plain.open(filename);
    plain = null;
    // System.out.println(file_data.getType());

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
    pressure = (RealType) pres_type.getRange();

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

    // System.out.println(ntimes + " " + nbands + " " + npcs + " " + nlevels);

    b1_ref = new DataReferenceImpl("b1_ref");
    b1r_ref = new DataReferenceImpl("b1r_ref");
    b2_ref = new DataReferenceImpl("b2_ref");
    b2r_ref = new DataReferenceImpl("b2r_ref");
    b3_ref = new DataReferenceImpl("b3_ref");
    b3r_ref = new DataReferenceImpl("b3r_ref");
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


    DisplayImpl displayb1 =
      new DisplayImplJ3D("displayb1", new TwoDDisplayRendererJ3D());
    final ScalarMap bmapb1 = new ScalarMap(band, Display.XAxis);
    displayb1.addMap(bmapb1);
    displayb1.addMap(new ScalarMap(band1, Display.YAxis));
    GraphicsModeControl modeb1 = displayb1.getGraphicsModeControl();
    modeb1.setScaleEnable(true);
    ConstantMap[] yellow = {new ConstantMap(0.0, Display.Blue)};
    ConstantMap[] cyan = {new ConstantMap(0.0, Display.Red)};
    displayb1.addReference(b1_ref, yellow);
    displayb1.addReference(b1r_ref, cyan);

    DisplayImpl displayb2 =
      new DisplayImplJ3D("displayb2", new TwoDDisplayRendererJ3D());
    final ScalarMap bmapb2 = new ScalarMap(band, Display.XAxis);
    displayb2.addMap(bmapb2);
    displayb2.addMap(new ScalarMap(band2, Display.YAxis));
    GraphicsModeControl modeb2 = displayb2.getGraphicsModeControl();
    modeb2.setScaleEnable(true);
    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    displayb2.addReference(b2_ref, yellow);
    displayb2.addReference(b2r_ref, cyan);

    DisplayImpl displayb3 =
      new DisplayImplJ3D("displayb3", new TwoDDisplayRendererJ3D());
    final ScalarMap bmapb3 = new ScalarMap(band, Display.XAxis);
    displayb3.addMap(bmapb3);
    displayb3.addMap(new ScalarMap(band3, Display.YAxis));
    GraphicsModeControl modeb3 = displayb3.getGraphicsModeControl();
    modeb3.setScaleEnable(true);
    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red)};
    displayb3.addReference(b3_ref, yellow);
    displayb3.addReference(b3r_ref, cyan);

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

    DisplayImpl displayll =
      new DisplayImplJ3D("displayll", new TwoDDisplayRendererJ3D());
      // new DisplayImplJ3D("displayll");
    ScalarMap lonmap = new ScalarMap(longitude, Display.XAxis);
    // ScalarMap lonmap = new ScalarMap(longitude, Display.Longitude);
    displayll.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(latitude, Display.YAxis);
    // ScalarMap latmap = new ScalarMap(latitude, Display.Latitude);
    displayll.addMap(latmap);
    GraphicsModeControl modell = displayll.getGraphicsModeControl();
    modell.setScaleEnable(true);
    modell.setPointSize(5);
    yellow = new ConstantMap[] {new ConstantMap(0.0, Display.Blue)};
    cyan = new ConstantMap[] {new ConstantMap(0.0, Display.Red),
                              new ConstantMap(0.5, Display.Green),
                              new ConstantMap(0.5, Display.Blue)};
    displayll.addReference(select_ll_ref, yellow);
    displayll.addReference(ll_ref, cyan);
    displayll.addReference(map_ref);

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
    sliders.add(new VisADSlider("time", 1, ntimes, 1, 1.0, time_ref,
                                 time));
    // sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("neigen", 0, npcs, 0, 1.0,
                                 num_eigen_ref,  numpcs));
    RangeSlider rs = new RangeSlider("band range", 0.0f, (float) nbands) {
      public void valuesUpdated() {
        float[] minmax = getMinMaxValues();
        float min = minmax[0];
        float max = minmax[1];
        try {
          bmapb1.setRange(min, max);
          bmapb2.setRange(min, max);
          bmapb3.setRange(min, max);
          bmapn1.setRange(min, max);
          bmapn2.setRange(min, max);
          bmapn3.setRange(min, max);
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
      System.out.println("mag eigen[" + j + "] = " + mag);
    }

    // compute pressureSet
    values = pressures.getFloats(false);
    Gridded1DSet pressureSet = new Gridded1DSet(pressure, values, nlevels);
    // compute ll_field and ll_select
    float latmin = Float.MAX_VALUE;
    float latmax = Float.MIN_VALUE;
    float lonmin = Float.MAX_VALUE;
    float lonmax = Float.MIN_VALUE;
    float del_lat = 1.0f;
    float del_lon = 1.0f;
    float[][] lls = new float[2][ntimes];
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
      public void doAction() throws VisADException, RemoteException {
        int t = (int) ((Real) time_ref.getData()).getValue() - 1;
        int ne = (int) ((Real) num_eigen_ref.getData()).getValue();
        if (t < 0 || ntimes <= t || ne < 0 || npcs < ne) {
          System.out.println("time " + t + " or neigens " +
                             ne + " out of bounds");
          return;
        }

        Tuple tup = (Tuple) time_series.getSample(t);
        FlatField bn = (FlatField) tup.getComponent(0);
        float[][] cvalues = bn.getFloats(false);
        float[] b = new float[nchannels];
        float[] n = new float[nchannels];
        float[] bm = new float[nchannels];
        for (int k=0; k<3; k++) {
          int kb = k * nbands;
          for (int i=0; i<nbands; i++) {
            b[kb + i] = cvalues[k][i];
            n[kb + i] = cvalues[3 + k][i];
            bm[kb + i] = mean_values[k][i];
          }
        }

        float[] bcoefs = new float[ne];
        float[] ncoefs = new float[ne];
        for (int j=0; j<ne; j++) {
          double bcoef = 0.0;
          double ncoef = 0.0;
          float[] pc = eigen_values[j];
          for (int i=0; i<nchannels; i++) {
            bcoef += pc[i] * (b[i] - bm[i]);
            ncoef += pc[i] * n[i];
          }
          bcoefs[j] = (float) bcoef;
          ncoefs[j] = (float) ncoef;
        }
        float[] rb = new float[nchannels];
        float[] rn = new float[nchannels];
        for (int i=0; i<nchannels; i++) {
          float bv = bm[i];
          float nv = 0.0f;
          for (int j=0; j<ne; j++) {
            bv += bcoefs[j] * eigen_values[j][i];
            nv += ncoefs[j] * eigen_values[j][i];
          }
          rb[i] = bv;
          rn[i] = nv;
        }
        float[][] rvalues = new float[6][nbands];
        for (int k=0; k<3; k++) {
          int kb = k * nbands;
          for (int i=0; i<nbands; i++) {
            rvalues[k][i] = rb[kb + i];
            rvalues[3 + k][i] = rn[kb + i];
          }
        }

        float[][] vals = {cvalues[0]};
        FlatField b1 = new FlatField(b1_func, band_set);
        b1.setSamples(vals, false);
        b1_ref.setData(b1);
        vals = new float[][] {rvalues[0]};
        FlatField b1r = new FlatField(b1_func, band_set);
        b1r.setSamples(vals, false);
        b1r_ref.setData(b1r);

        vals = new float[][] {cvalues[1]};
        FlatField b2 = new FlatField(b2_func, band_set);
        b2.setSamples(vals, false);
        b2_ref.setData(b2);
        vals = new float[][] {rvalues[1]};
        FlatField b2r = new FlatField(b2_func, band_set);
        b2r.setSamples(vals, false);
        b2r_ref.setData(b2r);

        vals = new float[][] {cvalues[2]};
        FlatField b3 = new FlatField(b3_func, band_set);
        b3.setSamples(vals, false);
        b3_ref.setData(b3);
        vals = new float[][] {rvalues[2]};
        FlatField b3r = new FlatField(b3_func, band_set);
        b3r.setSamples(vals, false);
        b3r_ref.setData(b3r);

        vals = new float[][] {cvalues[3]};
        FlatField n1 = new FlatField(n1_func, band_set);
        n1.setSamples(vals, false);
        n1_ref.setData(n1);
        vals = new float[][] {rvalues[3]};
        FlatField n1r = new FlatField(n1_func, band_set);
        n1r.setSamples(vals, false);
        n1r_ref.setData(n1r);

        vals = new float[][] {cvalues[4]};
        FlatField n2 = new FlatField(n2_func, band_set);
        n2.setSamples(vals, false);
        n2_ref.setData(n2);
        vals = new float[][] {rvalues[4]};
        FlatField n2r = new FlatField(n2_func, band_set);
        n2r.setSamples(vals, false);
        n2r_ref.setData(n2r);

        vals = new float[][] {cvalues[5]};
        FlatField n3 = new FlatField(n3_func, band_set);
        n3.setSamples(vals, false);
        n3_ref.setData(n3);
        vals = new float[][] {rvalues[5]};
        FlatField n3r = new FlatField(n3_func, band_set);
        n3r.setSamples(vals, false);
        n3r_ref.setData(n3r);

        select_ll_ref.setData(ll_select[t]);

        temp_ref.setData(tp[t]);
        wv_ref.setData(wvp[t]);
      }
    };
    // link sliders to slider_cell
    slider_cell.addReference(time_ref);
    slider_cell.addReference(num_eigen_ref);

    // make the JFrame visible
    frame.setVisible(true);
  }
}

