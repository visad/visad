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
  private ScalarMap animMap2, xMap2, yMap2;
  private ScalarMap xMap3, yMap3, zMap3, zMap3b;
  private MeasureMatrix matrix;
  private DisplayImpl display2;
  private DisplayImpl display3;

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
    matrix = new MeasureMatrix(files.length, display2, display3, toolbar);
    isw.setMatrix(matrix);
    loadFile(true);
    updateSlider();
  }

  /** Links the FileSeriesWidget with the given display. */
  public void setDisplay(DisplayImpl display2) { this.display2 = display2; }

  /** Links the FileSeriesWidget with the given 3-D display. */
  public void setDisplay3d(DisplayImpl display3) { this.display3 = display3; }

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

    if (doMaps && display2 != null) {
      try {
        // clear old displays
        display2.removeAllReferences();
        display2.clearMaps();
        if (display3 != null) {
          display3.removeAllReferences();
          display3.clearMaps();
        }
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }

      // set up mappings
      animMap2 = xMap2 = yMap2 = null;
      xMap3 = yMap3 = zMap3 = null;
      ScalarMap[] maps = field.getType().guessMaps(false);
      for (int i=0; i<maps.length; i++) {
        ScalarMap smap2 = maps[i];
        DisplayRealType drt = smap2.getDisplayScalar();
        boolean anim = Display.Animation.equals(drt);
        ScalarMap smap3 = anim ? null : (ScalarMap) smap2.clone();
        if (anim) {
          animMap2 = smap2;
          if (display3 != null) {
            try {
              smap3 = zMap3 = new ScalarMap(smap2.getScalar(), Display.ZAxis);
            }
            catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
          }
        }
        else if (Display.XAxis.equals(drt)) {
          xMap2 = smap2;
          if (display3 != null) xMap3 = smap3;
        }
        else if (Display.YAxis.equals(drt)) {
          yMap2 = smap2;
          if (display3 != null) yMap3 = smap3;
        }
        try {
          display2.addMap(smap2);
          if (display3 != null) display3.addMap(smap3);
        }
        catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
        catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
      }

      // add mapping to RGB
      isw.setGrayscale(true); // default to grayscale color mode
      try {
        ScalarMap colorMap = new ScalarMap(COLOR_TYPE, Display.RGB);
        colorMap.setRange(0, 255);
        display2.addMap(colorMap);
        display2.addReference(ref);
        if (display3 != null) {
          display3.addMap((ScalarMap) colorMap.clone());
          zMap3b = new ScalarMap(MeasureMatrix.ZAXIS_TYPE, Display.ZAxis);
          display3.addMap(zMap3b);
          display3.addReference(ref);
        }
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    }

    try {
      ref.setData(field);
      matrix.init(field, new ScalarMap[][] {
        {xMap2, xMap3}, {yMap2, yMap3}, {zMap3, zMap3b}
      });
      matrix.setIndex(curFile);
      if (isw != null && animMap2 != null) isw.setMap(animMap2);
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    if (pane != null) pane.setCursor(Cursor.getDefaultCursor());
  }

}
