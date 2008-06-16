/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CoordVar.java,v 1.3 2000-04-26 15:45:24 dglo Exp $
 */


package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Attribute;
import ucar.netcdf.Dimension;
import visad.Gridded1DSet;
import visad.Unit;
import visad.data.BadFormException;


/**
 * The CoordVar class handles the exporting of netCDF coordinate variables
 * from a VisAD data object.
 */
class CoordVar extends ExportVar
{
    /** The linear, sampling domain set. */
    private final Gridded1DSet	set;

    /** The unit in which the co-ordinate variable is measured. */
    private final Unit		unit;

    /**
     * Construct.
     *
     * @param name	The name of the coordinate variable.
     * @param dim	The netCDF dimension associated with the coordinate
     *			variable.
     * @param unit	The coordinate variable unit or <code>null</code>.
     * @param set	The values of the coordinate variable.
     * @exception BadFormException	A coordinate variable could not be
     *					formed from the given information.
     */
    CoordVar(String name, Dimension dim, Unit unit, Gridded1DSet set)
	    throws BadFormException {
	super(name, Float.TYPE, new Dimension[] {dim}, myAttributes(unit));
	this.set = set;
	this.unit = unit;
    }

    /**
     * Return my attributes for construction.
     *
     * @param unit	The coordinate variable unit to be made into a
     *			netCDF attribute or <code>null</code>.
     * @return		The array of netCDF attributes for the coordinate
     *			variable.
     */
    protected static Attribute[] myAttributes(Unit unit) {
	return unit == null
		? null
		: new Attribute[] { new Attribute("units", unit.toString()) };
    }

    /**
     * Return an array element identified by position.
     *
     * @param indexes	The position of the element in netCDF indexes.
     * @precondition	<code>indexes.length</code> == 1.
     * @return		The coordinate value at <code>indexes</code>.
     */
     public Object get(int[] indexes) throws IOException {
	int	index = indexes[indexes.length-1];

	try {
	    return new Float(set.indexToValue(new int[] {index})[0][0]);
	} catch (Exception e) {
	    throw new IOException(e.getMessage());
	}
     }

    /**
     * Indicate whether or not this co-ordinate variable is the same as
     * another co-ordinate variable.
     *
     * @param that	The other coordinate variable.
     * @return		<code>true</code> if and only if the coordinate
     *			variables are semantically identical.
     */
    public boolean equals(CoordVar that) {
	return getName().equals(that.getName())
		&& getRank() == that.getRank()
		&& getLengths()[0] == that.getLengths()[0]
		&& (unit == that.unit || unit.equals(that.unit))
		&& set.equals(that.set);
    }
}
