/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcVar.java,v 1.10 1998-09-16 15:06:39 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.CoordinateSystem;
import visad.Data;
import visad.DoubleSet;
import visad.FloatSet;
import visad.Linear1DSet;
import visad.MathType;
import visad.OffsetUnit;
import visad.RealType;
import visad.SI;
import visad.ScalarType;
import visad.Set;
import visad.SimpleSet;
import visad.Text;
import visad.TextType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Provides an abstract class for adapting a netCDF
 * variable that's being imported to a VisAD API.
 */
abstract class
NcVar
{
    /**
     * The netCDF dataset.
     */
    private final Netcdf	netcdf;

    /**
     * The netCDF Variable.
     */
    private final Variable	var;

    /**
     * The adapted, VisAD dimensions of the variable in netCDF order.
     */
    private final NcDim[]	dims;

    /**
     * The units of the netCDF variable.
     */
    private final Unit		unit;

    /**
     * The VisAD MathType of the variable's values.
     */
    private final ScalarType	mathType;

    /**
     * A cache of netCDF variables their units.
     */
    private static final Map	unitMap = 
	Collections.synchronizedMap(new WeakHashMap());

    /**
     * A cache of netCDF variables and their long names.
     */
    private static final Map	longNameMap = 
	Collections.synchronizedMap(new WeakHashMap());

    /**
     * A cache of NcVars.
     */
    private static final Map	cache = 
	Collections.synchronizedMap(new WeakHashMap());


    /**
     * Constructs from another NcVar.  Protected to ensure use by
     * trusted subclasses only.
     *
     * @param ncVar		The other NcVar.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws VisADException	Data access I/O failure.
     */
    protected
    NcVar(NcVar ncVar)
	throws VisADException, IOException
    {
	netcdf = ncVar.netcdf;
	var = ncVar.var;
	unit = ncVar.unit;
	mathType = ncVar.mathType;
	dims = ncVar.dims;
    }


    /**
     * Constructs from a netCDF variable, netCDF dataset, and VisAD MathType.
     *
     * @param var		The netCDF variable to be adapted.
     * @param netcdf		The netCDF dataset that contains
     *				<code>var</code>.
     * @param type		The VisAD MathType for this variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    protected
    NcVar(Variable var, Netcdf netcdf, ScalarType type)
	throws VisADException, IOException
    {
	mathType = type;
	this.var = var;
	this.netcdf = netcdf;

	Unit	tmpUnit = getUnit(var, netcdf);

	if (type instanceof RealType)
	{
	    /*
	     * Ensure that the units of this variable are convertible with
	     * the default units of the RealType to prevent a subsequent
	     * VisADException.
	     */
	    Unit	defaultUnit = ((RealType)type).getDefaultUnit();

	    if (tmpUnit == null)
	    {
		tmpUnit = defaultUnit;
	    }
	    else if (!Unit.canConvert(tmpUnit, defaultUnit))
	    {
		System.err.println("Unit of variable \"" + var.getName() +
		    "\" (" + tmpUnit + ") not convertible with that" +
		    " quantity's default unit (" + defaultUnit + ")" +
		    ".  Setting to default unit.");
		tmpUnit = defaultUnit;
	    }
	}

	unit = tmpUnit;

	/*
	 * Cache the adapted, netCDF dimensions in netCDF order.  This must
	 * be done carefully in order to avoid a circular dependency between
	 * NcVar and NcCoordDim.
	 */
	DimensionIterator	iter = var.getDimensionIterator();

	dims = new NcDim[var.getRank()];
	for (int i = 0; i < dims.length; ++i)
	    dims[i] = NcDim.newNcDim(iter.next(), netcdf, this);
    }


    /**
     * Factory method for creating an instance of the correct subtype.
     *
     * @param var		The netCDF variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @return			The NcVar for <code>var</code>.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     */
    public static NcVar
    newNcVar(Variable var, Netcdf netcdf)
	throws VisADException, IOException
    {
	Key	key = new Key(var, netcdf);
	NcVar	ncVar = (NcVar)cache.get(key);

	if (ncVar == null)
	{
	    Class	type = var.getComponentType();

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

	    cache.put(key, ncVar);
	}

	return ncVar;
    }


    /**
     * Determine the units of the given, netCDF variable.
     *
     * @param var		The netCDF variable to have it's units returned.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @return			The units of the variable if it has a decodable
     *				"unit" attribute; otherwise, <code>null</code>.
     */
    protected static Unit
    getUnit(Variable var, Netcdf netcdf)
    {
	Unit		unit;
	Key		key = new Key(var, netcdf);
	UnitMapValue	value = (UnitMapValue)unitMap.get(key);

	if (value != null)
	{
	    unit = value.unit;
	}
	else
	{
	    String[]	names = new String[] {"units", "unit"};
	    String	name = null;
	    Attribute	attr = null;

	    unit = null;

	    for (int i = 0; i < names.length; ++i)
	    {
		name = names[i];
		attr = var.getAttribute(name);
		if (attr != null)
		    break;
	    }

	    if (attr != null && attr.isString())
	    {
		String	unitSpec = attr.getStringValue();

		try
		{
		    unit = Parser.parse(unitSpec);
		}
		catch (ParseException e)
		{
		    String	reason = e.getMessage();

		    System.err.println("Couldn't decode attribute " +
			var.getName() + ":" + name + "=\"" + unitSpec + "\"" +
			(reason == null ? "" : (": " + reason)));
		}
	    }

	    unitMap.put(key, new UnitMapValue(unit));
	}

	return unit;
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
     * @param type		netCDF type (e.g. <code>Character.TYPE</code>, 
     *				<code>Float.TYPE</code>).
     * @return			The default fill-value value for the given 
     *				netCDF type.
     * @throws BadFormException	Unknown netCDF type.
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
     * @param type		netCDF type (e.g. <code>Character.TYPE</code>, 
     *				<code>Float.TYPE</code>).
     * @return			The minimum valid value for the given netCDF 
     *				type.
     * @throws BadFormException	Unknown netCDF type.
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
     * @param type		netCDF type (e.g. <code>Character.TYPE</code>, 
     *				<code>Float.TYPE</code>).
     * @return			The maximum valid value for the given netCDF 
     *				type.
     * @throws BadFormException	Unknown netCDF type
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
     * Return the VisAD MathType of this variable's values.  NB: The 
     * MathType of the <b>values</b> is returned -- not the MathType of
     * the implied function.
     *
     * @return	The VisAD MathType of the variable's values.
     */
    public MathType
    getMathType()
    {
	return mathType;
    }


    /**
     * Return the VisAD rank of this variable.
     *
     * @return	The VisAD rank of the variable.  This is one less than
     *		the rank of the netCDF variable for textual variables.
     */
    abstract int
    getRank();


    /**
     * Return the shape of this variable.
     *
     * @return			The length of each netCDF dimension of the
     *				variable (in netCDF order).
     * @postcondition		<code>getRank() == getLengths().length</code>.
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
     * Returns a VisAD dimension of this variable.  NB: Won't return
     * innermost dimension of textual variables.
     *
     * @param			The index of the dimension.  Assumes netCDF
     *				order.
     * @precondition		<code>index >= 0 && index < getRank()</code>
     * @return			The requested dimension.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws VisADException	Data access I/O failure.
     */
    NcDim
    getDimension(int index)
	throws VisADException, IOException
    {
	if (index < 0 || index >= getRank())
	    throw new VisADException("Index out of bounds");

	return dims[index];
    }


    /**
     * Gets the VisAD dimensions of this variable (in netCDF order).
     *
     * @return			The VisAD dimensions of the variable in
     *				netCDF order.
     * @postcondition		<code>getRank() == 
     *				</code>RETURN_VALUE</code>.length</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws VisADException	Data access I/O failure.
     */
    NcDim[]
    getDimensions()
	throws VisADException, IOException
    {
	NcDim[]	result = new NcDim[getRank()];

	System.arraycopy(dims, 0, result, 0, result.length);

	return result;
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
     * Return the long name of the variable.
     *
     * @param var		The netCDF variable to be examined.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @return			The long name of the variable or
     *				<code>null</code> if the variable doesn't
     *				have a long name.
     */
    static String
    getLongName(Variable var, Netcdf netcdf)
    {
	String			name;
	Key			key = new Key(var, netcdf);
	LongNameMapValue	value =
	    (LongNameMapValue)longNameMap.get(key);

	if (value != null)
	{
	    name = value.longName;
	}
	else
	{
	    Attribute	attr = var.getAttribute("long_name");

	    name = (attr == null || !attr.isString())
			? null
			: attr.getStringValue();

	    longNameMap.put(key, new LongNameMapValue(name));
	}

	return name;
    }


    /**
     * Return the long name of the variable.
     *
     * @return	The long name of the variable or <code>null</code> if the
     *		variable doesn't have a long name.
     */
    String
    getLongName()
    {
	String		longName;
	Attribute	attr = var.getAttribute("long_name");

	return attr == null
		? null
		: attr.getComponentType().equals(Character.class)
		    ? attr.getStringValue()
		    : null;
    }


    /**
     * Return the longest name of the variable.
     *
     * @return		The value of the "long_name" attribute; otherwise,
     *			the name of the variable.
     */
    String
    getLongestName()
    {
	String	name = getLongName();

	return name != null ? name : getName();
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
     * Indicate whether or not the variable is latitude.
     *
     * @return	<code>true</code> if and only if the variable represents
     *		latitude.
     */
    abstract boolean
    isLatitude();


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
     * @return                  The variable's values.
     * @throws IOException      I/O error.
     */
    abstract float[]
    getFloats()
        throws IOException;


    /**
     * Return the values of this variable as a packed array of doubles.
     *
     * @return                  The variable's values.
     * @throws IOException      I/O error.
     */
    public abstract double[]
    getDoubles()
        throws IOException, VisADException;


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @precondition            <code>getRank() >= 1</code>
     * @return                  The variable's values.
     * @throws IOException      I/O error.
     */
    abstract double[]
    getDoubles(int ipt)
        throws IOException, VisADException;



    /**
     * Return the netCDF dataset.
     */
    public Netcdf
    getNetcdf()
    {
	return netcdf;
    }


    /**
     * Return the underlying netCDF Variable.
     */
    protected Variable
    getVar()
    {
	return var;
    }


    /**
     * Supports the unit value in the unit cache.
     */
    protected static class
    UnitMapValue
    {
	protected Unit		unit;

	protected
	UnitMapValue(Unit unit)
	{
	    this.unit = unit;
	}
    }


    /**
     * Supports the long name value in the long name cache.
     */
    protected static class
    LongNameMapValue
    {
	protected String	longName;

	protected
	LongNameMapValue(String longName)
	{
	    this.longName = longName;
	}
    }


    /**
     * Supports the key field of the various caches.
     */
    protected static class
    Key
    {
	private Netcdf		netcdf;
	private Variable	var;

	protected
	Key(Variable var, Netcdf netcdf)
	{
	    this.var = var;
	    this.netcdf = netcdf;
	}

	public int
	hashCode()
	{
	    return var.getName().hashCode() ^ netcdf.hashCode();
	}

	public boolean
	equals(Object obj)
	{
	    boolean	equals;

	    if (this == obj)
	    {
		equals = true;
	    }
	    else
	    {
		Key	that = (Key)obj;

		equals = var.getName().equals(that.var.getName()) &&
			netcdf == that.netcdf;
	    }

	    return equals;
	}
    }
}


/**
 * Adapts an integral netCDF variable that's being imported to a VisAD API.
 */
abstract class
NcInteger
    extends NcNumber
{
    /**
     * Constructs from a netCDF variable, a netCDF dataset, and a VisAD
     * RealType.
     *
     * @param var		The netCDF integer variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @param type		The VisAD RealType of <code>var</code>.
     * @throws BadFormException	The netCDF variable cannot be adapted to a 
     *				VisAD API.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     */
    NcInteger(Variable var, Netcdf netcdf, RealType type)
	throws BadFormException, VisADException, IOException
    {
	super(var, netcdf, type);
    }


    /**
     * Indicates whether or not a netCDF variable can be represented as
     * an integer in the given VisAD range.
     *
     * @param var		The netCDF variable to be examined.
     * @param visadMin		The minimum representable VisAD value.
     * @param visadMax		The maximum representable VisAD value.
     * @return			<code>true</code> if and only if the netCDF 
     *				variable can be represented within the given 
     *				range.
     * @throws BadFormException	The netCDF variable cannot be adapted to a 
     *				VisAD API.
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
 * Adapts a netCDF byte variable that's being imported to a VisAD API.
 */
final class
NcByte
    extends NcInteger
{
    /**
     * Indicates whether or not a netCDF variable can be represented as 
     * an NcByte.  This is only possible if the netCDF variable doesn't
     * use the value -128 because that's used by VisAD to indicate a
     * "missing" byte range-value.
     *
     * @param var		The netCDF variable to be examined.
     * @return			<code>true</code> if and only if 
     *				<code>var</code> can be represented as bytes.
     * @throws BadFormException	The netCDF variable cannot be adapted to a 
     *				VisAD API.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, -127, 127);
    }


    /**
     * Constructs from a netCDF variable and dataset.
     *
     * @param var		The netCDF byte variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @precondition		<code>isRepresentable(var).
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     */
    NcByte(Variable var, Netcdf netcdf)
	throws VisADException, IOException
    {
	super(var, netcdf, getRealType(var, netcdf));

	if (!isRepresentable(var))
	    throw new VisADException("Variable not assignable to byte");
    }

    
    /**
     * Gets the range set of this variable.
     *
     * @param type		The VisAD RealType of the variable.
     * @return			The sampling set of the values of this variable.
     * @throws VisADException	Couldn't create VisAD Set.
     */
    protected SimpleSet
    getRangeSet(RealType type)
	throws VisADException
    {
	return (SimpleSet)new Linear1DSet(type,
				Byte.MIN_VALUE+1, Byte.MAX_VALUE, 
				Byte.MAX_VALUE - Byte.MIN_VALUE);
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
}


/**
 * Adapts a netCDF byte or short variable that's being imported to a VisAD API.
 */
final class
NcShort
    extends NcInteger
{
    /**
     * Indicates whether or not a netCDF variable can be represented as 
     * an NcShort.
     *
     * @param var		The netCDF variable to be examined.
     * @return			<code>true</code> if and only if 
     *				<code>var</code> can be represented as short
     *				values.
     * @throws BadFormException	The netCDF variable cannot be adapted to a 
     *				VisAD API.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, Short.MIN_VALUE+1, Short.MAX_VALUE);
    }


    /**
     * Constructs from a netCDF variable and dataset.
     *
     * @param var		The netCDF short variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @precondition		<code>isRepresentable(var).
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     */
    NcShort(Variable var, Netcdf netcdf)
	throws VisADException, IOException
    {
	super(var, netcdf, getRealType(var, netcdf));

	if (!isRepresentable(var))
	    throw new VisADException("Variable not assignable to short");
    }

    
    /**
     * Gets the range set of this variable.
     *
     * @param type		The VisAD RealType of the variable.
     * @return			The sampling set of the values of this variable.
     * @throws VisADException	Couldn't create VisAD Set.
     */
    protected SimpleSet
    getRangeSet(RealType type)
	throws VisADException
    {
	return (SimpleSet)new Linear1DSet(type,
				Short.MIN_VALUE+1, Short.MAX_VALUE, 
				Short.MAX_VALUE - Short.MIN_VALUE);
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
}


/**
 * Adapts a netCDF short, or int variable that's being imported to a VisAD API.
 */
final class
NcInt
    extends NcInteger
{
    /**
     * Indicates whether or not a netCDF variable can be represented as 
     * an NcInt.
     *
     * @param var		The netCDF variable to be examined.
     * @return			<code>true</code> if and only if 
     *				<code>var</code> can be represented in 32-bit
     *				values.
     * @throws BadFormException	The netCDF variable cannot be adapted to a 
     *				VisAD API.
     */
    static boolean
    isRepresentable(Variable var)
	throws BadFormException
    {
	return isRepresentable(var, Integer.MIN_VALUE+1, Integer.MAX_VALUE);
    }


    /**
     * Constructs from a netCDF variable and dataset.
     *
     * @param var		The netCDF "int" variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @precondition		<code>isRepresentable(var).
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     */
    NcInt(Variable var, Netcdf netcdf)
	throws VisADException, IOException
    {
	super(var, netcdf, getRealType(var, netcdf));

	if (!isRepresentable(var))
	    throw new VisADException("Variable not assignable to int");
    }

    
    /**
     * Gets the range set of this variable.
     *
     * @param type		The VisAD RealType of the variable.
     * @return			The sampling set of the values of this variable.
     * @throws VisADException	Couldn't create VisAD Set.
     */
    protected SimpleSet
    getRangeSet(RealType type)
	throws VisADException
    {
	/*
	 * The following is complicated due to the fact that the last
	 * argument to the Linear2DSet() constructor:
	 *
	 *     Linear1DSet(MathType type, double start, double stop, 
	 *			int length)
	 *
	 * is an "int" -- and the number of Java "int" values cannot
	 * be represented by a Java "int".
	 */
	Vetter	vetter = new Vetter(getVar());
	long	minValid = (long)vetter.minValid();
	long	maxValid = (long)vetter.maxValid();
	long	length	= maxValid - minValid + 1;

	return length <= Integer.MAX_VALUE
		    ? (SimpleSet)(new Linear1DSet(type, minValid,
				    maxValid, (int)length))
		    : (SimpleSet)(new FloatSet(type,
				    /*CoordinateSystem=*/null, 
				    new Unit[] {getUnit()}));
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
}


/**
 * Adapts a floating-point netCDF variable that's being imported to a VisAD API.
 */
abstract class
NcReal
    extends NcNumber
{
    /**
     * Constructs from a netCDF variable and dataset, and a VisAD RealType.
     *
     * @param var		The netCDF variable that's being adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @param type		The VisAD RealType of <code>var</code>.
     * @throws BadFormException	The netCDF variable cannot be adapted to a 
     *				VisAD API.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     */
    NcReal(Variable var, Netcdf netcdf, RealType type)
	throws BadFormException, VisADException, IOException
    {
	super(var, netcdf, type);
    }
}


/**
 * Adapts a netCDF float variable that's being imported to a VisAD API.
 */
final class
NcFloat
    extends NcReal
{
    /**
     * Indicates whether or not a netCDF variable can be represented as 
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
     * Constructs from a netCDF variable and dataset.
     *
     * @param var		The netCDF float variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @precondition		<code>isRepresentable(var).
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     */
    NcFloat(Variable var, Netcdf netcdf)
	throws VisADException, IOException
    {
	super(var, netcdf, getRealType(var, netcdf));

	if (!isRepresentable(var))
	    throw new VisADException("Variable not assignable to float");
    }

    
    /**
     * Gets the range set of this variable.
     *
     * @param type		The VisAD RealType of the variable.
     * @return			The sampling set of the values of this variable.
     * @throws VisADException	Couldn't create VisAD Set.
     */
    protected SimpleSet
    getRangeSet(RealType type)
	throws VisADException
    {
	return (SimpleSet)new FloatSet(type, /*CoordinateSystem=*/null,
			    new Unit[] {getUnit()});
    }


    /**
     * Indicate if this variable is float.
     *
     * @return			<code>true</code> always.
     */
    boolean
    isFloat()
    {
	return true;
    }
}


/**
 * Adapts a netCDF double variable that's being imported to a VisAD API.
 */
final class
NcDouble
    extends NcReal
{
    /**
     * Indicates whether or not a netCDF variable can be represented as 
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
     * Constructs from a netCDF variable and dataset.
     *
     * @param var		The netCDF double variable to be adapted.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @precondition		<code>isRepresentable(var).
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     */
    NcDouble(Variable var, Netcdf netcdf)
	throws VisADException, IOException
    {
	super(var, netcdf, getRealType(var, netcdf));

	if (!isRepresentable(var))
	    throw new VisADException("Variable not assignable to double");
    }

    
    /**
     * Gets the range set of this variable.
     *
     * @param type		The VisAD RealType of the variable.
     * @return			The sampling set of the values of this variable.
     * @throws VisADException	Couldn't create VisAD Set.
     */
    protected SimpleSet
    getRangeSet(RealType type)
	throws VisADException
    {
	return (SimpleSet)new DoubleSet(type, /*CoordinateSystem=*/null,
			    new Unit[] {getUnit()});
    }


    /**
     * Indicates if this variable is double.
     *
     * @return	<code>true</code> always.
     */
    boolean
    isDouble()
    {
	return true;
    }
}
