/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcVar.java,v 1.1 1998-03-20 20:57:05 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.CoordinateSystem;
import visad.Data;
import visad.DataImpl;
import visad.DoubleSet;
import visad.FloatSet;
import visad.Linear1DSet;
import visad.MathType;
import visad.OffsetUnit;
import visad.RealType;
import visad.SI;
import visad.Set;
import visad.Text;
import visad.TextType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * The NcVar class provides an abstract class for decorating a netCDF
 * variable that's being imported to a VisAD API.
 */
abstract class
NcVar
    extends	NcData
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
     * The range set of the netCDF variable.
     */
    protected /*final*/ Set		set;

    /**
     * The units of the netCDF variable.
     */
    protected final Unit		unit;


    /**
     * Construct from a netCDF variable and dataset.
     *
     * @param var	The netCDF variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     */
    NcVar(Variable var, Netcdf netcdf)
    {
	this.var = var;
	this.netcdf = netcdf;
	unit = getUnit(var);
    }


    /**
     * Determine the units of the netCDF variable.
     *
     * @param var	The netCDF variable to have it's units returned.
     * @return		The units of the variable or <code>null</code> if there
     *			was no "unit" attribute or an error occurred during 
     *			parsing.
     */
    private static Unit
    getUnit(Variable var)
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
     *
     * @param var	The netCDF variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @return		The NcVar for <code>var</code>.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    static NcVar
    create(Variable var, Netcdf netcdf)
	throws VisADException
    {
	Class	type = var.getComponentType();
	NcVar	ncVar;

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
     * @param type	The Java class of the netCDF variable (i.e.
     *			<code>Double.TYPE</code>, <code>Byte.TYPE</code>,
     *			<code>Character.TYPE</code>, etc.).
     * @return		<code>true</code> if and only if <code>type</code>
     *			has a default, netCDF fill-value.
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
     * @exception BadFormException
     *			Unknown netCDF type.
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
     * @exception BadFormException
     *			Unknown netCDF type.
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
     * @exception BadFormException
     *			Unknown netCDF type
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
     * Return the VisAD rank of this variable.
     *
     * @return	The VisAD rank of the variable.
     */
    abstract int
    getRank();


    /**
     * Return the shape of this variable.
     *
     * @return		The length of each netCDF dimension of the variable.
     * @postcondition	<code>getRank() == getLengths().length</code>.
     */
    int[]
    getLengths()
    {
	/*
	 * The following algorithm handles both numeric and textual 
	 * netCDF variables.
	 */

	int[]	lengths = new int[getRank()];

	System.arraycopy(var.getLengths(), 0, lengths, 0, lengths.length);

	return lengths;
    }


    /**
     * Return the dimensions of this variable.
     *
     * @return		The dimensions of the variable.
     * @postcondition	<code>getRank() == getDimensions().length</code>.
     */
    NcDim[]
    getDimensions()
	throws VisADException
    {
	/*
	 * The following algorithm handles both numeric and textual 
	 * netCDF variables.
	 */

	int			rank = getRank();
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
     *
     * @return	<code>true</code> if and only if the values of the variable
     *		are textual.
     */
    boolean
    isText()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are byte.
     *
     * @return	<code>true</code> if and only if the values of the variable
     *		are byte-valued.
     */
    boolean
    isByte()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are short.
     *
     * @return	<code>true</code> if and only if the values of the variable
     *		are short-valued.
     */
    boolean
    isShort()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are 32-bit *netCDF* integers.
     *
     * @return	<code>true</code> if and only if the values of the variable
     *		are int-valued.
     */
    boolean
    isInt()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are float.
     *
     * @return	<code>true</code> if and only if the values of the variable
     *		are float-valued.
     */
    boolean
    isFloat()
    {
	return false;
    }


    /**
     * Indicate if the values of this variable are double.
     *
     * @return	<code>true</code> if and only if the values of the variable
     *		are double-valued.
     */
    boolean
    isDouble()
    {
	return false;
    }


    /**
     * Indicate whether or not this variable is the same as another.
     *
     * @return	<code>true</code> if and only if the variable and another
     *		object are semantically identical.
     */
    public boolean
    equals(Object that)
    {
	return equals((NcVar)that);
    }


    /**
     * Indicate whether or not this variable is the same as another.
     *
     * @return	<code>true</code> if and only if the variable and another
     *		NcVar are semantically identical.
     */
    boolean
    equals(NcVar that)
    {
	return var.getName().equals(that.getName());
    }


    /**
     * Return the hash code of this variable information.
     *
     * @return	The hash code of the variable.
     */
    public int
    hashCode()
    {
	return var.getName().hashCode();
    }


    /**
     * Return the name of the variable.
     *
     * @return	The name of the variable.
     */
    String
    getName()
    {
	return var.getName();
    }


    /**
     * Convert this variable to a string.
     *
     * @return	The variable represented as a string.
     */
    public String
    toString()
    {
	return var.getName();
    }


    /**
     * Indicate whether or not the variable is a co-ordinate variable.
     *
     * @return	<code>true</code> if and only if the variable is a coordinate
     *		variable.
     */
    abstract boolean
    isCoordinateVariable();


    /**
     * Indicate whether or not the variable is longitude.
     *
     * @return	<code>true</code> if and only if the variable represents
     *		longitude.
     */
    abstract boolean
    isLongitude();


    /**
     * Indicate whether or not the variable is temporal in nature.
     *
     * @return	<code>true</code> if and only if the variable represents
     *		time.
     */
    abstract boolean
    isTime();


    /**
     * Return the values of this variable as a packed array of floats.
     *
     * @return			The variable's values.
     * @exception IOException	I/O error.
     */
    abstract float[]
    getFloatValues()
	throws IOException;


    /**
     * Return the values of this variable as a packed array of doubles.
     *
     * @return			The variable's values.
     * @exception IOException	I/O error.
     */
    abstract double[]
    getDoubleValues()
	throws IOException;


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @return			The variable's values.
     * @exception IOException	I/O error.
     */
    abstract double[]
    getDoubleValues(int ipt)
	throws IOException;

    /**
     * Return the variable as a VisAD data object.
     *
     * @return			The VisAD data object corresponding to the
     *				Variable.
     * @exception IOException   I/O error.
     */
    /*
    abstract Data
    getData()
	throws IOException;
     */
}


/**
 * The NcInteger class provides an abstract class for adapting an integral
 * netCDF variable that's being imported to a VisAD API.
 */
abstract class
NcInteger
    extends NcNumber
{
    /**
     * Construct.
     *
     * @param var	The netCDF integer variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @precondition	<code>isRepresentable(var).
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcInteger(Variable var, Netcdf netcdf)
	throws BadFormException, VisADException
    {
	super(var, netcdf);
    }


    /**
     * Indicate whether or not a netCDF variable can be represented as
     * an integer in the given VisAD range.
     *
     * @param var	The netCDF variable to be examined.
     * @param visadMin	The minimum representable VisAD value.
     * @param visadMax	The maximum representable VisAD value.
     * @return		<code>true</code> if and only if the netCDF variable
     *			can be represented within the given range.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
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
 * The NcByte class adapts a netCDF byte variable that's being
 * imported to a VisAD API.
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
     *
     * @param var	The netCDF variable to be examined.
     * @return		<code>true</code> if and only if <code>var</code> can
     *			be represented as bytes.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, -127, 127);
    }


    /**
     * Construct.
     *
     * @param var	The netCDF byte variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @precondition	<code>isRepresentable(var).
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
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
     *
     * @return	<code>true</code> always.
     */
    boolean
    isByte()
    {
	return true;
    }


    /**
     * Return the corresponding VisAD data object.
     */
    DataImpl
    getData()
    {
	return null;	// TODO
    }
}


/**
 * The NcShort class adapts a netCDF short variable that's being
 * imported to a VisAD API.
 */
final class
NcShort
    extends NcInteger
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcShort.
     *
     * @param var	The netCDF variable to be examined.
     * @return		<code>true</code> if and only if <code>var</code> can 
     *			be represented as short values.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, Short.MIN_VALUE+1, Short.MAX_VALUE);
    }


    /**
     * Construct.
     *
     * @param var	The netCDF short variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @precondition	<code>isRepresentable(var).
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
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
     *
     * @return	<code>true</code> always.
     */
    boolean
    isShort()
    {
	return true;
    }


    /**
     * Return the corresponding VisAD data object.
     */
    DataImpl
    getData()
    {
	return null;	// TODO
    }
}


