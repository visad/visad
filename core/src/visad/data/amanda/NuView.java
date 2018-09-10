/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.amanda;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import visad.AnimationControl;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.DisplayRenderer;
import visad.Integer1DSet;
import visad.RealType;
import visad.ScalarMap;
import visad.ShapeControl;
import visad.VisADException;

import visad.java3d.DisplayImplJ3D;

import visad.util.AnimationWidget;
import visad.util.CmdlineConsumer;
import visad.util.CmdlineParser;
import visad.util.Util;

/** run 'java NuView in_file' to display data.<br>
 *  try 'java NuView 100events.r'
 */
public class NuView
  extends WindowAdapter
  implements CmdlineConsumer
{
  private static HashMap colorHash;

  static {
    colorHash = new HashMap();
    colorHash.put("black", Color.black);
    colorHash.put("blue", Color.blue);
    colorHash.put("cyan", Color.cyan);
    colorHash.put("darkGray", Color.darkGray);
    colorHash.put("gray", Color.gray);
    colorHash.put("green", Color.green);
    colorHash.put("lightGray", Color.lightGray);
    colorHash.put("magenta", Color.magenta);
    colorHash.put("orange", Color.orange);
    colorHash.put("pink", Color.pink);
    colorHash.put("red", Color.red);
    colorHash.put("white", Color.white);
    colorHash.put("yellow", Color.yellow);
  };

  private String fileName;
  private int displayDim;
  private Color trackColor;

  private DisplayImpl display, display2;

  public NuView(String[] args)
    throws RemoteException, VisADException
  {
    CmdlineParser cmdline = new CmdlineParser(this);
    if (!cmdline.processArgs(args)) {
      System.exit(1);
      return;
    }

    AmandaFile file = openFile(fileName);

    display = new DisplayImplJ3D("amanda");

    JPanel widgetPanel = buildMainDisplay(display, file, trackColor);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // make sure display window will fit on the screen
    if (displayDim > screenSize.height) {
      displayDim = screenSize.height;
    }

    JPanel displayPanel = (JPanel )display.getComponent();
    Dimension dim = new Dimension(displayDim, displayDim);
    displayPanel.setPreferredSize(dim);
    displayPanel.setMinimumSize(dim);

    // create JPanel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    panel.add(widgetPanel);
    panel.add(displayPanel);

    JFrame frame = new JFrame("VisAD AMANDA Viewer");

    frame.addWindowListener(this);
    frame.getContentPane().add(panel);
    frame.pack();
    panel.invalidate();

    Dimension fSize = frame.getSize();

    frame.setLocation((screenSize.width - fSize.width) / 2,
                      (screenSize.height - fSize.height) / 2);

    frame.setVisible(true);
  }

  private static final Component buildHelp()
  {
    String text =
      "To rotate, hold down the left mouse button and move the mouse in the right-hand display window\n\nTo zoom, move the mouse while holding down the left mouse button and Shift key\n\nTo pan, move the mouse while holding down the left mouse button and Control key";
    JTextArea textArea = new JTextArea(text);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    return textArea;

  }

  private static final JPanel buildMainDisplay(DisplayImpl dpy,
                                               AmandaFile file,
                                               Color trackColor)
    throws RemoteException, VisADException
  {
    final double halfRange = getMaxRange(file) / 2.0;

    ScalarMap xMap = new ScalarMap(RealType.XAxis, Display.XAxis);
    setRange(xMap, file.getXMin(), file.getXMax(), halfRange);
    dpy.addMap(xMap);

    ScalarMap yMap = new ScalarMap(RealType.YAxis, Display.YAxis);
    setRange(yMap, file.getYMin(), file.getYMax(), halfRange);
    dpy.addMap(yMap);

    ScalarMap zMap = new ScalarMap(RealType.ZAxis, Display.ZAxis);
    setRange(zMap, file.getZMin(), file.getZMax(), halfRange);
    dpy.addMap(zMap);

    ScalarMap shapeMap = new ScalarMap(Hit.amplitudeType, Display.Shape);
    dpy.addMap(shapeMap);

    ScalarMap trackMap =
      new ScalarMap(BaseTrack.indexType, Display.SelectValue);
    dpy.addMap(trackMap);

    ShapeControl sctl = (ShapeControl )shapeMap.getControl();
    sctl.setShapeSet(new Integer1DSet(Hit.amplitudeType, 1));
    sctl.setShapes(F2000Util.getCubeArray());

    ScalarMap shapeScaleMap =
      new ScalarMap(Hit.amplitudeType, Display.ShapeScale);
    dpy.addMap(shapeScaleMap);
    shapeScaleMap.setRange(-20.0, 50.0);

    ScalarMap colorMap = new ScalarMap(Hit.leadingEdgeTimeType, Display.RGB);
    dpy.addMap(colorMap);

    // invert color table so colors match what is expected
    F2000Util.invertColorTable(colorMap);

    ScalarMap animMap = new ScalarMap(RealType.Time, Display.Animation);
    dpy.addMap(animMap);

    DisplayRenderer dpyRenderer = dpy.getDisplayRenderer();
    dpyRenderer.setBoxOn(false);
    dpyRenderer.setBackgroundColor(Color.white);
    dpyRenderer.setForegroundColor(Color.black);

    final DataReferenceImpl eventRef = new DataReferenceImpl("event");
    // data set by eventWidget below
    dpy.addReference(eventRef);

    final DataReferenceImpl trackRef = new DataReferenceImpl("track");
    // data set by eventWidget below
    dpy.addReference(trackRef, Util.getColorMaps(trackColor));

    final DataReferenceImpl modulesRef = new DataReferenceImpl("modules");
    modulesRef.setData(file.makeModuleData());
    dpy.addReference(modulesRef, Util.getColorMaps(Color.black));

/*
    LabeledColorWidget colorWidget = new LabeledColorWidget(colorMap);
    // align along left side, to match VisADSlider alignment
    //   (if we don't left-align, BoxLayout hoses everything)
    colorWidget.setAlignmentX(Component.LEFT_ALIGNMENT);
*/

    AnimationControl animCtl = (AnimationControl )animMap.getControl();

    HistogramWidget histoWidget = new HistogramWidget(colorMap);

    EventWidget eventWidget = new EventWidget(file, eventRef, trackRef,
                                              animCtl, trackMap, histoWidget);

    Component helpText = buildHelp();

    AnimationWidget animWidget;
    try {
      animWidget = new AnimationWidget(animMap);
    } catch (Exception ex) {
      ex.printStackTrace();
      animWidget = null;
    }

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setMaximumSize(new Dimension(400, 600));

//    panel.add(colorWidget);

    panel.add(eventWidget, BorderLayout.NORTH);
    if (animWidget != null) {
      panel.add(animWidget, BorderLayout.CENTER);
    }
    panel.add(helpText, BorderLayout.SOUTH);

//    panel.add(Box.createHorizontalGlue());

    return panel;
  }

  private static final Component buildText(String text)
  {
    JTextArea textArea = new JTextArea(text);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    return textArea;
  }

  public int checkKeyword(String mainName, int thisArg, String[] args)
  {
    if (fileName == null) {
      fileName = args[thisArg];
      return 1;
    }

    return 0;
  }

  public int checkOption(String mainName, char ch, String arg)
  {
    if (ch == 'd') {
      int tmpDim;
      try {
        tmpDim = Integer.parseInt(arg);
      } catch (NumberFormatException nfe) {
        System.err.println(mainName + ": Bad display dimension \"" + arg +
                           "\"");
        return -1;
      }

      displayDim = tmpDim;
      return 2;
    }

    if (ch == 't') {
      final String lName = arg.toLowerCase();
      if (!colorHash.containsKey(lName)) {
        System.err.println(mainName + ": Unknown color \"" + arg +
                           "\". Valid colors are:");

        Iterator iter = colorHash.keySet().iterator();
        while (iter.hasNext()) {
          System.err.println("  " + iter.next());
        }

        return -1;
      }

      trackColor = (Color )colorHash.get(lName);
      return 2;
    }

    return 0;
  }

  public boolean finalizeArgs(String mainName)
  {
    if (fileName == null) {
      System.err.println(mainName + ": No file specified!");
      return false;
    }

    return true;
  }

  private static final double getMaxRange(AmandaFile file)
  {
    final double xRange = file.getXMax() - file.getXMin();
    final double yRange = file.getYMax() - file.getYMin();
    final double zRange = file.getZMax() - file.getZMin();

    return -0.5 * Math.max(xRange, Math.max(yRange, zRange));
  }

  public void initializeArgs()
  {
    displayDim = 800;
    fileName = null;
    trackColor = Color.darkGray;
  }

  public String keywordUsage()
  {
    return " fileName";
  }

  private static final AmandaFile openFile(String fileName)
    throws VisADException
  {
    AmandaFile file;
    try {
      if (fileName.startsWith("http://")) {
        // "ftp://" throws "sun.net.ftp.FtpProtocolException: RETR ..."
        file = new AmandaFile(new URL(fileName));
      } else {
        file = new AmandaFile(fileName);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new VisADException(ioe.getMessage());
    }

    return file;
  }

  public String optionUsage()
  {
    return " [-d displayDim] [-t trackColor]";
  }

  private static final void setRange(ScalarMap map, double min, double max,
                                     double halfRange)
    throws RemoteException, VisADException
  {
    final double mid = (min + max) / 2.0;
    map.setRange(mid - halfRange, mid + halfRange);
  }

  public void windowClosing(WindowEvent evt)
  {
    try { display.destroy(); } catch (Exception e) { }
    try { display2.destroy(); } catch (Exception e) { }
    System.exit(0);
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new NuView(args);
  }
}
