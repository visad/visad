/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: FileStrategy.java,v 1.3 2000-06-26 20:27:54 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.NetcdfFile;
import visad.*;
import visad.data.BadFormException;
import visad.data.netcdf.*;


/**
 * Provides support for importing netCDF datasets using the strategy of
 * employing FileFlatField-s wherever possible, but merging the data so as to
 * keep the number of FileFlatField-s to a minimum.
 *
 * This class may be subclassed in order to use a different data merger tactic
 * -- one that maximizes the number of FileFlatField-s, for example (see {@link
 * #getMerger()}).
 *
 * @author Steven R. Emmerson
 */
public class FileStrategy
    extends	NetcdfAdapter.Strategy
{
    /**
     * The singleton instance of this class.
     */
    private static FileStrategy	instance;


    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static NetcdfAdapter.Strategy instance()
    {
	if (instance == null)
	{
	    synchronized(FileStrategy.class)
	    {
		if (instance == null)
		    instance = new FileStrategy();
	    }
	}
	return instance;
    }


    /**
     * Constructs from nothing.  Protected to ensure use of 
     * <code>instance()</code> method.
     *
     * @see #instance()
     */
    protected FileStrategy()
    {}


    /**
     * Returns a VisAD data object corresponding to the netCDF dataset.  This
     * method uses the Merger returned by <code>getMerger()</code>.
     *
     * @param adapter		The netCDF-to-VisAD adapter.
     * @return			The top-level, VisAD data object in the
     *				netCDF dataset.
     * @throws VisADException	Problem in core VisAD.  Probably some
     *				VisAD object couldn't be created.
     * @throws IOException	Data access I/O failure.
     * @throws BadFormException	netCDF dataset doesn't conform to
     *				conventions implicit in constructing
     *				View.
     * @throws OutOfMemoryError	Couldn't import netCDF dataset into 
     *				memory.
     * @see #getMerger()
     */
    public DataImpl
    getData(NetcdfAdapter adapter)
	throws IOException, VisADException, RemoteException,
	    BadFormException, OutOfMemoryError
    {
	try
	{
	    return
		adapter.importData(
		    adapter.getView(),
		    getMerger(),
		    FileDataFactory.instance());
	}
	catch (OutOfMemoryError e)
	{
	    throw new OutOfMemoryError(
		getClass().getName() + ".getData(): " +
		"Couldn't import netCDF dataset: " + e.getMessage());
	}
    }


    /**
     * Returns the Merger for cosolidating virtual data objects together.  The
     * Merger returned by this method is <code>Merger.instance()</code>.  This
     * method may be overridden in subclasses to supply a different merger
     * strategy (e.g. maximizing the number of FileFlatField-s).
     * @return			The Merger for cosolidating virtual data 
     *				objects together.
     * @see Merger
     */
    protected Merger getMerger()
    {
	return Merger.instance();
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

	System.setProperty(
	    NetcdfAdapter.IMPORT_STRATEGY_PROPERTY,
	    FileStrategy.class.getName());

	for (int i = 0; i < pathnames.length; ++i)
	{
	    NetcdfFile		file = new NetcdfFile(pathnames[i],
				    /*readonly=*/true);
	    NetcdfAdapter	adapter =
		new NetcdfAdapter(file, QuantityDBManager.instance());
	    DataImpl		data = adapter.getData();

	    System.out.println("data.getClass().getName() = " +
		data.getClass().getName());

	    System.out.println("data.getType().prettyString():\n" +
		data.getType().prettyString());
	}
    }
}
