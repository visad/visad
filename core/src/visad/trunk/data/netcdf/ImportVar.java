/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ImportVar.java,v 1.5 1998-03-10 19:49:34 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.CoordinateSystem;
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
     * The range set of the netCDF variable.
     */
    protected /*final*/ Set		set;

    /**
     * The units of the netCDF variable.
     */
    protected final Unit		unit;


    /**
     * Construct from a netCDF variable and dataset.
     */
    ImportVar(Variable var, Netcdf netcdf)
    {
	this.var = var;
	this.netcdf = netcdf;
	unit = setUnit();
    }


    /**
     * Determine the units of the netCDF variable.
     *
     * @return	The units of the variable or <code>null</code> if there
     *		was no "unit" attribute or an error occurred during parsing.
     */
    private Unit
    setUnit()
    {
	Unit		unit = null;
	Attribute	attr = var.getAttribute("units");

	if (attr == null)
	    attr = var.getAttribute("unit");

	if (attr != null && attr.isString())
	{
	    try
	    {
		unit = Parser.parse(attr.getStringValue());
	    }
	    catch (ParseException e)
	    {
	    }
	}

	return unit;
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
     * Get the units of this variable.
     *
     * @return	The unit of the variable or <code>null</code> if there isn't
     *		one.
     */
    Unit
    getUnit()
    {
	return unit;
    }


    /**
     * Get the range set of this variable.
     *
     * @return	The range set of the variable.
     */
    Set
    getSet()
    {
	return set;
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
	set = null;
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
	throws BadFormException, VisADException
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

	/*
	 * The following is complicated due to the circular dependency
	 * between MathType and Set.
	 */

	RealType	realType = mathType == null
	    ? new RealType(getName(), unit, (Set)null)
	    : (RealType)mathType;

	set = new Linear1DSet(realType, Byte.MIN_VALUE+1, Byte.MAX_VALUE, 
		    Byte.MAX_VALUE - Byte.MIN_VALUE);

	if (mathType == null)
	{
	    realType.setDefaultSet(set);
	    mathType = realType;
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

	/*
	 * The following is complicated due to the circular dependency
	 * between MathType and Set.
	 */

	RealType	realType = mathType == null
	    ? new RealType(getName(), unit, (Set)null)
	    : (RealType)mathType;

	set = new Linear1DSet(realType, Short.MIN_VALUE+1, Short.MAX_VALUE, 
		    Short.MAX_VALUE - Short.MIN_VALUE);

	if (mathType == null)
	{
	    realType.setDefaultSet(set);
	    mathType = realType;
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

	/*
	 * The following is complicated due to the circular dependency
	 * between MathType and Set.
	 */

	RealType	realType = mathType == null
	    ? new RealType(getName(), unit, (Set)null)
	    : (RealType)mathType;

	/*
	 * The following is complicated due to the fact that the last
	 * argument to the Linear2DSet() constructor:
	 *
	 *     Linear1DSet(MathType type, double start, double stop, int length)
	 *
	 * is an "int" -- and the number of Java "int" values cannot
	 * be represented by a Java "int".
	 */
	{
	    Vetter	vetter = new Vetter(var);
	    int		minValid = (int)vetter.minValid();
	    int		maxValid = (int)vetter.maxValid();
	    long	length	= maxValid - minValid + 1;
	    set = length <= Integer.MAX_VALUE
		    ? (Set)(new Linear1DSet(realType, minValid, maxValid, 
					    (int)length))
		    : (Set)(new FloatSet(realType, (CoordinateSystem)null,
					new Unit[] {unit}));
	}

	if (mathType == null)
	{
	    realType.setDefaultSet(set);
	    mathType = realType;
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
	throws BadFormException, VisADException
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
         * Integer.TYPE is left out of the following because a Java
         * Float has 24 bits of precision and, consequently, cannot
         * accurately represent all possible 32-bit integer values.
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

	/*
	 * The following is complicated due to the circular dependency
	 * between MathType and Set.
	 */

	RealType	realType = mathType == null
	    ? new RealType(getName(), unit, (Set)null)
	    : (RealType)mathType;

	set = new FloatSet(realType, (CoordinateSystem)null,
		    new Unit[] {unit});

	if (mathType == null)
	{
	    realType.setDefaultSet(set);
	    mathType = realType;
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

	/*
	 * The following is complicated due to the circular dependency
	 * between MathType and Set.
	 */

	RealType	realType = mathType == null
	    ? new RealType(getName(), unit, (Set)null)
	    : (RealType)mathType;

	set = new DoubleSet(realType, (CoordinateSystem)null,
		    new Unit[] {unit});

	if (mathType == null)
	{
	    realType.setDefaultSet(set);
	    mathType = realType;
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
