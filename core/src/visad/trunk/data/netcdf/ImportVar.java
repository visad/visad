package visad.data.netcdf;


import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.DoubleSet;
import visad.FloatSet;
import visad.Linear1DSet;
import visad.MathType;
import visad.OffsetUnit;
import visad.RealType;
import visad.SI;
import visad.Set;
import visad.TextType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Adapter class for importing a netCDF variable.
 */
abstract class
ImportVar
{
    /**
     * The netCDF dataset.
     */
    protected final Netcdf		netcdf;

    /**
     * The netCDF Variable.
     */
    protected final Variable		var;

    /**
     * The VisAD MathType of the netCDF variable.
     */
    protected /*final*/ MathType	mathType;


    /**
     * Construct from a netCDF variable and dataset.
     */
    ImportVar(Variable var, Netcdf netcdf)
    {
	this.var = var;
	this.netcdf = netcdf;
    }


    /**
     * Factory method for creating an instance of the correct subtype.
     */
    static ImportVar
    create(Variable var, Netcdf netcdf)
	throws VisADException
    {
	Class		type = var.getComponentType();
	ImportVar	ncVar;

	if (NcText.isRepresentable(var))
	    ncVar = new NcText(var, netcdf);
	else
	if (NcByte.isRepresentable(var))
	    ncVar = new NcByte(var, netcdf);
	else
	if (NcShort.isRepresentable(var))
	    ncVar = new NcShort(var, netcdf);
	else
	if (NcInt.isRepresentable(var))
	    ncVar = new NcInt(var, netcdf);
	else
	if (NcFloat.isRepresentable(var))
	    ncVar = new NcFloat(var, netcdf);
	else
	if (NcDouble.isRepresentable(var))
	    ncVar = new NcDouble(var, netcdf);
	else
	    throw new UnsupportedOperationException("Unknown netCDF type");

	return ncVar;
    }


    /**
     * Indicate whether or not a netCDF variable of the given type has
     * a default fill-value.
     *
     * @precondition	<code>type</code> is a valid netCDF type.
     */
    static boolean
    hasDefaultFillValue(Class type)
    {
	return !type.equals(Byte.TYPE);
    }


    /**
     * Return the default fill-value value for a netCDF variable of the
     * given type.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The default fill-value value for the given netCDF type.
     * @exception BadFormException	Unknown netCDF type.
     */
    static double
    getDefaultFillValue(Class type)
	throws BadFormException
    {
	double	value;

	if (type.equals(Character.TYPE))
	    value = 0;
	else
	if (type.equals(Byte.TYPE))
	    value = Double.NaN;		// i.e. no default fill-value
	else
	if (type.equals(Short.TYPE))
	    value = -32767;
	else
	if (type.equals(Integer.TYPE))
	    value = -2147483647;
	else
	if (type.equals(Float.TYPE) || type.equals(Double.TYPE))
	    value = 9.9692099683868690e+36;
	else
	    throw new BadFormException("Unknown netCDF type: " + type);

	return value;
    }


    /**
     * Return the minimum valid value for a netCDF variable of the given
     * type.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The minimum valid value for the given netCDF type.
     * @exception BadFormException	Unknown netCDF type
     */
    static double
    getMinValid(Class type)
	throws BadFormException
    {
	double	value;

	if (type.equals(Character.TYPE))
	    value = 0;
	else
	if (type.equals(Byte.TYPE))
	    value = Byte.MIN_VALUE;
	else
	if (type.equals(Short.TYPE))
	    value = Short.MIN_VALUE;
	else
	if (type.equals(Integer.TYPE))
	    value = Integer.MIN_VALUE;
	else
	if (type.equals(Float.TYPE))
	    value = Float.NEGATIVE_INFINITY;
	else
	if (type.equals(Double.TYPE))
	    value = Double.NEGATIVE_INFINITY;
	else
	    throw new BadFormException("Unknown netCDF type: " + type);

	return value;
    }


    /**
     * Return the maximum valid value for a netCDF variable of the given
     * type.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The maximum valid value for the given netCDF type.
     * @exception BadFormException	Unknown netCDF type
     */
    static double
    getMaxValid(Class type)
	throws BadFormException
    {
	double	value;

	if (type.equals(Character.TYPE))
	    value = 0;
	else
	if (type.equals(Byte.TYPE))
	    value = Byte.MAX_VALUE;
	else
	if (type.equals(Short.TYPE))
	    value = Short.MAX_VALUE;
	else
	if (type.equals(Integer.TYPE))
	    value = Integer.MAX_VALUE;
	else
	if (type.equals(Float.TYPE))
	    value = Float.POSITIVE_INFINITY;
	else
	if (type.equals(Double.TYPE))
	    value = Double.POSITIVE_INFINITY;
	else
	    throw new BadFormException("Unknown netCDF type: " + type);

	return value;
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
	throws VisADException
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
     */
    MathType
    getMathType()
    {
	return mathType;
    }


    /**
     * Indicate whether or not this variable is the same as another.
     */
    public boolean
    equals(Object that)
    {
	return equals((ImportVar)that);
    }


    /**
     * Indicate whether or not this variable is the same as another.
     */
    boolean
    equals(ImportVar that)
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
final class
NcText
    extends ImportVar
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcText.
     */
    static boolean
    isRepresentable(Variable var)
    {
	return var.getComponentType().equals(Character.TYPE);
    }


    /**
     * Construct.
     */
    NcText(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf);
	mathType = new TextType(var.getName());
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


/**
 * Abstract adaptor/decorator class for netCDF arithmetic variables.
 */
abstract class
NcNumber
    extends ImportVar
{
    /**
     * The value vetter.
     */
    protected final Vetter		vetter;

    /**
     * Whether or not the variable is a co-ordinate variable.
     */
    protected final boolean		isCoordVar;

    /**
     * Whether or not the variable is longitude in nature.
     */
    protected final boolean		isLongitude;

    /**
     * Whether or not the variable is temporal in nature.
     */
    protected final boolean		isTime;

    /**
     * A temporal offset unit for comparison purposes.
     */
    static final Unit			offsetTime =
	new OffsetUnit(0.0, SI.second);


    /**
     * Construct.
     */
    NcNumber(Variable var, Netcdf netcdf)
	throws BadFormException
    {
	super(var, netcdf);
	vetter = new Vetter(var);
	isCoordVar = setIsCoordVar();
	isLongitude = setIsLongitude();
	isTime = setIsTime();
	mathType = RealType.getRealTypeByName(getName());
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

	vetter.vet(values);

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

	vetter.vet(values);

	return values;
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
	throws BadFormException
    {
	super(var, netcdf);
    }


    /**
     * Indicate whether or not a netCDF variable can be represented as
     * an integer in the given VisAD range.
     */
    protected static boolean
    isRepresentable(Variable var, int visadMin, int visadMax)
	throws BadFormException
    {
	Class	type = var.getComponentType();

	// Short circuit: check for integer type
	if (!type.equals(Byte.TYPE) && !type.equals(Short.TYPE) &&
	    !type.equals(Integer.TYPE))
	{
	    return false;
	}

	Vetter	vetter = new Vetter(var);

	return vetter.minValid() >= visadMin && vetter.maxValid() <= visadMax;
    }
}


/**
 * Adaptor/decorator class for netCDF byte variables.
 */
final class
NcByte
    extends NcInteger
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcByte.  This is only possible if the netCDF variable doesn't
     * use the value -128 because that's used by VisAD to indicate a
     * "missing" byte range-value.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, -127, 127);
    }


    /**
     * Construct.
     */
    NcByte(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf);

	// TODO: support "units" attribute
	if (mathType == null)
	{
	    mathType = new RealType(getName(), (Unit)null, 
			    new Linear1DSet(RealType.Generic, -127, 127, 255));			
	}
    }


    /**
     * Indicate if this variable is byte.
     */
    boolean
    isByte()
    {
	return true;
    }
}


