/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ESat.java,v 1.1 1999-01-07 16:13:17 steve Exp $
 */

package visad.meteorology;

import visad.CommonUnit;
import visad.DerivedUnit;
import visad.SI;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Supports saturation water vapor pressure.
 *
 * Instances are immutable.
 *
 * @author Steven R. Emmerson
 */
public class
ESat
{
    /**
     * Computes saturation water vapor pressures from temperatures.
     * The algorithm is based on Bolton's 1980 variation of Teten's 1930 
     * formula for saturation vapor pressure (see "An  Introduction to 
     * Boundary Layer Meteorology" by Roland B. Stull (1988) equation 7.5.2d).
     *
     * @param temperatures	Temperatures in units of <code>
     *				temperatureUnit()</code>.
     * @param temperatureUnit	The unit of temperature.
     * @param pressureUnit	The unit of saturation water vapor pressure.
     * @return			Corresponding saturation water vapor pressures
     *				in units of <code>pressureUnit()</code>.
     *				RETURN_VALUE<code>.length</code> will equal
     *				<code>temperatures.length</code>.
     * @throws UnitException	Inappropriate unit argument.
     */
    public static double[]
    eSat(double[] temperatures, Unit temperatureUnit, Unit pressureUnit)
	throws UnitException
    {
	double[]	eSats = new double[temperatures.length];
	double		eSat0 = pressureUnit.toThis(611.2, CommonUnits.PASCAL);

	double[]	kelvins =
	    SI.kelvin.toThis(temperatures, temperatureUnit);

	for (int i = 0; i < temperatures.length; ++i)
	{
	    double	kelvin = kelvins[i];
	    eSats[i] = eSat0 * Math.exp(17.67*(kelvin - 273.16) /
		(kelvin - 29.66));
	}

	return eSats;
    }


    /**
     * Computes temperatures from saturation water vapor pressures.
     * The algorithm is based on Bolton's 1980 variation of Teten's 1930 
     * formula for saturation vapor pressure (see "An  Introduction to 
     * Boundary Layer Meteorology" by Roland B. Stull (1988) equation 7.5.2d).
     *
     * @param eSats		Saturation water vapor pressures in units of
     *				<code>pressureUnit()</code>.
     * @param pressureUnit	The unit of saturation water vapor pressure.
     * @param temperatureUnit	The unit of temperature.
     * @return			Corresponding temperatures in units 
     *				of <code>temperatureUnit()</code>.
     *				RETURN_VALUE<code>.length</code> will equal
     *				<code>eSats.length</code>.
     * @throws UnitException	Inappropriate unit argument.
     */
    public static double[]
    temperature(double[] eSats, Unit pressureUnit, Unit temperatureUnit)
	throws UnitException
    {
	double[]	temperatures = new double[eSats.length];
	double		eSat0 = pressureUnit.toThis(611.2, CommonUnits.PASCAL);

	for (int i = 0; i < eSats.length; ++i)
	{
	    double	log = Math.log(eSats[i]/eSat0);
	    temperatures[i] = (29.66 * log - 17.67 * 273.16) / (log - 17.67);
	}

	return temperatureUnit.toThis(temperatures, SI.kelvin);
    }
}
