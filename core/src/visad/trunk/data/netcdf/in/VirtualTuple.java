/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: VirtualTuple.java,v 1.1 1998-09-23 17:31:38 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.Data;
import visad.DataImpl;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarType;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;


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
     * The components that constitute this virtual tuple.
     */
    private final Vector	components = new Vector();

    /**
     * The VisAD MathType of the consolidated data items.
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
    }


    /**
     * Construct from a virtual data object.
     *
     * @param data		A virtual data object.
     */
    public
    VirtualTuple(VirtualData data)
    {
	add(data);
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
    public void
    add(VirtualData data)
    {
	components.add(data);
	isDirty = true;
    }


    /**
     * Gets the VisAD MathType of this virtual tuple.
     *
     * @return			The VisAD MathType of the consolidated data 
     *				items or <code>null</code> if no data items.
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
    public void
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
	DataImpl	data = null;
	int		size = size();

	if (size() == 1)
	{
	    data = ((VirtualData)components.get(0)).getData(context);
	}
	else if (size > 1)
	{
	    MathType	type = getType();

	    if (type instanceof RealTupleType)
	    {
		int		n = size();
		Real[]	reals = new Real[n];

		for (int i = 0; i < n; ++i)
		    reals[i] = (Real)((VirtualData)components.get(i)).
			getData(context);

		data = new RealTuple((RealTupleType)type, reals,
				     /*(CoordinateSystem)*/null);
	    }
	    else if (type instanceof TupleType)
	    {
		Data[]	datas = new DataImpl[size()];

		for (int i = 0; i < datas.length; ++i)
		    datas[i] =
			((VirtualData)components.get(i)).getData(context);

		data = new Tuple((TupleType)type, datas, /*copy=*/false);
	    }
	}

	return data;
    }
}
