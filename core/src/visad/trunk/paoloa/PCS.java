
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
import visad.util.LabeledColorWidget;
import visad.data.netcdf.Plain;
import java.rmi.RemoteException;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class PCS {

  /** the width and height of the UI frame */
  public static int WIDTH = 800;
  public static int HEIGHT = 600;

  // number of times
  int ntimes;
  // number of bands
  int nbands;
  // number of principal components
  int npcs;
  // number of levels
  int nlevels;

  // VisAD Tuple data object created from file
  Tuple file_data;
  FieldImpl time_series;
  FlatField eigen_vectors;
  FlatField means;
  FlatField pressures;

  // flag to use Java2D
  boolean java2d = false;

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
    RealTupleType twv = (RealTupleType) tr2.getRange();
    temperature = (RealType) twv.getComponent(0);
    watervapor = (RealType) twv.getComponent(1);

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

    ConstantMap[] yellow = {new ConstantMap(0.0, Display.Blue)};
    ConstantMap[] cyan = {new ConstantMap(0.0, Display.Red)};
    ConstantMap[] red = {new ConstantMap(0.0, Display.Blue),
                         new ConstantMap(0.0, Display.Green)};
    ConstantMap[] green = {new ConstantMap(0.0, Display.Blue),
                           new ConstantMap(0.0, Display.Red)};
    ConstantMap[] point = {new ConstantMap(0.0, Display.Blue),
                           new ConstantMap(2.0, Display.PointSize)};
    ConstantMap[] select = {new ConstantMap(0.0, Display.Blue),
                            new ConstantMap(5.0, Display.PointSize)};

    DisplayImpl displayb1 =
      new DisplayImplJ3D("displayb1", new TwoDDisplayRendererJ3D());
    displayb1.addMap(new ScalarMap(band, Display.XAxis));
    displayb1.addMap(new ScalarMap(band1, Display.YAxis));
    GraphicsModeControl modeb1 = displayb1.getGraphicsModeControl();
    modeb1.setScaleEnable(true);
    displayb1.addReference(b1_ref, yellow);
    displayb1.addReference(b1r_ref, cyan);

    DisplayImpl displayb2 =
      new DisplayImplJ3D("displayb2", new TwoDDisplayRendererJ3D());
    displayb2.addMap(new ScalarMap(band, Display.XAxis));
    displayb2.addMap(new ScalarMap(band2, Display.YAxis));
    GraphicsModeControl modeb2 = displayb2.getGraphicsModeControl();
    modeb2.setScaleEnable(true);
    displayb2.addReference(b2_ref, yellow);
    displayb2.addReference(b2r_ref, cyan);

    DisplayImpl displayb3 =
      new DisplayImplJ3D("displayb3", new TwoDDisplayRendererJ3D());
    displayb3.addMap(new ScalarMap(band, Display.XAxis));
    displayb3.addMap(new ScalarMap(band3, Display.YAxis));
    GraphicsModeControl modeb3 = displayb3.getGraphicsModeControl();
    modeb3.setScaleEnable(true);
    displayb3.addReference(b3_ref, yellow);
    displayb3.addReference(b3r_ref, cyan);

    DisplayImpl displayn1 =
      new DisplayImplJ3D("displayn1", new TwoDDisplayRendererJ3D());
    displayn1.addMap(new ScalarMap(band, Display.XAxis));
    displayn1.addMap(new ScalarMap(noise_band1, Display.YAxis));
    GraphicsModeControl moden1 = displayn1.getGraphicsModeControl();
    moden1.setScaleEnable(true);
    displayn1.addReference(n1_ref, yellow);
    displayn1.addReference(n1r_ref, cyan);

    DisplayImpl displayn2 =
      new DisplayImplJ3D("displayn2", new TwoDDisplayRendererJ3D());
    displayn2.addMap(new ScalarMap(band, Display.XAxis));
    displayn2.addMap(new ScalarMap(noise_band2, Display.YAxis));
    GraphicsModeControl moden2 = displayn2.getGraphicsModeControl();
    moden2.setScaleEnable(true);
    displayn2.addReference(n2_ref, yellow);
    displayn2.addReference(n2r_ref, cyan);

    DisplayImpl displayn3 =
      new DisplayImplJ3D("displayn3", new TwoDDisplayRendererJ3D());
    displayn3.addMap(new ScalarMap(band, Display.XAxis));
    displayn3.addMap(new ScalarMap(noise_band3, Display.YAxis));
    GraphicsModeControl moden3 = displayn3.getGraphicsModeControl();
    moden3.setScaleEnable(true);
    displayn3.addReference(n3_ref, yellow);
    displayn3.addReference(n3r_ref, cyan);

    DisplayImpl displayll =
      new DisplayImplJ3D("displayll", new TwoDDisplayRendererJ3D());
    displayll.addMap(new ScalarMap(longitude, Display.XAxis));
    displayll.addMap(new ScalarMap(latitude, Display.YAxis));
    GraphicsModeControl modell = displayll.getGraphicsModeControl();
    modell.setScaleEnable(true);
    displayll.addReference(ll_ref, point);
    displayll.addReference(select_ll_ref, select);
    displayll.addReference(map_ref);

    DisplayImpl displayprof =
      new DisplayImplJ3D("displayprof", new TwoDDisplayRendererJ3D());
    displayprof.addMap(new ScalarMap(temperature, Display.XAxis));
    displayprof.addMap(new ScalarMap(watervapor, Display.XAxis));
    displayprof.addMap(new ScalarMap(pressure, Display.YAxis));
    GraphicsModeControl modeprof = displayprof.getGraphicsModeControl();
    modeprof.setScaleEnable(true);
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
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("number of eigen vectors", 1, npcs, 1, 1.0,
                                 num_eigen_ref,  numpcs));

    // create sliders JPanel
    JPanel top = new JPanel();
    top.setName("PCS Sliders");
    top.setFont(new Font("Dialog", Font.PLAIN, 12));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    top.setAlignmentY(JPanel.TOP_ALIGNMENT);
    top.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(top);
    top.add(displayb1.getComponent());
    top.add(displayb2.getComponent());
    top.add(displayb3.getComponent());
    top.add(displayll.getComponent());

    // create sliders JPanel
    JPanel bottom = new JPanel();
    bottom.setName("PCS Sliders");
    bottom.setFont(new Font("Dialog", Font.PLAIN, 12));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.setAlignmentY(JPanel.TOP_ALIGNMENT);
    bottom.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(bottom);
    bottom.add(displayn1.getComponent());
    bottom.add(displayn2.getComponent());
    bottom.add(displayn3.getComponent());
    bottom.add(displayprof.getComponent());

    // CellImpl to change displays when user moves sliders
    CellImpl slider_cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        int t = (int) ((Real) time_ref.getData()).getValue() - 1;
        int ne = (int) ((Real) num_eigen_ref.getData()).getValue() - 1;
        if (t < 0 || ntimes <= t || ne < 0 || npcs <= ne) {
          System.out.println("time " + t + " or number of eigen vectors " +
                             ne + " out of bounds");
          return;
        }
        Tuple tup = (Tuple) time_series.getSample(t);


      }
    };
    // link sliders to slider_cell
    slider_cell.addReference(time_ref);
    slider_cell.addReference(num_eigen_ref);

    // make the JFrame visible
    frame.setVisible(true);
  }
}

