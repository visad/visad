/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import ucar.multiarray.RemoteAccessor;
import java.rmi.RemoteException;
import java.rmi.Remote;

/**
 * This interface wraps a single instance of Netcdf to
 * provide Remote services required in the construction
 * of an instance of RemoteNetcdf.
 * <p>
 * This interface is only needed by directory services like NetcdfService
 * to bootstrap instances of RemoteNetcdf.
 * It could be considered package or implementation private.
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:44:20 $
 */

public interface
NetcdfRemoteProxy
		extends Remote
{
	/**
	 * @return a Schema for the Netcdf this
	 * represents.
	 */
	public Schema
	getSchema()
		throws RemoteException;

	/**
	 * Get an Accessor for a Variable, by name.
	 * Given the Accessor and the ProtoVariable
	 * obtained indirectly from getSchema() above,
	 * RemoteNetcdf can create a remote proxy for the Variable.
	 * @param varName String which names a Variable in the
	 * Netcdf this represents.
	 * @return a (Remote)Accessor for the Variable.
	 */
	public RemoteAccessor
	getAccessor(String varName)
		throws  RemoteException;
}
