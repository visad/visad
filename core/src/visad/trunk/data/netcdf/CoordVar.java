/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CoordVar.java,v 1.5 1998-03-10 19:49:29 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import visad.Linear1DSet;
import visad.Unit;
import visad.data.BadFormException;




/*
 * Class for coordinate variables.
 */
class
CoordVar
    extends ExportVar
{
    /**
     * The linear, sampling domain set.
     */
    protected final Linear1DSet	set;

    /**
     * The unit.
     */
    protected final Unit	unit;


    /**
     * Construct.
     */
    protected
    CoordVar(String name, Dimension dim, Unit unit, Linear1DSet set)
	throws BadFormException
    {
	super(name, Float.TYPE, new Dimension[] {dim}, myAttributes(unit));
	this.set = set;
	this.unit = unit;
    }


    /**
     * Return my attributes for construction.
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
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	int	index = indexes[indexes.length-1];

	try
	{
	    return new Float(set.indexToValue(new int[] {index})[0][0]);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }


    /**
     * Indicate whether or not this co-ordinate variable is the same as
     * another co-ordinate variable.
     */
    public boolean
    equals(CoordVar that)
    {
	return getName().equals(that.getName()) && 
		getRank() == that.getRank() &&
		getLengths()[0] == that.getLengths()[0] &&
		(unit == that.unit || unit.equals(that.unit)) &&
		set.equals(that.set);
    }
}
