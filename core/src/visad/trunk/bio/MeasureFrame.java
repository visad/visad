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
import javax.swing.*;
import javax.swing.event.*;
import visad.*;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.*;
import visad.util.*;

/**
 * MeasureFrame is a class for measuring distances
 * between points in a field or stack of fields.
 */
public class MeasureFrame extends GUIFrame implements ChangeListener {

  /** Application title. */
  private static final String TITLE = "BioVisAD Measurement Tool";

  /** File chooser for loading and saving data. */
  private JFileChooser fileBox = Util.getVisADFileChooser();

  /** Series chooser for loading a series of data files. */
  private SeriesChooser seriesBox = new SeriesChooser();

  static {
    MathType.addTimeAlias("index");
  }

  /** Matrix of measurements. */
  private MeasureMatrix matrix;

  /** VisAD 2-D display. */
  private DisplayImpl display2;

  /** VisAD 3-D display. */
  private DisplayImpl display3;

  /** Widget for stepping through the image stack. */
  private ImageStackWidget vertWidget;

  /** Widget for stepping through data from the series of files. */
  private FileSeriesWidget horizWidget;

  /** Toolbar for performing various operations. */
  private MeasureToolbar toolbar;

  /** Prefix of current data series. */
  private String prefix;

  /** Constructs a new instance of the measurement tool. */
  public MeasureFrame() throws VisADException, RemoteException { this(false); }
  
  /** Constructs a new instance of the measurement tool. */
  public MeasureFrame(boolean twoD) throws VisADException, RemoteException {
    super(true);
    setTitle(TITLE);
    addMenuItem("File", "Open...", "fileOpen", 'o');
    addMenuSeparator("File");
    addMenuItem("File", "Restore lines (pixels)...", "fileRestoreLines", 'r');
    addMenuItem("File", "Save lines (pixels)...", "fileSaveLines", 's');
    addMenuItem("File", "Restore lines (microns)...",
      "fileRestoreMicrons", 'e');
    addMenuItem("File", "Save lines (microns)...", "fileSaveMicrons", 'a');
    addMenuSeparator("File");
    addMenuItem("File", "Exit", "fileExit", 'x');

    // lay out components
    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    setContentPane(pane);

    // main display
    display2 = null;
    if (!twoD) {
      try {
        display2 = new DisplayImplJ3D("display2",
          new TwoDDisplayRendererJ3D());
      }
      catch (Throwable t) { twoD = true; }
    }
    if (display2 == null) display2 = new DisplayImplJ2D("display2");
    pane.add(display2.getComponent(), BorderLayout.CENTER);

    // vertical slider
    vertWidget = new ImageStackWidget(false);
    vertWidget.setAlignmentY(ImageStackWidget.TOP_ALIGNMENT);
    pane.add(vertWidget, BorderLayout.WEST);

    // horizontal slider
    horizWidget = new FileSeriesWidget(true);
    horizWidget.setDisplay(display2);
    horizWidget.setWidget(vertWidget);
    horizWidget.addChangeListener(this);
    pane.add(horizWidget, BorderLayout.SOUTH);

    // custom toolbar
    toolbar = new MeasureToolbar(this, horizWidget, vertWidget);
    horizWidget.setToolbar(toolbar);
    pane.add(toolbar, BorderLayout.EAST);

    // 3-D display frame
    if (!twoD) {
      JFrame frame = new JFrame("BioVisAD - Image stack");
      JPanel fpane = new JPanel();
      fpane.setLayout(new BorderLayout());
      frame.setContentPane(fpane);

      // main 3-D display
      display3 = new DisplayImplJ3D("display3");
      fpane.add(display3.getComponent(), BorderLayout.CENTER);
      horizWidget.setDisplay3d(display3);
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
        horizWidget.setSeries(f);
        matrix = horizWidget.getMatrix();
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /** Restores a saved set of measurements. */
  public void fileRestore(boolean microns) {
    final JFrame frame = this;
    final boolean fmicrons = microns;
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
      
        // restore measurements
        try {
          MeasureDataFile mdf = new MeasureDataFile(f);
          if (fmicrons) {
            double mpp = toolbar.getMicronsPerPixel();
            double sd = toolbar.getSliceDistance();
            mdf.readMatrix(matrix, mpp, sd);
          }
          else mdf.readMatrix(matrix);
        }
        catch (IOException exc) { exc.printStackTrace(); }
        catch (VisADException exc) { exc.printStackTrace(); }
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /** Saves a set of measurements. */
  public void fileSave(boolean microns) {
    final JFrame frame = this;
    final boolean fmicrons = microns;
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
          if (fmicrons) {
            double mpp = toolbar.getMicronsPerPixel();
            double sd = toolbar.getSliceDistance();
            mdf.writeMatrix(matrix, mpp, sd);
          }
          else mdf.writeMatrix(matrix);
        }
        catch (IOException exc) { exc.printStackTrace(); }
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /** Restores a saved set of measurements (using pixel units). */
  public void fileRestoreLines() { fileRestore(false); }

  /** Saves a set of measurements (using pixel units). */
  public void fileSaveLines() { fileSave(false); }

  /** Restores a saved set of measurements (using micron units). */
  public void fileRestoreMicrons() { fileRestore(true); }

  /** Saves a set of measurements (using micron units). */
  public void fileSaveMicrons() { fileSave(true); }

  /** Exits the application. */
  public void fileExit() { System.exit(0); }

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
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    Util.centerWindow(mf);
    mf.show();
  }

}
