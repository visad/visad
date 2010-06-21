// $Id: RemoteNetcdf.java,v 1.4 2002-05-29 18:31:36 steve Exp $
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
import ucar.multiarray.Accessor;
import ucar.multiarray.MultiArray;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.lang.reflect.InvocationTargetException;


/**
 * A concrete implementation of the Netcdf interface,
 * this class uses java rmi to access a remote Netcdf.
 * <p>
 * 
 * @see Netcdf
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:36 $
 */
public class
RemoteNetcdf
	extends AbstractNetcdf
{

	/**
	 * Get remote dataset directory
	 * service from a given host.
	 * Convience function which wraps java.rmi.Naming.lookup().
	 * @param remoteHostName String host name or dotted quad
	 * @return NetcdfService
	 */
	static public NetcdfService
	getNetcdfService(String remoteHostName)
			throws RemoteException,
				java.rmi.NotBoundException,
				java.net.MalformedURLException
	{
		String svcName = "//" + remoteHostName + "/"
			+ NetcdfService.SVC_NAME;
		return (NetcdfService) Naming.lookup(svcName);
	}

	/**
	 * Given a NetcdfRemoteProxy, construct a RemoteNetcdf.
	 * The NetcdfRemoteProxy would be obtained from a directory
	 * service like NetcdfService.
	 */
	public
	RemoteNetcdf(NetcdfRemoteProxy remote)
			throws RemoteException
	{
		super(remote.getSchema(), false);
		this.remote = remote;
		try {
			super.initHashtable();
		}
		catch (InstantiationException ie)
		{
			// Can't happen: Variable is concrete
			throw new Error();
		}
		catch (IllegalAccessException iae)
		{
			// Can't happen: Variable is accessable
			throw new Error();
		}
		catch (InvocationTargetException ite)
		{
			// all the possible target exceptions are
			// RuntimeException
			throw (RuntimeException)
				ite.getTargetException();
		}
	}

	/**
	 * Open up a remote Netcdf by name.
	 * The remote host needs to be running a NetcdfService
	 * which exports the data set.
	 * @param remoteHostName String host name or dotted quad
	 * @param dataSetName String name of the remote Netcdf
	 */
	public
	RemoteNetcdf(String remoteHostName,
		String dataSetName)
			throws RemoteException,
				java.rmi.NotBoundException,
				java.net.MalformedURLException
	{
		this(getNetcdfService(remoteHostName).lookup(dataSetName));
	}

	/**
	 * Indicate that you are done with this remote Netcdf.
	 * Allows the service to free resources.
	 * We name this method close for symmetry with NetcdfFile.
	 * You do not have to call this. RMI runtime will
	 * eventually (~10 minutes?) call NetcdfRemoteProxyImpl.unreferenced()
	 * and accomplish the same thing.
	 * @see NetcdfRemoteProxy#release
	 */
	public void
	close()
		throws RemoteException
	{
		remote.release();
	}

	protected Accessor
	ioFactory(ProtoVariable proto)
			throws InvocationTargetException
	{
		try {
			return remote.getAccessor(proto.getName());
		}
		catch (IOException ee)
		{
			throw new InvocationTargetException(ee);
		}
	}
	
	/**
	 * Ensures that the remote resources associated with this are
	 * released when there are no more references to it. 
	 * @see #close()
	 */
	protected void
	finalize() throws Throwable
	{
		super.finalize();
		close();
	}

	private /* final */ NetcdfRemoteProxy remote;

	public static void
	main(String[] args)
	{
		if(args.length < 1)
		{
			System.out.println("test Usage: RemoteNetcdf nc_name");
			System.exit(1);
		}
		final String name = args[0];
		// else
		try {
			RemoteNetcdf rnc = new RemoteNetcdf("localhost", name);
			System.out.println(rnc);
			VariableIterator vi = rnc.iterator();
			while(vi.hasNext())
			{
				Variable v = vi.next();
				System.out.print(v.getName() + "[0, ...]: ");
				MultiArray ma = v.copyout(new int[v.getRank()],
					v.getLengths());
				System.out.println(ma.get(
					new int[ma.getRank()]));
			}
			rnc.close();
		}
		catch (Exception ee)
		{
			System.out.println(ee);
			System.exit(1);
		}
		System.exit(0);
	}
}
