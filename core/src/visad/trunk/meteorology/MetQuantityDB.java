/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: MetQuantityDB.java,v 1.1 1998-08-12 17:17:20 visad Exp $
 */

package visad.meteorology;

import visad.VisADException;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.QuantityMap;
import visad.data.netcdf.units.ParseException;


/**
 * Provides for the mapping of meteorological quantities to VisAD Quantities.
 */
public class
MetQuantityDB
    extends	QuantityDB
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
	try
	{
	    add("pressure", "millibars");
	    add("pressure reduced to MSL", "millibars");
	    add("temperature", "celsius");

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
	QuantityMap.push(MetQuantityDB.instance());
	System.out.println("pressure unit = " + 
	    QuantityMap.getFirst("pressure").getDefaultUnit());
    }
}
