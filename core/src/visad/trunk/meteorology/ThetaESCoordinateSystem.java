/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ThetaESCoordinateSystem.java,v 1.1 1998-08-24 18:24:22 steve Exp $
 */

package visad.meteorology;

import visad.SI;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.Parser;
import visad.data.netcdf.units.ParseException;


/**
 * Supports conversion between the spaces of (Display.XAxis, Display.YAxis)
 * and (pressure, saturation equivalent potential temperature).
 *
 * An instance of this class is immutable.
 *
 * Definitions:
 *	Real Coordinates	(pressure,
 *				saturation equivalent potential temperature)
 *	Display Coordinates	(Display.XAxis, Display.YAxis)
 *
 * @author Steven R. Emmerson
 */
public class
ThetaESCoordinateSystem
    extends	ThetaCoordinateSystem
{
    /**
     * Reference pressure for empirical computation of saturation water vapor
     * pressure in units of getPressureUnit().
     */
    private final double	eSat0;


    /**
     * Constructs from a potential temperature coordinate system.
     *
     * @param thetaCoordSys	Potential temperature cooordinate system.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    ThetaESCoordinateSystem(ThetaCoordinateSystem thetaCoordSys)
	throws VisADException, ParseException
    {
	super(thetaCoordSys);

	eSat0 =
	    getPressureUnit().toThis(0.61078, Parser.instance().parse("kPa"));
    }


    /**
     * Convert saturation equivalent potential temperature coordinates
     * to pressure and temperature coordinates.
     */
    protected double[][]
    convert(double[][] coords)
    {
	throw new UnsupportedOperationException(
	"Can't convert equivalent potential temperature to temperature -- yet");
    }


    /**
     * Convert pressure and temperature coordinates to saturation equivalent 
     * potential temperature coordinates.
     *
     * @exception UnitException	Incompatible Units.
     */
    protected double[][]
    invert(double[][] coords)
	throws UnitException
    {
	double[]	pressures = coords[0];
	double[]	temperatures = 
	    getTemperatureUnit().toThat(coords[1], SI.kelvin);

	for (int i = 0; i < pressures.length; ++i)
	{
	    double	pressure = pressures[i];
	    double	temperature = temperatures[i];
	    double	exponent = (17.2694*(temperature - 273.16)) /
		(temperature - 35.86);
	    double	eSat = eSat0 * Math.exp(exponent);
	    double	rSat = 0.622 * (eSat/(pressure - eSat));
	    double	theta = theta(pressure, temperature);

	    temperatures[i] = theta * (1.0 + 2500.0 * rSat / temperature);
	}

	coords[1] = getTemperatureUnit().toThis(temperatures, SI.kelvin);

	return coords;
    }


    /*
     * Indicate whether or not this coordinate system is the same as another.
     *
     * @param obj	The object to be compared with this one.
     * @return		<code>true</code> if and only if <code>obj</code> is
     *			semantically identical to this object.
     */
    public boolean
    equals(java.lang.Object obj)
    {
	if (!(obj instanceof ThetaESCoordinateSystem))
	    return false;

	return eSat0 == ((ThetaESCoordinateSystem)obj).eSat0 &&
	    super.equals(obj);
    }
}
