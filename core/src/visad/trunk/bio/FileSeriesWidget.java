//
// FileSeriesWidget.java
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

import java.io.File;
import java.rmi.RemoteException;
import javax.swing.JOptionPane;
import visad.*;
import visad.data.DefaultFamily;

/**
 * FileSeriesWidget is a GUI component for stepping through data
 * from a series of files.
 */
public class FileSeriesWidget extends StepWidget {

  private final DefaultFamily loader = new DefaultFamily("loader");
  private DataReferenceImpl ref;
  private File[] files;
  private int curFile;
  private DisplayImpl display;
  private ImageStackWidget isw;
  private ScalarMap animMap;

  /** Constructs a new FileSeriesWidget. */
  public FileSeriesWidget(boolean horizontal) {
    super(horizontal);
    try {
      ref = new DataReferenceImpl("ref");
    }
    catch (VisADException exc) { exc.printStackTrace(); }
  }

  /** Links the FileSeriesWidget with the given series of files. */
  public void setSeries(File[] files) {
    this.files = files;
    loadFile(true);
    updateSlider();
  }

  /** Links the FileSeriesWidget with the given display. */
  public void setDisplay(DisplayImpl display) { this.display = display; }

  /** Links the FileSeriesWidget with the given ImageStackWidget. */
  public void setWidget(ImageStackWidget widget) { isw = widget; }

  /** Updates the current file of the image series. */
  public void updateStep() {
    if (files != null && cur != curFile) {
      curFile = cur;
      loadFile(false);
    }
  }

  private void updateSlider() {
    int max = 1;
    if (files == null) setEnabled(false);
    else {
      setEnabled(true);
      max = files.length;
      curFile = 1;
    }
    setBounds(1, max, 1);
  }

  private void loadFile(boolean doMaps) {
    File f = files[curFile];
    Data data = null;
    try {
      data = loader.open(f.getPath());
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    if (data == null) {
      JOptionPane.showMessageDialog(this,
        "Cannot import data from " + f.getName(),
        "Cannot load file", JOptionPane.ERROR_MESSAGE);
      return;
    }
    FieldImpl field = null;
    if (data instanceof FieldImpl) field = (FieldImpl) data;
    else if (data instanceof Tuple) {
      Tuple tuple = (Tuple) data;
      int len = tuple.getDimension();
      for (int i=0; i<len; i++) {
        try {
          Data d = tuple.getComponent(i);
          if (d instanceof FieldImpl) {
            field = (FieldImpl) d;
            break;
          }
        }
        catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
        catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
      }
    }
    if (field == null) {
      JOptionPane.showMessageDialog(this,
        f.getName() + " does not contain an image stack",
        "Cannot load file", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (doMaps && display != null) {
      try {
        // clear old display
        display.removeAllReferences();
        display.clearMaps();
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }

      // set up mappings
      animMap = null;
      ScalarMap[] maps = field.getType().guessMaps(false);
      for (int i=0; i<maps.length; i++) {
        ScalarMap smap = maps[i];
        try {
          display.addMap(smap);
        }
        catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
        catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
        if (Display.Animation.equals(smap.getDisplayScalar())) {
          animMap = smap;
        }
      }
      try {
        display.addReference(ref);
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    }

    try {
      ref.setData(field);
      ImageStackMeasure ism = new ImageStackMeasure(field);
      if (display != null) ism.setDisplay(display);
      if (isw != null && animMap != null) isw.setMap(animMap);
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
  }

}
