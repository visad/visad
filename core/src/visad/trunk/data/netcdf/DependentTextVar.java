/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DependentTextVar.java,v 1.1 1998-03-11 16:21:49 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Attribute;
import visad.ScalarType;
import visad.Text;
import visad.data.BadFormException;


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
