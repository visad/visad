/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: RSat.java,v 1.1 1998-10-21 15:27:57 steve Exp $
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
     * Reference pressure for empirical computation of saturation water vapor
     * pressure in units of getPressureUnit().
     */
    private final double	eSat0;

    /**
     * Reference saturation mixing-ratio for empirical computation of
     * saturation mixing-ratio in units of getRSatUnit().
     */
    private final double	rSat0;

    /**
     * The unit of pressure.
     */
    private final Unit		pressureUnit;

    /**
     * The unit of temperature.
     */
    private final Unit		temperatureUnit;

    /**
     * The unit of saturation mixing-ratio.
     */
    private final Unit		rSatUnit;


    /**
     * Constructs from units for pressure, temperature, and saturation
     * mixing-ratio.
     *
     * @param pressureUnit	The unit of pressure to assume.
     * @param temperatureUnit	The unit of temperature to assume.
     * @param rSatUnit		The dimensionless unit of saturation mixing
     *				ratio to assume.
     * @throws UnitException	<code>pressureUnit</code> is not a unit of
     *				pressure or <code>temperatureUnit</code> is
     *				not a unit of temperature or <code>
     *				rSatUnit</code> is not a dimensionless unit.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    RSat(Unit pressureUnit, Unit temperatureUnit, Unit rSatUnit)
	throws UnitException, VisADException
    {
	Unit	kiloPascals;
	try
	{
	    kiloPascals = Parser.parse("kPa");
	}
	catch (ParseException e)
	{
	    throw new VisADException(e.getMessage());	// shouldn't happen
	}

	if (!Unit.canConvert(pressureUnit, kiloPascals) ||
	    !Unit.canConvert(temperatureUnit, SI.kelvin) ||
	    !Unit.canConvert(rSatUnit, CommonUnit.dimensionless))
	{
	    throw new UnitException("Incompatible units");
	}

	this.pressureUnit = pressureUnit;
	this.temperatureUnit = temperatureUnit;
	this.rSatUnit = rSatUnit;

	rSat0 = rSatUnit.toThis(0.622, CommonUnit.dimensionless);
	eSat0 = pressureUnit.toThis(0.61078, kiloPascals);
    }


    /**
     * Gets the assumed unit of pressure.
     *
     * @return			The assumed unit of pressure.
     */
    public Unit
    getPressureUnit()
    {
	return pressureUnit;
    }


    /**
     * Gets the assumed unit of temperature.
     *
     * @return			The assumed unit of temperature.
     */
    public Unit
    getTemperatureUnit()
    {
	return temperatureUnit;
    }


    /**
     * Gets the assumed unit of saturation mixing-ratio.
     *
     * @return			The assumed unit of saturation mixing-ratio.
     */
    public Unit
    getRSatUnit()
    {
	return rSatUnit;
    }


    /**
     * Computes saturation mixing-ratios from pressures and temperatures.
     *
     * @param pressures		Pressures in units of <code>getPressureUnit()
     *				</code>.
     * @param temperatures	Temperatures in units of <code>
     *				getTemperatureUnit()</code>.  <code>
     *				temperatures.length</code> must equal
     *				<code>pressures.length</code>.
     * @return			Corresponding saturation mixing-ratios in units 
     *				of <code>getRSatUnit()</code>.
     *				RETURN_VALUE<code>.length</code> will equal
     *				<code>pressures.length</code>.
     * @throws UnitException	<code>getTemperatureUnit()<code> is not a
     *				unit of temperature.  This should never be
     *				thrown.
     */
    public double[]
    rSat(double[] pressures, double[] temperatures)
	throws UnitException
    {
	double[]	kelvins =
	    SI.kelvin.toThis(temperatures, temperatureUnit);
	double[]	rSats = new double[pressures.length];

	for (int i = 0; i < pressures.length; ++i)
	{
	    double	kelvin = kelvins[i];
	    double	exponent =
		(17.2694*(kelvin - 273.16)) / (kelvin - 35.86);
	    double	eSat = eSat0 * Math.exp(exponent);

	    rSats[i] = rSat0 * (eSat/(pressures[i] - eSat));
	}

	return rSats;
    }


    /**
     * Computes saturation mixing-ratios from pressures and temperatures.
     *
     * @param pressures		Pressures in units of <code>getPressureUnit()
     *				</code>.
     * @param rSats		Saturation mixing-ratios in units of <code>
     *				getRSatUnit()</code>.  <code>
     *				rSats.length</code> must equal
     *				<code>pressures.length</code>.
     * @return			Corresponding temperatures in units 
     *				of <code>getTemperatureUnit()</code>.
     *				RETURN_VALUE<code>.length</code> will equal
     *				<code>pressures.length</code>.
     * @throws UnitException	<code>getTemperatureUnit()<code> is not a
     *				unit of temperature.  This should never be
     *				thrown.
     */
    public double[]
    temperature(double[] pressures, double[] rSats)
	throws UnitException
    {
	double		beta = Math.log(eSat0);
	double[]	temperatures = new double[pressures.length];

	for (int i = 0; i < pressures.length; ++i)
	{
	    double	alpha =
		Math.log(rSats[i]*pressures[i]/(rSat0 + rSats[i]));
	    double	mu = alpha - beta;

	    temperatures[i] = (mu*35.86 - 273.16) / (mu - 17.2694);
	}

	temperatures = temperatureUnit.toThis(temperatures, SI.kelvin);

	return temperatures;
    }


    /**
     * Indicates if this instance semantically equals an object.
     *
     * @param object		An object.
     * @return			<code>true</code> if and only if this instance
     *				is semantially identical to <code>object</code>.
     */
    public boolean
    equals(Object object)
    {
	if (!(object instanceof RSat))
	    return false;

	RSat	that = (RSat)object;

	return pressureUnit.equals(that.pressureUnit) &&
	       temperatureUnit.equals(that.temperatureUnit) &&
	       rSatUnit.equals(that.rSatUnit);
    }
}
