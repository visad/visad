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
import java.io.File;
import java.rmi.RemoteException;
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
   * File chooser for loading and saving data. This variable is static so
   * that the directory is remembered between each load or save command.
   */
  private static JFileChooser fileBox = Util.getVisADFileChooser();

  /** Image stack measurement object. */
  private ImageStackMeasure ism;

  /** VisAD Display. */
  private DisplayImpl display;

  /** Widget for stepping through the time stack. */
  private StepWidget sw;

  /** Frame for step widget. */
  private JFrame sf;

  /** Content pane for step widget. */
  private JPanel spane;

  /** Constructs a measurement object to match the given field. */
  public MeasureFrame() throws VisADException, RemoteException {
    super(true);
    setTitle("BioVisAD Measurement Tool");
    JPanel pane = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    try {
      display = new DisplayImplJ3D("display", new TwoDDisplayRendererJ3D());
    }
    catch (Throwable t) {
      display = new DisplayImplJ2D("display");
    }
    pane.add(display.getComponent());
    addMenuItem("File", "Open...", "fileOpen", 'o');
    addMenuItem("File", "Exit", "fileExit", 'x');
    addMenuItem("Measure", "Restore...", "measureRestore", 'r');
    addMenuItem("Measure", "Save...", "measureSave", 's');
    addMenuSeparator("Measure");
    addMenuItem("Measure", "Show controls", "measureShow", 'c');
    sf = new JFrame("Step controls");
    spane = new JPanel();
    sf.setContentPane(spane);
    spane.setLayout(new BoxLayout(spane, BoxLayout.Y_AXIS));
  }

  /** Loads a dataset. */
  public void fileOpen() {
    // get file name from file dialog
    fileBox.setDialogType(JFileChooser.OPEN_DIALOG);
    if (fileBox.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

    // make sure file exists
    File f = fileBox.getSelectedFile();
    if (!f.exists()) {
      JOptionPane.showMessageDialog(this, f.getName() + " does not exist",
        "Cannot load file", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      // load data
      DefaultFamily loader = new DefaultFamily("loader");
      Data data = loader.open(f.getPath());
      // CTR: need to search data for valid field subsection, but until then...
      FieldImpl field = (FieldImpl) data; // CTR silly hack :-P

      // clear old display
      display.removeAllReferences();
      display.clearMaps();

      // set up mappings
      ScalarMap animMap = null;
      ScalarMap[] maps = field.getType().guessMaps(true);
      for (int i=0; i<maps.length; i++) {
        ScalarMap smap = maps[i];
        display.addMap(smap);
        if (Display.Animation.equals(smap.getDisplayScalar())) animMap = smap;
      }
      DataReferenceImpl ref = new DataReferenceImpl("ref");
      ref.setData(field);
      display.addReference(ref);
      ism = new ImageStackMeasure(field);
      ism.setDisplay(display);
      if (animMap != null) {
        spane.removeAll();
        sw = new StepWidget(animMap);
        spane.add(sw);
        sf.pack();
        sf.show();
      }
    }
    catch (Throwable t) { t.printStackTrace(); }
  }

  /** Exits the application. */
  public void fileExit() {
    System.exit(0);
  }

  /** Restores a saved set of measurements. */
  public void measureRestore() {
    // CTR
  }

  /** Saves a set of measurements. */
  public void measureSave() {
    // CTR
  }

  /** Redisplays the step controls. */
  public void measureShow() {
    sf.show();
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
