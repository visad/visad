// $Id: RemoteAccessorImpl.java,v 1.4 2002-05-29 18:31:36 steve Exp $
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
import ucar.multiarray.RemoteAccessor;
import java.io.IOException;
import ucar.multiarray.MultiArray;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.RemoteObject;
import java.rmi.server.LogStream;


/**
 * RemoteAccessorImpl is a UnicastRemoteObject (RMI service)
 * which implements ucar.multiarray.RemoteAccessor using the proxy
 * pattern. Accessor methods are forwarded to the adaptee and
 * adaptee exceptions are wrapped in java.rmi.ServerException.
 * 
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:36 $
 */

public class
RemoteAccessorImpl
	extends RemoteObject
	implements RemoteAccessor
{
        /**
         * Construct a UnicastRemoteObject which acts as
	 * an Accessor proxy.
	 * @param svr NetcdfRemoteProxyImpl which owns this.
	 * 	May be null.
	 * @param adaptee Accessor to which the Accessor
	 * methods of this are forwarded.
	 *
	 */
	public
	RemoteAccessorImpl(NetcdfRemoteProxyImpl svr, Accessor adaptee)
			throws RemoteException
	{
		adaptee_ = adaptee;
		svr_ = svr;
	}

	public Object
	get(int [] index)
			throws RemoteException
	{
		try {
			return adaptee_.get(index);
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
			return adaptee_.getBoolean(index);
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
			return adaptee_.getChar(index);
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
			return adaptee_.getByte(index);
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
			return adaptee_.getShort(index);
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
			return adaptee_.getInt(index);
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
			return adaptee_.getLong(index);
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
			return adaptee_.getFloat(index);
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
			return adaptee_.getDouble(index);
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
			adaptee_.set(index, value);
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
			adaptee_.setBoolean(index, value);
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
			adaptee_.setChar(index, value);
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
			adaptee_.setByte(index, value);
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
			adaptee_.setShort(index, value);
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
			adaptee_.setInt(index, value);
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
			adaptee_.setLong(index, value);
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
			adaptee_.setFloat(index, value);
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
			adaptee_.setDouble(index, value);
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
			return adaptee_.copyout(origin, shape);
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
			adaptee_.copyin(origin, source);
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
			return adaptee_.toArray();
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
			return adaptee_.toArray(dst, origin, shape);
		}
		catch (IOException ioe)
		{
			throw new ServerException("toArray", ioe);
		}
	}

	/**
	 * @serial
	 */
	private final Accessor adaptee_;
	/**
	 * @serial
	 */
	private final NetcdfRemoteProxyImpl svr_;
}
