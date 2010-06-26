
//
// GoesCollaboration.java
//


package visad.paoloa;

// VisAD packages
import visad.*;
import visad.util.Delay;
import visad.util.VisADSlider;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java3d.DirectManipulationRendererJ3D;


// Java packages
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.net.MalformedURLException;

// JFC packages
import javax.swing.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   GoesCollaboration implements the interactive and collaborative
   Goes satellite sounding retrieval application using VisAD 2.0.
   It is rewritten from the IRGS.v application developed for
   VisAD 1.1 by Paolo Antonelli.<P>
*/
public class GoesCollaboration extends Object {

  /** RemoteServerImpl for server
      this GoesCollaboration is a server if server_server != null */
  RemoteServerImpl server_server;

  /** RemoteServer for client
      this GoesCollaboration is a client if client_server != null */
  RemoteServer client_server;

  /** declare MathTypes */
  RealType nchan;
  RealType indx;
  RealType nl;
  RealType tbc;
  RealType tbc_d;
  RealType wfn;
  RealType pres;
  RealType temp;
  RealType mixr;
  RealType ozone;
  RealType pressure;
  RealType data_real;
  RealType diff;

  /** declare DataReferences */
  DataReference wfna_ref;
  DataReference tempa_ref;
  DataReference mixra_ref;
  DataReference ozonea_ref;
  DataReference presa_ref;
  DataReference diff_col_ref;
  DataReference diff_ref;
  DataReference zero_line_ref;
  DataReference smr_ref;
  DataReference real_tbc_ref;
  DataReference wfnb_ref;
  DataReference wfna_old_ref;

  /** slider DataReferences */
  DataReference gzen_ref;
  DataReference tskin_ref;
  DataReference in_dx_ref;

  /** the width and height of the UI frame */
  public static int WIDTH = 1200;
  public static int HEIGHT = 1000;

  /** type 'java visad.paoloa.GoesCollaboration' to run this application;
      the main thread just exits, since Display, Cell and JFC threads
      run the application */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct GoesCollaboration application
    GoesCollaboration goes = new GoesCollaboration(args);

