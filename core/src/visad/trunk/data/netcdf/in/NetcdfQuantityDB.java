/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfQuantityDB.java,v 1.5 1998-09-23 18:18:50 steve Exp $
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
    public static Quantity
    getBest(String longName, String name, Unit unit)
    {
	Quantity	quantity = longName == null
					? null
					: getBest(longName, unit);

	return quantity != null
		? quantity
		: getBest(name, unit);
    }
}
