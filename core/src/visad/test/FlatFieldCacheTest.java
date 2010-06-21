package visad.test;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.AnimationControl;
import visad.ColorControl;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.DateTime;
import visad.Display;
import visad.DisplayImpl;
import visad.FieldImpl;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Integer2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.bom.ImageRendererJ3D;
import visad.data.FlatFieldCache;
import visad.data.AreaImageAccessor;
import visad.data.AreaImageCacheAdapter;
import visad.java3d.DisplayImplJ3D;
import visad.meteorology.SatelliteImage;

/**
 * Simple test for <code>AnimationControl</code> and
 * <code>ImageRendererJ3D</code>.
 */
public class FlatFieldCacheTest extends JPanel {

  private static Logger log = Logger.getLogger(FlatFieldCacheTest.class.getName());
  
  // array used to load AREA data into
  private static int[][][] readCache;

  private static int parseInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {}
    return -1;
  }
  
  public static void main(String[] args) throws Exception {
    
    try {
    Logger logger = Logger.getLogger("visad");
    logger.setLevel(Level.FINE);
    logger.setUseParentHandlers(false);
    Handler console = new ConsoleHandler();
    console.setLevel(Level.FINE);
    console.setFormatter(new Formatter() {
      public String format(LogRecord r) {
        if (r.getThrown() != null) {
          ByteArrayOutputStream buf = new ByteArrayOutputStream();
          r.getThrown().printStackTrace(new PrintStream(buf));
          return String.format("[%s] %s\n%s", r.getLevel().getName(), r.getMessage(), buf.toString());
        }
        return String.format("[%s] %s\n", r.getLevel().getName(), r.getMessage());
      }
    });
    logger.addHandler(console);
    
    RealType elementType = RealType.getRealType("ImageElement");
    RealType lineType = RealType.getRealType("ImageLine");
    RealType bandType = RealType.getRealType("Band1");
    
    FunctionType imgType = new FunctionType(new RealTupleType(elementType, lineType), bandType);
    FunctionType timePxlVal = new FunctionType(RealType.Time, imgType);
    log.fine("Image type: " + imgType.toString());
    log.fine("Animation type: " + timePxlVal.toString());
    
    FlatFieldCache cache = new FlatFieldCache(Integer.parseInt(args[1]));
    
    //
    // load AREA files
    //
    File dir = 
      new File(args[0]);
    log.info("File location:"+dir.getPath());
    File[] files = dir.listFiles(new FileFilter(){
      public boolean accept(File pathname) {
        if (pathname.getName().endsWith("area") && pathname.isFile()) {
          return true;
        }
        return false;
      }
    });
    
    //
    // Create and sort accessors
    //
    List<AreaImageAccessor> accessors = new ArrayList<AreaImageAccessor>();
    int dwell = parseInt(args[2]);
    int band = parseInt(args[3]);
    int startLine = parseInt(args[4]);
    int numLines = parseInt(args[5]);
    int startElement = parseInt(args[6]);
    int numElements = parseInt(args[7]);
    int mag = parseInt(args[8]);
    readCache = new int[1][numLines][numElements];
    for (File file : files) {
      AreaImageAccessor accessor = new AreaImageAccessor(file.getPath(), band, readCache);
      accessor.setAreaParams(startLine, numLines, mag, startElement, numElements, mag);
      accessors.add(accessor);
    }
    Collections.sort(accessors);

    // Domain
    FieldImpl timeFld = new FieldImpl(timePxlVal, new Integer1DSet(RealType.Time, accessors.size()));
    for (int i = 0; i < accessors.size(); i++) {
      SatelliteImage template = new SatelliteImage(imgType, new Integer2DSet(numElements, numLines),
          new DateTime(i), "MET9 Satellite Image" + i, "MET9");
      AreaImageCacheAdapter acc = new AreaImageCacheAdapter(template, accessors.get(i), cache);
      timeFld.setSample(i, acc, false);
    }

    DataReference ref = new DataReferenceImpl("image");
    ref.setData(timeFld);
    
    //
    // Display related 
    //
    
    DisplayImpl display = new DisplayImplJ3D("image display");
    display.addMap(new ScalarMap(elementType, Display.XAxis));
    display.addMap(new ScalarMap(lineType, Display.YAxis));
    ScalarMap map = new ScalarMap(bandType, Display.RGB);
    display.addMap(map);
//    ColorControl ctrl = (ColorControl) map.getControl();
//    ctrl.initGreyWedge();
    
    
    ScalarMap aniMap = new ScalarMap(RealType.Time, Display.Animation);
    display.addMap(aniMap);
    AnimationControl aniCtrl = (AnimationControl) aniMap.getControl();
    aniCtrl.setStep(dwell);
    aniCtrl.setOn(true);
    
    ImageRendererJ3D renderer = new ImageRendererJ3D();
    renderer.suggestBufImageType(BufferedImage.TYPE_BYTE_GRAY);
    display.addReferences(renderer, ref);
    JFrame frame = new JFrame("Image Animation Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    frame.add(display.getComponent());
    frame.setSize(600, 600);
    frame.setVisible(true);

    } catch (Exception e) {
      System.out.println("FlatFieldCacheTest <dir with .area files> <cache size> <dwell (ms)> <band> <startline> <numLines> <startelem> <numElems> <mag>");
      e.printStackTrace();
    }
  }
}
