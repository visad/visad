/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualField.java,v 1.2 2000-04-26 15:45:21 dglo Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FunctionType;
import visad.MathType;
import visad.RealType;
import visad.RealTupleType;
import visad.SampledSet;
import visad.VisADException;


/**
 * Provides support for a virtual VisAD Field.
 */
public class
VirtualField
    extends	VirtualData
{
    /**
     * The VisAD FunctionType of this field.
     */
    private final FunctionType	functionType;

    /**
     * The VisAD domain sampling of this field.
     */
    private final SampledSet	domainSet;

    /**
     * The range tuple of this field.
     */
    private final VirtualTuple	rangeTuple;


    /**
     * Constructs from a function type, domain set, and range tuple.
     *
     * @param funcType		The VisAD FunctionType of the field.
     * @param domainSet		The domain sampling set of the field.
     * @param rangeTuple	The range of the field.
     */
    protected
    VirtualField(FunctionType funcType, SampledSet domainSet,
	VirtualTuple rangeTuple)
    {
	this.functionType = funcType;
	this.domainSet = domainSet;
	this.rangeTuple = rangeTuple;
    }


    /**
     * Factory method for creating a new instance.
     *
     * @param funcType		The VisAD FunctionType of the field.
     * @param domainSet		The domain sampling set of the field.
     * @param rangeTuple	The range of the field.
     * @return			The corresponding VirtualField.
     */
     public static VirtualField
     newVirtualField(FunctionType funcType, SampledSet domainSet,
	VirtualTuple rangeTuple)
     {
	MathType	rangeType = funcType.getRange();

	return (rangeType instanceof RealType ||
		rangeType instanceof RealTupleType)
		    ? new VirtualFlatField(funcType, domainSet, rangeTuple)
		    : new VirtualField(funcType, domainSet, rangeTuple);
     }


    /**
     * Gets the FunctionType of this virtual Field.
     *
     * @return			The FunctionType of this virtual Field.
     */
    public FunctionType
    getFunctionType()
    {
	return functionType;
    }


    /**
     * Gets the MathType of this virtual Field.
     *
     * @return			The FunctionType of this virtual Field.
     */
    public MathType
    getType()
    {
	return getFunctionType();
    }


    /**
     * Gets the domain sampling set of this virtual field.
     *
     * @return			The domain sampling set of this field.
     */
    public SampledSet
    getDomainSet()
    {
	return domainSet;
    }


    /**
     * Gets the range tuple of this virtual field.
     *
     * @return			The range tuple of this virtual field.
     */
    public VirtualTuple
    getRangeTuple()
    {
	return rangeTuple;
    }


    /**
     * Gets the VisAD data object corresponding to this virtual, data
     * object.
     *
     * @param context		The context in which the data is to be
     *				retrieved.
     * @return			The VisAD Field corresponding to this
     *				virtual Field.
     * @throws VisADException	Couldn't created necessary VisAD object.
     * @throws RemoteException	Remote access failure.
     * @throws IOException	I/O failure.
     */
    public DataImpl
    getData(Context context)
	throws VisADException, RemoteException, IOException
    {
	FieldImpl	field = new FieldImpl(functionType, domainSet);
	int		n = domainSet.getLength();

	context = context.newSubContext();

	for (int i = 0; i < n; ++i)
	{
	    context.setSubContext(i);
	    field.setSample(i, rangeTuple.getData(context), /*copy=*/false);
	}

	return field;
    }
}
