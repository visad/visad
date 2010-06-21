// $Id: RemoteAccessor.java,v 1.2 2002-05-29 20:32:40 steve Exp $
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
package ucar.multiarray;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * This interface is the same as Accessor, just
 * tagged as java.rmi.Remote. This intervening
 * layer is necessary so that the rmi compiler
 * interpretes concrete multiarrays like MultiArrayImpl
 * as return by value. Otherwise, if Accessor itself
 * were remote, rmic would generate stubs for MultiArrayImpl
 * to be a remote reference.
 * <p>
 * As of this writing (jdk1.1),
 * the rmi compiler <code>rmic</code> is braindead in the
 * sense that it doesn't recognize that java.rmi.RemoteException isa
 * java.io.IOException. Hence, we reproduce each method declaration
 * from Accessor, narrowing the throws specification.
 * 
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:40 $
 */

public interface
RemoteAccessor
	extends Accessor, Remote
{
	public Object
	get(int [] index)
		throws RemoteException;

	public boolean
	getBoolean(int [] index)
		throws RemoteException;

	public char
	getChar(int [] index)
		throws RemoteException;

	public byte
	getByte(int [] index)
		throws RemoteException;

	public short
	getShort(int [] index)
		throws RemoteException;

	public int
	getInt(int [] index)
		throws RemoteException;

	public long
	getLong(int [] index)
		throws RemoteException;

	public float
	getFloat(int [] index)
		throws RemoteException;

	public double
	getDouble(int [] index)
		throws RemoteException;

	public void
	set(int [] index, Object value)
		throws RemoteException;

	public void
	setBoolean(int [] index, boolean value)
		throws RemoteException;

	public void
	setChar(int [] index, char value)
		throws RemoteException;

	public void
	setByte(int [] index, byte value)
		throws RemoteException;

	public void
	setShort(int [] index, short value)
		throws RemoteException;

	public void
	setInt(int [] index, int value)
		throws RemoteException;

	public void
	setLong(int [] index, long value)
		throws RemoteException;

	public void
	setFloat(int [] index, float value)
		throws RemoteException;

	public void
	setDouble(int [] index, double value)
		throws RemoteException;

	public MultiArray
	copyout(int [] origin, int [] shape)
			throws RemoteException;

	public void
	copyin(int [] origin, MultiArray source)
		throws RemoteException;

	public Object
	toArray()
		throws RemoteException;
	

	public Object
	toArray(Object anArray, int [] origin, int [] shape)
		throws RemoteException;

}
