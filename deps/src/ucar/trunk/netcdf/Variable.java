// $Id: Variable.java,v 1.6 2003-03-14 16:29:05 donm Exp $
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

/**
 * Variable is a (potentially large) multi-dimensional
 * array of primitives. The dimensions are named, allowing
 * relationships between Variables to be expressed. Variables have names and
 * may have descriptive attributes.
 * <p>
 * Objects which implement this interface exist in the context of a
 * particular Netcdf data set. If you factor out the data access
 * methods of this interface, leaving the descriptive "meta" information,
 * what remains is a ProtoVariable.
 * <p>
 * Although there is no explicit relationship between this interface and
 * class ProtoVariable, they share common method signatures and
 * semantics where appropriate.
 *
 * @see ProtoVariable
 * @see MultiArray
 * @author $Author: donm $
 * @version $Revision: 1.6 $ $Date: 2003-03-14 16:29:05 $
 */

public class
Variable
	implements MultiArray, Named
{
	
 /* Begin Constructors */

	/**
	 * The usual constructor. Think of it as package private or
	 * protected, let AbstactNetcdf call it for you.
	 *
	 * @param proto   the ProtoVariable used as metadata storage.
	 *   Shouldn't be null.
         *   It should be immutable over the lifetime of this object.
         *   (If in doubt, hand this its own private copy.)
	 */
	public
	Variable(ProtoVariable proto, Accessor io)
	{
		if(proto == null || io == null)
			throw new NullPointerException();
		meta = proto;
		this.io = io;
	}

 /* End Constructors */
 /* Begin MultiArrayInfo */

	/**
	 * Returns the Class object representing the component
	 * type of the array.
	 * @see ucar.multiarray.MultiArray#getComponentType
	 * @return Class of the component type
	 */
	public Class
	getComponentType()
		{ return meta.getComponentType(); }

	/**
	 * Returns the number of dimensions.
	 * @see ucar.multiarray.MultiArray#getRank
	 * @return int number of dimensions 
	 */
	public int
	getRank()
		{ return meta.getRank(); }

	/**
	 * @see ucar.multiarray.MultiArray#getLengths
	 * @return int array whose length is the rank of this
	 * and whose elements represent the
	 * length of each of its dimensions
	 */
	public int []
	getLengths()
		{ return meta.getLengths(); }

	/**
	 * Returns <code>true</code> if and only if the this variable can grow.
	 * This is equivalent to saying
	 * at least one of its dimensions is unlimited.
	 * In the current implementation, exactly one dimension, the most
	 * slowly varying (leftmost), can be unlimited.
	 * @return boolean <code>true</code> iff this can grow
	 */
	public boolean
	isUnlimited()
		{ return meta.isUnlimited(); }

	/**
	 * Convenience interface; return <code>true</code>
	 * if and only if the rank is zero.
	 * @return boolean <code>true</code> iff rank == 0
	 */
	public boolean
	isScalar()
		{ return meta.isScalar(); }

 /* End MultiArrayInfo */
 /* Begin Variable Introspection */

	/**
	 * Returns the name of this Variable.
	 * @return String which identifies this Variable.
	 */
	public String
	getName()
		{ return meta.getName(); }

	/**
	 * Returns a DimensionIterator of the dimensions
	 * used by this variable. The most slowly varying (leftmost
	 * for java and C programmers) dimension is first.
	 * For scalar variables, the set has no elements and the iteration
	 * is empty.
	 * @return DimensionIterator of the elements.
	 * @see DimensionIterator
	 */
	public DimensionIterator
	getDimensionIterator() 
		{ return meta.getDimensionIterator(); }

	/**
	 * Returns the set of attributes
	 * associated with this. 
	 * 
	 * @return AttributeSet. May be empty. Won't be null.
	 */
	public AttributeSet
	getAttributes()
		{ return meta.getAttributes(); }

	/**
	 * Convenience function; look up Attribute by name.
	 *
	 * @param name the name of the attribute
	 * @return the attribute, or null if not found
	 */
	public Attribute
	getAttribute(String name)
		{ return meta.getAttribute(name); }

 /* End Variable Introspection */
 /* Begin Accessor read access methods */

	public Object
	get(int [] index)
			throws IOException
		{ return io.get(index); }

	public boolean
	getBoolean(int [] index)
			throws IOException
		{ return io.getBoolean(index); }

	public char
	getChar(int [] index)
			throws IOException
		{ return io.getChar(index); }

	public byte
	getByte(int [] index)
			throws IOException
		{ return io.getByte(index); }

	public short
	getShort(int [] index)
			throws IOException
		{ return io.getShort(index); }

	public int
	getInt(int [] index)
			throws IOException
		{ return io.getInt(index); }

	public long
	getLong(int [] index)
			throws IOException
		{ return io.getLong(index); }

	public float
	getFloat(int [] index)
			throws IOException
		{ return io.getFloat(index); }

	public double
	getDouble(int [] index)
			throws IOException
		{ return io.getDouble(index); }

 /* End Accessor read access methods */
 /* Begin Accessor write access methods */

    	public void
	set(int [] index, Object value)
			throws IOException
		{ io.set(index, value); }

	public void
	setBoolean(int [] index, boolean value)
			throws IOException
		{ io.setBoolean(index, value); }

	public void
	setChar(int [] index, char value)
			throws IOException
		{ io.setChar(index, value); }

	public void
	setByte(int [] index, byte value)
			throws IOException
		{ io.setByte(index, value); }

	public void
	setShort(int [] index, short value)
			throws IOException
		{ io.setShort(index, value); }

	public void
	setInt(int [] index, int value)
			throws IOException
		{ io.setInt(index, value); }

	public void
	setLong(int [] index, long value)
			throws IOException
		{ io.setLong(index, value); }

	public void
	setFloat(int [] index, float value)
			throws IOException
		{ io.setFloat(index, value); }

	public void
	setDouble(int [] index, double value)
			throws IOException
		{ io.setDouble(index, value); }

 /* End Accessor write access methods */
 /* Begin Variable aggregate access */

	public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException
	{
		if(shape.length != getRank())
			throw new IllegalArgumentException("rank mismatch");
		// TODO vet shape elements
//		ucar.unidata.util.LogUtil.call1 ("Variable.copyout", " io=" + io.getClass().getName());
		MultiArray ma = io.copyout(origin, shape);
		//		ucar.unidata.util.LogUtil.call2 ("Variable.copyout");
		return ma;
	}

	public void
	copyin(int [] origin, MultiArray data)
			throws IOException
	{
		if(data.getRank() != getRank())
			throw new IllegalArgumentException(data.getRank()+" != "+getRank());

		if( data.getComponentType() != getComponentType())
			throw new IllegalArgumentException(data.getComponentType().getName()+" != "+getComponentType().getName());

		// TODO vet shape elements
		io.copyin(origin, data);
	}

	public Object
	toArray()
			throws IOException
	{
		return io.toArray();
	}

	public Object
	    getStorage ()
	{
	    try {
		return toArray ();
	    } catch (IOException ioe) {
		System.err.println ("Error:" + ioe);
	    }
	    return null;
	}

	public Object
	toArray(Object dst, int [] origin, int [] shape)
			throws IOException
	{
		return io.toArray(dst, origin, shape);
	}

 /* End Variable aggregate access */

	/**
	  * Format as CDL.
	  * @param buf StringBuffer into which to write
	  */
	public void
	toCdl(StringBuffer buf)
		{ meta.toCdl(buf); }

	/**
	  * @return a CDL string of this.
	  */
	public String
	toString()
	{
		return meta.toString(); // TODO
	}
    
	/**
	 * Ensure that the dimensions referenced by this
	 * are members of the specified Dictionary.
	 * This may modify dimArray elements to reference
	 * different (equivalent) dimension instances.
	 *
	 * package private
	 *
	 */
	void
	connectDims(DimensionDictionary dimensions)
		{ meta.connectDims(dimensions); }
	
 /* implementation */

        final ProtoVariable meta;
        final Accessor io;
}
