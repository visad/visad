//
//  LatLonPoint.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.georef;

import visad.Real;
import visad.Data;

/**
 * Interface for supporting latitude/longitude points.
 */
public interface LatLonPoint extends Data
{
    /**
     * Get the latitude of this point
     *
     * @return  Real representing the latitude
     */
    Real getLatitude();

    /**
     * Get the longitude of this point
     *
     * @return  Real representing the longitude
     */
    Real getLongitude();

    /**
     * See if this LatLonPoint is equal to the object in question.
     * Two points are equal if they are the same object, or if their
     * lat/lon componets are equal.
     *
     * @param  obj  object in question
     */
    boolean equals(Object obj);
}
