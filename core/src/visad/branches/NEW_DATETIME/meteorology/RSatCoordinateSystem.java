/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: RSatCoordinateSystem.java,v 1.3 1999-01-07 16:13:18 steve Exp $
 */

package visad.meteorology;

import visad.CoordinateSystem;
import visad.Display;
import visad.SI;
import visad.ScaledUnit;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Supports conversion between (pressure, saturation_mixing_ratio) and
 * other coordinate systems.
 *
 * An instance of this class is immutable.
 *
 * @author Steven R. Emmerson
 */
public class
RSatCoordinateSystem
    extends	CoordinateSystem
{
    /**
     * The default unit of saturation mixing-ratio.
     */
    public static Unit			DEFAULT_RSAT_UNIT =
	CommonUnits.GRAMS_PER_KILOGRAM;

    /**
     * The unit of saturation mixing ratio.
     */
    private final Unit			rSatUnit;

    /**
     * The associated Skew-T coordinate system.
     */
    private final SkewTCoordinateSystem	skewTCoordSys;


    /**
     * Constructs from a Skew-T coordinate system and a saturation mixing-
     * ratio unit.
     *
     * @param skewTCoordSys	The Skew-T coordinate system.
     * @param rSatUnit		The unit for saturation mixing ratios.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    RSatCoordinateSystem(SkewTCoordinateSystem skewTCoordSys, Unit rSatUnit)
	throws VisADException
    {
	super(skewTCoordSys.getReference(),
	    adjustUnits(skewTCoordSys, rSatUnit));

	this.skewTCoordSys = skewTCoordSys;
	this.rSatUnit = rSatUnit;
    }


    /**
     * Adjusts units for this coordinate system.
     *
     * @param skewTCoordSys	The Skew-T coordinate system.
     * @param pressureUnit	The assumed unit for pressure.
     * @param rSatUnit          The assumed unit for saturation mixing ratio.
     */
    protected static Unit[]
    adjustUnits(SkewTCoordinateSystem skewTCoordSys, Unit rSatUnit)
    {
	Unit[]	units = skewTCoordSys.getCoordinateSystemUnits();

	units[1] = rSatUnit;

	return units;
    }


    /**
     * Transforms coordinates to the reference space.
     *
     * @param coords    Coordinates.  On input,  <code>coords[0][i]</code>
     *                  and <code>coords[1][i]</code> are the
     *                  pressure and saturation mixing ratio coordinates,
     *                  respectively, of the <code>i</code>th point.
     *                  On output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  X and Y display coordinates, respectively.
     * @return		<code>coords</code>.
     */
    public double[][]
    toReference(double[][] coords)
	throws VisADException
    {
	coords[1] = RSat.temperature(
	    coords[0], skewTCoordSys.getPressureUnit(),
	    coords[1], getRSatUnit(), skewTCoordSys.getTemperatureUnit());

	return skewTCoordSys.toReference(coords);
    }


    /**
     * Transforms coordinates from the reference space.
     *
     * @param coords    Coordinates: On input, <code>coords[0][i]</code>
     *                  and <code>coords[1][i]</code> are the X
     *                  and Y display coordinates, respectively,
     *                  of the <code>i</code>th point.  On
     *                  output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  pressure and saturation mixing-ratio coordinates,
     *			respectively.
     * @return		<code>coords</code>).
     */
    public double[][]
    fromReference(double[][] coords)
	throws VisADException
    {
	skewTCoordSys.fromReference(coords);

	coords[1] = RSat.rSat(
	    coords[0], skewTCoordSys.getPressureUnit(), 
	    coords[1], skewTCoordSys.getTemperatureUnit(), getRSatUnit());

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
     * Gets the saturation mixing-ratio unit.
     *
     * @return	The unit of saturation mixing-ratio.
     */
    public Unit
    getRSatUnit()
    {
	return rSatUnit;
    }


    /**
     * Gets the associated Skew T, Log P coordinate system.
     *
     * @return	The associated Skew T, Log P coordinate system.
     */
    public SkewTCoordinateSystem
    getSkewTCoordSys()
    {
	return skewTCoordSys;
    }


    /*
     * Indicates if this coordinate system is the same an object.
     *
     * @param obj	The object to be compared with this one.
     * @return		<code>true</code> if and only if <code>obj</code> is
     *			semantically identical to this object.
     */
    public boolean
    equals(java.lang.Object obj)
    {
	if (!(obj instanceof RSatCoordinateSystem))
	    return false;

	RSatCoordinateSystem	that = (RSatCoordinateSystem)obj;

	return skewTCoordSys.equals(that.skewTCoordSys);
    }
}
