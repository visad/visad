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

	Attribute[]	attrs = new Attribute[unit == null ? 1 : 2];

	/*
	 * The following is necessary because the constructors
	 * Attribute(String, Number) and Attribute(String, Object) both
	 * exist and the compiler won't choose the "Number" constructor
	 * if the argument is the common-type "Object".
	 */
	attrs[0] = fillObject instanceof Number
		    ? new Attribute("_FillValue", (Number)fillObject)
		    : new Attribute("_FillValue", (String)fillObject);

	if (unit != null)
	    attrs[1] = new Attribute("units", unit.toString());

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
    static Object
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
     * given type.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The default fill-value object for the given netCDF
     *			type.
     * @exception BadFormException	Unknown netCDF type.
     */
    static Number
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


/*
 * Class for independent variables.  An independent variable is a netCDF
 * variable that has been created because it was necessary to define
 * the netCDF variables in terms of an "index" co-ordinate.  This can
 * happen, for example, in a Fleld with a domain that's a GriddedSet
 * but not a LinearSet.
 */
class
IndependentVar
    extends ExportVar
{
    /**
     * The dimension index of the "variable" in the domain.
     */
    protected final int		idim;


    /**
     * The sampling domain set.
     */
    protected final GriddedSet	set;


    /**
     * Construct from broken-out information.
     */
    protected
    IndependentVar(String name, Dimension dim, Unit unit,
	    GriddedSet set, int idim)
	throws BadFormException
    {
	super(name, Float.TYPE, new Dimension[] {dim}, unit);
	this.set = set;
	this.idim = idim;
    }


    /**
     * Return an array element identified by position.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	int	index = indexes[indexes.length-1];

	try
	{
	    return getExportObject(
		set.indexToValue(new int[] {index})[idim][0]);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}


/**
 * Superclass for a netCDF variable that exists somewhere in the 
 * range of a Field.
 */
abstract class
RangeVar
    extends	ExportVar
{
    /**
     * The field in which the data resides.
     */
    protected final Field	field;

    /**
     * The shape of the netCDF variable in netCDF order (i.e. big
     * endian).
     */
    protected /*final*/ int[]	shape;


    /**
     * Construct from broken-out information.
     */
    protected
    RangeVar(String name, Dimension[] dims, Unit unit, Field field, Set set)
	throws BadFormException, VisADException
    {
	super(name, getJavaClass(set), dims, unit);

	this.field = field;

	shape = new int[dims.length];

	for (int idim = 0; idim < dims.length; ++idim)
	    shape[idim] = dims[dims.length-1-idim].getLength();
    }


    /**
     * Convert a netCDF index vector to a VisAD scalar index.
     *
     * @param netcdfIndexes	Vector of netCDF indexes; innermost dimension
     *			last.
     * @return		VisAD index corresponding to netCDF index.
     * @precondition	<code>netcdfIndex</code> != null && 
     * 			<code>netcdfIndex.length</code> == domain rank
     *			&& indexed point lies within the sampling set.
     * @postcondition	Returned VisAD index lies within sampling set.
     */
    protected int
    visadIndex(int[] netcdfIndexes)
    {
	int	visadIndex = netcdfIndexes[0];

	for (int i = 1; i < shape.length; ++i)
	    visadIndex = visadIndex * shape[i] + netcdfIndexes[i];

	return visadIndex;
    }
}


/**
 * Inner class for a netCDF variable comprising a Field's range.
 */
class
RealVar
    extends RangeVar
{
    /**
     * Construct from broken-out information.
     */
    protected
    RealVar(String name, Dimension[] dims, Unit unit, Field field, Set set)
	throws BadFormException, VisADException
    {
	super(name, dims, unit, field, set);
    }


    /**
     * Return an array element identified by position.
     *
     * @precondition	<code>indexes</code> != null && 
     *			<code>indexes.length</code> == domain rank
     *			&& indexed point lies within the domain.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return getExportObject(
		((Real)field.getSample(visadIndex(indexes))).getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}


/*
 * Class for a netCDF variable comprising a Real component of 
 * a Field's Tuple or RealTuple range.
 */
class
TupleVar
    extends RealVar
{
    /**
     * The index of the component in the Tuple range corresponding
     * to the variable.
     */
    protected final int		icomp;


    /**
     * Construct from broken-out information.
     */
    protected
    TupleVar(String name, Dimension[] dims, Unit unit, Field field, int icomp)
	throws BadFormException, VisADException, RemoteException
    {
	super(name, dims, unit, field,
		((RealType)((TupleType)
		((FunctionType)field.getType()).getRange()).
		getComponent(icomp)).getDefaultSet());
	this.icomp = icomp;
    }


    /**
     * Return an array element identified by position.
     *
     * @precondition	<code>indexes</code> != null && 
     *			<code>indexes.length</code> == domain rank
     *			&& indexed point lies within the domain.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return getExportObject(((Real)((Tuple)field.
		getSample(visadIndex(indexes))).getComponent(icomp)).
		    getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}


/*
 * Class for a netCDF variable comprising a Real component of 
 * a RealTuple of a Field's Tuple range.
 */
class
TupleRealVar
    extends RealVar
{
    /**
     * The index of the component in the Tuple range containing
     * the variable.
     */
    protected final int	tupleComp;


    /**
     * The index of the component in the RealTuple corresponding
     * to the variable.
     */
    protected final int	realTupleComp;


    /**
     * Construct from broken-out information.
     */
    protected
    TupleRealVar(String name, Dimension[] dims, Unit unit, int tupleComp,
	    int realTupleComp, Field field)
	throws BadFormException, VisADException, RemoteException
    {
	super(name, dims, unit, field,
	    ((RealType)((RealTupleType)((TupleType)
		((FunctionType)field.getType()).getRange()).
		getComponent(tupleComp)).getComponent(realTupleComp)).
		getDefaultSet());
	this.tupleComp = tupleComp;
	this.realTupleComp = realTupleComp;
    }


    /**
     * Return an array element identified by position.
     *
     * @precondition	<code>indexes</code> != null && 
     *			<code>indexes.length</code> == domain rank
     *			&& indexed point lies within the domain.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return getExportObject(((Real)((RealTuple)((Tuple)field.
		getSample(visadIndex(indexes))).
		    getComponent(tupleComp)).getComponent(realTupleComp)).
		    getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}
