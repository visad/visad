/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.netcdf;
import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * NamedDictionary is a collection of Named things.
 * This is used in the implementation of the other netcdf collections.
 * Note: no public methods, all are package scope or more private.
 * <p>
 * It turns out that people think that the order of these things is
 * important. So, we implement as a Vector, with an aux Hashtable for
 * lookup by name.
 * <p>
 * For serialization, we only put the Vector part over the wire.
 * The Hashtable is reconstructed at the other end.
 * In fact, we encode the vector as an array for transport, saving
 * a few more bytes.
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.3 $
 */

class
NamedDictionary
		implements Serializable
{

	private void
	init(Named [] na)
	{
		if(na == null) {
			vector = new Vector(0);
		}
		else synchronized (na) {
			vector = new Vector(na.length);
			for(int ii = 0; ii < na.length; ii++) {
				vector.addElement(na[ii]);
			}
		}
		// TODO: Dont use Hashtable unless size is worth it?
		int size = vector.size();
		if(size < 3)
			size = 3;
		size *= 4;
		size /= 3;
		table = new Hashtable(size, .75f + Float.MIN_VALUE);
		Enumeration ee = vector.elements();
		while ( ee.hasMoreElements() ) {
			final Named value = (Named) ee.nextElement();
			table.put(value.getName(), value);
		}
	}

	NamedDictionary(Named [] na)
	{
		init(na);
	}

	NamedDictionary(int size, Enumeration ee)
	{
		vector = new Vector(size);
		table = new Hashtable(((size < 3 ? 3 : size) * 4)/3,
			 .75f + Float.MIN_VALUE);
		while ( ee.hasMoreElements() ) {
			final Named value = (Named) ee.nextElement();
			this.put(value);
		}
	}

	/**
	 */
	NamedDictionary(int size)
	{
		vector = new Vector(size);
		table = new Hashtable(((size < 3 ? 3 : size) * 4)/3,
			 .75f + Float.MIN_VALUE);
	}

	/**
	 * Returns the number of elements contained within the Dictionary. 
	 */
	int
	size()
		{ return vector.size(); }

	/**
	 * Returns an enumeration of the elements. Use the Enumeration methods 
	 * on the returned object to fetch the elements sequentially.
	 * @see java.util.Enumeration
	 */
	Enumeration
	elements()
		{ return vector.elements(); }

	/**
	 * Gets the object associated with the specified name.
	 * @param name the name of the dimension
	 * @return the dimension, or null if not found
	 * @see NamedDictionary#put
	 */
	Named
	get(String name)
		{ return (Named) table.get(name); }

	/**
	 * Puts the specified element into the Dictionary, using the its name as
	 * key.  The element may be retrieved by doing a get() with the same 
	 * name.  The element cannot be null.
	 * @param value the specified element 
	 * @return the old value of the key, or null if it did not have one.
	 * @exception NullPointerException If the value of the specified
	 * element is null.
	 * @see NamedDictionary#get
	 */
	synchronized Named
	put(Named value)
	{
		vector.addElement(value);
		return (Named) table.put(value.getName(), value);
	}

	/**
	 * Removes the element corresponding to the key. Does nothing if the
	 * key is not present.
	 * @param name the name of the Named that needs to be removed
	 * @return the Named, or null if no match.
	 *
	 */
	synchronized Named
	remove(String name)
	{
		vector.removeElement(get(name));
		return (Named) table.remove(name);
	}

	/**
	 * Searches for the specified object, starting from the first position
	 * and returns an index to it. Only used by DimensionDictionary.
	 * @param elem the desired element
	 * @return the index of the element, or -1 if it was not found.
	 */
	int
	indexOf(Named elem)
		{ return vector.indexOf(elem); }

	/**
	 * Tests if the Named identified by <code>name</code>
	 * is in this set.
	 * @param name String which identifies the desired object
	 * @return <code>true</code> if and only if this set contains
	 * the Named
	 */
	boolean
	contains(String name)
		{ return table.containsKey(name); }

	/**
	 * Tests if the argument is in this set.
	 * @param oo some Object
	 * @return <code>true</code> if and only if this set contains
	 * <code>oo</code>
	 * TODO? typecheck?
	 */
	boolean
	contains(Object oo)
		{ return table.contains(oo); }

	private void
	writeObject(ObjectOutputStream out)
     		throws IOException
	{
		final int sz = size();
		final Named [] na =  new Named[sz];
		int ii = 0;
		Enumeration ee = vector.elements();
		while ( ee.hasMoreElements() ) {
			na[ii++] = (Named) ee.nextElement();
		}
		out.writeObject(na);
	}

	private void
	readObject(ObjectInputStream in)
     		throws IOException, ClassNotFoundException
	{
		Named [] na = (Named []) in.readObject();
		init(na);
	}

	private Vector vector; // encoded over the wire as an array.
	private transient Hashtable table;
}
