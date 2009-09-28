package visad.test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import javax.media.j3d.View;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import visad.ContourControl;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.FieldImpl;
import visad.GraphicsModeControl;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.QuantityDBManager;
import visad.data.netcdf.in.NetcdfAdapter;
import visad.java3d.DisplayImplJ3D;
import visad.util.CmdlineConsumer;
import visad.util.CmdlineParser;

public class TriangleStripBuilderTest implements CmdlineConsumer {

  private static Logger log = Logger.getLogger(TriangleStripBuilderTest.class.getName());

  private int verbosity = 0;
  private String filename;

  private CmdlineParser parser;

  public TriangleStripBuilderTest() {
    parser = new CmdlineParser(this);
  }

  public static void main(String[] args) throws Exception {

//    Logger logger = Logger.getLogger("visad");
//    logger.setLevel(Level.FINEST);
//    logger.setUseParentHandlers(false);
//    Handler console = new ConsoleHandler();
//    console.setLevel(Level.FINEST);
//    console.setFormatter(new Formatter() {
//      public String format(LogRecord r) {
//        if (r.getThrown() != null) {
//          ByteArrayOutputStream buf = new ByteArrayOutputStream();
//          r.getThrown().printStackTrace(new PrintStream(buf));
//          return String.format("[%s] %s\n%s", r.getLevel().getName(), r.getMessage(), buf.toString());
//        }
//        return String.format("[%s] %s\n", r.getLevel().getName(), r.getMessage());
//      }
//    });
//    logger.addHandler(console);

    TriangleStripBuilderTest test = new TriangleStripBuilderTest();
    test.parser.processArgs(args);

    Netcdf netcdf = new NetcdfFile(test.filename, true);
    QuantityDB db = QuantityDBManager.instance();
    NetcdfAdapter adapter = new NetcdfAdapter(netcdf, db);
    final FieldImpl data = (FieldImpl) adapter.getData();
    log.info("loaded " + data.getType().toString());

    final DisplayImpl display = new DisplayImplJ3D("display");

    ScalarMap xmap = new ScalarMap(RealType.Longitude, Display.XAxis);
    display.addMap(xmap);
    ScalarMap ymap = new ScalarMap(RealType.Latitude, Display.YAxis);
    display.addMap(ymap);
    // ScalarMap zmap = new ScalarMap(RealType.Altitude, Display.ZAxis);
    // display.addMap(zmap);
    // zmap.setRange(-20, 20);
    ScalarMap rgbaMap = new ScalarMap(RealType.Altitude, Display.RGBA);
    display.addMap(rgbaMap);

    ScalarMap map1contour = new ScalarMap(RealType.Altitude, Display.IsoContour);
    display.addMap(map1contour);
    ContourControl ctrl = (ContourControl) map1contour.getControl();
    ctrl.setDashedStyle(GraphicsModeControl.SOLID_STYLE);
    ctrl.setContourFill(true);

    GraphicsModeControl mode = display.getGraphicsModeControl();
    mode.setProjectionPolicy(View.PARALLEL_PROJECTION);
    mode.setScaleEnable(true);

    final DataReferenceImpl ref = new DataReferenceImpl("r");
    ref.setData(data);
//    display.addReference(ref, null);

    JFrame jframe = new JFrame();
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jframe.setLayout(new BorderLayout());
    jframe.add((JPanel) display.getComponent(), BorderLayout.CENTER);
    JPanel panel = new JPanel();
    JButton start = new JButton("Start");
    start.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        try {
          display.addReference(ref, null);
        } catch (RemoteException e) {
          e.printStackTrace();
        } catch (VisADException e) {
          e.printStackTrace();
        }
      }
    });
    panel.add(start);
    jframe.add(panel, BorderLayout.SOUTH);
    jframe.pack();
    jframe.setVisible(true);
//
//    ContourWidget cw = new ContourWidget(map1contour);
//
//    JPanel big_panel = new JPanel();
//    big_panel.setLayout(new BorderLayout());
//    big_panel.add("Center", cw);
//
//    JFrame jframe2 = new JFrame();
//    jframe2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//    jframe2.setContentPane(big_panel);
//    jframe2.pack();
//    jframe2.setVisible(true);
//
////    LabeledColorWidget caw = new LabeledColorWidget(rgbaMap);
//    JPanel big_panel2 = new JPanel();
//    big_panel2.setLayout(new BorderLayout());
//    big_panel2.add(caw, BorderLayout.CENTER);
//    JFrame jframe3 = new JFrame();
//    jframe3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    jframe3.setContentPane(big_panel2);
//    jframe3.pack();
//    jframe3.setVisible(true);
  }

  public int checkKeyword(String mainName, int thisArg, String[] args) {
    filename = args[thisArg];
    if (!(new File(filename).exists())) {
      return -1;
    }
    return 1;
  }

  public int checkOption(String mainName, char ch, String arg) {
    switch (ch) {
    case 'v':
      verbosity++;
      return 1;
    }
    return 0;
  }

  public boolean finalizeArgs(String mainName) {
     visad.util.Util.configureLogging(verbosity);
    return true;
  }

  public void initializeArgs() {
  }

  public String keywordUsage() {
    return "";
  }

  public String optionUsage() {
    return "[-v ...] <nc file>";
  }

}
