//
// FileSeriesWidget.java
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

import java.io.File;
import java.rmi.RemoteException;
import java.awt.Cursor;
import javax.swing.*;
import visad.*;

/**
 * FileSeriesWidget is a GUI component for stepping through data
 * from a series of files.
 */
public class FileSeriesWidget extends BioStepWidget {

  // -- FIELDS --

  private File[] files;
  private int curFile;


  // -- CONSTRUCTOR --

  /** Constructs a new FileSeriesWidget. */
  public FileSeriesWidget(BioVisAD biovis) {
    super(biovis, true);
  }


  // -- API METHODS --

  /** Links the FileSeriesWidget with the given series of files. */
  public void setSeries(File[] files) {
    this.files = files;
    loadFile(true);
    updateSlider();
  }

  /** Updates the current file of the image series. */
  public void updateStep() {
    if (files != null && curFile != cur - 1 && !step.getValueIsAdjusting()) {
      curFile = cur - 1;
      loadFile(false);
      Measurement[] m = bio.mm.lists[curFile].getMeasurements();
      bio.mm.pool2.set(m);
      bio.mm.pool3.set(m);
    }
  }


  // -- HELPER METHODS --

  private void updateSlider() {
    int max = 1;
    if (files == null) {
      bio.toolMeasure.setEnabled(false);
      setEnabled(false);
    }
    else {
      bio.toolMeasure.setEnabled(true);
      setEnabled(true);
      max = files.length;
      curFile = 0;
    }
    setBounds(1, max, 1);
  }

  private void loadFile(boolean initialize) {
    bio.setWaitCursor(true);
    try {
      if (initialize) bio.sm.init(files, 0);
      else {
        boolean success = bio.sm.setData(files[curFile]);
        if (!success) {
          bio.setWaitCursor(false);
          JOptionPane.showMessageDialog(this,
            files[curFile].getName() + " does not contain an image stack",
            "Cannot load file", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    bio.setWaitCursor(false);
  }

}
