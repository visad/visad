/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Vetter.java,v 1.7 2000-04-26 15:45:19 dglo Exp $
 */

package visad.data.netcdf.in;

import ucar.netcdf.Attribute;
import ucar.netcdf.Variable;


/**
 * The Vetter class vets netCDF values, replacing invalid values with their
 * VisAD equivalents.
 */
final class
Vetter
{
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
    private double		fillValue;

    /**
     * The missing-value value.
     */
    private double		missingValue = Double.NaN;

    /**
     * The minimum, valid value for vetting.
     */
    private double		lowerVettingLimit = Double.NEGATIVE_INFINITY;

    /**
     * The maximum, valid value for vetting.
     */
    private double		upperVettingLimit = Double.POSITIVE_INFINITY;

    /**
     * Whether or not value-vetting should occur.
     */
    private boolean		doVet;


    /**
     * Constructs from nothing.  Protected to ensure use by subclasses
     * only.
     */
    protected
    Vetter()
    {
    }


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
	    fillValue = Double.NaN;		// i.e. no default fill-value
	    minValid = Byte.MIN_VALUE;
	    maxValid = Byte.MAX_VALUE;
	}
	else if (type.equals(short.class))
	{
	    fillValue = -32767;
	    minValid = Short.MIN_VALUE;
	    maxValid = Short.MAX_VALUE;
	}
	else if (type.equals(int.class))
	{
	    fillValue = -2147483647;
	    minValid = Integer.MIN_VALUE;
	    maxValid = Integer.MAX_VALUE;
	}
	else if (type.equals(float.class))
	{
	    fillValue = 9.9692099683868690e+36;
	    minValid = Float.NEGATIVE_INFINITY;
	    maxValid = Float.POSITIVE_INFINITY;
	}
	else if (type.equals(double.class))
	{
	    fillValue = 9.9692099683868690e+36;
	    minValid = Double.NEGATIVE_INFINITY;
	    maxValid = Double.POSITIVE_INFINITY;
	}
	else
	{
	    fillValue = 0;
	    minValid = 0;
	    maxValid = 0;
	}

	doVet = doVet();
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

	attr = var.getAttribute("_FillValue");
	if (attr != null)
	{
	    fillValue = attr.getNumericValue().doubleValue();
	    if (fillValue < 0)
	    {
		lowerVettingLimit =
			(type.equals(float.class) || type.equals(double.class))
			    ? fillValue/2
			    : fillValue + 1;
	    }
	    else
	    if (fillValue > 0)
	    {
		upperVettingLimit =
			(type.equals(float.class) || type.equals(double.class))
			    ? fillValue/2
			    : fillValue - 1;
	    }
	}

	attr = var.getAttribute("missing_value");
	if (attr != null)
	    missingValue = attr.getNumericValue().doubleValue();

	attr = var.getAttribute("valid_range");
	if (attr != null)
	{
	    lowerVettingLimit = attr.getNumericValue(0).doubleValue();
	    upperVettingLimit = attr.getNumericValue(1).doubleValue();
	}

	attr = var.getAttribute("valid_min");
	if (attr != null)
	    lowerVettingLimit = attr.getNumericValue().doubleValue();

	attr = var.getAttribute("valid_max");
	if (attr != null)
	    upperVettingLimit = attr.getNumericValue().doubleValue();

	// Account for NaN semantics in the following:
	if (minValid < lowerVettingLimit)
	    minValid = lowerVettingLimit;
	if (maxValid > upperVettingLimit)
	    maxValid = upperVettingLimit;

	doVet = doVet();
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
     * Indicates whether or not value-vetting should occur.
     *
     * @return		<code>true</code> if and only if all possible values
     *			are valid.
     */
    protected boolean
    doVet()
    {
	return !Double.isNaN(fillValue) ||
	       !Double.isNaN(missingValue) ||
	       !Double.isInfinite(lowerVettingLimit) ||
	       !(lowerVettingLimit < 0) ||
	       !Double.isInfinite(upperVettingLimit) ||
	       !(upperVettingLimit > 0);
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
	if (doVet)
	{
	    for (int i = 0; i < values.length; ++i)
	    {
		if (values[i] != values[i] ||  // test for Float.NaN
		    values[i] == fillValue ||
		    values[i] == missingValue ||
		    values[i] < lowerVettingLimit ||
		    values[i] > upperVettingLimit)
		{
		    values[i] = Float.NaN;
		}
	    }
	}
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
	if (doVet)
	{
	    for (int i = 0; i < values.length; ++i)
	    {
		if (values[i] != values[i] ||  // test for Double.NaN
		    values[i] == fillValue ||
		    values[i] == missingValue ||
		    values[i] < lowerVettingLimit ||
		    values[i] > upperVettingLimit)
		{
		    values[i] = Double.NaN;
		}
	    }
	}
    }
}
