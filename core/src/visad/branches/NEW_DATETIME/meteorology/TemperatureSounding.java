/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TemperatureSounding.java,v 1.3 1999-01-07 16:13:20 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.Gridded1DSet;
import visad.Real;
import visad.Unit;
import visad.VisADException;


public interface
TemperatureSounding
    extends	Sounding
{
    /**
     * Gets the sounding temperature at a given pressure.
     * @param pressure		The pressure at which to get the temperature.
     * @return			The temperature at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getTemperature(Real pressure)
	throws VisADException, RemoteException;
}
