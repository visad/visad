/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualScalar.java,v 1.1 1998-09-23 17:31:38 steve Exp $
 */

package visad.data.netcdf.in;


import java.io.IOException;
import ucar.netcdf.Variable;
import visad.DataImpl;
import visad.MathType;
import visad.Real;
import visad.RealType;
import visad.Scalar;
import visad.ScalarType;
import visad.SimpleSet;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for a virtual VisAD Scalar.
 */
public class
VirtualScalar
    extends	VirtualData
{
    /**
     * The VisAD MathType of the scalar.
     */
    private ScalarType		type;

    /**
     * The netCDF variable that constitutes the scalar.
     */
    private final Variable	var;

    /**
     * The range set of the scalar.
     */
    private SimpleSet		rangeSet;

    /**
     * The unit of the scalar.
     */
    private final Unit		unit;

    /**
     * The value vetter.
     */
    private final Vetter	vetter;

    /**
     * The shape of the netCDF variable.
     */
    private final int[]		lengths;


    /**
     * Constructs from a scalar type, a 1-D netCDF variable, a range set,
     * a unit, and a value vetter.
     *
     * @param type		The type of the nested scalar.
     * @param var		The 1-D netCDF variable.
     * @param rangeSet		The range set of the values.
     * @param unit		The unit of the values.
     * @param vetter		The value vetter.
     */
    public
    VirtualScalar(ScalarType type, Variable var, SimpleSet rangeSet,
	Unit unit, Vetter vetter)
    {
	this.type = type;
	this.var = var;
	this.rangeSet = rangeSet;
	this.unit = unit;
	this.vetter = vetter;
	lengths = var.getLengths();
    }


    /**
     * Gets the ScalarType of this scalar.
     *
     * @return			The ScalarType of this scalar.
     */
    public ScalarType
    getScalarType()
    {
	return type;
    }


    /**
     * Gets the MathType of this scalar.
     *
     * @return			The ScalarType of this scalar.
     */
    public MathType
    getType()
    {
	return getScalarType();
    }


    /**
     * Gets the range set of this scalar.
     *
     * @return			The range set of this scalar.
     */
    public SimpleSet
    getRangeSet()
    {
	return rangeSet;
    }


    /**
     * Gets the unit of the value.
     *
     * @return			The unit of the value.
     */
    public Unit
    getUnit()
    {
	return unit;
    }


    /**
     * Gets the netCDF variable.
     *
     * @return			The netCDF variable.
     */
    public Variable
    getVariable()
    {
	return var;
    }


    /**
     * Gets the value vetter.
     *
     * @return			The value vetter.
     */
    public Vetter
    getVetter()
    {
	return vetter;
    }


    /**
     * Gets the VisAD data object corresponding to this virtual, data
     * object.
     *
     * @return			The VisAD Scalar corresponding to this 
     *				virtual, data object.
     * throws InvalidContextException
     *				Invalid context.
     * throws VisADException	Couldn't create necessary VisAD object.
     * throws IOException	I/O failure.
     */
    public DataImpl
    getData(Context context)
	throws IOException, VisADException, InvalidContextException
    {
	double[]	values = getDoubles(context);

	if (values.length != 1)
	    throw new InvalidContextException(context);

	return new Real((RealType)getType(), values[0], getUnit());
    }


    /**
     * Gets the double values corresponding to this virtual, data
     * object at a given context.
     *
     * @return			The double values of this virtual, data object.
     * throws VisADException	Couldn't create necessary VisAD object.
     * throws IOException	I/O failure.
     */
    public double[]
    getDoubles(Context context)
	throws IOException, VisADException
    {
	int	rank = lengths.length;
	int[]	ioOrigin = new int[rank];
	int[]	ioShape = new int[rank];
	int[]	ioContext = context.getContext();

	System.arraycopy(ioContext, 0, ioOrigin, 0, ioContext.length);

	for (int i = 0; i < ioContext.length; ++i)
	    ioShape[i] = 1;

	int	total = 1;

	for (int i = ioContext.length; i < rank; ++i)
	{
	    ioOrigin[i] = 0;
	    ioShape[i] = lengths[i];
	    total *= lengths[i];
	}

	double[]	values = new double[total];

	Util.toArray(var, values, ioOrigin, ioShape);

	vetter.vet(values);

	return values;
    }
}
