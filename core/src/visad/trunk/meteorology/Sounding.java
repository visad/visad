/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Sounding.java,v 1.3 1998-10-28 17:16:49 steve Exp $
 */

package visad.meteorology;

import visad.CoordinateSystem;
import visad.FlatField;
import visad.FunctionType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for atmospheric soundings.
 *
 * @author Steven R. Emmerson
 */
public interface
Sounding
{
    /**
     * Gets the temperature sounding.  Should be overridden by temperature
     * soundings.
     *
     * @return			<code>null</code>, always.
     */
    public FlatField
    getTemperatureSounding();


    /**
     * Gets the dew-point sounding.  Should be overridden by dew-point
     * soundings.
     *
     * @return			<code>null</code>, always.
     */
    public FlatField
    getDewPointSounding();


    /**
     * Gets the wind profile.  Should be overridden by wind profiles.
     *
     * @return			<code>null</code>, always.
     */
    public FlatField
    getWindProfile();
}
