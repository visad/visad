package visad.data.netcdf;


import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.MathType;
import visad.OffsetUnit;
import visad.RealType;
import visad.SI;
import visad.TextType;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Adapter class for importing a netCDF variable.
 */
abstract class
NcVar
{
    /**
     * The netCDF dataset.
     */
    protected final Netcdf	netcdf;

    /**
     * The netCDF Variable.
     */
    protected final Variable	var;


    /**
     * Construct from a netCDF variable and dataset.
     */
    NcVar(Variable var, Netcdf netcdf)
    {
	this.var = var;
	this.netcdf = netcdf;
    }


    /**
     * Factory method for creating an instance of the correct subtype.
     */
    static NcVar
    create(Variable var, Netcdf netcdf)
    {
	Class	type = var.getComponentType();
	NcVar	ncVar;

	if (Character.TYPE.equals(type))
	    ncVar = new NcText(var, netcdf);
	else
	if (Byte.TYPE.equals(type))
	    ncVar = new NcByte(var, netcdf);
	else
	if (Short.TYPE.equals(type))
	    ncVar = new NcShort(var, netcdf);
	else
	if (Integer.TYPE.equals(type))
	    ncVar = new NcInt(var, netcdf);
	else
	if (Float.TYPE.equals(type))
	    ncVar = new NcFloat(var, netcdf);
	else
	if (Double.TYPE.equals(type))
	    ncVar = new NcDouble(var, netcdf);
	else
	    throw new UnsupportedOperationException("Unknown netCDF type");

	return ncVar;
    }


    /**
     * Return the rank of this variable.
     */
    int
    getRank()
    {
	return var.getRank();
    }


    /**
     * Return the shape of this variable.
     */
    int[]
    getLengths()
    {
	return var.getLengths();
    }


    /**
     * Return the dimensions of this variable.
     */
    NcDim[]
    getDimensions()
    {
	int			rank = var.getRank();
	NcDim[]			dims = new NcDim[rank];
	DimensionIterator	iter = var.getDimensionIterator();

	for (int i = 0; i < rank; ++i)
	    dims[i] = NcDim.create(iter.next(), netcdf);

	return dims;
    }


    /**
     * Indicate if the values of this variable are textual.
     */
    boolean
    isText()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are byte.
     */
    boolean
    isByte()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are short.
     */
    boolean
    isShort()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are 32-bit *netCDF* integers.
     */
    boolean
    isInt()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are float.
     */
    boolean
    isFloat()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are double.
     */
    boolean
    isDouble()
    {
	return false;
    }


    /**
     * Return the VisAD math type of this variable.
     *
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    abstract MathType
    getMathType()
	throws VisADException;


    /**
     * Indicate whether or not this variable is the same as another.
     */
    public boolean
    equals(Object that)
    {
	return equals((NcVar)that);
    }


    /**
     * Indicate whether or not this variable is the same as another.
     */
    boolean
    equals(NcVar that)
    {
	return var.getName().equals(that.getName());
    }


    /**
     * Return the hash code of this variable information.
     */
    public int
    hashCode()
    {
	return var.getName().hashCode();
    }


    /**
     * Return the name of the variable.
     */
    String
    getName()
    {
	return var.getName();
    }


    /**
     * Convert this variable to a string.
     */
    public String
    toString()
    {
	return var.getName();
    }


    /**
     * Indicate whether or not the variable is a co-ordinate variable.
     */
    abstract boolean
    isCoordinateVariable();


    /**
     * Indicate whether or not the variable is longitude.
     */
    abstract boolean
    isLongitude();


    /**
     * Indicate whether or not the variable is temporal in nature.
     */
    abstract boolean
    isTime();


    /**
     * Return the values of this variable as a packed array of floats.
     *
     * @exception IOException		I/O error.
     */
    abstract float[]
    getFloatValues()
	throws IOException;


    /**
     * Return the values of this variable as a packed array of doubles.
     *
     * @exception IOException		I/O error.
     */
    abstract double[]
    getDoubleValues()
	throws IOException;


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @exception IOException		I/O error.
     */
    abstract double[]
    getDoubleValues(int ipt)
	throws IOException;
}


