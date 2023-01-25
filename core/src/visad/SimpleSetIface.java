//
// SimpleSetIface.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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
 * Interface to the abstract superclass of Sets with a unique ManifoldDimension.
 */
public interface SimpleSetIface
  extends SetIface
{
  /**
   * Returns the interpolation parameters for an array of points.
   *
   * @param values              An array of points. <code>value[i][j]</code> is
   *                            the i-th coordinate of the j-th points.
   * @param indices             Indices of the neighboring samples in the set.
   *                            If the j-th points lies within the set, then
   *                            returned element [i][j] is the index of the
   *                            i-th neighboring sample in the set; otherwise,
   *                            returned array [j] is <code>null</code>.
   * @param weights             Weights for interpolating the neighboring
   *                            samples in the set.  If the j-th points lies
   *                            within the set, then returned element [i][j]
   *                            is the weight of the i-th neighboring sample
   *                            in the set; otherwise, returned array [j] is
   *                            <code>null</code>.
   * @throws VisADException	VisAD failure.
   */
  void valueToInterp(float[][] values, int[][] indices, float[][] weights)
    throws VisADException;
}
