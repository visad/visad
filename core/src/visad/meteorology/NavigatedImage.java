//
//  NavigatedImage.java
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

package visad.meteorology;

import visad.*;
import visad.georef.*;

/**
 * An implementation for representing single-banded planar 
 * satellite or radar imagery.  This specific type of SingleBandedImage has a 
 * navigation that will map domain values to latitude and longitude.
 */
public class NavigatedImage
    extends SingleBandedImageImpl
    implements NavigatedField
    
{
    private NavigatedCoordinateSystem navigation;

    /**
     * Construct a NavigatedImage without any data.  
     *
     * @param  function  FunctionType for this image.   It must have a domain
     *                   with a NavigatedCoordinateSystem and a Range that
     *                   has only one (Real) component.
     * @param  domain    DomainSet for this image.  The domain must have
     *                   mappings to Latitude/Longitude and or have a
     *                   NavigatedCoordinateSystem associated with it.
     * @param  startTime starting time of the image.
     * @param  desc      description
     *
     * @throws  VisADException  couldn't create the NavigatedImage
     */
    public NavigatedImage(FunctionType function, 
                          Set domain, 
                          DateTime startTime, 
                          String desc)
        throws VisADException
    {
        this(new FlatField(function, domain), 
             startTime, 
             desc);
    }

    /**
     * Construct a NavigatedImage from a FlatField.
     *
     * @param  image     FlatField representing an image.  It must
     *                   have a domain with a NavigatedCoordinateSystem
     *                   and a Range that only has one (Real) component.
     * @param  startTime starting time of the image.
     * @param  desc      description
     *
     * @throws  VisADException  couldn't create the NavigatedImage
     */
    public NavigatedImage(FlatField image, 
                          DateTime startTime, 
                          String desc)
        throws VisADException
    {
        this(image, startTime, desc, true);
    }

    /**
     * Construct a NavigatedImage from a FlatField.
     *
     * @param  image     FlatField representing an image.  It must
     *                   have a domain with a NavigatedCoordinateSystem
     *                   and a Range that only has one (Real) component.
     * @param  startTime starting time of the image.
     * @param  desc      description
     * @param  copyData  make a copy of the samples
     *
     * @throws  VisADException  couldn't create the NavigatedImage
     */
    public NavigatedImage(FlatField image, 
                          DateTime startTime, 
                          String desc,
                          boolean copyData)
        throws VisADException
    {
        super(image, startTime, desc);

        // make sure the domain is okay
        vetDomain();
    }
    
    /**
     * Get the coordinate system representing the navigation for the domain.
     *
     * @return NavigatedCoordinateSystem for the domain of this field.
     */
    public NavigatedCoordinateSystem getNavigation()
    {
        return navigation;
    }

    private void vetDomain()
        throws VisADException
    {
        CoordinateSystem cs = getDomainCoordinateSystem();
        if (cs != null && !(cs instanceof NavigatedCoordinateSystem))
        {
            throw new VisADException(
                "NavigatedImage: Domain CoordinateSystem must be " +
                "a NavigatedCoordinateSystem");
        }
        else if (cs == null)
        {
            if (getDomainSet().getDimension() < 2 ||
               !hasLatLon( (RealTupleType) getDomainSet().getType()))
            throw new VisADException(
                "NavigatedImage: Domain set must have " +
                "a Lat/Lon reference");
            cs = 
                new TrivialNavigation(((FunctionType) getType()).getDomain());
        }
        navigation = (NavigatedCoordinateSystem) cs;
    } 

    private boolean hasLatLon(RealTupleType type)
    {
        return (type.getIndex(RealType.Latitude) > -1 &&
                type.getIndex(RealType.Longitude) > -1);
    }

    /**
     * Check to see if this image has a domain that can map to Latitude
     * and Longitude.
     *
     * @return true if it has navigation, otherwise false
     */
    public boolean isNavigated()
    {
        return true;
    }

    /** return new NavigatedImage with value 'op this' */
    public Data unary(int op, MathType new_type, 
                      int sampling_mode, int error_mode)
                  throws VisADException
    {
        return 
            new NavigatedImage(
                (FlatField) 
                    super.unary(op, new_type, sampling_mode, error_mode),
                getStartTime(), getDescription(), false);
    }
}
