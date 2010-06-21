
//
// Galaxy.java
//

// Sun is at ix=0.0, Yy(iy)=8.5, iz=0.0

package visad.benjamin;

// the VisAD packages
import visad.*;
import visad.util.VisADSlider;
import visad.util.LabeledColorWidget;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java2d.DisplayImplJ2D;

// Java packages
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.net.MalformedURLException;
import java.io.*;

// JFC packages
import javax.swing.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   Galaxy implements the interactive Milky Way model
   application using VisAD 2.0.<P>
*/
public class Galaxy extends Object implements ActionListener {

  /** RemoteServerImpl for server
      this Galaxy is a server if server_server != null */
  RemoteServerImpl server_server;

  /** RemoteServer for client
      this Galaxy is a client if client_server != null */
  RemoteServer client_server;

  /** constants borrowed from Fortran include file dimen.h */
  static int NxpxMAX;
  static int NypxMAX;
  static int NRMAX;
  static int NZMAX;
  static int NXP;
  static int NYP;
  static int NZP;
  static int NX;
  static int NY;
  static int NZ;

  static double llast;
  static double elast;

  /** declare sets */
  Linear3DSet grid_set;
  Linear2DSet image_set;
  Linear2DSet lonlat_set;
  Linear1DSet distSol_set;

  Set distDomain;
  Set velDomain;
  Gridded3DSet line_to_sol;
  Gridded3DSet x_to_sol;
  Gridded3DSet y_to_sol;
  Gridded3DSet z_to_sol;
  SampledSet[] set_s = new SampledSet[3];
  UnionSet sol_sight;
  RealTuple sol;
  float sol_x = 0f;
  float sol_y = 8.5f;
  float sol_z = 0f;
  int npts = 2;
  float[][] samples = new float[3][npts];
  int n_profpts = 50;
  float[] xprof = new float[n_profpts];
  float[] yprof = new float[n_profpts];
  float[][] yprof_a = new float[1][n_profpts];
  float[][] xprof_a = new float[1][n_profpts];
  int i_type = 1;
  float last_x;
  float last_y;
  float last_z;
  float l;
  float b;
  float d;
  float[] lbd = new float[3];
  float[][] lonlat_b;
  float[][] image_b;
  int length_0;
  int length_1;



  /** declare MathTypes */
  RealType gridx;
  RealType gridy;
  RealType gridz;
  RealTupleType grid_domain;
  RealType density;
  RealType emission;
  RealType Intensity;
  RealType distance;
  RealType v_LSR;
  FunctionType grid_type;
  FunctionType dist_density;
  FunctionType vel_intensity;
  FlatField field_D;
  FlatField field_E;

  RealType line;
  RealType element;
  RealTupleType image_domain;
  RealType radiance;
  FunctionType image_type;

  RealType lon;
  RealType lat;
  RealTupleType lonlat_range;
  FunctionType lonlat_type;

  /** declare DataReferences */
  DataReference grid_ref;
  DataReference image_ref;
  DataReference lonlat_ref;
  DataReference sphrSkyMap_ref;
  DataReference ireg_set_ref;

  DataReference line_to_sol_ref;
  DataReference sol_ref;
  DataReference sol_sightRef;
  DataReference red_cursor_ref;
  DataReference dist_densityRef;
  DataReference vel_intensityRef;

  /** DataReferences for 13 interactive model paramters */
  DataReference n1_ref;
  DataReference h1_ref;
  DataReference A1_ref;
  DataReference n2_ref;
  DataReference h2_ref;
  DataReference A2_ref;
  DataReference na_ref;
  DataReference ha_ref;
  DataReference wa_ref;
  DataReference Aa_ref;

  final static double F1 = 0.36;
  final static double F2 = 40.0;
  final static double Fa = 6.0;

  /** DataReference for density contour slider */
  DataReference density_ref;

  /** DataReferences for two Cell trigger buttons */
  DataReference contour_button_ref;
  DataReference compute_button_ref;
  DataReference reset_button_ref;
  DataReference density_button_ref;
  DataReference flat_sphere_button_ref;

  /** Displays */
  DisplayImpl display1;
  DisplayImpl display2;
  DisplayImpl display3;
  DisplayImpl display4;
  DisplayImpl display5;  // Spherical Sky Map display

  JTextField[] coord_fields = new JTextField[3];

  JPanel center;
  JButton flat_sphere_button;
  JPanel panel2;
  JPanel panel5;

  JPanel widget_panel;

  /** color widgets for sky map images */
  LabeledColorWidget lw2;
  LabeledColorWidget lw5;

  ConstantMap[] cmaps;

  ConstantMap[] cmaps_sol;

  ConstantMap[] cmaps_line;

  ConstantMap[] yellow;

  /** ScalarMap for 'line' in display2 */
  ScalarMap linemap;

  /** type 'java -mx64m visad.benjamin.Galaxy' to run this application;
      the main thread just exits, since Display, Cell and JFC threads
      run the application */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct Galaxy application
    Galaxy galaxy = new Galaxy(args);

