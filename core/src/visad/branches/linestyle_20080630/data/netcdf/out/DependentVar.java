/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DependentVar.java,v 1.4 2001-11-27 22:29:38 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import visad.data.BadFormException;


/**
 * The DependentVar class provides an abstract class for adapting data in
 * a VisAD data object to a netCDF, dependent-variable API for the purpose
 * of exporting the data.
 */
abstract class
DependentVar
    extends	ExportVar
{
    /**
     * The VisADAccessor.
     */
    private final VisADAccessor	accessor;


    /**
     * Construct.
     *
     * @param name	The name of the netCDF, dependent variable.
     * @param type	The Java class of the type of the variable (i.e.
     *			Double, Byte, Character, etc.).
     * @param dims	The netCDF dimensions of the variable.
     * @param attrs	The netCDF attributes of the variable.
     * @exception BadFormException
     *			The VisAD data object cannot be adapted to a netCDF API.
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
     *
     * @param indexes	The netCDF indexes of the desired datum.  Includes all
     *			adapted dimensions -- including those of all enclosing
     *			VisAD data objects.
     * @return		An Object that contains the data value or the
     *			appropriate netCDF fill-value if the data is missing.
     * @exception IOException
     *			Data access failure.
     */
    public abstract Object
    get(int[] indexes)
	throws IOException;


    /**
     * Return the data accessor.
     *
     * @return	The data accessor that knows how to get the data from the
     *		VisAD data object.
     */
    protected VisADAccessor
    getAccessor()
    {
	return accessor;
    }
}
