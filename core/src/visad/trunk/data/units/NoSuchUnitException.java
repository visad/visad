/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NoSuchUnitException.java,v 1.1 2000-11-17 18:54:45 dglo Exp $
 */

package visad.data.units;


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
