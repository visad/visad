/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.util.logging.Logger;

/**
 * Growable array of float tuples.
 */
public class FloatTupleArrayImpl implements FloatTupleArray {

  private static Logger log = Logger.getLogger(FloatTupleArrayImpl.class.getName());

  /** Factor by which the arrays are grown when they run out of space. */
  public static final float DEF_GROW_FACTOR = 1.5f;

  protected float[][] elements;
  private int size; // last used element
  private final int dim;
  private final float growFactor;

  public FloatTupleArrayImpl(int dim, int initialSize) {
    this(dim, initialSize, DEF_GROW_FACTOR);
  }

  /**
   * Construct an instance of <code>initialSize</code>.
   * @param dim The tuple dimension.
   * @param initialSize The initial size of the internal array.
   * @param growFactor Factor to grow by when resizing is necessary.
   */
  public FloatTupleArrayImpl(int dim, int initialSize, float growFactor) {
    assert initialSize > 1;
    this.dim = dim;
    elements = new float[dim][initialSize];
    this.growFactor = growFactor;
  }

  /**
   * Grow the internal arrays. <code>System.arraycopy</code> is used for array
   * expansion.
   */
  protected void grow() {
    int newSize = (int)(elements[0].length * growFactor);
    log.fine("growing from " + elements[0].length + " to "+newSize);
    float[][] newArr = new float[elements.length][newSize];
    for (int i = 0; i < elements.length; i++) {
      System.arraycopy(elements[i], 0, newArr[i], 0, elements[0].length);
    }
    elements = newArr;
    newArr = null;
  }

  /** 
   * Add values. If there is not enough space for the number of values to be 
   * added the array is continually grown until there is room.
   * @param values Source array.
   * @param start index in the source array to start at
   * @param num number of values to add.
   */
  
  public void add(float[][] values, int start, int num) {

    int spaceLeft = this.elements[0].length - size;
    while (spaceLeft < num) {
      grow();
      spaceLeft = this.elements[0].length - size;
    }

    for (int i = 0; i < this.dim; i++) {
      System.arraycopy(values[i], start, this.elements[i], size, num);
    }
    size += num;
  }

  /**
   * Reference to the backing data array. Any changes made to this array will be 
   * reflected in the actual data.
   */
  public float[][] elements() {
    return elements;
  }

  /**
   * Add values. If there is not enough space for the number of values to be 
   * added the array is continually grown until there is room.
   * @param values Source array.
   */
  public void add(float[][] values) {
    add(values, 0, values[0].length);
  }

  /** 
   * Set a value.
   * @param i dimension index of the values to set
   * @param j tuple index of the value to set
   * @param val the value
   */
  public void set(int i, int j, float val) {
    assert i < dim;
    assert j < size;
    elements[i][j] = val;
  }

  /**
   * Get a tuple. 
   * @param idx The index of the tuple
   * @return an array of the tuple values at the provided index.
   */
  public float[] get(int idx) {
    assert idx >= 0 && idx < size;
    float[] point = new float[dim()];
    for (int i = 0; i < dim(); i++) {
      point[i] = elements[i][idx];
    }
    return point;
  }

  /**
   * Get a value.
   * @param i dimension index
   * @param j tuple index.
   */
  public float get(int i, int j) {
    assert i < elements.length;
    assert i < size;
    return elements[i][j];
  }

  /**
   * Get contained data as array. The size of the array is exactly the size of the
   * data. 
   * <p>
   * NOTE: This is an expensive operation.
   */
  public float[][] toArray() {
    float[][] ret = new float[dim][size];
    for (int i = 0; i < dim; i++) {
      System.arraycopy(elements[i], 0, ret[i], 0, size);
    }
    return ret;
  }

  /**
   * Get the size of the valid data.
   */
  public int size() {
    return size;
  }

  /**
   * Get the dimension of the tuple.
   */
  public int dim() {
    return dim;
  }
}
