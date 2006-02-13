
//
// ShallowFluid.java
//


package visad.aune;

// the VisAD package
import visad.*;
import visad.util.Delay;
import visad.util.VisADSlider;
import visad.java3d.*;

// Java packages
import java.rmi.*;
import java.net.MalformedURLException;

// JFC packages
import javax.swing.*;
import javax.swing.border.*;

// AWT packages
import java.awt.*;
import java.awt.event.*;

/**
   ShallowFluid implements the interactive 2-D shallow fluid
   model application using VisAD 2.0.  It is rewritten from the
   shallow.v application developed for VisAD 1.1 by Bob Aune.<P>
*/
public class ShallowFluid extends Object {

  /** RemoteServerImpl for server
      this ShallowFluid is a server if server_server != null */
  RemoteServerImpl server_server;

  /** RemoteServer for client
      this ShallowFluid is a client if client_server != null */
  RemoteServer client_server;

  /** declare MathTypes */
  RealType xloc;
  RealType yloc;
  RealType u;
  RealType v;
  RealType h;
  RealType cc;
  RealType udiff;
  RealType vdiff;
  RealType hdiff;
  RealType ccdiff;
  RealType time;

  RealTupleType loc;

  FunctionType vol;
  FunctionType voldiff;

  /** declare Sets */
  Set linear59x49;

  /** declare DataReferences */
  DataReference sim_time_ref;
  DataReference new_state_ref;
  DataReference old_state_ref;
  DataReference oldest_state_ref;
  DataReference newold_diff_ref;
  DataReference iopt_ref;
  DataReference ibc_ref;
  DataReference gravity_ref;
  DataReference alat_ref;
  DataReference ubar1_ref;
  DataReference vbar1_ref;
  DataReference hprm11_ref;
  DataReference hprm12_ref;
  DataReference delt_ref;
  DataReference eps_ref;
  DataReference adiff_ref;
  DataReference tfilt_ref;
  DataReference anim_delay_ref;

  /** the width and height of the UI frame */
  public static int WIDTH = 1200;
  public static int HEIGHT = 1000;

  /** flag model initialization */
  public boolean initial = true;

  /** type 'java visad.paoloa.ShallowFluid' to run this application;
      the main thread just exits, since Display, Cell and JFC threads
      run the application */
  public static void main(String args[])
         throws VisADException, RemoteException {
    // construct ShallowFluid application
    ShallowFluid shallow = new ShallowFluid(args);

    if (shallow.client_server != null) {
      shallow.setupClient();
    }
    else if (shallow.server_server != null) {
      // load native method library
      System.loadLibrary("ShallowFluid");
      shallow.setupServer();
    }
    else {
      // stand-alone (neither client nor server)
      // load native method library
      System.loadLibrary("ShallowFluid");
      shallow.setupServer();
    }

  }

