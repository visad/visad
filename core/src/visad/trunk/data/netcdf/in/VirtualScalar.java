/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualScalar.java,v 1.4 2001-11-06 17:55:55 steve Exp $
 */

package visad.data.netcdf.in;


import java.io.IOException;
import ucar.netcdf.Variable;
import visad.*;


/**
 * Provides support for a virtual VisAD Scalar.
 */
public class
VirtualScalar
    extends	VirtualData
{
    /**
     * The factory for creating VisAD data objects.
     */
    private DataFactory		dataFactory = DataFactory.instance();

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
	return getDataFactory().newData(context, this);
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

	toArray(var, values, ioOrigin, ioShape);

	vetter.vet(values);

	return values;
    }

    /**
     * Gets data values of a netCDF variable and performs type conversion.
     *
     * @param var               A netCDF variable.
     * @param values            The destination array for the data values.
     *                          <code>values.length</code> must be >=
     *                          the number of points represented by
     *                          <code>shape</code>.
     * @param origin            The origin vector for the values.
     * @param shape             The shape of the I/O transfer.
     * @return                  <code>values</code>.
     * @throws IOException      I/O failure.
     * @see ucar.netcdf.Variable#toArray(Object, int[], int[])
     */
    static Object
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
            Class       fromClass = var.getComponentType();

            if (fromClass.equals(double.class))
            {
                var.toArray(values, origin, shape);
            }
            else
            {
                int     length = 1;

                for (int i = 0; i < shape.length; ++i)
                    length *= shape[i];

                Object  dst = Array.newInstance(fromClass, length);

                var.toArray(dst, origin, shape);

                if (fromClass.equals(byte.class))
                {
                    byte[]      fromArray = (byte[])dst;

                    for (int i = 0; i < fromArray.length; ++i)
                        values[i] = fromArray[i];
                }
                else if (fromClass.equals(short.class))
                {
                    short[]     fromArray = (short[])dst;

                    for (int i = 0; i < fromArray.length; ++i)
                        values[i] = fromArray[i];
                }
                else if (fromClass.equals(int.class))
                {
                    int[]       fromArray = (int[])dst;

                    for (int i = 0; i < fromArray.length; ++i)
                        values[i] = fromArray[i];
                }
                else if (fromClass.equals(float.class))
                {
                    float[]     fromArray = (float[])dst;

                    for (int i = 0; i < fromArray.length; ++i)
                        values[i] = fromArray[i];
                }
            }
        }

        return values;
    }


    /**
     * Clones this instance.
     *
     * @return			A (deep) clone of this instance.
     */
    public Object clone()
    {
	return new VirtualScalar(type, var, rangeSet, unit, vetter);
    }


    /**
     * Sets the factory used to create VisAD data objects.
     *
     * @param factory		The factory for creating VisAD data objects.
     */
    public void setDataFactory(DataFactory factory)
    {
	dataFactory = factory;
    }


    /**
     * Returns the factory used to create VisAD data objects.
     *
     * @param factory		The factory for creating VisAD data objects.
     */
    public DataFactory getDataFactory()
    {
	return dataFactory;
    }
}
