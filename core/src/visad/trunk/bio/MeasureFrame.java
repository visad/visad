//
// MeasureFrame.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
import visad.*;
import visad.data.DefaultFamily;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.*;
import visad.util.*;

/**
 * MeasureFrame is a class for measuring the
 * distance between points in a field.
 */
public class MeasureFrame extends GUIFrame {

  /**
   * File chooser for loading and saving data.
   * Static so that the directory is remembered between each load command.
   */
  private static JFileChooser fileBox = Util.getVisADFileChooser();

  /** Series chooser for loading a series of data files. */
  private static SeriesChooser seriesBox = new SeriesChooser();

  static {
    MathType.addTimeAlias("index");
  }

  /** Image stack measurement object. */
  private ImageStackMeasure ism;

  /** VisAD Display. */
  private DisplayImpl display;

  /** Widget for stepping through the time stack. */
  private StepWidget sw;

  /** Synchronization object. */
  private Object lock = new Object();

  /** Constructs a measurement object to match the given field. */
  public MeasureFrame() throws VisADException, RemoteException {
    super(true);
    setTitle("BioVisAD Measurement Tool");
    addMenuItem("File", "Open...", "fileOpen", 'o');
    addMenuItem("File", "Open series...", "fileOpenSeries", 's');
    addMenuItem("File", "Exit", "fileExit", 'x');
    addMenuItem("Measure", "Restore...", "measureRestore", 'r');
    addMenuItem("Measure", "Save...", "measureSave", 's');

    // lay out components
    JPanel pane = new JPanel();
    JPanel top = new JPanel();
    JPanel bottom = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    /*
    top.setAlignmentY(JPanel.TOP_ALIGNMENT);
    bottom.setAlignmentY(JPanel.TOP_ALIGNMENT);
    */
    pane.add(top);
    pane.add(bottom);
    sw = new StepWidget();
    sw.setAlignmentY(StepWidget.TOP_ALIGNMENT);
    top.add(sw);
    try {
      display = new DisplayImplJ3D("display", new TwoDDisplayRendererJ3D());
    }
    catch (Throwable t) {
      display = new DisplayImplJ2D("display");
    }
    top.add(display.getComponent());
    // CTR: TODO: bottom.add(cool horiz slider thingie)
  }

