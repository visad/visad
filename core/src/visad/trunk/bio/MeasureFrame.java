//
// MeasureFrame.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.data.DefaultFamily;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.*;
import visad.util.*;

/**
 * MeasureFrame is a class for measuring the
 * distance between points in a field.
 */
public class MeasureFrame extends GUIFrame implements ChangeListener {

  /** Application title. */
  private static final String TITLE = "BioVisAD Measurement Tool";

  /**
   * File chooser for loading and saving data.
   * Static so that the directory is remembered between each load command.
   */
  private JFileChooser fileBox = Util.getVisADFileChooser();

  /** Series chooser for loading a series of data files. */
  private SeriesChooser seriesBox = new SeriesChooser();

  static {
    MathType.addTimeAlias("index");
  }

  /** Matrix of measurements. */
  private MeasureMatrix matrix;

  /** VisAD Display. */
  private DisplayImpl display;

  /** Widget for stepping through the image stack. */
  private ImageStackWidget vertWidget;

  /** Widget for stepping through data from the series of files. */
  private FileSeriesWidget horizWidget;

  /** Toolbar for performing various operations. */
  private MeasureToolbar toolbar;

  /** Prefix of current data series. */
  private String prefix;

  /** Constructs a measurement object to match the given field. */
  public MeasureFrame() throws VisADException, RemoteException { this(false); }
  
  /** Constructs a measurement object to match the given field. */
  public MeasureFrame(boolean twoD) throws VisADException, RemoteException {
    super(true);
    setTitle(TITLE);
    addMenuItem("File", "Open...", "fileOpen", 'o');
    addMenuSeparator("File");
    addMenuItem("File", "Restore lines...", "fileRestoreLines", 'r');
    addMenuItem("File", "Save lines...", "fileSaveLines", 's');
    addMenuSeparator("File");
    addMenuItem("File", "Exit", "fileExit", 'x');

    // lay out components
    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    setContentPane(pane);

    // main display
    display = null;
    if (!twoD) {
      try {
        display = new DisplayImplJ3D("display", new TwoDDisplayRendererJ3D());
      }
      catch (Throwable t) { }
    }
    if (display == null) display = new DisplayImplJ2D("display");
    pane.add(display.getComponent(), BorderLayout.CENTER);

    // vertical slider
    vertWidget = new ImageStackWidget(false);
    vertWidget.setAlignmentY(ImageStackWidget.TOP_ALIGNMENT);
    pane.add(vertWidget, BorderLayout.WEST);

    // horizontal slider
    horizWidget = new FileSeriesWidget(true);
    horizWidget.setDisplay(display);
    horizWidget.setWidget(vertWidget);
    horizWidget.addChangeListener(this);
    pane.add(horizWidget, BorderLayout.SOUTH);

    // custom toolbar
    toolbar = new MeasureToolbar(horizWidget, vertWidget);
    horizWidget.setToolbar(toolbar);
    pane.add(toolbar, BorderLayout.EAST);
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
        horizWidget.setSeries(f);
        matrix = horizWidget.getMatrix();
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /** Restores a saved set of measurements. */
  public void fileRestoreLines() {
    final JFrame frame = this;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(frame, "Temporarily disabled.",
          "BioVisAD", JOptionPane.WARNING_MESSAGE);
      }
    });
    /* CTR: TODO: restore lines
    final JFrame frame = this;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // get file name from file dialog
        fileBox.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fileBox.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
      
        // make sure file exists
        File f = fileBox.getSelectedFile();
        if (!f.exists()) {
          JOptionPane.showMessageDialog(frame,
            f.getName() + " does not exist", "Cannot load file",
            JOptionPane.ERROR_MESSAGE);
          setCursor(Cursor.getDefaultCursor());
          return;
        }
      
        try {
          MeasureDataFile mdf = new MeasureDataFile(f);
          MeasureMatrix mm = mdf.readMatrix();
          //
        }
        catch (IOException exc) { exc.printStackTrace(); }
        setCursor(Cursor.getDefaultCursor());
      }
    });
    */
  }

  /** Saves a set of measurements. */
  public void fileSaveLines() {
    final JFrame frame = this;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // get file name from file dialog
        fileBox.setDialogType(JFileChooser.SAVE_DIALOG);
        if (fileBox.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
    
        // save measurements
        File f = fileBox.getSelectedFile();
        try {
          MeasureDataFile mdf = new MeasureDataFile(f);
          mdf.writeMatrix(matrix);
        }
        catch (IOException exc) { exc.printStackTrace(); }
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /** Exits the application. */
  public void fileExit() {
    System.exit(0);
  }

  /** Listens for file series widget changes. */
  public void stateChanged(ChangeEvent e) {
    int max = horizWidget.getMaximum();
    int cur = horizWidget.getValue();
    setTitle(TITLE + " - " + prefix + " (" + cur + "/" + max + ")");
  }

  /** Launches the MeasureFrame GUI. */
  public static void main(String[] args) throws Exception {
    boolean twoD = args.length > 0 && args[0].equalsIgnoreCase("-2d");
    MeasureFrame mf = new MeasureFrame(twoD);
    mf.pack();
    mf.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        // CTR: maybe ask if user wants to save measurements?
        System.exit(0);
      }
    });
    Util.centerWindow(mf);
    mf.show();
  }

}
