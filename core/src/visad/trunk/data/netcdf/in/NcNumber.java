/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNumber.java,v 1.1 1998-03-20 20:56:54 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.MultiArray;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.OffsetUnit;
import visad.RealType;
import visad.SI;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * The NcNumber class decorators netCDF arithmetic variables.  It is useful
 * for importing netCDF variables
 */
abstract class
NcNumber
    extends NcVar
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
     *
     * @param var	The netCDF variable to be decorated.
     * @param netcdf	The netCDF dataset that contains <code>var</code>.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcNumber(Variable var, Netcdf netcdf)
	throws BadFormException, VisADException
    {
	super(var, netcdf);
	isCoordVar = setIsCoordVar();
	isLongitude = setIsLongitude();
	isTime = setIsTime();
	mathType = RealType.getRealTypeByName(getName());
	vetter = new Vetter(var);
    }


    /**
     * Set whether or not the variable is a co-ordinate variable.
     *
     * @return	<code>true</code> if and only if the variable is a netCDF
     *		coordinate variable.
     */
    private boolean
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
     *
     * @return	<code>true</code> if and only if the netCDF variable represents
     *		longitude.
     */
    private boolean
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
     *
     * @return	<code>true</code> if and only if the netCDF variable represents
     *		time.
     */
    private boolean
    setIsTime()
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
     * Indicate whether or not this variable is longitude.
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
     * Return the rank of this variable.
     *
     * @return	The rank (i.e. number of netCDF dimensions) of the variable.
     */
    int
    getRank()
    {
	return var.getRank();
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
	return getDoubleValues(var);
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
}
