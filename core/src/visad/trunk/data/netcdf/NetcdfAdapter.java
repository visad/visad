/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfAdapter.java,v 1.8 1998-03-17 15:54:56 steve Exp $
 */

package visad.data.netcdf;

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
     * The netCDF functions in the netCDF datset.
     */
    protected final Dictionary	functionSet;

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
    NetcdfAdapter(Netcdf netcdf)
	throws VisADException, RemoteException, IOException
    {
	this.netcdf = netcdf;

	functionSet = setFunctionSet(netcdf);

	ncData = setOutermost(functionSet);
    }


    /**
     * Set the VisAD data objects in the netCDF dataset.
     *
     * @param netcdf	The netCDF dataset to be adapted.
     * @return		A hashtable of functions in the netCDF dataset.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected static Hashtable
    setFunctionSet(Netcdf netcdf)
	throws VisADException, BadFormException, IOException
    {
	DomainTable		domTable = new DomainTable(netcdf.size());

	VariableIterator	varIter = netcdf.iterator();
	while (varIter.hasNext())
	{
	    ImportVar	var = ImportVar.create(varIter.next(), netcdf);

	    // TODO: support scalars
	    if (!var.isText() && !var.isCoordinateVariable() &&
		var.getRank() > 0)
	    {
		domTable.put(var);
	    }
	}

	Hashtable	table = new Hashtable(netcdf.size());

	DomainTable.Enumeration	domEnum = domTable.getEnumeration();
	while (domEnum.hasMoreElements())
	{
	    Domain	domain = domEnum.nextElement();
	    NcDim[]	dims = domain.getDimensions();
	    NcFunction	function =
		(domain.getRank() <= 1 || !dims[0].isTime())
		    ? new NcFunction(domain.getVariables())
		    : new NcNestedFunction(domain.getVariables());

	    table.put(function.getMathType(), function);
	}

	return table;
    }


    /**
     * Set the outermost, netCDF data object.
     *
     * @param functionSet	The set of functions in a netCDF dataset.
     * @return			The outermost data object of netCDF dataset.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception RemoteException
     *			Remote data access failure.
     */
    protected static NcData
    setOutermost(Dictionary functionSet)
	throws VisADException, RemoteException
    {
	NcData	data;
	int	numFunctions = functionSet.size();

	if (numFunctions == 0)
	    data = null;
	else
	if (numFunctions == 1)
	{
	    Enumeration	enum = functionSet.elements();

	    data = (NcFunction)enum.nextElement();
	}
	else
	{
	    Enumeration		enum = functionSet.elements();
	    NcFunction[]	functions = new NcFunction[numFunctions];

	    for (int i = 0; i < numFunctions; ++i)
		functions[i] = (NcFunction)enum.nextElement();

	    data = new NcTuple(functions);
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
    DataImpl
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

	NcFunction	function = (NcFunction)functionSet.get(type);

	if (function == null)
	    return null;

	return function.getData();
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
