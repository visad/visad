/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Hodograph3D.java,v 1.2 1999-01-07 18:13:33 steve Exp $
 */

package visad.meteorology;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import visad.ConstantMap;
import visad.CoordinateSystem;
import visad.DataReferenceImpl;
import visad.Display;
import visad.ErrorEstimate;
import visad.FlatField;
import visad.FlowControl;
import visad.FunctionType;
import visad.Gridded1DSet;
import visad.Gridded3DSet;
import visad.Real;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.ScalarMapEvent;
import visad.ScalarMapListener;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.DisplayImplJ3D;


/**
 * A VisAD display that provides support for viewing a wind profile as a 
 * 3-D hodograph.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
Hodograph3D
    implements	Serializable
{
    /**
     * The display.
     */
    private final DisplayImplJ3D		display;

    /**
     * The display renderer.
     */
    private final HodographDisplayRenderer3D	displayRenderer;

    /**
     * The wind profile.
     */
    private WindProfileImpl			windProfile;

    /**
     * The velocity profile property.
     */
    private final DataReferenceImpl		windProfileRef;

    /**
     * The type of the range of the flow profile.
     */
    private final RealTupleType			flowRangeType;

    /**
     * The flow profile data reference.
     */
    private final DataReferenceImpl		flowProfileRef;

    /**
     * ScalarMap for U-component.
     */
    private final ScalarMap			uMap;

    /**
     * ScalarMap for V-component.
     */
    private final ScalarMap			vMap;


    /**
     * Constructs from nothing.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    public
    Hodograph3D()
	throws VisADException, RemoteException
    {
	displayRenderer = new HodographDisplayRenderer3D();

	windProfile = new WindProfileImpl();
	windProfileRef = new DataReferenceImpl("windProfileRef");
	windProfileRef.setData(windProfile);

	FunctionType	windProfileType = (FunctionType)windProfile.getType();
	RealTupleType	windProfileRangeType =
	    (RealTupleType)windProfileType.getRange();
	RealType	uFlowType =
	    newFlowType((RealType)windProfileRangeType.getComponent(0));
	RealType	vFlowType =
	    newFlowType((RealType)windProfileRangeType.getComponent(1));
	flowRangeType = new RealTupleType(uFlowType, vFlowType);

	FlatField	flowProfile =
	    newFlowProfile(flowRangeType, windProfile);
	flowProfileRef = new DataReferenceImpl("flowProfileRef");
	flowProfileRef.setData(flowProfile);

	display = new DisplayImplJ3D("Wind Profile Hodograph", displayRenderer);

	uMap = new ScalarMap(windProfile.U_TYPE, displayRenderer.U);
	vMap = new ScalarMap(windProfile.V_TYPE, displayRenderer.V);
	display.addMap(uMap);
	display.addMap(vMap);
	uMap.setRangeByUnits();
	vMap.setRangeByUnits();
	ScalarMap	pressureMap =
	    new ScalarMap(windProfile.DOMAIN_TYPE, displayRenderer.PRESSURE);
	pressureMap.setRangeByUnits();
	display.addMap(pressureMap);

	final ScalarMap	uFlowMap = new ScalarMap(
	    (RealType)flowRangeType.getComponent(0), Display.Flow1X);
	final ScalarMap	vFlowMap = new ScalarMap(
	    (RealType)flowRangeType.getComponent(1), Display.Flow1Y);

	Real	maximumSpeed = displayRenderer.getMaximumSpeed();
	double	maxSpeed =
	    maximumSpeed.getValue(windProfile.U_TYPE.getDefaultUnit());
	uFlowMap.setRange(-maxSpeed, maxSpeed);
	vFlowMap.setRange(-maxSpeed, maxSpeed);

	display.addMap(uFlowMap);
	display.addMap(vFlowMap);
	((FlowControl)uFlowMap.getControl()).setFlowScale(5f);
	((FlowControl)vFlowMap.getControl()).setFlowScale(5f);

	display.addReference(
	    windProfileRef,
	    new ConstantMap[] {
		new ConstantMap(2.0, Display.LineWidth),
		new ConstantMap(1., Display.Red),
		new ConstantMap(0., Display.Blue),
		new ConstantMap(1., Display.Green)});
	    // new DirectManipulationRendererJ3D(),	doesn't work with Set-s

	display.addReference(
	    flowProfileRef,
	    new ConstantMap[] {
		new ConstantMap(2.0, Display.LineWidth),
		new ConstantMap(1., Display.Red),
		new ConstantMap(0., Display.Blue),
		new ConstantMap(0.5, Display.Green)});
    }


    /**
     * Creates a FlatField from a wind profile that is suitable for use
     * as a "flow" indicator.
     * @param flowRangeType	The type of the range of the new FlatField.
     * @param windProfile	The original wind profile.
     * @return			Essentially <code>windProfile</code> with the
     *				same data but with the type of the range
     *				changed to <code>flowRangeType</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    protected static FlatField
    newFlowProfile(RealTupleType flowRangeType, WindProfileImpl windProfile)
	throws VisADException, RemoteException
    {
	FunctionType	oldFuncType = (FunctionType)windProfile.getType();
	RealTupleType	oldRangeType = (RealTupleType)oldFuncType.getRange();
	FlatField	flowProfile =
	    new FlatField(
		new FunctionType(oldFuncType.getDomain(), flowRangeType),
		windProfile.getDomainSet());
	flowProfile.setSamples(windProfile.getValues(), false);
	return flowProfile;
    }


    /**
     * Gets the RealType of a "flow" component.
     * @param type		The type of the original component.
     * @return			The type of the new "flow" component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static RealType
    newFlowType(RealType oldType)
	throws VisADException
    {
	String		newName = oldType.getName() + "Flow";
	RealType	newType = RealType.getRealTypeByName(newName);
	if (newType == null)
	{
	    newType = new RealType(
		newName, oldType.getDefaultUnit(), oldType.getDefaultSet());
	}
	return newType;
    }


    /**
     * Sets the wind profile property from a sounding.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized void
    setSounding(SoundingImpl sounding)
	throws RemoteException, VisADException
    {
	WindProfileImpl	profile = (WindProfileImpl)sounding.getWindProfile();
	windProfileRef.setData(profile);
	windProfile = profile;
	displayRenderer.setWindProfile(profile);

	FlatField	flowProfile =
	    newFlowProfile(flowRangeType, windProfile);
	flowProfileRef.setData(flowProfile);
    }


    /**
     * Gets the AWT component corresponding of the VisAD display.
     *
     * @return			The AWT component corresponding of the VisAD
     *				display.
     */
    public Component
    getComponent()
    {
	return display.getComponent();
    }


    /**
     * Gets the pressure at the cursor position.
     * @return			The pressure at the cursor position.
     */
    public Real
    getCursorPressure()
    {
	return displayRenderer.getCursorPressure();
    }


    /**
     * Gets the wind speed at the cursor position.
     * @return			The wind speed at the cursor position.
     */
    public Real
    getCursorSpeed()
    {
	return displayRenderer.getCursorSpeed();
    }


    /**
     * Gets the wind direction at the cursor position.
     * @return			The wind direction at the cursor position.
     */
    public Real
    getCursorDirection()
    {
	return displayRenderer.getCursorDirection();
    }


    /**
     * Gets the profile wind speed at a given pressure.
     * @param pressure		The pressure at which to get the profile
     *				wind speed.
     * @return			The profile wind speed at <code>pressure
     *				</code>.
     */
    public Real
    getProfileSpeed(Real pressure)
    {
	Real	value;
	try
	{
	    value = windProfile.getSpeed(pressure);
	}
	catch (Exception e)
	{
	    value = null;
	}
	return value;
    }


    /**
     * Gets the profile wind direction at a given pressure.
     * @param pressure		The pressure at which to get the profile
     *				wind direction.
     * @return			The profile wind direction at <code>pressure
     *				</code>.
     */
    public Real
    getProfileDirection(Real pressure)
    {
	Real	value;
	try
	{
	    value = windProfile.getDirection(pressure);
	}
	catch (Exception e)
	{
	    value = null;
	}
	return value;
    }


    /**
     * Adds a listener for changes to the cursor pressure.
     * @param listener		The change listener.
     */
    public void
    addCursorPressureChangeListener(PropertyChangeListener listener)
    {
	displayRenderer.addCursorPressureChangeListener(listener);
    }


    /**
     * Tests this class by creating a diagram and displaying a wind
     * profile on it.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	JFrame	jframe = new JFrame("Wind Profile Hodograph");
	jframe.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {System.exit(0);}
	});

	Hodograph3D	bean = new Hodograph3D();
	jframe.getContentPane().add(bean.getComponent());

	jframe.pack();
	jframe.setVisible(true);

	Plain		plain = new Plain();
	FlatField	field = (FlatField)plain.open("sounding.nc");
	SoundingImpl	sounding = new SoundingImpl(field, 0, 1, 3, 2);
	bean.setSounding(sounding);
    }
}
