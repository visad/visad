/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Plain.java,v 1.14 1998-11-16 18:23:39 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.Schema;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.Data;
import visad.DataImpl;
import visad.UnimplementedException;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.FormNode;
import visad.data.netcdf.in.NetcdfAdapter;
import visad.data.netcdf.out.VisADAdapter;


/**
 * A moderately stupid implementation of a netCDF data form for the
 * storage of persistent data objects on local disk.
 */
public class
Plain
    extends NetCDF
{
    /**
     * The quantity database to use for mapping netCDF variables to 
     * VisAD Quantity-s.
     */
    private final QuantityDB	quantityDB;


    /**
     * Constructs a default, netCDF data form.
     *
     * @throws VisADException	Couldn't create necessary VisAD object
     */
    public
    Plain()
	throws VisADException
    {
	this(StandardQuantityDB.instance());
    }


    /**
     * Constructs a netCDF data form that uses the given quantity database.
     */
    public
    Plain(QuantityDB db)
    {
	super("Plain");
	quantityDB = db;
    }


    /**
     * Save a VisAD data object in this form.
     *
     * @param path			The pathname of the netCDF file to
     *					be created.
     * @param data			The data to be saved.
     * @param replace			Whether to replace an existing file.
     * @exception BadFormException	netCDF can't handle data object
     * @exception VisADException	Couldn't create necessary VisAD object
     * @exception IOException		I/O error.  File might already exist.
     * @exception RemoteException	Remote execution error
     * @exception UnimplementedException
     *					Not yet!
     */
    public synchronized void
    save(String path, Data data, boolean replace)
	throws BadFormException, IOException, RemoteException, VisADException,
	    UnimplementedException
    {
	VisADAdapter	adapter = new VisADAdapter(data);
	Schema		schema = new Schema(adapter);
	NetcdfFile	file = new NetcdfFile(path, replace, /*fill=*/false,
					schema);

	try
	{
	    VariableIterator	iter = file.iterator();

	    while (iter.hasNext())
	    {
		Variable	outVar = iter.next();
		Variable	inVar = adapter.get(outVar.getName());
		int		rank = outVar.getRank();
		int[]		origin = new int[rank];

		for (int i = 0; i < rank; ++i)
		    origin[i] = 0;

		outVar.copyin(origin, inVar);
	    }
	}
	finally
	{
	    file.close();
	}
    }


    /**
     * Add data to an existing data object.
     *
     * @param id	Pathname of the existing netCDF file.
     * @param data	Data to be saved.
     * @param replace	Whether or not to replace duplicate, existing data.
     * @exception BadFormException
     *			netCDF can't handle data object.
     */
    public synchronized void
    add(String id, Data data, boolean replace)
	throws BadFormException
    {
    }


    /**
     * Open an existing netCDF file and return a VisAD data object.
     *
     * @param path	Pathname of the existing netCDF file.
     * @return		A VisAD object corresponding to the netCDF dataset.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    public synchronized DataImpl
    open(String path)
	throws BadFormException, IOException, VisADException
    {
	DataImpl	data;
	NetcdfFile	file = new NetcdfFile(path, /*readonly=*/true);

	try
	{
	    data = new NetcdfAdapter(file, quantityDB).getData();
	}
	finally
	{
	    file.close();
	}

	return data;
    }


    /**
     * Open an existing netCDF file and return a proxy for a VisAD data object.
     *
     * @param path	Pathname of the existing netCDF file.
     * @return		A VisAD object corresponding to the netCDF dataset.
     * @exception BadFormException
     *			The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    public synchronized DataImpl
    openProxy(String path)
	throws BadFormException, IOException, VisADException
    {
	NetcdfFile	file = new NetcdfFile(path, /*readonly=*/true);

	return new NetcdfAdapter(file, quantityDB).getProxy();
    }


    /**
     * Open a URL.
     *
     * @param url	The URL of the netCDF dataset.
     * @return		A VisAD object corresponding to the netCDF datset.
     * @exception UnimplementedException
     *			Not implemented yet.  Always thrown.
     */
    public synchronized DataImpl
    open (URL url)
	throws UnimplementedException
    {
	throw new UnimplementedException("open(URL)");
    }


    /**
     * Return the data forms that are compatible with a data object.
     *
     * @param data	The VisAD data object to be examined.
     * @return		<code>this</code> if <code>data</code> is compatible;
     *			otherwise, <code>null</code>.
     * @exception VisADException	Problem with core VisAD.
     * @exception IOException		Problem with local data access.
     * @exception RemoteException	Problem with remote data access.
     */
    public synchronized FormNode
    getForms(Data data)
	throws VisADException, RemoteException, IOException
    {
	FormNode	form;

	try
	{
	    VisADAdapter	adapter = new VisADAdapter(data);
	    form = this;
	}
	catch (BadFormException e)
	{
	    form = null;
	}

	return form;
    }


    /**
     * Test this class.
     *
     * @param args		Runtime arguments.  Ignored.
     * @exception Exception	Something went wrong.
     */
    public static void main(String[] args)
	throws Exception
    {
	String	inPath;
	String	outPath = "plain.nc";

	if (args.length == 0)
	    inPath = "test.nc";
	else
	    inPath = args[0];

	Plain	plain = new Plain();

	System.out.println("Opening netCDF dataset \"" + inPath + "\"");

	Data	data = plain.open(inPath);

	System.out.println("Data:\n" + data);
	// System.out.println("data.getType().toString():\n" +
	    // data.getType());

	System.out.println("Writing netCDF dataset \"" + outPath + "\"");

	plain.save(outPath, data, /*replace=*/true);
    }
}
