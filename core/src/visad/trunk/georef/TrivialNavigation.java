//
// TrivialNavigation.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

public class TrivialNavigation extends NavigatedCoordinateSystem
{

    /**
     * Create a NavigationCoordinateSystem that just returns
     * the input tuple.
     *
     * @param  reference  reference RealTupleType
     *
     * @throws VisADException  reference does not contain Latitude/Longitude
     *                         or couldn't create the necessary VisAD object
     */
    public TrivialNavigation(RealTupleType reference)
        throws VisADException
    {
        super(reference, reference.getDefaultUnits());
    }

    /** 
     * Transform to the reference coordinates
     *
     * @param  tuple  array of values
     * @return  input array
     *
     * @throws VisADException  tuple is null or wrong dimension
     */
    public double[][] toReference(double[][] tuple)
        throws VisADException
    {
        if (tuple == null || getDimension() != tuple.length)
            throw new VisADException(
                "Values are null or wrong dimension");
        return tuple;
    }

    /** 
     * Transform from the reference coordinates
     *
     * @param  refTuple  array of values
     * @return  input array
     *
     * @throws VisADException  tuple is null or wrong dimension
     */
    public double[][] fromReference(double[][] refTuple)
        throws VisADException
    {
        if (refTuple == null || getDimension() != refTuple.length)
            throw new VisADException(
                "Values are null or wrong dimension");
        return refTuple;
    }

    /**
     * See if the object in question is equal to this CoordinateSystem.
     * The two objects are equal if they are the same object or if they
     * are both TrivialNavigations and have the same dimension.
     *
     * @param  cs  Object in question
     * @return  true if they are considered equal, otherwise false.
     */
    public boolean equals(Object cs)
    {
        if ((cs instanceof TrivialNavigation && 
               ((TrivialNavigation) cs).getDimension() == getDimension()) ||
            cs == this) return true;
        else
            return false;
    }
}
