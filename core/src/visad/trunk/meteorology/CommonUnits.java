/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CommonUnits.java,v 1.1 1998-10-21 15:27:57 steve Exp $
 */

package visad.meteorology;

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
    public static final Unit	MILLIBAR;
    public static final Unit	CELSIUS;
    public static final Unit	G_PER_KG;
    public static final Unit	METERS_PER_SECOND;

    static
    {
	Unit	pascal = null;
	Unit	millibar = null;
	Unit	celsius = null;
	Unit	gPerKg = null;
	Unit	metersPerSecond = null;

	try
	{
	    pascal = SI.kilogram.divide(SI.meter).divide(SI.second.pow(2));
	    millibar = new ScaledUnit(100, (DerivedUnit)pascal);
	    celsius = SI.kelvin.shift(273.15);
	    gPerKg = new ScaledUnit(0.001);
	    metersPerSecond = SI.meter.divide(SI.second);
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println("Couldn't initialize CommonUnits class" +
		(reason == null ? "" : ": " + reason));
	    e.printStackTrace();
	}

	PASCAL = pascal;
	MILLIBAR = millibar;
	CELSIUS = celsius;
	G_PER_KG = gPerKg;
	METERS_PER_SECOND = metersPerSecond;
    }
}
