package visad.data.netcdf;


import ucar.netcdf.Attribute;
import ucar.netcdf.Variable;
import visad.data.BadFormException;


/**
 * A class for vetting values.
 */
final class
Vetter
{
    /**
     * The type of the netCDF variable.
     */
    protected Class		type;

    /**
     * The minimum, valid, external, netCDF value (won't be NaN).
     */
    protected double		minValid;

    /**
     * The maximum, valid, external, netCDF value (won't be NaN).
     */
    protected double		maxValid;

    /**
     * The fill-value value.
     */
    protected double		fillValue;

    /**
     * The missing-value value.
     */
    protected double		missingValue = Double.NaN;

    /**
     * The minimum, valid value for vetting.
     */
    protected double		lowerVettingLimit = Double.NEGATIVE_INFINITY;

    /**
     * The maximum, valid value for vetting.
     */
    protected double		upperVettingLimit = Double.POSITIVE_INFINITY;

    /**
     * Whether or not the vetting is trivial (i.e. all values are valid).
     */
    protected boolean		isTrivial;


    /**
     * Construct from a netCDF variable type.
     */
    Vetter(Class type)
	throws BadFormException
    {
	this.type = type;

	fillValue = ImportVar.getDefaultFillValue(type);
	minValid = ImportVar.getMinValid(type);
	maxValid = ImportVar.getMaxValid(type);

	isTrivial = isTrivial();
    }


    /**
     * Construct from a netCDF variable.
     */
    Vetter(Variable var)
	throws BadFormException
    {
	this(var.getComponentType());

	Attribute	attr;

	attr = var.getAttribute("_FillValue");
	if (attr != null)
	{
	    fillValue = attr.getNumericValue().doubleValue();
	    if (fillValue < 0)
	    {
		lowerVettingLimit = type == Float.TYPE || type == Double.TYPE
			    ? fillValue/2
			    : fillValue + 1;
	    }
	    else
	    if (fillValue > 0)
	    {
		upperVettingLimit = type == Float.TYPE || type == Double.TYPE
			    ? upperVettingLimit = fillValue/2
			    : upperVettingLimit = fillValue - 1;
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

	isTrivial = isTrivial();
    }


    /**
     * Return the minimum, valid, netCDF value.
     */
    double
    minValid()
    {
	return minValid;
    }


    /**
     * Return the maximum, valid, netCDF value.
     */
    double
    maxValid()
    {
	return maxValid;
    }


    /**
     * Indicate if trivial vetting will occur (i.e. all values are valid).
     */
    private boolean
    isTrivial()
    {
	return Double.isNaN(fillValue) &&
	       Double.isNaN(missingValue) &&
	       Double.isInfinite(lowerVettingLimit) && lowerVettingLimit < 0 &&
	       Double.isInfinite(upperVettingLimit) && upperVettingLimit > 0;
    }


    /**
     * Indicate whether or not the given value is valid.
     *
     * @precondition	The value comes from a netCDF variable of the type
     *			used in the constructor.
     */
    private boolean
    isInvalid(double value)
    {
	// Carefully account for possible NaN semantics in the following
	// expresion.
	return
	    Double.isNaN(value) ||
	    value == fillValue ||
	    value == missingValue ||
	    value < lowerVettingLimit ||
	    value > upperVettingLimit;
    }


    /**
     * Vet the given float values.
     *
     * @precondition	The values come from a netCDF variable of the type
     *			used in the constructor.
     */
    void
    vet(float[] values)
    {
	if (!isTrivial)
	{
	    for (int i = 0; i < values.length; ++i)
		if (isInvalid(values[i]))
		    values[i] = Float.NaN;
	}
    }


    /**
     * Vet the given double values.
     *
     * @precondition	The values come from a netCDF variable of the type
     *			used in the constructor.
     */
    void
    vet(double[] values)
    {
	if (!isTrivial)
	{
	    for (int i = 0; i < values.length; ++i)
		if (isInvalid(values[i]))
		    values[i] = Double.NaN;
	}
    }
}
