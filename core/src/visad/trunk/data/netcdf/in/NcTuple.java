/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcTuple.java,v 1.7 1998-09-11 16:33:51 steve Exp $
 */

package visad.data.netcdf.in;

import java.rmi.RemoteException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import visad.Data;
import visad.DataImpl;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;


/**
 * Adapts a tuple of netCDF data objects to a VisAD Tuple.
 *
 * Instances are mutable.
 */
class
NcTuple
{
    /**
     * The elements of the tuple.
     */
    private final Vector	datas = new Vector(1);


    /**
     * Constructs from nothing.  Creates an empty tuple (which makes a
     * nice, top-level data object to which to add other data objects).
     *
     * @postcondition	<code>size() == 0</code>
     */
    public
    NcTuple()
	throws VisADException
    {
    }


    /**
     * Gets the VisAD MathType of this data object.
     *
     * @return			The VisAD MathType of this data object.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public MathType
    getMathType()
	throws VisADException
    {
	return newMathType(getMathTypes());
    }


    /**
     * Creates the VisAD MathType of the given array of data objects.
     *
     * @param types		The VisAD MathTypes to be considered together.
     * @return			The VisAD MathType of the given array of data 
     *				objects.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public static MathType
    newMathType(MathType[] types)
	throws VisADException
    {
	MathType	type;

	if (types.length == 1)
	{
	    type = types[0];
	}
	else
	{
	    boolean	allRealTypes = true;

	    for (int i = 0; i < types.length; ++i)
	    {
		if (!(types[i] instanceof RealType))
		{
		    allRealTypes = false;
		    break;
		}
	    }

	    if (!allRealTypes)
	    {
		type = new TupleType(types);
	    }
	    else
	    {
		RealType[]	realTypes = new RealType[types.length];

		for (int i = 0; i < types.length; ++i)
		    realTypes[i] = (RealType)types[i];

		type = new RealTupleType(realTypes);
	    }
	}

	return type;
    }


    /**
     * Gets the number of components in this tuple.
     *
     * @return			The number of components in this tuple.
     */
    public int
    size()
    {
	return datas.size();
    }


    /**
     * Gets a component of the tuple.
     *
     * @param index		The index of the component.
     * @precondition		<code>index >= 0 && index < size()</code>
     * @return			The <code>index</code>th component.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public NcData
    get(int index)
	throws VisADException
    {
	if (index < 0 || index >= size())
	    throw new VisADException("Index out of bounds");

	return (NcData)datas.get(index);
    }


    /**
     * Gets the MathType-s of the tuple components as an array.
     *
     * @return			The MathType-s of the tuple components.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     */
    public MathType[]
    getMathTypes()
	throws VisADException
    {
	MathType[]	types = new MathType[datas.size()];
	int		i = 0;

	for (Iterator iter = datas.iterator(); iter.hasNext(); )
	    types[i++] = ((NcData)iter.next()).getMathType();

	return types;
    }


    /**
     * Adds another data object to this one.
     *
     * @param data		The data to be added.
     * @return			The appropriate, top-level data object.
     * @postcondition		RETURN_VALUE<code>.wasCombined() == true</code>
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws IOException	Data access I/O failure.
     */
    public void
    addData(NcData data)
	throws VisADException, IOException
    {
	int	i = 0;
	boolean	done = false;

	/*
	 * First try the individual elements of the tuple.
	 */
	for (Iterator iter = datas.iterator(); iter.hasNext(); ++i)
	{
	    NcData	newData = ((NcData)iter.next()).tryCombine(data);

	    if (newData.wasCombined())
	    {
		datas.set(i, newData);
		done = true;
		break;
	    }
	}

	/*
	 * If that was unsuccessful, then add the data object to this tuple as
	 * another element.
	 */
	if (!done)
	    datas.addElement(data);
    }


    /**
     * Provides support for different data access mechanisms.
     */
    protected abstract class
    Accesser
    {
	protected DataImpl
	getData()
	    throws VisADException, IOException, RemoteException
	{
	    DataImpl	data;

	    if (size() == 1)
	    {
		data = get(0).getData();
	    }
	    else
	    {
		TupleType	type = (TupleType)getMathType();
		Data[]		values = new DataImpl[type.getDimension()];
		int		i = 0;

		for (Iterator iter = datas.iterator(); iter.hasNext(); )
		    values[i++] = getData((NcData)iter.next());

		data = type instanceof RealTupleType
			? new RealTuple((RealTupleType)type, (Real[])values,
					/*(CoordinateSystem)*/null)
			: new Tuple(type, values, /*copy=*/false);
	    }

	    return data;
	}

	protected abstract DataImpl
	getData(NcData data)
	    throws VisADException, IOException;
    }


    /**
     * Modifies Accesser for getting the actual data.
     */
    protected class
    ActualAccesser
	extends Accesser
    {
	protected DataImpl
	getData(NcData data)
	    throws VisADException, IOException
	{
	    return data.getData();
	}
    }


    /**
     * Modifies Accesser for getting a proxy for the actual data.
     */
    protected class
    ProxyAccesser
	extends Accesser
    {
	protected DataImpl
	getData(NcData data)
	    throws VisADException, IOException
	{
	    return data.getProxy();
	}
    }


    /**
     * Return the VisAD data object corresponding to this netCDF data object.
     *
     * @return			The VisAD data object corresponding to the
     *				netCDF data object.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData()
	throws VisADException, IOException
    {
	return new ActualAccesser().getData();
    }


    /**
     * Return a proxy for the VisAD data object corresponding to this 
     * netCDF data object.
     *
     * @return			The VisAD data object corresponding to the
     *				netCDF data object.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getProxy()
	throws VisADException, IOException
    {
	return new ProxyAccesser().getData();
    }


    /**
     * Return the VisAD data object corresponding to this netCDF data object.
     *
     * @param accesser		The data access mechanism.
     * @return			The VisAD data object corresponding to the
     *				netCDF data object.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    protected DataImpl
    getData(Accesser accesser)
	throws VisADException, IOException
    {
	TupleType	type = (TupleType)getMathType();
	Data[]		values = new DataImpl[type.getDimension()];
	int		i = 0;

	for (Iterator iter = datas.iterator(); iter.hasNext(); )
	    values[i++] = accesser.getData((NcData)iter.next());

	return type instanceof RealTupleType
		? new RealTuple((RealTupleType)type, (Real[])values,
				/*(CoordinateSystem)*/null)
		: new Tuple(type, values, /*copy=*/false);
    }


    /**
     * Factory method for creating the VisAD data object corresponding to an
     * array of VisAD data objects considered together.
     *
     * @param datas		The VisAD data objects
     * @return			The VisAD data object corresponding to 
     *				<code>datas</code>.
     * @throws VisADException	Couldn't create necessary VisAD data object.
     * @throws RemoteException	Remote data access failure.
     */
    public static DataImpl
    newData(DataImpl[] datas)
	throws VisADException, RemoteException
    {
	DataImpl	data;

	if (datas.length == 1)
	{
	    data = datas[0];
	}
	else
	{
	    boolean	allReal = true;

	    for (int i = 0; i < datas.length; ++i)
	    {
		if (!(datas[i] instanceof Real))
		{
		    allReal = false;
		    break;
		}
	    }

	    data = allReal
		    ? new RealTuple((Real[])datas)
		    : new Tuple(datas);
	}

	return data;
    }
}
