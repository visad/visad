//
// MeasureManager.java
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

import java.io.*;
import java.rmi.RemoteException;
import java.util.Vector;
import javax.swing.*;
import visad.VisADException;
import visad.util.Util;

/** MeasureManager is the class encapsulating VisBio's measurement logic. */
public class MeasureManager {

  // -- MEASUREMENT INFO --

  /** List of measurements for each timestep. */
  MeasureList[] lists;

  /** Measurement pool for 2-D display. */
  MeasurePool pool2;

  /** Measurement pool for 3-D display. */
  MeasurePool pool3;

  /** First free id number for measurement groups. */
  int maxId = 0;

  /** Measurement group list. */
  Vector groups = new Vector();

  /** Whether measurements have changed since last save. */
  boolean changed = false;


  // -- OTHER FIELDS --

  /** VisBio frame. */
  private VisBio bio;

  /** File chooser for loading and saving data. */
  private JFileChooser fileBox = new JFileChooser();


  // -- CONSTRUCTORS --

  /** Constructs a measurement manager. */
  public MeasureManager(VisBio biovis)
    throws VisADException, RemoteException
  {
    bio = biovis;

    // 2-D and 3-D measurement pools
    pool2 = new MeasurePool(bio, bio.display2, 2);
    if (bio.display3 != null) pool3 = new MeasurePool(bio, bio.display3, 3);
  }


  // -- API METHODS --

  /** Initializes the measurement lists. */
  public void initLists(int timesteps) throws VisADException, RemoteException {
    lists = new MeasureList[timesteps];
    for (int i=0; i<timesteps; i++) lists[i] = new MeasureList(bio);
    pool2.set(lists[0]);
    if (pool3 != null) pool3.set(lists[0]);
  }

  /** Clears all measurements from all image slices. */
  public void clear() {
    for (int i=0; i<lists.length; i++) lists[i].removeAll();
  }

  /** Gets measurement list for current index. */
  public MeasureList getList() { return lists[bio.sm.getIndex()]; }

  /**
   * Checks whether measurements have been saved,
   * and if not, prompts the user to save them.
   */
  public void checkSave() {
    boolean hasMeasure = false;
    if (lists != null) {
      for (int i=0; i<lists.length; i++) {
        if (lists[i].hasMeasurements()) {
          hasMeasure = true;
          break;
        }
      }
    }
    if (!changed || !hasMeasure) return;
    int ans = JOptionPane.showConfirmDialog(bio,
      "Save measurements?", "VisBio",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ans != JOptionPane.YES_OPTION) return;
    saveMeasurements();
  }

  /** Restores a saved set of measurements. */
  public void restoreMeasurements() {
    bio.setWaitCursor(true);
    // get file name from file dialog
    fileBox.setDialogType(JFileChooser.OPEN_DIALOG);
    if (fileBox.showOpenDialog(bio) != JFileChooser.APPROVE_OPTION) {
      bio.setWaitCursor(false);
      return;
    }

    // make sure file exists
    File f = fileBox.getSelectedFile();
    if (!f.exists()) {
      bio.setWaitCursor(false);
      JOptionPane.showMessageDialog(bio,
        f.getName() + " does not exist", "Cannot load file",
        JOptionPane.ERROR_MESSAGE);
      return;
    }

    // restore measurements
    try { new MeasureDataFile(bio, f).read(); }
    catch (IOException exc) { exc.printStackTrace(); }
    catch (VisADException exc) { exc.printStackTrace(); }
    bio.setWaitCursor(false);
  }

  /** Saves a set of measurements. */
  public void saveMeasurements() {
    bio.setWaitCursor(true);
    // get file name from file dialog
    fileBox.setDialogType(JFileChooser.SAVE_DIALOG);
    if (fileBox.showSaveDialog(bio) != JFileChooser.APPROVE_OPTION) {
      bio.setWaitCursor(false);
      return;
    }

    // save measurements
    File f = fileBox.getSelectedFile();
    try {
      MeasureDataFile mdf = new MeasureDataFile(bio, f);
      if (bio.toolAlign.getUseMicrons()) {
        double mw = bio.toolAlign.getMicronWidth();
        double mh = bio.toolAlign.getMicronHeight();
        double mx = mw / bio.sm.res_x;
        double my = mh / bio.sm.res_y;
        double sd = bio.toolAlign.getSliceDistance();
        mdf.write(mx, my, sd);
      }
      else mdf.write();
    }
    catch (IOException exc) { exc.printStackTrace(); }
    changed = false;
    bio.setWaitCursor(false);
  }


  // -- INTERNAL API METHODS --

  /** Writes the current program state to the given output stream. */
  void saveState(PrintWriter fout) throws IOException, VisADException {
    // CTR - TODO - MeasureManager.saveState
  }

  /** Restores the current program state from the given input stream. */
  void restoreState(BufferedReader fin) throws IOException, VisADException {
    // CTR - TODO - MeasureManager.restoreState
  }

}
