/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Theta.java,v 1.1 1998-08-28 16:50:25 steve Exp $
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
 * Supports conversions between pressure and temperature and
 * potential temperature.
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
     * The pressure unit.
     */
    private final Unit		pressureUnit;

    /**
     * The temperature unit.
     */
    private final Unit		temperatureUnit;

    /*
     * The following value is take from "An Introduction to Boundary
     * Layer Meteorology" by Roland B. Stull; chapter 13 (Boundary Layer
     * Clouds).
     */
    private static final float	kappa = 0.286f;


    /**
     * Constructs from pressure and temperature units.
     *
     * @param pressureUnit	Unit of pressure.
     * @param temperatureUnit	Unit of temperature.
     * @postcondition		<code>getPressureUnit()</code> will return
     *				<pressureUnit>.
     * @postcondition		<code>getTemperatureUnit()</code> will return
     *				<temperatureUnit>.
     * @exception UnitException	Invalid pressure or temperature unit.
     */
    public
    Theta(Unit pressureUnit, Unit temperatureUnit)
	throws UnitException, VisADException
    {
	this.pressureUnit = pressureUnit;
	this.temperatureUnit = temperatureUnit;

	try
	{
	    Unit	millibar = Parser.instance().parse("millibar");

	    referencePressure = getPressureUnit().toThis(1000.0, millibar);
	}
	catch (ParseException e)
	{
	    throw new VisADException(e.getMessage());
	}
    }


    /**
     * Computes potential temperatures from pressures and temperatures.
     *
     * @param pressures		Input Pressure values in units of 
     *				<code>getPressureUnit()</code>.  The array is 
     *				not modified.
     * @param temperatures	Input Temperature values in units of 
     *				<code>getTemperatureUnit()</code>.  The array
     *				is not modified.
     * @precondition		<code>pressures.length ==
     *				temperatures.length</code>
     * @precondition		<code>temperatures.length ==
     *				pressures.length</code>
     * @return			An array of length <code>pressures.length</code>
     *				containing the corresponding potential
     *				temperature values in units of 
     *				</code>getTemperatureUnit()</code>.
     * @exception UnitException	Non-convertible units.  Shouldn't happen.
     * @exception VisADException	<code>pressures.length != 
     *				temperatures.length</code>.
     */
    public double[]
    toTheta(double[] pressures, double[] temperatures)
	throws VisADException
    {
	if (pressures.length != temperatures.length)
	    throw new VisADException("pressures.length != temperatures.length");

	temperatures = getTemperatureUnit().toThat(temperatures, SI.kelvin);

	for (int i = 0; i < pressures.length; ++i)
	    temperatures[i] *=
		Math.pow(referencePressure/pressures[i], kappa);

	return getTemperatureUnit().toThis(temperatures, SI.kelvin);
    }


    /**
     * Computes potential temperature from pressure and temperature.
     *
     * @param pressure		The pressure in units of 
     *				<code>getPressureUnit()</code>.
     * @param temperatures	The temperature in units of 
     *				<code>getTemperatureUnit()</code>.
     */
    public double
    theta(double pressure, double temperature)
	throws VisADException
    {
	return toTheta(new double[] {pressure}, new double[] {temperature})[0];
    }


    /**
     * Computes temperatures from pressures and potential temperatures.
     *
     * @param pressures		Input Pressure values in units of 
     *				<code>getPressureUnit()</code>.  The array is 
     *				not modified.
     * @param temperatures	Input Temperature values in units of 
     *				<code>getTemperatureUnit()</code>.  The array
     *				is not modified.
     * @precondition		<code>pressures.length ==
     *				temperatures.length</code>
     * @precondition		<code>presures.length == thetas.length</code>
     * @return			An array of length <code>pressures.length</code>
     *				containing the corresponding temperature values
     *				in units of </code>getTemperatureUnit()</code>.
     * @exception UnitException	Non-convertible units.  Shouldn't happen.
     * @exception VisADException	<code>pressures.length != 
     *				thetas.length</code>.
     */
    public double[]
    temperature(double[] pressures, double[] thetas)
	throws VisADException
    {
	if (pressures.length != thetas.length)
	    throw new VisADException("pressures.length != thetas.length");

	thetas = getTemperatureUnit().toThat(thetas, SI.kelvin);

	for (int i = 0; i < pressures.length; ++i)
	    thetas[i] /= Math.pow(referencePressure/pressures[i], kappa);

	return getTemperatureUnit().toThis(thetas, SI.kelvin);
    }


    /**
     * Computes temperature from pressure and potential temperature.
     *
     * @param pressure		The pressure in units of 
     *				<code>getPressureUnit()</code>.
     * @param theta		The potential temperature in units of 
     *				<code>getTemperatureUnit()</code>.
     */
    public double
    temperature(double pressure, double theta)
	throws VisADException
    {
	return temperature(new double[] {pressure}, new double[] {theta})[0];
    }


    /**
     * Gets the pressure unit.
     *
     * @return	The unit of pressure
     */
    public Unit
    getPressureUnit()
    {
	return pressureUnit;
    }


    /**
     * Gets the temperature unit.
     *
     * @return	The unit of temperature
     */
    public Unit
    getTemperatureUnit()
    {
	return temperatureUnit;
    }


    /**
     * Indicates whether or not this potential temperature utility
     * is semantically idential to another.
     *
     * @param obj	The other potential temperature utility
     */
    public boolean
    equals(Object obj)
    {
	if (!(obj instanceof Theta))
	    return false;

	Theta	other = (Theta)obj;

	return
	    referencePressure == other.referencePressure &&
	    pressureUnit == other.pressureUnit &&
	    temperatureUnit == other.temperatureUnit;
    }
}
