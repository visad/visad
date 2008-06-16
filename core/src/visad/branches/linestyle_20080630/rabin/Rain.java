
//
// Rain.java
//

package visad.rabin;

// import needed classes
import visad.*;
import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.util.VisADSlider;
import visad.util.LabeledColorWidget;
import visad.util.Delay;
import visad.data.Form;
import visad.data.vis5d.Vis5DForm;
import visad.data.netcdf.Plain;
import visad.formula.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Rain implements ActionListener, ControlListener {

  /** RemoteServerImpl for server
      this Rain is a server if server_server != null
      this Rain is stand-alone if server_server == null
        and client_server == null */
  RemoteServerImpl server_server = null;

  /** RemoteServer for client
      this Rain is a client if client_server != null */
  RemoteServer client_server = null;

  /** whether to use Java2D or Java3D */
  boolean twod = false;

  static final int N_COLUMNS = 3;
  static final int N_ROWS = 4;
  final JPanel[] column_panels =
    new JPanel[N_COLUMNS];
  final JPanel[][] cell_panels =
    new JPanel[N_ROWS][N_COLUMNS];
  final DataReference[][] cell_refs =
    new DataReferenceImpl[N_ROWS][N_COLUMNS];
  final CellImpl[][] cells =
    new CellImpl[N_ROWS][N_COLUMNS];
  final DisplayImpl[][] displays =
    new DisplayImpl[N_ROWS][N_COLUMNS];
  final RemoteDisplayImpl[][] remote_displays =
    new RemoteDisplayImpl[N_ROWS][N_COLUMNS];
  final CellImpl[][] formula_update =
    new CellImpl[N_ROWS][N_COLUMNS];
  final boolean[][] display_done =
    new boolean[N_ROWS][N_COLUMNS];

  static final String[][] cell_names =
    {{"A1", "B1", "C1"}, {"A2", "B2", "C2"},
     {"A3", "B3", "C3"}, {"A4", "B4", "C4"}};

  static final String[][] cell_formulas =
    {{"", "A1[0]",
      "(10^(extract(B1, 0)/10)/num300) ^ (1/num1_4)"},
     {"(10^(extract(B1, 1)/10)/num300) ^ (1/num1_4)",
      "(10^(extract(B1, 2)/10)/num300) ^ (1/num1_4)",
      "(10^(extract(B1, 3)/10)/num300) ^ (1/num1_4)"},
     {"(10^(extract(B1, 4)/10)/num300) ^ (1/num1_4)",
      "(10^(extract(B1, 5)/10)/num300) ^ (1/num1_4)",
      "(10*C1 + 10*A2 + 10*B2 + 10*C2 + 10*A3 + 3*B3)/53"},
     {"extract(B1, 6)", "extract(B1, 7)", "extract(B1, 8)"}};

  JLabel[][] cell_fields = new JLabel[N_ROWS][N_COLUMNS];

  /** width and height of the UI frame */
  static final int WIDTH = 1100;
  static final int HEIGHT = 900;

  static final double MIN = 0.0;
  static final double MAX = 300.0;
  static final double MAXH2 = 10.0;

  /** remoted DataReferences */
  DataReference ref300 = null;
  DataReference ref1_4 = null;
  DataReference refMAX = null;
  DataReference ref_cursor = null;
  DataReference ref_vis5d = null;
  DataReference ref_projection = null;
  DataReference ref_colorH1 = null;
  DataReference ref_colorH2 = null;
  DataReference[][] cell_text = new DataReference[N_ROWS][N_COLUMNS];

  /** widgets and controls */
  VisADSlider slider300;
  LabeledColorWidget color_widgetH1 = null;
  LabeledColorWidget color_widgetH2 = null;
  ColorControl color_controlH1 = null;
  ColorControl color_controlH2 = null;
  ColorControl[][] color_controls = new ColorControl[N_ROWS][N_COLUMNS];
  ProjectionControl[][] projection_controls =
    new ProjectionControl[N_ROWS][N_COLUMNS];
  ScalarMap[][] color_maps = new ScalarMap[N_ROWS][N_COLUMNS];
  ScalarMap color_mapH1 = null;
  ScalarMap color_mapH2 = null;

  /** seventh band, used for point mode detection during band changes */
  RealType band7 = null;

  /** cursor */
  RealTupleType cursor_type = null;

  /** formula-related objects */
  FormulaManager f_manager = null;
  JTextField[][] jtfield = new JTextField[N_ROWS][N_COLUMNS];

  // type 'java Rain' to run this application
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {
    if (args == null || args.length < 1) {
      System.out.println("run 'java visad.rabin.Rain file.v5d'\n or");
      System.out.println("    'java visad.rabin.Rain file.nc'\n or");
      System.out.println("    'java visad.rabin.Rain server.ip.name'");
      System.exit(1);
    }
    Rain rain = new Rain(args);

    rain.makeRain();
  }

  private Rain(String args[])
          throws VisADException, RemoteException {
    if (args[0].endsWith(".v5d") || args[0].endsWith(".nc")) {
      // this is server
      // try to set up a RemoteServer
      server_server = new RemoteServerImpl();
      try {
        Naming.rebind("///Rain", server_server);
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

      Form form = null;
      if (args[0].endsWith(".v5d")) {
        try {
          form = new Vis5DForm();
        } catch (UnsatisfiedLinkError e) {
          System.out.println("Cannot find vis5d library: " + e.getMessage());
          System.exit(1);
        }
      }
      else {
        form = new Plain();
      }
      FieldImpl vis5d = null;
      try {
        vis5d = (FieldImpl) form.open(args[0]);
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
        System.exit(0);
      }
      if (vis5d == null) {
        System.out.println("bad Vis5D file read");
        System.exit(0);
      }

      ref300 = new DataReferenceImpl("num300");
      ref1_4 = new DataReferenceImpl("num1_4");
      refMAX = new DataReferenceImpl("colorMAX");
      ref_cursor = new DataReferenceImpl("cursor");
      ref_vis5d = new DataReferenceImpl("vis5d");
      ref_projection = new DataReferenceImpl("projection");
      ref_colorH1 = new DataReferenceImpl("colorH1");
      ref_colorH2 = new DataReferenceImpl("colorH2");
      for (int i=0; i<N_ROWS; i++) {
        for (int j=0; j<N_COLUMNS; j++) {
          cell_text[i][j] = new DataReferenceImpl("text_" + i + "_" + j);
        }
      }

      ref_vis5d.setData(vis5d);
      if (server_server != null) {
        // set RemoteDataReferenceImpls in RemoteServer
        RemoteDataReferenceImpl[] refs =
          new RemoteDataReferenceImpl[8 + N_ROWS*N_COLUMNS];
        refs[0] =
          new RemoteDataReferenceImpl((DataReferenceImpl) ref300);
        refs[1] =
          new RemoteDataReferenceImpl((DataReferenceImpl) ref1_4);
        refs[2] =
          new RemoteDataReferenceImpl((DataReferenceImpl) refMAX);
        refs[3] =
          new RemoteDataReferenceImpl((DataReferenceImpl) ref_cursor);
        refs[4] =
          new RemoteDataReferenceImpl((DataReferenceImpl) ref_vis5d);
        refs[5] =
          new RemoteDataReferenceImpl((DataReferenceImpl) ref_projection);
        refs[6] =
          new RemoteDataReferenceImpl((DataReferenceImpl) ref_colorH1);
        refs[7] =
          new RemoteDataReferenceImpl((DataReferenceImpl) ref_colorH2);
        for (int i=0; i<N_ROWS; i++) {
          for (int j=0; j<N_COLUMNS; j++) {
            refs[8 + N_COLUMNS*i + j] =
              new RemoteDataReferenceImpl((DataReferenceImpl) cell_text[i][j]);
          }
        }
        server_server.setDataReferences(refs);
      }
    }
    else { // if (!(args[0].endsWith(".v5d") || args[0].endsWith(".nc")))
      // this is client
      // try to connect to RemoteServer
      String domain = "//" + args[0] + "/Rain";
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
      RemoteDataReference[] refs = client_server.getDataReferences();
      if (refs == null) {
        System.out.println("Cannot connect to server");
        System.exit(0);
      }
      ref300 = refs[0];
      ref1_4 = refs[1];
      refMAX = refs[2];
      ref_cursor = refs[3];
      // localize the big data set once at the start
      ref_vis5d = new DataReferenceImpl("vis5d");
      ref_vis5d.setData(refs[4].getData().local());
      ref_projection = refs[5];
      ref_colorH1 = refs[6];
      ref_colorH2 = refs[7];
      for (int i=0; i<N_ROWS; i++) {
        for (int j=0; j<N_COLUMNS; j++) {
          cell_text[i][j] = refs[8 + N_COLUMNS*i + j];
        }
      }
    }
  }

  private void makeRain()
         throws VisADException, RemoteException, IOException {
    FieldImpl vis5d = (FieldImpl) ref_vis5d.getData();

    FunctionType vis5d_type = (FunctionType) vis5d.getType();
    RealType time = (RealType) vis5d_type.getDomain().getComponent(0);
    FunctionType grid_type = (FunctionType) vis5d_type.getRange();
    RealTupleType domain = grid_type.getDomain();
    RealType x_domain = (RealType) domain.getComponent(0);
    RealType y_domain = (RealType) domain.getComponent(1);
    RealTupleType range = (RealTupleType) grid_type.getRange();
    RealType rangeH1 = (RealType) range.getComponent(0);
    RealType rangeH2 = (RealType) range.getComponent(8);
    int dim = range.getDimension();
    RealType[] range_types = new RealType[dim];
    for (int i=0; i<dim; i++) {
      range_types[i] = (RealType) range.getComponent(i);
    }

    // create cursor
    final RealType shape = RealType.getRealType("shape");
    RealTupleType cursor_type = new RealTupleType(x_domain, y_domain, shape);
    SampledSet grid_set =
      (SampledSet) ((FlatField) vis5d.getSample(0)).getDomainSet();
    float[] lows = grid_set.getLow();
    float[] his = grid_set.getHi();
    double cursorx = 0.5 * (lows[0] + his[0]);
    double cursory = 0.5 * (lows[1] + his[1]);
    RealTuple cursor =
      new RealTuple(cursor_type, new double[] {cursorx, cursory, 0.0});
    ref_cursor.setData(cursor);
    final Gridded1DSet shape_count_set =
      new Gridded1DSet(shape, new float[][] {{0.0f}}, 1);
    VisADLineArray cross = new VisADLineArray();
    cross.coordinates = new float[]
      {0.1f,  0.0f, 0.0f,    -0.1f,  0.0f, 0.0f,
       0.0f, -0.1f, 0.0f,     0.0f,  0.1f, 0.0f};
    cross.colors = new byte[]
      {-1,  -1, -1,     -1,  -1, -1,
       -1,  -1, -1,     -1,  -1, -1};
    cross.vertexCount = cross.coordinates.length / 3;
    final VisADGeometryArray[] shapes = {cross};

    // create formula manager object with standard options
    f_manager = FormulaUtil.createStandardManager();
    f_manager.createVar("num300", ref300);
    f_manager.createVar("num1_4", ref1_4);

    //
    // construct JFC user interface
    //

    // create a JFrame
    JFrame frame = new JFrame("Vis5D");
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

    final JPanel left_panel = new JPanel();
    left_panel.setLayout(new BoxLayout(left_panel, BoxLayout.Y_AXIS));
    left_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    left_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(left_panel);

    JPanel display_panel = new JPanel();
    display_panel.setLayout(new BoxLayout(display_panel, BoxLayout.X_AXIS));
    display_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    display_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    big_panel.add(display_panel);

    // create column JPanels
    for (int j=0; j<N_COLUMNS; j++) {
      column_panels[j] = new JPanel();
      column_panels[j].setLayout(new BoxLayout(column_panels[j],
                                            BoxLayout.Y_AXIS));
      column_panels[j].setAlignmentY(JPanel.TOP_ALIGNMENT);
      column_panels[j].setAlignmentX(JPanel.LEFT_ALIGNMENT);
      display_panel.add(column_panels[j]);
    }
    // create row JPanels
    for (int i=0; i<N_ROWS; i++) {

      // create cell JPanels
      for (int j=0; j<N_COLUMNS; j++) {
        cell_panels[i][j] = new JPanel();
        cell_panels[i][j].setLayout(new BoxLayout(cell_panels[i][j],
                                                 BoxLayout.Y_AXIS));
        cell_panels[i][j].setAlignmentY(JPanel.TOP_ALIGNMENT);
        cell_panels[i][j].setAlignmentX(JPanel.LEFT_ALIGNMENT);
        column_panels[j].add(cell_panels[i][j]);
        if (i == 0 && j ==0) {
          cell_refs[i][j] = ref_vis5d;
        }
        else {
          cell_refs[i][j] = new DataReferenceImpl("cell_" + i + "_" + j);
        }
        displays[i][j] = newDisplay("display_" + i + "_" + j);
        if (client_server != null) {
          remote_displays[i][j] = new RemoteDisplayImpl(displays[i][j]);
        }
        displays[i][j].addMap(new ScalarMap(y_domain, Display.XAxis));
        displays[i][j].addMap(new ScalarMap(x_domain, Display.YAxis));

        // add cursor to cell
        ScalarMap shape_map = new ScalarMap(shape, Display.Shape);
        displays[i][j].addMap(shape_map);
        ShapeControl shape_control = (ShapeControl) shape_map.getControl();
        shape_control.setShapeSet(shape_count_set);
        shape_control.setShapes(shapes);

        projection_controls[i][j] = displays[i][j].getProjectionControl();
        projection_controls[i][j].addControlListener(this);

        display_done[i][j] = false;

        // add cell to FormulaManager database
        f_manager.createVar(cell_names[i][j], cell_refs[i][j]);
        f_manager.setTextRef(cell_names[i][j], cell_text[i][j]);

        // construct cell's formula text field
        JPanel fpanel = new JPanel();
        fpanel.setLayout(new BoxLayout(fpanel, BoxLayout.X_AXIS));
        jtfield[i][j] = new JTextField(cell_formulas[i][j]);
        Dimension psize = jtfield[i][j].getPreferredSize();
        Dimension msize = jtfield[i][j].getMaximumSize();
        msize.height = psize.height;
        jtfield[i][j].setMaximumSize(msize);
        jtfield[i][j].addActionListener(this);
        jtfield[i][j].setActionCommand("fc_" + cell_names[i][j]);
        fpanel.add(new JLabel(cell_names[i][j] + ": "));
        fpanel.add(jtfield[i][j]);
        cell_panels[i][j].add(fpanel);

        // construct cell's CellImpl for detecting changes in the cell
        final int fi = i;
        final int fj = j;
        final Rain rain = this;

        formula_update[i][j] = new CellImpl() {
          public void doAction() {
            // save old mappings
            ScalarMap[] maps = null;
            Vector v = displays[fi][fj].getMapVector();
            maps = new ScalarMap[v.size()];
            for (int k=0; k<maps.length; k++) {
              maps[k] = (ScalarMap) v.elementAt(k);
            }

            // identify whether the mappings need to be replaced
            boolean change = false;
            for (int k=0; k<maps.length && !change; k++) {
              RealType ort = (RealType) maps[k].getScalar();
              DisplayRealType drt = maps[k].getDisplayScalar();
              if (drt.equals(Display.RGB)) {
                try {
                  Data d = cell_refs[fi][fj].getData();
                  if (d != null) {
                    FunctionType f = (FunctionType) d.getType();
                    RealType rt = (RealType) f.getRange();
                    if (!rt.equals(ort)) change = true;
                  }
                }
                catch (ClassCastException exc) { }
                catch (VisADException exc) { }
                catch (RemoteException exc) { }
              }
            }

            if (change) {
              // clear old mappings
              try {
                displays[fi][fj].removeReference(cell_refs[fi][fj]);
                if (color_controls[fi][fj] != null || (fi == 0 && fj == 2)) {
                  removeCursor(fi, fj);
                }
                displays[fi][fj].clearMaps();
              }
              catch (VisADException exc) { }
              catch (RemoteException exc) { }

              // reapply mappings
              for (int k=0; k<maps.length; k++) {
                boolean done = false;
                RealType ort = (RealType) maps[k].getScalar();
                DisplayRealType drt = maps[k].getDisplayScalar();
                if (drt.equals(Display.RGB)) {
                  // fix ScalarMap and color table to use new range RealType
                  try {
                    Data d = cell_refs[fi][fj].getData();
                    FunctionType f = (FunctionType) d.getType();
                    RealType rt = (RealType) f.getRange();
                    if (!rt.equals(ort)) {
                      // range RealType has changed
                      ScalarMap sm = new ScalarMap(rt, Display.RGB);
                      maps[k] = sm;
                      ColorControl cc = null;
                      double max;
                      if (fi == 3 && fj == 2) {
                        cc = color_controlH2;
                        max = MAXH2;
                      }
                      else {
                        cc = color_controlH1;
                        max = ((Real) refMAX.getData()).getValue();
                      }
                      if (cc != null) {
                        float[][] table = cc.getTable();
                        color_maps[fi][fj] = sm;
                        color_maps[fi][fj].setRange(MIN, max);
                        displays[fi][fj].addMap(sm);
                        done = true;
                        color_controls[fi][fj] = (ColorControl)
                          color_maps[fi][fj].getControl();
                        if (table != null) {
                          color_controls[fi][fj].setTable(table);
                        }
                      }
                      // if cell displays band #7, enable point mode
                      boolean isBand7 = rt.equals(band7);
                      GraphicsModeControl mode =
                        displays[fi][fj].getGraphicsModeControl();
                      mode.setTextureEnable(!isBand7);
                      mode.setPointMode(isBand7);
                      mode.setPointSize(5.0f);
                    }
                  }
                  catch (ClassCastException exc) { }
                  catch (VisADException exc) { }
                  catch (RemoteException exc) { }
                }
                else if (drt.equals(Display.Shape)) {
                  try {
                    ScalarMap shape_mapx = new ScalarMap(shape, Display.Shape);
                    displays[fi][fj].addMap(shape_mapx);
                    ShapeControl shape_controlx =
                      (ShapeControl) shape_mapx.getControl();
                    shape_controlx.setShapeSet(shape_count_set);
                    shape_controlx.setShapes(shapes);
                    done = true;
                  }
                  catch (VisADException exc) { }
                  catch (RemoteException exc) { }
                }
                if (!done) {
                  try {
                    displays[fi][fj].addMap(maps[k]);
                  }
                  catch (VisADException exc) { }
                  catch (RemoteException exc) { }
                }
              }
              try {
                displays[fi][fj].addReference(cell_refs[fi][fj]);
                if (color_controls[fi][fj] != null) addCursor(fi, fj);
              }
              catch (VisADException exc) { }
              catch (RemoteException exc) { }
            }
          }
        };

        // construct cell's display
        JPanel d_panel = (JPanel) displays[i][j].getComponent();
        d_panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        Border etchedBorder5 =
          new CompoundBorder(new EtchedBorder(),
                             new EmptyBorder(5, 5, 5, 5));
        d_panel.setBorder(etchedBorder5);
        cell_panels[i][j].add(d_panel);

        // construct cell's current value label
        cell_fields[i][j] = new JLabel("---");
        cell_fields[i][j].setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cell_fields[i][j].setMinimumSize(jtfield[i][j].getMinimumSize());
        cell_fields[i][j].setPreferredSize(jtfield[i][j].getPreferredSize());
        cell_fields[i][j].setMaximumSize(jtfield[i][j].getMaximumSize());
        cell_panels[i][j].add(cell_fields[i][j]);

      } // end for (int j=0; j<N_ROWS; j++)
    } // end for (int i=0; i<N_COLUMNS; i++)

    slider300 = new VisADSlider("num300", 0, 600, 300, 1.0,
                                ref300, RealType.Generic);
    VisADSlider slider1_4 = new VisADSlider("num1_4", 0, 280, 140, 0.01,
                                            ref1_4, RealType.Generic);
    VisADSlider sliderMAX = new VisADSlider("colorMAX", 0, 1000, ((int) MAX),
                                            1.0, refMAX, RealType.Generic);

    left_panel.add(slider300);
    left_panel.add(new JLabel("  "));
    left_panel.add(slider1_4);
    left_panel.add(new JLabel("  "));
    left_panel.add(sliderMAX);
    left_panel.add(new JLabel("  "));

    // set up cells' formulas
    for (int i=0; i<N_ROWS; i++) {
      for (int j=0; j<N_COLUMNS; j++) {
        if (i != 0 || j != 0) {
          f_manager.assignFormula(cell_names[i][j], cell_formulas[i][j]);
        }
      }
    }

    // set up cells' displays

    // set up hidden cell 1, for use with the first ColorControl (cells C1-B4)
    color_mapH1 = new ScalarMap(rangeH1, Display.RGB);
    DisplayImpl displayH1 = newDisplay("display_hidden_1");
    displayH1.addMap(color_mapH1);
    color_widgetH1 = new LabeledColorWidget(color_mapH1);
    Dimension d = new Dimension(500, 170);
    color_widgetH1.setMaximumSize(d);
    color_mapH1.setRange(MIN, MAX);

    left_panel.add(color_widgetH1);
    left_panel.add(new JLabel("  "));

    color_controlH1 = (ColorControl) color_mapH1.getControl();
    // listener sets all non-null color_controls[i][j]
    // for ControlEvents from color_control
    color_controlH1.addControlListener(this);
    if (server_server != null) {
      float[][] table = color_controlH1.getTable();
      Integer1DSet set = new Integer1DSet(table[0].length);
      FlatField color_fieldH1 =
        new FlatField(FunctionType.REAL_1TO3_FUNCTION, set);
      color_fieldH1.setSamples(table);
      ref_colorH1.setData(color_fieldH1);
    }

    // set up cell for detecting color table changes and updating all displays
    if (server_server != null || client_server != null) {
      CellImpl color_cellH1 = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          FlatField field = (FlatField) ref_colorH1.getData().local();
          float[][] table = field.getFloats();
          float[][] old_table = color_controlH1.getTable();
          boolean identical = true;
          for (int i=0; i<3; i++) {
            if (identical) {
              for (int j=0; j<table[i].length; j++) {
                if (Math.abs(table[i][j] - old_table[i][j]) > 0.00001) {
                  identical = false;
                  break;
                }
              }
            }
          }
          if (!identical) {
            color_controlH1.setTable(table);
          }
        }
      };
      if (client_server != null) {
        RemoteCellImpl remote_cell = new RemoteCellImpl(color_cellH1);
        remote_cell.addReference(ref_colorH1);
      }
      else {
        color_cellH1.addReference(ref_colorH1);
      }
    }

    // set up hidden cell 2, for use with the second ColorControl (cell C4)
    color_mapH2 = new ScalarMap(rangeH2, Display.RGB);
    DisplayImpl displayH2 = newDisplay("display_hidden_2");
    displayH2.addMap(color_mapH2);
    color_widgetH2 = new LabeledColorWidget(color_mapH2);

    Dimension dH2 = new Dimension(500, 170);
    color_widgetH2.setMaximumSize(dH2);
    color_mapH2.setRange(MIN, MAXH2);
    color_controlH2 = (ColorControl) color_mapH2.getControl();
    color_controlH2.addControlListener(this);

    if (server_server != null) {
      float[][] table = color_controlH2.getTable();
      Integer1DSet set = new Integer1DSet(table[0].length);
      FlatField color_fieldH2 =
        new FlatField(FunctionType.REAL_1TO3_FUNCTION, set);
      color_fieldH2.setSamples(table);
      ref_colorH2.setData(color_fieldH2);
    }

    if (server_server != null || client_server != null) {
      CellImpl color_cellH2 = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          FlatField field = (FlatField) ref_colorH2.getData().local();
          float[][] table = field.getFloats();
          float[][] old_table = color_controlH2.getTable();
          boolean identical = true;
          for (int i=0; i<3; i++) {
            if (identical) {
              for (int j=0; j<table[i].length; j++) {
                if (Math.abs(table[i][j] - old_table[i][j]) > 0.00001) {
                  identical = false;
                  break;
                }
              }
            }
          }
          if (!identical) {
            color_controlH2.setTable(table);
          }
        }
      };
      if (client_server != null) {
        RemoteCellImpl remote_cell = new RemoteCellImpl(color_cellH2);
        remote_cell.addReference(ref_colorH2);
      }
      else {
        color_cellH2.addReference(ref_colorH2);
      }
    }

    left_panel.add(color_widgetH2);
    left_panel.add(new JLabel("  "));

    // set up cell A1
    displays[0][0].addMap(new ScalarMap(range_types[0], Display.Red));
    displays[0][0].addMap(new ScalarMap(range_types[1], Display.Green));
    displays[0][0].addMap(new ScalarMap(range_types[2], Display.Blue));
    displays[0][0].addMap(new ScalarMap(time, Display.Animation));
    displays[0][0].addReference(cell_refs[0][0]);
    display_done[0][0] = true;

    if (server_server != null) {
      double[] matrix = projection_controls[0][0].getMatrix();
      if (matrix.length != 6) {
        matrix = ProjectionControl.matrix3DTo2D(matrix);
      }
      Integer1DSet set = new Integer1DSet(6);
      FlatField projection_field =
        new FlatField(FunctionType.REAL_1TO1_FUNCTION, set);
      projection_field.setSamples(new double[][] {matrix});
      ref_projection.setData(projection_field);
    }

    if (server_server != null || client_server != null) {
      CellImpl projection_cell = new CellImpl() {
        public void doAction() throws VisADException, RemoteException {
          FlatField field = (FlatField) ref_projection.getData().local();
          double[] matrix = field.getValues()[0];
          double[] old = projection_controls[0][0].getMatrix();
          double[] old_matrix = null;
          if (old.length == 6) {
            old_matrix = old;
          }
          else {
            old_matrix = ProjectionControl.matrix3DTo2D(old);
          }
          boolean identical = true;
          for (int j=0; j<matrix.length; j++) {
            if (Math.abs(matrix[j] - old_matrix[j]) > 0.00001) {
              identical = false;
              break;
            }
          }
          if (!identical) {
            if (old.length == 6) {
              projection_controls[0][0].setMatrix(matrix);
            }
            else {
              double[] mat = ProjectionControl.matrix2DTo3D(matrix);
              projection_controls[0][0].setMatrix(mat);
            }

          }
        }
      };
      if (client_server != null) {
        RemoteCellImpl remote_cell = new RemoteCellImpl(projection_cell);
        remote_cell.addReference(ref_projection);
      }
      else {
        projection_cell.addReference(ref_projection);
      }
    }

    // cell B1
    displays[0][1].addMap(new ScalarMap(range_types[0], Display.Red));
    displays[0][1].addMap(new ScalarMap(range_types[1], Display.Green));
    displays[0][1].addMap(new ScalarMap(range_types[2], Display.Blue));
    displays[0][1].addReference(cell_refs[0][1]);
    display_done[0][1] = true;

    // cell C1
    finishDisplay(client_server, (RealType) range.getComponent(0), 0, 2);

    // cell A2
    finishDisplay(client_server, (RealType) range.getComponent(1), 1, 0);

    // cell B2
    finishDisplay(client_server, (RealType) range.getComponent(2), 1, 1);

    // cell C2
    finishDisplay(client_server, (RealType) range.getComponent(3), 1, 2);

    // cell A3
    finishDisplay(client_server, (RealType) range.getComponent(4), 2, 0);

    // cell B3
    finishDisplay(client_server, (RealType) range.getComponent(5), 2, 1);

    // cell C3
    finishDisplay(client_server, rangeH1, 2, 2);

    // cell A4
    finishDisplay(client_server, (RealType) range.getComponent(6), 3, 0);

    // cell B4
    band7 = (RealType) range.getComponent(7);
    finishDisplay(client_server, band7, 3, 1);

    // enable point mode for cell B4
    GraphicsModeControl mode = displays[3][1].getGraphicsModeControl();
    mode.setTextureEnable(false);
    mode.setPointMode(true);
    mode.setPointSize(5.0f);

    // cell C4
    finishDisplay(client_server, (RealType) range.getComponent(8), 3, 2);

    // cell for updating formula text fields when formulas change
    CellImpl cell_formulas = new CellImpl() {
      public void doAction() {
        for (int i=0; i<N_ROWS; i++) {
          for (int j=0; j<N_COLUMNS; j++) {
            try {
              Text t = (Text) cell_text[i][j].getThing();
              if (t != null) {
                String s = t.getValue();
                if (s == null) s = "";
                if (!s.equals(jtfield[i][j].getText())) {
                  final JTextField jtf = jtfield[i][j];
                  final String str = s;
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      jtf.setText(str);
                    }
                  });
                }
              }
            }
            catch (VisADException exc) { }
            catch (RemoteException exc) { }
          }
        }
      }
    };
    if (client_server != null) {
      RemoteCellImpl remote_cell = new RemoteCellImpl(cell_formulas);
      for (int i=0; i<N_ROWS; i++) {
        for (int j=0; j<N_COLUMNS; j++) {
          remote_cell.addReference(cell_text[i][j]);
        }
      }
    }
    else {
      for (int i=0; i<N_ROWS; i++) {
        for (int j=0; j<N_COLUMNS; j++) {
          cell_formulas.addReference(cell_text[i][j]);
        }
      }
    }

    CellImpl cellMAX = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        double max = ((Real) refMAX.getData()).getValue();
        color_mapH1.setRange(MIN, max);
        for (int i=0; i<N_ROWS; i++) {
          for (int j=0; j<N_COLUMNS; j++) {
            if (color_maps[i][j] != null && !(i == 3 && j == 2)) {
              color_maps[i][j].setRange(MIN, max);
            }
          }
        }
      }
    };
    if (client_server != null) {
      RemoteCellImpl remote_cell = new RemoteCellImpl(cellMAX);
      remote_cell.addReference(refMAX);
    }
    else {
      cellMAX.addReference(refMAX);
    }

    CellImpl cell_cursor = new CellImpl() {
      public void doAction() throws VisADException, RemoteException {
        RealTuple c = (RealTuple) ref_cursor.getData();
        RealTuple dom = new RealTuple(new Real[]
                        {(Real) c.getComponent(0), (Real) c.getComponent(1)});
        for (int i=0; i<N_ROWS; i++) {
          for (int j=0; j<N_COLUMNS; j++) {
            try {
              FlatField field = (FlatField) cell_refs[i][j].getData();
              double val = ((Real) field.evaluate(dom)).getValue();
              cell_fields[i][j].setText("" + val);
            }
            catch (Exception e) {}
          }
        }
      }
    };
    if (client_server != null) {
      RemoteCellImpl remote_cell = new RemoteCellImpl(cell_cursor);
      remote_cell.addReference(ref_cursor);
    }
    else {
      cell_cursor.addReference(ref_cursor);
    }

    // add formula update cell references
    new Delay(1000);  // give time for cell C3 to finish computing
    for (int i=0; i<N_ROWS; i++) {
      for (int j=0; j<N_COLUMNS; j++) {
        formula_update[i][j].addReference(cell_refs[i][j]);
      }
    }

    // make the JFrame visible
    frame.setVisible(true);
  }

  /** creates a new Java3D or Java2D display */
  public DisplayImpl newDisplay(String name) throws VisADException,
                                                    RemoteException {
    DisplayImpl display = null;
    if (!twod) {
      try {
        display = new DisplayImplJ3D(name, new TwoDDisplayRendererJ3D());
      }
      catch (UnsatisfiedLinkError e) {
        twod = true;
      }
    }
    if (twod) display = new DisplayImplJ2D(name);
    return display;
  }

  /** adds a cursor to display (i, j) */
  public void addCursor(int i, int j) throws VisADException, RemoteException {
    DataRenderer dr = null;
    if (twod) {
      dr = new DirectManipulationRendererJ2D();
    }
    else {
      dr = new DirectManipulationRendererJ3D();
    }
    if (client_server != null) {
      remote_displays[i][j].addReferences(dr, ref_cursor);
    }
    else {
      displays[i][j].addReferences(dr, ref_cursor);
    }
  }

  /** removes a cursor from display (i, j) */
  public void removeCursor(int i, int j) throws VisADException,
                                                RemoteException {
    if (client_server != null) {
      remote_displays[i][j].removeReference(ref_cursor);
    }
    else {
      displays[i][j].removeReference(ref_cursor);
    }
  }

  public void finishDisplay(RemoteServer cs, RealType rt, int i, int j)
         throws VisADException, RemoteException {
    color_maps[i][j] = new ScalarMap(rt, Display.RGB);
    displays[i][j].addMap(color_maps[i][j]);
    color_maps[i][j].setRange(MIN, MAX);
    color_controls[i][j] = (ColorControl) color_maps[i][j].getControl();
    ColorControl cc = null;
    if (i == 3 && j == 4) {
      // cell C4 uses ColorControl #2
      cc = color_controlH2;
    }
    else {
      // other cells all share ColorControl #1
      cc = color_controlH1;
    }
    if (cc != null) {
      float[][] table = cc.getTable();
      if (table != null) color_controls[i][j].setTable(table);
    }
    displays[i][j].addReference(cell_refs[i][j]);
    addCursor(i, j);
    display_done[i][j] = true;
  }

  /** Handle changes to formula text fields */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.startsWith("fc_")) {
      slider300.requestFocus();
      JTextField f_field = (JTextField) e.getSource();
      String formula = f_field.getText();
      String cell_name = cmd.substring(3, cmd.length());
      try {
        f_manager.assignFormula(cell_name, formula);
      }
      catch (FormulaException exc) { }
      catch (VisADException exc) { }
    }
  }

  boolean in_proj = false;

  /** Handle changes to controls */
  public void controlChanged(ControlEvent e)
         throws VisADException, RemoteException {
    Control control = e.getControl();
    if (control.equals(color_controlH1)) {
      float[][] table = color_controlH1.getTable();
      if (table != null) {
        for (int i=0; i<N_ROWS; i++) {
          for (int j=0; j<N_COLUMNS; j++) {
            if (color_controls[i][j] != null && !(i == 3 && j == 2)) {
              color_controls[i][j].setTable(table);
            }
          }
        }
      }
      Field field = (Field) ref_colorH1.getData();
      if (field != null) field.setSamples(table);
    }
    else if (control.equals(color_controlH2)) {
      float[][] table = color_controlH2.getTable();
      if (table != null) color_controls[3][2].setTable(table);
    }
    else if (!in_proj && control != null &&
             control instanceof ProjectionControl) {
      in_proj = true; // don't allow setMatrix below to re-trigger
      double[] matrix = ((ProjectionControl) control).getMatrix();
      for (int i=0; i<N_ROWS; i++) {
        for (int j=0; j<N_COLUMNS; j++) {
          if (control != projection_controls[i][j]) {
            if (projection_controls[i][j] != null) {
              projection_controls[i][j].setMatrix(matrix);
            }
          }
        }
      }
      Field field = (Field) ref_projection.getData();
      if (matrix.length == 6) {
        if (field != null) field.setSamples(new double[][] {matrix});
      }
      else {
        double[] mat = ProjectionControl.matrix3DTo2D(matrix);
        if (field != null) field.setSamples(new double[][] {mat});
      }
      in_proj = false;
    }
  }

}

