/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfAdapter.java,v 1.17 2000-04-26 15:45:17 dglo Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import visad.DataImpl;
import visad.FieldImpl;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.QuantityDBImpl;
import visad.data.netcdf.QuantityDBManager;


/**
 * The NetcdfAdapter class adapts a netCDF dataset to a VisAD API.  It is
 * useful for importing a netCDF dataset.
 */
public class
NetcdfAdapter
{
    /*
     * The view of the netCDF datset.
     */
    private final View		view;

    /*
     * The data-item consolidator for the netCDF dataset.
     */
    private final Consolidator	consolidator;


    /**
     * Constructs from a netCDF dataset.
     *
     * @param netcdf		The netCDF dataset to be adapted.
     * @param quantityDB	A quantity database to be used to map netCDF
     *				variables to VisAD Quantity-s.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws RemoteException	Remote data access failure.
     * @throws IOException	Data access I/O failure.
     * @throws BadFormException	Non-conforming netCDF dataset.
     */
    public
    NetcdfAdapter(Netcdf netcdf, QuantityDB quantityDB)
	throws VisADException, RemoteException, IOException, BadFormException
    {
	this(new DefaultView(netcdf, quantityDB), new DefaultConsolidator());
    }


    /**
     * Constructs from a view of a netCDF dataset and a data-item
     * consolidator.
     *
     * @param view		The view of the netCDF dataset to be adapted.
     * @param consolidator	The data-item consolidator.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws RemoteException	Remote data access failure.
     * @throws IOException	Data access I/O failure.
     * @throws BadFormException	netCDF dataset doesn't conform to conventions
     *				implicit in <code>extractor</code>.
     */
    public
    NetcdfAdapter(View view, Consolidator consolidator)
	throws VisADException, RemoteException, IOException, BadFormException
    {
	this.view = view;
	this.consolidator = consolidator;

	VirtualDataIterator	iter = view.getVirtualDataIterator();

	while (iter.hasNext())
	    consolidator.add(iter.next());
    }


    /**
     * Gets the VisAD data object corresponding to the netCDF dataset.
     *
     * @return			The top-level, VisAD data object in the netCDF
     *				dataset.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public DataImpl
    getData()
	throws IOException, VisADException, RemoteException
    {
	return consolidator.getData();
    }


    /**
     * Gets a proxy for the VisAD data object corresponding to the
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
	return consolidator.getProxy();
    }


    /**
     * Gets the view of the netCDF dataset.
     *
     * @return			The view of the netCDF dataset.
     */
    protected View
    getView()
    {
	return view;
    }


    /**
     * Gets the netCDF dataset data-item consolidator.
     *
     * @return			The data-item consolidator.
     */
    protected Consolidator
    getConsolidator()
    {
	return consolidator;
    }


    /**
     * Tests this class.
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
	    NetcdfAdapter	adapter =
		new NetcdfAdapter(file, QuantityDBManager.instance());
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
