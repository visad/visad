/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Remote;

/**
 * This service provides a way to 'open' remote Netcdf data sets
 * by name. It is a placeholder for a more elaborate
 * directory service, hopefully to be provided later on.
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:43:07 $
 */

public interface
NetcdfService
		extends Remote
{
	/**
	 * The string identifing this service in the rmi registry.
	 */
	public static final String SVC_NAME = "NetcdfService";

	/**
	 * Test if the service is alive.
	 * Used by the automatic registration feature of the
	 * NetcdfServer implementation.
	 * @return 0
	 */
	public int
	ping()
		throws RemoteException;

	/**
	 * Connect to (open) a remote Netcdf dataSet by name.
	 * If the name is not the same as one obtainable from
	 * the list() opteration on this service, then this method
	 * will fail.
	 * @param dataSetName String name of the remote Netcdf
	 * @return NetcdfRemoteProxy which can be used to create
	 * an instance of RemoteNetcdf.
	 */
	public NetcdfRemoteProxy
	lookup(String dataSetName)
		throws IOException, RemoteException;

	/**
	 * List the names of exported data sets.
	 */
	public String []
	list()
		throws IOException, RemoteException;
}