    if (galaxy.client_server != null) {
      galaxy.setupClient();
    }
    else if (galaxy.server_server != null) {
      // load native method library
      System.loadLibrary("Galaxy");
      galaxy.setupServer();
    }
    else {
      // stand-alone (neither client nor server)
      // load native method library
      System.loadLibrary("Galaxy");
      galaxy.setupServer();
    }
  }

  public Galaxy(String args[]) throws VisADException, RemoteException {

    if (args.length > 0) {
      // this is a client

      // try to connect to RemoteServer
      String domain = "//" + args[0] + "/Galaxy";
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
        Naming.rebind("///Galaxy", server_server);
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
  }

  /** set up as server */
  void setupServer() throws VisADException, RemoteException {

    int[] constants = new int[4];
    getcon_c(constants);
    NxpxMAX = constants[0];
    NypxMAX = constants[1];
    NRMAX = constants[2];
    NZMAX = constants[3];
    NXP = NRMAX-1;
    NYP = NXP;
    NZP = NZMAX-1;
    NX = 2*NXP+1;
    NY = 2*NYP+1;
    NZ = 2*NZP+1;

    gridx = RealType.getRealType("gridx");
    gridy = RealType.getRealType("gridy");
    gridz = RealType.getRealType("gridz");
    grid_domain = new RealTupleType(gridx, gridy, gridz);
    density = RealType.getRealType("density");
    // emission = RealType.getRealType("emission");
    Intensity = RealType.getRealType("Intensity");
    distance = RealType.getRealType("distance");
    v_LSR = RealType.getRealType("v_LSR");
    grid_type = new FunctionType(grid_domain, density);
    dist_density = new FunctionType(distance, density);
    vel_intensity = new FunctionType(v_LSR, Intensity);

    line = RealType.getRealType("line");
    element = RealType.getRealType("element");
    image_domain = new RealTupleType(element, line);
    radiance = RealType.getRealType("H-alpha");
    image_type = new FunctionType(image_domain, radiance);
    lon = RealType.getRealType("lon", CommonUnit.degree);
    lat = RealType.getRealType("lat", CommonUnit.degree);
    lonlat_range = new RealTupleType(lon, lat);
    lonlat_type = new FunctionType(image_domain, lonlat_range);

    //
    // construct Data objects and DataReferences to them
    //

    // construct density grid Data object and DataReference
    grid_ref = new DataReferenceImpl("grid_ref");
    final float[][] grid_a = new float[1][NX * NY * NZ];

    // construct sky image Data object and DataReference
    image_ref = new DataReferenceImpl("image_ref");
    final float[][] image_a = new float[1][NxpxMAX * NypxMAX];

    // construct sky lonlat Data object and DataReference
    lonlat_ref = new DataReferenceImpl("lonlat_ref");
    final float[][] lonlat_a = new float[2][NxpxMAX * NypxMAX];

    // construct spherical sky map Data object and DataReference
    sphrSkyMap_ref = new DataReferenceImpl("sphrSkyMap_ref");

    ireg_set_ref = new DataReferenceImpl("ireg_set_ref");

    //
    // construct DataReference objects linked to VisADSliders (the
    // JSlider constructors will construct Real data objects for
    // these, so there is no point in constructing Real data objects
    // here)
    //

    n1_ref = new DataReferenceImpl("n1_ref");
    h1_ref = new DataReferenceImpl("h1_ref");
    A1_ref = new DataReferenceImpl("A1_ref");
    n2_ref = new DataReferenceImpl("n2_ref");
    h2_ref = new DataReferenceImpl("h2_ref");
    A2_ref = new DataReferenceImpl("A2_ref");
    na_ref = new DataReferenceImpl("na_ref");
    ha_ref = new DataReferenceImpl("ha_ref");
    wa_ref = new DataReferenceImpl("wa_ref");
    Aa_ref = new DataReferenceImpl("Aa_ref");


    // set up DataReferences for compute button and contour button
    compute_button_ref = new DataReferenceImpl("compute_button");
    contour_button_ref = new DataReferenceImpl("contour_button");
    reset_button_ref = new DataReferenceImpl("reset_button");
    density_button_ref = new DataReferenceImpl("density_button");
    density_ref = new DataReferenceImpl("density");
    red_cursor_ref = new DataReferenceImpl("red_cursor_ref");
    sol_ref = new DataReferenceImpl("sol_ref");
    dist_densityRef = new DataReferenceImpl("dist_densityRef");
    vel_intensityRef = new DataReferenceImpl("vel_intensityRef");

    flat_sphere_button_ref = new DataReferenceImpl("flat_sphere_button");
    flat_sphere_button_ref.setData(new Real(0.0));

    final RealTuple init_red_cursor = new RealTuple( new Real[] {
                                               new Real( gridx, 0.0 ),
                                               new Real( gridy, 0.0 ),
                                               new Real( gridz, 20.0 ) } );

    sol_sightRef = new DataReferenceImpl( "sol_sight" );

    samples[0][0] = sol_x;
    samples[1][0] = sol_y;
    samples[2][0] = sol_z;
    samples[0][1] = (float) 20.;
    samples[1][1] = sol_y;
    samples[2][1] = sol_z;
    x_to_sol = new Gridded3DSet( grid_domain, samples, npts, null, null, null );
    samples[0][1] = sol_x;
    samples[1][1] = (float) 20.;
    samples[2][1] = sol_z;
    y_to_sol = new Gridded3DSet( grid_domain, samples, npts, null, null, null );
    samples[0][1] = sol_x;
    samples[1][1] = sol_y;
    samples[2][1] = (float) 80.;
    z_to_sol = new Gridded3DSet( grid_domain, samples, npts, null, null, null );
    set_s[0] = x_to_sol;
    set_s[1] = y_to_sol;
    set_s[2] = z_to_sol;
    sol_sight = new UnionSet( grid_domain, set_s );
    sol_sightRef.setData( sol_sight );

    RealTuple sol = new RealTuple( new Real[] {
                                   new Real( gridx, 0.0 ),
                                   new Real( gridy, 8.5 ),
                                   new Real( gridz, 0.0 ) } );
    sol_ref.setData( sol );
    line_to_sol_ref = new DataReferenceImpl("line_to_sol_ref");


    // set up Displays for server
    DisplayImpl[] displays = new DisplayImpl[5];
    VisADSlider[] sliders = new VisADSlider[1];
    setupDisplays(false, displays, sliders);

    // set up user interface
    setupUI(displays, sliders);


    // array to hold actual sizes of grid and image
    final int[] sizes = new int[5];
    // array to hold 13 interactive model paramters
    final float[] params = new float[13];

    // create a Cell to model milky way
    // (this is an anonymous inner class extending CellImpl)
    CellImpl cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        File file = new File("./switch.out");
        file.delete();
        params[0] = (float) ((Real) n1_ref.getData()).getValue();
        params[1] = (float) ((Real) h1_ref.getData()).getValue();
        params[2] = (float) ((Real) A1_ref.getData()).getValue();
        params[3] = (float) F1;
        params[4] = (float) ((Real) n2_ref.getData()).getValue();
        params[5] = (float) ((Real) h2_ref.getData()).getValue();
        params[6] = (float) ((Real) A2_ref.getData()).getValue();
        params[7] = (float) F2;
        params[8] = (float) ((Real) na_ref.getData()).getValue();
        params[9] = (float) ((Real) ha_ref.getData()).getValue();
        params[10] = (float) ((Real) wa_ref.getData()).getValue();
        params[11] = (float) ((Real) Aa_ref.getData()).getValue();
        params[12] = (float) Fa;
        ismgsc_c(params, sizes, grid_a[0], image_a[0], lonlat_a[0], lonlat_a[1]);

        // get sky map image size
        elast = sizes[0] - 1.0;
        llast = sizes[1] - 1.0;
        length_0 = sizes[0];
        length_1 = sizes[1];
        // create image Sets
        lonlat_set = new Linear2DSet(0.0, elast, sizes[0],
                                     0.0, llast, sizes[1]);
        image_set = lonlat_set;
        // scale image line ScalarMap
        linemap.setRange(-0.5 * llast, 1.5 * llast + 1.0);

        // create image
        FlatField image = new FlatField(image_type, image_set);
        image_b = new float[1][sizes[0] * sizes[1]];
        System.arraycopy(image_a[0], 0, image_b[0], 0, image_b[0].length);
        image.setSamples(image_b);
        image_ref.setData(image);

        // create lat/lon Field to be contoured
        FlatField lonlat = new FlatField(lonlat_type, lonlat_set);
        lonlat_b = new float[2][sizes[0] * sizes[1]];
        System.arraycopy(lonlat_a[0], 0, lonlat_b[0], 0, lonlat_b[0].length);
        System.arraycopy(lonlat_a[1], 0, lonlat_b[1], 0, lonlat_b[1].length);

        // create spherical sky map
        FlatField f_field = makeSphericalSkyMap();
        sphrSkyMap_ref.setData( f_field );

        // set missing lat/lons
        for (int i=0; i<sizes[0] * sizes[1]; i++) {
          if (lonlat_b[0][i] < -400.0f) lonlat_b[0][i] = Float.NaN;
          if (lonlat_b[1][i] < -400.0f) lonlat_b[1][i] = Float.NaN;
        }
        lonlat.setSamples(lonlat_b);
        lonlat_ref.setData(lonlat);

        // create galaxy density grid
        // grid_set = new Integer3DSet(sizes[2], sizes[3], sizes[4]);
        double halfx = 0.5 * (sizes[2] - 1.0);
        double halfy = 0.5 * (sizes[3] - 1.0);
        double halfz = 0.5 * (sizes[4] - 1.0);
        grid_set = new Linear3DSet(-halfx, halfx, sizes[2],
                                   -halfy, halfy, sizes[3],
                                   -halfz, halfz, sizes[4]);

        FlatField grid = new FlatField(grid_type, grid_set);
        float[][] grid_b = new float[1][sizes[2] * sizes[3] * sizes[4]];
        System.arraycopy(grid_a[0], 0, grid_b[0], 0, grid_b[0].length);
        grid.setSamples(grid_b);
        grid_ref.setData(grid);

        // trigger red_cursor_cell & computeDensityEmission_cell
        red_cursor_ref.setData( init_red_cursor );
      }
    };
    // link cell to compute_button to trigger doAction
    Real compute_button = new Real(0.0);
    compute_button_ref.setData(compute_button);
    cell.addReference(compute_button_ref);

    CellImpl reset_cell = new CellImpl() {
      boolean first = true;
      public void doAction() throws VisADException, RemoteException {
        if ( ! first ) {
          System.out.println("reset:doAction");
          n1_ref.setData( new Real(0.025) );
          h1_ref.setData( new Real(0.906) );
          A1_ref.setData( new Real(19.957) );
          n2_ref.setData( new Real(0.099) );
          h2_ref.setData( new Real(0.150) );
          A2_ref.setData( new Real(3.693) );
          na_ref.setData( new Real(0.074) );
          ha_ref.setData( new Real(0.297) );
          wa_ref.setData( new Real(0.298) );
          Aa_ref.setData( new Real(8.278) );
        }
        else {
          first = false;
        }
      }
    };
    reset_cell.addReference(reset_button_ref);

    CellImpl red_cursor_cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple red_cursor = (RealTuple) red_cursor_ref.getData();
        if (red_cursor == null) return;
        last_x = (float) ((Real)red_cursor.getComponent(0)).getValue();
        last_y = (float) ((Real)red_cursor.getComponent(1)).getValue();
        last_z = (float) ((Real)red_cursor.getComponent(2)).getValue();
        double step_x = ( ((Real)red_cursor.getComponent(0)).getValue() -
                          sol_x )/(npts-1.0);
        double step_y = ( ((Real)red_cursor.getComponent(1)).getValue() -
                          sol_y )/(npts-1.0);
        double step_z = ( ((Real)red_cursor.getComponent(2)).getValue() -
                          sol_z )/(npts-1.0);
        for ( int ii = 0; ii < npts; ii++ ) {
          samples[0][ii] = (float) (sol_x + ii*step_x);
          samples[1][ii] = (float) (sol_y + ii*step_y);
          samples[2][ii] = (float) (sol_z + ii*step_z);
        }
        line_to_sol = new Gridded3DSet( red_cursor.getType(), samples, npts,
                                        null, null, null );
        line_to_sol_ref.setData( line_to_sol );
        galtosol( last_x, last_y, last_z, lbd );

        coord_fields[0].setText(PlotText.shortString(lbd[0]));
        coord_fields[1].setText(PlotText.shortString(lbd[1]));
        coord_fields[2].setText(PlotText.shortString(lbd[2]));

        // trigger computeDensityEmission_cell
        density_button_ref.setData(new Real(0.0));
      }
    };
    // link red_cursor to red_cursor_cell
    red_cursor_ref.setData( init_red_cursor );
    red_cursor_cell.addReference(red_cursor_ref);

    CellImpl computeDensityEmission_cell = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        i_type = 1;
        profile_c(i_type, n_profpts, last_x, last_y, last_z, xprof, yprof);
        distDomain = new Linear1DSet( distance, (double)xprof[0],
                                      (double)xprof[n_profpts-1], n_profpts );
        field_D = new FlatField(dist_density, distDomain);
        yprof_a[0] = yprof;
        field_D.setSamples( yprof_a );
        dist_densityRef.setData( field_D );

    /*- i_type = 2 not implemented  */
        i_type = 2;
        profile_c(i_type, n_profpts, last_x, last_y, last_z, xprof, yprof);
