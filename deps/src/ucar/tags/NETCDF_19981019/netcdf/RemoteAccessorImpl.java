/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import ucar.multiarray.Accessor;
import ucar.multiarray.RemoteAccessor;
import java.io.IOException;
import ucar.multiarray.MultiArray;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.UnicastRemoteObject;


/**
 * RemoteAccessorImpl is a UnicastRemoteObject (RMI service)
 * which implements ucar.multiarray.RemoteAccessor using the proxy
 * pattern. Accessor methods are forwarded to the adaptee and
 * adaptee exceptions are wrapped in java.rmi.ServerException.
 * 
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:44:21 $
 */

public class
RemoteAccessorImpl
	extends UnicastRemoteObject
	implements RemoteAccessor
{
        /**
         * Construct a UnicastRemoteObject which acts as
	 * an Accessor proxy.
	 * @param adaptee Accessor to which the Accessor
	 * methods of this are forwarded.
	 *
	 */
	public
	RemoteAccessorImpl(Accessor adaptee)
			throws RemoteException
	{
		this.adaptee = adaptee;
	}

	public Object
	get(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.get(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public boolean
	getBoolean(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getBoolean(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public char
	getChar(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getChar(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public byte
	getByte(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getByte(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public short
	getShort(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getShort(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public int
	getInt(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getInt(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public long
	getLong(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getLong(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public float
	getFloat(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getFloat(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}

	public double
	getDouble(int [] index)
			throws RemoteException
	{
		try {
			return adaptee.getDouble(index);
		}
		catch (IOException ioe)
		{
			throw new ServerException("get", ioe);
		}
	}


	public void
	set(int [] index, Object value)
			throws RemoteException
	{
		try {
			adaptee.set(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setBoolean(int [] index, boolean value)
			throws RemoteException
	{
		try {
			adaptee.setBoolean(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setChar(int [] index, char value)
			throws RemoteException
	{
		try {
			adaptee.setChar(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setByte(int [] index, byte value)
			throws RemoteException
	{
		try {
			adaptee.setByte(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setShort(int [] index, short value)
			throws RemoteException
	{
		try {
			adaptee.setShort(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setInt(int [] index, int value)
			throws RemoteException
	{
		try {
			adaptee.setInt(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setLong(int [] index, long value)
			throws RemoteException
	{
		try {
			adaptee.setLong(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setFloat(int [] index, float value)
			throws RemoteException
	{
		try {
			adaptee.setFloat(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public void
	setDouble(int [] index, double value)
			throws RemoteException
	{
		try {
			adaptee.setDouble(index, value);
		}
		catch (IOException ioe)
		{
			throw new ServerException("set", ioe);
		}
	}

	public MultiArray
	copyout(int [] origin, int [] shape)
			throws RemoteException
	{
		try {
			return adaptee.copyout(origin, shape);
		}
		catch (IOException ioe)
		{
			throw new ServerException("copyout", ioe);
		}
	}

	public void
	copyin(int [] origin, MultiArray source)
			throws RemoteException
	{
		try {
			adaptee.copyin(origin, source);
		}
		catch (IOException ioe)
		{
			throw new ServerException("copyin", ioe);
		}
	}

	public Object
	toArray()
			throws RemoteException
	{
		try {
			return adaptee.toArray();
		}
		catch (IOException ioe)
		{
			throw new ServerException("toArray", ioe);
		}
	}

	public Object
	toArray(Object dst, int [] origin, int [] shape)
			throws RemoteException
	{
		// TODO: Avoid sending big dst over wire
		try {
			return adaptee.toArray(dst, origin, shape);
		}
		catch (IOException ioe)
		{
			throw new ServerException("toArray", ioe);
		}
	}

	private final Accessor adaptee;
}
