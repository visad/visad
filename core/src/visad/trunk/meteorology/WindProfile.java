/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: WindProfile.java,v 1.3 1999-01-07 16:13:21 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.Real;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for vertical wind profiles.
 *
 * Instances are mutable.
 *
 * @author Steven R. Emmerson
 */
public interface
WindProfile
    extends	Sounding
{
    /**
     * Gets the profile wind speed at a pressure.
     * @param pressure		The pressure at which to get the wind speed.
     * @return			The wind speed at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getSpeed(Real pressure)
	throws VisADException, RemoteException;


    /**
     * Gets the profile wind direction at a pressure.
     * @param pressure		The pressures at which to get the wind 
     *				direction.
     * @return			The wind direction at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getDirection(Real pressure)
	throws VisADException, RemoteException;
}
