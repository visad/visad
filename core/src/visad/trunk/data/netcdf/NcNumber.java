package visad.data.netcdf;


import ucar.netcdf.Attribute;
import ucar.netcdf.Variable;
import visad.data.BadFormException;
import visad.MathType;
import visad.RealType;
import visad.VisADException;


/**
 * Abstract adaptor/decorator class for netCDF arithmetic variables.
 */
public abstract class
NcNumber
    extends NcVar
{
    /**
     * The fill value:
     */
    protected double	fillValue;

    /**
     * The missing-value value:
     */
    protected double	missingValue;

    /**
     * The minimum, valid value:
     */
    protected double	validMin;

    /**
     * The maximum, valid value:
     */
    protected double	validMax;

    /**
     * Whether or not value-vetting is necessary:
     */
    protected boolean	isVettingRequired;


    /**
     * Construct.
     */
    public
    NcNumber(Variable var, NcFile file)
    {
	super(var, file);
	setVettingParameters();
    }


    /**
     * Construct.
     */
    public
    NcNumber(String name, Class type, NcDim[] dims)
    {
	super(name, type, dims);
    }


    /**
     * Set the value-vetting parameters.
     */
    protected void
    setVettingParameters()
    {
	Attribute	attr;

	isVettingRequired = isVettingTheDefault();
	fillValue = getDefaultFillValue();
	missingValue = Double.NaN;
	validMin = getDefaultValidMin(fillValue);
	validMax = getDefaultValidMax(fillValue);

	if ((attr = var.getAttribute("_FillValue")) != null)
	{
	    fillValue = attr.getNumericValue().doubleValue();
	    isVettingRequired = true;
	}

	if ((attr = var.getAttribute("missing_value")) != null)
	{
	    missingValue = attr.getNumericValue().doubleValue();
	    isVettingRequired = true;
	}

	if ((attr = var.getAttribute("valid_range")) != null)
	{
	    validMin = attr.getNumericValue(0).doubleValue();
	    validMax = attr.getNumericValue(1).doubleValue();
	    isVettingRequired = true;
	}
	else
	{
	    if ((attr = var.getAttribute("valid_min")) != null)
	    {
		validMin = attr.getNumericValue().doubleValue();
		isVettingRequired = true;
	    }

	    if ((attr = var.getAttribute("valid_max")) != null)
	    {
		validMax = attr.getNumericValue().doubleValue();
		isVettingRequired = true;
	    }
	}
    }


    /**
     * Indicate whether or not value-vetting is the default.
     */
    protected boolean
    isVettingTheDefault()
    {
	return true;	// default; overridden in NcByte
    }


    /**
     * Return the default fill value.
     */
    protected abstract double
    getDefaultFillValue();


    /**
     * Return the default minimum value.
     */
     protected abstract double
     getDefaultValidMin(double fillValue);


    /**
     * Return the default maximum value.
     */
     protected abstract double
     getDefaultValidMax(double fillValue);


    /**
     * Return the VisAD math type of this variable.
     */
    public MathType
    getMathType()
	throws VisADException
    {
	RealType	mathType = RealType.getRealTypeByName(getName());

	// TODO: support "units" attribute
	if (mathType == null)
	    mathType = new RealType(getName(), null, null);
	
	return mathType;
    }


    /**
     * Indicate whether or not this variable is longitude.
     */
    public boolean
    isLongitude()
    {
	return name.equals("Lon") ||
	       name.equals("lon") ||
	       name.equals("Longitude") ||
	       name.equals("longitude");
    }


    /**
     * Return the rank of the I/O vector.
     */
    protected int
    getIORank()
    {
	int	rank = var.getRank();

	return rank < 1
		? 1
		: rank-1;
    }


    /**
     * Create the buffer to hold the values.
     */
    protected Object
    createValues()
    {
	int	rank = var.getRank();
	int[]	shape = var.getLengths();
	int	npts;

        if (rank == 0)
            npts = 1;
        else
        {
            npts = shape[0];
            for (int i = 1; i < rank; ++i)
                npts *= shape[i];
        }

	return new double[npts];
    }


    /**
     * Vet the values.
     */
    protected void
    vet(Object values)
    {
	if (isVettingRequired)
	{
	    double[]	vals = (double[])values;

	    for (int i = 0; i < vals.length; ++i)
	    {
		double	val = vals[i];

		if (val == missingValue || val == fillValue ||
		    val < validMin || val > validMax)
		{
		    vals[i] = Double.NaN;
		}
	    }
	}
    }


    /**
     * Unpack the values.
     */
    protected void
    unpack(Object values)
    {
	Attribute	attr;
	double		scaleFactor;
	double		addOffset;
	boolean		unpackingRequired = false;

	if ((attr = var.getAttribute("scale_factor")) == null)
	    scaleFactor = 1;
	else
	{
	    scaleFactor = attr.getNumericValue().doubleValue();
	    unpackingRequired = true;
	}

	if ((attr = var.getAttribute("add_offset")) == null)
	    addOffset = 0;
	else
	{
	    addOffset = attr.getNumericValue().doubleValue();
	    unpackingRequired = true;
	}

	if (unpackingRequired)
	{
	    double[]	vals = (double[])values;

	    for (int i = 0; i < vals.length; ++i)
		vals[i] = vals[i]*scaleFactor + addOffset;
	}
    }


    /**
     * Abstract inner class for unraveling a nested, netCDF, arithmetic
     * array.
     */
    public abstract class
    NumberUnraveler
	extends Unraveler
    {
	/**
	 * The value buffer
	 * Effectively "final".
	 */
	protected double[]	buf;


	/**
	 * Construct.
	 */
	protected
	NumberUnraveler(Object values)
	    throws BadFormException
	{
	    super();

	    buf = (double[])values;

	    int		rank = var.getRank();
	    int[]	shape = var.getLengths();

	    ioShape = new int[rank];

	    for (int i = 0; i < rank-1; ++i)
		ioShape[i] = 1;
	    ioShape[rank-1] = shape[rank-1];
	}
    }
}
