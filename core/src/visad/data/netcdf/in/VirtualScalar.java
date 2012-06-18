/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualScalar.java,v 1.7 2002-10-21 20:07:47 donm Exp $
 */

package visad.data.netcdf.in;


import java.lang.reflect.Array;
import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.Variable;
import visad.*;


/**
 * Provides support for a virtual VisAD Scalar.
 */
public abstract class
VirtualScalar
    extends     VirtualData
{
    /**
     * The factory for creating VisAD data objects.
     */
    private DataFactory         dataFactory = DataFactory.instance();

    /**
     * The VisAD MathType of the scalar.
     */
    private ScalarType          type;

    /**
     * The netCDF variable that constitutes the scalar.
     */
    private final Variable      var;

    /**
     * Constructs from a scalar type, a 1-D netCDF variable, a range set,
     * a unit, and a value vetter.
     *
     * @param type              The type of the nested scalar.
     * @param var               The 1-D netCDF variable.
     * @param rangeSet          The range set of the values.
     * @param unit              The unit of the values.
     * @param vetter            The value vetter.
     */
    public
    VirtualScalar(ScalarType type, Variable var, SimpleSet rangeSet,
        Unit unit, Vetter vetter)
    {
        this(type, var);
    }


    /**
     * Constructs from a scalar type and a 1-D netCDF variable
     *
     * @param type              The type of the nested scalar.
     * @param var               The 1-D netCDF variable.
     */
    public
    VirtualScalar(ScalarType type, Variable var)
    {
        this.type = type;
        this.var = var;
    }

    /**
     * Gets the ScalarType of this scalar.
     *
     * @return                  The ScalarType of this scalar.
     */
    public ScalarType
    getScalarType()
    {
        return type;
    }


    /**
     * Gets the MathType of this scalar.
     *
     * @return                  The ScalarType of this scalar.
     */
    public MathType
    getType()
    {
        return getScalarType();
    }


    /**
     * Determines if this is a VirtualReal or not.
     *
     * @return true if this is a VirtualReal
     */
    public boolean
    isReal()
    {
        return false;
    }

    /**
     * Gets the range set of this scalar.
     *
     * @return                  The range set of this scalar.
     * @throws RuntimeException  if class doesn't support this.
     */
    public SimpleSet
    getRangeSet()
    {
        throw new RuntimeException();
    }


    /**
     * Gets the unit of the value.
     *
     * @return                  The unit of the value.
     * @throws RuntimeException  if class doesn't support this.
     */
    public Unit
    getUnit()
    {
        throw new RuntimeException();
    }


    /**
     * Gets the netCDF variable.
     *
     * @return                  The netCDF variable.
     */
    public Variable
    getVariable()
    {
        return var;
    }


    /**
     * Gets the value vetter.
     *
     * @return                  The value vetter.
     * @throws RuntimeException  if class doesn't support this.
     */
    public Vetter
    getVetter()
    {
        throw new RuntimeException();
    }


    /**
     * Gets the VisAD data object corresponding to this virtual, data
     * object.
     *
     * @return                  The VisAD Scalar corresponding to this
     *                          virtual, data object.
     * throws InvalidContextException
     *                          Invalid context.
     * @throws InvalidContextException
     *                          if the indicial context is invalid.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @throws RemoteException  if a Java RMI failure occurs.
     * @throws IOException      I/O failure.
     */
    public DataImpl getData(Context context) 
        throws InvalidContextException, VisADException, RemoteException, IOException
    {
        return getDataFactory().newData(context, this);
    }


    /**
     * Gets the Scalar object corresponding to this virtual, data
     * object.
     *
     * @return                  The VisAD Scalar corresponding to this
     *                          virtual, data object.
     * @throws InvalidContextException
     *                          if the indicial context is invalid.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @throws RemoteException  if a Java RMI failure occurs.
     * @throws IOException      I/O failure.
     */
    protected abstract Scalar getScalar(Context context)
        throws VisADException, InvalidContextException, IOException;

    /**
     * Gets the double values corresponding to this virtual, data
     * object at a given context.
     *
     * @return                  The double values of this virtual, data object.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @throws IOException      I/O failure.
     * @throws RuntimeException  if class doesn't support this.
     */
    public double[]
    getDoubles(Context context)
        throws IOException, VisADException
    {
        throw new RuntimeException();
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
     * Sets the factory used to create VisAD data objects.
     *
     * @param factory           The factory for creating VisAD data objects.
     */
    public void setDataFactory(DataFactory factory)
    {
        dataFactory = factory;
    }


    /**
     * Returns the factory used to create VisAD data objects.
     *
     * @return factory           The factory for creating VisAD data objects.
     */
    public DataFactory getDataFactory()
    {
        return dataFactory;
    }
}
