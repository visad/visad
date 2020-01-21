//
// VisADPointArray.java
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
   VisADPointArray stands in for j3d.PointArray
   and is Serializable.<P>
*/
public class VisADPointArray extends VisADGeometryArray {

  public Object clone() {
    VisADPointArray array = new VisADPointArray();
    copy(array);
    return array;
  }

  /** like the default implementation in VisADGeometryArray.java,
      except no need to construct new VisADPointArray since this
      already is a VisADPointArray;
      split any vectors or trianlges crossing crossing longitude
      seams when Longitude is mapped to a Cartesian display axis;
      default implementation: rotate if necessary, then return points */
  public VisADGeometryArray adjustLongitude(DataRenderer renderer)
         throws VisADException {
    float[] lons = getLongitudes(renderer);
    // note that rotateLongitudes makes any changes to this.coordinates
    return this;
  }

}

