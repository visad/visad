/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CommonUnits.java,v 1.3 1999-01-07 16:13:16 steve Exp $
 */

package visad.meteorology;

import visad.CommonUnit;
import visad.DerivedUnit;
import visad.SI;
import visad.ScaledUnit;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for units common to meteorology.
 */
public class
CommonUnits
{
    public static final Unit	PASCAL;
    public static final Unit	HECTOPASCAL;
    public static final Unit	MILLIBAR;
    public static final Unit	CELSIUS;
    public static final Unit	GRAMS_PER_KILOGRAM;
    public static final Unit	METERS_PER_SECOND;
    public static final Unit	HOUR;
    public static final Unit	NAUTICAL_MILE;
    public static final Unit	KNOT;
    public static final Unit	DEGREE;
    
    static
    {
	Unit	pascal = null;
	Unit	millibar = null;
	Unit	celsius = null;
	Unit	gPerKg = null;
	Unit	metersPerSecond = null;
	Unit	nauticalMile = null;
        Unit	knot = null;
        Unit	hour = null;
        
	try
	{
	    pascal = SI.kilogram.divide(SI.meter).divide(SI.second.pow(2));
	    millibar = new ScaledUnit(100, (DerivedUnit)pascal);
	    celsius = SI.kelvin.shift(273.15);
	    gPerKg = new ScaledUnit(0.001);
	    metersPerSecond = SI.meter.divide(SI.second);
	    nauticalMile = new ScaledUnit(1.852e3, SI.meter);
	    hour = new ScaledUnit(3600.0, SI.second);
	    knot = nauticalMile.divide(hour);
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println("Couldn't initialize CommonUnits class" +
		(reason == null ? "" : ": " + reason));
	    e.printStackTrace();
	}

	PASCAL = pascal;
	HECTOPASCAL = millibar;
	MILLIBAR = millibar;
	CELSIUS = celsius;
	GRAMS_PER_KILOGRAM = gPerKg;
	METERS_PER_SECOND = metersPerSecond;
	HOUR = hour;
	NAUTICAL_MILE = nauticalMile;
	KNOT = knot;
	DEGREE = CommonUnit.degree;
    }
}


