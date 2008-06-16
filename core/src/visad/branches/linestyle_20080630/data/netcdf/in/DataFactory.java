/*
 * Copyright 2000, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DataFactory.java,v 1.4 2002-10-21 20:07:45 donm Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.*;


/**
 * Provides support for creating VisAD Data objects from VirtualData objects.
 *
 * @author Steven R. Emmerson
 */
public class
DataFactory
{
    private static DataFactory  instance;

    static
    {
        instance = new DataFactory();
    }


    protected DataFactory()
    {}


    /**
     * Returns an instance of this class.
     *
     * @return                  An instance of this class.
     */
    public static DataFactory instance()
    {
        return instance;
    }


    /**
     * Creates a VisAD Data object from a netCDF indicial context and a 
     * VirtualData object.
     *
     * @param context           The netCDF indicial context.
     * @param virtualData       The virtual data.
     * @return                  The VisAD Data object corresponding to the
     *                          input.
     * @throws InvalidContextException
     *                          Invalid indicial context.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     * @throws IOException      I/O failure.
     */
    public DataImpl newData(Context context, VirtualData virtualData)
        throws RemoteException, VisADException, InvalidContextException,
            IOException
    {
        /*
         * If the types of virtual data proliferate, then the following may be
         * replaced by implementing the Visitor design pattern in VirtualData
         * (e.g. VirtualData.accept(DataFactory)) and using double dispatch to
         * invoke the proper method of this class.
         */
        return
            virtualData instanceof VirtualScalar
                ? (DataImpl)newData(context, (VirtualScalar)virtualData)
                : virtualData instanceof VirtualFlatField
                    ? (DataImpl)newData(context, (VirtualFlatField)virtualData)
                    : virtualData instanceof VirtualField
                        ? (DataImpl)newData(context, (VirtualField)virtualData)
                        : (DataImpl)newData(context, (VirtualTuple)virtualData);
    }


    /**
     * Creates a VisAD Scalar object from a netCDF indicial context and a 
     * VirtualScalar.
     *
     * @param context           The netCDF indicial context.
     * @param virtualScalar     The virtual data.
     * @return                  The VisAD Real corresponding to the input.
     * @throws InvalidContextException
     *                          Invalid indicial context.
     * @throws VisADException   VisAD failure.
     * @throws IOException      I/O failure.
     */
    public Scalar newData(Context context, VirtualScalar virtualScalar)
        throws VisADException, InvalidContextException, IOException
    {
        return virtualScalar.getScalar(context);
    }

    /**
     * Creates a VisAD FlatField object from a netCDF indicial context and a 
     * VirtualFlatField.
     *
     * @param context           The netCDF indicial context.
     * @param virtualField      The virtual data.
     * @return                  The VisAD FlatField corresponding to the input.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     * @throws IOException      I/O failure.
     */
    public FlatField newData(Context context, VirtualFlatField virtualField)
        throws VisADException, RemoteException, IOException
    {
        FunctionType    funcType = virtualField.getFunctionType();
        SampledSet      domainSet = virtualField.getDomainSet();
        VirtualTuple    rangeTuple = virtualField.getRangeTuple();
        int             componentCount = rangeTuple.size();
        Set[]           rangeSets = new Set[componentCount];
        Unit[]          rangeUnits = new Unit[componentCount];

        for (int i = 0; i < componentCount; ++i)
        {
            VirtualScalar       component = (VirtualScalar)rangeTuple.get(i);

            rangeSets[i] = component.getRangeSet();
            rangeUnits[i] = component.getUnit();
        }

        FlatField       field =
            new FlatField(
                funcType,
                domainSet,
                (CoordinateSystem)null,
                rangeSets,
                rangeUnits);

        double[][]      values = new double[componentCount][];

        for (int i = 0; i < componentCount; ++i)
            values[i] = ((VirtualScalar)rangeTuple.get(i)).getDoubles(context);

        field.setSamples(values, /*copy=*/false);

        return field;
    }


    /**
     * Creates a VisAD Field object from a netCDF indicial context and a 
     * VirtualField.
     *
     * @param context           The netCDF indicial context.
     * @param virtualField      The virtual data.
     * @return                  The VisAD Field corresponding to the input.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     * @throws IOException      I/O failure.
     */
    public FieldImpl newData(Context context, VirtualField virtualField)
        throws VisADException, RemoteException, IOException
    {
        FieldImpl       field;
        if (virtualField instanceof VirtualFlatField)
        {
            field = newData(context, (VirtualFlatField)virtualField);
        }
        else
        {
            FunctionType        funcType = virtualField.getFunctionType();
            SampledSet          domainSet = virtualField.getDomainSet();
            VirtualTuple        rangeTuple = virtualField.getRangeTuple();
            int                 sampleCount = domainSet.getLength();

            field = new FieldImpl(funcType, domainSet);
            context = context.newSubContext();
            for (int i = 0; i < sampleCount; ++i)
            {
                context.setSubContext(i);
                field.setSample(
                    i, newData(context, rangeTuple), /*copy=*/false);
            }
        }
        return field;
    }


    /**
     * Creates a VisAD Data object from a netCDF indicial context and a 
     * VirtualTuple.
     *
     * @param context           The netCDF indicial context.
     * @param virtualTuple      The virtual data.
     * @return                  The VisAD Tuple corresponding to the input.
     * @throws VisADException   VisAD failure.
     * @throws RemoteException  Java RMI failure.
     * @throws IOException      I/O failure.
     */
    public DataImpl newData(Context context, VirtualTuple virtualTuple)
        throws RemoteException, VisADException, IOException
    {
        DataImpl        data = null;
        int             size = virtualTuple.size();

        if (size == 1)
        {
            data = newData(context, virtualTuple.get(0));
        }
        else if (size > 1)
        {
            MathType    type = virtualTuple.getType();

            if (type instanceof RealTupleType)
            {
                Real[]  reals = new Real[size];

                for (int i = 0; i < size; ++i)
                    reals[i] = (Real)
                        newData(context, (VirtualScalar)virtualTuple.get(i));

                data = new RealTuple((RealTupleType)type, reals,
                                     /*(CoordinateSystem)*/null);
            }
            else if (type instanceof TupleType)
            {
                DataImpl[]      datas = new DataImpl[size];

                for (int i = 0; i < datas.length; ++i)
                    datas[i] = newData(context, virtualTuple.get(i));

                data = new Tuple((TupleType)type, datas, /*copy=*/false);
            }
        }

        return data;
    }
}
