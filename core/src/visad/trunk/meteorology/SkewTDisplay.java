/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTDisplay.java,v 1.4 1998-08-19 17:19:58 steve Exp $
 */

package visad.meteorology;

import com.sun.java.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.rmi.RemoteException;
import visad.ConstantMap;
import visad.ContourControl;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.FlatField;
import visad.FunctionType;
import visad.Linear2DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.SI;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.data.netcdf.QuantityMap;
import visad.data.netcdf.units.Parser;
import visad.java2d.DisplayImplJ2D;

/**
 * VisAD display bean for a Skew T, Log P Diagram (alias "Skew-T Chart").
 *
 * @author Steven R. Emmerson
 */
public class
SkewTDisplay
    implements	Serializable
{
    /**
     * The VisAD display.
     */
    private final DisplayImplJ2D	display;

    /**
     * The sounding property.
     */
    private Sounding			sounding;

    /**
     * The sounding data-reference.
     */
    private final DataReference		soundingRef;

    /**
     * Whether or not the display has been configured.
     */
    private boolean			displayInitialized;

    /**
     * Supports property changes.
     */
    private final PropertyChangeSupport	changes;

    /**
     * The Skew T, log p coordinate system.
     */
    private final SkewTCoordinateSystem	skewTCoordSys;

    /**
     * The potential temperature coordinate system.
     */
    private final PotentialTemperatureCoordSys	potentialTemperatureCoordSys;

    /**
     * The interval between isotherms.
     */
    private final float			deltaTemperature = 10f;

    /**
     * The interval between potential isotherms.
     */
    private final float			deltaPotentialTemperature = 10f;

    /**
     * The base isotherm.
     */
    private final float			baseIsotherm = 0f;

    /**
     * The DisplayRenderer.
     */
    private final SkewTDisplayRenderer	displayRenderer;


    /**
     * Constructs from nothing.
     */
    public
    SkewTDisplay()
	throws	VisADException, RemoteException
    {
	displayRenderer = new SkewTDisplayRenderer();

	sounding = null;
	soundingRef = new DataReferenceImpl("soundingRef");
	displayInitialized = false;
	changes = new PropertyChangeSupport(this);
	display = new DisplayImplJ2D("Skew T, Log P Diagram",
				     displayRenderer);
	skewTCoordSys = displayRenderer.skewTCoordSys;
	potentialTemperatureCoordSys =
	    displayRenderer.potentialTemperatureCoordSys;

	JFrame jframe = new JFrame("Skew-T Chart");
	jframe.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {System.exit(0);}
	});
	jframe.getContentPane().add(display.getComponent());
	jframe.setSize(256, 256);
	jframe.setVisible(true);
    }


    /**
     * Displays a given sounding.
     */
    protected void
    display(Sounding sounding)
	throws	RemoteException, VisADException
    {
	soundingRef.setData(sounding);

	if (!displayInitialized)
	{
	    configureDisplayForSounding(sounding);

	    /*
	     * Map the X and Y coordinates of the various background fields
	     * to the display X and Y coordinates.  This is necessary for
	     * contouring of the various fields -- though it does have the
	     * unfortunate effect of making XAxis and YAxis middle-mouse-
	     * button readouts appear.
	     */
	    ScalarMap	xMap = new ScalarMap(RealType.XAxis, Display.XAxis);
	    ScalarMap	yMap = new ScalarMap(RealType.YAxis, Display.YAxis);
	    display.addMap(xMap);
	    display.addMap(yMap);

	    DataReferenceImpl	temperatureRef = 
		configureDisplayForTemperature();
	    DataReferenceImpl	potentialTemperatureRef =
		configureDisplayForPotentialTemperature();

	    display.addReference(soundingRef);
	    display.addReference(temperatureRef);
	    display.addReference(potentialTemperatureRef);

	    displayInitialized = true;
	}
    }


    /**
     * Configure display for temperature field.
     *
     * @return	Display data-reference to temperature field.
     */
    protected DataReferenceImpl
    configureDisplayForTemperature()
	throws	VisADException, RemoteException
    {
	/*
	 * Define a field of temperature which will result in correct
	 * isotherm contours.
	 */
	MathType	domainType = 
	    new RealTupleType(new RealType[] {RealType.XAxis, RealType.YAxis});
	RealType	rangeType = new RealType("temperature_contour");
	FunctionType	funcType = new FunctionType(domainType, rangeType);
	Set		set = new Linear2DSet(
	    domainType,
	    skewTCoordSys.viewport.getX(), 
	    skewTCoordSys.viewport.getX() + skewTCoordSys.viewport.getWidth(),
	    2,
	    skewTCoordSys.viewport.getY(),
	    skewTCoordSys.viewport.getY() + skewTCoordSys.viewport.getHeight(),
	    2);
	FlatField	temperature = new FlatField(funcType, set);
	float[][]	xyCoords = set.getSamples(/*copy=*/false);
	float[][]	ptCoords = skewTCoordSys.fromReference(
	    new float[][] {xyCoords[0], xyCoords[1], null});

	temperature.setSamples(new float[][] {ptCoords[1]}, /*copy=*/false);

	/*
	 * Establish the isotherm contours.
	 */
	ScalarMap	contourMap = new ScalarMap(rangeType,
	    Display.IsoContour);
	display.addMap(contourMap);

	ContourControl	control = (ContourControl)contourMap.getControl();
	control.setContourInterval(deltaTemperature,
	    (float)skewTCoordSys.getTemperatureUnit().toThis(0.f, SI.kelvin),
	    500f,
	    baseIsotherm);

	/*
	 * Create a data-reference for the contours.
	 */
	DataReferenceImpl	isothermRef =
	    new DataReferenceImpl("isothermRef");
	isothermRef.setData(temperature);

	return isothermRef;
    }


    /**
     * Configure display for potential temperature.
     *
     * @return	Display data-reference to isotherms.
     */
    protected DataReferenceImpl
    configureDisplayForPotentialTemperature()
	throws	VisADException, RemoteException
    {
	Unit	kelvin = SI.kelvin;
	Unit	skewTTempUnit = skewTCoordSys.getTemperatureUnit();

	/*
	 * Define a field of potential temperature which will result in correct
	 * isotherm contours.
	 */
	int		nx = 10;
	int		ny = 10;
	MathType	domainType = 
	    new RealTupleType(new RealType[] {RealType.XAxis, RealType.YAxis});
	RealType	rangeType =
	    RealType.getRealTypeByName("temperature_contour");	// prev. defined
	    // new RealType("potential_temperature", 
	    // potentialTemperatureCoordSys.getTemperatureUnit(),
	    // /*(Set)*/null);
	FunctionType	funcType = new FunctionType(domainType, rangeType);
	Linear2DSet	set = new Linear2DSet(
	    domainType,
	    potentialTemperatureCoordSys.viewport.getX(), 
	    potentialTemperatureCoordSys.viewport.getX() +
		potentialTemperatureCoordSys.viewport.getWidth(),
	    nx,
	    potentialTemperatureCoordSys.viewport.getY(),
	    potentialTemperatureCoordSys.viewport.getY() +
		potentialTemperatureCoordSys.viewport.getHeight(),
	    ny);
	FlatField	potentialTemperature = new FlatField(funcType, set);
	float[][]	xyCoords = set.getSamples();
	float[][]	ptCoords = potentialTemperatureCoordSys.fromReference(
	    new float[][] {xyCoords[0], xyCoords[1], null});

	potentialTemperature.setSamples(new float[][] {ptCoords[1]},
	    /*copy=*/false);

	/*
	 * Establish middle-mouse-button readout.
	 */
	ScalarMap	thetaMap = 
	    new ScalarMap(new RealType("potential_temperature"),
		displayRenderer.potentialTemperature);
	/*
         * The following statement causes middle-mouse-button readout
         * of potential temperature to appear.  Unfortunately, it also
         * causes the potential temperature contours to not be drawn.
	 */
	display.addMap(thetaMap);

	/*
	 * Create a data-reference for the potential temperature.
	 */
	DataReferenceImpl	potentialTemperatureRef =
	    new DataReferenceImpl("potentialTemperatureRef");
	potentialTemperatureRef.setData(potentialTemperature);

	return potentialTemperatureRef;
    }


    /**
     * Configure display for soundings.
     *
     * @param sounding	The sounding.
     */
    protected void
    configureDisplayForSounding(Sounding sounding)
	throws	VisADException, RemoteException
    {
	ScalarMap	pressureMap = new ScalarMap(sounding.getPressureType(),
	    displayRenderer.pressure);
	ScalarMap	temperatureMap =
	    new ScalarMap(sounding.getTemperatureType(),
			  displayRenderer.temperature);

	pressureMap.setRangeByUnits();
	temperatureMap.setRangeByUnits();

	display.addMap(pressureMap);
	display.addMap(temperatureMap);
    }


    /**
     * Sets the sounding property.
     */
    public synchronized void
    setSounding(Sounding sounding)
    {
	try
	{
	    Sounding	oldSounding = this.sounding;

	    display(sounding);
	    this.sounding = sounding;
	    changes.firePropertyChange("sounding", oldSounding, sounding);
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println("Couldn't display sounding {" +
		sounding + "}" + (reason == null ? "" : (": " + reason)));
	}
    }


    /**
     * Tests this class.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	/*
	 * Create and display a Skew T, Log P Diagram.
	 */
	SkewTDisplay	display = new SkewTDisplay();

	QuantityMap.push(MetQuantityDB.instance());

	display.setSounding(
	    new Sounding((FlatField)new Plain().open("sounding.nc")));
    }
}
