//
// VisADTriangleArray.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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
   VisADTriangleArray stands in for j3d.TrianlgeArray
   and is Serializable.<P>
*/
public class VisADTriangleArray extends VisADGeometryArray {

  /**
   * Clone this VisADTriangleArray
   * @return clone of this
   */
  public Object clone() {
    VisADTriangleArray array = new VisADTriangleArray();
    copy(array);
    return array;
  }

  /**
   * Merge an array of VisADTriangleArrays into a single VisADTriangleArray.
   * @param  arrays  array of VisADTriangleArrays (may be null)
   * @return a single VisADTriangleArray with all the info of arrays.
   *         returns null if input is null.
   */
  public static VisADTriangleArray merge(VisADTriangleArray[] arrays)
         throws VisADException {
    if (arrays == null || arrays.length == 0) return null;
    VisADTriangleArray array = new VisADTriangleArray();
    merge(arrays, array);
    return array;
  }
}

