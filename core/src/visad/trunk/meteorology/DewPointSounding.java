/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DewPointSounding.java,v 1.3 1999-01-07 16:13:16 steve Exp $
 */

package visad.meteorology;

import java.rmi.RemoteException;
import visad.Gridded1DSet;
import visad.Real;
import visad.Unit;
import visad.VisADException;


public interface
DewPointSounding
    extends	Sounding
{
    /**
     * Gets the sounding dew point at a given pressure.
     * @param pressure		The pressure at which to get the dew point.
     * @return			The dew point at <code>pressure</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Real
    getDewPoint(Real pressure)
	throws VisADException, RemoteException;
}
