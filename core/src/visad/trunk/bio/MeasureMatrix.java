//
// MeasureMatrix.java
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

import java.rmi.RemoteException;
import visad.*;

/** MeasureMatrix maintains a 2-D matrix of measurements. */
public class MeasureMatrix {

  /** Z-axis RealType. */
  static final RealType ZAXIS_TYPE = RealType.getRealType("bio_line_z");

  /** 2-D matrix of measurements. */
  private MeasureList[][] matrix;

  /** Associated VisAD display. */
  private DisplayImpl display2;

  /** Associated VisAD display for 3-D window. */
  private DisplayImpl display3;

  /** Associated measurement tool panel. */
  private MeasureToolPanel measureTools;

  /** Pool of measurements. */
  private MeasurePool pool;

  /** Pools of measurements for 3-D display. */
  private MeasurePool[] pool3d;

  /** Current matrix index. */
  private int index = -1;

  /** Current matrix slice. */
  private int slice = -1;

  /** Number of slices. */
  private int numSlices = -1;

  /** Whether matrix has been initialized. */
  private boolean inited = false;

  /** Constructs a list of measurements. */
  public MeasureMatrix(int length, DisplayImpl display2,
    DisplayImpl display3, MeasureToolPanel measureTools)
  {
    matrix = new MeasureList[length][];
    this.display2 = display2;
    this.display3 = display3;
    this.measureTools = measureTools;
  }

  /** 
  private void extractMaps(FieldImpl field) {
  }

  /** Initializes the measurement matrix. */
  public void init(FieldImpl field, ScalarMap[][] xyzMaps)
    throws VisADException, RemoteException
  {
    // The FieldImpl must be of the form:
    //     (time -> ((x, y) -> range))
    // where time, x and y are RealTypes,
    // and range is a RealTupleType or RealType.

    if (inited) return;
    
    // extract needed information from FieldImpl image stack
    FunctionType type = (FunctionType) field.getType();
    RealTupleType timeDomain = type.getDomain();
    if (timeDomain.getDimension() > 1) {
      throw new VisADException("Field not an image stack");
    }
    RealType time = (RealType) timeDomain.getComponent(0);
    Set set = field.getDomainSet();
    if (!(set instanceof GriddedSet)) {
      throw new VisADException("Image stack not ordered");
    }
    GriddedSet gset = (GriddedSet) set;
    int[] lengths = gset.getLengths();
    numSlices = lengths[0];
    for (int i=0; i<numSlices; i++) {
      Data data = field.getSample(i);
      if (!(data instanceof FieldImpl)) {
        throw new VisADException("Data #" + i + " not a field");
      }
    }

    // CTR: 3-D stuff probably completely broken; fix it
    /* TEMP */ System.out.println("MeasureMatrix.init: field=" + field.getType());
    /* TEMP */ for (int i=0; i<xyzMaps.length; i++) {
    /* TEMP */   for (int j=0; j<xyzMaps[i].length; j++) {
    /* TEMP */     System.out.println("map[" + i + "][" + j + "]=" + xyzMaps[i][j]);
    /* TEMP */   }
    /* TEMP */ }

    // construct MathType with 3-D domain for display lines on image stack
    FieldImpl image = (FieldImpl) field.getSample(0);
    type = (FunctionType) image.getType();
    RealTupleType domain = type.getDomain();
    int len = domain.getDimension();
    RealType[] types = new RealType[len];
    for (int i=0; i<len; i++) types[i] = (RealType) domain.getComponent(i);
    RealTupleType domain3d = new RealTupleType(types); // CTR: TODO
    RealTupleType domain2d = new RealTupleType(types);

    // extract minimum and maximum values for image
    set = image.getDomainSet();
    float[][] samples = set.getSamples(false);
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
        for (int j=0; j<xyzMaps[i].length; j++) {
          if (xyzMaps[i][j] != null) xyzMaps[i][j].setRange(s1, s2);
        }
      }
      p1r[i] = new Real(rt, s1);
      p2r[i] = new Real(rt, s2);
      pxr[i] = new Real(rt, (s1 + s2) / 2);
    }
    if (xyzMaps != null && xyzMaps.length > len && xyzMaps[len] != null) {
      for (int j=0; j<xyzMaps[len].length; j++) {
        if (xyzMaps[len][j] != null) {
          xyzMaps[len][j].setRange(0, numSlices - 1);
        }
      }
    }

    // initialize measurement pools
    pool = new MeasurePool(display2, measureTools, 2,
      MeasurePool.MINIMUM_SIZE / 2);
    pool.expand(MeasurePool.MINIMUM_SIZE, domain);
    pool3d = new MeasurePool[numSlices];
    if (display3 != null) {
      display3.disableAction();
      for (int i=0; i<numSlices; i++) {
        pool3d[i] =
          new MeasurePool(display3, null, 3, MeasurePool.MINIMUM_SIZE / 2);
        pool3d[i].expand(MeasurePool.MINIMUM_SIZE, domain3d, false);
      }
      display3.enableAction();
    }

    // initialize all measurement lists
    for (int j=0; j<matrix.length; j++) {
      matrix[j] = new MeasureList[numSlices];
      for (int i=0; i<numSlices; i++) {
        matrix[j][i] =
          new MeasureList(this, p1r, p2r, pxr, slice, pool, pool3d[i]);
      }
    }

    inited = true;
    setEntry(0, 0);
  }

  /** Refreshes the onscreen measurements to match the current matrix entry. */
  public void refresh() {
    setEntry(index, slice);
    measureTools.updateGroupList();
  }

  /** Sets the measurement pool to match the given matrix index. */
  public void setIndex(int index) { setEntry(index, slice); }

  /** Sets the measurement pool to match the given matrix slice. */
  public void setSlice(int slice) { setEntry(index, slice); }

  /** Sets the measurement pool to match the given matrix entry. */
  public void setEntry(int index, int slice) {
    if (!inited) System.err.println("Warning: matrix not inited!");
    if (display3 != null) {
      if (this.index != index) {
        // set 3-D window to match given matrix index
        MeasureList[] lists = matrix[index];
        for (int i=0; i<lists.length; i++) {
          pool3d[i].set(lists[i].getMeasurements());
        }
      }
    }
    this.index = index;
    this.slice = slice;
    MeasureList ml = matrix[index][slice];
    pool.set(ml.getMeasurements());
  }

  /** Gets the current matrix index. */
  public int getIndex() { return index; }

  /** Gets the current matrix slice. */
  public int getSlice() { return slice; }

  /** Gets the number of slices in the matrix. */
  public int getNumberOfSlices() { return numSlices; }

  /** Gets the display linked to the matrix. */
  public DisplayImpl getDisplay() { return display2; }

  /** Gets the 3-D display linked to the matrix. */
  public DisplayImpl getDisplay3d() { return display3; }

  /** Gets the measurement list for the current index and slice. */
  public MeasureList getMeasureList() { return getMeasureList(index, slice); }

  /** Gets the measurement list for the given slice of the current index. */
  public MeasureList getMeasureList(int slice) {
    return getMeasureList(index, slice);
  }

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
