/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualReal.java,v 1.2 2006-02-13 22:30:08 curtis Exp $
 */

package visad.data.netcdf.in;


import java.io.IOException;
import ucar.netcdf.Variable;
import visad.*;


/**
 * Provides support for a virtual VisAD Real.
 */
public class
VirtualReal
    extends     VirtualScalar
{

    /**
     * The range set of the scalar.
     */
    private SimpleSet           rangeSet;

    /**
     * The unit of the scalar.
     */
    private final Unit          unit;

    /**
     * The value vetter.
     */
    private final Vetter        vetter;

    /**
     * The shape of the netCDF variable.
     */
    private final int[]         lengths;

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
    VirtualReal(RealType type, Variable var, SimpleSet rangeSet,
        Unit unit, Vetter vetter)
    {
        super(type, var);
        this.rangeSet = rangeSet;
        this.unit = unit;
        this.vetter = vetter;
        lengths = var.getLengths();

    }

    /**
     * Gets the range set of this scalar.
     *
     * @return                  The range set of this scalar.
     */
    public SimpleSet
    getRangeSet()
    {
        return rangeSet;
    }


    /**
     * Gets the unit of the value.
     *
     * @return                  The unit of the value.
     */
    public Unit
    getUnit()
    {
        return unit;
    }


    /**
     * Gets the value vetter.
     *
     * @return                  The value vetter.
     */
    public Vetter
    getVetter()
    {
        return vetter;
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
    protected Scalar getScalar(Context context) 
        throws VisADException, InvalidContextException, IOException
    {

        double[]        values = getDoubles(context);

        if (values.length != 1)
            throw new InvalidContextException(context);

        return (Scalar) new Real( (RealType)getScalarType(), 
                                  values[0], 
                                  getUnit());
    }

    /**
     * Gets the double values corresponding to this virtual, data
     * object at a given context.
     *
     * @return                  The double values of this virtual, data object.
     * throws VisADException    Couldn't create necessary VisAD object.
     * throws IOException       I/O failure.
     */
    public double[]
    getDoubles(Context context)
        throws IOException, VisADException
    {
        int     rank = lengths.length;
        int[]   ioOrigin = new int[rank];
        int[]   ioShape = new int[rank];
        int[]   ioContext = context.getContext();

        System.arraycopy(ioContext, 0, ioOrigin, 0, ioContext.length);

        for (int i = 0; i < ioContext.length; ++i)
            ioShape[i] = 1;

        int     total = 1;

        for (int i = ioContext.length; i < rank; ++i)
        {
            ioOrigin[i] = 0;
            ioShape[i] = lengths[i];
            total *= lengths[i];
        }

        double[]        values = new double[total];

        toArray(getVariable(), values, ioOrigin, ioShape);

        vetter.vet(values);

        return values;
    }


    /**
     * Determines if this is a VirtualReal or not.
     *
     * @return true if this is a VirtualReal
     */
    public boolean
    isReal()
    {
        return true;
    }

    /**
     * Clones this instance.
     *
     * @return                  A (deep) clone of this instance.
     */
    public Object clone()
    {
        return new VirtualReal((RealType)getScalarType(), getVariable(), 
                               rangeSet, unit, vetter);
    }
}
