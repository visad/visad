/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfQuantityDB.java,v 1.7 1999-01-07 17:01:30 steve Exp $
 */

package visad.data.netcdf.in;

import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.QuantityDBList;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for mapping netCDF elements to VisAD quantities.
 */
public class
NetcdfQuantityDB
    extends	QuantityDBList
{
    /**
     * Constructs from another quantity database.
     */
    public
    NetcdfQuantityDB(QuantityDB db)
    {
	super(db);
    }

	
    /**
     * Return the VisAD quantity corresponding to the best combination of
     * long name and name.
     *
     * @param longName	The long name of the quantity.  May be 
     *			<code>null</code>.
     * @param name	The name of the quantity.
     * @return		The corresponding, unique, VisAD quantity or 
     *			<code>null</code> if no such quantity exists.
     */
    public Quantity
    getBest(String longName, String name)
    {
	Quantity	quantity = longName == null
					? null
					: get(longName);

	return quantity != null
		? quantity
		: get(name);
    }
}
