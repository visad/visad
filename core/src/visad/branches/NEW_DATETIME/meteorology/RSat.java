/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: RSat.java,v 1.2 1999-01-07 16:13:18 steve Exp $
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
 * Supports saturation mixing-ratios.
 *
 * Instances are immutable.
 *
 * @author Steven R. Emmerson
 */
public class
RSat
{
    /**
     * The ratio of the water vapor and dry air gas constants.
     */
    private static final double	ratio = 0.622;


    /**
     * Computes saturation mixing-ratios from pressures and temperatures.
     *
     * @param pressures		Pressures in units of <code>pressureUnit()
     *				</code>.
     * @param pressureUnit	The unit of pressure.
     * @param temperatures	Temperatures in units of <code>
     *				temperatureUnit()</code>.  <code>
     *				temperatures.length</code> must equal
     *				<code>pressures.length</code>.
     * @param temperatureUnit	The unit of temperature.
     * @param rSatUnit		The unit of saturation mixing-ratio.  Must be
     *				dimensionless but may be scaled.
     * @return			Corresponding saturation mixing-ratios in units 
     *				of <code>getRSatUnit()</code>.
     *				RETURN_VALUE<code>.length</code> will equal
     *				<code>pressures.length</code>.
     * @throws UnitException	Inappropriate unit argument.
     */
    public static double[]
    rSat(double[] pressures, Unit pressureUnit, double[] temperatures,
	    Unit temperatureUnit, Unit rSatUnit)
	throws UnitException
    {
	double[]	eSats =
	    ESat.eSat(temperatures, temperatureUnit, pressureUnit);
	double[]	rSats = new double[pressures.length];
	double		rSat0 =
	    rSatUnit.toThis(ratio, CommonUnit.dimensionless);

	for (int i = 0; i < pressures.length; ++i)
	{
	    double	eSat = eSats[i];
	    rSats[i] = rSat0 * (eSat/(pressures[i] - eSat));
	}

	return rSats;
    }


    /**
     * Computes temperatures from pressures and saturation mixing-ratios.
     *
     * @param pressures		Pressures in units of <code>pressureUnit()
     *				</code>.
     * @param pressureUnit	The unit of pressure.
     * @param rSats		Saturation mixing-ratios in units of <code>
     *				RSatUnit()</code>.  <code>
     *				rSats.length</code> must equal
     *				<code>pressures.length</code>.
     * @param temperatureUnit	The unit of temperature.
     * @return			Corresponding temperatures in units 
     *				of <code>temperatureUnit()</code>.
     *				RETURN_VALUE<code>.length</code> will equal
     *				<code>pressures.length</code>.
     * @throws UnitException	Inappropriate unit argument.
     */
    public static double[]
    temperature(double[] pressures, Unit pressureUnit, double[] rSats,
	    Unit rSatUnit, Unit temperatureUnit)
	throws UnitException
    {
	double[]	eSats = new double[pressures.length];
	double		rSat0 =
	    rSatUnit.toThis(ratio, CommonUnit.dimensionless);

	for (int i = 0; i < pressures.length; ++i)
	{
	    double	rSat = rSats[i];
	    eSats[i] = (rSat * pressures[i]) / (rSat0 + rSat);
	}

	return ESat.temperature(eSats, pressureUnit, temperatureUnit);
    }
}