/**
 * Adaptor/decorator class for netCDF textual variables.
 */
class
NcText
    extends NcVar
{
    /**
     * Construct.
     */
    NcText(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Return the VisAD math type of this variable.
     *
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    MathType
    getMathType()
	throws VisADException
    {
	return new TextType(var.getName());
    }


    /**
     * Indicate if this variable is textual.
     */
    boolean
    isText()
    {
	return true;
    }

    /**
     * Indicate if this variable is longitude.
     */
    boolean
    isLongitude()
    {
	return false;
    }


    /**
     * Indicate whether or not the variable is temporal in nature.
     */
    boolean
    isTime()
    {
	return false;
    }


    /**
     * Indicate whether or not the variable is a co-ordinate variable.
     */
    boolean
    isCoordinateVariable()
    {
	return false;
    }


    /**
     * Return the values of this variable as a packed array of floats.
     *
     * @exception IOException		I/O error.
     */
    float[]
    getFloatValues()
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the values of this variable as a packed array of doubles.
     *
     * @exception IOException		I/O error.
     */
    double[]
    getDoubleValues()
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @exception IOException		I/O error.
     */
    double[]
    getDoubleValues(int ipt)
    {
	throw new UnsupportedOperationException();
    }
}


/**
 * Abstract adaptor/decorator class for netCDF arithmetic variables.
 */
abstract class
NcNumber
    extends NcVar
{
    /**
     * The fill value:
     */
    protected double		fillValue;

    /**
     * The missing-value value:
     */
    protected double		missingValue;

    /**
     * The minimum, valid value:
     */
    protected double		validMin;

    /**
     * The maximum, valid value:
     */
    protected double		validMax;

    /**
     * Whether or not value-vetting is necessary:
     */
    protected boolean		isVettingRequired;

    /**
     * Whether or not the variable is a co-ordinate variable.
     */
    protected final boolean	isCoordVar;

    /**
     * Whether or not the variable is longitude in nature.
     */
    protected final boolean	isLongitude;

    /**
     * Whether or not the variable is temporal in nature.
     */
    protected final boolean	isTime;

    /**
     * A temporal offset unit for comparison purposes.
     */
    static final Unit		offsetTime = new OffsetUnit(0.0, SI.second);


    /**
     * Construct.
     */
    NcNumber(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
	setVettingParameters();
	isCoordVar = setIsCoordVar();
	isLongitude = setIsLongitude();
	isTime = setIsTime();
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
     * Set whether or not the variable is a co-ordinate variable.
     */
    boolean
    setIsCoordVar()
    {
	if (var.getRank() == 1)
	{
	    String		varName = var.getName();
	    DimensionIterator	dimIter = var.getDimensionIterator();

	    while (dimIter.hasNext())
		if (dimIter.next().getName().equals(varName))
		    return true;
	}

	return false;
    }


    /**
     * Set whether or not this variable is longitude.
     */
    boolean
    setIsLongitude()
    {
	String	varName = var.getName();

	return varName.equals("Lon") ||
	       varName.equals("lon") ||
	       varName.equals("Longitude") ||
	       varName.equals("longitude");
    }


    /**
     * Set whether or not this variable is temporal in nature.
     */
    protected boolean
    setIsTime()
    {
	Attribute	attr = var.getAttribute("units");

	if (attr != null && attr.isString())
	{
	    try
	    {
		Unit	unit = Parser.parse((String)attr.getValue());

		if (Unit.canConvert(unit, SI.second) ||
		    Unit.canConvert(unit, offsetTime))
		{
		    return true;
		}
	    }
	    catch (ParseException e)
	    {}
	}

	return false;
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
     *
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    MathType
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
     * Indicate whether or not the variable is a co-ordinate variable.
     */
    boolean
    isCoordinateVariable()
    {
	return isCoordVar;
    }


    /**
     * Indicate whether or not this variable is longitude.
     */
    boolean
    isLongitude()
    {
	return isLongitude;
    }


    /**
     * Indicate whether or not the variable is temporal in nature.
     */
    boolean
    isTime()
    {
	return isTime;
    }


    /**
     * Return the values of this variable as a packed array of floats.
     *
     * @exception IOException		I/O error.
     */
    float[]
    getFloatValues()
	throws IOException
    {
	int[]	lengths = var.getLengths();
	int	npts = 1;

	for (int i = 0; i < lengths.length; ++i)
	    npts *= lengths[i];

	float[]		values = new float[npts];
	IndexIterator	iter = new IndexIterator(lengths);

	for (int i = 0; i < npts; ++i)
	{
	    values[i] = var.getFloat(iter.value());
	    iter.incr();
	}

	vet(values);

	return values;
    }


    /**
     * Compute the number of points in a shape vector.
     */
    protected static int
    product(int[] shape)
    {
	int	npts = 1;

	for (int i = 0; i < shape.length; ++i)
	    npts *= shape[i];

	return npts;
    }


    /**
     * Return all the values of this variable as a packed array of doubles.
     *
     * @exception IOException		I/O error.
     */
    double[]
    getDoubleValues()
	throws IOException
    {
	return getDoubleValues(var);
    }


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @precondition	The variable is rank 2 or greater.
     * @precondition	<code>ipt</code> lies within the outermost dimension.
     *
     * @exception IOException		I/O error.
     */
    double[]
    getDoubleValues(int ipt)
	throws IOException
    {
	int	rank = var.getRank();
	int[]	origin = new int[rank];
	int[]	lengths = var.getLengths();

	for (int idim = 1; idim < rank; ++idim)
	    origin[idim] = 0;
	origin[0] = ipt;
	lengths[0] = 1;

	return getDoubleValues(var.copyout(origin, lengths));
    }


    /**
     * Return a selected subset of the double values of this variable as a
     * packed array of doubles.
     */
    protected double[]
    getDoubleValues(MultiArray ma)
	throws IOException
    {
	int[]		lengths = ma.getLengths();
	int		npts = product(lengths);
	double[]	values = new double[npts];
	IndexIterator	iter = new IndexIterator(lengths);

	for (int i = 0; i < npts; ++i)
	{
	    values[i] = ma.getDouble(iter.value());
	    iter.incr();
	}

	vet(values);

	return values;
    }


    /**
     * Vet values.
     */
    protected void
    vet(float[] values)
    {
	if (isVettingRequired)
	{
	    for (int i = 0; i < values.length; ++i)
	    {
		float  val = values[i];

		if (val == missingValue || val == fillValue ||
		    val < validMin || val > validMax)
		{
		    values[i] = Float.NaN;
		}
	    }
	}
    }


    /**
     * Vet values.
     */
    protected void
    vet(double[] values)
    {
	if (isVettingRequired)
	{
	    for (int i = 0; i < values.length; ++i)
	    {
		double  val = values[i];

		if (val == missingValue || val == fillValue ||
		    val < validMin || val > validMax)
		{
		    values[i] = Double.NaN;
		}
	    }
	}
    }
}


/**
 * Abstract adaptor/decorator class for netCDF integer variables.
 */
abstract class
NcInteger
    extends NcNumber
{
    /**
     * Construct.
     */
    NcInteger(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Return the default, minimum valid value.
     */
     protected double
     getDefaultValidMin(double fillValue)
     {
	return fillValue < 0
		    ? fillValue + 1
		    : getMinValue();
     }


    /**
     * Return the default, maximum valid value.
     */
     protected double
     getDefaultValidMax(double fillValue)
     {
	return fillValue > 0
		    ? fillValue - 1
		    : getMaxValue();
     }


     /**
      * Return the minimum possible value.
      */
    protected abstract double
    getMinValue();


     /**
      * Return the maximum possible value.
      */
    protected abstract double
    getMaxValue();
}


/**
 * Adaptor/decorator class for netCDF byte variables.
 */
class
NcByte
    extends NcInteger
{
    /**
     * Construct.
     */
    NcByte(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Indicate whether or not value-vetting is the default.
     */
    protected boolean
    isVettingTheDefault()
    {
	return false;
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return Double.MAX_VALUE;	// there is no default fill value
    }


    /**
     * Indicate if this variable is byte.
     */
    boolean
    isByte()
    {
	return true;
    }


    /**
     * Return the default, minimum valid value.
     */
     protected double
     getDefaultValidMin(double fillValue)
     {
	return getMinValue();
     }


    /**
     * Return the default, maximum valid value.
     */
     protected double
     getDefaultValidMax(double fillValue)
     {
	return getMaxValue();
     }


     /**
      * Return the minimum possible value.
      */
    protected double
    getMinValue()
    {
	return Byte.MIN_VALUE;
    }


     /**
      * Return the maximum possible value.
      */
    protected double
    getMaxValue()
    {
	return Byte.MAX_VALUE;
    }
}


/**
 * Adaptor/decorator class for netCDF short variables.
 */
class
NcShort
    extends NcInteger
{
    /**
     * Construct.
     */
    NcShort(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Indicate if this variable is short.
     */
    boolean
    isShort()
    {
	return true;
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return -32767;
    }


     /**
      * Return the minimum possible value.
      */
    protected double
    getMinValue()
    {
	return Short.MIN_VALUE;
    }


     /**
      * Return the maximum possible value.
      */
    protected double
    getMaxValue()
    {
	return Short.MAX_VALUE;
    }
}


/**
 * Adaptor/decorator class for 32-bit netCDF integers.
 */
class
NcInt
    extends NcInteger
{
    /**
     * Construct.
     */
    NcInt(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Indicate if this variable is a 32-bit netCDF integer.
     */
    boolean
    isInt()
    {
	return true;
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return -2147483647;
    }


     /**
      * Return the minimum possible value.
      */
    protected double
    getMinValue()
    {
	return Integer.MIN_VALUE;
    }


     /**
      * Return the maximum possible value.
      */
    protected double
    getMaxValue()
    {
	return Integer.MAX_VALUE;
    }
}


/**
 * Abstract adaptor/decorator class for netCDF floating-point variables.
 */
abstract class
NcReal
    extends NcNumber
{
    /**
     * Construct.
     */
    NcReal(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Return the default fill value.
     */
    protected double
    getDefaultFillValue()
    {
	return 9.9692099683868690e+36;
    }


    /**
     * Return the default minimum value.
     */
     protected double
     getDefaultValidMin(double fillValue)
     {
	return fillValue < 0
		    ? fillValue/2
		    : -Double.MAX_VALUE;
     }


    /**
     * Return the default maximum value.
     */
     protected double
     getDefaultValidMax(double fillValue)
     {
	return fillValue > 0
		    ? fillValue/2
		    : Double.MAX_VALUE;
     }
}


/**
 * Adaptor/decorator class for netCDF float variables.
 */
class
NcFloat
    extends NcReal
{
    /**
     * Construct.
     */
    NcFloat(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Indicate if this variable is float.
     */
    boolean
    isFloat()
    {
	return true;
    }
}


/**
 * Adaptor/decorator class for netCDF double variables.
 */
class
NcDouble
    extends NcReal
{
    /**
     * Construct.
     */
    NcDouble(Variable var, Netcdf netcdf)
    {
	super(var, netcdf);
    }


    /**
     * Indicate if this variable is double.
     */
    boolean
    isDouble()
    {
	return true;
    }
}
