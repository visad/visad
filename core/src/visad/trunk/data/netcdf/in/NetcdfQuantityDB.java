/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfQuantityDB.java,v 1.3 1998-09-16 15:06:39 steve Exp $
 */

package visad.data.netcdf.in;

import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityMap;
import visad.Unit;
import visad.VisADException;


/**
 * This class maps netCDF variables and dimensions to VisAD quantities.
 */
public class
NetcdfQuantityDB
    extends	QuantityMap
{
    /**
     * Return the VisAD quantity corresponding to the best combination of
     * long name, name, and unit.
     *
     * @param longName	The long name of the quantity.  May be 
     *			<code>null</code>.
     * @param name	The name of the quantity.
     * @param unit	The unit of the quantity.  May be <code>null</code>.
     * @return		The corresponding, unique, VisAD quantity or 
     *			<code>null</code> if no such quantity exists.
     */
    protected static Quantity
    getBest(String longName, String name, Unit unit)
    {
	Quantity	quantity = longName == null
					? null
					: getBest(longName, unit);

	return quantity != null
		? quantity
		: getBest(name, unit);
    }


    /**
     * Return the VisAD quantity corresponding to a netCDF dimension.
     *
     * @param dim		The adapted, netCDF dimension to be examined.
     * @return			The quantity corresponding to <code>dim</dim>.
     */
    public static Quantity
    get(NcDim dim)
    {
	return getBest(dim.getLongName(), dim.getName(), dim.getUnit());
    }


    /**
     * Return the VisAD quantity corresponding to an adapted, netCDF variable.
     *
     * @param var		The adapted, netCDF variable to be examined.
     * @return			The quantity corresponding to <code>var</dim>.
     */
    public static Quantity
    get(NcVar var)
    {
	return getBest(var.getLongName(), var.getName(), var.getUnit());
    }


    /**
     * Return the VisAD quantity corresponding to a netCDF variable.
     *
     * @param var		The netCDF variable to be examined.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>var</code>.
     * @return			The quantity corresponding to <code>var</code>.
     */
    public static Quantity
    get(Variable var, Netcdf netcdf)
    {
	return getBest(NcVar.getLongName(var, netcdf), var.getName(), 
	    NcVar.getUnit(var, netcdf));
    }
}
