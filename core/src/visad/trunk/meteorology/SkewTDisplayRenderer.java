/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTDisplayRenderer.java,v 1.1 1998-08-12 17:17:22 visad Exp $
 */

package visad.meteorology;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import visad.ContourControl;
import visad.CoordinateSystem;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayException;
import visad.DisplayRealType;
import visad.DisplayTupleType;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.Set;
import visad.TupleType;
import visad.VisADAppearance;
import visad.VisADException;
import visad.VisADGroup;
import visad.VisADLineArray;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityMap;
import visad.java2d.DefaultDisplayRendererJ2D;
import visad.java2d.VisADCanvasJ2D;


/**
 * Provides a 2-D VisAD display for a Skew T, Log P Diagram (alias 
 * "Skew-T Chart").
 *
 * @author Steven R. Emmerson
 */
public class
SkewTDisplayRenderer
    extends	DefaultDisplayRendererJ2D
{
    /**
     * The pressure coordinate.
     */
    public /*final*/ static DisplayRealType	Pressure;

    /**
     * The temperature coordinate.
     */
    public /*final*/ static DisplayRealType	Temperature;

    /**
     * The superfluous Z coordinate.
     */
    public /*final*/ static DisplayRealType	SkewTZAxis;

    /**
     * The (pressure,temperature) space.
     */
    public /*final*/ static SkewTCoordinateSystem	SkewTCoordSys;
    public /*final*/ static DisplayRealType[] 		componentsSkewT;
    public /*final*/ static DisplayTupleType		DisplaySkewTTuple;

    /**
     * Taken from AWS/TR-79/006: "The Use of the Skew T, Log P Diagram
     * in Analysis and Forecasting".
     */
    private final static double	DELTA_P  = 50.0;
    private final static double	DELTA_T  =  5.0;


    static
    {
	try
	{
	    SkewTCoordSys = new SkewTCoordinateSystem();

	    Pressure = new DisplayRealType("Pressure", false, 
		SkewTCoordSys.minP, SkewTCoordSys.maxP, 0.0, 
		SkewTCoordSys.getPressureUnit());;
	    Temperature = new DisplayRealType("Temperature", false,
		SkewTCoordSys.minTAtMinP, 
		SkewTCoordSys.maxTAtMaxP, 0.0,
		SkewTCoordSys.getTemperatureUnit());;

	    SkewTZAxis = new DisplayRealType("SkewTZAxis", false,
		-1.0, 1.0, 0.0, null);

	    componentsSkewT =
		new DisplayRealType[] {Pressure, Temperature, SkewTZAxis};

	    DisplaySkewTTuple = 
		new DisplayTupleType(componentsSkewT, SkewTCoordSys);
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println(
		"Couldn't initialize class SkewTDisplayRenderer" +
		(reason == null ? "" : (": " + reason)));
	    e.printStackTrace();
	}
    }


    /**
     * Supports iterating over a series of values.
     */
    protected static abstract class
    ValueIterator
    {
	abstract public double
	next();

	abstract public int
	getSize();
    }


    /**
     * Supports iterating over a truncated arithmetic series.
     */
    protected static class
    ArithmeticSeriesIterator
	extends	ValueIterator
    {
	private double	first;
	private double	delta;
	private int	count;
	private int	i = 0;

	ArithmeticSeriesIterator(double first, double delta, int count)
	{
	    this.first = first;
	    this.delta = delta;
	    this.count = count;
	}

	public double
	next()
	{
	    return first + i++ * delta;
	}

	public int
	getSize()
	{
	    return count;
	}
    }


    /**
     * Creates the scene graph.
     */
    public VisADGroup
    createSceneGraph(VisADCanvasJ2D canvas)
	throws	DisplayException
    {
	VisADGroup		root = super.createSceneGraph(canvas);
	Color			brown = Color.getColor("brown", 
	    Color.getHSBColor((float)(1./12.), 0.5f, 0.5f));

	/*
	 * Isobars:
	 */
	root.addChild(createIsobars(brown, 
	    new ArithmeticSeriesIterator(SkewTCoordSys.minP, DELTA_P, 
		1 + (int)Math.round((SkewTCoordSys.maxP -
		    SkewTCoordSys.minP)/DELTA_P)),
	    SkewTCoordSys));

	/*
	 * Isotherms:
	try
	{
	    root.addChild(createIsotherms(brown,
		new ArithmeticSeriesIterator(SkewTCoordSys.minTAtMinP,
		    DELTA_T, 
		    1 + (int)Math.round((SkewTCoordSys.maxTAtMaxP -
		    SkewTCoordSys.minTAtMinP)/DELTA_T)),
		SkewTCoordSys));
	}
	catch (VisADException e)
	{
	    throw new DisplayException(e.getMessage());
	}
	 */

	return root;
    }


    /**
     * Creates a set of isobars.
     */
    protected static VisADAppearance
    createIsobars(Color color, ValueIterator pressures,
	SkewTCoordinateSystem SkewTCoordSys)
    {
	VisADAppearance		isobars = new VisADAppearance();
	Rectangle2D.Double	viewport = SkewTCoordSys.viewport;

	isobars.red = red(color);
	isobars.green = green(color);
	isobars.blue = blue(color);
	// isobars.color_flag = true;

	VisADLineArray	isobarArray = new VisADLineArray();
	int		numPressures = pressures.getSize();
	float		minX = (float)viewport.getX();
	float		maxX = (float)(minX + viewport.getWidth());

	isobarArray.vertexCount = 2*numPressures;
	isobarArray.coordinates = new float[3*isobarArray.vertexCount];
	for (int i = 0; i < numPressures; ++i)
	{
	    isobarArray.coordinates[6*i+0] = minX;
	    isobarArray.coordinates[6*i+1] = 
		(float)yValue(pressures.next());
	    isobarArray.coordinates[6*i+2] = 0.0f;
	    isobarArray.coordinates[6*i+3] = maxX;
	    isobarArray.coordinates[6*i+4] = isobarArray.coordinates[6*i+1];
	    isobarArray.coordinates[6*i+5] = 0.0f;
	}
	isobars.array = isobarArray;

	return isobars;
    }


    /**
     * Creates a set of isotherms.
     */
    protected static VisADAppearance
    createIsotherms(Color color, ValueIterator temperatures,
		    SkewTCoordinateSystem SkewTCoordSys)
	throws VisADException
    {
	VisADAppearance		isotherms = new VisADAppearance();
	Rectangle2D.Double	viewport = SkewTCoordSys.viewport;

	isotherms.red = red(color);
	isotherms.green = green(color);
	isotherms.blue = blue(color);
	// isotherms.color_flag = true;

	int		numTemperatures = temperatures.getSize();
	int		numInside = 0;

	VisADLineArray	isothermArray = new VisADLineArray();
	isothermArray.vertexCount = numTemperatures*2;
	isothermArray.coordinates = new float[3*isothermArray.vertexCount];
	for (int i = 0; i < numTemperatures; ++i)
	{
	    if (clipIsotherm(temperatures.next(), SkewTCoordSys,
		    isothermArray.coordinates, 6*i))
		numInside++;
	}
	isothermArray.vertexCount = numInside*2;
	isotherms.array = isothermArray;

	return isotherms;
    }


    /**
     * Creates an isotherm that is clipped against a viewport.
     */
    protected static boolean
    clipIsotherm(double temperature, SkewTCoordinateSystem SkewTCoordSys,
	    float[] coordinates, int pos)
	throws VisADException
    {
	Rectangle2D.Double	viewport = SkewTCoordSys.viewport;

	double[][]	endPoints =
	    SkewTCoordSys.toReference(
		    new double[][] {new double[] {SkewTCoordSys.maxP,
						  SkewTCoordSys.minP},
				    new double[] {temperature, temperature},
				    new double[] {0.0, 0.0}});
	Line2D		segment =
	    new Line2D.Float((float)endPoints[0][0], (float)endPoints[1][0],
			     (float)endPoints[0][1], (float)endPoints[1][1]);

	/*
	 * Abort if isotherm lies outside viewport.
	 */
	if (!segment.intersects(viewport))
	    return false;

	float	slope = (float)((endPoints[1][1] - endPoints[1][0]) /
				(endPoints[0][1] - endPoints[0][0]));

	if (endPoints[0][0] < viewport.x)
	{
	    coordinates[pos+0] = (float)viewport.x;
	    coordinates[pos+1] = (float)(endPoints[1][0] + slope *
		(viewport.x - endPoints[0][0]));
	}
	else
	{
	    coordinates[pos+0] = (float)endPoints[0][0];
	    coordinates[pos+1] = (float)endPoints[1][0];
	}
	coordinates[pos+2] = 0.0f;

	if (endPoints[0][1] < viewport.x + viewport.width)
	{
	    coordinates[pos+3] = (float)endPoints[0][1];
	    coordinates[pos+4] = (float)endPoints[1][1];
	}
	else
	{
	    coordinates[pos+3] = (float)(viewport.x + viewport.width);
	    coordinates[pos+4] = (float)(endPoints[1][0] + slope *
		(viewport.x + viewport.width - endPoints[0][0]));
	}
	coordinates[pos+5] = 0.0f;

	return true;
    }


    /**
     * Returns the red value of a given color.
     */
    protected static float
    red(Color color)
    {
	return (float)(color.getRed()/255.0);
    }


    /**
     * Returns the green value of a given color.
     */
    protected static float
    green(Color color)
    {
	return (float)(color.getGreen()/255.0);
    }


    /**
     * Returns the blue value of a given color.
     */
    protected static float
    blue(Color color)
    {
	return (float)(color.getBlue()/255.0);
    }


    /**
     * Returns the display Y value of a given pressure.
     */
    protected static double
    yValue(double pressure)
    {
	double	y = 0;

	try
	{
	    y = SkewTCoordSys.toReference(
		    new double[][] {
			new double[] {pressure},
			new double[] {SkewTCoordSys.minTAtMaxP},
			new double[] {0.0}})[1][0];
	}
	finally
	{
	    return  y;
	}
    }


    /**
     * Indicates whether or not the given display real type is legal
     * for this display renderer.
     *
     * @param type	The display real type to be vetted.
     * @return		<code>true</code> if and only if <code>type</code>
     *			is a legal display real type for this display
     * 			renderer.
     */
    public boolean
    legalDisplayScalar(DisplayRealType type)
    {
	return type.equals(Pressure) ||
	    type.equals(Temperature) ||
	    type.equals(SkewTZAxis) ||
	    super.legalDisplayScalar(type);
    }
}
