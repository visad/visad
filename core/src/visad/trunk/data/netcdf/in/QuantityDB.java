/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDB.java,v 1.2 1998-06-22 18:30:45 visad Exp $
 */

package visad.data.netcdf.in;

import ucar.netcdf.Variable;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.StandardQuantityDB;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.units.NoSuchUnitException;
import visad.data.netcdf.units.ParseException;


/**
 * This class maps netCDF variables and dimensions to VisAD quantities.
 */
public class
QuantityDB
    extends	visad.data.netcdf.QuantityDB
{
    /**
     * The singleton instance.
     */
    private static QuantityDB	db;


    /**
     * Construct.
     */
    private QuantityDB()
	throws VisADException
    {
	super(StandardQuantityDB.instance());

	try
	{
	    add("lat", super.get("latitude", "degrees_north"));	// alias
	    add("lon", super.get("longitude", "degrees_east"));	// alias
	    add("pressure reduced to MSL", "hectopascals");	// new
	}
	catch (ParseException e)
	{
	    /*
	     * This shouldn't happen because the above strings should be
	     * correct.
	     */
	    throw new VisADException(e.getMessage());
	}
    }


    /**
     * Return the singleton instance of this database.
     */
    public static QuantityDB
    instance()
	throws VisADException
    {
	if (db == null)
	    db = new QuantityDB();

	return db;
    }


    /**
     * Return the VisAD quantity corresponding to the best combination of
     * name and unit.
     *
     * @param name	The name of the quantity.
     * @param unit	The unit of the quantity.  May be <code>null</code>.
     * @return		The corresponding, unique, VisAD quantity or 
     *			<code>null</code> if no such quantity exists.
     */
    protected Quantity
    getBest(String name, Unit unit)
    {
	return unit == null
		    ? getIfUnique(name)
		    : get(name, unit);
    }


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
    protected Quantity
    getBest(String longName, String name, Unit unit)
    {
	Quantity	quantity = longName == null
					? null
					: getBest(longName, unit);

	if (quantity == null)
	{
	    quantity = getBest(name, unit);
	}

	return quantity;
    }


    /**
     * Return the VisAD quantity corresponding to a netCDF dimension.
     */
    public Quantity
    get(NcDim dim)
    {
	return getBest(dim.getLongName(), dim.getName(), dim.getUnit());
    }


    /**
     * Return the VisAD quantity corresponding to an adapted, netCDF variable.
     */
    public Quantity
    get(NcVar var)
    {
	return getBest(var.getLongName(), var.getName(), var.getUnit());
    }


    /**
     * Return the VisAD quantity corresponding to a netCDF variable.
     */
    public Quantity
    get(Variable var)
    {
	return getBest(NcVar.getLongName(var), var.getName(), 
	    NcVar.getUnit(var));
    }
}
