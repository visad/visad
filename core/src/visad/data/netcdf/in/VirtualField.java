/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualField.java,v 1.4 2001-01-08 17:12:59 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.*;


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
    private final VirtualTuple  rangeTuple;


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
     * @param domainSet		The domain sampling set of the field.
     * @param rangeTuple	The range of the field.
     * @return			The corresponding VirtualField.
     * @throws VisADException	Couldn't created necessary VisAD object.
     */
    public static VirtualField
    newVirtualField(SampledSet domainSet, VirtualTuple rangeTuple)
	throws VisADException
    {
	return
	    newVirtualField(
		new FunctionType(
		    ((SetType)domainSet.getType()).getDomain(),
		    rangeTuple.getType()),
		domainSet,
		rangeTuple);
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
	return getDataFactory().newData(context, this);
    }


    /**
     * Clones this instance.
     *
     * @return			A (deep) clone of this instance.
     */
    public Object clone()
    {
	return
	    new VirtualField(
		functionType, domainSet, (VirtualTuple)rangeTuple.clone());
    }


    /**
     * Sets the factory used to create VisAD data objects.
     *
     * @param factory		The factory for creating VisAD data objects.
     */
    public void setDataFactory(DataFactory factory)
    {
	rangeTuple.setDataFactory(factory);
    }


    /**
     * Returns the factory used to create VisAD data objects.
     *
     * @return factory		The factory for creating VisAD data objects.
     */
    public DataFactory getDataFactory()
    {
	return rangeTuple.getDataFactory();
    }
}
