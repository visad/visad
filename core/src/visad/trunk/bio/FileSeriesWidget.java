//
// FileSeriesWidget.java
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

import java.io.File;
import java.rmi.RemoteException;
import java.awt.Cursor;
import javax.swing.*;
import visad.*;
import visad.data.DefaultFamily;

/**
 * FileSeriesWidget is a GUI component for stepping through data
 * from a series of files.
 */
public class FileSeriesWidget extends StepWidget {

  static final RealType COLOR_TYPE = RealType.getRealType("color");

  private final DefaultFamily loader = new DefaultFamily("loader");
  private DataReferenceImpl ref;
  private File[] files;
  private int curFile;
  private ImageStackWidget isw;
  private MeasureToolbar toolbar;
  private ScalarMap animMap;
  private ScalarMap xMap;
  private ScalarMap yMap;
  private MeasureMatrix matrix;
  private DisplayImpl display;

  /** Constructs a new FileSeriesWidget. */
  public FileSeriesWidget(boolean horizontal) {
    super(horizontal);
    try {
      ref = new DataReferenceImpl("ref");
    }
    catch (VisADException exc) { exc.printStackTrace(); }
  }

  /** Gets the matrix of measurements linked to the widget. */
  public MeasureMatrix getMatrix() { return matrix; }

  /** Links the FileSeriesWidget with the given series of files. */
  public void setSeries(File[] files) {
    this.files = files;
    matrix = new MeasureMatrix(files.length, display, toolbar);
    isw.setMatrix(matrix);
    loadFile(true);
    updateSlider();
  }

  /** Links the FileSeriesWidget with the given display. */
  public void setDisplay(DisplayImpl display) { this.display = display; }

  /** Links the FileSeriesWidget with the given ImageStackWidget. */
  public void setWidget(ImageStackWidget widget) { isw = widget; }

  /** Links the FileSeriesWidget with the given MeasureToolbar. */
  public void setToolbar(MeasureToolbar toolbar) { this.toolbar = toolbar; }

  /** Updates the current file of the image series. */
  public void updateStep() {
    if (files != null && curFile != cur - 1 && !step.getValueIsAdjusting()) {
      curFile = cur - 1;
      loadFile(false);
    }
  }

  private void updateSlider() {
    int max = 1;
    if (files == null) {
      toolbar.setEnabled(false);
      setEnabled(false);
    }
    else {
      toolbar.setEnabled(true);
      setEnabled(true);
      max = files.length;
      curFile = 0;
    }
    setBounds(1, max, 1);
  }

  private void loadFile(boolean doMaps) {
    JRootPane pane = getRootPane();
    if (pane != null) {
      pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    File f = files[curFile];
    Data data = null;
    try {
      data = loader.open(f.getPath());
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    if (data == null) {
      if (pane != null) pane.setCursor(Cursor.getDefaultCursor());
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
      if (pane != null) pane.setCursor(Cursor.getDefaultCursor());
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
      xMap = null;
      yMap = null;
      ScalarMap[] maps = field.getType().guessMaps(false);
      for (int i=0; i<maps.length; i++) {
        ScalarMap smap = maps[i];
        try {
          display.addMap(smap);
        }
        catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
        catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
        DisplayRealType drt = smap.getDisplayScalar();
        if (Display.Animation.equals(drt)) animMap = smap;
        else if (Display.XAxis.equals(drt)) xMap = smap;
        else if (Display.YAxis.equals(drt)) yMap = smap;
      }
      isw.setGrayscale(true); // default to grayscale color mode
      try {
        ScalarMap colorMap = new ScalarMap(COLOR_TYPE, Display.RGB);
        display.addMap(colorMap);
        display.addReference(ref);
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    }

    try {
      ref.setData(field);
      matrix.initIndex(curFile, field, new ScalarMap[] {xMap, yMap}, false);
      if (isw != null && animMap != null) isw.setMap(animMap);
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    if (pane != null) pane.setCursor(Cursor.getDefaultCursor());
  }

}
