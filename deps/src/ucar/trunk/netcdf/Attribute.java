// $Id: Attribute.java,v 1.4 2002-05-29 18:31:32 steve Exp $
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
import java.io.Serializable;
import java.lang.String;
import java.lang.reflect.Array;

/**
 * Attributes are similar to "IndexedProperties" in the lingo
 * of java beans. They have a name, type and an array of
 * values. The array is often of length 1, degenerating into
 * a simple property. The array should never be length 0.
 * An Attribute object is used to contain netcdf "metadata",
 * like units of a measurable quantity or its valid range.
 * <p>
 * These attributes have fixed values over their lifetime;
 * no setValue() methods are provided.
 * <p>
 * Instances which have same name and same value elements are equal.
 * We override hashCode() and equals() to be consistent with
 * this semantic.
 *
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:32 $
 */
/*
 * Implementation Notes:
 * <p>
 * We factor the value into two cases, a String value and
 * an array of (numeric) primitive. Things would have been
 * completely symmetric (eliminating this layer) if we just copied
 * strings into array of char and copied out to string. Seems like a
 * waste when given an immutable object to start with.
 * <p>
 * The members are immutable. The value is built by copying
 * in. So, we can safely use the default clone() method.
 */
public class
Attribute
	implements Named, Serializable, Cloneable
{
	/**
	 * Construct simple numeric attribute.
	 *
	 * @param name  String which is to be the name of this Attribute
	 * @param value A Number to be the Attribute value
	 */
	public
	Attribute(String name, Number value)
	{
		final Class componentType = primitiveClass(value);
		if(!ProtoVariable.checkComponentType(componentType))
			throw new IllegalArgumentException("Invalid Type: "
				+ componentType);
		this.name = name;
		this.value = new NumericAttrVal(value, componentType);
	}

	/**
	 * Construct simple numeric attribute.
	 *
	 * @param name  String which is to be the name of this Attribute
	 * @param value A double to be the Attribute value
	 */
	public
	Attribute(String name, double value)
	{
		this.name = name;
		final double [] darray = {value};
		this.value = new NumericAttrVal(darray);
	}

	/**
	 * Construct a string valued attribute.
	 * This will be seen to have component type Character.TYPE
	 *
	 * @param name  String which is to be the name of this Attribute
	 * @param theValue  The value
	 */
	public
	Attribute(String name, String theValue)
	{
		this.name = name;
		this.value = new StringAttrVal(theValue);
	}

	/**
	 * Construct an array valued Attribute.
	 * Not often used.
	 *
	 * @param name  String which is to be the name of this Attribute
	 * @param theValue  The value, an array of primitives. The primitive
	 * 	type must be netcdf encodeable.
	 */
	public
	Attribute(String name, Object theValue)
	{
		/*
		 * check that arg is an array
		 */
		final Class aClass = theValue.getClass();
		if(!aClass.isArray())
			throw new IllegalArgumentException("Not an Array");
		// else

		/*
		 * Check that the array componentType is netcdf encodeable.
		 */
		final Class componentType = aClass.getComponentType();
		if(!ProtoVariable.checkComponentType(componentType))
			throw new IllegalArgumentException("Invalid Type: "
				+ componentType);
		// else

		this.name = name;
		if(componentType ==  Character.TYPE)
		{
			this.value = new StringAttrVal((char [])theValue);
		}
		else
		{
			/* make a private copy of the array */
			this.value = new NumericAttrVal(arrayClone(theValue));
		}
	}

 /* Begin Overrides */

	/**
	 * Instances which have same name and same value elements are equal.
	 * Overrides Object.equals() to be consistent with
	 * this semantic.
	 *
	 * @return the hash code value for this Attribute
	 */
	public int
	hashCode()
	{
		return (name.hashCode() ^ value.hashCode());
	}

	/**
	 * Instances which have same name and same value elements are equal.
	 * Overrides Object.equals() to be consistent with
	 * this semantic.
	 * TODO: test me.
	 */
	public boolean
	equals(Object oo)
	{
		if(this == oo) return true;
		if(oo instanceof Attribute)
		{
			final Attribute aa = (Attribute) oo;
			if(name.equals(aa.getName()))
			{
				return value.equals(aa.value);
			}
		}
		return false;
	}

	/**
	 * @return a string representation of this
	 */
	public String
	toString() {
		StringBuffer buf = new StringBuffer();
		toCdl(buf);
		return buf.toString();
	}

 /* End Overrides */

	/**
	 * Returns the name of this Attribute.
	 * @return String which identifies this Attribute.
	 */
	public final
	String getName()
	{
		return name;
	}

	/**
	 * Retrieve the value in its most general form.
	 * @return Object which is either a java.lang.String or
	 *   a 1-dimensional array of primitives.
	 * @see Attribute#isString
	 */
	public Object
	getValue()
		{ return value.getValue(); }

	/**
	 * Retrieve String value.
	 * @return String if this is a String valued attribute.
	 * @throws ClassCastException if this is not String valued.
	 * @see Attribute#isString
	 */
	public String
	getStringValue()
		{ return (String) value.getValue(); }

	/**
	 * Retrieve indexed value.
	 * @param index int which is the index into the value array.
	 * @return Number <code>value[index]</code>
	 */
	public Object
	get(int index)
		{ return value.get(index); }

	/**
	 * Retrieve indexed numeric value.
	 * @param index int which is the index into the value array.
	 * @return Number <code>value[index]</code>
	 */
	public Number
	getNumericValue(int index)
		{ return value.getNumericValue(index); }

	/**
	 * Retrieve simple numeric value.
	 * Equivalent to <code>getNumericValue(0)</code>
	 * @return Number the first element of the value array
	 */
	public Number
	getNumericValue()
		{ return value.getNumericValue(); }

	/**
	 * If the value is an instance of String, return <code>true</code>
	 * otherwise returns <code>false</code>
	 * @return boolean value instanceof String
	 */
	public boolean
	isString()
	{
		return value instanceof StringAttrVal;
	}

	/**
	 * If the value represents an array type, returns the Class
	 * object representing the component type of the array; otherwise
	 * returns null.
	 * @return Class the component type
	 * @see java.lang.Class#getComponentType
	 */
	public Class
	getComponentType()
		{ return value.getComponentType(); }

	/**
	 * If the value represents an array type, returns the length
	 * of the value array;
	 * otherwise return String.length of the String value.
	 * @return int length of the value
	 */
	public int
	getLength()
		{ return value.getLength(); }

	/**
	 * Format as CDL.
	 * @param buf StringBuffer into which to write
	 */
	public void
	toCdl(StringBuffer buf)
	{
		buf.append(":");
		buf.append(this.getName());
		buf.append(" = ");
		value.toCdl(buf);
		buf.append(" ;");
	}

	/**
	 * Why isn't this in java.lang.reflect.Array as native?
	 */
	static Object
	arrayClone(Object src)
	{
		final int length = Array.getLength(src);
		Object aa = Array.newInstance(src.getClass().getComponentType(),
			length);
		System.arraycopy(src, 0, aa, 0, length);
		return aa;
	}

	/**
	 * Use reflection to find out the TYPE (primitive class)
	 * corresponding to a Number.
	 */
	static Class
	primitiveClass(Number nn)
	{
		try {
			return (Class) nn.getClass().getDeclaredField("TYPE").
				get(nn);
		}
		catch (NoSuchFieldException ee)
		{
			// this shouldn't happen, since Numbers have TYPE
			throw new Error();
		}
		catch (IllegalAccessException ee)
		{
			// this shouldn't happen, since TYPE is public
			throw new Error();
		}
	}


	/**
	 * @serial
	 */
	private final String name;
	/**
	 * @serial
	 */
	private final AttrVal value;
}

