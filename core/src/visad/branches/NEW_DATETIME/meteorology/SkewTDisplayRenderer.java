/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTDisplayRenderer.java,v 1.7 1999-01-07 16:13:19 steve Exp $
 */

package visad.meteorology;

import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.SwingPropertyChangeSupport;
import visad.CoordinateSystem;
import visad.DisplayRealType;
import visad.DisplayTupleType;
import visad.FlatField;
import visad.Real;
import visad.SI;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Quantity;
import visad.java2d.DefaultDisplayRendererJ2D;


/**
 * Provides a 2-D VisAD display for a Skew T, Log P Diagram (alias 
 * "Skew-T Chart").
 *
 * Abbreviations:
 *
 * Token	Stands For
 * -----	----------
 * theta	potential temperature
 * thetaES	saturation equivalent potential temperature
 * rSat		saturation mixing-ratio
 *
 * @author Steven R. Emmerson
 */
public class
SkewTDisplayRenderer
    extends	DefaultDisplayRendererJ2D                                               
{
    /*
     * Coordinates:
     */
    public final DisplayRealType		pressure;
    public final DisplayRealType		temperature;
    public final DisplayRealType		zAxis;

    public final DisplayRealType		thetaPressure;
    public final DisplayRealType		theta;
    public final DisplayRealType		thetaZAxis;

    public final DisplayRealType		thetaESPressure;
    public final DisplayRealType		thetaES;
    public final DisplayRealType		thetaESZAxis;

    public final DisplayRealType		rSatPressure;
    public final DisplayRealType		rSat;
    public final DisplayRealType		rSatZAxis;

    /*
     * Vector spaces:
     */
    /**
     * (pressure, temperature, zAxis) vector space.
     */
    public final DisplayTupleType               displaySkewTTuple;
    /**
     * (thetaPressure, theta, thetaZAxis) vector space.
     */
    public final DisplayTupleType		displayThetaTuple;
    /**
     * (thetaESPressure, thetaES, thetaESZAxis) vector space.
     */
    public final DisplayTupleType		displayThetaESTuple;
    /**
     * (rSatPressure, rSat, rSatZAxis) vector space.
     */
    public final DisplayTupleType		displayRSatTuple;

    /*
     * Coordinate transformations:
     */
    /**
     * displaySkewTTuple <-> Display.DisplaySpatialCartesianTuple
     */
    public final SkewTCoordinateSystem		skewTCoordSys;
    /**
     * displayThetaTuple <-> Display.DisplaySpatialCartesianTuple
     */
    public final ThetaCoordinateSystem		thetaCoordSys;
    /**
     * displayThetaESTuple <-> Display.DisplaySpatialCartesianTuple
     
@directed*/
    public final ThetaESCoordinateSystem	thetaESCoordSys;
    /**
     * displayRSatTuple <-> Display.DisplaySpatialCartesianTuple
     */
    public final RSatCoordinateSystem		rSatCoordSys;
    /**
     * (pressure,sounding temperature) <-> Display.DisplaySpatialCartesianTuple
     */
    public final SoundingCoordinateSystem	temperatureSoundingCoordSys;
    /**
     * (pressure,sounding dew point) <-> Display.DisplaySpatialCartesianTuple
     */
    public final SoundingCoordinateSystem	dewPointSoundingCoordSys;

    /**
     * The format for the readouts.
     */
    private final DecimalFormat			format =
	new DecimalFormat("0.0");

    /**
     * The pressure format.
     */
    private final DecimalFormat			pressureFormat = format;

    /**
     * The temperature format.
     */
    private final DecimalFormat			temperatureFormat = format;

    /**
     * The saturation mixing-ratio format.
     */
    private final DecimalFormat			rSatFormat = 
	new DecimalFormat("0.00");

    /**
     * Supports changes to cursor pressure.
     */
    private final SwingPropertyChangeSupport	cursorPressureChanges;


    /**
     * Constructs from nothing.
     */
    public
    SkewTDisplayRenderer()
	throws VisADException
    {
	this(SkewTCoordinateSystem.DEFAULT_MIN_X,
	     SkewTCoordinateSystem.DEFAULT_MAX_X,
	     SkewTCoordinateSystem.DEFAULT_MIN_Y,
	     SkewTCoordinateSystem.DEFAULT_MAX_Y,
	     SkewTCoordinateSystem.DEFAULT_P_UNIT,
	     SkewTCoordinateSystem.DEFAULT_MIN_P,
	     SkewTCoordinateSystem.DEFAULT_MAX_P,
	     SkewTCoordinateSystem.DEFAULT_T_UNIT,
	     SkewTCoordinateSystem.DEFAULT_MIN_T,
	     SkewTCoordinateSystem.DEFAULT_MAX_T,
	     SkewTCoordinateSystem.DEFAULT_TANGENT);
    }


    /**
     * Constructs from display and data parameters.
     *
     * @param minX		The minimum display X coordinate.
     * @param maxX		The maximum display X coordinate.
     * @param minY		The minimum display Y coordinate.
     * @param maxY		The maximum display Y coordinate.
     * @param pressureUnit	The quantity of pressure.
     * @param minP		The minimum pressure coordinate.
     * @param maxP		The maximum pressure coordinate.
     * @param temperatureUnit	The temperature unit.
     * @param minT		The minimum temperature value in units of
     *				<code>temperatureUnit</code>.
     * @param maxT		The maximum temperature value in units of
     *				<code>temperatureUnit</code>.
     * @param tangent		The tangent of the isotherms in display space.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public
    SkewTDisplayRenderer(float minX, float maxX, float minY, float maxY,
	    Unit pressureUnit, float minP, float maxP,
	    Unit temperatureUnit, float minT, float maxT, float tangent)
	throws VisADException
    {
	cursorPressureChanges = new SwingPropertyChangeSupport(this);

	/*
	 * Define (pressure, temperature) vector space.
	 */
	skewTCoordSys = new SkewTCoordinateSystem(minX, maxX, minY, maxY,
	    pressureUnit, minP, maxP, 
	    temperatureUnit, minT, maxT, tangent);
	pressure = new DisplayRealType("SkewTPressure", false, 
	    skewTCoordSys.minP, skewTCoordSys.maxP, 0.0, 
	    skewTCoordSys.getPressureUnit());
	temperature = new DisplayRealType("SkewTTemperature", false,
	    skewTCoordSys.minT, 
	    skewTCoordSys.maxT, 0.0,
	    skewTCoordSys.getTemperatureUnit());
	zAxis = new DisplayRealType("SkewTSkewTZAxis", false,
	    -1.0, 1.0, 0.0, null);
	displaySkewTTuple = new DisplayTupleType(
	    new DisplayRealType[] {pressure, temperature, zAxis},
	    skewTCoordSys);

	/*
	 * Define (pressure, potential temperature) vector space.
	 */
	thetaCoordSys = new ThetaCoordinateSystem(skewTCoordSys);
	thetaPressure = new DisplayRealType("SkewTTheta_Pressure",
	    false, 
	    0.0, Double.POSITIVE_INFINITY, 0.0, 
	    thetaCoordSys.getPressureUnit());
	theta = new DisplayRealType("SkewTPotential_Temperature",
	    false,
	    thetaCoordSys.getThetaUnit().toThis(0., SI.kelvin),
		Double.POSITIVE_INFINITY, 0.0, 
	    thetaCoordSys.getThetaUnit());
	thetaZAxis = new DisplayRealType("SkewTTheta_ZAxis", false,
	    -1.0, 1.0, 0.0, null);
	displayThetaTuple = new DisplayTupleType(
	    new DisplayRealType[] {thetaPressure, theta, thetaZAxis},
	    thetaCoordSys);

	/*
	 * Define (pressure, saturation equivalent potential temperature)
	 * vector space.
	 */
	thetaESCoordSys = new ThetaESCoordinateSystem(thetaCoordSys);
	thetaESPressure = new DisplayRealType("SkewTThetaES_Pressure",
	    false, 
	    0.0, Double.POSITIVE_INFINITY, 0.0, 
	    thetaESCoordSys.getPressureUnit());
	thetaES = new DisplayRealType(
	    "SkewTSaturation_Equivalent_Potential_Temperature",
	    false,
	    thetaESCoordSys.getThetaESUnit().toThis(0., SI.kelvin),
		Double.POSITIVE_INFINITY, 0.0, 
	    thetaESCoordSys.getThetaESUnit());
	thetaESZAxis = new DisplayRealType("SkewTThetaES_ZAxis", false,
	    -1.0, 1.0, 0.0, null);
	displayThetaESTuple = new DisplayTupleType(
	    new DisplayRealType[] {thetaESPressure, thetaES, thetaESZAxis},
	    thetaESCoordSys);

	/*
	 * Define (pressure, saturation mixing ratio) vector space.
	 */
	rSatCoordSys = new RSatCoordinateSystem(skewTCoordSys, 
	    RSatCoordinateSystem.DEFAULT_RSAT_UNIT);
	rSatPressure = new DisplayRealType("SkewTRsat_Pressure",
	    false, 
	    0.0, Double.POSITIVE_INFINITY, 0.0, 
	    rSatCoordSys.getPressureUnit());
	rSat = new DisplayRealType(
	    "SkewTSaturation_Mixing_Ratio",
	    false,
	    0.0, Double.POSITIVE_INFINITY, 0.0, 
	    rSatCoordSys.getRSatUnit());
	rSatZAxis = new DisplayRealType("SkewTRsat_ZAxis", false,
	    -1.0, 1.0, 0.0, null);
	displayRSatTuple = new DisplayTupleType(
	    new DisplayRealType[] {rSatPressure, rSat, rSatZAxis},
	    rSatCoordSys);

	/*
	 * Define sounding vector spaces.
	 */
	temperatureSoundingCoordSys = new SoundingCoordinateSystem(
	    skewTCoordSys, CommonTypes.TEMPERATURE);
	dewPointSoundingCoordSys = new SoundingCoordinateSystem(
	    skewTCoordSys, CommonTypes.DEW_POINT);
    }


    /**
     * Sets the temperature sounding.
     * @param sounding		The temperature sounding or <code>null</code>.
     *				Must have 1-D domain and single range value
     *				with temperature unit.
     * @throws VisADException	Improper sounding.
     */
    public void
    setTemperatureSounding(FlatField sounding)
	throws VisADException
    {
	temperatureSoundingCoordSys.setSounding(sounding);
    }


    /**
     * Sets the dew-point sounding.
     * @param sounding		The dew-point sounding or <code>null</code>.
     *				Must have 1-D domain and single range value
     *				with dew-point unit.
     * @throws VisADException	Improper sounding.
     */
    public void
    setDewPointSounding(FlatField sounding)
	throws VisADException
    {
	dewPointSoundingCoordSys.setSounding(sounding);
    }


    /**
     * Gets the cursor coordinates.
     *
     * @return                  Cursor coordinates in display space.
     *
     * @param cursor            The location of the cursor in display
     *                          coordinates.
     */
    protected final double[][]
    getCursorCoords(double[] cursor)
    {
        return new double[][] {
            new double[] {cursor[0]}, 
            new double[] {cursor[1]}, 
            new double[] {cursor[2]}};
    }


    /**
     * Gets the pressure at the cursor position.
     * @return			The pressure at the cursor position or
     *				<code>null</code> if not available.
     */
    public Real
    getCursorPressure()
    {
	Real	value;
	try
	{
	    value = new Real(
		CommonTypes.PRESSURE,
		skewTCoordSys.fromReference(
		    getCursorCoords(getCursor()))[0][0],
		skewTCoordSys.getPressureUnit());
	}
	catch (Exception e)
	{
	    value = null;
	}
	return value;
    }


    /**
     * Gets the temperature at the cursor position.
     * @return			The temperature at the cursor position or
     *				<code>null</null> if not available.
     */
    public Real
    getCursorTemperature()
    {
	Real	value;
	try
	{
	    value = new Real(
		CommonTypes.TEMPERATURE,
		skewTCoordSys.fromReference(
		    getCursorCoords(getCursor()))[1][0],
		skewTCoordSys.getTemperatureUnit());
	}
	catch (Exception e)
	{
	    value = null;
	}
	return value;
    }


    /**
     * Gets the potential temperature at the cursor position.
     * @return			The potential temperature at the cursor
     *				position or <code>null</code> if not available.
     */
    public Real
    getCursorTheta()
    {
	Real	value;
	try
	{
	    value = new Real(
		CommonTypes.THETA,
		thetaCoordSys.fromReference(
		    getCursorCoords(getCursor()))[1][0],
		thetaCoordSys.getThetaUnit());
	}
	catch (Exception e)
	{
	    value = null;
	}
	return value;
    }


    /**
     * Gets the saturation equivalent potential temperature at the cursor
     * position.
     * @return			The saturation equivalent potential temperature
     *				at the cursor position or <code>null</code> if
     *				not available.
     */
    public Real
    getCursorThetaES()
    {
	Real	value;
	try
	{
	    value = new Real(
		CommonTypes.THETA_ES,
		thetaESCoordSys.fromReference(
		    getCursorCoords(getCursor()))[1][0],
		thetaESCoordSys.getThetaESUnit());
	}
	catch (Exception e)
	{
	    value = null;
	}
	return value;
    }


    /**
     * Gets the saturation mixing ratio at the cursor position.
     * @return			The saturation mixing ratio at the cursor
     *				position or <code>null</code> if not available.
     */
    public Real
    getCursorRSat()
    {
	Real	value;
	try
	{
	    value = new Real(
		CommonTypes.R_SAT,
		rSatCoordSys.fromReference(
		    getCursorCoords(getCursor()))[1][0],
		rSatCoordSys.getRSatUnit());
	}
	catch (Exception e)
	{
	    value = null;
	}
	return value;
    }


    /**
     * Sets strings in the vector that describes the current location of the
     * cursor.
     */
    public void
    setCursorStringVector()
    {
	cursorPressureChanges.firePropertyChange(
	    "Pressure", null, getCursorPressure());
	setCursorStringVector(null);
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
	return type.equals(pressure) ||
	    type.equals(temperature) ||
	    type.equals(theta) ||
	    type.equals(thetaES) ||
	    type.equals(rSat) ||
	    super.legalDisplayScalar(type);
    }


    /**
     * Adds a listener for changes to the cursor pressure.
     * @param listener		The change listener.
     */
    public void
    addCursorPressureChangeListener(PropertyChangeListener listener)
    {
	cursorPressureChanges.addPropertyChangeListener(listener);
    }
}
