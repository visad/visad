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
public class FileSeriesWidget extends BioStepWidget {

  static final RealType COLOR_TYPE = RealType.getRealType("color");

  private final DefaultFamily loader = new DefaultFamily("loader");
  private DataReferenceImpl ref;
  private File[] files;
  private int curFile;
  private ScalarMap animMap2, xMap2, yMap2;
  private ScalarMap xMap3, yMap3, zMap3, zMap3b;

  /** Constructs a new FileSeriesWidget. */
  public FileSeriesWidget(BioVisAD biovis, boolean horizontal) {
    super(biovis, horizontal);
    try {
      ref = new DataReferenceImpl("ref");
    }
    catch (VisADException exc) { exc.printStackTrace(); }
  }

  /** Links the FileSeriesWidget with the given series of files. */
  public void setSeries(File[] files) {
    this.files = files;
    bio.matrix = new MeasureMatrix(files.length,
      bio.display2, bio.display3, bio.toolMeasure);
    loadFile(true);
    updateSlider();
  }

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

    if (doMaps && bio.display2 != null) {
      try {
        // clear old displays
        bio.display2.removeAllReferences();
        bio.display2.clearMaps();
        if (bio.display3 != null) {
          bio.display3.removeAllReferences();
          bio.display3.clearMaps();
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
          if (bio.display3 != null) {
            try {
              smap3 = zMap3 = new ScalarMap(smap2.getScalar(), Display.ZAxis);
            }
            catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
          }
        }
        else if (Display.XAxis.equals(drt)) {
          xMap2 = smap2;
          if (bio.display3 != null) xMap3 = smap3;
        }
        else if (Display.YAxis.equals(drt)) {
          yMap2 = smap2;
          if (bio.display3 != null) yMap3 = smap3;
        }
        try {
          bio.display2.addMap(smap2);
          if (bio.display3 != null) bio.display3.addMap(smap3);
        }
        catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
        catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
      }

      // add mapping to RGB
      bio.vert.setGrayscale(true); // default to grayscale color mode
      try {
        ScalarMap colorMap = new ScalarMap(COLOR_TYPE, Display.RGB);
        colorMap.setRange(0, 255);
        bio.display2.addMap(colorMap);
        bio.display2.addReference(ref);
        if (bio.display3 != null) {
          bio.display3.addMap((ScalarMap) colorMap.clone());
          zMap3b = new ScalarMap(MeasureMatrix.ZAXIS_TYPE, Display.ZAxis);
          bio.display3.addMap(zMap3b);
          bio.display3.addReference(ref);
        }
      }
      catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
      catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    }

    try {
      ref.setData(field);
      bio.matrix.init(field, new ScalarMap[][] {
        {xMap2, xMap3}, {yMap2, yMap3}, {zMap3, zMap3b}
      });
      bio.matrix.setIndex(curFile);
      if (bio.vert != null && animMap2 != null) bio.vert.setMap(animMap2);
    }
    catch (VisADException exc) { if (DEBUG) exc.printStackTrace(); }
    catch (RemoteException exc) { if (DEBUG) exc.printStackTrace(); }
    if (pane != null) pane.setCursor(Cursor.getDefaultCursor());
  }

}