/*
System.out.println("last_x = " + last_x + " " + last_y + " " + last_z);
System.out.println("xprof = " + xprof[0] + " " + xprof[n_profpts-1] +
                   " n_profpts = " + n_profpts);
System.out.println("yprof = " + yprof[0] + " " + yprof[1] + " " +
                   yprof[2] + " " + yprof[3] + " " + yprof[n_profpts-1] + "\n");

doll% java visad.benjamin.Galaxy
last_x = 0.0 0.0 20.0
xprof = NaN NaN n_profpts = 50
yprof = 0.0 0.0 0.0 0.0 0.0

last_x = 0.0 0.0 20.0
xprof = NaN NaN n_profpts = 50
yprof = 0.0 0.0 0.0 0.0 0.0

// touch red cursor

last_x = -0.07375812 -0.2533907 20.01677
xprof = -24.866854 15.198111 n_profpts = 50
yprof = 1.8238283E-5 2.7213662E-5 4.006669E-5 5.8206664E-5 8.878365E-4

*/
        velDomain = new Linear1DSet( v_LSR, (double)xprof[0],
                                     (double)xprof[n_profpts-1], n_profpts );
        field_E = new FlatField(vel_intensity, velDomain);
        yprof_a[0] = yprof;
        field_E.setSamples( yprof_a );
        vel_intensityRef.setData( field_E );
      }
    };
    computeDensityEmission_cell.addReference( density_button_ref );


    if (server_server != null) {
      // set RemoteDataReferenceImpls in RemoteServer
      RemoteDataReferenceImpl[] refs =
        new RemoteDataReferenceImpl[27];
      refs[0] =
        new RemoteDataReferenceImpl((DataReferenceImpl) grid_ref);
      refs[1] =
        new RemoteDataReferenceImpl((DataReferenceImpl) image_ref);
      refs[2] =
        new RemoteDataReferenceImpl((DataReferenceImpl) lonlat_ref);
      refs[3] =
        new RemoteDataReferenceImpl((DataReferenceImpl) n1_ref);
      refs[4] =
        new RemoteDataReferenceImpl((DataReferenceImpl) h1_ref);
      refs[5] =
        new RemoteDataReferenceImpl((DataReferenceImpl) A1_ref);
      refs[6] =
        new RemoteDataReferenceImpl((DataReferenceImpl) n2_ref);
      refs[7] =
        new RemoteDataReferenceImpl((DataReferenceImpl) h2_ref);
      refs[8] =
        new RemoteDataReferenceImpl((DataReferenceImpl) A2_ref);
      refs[9] = null;
      refs[10] =
        new RemoteDataReferenceImpl((DataReferenceImpl) na_ref);
      refs[11] =
        new RemoteDataReferenceImpl((DataReferenceImpl) ha_ref);
      refs[12] =
        new RemoteDataReferenceImpl((DataReferenceImpl) wa_ref);
      refs[13] =
        new RemoteDataReferenceImpl((DataReferenceImpl) Aa_ref);
      refs[14] = null;
      refs[15] =
        new RemoteDataReferenceImpl((DataReferenceImpl) compute_button_ref);
      refs[16] =
        new RemoteDataReferenceImpl((DataReferenceImpl) contour_button_ref);
      refs[17] =
        new RemoteDataReferenceImpl((DataReferenceImpl) density_ref);
      refs[18] =
        new RemoteDataReferenceImpl((DataReferenceImpl) reset_button_ref);
      refs[19] =
        new RemoteDataReferenceImpl((DataReferenceImpl) red_cursor_ref);
      refs[20] =
        new RemoteDataReferenceImpl((DataReferenceImpl) sol_ref);
      refs[21] =
        new RemoteDataReferenceImpl((DataReferenceImpl) line_to_sol_ref);
      refs[22] =
        new RemoteDataReferenceImpl((DataReferenceImpl) dist_densityRef);
      refs[23] =
        new RemoteDataReferenceImpl((DataReferenceImpl) vel_intensityRef);
      refs[24] =
        new RemoteDataReferenceImpl((DataReferenceImpl) sol_sightRef);
      refs[25] =
        new RemoteDataReferenceImpl((DataReferenceImpl) density_button_ref);
      refs[26] =
        new RemoteDataReferenceImpl((DataReferenceImpl) sphrSkyMap_ref);

      server_server.setDataReferences(refs);
    }
  } //- end: setupServer


  /** set up as client */
  void setupClient() throws VisADException, RemoteException {

    //
    // get RemoteDataReferences
    //

    RemoteDataReference[] refs = client_server.getDataReferences();
    if (refs == null) {
      System.out.println("Cannot connect to server");
      System.exit(0);
    }

    grid_ref = refs[0];
    image_ref = refs[1];
    lonlat_ref = refs[2];
    n1_ref = refs[3];
    h1_ref = refs[4];
    A1_ref = refs[5];
    n2_ref = refs[6];
    h2_ref = refs[7];
    A2_ref = refs[8];
    na_ref = refs[10];
    ha_ref = refs[11];
    wa_ref = refs[12];
    Aa_ref = refs[13];
    compute_button_ref = refs[15];
    contour_button_ref = refs[16];
    density_ref = refs[17];
    reset_button_ref = refs[18];
    red_cursor_ref = refs[19];
    sol_ref = refs[20];
    line_to_sol_ref = refs[21];
    dist_densityRef = refs[22];
    vel_intensityRef = refs[23];
    sol_sightRef = refs[24];
    density_button_ref = refs[25];
    sphrSkyMap_ref = refs[26];

    flat_sphere_button_ref = new DataReferenceImpl("flat_sphere_button");
    flat_sphere_button_ref.setData(new Real(0.0));

    // get grid RealTypes needed for Display ScalarMaps
    grid_type = (FunctionType) grid_ref.getType();
    grid_domain = (RealTupleType) grid_type.getDomain();
    density = (RealType) grid_type.getRange();
    gridx = (RealType) grid_domain.getComponent(0);
    gridy = (RealType) grid_domain.getComponent(1);
    gridz = (RealType) grid_domain.getComponent(2);

    distance = (RealType)
      (((FunctionType)dist_densityRef.getType()).getDomain()).getComponent(0);
    // emission = (RealType) ((FunctionType)vel_intensityRef.getType()).getRange();
    v_LSR = (RealType)
      ((FunctionType)vel_intensityRef.getType()).getDomain().getComponent(0);
    Intensity = (RealType) ((FunctionType)vel_intensityRef.getType()).getRange();

    // get grid size
    Field grid = (Field) grid_ref.getData();
    grid_set = (Linear3DSet) grid.getDomainSet();
    int[] lens = grid_set.getLengths();
    NX = lens[0];
    NY = lens[1];
    NZ = lens[2];

    // get image RealTypes needed for Display ScalarMaps
    image_type = (FunctionType) image_ref.getType();
    image_domain = (RealTupleType) image_type.getDomain();
    radiance = (RealType) image_type.getRange();
    element = (RealType) image_domain.getComponent(0);
    line = (RealType) image_domain.getComponent(1);
    lonlat_type = (FunctionType) lonlat_ref.getType();
    lonlat_range = (RealTupleType) lonlat_type.getRange();
    lon = (RealType) lonlat_range.getComponent(0);
    lat = (RealType) lonlat_range.getComponent(1);

    // get image size
    Field lonlat = (Field) lonlat_ref.getData();
    lonlat_set = (Linear2DSet) lonlat.getDomainSet();
    lens = lonlat_set.getLengths();
    elast = lens[0];
    llast = lens[1];

    // set up Displays for client
    DisplayImpl[] displays = new DisplayImpl[5];
    VisADSlider[] sliders = new VisADSlider[1];
    setupDisplays(true, displays, sliders);

    // set up user interface
    setupUI(displays, sliders);
  }


  /** set up Displays; client is true for client and false for server;
      return constructed Displays in displays array */
  void setupDisplays(boolean client, DisplayImpl[] displays,
                     VisADSlider[] sliders)
       throws VisADException, RemoteException {

    // construct Display for density grid
    display1 = new DisplayImplJ3D("display1");
    /* WLH 23 Sept 98
    display1.setAlwaysAutoScale(true);
    */

    // map grid_domain to the Display spatial coordinates;
    display1.addMap(new ScalarMap(gridx, Display.XAxis));
    display1.addMap(new ScalarMap(gridy, Display.YAxis));
    ScalarMap mapz = new ScalarMap(gridz, Display.ZAxis);
    mapz.setRange((double) (-NZ), (double) (2*NZ-1));
    display1.addMap(mapz);
    GraphicsModeControl mode1 = display1.getGraphicsModeControl();
    mode1.setScaleEnable(true);

    // display1.addMap(new ScalarMap(density, Display.RGB));

    // construct mapping for interactive iso-surface
    ScalarMap mapdcontour = new ScalarMap(density, Display.IsoContour);
    display1.addMap(mapdcontour);
    ContourControl controldcontour = (ContourControl) mapdcontour.getControl();
    controldcontour.setSurfaceValue(0.06f);
    controldcontour.enableContours(true);

    cmaps = new ConstantMap[4];
    cmaps[0] =  new ConstantMap(0.0, Display.Blue);
    cmaps[1] =  new ConstantMap(1.0, Display.Red);
    cmaps[2] =  new ConstantMap(0.0, Display.Green);
    cmaps[3] =  new ConstantMap(4.0, Display.PointSize);

    cmaps_sol = new ConstantMap[4];
    cmaps_sol[0] = new ConstantMap(0.0, Display.Blue);
    cmaps_sol[1] = new ConstantMap(1.0, Display.Red);
    cmaps_sol[2] = new ConstantMap(1.0, Display.Green);
    cmaps_sol[3] = new ConstantMap(6.0, Display.PointSize);

    cmaps_line = new ConstantMap[3];
    cmaps_line[0] =  new ConstantMap(0.0, Display.Blue);
    cmaps_line[1] =  new ConstantMap(1.0, Display.Green);
    cmaps_line[2] =  new ConstantMap(0.0, Display.Red);

    yellow = new ConstantMap[3];
    yellow[0] = new ConstantMap(0.0, Display.Blue);
    yellow[1] = new ConstantMap(1.0, Display.Green);
    yellow[2] = new ConstantMap(1.0, Display.Red);

    if (client) {
      RemoteDisplayImpl remote_display1 =
        new RemoteDisplayImpl(display1);
      remote_display1.addReference(grid_ref);
      remote_display1.addReference(sol_ref, cmaps_sol);
      remote_display1.addReference(line_to_sol_ref, cmaps_line);
      remote_display1.addReference(sol_sightRef, yellow );
      remote_display1.addReferences(new DirectManipulationRendererJ3D(),
                                             red_cursor_ref, cmaps);
    }
    else { // server
      display1.addReference(grid_ref);
      display1.addReference(sol_ref, cmaps_sol);
      display1.addReference(line_to_sol_ref, cmaps_line);
      display1.addReference( sol_sightRef, yellow );
      display1.addReferences(new DirectManipulationRendererJ3D(),
                             red_cursor_ref, cmaps);
    }

    // slider to change galaxy density iso-surface
    VisADSlider density_slider =
      new VisADSlider("density", 0, 400, 60, 0.001, density_ref,
                      RealType.Generic);

    // CTR: 14 Sep 1999: allow density slider to become narrower
    Dimension slide_dim = density_slider.getMinimumSize();
    slide_dim.width = 70;
    density_slider.setMinimumSize(slide_dim);

    // construct Display for sky map image
    display2 = new DisplayImplJ2D("display2");
    display2.setAlwaysAutoScale(true);
    GraphicsModeControl mode2 = display2.getGraphicsModeControl();
    mode2.setScaleEnable(false);
    DisplayRenderer displayRenderer = display2.getDisplayRenderer();
    displayRenderer.setBoxOn(false);

    // map grid_domain to the Display spatial coordinates;
    display2.addMap(new ScalarMap(element, Display.XAxis));
    linemap = new ScalarMap(line, Display.YAxis);
    display2.addMap(linemap);
    ScalarMap rgbmap2 = new ScalarMap(radiance, Display.RGB);
    display2.addMap(rgbmap2);
    ScalarMap lonmap = new ScalarMap(lon, Display.IsoContour);
    display2.addMap(lonmap);
    ScalarMap latmap = new ScalarMap(lat, Display.IsoContour);
    display2.addMap(latmap);

    // color widget for sky map
    lw2 = new LabeledColorWidget(rgbmap2);

/* WLH 26 Jan 99
   need DisplayImpl.clearMap(ScalarMap map)
   or link ColorControls
*/

    // set iso-levels every 30 degrees for lat/lon contour lines
    ContourControl loncontrol = (ContourControl) lonmap.getControl();
    loncontrol.enableContours(true);
    loncontrol.setContourInterval(30.0f, -180.0f, 180.0f, -180.0f);
    ContourControl latcontrol = (ContourControl) latmap.getControl();
    latcontrol.enableContours(true);
    latcontrol.setContourInterval(30.0f, -90.0f, 89.0f, -90.0f);

    if (client) {
      // set range for sky map image lines
      linemap.setRange(-0.5 * llast, 1.5 * llast + 1.0);

      RemoteDisplayImpl remote_display2 =
        new RemoteDisplayImpl(display2);
      remote_display2.addReference(image_ref);
      remote_display2.addReference(lonlat_ref);
    }
    else { // server
      display2.addReference(image_ref);
      display2.addReference(lonlat_ref);
    }

    // set up Cell to change galaxy density iso-level
    // Cell triggered by contour_button
    ContourCell cell_density =
      this. new ContourCell(controldcontour, density_ref);
    Real contour_button = new Real(0.0);
    contour_button_ref.setData(contour_button);
    if (client) {
      RemoteCellImpl remote_cell_density =
        new RemoteCellImpl(cell_density);
      remote_cell_density.addReference(contour_button_ref);
    }
    else {
      cell_density.addReference(contour_button_ref);
    }

    display3 = new DisplayImplJ2D("display3");
    display4 = new DisplayImplJ2D("display4");

    display3.setAlwaysAutoScale(true);
    display4.setAlwaysAutoScale(true);

    GraphicsModeControl mode3 = display3.getGraphicsModeControl();
    mode3.setScaleEnable(true);
    GraphicsModeControl mode4 = display4.getGraphicsModeControl();
    mode4.setScaleEnable(true);

    display3.addMap( new ScalarMap( distance, Display.XAxis ));
    display3.addMap( new ScalarMap( density, Display.YAxis ));
    display3.addMap( new ScalarMap( density, Display.Green ));
    display4.addMap( new ScalarMap( v_LSR, Display.XAxis ));
    display4.addMap( new ScalarMap( Intensity, Display.YAxis ));
    display4.addMap( new ScalarMap( Intensity, Display.Green ));

    if (client) {
      RemoteDisplayImpl remote_display3 = new RemoteDisplayImpl(display3);
      remote_display3.addReference( dist_densityRef );
      RemoteDisplayImpl remote_display4 = new RemoteDisplayImpl(display4);
      remote_display4.addReference( vel_intensityRef );
    }
    else {
      display3.addReference( dist_densityRef );
      display4.addReference( vel_intensityRef );
    }

    //- Spherical sky map  --*
    display5 = new DisplayImplJ3D("display5");

    display5.addMap( new ScalarMap(lon, Display.Longitude));
    display5.addMap( new ScalarMap(lat, Display.Latitude));
    ScalarMap rgbmap5 = new ScalarMap(radiance, Display.RGB);
    display5.addMap(rgbmap5);

    // color widget for sky map
    lw5 = new LabeledColorWidget(rgbmap5);

    if (client) {
      RemoteDisplayImpl remote_display5 =
        new RemoteDisplayImpl(display5);
        remote_display5.addReference( sphrSkyMap_ref );
    }
    else {
      display5.addReference( sphrSkyMap_ref );
    }

    // return density_slider and Displays for inclusion in GUI
    sliders[0] = density_slider;
    displays[0] = display1;
    displays[1] = display2;
    displays[2] = display3;
    displays[3] = display4;
    displays[4] = display5;
  }


  /** construct user interface using JFC */
  void setupUI(DisplayImpl[] displays, VisADSlider[] sliders)
       throws VisADException, RemoteException {

    // create JFrame for GUI
    JFrame frame = new JFrame("VisAD Collaborative Galaxy Designer");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // size of JFrame
    int WIDTH = 1200;
    int HEIGHT = 950;
    frame.setSize(WIDTH, HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - WIDTH/2,
                      screenSize.height/2 - HEIGHT/2);

    // create big_panel JPanel in frame
    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
    big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.setContentPane(big_panel);

    // create left hand side JPanel for sliders and text
    JPanel left = new JPanel(); // FlowLayout and double buffer
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setAlignmentY(JPanel.TOP_ALIGNMENT);
    left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(left);

    left.add(new JLabel("Interactive Milky Way galaxy design"));
    left.add(new JLabel("using VisAD  -  see:"));
    left.add(new JLabel("  "));
    left.add(new JLabel("  http://www.ssec.wisc.edu/~billh/visad.html"));
    left.add(new JLabel("  "));
    left.add(new JLabel("for more information about VisAD."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Bill Hibbard and Bob Benjamin"));
    left.add(new JLabel("University of Wisconsin - Madison"));
    left.add(new JLabel("  "));
    left.add(new JLabel("  "));
    left.add(new JLabel("Adjust Milky Way galaxy parameters using"));
    left.add(new JLabel("sliders."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Then press 'Compute' button to compute"));
    left.add(new JLabel("new galaxy."));
    left.add(new JLabel("  "));
    left.add(new JLabel("  "));


    // create slider_panel JPanel
    JPanel slider_panel = new JPanel();
    slider_panel.setName("Galaxy Sliders");
    slider_panel.setFont(new Font("Dialog", Font.PLAIN, 12));
    slider_panel.setLayout(new BoxLayout(slider_panel, BoxLayout.Y_AXIS));
    slider_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    slider_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    left.add(slider_panel);

    slider_panel.add(new VisADSlider("n1", 0, 500, 250, 0.0001, n1_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("h1", 0, 300, 91, 0.01, h1_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("A1", 50, 400, 200, 0.1, A1_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("n2", 0, 500, 100, 0.001, n2_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("h2", 0, 300, 150, 0.001, h2_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("A2", 10, 600, 370, 0.01, A2_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("na", 0, 200, 80, 0.001, na_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("ha", 0, 100, 30, 0.01, ha_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("wa", 0, 500, 300, 0.001, wa_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));
    slider_panel.add(new VisADSlider("Aa", 50, 200, 85, 0.1, Aa_ref,
                                      RealType.Generic));
    slider_panel.add(new JLabel("  "));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    buttonPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // "GO" button for applying computation in sliders
    JButton compute = new JButton("Compute");
    //compute.setAlignmentY(JPanel.TOP_ALIGNMENT);
    //compute.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    compute.addActionListener(this);
    compute.setActionCommand("compute");
    buttonPanel.add(compute);
    // slider_panel.add(compute);

    JButton reset = new JButton("Reset");
    //reset.setAlignmentY(JPanel.TOP_ALIGNMENT);
    //reset.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
    reset.addActionListener(this);
    reset.setActionCommand("reset");
    buttonPanel.add(reset);
    // slider_panel.add(reset);
    slider_panel.add(buttonPanel);

    slider_panel.add(new JLabel("  "));
    slider_panel.add(new JLabel("  "));

    // create widget_panel JPanel
    widget_panel = new JPanel();
    widget_panel.setName("Color Widget");
    widget_panel.setFont(new Font("Dialog", Font.PLAIN, 12));
    widget_panel.setLayout(new BoxLayout(widget_panel, BoxLayout.Y_AXIS));
    widget_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    widget_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    Dimension d = new Dimension(400,950);
    left.setMaximumSize(d);
    left.add(widget_panel);

    // color widget for sky map image
    d = new Dimension(300, 170);
    lw2.setMaximumSize(d);
    lw5.setMaximumSize(d);
    widget_panel.add(lw2);

    // create center JPanel for Displays
    center = new JPanel();
    center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
    center.setAlignmentY(JPanel.TOP_ALIGNMENT);
    center.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    d = new Dimension(400, 950);
    center.setMaximumSize(d);

    // CTR: 14 Sep 1999: center and right need equal preferred sizes
    center.setPreferredSize(new Dimension(400, 950));

    big_panel.add(center);

     // create panel for contour slider and button
    JPanel contour_panel = new JPanel();
    contour_panel.setLayout(new BoxLayout(contour_panel, BoxLayout.X_AXIS));
    contour_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    contour_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    contour_panel.add(sliders[0]);
    JButton contour = new JButton("Contour");
    contour.setAlignmentY(JPanel.TOP_ALIGNMENT);
    contour.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    contour.addActionListener(this);
    contour.setActionCommand("contour");
    contour_panel.add(contour);

    // get Display panels
    JPanel panel1 = (JPanel) displays[0].getComponent();
    panel2 = (JPanel) displays[1].getComponent();
    JPanel panel3 = (JPanel) displays[2].getComponent();
    JPanel panel4 = (JPanel) displays[3].getComponent();
    panel5 = (JPanel) displays[4].getComponent();

    // make borders for Displays and embed in display_panel JPanel
    Border etchedBorder5 =
      new CompoundBorder(new EtchedBorder(),
                         new EmptyBorder(5, 5, 5, 5));
    panel1.setBorder(etchedBorder5);
    panel2.setBorder(etchedBorder5);
    panel3.setBorder(etchedBorder5);
    panel4.setBorder(etchedBorder5);
    panel5.setBorder(etchedBorder5);

/*- panel for coords  -*/
    JPanel coord_panel = new JPanel();
    coord_panel.setLayout(new BoxLayout(coord_panel, BoxLayout.X_AXIS));
    coord_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    coord_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    coord_fields[0] = new JTextField("---");
    coord_fields[0].setBackground(Color.black);
    coord_fields[0].setForeground(Color.white);
    coord_fields[1] = new JTextField("---");
    coord_fields[1].setBackground(Color.black);
    coord_fields[1].setForeground(Color.white);
    coord_fields[2] = new JTextField("---");
    coord_fields[2].setBackground(Color.black);
    coord_fields[2].setForeground(Color.white);

    // WLH 2 Dec 98
    for (int i=0; i<3; i++) {
      Dimension msize = coord_fields[i].getMaximumSize();
      Dimension psize = coord_fields[i].getPreferredSize();
      msize.height = psize.height;
      coord_fields[i].setMaximumSize(msize);
    }

    JLabel coord_label = new JLabel("L:");
    coord_panel.add(coord_label);
    coord_panel.add(coord_fields[0]);
    coord_label = new JLabel("B:");
    coord_panel.add(coord_label);
    coord_panel.add(coord_fields[1]);
    coord_label = new JLabel("D:");
    coord_panel.add(coord_label);
    coord_panel.add(coord_fields[2]);


    // make labels for displays
    JLabel display1_label = new JLabel("3D isodensity surface of Galaxy");
    JLabel display1a_label =
      new JLabel("set density slider and press Contour button");
    JLabel display2_label = new JLabel("H-alpha emission sky map");
    JLabel display2a_label =
      new JLabel("as seen from Earth");

    flat_sphere_button = new JButton("flat > sphere");
    flat_sphere_button.addActionListener(this);
    flat_sphere_button.setActionCommand("flat/sphere");

    // add contour_panel, displays and display labels for center panel
    center.add(contour_panel);
    center.add(panel1);
    center.add(display1_label);
    center.add(display1a_label);
    center.add(coord_panel);
    center.add(panel2);
    center.add(flat_sphere_button);
    center.add(display2_label);
    center.add(display2a_label);

/*
    JFrame frame2 = new JFrame("Density/Emission Plots");
    WIDTH = 400;
    HEIGHT = 800;
    frame2.setSize( WIDTH, HEIGHT );
    frame2.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
*/

/* WLH - not yet */
    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
    right.setAlignmentY(JPanel.TOP_ALIGNMENT);
    right.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // CTR: 14 Sep 1999: center and right need equal preferred sizes
    right.setPreferredSize(new Dimension(400, 950));

/*
    frame2.getContentPane().add(right);
*/
    big_panel.add(right);

/*
    JPanel panelA = new JPanel();
    panelA.setLayout(new BoxLayout(panelA, BoxLayout.X_AXIS));
    panelA.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panelA.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    JButton buttonA_0 = new JButton("density/emision");
    buttonA_0.setAlignmentX(JPanel.TOP_ALIGNMENT);
    buttonA_0.setAlignmentY(JPanel.LEFT_ALIGNMENT);
    buttonA_0.addActionListener(this);
    buttonA_0.setActionCommand("density/emission");
    panelA.add(buttonA_0);

    right.add(panelA);
*/
    right.add(panel3);
    // temporarily use spherical sky map in place of emission graph
    right.add(panel4);

/*
    JFrame frame3 = new JFrame("Spherical Sky Map");
    WIDTH = 500;
    HEIGHT = 500;
    frame3.setSize( WIDTH, HEIGHT );
    frame3.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    frame3.getContentPane().add(panel5);
    frame3.setVisible(true);
*/

    // make the JFrame visible
    frame.setVisible(true);
/*
    frame2.setVisible(true);
*/
  }

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("contour")) {
      try {
        contour_button_ref.setData(new Real(0.0));
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
    if (cmd.equals("compute")) {
      try {
        compute_button_ref.setData(new Real(0.0));
        density_button_ref.setData(new Real(0.0));
      }
      catch (VisADException ex) {
        System.out.println( ex.getMessage() );
      }
      catch (RemoteException ex) {
      }
    }
    if (cmd.equals("reset")) {
      try {
        reset_button_ref.setData(new Real(0.0));
      }
      catch (VisADException ex) {
      }
      catch (RemoteException ex) {
      }
    }
    if (cmd.equals("density/emission")) {
      try {
        density_button_ref.setData(new Real(0.0));
      }
      catch ( VisADException ex ) {
      }
      catch ( RemoteException ex ) {
      }
    }
    if (cmd.equals("flat/sphere")) {
      try {
        double val = ((Real) flat_sphere_button_ref.getData()).getValue();
        if (val < 0.5) {
          val = 1.0;
          center.remove(panel2);
          center.add(panel5, 5);
          widget_panel.remove(lw2);
          widget_panel.add(lw5, 0);
          flat_sphere_button.setText("sphere > flat");
        }
        else {
          val = 0.0;
          center.remove(panel5);
          center.add(panel2, 5);
          widget_panel.remove(lw5);
          widget_panel.add(lw2, 0);
          flat_sphere_button.setText("flat > sphere");
        }
        flat_sphere_button_ref.setData(new Real(val));
      }
      catch ( VisADException ex ) {
      }
      catch ( RemoteException ex ) {
      }
    }
  }

  // Cell to recompute galaxy density iso-surface
  class ContourCell extends CellImpl {
    ContourControl control;
    DataReference ref;
    double value;

    ContourCell(ContourControl c, DataReference r)
           throws VisADException, RemoteException {
      control = c;
      ref = r;
      value = ((Real) ref.getData()).getValue();
    }

    public void doAction() throws VisADException, RemoteException {
      double val = ((Real) ref.getData()).getValue();
      if (val == val && val != value) {
        control.setSurfaceValue((float) ((Real) ref.getData()).getValue());
        control.enableContours(true);
        value = val;
      }
    }

  }

  private float[][] getLonLat( float[][] lonlat, int length_0, int length_1 )
  {
     float[][] n_lonlat = new float[2][ lonlat[0].length ];
     float gb;
     float gl, ll;
     float cosbet, beta;
     for ( int jj = 0; jj < length_1; jj++ ) {
       for ( int ii = 0; ii < length_0; ii++ ) {
          int ii_a = (int) (ii*(10*2.5f))/10;
          int jj_a = (int) (jj*(10*2.5f))/10;
          int kk = jj*length_0 + ii;

          if ( lonlat[1][kk] < -400 ) {
            float x = ((2*ii_a - 1f) -181f)/180f;
            float y = ((2*jj_a - 1f) -91f)/180f;
            float rad = (float)Math.sqrt( x*x + 4*y*y );
            float sinalp = (float)Math.sqrt((4*y*y -1f)*(4*y*y -1f)+
                                            (x*y*2)*(x*y*2) );
            if (Math.abs(sinalp) > 1 ) {
              gb = 0f;
            }
            else {
              float alpha = (float) (Math.asin(sinalp)*(57.2957795));
              gb = 90f - alpha;
            }
            if ( y < 0 ) gb = -gb;

            if ( sinalp <= 1.e-4 ) {
              gl = 180f;
              gl = (float) Math.IEEEremainder( (double)gl, 360d );
            }
            else {
              cosbet = (1f - x*x - 4f*y*y)/sinalp;
              if (cosbet > 1f ) {
                gl = 0f;
                gb = 0f;
              }
              else {
                beta = (float) (Math.acos(cosbet)*(57.2957795));
                ll = 2*beta;
                gl = ll;
                if ( x > 0f ) {
                  gl = 360 - gl;
                }
                if ( ii > 36f ) {
                  gl = gl - 360;
                }
              }
            }
            n_lonlat[0][kk] = gl;
            n_lonlat[1][kk] = gb;
          }
          else {
            n_lonlat[0][kk] = lonlat[0][kk];
            n_lonlat[1][kk] = lonlat[1][kk];
          }
       }
     }
     return n_lonlat;
  }

  private FlatField makeSphericalSkyMap()
  {
    float[][] lonlat_c = new float[2][ length_0 * length_1 ];
    float[] samples_c = new float[ length_0 * length_1 ];
    lonlat_c = getLonLat( lonlat_b, length_0, length_1 );

    float[][] lonlat_e = new float[2][length_0 * (length_1 - 1)];
    float[][] samples_e = new float[1][length_0 * (length_1 - 1)];
    for (  int ii = 1; ii < length_1; ii++ ) {
      for ( int jj = 0; jj < length_0; jj++ ) {
        int i = ii*length_0 + jj;
        int i2 = (ii-1)*length_0 + jj;
        lonlat_e[0][i2] = lonlat_c[0][i];
        lonlat_e[1][i2] = lonlat_c[1][i];
        if ((lonlat_b[0][i] < -400f)||(lonlat_b[1][i] < -400f)) {
          samples_e[0][i2] = Float.NaN;
        }
        else {
          samples_e[0][i2] = image_b[0][i];
        }
      }
    }
    boolean foundFirst;
    float[] firsts = new float[length_1-1];
    for (  int ii = 0; ii < (length_1-1); ii++ ) {
      foundFirst = false;
      firsts[ii] = Float.NaN;
      for ( int jj = 0; jj < length_0; jj++ ) {
        int i = ii*length_0 + jj;
        if (Float.isNaN(samples_e[0][i])) {
        }
        else if (!foundFirst ) {
          firsts[ii] = samples_e[0][i];
          foundFirst = true;
        }
      }
    }
    for (  int ii = 0; ii < (length_1-1); ii++ ) {
      for ( int jj = 0; jj < length_0; jj++ ) {
        int i = ii*length_0 + jj;
         if (Float.isNaN(samples_e[0][i])) {
           samples_e[0][i] = firsts[ii];
         }
       }
    }
    FlatField f_fieldRS = null;
    try {
      Gridded2DSet g_set = new Gridded2DSet(lonlat_range, lonlat_e, length_0, (length_1-1));
      FlatField f_field = new FlatField(new FunctionType(lonlat_range, radiance), g_set);
      f_field.setSamples( samples_e );
      f_fieldRS = (FlatField)f_field.resample( new LinearLatLonSet(lonlat_range,
                                               -180d, 180d, 60, -77d, 67d, 50),
                                                Data.WEIGHTED_AVERAGE,
                                                Data.NO_ERRORS  );
    }
    catch ( VisADException e1 ) {
       System.out.println(e1.getMessage());
    }
    catch ( RemoteException e2 ) {
       System.out.println(e2.getMessage());
    }
    return f_fieldRS;
  }

  /** native method declarations, to Fortran via C */

  private native void getcon_c(int[] constants);

  private native void ismgsc_c(float[] params, int[] sizes, float[] grid_a,
                               float[] image_a, float[] lons, float[] lats);

  /** Given coordinates (X,Y,Z) of pt in galaxy profile returns
      two 1D arrays, xprof, yprof with NPROF_PT values each.
      itype=1 gives xprof=distance from sun, yprof=density
      itype=2 will give xprof=velocity, yprof=emission measure */
  private native void profile_c( int itype, int n_pts, float x, float y,
                                 float z, float[] xprof, float[] yprof );

  /** Convert galactic X,Y,Z to sun_centered l,b,distance */
  private native void galtosol( float x, float y, float z, float[] lbd );

} //- end class: Galaxy

