/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcFlatRange.java,v 1.1 1998-09-11 15:00:53 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Vector;
import visad.MathType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for the range of a VisAD FlatField.
 *
 * An instance of this class is mutable.
 */
public class
NcFlatRange
    extends	NcRange
{
    /**
     * Constructs from an adapted, netCDF variable.
     */
    public
    NcFlatRange(NcVar var)
	throws VisADException
    {
	add(var);
    }


    /**
     * Gets the VisAD MathType of the given netCDF variable.
     */
    protected MathType
    getMathType(NcVar var)
    {
	return var.getMathType();
    }


    /**
     * Gets the VisAD Sets of this range.
     *
     * @return		The VisAD Sets of this range.
     */
    public Set[]
    getSets()
	throws VisADException
    {
	Set[]	sets = new Set[size()];

	for (int i = 0; i < sets.length; ++i)
	    sets[i] = ((NcNumber)get(i)).getSet();

	return sets;
    }


    /**
     * Gets the units of this range.
     *
     * @return          The VisAD Units of this range.
     */
    public Unit[]
    getUnits()
	throws VisADException
    {
	Unit[]	units = new Unit[size()];

	for (int i = 0; i < units.length; ++i)
	    units[i] = ((NcNumber)get(i)).getUnit();

	return units;
    }


    /**
     * Gets the values of the netCDF variables in this range as an array
     * of doubles.
     *
     * @return			The values of the netCDF variables in this 
     *				range.
     * @throws IOException	I/O failure.
     */
    public double[][]
    getDoubles()
	throws IOException, VisADException
    {
	double[][]	values = new double[size()][];

	for (int i = 0; i < values.length; ++i)
	    values[i] = get(i).getDoubles();

	return values;
    }
}
