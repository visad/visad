/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTCoordinateSystem.java,v 1.6 1998-11-16 18:23:48 steve Exp $
 */

package visad.meteorology;

import java.awt.geom.Rectangle2D;
import visad.CoordinateSystem;
import visad.Display;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.Quantity;


/**
 * Supports conversion between the (x,y) points on a skew T - log P
 * diagram and (pressure,temperature) points.
 *
 * An instance of this class is immutable.
 *
 * Internal Definitions:
 *	Real Coordinates	(pressure, temperature)
 *	Display Coordinates	Coordinates of the 2-D VisAD display.
 *
 * @author Steven R. Emmerson
 */
public class
SkewTCoordinateSystem
    extends	CoordinateSystem
{
    /*
     * Default parameter values.
     */
    public static final float		DEFAULT_MIN_X =   -1.0f;
    public static final float		DEFAULT_MAX_X =    1.0f;
    public static final float		DEFAULT_MIN_Y =   -1.0f;
    public static final float		DEFAULT_MAX_Y =    1.0f;
    public static final float		DEFAULT_MIN_P =  100.0f;
    public static final float		DEFAULT_MAX_P = 1050.0f;
    public static final float		DEFAULT_MIN_T = -122.5f;
    public static final float		DEFAULT_MAX_T =   52.0f;
    public static final float		DEFAULT_TANGENT =  1.2f;
    public static final Quantity	DEFAULT_PRESSURE_QUANTITY;
    public static final Quantity	DEFAULT_TEMPERATURE_QUANTITY;

    /*
     * Actual parameter values.
     */
    public final float	minX;
    public final float	maxX;
    public final float	minY;
    public final float	maxY;
    public final float	minP;
    public final float	maxP;
    public final float	minT;
    public final float	maxT;
    public final float	tangent;
    public final float	maxTAtMinP;
    public final float	minTAtMaxP;

    /**
     * Transformation parameters.
     */
    private final float		yPerLogP;
    private final float		logMinP;
    private final float		xPerT;


    static
    {
	DEFAULT_PRESSURE_QUANTITY = CommonTypes.PRESSURE;;
	DEFAULT_TEMPERATURE_QUANTITY = CommonTypes.TEMPERATURE;
    }


    /**
     * Constructs from nothing.  Default display and data parameters
     * are used.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SkewTCoordinateSystem()
	throws VisADException
    {
	this(DEFAULT_MIN_X, DEFAULT_MAX_X, DEFAULT_MIN_Y, DEFAULT_MAX_Y,
	     DEFAULT_PRESSURE_QUANTITY.getDefaultUnit(),
	     DEFAULT_MIN_P, DEFAULT_MAX_P,
	     DEFAULT_TEMPERATURE_QUANTITY.getDefaultUnit(),
	     DEFAULT_MIN_T, DEFAULT_MAX_T, 
	     DEFAULT_TANGENT);
    }


    /**
     * Constructs from another SkewTCoordinateSystem.
     *
     * @param that		The other SkewTCoordinateSystem.
     * @throws UnitException	Improper unit.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SkewTCoordinateSystem(SkewTCoordinateSystem that)
	throws UnitException, VisADException
    {
	this(that.minX, that.maxX, that.minY, that.maxY,
	     that.getPressureUnit(), that.minP, that.maxP,
	     that.getTemperatureUnit(), that.minT, that.maxT,
	     that.tangent);
    }


    /**
     * Constructs from display and data parameters.
     *
     * @param minX		The minimum display X coordinate.
     * @param maxX		The maximum display X coordinate.
     * @param minY		The minimum display Y coordinate.
     * @param maxY		The maximum display Y coordinate.
     * @param pressureUnit	The unit of pressure.
     * @param minP		The minimum pressure coordinate.
     * @param maxP		The maximum pressure coordinate.
     * @param temperatureUnit	The unit of temperature.
     * @param minT		The minimum temperature coordinate.
     * @param maxT		The maximum temperature coordinate.
     * @param tangent		The tangent of the isotherms in display space.
     * @throws UnitException	Improper unit.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SkewTCoordinateSystem(float minX, float maxX, float minY, float maxY,
			  Unit pressureUnit, float minP, float maxP,
			  Unit temperatureUnit, float minT, float maxT,
			  float tangent)
	throws UnitException, VisADException
    {
	super(Display.DisplaySpatialCartesianTuple, 
	    getUnits(pressureUnit, temperatureUnit));

	this.minX = minX;
	this.maxX = maxX;
	this.minY = minY;
	this.maxY = maxY;
	this.minP = minP;
	this.maxP = maxP;
	this.minT = minT;
	this.maxT = maxT;
	this.tangent = tangent;

	float	deltaX = maxX - minX;
	float	deltaY = minY - maxY;
	float	deltaT = maxT - minT;
	float	normGradY = (float)-Math.sqrt(1/(tangent*tangent + 1));
	float	normGradX = -normGradY*tangent;
	float	dotProduct = (deltaX * normGradX + deltaY * normGradY);
	float	gradX = deltaT * normGradX / dotProduct;
	float	gradY = deltaT * normGradY / dotProduct;

	maxTAtMinP = minT + deltaX * gradX;
	minTAtMaxP = minT + deltaY * gradY;

	logMinP = (float)Math.log(minP);
	yPerLogP = (float)(deltaY / (Math.log(maxP) - logMinP));
	xPerT = deltaX / (maxT - minTAtMaxP);

	if (!Unit.canConvert(
		pressureUnit, DEFAULT_PRESSURE_QUANTITY.getDefaultUnit()) ||
	    !Unit.canConvert(
		temperatureUnit, DEFAULT_TEMPERATURE_QUANTITY.getDefaultUnit()))
	{
	    throw new UnitException("Improper unit argument(s)");
	}
    }


    /**
     * Gets the coordinate system units.
     */
    protected static Unit[]
    getUnits(Unit pressureUnit, Unit temperatureUnit)
    {
	Unit[]	units = new Unit[] {pressureUnit, temperatureUnit, null};

	return units;
    }


    /**
     * Return the pressure unit.
     */
    public Unit
    getPressureUnit()
    {
	return getCoordinateSystemUnits()[0];
    }


    /**
     * Return the temperature unit.
     */
    public Unit
    getTemperatureUnit()
    {
	return getCoordinateSystemUnits()[1];
    }


    /**
     * Transforms real coordinates to display coordinates.
     *
     * @param coords    Real coordinates: <code>coords[0][i]</code>
     *                  and <code>coords[0][i]</code> are the
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
	if (coords == null || coords.length < 2)
	    throw new IllegalArgumentException("Invalid real coordinates");

	int		npts = coords[0].length;
	double[]	x = coords[0];
	double[]	y = coords[1];

	for (int i = 0; i < npts; ++i)
	{
	    double	pressure = x[i];
	    double	temperature = y[i];
	    double	deltaY = yPerLogP * ((Math.log(pressure)) - logMinP);

	    x[i] = xPerT * (temperature - minT) + minX + deltaY/tangent;
	    y[i] = maxY + deltaY;
	}

	return coords;
    }


    /**
     * Transforms display coordinates to real coordinates.
     *
     * @param coords    Display coordinates: <code>coords[0][i]</code>
     *                  and <code>coords[0][i]</code> are the X
     *                  and Y display coordinates, respectively,
     *                  of the <code>i</code>th point.  On
     *                  output, <code>coords[0][i]</code> and
     *                  <code>coords[1][i]</code> are the corresponding
     *                  pressure and temperature coordinates,
     *                  respectively.
     * @return		Corresponding real coordinates (i.e. 
     *			<code>coords</code>).
     */
    public double[][]
    fromReference(double[][] coords)
	throws VisADException
    {
	if (coords == null || coords.length < 2)
	    throw new IllegalArgumentException("Invalid real coordinates");

	int		npts = coords[0].length;
	double[]	pressures = coords[0];
	double[]	temperatures = coords[1];

	for (int i = 0; i < npts; ++i)
	{
	    // System.out.print("SkewTCoordinateSystem.fromReference(): (" +
		// coords[0][i] + "," + coords[1][i] + ") -> ");

	    double	x = pressures[i];
	    double	deltaY = temperatures[i] - maxY;

	    pressures[i] = Math.exp(deltaY/yPerLogP + logMinP);
	    temperatures[i] = (x - deltaY/tangent - minX) /
		xPerT + minT;

	    // System.out.println("(" + coords[0][i] + "," +
		// coords[1][i] + ")");
	}

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
	if (!(obj instanceof SkewTCoordinateSystem))
	    return false;

	SkewTCoordinateSystem	that = (SkewTCoordinateSystem)obj;

	return
	    that.minX == minX &&
	    that.maxX == maxX &&
	    that.minY == minY &&
	    that.maxY == maxY &&
	    that.minP == minP &&
	    that.maxP == maxP &&
	    that.minT == minT &&
	    that.maxT == maxT &&
	    that.tangent == tangent;
    }


    /**
     * Tests this class.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	SkewTCoordinateSystem	cs = new SkewTCoordinateSystem();

	double[][]	coords = new double[][]
	{
	    {
		0, -1, +1, +1, -1
	    },
	    {
		0, -1, -1, +1, +1
	    },
	    {
		0,  0,  0,  0,  0
	    }
	};
	int		npts = coords[0].length;

	System.out.println("Display Coordinates: ");
	for (int i = 0; i < npts; ++i)
	    System.out.println("    (" + coords[0][i] + "," + coords[1][i] +
		")");

	cs.fromReference(coords);
	System.out.println("(P,T) Coordinates: ");
	for (int i = 0; i < npts; ++i)
	    System.out.println("    (" + coords[0][i] + "," + coords[1][i] +
		")");

	cs.toReference(coords);
	System.out.println("Display Coordinates: ");
	for (int i = 0; i < npts; ++i)
	    System.out.println("    (" + coords[0][i] + "," + coords[1][i] +
		")");
    }
}
