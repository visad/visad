//
// MeasureMatrix.java
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

import java.rmi.RemoteException;
import visad.*;

/** MeasureMatrix maintains a 2-D matrix of measurements. */
public class MeasureMatrix {

  /** 2-D matrix of measurements. */
  private MeasureList[][] matrix;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Pool of lines. */
  private LinePool pool;

  /** Current matrix index. */
  private int index;

  /** Current matrix slice. */
  private int slice;

  /** Whether matrix has been initialized. */
  private boolean inited = false;

  /** Constructs a list of measurements. */
  public MeasureMatrix(int length,
    DisplayImpl display, MeasureToolbar toolbar)
  {
    matrix = new MeasureList[length][];
    this.display = display;
    pool = new LinePool(display, toolbar, LinePool.MINIMUM_SIZE / 2);
  }

  /** Initializes the measure matrix. */
  public void init(FieldImpl field, ScalarMap[] xyzMaps)
    throws VisADException, RemoteException
  {
    if (inited) return;
    
    // extract needed information from FieldImpl image stack
    FunctionType type = (FunctionType) field.getType();
    RealTupleType domain = type.getDomain();
    if (domain.getDimension() > 1) {
      throw new VisADException("Field not an image stack");
    }
    Set set = field.getDomainSet();
    if (!(set instanceof GriddedSet)) {
      throw new VisADException("Image stack not ordered");
    }
    GriddedSet gset = (GriddedSet) set;
    int[] lengths = gset.getLengths();
    int numSlices = lengths[0];
    for (int i=0; i<numSlices; i++) {
      Data data = field.getSample(i);
      if (!(data instanceof FieldImpl)) {
        throw new VisADException("Data #" + i + " not a field");
      }
    }

    // extract needed information from single FieldImpl image
    FieldImpl image = (FieldImpl) field.getSample(0);
    type = (FunctionType) image.getType();
    domain = type.getDomain();
    set = image.getDomainSet();
    float[][] samples = set.getSamples(false);
    int len = domain.getDimension();
    Real[] p1r = new Real[len];
    Real[] p2r = new Real[len];
    Real[] pxr = new Real[len];
    for (int i=0; i<len; i++) {
      RealType rt = (RealType) domain.getComponent(i);
      float s1 = samples[i][0];
      float s2 = samples[i][samples[i].length - 1];
      if (s1 != s1) s1 = 0;
      if (s2 != s2) s2 = 0;
      if (xyzMaps != null && xyzMaps.length > i && xyzMaps[i] != null) {
        xyzMaps[i].setRange(s1, s2);
      }
      p1r[i] = new Real(rt, s1);
      p2r[i] = new Real(rt, s2);
      pxr[i] = new Real(rt, (s1 + s2) / 2);
    }

    // initialize all new indices within the specified range
    pool.expand(LinePool.MINIMUM_SIZE, domain);
    for (int j=0; j<matrix.length; j++) {
      matrix[j] = new MeasureList[numSlices];
      for (int i=0; i<numSlices; i++) {
        matrix[j][i] = new MeasureList(p1r, p2r, pxr, pool);
      }
    }
    inited = true;
  }

  /** Sets the line pool to match the given matrix index. */
  public void setIndex(int index) { setEntry(index, slice); }

  /** Sets the line pool to match the given matrix slice. */
  public void setSlice(int slice) { setEntry(index, slice); }

  /** Sets the line pool to match the given matrix entry. */
  public void setEntry(int index, int slice) {
    if (!inited) System.err.println("Warning: matrix not inited!");
    MeasureList ml = matrix[index][slice];
    this.index = index;
    this.slice = slice;
    pool.set(ml.getMeasurements());
  }

  /** Gets the display linked to the matrix. */
  public DisplayImpl getDisplay() { return display; }

  /** Gets the measurement list for the given slice of the specified index. */
  public MeasureList getMeasureList(int index, int slice) {
    return matrix[index][slice];
  }

  /** Gets all measurement lists in the matrix. */
  public MeasureList[][] getMeasureLists() {
    MeasureList[][] lists = new MeasureList[matrix.length][];
    for (int j=0; j<matrix.length; j++) {
      lists[j] = new MeasureList[matrix[j].length];
      System.arraycopy(matrix[j], 0, lists[j], 0, matrix[j].length);
    }
    return lists;
  }

}
