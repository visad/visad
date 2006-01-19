//
//  SatelliteImage.java
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

package visad.meteorology;

import visad.*;

/**
 * An implementation for representing single-banded planar satellite
 * that has navigation.  
 */
public class SatelliteImage extends SingleBandedImageImpl
    implements SatelliteData
    
{
    private String sensorName;

    /**
     * Construct a SatelliteImage without any data.  
     *
     * @param  function  FunctionType for this image.   It must have a 
     *                   Range that has only one (Real) component.
     * @param  domain    DomainSet for this image.  
     * @param  startTime starting time of the image.
     * @param  desc      description
     * @param  sensor    sensor description
     *
     * @throws  VisADException  couldn't create the SatelliteImage
     */
    public SatelliteImage(FunctionType function, 
                          Set domain, 
                          DateTime startTime, 
                          String desc,
                          String sensor)
        throws VisADException
    {
        this(new FlatField(function, domain), 
             startTime, 
             desc,  
             sensor);
    }

    
    /**
     * Construct a Satellite Image from a FlatField.
     *
     * @param  image     FlatField representing an image.  It must
     *                   have a Range that only has one (Real) component.
     * @param  startTime starting time of the image.
     * @param  desc      description
     * @param  sensor    sensor name
     *
     * @throws  VisADException  couldn't create the NavigatedImage
     */
    public SatelliteImage(FlatField image, 
                          DateTime startTime, 
                          String desc,
                          String sensor)
        throws VisADException
    {
        super(image, startTime, desc);
        sensorName = sensor;
    }
    
    /**
     * Get a description of the sensor.
     * @return  sensor description
     */
    public String getSensorName()
    {
        return sensorName;
    }
        
    /** return new SatelliteImage with value 'op this' */
    public Data unary(int op, MathType new_type, 
                      int sampling_mode, int error_mode)
                  throws VisADException
    {
        return 
            new SatelliteImage(
                (FlatField)
                    super.unary(op, new_type, sampling_mode, error_mode),
                getStartTime(), getDescription(), sensorName);
    }
}
