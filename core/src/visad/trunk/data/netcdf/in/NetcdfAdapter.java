/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfAdapter.java,v 1.10 1998-08-10 16:53:32 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.VariableIterator;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FunctionType;
import visad.MathType;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * The NetcdfAdapter class adapts a netCDF dataset to a VisAD API.  It is 
 * useful for importing a netCDF dataset.
 */
public class
NetcdfAdapter
{
    /**
     * The netCDF dataset.
     */
    private final Netcdf	netcdf;

    /**
     * The netCDF data objects in the netCDF datset.
     */
    private final Map		map;

    /**
     * The outermost, netCDF data object.
     */
    private final NcData	ncData;


    /**
     * Construct from a netCDF dataset.
     *
     * @param netcdf	The netCDF dataset to be adapted.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception RemoteException
     *			Remote data access failure.
     * @exception IOException
     *			Data access I/O failure.
     */
    public
    NetcdfAdapter(Netcdf netcdf)
	throws VisADException, RemoteException, IOException
    {
	this.netcdf = netcdf;

	map = getDataSet(netcdf);

	ncData = getOutermost(map);
	map.put(new Key(ncData.getMathType(), map.size()), ncData);
    }


    /**
     * Return the VisAD data objects in the given netCDF dataset.
     *
     * @param netcdf	The netCDF dataset to be examined.
     * @return		A Map of data objects in the netCDF dataset.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected static Map
    getDataSet(Netcdf netcdf)
	throws VisADException, BadFormException, IOException
    {
	DomainTable		domTable = new DomainTable(netcdf.size());

	VariableIterator	varIter = netcdf.iterator();
	while (varIter.hasNext())
	{
	    NcVar	var = NcVar.newNcVar(varIter.next(), netcdf);

	    // TODO: support text
	    if (!var.isText() && !var.isCoordinateVariable())
	    {
		domTable.put(var);
	    }
	}

	Map	map = new TreeMap();

	DomainTable.Enumeration	domEnum = domTable.getEnumeration();
	while (domEnum.hasMoreElements())
	{
	    NcData	ncData = NcData.newNcData(domEnum.nextElement());

	    map.put(new Key(ncData.getMathType(), map.size()), ncData);
	}

	return map;
    }


    /**
     * Inner class for a key to the map.
     */
    static class
    Key
	implements	Comparable
    {
	/**
	 * The MathType.
	 */
	protected final MathType	type;

	/**
	 * The sequence number.
	 */
	protected final int		seqNo;


	/**
	 * Construct from a VisAD MathType and a sequence number.
	 */
	Key(MathType type, int seqNo)
	    throws VisADException
	{
	    this.type = type;
	    this.seqNo = seqNo;
	}


	/**
	 * Compare this key to another.
	 */
	public int
	compareTo(Object key)
	{
	    Key	that = (Key)key;

	    return type.equals(that.type)
			? 0
			: seqNo - that.seqNo;
	}


	/**
	 * Convert this key to a string.
	 */
	public String
	toString()
	{
	    return type.toString();
	}
    }


    /**
     * Return the outermost, netCDF data object in the given map.
     *
     * @param DataSet		The map of object in a netCDF dataset.
     * @return			The outermost data object of the netCDF dataset.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception RemoteException
     *			Remote data access failure.
     */
    protected static NcData
    getOutermost(Map map)
	throws VisADException, RemoteException
    {
	NcData	data;
	int	numData = map.size();

	if (numData == 0)
	    data = null;
	else
	if (numData == 1)
	{
	    Iterator	iter = map.values().iterator();

	    data = (NcData)iter.next();
	}
	else
	{
	    Iterator		iter = map.values().iterator();
	    NcData[]		datums = new NcData[numData];

	    for (int i = 0; i < numData; ++i)
		datums[i] = (NcData)iter.next();

	    data = new NcTuple(datums);
	}

	return data;
    }


    /**
     * Return the outermost, VisAD data object.
     *
     * @return		The top-level, VisAD data object in the netCDF dataset.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    public DataImpl
    getData()
	throws IOException, VisADException
    {
	return ncData.getData();
    }


    /**
     * Return the VisAD data object corresponding to the given MathType.
     *
     * @param type	The MathType of the data object to be returned.
     * @return		The data object corresponding to <code>type</code>
     *			or <code>null</code> if there's no such object.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected DataImpl
    getData(MathType type)
	throws IOException, VisADException
    {
	NcData	ncData = (NcData)map.get(type);

	return ncData == null
		? null
		: ncData.getData();
    }


    /**
     * Return a proxy for the outermost, VisAD data object.
     *
     * @return		The top-level, VisAD data object in the netCDF dataset.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    public DataImpl
    getProxy()
	throws IOException, VisADException
    {
	return ncData.getProxy();
    }


    /**
     * Return a proxy for the VisAD data object corresponding to the given
     * MathType.
     *
     * @param type	The MathType of the data object to be returned.
     * @return		The data object corresponding to <code>type</code>
     *			or <code>null</code> if there's no such object.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected DataImpl
    getProxy(MathType type)
	throws IOException, VisADException
    {
	NcData	ncData = (NcData)map.get(type);

	return ncData == null
		? null
		: ncData.getProxy();
    }


    /**
     * Return the netCDF dataset.
     */
    protected Netcdf
    getNetcdf()
    {
	return netcdf;
    }


    /**
     * Return the netCDF data objects in the netCDF datset.
     */
    protected Map
    getMap()
    {
	return map;
    }


    /**
     * Return the outermost, netCDF data object.
     */
    protected NcData
    getNcData()
    {
	return ncData;
    }


    /**
     * Test this class.
     *
     * @param args		File pathnames.
     * @exception Exception	Something went wrong.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	String[]	pathnames;

	if (args.length == 0)
	    pathnames = new String[] {"test.nc"};
	else
	    pathnames = args;

	for (int i = 0; i < pathnames.length; ++i)
	{
	    NetcdfFile		file = new NetcdfFile(pathnames[i], 
				    /*readonly=*/true);
	    NetcdfAdapter	adapter = new NetcdfAdapter(file);
	    // DataImpl		data = adapter.getProxy();
	    DataImpl		data = adapter.getData();

	    // System.out.println("data.getType().toString():\n" +
		// data.getType());
	    System.out.println("Domain set:\n" + 
		((FieldImpl)data).getDomainSet());
	    System.out.println("Data:\n" + data);
	}
    }
}
