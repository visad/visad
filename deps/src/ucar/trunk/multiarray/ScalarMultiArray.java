// $Id: ScalarMultiArray.java,v 1.3 2003-02-03 20:09:07 donm Exp $
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
import java.lang.reflect.Array;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * MultiArray implementation which can only contain single values,
 * aka scalars.
 * If you have a single object and want to wrap it
 * in a MultiArray interface, use this class.
 * Rank of these is always zero.
 * The index argument to the set, setXXX, get, and getXXX is ignored.
 * <p>
 * When the component type is primitive, this class is an adapter for
 * the appropriate java.lang primitive wrapper (Double, Float, and so on).
 * One of the purposes of this class is to substitute for
 * the wrappers in the MultiArray context, providing continuity of access
 * method signature between arrays and scalars. Contrast this with the the
 * discontinuity between java.util.reflect.Array.getDouble()
 * and java.lang.Number.doubleValue().
 *
 * @see MultiArray
 * @see ArrayMultiArray
 *
 * @author $Author: donm $
 * @version $Revision: 1.3 $ $Date: 2003-02-03 20:09:07 $
 */
public class
ScalarMultiArray
	implements MultiArray
{

	/**
	 * Construct a new ScalarMultiArray with the specified component
	 * type. This form of constructor would typically be used for
	 * primitive component types such as java.lang.Double.TYPE.
	 * @param theComponentType the Class object representing
	 * the component type of the new MultiArray 
	 */
	public
	ScalarMultiArray(Class theComponentType)
	{
		componentType = theComponentType;
	}

	/**
	 * Construct a new ScalarMultiArray with the specified component
	 * type and initialize it to the value given.
	 * This form of constructor would typically be used for
	 * primitive component types such as java.lang.Double.TYPE,
	 * value would be a wrapped primitive of compatible class.
	 * @param theComponentType the Class object representing
	 * the component type of the new MultiArray 
	 * @param value the initial value
	 */
	public
	ScalarMultiArray(Class theComponentType, Object value)
	{
		componentType = theComponentType;
		this.set((int [])null, value);
	}

	/**
	 * Construct a new ScalarMultiArray with component
	 * type value.getClass() and initialize it to the value given.
	 * @param value the initial value
	 */
	public
	ScalarMultiArray(Object value)
	{
		componentType = value.getClass();
		obj = value;
	}

 /* Begin MultiArray Inquiry methods from MultiArrayInfo */

	/**
	 * @see MultiArrayInfo#getComponentType
	 */
	public Class
	getComponentType() {
		return componentType;
	}

	/**
	 * Always returns zero for members of this class.
	 * @see MultiArrayInfo#getRank
	 * @return int 0
	 */
	public int
	getRank() { return 0;}

	/**
	 * Always returns empty array for members of this class.
	 * @see MultiArrayInfo#getLengths
	 * @return int array of length zero.
	 */
	public int []
	getLengths() {
		int [] lengths = new int[0];
		return lengths;
	}

	/**
	 * Always returns <code>false</code> for members of this class.
	 * @see MultiArrayInfo#isUnlimited
	 * @return boolean <code>false</code>
	 */
	public boolean
	isUnlimited() { return false; }

	/**
	 * Alway <code>true</code> for members of this class.
	 * @see MultiArrayInfo#isUnlimited
	 * @return boolean <code>true</code>
	 */
	public boolean
	isScalar() { return true; }

 /* End MultiArrayInfo */
 /* Begin MultiArray Access methods from Accessor */

	/**
	 * Retrieve the object in this container.
	 * @see Accessor#get
	 * @param index ignored
	 * @return the Object contained herein
	 */
	public Object
	get(int [] index)
	{
		return obj;
	}

	/**
	 * As if <code>(((Character)this.get(index)).charValue();</code>	
	 * were called.
	 * @see Accessor#getChar
	 * @see #get
	 * @see java.lang.Character#charValue()
	 */
	public char
	getChar(int[] index)
	{
		return ((Character)obj).charValue();
	}

	/**
	 * As if <code>(((Boolean)this.get(index)).booleanValue();</code>	
	 * were called.
	 * @see Accessor#getBoolean
	 * @see #get
	 * @see java.lang.Boolean#booleanValue()
	 */
	public boolean
	getBoolean(int[] index)
	{
		return ((Boolean)obj).booleanValue();
	}

	/**
	 * As if <code>(((Number)this.get(index)).byteValue();</code>	
	 * were called.
	 * @see Accessor#getByte
	 * @see #get
	 * @see java.lang.Number#byteValue
	 */
	public byte
	getByte(int[] index)
	{
		return ((Number)obj).byteValue();
	}

	/**
	 * As if <code>(((Number)this.get(index)).shortValue();</code>	
	 * were called.
	 * @see Accessor#getShort
	 * @see #get
	 * @see java.lang.Number#shortValue
	 */
	public short
	getShort(int[] index)
	{
		return ((Number)obj).shortValue();
	}

	/**
	 * As if <code>(((Number)this.get(index)).intValue();</code>	
	 * were called.
	 * @see Accessor#getInt
	 * @see #get
	 * @see java.lang.Number#intValue
	 */
	public int
	getInt(int[] index)
	{
		return ((Number)obj).intValue();
	}

	/**
	 * As if <code>(((Number)this.get(index)).longValue();</code>	
	 * were called.
	 * @see Accessor#getLong
	 * @see #get
	 * @see java.lang.Number#longValue
	 */
	public long
	getLong(int[] index)
	{
		return ((Number)obj).longValue();
	}

	/**
	 * As if <code>(((Number)this.get(index)).floatValue();</code>	
	 * were called.
	 * @see Accessor#getFloat
	 * @see #get
	 * @see java.lang.Number#floatValue
	 */
	public float
	getFloat(int[] index)
	{
		return ((Number)obj).floatValue();
	}

	/**
	 * As if <code>(((Number)this.get(index)).doubleValue();</code>	
	 * were called.
	 * @see Accessor#getDouble
	 * @see #get
	 * @see java.lang.Number#doubleValue
	 */
	public double
	getDouble(int[] index)
	{
		return ((Number)obj).doubleValue();
	}

	/* TODO: ArrayStoreException vs IllegalArgumentException */
	/**
	 * Set the object contained here to value.
	 * The index argument is ignored.
	 * @see Accessor#set
	 * @param index ignored
	 * @param value the replacement contents
	 */
	public void
	set(int [] index, Object value)
	{
		final Class xcls = value.getClass();

		if(componentType.isPrimitive())
		{
			try {
				if(componentType ==
					(Class) xcls.getDeclaredField("TYPE").
						get(value))
				{
					// value is a wrapper for componentType
					obj = value;
					return;
				}
			}
			catch (NoSuchFieldException ee)
			{
				// continue
			}
			catch (IllegalAccessException ee)
			{
				// continue
			}
			// Value is not the specific wrapper.
			// Maybe it is assignable...

			Method tValueMethod;
			try {
				tValueMethod = xcls.getMethod(
					componentType.getName()+"Value",
					new Class[0]);
			}
			catch (NoSuchMethodException nsme)
			{
				// Can't convert
				throw new IllegalArgumentException();
			}
			// else
			try {
				obj = tValueMethod.invoke(value, new Object[0]);
				return;
			}
			catch (IllegalAccessException iae)
			{
				// assert(wrapper.xxxValue() is accessable);
				throw new Error();
			}
			catch (InvocationTargetException ite)
			{
				throw (RuntimeException)
					ite.getTargetException();
			}
		}
		// else

		if(!componentType.isAssignableFrom(xcls))
		{
			// Can't convert
			throw new IllegalArgumentException();
		}
		// else
		obj = value;
	}

	/**
	 * Set the object contained here to a boolean value.
	 * The index argument is ignored.
	 * @see Accessor#setBoolean
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setBoolean(int [] index, boolean value)
	{
		this.set(index, new Boolean(value));
	}

	/**
	 * Set the object contained here to a char value.
	 * The index argument is ignored.
	 * @see Accessor#setChar
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setChar(int [] index, char value)
	{
		this.set(index, new Character(value));
	}

	/**
	 * Set the object contained here to a byte value.
	 * The index argument is ignored.
	 * @see Accessor#setByte
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setByte(int [] index, byte value)
	{
		this.set(index, new Byte(value));
	}

	/**
	 * Set the object contained here to a short value.
	 * The index argument is ignored.
	 * @see Accessor#setShort
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setShort(int [] index, short value)
	{
		this.set(index, new Short(value));
	}

	/**
	 * Set the object contained here to a int value.
	 * The index argument is ignored.
	 * @see Accessor#setInt
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setInt(int [] index, int value)
	{
		this.set(index, new Integer(value));
	}

	/**
	 * Set the object contained here to a long value.
	 * The index argument is ignored.
	 * @see Accessor#setLong
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setLong(int [] index, long value)
	{
		this.set(index, new Long(value));
	}

	/**
	 * Set the object contained here to a float value.
	 * The index argument is ignored.
	 * @see Accessor#setFloat
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setFloat(int [] index, float value)
	{
		this.set(index, new Float(value));
	}

	/**
	 * Set the object contained here to a double value.
	 * The index argument is ignored.
	 * @see Accessor#setDouble
	 * @param index ignored
	 * @param value the new value
	 */
	public void
	setDouble(int[] index, double value)
	{
		this.set(index, new Double(value));
	}

	/**
	 * @see Accessor#copyout
	 */
	public MultiArray
	copyout(int [] origin, int [] shape)
	{
		if(origin != null && origin.length != 0
				|| shape != null && shape.length != 0)
			throw new IllegalArgumentException("Rank Mismatch");
		try {
			return new MultiArrayImpl(this);
		}
		catch (IOException ie)
		{
			// Can't happen: reading this won't generate i/o
			throw new Error();
		}
	}

	/**
	 * @see Accessor#copyin
	 */
	public void
	copyin(int [] origin, MultiArray src)
		throws IOException
	{
		if(origin != null && origin.length != 0
				|| src.getRank() != 0)
			throw new IllegalArgumentException("Rank Mismatch");
		// We know src isScalar, but we don't know if it ignores
		// the index arg...
		final int [] nonarg = new int[] {};
		set(nonarg, src.get(nonarg));
	}

	
	/**
	 * TODO: fishy semantics.
	 * @see Accessor#toArray
	 */
	 public Object
	toArray()
	{
		return obj;
	}
	/**
	 * TODO: fishy semantics.
	 * @see Accessor#toArray
	 */
	 public Object
	     getStorage()
	{
		return obj;
	}


	/**
	 * TODO: fishy semantics.
	 * @see Accessor#toArray
	 */
	public Object
	toArray(Object oo, int [] origin, int [] shape)
	{
		if(oo.getClass().isAssignableFrom(componentType))
		{
			return obj;
		}
		// TODO, numeric promotion

		// else, Can't convert
		throw new IllegalArgumentException();
	}
		
 /* End Accessor */

    private final Class componentType;
    private Object obj;
}
