/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Theta.java,v 1.2 1998-10-21 15:27:59 steve Exp $
 */

package visad.meteorology;

import visad.SI;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.Parser;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.NoSuchUnitException;


/**
 * Supports conversion between potential temperature and pressure and
 * temperature.
 *
 * An instance of this class is immutable.
 *
 * @author Steven R. Emmerson
 */
public class
Theta
{
    /**
     * The reference pressure.
     */
    private final double	referencePressure;

    /**
     * The assumed pressure unit.
     */
    private final Unit		pressureUnit;

    /**
     * The assumed temperature unit.
     */
    private final Unit		temperatureUnit;

    /**
     * The assumed potential temperature unit.
     */
    private final Unit		thetaUnit;

    /*
     * The following value is take from "An Introduction to Boundary
     * Layer Meteorology" by Roland B. Stull; chapter 13 (Boundary Layer
     * Clouds).
     */
    private static final float	kappa = 0.286f;


    /**
     * Constructs from units for pressure, temperature, and potential
     * temperature.
     *
     * @param pressureUnit	Unit of pressure to assume.
     * @param temperatureUnit	Unit of temperature to assume.
     * @param thetaUnit		Unit of potential temperature to assume.
     * @postcondition		<code>getPressureUnit()</code> will return
     *				<pressureUnit>.
     * @postcondition		<code>getTemperatureUnit()</code> will return
     *				<temperatureUnit>.
     * @throws UnitException	<code>pressureUnit</code> is not a unit of
     *				pressure or <code>temperatureUnit</code> is
     *				not a unit of temperature or <code>
     *				thetaUnit</code> is not a unit of temperature..
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws ParseException	Couldn't decode unit specification.  This
     *				Should never be throw.
     * @throws UnitException	Input units are not correct for their type.
     */
    public
    Theta(Unit pressureUnit, Unit temperatureUnit, Unit thetaUnit)
	throws UnitException, VisADException
    {
	Unit	millibar = null;

	try
	{
	    millibar = Parser.parse("millibar");
	}
	catch (ParseException e)
	{}

	if (!Unit.canConvert(pressureUnit, millibar) ||
	    !Unit.canConvert(temperatureUnit, SI.kelvin) ||
	    !Unit.canConvert(thetaUnit, SI.kelvin))
	{
	    throw new UnitException("Incompatible units");
	}

	this.pressureUnit = pressureUnit;
	this.temperatureUnit = temperatureUnit;
	this.thetaUnit = thetaUnit;

	referencePressure = pressureUnit.toThis(1000.0, millibar);
    }


    /**
     * Computes potential temperatures from pressures and temperatures.
     *
     * @param pressures		Pressures in units of 
     *				<code>getPressureUnit()</code>.
     * @param temperatures	Temperatures in units of 
     *				<code>getTemperatureUnit()</code>.
     *				<code>temperatures.length</code> must equal
     * 				<code>pressures.length</code>.
     * @return			Corresponding potential temperatures in units
     *				of <code>getThetaUnit()</code>.
     * @exception UnitException	Non-convertible units.  Shouldn't happen.
     * @exception VisADException	<code>pressures.length != 
     *					temperatures.length</code>.
     */
    public double[]
    theta(double[] pressures, double[] temperatures)
	throws VisADException
    {
	if (pressures.length != temperatures.length)
	    throw new VisADException("pressures.length != temperatures.length");

	temperatures = SI.kelvin.toThis(temperatures, temperatureUnit);

	for (int i = 0; i < pressures.length; ++i)
	    temperatures[i] *= Math.pow(referencePressure/pressures[i], kappa);

	return thetaUnit.toThis(temperatures, SI.kelvin);
    }


    /**
     * Computes temperatures from pressures and potential temperatures.
     *
     * @param pressures		Pressures in units of 
     *				<code>getPressureUnit()</code>.
     * @param thetas		Potential temperatures in units of 
     *				<code>getThetaUnit()</code>.
     * @precondition		<code>pressures.length ==
     *				temperatures.length</code>
     * @precondition		<code>presures.length == thetas.length</code>
     * @return			Corresponding temperatures
     *				in units of </code>getTemperatureUnit()</code>.
     * @exception UnitException	Non-convertible units.  Shouldn't happen.
     * @exception VisADException	<code>pressures.length != 
     *					thetas.length</code>.
     */
    public double[]
    temperature(double[] pressures, double[] thetas)
	throws VisADException
    {
	if (pressures.length != thetas.length)
	    throw new VisADException("pressures.length != thetas.length");

	thetas = thetaUnit.toThat(thetas, SI.kelvin);

	for (int i = 0; i < pressures.length; ++i)
	    thetas[i] /= Math.pow(referencePressure/pressures[i], kappa);

	return temperatureUnit.toThis(thetas, SI.kelvin);
    }


    /**
     * Gets the assumed pressure unit.
     *
     * @return			The assumed unit of pressure.
     */
    public Unit
    getPressureUnit()
    {
	return pressureUnit;
    }


    /**
     * Gets the assumed temperature unit.
     *
     * @return			The assumed unit of temperature.
     */
    public Unit
    getTemperatureUnit()
    {
	return temperatureUnit;
    }


    /**
     * Gets the assumed potential temperature unit.
     *
     * @return			The assumed unit of potential temperature.
     */
    public Unit
    getThetaUnit()
    {
	return thetaUnit;
    }


    /**
     * Indicates if this instance semantically equals an object.
     *
     * @param object		An object.
     */
    public boolean
    equals(Object object)
    {
	if (!(object instanceof Theta))
	    return false;

	Theta	that = (Theta)object;

	return
	    pressureUnit.equals(that.pressureUnit) &&
	    temperatureUnit.equals(that.temperatureUnit) &&
	    thetaUnit.equals(that.thetaUnit);
    }
}
