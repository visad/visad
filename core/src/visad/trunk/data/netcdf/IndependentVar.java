/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: IndependentVar.java,v 1.5 1998-03-12 22:03:04 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import visad.GriddedSet;
import visad.Unit;
import visad.data.BadFormException;


/*
 * The IndependentVar class provides a way to present an independent variable
 * in a VisAD data object as a netCDF variable.  An independent variable is a
 * netCDF variable that has been created because it was necessary to define
 * dependent netCDF variables in terms of an "index" co-ordinate.  This can
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
     *
     * @param name	The name for the netCDF variable.
     * @param dim	The netCDF dimension for the independent variable.
     * @param unit	The unit of the netCDF variable (may be 
     *			<code>null</code>).
     * @exception BadFormException
     *			The netCDF variable cannot be represented.
     */
    protected
    IndependentVar(String name, Dimension dim, Unit unit,
	    GriddedSet set, int idim)
	throws BadFormException
    {
	super(name, Float.TYPE, new Dimension[] {dim}, myAttributes(unit));
	this.set = set;
	this.idim = idim;
    }


    /**
     * Return my attributes for construction.
     *
     * @param unit	The unit of the netCDF variable (may be 
     *			<code>null</code>).
     * @return		The attributes for the netCDF variable.
     */
    protected static Attribute[]
    myAttributes(Unit unit)
    {
	return unit == null
		? null
		: new Attribute[] { new Attribute("units", unit.toString()) };
    }


    /**
     * Return an array element identified by position.
     *
     * @param indexes		The netCDF indexes of the array element.
     * @return			The array element at the given position.
     * @exception IOException	Data access I/O failure.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return new Float(
		set.indexToValue(new int[] {indexes[indexes.length-1]})
		    [idim][0]);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}
