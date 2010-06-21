// $Id: ProtoVariable.java,v 1.4 2002-05-29 18:31:35 steve Exp $
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
import ucar.multiarray.MultiArrayInfo;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.io.ObjectInputStream;
import java.io.InvalidClassException;

/**
 * Prototype for Netcdf Variable.
 * Instances of ProtoVariable provide the description of a Netcdf Variable
 * without data i/o functionality. Instances of this class are used in a Schema,
 * which is used when we create a new Netcdf. An instance has a name and a shape
 * specified by an array of Dimensions.
 * It may also have descriptive attributes. The attribute set is 
 * modifiable.
 * <p>
 * The data logically contained in a Netcdf Variable is not accessed
 * through this object.
 * <p>
 * Although there is no explicit relationship between this class and
 * and Variable, they share common method signatures and semantics where
 * appropriate.
 *
 * @see Variable
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:35 $
 */


public class
ProtoVariable
	implements Named, MultiArrayInfo, Serializable, Cloneable
{

	/**
	 * Check if the given Class corresponds to a netcdf type.
	 * This method is used to check Attributes as well...
	 *
	 * @param componentType Class to check
	 * @return <code>true</code> if Okay, <code>false</code> otherwise.
	 */
	static final boolean
	checkComponentType(Class componentType)
	{
		if(!componentType.isPrimitive()
				|| componentType.equals(Long.TYPE)
				|| componentType.equals(Boolean.TYPE))
			return false;
		return true;
	}
	
 /* Begin Constructors */

	/**
	 * The usual constructor, used when you are going to
	 * add the attributes after construction.
	 *
	 * @param name  String which is to be the name of this Variable
	 * @param componentType  Class (primitive type) contained herein.
	 * 	One of
	 *     	<code>Character.Type</code>,
	 *		<code>Byte.TYPE</code>,
	 *		<code>Short.TYPE</code>,
	 *		<code>Integer.TYPE</code>,
	 *		<code>Float.TYPE</code>,
	 *	or
	 * 		<code>Double.Type</code>.
	 * @param dimArray  The dimensions which define the
	 *	shape of this Variable.	If null or zero length array,
	 *	this is a scalar variable.
	 */
	public
	ProtoVariable(String name, Class componentType, Dimension [] dimArray)
	{
		this.name = name;
		if(!checkComponentType(componentType))
			throw new IllegalArgumentException("Invalid Type");
		this.componentType = componentType;
		if(dimArray == null) {
			this.dimArray = new Dimension[0];
		}
		else synchronized (dimArray) {
			this.dimArray = new Dimension[dimArray.length];
			for(int ii = 0; ii < dimArray.length; ii++) {
				final Dimension dim = dimArray[ii];
				if(dim instanceof UnlimitedDimension) {
					if(ii > 0)
	throw new IllegalArgumentException(
		"UnlimitedDimension not is leftmost position");
				} else if (dim.getLength() == 0) {
	throw new IllegalArgumentException(
		"Zero length dimension"); 
				}
				this.dimArray[ii] = dim;
			}
		}
		this.attributes = new AttributeDictionary();
	}

	/**
	 * Convenience constructor for 1-dimensional Variables, often
	 * used for coordinate variables. Typically attributes would
	 * be added after construction when this constructor is used.
	 *
	 * @param name  String which is to be the name of this Variable
	 * @param componentType  Class (primitive type) contained herein.
	 * 	One of
	 *     	<code>Character.Type</code>,
	 *		<code>Byte.TYPE</code>,
	 *		<code>Short.TYPE</code>,
	 *		<code>Integer.TYPE</code>,
	 *		<code>Float.TYPE</code>,
	 *	or
	 * 		<code>Double.Type</code>.
	 * @param dimension  A single dimension to define the array.
	 */
	public
	ProtoVariable(String name, Class componentType,
			 Dimension dimension)
	{
		this.name = name;
		if(!checkComponentType(componentType))
			throw new IllegalArgumentException("Invalid Type");
		this.componentType = componentType;
		this.dimArray = new Dimension[1];
		this.dimArray[0] = dimension;
		this.attributes = new AttributeDictionary();
	}


	/**
	 * More general constructor. Initializes attribute set
	 * during construction.
	 *
	 * @param name  String which is to be the name of this Variable
	 * @param componentType  Class (primitive type) contained herein.
	 * 	One of
	 *     	<code>Character.Type</code>,
	 *		<code>Byte.TYPE</code>,
	 *		<code>Short.TYPE</code>,
	 *		<code>Integer.TYPE</code>,
	 *		<code>Float.TYPE</code>,
	 *	or
	 * 		<code>Double.Type</code>.
	 * @param dimArray  The dimensions which define the shape
	 *	of this Variable. If null or zero length array,
	 *	this is a scalar variable.
	 * @param attrArray  Attributes associated with this Variable.
	 *	May be null or a zero length array.
	 */
	public
	ProtoVariable(String name, Class componentType,
			Dimension [] dimArray, Attribute [] attrArray)
	{
		this.name = name;
		if(!checkComponentType(componentType))
			throw new IllegalArgumentException("Invalid Type");
		this.componentType = componentType;
		if(dimArray == null) {
			this.dimArray = new Dimension[0];
		}
		else synchronized (dimArray) {
			this.dimArray = new Dimension[dimArray.length];
			for(int ii = 0; ii < dimArray.length; ii++) {
				final Dimension dim = dimArray[ii];
				if(dim instanceof UnlimitedDimension) {
					if(ii > 0)
	throw new IllegalArgumentException(
		"UnlimitedDimension not is leftmost position");
				} else if (dim.getLength() == 0){
	throw new IllegalArgumentException(
		"Zero length dimension"); 
				}
				this.dimArray[ii] = dim;
			}
		}
		this.attributes = new AttributeDictionary(attrArray);
	}

	/**
	 * copy constructor.
	 */ 
	ProtoVariable(ProtoVariable pv)
	{
		name = pv.getName();
		componentType = pv.getComponentType();
		pv.copyVolatile(this);
	}

	/**
	 * Conversion constructor.
	 */
	public
	ProtoVariable(Variable var)
	{
		/*
		 * Why ask why? Would prefer to say:
		 * this(var.meta); 
		 *  ==> Blank final xxx may not have been initialized.
		 */
		name = var.meta.getName();
		componentType = var.meta.getComponentType();
		var.meta.copyVolatile(this);
	}

 /* End Constructors */

	/**
	 * Factor common code used
	 * to implement copy constructor and clone() method.
	 * Copy modifiable portions of this to dest.
	 */
	private synchronized void
	copyVolatile(ProtoVariable dest)
	{
		dest.dimArray = new Dimension[dimArray.length];
		for(int ii = 0; ii < dimArray.length; ii++)
		{
			dest.dimArray[ii] = (Dimension) dimArray[ii].clone();
		}
		dest.attributes = new AttributeDictionary(attributes);
	}

	/**
	 * Returns a clone of this
	 */
	public Object
	clone()
	{
		try {
			final ProtoVariable pv = (ProtoVariable) super.clone();
			copyVolatile(pv);
			return pv;
		}
		catch (CloneNotSupportedException e)
		{
			// this shouldn't happen, since we are Cloneable
			throw new Error();
		}
	}

	/**
	 * Returns the name of this Variable.
	 * @return String which identifies this Variable.
	 */
	public final
	String getName()
	{
		return name;
	}

	/**
	 * Returns the Class object representing the component
	 * type of the Variable.
	 * @return Class The componentType
	 * @see java.lang.Class#getComponentType
	 */
	public final Class
	getComponentType()
	{
		return componentType;
	}

	/**
	 * Returns the number of dimensions of the variable.
	 * @return int number of dimensions of the variable
	 */
	public final int
	getRank()
	{
		return dimArray.length;
	}

	/**
	 * Return an array whose length is the rank of this
	 * and whose elements represent the
	 * length of each of its dimensions.
	 *
	 * @return int array whose length is the rank of this
	 * and whose elements represent the
	 * length of each of its dimensions
	 */
	public final int []
	getLengths()
	{
		int [] lengths = new int[dimArray.length];
		for(int ii = 0; ii < dimArray.length; ii++)
				lengths[ii] = dimArray[ii].getLength();
		
		return lengths;
	}

	/**
	 * Returns <code>true</code> if and only if the this variable can grow.
	 * This is equivalent to saying
	 * at least one of its dimensions is unlimited.
	 * In the current implementation, exactly one dimension, the most
	 * slowly varying (leftmost), can be unlimited.
	 * @return boolean <code>true</code> iff this can grow
	 */
	public final boolean
	isUnlimited()
	{
		return ((dimArray.length > 0)
			 && dimArray[0] instanceof UnlimitedDimension);
	}

	/**
	 * Convenience interface; return <code>true</code>
	 * if and only if the rank is zero.
	 * @return boolean <code>true</code> iff rank == 0
	 */
	public final boolean
	isScalar()
	{
		return (dimArray.length == 0);
	}

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
	{
		return new DimensionIterator() {
			int position = 0;
			
			public boolean hasNext() {
				return position < dimArray.length;
			}

			public Dimension next() {
				return dimArray[position++];
			}

		};
	}

	/**
	 * Convenience function; look up Attribute by name.
	 *
	 * @param name the name of the attribute
	 * @return the attribute, or null if not found
	 */
	public Attribute
	getAttribute(String name)
	{
		return attributes.get(name);
	}

	/**
	 * Returns the (modifiable) set of attributes
	 * associated with this. 
	 * 
	 * @return AttributeSet. May be empty. Won't be null.
	 */
	public AttributeSet
	getAttributes()
	{
		return (AttributeSet) attributes;
	}

	/**
	 * Convenience function; add attribute.
	 * @see AttributeSet#put
	 * @param attr the Attribute to be added to this set.
	 * @return Attribute replaced or null if not a replacement
	 */
	public Attribute
	putAttribute(Attribute attr)
	{
		return attributes.put(attr);
	}

	/**
	 * Format as CDL.
	 * @param buf StringBuffer into which to write
	 */
	public void
	toCdl(StringBuffer buf)
	{
		buf.append(this.getComponentType());
		buf.append(" ");
		buf.append(this.getName());
		buf.append("(");
		for(DimensionIterator iter = this.getDimensionIterator();
				iter.hasNext() ;) {
			buf.append( iter.next().getName() );
			if(!iter.hasNext())
				break;
			buf.append(", ");
		}
		buf.append(") ;\n");
		
		/*
		 * We need to tag each attribute with this variable's name.
		 */
		for (AttributeIterator iter = this.getAttributes().iterator();
				iter.hasNext() ;) {
			buf.append("\t\t");
			buf.append(this.getName());
			iter.next().toCdl(buf);
			buf.append("\n");
		}
	}

	/**
	 * @return a string representation of this
	 */
	public String
	toString()
	{
		StringBuffer buf = new StringBuffer();
		toCdl(buf);
		return buf.toString();
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
	synchronized void
	connectDims(DimensionDictionary dimensions)
	{
		for(int ii = 0; ii < dimArray.length; ii++)
		{
			dimArray[ii] = dimensions.put(dimArray[ii]);
		}
	}


	/**
	 * Because members of Class Class can't be serialized, we have
	 * to come up with our own encoding of the Class field 'componentType'.
	 * <p>
	 * The intent of this function is to use the same encoding as
	 * 'prim_typecode' from the object serialization stream spec.
	 *
	 */
	private int
	encodeComponentType() 
	{
		if(componentType.isPrimitive())
		{
			if(componentType.equals(Byte.TYPE))
				return (byte) 'B';
			if(componentType.equals(Character.TYPE))
				return (byte) 'C';
			if(componentType.equals(Double.TYPE))
				return (byte) 'D';
			if(componentType.equals(Float.TYPE))
				return (byte) 'F';
			if(componentType.equals(Integer.TYPE))
				return (byte) 'I';
			if(componentType.equals(Long.TYPE))
				return (byte) 'J';
			if(componentType.equals(Short.TYPE))
				return (byte) 'S';
			if(componentType.equals(Boolean.TYPE))
				return (byte) 'Z';
		}
		throw new IllegalArgumentException(componentType.toString());
	}

	static private Class
	decodeComponentType(int typecode)
			throws InvalidClassException
	{
		switch (typecode) {
		case (byte) 'B':
			return Byte.TYPE;
		case (byte) 'C':
			return Character.TYPE;
		case (byte) 'D':
			return Double.TYPE;
		case (byte) 'F':
			return Float.TYPE;
		case (byte) 'I':
			return Integer.TYPE;
		case (byte) 'J':
			return Long.TYPE;
		case (byte) 'S':
			return Short.TYPE;
		case (byte) 'Z':
			return Boolean.TYPE;
		}
		throw new InvalidClassException(Integer.toHexString(typecode));
	}

	private void
	writeObject(ObjectOutputStream out)
     		throws IOException
	{
		out.writeObject(name);
		out.write(encodeComponentType());
		out.writeObject(dimArray);
		out.writeObject(attributes);
	}

	private void
	readObject(ObjectInputStream in)
     		throws IOException, ClassNotFoundException
	{
		name = (String) in.readObject();
		final int typecode = in.read();
		componentType = decodeComponentType(typecode);
		dimArray = (Dimension []) in.readObject();
		attributes = (AttributeDictionary) in.readObject();
	}

	/**
	 * @serial
	 */
	private /* final */ String name;
	/**
	 * @serial
	 */
	private /* final */ Class componentType;
	/**
	 * @serial
	 */
	private /* final */ Dimension [] dimArray; 
	/**
	 * @serial
	 */
	private /* final */ AttributeDictionary attributes; 
}
