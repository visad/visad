/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfAdapter.java,v 1.4 1998-03-25 15:55:43 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.VariableIterator;
import visad.DataImpl;
import visad.FieldImpl;
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
    protected final Netcdf	netcdf;

    /**
     * The netCDF data objects in the netCDF datset.
     */
    protected final Dictionary	dataSet;

    /**
     * The outermost, netCDF data object.
     */
    protected final NcData	ncData;


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

	dataSet = getDataSet(netcdf);

	ncData = getOutermost(dataSet);
    }


    /**
     * Return the VisAD data objects in the given netCDF dataset.
     *
     * @param netcdf	The netCDF dataset to be adapted.
     * @return		A hashtable of data objects in the netCDF dataset.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected static Hashtable
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

	Hashtable	table = new Hashtable(netcdf.size());

	DomainTable.Enumeration	domEnum = domTable.getEnumeration();
	while (domEnum.hasMoreElements())
	{
	    Domain	domain = domEnum.nextElement();
	    NcData	ncData = NcData.newNcData(domain.getVariables());

	    table.put(ncData.getMathType(), ncData);
	}

	return table;
    }


    /**
     * Return the outermost, netCDF data object in the given domain Dictionary.
     *
     * @param DataSet		The domain dictionary of a netCDF dataset.
     * @return			The outermost data object of the netCDF dataset.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception RemoteException
     *			Remote data access failure.
     */
    protected static NcData
    getOutermost(Dictionary dataSet)
	throws VisADException, RemoteException
    {
	NcData	data;
	int	numData = dataSet.size();

	if (numData == 0)
	    data = null;
	else
	if (numData == 1)
	{
	    Enumeration	enum = dataSet.elements();

	    data = (NcData)enum.nextElement();
	}
	else
	{
	    Enumeration		enum = dataSet.elements();
	    NcData[]		datums = new NcData[numData];

	    for (int i = 0; i < numData; ++i)
		datums[i] = (NcData)enum.nextElement();

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
     * @prerequisite	<code>type</code> is a node in 
     *			<code>getMathType()</code>'s return-value.
     * @return		The data object corresponding to <code>type</code>.
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
	if (type.equals(ncData.getMathType()))
	    return getData();

	NcData	data = (NcData)dataSet.get(type);

	if (data == null)
	    return null;

	return data.getData();
    }


    /**
     * Test this class.
     *
     * @param args	Runtime arguments.  Ignored.
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
	    DataImpl		data = adapter.getData();

	    //System.out.println("data.getType().toString():\n" +
		//data.getType());
	    System.out.println("data.toString():\n" + data);
	}
    }
}
