/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualTuple.java,v 1.4 2001-01-08 17:13:16 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;


/**
 * Provides support for a virtual VisAD Tuple.
 *
 * Instances are mutable.
 */
public class
VirtualTuple
    extends	VirtualData
{
    /**
     * The factory for creating VisAD data objects.
     */
    private DataFactory		dataFactory = DataFactory.instance();

    /**
     * The components that constitute this virtual tuple.
     */
    private final Vector	components;

    /**
     * The VisAD MathType of the merged data items.
     */
    private MathType		mathType = null;

    /**
     * Whether or not the mathType needs to be recomputed.
     */
    private boolean		isDirty = true;


    /**
     * Constructs from nothing.
     */
    public
    VirtualTuple()
    {
	this(0);
    }


    /**
     * Constructs from an estimate of the number of elements to contain.
     */
    private
    VirtualTuple(int n)
    {
	components = new Vector(n);
    }


    /**
     * Constructs from a virtual data object.
     *
     * @param data		A virtual data object.
     */
    public
    VirtualTuple(VirtualData data)
    {
	this(1);
	add(data);
    }


    /**
     * Constructs from a 1-D array of virtual data objects.  Order is preserved.
     *
     * @param datas		A 1-D array of virtual data objects.
     */
    public
    VirtualTuple(VirtualData[] datas)
    {
	this(datas.length);
	for (int i = 0; i < datas.length; ++i)
	    add(datas[i]);
    }


    /**
     * Returns the number of components in this tuple.
     *
     * @return			The number of components in this tuple.
     */
    public int
    size()
    {
	return components.size();
    }


    /**
     * Adds a component to this tuple.
     *
     * @param data		The component to be added.
     */
    public synchronized void
    add(VirtualData data)
    {
	components.add(data);
	isDirty = true;
    }


    /**
     * Gets the VisAD MathType of this virtual tuple.
     *
     * @return			The VisAD MathType of the merged data
     *				items or <code>null</code> if no data items.
     * @throws VisADException	VisAD failure.
     */
    public MathType
    getType()
	throws VisADException
    {
	if (isDirty)
	{
	    int		componentCount = size();

	    if (componentCount == 0)
	    {
		mathType = null;
	    }
	    else if (componentCount == 1)
	    {
		mathType = ((VirtualData)components.get(0)).getType();
	    }
	    else
	    {
		MathType[]	types = new MathType[componentCount];
		boolean		allRealTypes = true;

		for (int i = 0; i < componentCount; ++i)
		{
		    types[i] = ((VirtualData)components.get(i)).getType();
			if (!(types[i] instanceof RealType))
			    allRealTypes = false;
		}

		if (!allRealTypes)
		{
		    mathType = new TupleType(types);
		}
		else
		{
		    RealType[]	realTypes = new RealType[componentCount];

		    for (int i = 0; i < componentCount; ++i)
			realTypes[i] = (RealType)types[i];

		    mathType = new RealTupleType(realTypes);
		}
	    }

	    isDirty = false;
	}

	return mathType;
    }


    /**
     * Gets a component of this tuple.
     *
     * @param index		The index of the component to get.
     * @return			The <code>index</code>-th component.
     * @throws java.lang.ArrayIndexOutOfBoundsException
     */
    public VirtualData
    get(int index)
	throws ArrayIndexOutOfBoundsException
    {
	return (VirtualData)components.get(index);
    }


    /**
     * Replaces a component of this tuple.
     *
     * @param index		The index of the component to replace.
     * @param data		The new component.
     * @throws java.lang.ArrayIndexOutOfBoundsException
     */
    public synchronized void
    replace(int index, VirtualData data)
	throws ArrayIndexOutOfBoundsException
    {
	components.set(index, data);
	isDirty = true;
    }


    /**
     * Gets the VisAD data object of this tuple, in context.
     *
     * @param context		The context for retrieving the data object.
     * @return			The VisAD data object or <code>null</code>
     *				if there is no data.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote access failure.
     * @throws IOException	I/0 failure.
     */
    public DataImpl
    getData(Context context)
	throws VisADException, RemoteException, IOException
    {
	return getDataFactory().newData(context, this);
    }


    /**
     * Clears this instance.
     */
    public synchronized void clear()
    {
	components.clear();
	mathType = null;
	isDirty = true;
    }


    /**
     * Clones this instance.
     *
     * @return			A (deep) clone of this instance.
     */
    public synchronized Object clone()
    {
	int		n = size();
	VirtualTuple	clone = new VirtualTuple(n);

	for (int i = 0; i < n; ++i)
	    clone.add((VirtualData)get(i).clone());

	return clone;
    }


    /**
     * Sets the factory used to create the VisAD data object corresponding to
     * this tuple and contained elements.
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
     * @return factory		The factory for creating VisAD data objects.
     */
    public DataFactory getDataFactory()
    {
	return dataFactory;
    }
}
