// $Id: NetcdfService.java,v 1.4 2002-05-29 18:31:35 steve Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:35 $
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
		throws RemoteException;

	/**
	 * List the names of exported data sets.
	 */
	public String []
	list()
		throws RemoteException;
}
