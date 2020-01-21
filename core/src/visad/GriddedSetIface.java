//
// GriddedSetIface.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
 * GriddedSetIface is the interface to a finite set of samples of R^n.<P>
 */
public interface GriddedSetIface
  extends SampledSetIface
{
  /**
   * Returns the number of grid points in a given dimension.
   *
   * @param i			The index of the dimension.
   * @return			The number of grid points in dimension 
   *				<code>i</code>.
   */
  int getLength(int i);

  /**
   * Returns the number of grid points in all dimensions.
   *
   * @return			The number of grid points in all dimensions.
   *				Element [i] is the number of grid points in
   *				dimension <code>i</code>.
   */
  int[] getLengths();

  /**
   * Returns the interpolated samples of the set corresponding to an array of
   * grid points with non-integer coordinates.
   *
   * @param grid                The coordinates of the interpolation grid
   *                            points for which interpolated sample values are
   *                            desired. <code>grid[i][j]</code> is the i-th
   *                            grid coordinate of the j-th interpolation point.
   * @return                    The interpolated samples of the set.  Element
   *                            [i][j] is the i-th coordinate of the j-th
   *                            interpolation point.
   * @throws VisADException	VisAD failure.
   */
  float[][] gridToValue(float[][] grid) throws VisADException;

  /**
   * Returns the non-integer grid coordinates corresponding to an array of 
   * points.
   *
   * @param value               The array of points for which non-integer
   *                            grid coordinates are desired.
   *                            <code>value[i][j]</code> is the i-th coordinate
   *                            of the j-th point.
   * @return                    The array of grid coordinates corresponding
   *                            to the points.  Element [i][j] is the i-th
   *                            non-integer grid coordinate of the j-th point.
   * @throws VisADException	VisAD failure.
   */
  float[][] valueToGrid(float[][] value) throws VisADException;
}