/**
 * The NcInt class adapts a 32-bit netCDF variable that's being
 * imported to a VisAD API.
 */
final class
NcInt
    extends NcInteger
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcInt.
     *
     * @param var	The netCDF variable to be examined.
     * @return		<code>true</code> if and only if <code>var</code> can
     *			be represented in 32-bit values.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, Integer.MIN_VALUE+1, Integer.MAX_VALUE);
    }


    /**
     * Construct.
     *
     * @param var	The 32-bit netCDF variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @precondition	<code>isRepresentable(var).
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
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
	    long	minValid = (long)vetter.minValid();
	    long	maxValid = (long)vetter.maxValid();
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
     *
     * @return	<code>true</code> always.
     */
    boolean
    isInt()
    {
	return true;
    }


    /**
     * Return the corresponding VisAD data object.
     */
    DataImpl
    getData()
    {
	return null;	// TODO
    }
}


/**
 * The NcReal class provides an abstract class for adapting a floating-point
 * netCDF variable that's being imported to a VisAD API.
 */
abstract class
NcReal
    extends NcNumber
{
    /**
     * Construct.
     *
     * @param var	The netCDF variable that's being adapted.
     * @parm netcdf	The netCDF dataset that contains <code>var</code>.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcReal(Variable var, Netcdf netcdf)
	throws BadFormException, VisADException
    {
	super(var, netcdf);
    }
}


/**
 * The NcFloat class adapts a netCDF float variable that's being
 * imported to a VisAD API.
 */
final class
NcFloat
    extends NcReal
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcFloat.
     *
     * @param var	The netCDF variable to be examined.
     * @return		<code>true</code> if and only if <code>var</code> can
     *			be represented as Java Float's.
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
     *
     * @param var	The netCDF variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @precondition	<code>isRepresentable(var).
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
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


    /**
     * Return the corresponding VisAD data object.
     */
    DataImpl
    getData()
    {
	return null;	// TODO
    }
}


/**
 * The NcDouble class adapts a netCDF double variable that's being
 * imported to a VisAD API.
 */
final class
NcDouble
    extends NcReal
{
    /**
     * Indicate whether or not a netCDF variable can be represented as 
     * an NcDouble.
     *
     * @param var	The netCDF variable to be examined.
     * @return		<code>true</code> if and only if <code>var</code> is
     *			representable as doubles.
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
     *
     * @param var	The netCDF variable to be adapted.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @precondition	<code>isRepresentable(var).
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
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
     *
     * @return	<code>true</code> always.
     */
    boolean
    isDouble()
    {
	return true;
    }


    /**
     * Return the corresponding VisAD data object.
     */
    DataImpl
    getData()
    {
	return null;	// TODO
    }
}
