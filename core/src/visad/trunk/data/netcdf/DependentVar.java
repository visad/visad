/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DependentVar.java,v 1.2 1998-03-11 16:21:50 steve Exp $
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
