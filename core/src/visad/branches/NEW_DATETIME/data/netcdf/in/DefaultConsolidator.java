/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DefaultConsolidator.java,v 1.1 1998-09-23 17:31:31 steve Exp $
 */

package visad.data.netcdf.in;

import java.rmi.RemoteException;
import java.util.Vector;
import java.io.IOException;
import visad.Data;
import visad.DataImpl;
import visad.FunctionType;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.ScalarType;
import visad.Text;
import visad.TextType;
import visad.Tuple;
import visad.TupleType;
import visad.TypeException;
import visad.VisADException;


/**
 * Supports default consolidation of VisAD data items.
 */
public class
DefaultConsolidator
    extends	Consolidator
{
    /**
     * The data item collection.
     */
    private final VirtualTuple	topTuple = new VirtualTuple();


    /**
     * Adds a data item.
     *
     * @param data		The virtual, VisAD data item to be added.
     * @throws TypeException	Unknown data item type.
     */
    public void
    add(VirtualData data)
	throws TypeException, VisADException
    {
	combine(topTuple, data);
    }
    

    /**
     * Combines two virtual data objects, if possible.
     *
     * @param data1		The first, virtual data object.
     * @param data2		The second, virtual data object.
     * @return			An virtual data object that is the combination
     *				of <code>data1</code> and <code>data2</code>
     *				or <code>null</code> if it's not possible to
     *				combine them.
     * throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static VirtualData
    combine(VirtualData data1, VirtualData data2)
	throws VisADException
    {
	VirtualData	combinedData;

	if (data1 instanceof VirtualTuple)
	{
	    combinedData = combine((VirtualTuple)data1, data2);
	}
	else if (data1 instanceof VirtualField)
	{
	    combinedData = combine((VirtualField)data1, data2);
	}
	else
	{
	    combinedData = null;
	}

	return combinedData;
    }


    /**
     * Combines a virtual tuple with another virtual data object.
     *
     * @param tuple		The virtual tuple.
     * @param data		The other virtual data object.
     * @return			<code>tuple</code> -- which will contain
     *				<code>data</code> in it somewhere.
     * throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static VirtualTuple
    combine(VirtualTuple tuple, VirtualData data)
	throws VisADException
    {
	if (data instanceof VirtualTuple)
	{
	    return combine(tuple, (VirtualTuple)data);
	}
	else
	{
	    boolean	notCombined = true;
	    int		componentCount = tuple.size();

	    for (int i = 0; notCombined && i < componentCount; ++i)
	    {
		VirtualData	component = tuple.get(i);
		VirtualData	combinedComponent = combine(component, data);

		if (combinedComponent != null)
		{
		    tuple.replace(i, combinedComponent);
		    notCombined = false;
		}
	    }

	    if (notCombined)
		tuple.add(data);
	}

	return tuple;		// always successful
    }


    /**
     * Combines two virtual tuples.
     *
     * @param tuple1		The first tuple.
     * @param tuple2		The second tuple.
     * @return			<code>tuple1</code> -- which will contain the
     *				components of <code>tuple2</code>.
     * throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static VirtualTuple
    combine(VirtualTuple tuple1, VirtualTuple tuple2)
	throws VisADException
    {
	int	n = tuple2.size();

	for (int i = 0; i < n; ++i)
	{
	    VirtualData	component = tuple2.get(i);

	    combine(tuple1, component);
	}

	return tuple1;		// always successful
    }


    /**
     * Combines a virtual field with another virtual data object.
     *
     * @param field		The virtual field.
     * @param data		The other virtual data object.
     * @return			<code>field</code> if and only if it
     *				contains <code>data</code> somewhere in it;
     *				otherwise <code>null</code>.
     * throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static VirtualField
    combine(VirtualField field1, VirtualData data)
	throws VisADException
    {
	VirtualField	combinedField = null;	// default

	if (data instanceof VirtualField)
	{
	    VirtualField	field2 = (VirtualField)data;
	    FunctionType	type1 = field1.getFunctionType();
	    FunctionType	type2 = field2.getFunctionType();
	    SampledSet		domainSet1 = field1.getDomainSet();
	    SampledSet		domainSet2 = field2.getDomainSet();
	    RealTupleType	domainType1 = type1.getDomain();
	    RealTupleType	domainType2 = type2.getDomain();

	    if (domainType1.equals(domainType2) &&
	        domainSet1.equals(domainSet2))
	    {
		VirtualTuple	range1Tuple = field1.getRangeTuple();
		VirtualTuple	range2Tuple = field2.getRangeTuple();
		VirtualTuple	combinedRangeTuple =
		    combine(range1Tuple, range2Tuple);
		MathType	combinedRangeType =
		    combinedRangeTuple.getType();
		FunctionType	combinedFunctionType =
		    new FunctionType(domainType1, combinedRangeType);

		combinedField = VirtualField.newVirtualField(
		    combinedFunctionType, domainSet1, combinedRangeTuple);
	    }
	}

	return combinedField;
    }


    /**
     * Gets the VisAD MathType of the consolidated data items.
     *
     * @return			The VisAD MathType of the consolidated data 
     *				items or <code>null</code> if no data items.
     */
    public MathType
    getType()
	throws VisADException
    {
	return topTuple.getType();
    }


    /**
     * Gets the consolidated, VisAD data object.
     *
     * @return			The consolidated, VisAD data object or
     *				<code>null</code> if there is no data object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote access failure.
     * @throws IOException	I/O failure.
     */
    public DataImpl
    getData()
	throws VisADException, RemoteException, IOException
    {
	return topTuple.getData();
    }


    /**
     * Gets a proxy for the consolidated, VisAD data object.
     *
     * @return			A proxy for the consolidated, VisAD data object.
     */
    public DataImpl
    getProxy()
    {
	return null;	// TODO
    }
}
