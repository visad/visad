/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Util.java,v 1.6 2000-04-26 15:45:19 dglo Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.DoubleSet;
import visad.FloatSet;
import visad.Gridded1DSet;
import visad.GriddedSet;
import visad.Integer1DSet;
import visad.IntegerNDSet;
import visad.Linear1DSet;
import visad.LinearLatLonSet;
import visad.LinearNDSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SI;
import visad.SampledSet;
import visad.ScalarType;
import visad.SimpleSet;
import visad.TextType;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Provides various utility methods that are useful when importing
 * netCDF data into VisAD.
 */
public class
Util
{
    /**
     * The quantity database to use to map netCDF variables to VisAD
     * Quantity-s.
     */
    private final NetcdfQuantityDB	quantityDB;

    /**
     * A counter for creating new names.
     */
    private static int			nameCount = 0;

    /**
     * The netCDF dataset.
     */
    private final Netcdf		netcdf;

    /**
     * A cache of netCDF variables and their units.
     */
    private final Map			unitMap =
	Collections.synchronizedMap(new WeakHashMap());

    /**
     * A cache of netCDF dimensions and their VisAD domain types.
     */
    private final Map			realTypeMap =
	Collections.synchronizedMap(new WeakHashMap());

    /**
     * A cache of netCDF dimensions and their VisAD domain sets.
     */
    private final Map			domainSetMap =
	Collections.synchronizedMap(new WeakHashMap());

    /**
     * A cache of netCDF datasets and their Util instances.
     */
    private static Map			utilMap =
	Collections.synchronizedMap(new WeakHashMap());

    /**
     * The quantity of time.
     */
    private final Quantity		time;

    /**
     * The quantity of longitude.
     */
    private Quantity			longitude;

    /**
     * The quantity of latitude.
     */
    private Quantity			latitude;


    /**
     * Constructs from a netCDF dataset and a quantity database.
     *
     * @param netcdf		The netCDF dataset.
     * @param quantityDB	The quantity database to use to map netCDF
     *				variables to VisAD Quantity-s.
     */
    private
    Util(Netcdf netcdf, QuantityDB quantityDB)
    {
	this.netcdf = netcdf;
	this.quantityDB = new NetcdfQuantityDB(quantityDB);
	time = quantityDB.get("time");
	longitude = quantityDB.get("longitude");
	latitude = quantityDB.get("latitude");
    }


    /**
     * Factory method for creating a new instance.
     *
     * @param netcdf		The netCDF dataset.
     * @param quantityDB	The quantity database to use to map netCDF
     *				variables to VisAD Quantity-s.
     */
    public static Util
    newUtil(Netcdf netcdf, QuantityDB quantityDB)
    {
	/*
	 * The order of get() and containsKey() are reversed because the map
	 * is a WeakHashMap.
	 */
	Util		util = (Util)utilMap.get(netcdf);

	if (!utilMap.containsKey(netcdf))
	{
	    util = new Util(netcdf, quantityDB);
	    utilMap.put(netcdf, util);
	}

	return util;
    }


    /**
     * Return the VisAD ScalarType of a netCDF variable.
     *
     * @param var		The netCDF variable.
     * @return			The VisAD ScalarType of <code>var</code>.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     */
    public ScalarType
    getScalarType(Variable var)
	throws VisADException
    {
	Class	cl = var.getComponentType();

	return cl.equals(char.class)
		? (ScalarType)getTextType(var)
		: (ScalarType)getRealType(var);
    }


    /**
     * Return the VisAD TextType of a netCDF variable.
     *
     * @param var		The netCDF variable.
     * @return			The VisAD TextType of <code>var</code>.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     */
    protected TextType
    getTextType(Variable var)
	throws VisADException
    {
	return new TextType(var.getName());
    }


    /**
     * Return the VisAD RealType of a netCDF variable.
     *
     * @param var		The netCDF variable.
     * @return			The VisAD RealType of <code>var</code>.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     */
    protected RealType
    getRealType(Variable var)
	throws VisADException
    {
	/*
	 * This implementation caches earlier results in order to avoid
	 * recomputation.
	 */

	/*
	 * The order of get() and containsKey() are reversed because the map
	 * is a WeakHashMap.
	 */
	RealType	type = (RealType)realTypeMap.get(var);

	if (!realTypeMap.containsKey(var))
	{
	    String	name = var.getName();
	    String	longName = getLongName(var);
	    Unit	varUnit = justGetUnit(var);

	    type = quantityDB.getBest(longName, name);

	    if (type == null)
	    {
		type = RealType.getRealTypeByName(name);

		if (type == null)
		    type = new RealType(name, varUnit, /*Set=*/null);
	    }

	    /*
	     * Ensure that the unit of this variable is
	     * convertible with the default unit of the RealType
	     * to prevent a subsequent VisADException.
	     */
	    Unit	defaultUnit = type.getDefaultUnit();

	    if (varUnit == null)
	    {
		varUnit = defaultUnit;
		setUnit(var, varUnit);
	    }
	    else if (!Unit.canConvert(varUnit, defaultUnit))
	    {
		String	newName = name + "_" + nameCount++;

		System.err.println("Unit of variable \"" + name +
		    "\" (" + varUnit + ") not convertible with that" +
		    " quantity's default unit (" + defaultUnit + ")" +
		    ".  Creating new quantity \"" + newName + "\".");
		type = new RealType(newName, varUnit, /*Set=*/null);
		setUnit(var, varUnit);
	    }

	    realTypeMap.put(var, type);
	}

	return type;
    }


    /**
     * Gets the unit of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @return			The units of the values of <code>var</code>
     *				if it has a decodable "unit" attribute;
     *				otherwise, <code>null</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Unit
    getUnit(Variable var)
	throws VisADException
    {
	/*
	 * First, try using the "unit" attribute.  If that's unsuccessful,
	 * then use the default unit of the RealType.
	 */

	Unit	unit = justGetUnit(var);

	return unit != null
		? unit
		: getRealType(var).getDefaultUnit();
    }


    /**
     * Just gets the unit of a netCDF variable (i.e. it doesn't do anything
     * else).
     *
     * @param var		A netCDF variable.
     * @return			The units of the values of <code>var</code>
     *				if it has a decodable "unit" attribute;
     *				otherwise, <code>null</code>.
     */
    protected Unit
    justGetUnit(Variable var)
    {
	/*
	 * This implementation caches units because of the following:
	 *   1.	Decoding a unit specification is expensive.
	 *   2.	A warning message might be printed every time an invalid
	 *	unit specification is encountered.
	 *   3. A dimension with a coordinate variable might cause
	 *	this method to be invoked many times.
	 */

	/*
	 * The order of get() and containsKey() are reversed because the map
	 * is a WeakHashMap.
	 */
	Unit	unit = (Unit)unitMap.get(var);

	if (!unitMap.containsKey(var))
	{
	    unit = null;

	    String[]	names = new String[] {"units", "unit"};
	    String	name = null;
	    Attribute	attr = null;

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

	    unitMap.put(var, unit);
	}

	return unit;
    }


    /**
     * Sets the unit of a netCDF variable.
     *
     * @param var		A netCDF variable.
     * @param unit		The unit to be obtained by a subsequent
     *				<code>justGetUnit(var)</code>.
     */
    private void
    setUnit(Variable var, Unit unit)
    {
	unitMap.put(var, unit);
    }


    /**
     * Gets the long name of a netCDF variable if possible.
     *
     * @param var		A netCDF variable.
     * @return			The long name of <code>var</code> it it exists;
     *				otherwise, <code>null</code>.
     */
    public String
    getLongName(Variable var)
    {
	String		longName = null;
	Attribute	attr = var.getAttribute("long_name");

	if (attr != null && attr.getComponentType().equals(char.class))
	{
	    longName = attr.getStringValue();
	}

	return longName;
    }


    /**
     * Gets the type of the domain corresponding to a netCDF domain.
     *
     * @param dims		A netCDF domain.  Dimensions are in netCDF
     *				order (outer dimension first).
     * @return			The type of the domain corresponding to
     *				<code>dims</code>.  RETURN_VALUE is
     *				<code>null</code>, a <code>RealType</code>,
     *				or a <code>RealTupleType</code> if
     *				<code>dims.length</code> is 0, 1, or greater
     *				than 1, respectively.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public MathType
    getDomainType(Dimension[] dims)
	throws VisADException
    {
	MathType	type;
	int		rank = dims.length;

	if (rank == 0)
	{
	    type = null;	// means scalar domain
	}
	else if ( rank == 1)
	{
	    type = (MathType)getRealType(dims[0]);
	}
	else
	{
	    RealType[]	types = new RealType[dims.length];

	    int	j = dims.length;
	    for (int i = 0; i < dims.length; ++i)
		types[--j] = getRealType(dims[i]);	// reverse order

	    type = new RealTupleType(types);
	}

	return type;
    }


    /**
     * Indicates if a netCDF variable is a coordinate variable (i.e. is
     * associated with a netCDF dimension).
     *
     * @param var		A netCDF variable.
     * @return			<code>true</code> if and only if <code>
     *				var</code> is a coordinate variable.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public boolean
    isCoordinateVariable(Variable var)
	throws VisADException
    {
	boolean	isCoordinateVariable = false;

	if (var.getRank() == 1 && isNumeric(var))
	{
	    DimensionIterator	iter = var.getDimensionIterator();
	    int			rank = var.getRank();
	    String		name = var.getName();

	    for (int i = 0; !isCoordinateVariable && i < rank; ++i)
		if (iter.next().getName().equals(name))
		    isCoordinateVariable = true;
	}

	return isCoordinateVariable;
    }


    /**
     * Gets the netCDF coordinate variable associated with a netCDF dimension,
     * if it exists.
     *
     * @param dim		A netCDF dimension.
     * @return			The netCDF coordinate variable associated
     *				with <code>dim</code> or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Variable
    getCoordinateVariable(Dimension dim)
	throws VisADException
    {
	Variable	var = netcdf.get(dim.getName());

	if (var != null)
	{
	    var = var.getRank() == 1 && isNumeric(var)
		    ? var
		    : null;
	}

	return var;
    }


    /**
     * Indicates if a netCDF variable is numeric.
     *
     * @param var		A netCDF variable.
     * @return			<code>true</code> if an only if <code>var</code>
     *				represents a numeric (i.e. not textual)
     *				quantity.
     */
    protected static boolean
    isNumeric(Variable var)
    {
	return !var.getComponentType().equals(char.class);
    }


    /**
     * Gets the type of the domain corresponding to a netCDF dimension.
     * Uses the dimension's coordinate variable if appropriate.
     *
     * @param dim		A netCDF dimension.
     * @return			The VisAD MathType of the domain corresponding
     *				to <code>dim</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public RealType
    getRealType(Dimension dim)
	throws VisADException
    {
	RealType	type;
	Variable	coordVar = getCoordinateVariable(dim);

	if (coordVar != null)
	{
	    type = getRealType(coordVar);
	}
	else
	{
	    String	name = dim.getName();

	    type = quantityDB.get(name);

	    if (type == null)
	    {
		type = RealType.getRealTypeByName(name);

		if (type == null)
		{
		    type = new RealType(name);

		    /*
		     * QUESTION: add co-ordinate system?  I don't think so for
		     * a netCDF dimension that doesn't have a co-ordinate
		     * variable.
		     */
		    type.setDefaultSet(new FloatSet(type));
		}
	    }
	}

	return type;
    }


    /**
     * Gets the VisAD sampling set of a netCDF variable's values.
     *
     * @param var		A netCDF variable.
     * @return			The VisAD Set corresponding to the values of
     *				<code>var</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public SimpleSet
    getValueSet(Variable var)
	throws VisADException
    {
	SimpleSet	set;
	Class		cl = var.getComponentType();

	if (cl.equals(char.class))
	{
	    set = null;
	}
	else
	{
	    RealType	type = getRealType(var);

	    if (cl.equals(byte.class))
	    {
		set = new Linear1DSet(type,
				      Byte.MIN_VALUE+1, Byte.MAX_VALUE,
				      Byte.MAX_VALUE - Byte.MIN_VALUE);
	    }
	    else if (cl.equals(short.class))
	    {
		set = new Linear1DSet(type,
				      Short.MIN_VALUE+1, Short.MAX_VALUE,
				      Short.MAX_VALUE - Short.MIN_VALUE);
	    }
	    else if (cl.equals(int.class))
	    {
		/*
		 * The following is complicated due to the fact that the last
		 * argument to the Linear1DSet() constructor:
		 *
		 *     Linear1DSet(MathType type, double start, double stop,
		 *			int length)
		 *
		 * is an "int" -- and the number of Java "int" values cannot
		 * be represented by a Java "int".
		 */
		Vetter	vetter = new Vetter(var);
		long	minValid = (long)vetter.minValid();
		long	maxValid = (long)vetter.maxValid();
		long	length	= maxValid - minValid + 1;

		set = length <= Integer.MAX_VALUE
			    ? (SimpleSet)(new Linear1DSet(type, minValid,
					    maxValid, (int)length))
			    : (SimpleSet)(new FloatSet(type,
					    /*CoordinateSystem=*/null,
					    new Unit[] {getUnit(var)}));
	    }
	    else if (cl.equals(float.class))
	    {
		set = new FloatSet(type, /*CoordinateSystem=*/null,
				    new Unit[] {getUnit(var)});
	    }
	    else
	    {
		set = (SimpleSet)new DoubleSet(type, /*CoordinateSystem=*/null,
				    new Unit[] {getUnit(var)});
	    }
	}

	return set;
    }


    /**
     * Gets the VisAD domain set corresponding to a netCDF domain.
     *
     * @param dims		A netCDF domain.  Dimensions are in netCDF
     *				order (outer dimension first).
     * @return			The VisAD domain set corresponding to
     *				<code>dims</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @exception IOException	I/O failure.
     */
    public SampledSet
    getDomainSet(Dimension[] dims)
	throws IOException, VisADException
    {
	GriddedSet	set;
	Gridded1DSet[]	sets = new Gridded1DSet[dims.length];

	int	j = dims.length;
	for (int i = 0; i < dims.length; ++i)
	    sets[--j] = getDomainSet(dims[i]);	// reverse order

	boolean	allInteger1DSets = true;

	for (int i = 0; allInteger1DSets && i < sets.length; ++i)
	    allInteger1DSets = sets[i] instanceof Integer1DSet;

	MathType	type = getDomainType(dims);

	if (allInteger1DSets)
	{
	    set = (GriddedSet)getIntegerSet(sets, type);
	}
	else
	{
	    boolean	allLinear1DSets = true;

	    for (int i = 0; allLinear1DSets && i < sets.length; ++i)
		allLinear1DSets = sets[i] instanceof Linear1DSet;

	    if (allLinear1DSets)
	    {
		set = (GriddedSet)getLinearSet(sets, type);
	    }
	    else
	    {
		set = getGriddedSet(sets, type);
	    }
	}

	return set;
    }


    /**
     * Gets the VisAD GriddedSet corresponding to a netCDF dimension.
     *
     * @param dim		A netCDF dimension.
     * @return			The VisAD GriddedSet corresponding to
     *				<code>dim</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public Gridded1DSet
    getDomainSet(Dimension dim)
	throws VisADException, IOException
    {
	/*
         * This implementation caches earlier results because this
         * operation is potentially expensive and may be invoked many
         * times for any given dimension.
	 */

	/*
	 * The order of get() and containsKey() are reversed because the map
	 * is a WeakHashMap.
	 */
	Gridded1DSet	set = (Gridded1DSet)domainSetMap.get(dim);

	if (!domainSetMap.containsKey(dim))
	{
	    Variable	coordVar = getCoordinateVariable(dim);

	    if (coordVar == null)
	    {
		// TODO: add CoordinateSystem argument
		set = new Integer1DSet(getRealType(dim), dim.getLength());
	    }
	    else
	    {
		ArithProg	ap = isLongitude(coordVar)
					? new LonArithProg()
					: new ArithProg();
		float[]	coordValues = new float[dim.getLength()];

		toArray(coordVar, coordValues, new int[] {0},
			new int[] {coordValues.length});

		if (ap.accumulate(coordValues))
		{
		    /*
		     * The coordinate-variable is an arithmetic progression.
		     */
		    // TODO: add CoordinateSystem argument
		    set = new Linear1DSet(
			    getRealType(dim),
			    ap.getFirst(),
			    ap.getLast(),
			    ap.getNumber(),
			    /*(CoordinateSystem)*/null,
			    new Unit[] {getUnit(coordVar)},
			    /*(ErrorEstimate[])*/null);
		}
		else
		{
		    /*
		     * The coordinate-variable is not an arithmetic progression.
		     */
		    // TODO: add CoordinateSystem argument
		    set = new Gridded1DSet(
			    getRealType(dim),
			    new float[][] {coordValues},
			    dim.getLength(),
			    /*(CoordinateSystem)*/null,
			    new Unit[] {getUnit(coordVar)},
			    /*(ErrorEstimate[])*/null);
		}
	    }

	    domainSetMap.put(dim, set);
	}

	return set;
    }


    /**
     * Gets the IntegerSet of combined Integer1DSet-s and domain type.
     *
     * @param sets		Integer1DSet-s of the domain.
     * @param domain		MathType of the domain.
     * @return			IntegerSet of the domain.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    private static GriddedSet
    getIntegerSet(Gridded1DSet[] sets, MathType type)
	throws VisADException
    {
	int	rank = sets.length;
	int[]	lengths = new int[rank];

	for (int idim = 0; idim < rank; ++idim)
	    lengths[idim] = ((Integer1DSet)sets[idim]).getLength(0);

	// TODO: add CoordinateSystem argument
	return IntegerNDSet.create(type, lengths, /*(CoordinateSystem)*/null,
		/*(Unit[])*/null, /*(ErrorEstimate[])*/null);
    }


    /**
     * Gets the LinearSet of combined Linear1DSet-s and domain type.
     *
     * @param sets		Linear1DSet-s of the domain.
     * @param type		VisAD math type of the domain set.
     *				NB: The units of the dimensions needn't be the
     *				same as the units in <code>type</code>.
     * @return			LinearSet of the domain of the function.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    private LinearSet
    getLinearSet(Gridded1DSet[] sets, MathType type)
	throws VisADException
    {
	LinearSet	set = null;
	int		rank = sets.length;
	double[]	firsts = new double[rank];
	double[]	lasts = new double[rank];
	int[]		lengths = new int[rank];
	Unit[]		units = new Unit[rank];

	for (int idim = 0; idim < rank; ++idim)
	{
	    Linear1DSet	linear1DSet = (Linear1DSet)sets[idim];

	    firsts[idim] = linear1DSet.getFirst();
	    lengths[idim] = linear1DSet.getLength(0);
	    lasts[idim] = linear1DSet.getLast();
	    units[idim] = linear1DSet.getSetUnits()[0];
	}


	// TODO: add CoordinateSystem argument
	if (rank == 2)
	{
	    RealType[]	types = ((RealTupleType)type).getRealComponents();

	    if ((types[0].equals(longitude) && types[1].equals(latitude)) ||
	        (types[1].equals(longitude) && types[0].equals(latitude)))
	    {
		set = new LinearLatLonSet(type,
					firsts[0], lasts[0], lengths[0],
					firsts[1], lasts[1], lengths[1],
					/*(CoordinateSystem)*/null,
					units,
					/*(ErrorEstimate[])*/null);
	    }
	}

	if (set == null)
	{
	    set = LinearNDSet.create(type,
				      firsts, lasts, lengths,
				      /*(CoordinateSystem)*/null,
				      units,
				      /*(ErrorEstimate[])*/null);
	}

	return set;
    }


    /**
     * Gets the GriddedSet of combined Gridded1DSet-s and domain type.
     *
     * @param sets		Gridded1DSet-s of the domain.
     * @param type		VisAD math type of the domain set.  NB: The
     *				units of the dimensions needn't be the same as
     *				the units in <code>type</code>.
     * @return			GriddedSet of the domain of the function.
     * @throws IOException	Data access I/O failure.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    private static GriddedSet
    getGriddedSet(Gridded1DSet[] sets, MathType type)
	throws VisADException, IOException
    {
	int		rank = sets.length;
	int[]		lengths = new int[rank];
	float[][]	values = new float[rank][];
	int		ntotal = 1;

	for (int idim = 0; idim < rank; ++idim)
	{
	    lengths[idim] = sets[idim].getLength(0);
	    ntotal *= lengths[idim];
	}

        int step = 1;
        int laststep = 1;

	for (int idim = 0; idim < rank; ++idim)
	{
	    float[]	vals = sets[idim].getSamples(false)[0];

	    values[idim] = new float[ntotal];

/* WLH 4 Aug 98
	    for (int pos = 0; pos < ntotal/vals.length; pos += vals.length)
		System.arraycopy(vals, 0, values[idim], pos, vals.length);
*/
            step *= lengths[idim];
            for (int i=0; i<lengths[idim]; i++) {
              int istep = i * laststep;
              for (int j=0; j<ntotal; j+=step) {
                for (int k=0; k<laststep; k++) {
                  values[idim][istep+j+k] = vals[i];
                }
              }
            }
            laststep = step;
	}

	Unit[]	units = new Unit[rank];

	for (int idim = 0; idim < rank; ++idim)
	    units[idim] = sets[idim].getSetUnits()[0];

	// TODO: add CoordinateSystem argument
	return GriddedSet.create(type, values, lengths,
		 /*(CoordinateSystem)*/null, units, /*(ErrorEstimate[])*/null);
    }


    /**
     * Indicates if a netCDF dimension represents time.
     *
     * @param dim		A netCDF dimension.
     * @return			<code>true</code> if an only if <code>dim</code>
     *				represents time.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public boolean
    isTime(Dimension dim)
	throws VisADException
    {
	return getRealType(dim).equals(time);
    }


    /**
     * Indicates if a netCDF dimension represents longitude.
     *
     * @param dim		A netCDF dimension.
     * @return			<code>true</code> if an only if <code>dim</code>
     *				represents longitude.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public boolean
    isLongitude(Variable var)
	throws VisADException
    {
	return getRealType(var).equals(longitude);
    }


    /**
     * Gets data values of a netCDF variable and performs type conversion.
     *
     * @param var		A netCDF variable.
     * @param values		The destination array for the data values.
     *				<code>values.length</code> must be >=
     *				the number of points represented by
     *				<code>shape</code>.
     * @param origin		The origin vector for the values.
     * @param shape		The shape of the I/O transfer.
     * @return			<code>values</code>.
     * @throws IOException	I/O failure.
     * @see ucar.netcdf.Variable#toArray(Object, int[], int[])
     */
    public static Object
    toArray(Variable var, float[] values, int[] origin, int[] shape)
	throws IOException
    {
	// TODO: support text

	if (var.getRank() == 0)
	{
	    values[0] = var.getFloat(new int[] {});
	}
	else
	{
	    Class	fromClass = var.getComponentType();

	    if (fromClass.equals(float.class))
	    {
		var.toArray(values, origin, shape);
	    }
	    else
	    {
		int	length = 1;

		for (int i = 0; i < shape.length; ++i)
		    length *= shape[i];

		Object	dst = Array.newInstance(fromClass, length);

		var.toArray(dst, origin, shape);

		if (fromClass.equals(byte.class))
		{
		    byte[]	fromArray = (byte[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = fromArray[i];
		}
		else if (fromClass.equals(short.class))
		{
		    short[]	fromArray = (short[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = fromArray[i];
		}
		else if (fromClass.equals(int.class))
		{
		    int[]	fromArray = (int[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = fromArray[i];
		}
		else if (fromClass.equals(double.class))
		{
		    double[]	fromArray = (double[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = (float)fromArray[i];
		}
	    }
	}

	return values;
    }


    /**
     * Gets data values of a netCDF variable and performs type conversion.
     *
     * @param var		A netCDF variable.
     * @param values		The destination array for the data values.
     *				<code>values.length</code> must be >=
     *				the number of points represented by
     *				<code>shape</code>.
     * @param origin		The origin vector for the values.
     * @param shape		The shape of the I/O transfer.
     * @return			<code>values</code>.
     * @throws IOException	I/O failure.
     * @see ucar.netcdf.Variable#toArray(Object, int[], int[])
     */
    public static Object
    toArray(Variable var, double[] values, int[] origin, int[] shape)
	throws IOException
    {
	// TODO: support text

	if (var.getRank() == 0)
	{
	    values[0] = var.getDouble(new int[] {});
	}
	else
	{
	    Class	fromClass = var.getComponentType();

	    if (fromClass.equals(double.class))
	    {
		var.toArray(values, origin, shape);
	    }
	    else
	    {
		int	length = 1;

		for (int i = 0; i < shape.length; ++i)
		    length *= shape[i];

		Object	dst = Array.newInstance(fromClass, length);

		var.toArray(dst, origin, shape);

		if (fromClass.equals(byte.class))
		{
		    byte[]	fromArray = (byte[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = fromArray[i];
		}
		else if (fromClass.equals(short.class))
		{
		    short[]	fromArray = (short[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = fromArray[i];
		}
		else if (fromClass.equals(int.class))
		{
		    int[]	fromArray = (int[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = fromArray[i];
		}
		else if (fromClass.equals(float.class))
		{
		    float[]	fromArray = (float[])dst;

		    for (int i = 0; i < fromArray.length; ++i)
			values[i] = fromArray[i];
		}
	    }
	}

	return values;
    }
}
