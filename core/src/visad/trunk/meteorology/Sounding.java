/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Sounding.java,v 1.5 1998-11-16 18:23:49 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Quantity;


/**
 * Provides support for atmospheric soundings.
 *
 * @author Steven R. Emmerson
 */
public interface
Sounding
{
    /**
     * Gets the type of the domain variable.
     */
    public Quantity
    getDomainType();


    /**
     * Gets the type of the range.
     */
    public MathType
    getRangeType();


    /**
     * Gets a component, single-parameter sounding of the specified type.
     *
     * @return			The single parameter sounding or <code>null
     *				</code> if no such component.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public SingleSounding
    getSingleSounding(MathType type)
	throws VisADException, RemoteException;


    /**
     * Gets the temperature sounding.
     *
     * @return			The temperature sounding or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public TemperatureSounding
    getTemperatureSounding()
	throws VisADException, RemoteException;


    /**
     * Gets the dew-point sounding.
     *
     * @return			The dew-point sounding or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public DewPointSounding
    getDewPointSounding()
	throws VisADException, RemoteException;


    /**
     * Gets the wind profile.
     *
     * @return			The wind profile or <code>null</code>
     *				if none exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws RemoteException	Remote data access failure.
     */
    public WindProfile
    getWindProfile()
	throws VisADException, RemoteException;
}
