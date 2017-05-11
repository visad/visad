//
//  NamedLocation.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

import visad.Text;

/**
 *  An interface for a named earth location.
 */
public interface NamedLocation extends EarthLocation
{
    /**
     * Return a unique identifier. This might be a Text object 
     * representing the name of the station (e.g.: "Denver"), the ICAO 
     * 4 letter id (e.g., "KDEN"), a WMO block and station number 
     * as a string (e.g., "72565"), or some other identifying string 
     * (i.e., "intersection of Mitchell and 47th" or "Point A", or "A")
     *
     * @return  Text whose getValue() method returns the identifier
     */
    Text getIdentifier();

    /**
     * Get the lat/lon/altitude as an EarthLocation.
     *
     * @return  EarthLocation for this object
     */
    EarthLocation getEarthLocation();
}
