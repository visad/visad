/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SoundingComponentProxy.java,v 1.1 1998-10-28 17:16:50 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.Data;
import visad.DataShadow;
import visad.FlatField;
import visad.FunctionType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.ShadowType;
import visad.Tuple;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;


/**
 * Acts as a proxy for a component of a sounding.  "Acts as a proxy" means
 * that this field doesn't have any data of its own; instead, it accesses the
 * data in the original field used during construction.  Thus, this class
 * acts like a "view-controller" for a "model" that's another field.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public abstract class
SoundingComponentProxy
    extends	AbstractSounding
{
    /**
     * The FlatField that's being proxied.
     */
    private final FlatField	field;

    /**
     * The indexes of the components of the field that are in this proxy.
     */
    private final int[]		proxyIndexes;

    /**
     * The indexes of the components of the field that are *not* in this proxy.
     */
    private final int[]		otherIndexes;


    /**
     * Constructs from a FlatField to be proxied and indexes of the components
     * in the field that are to constitute the range of this proxy.
     *
     * @param field		The FlatField to be adapted.
     * @param indexes		Range-indexes of the components of the field.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IllegalArgumentException
     *				<code>field == null</code>, or indexes
     *				out-of-bounds
     */
    public
    SoundingComponentProxy(FlatField field, int[] indexes)
	throws VisADException
    {
	super(
	    getFunctionType(field, indexes),
	    field.getDomainSet(), 
	    getDefaultUnits(field, indexes));

	this.field = field;
	proxyIndexes = indexes;

	int	fieldRangeRank =
	    ((TupleType)((FunctionType)field.getType()).getRange())
		.getDimension();
	int	fieldIndexes = new int[fieldRangeRank];
	for (int i = 0; i < fieldIndexes.length; ++i)
	    fieldIndexes[i] = i;
	for (int i = 0; i < proxyIndexes.length; ++i)
	    fieldIndexes[proxyIndexes[i]] = -1;

	otherIndexes = new int[fieldRangeRank - proxyIndexes.length];
	int	k = 0;
	for (int i = 0; i < fieldIndexes.length; ++i)
	    if (fieldIndexes[i] >= 0)
		otherIndexes[k++] = i;
    }


    /**
     * Gets the FunctionType of a view of a FlatField that comprises the given
     * range components.
     *
     * @param field             The field to be examined.
     * @param indexes           Indexes of the components in the range of the
     *                          field.
     * @throws IllegalArgumentException
     *				<code>field == null</code> or
     *				indexes out-of-bounds.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static FunctionType
    getFunctionType(FlatField field, int[] indexes)
	throws IllegalArgumentException, VisADException
    {
	if (field == null)
	    throw new IllegalArgumentException("Null field");

	FunctionType	oldFunctionType = (FunctionType)field.getType();
	RealTupleType	oldRangeType =
	    (RealTupleType)oldFunctionType.getRange();
	int		oldRangeRank = oldRangeType.getDimension();
	RealType[]	realTypes = new RealType[indexes.length];

	for (int i = 0; i < indexes.length; ++i)
	{
	    int		index = indexes[i];

	    if (index < 0 || index >= oldRangeRank)
		throw new IllegalArgumentException("Index out-of-bounds");

	    realTypes[i] = (RealType)oldRangeType.getComponent(index);
	}

	return new FunctionType(
	    oldFunctionType.getDomain(), new RealTupleType(realTypes));
    }


    /**
     * Gets the default units of the given range components of a FlatField.
     *
     * @param field		The field to be examined.
     * @param indexes		Indexes of the components in the range of the
     *				field.
     * @throws IllegalArgumentException
     *				<code>field == null</code> or
     *				indexes out-of-bounds.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static Unit[]
    getDefaultUnits(FlatField field, int[] indexes)
	throws IllegalArgumentException, VisADException
    {
	if (field == null)
	    throw new IllegalArgumentException("Null field");

	RealTupleType	rangeType = 
	    ((FunctionType)field.getType()).getFlatRange();
	int	rangeRank = rangeType.getDimension();
	Unit[]	units = new Unit[indexes.length];

	for (int i = 0; i < indexes.length; ++i)
	{
	    int		index = indexes[i];

	    if (index < 0 || index >= rangeRank)
		throw new IllegalArgumentException("Index out-of-bounds");

	    RealType	componentType = (RealType)rangeType.getComponent(index);

	    units[i] = componentType.getDefaultUnit();
	}

	return units;
    }


    /**
     * Sets the value of this field at a domain point.
     *
     * @param index		The index of the domain point.
     * @param value		The value to set the domain point to.
     * @throws VisADException	Couldn't set domain point to value.
     * @throws RemoteException	Remove access failure
     */
    public void
    setSample(int index, Data value)
	throws VisADException, RemoteException
    {
	Data		oldValue = field.getSample(index);
	Data		newValue;
	RealTuple	proxyValue = (RealTuple)value;

	if (oldValue instanceof RealTuple)
	{
	    RealTuple	tuple = (RealTuple)oldValue;
	    Real[]	datums = new Real[tuple.getDimension()];

	    for (int i = 0; i < otherIndexes.length; ++i)
	    {
		int	j = otherIndexes[i];
		datums[j] = (Real)tuple.getComponent(j);
	    }

	    for (int i = 0; i < proxyIndexes.length; ++i)
		datums[proxyIndexes[i]] = (Real)proxyValue.getComponent(i);

	    newValue = new RealTuple(datums);
	}
	else
	if (oldValue instanceof Tuple)
	{
	    Tuple	tuple = (Tuple)oldValue;
	    Data[]	datums = new Data[tuple.getDimension()];

	    for (int i = 0; i < otherIndexes.length; ++i)
	    {
		int	j = otherIndexes[i];
		datums[j] = tuple.getComponent(j);
	    }

	    for (int i = 0; i < proxyIndexes.length; ++i)
		datums[proxyIndexes[i]] = proxyValue.getComponent(i);

	    newValue = new Tuple(datums);
	}
	else
	{
	    throw new VisADException("Range not RealTuple or Tuple");
	}

	field.setSample(index, newValue);
    }


    /**
     * Compute the ranges of the data.
     */
    public DataShadow
    computeRanges(ShadowType type, DataShadow shadow)
       throws VisADException
    {
    }


    /**
     * Get values for 'Flat' components in default range Unit-s.
     */
    public double[][]
    getValues()
	throws VisADException
    {
	return null;
    }


    /**
     * Get the range value at the index-th sample.
     */
    public Data
    getSample(int index)
	throws VisADException, RemoteException
    {
	return null;
    }
}
