//
// NavigatedCoordinateSystem.java
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

package visad.georef;

import visad.*;

/**
 * Abstract class for CoordinateSystems that have RealType.Latitude
 * and RealType.Longitude in their reference RealTupleType.
 *
 * @author Don Murray
 */
public abstract class NavigatedCoordinateSystem extends CoordinateSystem
{
    private final int latIndex;
    private final int lonIndex;

    /**
     * Constructs from the type of the reference coordinate system and 
     * units for values in this coordinate system. The reference coordinate
     * system must contain RealType.Latitude and RealType.Longitude.
     *
     * @param reference  The type of the reference coordinate system. The
     *                   reference must contain RealType.Latitude and
     *                   RealType.Longitude.  Values in the reference 
     *                   coordinate system shall be in units of 
     *                   reference.getDefaultUnits() unless specified 
     *                   otherwise.
     * @param units      The default units for this coordinate system. 
     *                   Numeric values in this coordinate system shall be 
     *                   in units of units unless specified otherwise. 
     *                   May be null or an array of null-s.
     * @exception VisADException  Couldn't create necessary VisAD object or
     *                            reference does not contain RealType.Latitude
     *                            or RealType.Longitude.
     */
    public NavigatedCoordinateSystem(RealTupleType reference, Unit[] units)
        throws VisADException
    {
        super(reference, units);
        latIndex = reference.getIndex(RealType.Latitude);
        if (latIndex == -1)
            throw new CoordinateSystemException(
                "NavigatedCoordinateSystem: Reference must contain " + 
                "RealType.Latitude");
        lonIndex = reference.getIndex(RealType.Longitude);
        if (lonIndex == -1)
            throw new CoordinateSystemException(
                "NavigatedCoordinateSystem: Reference must contain " + 
                "RealType.Longitude");
    }

    /**
     * Get the index of RealType.Latitude in the reference RealTupleType.
     *
     * @return  index of RealType.Latitude in the reference
     */
    public int getLatitudeIndex()
    {
        return latIndex;
    }

    /**
     * Get the index of RealType.Longitude in the reference RealTupleType.
     *
     * @return  index of RealType.Longitude in the reference
     */
    public int getLongitudeIndex()
    {
        return lonIndex;
    }

}
