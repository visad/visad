//
// BioVisAD.java
//

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

package visad.bio;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.*;
import visad.util.*;

/**
 * BioVisAD is a multi-purpose biological analysis tool.
 *
 * It provides multi-dimensional visualization of biological data,
 * and supports distance measurement between points in a field or
 * stack of fields.
 */
public class BioVisAD extends GUIFrame implements ChangeListener {

  // -- CONSTANTS --

  /** Application title. */
  private static final String TITLE = "BioVisAD";


  // -- DIRECTIVES --

  /** Directs MathType.guessMaps to map index to Animation. */
  static {
    MathType.addTimeAlias("index");
  }


  // -- PACKAGE-WIDE BIO-VISAD OBJECTS --

  /** Matrix of measurements. */
  MeasureMatrix matrix;

  /** Widget for stepping through the image stack. */
  ImageStackWidget vert;

  /** Widget for stepping through data from the series of files. */
  FileSeriesWidget horiz;

  /** VisAD 2-D display. */
  DisplayImpl display2;

  /** VisAD 3-D display. */
  DisplayImpl display3;

  /** Tool panel for adjusting viewing parameters. */
  ViewToolPanel toolView;

  /** Tool panel for performing measurement operations. */
  MeasureToolPanel toolMeasure;

  /** Tool panel for performing rendering operations. */
  RenderToolPanel toolRender;


  // -- GUI COMPONENTS --

  /** Series chooser for loading a series of data files. */
  private SeriesChooser seriesBox = new SeriesChooser();


  // -- OTHER FIELDS --

  /** Prefix of current data series. */
  private String prefix;


  // -- CONSTRUCTORS --

  /** Constructs a new instance of BioVisAD (2-D only). */
  public BioVisAD() throws VisADException, RemoteException { this(false); }
  
  /** Constructs a new instance of BioVisAD. */
  public BioVisAD(boolean threeD) throws VisADException, RemoteException {
    super(true);
    setTitle(TITLE);

    // menu bar
    addMenuItem("File", "Open...", "fileOpen", 'o');
    addMenuSeparator("File");
    addMenuItem("File", "Exit", "fileExit", 'x');

    // lay out components
    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    setContentPane(pane);

    // main display
    display2 = null;
    try {
      display2 = new DisplayImplJ3D("display2",
        new TwoDDisplayRendererJ3D());
    }
    catch (Throwable t) { threeD = false; }
    if (display2 == null) display2 = new DisplayImplJ2D("display2");
    pane.add(display2.getComponent(), BorderLayout.CENTER);

    // vertical slider
    vert = new ImageStackWidget(this, false);
    vert.setAlignmentY(ImageStackWidget.TOP_ALIGNMENT);
    pane.add(vert, BorderLayout.WEST);

    // horizontal slider
    horiz = new FileSeriesWidget(this, true);
    horiz.addChangeListener(this);
    pane.add(horiz, BorderLayout.SOUTH);

    // tool panels
    JTabbedPane tabs = new JTabbedPane();
    pane.add(tabs, BorderLayout.EAST);

    // viewing tool panel
    toolView = new ViewToolPanel(this);
    tabs.addTab("View", toolView);

    // measurement tool panel
    toolMeasure = new MeasureToolPanel(this);
    tabs.addTab("Measure", toolMeasure);

    // rendering tool panel
    toolRender = new RenderToolPanel(this);
    tabs.addTab("Render", toolRender);

    // 3-D display frame
    if (threeD) {
      JFrame frame = new JFrame("BioVisAD - Image stack");
      JPanel fpane = new JPanel();
      fpane.setLayout(new BorderLayout());
      frame.setContentPane(fpane);

      // main 3-D display
      display3 = new DisplayImplJ3D("display3");
      fpane.add(display3.getComponent(), BorderLayout.CENTER);
      frame.pack();
      frame.show();
    }
  }

  /** Loads a series of datasets specified by the user. */
  public void fileOpen() {
    final JFrame frame = this;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // get file series from file dialog
        if (seriesBox.showDialog(frame) != SeriesChooser.APPROVE_OPTION) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }

        // load first file in series
        File[] f = seriesBox.getSeries();
        prefix = seriesBox.getPrefix();
        if (f == null || f.length < 1) {
          JOptionPane.showMessageDialog(frame,
            "Invalid series", "Cannot load series",
            JOptionPane.ERROR_MESSAGE);
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        horiz.setSeries(f);
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /** Exits the application. */
  public void fileExit() { System.exit(0); }

  /** Listens for file series widget changes. */
  public void stateChanged(ChangeEvent e) {
    int max = horiz.getMaximum();
    int cur = horiz.getValue();
    setTitle(TITLE + " - " + prefix + " (" + cur + "/" + max + ")");
  }

  /** Launches the BioVisAD GUI. */
  public static void main(String[] args) throws Exception {
    boolean threeD = args.length > 0 && args[0].equalsIgnoreCase("-3d");
    BioVisAD mf = new BioVisAD(threeD);
    mf.pack();
    mf.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    Util.centerWindow(mf);
    mf.show();
  }

}
