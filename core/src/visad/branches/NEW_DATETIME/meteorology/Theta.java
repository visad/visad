/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Theta.java,v 1.3 1999-01-07 16:13:20 steve Exp $
 */

package visad.meteorology;

import visad.SI;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;


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
    /*
     * The following value is take from "An Introduction to Boundary
     * Layer Meteorology" by Roland B. Stull; chapter 13 (Boundary Layer
     * Clouds).
     */
    private static final float	kappa = 0.286f;


    /**
     * Computes potential temperatures from pressures and temperatures.
     *
     * @param pressures		Pressures in units of 
     *				<code>pressureUnit()</code>.
     * @param pressureUnit	The unit of pressure.
     * @param temperatures	Temperatures in units of 
     *				<code>temperatureUnit()</code>.
     *				<code>temperatures.length</code> must equal
     * 				<code>pressures.length</code>.
     * @param temperatureUnit	The unit of temperature.
     * @param thetaUnit		The unit of potential temperature.
     * @return			Corresponding potential temperatures in units
     *				of <code>thetaUnit()</code>.
     * @exception UnitException	One of the unit arguments isn't appropriate.
     * @exception VisADException	<code>pressures.length != 
     *					temperatures.length</code>.
     */
    public static double[]
    theta(double[] pressures, Unit pressureUnit, double[] temperatures,
	    Unit temperatureUnit, Unit thetaUnit)
	throws UnitException, VisADException
    {
	if (pressures.length != temperatures.length)
	    throw new VisADException(
		"pressures.length != temperatures.length");

	double	referencePressure =
	    pressureUnit.toThis(1000, CommonUnits.MILLIBAR);
	temperatures = SI.kelvin.toThis(temperatures, temperatureUnit);

	for (int i = 0; i < pressures.length; ++i)
	    temperatures[i] *= Math.pow(referencePressure/pressures[i], kappa);

	return thetaUnit.toThis(temperatures, SI.kelvin);
    }


    /**
     * Computes temperatures from pressures and potential temperatures.
     *
     * @param pressures		Pressures in units of 
     *				<code>pressureUnit()</code>.
     * @param pressureUnit	The unit of pressure.
     * @param thetas		Potential temperatures in units of 
     *				<code>thetaUnit()</code>.
     * @param thetaUnit		The unit of potential temperature.
     * @param temperatureUnit	The unit of temperature.
     * @precondition		<code>pressures.length ==
     *				temperatures.length</code>
     * @precondition		<code>presures.length == thetas.length</code>
     * @return			Corresponding temperatures
     *				in units of </code>temperatureUnit()</code>.
     * @exception UnitException	A unit argument isn't appropriate.
     * @exception VisADException	<code>pressures.length != 
     *					thetas.length</code>.
     */
    public static double[]
    temperature(double[] pressures, Unit pressureUnit, double[] thetas,
	    Unit thetaUnit, Unit temperatureUnit)
	throws VisADException
    {
	if (pressures.length != thetas.length)
	    throw new VisADException("pressures.length != thetas.length");

	double	referencePressure =
	    pressureUnit.toThis(1000, CommonUnits.MILLIBAR);
	thetas = SI.kelvin.toThis(thetas, thetaUnit);

	for (int i = 0; i < pressures.length; ++i)
	    thetas[i] /= Math.pow(referencePressure/pressures[i], kappa);

	return temperatureUnit.toThis(thetas, SI.kelvin);
    }
}
