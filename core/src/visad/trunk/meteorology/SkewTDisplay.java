/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTDisplay.java,v 1.2 1998-08-12 17:44:33 visad Exp $
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
import visad.Set;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.data.netcdf.QuantityMap;
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
    private final DisplayImplJ2D		display;

    /**
     * The sounding property.
     */
    private Sounding				sounding;

    /**
     * The sounding data-reference.
     */
    private final DataReference			soundingRef;

    /**
     * Whether or not the display has been configured.
     */
    private boolean				displayInitialized;

    /**
     * Supports property changes.
     */
    private final PropertyChangeSupport		changes;

    /**
     * The Skew T, log p coordinate system.
     */
    private final SkewTCoordinateSystem		skewTCoordSys;

    /**
     * The interval between isotherms.
     */
    private final float				deltaTemperature = 5f;

    /**
     * The base isotherm.
     */
    private final float				baseIsotherm = 0f;


    /**
     * Constructs from nothing.
     */
    public
    SkewTDisplay()
	throws	VisADException, RemoteException
    {
	sounding = null;
	soundingRef = new DataReferenceImpl("soundingRef");
	displayInitialized = false;
	changes = new PropertyChangeSupport(this);
	display = new DisplayImplJ2D("Skew T, Log P Diagram",
				     new SkewTDisplayRenderer());
	skewTCoordSys = SkewTDisplayRenderer.SkewTCoordSys;

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

	    DataReferenceImpl	isothermRef = configureDisplayForIsotherms();

	    display.addReference(soundingRef);
	    display.addReference(isothermRef);

	    displayInitialized = true;
	}
    }


    /**
     * Configure display for isotherms.
     *
     * @return	Display data-reference to isotherms.
     */
    protected DataReferenceImpl
    configureDisplayForIsotherms()
	throws	VisADException, RemoteException
    {
	/*
	 * Define a field of temperature which will result in correct
	 * isotherm contours.
	 */
	MathType	domainType = 
	    new RealTupleType(new RealType[] {RealType.XAxis, RealType.YAxis});
	RealType	rangeType = new RealType("temperature contour");
	FunctionType	funcType = new FunctionType(domainType, rangeType);
	Set		set = new Linear2DSet(
	    domainType,
	    skewTCoordSys.viewport.x, 
	    skewTCoordSys.viewport.x + skewTCoordSys.viewport.width, 2,
	    skewTCoordSys.viewport.y,
	    skewTCoordSys.viewport.y + skewTCoordSys.viewport.height, 2);
	FlatField	temperature = new FlatField(funcType, set);

	float[][]	xyCoords = set.getSamples(/*copy=*/false);
	float[][]	ptCoords = skewTCoordSys.fromReference(
	    new float[][] {xyCoords[0], xyCoords[1], new float[] {}});

	temperature.setSamples(new float[][] {ptCoords[1]}, /*copy=*/false);

	/*
	 * Map the X and Y coordinates of the field to the display X and Y
	 * coordinates.
	 */
	ScalarMap	xMap = new ScalarMap(RealType.XAxis, Display.XAxis);
	ScalarMap	yMap = new ScalarMap(RealType.YAxis, Display.YAxis);

	display.addMap(xMap);
	display.addMap(yMap);

	/*
	 * Establish the isotherm contours.
	 */
	ScalarMap	contourMap = new ScalarMap(rangeType,
	    Display.IsoContour);
	display.addMap(contourMap);

	ContourControl	control = (ContourControl)contourMap.getControl();
	control.setContourInterval(deltaTemperature,
	    (float)skewTCoordSys.minTAtMinP,
	    (float)skewTCoordSys.maxTAtMaxP, baseIsotherm);

	/*
	 * Create a data-reference for the contours.
	 */
	DataReferenceImpl	isothermRef =
	    new DataReferenceImpl("isothermRef");
	isothermRef.setData(temperature);

	return isothermRef;
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
	    SkewTDisplayRenderer.Pressure);
	ScalarMap	temperatureMap =
	    new ScalarMap(sounding.getTemperatureType(),
			  SkewTDisplayRenderer.Temperature);

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
