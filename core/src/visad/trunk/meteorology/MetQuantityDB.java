/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: MetQuantityDB.java,v 1.2 1998-11-16 18:23:48 steve Exp $
 */

package visad.meteorology;

import visad.VisADException;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.QuantityDBImpl;
import visad.data.netcdf.QuantityDBList;
import visad.data.netcdf.StandardQuantityDB;
import visad.data.netcdf.units.ParseException;


/**
 * Provides support for mapping meteorological quantities to VisAD Quantities.
 */
public class
MetQuantityDB
    extends	QuantityDBList
{
    /**
     * The singleton instance.
     */
    private static MetQuantityDB	db;


    /**
     * Constructs from nothing.  Private to ensure use of instance() method.
     */
    private MetQuantityDB()
	throws VisADException
    {
	QuantityDBImpl	metQuantityDB = new QuantityDBImpl();

	try
	{
	    Quantity	quantity = new Quantity("Pressure", "millibar");
	    metQuantityDB.add("Pressure", quantity);
	    metQuantityDB.add("PressureReducedToMSL", quantity);
	    metQuantityDB.add("Temperature", "degC");
	    metQuantityDB.add("DewPoint", "degC");
	    metQuantityDB.add("Theta", "degC");
	    metQuantityDB.add("ThetaES", "degC");
	    metQuantityDB.add("Rsat", "grams/kilogram");
	    metQuantityDB.add("Speed", "kt");
	    metQuantityDB.add("Direction", "degrees_true");
	    metQuantityDB.add("U", "kt");
	    metQuantityDB.add("V", "kt");
	    metQuantityDB.add("W", "kt");

	}
	catch (ParseException e)
	{
	    /*
	     * This shouldn't happen because the above strings should be
	     * correct.
	     */
	    throw new VisADException(e.getMessage());
	}

	append(metQuantityDB);
	append(StandardQuantityDB.instance());
    }


    /**
     * Returns the singleton instance of this class.
     */
    public static MetQuantityDB
    instance()
	throws VisADException
    {
	if (db == null)
	    db = new MetQuantityDB();

	return db;
    }


    /**
     * Tests this class.
     */
    public static void
    main(String[] args)
	throws	Exception
    {
	MetQuantityDB	metQuantityDB = MetQuantityDB.instance();
	QuantityDB	standardQuantityDB = StandardQuantityDB.instance();

	System.out.println(
    "standardQuantityDB.getFirst(\"pressure\").getDefaultUnitString()=\"" +
	    standardQuantityDB.getFirst(
		"pressure").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"pressure\").getDefaultUnitString()=\"" +
	    metQuantityDB.getFirst(
		"pressure").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"U\").getDefaultUnitString()=\"" +
	    metQuantityDB.getFirst(
		"U").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"V\").getDefaultUnitString()=\"" +
	    metQuantityDB.getFirst(
		"V").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"Speed\").getDefaultUnitString()=\"" +
	    metQuantityDB.getFirst(
		"Speed").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"Direction\").getDefaultUnitString()=\"" +
	    metQuantityDB.getFirst(
		"Direction").getDefaultUnitString() + "\"");
    }
}
