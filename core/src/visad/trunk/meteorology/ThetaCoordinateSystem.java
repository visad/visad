/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ThetaCoordinateSystem.java,v 1.2 1998-08-28 16:50:25 steve Exp $
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
    extends	CoordinateSystem
{
    /**
     * The potential temperature utility.
     */
    private final Theta			theta;

    /**
     * The associated Skew-T, Log P coordinate system.
     */
    private final SkewTCoordinateSystem	skewTCoordSys;


    /**
     * Constructs from another ThetaCoordinateSystem.
     *
     * @param thetaCoordSys	The other ThetaCoordinateSystem.
     */
    public
    ThetaCoordinateSystem(ThetaCoordinateSystem thetaCoordSys)
	throws VisADException
    {
	this(thetaCoordSys.skewTCoordSys);
    }


    /**
     * Constructs from a SkewTCoordinateSystem.
     *
     * @param skewTCoordSys	Skew-T, log p coordinate system.
     * @postcondition		<code>getSkewTCoordSys()</code> will return
     *				<code>skewTCoordSys</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    ThetaCoordinateSystem(SkewTCoordinateSystem skewTCoordSys)
	throws VisADException
    {
	super(Display.DisplaySpatialCartesianTuple, 
	    skewTCoordSys.getCoordinateSystemUnits());

	this.skewTCoordSys = skewTCoordSys;

	theta = new Theta(skewTCoordSys.getPressureUnit(),
			  skewTCoordSys.getTemperatureUnit());
    }


    /**
     * Transforms real coordinates to display coordinates.
     *
     * @param coords    Coordinates.  On input,  <code>coords[0][i]</code>
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
	    return skewTCoordSys.toReference(convert(coords));
	}
	catch (UnitException e)
	{
	    throw new VisADException(e.getMessage());
	}
    }


    /**
     * Transforms display coordinates to real coordinates.
     *
     * @param coords    Coordinates: On input, <code>coords[0][i]</code>
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
	    return invert(skewTCoordSys.fromReference(coords));
	}
	catch (UnitException e)
	{
	    throw new VisADException(e.getMessage());
	}
    }


    /**
     * Convert potential temperature coordinates to temperature coordinates.
     *
     * @exception UnitException		Incompatible Units.
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    protected double[][]
    convert(double[][] coords)
	throws UnitException, VisADException
    {
	coords[1] = theta.temperature(coords[0], coords[1]);

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
     * @precondition	<code>coords[0].length == coords[1].length</code>
     * @return		<code>coords</code>.
     * @exception UnitException		Incompatible Units.  Shouldn't happen.
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    protected double[][]
    invert(double[][] coords)
	throws UnitException, VisADException
    {
	coords[1] = theta.toTheta(coords[0], coords[1]);

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
	return theta(pressure, temperature);
    }


    /**
     * Gets the pressure unit.
     *
     * @return	The unit of pressure.
     */
    public Unit
    getPressureUnit()
    {
	return skewTCoordSys.getPressureUnit();
    }


    /**
     * Gets the temperature unit.
     *
     * @return	The unit of temperature.
     */
    public Unit
    getTemperatureUnit()
    {
	return skewTCoordSys.getTemperatureUnit();
    }


    /**
     * Returns the associated Skew T, Log P coordinate system.
     *
     * @return	The associated Skew T, Log P coordinate system.
     */
    public SkewTCoordinateSystem
    getSkewTCoordSys()
    {
	return skewTCoordSys;
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

	ThetaCoordinateSystem	that = (ThetaCoordinateSystem)obj;

	return theta.equals(that.theta) &&
	       skewTCoordSys.equals(that.skewTCoordSys);
    }
}
