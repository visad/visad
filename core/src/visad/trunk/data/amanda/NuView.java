/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.AnimationControl;
import visad.BaseColorControl;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.DisplayRenderer;
import visad.FieldImpl;
import visad.GraphicsModeControl;
import visad.Integer1DSet;
import visad.ScalarType;
import visad.RealType;
import visad.ScalarMap;
import visad.ShapeControl;
import visad.VisADException;

import visad.java3d.DisplayImplJ3D;

import visad.util.AnimationWidget;
import visad.util.CmdlineConsumer;
import visad.util.CmdlineParser;
import visad.util.LabeledColorWidget;
import visad.util.VisADSlider;

/** run 'java NuView in_file' to display data.<br>
 *  try 'java NuView 100events.r'
 */
public class NuView
  extends WindowAdapter
  implements CmdlineConsumer
{
  private String fileName;

  private DisplayImpl display;

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

    final double halfRange = getMaxRange(file) / 2.0;

    ScalarMap xMap = new ScalarMap(RealType.XAxis, Display.XAxis);
    setRange(xMap, file.getXMin(), file.getXMax(), halfRange);
    display.addMap(xMap);

    ScalarMap yMap = new ScalarMap(RealType.YAxis, Display.YAxis);
    setRange(yMap, file.getYMin(), file.getYMax(), halfRange);
    display.addMap(yMap);

    ScalarMap zMap = new ScalarMap(RealType.ZAxis, Display.ZAxis);
    setRange(zMap, file.getZMin(), file.getZMax(), halfRange);
    display.addMap(zMap);

    ScalarMap shapeMap = new ScalarMap(Hit.amplitudeType, Display.Shape);
    display.addMap(shapeMap);

    ScalarMap trackMap =
      new ScalarMap(BaseTrack.indexType, Display.SelectValue);
    display.addMap(trackMap);

    ShapeControl sctl = (ShapeControl )shapeMap.getControl();
    sctl.setShapeSet(new Integer1DSet(Hit.amplitudeType, 1));
    sctl.setShapes(F2000Util.getCubeArray());

    ScalarMap shapeScaleMap =
      new ScalarMap(Hit.amplitudeType, Display.ShapeScale);
    display.addMap(shapeScaleMap);
    shapeScaleMap.setRange(-20.0, 50.0);

    ScalarMap colorMap = new ScalarMap(Hit.leadingEdgeTimeType, Display.RGB);
    display.addMap(colorMap);

    // invert color table so colors match what is expected
    BaseColorControl colorCtl = (BaseColorControl )colorMap.getControl();
    final int numColors = colorCtl.getNumberOfColors();
    final int numComps = colorCtl.getNumberOfComponents();
    float[][] table = colorCtl.getTable();
    for (int i = 0; i < numColors / 2; i++) {
      final int swaploc = numColors - (i + 1);
      for (int j = 0; j < numComps; j++) {
        float tmp = table[j][i];
        table[j][i] = table[j][swaploc];
        table[j][swaploc] = tmp;
      }
    }
    colorCtl.setTable(table);

    ScalarMap animMap = new ScalarMap(RealType.Time, Display.Animation);
    display.addMap(animMap);

    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    displayRenderer.setBoxOn(false);

    final DataReferenceImpl eventRef = new DataReferenceImpl("event");
    // data set by eventWidget below
    display.addReference(eventRef);

    final DataReferenceImpl trackRef = new DataReferenceImpl("track");
    // data set by eventWidget below
    display.addReference(trackRef);

    final DataReferenceImpl modulesRef = new DataReferenceImpl("modules");
    modulesRef.setData(file.makeModuleData());
    display.addReference(modulesRef);

    LabeledColorWidget colorWidget = new LabeledColorWidget(colorMap);
    // align along left side, to match VisADSlider alignment
    //   (if we don't left-align, BoxLayout hoses everything)
    colorWidget.setAlignmentX(Component.LEFT_ALIGNMENT);

    AnimationControl animCtl = (AnimationControl )animMap.getControl();

    EventWidget eventWidget = new EventWidget(file, eventRef, trackRef,
                                              animCtl, trackMap);

    AnimationWidget animWidget;
    try {
      animWidget = new AnimationWidget(animMap);
    } catch (Exception ex) {
      ex.printStackTrace();
      animWidget = null;
    }

    JPanel widgetPanel = new JPanel();
    widgetPanel.setLayout(new BoxLayout(widgetPanel, BoxLayout.Y_AXIS));
    widgetPanel.setMaximumSize(new Dimension(400, 600));

    widgetPanel.add(colorWidget);
    widgetPanel.add(eventWidget);
    if (animWidget != null) {
      widgetPanel.add(animWidget);
    }

    widgetPanel.add(Box.createHorizontalGlue());

    JPanel displayPanel = (JPanel )display.getComponent();
    Dimension dim = new Dimension(800, 800);
    displayPanel.setPreferredSize(dim);
    displayPanel.setMinimumSize(dim);

    // if widgetPanel alignment doesn't match
    //  displayPanel alignment, BoxLayout will freak out
    widgetPanel.setAlignmentX(displayPanel.getAlignmentX());
    widgetPanel.setAlignmentY(displayPanel.getAlignmentY());

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

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((screenSize.width - fSize.width)/2,
                      (screenSize.height - fSize.height)/2);

    frame.setVisible(true);
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
    fileName = null;
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

  public String keywordUsage()
  {
    return " fileName";
  }

  public String optionUsage()
  {
    return " [-o(ldData)]";
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
    System.exit(0);
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new NuView(args);
  }
}
