/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ThetaESCoordinateSystem.java,v 1.2 1998-08-28 16:50:25 steve Exp $
 */

package visad.meteorology;

import visad.CoordinateSystem;
import visad.Display;
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
 *	Display Coordinates	(Display.XAxis, Display.YAxis)
 *
 * @author Steven R. Emmerson
 */
public class
ThetaESCoordinateSystem
    extends	CoordinateSystem
{
    /**
     * Reference pressure for empirical computation of saturation water vapor
     * pressure in units of getPressureUnit().
     */
    private final double		eSat0;

    /**
     * The associated potential temperature coordinate system.
     */
    private final ThetaCoordinateSystem	thetaCoordSys;


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
	super(Display.DisplaySpatialCartesianTuple, 
	    thetaCoordSys.getCoordinateSystemUnits());

	this.thetaCoordSys = thetaCoordSys;

	eSat0 =
	    getPressureUnit().toThis(0.61078, Parser.instance().parse("kPa"));
    }


    /**
     * Gets the pressure unit.
     *
     * @return	The unit of pressure.
     */
    public Unit
    getPressureUnit()
    {
	return thetaCoordSys.getPressureUnit();
    }


    /**
     * Gets the temperature unit.
     *
     * @return	The unit of temperature.
     */
    public Unit
    getTemperatureUnit()
    {
	return thetaCoordSys.getTemperatureUnit();
    }


    /**
     * Transforms display coordinates to saturation equivalent potential
     * temperature coordinates.
     *
     * @param coords    Coordinates.  On input, <code>coords[0][i]</code>
     *                  and <code>coords[1][i]</code> are the X
     *                  and Y display coordinates, respectively,
     *                  of the <code>i</code>th point.  On
     *                  output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  pressure and saturation equivalent temperature
     *			coordinates, respectively.
     * @return		<code>coords</code>).
     * @exception VisADException	Unsupported operation.
     */
    public double[][]
    fromReference(double[][] coords)
	throws VisADException
    {
	coords = thetaCoordSys.getSkewTCoordSys().fromReference(coords);

	double[]	pressures = coords[0];
	double[]	temperatures =
	    SI.kelvin.toThis(coords[1], getTemperatureUnit());
	double[]	thetas = new Theta(getPressureUnit(),
	    SI.kelvin).toTheta(pressures, temperatures);

	for (int i = 0; i < pressures.length; ++i)
	{
	    double	pressure = pressures[i];
	    double	temperature = temperatures[i];
	    double	exponent = (17.2694*(temperature - 273.16)) /
		(temperature - 35.86);
	    double	eSat = eSat0 * Math.exp(exponent);
	    double	rSat = 0.622 * (eSat/(pressure - eSat));

	    temperatures[i] = thetas[i] * (1.0 + 2500.0 * rSat / temperature);
	}

	coords[1] = getTemperatureUnit().toThis(temperatures, SI.kelvin);

	return coords;
    }


    /**
     * Transforms saturation equivalent potential temperature coordinates
     * to display coordinates.
     *
     * @param coords    Coordinates.  On input,
     *			<code>coords[0][i]</code>
     *                  and <code>coords[1][i]</code> are the
     *                  pressure and saturation equivalent potential
     *			temperature coordinates,
     *                  respectively, of the <code>i</code>th point.
     *                  On output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  X and Y display coordinates, respectively.
     * @return		<code>coords</code>).
     * @exception VisADException	Unsupported operation.
     */
    public double[][]
    toReference(double[][] coords)
	throws VisADException
    {
	throw new VisADException(
	"Can't convert equivalent potential temperature to temperature -- yet");
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
	    thetaCoordSys.equals(obj);
    }
}
