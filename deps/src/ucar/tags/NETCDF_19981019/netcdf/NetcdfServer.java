/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.rmi.ServerException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Enumeration;

import java.rmi.ConnectException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

/**
 * A UnicastRemoteObject implementation of NetcdfService.
 * @note NetcdfService is a placeholder directory service
 * for a more elaborate directory service,
 * hopefully to be provided later on.
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:44:20 $
 */

public class
NetcdfServer
	extends UnicastRemoteObject implements NetcdfService {

	public
	NetcdfServer(String [] exports, Registry registry)
		throws RemoteException, AlreadyBoundException
	{
		super();
		table = new Hashtable();
		for(int ii = 0; ii < exports.length; ii++)
			export(exports[ii]);
		if(table.size() == 0)
			throw new IllegalArgumentException("No exports");
		if(registry != null)
		{
			this.registry = registry;
			registry.bind(SVC_NAME, this);
                        System.out.println(SVC_NAME
				+ " bound in registry");
		}
	}

	public int
	ping()
		throws RemoteException
	{
		return 0;
	}
	
	public NetcdfRemoteProxy
	lookup(String dataSetName)
		throws RemoteException
	{
		if( !table.containsKey(dataSetName) )
			throw new AccessException(dataSetName +
				" not available");
		try {
			return new NetcdfRemoteProxyImpl(
				get(dataSetName).getNetcdfFile());
		}
		catch (IOException ioe)
		{
			throw new ServerException("lookup", ioe);
		}
	}

	public String []
	list()
		throws RemoteException
	{
		String [] ret = new String [table.size()];
		Enumeration ee = table.keys();
		for(int ii = 0; ee.hasMoreElements(); ii++)
			ret[ii] = (String) ee.nextElement();
		return ret;
	}

	public void
	export(File ff)
	{
		if(!ff.isFile())
			throw new IllegalArgumentException(ff.getPath()
				+ " not a File");
		Entry entry = new Entry(ff);
		String keyval = entry.keyValue();
		System.out.println("Exporting " + ff + " as "
			+ keyval);
		put(keyval, entry);
	}

	public void
	export(String path)
	{
		export(new File(path));
	}

	protected void
	finalize()
	{
		System.out.print("finalize: ");
		if(registry != null)
		{
			try {
				System.out.print("unbind");
				registry.unbind(SVC_NAME);
			} catch (Exception ee) {
				// we tried.
                        	System.out.println( ": " + ee.getMessage());
                        	ee.printStackTrace();
				;
			}
		}
		System.out.println("");
		registry = null;
	}

	public static Registry
	startRegistry()
			throws RemoteException
	{
		System.out.println("No registry, starting one");
		return LocateRegistry.createRegistry(
			Registry.REGISTRY_PORT);
	}

	public static Registry
	checkRegistry(Registry regis, int tryagain)
			throws RemoteException
	{
		if(regis == null)
			regis = startRegistry(); 

		NetcdfService existing = (NetcdfService) null;

                try {
			existing = (NetcdfService) regis.lookup(SVC_NAME);
                }
		catch (ConnectException ce) {
			if(--tryagain > 0)
			{
				return checkRegistry(startRegistry(),
					tryagain);
			}
			throw ce;
		}
		catch (NotBoundException nbe) {
			return regis;	// Normal return
		}
		// else, AlreadyBound. Is it bogus?
		try {
			existing.ping();
		}
		catch (ConnectException ce) { // ?? any RemoteException
			// bogus
			try {
				System.out.println(
					"unbinding dead registry entry");
				regis.unbind(SVC_NAME);
			} catch (NotBoundException nbe) {
				// Race condition.
				// Ignore here and catch it later.
			}
		}
		return regis;
	}

	public static void
	main(String args[])
	{
		System.setSecurityManager(new RMISecurityManager());
		// setLog(System.out);
		
		Registry regis = (Registry) null;
                try {
			regis = checkRegistry(LocateRegistry.getRegistry(), 2);
                }
                catch (Exception ee) {
                        System.out.println(
				"NetcdfServer: error getting registry: "
				 + ee.getMessage());
                        ee.printStackTrace();
			System.exit(1);
                }

                try {
                	NetcdfServer svc = new NetcdfServer(args, regis);
                } catch (Throwable ee) {
                        System.out.println("NetcdfServer err: "
				 + ee.getMessage());
                        ee.printStackTrace();
			System.exit(1);
                }
	}

/**/
	/**
	 * Gets the Entry associated with the specified name.
	 * @param dataSetName the name 
	 * @returns the Entry, or null if not found
	 */
	private Entry
	get(String dataSetName)
		{ return (Entry) table.get(dataSetName); }

	/**
	 * Puts the specified element into the Dictionary, using its
	 * keyValue() as key. 
	 * The element may be retrieved by doing a get() with the key value.
	 * name.  The element cannot be null.
	 * @param entry the new entry;
	 * @exception NullPointerException If the value of the specified
	 * element is null.
	 */
	synchronized private void
	put(String keyval, Entry entry)
	{
		table.put(keyval, entry);
	}

	private Hashtable table;
	private Registry registry;
}

class Entry
{
	final File dirent;
	NetcdfFile nc;
	
	Entry(File ff)
	{
		this.dirent = ff;
		nc = (NetcdfFile) null;
	}

	String
	keyValue()
	{
		// Strip leading path
		final String name = dirent.getName();
		// Strip extension
		final int index = name.indexOf('.');
		return name.substring(0, index).intern();
	}

	NetcdfFile
	open(boolean readonly)
		throws IOException
	{
		if(nc != null)
			throw new IllegalArgumentException("dataSet "
				+ keyValue() + " already open");
		nc = new NetcdfFile(dirent, readonly);
		return nc;
	}

	/*
	 * TODO: How to hook this up?
	 */
	void
	close()
		throws IOException
	{
		nc.close();
		nc = (NetcdfFile) null;
	}

	NetcdfFile
	getNetcdfFile()
		throws IOException
	{
		if(nc == null)
			return open(true); // all access readonly for now
		return nc;
	}
}
