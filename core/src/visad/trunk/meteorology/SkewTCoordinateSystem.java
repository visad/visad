/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTCoordinateSystem.java,v 1.3 1998-08-19 17:19:57 steve Exp $
 */

package visad.meteorology;

import java.awt.geom.Rectangle2D;
import visad.CoordinateSystem;
import visad.Display;
import visad.DisplayTupleType;
import visad.DisplayRealType;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.units.Parser;


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
    /**
     * The coordinate system units.
     */
    private final static Unit[]	units = new Unit[3];

    /**
     * Transformation parameters.
     */
    private final double	YPerNegLogP;
    private final double	negLogMaxP;
    private final double	XPerT;
    public final Rectangle2D	viewport;
    public final double		minP;
    public final double		maxP;
    public final double		minTAtMaxP;
    public final double		maxTAtMaxP;
    public final double		minTAtMinP;
    public final double		maxTAtMinP;
    public final double		isothermTangent;

    /**
     * Canonical diagram parameters.  Estimated from page 2-3 of
     * AWS/TR-79/006: "The Use of the Skew T, Log P Diagram in
     * Analysis and Forecasting".
     */
    private static final double	CANONICAL_MIN_P            =  100.0;
    private static final double	CANONICAL_MAX_P            = 1050.0;
    private static final double	CANONICAL_MIN_T_AT_MAX_P   =  -46.5;
    private static final double	CANONICAL_MAX_T_AT_MAX_P   =   52.0;
    private static final double	CANONICAL_ISOTHERM_TANGENT =    1.2;


    static
    {
	try
	{
	    Parser	parser = Parser.instance();

	    units[0] = parser.parse("millibar");
	    units[1] = parser.parse("celsius");
	    units[2] = null;
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println
		("Couldn't initialize class SkewTCoordinateSystem" +
		(reason == null ? "" : (": " + reason)));
	    e.printStackTrace();
	}
    }


    /**
     * Return the canonical pressure unit.
     */
    public static Unit
    getPressureUnit()
    {
	return units[0];
    }


    /**
     * Return the canonical temperature unit.
     */
    public static Unit
    getTemperatureUnit()
    {
	return units[1];
    }


    /**
     * Constructs from nothing.  Default pressure and temperature extents 
     * and default isotherm angle are used.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SkewTCoordinateSystem()
	throws VisADException
    {
	this(CANONICAL_MIN_P, CANONICAL_MAX_P, 
	    CANONICAL_MIN_T_AT_MAX_P, CANONICAL_MAX_T_AT_MAX_P,
	    CANONICAL_ISOTHERM_TANGENT, getDefaultDisplayRectangle());
    }


    /**
     * Constructs from another SkewTCoordinateSystem.
     *
     * @param that	The other SkewTCoordinateSystem.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SkewTCoordinateSystem(SkewTCoordinateSystem that)
	throws VisADException
    {
	this(that.minP, that.maxP, that.minTAtMaxP, that.maxTAtMaxP,
	    that.isothermTangent, that.viewport);
    }


    /**
     * Gets the default display rectangle.  
     *
     * @return		Display viewport.
     */
    protected static Rectangle2D
    getDefaultDisplayRectangle()
	throws	VisADException
    {
	double[]		xRange = new double[2];
	double[]		yRange = new double[2];
	DisplayTupleType	reference =
	    Display.DisplaySpatialCartesianTuple;

	((DisplayRealType)reference.getComponent(0)).getRange(xRange);
	((DisplayRealType)reference.getComponent(1)).getRange(yRange);

	return new Rectangle2D.Float(
	    (float)xRange[0], (float)yRange[0], 
	    (float)(xRange[1] - xRange[0]), (float)(yRange[1] - yRange[0]));
    }



    /**
     * Constructs, given pressure and temperature extents and isotherm angle.
     *
     * @param reference		The reference coordinate space (e.g.
     *				visad.Display.DisplaySpatialCartesianTuple).
     * @param minP		Minimum pressure on the plot in millibar.
     * @param maxP		Maximum pressure on the plot in millibar.
     * @param minTAtMaxP	Lower left temperature on the plot in celsius.
     * @param maxTAtMaxP	Lower right temperature on the plot in celsius.
     * @param isothermTangent	Tangent of the isotherm angle with the 
     *				horizontal.
     * @param viewport		Display viewport.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SkewTCoordinateSystem(double minP, double maxP,
	    double minTAtMaxP, double maxTAtMaxP,
	    double isothermTangent, Rectangle2D viewport)
	throws VisADException
    {
	super(Display.DisplaySpatialCartesianTuple, units);

	this.minP = minP;
	this.maxP = maxP;
	this.minTAtMaxP = minTAtMaxP;
	this.maxTAtMaxP = maxTAtMaxP;
	this.isothermTangent = isothermTangent;
	this.viewport = viewport;

	negLogMaxP = -Math.log(maxP);
	YPerNegLogP = viewport.getHeight() / ((-Math.log(minP)) - negLogMaxP);
	XPerT = viewport.getWidth() / (maxTAtMaxP - minTAtMaxP);

	double[]	coords[] = fromReference(
	    new double[][] {new double[] {viewport.getX(), 
					  viewport.getY()+viewport.getHeight()},
			    new double[] {viewport.getX()+viewport.getWidth(),
					  viewport.getY()+viewport.getHeight()},
			    new double[] {0, 0}});
	minTAtMinP = coords[1][0];
	maxTAtMinP = coords[1][1];
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
	if (coords == null || coords.length != 3)
	    throw new IllegalArgumentException("Invalid real coordinates");

	int		npts = coords[0].length;

	for (int i = 0; i < npts; ++i)
	{
	    double	pressure = coords[0][i];
	    double	temperature = coords[1][i];
	    double	deltaY = YPerNegLogP*
		((-Math.log(pressure)) - negLogMaxP);

	    coords[0][i] = XPerT * (temperature - minTAtMaxP) +
		viewport.getX() + deltaY/isothermTangent;	// X
	    coords[1][i] = viewport.getY() + deltaY;		// Y
	    coords[2][i] = 0;					// Z
	}

	return coords;
    }


    /**
     * Transforms a real coordinate to a display coordinate.
     */
    public double[]
    toReference(double pressure, double temperature)
	throws VisADException
    {
	double[][]	xyCoords = toReference(
	    new double[][] {new double[] {pressure}, new double[] {temperature},
			    new double[] {0}});

	return new double[] {xyCoords[0][0], xyCoords[1][0]};
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
	if (coords == null || coords.length != 3)
	    throw new IllegalArgumentException("Invalid real coordinates");

	int		npts = coords[0].length;

	for (int i = 0; i < npts; ++i)
	{
	    // System.out.print("SkewTCoordinateSystem.fromReference(): (" +
		// coords[0][i] + "," + coords[1][i] + ") -> ");

	    double	x = coords[0][i];
	    double	deltaY = coords[1][i] - viewport.getY();

	    coords[0][i] = Math.exp(-deltaY/YPerNegLogP - negLogMaxP);
						// pressure
	    coords[1][i] = (x - deltaY/isothermTangent - viewport.getX()) /
		XPerT + minTAtMaxP;		// temperature

	    // System.out.println("(" + coords[0][i] + "," +
		// coords[1][i] + ")");
	}

	return coords;
    }


    /**
     * Transforms a display coordinate to a real coordinate.
     */
    public double[]
    fromReference(double x, double y)
	throws VisADException
    {
	double[][]	ptCoords = fromReference(
	    new double[][] {new double[] {x}, new double[] {y},
			    new double[] {}});

	return new double[] {ptCoords[0][0], ptCoords[1][0]};
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

	SkewTCoordinateSystem	other = (SkewTCoordinateSystem)obj;

	return
	    other.YPerNegLogP     == YPerNegLogP &&
	    other.negLogMaxP      == YPerNegLogP &&
	    other.XPerT           == XPerT &&
	    other.minTAtMaxP      == minTAtMaxP &&
	    other.viewport.equals(viewport) &&
	    other.isothermTangent == isothermTangent;
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
