/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NoSuchUnitException.java,v 1.2 2001-11-27 22:29:42 dglo Exp $
 */

package visad.data.units;


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
