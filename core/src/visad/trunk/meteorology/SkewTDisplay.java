/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTDisplay.java,v 1.5 1998-08-19 22:13:34 steve Exp $
 */

package visad.meteorology;

import com.sun.java.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.rmi.RemoteException;
import visad.ConstantMap;
import visad.ContourControl;
import visad.CoordinateSystem;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayRealType;
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
	    /*
	     * Map the X and Y coordinates of the various background fields
	     * to the display X and Y coordinates.  This is necessary for
	     * contouring of the various fields -- though it does have the
	     * unfortunate effect of making XAxis and YAxis value readouts
	     * appear.
	     */
	    ScalarMap	xMap = new ScalarMap(RealType.XAxis, Display.XAxis);
	    ScalarMap	yMap = new ScalarMap(RealType.YAxis, Display.YAxis);
	    display.addMap(xMap);
	    display.addMap(yMap);

	    /*
	     * Map the sounding types to display types.  NB: Because of this
	     * mapping, the display will have a value readout for regular
	     * temperature values.  Consequently, we don't need to create
	     * one for the regular temperature field.
	     */
	    ScalarMap	soundingPressureMap = new ScalarMap(
		sounding.getPressureType(), displayRenderer.pressure);
	    soundingPressureMap.setRangeByUnits();
	    display.addMap(soundingPressureMap);

	    ScalarMap	soundingTemperatureMap = new ScalarMap(
		sounding.getTemperatureType(), displayRenderer.temperature);
	    soundingTemperatureMap.setRangeByUnits();
	    display.addMap(soundingTemperatureMap);

	    /*
	     * Define the temperature type for the various
	     * temperature fields which will be contoured.
	     */
	    RealType	isothermType = new RealType("isotherm");

	    /*
	     * Map the temperature parameter of the various temperature
	     * fields to display contours.
	     */
	    ScalarMap	contourMap = new ScalarMap(isothermType,
		Display.IsoContour);
	    display.addMap(contourMap);
	    ContourControl	control =
		(ContourControl)contourMap.getControl();
	    Unit	temperatureUnit = skewTCoordSys.getTemperatureUnit();
	    control.setContourInterval(deltaTemperature,
		(float)temperatureUnit.toThis(0.f, SI.kelvin),
		(float)temperatureUnit.toThis(500f, SI.kelvin),
		0f);

	    /*
	     * Establish value readouts for the non-sounding temperature
	     * parameters.
	     */
	    RealType	type = new RealType("potential_temperature");
	    ScalarMap	fieldTemperatureMap = new ScalarMap(type, 
		displayRenderer.potentialTemperature);
	    display.addMap(fieldTemperatureMap);

	    /*
	     * Create the temperature fields.
	     */
	    DataReferenceImpl	temperatureRef = 
		createTemperatureField("temperature", isothermType,
		    skewTCoordSys.viewport, 2, 2, skewTCoordSys);
	    DataReferenceImpl	potentialTemperatureRef =
		createTemperatureField("potential_temperature", isothermType,
		    potentialTemperatureCoordSys.viewport, 10, 10, 
		    potentialTemperatureCoordSys);
	    /*
	    DataReferenceImpl	equivalentPotentialTemperatureRef = 
		createTemperatureField("equivalent_potential_temperature",
		    isothermType,
		    equivalentPotentialTemperatureCoordSys.viewport, 10, 10, 
		    equivalentPotentialTemperatureCoordSys);
	    */

	    /*
	     * Add the temperature fields to the display.
	     */
	    display.addReference(soundingRef);
	    display.addReference(temperatureRef);
	    display.addReference(potentialTemperatureRef);
	    // display.addReference(equivalentPotentialTemperatureRef);

	    displayInitialized = true;
	}
    }


    /**
     * Creates a particular temperature field.
     *
     * @param name		The name of the parameter (e.g. 
     *				"potential_temperature") for the purpose of
     *				naming the returned DdataReference.
     * @param rangeType		The RealType of the parameter.
     * @param viewport		The display viewport.
     * @param nx		The number of samples in the X dimension.
     * @param ny		The number of samples in the Y dimension.
     * @param coordSys		The coordinate system transform.
     * @return			Data reference to created field.
     * @exception VisADException	Can't create necessary VisAD object.
     * @exception RemoteException	Remote access failure.
     */
    protected DataReferenceImpl
    createTemperatureField(String name, RealType rangeType,
	    Rectangle2D viewport, int nx, int ny, CoordinateSystem coordSys)
	throws	VisADException, RemoteException
    {
	MathType	domainType = 
	    new RealTupleType(new RealType[] {RealType.XAxis, RealType.YAxis});
	FunctionType	funcType = new FunctionType(domainType, rangeType);
	Linear2DSet	set = new Linear2DSet(domainType,
	    viewport.getX(), viewport.getX() + viewport.getWidth(), nx,
	    viewport.getY(), viewport.getY() + viewport.getHeight(), ny);
	FlatField	temperature = new FlatField(funcType, set);
	float[][]	xyCoords = set.getSamples();
	float[][]	ptCoords = coordSys.fromReference(
	    new float[][] {xyCoords[0], xyCoords[1], null});

	temperature.setSamples(new float[][] {ptCoords[1]}, /*copy=*/false);

	/*
	 * Create a data-reference for the temperature field.
	 */
	DataReferenceImpl	temperatureRef =
	    new DataReferenceImpl(name + "_ref");
	temperatureRef.setData(temperature);

	return temperatureRef;
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