/**
 * Adaptor/decorator class for netCDF short variables.
 */
final class
NcShort
    extends NcInteger
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcShort.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, Short.MIN_VALUE+1, Short.MAX_VALUE);
    }


    /**
     * Construct.
     */
    NcShort(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf);

	// TODO: support "units" attribute
	if (mathType == null)
	{
	    mathType = new RealType(getName(), (Unit)null, 
			    new Linear1DSet(RealType.Generic,
				Short.MIN_VALUE+1, Short.MAX_VALUE,
				Short.MAX_VALUE - Short.MIN_VALUE));
	}
    }


    /**
     * Indicate if this variable is short.
     */
    boolean
    isShort()
    {
	return true;
    }
}


/**
 * Adaptor/decorator class for 32-bit netCDF integers.
 */
final class
NcInt
    extends NcInteger
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcInt.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, Integer.MIN_VALUE+1, Integer.MAX_VALUE);
    }


    /**
     * Construct.
     */
    NcInt(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf);

	// TODO: support "units" attribute
	if (mathType == null)
	{
	    mathType = new RealType(getName(), (Unit)null, 
			    new FloatSet(RealType.Generic));
	}
    }


    /**
     * Indicate if this variable is a 32-bit netCDF integer.
     */
    boolean
    isInt()
    {
	return true;
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
	throws BadFormException
    {
	super(var, netcdf);
    }
}


/**
 * Adaptor/decorator class for netCDF float variables.
 */
final class
NcFloat
    extends NcReal
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcFloat.
     */
    static boolean
    isRepresentable(Variable var)
    {
	Class	type = var.getComponentType();

	/*
	 * Integer.TYPE is left out of the following because a float cannot
	 * accurately represent all possible integer values.
	 */
	return type.equals(Float.TYPE) ||
		type.equals(Short.TYPE) ||
		type.equals(Byte.TYPE);
    }


    /**
     * Construct.
     */
    NcFloat(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf);

	// TODO: support "units" attribute
	if (mathType == null)
	{
	    mathType = new RealType(getName(), (Unit)null, 
			    new FloatSet(RealType.Generic));
	}
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
final class
NcDouble
    extends NcReal
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcDouble.
     */
    static boolean
    isRepresentable(Variable var)
    {
	Class	type = var.getComponentType();

	return type.equals(Double.TYPE) ||
		type.equals(Float.TYPE) ||
		type.equals(Integer.TYPE) ||
		type.equals(Short.TYPE) ||
		type.equals(Byte.TYPE);
    }


    /**
     * Construct.
     */
    NcDouble(Variable var, Netcdf netcdf)
	throws VisADException
    {
	super(var, netcdf);

	// TODO: support "units" attribute
	if (mathType == null)
	{
	    mathType = new RealType(getName(), (Unit)null, 
			    new DoubleSet(RealType.Generic));
	}
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
