/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ExportVar.java,v 1.4 1998-02-23 15:58:17 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import java.rmi.RemoteException;
import ucar.multiarray.Accessor;
import ucar.multiarray.MultiArray;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import ucar.netcdf.ProtoVariable;
import visad.DoubleSet;
import visad.Field;
import visad.FloatSet;
import visad.FunctionType;
import visad.GriddedSet;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.Tuple;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/*
 * Superclass for adapting VisAD data to a netCDF Variable.
 */
abstract class
ExportVar
    implements	Accessor
{
    /**
     * The fill-value export object.
     */
    protected final Object		fillObject;

    /**
     * The associated netCDF ProtoVariable.
     */
    protected final ProtoVariable	protoVar;

    /**
     * The physical units of the variable (used in equals() method).
     */
    protected final Unit		unit;


    /**
     * Construct from broken-out information.
     */
    protected
    ExportVar(String name, Class type, Dimension[] dims, Unit unit)
	throws BadFormException
    {
	this.unit = unit;

	fillObject = getFillValueObject(type);

	int		nattrs = (unit == null ? 0 : 1) +
				 (fillObject == null ? 0 : 1);
	Attribute[]	attrs;

	if (nattrs == 0)
	    attrs = null;
	else
	{
	    int	iattr = 0;

	    attrs = new Attribute[nattrs];

	    /*
	     * The following is necessary because the constructors
	     * Attribute(String, Number) and Attribute(String, Object) both
	     * exist and the compiler won't choose the "Number" constructor
	     * if the argument is the common-type "Object".
	     */
	    if (fillObject != null)
		attrs[iattr++] =
		    fillObject instanceof Number
			? new Attribute("_FillValue", (Number)fillObject)
			: new Attribute("_FillValue", (String)fillObject);

	    if (unit != null)
		attrs[iattr++] = new Attribute("units", unit.toString());
	}

	protoVar = new ProtoVariable(name, type, reverse(dims), attrs);
    }


    /**
     * Return the fill-value object for a netCDF variable of the
     * given type.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The default fill-value object for the given netCDF
     *			type.
     * @exception BadFormException	Unknown netCDF type.
     */
    Object
    getFillValueObject(Class type)
	throws BadFormException
    {
	Object	object;

	if (type.equals(Character.TYPE))
	    object = getFillValueString();
	else
	    object = getFillValueNumber(type);

	return object;
    }


    /**
     * Return the fill-value object for a netCDF text variable.
     */
    static String
    getFillValueString()
    {
	return "\000";
    }


    /**
     * Return the fill-value object for a numeric netCDF variable of the
     * given type.  Overridden in class CoordVar.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The default fill-value object for the given netCDF
     *			type.
     * @exception BadFormException	Unknown netCDF type.
     */
    Number
    getFillValueNumber(Class type)
	throws BadFormException
    {
	Number	number;

	if (type.equals(Byte.TYPE))
	    number = new Byte(Byte.MIN_VALUE);
	else
	if (type.equals(Short.TYPE))
	    number = new Short((short)-32767);
	else
	if (type.equals(Integer.TYPE))
	    number = new Integer(-2147483647);
	else
	if (type.equals(Float.TYPE))
	    number = new Float(9.9692099683868690e+36);
	else
	if (type.equals(Double.TYPE))
	    number = new Double(9.9692099683868690e+36);
	else
	    throw new BadFormException("Unknown netCDF type: " + type);

	return number;
    }


    /**
     * Reverse the order of an array of dimensions.
     */
    private static Dimension[]
    reverse(Dimension[] dims)
	throws BadFormException
    {
	Dimension[]	reversed = new Dimension[dims.length];

	for (int idim = 0; idim < dims.length; ++idim)
	    reversed[idim] = dims[dims.length-1-idim];

	return reversed;
    }


    /**
     * Get the class of the Java primitive type corresponding to the
     * VisAD Set of a VisAD range value.
     *
     * @precondition	The set is that of a range value.
     */
    protected static Class
    getJavaClass(Set set)
	throws VisADException
    {
	if (set == null || set instanceof DoubleSet)
	    return Double.TYPE;
	if (set instanceof FloatSet)
	    return Float.TYPE;
	
	int	nelts = set.getLength();

	return nelts >= 65536
		    ? Integer.TYPE
		    : nelts >= 256
			? Short.TYPE
			: Byte.TYPE;
    }


    /**
     * Return the object to be exported given the float value.
     */
    protected Object
    getExportObject(float value)
    {
	return Float.isNaN(value)
		? fillObject
		: new Float(value);
    }


    /**
     * Return the object to be exported given the double value.
     */
    protected Object
    getExportObject(double value)
    {
	return Double.isNaN(value)
		? fillObject
		: new Double(value);
    }


    /**
     * Return the associated netCDF ProtoVariable.
     */
    ProtoVariable
    getProtoVariable()
    {
	return protoVar;
    }


    /**
     * Return the name of the netCDF variable.
     */
    String
    getName()
    {
	return protoVar.getName();
    }


    /**
     * Return the rank of the netCDF variable.
     */
    int
    getRank()
    {
	return protoVar.getRank();
    }


    /**
     * Return the dimensional lengths of the netCDF variable.
     */
    int[]
    getLengths()
    {
	return protoVar.getLengths();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    copyin(int[] origin, MultiArray multiArray)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    set(int[] index,  Object value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setBoolean(int[] index,  boolean value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setChar(int[] index,  char value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setByte(int[] index,  byte value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setShort(int[] index, short value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setInt(int[] index, int value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setLong(int[] index, long value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setFloat(int[] index, float value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Set an array element identified by position.  Not supported for
     * read-only, VisAD data objects.
     */
    public void
    setDouble(int[] index, double value)
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     *
     * This is the only method that needs to be implemented to support
     * the saving of VisAD data in a netCDF dataset.
     */
     public abstract Object
     get(int[] indexes)
	throws IOException;


    /**
     * Return an array element identified by position.
     */
     public boolean
     getBoolean(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public char
     getChar(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public byte
     getByte(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public short
     getShort(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public int
     getInt(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public long
     getLong(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public float
     getFloat(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return an array element identified by position.
     */
     public double
     getDouble(int[] indexes)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return a MultiArray into a slice of the data.
     */
    public MultiArray
    copyout(int[] origin, int[] shape)
	throws IOException
    {
	throw new UnsupportedOperationException();
    }
}