    if (goes.client_server != null) {
      goes.setupClient();
    }
    else if (goes.server_server != null) {
      // load native method library (only needed for server)
      System.loadLibrary("GoesCollaboration");
      goes.setupServer();
    }
    else {
      // stand-alone (neither client nor server)
      // load native method library
      System.loadLibrary("GoesCollaboration");
      goes.setupServer();
    }
  }

  /**
     Construct the GoesCollaboration application, including Data
     objects, Display objects, Cell (computational) objects,
     and JFC (slider) user interface objects.  The Display,
     Cell and JFC objects include threads and links to Data
     objects (via DataReference objects).  Display and Cell
     threads wake up when linked Data objects change.  Display
     and JFC objects wake up on mouse events.  Display, Cell
     and JFC objects cause changes to Data objects.<P>

     Here's a summary of the event logic among Data, Displays,
     Cells, and JSliders:<P>

  <PRE>
  initialization ->
    zero_line = 0                              -> display4

  slider <--> in_dx

  slider <--> gzen

  slider <--> tskin

  slider <--> save_config

  in_dx -> real_tbcCell
    real_tbc = re_read_1_c(in_dx)
    month = 6
    lat = real_tbc[18];
    (tempa, mixra, ozonea, presa) =
      get_profil_c(lat, month)                 -> display2

  direct_manipualtion (in display2) ->
    (tempa, mixra, ozonea)                     -> display2

  gzen, tskin, tempa, mixra, ozonea, presa -> wfnbCell
    wfnb = goesrte_2_c(gzen, tskin, tempa, mixra, ozonea, presa)

  wfnb, real_tbc -> wfnaCell
    wfna = wfnb.wfn                            -> display1
    diff_DATA = wfnb.tbc[nl=1] - real_tbc      -> display4
    smr = RootMeanSquare(diff_DATA)            -> display4

  save_config -> wfna_oldCell
    wfna_old = wfna

  wfna, wfna_old -> diff_colCell
    diff_col = wfna - wfna_old                 -> display3
   </PRE>
  */
  public GoesCollaboration(String args[])
         throws VisADException, RemoteException {

    if (args.length > 0) {
      // this is a client

      // try to connect to RemoteServer
      String domain = "//" + args[0] + "/GoesCollaboration";
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

      /* CTR: 30 Sep 1998 */
      // check for the existence of necessary data files
      {
        File f1 = new File("data_obs_1.dat");
        File f2 = new File("goesrtcf");
        if (!f1.exists() || !f2.exists()) {
          System.out.println("This program requires the data files " +
                             "\"data_obs_1.dat\"");
          System.out.println("and \"goesrtcf\", available at:");
          System.out.println("   ftp://ftp.ssec.wisc.edu/pub/visad-2.0/" +
                             "paoloa-files.tar.Z");
          System.exit(1);
        }
        if (!f2.exists()) {
          System.out.println("");
          System.exit(2);
        }
      }

      // try to set up a RemoteServer
      server_server = new RemoteServerImpl();
      try {
        Naming.rebind("///GoesCollaboration", server_server);
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

    //
    // construct function domain sampling Sets
    //

    // construct 1-D Sets
    Set linear18 = new Linear1DSet(1.0, 18.0, 18);
    Set linear19 = new Linear1DSet(1.0, 19.0, 19);
    Set linear40 = new Linear1DSet(1.0, 40.0, 40);

    // construct 2-D Set
    Set linear40x18 = new Linear2DSet(1.0, 40.0, 40, 1.0, 18.0, 18);

    //
    // construct MathTypes for Data objects
    //

    // construct RealTypes used as Function domains
    // with null Units but non-null default Sets (for
    // function domain samplings)
    nchan = RealType.getRealType("nchan", null, linear18);
    indx = RealType.getRealType("indx", null, linear19);
    nl = RealType.getRealType("nl", null, linear40);

    // construct RealTypes used as Function ranges
    // or for simple Real values, with null Units
    // and null default Sets
    tbc = RealType.getRealType("tbc");
    tbc_d = RealType.getRealType("tbc_d");
    wfn = RealType.getRealType("wfn");
    pres = RealType.getRealType("pres");
    temp = RealType.getRealType("temp");
    mixr = RealType.getRealType("mixr");
    ozone = RealType.getRealType("ozone");
    pressure = RealType.getRealType("pressure");
    data_real = RealType.getRealType("data_real");
    diff = RealType.getRealType("diff");

    // construct RealTupleType used as a Function domain
    // with non-null default Set
    RealTupleType nl_nchan = new RealTupleType(nl, nchan, null, linear40x18);

    // construct FunctionTypes
    FunctionType obs_data = new FunctionType(indx, data_real);
    FunctionType wfn_big = new FunctionType(nl_nchan,
                                            new RealTupleType(wfn, tbc));
    FunctionType tbc_array_dif = new FunctionType(nchan, tbc_d);
    FunctionType wfn_array = new FunctionType(nl_nchan, wfn);
    FunctionType temp_array = new FunctionType(nl, temp);
    FunctionType mixr_array = new FunctionType(nl, mixr);
    FunctionType ozone_array = new FunctionType(nl, ozone);
    FunctionType pres_array = new FunctionType(nl, pressure);

    //
    // construct Data objects and DataReferences to them
    //

    // construct weighting function Data object and DataReference
    FlatField wfna = new FlatField(wfn_array);
    wfna_ref = new DataReferenceImpl("wfna");
    wfna_ref.setData(wfna);

    // construct temperature profile Data object and DataReference
    FlatField tempa = new FlatField(temp_array);
    tempa_ref = new DataReferenceImpl("tempa");
    tempa_ref.setData(tempa);

    // construct mixing ratio profile Data object and DataReference
    FlatField mixra = new FlatField(mixr_array);
    mixra_ref = new DataReferenceImpl("mixra");
    mixra_ref.setData(mixra);

    // construct ozone profile Data object and DataReference
    FlatField ozonea = new FlatField(ozone_array);
    ozonea_ref = new DataReferenceImpl("ozonea");
    ozonea_ref.setData(ozonea);

    // construct pressure profile Data object and DataReference
    FlatField presa = new FlatField(pres_array);
    presa_ref = new DataReferenceImpl("presa");
    presa_ref.setData(presa);

    // construct weighting function difference Data object
    // and DataReference
    FlatField diff_col = new FlatField(wfn_array);
    diff_col_ref = new DataReferenceImpl("diff_col");
    diff_col_ref.setData(diff_col);

    // construct brightness temperature error Data object
    // and DataReference
    FlatField diff_DATA = new FlatField(tbc_array_dif);
    diff_ref = new DataReferenceImpl("diff");
    diff_ref.setData(diff_DATA);

    // construct zero line Data object and DataReference
    FlatField zero_line = new FlatField(tbc_array_dif);
    zero_line_ref = new DataReferenceImpl("zero_line");
    zero_line_ref.setData(zero_line);

    // construct brightness temperature error root mean square
    // Data object and DataReference
    Real smr = new Real(tbc_d);
    smr_ref = new DataReferenceImpl("smr");
    smr_ref.setData(smr);

    // construct observed brightness temperature Data object
    // and DataReference
    FlatField real_tbc = new FlatField(obs_data);
    real_tbc_ref = new DataReferenceImpl("real_tbc");
    real_tbc_ref.setData(real_tbc);

    // construct compound weighting function Data object
    // and DataReference
    FlatField wfnb = new FlatField(wfn_big);
    wfnb_ref = new DataReferenceImpl("wfnb");
    wfnb_ref.setData(wfnb);

    // construct saved weighting function Data object
    // and DataReference
    FlatField wfna_old = new FlatField(wfn_array);
    wfna_old_ref = new DataReferenceImpl("wfna_old");
    wfna_old_ref.setData(wfna);


    //
    // construct DataReference objects linked to VisADSliders (the
    // JSlider constructors will construct Real data objects for
    // these, so there is no point in constructing Real data objects
    // here)
    //

    // DataReference for zenith angle
    gzen_ref = new DataReferenceImpl("gzen");

    // DataReference for skin temperature
    tskin_ref = new DataReferenceImpl("tskin");

    // DataReference for index into model atmospheres
    in_dx_ref = new DataReferenceImpl("in_dx");

    // DataReference used to trigger copying wfna to wfna_old
    DataReference save_config_ref = new DataReferenceImpl("save_config");


    // set up Displays for server
    DisplayImpl[] displays = new DisplayImpl[4];
    setupDisplays(displays);
    if (server_server != null) {
      for (int i = 0; i < displays.length; i++) {
        server_server.addDisplay(new RemoteDisplayImpl(displays[i]));
      }
    }

    // set up user interface
    setupUI(displays, in_dx_ref, save_config_ref, gzen_ref, tskin_ref);


    // initialize zero reference line for brightness temperature errors
    double[][] zero_line_x = zero_line.getValues();
    for (int i=0; i<zero_line_x[0].length; i++) zero_line_x[0][i] = 0.0;
    zero_line.setSamples(zero_line_x);


    // make sure Data are initialized
    new Delay(1000);
    gzen_ref.incTick();
    save_config_ref.incTick();
    new Delay(1000);


    //
    // construct computational Cells and links to DataReferences
    // that trigger them
    //

    // construct a real_tbcCell
    real_tbcCell real_tbc_cell = new real_tbcCell();
    real_tbc_cell.addReference(in_dx_ref);
    new Delay(500);

    // construct a wfnbCell
    wfnbCell wfnb_cell = new wfnbCell();
    wfnb_cell.addReference(gzen_ref);
    wfnb_cell.addReference(tskin_ref);
    wfnb_cell.addReference(tempa_ref);
    wfnb_cell.addReference(mixra_ref);
    wfnb_cell.addReference(ozonea_ref);
    wfnb_cell.addReference(presa_ref);
    new Delay(500);

    // construct a wfnaCell
    wfnaCell wfna_cell = new wfnaCell();
    wfna_cell.addReference(wfnb_ref);
    wfna_cell.addReference(real_tbc_ref);
    new Delay(500);

    // construct a wfna_oldCell
    wfna_oldCell wfna_old_cell = new wfna_oldCell();
    wfna_old_cell.addReference(save_config_ref);
    new Delay(500);

    // construct a diff_colCell
    diff_colCell diff_col_cell = new diff_colCell();
    diff_col_cell.addReference(wfna_ref);
    diff_col_cell.addReference(wfna_old_ref);
    new Delay(500);


    if (server_server != null) {
      // set RemoteDataReferenceImpls in RemoteServer
      RemoteDataReferenceImpl[] refs =
        new RemoteDataReferenceImpl[4];
      refs[0] =
        new RemoteDataReferenceImpl((DataReferenceImpl) gzen_ref);
      refs[1] =
        new RemoteDataReferenceImpl((DataReferenceImpl) tskin_ref);
      refs[2] =
        new RemoteDataReferenceImpl((DataReferenceImpl) in_dx_ref);
      refs[3] =
        new RemoteDataReferenceImpl((DataReferenceImpl) save_config_ref);

      server_server.setDataReferences(refs);
    }


    // make sure Data are initialized (again)
    new Delay(1000);
    gzen_ref.incTick();
    save_config_ref.incTick();

  }

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

    gzen_ref = refs[0];
    tskin_ref = refs[1];
    in_dx_ref = refs[2];
    DataReference save_config_ref = refs[3];

    // set up Displays for client
    DisplayImpl[] displays = new DisplayImpl[4];
    displays[0] = new DisplayImplJ3D(client_server.getDisplay("display1"));
    displays[1] = new DisplayImplJ3D(client_server.getDisplay("display2"));
    displays[2] = new DisplayImplJ3D(client_server.getDisplay("display3"));
    displays[3] = new DisplayImplJ3D(client_server.getDisplay("display4"));

    // set up user interface
    setupUI(displays, in_dx_ref, save_config_ref, gzen_ref, tskin_ref);

  }


  /** set up Displays; return constructed Displays in displays array */
  void setupDisplays(DisplayImpl[] displays)
       throws VisADException, RemoteException {

    //
    // construct Displays and link to Data objects
    //

    // construct Display 1 (using default DisplayRenderer);
    // the text name is used only for debugging
    DisplayImplJ3D display1 = new DisplayImplJ3D("display1");
    // construct ScalarMaps for Display 1;
    // explicitly set data range for nl values (in order to
    // invert scale)
    ScalarMap map1nl = new ScalarMap(nl, Display.YAxis);
    map1nl.setRange(40.0, 1.0);
    display1.addMap(map1nl);
    // setRange is not invoked for other ScalarMaps - they will
    // use auto-scaling from actual data values
    display1.addMap(new ScalarMap(nchan, Display.XAxis));
    display1.addMap(new ScalarMap(wfn, Display.Green));
    display1.addMap(new ScalarMap(wfn, Display.ZAxis));
    display1.addMap(new ConstantMap(0.5f, Display.Red));
    display1.addMap(new ConstantMap(0.5f, Display.Blue));

    GraphicsModeControl mode1 = display1.getGraphicsModeControl();
    mode1.setScaleEnable(true);

    // link weighting function Data object to display1
    // (using default DataRenderer and a null array of ConstantMaps)
    display1.addReference(wfna_ref);


    // construct Display 2 and its ScalarMaps (using non-default
    // 2-D DisplayRenderer)
    DisplayImplJ3D display2 =
      new DisplayImplJ3D("display2", new TwoDDisplayRendererJ3D());
    // explicitly set data range for nl values (in order to
    // invert scale)
    ScalarMap map2nl = new ScalarMap(nl, Display.YAxis);
    map2nl.setRange(40.0, 1.0);
    display2.addMap(map2nl);
    // map temp, mixr and ozone to XAxis and
    // set axis scale colors
    ScalarMap map2temp = new ScalarMap(temp, Display.XAxis);
    display2.addMap(map2temp);
    map2temp.setScaleColor(new float[] {1.0f, 0.0f, 0.0f});
    ScalarMap map2mixr = new ScalarMap(mixr, Display.XAxis);
    display2.addMap(map2mixr);
    map2mixr.setScaleColor(new float[] {0.0f, 1.0f, 0.0f});
    ScalarMap map2ozone = new ScalarMap(ozone, Display.XAxis);
    display2.addMap(map2ozone);
    map2ozone.setScaleColor(new float[] {0.0f, 0.0f, 1.0f});
    display2.addMap(new ScalarMap(pressure, Display.XAxis));

    GraphicsModeControl mode2 = display2.getGraphicsModeControl();
    mode2.setLineWidth(2.0f);
    mode2.setScaleEnable(true);

    // color temperature profile red
    ConstantMap[] tmaps = {new ConstantMap(1.0f, Display.Red),
                           new ConstantMap(0.0f, Display.Green),
                           new ConstantMap(0.0f, Display.Blue)};

    // color mixing ratio profile green
    ConstantMap[] mmaps = {new ConstantMap(0.0f, Display.Red),
                           new ConstantMap(1.0f, Display.Green),
                           new ConstantMap(0.0f, Display.Blue)};

    // color ozone profile blue
    ConstantMap[] omaps = {new ConstantMap(0.0f, Display.Red),
                           new ConstantMap(0.0f, Display.Green),
                           new ConstantMap(1.0f, Display.Blue)};

    // color pressure profile white
    ConstantMap[] pmaps = {new ConstantMap(1.0f, Display.Red),
                           new ConstantMap(1.0f, Display.Green),
                           new ConstantMap(1.0f, Display.Blue)};

    // enable direct manipulation for temperature, mixing ratio
    // and ozone profiles; do not enable direct manipulation for
    // pressure;
    // note that addReferences rather than addReference is
    // invoked for non-default DataRenderers (in this case,
    // DirectManipulationRendererJ3D);
    // note also that addReference and addReferences may take
    // an array of ConstantMaps that apply only to one Data
    // object
    display2.addReferences(new DirectManipulationRendererJ3D(),
			   tempa_ref, tmaps);
    display2.addReferences(new DirectManipulationRendererJ3D(),
			   mixra_ref, mmaps);
    display2.addReferences(new DirectManipulationRendererJ3D(),
			   ozonea_ref, omaps);
    display2.addReference(presa_ref, pmaps);


    // construct Display 3 and its ScalarMaps
    DisplayImplJ3D display3 = new DisplayImplJ3D("display3");
    // explicitly set data range for nl values (in order to
    // invert scale)
    ScalarMap map3nl = new ScalarMap(nl, Display.YAxis);
    map3nl.setRange(40.0, 1.0);
    display3.addMap(map3nl);
    display3.addMap(new ScalarMap(nchan, Display.XAxis));
    display3.addMap(new ScalarMap(wfn, Display.ZAxis));
    display3.addMap(new ScalarMap(wfn, Display.Green));
    display3.addMap(new ConstantMap(0.5f, Display.Red));
    display3.addMap(new ConstantMap(0.5f, Display.Blue));

    GraphicsModeControl mode3 = display3.getGraphicsModeControl();
    mode3.setScaleEnable(true);

    // link weighting function difference Data object to display3
    display3.addReference(diff_col_ref);


    // construct Display 4 and its ScalarMaps (using non-default
    // 2-D DisplayRenderer)
    DisplayImplJ3D display4 =
      new DisplayImplJ3D("display4", new TwoDDisplayRendererJ3D());
    display4.addMap(new ScalarMap(nchan, Display.XAxis));
    // explicitly set data range for tbc_d values
    ScalarMap map4tbc_d = new ScalarMap(tbc_d, Display.YAxis);
    map4tbc_d.setRange(-40.0, 40.0);
    display4.addMap(map4tbc_d);

    // set pointSize = 5 in display4 to make single Real value smr
    //   easily visible
    GraphicsModeControl mode4 = display4.getGraphicsModeControl();
    mode4.setPointSize(5.0f);
    mode4.setLineWidth(2.0f);
    mode4.setScaleEnable(true);

    // link brightness temperature error, zero line and brightness
    // temperature error root mean square Data objects to display4
    display4.addReference(diff_ref);
    display4.addReference(zero_line_ref);
    display4.addReference(smr_ref);

    // return DisplayImpls
    displays[0] = display1;
    displays[1] = display2;
    displays[2] = display3;
    displays[3] = display4;
  }


  /** construct user interface using JFC */
  void setupUI(DisplayImpl[] displays, DataReference in_dx_ref,
               DataReference save_config_ref, DataReference gzen_ref,
               DataReference tskin_ref)
       throws VisADException, RemoteException {

    //
    // construct JFC user interface with JSliders linked to
    // Data objects, and embed Displays into JFC JFrame
    //

    // create a JFrame
    JFrame frame = new JFrame("GoesCollaboration");
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
    big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
    big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(big_panel);

    // create left hand side JPanel for sliders and text
    JPanel left = new JPanel(); // FlowLayout and double buffer
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setAlignmentY(JPanel.TOP_ALIGNMENT);
    left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(left);

    // construct JLabels
    // (JTextArea does not align in BoxLayout well, so use JLabels)
    left.add(new JLabel("Interactive GOES satellite sounding " +
                        "retrieval"));
    left.add(new JLabel("using VisAD  -  see:"));
    left.add(new JLabel("  "));
    left.add(new JLabel("  http://www.ssec.wisc.edu/~billh/visad.html"));
    left.add(new JLabel("  "));
    left.add(new JLabel("for more information about VisAD."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Bill Hibbard, Paolo Antonelli and Bob Aune"));
    left.add(new JLabel("Space Science and Engineering Center"));
    left.add(new JLabel("University of Wisconsin - Madison"));
    left.add(new JLabel("  "));
    left.add(new JLabel("  "));
    left.add(new JLabel("Move index slider to retrieve a new model"));
    left.add(new JLabel("atmosphere."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Touch ref. conf. slider to save a new"));
    left.add(new JLabel("reference for weighting function " +
                        "difference."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Move zenith angle and skin T sliders to"));
    left.add(new JLabel("to modify atmosphere conditions."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Rotate scenes with left mouse button."));
    left.add(new JLabel("  "));
    left.add(new JLabel("Redraw temperature, water vapor and ozone " +
                        "with"));
    left.add(new JLabel("right mouse button to modify model " +
                        "atmosphere."));
    left.add(new JLabel("  "));
    left.add(new JLabel("  "));

    // create sliders JPanel
    JPanel sliders = new JPanel();
    sliders.setName("GoesCollaboration Sliders");
    sliders.setFont(new Font("Dialog", Font.PLAIN, 12));
    sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));
    sliders.setAlignmentY(JPanel.TOP_ALIGNMENT);
    sliders.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    left.add(sliders);

    // construct VisADSliders linked to Real Data objects and embedded
    // in sliders JPanel
    sliders.add(new VisADSlider("index", 1, 2234, 1, 1.0, in_dx_ref,
                                 RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("save as ref. conf.?", 0, 1000, 0, 1.0,
                                 save_config_ref,  RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("zenith angle (deg)", 0, 65, 35, 1.0,
                                 gzen_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("skin T (K)", 250, 340, 300, 1.0,
                                 tskin_ref, RealType.Generic));

    // construct JPanel and sub-panels for Displays
    JPanel display_panel = new JPanel();
    display_panel.setLayout(new BoxLayout(display_panel,
                                          BoxLayout.X_AXIS));
    display_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(display_panel);

    JPanel display_left = new JPanel();
    display_left.setLayout(new BoxLayout(display_left,
                                         BoxLayout.Y_AXIS));
    display_left.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    display_panel.add(display_left);

    JPanel display_right = new JPanel();
    display_right.setLayout(new BoxLayout(display_right,
                                          BoxLayout.Y_AXIS));
    display_right.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_right.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    display_panel.add(display_right);

    // get Display panels
    JPanel panel1 = (JPanel) displays[0].getComponent();
    JPanel panel2 = (JPanel) displays[1].getComponent();
    JPanel panel3 = (JPanel) displays[2].getComponent();
    JPanel panel4 = (JPanel) displays[3].getComponent();

    // make borders for Displays and embed in display_panel JPanel
    Border etchedBorder10 =
      new CompoundBorder(new EtchedBorder(),
                         new EmptyBorder(10, 10, 10, 10));
    panel1.setBorder(etchedBorder10);
    panel2.setBorder(etchedBorder10);
    panel3.setBorder(etchedBorder10);
    panel4.setBorder(etchedBorder10);

    // make labels for Displays
    JLabel display1_label = new JLabel("weighting function");
    JLabel display1a_label =
      new JLabel("vertical level (Y) vs channel (X)");
    JLabel display2_label = new JLabel("model atmosphere profile");
    JLabel display2a_label =
      new JLabel("temperature (red), ozone (blue),");
    JLabel display2b_label =
      new JLabel("water vapor (green), pressure (white)");
    JLabel display3_label = new JLabel("weighting function difference");
    JLabel display3a_label =
       new JLabel("vertical level (Y) vs channel (X)");
    JLabel display4_label = new JLabel("brightness temperature errors");
    JLabel display4a_label = new JLabel("with zero reference line and");
    JLabel display4b_label =
       new JLabel("root mean square error (single point)");

    // embed Displays and their labels in display_panel JPanel
    display_left.add(panel1);
    display_left.add(display1_label);
    display_left.add(display1a_label);
    display_left.add(panel2);
    display_left.add(display2_label);
    display_left.add(display2a_label);
    display_left.add(display2b_label);
    display_right.add(panel3);
    display_right.add(display3_label);
    display_right.add(display3a_label);
    display_right.add(panel4);
    display_right.add(display4_label);
    display_right.add(display4a_label);
    display_right.add(display4b_label);

    // make the JFrame visible
    frame.setVisible(true);
  }


  /** get observed brightness temperatures, as well as temperature,
      water-vapor mixing-ratio, ozone and pressure profiles */
  class real_tbcCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      // get index into model atmospheres
      int in_dx = (int) ((Real) in_dx_ref.getData()).getValue();
      if (in_dx < 1 || in_dx > 2234) return;

      // read observed brightness temperatures from data_obs_1.dat
      float[][] data_b = new float[1][19];
      re_read_1_c(in_dx, data_b[0]);
      ((FlatField) real_tbc_ref.getData()).setSamples(data_b);

      // obtain climatological temperature, water-vapor mixing-ratio,
      // and ozone mixing-ratio profiles by interpolating in month
      // and latitude amongst the FASCODE model atmospheres;
      // also get fixed pressure levels
      float lat = data_b[0][18];
      int month = 6;
      float[][] t_x = new float[1][40];
      float[][] m_x = new float[1][40];
      float[][] o_x = new float[1][40];
      float[][] p_x = new float[1][40];
      get_profil_c(lat, month, t_x[0], m_x[0], o_x[0], p_x[0]);

      ((FlatField) tempa_ref.getData()).setSamples(t_x);
      ((FlatField) mixra_ref.getData()).setSamples(m_x);
      ((FlatField) ozonea_ref.getData()).setSamples(o_x);
      ((FlatField) presa_ref.getData()).setSamples(p_x);
    }
  }

  /** compute weighting function of channel versus vertical level */
  class wfnbCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      // get zenith angle and skin temperature
      float gzen = (float) ((Real) gzen_ref.getData()).getValue();
      float tskin = (float) ((Real) tskin_ref.getData()).getValue();

      // compute weighting function of channel versus vertical level
      float[][] t_x = Set.doubleToFloat(((FlatField)
                          tempa_ref.getData()).getValues());
      float[][] m_x = Set.doubleToFloat(((FlatField)
                          mixra_ref.getData()).getValues());
      float[][] o_x = Set.doubleToFloat(((FlatField)
                          ozonea_ref.getData()).getValues());
      float[][] p_x = Set.doubleToFloat(((FlatField)
                          presa_ref.getData()).getValues());
      float[][] wfn = new float[2][40*18];
      goesrte_2_c(gzen, tskin, t_x[0], m_x[0], o_x[0], p_x[0],
                  wfn[0], wfn[1]);
      ((FlatField) wfnb_ref.getData()).setSamples(wfn);
    }
  }

  /** compute brightness temperature errors and root mean square */
  class wfnaCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      // compute brightness temperature errors
      float[][] t_x = new float[1][];
      float[][] wfn =
        Set.doubleToFloat(((FlatField) wfnb_ref.getData()).getValues());
      t_x[0] = wfn[0];
      ((FlatField) wfna_ref.getData()).setSamples(t_x);
      float[][] real_tbc_x = Set.doubleToFloat(((FlatField)
                             real_tbc_ref.getData()).getValues());
      float[][] diff_DATA_x = new float[1][18];
      float squ_mod = 0.0f;
      for (int c=0; c<18; c++) {
        diff_DATA_x[0][c] = wfn[1][0 + 40 * c] - real_tbc_x[0][c];
        squ_mod += diff_DATA_x[0][c] * diff_DATA_x[0][c] / 18.0f;
      }
      ((FlatField) diff_ref.getData()).setSamples(diff_DATA_x);

      // smr is root mean square of brightness temperature errors
      smr_ref.setData(new Real(tbc_d, Math.sqrt(squ_mod)));
    }
  }

  /** save a copy of wfna in wfna_old */
  class wfna_oldCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      // save a copy of wfna in wfna_old (i.e., wfna_old = wfna)
      wfna_old_ref.setData(
        (FlatField) ((FlatField) wfna_ref.getData()).clone());
    }
  }

  /** compute diff_col = wfna - wfna_old */
  class diff_colCell extends CellImpl {

    public void doAction() throws VisADException, RemoteException {
      // compute diff_col = wfna - wfna_old
      diff_col_ref.setData(
        wfna_ref.getData().subtract(wfna_old_ref.getData()));
    }
  }

  /** native method declarations, to Fortran via C */
  private native void re_read_1_c(int i, float[] data_b);

  private native void goesrte_2_c(float gzen, float tskin, float[] t,
                                  float[] w, float[] c, float[] p,
                                  float[] wfn, float[] tbcx);

  private native void get_profil_c(float rlat, int imon, float[] tpro,
                                   float[] wpro, float[] opro,
                                   float[] pref);

}

