/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: MetQuantityDB.java,v 1.3 1999-01-07 16:13:18 steve Exp $
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

	append(metQuantityDB);
	append(StandardQuantityDB.instance());

	try
	{
	    metQuantityDB.add("PressureReducedToMSL", get("Pressure"));
	    metQuantityDB.add("DewPoint", "Cel");
	    metQuantityDB.add("Theta", "K");
	    metQuantityDB.add("ThetaES", "K");
	    metQuantityDB.add("Rsat", "g/kg");
	    metQuantityDB.add("U", "m/s");
	    metQuantityDB.add("V", "m/s");
	    metQuantityDB.add("W", "m/s");
	    metQuantityDB.add("VirtualTemperature", "K");

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
	    standardQuantityDB.get("pressure").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"pressure\").getDefaultUnitString()=\"" +
	    metQuantityDB.get("pressure").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"U\").getDefaultUnitString()=\"" +
	    metQuantityDB.get("U").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"V\").getDefaultUnitString()=\"" +
	    metQuantityDB.get("V").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"Speed\").getDefaultUnitString()=\"" +
	    metQuantityDB.get("Speed").getDefaultUnitString() + "\"");
	System.out.println(
    "metQuantityDB.getFirst(\"Direction\").getDefaultUnitString()=\"" +
	    metQuantityDB.get("Direction").getDefaultUnitString() + "\"");
    }
}
