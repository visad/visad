// $Id: NetcdfRemoteProxy.java,v 1.4 2002-05-29 18:31:35 steve Exp $
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
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:35 $
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

	/**
	 * Indicate that you are done with this
	 * Netcdf data set. Allows the service to free
	 * resources (close the data set).
	 */
	public void
	release()
		throws RemoteException;
}
