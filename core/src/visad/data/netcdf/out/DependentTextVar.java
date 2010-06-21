/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DependentTextVar.java,v 1.3 2000-01-18 18:56:14 steve Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Attribute;
import visad.ScalarType;
import visad.Text;
import visad.data.BadFormException;


/**
 * The DependentTextVar class adapts textual data in a VisAD data object to
 * a netCDF, dependent-variable, API for the purpose of exporting the data.
 * in an adapted VisAD data object.
 */
class
DependentTextVar
    extends	DependentVar
{
    /**
     * The fill-value object.
     */
    private final Character	fillValue;


    /**
     * Construct.
     *
     * @param text	The VisAD Text object to be adapted.
     * @param accessor	The means for accessing the individual VisAD
     *			<code>Text</code> objects of the enclosing
     *			VisAD data object.
     * @exception BadFormException
     *			The VisAD data object cannot be adapted to a netCDF API.
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
     *
     * @return	An array of netCDF Attributes for the variable.
     */
    protected static Attribute[]
    myAttributes()
    {
	return new Attribute[]
	    {new Attribute(
		"_FillValue",
		new String(new char[] {getFillValue().charValue()}))};
    }


    /**
     * Return the fill-value.
     *
     * @return	The netCDF fill-value for netCDF character variables.
     */
    protected static Character
    getFillValue()
    {
	return new Character('\000');
    }


    /**
     * Return a netCDF datum identified by position.
     *
     * @param indexes	The netCDF indexes of the desired datum.  Includes all
     *			adapted dimensions -- including those of all enclosing
     *			VisAD data objects.
     * @return		A Java Character that contains the data value or the
     *			appropriate netCDF fill-value if the data is missing.
     */
    public Object
    get(int[] indexes)
	throws IOException
    {
	try
	{
	    return getAccessor().get(indexes);
	}
	catch (StringIndexOutOfBoundsException e)
	{
	    return fillValue;
	}
    }
}
