//
// CoordinateSystemException.java
//

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

package visad;

/**
   CoordinateSystemException is an exception for a bad VisAD CoordinateSystem.<P>
*/
public class CoordinateSystemException extends VisADException {

  /**
   * construct a CoordinateSystemException with no message
   */
  public CoordinateSystemException() { super(); }

  /**
   * construct a CoordinateSystemException with given message
   * @param s - message String
   */
  public CoordinateSystemException(String s) { super(s); }

  /**
   * construct a CoordinateSystemException for unequal
   * CoordinateSystems
   * @param cs1 - first CoordinateSystem
   * @param cs2 - second CoordinateSystem
   */
  public CoordinateSystemException(CoordinateSystem cs1, CoordinateSystem cs2) {

    this("Coordinate system mismatch: " +
      (cs1 == null ? "null" : cs1.getReference().toString()) + " != " +
      (cs2 == null ? "null" : cs2.getReference().toString()));
  }

}

