/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DewPointSounding.java,v 1.1 1998-10-28 17:16:47 steve Exp $
 */

package visad.meteorology;

import visad.FlatField;
import visad.FunctionType;
import visad.RealType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for temperature soundings.
 *
 * @author Steven R. Emmerson
 */
public interface
DewPointSounding
    extends	Sounding
{
    public final RealType	DEFAULT_RANGE_TYPE = null;
    public final RealType	DEFAULT_DOMAIN_TYPE = null;
}
