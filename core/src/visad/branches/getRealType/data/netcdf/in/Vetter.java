/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Vetter.java,v 1.8 2001-03-14 17:44:15 steve Exp $
 */

package visad.data.netcdf.in;

import ucar.netcdf.Attribute;
import ucar.netcdf.Variable;
import visad.data.in.OffsetUnpacker;
import visad.data.in.ScaleAndOffsetUnpacker;
import visad.data.in.ScaleUnpacker;
import visad.data.in.ValueRanger;
import visad.data.in.ValueUnpacker;
import visad.data.in.ValueVetter;


/**
 * The Vetter class vets netCDF values, replacing invalid values with their
 * VisAD equivalents.
 */
final class
Vetter
{
    /**
     * The object that vets the raw data.
     */
    private ValueVetter		vetter;

    /**
     * The object that unpacks the vetted data.
     */
    private ValueUnpacker	unpacker;

    /**
     * The object that ranges the unpacked data.
     */
    private ValueRanger		ranger;

    /**
     * The type of the netCDF variable.
     */
    private Class		type;

    /**
     * The minimum, valid, external, netCDF value (won't be NaN).
     */
    private double		minValid;

    /**
     * The maximum, valid, external, netCDF value (won't be NaN).
     */
    private double		maxValid;

    /**
     * The fill-value value.
     */
    private double		fill;


    /**
     * Constructs from nothing.  Protected to ensure use by subclasses
     * only.
     */
    protected
    Vetter()
    {}


    /**
     * Constructs from a netCDF variable type.
     *
     * @param type		The Java type of the netCDF variable (i.e.
     *				float.class, char.class, etc.)
     */
    Vetter(Class type)
    {
	this.type = type;

	if (type.equals(byte.class))
	{
	    fill = Double.NaN;		// i.e. no default fill-value
	    minValid = Byte.MIN_VALUE;
	    maxValid = Byte.MAX_VALUE;
	}
	else if (type.equals(short.class))
	{
	    fill = -32767;
	    minValid = Short.MIN_VALUE;
	    maxValid = Short.MAX_VALUE;
	}
	else if (type.equals(int.class))
	{
	    fill = -2147483647;
	    minValid = Integer.MIN_VALUE;
	    maxValid = Integer.MAX_VALUE;
	}
	else if (type.equals(float.class))
	{
	    fill = 9.9692099683868690e+36;
	    minValid = Float.NEGATIVE_INFINITY;
	    maxValid = Float.POSITIVE_INFINITY;
	}
	else if (type.equals(double.class))
	{
	    fill = 9.9692099683868690e+36;
	    minValid = Double.NEGATIVE_INFINITY;
	    maxValid = Double.POSITIVE_INFINITY;
	}
	else
	{
	    fill = 0;
	    minValid = 0;
	    maxValid = 0;
	}
    }


    /**
     * Constructs from a netCDF variable.
     *
     * @param var		The netCDF variable to be examined.
     */
    Vetter(Variable var)
    {
	this(var.getComponentType());	// set paramters to default values

	Attribute	attr;
	double		missing = Double.NaN;
	double		lower = Double.NEGATIVE_INFINITY;
	double		upper = Double.POSITIVE_INFINITY;

	/*
	 * Set the object that will vet the raw data.
	 */
	{
	    attr = var.getAttribute("_FillValue");
	    if (attr != null)
	    {
		fill = attr.getNumericValue().doubleValue();
		if (fill < 0)
		{
		    lower =
			(type.equals(float.class) || type.equals(double.class))
			    ? fill/2
			    : fill + 1;
		}
		else if (fill > 0)
		{
		    upper =
			(type.equals(float.class) || type.equals(double.class))
			    ? fill/2
			    : fill - 1;
		}
	    }
	    attr = var.getAttribute("missing_value");
	    if (attr != null)
		missing = attr.getNumericValue().doubleValue();
	    vetter = ValueVetter.valueVetter(new double[] {fill, missing});
	}

	/*
	 * Set the object that will unpack the vetted data.
	 */
	{
	    attr = var.getAttribute("scale_factor");
	    double	scale =
		attr == null ? 1 : attr.getNumericValue().doubleValue();
	    attr = var.getAttribute("add_offset");
	    double	offset =
		attr == null ? 0 : attr.getNumericValue().doubleValue();
	    if (scale == scale && scale != 1 && offset == offset && offset != 0)
	    {
		unpacker = ScaleAndOffsetUnpacker.scaleAndOffsetUnpacker(
		    scale, offset);
	    }
	    else if (scale == scale && scale != 1)
	    {
		unpacker = ScaleUnpacker.scaleUnpacker(scale);
	    }
	    else if (offset == offset && offset != 0)
	    {
		unpacker = OffsetUnpacker.offsetUnpacker(offset);
	    }
	    else
	    {
		unpacker = ValueUnpacker.valueUnpacker();
	    }
	}

	/*
	 * Set the object that will range the unpacked data.
	 */
	{
	    attr = var.getAttribute("valid_range");
	    if (attr != null)
	    {
		lower = attr.getNumericValue(0).doubleValue();
		upper = attr.getNumericValue(1).doubleValue();
	    }
	    attr = var.getAttribute("valid_min");
	    if (attr != null)
		lower = attr.getNumericValue().doubleValue();
	    attr = var.getAttribute("valid_max");
	    if (attr != null)
		upper = attr.getNumericValue().doubleValue();
	    ranger = ValueRanger.valueRanger(lower, upper);
	    /*
	     * Account for NaN semantics in the following:
	     */
	    if (minValid < lower)
		minValid = lower;
	    if (maxValid > upper)
		maxValid = upper;
	}
    }


    /**
     * Returns the minimum, valid, netCDF value.
     *
     * @return	The minimum, valid, value for the variable.
     */
    double
    minValid()
    {
	return minValid;
    }


    /**
     * Returns the maximum, valid, netCDF value.
     *
     * @return	The maximum, valid, value for the variable.
     */
    double
    maxValid()
    {
	return maxValid;
    }


    /**
     * Vets the given float values.
     *
     * @param values	The values to be vetted.
     * @postcondition	All invalid values in <code>values</code> have been
     *			replaced with NaN's.
     */
    public void
    vet(float[] values)
    {
	ranger.process(unpacker.process(vetter.process(values)));
    }


    /**
     * Vets the given double values.
     *
     * @param values	The values to be vetted.
     * @postcondition	All invalid values in <code>values</code> have been
     *			replaced with NaN's.
     */
    public void
    vet(double[] values)
    {
	ranger.process(unpacker.process(vetter.process(values)));
    }
}
