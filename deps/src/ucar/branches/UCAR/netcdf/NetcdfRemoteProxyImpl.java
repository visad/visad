/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import ucar.multiarray.Accessor;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class provides implementation of the interface
 * NetcdfRemoteProxy. It wraps a single instance of Netcdf
 * provide Remote services required in the construction
 * of an instance of RemoteNetcdf.
 *
 * @see NetcdfRemoteProxy
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:43:07 $
 */

public class
NetcdfRemoteProxyImpl
	extends UnicastRemoteObject implements NetcdfRemoteProxy {

        /**
         * Construct a UnicastRemoteObject which acts as
	 * a NetcdfRemoteProxy for a single Netcdf.
	 * @param nc Netcdf  which this will represent.
	 *
	 */
	public
	NetcdfRemoteProxyImpl(Netcdf nc)
		throws RemoteException
	{
		super();
		this.nc = nc;
	}
	
	public Schema
	getSchema()
		throws RemoteException
	{
		return new Schema(nc);
	}

	public Accessor
	getAccessor(String varName)
		throws RemoteException
	{
		return new RemoteAccessor(nc.get(varName));
	}

	private final Netcdf nc;
}
