/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: MetQuantityDB.java,v 1.4 1999-01-20 18:06:57 steve Exp $
 */

package visad.meteorology;

import visad.TypeException;
import visad.VisADException;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.QuantityDBManager;
import visad.data.netcdf.units.ParseException;


/**
 * Provides support for mapping meteorological quantities to VisAD Quantities.
 */
public final class
MetQuantityDB
{
    /**
     * Whether or not this class has been initialized.
     */
    private static boolean	initialized = false;

    /**
     * Quantity definitions.
     */
    static final String[]	definitions =
	new String[]
	{
	    "DewPoint", "Cel",
	    "PotentialTemperature", "K",
	    "SaturationEquivalentPotentialTemperature", "K",
	    "SaturationMixingRatio", "g/kg",
	    "U", "m/s",
	    "V", "m/s",
	    "W", "m/s",
	    "VirtualTemperature", "K",
	};

    /**
     * Quantity aliases.
     */
    static final String[]	aliases =
	new String[]
	{
	    "PressureReducedToMSL", "Pressure",
	    "SurfacePressure", "Pressure",
	    "Theta", "PotentialTemperature",
	    "ThetaES", "SaturationEquivalentPotentialTemperature",
	    "Rsat", "SaturationMixingRatio",
	};


    /**
     * Constructs from nothing.  Private to prevent instantiation.
     */
    private
    MetQuantityDB()
    {
    }


    /**
     * Initializes the quantity database for meteorology.  Idempotent.
     * @throws ParseException	Couldn't decode unit specification.
     * @throws TypeException	Incompatible Quantity already exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public static void
    initialize()
	throws ParseException, TypeException, VisADException
    {
	if (!initialized)
	{
	    QuantityDBManager.setInstance(
		QuantityDBManager.instance().add(definitions, aliases));
	    initialized = true;
	}
    }


    /**
     * Tests this class.  If the only argument is "list", then this method
     * will print the list of meteorology quantities.
     */
    public static void
    main(String[] args)
	throws	Exception
    {
	MetQuantityDB.initialize();

	if (args.length == 1 && args[0].equals("list"))
	{
	    for (int i = 0; i < definitions.length; i += 2)
	    {
		System.out.println(
		    definitions[i] + " (" + definitions[i] + ") in " +
		    definitions[i+1]);
	    }
	    QuantityDB	db = QuantityDBManager.instance();
	    for (int i = 0; i < aliases.length; i += 2)
	    {
		Quantity	quantity = db.get(aliases[i+1]);
		System.out.println(
		    aliases[i] + " (" +  quantity.getName() + 
		    ") in " + quantity.getDefaultUnitString());
	    }
	}
	else
	{

	    QuantityDB	db = QuantityDBManager.instance();

	    System.out.println(
		"db.get(\"pressure\").getDefaultUnitString()=\"" +
		db.get("pressure").getDefaultUnitString() + "\"");
	    System.out.println(
		"db.get(\"pressure\").getDefaultUnitString()=\"" +
		db.get("pressure").getDefaultUnitString() + "\"");
	    System.out.println(
		"db.get(\"U\").getDefaultUnitString()=\"" +
		db.get("U").getDefaultUnitString() + "\"");
	    System.out.println(
		"db.get(\"V\").getDefaultUnitString()=\"" +
		db.get("V").getDefaultUnitString() + "\"");
	    System.out.println(
		"db.get(\"Speed\").getDefaultUnitString()=\"" +
		db.get("Speed").getDefaultUnitString() + "\"");
	    System.out.println(
		"db.get(\"Direction\").getDefaultUnitString()=\"" +
		db.get("Direction").getDefaultUnitString() + "\"");
	}
    }
}
