/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: ThetaESCoordinateSystem.java,v 1.4 1999-01-07 16:13:21 steve Exp $
 */

package visad.meteorology;

import visad.CommonUnit;
import visad.CoordinateSystem;
import visad.Display;
import visad.SI;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
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
     * The associated potential temperature coordinate system.
     */
    private final ThetaCoordinateSystem	thetaCoordSys;

    /**
     * The associated Skew-T coordinate system.
     */
    private final SkewTCoordinateSystem	skewTCoordSys;


    /**
     * Constructs from a potential temperature coordinate system.
     *
     * @param thetaCoordSys	Potential temperature cooordinate system.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    ThetaESCoordinateSystem(ThetaCoordinateSystem thetaCoordSys)
	throws VisADException
    {
	super(thetaCoordSys.getReference(), 
	    thetaCoordSys.getCoordinateSystemUnits());

	this.thetaCoordSys = thetaCoordSys;
	skewTCoordSys = thetaCoordSys.getSkewTCoordSys();
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
     * Gets the saturation equivalent potential temperature unit.
     *
     * @return	The unit of saturation equivalent potential temperature.
     */
    public Unit
    getThetaESUnit()
    {
	return thetaCoordSys.getThetaUnit();
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
     *                  pressure and saturation equivalent potential 
     *			temperature coordinates, respectively.
     * @return		<code>coords</code>).
     * @exception VisADException	Unsupported operation.
     */
    public double[][]
    fromReference(double[][] coords)
	throws VisADException
    {
	coords = skewTCoordSys.fromReference(coords);
	double[]	pressures = coords[0];
	double[]	kelvins =
	    SI.kelvin.toThis(coords[1], skewTCoordSys.getTemperatureUnit());
	double[]	rSats = RSat.rSat(
	    pressures, skewTCoordSys.getPressureUnit(),
	    kelvins, SI.kelvin, CommonUnit.dimensionless);
	double[]	thetas =
	    Theta.theta(
		pressures, skewTCoordSys.getPressureUnit(),
		kelvins, SI.kelvin, SI.kelvin);

	for (int i = 0; i < rSats.length; ++i)
	    kelvins[i] = thetas[i] * (1.0 + 2500 * rSats[i] / kelvins[i]);

	coords[1] = getThetaESUnit().toThis(kelvins, SI.kelvin);

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

	ThetaESCoordinateSystem	that = (ThetaESCoordinateSystem)obj;

	return thetaCoordSys.equals(that.thetaCoordSys);
    }
}
