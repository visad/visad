/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ThetaCoordinateSystem.java,v 1.1 1998-08-24 18:24:21 steve Exp $
 */

package visad.meteorology;

import visad.SI;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.Parser;
import visad.data.netcdf.units.ParseException;


/**
 * Supports conversion between (pressure, potential_temperature) and
 * (Display.XAxis, Display.YAxis) coordinate systems.
 *
 * An instance of this class is immutable.
 *
 * Definitions:
 *	Real Coordinates	(pressure, potential temperature)
 *	Display Coordinates	Coordinates of the 2-D VisAD display.
 *
 * @author Steven R. Emmerson
 */
public class
ThetaCoordinateSystem
    extends	SkewTCoordinateSystem
{
    /**
     * The reference pressure in units of getPressureUnit().
     */
    private final double		referencePressure;

    /*
     * The following value is take from "An Introduction to Boundary
     * Layer Meteorology" by Roland B. Stull; chapter 13 (Boundary Layer
     * Clouds).
     */
    private static final float		kappa = 0.286f;


    /**
     * Constructs from another ThetaCoordinateSystem.
     *
     * @param thetaCoordSys	The other ThetaCoordinateSystem.
     */
    public
    ThetaCoordinateSystem(ThetaCoordinateSystem thetaCoordSys)
	throws VisADException
    {
	this((SkewTCoordinateSystem)thetaCoordSys);
    }


    /**
     * Constructs from a SkewTCoordinateSystem.
     *
     * @param skewTCoordSys	Skew-T, log p coordinate system.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    ThetaCoordinateSystem(SkewTCoordinateSystem skewTCoordSys)
	throws VisADException
    {
	super(skewTCoordSys);

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
     * Transforms real coordinates to display coordinates.
     *
     * @param coords    Real coordinates: <code>coords[0][i]</code>
     *                  and <code>coords[1][i]</code> are the
     *                  pressure and temperature coordinates,
     *                  respectively, of the <code>i</code>th point.
     *                  On output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  X and Y display coordinates, respectively.
     * @return		Corresponding display coordinates (i.e. 
     *			<code>coords</code>).
     */
    public double[][]
    toReference(double[][] coords)
	throws VisADException
    {
	try
	{
	    return super.toReference(convert(coords));
	}
	catch (UnitException e)
	{
	    throw new VisADException(e.getMessage());
	}
    }


    /**
     * Transforms display coordinates to real coordinates.
     *
     * @param coords    Display coordinates: <code>coords[0][i]</code>
     *                  and <code>coords[1][i]</code> are the X
     *                  and Y display coordinates, respectively,
     *                  of the <code>i</code>th point.  On
     *                  output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  pressure and temperature coordinates, respectively.
     * @return		Corresponding real coordinates (i.e. 
     *			<code>coords</code>).
     */
    public double[][]
    fromReference(double[][] coords)
	throws VisADException
    {
	try
	{
	    return invert(super.fromReference(coords));
	}
	catch (UnitException e)
	{
	    throw new VisADException(e.getMessage());
	}
    }


    /**
     * Convert potential temperature coordinates to temperature coordinates.
     *
     * @exception UnitException	Incompatible Units.
     */
    protected double[][]
    convert(double[][] coords)
	throws UnitException
    {
	double[]	pressures = coords[0];
	double[]	temperatures = coords[1];

	temperatures = getTemperatureUnit().toThat(temperatures, SI.kelvin);

	for (int i = 0; i < pressures.length; ++i)
	{
	    double	factor = Math.pow(referencePressure/pressures[i],
					  kappa);

	    temperatures[i] /= factor;
	}

	temperatures = getTemperatureUnit().toThis(temperatures, SI.kelvin);

	coords[1] = temperatures;

	return coords;
    }


    /**
     * Convert temperature coordinates to potential temperature coordinates.
     *
     * @param coords    Pressure/temperature coordinates:
     *                  <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the pressure
     *                  and temperature coordinates, respectively,
     *                  of the <code>i</code>th point.  On
     *                  output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  pressure and potential temperature coordinates,
     *                  respectively.
     * @exception UnitException	Incompatible Units.
     */
    protected double[][]
    invert(double[][] coords)
	throws UnitException
    {
	double[]	pressures = coords[0];
	double[]	temperatures = coords[1];

	temperatures = getTemperatureUnit().toThat(temperatures, SI.kelvin);

	for (int i = 0; i < pressures.length; ++i)
	    temperatures[i] = theta(pressures[i], temperatures[i]);

	temperatures = getTemperatureUnit().toThis(temperatures, SI.kelvin);

	coords[1] = temperatures;

	return coords;
    }


    /**
     * Computes potential temperature from pressure and temperature.
     * May be used by subclasses.
     *
     * @param pressure		Pressure in getPressureUnit() units.
     * @param temperature	Temperature in K.
     * @return			Potential temperature in K.
     */
    protected final double
    theta(double pressure, double temperature)
    {
	return temperature * Math.pow(referencePressure/pressure, kappa);
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
	if (!(obj instanceof ThetaCoordinateSystem))
	    return false;

	return referencePressure == 
		    ((ThetaCoordinateSystem)obj).referencePressure &&
	       super.equals(obj);
    }
}
