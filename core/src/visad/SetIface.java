//
// SetIface.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

/**
 * Interface to the abstract superclass of the VisAD hierarchy of sets.<P>
 * Sets are subsets of R^n for n>0.
 */
public interface SetIface
  extends Data
{
  /**
   * Returns the units of the samples in the set.
   *
   * @return			The units of the samples in the set.  Element
   *				[i] is the unit of the i-th coordinate.
   */
  Unit[] getSetUnits();

  /**
   * Returns the error estimates of the samples in the set.
   *
   * @return			The error estimates of the samples in the set.
   *				Element [i] is the error estimate of the i-th
   *				coordinate.
   */
  ErrorEstimate[] getSetErrors();

  /**
   * Returns the coordinate system transformation of the set.
   *
   * @return			The coordinate system transformation of the set.
   */
  CoordinateSystem getCoordinateSystem();

  /**
   * Returns the rank of the samples in the set.
   *
   * @return			The rank of the samples in the set.
   */
  int getDimension();

  /**
   * Returns the rank of the manifold of the set.
   *
   * @return			The rank of the manifold of the set.
   */
  int getManifoldDimension();

  /**
   * Returns the number of samples in the set.
   *
   * @return			The number of samples in the set.
   */
  int getLength() throws VisADException;

  /**
   * Returns the samples of the set corresponding to an array of 1-D indices.
   *
   * @param index		The array of 1-D indices.
   * @return			The values of the set corresponding to the input
   *				indices.  Element [i][j] is the i-th coordinate
   *				of the sample at index <code>index[j]</code>.
   * @throws VisADException	VisAD failure.
   */
  float[][] indexToValue(int[] index) throws VisADException;

  /**
   * Returns the 1-D indices corresponding to an array of points.
   * The set must have at least two samples.
   *
   * @param value               An array of points. <code>value[i][j]</code> is
   *                            the i-th coordinate of the j-th point.
   * @return                    Indices of the nearest samples in the set.
   *                            If the j-th point lies within the set, then
   *                            element [i] is the index of the closest sample;
   *                            otherwise, element [i] is -1.
   * @throws VisADException	VisAD failure.
   */
  int[] valueToIndex(float[][] value) throws VisADException;

  Set makeSpatial(SetType type, float[][] values) throws VisADException;

  /**
   * Returns a zig-zagging enumeration of sample indices with good coherence.
   *
   * @return			Indices of the samples of the set in a zig-
   *				zagging pattern with good coherence.
   */
  int[] getWedge();

  /**
   * Returns an enumeration of the samples of the set in index order.  This is
   * the same as <code>getSamples(true)</code>.
   *
   * @return			An enumeration of the samples of the set.
   *				Element [i][j] is the i-th coordinate of the
   *				j-th sample.
   * @throws VisADException	VisAD failure.
   * @see #getSamples(boolean copy)
   */
  float[][] getSamples() throws VisADException;

  /**
   * Returns an enumeration of the samples of the set in index order.
   *
   * @param copy		Whether or not to make a copy of the samples
   *				of the set.
   * @return			An enumeration of the samples of the set.
   *				Element [i][j] is the i-th coordinate of the
   *				j-th sample.
   * @throws VisADException	VisAD failure.
   */
  float[][] getSamples(boolean copy) throws VisADException;

  /**
   * Returns an enumeration of the samples of the set in index order.  This is
   * the same as <code>getDoubles(true)</code>.
   *
   * @return			An enumeration of the samples of the set.
   *				Element [i][j] is the i-th coordinate of the
   *				j-th sample.
   * @throws VisADException	VisAD failure.
   * @see #getDoubles(boolean copy)
   */
  double[][] getDoubles() throws VisADException;

  /**
   * Returns an enumeration of the samples of the set in index order.
   *
   * @param copy		Whether or not to make a copy of the samples
   *				of the set.
   * @return			An enumeration of the samples of the set.
   *				Element [i][j] is the i-th coordinate of the
   *				j-th sample.
   * @throws VisADException	VisAD failure.
   */
  double[][] getDoubles(boolean copy) throws VisADException;

  void cram_missing(boolean[] range_select);

  Set merge1DSets(Set set) throws VisADException;

  VisADGeometryArray make1DGeometry(byte[][] color_values)
    throws VisADException;

  VisADGeometryArray make2DGeometry(byte[][] color_values, boolean indexed)
    throws VisADException;

  VisADGeometryArray[] make3DGeometry(byte[][] color_values)
    throws VisADException;

  VisADGeometryArray makePointGeometry(byte[][] color_values)
    throws VisADException;

  VisADGeometryArray[][] makeIsoLines(
      float[] intervals,
      float lowlimit,
      float highlimit,
      float base,
      float[] fieldValues,
      byte[][] color_values,
      boolean[] swap,
      boolean dash,
      boolean fill,
      ScalarMap[] smap,
      double[] scale,
      double label_size,
      boolean sphericalDisplayCS)
    throws VisADException;

  VisADGeometryArray makeIsoSurface(
      float isolevel,
      float[] fieldValues,
      byte[][] color_values,
      boolean indexed)
    throws VisADException;

  double[][] indexToDouble(int[] index)
    throws VisADException;

  int[] doubleToIndex(double[][] value)
    throws VisADException;

  void getNeighbors(int[][] neighbors)
    throws VisADException;

  void getNeighbors(int[][] neighbors, float[][] weights)
    throws VisADException;

  int[][] getNeighbors(int dimension) throws VisADException;

  boolean equalUnitAndCS(Set set);

  boolean equals(java.lang.Object set);

  /**
   * Clones this set -- changing the MathType.
   *
   * @param type		The MathType for the clone.
   * @return			A clone of this set with the new MathType.
   * @throws VisADException	VisAD failure.
   */
  Object cloneButType(MathType type) throws VisADException;
}
