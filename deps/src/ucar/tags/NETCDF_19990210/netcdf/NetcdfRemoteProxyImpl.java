/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import ucar.multiarray.RemoteAccessor;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;

/**
 * This class provides implementation of the interface
 * NetcdfRemoteProxy. It wraps a single instance of Netcdf
 * provide Remote services required in the construction
 * of an instance of RemoteNetcdf.
 *
 * @see NetcdfRemoteProxy
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.4 $ $Date: 2000-08-28 21:45:46 $
 */

public class
NetcdfRemoteProxyImpl
		extends RemoteObject
		implements NetcdfRemoteProxy, Unreferenced {

        /**
         * Construct a RemoteObject which acts as
	 * a NetcdfRemoteProxy for a single Netcdf.
	 * @param svr NetcdfServer which owns this.
	 * @param key String by which svr knows us.
	 * @param nc Netcdf  which this will represent.
	 *
	 */
	public
	NetcdfRemoteProxyImpl(NetcdfServer svr, String key, AbstractNetcdf nc)
		throws RemoteException
	{
		super();
		svr_ = svr;
		key_ = key;
		nc_ = nc;
	}

/* Begin NetcdfRemoteProxy */	

	public Schema
	getSchema()
		throws RemoteException
	{
		return nc_.getSchema();
	}

	public RemoteAccessor
	getAccessor(String varName)
		throws RemoteException
	{
		if(svr_ != null)
			return (RemoteAccessor) svr_.exportObject(
				new RemoteAccessorImpl(this, nc_.get(varName)));
		// else
		return (RemoteAccessor) UnicastRemoteObject.exportObject(
				new RemoteAccessorImpl(this, nc_.get(varName)));
	}

	public void
	release()
		throws RemoteException
	{
		_release();
	}

/* End NetcdfRemoteProxy */	
/* Begin java.rmi.server.Unreferenced */	

	/**
	 * Equivalent to release(), called automatically by
	 * the runtime system.
	 * @see java.rmi.server.Unreferenced#unreferenced
	 * @see NetcdfRemoteProxy#release
	 */
	public void
	unreferenced()
	{
		if(svr_ != null && svr_.logger_ != null)
			svr_.logger_.logDebug(this + ".unreferenced()");
		_release();
	}
/* End java.rmi.server.Unreferenced */	

	protected void
	finalize()
		throws Throwable
	{ 
		if(svr_ != null && svr_.logger_ != null)
			svr_.logger_.logDebug(this + ".finalize()");
		super.finalize();
		_release();
	}

	protected void
	_release()
	{
		if(nc_ != null)
		{
			svr_._release(key_);
			nc_ = null;
		}
	}

	/**
	 * @serial
	 */
	private final NetcdfServer svr_;
	/**
	 * @serial
	 */
	private final String key_;
	/**
	 * @serial
	 */
	private /* final */ AbstractNetcdf nc_;
}
