/*
 * Copyright 2000, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: FileDataFactory.java,v 1.3 2001-01-08 17:10:31 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;


/**
 * Provides support for creating VisAD Data objects that use a file 
 * backing-store from VirtualData objects.  Currently, the only supported
 * in-file VisAD data object is the FileFlatField.
 *
 * @author Steven R. Emmerson
 */
public class
FileDataFactory
    extends	DataFactory
{
    private static FileDataFactory	instance;

    private static CacheStrategy	cacheStrategy = new CacheStrategy();

    static
    {
	instance = new FileDataFactory();
    }


    private FileDataFactory()
    {}


    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static DataFactory instance()
    {
	return instance;
    }


    /**
     * Creates a VisAD FlatField object from a netCDF indicial context and a 
     * VirtualFlatField.  The returned FlatField object is, actually, a
     * FileFlatField that uses the netCDF dataset as its (read-only)
     * backing-store.
     *
     * @param context		The netCDF indicial context.
     * @param virtualField	The virtual data.
     * @return			The VisAD FileFlatField corresponding to the
     *				input.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public FlatField newData(Context context, VirtualFlatField virtualField)
	throws VisADException, RemoteException, IOException
    {
	return
	    new FileFlatField(
		new netCDFFlatFieldAccessor(context, virtualField),
		cacheStrategy);
    }


    /**
     * Provides support for reading a FlatField from a netCDF dataset.
     *
     * @author Steven R. Emmerson
     */
    protected class
    netCDFFlatFieldAccessor
	extends FileAccessor
    {
	private Context		context;
	private VirtualField	virtualField;


	/**
	 * Constructs from a netCDF indicial context and a virtual Field.
	 *
	 * @param context	The netCDF indicial context.
	 * @param virtualField	The virtual Field.
	 */
	public netCDFFlatFieldAccessor(
	    Context context, VirtualField virtualField)
	{
	    this.context = (Context)context.clone();
	    this.virtualField = virtualField;
	}


	/**
	 * Returns the associated FlatField.
	 *
	 * @return			The associated FlatField.
	 * @throws VisADException	VisAD failure.
	 * @throws RemoteException	Java RMI failure.
	 */
	public FlatField getFlatField()
	    throws VisADException, RemoteException
	{
	    try
	    {
		return (FlatField)
		    DataFactory.instance().newData(context, virtualField);
	    }
	    catch (RemoteException e)
	    {
		throw e;
	    }
	    catch (IOException e)
	    {
		throw new RemoteException(e.getMessage());
	    }
	}


	/**
	 * Returns the VisAD FunctionType of the FlatField.
	 *
	 * @return			The VisAD FunctionType of the FlatField.
	 */
	public FunctionType getFunctionType()
	{
	    return virtualField.getFunctionType();
	}


	/**
	 * Returns <code>null</code>.
	 *
	 * @return			<code>null</code>.
	 */
	public double[][] readFlatField(FlatField template, int[] fileLocation)
	{
	    return null;
	}


	/**
	 * Does nothing.
	 */
	public void writeFile(int[] fileLocation, Data range)
	{}


	/**
	 * Does nothing.
	 */
	public void writeFlatField(
	    double[][] values, FlatField template, int[] fileLocation)
	{}
    }
}
