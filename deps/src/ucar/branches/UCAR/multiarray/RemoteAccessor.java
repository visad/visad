/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
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
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:44:19 $
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
