/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNumber.java,v 1.7 1998-09-11 15:00:53 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.multiarray.MultiArrayImpl;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.CoordinateSystem;
import visad.DataImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.OffsetUnit;
import visad.Real;
import visad.RealType;
import visad.SI;
import visad.Set;
import visad.SimpleSet;
import visad.Unit;
import visad.TypeException;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.CacheStrategy;
import visad.data.FileAccessor;
import visad.data.FileFlatField;
import visad.data.netcdf.QuantityMap;


/**
 * The NcNumber class decorates netCDF arithmetic variables.  It is useful
 * for importing netCDF variables
 */
abstract class
NcNumber
    extends	NcVar
{
    /**
     * The range set of the netCDF variable.  In general, this set will
     * differ from the default set of the variable's MathType.
     */
    private final Set		set;

    /**
     * The value vetter.
     */
    private final Vetter	vetter;

    /**
     * Whether or not the variable is a co-ordinate variable.
     */
    private final boolean	isCoordVar;

    /**
     * Whether or not the variable is latitude in nature.
     */
    private final boolean	isLatitude;

    /**
     * Whether or not the variable is longitude in nature.
     */
    private final boolean	isLongitude;

    /**
     * Whether or not the variable is temporal in nature.
     */
    private final boolean	isTime;

    /**
     * A temporal offset unit for comparison purposes.
     */
    static final Unit		offsetTime = new OffsetUnit(0.0, SI.second);


    /**
     * Constructs from another NcNumber.  Protected to ensure use by 
     * trusted subclasses only.
     *
     * @param ncVar		The adapted, netCDF variable.
     */
    protected
    NcNumber(NcNumber ncVar)
    {
	super(ncVar);

	this.set = ncVar.set;
	this.vetter = ncVar.vetter;
	this.isCoordVar = ncVar.isCoordVar;
	this.isLatitude = ncVar.isLatitude;
	this.isLongitude = ncVar.isLongitude;
	this.isTime = ncVar.isTime;
    }


    /**
     * Constructs from netCDF variable, netCDF dataset, and VisAD MathType.
     *
     * @param var		The netCDF variable to be decorated.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @param type		The VisAD MathType of <code>var</code>.
     * @throws BadFormException	The netCDF variable cannot be adapted to a 
     *				VisAD API.
     * @throws VisADException	Problem in core VisAD.  Probably some
     *				VisAD object couldn't be created.
     */
    NcNumber(Variable var, Netcdf netcdf, RealType type)
	throws BadFormException, VisADException
    {
	super(var, netcdf, type);

	set = getRangeSet(type);
	isCoordVar = isCoordVar(var);
	isLatitude = type.equals(QuantityMap.get("latitude", SI.radian));
	isLongitude = type.equals(QuantityMap.get("longitude", SI.radian));
	isTime = isTime(type.getDefaultUnit());
	vetter = new Vetter(var);
    }


    /**
     * Return the VisAD RealType of the given, netCDF variable.
     *
     * @param var		The netCDF variable.
     * @return			The VisAD RealType of <code>var</code>.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     */
    protected static RealType
    getRealType(Variable var)
	throws VisADException
    {
	RealType	realType = NetcdfQuantityDB.get(var);

	if (realType == null)
	{
	    try
	    {
		realType = new RealType(var.getName(), getUnit(var), 
					/*Set=*/null);	// default set
	    }
	    catch (TypeException e)
	    {
		realType = RealType.getRealTypeByName(var.getName());
	    }
	}

	return realType;
    }


    /**
     * Return the range set of the variable.
     *
     * @param type		The VisAD RealType of the variable.
     * @return			The sampling set of the values of this variable.
     * @throws VisADException	Couldn't create set.
     */
    protected abstract SimpleSet
    getRangeSet(RealType type)
	throws VisADException;


    /**
     * Indicates whether or not the variable is a co-ordinate variable.
     *
     * @param var		The netCDF variable.
     * @return			<code>true</code> if and only if the variable
     *				is a netCDF coordinate variable.
     */
    private static boolean
    isCoordVar(Variable var)
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
     * Indicates whether or not this variable is longitude.
     *
     * @param var		The netCDF variable.
     * @return			<code>true</code> if and only if the netCDF 
     *				variable represents longitude.
     */
    private static boolean
    isLongitude(Variable var)
    {
	String	varName = var.getName();

	return varName.equals("Lon") ||
	       varName.equals("lon") ||
	       varName.equals("Longitude") ||
	       varName.equals("longitude");
    }


    /**
     * Indicates whether or not a unit is temporal in nature.
     *
     * @param unit		The unit to be considered.
     * @return			<code>true</code> if and only if 
     *				<code>unit</code> is a unit of time.
     */
    private static boolean
    isTime(Unit unit)
    {
	return Unit.canConvert(unit, SI.second) ||
	       Unit.canConvert(unit, offsetTime);
    }


    /**
     * Indicate whether or not the variable is a co-ordinate variable.
     *
     * @return	<code>true</code> if and only if this variable is a netCDF
     *		coordinate variable.
     */
    boolean
    isCoordinateVariable()
    {
	return isCoordVar;
    }


    /**
     * Indicate whether or not this variable is temporal in nature.
     *
     * @return	<code>true</code> if and only if this netCDF variable represents
     *		time.
     */
    boolean
    isTime()
    {
	return isTime;
    }


    /**
     * Indicate whether or not this variable is temporal in nature.
     *
     * @return	<code>true</code> if and only if this netCDF variable represents
     *		latitude.
     */
    boolean
    isLatitude()
    {
	return isLatitude;
    }


    /**
     * Indicate whether or not this variable represents longitude.
     *
     * @return	<code>true</code> if and only if this netCDF variable represents
     *		longitude.
     */
    boolean
    isLongitude()
    {
	return isLongitude;
    }


    /**
     * Gets the VisAD range set of this variable.
     *
     * @return	The VisAD range set of the variable.
     */
    Set
    getSet()
    {
	return set;
    }


    /**
     * Gets the rank of this variable.
     *
     * @return	The rank (i.e. number of netCDF dimensions) of the variable.
     */
    int
    getRank()
    {
	return getVar().getRank();
    }


    /**
     * Gets the values of this variable as a packed array of floats.
     *
     * @return			The values of the variable.
     * @throws IOException	I/O error.
     */
    float[]
    getFloats()
	throws IOException
    {
	Variable	var = getVar();
	int[]		lengths = var.getLengths();
	int		npts = 1;

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
     *
     * @param shape	The dimensional lengths.
     * @return		The total number of points.
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
     * @return			The values of the variable.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	I/O error.
     */
    public double[]
    getDoubles()
	throws IOException, VisADException
    {
	int[]		lengths = getVar().getLengths();
	IndexIterator	iter = new IndexIterator(lengths);

	return getDoubles(iter, getMaxIOLengths(lengths),
	    product(lengths));
    }


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @param ipt		The position in the outermost dimension.
     * @precondition		<code>getRank() >= 1</code>
     * @precondition		<code>ipt >= 0 &&
     *				ipt < getDimension(0).getLength()</code>
     * @return			The values of the variable at the given 
     *				position.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	I/O error.
     */
    protected double[]
    getDoubles(int ipt)
	throws IOException, VisADException
    {
	if (getRank() < 1)
	    throw new VisADException("Variable is scalar");

	if (ipt < 0 || ipt >= getDimension(0).getLength())
	    throw new VisADException("Index out of bounds");

	int[]		lengths = getLengths();
	int[]		maxIOLengths = getMaxIOLengths(lengths);
	IndexIterator	iter = new IndexIterator(lengths);

	maxIOLengths[0] = 1;
	iter.value()[0] = ipt;

	return getDoubles(iter, maxIOLengths, product(lengths)/lengths[0]);
    }


    /**
     * Computes maximum dimensional lengths for I/O.
     *
     * @param lengths	The dimensional sizes of a netCDF variable.
     * @return		The shape of the variable for I/O.
     */
    protected int[]
    getMaxIOLengths(int[] lengths)
    {
	int[]		maxIOLengths = new int[lengths.length];
	int		product = 1;	// dimensional length product
	int		bufferSize = 10000;

	for (int idim = lengths.length-1; idim >= 0; --idim)
	{
	    maxIOLengths[idim] = 
		Math.min(Math.max(bufferSize/product, 1), lengths[idim]);
	    product *= lengths[idim];
	}

	return maxIOLengths;
    }


    /**
     * Gets double values.
     *
     * @param iter		The iterator for getting values.
     * @param maxIOLengths	The shape of the I/O array.
     * @param count		The total number of values to get.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	Data access I/O failure.
     */
    protected double[]
    getDoubles(IndexIterator iter, int[] maxIOLengths, int count)
	throws IOException, VisADException
    {
	double[]	values = new double[count];

	if (values.length == 1)
	{
	    values[0] = getVar().getDouble(iter.value());
	}
	else
	{
	    int[]	lengths = getVar().getLengths();
	    int[]	ioLengths = new int[lengths.length];

	    for (int istart = 0; istart < count; )
	    {
		int[]	origin = iter.value();

		for (int idim = 0; idim < lengths.length; ++idim)
		{
		    ioLengths[idim] = Math.min(maxIOLengths[idim],
					       lengths[idim] - origin[idim]);
		}

		MultiArrayImpl	ma = 
		    (MultiArrayImpl)getVar().copyout(origin, ioLengths);
		int			nread = product(ioLengths);

		arrayCopy(ma.storage, values, istart, nread);

		iter.advance(nread);
		istart += nread;
	    }
	}

	vetter.vet(values);

	return values;
    }


    /**
     * Copies an array of the underlying netCDF type into a double array.
     *
     * @param fromArray	Array of unspecified type from which to copy values.
     * @param toArray	Array of tyoe double to which values are copied.
     * @param start	Starting index of <code>toArray</code> for copy.
     * @param npts	The number of points to copy.
     * @precondition	<code>start+npts <= toArray.length</code>.
     * @postcondition	<code>toArray[start+i] == fromArray[i]</code> for all 
     *			</code>i</code>.
     * @throws VisADException	<code>toArray</code> is too small.
     */
    void
    arrayCopy(Object fromArray, double[] toArray, int start, int npts)
	throws VisADException
    {
	if (start + npts > toArray.length)
	    throw new VisADException("Destination array too small");

	Class	type = getVar().getComponentType();

	if (type.equals(Byte.TYPE))
	    arrayCopy((byte[])fromArray, toArray, start, npts);
	else
	if (type.equals(Short.TYPE))
	    arrayCopy((short[])fromArray, toArray, start, npts);
	else
	if (type.equals(Integer.TYPE))
	    arrayCopy((int[])fromArray, toArray, start, npts);
	else
	if (type.equals(Float.TYPE))
	    arrayCopy((float[])fromArray, toArray, start, npts);
	else
	if (type.equals(Double.TYPE))
	    arrayCopy((double[])fromArray, toArray, start, npts);
    }


    /**
     * Copies a byte array into a double array.
     *
     * @param fromArray	Array of type byte from which to copy values.
     * @param toArray	Array of type double to which values are copied.
     * @param start	Starting index of <code>toArray</code> for copy.
     * @param npts	The number of points to copy.
     * @precondition	<code>start+npts <= toArray.length</code>.
     * @postcondition	<code>toArray[start+i] == fromArray[i]</code> for all 
     *			</code>i</code>.
     * @throws VisADException	<code>toArray</code> is too small.
     */
    static void
    arrayCopy(byte[] fromArray, double[] toArray, int start, int npts)
	throws VisADException
    {
	if (start + npts > toArray.length)
	    throw new VisADException("Destination array too small");

	for (int i = 0; i < npts; ++i)
	    toArray[start+i] = fromArray[i];
    }


    /**
     * Copies a short array into a double array.
     *
     * @param fromArray	Array of type short from which to copy values.
     * @param toArray	Array of type double to which values are copied.
     * @param start	Starting index of <code>toArray</code> for copy.
     * @param npts	The number of points to copy.
     * @precondition	<code>start+npts <= toArray.length</code>.
     * @postcondition	<code>toArray[start+i] == fromArray[i]</code> for all 
     *			</code>i</code>.
     * @throws VisADException	<code>toArray</code> is too small.
     */
    static void
    arrayCopy(short[] fromArray, double[] toArray, int start, int npts)
	throws VisADException
    {
	if (start + npts > toArray.length)
	    throw new VisADException("Destination array too small");

	for (int i = 0; i < npts; ++i)
	    toArray[start+i] = fromArray[i];
    }


    /**
     * Copies an int array into a double array.
     *
     * @param fromArray	Array of type int from which to copy values.
     * @param toArray	Array of type double to which values are copied.
     * @param start	Starting index of <code>toArray</code> for copy.
     * @param npts	The number of points to copy.
     * @precondition	<code>start+npts <= toArray.length</code>.
     * @postcondition	<code>toArray[start+i] == fromArray[i]</code> for all 
     *			</code>i</code>.
     * @throws VisADException	<code>toArray</code> is too small.
     */
    static void
    arrayCopy(int[] fromArray, double[] toArray, int start, int npts)
	throws VisADException
    {
	if (start + npts > toArray.length)
	    throw new VisADException("Destination array too small");

	for (int i = 0; i < npts; ++i)
	    toArray[start+i] = fromArray[i];
    }


    /**
     * Copies a float array into a double array.
     *
     * @param fromArray	Array of type float from which to copy values.
     * @param toArray	Array of type double to which values are copied.
     * @param start	Starting index of <code>toArray</code> for copy.
     * @param npts	The number of points to copy.
     * @precondition	<code>start+npts <= toArray.length</code>.
     * @postcondition	<code>toArray[start+i] == fromArray[i]</code> for all 
     *			</code>i</code>.
     * @throws VisADException	<code>toArray</code> is too small.
     */
    static void
    arrayCopy(float[] fromArray, double[] toArray, int start, int npts)
	throws VisADException
    {
	if (start + npts > toArray.length)
	    throw new VisADException("Destination array too small");

	for (int i = 0; i < npts; ++i)
	    toArray[start+i] = fromArray[i];
    }


    /**
     * Copies a double array into a double array.
     *
     * @param fromArray	Array of type double from which to copy values.
     * @param toArray	Array of type double to which values are copied.
     * @param start	Starting index of <code>toArray</code> for copy.
     * @param npts	The number of points to copy.
     * @precondition	<code>start+npts <= toArray.length</code>.
     * @postcondition	<code>toArray[start+i] == fromArray[i]</code> for all 
     *			</code>i</code>.
     * @throws VisADException	<code>toArray</code> is too small.
     */
    static void
    arrayCopy(double[] fromArray, double[] toArray, int start, int npts)
	throws VisADException
    {
	if (start + npts > toArray.length)
	    throw new VisADException("Destination array too small");

	System.arraycopy(fromArray, 0, toArray, start, npts);
    }


    /**
     * Gets the VisAD data object corresponding to this variable.
     *
     * @return			The variable's values.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData()
	throws IOException, VisADException
    {
	return getData(new NcDomain(getDimensions()), getDoubles());
    }


    /**
     * Return the VisAD data object corresponding to this variable on a
     * specified domain.
     *
     * @param domain		The domain over which the values are defined.
     * @param values		The values of the variable.
     * @return			The corresponding VisAD data object.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	I/O error.
     */
    protected DataImpl
    getData(NcDomain domain, double[] values)
	throws IOException, VisADException
    {
	Unit		unit = getUnit();
	RealType	type = (RealType)getMathType();
	DataImpl	data;

	if (values.length == 1)
	{
	    data = new Real(type, values[0], unit);
	}
	else
	{
	    FunctionType	funcType = 
		new FunctionType(domain.getType(), type);
	    FlatField		field = new FlatField(funcType,
		domain.getSet(), (CoordinateSystem)null, /*(Set[])*/null, 
		new Unit[] {unit});

	    field.setSamples(new double[][] {values}, /*copy=*/false);

	    data = field;
	}

	return data;
    }


    /**
     * Gets the VisAD FunctionType of this variable.
     *
     * @precondition		<code>getRank() >= 1</code>
     * @return			The VisAD FunctionType of this variable.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public FunctionType
    getFunctionType()
	throws VisADException
    {
	if (getRank() < 1)
	    throw new VisADException("Scalar " + getName() + 
		" can't be a function");

	NcDomain	domain = new NcDomain(getDimensions());

	return new FunctionType(domain.getType(), (RealType)getMathType());
    }


    /**
     * Gets a proxy for the VisAD Data object corresponding to this variable.
     *
     * @return			A proxy for the VisAD Data object 
     *				corresponding to this variable.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	I/O error.
     */
    public DataImpl
    getProxy()
	throws VisADException, IOException
    {
	return getRank() == 0
		? getData()	// scalars don't deserve a proxy
		: new FileFlatField(new Accessor(this), new CacheStrategy());
    }


    /**
     * Return the value vetter for this variable.
     *
     * @return			The object that vets the values of this
     *				variable.
     */
    protected Vetter
    getVetter()
    {
	return vetter;
    }


    /**
     * Indicate whether or not this variable is a co-ordinate variable.
     *
     * @return			<code>true</code> if and only if this
     *				variable is a coordinate variable.
     */
    protected boolean
    isCoordVar()
    {
	return isCoordVar;
    }


    /**
     * Gets a proxy for the VisAD Data object corresponding to this variable
     * at a point in the outermost dimension.
     *
     * @param index		The position in the outermost dimension.
     * @precondition		<code>getRank() >= 1</code>
     * @precondition		<code>ipt >= 0 &&
     *				ipt < getDimension(0).getLength()</code>
     * @return			A proxy for the VisAD data object corresponding
     *				to this variable at a point in the outermost
     *				dimension.
     * @throws VisADException	Couldn't create necessary VisAD data object
     *				or variable is scalar or index is out-of-bounds.
     * @throws IOException	I/O error.
     */
    public DataImpl
    getProxy(int index)
	throws IOException, VisADException
    {
	if (getRank() < 1)
	    throw new VisADException("Variable is scalar");

	return new FileFlatField(new Accessor(this), new CacheStrategy());
    }


    /**
     * Return the VisAD data object corresponding to this variable at a
     * point in the outermost dimension.
     *
     * @param ipt		The position in the outermost dimension.
     * @precondition		<code>getRank() >= 1</code>
     * @precondition		<code>ipt >= 0 &&
     *				ipt < getDimension(0).getLength()</code>
     * @return			The values of the variable at the given 
     *				position.
     * @throws VisADException	Couldn't create necessary VisAD data object
     *				or variable is scalar or index is out-of-bounds.
     * @throws IOException	I/O error.
     */
    public DataImpl
    getData(int ipt)
	throws IOException, VisADException
    {
	if (getRank() < 1)
	    throw new VisADException("Variable is scalar");

	if (ipt < 0 || ipt >= getDimension(0).getLength())
	    throw new VisADException("Index out of bounds");

	NcDim[]		dims = new NcDim[getRank()-1];

	System.arraycopy(getDimensions(), 1, dims, 0, dims.length);

	return getData(new NcDomain(dims), getDoubles(ipt));
    }


    /**
     * Gets the VisAD MathType of the inner portion of the netCDF variable.
     *
     * @precondition		<code>getRank() >= 1</code>
     * @return			The VisAD MathType of the inner portion of this
     *				variable.
     * @throws VisADException	Couldn't create necessary VisAD data object
     *				or variable is scalar.
     */
    public MathType
    getInnerMathType()
	throws VisADException
    {
	if (getRank() < 1)
	    throw new VisADException("Variable is scalar");

	NcDim[]		innerDims = getInnerDimensions();
	MathType	varType = getMathType();
	MathType	innerType;

	if (innerDims.length == 0)
	{
	    innerType = varType;
	}
	else
	{
	    NcDomain	innerDomain = new NcDomain(innerDims);

	    innerType =
		new FunctionType(innerDomain.getType(), varType);
	}

	return innerType;
    }


    /**
     * Gets the inner dimensions of the netCDF variable (in netCDF order).
     *
     * @precondition		<code>getRank() >= 1</code>
     * @return			The inner dimensions of this variable (in 
     *				netCDF order).
     * @throws VisADException	Couldn't create necessary VisAD data object
     *				or variable is scalar.
     */
    public NcDim[]
    getInnerDimensions()
	throws VisADException
    {
	if (getRank() < 1)
	    throw new VisADException("Variable is scalar");

	NcDim[]	innerDims = new NcDim[getRank()-1];

	System.arraycopy(getDimensions(), 1, innerDims, 0, 
	    innerDims.length);

	return innerDims;
    }
}
