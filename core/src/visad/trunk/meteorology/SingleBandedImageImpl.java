//
//  SingleBandedImageImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.RemoteException;

/**
 * An implementation for representing single-banded planar 
 * satellite or radar imagery.  
 */
public class SingleBandedImageImpl
    extends FlatField
    implements SingleBandedImage
    
{
    private DateTime startTime;
    private String description;
    private Real minValue;
    private Real maxValue;

    /**
     * Construct a SingleBandedImageImpl without any data.  
     *
     * @param  function  FunctionType for this image.   It must have a 
     *                   Range that has only one (Real) component.
     * @param  domain    DomainSet for this image.  
     * @param  startTime starting time of the image.
     * @param  desc      description
     *
     * @throws  VisADException  couldn't create the SingleBandedImageImpl
     */
    public SingleBandedImageImpl(FunctionType function, 
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
     * Construct a SingleBandedImageImpl from a FlatField.
     *
     * @param  image     FlatField representing an image.  It must
     *                   have a Range that only has one (Real) component.
     * @param  startTime starting time of the image.
     * @param  desc      description
     *
     * @throws  VisADException  couldn't create the SingleBandedImageImpl
     */
    public SingleBandedImageImpl(FlatField image, 
                          DateTime startTime, 
                          String desc)
        throws VisADException
    {
        super((FunctionType) image.getType(), image.getDomainSet());

        // vet the range
        if (((FunctionType) 
              getType()).getFlatRange().getNumberOfRealComponents() > 1)
        {
            throw new VisADException(
                "SingleBandedImageImpl: Range must be a RealType or " +
                "RealTupleType with one component");
        }

        // add in the data
        try
        {
            if (!image.isMissing()) 
            {
                setSamples(image.getValues());
                setRangeErrors(image.getRangeErrors());
                setMaxMinValues();
            }
        }
        catch (java.rmi.RemoteException re) {;}  // can't happen since local

        // set some local variables
        this.startTime = startTime;
        description = desc;
    }
    
    /**
     * Get the start time of the image.
     * @return  DateTime representing the start time of the image.
     */
    public DateTime getStartTime()
    {
        return startTime;
    }

    /**
     * Return a descriptive string for this image.
     * @return description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get the minimum possible value for this image
     * @return  a Real representing the minimum possible value.  Using a
     *          Real allows us to associate units and error estimates with
     *          the value
     */
    public Real getMinRangeValue()
    {
        return minValue;
    }

    /**
     * Get the maximum possible value for this image
     * @return  a Real representing the maximum possible value.  Using a
     *          Real allows us to associate units and error estimates with
     *          the value
     */
    public Real getMaxRangeValue()
    {
        return maxValue;
    }

    /**
     * Check to see if this image has a domain that can map to Latitude
     * and Longitude.
     *
     * @return true if it has navigation, otherwise false
     */
    public boolean isNavigated()
    {
        return getDomainCoordinateSystem() instanceof NavigatedCoordinateSystem;
    }

    /** 
     * Set the range values of the function including ErrorEstimate-s;
     * the order of range values must be the same as the order of
     * domain indices in the DomainSet.  Overridden so we can set
     * max and min values.
     *
     * @param  range    pixel values as doubles
     * @param  errors   ErrorEstimates for values (may be null);
     * @param  copy     flag to make a copy of value array or not
     *
     * @throws VisADException  couldn't set values
     * @throws RemoteException couldn't set remote object
     */
    public void setSamples(float[][] range, 
                           ErrorEstimate[] errors, 
                           boolean copy) 
        throws VisADException, RemoteException 
    {
        super.setSamples(range, errors, copy);
        setMaxMinValues();
    }

    /** 
     * Set the range values of the function including ErrorEstimate-s;
     * the order of range values must be the same as the order of
     * domain indices in the DomainSet.  Overridden so we can set
     * max and min values.
     *
     * @param  range    pixel values as doubles
     * @param  errors   ErrorEstimates for values (may be null);
     * @param  copy     flag to make a copy of value array or not
     *
     * @throws VisADException  couldn't set values
     * @throws RemoteException couldn't set remote object
     */
    public void setSamples(double[][] range, 
                           ErrorEstimate[] errors, 
                           boolean copy) 
        throws VisADException, RemoteException 
    {
        super.setSamples(range, errors, copy);
        setMaxMinValues();
    }

    /** return new SingleBandedImageImpl with value 'op this' */
    public Data unary(int op, MathType new_type, 
                      int sampling_mode, int error_mode)
                  throws VisADException
    {
        return 
            new SingleBandedImageImpl(
                (FlatField) 
                    super.unary(op, new_type, sampling_mode, error_mode),
                startTime, description);
    }

    /**
     * Override clone in FlatField so we carry the metadata with
     * us (and to make sure that the clone is a SingleBandedImage.
     * @return  new SingleBandedImage which is a clone of this one.
     */
    public Object clone()
    {
        SingleBandedImage newImage;
        try
        {
            newImage = 
                new SingleBandedImageImpl(
                    (FlatField) super.clone(), startTime, description);
        }
        catch (VisADException excp)
        {
            throw new VisADError(
                "SingleBandedImageImpl.clone(): VisADException occurred");
        }
        return newImage;
    }

    private void setMaxMinValues()
        throws VisADException
    {
        Unit units = null;
        RealType type = RealType.Generic;
        ErrorEstimate errors = null;
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;
        try
        {
            Set rangeSet = getRangeSets()[0];
            units = getRangeUnits()[0][0];
            type = (RealType) 
                ((RealTupleType) 
                ((SetType) rangeSet.getType()).getDomain()).getComponent(0);
            errors = getRangeErrors()[0];
            if (rangeSet instanceof SampledSet)
            {
                min = ((SampledSet) rangeSet).getLow()[0];
                max = ((SampledSet) rangeSet).getHi()[0];
            }
            else
            {
                double[] values = getValues(0);
                for (int i = 0; i < values.length; i++)
                {
                    // initialize on first non-missing value
                    if (values[i] != Double.NaN)
                    {
                        if (min == Double.MIN_VALUE)  // initialize first time
                        {
                           min = values[i];
                           max = values[i];
                        }
                        else 
                        {
                            if (values[i] < min)  min = values[i];
                            if (values[i] > max)  max = values[i];
                        }
                    }
                }
            }
        }
        catch (Exception e) {;}
        minValue =  new Real(type, min, units, errors);
        maxValue =  new Real(type, max, units, errors);
    }
            
}
