/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import ucar.multiarray.RemoteAccessor;
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
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:44:20 $
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
	NetcdfRemoteProxyImpl(AbstractNetcdf nc)
		throws RemoteException
	{
		super();
		this.nc = nc;
	}
	
	public Schema
	getSchema()
		throws RemoteException
	{
		return nc.getSchema();
	}

	public RemoteAccessor
	getAccessor(String varName)
		throws RemoteException
	{
		return new RemoteAccessorImpl(nc.get(varName));
	}

	private final AbstractNetcdf nc;
}