abstract class
AttrVal
	implements Serializable, Cloneable
{
	abstract Object
	getValue();

	abstract Object
	get(int index);

	abstract Number
	getNumericValue(int index);

	abstract Number
	getNumericValue();

	abstract Class
	getComponentType();

	abstract int
	getLength();

	abstract void
	toCdl(StringBuffer buf);
}


final class
NumericAttrVal
	extends AttrVal
{

	NumericAttrVal(Number nn, Class componentType)
	{
		data = Array.newInstance(componentType, 1);
		Array.set(data, 0, nn);
	}


	NumericAttrVal(Object data)
	{
		/* sanity checking and copy in semantics done in caller */
		this.data = data;
	}

	public int
	hashCode()
	{
		int h = 0;
		final int len = getLength();
		for(int ii = 0; ii < len; ii++)
			h = (h * 13) + get(ii).hashCode();
		return h;
	}

	public boolean
	equals(Object oo)
	{
		if(oo instanceof NumericAttrVal)
		{
			final NumericAttrVal aa = (NumericAttrVal) oo;
			if(getComponentType() == aa.getComponentType())
			{
				// potentially equal
				final int length = getLength();
				if(length == aa.getLength())
				{
					for(int ii = 0; ii < length; ii++)
						if(!get(ii).equals(aa.get(ii)))
							return false;
					return true;
				}
			}
		}
		return false;
	}

	Object
	getValue()
	{
		return Attribute.arrayClone(data);
	}

	Object
	get(int index)
	{
		return Array.get(data, index);
	}

	Number
	getNumericValue(int index)
	{
		return (Number)	Array.get(data, index);
	}

	Number
	getNumericValue() {
		return this.getNumericValue(0);
	}

	Class
	getComponentType()
	{
		return data.getClass().getComponentType();
	}

	int
	getLength()
	{
		return Array.getLength(data);
	}

	void
	toCdl(StringBuffer buf)
	{
		final int last = Array.getLength(data) - 1;
		for(int ii = 0; ii <= last; ii++) {
			buf.append(Array.get(data, ii));
			if(ii < last)
				buf.append(", ");
		}
	}

	/**
	 * An array of primitives
	 */
	private final Object data;

}

final class StringAttrVal extends AttrVal {

  StringAttrVal(String str) {
    if (str.length() == 0) {  // empty string test added -dwd
      data = "";
      return;
    }
    if (str.charAt(str.length()-1) == 0) { // trailing null test - dwd
      data = str.substring(0, str.length()-1);
    } else
      data = str;
    }


  StringAttrVal(char [] charArray) {
    int len = charArray.length;
    if (len == 0) {  // empty string test added - dwd
      data="";
      return;
    }
    if (charArray[len-1] == 0) {  // trailing null test - dwd
      data = new String(charArray, 0, len-1);
    } else
      data = new String(charArray);
    }

	public int
	hashCode()
	{
		return data.hashCode();
	}

	public boolean
	equals(Object oo)
	{
		if(oo instanceof StringAttrVal)
		{
			final String other = (String)((StringAttrVal)oo).data;
			if(data == other)
				return true;
			// else
			return data.equals(other);
		}
		return false;
	}

	Object
	getValue()
	{
		return data;
	}

	Object
	get(int index)
	{
		return new Character(data.charAt(index));
	}

	Number
	getNumericValue(int index)
	{
		return new Integer(data.charAt(index));
	}

	Number
	getNumericValue() {
		return this.getNumericValue(0);
	}

	Class
	getComponentType()
	{
		return Character.TYPE;
	}

	int
	getLength()
	{
		return data.length();
	}

	void
	toCdl(StringBuffer buf)
	{
		buf.append("\"");
		buf.append(data);
		buf.append("\"");
	}

	private final String data;
}