  /** Loads the given dataset. */
  public void loadFile(File f) {
    // make sure file exists
    if (f == null) {
      JOptionPane.showMessageDialog(this,
        "Invalid file", "Cannot load file",
        JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (!f.exists()) {
      JOptionPane.showMessageDialog(this,
        f.getName() + " does not exist", "Cannot load file",
        JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      // load data
      DefaultFamily loader = new DefaultFamily("loader");
      Data data = loader.open(f.getPath());
      FieldImpl field = null;
      if (data instanceof FieldImpl) field = (FieldImpl) data;
      else if (data instanceof Tuple) {
        Tuple tuple = (Tuple) data;
        int len = tuple.getDimension();
        for (int i=0; i<len; i++) {
          Data d = tuple.getComponent(i);
          if (d instanceof FieldImpl) {
            field = (FieldImpl) d;
            break;
          }
        }
      }
      if (field == null) {
        JOptionPane.showMessageDialog(this,
          f.getName() + " does not contain an image stack",
          "Cannot load file", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // clear old display
      display.removeAllReferences();
      display.clearMaps();

      // set up mappings
      ScalarMap animMap = null;
      ScalarMap[] maps = field.getType().guessMaps(false);
      for (int i=0; i<maps.length; i++) {
        ScalarMap smap = maps[i];
        display.addMap(smap);
        if (Display.Animation.equals(smap.getDisplayScalar())) {
          animMap = smap;
        }
      }
      DataReferenceImpl ref = new DataReferenceImpl("ref");
      ref.setData(field);
      display.addReference(ref);
      ism = new ImageStackMeasure(field);
      ism.setDisplay(display);
      if (animMap != null) sw.setMap(animMap);
    }
    catch (Throwable t) { t.printStackTrace(); }
  }

  /** Loads a dataset specified by the user. */
  public void fileOpen() {
    final JFrame frame = this;
    Thread t = new Thread(new Runnable() {
      public void run() {
        synchronized (lock) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          // get file name from file dialog
          fileBox.setDialogType(JFileChooser.OPEN_DIALOG);
          if (fileBox.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            setCursor(Cursor.getDefaultCursor());
            return;
          }
          loadFile(fileBox.getSelectedFile());
          setCursor(Cursor.getDefaultCursor());
        }
      }
    });
    t.start();
  }

  /** Loads a series of datasets specified by the user. */
  public void fileOpenSeries() {
    final JFrame frame = this;
    Thread t = new Thread(new Runnable() {
      public void run() {
        synchronized (lock) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          // get file series from file dialog
          if (seriesBox.showDialog(frame) != SeriesChooser.APPROVE_OPTION) {
            setCursor(Cursor.getDefaultCursor());
            return;
          }

          // load first file in series
          File[] f = seriesBox.getFileSeries();
          if (f == null || f.length < 1) {
            JOptionPane.showMessageDialog(frame,
              "Invalid series", "Cannot load series",
              JOptionPane.ERROR_MESSAGE);
            setCursor(Cursor.getDefaultCursor());
            return;
          }
          loadFile(f[0]);
          // CTR: TODO: finish this method... load up widgets and stuff
          setCursor(Cursor.getDefaultCursor());
        }
      }
    });
    t.start();
  }

  /** Exits the application. */
  public void fileExit() {
    System.exit(0);
  }

  /** Restores a saved set of measurements. */
  public void measureRestore() {
    final JFrame frame = this;
    Thread t = new Thread(new Runnable() {
      public void run() {
        synchronized (lock) {
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
            // load measurement data
            BufferedReader fin = new BufferedReader(new FileReader(f));
            int numSlices = ism.getSliceCount();
            for (int i=0; i<numSlices; i++) {
              String line = fin.readLine();
              if (line == null) {
                JOptionPane.showMessageDialog(frame,
                  f.getName() + ": premature end of file", "Cannot load file",
                  JOptionPane.ERROR_MESSAGE);
                fin.close();
                setCursor(Cursor.getDefaultCursor());
                return;
              }
              StringTokenizer st = new StringTokenizer(line);
              if (st.countTokens() < 4) {
                JOptionPane.showMessageDialog(frame,
                  f.getName() + ": invalid data format", "Cannot load file",
                  JOptionPane.ERROR_MESSAGE);
                fin.close();
                setCursor(Cursor.getDefaultCursor());
                return;
              }
              double[][] values = new double[2][2];
              for (int j=0; j<4; j++) {
                values[j % 2][j / 2] = Double.parseDouble(st.nextToken());
              }
              ism.setValues(i, values);
            }
            fin.close();
          }
          catch (IOException exc) { exc.printStackTrace(); }
          setCursor(Cursor.getDefaultCursor());
        }
      }
    });
    t.start();
  }

  /** Saves a set of measurements. */
  public void measureSave() {
    final JFrame frame = this;
    Thread t = new Thread(new Runnable() {
      public void run() {
        synchronized (lock) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          // get file name from file dialog
          fileBox.setDialogType(JFileChooser.SAVE_DIALOG);
          if (fileBox.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            setCursor(Cursor.getDefaultCursor());
            return;
          }
      
          // save measurements
          File f = fileBox.getSelectedFile();
          String save = ism.getDistanceString();
          try {
            FileWriter fout = new FileWriter(f);
            fout.write(save, 0, save.length());
            fout.close();
          }
          catch (IOException exc) { exc.printStackTrace(); }
          setCursor(Cursor.getDefaultCursor());
        }
      }
    });
    t.start();
  }

  /** Launches the MeasureFrame GUI. */
  public static void main(String[] args) throws Exception {
    MeasureFrame mf = new MeasureFrame();
    mf.pack();
    mf.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        // CTR: maybe ask if user wants to save measurements?
        System.exit(0);
      }
    });
    mf.show();
  }

}
