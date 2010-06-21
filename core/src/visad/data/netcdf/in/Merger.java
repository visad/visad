/*
 * Copyright 2000, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Merger.java,v 1.3 2001-11-27 22:29:35 dglo Exp $
 */

package visad.data.netcdf.in;

import visad.*;


/**
 * Provides support for merging of virtual data objects.  This class merges
 * virtual data objects to the maximum extent possible.
 *
 * @author Steven R. Emmerson
 */
public class
Merger
{
    /**
     * The singleton instance.
     */
    private static Merger	instance;


    /**
     * Constructs from nothing.
     */
    protected Merger()
    {}


    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static Merger instance()
    {
	if (instance == null)
	{
	    synchronized(Merger.class)
	    {
		if (instance == null)
		    instance = new Merger();
	    }
	}

	return instance;
    }


    /**
     * Merges two virtual data objects.  The order of the objects sub-components
     * is preserved.
     *
     * @param data1		The first virtual data object.
     * @param data2		The second virtual data object.
     * @return			A virtual data object comprising the ordered
     *				merger of the two virtual data objects if 
     *				possible; otherwise <code>null</code>.  May be
     *				one of the input objects.
     * throws VisADException	VisAD failure.
     */
    public VirtualData merge(VirtualData data1, VirtualData data2)
	throws VisADException
    {
	return
	    data1 instanceof VirtualTuple
		? (VirtualData)merge((VirtualTuple)data1, data2)
		: data1 instanceof VirtualField
		    ? (VirtualData)merge((VirtualField)data1, data2)
		    : (VirtualData)null;
    }


    /**
     * Merges a virtual tuple with another virtual data object.  Order is
     * preserved.
     *
     * @param tuple		The virtual tuple.
     * @param data		The other virtual data object.
     * @return			<code>tuple</code>.
     * throws VisADException	VisAD failure.
     */
    protected VirtualTuple merge(VirtualTuple tuple, VirtualData data)
	throws VisADException
    {
	if (data instanceof VirtualTuple)
	{
	    merge(tuple, (VirtualTuple)data);
	}
	else
	{
	    int		n = tuple.size();
	    boolean	merged = false;

	    for (int i = 0; i < n; ++i)
	    {
		VirtualData     element = tuple.get(i);
		VirtualData     mergedElement = merge(element, data);

		if (mergedElement != null)
		{
		    tuple.replace(i, mergedElement);
		    merged = true;
		    break;
		}
	    }

	    if (!merged)
		tuple.add(data);
	}

	return tuple;
    }


    /**
     * Merges two virtual tuples.
     *
     * @param tuple1		The first virtual tuple.
     * @param tuple2		The second virtual tuple.
     * @return			<code>tuple1</code>.
     * throws VisADException	VisAD failure.
     */
    protected VirtualTuple merge(VirtualTuple tuple1, VirtualTuple tuple2)
	throws VisADException
    {
	int	n = tuple2.size();

	for (int i = 0; i < n; ++i)
	{
	    VirtualData	element = tuple2.get(i);

	    merge(tuple1, element);
	}

	return tuple1;		// always successful
    }


    /**
     * Merges a virtual field with another virtual data object.  Order is
     * preserved.
     *
     * @param field		The virtual field.
     * @param data		The other virtual data object.
     * @return			A virtual field comprising the merger of the
     *				input objects if possible; otherwise 
     *				<code>null</code>.  May be <code>field</code>.
     * throws VisADException	VisAD failure.
     */
    protected VirtualField merge(VirtualField field, VirtualData data)
	throws VisADException
    {
	return
	    field instanceof VirtualFlatField
		? (VirtualField)merge((VirtualFlatField)field, data)
		: data instanceof VirtualField
		    ? merge(field, (VirtualField)data)
		    : (VirtualField)null;
    }


    /**
     * Merges a virtual field with another virtual field.  Order is
     * preserved.
     *
     * @param field1		The first virtual field.
     * @param field2		The second virtual field.
     * @return			A virtual field comprising the merger of the
     *				input fields if possible; otherwise 
     *				<code>null</code>.  May be <code>field1</code>.
     * throws VisADException	VisAD failure.
     */
    protected VirtualField merge(VirtualField field1, VirtualField field2)
	throws VisADException
    {
	SampledSet	domain1 = field1.getDomainSet();

	return
	    (field1.getFunctionType().getDomain().equals(
		field2.getFunctionType().getDomain()) &&
	    domain1.equals(field2.getDomainSet()))
		? VirtualField.newVirtualField(
		    domain1, 
		    merge(field1.getRangeTuple(), field2.getRangeTuple()))
		: (VirtualField)null;
    }


    /**
     * Merges a virtual flat-field with another virtual data object.  Order is
     * preserved.
     *
     * @param field		The virtual flat-field.
     * @param data		The other virtual data object.
     * @return			A virtual field comprising the merger of the
     *				input objects if possible; otherwise 
     *				<code>null</code>.  May be <code>field</code>.
     * throws VisADException	VisAD failure.
     */
    protected VirtualField merge(VirtualFlatField field, VirtualData data)
	throws VisADException
    {
	return
	    data instanceof VirtualFlatField
		? (VirtualField)merge(field, (VirtualFlatField)data)
		: data instanceof VirtualField
		    ? (VirtualField)merge(field, (VirtualField)data)
		    : (VirtualField)null;
    }


    /**
     * Merges a virtual flat-field with a virtual field.  Simply uses
     * <code>merge(VirtualField, VirtualField)</code>.  This method may
     * be overridden by subclasses.
     *
     * @param field1		The virtual flat-field.
     * @param field2		The virtual field.
     * @return			A virtual field comprising the merger of
     *				the input objects if possible; otherwise 
     *				<code>null</code>.
     * throws VisADException	VisAD failure.
     * @see #merge(VirtualField, VirtualField)
     */
    protected VirtualField merge(VirtualFlatField field1, VirtualField field2)
	throws VisADException
    {
	return 
	    merge((VirtualField)field1, (VirtualField)field2);
    }


    /**
     * Merges a virtual flat-field with another virtual flat-field.  Simply uses
     * <code>merge(VirtualFlatField, VirtualFlatField)</code>.  This method may
     * be overridden by subclasses.
     *
     * @param field1		The virtual flat-field.
     * @param field2		The other virtual flat-field.
     * @return			A virtual flat-field comprising the merger of
     *				the input objects if possible; otherwise 
     *				<code>null</code>.  May be <code>field1</code>.
     * throws VisADException	VisAD failure.
     * @see #merge(VirtualFlatField, VirtualFlatField)
     */
    protected VirtualFlatField merge(
	    VirtualFlatField field1, VirtualFlatField field2)
	throws VisADException
    {
	return (VirtualFlatField)
	    merge((VirtualField)field1, (VirtualField)field2);
    }
}
