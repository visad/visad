//
// MeasureMatrix.java
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

  /** Constructs a list of measurements. */
  public MeasureMatrix(int length, DisplayImpl display) {
    matrix = new MeasureList[length][];
    this.display = display;
    pool = new LinePool(display, MeasureList.MIN_POOL_SIZE / 2);
  }

  /** Initializes the given index and sets it as the current index. */
  public void initIndex(int index, FieldImpl field, ScalarMap[] xyzMaps,
    boolean overwrite) throws VisADException, RemoteException
  {
    if (overwrite || matrix[index] == null) {
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
      matrix[index] = new MeasureList[numSlices];
      for (int i=0; i<numSlices; i++) {
        Data data = field.getSample(i);
        if (!(data instanceof FieldImpl)) {
          throw new VisADException("Data #" + i + " not a field");
        }
        FieldImpl slice = (FieldImpl) data;
        matrix[index][i] = new MeasureList(display, slice, pool, xyzMaps);
      }
    }
    this.index = index;
    setEntry(index, 0);
  }

  /** Sets the line pool to match the given matrix slice. */
  public void setSlice(int slice) { setEntry(index, slice); }

  /** Sets the line pool to match the given matrix entry. */
  public void setEntry(int index, int slice) {
    MeasureList ml = matrix[index][slice];
    pool.setLines(ml.getMeasurements());
  }

  /** Gets the measurement list for the given slice of the specified index. */
  public MeasureList getMeasureList(int index, int slice) {
    return matrix[index][slice];
  }

}
