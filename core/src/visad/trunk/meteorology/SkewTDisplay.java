/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SkewTDisplay.java,v 1.13 1998-11-03 22:27:35 steve Exp $
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
import visad.FlatField;
import visad.FunctionType;
import visad.GraphicsModeControl;
import visad.Integer1DSet;
import visad.Linear2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.SI;
import visad.ScalarMap;
import visad.ScaledUnit;
import visad.Set;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.data.netcdf.QuantityMap;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;
import visad.java2d.DirectManipulationRendererJ2D;
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
     * Whether or not the temperature sounding has bee modified.
     */
    private boolean				temperatureSoundingIsDirty =
	false;

    /**
     * Whether or not the dew-point sounding has bee modified.
     */
    private boolean				dewPointSoundingIsDirty =
	false;

    /**
     * The original Sounding.
     */
    private Sounding				originalSounding;

    /**
     * The edited Sounding.
     */
    private Sounding				editedSounding;

    /**
     * The empty temperature sounding.
     */
    private static final FlatField		emptyTemperatureSounding;

    /**
     * The empty dew-point sounding.
     */
    private static final FlatField		emptyDewPointSounding;

    /**
     * The VisAD display.
     */
    private final DisplayImplJ2D		display;

    /**
     * The temperature-sounding data-reference.
     */
    private final DataReference			temperatureSoundingRef;

    /**
     * The dewPoint-sounding data-reference.
     */
    private final DataReference			dewPointSoundingRef;

    /**
     * Supports property changes.
     */
    private final PropertyChangeSupport		changes;

    /**
     * The Skew T, log p coordinate system.
     */
    private final SkewTCoordinateSystem		skewTCoordSys;

    /**
     * The potential temperature coordinate system.
     */
    private final ThetaCoordinateSystem		thetaCoordSys;

    /**
     * The saturation equivalent potential temperature coordinate system.
     */
    private final ThetaESCoordinateSystem	thetaESCoordSys;

    /**
     * The saturation mixing-ratio coordinate system.
     */
    private final RSatCoordinateSystem		rSatCoordSys;

    /**
     * The interval between isotherms.
     */
    private static final float			deltaTemperature = 10f;

    /**
     * The DisplayRenderer.
     */
    private final SkewTDisplayRenderer		displayRenderer;

    /**
     * Temperature sounding constant maps.
     */
    private ConstantMap[]			temperatureSoundingConstantMaps;

    /**
     * DewPoint sounding constant maps
     */
    private ConstantMap[]			dewPointSoundingConstantMaps;

    /**
     * Saturation equivalent potential temperature constant maps.
     */
    private ConstantMap[]			thetaESConstantMaps;

    /**
     * Saturation mixing-ratio constant maps.
     */
    private ConstantMap[]			rSatConstantMaps;

    /**
     * Data references.
     */
    private DataReferenceImpl			bgPressureRef;
    private DataReferenceImpl			bgTemperatureRef;
    private DataReferenceImpl			thetaRef;
    private DataReferenceImpl			thetaESRef;
    private DataReferenceImpl			rSatRef;

    /*
     * Types.
     */
    private RealType				bgTemperatureType;
    private RealTupleType			domainType;
	/**@shapeType DependencyLink*/
	/*#  SkewTDisplay$1 lnkUnnamed1*/
	/**@shapeType DependencyLink*/
	/*#  CommonUnits lnkUnnamed2*/
	/**@shapeType DependencyLink*/
	/*#  CommonTypes lnkUnnamed3*/
	/**@shapeType DependencyLink*/
	/*#  SoundingException lnkUnnamed4*/


    static
    {
	FlatField	temperatureSounding = null;
	FlatField	dewPointSounding = null;

	try
	{
	    temperatureSounding = new TemperatureSoundingImpl();
	    dewPointSounding = new DewPointSoundingImpl();
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println("Couldn't initialize SkewTDisplay class" +
		(reason == null ? "" : ": " + reason));
	    e.printStackTrace();
	}

	emptyTemperatureSounding = temperatureSounding;
	emptyDewPointSounding = dewPointSounding;
    }


    /**
     * Constructs from nothing.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote access failure.
     */
    public
    SkewTDisplay()
	throws	VisADException, RemoteException
    {
	displayRenderer = new SkewTDisplayRenderer();

	temperatureSoundingRef =
	    new DataReferenceImpl("temperatureSoundingRef");
	dewPointSoundingRef = new DataReferenceImpl("dewPointSoundingRef");

	changes = new PropertyChangeSupport(this);

	display = new DisplayImplJ2D("Skew T, Log P Diagram", displayRenderer);

	JFrame	jframe = new JFrame("Skew-T Chart");
	jframe.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {System.exit(0);}
	});
	jframe.setSize(512, 512);
	jframe.setVisible(true);
	jframe.getContentPane().add(display.getComponent());

	skewTCoordSys = displayRenderer.skewTCoordSys;
	thetaCoordSys = displayRenderer.thetaCoordSys;
	thetaESCoordSys = displayRenderer.thetaESCoordSys;
	rSatCoordSys = displayRenderer.rSatCoordSys;

	domainType = new RealTupleType(RealType.XAxis, RealType.YAxis);

	temperatureSoundingRef.setData(emptyTemperatureSounding);
	dewPointSoundingRef.setData(emptyDewPointSounding);

	defineScalarMaps();

	/*
	 * Define the background.
	 */
	definePressureBackground();
	defineTemperatureBackground();
	/*
         * Hold off on displaying isopleths of saturation mixing-ratio 
	 * until ContourControl-s can contour arbitrary isopleths (the
	 * contour values don't form an arithmetic series).
	 */
	// defineRSatBackground();

	/*
	 * Add the various data references to the display.
	 */
	display.addReference(bgPressureRef);
	display.addReference(bgTemperatureRef);
	display.addReference(thetaRef);
	display.addReference(thetaESRef, thetaESConstantMaps);
	// display.addReference(rSatRef, rSatConstantMaps);
	display.addReferences(new DirectManipulationRendererJ2D(),
	    temperatureSoundingRef, temperatureSoundingConstantMaps);
	display.addReferences(new DirectManipulationRendererJ2D(),
	    dewPointSoundingRef, dewPointSoundingConstantMaps);
    }


    /**
     * Define the pressure background.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected void
    definePressureBackground()
	throws VisADException, RemoteException
    {
	/*
	 * Create the background pressure field.
	 *
	 * Defining the pressure only at the corners of a linear
	 * pressure domain won't work because VisAD transforms the
	 * corner locations and values before doing the contouring
	 * using linear interpolation.
	 */

	RealType	bgPressureType = new RealType(
	    "bgPressureType", CommonTypes.PRESSURE.getDefaultUnit(), null);

	FunctionType	funcType = new FunctionType(domainType, bgPressureType);
	Linear2DSet	domainSet = new Linear2DSet(-1., 1., 2, -1., 1., 100);
	FlatField	bgPressure = new FlatField(funcType, domainSet);
	float[]		pressures = domainSet.getSamples()[1];
	float		scale =
	    (float)(2./Math.log(skewTCoordSys.maxP/skewTCoordSys.minP));

	for (int i = 0; i < pressures.length; ++i)
	    pressures[i] = (float)(skewTCoordSys.minP *
		Math.exp((1. - pressures[i]) / scale));

	bgPressure.setSamples(new float[][] {pressures}, false);
	bgPressureRef = new DataReferenceImpl("bgPressure_ref");
	bgPressureRef.setData(bgPressure);

	/**
	 * Define contouring of the background pressure.
	 */
	ScalarMap	bgPressureMap =
	    new ScalarMap(bgPressureType, Display.IsoContour);
	display.addMap(bgPressureMap);
	ContourControl	control = (ContourControl)bgPressureMap.getControl();
	control.setContourInterval(50f, 1f, Float.POSITIVE_INFINITY, 1000f);
	control.enableLabels(false);
    }


    /**
     * Define the saturation mixing-ratio background.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected void
    defineRSatBackground()
	throws VisADException, RemoteException
    {
	/*
	 * Create the background saturation mixing-ratio field.
	 */
	RealType	bgRSatType =
	    new RealType("bgRsat", rSatCoordSys.getRSatUnit(), null);
	FunctionType	funcType = new FunctionType(domainType, bgRSatType);
	Linear2DSet	domainSet = new Linear2DSet(-1., 1., 20, -1., 1., 20);
	FlatField	rSat = new FlatField(funcType, domainSet);
	float[][]	samples = domainSet.getSamples();

	rSat.setSamples(new float[][] {
	    rSatCoordSys.fromReference(samples)[1]}, false);

	rSatRef = new DataReferenceImpl("Rsat_ref");
	rSatRef.setData(rSat);

	/*
	 * Define contouring of the background saturation mixing-ratio.
	 */
	ScalarMap	bgRSatMap =
	    new ScalarMap(bgRSatType, Display.IsoContour);
	display.addMap(bgRSatMap);
	ContourControl	control = (ContourControl)bgRSatMap.getControl();
	control.setContourInterval(5f, 0f, 1000f, 5f);
	control.enableLabels(false);
    }


    /**
     * Define the temperature backgrounds.
     */
    protected void
    defineTemperatureBackground()
	throws RemoteException, VisADException
    {
	RealType	bgTemperatureType = new RealType(
	    "bgTemperature", CommonTypes.TEMPERATURE.getDefaultUnit(), null);

	/*
	 * Create the regular temperature background.
	 */
	bgTemperatureRef = createTemperatureField("temperature",
	    bgTemperatureType,
	    skewTCoordSys.minX, skewTCoordSys.maxX, 2,
	    skewTCoordSys.minY, skewTCoordSys.maxY, 2,
	    skewTCoordSys);

	/*
	 * Create the potential temperature background.
	 */
	thetaRef = createTemperatureField("theta",
	    bgTemperatureType,
	    skewTCoordSys.minX, skewTCoordSys.maxX, 10,
	    skewTCoordSys.minY, skewTCoordSys.maxY, 10,
	    thetaCoordSys);

	/*
	 * Create the saturation potential temperature background.
	 */
	thetaESRef = createTemperatureField("thetaES",
	    bgTemperatureType,
	    skewTCoordSys.minX, skewTCoordSys.maxX, 20,
	    skewTCoordSys.minY, skewTCoordSys.maxY, 20,
	    thetaESCoordSys);

	/*
	 * Define contouring of the background temperatures.
	 */
	ScalarMap	contourMap =
	    new ScalarMap(bgTemperatureType, Display.IsoContour);
	display.addMap(contourMap);
	ContourControl	control = (ContourControl)contourMap.getControl();
	control.setContourInterval(deltaTemperature, -273.15f,
	    Float.POSITIVE_INFINITY, 0f);
	control.enableLabels(false);
    }


    /**
     * Creates a particular temperature field.  Maps display points to
     * coordinate system points and stores the temperature values in the
     * field's range.
     *
     * @param name		The name of the parameter (e.g. 
     *				"potential_temperature") for the purpose of
     *				naming the returned DdataReference.
     * @param rangeType		The RealType of the parameter.
     * @param minX		The minimum display X.
     * @param maxX		The maximum display X.
     * @param nx		The number of samples in the X dimension.
     * @param minY		The minimum display Y.
     * @param maxY		The maximum display Y.
     * @param ny		The number of samples in the Y dimension.
     * @param coordSys		The coordinate system transform.
     * @return			Data reference to created field.
     * @exception VisADException	Can't create necessary VisAD object.
     * @exception RemoteException	Remote access failure.
     */
    protected DataReferenceImpl
    createTemperatureField(String name, RealType rangeType,
	    float minX, float maxX, int nx,
	    float minY, float maxY, int ny,
	    CoordinateSystem coordSys)
	throws	VisADException, RemoteException
    {
	FunctionType	funcType = new FunctionType(domainType, rangeType);
	Linear2DSet	set = new Linear2DSet(domainType,
					      minX, maxX, nx, minY, maxY, ny);
	FlatField	temperature = new FlatField(funcType, set);
	float[][]	xyCoords = set.getSamples();
	float[][]	ptCoords = coordSys.fromReference(
	    new float[][] {xyCoords[0], xyCoords[1], null});

	temperature.setSamples(new float[][] {ptCoords[1]}, /*copy=*/false);

	/*
	 * Create a data-reference for the temperature field.
	 */
	DataReferenceImpl	ref = new DataReferenceImpl(name + "_ref");
	ref.setData(temperature);

	return ref;
    }


    /*
     * Define the data-independent VisAD mappings for this display.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected void
    defineScalarMaps()
	throws VisADException, RemoteException
    {
	temperatureSoundingConstantMaps = new ConstantMap[] {
	    new ConstantMap(1., Display.Red),
	    new ConstantMap(0., Display.Blue),
	    new ConstantMap(0., Display.Green),
	    new ConstantMap(3.0, Display.LineWidth)
	};

	dewPointSoundingConstantMaps = new ConstantMap[] {
	    new ConstantMap(0., Display.Red),
	    new ConstantMap(1., Display.Blue),
	    new ConstantMap(0., Display.Green),
	    new ConstantMap(3.0, Display.LineWidth)
	};

	thetaESConstantMaps = new ConstantMap[] {
	    new ConstantMap(0., Display.Red),
	    new ConstantMap(0., Display.Blue),
	    new ConstantMap(1., Display.Green)
	};

	rSatConstantMaps = new ConstantMap[] {
	    new ConstantMap(0., Display.Red),
	    new ConstantMap(0., Display.Blue),
	    new ConstantMap(1., Display.Green)
	};

	/*
         * Map the X and Y coordinates of the various background fields
         * to the display X and Y coordinates.  This is necessary for
         * contouring the background fields and drawing the soundings.
	 */
	display.addMap(new ScalarMap(RealType.XAxis, Display.XAxis));
	display.addMap(new ScalarMap(RealType.YAxis, Display.YAxis));

	/*
	 * Map the other types to display types.
	 */
	ScalarMap	pressureMap =
	    new ScalarMap(CommonTypes.PRESSURE, displayRenderer.pressure);
	display.addMap(pressureMap);
	pressureMap.setRangeByUnits();

	ScalarMap	temperatureMap =
	    new ScalarMap(CommonTypes.TEMPERATURE, displayRenderer.temperature);
	display.addMap(temperatureMap);
	temperatureMap.setRangeByUnits();

	ScalarMap	dewPointMap =
	    new ScalarMap(CommonTypes.DEW_POINT, displayRenderer.temperature);
	dewPointMap.setRangeByUnits();
	display.addMap(dewPointMap);

	display.addMap(new ScalarMap(CommonTypes.THETA,
	    displayRenderer.theta));
	display.addMap(new ScalarMap(CommonTypes.THETA_ES,
	    displayRenderer.thetaES));
	display.addMap(new ScalarMap(CommonTypes.R_SAT,
	    displayRenderer.rSat));
    }


    /**
     * Sets the temperature sounding.
     *
     * @param sounding		Temperature sounding.  Must be single 
     *				temperature parameter defined over 1-D
     *				pressure domain.
     */
    protected void
    setTemperatureSounding(TemperatureSoundingImpl sounding)
    {
	try
	{
	    if (sounding == null)
	    {
		temperatureSoundingRef.setData(emptyTemperatureSounding);
		displayRenderer.setTemperatureSounding(
		    emptyTemperatureSounding);
	    }
	    else
	    {
		temperatureSoundingRef.setData(sounding);
		displayRenderer.setTemperatureSounding(sounding);
	    }
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println(
		"Couldn't display temperature sounding" +
		(reason == null ? "" : (": " + reason)));
	}
    }


    /**
     * Sets the dew-point sounding.
     *
     * @param sounding		Dew-point sounding.  Must be single 
     *				dew-point parameter defined over 1-D
     *				pressure domain.
     */
    protected void
    setDewPointSounding(DewPointSoundingImpl sounding)
    {
	try
	{
	    if (sounding == null)
	    {
		dewPointSoundingRef.setData(emptyDewPointSounding);
		displayRenderer.setDewPointSounding(emptyDewPointSounding);
	    }
	    else
	    {
		dewPointSoundingRef.setData(sounding);
		displayRenderer.setDewPointSounding(sounding);
	    }
	}
	catch (Exception e)
	{
	    String	reason = e.getMessage();

	    System.err.println(
		"Couldn't display dew-point sounding" +
		(reason == null ? "" : (": " + reason)));
	}
    }


    /**
     * Vets a single-parameter sounding.
     *
     * @param sounding		The sounding to be vetted.  Must single-
     *				parameter <code>desiredRangeType</code> defined
     *				over 1-D pressure domain.
     * @throws SoundingException	Invalid sounding.
     */
    protected static void
    vetSounding(FlatField sounding, RealType desiredRangeType)
	throws SoundingException
    {
	FunctionType	soundingType = (FunctionType)sounding.getType();
	RealType[]	soundingDomain =
	    soundingType.getDomain().getRealComponents();

	if (soundingDomain.length != 1)
	    throw new SoundingException("Temperature sounding isn't 1-D");

	if (!soundingDomain[0].equals(CommonTypes.PRESSURE))
	    throw new SoundingException("Sounding domain isn't pressure");

	RealType[]	soundingRangeTypes =
	    soundingType.getFlatRange().getRealComponents();

	if (soundingRangeTypes.length != 1)
	    throw new SoundingException(
		"Sounding range isn't single-parameter");

	if (!soundingRangeTypes[0].equals(desiredRangeType))
	    throw new SoundingException(
		"Sounding parameter isn't appropriate");
    }


    /**
     * Sets the sounding property.
     *
     * @param sounding		The atmospheric sounding.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized void
    setSounding(SoundingImpl sounding)
	throws RemoteException, VisADException
    {
	originalSounding = sounding;
	// TODO: clone original sounding
	editedSounding = sounding;

	setTemperatureSounding(
	    (TemperatureSoundingImpl)editedSounding.getTemperatureSounding());
	setDewPointSounding(
	    (DewPointSoundingImpl)editedSounding.getDewPointSounding());
    }


    /**
     * Gets the sounding property.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote access failure.
     */
    public synchronized Sounding
    getSounding()
	throws VisADException, RemoteException
    {
	return new SoundingImpl(
	    new SingleSounding[] {
		(SingleSounding)temperatureSoundingRef.getData(),
		(SingleSounding)dewPointSoundingRef.getData()});
    }


    /**
     * Tests this class by creating a Skew T, Log P Diagram and displaying
     * a sounding on it.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	SkewTDisplay	display = new SkewTDisplay();
	Plain		plain = new Plain();

	QuantityMap.push(MetQuantityDB.instance());

	/*
	 * Simulate arbitrary setting of the sounding property by 
	 * looping over a single sounding.
	for (;;)
	 */
	{
	    FlatField		field;
	    SoundingImpl	sounding;

	    /*
	    field = (FlatField)plain.open("soundingA.nc");
	    sounding = new SoundingImpl(field, CommonTypes.PRESSURE,
		CommonTypes.TEMPERATURE, CommonTypes.DEW_POINT);
	    display.setSounding(sounding);
	    java.lang.Thread.sleep(5000);
	    */

	    field = (FlatField)plain.open("sounding.nc");
	    sounding = new SoundingImpl(field, 0, 1, -1, -1);
	    display.setSounding(sounding);
	    // java.lang.Thread.sleep(5000);
	}
    }
}
