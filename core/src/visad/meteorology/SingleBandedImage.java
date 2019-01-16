//
//  SingleBandedImage.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.meteorology;

import visad.*;

/**
 * An interface for representing single--banded planar satellite or 
 * radar imagery.  This type of Field has a Range Dimension of 1 with
 * a range Type being a RealType.
 */
public interface SingleBandedImage extends Field
{
    /**
     * Get the start time of the image.
     * @return  DateTime representing the start time of the image.
     */
    DateTime getStartTime();

    /**
     * Return a descriptive string for this image.
     * @return description
     */
    String getDescription();

    /**
     * Get the minimum possible value for this image
     * @return  a Real representing the minimum possible value.  Using a
     *          Real allows us to associate units and error estimates with
     *          the value
     */
    Real getMinRangeValue();

    /**
     * Get the maximum possible value for this image
     * @return  a Real representing the maximum possible value.  Using a
     *          Real allows us to associate units and error estimates with
     *          the value
     */
    Real getMaxRangeValue();

    /**
     * Check to see if this image has a domain that can map to Latitude
     * and Longitude.
     *
     * @return true if it has navigation, otherwise false
     */
    boolean isNavigated();
}
