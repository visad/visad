/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DependentRealVar.java,v 1.4 2000-04-26 15:45:24 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Attribute;
import visad.DoubleSet;
import visad.FloatSet;
import visad.Real;
import visad.RealType;
import visad.ScalarType;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * The DependentRealVar class adapts numeric data in a VisAD data object to
 * a netCDF, dependent-variable, API for the purpose of exporting the data.
 */
class
DependentRealVar
    extends	DependentVar
{
    /**
     * The fill-value object.
     */
    private final Number	fillValue;


    /**
     * Construct.
     *
     * @param real		The VisAD Real object to be adapted.
     * @param accessor		The means for accessing the individual VisAD
     *				<code>Real</code> objects of the enclosing
     *				VisAD data object.
     * @exception BadFormException		The VisAD data object cannot be
     *		adapted to a netCDF API
     * @exception VisADException		Problem in core VisAD.
     *		Probably some VisAD object couldn't be created.
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
     *
     * @param real	The VisAD data object for which netCDF Attribute
     *			must be created.
     * @return		An array of netCDF Attributes for <code>real</code>.
     * @exception BadFormException		The VisAD data object cannot be
     *		adapted to a netCDF API
     * @exception VisADException		Problem in core VisAD.
     *		Probably some VisAD object couldn't be created.
     */
    protected static Attribute[]
    myAttributes(Real real)
	throws VisADException, BadFormException
    {
	RealType	realType = (RealType)real.getType();
	Number		fillNumber = getFillValue(getJavaClass(
	    realType.getDefaultSet()));
	Unit		unit = real.getUnit();
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
     * @param set	The VisAD Set describing the range of the variable
     *			data.
     * @precondition	The set is that of a range value (i.e. DoubleSet,
     *			FloatSet, Linear1DSet, etc.).
     * @return		The Java class corresponding to the variable data
     *			(i.e. Double, Float, Integer, etc.).
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
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
     * @exception BadFormException
     *			Unknown netCDF type.
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
     *
     * @param indexes	The netCDF indexes of the desired datum.  Includes all
     *			adapted dimensions -- including those of all enclosing
     *			VisAD data objects.
     * @return		A Java Double that contains the data value or NaN if
     *			the data is missing.
     * @exception IOException
     *			Data access failure.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	Double	value = (Double)getAccessor().get(indexes);

	return value.isNaN()
		    ? fillValue
		    : value;
    }
}