  public ShallowFluid(String args[])
         throws VisADException, RemoteException {
    if (args.length > 0) {
      // this is a client

      // try to connect to RemoteServer
      String domain = "//" + args[0] + "/ShallowFluid";
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
        Naming.rebind("///ShallowFluid", server_server);
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


  /** set up as server:
     Construct the ShallowFluid application, including Data
     objects, Display objects, Cell (computational) objects,
     and JFC (slider) user interface objects.  The Display,
     Cell and JFC objects include threads and links to Data
     objects (via DataReference objects).  Display and Cell
     threads wake up when linked Data objects change.  Display
     and JFC objects wake up on mouse events.  Display, Cell
     and JFC objects cause changes to Data objects.<P>
  */
  void setupServer() throws VisADException, RemoteException {

    //
    // construct 2-D function domain sampling Set
    //

    RealTupleType generic2d =
      new RealTupleType(RealType.Generic, RealType.Generic);
    linear59x49 = new Linear2DSet(generic2d, 1.0, 59.0, 59,
                                             1.0, 49.0, 49);

    //
    // construct MathTypes for Data objects
    //

    // construct RealTypes used as Function ranges
    // or for simple Real values, with null Units
    // and null default Sets
    xloc = RealType.getRealType("xloc");
    yloc = RealType.getRealType("yloc");
    u = RealType.getRealType("u");
    v = RealType.getRealType("v");
    h = RealType.getRealType("h");
    cc = RealType.getRealType("cc");
    udiff = RealType.getRealType("udiff");
    vdiff = RealType.getRealType("vdiff");
    hdiff = RealType.getRealType("hdiff");
    ccdiff = RealType.getRealType("ccdiff");
    time = RealType.getRealType("time");

    // construct RealTupleType used as a Function domain
    // with non-null default Set
    loc = new RealTupleType(xloc, yloc, null, linear59x49);

    // construct FunctionTypes
    vol = new FunctionType(loc, new RealTupleType(u, v, h, cc));
    voldiff =
      new FunctionType(loc, new RealTupleType(udiff, vdiff, hdiff, ccdiff));


    //
    // construct Data objects and DataReferences to them
    //

    // construct simulated time Data object and DataReference
    Real sim_time = new Real(time, 0.0);
    sim_time_ref = new DataReferenceImpl("sim_time");
    sim_time_ref.setData(sim_time);

    // construct new state Data object and DataReference
    FlatField new_state = new FlatField(vol);
    new_state_ref = new DataReferenceImpl("new_state");
    new_state_ref.setData(new_state);

    // construct old state Data object and DataReference
    FlatField old_state = new FlatField(vol);
    old_state_ref = new DataReferenceImpl("old_state");
    old_state_ref.setData(old_state);

    // construct oldest state Data object and DataReference
    FlatField oldest_state = new FlatField(vol);
    oldest_state_ref = new DataReferenceImpl("oldest_state");
    oldest_state_ref.setData(oldest_state);

    // construct new-old difference Data object and DataReference
    FlatField newold_diff = new FlatField(voldiff);
    newold_diff_ref = new DataReferenceImpl("newold_diff");
    newold_diff_ref.setData(newold_diff);


    //
    // construct DataReference objects linked to JSliders (the slider
    //   listeners will construct Real data objects for these)
    //

    // DataReference for initial configuration
    iopt_ref = new DataReferenceImpl("iopt");

    // DataReference for boundary condition type
    ibc_ref = new DataReferenceImpl("ibc");

    // DataReference for gravity constant
    gravity_ref = new DataReferenceImpl("gravity");

    // DataReference for latitude of south boundary
    alat_ref = new DataReferenceImpl("alat");

    // DataReference for mean u
    ubar1_ref = new DataReferenceImpl("ubar1");

    // DataReference for mean v
    vbar1_ref = new DataReferenceImpl("vbar1");

    // DataReference for NYSE closing price
    hprm11_ref = new DataReferenceImpl("hprm11");

    // DataReference for total home runs in last World Series
    hprm12_ref = new DataReferenceImpl("hprm12");

    // DataReference for simulated time step
    delt_ref = new DataReferenceImpl("delt");

    // DataReference for spatial filter constant
    eps_ref = new DataReferenceImpl("eps");

    // DataReference for interior diffusion coefficient
    adiff_ref = new DataReferenceImpl("adiff");

    // DataReference for temporal filter
    tfilt_ref = new DataReferenceImpl("tfilt");

    // DataReference for animation delay
    anim_delay_ref = new DataReferenceImpl("anim_delay");

    // set up Displays for server
    DisplayImpl[] displays = new DisplayImpl[2];
    setupDisplays(false, displays);

    // set up user interface
    setupUI(displays);


    //
    // construct computational Cells and links to DataReferences
    // that trigger them
    //

    // flag for initial time step
    initial = true;

    // construct a shalstepCell
    shalstepCell shalstep_cell = new shalstepCell("shalstep_cell");
    shalstep_cell.addReference(new_state_ref);

    // construct an initializeCell
    initializeCell initialize_cell = new initializeCell();
    initialize_cell.addReference(iopt_ref);

    if (server_server != null) {
      // set RemoteDataReferenceImpls in RemoteServer
      RemoteDataReferenceImpl[] refs =
        new RemoteDataReferenceImpl[14];
      refs[0] =
        new RemoteDataReferenceImpl((DataReferenceImpl) new_state_ref);
      refs[1] =
        new RemoteDataReferenceImpl((DataReferenceImpl) iopt_ref);
      refs[2] =
        new RemoteDataReferenceImpl((DataReferenceImpl) ibc_ref);
      refs[3] =
        new RemoteDataReferenceImpl((DataReferenceImpl) gravity_ref);
      refs[4] =
        new RemoteDataReferenceImpl((DataReferenceImpl) alat_ref);
      refs[5] =
        new RemoteDataReferenceImpl((DataReferenceImpl) ubar1_ref);
      refs[6] =
        new RemoteDataReferenceImpl((DataReferenceImpl) vbar1_ref);
      refs[7] =
        new RemoteDataReferenceImpl((DataReferenceImpl) hprm11_ref);
      refs[8] =
        new RemoteDataReferenceImpl((DataReferenceImpl) hprm12_ref);
      refs[9] =
        new RemoteDataReferenceImpl((DataReferenceImpl) delt_ref);
      refs[10] =
        new RemoteDataReferenceImpl((DataReferenceImpl) eps_ref);
      refs[11] =
        new RemoteDataReferenceImpl((DataReferenceImpl) adiff_ref);
      refs[12] =
        new RemoteDataReferenceImpl((DataReferenceImpl) tfilt_ref);
      refs[13] =
        new RemoteDataReferenceImpl((DataReferenceImpl) anim_delay_ref);

      server_server.setDataReferences(refs);
    }
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

    new_state_ref = refs[0];
    iopt_ref = refs[1];
    ibc_ref = refs[2];
    gravity_ref = refs[3];
    alat_ref = refs[4];
    ubar1_ref = refs[5];
    vbar1_ref = refs[6];
    hprm11_ref = refs[7];
    hprm12_ref = refs[8];
    delt_ref = refs[9];
    eps_ref = refs[10];
    adiff_ref = refs[11];
    tfilt_ref = refs[12];
    anim_delay_ref = refs[13];

    // get RealTypes needed for Display ScalarMaps
    vol = (FunctionType) new_state_ref.getType();
    loc = (RealTupleType) vol.getDomain();
    xloc = (RealType) loc.getComponent(0);
    yloc = (RealType) loc.getComponent(1);
    RealTupleType range = (RealTupleType) vol.getRange();
    u = (RealType) range.getComponent(0);
    v = (RealType) range.getComponent(1);
    h = (RealType) range.getComponent(2);
    cc = (RealType) range.getComponent(3);

    // set up Displays for server
    DisplayImpl[] displays = new DisplayImpl[2];
    setupDisplays(true, displays);

    // set up user interface
    setupUI(displays);
  }


  /** set up Displays; client is true for client and false for server;
      return constructed Displays in displays array */
  void setupDisplays(boolean client, DisplayImpl[] displays)
       throws VisADException, RemoteException {

    //
    // construct Displays and link to Data objects
    //

    // construct Display 1 and it ScalarMaps
    DisplayImplJ3D display1 = new DisplayImplJ3D("display1");
    display1.addMap(new ScalarMap(xloc, Display.XAxis));
    display1.addMap(new ScalarMap(yloc, Display.YAxis));
    // display1.addMap(new ScalarMap(h, Display.ZAxis));
    // explicitly set data range for h values
    ScalarMap map1h = new ScalarMap(h, Display.ZAxis);
    map1h.setRange(5450.0, 5700.0);
    display1.addMap(map1h);
    // display1.addMap(new ScalarMap(cc, Display.Green));
    // explicitly set data range for cc values
    ScalarMap map1cc = new ScalarMap(cc, Display.Green);
    map1cc.setRange(-40.0, 40.0);
    display1.addMap(map1cc);

    ScalarMap map1u = new ScalarMap(u, Display.Flow1X);
    display1.addMap(map1u);
    display1.addMap(new ScalarMap(v, Display.Flow1Y));
    ((FlowControl) map1u.getControl()).setFlowScale(0.15f);

    display1.addMap(new ConstantMap(0.5f, Display.Red));
    display1.addMap(new ConstantMap(0.5f, Display.Blue));

    // link new state Data object to display1
    if (client) {
      RemoteDisplayImpl remote_display1 =
        new RemoteDisplayImpl(display1);
      remote_display1.addReference(new_state_ref);
    }
    else { // server
      display1.addReference(new_state_ref);
    }

    // construct Display 2 and it ScalarMaps
    DisplayImplJ3D display2 = new DisplayImplJ3D("display2");
    display2.addMap(new ScalarMap(xloc, Display.XAxis));
    display2.addMap(new ScalarMap(yloc, Display.YAxis));
    // display2.addMap(new ScalarMap(h, Display.ZAxis));
    // explicitly set data range for h values
    ScalarMap map2h = new ScalarMap(h, Display.ZAxis);
    map2h.setRange(5450.0, 5700.0);
    display2.addMap(map2h);
    // display2.addMap(new ScalarMap(cc, Display.Green));
    // explicitly set data range for cc values
    ScalarMap map2cc = new ScalarMap(cc, Display.Green);
    map2cc.setRange(-40.0, 40.0);
    display2.addMap(map2cc);

    GraphicsModeControl mode = display2.getGraphicsModeControl();
    mode.setTextureEnable(false);

    display2.addMap(new ConstantMap(0.5f, Display.Red));
    display2.addMap(new ConstantMap(0.5f, Display.Blue));

    // link new state Data object to display2
    if (client) {
      RemoteDisplayImpl remote_display2 =
        new RemoteDisplayImpl(display2);
      remote_display2.addReference(new_state_ref);
    }
    else { // server
      display2.addReference(new_state_ref);
    }

    displays[0] = display1;
    displays[1] = display2;
  }


  /** construct user interface using JFC */
  void setupUI(DisplayImpl[] displays)
       throws VisADException, RemoteException {

    //
    // construct JFC user interface with JSliders linked to
    // Data objects, and embed Displays into JFC JFrame
    //

    // create a JFrame
    JFrame frame = new JFrame("ShallowFluid");
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

    // create top JPanel for sliders and text
    JPanel top = new JPanel(); // FlowLayout and double buffer
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    top.setAlignmentY(JPanel.TOP_ALIGNMENT);
    top.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(top);

    // create text (left) JPanel
    JPanel text = new JPanel(); // FlowLayout and double buffer
    text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
    text.setAlignmentY(JPanel.TOP_ALIGNMENT);
    text.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    top.add(text);

    // construct JLabels
    // (JTextArea does not align in BoxLayout well, so use JLabels)
    text.add(new JLabel("Interactive 2-D shallow fluid model"));
    text.add(new JLabel("using VisAD  -  see:"));
    text.add(new JLabel("  "));
    text.add(new JLabel("  http://www.ssec.wisc.edu/~billh/visad.html"));
    text.add(new JLabel("  "));
    text.add(new JLabel("for more information about VisAD."));
    text.add(new JLabel("  "));
    text.add(new JLabel("Bill Hibbard and Bob Aune"));
    text.add(new JLabel("Space Science and Engineering Center"));
    text.add(new JLabel("University of Wisconsin - Madison"));
    text.add(new JLabel("  "));
    text.add(new JLabel("  "));
    text.add(new JLabel("Move initial configuration slider to restart model."));
    text.add(new JLabel("  "));
    text.add(new JLabel("Move animation delay slider to adjust model speed."));
    text.add(new JLabel("  "));
    text.add(new JLabel("Move other sliders to adjust model parameters."));
    text.add(new JLabel("  "));
    text.add(new JLabel("Rotate scenes with text mouse button."));
    text.add(new JLabel("  "));
    text.add(new JLabel("  "));

    // create slider (right) JPanel
    JPanel sliders = new JPanel();
    sliders.setName("ShallowFluid Sliders");
    sliders.setFont(new Font("Dialog", Font.PLAIN, 12));
    sliders.setLayout(new BoxLayout(sliders, BoxLayout.Y_AXIS));
    sliders.setAlignmentY(JPanel.TOP_ALIGNMENT);
    sliders.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    top.add(sliders);

    // construct JSliders linked to Data objects and embedded in
    // sliders JPanel
    sliders.add(new VisADSlider("initial configuration", 1, 5, 5, 1.0,
                                iopt_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("boundary condition", 1, 3, 3, 1.0,
                                ibc_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("gravity constant", 0, 9806, 9806, 0.001,
                                gravity_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("latitude of south boundary", 0, 90, 10, 1.0,
                                alat_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("mean u", -100, 100, 0, 1.0,
                                ubar1_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("mean v", -100, 100, 0, 1.0,
                                vbar1_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("hprm11", -100, 200, -100, 1.0,
                                hprm11_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("hprm12", -100, 200, -66, 1.0,
                                hprm12_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("simulated time step", 0, 1200, 325, 1.0,
                                delt_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("spatial filter constant", 0, 200, 0, 0.01,
                                eps_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("interior diffusion coefficient", 0, 1000000, 0, 1.0,
                                adiff_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("temporal filter constant", 0, 100, 0, 0.01,
                                tfilt_ref, RealType.Generic));
    sliders.add(new JLabel("  "));
    sliders.add(new VisADSlider("animation delay (ms)", 10, 2000, 2000, 1.0,
                                anim_delay_ref, RealType.Generic));
    sliders.add(new JLabel("  "));

    // construct JPanel (and sub-panels) for Displays
    JPanel display_panel = new JPanel();
    display_panel.setLayout(new BoxLayout(display_panel, BoxLayout.X_AXIS));
    display_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(display_panel);

    JPanel display_left = new JPanel();
    display_left.setLayout(new BoxLayout(display_left, BoxLayout.Y_AXIS));
    display_left.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    display_panel.add(display_left);

    JPanel display_right = new JPanel();
    display_right.setLayout(new BoxLayout(display_right, BoxLayout.Y_AXIS));
    display_right.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_right.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    display_panel.add(display_right);

    // make borders for Displays and embed in display_panel JPanel
    JPanel panel1 = (JPanel) displays[0].getComponent();
    JPanel panel2 = (JPanel) displays[1].getComponent();
    // JPanel panel3 = (JPanel) display3.getComponent();
    // JPanel panel4 = (JPanel) display4.getComponent();

    Border etchedBorder10 =
      new CompoundBorder( new EtchedBorder(), new EmptyBorder(10, 10, 10, 10));
    panel1.setBorder(etchedBorder10);
    panel2.setBorder(etchedBorder10);
    // panel3.setBorder(etchedBorder10);
    // panel4.setBorder(etchedBorder10);

    // make labels for Displays
    JLabel display1_label = new JLabel("fluid flow vectors");
    JLabel display2_label = new JLabel("fluid height surface");

    // embed Displays and their labels in display_panel JPanel
    display_left.add(panel1);
    display_left.add(display1_label);
    display_right.add(panel2);
    display_right.add(display2_label);

    // make the JFrame visible
    frame.setVisible(true);
  }


  /** advance 2-D shallow fluid state by one time step */
  class shalstepCell extends CellImpl {
    public shalstepCell(String name) throws VisADException, RemoteException {
      super(name);
    }

    public void doAction() throws VisADException, RemoteException {
      // get time step number
      int step = (int) ((Real) sim_time_ref.getData()).getValue();
      // get initial configuration
      int iopt = (int) ((Real) iopt_ref.getData()).getValue();
      // get boundary condition
      int ibc = (int) ((Real) ibc_ref.getData()).getValue();
      // get gravity constant
      float gravity = (float) ((Real) gravity_ref.getData()).getValue();
      // get latitude of south boundary
      float alat = (float) ((Real) alat_ref.getData()).getValue();
      // get mean u
      float ubar1 = (float) ((Real) ubar1_ref.getData()).getValue();
      // get mean v
      float vbar1 = (float) ((Real) vbar1_ref.getData()).getValue();
      // get hprm11
      float hprm11 = (float) ((Real) hprm11_ref.getData()).getValue();
      // get hprm12
      float hprm12 = (float) ((Real) hprm12_ref.getData()).getValue();
      // get simulated time step
      float delt = (float) ((Real) delt_ref.getData()).getValue();
      // get spatial filter constant
      float eps = (float) ((Real) eps_ref.getData()).getValue();
      // get interior diffusion coefficient
      float adiff = (float) ((Real) adiff_ref.getData()).getValue();
      // get temporal filter constant
      float tfilt = (float) ((Real) tfilt_ref.getData()).getValue();
      // get animation delay
      int anim_delay = (int) ((Real) anim_delay_ref.getData()).getValue();

      float[][] new_x;
      float[][] old_x;
      float[][] oldest_x;

      if (initial) {
        step = 0;
        delt = 0.0f;
        new_x = new float[4][59*49];
        old_x = new float[4][59*49];
        oldest_x = new float[4][59*49];
      }
      else {
        new_x = Set.doubleToFloat(((FlatField) new_state_ref.getData()).getValues());
        old_x = Set.doubleToFloat(((FlatField) old_state_ref.getData()).getValues());
        oldest_x =
          Set.doubleToFloat(((FlatField) oldest_state_ref.getData()).getValues());
      }

      shalstep_c(step, oldest_x[0], oldest_x[1], oldest_x[2], oldest_x[3],
                 old_x[0], old_x[1], old_x[2], old_x[3],
                 new_x[0], new_x[1], new_x[2], new_x[3],
                 iopt, ibc, gravity, alat, ubar1, vbar1, hprm11, hprm12,
                 delt, eps, adiff, tfilt);
/*
System.out.println("shalstep: initial = " + initial + " step = " + step +
                   " iopt = " + iopt + " new_x[3][1234] = " + new_x[3][1234]);
*/
      ((FlatField) new_state_ref.getData()).setSamples(new_x);

/*
System.out.println("new_state = " + new_state_ref.getData());
*/

      if (initial) {
        initial = false;
        old_state_ref.setData((FlatField)
          ((FlatField) new_state_ref.getData()).clone());
        oldest_state_ref.setData((FlatField)
          ((FlatField) new_state_ref.getData()).clone());
      }
      else {
        newold_diff_ref.setData(new_state_ref.getData().
                                subtract(oldest_state_ref.getData()));
        oldest_state_ref.setData((FlatField)
          ((FlatField) old_state_ref.getData()).clone());
        old_state_ref.setData((FlatField)
          ((FlatField) new_state_ref.getData()).clone());
      }
      step++;
      sim_time_ref.setData(new Real(time, (double) step));

      new Delay(anim_delay);
    }
  }

  /** re-initialize */
  class initializeCell extends CellImpl {
    public initializeCell() throws VisADException, RemoteException {
    }

    public void doAction() throws VisADException, RemoteException {
      initial = true;
    }
  }


  /** native method declaration, to Fortran via C */
  private native void shalstep_c(int step, float[] oldest_x0, float[] oldest_x1,
                                 float[] oldest_x2, float[] oldest_x3,
                                 float[] old_x0, float[] old_x1,
                                 float[] old_x2, float[] old_x3,
                                 float[] new_x0, float[] new_x1,
                                 float[] new_x2, float[] new_x3,
                                 int iopt, int ibc, float gravity, float alat,
                                 float ubar1, float vbar1, float hprm11,
                                 float hprm12, float delt, float eps,
                                 float adiff, float tfilt);

}

