/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * MultiArray implementation which is an adapter for single values,
 * aka scalars.
 * If you have a single object and want to wrap it
 * in a MultiArray interface, use this class.
 * Rank of these is always zero.
 * The index argument to the set, setXXX, get, and getXXX is ignored.
 * <p>
 * One of the purposes of this class is to substitute for
 * the java.lang primitive wrappers (Double, Float, and so on)
 * in the MultiArray context, providing continuity of access
 * between arrays and scalars. Contrast this with the the
 * discontinuity between java.util.reflect.Array.getDouble()
 * and java.lang.Number.doubleValue().
 * <p>
 * In order to provide the required primitive unwrapping when
 * a ScalarMultiArray has primitive component type,
 * we provide set() operations for each of the primitive wrappers
 * which override set(int [], Object).
 * 
 *
 * @see MultiArray
 * @see ArrayMultiArray
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class ScalarMultiArray implements MultiArray {

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

    /**
     * @see MultiArray#getComponentType
     */
    public Class getComponentType() {
	return componentType;
    }

    /**
     * Always returns zero for members of this class.
     * @see MultiArray#getRank
     * @return int 0
     */
    public int getRank() { return 0;}

    /**
     * Always returns empty array for members of this class.
     * @see MultiArray#getLengths
     * @return int array of length zero.
     */
    public int [] getLengths() {
	int [] lengths = new int[0];
	return lengths;
    }

    /**
     * Always returns <code>false</code> for members of this class.
     * @see MultiArray#isUnlimited
     * @return boolean <code>false</code>
     */
    public boolean isUnlimited() { return false; }

    /**
     * Alway <code>true</code> for members of this class.
     * @see MultiArray#isUnlimited
     * @return boolean <code>true</code>
     */
    public boolean isScalar() { return true; }

/****/

    /**
     * Retrieve the object in this container.
     * @see MultiArray#get
     * @param index ignored
     * @return the Object contained herein
     */
    public Object get(int [] index)
    {
	return obj;
    }

    /**
     * As if <code>(((Character)this.get(index)).charValue();</code>	
     * were called.
     * @see MultiArray#getChar
     * @see #get
     * @see java.lang.Character#charValue
     */
    public char getChar(int[] index)
    {
	return ((Character)obj).charValue();
    }

    /**
     * As if <code>(((Boolean)this.get(index)).booleanValue();</code>	
     * were called.
     * @see MultiArray#getBoolean
     * @see #get
     * @see java.lang.Boolean#booleanValue
     */
    public boolean getBoolean(int[] index)
    {
	return ((Boolean)obj).booleanValue();
    }

    /**
     * As if <code>(((Number)this.get(index)).byteValue();</code>	
     * were called.
     * @see MultiArray#getByte
     * @see #get
     * @see java.lang.Number#byteValue
     */
    public byte getByte(int[] index)
    {
	return ((Number)obj).byteValue();
    }

    /**
     * As if <code>(((Number)this.get(index)).shortValue();</code>	
     * were called.
     * @see MultiArray#getShort
     * @see #get
     * @see java.lang.Number#shortValue
     */
    public short getShort(int[] index)
    {
	return ((Number)obj).shortValue();
    }

    /**
     * As if <code>(((Number)this.get(index)).intValue();</code>	
     * were called.
     * @see MultiArray#getInt
     * @see #get
     * @see java.lang.Number#intValue
     */
    public int getInt(int[] index)
    {
	return ((Number)obj).intValue();
    }

    /**
     * As if <code>(((Number)this.get(index)).longValue();</code>	
     * were called.
     * @see MultiArray#getLong
     * @see #get
     * @see java.lang.Number#longValue
     */
    public long getLong(int[] index)
    {
	return ((Number)obj).longValue();
    }

    /**
     * As if <code>(((Number)this.get(index)).floatValue();</code>	
     * were called.
     * @see MultiArray#getFloat
     * @see #get
     * @see java.lang.Number#floatValue
     */
    public float getFloat(int[] index)
    {
	return ((Number)obj).floatValue();
    }

    /**
     * As if <code>(((Number)this.get(index)).doubleValue();</code>	
     * were called.
     * @see MultiArray#getDouble
     * @see #get
     * @see java.lang.Number#doubleValue
     */
    public double getDouble(int[] index)
    {
	return ((Number)obj).doubleValue();
    }

    /**
     * Set the object contained here to value.
     * The index argument is ignored.
     * @param index ignored
     * @param Object value the replacement contents
     */
    public void set(int [] index, Object value)
    {
	final Class xcls = value.getClass();
	if(!componentType.isAssignableFrom(xcls))
	{
		throw new IllegalArgumentException();
	}
	obj = value;
    }

    /**
     * Overides <code>set(int [], Object)</code>
     *to provide primitive unwrapping.
     * Set the object contained here to Boolean value.
     * The index argument is ignored.
     * @param index ignored
     * @param Boolean value the replacement contents
     */
    public void set(int [] index, Boolean value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Boolean.TYPE)
	{
		obj = value;
		return;
	}
	// else
	throw new IllegalArgumentException();
    }

    /**
     * Set the object contained here to a boolean value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param boolean value the new value
     */
    public void setBoolean(int [] index, boolean value)
    {
	this.set(index, new Boolean(value));
    }

    /**
     * Overides <code>set(int [], Object)</code>
     * to provide primitive unwrapping.
     * Set the object contained here to Character value.
     * The index argument is ignored.
     * @param index ignored
     * @param Character value the replacement contents
     */
    public void set(int [] index, Character value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Character.TYPE)
	{
		obj = value;
		return;
	}
	// else
	throw new IllegalArgumentException();
    }

    /**
     * Set the object contained here to a char value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param char value the new value
     */
    public void setChar(int [] index, char value)
    {
	this.set(index, new Character(value));
    }

    /**
     * Sort of a cast operation.
     * Return a new primitive wrapper to contain
     * primitive type ct with value nn.
     */
    static Number
    reWrap(Class ct, Number nn)
    {
	if(ct == Byte.TYPE)
		return new Byte(nn.byteValue());
	if(ct == Short.TYPE)
		return new Short(nn.shortValue());
	if(ct == Integer.TYPE)
		return new Integer(nn.intValue());
	if(ct == Long.TYPE)
		return new Long(nn.longValue());
	if(ct == Float.TYPE)
		return new Float(nn.floatValue());
	if(ct == Double.TYPE)
		return new Double(nn.doubleValue());
	// else
	throw new IllegalArgumentException();
    }

    /**
     * Overides <code>set(int [], Object)</code>
     * to provide primitive unwrapping.
     * Set the object contained here to Byte value.
     * The index argument is ignored.
     * @param index ignored
     * @param Object value the replacement contents
     */
    public void set(int [] index, Byte value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Byte.TYPE)
	{
		obj = value;
		return;
	}
	// else
	obj = reWrap(componentType, value);
    }

    /**
     * Set the object contained here to a byte value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param char value the new value
     */
    public void setByte(int [] index, byte value)
    {
	this.set(index, new Byte(value));
    }

    /**
     * Overides <code>set(int [], Object)</code>
     * to provide primitive unwrapping.
     * Set the object contained here to Short value.
     * The index argument is ignored.
     * @param index ignored
     * @param Short value the replacement contents
     */
    public void set(int [] index, Short value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Short.TYPE)
	{
		obj = value;
		return;
	}
	// else
	obj = reWrap(componentType, value);
    }

    /**
     * Set the object contained here to a short value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param short value the new value
     */
    public void setShort(int [] index, short value)
    {
	this.set(index, new Short(value));
    }

    /**
     * Overides <code>set(int [], Object)</code>
     * to provide primitive unwrapping.
     * Set the object contained here to Integer value.
     * The index argument is ignored.
     * @param index ignored
     * @param Integer value the replacement contents
     */
    public void set(int [] index, Integer value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Integer.TYPE)
	{
		obj = value;
		return;
	}
	// else
	obj = reWrap(componentType, value);
    }

    /**
     * Set the object contained here to a int value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param int value the new value
     */
    public void setInt(int [] index, int value)
    {
	this.set(index, new Integer(value));
    }

    /**
     * Overides <code>set(int [], Object)</code>
     * to provide primitive unwrapping.
     * Set the object contained here to Long value.
     * The index argument is ignored.
     * @param index ignored
     * @param Long value the replacement contents
     */
    public void set(int [] index, Long value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Long.TYPE)
	{
		obj = value;
		return;
	}
	// else
	obj = reWrap(componentType, value);
    }

    /**
     * Set the object contained here to a long value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param long value the new value
     */
    public void setLong(int [] index, long value)
    {
	this.set(index, new Long(value));
    }

    /**
     * Overides <code>set(int [], Object)</code>
     * to provide primitive unwrapping.
     * Set the object contained here to Float value.
     * The index argument is ignored.
     * @param index ignored
     * @param Float value the replacement contents
     */
    public void set(int [] index, Float value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Float.TYPE)
	{
		obj = value;
		return;
	}
	// else
	obj = reWrap(componentType, value);
    }

    /**
     * Set the object contained here to a float value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param float value the new value
     */
    public void setFloat(int [] index, float value)
    {
	this.set(index, new Float(value));
    }

    /**
     * Overides <code>set(int [], Object)</code>
     * to provide primitive unwrapping.
     * Set the object contained here to Double value.
     * The index argument is ignored.
     * @param index ignored
     * @param Double value the replacement contents
     */
    public void set(int [] index, Double value)
    {
	final Class xcls = value.getClass();
	if(componentType.isAssignableFrom(xcls) ||
		 componentType == Double.TYPE)
	{
		obj = value;
		return;
	}
	// else
	obj = reWrap(componentType, value);
    }

    /**
     * Set the object contained here to a double value.
     * The index argument is ignored.
     * @see MultiArray#set
     * @param index ignored
     * @param double value the new value
     */
    public void setDouble(int[] index, double value)
    {
	this.set(index, new Double(value));
    }

	public MultiArray
	copyout(int [] origin, int [] shape)
	{
		throw new RuntimeException("Not Yet Implemented");
	}

	public void
	copyin(int [] origin, MultiArray data)
	{
		throw new RuntimeException("Not Yet Implemented");
	}


    private final Class componentType;
    private Object obj;
}
