/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ThetaCoordinateSystem.java,v 1.4 1999-01-07 16:13:21 steve Exp $
 */

package visad.meteorology;

import visad.CoordinateSystem;
import visad.Display;
import visad.SI;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


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
     * The associated Skew-T, Log P coordinate system.
     */
    private final SkewTCoordinateSystem	skewTCoordSys;

    /**
     * The unit of potential temperature.
     */
    private final Unit			thetaUnit;


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
	super(
	    skewTCoordSys.getReference(), 
	    new Unit[] {
		skewTCoordSys.getPressureUnit(),
		CommonTypes.THETA.getDefaultUnit(),
		null
	    }
	);

	this.skewTCoordSys = skewTCoordSys;
	thetaUnit = CommonTypes.THETA.getDefaultUnit();
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
	coords[1] =
	    Theta.temperature(
		coords[0], getPressureUnit(), coords[1], getThetaUnit(),
		skewTCoordSys.getTemperatureUnit());

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
	coords[1] =
	    Theta.theta(
		coords[0], getPressureUnit(), coords[1],
		skewTCoordSys.getTemperatureUnit(), getThetaUnit());

	return coords;
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
     * Gets the potential temperature unit.
     *
     * @return	The unit of potential temperature.
     */
    public Unit
    getThetaUnit()
    {
	return thetaUnit;
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

	return thetaUnit.equals(that.thetaUnit) &&
	       skewTCoordSys.equals(that.skewTCoordSys);
    }
}
