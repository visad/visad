//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

import visad.*;
import visad.georef.*;
import java.rmi.RemoteException;
import visad.util.DataUtility;

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
    private boolean copyOnClone = true;

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
        this(new FlatField(function, domain), startTime, desc);
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
        this(image, startTime, desc, true);
    }

    /**
     * Construct a SingleBandedImage from the FlatField specified.
     *
     * @param  image     FlatField representing an image.  It must
     *                   have a Range that only has one (Real) component.
     * @param  startTime starting time of the image.
     * @param  desc      description
     * @param  copyData  make a copy of the data on setSample call
     *
     * @throws  VisADException  couldn't create the SingleBandedImageImpl
     */
    public SingleBandedImageImpl(FlatField image, 
                          DateTime startTime, 
                          String desc,
                          boolean copyData)
        throws VisADException
    {
        super((FunctionType) image.getType(), image.getDomainSet(),
              image.getRangeCoordinateSystem()[0],
              image.getRangeSets(), 
              DataUtility.getRangeUnits(image));

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
                setSamples(
                   //image.getFloats(false), image.getRangeErrors(), copyData);
                   image.getFloats(false), copyData);
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
        checkMaxMinValues();
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
        checkMaxMinValues();
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
        minValue = maxValue = null;
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
        minValue = maxValue = null;
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
                startTime, description, false);
    }

    private void checkMaxMinValues() {
        if(minValue == null) {
            try {
                setMaxMinValues();
            } catch(Exception exc) {
                System.err.println ("error:" + exc);
                exc.printStackTrace();
            }
        }
    }



    private void setMaxMinValues()
        throws VisADException
    {
        Unit units = null;
        RealType type = RealType.Generic;
        ErrorEstimate errors = null;
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
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
                float[] values = getFloats(false)[0];
                final int len = values.length;
                for (int i = 0; i < len; i++)
                {
                    float value = values[i];
                    if (value < min)  min = value;
                    if (value > max)  max = value;
                }
            }
        }
        catch (Exception e) {;}
        minValue =  new Real(type, min, units, errors);
        maxValue =  new Real(type, max, units, errors);
    }

    /**
     * Return the result of a binary operation between this instance and another
     * operand.  If the other operand is an object of this class and the result
     * of the {@link FlatField#binary(Data, int, int, int)} method is a
     * {@link FlatField} from which an object of this class can be constructed,
     * then this method returns an instance of this class with a description
     * determined by the input descriptions and the operation and a time equal
     * to the average times of the input; otherwise, the object resulting from
     * the {@link FlatField#binary} method is returned.
     *
     * @param data                 The other operand of the operation.
     * @param op                   The operation to perform (e.g. {@link
     *                             Data#ADD}, {@link Data#DIVIDE}, etc.).
     * @param samplingMode         The sampling mode.  One of {@link 
     *                             Data#NEAREST_NEIGHBOR} or {@link
     *                             Data#WEIGHTED_AVERAGE}.
     * @param errorMode            The error propagation mode.  One of {@link
     *                             Data#NO_ERRORS}, {@link Data#INDEPENDENT},
     *                             or {@link Data#DEPENDENT}.
     * @return                     The result of the operation on this
     *                             instance and the other operand.
     * @throws VisADException      if a VisAD failure occurs.
     * @throws RemovetException    if a Java RMI failure occurs.
     */
    public Data binary(Data data, int op, int samplingMode, int errorMode) 
        throws VisADException, RemoteException {

        Data result = super.binary(data, op, samplingMode, errorMode);

        if (data instanceof SingleBandedImage && result instanceof FlatField) {

            SingleBandedImage that = (SingleBandedImage)data;
            double            time1 = startTime.getReal().getValue();
            double            time2 = that.getStartTime().getReal().getValue();
            DateTime          time = new DateTime((time1 + time2) / 2);
            String            desc;
            String            desc1 = description;
            String            desc2 = that.getDescription();

            if (desc1.indexOf(' ') != -1)
                desc1 = "(" + desc1 + ")";

            if (desc2.indexOf(' ') != -1)
                desc2 = "(" + desc2 + ")";

            switch (op) {
                case ADD:
                    desc = desc1 + " + " + desc2;
                    break;
                case SUBTRACT:
                    desc = desc1 + " - " + desc2;
                    break;
                case MULTIPLY:
                    desc = desc1 + " * " + desc2;
                    break;
                case DIVIDE:
                    desc = desc1 + " / " + desc2;
                    break;
                case POW:
                    desc = "POW(" + desc1 + ", " + desc2 + ")";
                    break;
                case MAX:
                    desc = "MAX(" + desc1 + ", " + desc2 + ")";
                    break;
                case MIN:
                    desc = "MIN(" + desc1 + ", " + desc2 + ")";
                    break;
                case ATAN:
                    desc = "ATAN2(" + desc1 + ", " + desc2 + ")";
                    break;
                case ATAN2_DEGREES:
                    desc = "ATAN2_DEGREES(" + desc1 + ", " + desc2 + ")";
                    break;
                case REMAINDER:
                    desc = "REMAINDER(" + desc1 + ", " + desc2 + ")";
                    break;
                default:
                    throw new Error("Assertion Failure");
            }

            try {
                result = 
                    new SingleBandedImageImpl((FlatField)result, time, desc, false);
            }
            catch (VisADException ex) {
                // do nothing: return the original result
            }
        }

        return result;
    }

    /**
     * Return the result of a unary operation on this instance.  If the result
     * of the {@link FlatField#unary(int, int, int)} method is a {@link
     * FlatField} from which an object of this class can be constructed,
     * then this method returns an instance of this class with a description
     * determined by the input description and the operation and a time equal
     * to the time of this instance; otherwise, the object resulting from the
     * {@link FlatField#unary} method is returned.
     *
     * @param op                   The operation to perform (e.g. {@link
     *                             Data#ABS}, {@link Data#COS}, etc.).
     * @param samplingMode         The sampling mode.  One of {@link 
     *                             Data#NEAREST_NEIGHBOR} or {@link
     *                             Data#WEIGHTED_AVERAGE}.
     * @param errorMode            The error propagation mode.  One of {@link
     *                             Data#NO_ERRORS}, {@link Data#INDEPENDENT},
     *                             or {@link Data#DEPENDENT}.
     * @return                     The result of the operation on this instance.
     * @throws VisADException      if a VisAD failure occurs.
     * @throws RemovetException    if a Java RMI failure occurs.
     */
    public Data unary(int op, int samplingMode, int errorMode) 
        throws VisADException, RemoteException {

        Data result = super.unary(op, samplingMode, errorMode);

        if (result instanceof FlatField) {

            String            desc = description;

            switch (op) {
                case ABS:
                    desc = "ABS(" + desc + ")";
                    break;
                case ACOS:
                    desc = "ACOS(" + desc + ")";
                    break;
                case ACOS_DEGREES:
                    desc = "ACOS_DEGREES(" + desc + ")";
                    break;
                case ASIN:
                    desc = "ASIN(" + desc + ")";
                    break;
                case ASIN_DEGREES:
                    desc = "ASIN_DEGREES(" + desc + ")";
                    break;
                case ATAN:
                    desc = "ATAN(" + desc + ")";
                    break;
                case ATAN_DEGREES:
                    desc = "ATAN_DEGREES(" + desc + ")";
                    break;
                case CEIL:
                    desc = "CEIL(" + desc + ")";
                    break;
                case COS:
                    desc = "COS(" + desc + ")";
                    break;
                case COS_DEGREES:
                    desc = "COS_DEGREES(" + desc + ")";
                    break;
                case EXP:
                    desc = "EXP(" + desc + ")";
                    break;
                case FLOOR:
                    desc = "FLOOR(" + desc + ")";
                    break;
                case LOG:
                    desc = "LOG(" + desc + ")";
                    break;
                case RINT:
                    desc = "RINT(" + desc + ")";
                    break;
                case ROUND:
                    desc = "ROUND(" + desc + ")";
                    break;
                case SIN:
                    desc = "SIN(" + desc + ")";
                    break;
                case SIN_DEGREES:
                    desc = "SIN_DEGREES(" + desc + ")";
                    break;
                case SQRT:
                    desc = "SQRT(" + desc + ")";
                    break;
                case TAN:
                    desc = "TAN(" + desc + ")";
                    break;
                case TAN_DEGREES:
                    desc = "TAN_DEGREES(" + desc + ")";
                    break;
                case NEGATE:
                    desc = "NEGATE(" + desc + ")";
                    break;
                default:
                    throw new Error("Assertion Failure");
            }

            try {
                result = 
                    new SingleBandedImageImpl((FlatField)result, startTime, desc, false);
            }
            catch (VisADException ex) {
                // do nothing: return the original result
            }
        }

        return result;
    }



    /**
     * for effeciency provide access to the uncopied floats
     */
    public float[][] getImageData() throws VisADException {
	return unpackFloats(false);
    }


}
