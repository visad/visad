/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNumber.java,v 1.3 1998-06-17 20:30:31 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.DataImpl;
import visad.OffsetUnit;
import visad.Real;
import visad.RealType;
import visad.SI;
import visad.Set;
import visad.SimpleSet;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.netcdf.in.QuantityDB;


/**
 * The NcNumber class decorators netCDF arithmetic variables.  It is useful
 * for importing netCDF variables
 */
abstract class
NcNumber
    extends NcVar
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
     * Construct.
     *
     * @param var	The netCDF variable to be decorated.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @param type	The VisAD MathType of <code>var</code>.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcNumber(Variable var, Netcdf netcdf, RealType type)
	throws BadFormException, VisADException
    {
	super(var, netcdf, type);

	QuantityDB	quantityDB = QuantityDB.instance();

	set = getRangeSet(type);
	isCoordVar = isCoordVar(var);
	isLatitude = type.equals(quantityDB.get("latitude", SI.radian));
	isLongitude = type.equals(quantityDB.get("longitude", SI.radian));
	isTime = isTime(type.getDefaultUnit());
	vetter = new Vetter(var);
    }


    /**
     * Return the VisAD RealType of the given, netCDF variable.
     *
     * @param var	The netCDF variable.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    protected static RealType
    getRealType(Variable var)
	throws VisADException
    {
	RealType	realType = QuantityDB.instance().get(var);

	if (realType == null)
	{
	    realType = RealType.getRealTypeByName(var.getName());

	    if (realType == null)
	    {
		realType = new RealType(var.getName(), getUnit(var), 
					/*Set=*/null);	// default set
	    }
	}

	return realType;
    }


    /**
     * Return the range set of the variable.
     *
     * @param type	The VisAD RealType of the variable.
     * @return		The sampling set of the values of this variable.
     * @exception VisADException	Couldn't create set.
     */
    protected abstract SimpleSet
    getRangeSet(RealType type)
	throws VisADException;


    /**
     * Set whether or not the variable is a co-ordinate variable.
     *
     * @return	<code>true</code> if and only if the variable is a netCDF
     *		coordinate variable.
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
     * Set whether or not this variable is longitude.
     *
     * @return	<code>true</code> if and only if the netCDF variable represents
     *		longitude.
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
     * Set whether or not this variable is temporal in nature.
     *
     * @return	<code>true</code> if and only if the netCDF variable represents
     *		time.
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
     * @return	<code>true</code> if and only if the variable is a netCDF
     *		coordinate variable.
     */
    boolean
    isCoordinateVariable()
    {
	return isCoordVar;
    }


    /**
     * Indicate whether or not the variable is temporal in nature.
     *
     * @return	<code>true</code> if and only if the netCDF variable represents
     *		time.
     */
    boolean
    isTime()
    {
	return isTime;
    }


    /**
     * Indicate whether or not the variable is temporal in nature.
     *
     * @return	<code>true</code> if and only if the netCDF variable represents
     *		latitude.
     */
    boolean
    isLatitude()
    {
	return isLatitude;
    }


    /**
     * Indicate whether or not the variable is temporal in nature.
     *
     * @return	<code>true</code> if and only if the netCDF variable represents
     *		longitude.
     */
    boolean
    isLongitude()
    {
	return isLongitude;
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
     * Return the rank of this variable.
     *
     * @return	The rank (i.e. number of netCDF dimensions) of the variable.
     */
    int
    getRank()
    {
	return getVar().getRank();
    }


    /**
     * Return the values of this variable as a packed array of floats.
     *
     * @return			The values of the variable.
     * @exception IOException	I/O error.
     */
    float[]
    getFloatValues()
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
     * @exception IOException	I/O error.
     */
    double[]
    getDoubleValues()
	throws IOException
    {
	return getDoubleValues(getVar());
    }


    /**
     * Return the values of this variable -- at a given point of the outermost
     * dimension -- as a packed array of doubles.
     *
     * @param ipt	The position in the outermost dimension.
     * @precondition	The variable is rank 2 or greater.
     * @precondition	<code>ipt</code> lies within the outermost dimension.
     * @return		The values of the variable at the given position.
     * @exception IOException		I/O error.
     */
    double[]
    getDoubleValues(int ipt)
	throws IOException
    {
	Variable	var = getVar();
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
     *
     * @param ma	The MultiArray accessor for the values.
     * @precondition	<code>ma</code> accesses double values.
     * @return		The values.  Invalid values are replaced with NaN's.
     * @exception IOException
     *			Data access I/O failure.
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


    /**
     * Return the values of this variable as a packed array of VisAD
     * DataImpl objects.  It would be really, really stupid to use this
     * method on a variable of any length.
     *
     * @return		The variable's values.
     * @exception IOException
     *			Data access I/O failure.
     */
    DataImpl[]
    getData()
	throws IOException, VisADException
    {
	Variable	var = getVar();
	Unit		unit = getUnit();
	RealType	type = (RealType)getMathType();
	int[]		lengths = var.getLengths();
	int		npts = product(lengths);
	IndexIterator	iter = new IndexIterator(lengths);
	Real[]		values = new Real[npts];
	double[]	val = new double[1];

	for (int i = 0; i < npts; ++i)
	{
	    val[0] = var.getDouble(iter.value());
	    iter.incr();

	    vetter.vet(val);

	    values[i] = new Real(type, val[0], unit);
	}

	return values;
    }


    /**
     * Return the value vetter for this variable.
     */
    protected Vetter
    getVetter()
    {
	return vetter;
    }


    /**
     * Indicate whether or not this variable is a co-ordinate variable.
     */
    protected boolean
    isCoordVar()
    {
	return isCoordVar;
    }
}
