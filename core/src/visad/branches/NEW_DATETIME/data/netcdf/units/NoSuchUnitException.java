/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NoSuchUnitException.java,v 1.3 1998-02-23 15:58:58 steve Exp $
 */

package visad.data.netcdf.units;


import visad.VisADException;


/**
 * Exception thrown when a unit specification can't be parsed because of an
 * unknown unit.
 */
public class
NoSuchUnitException
    extends ParseException
{
    /**
     * Construct an exception with a message.
     */
    public NoSuchUnitException(String msg)
    {
	super(msg);
    }
}
