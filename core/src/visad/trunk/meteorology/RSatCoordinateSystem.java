/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: RSatCoordinateSystem.java,v 1.1 1998-10-21 15:27:58 steve Exp $
 */

package visad.meteorology;

import visad.CoordinateSystem;
import visad.Display;
import visad.SI;
import visad.ScaledUnit;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.Parser;
import visad.data.netcdf.units.ParseException;


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
    public static ScaledUnit			DEFAULT_RSAT_UNIT =
	new ScaledUnit(0.001);			// g/kg

    /**
     * The associated Skew-T coordinate system.
     */
    private final SkewTCoordinateSystem	skewTCoordSys;

    /**
     * The saturation mixing ratio utility.
     */
    private final RSat			rSat;


    /**
     * Constructs from a Skew-T coordinate system and a saturation mixing-
     * ratio unit.
     *
     * @param skewTCoordSys	The Skew-T coordinate system.
     * @param rSatUnit		The assumed unit for saturation mixing ratio.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    RSatCoordinateSystem(SkewTCoordinateSystem skewTCoordSys, Unit rSatUnit)
	throws VisADException
    {
	super(skewTCoordSys.getReference(),
	    adjustUnits(skewTCoordSys, rSatUnit));

	this.skewTCoordSys = skewTCoordSys;

	rSat = new RSat(skewTCoordSys.getPressureUnit(),
	    skewTCoordSys.getTemperatureUnit(), rSatUnit);
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
	coords[1] = rSat.temperature(coords[0], coords[1]);

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

	coords[1] = rSat.rSat(coords[0], coords[1]);

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
	return rSat.getPressureUnit();
    }


    /**
     * Gets the saturation mixing-ratio unit.
     *
     * @return	The unit of saturation mixing-ratio.
     */
    public Unit
    getRSatUnit()
    {
	return rSat.getRSatUnit();
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

	return rSat.equals(that.rSat) &&
	       skewTCoordSys.equals(that.skewTCoordSys);
    }
}
