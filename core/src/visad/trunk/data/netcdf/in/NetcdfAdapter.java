/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfAdapter.java,v 1.12 1998-09-11 15:00:55 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.VariableIterator;
import visad.DataImpl;
import visad.FieldImpl;
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

    /*
     * The top-level tuple of the netCDF dataset.
     */
    private final NcTuple	tuple;


    /**
     * Constructs from a netCDF dataset.
     *
     * @param netcdf		The netCDF dataset to be adapted.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws RemoteException	Remote data access failure.
     * @throws IOException	Data access I/O failure.
     */
    public
    NetcdfAdapter(Netcdf netcdf)
	throws VisADException, RemoteException, IOException
    {
	tuple = new NcTuple();

	this.netcdf = netcdf;

	VariableIterator	varIter = netcdf.iterator();

	while (varIter.hasNext())
	{
	    NcVar	var = NcVar.newNcVar(varIter.next(), netcdf);

	    // TODO: support text
	    if (!var.isText() && !var.isCoordinateVariable())
	    {
		NcData	data = NcData.newNcData(var);

		tuple.addData(data);
	    }
	}
    }


    /**
     * Return the VisAD data object corresponding to the netCDF dataset.
     *
     * @return			The top-level, VisAD data object in the netCDF 
     *				dataset.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData()
	throws IOException, VisADException
    {
	return tuple.getData();
    }


    /**
     * Return a proxy for the VisAD data object corresponding to the 
     * netCDF dataset.
     *
     * @return			The top-level, VisAD data object in the netCDF 
     *				dataset.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD 
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getProxy()
	throws IOException, VisADException
    {
	return tuple.getProxy();
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
     * Test this class.
     *
     * @param args		File pathnames.
     * @throws Exception	Something went wrong.
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
	    // Data		data = adapter.getProxy();
	    DataImpl		data = adapter.getData();

	    System.out.println("data.getType().prettyString():\n" +
		data.getType().prettyString());
	    // System.out.println("Domain set:\n" + 
		// ((FieldImpl)data).getDomainSet());
	    // System.out.println("Data:\n" + data);
	}
    }
}
