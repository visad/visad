/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DependentVar.java,v 1.1 1998-03-10 19:49:32 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import visad.DoubleSet;
import visad.FloatSet;
import visad.Real;
import visad.RealType;
import visad.ScalarType;
import visad.Set;
import visad.Text;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * Class for creating the dependent, netCDF variables that reside in an
 * adapted VisAD data object.
 */
abstract class 
DependentVar
    extends	ExportVar
{
    /**
     * The VisADAccessor.
     */
    protected final VisADAccessor	accessor;


    /**
     * Construct.
     */
    protected
    DependentVar(String name, Class type, Dimension[] dims, Attribute[] attrs,
	    VisADAccessor accessor)
	throws BadFormException
    {
	super(name, type, dims, attrs);

	this.accessor = accessor;
    }


    /**
     * Return a netCDF datum identified by position.
     */
    public abstract Object
    get(int[] indexes)
	throws IOException;
}


/**
 * Class for creating the dependent, netCDF, textual variables that reside 
 * in an adapted VisAD data object.
 */
class
DependentTextVar
    extends	DependentVar
{
    /**
     * The fill-value object.
     */
    protected final String	fillValue;


    /**
     * Construct.
     */
    protected
    DependentTextVar(Text text, VisADAccessor accessor)
	throws BadFormException
    {
	super(((ScalarType)text.getType()).getName(), Character.TYPE,
	    accessor.getDimensions(), myAttributes(), accessor);

	fillValue = getFillValue();
    }


    /**
     * Get the netCDF attributes for a DependentTextVar.
     */
    protected static Attribute[]
    myAttributes()
    {
	return new Attribute[]
	    {new Attribute("_FillValue", getFillValue())};
    }


    /**
     * Return the fill-value.
     */
    protected static String
    getFillValue()
    {
	return "\000";
    }


    /**
     * Return a netCDF datum identified by position.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	try
	{
	    return accessor.get(indexes);
	}
	catch (StringIndexOutOfBoundsException e)
	{
	    return fillValue;
	}
    }
}


/**
 * Class for creating the dependent, netCDF, real variables that reside 
 * in an adapted VisAD data object.
 */
class
DependentRealVar
    extends	DependentVar
{
    /**
     * The fill-value object.
     */
    protected final Number	fillValue;


    /**
     * Construct.
     */
    protected
    DependentRealVar(Real real, VisADAccessor accessor)
	throws VisADException, BadFormException
    {
	super(((ScalarType)real.getType()).getName(), 
	    getJavaClass(((RealType)real.getType()).getDefaultSet()),
	    accessor.getDimensions(), 
	    myAttributes(real),
	    accessor);

	fillValue = getFillValue(getJavaClass(
	    ((RealType)real.getType()).getDefaultSet()));
    }


    /**
     * Get the netCDF attributes for a DependentRealVar.
     */
    protected static Attribute[]
    myAttributes(Real real)
	throws VisADException, BadFormException
    {
	RealType	realType = (RealType)real.getType();
	Number		fillNumber = getFillValue(getJavaClass(
	    realType.getDefaultSet()));
	Unit		unit = realType.getDefaultUnit();
	Attribute[]	attrs;

	if (unit == null)
	    attrs = new Attribute[]
	    {
		new Attribute("_FillValue", fillNumber)
	    };
	else
	    attrs = new Attribute[]
	    {
		new Attribute("_FillValue", fillNumber),
		new Attribute("units", unit.toString())
	    };

	return attrs;
    }


    /**
     * Get the class of the Java primitive type that can contain the
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
     * Return the fill-value object for a numeric netCDF variable of the
     * given type.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The default fill-value object for the given netCDF
     *			type.
     * @exception BadFormException	Unknown netCDF type.
     */
    protected static Number
    getFillValue(Class type)
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
     * Return a netCDF datum identified by position.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	Double	value = (Double)accessor.get(indexes);

	return value.isNaN()
		    ? fillValue
		    : value;
    }
}
