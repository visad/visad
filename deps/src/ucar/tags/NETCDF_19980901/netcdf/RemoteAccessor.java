/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import ucar.multiarray.Accessor;
import java.io.IOException;
import ucar.multiarray.MultiArray;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * RemoteAccessor is a UnicastRemoteObject (RMI service)
 * which implements ucar.multiarray.Accessor using the proxy
 * pattern. Accessor methods are forwarded to the adaptee.
 * This is case where multiple inheritance would have made
 * more sense.
 * 
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:43:08 $
 */

public class
RemoteAccessor
	extends UnicastRemoteObject
	implements Accessor
{
        /**
         * Construct a UnicastRemoteObject which acts as
	 * an Accessor proxy.
	 * @param adaptee Accessor to which the Accessor
	 * methods of this are forwarded.
	 *
	 */
	public
	RemoteAccessor(Accessor adaptee)
			throws RemoteException
	{
		this.adaptee = adaptee;
	}

	public Object
	get(int [] index)
			throws IOException, RemoteException
		{ return adaptee.get(index); }

	public boolean
	getBoolean(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getBoolean(index); }

	public char
	getChar(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getChar(index); }

	public byte
	getByte(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getByte(index); }

	public short
	getShort(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getShort(index); }

	public int
	getInt(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getInt(index); }

	public long
	getLong(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getLong(index); }

	public float
	getFloat(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getFloat(index); }

	public double
	getDouble(int [] index)
			throws IOException, RemoteException
		{ return adaptee.getDouble(index); }


	public void
	set(int [] index, Object value)
			throws IOException, RemoteException
		{ adaptee.set(index, value); }

	public void
	setBoolean(int [] index, boolean value)
			throws IOException, RemoteException
		{ adaptee.setBoolean(index, value); }

	public void
	setChar(int [] index, char value)
			throws IOException, RemoteException
		{ adaptee.setChar(index, value); }

	public void
	setByte(int [] index, byte value)
			throws IOException, RemoteException
		{ adaptee.setByte(index, value); }

	public void
	setShort(int [] index, short value)
			throws IOException, RemoteException
		{ adaptee.setShort(index, value); }

	public void
	setInt(int [] index, int value)
			throws IOException, RemoteException
		{ adaptee.setInt(index, value); }

	public void
	setLong(int [] index, long value)
			throws IOException, RemoteException
		{ adaptee.setLong(index, value); }

	public void
	setFloat(int [] index, float value)
			throws IOException, RemoteException
		{ adaptee.setFloat(index, value); }

	public void
	setDouble(int [] index, double value)
			throws IOException, RemoteException
		{ adaptee.setDouble(index, value); }

	public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException, RemoteException
		{ return adaptee.copyout(origin, shape); }

	public void
	copyin(int [] origin, MultiArray source)
			throws IOException, RemoteException
		{ adaptee.copyin(origin, source); }

	public Object
	toArray()
			throws IOException
		{ return adaptee.toArray(); }

	public Object
	toArray(Object dst, int [] origin, int [] shape)
			throws IOException
	{
		// TODO: Avoid sending big dst over wire
		return adaptee.toArray(dst, origin, shape);
	}

	private final Accessor adaptee;
}
