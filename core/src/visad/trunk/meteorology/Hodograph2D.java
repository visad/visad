/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Hodograph2D.java,v 1.3 1998-11-16 18:23:48 steve Exp $
 */

package visad.meteorology;

import com.sun.java.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.rmi.RemoteException;
import visad.ConstantMap;
import visad.CoordinateSystem;
import visad.DataReferenceImpl;
import visad.Display;
import visad.ErrorEstimate;
import visad.FlatField;
import visad.Gridded3DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.java2d.DisplayImplJ2D;


/**
 * A Bean that provides support for viewing a wind profile as a 2-D hodograph.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
Hodograph2D
    implements	Serializable
{
    /**
     * The velocity profile property.
     */
    private final DataReferenceImpl		windProfileRef;

    /**
     * Supports property changes.
     */
    private final PropertyChangeSupport		changes;


    /**
     * Constructs from nothing.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Java RMI failure.
     */
    public
    Hodograph2D()
	throws VisADException, RemoteException
    {
	HodographDisplayRenderer2D	displayRenderer =
	    new HodographDisplayRenderer2D();

	windProfileRef = new DataReferenceImpl("windProfileRef");
	windProfileRef.setData(new WindProfileImpl());

	changes = new PropertyChangeSupport(this);

	DisplayImplJ2D	display =
	    new DisplayImplJ2D("Wind Profile Hodograph", displayRenderer);
	{
	    JFrame	jframe = new JFrame("Wind Profile Hodograph");

	    jframe.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
	    });
	    jframe.setSize(512, 512);
	    jframe.setVisible(true);

	    jframe.getContentPane().add(display.getComponent());
	}
	display.addMap(new ScalarMap(WindProfileImpl.U_TYPE,
	    displayRenderer.U));
	display.addMap(new ScalarMap(WindProfileImpl.V_TYPE, 
	    displayRenderer.V));
	display.addMap(new ScalarMap(CommonTypes.W, displayRenderer.Z));
	display.addReference(
	    windProfileRef,
	    new ConstantMap[] {new ConstantMap(2.0, Display.LineWidth)});
	    // new DirectManipulationRendererJ2D(),	doesn't work with Set-s
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
	float[][]	uvValues = Set.doubleToFloat(profile.getValues());
	float[]		u = uvValues[0];
	float[]		v = uvValues[1];
	int		npts = u.length;
	float[]		w = new float[npts];
	Unit[]		units = profile.getDefaultRangeUnits();
	Set		set = new Gridded3DSet(
	    new RealTupleType(
		WindProfileImpl.U_TYPE, WindProfileImpl.V_TYPE, CommonTypes.W),
	    new float[][] {u, v, w}, npts,
	    (CoordinateSystem)null,
	    new Unit[] {units[0], units[1], CommonTypes.W.getDefaultUnit()},
	    (ErrorEstimate[])null);
	windProfileRef.setData(set);
    }


    /**
     * Tests this class by creating a diagram and displaying a wind
     * profile on it.
     */
    public static void
    main(String[] args)
	throws Exception
    {
	Hodograph2D	display = new Hodograph2D();
	Plain		plain = new Plain();
	FlatField	field = (FlatField)plain.open("sounding.nc");
	SoundingImpl	sounding = new SoundingImpl(field, 0, 1, 3, 2);
	display.setSounding(sounding);
    }
}
